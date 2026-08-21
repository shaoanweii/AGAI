// 性别分布图表数据接口
export interface GenderDistributionData {
  male: number
  female: number
}

// 性别分布图表组件 Props
export interface GenderDistributionChartProps {
  data: GenderDistributionData
  width?: string
  height?: string
  theme?: string
}

// 图表事件参数
export interface ChartEventParams {
  name: string
  value: number
  percent: number
  dataIndex: number
} 