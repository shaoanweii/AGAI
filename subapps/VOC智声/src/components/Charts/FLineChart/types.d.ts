export interface FieldMapping {
  xAxis: string
  series: SeriesConfig[]
}

export interface SeriesConfig {
  name: string
  field: string
  color?: string
}

export interface LineChartProps {
  data: Array<Record<string, any>>
  fieldMapping: FieldMapping
  width?: string
  height?: string
  smooth?: boolean
  showSymbol?: boolean
  showLegend?: boolean
  showGrid?: boolean
  tooltipFormatter?: (params: any) => any
  clickable?: boolean
}

export type TooltipFormatter = (params: any) => string | HTMLElement
