export interface FileItem {
  fileName: string
  uploadTime: string
  size?: number
}

export interface ReportData {
  currentPeriod: string
  fileList: FileItem[]
}

export interface ReportListParams {
  pageSize?: string | number
  pageNum?: string | number
  searchKeyword?: string
  firstLevelZoneId?: string
  firstLevelZoneIds?: string[]
  specialTypeId?: string
  specialTypeIds?: string[]
  status?: string
  statuses?: string[]
  reportName?: string
  isAuditBy?: boolean
}

export interface TopReportParams {
  /** 报告ID */
  id: string | number
  /** 是否置顶：1 置顶，0 取消置顶 */
  pinToTop: 0 | 1
}

export interface DeleteReportParams {
  /** 报告ID */
  id: string | number
}

export interface BatchDeleteReportParams {
  /** 报告ID列表 */
  ids: Array<string | number>
}
