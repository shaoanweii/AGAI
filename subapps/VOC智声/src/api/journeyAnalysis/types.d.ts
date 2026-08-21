/**
 * 旅程分析模块类型定义
 *
 * 注意：旅程分析模块使用通用报表调用接口
 * 该接口基于通用报表调用模块的 CommonReportInvokeModel 和 CommonFilterModel
 */

// 通用过滤模型
export interface CommonFilterModel {
  /** 时间范围-开始时间，格式：yyyy-MM-dd */
  startDate: string
  /** 时间范围-结束时间，格式：yyyy-MM-dd */
  endDate: string
  /** 排序字段，示例：publish_time */
  sortField?: string
  /** 排序类型：asc/desc */
  sortOrder?: 'asc' | 'desc'
  /** 数据类型：negativeRate/mention (负面率/提及量),brand/series (品牌/车系),negativeRateMention (负面率+提及量),negativeRateMoM (负面率+负面率环比),mentionMoM (提及量+提及量环比) */
  dataType?:
    | 'negativeRate'
    | 'mention'
    | 'brand'
    | 'series'
    | 'negativeRateMention'
    | 'negativeRateMoM'
    | 'mentionMoM'
    | ''
}

// 通用报表调用入参模型
export interface CommonReportInvokeModel {
  /** 请求类型：1=简单查询, 2=指定XML, 3=指定解析类 */
  requestType?: 1 | 2 | 3
  /** 来源表名（requestType=1时必填） */
  sourceTable?: string
  /** 显示字段（requestType=1时必填） */
  displayFields?: string[]
  /** XML语句ID（requestType=2或3时可用），可传入完整namespace.id，或仅id（默认namespace） */
  sqlId?: string
  /** 解析处理类（requestType=3时必填），需实现ReportResultParser接口 */
  parserClass?: string
  /** 过滤数据 */
  filterData?: CommonFilterModel
  /** 排序字段 */
  sortField?: string
  /** 排序方向：asc/desc */
  sortOrder?: 'asc' | 'desc'
}

// 通用报表调用响应结果
export interface ResultObject {
  /** 成功标志 */
  success: boolean
  /** 返回处理消息 */
  message: string
  /** 返回代码 */
  code: string
  /** 返回数据对象 */
  result: any
  /** 请求标识 */
  tid: string
}

// ==================== 分割线 ====================
// 以下是从API文档中读取的旅程分析模块接口类型定义

// 发声用户TOP5 - VoiceUserTopVo
export interface VoiceUserTopVo {
  /** 用户名 */
  userName: string
  /** 用户ID */
  userId: string
  /** 提及量 */
  value: number
  /** 负面率(%) */
  negativeRate: number
  /** 环比(%) */
  valueMoM: number
  /** 同比(%) */
  valueYoY: number
}

// 旅程细化分析 - JourneyDetailAnalysisVo
export interface JourneyDetailAnalysisVo {
  /** 标签名称 */
  tagName: string
  /** 标签编码 */
  tagCode: string
  /** 标签层级(1/2/3/4) */
  tagLevel: number
  /** 负面率/提及量 */
  value: number
  /** 负面率/提及量均值 */
  valueAvg: number
  /** 负面率/提及量环比(%) */
  valueMoM: number
  /** 负面率/提及量同比(%) */
  valueYoY: number
}

// 用户类型占比 - UserTypeDistributionVo
export interface UserTypeDistributionVo {
  /** 用户类型 */
  userType: string
  /** 用户数量 */
  value: number
  /** 占比(%) */
  percent: number
  /** 环比(%) */
  valueMoM: number
  /** 同比(%) */
  valueYoY: number
}

// 意图观点TOP - IntentionOpinionTopVo (根据API文档更新)
export interface IntentionOpinionTopVo {
  /** 原声内容 */
  originalSound: LatestContentVo
  /** 用户意图观点TOP */
  opinionTops: OpinionTops[]
}

export interface OpinionTops {
  /** 观点名 */
  opinion?: string
  /** 提及量 */
  mentions?: number
  /** 提及量环比，% 两位小数 */
  mentionsMoM?: number
  /** 提及量同比，% 两位小数 */
  mentionsYoY?: number
  /** 事件 */
  remark?: string[]
}

export interface LatestContentVo {
  /** 原文ID */
  id?: string
  /** 原声内容 */
  content?: string
  /** 观点标签（topic） */
  topics?: any[]
  /** 用户名称 */
  userName?: string
}

// 用户关注场景TOP - UserFocusSceneTopVo
export interface UserFocusSceneTopVo {
  /** 场景名称 */
  sceneName: string
  /** 用户数量 */
  value: number
  /** 环比(%) */
  valueMoM: number
  /** 同比(%) */
  valueYoY: number
}

// 飙升场景TOP - SurgingSceneTopVo
export interface SurgingSceneTopVo {
  /** 场景名称 */
  sceneName?: string

  /** 场景编码 */
  sceneCode?: string

  /** 提及量（对应int64类型，使用number兼容，如需精确大整数可改用bigint） */
  mentions?: number | bigint

  /** 提及量环比(%) */
  mentionsMoM?: number

  /** 提及量同比(%) */
  mentionsYoY?: number

  /** 负面率(%) */
  negativeRate?: number

  /** 负面率环比(%) */
  negativeRateMoM?: number

  /** 负面率同比(%) */
  negativeRateYoY?: number
}

// 所在区域占比 - RegionDistributionVo（与文档对齐）
export interface RegionDistributionVo {
  /** 省份名称 */
  provinceName: string
  /** 省份编码 */
  provinceCode: string
  /** 用户数量 */
  value: number
  /** 占比(%) */
  percent: number
  /** 环比(%) */
  valueMoM: number
  /** 同比(%) */
  valueYoY: number
}

// 综合分析简报 - ProductBriefVo（根据API文档校准）
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

// 高频场景TOP - HighFreqSceneTopVo
export interface HighFreqSceneTopVo {
  /** 场景名称 */
  sceneName?: string
  /** 场景编码 */
  sceneCode?: string
  /** 提及量（对应 int64 类型，TypeScript 中用 number 兼容，若需严格类型可使用 bigint） */
  mentions?: number | bigint
  /** 提及量环比(%) */
  mentionsMoM?: number
  /** 提及量同比(%) */
  mentionsYoY?: number
  /** 负面率(%) */
  negativeRate?: number
  /** 负面率环比(%) */
  negativeRateMoM?: number
  /** 负面率同比(%) */
  negativeRateYoY?: number
}

// 用户性别占比 - GenderDistributionVo（与文档对齐）
export interface GenderDistributionVo {
  /** 性别 */
  gender: string
  /** 用户数量 */
  value: number
  /** 占比(%) */
  percent: number
  /** 环比(%) */
  valueMoM: number
  /** 同比(%) */
  valueYoY: number
}

// 关注场景TOP - FocusSceneTopVo（根据API文档校准）
export interface FocusSceneTopVo {
  /** 场景名称 */
  scenario?: string

  /** 提及量 */
  mentions?: number

  /** 负面率，保留两位小数（%） */
  negativeRate?: number

  /** 提及量环比数据组，保留两位小数（%），数组元素为字符串类型 */
  mentionsMoMGroup?: string[]

  /** 负面率环比数据组，保留两位小数（%），数组元素为字符串类型 */
  negativeRateMoMGroup?: string[]
}

// 产品趋势点数据类型
interface ProductTrendPointVo {
  /** 日期，格式为 yyyy-MM-dd */
  date?: string

  /** 负面率，保留两位小数（%） */
  negativeRate?: number

  /** 负面率环比，保留两位小数（%） */
  negativeRateMoM?: number

  /** 负面率同比，保留两位小数（%） */
  negativeRateYoY?: number

  /** 正面提及量 */
  positiveMentions?: number

  /** 正面提及量环比，保留两位小数（%） */
  positiveMentionsMoM?: number

  /** 正面提及量同比，保留两位小数（%） */
  positiveMentionsYoY?: number

  /** 中性提及量 */
  neutralMentions?: number

  /** 中性提及量环比，保留两位小数（%） */
  neutralMentionsMoM?: number

  /** 中性提及量同比，保留两位小数（%） */
  neutralMentionsYoY?: number

  /** 负面提及量 */
  negativeMentions?: number

  /** 负面提及量环比，保留两位小数（%） */
  negativeMentionsMoM?: number

  /** 负面提及量同比，保留两位小数（%） */
  negativeMentionsYoY?: number

  /** 总提及量 */
  totalMentions?: number

  /** 总提及量环比，保留两位小数（%） */
  totalMentionsMoM?: number

  /** 总提及量同比，保留两位小数（%） */
  totalMentionsYoY?: number

  /** 表情/图标编码：1-愤怒，2-失望，3-一般/中立，4-满意，5-惊喜 */
  emotionType?: string
  /** 负面平均值 */
  negativeAyg: number
}

// 数据趋势变化 - DataTrendChangeVo
export interface DataTrendChangeVo {
  /** 负面率均值，保留两位小数（%） */
  negativeRateAvg?: number

  /** 趋势数据数组 */
  trend?: ProductTrendPointVo[]
}

// 渠道数据排行 - DataSourceAnalysisVo
export interface DataSourceAnalysisVo {
  /** 渠道名称 */
  channelName?: string

  /** 渠道编码 */
  channelCode?: string

  /** 负面率，保留两位小数（%） */
  negativeRate?: number

  /** 负面率环比，保留两位小数（%） */
  negativeRateMoM?: number

  /** 负面率同比，保留两位小数（%） */
  negativeRateYoY?: number

  /** 提及量 */
  mentions?: number

  /** 提及量环比，保留两位小数（%） */
  mentionsMoM?: number

  /** 提及量同比，保留两位小数（%） */
  mentionsYoY?: number
}

// 渠道负面率趋势变化 - 根据 Swagger 文档对齐
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

// 渠道提及量占比 - 根据 Swagger 文档对齐
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

// 渠道占比 - 根据 Swagger 文档中的 ChannelShareVo 类型
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

// 各年龄段占比 - AgeDistributionVo（与文档对齐）
export interface AgeDistributionVo {
  /** 年龄段 */
  title: string
  /** 用户数量 */
  value: number
  /** 占比(%) */
  percent: number
  /** 环比(%) */
  valueMoM: number
  /** 同比(%) */
  valueYoY: number
}

// 旅程分析API响应类型 - 使用全局 BaseResponse 类型保持项目一致性
export type ResultCommonReportInvoke = BaseResponse<any>
export type ResultVoiceUserTop = BaseResponse<VoiceUserTopVo[]>
export type ResultJourneyDetailAnalysis = BaseResponse<JourneyDetailAnalysisVo[]>
export type ResultUserTypeDistribution = BaseResponse<UserTypeDistributionVo[]>
export type ResultIntentionOpinionTop = BaseResponse<IntentionOpinionTopVo>
export type ResultUserFocusSceneTop = BaseResponse<UserFocusSceneTopVo[]>
export type ResultSurgingSceneTop = BaseResponse<SurgingSceneTopVo[]>
export type ResultRegionDistribution = BaseResponse<RegionDistributionVo[]>
export type ResultProductBrief = BaseResponse<ProductBriefVo>
export type ResultHighFreqSceneTop = BaseResponse<HighFreqSceneTopVo[]>
export type ResultGenderDistribution = BaseResponse<GenderDistributionVo[]>
export type ResultFocusSceneTop = BaseResponse<FocusSceneTopVo[]>
export type ResultDataTrendChange = BaseResponse<DataTrendChangeVo[]>
export type ResultDataSourceAnalysis = BaseResponse<DataSourceAnalysisVo[]>
export type ResultChannelNegativeTrend = BaseResponse<ChannelNegativeTrendVo[]>
export type ResultChannelMentionShare = BaseResponse<ChannelShareVo[]>
export type ResultAgeDistribution = BaseResponse<AgeDistributionVo[]>
