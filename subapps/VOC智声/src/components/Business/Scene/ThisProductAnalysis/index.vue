<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useGeneralScenarioStore } from '@/store'
import { useQueryStore } from '@/store/modules/query'
import {
  getProductSelfBrief,
  getFocusSceneTop,
  getDataTrendChange,
  getUserJourneyAnalysis,
  getChannelMentionShare,
  getChannelNegativeTrend,
  getDataSourceAnalysis,
  getServiceTagAnalysis,
  getProductTagAnalysis
} from '@/api/thisProductAnalysis'
import type {
  ProductSelfBriefVo,
  ProductSelfSceneTopVo,
  ProductSelfTrendVo,
  ProductSelfJourneyAnalysisVo,
  ProductSelfChannelMentionShareVo,
  ProductSelfChannelNegativeTrendVo,
  ProductSelfDataSourceAnalysisVo,
  ProductSelfTagAnalysisRowVo
} from '@/api/thisProductAnalysis/types.d'
import SCHeader from '@/components/Business/Scene/Common/SCHeader/index.vue'
import ReportSummary from '@/components/Business/Scene/Common/ReportSummary.vue'
import ComprehensiveAnalysis from '@/components/Business/Scene/Common/ComprehensiveAnalysis/index.vue'
import CPFX from './CPFX/index.vue'
import FWFX from './FWFX/index.vue'
import UserJourneyAnalysis from './UserJourneyAnalysis/index.vue'
import DataSourceAnalysis from '@/components/Business/Scene/Common/DataSourceAnalysis/index.vue'
import { useQueryListener } from '@/hooks/useQueryListener'
import { useUserStore } from '@/store'
import useGeneralDrillDownStore from '@/store/modules/generalDrillDown'
import UniversaFilter from '@/components/Business/UniversaFilter/index.vue'

import {
  getUserJourneyAnalysisResult,
  getServiceTagAnalysisResult,
  getThisProductTagAnalysisResult,
  getDataTrendChangeResult,
  getChannelNegativeTrendResult
} from '@/api/reportSummary/index'
import { getDateRange } from '@/utils/date'
import { DrillTabKey } from '@components/Business/DrillDownDialog/constants.ts'
import { DEFAULT_MENTION_NEGATIVE_RATE_TYPE } from '@/constants'
import { usePageCardDownload } from '@/hooks/usePageCardDownload'
import { getCardStatExportRequest, productSelfStatExportMap } from '@/api/downloadTask'
import { CARD_EXPORT_KEYS } from '@/constants/cardExportKeys'

defineOptions({
  name: 'ThisProductAnalysis'
})

const router = useRouter()
const generalScenarioStore = useGeneralScenarioStore()
const userStore = useUserStore()
const route = useRoute()

const searchParams = ref<any>({})
usePageCardDownload({
  getParams: () => ({ ...searchParams.value }),
  getStatRequest: payload => getCardStatExportRequest(productSelfStatExportMap, payload)
})

// ComprehensiveAnalysis 组件引用
const comprehensiveAnalysisRef = ref<InstanceType<typeof ComprehensiveAnalysis>>()

// 本品分析综合分析数据
const productBriefData = ref<ProductSelfBriefVo | null>(null)
// 关注场景TOP数据
const focusSceneTopData = ref<ProductSelfSceneTopVo[]>([])
// 数据趋势变化数据
const dataTrendChangeData = ref<ProductSelfTrendVo | null>(null)
// 用户旅程分析数据
const userJourneyAnalysisData = ref<ProductSelfJourneyAnalysisVo[]>([])

// 数据来源分析相关数据
// 渠道提及量占比数据
const channelMentionShareData = ref<ProductSelfChannelMentionShareVo[]>([])
// 渠道负面率趋势数据
const channelNegativeTrendData = ref<ProductSelfChannelNegativeTrendVo[]>([])
// 数据来源分析数据
const dataSourceAnalysisData = ref<ProductSelfDataSourceAnalysisVo[]>([])

// 服务分析数据
const serviceTagAnalysisData = ref<ProductSelfTagAnalysisRowVo[]>([])
// 产品分析数据
const productTagAnalysisData = ref<ProductSelfTagAnalysisRowVo[]>([])

// ReportSummary 是否已初始化
const reportSummaryInitialized = ref(false)

/**
 * 获取本品分析综合分析简报数据
 */
const fetchProductSelfBrief = async () => {
  try {
    const response = await getProductSelfBrief(searchParams.value)
    if (response.success) {
      productBriefData.value = response.result
    }
  } catch (error) {
    console.error('获取本品分析综合分析简报数据失败:', error)
  }
}

/**
 * 获取本品分析关注场景TOP数据
 */
const fetchFocusSceneTop = async () => {
  try {
    const response = await getFocusSceneTop(searchParams.value)
    if (response.success) {
      focusSceneTopData.value = response.result
    }
  } catch (error) {
    console.error('获取本品分析关注场景TOP数据失败:', error)
  }
}

/**
 * 获取本品分析数据趋势变化数据
 */
const fetchDataTrendChange = async () => {
  try {
    const response = await getDataTrendChange(searchParams.value)
    if (response.success) {
      dataTrendChangeData.value = response.result
    }
  } catch (error) {
    console.error('获取本品分析数据趋势变化数据失败:', error)
  }
}

/**
 * 获取用户旅程分析数据
 */
const fetchUserJourneyAnalysis = async () => {
  try {
    const response = await getUserJourneyAnalysis(searchParams.value)
    if (response.success) {
      userJourneyAnalysisData.value = response.result
    }
  } catch (error) {
    console.error('获取用户旅程分析数据失败:', error)
  }
}

/**
 * 获取渠道提及量占比数据
 */
const fetchChannelMentionShare = async () => {
  try {
    const response = await getChannelMentionShare(searchParams.value)
    if (response.success) {
      channelMentionShareData.value = response.result || []
      // 排序逻辑已下沉至 DataSourceAnalysis 组件内部，这里保持原始顺序传入
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
 * 获取服务分析数据
 */
const fetchServiceTagAnalysis = async () => {
  try {
    const response = await getServiceTagAnalysis(searchParams.value)
    if (response.success) {
      serviceTagAnalysisData.value = response.result
    }
  } catch (error) {
    console.error('获取服务分析数据失败:', error)
  }
}

/**
 * 获取产品分析数据
 */
const fetchProductTagAnalysis = async () => {
  try {
    const response = await getProductTagAnalysis(searchParams.value)
    if (response.success) {
      productTagAnalysisData.value = response.result
    }
  } catch (error) {
    console.error('获取产品分析数据失败:', error)
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
    console.error('获取本品分析关注场景TOP排序数据失败:', error)
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
 * 处理关注场景TOP“查看更多”点击事件
 */
const handleSceneViewMore = () => {
  console.log('关注场景TOP查看更多点击')
  ddStore.openDD(
    {
      searchLabelLevel: 4
    },
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
  // 取 SCHeader 的 subtitle（本品分析下为品牌/车系名）
  // 优先使用车系，其次品牌；均从用户品牌树中按 code 匹配 label
  // const { brandCode, carSeriesCode } = (queryStore.currentQueryParams as any) || {}
  const { brandCode, carSeriesCode } = (searchParams.value as any) || {}
  const options = userStore.getBrandService || []
  // 在品牌树中递归查找节点
  const findNodeByKey = (nodes: any[], key: string): any => {
    if (!nodes || !key) return null
    for (const node of nodes) {
      if (node.key === key) return node
      if (node.children) {
        const found = findNodeByKey(node.children, key)
        if (found) return found
      }
    }
    return null
  }
  const node = findNodeByKey(options, carSeriesCode || brandCode)
  const subTitle = node?.value || ''
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
 * 处理用户旅程分析点击事件
 */
const handleJourneyClick = (data: any) => {
  console.log('用户旅程分析点击数据:', data)
  if (data.type === 'name') {
    // queryStore.updateQueryParams({
    //   tag1Code: data.journey.journeyCode
    // })

    router.push({
      path: '/scene/journeyAnalysis',
      query: {
        tag1Code: data.journey.journeyCode
      }
    })
  } else if (data.type === 'chart') {
    // queryStore.updateQueryParams({
    //   tag1Code: data.journey.journeyCode
    // })
    router.push({
      path: '/scene/journeyAnalysis',
      query: {
        tag1Code: data?.journey?.data?.journeyCode
      }
    })
  } else if (['negativeRate', 'mentions'].includes(data.type)) {
    router.push({
      path: '/scene/journeyAnalysis',
      query: {
        tag1Code: data?.journey?.journeyCode
      }
    })
  } else {
    router.push('/scene/journeyAnalysis')
  }
}

/**
 * 处理TopRank行点击事件
 */
const handleTopRankRowClick = (data: any) => {
  console.log('TopRank行点击数据:', data)
  ddStore.openDD(
    {
      topic: data.label,
      sentimentList: [data.sentiment]
    },
    {
      subTitle: data.label
    },
    [
      {
        text: data.sentiment,
        value: { sentimentList: [data.sentiment] }
      },
      { text: data.label, value: { topic: data.label } }
    ]
  )
}

/**
 * 处理TopRank“查看更多”点击事件
 */
const handleTopRankViewMore = (payload: any) => {
  console.log('TopRank查看更多点击数据:', payload)

  const sentiment = payload?.sentiment
  const journeyName = payload?.journey?.journeyName || ''
  const categoryTitle = payload?.category === 'satisfied' ? '客户满意TOP' : '客户不满TOP'
  const subTitle = journeyName ? `${journeyName} - ${categoryTitle}` : categoryTitle

  ddStore.openDD(
    {
      tagType: 'CJ',
      tag1Code: payload?.journey?.journeyCode,
      ...(sentiment ? { sentimentList: [sentiment] } : {})
    },
    {
      subTitle,
      activeTab: DrillTabKey.VIEWPOINT
    }
    // [...(sentiment ? [{ text: sentiment, value: { sentimentList: [sentiment] } }] : [])]
  )
}

/**
 * 处理服务分析单元格点击事件
 */
const handleServiceCellClick = (data: ProductSelfTagAnalysisRowVo) => {
  console.log('服务分析单元格点击数据:', data)
  if (data.name !== '集团均值') {
    ddStore.openDD(
      {
        brandCode: data.code,
        tag1Code: data.tag1Code,
        tag2Code: data.tag2Code
      },
      {
        // 弹框副标题显示：传入点击行的名称（仅用于展示）
        subTitle: `${data.name || ''}${data.tag2Name && data.name ? ' - ' : ''}${data.tag2Name || ''}`
      },
      [
        { text: data.name, value: { brandCode: data.code } },
        { text: data.tag1Name, value: { tag1Code: data.tag1Code } },
        { text: data.tag2Name, value: { tag2Code: data.tag2Code } }
      ]
    )
  }
}

/**
 * 处理产品分析单元格点击事件
 */
const handleProductCellClick = (data: ProductSelfTagAnalysisRowVo) => {
  console.log('产品分析单元格点击数据:', data)
  if (data.name !== '集团均值') {
    ddStore.openDD(
      {
        brandCode: data.code,
        tag1Code: data.tag1Code,
        tag2Code: data.tag2Code
      },
      {
        // 弹框副标题显示：传入点击行的名称（仅用于展示）
        subTitle: `${data.name || ''}${data.tag2Name && data.name ? ' - ' : ''}${data.tag2Name || ''}`
      },
      [
        { text: data.name, value: { brandCode: data.code } },
        { text: data.tag1Name, value: { tag1Code: data.tag1Code } },
        { text: data.tag2Name, value: { tag2Code: data.tag2Code } }
      ]
    )
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

const refreshAllData = () => {
  fetchProductSelfBrief()
  fetchFocusSceneTop()
  fetchDataTrendChange()
  fetchUserJourneyAnalysis()
  // 数据来源分析相关接口
  fetchChannelMentionShare()
  fetchChannelNegativeTrend()
  fetchDataSourceAnalysis()
  // 服务分析和产品分析
  fetchServiceTagAnalysis()
  fetchProductTagAnalysis()
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

const handleSearch = (_formData: any) => {
  console.log('handleSearch', _formData)
  searchParams.value = _formData

  // 清空关注场景TOP模块中表格的排序状态
  comprehensiveAnalysisRef.value?.clearSceneTopSort()

  reportSummaryInitialized.value = true

  refreshAllData()
}
</script>

<template>
  <FAnalyseWrap v-model="generalScenarioStore.visible">
    <template #header>
      <!--  @init-complete="handleHeaderInitComplete" -->
      <SCHeader title="本品分析" subtitle="智行"></SCHeader>
    </template>
    <UniversaFilter :routeName="`${route.name as string}`" @search="handleSearch"></UniversaFilter>
    <FCard
      title="综合分析"
      :card-key="CARD_EXPORT_KEYS.productSelf.Composite"
      tooltip="综合分析"
      :height="'1001px'"
      class="mt-24"
      downloadable
      data-page-export-card="summary"
    >
      <ReportSummary
        :api-function="getDataTrendChangeResult"
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
      title="用户旅程分析"
      :card-key="CARD_EXPORT_KEYS.productSelf.UserJourney"
      tooltip="用户旅程分析"
      :height="'1061px'"
      class="mt-24"
      downloadable
    >
      <ReportSummary
        :api-function="getUserJourneyAnalysisResult"
        :query-params="reportSummaryInitialized ? searchParams : {}"
      ></ReportSummary>
      <UserJourneyAnalysis
        :data="userJourneyAnalysisData"
        class="mt-24"
        @journey-click="handleJourneyClick"
        @row-click="handleTopRankRowClick"
        @view-more="handleTopRankViewMore"
      ></UserJourneyAnalysis>
    </FCard>

    <FCard
      title="服务分析"
      :card-key="CARD_EXPORT_KEYS.productSelf.Service"
      tooltip="服务分析"
      :height="'1001px'"
      class="mt-24"
      downloadable
    >
      <ReportSummary
        :api-function="getServiceTagAnalysisResult"
        :query-params="reportSummaryInitialized ? searchParams : {}"
      ></ReportSummary>
      <FWFX :data="serviceTagAnalysisData" @cell-click="handleServiceCellClick"></FWFX>
    </FCard>
    <FCard
      title="产品分析"
      :card-key="CARD_EXPORT_KEYS.productSelf.Product"
      tooltip="产品分析"
      :height="'1001px'"
      class="mt-24"
      downloadable
    >
      <ReportSummary
        :api-function="getThisProductTagAnalysisResult"
        :query-params="reportSummaryInitialized ? searchParams : {}"
      ></ReportSummary>
      <CPFX :data="productTagAnalysisData" @cell-click="handleProductCellClick"></CPFX>
    </FCard>

    <FCard
      title="数据来源分析"
      :card-key="CARD_EXPORT_KEYS.productSelf.DataSource"
      tooltip="数据来源分析"
      :height="'1101px'"
      class="mt-24"
      downloadable
    >
      <ReportSummary
        :api-function="getChannelNegativeTrendResult"
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
