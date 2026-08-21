/**
 * 数据源分析组件类型定义
 * 数据来源：接口返回数据经过转换后的格式
 */

export interface WordCloudItem {
  /** 热词名称 */
  name: string
  /** 热词权重值 */
  value: number
  /** 情感倾向 */
  sentiment?: string
  /** 提及量环比 */
  mentionsMoM?: number
  /** 提及量同比 */
  mentionsYoY?: number
}
