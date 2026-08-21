/**
 * 图表组件类型定义
 */

// 图表系列配置接口
export interface ChartSeries {
  name: string // 系列名称
  key: string // 数据键名
  type?: 'bar' | 'scatter' | 'line' // 图表类型
  color?: string // 自定义颜色
  yAxisIndex?: number // Y轴索引（双Y轴时使用）
  // ECharts 系列配置属性
  itemStyle?: any // 图形样式
  barMaxWidth?: number // 柱状图最大宽度
  data?: any // 系列数据
  symbol?: string // 图形标记
  smooth?: boolean // 是否平滑曲线
  symbolSize?: number // 标记大小
  lineStyle?: any // 线条样式
}

// 图表数据接口
export interface ChartData {
  data: Array<Record<string, any>> // 数据数组
  xDataKey: string // X轴数据键名
  seriesDataKey: ChartSeries[] // 系列配置
  chart?: string // 图表类型标识
}

// 图表组件Props接口
export interface BarAndPointChartProps {
  data: ChartData // 图表数据
  divId: string // 图表容器ID
  xNameText?: string // X轴名称文本
  transverse?: boolean // 是否横向显示
  xAxisName?: string // X轴名称
  yAxisName?: string | string[] // Y轴名称
  isTwoYaxis?: boolean // 是否使用双Y轴
  tooltipFormatter?: (params: any) => string // 自定义提示框格式化函数
  needLinkage?: boolean // 是否需要联动功能
}

// 图表事件接口
export interface ChartEvents {
  seeDetail: (data: { name: string; seriesName?: string }) => void // 查看详情事件
  drill: (data: { name: string }) => void // 数据钻取事件
}

// ECharts 配置接口
export interface EChartsOption {
  color?: string[]
  legend?: any
  grid?: any
  tooltip?: any
  xAxis?: any
  yAxis?: any
  series?: any[]
}

// 图表实例接口
export interface ChartInstance {
  setOption: (option: EChartsOption) => void
  resize: () => void
  dispose: () => void
  getZr: () => any
  containPixel: (componentType: string, point: number[]) => boolean
  convertFromPixel: (finder: any, point: number[]) => number[]
  dispatchAction: (action: any) => void
  on: (event: string, handler: (...args: any[]) => void) => void
  off: (event: string) => void
  getOption: () => any
  getDataURL: (options?: any) => string
}

// 数据处理结果接口
export interface ProcessedChartData {
  xDataArr: string[] // X轴数据数组
  seriesData: ChartSeries[] // 处理后的系列数据
  legendData: string[] // 图例数据
}

// 样式配置接口
export interface ChartStyleConfig {
  colors: string[] // 颜色配置
  grid: {
    top: number
    right: number
    bottom: number
    left: number
    containLabel: boolean
  }
  barStyle: {
    borderRadius: number
    borderColor: string
    borderWidth: number
    barMaxWidth: number
  }
  lineStyle: {
    type: string
    color: string
    symbol: string
    smooth: boolean
    symbolSize: number
  }
}

// 工具函数类型
export type DataFormatter = (value: number | string) => string
export type TooltipFormatter = (params: any) => string
