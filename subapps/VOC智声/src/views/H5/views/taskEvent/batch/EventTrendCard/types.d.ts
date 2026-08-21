/**
 * 任务事件趋势组件类型定义
 */

// 单日事件数数据
export interface EventTrendPoint {
  /** 事件日期（后端字段 dateStr，兼容旧字段 date） */
  dateStr?: string
  /** 旧字段，保留兼容 */
  date?: string
  /** 事件数量（后端字段 counts，兼容旧字段 count） */
  counts?: number
  /** 旧字段，保留兼容 */
  count?: number
}

// 组件 Props
export interface EventTrendCardProps {
  /** 事件趋势数据列表（接口返回数据预留） */
  data?: EventTrendPoint[]
  /** 加载状态 */
  loading?: boolean
}
