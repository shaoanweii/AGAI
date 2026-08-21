// 仅聚合常量，便于父/子组件共享与类型推断
export const ConditionType = {
  AD_TYPE: 'AD_type',
  CAR_SERIES: 'carSeries',
  EXPERIENCE_CODE: 'experience_code',
  STANDPOINT: 'standpoint'
} as const

export const InputComponentEnum = {
  SelectSingle: 'select-single',
  SelectMultiple: 'select-multiple',
  Input: 'input',
  CascaderSingle: 'cascader-single',
  CascaderMultiple: 'cascader-multiple'
} as const

export const InputOptionEnum = {
  value: 'value'
} as const

export type ConditionTypeCode = (typeof ConditionType)[keyof typeof ConditionType]
export type InputComponent = (typeof InputComponentEnum)[keyof typeof InputComponentEnum]
