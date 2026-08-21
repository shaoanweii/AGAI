/**
 * 批量事件客户原声观点标签。
 */
export interface BatchEventCustomerVoiceTopic {
  /** 观点名称 */
  topic?: string
  /** 情感倾向 */
  sentiment?: string
  /** 用户意图 */
  intention?: string
}

/**
 * 批量事件客户原声接口原始项。
 * 字段兼容接口可能返回的声音列表命名，方便集中归一化。
 */
export interface BatchEventCustomerVoiceRawItem {
  /** 声音 ID */
  id?: string | number
  /** 声音 ID 兼容字段 */
  newId?: string | number
  /** 原声 ID */
  dataId?: string
  /** 原文 ID */
  originalId?: string
  /** 品牌编码 */
  brandCode?: string
  /** 意图 */
  intent?: string
  /** 标题 */
  title?: string
  /** 原声片段内容 */
  content?: string
  /** 原声片段内容兼容字段 */
  originalTexTScene?: string
  /** 原声片段内容兼容字段 */
  originalTextScene?: string
  /** 客户名称 */
  custName?: string
  /** 客户名称兼容字段 */
  username?: string
  /** 渠道 */
  channel?: string
  /** 渠道兼容字段 */
  channelName?: string
  /** 数据创建时间 */
  dataCreateTime?: string
  /** 数据创建时间兼容字段 */
  evaluateTime?: string
  /** 观点列表 */
  topics?: Array<BatchEventCustomerVoiceTopic | string>
}

/**
 * 批量事件客户原声移动端展示项。
 * 容器组件先完成后端字段归一化，展示组件只消费稳定字段。
 */
export interface BatchEventCustomerVoiceDisplayItem {
  /** 列表渲染主键 */
  id: string
  /** 标题，内容缺失时作为兜底展示 */
  title: string
  /** 原声片段 HTML/文本 */
  content: string
  /** 客户名称 */
  custName: string
  /** 渠道名称 */
  channel: string
  /** 数据创建时间 */
  dataCreateTime: string
  /** 观点标签 */
  topics: BatchEventCustomerVoiceTopic[]
  /** 原始接口数据，保留给后续详情跳转或埋点使用 */
  raw: BatchEventCustomerVoiceRawItem
}
