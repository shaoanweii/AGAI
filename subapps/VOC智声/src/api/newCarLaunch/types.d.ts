/**
 * 新车上市模块类型定义（与 Swagger 文档严格对齐）
 *
 * 注意：使用全局的 VocQueryParams 类型
 * 该类型已在 src/types/common.d.ts 中定义，基于 ExtendComQueryModel 内容更新
 */

// 用户意图观点TOP响应数据（根据 Swagger 文档更新）
export interface IntentionOpinionTopVo {
  /** 观点名 */
  opinion: string
  /** 提及量 */
  mentions: number
  /** 提及量环比，% 两位小数 */
  mentionsMoM: number
  /** 提及量同比，% 两位小数 */
  mentionsYoY: number

  channelName: string

  negativeRate: string
}

// 车系条件响应数据
export interface SeriesConditionVo {
  /** 新品车系列表 */
  newCarSeries: Array<{
    /** 车系编码 */
    code: string
    /** 车系名称 */
    name: string
    /** 子元素 */
    cars?: Array<{
      /** 车系编码 */
      code: string
      /** 车系名称 */
      name: string
    }>
  }>
  /** 对比车系列表 */
  compareCarSeries: Array<{
    /** 车系编码 */
    code: string
    /** 车系名称 */
    name: string
    /** 子元素 */
    cars?: Array<{
      /** 车系编码 */
      code: string
      /** 车系名称 */
      name: string
    }>
  }>
}

// 产品分析API响应类型 - 使用全局 BaseResponse 类型保持项目一致性
export type ResultIntentionOpinionTopVo = BaseResponse<IntentionOpinionTopVo[]>

// 关注场景TOP响应数据
export interface FocusSceneTopVo {
  /** 场景名称 */
  scenario: string
  /** 提及量 */
  mentions: number
  /** 负面率，% */
  negativeRate: number
  /** 负面率字体颜色 */
  rateColor?: string
}

// 关注场景TOP API响应类型
export type ResultFocusSceneTopVo = BaseResponse<FocusSceneTopVo[]>

// 产品简报响应数据
export interface ProductBriefVo {
  /** 品牌名称 */
  brandName?: string
  /** 车系名称 */
  seriesName?: string
  /** 车系logo */
  logo?: string
  /** 正面率 */
  positiveRate?: number
  /** 负面率 */
  negativeRate?: number
  /** 提及量 */
  mentionCount?: string
}

// 产品简报API响应类型
export type ResultProductBriefVo = BaseResponse<ProductBriefVo[]>
