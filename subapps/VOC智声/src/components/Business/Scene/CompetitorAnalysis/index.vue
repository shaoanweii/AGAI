<script setup lang="ts">
import { useGeneralScenarioStore } from '@/store'
import SCHeader from '@/components/Business/Scene/Common/SCHeader/index.vue'
import ReportSummary from '@/components/Business/Scene/Common/ReportSummary.vue'
import CPFX from './CPFX/index.vue'
import FWFX from './FWFX/index.vue'
import CompareHead from './CompareHead/index.vue'
import ComprehensiveComparison from './ComprehensiveComparison/index.vue'
import SceneRefinement from './SceneRefinement/index.vue'
import ViewpointEvaluationCard from './ViewpointEvaluationCard/index.vue'
import DSTable from './DSTable/index.vue'
import { computed, nextTick, ref, onBeforeUnmount, watch, onMounted } from 'vue'
import { onBeforeRouteLeave, useRoute } from 'vue-router'
import useGlobalCancelRequestStore from '@/store/modules/globalCancelRequest'
import {
  comprehensiveComparisonResult,
  dataSourceComparisonResult,
  productComparisonResult,
  sceneComparisonResult,
  serviceComparisonResult,
  userTopicComparisonResult
} from '@/api/reportSummary/index'
import { useFetch } from './useFetch'
import { MarketAverage, QueryType } from './constants'
import useGeneralDrillDownStore from '@/store/modules/generalDrillDown'
import { getDateRange } from '@/utils/date'
import UniversaFilter from '@/components/Business/UniversaFilter/index.vue'
import useMiddlewareStore from '@/store/modules/middleware'
import { useQueryStore } from '@/store/modules/query'
import { DrillTabKey } from '@components/Business/DrillDownDialog/constants'
import { usePageCardDownload } from '@/hooks/usePageCardDownload'
import { competitorStatExportMap, getCardStatExportRequest } from '@/api/downloadTask'
import { CARD_EXPORT_KEYS } from '@/constants/cardExportKeys'

defineOptions({
  name: 'CompetitorAnalysis'
})
const generalScenarioStore = useGeneralScenarioStore()
const ddStore = useGeneralDrillDownStore()
const route = useRoute()
const middlewareStore = useMiddlewareStore()
const querySoreStore = useQueryStore()

const searchParams = ref<any>({})

const serviceReputationDataType = ref<'negativeRateMention' | 'negativeRateMoM' | 'mentionMoM'>(
  'negativeRateMention'
)
const productAnalysisDataType = ref<'negativeRateMention' | 'negativeRateMoM' | 'mentionMoM'>(
  'negativeRateMention'
)

// 选中的品牌/车系代码
const firstSelectedCode = ref<string | undefined>()
const secondSelectedCode = ref<string | undefined>()
const firstSelectedName = ref('')
const secondSelectedName = ref('')
const compareHeadRef = ref<InstanceType<typeof CompareHead>>()
const sceneRefinementRef = ref<InstanceType<typeof SceneRefinement>>()

const splitCodeExportKeys: string[] = [
  CARD_EXPORT_KEYS.competitor.Opinion,
  CARD_EXPORT_KEYS.competitor.Scene
]

/**
 * 组装竞品对比导出参数。
 * 观点评价和场景细化导出需要区分本品、竞品编码，其余导出沿用列表参数。
 */
const getCompetitorExportParams = (payload?: { cardKey?: string; exportMenu?: string }) => {
  const queryType = middlewareStore.brandServiceCategoryType
  const selectedCodes = [firstSelectedCode.value, secondSelectedCode.value].filter(
    (code): code is string => Boolean(code)
  )
  const cardKey = String(payload?.cardKey || '').trim()

  if (splitCodeExportKeys.includes(cardKey)) {
    const selfCodeList = firstSelectedCode.value ? [firstSelectedCode.value] : undefined
    const competitorCodeList = secondSelectedCode.value ? [secondSelectedCode.value] : undefined
    const selectedCodeParams =
      queryType === QueryType.Series
        ? {
            carSeriesList: selfCodeList,
            compCarSeriesList: competitorCodeList
          }
        : {
            brandCodeList: selfCodeList,
            compBrandCodeList: competitorCodeList
          }

    return {
      ...searchParams.value,
      queryType,
      ...selectedCodeParams
    }
  }

  return {
    ...searchParams.value,
    queryType,
    brandCodeList:
      queryType === QueryType.Brand && selectedCodes.length ? selectedCodes : undefined,
    carSeriesList:
      queryType === QueryType.Series && selectedCodes.length ? selectedCodes : undefined
  }
}

usePageCardDownload({
  getParams: getCompetitorExportParams,
  getStatRequest: payload => getCardStatExportRequest(competitorStatExportMap, payload)
})

// ReportSummary 参数（从计算属性改为 ref）
const reportSummaryParams = ref<Record<string, any>>({})

// 标记是否正在刷新数据，防止重复调用
const isRefreshing = ref(false)

// 刷新所有数据（查询条件变化时调用）
const refreshAllData = async () => {
  // 设置刷新标志
  isRefreshing.value = true

  try {
    // 先调用初始化数据（可能会更新默认的品牌车系）
    await fetchInitialData()

    // 如果已经有选中的品牌/车系，也需要刷新对比数据
    if (firstSelectedCode.value && secondSelectedCode.value) {
      await fetchComparisonData()
    }
  } finally {
    // 重置刷新标志
    isRefreshing.value = false
  }
}

// const { queryStore } = useQueryListener(refreshAllData, ['updateQueryParams'], 300, false)

const queryStore = computed(() => {
  console.log('searchParams.value', searchParams.value)

  return {
    currentQueryParams: {
      ...searchParams.value,
      queryType: middlewareStore.brandServiceCategoryType
    }
  }
})

const {
  isUnmounted,
  fetchInitialData,
  fetchComparisonData,
  fetchServiceTagAnalysis,
  fetchProductTagAnalysis,
  fetchSceneComparisonTop,
  fetchUseOpinionComparisonTop,
  defaultHighestBrandCarData,
  allBrandOrCarSeriesOptions,
  comparativeBriefData,
  trendChangeCompareData,
  serviceTagAnalysisData,
  productTagAnalysisData,
  sceneComparisonTopData,
  useOpinionComparisonTopData,
  comparisonDataSourcesData,
  getAllBrandOrCarSeriesOptions
} = useFetch(queryStore, {
  firstSelectedCode,
  secondSelectedCode,
  serviceReputationDataType,
  productAnalysisDataType
})

const cancelRequestStore = useGlobalCancelRequestStore()

// 路由离开守卫 - 取消所有未完成的请求并设置卸载标志
onBeforeRouteLeave(() => {
  isUnmounted.value = true
  cancelRequestStore.cancelAllRequests()
})

// 组件卸载时清理
onBeforeUnmount(() => {
  cancelRequestStore.clearRequests()
})

// 处理选中代码更新
const handleSelectedCodesUpdate = (
  first: string | undefined,
  second: string | undefined,
  firstName?: string,
  secondName?: string
) => {
  firstSelectedCode.value = first
  secondSelectedCode.value = second
  if (firstName) firstSelectedName.value = firstName
  if (secondName) secondSelectedName.value = secondName

  // 更新 ReportSummary 参数
  updateReportSummaryParams()

  // 保存竞品对比数据到 store，用于发布报告
  if (first && second) {
    querySoreStore.setCompetitorAnalysisData({
      queryType: middlewareStore.brandServiceCategoryType,
      firstSelectedCode: first,
      secondSelectedCode: second,
      firstSelectedName: firstName,
      secondSelectedName: secondName
    })
  }

  // 如果正在刷新数据，不重复调用（refreshAllData 会调用）
  if (isRefreshing.value) {
    return
  }

  // 只有当两个code都有值时才调用对比数据接口
  if (first && second) {
    fetchComparisonData()
  }
}

// 更新 ReportSummary 参数
const updateReportSummaryParams = () => {
  // 基础查询参数
  const baseParams = {
    ...searchParams.value,
    queryType: middlewareStore.brandServiceCategoryType
  }

  // 只有当两个 code 都有值时才添加品牌/车系参数
  if (firstSelectedCode.value && secondSelectedCode.value) {
    const codes = [firstSelectedCode.value, secondSelectedCode.value] as string[]
    reportSummaryParams.value = {
      ...baseParams,
      firstSelectedName: firstSelectedName.value,
      secondSelectedName: secondSelectedName.value,
      brandCodeList:
        middlewareStore.brandServiceCategoryType === QueryType.Brand ? codes : undefined,
      carSeriesList:
        middlewareStore.brandServiceCategoryType === QueryType.Series ? codes : undefined
    }
  } else {
    // 如果 code 还没有值，设置为空对象（ReportSummary 会跳过接口调用）
    reportSummaryParams.value = {}
  }
}

// 标记是否有恢复的竞品对比数据需要处理
const hasRestoredCompetitorData = ref(false)

// 组件挂载时检查是否有恢复的数据
onMounted(async () => {
  const competitorData = querySoreStore.getCompetitorAnalysisData()
  if (competitorData && competitorData.firstSelectedCode && competitorData.secondSelectedCode) {
    hasRestoredCompetitorData.value = true

    // 等待组件完全渲染
    await nextTick()
    await nextTick()

    // 设置恢复数据到 CompareHead
    if (compareHeadRef.value) {
      // 先直接设置父组件的值
      firstSelectedCode.value = competitorData.firstSelectedCode
      secondSelectedCode.value = competitorData.secondSelectedCode

      // 调用 CompareHead 的方法设置显示，使用 forceRestore 参数强制恢复
      await compareHeadRef.value.setDefaultBrancCarSeries(
        {
          self: {
            code: competitorData.firstSelectedCode,
            name: competitorData.firstSelectedName || ''
          },
          competitor: {
            code: competitorData.secondSelectedCode,
            name: competitorData.secondSelectedName || ''
          }
        },
        true // 强制恢复，设置 isRestoring 标志
      )

      // 更新 ReportSummary 参数
      updateReportSummaryParams()
    }
  }
})

// 查询条件初始化完成事件
const handleHeaderInitComplete = async () => {
  // 检查是否是详情页
  const isDetailPage = route.query.isBack === '1'

  // 检查是否有恢复的竞品对比数据
  if (hasRestoredCompetitorData.value || isDetailPage) {
    // 如果是详情页或有恢复的数据，只获取选项列表，不获取默认品牌车系（避免覆盖恢复的数据）
    // CompareHead 的 watch 在详情页时会自动跳过初始化逻辑
    await getAllBrandOrCarSeriesOptions()

    // 等待初始化完成
    await nextTick()

    // 更新 ReportSummary 参数（此时 firstSelectedCode 和 secondSelectedCode 应该已经有值了）
    updateReportSummaryParams()

    // 调用对比数据接口
    await fetchComparisonData()
  } else {
    // 调用初始化数据，这会触发 CompareHead 的 watch
    // CompareHead 会通过 emit 更新 firstSelectedCode 和 secondSelectedCode
    // 然后在 handleSelectedCodesUpdate 中调用 fetchComparisonData 和 updateReportSummaryParams
    await fetchInitialData()

    const defaultComparison = defaultHighestBrandCarData.value
    if (defaultComparison.self?.code && defaultComparison.competitor?.code) {
      firstSelectedCode.value = defaultComparison.self.code
      secondSelectedCode.value = defaultComparison.competitor.code
      firstSelectedName.value = defaultComparison.self.name || '智行'
      secondSelectedName.value = defaultComparison.competitor.name || '远途'
      await compareHeadRef.value?.setDefaultBrancCarSeries(defaultComparison)
    }

    // 等待下一个tick，确保 CompareHead 的 watch 处理完成，code 已经设置好
    await nextTick()

    // 更新 ReportSummary 参数
    updateReportSummaryParams()
  }
}

// 处理CCCard卡片点击事件（来自ComprehensiveComparison组件）
const handleCardClick = (data: any) => {
  console.log('CCCard卡片点击事件:', data)
  const _queryType = queryStore.value.currentQueryParams.queryType
  //  市场均值不下钻
  if (data.name === MarketAverage) {
    return
  }
  ddStore.openDD(
    {
      brandCode: _queryType === QueryType.Brand ? data.code : undefined,
      carSeriesCode: _queryType === QueryType.Series ? data.code : undefined,
      brandDataType: 3,
      channelCatagory: '公域'
    },
    {
      subTitle: data.name
    }
  )
}

// 处理FEcharts图表点击事件（来自ComprehensiveComparison组件）
const handleChartClick = (params: any) => {
  console.log('FEcharts图表点击事件:', params)
  const _queryType = queryStore.value.currentQueryParams.queryType
  if (params.name === MarketAverage) {
    return
  }
  // 这里可以添加具体的业务逻辑
  ddStore.openDD(
    {
      brandCode: _queryType === QueryType.Brand ? params.data.code : undefined,
      carSeriesCode: _queryType === QueryType.Series ? params.data.code : undefined,
      brandDataType: 3,
      channelCatagory: '公域',
      ...(params.data.date && getDateRange(params.data.date))
    },
    {
      subTitle: params.name
    }
  )
}

// 处理服务分析单元格点击事件
const handleServiceCellClick = (data: any) => {
  console.log('服务分析单元格点击数据:', data)
  const _queryType = queryStore.value.currentQueryParams.queryType
  if (data.name !== MarketAverage) {
    ddStore.openDD(
      {
        brandCode: _queryType === QueryType.Brand ? data.code : undefined,
        carSeriesCode: _queryType === QueryType.Series ? data.code : undefined,
        tag1Code: data.tag1Code,
        tag2Code: data.tag2Code,
        brandDataType: 3,
        channelCatagory: '公域'
      },
      {
        subTitle: `${data.name || ''}${data.tag2Name && data.name ? ' - ' : ''}${data.tag2Name || ''}`
      },
      [
        {
          text: data.name,
          value: { [_queryType === QueryType.Brand ? 'brandCode' : 'carSeriesCode']: data.code }
        },
        { text: data.tag1Name, value: { tag1Code: data.tag1Code } },
        { text: data.tag2Name, value: { tag2Code: data.tag2Code } }
      ]
    )
  }
}

// 处理产品分析单元格点击事件
const handleProductCellClick = (data: any) => {
  console.log('产品分析单元格点击数据:', data)
  const _queryType = queryStore.value.currentQueryParams.queryType
  //  市场均值不下钻
  if (data.name !== MarketAverage) {
    ddStore.openDD(
      {
        brandCode: _queryType === QueryType.Brand ? data.code : undefined,
        carSeriesCode: _queryType === QueryType.Series ? data.code : undefined,
        tag1Code: data.tag1Code,
        tag2Code: data.tag2Code,
        brandDataType: 3,
        channelCatagory: '公域'
      },
      {
        subTitle: `${data.name || ''}${data.tag2Name && data.name ? ' - ' : ''}${data.tag2Name || ''}`
      },
      [
        {
          text: data.name,
          value: { [_queryType === QueryType.Brand ? 'brandCode' : 'carSeriesCode']: data.code }
        },
        { text: data.tag1Name, value: { tag1Code: data.tag1Code } },
        { text: data.tag2Name, value: { tag2Code: data.tag2Code } }
      ]
    )
  }
}

const handleServiceReputationSwitch = (option: { value: string | number; label: string }) => {
  const dataType = option.value as 'negativeRateMention' | 'negativeRateMoM' | 'mentionMoM'
  serviceReputationDataType.value = dataType
  fetchServiceTagAnalysis()
}

const handleProductAnalysisSwitch = (option: { value: string | number; label: string }) => {
  const dataType = option.value as 'negativeRateMention' | 'negativeRateMoM' | 'mentionMoM'
  productAnalysisDataType.value = dataType
  fetchProductTagAnalysis()
}

// 处理场景细化对比排序事件
const handleSceneRefinementSort = (data: { index: number; prop: string; order: string }) => {
  console.log('场景细化对比排序:', data)
  // 只查询当前表格的数据，并传入排序参数
  fetchSceneComparisonTop(data.index, { prop: data.prop, order: data.order })
}

// 处理场景细化对比行点击事件
const handleSceneRefinementRowClick = (data: { rowData: any; code: string; name: string }) => {
  console.log('场景细化对比行点击:', data)
  const _queryType = queryStore.value.currentQueryParams.queryType
  // 市场均值不下钻
  if (data.name === MarketAverage) {
    return
  }
  ddStore.openDD(
    {
      brandCode: _queryType === QueryType.Brand ? data.code : undefined,
      carSeriesCode: _queryType === QueryType.Series ? data.code : undefined,
      // tag3Code: data.rowData.tag3Code,
      tag4Code: data.rowData.tag4Code,
      brandDataType: 3,
      channelCatagory: '公域'
    },
    {
      subTitle: `${data.name || ''}${data.rowData.scene && data.name ? ' - ' : ''}${data.rowData.scene || ''}`
    },
    [
      {
        text: data.name,
        value: { [_queryType === QueryType.Brand ? 'brandCode' : 'carSeriesCode']: data.code }
      },
      {
        text: data.rowData.scene,
        value: { tag4Code: data.rowData.tag4Code }
      }
    ]
  )
}

// 处理场景细化对比“查看更多”点击事件
const handleSceneRefinementViewMore = (data: { code: string; name: string }) => {
  console.log('场景细化对比查看更多点击:', data)
  const _queryType = queryStore.value.currentQueryParams.queryType
  // 市场均值不下钻
  if (data.name === MarketAverage) {
    return
  }

  ddStore.openDD(
    {
      brandCode: _queryType === QueryType.Brand ? data.code : undefined,
      carSeriesCode: _queryType === QueryType.Series ? data.code : undefined,
      brandDataType: 3,
      channelCatagory: '公域',
      searchLabelLevel: 4
    },
    {
      subTitle: `${data.name || ''} - 场景TOP`,
      activeTab: DrillTabKey.INDICATOR
    }
    // [
    //   {
    //     text: data.name,
    //     value: { [_queryType === QueryType.Brand ? 'brandCode' : 'carSeriesCode']: data.code }
    //   }
    // ]
  )
}

// 处理观点评价情感切换事件
const handleViewpointSentimentChange = (payload: {
  index: number
  sentiment: string | undefined
}) => {
  fetchUseOpinionComparisonTop(payload.index, payload.sentiment)
}

// 处理观点词云点击事件
const handleViewpointWordClick = (payload: { wordData: any; code: string; name: string }) => {
  console.log('观点词云点击:', payload)
  const _queryType = queryStore.value.currentQueryParams.queryType
  // 市场均值不下钻
  if (payload.name === MarketAverage) {
    return
  }
  ddStore.openDD(
    {
      brandCode: _queryType === QueryType.Brand ? payload.code : undefined,
      carSeriesCode: _queryType === QueryType.Series ? payload.code : undefined,
      topic: payload.wordData.name,
      brandDataType: 3,
      channelCatagory: '公域',
      sentiment: payload.wordData.sentiment
    },
    {
      subTitle: `${payload.name || ''}${payload.wordData.name && payload.name ? ' - ' : ''}${payload.wordData.name || ''}`
    },
    [
      {
        text: payload.name,
        value: { [_queryType === QueryType.Brand ? 'brandCode' : 'carSeriesCode']: payload.code }
      },
      { text: payload.wordData.name, value: { topic: payload.wordData.name } }
    ]
  )
}

// 处理数据来源单元格点击事件
const handleDataSourceCellClick = (data: { brand: any; dimension: string; rowData: any }) => {
  console.log('数据来源单元格点击:', data)
  const _queryType = queryStore.value.currentQueryParams.queryType
  const lastDashIndex = data.dimension.lastIndexOf('-')
  const channelName =
    lastDashIndex > 0 ? data.dimension.substring(0, lastDashIndex) : data.dimension

  // 市场均值不下钻
  if (data.rowData.name === MarketAverage) {
    return
  }
  ddStore.openDD(
    {
      brandCode: _queryType === QueryType.Brand ? data.brand.brandCode : undefined,
      carSeriesCode: _queryType === QueryType.Series ? data.brand.brandCode : undefined,
      channelCode: data.rowData.channelCode,
      brandDataType: 3,
      channelCatagory: '公域'
    },
    {
      subTitle: `${data.brand.brandName || ''}${channelName && data.brand.brandName ? ' - ' : ''}${channelName || ''}`
    },
    [
      {
        text: data.brand.brandName,
        value: {
          [_queryType === QueryType.Brand ? 'brandCode' : 'carSeriesCode']: data.brand.brandCode
        }
      },
      { text: channelName, value: { channelCode: data.rowData.channelCode } }
    ]
  )
}

watch(
  () => middlewareStore.brandServiceCategoryType,
  async () => {
    // 切换品牌/车系时先清空旧维度，避免用品牌编码请求车系数据，反之亦然。
    isRefreshing.value = true
    firstSelectedCode.value = undefined
    secondSelectedCode.value = undefined
    firstSelectedName.value = ''
    secondSelectedName.value = ''
    reportSummaryParams.value = {}

    try {
      await fetchInitialData()
      await nextTick()

      if (
        compareHeadRef.value &&
        defaultHighestBrandCarData.value?.self &&
        defaultHighestBrandCarData.value?.competitor
      ) {
        await compareHeadRef.value.setDefaultBrancCarSeries(
          defaultHighestBrandCarData.value,
          false,
          true
        )
      }

      updateReportSummaryParams()
      if (firstSelectedCode.value && secondSelectedCode.value) {
        await fetchComparisonData()
      }
    } finally {
      isRefreshing.value = false
    }
  }
)

const handleSearch = (_formData: any) => {
  console.log('handleSearch', _formData)
  searchParams.value = _formData

  querySoreStore.commonQueryParams = {
    ..._formData,
    queryType: middlewareStore.brandServiceCategoryType
  }

  // 清空场景细化对比模块中所有表格的排序状态
  sceneRefinementRef.value?.clearAllSort()

  // 更新 ReportSummary 参数
  updateReportSummaryParams()

  refreshAllData()
}
</script>

<template>
  <!--  header-height="120px" -->
  <FAnalyseWrap v-model="generalScenarioStore.visible">
    <template #header>
      <SCHeader
        title="竞品对比"
        subtitle="品牌"
        @init-complete="handleHeaderInitComplete"
        style="height: 83px"
      ></SCHeader>
    </template>
    <UniversaFilter :routeName="`${route.name as string}`" @search="handleSearch"></UniversaFilter>
    <CompareHead
      ref="compareHeadRef"
      :defaultHighestBrandCarData="defaultHighestBrandCarData"
      :allBrandOrCarSeriesOptions="allBrandOrCarSeriesOptions"
      @update:selectedCodes="handleSelectedCodesUpdate"
    >
    </CompareHead>
    <FCard
      title="综合对比"
      :card-key="CARD_EXPORT_KEYS.competitor.Composite"
      :height="'1001px'"
      class="mt-24"
      downloadable
      data-page-export-card="summary"
    >
      <ReportSummary
        :api-function="comprehensiveComparisonResult"
        :query-params="reportSummaryParams"
      ></ReportSummary>
      <ComprehensiveComparison
        :comparativeBriefData="comparativeBriefData"
        :trendChangeCompareData="trendChangeCompareData"
        @cardClick="handleCardClick"
        @chartClick="handleChartClick"
      >
      </ComprehensiveComparison>
    </FCard>

    <FCard
      title="服务对比"
      :card-key="CARD_EXPORT_KEYS.competitor.Service"
      tooltip="服务对比"
      :height="'1001px'"
      class="mt-24"
      downloadable
    >
      <!-- <template #more>
        <SwitchButton
          v-model="serviceReputationDataType"
          :options="[
            { value: 'negativeRateMention', label: '负面率+提及量' },
            { value: 'negativeRateMoM', label: '负面率+环比' },
            { value: 'mentionMoM', label: '提及量+环比' }
          ]"
          @change="handleServiceReputationSwitch"
        ></SwitchButton>
      </template> -->
      <ReportSummary
        :api-function="serviceComparisonResult"
        :query-params="reportSummaryParams"
      ></ReportSummary>
      <FWFX
        :data="serviceTagAnalysisData"
        :data-type="serviceReputationDataType"
        @cell-click="handleServiceCellClick"
      >
      </FWFX>
    </FCard>
    <FCard
      title="产品对比"
      :card-key="CARD_EXPORT_KEYS.competitor.Product"
      tooltip="产品对比"
      :height="'1001px'"
      class="mt-24"
      downloadable
    >
      <!-- <template #more>
        <SwitchButton
          v-model="productAnalysisDataType"
          :options="[
            { value: 'negativeRateMention', label: '负面率+提及量' },
            { value: 'negativeRateMoM', label: '负面率+环比' },
            { value: 'mentionMoM', label: '提及量+环比' }
          ]"
          @change="handleProductAnalysisSwitch"
        ></SwitchButton>
      </template> -->
      <ReportSummary
        :api-function="productComparisonResult"
        :query-params="reportSummaryParams"
      ></ReportSummary>
      <CPFX
        :data="productTagAnalysisData"
        :data-type="productAnalysisDataType"
        @cell-click="handleProductCellClick"
      >
      </CPFX>
    </FCard>

    <FCard
      title="场景细化对比"
      :card-key="CARD_EXPORT_KEYS.competitor.Scene"
      tooltip="场景细化对比"
      :height="'1001px'"
      class="mt-24"
      downloadable
    >
      <ReportSummary
        :api-function="sceneComparisonResult"
        :query-params="reportSummaryParams"
      ></ReportSummary>
      <SceneRefinement
        ref="sceneRefinementRef"
        :data="sceneComparisonTopData"
        @sort-change="handleSceneRefinementSort"
        @row-click="handleSceneRefinementRowClick"
        @view-more="handleSceneRefinementViewMore"
      ></SceneRefinement>
    </FCard>
    <FCard
      title="观点评价对比"
      :card-key="CARD_EXPORT_KEYS.competitor.Opinion"
      tooltip="观点评价对比"
      :height="'1001px'"
      class="mt-24"
      downloadable
    >
      <ReportSummary
        :api-function="userTopicComparisonResult"
        :query-params="reportSummaryParams"
      ></ReportSummary>
      <ViewpointEvaluationCard
        :data="useOpinionComparisonTopData"
        @sentiment-change="handleViewpointSentimentChange"
        @word-click="handleViewpointWordClick"
      ></ViewpointEvaluationCard>
    </FCard>

    <FCard
      title="数据来源对比"
      :card-key="CARD_EXPORT_KEYS.competitor.DataSource"
      tooltip="数据来源分析"
      :height="'1001px'"
      class="mt-24"
      downloadable
    >
      <ReportSummary
        :api-function="dataSourceComparisonResult"
        :query-params="reportSummaryParams"
      ></ReportSummary>
      <DSTable :data="comparisonDataSourcesData" @cell-click="handleDataSourceCellClick"></DSTable>
    </FCard>
  </FAnalyseWrap>
</template>

<style lang="scss" scoped></style>
