<script setup lang="ts">
import { ref, onMounted, computed, watch, onBeforeUnmount } from 'vue'
import { useGeneralScenarioStore } from '@/store'
import { useQueryStore } from '@/store/modules/query'
import SCHeader from '@/components/Business/Scene/Common/SCHeader/index.vue'
import ReportSummary from '@/components/Business/Scene/Common/ReportSummary.vue'
import UniversaFilter from '@/components/Business/UniversaFilter/index.vue'
import SwitchButton from '@/components/UI/SwitchButton/index.vue'
import useMiddlewareStore from '@/store/modules/middleware'
import TopTable from '@/components/Business/Scene/MainAccount/ScensTable/TopTable.vue'
import ViewMore from '@/components/Business/DrillDownDialog/components/ViewMore.vue'
// 分块组件开始
import ZhAnalysis from './ZhAnalysis/index.vue'
import FourceScren from './FourceScren/index.vue'
import DataTrend from './DataTrend.vue'
import OpinionEvaluation from './OpinionEvaluation/index.vue'
import DataSourceAnalysis from '@/components/Business/Scene/Common/DataSourceAnalysis/index.vue'
import VoiceList from '@/components/Business/VoiceListPanel/index.vue'
import QdYsTrend from './QdYsTrend.vue'
import { getQueryDataByid } from '@/api/subscribeReport'

import { useQueryListener } from '@/hooks/useQueryListener'
import useGeneralDrillDownStore from '@/store/modules/generalDrillDown'
import {
  getJgsjZhfxResult,
  getYssjZhfxResult,
  getYssjGzcjResult,
  getYssjCjfxResult,
  getYssjGdpjResult,
  getJgsjSjlyResult,
  getYssjSjlyResult
} from '@/api/reportSummary/index'
import { getDateDimension, getDateRange } from '@/utils/date'
import type {
  ChannelShareVo as ServiceChannelMentionShareVo,
  ChannelNegativeTrendVo as ServiceChannelNegativeTrendVo,
  DataSourceAnalysisVo as ServiceDataSourceAnalysisVo
} from '@/api/serviceAnalysis/types'

import { DrillTabKey } from '@/components/Business/DrillDownDialog/constants.ts'
import {
  getHotEvDetail,
  getHotBriefData,
  getHotYsBriefData,
  getHotTendData,
  getHotYsTendData,
  getHotSeceData,
  getHotWordTopData,
  getHotSceneAnalysisChart,
  getHotUserIntentionOpinionTop,
  getHotOpinionEvaluation,
  getHotChannelMentionShare,
  getHotChannelNegativeTrend,
  getHotDataSourceAnalysis,
  getHotYsDataSourceAnalysis
} from '@/api/hotAphttp'

import { formatDate } from '@/utils'
import { useRoute, useRouter } from 'vue-router'
import { DEFAULT_MENTION_NEGATIVE_RATE_TYPE, OriginalDataType } from '@/constants'

import top_gd1 from '@/assets/images/top-gd1.png'
import top_gd2 from '@/assets/images/top-gd2.png'
import top_gd3 from '@/assets/images/top-gd3.png'
import top_gd4 from '@/assets/images/top-gd4.png'

defineOptions({
  name: 'HotDetailEvents'
})
const generalScenarioStore = useGeneralScenarioStore()
const route = useRoute()
// console.log('路由信息xxx', route, route.query)

const hotDetailData = ref<any>(null) // 详情数据 用于回显对应的条件

const searchParams = ref<any>({})
const tagPath = ref<Array<{ code: string; name: string; level?: number }> | undefined>(undefined)
const middlewareStore = useMiddlewareStore()

// 初始化 ddStore
const ddStore = useGeneralDrillDownStore()

const queryStore = useQueryStore()

// 综合分析-卡片数据
const productBriefData = ref<any>(null)
// 综合分析 - 数据趋势变化数据
const dataTrendChangeData = ref<any>(null)

// 关注场景 - TOP10
const focusSceneTopData = ref<any[]>([])
// 关注场景 - 数据词云数据
const dataWordListData = ref<any[]>([])

const queryInfoJson = ref<any>(null) // 回显查询条件相关

// 场景分析
// 场景分析 数据趋势变化 选择的柱子数据
const cjfxSelectBarData = ref<any>(null)
const cjfxData = ref<any>(null)
const cjfxLoading = ref(false)
const opinionLoading = ref(false)
// 观点TOP数据（复用领导总览组件）
const opinionTopData = ref<any>({
  complaintOpinions: [],
  consultOpinions: [],
  suggestionOpinions: [],
  praiseOpinions: []
})

// 观点评价 - 数据
const opinionEvaluationData = ref<{
  goodOpinions: any[]
  badOpinions: any[]
}>({
  goodOpinions: [],
  badOpinions: []
})

//  数据来源分析
const channelMentionShareData = ref<ServiceChannelMentionShareVo[]>([])
const channelNegativeTrendData = ref<ServiceChannelNegativeTrendVo[]>([])
const dataSourceAnalysisData = ref<ServiceDataSourceAnalysisVo[]>([])

//  原始数据-数据来源分析-柱状图
const dataYsSourceAnalysisData = ref<any[]>([])

/**
 * 原始数据页签恢复为只读查看模式，仅保留数据类型筛选与详情查看。
 */
const originalDataVoiceListProps = {
  showSortSelect: true,
  showDataStatus: false,
  showHighQualityFilter: false,
  enableHighQualityActions: false,
  enableHighQualityInfo: false,
  enableVoiceManagementParams: false,
  enableErrorCorrection: false,
  showBatchAction: false,
  showCorpusCreateAction: true,
  showTopicsInList: false,
  detailSource: 'list' as const,
  showTopicsInDetail: false,
  showBrandSeriesInDetail: false,
  showRelationEventsInDetail: false,
  showKeywordSearch: true
}

const handConverQuery = (query?: any) => {
  // 处理参数
  const copyQsStore = {
    ...queryStore.currentQueryParams,
    ...queryStore.commonQueryParams,
    ...(query || {})
    // 公共参数
    // brandDataType: 3,
    // tagType: 'CA'
  }
  if (copyQsStore.brandCode) {
    // @ts-ignore
    copyQsStore.brandCodeList = Array.isArray(copyQsStore.brandCode)
      ? copyQsStore.brandCode
      : [copyQsStore.brandCode]
    copyQsStore.brandCode = undefined
  }

  delete copyQsStore.compCarSeriesObjList
  delete copyQsStore.newCarSeriesList
  delete copyQsStore.newCarSeriesObjList

  // 本地开发需要剔除keyWords参数
  // const e = import.meta.env.MODE
  // console.log('开发环境', e)

  // if (e === 'development') {
  //   copyQsStore.keyWords = undefined
  // }

  return copyQsStore
}

// ==================== 数据状态 ====================

// ==================== 接口调用方法 ====================
/**
 * 综合分析 - 结果数据 -获取卡片数据
 */
const fetchProductBrief = async () => {
  try {
    const response = await getHotBriefData({ ...searchParams.value }) //
    if (response.success) {
      productBriefData.value = response.result
    }
  } catch (error) {
    console.error('获取卡片数据失败:', error)
  }
}

/**
 * 综合分析 - 原始数据 -获取卡片数据
 */
const fetchYsProductBrief = async () => {
  try {
    const response = await getHotYsBriefData({ ...searchParams.value }) //
    if (response.success) {
      productBriefData.value = response.result
    }
  } catch (error) {
    console.error('获取卡片数据失败:', error)
  }
}

/**
 * 综合分析 - 结果数据 - 获取数据趋势变化数据
 */
const fetchDataTrendChange = async () => {
  try {
    const response = await getHotTendData(searchParams.value) //
    if (response.success) {
      dataTrendChangeData.value = response.result
    }
  } catch (error) {
    console.error('获取数据趋势变化数据失败:', error)
  }
}

/**
 * 综合分析 - 原始数据 - 获取数据趋势变化数据
 */
const fetchYsDataTrendChange = async () => {
  try {
    const response = await getHotYsTendData(searchParams.value) //
    if (response.success) {
      dataTrendChangeData.value = response.result
    }
  } catch (error) {
    console.error('获取数据趋势变化数据失败:', error)
  }
}

/**
 * 关注场景 - TOP10数据
 */
const fetchFocusSceneTop = async () => {
  try {
    const response = await getHotSeceData(searchParams.value)
    if (response.success) {
      focusSceneTopData.value = response.result
    }
  } catch (error) {
    console.error('获取关注场景TOP数据失败:', error)
  }
}

/**
 * 关注场景 - 词云数据
 */
const fetchWordData = async (params: any) => {
  try {
    const response = await getHotWordTopData(params)
    if (response.success) {
      dataWordListData.value = response.result || []
    }
  } catch (error) {
    console.error('获取词云数据失败:', error)
  }
}

/**
 *  场景分析 - 图表数据
 */
const fetchCjfxData = async () => {
  cjfxLoading.value = true
  try {
    const queryParams: any = {
      ...searchParams.value
    }
    const response = await getHotSceneAnalysisChart(queryParams)
    if (response.success) {
      // 将接口返回的数据转换为DataTrend组件期望的格式
      // tag2Name字段作为x轴字段，需要转换为date字段
      const result = response.result
      let formattedData: any = null

      // 判断接口返回的数据格式
      if (Array.isArray(result)) {
        // 如果直接返回数组，包装成DataTrend组件期望的格式
        formattedData = {
          trend: result.map((item: any) => ({
            ...item,
            date: item.tag2Name || item.date
          }))
        }
      } else if (result && result.trend) {
        // 如果包含trend字段，直接转换
        formattedData = {
          ...result,
          trend: result.trend.map((item: any) => ({
            ...item,
            date: item.tag2Name || item.date
          }))
        }
      } else {
        // 其他情况直接使用
        formattedData = result
      }

      cjfxData.value = formattedData
    }
  } catch (error) {
    console.error('获取场景分析数据失败:', error)
  } finally {
    cjfxLoading.value = false
  }
}

const paramsCusScenTop = () => {
  if (cjfxSelectBarData.value) {
    let fourCodeTag: any = undefined
    let threeCodeTag: any = undefined
    let secondCodeTag: any = undefined
    let firstCodeTag: any = undefined
    const tagLevel = cjfxSelectBarData.value.tagLevel
    const tagCode = cjfxSelectBarData.value.tagCode
    if (tagLevel === '1') {
      firstCodeTag = [tagCode]
    } else if (tagLevel === '2') {
      secondCodeTag = [tagCode]
    } else if (tagLevel === '3') {
      threeCodeTag = [tagCode]
    } else if (tagLevel === '4') {
      fourCodeTag = [tagCode]
    }
    return { ...searchParams.value, fourCodeTag, threeCodeTag, secondCodeTag, firstCodeTag }
  }
  return searchParams.value
}

/**
 * 场景分析 - 观点TOP数据（复用领导总览组件格式）
 */
const fetchOpinionTopData = async () => {
  try {
    opinionLoading.value = true

    const intentions = ['抱怨', '咨询', '建议', '表扬']
    const keys = ['complaintOpinions', 'consultOpinions', 'suggestionOpinions', 'praiseOpinions']
    // const sceneType = 'PRODUCT'

    const promises = intentions.map(intention =>
      getHotUserIntentionOpinionTop({
        ...paramsCusScenTop(),
        topic: '',
        intention
        // sceneType
      })
    )

    const responses = await Promise.all(promises)

    const result: any = {}
    responses.forEach((response, index) => {
      if (response.success && response.result) {
        // 将接口返回的字段映射为TopTable组件期望的格式
        result[keys[index]] = response.result.map((item: any) => ({
          ...item
        }))
      } else {
        result[keys[index]] = []
      }
    })

    opinionTopData.value = result
  } catch (error) {
    console.error('获取用户意图观点TOP数据失败:', error)
  } finally {
    opinionLoading.value = false
  }
}

/**
 * 观点评价 - 分别获取 好评、抱怨的数据
 */
const fetchOpinionEvaluation = async () => {
  try {
    const params = {
      ...searchParams.value
    }

    const responses = await getHotOpinionEvaluation(params)

    // 处理响应数据
    opinionEvaluationData.value = {
      goodOpinions: responses?.result?.goodOpinions || [],
      badOpinions: responses?.result?.badOpinions || []
    }
  } catch (error) {
    console.error('获取观点评价数据失败:', error)
  }
}

/**
 * 数据来源分析 - 获取渠道提及量占比数据
 */
const fetchChannelMentionShare = async () => {
  try {
    const response = await getHotChannelMentionShare(searchParams.value)
    if (response.success) {
      channelMentionShareData.value = response.result
    }
  } catch (error) {
    console.error('获取渠道提及量占比数据失败:', error)
  }
}

/**
 * 数据来源分析 - 获取渠道数据趋势数据
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
    const response = await getHotChannelNegativeTrend(params)
    if (response.success) {
      channelNegativeTrendData.value = response.result
    }
  } catch (error) {
    console.error('获取渠道数据趋势数据失败:', error)
  }
}
/**
 * 数据来源分析 - 获取数据来源分析数据
 */
const fetchDataSourceAnalysis = async () => {
  try {
    const response = await getHotDataSourceAnalysis(searchParams.value)
    if (response.success) {
      dataSourceAnalysisData.value = response.result
    }
  } catch (error) {
    console.error('获取数据来源分析数据失败:', error)
  }
}

/**
 * 原始数据 - 数据来源分析 - 柱状图数据
 */
const fetchYsDataSourceAnalysis = async () => {
  try {
    const response = await getHotYsDataSourceAnalysis(searchParams.value)
    if (response.success) {
      console.log('原始数据 - 数据来源分析 - 柱状图数据', response)

      dataYsSourceAnalysisData.value = response.result
    }
  } catch (error) {
    console.error('获取数据来源分析数据失败:', error)
  }
}

// ==================== 事件处理 ====================
/**
 * 综合分析 - 处理卡片点击事件
 */
const handleCardChange = (data: any) => {
  console.log('卡片点击数据:', data)
  // 在这里处理点击数据，比如跳转到详情页或打开弹窗
  // 如果是结果数据可以下转
  if (middlewareStore.originalDataType === OriginalDataType.ResultData) {
    ddStore.openDD({ ...searchParams.value }, { subTitle: '' })
  }
}

/**
 * 综合分析 - 处理图表点击事件
 */
const handleChartClick = (data: any) => {
  console.log('趋势图表点击数据:', data)
  // 重新处理开始时间 结束时间
  // 如果是月份
  const date = data?.data?.date
  const dayType = getDateDimension(date)
  let startDate = searchParams.value.startDate
  let endDate = searchParams.value.endDate
  if (dayType === 'month') {
    // 转换成这个月第一天
    startDate = date + '-01'
    // 转换成这个月最后一天
    const endDateFirst = new Date(startDate)
    const endYear = endDateFirst.getFullYear()
    const endMonth = endDateFirst.getMonth() + 1
    // 返回本月最后一天
    const lastDay = new Date(endYear, endMonth, 0).getDate()
    endDate = `${date}-${String(lastDay).padStart(2, '0')}`
  }

  // 在这里处理点击数据，比如跳转到详情页或打开弹窗
  ddStore.openDD(
    {
      ...searchParams.value,
      startDate,
      endDate
    },
    {
      subTitle: data.data.date
    }
  )
}

/**
 * 关注场景 - TOP10排序变化
 * @param sortData 排序数据对象
 */
const handleSceneTopSort = async ({ prop, order }: { prop: string; order: string }) => {
  try {
    const response = await getHotSeceData({
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
 * 关注场景 - TOP行点击事件
 */
const handleSceneRowClick = (data: any) => {
  console.log('场景行点击数据:', data)
  // 在这里处理点击数据，比如跳转到详情页或打开弹窗
  ddStore.openDD(
    {
      ...searchParams.value,
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
 * 关注场景 - TOP“查看更多”点击事件
 */
const handleSceneViewMore = () => {
  console.log('关注场景TOP查看更多点击')
  ddStore.openDD(
    { ...searchParams.value, searchLabelLevel: 4 },
    {
      subTitle: '关注场景TOP',
      activeTab: DrillTabKey.INDICATOR
    }
  )
}

/**
 * 关注场景 -  词云TOP50“查看更多”点击事件
 */
const viewMoreWord = (data: any) => {
  console.log('点击更多', data)
  if (data?.data?.__viewMore) {
    // 查看更多
    ddStore.openDD(
      { ...searchParams.value },
      {
        // subTitle: '关注场景TOP',
        activeTab: DrillTabKey.VIEWPOINT
      }
    )
  } else {
    //
    const word = data?.data?.data || {}
    ddStore.openDD(
      {
        ...searchParams.value,
        topic: word.name,
        sentimentList: [word.sentiment]
      },
      { subTitle: word.name },
      [{ text: word.name, value: { topic: word.name } }]
    )
  }
}

// 关注场景 -  处理情感切换事件
const handleSentimentChange = (payload: { sentiment: string | undefined }) => {
  // 重新获取词云数据
  const np = { ...searchParams.value, ...payload }

  fetchWordData(np)
}

// 场景分析 - 趋势图点击联动效果处理
const cjfzChartClick = (data: any) => {
  console.log('场景分析-趋势图-点击柱子加载效果处理', data)
  const preName = cjfxSelectBarData.value?.name
  const curName = data?.data?.name
  if (preName === curName) {
    // 说明是点击的本数据 取消
    cjfxSelectBarData.value = null
  } else {
    cjfxSelectBarData.value = data?.data
  }
}

/**
 * 场景分析 -  观点TOP排序
 */
const handleOpinionSort = async (intention: string, prop: string, order: string | null) => {
  try {
    const intentionMap: Record<string, string> = {
      抱怨: 'complaintOpinions',
      咨询: 'consultOpinions',
      建议: 'suggestionOpinions',
      表扬: 'praiseOpinions'
    }

    const response = await getHotUserIntentionOpinionTop({
      ...paramsCusScenTop(),
      topic: '',
      intention,
      sortField: prop,
      sortOrder: order
    })

    if (response.success) {
      const key = intentionMap[intention]
      opinionTopData.value[key] = response.result || []
    }
  } catch (error) {
    console.error('排序失败:', error)
  }
}

/**
 * 场景分析 - 观点TOP查看更多
 */
const handleOpinionViewMore = (intention: string) => {
  ddStore.openDD(
    {
      ...paramsCusScenTop(),
      topic: '',
      intention
    },
    {
      subTitle: `${intention || ''}观点TOP`,
      activeTab: DrillTabKey.VIEWPOINT
    },
    [],
    {
      mergeCommonQueryParams: false
    }
  )
}

/**
 * 场景分析 - 处理观点TOP行点击
 */
const handleOpinionClick = (opinionName: string, intention: string, data: any) => {
  console.log('场景分析 - 处理观点TOP行点击:', { intention, data, opinionName })

  ddStore.openDD(
    {
      ...paramsCusScenTop(),
      intention,
      topic: data.opinion
    },
    { subTitle: data.opinion },
    [
      { text: intention, value: { intention } },
      { text: data.opinion, value: { topic: data.opinion } }
    ]
  )
}

/**
 * 观点评价 - 排序变化
 */
const handleOpinionTopSort = async ({
  sentiment,
  prop,
  order
}: {
  sentiment: string
  prop: string
  order: string
}) => {
  try {
    // 实际项目中应该调用真实接口
  } catch (error) {
    console.error('获取观点评价排序数据失败:', error)
  }
}

/**
 * 观点评价 - 表格行点击事件
 */
const handleOpinionRowClick = ({ sentiment, data }: { sentiment: string; data: any }) => {
  console.log('观点评价 - 表格行点击事件', sentiment, data)

  const isViewMore = data?.__viewMore || !data?.opinion
  // 处理参数
  const copyQsStore = searchParams.value
  // const newCarSeriesObjList = queryStore.currentQueryParams.newCarSeriesObjList
  // const tags =
  //   newCarSeriesObjList?.map((item: NewCarSeriesSelectorObj) => {
  //     return { text: item.name, value: { carSeriesCode: item.code } }
  //   }) || []

  const sl = sentiment === 'positive' ? ['正面'] : ['负面']
  const intention = sentiment === 'positive' ? '表扬' : '抱怨'
  if (isViewMore) {
    ddStore.openDD(
      {
        ...copyQsStore,
        intention
        // sentimentList: sl
      },
      {
        subTitle: data?.tableTitle || `${sentiment === 'positive' ? '表扬' : '抱怨'}TOP`,
        activeTab: DrillTabKey.VIEWPOINT
      }
      // tags
    )
  } else {
    ddStore.openDD(
      {
        ...copyQsStore,
        // sentimentList: sl,
        intention,
        topic: data.opinion
      },
      { subTitle: data.opinion },
      [
        // ...tags,
        // { text: sentiment, value: { sentimentList: sl } },
        { text: intention, value: { intention } },
        { text: data.opinion, value: { topic: data.opinion } }
      ]
    )
  }
}

/**
 * 数据来源分析 - 处理渠道数据趋势切换事件
 * @param dataType 数据类型：MentionNegativeRateType
 */
const handleChannelTrendDataTypeChange = async (dataType: MentionNegativeRateType) => {
  await fetchChannelNegativeTrend(dataType)
}

/**
 * 数据来源分析 - 处理数据来源分析图表点击事件
 */
const handleDataSourceChartClick = (data: any) => {
  console.log('数据来源分析图表点击数据:', data)
  const canverParams = {
    // keyWords: undefined, // 本地开发测试
    brandCode: undefined,
    brandCodeList: searchParams.value.brandCodeList,
    channelCode: undefined
  }
  if (data.type === 'proportion') {
    ddStore.openDD(
      {
        ...canverParams,
        channelIds: [data.data.channelCode]
      },
      {
        subTitle: data.data.channelName
      },
      [{ text: data.data.channelName, value: { channelCode: data.data.channelCode } }]
    )
  } else if (data.type === 'trend') {
    ddStore.openDD(
      {
        ...canverParams,
        channelIds: [data.data.chDatas[0]?.channelCode],
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
 * 数据来源分析 - 处理数据来源分析单元格点击事件
 */
const handleDataSourceCellClick = (data: any) => {
  console.log('数据来源分析单元格点击数据:', data)
  const canverParams = {
    // keyWords: undefined, // 本地开发测试
    brandCode: undefined,
    brandCodeList: searchParams.value.brandCodeList,
    channelCode: undefined
  }
  ddStore.openDD(
    {
      ...canverParams,
      channelIds: [data.channelCode]
    },
    {
      subTitle: data.channelName
    },
    [{ text: data.channelName, value: { channelCode: data.channelCode } }]
  )
}

/**
 * 原始数据 - 数据来源分析 - 柱状图点击事件
 */
const handleChartBarClick = (data: any) => {
  console.log('原始数据 - 数据来源分析 - 柱状图点击事件:', data)
}

// ==================== 生命周期 ====================

const isInistialLoad = computed(() => {
  return searchParams.value?.startDate && searchParams.value?.endDate
})

const refreshAllData = () => {
  // !hotDetailData.value ||
  if (!searchParams.value?.startDate || !searchParams.value?.endDate) {
    // 如果详情没获取到就不查询
    return
  }
  // 初始化加载数据（车系条件数据只在组件挂载时加载一次）
  if (middlewareStore.originalDataType === OriginalDataType.ResultData) {
    // 综合分析
    fetchProductBrief()
    fetchDataTrendChange()
    // 关注场景
    fetchFocusSceneTop()
    fetchWordData(searchParams.value)
    // 场景分析
    fetchCjfxData()
    fetchOpinionTopData()
    // 观点评价
    fetchOpinionEvaluation()
    // 数据来源分析
    fetchChannelMentionShare()
    fetchChannelNegativeTrend()
    fetchDataSourceAnalysis()
  } else if (middlewareStore.originalDataType === OriginalDataType.OriginalData) {
    // 综合分析
    fetchYsProductBrief()
    fetchYsDataTrendChange()
    // 数据来源分析
    fetchYsDataSourceAnalysis()
  }
}

// 组件挂载时加载数据
onMounted(() => {
  // 进入页面直接重置 防止其他页面切换情感tab导致页面异常
  middlewareStore.setOriginalDataType(OriginalDataType.ResultData)
  // 根据id查询详情
  if (route?.query?.id) {
    getHotEvDetail({ id: route?.query?.id }).then((res: any) => {
      hotDetailData.value = res?.result
      const filterStr = res?.result?.filterJson
      let filterJson: any = {}
      if (filterStr) {
        try {
          filterJson = JSON.parse(filterStr)
        } catch (error) {
          //
        }
      }
      // 特殊处理 体验代码
      queryInfoJson.value = filterJson
    })
  }

  // 订阅跳转页面到热点事件详情页面 额外处理逻辑
  // 判断route是不是带有  reportJudgeId 说明是报告跳转过来的
  const reportJudgeId = route.query.reportJudgeId
  if ((route?.query?.centerJudge || reportJudgeId) && route.name === 'hotDetailEvents') {
    const { reportHotId: reportId, taskHotId: taskId } = route.query
    if (reportId || taskId) {
      // 获取查询条件
      getQueryDataByid({
        reportId,
        taskId
      }).then((res: any) => {
        const filter = res?.result?.filter
        let filterJson: any = {}
        if (filter) {
          try {
            filterJson = JSON.parse(filter)
          } catch (error) {
            //
          }
          queryInfoJson.value = filterJson?.formData || {}
        }
      })
    }
  }

  // 设置页面默认筛选条件（加载角色配置的默认值）
  queryStore.setPageDefaultFilter(route.name as string)
  // 初始化搜索参数
  searchParams.value = handConverQuery()
  if (!route.query.id) {
    hotDetailData.value = {
      id: 'voc-hot-event-001',
      eventName: '智能座舱升级稳定性热点事件',
      filterJson: '{}'
    }
  }
  refreshAllData()
})

// ==================== 计算变量 ====================
// 标题
const detileText = computed(() => {
  return hotDetailData?.value?.eventName || ''
})

const handleSearch = (
  _formData: any,
  _tagPath?: Array<{ code: string; name: string; level?: number }>
) => {
  // console.log('查询条件', _formData, queryStore)
  const copyFd = { ..._formData }
  const paramsCopy = handConverQuery(copyFd)
  searchParams.value = { ...paramsCopy }
  // console.log('执行查询参数', searchParams.value)

  tagPath.value = _tagPath
  refreshAllData()
}

watch(
  () => middlewareStore.originalDataType,
  (newType, oldType) => {
    // 清空对应的数据
    productBriefData.value = null
    dataTrendChangeData.value = null
  }
)

watch(
  () => cjfxSelectBarData.value,
  (newVal, oldVal) => {
    if (!newVal && !oldVal) {
      // 初始化的时候不需要重复加载
      return
    }
    // 请求数据
    fetchOpinionTopData()
  },
  { deep: true }
)
</script>

<template>
  <!-- 结果数据 -->
  <FAnalyseWrap
    v-model="generalScenarioStore.visible"
    v-if="middlewareStore.originalDataType === OriginalDataType.ResultData"
  >
    <template #header>
      <SCHeader :title="detileText" subtitle="智行汽车集团" :tydmArrObj="queryInfoJson"></SCHeader>
    </template>
    <UniversaFilter :routeName="`hotDetailEvents`" @search="handleSearch"></UniversaFilter>

    <FCard title="综合分析" tooltip="综合分析" :height="'auto'" class="mt-24">
      <ReportSummary
        v-if="isInistialLoad"
        :api-function="getJgsjZhfxResult"
        :query-params="searchParams"
      ></ReportSummary>
      <ZhAnalysis
        ref="comprehensiveAnalysisRef"
        :product-brief-data="productBriefData"
        :data-trend-change-data="dataTrendChangeData"
        @cardChange="handleCardChange"
        @chart-click="handleChartClick"
      ></ZhAnalysis>
    </FCard>

    <FCard title="关注场景" tooltip="关注场景" :height="'auto'" class="mt-24">
      <ReportSummary
        v-if="isInistialLoad"
        :api-function="getYssjGzcjResult"
        :query-params="searchParams"
      ></ReportSummary>
      <FourceScren
        :focus-scene-top-data="focusSceneTopData"
        :opinionTopVos="dataWordListData"
        @scene-top-sort="handleSceneTopSort"
        @scene-row-click="handleSceneRowClick"
        @scene-view-more="handleSceneViewMore"
        @view-more-word="viewMoreWord"
        @sentiment-change="handleSentimentChange"
      >
      </FourceScren>
    </FCard>

    <FCard title="场景分析" tooltip="场景分析" :height="'auto'" class="mt-24">
      <ReportSummary
        v-if="isInistialLoad"
        :api-function="getYssjCjfxResult"
        :query-params="searchParams"
      ></ReportSummary>
      <div v-loading="cjfxLoading" class="mt-24">
        <DataTrend
          :data-trend-change-data="cjfxData"
          :is-show-title="true"
          :is-show-legend="true"
          :is-borderless="false"
          :height="'380px'"
          title-text="场景分析"
          @chart-click="cjfzChartClick"
        ></DataTrend>
      </div>
      <!-- 四组观点卡片 -->
      <div class="mt-24 top-container" v-loading="opinionLoading">
        <FCard
          :title="'抱怨观点TOP'"
          :height="'340px'"
          :isShowMore="true"
          class="f-card-border"
          @handleMore="() => handleOpinionViewMore('抱怨')"
        >
          <template #leftExtra>
            <img :src="top_gd1" alt="" class="topImg" />
          </template>
          <template #more>
            <ViewMore />
          </template>
          <TopTable
            ref="complaintTableRef"
            :data="opinionTopData.complaintOpinions"
            intention="抱怨"
            @sort-change="handleOpinionSort"
            @row-click="handleOpinionClick"
          ></TopTable>
        </FCard>
        <FCard
          :title="'咨询观点TOP'"
          :height="'340px'"
          :isShowMore="true"
          class="f-card-border"
          @handleMore="() => handleOpinionViewMore('咨询')"
        >
          <template #leftExtra>
            <img :src="top_gd2" alt="" class="topImg" />
          </template>
          <template #more>
            <ViewMore />
          </template>
          <TopTable
            ref="consultTableRef"
            :data="opinionTopData.consultOpinions"
            intention="咨询"
            @sort-change="handleOpinionSort"
            @row-click="handleOpinionClick"
          ></TopTable>
        </FCard>
        <FCard
          :title="'建议观点TOP'"
          :height="'340px'"
          :isShowMore="true"
          class="f-card-border"
          @handleMore="() => handleOpinionViewMore('建议')"
        >
          <template #leftExtra>
            <img :src="top_gd3" alt="" class="topImg" />
          </template>
          <template #more>
            <ViewMore />
          </template>
          <TopTable
            ref="suggestionTableRef"
            :data="opinionTopData.suggestionOpinions"
            intention="建议"
            @sort-change="handleOpinionSort"
            @row-click="handleOpinionClick"
          ></TopTable>
        </FCard>
        <FCard
          :title="'表扬观点TOP'"
          :height="'340px'"
          :isShowMore="true"
          class="f-card-border"
          @handleMore="() => handleOpinionViewMore('表扬')"
        >
          <template #leftExtra>
            <img :src="top_gd4" alt="" class="topImg" />
          </template>
          <template #more>
            <ViewMore />
          </template>
          <TopTable
            ref="praiseTableRef"
            :data="opinionTopData.praiseOpinions"
            intention="表扬"
            @sort-change="handleOpinionSort"
            @row-click="handleOpinionClick"
          ></TopTable>
        </FCard>
      </div>
    </FCard>

    <FCard title="观点评价" tooltip="观点评价" :height="'auto'" class="mt-24">
      <ReportSummary
        v-if="isInistialLoad"
        :api-function="getYssjGdpjResult"
        :query-params="searchParams"
      ></ReportSummary>
      <OpinionEvaluation
        :goodOpinions="opinionEvaluationData.goodOpinions"
        :badOpinions="opinionEvaluationData.badOpinions"
        @opinion-top-sort="handleOpinionTopSort"
        @opinion-row-click="handleOpinionRowClick"
      ></OpinionEvaluation>
    </FCard>

    <FCard title="数据来源分析" tooltip="数据来源分析" :height="'auto'" class="mt-24">
      <ReportSummary
        v-if="isInistialLoad"
        :api-function="getJgsjSjlyResult"
        :query-params="searchParams"
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

  <!-- 原始数据 -->
  <FAnalyseWrap
    v-model="generalScenarioStore.visible"
    v-if="middlewareStore.originalDataType === OriginalDataType.OriginalData"
  >
    <template #header>
      <SCHeader :title="detileText" subtitle="智行汽车集团" :tydmArrObj="queryInfoJson"></SCHeader>
    </template>
    <UniversaFilter :routeName="`hotDetailOriginalEvents`" @search="handleSearch"></UniversaFilter>

    <FCard title="综合分析" tooltip="综合分析" :height="'auto'" class="mt-24">
      <ReportSummary
        v-if="isInistialLoad"
        :api-function="getYssjZhfxResult"
        :query-params="searchParams"
      ></ReportSummary>
      <ZhAnalysis
        ref="comprehensiveAnalysisRef"
        :product-brief-data="productBriefData"
        :data-trend-change-data="dataTrendChangeData"
        @cardChange="handleCardChange"
        @chart-click="handleChartClick"
      ></ZhAnalysis>
    </FCard>
    <FCard title="数据来源分析" tooltip="数据来源分析" :height="'auto'" class="mt-24">
      <ReportSummary
        v-if="isInistialLoad"
        :api-function="getYssjSjlyResult"
        :query-params="searchParams"
      ></ReportSummary>
      <QdYsTrend :data="dataYsSourceAnalysisData" @chart-click="handleChartBarClick"></QdYsTrend>
    </FCard>
    <!-- 客户原生 -->
    <div class="content-voice-wrapper">
      <!-- 原始数据 -->
      <VoiceList
        ref="originalDataVoiceListRef"
        key="OriginalDataList"
        class="el-card"
        :queryParams="searchParams"
        v-bind="originalDataVoiceListProps"
        list-api-url="/report/vocLeadership/getRawData"
      />
    </div>
  </FAnalyseWrap>
</template>

<style lang="scss" scoped>
.top-container {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;

  :deep(.f-card) {
    height: 335px !important;
    background: #fff;
    box-shadow: 0px 1px 2px 0px rgba(10, 13, 18, 0.05) !important;
    border-radius: 12px 12px 12px 12px !important;
    border: 1px solid #ebedf0;

    .fc-header {
      padding: 16px 16px 0;
    }
    .fc-body {
      padding: 16px;
    }

    .fch-left {
      position: relative;
      .text-h2 {
        margin-left: 28px;
        font-size: 16px !important;
      }
      .topImg {
        position: absolute;
        left: 0;
        top: 6px;
        width: 20px;
      }
    }

    .text-h3 {
      line-height: 32px;
      font-size: 16px !important;
    }
  }
}

.wordcloud-container {
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.no-data {
  color: #999;
  font-size: 14px;
}

.content-voice-wrapper {
  width: 100%;
  height: 950px;
  margin-top: 24px;
}

.content-voice-wrapper :deep(.voice-list) {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.content-voice-wrapper :deep(.voice-list__container) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.content-voice-wrapper :deep(.theWrap) {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.content-voice-wrapper :deep(.theList) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.content-voice-wrapper :deep(.voice-list__items) {
  flex: 1;
  overflow-y: auto;
}
</style>
