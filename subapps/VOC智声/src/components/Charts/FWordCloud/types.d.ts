// 词云数据项
export interface WordCloudDataItem {
  name: string // 词汇名称
  value: number // 词汇权重值
  [key: string]: any // 扩展字段
}

// 字段映射配置
export interface WordCloudFieldMapping {
  nameField: string // 名称字段，默认 'name'
  valueField: string // 数值字段，默认 'value'
}

// tooltip 格式化函数类型
export type TooltipFormatter = (params: any) => string

// 强调效果配置
export interface EmphasisStyle {
  shadowBlur?: number
  shadowColor?: string
}

// 词云组件 Props
export interface FWordCloudProps {
  data: WordCloudDataItem[]
  width?: string
  height?: string
  sizeRange?: [number, number]
  gridSize?: number
  left?: string
  top?: string
  fontFamily?: string
  fontWeight?: string | number
  theme?: string
  tooltip?: boolean
  tooltipFormatter?: TooltipFormatter
  fieldMapping?: WordCloudFieldMapping
  drawOutOfBound?: boolean
  emphasis?: EmphasisStyle
}
