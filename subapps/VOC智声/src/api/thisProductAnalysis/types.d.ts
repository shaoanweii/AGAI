/**
 * 本品分析模块类型定义（与 Swagger 文档严格对齐）
 *
 * 注意：本品分析模块使用全局的 VocQueryParams 类型
 * 该类型已在 src/types/common.d.ts 中定义，基于 ExtendComQueryModel 内容更新
 */

// ==================== 用户旅程分析相关类型 ====================

/** 本品分析-用户旅程观点项 */
export interface ProductSelfJourneyOpinionItemVo {
  /** 观点名称 */
  opinionName: string
  /** 提及量 */
  mentions: number
  /** 提及量环比（%），保留两位小数 */
  mom: number
  /** 提及量同比（%），保留两位小数 */
  yoy: number
}

/** 本品分析-用户旅程分析响应数据 */
export interface ProductSelfJourneyAnalysisVo {
  /** 旅程名称(全旅程客户标签1级) */
  journeyName: string
  /** 旅程编码(全旅程客户标签1级编码) */
  journeyCode: string
  /** 提及量 */
  mentions: number
  /** 提及量环比(%)，四舍五入保留两位小数 */
  mentionsMoM: number
  /** 提及量同比(%)，四舍五入保留两位小数 */
  mentionsYoY: number
  /** 负面率(%)，四舍五入保留两位小数 */
  negativeRate: number
  /** 负面率环比(%)，四舍五入保留两位小数 */
  negativeRateMoM: number
  /** 表情类型：1愤怒 2失望 3满意 4喜悦 */
  emotionType: number
  rateBackgroundColor: string
  rateColor: string
  /** 客户满意 Top5 */
  satisfiedTop5: ProductSelfJourneyOpinionItemVo[]
  /** 客户不满 Top5 */
  dissatisfiedTop5: ProductSelfJourneyOpinionItemVo[]
}

// ==================== 综合分析简报相关类型 ====================

/** 本品分析-综合分析简报响应数据 */
export interface ProductSelfBriefVo {
  /** 负面率，单位% 保留两位小数 */
  negativeRate: number
  /** 负面率环比，单位% 保留两位小数 */
  negativeRateMoM: number
  /** 负面率同比，单位% 保留两位小数 */
  negativeRateYoY: number
  /** 正面率，单位% 保留两位小数 */
  positiveRate: number
  /** 正面率环比，单位% 保留两位小数 */
  positiveRateMoM: number
  /** 正面率同比，单位% 保留两位小数 */
  positiveRateYoY: number
  /** 提及量 */
  mentions: number
  /** 提及量环比，单位% 保留两位小数 */
  mentionsMoM: number
  /** 提及量同比，单位% 保留两位小数 */
  mentionsYoY: number
  /** 用户数（去重one_id） */
  users: number
  /** 用户数环比，单位% 保留两位小数 */
  usersMoM: number
  /** 用户数同比，单位% 保留两位小数 */
  usersYoY: number
  /** 文字颜色 */
  rateColor?: string
  /** 背景颜色 */
  rateBackgroundColor?: string
}

// ==================== 关注场景TOP相关类型 ====================

/** 本品分析-场景TOP响应数据 */
export interface ProductSelfSceneTopVo {
  /** 场景名称 */
  scenario: string
  /** 提及量 */
  mentions: number
  /** 负面率，% 两位小数 */
  negativeRate: number
  /** 提及量环比数据组，% 两位小数 */
  mentionsMoMGroup: string[]
  /** 负面率环比数据组，% 两位小数 */
  negativeRateMoMGroup: string[]
}

// ==================== 数据趋势变化相关类型 ====================

/** 本品分析-数据趋势变化点数据 */
export interface ProductSelfTrendPointVo {
  /** 日期，yyyy-MM-dd */
  date: string
  /** 负面率，% 两位小数 */
  negativeRate: number
  /** 负面率环比，% 两位小数 */
  negativeRateMoM: number
  /** 负面率同比，% 两位小数 */
  negativeRateYoY: number
  /** 正面提及量 */
  positiveMentions: number
  /** 正面提及量环比，% 两位小数 */
  positiveMentionsMoM: number
  /** 正面提及量同比，% 两位小数 */
  positiveMentionsYoY: number
  /** 中性提及量 */
  neutralMentions: number
  /** 中性提及量环比，% 两位小数 */
  neutralMentionsMoM: number
  /** 中性提及量同比，% 两位小数 */
  neutralMentionsYoY: number
  /** 负面提及量 */
  negativeMentions: number
  /** 负面提及量环比，% 两位小数 */
  negativeMentionsMoM: number
  /** 负面提及量同比，% 两位小数 */
  negativeMentionsYoY: number
  /** 总提及量 */
  totalMentions: number
  /** 总提及量环比，% 两位小数 */
  totalMentionsMoM: number
  /** 总提及量同比，% 两位小数 */
  totalMentionsYoY: number
  /** 表情类型 1：愤怒 2：失望 3：满意 4：喜悦 */
  emotionType: string
  /** 负面平均值 */
  negativeAyg: number
}

/** 本品分析-数据趋势变化响应数据 */
export interface ProductSelfTrendVo {
  /** 负面率均值，% 两位小数 */
  negativeRateAvg: number
  /** 趋势数据 */
  trend: ProductSelfTrendPointVo[]
}

// ==================== 渠道数据排行相关类型 ====================

/** 本品分析-数据来源分析响应数据 */
export interface ProductSelfDataSourceAnalysisVo {
  /** 品牌图片URL，暂无 */
  brandImageUrl?: string
  /** 品牌名称 */
  brandName?: string
  /** 品牌编码 */
  brandCode?: string
  /** 渠道名称 */
  channelName: string
  /** 渠道编码 */
  channelCode: string
  /** 提及量 */
  mentions: number
  /** 提及量环比 */
  mentionsMoM: number
  /** 提及量同比 */
  mentionsYoY: number
  /** 负面率（%） */
  negativeRate: number
  /** 负面率环比，% 两位小数 */
  negativeRateMoM?: number
}

// ==================== 渠道负面率趋势变化相关类型 ====================

/** 本品分析-渠道负面率趋势变化响应数据 */
export interface ProductSelfChannelNegativeTrendPointVo {
  /** 渠道名称 */
  channelName: string
  /** 渠道编码 */
  channelCode: string
  /** 日期，yyyy-MM-dd */
  date: string
  /** 提及量/负面率（% 两位小数） */
  value: number
  /** 提及量/负面率（% 两位小数） 环比 */
  valueMoM: number
  /** 提及量/负面率（% 两位小数） 同比 */
  valueYoY: number
}

export interface ProductSelfChannelNegativeTrendVo {
  /** 日期，yyyy-MM-dd */
  date: string
  /** 负面率，% 两位小数 */
  chDatas: ProductSelfChannelNegativeTrendPointVo[]
}

// ==================== 渠道提及量占比相关类型 ====================

/** 本品分析-渠道提及量占比响应数据 */
export interface ProductSelfChannelMentionShareVo {
  /** 渠道名称 */
  channelName: string
  /** 渠道编码 */
  channelCode: string
  /** 提及量 */
  mentions: number
  /** 占比，% 两位小数 */
  share: number
}

// ==================== 标签分析相关类型 ====================

/** 本品分析-标签分析行数据 */
export interface ProductSelfTagAnalysisRowVo {
  // 名称
  name: string
  // 编码
  code: string
  // 图片URL
  imageUrl: string
  // 一级标签名称
  tag1Name: string
  // 一级标签编码
  tag1Code: string
  // 二级标签名称
  tag2Name: string
  // 二级标签编码
  tag2Code: string
  // 负面率/提及量
  value1: number
  // 负面率/提及量 环比 (悬浮时使用)
  value1MoM: number
  // 负面率/提及量 同比 (悬浮时使用)
  value1YoY: number
  // 提及量/负面率环比，提及量环比
  value2: number
  // 提及量环比 (悬浮时使用)
  value2MoM: number
  // 提及量同比 (悬浮时使用)
  value2YoY: number
  /** 文字颜色 */
  rateColor?: string
  /** 背景颜色 */
  rateBackgroundColor?: string
}

// ==================== API响应类型定义 ====================

/** 本品分析API响应类型 - 使用全局 BaseResponse 类型保持项目一致性 */
export type ResultListProductSelfJourneyAnalysisVo = BaseResponse<ProductSelfJourneyAnalysisVo[]>
export type ResultProductSelfBriefVo = BaseResponse<ProductSelfBriefVo>
export type ResultListProductSelfSceneTopVo = BaseResponse<ProductSelfSceneTopVo[]>
export type ResultProductSelfTrendVo = BaseResponse<ProductSelfTrendVo>
export type ResultListProductSelfDataSourceAnalysisVo = BaseResponse<
  ProductSelfDataSourceAnalysisVo[]
>
export type ResultListProductSelfChannelNegativeTrendVo = BaseResponse<
  ProductSelfChannelNegativeTrendVo[]
>
export type ResultListProductSelfChannelMentionShareVo = BaseResponse<
  ProductSelfChannelMentionShareVo[]
>
export type ResultListProductSelfTagAnalysisRowVo = BaseResponse<ProductSelfTagAnalysisRowVo[]>
