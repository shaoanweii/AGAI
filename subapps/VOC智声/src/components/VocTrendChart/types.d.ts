export interface VocTrendChartData {
  keyWord: string // 日期（X轴标签）
  experienceValue: number // 体验值（右侧Y轴）
  positiveMentionValue: number // 正面提及量
  neutralMentionValue: number // 中性提及量
  negativeMentionValue: number // 负面提及量
  totalMentionValue?: number // 总提及量（可选）
}
