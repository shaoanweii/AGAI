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
  getReceiversUserList,
  getMainAccTreeData
} from '@/api/common'
import { TagType, PeriodType, SendRuleType } from '@/constants'
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
import {
  getQueryDataByid,
  createSubscribeReport,
  getSubscribeTaskDetail,
  updateSubscribeTaskList
} from '@/api/subscribeReport'
import dayjs from 'dayjs'
import { groupBy } from 'lodash-es'
import { userInfo } from '@/api/main'

defineOptions({
  name: 'SubscribeReport'
})

const emit = defineEmits<{
  (e: 'updateCallback', data: any): void //新增或者编辑成功回调函数
  (e: 'closeCallback', data: any): void //新增或者编辑成功回调函数
}>()

const { editItem = null, tydmArrObj = null } = defineProps<{
  editItem?: any
  tydmArrObj?: any
}>()
const visible = defineModel<boolean>({ default: false })

const tydmObj = ref<any>(null) // 体验代码回显相关的数据

const queryStore = useQueryStore()
const userStore = useUserStore()
const middlewareStore = useMiddlewareStore()
// getDictItems

const ruleFormRef = ref<FormInstance>()
const receiverCascaderRef = ref() // 人员树ref
// 选择的人员分组列表显示
const checkedUserGroupData = ref<any>({})
// 是否显示选择人员分组
const showUserGroup = ref<boolean>(false)

const userDatainfo = ref<any>({})

const editDetail = ref<any>(null)

// 提示待完成项目
const waitTodo = ref<any>([])

const reportQueryInfo = ref<any>(null) // 报告的查询条件

// 订阅周期选择按钮
const subscribeperiodOptions = ref<any>([
  { label: '30天', value: [dayjs(), dayjs().add(30, 'day')], type: PeriodType.Day30 },
  { label: '90天', value: [dayjs(), dayjs().add(90, 'day')], type: PeriodType.Day90 },
  { label: '180天', value: [dayjs(), dayjs().add(180, 'day')], type: PeriodType.Day180 },
  { label: '自定义', value: undefined, type: PeriodType.Custom }
])

// 发送规则选择按钮
const sendRuleOptions = ref<any>([
  { label: '每天', value: SendRuleType.Daily },
  { label: '每周', value: SendRuleType.Weekly },
  { label: '每月', value: SendRuleType.Monthly }
])

// 周一到周末
const weekOptions = ref<any>([
  { label: '周一', value: 1 },
  { label: '周二', value: 2 },
  { label: '周三', value: 3 },
  { label: '周四', value: 4 },
  { label: '周五', value: 5 },
  { label: '周六', value: 6 },
  { label: '周日', value: 7 }
])

// 1-28号
const monthDayOptions = ref<any>(
  Array.from({ length: 28 }, (_, i) => ({ label: `${i + 1}号`, value: i + 1 }))
)

const ruleForm = reactive<any>({
  taskName: undefined,
  periodType: subscribeperiodOptions.value[1].type, //订阅周期选择的类型
  timePeriodRange: subscribeperiodOptions.value[1].value, //订阅周期开始结束时间 //startDate endDate
  sendRule: sendRuleOptions.value[0].value, // 发送规则 类型
  sendDay: undefined, // 发送日（每周周几或每月几号）
  // 初始化当前时间的09时00分00秒
  sendDatetime: dayjs().hour(9).minute(0).second(0).format('HH:mm'), // 发送规则-发送时间
  receiverIds: undefined, // 接收人
  receiveChannel: ['1', '2'] // 接收渠道
})

const rules = reactive<FormRules<PublishReportQueryParams>>({
  taskName: [
    { required: true, message: '请输入报告名称', trigger: 'blur' },
    { min: 4, max: 50, message: '字数限制4-50', trigger: 'blur' }
  ],
  sendRule: [{ required: true, message: '请选择发送规则', trigger: 'change' }],
  receiverIds: [{ required: true, message: '请至少选择1位接收人', trigger: 'change' }],
  timePeriodRange: [{ required: true, message: '请选择订阅周期', trigger: 'blur' }],
  receiveChannel: [{ required: true, message: '请选择接收渠道', trigger: 'change' }]
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

const receiversUserOptions = ref<any[]>([]) // 接收人选项
const standardViewpointOptions = ref<any[]>([]) // 标准观点选项
const experienceCodeSelectorRef = ref<any>(null) // 客户体验代码选择器引用

// 竞品对比相关数据
const isCompetitorAnalysis = computed(() => route.name === 'competitorAnalysis')
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
      'rootCause'
    ].includes(routeName)
  ) {
    return TagType.Domain
  }
  // 默认返回 Domain
  return TagType.Domain
}

// 发送规则改变事件处理
const handleSendRuleChange = (item: any) => {
  // 如果选择了每周，默认设置发送日为周一
  if (item.value === SendRuleType.Weekly) {
    // 设置周的发送日 默认第一个
    ruleForm.sendDay = weekOptions.value[0].value
  }
  // 如果选择了每月，默认设置发送日为每月1号
  else if (item.value === SendRuleType.Monthly) {
    ruleForm.sendDay = monthDayOptions.value[0].value
  } else {
    ruleForm.sendDay = undefined
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

const initReceiversUserOptions = async () => {
  try {
    const res = await getReceiversUserList()
    receiversUserOptions.value = res.result || []
  } catch (error) {
    console.error('获取接收人选项失败:', error)
    receiversUserOptions.value = []
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
    const isRootCauseOrResultData = ['rootCause', 'ResultData'].includes(routeName)

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
 * @description: 弹窗打开
 * @return {*}
 */
const handleOpen = async () => {
  // 初始化数据源选项
  await initChannelOptions()
  await initMainAccTreeOptions()
  await initReceiversUserOptions()

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
  // 重置提交状态
  isSubmitting.value = false
  visible.value = false
  waitTodo.value = []
  emit('closeCallback', {}) // 关闭事件
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
  console.log('ruleForm', ruleForm)

  if (!formEl) return

  // 如果正在提交，直接返回
  if (isSubmitting.value) {
    return
  }

  await formEl.validate(async (valid, invalidFields) => {
    if (valid) {
      waitTodo.value = []
      // 设置提交状态
      isSubmitting.value = true

      const timePeriodRange = ruleForm.timePeriodRange
      let startDate = ''
      let endDate = ''
      if (timePeriodRange) {
        startDate = dayjs(timePeriodRange[0]).format('YYYY-MM-DD')
        endDate = dayjs(timePeriodRange[1]).format('YYYY-MM-DD')
      }
      // sendDatetime 需要转换成字符串格式的时间
      // const sendDatetime = dayjs(ruleForm.sendDatetime).format('HH:mm')

      // 处理接收人 转换成json字符串
      const receiverIds = JSON.stringify(ruleForm.receiverIds)
      // 处理接收渠道 转换成json字符串
      const receiveChannel = JSON.stringify(ruleForm.receiveChannel)

      const createTime = dayjs().format('YYYY-MM-DD HH:mm:ss')
      const { employeeId, name } = userDatainfo.value

      try {
        if (editDetail.value) {
          // 说明是编辑
          const upParams = {
            ...editDetail.value,
            ...ruleForm,
            startDate,
            endDate,
            // sendDatetime,
            receiverIds,
            receiveChannel
          }
          delete upParams.timePeriodRange
          const response = await updateSubscribeTaskList(upParams)
          if (response.success) {
            ElMessage.success('修改订阅成功')
            emit('updateCallback', response) // 编辑成功
            handleClose()
          } else {
            ElMessage.error(response.message)
          }
        } else {
          // 其他参数
          let otherParams: any = {}
          const reportJudgeId = route.query.reportJudgeId
          if (reportJudgeId) {
            // 从报告页面跳转过来 新增的订阅任务
            otherParams = {
              sendReportId: reportJudgeId,
              taskFilterJson: reportQueryInfo.value
            }
          } else {
            // 说明是每个独立页面创建订阅任务 读取本地的查询参数 构建一个和发布一样的参数实体 代码直接拷贝
            const cpFormData = cloneDeep(formData.value)
            // 处理数据
            if (newCarSeriesSelector.value?.length) {
              cpFormData.newCarSeriesObjList = newCarSeriesSelector.value
            } else {
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
            // 热点事件详情页面订阅 处理一下字段
            if (route.name === 'hotDetailEvents') {
              cpFormData.keywords = tydmArrObj?.keywords
              cpFormData.dateRange = 'custom'
              // 存储当前的tab页面 是结果数据还是原始数据
              cpFormData.hotDetailPageType = middlewareStore.originalDataType
            }
            cpFormData.tydmObj = tydmObj.value
            console.log('订阅数据', cpFormData)

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

            otherParams = {
              taskFilterJson: JSON.stringify(conditionData)
            }
          }
          const params = {
            ...ruleForm,
            ...otherParams,
            startDate,
            endDate,
            // sendDatetime,
            receiverIds,
            receiveChannel,
            sourceModule: route.meta.title, // 当前模块名称
            pagePath: window.location.href,
            creatorId: employeeId,
            creatorName: name,
            createTime
          }
          delete params.timePeriodRange
          // 说明是新增
          const response = await createSubscribeReport(params)
          if (response.success) {
            ElMessage.success('订阅成功')
            handleClose()
          } else {
            ElMessage.error(response.message)
          }
        }

        handleClose()
      } catch (error: any) {
        ElMessage.error(error.message)
      } finally {
        // 无论成功或失败，都要重置提交状态
        isSubmitting.value = false
      }
    } else {
      const map: any = {
        taskName: '报告名称',
        timePeriodRange: '订阅周期',
        receiverIds: '接收人',
        receiveChannel: '接收渠道'
      }
      if (invalidFields) {
        const xxx = Object.keys(invalidFields).map((key: any) => map[key])
        waitTodo.value = xxx
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

// 选择类型改变
const handleRadioChange = (item: any) => {
  ruleForm.timePeriodRange = item.value
}

// 周期禁用逻辑
const disabledPeriodDate = (date: Date) => {
  const today = dayjs()
  // 大于今天的时间禁用
  if (dayjs(date).isBefore(today)) return true

  return false
}

// 周期变化事件
const handlePeriodCalendarChange = (val: Array<Date | null>) => {
  const [start, end] = val || []
  if (start && !end) {
    // [dayjs(start).format('YYYY-MM-DD'), dayjs(start).format('YYYY-MM-DD')]
    return
  }
}

// 订阅周期时间范围变化事件
const handlePeriodTimeRangeChange = async (val: [string, string]) => {
  if (!val || val.length !== 2) return
}

const userCascaderChange = (value: any) => {
  // 获取所有已选节点的详细信息
  const checkedNodes = receiverCascaderRef.value.getCheckedNodes()
  // checkedNodes 是一个数组，每项是选中路径的节点对象数组
  // 例如：checkedNodes[0].pathNodes 得到该选项的所有上级节点对象
  // checkedNodes.forEach((node: any) => {
  //   console.log('完整路径节点:', node.pathNodes)
  // })
  const filterMatchNode = checkedNodes.filter((nodes: any) =>
    nodes?.pathNodes?.find((x: any) => value.includes(x?.value))
  )
  // 处理成数组对象数据
  const checkedNodesData = filterMatchNode.map((nodes: any) => {
    const pathLabels = nodes.pathLabels
    // 取pathLabels数组的0到pathLabels.length-1项（即去掉最后一个），用斜杠连接成字符串，作为部门层级显示
    const pathParentStr = pathLabels.slice(0, pathLabels.length - 1).join(' / ')
    return {
      ...(nodes.data || {}),
      pathParentStr
    }
  })
  // 使用groupBy函数对列表checkedNodesData 的每个pathParentStr进行分组
  const groupData = groupBy(checkedNodesData, 'pathParentStr')
  checkedUserGroupData.value = groupData
}

const lookViewList = (type: number) => {
  if (type == 1) {
    showUserGroup.value = true
  } else if (type == 2) {
    showUserGroup.value = false
  }
}

const clearSelectUser = () => {
  checkedUserGroupData.value = []
  ruleForm.receiverIds = undefined
}

const cancelUserSelect = (item: any) => {
  const f = ruleForm?.receiverIds?.filter((i: any) => i !== item.id)
  ruleForm.receiverIds = f
  // 等待页面刷新完成
  nextTick(() => {
    userCascaderChange(f)
  })
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

watch(
  () => tydmArrObj,
  async (newVal: any) => {
    tydmObj.value = newVal?.tydmObj
  },
  { immediate: true, deep: true }
)

// 监听 options 变化，如果父组件传入了 options 且数据已加载完成
watch(
  () => editItem,
  async (newVal: any) => {
    if (newVal) {
      // 如果存在值 那就是编辑得初始化

      const detailRes = await getSubscribeTaskDetail(newVal.id)
      if (detailRes.code === 200) {
        const detailSubscribe = detailRes.result || {}
        editDetail.value = {
          ...detailSubscribe
        }

        // 根绝id查询详情
        ruleForm.taskName = detailSubscribe.taskName
        const startDay = dayjs(detailSubscribe.startDate)
        const endDay = dayjs(detailSubscribe.endDate)
        // 计算 endDay 和 startDay 的差值
        const diffDays = endDay.diff(startDay, 'day')
        // 判断是30  90  180 还是自定义
        if (diffDays === 30) {
          ruleForm.periodType = PeriodType.Day30
        } else if (diffDays === 90) {
          ruleForm.periodType = PeriodType.Day90
        } else if (diffDays === 180) {
          ruleForm.periodType = PeriodType.Day180
        } else {
          ruleForm.periodType = PeriodType.Custom
        }

        ruleForm.timePeriodRange =
          detailSubscribe.startDate && detailSubscribe.endDate ? [startDay, endDay] : undefined
        ruleForm.sendRule = detailSubscribe.sendRule
        ruleForm.sendDay = detailSubscribe.sendDay
        ruleForm.sendDatetime = detailSubscribe.sendDatetime
        ruleForm.receiverIds = detailSubscribe.receiverIds
          ? JSON.parse(detailSubscribe.receiverIds)
          : undefined
        ruleForm.receiveChannel = detailSubscribe.receiveChannel
          ? JSON.parse(detailSubscribe.receiveChannel)
          : undefined
      }
    }
  },
  { immediate: true, deep: true }
)

onMounted(() => {
  // 组件挂载时，如果visible为true，执行打开逻辑
  userInfo().then((res: any) => {
    if (res.code === 200) {
      userDatainfo.value = res.result
    }
  })
  // 判断route是不是带有  reportJudgeId 说明是报告跳转过来的
  const reportJudgeId = route.query.reportJudgeId
  if (reportJudgeId) {
    // 获取查询参数 创建订阅的时候插入数据库
    getQueryDataByid({
      reportId: reportJudgeId
    }).then((res: any) => {
      if (res.success) {
        const filterStr = res.result?.filter
        reportQueryInfo.value = filterStr
      }
    })
  }
})
</script>

<template>
  <Teleport to="body">
    <div v-if="visible" class="public-subscribe">
      <div class="pr-wrap">
        <div class="pr-header">
          <div class="pr-title">订阅报告</div>
          <div class="pr-close" @click="handleClose">
            <el-icon :size="24"><Close /></el-icon>
          </div>
        </div>
        <div class="pr-content">
          <el-form ref="ruleFormRef" :model="ruleForm" :rules="rules" label-width="auto">
            <el-form-item label="报告名称" prop="taskName">
              <el-input v-model="ruleForm.taskName" />
            </el-form-item>
            <!-- 筛选条件 目前不需要 -->
            <!-- 订阅周期 -->
            <el-form-item label="订阅周期" prop="timePeriodRange">
              <!-- 30天 90天 180天 自定义按钮 -->
              <el-radio-group v-model="ruleForm.periodType">
                <el-radio-button
                  v-for="item in subscribeperiodOptions"
                  :value="item.type"
                  :key="item.type"
                  @change="() => handleRadioChange(item)"
                >
                  {{ item.label }}
                </el-radio-button>
              </el-radio-group>
              <!--
              @change="handlePeriodTimeRangeChange"
              @calendar-change="handlePeriodCalendarChange"
              -->
              <el-row :gutter="24" style="width: 100%; margin-left: 0px">
                <el-date-picker
                  v-model="ruleForm.timePeriodRange"
                  type="daterange"
                  range-separator="-"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  format="YYYY/MM/DD"
                  value-format="YYYY-MM-DD"
                  :clearable="false"
                  :disabled-date="disabledPeriodDate"
                  style="margin-top: 6px"
                  :disabled="![PeriodType.Custom].includes(ruleForm.periodType)"
                />
              </el-row>
            </el-form-item>
            <!-- 发送规则 -->
            <el-form-item label="发送规则" prop="sendRule">
              <el-radio-group v-model="ruleForm.sendRule" style="width: 100%">
                <el-radio-button
                  v-for="item in sendRuleOptions"
                  :value="item.value"
                  :key="item.value"
                  @change="handleSendRuleChange(item)"
                >
                  {{ item.label }}
                </el-radio-button>
              </el-radio-group>

              <!-- 一行显示 发送时间 -->
              <el-row :gutter="24" class="mt-6" style="width: 100%">
                <el-col :span="12" v-if="ruleForm.sendRule === SendRuleType.Weekly">
                  <div class="sendRowText">发送日</div>
                  <el-select
                    v-model="ruleForm.sendDay"
                    placeholder="请选择"
                    style="margin-top: 6px"
                  >
                    <el-option
                      v-for="item in weekOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </el-col>
                <el-col :span="12" v-if="ruleForm.sendRule === SendRuleType.Monthly">
                  <div class="sendRowText">发送日</div>
                  <el-select
                    v-model="ruleForm.sendDay"
                    placeholder="请选择"
                    style="margin-top: 6px"
                  >
                    <el-option
                      v-for="item in monthDayOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </el-col>
                <el-col :span="12">
                  <div class="sendRowText">发送时间</div>
                  <el-time-picker
                    v-model="ruleForm.sendDatetime"
                    placeholder="请选择"
                    style="margin-top: 6px"
                    :clearable="false"
                    value-format="HH:mm"
                    format="HH:mm"
                  />
                </el-col>
              </el-row>
            </el-form-item>

            <!-- 接收人 -->
            <el-form-item label="接收人" prop="receiverIds">
              <div class="receivers-box">
                <!-- 顶部文本显示以及操作按钮 -->
                <div class="top-box">
                  <!-- 左侧布局 -->
                  <div class="top-left-ui">
                    <div>已选{{ ruleForm.receiverIds?.length || 0 }}人</div>
                    <div>可通过部门层级或搜索快速添加接收人。</div>
                  </div>
                  <!-- 右侧操作按钮 -->
                  <div class="top-right-ui">
                    <el-button
                      v-if="!showUserGroup"
                      type="primary"
                      size="small"
                      @click="() => lookViewList(1)"
                      >查看已选</el-button
                    >
                    <el-button v-else type="primary" size="small" @click="() => lookViewList(2)"
                      >收起已选</el-button
                    >
                    <el-button type="primary" size="small" @click="clearSelectUser">清空</el-button>
                  </div>
                </div>
                <!-- 选择框 -->
                <div class="select-box">
                  <el-cascader
                    ref="receiverCascaderRef"
                    v-model="ruleForm.receiverIds"
                    :options="receiversUserOptions"
                    collapse-tags
                    :max-collapse-tags="1"
                    placeholder="搜索部门或人员"
                    filterable
                    clearable
                    :props="{
                      value: 'id',
                      label: 'name',
                      children: 'children',
                      multiple: true,
                      emitPath: false, // 在选中节点改变时，是否返回由该节点所在的各级菜单的值所组成的数组，若设置 false，则只返回该节点的值
                      checkStrictly: false
                    }"
                    style="width: 100%"
                    @change="userCascaderChange"
                  />
                </div>
                <div
                  v-if="showUserGroup && Object.keys(checkedUserGroupData).length === 0"
                  class="empty-user-list"
                >
                  <div class="inner">当前还没有已选接收人，先从下方组织结构中添加。</div>
                </div>
                <!-- 列表展示 -->
                <div
                  class="list-show-box"
                  v-else-if="showUserGroup && Object.keys(checkedUserGroupData).length > 0"
                >
                  <div class="card" v-for="(list, key) in checkedUserGroupData" :key="key">
                    <!-- 分组标题 -->
                    <div class="c-t">{{ key }}</div>
                    <!-- 标签 -->
                    <div class="c-tag">
                      <el-tag
                        type="info"
                        class="receiver-tag"
                        v-for="(item, index) in list"
                        :key="index"
                        >{{ item.name }}
                        <span @click="() => cancelUserSelect(item)">x</span></el-tag
                      >
                    </div>
                  </div>
                </div>
              </div>
            </el-form-item>
            <!-- 接收渠道 -->
            <el-form-item label="接收渠道" prop="receiveChannel">
              <el-checkbox-group v-model="ruleForm.receiveChannel" size="large">
                <el-checkbox label="邮件" value="2" border />
                <el-checkbox label="站内通知" value="1" border />
              </el-checkbox-group>
            </el-form-item>
          </el-form>
        </div>
        <div class="pr-footer">
          <div class="tit" v-if="waitTodo.length === 0">请完成配置后确认订阅。</div>
          <div class="tit" v-if="waitTodo.length !== 0">
            还有未完成项：{{ waitTodo.join(', ') }}
          </div>
          <div class="cancel cursor-point" @click="handleCancel()">取消</div>
          <div
            class="confirm cursor-point"
            :class="{ 'is-disabled': isSubmitting }"
            @click="handleConfirm()"
          >
            {{ isSubmitting ? '订阅中...' : '确认订阅' }}
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

.public-subscribe {
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

  // 接收人布局
  .receivers-box {
    width: 100%;
    background: #f7f9fc;
    padding: 10px;
    .top-box {
      display: flex;
      width: 100%;
      height: 50px;
      .top-left-ui {
        width: 70%;
        display: flex;
        flex-direction: column;
        justify-content: center;
      }
      .top-right-ui {
        width: 30%;
        display: flex;
        justify-content: flex-end;
        align-items: center;
      }
    }
    .empty-user-list {
      width: 100%;
      height: 90px;
      padding: 10px;

      margin-top: 20px;
      background: #fff;
      .inner {
        width: 100%;
        height: 100%;
        background: #f4f7fb;
        font-size: 14px;
        color: #6f7d91;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 4px;
      }
    }
    .list-show-box {
      background: #fff;
      padding: 10px;
      width: 100%;
      margin-top: 20px;
      .card {
        padding: 10px;
        width: 100%;
        background: #f9fbfd;
        margin-bottom: 10px;
        .c-t {
          font-weight: 500;
          font-size: 14px;
          color: #1d2129;
        }
        .c-tag {
          margin-top: 8px;
          cursor: pointer;
          .receiver-tag {
            margin-right: 8px;
          }
        }
      }
    }
  }

  .pr-wrap {
    width: 880px;
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
      .tit {
        flex: 3;
        color: #c2464d;
        height: 100%;
        display: flex;
        margin-top: 10px;
      }
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
