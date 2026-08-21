<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useGeneralScenarioStore } from '@/store'
import { useQueryStore } from '@/store/modules/query'
import SCHeader from '@/components/Business/Scene/Common/SCHeader/index.vue'
import ReportSummary from '@/components/Business/Scene/Common/ReportSummary.vue'
import ComprehensiveAnalysis from './ComprehensiveAnalysis/index.vue'
import ServiceReputationAnalysis from './ServiceReputationAnalysis/index.vue'
import CPAnalysis from './CPAnalysis/index.vue'
import DSTable from './DSTable/index.vue'
import ViewpointEvaluation from '@/components/Business/Scene/Common/ViewpointEvaluation/index.vue'
import UniversaFilter from '@/components/Business/UniversaFilter/index.vue'
import { DEFAULT_MENTION_NEGATIVE_RATE_TYPE } from '@/constants'

import {
  getGroupProductBrief,
  getBrandTrendChange,
  getBrandSeriesRank,
  getServiceReputationAnalysis,
  getProductTagAnalysis,
  getOpinionEvaluation,
  getGroupDataSourceAnalysis
} from '@/api/groupAnalysis'
import type {
  ProductBriefVo,
  BrandTrendVo,
  SeriesRankItemVo,
  TagAnalysisRowVo,
  OpinionTopVo,
  GroupDataSourceAnalysisVo
} from '@/api/groupAnalysis/types'
// import FocusOnSceneDistribution from './FocusOnSceneDistribution/index.vue'
// import DataSourceAnalysis from './DataSourceAnalysis/index.vue'
// import RegionalAnalysis from './RegionalAnalysis/index.vue'
import { useQueryListener } from '@/hooks/useQueryListener'
import useGeneralDrillDownStore from '@/store/modules/generalDrillDown'
import {
  getProductReportResult,
  getServiceReputationAnalysisResult,
  getProductTagAnalysisResult,
  getOpinionEvaluationResult,
  getDataSourceAnalysisResult
} from '@/api/reportSummary/index'
import { getDateRange } from '@/utils/date'
import { useRoute, useRouter } from 'vue-router'
import { DrillTabKey } from '@components/Business/DrillDownDialog/constants.ts'
import { usePageCardDownload } from '@/hooks/usePageCardDownload'
import { getCardStatExportRequest, groupStatExportMap } from '@/api/downloadTask'
import { CARD_EXPORT_KEYS } from '@/constants/cardExportKeys'

/**
 * 集团分析
 */
defineOptions({
  name: 'GroupAnalysis'
})
const generalScenarioStore = useGeneralScenarioStore()
const route = useRoute()
const searchParams = ref<any>({})
usePageCardDownload({
  getParams: () => ({ ...searchParams.value }),
  getStatRequest: payload => getCardStatExportRequest(groupStatExportMap, payload)
})

// const queryStore = useQueryStore()

// ==================== 数据状态 ====================

// 数据简报
const productBriefData = ref<ProductBriefVo | null>(null)

// 品牌趋势变化
const brandTrendData = ref<BrandTrendVo[] | null>(null)
const brandTrendDataType = ref<MentionNegativeRateType>(DEFAULT_MENTION_NEGATIVE_RATE_TYPE)

// 品牌车系排行
const brandSeriesRankData = ref<SeriesRankItemVo[]>([])
const brandSeriesRankDataType = ref<'brand' | 'series'>('brand')

// 服务&口碑分析
const serviceReputationData = ref<TagAnalysisRowVo[] | null>(null)
const serviceReputationDataType = ref<'negativeRateMention' | 'negativeRateMoM' | 'mentionMoM'>(
  'negativeRateMention'
)

// 产品分析
const productAnalysisData = ref<TagAnalysisRowVo[] | null>(null)
const productAnalysisDataType = ref<'negativeRateMention' | 'negativeRateMoM' | 'mentionMoM'>(
  'negativeRateMention'
)

// 观点评价
const opinionEvaluationData = ref<OpinionTopVo[] | null>(null)

// 数据来源分析
const dataSourceAnalysisData = ref<GroupDataSourceAnalysisVo[] | null>(null)

// ==================== 接口调用方法 ====================

/**
 * 获取数据简报
 */
const fetchProductBrief = async () => {
  try {
    const queryParams: VocQueryParams = {
      ...searchParams.value
    }

    const response = await getGroupProductBrief(queryParams)
    if (response.success && response.result) {
      productBriefData.value = response.result
    } else {
      ElMessage.error(response.message || '获取数据简报失败')
    }
  } catch (error) {
    console.error('获取数据简报失败:', error)
    ElMessage.error('获取数据简报失败，请稍后重试')
  }
}

/**
 * 获取品牌趋势变化数据
 */
const fetchBrandTrendChange = async (
  dataType: MentionNegativeRateType = DEFAULT_MENTION_NEGATIVE_RATE_TYPE
) => {
  try {
    const queryParams: VocQueryParams = {
      ...searchParams.value,
      dataType
    }

    const response = await getBrandTrendChange(queryParams)
    if (response.success && response.result) {
      brandTrendData.value = response.result
      brandTrendDataType.value = dataType
    } else {
      ElMessage.error(response.message || '获取品牌趋势数据失败')
    }
  } catch (error) {
    console.error('获取品牌趋势数据失败:', error)
    ElMessage.error('获取品牌趋势数据失败，请稍后重试')
  }
}

/**
 * 获取品牌车系排行数据
 */
const fetchBrandSeriesRank = async (
  dataType: 'brand' | 'series' = 'brand',
  sortField?: string,
  sortOrder?: 'asc' | 'desc'
) => {
  try {
    const queryParams: VocQueryParams = {
      ...searchParams.value,
      dataType,
      ...(sortField && { sortField }),
      ...(sortOrder && { sortOrder })
    }

    const response = await getBrandSeriesRank(queryParams)
    if (response.success && response.result) {
      brandSeriesRankData.value = response.result
      brandSeriesRankDataType.value = dataType
    } else {
      ElMessage.error(response.message || '获取排行数据失败')
    }
  } catch (error) {
    console.error('获取排行数据失败:', error)
    ElMessage.error('获取排行数据失败，请稍后重试')
  }
}

/**
 * 获取服务&口碑分析数据
 */
const fetchServiceReputationAnalysis = async (
  dataType: 'negativeRateMention' | 'negativeRateMoM' | 'mentionMoM' = 'negativeRateMention'
) => {
  try {
    const queryParams: VocQueryParams = {
      ...searchParams.value,
      dataType
    }

    const response = await getServiceReputationAnalysis(queryParams)
    if (response.success && response.result) {
      serviceReputationData.value = response.result
      serviceReputationDataType.value = dataType
    } else {
      ElMessage.error(response.message || '获取服务&口碑分析数据失败')
    }
  } catch (error) {
    console.error('获取服务&口碑分析数据失败:', error)
    ElMessage.error('获取服务&口碑分析数据失败，请稍后重试')
  }
}

/**
 * 获取产品分析数据
 */
const fetchProductTagAnalysis = async (
  dataType: 'negativeRateMention' | 'negativeRateMoM' | 'mentionMoM' = 'negativeRateMention'
) => {
  try {
    const queryParams: VocQueryParams = {
      ...searchParams.value,
      dataType
    }

    const response = await getProductTagAnalysis(queryParams)
    if (response.success && response.result) {
      productAnalysisData.value = response.result
      productAnalysisDataType.value = dataType
    } else {
      ElMessage.error(response.message || '获取产品分析数据失败')
    }
  } catch (error) {
    console.error('获取产品分析数据失败:', error)
    ElMessage.error('获取产品分析数据失败，请稍后重试')
  }
}

/**
 * 获取观点评价数据
 */
const fetchOpinionEvaluation = async () => {
  try {
    const queryParams: VocQueryParams = {
      ...searchParams.value
    }

    const response = await getOpinionEvaluation(queryParams)
    if (response.success && response.result) {
      opinionEvaluationData.value = response.result
      opinionEvaluationData.value.map(item => {
        if (item.badOpinions) {
          item.badOpinions.map(opinion => {
            opinion.sentiment = opinion.sentiment || '负面'
          })
        }
        if (item.goodOpinions) {
          item.goodOpinions.map(opinion => {
            opinion.sentiment = opinion.sentiment || '正面'
          })
        }
      })
    } else {
      ElMessage.error(response.message || '获取观点评价数据失败')
    }
  } catch (error) {
    console.error('获取观点评价数据失败:', error)
    ElMessage.error('获取观点评价数据失败，请稍后重试')
  }
}

/**
 * 获取数据来源分析数据
 */
const fetchDataSourceAnalysis = async () => {
  try {
    const queryParams: VocQueryParams = {
      ...searchParams.value
    }

    const response = await getGroupDataSourceAnalysis(queryParams)
    if (response.success && response.result) {
      dataSourceAnalysisData.value = response.result
    } else {
      ElMessage.error(response.message || '获取数据来源分析数据失败')
    }
  } catch (error) {
    console.error('获取数据来源分析数据失败:', error)
    ElMessage.error('获取数据来源分析数据失败，请稍后重试')
  }
}

// ==================== 事件处理 ====================

/**
 * 处理品牌趋势切换事件
 */
const handleBrandTrendSwitch = (dataType: MentionNegativeRateType) => {
  fetchBrandTrendChange(dataType)
}

/**
 * 处理品牌车系排行切换事件
 */
const handleBrandSeriesRankSwitch = (dataType: 'brand' | 'series') => {
  comprehensiveAnalysisRef.value?.resetBrandSeriesRankSort()
  fetchBrandSeriesRank(dataType)
}

/**
 * 处理品牌车系排行排序事件
 */
const handleBrandSeriesRankSort = (sortField: string, sortOrder: 'asc' | 'desc') => {
  fetchBrandSeriesRank(brandSeriesRankDataType.value, sortField, sortOrder)
}

/**
 * 处理服务&口碑分析切换事件
 */
const handleServiceReputationSwitch = (option: { value: string | number; label: string }) => {
  const dataType = option.value as 'negativeRateMention' | 'negativeRateMoM' | 'mentionMoM'
  fetchServiceReputationAnalysis(dataType)
}

/**
 * 处理产品分析切换事件
 */
const handleProductAnalysisSwitch = (option: { value: string | number; label: string }) => {
  const dataType = option.value as 'negativeRateMention' | 'negativeRateMoM' | 'mentionMoM'
  fetchProductTagAnalysis(dataType)
}

/**
 * 处理服务&口碑分析表格单元格点击事件
 */
const handleServiceReputationCellClick = (data: any) => {
  console.log('服务&口碑分析表格单元格点击数据:', data)
  // 在这里处理点击数据，比如跳转到详情页或打开弹窗
  if (!data.rowData) return
  ddStore.openDD(
    {
      brandCode: data.rowData.brandCode,
      tag1Code: data.rowData.tag1Code,
      tag2Code: data.rowData.tag2Code,
      tagType: 'DOM'
    },
    {
      // 弹框副标题显示：传入点击行的名称（仅用于展示）
      subTitle: `${data.rowData.brandName || ''}${data.rowData.tag2Name && data.rowData.brandName ? ' - ' : ''}${data.rowData.tag2Name || ''}`
    },
    [
      { text: data.rowData.brandName, value: { brandCode: data.rowData.brandCode } },
      { text: data.rowData.tag1Name, value: { tag1Code: data.rowData.tag1Code } },
      { text: data.rowData.tag2Name, value: { tag2Code: data.rowData.tag2Code } }
    ]
  )
}

/**
 * 处理产品分析表格单元格点击事件
 */
const handleProductAnalysisCellClick = (data: any) => {
  console.log('产品分析表格单元格点击数据:', data)
  // 在这里处理点击数据，比如跳转到详情页或打开弹窗
  if (!data.rowData) return
  ddStore.openDD(
    {
      brandCode: data.rowData.brandCode,
      tag1Code: data.rowData.tag1Code,
      tag2Code: data.rowData.tag2Code,
      tagType: 'DOM'
    },
    {
      // 弹框副标题显示：传入点击行的名称（仅用于展示）
      subTitle: `${data.rowData.brandName || ''}${data.rowData.tag2Name && data.rowData.brandName ? ' - ' : ''}${data.rowData.tag2Name || ''}`
    },
    [
      { text: data.rowData.brandName, value: { brandCode: data.rowData.brandCode } },
      { text: data.rowData.tag1Name, value: { tag1Code: data.rowData.tag1Code } },
      { text: data.rowData.tag2Name, value: { tag2Code: data.rowData.tag2Code } }
    ]
  )
}

/**
 * 处理观点评价表格行点击事件
 */
const handleViewpointEvaluationRowClick = (data: any, curItem: any) => {
  console.log('观点评价表格行点击数据:', data)
  console.log('观点评价表格行点击数据:-->curItem', curItem)
  // 在这里处理点击数据，比如跳转到详情页或打开弹窗
  ddStore.openDD(
    {
      topic: data.opinion,
      brandCode: curItem.brandCode,
      sentimentList: [data.sentiment]
    },
    {
      // 弹框副标题显示：传入点击行的名称（仅用于展示）
      // subTitle: `${curItem.brandName || ''}${curItem.brandName && data.opinion ? ' - ' : ''}${data.opinion || ''}`
      subTitle: data.opinion
    },
    [
      { text: curItem.brandName, value: { brandCode: curItem.brandCode } },
      { text: data.sentiment, value: { sentimentList: [data.sentiment] } },
      { text: data.opinion, value: { topic: data.opinion } }
    ]
  )
}

/**
 * 处理观点评价“查看更多”点击事件
 * 说明：这里的“查看更多”不带具体 topic 过滤，仅按品牌 + 情感（若可取到）进入下钻。
 */
const handleViewpointEvaluationViewMore = (payload: any, curItem: any) => {
  console.log('观点评价查看更多点击数据:', payload)
  console.log('观点评价查看更多点击数据:-->curItem', curItem)

  const sentiment = payload?.sentiment

  ddStore.openDD(
    {
      brandCode: curItem.brandCode,
      ...(sentiment ? { sentimentList: [sentiment] } : {})
    },
    {
      subTitle: payload?.title || '观点评价',
      activeTab: DrillTabKey.VIEWPOINT
    }
    // [
    //   { text: curItem.brandName, value: { brandCode: curItem.brandCode } },
    //   ...(sentiment ? [{ text: sentiment, value: { sentimentList: [sentiment] } }] : [])
    // ]
  )
}

/**
 * 处理数据来源表格单元格点击事件
 */
const handleDataSourceCellClick = (data: any) => {
  console.log('数据来源表格单元格点击数据:', data)
  // 在这里处理点击数据，比如跳转到详情页或打开弹窗
  if (!data.rowData) return
  ddStore.openDD(
    {
      brandCode: data.rowData.brandCode,
      channelCode: data.rowData.channelCode
    },
    {
      // 弹框副标题显示：传入点击行的名称（仅用于展示）
      subTitle: `${data.rowData.brandName || ''}${data.rowData.channelName && data.rowData.brandName ? ' - ' : ''}${data.rowData.channelName || ''}`
    },
    [
      { text: data.rowData.brandName, value: { brandCode: data.rowData.brandCode } },
      { text: data.rowData.channelName, value: { channelCode: data.rowData.channelCode } }
    ]
  )
}

/**
 * 处理品牌趋势图表点击事件
 */
const handleBrandTrendClick = (data: any) => {
  console.log('品牌趋势图表点击数据:', data)
  // 在这里处理点击数据，比如跳转到详情页或打开弹窗
  // const times = getDateRange(data.date)
  ddStore.openDD(
    {
      brandCode: data.brandCode,
      ...(data.date && getDateRange(data.date))
    },
    {
      // 弹框副标题显示：传入点击行的名称（仅用于展示）
      subTitle: data.brandName
    },
    [{ text: data.brandName, value: { brandCode: data.brandCode } }]
  )
}

/**
 * 处理车系排行表格点击事件
 */
const handleCarSeriesRowClick = (rowData: any) => {
  console.log('车系排行表格点击数据:', rowData)
  // 在这里处理点击数据，比如跳转到详情页或打开弹窗
  const filterTags = []
  if (brandSeriesRankDataType.value === 'brand') {
    filterTags.push({ text: rowData.name, value: { brandCode: rowData.code } })
  } else if (brandSeriesRankDataType.value === 'series') {
    filterTags.push({ text: rowData.name, value: { carSeriesCode: rowData.code } })
  }
  ddStore.openDD(
    {
      brandCode: brandSeriesRankDataType.value === 'brand' ? rowData.code : undefined,
      carSeriesCode: brandSeriesRankDataType.value === 'series' ? rowData.code : undefined
    },
    {
      // 弹框副标题显示：传入点击行的名称（仅用于展示）
      subTitle: rowData.name
    },
    filterTags
  )
}

// ==================== 生命周期 ====================

const refreshAllData = () => {
  // 初始化加载数据
  fetchProductBrief()
  fetchBrandTrendChange(DEFAULT_MENTION_NEGATIVE_RATE_TYPE)
  fetchBrandSeriesRank('brand')
  fetchServiceReputationAnalysis('negativeRateMention')
  fetchProductTagAnalysis('negativeRateMention')
  fetchOpinionEvaluation()
  fetchDataSourceAnalysis()
}

// 使用查询监听 hooks
// const { queryStore } = useQueryListener(refreshAllData)
const ddStore = useGeneralDrillDownStore()

const cardChange = (cardType: string) => {
  console.log('cardType', cardType)
  ddStore.openDD(
    {},
    {
      subTitle: '智行汽车集团'
    }
  )
}

// ComprehensiveAnalysis 组件引用
const comprehensiveAnalysisRef = ref<any>(null)

const handleSearch = (_formData: any) => {
  console.log('handleSearch', _formData)
  searchParams.value = _formData
  // 重置品牌排行表格的排序状态
  comprehensiveAnalysisRef.value?.resetBrandSeriesRankSort()
  refreshAllData()
}
</script>

<template>
  <FAnalyseWrap v-model="generalScenarioStore.visible">
    <template #header>
      <SCHeader title="集团分析" subtitle="智行汽车集团"></SCHeader>
    </template>
    <UniversaFilter :routeName="`${route.name as string}`" @search="handleSearch"></UniversaFilter>
    <FCard
      title="综合分析"
      :card-key="CARD_EXPORT_KEYS.group.Composite"
      tooltip="综合分析"
      :height="'1001px'"
      class="mt-24"
      downloadable
      data-page-export-card="summary"
    >
      <ReportSummary
        :api-function="getProductReportResult"
        :query-params="searchParams"
      ></ReportSummary>
      <ComprehensiveAnalysis
        ref="comprehensiveAnalysisRef"
        :product-brief-data="productBriefData"
        :brand-trend-data="brandTrendData"
        :brand-trend-data-type="brandTrendDataType"
        :brand-series-rank-data="brandSeriesRankData"
        :brand-series-rank-data-type="brandSeriesRankDataType"
        @brand-trend-switch="handleBrandTrendSwitch"
        @brand-series-rank-switch="handleBrandSeriesRankSwitch"
        @brand-series-rank-sort="handleBrandSeriesRankSort"
        @brand-trend-click="handleBrandTrendClick"
        @car-series-row-click="handleCarSeriesRowClick"
        @cardChange="cardChange"
      >
      </ComprehensiveAnalysis>
    </FCard>
    <FCard
      title="服务分析"
      :card-key="CARD_EXPORT_KEYS.group.Service"
      tooltip="服务分析"
      :height="'1001px'"
      class="mt-24"
      downloadable
    >
      <template #more>
        <SwitchButton
          v-model="serviceReputationDataType"
          :options="[
            { value: 'negativeRateMention', label: '负面率+提及量' },
            { value: 'negativeRateMoM', label: '负面率+环比' },
            { value: 'mentionMoM', label: '提及量+环比' }
          ]"
          @change="handleServiceReputationSwitch"
        ></SwitchButton>
      </template>
      <ReportSummary
        :api-function="getServiceReputationAnalysisResult"
        :query-params="searchParams"
      ></ReportSummary>
      <ServiceReputationAnalysis
        :data="serviceReputationData"
        :data-type="serviceReputationDataType"
        :highlight-threshold="30"
        @cell-click="handleServiceReputationCellClick"
      ></ServiceReputationAnalysis>
    </FCard>

    <FCard
      title="产品分析"
      :card-key="CARD_EXPORT_KEYS.group.Product"
      tooltip="产品分析"
      :height="'1001px'"
      class="mt-24"
      downloadable
    >
      <template #more>
        <SwitchButton
          v-model="productAnalysisDataType"
          :options="[
            { value: 'negativeRateMention', label: '负面率+提及量' },
            { value: 'negativeRateMoM', label: '负面率+环比' },
            { value: 'mentionMoM', label: '提及量+环比' }
          ]"
          @change="handleProductAnalysisSwitch"
        ></SwitchButton>
      </template>
      <ReportSummary
        :api-function="getProductTagAnalysisResult"
        :query-params="searchParams"
      ></ReportSummary>
      <CPAnalysis
        :data="productAnalysisData"
        :data-type="productAnalysisDataType"
        :highlight-threshold="30"
        @cell-click="handleProductAnalysisCellClick"
      ></CPAnalysis>
    </FCard>

    <!-- <FCard title="观点评价" tooltip="观点评价" :height="'1001px'" class="mt-24"> -->
    <FCard
      title="观点评价"
      :card-key="CARD_EXPORT_KEYS.group.Opinion"
      tooltip="观点评价"
      :height="'auto'"
      class="mt-24"
      downloadable
    >
      <ReportSummary
        :api-function="getOpinionEvaluationResult"
        :query-params="searchParams"
      ></ReportSummary>
      <ViewpointEvaluation
        :data="opinionEvaluationData"
        @row-click="handleViewpointEvaluationRowClick"
        @view-more="handleViewpointEvaluationViewMore"
      >
      </ViewpointEvaluation>
    </FCard>

    <FCard
      title="数据来源"
      :card-key="CARD_EXPORT_KEYS.group.DataSource"
      tooltip="数据来源"
      :height="'1001px'"
      class="mt-24"
      downloadable
    >
      <ReportSummary
        :api-function="getDataSourceAnalysisResult"
        :query-params="searchParams"
      ></ReportSummary>
      <DSTable :data="dataSourceAnalysisData" @cell-click="handleDataSourceCellClick"></DSTable>
    </FCard>
  </FAnalyseWrap>
</template>

<style lang="scss" scoped></style>
