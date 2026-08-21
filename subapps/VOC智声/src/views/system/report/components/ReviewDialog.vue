<script setup lang="ts">
import { reactive, ref, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { reviewReport } from '@/api/sceneAnalysis'
import { useLoading } from '@/hooks/useLoading'
import { getFilterConfig } from '@/components/Business/UniversaFilter/helper'
import {
  getUserChannelTree,
  findFinalTagLibClientVoListByTagId,
  findAllAttributeLabelList
} from '@/api/common'
import { TagType } from '@/constants'
import BrandSelector from '@/components/Business/UniversaFilter/components/BrandSelector.vue'
import SeriesSelector from '@/components/Business/UniversaFilter/components/SeriesSelector.vue'
import ExperienceCodeSelector from '@/components/Business/UniversaFilter/components/ExperienceCodeSelector.vue'
import ExperienceCodeLinkageSelector from '@/components/Business/UniversaFilter/components/ExperienceCodeLinkageSelector.vue'
import SelectV2WithSelectAll from '@/components/Business/UniversaFilter/components/SelectV2WithSelectAll.vue'
import DataSourceCascader from '@/components/Business/AdvancedFilter/DataSourceCascader.vue'
import DatePicker from '@/components/Business/UniversaFilter/components/DatePicker.vue'
import { getSpecialZoneOptions } from '@/api/overview'
import { BrandServiceCategoryOptions } from '@/components/Business/Scene/CompetitorAnalysis/constants'
import { getAllBrandOrCarSeriesData } from '@/api/competitorAnalysis'
import type { brandCarSeriesItem } from '@/api/competitorAnalysis/types'
import SwitchButton from '@/components/UI/SwitchButton/index.vue'
import BtnSwitch from '@/components/UI/BtnSwitch/index.vue'
import { getDrillDownConditions, type DrillDownConditionItem } from '@/api/drillDownDialog'
import { getDataPlazaConditions } from '@/api/dataPlaza'
import type { DataPlazaConditionOption } from '@/api/dataPlaza/types'
import {
  extractUsageScenarioOptions,
  USAGE_SCENARIO_PROP
} from '@/components/Business/UniversaFilter/usageScenario'

defineOptions({
  name: 'ReviewDialog'
})

const emits = defineEmits<{
  (e: 'confirm'): void
}>()

const visible = defineModel<boolean>('visible', { default: false })
const { data } = defineProps<{ data?: any }>()

const { showLoading, hideLoading } = useLoading()

const ruleForm = reactive({
  reportName: '',
  zoneId: [] as string[]
})

const zoneOptions = ref<any[]>([])
const formData = ref<Record<string, any>>({})
const customTimes = ref<string[]>([])
const channelOptions = ref<any[]>([])
const standardViewpointOptions = ref<any[]>([]) // 标准观点选项
const experienceCodeSelectorRef = ref<any>(null) // 客户体验代码选择器引用
const experienceCodeTypeOptions = ref<Array<{ key: string; value: string }>>([])
const attributeTagOptions = ref<any[]>([]) // 属性标签选项
const competitiveTreeOptions = ref<any[]>([]) // 竞品品牌车系树
const usageScenarioOptions = ref<DataPlazaConditionOption[]>([]) // 用车场景树

const COMPETITIVE_BRAND_PROP = 'compBrandCodeList'
const COMPETITIVE_SERIES_PROP = 'compCarSeriesList'

// 竞品对比相关数据
const isCompetitorAnalysis = computed(() => routeName.value === 'competitorAnalysis')
const competitorQueryType = ref<string>('brand')
const competitorFirstCode = ref<string | undefined>()
const competitorSecondCode = ref<string | undefined>()
const competitorFirstName = ref<string>('')
const competitorSecondName = ref<string>('')
const competitorBrandCarSeriesOptions = ref<brandCarSeriesItem[]>([])

// 根据报告 URL 获取路由名称
const routeName = computed(() => {
  if (data?.reportUrl) {
    // 从 reportUrl 中提取路由名称，例如 /scene/groupAnalysis -> groupAnalysis
    const pathParts = data.reportUrl.split('/').filter(Boolean)
    return pathParts[pathParts.length - 1] || ''
  }
  return ''
})

// 根据路由名称获取 UniversaFilter 的配置
const filterConfig = computed(() => {
  if (routeName.value) {
    return getFilterConfig(routeName.value)
  }
  return []
})

/**
 * 将单选或多选字段统一转换为有效值数组。
 * @param value 审核弹窗中的字段值
 * @returns 去除空值后的字段值数组
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
 * 校验已保存的编码是否与竞品选项匹配，兼容 key、value 与 code 三种格式。
 * @param option 竞品品牌或车系选项
 * @param value 已保存的字段值
 * @param valueKey 当前字段的取值键
 * @returns 是否匹配
 */
const isMatchedCompetitiveValue = (option: any, value: unknown, valueKey: string) => {
  return [option?.[valueKey], option?.key, option?.value, option?.code].some(
    candidate =>
      candidate !== undefined && candidate !== null && String(candidate) === String(value)
  )
}

/**
 * 获取审核报告当前页面的竞品筛选字段配置。
 * @param prop 字段标识
 * @returns 对应的筛选字段配置
 */
const getCompetitiveField = (prop: string) => {
  return filterConfig.value.find(field => field.type === 'selectv2' && field.prop === prop)
}

const competitiveBrandValue = computed(() => {
  const brandField = getCompetitiveField(COMPETITIVE_BRAND_PROP)
  return brandField ? (formData.value[brandField.prop] ?? brandField.defaultValue ?? []) : []
})

/**
 * 按审核报告已保存的竞品品牌聚合对应车系选项。
 */
const competitiveSeriesOptions = computed(() => {
  const brandField = getCompetitiveField(COMPETITIVE_BRAND_PROP)
  const seriesField = getCompetitiveField(COMPETITIVE_SERIES_PROP)
  if (!brandField || !seriesField || competitiveTreeOptions.value.length === 0) {
    return []
  }

  const selectedBrands = normalizeFieldValues(competitiveBrandValue.value)
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
 * 获取审核弹窗下拉字段的动态选项，确保已保存编码可解析为中文名称。
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
 * 获取审核弹窗级联字段的动态选项。
 * @param field 筛选字段配置
 * @returns 对应的级联选项
 */
const getCascaderOptions = (field: any) => {
  return field.prop === USAGE_SCENARIO_PROP ? usageScenarioOptions.value : field.options || []
}

const getSelectOptions = (field: any) => {
  return field.prop === 'scenarioAttr' ? attributeTagOptions.value : field.options || []
}

/**
 * 转换按钮开关组选项为 BtnSwitch 所需的 label/value 结构。
 * @param field 审核报告中的筛选字段配置
 * @returns 可展示的按钮组选项
 */
const getBtnSwitchOptions = (field: any) => {
  return (
    field.options?.map((option: any) => ({
      label: option[field.props?.label || 'text'] || option.label || option.value,
      value: option[field.props?.value || 'value'] || option.value
    })) || []
  )
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

/**
 * 加载属性标签选项，使审核弹窗能够将已保存的标签 ID 展示为中文名称。
 */
const initAttributeTagOptions = async () => {
  try {
    const res = await findAllAttributeLabelList({})
    attributeTagOptions.value = Array.isArray(res.result) ? res.result : []
  } catch (error) {
    console.error('获取属性标签选项失败:', error)
    attributeTagOptions.value = []
  }
}

const findDrillDownConditionByKey = (conditions: DrillDownConditionItem[], key: string) => {
  return conditions.find(item => item?.key === key)
}

/**
 * 加载下钻筛选条件，使审核弹窗能够回显标签体系和竞品编码。
 */
const initCompetitiveTreeOptions = async () => {
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
 * 加载审核弹窗中的用车场景选项。
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
 * 根据路由名称获取客户体验代码的 tagLibType
 * @returns TagType 值
 */
const getTagLibTypeByRouteName = (): string => {
  const routeNameValue = routeName.value
  if (['journeyAnalysis'].includes(routeNameValue)) {
    return TagType.UserJourney
  } else if (
    [
      'serviceAnalysis',
      'productAnalysis',
      'voiceManagement',
      'selfServiceOriginalSoundQuery',
      'rootCause'
    ].includes(routeNameValue)
  ) {
    return TagType.Domain
  }
  // 默认返回 Domain
  return TagType.Domain
}

/**
 * 获取审核报告保存的体验代码标签体系。
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

/**
 * @description: 初始化标准观点选项
 * @param {*} tagParentCodes 客户体验代码的末级code数组
 * @return {*}
 */
const initStandardViewpointOptions = async (tagParentCodes?: string[]) => {
  try {
    const routeNameValue = routeName.value
    const isRootCauseOrResultData = ['rootCause', 'ResultData'].includes(routeNameValue)

    // 如果有tagParentCodes，传入codes参数
    if (tagParentCodes && tagParentCodes.length > 0) {
      const tagType = getCurrentExperienceCodeTagType()
      const res = await findFinalTagLibClientVoListByTagId({
        codes: tagParentCodes,
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
 * @description: 从 defaultCondition 初始化表单数据
 * @return {*}
 */
const initFormDataFromDefaultCondition = () => {
  if (!data?.defaultCondition) {
    formData.value = {}
    customTimes.value = []
    return
  }

  try {
    const conditionData = JSON.parse(data.defaultCondition)

    // 新格式：直接包含 formData 和 customTimes
    if (conditionData.formData) {
      formData.value = { ...conditionData.formData }
      if (
        conditionData.customTimes &&
        Array.isArray(conditionData.customTimes) &&
        conditionData.customTimes.length === 2
      ) {
        customTimes.value = [...conditionData.customTimes]
      } else {
        customTimes.value = []
      }

      // 如果是竞品对比页面，解析竞品对比数据
      if (isCompetitorAnalysis.value && conditionData.competitorAnalysis) {
        const competitorData = conditionData.competitorAnalysis
        competitorQueryType.value = competitorData.queryType || 'brand'
        competitorFirstCode.value = competitorData.firstSelectedCode
        competitorSecondCode.value = competitorData.secondSelectedCode
        competitorFirstName.value = competitorData.firstSelectedName || ''
        competitorSecondName.value = competitorData.secondSelectedName || ''
      }

      console.log('审核弹窗初始化后的 formData:', formData.value)
      console.log('审核弹窗初始化后的 customTimes:', customTimes.value)
      return
    }

    // 兼容旧格式：filterItems 数组格式
    if (Array.isArray(conditionData)) {
      const filterItems = conditionData
      const config = filterConfig.value
      const newFormData: Record<string, any> = {}

      // 遍历 filterItems，将数据还原到 formData
      filterItems.forEach((item: any) => {
        // 处理日期范围（filterType === '93'）
        if (item.filterType === '93') {
          const dateField = config.find(field => field.type === 'daterange')
          if (dateField) {
            // 如果 ext 中有 startDate 和 endDate，说明是自定义时间
            if (item.ext?.startDate && item.ext?.endDate) {
              newFormData[dateField.prop] = 'custom'
              customTimes.value = [item.ext.startDate, item.ext.endDate]
            } else {
              // 否则是快捷选项
              newFormData[dateField.prop] = item.value
            }
          }
        } else {
          // 其他字段，根据 name 找到对应的配置
          const field = config.find(f => f.label === item.name)
          if (field) {
            // 根据 filterType 还原值
            if (item.filterType === '91' && Array.isArray(item.value)) {
              // 品牌：多选页面保留数组，单选页面兼容旧逻辑取首项
              newFormData[field.prop] = field.multiple ? item.value : item.value[0]
            } else if (item.filterType === '92' && Array.isArray(item.value)) {
              // 体验代码：直接使用数组
              newFormData[field.prop] = item.value
            } else if (item.filterType === '95' && Array.isArray(item.value)) {
              // 数据源：直接使用数组
              newFormData[field.prop] = item.value
            } else if (item.filterType === '1' && Array.isArray(item.value)) {
              // 下拉选择和按钮开关组：多选字段保留完整数组。
              newFormData[field.prop] = field.multiple ? item.value : item.value[0]
            } else if (item.filterType === '2') {
              // 输入框：直接使用值
              newFormData[field.prop] = item.value
            }
          }
        }
      })

      formData.value = newFormData
      console.log('审核弹窗初始化后的 formData (旧格式):', formData.value)
      console.log('审核弹窗初始化后的 customTimes (旧格式):', customTimes.value)
    }
  } catch (error) {
    console.error('解析 defaultCondition 失败:', error)
    formData.value = {}
    customTimes.value = []
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

const handleOpen = async () => {
  if (!data) return

  ruleForm.reportName = data.reportName || ''

  const zoneIds: string[] = []
  if (data.class1) zoneIds.push(data.class1)
  if (data.class2) zoneIds.push(data.class2)
  ruleForm.zoneId = zoneIds

  // 先加载动态选项，再回填已保存的编码，保证禁用下拉可直接显示中文名称。
  await Promise.all([
    initChannelOptions(),
    initAttributeTagOptions(),
    initCompetitiveTreeOptions(),
    initUsageScenarioOptions()
  ])

  // 从 defaultCondition 初始化表单数据
  initFormDataFromDefaultCondition()

  // 使用 nextTick 确保 formData 已正确设置
  await nextTick()

  // 检查是否有客户体验代码的默认值，如果有则触发联动加载标准观点
  const experienceCodeField = filterConfig.value.find(field =>
    ['experienceCode', 'experienceCodeLinkage'].includes(field.type)
  )
  if (experienceCodeField && formData.value[experienceCodeField.prop]) {
    const experienceCodeValue = formData.value[experienceCodeField.prop]
    if (Array.isArray(experienceCodeValue) && experienceCodeValue.length > 0) {
      // 等待ExperienceCodeSelector组件初始化完成后再触发
      // 需要多次nextTick确保组件完全渲染和tagOptions加载完成
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
            await initStandardViewpointOptions(info.lastLevelCodes)
            break
          }
          // 如果还没有获取到末级信息，等待100ms后重试
          await new Promise(resolve => setTimeout(resolve, 100))
          retryCount++
        }
      }
    }
  } else {
    // 如果没有客户体验代码，初始化空的标准观点选项
    await initStandardViewpointOptions()
  }

  // 如果是竞品对比页面，初始化竞品对比选项
  if (isCompetitorAnalysis.value) {
    await initCompetitorBrandCarSeriesOptions()
  }

  // 获取专区选项
  try {
    const res = await getSpecialZoneOptions({})
    if (res.success) {
      zoneOptions.value = res.result
    } else {
      zoneOptions.value = []
    }
  } catch (error: any) {
    console.error('获取专区选项失败:', error)
    zoneOptions.value = []
  }
}

const handleClose = () => {
  ruleForm.reportName = ''
  ruleForm.zoneId = []
  formData.value = {}
  customTimes.value = []
  zoneOptions.value = []
  channelOptions.value = []
  standardViewpointOptions.value = []
  experienceCodeTypeOptions.value = []
  attributeTagOptions.value = []
  competitiveTreeOptions.value = []
  usageScenarioOptions.value = []
}

const loading = ref(false)

// 审核操作
const handleReview = async (status: string) => {
  if (!data?.id || loading.value) return

  loading.value = true
  showLoading({ text: '审核中...' })
  try {
    const res = await reviewReport({ id: data.id, status })
    if (res.success) {
      ElMessage.success('审核成功')
      emits('confirm')
      visible.value = false
    } else {
      ElMessage.error(res.message || '审核失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '审核失败')
  } finally {
    hideLoading()
    loading.value = false
  }
}

const handlePass = () => handleReview('1')

const handleReject = () => handleReview('3')
</script>

<template>
  <FDialog
    v-model:visible="visible"
    width="800px"
    @open="handleOpen"
    @close="handleClose"
    :show-footer="false"
  >
    <template #header>
      <span>报告审核</span>
    </template>
    <div class="content">
      <el-form :model="ruleForm" label-width="auto">
        <div class="title-info mb-16">基本信息</div>
        <el-form-item label="报告名称">
          <el-input v-model="ruleForm.reportName" disabled />
        </el-form-item>
        <el-form-item label="选择专区">
          <el-cascader
            v-model="ruleForm.zoneId"
            :options="zoneOptions"
            :props="{ value: 'id', label: 'name', children: 'children', checkStrictly: true }"
            disabled
            class="w-full"
          />
        </el-form-item>
        <el-divider />
        <div class="title-info mb-16">数据范围</div>
        <el-row :gutter="24">
          <template v-for="(field, index) in filterConfig" :key="field.prop || index">
            <!-- 跳过占位符 -->
            <template v-if="field.type !== 'placeholder'">
              <!-- 日期范围 -->
              <el-col v-if="field.type === 'daterange'" :span="24">
                <el-form-item :label="field.label">
                  <DatePicker
                    v-model="formData[field.prop]"
                    v-model:custom-times="customTimes"
                    disabled
                  />
                </el-form-item>
              </el-col>
              <!-- 通用级联选择（包括用车场景） -->
              <el-col v-else-if="field.type === 'cascader'" :span="24">
                <el-form-item :label="field.label">
                  <el-cascader
                    v-model="formData[field.prop]"
                    :options="getCascaderOptions(field)"
                    :props="field.cascaderProps"
                    :clearable="field.clearable"
                    :placeholder="field.placeholder || '请选择'"
                    :max-collapse-tags="1"
                    :show-all-levels="false"
                    collapse-tags
                    filterable
                    class="w-full"
                    disabled
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
                    disabled
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
                    disabled
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
                    :expand-when-non-core-selected="true"
                    disabled
                  />
                </el-form-item>
              </el-col>
              <!-- 体验代码 -->
              <el-col v-else-if="field.type === 'experienceCode'" :span="24">
                <el-form-item :label="field.label">
                  <ExperienceCodeSelector
                    ref="experienceCodeSelectorRef"
                    v-model="formData[field.prop]"
                    :disabled="true"
                    :page-name="routeName"
                  />
                </el-form-item>
              </el-col>
              <!-- 新版级联体验代码 -->
              <el-col v-else-if="field.type === 'experienceCodeLinkage'" :span="24">
                <el-form-item :label="field.label">
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
                    :ty-page-type="field.tyPageType"
                    disabled
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
                    disabled
                  />
                  <el-select-v2
                    v-else
                    v-model="formData[field.prop]"
                    :options="getSelectV2Options(field)"
                    :props="field.props"
                    :clearable="field.clearable"
                    :placeholder="field.placeholder"
                    :multiple="field.multiple"
                    :max-collapse-tags="1"
                    collapse-tags
                    filterable
                    class="w-full"
                    disabled
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
                    disabled
                  />
                </el-form-item>
              </el-col>
              <!-- 按钮开关组（根因分析的情感、意图） -->
              <el-col v-else-if="field.type === 'btnSwitch'" :span="24">
                <el-form-item :label="field.label">
                  <BtnSwitch
                    v-model="formData[field.prop]"
                    :options="getBtnSwitchOptions(field)"
                    :multiple="field.multiple ?? false"
                    disabled
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
                    disabled
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
                  disabled
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
                  disabled
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
                  disabled
                />
              </el-form-item>
            </el-col>
          </el-row>
        </template>
      </el-form>
    </div>
    <template #footer>
      <el-button @click.stop="handleReject">拒绝</el-button>
      <el-button type="primary" @click.stop="handlePass">通过</el-button>
    </template>
  </FDialog>
</template>

<style lang="scss" scoped>
.content {
  max-height: 50vh;
  overflow-y: auto;
  overflow-x: hidden;
  width: 100%;
  box-sizing: border-box;
  padding: 0;
}
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

// 确保 el-row 的 gutter 不会导致水平滚动
:deep(.el-row) {
  box-sizing: border-box;
}

// 确保 el-col 不会超出容器
:deep(.el-col) {
  box-sizing: border-box;
}

// 确保表单内容不会超出容器
:deep(.el-form) {
  width: 100%;
  box-sizing: border-box;
  overflow-x: hidden;
}

// 确保所有输入组件不会超出容器
:deep(.el-input),
:deep(.el-select),
:deep(.el-select-v2),
:deep(.el-cascader),
:deep(.w-full) {
  max-width: 100%;
  box-sizing: border-box;
}
</style>
