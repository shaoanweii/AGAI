import { defineStore } from 'pinia'
import { computed, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { getRoleFilterTypeList } from '@/api/common'
import useSceneAnalysisStore from './sceneAnalysis'
import { cloneDeep } from 'lodash-es'
import { getShortcutDateRange, getTimeDimensionByCode } from '@/utils/date'

/**
 * 通用查询逻辑 Store
 * 用于管理系统中所有查询相关的状态和逻辑
 */
export const useQueryStore = defineStore('query', () => {
  // ==================== 状态定义 ====================

  /**
   * 通用查询参数
   * 用于存储通用查询条件
   */
  const commonQueryParams = ref<Record<string, any>>({})

  // 查询条件默认值 存储的key为当前路由的name
  const defaultJsonObjectMap = ref<Record<any, any>>({})
  /**
   * 默认查询参数
   * 基于 VocQueryParams 类型，设置系统默认的查询条件
   */
  const defaultQueryParams = reactive<VocQueryParams>({
    // 基础时间参数
    startDate: dayjs().subtract(7, 'day').format('YYYY-MM-DD'),
    endDate: dayjs().format('YYYY-MM-DD')
    // 分页参数
    // pageNum: 1,
    // pageSize: 20
  })
  // 声音标记高级筛选外部查询条件
  const voiceManagementParams = ref<any>({
    brandCarCodes: [],
    tagCodes: [],
    viewpoint: undefined,
    highQuality: undefined
  })

  const globalShortcutValue = ref()

  /**
   * 当前查询参数
   * 用于存储当前页面的查询条件
   */
  const currentQueryParams = reactive<VocQueryParams>({ ...defaultQueryParams })

  /**
   * 高级筛选类型数据
   */
  const advancedFilterTypeData = ref<any[]>([])

  /**
   * 高级筛选类型数据加载状态
   */
  const isLoadingAdvancedFilterType = ref(false)
  /**
   * 当前路由下的高级筛选项
   */
  const _curPageJsonObject = ref<any>()

  // ==================== 计算属性 ====================

  // const getJsonObjectByRouteName = () => {
  //   return defaultJsonObjectMap.value[route.name]
  // }
  // 获取当前页面的默认筛选项
  const getCutPageJsonObject = computed(() => {
    return _curPageJsonObject.value
  })

  // ==================== 方法定义 ====================

  /**
   * @description: 根据路由获取当前页面的默认值
   * @param {string} routeName
   * @return {*}
   */
  const getCurDefByRouterName = (routeName: string) => {
    return defaultJsonObjectMap.value[routeName]
  }

  /**
   * 更新查询参数
   * @param params 要更新的参数
   * @param isRewrite 是否重写所有参数
   */
  const updateQueryParams = (params: Partial<VocQueryParams>, isRewrite = false) => {
    if (isRewrite) {
      // 移除所有的参数， 然后重新写入参数
      Object.keys(currentQueryParams).forEach(key => {
        delete currentQueryParams[key as keyof VocQueryParams]
      })
      Object.assign(currentQueryParams, params)
    } else {
      Object.assign(currentQueryParams, params)
    }

    // console.log('currentQueryParams',currentQueryParams)
  }

  /**
   * 获取高级筛选类型数据
   * @param data 查询参数
   * @returns 高级筛选类型数据
   */
  const fetchAdvancedFilterTypeList = async (data?: any) => {
    // 如果数据已存在，直接返回
    if (advancedFilterTypeData.value.length > 0) {
      return advancedFilterTypeData.value
    }

    // 如果正在加载中，等待加载完成
    if (isLoadingAdvancedFilterType.value) {
      return new Promise(resolve => {
        const checkLoading = () => {
          if (!isLoadingAdvancedFilterType.value) {
            resolve(advancedFilterTypeData.value)
          } else {
            setTimeout(checkLoading, 50)
          }
        }
        checkLoading()
      })
    }

    isLoadingAdvancedFilterType.value = true

    try {
      const response = await getRoleFilterTypeList(data)
      if (response.success && response.result) {
        advancedFilterTypeData.value = response.result
        return response.result
      }
      return []
    } catch (error) {
      console.error('获取高级筛选类型数据失败:', error)
      return []
    } finally {
      isLoadingAdvancedFilterType.value = false
    }
  }

  /**
   * @description: 从后端返回的菜单中获取所有的筛选项默认值, 使用Record<router.name, JsonObject>的结构存储
   * @param {any} menu
   * @return {*}
   */
  const setDefaultJsonObjectMap = (menu: any[]) => {
    menu?.forEach((item: any) => {
      if (item.children && item.children.length > 0) {
        setDefaultJsonObjectMap(item.children)
      } else {
        // 一级菜单需要单独处理, 把根据一级的path, 将数据存入到children中的name中
        if (item.path === '/overview') {
          defaultJsonObjectMap.value['overview'] = item.jsonObject
        } else if (item.path === '/leaderOverview') {
          defaultJsonObjectMap.value['leaderOverviewPage'] = item.jsonObject
        } else {
          defaultJsonObjectMap.value[item.permissionKey] = item.jsonObject
        }
      }
    })
  }

  const sceneAnalysisStore = useSceneAnalysisStore()

  /**
   * @description: 设置当前页面的默认筛选条件
   * @param {string} routeName
   * @return {*}
   */
  const setPageDefaultFilter = (routeName: string) => {
    // console.log('defaultJsonObjectMap.value', defaultJsonObjectMap.value)
    // 品牌，标签，时间
    const specialFilterType = ['91', '92', '93']
    const curPageJsonObject = cloneDeep(defaultJsonObjectMap.value[routeName])
    _curPageJsonObject.value = curPageJsonObject
    console.log('curPageJsonObject', curPageJsonObject)
    // console.log('currentQueryParams', currentQueryParams)
    // 清空上个页面的高级筛选
    if (currentQueryParams.filterItems) {
      currentQueryParams.filterItems = []
    }
    // 清空上个页面的品牌数据，防止带入到集团分析页面
    if (currentQueryParams.brandCode) {
      currentQueryParams.brandCode = undefined
    }

    // 根据filterType分组
    const specialItems: any[] = []
    const normalItems: any[] = []
    // 根据filterType分组
    if (curPageJsonObject) {
      curPageJsonObject.forEach((item: any) => {
        if (specialFilterType.includes(item.filterType)) {
          specialItems.push(item)
        } else {
          normalItems.push(item)
        }
      })
    }
    const brandCode = ref<any>(undefined)
    const tagCode = ref<any>(undefined)
    // 93: 时间 ， 92： 标签， 91：品牌
    if (specialItems.length) {
      specialItems.forEach((item: any) => {
        if (item.filterType === '93') {
          // 优先使用角色配置的具体日期，如果没有日期则使用 selectedShortcut
          // const [startDate, endDate] = item.value ?? []
          const [code] = item.value ?? [2]

          // globalShortcutValue
          const dimensionItem = getTimeDimensionByCode(code)
          const [startDate, endDate] = getShortcutDateRange(code)
          globalShortcutValue.value = dimensionItem?.name

          if (startDate && endDate) {
            updateQueryParams({
              startDate,
              endDate
            })
          }
          // 如果角色配置中既没有具体日期也没有 selectedShortcut，则保持默认值
        }
        if (item.filterType === '92') {
          tagCode.value = item.value
          const length = item.value?.length
          const [tag1, tag2] = item.value ?? []

          updateQueryParams({
            tag1Code: length === 1 ? (tag1 === 'all' ? undefined : tag1) : undefined,
            tag2Code: length === 2 ? tag2 : undefined
          })
        }
        if (item.filterType === '91') {
          if (item.value) {
            brandCode.value = item.value
            const [_brandCode, carSeriesCode] = item.value ?? []
            const { length } = item.value ?? []

            updateQueryParams({
              // 仅 length=1 时赋值_brandCode，否则 undefined
              brandCode: length === 1 ? _brandCode : undefined,
              // 仅 length=2 时赋值 carSeriesCode，否则 undefined
              carSeriesCode: length === 2 ? carSeriesCode : undefined
            })
          } else {
            brandCode.value = undefined
            updateQueryParams({
              brandCode: undefined,
              carSeriesCode: undefined
            })
          }
        }
      })
    } else {
      updateQueryParams({
        tag2Code: undefined,
        tag1Code: undefined,
        brandCode: undefined,
        carSeriesCode: undefined
      })
    }
    if (normalItems.length) {
      updateQueryParams({
        filterItems: normalItems
      })
    }
    // 如果是详情页面, 需要将详情页面的筛选项带入当前页面
    const { startDate, endDate, selectedShortcut, formData, customTimes } =
      sceneAnalysisStore.getDetailFilter
    if (!sceneAnalysisStore.detailFlag) {
      updateQueryParams({
        startDate,
        endDate
      })

      // 报告条件只更新当前查询上下文，不写入普通页面公共缓存。
      if (formData && Object.keys(formData).length > 0) {
        setUniversaFilterFormData(formData, customTimes || [])
      }
    }

    return {
      filterItems: normalItems,
      brandCode: brandCode.value,
      tagCode: tagCode.value,
      selectedShortcut
    }
  }

  // ==================== 所有场景页筛选条件通用 ====================
  // 缓存场景页面中上个页面的查询条件， 用于带入下个页面
  const cacheQueryOfParams = ref<any>({})
  // 设置缓存场景页面中上个页面的查询条件
  const setCacheQueryOfParams = (params: any) => {
    cacheQueryOfParams.value = cloneDeep(params)
  }
  // 获取缓存场景页面中上个页面的查询条件
  const getCacheQueryOfParams = computed(() => cacheQueryOfParams.value)

  // ==================== UniversaFilter 组件查询条件缓存 ====================
  /**
   * UniversaFilter 组件查询条件缓存
   * 所有页面的查询条件字段都是一样的，只需要缓存条件的值
   */
  const universaFilterCancheSearchParams = ref<Record<string, Record<string, any>>>({})
  const DEFAULT_UNIVERSA_FILTER_CACHE_KEY = '__default__'

  /**
   * UniversaFilter 组件的原始 formData（未处理的数据）
   * 用于发布报告弹窗回显
   */
  const universaFilterFormData = ref<Record<string, any>>({})

  /**
   * UniversaFilter 组件的 customTimes（自定义时间范围）
   * 用于发布报告弹窗回显
   */
  const universaFilterCustomTimes = ref<string[]>([])

  /**
   * 设置 UniversaFilter 组件的查询条件缓存
   * @param params 查询条件
   * @param cacheKey 缓存 key（用于同页面多个筛选器或多页面隔离）
   */
  const setUniversaFilterCacheSearchParams = (
    params: any,
    cacheKey: string = DEFAULT_UNIVERSA_FILTER_CACHE_KEY
  ) => {
    universaFilterCancheSearchParams.value[cacheKey] = cloneDeep(params || {})
  }

  /**
   * 获取 UniversaFilter 组件的查询条件缓存
   * @param cacheKey 缓存 key（用于同页面多个筛选器或多页面隔离）
   * @returns 缓存的查询条件，如果没有则返回 undefined
   */
  const getUniversaFilterCacheSearchParams = (
    cacheKey: string = DEFAULT_UNIVERSA_FILTER_CACHE_KEY
  ) => {
    return universaFilterCancheSearchParams.value[cacheKey] || {}
  }

  /**
   * 清除 UniversaFilter 组件的查询条件缓存
   * @param cacheKey 缓存 key，不传则清空全部
   */
  const clearUniversaFilterCacheSearchParams = (cacheKey?: string) => {
    if (cacheKey) {
      delete universaFilterCancheSearchParams.value[cacheKey]
      return
    }
    universaFilterCancheSearchParams.value = {}
  }

  /**
   * 设置 UniversaFilter 组件的原始 formData
   * @param formData 原始表单数据
   * @param customTimes 自定义时间范围
   */
  const setUniversaFilterFormData = (formData: Record<string, any>, customTimes: string[] = []) => {
    universaFilterFormData.value = cloneDeep(formData)
    universaFilterCustomTimes.value = cloneDeep(customTimes)
  }

  /**
   * 获取 UniversaFilter 组件的原始 formData
   * @returns 原始表单数据
   */
  const getUniversaFilterFormData = () => {
    return {
      formData: universaFilterFormData.value,
      customTimes: universaFilterCustomTimes.value
    }
  }

  // ==================== 竞品对比数据缓存 ====================
  /**
   * 竞品对比数据缓存
   * 用于存储品牌车系分类、本品代码、竞品代码
   */
  const competitorAnalysisData = ref<{
    queryType?: string // 品牌车系分类：'brand' 或 'series'
    firstSelectedCode?: string // 本品代码
    secondSelectedCode?: string // 竞品代码
    firstSelectedName?: string // 本品名称
    secondSelectedName?: string // 竞品名称
  }>({})

  /**
   * 设置竞品对比数据
   * @param data 竞品对比数据
   */
  const setCompetitorAnalysisData = (data: {
    queryType?: string
    firstSelectedCode?: string
    secondSelectedCode?: string
    firstSelectedName?: string
    secondSelectedName?: string
  }) => {
    competitorAnalysisData.value = { ...data }
  }

  /**
   * 获取竞品对比数据
   * @returns 竞品对比数据
   */
  const getCompetitorAnalysisData = () => {
    return competitorAnalysisData.value
  }

  /**
   * 清除竞品对比数据
   */
  const clearCompetitorAnalysisData = () => {
    competitorAnalysisData.value = {}
  }

  // ==================== 返回 Store 接口 ====================

  return {
    // 状态
    defaultQueryParams,
    currentQueryParams,
    advancedFilterTypeData,
    getCutPageJsonObject,
    voiceManagementParams,
    globalShortcutValue,
    commonQueryParams,
    universaFilterCancheSearchParams,
    universaFilterFormData,
    universaFilterCustomTimes,
    competitorAnalysisData,

    // 方法
    updateQueryParams,
    fetchAdvancedFilterTypeList,
    setDefaultJsonObjectMap,
    setPageDefaultFilter,
    setCacheQueryOfParams,
    getCacheQueryOfParams,
    getCurDefByRouterName,
    setUniversaFilterCacheSearchParams,
    getUniversaFilterCacheSearchParams,
    clearUniversaFilterCacheSearchParams,
    setUniversaFilterFormData,
    getUniversaFilterFormData,
    setCompetitorAnalysisData,
    getCompetitorAnalysisData,
    clearCompetitorAnalysisData
  }
})

export default useQueryStore
