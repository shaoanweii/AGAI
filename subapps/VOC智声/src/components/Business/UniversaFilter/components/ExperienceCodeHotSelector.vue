<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref, shallowRef, watch } from 'vue'
import type { LabelTag } from '@/api/common/index.d'
import { getTagLibClientTree } from '@/api/common'
import { cloneDeep } from 'lodash-es'

defineOptions({
  name: 'ExperienceCodeHotSelector'
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
}

interface Emits {
  (e: 'handTagValueChange', value: any): void
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
  tyPageType: '',
  defaultTagType: 'CA',
  typeOptions: () => []
})

const emit = defineEmits<Emits>()

const selectedTagType = ref<string>(props.tagType || props.defaultTagType || 'CA')
const resolvedTypeOptions = computed(() => {
  const a = (props.typeOptions || []).filter(item => item?.key && item?.value)

  return a
})

// 树数据量大时，避免深度响应式带来的开销（el-cascader 只需要读数据，不依赖深层响应）
// const tagOptions = shallowRef<TagTreeNode[]>([])
const tagOptions = ref<TagTreeNode[]>([])

const cascaderValue = ref<string[][]>([])

const tagTreeLoading = ref(false)

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

// 体验代码树来自 /report/findTagLabelByType（按 tagLibType）
const loadTagTree = async (tagType: string) => {
  if (!tagType) {
    tagOptions.value = []
    return
  }
  try {
    // if (tagType === 'CA' && tagOptions.value.length === 0) {

    // }
    tagTreeLoading.value = true
    const res = await getTagLibClientTree({ tagLibType: tagType })
    tagOptions.value = Array.isArray(res.result) ? (res.result as TagTreeNode[]) : []
  } catch {
    tagOptions.value = []
  } finally {
    tagTreeLoading.value = false
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
  const codes = uniqueArray([...firstArr, ...secondArr, ...threeArr, ...fourArr])
  const paths: string[][] = []
  codes.forEach(code => {
    // 优先走缓存索引，避免每次回显都做全树递归
    const p = getPathByCodeFast(code) || findPathByCode(tagOptions.value, code)
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
  return dedup
}

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
      const node = getNodeByCodeFast(code) || findNodeByCode(tagOptions.value, code)
      if (node?.tagName) names.push(node.tagName)
    })
  })

  const lastLevelIds: string[] = []
  lastLevelCodesArray.forEach(code => {
    const node = getNodeByCodeFast(code) || findNodeByCode(tagOptions.value, code)
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
    const node = getNodeByCodeFast(code) || findNodeByCode(tagOptions.value, code)
    if (node) tagPath.push({ code, name: node.tagName || '', level: 1 })
  })
  secondArr.forEach(code => {
    const node = getNodeByCodeFast(code) || findNodeByCode(tagOptions.value, code)
    if (node) tagPath.push({ code, name: node.tagName || '', level: 2 })
  })
  threeArr.forEach(code => {
    const node = getNodeByCodeFast(code) || findNodeByCode(tagOptions.value, code)
    if (node) tagPath.push({ code, name: node.tagName || '', level: 3 })
  })
  fourArr.forEach(code => {
    const node = getNodeByCodeFast(code) || findNodeByCode(tagOptions.value, code)
    if (node) tagPath.push({ code, name: node.tagName || '', level: 4 })
  })

  return tagPath
}

// 暴露给父组件（UniversaFilter）复用现有联动逻辑
defineExpose({
  getLastLevelInfo,
  getTagPath,
  findNodeByCode,
  tagOptions
})

const onTagTypeChange = (value: any) => {
  emit('handTagValueChange', value)
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

  const paths = normalizeCascaderValueToPaths(val)
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

// 第一个下拉的值
watch(
  () => selectedTagType.value,
  async (val, oldVal) => {
    if (!val) return

    emit('update:tagType', val)

    // 类型切换：清空右侧选择，避免跨体系残留
    if (oldVal && oldVal !== val) {
      syncingFromModel.value = true
      cascaderValue.value = []
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

    await loadTagTree(val)

    // 体验代码回显：等树加载后根据 modelValue 还原 cascader 路径
    await nextTick()
    syncingFromModel.value = true
    cascaderValue.value = buildPathsFromModelValue(props.modelValue || [])
    await nextTick()
    syncingFromModel.value = false

    if (userTriggered.value) {
      userTriggered.value = false
    }
    await nextTick()
  },
  { deep: true, immediate: true }
)

// 处理禁用逻辑
watch(
  () => [props.modelValue, tagOptions.value],
  async newVal => {
    if (props.tyPageType === 'hotFilterType') {
      // 只有回显筛选项需要
      const selectCascaderArr = props.modelValue

      if (selectCascaderArr?.length && tagOptions?.value?.length) {
        // 判断需要禁用
        const disLeavel = selectCascaderArr.length

        const setDisabledByLevel = (tree: any, disLeavel: any) => {
          if (!Array.isArray(tree)) return tree

          const recurse = (nodes: any, level: any) => {
            nodes.forEach((node: any) => {
              if (!node || typeof node !== 'object') return

              node.disabled = level !== disLeavel

              if (Array.isArray(node.child) && node.child.length > 0) {
                recurse(node.child, level + 1)
              }
            })
          }

          recurse(tree, 1)
          return tree
        }
        setDisabledByLevel(tagOptions?.value, disLeavel)
      } else {
        // 递归遍历 全部设置成 false
        const findLeavel = (arr: any) => {
          arr.forEach((item: any) => {
            item.disabled = false
            if (item.disabled) {
              delete item.disabled
            }
            const list = item[cascaderProps.value.children]
            if (list && list.length) {
              findLeavel(list)
            }
          })
        }
        findLeavel(tagOptions.value)
      }
    }
  },
  { deep: true, immediate: true }
)

// 第二个联动组件的tree列表数据 第二个联动组件的值
watch(
  () => [tagOptions.value, props.modelValue, props.tyPageType],
  async (valArr: any) => {
    const val: any = valArr[0]

    rebuildTagIndex(val || [])
    await nextTick()

    const newVal: any = valArr[1]
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
    const xxxxx = buildPathsFromModelValue(newVal)

    cascaderValue.value = xxxxx
    await nextTick()
    syncingFromModel.value = false
    await nextTick()
  },
  { deep: true, immediate: true }
)

onBeforeUnmount(() => {
  if (changeEmitTimer) window.clearTimeout(changeEmitTimer)
  changeEmitTimer = null
})
</script>

<template>
  <div class="experience-code-linkage">
    <el-select
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
