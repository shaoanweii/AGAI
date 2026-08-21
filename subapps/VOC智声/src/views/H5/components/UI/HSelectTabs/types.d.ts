export type HSelectTabCode = string | number

export interface HSelectTabOption {
  /** 选项编码，作为 v-model 传值 */
  code: HSelectTabCode
  /** 选项展示文案 */
  name: string
  /** 是否禁用 */
  disabled?: boolean
}

/** v-model 值：单选为 code，多选为 code 数组 */
export type HSelectTabsValue = HSelectTabCode | HSelectTabCode[]

export interface HSelectTabsProps {
  /** 外部传入的数据源 */
  options: HSelectTabOption[]
  /** 是否多选 */
  multiSelect?: boolean
  /** 是否禁用整个组件 */
  disabled?: boolean
  /** 自定义样式类名 */
  customClass?: string
  /** “全部”展示文案 */
  allLabel?: string
  /** “全部”对应的 code */
  allCode?: HSelectTabCode
  /** 字段映射（兼容非 code/name 的数据结构） */
  fields?: {
    code?: string
    name?: string
  }
}

export interface HSelectTabsEmits {
  /** 值更新事件 */
  'update:modelValue': [value: HSelectTabsValue]
  /** 选中变化事件 */
  change: [value: HSelectTabsValue, oldValue: HSelectTabsValue]
}

