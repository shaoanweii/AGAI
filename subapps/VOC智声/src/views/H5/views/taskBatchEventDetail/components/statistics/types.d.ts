/**
 * 批量详情统计页基础信息。
 * 当前保留给统计展示数据结构复用。
 */
export interface BatchEventStatisticsDetail {
  creatorName?: string
  brandName?: string
  eventPriorityName?: string
  warningFrequency?: string
  warningStartTime?: string
  warningEndTime?: string
  subjectCategoryName?: string
  secondDeptName?: string
  thirdDeptName?: string
  /** 后端已废弃，仅用于兼容旧数据 */
  mainRespOrgName?: string
  mainRespUserName?: string
  businessOwnerName?: string
  focusTopics?: string[]
  moreItems?: Array<{
    label: string
    value: string
  }>
}

/** 报告解读内容。 */
export interface BatchEventReportInterpretation {
  content?: string
}

/** 顶部指标卡。 */
export interface BatchEventMetricCard {
  label: string
  value: string
  change: string
  tone: 'warning' | 'positive' | 'neutral'
}

/** 趋势图单日数据。 */
export interface BatchEventTrendItem {
  date: string
  positiveCount: number
  neutralCount: number
  negativeCount: number
}

/** 简单柱状图数据项。 */
export interface BatchEventBarItem {
  name: string
  value: number
}

/** 聚焦场景堆叠数据项。 */
export interface BatchEventSceneItem {
  name: string
  positiveCount: number
  neutralCount: number
  negativeCount: number
}

/** 观点词云数据项。 */
export interface BatchEventOpinionItem {
  name: string
  value: number
  sentiment: 'positive' | 'neutral' | 'negative'
}

/** 省份分布数据项。 */
export interface BatchEventProvinceItem {
  name: string
  value: number
  color: string
}

/** 渠道排行数据项。 */
export interface BatchEventChannelRankItem {
  rank: number
  channelName: string
  mentionCount: number
  percent: string
}

/** 事件统计 Tab 的完整展示数据。 */
export interface BatchEventStatisticsData {
  detail: BatchEventStatisticsDetail
  report: BatchEventReportInterpretation
  metrics: BatchEventMetricCard[]
  trends: BatchEventTrendItem[]
  carSeries: BatchEventBarItem[]
  focusScenes: BatchEventSceneItem[]
  opinions: BatchEventOpinionItem[]
  provinces: BatchEventProvinceItem[]
  channelRanks: BatchEventChannelRankItem[]
}
