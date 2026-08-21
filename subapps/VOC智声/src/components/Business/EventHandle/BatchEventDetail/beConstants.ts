/**
 * 批量事件详情页签标识常量。
 * 统一维护页签 key，避免组件内部散落魔法字符串。
 */
export const BatchEventTabKey = {
  Statistics: 'statistics',
  VoiceList: 'voiceList',
  Progress: 'progress'
} as const

/**
 * 批量事件详情页签标识类型。
 */
export type BatchEventTabKey = (typeof BatchEventTabKey)[keyof typeof BatchEventTabKey]

/**
 * 批量事件详情页签配置。
 * 当前页签文案、图标和 key 统一由常量文件维护。
 */
export const BatchEventTabOptions: Array<{ key: BatchEventTabKey; label: string; icon: string }> = [
  { key: BatchEventTabKey.Statistics, label: '事件统计', icon: 'line-chart-up-01' },
  { key: BatchEventTabKey.VoiceList, label: '客户原声', icon: 'list-check' },
  { key: BatchEventTabKey.Progress, label: '处理进度', icon: 'compass-03' }
]

/**
 * 批量事件任务完成进度值。
 * 与后端任务进度枚举保持一致，更新进度弹窗直接提交该值。
 */
export const BatchEventTaskProgressValue = {
  NotStarted: 'NOT_STARTED',
  InProgress: 'IN_PROGRESS',
  Completed: 'COMPLETED'
} as const

/**
 * 批量事件任务完成进度值类型。
 */
export type BatchEventTaskProgressValue =
  (typeof BatchEventTaskProgressValue)[keyof typeof BatchEventTaskProgressValue]

/**
 * 批量事件任务完成进度下拉选项。
 */
export const BatchEventTaskProgressOptions: Array<{
  label: string
  value: BatchEventTaskProgressValue
}> = [
  { label: '未开始', value: BatchEventTaskProgressValue.NotStarted },
  { label: '进行中', value: BatchEventTaskProgressValue.InProgress },
  { label: '已完成', value: BatchEventTaskProgressValue.Completed }
]
