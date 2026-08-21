/**
 * 品牌API相关类型定义
 */

/**
 * 品牌项接口
 */
export interface BrandItem {
  /** 品牌ID */
  key: string
  /** 品牌名称 */
  value: string
  /** 品牌Logo URL */
  img?: string
}

/**
 * 品牌列表响应接口
 */
export interface BrandListResponse {
  /** 品牌列表 */
  list: BrandItem[]
  /** 总数 */
  total: number
}
