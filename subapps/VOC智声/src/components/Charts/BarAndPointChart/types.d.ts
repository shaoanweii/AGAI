/**
 * BarAndPointChart 组件类型定义
 */

import type { ChartInstance } from '@/types/chart'

// 组件实例接口
export interface BarAndPointChartInstance {
  downloadChart: (chartName: string) => void
  chartInstance: ChartInstance | null
}

// 组件Props接口
export interface BarAndPointChartProps {
  data: {
    data: Array<Record<string, any>>
    xDataKey: string
    seriesDataKey: Array<{
      name: string
      key: string
      type?: 'bar' | 'scatter' | 'line'
    }>
    chart?: string
  }
  width?: string | number
  height?: string | number
  needDetails?: boolean
  yAxisName?: string | string[]
  isTwoYaxis?: boolean
}

// 组件事件接口
export interface BarAndPointChartEvents {
  seeDetail: (data: { name: string; seriesName?: string }) => void
  drill: (data: { name: string }) => void
}
