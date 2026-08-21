import type { BatchEventMobilePageVo } from '@h5/api/batchEvent'

// 任务事件类型：用于决定列表卡片进入单点或批量详情路由
export type TaskEventType = 'batch' | 'single'

// 单条任务事件记录
export interface TaskEventItem extends BatchEventMobilePageVo {
  /** 事件ID（列表中的 id，进入详情页需要） */
  id: string

  /** 事件信息（用于卡片标题展示） */
  eventInfo?: string

  /** 事件状态编码（例如 10 / 20 / 30 / 40 / 90） */
  statusCode: number | string
  /** 事件状态展示文案 */
  statusLabel: string
  /** 状态点颜色（由状态编码通过映射计算得到） */
  statusColor: string

  /** 品牌-车系-观点 | 标准观点 | 用车场景 | 用户类型（聚合展示文本） */
  intentionSummary?: string

  /** 处理人员 */
  handlerName?: string
  /** 主责部门展示名称 */
  primaryDepDisplayName?: string
  /** 优先级色板 key */
  priorityColorKey?: string
  /** 底部元信息展示项 */
  metaItems?: string[]

  /** 原声ID - 确保存在 */
  dataId?: string
  /** 事件名称 - 确保存在 */
  eventName?: string
  /** 预警事件编号 - 确保存在 */
  warningEventNo?: string
  /** 主题分类名称 - 确保存在 */
  subjectCategoryName?: string
  /** 事件优先级 - 确保存在 */
  eventPriority?: string
  /** 事件优先级名称 - 确保存在 */
  eventPriorityName?: string
  /** 事件等级 - 确保存在 */
  eventLevel?: string
  /** 事件等级名称 - 确保存在 */
  eventLevelName?: string
  /** 预警时间 - 确保存在 */
  warningTime?: string
  /** 主责部门名称 - 确保存在 */
  mainRespOrgName?: string
  /** 主责部门名称 - 新版批量事件字段 */
  primaryDepName?: string
  /** 任务状态名称 - 确保存在 */
  taskStatusName?: string
}

// 列表查询参数（后续可与后端接口对齐）
export interface TaskEventListQuery {
  /** 当前页码，从 1 开始 */
  pageNum: number
  /** 每页条数 */
  pageSize: number
  /** 开始时间（示例：2025-12-01 00:00:00） */
  startTime?: string
  /** 结束时间（示例：2025-12-15 23:59:59） */
  endTime?: string

  /** 透传外层的筛选条件：品牌、时间、公私域等 */
  [key: string]: any
}

// 列表接口返回结构
export interface TaskEventListResult {
  list: TaskEventItem[]
  total: number
}

// 组件 Props
export interface EventListCardProps {
  /**
   * 基础请求参数
   * - 预计来自任务事件页面的 hub.requestParams
   * - 内部仅做透传，不修改原对象
   */
  baseRequestParams?: Record<string, any>

  /** 当前顶部事件类型筛选，用于决定点击事件卡片进入哪个详情页 */
  eventType?: TaskEventType
}
