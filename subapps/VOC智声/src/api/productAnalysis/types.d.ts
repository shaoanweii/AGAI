/**
 * 产品分析模块类型定义（与 Swagger 文档严格对齐）
 *
 * 注意：产品分析模块使用全局的 VocQueryParams 类型
 * 该类型已在 src/types/common.d.ts 中定义，基于 ExtendComQueryModel 内容更新
 */

// 最新声音数据 - 从 Swagger 文档中发现的类型
export interface LatestSoundVo {
  /** 声音ID */
  id: string
  /** 原声内容 */
  soundContent: string
  /** 标签 */
  tags: string[]
  /** 用户名称 */
  userName: string
}

// 综合分析简报响应数据
export interface ProductBriefVo {
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

// 观点项
export interface OpinionItem {
  /** 观点名 */
  opinion: string
  /** 提及量 */
  mentions: number
  /** 提及量环比，% 两位小数 */
  mentionsMoM: number
}

// 用户意图观点TOP响应数据（根据 Swagger 文档更新）
export interface IntentionOpinionTopVo {
  /** 观点名 */
  opinion: string
  /** 提及量 */
  mentions: number
  /** 提及量环比，% 两位小数 */
  mentionsMoM: number
  /** 提及量同比，% 两位小数 */
  mentionsYoY: number
  /** 最新声音数据 */
  sound: LatestSoundVo
}

// 场景TOP响应数据（与本品分析保持一致）
export interface SceneTopVo {
  /** 场景名称 */
  scenario: string
  /** 提及量 */
  mentions: number
  /** 负面率，% 两位小数 */
  negativeRate: number
  /** 提及量环比，% 两位小数 */
  mentionsMoM: number
  /** 负面率环比，% 两位小数 */
  negativeRateMoM: number
  /** 提及量环比数据组，% 两位小数 */
  mentionsMoMGroup: string[]
  /** 负面率环比数据组，% 两位小数 */
  negativeRateMoMGroup: string[]
}

// 关注场景分析单个标签数据
export interface SceneAnalysisVo {
  /** 标签名称 */
  tagName: string
  /** 标签Code */
  tagCode: string
  /** 品牌提及量/车系提及量，品牌负面率/车系负面率 */
  value: number
  /** 集团提及量均值/车系提及量均值，集团负面率均值/车系负面率均值 */
  valueAvg: number
  /** 品牌提及量/车系提及量，品牌负面率/车系负面率 环比 */
  valueMoM: number
  /** 集团提及量均值/车系提及量均值，集团负面率均值/车系负面率均值 环比 */
  valueAvgMoM: number
  /** 品牌提及量/车系提及量，品牌负面率/车系负面率 同比 */
  valueYoY: number
  /** 集团提及量均值/车系提及量均值，集团负面率均值/车系负面率均值 同比 */
  valueAvgYoY: number
}

// 关注场景分析基础响应数据（根据Swagger文档更新）
export interface SceneAnalysisBaseVo {
  /** 品牌或车系名称 */
  name: string
  /** 均值名称 */
  avgName: string
  /** 标签数据 */
  tagData: SceneAnalysisVo[]
}

// 数据趋势变化点数据
export interface ProductTrendPointVo {
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

// 数据趋势变化响应数据
export interface ProductTrendVo {
  /** 负面率均值，% 两位小数 */
  negativeRateAvg: number
  /** 趋势数据 */
  trend: ProductTrendPointVo[]
}

// 数据来源分析响应数据
export interface DataSourceAnalysisVo {
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
  negativeRateYoY?: number
  /** 文字颜色 */
  rateColor?: string
  /** 背景颜色 */
  rateBackgroundColor?: string
}

// 渠道负面率趋势变化响应数据 - 根据 Swagger 文档重新定义
export interface ChannelNegativeTrendPointVo {
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

export interface ChannelNegativeTrendVo {
  /** 日期，yyyy-MM-dd */
  date: string
  /** 负面率，% 两位小数 */
  chDatas: ChannelNegativeTrendPointVo[]
}

// 渠道提及量占比响应数据
export interface ChannelMentionShareVo {
  /** 渠道名称 */
  channelName: string
  /** 渠道编码 */
  channelCode: string
  /** 提及量 */
  mentions: number
  /** 占比，% 两位小数 */
  share: number
}

// 产品分析API响应类型 - 使用全局 BaseResponse 类型保持项目一致性
export type ResultProductBriefVo = BaseResponse<ProductBriefVo>
export type ResultIntentionOpinionTopVo = BaseResponse<IntentionOpinionTopVo[]>
export type ResultListSceneTopVo = BaseResponse<SceneTopVo[]>
export type ResultSceneAnalysisBaseVo = BaseResponse<SceneAnalysisBaseVo>
export type ResultProductTrendVo = BaseResponse<ProductTrendVo>
export type ResultListDataSourceAnalysisVo = BaseResponse<DataSourceAnalysisVo[]>
export type ResultListChannelNegativeTrendVo = BaseResponse<ChannelNegativeTrendVo[]>
export type ResultListChannelMentionShareVo = BaseResponse<ChannelMentionShareVo[]>
