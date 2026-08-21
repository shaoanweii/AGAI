/**
 * 提及量/负面率切换项
 * 用于需要在两个核心指标之间切换展示的通用场景。
 */
export const MENTION_NEGATIVE_RATE_SWITCH_OPTIONS: [
  { value: 'mention'; label: '提及量' },
  { value: 'negativeRate'; label: '负面率' }
] = [
  { value: 'mention', label: '提及量' },
  { value: 'negativeRate', label: '负面率' }
]

/**
 * 提及量/负面率默认指标
 * 统一控制相关模块首次展示的默认值，后续如需切换默认展示项只需修改这里。
 */
export const DEFAULT_MENTION_NEGATIVE_RATE_TYPE: MentionNegativeRateType = 'mention'
