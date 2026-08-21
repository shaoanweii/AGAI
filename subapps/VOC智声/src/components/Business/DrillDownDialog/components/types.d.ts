/**
 * 渠道发声数据类型
 * 对应接口: /report/drill-down/channel-top
 */
export interface ChannelTopVo {
  /** 渠道名称 */
  channelName: string
  /** 渠道编码 */
  channelCode: string
  /** 数值（负面率% 两位小数 或 提及量） */
  value: number
}

/**
 * 数据源列表数据类型
 * 对应接口: /report/drill-down/data-source-list
 */
export interface DataSourceListVo {
  /** 渠道名称 */
  channelName: string
  /** 渠道编码 */
  channelCode: string
  /** 提及量 */
  mentions: number
  /** 提及量占比，% 两位小数 */
  mentionsShare: number
  /** 趋势-提及 */
  mentionTrend: string[]
  /** 提及-环比，% 两位小数 */
  mentionsMoM: number
  /** 负面-负面率，% 两位小数 */
  negativeRateValue: number
  /** 负面-趋势 */
  negativeTrend: string[]
  /** 负面-环比，% 两位小数 */
  negativeRateMoM: number
  /** 正面-正面率，% 两位小数 */
  positiveRateValue: number
  /** 正面-趋势 */
  positiveTrend: string[]
  /** 正面-环比，% 两位小数 */
  positiveRateMoM: number
  /** 中性-中性率，% 两位小数 */
  neutralRateValue: number
  /** 中性-趋势 */
  neutralTrend: string[]
  /** 中性-环比，% 两位小数 */
  neutralRateMoM: number
}

/**
 * 观点评价Top数据类型
 * 对应接口: /report/drill-down/opinion-evaluate-top
 */
export interface OpinionEvaluateTopVo {
  /** 观点 */
  opinion: string
  /** 正面提及量 */
  positiveMentions: number
  /** 负面提及量 */
  negativeMentions: number
  /** 中性提及量 */
  neutralMentions: number
  /** 总提及量 */
  totalMentions: number
}