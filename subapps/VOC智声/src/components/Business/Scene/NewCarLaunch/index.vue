<script setup lang="ts">
import { ref, onMounted, computed, onBeforeMount } from 'vue'
import { ElMessage } from 'element-plus'
import { useGeneralScenarioStore } from '@/store'
import { useQueryStore } from '@/store/modules/query'
import SCHeader from '@/components/Business/Scene/Common/SCHeader/index.vue'
import ReportSummary from '@/components/Business/Scene/Common/ReportSummary.vue'
import UniversaFilter from '@/components/Business/UniversaFilter/index.vue'
import OverallImpression from './OverallImpression/index.vue'
import DataSourceAnalysis from './DataSourceAnalysis/index.vue'
import ComprehensiveAnalysis from './ComprehensiveAnalysis/index.vue'
import FocusSceneTop from './FocusSceneTop/index.vue'
import OpinionEvaluation from './OpinionEvaluation/index.vue'
import { DEFAULT_MENTION_NEGATIVE_RATE_TYPE } from '@/constants'
import VoiceList from '@/components/Business/VoiceListPanel/index.vue'

import { getGroupProductBrief } from '@/api/groupAnalysis'
import type { ProductBriefVo } from '@/api/groupAnalysis/types'
import useMiddlewareStore from '@/store/modules/middleware'

import { useQueryListener } from '@/hooks/useQueryListener'
import useGeneralDrillDownStore from '@/store/modules/generalDrillDown'
import { getNewCarDataSourceResult, getNewCarYingXiangResult } from '@/api/reportSummary/index'
import { DrillTabKey } from '@/components/Business/DrillDownDialog/constants.ts'

import { getUserIntentionOpinionTop } from '@/api/productAnalysis'
import {
  getDataSourceAnalysis,
  getFocusSceneTop,
  getOverallImpression,
  getDataTrendChange,
  getOpinionEvaluation
} from '@/api/newCarLaunch'
import { getSeriesCondition } from '@/api/newCarLaunch'
import { getProductBrief } from '@/api/newCarLaunch'
import type { ProductBriefVo as NewCarProductBriefVo } from '@/api/newCarLaunch/types'
import { getDateRange } from '@/utils/date'
import { formatDate } from '@/utils'
import { useRoute, useRouter } from 'vue-router'
import { QueryType, INTERVAL_TYPE_OPTIONS } from './constants'

/**
 * 新车上市
 */
defineOptions({
  name: 'NewCarLaunch'
})

type NewCarPhaseLabel = '预热期' | '上市期' | '稳定期'
type NewCarProductBriefItem = NewCarProductBriefVo & {
  phase?: NewCarPhaseLabel
}
type NewCarProductBriefGroup = {
  preheat: NewCarProductBriefItem[]
  launch: NewCarProductBriefItem[]
  stable: NewCarProductBriefItem[]
}

const generalScenarioStore = useGeneralScenarioStore()
const route = useRoute()
const searchParams = ref<any>({})
const tagPath = ref<Array<{ code: string; name: string; level?: number }> | undefined>(undefined)
const middlewareStore = useMiddlewareStore()

const serviceReputationDataType = ref<'negativeRateMention' | 'negativeRateMoM' | 'mentionMoM'>(
  'negativeRateMention'
)
const productAnalysisDataType = ref<'negativeRateMention' | 'negativeRateMoM' | 'mentionMoM'>(
  'negativeRateMention'
)

// 渠道数据排行   预热期 上市期 稳定期
const intentionOpinionTopData = ref<{
  complaint: any[]
  consultation: any[]
  suggestion: any[]
}>({
  complaint: [],
  consultation: [],
  suggestion: []
})

// 产品简报数据   预热期 上市期 稳定期
const productBriefData = ref<NewCarProductBriefGroup>({
  preheat: [],
  launch: [],
  stable: []
})

// 数据趋势变化数据
const dataTrendChangeData = ref<{
  preheat: any
  launch: any
  stable: any
}>({
  preheat: null,
  launch: null,
  stable: null
})

// 观点评价数据
const opinionEvaluationData = ref<{
  preheat: {
    goodOpinions: any[]
    badOpinions: any[]
  }
  launch: {
    goodOpinions: any[]
    badOpinions: any[]
  }
  stable: {
    goodOpinions: any[]
    badOpinions: any[]
  }
}>({
  preheat: { goodOpinions: [], badOpinions: [] },
  launch: { goodOpinions: [], badOpinions: [] },
  stable: { goodOpinions: [], badOpinions: [] }
})

// 车系条件数据
const seriesConditionData = ref<{
  newCarSeries: Array<{ code: string; name: string; cars?: Array<{ code: string; name: string }> }>
  compareCarSeries: Array<{
    code: string
    name: string
    cars?: Array<{ code: string; name: string }>
  }>
}>({
  newCarSeries: [],
  compareCarSeries: []
})

// 初始化 ddStore
const ddStore = useGeneralDrillDownStore()

const queryStore = useQueryStore()

const carName = computed(() => {
  const carInfo = queryStore?.currentQueryParams?.newCarSeriesObjList?.[0]
  const x = carInfo?.name ? `-${carInfo?.name}` : ''
  return x
})

// 获取当前时间
const currentDate = new Date()
const formatCurrentDate = formatDate(currentDate, 'YYYY-MM-DD')

// ==================== 数据状态 ====================

// 整体印象 预热期 上市期 稳定期
const opinionComparisonTopData = ref<any>([[], [], []]) // 数组 存储三个数据列表
// 各自选中状态
const opinionComparisonSelectedCode = ref<any>([undefined, undefined, undefined])

// 关注场景TOP数据 预热期 上市期 稳定期
const focusSceneTopData = ref<{
  preheat: any[]
  launch: any[]
  stable: any[]
}>({
  preheat: [],
  launch: [],
  stable: []
})

// ==================== 接口调用方法 ====================
// 根据类型转换查询参数
const handConverQuery = (index: number) => {
  // 处理参数
  const copyQsStore = { ...queryStore.currentQueryParams, ...queryStore.commonQueryParams }
  const cl =
    copyQsStore.newCarSeriesObjList?.map((item: NewCarSeriesSelectorObj) => item.code) || []
  // 处理车辆参数 需要是数组格式的
  copyQsStore.newCarSeriesList = cl
  copyQsStore.carSeriesList = cl

  let startDate = ''
  let endDate = ''
  if (index === 0) {
    // 预热期
    startDate = copyQsStore.newCarSeriesObjList?.[0]?.preheatStartTime || ''
    endDate = copyQsStore.newCarSeriesObjList?.[0]?.preheatEndTime || ''
  } else if (index === 1) {
    // 上市期
    startDate = copyQsStore.newCarSeriesObjList?.[0]?.launchStartTime || ''
    endDate = copyQsStore.newCarSeriesObjList?.[0]?.launchEndTime || ''
  } else if (index === 2) {
    // 稳定期
    startDate = copyQsStore.newCarSeriesObjList?.[0]?.stableStartTime || ''
    endDate = copyQsStore.newCarSeriesObjList?.[0]?.stableEndTime || ''
  }
  copyQsStore.startDate = startDate
  copyQsStore.endDate = endDate
  // 删除不要的参数
  delete copyQsStore.newCarSeriesObjList
  copyQsStore.compBrandCodeList = undefined
  // 返回全新的查询参数

  return copyQsStore
}

/**
 * 获取整体印象数据
 */
const fetchOpinionComparison = async (index: number) => {
  // 根据index获取对应的选中code
  const qType = opinionComparisonSelectedCode.value[index]
  const kv = INTERVAL_TYPE_OPTIONS.map(item => item.value)
  const qName = kv[index]
  // 处理参数
  const copyQsStore = handConverQuery(index)
  try {
    const phases = kv
    const queryParams: VocQueryParams = {
      ...copyQsStore,
      // ...searchParams.value,
      phase: phases[index]
    }

    const response = await getOverallImpression({
      ...queryParams,
      sentiment: qType || undefined
    })
    if (response.success && response.result) {
      opinionComparisonTopData.value[index] = response.result
    } else {
      ElMessage.error(response.message || `获取${qName}失败`)
    }
  } catch (error) {
    console.error('整体印象数据失败:', error)
    ElMessage.error(`获取${qName}失败`)
  }
}

/**
 * 获取车系条件数据
 */
const fetchSeriesCondition = async () => {
  try {
    const response = await getSeriesCondition()
    if (response.success && response.result) {
      // 储存到sessionStorage里面去
      sessionStorage.setItem('seriesCarSessionData', JSON.stringify(response.result))
      seriesConditionData.value = response.result
    } else {
      ElMessage.error(response.message || '获取车系条件失败')
    }
  } catch (error) {
    console.error('获取车系条件失败:', error)
    ElMessage.error('获取车系条件失败')
  }
}

/**
 * 获取渠道数据排行数据
 * 需要调用3次接口，分别获取预热期、上市期、稳定期的数据
 */
const fetchUserIntentionOpinionTop = async (params: any = {}) => {
  const intentions = INTERVAL_TYPE_OPTIONS

  try {
    // 调用3个接口

    // 这里应该取上面新品车系筛选里的 预热期 上市期  稳定期 的各自的日期开始以及结束时间范围，目前先写死
    /***/

    const promises = intentions.map((intention, index) => {
      // 处理参数
      const copyQsStore = handConverQuery(index)
      const params = {
        ...copyQsStore,
        sortField: 'mentions',
        sortOrder: 'desc'
        // contentType: 'post_cmt'
      }
      return getDataSourceAnalysis({
        ...params,
        phase: intention.value
      })
    })

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
 * 获取产品简报数据
 * 只请求一次接口，获取预热期、上市期、稳定期的数据
 */
const fetchProductBrief = async () => {
  try {
    // 获取基础筛选参数（参考 data-source-analysis 接口的传参方式）
    const baseParams = { ...queryStore.currentQueryParams, ...queryStore.commonQueryParams }

    // 获取新品车系对象
    const newCarSeriesObj = baseParams.newCarSeriesObjList?.[0]

    // 获取对比车系对象
    const compareCarSeriesObj = baseParams.compCarSeriesObjList?.[0]

    // 处理车辆参数
    const newCarSeriesCodes =
      baseParams.newCarSeriesObjList?.map((item: NewCarSeriesSelectorObj) => item.code) || []
    const compareCarSeriesCodes =
      baseParams.compCarSeriesObjList?.map((item: NewCarSeriesSelectorObj) => item.code) || []

    // 构建请求参数（包含完整的筛选条件字段）
    const params = {
      ...baseParams,
      // 新品车系列表
      newCarSeriesList: newCarSeriesCodes,
      // 对比车系列表
      compareCarSeriesList: compareCarSeriesCodes,
      // 车辆参数（用于筛选）
      carSeriesList: undefined, // 后端不需要这个参数
      // 新品车系日期
      preheatStartTime: newCarSeriesObj?.preheatStartTime || '',
      preheatEndTime: newCarSeriesObj?.preheatEndTime || '',
      launchStartTime: newCarSeriesObj?.launchStartTime || '',
      launchEndTime: newCarSeriesObj?.launchEndTime || '',
      stableStartTime: newCarSeriesObj?.stableStartTime || '',
      stableEndTime: newCarSeriesObj?.stableEndTime || '',
      // 时期
      phase: '',
      // 对比车系日期（后缀C表示compare）
      preheatStartTimeC: compareCarSeriesObj?.preheatStartTime || '',
      preheatEndTimeC: compareCarSeriesObj?.preheatEndTime || '',
      launchStartTimeC: compareCarSeriesObj?.launchStartTime || '',
      launchEndTimeC: compareCarSeriesObj?.launchEndTime || '',
      stableStartTimeC: compareCarSeriesObj?.stableStartTime || '',
      stableEndTimeC: compareCarSeriesObj?.stableEndTime || '',
      // 排序参数
      sortField: 'negativeRate',
      sortOrder: 'asc'
    }

    // 删除不需要的参数
    delete params.newCarSeriesObjList
    delete params.compCarSeriesObjList
    params.compBrandCodeList = undefined

    // 只请求一次接口
    const response = await getProductBrief(params)

    // 处理响应数据
    if (response.success) {
      // 重置数据
      productBriefData.value.preheat = []
      productBriefData.value.launch = []
      productBriefData.value.stable = []

      const result = response.result as NewCarProductBriefItem[] | Partial<NewCarProductBriefGroup>

      if (Array.isArray(result)) {
        // 如果返回的是数组，根据 phase 字段分配到对应时期
        result.forEach(item => {
          switch (item.phase) {
            case '预热期':
              productBriefData.value.preheat.push(item)
              break
            case '上市期':
              productBriefData.value.launch.push(item)
              break
            case '稳定期':
              productBriefData.value.stable.push(item)
              break
          }
        })
      } else if (result && typeof result === 'object') {
        // 如果返回的是对象，按照属性分配到三个时期
        productBriefData.value.preheat = result.preheat || []
        productBriefData.value.launch = result.launch || []
        productBriefData.value.stable = result.stable || []
      }
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
    // 获取基础筛选参数（参考 data-source-analysis 接口的传参方式）
    const baseParams = { ...queryStore.currentQueryParams, ...queryStore.commonQueryParams }

    // 获取新品车系对象
    const newCarSeriesObj = baseParams.newCarSeriesObjList?.[0]

    // 获取对比车系对象
    const compareCarSeriesObj = baseParams.compCarSeriesObjList?.[0]

    // 处理车辆参数
    const newCarSeriesCodes =
      baseParams.newCarSeriesObjList?.map((item: NewCarSeriesSelectorObj) => item.code) || []
    const compareCarSeriesCodes =
      baseParams.compCarSeriesObjList?.map((item: NewCarSeriesSelectorObj) => item.code) || []

    // 构建请求参数（包含完整的筛选条件字段）
    const params = {
      ...baseParams,
      // 新品车系列表
      newCarSeriesList: newCarSeriesCodes,
      // 对比车系列表
      compareCarSeriesList: compareCarSeriesCodes,
      // 车辆参数（用于筛选）
      carSeriesList: newCarSeriesCodes,
      // 新品车系日期
      preheatStartTime: newCarSeriesObj?.preheatStartTime || '',
      preheatEndTime: newCarSeriesObj?.preheatEndTime || '',
      launchStartTime: newCarSeriesObj?.launchStartTime || '',
      launchEndTime: newCarSeriesObj?.launchEndTime || '',
      stableStartTime: newCarSeriesObj?.stableStartTime || '',
      stableEndTime: newCarSeriesObj?.stableEndTime || '',
      // 对比车系日期（后缀C表示compare）
      preheatStartTimeC: compareCarSeriesObj?.preheatStartTime || '',
      preheatEndTimeC: compareCarSeriesObj?.preheatEndTime || '',
      launchStartTimeC: compareCarSeriesObj?.launchStartTime || '',
      launchEndTimeC: compareCarSeriesObj?.launchEndTime || '',
      stableStartTimeC: compareCarSeriesObj?.stableStartTime || '',
      stableEndTimeC: compareCarSeriesObj?.stableEndTime || ''
    }

    // 删除不需要的参数
    delete params.newCarSeriesObjList
    delete params.compCarSeriesObjList
    params.compBrandCodeList = undefined

    // 调用接口
    const response = await getDataTrendChange(params)

    // 处理响应数据
    if (response.success) {
      // 重置数据
      dataTrendChangeData.value = {
        preheat: null,
        launch: null,
        stable: null
      }
      // 处理对应的数据

      dataTrendChangeData.value.preheat = response.result
      // dataTrendChangeData.value.launch = response.result
      // dataTrendChangeData.value.stable = response.result
    }
  } catch (error) {
    console.error('获取数据趋势变化数据失败:', error)
  }
}

/**
 * 获取关注场景TOP数据
 * 需要调用3次接口，分别获取预热期、上市期、稳定期的数据
 */
const fetchFocusSceneTop = async () => {
  const phases = ['preheat', 'launch', 'stable']
  const phaseNames = ['预热期', '上市期', '稳定期']

  try {
    const promises = phases.map((phase, index) => {
      // 处理参数
      const copyQsStore = handConverQuery(index)
      const params = {
        ...copyQsStore,
        sortField: 'mentions',
        sortOrder: 'desc'
      }
      return getFocusSceneTop({
        ...params,
        phase: phaseNames[index]
      })
    })

    const responses = await Promise.all(promises)

    // 处理响应数据
    responses.forEach((response: any, index: number) => {
      if (response.success && response.result) {
        focusSceneTopData.value[phases[index] as keyof typeof focusSceneTopData.value] =
          response.result
      }
    })
  } catch (error) {
    console.error('获取关注场景TOP数据失败:', error)
  }
}

/**
 * 获取观点评价数据
 * 需要调用3次接口，分别获取预热期、上市期、稳定期的数据
 */
const fetchOpinionEvaluation = async () => {
  const phases = ['preheat', 'launch', 'stable']
  const phaseNames = ['预热期', '上市期', '稳定期']

  try {
    const promises = phases.map((phase, index) => {
      // 处理参数
      const copyQsStore = handConverQuery(index)
      const params = {
        ...copyQsStore,
        phase: phaseNames[index]
      }
      return getOpinionEvaluation(params)
    })

    const responses = await Promise.all(promises)

    // 处理响应数据
    responses.forEach((response: any, index: number) => {
      if (response.success && response.result) {
        opinionEvaluationData.value[phases[index] as keyof typeof opinionEvaluationData.value] = {
          goodOpinions: response.result.goodOpinions || [],
          badOpinions: response.result.badOpinions || []
        }
      }
    })
  } catch (error) {
    console.error('获取观点评价数据失败:', error)
  }
}

// ==================== 事件处理 ====================

/**
 * 处理渠道数据排行排序变化
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
  // 找到对应的意图索引
  const intentions = INTERVAL_TYPE_OPTIONS.map(item => item.value)
  const intentionKeys = INTERVAL_TYPE_OPTIONS.map(item => item.key)
  const index = intentions.indexOf(intention)
  // 处理参数
  const copyQsStore = handConverQuery(index)
  try {
    const params = {
      ...copyQsStore,
      // ...searchParams.value,
      sortField: prop,
      sortOrder: order,
      phase: intention
    }

    const response = await getDataSourceAnalysis(params)

    if (response.success) {
      if (index !== -1) {
        // 更新对应的数据
        const intentionKey = intentionKeys[index] as keyof typeof intentionOpinionTopData.value
        intentionOpinionTopData.value[intentionKey] = response.result
      }
    }
  } catch (error) {
    console.error('获取渠道数据排行排序数据失败:', error)
  }
}

/**
 * 处理关注场景TOP排序变化
 */
const handleFocusSceneTopSort = async ({
  intention,
  prop,
  order
}: {
  intention: string
  prop: string
  order: string
}) => {
  // 找到对应的时期索引
  const phases = ['预热期', '上市期', '稳定期']
  const phaseKeys = ['preheat', 'launch', 'stable']
  const index = phases.indexOf(intention)

  if (index === -1) return

  // 处理参数
  const copyQsStore = handConverQuery(index)
  try {
    const params = {
      ...copyQsStore,
      sortField: prop,
      sortOrder: order,
      phase: intention
    }

    const response = await getFocusSceneTop(params)

    if (response.success) {
      // 更新对应时期的数据
      const phaseKey = phaseKeys[index] as keyof typeof focusSceneTopData.value
      focusSceneTopData.value[phaseKey] = response.result || []
    }
  } catch (error) {
    console.error('获取关注场景TOP排序数据失败:', error)
  }
}

/**
 * 处理数据来源分析表格行点击事件
 */
const handleFocusSceneTableRowClick = ({ intention, data }: { intention: string; data: any }) => {
  const isViewMore = data?.__viewMore

  // 找到对应的意图索引
  const intentions = INTERVAL_TYPE_OPTIONS.map(item => item.value)
  const index = intentions.indexOf(intention)
  // 处理参数
  const copyQsStore = handConverQuery(index)
  const newCarSeriesObjList = queryStore.currentQueryParams.newCarSeriesObjList
  const tags =
    newCarSeriesObjList?.map((item: NewCarSeriesSelectorObj) => {
      return { text: item.name, value: { carSeriesCode: item.code } }
    }) || []

  if (isViewMore) {
    // ddStore.openDD(drillParams, viewParams, tags)
    ddStore.openDD(
      { ...copyQsStore },
      {
        activeTab: DrillTabKey.DATASOURCE,
        subTitle: intention
      },
      tags
    )
  } else {
    ddStore.openDD(
      { ...copyQsStore, channelCode: data.channelCode },
      { subTitle: intention + '·' + data.channelName },
      [...tags, { text: data.channelName, value: { channelCode: data.channelCode } }]
    )
  }
}

/**
 * 处理综合分析表格行点击事件
 */
const handleZhTableRowClick = (data: any) => {
  console.log('点击data', data)
  // type =0 为 新品车系 type =1 为 对比车系
  const { period, car, type, periodData } = data || {}
  // 构建下钻参数
  const intentions = ['preheat', 'launch', 'stable']
  const indexSort = intentions.indexOf(period)
  // 处理开始时间 结束时间
  // 新品车系
  const newCarSeriesObjList = queryStore.currentQueryParams.newCarSeriesObjList
  const newCarSeriesObj = newCarSeriesObjList?.[0]
  // 对比车系
  const compCarSeriesObjList = queryStore.currentQueryParams.compCarSeriesObjList
  const compCarSeriesObj = compCarSeriesObjList?.[0]
  let carSeriesList = undefined

  if (type === 0) {
    carSeriesList = newCarSeriesObjList?.map((item: NewCarSeriesSelectorObj) => item.code) || []
  } else if (type === 1) {
    carSeriesList = compCarSeriesObjList?.map((item: NewCarSeriesSelectorObj) => item.code) || []
  }
  // console.log(
  //   'carSeriesList',
  //   carSeriesList,
  //   '新车车系',
  //   newCarSeriesObjList,
  //   '对比车系',
  //   compCarSeriesObjList
  // )
  let startDate: string = ''
  let endDate: string = ''
  if (indexSort === 0 && type === 0) {
    // 点击的是预热期 并且是新品车系
    startDate = newCarSeriesObj?.preheatStartTime || ''
    endDate = newCarSeriesObj?.preheatEndTime || ''
  } else if (indexSort === 0 && type === 1) {
    // 点击的是预热期 并且是对比车系
    startDate = compCarSeriesObj?.preheatStartTime || ''
    endDate = compCarSeriesObj?.preheatEndTime || ''
  } else if (indexSort === 1 && type === 0) {
    // 点击的是上市期 并且是新品车系
    startDate = newCarSeriesObj?.launchStartTime || ''
    endDate = newCarSeriesObj?.launchEndTime || ''
  } else if (indexSort === 1 && type === 1) {
    // 点击的是上市期 并且是对比车系
    startDate = compCarSeriesObj?.launchStartTime || ''
    endDate = compCarSeriesObj?.launchEndTime || ''
  } else if (indexSort === 2 && type === 0) {
    // 点击的是稳定期 并且是新品车系
    startDate = newCarSeriesObj?.stableStartTime || ''
    endDate = newCarSeriesObj?.stableEndTime || ''
  } else if (indexSort === 2 && type === 1) {
    // 点击的是稳定期 并且是对比车系
    startDate = compCarSeriesObj?.stableStartTime || ''
    endDate = compCarSeriesObj?.stableEndTime || ''
  }

  // 处理参数
  const copyQsStore = handConverQuery(indexSort)
  const drillParams = {
    ...copyQsStore,
    startDate,
    endDate,

    carSeriesList: carSeriesList,
    newCarSeriesList: carSeriesList,
    carSeriesName: car.series,
    brandName: car.brand,
    brandDataType: type === 1 ? 2 : undefined,
    period: period
  }

  // 构建视图参数
  const viewParams = {
    subTitle: `${periodData.value[period as keyof typeof periodData.value].name} · ${car.series}`,
    activeTab: 'trend'
  }

  // 构建标签
  const tags = [
    {
      text: `${car.series} · ${car.brand}`,
      value: { carSeriesName: car.series, brandName: car.brand },
      deletable: true
    }
    // {
    //   text: periodData.value[period as keyof typeof periodData.value].name,
    //   value: { period: period },
    //   deletable: true
    // }
  ]

  // 打开弹框
  ddStore.openDD(drillParams, viewParams, tags)
}

/**
 * 处理关注场景分析表格行点击事件
 */
const handleTableRowClick = (data: { intention: string; data: any }) => {
  const { intention, data: rowData } = data

  const isViewMore = rowData?.__viewMore || (!rowData?.scene && !rowData?.scenario)

  const intentions = INTERVAL_TYPE_OPTIONS.map(item => item.value)
  const index = intentions.indexOf(intention)

  // 处理参数
  const copyQsStore = handConverQuery(index)
  const newCarSeriesObjList = queryStore.currentQueryParams.newCarSeriesObjList
  const tags =
    newCarSeriesObjList?.map((item: NewCarSeriesSelectorObj) => {
      return { text: item.name, value: { carSeriesCode: item.code } }
    }) || []

  if (isViewMore) {
    ddStore.openDD(
      { ...copyQsStore, searchLabelLevel: 4 },
      { subTitle: rowData?.tableTitle || intention, activeTab: DrillTabKey.INDICATOR },
      tags
    )
  } else {
    const sceneValue = rowData.scene || rowData.scenario
    ddStore.openDD(
      { ...copyQsStore, tag4Code: rowData.tag4Code },
      { subTitle: intention + '·' + sceneValue },
      [
        ...tags,
        {
          text: rowData.scene || rowData.scenario,
          value: { topic: rowData.scene || rowData.scenario }
        }
      ]
    )
  }
}

/**
 * 处理图表点击事件
 */
const handleChartClick = (data: any) => {
  console.log('图表点击数据:', data)

  // 获取当前的查询参数
  const copyQsStore = { ...queryStore.currentQueryParams, ...queryStore.commonQueryParams }
  const cl =
    copyQsStore.newCarSeriesObjList?.map((item: NewCarSeriesSelectorObj) => item.code) || []
  copyQsStore.newCarSeriesList = cl
  copyQsStore.carSeriesList = cl
  delete copyQsStore.newCarSeriesObjList
  copyQsStore.compBrandCodeList = undefined

  // 获取日期范围参数
  const dateRangeParams = data.data.date ? getDateRange(data.data.date) : {}

  // 获取标签
  const newCarSeriesObjList = queryStore.currentQueryParams.newCarSeriesObjList
  const tags = newCarSeriesObjList?.map((item: NewCarSeriesSelectorObj) => {
    return { text: item.name, value: { carSeriesCode: item.code } }
  })

  // 打开下钻弹窗，传递完整参数
  ddStore.openDD(
    {
      ...copyQsStore,
      // brandDataType: 1,
      // tagType: 'DOM',
      ...dateRangeParams
    },
    {
      subTitle: data.data.date
    },
    tags
  )
}

// 处理观点评价情感切换事件
const handleViewpointSentimentChange = (payload: {
  index: number
  sentiment: string | undefined
}) => {
  // fetchUseOpinionComparisonTop(payload.index, payload.sentiment)
  // 更新选中状态
  opinionComparisonSelectedCode.value[payload.index] = payload.sentiment
  // 重新获取数据
  fetchOpinionComparison(payload.index)
}

// 处理整体印象点击事件
const handleViewpointWordClick = (payload: any) => {
  const index = payload.index
  // 处理参数
  const copyQsStore = handConverQuery(index)

  const newCarSeriesObjList = queryStore.currentQueryParams.newCarSeriesObjList
  const tags =
    newCarSeriesObjList?.map((item: NewCarSeriesSelectorObj) => {
      return { text: item.name, value: { carSeriesCode: item.code } }
    }) || []

  const intentions = INTERVAL_TYPE_OPTIONS.map(item => item.value)

  if (payload?.e?.type === 'click') {
    // 点击的更多
    ddStore.openDD(
      { ...copyQsStore },
      { activeTab: DrillTabKey.VIEWPOINT, subTitle: intentions[index] },
      tags
    )
  } else {
    ddStore.openDD(
      {
        ...copyQsStore,
        sentiment: payload?.e?.sentiment ? payload?.e?.sentiment : undefined,
        topic: payload?.e?.name ? payload?.e?.name : undefined
      },
      {
        // activeTab: DrillTabKey.VIEWPOINT,
        subTitle: intentions[index] + '·' + payload?.e?.name
      },
      [
        ...tags,
        // { text: payload?.e?.sentiment, value: { intention: payload?.e?.sentiment || undefined } },
        { text: payload?.e?.name, value: { topic: payload?.e?.name || undefined } }
      ]
    )
  }
}

/**
 * 处理观点评价排序变化
 */
const handleOpinionTopSort = async ({
  phase,
  sentiment,
  prop,
  order
}: {
  phase: string
  sentiment: string
  prop: string
  order: string
}) => {
  try {
    const mockParams = {
      ...queryStore.currentQueryParams,
      ...searchParams.value,
      // 模拟数据后面需要删除
      newCarSeriesList: ['A01A14'],
      contentType: 'post_cmt',
      sortField: prop,
      sortOrder: order,
      phase: phase,
      sentiment: sentiment
    }

    // 实际项目中应该调用真实接口
  } catch (error) {
    console.error('获取观点评价排序数据失败:', error)
  }
}

/**
 * 处理观点评价表格行点击事件
 */
const handleOpinionRowClick = ({
  phase,
  sentiment,
  data
}: {
  phase: string
  sentiment: string
  data: any
}) => {
  const isViewMore = data?.__viewMore || !data?.opinion

  const intentions = INTERVAL_TYPE_OPTIONS.map(item => item.value)
  const index = intentions.indexOf(phase)

  // 处理参数
  const copyQsStore = handConverQuery(index)
  const newCarSeriesObjList = queryStore.currentQueryParams.newCarSeriesObjList
  const tags =
    newCarSeriesObjList?.map((item: NewCarSeriesSelectorObj) => {
      return { text: item.name, value: { carSeriesCode: item.code } }
    }) || []

  const sl = sentiment === 'positive' ? ['正面'] : ['负面']
  if (isViewMore) {
    ddStore.openDD(
      {
        ...copyQsStore,
        sentimentList: sl
      },
      {
        subTitle: data?.tableTitle || `${phase}${sentiment === 'positive' ? '好评' : '抱怨'}TOP`,
        activeTab: DrillTabKey.VIEWPOINT
      },
      tags
    )
  } else {
    ddStore.openDD(
      {
        ...copyQsStore,
        sentimentList: sl,
        topic: data.opinion
      },
      { subTitle: phase + '·' + data.opinion },
      [
        ...tags,
        // { text: sentiment, value: { sentimentList: sl } },
        { text: data.opinion, value: { topic: data.opinion } }
      ]
    )
  }
}

// ==================== 生命周期 ====================

const refreshAllData = () => {
  // 初始化加载数据（车系条件数据只在组件挂载时加载一次）
  fetchOpinionComparison(0)
  fetchOpinionComparison(1)
  fetchOpinionComparison(2)
  fetchUserIntentionOpinionTop()
  fetchProductBrief()
  fetchFocusSceneTop()
  fetchDataTrendChange()
  fetchOpinionEvaluation()
}

// 使用查询监听 hooks
// const { queryStore } = useQueryListener(refreshAllData)

// ComprehensiveAnalysis 组件引用
const comprehensiveAnalysisRef = ref<any>(null)

// VoiceList 组件引用
const resultDataVoiceListRef = ref<any>(null)

onBeforeMount(() => {
  // 车系条件数据只在组件挂载时加载一次
  fetchSeriesCondition() // 这个方法必须在这里请求
})

// 组件挂载时加载数据
onMounted(() => {
  // 数据加载放在 handleSearch 里了，初始时不需要加载，等用户点击查询后才加载 如果需要特殊接口请求都是需要单独请求不要调用这个方法
  // refreshAllData()
})

const handleSearch = (
  _formData: any,
  _tagPath?: Array<{ code: string; name: string; level?: number }>
) => {
  const copyFd = { ..._formData }
  // 设置车辆信息
  copyFd.newCarSeriesList =
    queryStore.currentQueryParams.newCarSeriesObjList?.map(
      (item: NewCarSeriesSelectorObj) => item.code
    ) || []

  const newCarSeriesObj = queryStore.currentQueryParams.newCarSeriesObjList?.[0]
  const compCarSeriesObjList = queryStore.currentQueryParams.compCarSeriesObjList?.[0]
  console.log('新车上市查询参数,新品车系', newCarSeriesObj, '对比车系', compCarSeriesObjList)

  // 查原声时要带时间范围:新品车系预热期的开始时间~~当前查询时间 统一处理开始时间 结束时间
  copyFd.startDate = newCarSeriesObj?.preheatStartTime || ''
  copyFd.endDate = newCarSeriesObj?.preheatEndTime || ''

  copyFd.preheatStartTime = newCarSeriesObj?.preheatStartTime || ''
  copyFd.preheatEndTime = newCarSeriesObj?.preheatEndTime || ''

  copyFd.launchStartTime = newCarSeriesObj?.launchStartTime || ''
  copyFd.launchEndTime = newCarSeriesObj?.launchEndTime || ''

  copyFd.stableStartTime = newCarSeriesObj?.stableStartTime || ''
  copyFd.stableEndTime = newCarSeriesObj?.stableEndTime || ''

  // 删除多余的垃圾数据
  delete copyFd.compBrandCodeList
  searchParams.value = { ...copyFd }

  tagPath.value = _tagPath
  // 重置品牌排行表格的排序状态
  comprehensiveAnalysisRef.value?.resetBrandSeriesRankSort()
  refreshAllData()
}
</script>

<template>
  <FAnalyseWrap v-model="generalScenarioStore.visible">
    <template #header>
      <SCHeader title="新车上市" subtitle="智行汽车集团"></SCHeader>
    </template>
    <UniversaFilter
      :routeName="`${route.name as string}`"
      v-if="
        seriesConditionData?.newCarSeries?.length > 0 &&
        seriesConditionData?.compareCarSeries?.length > 0
      "
      :newCarSeriesOptions="seriesConditionData.newCarSeries"
      :competitiveTreeOptions="seriesConditionData.compareCarSeries"
      @search="handleSearch"
    ></UniversaFilter>
    <FCard title="综合分析" tooltip="综合分析" :height="'auto'" class="mt-24">
      <ComprehensiveAnalysis
        ref="comprehensiveAnalysisRef"
        :query-params="searchParams"
        :queryStore="queryStore"
        :product-brief-data="productBriefData"
        :data-trend-change-data="dataTrendChangeData"
        @table-row-click="handleZhTableRowClick"
        @chart-click="handleChartClick"
      ></ComprehensiveAnalysis>
    </FCard>
    <FCard
      :title="'关注场景TOP' + carName"
      :tooltip="'关注场景TOP' + carName"
      :height="'auto'"
      class="mt-24"
    >
      <FocusSceneTop
        :focus-scene-top-data="focusSceneTopData"
        :query-params="searchParams"
        @focus-scene-top-sort="handleFocusSceneTopSort"
        @table-row-click="handleTableRowClick"
      ></FocusSceneTop>
    </FCard>
    <FCard
      :title="'观点评价' + carName"
      :tooltip="'观点评价' + carName"
      :height="'auto'"
      class="mt-24"
    >
      <OpinionEvaluation
        :query-params="searchParams"
        :opinion-evaluation-data="opinionEvaluationData"
        @opinion-top-sort="handleOpinionTopSort"
        @opinion-row-click="handleOpinionRowClick"
      ></OpinionEvaluation>
    </FCard>

    <FCard
      :title="'整体印象' + carName"
      :tooltip="'整体印象' + carName"
      :height="'1000px'"
      class="mt-24"
    >
      <ReportSummary
        :api-function="getNewCarYingXiangResult"
        :query-params="searchParams"
      ></ReportSummary>
      <OverallImpression
        :data="opinionComparisonTopData"
        @sentiment-change="handleViewpointSentimentChange"
        @word-click="handleViewpointWordClick"
      ></OverallImpression>
    </FCard>

    <FCard
      :title="'数据来源分析' + carName"
      :tooltip="'数据来源分析' + carName"
      :height="'auto'"
      class="mt-24"
    >
      <ReportSummary
        :api-function="getNewCarDataSourceResult"
        :query-params="searchParams"
      ></ReportSummary>
      <DataSourceAnalysis
        ref="focusSceneDistributionRef"
        :intention-opinion-top-data="intentionOpinionTopData"
        :search-params="searchParams"
        :tag-path="tagPath"
        @intention-top-sort="handleIntentionTopSort"
        @table-row-click="handleFocusSceneTableRowClick"
      ></DataSourceAnalysis>
    </FCard>

    <!-- 客户原声 -->
    <div class="content-voice-wrapper">
      <VoiceList
        ref="resultDataVoiceListRef"
        key="NEWCAR-ResultDataList"
        :title="'客户原声' + carName"
        class="el-card"
        :queryParams="{
          ...searchParams,
          endDate: formatCurrentDate,
          carSeriesList: searchParams.newCarSeriesList
        }"
        :show-sort-select="true"
        :show-high-quality-filter="false"
        :enable-high-quality-actions="true"
        :enable-high-quality-info="true"
        :enable-voice-management-params="true"
        :enable-error-correction="true"
        :show-batch-action="false"
      />
    </div>
  </FAnalyseWrap>
</template>

<style lang="scss" scoped>
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
