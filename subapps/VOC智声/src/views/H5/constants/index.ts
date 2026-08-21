//根因分析-声音列表 枚举
export enum AnalysisAndVoiceEnum {
  'rootCauseAnalysis' = 'rootCauseAnalysis', //根因分析
  'voiceList' = 'voiceList' //声音列表
}

// h5页面title VOC任务管理
export const PAGE_TITLE = '客户之声'

/**
 * 任务事件相关常量
 */
export const taskStatusLabelMap = {
  '10': '预警初审',
  '11': '预警初审',
  '20': '业务响应',
  '30': '闭环处理',
  '40': '闭环处理',
  '90': '事件关闭'
}

export const taskStatusColorMap = {
  '10': '#FAB007',
  '11': '#FAB007',
  '20': '#1677FF',
  '30': '#28C7C7',
  '40': '#28C7C7',
  '90': '#7298D0'
}

// 事件状态分组元信息（颜色/文案/编码/顺序统一来源）
export interface EventStatusMetaItem {
  key: string
  label: string
  color: string
  /** 该分组包含的后端状态编码集合 */
  codes: string | string[]
}

export const eventStatusMeta: EventStatusMetaItem[] = [
  { key: '10', label: '预警初审', color: '#FAB007', codes: ['10', '11'] },
  { key: '20', label: '业务响应', color: '#1677FF', codes: ['20'] },
  { key: '40', label: '闭环处理', color: '#28C7C7', codes: ['30', '40'] },
  { key: '90', label: '事件关闭', color: '#7298D0', codes: ['90'] }
]

// 任务事件状态编码枚举（与后端编码保持一致）
export enum TaskStatusEnum {
  WarningReview10 = '10',
  WarningReview11 = '11',
  BusinessResponse = '20',
  ClosedLoop30 = '30',
  ClosedLoop40 = '40',
  EventClosed = '90'
}

// 事件状态业务 key 到展示文案映射
export const EVENT_STATUS_KEY_TO_LABEL: Record<string, string> = {
  warningReview: '预警初审',
  businessResponse: '业务响应',
  closedLoop: '闭环处理',
  eventClosed: '事件关闭'
}

// 事件优先级/等级颜色映射：P0-P4 共用一套配置
export const EVENT_PRIORITY_LEVEL_COLORS: Record<string, string> = {
  p0: '#F53F3F',
  p1: '#FE7940',
  p2: '#FAB007',
  p3: '#1677FF',
  p4: '#28C7C7'
}

// 等级文案到 P0-P4 色板的映射（如需调整分级规则，可在此统一修改）
export const EVENT_LEVEL_TO_PRIORITY_KEY: Record<string, string> = {
  'S': '#F53F3F',
  'A': '#FE7940',
  'B': '#FAB007',
  'C': '#1677FF',
  'D': '#28C7C7'
}

/**
 * 处理方式枚举
 * 仅回评-> only reply
 * 仅私信-> only private msg
 * 回评和私信-> reply and private msg
 */
export enum HandleModeEnum {
  // 仅回评
  OnlyReply = 'only reply',
  // 仅私信
  OnlyPrivateMsg = 'only private msg',
  // 回评和私信
  ReplyAndPrivateMsg = 'reply and private msg'
}

// 无效事件
export const INVALID_EVENT = 'invalid'
// 有效事件
export const VALID_EVENT = 'valid'

// 事件优先级与处理建议的映射
export const EventPriorityTipMap = {
  p0: '高优先级处理',
  p1: '中高优先级处理',
  p2: '中优先级处理',
  p3: '中低优先级处理',
  p4: '低优先级处理'
}

// 事件等级与处理建议的映射
export const EventLevelTipMap = {
  S: '特别重大事件',
  A: '重大事件',
  B: '较大事件',
  C: '一般事件'
}
