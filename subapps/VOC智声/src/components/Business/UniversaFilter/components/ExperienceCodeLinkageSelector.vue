<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref, shallowRef, watch } from 'vue'
import type { LabelTag } from '@/api/common/index.d'
import { getTagLibClientTree } from '@/api/common'
import { TagType } from '@/constants'
import { ElMessage } from 'element-plus'
import { EXPERIENCE_CODE_SAME_LEVEL_WARNING } from '../constants'

defineOptions({
  name: 'ExperienceCodeLinkageSelector'
})

type TagTypeOption = { key: string; value: string }
type TagTreeNode = LabelTag & Record<string, any>

interface Props {
  modelValue?: any[]
  tagType?: string
  typeOptions?: TagTypeOption[]
  disabled?: boolean
  defaultTagType?: string
  tyPageType?: string
  sameLevelOnly?: boolean
  fixedTagType?: string
  hideTagType?: boolean
  rootTagName?: string
  requestLevel?: number
  hideRootInCascader?: boolean
}

interface Emits {
  (e: 'update:modelValue', value: any[]): void
  (e: 'update:tagType', value: string): void
  (
    e: 'change',
    data: {
      lastLevelCodes: string[]
      lastLevelIds: string[]
      names: string[]
      source?: 'user'
    }
  ): void
}

type ChangePayload = {
  lastLevelCodes: string[]
  lastLevelIds: string[]
  names: string[]
  source?: 'user'
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false,
  sameLevelOnly: false,
  tyPageType: '',
  defaultTagType: 'CA',
  fixedTagType: '',
  hideTagType: false,
  rootTagName: '',
  requestLevel: 0,
  hideRootInCascader: false,
  typeOptions: () => []
})

const emit = defineEmits<Emits>()

const selectedTagType = ref<string>(
  props.tagType || props.fixedTagType || props.defaultTagType || 'CA'
)
const isFixedTagType = computed(() => !!props.fixedTagType || props.hideTagType)
const resolvedTypeOptions = computed(() => {
  const a = (props.typeOptions || []).filter(item => item?.key && item?.value)

  // if (props.tyPageType === 'newCarPage') {
  //   // 新车页面只展示与新车相关的体验代码类型
  //   return a.filter(item => item.key.startsWith('CA'))
  // }

  return a
})

// 树数据量大时，避免深度响应式带来的开销（el-cascader 只需要读数据，不依赖深层响应）
const tagOptions = shallowRef<TagTreeNode[]>([])
const hiddenRootNode = shallowRef<TagTreeNode | null>(null)
const cascaderValue = ref<string[][]>([])
const lastValidCascaderValue = ref<string[][]>([])

const tagTreeLoading = ref(false)
let tagTreeRequestSerial = 0

// 避免“回显 -> 触发 watch -> 再次 emit”导致的循环
const syncingFromModel = ref(false)

// 标记是否由用户交互触发（用于避免初始化回显时清空标准观点）
const userTriggered = ref(false)

type TagIndex = {
  nodeByCode: Map<string, LabelTag>
  pathByCode: Map<string, string[]>
}

// 为 el-cascader 大数据做索引缓存，避免频繁递归遍历整棵树导致卡顿
const tagIndex = shallowRef<TagIndex>({ nodeByCode: new Map(), pathByCode: new Map() })

/**
 * 复制级联路径，避免回滚值与当前 v-model 共用数组引用。
 * @param paths 当前级联路径数组
 * @returns 复制后的级联路径数组
 */
const cloneCascaderPaths = (paths: string[][]) => paths.map(path => [...path])

const getHiddenRootCode = () => hiddenRootNode.value?.tagCode || ''

const toRealPaths = (paths: string[][]) => {
  const hiddenRootCode = getHiddenRootCode()
  if (!props.hideRootInCascader || !hiddenRootCode) return paths
  return paths.map(path => (Array.isArray(path) ? [hiddenRootCode, ...path] : []))
}

const toDisplayPaths = (paths: string[][]) => {
  const hiddenRootCode = getHiddenRootCode()
  if (!props.hideRootInCascader || !hiddenRootCode) return paths
  return paths
    .filter(path => Array.isArray(path) && path[0] === hiddenRootCode && path.length > 1)
    .map(path => path.slice(1))
}

const getIndexOptions = () => {
  if (!props.hideRootInCascader || !hiddenRootNode.value) return tagOptions.value
  return [hiddenRootNode.value]
}

/**
 * 校验体验代码是否只选中了同一层级。
 * @param paths 级联组件选中的完整路径数组
 * @returns 是否未跨层级选择
 */
const isSameLevelSelection = (paths: string[][]) => {
  const selectedLevels = new Set<number>()
  paths.forEach(path => {
    if (Array.isArray(path) && path.length > 0) {
      selectedLevels.add(path.length)
    }
  })
  return selectedLevels.size <= 1
}

const rebuildTagIndex = (options: LabelTag[]) => {
  const nodeByCode = new Map<string, LabelTag>()
  const pathByCode = new Map<string, string[]>()

  const stack: Array<{ node: LabelTag; path: string[] }> = []
  ;(options || []).forEach(node => {
    if (node) stack.push({ node, path: [] })
  })

  while (stack.length > 0) {
    const item = stack.pop()
    if (!item) continue
    const { node, path } = item
    const code = node?.tagCode || ''
    if (!code) continue

    const curPath = [...path, code]
    nodeByCode.set(code, node)
    pathByCode.set(code, curPath)

    const children = Array.isArray(node.child) ? node.child : []
    for (let i = children.length - 1; i >= 0; i--) {
      const child = children[i]
      if (child) stack.push({ node: child, path: curPath })
    }
  }

  tagIndex.value = { nodeByCode, pathByCode }
}

const getNodeByCodeFast = (code: string) => {
  if (!code) return null
  return tagIndex.value.nodeByCode.get(code) || null
}

const getPathByCodeFast = (code: string) => {
  if (!code) return null
  return tagIndex.value.pathByCode.get(code) || null
}

const cascaderProps = computed(() => {
  return {
    value: 'tagCode',
    label: 'tagName',
    children: 'child',
    multiple: true,
    emitPath: true,
    checkStrictly: true
  } as const
})

const normalizeModelValueToLevelArrays = (value: any[] | undefined) => {
  const arr = Array.isArray(value) ? value : []
  const first = arr[0]
  const second = arr[1]
  const three = arr[2]
  const four = arr[3]

  const firstArr = first ? (Array.isArray(first) ? first : [first]) : []
  const secondArr = second ? (Array.isArray(second) ? second : [second]) : []
  const threeArr = three ? (Array.isArray(three) ? three : [three]) : []
  const fourArr = four ? (Array.isArray(four) ? four : [four]) : []

  return { firstArr, secondArr, threeArr, fourArr }
}

const uniqueArray = (arr: string[]) => Array.from(new Set(arr.filter(Boolean)))

const findNodeByCode = (options: LabelTag[], targetCode: string): LabelTag | null => {
  for (const option of options || []) {
    if (option.tagCode === targetCode) return option
    if (option.child && option.child.length > 0) {
      const found = findNodeByCode(option.child, targetCode)
      if (found) return found
    }
  }
  return null
}

const findPathByCode = (
  options: LabelTag[],
  targetCode: string,
  path: string[] = []
): string[] | null => {
  for (const node of options || []) {
    const curPath = [...path, node.tagCode || '']
    if (node.tagCode === targetCode) return curPath.filter(Boolean)
    if (node.child && node.child.length > 0) {
      const found = findPathByCode(node.child, targetCode, curPath)
      if (found) return found
    }
  }
  return null
}

// 体验代码类型选项由父组件统一请求 /report/drill-down/conditions 后透传
const syncSelectedTagTypeWithOptions = () => {
  if (props.tagType || resolvedTypeOptions.value.length === 0) {
    return
  }

  const exists = resolvedTypeOptions.value.some(item => item.key === selectedTagType.value)
  if (!exists) {
    selectedTagType.value = resolvedTypeOptions.value[0].key
  }
}

/**
 * 按标签体系加载体验代码树。
 * 仅最后一次请求可以更新选项，避免慢响应覆盖报告回显所需的新标签体系。
 * @param tagType 标签体系编码
 * @returns 本次请求结果是否已应用
 */
const loadTagTree = async (tagType: string) => {
  const requestSerial = ++tagTreeRequestSerial
  const requestTagType = props.fixedTagType || tagType
  if (!requestTagType) {
    tagOptions.value = []
    hiddenRootNode.value = null
    tagTreeLoading.value = false
    return true
  }

  try {
    tagTreeLoading.value = true
    const res = await getTagLibClientTree({
      tagLibType: requestTagType,
      ...(props.requestLevel ? { level: props.requestLevel } : {})
    })

    if (requestSerial !== tagTreeRequestSerial) return false

    const options = Array.isArray(res.result) ? (res.result as TagTreeNode[]) : []
    if (props.rootTagName) {
      const rootNode = options.find(item => item?.tagName === props.rootTagName) || null
      hiddenRootNode.value = props.hideRootInCascader ? rootNode : null
      tagOptions.value = props.hideRootInCascader
        ? ((rootNode?.child || []) as TagTreeNode[])
        : rootNode
          ? [rootNode]
          : []
    } else {
      hiddenRootNode.value = null
      tagOptions.value = options
    }

    return true
  } catch {
    if (requestSerial === tagTreeRequestSerial) {
      hiddenRootNode.value = null
      tagOptions.value = []
      return true
    }

    return false
  } finally {
    if (requestSerial === tagTreeRequestSerial) {
      tagTreeLoading.value = false
    }
  }
}

const buildModelValueFromPaths = (paths: string[][]) => {
  const l1: string[] = []
  const l2: string[] = []
  const l3: string[] = []
  const l4: string[] = []

  ;(paths || []).forEach(p => {
    if (!Array.isArray(p)) return
    if (p[0]) l1.push(p[0])
    if (p[1]) l2.push(p[1])
    if (p[2]) l3.push(p[2])
    if (p[3]) l4.push(p[3])
  })

  const firstArr = uniqueArray(l1)
  const secondArr = uniqueArray(l2)
  const threeArr = uniqueArray(l3)
  const fourArr = uniqueArray(l4)

  // 与旧版 ExperienceCodeSelector 保持一致：只推入“非空层级”，避免出现 [[],[],[],[]] 这种误判为“有值”的情况
  const result: any[] = []
  if (firstArr.length > 0) result.push(firstArr)
  if (secondArr.length > 0) result.push(secondArr)
  if (threeArr.length > 0) result.push(threeArr)
  if (fourArr.length > 0) result.push(fourArr)

  return { modelValue: result, firstArr, secondArr, threeArr, fourArr }
}

const buildPathsFromModelValue = (value: any[]) => {
  const { firstArr, secondArr, threeArr, fourArr } = normalizeModelValueToLevelArrays(value)
  // 隐藏固定根节点时只回显最深选中层级，避免把自动补齐的父级路径显示成已选节点。
  const selectedLevelCodes = props.hideRootInCascader
    ? fourArr.length
      ? fourArr
      : threeArr.length
        ? threeArr
        : secondArr.length
          ? secondArr
          : firstArr
    : [...firstArr, ...secondArr, ...threeArr, ...fourArr]
  const codes = uniqueArray(selectedLevelCodes)
  const paths: string[][] = []
  codes.forEach(code => {
    // 优先走缓存索引，避免每次回显都做全树递归
    const p =
      (props.hideRootInCascader ? null : getPathByCodeFast(code)) ||
      findPathByCode(getIndexOptions(), code)
    if (p && p.length > 0) paths.push(p)
  })
  // 去重：相同路径只保留一次
  const uniqueKey = new Set<string>()
  const dedup: string[][] = []
  paths.forEach(p => {
    const k = p.join('>')
    if (!uniqueKey.has(k)) {
      uniqueKey.add(k)
      dedup.push(p)
    }
  })
  return toDisplayPaths(dedup)
}

const findNodeInCurrentTree = (code: string) =>
  getNodeByCodeFast(code) || findNodeByCode(getIndexOptions(), code)

const shouldSkipDisplayCode = (code: string) =>
  props.hideRootInCascader && !!getHiddenRootCode() && code === getHiddenRootCode()

const buildLastLevelInfoFromLevelArrays = (levelArrays: {
  firstArr: string[]
  secondArr: string[]
  threeArr: string[]
  fourArr: string[]
}) => {
  const { firstArr, secondArr, threeArr, fourArr } = levelArrays

  let lastLevelCodesArray: string[] = []
  if (fourArr.length > 0) lastLevelCodesArray = fourArr
  else if (threeArr.length > 0) lastLevelCodesArray = threeArr
  else if (secondArr.length > 0) lastLevelCodesArray = secondArr
  else if (firstArr.length > 0) lastLevelCodesArray = firstArr

  if (!lastLevelCodesArray || lastLevelCodesArray.length === 0) {
    return { lastLevelCodes: [], lastLevelIds: [], names: [] }
  }

  const names: string[] = []
  ;[firstArr, secondArr, threeArr, fourArr].forEach(levelCodes => {
    levelCodes.forEach(code => {
      if (shouldSkipDisplayCode(code)) return
      const node = findNodeInCurrentTree(code)
      if (node?.tagName) names.push(node.tagName)
    })
  })

  const lastLevelIds: string[] = []
  lastLevelCodesArray.forEach(code => {
    const node = findNodeInCurrentTree(code)
    if (node?.id) lastLevelIds.push(node.id)
  })

  return { lastLevelCodes: [...lastLevelCodesArray], lastLevelIds, names }
}

const buildModelValueKey = (value: any[] | undefined) => {
  const { firstArr, secondArr, threeArr, fourArr } = normalizeModelValueToLevelArrays(value)
  const sortJoin = (arr: string[]) => [...arr].filter(Boolean).slice().sort().join(',')
  return [sortJoin(firstArr), sortJoin(secondArr), sortJoin(threeArr), sortJoin(fourArr)].join('|')
}

// 记录“本组件刚 emit 出去”的值，避免父组件回写后再次触发回显计算造成卡顿
const pendingSelfEmittedModelKey = ref<string | null>(null)

// change 事件会触发父组件请求标准观点，做一个轻量防抖提升交互手感
let changeEmitTimer: number | null = null
const emitChangeDebounced = (payload: ChangePayload) => {
  if (changeEmitTimer) window.clearTimeout(changeEmitTimer)
  changeEmitTimer = window.setTimeout(() => {
    emit('change', payload)
  }, 120)
}

// 获取末级信息：用于联动加载标准观点（以及筛选标签显示）
const getLastLevelInfo = () => {
  const { firstArr, secondArr, threeArr, fourArr } = normalizeModelValueToLevelArrays(
    props.modelValue
  )

  let lastLevelCodesArray: string[] = []
  if (fourArr.length > 0) lastLevelCodesArray = fourArr
  else if (threeArr.length > 0) lastLevelCodesArray = threeArr
  else if (secondArr.length > 0) lastLevelCodesArray = secondArr
  else if (firstArr.length > 0) lastLevelCodesArray = firstArr

  if (!lastLevelCodesArray || lastLevelCodesArray.length === 0) {
    return { lastLevelCodes: [], lastLevelIds: [], names: [] }
  }

  return buildLastLevelInfoFromLevelArrays({ firstArr, secondArr, threeArr, fourArr })
}

const getTagPath = (): Array<{ code: string; name: string; level: number }> => {
  const { firstArr, secondArr, threeArr, fourArr } = normalizeModelValueToLevelArrays(
    props.modelValue
  )
  const tagPath: Array<{ code: string; name: string; level: number }> = []

  firstArr.forEach(code => {
    if (shouldSkipDisplayCode(code)) return
    const node = findNodeInCurrentTree(code)
    if (node) tagPath.push({ code, name: node.tagName || '', level: 1 })
  })
  secondArr.forEach(code => {
    const node = findNodeInCurrentTree(code)
    if (node) tagPath.push({ code, name: node.tagName || '', level: 2 })
  })
  threeArr.forEach(code => {
    const node = findNodeInCurrentTree(code)
    if (node) tagPath.push({ code, name: node.tagName || '', level: 3 })
  })
  fourArr.forEach(code => {
    const node = findNodeInCurrentTree(code)
    if (node) tagPath.push({ code, name: node.tagName || '', level: 4 })
  })

  return tagPath
}

/**
 * 判断当前级联选择是否只包含同一层级。
 * 父组件用于查询前兜底校验，避免把路径里的父级节点误判成跨级选择。
 * @returns 是否只选中了同一层级
 */
const isSelectionSameLevel = () => isSameLevelSelection(toRealPaths(cascaderValue.value))

// 暴露给父组件（UniversaFilter）复用现有联动逻辑
defineExpose({
  getLastLevelInfo,
  getTagPath,
  isSelectionSameLevel,
  findNodeByCode,
  tagOptions
})

const onTagTypeChange = () => {
  userTriggered.value = true
}

const normalizeCascaderValueToPaths = (value: unknown): string[][] => {
  if (!Array.isArray(value)) return []
  if (value.length === 0) return []

  // multiple + emitPath 时通常是二维数组；部分情况下（类型推断/兼容）可能是单一路径
  const first = value[0] as unknown
  if (Array.isArray(first)) {
    return (value as unknown[]).map(p =>
      Array.isArray(p) ? (p.filter(Boolean).map(v => String(v)) as string[]) : []
    ) as string[][]
  }

  return [(value as unknown[]).filter(Boolean).map(v => String(v))]
}

const onCascaderChange = (val: unknown) => {
  // 注意：有些场景（回显）可能会触发 change，这里必须加保护避免循环
  if (syncingFromModel.value) return

  const displayPaths = normalizeCascaderValueToPaths(val)
  const paths = toRealPaths(displayPaths)
  if (props.sameLevelOnly && !isSameLevelSelection(paths)) {
    ElMessage.warning(EXPERIENCE_CODE_SAME_LEVEL_WARNING)
    syncingFromModel.value = true
    cascaderValue.value = cloneCascaderPaths(lastValidCascaderValue.value)
    nextTick(() => {
      syncingFromModel.value = false
    })
    return
  }

  lastValidCascaderValue.value = cloneCascaderPaths(displayPaths)
  const built = buildModelValueFromPaths(paths)
  pendingSelfEmittedModelKey.value = buildModelValueKey(built.modelValue)

  emit('update:modelValue', built.modelValue)

  // change 事件主要用于联动加载标准观点，默认认为来自用户交互
  const info = buildLastLevelInfoFromLevelArrays(built)
  emitChangeDebounced({ ...info, source: 'user' })
}

watch(
  () => props.tagType,
  v => {
    if (isFixedTagType.value) return
    if (v && v !== selectedTagType.value) {
      selectedTagType.value = v
    }
  }
)

watch(
  () => resolvedTypeOptions.value,
  () => {
    syncSelectedTagTypeWithOptions()
  },
  { immediate: true, deep: true }
)

watch(
  () => [props.fixedTagType, props.rootTagName, props.requestLevel] as const,
  async () => {
    if (!isFixedTagType.value) return
    selectedTagType.value = props.fixedTagType || props.defaultTagType || TagType.Domain
    const tagTreeReady = await loadTagTree(selectedTagType.value)
    if (!tagTreeReady) return
    await nextTick()
    syncingFromModel.value = true
    const paths = buildPathsFromModelValue(props.modelValue || [])
    cascaderValue.value = paths
    if (isSameLevelSelection(toRealPaths(paths))) {
      lastValidCascaderValue.value = cloneCascaderPaths(paths)
    }
    await nextTick()
    syncingFromModel.value = false
  },
  { immediate: true }
)

watch(
  () => selectedTagType.value,
  async (val, oldVal) => {
    if (!val || isFixedTagType.value) return
    emit('update:tagType', val)

    // 仅在用户主动切换标签体系时清空；初始化和报告回显必须保留已保存的体验代码。
    if (userTriggered.value && oldVal && oldVal !== val) {
      syncingFromModel.value = true
      cascaderValue.value = []
      lastValidCascaderValue.value = []
      emit('update:modelValue', [])
      await nextTick()
      syncingFromModel.value = false
      const source = userTriggered.value ? 'user' : undefined
      emit(
        'change',
        source
          ? { lastLevelCodes: [], lastLevelIds: [], names: [], source }
          : { lastLevelCodes: [], lastLevelIds: [], names: [] }
      )
    }

    const tagTreeReady = await loadTagTree(val)
    if (!tagTreeReady) return

    // 体验代码回显：等树加载后根据 modelValue 还原 cascader 路径
    await nextTick()
    syncingFromModel.value = true
    const paths = buildPathsFromModelValue(props.modelValue || [])
    cascaderValue.value = paths
    if (isSameLevelSelection(toRealPaths(paths))) {
      lastValidCascaderValue.value = cloneCascaderPaths(paths)
    }
    await nextTick()
    syncingFromModel.value = false

    if (userTriggered.value) {
      userTriggered.value = false
    }
  },
  { immediate: true }
)

watch(
  () => props.modelValue,
  async newVal => {
    // 外部回显（例如从 store 恢复）时，刷新右侧路径
    if (syncingFromModel.value) return
    if (!Array.isArray(newVal)) return

    const nextKey = buildModelValueKey(newVal)
    if (pendingSelfEmittedModelKey.value && pendingSelfEmittedModelKey.value === nextKey) {
      pendingSelfEmittedModelKey.value = null
      return
    }
    await nextTick()
    syncingFromModel.value = true
    const paths = buildPathsFromModelValue(newVal)
    cascaderValue.value = paths
    if (isSameLevelSelection(paths)) {
      lastValidCascaderValue.value = cloneCascaderPaths(paths)
    }
    await nextTick()
    syncingFromModel.value = false
  },
  { deep: true }
)

watch(
  () => tagOptions.value,
  val => {
    rebuildTagIndex(val || [])
  },
  { immediate: true }
)

onBeforeUnmount(() => {
  tagTreeRequestSerial += 1
  if (changeEmitTimer) window.clearTimeout(changeEmitTimer)
  changeEmitTimer = null
})
</script>

<template>
  <div class="experience-code-linkage">
    <el-select
      v-if="!hideTagType"
      v-model="selectedTagType"
      class="experience-code-linkage__type"
      :disabled="disabled"
      :clearable="false"
      filterable
      :options="resolvedTypeOptions"
      :props="{ label: 'value', value: 'key' }"
      placeholder="请选择"
      @change="onTagTypeChange"
    />

    <!-- v-loading 需要挂在真实 DOM 节点上，避免作用在组件根节点为 Fragment 时失效 -->
    <div v-loading="tagTreeLoading" class="experience-code-linkage__cascader-wrap">
      <el-cascader
        v-model="cascaderValue"
        class="experience-code-linkage__cascader"
        :disabled="disabled || tagTreeLoading"
        :options="tagOptions"
        :props="cascaderProps"
        collapse-tags
        :max-collapse-tags="1"
        collapse-tags-tooltip
        filterable
        clearable
        placeholder="不限"
        @change="onCascaderChange"
      />
    </div>
  </div>
</template>

<style lang="scss">
.experience-code-linkage {
  width: 100%;
  display: flex;
  gap: 12px;

  &__type {
    width: 180px;
    flex: 0 0 auto;
  }

  &__cascader {
    width: 100%;
  }

  &__cascader-wrap {
    flex: 1 1 auto;
    min-width: 0;
  }
}
</style>
