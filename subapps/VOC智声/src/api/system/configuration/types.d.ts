/**
 * 显示规则管理模块类型定义
 */

/**
 * 显示规则项
 */
export interface DisplayRuleItem {
  /** 规则ID */
  id?: string
  /** 指标编码 */
  metricCode?: string
  /** 指标名称 */
  metricName?: string
  /** 范围最小值 */
  rangeMin?: number
  /** 范围最大值 */
  rangeMax?: number
  /** 颜色十六进制值 */
  colorHex?: string
  backgroundColorHex?: string
  /** 表情符号键值 */
  emojiKey?: string
  /** 排序号 */
  sortNo?: number
  /** 状态 0-禁用 1-启用 */
  status?: number
}

/**
 * 显示规则查询参数
 */
export interface DisplayRuleQueryParams {
  /** 指标编码 */
  metricCode?: string
  /** 指标名称 */
  metricName?: string
  /** 状态 */
  status?: number
  /** 页码 */
  pageNo?: number
  /** 每页大小 */
  pageSize?: number
}

/**
 * 新增/更新显示规则请求参数
 */
export interface UpdateDisplayRuleRequest {
  /** 规则ID */
  id?: string
  /** 指标编码 */
  metricCode?: string
  /** 指标名称 */
  metricName?: string
  /** 范围最小值 */
  rangeMin?: number
  /** 范围最大值 */
  rangeMax?: number
  /** 演色十六进制值 */
  colorHex?: string
  /** 背景色十六进制值 */
  backgroundColorHex?: string
  /** 表情符号键值 */
  emojiKey?: string
  /** 排序号 */
  sortNo?: number
  /** 状态 0-禁用 1-启用 */
  status?: number
}
