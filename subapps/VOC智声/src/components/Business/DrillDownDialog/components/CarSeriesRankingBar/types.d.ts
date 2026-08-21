/**
 * CarSeriesRankingBar 组件类型定义
 * 车系排行柱状图组件相关接口
 */

// 排行数据项接口
export interface RankItem {
  /** 名称（如：深蓝S09） */
  name: string
  /** 百分比（0-100 或 0-1 都支持），用于"负面率"模式 */
  percent?: number
  /** 数值（用于"提及量"模式） */
  value?: number
  //环比
  mom?: string
  //同比
  yoy?: string
  /** 是否高亮（置顶两个红色示例） */
  highlight?: boolean

  tagLevel?: number
}

// 组件 Props 接口
export interface CarSeriesRankingBarProps {
  /** 数据源 */
  data: RankItem[]
  /** 模式：rate=百分比堆叠（显示百分比），value=按值高度（显示值） */
  mode: MentionNegativeRateType
  title?: string
  /** 图表高度 */
  height?: string
  /** 每根柱宽 */
  barWidth?: number
  /** 是否显示顶部切换器 */
  showSwitcher?: boolean
}

// 组件事件接口
export interface CarSeriesRankingBarEvents {
  /** 更新模式事件 */
  (e: 'update:mode', value: MentionNegativeRateType): void
  /** 柱状图点击事件 */
  (e: 'barClick', data: RankItem): void
  /** 切换模式事件 */
  (e: 'modeChange', mode: MentionNegativeRateType): void
}

// 组件实例接口
export interface CarSeriesRankingBarInstance {
  /** 切换显示模式 */
  switchMode: (mode: MentionNegativeRateType) => void
  /** 获取当前模式 */
  getCurrentMode: () => MentionNegativeRateType
  /** 刷新图表 */
  refreshChart: () => void
}
