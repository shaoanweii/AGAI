/**
 * 任务事件状态筛选组件类型定义
 */
// 事件状态筛选 key 类型
export type EventStatusFilterKey = string

// 单个状态选项
export interface EventStatusFilterOption {
  /** 业务 key，用于后续和接口字段做映射 */
  key: string
  /** 展示文案 */
  label: string
  /** 状态编码 */
  codes?: string | string[]
}

// 组件 Props
export interface EventStatusFilterProps {
  /** 是否禁用 */
  disabled?: boolean
  /** 自定义状态选项；不传时使用默认任务事件状态 */
  options?: EventStatusFilterOption[]
}

// 组件 Emits
export interface EventStatusFilterEmits {
  /**
   * 选中项变化
   * @param e 事件名
   * @param option 当前选中的选项
   */
  (e: 'change', option: EventStatusFilterOption): void
}
