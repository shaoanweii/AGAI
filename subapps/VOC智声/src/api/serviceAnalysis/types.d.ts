/**
 * 服务分析模块类型定义（与 Swagger 文档严格对齐）
 *
 * 注意：所有接口请求参数均使用全局 VocQueryParams（src/types/common.d.ts）
 */

// 0) 最新声音数据 - 从 Swagger 文档中发现的新类型
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

// 1) 用户意图观点TOP - 元素项（根据 Swagger 文档更新）
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
// 为兼容现有组件命名，保留别名
export type ServiceIntentionOpinionTopVo = IntentionOpinionTopVo

// 2) 省份排行/地图 - 元素项（根据 Swagger 文档更新）
export interface ServiceProvinceRankVo {
  /** 省份名称 */
  provinceName: string
  /** 省份编码 */
  provinceCode: string
  /** 负面率，% 两位小数 */
  negativeRate: number
  /** 负面率，% 两位小数 环比 */
  negativeRateMoM: number
  /** 负面率，% 两位小数 同比 */
  negativeRateYoY: number
  /** 提及量 */
  mentions: number
}

// 3) 综合分析简报
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
}

// 4) 关注场景TOP - 元素项（与本品分析保持一致）
export interface SceneTopVo {
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

// 5) 关注场景分析
export interface SceneAnalysisVo {
  /** 标签名称 */
  tagName: string
  /** 标签Code */
  tagCode: string
  /** 品牌/车系：提及量或负面率数值 */
  value: number
  /** 均值：提及量或负面率 */
  valueAvg: number
  /** 品牌/车系：环比 */
  valueMoM: number
  /** 均值：环比 */
  valueAvgMoM: number
  /** 品牌/车系：同比 */
  valueYoY: number
  /** 均值：同比 */
  valueAvgYoY: number
}
export interface SceneAnalysisBaseVo {
  /** 品牌或车系名称 */
  name: string
  /** 均值名称 */
  avgName: string
  /** 标签数据 */
  tagData: SceneAnalysisVo[]
}

// 6) 经销商评价排行TOP - 元素项
export interface ServiceDealerRankVo {
  /** 经销商名称 */
  dealerName: string
  /** 经销商编码 */
  dealerCode: string
  /** 负面率，% 两位小数 */
  negativeRate: number
  /** 提及量 */
  mentions: number
  /** 评分 */
  score: number
}

// 7) 数据趋势变化
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
export interface ProductTrendVo {
  /** 负面率均值，% 两位小数 */
  negativeRateAvg: number
  /** 趋势数据 */
  trend: ProductTrendPointVo[]
}

// 8) 数据来源分析 - 元素项
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
}

// 9) 渠道负面率趋势变化 - 根据 Swagger 文档重新定义
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

// 10) 渠道提及量占比 - 元素项
export interface ChannelShareVo {
  /** 渠道名称 */
  channelName: string
  /** 渠道编码 */
  channelCode: string
  /** 提及量 */
  mentions: number
  /** 占比，% 两位小数 */
  share: number
}

// ================= 返回包装类型（BaseResponse） =================
export type ResultListIntentionOpinionTopVo = BaseResponse<IntentionOpinionTopVo[]>
export type ResultListServiceProvinceRankVo = BaseResponse<ServiceProvinceRankVo[]>
export type ResultProductBriefVo = BaseResponse<ProductBriefVo>
export type ResultListSceneTopVo = BaseResponse<SceneTopVo[]>
export type ResultSceneAnalysisBaseVo = BaseResponse<SceneAnalysisBaseVo>
export type ResultListServiceDealerRankVo = BaseResponse<ServiceDealerRankVo[]>
export type ResultProductTrendVo = BaseResponse<ProductTrendVo>
export type ResultListDataSourceAnalysisVo = BaseResponse<DataSourceAnalysisVo[]>
export type ResultListChannelNegativeTrendVo = BaseResponse<ChannelNegativeTrendVo[]>
export type ResultListChannelShareVo = BaseResponse<ChannelShareVo[]>
