// 组件导出文件
// 在这里导出公共组件
/**
 * 数据对比组件
 * 用于显示数据的环比/同比变化情况
 * 支持提及数、体验值、负面提及率三种指标类型
 * 根据数值正负自动显示不同颜色（绿色表示正向，红色表示负向）
 *
 * @example
 * <ShowCompare
 *   :value="0.15"
 *   :type="'mention'"
 *   :show-arrow="true"
 * />
 */
export { default as ShowCompare } from './ShowCompare/index.vue'
export { default as VocTrendChart } from './VocTrendChart/index.vue'
export { default as FCollapseSection } from './UI/FCollapseSection/index.vue'

export * from './Charts'
export * from './appDialog'

// 类型导出
export type * from './Charts'
export type * from './ShowCompare/types.d'
export type * from './DataSourceAnalysis/types.d'

export default {}
