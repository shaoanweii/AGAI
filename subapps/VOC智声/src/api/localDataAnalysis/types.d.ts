export type LocalDataAnalysisTaskStatus = 'notStarted' | 'processing' | 'completed' | 'failed'
import type { LocalDataAnalysisStatusCode } from './constants'

export interface LocalDataAnalysisQueryParams {
  /** 每页条数 */
  pageSize?: number
  /** 页码 */
  pageNum?: number
  /** 创建人 ID 列表 */
  userIds?: string[]
  /** 任务名称 */
  taskName?: string
  /** 是否可见全部任务 */
  isAllVisible?: boolean
}

export interface LocalDataAnalysisDataSourceQueryParams {
  /** 每页条数 */
  pageSize?: number
  /** 页码 */
  pageNum?: number
  /** 创建人员 ID 列表 */
  createUserIdList?: string[]
  /** 数据源名称关键词 */
  dataSourceName?: string
  /** 是否可见全部数据 */
  isAllVisible?: boolean
}

export interface LocalDataAnalysisUploadResult {
  /** 上传文件的唯一标识 */
  key: string
  /** 文件名称 */
  name: string | null
  /** 文件地址 */
  url: string | null
}

export interface LocalDataAnalysisCheckUploadParams {
  /** 文件名称 */
  fileName: string
}

export interface LocalDataAnalysisCheckUploadResult {
  /** 校验提示信息 */
  message: string
  /** 批次 ID */
  batchId: string
  /** 校验成功条数 */
  success: string
  /** 数据总数 */
  total: string
}

export interface LocalDataAnalysisSaveUploadDataSourceParams {
  /** 主键 ID */
  id?: string
  /** 文件名称 */
  fileName: string
  /** 批次 ID */
  batchId: string
  /** 数据源名称 */
  dataSourceName: string
}

export interface LocalDataAnalysisTaskItem {
  /** 任务 ID */
  id: string
  /** 任务名称 */
  taskName: string
  /** 导入总条数 */
  importCount: number
  /** 已处理条数 */
  processedCount: number
  /** 处理成功条数 */
  successCount?: number
  /** 创建人 ID */
  creatorId: string
  /** 创建人名称 */
  creatorName: string
  /** 创建时间 */
  createTime: string
  /** 当前状态 */
  status: LocalDataAnalysisTaskStatus
}

export interface LocalDataAnalysisTaskPage {
  /** 当前页数据 */
  records: LocalDataAnalysisTaskItem[]
  /** 总记录数 */
  total: number
  /** 当前页码 */
  current?: number
  /** 每页条数 */
  size?: number
}

/**
 * 本地数据分析数据源列表项。
 */
export interface LocalDataAnalysisDataSourceItem {
  /** 数据源 ID */
  id: string
  /** 任务明细 */
  taskInfo: string
  /** 数据名称 */
  dataName: string
  /** 导入批次 ID */
  batchId: string
  /** 当前状态编码：0 未处理，1 处理中，2 处理完成，3 处理失败 */
  statusCode: LocalDataAnalysisStatusCode
  /** 当前状态名称 */
  status: string
  /** 创建人员 */
  createUser: string
  /** 创建人员 ID */
  create_userId: string | null
  /** 创建时间 */
  createTime: string
}

/**
 * 本地数据分析数据源分页结果。
 */
export interface LocalDataAnalysisDataSourcePage {
  /** 当前页数据 */
  list: LocalDataAnalysisDataSourceItem[]
  /** 总记录数 */
  total: number
  /** 页码 */
  pageNum: number
  /** 每页条数 */
  pageSize: number
  /** 当前页数量 */
  size: number
  /** 总页数 */
  pages: number
}
