export interface HSwitchOption {
  /** 选项值 */
  value: string | number
  /** 选项标签 */
  label: string
  /** 是否禁用 */
  disabled?: boolean
}

export interface HSwitchButtonProps {
  /** 当前选中的值 */
  modelValue: string | number
  /** 切换选项 */
  options: HSwitchOption[]
  /** 是否禁用整个组件 */
  disabled?: boolean
  /** 自定义样式类名 */
  customClass?: string
}

export interface HSwitchButtonEmits {
  /** 值更新事件 */
  'update:modelValue': [value: string | number]
  /** 值变化事件 */
  change: [value: string | number, oldValue: string | number]
}
