/**
 * 水平分段条形图组件类型定义
 */

// 分段数据项
export interface SegmentData {
  /** 分段名称 */
  title: string
  value: string
  percent?: string
  valueMoM?: string
  valueYoY?: string
  color?: string
}

// 组件 Props
export interface HorizontalSegmentedBarProps {
  /** 分段数据数组 */
  data: SegmentData[]
  /** 图表宽度 */
  width?: string
  /** 图表高度 */
  height?: string

  /** 自定义颜色数组 */
  colors?: string[]
  /** 左侧类目轴显示文本（不传则回退到 ageGroup 或空字符串） */
  categoryLabel?: string
  /** 是否显示左侧类目轴标签（默认不显示） */
  showCategoryLabel?: boolean,
  /** 是否显示图表图例（默认不显示） */
  showLegend?: boolean
}

// 组件事件
export interface HorizontalSegmentedBarEvents {
  /** 图表点击事件 */
  chartClick: [params: any]
}
