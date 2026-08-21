<script setup lang="ts">
import { computed, nextTick, reactive, ref, watch } from 'vue'

import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { getSpecialZoneOptions, publishReport } from '@/api/overview'
import type { PublishReportQueryParams } from '@/api/overview/type'
import { useQueryStore, useUserStore } from '@/store'
import { useRoute } from 'vue-router'
import { getFilterConfig } from '@/components/Business/UniversaFilter/helper'
import { getDrillDownConditions, type DrillDownConditionItem } from '@/api/drillDownDialog'
import ExperienceCodeHotSelector from '@/components/Business/UniversaFilter/components/ExperienceCodeHotSelector.vue'
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
import { createHotData, getHotEvDetail, updateHotData } from '@/api/hotAphttp'
import dayjs from 'dayjs'

defineOptions({
  name: 'HotDialogEvents'
})

const visiList = [
  {
    label: '仅自己可见',
    value: '1'
  },
  {
    label: '所有人可见',
    value: '2'
  }
]

const emit = defineEmits<{
  (e: 'updateCallback', data: any): void //新增或者编辑成功回调函数
  (e: 'closeCallback', data: any): void //关闭回调函数
}>()

const { editItem = null } = defineProps<{
  editItem?: any
}>()

const visible = defineModel<boolean>({ default: false })

const queryStore = useQueryStore()
const userStore = useUserStore()
const brandOptions = userStore.getBrandService || []
const middlewareStore = useMiddlewareStore()
// getDictItems

const ruleFormRef = ref<FormInstance>()

const hotDetailData = ref<any>(null) // 详情数据
const loadingDetailLoading = ref(false) // 详情数据加载中
const seriesRefSelector = ref<any>(null)

const tydmObj = ref<any>(null) // 体验代码回显相关的数据

const attributeTagOptions = ref<any[]>([]) // 属性标签选项

const ruleForm = reactive<PublishReportQueryParams>({
  eventName: undefined, // 事件名称
  keywords: undefined, // 关键词
  // 日期范围 默认开始时间是当前日期 结束时间为当天往后推 1 个月
  customRangeTimes: [dayjs().format('YYYY-MM-DD'), dayjs().add(1, 'month').format('YYYY-MM-DD')],

  brandList: undefined, // 品牌
  seriesList: undefined, // 车系

  dataDSource: undefined, // 数据源
  contentDTypes: undefined, // 数据类型
  experienceDCode: undefined, // 客户体验代码
  tagTypeDProp: undefined, // 客户体验代码关联的tagType字段
  standardViewpoint: undefined, // 标准观点

  scenarioAttr: undefined, //  属性标签

  province: undefined, // 省份

  visibility: visiList[0].value // 可见范围
})

const rules = reactive<FormRules<PublishReportQueryParams>>({
  eventName: [
    { required: true, message: '请输入事件名称', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value?.length > 20) {
          callback(new Error('事件名称不能超过 20 个字'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  keywords: [
    { required: true, message: '请输入关键词', trigger: 'blur' },
    // 关键词，最多 3 个，用空格隔开 超过 3 个提示「关键词最多支持输入 3 个，请用空格隔开」
    {
      validator: (rule, value, callback) => {
        const valueArr = value.split(' ')
        // 判断 valueArr 是否重复
        if (valueArr.length > 3) {
          callback(new Error('关键词最多支持输入 3 个，请用空格隔开'))
        } else if (new Set(valueArr).size !== valueArr.length) {
          callback(new Error('关键词不能重复'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})

const resetForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.resetFields()
}

// 表单数据，用于存储查询条件的值
const formData = ref<Record<string, any>>({})
const customTimes = ref<string[]>([]) // 自定义时间范围
const channelOptions = ref<any[]>([]) // 数据源选项
const standardViewpointOptions = ref<any[]>([]) // 标准观点选项
const experienceCodeSelectorRef = ref<any>(null) // 客户体验代码选择器引用

const experienceCodeTypeOptions = ref<Array<{ key: string; value: string }>>([]) // 体验代码类型选项
const calendarChangeData = ref<Array<Date | string | null>>([])

// 根据路由名称获取 UniversaFilter 的配置
const filterConfig = computed(() => {
  return getFilterConfig(route.name as string)
})

const chexiList = ref<any[]>([])

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
      'rootCause',
      'hotEvents'
    ].includes(routeName)
  ) {
    return TagType.Domain
  }
  // 默认返回 Domain
  return TagType.Domain
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
      else if (field.type === 'experienceCode') {
        newFormData[field.prop] = Array.isArray(value) ? value : []
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
        else if (field.type === 'experienceCode') {
          // 体验代码字段不会被缓存，所以这里不需要处理
          // 如果需要从缓存恢复，应该从 UniversaFilter 的 formData 中获取
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

/**
 * @description: 初始化标准观点选项
 * @param {*} tagParentCodes 客户体验代码的末级code数组
 * @return {*}
 */
const initStandardViewpointOptions = async (tagParentCodes?: string[]) => {
  try {
    const routeName = route.name as string
    const isRootCauseOrResultData = ['rootCause', 'ResultData', 'hotEvents'].includes(routeName)

    // 如果有tagParentCodes，传入codes参数
    if (tagParentCodes && tagParentCodes.length > 0) {
      const filteredCodes = tagParentCodes.filter(code => code !== 'all')
      const tagType = getTagLibTypeByRouteName()
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
        const tagType = TagType.Domain
        const res = await findFinalTagLibClientVoListByTagId({
          tagType
        })
        if (res.result && Array.isArray(res.result)) {
          standardViewpointOptions.value = res.result
        } else {
          standardViewpointOptions.value = []
        }
        await nextTick()
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
 * 根据 key 获取指定的下钻筛选条件项
 * @param conditions 下钻筛选条件列表
 * @param key 条件 key
 * @returns 匹配到的条件项
 */
const findDrillDownConditionByKey = (conditions: DrillDownConditionItem[], key: string) => {
  return conditions.find(item => item?.key === key)
}

/**
 * @description: 弹窗打开
 * @return {*}
 */
const handleOpen = async () => {
  // 初始化数据源选项
  await initChannelOptions()

  initAttributeTagOptions()

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
  const experienceCodeField = filterConfig.value.find(field => field.type === 'experienceCode')
  const experienceCodeValue = universaFormData?.[experienceCodeField?.prop || '']

  // 处理初始化另一种 客户体验代码
  const res = await getDrillDownConditions()
  const conditions = Array.isArray(res.result) ? res.result : []
  const tagTypeItem = findDrillDownConditionByKey(conditions, 'tagType')
  const tagTypeDetails = Array.isArray(tagTypeItem?.details) ? tagTypeItem.details : []

  experienceCodeTypeOptions.value = tagTypeDetails
    .map((item: any) => ({ key: item?.key, value: item?.value }))
    .filter((item: any) => item.key && item.value)

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
    // 如果没有客户体验代码，初始化空的标准观点选项
    await initStandardViewpointOptions()
    // 完整初始化formData
    initFormDataFromUniversaFilter()
  }

  // 使用 nextTick 确保 formData 和 brandValue 都已正确设置后再继续
  await nextTick()
}

/**
 * @description: 弹窗关闭
 * @return {*}
 */
const handleClose = () => {
  hotDetailData.value = null
  resetForm(ruleFormRef.value)
  // 重置提交状态
  isSubmitting.value = false
  visible.value = false
  emit('closeCallback', {}) // 关闭弹窗
}

/**
 * @description: 取消
 * @return {*}
 */
const handleCancel = () => {
  handleClose()
}

const route = useRoute()

// 提交状态标志，防止重复提交
const isSubmitting = ref(false)

const submitForm = async (formEl: FormInstance | undefined) => {
  console.log(
    'ruleForm',
    ruleForm,
    '品牌',
    brandOptions,
    '车系',
    seriesRefSelector.value?.getAllOptions()
  )
  if (!formEl) return

  // 如果正在提交，直接返回
  if (isSubmitting.value) {
    return
  }

  await formEl.validate(async valid => {
    if (valid) {
      // 设置提交状态
      isSubmitting.value = true

      // 根据品牌id获取品牌中文名称
      const brandList = ruleForm.brandList
      const blas = brandOptions
        .filter((item: any) => brandList?.includes?.(item.key))
        ?.map((item: any) => item.value)
      // 根据车系id 获取车系中文名称
      const seriesList = ruleForm.seriesList
      const al = seriesRefSelector.value?.getAllOptions()?.options || []
      const slas = al
        ?.filter((item: any) => seriesList?.includes?.(item.key))
        ?.map((item: any) => item.value)

      const cpFormData = cloneDeep(ruleForm)
      try {
        if (hotDetailData.value) {
          // 说明是更新
          const p = {
            ...hotDetailData.value, // 原始数据
            ...cpFormData,
            filterJson: JSON.stringify({
              ...formData.value,
              ...cpFormData,
              tydmObj: tydmObj.value
            }),
            brandList: blas, // 中文 用于列表显示 回显的数据需要去 filterJson 中取
            seriesList: slas, // 中文 用于列表显示  回显的数据需要去 filterJson 中取
            startDate: cpFormData.customRangeTimes?.[0],
            endDate: cpFormData.customRangeTimes?.[1]
          }
          const response = await updateHotData(p)
          if (response.success) {
            ElMessage.success('修改成功')
            emit('updateCallback', response) // 编辑成功
            handleClose()
          } else {
            ElMessage.error(response.message)
          }
        } else {
          // 说明是新增
          // filterJson customRangeTimes
          const p = {
            ...cpFormData,
            filterJson: JSON.stringify({ ...cpFormData, tydmObj: tydmObj.value }),
            brandList: blas, // 中文 用于列表显示 回显的数据需要去 filterJson 中取
            seriesList: slas, // 中文 用于列表显示  回显的数据需要去 filterJson 中取
            startDate: cpFormData.customRangeTimes?.[0],
            endDate: cpFormData.customRangeTimes?.[1]
          }

          const response = await createHotData(p)
          if (response.success) {
            ElMessage.success('创建成功')
            emit('updateCallback', response) // 编辑成功
            handleClose()
          } else {
            ElMessage.error(response.message)
          }
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

/**
 * @description: 确认按钮（使用防抖处理）
 * @return {*}
 */
const handleConfirm = debounce(() => {
  submitForm(ruleFormRef.value)
}, 300)

/**
 * 监听日期面板的临时选中值，供禁用逻辑判断跨度使用。
 * @param val 当前面板中正在选择的起止日期
 */
const handleCalendarChange = (val: [Date | string, Date | string | null] | null) => {
  calendarChangeData.value = val || []
}
/**
 * 禁用未来日期，并限制用户手动选择的日期跨度最长为 365 天。
 * @param date 当前待渲染的日期
 * @returns `true` 表示该日期不可选
 */
const disabledDate = (date: Date) => {
  // 开始日期不得晚于结束日期，跨度最长 365 天
  const [start, end] = calendarChangeData.value || []
  if (!start) return false
  const diffDays = Math.abs(dayjs(start).diff(dayjs(date), 'day'))
  return diffDays > 365
}

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

// 目前不知道这个监听有什么用 但是如果不加 车系多选显示异常
watch(
  () => ruleForm.brandList,
  val => {
    const findBrand = brandOptions.find((brand: any) => brand.key === val)
    // console.log('品牌值变化', val, findBrand)
    if (findBrand) {
      chexiList.value = findBrand.children || []
    } else {
      chexiList.value = []
    }
  },
  { immediate: true, deep: true }
)

// 深度监听 brandOptions 的变化，确保 brandValue 能正确更新

watch(
  () => brandOptions,
  (newValue, oldValue) => {
    // console.log('深度监听 brandOptions 的变化', newValue)

    if (newValue && newValue.length > 0) {
      //设置品牌默认值 第一个
      ruleForm.brandList = [newValue[0].key]
      // 设置车系的选项
      chexiList.value = newValue[0].children
    }
  },
  { immediate: true, deep: true }
)

// 监听
watch(
  () => [editItem, visible.value],
  async (newValArr: any) => {
    const newVal = newValArr[0]
    if (visible.value) {
      if (newVal) {
        // 如果存在值 那就是编辑得初始化
        // 获取详情
        loadingDetailLoading.value = true
        getHotEvDetail({ id: newVal?.id })
          .then((res: any) => {
            hotDetailData.value = res?.result
            // 开始初始化每一项的值

            const filterJsonStr = newVal?.filterJson

            let filterJson: any = {}
            if (filterJsonStr) {
              try {
                filterJson = JSON.parse(filterJsonStr)
              } catch (error) {
                //
              }
            }
            console.log('编辑详情数据', filterJson)
            ruleForm.eventName = filterJson?.eventName
            ruleForm.keywords = filterJson?.keywords
            ruleForm.customRangeTimes = filterJson?.customRangeTimes
            ruleForm.brandList = filterJson?.brandList
            ruleForm.seriesList = filterJson?.seriesList
            ruleForm.dataDSource = filterJson?.dataDSource
            ruleForm.contentDTypes = filterJson?.contentDTypes
            ruleForm.experienceDCode = filterJson?.experienceDCode
            ruleForm.tagTypeDProp = filterJson?.tagTypeDProp
            ruleForm.standardViewpoint = filterJson?.standardViewpoint
            ruleForm.scenarioAttr = filterJson?.scenarioAttr
            ruleForm.province = filterJson?.province
            ruleForm.visibility = filterJson?.visibility
            tydmObj.value = filterJson?.tydmObj
          })
          .finally(() => {
            loadingDetailLoading.value = false
          })
      } else {
        hotDetailData.value = null
        // 全部清空初始化
        ruleForm.eventName = undefined
        ruleForm.keywords = undefined
        // ruleForm.customRangeTimes = undefined
        // ruleForm.brandList = undefined
        // ruleForm.seriesList = undefined
        // ruleForm.dataDSource = undefined
        // ruleForm.contentDTypes = undefined
        // ruleForm.experienceDCode = undefined
        // ruleForm.tagTypeDProp = undefined
        // ruleForm.standardViewpoint = undefined
        // ruleForm.province = undefined
        // ruleForm.visibility = visiList[0].value
      }
    }
  },
  { immediate: true, deep: true }
)
</script>

<template>
  <Teleport to="body">
    <div v-if="visible" class="public-report">
      <div class="pr-wrap">
        <div class="pr-header">
          <div class="pr-title">{{ editItem ? '编辑热点事件' : '创建热点事件' }}</div>
          <div class="pr-close" @click="handleClose">
            <el-icon :size="24"><Close /></el-icon>
          </div>
        </div>
        <div class="pr-content">
          <el-form ref="ruleFormRef" :model="ruleForm" :rules="rules" label-width="auto">
            <el-form-item label="事件名称" prop="eventName">
              <el-input
                v-model="ruleForm.eventName"
                placeholder="请输入事件名称，如“智行汽车舆情监测”"
              />
            </el-form-item>
            <el-form-item label="关键词" prop="keywords">
              <el-input
                v-model="ruleForm.keywords"
                placeholder="请输入关键词，最多 3 个，用空格隔开"
              />
            </el-form-item>
            <!-- 日期范围 -->
            <el-form-item label="日期范围" prop="customRangeTimes">
              <el-date-picker
                v-model="ruleForm.customRangeTimes"
                type="daterange"
                placeholder="自定义"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                format="YYYY.MM.DD"
                value-format="YYYY-MM-DD"
                :clearable="false"
                :disabled-date="disabledDate"
                @calendar-change="handleCalendarChange"
                style="width: 50%"
              />
              <span>跨度最长可选 1 年，建议控制在 1 个月内</span>
            </el-form-item>

            <!-- 品牌 -->
            <el-form-item label="品牌" prop="brandList">
              <BrandSelector
                v-model="ruleForm.brandList"
                :options="brandOptions || []"
                :multiple="true"
                :atLeastOne="true"
              />
            </el-form-item>

            <!-- 车系 -->
            <el-form-item label="车系" prop="seriesList">
              <SeriesSelector
                ref="seriesRefSelector"
                v-model="ruleForm.seriesList"
                :brand-value="ruleForm.brandList"
                :options="chexiList"
              />
            </el-form-item>
            <el-row :gutter="24">
              <template v-for="(field, index) in filterConfig" :key="field.prop || index">
                <!-- 跳过占位符 -->
                <template v-if="field.type !== 'placeholder'">
                  <!-- 数据源 -->
                  <el-col v-if="field.type === 'dataSource'" :span="24">
                    <el-form-item :label="field.label">
                      <DataSourceCascader
                        v-model="ruleForm.dataDSource"
                        :options="channelOptions"
                        :condition="{ multiSelect: field.multiple ?? true }"
                        :child-key="field.prop"
                        :wait-for-parent="true"
                        class="w-full"
                      />
                    </el-form-item>
                  </el-col>

                  <!-- 数据类型 -->
                  <el-col
                    v-else-if="field.type === 'select' && field.prop === 'contentTypes'"
                    :span="24"
                  >
                    <el-form-item :label="field.label">
                      <el-select
                        v-model="ruleForm.contentDTypes"
                        :options="field.options"
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

                  <!-- 客户体验代码 -->
                  <el-col
                    v-else-if="field.type === 'experienceCodeLinkage' && !loadingDetailLoading"
                    :span="24"
                  >
                    <el-form-item :label="field.label">
                      <ExperienceCodeHotSelector
                        ref="experienceCodeSelectorRef"
                        v-model="ruleForm.experienceDCode"
                        v-model:tagType="ruleForm.tagTypeDProp"
                        :type-options="experienceCodeTypeOptions"
                        :default-tag-type="'CA'"
                        @change="handleExperienceCodeChange"
                      />
                    </el-form-item>
                  </el-col>
                  <!-- 标准观点 -->
                  <el-col v-else-if="field.type === 'selectv2'" :span="24">
                    <el-form-item :label="field.label">
                      <SelectV2WithSelectAll
                        v-if="field.showSelectAll && field.prop === 'topicCodes'"
                        v-model="ruleForm.standardViewpoint"
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
                    </el-form-item>
                  </el-col>
                  <!-- 属性标签 -->
                  <el-col v-else-if="field.prop === 'scenarioAttr'" :span="24">
                    <el-form-item :label="field.label">
                      <el-select
                        v-model="ruleForm.scenarioAttr"
                        :options="attributeTagOptions"
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
                  <!-- 省份 -->
                  <el-col
                    v-else-if="field.type === 'select' && field.prop === 'custProvinceCodeSet'"
                    :span="24"
                  >
                    <el-form-item :label="field.label">
                      <el-select
                        v-model="ruleForm.province"
                        :options="field.options"
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
                </template>
              </template>
            </el-row>
            <!-- 可见范围 -->
            <el-form-item label="可见范围" prop="visibility">
              <el-radio-group v-model="ruleForm.visibility">
                <el-radio
                  v-for="item in visiList"
                  :key="item.value"
                  :value="item.value"
                  size="large"
                >
                  {{ item.label }}
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-form>
        </div>
        <div class="pr-footer">
          <div class="cancel cursor-point" @click="handleCancel()">取消</div>
          <div
            class="confirm cursor-point"
            :class="{ 'is-disabled': isSubmitting }"
            @click="handleConfirm()"
          >
            {{
              isSubmitting
                ? editItem
                  ? '修改中...'
                  : '创建中...'
                : editItem
                  ? '编辑事件'
                  : '创建事件'
            }}
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
    width: 900px;
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
