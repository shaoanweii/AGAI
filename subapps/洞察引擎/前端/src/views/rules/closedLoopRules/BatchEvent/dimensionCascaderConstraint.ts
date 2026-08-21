import type { BatchCascaderOption } from './types'

export interface BatchCascaderNodeMeta {
  level: number
  parentValue: string | null
  rootValue: string
  path: string[]
}

export interface BatchCascaderConstraintConfig {
  sameLevelOnly: boolean
  sameParentOnly: boolean
  sameRootOnly: boolean
  multipleLimit: number
  checkStrictly: boolean
  shouldUseBranchStateFallback: boolean
}

export interface BatchCascaderConstraintState {
  selectedValues: string[]
  selectedLevel: number
  selectedParentValue: string | null
  selectedRootValue: string | null
}

export type BatchCascaderValidationReason = 'limit' | 'level' | 'root' | 'parent'

/**
 * 为级联节点建立 value -> 元数据索引，统一支撑层级与父级约束判断。
 * @param options 级联选项
 * @param level 当前层级
 * @param parentValue 当前节点父值
 * @param parentPath 当前节点祖先路径
 * @param bucket 节点索引容器
 * @returns Map<string, BatchCascaderNodeMeta>
 */
export const buildBatchCascaderMetaMap = (
  options: BatchCascaderOption[] = [],
  level = 1,
  parentValue: string | null = null,
  parentPath: string[] = [],
  bucket = new Map<string, BatchCascaderNodeMeta>()
) => {
  options.forEach(item => {
    const currentPath = [...parentPath, item.value]

    bucket.set(item.value, {
      level,
      parentValue,
      rootValue: currentPath[0] || item.value,
      path: currentPath
    })

    if (Array.isArray(item.children) && item.children.length) {
      buildBatchCascaderMetaMap(item.children, level + 1, item.value, currentPath, bucket)
    }
  })

  return bucket
}

/**
 * 统计当前选中值所在的层级集合，层级超过一种时说明出现跨级选择。
 * @param values 当前已选值
 * @param cascaderMetaMap 节点层级索引
 * @returns number[]
 */
export const getBatchCascaderSelectionLevels = (
  values: string[],
  cascaderMetaMap: Map<string, BatchCascaderNodeMeta>
) => {
  return Array.from(
    new Set(
      values.map(item => cascaderMetaMap.get(item)?.level).filter((item): item is number => !!item)
    )
  )
}

/**
 * 计算当前选中项的父级集合，用于限制同层级下只能选择同一父节点的子项。
 * 一级节点统一归为 ROOT，保证顶层节点仍可并列多选。
 * @param values 当前已选值
 * @param cascaderMetaMap 节点元数据索引
 * @returns string[]
 */
export const getBatchCascaderSelectionParents = (
  values: string[],
  cascaderMetaMap: Map<string, BatchCascaderNodeMeta>
) => {
  return Array.from(
    new Set(
      values
        .map(item => cascaderMetaMap.get(item)?.parentValue ?? 'ROOT')
        .filter(parentValue => !!parentValue)
    )
  )
}

/**
 * 体验代码要求同一一级分类下混选，因此这里统一提取选中项的根节点集合。
 * @param values 当前已选值
 * @param cascaderMetaMap 节点元数据索引
 * @returns string[]
 */
export const getBatchCascaderSelectionRoots = (
  values: string[],
  cascaderMetaMap: Map<string, BatchCascaderNodeMeta>
) => {
  return Array.from(
    new Set(
      values
        .map(item => cascaderMetaMap.get(item)?.rootValue)
        .filter((rootValue): rootValue is string => !!rootValue)
    )
  )
}

/**
 * 基于当前选中值计算约束状态，供禁用态和合法性校验共同复用。
 * @param selectedValues 当前已选值
 * @param cascaderMetaMap 节点元数据索引
 * @returns BatchCascaderConstraintState
 */
export const resolveBatchCascaderConstraintState = (
  selectedValues: string[],
  cascaderMetaMap: Map<string, BatchCascaderNodeMeta>
): BatchCascaderConstraintState => {
  const levels = getBatchCascaderSelectionLevels(selectedValues, cascaderMetaMap)
  const parents = getBatchCascaderSelectionParents(selectedValues, cascaderMetaMap)
  const roots = getBatchCascaderSelectionRoots(selectedValues, cascaderMetaMap)

  return {
    selectedValues,
    selectedLevel: levels[0] || 0,
    selectedParentValue: parents[0] || null,
    selectedRootValue: roots[0] || null
  }
}

/**
 * 判断当前选中值是否满足字段级联约束，非法时返回首个命中的失败原因。
 * @param selectedValues 当前已选值
 * @param cascaderMetaMap 节点元数据索引
 * @param config 约束配置
 * @returns BatchCascaderValidationReason | ''
 */
export const resolveBatchCascaderValidationReason = (
  selectedValues: string[],
  cascaderMetaMap: Map<string, BatchCascaderNodeMeta>,
  config: Pick<
    BatchCascaderConstraintConfig,
    'sameLevelOnly' | 'sameParentOnly' | 'sameRootOnly' | 'multipleLimit'
  >
) => {
  const levels = getBatchCascaderSelectionLevels(selectedValues, cascaderMetaMap)
  const parents = getBatchCascaderSelectionParents(selectedValues, cascaderMetaMap)
  const roots = getBatchCascaderSelectionRoots(selectedValues, cascaderMetaMap)

  if (config.multipleLimit > 0 && selectedValues.length > config.multipleLimit) {
    return 'limit'
  }

  if (config.sameLevelOnly && levels.length > 1) {
    return 'level'
  }

  if (config.sameRootOnly && roots.length > 1) {
    return 'root'
  }

  if (config.sameParentOnly && parents.length > 1) {
    return 'parent'
  }

  return ''
}

/**
 * 按当前已选层级和数量限制动态禁用级联节点，尽量在不影响展开浏览的前提下降低误选概率。
 * `checkStrictly: false` 的数据源场景需要额外保留可展开分支。
 * @param options 原始级联选项
 * @param cascaderMetaMap 节点元数据索引
 * @param state 当前选中状态
 * @param config 约束配置
 * @returns BatchCascaderOption[]
 */
export const buildBatchConstrainedCascaderOptions = (
  options: BatchCascaderOption[] = [],
  cascaderMetaMap: Map<string, BatchCascaderNodeMeta>,
  state: BatchCascaderConstraintState,
  config: BatchCascaderConstraintConfig
): BatchCascaderOption[] => {
  const selectedSet = new Set(state.selectedValues)
  const reachedLimit =
    config.multipleLimit > 0 && state.selectedValues.length >= config.multipleLimit
  const selectedParentPath =
    config.sameParentOnly && state.selectedParentValue
      ? cascaderMetaMap.get(state.selectedParentValue)?.path ?? []
      : []
  const selectedNavigationValues = config.shouldUseBranchStateFallback
    ? new Set(
        state.selectedValues.flatMap(value => {
          const path = cascaderMetaMap.get(value)?.path || []
          return path.slice(0, -1)
        })
      )
    : new Set<string>()

  const mapNode = (item: BatchCascaderOption): BatchCascaderOption => {
    const itemMeta = cascaderMetaMap.get(item.value)
    const itemLevel = itemMeta?.level || 1
    const isSelected = selectedSet.has(item.value)
    const hasChildren = Array.isArray(item.children) && item.children.length > 0
    const isSelectableNode = config.checkStrictly || !hasChildren
    const nextChildren = hasChildren ? item.children!.map(child => mapNode(child)) : undefined
    const canKeepForNavigation =
      state.selectedLevel > 0 &&
      itemLevel < state.selectedLevel &&
      hasChildren &&
      (!config.sameParentOnly || selectedParentPath.includes(item.value)) &&
      (!config.sameRootOnly ||
        !state.selectedRootValue ||
        itemMeta?.rootValue === state.selectedRootValue)
    const disabledByLevel =
      state.selectedLevel > 0 &&
      !isSelected &&
      !canKeepForNavigation &&
      itemLevel !== state.selectedLevel
    const disabledByParent =
      config.sameParentOnly &&
      state.selectedLevel > 1 &&
      itemLevel === state.selectedLevel &&
      !isSelected &&
      (itemMeta?.parentValue ?? null) !== state.selectedParentValue
    const disabledByRoot =
      config.sameRootOnly &&
      !!state.selectedRootValue &&
      !isSelected &&
      itemMeta?.rootValue !== state.selectedRootValue
    const hasInteractiveChild = !!nextChildren?.some(child => !child.disabled)
    const disabledByLimit = config.shouldUseBranchStateFallback
      ? reachedLimit && !isSelected && isSelectableNode && !selectedNavigationValues.has(item.value)
      : reachedLimit && !isSelected
    const disabledByChildState =
      config.shouldUseBranchStateFallback &&
      hasChildren &&
      !isSelected &&
      !selectedNavigationValues.has(item.value) &&
      !hasInteractiveChild
    const disabled =
      config.shouldUseBranchStateFallback && hasChildren
        ? !!item.disabled ||
          disabledByLevel ||
          disabledByParent ||
          disabledByRoot ||
          disabledByChildState
        : !!item.disabled ||
          disabledByLevel ||
          disabledByParent ||
          disabledByRoot ||
          disabledByLimit

    return {
      ...item,
      disabled,
      children: nextChildren
    }
  }

  return options.map(item => mapNode(item))
}
