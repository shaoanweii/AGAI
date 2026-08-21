/**
 * 集团分析模块类型定义
 *
 * 注意：集团分析模块使用全局的 VocQueryParams 类型
 * 该类型已在 src/types/common.d.ts 中定义，基于 ExtendComQueryModel 内容更新
 */

// 集团综合分析简报响应数据（根据API文档更新）
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

// 标签分析行数据
export interface TagAnalysisRowVo {
  /** 品牌名称 */
  brandName: string
  /** 品牌编码 */
  brandCode: string
  /** 品牌图片URL */
  brandImageUrl: string
  /** 一级标签名称 */
  tag1Name: string
  /** 一级标签编码 */
  tag1Code: string
  /** 二级标签名称 */
  tag2Name: string
  /** 二级标签编码 */
  tag2Code: string
  /** 负面率/提及量 */
  value1: number
  /** 负面率/提及量 环比 (悬浮时使用) */
  value1MoM: number
  /** 负面率/提及量 同比 (悬浮时使用) */
  value1YoY: number
  /** 提及量/负面率环比，提及量环比 */
  value2: number
  /** 提及量环比 (悬浮时使用) */
  value2MoM: number
  /** 提及量同比 (悬浮时使用) */
  value2YoY: number
  /** 文字颜色 */
  rateColor?: string
  /** 背景颜色 */
  rateBackgroundColor?: string
}

// 观点评价数据 - 根据最新API文档更新
export interface IntentionOpinionTopVo {
  /** 观点名 */
  opinion: string
  /** 提及量 */
  mentions: number
  /** 提及量环比，% 两位小数 */
  mentionsMoM: number
  /** 提及量同比，% 两位小数 */
  mentionsYoY: number
  sentiment: string
}

export interface OpinionTopVo {
  /** 品牌图片URL，暂无 */
  brandImageUrl: string
  /** 品牌名称 */
  brandName: string
  /** 品牌编码 */
  brandCode: string
  /** 好评观点TOP5 */
  goodOpinions: IntentionOpinionTopVo[]
  /** 差评观点TOP5 */
  badOpinions: IntentionOpinionTopVo[]
}

// 数据来源分析数据 - 根据API文档更新
export interface GroupDataSourceAnalysisVo {
  /** 品牌图片URL，暂无 */
  brandImageUrl: string
  /** 品牌名称 */
  brandName: string
  /** 品牌编码 */
  brandCode: string
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
  /** 文字颜色 */
  rateColor?: string
  /** 背景颜色 */
  rateBackgroundColor?: string
}

// 品牌趋势系列数据（单个品牌在某个时间点的数据）
export interface BrandTrendSeriesVo {
  /** 时间轴（根据维度：天或月），单个时间点 */
  date: string
  /** 品牌名称/智行汽车集团 */
  brandName: string
  /** 品牌编码/智行汽车集团编码 */
  brandCode: string
  /** 提及量/集团均值 */
  value1: number
  /** 负面率/集团均值 */
  value2: number
}

// 品牌趋势数据（单个时间点的所有品牌数据）
export interface BrandTrendVo {
  /** 时间轴（根据维度：天或月），单个时间点 */
  date: string
  /** 该时间点的品牌数据集合 */
  brandSeries: BrandTrendSeriesVo[]
}

// 集团车系排行数据（根据API文档更新）
export interface SeriesRankItemVo {
  /** 图片URL */
  imageUrl: string
  /** 名称 */
  name: string
  /** 编码 */
  code: string
  /** 负面率（%） */
  negativeRate: number
  /** 负面率环比（%） */
  negativeRateMoM: number
  /** 负面率同比（%） */
  negativeRateYoY: number
  /** 提及量 */
  mentions: number
  /** 提及量 环比 */
  mentionsMoM: number
  /** 提及量 同比 */
  mentionsYoY: number
  /** 提及量趋势（与时间轴长度一致） */
  mentionsTrend: string[]
  /** 负面数趋势（与时间轴长度一致） */
  negativeMentionsTrend: string[]
  /** 提及量趋势字符串 */
  mentionsTrendStr: string
  /** 负面数趋势字符串 */
  negativeMentionsTrendStr: string
}

// 集团分析API响应类型 - 使用全局 BaseResponse 类型保持项目一致性
export type ResultProductBriefVo = BaseResponse<ProductBriefVo>
export type ResultListTagAnalysisRowVo = BaseResponse<TagAnalysisRowVo[]>
export type ResultListOpinionTopVo = BaseResponse<OpinionTopVo[]>
export type ResultListGroupDataSourceAnalysisVo = BaseResponse<GroupDataSourceAnalysisVo[]>
export type ResultListBrandTrendVo = BaseResponse<BrandTrendVo[]>
export type ResultListSeriesRankItemVo = BaseResponse<SeriesRankItemVo[]>
