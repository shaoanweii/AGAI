/**
 * 趋势概览卡片中的单个指标项。
 */
export interface TrendSummaryMetric {
  /** 指标名称 */
  label: string
  /** 指标展示值 */
  value: string
  /** 环比/标签文案 */
  tag: string
  /** 数值附加样式类 */
  valueClassName?: string
}

/**
 * 趋势概览卡片定义。
 */
export interface TrendSummaryCardItem {
  /** 唯一标识 */
  key: string
  /** 图标名称 */
  icon: string
  /** 图标颜色 */
  iconColor?: string
  /** 卡片附加样式类 */
  customClass?: string
  /** 卡片内指标列表 */
  metrics: TrendSummaryMetric[]
}
