/**
 * 报告查看记录相关接口
 */

import request from '@/api/http'
import type { ReportViewLogInsertParams } from './types'

/**
 * @description: 新增报告查看记录
 * @param {ReportViewLogInsertParams} params
 * @return {*}
 */
export const insertReportViewLog = (params: ReportViewLogInsertParams): Promise<BaseResponse<any>> => {
  return request.post('/report/report-view-log/insert', params)
}

export type * from './types'

