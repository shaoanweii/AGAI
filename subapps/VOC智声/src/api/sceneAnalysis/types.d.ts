/**
 * 场景分析模块类型定义
 */

// 基础分页参数
export interface BasePageParams {
  pageSize?: number
  pageNum?: number
  order?: string
}

// 自定义报告模型
export interface CustomReportModel extends BasePageParams {
  id?: string
  reportName?: string
  viewCount?: number
  collectionCount?: number
  type?: number
  defaultCondition?: string
  brandCode?: string
  specialTypeId?: string
  status?: number
  createTime?: string
  updateTime?: string
  createBy?: string
  updateBy?: string
  description?: string
}

// 自定义报告列表项
export interface CustomReportListVo {
  id: string
  reportName: string
  viewCount: number
  collectionCount: number
  type: number
  status: number
  createTime: string
  /**
   * 是否置顶：1-置顶，0-未置顶
   */
  pinToTop?: number
  /**
   * 报告对应的路由地址（后端/配置可能会下发）
   */
  reportUrl?: string
}

// 自定义报告详情
export interface CustomReportDetailVo {
  id: string
  reportName: string
  viewCount: number
  collectionCount: number
  type: number
  defaultCondition: string
  brandCode: string
  specialTypeId: string
  status: number
  createTime: string
  updateTime: string
  createBy: string
  updateBy: string
  description: string
}

// 专项分析类型查询模型
export interface SpecialAnalysisTypeModel extends BasePageParams {
  id?: string
  name?: string
  createTime?: string
  updateTime?: string
  createBy?: string
  updateBy?: string
  delFlag?: number
  description?: string
}

// 专项分析类型列表项
export interface SpecialAnalysisTypeListVo {
  id: string
  name: string
  icon: string
  reportCnt: number
}

// 分页信息
export interface PageInfo<T> {
  total: number
  list: T[]
  pageNum: number
  pageSize: number
  size: number
  startRow: number
  endRow: number
  pages: number
  prePage: number
  nextPage: number
  isFirstPage: boolean
  isLastPage: boolean
  hasPreviousPage: boolean
  hasNextPage: boolean
  navigatePages: number
  navigatepageNums: number[]
  navigateFirstPage: number
  navigateLastPage: number
}

// 分页响应结构
export type PageResult<T> = BaseResponse<PageInfo<T>>

// 列表响应结构
export type ListResult<T> = BaseResponse<T[]>

// 详情响应结构
export type DetailResult<T> = BaseResponse<T>

// 操作响应结构
export type OperationResult = BaseResponse<number>

// 自定义报告查询参数
export interface CustomReportQueryParams {
  id?: string
  reportName?: string
  type?: number
  status?: number
  specialTypeId?: string
  firstLevelZoneId?: string
  brandCode?: string
  /**
   * 发布人（用于“我发布的”场景按当前登录人筛选）
   */
  createBy?: string
  /**
   * 后端排序字段（约定：create_time/view_count 等）
   */
  sortfield?: string
  /**
   * 排序方式（asc/desc）
   */
  sortorder?: 'asc' | 'desc' | string
  /**
   * 角色过滤（部分接口会按角色返回可见范围）
   */
  roleIds?: string[]
  pageSize?: number
  pageNum?: number
  order?: string
}

// 自定义报告创建参数
export interface CustomReportCreateParams {
  reportName?: string
  type?: number
  defaultCondition?: string
  brandCode?: string
  specialTypeId?: string
  description?: string
}

// 自定义报告更新参数
export interface CustomReportUpdateParams extends CustomReportCreateParams {
  id?: string
  status?: string
  firstLevelZoneId?: string
  ids?: string[]
}

// 自定义报告删除参数
export interface CustomReportDeleteParams {
  id: string
}

// 专项分析类型查询参数
export interface SpecialTypeQueryParams {
  id?: string
  name?: string
  /**
   * 类型：1-一级分类，2-二级分类
   */
  type?: number
  /**
   * 父级 id（二级分类查询时使用）
   */
  pid?: string
  /**
   * 角色过滤（按角色返回可见范围）
   */
  roleIds?: string[]
  pageSize?: number
  pageNum?: number
  order?: string
}

// 报告状态枚举
export enum ReportStatus {
  DRAFT = 0, // 草稿
  PUBLISHED = 1, // 已发布
  ARCHIVED = 2 // 已归档
}

// 报告类型枚举
export enum ReportType {
  STANDARD = 0, // 标准报告
  CUSTOM = 1, // 自定义报告
  TEMPLATE = 2 // 模板报告
}
