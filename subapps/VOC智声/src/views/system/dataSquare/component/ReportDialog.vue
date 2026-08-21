<script setup lang="ts">
import { computed, nextTick, reactive, ref, watch } from 'vue'
import { ElMessage, type CascaderOption, type FormInstance, type FormRules } from 'element-plus'
import { findFinalTagLibClientVoListByTagId } from '@/api/common'
import { insertDataPlazaReport, updateDataPlazaReport } from '@/api/dataPlaza/index'
import type {
  DataPlazaConditionOption,
  DataPlazaReportDateCondition,
  DataPlazaReportDefaultCondition,
  DataPlazaReportItem,
  DataPlazaReportSaveParams
} from '@/api/dataPlaza/types'
import { FE_TIME_DIMENSION_OPTIONS } from '@/constants'
import { useUserStore } from '@/store'
import { getTimeDimensionByCode } from '@/utils/date'
import AppDialog from '@/components/AppDialog.vue'
import BtnSwitchWithAll from '@/components/UI/BtnSwitchWithAll/index.vue'
import SelectV2WithSelectAll from '@/components/Business/UniversaFilter/components/SelectV2WithSelectAll.vue'
import DataSourceCascader from '@/components/Business/AdvancedFilter/DataSourceCascader.vue'
import DataSquareExperienceCodeSelector from './DataSquareExperienceCodeSelector.vue'
import {
  createEmptyExperienceCodeValue,
  getExperienceLastLevelCodes,
  normalizeExperienceCodeValue,
  type ExperienceCodeValue
} from './experienceCode'
import { dataSquareActions, dataSquareStore } from '../store'

defineOptions({
  name: 'ReportDialog'
})

type ReportDialogMode = 'create' | 'edit'
type TagTypeOption = Pick<DataPlazaConditionOption, 'key' | 'value'>
type StandardViewpointOption = {
  tagCode?: string
  tagName?: string
}
type DataPlazaConditionMap = Record<string, DataPlazaConditionOption[]>

const CUSTOM_DATE_RANGE = 'custom'
const DEFAULT_DYNAMIC_DATE_RANGE = '2'
const DYNAMIC_DATE_RANGE_CODES = ['2', '3'] as const
const BRAND_SERIES_CONDITION_KEY = 'product_competitors_brand_car_series'

interface ReportDialogFormModel {
  reportName: string
  categoryPath: string[]
  dateRange: string
  customTimes: string[]
  brandList: string[]
  carSeriesList: string[]
  channelIds: string[]
  sentimentList: string[]
  intentionList: string[]
  tagType: string
  experienceCode: ExperienceCodeValue
  topicCodes: string[]
  usageScenarioCodes: string[]
  scenarioAttr: string[]
  contentTypes: string[]
  advertisementType: string[]
  accountTypes: string[]
}

interface Emits {
  (e: 'update:visible', value: boolean): void
  (e: 'success', payload: { selectedParentId: string; selectedCategoryId: string }): void
}

const props = withDefaults(
  defineProps<{
    visible: boolean
    mode?: ReportDialogMode
    categoryId?: string
    editData?: DataPlazaReportItem | null
  }>(),
  {
    mode: 'create',
    categoryId: '',
    editData: null
  }
)

const emit = defineEmits<Emits>()

const userStore = useUserStore()

const innerVisible = computed({
  get: () => props.visible,
  set: value => emit('update:visible', value)
})

const dialogTitle = computed(() => (props.mode === 'edit' ? '编辑报告' : '新建报告'))
const isEditMode = computed(() => props.mode === 'edit')

const ruleFormRef = ref<FormInstance>()
const submitting = ref(false)
const loading = ref(false)
const suppressOptionWatch = ref(false)
const standardViewpointOptions = ref<StandardViewpointOption[]>([])

const conditionMap = computed<DataPlazaConditionMap>(() => {
  return (dataSquareStore.conditionGroups || []).reduce<DataPlazaConditionMap>((acc, item) => {
    acc[item.key] = Array.isArray(item.details) ? item.details : []
    return acc
  }, {})
})

const attributeTagOptions = computed(() => dataSquareStore.attributeTagOptions || [])

const formModel = reactive<ReportDialogFormModel>({
  reportName: '',
  categoryPath: [],
  dateRange: DEFAULT_DYNAMIC_DATE_RANGE,
  customTimes: [],
  brandList: [],
  carSeriesList: [],
  channelIds: [],
  sentimentList: [],
  intentionList: [],
  tagType: 'CA',
  experienceCode: createEmptyExperienceCodeValue(),
  topicCodes: [],
  usageScenarioCodes: [],
  scenarioAttr: [],
  contentTypes: [],
  advertisementType: [],
  accountTypes: []
})

const rules = reactive<FormRules<ReportDialogFormModel>>({
  reportName: [
    { required: true, message: '请输入报告名称', trigger: 'blur' },
    { max: 50, message: '报告名称不能超过50个字符', trigger: 'blur' }
  ],
  categoryPath: [
    {
      validator: (_rule, value: string[], callback) => {
        if (!Array.isArray(value) || value.length !== 2 || !value[1]) {
          callback(new Error('请选择所属分类'))
          return
        }
        callback()
      },
      trigger: 'change'
    }
  ],
  customTimes: [
    {
      validator: (_rule, _value, callback) => {
        if (isCustomDateRange.value && !isValidCustomTimes(formModel.customTimes)) {
          callback(new Error('请选择开始日期和结束日期'))
          return
        }
        callback()
      },
      trigger: 'change'
    }
  ],
  brandList: [
    {
      validator: (_rule, value: string[], callback) => {
        if (!Array.isArray(value) || value.length === 0) {
          callback(new Error('请选择品牌'))
          return
        }
        callback()
      },
      trigger: 'change'
    }
  ]
})

const categoryOptions = computed<CascaderOption[]>(() => {
  return (dataSquareStore.categoryTree || []).map(parent => ({
    value: parent.id,
    label: parent.categoryName,
    children: (parent.children || []).map(child => ({
      value: child.id,
      label: child.categoryName
    }))
  }))
})

const sentimentOptions = computed(() =>
  normalizeDictOptions(userStore.getDictItems('voc_sentiment'))
)
const intentionOptions = computed(() =>
  normalizeDictOptions(userStore.getDictItems('voc_intention'))
)
const contentTypeOptions = computed(() =>
  normalizeDictOptions(userStore.getDictItems('content_type'))
)
const adTypeOptions = computed(() => normalizeDictOptions(userStore.getDictItems('batch_ad_type')))
const accountTypeOptions = computed(() =>
  normalizeDictOptions(userStore.getDictItems('account_type'))
)

const tagTypeOptions = computed<TagTypeOption[]>(() => {
  const options = conditionMap.value.tagType || []
  return options.map(item => ({
    key: item.key,
    value: item.value
  }))
})

const brandSeriesTreeOptions = computed<DataPlazaConditionOption[]>(() => {
  return conditionMap.value[BRAND_SERIES_CONDITION_KEY] || []
})

const usageScenarioOptions = computed<CascaderOption[]>(() => {
  return (conditionMap.value.carScene || []) as unknown as CascaderOption[]
})

const topicCodesPlaceholder = computed(() => {
  return formModel.tagType ? '请选择标准观点' : '请先选择体验代码'
})

const channelTree = computed(() => dataSquareStore.channelTree || [])
const experienceCodeTreeOptions = computed(() => {
  return dataSquareStore.tagTreeMap[formModel.tagType] || []
})
const experienceCodeTreeLoading = computed(() => dataSquareStore.tagTreeLoading)

const categoryIdValue = computed(() => {
  return formModel.categoryPath[1] || ''
})

const isCustomDateRange = computed(() => formModel.dateRange === CUSTOM_DATE_RANGE)
const dateRangeMode = computed<'dynamic' | 'custom'>({
  get: () => (isCustomDateRange.value ? 'custom' : 'dynamic'),
  set: value => {
    switchDateRangeMode(value)
  }
})
const dynamicDateRangeOptions = computed(() => {
  return FE_TIME_DIMENSION_OPTIONS.filter(item =>
    DYNAMIC_DATE_RANGE_CODES.includes(
      String(item.code) as (typeof DYNAMIC_DATE_RANGE_CODES)[number]
    )
  )
})
const currentDateCondition = computed(() => buildDateConditionValue())

const normalizedSeriesOptions = computed(() => {
  const selectedBrandSet = new Set(formModel.brandList)
  const brandTreeSource = brandSeriesTreeOptions.value

  if (selectedBrandSet.size === 0) {
    return brandTreeSource.flatMap(item => item.children || [])
  }

  return brandTreeSource
    .filter(item => item.code && selectedBrandSet.has(item.code))
    .flatMap(item => item.children || [])
})

const brandSelectOptions = computed(() => {
  return brandSeriesTreeOptions.value
    .filter(item => !!item.code)
    .map(item => ({
      label: item.value,
      value: item.code || ''
    }))
})

const seriesSelectOptions = computed(() => {
  return normalizedSeriesOptions.value
    .filter(item => !!item.code)
    .map(item => ({
      label: item.value,
      value: item.code || ''
    }))
})

const validSeriesCodeSet = computed(() => {
  return new Set(
    normalizedSeriesOptions.value.map(item => item.code).filter((code): code is string => !!code)
  )
})

/**
 * 统一转换字典项，兼容 text/value 结构。
 * @param items 字典项列表
 * @returns 适配后的选项
 */
function normalizeDictOptions(items: Array<{ text?: string; value?: string }>) {
  return (items || []).map(item => ({
    label: item.text || '',
    value: item.value || ''
  }))
}

/**
 * 在初始化、回填、关闭清理阶段暂时屏蔽联动 watch，
 * 避免程序主动赋值重复触发接口请求。
 * @param task 需要在保护态内执行的任务
 * @returns 任务执行结果
 */
async function runWithWatchSuppressed<T>(task: () => Promise<T> | T) {
  suppressOptionWatch.value = true
  try {
    return await task()
  } finally {
    await nextTick()
    suppressOptionWatch.value = false
  }
}

/**
 * 重置表单到初始状态。
 */
function resetFormModel() {
  formModel.reportName = ''
  formModel.categoryPath = []
  formModel.dateRange = DEFAULT_DYNAMIC_DATE_RANGE
  formModel.customTimes = []
  formModel.brandList = []
  formModel.carSeriesList = []
  formModel.channelIds = []
  formModel.sentimentList = []
  formModel.intentionList = []
  formModel.tagType = getDefaultTagType()
  formModel.experienceCode = createEmptyExperienceCodeValue()
  formModel.topicCodes = []
  formModel.usageScenarioCodes = []
  formModel.scenarioAttr = []
  formModel.contentTypes = []
  formModel.advertisementType = []
  formModel.accountTypes = []
}

/**
 * 获取默认体验代码类型，优先取 CA。
 * @returns 默认体验代码 key
 */
function getDefaultTagType() {
  const typeOptions = tagTypeOptions.value
  return typeOptions.find(item => item.key === 'CA')?.key || typeOptions[0]?.key || 'CA'
}

/**
 * 根据二级分类 ID 回填所属分类路径。
 * @param categoryId 二级分类 ID
 * @returns 级联路径
 */
function resolveCategoryPath(categoryId?: string) {
  if (!categoryId) {
    return []
  }
  for (const parent of dataSquareStore.categoryTree || []) {
    if (parent.id === categoryId && parent.children?.length) {
      return [parent.id, parent.children[0].id]
    }
    const child = (parent.children || []).find(item => item.id === categoryId)
    if (child) {
      return [parent.id, child.id]
    }
  }
  return []
}

/**
 * 判断自定义时间是否已完整选择开始和结束日期。
 * @param value 自定义时间数组
 * @returns 是否为完整日期范围
 */
function isValidCustomTimes(value: string[]) {
  return Array.isArray(value) && value.length === 2
}

/**
 * 切换时间范围模式，保持两行布局下的状态切换语义清晰。
 * @param mode 目标时间模式
 */
function switchDateRangeMode(mode: 'dynamic' | 'custom') {
  if (mode === 'custom') {
    formModel.dateRange = CUSTOM_DATE_RANGE
    return
  }

  if (isCustomDateRange.value) {
    formModel.dateRange = DEFAULT_DYNAMIC_DATE_RANGE
  }
}

/**
 * 回填时间范围字段，统一处理动态时间与自定义时间。
 * @param storedDateRange 已保存的时间范围值
 * @param dateCondition 已保存的日期区间信息
 */
function applyStoredDateRange(
  storedDateRange: string,
  dateCondition: DataPlazaReportDateCondition
) {
  if (storedDateRange === CUSTOM_DATE_RANGE) {
    formModel.dateRange = CUSTOM_DATE_RANGE
    formModel.customTimes = [dateCondition.startDate || '', dateCondition.endDate || '']
    return
  }

  formModel.dateRange = storedDateRange
  formModel.customTimes = []
}

/**
 * 批量回填表单中的数组字段，保持编辑态字段处理方式一致。
 * @param conditionFormData 已保存的筛选条件
 */
function patchArrayFields(conditionFormData: DataPlazaReportDefaultCondition) {
  formModel.brandList = [...(conditionFormData.brandList || [])]
  formModel.carSeriesList = [...(conditionFormData.carSeriesList || [])]
  formModel.channelIds = conditionFormData.channelIds || []
  formModel.sentimentList = conditionFormData.sentimentList || []
  formModel.intentionList = conditionFormData.intentionList || []
  formModel.contentTypes = conditionFormData.contentTypes || []
  formModel.advertisementType = conditionFormData.advertisementType || []
  formModel.accountTypes = conditionFormData.accountTypes || []
  formModel.usageScenarioCodes = conditionFormData.usageScenarioCodes || []
  formModel.scenarioAttr = conditionFormData.scenarioAttr || []
  formModel.topicCodes = conditionFormData.topicCodes || []
}

/**
 * 按体验代码末级 code 初始化标准观点。
 * @param codes 末级体验代码
 */
async function initStandardViewpointOptions(codes?: string[]) {
  try {
    const filteredCodes = (codes || []).filter(code => code !== 'all')
    const response = await findFinalTagLibClientVoListByTagId(
      filteredCodes.length > 0
        ? {
            codes: filteredCodes,
            tagType: formModel.tagType
          }
        : {
            tagType: formModel.tagType
          }
    )
    standardViewpointOptions.value = Array.isArray(response.result) ? response.result : []
  } catch (error) {
    console.error('获取数据广场标准观点失败', error)
    standardViewpointOptions.value = []
  }
}

/**
 * 处理体验代码联动变化，统一刷新标准观点候选并清空已选值。
 * @param value 体验代码路径数组
 */
async function handleExperienceCodeChange(value: string[][]) {
  if (!props.visible || suppressOptionWatch.value) {
    return
  }
  formModel.topicCodes = []
  await initStandardViewpointOptions(getExperienceLastLevelCodes(value))
}

/**
 * 构造时间条件，仅保存展示所需的日期区间信息。
 * @returns 时间条件对象
 */
function buildDateConditionValue(): DataPlazaReportDateCondition {
  if (isCustomDateRange.value) {
    const [startDate = '', endDate = ''] = formModel.customTimes || []
    return {
      selectedShortcut: '自定义',
      startDate,
      endDate
    }
  }

  const dimensionItem = getTimeDimensionByCode(formModel.dateRange)
  return {
    selectedShortcut: dimensionItem?.name || ''
  }
}

/**
 * 构造 defaultCondition，统一保存报告筛选条件。
 * @returns defaultCondition 对象
 */
function buildDefaultCondition() {
  const conditionValue: DataPlazaReportDefaultCondition = {
    dateRange: formModel.dateRange,
    brandList: [...formModel.brandList],
    carSeriesList: [...formModel.carSeriesList],
    channelIds: [...formModel.channelIds],
    sentimentList: [...formModel.sentimentList],
    intentionList: [...formModel.intentionList],
    tagType: formModel.tagType,
    experienceCode: normalizeExperienceCodeValue(formModel.experienceCode),
    topicCodes: [...formModel.topicCodes],
    usageScenarioCodes: [...formModel.usageScenarioCodes],
    scenarioAttr: [...formModel.scenarioAttr],
    contentTypes: [...formModel.contentTypes],
    advertisementType: [...formModel.advertisementType],
    accountTypes: [...formModel.accountTypes]
  }
  return conditionValue
}

/**
 * 根据当前品牌筛选结果清理车系选中值，避免保留无效车系。
 * 无品牌时保留全部品牌下仍存在的车系。
 */
function sanitizeCarSeriesSelection() {
  if (!Array.isArray(formModel.carSeriesList) || formModel.carSeriesList.length === 0) {
    return
  }

  formModel.carSeriesList = formModel.carSeriesList.filter(code =>
    validSeriesCodeSet.value.has(code)
  )
}

/**
 * 从编辑数据回填表单。
 * @param row 报告数据
 */
async function patchFormByEditData(row: DataPlazaReportItem) {
  await runWithWatchSuppressed(async () => {
    resetFormModel()
    formModel.reportName = row.reportName || ''
    formModel.categoryPath = resolveCategoryPath(row.categoryId)

    const defaultCondition = row.defaultCondition as DataPlazaReportDefaultCondition
    const dateCondition = row.dateCondition as DataPlazaReportDateCondition

    patchArrayFields(defaultCondition)
    const nextTagType = defaultCondition.tagType || getDefaultTagType()
    formModel.tagType = nextTagType
    formModel.experienceCode = normalizeExperienceCodeValue(defaultCondition.experienceCode)
    applyStoredDateRange(defaultCondition.dateRange, dateCondition)
  })

  await nextTick()
  sanitizeCarSeriesSelection()
  await initStandardViewpointOptions(getExperienceLastLevelCodes(formModel.experienceCode))
}

/**
 * 初始化弹窗数据。
 */
async function initDialog() {
  loading.value = true
  try {
    await runWithWatchSuppressed(async () => {
      resetFormModel()
      await Promise.all([
        dataSquareStore.categoryTree.length
          ? Promise.resolve()
          : dataSquareActions.updateCategoryTree(),
        dataSquareStore.channelTree.length
          ? Promise.resolve()
          : dataSquareActions.updateChannelTree(),
        dataSquareStore.conditionGroups.length
          ? Promise.resolve()
          : dataSquareActions.updateConditionGroups(),
        dataSquareStore.attributeTagOptions.length
          ? Promise.resolve()
          : dataSquareActions.updateAttributeTagOptions()
      ])

      if (isEditMode.value && props.editData) {
        return
      }

      formModel.tagType = getDefaultTagType()
      formModel.categoryPath = resolveCategoryPath(props.categoryId)
    })

    if (isEditMode.value && props.editData) {
      await patchFormByEditData(props.editData)
    } else {
      await initStandardViewpointOptions()
    }
  } finally {
    loading.value = false
  }
}

/**
 * 关闭弹窗并清理状态。
 */
function handleClose() {
  void runWithWatchSuppressed(() => {
    ruleFormRef.value?.clearValidate()
    standardViewpointOptions.value = []
    loading.value = false
    submitting.value = false
    resetFormModel()
  })
}

/**
 * 提交报告新增/编辑。
 * @param ctx 弹窗关闭上下文
 */
async function handleSubmit(ctx: { close: () => void }) {
  if (!ruleFormRef.value || submitting.value) {
    return
  }

  const valid = await ruleFormRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  submitting.value = true
  try {
    const payload: DataPlazaReportSaveParams = {
      reportName: formModel.reportName.trim(),
      categoryId: categoryIdValue.value,
      dateCondition: currentDateCondition.value,
      defaultCondition: buildDefaultCondition()
    }

    const response =
      isEditMode.value && props.editData?.id
        ? await updateDataPlazaReport({
            ...payload,
            id: props.editData.id
          })
        : await insertDataPlazaReport(payload)

    if (!response.success) {
      ElMessage.error(response.message || `操作失败`)
      return
    }

    ElMessage.success(response.message || `操作成功`)
    emit('success', {
      selectedParentId: formModel.categoryPath[0] || '',
      selectedCategoryId: categoryIdValue.value
    })
    ctx.close()
  } catch (error) {
    console.error('提交数据广场报告失败', error)
    ElMessage.error(error instanceof Error ? error.message : '操作失败')
  } finally {
    submitting.value = false
  }
}

watch(
  () => formModel.brandList,
  () => {
    sanitizeCarSeriesSelection()
  },
  { deep: true }
)

watch(
  () => brandSeriesTreeOptions.value,
  () => {
    if (!props.visible) {
      return
    }
    sanitizeCarSeriesSelection()
  },
  { deep: true }
)

watch(
  () => props.visible,
  value => {
    if (value) {
      void initDialog()
    }
  },
  { immediate: true }
)
</script>

<template>
  <AppDialog
    v-model:visible="innerVisible"
    :title="dialogTitle"
    width="730px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    destroy-on-close
    :confirm="handleSubmit"
    @close="handleClose"
  >
    <div class="report-dialog">
      <el-form v-loading="loading" ref="ruleFormRef" :model="formModel" :rules="rules" label-width="84px">
        <el-form-item label="报告名称" prop="reportName" required>
          <el-input v-model="formModel.reportName" placeholder="请输入" maxlength="50" />
        </el-form-item>

        <el-form-item label="所属分类" prop="categoryPath" required>
          <el-cascader
            v-model="formModel.categoryPath"
            :options="categoryOptions"
            :props="{
              value: 'value',
              label: 'label',
              children: 'children',
              emitPath: true,
              checkStrictly: false
            }"
            clearable
            class="w-full"
            placeholder="请选择"
          />
        </el-form-item>

        <el-form-item label="时间范围" required class="report-dialog__time-item">
          <div class="report-dialog__time-layout">
            <div class="report-dialog__time-row">
              <el-radio-group v-model="dateRangeMode">
                <el-radio label="dynamic" value="dynamic">动态时间</el-radio>
              </el-radio-group>
              <div class="report-dialog__time-box">
                <el-radio-group
                  v-model="formModel.dateRange"
                  :disabled="dateRangeMode !== 'dynamic'"
                >
                  <el-radio
                    v-for="item in dynamicDateRangeOptions"
                    :key="item.code"
                    :label="String(item.code)"
                    :value="String(item.code)"
                  >
                    {{ item.name }}
                  </el-radio>
                </el-radio-group>
              </div>
            </div>

            <div class="report-dialog__time-row">
              <el-radio-group v-model="dateRangeMode">
                <el-radio label="custom" value="custom">特定时间</el-radio>
              </el-radio-group>
              <el-form-item prop="customTimes" class="report-dialog__time-picker">
                <el-date-picker
                  v-model="formModel.customTimes"
                  type="daterange"
                  value-format="YYYY-MM-DD"
                  format="YYYY-MM-DD"
                  range-separator="—"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  clearable
                  :disabled="dateRangeMode !== 'custom'"
                />
              </el-form-item>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="品牌" prop="brandList" required>
          <el-select-v2
            v-model="formModel.brandList"
            :options="brandSelectOptions"
            multiple
            clearable
            collapse-tags
            :max-collapse-tags="3"
            collapse-tags-tooltip
            filterable
            placeholder="请选择"
            class="w-full"
          />
        </el-form-item>

        <el-form-item label="车系">
          <el-select-v2
            v-model="formModel.carSeriesList"
            :options="seriesSelectOptions"
            multiple
            clearable
            collapse-tags
            :max-collapse-tags="3"
            collapse-tags-tooltip
            filterable
            placeholder="请选择"
            class="w-full"
          />
        </el-form-item>

        <el-form-item label="数据源">
          <DataSourceCascader
            v-model="formModel.channelIds"
            :options="channelTree"
            :condition="{ multiSelect: true }"
            child-key="data-square-channel"
            :wait-for-parent="true"
            class="w-full"
          />
        </el-form-item>

        <el-form-item label="情感">
          <BtnSwitchWithAll v-model="formModel.sentimentList" :options="sentimentOptions" />
        </el-form-item>

        <el-form-item label="意图">
          <BtnSwitchWithAll v-model="formModel.intentionList" :options="intentionOptions" />
        </el-form-item>

        <el-form-item label="体验代码">
          <DataSquareExperienceCodeSelector
            v-model="formModel.experienceCode"
            v-model:tagType="formModel.tagType"
            :type-options="tagTypeOptions"
            :options="experienceCodeTreeOptions"
            :loading="experienceCodeTreeLoading"
            :default-tag-type="getDefaultTagType()"
            class="w-full"
            @change="handleExperienceCodeChange"
          />
        </el-form-item>

        <el-form-item label="标准观点">
          <SelectV2WithSelectAll
            v-model="formModel.topicCodes"
            :options="standardViewpointOptions"
            :props="{ label: 'tagName', value: 'tagCode' }"
            :placeholder="topicCodesPlaceholder"
            :show-select-all="true"
            :multiple="true"
            class="w-full"
          />
        </el-form-item>

        <el-form-item label="用车场景">
          <el-cascader
            v-model="formModel.usageScenarioCodes"
            :options="usageScenarioOptions"
            :props="{
              value: 'value',
              label: 'value',
              children: 'children',
              multiple: true,
              emitPath: false,
              checkStrictly: false
            }"
            collapse-tags
            :max-collapse-tags="2"
            collapse-tags-tooltip
            filterable
            clearable
            placeholder="请选择"
            class="w-full"
          />
        </el-form-item>

        <el-form-item label="属性标签">
          <el-select-v2
            v-model="formModel.scenarioAttr"
            :options="attributeTagOptions"
            :props="{ label: 'name', value: 'id' }"
            multiple
            clearable
            collapse-tags
            :max-collapse-tags="3"
            collapse-tags-tooltip
            filterable
            placeholder="请选择"
            class="w-full"
          />
        </el-form-item>

        <el-form-item label="内容类型">
          <el-select
            v-model="formModel.contentTypes"
            :options="contentTypeOptions"
            :props="{ label: 'label', value: 'value' }"
            multiple
            clearable
            collapse-tags
            :max-collapse-tags="3"
            collapse-tags-tooltip
            filterable
            placeholder="请选择"
            class="w-full"
          />
        </el-form-item>

        <el-form-item label="原声类型">
          <el-select
            v-model="formModel.advertisementType"
            :options="adTypeOptions"
            :props="{ label: 'label', value: 'value' }"
            multiple
            clearable
            collapse-tags
            collapse-tags-tooltip
            :max-collapse-tags="3"
            placeholder="请选择"
            class="w-full"
          />
        </el-form-item>

        <el-form-item label="账号类型">
          <el-select
            v-model="formModel.accountTypes"
            :options="accountTypeOptions"
            :props="{ label: 'label', value: 'label' }"
            multiple
            clearable
            collapse-tags
            collapse-tags-tooltip
            :max-collapse-tags="3"
            filterable
            placeholder="请选择"
            class="w-full"
          />
        </el-form-item>
      </el-form>
    </div>
  </AppDialog>
</template>

<style lang="scss" scoped>
.report-dialog {
  max-height: calc(100vh - 220px);
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 8px;
}

.report-dialog__time-item {
  :deep(.el-form-item__content) {
    display: block;
  }
}

.report-dialog__time-layout {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.report-dialog__time-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.report-dialog__time-box {
  flex: 1;
  min-height: 40px;
  padding: 0 16px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  display: flex;
  align-items: center;
}

.report-dialog__time-picker {
  flex: 1;
  margin-bottom: 0 !important;

  :deep(.el-form-item__content) {
    width: 100%;
  }

  :deep(.el-date-editor) {
    width: 100%;
  }
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.switch-btn-wrapper) {
  width: 100%;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

:deep(.sw-item) {
  min-width: 56px;
  height: 32px;
  padding: 0 16px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  color: #4e5969;
  cursor: pointer;
  transition: all 0.2s ease;
}

:deep(.sw-item.active) {
  border-color: #165dff;
  background: #165dff;
  color: #fff;
}
</style>
