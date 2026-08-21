<script setup lang="ts">
import { ref } from 'vue'
import { debounce } from 'lodash-es'
import { useGeneralScenarioStore, useUserStore } from '@/store'
import {
  getProductBrief,
  getUserIntentionOpinionTop,
  getFocusSceneTop,
  getDataTrendChange,
  getFocusSceneAnalysis,
  getChannelMentionShare,
  getChannelNegativeTrend,
  getDataSourceAnalysis,
  getProvinceRank,
  getProvinceMap,
  getDealerRankTop
} from '@/api/serviceAnalysis'
import type {
  ProductBriefVo as ServiceProductBriefVo,
  IntentionOpinionTopVo as ServiceIntentionOpinionTopVo,
  SceneTopVo as ServiceSceneTopVo,
  ProductTrendVo as ServiceTrendVo,
  ChannelShareVo as ServiceChannelMentionShareVo,
  ChannelNegativeTrendVo as ServiceChannelNegativeTrendVo,
  DataSourceAnalysisVo as ServiceDataSourceAnalysisVo,
  ServiceProvinceRankVo,
  ServiceDealerRankVo as ServiceDealerRankTopVo
} from '@/api/serviceAnalysis/types'
import SCHeader from '@/components/Business/Scene/Common/SCHeader/index.vue'
import ReportSummary from '@/components/Business/Scene/Common/ReportSummary.vue'
import ComprehensiveAnalysis from '@/components/Business/Scene/Common/ComprehensiveAnalysis/index.vue'
import FocusOnSceneDistribution from '@/components/Business/Scene/Common/FocusOnSceneDistribution/index.vue'
import DataSourceAnalysis from '@/components/Business/Scene/Common/DataSourceAnalysis/index.vue'
import RegionalAnalysis from './RegionalAnalysis/index.vue'
import { useQueryListener } from '@/hooks/useQueryListener'
import { useLoading } from '@/hooks/useLoading'
import useGeneralDrillDownStore from '@/store/modules/generalDrillDown'
import {
  getProvinceRankResult,
  getSAFocusSceneAnalysisResult,
  getSADataTrendChangeResult,
  getSAChannelNegativeTrendResult
} from '@/api/reportSummary/index'
import { getDateRange } from '@/utils/date'
import { DrillTabKey } from '@components/Business/DrillDownDialog/constants.ts'
import UniversaFilter from '@/components/Business/UniversaFilter/index.vue'
import { useRoute } from 'vue-router'
import {
  DEFAULT_MENTION_NEGATIVE_RATE_TYPE,
  ServiceFilterTagCode,
  ServiceFilterTagName
} from '@/constants'
import { usePageCardDownload } from '@/hooks/usePageCardDownload'
import { getCardStatExportRequest, serviceStatExportMap } from '@/api/downloadTask'
import { CARD_EXPORT_KEYS } from '@/constants/cardExportKeys'

defineOptions({
  name: 'ServiceAnalysis'
})

const generalScenarioStore = useGeneralScenarioStore()
const route = useRoute()
const { showLoading, hideLoading } = useLoading()

const searchParams = ref<any>({})
usePageCardDownload({
  getParams: () => ({ ...searchParams.value }),
  getStatRequest: payload => getCardStatExportRequest(serviceStatExportMap, payload)
})
const tagPath = ref<Array<{ code: string; name: string; level?: number }> | undefined>(undefined)
const fixedRootTag = {
  code: ServiceFilterTagCode,
  name: ServiceFilterTagName,
  level: 1
}

// ComprehensiveAnalysis 组件引用
const comprehensiveAnalysisRef = ref<InstanceType<typeof ComprehensiveAnalysis>>()

// 响应式数据
const productBriefData = ref<ServiceProductBriefVo | null>(null)
const focusSceneTopData = ref<ServiceSceneTopVo[]>([])
const dataTrendChangeData = ref<ServiceTrendVo | null>(null)
const focusSceneAnalysisData = ref<any>(null)

// 场景分析数据类型
const sceneDataType = ref('negativeRate')
const intentionOpinionTopData = ref<{
  complaint: ServiceIntentionOpinionTopVo[]
  consultation: ServiceIntentionOpinionTopVo[]
  suggestion: ServiceIntentionOpinionTopVo[]
  praise: ServiceIntentionOpinionTopVo[]
}>({
  complaint: [],
  consultation: [],
  suggestion: [],
  praise: []
})
const channelMentionShareData = ref<ServiceChannelMentionShareVo[]>([])
const channelNegativeTrendData = ref<ServiceChannelNegativeTrendVo[]>([])
const dataSourceAnalysisData = ref<ServiceDataSourceAnalysisVo[]>([])
const provinceRankData = ref<ServiceProvinceRankVo[]>([])
const provinceMapData = ref<ServiceProvinceRankVo[]>([])
const dealerRankTopData = ref<ServiceDealerRankTopVo[]>([])
const selectedProvinceCode = ref<string>('')

// ReportSummary 是否已初始化
const reportSummaryInitialized = ref(false)

// 缓存关注场景图表点击的下钻参数
const cachedDrillDownParams = ref<any>({})

/**
 * 获取综合分析简报数据
 */
const fetchProductBrief = async () => {
  try {
    const response = await getProductBrief(searchParams.value)
    if (response.success) {
      productBriefData.value = response.result
    }
  } catch (error) {
    console.error('获取综合分析简报数据失败:', error)
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

const debouncedSceneDataTypeChange = debounce(async (dataType: string, data: any) => {
  showLoading({
    background: 'rgba(255, 255, 255, 0.7)'
  })
  try {
    sceneDataType.value = dataType
    await fetchFocusSceneAnalysis(data)
  } finally {
    hideLoading()
  }
}, 300)

// 场景数据类型切换
const sceneDataTypeChange = (dataType: string = 'negativeRate', data: any) => {
  debouncedSceneDataTypeChange(dataType, data)
}

/**
 * 获取关注场景分析数据
 * @param queryParams 查询参数，包含下钻数据
 */
const fetchFocusSceneAnalysis = async (queryParams: any = {}) => {
  try {
    const params = {
      ...searchParams.value,
      dataType: sceneDataType.value,
      ...queryParams
    }
    const response = await getFocusSceneAnalysis(params)
    if (response.success) {
      // 传递完整的接口返回数据，而不仅仅是tagData
      focusSceneAnalysisData.value = response.result
    }
  } catch (error) {
    console.error('获取关注场景分析数据失败:', error)
  }
}

/**
 * 获取用户意图观点TOP数据
 * 需要调用4次接口，分别获取抱怨、咨询、建议、表扬的TOP数据
 * @param params 额外的查询参数，包含下钻数据
 */
const fetchUserIntentionOpinionTop = async (params: any = {}) => {
  const intentions = [
    { key: 'complaint', value: '抱怨' },
    { key: 'consultation', value: '咨询' },
    { key: 'suggestion', value: '建议' },
    { key: 'praise', value: '表扬' }
  ]

  try {
    // 并行调用4个接口
    const promises = intentions.map(intention =>
      getUserIntentionOpinionTop({
        ...searchParams.value,
        intention: intention.value,
        ...params
      })
    )

    const responses = await Promise.all(promises)

    // 处理响应数据
    responses.forEach((response, index) => {
      if (response.success) {
        const intentionKey = intentions[index].key as keyof typeof intentionOpinionTopData.value
        intentionOpinionTopData.value[intentionKey] = response.result
      }
    })
  } catch (error) {
    console.error('获取用户意图观点TOP数据失败:', error)
  }
}

/**
 * 处理用户观点TOP表格排序变化
 * @param sortData 排序数据对象
 */
const handleIntentionTopSort = async ({
  intention,
  prop,
  order
}: {
  intention: string
  prop: string
  order: string
}) => {
  try {
    const response = await getUserIntentionOpinionTop({
      ...searchParams.value,
      intention,
      sortField: prop,
      sortOrder: order,
      ...cachedDrillDownParams.value
    })

    if (response.success) {
      // 根据意图类型更新对应的数据
      const intentionMap: Record<string, keyof typeof intentionOpinionTopData.value> = {
        抱怨: 'complaint',
        咨询: 'consultation',
        建议: 'suggestion',
        表扬: 'praise'
      }

      const key = intentionMap[intention]
      if (key) {
        intentionOpinionTopData.value[key] = response.result
      }
    }
  } catch (error) {
    console.error(`获取${intention}观点TOP排序数据失败:`, error)
  }
}

/**
 * 处理关注场景TOP排序变化
 * @param sortData 排序数据对象
 */
const handleSceneTopSort = async ({ prop, order }: { prop: string; order: string }) => {
  try {
    const response = await getFocusSceneTop({
      ...searchParams.value,
      sortField: prop,
      sortOrder: order
    })

    if (response.success) {
      focusSceneTopData.value = response.result
    }
  } catch (error) {
    console.error('获取关注场景TOP排序数据失败:', error)
  }
}

/**
 * 获取省份排行数据
 * @param sortField 排序字段 (可选)
 * @param sortOrder 排序方式 (可选)
 */
const fetchProvinceRank = async (sortField?: string, sortOrder?: string) => {
  try {
    const params: any = {
      ...searchParams.value
    }

    // 如果有排序参数，添加到请求中
    if (sortField && sortOrder) {
      params.sortField = sortField
      params.sortOrder = sortOrder
    }

    const response = await getProvinceRank(params)
    if (response.success) {
      provinceRankData.value = response.result
    }
  } catch (error) {
    console.error('获取省份排行数据失败:', error)
  }
}

/**
 * 获取省份地图数据
 */
const fetchProvinceMap = async () => {
  try {
    const response = await getProvinceMap(searchParams.value)
    if (response.success) {
      provinceMapData.value = response.result
    }
  } catch (error) {
    console.error('获取省份地图数据失败:', error)
  }
}

/**
 * 获取经销商评价TOP数据
 * @param provinceCodes 省份编码数组 (可选)
 * @param sortField 排序字段 (可选)
 * @param sortOrder 排序方式 (可选)
 */
const fetchDealerRankTop = async (
  provinceCodes?: string[],
  sortField?: string,
  sortOrder?: string
) => {
  try {
    const params: any = {
      ...searchParams.value
    }

    // 如果有省份编码，添加到请求中
    if (provinceCodes && provinceCodes.length > 0) {
      params.provinceCodeSet = provinceCodes
    }

    // 如果有排序参数，添加到请求中
    if (sortField && sortOrder) {
      params.sortField = sortField
      params.sortOrder = sortOrder
    }

    const response = await getDealerRankTop(params)
    if (response.success) {
      dealerRankTopData.value = response.result
    }
  } catch (error) {
    console.error('获取经销商评价TOP数据失败:', error)
  }
}

/**
 * 处理省份排行表格排序变化
 * @param sortData 排序数据对象
 */
const handleProvinceRankSort = async ({ prop, order }: { prop: string; order: string }) => {
  await fetchProvinceRank(prop, order)
}

/**
 * 处理经销商评价TOP表格排序变化
 * @param sortData 排序数据对象
 */
const handleDealerRankTopSort = async ({ prop, order }: { prop: string; order: string }) => {
  const provinceCodes = selectedProvinceCode.value ? [selectedProvinceCode.value] : undefined
  await fetchDealerRankTop(provinceCodes, prop, order)
}

/**
 * 处理省份选择变化
 * @param provinceCode 省份编码
 */
const handleProvinceSelect = async (provinceCode: string) => {
  selectedProvinceCode.value = provinceCode
  // 根据选中的省份获取经销商评价TOP数据
  await fetchDealerRankTop([provinceCode])
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
 * 获取渠道数据趋势数据
 * @param dataType 数据类型：MentionNegativeRateType
 */
const fetchChannelNegativeTrend = async (
  dataType: MentionNegativeRateType = DEFAULT_MENTION_NEGATIVE_RATE_TYPE
) => {
  try {
    const params = {
      ...searchParams.value,
      dataType
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
 * 获取数据来源分析数据
 */
const fetchDataSourceAnalysis = async () => {
  try {
    const response = await getDataSourceAnalysis(searchParams.value)
    if (response.success) {
      dataSourceAnalysisData.value = response.result
    }
  } catch (error) {
    console.error('获取数据来源分析数据失败:', error)
  }
}

/**
 * 处理渠道数据趋势切换事件
 * @param dataType 数据类型：MentionNegativeRateType
 */
const handleChannelTrendDataTypeChange = async (dataType: MentionNegativeRateType) => {
  await fetchChannelNegativeTrend(dataType)
}

/**
 * 处理场景行点击事件
 */
const handleSceneRowClick = (data: any) => {
  console.log('场景行点击数据:', data)
  // 在这里处理点击数据，比如跳转到详情页或打开弹窗
  ddStore.openDD(
    {
      tag4Code: data.tag4Code
    },
    {
      subTitle: data.scene,
      drillScene: DrillTabKey.SCENARIO
    },
    [{ text: data.scene, value: { tag4Code: data.tag4Code } }]
  )
}

/**
 * 处理卡片点击事件
 */
const handleCardChange = (data: any) => {
  console.log('卡片点击数据:', data)
  // 在这里处理点击数据，比如跳转到详情页或打开弹窗
  // 副标题取自 SCHeader 的 subtitle（服务分析为标签名）
  const subFromHeader = (generalScenarioStore?.journeyPageHeadTag as any)?.key || '全服务'
  const curBrandCode = (searchParams.value as any)?.brandCode
  const brandItem = (userStore.getBrandService || []).find((b: any) => b.key === curBrandCode)
  const brandName = brandItem?.value || ''
  const subTitle = brandName ? `${subFromHeader} - ${brandName}` : subFromHeader
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
 * 处理关注场景分析图表点击事件
 */
const handleFocusSceneChartClick = async (data: any) => {
  console.log('关注场景分析图表点击事件:data', data)

  // 缓存下钻参数
  cachedDrillDownParams.value = data

  // 清空观点TOP的排序状态
  focusSceneDistributionRef.value?.clearAllSort()

  await Promise.all([fetchFocusSceneAnalysis(data), fetchUserIntentionOpinionTop(data)])
}

/**
 * 处理关注场景分析表格行点击事件
 */
const handleFocusSceneTableRowClick = ({ intention, data }: { intention: string; data: any }) => {
  console.log('关注场景分析表格行点击数据:', { intention, data })
  const isViewMore = data?.__viewMore || !data?.opinion

  if (isViewMore) {
    ddStore.openDD(
      { intention, ...cachedDrillDownParams.value },
      { subTitle: data?.tableTitle || intention, activeTab: DrillTabKey.VIEWPOINT }
      // [{ text: intention, value: { intention } }]
    )
  } else {
    ddStore.openDD(
      { ...cachedDrillDownParams.value, intention, topic: data.opinion },
      { subTitle: data.opinion },
      [
        { text: intention, value: { intention } },
        { text: data.opinion, value: { topic: data.opinion } }
      ]
    )
  }
}

/**
 * 处理关注场景TOP“查看更多”点击事件
 */
const handleSceneViewMore = () => {
  console.log('关注场景TOP查看更多点击')
  ddStore.openDD(
    { searchLabelLevel: 4 },
    {
      subTitle: '关注场景TOP',
      activeTab: DrillTabKey.INDICATOR
    }
  )
}

/**
 * 处理地图点击事件
 */
const handleMapClick = (data: any) => {
  console.log('地图点击数据:', data)
  ddStore.openDD(
    {
      provinceCodeSet: data.data.provinceCode ? [data.data.provinceCode] : undefined
    },
    {
      subTitle: data.data.provinceName
    },
    [
      {
        text: data.data.provinceName,
        value: { provinceCodeSet: data.data.provinceCode ? [data.data.provinceCode] : undefined }
      }
    ]
  )
}

const refreshAllData = () => {
  fetchProductBrief()
  fetchFocusSceneTop()
  fetchDataTrendChange()
  fetchFocusSceneAnalysis()
  fetchUserIntentionOpinionTop()
  // 数据来源分析相关接口
  fetchChannelMentionShare()
  fetchChannelNegativeTrend()
  fetchDataSourceAnalysis()
  // 地域分析相关接口
  fetchProvinceRank()
  fetchProvinceMap()
  fetchDealerRankTop()
}

// 使用查询监听 hooks，禁用自动刷新，等待 SCHeader 初始化完成
// eslint-disable-next-line @typescript-eslint/no-unused-vars
const { queryStore } = useQueryListener(refreshAllData, ['updateQueryParams'], 300, false)
const ddStore = useGeneralDrillDownStore()
const userStore = useUserStore()

// 关注场景分析组件引用
const focusSceneDistributionRef = ref<any>(null)

// 区域分布组件引用
const regionalAnalysisRef = ref<InstanceType<typeof RegionalAnalysis>>()

/**
 * SCHeader 初始化完成后的回调
 * 确保品牌等参数已经设置完成后再触发数据加载
 */
// eslint-disable-next-line @typescript-eslint/no-unused-vars
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
  cachedDrillDownParams.value = {}

  // 清空关注场景TOP模块中表格的排序状态
  comprehensiveAnalysisRef.value?.clearSceneTopSort()

  // 清空关注场景分析模块中表格的排序状态
  focusSceneDistributionRef.value?.clearAllSort()

  // 清空区域分布模块中表格的排序状态
  regionalAnalysisRef.value?.clearAllSort()

  // 标记 ReportSummary 已初始化，允许其开始响应参数变化
  reportSummaryInitialized.value = true

  refreshAllData()
}
</script>

<template>
  <FAnalyseWrap v-model="generalScenarioStore.visible">
    <template #header>
      <!-- @init-complete="handleHeaderInitComplete" -->
      <SCHeader title="服务分析" subtitle="全服务"></SCHeader>
    </template>
    <UniversaFilter :routeName="`${route.name as string}`" @search="handleSearch"></UniversaFilter>
    <FCard
      title="综合分析"
      :card-key="CARD_EXPORT_KEYS.service.Composite"
      tooltip="综合分析"
      :height="'1001px'"
      class="mt-24"
      downloadable
      data-page-export-card="summary"
    >
      <ReportSummary
        :api-function="getSADataTrendChangeResult"
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
      title="关注场景分析"
      :card-key="CARD_EXPORT_KEYS.service.FocusScene"
      tooltip="服务多维分析"
      :height="'1001px'"
      class="mt-24"
      downloadable
    >
      <ReportSummary
        :api-function="getSAFocusSceneAnalysisResult"
        :query-params="reportSummaryInitialized ? searchParams : {}"
      ></ReportSummary>
      <FocusOnSceneDistribution
        ref="focusSceneDistributionRef"
        :focus-scene-analysis-data="focusSceneAnalysisData"
        :intention-opinion-top-data="intentionOpinionTopData"
        :search-params="searchParams"
        :tag-path="tagPath"
        :fixed-root-tag="fixedRootTag"
        @intention-top-sort="handleIntentionTopSort"
        @data-type-change="sceneDataTypeChange"
        @chart-click="handleFocusSceneChartClick"
        @table-row-click="handleFocusSceneTableRowClick"
      ></FocusOnSceneDistribution>
    </FCard>

    <FCard
      title="地域分析"
      :card-key="CARD_EXPORT_KEYS.service.Region"
      tooltip="地域分析"
      :height="'1001px'"
      class="mt-24"
      downloadable
    >
      <ReportSummary
        :api-function="getProvinceRankResult"
        :query-params="reportSummaryInitialized ? searchParams : {}"
      ></ReportSummary>
      <RegionalAnalysis
        ref="regionalAnalysisRef"
        :province-rank-data="provinceRankData"
        :province-map-data="provinceMapData"
        :dealer-rank-top-data="dealerRankTopData"
        :selected-province-code="selectedProvinceCode"
        @province-rank-sort="handleProvinceRankSort"
        @dealer-rank-top-sort="handleDealerRankTopSort"
        @province-select="handleProvinceSelect"
        @map-click="handleMapClick"
      ></RegionalAnalysis>
    </FCard>

    <FCard
      title="数据来源分析"
      :card-key="CARD_EXPORT_KEYS.service.DataSource"
      tooltip="数据来源分析"
      :height="'1101px'"
      class="mt-24"
      downloadable
    >
      <ReportSummary
        :api-function="getSAChannelNegativeTrendResult"
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
  </FAnalyseWrap>
</template>

<style lang="scss" scoped></style>
