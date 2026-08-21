/**
 * 任务事件状态分布组件类型定义
 */

// 原始状态统计数据（建议与接口字段保持一致）
export interface EventStatusRawItem {
  /** 事件状态名称（后端字段） */
  taskStatusName?: string
  /** 事件状态编码（后端字段） */
  taskStatus?: string | number
  /** 事件数量（后端字段） */
  currentCounts?: number
  /** 占比（后端字段，单位不确定，当前不作为展示计算的唯一来源） */
  percent?: number

  /** 兼容旧字段：预警状态编码，例如 10、11、20、30、40、90 */
  statusCode?: string | number
  /** 兼容旧字段：对应数量 */
  count?: number
}

// 组件 Props
export interface EventStatusDistributionProps {
  /** 原始状态统计列表（接口返回数据预留） */
  data?: EventStatusRawItem[]
  /** 加载状态 */
  loading?: boolean
}
