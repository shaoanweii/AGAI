// 饼图数据项
export interface PieDataItem {
  name: string
  value: number
  [key: string]: any
}

// 字段映射配置
export interface PieFieldMapping {
  nameField: string // 名称字段，默认 'name'
  valueField: string // 数值字段，默认 'value'
}

// tooltip 格式化函数类型
export type TooltipFormatter = (params: any) => string

// 标签位置类型
export type LabelPosition = 'inside' | 'outside' | 'center'

// 饼图组件 Props
export interface FPieChartProps {
  data: PieDataItem[]
  width?: string
  height?: string
  radius?: string | string[]
  center?: string[]
  labelPosition?: LabelPosition
  showLabel?: boolean
  showPercentage?: boolean
  theme?: string
  tooltipFormatter?: TooltipFormatter
  fieldMapping?: PieFieldMapping
}
