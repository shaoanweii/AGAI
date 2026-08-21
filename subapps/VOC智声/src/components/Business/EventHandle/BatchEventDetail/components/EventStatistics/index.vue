<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import ReportSummary from '@/components/Business/Scene/Common/ReportSummary.vue'
import {
  getBatchEventCarSeriesStat,
  getBatchEventChannelStat,
  getBatchEventDataStat,
  getBatchEventOpinionStat,
  getBatchEventProvinceStat,
  getBatchEventReportSummaryStream,
  getBatchEventSceneStat,
  getBatchEventTrendStat
} from '@/api/batchEvent'
import type {
  BatchEventBriefDetailVo,
  BatchEventDataStatVo,
  BatchEventTrendStatVo
} from '@/api/batchEvent/types'
import type { BatchRiskEventPageVo } from '@/api/batchEvent/types'
import EventPriorityTag from '@/views/customerDirectEngage/singlePointEvent/components/EventPriorityTag.vue'
import type { WordCloudItem } from '@/components/DataSourceAnalysis/types.d'
import { fmtNum } from '@/utils'
import { useUserStore } from '@/store'
import { useLoading } from '@/hooks/useLoading'
import type {
  BatchEventStatisticsChannelItem,
  BatchEventStatisticsProvinceMapItem,
  BatchEventStatisticsProvinceTopItem,
  BatchEventStatisticsSceneItem,
  BatchEventStatisticsSentimentOption,
  BatchEventStatisticsSentimentFilter,
  BatchEventStatisticsSeriesDetail,
  BatchEventStatisticsSeriesDistributionModule,
  BatchEventStatisticsSeriesItem
} from '../../types'
import { formatBatchEventDetailTitle, formatBatchEventMainRespUser } from '../../utils'
import EventStatisticsCard from './EventStatisticsCard.vue'
import EventStatisticsMetricCards from './EventStatisticsMetricCards.vue'
import EventStatisticsSeriesDistribution from './EventStatisticsSeriesDistribution.vue'
import EventStatisticsTrendChart from './EventStatisticsTrendChart.vue'

/**
 * 统计指标卡片项。
 * 由批量事件统计接口返回值转换后驱动指标卡展示。
 */
interface EventStatisticsMetricItem {
  label: string
  value: string
  tone: 'negative' | 'positive' | 'neutral'
  iconName: string
}

/**
 * 趋势序列项。
 * 使用日期和三条提及量曲线驱动“数据趋势变化”图表。
 */
interface EventTrendSeriesItem {
  date: string
  positiveMentions: number
  neutralMentions: number
  negativeMentions: number
}

interface Props {
  row?: Partial<BatchRiskEventPageVo> & Record<string, any>
  briefData?: BatchEventBriefDetailVo
  startTime?: string
  endTime?: string
}

/**
 * 批量事件统计概览页。
 * 报告解读复用现有组件，其他统计模块按批量事件统计接口返回值展示。
 */
defineOptions({
  name: 'EventStatistics'
})

const props = defineProps<Props>()
const userStore = useUserStore()
const { showLoading, hideLoading } = useLoading()

const metricData = ref<BatchEventDataStatVo>({})
const trendData = ref<BatchEventTrendStatVo>({})
const statisticsLoading = ref(false)
const selectedSeriesCode = ref<string>()
const selectedSentiment = ref<BatchEventStatisticsSentimentFilter>('all')
const reportSummaryParams = ref<Record<string, any>>({})
const DEFAULT_SERIES_DETAIL_KEY = '__all__'
const SERIES_TOP_COUNT = 10
const PROVINCE_TOP_COUNT = 10
const PROVINCE_TOP_COLORS = [
  '#1F6FFF',
  '#2EC9D3',
  '#FFB300',
  '#FF7436',
  '#7B9DD8',
  '#65D6A6',
  '#9B7BFF',
  '#FF8A8B',
  '#38BDF8',
  '#F97316'
]
const seriesDetailRequests = new Map<string, Promise<BatchEventStatisticsSeriesDetail | null>>()
let statisticsFullscreenLoadingCount = 0
const seriesDistributionModule = ref<BatchEventStatisticsSeriesDistributionModule>({
  sentimentOptions: [],
  seriesDistribution: [],
  seriesDetails: {}
})

/**
 * 将字典字段值转换为稳定字符串，保留 0 这类有效值。
 * @param value 字典字段值
 * @returns 字符串值
 */
const formatDictField = (value: unknown) => {
  return value === null || value === undefined ? '' : String(value)
}

/**
 * 从字典项中按候选字段取出首个有效值，兼容 PC/H5 常见字典字段命名。
 * @param item 字典项
 * @param keys 候选字段名
 * @returns 字典字段值
 */
const pickDictField = (item: Record<string, unknown>, keys: string[]) => {
  for (const key of keys) {
    const value = item?.[key]
    if (value !== null && value !== undefined && value !== '') {
      return value
    }
  }

  return ''
}

/**
 * 获取字典项展示文案。
 * @param item 字典项
 * @returns 字典展示文案
 */
const getDictItemLabel = (item: Record<string, unknown>) => {
  return formatDictField(
    pickDictField(item, ['text', 'label', 'name', 'itemText', 'itemTextEn', 'value', 'itemValue'])
  )
}

/**
 * 获取字典项提交值。
 * @param item 字典项
 * @returns 字典提交值
 */
const getDictItemValue = (item: Record<string, unknown>) => {
  return formatDictField(
    pickDictField(item, ['value', 'itemValue', 'text', 'label', 'name', 'itemText'])
  )
}

const sentimentOptions = computed<BatchEventStatisticsSentimentOption[]>(() => [
  { label: '全部情感', value: 'all' },
  ...userStore.getDictItems('voc_sentiment').map((item: Record<string, unknown>) => ({
    label: getDictItemLabel(item),
    value: getDictItemValue(item)
  }))
])

const eventId = computed(() => (props.row?.id ? String(props.row.id) : ''))
const briefData = computed(() => props.briefData || {})

const displayTitle = computed(() => {
  return formatBatchEventDetailTitle(
    briefData.value.eventName || props.row?.warningEventName,
    briefData.value.warningEventNo || props.row?.warningEventNo
  )
})

const priorityTag = computed(() => {
  return (props.row?.eventPriority || briefData.value.eventPriorityName || '').toLocaleUpperCase()
})

const priorityTagType = computed(() => {
  return (props.row?.eventPriority || briefData.value.eventPriorityName || '').toLocaleLowerCase()
})

const businessTag = computed(() => {
  return briefData.value.taskStatusName || props.row?.taskStatusName || ''
})

const isRejected = computed(() => {
  // 驳回需要审核，审核驳回期间状态可能被推进；进入闭环处理后不再展示驳回标识。
  return briefData.value.isReject === '1' && briefData.value.taskStatusName !== '闭环处理'
})

const rejectReasonText = computed(() => {
  return briefData.value.rejectReason || props.row?.rejectReason || '暂无驳回原因'
})

const summaryItems = computed(() => {
  const warningInfo = [
    briefData.value.warningPeriod || props.row?.warningPeriod,
    briefData.value.warningTime || props.row?.warningTime
  ]
    .filter(Boolean)
    .join(' / ')
  const mainRespUserName = briefData.value.mainRespUserName || props.row?.mainRespUserName
  const mainRespUserEmpNo = briefData.value.mainRespUserEmpNo || props.row?.mainRespUserEmpNo

  return [
    { label: '事件信息', value: briefData.value.eventName || props.row?.warningEventName || '-' },
    { label: '预警频率/时间', value: warningInfo || '-' },
    {
      label: '主题分类',
      value: briefData.value.subjectCategoryName || props.row?.subjectCategoryName || '-'
    },
    { label: '品牌范围', value: briefData.value.brandName || props.row?.brandName || '-' },
    {
      label: '业务责任人',
      value: formatBatchEventMainRespUser(mainRespUserName, mainRespUserEmpNo)
    },
    {
      label: '主责部门',
      value: briefData.value.primaryDepName || '-'
    }
  ]
})

const focusTopics = computed(() => {
  if (Array.isArray(briefData.value.focusTopics)) {
    return briefData.value.focusTopics.filter(Boolean)
  }

  return []
})

/**
 * 将接口返回的字符串或数字转换为安全数值。
 * @param value 接口数值字段
 * @returns 可参与图表计算的数值
 */
const toStatNumber = (value?: string | number | null) => {
  const numericValue = Number(value)
  return Number.isFinite(numericValue) ? numericValue : 0
}

/**
 * 将后端小数占比转换为百分比展示。
 * @param value 后端小数占比
 * @returns 百分比文本
 */
const formatRatioValue = (value?: string | number) => {
  if (value === null || value === undefined || value === '') return '-'
  return `${(toStatNumber(value) * 100).toFixed(2)}%`
}

const metricCards = computed<EventStatisticsMetricItem[]>(() => {
  return [
    {
      label: '负面率',
      value: formatRatioValue(metricData.value.negativeRatio),
      tone: 'negative',
      iconName: 'nsr-zm'
    },
    {
      label: '正面率',
      value: formatRatioValue(metricData.value.positiveRatio),
      tone: 'positive',
      iconName: 'nsr-my'
    },
    {
      label: '提及量',
      value:
        metricData.value.mentionCount !== null && metricData.value.mentionCount !== undefined
          ? fmtNum(toStatNumber(metricData.value.mentionCount))
          : '-',
      tone: 'neutral',
      iconName: 'voiceprint-fill'
    },
    {
      label: '用户数',
      value:
        metricData.value.userCount !== null && metricData.value.userCount !== undefined
          ? fmtNum(toStatNumber(metricData.value.userCount))
          : '-',
      tone: 'neutral',
      iconName: 'users02'
    }
  ]
})

const trendSeries = computed<EventTrendSeriesItem[]>(() => {
  const itemMap = new Map<string, EventTrendSeriesItem>()

  const ensureItem = (date?: string) => {
    const key = date || ''
    if (!key) return null
    if (!itemMap.has(key)) {
      itemMap.set(key, {
        date: key,
        positiveMentions: 0,
        neutralMentions: 0,
        negativeMentions: 0
      })
    }
    return itemMap.get(key)!
  }

  trendData.value.positive?.forEach(item => {
    const target = ensureItem(item.date)
    if (target) target.positiveMentions = toStatNumber(item.count)
  })
  trendData.value.neutral?.forEach(item => {
    const target = ensureItem(item.date)
    if (target) target.neutralMentions = toStatNumber(item.count)
  })
  trendData.value.negative?.forEach(item => {
    const target = ensureItem(item.date)
    if (target) target.negativeMentions = toStatNumber(item.count)
  })

  return Array.from(itemMap.values()).sort((prev, next) => prev.date.localeCompare(next.date))
})

/**
 * 将接口情感值转换成词云颜色可识别的情感名称。
 * @param sentiment 接口返回的情感值或情感名称
 * @returns 标准情感名称
 */
const normalizeSentimentName = (sentiment?: string) => {
  const sentimentText = String(sentiment || '')
  const dictItem = userStore.getDictItems('voc_sentiment').find((item: Record<string, unknown>) => {
    return [
      getDictItemValue(item),
      getDictItemLabel(item),
      item.value,
      item.itemValue,
      item.text,
      item.label,
      item.name,
      item.itemText
    ].some(value => {
      return formatDictField(value) === sentimentText
    })
  })
  const displayText = dictItem ? getDictItemLabel(dictItem) : sentimentText
  const lowerText = displayText.toLocaleLowerCase()

  if (displayText.includes('正') || lowerText.includes('positive')) return '正面'
  if (displayText.includes('中') || lowerText.includes('neutral')) return '中性'
  if (displayText.includes('负') || lowerText.includes('negative')) return '负面'

  return '中性'
}

/**
 * 将观点接口数据转换成词云数据。
 * @param items 观点列表
 * @returns 词云展示数据
 */
const buildWordCloudItems = (
  items: Array<{ opinion?: string; sentiment?: string; totalMentions?: string | number }>
): WordCloudItem[] => {
  return items
    .filter(item => item.opinion)
    .map(item => ({
      name: item.opinion || '',
      value: toStatNumber(item.totalMentions),
      sentiment: normalizeSentimentName(item.sentiment)
    }))
}

/**
 * 构建当前接口结果对应的词云映射。
 * 观点接口已经按情感过滤，因此统一放入 all 槽位供子组件展示。
 * @param items 词云展示数据
 * @returns 当前词云映射
 */
const buildCurrentWordCloudMap = (items: WordCloudItem[] = []) => {
  return {
    all: Array.isArray(items) ? items : []
  }
}

/**
 * 按车系构建联动统计详情。
 * @param carSeriesCode 车系编码；为空时查询当前事件默认明细
 * @returns 当前筛选条件下的联动详情
 */
const loadSeriesDetail = async (
  carSeriesCode?: string
): Promise<BatchEventStatisticsSeriesDetail | null> => {
  if (!eventId.value) return null

  const query = {
    id: eventId.value,
    ...(carSeriesCode ? { carSeriesCode } : {})
  }

  const [sceneRes, provinceRes, channelRes] = await Promise.allSettled([
    getBatchEventSceneStat(query),
    getBatchEventProvinceStat(query),
    getBatchEventChannelStat(query)
  ])

  const scenes =
    sceneRes.status === 'fulfilled' && Array.isArray(sceneRes.value.result)
      ? sceneRes.value.result
      : []
  const provinces =
    provinceRes.status === 'fulfilled' && Array.isArray(provinceRes.value.result)
      ? provinceRes.value.result
      : []
  const channels =
    channelRes.status === 'fulfilled' && Array.isArray(channelRes.value.result)
      ? channelRes.value.result
      : []

  const provinceTopSource = [...provinces]
    .sort(
      (prev, next) => toStatNumber(next.num ?? next.count) - toStatNumber(prev.num ?? prev.count)
    )
    .slice(0, PROVINCE_TOP_COUNT)

  return {
    sceneDistribution: scenes.map<BatchEventStatisticsSceneItem>(scene => ({
      sceneName: scene.sceneName || '-',
      positiveMentions: scene.positiveCount || 0,
      neutralMentions: scene.neutralCount || 0,
      negativeMentions: scene.negativeCount || 0
    })),
    wordCloudMap: buildCurrentWordCloudMap(),
    provinceMapData: provinces.map<BatchEventStatisticsProvinceMapItem>(province => ({
      provinceName: province.provinceName || '',
      provinceCode: province.provinceCode || '',
      mentions: toStatNumber(province.num ?? province.count),
      negativeRate: Number(toStatNumber(province.percentage).toFixed(2))
    })),
    provinceTopList: provinceTopSource.map<BatchEventStatisticsProvinceTopItem>(
      (province, index) => ({
        provinceName: province.provinceName || '-',
        percent: Number(toStatNumber(province.percentage).toFixed(2)),
        color: PROVINCE_TOP_COLORS[index] || '#7B9DD8'
      })
    ),
    channelTable: channels.map<BatchEventStatisticsChannelItem>(channel => ({
      channelName: channel.channelName || '-',
      mentions: toStatNumber(channel.num ?? channel.count),
      ratio: Number(toStatNumber(channel.percentage).toFixed(2))
    }))
  }
}

/**
 * 获取车系明细缓存键。
 * 空车系编码表示事件默认明细，不传 carSeriesCode 给后端。
 * @param carSeriesCode 车系编码
 * @returns 明细缓存键
 */
const getSeriesDetailKey = (carSeriesCode?: string) => {
  return carSeriesCode || DEFAULT_SERIES_DETAIL_KEY
}

/**
 * 获取车系联动详情请求。
 * 对同一车系或默认明细的并发点击做请求复用，避免重复触发四个明细接口。
 * @param carSeriesCode 车系编码
 * @returns 当前筛选条件下的联动详情请求
 */
const getSeriesDetailRequest = (carSeriesCode?: string) => {
  const detailKey = getSeriesDetailKey(carSeriesCode)
  const cachedRequest = seriesDetailRequests.get(detailKey)
  if (cachedRequest) return cachedRequest

  const request = loadSeriesDetail(carSeriesCode).finally(() => {
    if (seriesDetailRequests.get(detailKey) === request) {
      seriesDetailRequests.delete(detailKey)
    }
  })
  seriesDetailRequests.set(detailKey, request)
  return request
}

/**
 * 请求观点评价 TOP 数据。
 * 情感为全部时不传 sentiment，保持与竞品分析页一致。
 * @param carSeriesCode 车系编码
 * @param sentiment 情感字典值
 * @returns 当前条件下的词云数据
 */
const loadOpinionItems = async (carSeriesCode?: string, sentiment?: string) => {
  if (!eventId.value) return null

  const response = await getBatchEventOpinionStat({
    id: eventId.value,
    ...(carSeriesCode ? { carSeriesCode } : {}),
    ...(sentiment ? { sentiment } : {})
  })

  const items = Array.isArray(response.result) ? response.result : []
  return buildWordCloudItems(items)
}

/**
 * 更新指定车系详情中的当前观点词云数据。
 * 词云组件只读取 all 槽位；情感切换时由接口返回结果直接覆盖当前展示数据。
 * @param carSeriesCode 车系编码
 * @param items 当前筛选条件下的词云数据
 */
const setSeriesOpinionItems = (carSeriesCode: string | undefined, items: WordCloudItem[]) => {
  const detailKey = getSeriesDetailKey(carSeriesCode)
  const currentDetail = seriesDistributionModule.value.seriesDetails[detailKey]

  if (!currentDetail) return

  seriesDistributionModule.value = {
    ...seriesDistributionModule.value,
    seriesDetails: {
      ...seriesDistributionModule.value.seriesDetails,
      [detailKey]: {
        ...currentDetail,
        wordCloudMap: {
          ...currentDetail.wordCloudMap,
          all: items
        }
      }
    }
  }
}

/**
 * 按当前车系和情感加载观点词云。
 * 只更新 wordCloudMap，不触发聚焦场景、省份分布、渠道分布重载。
 * @param carSeriesCode 车系编码
 * @param sentimentValue 情感选项值，all 表示全部情感
 */
const loadOpinionDetail = async (carSeriesCode: string | undefined, sentimentValue: string) => {
  if (!eventId.value) return

  const normalizedSentiment = sentimentValue === 'all' ? '' : sentimentValue
  const detailKey = getSeriesDetailKey(carSeriesCode)
  const targetDetail = seriesDistributionModule.value.seriesDetails[detailKey]

  if (!targetDetail) return

  const requestEventId = eventId.value
  const items = await loadOpinionItems(carSeriesCode, normalizedSentiment)

  if (!items || requestEventId !== eventId.value) return

  const shouldUpdateCurrent =
    selectedSeriesCode.value === carSeriesCode && selectedSentiment.value === sentimentValue

  if (shouldUpdateCurrent) {
    setSeriesOpinionItems(carSeriesCode, items)
  }
}

/**
 * 处理车系分布点击联动。
 * 仅在用户选中具体车系后按需加载对应明细，并把结果缓存到当前事件详情内。
 * @param carSeriesCode 选中的车系编码，未传表示取消联动
 */
const handleSeriesCodeChange = async (carSeriesCode?: string) => {
  selectedSeriesCode.value = carSeriesCode

  if (!eventId.value) return

  const detailKey = getSeriesDetailKey(carSeriesCode)
  if (seriesDistributionModule.value.seriesDetails[detailKey]) {
    await loadOpinionDetail(carSeriesCode, selectedSentiment.value)
    return
  }

  const requestEventId = eventId.value
  const detail = await getSeriesDetailRequest(carSeriesCode)

  if (!detail || requestEventId !== eventId.value) return

  seriesDistributionModule.value = {
    ...seriesDistributionModule.value,
    seriesDetails: {
      ...seriesDistributionModule.value.seriesDetails,
      [detailKey]: detail
    }
  }

  await loadOpinionDetail(carSeriesCode, selectedSentiment.value)
}

/**
 * 处理观点评价 TOP 情感切换。
 * 只按当前车系重载观点接口，避免连带刷新其他统计卡片。
 * @param sentiment 情感选项值
 */
const handleSentimentChange = async (sentiment: string) => {
  selectedSentiment.value = sentiment

  if (!eventId.value) return

  const detailKey = getSeriesDetailKey(selectedSeriesCode.value)
  if (!seriesDistributionModule.value.seriesDetails[detailKey]) {
    await handleSeriesCodeChange(selectedSeriesCode.value)
    return
  }

  await loadOpinionDetail(selectedSeriesCode.value, sentiment)
}

/**
 * 展示事件统计全屏加载态。
 * 使用本组件计数避免并发切换事件时提前关闭，也避免卸载后影响其它全局 loading。
 */
const showStatisticsFullscreenLoading = () => {
  statisticsFullscreenLoadingCount += 1
  showLoading({ text: '事件统计加载中...' })
}

/**
 * 关闭本组件打开的一次事件统计全屏加载态。
 */
const hideStatisticsFullscreenLoading = () => {
  if (statisticsFullscreenLoadingCount <= 0) return

  statisticsFullscreenLoadingCount -= 1
  hideLoading()
}

/**
 * 组件卸载时清理本组件未结束的全屏 loading，避免关闭弹窗后残留遮罩。
 */
const clearStatisticsFullscreenLoading = () => {
  while (statisticsFullscreenLoadingCount > 0) {
    hideStatisticsFullscreenLoading()
  }
}

/**
 * 加载批量事件统计全部模块。
 */
const loadStatistics = async () => {
  statisticsLoading.value = false
  metricData.value = {}
  trendData.value = {}
  reportSummaryParams.value = {}
  selectedSeriesCode.value = undefined
  selectedSentiment.value = 'all'
  seriesDetailRequests.clear()
  seriesDistributionModule.value = {
    ...seriesDistributionModule.value,
    seriesDistribution: [],
    seriesDetails: {}
  }

  if (!eventId.value) return

  const requestEventId = eventId.value
  const query = { id: requestEventId }
  statisticsLoading.value = true
  showStatisticsFullscreenLoading()

  try {
    const [metricRes, trendRes, seriesRes] = await Promise.allSettled([
      getBatchEventDataStat(query),
      getBatchEventTrendStat(query),
      getBatchEventCarSeriesStat(query)
    ])

    if (requestEventId !== eventId.value) return

    if (metricRes.status === 'fulfilled' && metricRes.value.result) {
      metricData.value = metricRes.value.result
    }
    if (trendRes.status === 'fulfilled' && trendRes.value.result) {
      trendData.value = trendRes.value.result
    }

    const seriesItems =
      seriesRes.status === 'fulfilled' && Array.isArray(seriesRes.value.result)
        ? seriesRes.value.result
        : []
    const seriesTopItems = [...seriesItems]
      .sort(
        (prev, next) => toStatNumber(next.num ?? next.count) - toStatNumber(prev.num ?? prev.count)
      )
      .slice(0, SERIES_TOP_COUNT)

    seriesDistributionModule.value = {
      ...seriesDistributionModule.value,
      seriesDistribution: seriesTopItems.map<BatchEventStatisticsSeriesItem>(item => ({
        code: item.carSeriesCode || item.carSeriesName || '',
        name: item.carSeriesName || item.carSeriesCode || '-',
        mentions: toStatNumber(item.num ?? item.count)
      })),
      seriesDetails: {}
    }

    try {
      await handleSeriesCodeChange()
    } catch (error) {
      if (requestEventId === eventId.value) {
        console.error('获取批量事件联动统计失败:', error)
      }
    }
  } finally {
    hideStatisticsFullscreenLoading()

    if (requestEventId === eventId.value) {
      statisticsLoading.value = false
    }
  }

  if (requestEventId !== eventId.value) return

  reportSummaryParams.value = query
}

watch(eventId, () => void loadStatistics(), { immediate: true })

onBeforeUnmount(() => {
  clearStatisticsFullscreenLoading()
})
</script>

<template>
  <div class="event-statistics">
    <div class="event-statistics__headline">
      <div class="event-statistics__headline-title">{{ displayTitle }}</div>
      <EventPriorityTag v-if="priorityTag" :tag-name="priorityTag" :type="priorityTagType" />
      <el-tooltip v-if="isRejected" :content="rejectReasonText" placement="top">
        <div class="event-statistics__reject-tag">驳</div>
      </el-tooltip>
      <div v-if="businessTag" class="event-statistics__scene-tag">
        <span class="event-statistics__scene-dot"></span>
        <span>{{ businessTag }}</span>
      </div>
    </div>

    <EventStatisticsCard :summary-items="summaryItems" :focus-topics="focusTopics" />

    <div class="event-statistics__modules">
      <div class="event-statistics__report-summary">
        <ReportSummary
          :api-function="getBatchEventReportSummaryStream"
          :query-params="reportSummaryParams"
        />
      </div>

      <EventStatisticsMetricCards :items="metricCards" />

      <EventStatisticsTrendChart :series="trendSeries" />

      <EventStatisticsSeriesDistribution
        :selected-series-code="selectedSeriesCode"
        :selected-sentiment="selectedSentiment"
        :sentiment-options="sentimentOptions"
        :module-data="seriesDistributionModule"
        @update:selected-series-code="handleSeriesCodeChange"
        @update:selected-sentiment="handleSentimentChange"
      />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.event-statistics {
  --event-statistics-border: #ebedf0;
  --event-statistics-text-primary: rgba(0, 0, 0, 0.9);
  --event-statistics-text-secondary: rgba(0, 0, 0, 0.4);
  --event-statistics-accent: #1677ff;
  --event-statistics-chip-bg: #fff;

  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 100%;
}

.event-statistics__headline {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
  flex-wrap: wrap;
}

.event-statistics__headline-title {
  position: relative;
  padding-left: 12px;
  font-weight: 500;
  font-size: 16px;
  line-height: 24px;
  color: var(--event-statistics-text-primary);
  word-break: break-word;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    width: 3px;
    height: 24px;
    border-radius: 99px;
    background: var(--event-statistics-accent);
  }
}

.event-statistics__reject-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 34px;
  padding: 1px 8px;
  border-radius: 4px;
  background: #fff7e8;
  color: #ff7d00;
  font-size: 14px;
  font-weight: 500;
  line-height: 22px;
  box-sizing: border-box;
  cursor: default;
}

.event-statistics__scene-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0 10px;
  border-radius: 6px;
  background: #edf5ff;
  color: var(--event-statistics-accent);
  font-size: 14px;
  line-height: 24px;
  box-sizing: border-box;
}

.event-statistics__scene-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

.event-statistics__modules {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.event-statistics__report-summary {
  position: relative;
  min-height: 140px;
}
</style>
