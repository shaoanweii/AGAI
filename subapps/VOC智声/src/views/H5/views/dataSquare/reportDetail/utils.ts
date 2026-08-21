import { getShortcutDateRange } from '@/utils/date'
import type {
  H5DataSquareReportDateCondition,
  H5DataSquareReportDefaultCondition,
  H5DataSquareReportDetail
} from '@h5/api/dataSquare'
import type { H5VocBaseRequest, VoiceListItem } from '@h5/api/home/types'

const VALID_DYNAMIC_DATE_RANGE_CODES = ['2', '3']

/**
 * 过滤数组中的空值和全选占位值。
 * @param value 原始数组
 * @returns 可提交给接口的有效值
 */
export const filterValidArray = (value?: unknown): string[] => {
  if (!Array.isArray(value)) {
    return []
  }

  return value.filter((item): item is string => {
    return typeof item === 'string' && !!item.trim() && item !== 'all'
  })
}

/**
 * 提取报告配置中的时间范围。
 * @param detail 报告详情
 * @returns 开始和结束日期
 */
export const resolveReportDateRange = (
  detail?: H5DataSquareReportDetail | null
): Pick<H5VocBaseRequest, 'startDate' | 'endDate'> => {
  return resolveConditionDateRange(detail?.defaultCondition, detail?.dateCondition)
}

/**
 * 提取筛选条件中的时间范围。
 * @param defaultCondition 默认筛选条件
 * @param dateCondition 自定义日期条件
 * @returns 开始和结束日期
 */
export const resolveConditionDateRange = (
  defaultCondition?: H5DataSquareReportDefaultCondition | null,
  dateCondition?: H5DataSquareReportDateCondition | null
): Pick<H5VocBaseRequest, 'startDate' | 'endDate'> => {
  const dateRange = defaultCondition?.dateRange

  if (dateRange && VALID_DYNAMIC_DATE_RANGE_CODES.includes(dateRange)) {
    const [startDate, endDate] = getShortcutDateRange(dateRange)
    return { startDate, endDate }
  }

  return {
    startDate: dateCondition?.startDate || undefined,
    endDate: dateCondition?.endDate || undefined
  }
}

/**
 * 将报告筛选条件转换为 H5 VOC 查询参数。
 * @param defaultCondition 当前筛选条件
 * @param dateCondition 当前日期条件
 * @returns 查询参数
 */
export const buildReportBaseParamsByCondition = (
  defaultCondition?: H5DataSquareReportDefaultCondition | null,
  dateCondition?: H5DataSquareReportDateCondition | null
): H5VocBaseRequest => {
  const condition: H5DataSquareReportDefaultCondition = defaultCondition || {}
  const brandCodeList = condition.brandList || []
  const carSeriesList = condition.carSeriesList || []
  const channelCodes = filterValidArray(condition.channelIds)
  const sentimentList = filterValidArray(condition.sentimentList)
  const intentionList = filterValidArray(condition.intentionList)
  const contentTypes = filterValidArray(condition.contentTypes)
  const usageScenarioCodes = filterValidArray(condition.usageScenarioCodes)
  const topicCodes = filterValidArray(condition.topicCodes)
  const scenarioAttr = filterValidArray(condition.scenarioAttr)
  const advertisementType = filterValidArray(condition.advertisementType)
  const accountTypes = filterValidArray(condition.accountTypes)
  const experienceCodePaths = normalizeExperienceCodeValue(condition.experienceCode)

  const params: H5VocBaseRequest & Record<string, unknown> = {
    brandDataType: 3,
    ...resolveConditionDateRange(condition, dateCondition)
  }

  if (condition.tagType) {
    params.tagType = condition.tagType
  }

  if (brandCodeList.length > 0) {
    params.brandCodeList = brandCodeList
  }

  if (carSeriesList.length > 0) {
    params.carSeriesList = carSeriesList
  }

  if (channelCodes.length > 0) {
    params.channelIds = channelCodes
  }

  if (sentimentList.length > 0) {
    params.sentimentList = sentimentList
    if (sentimentList.length === 1) {
      params.sentiment = sentimentList[0]
    }
  }

  if (intentionList.length > 0) {
    params.intentionList = intentionList
    if (intentionList.length === 1) {
      params.intention = intentionList[0]
    }
  }

  if (contentTypes.length > 0) {
    params.contentTypes = contentTypes
  }

  if (usageScenarioCodes.length > 0) {
    params.usageScenarioSecondList = usageScenarioCodes
  }

  if (scenarioAttr.length > 0) {
    params.scenarioAttr = scenarioAttr
  }

  if (advertisementType.length > 0) {
    params.advertisementType = advertisementType
  }

  if (accountTypes.length > 0) {
    params.isBigV = accountTypes
  }

  if (topicCodes.length > 0) {
    params.topicCodes = topicCodes
  }

  const experienceLevelCodeMap = getExperienceSelectedLevelCodeMap(experienceCodePaths)
  const firstCodes = experienceLevelCodeMap[1] || []
  const secondCodes = experienceLevelCodeMap[2] || []
  const threeCodes = experienceLevelCodeMap[3] || []
  const fourCodes = experienceLevelCodeMap[4] || []

  if (firstCodes.length > 0) {
    params.firstCodeTag = firstCodes
  }
  if (secondCodes.length > 0) {
    params.secondCodeTag = secondCodes
  }
  if (threeCodes.length > 0) {
    params.threeCodeTag = threeCodes
  }
  if (fourCodes.length > 0) {
    params.fourCodeTag = fourCodes
  }

  return params
}

/**
 * 将报告保存条件转换为 H5 VOC 查询参数。
 * @param detail 报告详情
 * @returns 查询参数
 */
export const buildReportBaseParams = (
  detail?: H5DataSquareReportDetail | null
): H5VocBaseRequest => {
  return buildReportBaseParamsByCondition(detail?.defaultCondition, detail?.dateCondition)
}

/**
 * 生成可编辑的报告筛选条件副本，补齐所有数组字段。
 * @param condition 原始筛选条件
 * @returns 标准筛选条件
 */
export const normalizeReportDefaultCondition = (
  condition?: H5DataSquareReportDefaultCondition | null
): H5DataSquareReportDefaultCondition => {
  return {
    dateRange: condition?.dateRange || '3',
    brandList: condition?.brandList || [],
    carSeriesList: condition?.carSeriesList || [],
    channelIds: filterValidArray(condition?.channelIds),
    sentimentList: filterValidArray(condition?.sentimentList),
    intentionList: filterValidArray(condition?.intentionList),
    tagType: condition?.tagType || 'CA',
    experienceCode: normalizeExperienceCodeValue(condition?.experienceCode),
    topicCodes: filterValidArray(condition?.topicCodes),
    usageScenarioCodes: filterValidArray(condition?.usageScenarioCodes),
    scenarioAttr: filterValidArray(condition?.scenarioAttr),
    contentTypes: filterValidArray(condition?.contentTypes),
    advertisementType: filterValidArray(condition?.advertisementType),
    accountTypes: filterValidArray(condition?.accountTypes)
  }
}

/**
 * 清洗体验代码路径数组，保持每条路径最多四级且去除重复路径。
 * @param value 原始路径值
 * @returns 标准路径数组
 */
export const normalizeExperienceCodeValue = (value?: unknown): string[][] => {
  if (!Array.isArray(value) || value.length === 0) {
    return []
  }

  const pathKeySet = new Set<string>()
  const paths: string[][] = []

  value.forEach(item => {
    if (!Array.isArray(item)) {
      return
    }

    const path = item
      .filter((code): code is string => typeof code === 'string' && !!code.trim())
      .slice(0, 4)

    if (path.length === 0) {
      return
    }

    const pathKey = path.join('>')
    if (pathKeySet.has(pathKey)) {
      return
    }

    pathKeySet.add(pathKey)
    paths.push(path)
  })

  return paths
}

/**
 * 生成体验代码路径唯一键，用于识别本次新增路径。
 * @param path 体验代码路径
 * @returns 路径唯一键
 */
const getExperiencePathKey = (path: string[]) => {
  return JSON.stringify(path)
}

/**
 * 按新增路径所在层级过滤体验代码，避免交互新增时混选不同层级。
 * @param value 当前路径数组
 * @param previousValue 上一次路径数组
 * @returns 同层级路径数组
 */
export const normalizeSameLevelExperienceCodeValue = (
  value?: unknown,
  previousValue?: unknown
): string[][] => {
  const nextPaths = normalizeExperienceCodeValue(value)
  if (nextPaths.length <= 1) {
    return nextPaths
  }

  const previousPathKeySet = new Set(
    normalizeExperienceCodeValue(previousValue).map(path => getExperiencePathKey(path))
  )
  const addedPath = nextPaths.find(path => !previousPathKeySet.has(getExperiencePathKey(path)))
  if (!addedPath) {
    return nextPaths
  }

  return nextPaths.filter(path => path.length === addedPath.length)
}

/**
 * 获取体验代码每条路径的末级编码，用于联动标准观点。
 * @param value 体验代码路径数组
 * @returns 末级编码列表
 */
export const getExperienceLastLevelCodes = (value?: unknown) => {
  return Array.from(
    new Set(
      normalizeExperienceCodeValue(value)
        .map(path => path[path.length - 1] || '')
        .filter((code): code is string => !!code)
    )
  )
}

/**
 * 按用户实际选择的路径层级提取体验代码编码。
 * 例如选择二级节点时只写入 secondCodeTag，不再携带一级祖先编码。
 * @param paths 体验代码路径数组
 * @returns 按选择层级分组后的编码列表
 */
export const getExperienceSelectedLevelCodeMap = (paths: string[][]) => {
  return paths.reduce<Record<number, string[]>>((map, path) => {
    const level = path.length
    const code = path[level - 1] || ''
    if (!level || !code) {
      return map
    }

    const levelCodes = map[level] || []
    if (!levelCodes.includes(code)) {
      map[level] = [...levelCodes, code]
    }

    return map
  }, {})
}

/**
 * 判断报告默认条件是否已限制到标准观点且没有体验代码。
 * @param detail 报告详情
 * @returns 是否需要隐藏指标分析模块
 */
export const isOnlyTopicWithoutExperienceCode = (detail?: H5DataSquareReportDetail | null) => {
  const topicCodes = filterValidArray(detail?.defaultCondition?.topicCodes)
  const experienceCode = detail?.defaultCondition?.experienceCode || []
  const hasExperienceCode = experienceCode.some(level => filterValidArray(level).length > 0)

  return topicCodes.length > 0 && !hasExperienceCode
}

/**
 * 根据标签层级生成联动查询条件。
 * @param item 指标项
 * @returns 标签下钻参数
 */
export const buildTagDrillParams = (item: {
  tagCode?: string
  tagLevel?: number | string
}): H5VocBaseRequest => {
  const tagCode = item.tagCode || ''
  if (!tagCode) return {}

  const tagLevel = String(item.tagLevel || '1')
  if (tagLevel === '2') return { tag2Code: tagCode }
  if (tagLevel === '3') return { tag3Code: tagCode }
  if (tagLevel === '4') return { tag4Code: tagCode }

  return { tag1Code: tagCode }
}

/**
 * 将客户原声接口结果转换为 HVoiceList 可展示结构。
 * @param item 原始接口项
 * @returns 声音列表项
 */
export const mapVoiceItem = (item: any): VoiceListItem => {
  return {
    ...item,
    id: item.id || item.newId || '',
    dataId: item.dataId || item.newId || '',
    channelCode: item.channelCode || '',
    originalId: item.originalId || '',
    originalTexTScene: item.originalTextScene || item.originalTexTScene || '',
    channel: item.channelName || item.channel || '',
    brandCode: item.brandCode || '',
    brand: item.brandName || item.brand || '',
    carSeriesCode: item.carSeriesCode || '',
    carSeries: item.carSeriesName || item.carSeries || '',
    sentiment: item.sentiment || '',
    intention: item.intent || item.intention || '',
    dataCreateTime: item.evaluateTime || item.dataCreateTime || '',
    oneId: item.oneId || '',
    custName: item.username || item.custName || '',
    topics: item.topics || [],
    title: item.title || '',
    content: item.content || item.originalTextScene || ''
  }
}
