<script setup lang="ts">
import { useTable } from '@/hooks/table'
import useConditions from '@/hooks/useConditions'
import { computedCardHeight } from '@/utils'
import { ElMessage } from 'element-plus'
import { createOpinionSynonym, updateOpinionSynonymById } from '@/api/opinionSynonyms'
import { findAllFinalTagLib } from '@/api/tag'
import { getTagLibClientTree } from '@/api/main'
import { DATA_KNOWLEDGE_API_KEY_HEADER } from '@/api/constants'
import { getLoginUserMetaOrNull } from '@/utils/loginUserMeta'
import CorpusMappingCreateDialog from '../components/CorpusMappingCreateDialog.vue'
import NewWordDetailsDialog from '../../../dataCenter/discovery/NewWordDetailsDialog.vue'
import FDatePicker from '@/components/FDatePicker/index.vue'
import AppDialog from '@/components/AppDialog.vue'
import dayjs from 'dayjs'
import { debounce } from 'lodash-es'
import { formatWeekRangeLabel } from '@/utils/weekRange'
import {
  useLazyExperienceCodeCascader,
  normalizeExperienceCodeSelectionPaths,
  type ExperienceCodeSourceNode
} from '@/hooks/useLazyExperienceCodeCascader'

defineOptions({
  name: 'KnowledgeCenterTextCorpus'
})

interface Props {
  opinionType?: 0 | 1
  variant?: 'corpus' | 'textNewWord' | 'surveyNewWord'
}

const props = withDefaults(defineProps<Props>(), {
  opinionType: 1,
  variant: 'corpus'
})

const isSurveyCorpus = computed(() => props.opinionType === 0)
const isNewWordVariant = computed(
  () => props.variant === 'textNewWord' || props.variant === 'surveyNewWord'
)
const standardOpinionFieldLabel = computed(() => (isNewWordVariant.value ? '推荐观点' : '标准观点'))

const buildDefaultTimeRange = () => {
  // 默认按本月时间范围查询
  const end = dayjs()
  const start = end.startOf('month')
  return [start.format('YYYY-MM-DD'), end.format('YYYY-MM-DD')]
}

const { conditions } = useConditions({
  url: '/insights/addLabel/conditions',
  headers: { ...DATA_KNOWLEDGE_API_KEY_HEADER }
})
provide('conditions', conditions)

type EnableStatus = 'all' | 'enabled' | 'disabled'

const enableStatusOptions: { label: string; value: EnableStatus }[] = [
  { label: '不限', value: 'all' },
  { label: '已启用', value: 'enabled' },
  { label: '已禁用', value: 'disabled' }
]

interface StandardOpinionOption {
  id?: string
  tagParentId?: string
  tagName: string
  tagCode: string
  [key: string]: any
}

interface StandardOpinionListController {
  states?: {
    value?: {
      scrollOffset?: number
    }
    scrollOffset?: number
  }
  scrollTo?: (offset: number) => void
  value?: StandardOpinionListController
}

interface StandardOpinionSelectExpose {
  menuRef?: {
    listRef?: StandardOpinionListController
  }
  filteredOptions?: StandardOpinionOption[] | { value?: StandardOpinionOption[] }
}

const STANDARD_OPINION_SELECT_LIMIT = 200

// 标准观点下拉选项（接口返回原始结构，统一使用 tagName / tagCode）
const allStandardOpinionOptions = ref<StandardOpinionOption[]>([])
const filterStandardOpinionOptions = ref<StandardOpinionOption[]>([])
const standardOpinionLoading = ref(false)
const experienceCodeSelection = ref<string[][]>([])
const experienceCodeLoading = ref(false)
const standardOpinionRequestSeq = ref(0)
const standardOpinionOptionsSyncPending = ref(false)
const standardOpinionSelectRef = ref<StandardOpinionSelectExpose | null>(null)
const lastEffectiveStandardOpinionSelection = ref<string[]>([])

const {
  cascaderOptions: experienceCodeOptions,
  nodeMap: experienceCodeNodeMap,
  cascaderProps: experienceCodeCascaderProps,
  setSourceTree: setExperienceCodeTree,
  beforeFilter: handleExperienceCodeBeforeFilter,
  filterMethod: filterExperienceCodeMethod,
  formatSuggestionPath: formatExperienceCodeSuggestionPath
} = useLazyExperienceCodeCascader()

const queryForm = reactive({
  // 文本语料筛选：主体关键词
  subjectKeyword: '',
  // 文本语料筛选：描述关键词
  descriptionKeyword: '',
  // 文本语料筛选：操作人
  operatorKeyword: '',
  // 文本语料筛选：意图
  intention: '',
  // 兼容新词变体：主体/描述混合检索
  subject_desc: '',
  // 查询接口已改为按标准观点中文名称筛选，这里统一缓存名称数组。
  standard_opinion: [] as string[],
  enable_status: 'all' as EnableStatus,
  process_status: 'all' as 'all' | 'pending' | 'processed',
  time_range: buildDefaultTimeRange()
})

const standardOpinionSelection = ref<string[]>([])

type CorpusSortColumnKey = 'standardOpinion' | 'createTime' | 'updatedTime'
type CorpusSortOrder = 'asc' | 'desc'

const SORT_FIELD_MAP: Record<CorpusSortColumnKey, string> = {
  standardOpinion: 'standard_opinion',
  createTime: 'created_time',
  updatedTime: 'updated_time'
}

const tableSortState = reactive<{
  key: CorpusSortColumnKey | ''
  order: CorpusSortOrder
}>({
  key: '',
  order: 'desc'
})

/**
 * 将前端列 key 映射为后端排序字段，并拆分为 sort_by / sort_order 两个参数。
 * 未选择排序列时不传排序字段，避免覆盖接口默认排序逻辑。
 */
const resolveSortParams = (
  columnKey: CorpusSortColumnKey | '',
  order: CorpusSortOrder
): { sort_by?: string; sort_order?: CorpusSortOrder } => {
  if (!columnKey) return {}
  const sortBy = SORT_FIELD_MAP[columnKey]
  if (!sortBy) return {}
  return {
    sort_by: sortBy,
    sort_order: order
  }
}

/**
 * @description: 统一清洗标准观点多选结果，避免空值和重复编码进入查询参数。
 * @param {unknown} selection 当前标准观点选择值
 * @return {string[]} 清洗后的标准观点编码数组
 */
const normalizeStandardOpinionSelection = (selection: unknown): string[] => {
  if (!Array.isArray(selection)) return []

  return Array.from(new Set(selection.map(item => String(item ?? '').trim()).filter(Boolean)))
}

/**
 * @description: 统一清洗 el-cascader 返回的单条路径，避免空值污染接口入参。
 * @param {unknown} path 当前选中的单条级联路径
 * @return {string[]} 清洗后的 id 路径
 */
const normalizeExperienceCodePath = (path: unknown): string[] => {
  if (!Array.isArray(path)) return []

  return path.map(item => String(item ?? '').trim()).filter(Boolean)
}

/**
 * @description: 体验代码接口首项固定为“全领域业务”，筛选区只需要其下 1-4 级真实节点。
 * @param {ExperienceCodeSourceNode | null | undefined} rootNode 接口根节点
 * @return {ExperienceCodeSourceNode[]} 一级分类列表
 */
const getExperienceCodeRootChildren = (rootNode?: ExperienceCodeSourceNode | null) => {
  return Array.isArray(rootNode?.child) ? rootNode.child : []
}

/**
 * @description: 提取当前体验代码多选项的末级 id，供标准观点接口按分类范围过滤。
 * @param {string[][]} selection 当前多选路径
 * @return {string[]} 去重后的分类 id
 */
const collectSelectedExperienceCodeIds = (selection: string[][]) => {
  const tagParentIds = new Set<string>()

  normalizeExperienceCodeSelectionPaths(selection).forEach(path => {
    const normalizedPath = normalizeExperienceCodePath(path)
    const selectedId = normalizedPath[normalizedPath.length - 1]
    if (selectedId) {
      tagParentIds.add(selectedId)
    }
  })

  return Array.from(tagParentIds)
}

/**
 * @description: 页面内直接获取 1-4 级体验代码树，避免继续依赖公共筛选组件。
 * @return {*}
 */
const getExperienceCodeOptions = async () => {
  experienceCodeLoading.value = true
  try {
    const resp = await getTagLibClientTree({ level: '4', tagAttribute: 'Category', tagType: 'CA' })
    const sourceTree = getExperienceCodeRootChildren(resp?.result?.[0])
    setExperienceCodeTree(sourceTree)
    experienceCodeSelection.value = experienceCodeSelection.value
      .map(path => normalizeExperienceCodePath(path))
      .filter(path => {
        const selectedId = path[path.length - 1]
        return !!selectedId && !!experienceCodeNodeMap.value[selectedId]
      })
  } catch {
    setExperienceCodeTree([])
    experienceCodeSelection.value = []
  } finally {
    experienceCodeLoading.value = false
  }
}

/**
 * @description: 进入页面先拉全量标准观点；体验代码变化后再按选中分类 id 缩小范围。
 * @param {{ tagParentIds?: string[] }} params 标准观点查询参数
 * @return {*}
 */
const getLastTagOptions = async (params: { tagParentIds?: string[] } = {}) => {
  const requestSeq = standardOpinionRequestSeq.value + 1
  standardOpinionRequestSeq.value = requestSeq
  standardOpinionLoading.value = true
  try {
    const resp = await findAllFinalTagLib(params)
    if (requestSeq !== standardOpinionRequestSeq.value) return

    const list = Array.isArray(resp?.result) ? resp.result : []
    const options = list.filter((item: any) => !!item && !!item.tagCode)
    filterStandardOpinionOptions.value = options
    if (!params.tagParentIds?.length) {
      allStandardOpinionOptions.value = options
    }
    const availableCodes = new Set(
      options.map(item => String(item?.tagCode ?? '').trim()).filter(Boolean)
    )
    // 中文注释：体验代码范围切换后，仅保留当前结果集中仍存在的标准观点编码，避免提交失效值。
    standardOpinionSelection.value = standardOpinionSelection.value.filter(code =>
      availableCodes.has(String(code))
    )
    lastEffectiveStandardOpinionSelection.value = [...standardOpinionSelection.value]
    standardOpinionOptionsSyncPending.value = false
    syncStandardOpinionQueryValue()
  } catch {
    if (requestSeq !== standardOpinionRequestSeq.value) return
    filterStandardOpinionOptions.value = []
    if (!params.tagParentIds?.length) {
      allStandardOpinionOptions.value = []
    }
    standardOpinionSelection.value = []
    lastEffectiveStandardOpinionSelection.value = []
    standardOpinionOptionsSyncPending.value = false
    syncStandardOpinionQueryValue()
  } finally {
    if (requestSeq === standardOpinionRequestSeq.value) {
      standardOpinionLoading.value = false
    }
  }
}

const standardOpinionLabelMap = computed(() => {
  const labelMap = new Map<string, string>()

  ;[...allStandardOpinionOptions.value, ...filterStandardOpinionOptions.value].forEach(item => {
    const code = String(item?.tagCode ?? '').trim()
    const label = String(item?.tagName ?? '').trim()
    if (code && label && !labelMap.has(code)) {
      labelMap.set(code, label)
    }
  })

  return labelMap
})

const findStandardOpinionLabelByCode = (code: string) => {
  if (!code) return ''
  return standardOpinionLabelMap.value.get(String(code)) || ''
}

/**
 * @description: 将标准观点编码批量转换为中文名称，保证列表查询统一走后端新口径。
 * @param {string[]} codes 标准观点编码数组
 * @return {string[]} 标准观点中文名称数组
 */
const resolveStandardOpinionNames = (codes: string[] = []) => {
  const nameSet = new Set<string>()
  codes.forEach(code => {
    const label = findStandardOpinionLabelByCode(code)
    if (label) {
      nameSet.add(label)
    }
  })
  return Array.from(nameSet)
}

/**
 * @description: 计算当前查询应提交的标准观点编码。
 * 优先使用用户显式选择；如果只选择了体验代码，则自动带出该范围下的全部标准观点。
 * @return {string[]} 标准观点编码数组
 */
const resolveQueryStandardOpinionCodes = (): string[] => {
  const selectedCodes = standardOpinionSelection.value
    .map(code => String(code ?? '').trim())
    .filter(Boolean)
  if (selectedCodes.length) {
    return Array.from(new Set(selectedCodes))
  }

  // 中文注释：体验代码范围刚变化但联动结果尚未返回时，不再复用旧 options 反推标准观点，避免误查。
  if (standardOpinionOptionsSyncPending.value) {
    return []
  }

  if (!experienceCodeSelection.value.length) {
    return []
  }

  return Array.from(
    new Set(
      filterStandardOpinionOptions.value
        .map(item => String(item?.tagCode ?? '').trim())
        .filter(Boolean)
    )
  )
}

/**
 * @description: 同步标准观点查询入参，统一转换为列表接口要求的中文名称数组。
 * @return {string[]} 当前有效的标准观点编码
 */
const syncStandardOpinionQueryValue = (codes?: string[]): string[] => {
  const effectiveCodes = Array.isArray(codes)
    ? normalizeStandardOpinionSelection(codes)
    : resolveQueryStandardOpinionCodes()
  queryForm.standard_opinion = resolveStandardOpinionNames(effectiveCodes)
  return effectiveCodes
}

const isStandardOpinionLinkagePending = computed(() => {
  return standardOpinionOptionsSyncPending.value
})

const isStandardOpinionSelectDisabled = computed(() => {
  return standardOpinionLoading.value || isStandardOpinionLinkagePending.value
})

/**
 * @description: 读取标准观点虚拟列表控制器，统一兼容组件实例上的 Ref 包装与直接暴露对象。
 * @return {StandardOpinionListController | null} 标准观点虚拟列表控制器
 */
const getStandardOpinionListController = (): StandardOpinionListController | null => {
  const rawListRef = standardOpinionSelectRef.value?.menuRef?.listRef
  if (!rawListRef) return null
  return rawListRef.value || rawListRef
}

/**
 * @description: 优先读取下拉当前过滤后的展示项，保证搜索场景下“全选”只作用于可见结果。
 * @return {StandardOpinionOption[]} 当前下拉展示的标准观点选项
 */
const getDisplayedStandardOpinionOptions = (): StandardOpinionOption[] => {
  const rawFilteredOptions = standardOpinionSelectRef.value?.filteredOptions
  const resolvedOptions =
    (Array.isArray(rawFilteredOptions) ? rawFilteredOptions : rawFilteredOptions?.value) || []

  const displayedOptions = resolvedOptions.filter(item => item && item.type !== 'Group')
  if (displayedOptions.length) {
    return displayedOptions
  }

  return filterStandardOpinionOptions.value
}

const displayedStandardOpinionCodes = computed(() => {
  return Array.from(
    new Set(
      getDisplayedStandardOpinionOptions()
        .map(item => String(item?.tagCode ?? '').trim())
        .filter(Boolean)
    )
  )
})

const isExperienceCodeRangeOverflow = computed(() => {
  return (
    !!experienceCodeSelection.value.length &&
    !standardOpinionOptionsSyncPending.value &&
    !standardOpinionSelection.value.length &&
    filterStandardOpinionOptions.value.length > STANDARD_OPINION_SELECT_LIMIT
  )
})

const isStandardOpinionSelectionOverflow = computed(() => {
  return standardOpinionSelection.value.length > STANDARD_OPINION_SELECT_LIMIT
})

const isStandardOpinionAllDisplayedSelected = computed(() => {
  const displayedCodes = displayedStandardOpinionCodes.value
  if (!displayedCodes.length) return false

  const selectedCodeSet = new Set(
    standardOpinionSelection.value.map(code => String(code ?? '').trim()).filter(Boolean)
  )
  return displayedCodes.every(code => selectedCodeSet.has(code))
})

const isStandardOpinionDisplayedIndeterminate = computed(() => {
  const displayedCodes = displayedStandardOpinionCodes.value
  if (!displayedCodes.length) return false

  const selectedCodeSet = new Set(
    standardOpinionSelection.value.map(code => String(code ?? '').trim()).filter(Boolean)
  )
  const selectedDisplayedCount = displayedCodes.filter(code => selectedCodeSet.has(code)).length
  return selectedDisplayedCount > 0 && selectedDisplayedCount < displayedCodes.length
})

/**
 * @description: 当前体验代码范围过大时，统一提示用户继续缩小范围后再选择标准观点。
 * @return {*}
 */
const showExperienceCodeRangeLimitWarning = () => {
  ElMessage.warning(
    `当前体验代码范围下${standardOpinionFieldLabel.value}超过${STANDARD_OPINION_SELECT_LIMIT}个，请继续缩小体验代码范围或选择不超过${STANDARD_OPINION_SELECT_LIMIT}个${standardOpinionFieldLabel.value}后再查询`
  )
}

/**
 * @description: 体验代码切换后标准观点仍在按最新范围联动刷新，请等待结果稳定后再继续查询。
 * @return {*}
 */
const showStandardOpinionPendingWarning = () => {
  ElMessage.warning(`${standardOpinionFieldLabel.value}正在按最新体验代码刷新，请稍候再操作`)
}

/**
 * @description: 显式选择标准观点时最多保留 200 个，超过后阻止继续新增并给出黄色提示。
 * @return {*}
 */
const showStandardOpinionSelectionLimitWarning = () => {
  ElMessage.warning(
    `${standardOpinionFieldLabel.value}最多只能选择${STANDARD_OPINION_SELECT_LIMIT}个`
  )
}

/**
 * @description: 读取标准观点下拉当前虚拟列表滚动偏移，便于超限回滚时恢复用户浏览位置。
 * @return {number} 当前滚动偏移
 */
const getStandardOpinionScrollOffset = (): number => {
  const listRef = getStandardOpinionListController()
  const rawOffset = listRef?.states?.value?.scrollOffset ?? listRef?.states?.scrollOffset ?? 0
  return Number(rawOffset) || 0
}

/**
 * @description: 选择变更后统一恢复下拉滚动位置，避免 select-v2 内部刷新把长列表甩回顶部。
 * @param {number} scrollOffset 需要恢复的滚动偏移
 * @return {*}
 */
const restoreStandardOpinionScrollOffset = (scrollOffset: number) => {
  nextTick(() => {
    const runRestore = () => {
      const listRef = getStandardOpinionListController()
      listRef?.scrollTo?.(scrollOffset)
    }

    if (typeof window !== 'undefined' && typeof window.requestAnimationFrame === 'function') {
      window.requestAnimationFrame(() => {
        runRestore()
      })
      return
    }

    setTimeout(runRestore, 0)
  })
}

/**
 * @description: 正常选择时仅同步缓存和查询参数，不重复回写 v-model，减少 select-v2 额外刷新。
 * @param {string[]} selection 当前已选择的标准观点编码
 * @param {number} scrollOffset 选择前的滚动偏移
 * @return {*}
 */
const syncEffectiveStandardOpinionSelection = (selection: string[], scrollOffset?: number) => {
  lastEffectiveStandardOpinionSelection.value = [...selection]
  syncStandardOpinionQueryValue(selection)
  if (typeof scrollOffset === 'number') {
    restoreStandardOpinionScrollOffset(scrollOffset)
  }
}

/**
 * @description: 标准观点超限时回滚到上一次有效选择，并尽量恢复用户当前浏览滚动位置。
 * @param {string[]} selection 需要回滚到的标准观点编码
 * @param {number} scrollOffset 回滚前的滚动偏移
 * @return {*}
 */
const rollbackStandardOpinionSelection = (selection: string[], scrollOffset: number) => {
  standardOpinionSelection.value = [...selection]
  syncEffectiveStandardOpinionSelection(selection, scrollOffset)
}

/**
 * @description: 查询前统一校验体验代码范围和显式选择数量，避免提交超大查询范围。
 * @return {boolean} 是否允许继续查询
 */
const ensureStandardOpinionQueryAllowed = () => {
  if (isStandardOpinionLinkagePending.value) {
    showStandardOpinionPendingWarning()
    return false
  }

  if (isStandardOpinionSelectionOverflow.value) {
    showStandardOpinionSelectionLimitWarning()
    return false
  }

  if (isExperienceCodeRangeOverflow.value) {
    showExperienceCodeRangeLimitWarning()
    return false
  }

  return true
}

/**
 * @description: 选中值写回前先做前置校验，保证全选和逐项勾选都能在第一次超限时立即拦截。
 * @param {unknown[]} selection 当前即将写入的标准观点编码数组
 * @return {boolean} 是否允许本次变更生效
 */
const beforeStandardOpinionSelectionChange = (selection: unknown[]) => {
  if (isStandardOpinionLinkagePending.value) {
    rollbackStandardOpinionSelection(
      lastEffectiveStandardOpinionSelection.value,
      getStandardOpinionScrollOffset()
    )
    showStandardOpinionPendingWarning()
    return false
  }

  const normalizedSelection = normalizeStandardOpinionSelection(selection)
  if (normalizedSelection.length <= STANDARD_OPINION_SELECT_LIMIT) {
    return true
  }

  const previousSelection = lastEffectiveStandardOpinionSelection.value
  const isRemovalOnly = normalizedSelection.every(code => previousSelection.includes(code))
  if (isRemovalOnly) {
    return true
  }

  rollbackStandardOpinionSelection(previousSelection, getStandardOpinionScrollOffset())
  showStandardOpinionSelectionLimitWarning()
  return false
}

/**
 * @description: 统一应用标准观点选择结果，仅在前置校验通过后更新状态，避免组件先写入再回滚。
 * @param {unknown[]} selection 当前下拉回传的标准观点编码数组
 * @return {*}
 */
const applyStandardOpinionSelection = (selection: unknown[]) => {
  if (!beforeStandardOpinionSelectionChange(selection)) {
    return
  }

  const normalizedSelection = normalizeStandardOpinionSelection(selection)
  const scrollOffset = getStandardOpinionScrollOffset()
  standardOpinionSelection.value = [...normalizedSelection]
  syncEffectiveStandardOpinionSelection(normalizedSelection, scrollOffset)
}

/**
 * @description: 原生下拉变更时走统一前置校验，保证逐项勾选与批量全选行为一致。
 * @param {unknown[]} selection 当前下拉回传的标准观点编码数组
 * @return {*}
 */
const handleStandardOpinionModelValueChange = (selection: unknown[]) => {
  applyStandardOpinionSelection(selection)
}

/**
 * @description: 当前展示项范围内切换“全选/取消全选”，单次点击即生效，不依赖公共组件内部事件时序。
 * @return {*}
 */
const toggleStandardOpinionSelectAll = () => {
  const displayedCodes = displayedStandardOpinionCodes.value
  if (!displayedCodes.length) return

  const selectedCodes = normalizeStandardOpinionSelection(standardOpinionSelection.value)
  const displayedCodeSet = new Set(displayedCodes)

  if (isStandardOpinionAllDisplayedSelected.value) {
    applyStandardOpinionSelection(selectedCodes.filter(code => !displayedCodeSet.has(code)))
    return
  }

  const nextSelection = Array.from(new Set([...selectedCodes, ...displayedCodes]))
  applyStandardOpinionSelection(nextSelection)
}

/**
 * @description: 体验代码变化后，按当前选中 1-4 级 id 重新拉取标准观点选项。
 * @param {string[][]} selection 当前体验代码级联选中路径
 * @return {*}
 */
const triggerStandardOpinionLinkage = debounce((tagParentIds: string[]) => {
  void getLastTagOptions(tagParentIds.length ? { tagParentIds } : {})
}, 300)

/**
 * @description: 取消尚未执行的体验代码联动请求，避免旧选择在重置或离开页面后覆盖新状态。
 * @return {*}
 */
const cancelPendingStandardOpinionLinkage = () => {
  triggerStandardOpinionLinkage.cancel()
  standardOpinionOptionsSyncPending.value = false
}

const handleExperienceCodeChange = (selection: string[][]) => {
  const tagParentIds = collectSelectedExperienceCodeIds(selection)
  // 中文注释：保留旧下拉项减少闪烁，但同步标记当前范围待刷新，避免查询阶段继续消费旧范围数据。
  standardOpinionOptionsSyncPending.value = true
  syncStandardOpinionQueryValue()
  triggerStandardOpinionLinkage(tagParentIds)
}

// 动态识别标准观点中“意图”字段的键名，兼容后端不同命名
const STANDARD_OPINION_INTENTION_KEYS = [
  'intention',
  'intentionName',
  'intention_name',
  'intent',
  'intentName',
  'intent_name'
]

const standardOpinionIntentionKey = computed<string | null>(() => {
  const list = allStandardOpinionOptions.value
  if (!Array.isArray(list) || !list.length) return null
  const sample = list.find(item => item && typeof item === 'object')
  if (!sample) return null

  for (const key of STANDARD_OPINION_INTENTION_KEYS) {
    if (Object.prototype.hasOwnProperty.call(sample, key)) {
      return key
    }
  }

  const dynamicKey = Object.keys(sample).find(key => key.toLowerCase().includes('intent'))
  return dynamicKey || null
})

const buildStandardOpinionMaps = computed(() => {
  const byId = new Map<string, StandardOpinionOption>()
  const byName = new Map<string, StandardOpinionOption>()
  const list = allStandardOpinionOptions.value

  if (!Array.isArray(list) || !list.length) {
    return { byId, byName }
  }

  list.forEach(item => {
    if (!item) return
    const code = item.tagCode != null ? String(item.tagCode) : ''
    const name = item.tagName != null ? String(item.tagName) : ''
    if (code) {
      byId.set(code, item)
    }
    if (name) {
      byName.set(name, item)
    }
  })

  return { byId, byName }
})

const findStandardOpinionOptionForRow = (row: any): StandardOpinionOption | null => {
  if (!row) return null
  const { byId, byName } = buildStandardOpinionMaps.value
  if (!byId.size && !byName.size) return null

  const idCandidates: string[] = []
  const textCandidates: string[] = []

  const rawIdFields = [
    row.standard_opinion_id,
    row.standardOpinionId,
    row.standardOpinionCode,
    row.standard_opinion_code
  ]
  rawIdFields.forEach(val => {
    if (val !== undefined && val !== null && val !== '') {
      idCandidates.push(String(val))
    }
  })

  const rawTextFields = [row.standardOpinion, row.standard_opinion, row.standardOpinionText]
  rawTextFields.forEach(val => {
    if (val !== undefined && val !== null && val !== '') {
      textCandidates.push(String(val))
    }
  })

  for (const id of idCandidates) {
    const matched = byId.get(id)
    if (matched) return matched
  }

  for (const text of textCandidates) {
    const matched = byName.get(text)
    if (matched) return matched
  }

  return null
}

// 列表数据归一化：按类型兼容文本语料与问卷语料
const normalizeCorpusMappingItem = (item: any) => {
  const opinionType = props.opinionType ?? 1
  if (opinionType === 0) {
    return {
      ...item,
      // 问卷语料以语料本身为主体描述
      subject: item.opinion ?? item.subject ?? item.description ?? item.entity,
      subject_desc: item.description ?? item.subject_desc,
      standardOpinion: item.standard_opinion,
      createTime: item.created_time
    }
  }
  return {
    ...item,
    subject: item.entity,
    subject_desc: item.description,
    standardOpinion: item.standard_opinion,
    createTime: item.created_time
  }
}

const {
  table,
  form,
  handleReset,
  getFirstPageTableData,
  refreshTableData,
  handleCurrentChange,
  handleSizeChange
} = useTable(
  {
    method: 'POST',
    url: '/ai/opinion-synonyms/search',
    timeout: 0,
    pageSize: 100,
    headers: { ...DATA_KNOWLEDGE_API_KEY_HEADER }
  },
  res => {
    const result = res.result || {}
    const items = Array.isArray(result.items) ? result.items : []
    const list = items.map(normalizeCorpusMappingItem)
    return {
      list,
      total: result.total ?? 0
    }
  }
)

const syncTableFilters = () => {
  const nextFilter: any = {}
  // 优先使用用户勾选的标准观点；若仅选了体验代码，则自动带出当前范围下全部标准观点。
  syncStandardOpinionQueryValue()
  nextFilter.standard_opinion = [...queryForm.standard_opinion]

  if (isNewWordVariant.value) {
    if (queryForm.subject_desc) {
      nextFilter.subject_desc = queryForm.subject_desc
    }

    nextFilter.process_status = queryForm.process_status

    const range =
      Array.isArray(queryForm.time_range) && queryForm.time_range.length === 2
        ? queryForm.time_range
        : buildDefaultTimeRange()
    const [start, end] = range
    nextFilter.start_time = dayjs(start).format('YYYY-MM-DD')
    nextFilter.end_time = dayjs(end).format('YYYY-MM-DD')
  } else {
    if (queryForm.subjectKeyword) {
      if (props.opinionType === 0) {
        nextFilter.opinion = queryForm.subjectKeyword
      } else {
        nextFilter.entity = queryForm.subjectKeyword
      }
    }

    if (!isSurveyCorpus.value && queryForm.descriptionKeyword) {
      nextFilter.description = queryForm.descriptionKeyword
    }

    if (queryForm.operatorKeyword) {
      nextFilter.operator = queryForm.operatorKeyword
    }

    nextFilter.enable_status = queryForm.enable_status
  }

  nextFilter.opinion_type = props.opinionType
  const { sort_by, sort_order } = resolveSortParams(tableSortState.key, tableSortState.order)
  if (sort_by) {
    nextFilter.sort_by = sort_by
    nextFilter.sort_order = sort_order
  }
  table.filter = nextFilter
}

const query = () => {
  if (!ensureStandardOpinionQueryAllowed()) return

  clearSelection()
  syncTableFilters()
  getFirstPageTableData()
}

const reset = () => {
  clearSelection()
  queryForm.subjectKeyword = ''
  queryForm.descriptionKeyword = ''
  queryForm.operatorKeyword = ''
  queryForm.intention = ''
  queryForm.subject_desc = ''
  queryForm.standard_opinion = []
  experienceCodeSelection.value = []
  standardOpinionSelection.value = []
  lastEffectiveStandardOpinionSelection.value = []
  queryForm.enable_status = 'all'
  queryForm.process_status = 'all'
  queryForm.time_range = buildDefaultTimeRange()
  cancelPendingStandardOpinionLinkage()
  void getLastTagOptions({})
  handleReset(() => {
    syncTableFilters()
  })
}

onMounted(() => {
  query()
  void Promise.all([getExperienceCodeOptions(), getLastTagOptions({})])
})

onBeforeUnmount(() => {
  cancelPendingStandardOpinionLinkage()
})

const enableStatusLabelMap: Record<string, string> = {
  '1': '已启用',
  '2': '已启用',
  '0': '已禁用',
  '-1': '已禁用'
}

const getEnableStatusText = (row: any) => {
  const rawStatus = row.status ?? row.enable_status ?? row.auditStatusCode ?? row.auditStatus ?? ''
  if (rawStatus === 'enabled') {
    return '已启用'
  }
  if (rawStatus === 'disabled') {
    return '已禁用'
  }
  const statusKey = `${rawStatus}`
  return row.statusText || enableStatusLabelMap[statusKey] || '-'
}

const getEnableStatusTagStyle = (row: any) => {
  const text = getEnableStatusText(row)
  let color = ''
  switch (text) {
    case '已启用':
      color = '#00B42A'
      break
    case '已禁用':
      color = '#C9CDD4'
      break
    default:
      color = '#C9CDD4'
      break
  }
  return { backgroundColor: color }
}

const getSubjectText = (row: any) => row.subject ?? row.entity ?? '-'
const getDescriptionText = (row: any) =>
  row.description ?? row.entity_desc ?? row.subject_desc ?? '-'
const getStandardOpinionText = (row: any) =>
  row.standardOpinion ?? row.standard_opinion ?? row.standardOpinionText ?? '-'
const getCreateTimeText = (row: any) =>
  row.createTime ?? row.created_time ?? row.created_at ?? row.createdAt ?? '-'

const getFrequencyText = (row: any) => row.frequency ?? row.freq ?? row.count ?? '-'

const getUpdatedTimeText = (row: any) => {
  const updated = row.updated_time
  if (!updated) {
    return ''
  }
  return updated
}

const getOperatorText = (row: any) => {
  const operator =
    row.operator ??
    row.last_operator ??
    row.username ??
    row.lastOperator ??
    row.operatorName ??
    row.operator_name
  if (!operator) return '-'
  return operator
}

const getIntentionText = (row: any) => {
  const intentionKey = standardOpinionIntentionKey.value
  if (!intentionKey) return ''
  const matched = findStandardOpinionOptionForRow(row)
  if (!matched) return ''
  const raw = (matched as any)[intentionKey]
  if (raw === undefined || raw === null) return ''
  return String(raw)
}

const intentionOptions = [
  { label: '抱怨', value: '抱怨' },
  { label: '表扬', value: '表扬' },
  { label: '咨询', value: '咨询' },
  { label: '建议', value: '建议' }
]

// 文案：根据语料类型切换问卷语料/文本语料展示
const filterSubjectLabel = computed(() => {
  if (isNewWordVariant.value) {
    return '主体描述'
  }
  return isSurveyCorpus.value ? '语料描述' : '主体描述'
})
const filterSubjectPlaceholder = computed(() => {
  if (isNewWordVariant.value) {
    return '请输入主体描述'
  }
  return isSurveyCorpus.value ? '请输入语料描述' : '请输入主体描述'
})
const subjectColumnLabel = computed(() => {
  if (isNewWordVariant.value) {
    return '主体'
  }
  return isSurveyCorpus.value ? '语料描述' : '主体'
})

const subjectKeywordLabel = computed(() => (isSurveyCorpus.value ? '语料描述' : '主体'))
const subjectKeywordPlaceholder = computed(() =>
  isSurveyCorpus.value ? '请输入语料描述' : '请输入主体'
)

const standardOpinionColumnLabel = computed(() =>
  isNewWordVariant.value ? '推荐观点' : '标准观点'
)

// 统一标准观点下拉占位文案，加载中时与体验代码保持同样的反馈方式。
const standardOpinionSelectPlaceholder = computed(() => {
  return standardOpinionLoading.value
    ? `${standardOpinionFieldLabel.value}加载中...`
    : `请选择${standardOpinionFieldLabel.value}`
})

const createTimeColumnLabel = computed(() => (isNewWordVariant.value ? '处理时间' : '创建时间'))

const updatedTimeColumnLabel = computed(() => '更新时间')

const operatorColumnLabel = computed(() => '操作人')

const listCardTitle = computed(() => (isNewWordVariant.value ? '新词列表' : '语料列表'))

const createButtonText = computed(() => (isNewWordVariant.value ? '新建新词' : '新建语料'))

const weekDisplay = computed(() => formatWeekRangeLabel(queryForm.time_range))

const timeRangeShortcutValue = ref('本月')

const createDialogVisible = ref(false)
const createDialogMode = ref<'create' | 'edit'>('create')
const editingRow = ref<any | null>(null)
const detailDialogVisible = ref(false)
const detailRow = ref<any | null>(null)

const selectedRows = ref<any[]>([])
const selectedIds = ref<(string | number)[]>([])

// 统一同步勾选 ID，保证批量操作始终基于最新勾选结果
const syncSelectedIds = () => {
  selectedIds.value = (selectedRows.value || [])
    .map(item => item?.id)
    .filter(id => id !== undefined && id !== null)
}

const clearSelection = () => {
  selectedRows.value = []
  selectedIds.value = []
}

const selectedRowKeySet = computed(() => {
  return new Set((selectedRows.value || []).map(item => String(item?.id ?? '')))
})

const isRowSelected = (row: any) => {
  return selectedRowKeySet.value.has(String(row?.id ?? ''))
}

const isAllSelected = computed(() => {
  const list = Array.isArray(table.list) ? table.list : []
  return !!list.length && list.every(row => isRowSelected(row))
})

const isIndeterminate = computed(() => {
  const list = Array.isArray(table.list) ? table.list : []
  if (!list.length) return false
  const selectedCount = list.filter(row => isRowSelected(row)).length
  return selectedCount > 0 && selectedCount < list.length
})

const handleRowSelectChange = (checked: boolean, row: any) => {
  const rowKey = String(row?.id ?? '')
  const exists = selectedRowKeySet.value.has(rowKey)
  if (checked && !exists) {
    selectedRows.value = [...selectedRows.value, row]
  }
  if (!checked && exists) {
    selectedRows.value = selectedRows.value.filter(item => String(item?.id ?? '') !== rowKey)
  }
  syncSelectedIds()
}

const handleSelectAllChange = (checked: boolean) => {
  if (checked) {
    selectedRows.value = Array.isArray(table.list) ? [...table.list] : []
    syncSelectedIds()
    return
  }
  clearSelection()
}

// 分页切换时与原表格行为保持一致：清空当前页勾选
const handleTablePageSizeChange = (size: number) => {
  clearSelection()
  handleSizeChange(size)
}

const handleTablePageCurrentChange = (page: number) => {
  clearSelection()
  handleCurrentChange(page)
}

/**
 * 当前页仅开放“标准观点、创建时间、更新时间”三列的接口排序，
 * 点击后按 sort_by / sort_order 新参数口径请求，切换列时默认先按升序。
 */
const toggleColumnSort = (columnKey: string) => {
  if (!Object.prototype.hasOwnProperty.call(SORT_FIELD_MAP, columnKey)) return
  if (!ensureStandardOpinionQueryAllowed()) return

  const nextKey = columnKey as CorpusSortColumnKey
  if (tableSortState.key === nextKey) {
    tableSortState.order = tableSortState.order === 'asc' ? 'desc' : 'asc'
  } else {
    tableSortState.key = nextKey
    tableSortState.order = 'asc'
  }

  clearSelection()
  syncTableFilters()
  getFirstPageTableData()
}

/**
 * 判断列头是否需要展示排序交互，避免把选择列、操作列等非接口字段误做排序入口。
 */
const isSortableColumn = (columnKey: string) => {
  return Object.prototype.hasOwnProperty.call(SORT_FIELD_MAP, columnKey)
}

watch(
  () => table.list,
  () => {
    clearSelection()
  }
)

const tableColumns = computed(() => {
  const isFixedTextCorpusSubjectColumn = !isSurveyCorpus.value && !isNewWordVariant.value
  // 文本语料普通页的主体列固定收窄，避免挤占描述列的三行展示空间。
  const subjectColumnWidthConfig = isFixedTextCorpusSubjectColumn
    ? {
        width: 180,
        minWidth: 180,
        baseWidth: 180
      }
    : {
        width: 480,
        minWidth: 480,
        baseWidth: 180,
        flexWeight: 1
      }

  const columns: any[] = [
    {
      key: 'selection',
      dataKey: 'selection',
      title: '',
      width: 56,
      baseWidth: 56,
      align: 'center',
      fixed: 'left'
    },
    {
      key: 'subject',
      dataKey: 'subject',
      title: subjectColumnLabel.value,
      ...subjectColumnWidthConfig
    },
    {
      key: 'standardOpinion',
      dataKey: 'standardOpinion',
      title: standardOpinionColumnLabel.value,
      width: 440,
      minWidth: 180,
      baseWidth: 220,
      flexWeight: 2
    }
  ]

  if (!isSurveyCorpus.value) {
    columns.splice(2, 0, {
      key: 'description',
      dataKey: 'description',
      title: '描述',
      width: 260,
      minWidth: 260,
      baseWidth: 260
    })
  }

  if (!isNewWordVariant.value) {
    columns.push({
      key: 'intention',
      dataKey: 'intention',
      title: '意图',
      width: 90,
      minWidth: 90,
      baseWidth: 90
    })
  }

  if (isNewWordVariant.value) {
    columns.push({
      key: 'frequency',
      dataKey: 'frequency',
      title: '频率',
      width: 100,
      baseWidth: 100,
      align: 'center'
    })
  }

  columns.push({
    key: 'createTime',
    dataKey: 'createTime',
    title: createTimeColumnLabel.value,
    width: 180,
    minWidth: 160,
    baseWidth: 180,
    flexWeight: 1
  })

  if (!isNewWordVariant.value) {
    columns.push({
      key: 'updatedTime',
      dataKey: 'updatedTime',
      title: updatedTimeColumnLabel.value,
      width: 180,
      minWidth: 160,
      baseWidth: 180,
      flexWeight: 1
    })
    columns.push({
      key: 'operator',
      dataKey: 'operator',
      title: operatorColumnLabel.value,
      width: 150,
      minWidth: 130,
      baseWidth: 150,
      flexWeight: 1
    })
  }

  columns.push({
    key: 'status',
    dataKey: 'status',
    title: isNewWordVariant.value ? '状态' : '启用状态',
    width: 110,
    baseWidth: 110,
    fixed: 'right'
  })
  columns.push({
    key: 'operation',
    dataKey: 'operation',
    title: '操作',
    width: 100,
    baseWidth: 100,
    fixed: 'right'
  })
  return columns
})

/**
 * 根据表格容器宽度计算最终列宽，保证列尽量撑满整行。
 * 规则：以 baseWidth 为基础宽度，仅把剩余空间按 flexWeight 分配给文本列。
 */
const resolveTableColumns = (containerWidth: number) => {
  const sourceColumns = (tableColumns.value || []) as any[]
  if (!sourceColumns.length) return []

  const totalBaseWidth = sourceColumns.reduce((sum, column) => {
    return sum + Number(column?.baseWidth ?? column?.width ?? 0)
  }, 0)

  const extraWidth = Math.max(0, Number(containerWidth || 0) - totalBaseWidth)
  const totalFlexWeight = sourceColumns.reduce((sum, column) => {
    return sum + Number(column?.flexWeight ?? 0)
  }, 0)

  return sourceColumns.map(column => {
    const { baseWidth, flexWeight, ...rest } = column
    const base = Number(baseWidth ?? column?.width ?? 0)
    const minWidth = Number(column?.minWidth ?? 0)

    let nextWidth = base
    if (extraWidth > 0 && totalFlexWeight > 0 && Number(flexWeight) > 0) {
      const growWidth = (extraWidth * Number(flexWeight)) / totalFlexWeight
      nextWidth = base + growWidth
    }

    const width = Math.max(minWidth || 0, Math.floor(nextWidth))
    return {
      ...rest,
      width
    }
  })
}

type BatchAction = 'enable' | 'disable' | 'move'

const openCreateDialog = () => {
  createDialogMode.value = 'create'
  editingRow.value = null
  createDialogVisible.value = true
}

const openEditDialog = (row: any) => {
  if (isNewWordVariant.value) {
    detailRow.value = row
    detailDialogVisible.value = true
    return
  }
  createDialogMode.value = 'edit'
  editingRow.value = row
  createDialogVisible.value = true
}

const deriveStatusName = (row: any): 'enabled' | 'disabled' => {
  const rawStatus = row.status ?? row.enable_status ?? row.auditStatusCode ?? row.auditStatus ?? ''
  if (rawStatus === 'enabled') {
    return 'enabled'
  }
  if (rawStatus === 'disabled') {
    return 'disabled'
  }
  const statusKey = `${rawStatus}`
  if (statusKey === '1' || statusKey === '2') {
    return 'enabled'
  }
  return 'disabled'
}

const handleCreateSubmit = async (payload: {
  subject: string
  description: string
  standardOpinionId: string
  enableStatus: 'enabled' | 'disabled'
  variant: 'text' | 'survey'
}) => {
  if (createDialogMode.value === 'edit') {
    if (!editingRow.value) {
      ElMessage.error('未找到待编辑的语料数据')
      return
    }
    try {
      const row = editingRow.value
      const idValue = row.id
      if (idValue === undefined || idValue === null) {
        ElMessage.error('当前记录缺少ID，无法编辑')
        return
      }
      const opinionType = props.opinionType ?? (payload.variant === 'survey' ? 0 : 1)
      const standardOpinionId =
        payload.standardOpinionId || row.standard_opinion_id || row.standardOpinionId || ''
      const standardOpinionText =
        findStandardOpinionLabelByCode(standardOpinionId) ||
        row.standard_opinion ||
        row.standardOpinion ||
        ''
      const nextData: any = {
        standard_opinion: standardOpinionText,
        standard_opinion_id: standardOpinionId,
        status_name: payload.enableStatus,
        opinion_type: opinionType
      }
      if (opinionType === 0) {
        nextData.opinion = payload.subject
      } else {
        nextData.entity = payload.subject
        nextData.description = payload.description
      }
      const loginMeta = getLoginUserMetaOrNull()
      if (loginMeta) {
        nextData.operator = loginMeta.operatorName
      }
      await updateOpinionSynonymById({
        old_id: Number(idValue),
        new: nextData
      })
      ElMessage.success('编辑成功')
      refreshTableData(false)
    } catch (error: any) {
      ElMessage.error(error?.message || '编辑失败')
    }
    return
  }
  try {
    const opinionType = props.opinionType ?? (payload.variant === 'survey' ? 0 : 1)
    const standardOpinionId = payload.standardOpinionId
    const standardOpinionText =
      findStandardOpinionLabelByCode(standardOpinionId) || standardOpinionId
    const payloadBody: any = {
      standard_opinion: standardOpinionText,
      standard_opinion_id: standardOpinionId,
      status_name: payload.enableStatus,
      opinion_type: opinionType
    }
    if (opinionType === 0) {
      payloadBody.opinion = payload.subject
    } else {
      payloadBody.entity = payload.subject
      payloadBody.description = payload.description
    }
    const loginMeta = getLoginUserMetaOrNull()
    if (loginMeta) {
      payloadBody.operator = loginMeta.operatorName
    }
    await createOpinionSynonym(payloadBody)
    ElMessage.success('新增成功')
    refreshTableData()
  } catch (error: any) {
    ElMessage.error(error?.message || '新增失败')
  }
}

const handleBatchUpdateStatus = async (status: 'enabled' | 'disabled') => {
  if (!selectedRows.value.length) {
    ElMessage.warning('请先选择语料')
    return
  }
  const opinionType = props.opinionType ?? 1
  const statusText = status === 'enabled' ? '启用' : '禁用'
  const loginMeta = getLoginUserMetaOrNull()
  if (!loginMeta) {
    ElMessage.error('缺少登录用户信息，无法批量操作')
    return
  }

  try {
    const oldIdList: number[] = []
    const newList: any[] = []

    selectedRows.value.forEach(row => {
      const idValue = row?.id
      if (idValue === undefined || idValue === null) {
        return
      }
      const standardOpinion = row.standard_opinion ?? row.standardOpinion ?? ''
      const standardOpinionId = row.standard_opinion_id || row.standardOpinionId || standardOpinion
      const nextData: any = {
        standard_opinion: standardOpinion,
        standard_opinion_id: standardOpinionId,
        status_name: status,
        opinion_type: opinionType,
        operator: loginMeta.operatorName
      }
      if (opinionType === 0) {
        nextData.opinion = row.opinion
      } else {
        nextData.entity = row.entity
        nextData.description = row.description
      }

      oldIdList.push(Number(idValue))
      newList.push(nextData)
    })

    if (!oldIdList.length) {
      ElMessage.warning('选中的语料缺少有效ID，无法批量操作')
      return
    }

    await updateOpinionSynonymById({
      old_id: oldIdList,
      new: newList
    })

    ElMessage.success(`批量${statusText}成功`)
    selectedRows.value = []
    selectedIds.value = []
    refreshTableData()
  } catch (error: any) {
    ElMessage.error(error?.message || `批量${statusText}失败`)
  }
}

const batchDialogVisible = ref(false)
const batchDialogAction = ref<BatchAction>('enable')
const batchMoveStandardOpinionId = ref('')

const openBatchDialog = (action: BatchAction) => {
  if (!selectedRows.value.length) {
    ElMessage.warning('请先选择语料')
    return
  }
  batchDialogAction.value = action
  if (action === 'move') {
    batchMoveStandardOpinionId.value = ''
  }
  batchDialogVisible.value = true
}

const handleBatchCommand = (command: BatchAction) => {
  if (command === 'enable' || command === 'disable' || command === 'move') {
    openBatchDialog(command)
  }
}

const handleBatchConfirm = async ({ close }: { close: () => void }) => {
  if (!selectedRows.value.length) {
    ElMessage.warning('请先选择语料')
    return
  }
  const action = batchDialogAction.value
  if (action === 'move' && !batchMoveStandardOpinionId.value) {
    ElMessage.warning('请选择目标标准观点')
    return
  }

  if (action === 'enable' || action === 'disable') {
    await handleBatchUpdateStatus(action === 'enable' ? 'enabled' : 'disabled')
    close()
    return
  }

  const loginMeta = getLoginUserMetaOrNull()
  if (!loginMeta) {
    ElMessage.error('缺少登录用户信息，无法批量操作')
    return
  }

  const opinionType = props.opinionType ?? 1
  const targetId = batchMoveStandardOpinionId.value
  const targetOption = allStandardOpinionOptions.value.find(
    item => String(item.tagCode) === String(targetId)
  )
  const targetName = targetOption?.tagName || ''
  if (!targetId || !targetName) {
    ElMessage.error('请选择有效的目标标准观点')
    return
  }

  try {
    const oldIdList: number[] = []
    const newList: any[] = []

    selectedRows.value.forEach(row => {
      const idValue = row?.id
      if (idValue === undefined || idValue === null) {
        return
      }
      const nextData: any = {
        standard_opinion: targetName,
        standard_opinion_id: targetId,
        status_name: deriveStatusName(row),
        opinion_type: opinionType,
        operator: loginMeta.operatorName
      }
      if (opinionType === 0) {
        nextData.opinion = row.opinion ?? row.subject
      } else {
        nextData.entity = row.entity ?? row.subject
        nextData.description = row.description ?? row.subject_desc
      }

      oldIdList.push(Number(idValue))
      newList.push(nextData)
    })

    if (!oldIdList.length) {
      ElMessage.warning('选中的语料缺少有效ID，无法批量移动')
      return
    }

    await updateOpinionSynonymById({
      old_id: oldIdList,
      new: newList
    })

    ElMessage.success('批量移动成功')
    selectedRows.value = []
    selectedIds.value = []
    refreshTableData()
    close()
  } catch (error: any) {
    ElMessage.error(error?.message || '批量移动失败')
  }
}
</script>

<template>
  <div>
    <FtCard title="筛选条件" model="titleOperation" clear-content-top-padding>
      <div class="flex w-full text-corpus">
        <div class="flex-1">
          <el-form layout="inline" :model="queryForm">
            <el-row v-if="!isNewWordVariant" class="w-full" :gutter="24">
              <el-col :span="8">
                <el-form-item :label="subjectKeywordLabel" class="mb-18">
                  <el-input
                    v-model.trim="queryForm.subjectKeyword"
                    :placeholder="subjectKeywordPlaceholder"
                    maxlength="100"
                    clearable
                  />
                </el-form-item>
              </el-col>
              <el-col v-if="!isSurveyCorpus" :span="8">
                <el-form-item label="描述" class="mb-18">
                  <el-input
                    v-model.trim="queryForm.descriptionKeyword"
                    placeholder="请输入描述"
                    maxlength="100"
                    clearable
                  />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="意图" class="mb-18">
                  <el-select
                    v-model="queryForm.intention"
                    placeholder="请选择意图"
                    clearable
                    class="w-full"
                  >
                    <el-option
                      v-for="item in intentionOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <!-- 按筛选习惯先放操作人，再进入体验代码和标准观点联动筛选 -->
                <el-form-item label="操作人">
                  <el-input
                    v-model.trim="queryForm.operatorKeyword"
                    placeholder="请输入操作人（姓名/工号）"
                    maxlength="50"
                    clearable
                  />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="体验代码">
                  <el-cascader
                    v-model="experienceCodeSelection"
                    :options="experienceCodeOptions"
                    :props="experienceCodeCascaderProps"
                    class="w-full"
                    collapse-tags
                    collapse-tags-tooltip
                    clearable
                    filterable
                    :disabled="experienceCodeLoading"
                    :show-all-levels="false"
                    :placeholder="experienceCodeLoading ? '体验代码加载中...' : '请选择体验代码'"
                    :before-filter="handleExperienceCodeBeforeFilter"
                    :filter-method="filterExperienceCodeMethod"
                    @change="handleExperienceCodeChange"
                  >
                    <template #suggestion-item="{ item }">
                      <span>{{ formatExperienceCodeSuggestionPath(item) }}</span>
                    </template>
                  </el-cascader>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="标准观点">
                  <el-select-v2
                    ref="standardOpinionSelectRef"
                    :model-value="standardOpinionSelection"
                    :loading="standardOpinionLoading"
                    :disabled="isStandardOpinionSelectDisabled"
                    :options="filterStandardOpinionOptions"
                    :props="{ label: 'tagName', value: 'tagCode' }"
                    :placeholder="standardOpinionSelectPlaceholder"
                    class="w-full"
                    clearable
                    filterable
                    multiple
                    collapse-tags
                    :max-collapse-tags="1"
                    :fit-input-width="false"
                    @update:model-value="handleStandardOpinionModelValueChange"
                  >
                    <template #header>
                      <div
                        class="select-v2-header"
                        @mousedown.prevent
                        @click.stop="toggleStandardOpinionSelectAll"
                      >
                        <el-checkbox
                          :model-value="isStandardOpinionAllDisplayedSelected"
                          :indeterminate="isStandardOpinionDisplayedIndeterminate"
                          @click.prevent
                        >
                          全选
                        </el-checkbox>
                      </div>
                    </template>
                  </el-select-v2>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="启用状态">
                  <el-select v-model="queryForm.enable_status" placeholder="请选择启用状态">
                    <el-option
                      v-for="item in enableStatusOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row v-else class="w-full" :gutter="24">
              <el-col :span="8">
                <el-form-item label="时间范围">
                  <FDatePicker
                    v-model="queryForm.time_range"
                    v-model:shortcutValue="timeRangeShortcutValue"
                    type="daterange"
                    :clearable="false"
                    class="corpus-mapping-time-range"
                  ></FDatePicker>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="体验代码">
                  <el-cascader
                    v-model="experienceCodeSelection"
                    :options="experienceCodeOptions"
                    :props="experienceCodeCascaderProps"
                    class="w-full"
                    collapse-tags
                    collapse-tags-tooltip
                    clearable
                    filterable
                    :disabled="experienceCodeLoading"
                    :show-all-levels="false"
                    :placeholder="experienceCodeLoading ? '体验代码加载中...' : '请选择体验代码'"
                    :before-filter="handleExperienceCodeBeforeFilter"
                    :filter-method="filterExperienceCodeMethod"
                    @change="handleExperienceCodeChange"
                  >
                    <template #suggestion-item="{ item }">
                      <span>{{ formatExperienceCodeSuggestionPath(item) }}</span>
                    </template>
                  </el-cascader>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item :label="standardOpinionFieldLabel">
                  <el-select-v2
                    ref="standardOpinionSelectRef"
                    :model-value="standardOpinionSelection"
                    :loading="standardOpinionLoading"
                    :disabled="isStandardOpinionSelectDisabled"
                    :options="filterStandardOpinionOptions"
                    :props="{ label: 'tagName', value: 'tagCode' }"
                    :placeholder="standardOpinionSelectPlaceholder"
                    class="w-full"
                    clearable
                    filterable
                    multiple
                    collapse-tags
                    :max-collapse-tags="1"
                    :fit-input-width="false"
                    @update:model-value="handleStandardOpinionModelValueChange"
                  >
                    <template #header>
                      <div
                        class="select-v2-header"
                        @mousedown.prevent
                        @click.stop="toggleStandardOpinionSelectAll"
                      >
                        <el-checkbox
                          :model-value="isStandardOpinionAllDisplayedSelected"
                          :indeterminate="isStandardOpinionDisplayedIndeterminate"
                          @click.prevent
                        >
                          全选
                        </el-checkbox>
                      </div>
                    </template>
                  </el-select-v2>
                </el-form-item>
              </el-col>
              <el-col :span="4">
                <el-form-item :label="filterSubjectLabel">
                  <el-input
                    v-model.trim="queryForm.subject_desc"
                    :placeholder="filterSubjectPlaceholder"
                    maxlength="100"
                    clearable
                  />
                </el-form-item>
              </el-col>
              <el-col :span="4">
                <el-form-item :label="isNewWordVariant ? '处理状态' : '启用状态'">
                  <el-select
                    v-if="isNewWordVariant"
                    v-model="queryForm.process_status"
                    placeholder="请选择处理状态"
                  >
                    <el-option label="不限" value="all" />
                    <el-option label="待处理" value="pending" />
                    <el-option label="已处理" value="processed" />
                  </el-select>
                  <el-select v-else v-model="queryForm.enable_status" placeholder="请选择启用状态">
                    <el-option
                      v-for="item in enableStatusOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>
        <div class="w-220 border-left-e5e6eb">
          <div class="w-full h-full flex justify-center items-center">
            <div>
              <el-button type="primary" @click="query">
                <el-icon style="vertical-align: middle" class="mr-10">
                  <Search />
                </el-icon>
                查询
              </el-button>
            </div>
            <div class="ml-20">
              <el-button color="#F2F3F5" class="" @click="reset">
                <el-icon style="vertical-align: middle" class="mr-10"><RefreshRight /></el-icon>
                重置
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </FtCard>

    <FtCard
      :style="computedCardHeight(178)"
      :title="listCardTitle"
      model="titleOperation"
      clear-content-top-padding
      class="mt-24"
    >
      <template #extra v-if="!isNewWordVariant">
        <div class="flex gap-12">
          <el-dropdown trigger="click" placement="bottom-end" @command="handleBatchCommand as any">
            <el-button text bg :disabled="!selectedRows.length">批量操作</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="enable">启用</el-dropdown-item>
                <el-dropdown-item command="disable">禁用</el-dropdown-item>
                <el-dropdown-item command="move">移动</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-button type="primary" @click="openCreateDialog">
            <template #icon>
              <el-icon>
                <el-icon-plus />
              </el-icon>
            </template>
            {{ createButtonText }}</el-button
          >
        </div>
      </template>

      <div class="corpus-mapping-table" v-loading="table.loading">
        <el-auto-resizer>
          <template #default="{ height, width }">
            <el-table-v2
              :columns="resolveTableColumns(width)"
              :data="table.list"
              :width="width"
              :height="height"
              :header-height="60"
              :row-height="60"
              row-key="id"
              fixed
            >
              <template #header-cell="{ column }">
                <template v-if="column.key === 'selection'">
                  <el-checkbox
                    :indeterminate="isIndeterminate"
                    :model-value="isAllSelected"
                    @change="handleSelectAllChange"
                  />
                </template>
                <template v-else-if="isSortableColumn(String(column.key || ''))">
                  <div
                    class="corpus-mapping-sortable-header"
                    @click="toggleColumnSort(String(column.key || ''))"
                  >
                    <span>{{ column.title }}</span>
                    <span
                      class="corpus-mapping-sortable-header__caret-wrapper"
                      :class="{
                        ascending:
                          tableSortState.key === String(column.key || '') &&
                          tableSortState.order === 'asc',
                        descending:
                          tableSortState.key === String(column.key || '') &&
                          tableSortState.order === 'desc'
                      }"
                    >
                      <i class="sort-caret ascending"></i>
                      <i class="sort-caret descending"></i>
                    </span>
                  </div>
                </template>
                <template v-else>
                  {{ column.title }}
                </template>
              </template>

              <template #cell="{ column, rowData }">
                <template v-if="column.key === 'selection'">
                  <el-checkbox
                    :model-value="isRowSelected(rowData)"
                    @change="(val: boolean) => handleRowSelectChange(val, rowData)"
                  />
                </template>

                <template v-else-if="column.key === 'subject'">
                  <el-tooltip
                    :content="getSubjectText(rowData)"
                    placement="top"
                    :disabled="getSubjectText(rowData) === '-'"
                    popper-class="common-tooltip"
                  >
                    <div class="cell-ellipsis-multiline">
                      {{ getSubjectText(rowData) }}
                    </div>
                  </el-tooltip>
                </template>

                <template v-else-if="column.key === 'description'">
                  <el-tooltip
                    :content="getDescriptionText(rowData)"
                    placement="top"
                    :disabled="getDescriptionText(rowData) === '-'"
                    popper-class="common-tooltip"
                  >
                    <div class="cell-ellipsis-multiline">{{ getDescriptionText(rowData) }}</div>
                  </el-tooltip>
                </template>

                <template v-else-if="column.key === 'standardOpinion'">
                  <el-tooltip
                    :content="getStandardOpinionText(rowData)"
                    placement="top"
                    :disabled="getStandardOpinionText(rowData) === '-'"
                    popper-class="common-tooltip"
                  >
                    <div class="cell-ellipsis">{{ getStandardOpinionText(rowData) }}</div>
                  </el-tooltip>
                </template>

                <template v-else-if="column.key === 'intention'">
                  <el-tooltip
                    :content="getIntentionText(rowData)"
                    placement="top"
                    :disabled="!getIntentionText(rowData)"
                    popper-class="common-tooltip"
                  >
                    <div class="cell-ellipsis">{{ getIntentionText(rowData) || '-' }}</div>
                  </el-tooltip>
                </template>

                <template v-else-if="column.key === 'frequency'">
                  {{ getFrequencyText(rowData) }}
                </template>

                <template v-else-if="column.key === 'createTime'">
                  <el-tooltip
                    :content="getCreateTimeText(rowData)"
                    placement="top"
                    :disabled="getCreateTimeText(rowData) === '-'"
                    popper-class="common-tooltip"
                  >
                    <div class="cell-ellipsis">{{ getCreateTimeText(rowData) }}</div>
                  </el-tooltip>
                </template>

                <template v-else-if="column.key === 'updatedTime'">
                  <el-tooltip
                    :content="getUpdatedTimeText(rowData)"
                    placement="top"
                    :disabled="!getUpdatedTimeText(rowData)"
                    popper-class="common-tooltip"
                  >
                    <div class="cell-ellipsis">{{ getUpdatedTimeText(rowData) || '-' }}</div>
                  </el-tooltip>
                </template>

                <template v-else-if="column.key === 'operator'">
                  <el-tooltip
                    :content="getOperatorText(rowData)"
                    placement="top"
                    :disabled="getOperatorText(rowData) === '-'"
                    popper-class="common-tooltip"
                  >
                    <div class="cell-ellipsis">{{ getOperatorText(rowData) }}</div>
                  </el-tooltip>
                </template>

                <template v-else-if="column.key === 'status'">
                  <div class="flex-y-center">
                    <div class="status-icon mr-8" :style="getEnableStatusTagStyle(rowData)"></div>
                    <span>{{ getEnableStatusText(rowData) }}</span>
                  </div>
                </template>

                <template v-else-if="column.key === 'operation'">
                  <el-button link type="primary" @click="openEditDialog(rowData)">
                    {{ isNewWordVariant ? '查看' : '编辑' }}
                  </el-button>
                </template>
              </template>
            </el-table-v2>
          </template>
        </el-auto-resizer>
      </div>

      <el-pagination
        v-if="table.total > 0"
        v-model:current-page="table.pageNum"
        v-model:page-size="table.pageSize"
        :page-sizes="[10, 20, 50, 100, 200, 500, 1000]"
        :total="table.total"
        layout="total, sizes, prev, pager, next"
        @size-change="handleTablePageSizeChange"
        @current-change="handleTablePageCurrentChange"
        class="mt-16 flex justify-end"
      />
    </FtCard>

    <CorpusMappingCreateDialog
      v-model:visible="createDialogVisible"
      :mode="createDialogMode"
      :variant="isSurveyCorpus ? 'survey' : 'text'"
      :standard-opinion-loading="standardOpinionLoading"
      :standard-opinions="allStandardOpinionOptions"
      :initial-values="
        createDialogMode === 'edit'
          ? {
              subject: getSubjectText(editingRow || {}),
              description: getDescriptionText(editingRow || {}),
              standardOpinionId:
                (editingRow && (editingRow.standard_opinion_id || editingRow.standardOpinionId)) ||
                '',
              enableStatus: deriveStatusName(editingRow || {})
            }
          : undefined
      "
      @submit="handleCreateSubmit"
    ></CorpusMappingCreateDialog>

    <NewWordDetailsDialog
      v-if="isNewWordVariant"
      v-model:visible="detailDialogVisible"
      :row="detailRow"
      :time-range="queryForm.time_range"
      :opinion-type="isSurveyCorpus ? 0 : 1"
      @refresh="refreshTableData(false)"
    />

    <AppDialog
      v-if="!isNewWordVariant"
      v-model:visible="batchDialogVisible"
      :title="
        batchDialogAction === 'enable'
          ? '批量启用'
          : batchDialogAction === 'disable'
          ? '批量禁用'
          : '批量移动'
      "
      width="480px"
      :confirm="handleBatchConfirm"
    >
      <template v-if="batchDialogAction === 'enable'"> 是否确认批量启用选中语料？ </template>
      <template v-else-if="batchDialogAction === 'disable'"> 是否确认批量禁用选中语料？ </template>
      <template v-else>
        <el-form label-width="80px">
          <el-form-item label="标准观点">
            <el-select-v2
              v-model="batchMoveStandardOpinionId"
              :options="allStandardOpinionOptions"
              :props="{ label: 'tagName', value: 'tagCode' }"
              placeholder="请选择标准观点"
              filterable
              clearable
              class="w-full"
              :fit-input-width="false"
              popper-class="corpus-mapping-standard-opinion-popper"
              placement="bottom"
            />
          </el-form-item>
        </el-form>
      </template>
    </AppDialog>
  </div>
</template>

<style lang="scss" scoped>
.corpus-mapping-table {
  height: calc(100% - 48px);
}

.corpus-mapping-table :deep(.el-table-v2__row-cell),
.corpus-mapping-table :deep(.el-table-v2__header-cell) {
  height: 55px;
  padding-top: 0;
  padding-bottom: 0;
}

.corpus-mapping-page .corpus-mapping-table :deep(.el-table-v2__row-cell) {
  color: #1d2129;
}

.corpus-mapping-table :deep(.cell-ellipsis) {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.corpus-mapping-table :deep(.cell-ellipsis-multiline) {
  line-height: 18px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  word-break: break-all;
  text-align: justify;
}

.corpus-mapping-sortable-header {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  width: 100%;
  cursor: pointer;
  user-select: none;
}

.corpus-mapping-sortable-header__caret-wrapper {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  width: 24px;
  height: 14px;
  overflow: initial;
  position: relative;
  flex-shrink: 0;
}

.corpus-mapping-sortable-header__caret-wrapper .sort-caret {
  width: 0;
  height: 0;
  border: solid 5px transparent;
  position: absolute;
  left: 7px;
  display: block;
}

.corpus-mapping-sortable-header__caret-wrapper .sort-caret.ascending {
  border-bottom-color: var(--el-text-color-placeholder);
  top: -5px;
}

.corpus-mapping-sortable-header__caret-wrapper .sort-caret.descending {
  border-top-color: var(--el-text-color-placeholder);
  bottom: -3px;
}

.corpus-mapping-sortable-header__caret-wrapper.ascending .sort-caret.ascending {
  border-bottom-color: var(--el-color-primary);
}

.corpus-mapping-sortable-header__caret-wrapper.descending .sort-caret.descending {
  border-top-color: var(--el-color-primary);
}

.corpus-mapping-time-range {
  width: 100% !important;
  max-width: 320px !important;
}

.select-v2-header {
  padding-left: 10px;
}

.corpus-mapping-standard-opinion-popper {
  width: 600px !important;
  max-width: calc(100vw - 40px);
}

.corpus-mapping-standard-opinion-popper .el-select-dropdown {
  width: 100% !important;
}

.corpus-mapping-standard-opinion-popper .el-select-dropdown__list {
  width: 100% !important;
}

.corpus-mapping-standard-opinion-popper .el-virtual-list__window {
  width: 100% !important;
}

.corpus-mapping-standard-opinion-popper .el-select-dropdown__item {
  width: 100%;
}
</style>
