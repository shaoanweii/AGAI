<script setup lang="ts">
import { computed, nextTick, reactive, ref, watch, onMounted } from 'vue'

import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { getSpecialZoneOptions, publishReport } from '@/api/overview'
import type { PublishReportQueryParams } from '@/api/overview/type'
import { useQueryStore, useUserStore } from '@/store'
import { useRoute } from 'vue-router'
import { getFilterConfig } from '@/components/Business/UniversaFilter/helper'
import {
  getUserChannelTree,
  findFinalTagLibClientVoListByTagId,
  findAllAttributeLabelList,
  getMainAccTreeData
} from '@/api/common'
import { TagType } from '@/constants'
import BrandSelector from '@/components/Business/UniversaFilter/components/BrandSelector.vue'
import SeriesSelector from '@/components/Business/UniversaFilter/components/SeriesSelector.vue'
import ExperienceCodeSelector from '@/components/Business/UniversaFilter/components/ExperienceCodeSelector.vue'
import ExperienceCodeLinkageSelector from '@/components/Business/UniversaFilter/components/ExperienceCodeLinkageSelector.vue'
import SelectV2WithSelectAll from '@/components/Business/UniversaFilter/components/SelectV2WithSelectAll.vue'
import DataSourceCascader from '@/components/Business/AdvancedFilter/DataSourceCascader.vue'
import DatePicker from '@/components/Business/UniversaFilter/components/DatePicker.vue'
import { cloneDeep, debounce } from 'lodash-es'
import useMiddlewareStore from '@/store/modules/middleware'
import { BrandServiceCategoryOptions } from '@/components/Business/Scene/CompetitorAnalysis/constants'
import SwitchButton from '@/components/UI/SwitchButton/index.vue'
import { getAllBrandOrCarSeriesData } from '@/api/competitorAnalysis'
import type { brandCarSeriesItem } from '@/api/competitorAnalysis/types'
import { findNodeByField } from '@/utils'
import { getSeriesCondition } from '@/api/newCarLaunch'
import { getHotEvDetail } from '@/api/hotAphttp'
import ExperienceCodeHotSelector from '@/components/Business/UniversaFilter/components/ExperienceCodeHotSelector.vue'
import { getDrillDownConditions, type DrillDownConditionItem } from '@/api/drillDownDialog'
import { getDataPlazaConditions } from '@/api/dataPlaza'
import type { DataPlazaConditionOption } from '@/api/dataPlaza/types'
import {
  extractUsageScenarioOptions,
  USAGE_SCENARIO_PROP
} from '@/components/Business/UniversaFilter/usageScenario'

defineOptions({
  name: 'PublishReport'
})

const { tydmArrObj = null } = defineProps<{
  tydmArrObj?: any
}>()
const visible = defineModel<boolean>({ default: false })

const tydmObj = ref<any>(null) // 体验代码回显相关的数据

const queryStore = useQueryStore()
const userStore = useUserStore()
const middlewareStore = useMiddlewareStore()
const route = useRoute()

const hotDetailData = ref<any>(null) // 详情数据 用于回显对应的条件
const experienceCodeTypeOptions = ref<Array<{ key: string; value: string }>>([]) // 体验代码类型选项
// getDictItems

const ruleFormRef = ref<FormInstance>()

const ruleForm = reactive<PublishReportQueryParams>({
  reportName: undefined,
  dateCondition: undefined,
  defaultCondition: undefined,
  firstLevelZoneId: undefined,
  specialTypeId: undefined,
  // 额外参数，用于处理时间
  dateType: undefined,
  dateTimes: [],
  zoneId: []
})

const rules = reactive<FormRules<PublishReportQueryParams>>({
  reportName: [
    { required: true, message: '请输入报告名称', trigger: 'blur' },
    { min: 4, max: 20, message: '字数限制4-20', trigger: 'blur' }
  ],
  zoneId: [{ required: true, message: '请选择专区', trigger: 'change' }]
})

const resetForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.resetFields()
}

const zoneOptions = ref<any[]>()

// 车系条件数据
const seriesConditionData = ref<{
  newCarSeries: Array<any> // 新品车系（自有品牌）
  compareCarSeries: Array<any> // 对比车系（自有+竞品品牌）
}>({
  newCarSeries: [],
  compareCarSeries: []
})

/**
 * @description: 获取专区下拉树
 * @return {*}
 */
const getZoneOptions = async () => {
  try {
    const res = await getSpecialZoneOptions({
      roleIds: userStore.roleId ? [userStore.roleId] : undefined
    })
    if (res.success) {
      zoneOptions.value = res.result
    } else {
      zoneOptions.value = []
    }
  } catch (error: any) {
    ElMessage.error(error.message)
  }
}

/**
 * 获取车系条件数据
 */
const fetchSeriesCondition = async () => {
  try {
    // 接口文档中不需要传递参数，不传递任何参数
    const response = await getSeriesCondition()
    if (response.success && response.result) {
      seriesConditionData.value = response.result
    } else {
      ElMessage.error(response.message || '获取车系条件失败')
    }
  } catch (error) {
    console.error('获取车系条件失败:', error)
    ElMessage.error('获取车系条件失败')
  }
}

// 表单数据，用于存储查询条件的值
const formData = ref<Record<string, any>>({})
const customTimes = ref<string[]>([]) // 自定义时间范围
const channelOptions = ref<any[]>([]) // 数据源选项
const mainAccOptions = ref<any[]>([]) // 重点账号选项
const attributeTagOptions = ref<any[]>([]) // 属性标签选项
const standardViewpointOptions = ref<any[]>([]) // 标准观点选项
const experienceCodeSelectorRef = ref<any>(null) // 客户体验代码选择器引用
const competitiveTreeOptions = ref<any[]>([]) // 竞品品牌车系树
const usageScenarioOptions = ref<DataPlazaConditionOption[]>([]) // 用车场景树

const COMPETITIVE_BRAND_PROP = 'compBrandCodeList'
const COMPETITIVE_SERIES_PROP = 'compCarSeriesList'

// 竞品对比相关数据
const isCompetitorAnalysis = computed(() => route.name === 'competitorAnalysis')
const isRootCause = computed(() => route.name === 'rootCause')
const shouldLimitExperienceCodeSameLevel = computed(() =>
  ['rootCause', 'ResultData', 'productAnalysis', 'serviceAnalysis', 'competitorAnalysis'].includes(
    route.name as string
  )
)
const competitorQueryType = ref<string>('brand') // 品牌车系分类
const competitorFirstCode = ref<string | undefined>() // 本品代码
const competitorSecondCode = ref<string | undefined>() // 竞品代码
const competitorFirstName = ref<string>('') // 本品名称
const competitorSecondName = ref<string>('') // 竞品名称
const competitorBrandCarSeriesOptions = ref<brandCarSeriesItem[]>([]) // 品牌车系选项

const newCarSeriesSelector = ref<any[]>([]) // 新车上市-新品车系 级联选择
const newCarCompareSeriesSelector = ref<any[]>([]) // 新车上市-对比车系 级联选择

// 根据路由名称获取 UniversaFilter 的配置
const filterConfig = computed(() => {
  return getFilterConfig(route.name as string)
})

/**
 * 将单选或多选字段统一转换为有效值数组。
 * @param value 表单字段值
 * @returns 去除空值后的选中值数组
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
 * 判断字段值是否与选项的 key、value 或 code 匹配，兼容已缓存的历史格式。
 * @param option 竞品品牌或车系选项
 * @param value 当前字段值
 * @param valueKey 当前字段配置的取值键
 * @returns 是否匹配
 */
const isMatchedCompetitiveValue = (option: any, value: unknown, valueKey: string) => {
  return [option?.[valueKey], option?.key, option?.value, option?.code].some(
    candidate =>
      candidate !== undefined && candidate !== null && String(candidate) === String(value)
  )
}

/**
 * 获取竞品品牌、车系字段配置，供发布弹窗的选项与联动逻辑复用。
 * @param prop 字段标识
 * @returns 对应筛选字段配置
 */
const getCompetitiveField = (prop: string) => {
  return filterConfig.value.find(field => field.type === 'selectv2' && field.prop === prop)
}

const competitiveBrandValue = computed(() => {
  const field = getCompetitiveField(COMPETITIVE_BRAND_PROP)
  return field ? (formData.value[field.prop] ?? field.defaultValue ?? []) : []
})

const isCompetitiveBrandSelected = computed(
  () => normalizeFieldValues(competitiveBrandValue.value).length > 0
)

/**
 * 根据当前已选竞品品牌聚合车系选项，避免展示不属于所选品牌的车系。
 */
const competitiveSeriesOptions = computed(() => {
  const seriesField = getCompetitiveField(COMPETITIVE_SERIES_PROP)
  const brandField = getCompetitiveField(COMPETITIVE_BRAND_PROP)
  if (!seriesField || !brandField || competitiveTreeOptions.value.length === 0) {
    return []
  }

  const selectedBrands = normalizeFieldValues(competitiveBrandValue.value)
  if (selectedBrands.length === 0) {
    return []
  }

  const brandValueKey = brandField.props?.value || 'key'
  const seriesValueKey = seriesField.props?.value || 'key'
  const seriesMap = new Map<string, any>()

  competitiveTreeOptions.value
    .filter(brand =>
      selectedBrands.some(value => isMatchedCompetitiveValue(brand, value, brandValueKey))
    )
    .forEach(brand => {
      ;(brand.children || []).forEach((series: any) => {
        const seriesKey = String(
          series?.[seriesValueKey] ?? series?.key ?? series?.value ?? series?.code ?? ''
        )
        if (seriesKey && !seriesMap.has(seriesKey)) {
          seriesMap.set(seriesKey, series)
        }
      })
    })

  return Array.from(seriesMap.values())
})

/**
 * 为 select-v2 字段提供动态选项，竞品字段使用下钻条件接口返回的树结构。
 * @param field 筛选字段配置
 * @returns 对应的可选项
 */
const getSelectV2Options = (field: any) => {
  if (field.prop === 'topicCodes') {
    return standardViewpointOptions.value
  }
  if (field.prop === COMPETITIVE_BRAND_PROP) {
    return competitiveTreeOptions.value
  }
  if (field.prop === COMPETITIVE_SERIES_PROP) {
    return competitiveSeriesOptions.value
  }
  return field.options || []
}

/**
 * 获取级联字段的动态选项。
 * @param field 筛选字段配置
 * @returns 对应的级联选项
 */
const getCascaderOptions = (field: any) => {
  return field.prop === USAGE_SCENARIO_PROP ? usageScenarioOptions.value : field.options || []
}

/**
 * 竞品车系需要先选择竞品品牌，其他下拉维持字段原有行为。
 * @param field 筛选字段配置
 * @returns 是否禁用
 */
const isSelectV2Disabled = (field: any) => {
  return field.prop === COMPETITIVE_SERIES_PROP
    ? Boolean(field.disabled) || !isCompetitiveBrandSelected.value
    : Boolean(field.disabled)
}

/**
 * 未选竞品品牌时提示依赖关系，其他下拉沿用原始占位文案。
 * @param field 筛选字段配置
 * @returns 下拉占位文案
 */
const getSelectV2Placeholder = (field: any) => {
  return field.prop === COMPETITIVE_SERIES_PROP && !isCompetitiveBrandSelected.value
    ? '请先选择竞品品牌'
    : field.placeholder
}

/**
 * 竞品品牌变更后只保留属于当前品牌的车系，防止提交失效筛选条件。
 */
watch(
  () => competitiveBrandValue.value,
  () => {
    const seriesField = getCompetitiveField(COMPETITIVE_SERIES_PROP)
    if (!seriesField || competitiveTreeOptions.value.length === 0) {
      return
    }

    const selectedSeries = normalizeFieldValues(formData.value[seriesField.prop])
    if (selectedSeries.length === 0) {
      return
    }

    const seriesValueKey = seriesField.props?.value || 'key'
    const validSeries = selectedSeries.filter(value =>
      competitiveSeriesOptions.value.some(series =>
        isMatchedCompetitiveValue(series, value, seriesValueKey)
      )
    )

    if (JSON.stringify(selectedSeries) !== JSON.stringify(validSeries)) {
      formData.value[seriesField.prop] = validSeries
    }
  },
  { deep: true }
)

/**
 * 根据路由名称获取客户体验代码的 tagLibType
 * @returns TagType 值
 */
const getTagLibTypeByRouteName = (): string => {
  const routeName = route.name as string
  if (['journeyAnalysis'].includes(routeName)) {
    return TagType.UserJourney
  } else if (
    [
      'serviceAnalysis',
      'productAnalysis',
      'voiceManagement',
      'selfServiceOriginalSoundQuery',
      'hotDetailEvents',
      'rootCause'
    ].includes(routeName)
  ) {
    return TagType.Domain
  }
  // 默认返回 Domain
  return TagType.Domain
}

/**
 * 获取当前体验代码实际使用的标签体系，联动版优先使用表单已选值。
 * @returns 当前 tagType
 */
const getCurrentExperienceCodeTagType = (): string => {
  const experienceCodeField = filterConfig.value.find(field =>
    ['experienceCode', 'experienceCodeLinkage'].includes(field.type)
  )

  if (experienceCodeField?.type === 'experienceCodeLinkage') {
    const tagTypeProp = experienceCodeField.tagTypeProp
    const selectedTagType = tagTypeProp ? formData.value[tagTypeProp] : undefined
    return (
      selectedTagType ||
      experienceCodeField.fixedTagType ||
      experienceCodeField.tagTypeDefaultValue ||
      getTagLibTypeByRouteName()
    )
  }

  return getTagLibTypeByRouteName()
}

// 属性标签 相关方法
const getAttributeTagField = () => {
  return filterConfig.value.find(field => field.prop === 'scenarioAttr')
}
const setAttributeTagOptions = (options: any[]) => {
  attributeTagOptions.value = options

  const attributeTagField = getAttributeTagField()
  if (!attributeTagField) {
    return
  }

  attributeTagField.options = options
  nextTick()
}
// 初始化属性标签数据
const initAttributeTagOptions = async () => {
  try {
    const res = await findAllAttributeLabelList({})
    setAttributeTagOptions(Array.isArray(res.result) ? res.result : [])
  } catch (error) {
    console.error('获取属性标签选项失败:', error)
    setAttributeTagOptions([])
  }
}

// 获取品牌的值（用于传递给车系选择器）
const brandValue = computed(() => {
  const brandField = filterConfig.value.find(field => field.type === 'brand')
  if (brandField) {
    return formData.value[brandField.prop] ?? null
  }
  return null
})

/**
 * @description: 从 UniversaFilter 的原始 formData 初始化表单数据
 * @param {*} skipStandardViewpoint 是否跳过标准观点字段（用于先加载选项再设置值）
 * @return {*}
 */
const initFormDataFromUniversaFilter = (skipStandardViewpoint = false) => {
  // 从 store 中获取 UniversaFilter 的原始 formData
  const { formData: universaFormData, customTimes: universaCustomTimes } =
    queryStore.getUniversaFilterFormData()

  const config = filterConfig.value
  const newFormData: Record<string, any> = {}

  // 如果 UniversaFilter 的 formData 存在，直接使用
  if (universaFormData && Object.keys(universaFormData).length > 0) {
    // 遍历配置，从 UniversaFilter 的 formData 中获取值
    config.forEach(field => {
      // 跳过占位符
      if (field.type === 'placeholder') {
        return
      }

      // 如果skipStandardViewpoint为true，跳过标准观点字段
      if (skipStandardViewpoint && field.prop === 'topicCodes') {
        return
      }

      const value = universaFormData[field.prop]

      // 日期范围字段特殊处理
      if (field.type === 'daterange') {
        // 如果值存在，直接使用（可能是快捷选项的code或'custom'）
        if (value !== null && value !== undefined && value !== '') {
          newFormData[field.prop] = value
        }
        // 注意：customTimes 已经在函数外部单独处理了
        return
      }

      // 车系字段特殊处理：确保是数组格式
      if (field.type === 'series') {
        if (value !== null && value !== undefined) {
          newFormData[field.prop] = Array.isArray(value) ? value : [value]
        } else {
          // 如果值为 null 或 undefined，初始化为空数组，确保 SeriesSelector 能正确接收
          newFormData[field.prop] = []
        }
      }
      // 体验代码字段：始终同步，包括空数组
      else if (field.type === 'experienceCode' || field.type === 'experienceCodeLinkage') {
        newFormData[field.prop] = Array.isArray(value) ? value : []
        if (field.type === 'experienceCodeLinkage' && field.tagTypeProp) {
          newFormData[field.tagTypeProp] =
            universaFormData[field.tagTypeProp] ||
            field.fixedTagType ||
            field.tagTypeDefaultValue ||
            TagType.Domain
        }
      }
      // 按钮开关组多选模式：同步数组，包括空数组
      else if (field.type === 'btnSwitch' && field.multiple) {
        newFormData[field.prop] = Array.isArray(value) ? value : []
      }
      // 如果值不为空，设置到表单数据中
      else if (
        value !== null &&
        value !== undefined &&
        value !== '' &&
        !(Array.isArray(value) && value.length === 0)
      ) {
        newFormData[field.prop] = value
      }
    })

    // 设置自定义时间范围
    if (
      universaCustomTimes &&
      Array.isArray(universaCustomTimes) &&
      universaCustomTimes.length === 2
    ) {
      customTimes.value = [...universaCustomTimes]
    }
  } else {
    // 如果没有 UniversaFilter 的 formData，尝试从缓存中获取
    const cachedParams = queryStore.getUniversaFilterCacheSearchParams()

    if (cachedParams && Object.keys(cachedParams).length > 0) {
      config.forEach(field => {
        if (field.type === 'placeholder') {
          return
        }

        // 如果skipStandardViewpoint为true，跳过标准观点字段
        if (skipStandardViewpoint && field.prop === 'topicCodes') {
          return
        }

        // 处理日期范围
        if (field.type === 'daterange') {
          if (cachedParams.dateRange !== undefined) {
            const dateRangeValue = cachedParams.dateRange
            if (dateRangeValue === 'custom') {
              newFormData[field.prop] = 'custom'
              if (cachedParams.startDate && cachedParams.endDate) {
                customTimes.value = [cachedParams.startDate, cachedParams.endDate]
              }
            } else {
              newFormData[field.prop] = dateRangeValue
            }
          }
          return
        }

        const value = cachedParams[field.prop]

        // 车系字段特殊处理：确保是数组格式
        if (field.type === 'series') {
          if (value !== null && value !== undefined) {
            newFormData[field.prop] = Array.isArray(value) ? value : [value]
          } else {
            // 如果值为 null 或 undefined，初始化为空数组
            newFormData[field.prop] = []
          }
        }
        // 体验代码字段：始终同步，包括空数组（但缓存中可能没有这个字段，因为 UniversaFilter 不缓存它）
        else if (field.type === 'experienceCode' || field.type === 'experienceCodeLinkage') {
          if (field.type === 'experienceCodeLinkage' && field.tagTypeProp) {
            newFormData[field.tagTypeProp] =
              cachedParams[field.tagTypeProp] ||
              field.fixedTagType ||
              field.tagTypeDefaultValue ||
              TagType.Domain
          }
        }
        // 按钮开关组多选模式：同步数组，包括空数组
        else if (field.type === 'btnSwitch' && field.multiple) {
          newFormData[field.prop] = Array.isArray(value) ? value : []
        }
        // 其他字段的正常处理
        else if (
          value !== null &&
          value !== undefined &&
          value !== '' &&
          !(Array.isArray(value) && value.length === 0)
        ) {
          newFormData[field.prop] = value
        }
      })
    }
  }

  // 如果skipStandardViewpoint为false，直接设置所有值；如果为true，则合并到现有formData
  if (skipStandardViewpoint) {
    Object.assign(formData.value, newFormData)
  } else {
    formData.value = newFormData
  }
}

/**
 * @description: 初始化数据源选项
 * @return {*}
 */
const initChannelOptions = async () => {
  try {
    const res = await getUserChannelTree()
    channelOptions.value = res.result || []
  } catch (error) {
    console.error('获取数据源选项失败:', error)
    channelOptions.value = []
  }
}

// 初始化重点账号树选项
const initMainAccTreeOptions = async () => {
  try {
    const res = await getMainAccTreeData()
    mainAccOptions.value = res.result || []
    await nextTick()
  } catch (error) {
    console.error('获取重点账号树选项失败:', error)
    mainAccOptions.value = []
  }
}

/**
 * @description: 初始化标准观点选项
 * @param {*} tagParentCodes 客户体验代码的末级code数组
 * @return {*}
 */
const initStandardViewpointOptions = async (tagParentCodes?: string[]) => {
  try {
    const routeName = route.name as string
    const isRootCauseOrResultData = [
      'rootCause',
      'ResultData',
      'hotDetailEvents',
      'mainAccount',
      'newCarLaunch'
    ].includes(routeName)

    // 如果有tagParentCodes，传入codes参数
    if (tagParentCodes && tagParentCodes.length > 0) {
      const filteredCodes = tagParentCodes.filter(code => code !== 'all')
      const tagType = getCurrentExperienceCodeTagType()
      const res = await findFinalTagLibClientVoListByTagId({
        codes: filteredCodes,
        tagType
      })
      if (res.result && Array.isArray(res.result)) {
        standardViewpointOptions.value = res.result
      } else {
        standardViewpointOptions.value = []
      }
    } else {
      // 如果没有tagParentCodes，根据路由名称决定是否查询所有标准观点
      if (isRootCauseOrResultData) {
        // rootCause 和 ResultData 路由：查询所有标准观点
        const tagType = getCurrentExperienceCodeTagType()
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

/**
 * @description: 处理客户体验代码变化
 * @param {*} data 客户体验代码变化的数据
 * @return {*}
 */
const handleExperienceCodeChange = async (data: {
  lastLevelCodes: string[]
  lastLevelIds: string[]
  names: string[]
}) => {
  tydmObj.value = data
  // 清空标准观点的选中值（因为客户体验代码变化了）
  const standardViewpointField = filterConfig.value.find(field => field.prop === 'topicCodes')
  if (standardViewpointField) {
    formData.value[standardViewpointField.prop] = []
  }

  // 如果有末级code，调用接口获取标准观点
  if (data.lastLevelCodes && Array.isArray(data.lastLevelCodes) && data.lastLevelCodes.length > 0) {
    await initStandardViewpointOptions(data.lastLevelCodes)
  } else {
    // 如果没有末级code，清空标准观点选项和值（末级标签的查询逻辑不变，只有初始化时才查询所有标准观点）
    standardViewpointOptions.value = []
    if (standardViewpointField) {
      formData.value[standardViewpointField.prop] = []
    }
  }
}

/**
 * @description: 初始化竞品对比的品牌车系选项
 * @return {*}
 */
const initCompetitorBrandCarSeriesOptions = async () => {
  if (!isCompetitorAnalysis.value) return

  try {
    const res = await getAllBrandOrCarSeriesData({
      ...queryStore.currentQueryParams,
      queryType: competitorQueryType.value
    })
    if (res.success) {
      competitorBrandCarSeriesOptions.value = res.result || []
    } else {
      competitorBrandCarSeriesOptions.value = []
    }
  } catch (error) {
    console.error('获取品牌车系选项失败:', error)
    competitorBrandCarSeriesOptions.value = []
  }
}

/**
 * @description: 处理品牌车系分类变化
 * @return {*}
 */
const handleCompetitorQueryTypeChange = async () => {
  // 重置选中的代码和名称
  competitorFirstCode.value = undefined
  competitorSecondCode.value = undefined
  competitorFirstName.value = ''
  competitorSecondName.value = ''

  // 重新获取选项
  await initCompetitorBrandCarSeriesOptions()
}

/**
 * @description: 处理本品选择变化
 * @return {*}
 */
const handleCompetitorFirstChange = (code: string) => {
  const node = findNodeByField(competitorBrandCarSeriesOptions.value, code, 'code')
  competitorFirstName.value = node?.name || ''

  // 如果竞品与本品相同，清空竞品
  if (competitorSecondCode.value === code) {
    competitorSecondCode.value = undefined
    competitorSecondName.value = ''
  }
}

/**
 * @description: 处理竞品选择变化
 * @return {*}
 */
const handleCompetitorSecondChange = (code: string) => {
  const node = findNodeByField(competitorBrandCarSeriesOptions.value, code, 'code')
  competitorSecondName.value = node?.name || ''
}

/**
 * @description: 初始化竞品对比数据
 * @return {*}
 */
const initCompetitorAnalysisData = () => {
  if (!isCompetitorAnalysis.value) return

  // 从 store 中获取竞品对比数据
  const competitorData = queryStore.getCompetitorAnalysisData()
  if (competitorData && Object.keys(competitorData).length > 0) {
    competitorQueryType.value = competitorData.queryType || 'brand'
    competitorFirstCode.value = competitorData.firstSelectedCode
    competitorSecondCode.value = competitorData.secondSelectedCode
    competitorFirstName.value = competitorData.firstSelectedName || ''
    competitorSecondName.value = competitorData.secondSelectedName || ''
  } else {
    // 如果没有缓存数据，从 middlewareStore 获取当前值
    competitorQueryType.value = middlewareStore.brandServiceCategoryType
  }
}

/**
 * @description: 获取普通下拉选项，属性标签使用弹窗内异步加载的选项保证回显名称
 * @param {*} field 筛选字段配置
 * @return {*}
 */
const getSelectOptions = (field: any) => {
  if (field.prop === 'scenarioAttr') {
    return attributeTagOptions.value
  }

  return field.options || []
}

const findDrillDownConditionByKey = (conditions: DrillDownConditionItem[], key: string) => {
  return conditions.find(item => item?.key === key)
}

/**
 * 初始化下钻条件中的体验代码类型与竞品品牌车系树。
 * 竞品树用于发布弹窗的下拉选项和默认筛选条件回显。
 */
const initDrillDownConditionOptions = async () => {
  try {
    const res = await getDrillDownConditions()
    const conditions = Array.isArray(res.result) ? res.result : []
    const tagTypeItem = findDrillDownConditionByKey(conditions, 'tagType')
    const competitiveTree = findDrillDownConditionByKey(conditions, 'competitiveTree')
    const tagTypeDetails = Array.isArray(tagTypeItem?.details) ? tagTypeItem.details : []

    experienceCodeTypeOptions.value = tagTypeDetails
      .map((item: any) => ({ key: item?.key, value: item?.value }))
      .filter((item: any) => item.key && item.value)
    competitiveTreeOptions.value = Array.isArray(competitiveTree?.details)
      ? competitiveTree.details
      : []
  } catch (error) {
    console.error('获取下钻筛选条件失败:', error)
    experienceCodeTypeOptions.value = []
    competitiveTreeOptions.value = []
  }
}

/**
 * 加载发布弹窗中的用车场景选项。
 */
const initUsageScenarioOptions = async () => {
  const usageScenarioField = filterConfig.value.find(
    field => field.type === 'cascader' && field.prop === USAGE_SCENARIO_PROP
  )
  if (!usageScenarioField) {
    usageScenarioOptions.value = []
    return
  }

  try {
    const res = await getDataPlazaConditions()
    usageScenarioOptions.value = extractUsageScenarioOptions(res.result)
  } catch (error) {
    console.error('获取用车场景筛选条件失败:', error)
    usageScenarioOptions.value = []
  }
}

/**
 * @description: 弹窗打开
 * @return {*}
 */
const handleOpen = async () => {
  // 初始化数据源选项
  await initChannelOptions()
  await initMainAccTreeOptions()
  await initAttributeTagOptions()
  await initUsageScenarioOptions()

  // 先从 store 中获取 UniversaFilter 的原始 formData（不立即设置，先检查是否有客户体验代码）
  const { formData: universaFormData, customTimes: universaCustomTimes } =
    queryStore.getUniversaFilterFormData()

  // 先设置自定义时间范围
  if (
    universaCustomTimes &&
    Array.isArray(universaCustomTimes) &&
    universaCustomTimes.length === 2
  ) {
    customTimes.value = [...universaCustomTimes]
  }

  // 检查是否有客户体验代码，如果有则先加载标准观点选项
  const experienceCodeField = filterConfig.value.find(field =>
    ['experienceCode', 'experienceCodeLinkage'].includes(field.type)
  )
  const experienceCodeValue = universaFormData?.[experienceCodeField?.prop || '']

  // 初始化体验代码类型与竞品品牌车系树，再回填依赖该树的筛选条件。
  await initDrillDownConditionOptions()

  if (
    experienceCodeField &&
    experienceCodeValue &&
    Array.isArray(experienceCodeValue) &&
    experienceCodeValue.length > 0
  ) {
    // 先初始化formData（但不包含标准观点），让ExperienceCodeSelector组件能正确初始化
    initFormDataFromUniversaFilter(true)

    // 等待ExperienceCodeSelector组件初始化完成
    await nextTick()
    await nextTick()

    // 通过ref调用ExperienceCodeSelector的方法获取末级信息
    const selectorInstance = Array.isArray(experienceCodeSelectorRef.value)
      ? experienceCodeSelectorRef.value[0]
      : experienceCodeSelectorRef.value

    if (selectorInstance && typeof selectorInstance.getLastLevelInfo === 'function') {
      // 等待tagOptions加载完成，最多等待3秒
      let retryCount = 0
      const maxRetries = 30 // 最多重试30次，每次100ms
      while (retryCount < maxRetries) {
        const info = selectorInstance.getLastLevelInfo()
        if (
          info &&
          info.lastLevelCodes &&
          Array.isArray(info.lastLevelCodes) &&
          info.lastLevelCodes.length > 0
        ) {
          // 先加载标准观点选项
          await initStandardViewpointOptions(info.lastLevelCodes)
          break
        }
        // 如果还没有获取到末级信息，等待100ms后重试
        await new Promise(resolve => setTimeout(resolve, 100))
        retryCount++
      }
    }

    // 现在再设置标准观点的值（选项已经加载完成）
    const topicCodesValue = universaFormData?.topicCodes
    if (topicCodesValue !== null && topicCodesValue !== undefined && topicCodesValue !== '') {
      if (Array.isArray(topicCodesValue) && topicCodesValue.length > 0) {
        formData.value.topicCodes = topicCodesValue
      } else if (!Array.isArray(topicCodesValue)) {
        formData.value.topicCodes = [topicCodesValue]
      }
    }
  } else {
    // 完整初始化formData
    initFormDataFromUniversaFilter()
    // 如果没有客户体验代码，按当前标签体系初始化标准观点选项
    await initStandardViewpointOptions()
  }

  // 使用 nextTick 确保 formData 和 brandValue 都已正确设置后再继续
  await nextTick()

  // 如果是竞品对比页面，初始化竞品对比数据
  if (isCompetitorAnalysis.value) {
    initCompetitorAnalysisData()
    await initCompetitorBrandCarSeriesOptions()
  }

  getZoneOptions()
  fetchSeriesCondition()
}

/**
 * @description: 弹窗关闭
 * @return {*}
 */
const handleClose = () => {
  resetForm(ruleFormRef.value)
  usageScenarioOptions.value = []
  // 重置提交状态
  isSubmitting.value = false
  visible.value = false
}

/**
 * @description: 取消
 * @return {*}
 */
const handleCancel = () => {
  handleClose()
}

// 提交状态标志，防止重复提交
const isSubmitting = ref(false)

const submitForm = async (formEl: FormInstance | undefined) => {
  if (!formEl) return

  // 如果正在提交，直接返回
  if (isSubmitting.value) {
    return
  }

  await formEl.validate(async valid => {
    if (valid) {
      // 设置提交状态
      isSubmitting.value = true

      try {
        // 直接存储 formData 和 customTimes，使用更简单的数据结构
        const cpFormData = cloneDeep(formData.value)
        // 处理数据
        if (newCarSeriesSelector.value?.length) {
          cpFormData.newCarSeriesObjList = newCarSeriesSelector.value
        } else {
          // 新车上市页面数据处理
          const newCarSeriesObjList = queryStore.currentQueryParams.newCarSeriesObjList
          if (newCarSeriesObjList?.length) {
            cpFormData.newCarSeriesObjList = newCarSeriesObjList
          }
        }
        if (newCarCompareSeriesSelector.value?.length) {
          cpFormData.compCarSeriesObjList = newCarCompareSeriesSelector.value
        } else {
          const compCarSeriesObjList = queryStore.currentQueryParams.compCarSeriesObjList
          if (compCarSeriesObjList?.length) {
            cpFormData.compCarSeriesObjList = compCarSeriesObjList
          }
        }
        // 为了回显体验代码
        cpFormData.tydmObj = tydmObj.value
        if (route.name === 'hotDetailEvents') {
          cpFormData.keywords = tydmArrObj?.keywords
          // 存储当前的tab页面 是结果数据还是原始数据
          cpFormData.hotDetailPageType = middlewareStore.originalDataType
        }
        console.log('发布数据', cpFormData)

        const conditionData: any = {
          formData: cpFormData,
          customTimes:
            customTimes.value && customTimes.value.length === 2 ? [...customTimes.value] : []
        }

        // 如果是竞品对比页面，添加竞品对比数据
        if (isCompetitorAnalysis.value) {
          conditionData.competitorAnalysis = {
            queryType: competitorQueryType.value,
            firstSelectedCode: competitorFirstCode.value,
            secondSelectedCode: competitorSecondCode.value,
            firstSelectedName: competitorFirstName.value,
            secondSelectedName: competitorSecondName.value
          }
        }

        const params: PublishReportQueryParams = {
          reportName: ruleForm.reportName,
          dateCondition: undefined, // 不再使用 dateCondition
          defaultCondition: JSON.stringify(conditionData),
          reportUrl: route.path
        }

        if (ruleForm.zoneId?.length && ruleForm.zoneId?.length === 2) {
          params.firstLevelZoneId = ruleForm.zoneId[0]
          params.specialTypeId = ruleForm.zoneId[1]
        } else if (ruleForm.zoneId?.length && ruleForm.zoneId?.length === 1) {
          params.firstLevelZoneId = ruleForm.zoneId[0]
          params.specialTypeId = undefined
        }

        const response = await publishReport(params)
        if (response.success) {
          ElMessage.success('报告发布成功，可前往"场景分析"页面查看')
          handleClose()
        } else {
          ElMessage.error(response.message)
        }
      } catch (error: any) {
        ElMessage.error(error.message)
      } finally {
        // 无论成功或失败，都要重置提交状态
        isSubmitting.value = false
      }
    }
  })
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
// 新车上市 新品车系和对比车系级联选项数据处理
const handleCascaderChange = (value: any, field: any) => {
  // 新车上市 并且  新品车系处理以下逻辑
  if (field.type === 'cascader' && field.prop === 'newCarSeriesList') {
    const selectedOption = findOptionByKey(seriesConditionData.value.newCarSeries, value)
    if (selectedOption) {
      // 缓存数据
      newCarSeriesSelector.value = [selectedOption]
    }
  }
  // 新车上市 并且  对比车系
  if (field.prop === 'compCarSeriesList') {
    // 递归遍历newCarSeriesOptions找到属性key等于value的项 并返回这个对象
    const selectedOption = findOptionByKey(seriesConditionData.value.compareCarSeries, value)
    if (selectedOption) {
      // 缓存数据
      newCarCompareSeriesSelector.value = [selectedOption]
    }
  }
}

/**
 * @description: 确认按钮（使用防抖处理）
 * @return {*}
 */
const handleConfirm = debounce(() => {
  submitForm(ruleFormRef.value)
}, 300)

watch(
  () => visible.value,
  val => {
    if (val) {
      handleOpen()
    } else {
      handleClose()
    }
  }
)

watch(
  () => tydmArrObj,
  async (newVal: any) => {
    tydmObj.value = newVal?.tydmObj
  },
  { immediate: true, deep: true }
)

// 热点事件详情页面
const isHotDetailPage = computed(() => {
  return ['hotDetailEvents'].includes(route.name as string)
})
// 组件挂载时加载数据
onMounted(() => {
  // 根据id查询详情
  const routeName = route.name as string
  // 热点事件详情页面
  if (route?.query?.id && ['hotDetailEvents'].includes(routeName)) {
    getHotEvDetail({ id: route?.query?.id }).then((res: any) => {
      hotDetailData.value = res?.result
      const filterJsonStr = res?.result?.filterJson

      let filterJson: any = {}
      if (filterJsonStr) {
        try {
          filterJson = JSON.parse(filterJsonStr)
          ruleForm.keywords = filterJson?.keywords
          formData.value.customRangeTimes = filterJson?.customRangeTimes
          customTimes.value = filterJson?.customRangeTimes
          formData.value.experienceDCode = filterJson?.experienceDCode
          formData.value.tagTypeDProp = filterJson?.tagTypeDProp
        } catch (error) {
          //
        }
      }
    })
  }
})
const handleCusDateChange = (va1: any) => {
  customTimes.value = va1
}
</script>

<template>
  <Teleport to="body">
    <div v-if="visible" class="public-report" data-page-export-exclude>
      <div class="pr-wrap">
        <div class="pr-header">
          <div class="pr-title">发布报告</div>
          <div class="pr-close" @click="handleClose">
            <el-icon :size="24"><Close /></el-icon>
          </div>
        </div>
        <div class="pr-content">
          <el-form ref="ruleFormRef" :model="ruleForm" :rules="rules" label-width="auto">
            <el-form-item label="报告名称" prop="reportName">
              <el-input v-model="ruleForm.reportName" />
            </el-form-item>
            <el-form-item label="选择专区" prop="zoneId">
              <el-cascader
                v-model="ruleForm.zoneId"
                :options="zoneOptions"
                :props="{
                  value: 'id',
                  label: 'name',
                  children: 'children',
                  checkStrictly: true,
                  checkOnClickLeaf: false
                }"
                clearable
                class="w-full"
              />
            </el-form-item>
            <el-form-item v-if="isHotDetailPage" label="关键词" prop="keywords">
              <el-input
                v-model="ruleForm.keywords"
                placeholder="请输入关键词，最多 3 个，用空格隔开"
              />
            </el-form-item>
            <el-row :gutter="24">
              <template v-for="(field, index) in filterConfig" :key="field.prop || index">
                <!-- 跳过占位符 -->
                <template v-if="field.type !== 'placeholder'">
                  <!-- 热点事件详情页面的时间 -->
                  <el-col v-if="isHotDetailPage && field.prop === 'customRangeTimes'" :span="24">
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
                  <el-col v-else-if="field.type === 'daterange'" :span="24">
                    <el-form-item :label="field.label">
                      <DatePicker
                        v-model="formData[field.prop]"
                        v-model:custom-times="customTimes"
                      />
                    </el-form-item>
                  </el-col>
                  <!-- 根因分析竞品车系与查询条件保持相同的扁平多选组件 -->
                  <el-col
                    v-else-if="
                      isRootCause &&
                      field.type === 'selectv2' &&
                      field.prop === COMPETITIVE_SERIES_PROP
                    "
                    :span="24"
                  >
                    <el-form-item :label="field.label">
                      <SelectV2WithSelectAll
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
                        class="w-full"
                      />
                    </el-form-item>
                  </el-col>
                  <!-- 新品车系-->
                  <el-col v-else-if="field.prop === 'newCarSeriesList'" :span="24">
                    <el-form-item :label="field.label">
                      <el-cascader
                        v-model="formData[field.prop]"
                        :options="seriesConditionData.newCarSeries"
                        :props="{
                          value: 'code',
                          label: 'name',
                          children: 'cars',
                          multiple: false,
                          checkStrictly: false,
                          emitPath: false,
                          checkOnClickLeaf: false
                        }"
                        clearable
                        class="w-full"
                        @change="value => handleCascaderChange(value, field)"
                      />
                    </el-form-item>
                  </el-col>

                  <!-- 对比车系-->
                  <el-col v-else-if="field.prop === 'compCarSeriesList'" :span="24">
                    <el-form-item :label="field.label">
                      <el-cascader
                        v-model="formData[field.prop]"
                        :options="seriesConditionData.compareCarSeries"
                        :props="{
                          value: 'code',
                          label: 'name',
                          children: 'cars',
                          multiple: false,
                          checkStrictly: false,
                          emitPath: false,
                          checkOnClickLeaf: false
                        }"
                        clearable
                        class="w-full"
                        @change="value => handleCascaderChange(value, field)"
                      />
                    </el-form-item>
                  </el-col>

                  <!-- 重点账号 -->
                  <el-col v-else-if="field.prop === 'keyAccounts'" :span="24">
                    <el-form-item :label="field.label" :prop="field.prop">
                      <el-cascader
                        v-model="formData[field.prop]"
                        :options="mainAccOptions"
                        collapse-tags
                        :max-collapse-tags="1"
                        :show-all-levels="false"
                        filterable
                        :key="field.prop"
                        :props="field.cascaderProps"
                        :teleported="true"
                        style="width: 100%"
                      />
                    </el-form-item>
                  </el-col>

                  <!-- 通用级联选择（包括用车场景） -->
                  <el-col v-else-if="field.type === 'cascader'" :span="24">
                    <el-form-item :label="field.label" :prop="field.prop">
                      <el-cascader
                        v-model="formData[field.prop]"
                        :options="getCascaderOptions(field)"
                        :props="field.cascaderProps"
                        :disabled="field.disabled"
                        :clearable="field.clearable"
                        :placeholder="field.placeholder || '请选择'"
                        :max-collapse-tags="1"
                        :show-all-levels="false"
                        collapse-tags
                        filterable
                        class="w-full"
                      />
                    </el-form-item>
                  </el-col>

                  <!-- 数据源 -->
                  <el-col v-else-if="field.type === 'dataSource'" :span="24">
                    <el-form-item :label="field.label">
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
                  <el-col v-else-if="field.type === 'brand'" :span="24">
                    <el-form-item :label="field.label">
                      <BrandSelector
                        v-model="formData[field.prop]"
                        :options="field.options || []"
                        :props="field.props"
                        :multiple="field.multiple"
                      />
                    </el-form-item>
                  </el-col>
                  <!-- 车系 -->
                  <el-col v-else-if="field.type === 'series'" :span="24">
                    <el-form-item :label="field.label">
                      <SeriesSelector
                        v-model="formData[field.prop]"
                        :brand-value="brandValue"
                        :options="field.options || []"
                        :props="field.props"
                      />
                    </el-form-item>
                  </el-col>
                  <!-- 热点事件的体验代码 -->
                  <el-col
                    v-else-if="isHotDetailPage && field.type === 'experienceCodeLinkage'"
                    :span="24"
                  >
                    <el-form-item :label="field.label">
                      <ExperienceCodeHotSelector
                        ref="experienceCodeSelectorRef"
                        v-model="formData.experienceDCode"
                        v-model:tagType="formData.tagTypeDProp"
                        :type-options="experienceCodeTypeOptions"
                        :default-tag-type="'CA'"
                        @change="handleExperienceCodeChange"
                      />
                    </el-form-item>
                  </el-col>
                  <!-- 体验代码 -->
                  <el-col v-else-if="field.type === 'experienceCode'" :span="24">
                    <el-form-item :label="field.label">
                      <ExperienceCodeSelector
                        ref="experienceCodeSelectorRef"
                        v-model="formData[field.prop]"
                        :disabled="field.disabled"
                        :page-name="route.name as string"
                        @change="handleExperienceCodeChange"
                      />
                    </el-form-item>
                  </el-col>
                  <!-- 新版级联体验代码 -->
                  <el-col v-else-if="field.type === 'experienceCodeLinkage'" :span="24">
                    <el-form-item :label="field.label" :prop="field.prop">
                      <ExperienceCodeLinkageSelector
                        ref="experienceCodeSelectorRef"
                        v-model="formData[field.prop]"
                        v-model:tagType="formData[field.tagTypeProp || 'tagType']"
                        :type-options="experienceCodeTypeOptions"
                        :default-tag-type="field.tagTypeDefaultValue"
                        :fixed-tag-type="field.fixedTagType"
                        :hide-tag-type="!!field.hideTagType"
                        :root-tag-name="field.rootTagName"
                        :request-level="field.requestLevel"
                        :hide-root-in-cascader="!!field.hideRootInCascader"
                        :disabled="field.disabled"
                        :ty-page-type="field.tyPageType"
                        :same-level-only="shouldLimitExperienceCodeSameLevel"
                        @change="handleExperienceCodeChange"
                      />
                    </el-form-item>
                  </el-col>
                  <!-- 下拉选择 v2（用于大量数据） -->
                  <el-col v-else-if="field.type === 'selectv2'" :span="24">
                    <el-form-item :label="field.label">
                      <SelectV2WithSelectAll
                        v-if="field.showSelectAll && field.prop === 'topicCodes'"
                        v-model="formData[field.prop]"
                        :options="standardViewpointOptions"
                        :props="field.props"
                        :clearable="field.clearable"
                        :placeholder="field.placeholder"
                        :multiple="field.multiple"
                        :max-collapse-tags="1"
                        :collapse-tags="true"
                        :filterable="true"
                        :show-select-all="field.showSelectAll"
                        value-key="tagCode"
                        class="w-full"
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
                        collapse-tags
                        filterable
                        :disabled="isSelectV2Disabled(field)"
                        class="w-full"
                      />
                    </el-form-item>
                  </el-col>
                  <!-- 下拉选择 -->
                  <el-col v-else-if="field.type === 'select'" :span="24">
                    <el-form-item :label="field.label">
                      <el-select
                        v-model="formData[field.prop]"
                        :options="getSelectOptions(field)"
                        :props="field.props"
                        :clearable="field.clearable"
                        :placeholder="field.placeholder"
                        :multiple="field.multiple"
                        :max-collapse-tags="1"
                        :collapseTags="true"
                        class="w-full"
                      />
                    </el-form-item>
                  </el-col>
                  <!-- 输入框 -->
                  <el-col v-else-if="field.type === 'input'" :span="24">
                    <el-form-item :label="field.label">
                      <el-input
                        v-model="formData[field.prop]"
                        :placeholder="field.placeholder"
                        clearable
                        :maxLength="field.maxLength"
                        class="w-full"
                      />
                    </el-form-item>
                  </el-col>
                </template>
              </template>
            </el-row>
            <!-- 竞品对比相关字段 -->
            <template v-if="isCompetitorAnalysis">
              <el-divider />
              <div class="title-info mb-16">竞品对比设置</div>
              <el-row :gutter="24">
                <el-col :span="24">
                  <el-form-item label="品牌车系分类">
                    <SwitchButton
                      v-model="competitorQueryType"
                      :options="BrandServiceCategoryOptions"
                      @change="handleCompetitorQueryTypeChange"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="本品">
                    <el-select-v2
                      v-model="competitorFirstCode"
                      :options="competitorBrandCarSeriesOptions"
                      :props="{ value: 'code', label: 'name' }"
                      filterable
                      placeholder="请选择本品"
                      class="w-full"
                      @change="handleCompetitorFirstChange"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="竞品">
                    <el-select-v2
                      v-model="competitorSecondCode"
                      :options="competitorBrandCarSeriesOptions"
                      :props="{ value: 'code', label: 'name' }"
                      filterable
                      placeholder="请选择竞品"
                      class="w-full"
                      :disabled="!competitorFirstCode"
                      @change="handleCompetitorSecondChange"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
            </template>
          </el-form>
        </div>
        <div class="pr-footer">
          <div class="cancel cursor-point" @click="handleCancel()">取消</div>
          <div
            class="confirm cursor-point"
            :class="{ 'is-disabled': isSubmitting }"
            @click="handleConfirm()"
          >
            {{ isSubmitting ? '发布中...' : '发布报告' }}
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style lang="scss" scoped>
.title-info {
  font-weight: 500;
  font-size: 16px;
  color: #1d2129;
  line-height: 24px;
}

// 重置 el-form-item 对 SwitchButton 的影响，确保高度一致
:deep(.el-form-item) {
  .el-form-item__content {
    line-height: normal;
  }
}

.public-report {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  width: 100%;
  height: 100%;
  background-color: #00000080;
  z-index: 200;
  display: flex;
  justify-content: center;
  align-items: center;

  .pr-wrap {
    width: 680px;
    height: 576px;
    border-radius: 12px;
    background-color: #fff;
    display: flex;
    flex-direction: column;
    .pr-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      height: 64px;
      padding: 0 24px;

      font-weight: 600;
      font-size: 20px;
      color: #1f2733;
      line-height: 32px;
    }

    .pr-content {
      flex: 1;
      min-height: 0;
      min-width: 0;
      padding: 0 40px;
      overflow: auto;
    }

    .pr-footer {
      height: 72px;
      display: flex;
      gap: 24px;
      padding: 16px 40px 0;
      border-top: 1px solid #ebedf0;
      .cancel {
        flex: 1;
        height: 32px;
        background: #f2f3f5;
        border-radius: 2px 2px 2px 2px;
        font-weight: 400;
        font-size: 14px;
        color: #4e5969;
        line-height: 32px;
        text-align: center;
      }

      .confirm {
        flex: 1;
        height: 32px;
        background: #165dff;
        border-radius: 2px 2px 2px 2px;
        font-weight: 400;
        font-size: 14px;
        color: #ffffff;
        line-height: 32px;
        text-align: center;

        &.is-disabled {
          background: #94bfff;
          cursor: not-allowed;
          pointer-events: none;
        }
      }
    }
  }
}
</style>
