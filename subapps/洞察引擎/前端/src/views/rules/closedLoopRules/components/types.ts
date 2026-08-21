import type { ConditionTypeCode } from './constants'

// 条件行的数据结构（与原 createCond 保持一致）
export interface ConditionRow {
  conditionType: ConditionTypeCode | string
  operator: string
  option: string
  valueType: string
  value: any
  sortOrder: number
  key?: string | number
}
