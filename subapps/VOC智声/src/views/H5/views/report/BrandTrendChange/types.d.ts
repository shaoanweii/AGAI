/**
 * 品牌趋势变化组件的数据接口（报表页副本）
 */

// 趋势项数据类型
export interface TrendItem {
  /** 负面率名称 */
  negativeRateName: string
  /** 负面率 */
  negativeRate: number
  /** 负面率环比 */
  negativeRateMom: number
  /** 负面率同比 */
  negativeRateYoy: number
  /** 提及量名称 */
  mentionName: string
  /** 提及量 */
  mention: number
  /** 提及量环比 */
  mentionMom: number
  /** 提及量同比 */
  mentionYoy: number
  /** 日期 */
  date: string
  /** 事件 */
  remark: string[]
  /** 负面率颜色 */
  rateColor?: string
  /** 颜色（用于图表显示） */
  color?: string
  /** 是否为百分比（用于显示格式化） */
  isPercent?: boolean
  // 时间
  startTime: string
  // 时间
  endTime: string
}

// 品牌趋势变化组件Props类型
export interface BrandTrendChangeProps {
  showTooltip?: boolean
  /** 趋势数据项 */
  items: TrendItem[]
  /** 标题中的品牌名，显示为【品牌名】 */
  brandName?: string
}

// 品牌趋势变化组件Emits类型
export interface BrandTrendChangeEmits {
  /** 根因分析点击 */
  (e: 'root-cause-click', params: any): void
}
