<script setup lang="ts">
import { ref } from 'vue'
import { debounce } from 'lodash-es'
import { useGeneralScenarioStore } from '@/store'
import {
  getProductBrief,
  getDataTrendChange,
  getFocusSceneTop,
  getDataSourceAnalysis,
  getChannelNegativeTrend,
  getChannelMentionShare,
  getAgeDistribution,
  getRegionDistribution,
  getGenderDistribution,
  getUserTypeDistribution,
  getUserFocusSceneTop,
  getVoiceUserTop,
  getJourneyDetailAnalysis,
  getSurgingSceneTop,
  getHighFreqSceneTop,
  getUserIntentionOpinionTop
} from '@/api/journeyAnalysis'
import type {
  ProductBriefVo,
  AgeDistributionVo,
  RegionDistributionVo,
  GenderDistributionVo,
  UserTypeDistributionVo,
  UserFocusSceneTopVo,
  VoiceUserTopVo,
  JourneyDetailAnalysisVo,
  SurgingSceneTopVo,
  HighFreqSceneTopVo,
  IntentionOpinionTopVo
} from '@/api/journeyAnalysis/types.d'
import SCHeader from '@/components/Business/Scene/Common/SCHeader/index.vue'
import ReportSummary from '@/components/Business/Scene/Common/ReportSummary.vue'
import ComprehensiveAnalysis from '@/components/Business/Scene/Common/ComprehensiveAnalysis/index.vue'
import DataSourceAnalysis from '@/components/Business/Scene/Common/DataSourceAnalysis/index.vue'
import PopulationFeatureAnalysis from './PopulationFeatureAnalysis/index.vue'
import JourneyDetailed from './JourneyDetailed/index.vue'
import OpinionDemandAnalysis from './OpinionDemandAnalysis/index.vue'
import { useQueryListener } from '@/hooks/useQueryListener'
import { useLoading } from '@/hooks/useLoading'
import { useUserStore } from '@/store'
import useGeneralDrillDownStore from '@/store/modules/generalDrillDown'
import {
  getUserIntentionOpinionTopResult,
  getJourneyDetailAnalysisResult,
  getFocusSceneTopResult,
  getJADataTrendChangeResult,
  getJAChannelNegativeTrendResult
} from '@/api/reportSummary/index'
import { getDateRange } from '@/utils/date'
import VoiceDetailsDialog from '@components/Business/VoiceDetailsDialog'
import { DrillTabKey } from '@components/Business/DrillDownDialog/constants.ts'
import { useRoute } from 'vue-router'
import UniversaFilter from '@/components/Business/UniversaFilter/index.vue'
import { DEFAULT_MENTION_NEGATIVE_RATE_TYPE } from '@/constants'
import { usePageCardDownload } from '@/hooks/usePageCardDownload'
import { getCardStatExportRequest, journeyStatExportMap } from '@/api/downloadTask'
import { CARD_EXPORT_KEYS } from '@/constants/cardExportKeys'

/**
 * 旅程分析场景
 */
defineOptions({
  name: 'JourneyAnalysis'
})

const generalScenarioStore = useGeneralScenarioStore()
const userStore = useUserStore()
const route = useRoute()
const { showLoading, hideLoading } = useLoading()

const searchParams = ref<any>({})
usePageCardDownload({
  getParams: () => ({ ...searchParams.value }),
  getStatRequest: payload => getCardStatExportRequest(journeyStatExportMap, payload)
})
const tagPath = ref<Array<{ code: string; name: string; level?: number }> | undefined>(undefined)

// ComprehensiveAnalysis 组件引用
const comprehensiveAnalysisRef = ref<InstanceType<typeof ComprehensiveAnalysis>>()
const opinionDemandAnalysisRef = ref<InstanceType<typeof OpinionDemandAnalysis>>()

// 综合分析相关数据
const productBriefData = ref<ProductBriefVo | null>(null)
const focusSceneTopData = ref<any[]>([])
const dataTrendChangeData = ref<any>()

// 数据来源分析相关数据
const dataSourceAnalysisData = ref<any[]>([])
const channelNegativeTrendData = ref<any[]>([])
const channelMentionShareData = ref<any[]>([])

// 人群特征分析
const ageDistributionData = ref<AgeDistributionVo[]>([])
const regionDistributionData = ref<RegionDistributionVo[]>([])
const genderData = ref<GenderDistributionVo[]>([])
const userTypeData = ref<UserTypeDistributionVo[]>([])
const userFocusSceneTopData = ref<UserFocusSceneTopVo[]>([])
const voiceUserTopData = ref<VoiceUserTopVo[]>([])

// 旅程细化分析
const journeyDetailAnalysisData = ref<JourneyDetailAnalysisVo[]>([])
const surgingSceneTopData = ref<SurgingSceneTopVo[]>([])
const highFreqSceneTopData = ref<HighFreqSceneTopVo[]>([])
const journeyDataType = ref<MentionNegativeRateType>('negativeRate')
// 缓存旅程图表点击后的下钻参数，供场景“查看更多”复用
const journeyDrillDownParams = ref<any>({})

// 渠道数据趋势的数据类型
const channelTrendDataType = ref<MentionNegativeRateType>(DEFAULT_MENTION_NEGATIVE_RATE_TYPE)

// 观点诉求分析数据
const intentionOpinionData = ref<IntentionOpinionTopVo[]>([])
// 声音详情弹框状态
const voiceDialogVisible = ref(false)
const voiceDialogVoice = ref<{ id: string | number; originalId?: string | number } | null>(null)

// ReportSummary 是否已初始化
const reportSummaryInitialized = ref(false)

/**
 * 获取产品简报数据
 */
const fetchProductBrief = async () => {
  try {
    const response = await getProductBrief(searchParams.value)
    if (response.success) {
      productBriefData.value = response.result
    }
  } catch (error) {
    console.error('获取产品简报数据失败:', error)
  }
}

/**
 * 获取数据趋势变化数据
 */
const fetchDataTrendChange = async () => {
  try {
    const response = await getDataTrendChange(searchParams.value)
    if (response.success) {
      dataTrendChangeData.value = response.result
    }
  } catch (error) {
    console.error('获取数据趋势变化数据失败:', error)
  }
}

/**
 * 获取关注场景TOP数据
 */
const fetchFocusSceneTop = async () => {
  try {
    const response = await getFocusSceneTop(searchParams.value)
    if (response.success) {
      focusSceneTopData.value = response.result
    }
  } catch (error) {
    console.error('获取关注场景TOP数据失败:', error)
  }
}

/**
 * 获取渠道数据排行数据
 */
const fetchDataSourceAnalysis = async () => {
  try {
    const response = await getDataSourceAnalysis(searchParams.value)
    if (response.success) {
      dataSourceAnalysisData.value = response.result
    }
  } catch (error) {
    console.error('获取渠道数据排行数据失败:', error)
  }
}

/**
 * 获取渠道数据趋势数据
 */
const fetchChannelNegativeTrend = async () => {
  try {
    const params = {
      ...searchParams.value,
      dataType: channelTrendDataType.value
    }
    const response = await getChannelNegativeTrend(params)
    if (response.success) {
      channelNegativeTrendData.value = response.result
    }
  } catch (error) {
    console.error('获取渠道数据趋势数据失败:', error)
  }
}

/**
 * 获取渠道提及量占比数据
 */
const fetchChannelMentionShare = async () => {
  try {
    const response = await getChannelMentionShare(searchParams.value)
    if (response.success) {
      channelMentionShareData.value = response.result
    }
  } catch (error) {
    console.error('获取渠道提及量占比数据失败:', error)
  }
}

/**
 * 获取年龄段数据
 */
const fetchAgeDistribution = async () => {
  try {
    const response = await getAgeDistribution(searchParams.value)
    if (response.success) {
      ageDistributionData.value = response.result
    }
  } catch (error) {
    console.error('获取年龄段数据失败:', error)
  }
}

/**
 * 获取常驻地数据
 */
const fetchRegionDistribution = async () => {
  try {
    const response = await getRegionDistribution(searchParams.value)
    if (response.success) {
      regionDistributionData.value = response.result
    }
  } catch (error) {
    console.error('获取常驻地数据失败:', error)
  }
}

/**
 * 获取用户性别数据
 */
const fetchGenderDistribution = async () => {
  try {
    const response = await getGenderDistribution(searchParams.value)
    if (response.success) {
      genderData.value = response.result
    }
  } catch (error) {
    console.error('获取用户性别数据失败:', error)
  }
}

/**
 * 获取用户类型数据
 */
const fetchUserTypeDistribution = async () => {
  try {
    const response = await getUserTypeDistribution(searchParams.value)
    if (response.success) {
      userTypeData.value = response.result
    }
  } catch (error) {
    console.error('获取用户类型数据失败:', error)
  }
}

/**
 * 获取用户关注场景TOP数据
 */
const fetchUserFocusSceneTop = async () => {
  try {
    const response = await getUserFocusSceneTop(searchParams.value)
    if (response.success) {
      userFocusSceneTopData.value = response.result
    }
  } catch (error) {
    console.error('获取用户关注场景TOP数据失败:', error)
  }
}

/**
 * 获取发声用户TOP数据
 */
const fetchVoiceUserTop = async () => {
  try {
    const response = await getVoiceUserTop(searchParams.value)
    if (response.success) {
      voiceUserTopData.value = response.result
    }
  } catch (error) {
    console.error('获取发声用户TOP数据失败:', error)
  }
}

/**
 * 获取旅程细化分析数据
 */
const fetchJourneyDetailAnalysis = async (tags: any = {}) => {
  try {
    const params = {
      ...searchParams.value,
      dataType: journeyDataType.value,
      ...tags
    }
    const response = await getJourneyDetailAnalysis(params)
    if (response.success) {
      journeyDetailAnalysisData.value = response.result
    }
  } catch (error) {
    console.error('获取旅程细化分析数据失败:', error)
  }
}

/**
 * 处理数据类型切换
 */
const debouncedJourneyDataTypeChange = debounce(
  async (dataType: MentionNegativeRateType, data: any) => {
    showLoading({
      background: 'rgba(255, 255, 255, 0.7)'
    })
    try {
      journeyDataType.value = dataType
      await fetchJourneyDetailAnalysis(data)
    } finally {
      hideLoading()
    }
  },
  300
)

const handleJourneyDataTypeChange = async (dataType: MentionNegativeRateType, data: any) => {
  debouncedJourneyDataTypeChange(dataType, data)
}

/**
 * 获取飙升场景TOP数据
 */
const fetchSurgingSceneTop = async (params?: any) => {
  try {
    const _params = {
      ...searchParams.value,
      ...params
    }
    const response = await getSurgingSceneTop(_params)
    if (response.success) {
      surgingSceneTopData.value = response.result
    }
  } catch (error) {
    console.error('获取飙升场景TOP数据失败:', error)
  }
}

/**
 * 获取高频场景TOP数据
 */
const fetchHighFreqSceneTop = async (params?: any) => {
  try {
    const _params = {
      ...searchParams.value,
      ...params
    }
    const response = await getHighFreqSceneTop(_params)
    if (response.success) {
      highFreqSceneTopData.value = response.result
    }
  } catch (error) {
    console.error('获取高频场景TOP数据失败:', error)
  }
}

/**
 * 获取意图观点TOP数据
 */
const fetchIntentionOpinionTop = async () => {
  try {
    const intentions = ['抱怨', '咨询', '建议', '表扬']

    const promises = intentions.map(intention =>
      getUserIntentionOpinionTop({
        ...searchParams.value,
        intention
      })
    )

    const responses = await Promise.all(promises)

    const results = responses.map(response => {
      if (response.success) {
        return response.result
      } else {
        return {
          originalSound: {},
          opinionTops: []
        }
      }
    })

    intentionOpinionData.value = results
  } catch (error) {
    console.error('获取意图观点TOP数据失败:', error)
  }
}

/**
 * 处理关注场景TOP排序变化
 * @param sortData 排序数据对象
 */
const handleSceneTopSort = async ({ prop, order }: { prop: string; order: string }) => {
  try {
    const sortOrder = order as 'asc' | 'desc'
    const params = {
      ...searchParams.value,
      sortField: prop,
      sortOrder
    }
    const response = await getFocusSceneTop(params)

    if (response.success) {
      focusSceneTopData.value = response.result
    }
  } catch (error) {
    console.error('获取关注场景TOP排序数据失败:', error)
  }
}

/**
 * 处理渠道数据趋势切换事件
 * @param dataType 数据类型：MentionNegativeRateType
 */
const handleChannelTrendDataTypeChange = async (dataType: MentionNegativeRateType) => {
  channelTrendDataType.value = dataType
  await fetchChannelNegativeTrend()
}

/**
 * 处理意图观点TOP排序变化
 */
const handleIntentionOpinionSort = async ({
  intention,
  prop,
  order
}: {
  intention: string
  prop: string
  order: string
}) => {
  try {
    const params = {
      ...searchParams.value,
      intention,
      sortField: prop,
      sortOrder: order
    }
    const response = await getUserIntentionOpinionTop(params)

    if (response.success) {
      // 找到对应的意图索引
      const intentions = ['抱怨', '咨询', '建议', '表扬']
      const index = intentions.indexOf(intention)

      if (index !== -1) {
        // 更新对应的数据
        const newData = [...intentionOpinionData.value]
        newData[index] = response.result
        intentionOpinionData.value = newData
      }
    }
  } catch (error) {
    console.error('获取意图观点TOP排序数据失败:', error)
  }
}

/**
 * 处理观点点击事件
 */
const handleOpinionClick = ({
  intention,
  type,
  data
}: {
  intention: string
  type: 'table' | 'original'
  data: any
}) => {
  console.log('观点点击数据:', { intention, type, data })
  if (type === 'original') {
    if (data.originalId) {
      voiceDialogVoice.value = { id: data.id, originalId: data.originalId || '' }
      voiceDialogVisible.value = true
    }
  } else {
    // 表格行点击 / 查看更多
    const isViewMore = data?.__viewMore || !data?.opinion
    if (isViewMore) {
      ddStore.openDD(
        { intention, searchLabelLevel: 3 },
        { subTitle: data?.tableTitle || intention, activeTab: DrillTabKey.VIEWPOINT }
      )
    } else {
      ddStore.openDD({ intention, topic: data.opinion }, { subTitle: data.opinion }, [
        { text: intention, value: { intention } },
        { text: data.opinion, value: { topic: data.opinion } }
      ])
    }
  }
}

/**
 * 处理数据来源分析图表点击事件
 */
const handleDataSourceChartClick = (data: any) => {
  console.log('数据来源分析图表点击数据:', data)
  if (data.type === 'proportion') {
    ddStore.openDD(
      {
        channelCode: data.data.channelCode
      },
      {
        subTitle: data.data.channelName
      },
      [{ text: data.data.channelName, value: { channelCode: data.data.channelCode } }]
    )
  } else if (data.type === 'trend') {
    ddStore.openDD(
      {
        channelCode: data.data.chDatas[0]?.channelCode,
        ...(data.data.date && getDateRange(data.data.date))
      },
      {
        subTitle: data.data.chDatas[0]?.channelName
      },
      [
        {
          text: data.data.chDatas[0]?.channelName,
          value: { channelCode: data.data.chDatas[0]?.channelCode }
        }
      ]
    )
  }
}

/**
 * 处理数据来源分析单元格点击事件
 */
const handleDataSourceCellClick = (data: any) => {
  console.log('数据来源分析单元格点击数据:', data)
  ddStore.openDD(
    {
      channelCode: data.channelCode
    },
    {
      subTitle: data.channelName
    },
    [{ text: data.channelName, value: { channelCode: data.channelCode } }]
  )
}

/**
 * 处理场景行点击事件
 */
const handleSceneRowClick = (data: any) => {
  console.log('场景行点击数据:', data)
  // 在这里处理点击数据，比如跳转到详情页或打开弹窗
  ddStore.openDD(
    {
      tag3Code: data.tag3Code
    },
    {
      subTitle: data.scene,
      drillScene: DrillTabKey.SCENARIO
    },
    [{ text: data.scene, value: { tag3Code: data.tag3Code } }]
  )
}

/**
 * 处理关注场景TOP“查看更多”点击事件
 */
const handleSceneViewMore = () => {
  console.log('关注场景TOP查看更多点击')
  ddStore.openDD(
    { searchLabelLevel: 3 },
    {
      subTitle: '关注场景TOP',
      activeTab: DrillTabKey.INDICATOR
    }
  )
}

/**
 * 处理卡片点击事件
 */
const handleCardChange = (data: any) => {
  console.log('卡片点击数据:', data)
  // 取 SCHeader 的 subtitle（旅程分析下为 tagTitle），以及当前品牌名称，作为副标题
  const subFromHeader = (generalScenarioStore?.journeyPageHeadTag as any)?.key || '全旅程'
  // const curBrandCode = (queryStore.currentQueryParams as any)?.brandCode
  const curBrandCode = (searchParams.value as any)?.brandCode
  const brandItem = (userStore.getBrandService || []).find((b: any) => b.key === curBrandCode)
  const brandName = brandItem?.value || ''
  const subTitle = brandName ? `${subFromHeader} - ${brandName}` : subFromHeader
  // 打开下钻：接口参数保持独立，副标题走 viewParams
  ddStore.openDD({}, { subTitle })
}

/**
 * 处理图表点击事件
 */
const handleChartClick = (data: any) => {
  console.log('图表点击数据:', data)
  // 在这里处理点击数据，比如跳转到详情页或打开弹窗
  ddStore.openDD(
    {
      ...(data.data.date && getDateRange(data.data.date))
    },
    {
      subTitle: data.data.date
    }
  )
}

/**
 * 处理人群特征分析图表点击事件
 */
const handlePopulationChartClick = (data: any) => {
  console.log('人群特征分析图表点击数据:', data)
  if (data.chartType === 'age') {
    ddStore.openDD(
      {
        ageCode: data.age ? [data.age] : undefined
      },
      {
        subTitle: data.name
      },
      [{ text: data.name, value: { ageCode: data.age ? [data.age] : undefined } }]
    )
  } else if (data.chartType === 'region') {
    ddStore.openDD(
      {
        custProvinceCodeSet: data.provinceCode ? [data.provinceCode] : undefined
      },
      {
        subTitle: data.name
      },
      [
        {
          text: data.name,
          value: { custProvinceCodeSet: data.provinceCode ? [data.provinceCode] : undefined }
        }
      ]
    )
  }
}

/**
 * 处理性别点击事件
 */
const handleGenderClick = (data: GenderDistributionVo) => {
  console.log('性别点击数据:', data)
  ddStore.openDD(
    {
      gender: data.gender
    },
    {
      subTitle: data.gender
    },
    [{ text: data.gender, value: { gender: data.gender } }]
  )
}

/**
 * 处理用户类型点击事件
 */
const handleUserTypeClick = (data: UserTypeDistributionVo) => {
  console.log('用户类型点击数据:', data)
  ddStore.openDD(
    {
      custType: data.userType
    },
    {
      subTitle: data.userType
    },
    [{ text: data.userType, value: { custType: data.userType } }]
  )
}

/**
 * 处理关注场景标签点击事件
 */
const handleTagClick = (data: UserFocusSceneTopVo) => {
  console.log('关注场景标签点击数据:', data)
  ddStore.openDD(
    {
      usageScenarioSecond: data.sceneName
    },
    {
      subTitle: data.sceneName,
      drillScene: DrillTabKey.SCENARIO
    },
    [{ text: data.sceneName, value: { usageScenarioSecond: data.sceneName } }]
  )
}

/**
 * 处理发声用户点击事件
 */
const handleVoiceUserClick = (data: VoiceUserTopVo) => {
  console.log('发声用户点击数据:', data)
  // ddStore.openDD()
  ddStore.showUserDetail(
    {
      userId: data.userId,
      queryParams: {
        ...searchParams.value,
        ...ddStore.setDefaultDDParamsByRouteName()
      }
    },
    'close' // 新增：仅显示“关闭”
  )
}

/**
 * 处理旅程分析图表点击事件
 */
const handleJourneyChartClick = async (data: JourneyDetailAnalysisVo) => {
  console.log('旅程分析图表点击数据:--2', data)
  // 同步缓存图表下钻参数，保障“查看更多”沿用当前旅程层级
  journeyDrillDownParams.value = { ...(data || {}) }

  await Promise.all([
    fetchJourneyDetailAnalysis(data),
    fetchSurgingSceneTop(data),
    fetchHighFreqSceneTop(data)
  ])
}

/**
 * 处理旅程场景点击事件
 */
const handleJourneySceneClick = (data: any) => {
  console.log('旅程场景点击数据:', data)

  // “查看更多”场景不带具体 tag3Code
  if (data?.__viewMore) {
    let surParams: any = {}
    if (data.source === 'surging') {
      surParams.sortField = 'mentionsMoM'
      surParams.sortOrder = 'desc'
    }
    ddStore.openDD(
      {
        ...journeyDrillDownParams.value,
        searchLabelLevel: 3,
        ...surParams
      },
      {
        subTitle: data?.sceneName || '场景TOP',
        activeTab: DrillTabKey.INDICATOR
      }
    )
    return
  }

  ddStore.openDD(
    {
      tag3Code: data.sceneCode
    },
    {
      subTitle: data.sceneName,
      drillScene: DrillTabKey.SCENARIO
    },
    [{ text: data.sceneName, value: { tag3Code: data.sceneCode } }]
  )
}

const refreshAllData = () => {
  // 综合分析相关接口
  fetchProductBrief()
  fetchDataTrendChange()
  fetchFocusSceneTop()

  // 数据来源分析相关接口
  fetchDataSourceAnalysis()
  fetchChannelNegativeTrend()
  fetchChannelMentionShare()

  // 人群特征分析
  fetchAgeDistribution()
  fetchRegionDistribution()
  fetchGenderDistribution()
  fetchUserTypeDistribution()
  fetchUserFocusSceneTop()
  fetchVoiceUserTop()

  // 旅程细化分析
  fetchJourneyDetailAnalysis()
  fetchSurgingSceneTop()
  fetchHighFreqSceneTop()

  // 观点诉求分析
  fetchIntentionOpinionTop()
}

// 使用查询监听 hooks，禁用自动刷新，等待 SCHeader 初始化完成
// const { queryStore } = useQueryListener(refreshAllData, ['updateQueryParams'], 300, false)
const ddStore = useGeneralDrillDownStore()

/**
 * SCHeader 初始化完成后的回调
 * 确保品牌等参数已经设置完成后再触发数据加载
 */
const handleHeaderInitComplete = () => {
  // console.log('SCHeader 初始化完成，开始加载数据')
  // console.log('当前品牌参数:', queryStore.currentQueryParams.brandCode)

  // 标记 ReportSummary 已初始化，允许其开始响应参数变化
  reportSummaryInitialized.value = true

  // 直接调用 refreshAllData，不使用节流函数，避免额外的延迟
  refreshAllData()
}

const handleSearch = (
  _formData: any,
  _tagPath?: Array<{ code: string; name: string; level?: number }>
) => {
  console.log('handleSearch', _formData, _tagPath)
  searchParams.value = _formData
  tagPath.value = _tagPath
  journeyDrillDownParams.value = {}

  // 清空关注场景TOP模块中表格的排序状态
  comprehensiveAnalysisRef.value?.clearSceneTopSort()

  // 清空观点诉求分析模块中表格的排序状态
  opinionDemandAnalysisRef.value?.clearAllSort()

  reportSummaryInitialized.value = true

  refreshAllData()
}
</script>

<template>
  <FAnalyseWrap v-model="generalScenarioStore.visible">
    <template #header>
      <!--  @init-complete="handleHeaderInitComplete" -->
      <SCHeader title="旅程分析" subtitle="全旅程"></SCHeader>
    </template>
    <UniversaFilter :routeName="`${route.name as string}`" @search="handleSearch"></UniversaFilter>
    <FCard
      title="综合分析"
      :card-key="CARD_EXPORT_KEYS.journey.Composite"
      tooltip="综合分析"
      :height="'1001px'"
      class="mt-24"
      downloadable
      data-page-export-card="summary"
    >
      <ReportSummary
        :api-function="getJADataTrendChangeResult"
        :query-params="reportSummaryInitialized ? searchParams : {}"
      ></ReportSummary>
      <ComprehensiveAnalysis
        ref="comprehensiveAnalysisRef"
        :product-brief-data="productBriefData"
        :focus-scene-top-data="focusSceneTopData"
        :data-trend-change-data="dataTrendChangeData"
        @scene-top-sort="handleSceneTopSort"
        @scene-row-click="handleSceneRowClick"
        @scene-view-more="handleSceneViewMore"
        @cardChange="handleCardChange"
        @chart-click="handleChartClick"
      ></ComprehensiveAnalysis>
    </FCard>
    <FCard
      title="人群特征分析"
      :card-key="CARD_EXPORT_KEYS.journey.Crowd"
      tooltip="人群特征分析"
      :height="'1001px'"
      class="mt-24"
      downloadable
    >
      <ReportSummary
        :api-function="getFocusSceneTopResult"
        :query-params="reportSummaryInitialized ? searchParams : {}"
      ></ReportSummary>
      <PopulationFeatureAnalysis
        :ageDistributionData="ageDistributionData"
        :regionDistributionData="regionDistributionData"
        :genderData="genderData"
        :userTypeData="userTypeData"
        :userFocusSceneTopData="userFocusSceneTopData"
        :voiceUserTopData="voiceUserTopData"
        @chart-click="handlePopulationChartClick"
        @gender-click="handleGenderClick"
        @user-type-click="handleUserTypeClick"
        @tag-click="handleTagClick"
        @user-click="handleVoiceUserClick"
      ></PopulationFeatureAnalysis>
    </FCard>

    <FCard
      title="旅程细化分析"
      :card-key="CARD_EXPORT_KEYS.journey.Detail"
      tooltip="服务分析"
      :height="'1001px'"
      class="mt-24"
      downloadable
    >
      <ReportSummary
        :api-function="getJourneyDetailAnalysisResult"
        :query-params="reportSummaryInitialized ? searchParams : {}"
      ></ReportSummary>
      <JourneyDetailed
        :journeyDetailAnalysisData="journeyDetailAnalysisData"
        :surgingSceneTopData="surgingSceneTopData"
        :highFreqSceneTopData="highFreqSceneTopData"
        :dataType="journeyDataType"
        :tag-path="tagPath"
        @data-type-change="handleJourneyDataTypeChange"
        @chart-click="handleJourneyChartClick"
        @scene-click="handleJourneySceneClick"
      ></JourneyDetailed>
    </FCard>
    <FCard
      title="观点诉求分析"
      :card-key="CARD_EXPORT_KEYS.journey.Opinion"
      tooltip="产品分析"
      :height="'1021px'"
      class="mt-24"
      downloadable
    >
      <ReportSummary
        :api-function="getUserIntentionOpinionTopResult"
        :query-params="reportSummaryInitialized ? searchParams : {}"
      ></ReportSummary>
      <OpinionDemandAnalysis
        ref="opinionDemandAnalysisRef"
        :intention-opinion-data="intentionOpinionData"
        @sort-change="handleIntentionOpinionSort"
        @opinion-click="handleOpinionClick"
      ></OpinionDemandAnalysis>
    </FCard>

    <FCard
      title="数据来源分析"
      :card-key="CARD_EXPORT_KEYS.journey.DataSource"
      tooltip="数据来源分析"
      :height="'1101px'"
      class="mt-24"
      downloadable
    >
      <ReportSummary
        :api-function="getJAChannelNegativeTrendResult"
        :query-params="reportSummaryInitialized ? searchParams : {}"
      ></ReportSummary>
      <DataSourceAnalysis
        :channel-mention-share-data="channelMentionShareData"
        :channel-negative-trend-data="channelNegativeTrendData"
        :data-source-analysis-data="dataSourceAnalysisData"
        @data-type-change="handleChannelTrendDataTypeChange"
        @chart-click="handleDataSourceChartClick"
        @cell-click="handleDataSourceCellClick"
      ></DataSourceAnalysis>
    </FCard>
    <!-- 声音详情弹框 -->
    <VoiceDetailsDialog v-model:visible="voiceDialogVisible" :voice="voiceDialogVoice" />
  </FAnalyseWrap>
</template>

<style lang="scss" scoped></style>
