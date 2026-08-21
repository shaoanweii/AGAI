/**
 * 性别分布环形图组件类型定义
 */

export interface GenderData {
  /** 男性数量 */
  male: number
  /** 女性数量 */
  female: number
}

export interface GenderDonutChartProps {
  /** 性别数据 */
  data: GenderData
  /** 图表宽度 */
  width?: string
  /** 图表高度 */
  height?: string
}

export interface ChartClickEvent {
  /** 事件名称 */
  name: string
  /** 数据值 */
  value: number
  /** 数据索引 */
  dataIndex: number
  /** 系列名称 */
  seriesName: string
  /** 系列类型 */
  seriesType: string
  /** 系列索引 */
  seriesIndex: number
  /** 数据名称 */
  dataType: string
  /** 百分比 */
  percent: number
} 