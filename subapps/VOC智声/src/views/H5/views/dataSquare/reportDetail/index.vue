<script setup lang="ts">
import dayjs from 'dayjs'
import { computed, onActivated, watch, nextTick, reactive, ref, type Ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import filterLinesPng from '@/assets/h5/filter-lines.png'
import HPage from '@h5/components/UI/HPage'
import HNavBar from '@h5/components/UI/HNavBar'
import HCard from '@h5/components/UI/HCard/index.vue'
import HVoiceList from '@h5/components/HVoiceList/index.vue'
import BackToAnchor from '@h5/components/BackToAnchor/index.vue'
import { useKeepAliveScroll } from '@h5/hooks/useKeepAliveScroll'
import {
  getH5DataSquareDrillDownBrief,
  getH5DataSquareReportDetail,
  getH5DataSquareSeriesRank,
  getH5DataSquareTagAnalysis,
  type H5DataSquareDrillDownBrief,
  type H5DataSquareReportDateCondition,
  type H5DataSquareReportDefaultCondition,
  type H5DataSquareReportDetail
} from '@h5/api/dataSquare'
import { getUserDynamicEvaluation } from '@h5/api/home'
import type { H5VocBaseRequest, VoiceListItem } from '@h5/api/home/types'
import { getDataTrendChange, getUserIntentionOpinionTop } from '@h5/api/rootCauseAnalysis'
import type {
  IntentionOpinionTopVo,
  ProductTrendPointVo,
  SeriesRankItemVo,
  TagSentimentAnalysisVo
} from '@h5/api/rootCauseAnalysis/types'
import CoreDataCard from './components/CoreDataCard.vue'
import TrendChangeCard from './components/TrendChangeCard.vue'
import CarSeriesRankCard from './components/CarSeriesRankCard.vue'
import IndicatorAnalysisCard from './components/IndicatorAnalysisCard.vue'
import OpinionCloudCard from './components/OpinionCloudCard.vue'
import ReportFilterStub from './components/ReportFilterStub.vue'
import ReportFilterPanel from './components/ReportFilterPanel.vue'
import { invokeShareAppMessage } from '@h5/utils/weWork'
import { isWeWorkEnvironment } from '@/utils/environment'
import { useShareStore } from '@h5/store'
import {
  buildReportBaseParams,
  buildReportBaseParamsByCondition,
  buildTagDrillParams,
  isOnlyTopicWithoutExperienceCode,
  mapVoiceItem,
  normalizeReportDefaultCondition
} from './utils'

defineOptions({
  name: 'H5DataSquareReportDetail'
})

type LoadMoreSource = 'reset' | 'append'
type SeriesSortField = 'mentions' | 'mentionsMoM'
type SeriesSortOrder = 'asc' | 'desc'

const route = useRoute()
const router = useRouter()
const shareStore = useShareStore()

// 报告不同则使用独立滚动缓存，返回原声详情时恢复当前报告的阅读位置
const { setPosition: setReportScrollPosition } = useKeepAliveScroll({
  getCacheKey: currentRoute => String(currentRoute.fullPath || currentRoute.name || '')
})

const seriesAnchor = ref<HTMLElement | null>(null)
const indicatorAnchor = ref<HTMLElement | null>(null)
const opinionAnchor = ref<HTMLElement | null>(null)
const voiceAnchor = ref<HTMLElement | null>(null)

const hub = reactive({
  pageLoading: false,
  detailLoadFailed: false,
  detailEmptyText: '暂无数据',
  filterVisible: false,
  reportDetail: null as H5DataSquareReportDetail | null,
  appliedCondition: null as H5DataSquareReportDefaultCondition | null,
  appliedDateCondition: null as H5DataSquareReportDateCondition | null,
  baseParams: {} as H5VocBaseRequest,
  briefData: null as H5DataSquareDrillDownBrief | null,
  trendData: [] as ProductTrendPointVo[],
  seriesFullData: [] as SeriesRankItemVo[],
  seriesData: [] as SeriesRankItemVo[],
  tagData: [] as TagSentimentAnalysisVo[],
  opinionData: [] as IntentionOpinionTopVo[],
  activeSeries: null as SeriesRankItemVo | null,
  activeTag: null as TagSentimentAnalysisVo | null,
  activeOpinion: null as IntentionOpinionTopVo | null,
  seriesPageNum: 1,
  seriesPageSize: 10,
  seriesHasMore: false,
  seriesLoading: false,
  seriesSortField: undefined as SeriesSortField | undefined,
  seriesSortOrder: undefined as SeriesSortOrder | undefined,
  voiceData: {
    loading: false,
    finished: false,
    pageNum: 1,
    pageSize: 10,
    total: 0,
    list: [] as VoiceListItem[]
  }
})

const reportId = computed(() => String(route.query.reportId || ''))
const currentReportConditionDetail = computed<H5DataSquareReportDetail | null>(() => {
  if (!hub.reportDetail) {
    return null
  }
  return {
    ...hub.reportDetail,
    defaultCondition: hub.appliedCondition || hub.reportDetail.defaultCondition
  }
})
const hideCarSeriesCard = computed(
  () => currentReportConditionDetail.value?.defaultCondition?.carSeriesList?.length === 1
)
const hideIndicatorCard = computed(() =>
  isOnlyTopicWithoutExperienceCode(currentReportConditionDetail.value)
)
const activeSeriesName = computed(() => hub.activeSeries?.name || '')
const activeTagName = computed(() => hub.activeTag?.tagName || '')
const activeOpinionName = computed(() => hub.activeOpinion?.opinion || '')
const initialCondition = computed(() => hub.reportDetail?.defaultCondition || null)
const initialDateCondition = computed(() => hub.reportDetail?.dateCondition || null)
const isPreviewMode = computed(() => route.query.preview === '1')
const previewTitle = computed(() => String(route.query.previewTitle || ''))
const isDetailEmpty = computed(
  () => !hub.pageLoading && (hub.detailLoadFailed || !hub.reportDetail)
)
const showVoiceCard = computed(() => hub.voiceData.loading || hub.voiceData.list.length > 0)
const navTitle = computed(
  () =>
    previewTitle.value ||
    hub.reportDetail?.reportName ||
    String(route.query.reportName || '数据报告')
)

// 计算分享标题
const shareTitle = computed(() => {
  return hub.reportDetail?.reportName || '数据报告详情'
})

const REPORT_DETAIL_ROUTE_NAME = 'H5DataSquareReportDetail'
const VOICE_DETAIL_ROUTE_NAME = 'H5VoiceDetail'

/**
 * 格式化分享描述中的日期，避免不同 WebView 对 YYYY-MM-DD 解析不一致。
 * @param date 原始日期
 * @returns 展示日期
 */
const formatShareDate = (date?: string) => {
  if (!date) {
    return ''
  }

  const parsedDate = dayjs(date)
  return parsedDate.isValid() ? parsedDate.format('YYYY/MM/DD') : date
}

// 计算分享描述：报告名称+品牌+时间范围
const shareDesc = computed(() => {
  if (!hub.reportDetail) return '数据报告详情描述'

  const parts: string[] = []

  // 报告名称
  if (hub.reportDetail.reportName) {
    parts.push(hub.reportDetail.reportName)
  }

  // 品牌
  if (hub.reportDetail.brandName) {
    parts.push(`品牌：${hub.reportDetail.brandName}`)
  }

  // 时间范围
  if (hub.reportDetail.dateCondition?.startDate && hub.reportDetail.dateCondition?.endDate) {
    const startDate = formatShareDate(hub.reportDetail.dateCondition.startDate)
    const endDate = formatShareDate(hub.reportDetail.dateCondition.endDate)
    parts.push(`时间：${startDate}-${endDate}`)
  } else if (hub.reportDetail.dateCondition?.selectedShortcut) {
    parts.push(`时间：${hub.reportDetail.dateCondition.selectedShortcut}`)
  }

  return parts.length > 0 ? parts.join(' | ') : '数据报告详情描述'
})

/**
 * 同步当前报告的分享信息。
 * keep-alive 返回本页时，标题和描述未变化不会触发 watch，需要主动覆盖详情页写入的分享信息。
 */
const syncShareInfo = () => {
  shareStore.setShareInfo(shareTitle.value, shareDesc.value)
}

/**
 * 将当前报告详情的滚动缓存和容器位置重置到顶部。
 * 外部入口重新进入时需要从顶部开始阅读，原声详情返回时不调用该方法。
 */
const resetReportScrollTop = (shouldScrollContainer = true) => {
  setReportScrollPosition(0)
  if (!shouldScrollContainer) return

  nextTick(() => {
    const container = document.querySelector('.f-page__content') as HTMLElement | null
    container?.scrollTo({ top: 0, behavior: 'auto' })
  })
}

/**
 * 将下游卡片滚动到可视区域顶部附近。
 * @param targetRef 目标卡片前置锚点
 */
const scrollToAnchor = (targetRef: Ref<HTMLElement | null>) => {
  nextTick(() => {
    try {
      const container = document.querySelector('.f-page__content') as HTMLElement | null
      const target = targetRef.value
      if (!container || !target) return

      const containerTop = container.getBoundingClientRect().top
      const targetTop = target.getBoundingClientRect().top
      const top = container.scrollTop + (targetTop - containerTop)
      container.scrollTo({ top, behavior: 'smooth' })
    } catch (error) {
      console.warn('报告详情锚点滚动失败:', error)
    }
  })
}

/**
 * 页面返回上一层。
 */
const handleBack = () => {
  router.back()
}

/**
 * 打开报告筛选弹层。
 */
const handleFilterOpen = () => {
  if (isPreviewMode.value) return
  if (!hub.reportDetail) return
  hub.filterVisible = true
}

// 处理分享点击
const handleShare = () => {
  invokeShareAppMessage(shareStore.shareTitle, shareStore.shareDesc)
}

/**
 * 组合全页面当前筛选条件。
 * @returns 当前查询参数
 */
const getCurrentParams = (): H5VocBaseRequest => {
  return {
    ...hub.baseParams,
    ...(hub.activeSeries?.code ? { carSeriesCode: hub.activeSeries.code } : {}),
    ...(hub.activeTag ? buildTagDrillParams(hub.activeTag) : {}),
    ...(hub.activeOpinion?.opinion ? { topic: hub.activeOpinion.opinion } : {})
  }
}

/**
 * 组合车系联动后的下游筛选条件。
 * @returns 查询参数
 */
const getSeriesLinkedParams = (): H5VocBaseRequest => {
  return {
    ...hub.baseParams,
    ...(hub.activeSeries?.code ? { carSeriesCode: hub.activeSeries.code } : {})
  }
}

/**
 * 组合车系与指标联动后的下游筛选条件。
 * @returns 查询参数
 */
const getTagLinkedParams = (): H5VocBaseRequest => {
  return {
    ...getSeriesLinkedParams(),
    ...(hub.activeTag ? buildTagDrillParams(hub.activeTag) : {})
  }
}

/**
 * 获取核心数据。
 */
const fetchBriefData = async () => {
  try {
    const response = await getH5DataSquareDrillDownBrief(getCurrentParams(), {
      cancelPrevious: true
    })
    hub.briefData = response.success ? response.result || null : null
  } catch (error) {
    console.error('获取数据报告核心数据失败:', error)
    hub.briefData = null
  }
}

/**
 * 获取趋势变化数据。
 */
const fetchTrendData = async () => {
  try {
    const response = await getDataTrendChange(getCurrentParams(), { cancelPrevious: true })
    const getTrendDateTime = (date?: string) => (date ? new Date(date).getTime() || 0 : 0)
    hub.trendData =
      response.success && Array.isArray(response.result)
        ? [...response.result].sort((a, b) => {
            return getTrendDateTime(a.date) - getTrendDateTime(b.date)
          })
        : []
  } catch (error) {
    console.error('获取数据报告趋势变化失败:', error)
    hub.trendData = []
  }
}

/**
 * 将接口全量车系数据按本地分页同步到展示列表。
 */
const syncSeriesDisplayData = () => {
  const displaySize = hub.seriesPageNum * hub.seriesPageSize

  hub.seriesData = hub.seriesFullData.slice(0, displaySize)
  hub.seriesHasMore = hub.seriesData.length < hub.seriesFullData.length
}

/**
 * 获取车系排行，接口一次性返回全量数据，排序交给接口处理，前端只负责每次 10 条展开。
 */
const fetchSeriesRank = async () => {
  try {
    hub.seriesLoading = true
    hub.seriesPageNum = 1
    hub.seriesFullData = []
    hub.seriesData = []
    hub.seriesHasMore = false

    const response = await getH5DataSquareSeriesRank(
      {
        ...hub.baseParams,
        ...(hub.seriesSortField && hub.seriesSortOrder
          ? {
              sortField: hub.seriesSortField,
              sortOrder: hub.seriesSortOrder
            }
          : {})
      },
      {
        cancelPrevious: true
      }
    )
    const rawResult: any = response.result
    hub.seriesFullData = Array.isArray(rawResult)
      ? rawResult
      : Array.isArray(rawResult?.list)
        ? rawResult.list
        : []

    syncSeriesDisplayData()
  } catch (error) {
    console.error('获取数据报告车系排行失败:', error)
    hub.seriesFullData = []
    hub.seriesData = []
    hub.seriesHasMore = false
  } finally {
    hub.seriesLoading = false
  }
}

/**
 * 获取指标分析数据。
 */
const fetchTagAnalysis = async () => {
  try {
    const response = await getH5DataSquareTagAnalysis(getSeriesLinkedParams(), {
      cancelPrevious: true
    })
    hub.tagData = response.success && Array.isArray(response.result) ? response.result : []
  } catch (error) {
    console.error('获取数据报告指标分析失败:', error)
    hub.tagData = []
  }
}

/**
 * 获取观点评价词云数据。
 */
const fetchOpinionData = async () => {
  try {
    const response = await getUserIntentionOpinionTop(getTagLinkedParams(), {
      cancelPrevious: true
    })
    hub.opinionData =
      response.success && Array.isArray(response.result) ? response.result.slice(0, 20) : []
  } catch (error) {
    console.error('获取数据报告观点评价失败:', error)
    hub.opinionData = []
  }
}

/**
 * 获取客户原声列表。
 * @param source 加载来源
 */
const fetchVoiceList = async (source: LoadMoreSource = 'reset') => {
  try {
    hub.voiceData.loading = true
    if (source === 'reset') {
      hub.voiceData.pageNum = 1
      hub.voiceData.list = []
      hub.voiceData.finished = false
    }

    const response: any = await getUserDynamicEvaluation(
      {
        ...getCurrentParams(),
        pageNum: hub.voiceData.pageNum,
        pageSize: hub.voiceData.pageSize,
        checkPermission: true
      } as H5VocBaseRequest,
      {
        cancelPrevious: true
      }
    )

    const result = response.result || {}
    const list = Array.isArray(result.list) ? result.list.map(mapVoiceItem) : []
    hub.voiceData.total = result.total || 0
    hub.voiceData.list = source === 'append' ? [...hub.voiceData.list, ...list] : list
    hub.voiceData.finished =
      hub.voiceData.list.length >= hub.voiceData.total || list.length < hub.voiceData.pageSize
  } catch (error) {
    console.error('获取数据报告客户原声失败:', error)
    if (source === 'reset') {
      hub.voiceData.list = []
      hub.voiceData.total = 0
    }
    hub.voiceData.finished = true
  } finally {
    hub.voiceData.loading = false
  }
}

/**
 * 刷新当前筛选对应的全页面数据。
 */
const refreshAllData = async () => {
  await Promise.all([
    fetchBriefData(),
    fetchTrendData(),
    hideCarSeriesCard.value ? Promise.resolve() : fetchSeriesRank(),
    hideIndicatorCard.value ? Promise.resolve() : fetchTagAnalysis(),
    fetchOpinionData(),
    fetchVoiceList()
  ])
}

/**
 * 应用筛选条件后刷新整页数据，并清理卡片下钻状态。
 * @param payload 筛选弹层提交值
 */
const handleFilterConfirm = async (payload: {
  defaultCondition: H5DataSquareReportDefaultCondition
  dateCondition: H5DataSquareReportDateCondition
}) => {
  hub.appliedCondition = normalizeReportDefaultCondition(payload.defaultCondition)
  hub.appliedDateCondition = payload.dateCondition
  hub.baseParams = buildReportBaseParamsByCondition(hub.appliedCondition, hub.appliedDateCondition)
  hub.activeSeries = null
  hub.activeTag = null
  hub.activeOpinion = null
  await refreshAllData()
}

/**
 * 刷新车系以下模块。
 */
const refreshAfterSeriesChange = async () => {
  hub.activeTag = null
  hub.activeOpinion = null
  await Promise.all([
    hideIndicatorCard.value ? Promise.resolve() : fetchTagAnalysis(),
    fetchOpinionData(),
    fetchVoiceList()
  ])
}

/**
 * 刷新指标以下模块。
 */
const refreshAfterTagChange = async () => {
  hub.activeOpinion = null
  await Promise.all([fetchOpinionData(), fetchVoiceList()])
}

/**
 * 刷新观点以下模块。
 */
const refreshAfterOpinionChange = async () => {
  await fetchVoiceList()
}

/**
 * 点击车系时切换联动状态。
 * @param item 当前车系
 */
const handleSeriesClick = (item: SeriesRankItemVo) => {
  hub.activeSeries = hub.activeSeries?.code === item.code ? null : item
  void refreshAfterSeriesChange()
  scrollToAnchor(hideIndicatorCard.value ? opinionAnchor : indicatorAnchor)
}

/**
 * 加载更多车系排行。
 */
const handleSeriesLoadMore = () => {
  if (hub.seriesLoading || !hub.seriesHasMore) return
  hub.seriesPageNum += 1
  syncSeriesDisplayData()
}

/**
 * 处理车系排行表头排序变化。
 * @param payload 排序字段和排序方向
 */
const handleSeriesSortChange = (payload: {
  sortField?: SeriesSortField
  sortOrder?: SeriesSortOrder
}) => {
  hub.seriesSortField = payload.sortField
  hub.seriesSortOrder = payload.sortOrder
  hub.seriesPageNum = 1
  hub.activeSeries = null
  hub.activeTag = null
  hub.activeOpinion = null
  void Promise.all([
    fetchSeriesRank(),
    hideIndicatorCard.value ? Promise.resolve() : fetchTagAnalysis(),
    fetchOpinionData(),
    fetchVoiceList()
  ])
}

/**
 * 点击指标时切换联动状态。
 * @param item 当前指标
 */
const handleTagClick = (item: TagSentimentAnalysisVo) => {
  hub.activeTag = hub.activeTag?.tagCode === item.tagCode ? null : item
  void refreshAfterTagChange()
  scrollToAnchor(opinionAnchor)
}

/**
 * 点击观点时切换客户原声联动状态。
 * @param item 当前观点
 */
const handleOpinionClick = (item: IntentionOpinionTopVo) => {
  hub.activeOpinion = hub.activeOpinion?.opinion === item.opinion ? null : item
  void refreshAfterOpinionChange()
  scrollToAnchor(voiceAnchor)
}

/**
 * 客户原声加载更多。
 */
const handleVoiceLoadMore = () => {
  if (hub.voiceData.loading || hub.voiceData.finished) return
  hub.voiceData.pageNum += 1
  void fetchVoiceList('append')
}

/**
 * 点击客户原声进入原声详情。
 * @param item 当前声音
 */
const handleVoiceItemClick = (item: VoiceListItem) => {
  if (isPreviewMode.value) return

  router.push({
    name: 'H5VoiceDetail',
    query: {
      id: item.id || '',
      originalId: item.originalId || '',
      brandCode: item.brandCode || hub.reportDetail?.brandCode || '',
      intent: item.intention || '',
      channelName: item.channel || ''
    }
  })
}

/**
 * 初始化报告详情及默认数据。
 */
const initPage = async (isLoading = true) => {
  if (!reportId.value) {
    showToast('报告信息缺失')
    handleBack()
    return
  }
  if (!isLoading) return

  hub.pageLoading = isLoading
  hub.detailLoadFailed = false
  hub.detailEmptyText = '暂无数据'
  try {
    const response = await getH5DataSquareReportDetail(
      { id: reportId.value },
      { cancelPrevious: true }
    )
    if (!response.success || !response.result) {
      hub.reportDetail = null
      hub.detailLoadFailed = true
      hub.detailEmptyText = response.message || '获取报告详情失败'
      return
    }

    hub.reportDetail = response.result
    hub.appliedCondition = response.result.defaultCondition || null
    hub.appliedDateCondition = response.result.dateCondition || null
    hub.baseParams = buildReportBaseParams(response.result)
    await refreshAllData()
  } catch (error) {
    console.error('初始化数据报告详情失败:', error)
    hub.reportDetail = null
    hub.detailLoadFailed = true
    hub.detailEmptyText = '获取报告详情失败'
  } finally {
    hub.pageLoading = false
  }
}

const reset = () => {
  isFirstLoad.value = true
  resetReportScrollTop()
}

// 控制 keep-alive 激活后的数据刷新：仅原声详情返回保留数据和滚动位置。
const isFirstLoad = ref(true)

watch(
  () => router.currentRoute.value,
  (to, from) => {
    const toName = String(to?.name ?? '')
    const fromName = String(from?.name ?? '')

    if (toName !== REPORT_DETAIL_ROUTE_NAME && toName !== VOICE_DETAIL_ROUTE_NAME) return reset()
    if (fromName === VOICE_DETAIL_ROUTE_NAME) return
  },
  { flush: 'post' }
)

// 监听分享标题和描述变化，更新到store
watch(
  [shareTitle, shareDesc],
  ([title, desc]) => {
    shareStore.setShareInfo(title, desc)
  },
  { immediate: true }
)

onActivated(() => {
  void initPage(isFirstLoad.value).finally(() => {
    isFirstLoad.value = false
    syncShareInfo()
  })
})
</script>

<template>
  <HPage class="data-report-detail" background-color="#f5f7fa">
    <template #nav-bar>
      <HNavBar :title="navTitle" :left-arrow="!isPreviewMode" left-text="" @click-left="handleBack">
        <template v-if="!isPreviewMode" #right>
          <div v-if="isWeWorkEnvironment()" class="data-report-detail__nav-btns">
            <van-image
              @click="handleFilterOpen"
              class="data-report-detail__filter-icon pr-8"
              :src="filterLinesPng"
              width="28"
              height="20"
              fit="contain"
            />
            <div class="split-line"></div>
            <van-icon @click="handleShare" name="share-o" class="data-report-detail__share-icon" />
          </div>
          <div v-else class="data-report-detail__filter-entry" @click="handleFilterOpen">
            <van-image
              class="data-report-detail__filter-icon"
              :src="filterLinesPng"
              width="22"
              height="22"
              fit="contain"
            />
          </div>
        </template>
      </HNavBar>
    </template>

    <div class="data-report-detail__content">
      <van-skeleton v-if="hub.pageLoading" title :row="12" />
      <div v-else-if="isDetailEmpty" class="data-report-detail__empty">
        <van-empty :description="hub.detailEmptyText" />
      </div>
      <template v-else>
        <ReportFilterStub :loading="hub.pageLoading" />
        <CoreDataCard :data="hub.briefData" />
        <TrendChangeCard class="mt-12" :data="hub.trendData" />
        <div ref="seriesAnchor" class="data-report-detail__anchor" />
        <CarSeriesRankCard
          v-if="!hideCarSeriesCard"
          class="mt-12"
          :data="hub.seriesData"
          :active-code="hub.activeSeries?.code"
          :loading="hub.seriesLoading"
          :has-more="hub.seriesHasMore"
          :sort-field="hub.seriesSortField"
          :sort-order="hub.seriesSortOrder"
          @row-click="handleSeriesClick"
          @load-more="handleSeriesLoadMore"
          @sort-change="handleSeriesSortChange"
        />
        <div v-if="!hideIndicatorCard" ref="indicatorAnchor" class="data-report-detail__anchor" />
        <IndicatorAnalysisCard
          v-if="!hideIndicatorCard"
          class="mt-12"
          :data="hub.tagData"
          :active-code="hub.activeTag?.tagCode"
          :title-suffix="activeSeriesName"
          @item-click="handleTagClick"
        />
        <div ref="opinionAnchor" class="data-report-detail__anchor" />
        <OpinionCloudCard
          class="mt-12"
          :data="hub.opinionData"
          :car-series-name="activeSeriesName"
          :tag-name="activeTagName"
          :active-opinion="activeOpinionName"
          @word-click="handleOpinionClick"
        />
        <div ref="voiceAnchor" class="data-report-detail__anchor" />
        <HCard v-if="showVoiceCard" class="mt-12" title="客户原声">
          <template v-if="activeSeriesName || activeTagName || activeOpinionName" #left>
            <div class="data-report-detail__title-suffix van-ellipsis flex-1">
              <span v-if="activeSeriesName">【{{ activeSeriesName }}】</span>
              <span v-if="activeTagName">【{{ activeTagName }}】</span>
              <span v-if="activeOpinionName">【{{ activeOpinionName }}】</span>
            </div>
          </template>
          <HVoiceList
            :loading="hub.voiceData.loading"
            :voice-list="hub.voiceData.list"
            :is-load-more="!hub.voiceData.finished"
            @load-more="handleVoiceLoadMore"
            @item-click="handleVoiceItemClick"
          />
        </HCard>
        <BackToAnchor v-if="!isPreviewMode" :target-el="seriesAnchor" />
      </template>
    </div>

    <ReportFilterPanel
      v-model:show="hub.filterVisible"
      :current-condition="hub.appliedCondition"
      :current-date-condition="hub.appliedDateCondition"
      :initial-condition="initialCondition"
      :initial-date-condition="initialDateCondition"
      @confirm="handleFilterConfirm"
    />
  </HPage>
</template>

<style scoped lang="scss">
.data-report-detail {
  &__content {
    padding: 12px;
  }

  &__nav-btns {
    display: flex;
    align-items: center;
    border-radius: 20px;
    border: 1px solid #ebedf0;
    padding: 4px 8px;
  }

  .split-line {
    width: 1px;
    height: 20px;
    background: #dfe2e8;
  }

  &__share-icon {
    display: block;
    font-size: 17px;
    padding: 2px 0 2px 11px;
  }

  &__filter-entry {
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__filter-icon {
    display: block;
  }

  &__empty {
    min-height: calc(100vh - 92px);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__anchor {
    height: 0;
  }

  &__title-suffix {
    min-width: 0;
    overflow: hidden;
    color: #1677ff;
    font-size: 14px;
    font-weight: 600;
    white-space: nowrap;
    text-overflow: ellipsis;
  }

  :deep(.card) {
    box-shadow: 0 1px 2px rgba(16, 24, 40, 0.04);
  }
}
</style>
