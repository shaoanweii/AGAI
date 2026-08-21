import request from './index'

/**
 * 数据查询-原始数据导出（创建下载任务）
 * 注意：该接口为异步导出，成功后可前往【下载管理】查看结果
 */
export const exportRawData = (data: any) => {
  return request<any>({
    method: 'POST',
    url: '/insights/insCqCaDataSource/exportRawData',
    data
  })
}

/**
 * 数据查询-结果数据导出（创建下载任务）
 * 注意：该接口为异步导出，成功后可前往【下载管理】查看结果
 */
export const exportResultData = (data: any) => {
  return request<any>({
    method: 'POST',
    url: '/insights/insCqCaDataSource/exportRawDataResult',
    data
  })
}

/**
 * 下载管理-下载列表查询接口
 * 说明：后端接口已从 /insights/downLoad/findDownLoadFileList 调整为该地址
 */
export const FIND_DOWNLOAD_FILE_LIST_URL = '/insights/downLoad/findDownLoadFileList'

export interface FindReportDownLoadFileList {
  /** 每页条数 */
  pageSize?: number
  /** 页码 */
  pageNum?: number
  /** 排序方式 */
  order?: string
  /** 主键ID */
  id?: string
  /** 任务id */
  taskId?: string
  /** 用户id */
  userId?: string
  /** 用户名称 */
  userName?: string
  /** 任务名称 */
  taskName?: string
  /** 文件类型 */
  type?: string
  /** 下载状态 */
  status?: string
  /** 文件key */
  fileKey?: string
  /** 文件下载地址 */
  fileUrl?: string
  /** 文件名称 */
  fileName?: string
  /** 请求参数 */
  parameters?: string
  /** 创建时间 (date-time格式) */
  createTime?: string
  /** 用户ID列表 */
  userIds?: string[]
  /** 是否全部可见 */
  isAllVisible?: boolean
}

/**
 * 报表下载文件的分页返回结果类型
 */
export interface IPageReportDownLoadFileVo {
  /** 每页条数 */
  size: number
  /** 当前页码 */
  current: number
  /** 报表下载文件列表 */
  records: ReportDownLoadFileVo[]
  /** 总记录数 */
  total: number
  /** 总页数 */
  pages: number
}

/**
 * 报表下载文件的详情信息类型
 */
export interface ReportDownLoadFileVo {
  /** 记录ID */
  id: string
  /** 文件名 */
  fileName: string
  /** 文件路径 */
  filePath: string
  /** 下载时间（date-time格式） */
  downloadTime: string
  /** 操作人 */
  operator: string
  /** 当前状态：空表示正在下载，0表示下载失败，1表示下载成功 */
  status: string
}

/**
 * @description: 查询下载列表
 */
export const findReportDownLoadFileList = (data?: FindReportDownLoadFileList) => {
  return request<IPageReportDownLoadFileVo>({
    url: FIND_DOWNLOAD_FILE_LIST_URL,
    method: 'POST',
    data
  })
}

// 新命名别名：保持老方法兼容的同时，便于后续代码使用统一命名
export const findDownLoadFileList = findReportDownLoadFileList

/**
 * @description: 重新下载（重新触发生成任务）
 */
export const downloadAgain = (data: { id: string }) => {
  return request<any>({
    url: '/insights/downLoad/downloadAgain',
    method: 'POST',
    data
  })
}

/**
 * @description: 获取可见用户列表
 */
export const findVisibleUserList = (isAllVisible: boolean) => {
  return request<any>({
    url: '/insights/downLoad/findVisibleUserList',
    method: 'GET',
    params: { isAllVisible }
  })
}
