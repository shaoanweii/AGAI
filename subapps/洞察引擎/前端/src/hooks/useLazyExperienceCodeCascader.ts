import { computed, ref } from 'vue'

export interface ExperienceCodeSourceNode {
  id?: string | number
  tagCode?: string | number
  tagName?: string
  child?: ExperienceCodeSourceNode[] | null
  [key: string]: any
}

export interface ExperienceCodeCacheNode extends ExperienceCodeSourceNode {
  id: string
  tagCode: string
  tagName: string
  value: string
  label: string
  level: number
  leaf: boolean
  pathIds: string[]
  pathCodes: string[]
  pathLabels: string[]
  child: ExperienceCodeCacheNode[]
  children: ExperienceCodeCacheNode[]
}

export interface ExperienceCodePanelOption {
  id: string
  tagCode: string
  tagName: string
  value: string
  label: string
  level: number
  leaf: boolean
  pathIds: string[]
  pathCodes: string[]
  pathLabels: string[]
  children?: ExperienceCodePanelOption[]
}

interface CascaderLazyNode {
  level: number
  data?: {
    value?: string
    label?: string
  }
  text?: string
  label?: string
  calcText?: (allLevels: boolean, separator: string) => string
}

/**
 * @description: 统一清洗级联选中结果，并在父子同时命中时仅保留最末级路径。
 * @param {unknown} selection el-cascader 当前回传的多选路径
 * @return {string[][]} 清洗后的末级选中路径
 */
export const normalizeExperienceCodeSelectionPaths = (selection: unknown): string[][] => {
  if (!Array.isArray(selection)) return []

  const uniquePaths = Array.from(
    new Map(
      selection
        .map(path => {
          if (!Array.isArray(path)) return null
          const normalizedPath = path.map(item => normalizeText(item)).filter(Boolean)
          if (!normalizedPath.length) return null
          return [normalizedPath.join('__CASCADER_PATH__'), normalizedPath] as const
        })
        .filter(Boolean) as Array<readonly [string, string[]]>
    ).values()
  )

  return uniquePaths.filter((currentPath, currentIndex) => {
    return !uniquePaths.some((candidatePath, candidateIndex) => {
      if (candidateIndex === currentIndex || candidatePath.length <= currentPath.length) {
        return false
      }

      return currentPath.every((pathId, levelIndex) => pathId === candidatePath[levelIndex])
    })
  })
}

/**
 * @description: 统一清洗体验代码树中的文本字段，避免空格和空值影响级联节点匹配。
 * @param {unknown} value 原始字段值
 * @return {string} 规范化后的文本
 */
const normalizeText = (value: unknown) => String(value ?? '').trim()

/**
 * @description: 构建体验代码级联懒加载能力，首屏仅挂载当前可见层级，搜索时再补齐整棵树。
 * @return {*}
 */
export const useLazyExperienceCodeCascader = () => {
  const cascaderOptions = ref<ExperienceCodePanelOption[]>([])
  const cacheTree = ref<ExperienceCodeCacheNode[]>([])
  const nodeMap = ref<Record<string, ExperienceCodeCacheNode>>({})
  const allNodesLoaded = ref(false)

  /**
   * @description: 将接口原始树转换为带路径信息的缓存树，并同步维护节点索引。
   * @param {ExperienceCodeSourceNode[]} nodes 原始树节点
   * @param {number} level 当前层级
   * @param {string[]} parentIds 父级 id 路径
   * @param {string[]} parentCodes 父级 code 路径
   * @param {string[]} parentLabels 父级名称路径
   * @return {ExperienceCodeCacheNode[]} 缓存树节点
   */
  const buildCacheTree = (
    nodes: ExperienceCodeSourceNode[],
    level = 1,
    parentIds: string[] = [],
    parentCodes: string[] = [],
    parentLabels: string[] = []
  ): ExperienceCodeCacheNode[] => {
    return (Array.isArray(nodes) ? nodes : [])
      .map(node => {
        const id = normalizeText(node?.id)
        const tagCode = normalizeText(node?.tagCode)
        const tagName = normalizeText(node?.tagName)
        if (!id || !tagCode || !tagName) return null

        const pathIds = [...parentIds, id]
        const pathCodes = [...parentCodes, tagCode]
        const pathLabels = [...parentLabels, tagName]
        const child = buildCacheTree(
          Array.isArray(node?.child) ? node.child : [],
          level + 1,
          pathIds,
          pathCodes,
          pathLabels
        )
        const cacheNode: ExperienceCodeCacheNode = {
          ...node,
          id,
          tagCode,
          tagName,
          value: id,
          label: tagName,
          level,
          leaf: child.length === 0,
          pathIds,
          pathCodes,
          pathLabels,
          child,
          children: child
        }

        nodeMap.value[id] = cacheNode
        return cacheNode
      })
      .filter(Boolean) as ExperienceCodeCacheNode[]
  }

  /**
   * @description: 按级联面板当前需要的深度裁剪节点，避免首次渲染一次性挂载完整 children。
   * @param {ExperienceCodeCacheNode} node 缓存树节点
   * @param {boolean} includeChildren 是否递归挂载全部子节点
   * @return {ExperienceCodePanelOption} 面板节点
   */
  const createPanelOption = (
    node: ExperienceCodeCacheNode,
    includeChildren = false
  ): ExperienceCodePanelOption => {
    return {
      id: node.id,
      tagCode: node.tagCode,
      tagName: node.tagName,
      value: node.value,
      label: node.label,
      level: node.level,
      leaf: node.leaf,
      pathIds: node.pathIds,
      pathCodes: node.pathCodes,
      pathLabels: node.pathLabels,
      children: includeChildren
        ? node.child.map(childNode => createPanelOption(childNode, true))
        : undefined
    }
  }

  /**
   * @description: 用新的体验代码树刷新级联缓存，并回退到仅首列节点的懒加载展示模式。
   * @param {ExperienceCodeSourceNode[]} nodes 原始树节点
   * @return {*}
   */
  const setSourceTree = (nodes: ExperienceCodeSourceNode[]) => {
    nodeMap.value = {}
    cacheTree.value = buildCacheTree(nodes)
    cascaderOptions.value = cacheTree.value.map(node => createPanelOption(node))
    allNodesLoaded.value = false
  }

  /**
   * @description: 级联懒加载回调，从缓存树按需补齐当前展开节点的下一层 children。
   * @param {CascaderLazyNode} node 当前展开节点
   * @param {(data: ExperienceCodePanelOption[]) => void} resolve 子节点回调
   * @return {*}
   */
  const lazyLoad = (
    node: CascaderLazyNode,
    resolve: (data: ExperienceCodePanelOption[]) => void
  ) => {
    if (node.level === 0) {
      resolve(cascaderOptions.value)
      return
    }

    const parentValue = normalizeText(node?.data?.value)
    const parentNode = nodeMap.value[parentValue]
    resolve((parentNode?.child || []).map(childNode => createPanelOption(childNode)))
  }

  /**
   * @description: 搜索前补齐完整树，兼顾大数据量首屏渲染和级联面板全量搜索能力。
   * @param {string} keyword 搜索关键字
   * @return {boolean} 是否继续执行筛选
   */
  const beforeFilter = (keyword: string) => {
    if (!normalizeText(keyword)) return true

    if (!allNodesLoaded.value) {
      cascaderOptions.value = cacheTree.value.map(node => createPanelOption(node, true))
      allNodesLoaded.value = true
    }
    return true
  }

  /**
   * @description: 搜索时仅按当前节点名称匹配，避免完整路径文本干扰用户输入。
   * @param {CascaderLazyNode} node 级联节点实例
   * @param {string} keyword 搜索关键字
   * @return {boolean} 是否命中
   */
  const filterMethod = (node: CascaderLazyNode, keyword: string) => {
    const normalizedKeyword = normalizeText(keyword).toLowerCase()
    const currentLabel = normalizeText(node?.text || node?.label || node?.data?.label).toLowerCase()
    return currentLabel.includes(normalizedKeyword)
  }

  /**
   * @description: 统一格式化搜索建议项路径，解决同名体验代码难以区分的问题。
   * @param {CascaderLazyNode} node 搜索建议节点
   * @return {string} 展示路径
   */
  const formatSuggestionPath = (node: CascaderLazyNode) => {
    return normalizeText(node?.calcText?.(true, ' / ') || node?.text || node?.label)
  }

  const cascaderProps = computed(() => {
    return {
      multiple: true,
      checkStrictly: true,
      emitPath: true,
      lazy: true,
      lazyLoad,
      value: 'value',
      label: 'label',
      children: 'children',
      leaf: 'leaf'
    }
  })

  return {
    cascaderOptions,
    nodeMap,
    cascaderProps,
    setSourceTree,
    beforeFilter,
    filterMethod,
    formatSuggestionPath
  }
}
