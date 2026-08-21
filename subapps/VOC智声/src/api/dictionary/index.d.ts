/**
 * 数据字典相关类型定义
 */

// 数据字典类型枚举
export enum DictType {
  STRING = 0,
  NUMBER = 1,
  BOOLEAN = 2
}

// 数据字典状态枚举
export enum DictStatus {
  DISABLED = 0,
  ENABLED = 1
}

// 数据字典类型模型
export interface DictTypeModel {
  id?: string
  dictName: string
  dictCode: string
  type: DictType
  description?: string
  operator?: string
  createTime?: string
  updateTime?: string
}

// 数据字典列表VO
export interface DictListVo {
  id: string
  dictName: string
  dictCode: string
  type: number
  description?: string
  operator?: string
  createTime?: string
  updateTime?: string
  itemCount?: number
  dictType?: string
}

// 数据字典项模型
export interface DictItemModel {
  id?: string
  dictId: string
  itemText: string
  itemTextEn?: string
  itemKey: string
  itemValue: string
  description?: string
  sortOrder: number
  status: DictStatus
  operator?: string
  createTime?: string
}

// 数据字典项列表VO
export interface DictItemListVo {
  id: string
  dictId: string
  itemText: string
  itemTextEn?: string
  itemKey: string
  itemValue: string
  description?: string
  sortOrder: number
  status: number
  operator?: string
  createTime?: string
}

// 分页查询参数
export interface DictQueryParams {
  pageNum: number
  pageSize: number
  dictName?: string
  dictCode?: string
}

export interface DictItemQueryParams {
  pageNum: number
  pageSize: number
  dictId: string
  itemText?: string
  status?: number
}

// 分页响应类型
export interface PageInfo<T> {
  total: number
  list: T[]
  pageNum: number
  pageSize: number
  size: number
  pages: number
  prePage: number
  nextPage: number
  isFirstPage: boolean
  isLastPage: boolean
  hasPreviousPage: boolean
  hasNextPage: boolean
}

// 使用全局 BaseResponse 类型，保持项目一致性
export type DictPageResult = BaseResponse<PageInfo<DictListVo>>
export type DictItemPageResult = BaseResponse<PageInfo<DictItemListVo>>
export type DictItemListResult = BaseResponse<DictItemListVo[]>
export type DictDetailResult = BaseResponse<DictListVo>
export type DictItemDetailResult = BaseResponse<DictItemListVo>
