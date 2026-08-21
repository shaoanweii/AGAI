/**
 * 通用类型定义文件
 * 定义项目中通用的全局类型，无需手动导入
 */

/**
 * 年龄范围接口
 */
declare interface AgeRange {
  [key: string]: any
}

/**
 * 提及量/负面率切换类型
 * 用于品牌趋势、渠道趋势、下钻图表等指标切换场景。
 */
declare type MentionNegativeRateType = 'negativeRate' | 'mention'

/**
 * VoC 通用查询参数接口
 * 用于所有 VoC 相关的 API 查询
 * 基于 ExtendComQueryModel 更新，包含用户意图观点TOP接口的所有参数
 */
declare interface VocQueryParams {
  /** 开始时间 */
  startDate?: string
  /** 结束时间 */
  endDate?: string
  /** 用户ID */
  userId?: string
  /** ONE_ID（用户唯一标识，用于用户详情/轨迹等接口） */
  oneId?: string

  /** 观点 */
  topic?: string

  /** 意图字段：抱怨、投诉、建议、咨询、表扬、陈述 */
  intention?: string

  /** 情感字段：(负面/正面/中性) */
  sentiment?: string | string[]
  /** 情感字段集合：(负面/正面/中性) */
  sentimentList?: string | string[]
  /** 情感程度，仅在负面情感分支查询中使用 */
  emotionalLevel?: string | number

  /** 主帖用户 */
  retweetedName?: string
  /** 主帖用户 ID */
  retweetedUserId?: string

  /** 性别字段：(男/女) */
  gender?: string

  /** 客户类型 */
  custType?: string

  /** 数据类型：negativeRate/mention (负面率/提及量),brand/series (品牌/车系),negativeRateMention (负面率+提及量),negativeRateMoM (负面率+负面率环比),mentionMoM (提及量+提及量环比) */
  dataType?: string
  /** 数据类型：brand/series (品牌/车系)  同时筛选多个数据类型时，将品牌/车系 做单独字段*/
  queryType?: string
  /** 排序字段,示例值(publish_time) */
  sortField?: string
  /** 排序类型：asc/desc,示例值(desc) */
  sortOrder?: string

  /** 标签一级编码 */
  tag1Code?: string
  /** 标签二级编码 */
  tag2Code?: string
  /** 标签三级编码 */
  tag3Code?: string
  /** 标签四级编码 */
  tag4Code?: string
  /** 客户体验代码一级编码集合 */
  firstCodeTag?: string[]
  /** 客户体验代码二级编码集合 */
  secondCodeTag?: string[]
  /** 客户体验代码三级编码集合 */
  threeCodeTag?: string[]
  /** 客户体验代码四级编码集合 */
  fourCodeTag?: string[]
  /** 省份编码集合 */
  provinceCodeSet?: string[]
  custProvinceCodeSet?: string[]
  /** 来源渠道分类，一级分类：channel1，二级分类：channel2 */
  sourceChannelClass?: string

  /** 页码 */
  pageNum?: number
  /** 每页大小 */
  pageSize?: number
  /** 高级筛选 */
  filterItems?: any[]
  /** 品牌编码 */
  brandCode?: string
  /** 品牌编码集合 */
  brandCodeList?: string[]
  /** 车系编码 */
  carSeriesCode?: string
  /** 车系编码集合 */
  carSeriesList?: string[]
  /** 竞品品牌编码集合 */
  compBrandCodeList?: string[]
  /** 竞品车系编码集合 */
  compCarSeriesList?: string[]
  /** 声音ID */
  newId?: string
  /** 原文id */
  originalId?: string
  /** 搜索关键词 */
  searchKeywords?: string
  /* 特殊场景：如同一页面两个品牌联动多个模块,需要存储切换 */
  tempCode?: string
  /** 经销商编码 */
  dealerCode?: string
  /** 场景 */
  scenario?: string
  /** 用车场景一级集合 */
  usageScenarioFirstList?: string[]
  /** 用车场景二级集合 */
  usageScenarioSecondList?: string[]
  /** 用车场景前端级联选择值 */
  usageScenarioCodes?: string[] | string[][]

  /** 渠道编码 */
  channelCode?: string
  /** 渠道编码集合 */
  channelIds?: string[]

  /** 车企 */
  seriesFactory?: string

  /** 年龄段 */
  ageCode?: string[]
  // 标签类型
  tagType?: string
  channelCatagory?: string

  /** 内容类型 */
  contentType?: string
  /** 阶段 */
  phase?: string
  /** 新车系列表 */
  newCarSeriesList?: string[]
  /** 新车上市 新品车系选择的对象值 数组格式 方便拓展 */
  newCarSeriesObjList?: NewCarSeriesSelectorObj[]

  /** 新车上市 对比车系 选择的对象值 数组格式 方便拓展 */
  compCarSeriesObjList?: NewCarSeriesSelectorObj[]
  // 重点账号
  keyAccounts?: string[]
}

declare interface NewCarSeriesSelectorObj {
  name: string
  code: string
  id?: string
  /** 预热开始时间 */
  preheatStartTime?: string
  preheatEndTime?: string
  /** 上市时间 */
  launchStartTime?: StringLiteral
  launchEndTime?: string
  /** 稳定时间 */
  stableStartTime?: string
  stableEndTime?: string
}
