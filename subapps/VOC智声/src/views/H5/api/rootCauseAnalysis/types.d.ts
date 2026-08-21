export interface IntentionOpinionTopVo {
  // 观点名，必选
  opinion: string

  // 情感，必选
  sentiment: string

  // 提及量，必选
  mentions: number

  // 提及量环比，% 两位小数，可选
  mentionsMoM?: number

  // 提及量同比，% 两位小数，可选
  mentionsYoY?: number

  // 事件，字符串数组，可选
  remark?: string[]
}

export interface ProductTrendPointVo {
  // 日期，yyyy-MM-dd
  date?: string
  // 负面率，% 两位小数
  negativeRate?: number
  // 负面率环比，% 两位小数
  negativeRateMoM?: number
  // 负面率同比，% 两位小数
  negativeRateYoY?: number
  // 正面提及量
  positiveMentions?: number
  // 正面提及量环比，% 两位小数
  positiveMentionsMoM?: number
  // 正面提及量同比，% 两位小数
  positiveMentionsYoY?: number
  // 中性提及量
  neutralMentions?: number
  // 中性提及量环比，% 两位小数
  neutralMentionsMoM?: number
  // 中性提及量同比，% 两位小数
  neutralMentionsYoY?: number
  // 负面提及量
  negativeMentions?: number
  // 负面提及量环比，% 两位小数
  negativeMentionsMoM?: number
  // 负面提及量同比，% 两位小数
  negativeMentionsYoY?: number
  // 总提及量
  totalMentions?: number
  // 总提及量环比，% 两位小数
  totalMentionsMoM?: number
  // 总提及量同比，% 两位小数
  totalMentionsYoY?: number
  // 表情/图标编码 1：愤怒 2：失望 3：一般/中立 4：满意 5：惊喜
  emotionType?: string
  negativeAyg?: number
}

export interface TagSentimentAnalysisVo {
  // 标签名称
  tagName?: string
  // 标签编码
  tagCode?: string
  // 提及量
  mention?: number
  // 正面提及量
  positiveMention?: number
  // 负面提及量
  negativeMention?: number
  // 中性提及量
  neutralMention?: number
  // 负面率
  negativeRate?: number
  // 负面率环比
  negativeRateMom?: number
  // 负面率同比
  negativeRateYoy?: number
  // 提及量环比
  mentionMom?: number
  // 提及量同比
  mentionYoy?: number
}

export interface SeriesRankItemVo {
  // 图片URL
  imageUrl?: string
  // 名称
  name?: string
  // 编码
  code?: string
  // 负面率（%）
  negativeRate?: number
  // 负面率环比（%）
  negativeRateMoM?: number
  // 负面率同比（%）
  negativeRateYoY?: number
  // 提及量
  mentions?: number
  // 提及量环比
  mentionsMoM?: number
  // 提及量同比
  mentionsYoY?: number
  // 提及量趋势（与时间轴长度一致）
  mentionsTrend?: string[]
  // 负面率颜色值
  rateColor?: string
  // 负面数趋势（与时间轴长度一致）
  negativeMentionsTrend?: string[]
}
