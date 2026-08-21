// 综合分析简报响应数据

export interface ProductBriefVo {
  /** 负面率，单位% 保留两位小数 */
  negativeRate: number
  /** 负面率环比，单位% 保留两位小数 */
  negativeRateMoM: number
  /** 负面率同比，单位% 保留两位小数 */
  negativeRateYoY: number

  /** 正面率，单位% 保留两位小数 */
  positiveRate: number
  /** 正面率环比，单位% 保留两位小数 */
  positiveRateMoM: number
  /** 正面率同比，单位% 保留两位小数 */
  positiveRateYoY: number

  /** 提及量 */
  mentions: number
  /** 提及量环比，单位% 保留两位小数 */
  mentionsMoM: number
  /** 提及量同比，单位% 保留两位小数 */
  mentionsYoY: number

  /** 用户数（去重one_id） */
  users: number
  /** 用户数环比，单位% 保留两位小数 */
  usersMoM: number
  /** 用户数同比，单位% 保留两位小数 */
  usersYoY: number

  /** 文字颜色 */
  rateColor?: string
  /** 背景颜色 */
  rateBackgroundColor?: string

  comments: number //有效评论数
  commentsMoM: number //环比
  commentsYoY: number //同比
}

// API响应类型 - 使用全局 BaseResponse 类型保持项目一致性
export type ResultProductBriefVo = BaseResponse<ProductBriefVo>
