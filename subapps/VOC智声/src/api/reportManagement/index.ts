import http from '@/api/http'
import type {
  BatchDeleteReportParams,
  DeleteReportParams,
  ReportData,
  ReportListParams,
  TopReportParams
} from './types'

/**
 * @description: 查询文件列表
 * @param {*} Promise
 * @return {*}
 */
export const getFiles = (): Promise<BaseResponse<ReportData>> => {
  return http.get('/report/submit-attach/files')
}

/**
 * @description: 上传附件
 * @param {FormData} file
 * @return {*}
 */
export const saUpload = (formData: FormData): Promise<BaseResponse<any>> => {
  return http.post('/report/submit-attach/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * @description: 报告管理
 * @param {any} data
 * @return {*}
 */
export const reportList = (data: ReportListParams): Promise<BaseResponse<any>> => {
  return http.post('/report/custom-report/reportList', data)
}

/**
 * @description: 报告置顶/取消置顶
 * @param {TopReportParams} data
 * @return {*}
 */
export const topReport = (data: TopReportParams): Promise<BaseResponse<any>> => {
  return http.post('/report/custom-report/topReport', data)
}

/**
 * @description: 删除单个报告
 * @param {DeleteReportParams} data
 * @return {*}
 */
export const deleteCustomReport = (data: DeleteReportParams): Promise<BaseResponse<any>> => {
  return http.post('/report/custom-report/delete', data)
}

/**
 * @description: 批量删除报告
 * @param {BatchDeleteReportParams} data
 * @return {*}
 */
export const batchDeleteCustomReport = (
  data: BatchDeleteReportParams
): Promise<BaseResponse<any>> => {
  return http.post('/report/custom-report/batchDelete', data)
}
