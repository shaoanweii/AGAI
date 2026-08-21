import { ref, type Ref } from 'vue'
import {
  defaultHighestBrandCar,
  getAllBrandOrCarSeriesData,
  getComparativeBrief,
  getComparisonDataSources,
  getProductTagAnalysis,
  getSceneComparisonTop,
  getServiceTagAnalysis,
  getTrendChangeCompare,
  getUseOpinionComparisonTop
} from '@/api/competitorAnalysis'
import type {
  brandCarSeriesItem,
  ComparativeBriefVo,
  HighestBrandCarVo,
  SceneComparisonVo,
  SourceCompareVo,
  TagAnalysisVo,
  TrendVo
} from '@/api/competitorAnalysis/types'
import { useQueryStore } from '@/store/modules/query'
import { QueryType } from './constants'

type QueryStore = ReturnType<typeof useQueryStore>

interface UseFetchParams {
  firstSelectedCode: Ref<string | undefined>
  secondSelectedCode: Ref<string | undefined>
  serviceReputationDataType: Ref<'negativeRateMention' | 'negativeRateMoM' | 'mentionMoM'>
  productAnalysisDataType: Ref<'negativeRateMention' | 'negativeRateMoM' | 'mentionMoM'>
}

interface UseFetchResult {
  isUnmounted: Ref<boolean>
  defaultHighestBrandCarData: Ref<HighestBrandCarVo>
  allBrandOrCarSeriesOptions: Ref<brandCarSeriesItem[]>
  comparativeBriefData: Ref<ComparativeBriefVo[] | undefined>
  trendChangeCompareData: Ref<TrendVo[] | undefined>
  serviceTagAnalysisData: Ref<TagAnalysisVo[] | undefined>
  productTagAnalysisData: Ref<TagAnalysisVo[] | undefined>
  sceneComparisonTopData: Ref<SceneComparisonVo[]>
  useOpinionComparisonTopData: Ref<SceneComparisonVo[]>
  comparisonDataSourcesData: Ref<SourceCompareVo[] | undefined>
  fetchInitialData: () => Promise<void>
  fetchComparisonData: () => Promise<void>
  fetchServiceTagAnalysis: () => Promise<void>
  fetchProductTagAnalysis: () => Promise<void>
  fetchComparisonDataSources: () => Promise<void>
  fetchSceneComparisonTop: (
    index?: number,
    sortParams?: { prop: string; order: string }
  ) => Promise<void>
  fetchUseOpinionComparisonTop: (index?: number, sentiment?: string) => Promise<void>
  getAllBrandOrCarSeriesOptions: () => Promise<void>
}

// export function useFetch(queryStore: QueryStore, params: UseFetchParams): UseFetchResult {
export function useFetch(queryStore: any, params: UseFetchParams): UseFetchResult {
  // 组件是否已卸载的标志
  const isUnmounted = ref(false)
  let activeComparisonRequest = 0

  const isLatestComparisonRequest = (requestId?: number) =>
    requestId === undefined || requestId === activeComparisonRequest

  const defaultHighestBrandCarData = ref<HighestBrandCarVo>({
    self: undefined,
    competitor: undefined
  })

  /**
   * @description: 默认提及最高的品牌-车系
   * @return {*}
   */
  const fetchDefaultHighestBrandCar = async () => {
    if (isUnmounted.value) return
    try {
      const res = await defaultHighestBrandCar({
        ...queryStore.value.currentQueryParams
      })
      if (res.success) {
        defaultHighestBrandCarData.value = res.result || []
      }
    } catch (error: any) {
      if (error.name !== 'CanceledError') {
        defaultHighestBrandCarData.value.self = undefined
        defaultHighestBrandCarData.value.competitor = undefined
      }
    }
  }

  const allBrandOrCarSeriesOptions = ref<brandCarSeriesItem[]>([])
  /**
   * @description: 获取本竞品品牌车系下拉选项
   * @return {*}
   */
  const getAllBrandOrCarSeriesOptions = async () => {
    if (isUnmounted.value) return
    try {
      const res = await getAllBrandOrCarSeriesData({
        ...queryStore.value.currentQueryParams
      })
      if (res.success) {
        allBrandOrCarSeriesOptions.value = res.result || []
      }
    } catch (error: any) {
      if (error.name !== 'CanceledError') {
        allBrandOrCarSeriesOptions.value = []
      }
    }
  }

  const comparativeBriefData = ref<ComparativeBriefVo[]>()
  /**
   * @description: 获取综合对比简报数据
   * @return {*}
   */
  const fetchComparativeBrief = async (requestId?: number) => {
    if (isUnmounted.value) return
    if (!params.firstSelectedCode.value || !params.secondSelectedCode.value) {
      comparativeBriefData.value = undefined
      return
    }

    try {
      const { queryType } = queryStore.value.currentQueryParams
      const codes = [params.firstSelectedCode.value, params.secondSelectedCode.value] as string[]
      const res = await getComparativeBrief({
        ...queryStore.value.currentQueryParams,
        brandCodeList: queryType == QueryType.Brand ? codes : undefined,
        carSeriesList: queryType == QueryType.Series ? codes : undefined
      })
      if (res.success && isLatestComparisonRequest(requestId)) {
        comparativeBriefData.value = res.result
      }
    } catch (error: any) {
      if (error.name !== 'CanceledError' && isLatestComparisonRequest(requestId)) {
        comparativeBriefData.value = undefined
      }
    }
  }

  const trendChangeCompareData = ref<TrendVo[]>()
  /**
   * @description: 趋势变化对比接口
   * @return {*}
   */
  const fetchTrendChangeCompare = async (requestId?: number) => {
    if (isUnmounted.value) return
    if (!params.firstSelectedCode.value || !params.secondSelectedCode.value) {
      trendChangeCompareData.value = undefined
      return
    }

    try {
      const { queryType } = queryStore.value.currentQueryParams
      const codes = [params.firstSelectedCode.value, params.secondSelectedCode.value] as string[]
      const res = await getTrendChangeCompare({
        ...queryStore.value.currentQueryParams,
        brandCodeList: queryType == QueryType.Brand ? codes : undefined,
        carSeriesList: queryType == QueryType.Series ? codes : undefined
      })
      if (res.success && isLatestComparisonRequest(requestId)) {
        trendChangeCompareData.value = res.result
      }
    } catch (error: any) {
      if (error.name !== 'CanceledError' && isLatestComparisonRequest(requestId)) {
        trendChangeCompareData.value = undefined
      }
    }
  }

  const serviceTagAnalysisData = ref<TagAnalysisVo[]>()
  /**
   * @description: 服务对比分析接口
   * @return {*}
   */
  const fetchServiceTagAnalysis = async (requestId?: number) => {
    if (isUnmounted.value) return
    if (!params.firstSelectedCode.value || !params.secondSelectedCode.value) {
      serviceTagAnalysisData.value = undefined
      return
    }

    try {
      const { queryType } = queryStore.value.currentQueryParams
      const codes = [params.firstSelectedCode.value, params.secondSelectedCode.value] as string[]
      const res = await getServiceTagAnalysis({
        ...queryStore.value.currentQueryParams,
        brandCodeList: queryType == QueryType.Brand ? codes : undefined,
        carSeriesList: queryType == QueryType.Series ? codes : undefined,
        dataType: params.serviceReputationDataType.value
      })
      if (res.success && isLatestComparisonRequest(requestId)) {
        serviceTagAnalysisData.value = res.result
      }
    } catch (error: any) {
      if (error.name !== 'CanceledError' && isLatestComparisonRequest(requestId)) {
        serviceTagAnalysisData.value = undefined
      }
    }
  }

  const productTagAnalysisData = ref<TagAnalysisVo[]>()
  /**
   * @description: 产品对比分析接口
   * @return {*}
   */
  const fetchProductTagAnalysis = async (requestId?: number) => {
    if (isUnmounted.value) return
    if (!params.firstSelectedCode.value || !params.secondSelectedCode.value) {
      productTagAnalysisData.value = undefined
      return
    }

    try {
      const { queryType } = queryStore.value.currentQueryParams
      const codes = [params.firstSelectedCode.value, params.secondSelectedCode.value] as string[]
      const res = await getProductTagAnalysis({
        ...queryStore.value.currentQueryParams,
        brandCodeList: queryType == QueryType.Brand ? codes : undefined,
        carSeriesList: queryType == QueryType.Series ? codes : undefined,
        dataType: params.productAnalysisDataType.value
      })
      if (res.success && isLatestComparisonRequest(requestId)) {
        productTagAnalysisData.value = res.result
      }
    } catch (error: any) {
      if (error.name !== 'CanceledError' && isLatestComparisonRequest(requestId)) {
        productTagAnalysisData.value = undefined
      }
    }
  }

  const comparisonDataSourcesData = ref<SourceCompareVo[]>()
  /**
   * @description: 数据来源对比接口
   * @return {*}
   */
  const fetchComparisonDataSources = async (requestId?: number) => {
    if (isUnmounted.value) return
    if (!params.firstSelectedCode.value || !params.secondSelectedCode.value) {
      comparisonDataSourcesData.value = undefined
      return
    }

    try {
      const { queryType } = queryStore.value.currentQueryParams
      const codes = [params.firstSelectedCode.value, params.secondSelectedCode.value] as string[]
      const res = await getComparisonDataSources({
        ...queryStore.value.currentQueryParams,
        brandCodeList: queryType == QueryType.Brand ? codes : undefined,
        carSeriesList: queryType == QueryType.Series ? codes : undefined,
        dataType: params.productAnalysisDataType.value
      })
      if (res.success && isLatestComparisonRequest(requestId)) {
        comparisonDataSourcesData.value = res.result
      }
    } catch (error: any) {
      if (error.name !== 'CanceledError' && isLatestComparisonRequest(requestId)) {
        comparisonDataSourcesData.value = undefined
      }
    }
  }

  const sceneComparisonTopData = ref<SceneComparisonVo[]>([])
  /**
   * @description: 场景对比TOP - 批量调用三次接口（市场均值、本品、竞品）或单独查询某个表格
   * @param index 可选，指定查询第几个表格（0-2），不传则查询全部
   * @param sortParams 可选，排序参数 { prop: 字段名, order: 'asc' | 'desc' }
   * @return {*}
   */
  const fetchSceneComparisonTop = async (
    index?: number,
    sortParams?: { prop: string; order: string },
    requestId?: number
  ) => {
    if (isUnmounted.value) return
    if (!params.firstSelectedCode.value || !params.secondSelectedCode.value) {
      sceneComparisonTopData.value = []
      return
    }

    try {
      const { queryType } = queryStore.value.currentQueryParams
      const codes = [undefined, params.firstSelectedCode.value, params.secondSelectedCode.value]
      const sortField = sortParams?.prop
      const sortOrder = sortParams?.order

      if (index !== undefined && index >= 0 && index < codes.length) {
        const code = codes[index]
        const res = await getSceneComparisonTop({
          ...queryStore.value.currentQueryParams,
          brandCode: queryType == QueryType.Brand ? code : undefined,
          carSeriesCode: queryType == QueryType.Series ? code : undefined,
          sortField,
          sortOrder
        })

        if (res.success && res.result && isLatestComparisonRequest(requestId)) {
          sceneComparisonTopData.value[index] = res.result
        }
      } else {
        const promises = codes.map(code =>
          getSceneComparisonTop({
            ...queryStore.value.currentQueryParams,
            brandCode: queryType == QueryType.Brand ? code : undefined,
            carSeriesCode: queryType == QueryType.Series ? code : undefined,
            sortField,
            sortOrder
          })
        )

        const results = await Promise.all(promises)
        if (isLatestComparisonRequest(requestId)) {
          sceneComparisonTopData.value = results
            .filter(res => res.success && res.result)
            .map(res => res.result as SceneComparisonVo)
        }
      }
    } catch (error: any) {
      if (
        error.name !== 'CanceledError' &&
        index === undefined &&
        isLatestComparisonRequest(requestId)
      ) {
        sceneComparisonTopData.value = []
      }
    }
  }

  const useOpinionComparisonTopData = ref<SceneComparisonVo[]>([])
  /**
   * @description: 用户观点对比TOP - 批量调用三次接口（市场均值、本品、竞品）或单独查询某个表格
   * @param index 可选，指定查询第几个表格（0-2），不传则查询全部
   * @param sentiment 可选，情感参数
   * @return {*}
   */
  const fetchUseOpinionComparisonTop = async (
    index?: number,
    sentiment?: string,
    requestId?: number
  ) => {
    if (isUnmounted.value) return
    if (!params.firstSelectedCode.value || !params.secondSelectedCode.value) {
      useOpinionComparisonTopData.value = []
      return
    }

    try {
      const { queryType } = queryStore.value.currentQueryParams
      const codes = [undefined, params.firstSelectedCode.value, params.secondSelectedCode.value]

      if (index !== undefined && index >= 0 && index < codes.length) {
        const code = codes[index]
        const res = await getUseOpinionComparisonTop({
          ...queryStore.value.currentQueryParams,
          brandCode: queryType == QueryType.Brand ? code : undefined,
          carSeriesCode: queryType == QueryType.Series ? code : undefined,
          sentiment: sentiment || undefined
        })

        if (res.success && res.result && isLatestComparisonRequest(requestId)) {
          useOpinionComparisonTopData.value.splice(index, 1, res.result)
        }
      } else {
        const promises = codes.map(code =>
          getUseOpinionComparisonTop({
            ...queryStore.value.currentQueryParams,
            brandCode: queryType == QueryType.Brand ? code : undefined,
            carSeriesCode: queryType == QueryType.Series ? code : undefined,
            sentiment: sentiment || undefined
          })
        )

        const results = await Promise.all(promises)
        if (isLatestComparisonRequest(requestId)) {
          useOpinionComparisonTopData.value = results
            .filter(res => res.success && res.result)
            .map(res => res.result as SceneComparisonVo)
        }
      }
    } catch (error: any) {
      if (
        error.name !== 'CanceledError' &&
        index === undefined &&
        isLatestComparisonRequest(requestId)
      ) {
        useOpinionComparisonTopData.value = []
      }
    }
  }

  // 品牌车系分类flag标记
  const queryTypeFlag = ref()

  /**
   * @description: 初始化数据（获取默认品牌车系和下拉选项）
   * @return {*}
   */
  const fetchInitialData = async () => {
    // const { queryType } = queryStore.currentQueryParams

    // 获取默认提及最高的品牌车系
    await fetchDefaultHighestBrandCar()
    await getAllBrandOrCarSeriesOptions()

    // 获取下拉选项（只在 dataType 变化时调用）
    // if (queryTypeFlag.value !== queryType) {
    //   await getAllBrandOrCarSeriesOptions()
    //   queryTypeFlag.value = queryType
    // }
  }

  /**
   * @description: 获取对比数据（当选择的品牌/车系变化时调用）
   * @return {*}
   */
  const fetchComparisonData = async () => {
    // 切换品牌/车系时只接受最新一轮响应，避免旧请求回写覆盖新维度。
    const requestId = ++activeComparisonRequest
    comparativeBriefData.value = undefined
    trendChangeCompareData.value = undefined
    serviceTagAnalysisData.value = undefined
    productTagAnalysisData.value = undefined
    sceneComparisonTopData.value = []
    useOpinionComparisonTopData.value = []
    comparisonDataSourcesData.value = undefined

    await Promise.all([
      fetchComparativeBrief(requestId),
      fetchTrendChangeCompare(requestId),
      fetchServiceTagAnalysis(requestId),
      fetchProductTagAnalysis(requestId),
      fetchSceneComparisonTop(undefined, undefined, requestId),
      fetchUseOpinionComparisonTop(undefined, undefined, requestId),
      fetchComparisonDataSources(requestId)
    ])
  }

  return {
    isUnmounted,
    defaultHighestBrandCarData,
    allBrandOrCarSeriesOptions,
    comparativeBriefData,
    trendChangeCompareData,
    serviceTagAnalysisData,
    productTagAnalysisData,
    sceneComparisonTopData,
    useOpinionComparisonTopData,
    comparisonDataSourcesData,

    fetchInitialData,
    fetchComparisonData,
    fetchServiceTagAnalysis,
    fetchProductTagAnalysis,
    fetchSceneComparisonTop,
    fetchUseOpinionComparisonTop,
    fetchComparisonDataSources,
    getAllBrandOrCarSeriesOptions
  }
}
