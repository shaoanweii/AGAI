/**
 * 下钻简报卡片数据类型
 * 对应接口: /report/drill-down/getDrillDownBrief
 */
export interface ProductBriefVo {
  /** 负面率，单位% 保留一位小数 */
  negativeRate: number
  /** 负面率环比，单位% 保留一位小数 */
  negativeRateMoM: number
  /** 负面率同比，单位% 保留一位小数 */
  negativeRateYoY: number
  /** 正面率，单位% 保留一位小数 */
  positiveRate: number
  /** 正面率环比，单位% 保留一位小数 */
  positiveRateMoM: number
  /** 正面率同比，单位% 保留一位小数 */
  positiveRateYoY: number
  /** 负面提及量 */
  negativeMentions: number
  /** 负面提及量环比，单位% 保留一位小数 */
  negativeMentionsMoM: number
  /** 正面提及量 */
  positiveMentions: number
  /** 正面提及量环比，单位% 保留一位小数 */
  positiveMentionsMoM: number
  /** 提及量 */
  mentions: number
  /** 提及量环比，单位% 保留一位小数 */
  mentionsMoM: number
  /** 提及量同比，单位% 保留一位小数 */
  mentionsYoY: number
  /** 用户数（去重 one_id） */
  users: number
  /** 用户数环比，单位% 保留一位小数 */
  usersMoM: number
  /** 用户数同比，单位% 保留一位小数 */
  usersYoY: number
  /** 文字颜色 */
  rateColor?: string
  /** 背景颜色 */
  rateBackgroundColor?: string
}

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
  /** 文字颜色 */
  rateColor?: string
  /** 背景颜色 */
  rateBackgroundColor?: string
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
/**
 * 车系相关API类型定义
 */

/**
 * 车系排行数据接口（来自 /report/drill-down/car-series-rank）
 */
export interface CarSeriesRankItem {
  /** 车系名称 */
  carSeriesName: string
  /** 车系编码 */
  carSeriesCode: string
  /** 负面率，% 两位小数 */
  negativeRate: number
  /** 提及量 */
  mentions: number
  /** 环比，% 两位小数 */
  mom: number
  /** 同比，% 两位小数 */
  yoy: number
}

/**
 * 车系列表数据接口（来自 /report/drill-down/car-series-list）
 */
export interface CarSeriesListItem {
  /** 品牌名称 */
  brandName: string
  /** 品牌编码 */
  brandCode: string
  /** 车系名称 */
  carSeriesName: string
  /** 车系编码 */
  carSeriesCode: string
  /** 负面率，% 两位小数 */
  negativeRate: number
  /** 提及量 */
  mentions: number
  /** 提及量占比，% 两位小数 */
  mentionsShare: number
  /** 提及-趋势 */
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
  /** 文字颜色 */
  rateColor?: string
  /** 背景颜色 */
  rateBackgroundColor?: string
}

/**
 * 观点分析数据类型
 */
export interface OpinionAnalysisVo {
  /** 观点 */
  opinion: string
  /** 意图 */
  intention: string
  /** 提及量 */
  mentions: number
  /** 提及量占比，% 两位小数 */
  mentionsShare: number
  /** 趋势 */
  mentionTrend: string[]
  /** 环比，% 两位小数 */
  mentionsMoM: number
  /** 请求标识 */
  tid: string
}

/**
 * 场景分析数据类型
 */
export interface ScenarioAnalysisVo {
  /** 场景 */
  scenario: string
  /** 提及量 */
  mentions: number
  /** 提及量占比，% 两位小数 */
  mentionsShare: number
  /** 趋势 */
  mentionTrend: string[]
  /** 环比，% 两位小数 */
  mentionsMoM: number
}

/**
 * 指标排行数据类型
 */
export interface IndicatorRankVo {
  /** 标签名称 */
  tagName: string
  /** 标签编码 */
  tagCode: string
  /** 数值（负面率% 两位小数 或 提及量） */
  value: number
  /** 环比，% 两位小数 */
  valueMom: string
  /** 同比，% 两位小数 */
  valueYoy: string
  tagLevel: number
}

/**
 * 指标列表数据类型
 */
export interface IndicatorListVo {
  /** 标签名称 */
  tagName: string
  /** 标签编码 */
  tagCode: string
  /** 提及量 */
  mentions: number
  /** 提及量占比，% 两位小数 */
  mentionsShare: number
  /** 提及-趋势 */
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
  /** 文字颜色 */
  rateColor?: string
  /** 背景颜色 */
  rateBackgroundColor?: string
}

/**
 * 地域分析 - 省份地图数据项
 * 接口: /report/drill-down/getProvinceMap
 */
export interface ProvinceMapItem {
  /** 省份名称 */
  provinceName: string
  /** 省份编码 */
  provinceCode: string
  /** 负面率，% 两位小数 */
  negativeRate: number
  /** 负面率环比，% 两位小数 */
  negativeRateMoM: number
  /** 负面率同比，% 两位小数 */
  negativeRateYoY: number
  /** 提及量 */
  mentions: number
}

/**
 * 地域分析 - 经销商评价排行TOP数据项
 * 接口: /report/drill-down/getDealerRankTop
 */
export interface DealerRankTopItem {
  /** 经销商名称 */
  dealerName: string
  /** 经销商编码 */
  dealerCode: string
  /** 省份名称 */
  provinceName: string
  /** 省份编码 */
  provinceCode: string
  /** 负面率，% 两位小数 */
  negativeRate: number
  /** 负面率环比，% 两位小数 */
  negativeRateMoM: number
  /** 负面率同比，% 两位小数 */
  negativeRateYoY: number
  /** 提及量 */
  mentions: number
  /** 提及量环比，% 两位小数 */
  mentionsMoM: number
  /** 提及量同比，% 两位小数 */
  mentionsYoY: number
}

/**
 * 地域分析 - 区域列表数据项
 * 接口: /report/drill-down/data-province-list
 */
export interface ProvinceListItem {
  /** 名称 */
  name: string
  /** 编码 */
  code: string
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
  /** 文字颜色 */
  rateColor?: string
  /** 背景颜色 */
  rateBackgroundColor?: string
}

/**
 * 声音列表 返回项
 * 接口: /report/voc-sounds/getVocListSounds
 */
export interface VoiceSoundItem {
  id: string
  dataId: string
  channelCode: string
  originalId: string
  originalTexTScene: string
  title: string
  channel: string
  brandCode: string
  brand: string
  carSeriesCode: string
  carSeries: string
  sentiment: string
  intention: string
  dataCreateTime: string
  oneId: string
  custName: string
  topics: any[]
}

/**
 * 声音详情返回的数据类型
 * 接口: /report/voc-sounds/getSoundsDetails
 */
export interface SoundsDetailsVo {
  /** 原始声音 */
  originalTextScene: string
  /** 标题 */
  title: string
  /** 文章质量 */
  quality: string
  /** 浏览时长 */
  browsingDuration: string
  /** 观点 */
  opinion: string[]
  /** 声音id */
  newId: string
  /** 原文id */
  originalId: string
  /** 意图 */
  intent: string
  /** 用户id */
  userId: string
  /** 用户名 */
  username: string
  /** 渠道名称 */
  channelName: string
  /** 渠道编码 */
  channelCode: string
  /** 评价时间 */
  evaluateTime: string
  /** 扩展字段 */
  ext: ExtVo[]
  /** 请求标识 */
  tid: string
}

/**
 * 扩展字段类型
 */
export interface ExtVo {
  /** 扩展字段名称 */
  name: string
  /** 扩展字段值 */
  value: string
}

/**
 * 人群特征 - 性别占比
 * 接口: /report/drill-down/getGenderDistribution
 */
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

/**
 * 人群特征 - 年龄段占比
 * 接口: /report/drill-down/getAgeDistribution
 */
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

/**
 * 人群特征 - 用户类型占比
 * 接口: /report/drill-down/getUserTypeDistribution
 */
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

/**
 * 人群特征 - 常住地省份占比
 * 接口: /report/drill-down/getRegionDistribution
 */
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

/**
 * 人群特征 - 用户关注场景TOP10
 * 接口: /report/drill-down/getUserFocusSceneTop
 */
export interface UserFocusSceneTopVo {
  /** 场景/标签 */
  sceneName: string
  /** 负面率/提及量 */
  value: number
  /** 环比 */
  ringRatio: number
  /** 同比 */
  yearOnYearRatio: number
}

/**
 * 人群特征 - 用户列表
 * 接口: /report/drill-down/getUserList
 */
export interface UserListItemVo {
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
  /** 数据源 */
  dataSource: number
  /** 发帖数 */
  postCount: number
  /** 抱怨 */
  complainCount: number
  /** 咨询 */
  consultCount: number
  /** 建议 */
  suggestCount: number
  /** 表扬 */
  praiseCount: number
}

/**
 * 关注场景数据类型
 * 用于用户详情中的关注场景
 */
export interface FocusSceneTopVo {
  /** 场景/标签 */
  sceneName: string
  /** 负面率/提及量 */
  value: number
  /** 环比 */
  ringRatio: number
  /** 同比 */
  yearOnYearRatio: number
}

/**
 * 发声渠道数据类型
 * 用于用户详情中的发声渠道
 */
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

/**
 * 用户详情数据类型
 * 对应接口: /report/drill-down/getUserDetail
 */
export interface UserDetailVo {
  /** 用户昵称 */
  nickname: string
  /** 性别 */
  gender: string
  /** 年龄 */
  age: number
  /** 省份 */
  province: string
  /** 用户类型 */
  userType: string
  /** 手机号码 */
  mobile: string
  /** 车架号 */
  vin: string
  /** 负面率 */
  negativeRate: number
  /** 负面提及量 */
  negative: number
  /** 正面提及量 */
  positive: number
  /** 中性提及量 */
  neutral: number
  /** 发帖数 */
  postNum: number
  /** 渠道数 */
  channelNum: number
  /** 总提及量 */
  totalMentions: number
  /** 抱怨提及量 */
  complaint: number
  /** 咨询提及量 */
  consult: number
  /** 建议提及量 */
  suggest: number
  /** 表扬提及量 */
  praise: number
  /** 关注场景 */
  focusScenes: FocusSceneTopVo[]
  /** 发声渠道 */
  voiceChannels: any[]
}

/**
 * 用户渠道轨迹（Tab数据）
 * 接口: /report/drill-down/getUserDetail-channel-trajectory
 */
export interface UserChannelTrajectoryItem {
  /** 渠道名 */
  channelName: string
  /** 渠道code */
  channelCode: string
  /** 发声数 */
  voiceNum: number
}

/**
 * 用户数据轨迹（时间线列表）
 * 接口: /report/drill-down/getUser-trajectory
 */
export interface UserTrajectoryItem {
  /** 年月，例如 2025-08 */
  yearMonth: string
  /** 月日，例如 08-21 */
  monthDay: string
  /** 渠道名称 */
  channelName: string
  /** 渠道code */
  channelCode: string
  /** 原文id */
  originalId: string
  /** 标题 */
  title: string
  /** 原文内容 */
  content: string
  /** 观点列表 */
  topics: any[]
}
