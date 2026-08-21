export interface HListSingleSelectFields {
  /** 展示字段 */
  label: string
  /** 值字段 */
  value: string
  /** 禁用字段（可选） */
  disabled?: string
}

export interface HListSingleSelectProps<T = any> {
  /** 当前选中的值（null 表示未选择） */
  modelValue: string | number | null
  /** 选项列表 */
  options?: T[]
  /** 字段映射：适配不同接口返回结构 */
  fields?: HListSingleSelectFields
  /** 搜索字段（基于原始 options 元素，仅支持一层 key） */
  searchFields?: string[]
  /** 自定义搜索内容提取函数（优先级高于 searchFields） */
  searchBy?: (item: T) => string
  /** 弹框标题 */
  title?: string
  /** 占位文本 */
  placeholder?: string
  /** 是否禁用 */
  disabled?: boolean
  /** 是否支持搜索 */
  searchable?: boolean
}

export interface HListSingleSelectEmits {
  /** v-model 更新 */
  'update:modelValue': [value: string | number | null]
  /** 值变化事件（选中/取消都会触发） */
  change: [value: string | number | null, oldValue: string | number | null]
}
