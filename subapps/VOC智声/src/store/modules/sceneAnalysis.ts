import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getShortcutDateRange, getTimeDimensionByCode } from '@/utils/date'
import { useMiddlewareStore } from '@/store/modules/middleware'

interface SceneOriginState {
  isDetail: boolean
  reportId: string
  hasValidCondition: boolean
  formData: Record<string, any>
  customTimes: string[]
  reportName: string
}

interface SceneOriginData {
  id?: unknown
  reportId?: unknown
  isDetail?: boolean
  defaultCondition?: unknown
  reportName?: unknown
}

interface ReportDefaultCondition {
  formData: Record<string, any>
  customTimes: string[]
  competitorAnalysis?: Record<string, any>
  valid: boolean
}

/**
 * 创建场景详情上下文的默认状态。
 * @returns 空的场景详情上下文
 */
const createDefaultSceneOriginData = (): SceneOriginState => ({
  isDetail: false,
  reportId: '',
  hasValidCondition: false,
  formData: {},
  customTimes: [],
  reportName: ''
})

/**
 * 将未知值归一化为去除首尾空白的字符串。
 * @param value 待归一化值
 * @returns 可用于展示或比较的字符串
 */
const normalizeString = (value: unknown): string => {
  if (Array.isArray(value)) {
    return normalizeString(value[0])
  }

  if (typeof value === 'number') {
    return String(value)
  }

  return typeof value === 'string' ? value.trim() : ''
}

/**
 * 解析已发布报告的默认条件。
 * @param defaultCondition 后端保存的 JSON 字符串
 * @param strict 是否在格式异常时抛出错误
 * @returns 可供筛选器恢复的结构化条件
 */
const parseDefaultCondition = (
  defaultCondition: unknown,
  strict: boolean
): ReportDefaultCondition => {
  if (!defaultCondition) {
    if (strict) {
      throw new Error('报告未配置默认条件')
    }
    return { formData: {}, customTimes: [], valid: false }
  }

  try {
    const parsed =
      typeof defaultCondition === 'string' ? JSON.parse(defaultCondition) : defaultCondition

    if (
      !parsed ||
      typeof parsed !== 'object' ||
      Array.isArray(parsed) ||
      !parsed.formData ||
      typeof parsed.formData !== 'object' ||
      Array.isArray(parsed.formData)
    ) {
      throw new Error('报告默认条件格式不正确')
    }

    const customTimes = parsed.customTimes ?? []
    if (!Array.isArray(customTimes) || ![0, 2].includes(customTimes.length)) {
      throw new Error('报告自定义时间格式不正确')
    }
    if (parsed.formData.dateRange === 'custom' && customTimes.length !== 2) {
      throw new Error('报告缺少完整的自定义时间范围')
    }

    return {
      formData: parsed.formData,
      customTimes,
      competitorAnalysis:
        parsed.competitorAnalysis && typeof parsed.competitorAnalysis === 'object'
          ? parsed.competitorAnalysis
          : undefined,
      valid: true
    }
  } catch (error) {
    if (strict) {
      throw error instanceof Error ? error : new Error('报告默认条件解析失败')
    }

    console.error('解析 defaultCondition 失败:', error)
    return { formData: {}, customTimes: [], valid: false }
  }
}

/**
 * 处理场景分析页面跳转到场景详情页逻辑。
 */
export const useSceneAnalysisStore = defineStore('sceneAnalysis', () => {
  const sceneOriginData = ref<SceneOriginState>(createDefaultSceneOriginData())

  /**
   * 恢复报告中的竞品和新车上市专属条件。
   * @param condition 已解析的报告默认条件
   */
  const restoreSpecialReportCondition = async (condition: ReportDefaultCondition) => {
    const originalFormData = condition.formData
    const competitorData = condition.competitorAnalysis

    if (competitorData) {
      const middlewareStore = useMiddlewareStore()
      const { useQueryStore } = await import('@/store/modules/query')
      const queryStore = useQueryStore()

      if (competitorData.queryType) {
        middlewareStore.setBrandServiceCategoryType(competitorData.queryType)
      }

      queryStore.setCompetitorAnalysisData({
        queryType: competitorData.queryType,
        firstSelectedCode: competitorData.firstSelectedCode,
        secondSelectedCode: competitorData.secondSelectedCode,
        firstSelectedName: competitorData.firstSelectedName,
        secondSelectedName: competitorData.secondSelectedName
      })
    }

    if (
      Array.isArray(originalFormData.newCarSeriesObjList) ||
      Array.isArray(originalFormData.compCarSeriesObjList)
    ) {
      const { useQueryStore } = await import('@/store/modules/query')
      const queryStore = useQueryStore()

      queryStore.updateQueryParams({
        ...(Array.isArray(originalFormData.newCarSeriesObjList)
          ? {
              newCarSeriesList: originalFormData.newCarSeriesObjList.map(item => item.code),
              newCarSeriesObjList: originalFormData.newCarSeriesObjList
            }
          : {}),
        ...(Array.isArray(originalFormData.compCarSeriesObjList)
          ? { compCarSeriesObjList: originalFormData.compCarSeriesObjList }
          : {})
      })
    }
  }

  /**
   * 写入场景报告上下文，不修改普通页面的筛选缓存。
   * @param originData 报告详情或用于清空状态的对象
   * @param strict 是否严格校验已发布报告条件
   */
  const applySceneOriginData = async (originData: SceneOriginData, strict: boolean) => {
    const isDetail = originData?.isDetail === true
    const condition = isDetail
      ? parseDefaultCondition(originData.defaultCondition, strict)
      : { formData: {}, customTimes: [], valid: false }

    if (isDetail) {
      await restoreSpecialReportCondition(condition)
    }

    sceneOriginData.value = {
      isDetail,
      reportId: isDetail ? normalizeString(originData.reportId || originData.id) : '',
      hasValidCondition: isDetail && condition.valid,
      formData: condition.formData,
      customTimes: condition.customTimes,
      reportName: isDetail ? normalizeString(originData.reportName) : ''
    }
  }

  /**
   * 设置站内跳转使用的报告源数据；历史异常数据保持宽松兼容。
   * @param originData 报告详情或用于清空状态的对象
   */
  const setSceneOriginData = async (originData: SceneOriginData) => {
    await applySceneOriginData(originData, false)
  }

  /**
   * 初始化外部直链报告；默认条件异常时抛错并阻止业务页面发起查询。
   * @param originData 报告详情
   */
  const initializeSceneReportDetail = async (originData: SceneOriginData) => {
    await applySceneOriginData(originData, true)
  }

  /** 是否显示发布按钮，详情状态下不展示发布按钮。 */
  const detailFlag = computed(() => !sceneOriginData.value.isDetail)

  /** 获取详情中的筛选条件及解析后的日期范围。 */
  const getDetailFilter = computed(() => {
    const formData = sceneOriginData.value.formData
    const customTimes = sceneOriginData.value.customTimes
    let startDate: string | undefined
    let endDate: string | undefined
    let selectedShortcut: string | undefined
    const dateRangeValue = formData?.dateRange

    if (dateRangeValue === 'custom' && customTimes.length === 2) {
      startDate = customTimes[0]
      endDate = customTimes[1]
      selectedShortcut = '自定义'
    } else if (dateRangeValue) {
      const times = getShortcutDateRange(dateRangeValue)
      startDate = times[0]
      endDate = times[1]
      selectedShortcut = getTimeDimensionByCode(dateRangeValue)?.name
    }

    return {
      formData,
      customTimes,
      startDate,
      endDate,
      selectedShortcut
    }
  })

  return {
    sceneOriginData,
    setSceneOriginData,
    initializeSceneReportDetail,
    detailFlag,
    getDetailFilter
  }
})

export default useSceneAnalysisStore
