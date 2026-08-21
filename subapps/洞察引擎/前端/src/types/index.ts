/**
 * 数据字典
 */
export interface Conditions {
  key: string
  details: ConditionsDetailItem[]
}

export interface ConditionsDetailItem {
  key: string
  value: string
  children?: ConditionsDetailItem[]
  [props: string]: any
}

export interface ConditionsByType {
  classifyCode: string
  classifyName: string
  typeCode: string
  typeName: string
}

/**
 * 视图状态类型
 */
export type ViewStatus = 'add' | 'edit' | 'view'

export interface CommonObj {
  [key: string]: any
}
