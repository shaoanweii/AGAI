// 图表数据项类型
export interface ChartDataItem {
  keyWord: string
  positiveMentionValue: number
  neutralMentionValue: number
  negativeMentionValue: number
  experienceValue: number
}

// 组件 Props 类型
export interface BarOrLineChartProps {
  data: ChartDataItem[]
  width?: string | number
  height?: string | number
  needDetails?: boolean
}

// 组件事件类型
export interface BarOrLineChartEvents {
  (e: 'barClick', payload: { date: string }): void
}
