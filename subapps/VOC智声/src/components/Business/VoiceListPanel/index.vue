<script setup lang="ts">
// 列表+详情通用面板（PC端）
// - 支持排序下拉
// - 支持“是否高质量声音”筛选
// - 支持高质量标记/取消标记（受权限控制）
// - 支持单条/批量事件下发前端交互
// - 右侧展示详情
import { ref, reactive, onMounted, onBeforeUnmount, watch, computed, getCurrentInstance } from 'vue'
import { ElMessage } from 'element-plus'
import { cloneDeep, debounce, throttle } from 'lodash-es'

import CommonTitle from '@components/Business/DrillDownDialog/components/CommonTitle'
import TheDetails from './TheDetails.vue'
import ErrorCorrectionDialog from './components/ErrorCorrectionDialog.vue'
import BatchMarkDialog from './components/BatchMarkDialog.vue'
import EventIssueDialog from './components/EventIssueDialog.vue'

import { getRealAttr } from '@/views/leaderOverview/leader/common/fn.ts'
import { useQueryStore } from '@/store/modules/query'
import useUserStore from '@/store/modules/user'
import { useLoading } from '@/hooks/useLoading'
import { useErrorCorrectionDialog } from '@/hooks/useErrorCorrectionDialog'
import { toRgba } from '@/utils'
import { sentimentColors } from '@/constants'

import { postVocListSoundsByUrl, highQuality, highQualityDel } from '@/api/overview/leader'
import { FunctionPermission } from '@/constants/btnPermMap'
import { Search } from '@element-plus/icons-vue'
import type { BatchEventDataSourceType, BatchEventTopicOption } from '@/api/batchEvent/types'

defineOptions({ name: 'VoiceListPanel' })

type UserDetailMode = 'back' | 'close'

// 可配置项（尽量与现有两处使用场景向后兼容）
interface Props {
  // 标题文案
  title?: string
  // 外部传入查询参数（用于脱离 queryStore.currentQueryParams 的场景）
  // 说明：不传则沿用现有逻辑（依赖 queryStore.currentQueryParams + 领导页品牌洞察筛选兜底）
  queryParams?: Partial<VocQueryParams> & Record<string, any>
  // 外部查询参数组装模式
  // merge-default=沿用现有通用逻辑；external-only=仅保留 queryParams 与分页参数
  queryParamsMode?: 'merge-default' | 'external-only'
  // 列表接口地址（POST）
  // 说明：默认使用领导版列表接口；如需复用为其它场景，可传入其它等价返回结构的接口地址
  listApiUrl?: string
  // 详情接口地址（POST）
  // 说明：默认使用领导版详情接口；下钻等场景可切换到其它兼容接口
  detailApiUrl?: string
  // 默认分页大小
  defaultPageSize?: number
  // 是否监听全局 queryStore.currentQueryParams
  // 说明：下钻/独立查询场景应关闭，仅依赖外部传入 queryParams
  watchStoreQuery?: boolean
  // 容器模式：card=默认卡片；embedded=嵌入现有容器，不额外渲染卡片外观
  containerMode?: 'card' | 'embedded'
  // 是否显示排序下拉
  showSortSelect?: boolean
  // 是否展示“已打标/未打标”筛选
  showDataStatus?: boolean
  // 是否展示“是否高质量声音”筛选
  showHighQualityFilter?: boolean
  // 是否启用高质量标记/取消标记按钮（最终仍受权限控制）
  enableHighQualityActions?: boolean
  // 是否启用 高质量标记/取消标记按钮 及 高质量文本标记
  enableHighQualityInfo?: boolean
  // 列表是否展示观点（topics）
  showTopicsInList?: boolean
  // 详情数据来源：remote=详情接口；list=直接使用列表项数据
  detailSource?: 'remote' | 'list'
  // 详情是否展示识别观点
  showTopicsInDetail?: boolean
  // 详情是否展示品牌车系
  showBrandSeriesInDetail?: boolean
  // 详情是否展示关联事件
  showRelationEventsInDetail?: boolean
  // 是否启用 voiceManagementParams 过滤并监听（系统-客户原声需要，领导页默认不需要）
  enableVoiceManagementParams?: boolean
  // 是否启用数据纠错（仅系统-声音标记需要）
  enableErrorCorrection?: boolean
  // 是否显示批量操作按钮
  showBatchAction?: boolean
  // 是否启用事件下发操作（单条事件下发与批量下发）
  enableEventIssueAction?: boolean
  // 事件下发数据源类型，原始数据下发时 id 缺失可回退 dataId
  eventIssueDataSourceType?: BatchEventDataSourceType
  // 事件下发聚焦观点选项；原始数据场景用于复用结果数据标准观点全量数据源
  eventIssueTopicOptions?: BatchEventTopicOption[]
  // 是否显示导出数据按钮
  showExportAction?: boolean
  // 是否显示导出事件数据按钮
  showEventExportAction?: boolean
  // 导出事件数据按钮 loading 状态，供后续接入真实导出接口时由父层控制
  exportActionLoading?: boolean
  // 导出数据按钮 loading 状态，兼容原声查询等历史调用方
  exportLoading?: boolean
  // 是否显示关键词搜索
  showKeywordSearch?: boolean
  // 是否显示详情区“添加语料”按钮
  showCorpusCreateAction?: boolean
  // 场景专属提示文案；不传时不展示，避免影响其它复用场景
  sceneTipText?: string
  // 用户详情关闭模式：下钻内使用 back，其它独立页面使用 close
  userDetailMode?: UserDetailMode
  // 是否显示情感多选筛选
  showSentimentFilter?: boolean
  // 情感多选筛选写入的查询字段，兼容不同后端接口字段约定
  sentimentQueryField?: 'sentiment' | 'sentimentList'
  // 是否显示情感程度筛选，仅在情感分支数据场景启用
  showEmotionalLevelFilter?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  title: '客户原声',
  listApiUrl: '/report/vocLeadership/getVocListSounds',
  detailApiUrl: '/report/vocLeadership/getSoundsDetails',
  queryParamsMode: 'merge-default',
  defaultPageSize: 20,
  containerMode: 'card',
  showSortSelect: true,
  showDataStatus: false,
  showHighQualityFilter: false,
  enableHighQualityActions: false,
  showTopicsInList: true,
  detailSource: 'remote',
  showTopicsInDetail: true,
  showBrandSeriesInDetail: true,
  showRelationEventsInDetail: true,
  enableVoiceManagementParams: false,
  enableErrorCorrection: false,
  showBatchAction: false,
  enableEventIssueAction: false,
  eventIssueDataSourceType: 'RESULT',
  eventIssueTopicOptions: () => [],
  showExportAction: false,
  showEventExportAction: false,
  exportActionLoading: false,
  exportLoading: false,
  showKeywordSearch: true,
  showCorpusCreateAction: true,
  showSentimentFilter: false,
  showEmotionalLevelFilter: false,
  sceneTipText: '',
  userDetailMode: 'close',
  sentimentQueryField: 'sentiment'
})

const emit = defineEmits<{
  export: [queryParams: VocQueryParams]
  'export-data': [queryParams: VocQueryParams]
  'event-issue': [payload: EventIssuePayload]
}>()

const instance = getCurrentInstance()
const hasWatchStoreQueryProp = computed(() => {
  const vnodeProps = instance?.vnode.props || {}
  return 'watchStoreQuery' in vnodeProps || 'watch-store-query' in vnodeProps
})

const hasEventIssueListener = computed(() => {
  const vnodeProps = instance?.vnode.props || {}
  return 'onEventIssue' in vnodeProps || 'onEvent-issue' in vnodeProps
})

/**
 * 计算是否需要监听全局 queryStore。
 * `watchStoreQuery` 是布尔 prop，Vue 在未显式传值时会默认注入 `false`，
 * 因此不能直接用 `props.watchStoreQuery ?? !props.queryParams` 判断，否则会把默认场景误判为不监听。
 */
const shouldWatchStoreQuery = computed(() => {
  if (hasWatchStoreQueryProp.value) return props.watchStoreQuery
  return !props.queryParams
})

/**
 * 归一化可选值，统一将空字符串/null 转为 undefined，便于接口入参与筛选状态判断。
 *
 * @param value 原始值
 * @returns 归一化后的筛选值
 */
const normalizeOptionalValue = <T = unknown,>(value: T): T | undefined => {
  if (value === '' || value === null || value === undefined) return undefined
  return value
}

// 类型声明（与原实现保持一致）
interface VoiceTag {
  topic: string
  sentiment: string
  intention: string
}

interface VoiceItem {
  id: string | number
  ids?: string | Array<string | number>
  title: string
  avatar?: string
  custName: string
  brand: string
  dataCreateTime: string
  originalId: string
  originalTexTScene: string
  channel: string
  topics?: VoiceTag[]
  role?: any
  highQuality?: boolean
  dataId?: string
  auditStatus?: number
  mainRespOrgId?: string
}

type EventIssueMode = 'single' | 'batch'

interface EventIssuePayload {
  mode: EventIssueMode
  selection: VoiceItem[]
}

const defItem: VoiceItem & Record<string, any> = {
  id: '',
  ids: '',
  title: '',
  dataId: '',
  channelCode: '',
  originalId: '',
  originalTexTScene: '',
  channel: '',
  brandCode: '',
  brand: '',
  carSeriesCode: '',
  carSeries: '',
  sentiment: '',
  intention: '',
  dataCreateTime: '',
  oneId: '',
  custName: '',
  topics: []
}

// 基础状态
const loading = ref(false)
const listData = ref<VoiceItem[]>([])
const lastItem = ref<VoiceItem>(defItem)
const curItem = ref<VoiceItem>(defItem)

const queryStore = useQueryStore()
const userStore = useUserStore()
const storePms = queryStore.currentQueryParams
const pagination = reactive({
  pageNum: 1,
  pageSize: props.defaultPageSize
})
const total = ref(0)
const EXPORT_ACTION_LOCK_MS = 1000
const exportActionLocked = ref(false)
let exportActionUnlockTimer: ReturnType<typeof setTimeout> | undefined

// 排序
const selectType = ref<any>('normal')
const selectOpts = ref([
  { itemValue: 'normal', itemText: '综合排序' },
  { itemValue: 'asc', itemText: '时间正序' },
  { itemValue: 'desc', itemText: '时间倒序' }
])

const allSentimentValue = '__ALL__'
const selectedSentiments = ref<string[]>([allSentimentValue])
const sentimentOptions = computed(() => [
  { text: '全部情感', value: allSentimentValue },
  ...(userStore.getDictItems('voc_sentiment') || [])
])
const emotionalLevel = ref<string | number | undefined>(undefined)
const emotionalLevelOptions = computed(() => userStore.getDictItems('emotional_level') || [])

/**
 * 判断当前情感筛选是否包含字典展示文本为“负面”的选项。
 * 情感程度仅用于负面情感数据，字典 value 不固定时按展示文本兼容判定。
 */
const isEmotionalLevelEnabled = computed(() => {
  if (!props.showEmotionalLevelFilter) return false

  const selectedValues = new Set(
    selectedSentiments.value.filter(value => value && value !== allSentimentValue).map(String)
  )

  return sentimentOptions.value.some(
    option => option.text === '负面' && selectedValues.has(String(option.value))
  )
})

// 取消负面情感后立即移除已选程度，避免界面和实际查询条件不一致。
watch(isEmotionalLevelEnabled, enabled => {
  if (!enabled) {
    emotionalLevel.value = undefined
  }
})

//数据筛选 全部数据、已打标数据、未打标数据、过滤数据
const dataStatus = ref<any[]>([])
const dataStatusOptions = computed(() => {
  const options = userStore.getDictItems('data_type') || []
  return options
})

// 搜索
const searchVal = ref<string>('')
const highQualityFilterValue = ref<any>(undefined)

/**
 * 将情感多选值转换为查询参数。
 * @returns 全部情感返回空字符串，具体情感返回多选数组
 */
const getSentimentQueryValue = () => {
  const values = Array.isArray(selectedSentiments.value) ? selectedSentiments.value : []
  const concreteValues = values.filter(value => value && value !== allSentimentValue)
  return concreteValues.length > 0 ? concreteValues : ''
}

/**
 * 应用情感筛选变更，维护“全部情感”和具体情感的互斥关系，并刷新列表。
 * @param value 当前下拉选中值
 */
const applySentimentFilterChange = (value: string[] = []) => {
  const values = Array.isArray(value) ? value : []
  const concreteValues = values.filter(item => item && item !== allSentimentValue)
  const hasAll = values.includes(allSentimentValue)
  const lastValue = values[values.length - 1]

  if (values.length === 0 || (hasAll && lastValue === allSentimentValue)) {
    selectedSentiments.value = [allSentimentValue]
  } else if (concreteValues.length > 0) {
    selectedSentiments.value = Array.from(new Set(concreteValues))
  } else {
    selectedSentiments.value = [allSentimentValue]
  }

  resetPageAndFetch()
}

/**
 * 情感切换可能在多选场景连续触发，节流后只按最后一次选择刷新列表。
 */
const handleSentimentFilterChange = throttle(applySentimentFilterChange, 300, {
  leading: false,
  trailing: true
})

/**
 * 情感程度改变后刷新列表；该筛选仅在选中负面情感时有效。
 */
const handleEmotionalLevelFilterChange = () => {
  resetPageAndFetch()
}

/**
 * 同步高质量筛选值，并在启用 voiceManagementParams 时回写全局筛选态。
 *
 * @param value 高质量筛选值
 */
const syncHighQualityFilterValue = (value: unknown) => {
  const normalizedValue = normalizeOptionalValue(value)
  highQualityFilterValue.value = normalizedValue
  if (
    props.enableVoiceManagementParams &&
    queryStore.voiceManagementParams.highQuality !== normalizedValue
  ) {
    queryStore.voiceManagementParams.highQuality = normalizedValue
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  debouncedFetchData()
}

// 系统-客户原声特有：voiceManagementParams 组装
const _voiceManagementParams = ref<any>({})

// 数据拉取
const { showLoading, hideLoading } = useLoading()
const leaderGroupCode = 'groupCode'
const leaderGroupName = '智行汽车集团'

/**
 * 组装领导页品牌洞察模块的品牌筛选参数。
 * 前端状态里仍使用 groupCode 占位，真正请求时再转换为 automark。
 *
 * @returns 客户原声列表需要附带的品牌筛选参数
 */
const getLeaderInsightBrandParams = (tempCode?: string): Record<string, string | undefined> => {
  if (tempCode === leaderGroupCode) {
    return {
      automark: leaderGroupName,
      brandCode: undefined,
      tempCode: undefined
    }
  }

  return {
    brandCode: tempCode,
    automark: undefined,
    tempCode: undefined
  }
}

/**
 * 统一高质量标识字段，兼容布尔/数字/字符串三种后端返回形式。
 *
 * @param value 原始高质量标识
 * @returns 是否高质量
 */
const normalizeHighQualityFlag = (value: unknown): boolean => {
  if (value === true || value === 1 || value === '1') return true
  if (typeof value === 'string') {
    return value.toLowerCase() === 'true'
  }
  return false
}

/**
 * 统一观点标签数组，兼容字符串数组与对象数组两种格式。
 *
 * @param topics 原始观点列表
 * @returns 标准化后的观点标签
 */
const normalizeVoiceTopics = (topics: unknown): VoiceTag[] => {
  if (!Array.isArray(topics)) return []
  return topics
    .map((topic: unknown) => {
      if (typeof topic === 'string') {
        return {
          topic,
          sentiment: '',
          intention: ''
        }
      }
      if (topic && typeof topic === 'object') {
        return {
          topic: String((topic as Record<string, any>).topic ?? ''),
          sentiment: String((topic as Record<string, any>).sentiment ?? ''),
          intention: String((topic as Record<string, any>).intention ?? '')
        }
      }
      return null
    })
    .filter((topic): topic is VoiceTag => Boolean(topic?.topic))
}

/**
 * 统一列表项字段，兼容不同页面声音列表接口的字段命名差异。
 *
 * @param item 原始列表项
 * @returns 统一后的客户原声列表项
 */
const normalizeVoiceItem = (item: Record<string, any>): VoiceItem => {
  const safeItem = item || {}
  return {
    ...safeItem,
    id: safeItem.id ?? safeItem.newId ?? '',
    title: safeItem.title ?? '',
    custName: safeItem.custName ?? safeItem.username ?? safeItem.customerName ?? '',
    brand: safeItem.brand ?? safeItem.brandName ?? '',
    dataCreateTime: safeItem.dataCreateTime ?? safeItem.evaluateTime ?? '',
    originalId: safeItem.originalId ?? safeItem.dataId ?? '',
    originalTexTScene:
      safeItem.originalTexTScene ?? safeItem.originalTextScene ?? safeItem.content ?? '',
    channel: safeItem.channel ?? safeItem.channelName ?? '',
    topics: normalizeVoiceTopics(safeItem.topics),
    highQuality: normalizeHighQualityFlag(safeItem.highQuality),
    dataId: safeItem.dataId ?? safeItem.originalId ?? ''
  }
}

/**
 * 统一观点筛选值，兼容历史代码把整行对象写入 topic 的情况。
 *
 * @param value 原始观点筛选值
 * @returns 可直接给接口使用的观点名称
 */
const normalizeTopicQueryValue = (value: unknown): string | undefined => {
  if (typeof value === 'string') {
    const trimmedValue = value.trim()
    return trimmedValue || undefined
  }

  if (value && typeof value === 'object') {
    const topicObject = value as Record<string, any>
    const candidate = topicObject.opinionName ?? topicObject.topic ?? topicObject.label ?? ''
    const normalizedCandidate = String(candidate).trim()
    return normalizedCandidate || undefined
  }

  return undefined
}

/**
 * 严格外部参数模式仅服务少数独立接口场景。
 * 开启后列表请求不再混入全局默认筛选、搜索、排序等通用字段。
 */
const isExternalOnlyQueryMode = computed(() => {
  return props.queryParamsMode === 'external-only' && Boolean(props.queryParams)
})

/**
 * 按当前启用的列表私有筛选补充查询参数。
 * @param queryParams 已完成基础归一化的列表查询参数
 * @returns 最终查询参数
 */
const appendPanelFilterParams = (queryParams: VocQueryParams): VocQueryParams => {
  if (props.showSentimentFilter) {
    const sentimentQueryValue = getSentimentQueryValue()

    if (props.sentimentQueryField === 'sentimentList') {
      delete queryParams.sentiment
      if (sentimentQueryValue) {
        queryParams.sentimentList = sentimentQueryValue
      }
    } else {
      queryParams.sentiment = sentimentQueryValue
    }
  }

  if (props.showEmotionalLevelFilter) {
    delete queryParams.emotionalLevel
    if (isEmotionalLevelEnabled.value && emotionalLevel.value !== undefined) {
      queryParams.emotionalLevel = emotionalLevel.value
    }
  }

  return queryParams
}

const buildQuery = (): VocQueryParams => {
  const externalQueryParams = props.queryParams

  if (isExternalOnlyQueryMode.value && externalQueryParams) {
    return appendPanelFilterParams(
      getRealAttr({
        ...externalQueryParams,
        pageSize: pagination.pageSize,
        pageNum: pagination.pageNum,
        searchKeywords: searchVal.value,
        sortOrder: selectType.value === 'normal' ? '' : selectType.value,
        ...(highQualityFilterValue.value !== undefined
          ? { highQuality: highQualityFilterValue.value }
          : {})
      })
    )
  }

  const baseParams = externalQueryParams
    ? {
        ...queryStore.defaultQueryParams,
        ...externalQueryParams
      }
    : storePms

  return appendPanelFilterParams(
    getRealAttr({
      ...baseParams,
      ...(externalQueryParams
        ? {
            brandCode: baseParams.brandCode,
            tempCode: undefined
          }
        : getLeaderInsightBrandParams(storePms.tempCode)),
      topic: normalizeTopicQueryValue(baseParams.topic),
      pageSize: pagination.pageSize,
      pageNum: pagination.pageNum,
      searchKeywords: searchVal.value,
      dataStatus: dataStatus.value || [],
      sortOrder: selectType.value === 'normal' ? '' : selectType.value,
      ...(props.enableVoiceManagementParams ? _voiceManagementParams.value : {}),
      ...(highQualityFilterValue.value !== undefined
        ? { highQuality: highQualityFilterValue.value }
        : {}),
      checkPermission: true
    })
  )
}

const fetchData = async (refresh: boolean = true) => {
  const errMsg = '获取客户原声列表数据失败'
  try {
    if (refresh) {
      loading.value = true
      listData.value = []
    }
    const queryParams: VocQueryParams = buildQuery()
    const response = await postVocListSoundsByUrl(props.listApiUrl, queryParams)
    if (response.success && response.result) {
      listData.value = (response.result.list || []).map((item: VoiceItem) =>
        normalizeVoiceItem(item)
      )
      total.value = response.result.total
    } else {
      listData.value = []
      total.value = 0
      ElMessage.error(response.message || errMsg)
    }
  } catch (error) {
    console.error(`${errMsg}:, ${error}`)
    // ElMessage.error(`${errMsg}，请稍后重试`)
    listData.value = []
    total.value = 0
  } finally {
    if (refresh && listData.value[0]) tabItem(listData.value[0])
    loading.value = false
  }
}

const debouncedFetchData = debounce(fetchData, 300)

const resetPageAndFetch = () => {
  pagination.pageNum = 1
  debouncedFetchData()
}

/**
 * 高质量筛选变更后，统一重置分页并刷新列表。
 *
 * @param value 高质量筛选值
 */
const handleHighQualityFilterChange = (value: unknown) => {
  syncHighQualityFilterValue(value)
  resetPageAndFetch()
}

/**
 * 组装导出查询参数。导出默认按当前筛选条件导出全部数据，不携带分页参数。
 *
 * @returns 当前客户原声列表导出所需查询参数
 */
const buildExportQuery = (): VocQueryParams => {
  const queryParams = { ...buildQuery() } as VocQueryParams
  delete queryParams.pageNum
  delete queryParams.pageSize
  return queryParams
}

// 用户详情沿用当前列表筛选条件，但不需要携带列表分页参数。
const userDetailQueryParams = computed(() => buildExportQuery())

/**
 * 导出当前列表数据。
 * 入参复用列表当前实际查询参数，确保导出范围与页面查询结果保持一致。
 */
const handleExportData = () => {
  if (props.exportLoading) return
  emit('export-data', buildQuery())
}

/**
 * 抛出导出事件数据事件，并用短时锁拦截连续点击。
 * 事件导出默认按当前筛选条件导出全部数据，不携带分页参数。
 */
const handleEventExportAction = () => {
  if (exportActionLocked.value || props.exportActionLoading) return

  exportActionLocked.value = true
  emit('export', buildExportQuery())

  if (exportActionUnlockTimer) {
    clearTimeout(exportActionUnlockTimer)
  }
  exportActionUnlockTimer = setTimeout(() => {
    exportActionLocked.value = false
    exportActionUnlockTimer = undefined
  }, EXPORT_ACTION_LOCK_MS)
}

// 分页
const handleSizeChange = () => {
  pagination.pageNum = 1
  debouncedFetchData()
}
const handleCurrentChange = () => {
  debouncedFetchData()
}

onBeforeUnmount(() => {
  handleSentimentFilterChange.cancel()
  if (exportActionUnlockTimer) {
    clearTimeout(exportActionUnlockTimer)
  }
})

const tabItem = (item: VoiceItem) => {
  console.log('item--->tabItem', item)
  if (item.originalId === curItem.value.originalId) {
    return
  }
  if (curItem.value.originalId) {
    lastItem.value = cloneDeep(curItem.value)
  }
  curItem.value = item
}

onMounted(() => {
  if (props.enableVoiceManagementParams) {
    syncHighQualityFilterValue(queryStore.voiceManagementParams.highQuality)
  } else if (props.queryParams?.highQuality !== undefined) {
    syncHighQualityFilterValue(props.queryParams.highQuality)
  }
  debouncedFetchData()
})

// 外部查询参数变化（系统-自助服务等场景）
watch(
  () => props.queryParams,
  newQueryParams => {
    if (!props.queryParams) return
    if (newQueryParams?.highQuality !== undefined) {
      syncHighQualityFilterValue(newQueryParams.highQuality)
    }
    pagination.pageNum = 1
    debouncedFetchData()
  },
  { deep: true }
)

watch(
  () => props.defaultPageSize,
  pageSize => {
    if (!pageSize || pageSize === pagination.pageSize) return
    pagination.pageSize = pageSize
    pagination.pageNum = 1
    debouncedFetchData()
  }
)

if (shouldWatchStoreQuery.value) {
  // 日期变化
  watch(
    () => ({ startDate: storePms.startDate, endDate: storePms.endDate }),
    () => {
      pagination.pageNum = 1
      debouncedFetchData()
    }
  )

  // 品牌、观点、意图切换（领导页品牌洞察统一使用 tempCode，请求时再转换）
  watch(
    () => [
      storePms.tempCode,
      storePms.channelCatagory,
      storePms.tag2Code,
      storePms.topic,
      storePms.intention
    ],
    () => {
      pagination.pageNum = 1
      debouncedFetchData()
    }
  )

  watch(
    () => storePms.filterItems,
    () => {
      pagination.pageNum = 1
      debouncedFetchData()
    },
    { deep: true }
  )
}

// 系统-客户原声：监听 voiceManagementParams 并转换
if (props.enableVoiceManagementParams) {
  watch(
    () => queryStore.voiceManagementParams,
    () => {
      const tagCodes = queryStore.voiceManagementParams.tagCodes || []
      const brandCarCodes = queryStore.voiceManagementParams.brandCarCodes || []
      _voiceManagementParams.value = {
        ...(brandCarCodes[0] ? { brandCode: brandCarCodes[0] } : {}),
        ...(brandCarCodes[1] ? { carSeriesCode: brandCarCodes[1] } : {}),
        tag1Code: tagCodes[0] || undefined,
        tag2Code: tagCodes[1] || undefined,
        viewpoint: queryStore.voiceManagementParams.viewpoint || undefined
      }
      syncHighQualityFilterValue(queryStore.voiceManagementParams.highQuality)
      pagination.pageNum = 1
      debouncedFetchData()
    },
    { deep: true }
  )
}

// 高质量标记（仅在 props.enableHighQualityActions 且具有权限时显示）
const handleAddHigh = debounce(async (item: VoiceItem) => {
  if (!props.enableHighQualityActions) return
  showLoading()
  try {
    const res = await highQuality([item.originalId as string])
    if (res.success) {
      // ElMessage.success('标记成功')
      ElMessage.success('高质量标记申请已提交，预计下一轮数据更新时生效，请勿重复操作。')
      fetchData(false)
    } else {
      ElMessage.error(res.message)
    }
  } catch (error: any) {
    ElMessage.error(error.message)
  } finally {
    hideLoading()
  }
}, 200)

const handleDelHigh = debounce(async (item: VoiceItem) => {
  if (!props.enableHighQualityActions) return
  showLoading()
  try {
    const res = await highQualityDel([item.originalId as string])
    if (res.success) {
      ElMessage.success('取消标记成功，预计下一轮数据更新时生效，请勿重复操作。')
      fetchData(false)
    } else {
      ElMessage.error(res.message)
    }
  } catch (error: any) {
    ElMessage.error(error.message)
  } finally {
    hideLoading()
  }
}, 200)

// 计算是否显示高质量操作按钮
const canShowHighQualityAction = computed(() => {
  return (
    props.enableHighQualityActions &&
    userStore.checkfunctionPermission(FunctionPermission.HIGH_QUALITY_SOUND)
  )
})

/**
 * 判断 PC 端数据纠错入口是否可见，避免仅打开批量选择时误放出纠错能力。
 */
const canShowErrorCorrectionAction = computed(() => {
  return (
    props.enableErrorCorrection &&
    userStore.checkfunctionPermission(FunctionPermission.DATA_CORRENCTION)
  )
})

/**
 * 判断 PC 端事件下发入口是否可见，调用方开关与操作权限需同时满足。
 */
const canShowEventIssueAction = computed(() => {
  return (
    props.enableEventIssueAction &&
    userStore.checkfunctionPermission(FunctionPermission.PC_EVENT_ISSUANCE)
  )
})

/**
 * 统一判断批量操作区是否需要展示。
 * 事件下发、数据纠错、高质量标记任一能力可用时，才展示批量入口。
 */
const canShowBatchAction = computed(() => {
  return (
    props.showBatchAction &&
    (canShowEventIssueAction.value ||
      canShowErrorCorrectionAction.value ||
      canShowHighQualityAction.value)
  )
})

/**
 * 统一判断列表卡片右侧操作区是否需要展示，避免多个操作按钮各自撑开布局。
 */
const canShowInlineActions = computed(() => {
  return (
    props.enableHighQualityInfo ||
    canShowErrorCorrectionAction.value ||
    canShowEventIssueAction.value
  )
})

/**
 * 获取数据纠错按钮文案。
 *
 * @param auditStatus 纠错审核状态
 * @returns 数据纠错入口展示文案
 */
const getErrorCorrectionText = (auditStatus?: number) => {
  if (auditStatus === 1) return '纠错审核中'
  if (auditStatus === 2) return '已纠错'

  return '数据纠错'
}

const handleCorrectionSuccess = () => {
  if (isBatchMode.value) {
    isBatchMode.value = false
    selectedItems.value = []
  }
  fetchData(false)
}

// 数据纠错逻辑
const {
  visible: errorCorrectionVisible,
  dataIdList: correctionDataIdList,
  open: openErrorCorrection,
  normalizeDataIdList
} = useErrorCorrectionDialog()

const handleOpenCorrection = (item: VoiceItem) => {
  openErrorCorrection([item.originalId])
}

// 事件下发逻辑
const eventIssueVisible = ref(false)
const eventIssueMode = ref<EventIssueMode>('single')
const eventIssueSelection = ref<VoiceItem[]>([])

/**
 * 解析后端返回的数组 ID 字段，兼容 JSON 字符串、数组和逗号分隔字符串。
 *
 * @param raw 原始 ID 字段
 * @returns 去重后的 ID 列表
 */
const normalizeJsonIdList = (raw: unknown): string[] => {
  const result: string[] = []

  const pushValue = (value: unknown) => {
    if (Array.isArray(value)) {
      value.forEach(pushValue)
      return
    }

    const id = String(value ?? '').trim()
    if (id) result.push(id)
  }

  if (Array.isArray(raw)) {
    pushValue(raw)
  } else if (typeof raw === 'string') {
    const value = raw.trim()
    if (!value) return []

    const tryParseJsonArray = (jsonValue: string) => {
      try {
        const parsed = JSON.parse(jsonValue)
        if (Array.isArray(parsed)) {
          pushValue(parsed)
          return true
        }
      } catch {
        return false
      }

      return false
    }

    if (!tryParseJsonArray(value) && !tryParseJsonArray(value.replace(/,\s*]/g, ']'))) {
      value
        .replace(/^\[/, '')
        .replace(/\]$/, '')
        .split(',')
        .map(id => id.trim().replace(/^['"]|['"]$/g, '').trim())
        .forEach(pushValue)
    }
  } else {
    pushValue(raw)
  }

  return [...new Set(result)]
}

/**
 * 根据事件下发数据源类型提取可提交 ID 列表。
 * 结果数据使用列表 ids；原始数据优先使用 id，缺失时回退 dataId。
 *
 * @param item 当前原声列表项
 * @returns 可用于事件下发的 ID 列表
 */
const getEventIssueIds = (item: VoiceItem) => {
  if (props.eventIssueDataSourceType !== 'ORIGINAL') {
    return normalizeJsonIdList(item.ids)
  }

  const id = normalizeDataIdList([item.id])[0]

  return normalizeDataIdList([id || item.dataId])
}

/**
 * 判断选中项中是否存在缺少下发 ID 的记录。
 *
 * @param items 当前选中的原声列表项
 * @returns 是否存在不可下发记录
 */
const hasMissingEventIssueIds = (items: VoiceItem[]) => {
  return items.some(item => getEventIssueIds(item).length === 0)
}

/**
 * 打开单条事件下发弹窗。
 * 有外部监听时交给父组件处理；否则按数据源类型打开内置事件下发弹窗。
 * 内置下发按数据源类型提取 ID，结果数据从 ids 解析，原始数据缺失 id 时回退 dataId。
 *
 * @param item 当前点击的原声列表项
 */
const handleOpenEventIssue = (item: VoiceItem) => {
  if (hasEventIssueListener.value) {
    const selectedPrimaryIdList = normalizeDataIdList([item.id])
    const selectedOriginalIdList = normalizeDataIdList([item.originalId])
    if (!selectedPrimaryIdList.length || !selectedOriginalIdList.length) {
      ElMessage.warning('当前原声缺少主键或原始数据 id，无法执行事件下发')
      return
    }

    emit('event-issue', {
      mode: 'single',
      selection: [item]
    })
    return
  }

  if (getEventIssueIds(item).length === 0) {
    ElMessage.warning('当前原声缺少下发 ID，无法执行事件下发')
    return
  }

  eventIssueMode.value = 'single'
  eventIssueSelection.value = [item]
  eventIssueVisible.value = true
}

// 批量操作
const isBatchMode = ref(false)
const batchMarkVisible = ref(false)
const selectedItems = ref<VoiceItem[]>([])
const selectedDataIdList = computed(() =>
  normalizeDataIdList(selectedItems.value.map(v => v.originalId))
)
const selectedEventIssuePrimaryIdList = computed(() =>
  normalizeDataIdList(selectedItems.value.map(v => v.id))
)
const selectedEventIssueOriginalIdList = computed(() =>
  normalizeDataIdList(selectedItems.value.map(v => v.originalId))
)
const currentPageDataIdList = computed(() =>
  normalizeDataIdList(listData.value.map(v => v.originalId))
)
const isCurrentPageAllSelected = computed(() => {
  const currentIds = currentPageDataIdList.value
  if (!currentIds.length) return false
  const selectedIdSet = new Set(selectedDataIdList.value)
  return currentIds.every(id => selectedIdSet.has(id))
})

const handleBatchAction = () => {
  isBatchMode.value = true
  selectedItems.value = []
}

const handleCancelBatch = () => {
  isBatchMode.value = false
  selectedItems.value = []
}

const handleSelectCurrentPage = () => {
  if (!isBatchMode.value) return
  if (!listData.value.length) return

  const currentIdSet = new Set(currentPageDataIdList.value)
  if (isCurrentPageAllSelected.value) {
    selectedItems.value = selectedItems.value.filter(item => {
      const normalizedId = normalizeDataIdList([item.originalId])[0]
      return normalizedId ? !currentIdSet.has(normalizedId) : true
    })
    return
  }

  const selectedIdSet = new Set(selectedDataIdList.value)
  const nextSelectedItems = [...selectedItems.value]
  for (const item of listData.value) {
    const normalizedId = normalizeDataIdList([item.originalId])[0]
    if (!normalizedId) continue
    if (selectedIdSet.has(normalizedId)) continue
    selectedIdSet.add(normalizedId)
    nextSelectedItems.push(item)
  }
  selectedItems.value = nextSelectedItems
}

const handleBatchMark = () => {
  if (!selectedItems.value.length) return
  if (selectedDataIdList.value.length !== selectedItems.value.length) {
    ElMessage.warning('选中的数据存在缺少 originalId 的记录，无法执行该操作')
    return
  }
  batchMarkVisible.value = true
}

/**
 * 打开批量事件下发弹窗。
 * 有外部监听时校验视图主键和原始数据 id 后外抛；否则按数据源类型提取 ID 打开内置弹窗。
 * 内置下发会从结果数据 ids 聚合去重，原始数据缺失 id 时回退 dataId。
 */
const handleBatchIssue = () => {
  if (!selectedItems.value.length) return

  if (hasEventIssueListener.value) {
    if (
      selectedEventIssuePrimaryIdList.value.length !== selectedItems.value.length ||
      selectedEventIssueOriginalIdList.value.length !== selectedItems.value.length
    ) {
      ElMessage.warning('选中的数据存在缺少主键或原始数据 id 的记录，无法执行该操作')
      return
    }

    emit('event-issue', {
      mode: 'batch',
      selection: [...selectedItems.value]
    })
    return
  }

  if (hasMissingEventIssueIds(selectedItems.value)) {
    ElMessage.warning('选中的数据存在缺少下发 ID 的记录，无法执行该操作')
    return
  }

  eventIssueMode.value = 'batch'
  eventIssueSelection.value = [...selectedItems.value]
  eventIssueVisible.value = true
}

const handleBatchSuccess = () => {
  isBatchMode.value = false
  selectedItems.value = []
  pagination.pageNum = 1
  debouncedFetchData()
}

/**
 * 对外清理批量选择态。
 * 事件下发弹窗提交成功后同步退出批量模式，避免旧勾选残留。
 */
const clearBatchSelection = () => {
  isBatchMode.value = false
  selectedItems.value = []
}

const handleEventIssueSuccess = () => {
  if (eventIssueMode.value === 'batch') {
    clearBatchSelection()
  }
}

const handleBatchCorrection = () => {
  if (!selectedItems.value.length) return
  if (selectedDataIdList.value.length !== selectedItems.value.length) {
    ElMessage.warning('选中的数据存在缺少 originalId 的记录，无法执行该操作')
    return
  }
  openErrorCorrection(selectedDataIdList.value)
}

// 列表点击代理
const handleItemClick = (item: VoiceItem) => {
  if (isBatchMode.value) {
    const idx = selectedItems.value.findIndex(v => v.originalId === item.originalId)
    if (idx > -1) {
      selectedItems.value.splice(idx, 1)
    } else {
      selectedItems.value.push(item)
    }
  } else {
    tabItem(item)
  }
}

// TheDetails 组件的引用
const theDetailsRef = ref<InstanceType<typeof TheDetails> | null>(null)

// 暴露浏览记录上报方法给父组件
const submitBrowseRecord = async () => {
  if (theDetailsRef.value) {
    await theDetailsRef.value.submitBrowseRecord()
  }
}

/**
 * 对外暴露刷新方法，便于外层在特殊场景下主动触发重查。
 */
const refresh = async () => {
  pagination.pageNum = 1
  await fetchData()
}

// 暴露方法给父组件
defineExpose({
  submitBrowseRecord,
  refresh,
  buildQuery,
  clearBatchSelection
})
</script>

<template>
  <div
    class="voice-list"
    :class="{
      'f-card p-24': props.containerMode === 'card',
      'voice-list--embedded': props.containerMode === 'embedded'
    }"
  >
    <!-- 标题与筛选 -->
    <CommonTitle :title="props.title" class="mb-16">
      <template #left>
        <!-- 权限说明固定紧跟标题，确保始终位于排序筛选前方。 -->
        <el-tooltip
          content="遵从各品牌主体数据安全管理规范约束，品牌原声私域数据需对应权限方可查阅。"
          placement="top"
          popper-class="text-tooltip-light common-tooltip"
        >
          <el-icon class="tip voice-list__permission-tip-icon"><InfoFilled /></el-icon>
        </el-tooltip>

        <!-- 排序选择 -->
        <template v-if="props.showSortSelect">
          <span class="select-wrap">
            <FSelect
              v-model="selectType"
              :options="selectOpts"
              :clearable="false"
              :filterable="false"
              style="width: 108px"
              @change="resetPageAndFetch"
            />
            <SvgIcon
              name="arrow-up-down-fill"
              width="17px"
              height="20px"
              color="#929AA6"
              class="svg"
            />
          </span>
        </template>

        <!-- 情感筛选（情感分支数据需要） -->
        <template v-if="props.showSentimentFilter">
          <FSelect
            v-model="selectedSentiments"
            class="w-150 ml-16"
            :clearable="true"
            :multiple="true"
            :collapse-tags="true"
            :collapse-tags-tooltip="true"
            :options="sentimentOptions"
            placeholder="全部情感"
            :fields="{ label: 'text', value: 'value' }"
            @change="handleSentimentFilterChange"
          />
        </template>

        <!-- 情感程度仅在选择负面情感时可用（情感分支数据需要） -->
        <template v-if="props.showEmotionalLevelFilter">
          <FSelect
            v-model="emotionalLevel"
            class="w-150 ml-16"
            :clearable="true"
            :disabled="!isEmotionalLevelEnabled"
            :options="emotionalLevelOptions"
            placeholder="情感程度"
            :fields="{ label: 'text', value: 'value' }"
            @change="handleEmotionalLevelFilterChange"
          />
        </template>

        <!-- 是否高质量筛选（系统-客户原声需要） -->
        <template v-if="props.showHighQualityFilter">
          <FSelect
            v-model="highQualityFilterValue"
            class="w-150 ml-16"
            :clearable="true"
            :options="userStore.getDictItems('high_quality_tag') || []"
            placeholder="是否高质量声音"
            :fields="{ label: 'text', value: 'value' }"
            @change="handleHighQualityFilterChange"
          />
        </template>

        <!-- 是否数据筛选（系统-原生查询-原始数据需要） -->
        <template v-if="props.showDataStatus">
          <FSelect
            v-model="dataStatus"
            class="w-150 ml-16"
            :clearable="true"
            :multiple="true"
            :collapse-tags="true"
            :collapse-tags-tooltip="true"
            :options="dataStatusOptions"
            placeholder="全部数据"
            :fields="{ label: 'text', value: 'value' }"
            @change="resetPageAndFetch"
          />
        </template>

        <!-- 导出数据 -->
        <el-button
          v-if="
            props.showExportAction &&
            userStore.checkfunctionPermission(FunctionPermission.ORIGINAL_SOUND_DOWNLOAD)
          "
          type="primary"
          class="ml-24"
          :loading="props.exportLoading"
          :disabled="props.exportLoading"
          @click="handleExportData"
        >
          <SvgIcon name="download-01" width="16px" height="16px" color="#fff" class="mr-6" />
          导出数据
        </el-button>

        <!-- 导出事件数据 -->
        <el-button
          v-if="
            props.showEventExportAction &&
            userStore.checkfunctionPermission(FunctionPermission.EVENT_ORIGINAL_SOUND_DOWNLOAD)
          "
          type="primary"
          class="ml-16"
          :loading="props.exportActionLoading || exportActionLocked"
          :disabled="props.exportActionLoading || exportActionLocked"
          @click="handleEventExportAction"
        >
          <SvgIcon name="download-01" width="16px" height="16px" color="#fff" class="mr-6" />
          导出事件数据
        </el-button>

        <!-- 批量操作 -->
        <div v-if="canShowBatchAction" class="batch-action-container ml-16">
          <template v-if="!isBatchMode">
            <el-button type="primary" @click="handleBatchAction">批量操作</el-button>
          </template>
          <template v-else>
            <el-button @click="handleCancelBatch">取消操作</el-button>
            <el-button @click="handleSelectCurrentPage">{{
              isCurrentPageAllSelected ? '取消全选' : '本页全选'
            }}</el-button>
            <el-button
              v-if="canShowEventIssueAction"
              type="primary"
              :disabled="!selectedItems.length"
              @click="handleBatchIssue"
              >批量下发</el-button
            >
            <el-button
              v-if="canShowErrorCorrectionAction"
              type="primary"
              :disabled="!selectedItems.length"
              @click="handleBatchCorrection"
              >批量纠错</el-button
            >
            <el-button
              v-if="canShowHighQualityAction"
              type="primary"
              :disabled="!selectedItems.length"
              @click="handleBatchMark"
              >批量标记</el-button
            >
          </template>
        </div>
      </template>
      <template #right>
        <span v-if="props.showKeywordSearch" style="position: relative">
          <el-input
            style="width: 172px"
            v-model="searchVal"
            :maxlength="50"
            clearable
            placeholder="请输入关键词搜索"
            :suffix-icon="Search"
            @keyup.enter="handleSearch"
            @change="handleSearch"
          />
          <!-- <img :src="searchPng" class="img2" /> -->
        </span>
      </template>
    </CommonTitle>
    <div v-if="props.sceneTipText" class="voice-list__scene-tip">
      <el-icon><InfoFilled /></el-icon>
      <span class="ml-8">{{ props.sceneTipText }}</span>
    </div>

    <!-- 主体 -->
    <div class="voice-list__container flex-1" v-loading="loading">
      <!-- 空态 -->
      <div v-if="listData.length === 0" class="voice-list__empty">
        <el-empty description="暂无声音数据" />
      </div>

      <!-- 列表 + 详情 -->
      <div v-else class="flex theWrap h-full">
        <!-- 左侧 列表 -->
        <div class="flex-1 theList">
          <div class="voice-list__items">
            <div
              v-for="(item, index) in listData"
              :key="`${item.originalId}${index}`"
              @click="handleItemClick(item)"
              class="p-16 voice-list__item flex"
              :class="{ 'voice-list__item-active': item.originalId === curItem.originalId }"
            >
              <div v-if="isBatchMode" class="mr-8 flex-y-center" @click.stop>
                <el-checkbox
                  :model-value="selectedDataIdList.includes(item.originalId)"
                  @change="handleItemClick(item)"
                />
              </div>
              <div class="flex-1 overflow-hidden">
                <!-- 标题 -->
                <div
                  v-if="item.title && item.title !== '-'"
                  class="fs-16 fw-600 text-primary single-line-ellipsis"
                >
                  {{ item.title }}
                </div>

                <!-- 内容片段 -->
                <div
                  class="fs-14 fw-400 text-secondary mt-8 theDesc"
                  v-if="item.originalTexTScene"
                  v-html="item.originalTexTScene"
                ></div>

                <!-- 标签/观点 -->
                <div
                  class="voice-list__tags flex flex-wrap mt-12"
                  v-if="props.showTopicsInList && item.topics && item.topics.length"
                >
                  <template v-for="(text, idx) in item.topics" :key="idx">
                    <span
                      v-if="text.topic"
                      class="theTag fw-400 fs-12 text-link"
                      :style="{
                        'background-color': `${toRgba(sentimentColors[text?.sentiment], 0.1)}`,
                        color: `${sentimentColors[text?.sentiment]}`
                      }"
                      >{{ text.topic }}</span
                    >
                  </template>
                </div>

                <!-- 作者、渠道、时间 & 高质量操作 -->
                <div class="flex-between">
                  <div class="fs-14 fw-400 mt-12">
                    {{ item.custName || '-' }}<span class="ml-10 mr-10 split">|</span
                    >{{ item.channel || '-' }}<span class="ml-10 mr-10 split">|</span
                    >{{ item.dataCreateTime }}
                  </div>
                  <div v-if="canShowInlineActions" class="flex-y-center voice-list__item-actions">
                    <el-button
                      v-if="canShowEventIssueAction"
                      type="primary"
                      class="mr-12"
                      @click.stop="handleOpenEventIssue(item)"
                    >
                      事件下发
                    </el-button>

                    <!-- 数据纠错 -->
                    <el-button
                      v-if="canShowErrorCorrectionAction"
                      type="primary"
                      class="mr-12"
                      @click.stop="handleOpenCorrection(item)"
                    >
                      {{ getErrorCorrectionText(item.auditStatus) }}
                    </el-button>

                    <template v-if="canShowHighQualityAction">
                      <el-button
                        v-if="item.highQuality"
                        type="primary"
                        @click.stop="handleDelHigh(item)"
                        >高质量声音</el-button
                      >
                      <el-button v-else @click.stop="handleAddHigh(item)">标记为高质量</el-button>
                    </template>
                    <template v-else>
                      <el-button v-if="item.highQuality" type="primary">高质量声音</el-button>
                    </template>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 分页 -->
          <div class="flex-y-center pagination-class mt-10">
            <el-pagination
              size="small"
              background
              v-model:current-page="pagination.pageNum"
              v-model:page-size="pagination.pageSize"
              :total="total"
              :page-sizes="[10, 20, 50, 100, 200, 500, 1000]"
              layout="->,total, prev, pager, next, sizes"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </div>

        <!-- 右侧 详情 -->
        <TheDetails
          ref="theDetailsRef"
          :curItem="curItem"
          :lastItem="lastItem"
          :enableErrorCorrection="props.enableErrorCorrection"
          :show-corpus-create-action="props.showCorpusCreateAction"
          :detail-source="props.detailSource"
          :detail-api-url="props.detailApiUrl"
          :show-topics="props.showTopicsInDetail"
          :show-brand-series="props.showBrandSeriesInDetail"
          :show-relation-events="props.showRelationEventsInDetail"
          :query-params="userDetailQueryParams"
          :user-detail-mode="props.userDetailMode"
          @correction-success="handleCorrectionSuccess"
        />
      </div>
    </div>

    <ErrorCorrectionDialog
      v-if="props.enableErrorCorrection"
      v-model:visible="errorCorrectionVisible"
      :data-id-list="correctionDataIdList"
      :filter="props.queryParams"
      @success="handleCorrectionSuccess"
    />

    <BatchMarkDialog
      v-if="batchMarkVisible"
      v-model:visible="batchMarkVisible"
      :ids="selectedDataIdList"
      @success="handleBatchSuccess"
    />

    <EventIssueDialog
      v-if="eventIssueVisible"
      v-model:visible="eventIssueVisible"
      :mode="eventIssueMode"
      :selection="eventIssueSelection"
      :data-source-type="props.eventIssueDataSourceType"
      :topic-options="props.eventIssueTopicOptions"
      @success="handleEventIssueSuccess"
    />
  </div>
</template>

<style scoped lang="scss">

// :deep(.common-title__custom-left) {
//     margin-left: 10px !important;
// }
.f-card {
  box-shadow: 0px 1px 2px 0px rgba(10, 13, 18, 0.05);
  border-radius: 12px;
}

.voice-list--embedded {
  height: 100%;
  padding: 0;
  background: transparent;
  box-shadow: none;
  border-radius: 0;
}

// 容器
.voice-list {
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;

  .img2 {
    position: absolute;
    right: 10px;
    top: 50%;
    transform: translateY(-50%);
    width: 16px;
    height: 16px;
  }

  .select-wrap {
    position: relative;
    :deep(.el-icon) {
      display: none;
    }
    .svg {
      position: absolute;
      right: 9px;
      top: 2px;
      z-index: 0;
    }
  }

  .voice-list__permission-tip-icon {
    margin-right: 16px;
    color: #1677ff;
    font-size: 16px;
    cursor: pointer;
    vertical-align: middle;
  }

  .voice-list__scene-tip {
    color: #1677ff;
    line-height: 24px;
    font-size: 14px;
    font-weight: 500;
    margin-bottom: 16px;
    padding: 8px 16px;
    display: flex;
    align-items: center;
    background: #e6f4ff;
    border: 1px solid #bae0ff;
    border-radius: 4px;
  }

  .batch-action-container {
    display: inline-flex;
    align-items: center;
    vertical-align: middle;
  }

  .voice-list__container {
    flex: 1;
    min-height: 0;
  }

  .voice-list__empty {
    display: flex;
    height: 100%;
    min-height: 240px;
    justify-content: center;
    align-items: center;
    padding: 24px 0;
    text-align: center;
    color: rgba(0, 0, 0, 0.45);
  }

  .theList,
  .theDetail {
    width: 50%;
    height: 100%;
    min-height: 0;
    min-width: 0;
    color: #5f6a7a;
  }
}

.theWrap {
  gap: 24px;
  min-height: 0;
  overflow: hidden;
}

// 左侧列表
.theList {
  border-right: 1px solid #e9eaeb;
  display: flex;
  flex-direction: column;
}

.voice-list__items {
  flex: 1;
  min-height: 0;
  overflow: auto;
  display: flex;
  flex-direction: column;
}

.voice-list__item {
  border-bottom: 1px solid #e9eaeb;
  cursor: pointer;

  &.voice-list__item-active {
    background: #f5f7fa;
    border-radius: 0;
  }

  .text-primary {
    line-height: 22px;
    color: #4b5468;
    font-size: 16px;
    font-weight: bold;
    font-family: 'yahei';
    margin-bottom: 6px;
  }

  .voice-list__right {
    width: 172px;
  }

  .divider {
    height: 80px;
    width: 1px;
    border-radius: 0;
    border-left: 1px solid #e9eaeb;
  }

  .theDesc {
    margin-top: 0 !important;
    color: #4b5468;
    line-height: 22px;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2; /* 只显示2行 */
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .theDesc :deep(p) {
    display: inline;
    margin: 0;
  }
  .theDesc :deep(br) {
    display: inline;
  }
  .theDesc :deep(div) {
    display: inline;
  }
}

.voice-list__tags {
  gap: 8px;
  .theTag {
    background: #e2f3fe;
    border-radius: 4px;
    padding: 6px 12px;
    font-weight: 400;
  }
}

.voice-list__item-actions {
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 12px;
}

.voice-list__item-actions .el-button {
  margin-left: 0;
}

.fw-600 {
  font-weight: 600;
}

.split {
  color: #929aa6;
  display: inline-block;
  height: 13px;
  overflow: hidden;
  vertical-align: top;
}

.pagination-class {
  flex-shrink: 0;
  padding-right: 24px;
  justify-content: flex-end;
}
</style>
