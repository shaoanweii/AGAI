<script setup lang="ts">
import dayjs from 'dayjs'
import { computed, nextTick, reactive, ref, shallowRef, watch } from 'vue'
import { showToast } from 'vant'
import { FE_TIME_DIMENSION_OPTIONS } from '@/constants'
import { useUserStore } from '@/store'
import { getShortcutDateRange, getTimeDimensionByCode } from '@/utils/date'
import HSelectTabs from '@h5/components/UI/HSelectTabs'
import HListSingleSelect from '@h5/components/UI/HListSingleSelect'
import HFilterTreeMultiSelect from '@h5/components/UI/HFilterTreeMultiSelect/index.vue'
import HFilterMultiSelect from '@h5/views/taskEvent/components/HFilterMultiSelect.vue'
import type {
  H5DataSquareConditionOption,
  H5DataSquareLabelTag,
  H5DataSquareReportDateCondition,
  H5DataSquareReportDefaultCondition,
  H5DataSquareStandardViewpointOption
} from '@h5/api/dataSquare'
import { findH5FinalTagLibClientVoListByTagId, getH5TagLibClientTree } from '@h5/api/dataSquare'
import { h5DataSquareActions, h5DataSquareStore } from '@h5/views/dataSquare/store'
import {
  getExperienceLastLevelCodes,
  normalizeExperienceCodeValue,
  normalizeReportDefaultCondition
} from '../utils'
import ReportExperienceCodeTreeSelect from './ReportExperienceCodeTreeSelect.vue'

defineOptions({
  name: 'ReportFilterPanel'
})

type OptionItem = {
  label: string
  value: string
}

type H5DataSquareConditionMap = Record<string, H5DataSquareConditionOption[]>

type ReportFilterFormModel = Omit<
  H5DataSquareReportDefaultCondition,
  'brandList' | 'carSeriesList'
> & {
  brandList: string[]
  carSeriesList: string[]
}

interface Emits {
  (e: 'update:show', value: boolean): void
  (
    e: 'confirm',
    payload: {
      defaultCondition: H5DataSquareReportDefaultCondition
      dateCondition: H5DataSquareReportDateCondition
    }
  ): void
}

const props = withDefaults(
  defineProps<{
    show: boolean
    currentCondition?: H5DataSquareReportDefaultCondition | null
    currentDateCondition?: H5DataSquareReportDateCondition | null
    initialCondition?: H5DataSquareReportDefaultCondition | null
    initialDateCondition?: H5DataSquareReportDateCondition | null
  }>(),
  {
    currentCondition: null,
    currentDateCondition: null,
    initialCondition: null,
    initialDateCondition: null
  }
)

const emit = defineEmits<Emits>()

const CUSTOM_DATE_RANGE = 'custom'
const DEFAULT_DYNAMIC_DATE_RANGE = '3'
const DYNAMIC_DATE_RANGE_CODES = ['2', '3'] as const
const BRAND_SERIES_CONDITION_KEY = 'product_competitors_brand_car_series'

const userStore = useUserStore()
const loading = ref(false)
const panelReady = ref(false)
const calendarVisible = ref(false)
const standardViewpointOptions = ref<H5DataSquareStandardViewpointOption[]>([])
const tagTreeLoading = ref(false)
const tagOptions = shallowRef<H5DataSquareLabelTag[]>([])
const suppressWatch = ref(false)
const lastStandardViewpointRequestKey = ref('')
const currentTagTreeKey = ref('')
let openSeq = 0
let tagTreeRequestSeq = 0
const tagTreeCache = new Map<string, H5DataSquareLabelTag[]>()
const tagTreeRequestMap = new Map<string, Promise<H5DataSquareLabelTag[]>>()

const formModel = reactive<ReportFilterFormModel>({
  dateRange: DEFAULT_DYNAMIC_DATE_RANGE,
  brandList: [],
  carSeriesList: [],
  channelIds: [],
  sentimentList: [],
  intentionList: [],
  tagType: 'CA',
  experienceCode: [],
  topicCodes: [],
  usageScenarioCodes: [],
  scenarioAttr: [],
  contentTypes: [],
  advertisementType: [],
  accountTypes: []
})

const customTimes = ref<string[]>(getShortcutDateRange(DEFAULT_DYNAMIC_DATE_RANGE))

const innerVisible = computed({
  get: () => props.show,
  set: value => emit('update:show', value)
})

const conditionMap = computed<H5DataSquareConditionMap>(() => {
  return (h5DataSquareStore.conditionGroups || []).reduce<H5DataSquareConditionMap>((acc, item) => {
    acc[item.key] = Array.isArray(item.details) ? item.details : []
    return acc
  }, {})
})

const brandSeriesTreeOptions = computed<H5DataSquareConditionOption[]>(() => {
  return conditionMap.value[BRAND_SERIES_CONDITION_KEY] || []
})

const selectedBrandSet = computed(() => new Set(formModel.brandList || []))

const normalizedSeriesOptions = computed(() => {
  if (selectedBrandSet.value.size === 0) {
    return brandSeriesTreeOptions.value.flatMap(item => item.children || [])
  }

  return brandSeriesTreeOptions.value
    .filter(item => item.code && selectedBrandSet.value.has(item.code))
    .flatMap(item => item.children || [])
})

const brandSelectOptions = computed<OptionItem[]>(() => {
  return brandSeriesTreeOptions.value
    .filter(item => !!item.code)
    .map(item => ({
      label: item.value,
      value: item.code || ''
    }))
})

const seriesSelectOptions = computed<OptionItem[]>(() => {
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

const usageScenarioTreeOptions = computed(() => conditionMap.value.carScene || [])

const usageScenarioVariant = computed(() => {
  return getTreeMaxDepth(usageScenarioTreeOptions.value, 'children') > 2 ? 'tree' : 'group'
})

const attributeTagOptions = computed<OptionItem[]>(() => {
  return (h5DataSquareStore.attributeTagOptions || []).map(item => ({
    label: item.name || '',
    value: item.id || ''
  }))
})

const tagTypeOptions = computed<OptionItem[]>(() => {
  return (conditionMap.value.tagType || []).map(item => ({
    label: item.value,
    value: item.key
  }))
})

const sentimentTabOptions = computed(() =>
  normalizeDictTabOptions(userStore.getDictItems('voc_sentiment'))
)
const intentionTabOptions = computed(() =>
  normalizeDictTabOptions(userStore.getDictItems('voc_intention'))
)
const contentTypeOptions = computed(() =>
  normalizeDictOptions(userStore.getDictItems('content_type'))
)
const adTypeOptions = computed(() => normalizeDictOptions(userStore.getDictItems('batch_ad_type')))
const accountTypeOptions = computed(() =>
  normalizeDictOptions(userStore.getDictItems('account_type'))
)

const dynamicDateOptions = computed(() => {
  return FE_TIME_DIMENSION_OPTIONS.filter(item =>
    DYNAMIC_DATE_RANGE_CODES.includes(
      String(item.code) as (typeof DYNAMIC_DATE_RANGE_CODES)[number]
    )
  )
})

const isCustomDateRange = computed(() => formModel.dateRange === CUSTOM_DATE_RANGE)

const calendarDefaultDate = computed<[Date, Date]>(() => {
  const [startDate, endDate] = customTimes.value
  const start = dayjs(startDate)
  const end = dayjs(endDate)
  if (start.isValid() && end.isValid()) {
    return [start.toDate(), end.toDate()]
  }
  return [dayjs().subtract(29, 'day').toDate(), dayjs().toDate()]
})

const dateRangeText = computed(() => {
  const [startDate, endDate] = customTimes.value
  if (!startDate || !endDate) {
    return '请选择日期范围'
  }
  return `${startDate.replace(/-/g, '.')}–${endDate.replace(/-/g, '.')}`
})

const topicCodesPlaceholder = computed(() => {
  if (standardViewpointOptions.value.length === 0) {
    return '请选择'
  }
  return '请选择'
})

const experienceLastLevelCodes = computed(() =>
  getExperienceLastLevelCodes(formModel.experienceCode)
)

const standardViewpointQueryKey = computed(() =>
  [formModel.tagType || '', ...experienceLastLevelCodes.value].join('|')
)

/**
 * 字典项转换为 H5 选择组件需要的 label/value 结构。
 * @param items 字典项
 * @returns 选择项
 */
function normalizeDictOptions(items: Array<{ text?: string; value?: string }>) {
  return (items || []).map(item => ({
    label: item.text || '',
    value: item.value || ''
  }))
}

/**
 * 字典项转换为按钮组需要的 code/name 结构。
 * @param items 字典项
 * @returns 按钮组选项
 */
function normalizeDictTabOptions(items: Array<{ text?: string; value?: string }>) {
  return (items || []).map(item => ({
    name: item.text || '',
    code: item.value || ''
  }))
}

/**
 * 获取树的最大深度，用于判断 H5 展示应采用分组还是树形。
 * @param list 树形数据
 * @param childrenKey 子节点字段
 * @returns 最大深度
 */
function getTreeMaxDepth(list: any[], childrenKey: string): number {
  if (!Array.isArray(list) || list.length === 0) {
    return 0
  }

  return list.reduce((maxDepth, item) => {
    const children = Array.isArray(item?.[childrenKey]) ? item[childrenKey] : []
    return Math.max(maxDepth, 1 + getTreeMaxDepth(children, childrenKey))
  }, 0)
}

/**
 * 根据当前品牌清理无效车系，避免提交跨品牌残留值。
 */
function sanitizeCarSeriesSelection() {
  const selected = formModel.carSeriesList || []
  if (!selected.length) {
    return
  }
  formModel.carSeriesList = selected.filter(code => validSeriesCodeSet.value.has(code))
}

/**
 * 获取体验代码类型默认值。
 * @returns 默认类型
 */
function getDefaultTagType() {
  return (
    tagTypeOptions.value.find(item => item.value === 'CA')?.value ||
    tagTypeOptions.value[0]?.value ||
    'CA'
  )
}

/**
 * 回填弹层表单。
 * @param condition 筛选条件
 * @param dateCondition 日期条件
 */
function patchForm(
  condition?: H5DataSquareReportDefaultCondition | null,
  dateCondition?: H5DataSquareReportDateCondition | null
) {
  const nextCondition = normalizeReportDefaultCondition(condition)
  Object.assign(formModel, {
    ...nextCondition,
    brandList: [...(nextCondition.brandList || [])],
    carSeriesList: [...(nextCondition.carSeriesList || [])],
    tagType: nextCondition.tagType || getDefaultTagType(),
    experienceCode: normalizeExperienceCodeValue(nextCondition.experienceCode)
  })

  if (nextCondition.dateRange === CUSTOM_DATE_RANGE) {
    customTimes.value = [dateCondition?.startDate || '', dateCondition?.endDate || ''].filter(
      Boolean
    )
    return
  }

  if (nextCondition.dateRange) {
    customTimes.value = getShortcutDateRange(nextCondition.dateRange)
    return
  }

  formModel.dateRange = DEFAULT_DYNAMIC_DATE_RANGE
  customTimes.value = getShortcutDateRange(DEFAULT_DYNAMIC_DATE_RANGE)
}

/**
 * 构造日期条件，供详情页查询参数转换使用。
 * @returns 日期条件
 */
function buildDateCondition(): H5DataSquareReportDateCondition {
  if (isCustomDateRange.value) {
    const [startDate = '', endDate = ''] = customTimes.value
    return {
      selectedShortcut: '自定义',
      startDate,
      endDate
    }
  }

  const [startDate, endDate] = getShortcutDateRange(
    formModel.dateRange || DEFAULT_DYNAMIC_DATE_RANGE
  )
  const dimensionItem = getTimeDimensionByCode(formModel.dateRange || DEFAULT_DYNAMIC_DATE_RANGE)
  return {
    selectedShortcut: dimensionItem?.name || '',
    startDate,
    endDate
  }
}

/**
 * 构造可提交的筛选条件。
 * @returns 筛选条件
 */
function buildDefaultCondition(): H5DataSquareReportDefaultCondition {
  return {
    dateRange: formModel.dateRange || DEFAULT_DYNAMIC_DATE_RANGE,
    brandList: [...(formModel.brandList || [])],
    carSeriesList: [...(formModel.carSeriesList || [])],
    channelIds: [...(formModel.channelIds || [])],
    sentimentList: [...(formModel.sentimentList || [])],
    intentionList: [...(formModel.intentionList || [])],
    tagType: formModel.tagType || getDefaultTagType(),
    experienceCode: normalizeExperienceCodeValue(formModel.experienceCode),
    topicCodes: [...(formModel.topicCodes || [])],
    usageScenarioCodes: [...(formModel.usageScenarioCodes || [])],
    scenarioAttr: [...(formModel.scenarioAttr || [])],
    contentTypes: [...(formModel.contentTypes || [])],
    advertisementType: [...(formModel.advertisementType || [])],
    accountTypes: [...(formModel.accountTypes || [])]
  }
}

/**
 * 加载当前体验代码类型下的标签树，并按类型缓存，避免弹层重复打开时反复请求。
 * @param tagType 标签类型
 */
async function loadTagTree(tagType: string) {
  if (!tagType) {
    tagOptions.value = []
    currentTagTreeKey.value = ''
    return
  }

  currentTagTreeKey.value = tagType
  if (tagTreeCache.has(tagType)) {
    tagOptions.value = tagTreeCache.get(tagType) || []
    return
  }

  const requestSeq = ++tagTreeRequestSeq
  try {
    tagTreeLoading.value = true
    let requestTask = tagTreeRequestMap.get(tagType)
    if (!requestTask) {
      requestTask = getH5TagLibClientTree({ tagLibType: tagType })
        .then(response => (Array.isArray(response.result) ? response.result : []))
        .finally(() => {
          tagTreeRequestMap.delete(tagType)
        })
      tagTreeRequestMap.set(tagType, requestTask)
    }

    const nextOptions = await requestTask
    tagTreeCache.set(tagType, nextOptions)
    if (currentTagTreeKey.value === tagType) {
      tagOptions.value = nextOptions
    }
  } catch (error) {
    console.error('获取数据报告体验代码树失败', error)
    if (currentTagTreeKey.value === tagType) {
      tagOptions.value = []
    }
  } finally {
    if (requestSeq === tagTreeRequestSeq) {
      tagTreeLoading.value = false
    }
  }
}

/**
 * 按体验代码末级编码加载标准观点。
 * @param codes 末级编码
 */
async function initStandardViewpointOptions(codes?: string[]) {
  const filteredCodes = (codes || []).filter(code => code !== 'all')
  const requestKey = [formModel.tagType || '', ...filteredCodes].join('|')
  if (lastStandardViewpointRequestKey.value === requestKey) {
    return
  }

  lastStandardViewpointRequestKey.value = requestKey
  try {
    const response = await findH5FinalTagLibClientVoListByTagId(
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
    console.error('获取数据报告标准观点失败', error)
    lastStandardViewpointRequestKey.value = ''
    standardViewpointOptions.value = []
  }
}

/**
 * 初始化筛选选项。
 */
async function initOptions() {
  loading.value = true
  try {
    await Promise.all([
      h5DataSquareStore.channelTree.length
        ? Promise.resolve()
        : h5DataSquareActions.updateChannelTree(),
      h5DataSquareStore.conditionGroups.length
        ? Promise.resolve()
        : h5DataSquareActions.updateConditionGroups(),
      h5DataSquareStore.attributeTagOptions.length
        ? Promise.resolve()
        : h5DataSquareActions.updateAttributeTagOptions(),
      Object.keys(userStore.allDictItems || {}).length
        ? Promise.resolve()
        : userStore.getSysAllDictItems()
    ])
  } finally {
    loading.value = false
  }
}

/**
 * 等待两帧后再挂载筛选表单，减少底部弹层打开卡顿。
 */
async function schedulePanelReady() {
  const seq = ++openSeq
  panelReady.value = false
  await nextTick()
  await new Promise<void>(resolve => requestAnimationFrame(() => resolve()))
  await new Promise<void>(resolve => requestAnimationFrame(() => resolve()))
  if (seq === openSeq) {
    panelReady.value = true
  }
}

/**
 * 打开弹层时初始化依赖和回显当前条件。
 */
async function handleOpen() {
  await initOptions()
  suppressWatch.value = true
  patchForm(props.currentCondition, props.currentDateCondition)
  await nextTick()
  suppressWatch.value = false
  await Promise.all([
    loadTagTree(formModel.tagType || getDefaultTagType()),
    initStandardViewpointOptions(experienceLastLevelCodes.value)
  ])
  void schedulePanelReady()
}

/**
 * 关闭弹层时清理重组件状态。
 */
function handleClosed() {
  openSeq++
  panelReady.value = false
  calendarVisible.value = false
}

/**
 * 切换快捷日期。
 * @param code 快捷日期编码
 */
function handleDateShortcut(code: string) {
  formModel.dateRange = code
  customTimes.value = getShortcutDateRange(code)
}

/**
 * 打开自定义日期选择器。
 */
function handleCustomDateClick() {
  formModel.dateRange = CUSTOM_DATE_RANGE
  calendarVisible.value = true
}

/**
 * 自定义日期确认。
 * @param value 日期范围
 */
function handleCalendarConfirm(value: Date | Date[]) {
  const dates = Array.isArray(value) ? value : [value]
  if (dates.length < 2 || !dates[0] || !dates[1]) {
    return
  }
  customTimes.value = [dayjs(dates[0]).format('YYYY-MM-DD'), dayjs(dates[1]).format('YYYY-MM-DD')]
  calendarVisible.value = false
}

/**
 * 重置为报告原始默认筛选条件。
 */
async function handleReset() {
  suppressWatch.value = true
  patchForm(props.initialCondition, props.initialDateCondition)
  await nextTick()
  suppressWatch.value = false
  sanitizeCarSeriesSelection()
  await Promise.all([
    loadTagTree(formModel.tagType || getDefaultTagType()),
    initStandardViewpointOptions(experienceLastLevelCodes.value)
  ])
}

/**
 * 提交筛选条件，交由详情页刷新全部模块。
 */
function handleSubmit() {
  if (isCustomDateRange.value && customTimes.value.length !== 2) {
    showToast('请选择时间范围')
    return
  }

  if (!formModel.brandList?.length) {
    showToast('请选择品牌')
    return
  }

  emit('confirm', {
    defaultCondition: buildDefaultCondition(),
    dateCondition: buildDateCondition()
  })
  innerVisible.value = false
}

watch(
  () => formModel.brandList,
  () => {
    sanitizeCarSeriesSelection()
  },
  { deep: true }
)

watch(
  () => formModel.dateRange,
  value => {
    if (!value || value === CUSTOM_DATE_RANGE) {
      return
    }
    customTimes.value = getShortcutDateRange(value)
  }
)

watch(
  () => formModel.tagType,
  async (value, oldValue) => {
    if (!value || suppressWatch.value) {
      return
    }
    await loadTagTree(value)
    if (oldValue && oldValue !== value) {
      formModel.experienceCode = []
      formModel.topicCodes = []
      standardViewpointOptions.value = []
      lastStandardViewpointRequestKey.value = ''
      await initStandardViewpointOptions()
    }
  }
)

watch(
  () => standardViewpointQueryKey.value,
  async () => {
    if (suppressWatch.value) {
      return
    }
    formModel.topicCodes = []
    await initStandardViewpointOptions(experienceLastLevelCodes.value)
  }
)
</script>

<template>
  <van-popup
    v-model:show="innerVisible"
    position="bottom"
    round
    :safe-area-inset-bottom="true"
    :style="{ maxHeight: '95%', overflow: 'hidden', width: '100%' }"
    teleport="body"
    @open="handleOpen"
    @closed="handleClosed"
  >
    <div class="report-filter-panel">
      <div v-if="loading || !panelReady" class="report-filter-panel__loading">
        <van-loading size="24px" color="#1677ff">加载筛选条件...</van-loading>
      </div>
      <van-form v-else class="report-filter-panel__form" @submit="handleSubmit">
        <div class="report-filter-panel__body">
          <div class="filter-form-item is-required">
            <div class="filter-form-item__label">时间范围</div>
            <div class="filter-form-item__control">
              <div class="date-filter">
                <div class="date-filter__tabs">
                  <div
                    v-for="item in dynamicDateOptions"
                    :key="item.code"
                    class="date-filter__tab"
                    :class="{ 'is-active': formModel.dateRange === String(item.code) }"
                    @click="handleDateShortcut(String(item.code))"
                  >
                    {{ item.name }}
                  </div>
                  <div
                    class="date-filter__tab"
                    :class="{ 'is-active': isCustomDateRange }"
                    @click="handleCustomDateClick"
                  >
                    自定义
                  </div>
                </div>
                <div
                  v-if="isCustomDateRange"
                  class="date-filter__custom"
                  @click="calendarVisible = true"
                >
                  <span class="date-filter__custom-text van-ellipsis">{{ dateRangeText }}</span>
                  <van-icon name="calendar-o" />
                </div>
              </div>
            </div>
          </div>

          <div class="filter-form-item is-required">
            <div class="filter-form-item__label">品牌</div>
            <div class="filter-form-item__control">
              <HFilterMultiSelect
                v-model="formModel.brandList"
                title="品牌"
                placeholder="请选择"
                :options="brandSelectOptions"
              />
            </div>
          </div>

          <div class="filter-form-item">
            <div class="filter-form-item__label">车系</div>
            <div class="filter-form-item__control">
              <HFilterMultiSelect
                v-model="formModel.carSeriesList"
                title="车系"
                placeholder="请选择"
                :options="seriesSelectOptions"
              />
            </div>
          </div>

          <div class="filter-form-item">
            <div class="filter-form-item__label">数据源</div>
            <div class="filter-form-item__control">
              <HFilterTreeMultiSelect
                v-model="formModel.channelIds"
                title="数据源"
                placeholder="请选择"
                :options="h5DataSquareStore.channelTree"
                :fields="{ label: 'name', value: 'code', children: 'child' }"
                variant="tree"
                leaf-only
                cascade-check
                searchable
              />
            </div>
          </div>

          <div class="filter-form-item">
            <div class="filter-form-item__label">情感</div>
            <div class="filter-form-item__control">
              <HSelectTabs
                v-model="formModel.sentimentList"
                :options="sentimentTabOptions"
                multi-select
                custom-class="report-filter-tabs"
              />
            </div>
          </div>

          <div class="filter-form-item">
            <div class="filter-form-item__label">意图</div>
            <div class="filter-form-item__control">
              <HSelectTabs
                v-model="formModel.intentionList"
                :options="intentionTabOptions"
                multi-select
                custom-class="report-filter-tabs"
              />
            </div>
          </div>

          <div class="filter-form-item">
            <div class="filter-form-item__label">体验代码</div>
            <div class="filter-form-item__control">
              <div class="experience-layout">
                <HListSingleSelect
                  v-model="formModel.tagType"
                  title="体验代码类型"
                  placeholder="请选择"
                  :options="tagTypeOptions"
                  :searchable="false"
                  class="experience-layout__type"
                />
                <ReportExperienceCodeTreeSelect
                  v-model="formModel.experienceCode"
                  class="experience-layout__code"
                  :options="tagOptions"
                  :loading="tagTreeLoading"
                />
              </div>
            </div>
          </div>

          <div class="filter-form-item">
            <div class="filter-form-item__label">标准观点</div>
            <div class="filter-form-item__control">
              <HFilterMultiSelect
                v-model="formModel.topicCodes"
                title="标准观点"
                :placeholder="topicCodesPlaceholder"
                :options="standardViewpointOptions"
                :fields="{ label: 'tagName', value: 'tagCode' }"
                pin-mode="selected"
              />
            </div>
          </div>

          <div class="filter-form-item">
            <div class="filter-form-item__label">用车场景</div>
            <div class="filter-form-item__control">
              <HFilterTreeMultiSelect
                v-model="formModel.usageScenarioCodes"
                title="用车场景"
                placeholder="请选择"
                :options="usageScenarioTreeOptions"
                :fields="{ label: 'value', value: 'value', children: 'children' }"
                :variant="usageScenarioVariant"
                leaf-only
                cascade-check
                searchable
              />
            </div>
          </div>

          <div class="filter-form-item">
            <div class="filter-form-item__label">属性标签</div>
            <div class="filter-form-item__control">
              <HFilterMultiSelect
                v-model="formModel.scenarioAttr"
                title="属性标签"
                placeholder="请选择"
                :options="attributeTagOptions"
              />
            </div>
          </div>

          <div class="filter-form-item">
            <div class="filter-form-item__label">内容类型</div>
            <div class="filter-form-item__control">
              <HFilterMultiSelect
                v-model="formModel.contentTypes"
                title="内容类型"
                placeholder="请选择"
                :options="contentTypeOptions"
              />
            </div>
          </div>

          <div class="filter-form-item">
            <div class="filter-form-item__label">原声类型</div>
            <div class="filter-form-item__control">
              <HFilterMultiSelect
                v-model="formModel.advertisementType"
                title="原声类型"
                placeholder="请选择"
                :options="adTypeOptions"
              />
            </div>
          </div>

          <div class="filter-form-item">
            <div class="filter-form-item__label">账号类型</div>
            <div class="filter-form-item__control">
              <HFilterMultiSelect
                v-model="formModel.accountTypes"
                title="账号类型"
                placeholder="请选择"
                :options="accountTypeOptions"
              />
            </div>
          </div>
        </div>

        <div class="report-filter-panel__actions">
          <van-button class="report-filter-panel__btn" block @click.prevent="handleReset">
            重置
          </van-button>
          <van-button class="report-filter-panel__btn" block type="primary" native-type="submit">
            查询
          </van-button>
        </div>
      </van-form>

      <van-calendar
        v-model:show="calendarVisible"
        type="range"
        :show-confirm="false"
        :allow-same-day="true"
        :default-date="calendarDefaultDate"
        :max-date="new Date()"
        :min-date="dayjs().subtract(3, 'year').toDate()"
        teleport="body"
        @confirm="handleCalendarConfirm"
      />
    </div>
  </van-popup>
</template>

<style scoped lang="scss">
.report-filter-panel {
  height: min(95vh, 645px);
  padding: 12px 16px calc(12px + env(safe-area-inset-bottom));
  background: #fff;
  display: flex;
  flex-direction: column;
}

.report-filter-panel__loading {
  min-height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.report-filter-panel__form {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.report-filter-panel__body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  -webkit-overflow-scrolling: touch;
  padding: 0 0 12px;
}

.filter-form-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 7px 0;
}

.filter-form-item__label {
  flex: none;
  width: 60px;
  padding-top: 5px;
  color: #1f2733;
  font-size: 12px;
  line-height: 22px;
  text-align: right;
}

.filter-form-item.is-required {
  .filter-form-item__label::before {
    content: '*';
    margin-right: 4px;
    color: #f53f3f;
  }
}

.filter-form-item__control {
  flex: 1;
  min-width: 0;
}

.date-filter {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.date-filter__tabs {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.date-filter__tab {
  height: 24px;
  border: 1px solid transparent;
  border-radius: 2px;
  background: #f8f8f9;
  color: #222229;
  font-size: 12px;
  line-height: 22px;
  text-align: center;
  user-select: none;

  &.is-active {
    border-color: #1677ff;
    background: #eaf3ff;
    color: #1677ff;
  }
}

.date-filter__custom {
  height: 28px;
  padding: 0 10px;
  border: 1px solid #e5e6eb;
  border-radius: 3px;
  color: #1f2733;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  font-size: 12px;
}

.date-filter__custom-text {
  flex: 1;
  min-width: 0;
}

.experience-layout {
  display: grid;
  grid-template-columns: minmax(104px, 128px) minmax(0, 1fr);
  gap: 8px;
  align-items: center;
}

.experience-layout__type {
  width: 100%;
  min-width: 0;
}

.experience-layout__code {
  width: 100%;
  min-width: 0;
}

.experience-layout {
  :deep(.hlss-trigger),
  :deep(.rects-trigger) {
    height: 32px;
  }
}

.report-filter-panel__actions {
  flex: none;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  padding-top: 8px;
}

.report-filter-panel__btn {
  height: 36px;
  border-radius: 2px;

  &:first-child {
    border-color: #f2f3f5;
    background: #f2f3f5;
    color: #4e5969;
  }
}

:deep(.hfms-trigger) {
  height: 28px;
}
</style>
