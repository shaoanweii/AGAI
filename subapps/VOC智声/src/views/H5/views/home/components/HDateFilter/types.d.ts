/**
 * 日期筛选组件类型定义
 */
// 日期选项类型
export interface DateOption {
  name: string
  code: number
  startTime: string
  endTime: string
  child?: DateOption[]
}

// 默认时间维度配置（由父组件传入）
export interface DateUnitInfo {
  isDef: boolean
  dateUnit: number | undefined
  dateTime: DateOption | null | undefined
}

// 快捷日期模式
export type QuickRangeMode = 'lastDays' | 'thisQuarter' | 'thisYear'

// 自定义日期弹框内快捷范围配置
export interface QuickRangeConfig {
  /** 唯一 key，用于内部识别和样式绑定 */
  key: string
  /** 展示文案 */
  label: string
  /** 快捷日期计算模式 */
  mode: QuickRangeMode
  /** 模式参数，例如 lastDays 的天数 */
  value?: number
}

// 组件属性类型
export interface HDateFilterProps {
  /**
   * 日期标签页（可选）
   * - 不传或为空时，组件会默认从 H5 权限 Store 中读取 timeDimension 作为 tabs
   */
  tabs?: DateOption[]
  /**
   * 默认时间配置（建议）
   * - 由父组件决定默认选中项（例如来自 H5AppState.dateUnitInfo / dateTaskUnitInfo）
   */
  defaultUnitInfo?: DateUnitInfo
  /** 默认选中的日期类型 */
  defaultType?: number
  /** 默认选中的日期选项 */
  defaultValue?: DateOption
  /** 是否禁用组件 */
  disabled?: boolean
  /** 自定义日期弹框内快捷操作配置 */
  quickRanges?: QuickRangeConfig[]
  /** 需要排除的时间维度 code 列表（例如隐藏季度、本年等） */
  excludeCodes?: number[]
}

// 组件事件类型
export interface HDateFilterEmits {
  /** 日期改变事件 */
  (e: 'change', type: number, option: DateOption): void
  /** 日期类型改变事件 */
  (e: 'typeChange', type: number): void
}
