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
