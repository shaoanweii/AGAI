/**
 * 图表组件导出
 * 提供基于 ECharts 的专业图表组件
 */

/**
 * 柱状图加散点图混合组件
 * 支持柱状图显示主要数据，散点图显示辅助数据
 * 适用于需要同时展示数量分布和趋势点的场景
 *
 * @example
 * <BarAndPointChart
 *   :data="chartData"
 *   :width="600"
 *   :height="400"
 *   :need-details="true"
 *   @bar-click="handleBarClick"
 * />
 */
export { default as BarAndPointChart } from './BarAndPointChart/index.vue'

/**
 * 柱状图和折线图组合组件
 * 支持柱状图显示数量数据，折线图显示趋势数据
 * 适用于需要同时展示绝对值和变化趋势的场景
 *
 * @example
 * <BarOrLineChart
 *   :data="chartData"
 *   :width="600"
 *   :height="400"
 *   :need-details="true"
 *   @bar-click="handleBarClick"
 * />
 */
export { default as BarOrLineChart } from './BarOrLineChart/index.vue'

export { default as SmallLineTrendChart } from './SmallLineTrendChart/index.vue'

/**
 * BarAndPointChart 组件类型定义
 * 包含组件实例、Props 接口和事件类型
 */
export type {
  BarAndPointChartInstance,
  BarAndPointChartProps,
  BarAndPointChartEvents
} from './BarAndPointChart/types'

/**
 * BarOrLineChart 组件类型定义
 * 包含图表数据项、Props 接口和事件类型
 */
export type {
  ChartDataItem,
  BarOrLineChartProps,
  BarOrLineChartEvents
} from './BarOrLineChart/types'
