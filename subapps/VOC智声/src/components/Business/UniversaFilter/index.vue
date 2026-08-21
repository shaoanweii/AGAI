<script setup lang="ts">
import { ref, computed, nextTick, onMounted, watch, watchEffect, onBeforeUnmount } from 'vue'
import { getFilterConfig, formatFilterTags } from './helper'
import { EXPERIENCE_CODE_SAME_LEVEL_WARNING } from './constants'
import type { FilterFieldConfig } from './types'
import BrandSelector from './components/BrandSelector.vue'
import SeriesSelector from './components/SeriesSelector.vue'
import ExperienceCodeSelector from './components/ExperienceCodeSelector.vue'
import ExperienceCodeLinkageSelector from './components/ExperienceCodeLinkageSelector.vue'
import ExperienceCodeHotSelector from './components/ExperienceCodeHotSelector.vue'

import DatePicker from './components/DatePicker.vue'
import SelectV2WithSelectAll from './components/SelectV2WithSelectAll.vue'
import DataSourceCascader from '../AdvancedFilter/DataSourceCascader.vue'
import { useQueryStore } from '@/store/modules/query'
import { useUserStore } from '@/store'
import useSceneAnalysisStore from '@/store/modules/sceneAnalysis'
import { getDrillDownConditions, type DrillDownConditionItem } from '@/api/drillDownDialog'
import { getShortcutDateRange, getTimeDimensionByCode } from '@/utils/date'
import {
  getUserChannelTree,
  findFinalTagLibClientVoListByTagId,
  getMainAccTreeData,
  findAllAttributeLabelList
} from '@/api/common'
import { TagType } from '@/constants'
import { onBeforeRouteLeave, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getHotEvDetail } from '@/api/hotAphttp'
import { getQueryDataByid } from '@/api/subscribeReport'
import { cloneDeep } from 'lodash-es'
import { getDataPlazaConditions } from '@/api/dataPlaza'
import type { DataPlazaConditionGroup, DataPlazaConditionOption } from '@/api/dataPlaza/types'

defineOptions({
  name: 'UniversaFilter'
})

interface Props {
  routeName: string // 路由名称
  cacheKey?: string // 查询条件缓存 key（用于同页面多个筛选器隔离）
  newCarSeriesOptions?: any[] // 新品车系选项
  competitiveTreeOptions?: any[] // 竞品品牌车系选项
  mainAccList?: any[] // 重点帐号
}

interface Emits {
  (
    e: 'search',
    value: Record<string, any>,
    tagPath?: Array<{ code: string; name: string; level?: number }>
  ): void
  (e: 'reset'): void
}

const props = defineProps<Props>()

const emit = defineEmits<Emits>()

const queryStore = useQueryStore()
const userStore = useUserStore()
const sceneAnalysisStore = useSceneAnalysisStore()
const route = useRoute()

// 全局共享缓存的 key（所有页面统一使用）
const GLOBAL_SHARED_CACHE_KEY = '__default__'
const TIME_ONLY_SHARED_ROUTE_NAMES = [
  'rootCause',
  'ResultData',
  'OriginalData',
  'SentimentBranchData'
]
const ORIGINAL_SOUND_QUERY_TAB_ROUTE_NAMES = ['ResultData', 'OriginalData', 'SentimentBranchData']
const SAME_LEVEL_EXPERIENCE_CODE_ROUTE_NAMES = [
  'rootCause',
  'ResultData',
  'productAnalysis',
  'serviceAnalysis',
  'competitorAnalysis'
]
const ORIGINAL_SOUND_QUERY_SHARED_CACHE_KEY = 'OriginalSoundQuery_Shared'
const USAGE_SCENARIO_PROP = 'usageScenarioCodes'
const SHARED_TIME_CACHE_KEYS = ['startDate', 'endDate', 'dateRange', 'globalShortcutValue'] as const
type SharedTimeCacheKey = (typeof SHARED_TIME_CACHE_KEYS)[number]

const isTimeOnlySharedRoute = computed(() => TIME_ONLY_SHARED_ROUTE_NAMES.includes(props.routeName))
const isOriginalSoundQueryTabRoute = computed(() =>
  ORIGINAL_SOUND_QUERY_TAB_ROUTE_NAMES.includes(props.routeName)
)
const shouldLimitExperienceCodeSameLevel = computed(() =>
  SAME_LEVEL_EXPERIENCE_CODE_ROUTE_NAMES.includes(props.routeName)
)
// 报告条件只允许在当前携带详情标识的路由中恢复，避免 Pinia 残留状态串到普通页面。
const isReportDetailPage = computed(
  () =>
    sceneAnalysisStore.sceneOriginData.isDetail &&
    (route.query.isBack === '1' || Boolean(route.query.reportJudgeId))
)
const isolatedCacheKey = computed(() =>
  isTimeOnlySharedRoute.value && props.cacheKey ? props.cacheKey : ''
)

const isExpanded = ref(false)
const formData = ref<Record<string, any>>({})
const datePickerRef = ref<any>(null)
const experienceCodeSelectorRef = ref<any>(null) // 客户体验代码选择器引用
const customTimes = ref<string[]>([])
const channelOptions = ref<any[]>([]) // 数据源选项

const mainAccOptions = ref<any[]>([]) // 重点账号选项

const experienceCodeTypeOptions = ref<Array<{ key: string; value: string }>>([]) // 体验代码类型选项
const localCompetitiveTreeOptions = ref<any[]>([]) // 竞品品牌车系选项
const localNewCarSeriesOptions = ref<any[]>([]) // 新品车系选项
const localUsageScenarioOptions = ref<DataPlazaConditionOption[]>([]) // 用车场景选项
const filterTags = ref<string[]>([]) // 筛选标签列表
const standardViewpointOptions = ref<any[]>([]) // 标准观点选项
const attributeTagOptions = ref<any[]>([]) // 属性标签选项
const experienceCodeNames = ref<string[]>([]) // 客户体验代码每一级的中文名称
const hasLoadedDrillDownConditions = ref(false)
const hasLoadedUsageScenarioOptions = ref(false)
const isInitializingFormData = ref(false) // 标记表单初始化/缓存回填阶段，避免误触发竞品品牌联动
const COMPETITIVE_BRAND_PROP = 'compBrandCodeList'
const COMPETITIVE_SERIES_PROP = 'compCarSeriesList'
const isFormDataInitialized = ref(false) // 初始化表单数据的标志，避免重复初始化
const pendingDynamicDefaultProps = ref<string[]>([]) // 等待异步数据源补齐的动态默认值字段
const pendingRouteBrandCode = ref<string | null>(null) // 等待品牌数据加载后应用的路由品牌参数

const hotDetailData = ref<any>(null) // 热点事件 详情数据 用于回显对应的条件
const loadingDetailLoading = ref(false) // 详情数据加载中

// 根据路由名称获取配置
const config = computed(() => getFilterConfig(props.routeName))

// 获取当前页面的体验代码字段配置（兼容旧版/联动版）
const getExperienceCodeField = () => {
  return config.value.find(field =>
    ['experienceCode', 'experienceCodeLinkage'].includes(field.type as any)
  )
}

/**
 * 获取当前页面的竞品品牌字段配置
 * @returns 竞品品牌字段配置
 */
const getCompetitiveBrandField = () => {
  return config.value.find(
    field => field.type === 'selectv2' && field.prop === COMPETITIVE_BRAND_PROP
  )
}

/**
 * 获取当前页面的竞品车字段配置（双下拉模式）
 * @returns 竞品车系字段配置
 */
const getCompetitiveSeriesField = () => {
  return config.value.find(
    field => field.type === 'selectv2' && field.prop === COMPETITIVE_SERIES_PROP
  )
}

/**
 * 构造竞品品牌车系树的辅助级联配置
 * 仅在结果数据/原始数据页用于兼容旧缓存中的级联值结构。
 * @param valueKey 当前页面期望的取值字段
 * @returns 级联辅助配置
 */
const createCompetitiveTreeHelperField = (valueKey = 'key'): FilterFieldConfig => {
  return {
    type: 'cascader',
    prop: COMPETITIVE_SERIES_PROP,
    multiple: true,
    options: [],
    cascaderProps: {
      value: valueKey,
      label: 'value',
      children: 'children',
      multiple: true,
      emitPath: true,
      checkStrictly: true
    }
  }
}

/**
 * 获取当前页面的竞品品牌车系树字段配置
 * 若页面仍使用级联配置则直接返回；双下拉页面返回辅助配置，用于兼容旧缓存与值转换。
 * @returns 竞品品牌车系树字段配置
 */
const getCompetitiveTreeField = () => {
  const cascaderField = config.value.find(
    field => field.type === 'cascader' && field.prop === COMPETITIVE_SERIES_PROP
  )
  if (cascaderField) {
    return cascaderField
  }

  const competitiveSeriesField = getCompetitiveSeriesField()
  if (!competitiveSeriesField) {
    return undefined
  }

  return createCompetitiveTreeHelperField(competitiveSeriesField.props?.value || 'key')
}

// 获取当前页面的新品车系字段配置
const getNewCarSeriesField = () => {
  return config.value.find(field => field.type === 'cascader' && field.prop === 'newCarSeriesList')
}

// 获取当前页面的用车场景字段配置
const getUsageScenarioField = () => {
  return config.value.find(field => field.type === 'cascader' && field.prop === USAGE_SCENARIO_PROP)
}

// 当前页面是否需要加载数据广场用车场景
const shouldLoadUsageScenarioOptions = computed(() => !!getUsageScenarioField())

// 当前页面是否需要加载 /report/drill-down/conditions
const shouldLoadDrillDownConditions = computed(() => {
  const experienceCodeField = getExperienceCodeField()
  const hasCompetitiveField =
    !!getCompetitiveTreeField() || !!getCompetitiveBrandField() || !!getCompetitiveSeriesField()

  return (
    hasCompetitiveField ||
    !!getNewCarSeriesField() ||
    (experienceCodeField?.type === 'experienceCodeLinkage' &&
      !(experienceCodeField as any).fixedTagType)
  )
})

/**
 * 根据 key 获取指定的下钻筛选条件项
 * @param conditions 下钻筛选条件列表
 * @param key 条件 key
 * @returns 匹配到的条件项
 */
const findDrillDownConditionByKey = (conditions: DrillDownConditionItem[], key: string) => {
  return conditions.find(item => item?.key === key)
}

// 收集当前页面所有需要参与“取值/缓存/回显”的字段 key（兼容体验代码联动版的额外 tagType 字段）
const getCurrentPagePropKeys = () => {
  const keys: string[] = []
  config.value.forEach((field: any) => {
    if (field.type === 'placeholder') return
    if (field.prop) keys.push(field.prop)
    if (field.type === 'experienceCodeLinkage' && field.tagTypeProp && !field.fixedTagType) {
      keys.push(field.tagTypeProp)
    }
  })
  return new Set(keys)
}

/**
 * 获取指定路由的筛选字段 key 集合
 * @param routeName 路由名称
 * @returns 路由对应的字段 key 集合
 */
const getPagePropKeysByRouteName = (routeName: string) => {
  const keys: string[] = []
  getFilterConfig(routeName).forEach((field: any) => {
    if (field.type === 'placeholder') return
    if (field.prop) keys.push(field.prop)
    if (field.type === 'experienceCodeLinkage' && field.tagTypeProp && !field.fixedTagType) {
      keys.push(field.tagTypeProp)
    }
  })
  return new Set(keys)
}

/**
 * originalSoundQuery 各 tab 的公共字段集合
 * 仅同步所有页签都存在的同名字段；时间字段仍走全局时间缓存，不在这里重复维护。
 */
const originalSoundQuerySharedFieldKeys = computed(() => {
  const [firstTabKeys = new Set<string>(), ...restTabKeys] =
    ORIGINAL_SOUND_QUERY_TAB_ROUTE_NAMES.map(routeName => getPagePropKeysByRouteName(routeName))

  return new Set(
    [...firstTabKeys].filter(
      key => key !== 'dateRange' && restTabKeys.every(tabKeys => tabKeys.has(key))
    )
  )
})

/**
 * 判断字段是否属于公共时间缓存字段
 * @param key 字段名
 * @returns 是否为时间缓存字段
 */
const isSharedTimeCacheKey = (key: string): key is SharedTimeCacheKey => {
  return SHARED_TIME_CACHE_KEYS.includes(key as SharedTimeCacheKey)
}

/**
 * 获取全局共享缓存
 * @returns 全局共享缓存对象
 */
const getGlobalSharedCacheParams = () => {
  return queryStore.getUniversaFilterCacheSearchParams(GLOBAL_SHARED_CACHE_KEY) || {}
}

/**
 * 获取当前页签的私有缓存
 * @returns 当前页签私有缓存对象
 */
const getIsolatedCacheParams = () => {
  return isolatedCacheKey.value
    ? queryStore.getUniversaFilterCacheSearchParams(isolatedCacheKey.value) || {}
    : {}
}

/**
 * 获取 originalSoundQuery 页内共享缓存
 * @returns 页内共享缓存对象
 */
const getOriginalSoundQuerySharedCacheParams = () => {
  return queryStore.getUniversaFilterCacheSearchParams(ORIGINAL_SOUND_QUERY_SHARED_CACHE_KEY) || {}
}

/**
 * 判断缓存中是否存在可用的时间范围
 * @param cachedParams 缓存对象
 * @returns 是否包含开始/结束时间
 */
const hasCachedTimeRange = (cachedParams: Record<string, any>) => {
  return !!(cachedParams?.startDate && cachedParams?.endDate)
}

/**
 * 清理全局共享缓存中的时间字段
 * 仅用于 rootCause / originalSoundQuery 相关页面，避免它们把非时间条件带入公共缓存逻辑。
 */
const clearSharedTimeCache = () => {
  const nextCache = { ...getGlobalSharedCacheParams() }
  SHARED_TIME_CACHE_KEYS.forEach(key => {
    delete nextCache[key]
  })
  queryStore.setUniversaFilterCacheSearchParams(nextCache, GLOBAL_SHARED_CACHE_KEY)
}

/**
 * 清理当前页签的私有缓存
 * 用于 originalSoundQuery 离页时移除 tab 内筛选态，避免跨页面再次进入时串值。
 */
const clearIsolatedCache = () => {
  if (!isolatedCacheKey.value) {
    return
  }
  queryStore.clearUniversaFilterCacheSearchParams(isolatedCacheKey.value)
}

/**
 * 清理 originalSoundQuery 页内共享缓存
 */
const clearOriginalSoundQuerySharedCache = () => {
  queryStore.clearUniversaFilterCacheSearchParams(ORIGINAL_SOUND_QUERY_SHARED_CACHE_KEY)
}

/**
 * 将字段值统一规范为数组，便于多选/单选逻辑复用
 * @param value 原始字段值
 * @returns 过滤空值后的数组
 */
const normalizeFieldValues = (value: unknown): any[] => {
  if (Array.isArray(value)) {
    return value.filter(item => item !== '' && item !== null && item !== undefined)
  }
  if (value === '' || value === null || value === undefined) {
    return []
  }
  return [value]
}

/**
 * 判断品牌字段是否为空
 * @param value 品牌值
 * @returns 是否为空
 */
const isEmptyBrandValue = (value: unknown) => normalizeFieldValues(value).length === 0

/**
 * 比较候选值是否匹配当前值
 * @param candidates 候选值列表
 * @param currentValue 当前值
 * @returns 是否匹配
 */
const isMatchedValue = (candidates: unknown[], currentValue: unknown) => {
  return candidates.some(
    candidate => candidate !== undefined && String(candidate) === String(currentValue)
  )
}

/**
 * 获取品牌字段配置
 * @returns 品牌字段配置
 */
const getBrandField = () => config.value.find(field => field.type === 'brand')

/**
 * 获取车系字段配置
 * @returns 车系字段配置
 */
const getSeriesField = () => config.value.find(field => field.type === 'series')

/**
 * 根据当前字段值匹配品牌节点
 * @param brandValue 品牌值
 * @param brandField 品牌字段配置
 * @returns 匹配到的品牌节点列表
 */
const getMatchedBrandOptions = (brandValue: unknown, brandField = getBrandField()) => {
  if (!brandField) {
    return []
  }

  const values = normalizeFieldValues(brandValue)
  const valueKey = brandField.props?.value || 'key'

  return (userStore.getBrandService || []).filter((item: any) =>
    values.some(currentValue =>
      isMatchedValue([item[valueKey], item.key, item.value, item.brandCode], currentValue)
    )
  )
}

/**
 * 根据已选品牌聚合可用车系列表
 * @param brandValue 品牌值
 * @param brandField 品牌字段配置
 * @returns 聚合去重后的车系列表
 */
const getMergedSeriesOptions = (brandValue: unknown, brandField = getBrandField()) => {
  const uniqueSeriesMap = new Map<string, any>()

  getMatchedBrandOptions(brandValue, brandField).forEach((brand: any) => {
    ;(brand.children || []).forEach((series: any) => {
      const uniqueKey = String(series.key ?? series.value ?? '')
      if (uniqueKey && !uniqueSeriesMap.has(uniqueKey)) {
        uniqueSeriesMap.set(uniqueKey, series)
      }
    })
  })

  return Array.from(uniqueSeriesMap.values())
}

/**
 * 将品牌值转换为当前字段使用的取值格式（兼容 key/value）
 * @param value 原始值
 * @param brandField 品牌字段配置
 * @returns 转换后的品牌值
 */
const resolveBrandFieldValue = (value: unknown, brandField = getBrandField()) => {
  if (!brandField) {
    return value
  }

  const valueKey = brandField.props?.value || 'key'
  const values = normalizeFieldValues(value)
  const matchedBrands = getMatchedBrandOptions(values, brandField)
  const resolvedValues = values
    .map(currentValue => {
      const matchedBrand = matchedBrands.find((item: any) =>
        isMatchedValue([item[valueKey], item.key, item.value, item.brandCode], currentValue)
      )
      return matchedBrand ? matchedBrand[valueKey] : currentValue
    })
    .filter(item => item !== '' && item !== null && item !== undefined)

  const uniqueValues = Array.from(new Set(resolvedValues))
  return brandField.multiple ? uniqueValues : (uniqueValues[0] ?? '')
}

/**
 * 将车系值转换为当前字段使用的取值格式（兼容 key/value）
 * @param value 原始值
 * @param brandValue 当前品牌值
 * @param seriesField 车系字段配置
 * @param brandField 品牌字段配置
 * @returns 转换后的车系值
 */
const resolveSeriesFieldValue = (
  value: unknown,
  brandValue: unknown,
  seriesField = getSeriesField(),
  brandField = getBrandField()
) => {
  if (!seriesField) {
    return value
  }

  const valueKey = seriesField.props?.value || 'key'
  const values = normalizeFieldValues(value)
  const mergedSeriesOptions = getMergedSeriesOptions(brandValue, brandField)
  const resolvedValues = values
    .map(currentValue => {
      const matchedSeries = mergedSeriesOptions.find((item: any) =>
        isMatchedValue([item[valueKey], item.key, item.value], currentValue)
      )
      return matchedSeries ? matchedSeries[valueKey] : currentValue
    })
    .filter(item => item !== '' && item !== null && item !== undefined)

  return Array.from(new Set(resolvedValues))
}

/**
 * 根据当前字段值匹配竞品品牌节点
 * @param brandValue 竞品品牌值
 * @param brandField 竞品品牌字段配置
 * @param options 竞品品牌车系树
 * @returns 匹配到的竞品品牌节点列表
 */
const getMatchedCompetitiveBrandOptions = (
  brandValue: unknown,
  brandField = getCompetitiveBrandField(),
  options = localCompetitiveTreeOptions.value
) => {
  if (!brandField || !Array.isArray(options) || options.length === 0) {
    return []
  }

  const values = normalizeFieldValues(brandValue)
  const valueKey = brandField.props?.value || 'key'

  return options.filter((item: any) =>
    values.some(currentValue =>
      isMatchedValue([item[valueKey], item.key, item.value, item.code], currentValue)
    )
  )
}

/**
 * 根据已选竞品品牌聚合可用竞品车系列表
 * @param brandValue 竞品品牌值
 * @param brandField 竞品品牌字段配置
 * @param seriesField 竞品车系字段配置
 * @param options 竞品品牌车系树
 * @param fallbackToAllBrands 未选品牌时是否兜底使用全量品牌
 * @returns 聚合去重后的竞品车系列表
 */
const getMergedCompetitiveSeriesOptions = (
  brandValue: unknown,
  brandField = getCompetitiveBrandField(),
  seriesField = getCompetitiveSeriesField(),
  options = localCompetitiveTreeOptions.value,
  fallbackToAllBrands = false
) => {
  if (!seriesField || !Array.isArray(options) || options.length === 0) {
    return []
  }

  const matchedBrands = getMatchedCompetitiveBrandOptions(brandValue, brandField, options)
  const sourceBrands = matchedBrands.length > 0 ? matchedBrands : fallbackToAllBrands ? options : []
  const uniqueSeriesMap = new Map<string, any>()
  const seriesValueKey = seriesField.props?.value || 'key'

  sourceBrands.forEach((brand: any) => {
    ;(brand.children || []).forEach((series: any) => {
      const uniqueKey = String(series?.[seriesValueKey] ?? series?.key ?? series?.value ?? '')
      if (uniqueKey && !uniqueSeriesMap.has(uniqueKey)) {
        uniqueSeriesMap.set(uniqueKey, series)
      }
    })
  })

  return Array.from(uniqueSeriesMap.values())
}

/**
 * 将竞品品牌值转换为当前字段使用的取值格式（兼容 key/value）
 * @param value 原始值
 * @param brandField 竞品品牌字段配置
 * @param options 竞品品牌车系树
 * @param targetValueKey 目标取值字段，默认使用当前字段 value 配置
 * @returns 转换后的竞品品牌值
 */
const resolveCompetitiveBrandFieldValue = (
  value: unknown,
  brandField = getCompetitiveBrandField(),
  options = localCompetitiveTreeOptions.value,
  targetValueKey?: string
) => {
  if (!brandField) {
    return normalizeFieldValues(value)
  }

  const resolvedTargetValueKey = targetValueKey || brandField.props?.value || 'key'
  const values = normalizeFieldValues(value)
  const matchedBrands = getMatchedCompetitiveBrandOptions(values, brandField, options)
  const resolvedValues = values
    .map(currentValue => {
      const matchedBrand = matchedBrands.find((item: any) =>
        isMatchedValue(
          [item[resolvedTargetValueKey], item.key, item.value, item.brandCode],
          currentValue
        )
      )
      return matchedBrand ? matchedBrand[resolvedTargetValueKey] : currentValue
    })
    .filter(item => item !== '' && item !== null && item !== undefined)

  return Array.from(new Set(resolvedValues))
}

/**
 * 将竞品车系值转换为当前字段使用的取值格式（兼容 key/value）
 * @param value 原始值
 * @param brandValue 当前竞品品牌值
 * @param seriesField 竞品车系字段配置
 * @param brandField 竞品品牌字段配置
 * @param options 竞品品牌车系树
 * @param fallbackToAllBrands 未选品牌时是否兜底使用全量品牌
 * @param targetValueKey 目标取值字段，默认使用当前字段 value 配置
 * @returns 转换后的竞品车系值
 */
const resolveCompetitiveSeriesFieldValue = (
  value: unknown,
  brandValue: unknown,
  seriesField = getCompetitiveSeriesField(),
  brandField = getCompetitiveBrandField(),
  options = localCompetitiveTreeOptions.value,
  fallbackToAllBrands = false,
  targetValueKey?: string
) => {
  if (!seriesField) {
    return normalizeFieldValues(value)
  }

  const resolvedTargetValueKey = targetValueKey || seriesField.props?.value || 'key'
  const values = normalizeFieldValues(value)
  const mergedSeriesOptions = getMergedCompetitiveSeriesOptions(
    brandValue,
    brandField,
    seriesField,
    options,
    fallbackToAllBrands
  )
  const resolvedValues = values
    .map(currentValue => {
      const matchedSeries = mergedSeriesOptions.find((item: any) =>
        isMatchedValue([item[resolvedTargetValueKey], item.key, item.value], currentValue)
      )
      return matchedSeries ? matchedSeries[resolvedTargetValueKey] : currentValue
    })
    .filter(item => item !== '' && item !== null && item !== undefined)

  return Array.from(new Set(resolvedValues))
}

/**
 * 按当前竞品品牌筛掉不再有效的竞品车系。
 * 仅保留仍属于剩余品牌集合的车系；当品牌被清空时，车系同步清空。
 * @param seriesValue 当前竞品车系值
 * @param brandValue 当前竞品品牌值
 * @param seriesField 竞品车系字段配置
 * @param brandField 竞品品牌字段配置
 * @param options 竞品品牌车系树
 * @returns 过滤后的竞品车系值数组
 */
const filterCompetitiveSeriesByBrand = (
  seriesValue: unknown,
  brandValue: unknown,
  seriesField = getCompetitiveSeriesField(),
  brandField = getCompetitiveBrandField(),
  options = localCompetitiveTreeOptions.value
) => {
  if (!seriesField) {
    return normalizeFieldValues(seriesValue)
  }

  const values = normalizeFieldValues(seriesValue)
  if (values.length === 0) {
    return []
  }

  const mergedSeriesOptions = getMergedCompetitiveSeriesOptions(
    brandValue,
    brandField,
    seriesField,
    options,
    false
  )
  if (mergedSeriesOptions.length === 0) {
    return []
  }

  const resolvedTargetValueKey = seriesField.props?.value || 'key'
  return Array.from(
    new Set(
      values.filter(currentValue =>
        mergedSeriesOptions.some((item: any) =>
          isMatchedValue([item[resolvedTargetValueKey], item.key, item.value], currentValue)
        )
      )
    )
  )
}

/**
 * 统一获取 select-v2 字段的数据源
 * @param field 字段配置
 * @returns 当前字段可用的选项列表
 */
const getSelectV2Options = (field: any) => {
  if (field.prop === 'topicCodes') {
    return standardViewpointOptions.value
  }
  if (field.prop === COMPETITIVE_BRAND_PROP) {
    return localCompetitiveTreeOptions.value
  }
  if (field.prop === COMPETITIVE_SERIES_PROP) {
    return getMergedCompetitiveSeriesOptions(competitiveBrandValue.value)
  }
  return field.options || []
}

/**
 * 获取 select-v2 字段的占位文案
 * @param field 字段配置
 * @returns 占位文案
 */
const getSelectV2Placeholder = (field: any) => {
  if (field.prop === COMPETITIVE_SERIES_PROP && !isCompetitiveBrandSelected.value) {
    return '请先选择竞品品牌'
  }
  return field.placeholder
}

/**
 * 判断 select-v2 字段是否禁用
 * @param field 字段配置
 * @returns 是否禁用
 */
const isSelectV2Disabled = (field: any) => {
  if (field.prop === COMPETITIVE_SERIES_PROP) {
    return Boolean(field.disabled) || !isCompetitiveBrandSelected.value
  }
  return Boolean(field.disabled)
}

/**
 * 获取级联字段的关键配置
 * @param field 级联字段配置
 * @returns 当前级联字段的 value/children 键名
 */
const getCascaderFieldKeys = (field = getCompetitiveTreeField()) => {
  return {
    valueKey: field?.cascaderProps?.value || 'key',
    childrenKey: field?.cascaderProps?.children || 'children'
  }
}

/**
 * 递归查找级联节点，兼容 key/value/code 多种取值格式
 * @param options 级联选项树
 * @param targetValue 待匹配的值
 * @param field 级联字段配置
 * @returns 匹配到的节点
 */
const findMatchedCascaderNode = (
  options: any[],
  targetValue: unknown,
  field = getCompetitiveTreeField()
): any | null => {
  if (!field || !Array.isArray(options) || options.length === 0) {
    return null
  }

  if (targetValue === '' || targetValue === null || targetValue === undefined) {
    return null
  }

  const { valueKey, childrenKey } = getCascaderFieldKeys(field)

  for (const option of options) {
    if (
      isMatchedValue([option?.[valueKey], option?.key, option?.value, option?.code], targetValue)
    ) {
      return option
    }

    const children = Array.isArray(option?.[childrenKey]) ? option[childrenKey] : []
    const matchedChild = findMatchedCascaderNode(children, targetValue, field)
    if (matchedChild) {
      return matchedChild
    }
  }

  return null
}

/**
 * 获取指定级联字段当前应使用的选项树
 * @param field 级联字段配置
 * @returns 当前字段对应的级联选项
 */
const getCascaderOptionsByField = (field?: FilterFieldConfig) => {
  if (!field) {
    return []
  }

  if (field.prop === 'compCarSeriesList') {
    return localCompetitiveTreeOptions.value
  }

  if (field.prop === 'newCarSeriesList') {
    return localNewCarSeriesOptions.value
  }

  if (field.prop === 'keyAccounts') {
    return mainAccOptions.value
  }

  if (field.prop === USAGE_SCENARIO_PROP) {
    return localUsageScenarioOptions.value
  }

  return Array.isArray(field.options) ? field.options : []
}

/**
 * 将级联字段值转换为目标字段使用的取值格式
 * @param value 原始值
 * @param field 级联字段配置
 * @param options 级联选项树
 * @param targetValueKey 目标取值字段，默认使用当前字段的 value 配置
 * @returns 转换后的级联值
 */
const resolveCascaderFieldValue = (
  value: unknown,
  field = getCompetitiveTreeField(),
  options = getCascaderOptionsByField(field),
  targetValueKey?: string
) => {
  if (!field) {
    return value
  }

  const resolvedTargetValueKey = targetValueKey || field.cascaderProps?.value || 'key'
  const shouldEmitPath = field.cascaderProps?.emitPath !== false
  const values = normalizeFieldValues(value)
  const resolvedValues = values
    .map(currentValue => {
      if (shouldEmitPath) {
        return resolveCascaderPathValue(currentValue, field, options, resolvedTargetValueKey)
      }
      const matchedNode = findMatchedCascaderNode(options, currentValue, field)
      return matchedNode ? matchedNode?.[resolvedTargetValueKey] : currentValue
    })
    .filter(item =>
      Array.isArray(item) ? item.length > 0 : item !== '' && item !== null && item !== undefined
    )

  if (shouldEmitPath) {
    const uniquePathMap = new Map<string, any[]>()
    resolvedValues.forEach(item => {
      const path = Array.isArray(item) ? item : [item]
      const normalizedPath = path.filter(
        segment => segment !== '' && segment !== null && segment !== undefined
      )
      if (normalizedPath.length === 0) {
        return
      }
      uniquePathMap.set(JSON.stringify(normalizedPath), normalizedPath)
    })

    const uniqueValues = Array.from(uniquePathMap.values())
    return field.multiple ? uniqueValues : (uniqueValues[0] ?? [])
  }

  const uniqueValues = Array.from(new Set(resolvedValues))
  return field.multiple ? uniqueValues : (uniqueValues[0] ?? '')
}

/**
 * 递归查找级联节点路径，兼容旧缓存中的单值与新路径值
 * @param options 级联选项树
 * @param targetValue 待匹配的值
 * @param field 级联字段配置
 * @param parentNodes 父级节点路径
 * @returns 匹配到的节点路径
 */
const findMatchedCascaderNodePath = (
  options: any[],
  targetValue: unknown,
  field = getCompetitiveTreeField(),
  parentNodes: any[] = []
): any[] | null => {
  if (!field || !Array.isArray(options) || options.length === 0) {
    return null
  }

  if (targetValue === '' || targetValue === null || targetValue === undefined) {
    return null
  }

  const { valueKey, childrenKey } = getCascaderFieldKeys(field)

  for (const option of options) {
    const currentPath = [...parentNodes, option]

    if (
      isMatchedValue([option?.[valueKey], option?.key, option?.value, option?.code], targetValue)
    ) {
      return currentPath
    }

    const children = Array.isArray(option?.[childrenKey]) ? option[childrenKey] : []
    const matchedChildPath = findMatchedCascaderNodePath(children, targetValue, field, currentPath)
    if (matchedChildPath) {
      return matchedChildPath
    }
  }

  return null
}

/**
 * 将级联字段值转换为路径数组，兼容旧缓存中的单值与新值格式
 * @param value 原始字段值
 * @param field 级联字段配置
 * @param options 级联选项树
 * @param targetValueKey 目标取值字段
 * @returns 标准化后的路径数组
 */
const resolveCascaderPathValue = (
  value: unknown,
  field = getCompetitiveTreeField(),
  options = getCascaderOptionsByField(field),
  targetValueKey?: string
) => {
  if (!field) {
    return value
  }

  const resolvedTargetValueKey = targetValueKey || field.cascaderProps?.value || 'key'
  const pathValues = Array.isArray(value)
    ? value.filter(item => item !== '' && item !== null && item !== undefined)
    : []
  const lastValue = pathValues.length > 0 ? pathValues[pathValues.length - 1] : value
  const matchedPath = findMatchedCascaderNodePath(options, lastValue, field)

  if (!matchedPath) {
    return pathValues.length > 0 ? pathValues : value
  }

  return matchedPath
    .map(node => node?.[resolvedTargetValueKey] ?? node?.key ?? node?.value ?? node?.code)
    .filter(item => item !== '' && item !== null && item !== undefined)
}

/**
 * 将竞品品牌车系的路径值拆分为品牌与车系两个查询字段
 * @param value 竞品品牌车系当前选中值
 * @param field 竞品品牌车系字段配置
 * @param options 竞品品牌车系选项树
 * @returns 拆分后的查询参数
 */
const splitCompetitiveTreeQueryParams = (
  value: unknown,
  field = getCompetitiveTreeField(),
  options = getCascaderOptionsByField(field)
) => {
  if (!field || !Array.isArray(options) || options.length === 0) {
    return {} as Pick<VocQueryParams, 'compBrandCodeList' | 'compCarSeriesList'>
  }

  const resolvedValue = resolveCascaderFieldValue(value, field, options)
  const selectedPaths = normalizeFieldValues(resolvedValue)
  const brandValues: string[] = []
  const seriesValues: string[] = []

  selectedPaths.forEach(currentValue => {
    const currentPath = Array.isArray(currentValue) ? currentValue : [currentValue]
    const brandValue = currentPath[0]
    const seriesValue = currentPath[1]

    if (brandValue !== '' && brandValue !== null && brandValue !== undefined) {
      brandValues.push(String(brandValue))
    }

    if (seriesValue !== '' && seriesValue !== null && seriesValue !== undefined) {
      seriesValues.push(String(seriesValue))
    }
  })

  const params: Pick<VocQueryParams, 'compBrandCodeList' | 'compCarSeriesList'> = {}

  if (brandValues.length > 0) {
    params.compBrandCodeList = Array.from(new Set(brandValues))
  }

  if (seriesValues.length > 0) {
    params.compCarSeriesList = Array.from(new Set(seriesValues))
  }

  return params
}

/**
 * 将用车场景级联路径拆分为后端查询字段。
 * 选中一级时进入一级集合，选中二级时进入二级集合；接口按场景名称匹配。
 * @param value 用车场景当前选中值
 * @returns 用车场景查询参数
 */
const splitUsageScenarioQueryParams = (value: unknown) => {
  const firstSet = new Set<string>()
  const secondSet = new Set<string>()

  normalizeFieldValues(value).forEach(currentValue => {
    const path = Array.isArray(currentValue)
      ? currentValue.filter(item => item !== '' && item !== null && item !== undefined)
      : [currentValue].filter(item => item !== '' && item !== null && item !== undefined)

    if (path.length === 1) {
      firstSet.add(String(path[0]))
      return
    }

    if (path.length >= 2) {
      secondSet.add(String(path[path.length - 1]))
    }
  })

  return {
    usageScenarioFirstList: Array.from(firstSet),
    usageScenarioSecondList: Array.from(secondSet)
  }
}

/**
 * 判断两个字段值是否一致，避免重复写入响应式状态
 * @param left 左侧值
 * @param right 右侧值
 * @returns 是否一致
 */
const isSameFieldValue = (left: unknown, right: unknown) => {
  if (Array.isArray(left) && Array.isArray(right)) {
    if (left.length !== right.length) {
      return false
    }

    return left.every((item, index) => String(item) === String(right[index]))
  }

  return String(left ?? '') === String(right ?? '')
}

/**
 * 同步新品车系选中对象到查询 Store
 * 新车上市页后续会从该对象中读取预热期、上市期、稳定期时间范围。
 * @param value 新品车系字段当前值
 * @param field 新品车系字段配置
 * @param options 新品车系选项树
 * @returns 本次同步是否改变了 Store 中的车系对象
 */
const syncNewCarSeriesSelection = (
  value: unknown,
  field = getNewCarSeriesField(),
  options = getCascaderOptionsByField(field)
) => {
  if (!field) {
    return false
  }

  const selectedNodes = normalizeFieldValues(value)
    .map(currentValue => {
      const targetValue = Array.isArray(currentValue)
        ? currentValue[currentValue.length - 1]
        : currentValue
      return findMatchedCascaderNode(options, targetValue, field)
    })
    .filter(Boolean) as NewCarSeriesSelectorObj[]

  const selectedCodes = selectedNodes
    .map(item => item.code)
    .filter(code => code !== '' && code !== null && code !== undefined)
  const currentCodes =
    queryStore.currentQueryParams.newCarSeriesObjList?.map(item => String(item.code)) || []
  const nextCodes = selectedCodes.map(code => String(code))
  const hasChanged =
    currentCodes.length !== nextCodes.length ||
    currentCodes.some((code, index) => code !== nextCodes[index])

  queryStore.updateQueryParams({
    newCarSeriesList: selectedCodes,
    newCarSeriesObjList: selectedNodes
  })

  return hasChanged
}

/**
 * 判断对象上是否存在指定字段（即使值为空）
 * @param target 目标对象
 * @param key 字段名
 * @returns 是否存在该字段
 */
const hasOwnField = (target: Record<string, any>, key: string) => {
  return Object.prototype.hasOwnProperty.call(target, key)
}

/**
 * 判断字段是否显式配置了静态默认值
 * 约定：仅 `undefined` 视为“未配置默认值”，其余空数组/空字符串/null 都视为显式默认值。
 * @param field 字段配置
 * @returns 是否存在静态默认值
 */
const hasStaticDefaultValue = (field: FilterFieldConfig) => field.defaultValue !== undefined

/**
 * 获取字段默认值，遵循“静态默认值优先，动态默认值兜底”的规则。
 * @param field 字段配置
 * @returns 原始默认值
 */
const getFieldDefaultValue = (field: FilterFieldConfig) => {
  if (hasStaticDefaultValue(field)) {
    return field.defaultValue
  }

  return field.getDefaultValue?.(field, { allConfig: config.value, route })
}

/**
 * 将原始默认值规范为表单初始化需要的值结构
 * @param field 字段配置
 * @param rawDefaultValue 原始默认值
 * @returns 可直接写入 formData 的默认值
 */
const normalizeFieldDefaultValue = (field: FilterFieldConfig, rawDefaultValue: any) => {
  if (field.type === 'cascader') {
    const isMultiple = field.multiple || field.cascaderProps?.multiple

    if (isMultiple) {
      return Array.isArray(rawDefaultValue)
        ? rawDefaultValue
        : rawDefaultValue !== null && rawDefaultValue !== undefined
          ? [rawDefaultValue]
          : []
    }

    if (field.cascaderProps?.emitPath === false) {
      return rawDefaultValue ?? null
    }

    return Array.isArray(rawDefaultValue)
      ? rawDefaultValue
      : rawDefaultValue !== null && rawDefaultValue !== undefined
        ? [rawDefaultValue]
        : []
  }

  if ((field.type === 'brand' && field.multiple) || field.type === 'series') {
    return Array.isArray(rawDefaultValue)
      ? rawDefaultValue
      : rawDefaultValue !== null && rawDefaultValue !== undefined
        ? [rawDefaultValue]
        : []
  }

  if (field.type === 'experienceCode' || field.type === 'experienceCodeLinkage') {
    return Array.isArray(rawDefaultValue) ? rawDefaultValue : []
  }

  if (field.type === 'btnSwitch' && field.multiple) {
    return Array.isArray(rawDefaultValue) ? rawDefaultValue : []
  }

  return rawDefaultValue ?? null
}

/**
 * 根据字段配置构建初始化值
 * @param field 字段配置
 * @returns 字段初始化值
 */
const buildFieldInitialValue = (field: FilterFieldConfig) => {
  return normalizeFieldDefaultValue(field, getFieldDefaultValue(field))
}

/**
 * 判断字段当前是否为空值，用于异步默认值补齐时避免覆盖已有选择。
 * @param field 字段配置
 * @param value 当前字段值
 * @returns 是否为空
 */
const isFieldEmpty = (field: FilterFieldConfig, value: unknown) => {
  if (field.type === 'experienceCode' || field.type === 'experienceCodeLinkage') {
    return (
      !Array.isArray(value) || value.every(level => !Array.isArray(level) || level.length === 0)
    )
  }

  if (
    Array.isArray(value) ||
    field.multiple ||
    field.type === 'brand' ||
    field.type === 'series' ||
    field.type === 'cascader'
  ) {
    return normalizeFieldValues(value).length === 0
  }

  return value === '' || value === null || value === undefined
}

/**
 * 应用新品车系选项并补齐字段配置
 * 字段配置中的 options 是 getDefaultValue 的数据源，必须与实际级联选项保持一致。
 * @param options 新品车系选项树
 */
const applyNewCarSeriesOptions = (options: any[]) => {
  localNewCarSeriesOptions.value = options

  const newCarSeriesField = getNewCarSeriesField()
  if (!newCarSeriesField) {
    return
  }

  newCarSeriesField.options = options

  if (options.length === 0) {
    return
  }

  const currentValue = formData.value[newCarSeriesField.prop]
  const isCurrentValueEmpty = isFieldEmpty(newCarSeriesField, currentValue)

  if (isCurrentValueEmpty) {
    const shouldApplyDynamicDefault =
      pendingDynamicDefaultProps.value.includes(newCarSeriesField.prop) &&
      typeof newCarSeriesField.getDefaultValue === 'function'

    if (!shouldApplyDynamicDefault) {
      return
    }

    const rawDefaultValue = getFieldDefaultValue(newCarSeriesField)
    const normalizedDefaultValue = normalizeFieldDefaultValue(newCarSeriesField, rawDefaultValue)

    if (isFieldEmpty(newCarSeriesField, normalizedDefaultValue)) {
      return
    }

    formData.value[newCarSeriesField.prop] = normalizedDefaultValue
    pendingDynamicDefaultProps.value = pendingDynamicDefaultProps.value.filter(
      prop => prop !== newCarSeriesField.prop
    )
  }

  const hasSynced = syncNewCarSeriesSelection(
    formData.value[newCarSeriesField.prop],
    newCarSeriesField,
    options
  )

  if (hasSynced && isFormDataInitialized.value) {
    nextTick(() => {
      handleSearch()
    })
  }
}

/**
 * 应用竞品品牌车系选项并补齐字段配置
 * @param options 竞品品牌车系选项树
 */
const applyCompetitiveTreeOptions = (options: any[]) => {
  localCompetitiveTreeOptions.value = options

  const competitiveTreeField = getCompetitiveTreeField()
  if (competitiveTreeField) {
    competitiveTreeField.options = options
  }
}

/**
 * 从数据广场筛选条件中提取用车场景树。
 * @param result conditions 接口返回结果
 * @returns 用车场景一二级树
 */
const extractUsageScenarioOptions = (result: unknown): DataPlazaConditionOption[] => {
  if (Array.isArray(result)) {
    const conditionGroups = result as DataPlazaConditionGroup[]
    const carSceneGroup = conditionGroups.find(item => item?.key === 'carScene')

    if (Array.isArray(carSceneGroup?.details)) {
      return carSceneGroup.details
    }

    // 兼容接口直接返回 carScene 明细数组的情况。
    return result.filter((item: any) => item?.value || item?.children) as DataPlazaConditionOption[]
  }

  const resultMap = result as Record<string, any>
  if (Array.isArray(resultMap?.carScene)) {
    return resultMap.carScene
  }

  return []
}

/**
 * 应用用车场景选项，并同步到当前字段配置供展示、回显和标签格式化使用。
 * @param options 用车场景树
 */
const applyUsageScenarioOptions = (options: DataPlazaConditionOption[]) => {
  localUsageScenarioOptions.value = options

  const usageScenarioField = getUsageScenarioField()
  if (!usageScenarioField) {
    return
  }

  usageScenarioField.options = options

  if (isFormDataInitialized.value) {
    nextTick(() => {
      filterTags.value = formatFilterTags(
        config.value,
        formData.value,
        customTimes.value,
        channelOptions.value,
        standardViewpointOptions.value,
        localCompetitiveTreeOptions.value,
        localNewCarSeriesOptions.value,
        experienceCodeNames.value,
        mainAccOptions.value
      )
    })
  }
}

// 监听父组件传入的新品车系和对比车系选项变化
watch(
  () => [props.newCarSeriesOptions, props.competitiveTreeOptions],
  ([newCarOptions, competitiveOptions]) => {
    if (Array.isArray(newCarOptions)) {
      applyNewCarSeriesOptions(newCarOptions)
    }

    if (Array.isArray(competitiveOptions)) {
      applyCompetitiveTreeOptions(competitiveOptions)
    }
  },
  { immediate: true }
)

/**
 * 判断字段是否在本轮已应用的缓存中出现过
 * 即使缓存值为空，也认为用户/页面已明确给出该字段状态，不再回退到动态默认值。
 * @param field 字段配置
 * @param sharedCachedParams 全局共享缓存
 * @param originalSoundSharedCachedParams originalSoundQuery 页内共享缓存
 * @param isolatedCachedParams 当前页签私有缓存
 * @returns 是否已被缓存覆盖
 */
const hasAppliedCacheValueForField = (
  field: FilterFieldConfig,
  sharedCachedParams: Record<string, any>,
  originalSoundSharedCachedParams: Record<string, any>,
  isolatedCachedParams: Record<string, any>
) => {
  if (isTimeOnlySharedRoute.value) {
    if (isOriginalSoundQueryTabRoute.value) {
      if (
        originalSoundQuerySharedFieldKeys.value.has(field.prop) &&
        hasOwnField(originalSoundSharedCachedParams, field.prop)
      ) {
        return true
      }

      if (
        !originalSoundQuerySharedFieldKeys.value.has(field.prop) &&
        hasOwnField(isolatedCachedParams, field.prop)
      ) {
        return true
      }

      return false
    }

    return hasOwnField(isolatedCachedParams, field.prop)
  }

  return hasOwnField(sharedCachedParams, field.prop)
}

/**
 * 尝试应用路由品牌参数。
 * 当品牌数据尚未加载时，仅记录待处理状态，等待异步数据返回后再次尝试。
 * @param routeBrandCode 路由中的品牌编码
 * @returns 是否实际更新了品牌字段
 */
const applyRouteBrandOverride = (routeBrandCode?: string) => {
  const brandField = getBrandField()

  if (!brandField || !routeBrandCode) {
    pendingRouteBrandCode.value = null
    return false
  }

  const brandOptions = userStore.getBrandService || []
  if (!brandOptions.length) {
    pendingRouteBrandCode.value = routeBrandCode
    return false
  }

  const matchedBrand = brandOptions.find(
    (item: any) => item.key === routeBrandCode || item.brandCode === routeBrandCode
  )
  pendingRouteBrandCode.value = null

  if (!matchedBrand) {
    return false
  }

  const nextValue = resolveBrandFieldValue(routeBrandCode, brandField)
  if (isSameFieldValue(formData.value[brandField.prop], nextValue)) {
    return false
  }

  formData.value[brandField.prop] = nextValue
  return true
}

/**
 * 判断 filterType=91 的默认值是否仍是旧的“品牌+车系”级联格式
 * @param values 默认值数组
 * @param brandField 品牌字段配置
 * @param seriesField 车系字段配置
 * @returns 是否为旧格式
 */
const isLegacyBrandSeriesDefault = (
  values: any[],
  brandField: any,
  seriesField = getSeriesField()
) => {
  if (!brandField || !seriesField || values.length !== 2) {
    return false
  }

  const [brandValue, seriesValue] = values
  return getMatchedBrandOptions(brandValue, brandField).some((brand: any) =>
    (brand.children || []).some((series: any) =>
      isMatchedValue([series.key, series.value], seriesValue)
    )
  )
}

/**
 * 根据路由名称获取客户体验代码的 tagLibType
 * @returns TagType 值
 */
const getTagLibTypeByRouteName = (): string => {
  const routeName = props.routeName
  if (['journeyAnalysis'].includes(routeName)) {
    return TagType.UserJourney
  } else if (
    [
      'serviceAnalysis',
      'productAnalysis',
      'voiceManagement',
      'selfServiceOriginalSoundQuery',
      'rootCause',
      'hotEvents',
      'hotDetailEvents'
    ].includes(routeName)
  ) {
    return TagType.Domain
  }
  // 默认返回 Domain
  return TagType.Domain
}

/**
 * @description: 设置角色中配置的默认值到查询条件中
 * @param {*} routeName
 * @return {*}
 */
const setFilterDefByRouterName = (routeName: string) => {
  // 如果没有缓存，使用角色配置的默认值
  const defaultList = queryStore.getCurDefByRouterName(routeName)

  // 如果 defaultList 不存在或为空，直接返回
  if (!defaultList || !Array.isArray(defaultList) || defaultList.length === 0) {
    return
  }

  // 遍历 defaultList，将匹配的 filterType 的 value 赋值给 config 中的 defaultValue
  defaultList.forEach((defaultItem: any) => {
    if (defaultItem?.filterType) {
      // 在 config 中查找相同 filterType 的配置项
      // 支持字符串和数字类型的比较
      const matchedConfig = config.value.find(
        configItem =>
          String(configItem.filterType) === String(defaultItem.filterType) ||
          configItem.filterType === defaultItem.filterType
      )

      // 如果找到匹配的配置项，更新其 defaultValue
      if (matchedConfig && defaultItem.value !== undefined) {
        // filterType 为 93 时，直接取 value[0]
        if (defaultItem.filterType === '93') {
          matchedConfig.defaultValue = Array.isArray(defaultItem.value)
            ? defaultItem.value[0]
            : defaultItem.value
        } else if (defaultItem.filterType === '91') {
          const defaultValues = normalizeFieldValues(defaultItem.value)

          if (matchedConfig.multiple) {
            const seriesField = getSeriesField()
            if (isLegacyBrandSeriesDefault(defaultValues, matchedConfig, seriesField)) {
              const [brandValue, seriesValue] = defaultValues
              matchedConfig.defaultValue = resolveBrandFieldValue(brandValue, matchedConfig)
              if (seriesField) {
                seriesField.defaultValue = resolveSeriesFieldValue(
                  seriesValue,
                  matchedConfig.defaultValue,
                  seriesField,
                  matchedConfig
                )
              }
            } else {
              matchedConfig.defaultValue = resolveBrandFieldValue(defaultValues, matchedConfig)
            }
          } else {
            matchedConfig.defaultValue = Array.isArray(defaultItem.value)
              ? defaultItem.value[0]
              : defaultItem.value
          }
        } else if (defaultItem.filterType === '92') {
          // filterType 为 92 时，处理客户体验代码的回显
          // 直接保存数组格式，例如: ['code1', 'code2', 'code3', 'code4']
          // 在 ExperienceCodeSelector 组件中会解构成 firstCodeTag, secondCodeTag, threeCodeTag, fourCodeTag

          if (Array.isArray(defaultItem.value) && defaultItem.value.length > 0) {
            matchedConfig.defaultValue = defaultItem.value
          } else {
            matchedConfig.defaultValue = []
          }
        } else {
          matchedConfig.defaultValue = defaultItem.value
        }
      }
    }
  })
}

/**
 * @description: 根据配置构建表单数据
 * @return {*} 表单数据对象
 */
function buildFormDataFromConfig(): Record<string, any> {
  const data: Record<string, any> = {}
  config.value.forEach(field => {
    if (field.type !== 'placeholder') {
      data[field.prop] = buildFieldInitialValue(field)

      if (field.type === 'experienceCode' || field.type === 'experienceCodeLinkage') {
        // 联动版额外写入体验代码类型字段（例如 tagType），用于查询传参
        if (
          field.type === 'experienceCodeLinkage' &&
          (field as any).tagTypeProp &&
          !(field as any).fixedTagType
        ) {
          const key = (field as any).tagTypeProp as string
          data[key] = (field as any).tagTypeDefaultValue ?? data[key] ?? null
        }
      }
    }
  })
  return data
}

// 初始化表单数据
// 说明：初始化过程中有些场景会被调用多次（例如挂载后检测到缓存变更再次初始化）。
// 为了避免重复触发查询，提供 triggerSearch 开关，调用方决定是否在本次初始化结束后触发 handleSearch。
function initFormData(options: { triggerSearch?: boolean } = {}) {
  const { triggerSearch = true } = options
  isInitializingFormData.value = true
  const sharedCachedParams = getGlobalSharedCacheParams()
  const originalSoundSharedCachedParams = getOriginalSoundQuerySharedCacheParams()
  const isolatedCachedParams = getIsolatedCacheParams()
  pendingDynamicDefaultProps.value = []
  pendingRouteBrandCode.value = null

  // 步骤 2: 优先从 sceneAnalysisStore 获取保存的 formData（报告详情页的数据源）
  // 如果是详情页，从 sceneAnalysisStore 获取；否则从 queryStore 获取
  let savedFormData: Record<string, any> = {}
  let savedCustomTimes: string[] = []

  if (isReportDetailPage.value) {
    // 详情页：从 sceneAnalysisStore 获取
    const detailFilter = sceneAnalysisStore.getDetailFilter
    savedFormData = detailFilter.formData || {}
    savedCustomTimes = detailFilter.customTimes || []
  } else {
    // 非详情页：从 queryStore 获取
    const queryFormData = queryStore.getUniversaFilterFormData()
    savedFormData = queryFormData.formData || {}
    savedCustomTimes = queryFormData.customTimes || []
  }

  const hasSavedFormData = savedFormData && Object.keys(savedFormData).length > 0

  // 步骤 3: 先回到当前页面默认值，再按“公共缓存/页签私有缓存”规则回填
  setFilterDefByRouterName(props.routeName)
  const defaultData = buildFormDataFromConfig()
  Object.keys(defaultData).forEach(key => {
    formData.value[key] = defaultData[key]
  })

  if (isReportDetailPage.value) {
    // 报告详情必须完整使用发布时保存的条件，不能混入用户的公共缓存或角色默认值。
    const detailFilter = sceneAnalysisStore.getDetailFilter
    applyCacheToFormData(
      {
        ...savedFormData,
        startDate: detailFilter.startDate || '',
        endDate: detailFilter.endDate || '',
        globalShortcutValue: detailFilter.selectedShortcut || ''
      },
      {
        includeRestrictedFields: true,
        includeEmptyValues: true
      }
    )
  } else if (isTimeOnlySharedRoute.value) {
    // rootCause / originalSoundQuery 只参与公共时间条件，不回填其他公共筛选项。
    applyCacheToFormData(sharedCachedParams, {
      includeFields: false,
      includeTime: true
    })

    if (isOriginalSoundQueryTabRoute.value) {
      // originalSoundQuery 页内切换时，交集字段以页内共享缓存为准。
      applyCacheToFormData(originalSoundSharedCachedParams, {
        includeFields: true,
        includeTime: false,
        fieldKeys: originalSoundQuerySharedFieldKeys.value
      })

      // 再叠加当前 tab 私有字段，避免另一 tab 的独有条件串进来。
      applyCacheToFormData(isolatedCachedParams, {
        includeFields: true,
        includeTime: false,
        includeRestrictedFields: true,
        excludedFieldKeys: originalSoundQuerySharedFieldKeys.value
      })
    } else {
      // rootCause 沿用“仅时间公共条件 + 当前页私有字段”的模式。
      applyCacheToFormData(isolatedCachedParams, {
        includeFields: true,
        includeTime: false,
        includeRestrictedFields: true
      })
    }
  } else {
    // 其他页面沿用原有逻辑：公共缓存整体回填。
    applyCacheToFormData(sharedCachedParams)
  }

  // 步骤 4: 如果有保存的 formData（通常是从报告详情页跳转过来的），设置特殊字段
  // 注意：报告详情回填仍然单独兜底处理，避免与公共缓存/页签私有缓存的优先级混淆。
  // 标准观点虽然在私有缓存场景可回填，但详情页进入时仍在这里显式处理，确保选项未加载前也能拿到值。
  // 重要：客户体验代码只在从报告详情页跳转过来时才恢复，普通页面跳转应该使用角色配置的默认值
  if (isReportDetailPage.value && hasSavedFormData) {
    const experienceCodeField = getExperienceCodeField()
    const standardViewpointField = config.value.find(field => field.prop === 'topicCodes')

    // 设置客户体验代码（被 applyCacheToFormData 跳过，必须在这里设置）
    // 但只在从报告详情页跳转过来时才恢复，普通页面跳转应该使用角色配置的默认值
    if (
      experienceCodeField &&
      savedFormData[experienceCodeField.prop] !== undefined &&
      savedFormData[experienceCodeField.prop] !== null
    ) {
      formData.value[experienceCodeField.prop] = savedFormData[experienceCodeField.prop]
    }

    // 联动版：同时恢复体验代码类型（例如 tagType）
    if (
      experienceCodeField?.type === 'experienceCodeLinkage' &&
      (experienceCodeField as any).tagTypeProp
    ) {
      const key = (experienceCodeField as any).tagTypeProp as string
      if (
        savedFormData[key] !== undefined &&
        savedFormData[key] !== null &&
        savedFormData[key] !== ''
      ) {
        formData.value[key] = savedFormData[key]
      }
    }

    // 设置标准观点（确保值在选项加载前被设置）
    // 但只在从报告详情页跳转过来时才恢复，普通页面跳转应该使用角色配置的默认值
    if (
      standardViewpointField &&
      savedFormData[standardViewpointField.prop] !== undefined &&
      savedFormData[standardViewpointField.prop] !== null
    ) {
      formData.value[standardViewpointField.prop] = savedFormData[standardViewpointField.prop]
    }

    const dateField = config.value.find(field => field.type === 'daterange')
    if (dateField && savedFormData[dateField.prop]) {
      const dateRangeValue = savedFormData[dateField.prop]
      const times =
        dateRangeValue === 'custom' ? savedCustomTimes : getShortcutDateRange(dateRangeValue)
      if (Array.isArray(times) && times.length === 2) {
        customTimes.value = [...times]
      }
    }
  }

  // 清理不再存在的属性
  const currentDefaultData = buildFormDataFromConfig()
  Object.keys(formData.value).forEach(key => {
    if (!(key in currentDefaultData)) {
      delete formData.value[key]
    }
  })

  // 步骤 5: 检查路由参数中的 brandCode，优先级最高
  // 只有当路由参数中存在 brandCode 且在品牌数据中能找到对应品牌时，才覆盖默认值
  const routeBrandCodeQuery = route.query.brandCode
  const routeBrandCode =
    (Array.isArray(routeBrandCodeQuery) ? routeBrandCodeQuery[0] : routeBrandCodeQuery) || undefined
  applyRouteBrandOverride(routeBrandCode)

  // 旅程分析页：如果路由携带 tag1Code，则强制覆盖“客户体验代码”查询条件
  const routeTag1CodeQuery = route.query.tag1Code
  const routeTag1Code = Array.isArray(routeTag1CodeQuery)
    ? routeTag1CodeQuery[0]
    : routeTag1CodeQuery
  if (props.routeName === 'journeyAnalysis' && routeTag1Code) {
    const experienceCodeField = getExperienceCodeField()
    if (experienceCodeField) {
      formData.value[experienceCodeField.prop] = [['all'], [routeTag1Code]]
    }

    // 标准观点依赖体验代码，覆盖体验代码后清空，避免回显串值
    const standardViewpointField = config.value.find(field => field.prop === 'topicCodes')
    if (standardViewpointField) {
      formData.value[standardViewpointField.prop] = []
    }
  }

  pendingDynamicDefaultProps.value = config.value
    .filter(
      field =>
        field.type !== 'placeholder' &&
        typeof field.getDefaultValue === 'function' &&
        !(isReportDetailPage.value
          ? hasOwnField(savedFormData, field.prop)
          : hasAppliedCacheValueForField(
              field,
              sharedCachedParams,
              originalSoundSharedCachedParams,
              isolatedCachedParams
            )) &&
        isFieldEmpty(field, formData.value[field.prop])
    )
    .map(field => field.prop)

  // 等待组件挂载完成后再调用 handleSearch
  isInitializingFormData.value = false
  if (triggerSearch) {
    nextTick(() => {
      handleSearch()
    })
  }
}

/**
 * 应用缓存数据到表单
 * @param cachedParams 缓存参数
 */
function applyCacheToFormData(
  cachedParams: Record<string, any>,
  options: {
    includeFields?: boolean
    includeTime?: boolean
    includeRestrictedFields?: boolean
    includeEmptyValues?: boolean
    fieldKeys?: Set<string>
    excludedFieldKeys?: Set<string>
  } = {}
) {
  const {
    includeFields = true,
    includeTime = true,
    includeRestrictedFields = false,
    includeEmptyValues = false,
    fieldKeys,
    excludedFieldKeys
  } = options

  if (!cachedParams || Object.keys(cachedParams).length === 0) {
    return
  }

  // 获取当前页面配置中的所有 prop 字段
  const currentPageProps = getCurrentPagePropKeys()

  // 使用缓存的数据覆盖表单字段（只覆盖缓存中存在的字段，且当前页面配置中有该字段）
  if (includeFields) {
    Object.keys(cachedParams).forEach(key => {
      // 时间字段单独处理，避免与表单字段写入顺序互相影响
      if (isSharedTimeCacheKey(key)) {
        return
      }

      // 体验代码 / 标准观点只允许在页签私有缓存中回填，避免跨页面串值
      if (!includeRestrictedFields && ['experienceCode', 'topicCodes'].includes(key)) {
        return
      }

      // 只处理当前页面配置中存在的字段
      if (currentPageProps.has(key)) {
        if (fieldKeys && !fieldKeys.has(key)) {
          return
        }
        if (excludedFieldKeys?.has(key)) {
          return
        }

        const value = cachedParams[key]
        const fieldConfig = config.value.find(field => field.prop === key)

        // 如果值有效，则使用缓存的值
        // 对于数组类型（如车系），空数组也是有效值，需要保留
        if (value !== undefined && value !== null && value !== '') {
          let finalValue = value

          // 处理品牌和车系字段的 props 转换
          // 如果当前页面配置使用 value 作为值（如 OriginalData），但缓存中存储的是 key，需要转换
          if (fieldConfig?.type === 'brand' && fieldConfig.props?.value === 'value') {
            // 品牌字段：将 key 转换为当前字段使用的值
            finalValue = resolveBrandFieldValue(value, fieldConfig)
          } else if (fieldConfig?.type === 'series' && fieldConfig.props?.value === 'value') {
            // 车系字段：需要基于当前已选品牌，将 key 转换为当前字段使用的值
            const brandField = getBrandField()
            // 优先从 formData 中获取品牌值（可能已经应用了），如果没有则从缓存中获取
            let brandValue = brandField ? formData.value[brandField.prop] : null
            if (isEmptyBrandValue(brandValue) && brandField && cachedParams[brandField.prop]) {
              brandValue = resolveBrandFieldValue(cachedParams[brandField.prop], brandField)
            }

            finalValue = resolveSeriesFieldValue(value, brandValue, fieldConfig, brandField)
          } else if (
            fieldConfig?.type === 'selectv2' &&
            fieldConfig.prop === COMPETITIVE_BRAND_PROP &&
            fieldConfig.props?.value === 'value'
          ) {
            // 竞品品牌字段：原始数据页缓存统一按 key 存储，回填时需要转换为中文值
            finalValue = resolveCompetitiveBrandFieldValue(value, fieldConfig)
          } else if (
            fieldConfig?.type === 'selectv2' &&
            fieldConfig.prop === COMPETITIVE_SERIES_PROP &&
            fieldConfig.props?.value === 'value'
          ) {
            // 竞品车系字段：优先基于当前已选竞品品牌转换；如果品牌还没回填，则用全量竞品树兜底。
            const competitiveBrandField = getCompetitiveBrandField()
            let competitiveBrandValue = competitiveBrandField
              ? formData.value[competitiveBrandField.prop]
              : []

            if (
              isEmptyBrandValue(competitiveBrandValue) &&
              competitiveBrandField &&
              cachedParams[competitiveBrandField.prop]
            ) {
              competitiveBrandValue = resolveCompetitiveBrandFieldValue(
                cachedParams[competitiveBrandField.prop],
                competitiveBrandField
              )
            }

            const normalizedSeriesValue =
              localCompetitiveTreeOptions.value.length > 0 &&
              normalizeFieldValues(value).some(Array.isArray)
                ? splitCompetitiveTreeQueryParams(value).compCarSeriesList || value
                : value

            finalValue = resolveCompetitiveSeriesFieldValue(
              normalizedSeriesValue,
              competitiveBrandValue,
              fieldConfig,
              competitiveBrandField,
              localCompetitiveTreeOptions.value,
              true
            )
          } else if (fieldConfig?.type === 'cascader') {
            if (fieldConfig.prop === USAGE_SCENARIO_PROP) {
              // 用车场景按名称取值，缓存直接保留 value 路径。
              finalValue = value
            } else {
              // 级联字段：兼容缓存中的 key/value 混用，统一转换为当前页面的取值格式
              finalValue = resolveCascaderFieldValue(
                value,
                fieldConfig,
                getCascaderOptionsByField(fieldConfig)
              )
            }
          }

          // 如果是数组类型，即使是空数组也要应用（可能是用户清空了选择）
          if (Array.isArray(finalValue)) {
            formData.value[key] = finalValue
          } else {
            // 非数组类型，直接赋值
            formData.value[key] = finalValue
          }
        } else if (includeEmptyValues) {
          // 报告详情中的空值代表发布时明确清空，不能回退到角色默认条件。
          formData.value[key] = value
        } else if (value === '' && fieldConfig?.type === 'brand' && fieldConfig?.clearable) {
          // 品牌字段开启 clearable：空字符串视为用户“清空品牌”的明确选择，需要回显/应用
          formData.value[key] = ''
        } else if (value === '' && fieldConfig?.defaultValue && !fieldConfig?.clearable) {
          // 如果缓存中的值是空字符串，但字段存在默认值：默认不覆盖，避免默认值被空字符串冲掉
          // 当字段开启 clearable（允许清空）时，认为空字符串是用户的明确选择，需要允许覆盖默认值
          return
        }
      }
    })
  }

  // 处理时间：如果有缓存的时间，优先使用缓存
  if (includeTime && cachedParams.startDate && cachedParams.endDate) {
    const dateField = config.value.find(field => field.type === 'daterange')
    if (dateField) {
      // 优先检查 dateRange，确保自定义时间优先于 globalShortcutValue
      const dateRangeValue = cachedParams.dateRange
      if (dateRangeValue === 'custom' || cachedParams.globalShortcutValue === '自定义') {
        // 如果 dateRange 是 'custom' 或者 globalShortcutValue 是 '自定义'，设置为自定义时间
        formData.value[dateField.prop] = 'custom'
        // 直接设置 customTimes，确保值能正确传递到 DatePicker 组件
        customTimes.value = [cachedParams.startDate, cachedParams.endDate]
        queryStore.globalShortcutValue = '自定义'
      } else if (dateRangeValue) {
        // 如果有 dateRange 且不是 'custom'，使用 dateRange
        formData.value[dateField.prop] = dateRangeValue
        const dimensionItem = getTimeDimensionByCode(dateRangeValue)
        if (dimensionItem && dimensionItem.name) {
          queryStore.globalShortcutValue = dimensionItem.name
          // 同步更新 customTimes，确保 el-date-picker 显示正确的日期范围
          const times = getShortcutDateRange(dateRangeValue)
          customTimes.value = times
        }
      } else if (
        cachedParams.globalShortcutValue &&
        cachedParams.globalShortcutValue !== '自定义'
      ) {
        // 如果没有 dateRange，但有 globalShortcutValue 且不是"自定义"，使用对应的快捷选项
        const dimensionItem = getTimeDimensionByCode(cachedParams.globalShortcutValue)
        if (dimensionItem && dimensionItem.code !== undefined) {
          const shortcutCode = dimensionItem.code.toString()
          formData.value[dateField.prop] = shortcutCode
          queryStore.globalShortcutValue = cachedParams.globalShortcutValue
          // 同步更新 customTimes，确保 el-date-picker 显示正确的日期范围
          const times = getShortcutDateRange(shortcutCode)
          customTimes.value = times
        }
      }
    }
  }
}

// 初始化数据源选项
const initChannelOptions = async () => {
  try {
    const res = await getUserChannelTree()
    channelOptions.value = res.result || []
  } catch (error) {
    console.error('获取数据源选项失败:', error)
    channelOptions.value = []
  }
}

const refreshFilterTags = () => {
  filterTags.value = formatFilterTags(
    config.value,
    formData.value,
    customTimes.value,
    channelOptions.value,
    standardViewpointOptions.value,
    localCompetitiveTreeOptions.value,
    localNewCarSeriesOptions.value,
    experienceCodeNames.value,
    mainAccOptions.value
  )
}

const getAttributeTagField = () => {
  return config.value.find(field => field.prop === 'scenarioAttr')
}

const setAttributeTagOptions = (options: any[]) => {
  attributeTagOptions.value = options

  const attributeTagField = getAttributeTagField()
  if (!attributeTagField) {
    return
  }

  attributeTagField.options = options

  if (Array.isArray(formData.value[attributeTagField.prop])) {
    nextTick(refreshFilterTags)
  }
}

const initAttributeTagOptions = async () => {
  try {
    const res = await findAllAttributeLabelList({})
    setAttributeTagOptions(Array.isArray(res.result) ? res.result : [])
  } catch (error) {
    console.error('获取属性标签选项失败:', error)
    setAttributeTagOptions([])
  }
}

// 初始化重点账号树选项
const initMainAccTreeOptions = async () => {
  try {
    const res = await getMainAccTreeData()
    mainAccOptions.value = res.result || []
  } catch (error) {
    console.error('获取重点账号树选项失败:', error)
    mainAccOptions.value = []
  }
}

// 初始化依赖 /report/drill-down/conditions 的选项
const initDrillDownConditionOptions = async () => {
  if (!shouldLoadDrillDownConditions.value || hasLoadedDrillDownConditions.value) {
    return
  }

  try {
    const res = await getDrillDownConditions()
    const conditions = Array.isArray(res.result) ? res.result : []
    const competitiveTree = findDrillDownConditionByKey(conditions, 'competitiveTree')
    const newCarTree = findDrillDownConditionByKey(conditions, 'newCarTree')
    const tagTypeItem = findDrillDownConditionByKey(conditions, 'tagType')
    const tagTypeDetails = Array.isArray(tagTypeItem?.details) ? tagTypeItem.details : []

    experienceCodeTypeOptions.value = tagTypeDetails
      .map((item: any) => ({ key: item?.key, value: item?.value }))
      .filter((item: any) => item.key && item.value)

    // 只有当没有从父组件传入数据时，才使用接口返回的数据
    if (!props.competitiveTreeOptions || props.competitiveTreeOptions.length === 0) {
      applyCompetitiveTreeOptions(
        Array.isArray(competitiveTree?.details) ? competitiveTree.details : []
      )
    }
    if (!props.newCarSeriesOptions || props.newCarSeriesOptions.length === 0) {
      applyNewCarSeriesOptions(Array.isArray(newCarTree?.details) ? newCarTree.details : [])
    }
    hasLoadedDrillDownConditions.value = true
  } catch (error) {
    console.error('获取筛选条件选项失败:', error)
    experienceCodeTypeOptions.value = []

    // 只有当没有从父组件传入数据时，才设置为空数组
    if (!props.competitiveTreeOptions || props.competitiveTreeOptions.length === 0) {
      applyCompetitiveTreeOptions([])
    }
    if (!props.newCarSeriesOptions || props.newCarSeriesOptions.length === 0) {
      applyNewCarSeriesOptions([])
    }
    hasLoadedDrillDownConditions.value = false
  }
}

// 初始化用车场景选项
const initUsageScenarioOptions = async () => {
  if (!shouldLoadUsageScenarioOptions.value || hasLoadedUsageScenarioOptions.value) {
    return
  }

  try {
    const res = await getDataPlazaConditions()
    applyUsageScenarioOptions(extractUsageScenarioOptions(res.result))
    hasLoadedUsageScenarioOptions.value = true
  } catch (error) {
    console.error('获取用车场景筛选条件失败:', error)
    applyUsageScenarioOptions([])
    hasLoadedUsageScenarioOptions.value = false
  }
}

// 初始化标准观点选项
const initStandardViewpointOptions = async (tagParentCodes?: string[]) => {
  try {
    const routeName = props.routeName
    const isRootCauseOrResultData = [
      'rootCause',
      'ResultData',
      'newCarLaunch',
      'mainAccount',
      'hotEvents',
      'hotDetailEvents'
    ].includes(routeName)

    // 如果有tagParentIds，传入tagParentId参数
    if (tagParentCodes && tagParentCodes.length > 0) {
      // 过滤掉 "all" 值
      const filteredCodes = tagParentCodes.filter(code => code !== 'all')
      const tagType = (() => {
        const experienceField = getExperienceCodeField() as any
        if (experienceField?.type === 'experienceCodeLinkage' && experienceField?.tagTypeProp) {
          const v = formData.value[experienceField.tagTypeProp]
          if (v !== undefined && v !== null && v !== '') return v
        }
        return getTagLibTypeByRouteName()
      })()
      const res = await findFinalTagLibClientVoListByTagId({
        codes: filteredCodes, // 过滤后的数组，可能为空数组 []
        tagType
      })
      if (res.result && Array.isArray(res.result)) {
        standardViewpointOptions.value = res.result
      } else {
        standardViewpointOptions.value = []
      }
    } else {
      // 如果没有tagParentIds，根据路由名称决定是否查询所有标准观点
      if (isRootCauseOrResultData) {
        // rootCause 和 ResultData 路由：查询所有标准观点
        const tagType = (() => {
          const experienceField = getExperienceCodeField() as any
          if (experienceField?.type === 'experienceCodeLinkage' && experienceField?.tagTypeProp) {
            const v = formData.value[experienceField.tagTypeProp]
            if (v !== undefined && v !== null && v !== '') return v
          }
          return TagType.Domain
        })()
        const res = await findFinalTagLibClientVoListByTagId({
          tagType
        })
        if (res.result && Array.isArray(res.result)) {
          standardViewpointOptions.value = res.result
        } else {
          standardViewpointOptions.value = []
        }
      } else {
        // 其他路由：标准观点为空
        standardViewpointOptions.value = []
      }
    }
  } catch (error) {
    console.error('获取标准观点选项失败:', error)
    standardViewpointOptions.value = []
  }
}

// 处理客户体验代码变化
const handleExperienceCodeChange = async (data: {
  lastLevelCodes: string[]
  lastLevelIds: string[]
  names: string[]
  source?: 'user'
}) => {
  // 初始化回显需要保存中文名称，避免后续异步选项加载重算标签时丢失。
  // 用户编辑仍在点击查询时提交，筛选范围继续表示最近一次查询条件。
  if (data.source !== 'user') {
    experienceCodeNames.value = Array.isArray(data.names) ? [...data.names] : []
  }

  // 清空标准观点的选中值（因为客户体验代码变化了）
  const standardViewpointField = config.value.find(field => field.prop === 'topicCodes')
  const experienceCodeField = getExperienceCodeField() as any
  if (standardViewpointField && data.source === 'user') {
    formData.value[standardViewpointField.prop] = []
  }

  // 有末级体验代码时，按当前末级 code 查询对应标准观点
  if (data.lastLevelCodes && Array.isArray(data.lastLevelCodes) && data.lastLevelCodes.length > 0) {
    if (standardViewpointField) {
      await initStandardViewpointOptions(data.lastLevelCodes)
    }
  } else {
    // 仅“多套标签体系切换”页面在切换标签体系后，需要按当前 tagType 重新加载标准观点。
    // 普通体验代码页面仍保持原有行为：没有当前最深已选层级时，标准观点置空，避免影响其他页面。
    if (standardViewpointField && experienceCodeField?.type === 'experienceCodeLinkage') {
      await initStandardViewpointOptions()
    } else {
      standardViewpointOptions.value = []
    }
  }
  // 注意：不再在这里更新筛选范围，只在点击查询时更新
}

const onTagTypeChange = async (value: any) => {
  formData.value.tagType = value
}

const findOptionByKey: any = (options: any[], key: any) => {
  for (const option of options) {
    if (option.code === key) {
      return option
    }
    if (option.cars) {
      const foundOption = findOptionByKey(option.cars, key)
      if (foundOption) {
        return foundOption
      }
    }
  }
  return null
}

const handleCascaderChange = (value: any, field: any) => {
  // console.log('级联字段变化:', value, field, newCarSeriesOptions, competitiveTreeOptions)

  // 处理禁用逻辑
  const todoDis = (options: any) => {
    options?.forEach((option: any) => {
      const children = option[field?.cascaderProps.children]
      if (children?.length) {
        children.forEach((child: any) => {
          if (child[field?.cascaderProps.value] === value) {
            child.disabled = true
          } else {
            child.disabled = false
          }
        })
      }
    })
  }
  // 只有在新车上市 并且  新品车系处理以下逻辑
  if (field.type === 'cascader' && field.prop === 'newCarSeriesList') {
    syncNewCarSeriesSelection(value, field)

    // 去修改竞品车系的option 禁用逻辑
    const cfile = getCompetitiveTreeField()
    if (cfile) {
      const options = cfile.options
      todoDis(options)
    }
  }
  if (field.prop === 'compCarSeriesList') {
    // 递归遍历newCarSeriesOptions找到属性key等于value的项 并返回这个对象
    const selectedOption = findOptionByKey(localCompetitiveTreeOptions.value, value)
    if (selectedOption) {
      // 中转页面跳转 或者 报告列表跳转过来
      // if (!route?.query?.centerJudge) {

      // }
      queryStore.updateQueryParams({
        compCarSeriesObjList: [selectedOption]
      })
    }

    // 去修改新品车系的option 禁用逻辑
    const nfile = getNewCarSeriesField()
    if (nfile) {
      const options = nfile.options
      todoDis(options)
    }
  }
}

// 监听数据源选项加载完成，如果有选中的值则重新格式化标签
watch(
  () => channelOptions.value,
  newVal => {
    if (newVal && newVal.length > 0) {
      // 检查是否有选中的数据源
      const dataSourceField = config.value.find(field => field.type === 'dataSource')
      if (dataSourceField && formData.value[dataSourceField.prop]) {
        const value = formData.value[dataSourceField.prop]
        if (Array.isArray(value) && value.length > 0) {
          // 数据源选项加载完成后，重新格式化标签
          nextTick(() => {
            filterTags.value = formatFilterTags(
              config.value,
              formData.value,
              customTimes.value,
              channelOptions.value,
              standardViewpointOptions.value,
              localCompetitiveTreeOptions.value,
              localNewCarSeriesOptions.value,
              experienceCodeNames.value,
              mainAccOptions.value
            )
          })
        }
      }
    }
  },
  { deep: true }
)

// 监听标准观点选项加载完成，如果有选中的值则重新格式化标签
watch(
  () => standardViewpointOptions.value,
  (newVal, oldVal) => {
    if (newVal && newVal.length > 0) {
      // 检查是否有选中的标准观点
      const standardViewpointField = config.value.find(field => field.prop === 'topicCodes')
      if (standardViewpointField) {
        const currentValue = formData.value[standardViewpointField.prop]

        // 如果选项从空变为有值，仅在“详情页（报告查看）”场景尝试恢复值
        if (
          (!oldVal || oldVal.length === 0) &&
          (!currentValue || (Array.isArray(currentValue) && currentValue.length === 0))
        ) {
          if (isReportDetailPage.value) {
            const detailFilter = sceneAnalysisStore.getDetailFilter
            const savedFormData = detailFilter.formData || {}

            if (savedFormData && savedFormData[standardViewpointField.prop]) {
              const savedValue = savedFormData[standardViewpointField.prop]
              const savedCodes = Array.isArray(savedValue) ? savedValue : [savedValue]

              // 过滤掉不在选项中的值
              const validCodes = savedCodes.filter(code => {
                return newVal.some((option: any) => option.tagCode === code)
              })

              if (validCodes.length > 0) {
                nextTick(() => {
                  formData.value[standardViewpointField.prop] = [...validCodes]
                  console.log('从 watch 恢复标准观点值:', validCodes, '来源: sceneAnalysisStore')
                })
              }
            }
          }
        } else if (currentValue && Array.isArray(currentValue) && currentValue.length > 0) {
          // 如果已有值，验证并过滤无效值
          const validCodes = currentValue.filter(code => {
            return newVal.some((option: any) => option.tagCode === code)
          })
          if (validCodes.length > 0 && validCodes.length !== currentValue.length) {
            // 如果有无效值被过滤掉，更新 formData
            formData.value[standardViewpointField.prop] = validCodes
          }
        }

        // 标准观点选项加载完成后，重新格式化标签
        nextTick(() => {
          filterTags.value = formatFilterTags(
            config.value,
            formData.value,
            customTimes.value,
            channelOptions.value,
            standardViewpointOptions.value,
            localCompetitiveTreeOptions.value,
            localNewCarSeriesOptions.value,
            experienceCodeNames.value,
            mainAccOptions.value
          )
        })
      }
    }
  },
  { deep: true }
)

// 监听选项加载完成，如果有选中的值则重新格式化标签
watch(
  () => mainAccOptions.value,
  newVal => {
    if (newVal && newVal.length > 0) {
      // 检查是否有选中重点账号
      const mainAccField = config.value.find(field => field.prop === 'keyAccounts')
      if (mainAccField) {
        // 关键 给option列表赋值
        mainAccField.options = newVal

        // 重点账号选项加载完成后，重新格式化标签
        nextTick(() => {
          filterTags.value = formatFilterTags(
            config.value,
            formData.value,
            customTimes.value,
            channelOptions.value,
            standardViewpointOptions.value,
            localCompetitiveTreeOptions.value,
            localNewCarSeriesOptions.value,
            experienceCodeNames.value,
            mainAccOptions.value
          )
        })
      }
    }
  },
  { deep: true }
)

const handleCusDateChange = (va1: any) => {
  customTimes.value = va1
}

// 初始热点事件详情页面得查询条件
const initHotDetailData = (detailData: any) => {
  if (detailData) {
    // 重新格式化标签
    nextTick(() => {
      const filterJsonStr = detailData?.filterJson

      let filterJson: any = {}
      if (filterJsonStr) {
        try {
          filterJson = JSON.parse(filterJsonStr)
        } catch (error) {
          //
        }
      }
      console.log('筛选组件热点详情数据', filterJson)

      // 关键词参数
      const keywords = filterJson?.keywords?.split(' ')
      formData.value.keyWords = keywords?.length ? keywords : undefined
      const contentTypes = filterJson?.contentTypes || filterJson?.contentDTypes
      formData.value.contentTypes = contentTypes?.length ? contentTypes : undefined
      config.value.forEach(field => {
        if (field.prop === 'customRangeTimes') {
          // 日期范围
          formData.value[field.prop] = filterJson?.customRangeTimes
          customTimes.value = filterJson?.customRangeTimes
          formData.value['dateRange'] = 'custom'
        } else if (field.prop === 'brandCodeList') {
          // 品牌
          formData.value[field.prop] =
            filterJson?.brandCodeList || filterJson?.brandList || filterJson?.brandCode
        } else if (field.prop === 'carSeriesList') {
          // 车系
          formData.value[field.prop] = filterJson?.seriesList || filterJson?.carSeriesList
        } else if (field.prop === 'channelIds') {
          // 数据源
          formData.value[field.prop] = filterJson?.dataDSource || filterJson?.channelIds
        } else if (field.prop === 'contentTypes') {
          // 数据类型
          formData.value[field.prop] = filterJson?.contentDTypes || filterJson?.contentTypes
        } else if (field.type === 'experienceCodeLinkage') {
          // 客户体验代码 目前值设置成功 但是组件内部数据没更新导致数据回显不成功
          // 第一个下拉的值
          if (filterJson.tagTypeDProp || filterJson.tagTypeProp) {
            formData.value['tagType'] = filterJson.tagTypeDProp || filterJson.tagTypeProp
            formData.value['tagTypeProp'] = filterJson.tagTypeDProp || filterJson.tagTypeProp
          }

          // 第二个级联下拉的数据
          const xxxxx = cloneDeep(filterJson?.experienceCode || filterJson?.experienceDCode)
          if (xxxxx && xxxxx.length > 0) {
            const l = xxxxx.length - 1
            xxxxx?.forEach((ele: any, ix: any) => {
              if (ix < l) {
                xxxxx[ix] = [undefined]
              }
            })
            formData.value['experienceCode'] = xxxxx
          } else {
            formData.value['experienceCode'] = undefined
          }
        } else if (field.prop === 'topicCodes') {
          // 标准观点
          formData.value[field.prop] = filterJson?.standardViewpoint || filterJson?.topicCodes
        } else if (field.prop === 'custProvinceCodeSet') {
          // 省份
          formData.value[field.prop] = filterJson?.province || filterJson?.custProvinceCodeSet
        } else if (field.prop === 'scenarioAttr') {
          // 属性标签
          formData.value[field.prop] = filterJson?.scenarioAttr
        }
      })
      loadingDetailLoading.value = false
      nextTick(() => {
        filterTags.value = formatFilterTags(
          config.value,
          formData.value,
          customTimes.value,
          channelOptions.value,
          standardViewpointOptions.value,
          localCompetitiveTreeOptions.value,
          localNewCarSeriesOptions.value,
          experienceCodeNames.value,
          mainAccOptions.value
        )
      })
      nextTick(() => {
        handleSearch('hotDetailInit')
      })
    })
  }
}

// 监听热点事件详情加载完成，将筛选条件重新格式化标签
watch(
  () => hotDetailData.value,
  newVal => {
    initHotDetailData(newVal)
  },
  { deep: true, immediate: true }
)

// 监听竞品品牌车系选项加载完成，如果已有选中值则刷新筛选标签
watch(
  () => localCompetitiveTreeOptions.value,
  newVal => {
    if (!newVal || newVal.length === 0) {
      return
    }

    const competitiveCascaderField = config.value.find(
      field => field.type === 'cascader' && field.prop === COMPETITIVE_SERIES_PROP
    )

    if (competitiveCascaderField) {
      const currentValue = formData.value[competitiveCascaderField.prop]

      if (Array.isArray(currentValue) && currentValue.length > 0) {
        const resolvedValue = resolveCascaderFieldValue(
          currentValue,
          competitiveCascaderField,
          newVal
        )
        const hasValueChanged = !isSameFieldValue(currentValue, resolvedValue)

        if (hasValueChanged) {
          formData.value[competitiveCascaderField.prop] = resolvedValue
        }

        nextTick(() => {
          filterTags.value = formatFilterTags(
            config.value,
            formData.value,
            customTimes.value,
            channelOptions.value,
            standardViewpointOptions.value,
            localCompetitiveTreeOptions.value,
            localNewCarSeriesOptions.value,
            experienceCodeNames.value,
            mainAccOptions.value
          )
        })

        if (hasValueChanged && ['OriginalData', 'SentimentBranchData'].includes(props.routeName)) {
          nextTick(() => {
            handleSearch()
          })
        }
      }

      return
    }

    const hasValueChanged = syncCompetitiveSelectFieldValues({
      allowDeriveBrandFromSeries: true
    })

    nextTick(() => {
      filterTags.value = formatFilterTags(
        config.value,
        formData.value,
        customTimes.value,
        channelOptions.value,
        standardViewpointOptions.value,
        localCompetitiveTreeOptions.value,
        localNewCarSeriesOptions.value,
        experienceCodeNames.value,
        mainAccOptions.value
      )
    })

    // 原声查询页签可能先基于旧缓存/共享缓存发起查询；
    // 当竞品树加载完成且值被纠正后，需要补发一次查询，确保接口拿到最终格式。
    if (hasValueChanged) {
      nextTick(() => {
        handleSearch()
      })
    }
  },
  { deep: true }
)

/**
 * 监听动态默认值依赖的数据源变化，并在字段仍为空时补齐默认值。
 * 仅补一次：一旦字段已有值或被缓存/路由参数命中，就会从待处理列表中移除。
 */
watchEffect(() => {
  if (!isFormDataInitialized.value) {
    return
  }

  const currentPendingProps = pendingDynamicDefaultProps.value
  const currentRouteBrandCode = pendingRouteBrandCode.value

  if (!currentRouteBrandCode && currentPendingProps.length === 0) {
    return
  }

  let hasUpdated = false

  if (currentRouteBrandCode && applyRouteBrandOverride(currentRouteBrandCode)) {
    const brandField = getBrandField()
    if (brandField) {
      pendingDynamicDefaultProps.value = pendingDynamicDefaultProps.value.filter(
        prop => prop !== brandField.prop
      )
    }
    hasUpdated = true
  }

  const nextPendingProps: string[] = []
  currentPendingProps.forEach(prop => {
    const field = config.value.find(item => item.prop === prop)
    if (!field || typeof field.getDefaultValue !== 'function') {
      return
    }

    if (!isFieldEmpty(field, formData.value[prop])) {
      return
    }

    const rawDefaultValue = getFieldDefaultValue(field)
    if (rawDefaultValue === undefined) {
      nextPendingProps.push(prop)
      return
    }

    const normalizedDefaultValue = normalizeFieldDefaultValue(field, rawDefaultValue)
    if (isFieldEmpty(field, normalizedDefaultValue)) {
      nextPendingProps.push(prop)
      return
    }

    formData.value[prop] = normalizedDefaultValue

    // 新车上市 新品车系
    if (field.prop === 'newCarSeriesList') {
      syncNewCarSeriesSelection(normalizedDefaultValue, field)
    }

    // 新车上市 对比车系
    if (field.prop === 'compCarSeriesList') {
      // 递归遍历newCarSeriesOptions找到属性key等于value的项 并返回这个对象
      const selectedOption = findOptionByKey(
        localCompetitiveTreeOptions.value,
        normalizedDefaultValue
      )
      console.log(
        '选中的对比车系选项:',
        selectedOption,
        'normalizedDefaultValue',
        normalizedDefaultValue
      )
      if (selectedOption) {
        // 中转页面跳转 或者 报告列表跳转过来
        // if (!route?.query?.centerJudge) {
        // }
        queryStore.updateQueryParams({
          compCarSeriesObjList: [selectedOption]
        })
      }
    }

    hasUpdated = true
  })

  const hasPendingChanged =
    nextPendingProps.length !== pendingDynamicDefaultProps.value.length ||
    nextPendingProps.some((prop, index) => prop !== pendingDynamicDefaultProps.value[index])

  if (hasPendingChanged) {
    pendingDynamicDefaultProps.value = nextPendingProps
  }

  // 处理新车上市页面重复触发搜索请求的问题
  if (currentPendingProps.length === 1 && props.routeName === 'mainAccount') {
    return
  }

  if (hasUpdated) {
    nextTick(() => {
      handleSearch()
    })
  }
})

/**
 * 构建缓存参数（只包含当前页面配置的字段）
 */
function buildCacheParams(
  options: {
    includeFields?: boolean
    includeTime?: boolean
    includeRestrictedFields?: boolean
    fieldKeys?: Set<string>
    excludedFieldKeys?: Set<string>
  } = {}
): Record<string, any> {
  const {
    includeFields = true,
    includeTime = true,
    includeRestrictedFields = false,
    fieldKeys,
    excludedFieldKeys
  } = options

  // 获取当前页面配置中的所有 prop 字段
  const currentPageProps = getCurrentPagePropKeys()

  const params: Record<string, any> = {}

  // 只包含当前页面配置的字段
  if (includeFields) {
    Object.keys(formData.value).forEach(key => {
      // 体验代码 / 标准观点默认不进入公共缓存，仅页签私有缓存允许保留
      if (!includeRestrictedFields && (key === 'experienceCode' || key === 'topicCodes')) {
        return
      }

      // 只包含当前页面配置中存在的字段
      if (currentPageProps.has(key) && key !== 'dateRange') {
        if (fieldKeys && !fieldKeys.has(key)) {
          return
        }
        if (excludedFieldKeys?.has(key)) {
          return
        }

        const value = formData.value[key]
        const fieldConfig = config.value.find(field => field.prop === key)

        // 处理品牌和车系字段的 props 转换
        // 如果当前页面使用 value 作为值（如 OriginalData），需要转换为 key 再存储到缓存
        if (fieldConfig?.type === 'brand' && fieldConfig.props?.value === 'value') {
          // 品牌字段：将 value 转换为 key
          const convertedValues = normalizeFieldValues(value).map(currentValue => {
            const matchedBrand = getMatchedBrandOptions(currentValue, fieldConfig).find(
              (item: any) => isMatchedValue([item.value, item.key, item.brandCode], currentValue)
            )
            return matchedBrand?.key || currentValue
          })
          params[key] = fieldConfig.multiple ? convertedValues : (convertedValues[0] ?? value)
        } else if (fieldConfig?.type === 'series' && fieldConfig.props?.value === 'value') {
          // 车系字段：需要先找到品牌，然后从品牌的 children 中查找车系
          const brandField = getBrandField()
          const brandValue = brandField ? formData.value[brandField.prop] : null

          const mergedSeriesOptions = getMergedSeriesOptions(brandValue, brandField)
          const convertedValues = normalizeFieldValues(value).map(currentValue => {
            const matchedSeries = mergedSeriesOptions.find((item: any) =>
              isMatchedValue([item.value, item.key], currentValue)
            )
            return matchedSeries?.key || currentValue
          })

          params[key] = convertedValues
        } else if (
          fieldConfig?.type === 'selectv2' &&
          fieldConfig.prop === COMPETITIVE_BRAND_PROP &&
          fieldConfig.props?.value === 'value'
        ) {
          // 竞品品牌字段：跨页共享缓存统一存 key，便于结果数据/原始数据互转。
          params[key] = resolveCompetitiveBrandFieldValue(
            value,
            fieldConfig,
            localCompetitiveTreeOptions.value,
            'key'
          )
        } else if (
          fieldConfig?.type === 'selectv2' &&
          fieldConfig.prop === COMPETITIVE_SERIES_PROP &&
          fieldConfig.props?.value === 'value'
        ) {
          // 竞品车系字段：跨页共享缓存统一存 key，便于结果数据/原始数据互转。
          const competitiveBrandField = getCompetitiveBrandField()
          const competitiveBrandValue = competitiveBrandField
            ? formData.value[competitiveBrandField.prop]
            : []
          const normalizedSeriesValue =
            localCompetitiveTreeOptions.value.length > 0 &&
            normalizeFieldValues(value).some(Array.isArray)
              ? splitCompetitiveTreeQueryParams(value).compCarSeriesList || value
              : value

          params[key] = resolveCompetitiveSeriesFieldValue(
            normalizedSeriesValue,
            competitiveBrandValue,
            fieldConfig,
            competitiveBrandField,
            localCompetitiveTreeOptions.value,
            true,
            'key'
          )
        } else if (fieldConfig?.type === 'cascader') {
          // 级联字段：统一缓存为 key，避免不同页面 value/key 配置不一致导致回显异常
          // 新车上市页面的两个字段的回显 新品车系 对比车系
          if (fieldConfig.prop === USAGE_SCENARIO_PROP) {
            // 用车场景查询按 value 传参，缓存也保留 value 路径，避免回显转码。
            params[key] = value
          } else if (fieldConfig.prop === 'keyAccounts') {
            const aaa = resolveCascaderFieldValue(
              value,
              fieldConfig,
              getCascaderOptionsByField(fieldConfig),
              fieldConfig?.cascaderProps?.value || 'accountId'
            )
            params[key] = aaa
          } else if (
            fieldConfig.prop === 'newCarSeriesList' ||
            fieldConfig.prop === 'compCarSeriesList'
          ) {
            params[key] = value
          } else {
            params[key] = resolveCascaderFieldValue(
              value,
              fieldConfig,
              getCascaderOptionsByField(fieldConfig),
              'key'
            )
          }
        } else {
          // 其他字段直接使用原值
          params[key] = value
        }
      }
    })
  }

  // 处理时间
  if (includeTime) {
    const dateRangeValue = formData.value.dateRange
    if (dateRangeValue === 'custom') {
      if (customTimes.value && Array.isArray(customTimes.value) && customTimes.value.length === 2) {
        params.startDate = customTimes.value[0]
        params.endDate = customTimes.value[1]
        params.dateRange = 'custom'
        params.globalShortcutValue = '自定义'
      }
    } else if (dateRangeValue) {
      const times = getShortcutDateRange(dateRangeValue)
      params.startDate = times[0]
      params.endDate = times[1]
      params.dateRange = dateRangeValue
      const dimensionItem = getTimeDimensionByCode(dateRangeValue)
      if (dimensionItem && dimensionItem.name) {
        params.globalShortcutValue = dimensionItem.name
      }
    }
  }

  return params
}

// 缓存当前页面的查询条件
const cacheCurrentSearchParams = () => {
  // 报告详情条件由 sceneAnalysisStore 承载，离开时不能再写回公共缓存。
  if (isReportDetailPage.value) {
    return
  }

  const existingGlobalCache = getGlobalSharedCacheParams()

  if (isTimeOnlySharedRoute.value) {
    // 特殊页面：只把时间写入全局共享缓存，其他筛选写入页签私有缓存（如存在）。
    const sharedTimeParams = buildCacheParams({
      includeFields: false,
      includeTime: true
    })
    const updatedGlobalParams: Record<string, any> = { ...existingGlobalCache }
    SHARED_TIME_CACHE_KEYS.forEach(key => {
      delete updatedGlobalParams[key]
    })
    Object.assign(updatedGlobalParams, sharedTimeParams)
    queryStore.setUniversaFilterCacheSearchParams(updatedGlobalParams, GLOBAL_SHARED_CACHE_KEY)

    if (isOriginalSoundQueryTabRoute.value) {
      const sharedFilterParams = buildCacheParams({
        includeFields: true,
        includeTime: false,
        fieldKeys: originalSoundQuerySharedFieldKeys.value
      })
      queryStore.setUniversaFilterCacheSearchParams(
        sharedFilterParams,
        ORIGINAL_SOUND_QUERY_SHARED_CACHE_KEY
      )
    }

    if (isolatedCacheKey.value) {
      const isolatedParams = buildCacheParams({
        includeFields: true,
        includeTime: false,
        includeRestrictedFields: true,
        excludedFieldKeys: isOriginalSoundQueryTabRoute.value
          ? originalSoundQuerySharedFieldKeys.value
          : undefined
      })
      queryStore.setUniversaFilterCacheSearchParams(isolatedParams, isolatedCacheKey.value)
    }
    return
  }

  // 其他页面沿用原有逻辑：整页筛选条件写入公共缓存
  const currentPageParams = buildCacheParams()
  const updatedGlobalParams: Record<string, any> = { ...existingGlobalCache, ...currentPageParams }
  queryStore.setUniversaFilterCacheSearchParams(updatedGlobalParams, GLOBAL_SHARED_CACHE_KEY)
}

// 暴露方法给父组件，用于 tab 切换时主动更新缓存
defineExpose({
  cacheCurrentSearchParams
})

// 路由离开时缓存查询条件
onBeforeRouteLeave(() => {
  cacheCurrentSearchParams()
})

// 监听 formData 变化，实时更新缓存（用于 tab 切换时及时更新）
// 使用防抖，避免频繁更新
let cacheTimer: ReturnType<typeof setTimeout> | null = null
watch(
  () => formData.value,
  () => {
    // 清除之前的定时器
    if (cacheTimer) {
      clearTimeout(cacheTimer)
    }
    // 延迟更新，避免频繁更新（减少延迟时间，确保 tab 切换时能及时更新）
    cacheTimer = setTimeout(() => {
      cacheCurrentSearchParams()
      cacheTimer = null
    }, 100)
  },
  { deep: true }
)

// 组件卸载时清除定时器并立即更新缓存
onBeforeUnmount(() => {
  // 清除定时器
  if (cacheTimer) {
    clearTimeout(cacheTimer)
    cacheTimer = null
  }
  // 立即更新缓存，确保在卸载前保存最新数据（不使用防抖）
  cacheCurrentSearchParams()
})

// 监听路由变化，重新初始化表单数据
watch(
  () => props.routeName,
  () => {
    isFormDataInitialized.value = false
    initFormData()
    isFormDataInitialized.value = true
    initDrillDownConditionOptions()
    initUsageScenarioOptions()
    if (attributeTagOptions.value.length > 0) {
      setAttributeTagOptions(attributeTagOptions.value)
    } else {
      initAttributeTagOptions()
    }
  },
  { immediate: false }
)

// 初始化 - 使用 onMounted 确保组件已挂载
onMounted(() => {
  // 热点事件 根据id查询详情
  if (route?.query?.id && route.name === 'hotDetailEvents') {
    loadingDetailLoading.value = true
    getHotEvDetail({ id: route?.query?.id })
      .then((res: any) => {
        hotDetailData.value = res?.result
      })
      .catch(() => {
        // 错误处理
        loadingDetailLoading.value = false
      })
  }
  // 订阅跳转页面到热点事件详情页面 额外处理逻辑
  const reportJudgeId = route.query.reportJudgeId
  if ((route?.query?.centerJudge || reportJudgeId) && route.name === 'hotDetailEvents') {
    const { reportHotId, reportJudgeId, taskHotId: taskId } = route.query
    const reportId = reportJudgeId || reportHotId
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
            filterJson = undefined
          }
          if (filterJson) {
            const obj = {
              filterJson: JSON.stringify(filterJson?.formData || {})
            }
            initHotDetailData(obj)
          }
        }
      })
    }
  }
  // 首次挂载先初始化数据，但不立即触发查询；
  // 采用另一种赋值方式
  if (props.mainAccList?.length) {
    mainAccOptions.value = props.mainAccList || [] // 直接赋值
  }

  // 后续会在“缓存检查/联动初始化”流程结束后统一触发一次查询，避免重复 emit('search')
  initFormData({ triggerSearch: false })
  isFormDataInitialized.value = true
  initChannelOptions()
  // initMainAccTreeOptions()

  initAttributeTagOptions()
  initDrillDownConditionOptions()
  initUsageScenarioOptions()
  // 不在这里初始化标准观点选项，等待客户体验代码检查完成后再决定是否加载

  // 延迟检查缓存，确保 setSceneOriginData 已经设置缓存
  // 使用多次 nextTick 确保缓存已设置
  nextTick(() => {
    nextTick(() => {
      const sharedCachedParams = getGlobalSharedCacheParams()
      const originalSoundSharedCachedParams = getOriginalSoundQuerySharedCacheParams()
      const isolatedCachedParams = getIsolatedCacheParams()
      const shouldReInitByCache = isTimeOnlySharedRoute.value
        ? hasCachedTimeRange(sharedCachedParams) ||
          Object.keys(originalSoundSharedCachedParams).length > 0 ||
          Object.keys(isolatedCachedParams).length > 0
        : Object.keys(sharedCachedParams).length > 0

      if (shouldReInitByCache) {
        // 如果缓存有数据，重新初始化表单
        initFormData({ triggerSearch: false })
        isFormDataInitialized.value = true
      }
      // 统一触发一次查询：确保本轮初始化（含可能的二次 initFormData）完成后再发起
      handleSearch()
      // 检查是否有客户体验代码的默认值，如果有则触发联动加载标准观点选项
      const experienceCodeField = getExperienceCodeField()
      const standardViewpointField = config.value.find(field => field.prop === 'topicCodes')

      // 标准观点仅在“详情页（报告查看）”场景允许回显。
      // 普通页面/页签切换时不做回显，避免跨页面串值导致 UI 与接口参数不一致。
      let topicCodesSource: Record<string, any> = {}
      if (isReportDetailPage.value) {
        const detailFilter = sceneAnalysisStore.getDetailFilter
        topicCodesSource = detailFilter.formData || {}
      }

      const savedTopicCodes =
        standardViewpointField && topicCodesSource[standardViewpointField.prop]
          ? [
              ...(Array.isArray(topicCodesSource[standardViewpointField.prop])
                ? topicCodesSource[standardViewpointField.prop]
                : [topicCodesSource[standardViewpointField.prop]])
            ]
          : null

      if (experienceCodeField && formData.value[experienceCodeField.prop]) {
        const experienceCodeValue = formData.value[experienceCodeField.prop]
        if (Array.isArray(experienceCodeValue) && experienceCodeValue.length > 0) {
          // 等待ExperienceCodeSelector组件初始化完成后再触发
          nextTick(() => {
            // 通过ref调用ExperienceCodeSelector的方法获取末级ID，然后加载标准观点选项
            const selectorInstance = Array.isArray(experienceCodeSelectorRef.value)
              ? experienceCodeSelectorRef.value[0]
              : experienceCodeSelectorRef.value

            if (selectorInstance && typeof selectorInstance.getLastLevelInfo === 'function') {
              const info = selectorInstance.getLastLevelInfo()
              if (
                info &&
                info.lastLevelCodes &&
                Array.isArray(info.lastLevelCodes) &&
                info.lastLevelCodes.length > 0
              ) {
                // 先持久化回显名称，标准观点选项更新会触发标签重算。
                experienceCodeNames.value = Array.isArray(info.names) ? [...info.names] : []
                // 加载标准观点选项
                initStandardViewpointOptions(info.lastLevelCodes).then(() => {
                  // 标准观点选项加载完成后，恢复保存的值
                  if (standardViewpointField && savedTopicCodes && savedTopicCodes.length > 0) {
                    // 确保值的格式正确（应该是 tagCode 数组）
                    // 过滤掉不在选项中的值（防止选项变化导致的值无效）
                    const validCodes = savedTopicCodes.filter(code => {
                      return standardViewpointOptions.value.some(
                        (option: any) => option.tagCode === code
                      )
                    })
                    if (validCodes.length > 0) {
                      // 使用 nextTick 确保组件已更新
                      nextTick(() => {
                        formData.value[standardViewpointField.prop] = [...validCodes]
                        console.log(
                          '标准观点值已恢复:',
                          validCodes,
                          '选项数量:',
                          standardViewpointOptions.value.length
                        )
                      })
                    } else {
                      console.warn(
                        '标准观点值无效，已保存的值:',
                        savedTopicCodes,
                        '可用选项:',
                        standardViewpointOptions.value.map((o: any) => o.tagCode)
                      )
                    }
                  } else {
                    console.log('没有保存的标准观点值需要恢复')
                  }
                  // 重新格式化标签以确保显示正确
                  nextTick(() => {
                    refreshFilterTags()
                  })
                })
              } else {
                // 如果没有客户体验代码的末级ID，清空标准观点选项
                initStandardViewpointOptions()
              }
            } else {
              // 如果无法获取客户体验代码信息，清空标准观点选项
              initStandardViewpointOptions()
            }
          })
        } else {
          // 如果没有客户体验代码，清空标准观点选项
          initStandardViewpointOptions()
        }
      } else {
        // 如果没有客户体验代码字段或值，清空标准观点选项
        initStandardViewpointOptions()
      }
    })
  })
})

function toggleExpand() {
  isExpanded.value = !isExpanded.value
}

// 查询
async function handleSearch(typeAction?: any) {
  // 每次查询时都从 ExperienceCodeSelector 组件获取最新的名称
  // 使用 nextTick 确保组件已经渲染完成
  await nextTick()

  if (props.routeName === 'mainAccount' && formData?.value?.keyAccounts?.length === 0) {
    ElMessage.warning('请至少选择1个账号')
    return
  }

  // experienceCodeSelectorRef.value 可能是数组，需要获取第一个元素
  const selectorInstance = Array.isArray(experienceCodeSelectorRef.value)
    ? experienceCodeSelectorRef.value[0]
    : experienceCodeSelectorRef.value

  // 检查是否有客户体验代码的值
  const experienceCodeField = getExperienceCodeField()
  const hasExperienceCodeValue =
    experienceCodeField &&
    formData.value[experienceCodeField.prop] &&
    Array.isArray(formData.value[experienceCodeField.prop]) &&
    formData.value[experienceCodeField.prop].length > 0

  if (
    shouldLimitExperienceCodeSameLevel.value &&
    experienceCodeField &&
    selectorInstance &&
    typeof selectorInstance.isSelectionSameLevel === 'function' &&
    !selectorInstance.isSelectionSameLevel()
  ) {
    ElMessage.warning(EXPERIENCE_CODE_SAME_LEVEL_WARNING)
    return
  }

  if (selectorInstance) {
    // 检查方法是否存在
    if (typeof selectorInstance.getLastLevelInfo === 'function') {
      let info = selectorInstance.getLastLevelInfo()

      // 如果有客户体验代码的值但获取不到名称，使用轮询等待机制
      if (
        hasExperienceCodeValue &&
        (!info || !info.names || !Array.isArray(info.names) || info.names.length === 0)
      ) {
        // 轮询等待组件初始化完成，最多重试30次，每次100ms（总共最多3秒）
        let retryCount = 0
        const maxRetries = 30

        while (retryCount < maxRetries) {
          await new Promise(resolve => setTimeout(resolve, 100))
          info = selectorInstance.getLastLevelInfo()
          if (info && info.names && Array.isArray(info.names) && info.names.length > 0) {
            break
          }
          retryCount++
        }
      }

      if (info && info.names && Array.isArray(info.names) && info.names.length > 0) {
        experienceCodeNames.value = info.names
      } else {
        experienceCodeNames.value = []
      }
    } else {
      experienceCodeNames.value = []
    }
  } else {
    experienceCodeNames.value = []
  }

  //  处理热点事件详情页面 初始化回显被置空bug 最小化改动 不动其他逻辑
  if (typeAction === 'hotDetailInit' && ['hotDetailEvents'].includes(props.routeName)) {
    if (!experienceCodeNames.value?.length) {
      const filterJsonStr = hotDetailData.value?.filterJson

      let filterJson: any = {}
      if (filterJsonStr) {
        try {
          filterJson = JSON.parse(filterJsonStr)
          experienceCodeNames.value = filterJson?.tydmObj?.names || []
        } catch (error) {
          //
        }
      }
    }
  }

  // 格式化标签
  filterTags.value = formatFilterTags(
    config.value,
    formData.value,
    customTimes.value,
    channelOptions.value,
    standardViewpointOptions.value,
    localCompetitiveTreeOptions.value,
    localNewCarSeriesOptions.value,
    experienceCodeNames.value,
    mainAccOptions.value
  )

  const newCarSeriesField = getNewCarSeriesField()
  if (
    newCarSeriesField &&
    !isFieldEmpty(newCarSeriesField, formData.value[newCarSeriesField.prop])
  ) {
    syncNewCarSeriesSelection(formData.value[newCarSeriesField.prop], newCarSeriesField)
  }

  // 从 DatePicker 组件获取时间范围（支持快捷选项和自定义时间）
  let startDate = ''
  let endDate = ''

  const dateRangeValue = formData.value.dateRange

  // 如果 dateRange 是 'custom'，直接使用双向绑定的 customTimes
  if (dateRangeValue === 'custom') {
    if (customTimes.value && Array.isArray(customTimes.value) && customTimes.value.length === 2) {
      startDate = customTimes.value[0]
      endDate = customTimes.value[1]
    }
  } else if (dateRangeValue) {
    // 如果是快捷选项的 code，直接使用 getShortcutDateRange 计算
    const times = getShortcutDateRange(dateRangeValue)
    startDate = times[0]
    endDate = times[1]
  }

  // 只保留当前页面配置中定义的字段，过滤掉其他页面的多余字段
  const currentPageProps = getCurrentPagePropKeys()

  // 构建查询参数，只包含当前页面配置的字段
  const params: Record<string, any> = {
    startDate,
    endDate
  }

  // 遍历当前页面配置的字段，从 formData 中提取值
  currentPageProps.forEach(prop => {
    if (prop in formData.value) {
      const value = formData.value[prop]
      const fieldConfig = config.value.find(field => field.prop === prop)
      // 跳过 dateRange（已经在上面处理为 startDate 和 endDate）
      if (prop === 'dateRange') {
        return
      }

      // 仅旧级联模式需要在查询时按节点层级拆成品牌和车系两个字段。
      if (prop === COMPETITIVE_SERIES_PROP && fieldConfig?.type === 'cascader') {
        Object.assign(params, splitCompetitiveTreeQueryParams(value))
        return
      }

      // 用车场景内部保留级联路径，查询接口按一级/二级名称集合接收。
      if (prop === USAGE_SCENARIO_PROP && fieldConfig?.type === 'cascader') {
        Object.assign(params, splitUsageScenarioQueryParams(value))
        return
      }

      // 处理 experienceCode：将四个级别的多选值分别赋值给firstCodeTag等字段
      // value 格式: [level1Codes[], level2Codes[], level3Codes[], level4Codes[]]
      // 每个元素都是数组（如果该级别有值的话）
      if (prop === 'experienceCode' && Array.isArray(value)) {
        const [first, second, three, four] = value

        // 旅程分析页面特殊处理：如果第一级是 'all'（虚拟标签），需要过滤掉并向前提一级
        const isJourneyAnalysis = props.routeName === 'journeyAnalysis'
        const firstArray = first ? (Array.isArray(first) ? first : [first]) : []
        const isFirstLevelAll =
          firstArray.length > 0 && firstArray.every((code: string) => code === 'all')

        if (isJourneyAnalysis && isFirstLevelAll) {
          // 旅程分析页面：第一级是 'all'，过滤掉第一级，后面的级别向前提一级
          // 二级 -> firstCodeTag
          if (second) {
            const secondArray = Array.isArray(second) ? second : [second]
            const filteredSecond = secondArray.filter(
              (code: string) =>
                code && typeof code === 'string' && code !== 'all' && code.trim() !== ''
            )
            if (filteredSecond.length > 0) {
              params.firstCodeTag = filteredSecond
              // 旅程分析接口兼容单值标签参数，单选时同步传 tag1Code
              if (filteredSecond.length === 1) {
                params.tag1Code = filteredSecond[0]
              }
            }
          }

          // 三级 -> secondCodeTag
          if (three) {
            const threeArray = Array.isArray(three) ? three : [three]
            const filteredThree = threeArray.filter(
              (code: string) =>
                code && typeof code === 'string' && code !== 'all' && code.trim() !== ''
            )
            if (filteredThree.length > 0) {
              params.secondCodeTag = filteredThree
            }
          }

          // 四级 -> threeCodeTag
          if (four) {
            const fourArray = Array.isArray(four) ? four : [four]
            const filteredFour = fourArray.filter(
              (code: string) =>
                code && typeof code === 'string' && code !== 'all' && code.trim() !== ''
            )
            if (filteredFour.length > 0) {
              params.threeCodeTag = filteredFour
            }
          }
        } else {
          // 其他页面或第一级不是 'all' 的情况，使用原有逻辑
          // 处理第一级：过滤掉 'all' 和空值，确保是数组格式
          if (first) {
            const filteredFirst = firstArray.filter(
              (code: string) =>
                code && typeof code === 'string' && code !== 'all' && code.trim() !== ''
            )
            if (filteredFirst.length > 0) {
              params.firstCodeTag = filteredFirst
            }
          }

          // 处理第二级：过滤掉 'all' 和空值，确保是数组格式
          if (second) {
            const secondArray = Array.isArray(second) ? second : [second]
            const filteredSecond = secondArray.filter(
              (code: string) =>
                code && typeof code === 'string' && code !== 'all' && code.trim() !== ''
            )
            if (filteredSecond.length > 0) {
              params.secondCodeTag = filteredSecond
            }
          }

          // 处理第三级：过滤掉 'all' 和空值，确保是数组格式
          if (three) {
            const threeArray = Array.isArray(three) ? three : [three]
            const filteredThree = threeArray.filter(
              (code: string) =>
                code && typeof code === 'string' && code !== 'all' && code.trim() !== ''
            )
            if (filteredThree.length > 0) {
              params.threeCodeTag = filteredThree
            }
          }

          // 处理第四级：过滤掉 'all' 和空值，确保是数组格式
          if (four) {
            const fourArray = Array.isArray(four) ? four : [four]
            const filteredFour = fourArray.filter(
              (code: string) =>
                code && typeof code === 'string' && code !== 'all' && code.trim() !== ''
            )
            if (filteredFour.length > 0) {
              params.fourCodeTag = filteredFour
            }
          }
        }

        // 不添加 experienceCode 字段到 params
        return
      }

      // 其他字段直接添加
      params[prop] = value
    }
  })
  // 构建 tagPath（标签路径信息）
  let tagPath: Array<{ code: string; name: string; level?: number }> | undefined = undefined

  // 检查是否有 experienceCode 字段并构建 tagPath
  // 注意：experienceCodeField 在上面已经声明（第952行），这里直接使用
  if (experienceCodeField && formData.value[experienceCodeField.prop]) {
    const experienceCodeValue = formData.value[experienceCodeField.prop]
    if (Array.isArray(experienceCodeValue) && experienceCodeValue.length > 0) {
      // 从 ExperienceCodeSelector 组件获取标签路径信息
      // experienceCodeSelectorRef.value 可能是数组，需要获取第一个元素
      const tagPathSelectorInstance = Array.isArray(experienceCodeSelectorRef.value)
        ? experienceCodeSelectorRef.value[0]
        : experienceCodeSelectorRef.value
      if (tagPathSelectorInstance && typeof tagPathSelectorInstance.getTagPath === 'function') {
        tagPath = tagPathSelectorInstance.getTagPath()
      }
    }
  }
  // 热点事件关键词参数
  if (route.name === 'hotDetailEvents') {
    params.keyWords = formData.value.keyWords
    params.contentTypes = formData.value.contentTypes
    params.tagType = formData.value.tagType
  }

  // 将查询条件保存到通用查询参数中,用于下钻时使用
  queryStore.commonQueryParams = params
  // 将原始 formData 保存到 store，用于发布报告弹窗回显
  queryStore.setUniversaFilterFormData(formData.value, customTimes.value)

  emit('search', params, tagPath)
  if (isExpanded.value) {
    toggleExpand()
  }
}

// 重置
function handleReset() {
  // 报告详情重置到报告发布时的条件，不清理用户在普通页面保留的缓存。
  if (isReportDetailPage.value) {
    customTimes.value = []
    initFormData()
    emit('reset')
    return
  }

  // 热点事件详情重置逻辑完全不一样 需要单独处理
  if (['hotDetailEvents'].includes(props.routeName)) {
    initHotDetailData(hotDetailData.value)
    return
  }
  // 特殊页面只清理共享时间 + 当前页签私有缓存，避免影响其他页面的公共筛选状态。
  if (isTimeOnlySharedRoute.value) {
    clearSharedTimeCache()
    if (isOriginalSoundQueryTabRoute.value) {
      clearOriginalSoundQuerySharedCache()
    }
    clearIsolatedCache()
  } else {
    // 其他页面沿用原有行为，重置后直接回到自身默认值。
    queryStore.setUniversaFilterCacheSearchParams({}, GLOBAL_SHARED_CACHE_KEY)
  }
  customTimes.value = []
  initFormData()
  emit('reset')
}

// 获取品牌的值（用于传递给车系选择器）
const brandValue = computed(() => {
  // 查找品牌字段的配置
  const brandField = config.value.find(field => field.type === 'brand')
  if (brandField) {
    // 优先使用表单中的值，如果没有则使用默认值
    return formData.value[brandField.prop] ?? brandField.defaultValue ?? null
  }
  return null
})

// 品牌是否已选择（用于控制车系显隐）
const isBrandSelected = computed(() => !isEmptyBrandValue(brandValue.value))

// 获取竞品品牌的值（用于竞品车系联动）
const competitiveBrandValue = computed(() => {
  const competitiveBrandField = getCompetitiveBrandField()
  if (competitiveBrandField) {
    return formData.value[competitiveBrandField.prop] ?? competitiveBrandField.defaultValue ?? []
  }
  return []
})

// 竞品品牌是否已选择（用于控制竞品车系显隐）
const isCompetitiveBrandSelected = computed(() => {
  return !isEmptyBrandValue(competitiveBrandValue.value)
})

/**
 * 同步竞品品牌/车系双下拉的值格式
 * 1. 兼容原始数据页在缓存中存 key、页面展示 value 的差异。
 * 2. 兼容旧版“竞品品牌车系级联”缓存，必要时自动拆解出品牌和车系。
 * 3. 当用户切换竞品品牌时，自动剔除已失效的竞品车系。
 * @param options 同步选项
 * @returns 本次是否修正了字段值
 */
const syncCompetitiveSelectFieldValues = (
  options: { allowDeriveBrandFromSeries?: boolean } = {}
) => {
  const { allowDeriveBrandFromSeries = false } = options
  const competitiveBrandField = getCompetitiveBrandField()
  const competitiveSeriesField = getCompetitiveSeriesField()

  if (
    !competitiveBrandField ||
    !competitiveSeriesField ||
    localCompetitiveTreeOptions.value.length === 0
  ) {
    return false
  }

  const currentBrandValue = formData.value[competitiveBrandField.prop]
  const currentSeriesValue = formData.value[competitiveSeriesField.prop]
  const normalizedSeriesValue = normalizeFieldValues(currentSeriesValue)
  const derivedCompetitiveParams =
    normalizedSeriesValue.length > 0 ? splitCompetitiveTreeQueryParams(currentSeriesValue) : {}
  const derivedBrandValue = derivedCompetitiveParams.compBrandCodeList || []
  const derivedSeriesValue = derivedCompetitiveParams.compCarSeriesList || []
  const hasLegacySeriesPath = normalizedSeriesValue.some(Array.isArray)

  const nextBrandValue = resolveCompetitiveBrandFieldValue(
    allowDeriveBrandFromSeries && isEmptyBrandValue(currentBrandValue)
      ? derivedBrandValue
      : currentBrandValue,
    competitiveBrandField
  )
  const nextSeriesValue = resolveCompetitiveSeriesFieldValue(
    hasLegacySeriesPath ? derivedSeriesValue : currentSeriesValue,
    nextBrandValue,
    competitiveSeriesField,
    competitiveBrandField,
    localCompetitiveTreeOptions.value,
    allowDeriveBrandFromSeries
  )

  let hasChanged = false

  if (!isSameFieldValue(currentBrandValue, nextBrandValue)) {
    formData.value[competitiveBrandField.prop] = nextBrandValue
    hasChanged = true
  }

  if (!isSameFieldValue(currentSeriesValue, nextSeriesValue)) {
    formData.value[competitiveSeriesField.prop] = nextSeriesValue
    hasChanged = true
  }

  return hasChanged
}

// 监听竞品品牌变化：
// 1. 竞品品牌清空时，竞品车系同步清空；
// 2. 移除某个竞品品牌时，仅移除不再属于剩余品牌的竞品车系。
watch(
  () => competitiveBrandValue.value,
  (newVal, oldVal) => {
    if (!getCompetitiveSeriesField() || localCompetitiveTreeOptions.value.length === 0) {
      return
    }

    const hasBrandChanged = !isSameFieldValue(newVal, oldVal)
    if (!hasBrandChanged || !isFormDataInitialized.value || isInitializingFormData.value) {
      return
    }

    const competitiveSeriesField = getCompetitiveSeriesField()
    const currentSeriesValue = competitiveSeriesField
      ? formData.value[competitiveSeriesField.prop]
      : []
    const nextSeriesValue = filterCompetitiveSeriesByBrand(currentSeriesValue, newVal)
    const hasSeriesChanged = !isSameFieldValue(currentSeriesValue, nextSeriesValue)

    if (competitiveSeriesField && hasSeriesChanged) {
      formData.value[competitiveSeriesField.prop] = nextSeriesValue
    }

    if (hasBrandChanged || hasSeriesChanged) {
      nextTick(() => {
        filterTags.value = formatFilterTags(
          config.value,
          formData.value,
          customTimes.value,
          channelOptions.value,
          standardViewpointOptions.value,
          localCompetitiveTreeOptions.value,
          localNewCarSeriesOptions.value,
          experienceCodeNames.value,
          mainAccOptions.value
        )
      })
    }
  },
  { deep: true }
)
</script>

<template>
  <div class="universa-filter">
    <div class="uf-header">
      <div class="ufh-left">
        <div>筛选范围</div>
        <div class="ufhl-tags">
          <template v-for="(tag, index) in filterTags" :key="index">
            <div class="ufhl-tag">{{ tag }}</div>
          </template>
        </div>
      </div>
      <div class="ufh-right ml-16" @click="toggleExpand">
        <template v-if="!isExpanded">
          <span class="mr-8">展开筛选</span>
          <SvgIcon name="chevron-down" width="20px" height="20px" color="#929AA6" />
        </template>
        <template v-else>
          <span class="mr-8">收起筛选</span>
          <SvgIcon name="chevron-up" width="20px" height="20px" color="#929AA6" />
        </template>
      </div>
    </div>
    <!-- name="expand" -->
    <Transition>
      <div class="uf-content" v-show="isExpanded">
        <el-form :model="formData" @submit.prevent>
          <el-row :gutter="24">
            <template v-for="(field, index) in config" :key="field.prop || index">
              <!-- 占位符 -->
              <el-col v-if="field.type === 'placeholder'" :span="field.span || 6"> </el-col>

              <!-- 热点事件详情页面的时间 -->

              <el-col
                v-else-if="
                  ['hotDetailEvents', 'hotDetailOriginalEvents'].includes(routeName) &&
                  field.prop === 'customRangeTimes'
                "
                :span="field.span || 6"
              >
                <el-form-item :label="field.label" :prop="field.prop">
                  <el-date-picker
                    v-model="formData[field.prop]"
                    type="daterange"
                    placeholder="自定义"
                    start-placeholder="开始时间"
                    end-placeholder="结束时间"
                    format="YYYY.MM.DD"
                    value-format="YYYY-MM-DD"
                    :clearable="false"
                    style="width: 50%"
                    @change="handleCusDateChange"
                  />
                  <span>跨度最长可选 1 年，建议控制在 1 个月内</span>
                </el-form-item>
              </el-col>
              <!-- 日期范围 -->
              <el-col v-else-if="field.type === 'daterange'" :span="field.span || 6">
                <el-form-item :label="field.label" :prop="field.prop">
                  <div>
                    <DatePicker
                      ref="datePickerRef"
                      v-model="formData[field.prop]"
                      v-model:custom-times="customTimes"
                      :default-value="field.defaultValue"
                    />
                    <!-- <el-date-picker
                      v-model="formData[field.prop]"
                      type="daterange"
                      :placeholder="field.placeholder"
                      style="width: 280px"
                    /> -->
                  </div>
                </el-form-item>
              </el-col>
              <!-- 重点账号 -->
              <el-col v-else-if="field.prop === 'keyAccounts'" :span="field.span || 6">
                <el-form-item :label="field.label" :prop="field.prop">
                  <el-cascader
                    v-model="formData[field.prop]"
                    :options="mainAccOptions"
                    collapse-tags
                    :max-collapse-tags="1"
                    :show-all-levels="false"
                    filterable
                    clearable
                    :key="field.prop"
                    :props="field.cascaderProps"
                    :teleported="true"
                    style="width: 25%"
                  />
                  <span
                    v-if="formData[field.prop] && formData[field.prop].length === 0"
                    style="color: #f5222d; margin-left: 5px"
                    >请至少选择1个账号</span
                  >
                </el-form-item>
              </el-col>

              <!-- 数据源 -->
              <el-col v-else-if="field.type === 'dataSource'" :span="field.span || 6">
                <el-form-item :label="field.label" :prop="field.prop">
                  <DataSourceCascader
                    v-model="formData[field.prop]"
                    :options="channelOptions"
                    :condition="{ multiSelect: field.multiple ?? true }"
                    :child-key="field.prop"
                    :wait-for-parent="true"
                    class="w-full"
                  />
                </el-form-item>
              </el-col>
              <!-- 品牌 -->
              <el-col v-else-if="field.type === 'brand'" :span="field.span || 6">
                <el-form-item :label="field.label" :prop="field.prop">
                  <!--  :options="field.options" -->
                  <BrandSelector
                    v-model="formData[field.prop]"
                    :options="field.options || []"
                    :props="field.props"
                    :multiple="field.multiple"
                    :disabled="field.disabled"
                    :clearable="field.clearable"
                  />
                </el-form-item>
              </el-col>
              <!-- 车系 -->
              <el-col
                v-else-if="field.type === 'series' && isBrandSelected"
                :span="field.span || 6"
              >
                <el-form-item :label="field.label" :prop="field.prop">
                  <SeriesSelector
                    v-model="formData[field.prop]"
                    :brand-value="brandValue"
                    :options="field.options || []"
                    :props="field.props"
                    :disabled="field.disabled"
                  />
                </el-form-item>
              </el-col>

              <!-- 级联选择 -->
              <el-col v-else-if="field.type === 'cascader'" :span="field.span || 6">
                <el-form-item :label="field.label" :prop="field.prop">
                  <el-cascader
                    v-model="formData[field.prop]"
                    :options="
                      field.prop === 'compCarSeriesList'
                        ? localCompetitiveTreeOptions
                        : field.prop === 'newCarSeriesList'
                          ? localNewCarSeriesOptions
                          : field.options || []
                    "
                    :props="field.cascaderProps"
                    :disabled="field.disabled"
                    :clearable="field.clearable"
                    :placeholder="field.placeholder || '请选择'"
                    :max-collapse-tags="1"
                    :show-all-levels="false"
                    collapse-tags
                    filterable
                    class="w-full"
                    @change="value => handleCascaderChange(value, field)"
                  />
                </el-form-item>
              </el-col>

              <!-- 体验代码 -->
              <el-col v-else-if="field.type === 'experienceCode'" :span="field.span || 6">
                <el-form-item :label="field.label" :prop="field.prop">
                  <ExperienceCodeSelector
                    ref="experienceCodeSelectorRef"
                    v-model="formData[field.prop]"
                    :disabled="field.disabled"
                    :page-name="routeName"
                    @change="handleExperienceCodeChange"
                  />
                </el-form-item>
              </el-col>

              <!-- 热点事件的体验代码 -->
              <el-col
                v-else-if="
                  ['hotDetailEvents'].includes(routeName) &&
                  field.type === 'experienceCodeLinkage' &&
                  !loadingDetailLoading
                "
                :span="field.span || 6"
              >
                <el-form-item :label="field.label" :prop="field.prop">
                  <ExperienceCodeHotSelector
                    ref="experienceCodeSelectorRef"
                    v-model="formData.experienceCode"
                    v-model:tagType="formData.tagTypeProp"
                    :type-options="experienceCodeTypeOptions"
                    :default-tag-type="(field as any).tagTypeDefaultValue"
                    :disabled="field.disabled"
                    tyPageType="hotFilterType"
                    @change="handleExperienceCodeChange"
                    @handTagValueChange="onTagTypeChange"
                  />
                </el-form-item>
              </el-col>

              <!-- 体验代码 -->
              <el-col v-else-if="field.type === 'experienceCodeLinkage'" :span="field.span || 6">
                <el-form-item :label="field.label" :prop="field.prop">
                  <ExperienceCodeLinkageSelector
                    ref="experienceCodeSelectorRef"
                    v-model="formData[field.prop]"
                    v-model:tagType="formData[(field as any).tagTypeProp || 'tagType']"
                    :type-options="experienceCodeTypeOptions"
                    :default-tag-type="(field as any).tagTypeDefaultValue"
                    :fixed-tag-type="(field as any).fixedTagType"
                    :hide-tag-type="!!(field as any).hideTagType"
                    :root-tag-name="(field as any).rootTagName"
                    :request-level="(field as any).requestLevel"
                    :hide-root-in-cascader="!!(field as any).hideRootInCascader"
                    :disabled="field.disabled"
                    :tyPageType="field.tyPageType"
                    :same-level-only="shouldLimitExperienceCodeSameLevel"
                    @change="handleExperienceCodeChange"
                  />
                </el-form-item>
              </el-col>

              <!-- 下拉选择 v2（用于大量数据） -->
              <el-col v-else-if="field.type === 'selectv2'" :span="field.span || 6">
                <el-form-item :label="field.label" :prop="field.prop">
                  <SelectV2WithSelectAll
                    v-if="field.showSelectAll"
                    v-model="formData[field.prop]"
                    :options="getSelectV2Options(field)"
                    :props="field.props"
                    :clearable="field.clearable"
                    :placeholder="getSelectV2Placeholder(field)"
                    :multiple="field.multiple"
                    :max-collapse-tags="1"
                    :collapse-tags="true"
                    :filterable="true"
                    :show-select-all="field.showSelectAll"
                    :disabled="isSelectV2Disabled(field)"
                  />
                  <el-select-v2
                    v-else
                    v-model="formData[field.prop]"
                    :options="getSelectV2Options(field)"
                    :props="field.props"
                    :clearable="field.clearable"
                    :placeholder="getSelectV2Placeholder(field)"
                    :multiple="field.multiple"
                    :max-collapse-tags="1"
                    :disabled="isSelectV2Disabled(field)"
                    collapse-tags
                    filterable
                  />
                </el-form-item>
              </el-col>

              <!-- 下拉选择 -->
              <el-col v-else-if="field.type === 'select'" :span="field.span || 6">
                <el-form-item :label="field.label" :prop="field.prop">
                  <el-select
                    v-model="formData[field.prop]"
                    :options="field.options"
                    :props="field.props"
                    :clearable="field.clearable"
                    :placeholder="field.placeholder"
                    :multiple="field.multiple"
                    :max-collapse-tags="1"
                    :collapseTags="true"
                    filterable
                  />
                </el-form-item>
              </el-col>

              <!-- 输入框 -->
              <el-col v-else-if="field.type === 'input'" :span="field.span || 6">
                <el-form-item :label="field.label" :prop="field.prop">
                  <el-input
                    v-model="formData[field.prop]"
                    :placeholder="field.placeholder"
                    clearable
                    :maxlength="field.maxLength"
                  />
                </el-form-item>
              </el-col>

              <!-- 按钮开关组 -->
              <el-col v-else-if="field.type === 'btnSwitch'" :span="field.span || 6">
                <el-form-item :label="field.label" :prop="field.prop">
                  <BtnSwitch
                    v-model="formData[field.prop]"
                    :options="
                      field.options?.map((opt: any) => ({
                        label: opt[field.props?.label || 'text'] || opt.label || opt.value,
                        value: opt[field.props?.value || 'value'] || opt.value
                      })) || []
                    "
                    :multiple="field.multiple ?? false"
                    :disabled="field.disabled ?? false"
                  />
                </el-form-item>
              </el-col>

              <!-- 分割线 -->
              <div
                v-if="field.showSplitLine && (field.type != 'series' || isBrandSelected)"
                class="split-line"
              ></div>
            </template>

            <!-- 操作按钮 -->
            <div class="split-line"></div>
            <el-col :span="24">
              <div class="flex-x-end">
                <el-button type="primary" @click="handleSearch">查询</el-button>
                <el-button type="default" @click="handleReset">重置</el-button>
              </div>
            </el-col>
          </el-row>
        </el-form>
      </div>
    </Transition>
  </div>
</template>

<style lang="scss" scoped>
.universa-filter {
  width: calc(100% + 56px);
  margin: 0 -28px;

  .split-line {
    width: calc(100% + 56px);
    margin: 8px -28px;
    height: 1px;
    background-color: #ebedf0;
  }
  .split-line-bottom {
    border-bottom: 1px solid #ebedf0;
  }
  .split-line-top {
    border-top: 1px solid #ebedf0;
  }
  .uf-header {
    width: 100%;
    height: 48px;
    background-color: #fff;
    padding: 0 40px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid #ebedf0;

    .ufh-left {
      font-size: 14px;
      color: #1f2733;
      line-height: 20px;
      display: flex;
      align-items: center;
      gap: 16px;
      flex: 1;
      min-width: 0;

      .ufhl-tags {
        display: flex;
        align-items: center;
        gap: 16px;
        flex: 1;
        overflow-x: auto;
        overflow-y: hidden;
        scrollbar-width: thin;
        scrollbar-color: #cbd5e0 transparent;

        &::-webkit-scrollbar {
          height: 6px;
        }

        &::-webkit-scrollbar-track {
          background: transparent;
        }

        &::-webkit-scrollbar-thumb {
          background-color: #cbd5e0;
          border-radius: 3px;
        }

        &::-webkit-scrollbar-thumb:hover {
          background-color: #a0aec0;
        }
      }

      .ufhl-tag {
        background: #eaf3ff;
        border-radius: 4px;
        border: 1px solid #ebedf0;
        padding: 5px 16px;
        font-weight: 400;
        font-size: 14px;
        color: #1677ff;
        line-height: 20px;
        box-sizing: border-box;
        white-space: nowrap;
        flex-shrink: 0;
      }
    }
    .ufh-right {
      font-size: 14px;
      color: #5f6a7a;
      line-height: 32px;
      display: flex;
      align-items: center;
      cursor: pointer;
    }
  }
  .uf-content {
    background: #f8fbff;
    padding: 16px 40px;
    width: 100%;
    overflow: hidden;
  }
}

.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s ease;
}

.expand-enter-from,
.expand-leave-to {
  max-height: 0;
  padding-top: 0;
  padding-bottom: 0;
  opacity: 0;
}

.expand-enter-to,
.expand-leave-from {
  max-height: 500px;
  opacity: 1;
}
</style>
