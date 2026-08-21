/**
 * 声音列表API请求参数接口
 */
export interface H5VocBaseRequest {
  /** 分页大小 */
  pageSize?: number
  /** 页码 */
  pageNum?: number
  /** 排序 */
  order?: string
  /** 时间范围-开始时间，示例值(2025-08-04) */
  startDate?: string
  /** 时间范围-结束时间，示例值(2025-09-03) */
  endDate?: string
  /** 排序字段，示例值(publish_time) */
  sortField?: string
  /** 排序类型：asc/desc，示例值(desc) */
  sortOrder?: 'asc' | 'desc'
  /**
   * 数据类型：
   * - negativeRate: 负面率
   * - mention: 提及量
   * - brand: 品牌
   * - series: 车系
   * - negativeRateMention: 负面率+提及量
   * - negativeRateMoM: 负面率+负面率环比
   * - mentionMoM: 提及量+提及量环比
   */
  dataType?:
    | 'negativeRate'
    | 'mention'
    | 'brand'
    | 'series'
    | 'negativeRateMention'
    | 'negativeRateMoM'
    | 'mentionMoM'
    | string
  /** 标签一级编码 */
  tag1Code?: string
  /** 标签二级编码 */
  tag2Code?: string
  /** 标签三级编码 */
  tag3Code?: string
  /** 标签四级编码 */
  tag4Code?: string
  /** 观点 */
  topic?: string
  /** 情感 */
  sentiment?: string
  /** 品牌编码 */
  brandCode?: string
  /** 品牌数据类型 */
  brandDataType?: number
  /** 本品品牌编码集合 */
  brandCodeList?: string[]
  /** 车系编码 */
  carSeriesCode?: string
  /** 本品车系集合 */
  carSeriesList?: string[]
  /** 竞品品牌集合 */
  compBrandCodeList?: string[]
  /** 竞品车系集合 */
  compCarSeriesList?: string[]
  /** 意图 */
  intention?: string
  /** 内容类型：工单，咨询。。。 */
  contentType?: string
  /** 省份编码，可多选 */
  provinceCodeSet?: string[]
  /** 来源渠道分类，一级分类：channel1，二级分类：channel2 */
  sourceChannelClass?: 'channel1' | 'channel2' | string
  channelCatagory?: string
  /**
   * 时间 移动端使用：
   * - 当dateUnit为2时，0:本周 -1:上周
   * - 当dateUnit为3时，1:1月，2:2月
   * - 当dateUnit为4时，1:第一季度，2:第二季度
   * - 当dateUnit为4时，2025:2025年，2024:2024年
   */
  dateUnit?: number
  time?: number
  // 原文id
  originalId?: string
  /** 声音ID */
  newId?: string

  filterItems?: any
}

/**
 * 声音列表项接口
 */
export interface VoiceListItem {
  /** 声音ID */
  id: string
  /** 数据唯一标识 */
  dataId: string
  /** 渠道编码 */
  channelCode: string
  /** 原文id */
  originalId: string
  /** 声音片段内容 */
  originalTexTScene: string
  /** 渠道名称 */
  channel: string
  /** 品牌编码 */
  brandCode: string
  /** 品牌名称 */
  brand: string
  /** 车系编码 */
  carSeriesCode: string
  /** 车系名称 */
  carSeries: string
  /** 情感 */
  sentiment: string
  /** 意图 */
  intention: string
  /** 数据产生时间 */
  dataCreateTime: string
  /** oneId */
  oneId: string
  /** 客户姓名 */
  custName: string
  /** 观点 */
  topics: any[]
  title: string
  content: string
}

/**
 * 声音类型枚举
 */
export type VoiceType = 'negative' | 'consult' | 'positive' | 'praise'

/**
 * 场景类型枚举
 */
export type SceneType = 'workOrder' | 'brand' | 'model'

/**
 * API返回的声音数据项接口
 */
export interface ApiVoiceItem {
  /** 新ID */
  newId?: string
  /** 渠道编码 */
  channelCode?: string
  /** 渠道名称 */
  channelName?: string
  /** 原文场景 */
  originalTextScene?: string
  /** 评估时间 */
  evaluateTime?: string
  /** 用户ID */
  userId?: string
  /** 用户名 */
  username?: string
  /** 观点数组 */
  opinion?: string[]
  /** 扩展字段 */
  ext?: Array<{
    name: string
    value: string
  }>
}

// 扩展字段接口
export interface ExtVo {
  // 扩展字段名称
  name?: string
  // 扩展字段值
  value?: string
}

// 用户动态评价详情接口
export interface UserVoiceVo {
  // 原始声音
  originalTextScene?: string
  // 浏览时长
  browsingDuration?: string
  // 观点
  opinion?: string[]
  // 声音id
  newId?: string
  // 用户id
  userId?: string
  // 用户名
  username?: string
  // 渠道名称
  channelName?: string
  // 渠道编码
  channelCode?: string
  //brandName\
  brandName?: string
  carSeriesName?: string
  // 评价时间
  evaluateTime?: string
  originalId?: string
  intent?: string
  quality?: string
  title?: string
  topics?: any[]
  soundslist?: any[]
  relationEvents?: any[]
  // 扩展字段
  ext?: ExtVo[]
}

export interface userBrowseRecordVoParam {
  // 主键
  id?: string
  // 声音id
  soundId?: string
  // 原文id
  originalId?: string
  // 浏览人id
  browseUserId?: string
  // 创建时间
  createTime?: string
  // 浏览时长(秒)
  browseDuration?: number
  // 声音意图
  soundIntention?: string
}

// 浏览数据接口
export interface BrowseDataVo {
  // 总浏览量
  totalCount?: number
  // 抱怨量
  complainCount?: number
  // 咨询量
  consultCount?: number
  // 建议量
  suggestionCount?: number
  // 表扬量
  praiseCount?: number
}

// 任务完成率接口
export interface TaskCompletionVo {
  // 任务完成率
  completionRate?: number
  // 累计浏览时长
  totalBrowsingDuration?: string
  // 浏览数据
  browseData?: BrowseDataVo

  // 剩余数量
  remainingCount?: number
  // 剩余时间
  remainingTime?: string
}

// 浏览趋势接口
export interface BrowseTrendVo {
  // 时间轴
  dateTime?: string
  // 总浏览量
  totalCount?: number
  // 抱怨量
  complainCount?: number
  // 咨询量
  consultCount?: number
  // 建议量
  suggestionCount?: number
  // 表扬量
  praiseCount?: number
}

export interface BrowseRecordVo {
  // 时间轴
  dateTime?: string
  // 浏览时长
  browseDuration?: string
  // 声音id
  soundId?: string
  // 原文id
  originalId?: string
  // 声音内容
  originalTexTScene?: string
  // 观点
  topics?: string[]
}

/**
 * 字典项接口
 */
export interface DictItemVo {
  /** 字典项ID */
  id: string
  /** 字典ID */
  dictId: string
  /** 字典项文本 */
  itemText: string
  /** 字典项英文文本 */
  itemTextEn: string
  /** 字典项键 */
  itemKey: string
  /** 字典项值 */
  itemValue: string
  /** 描述 */
  description: string
  /** 排序 */
  sortOrder: number
  /** 状态（1启用 0不启用） */
  status: number
  /** 创建人 */
  operator: string
  /** 创建时间 */
  createTime: string
}
