/**
 * 渠道发声TOP 横向排行条形图 组件类型
 */

export interface ChannelRankItem {
  /** 名称（渠道名） */
  name: string
  channelCode?: string
  /** 百分比（0-100 或 0-1）用于 rate 模式 */
  percent?: number
  /** 数值（用于 value 模式） */
  value?: number
  /** 是否高亮（置顶项红色） */
  highlight?: boolean
}

export interface ChannelVoiceRankingBarProps {
  /** 数据源 */
  data: ChannelRankItem[]
  /** 模式：rate=百分比，value=按值 */
  mode?: MentionNegativeRateType
  /** 组件高度 */
  height?: string
  /** 每条柱子的厚度（像素） */
  barHeight?: number
}

export interface ChannelVoiceRankingBarEvents {
  (e: 'update:mode', value: MentionNegativeRateType): void
  (e: 'barClick', data: ChannelRankItem): void
  (e: 'modeChange', mode: MentionNegativeRateType): void
}

export interface ChannelVoiceRankingBarInstance {
  switchMode: (mode: MentionNegativeRateType) => void
  getCurrentMode: () => MentionNegativeRateType
  refreshChart: () => void
}
