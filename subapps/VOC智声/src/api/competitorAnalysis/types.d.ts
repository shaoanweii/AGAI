export interface brandCarSeriesItem {
  code: string
  name: string
}

export interface HighestBrandCarVo {
  self: brandCarSeriesItem | undefined
  competitor: brandCarSeriesItem | undefined
}

/**
 * @description: 简报
 * @return {*}
 */
export interface ComparativeBriefVo {
  /** 市场均值/品牌/车系名称 */
  name?: string

  /** 市场均值/品牌/车系编码 */
  code?: string

  /** 市场均值/品牌/车系对应的图片链接 */
  imgUrl?: string

  /** 负面率，单位：%，保留一位小数 */
  negativeRate?: number

  /** 负面率环比变化，单位：%，保留一位小数 */
  negativeRateMoM?: number

  /** 负面率同比变化，单位：%，保留一位小数 */
  negativeRateYoY?: number

  /** 提及量（舆情讨论次数） */
  mentions?: number

  /** 提及量环比变化，单位：%，保留一位小数 */
  mentionsMoM?: number

  /** 提及量同比变化，单位：%，保留一位小数 */
  mentionsYoY?: number

  /** 负面率字段的文字颜色（如：#FF0000、red） */
  rateColor?: string

  /** 负面率字段的背景颜色（如：#FEEEEE、rgba(255,0,0,0.1)） */
  rateBackgroundColor?: string
}

/**
 * 品牌车系单条舆情数据项类型（ItemVo）
 * 对应单个品牌车系在某个时间点的舆情详情
 */
interface ItemVo {
  /** 时间轴（根据维度：天或月），单个时间点（格式示例：2025-11-07 或 2025-11） */
  date?: string

  /** 品牌车系编码 */
  code?: string

  /** 品牌车系名称 */
  name?: string

  /** 负面率，单位：% */
  negativeRate?: number

  /** 负面率环比变化，单位：% */
  negativeRateMoM?: number

  /** 负面率同比变化，单位：% */
  negativeRateYoY?: number

  /** 提及量（舆情讨论次数） */
  mentions?: number

  /** 提及量环比变化 */
  mentionsMoM?: number

  /** 提及量同比变化 */
  mentionsYoY?: number
}

/**
 * @description: 趋势变化
 * @return {*}
 */
export interface TrendVo {
  /** 时间轴（根据维度：天或月），单个时间点（格式示例：2025-11-07 或 2025-11） */
  date?: string

  /** 该时间点对应的所有品牌车系舆情数据集合 */
  items?: ItemVo[]
}

export interface TagAnalysisVo {
  /** 名称 */
  name?: string
  /** 编码 */
  code?: string
  /** 图片URL */
  imageUrl?: string
  /** 一级标签名称 */
  tag1Name?: string
  /** 一级标签编码 */
  tag1Code?: string
  /** 二级标签名称 */
  tag2Name?: string
  /** 二级标签编码 */
  tag2Code?: string
  /** 负面率/提及量（根据业务场景二选一） */
  value1?: number
  /** 负面率/提及量 环比（悬浮提示时展示） */
  value1MoM?: number
  /** 负面率/提及量 同比（悬浮提示时展示） */
  value1YoY?: number
  /** 提及量/负面率环比 或 提及量环比（根据业务场景二选一） */
  value2?: number
  /** 提及量环比（悬浮提示时展示） */
  value2MoM?: number
  /** 提及量同比（悬浮提示时展示） */
  value2YoY?: number
  /** 比率颜色（用于标识正负趋势或数值区间的颜色值） */
  rateColor?: string
  /** 负面率背景颜色（用于负面率数值区域的背景色渲染） */
  rateBackgroundColor?: string
}

/**
 * 场景TOP数据项类型（嵌套在 sceneTopVos 数组中）
 * 仅定义外层引用，具体字段可根据实际业务扩展
 */
interface SceneTopVo {
  /** 场景名称 */
  scenario?: string
  /** 场景提及量 */
  mentions?: number
  /** 场景提及量环比 */
  mentionsMoM?: number
  /** 场景提及量同比 */
  mentionsYoY?: number
  /** 场景负面率（百分比形式，保留一位小数） */
  negativeRate?: number
  /** 场景负面率环比 */
  negativeRateMoM?: number
  /** 场景负面率同比 */
  negativeRateYoY?: number
  /** 提及量环比数据组（百分比形式，保留两位小数） */
  mentionsMoMGroup?: string[]
  /** 负面率环比数据组（百分比形式，保留两位小数） */
  negativeRateMoMGroup?: string[]
  /** 比率颜色（用于标识数据趋势正负或数值区间的颜色值，如红色/绿色） */
  rateColor?: string
  /** 负面率背景颜色（用于负面率数值展示区域的背景色渲染） */
  rateBackgroundColor?: string
  /** 四级标签编码 */
  tag4Code?: string
  /** 三级标签编码 */
  tag3Code?: string
}

/**
 * 观点TOP数据项类型（嵌套在 opinionTopVos 数组中）
 * 仅定义外层引用，具体字段可根据实际业务扩展
 */
interface IntentionOpinionTopVo {
  /** 观点名（核心观点描述） */
  opinion?: string
  /** 情感倾向（如：正面/负面/中性，具体取值需结合业务场景） */
  sentiment?: string
  /** 该观点的提及量 */
  mentions?: number
  /** 提及量环比（百分比形式，保留一位小数） */
  mentionsMoM?: number
  /** 提及量同比（百分比形式，保留一位小数） */
  mentionsYoY?: number
  /** 关联事件列表（与该观点相关的事件描述集合） */
  remark?: string[]
  /** 品牌编码（品牌唯一标识） */
  brandCode?: string
  /** 品牌名称 */
  brandName?: string
}

export interface SceneComparisonVo {
  /** 编码（唯一标识字段） */
  code?: string
  /** 名称（如品牌名、产品名等） */
  name?: string
  /** 图片URL（资源访问地址） */
  imgUrl?: string
  /** 场景TOP列表（存储各场景下的核心统计数据） */
  sceneTopVos?: SceneTopVo[]
  /** 观点TOP列表（存储各核心观点的相关数据） */
  opinionTopVos?: IntentionOpinionTopVo[]
}

export interface SourceCompareVo {
  /** 品牌图片URL（备注：暂无图片时可能返回空字符串或 undefined） */
  imgUrl?: string
  /** 品牌名称 */
  name?: string
  /** 品牌编码（品牌唯一标识） */
  code?: string
  /** 渠道名称（数据统计对应的渠道，如APP、小程序、社交媒体等） */
  channelName?: string
  /** 渠道编码（渠道唯一标识） */
  channelCode?: string
  /** 提及量（品牌在该渠道下的被提及次数） */
  mentions?: number
  /** 提及量环比（与上一统计周期相比的变化率） */
  mentionsMoM?: number
  /** 提及量同比（与去年同期统计周期相比的变化率） */
  mentionsYoY?: number
  /** 负面率（百分比形式，品牌在该渠道下负面提及占比） */
  negativeRate?: number
  /** 负面率环比（与上一统计周期相比的负面率变化率） */
  negativeRateMoM?: number
  /** 负面率同比（与去年同期统计周期相比的负面率变化率） */
  negativeRateYoY?: number
  /** 比率颜色（用于标识数据趋势正负或数值区间的颜色值，如红色标识下降、绿色标识上升） */
  rateColor?: string
  /** 负面率背景颜色（用于负面率数值展示区域的背景色渲染，提升视觉区分度） */
  rateBackgroundColor?: string
}
