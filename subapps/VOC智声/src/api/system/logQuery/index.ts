/**
 * 日志查询模块API接口
 *
 * 注意：接口地址可能会随服务端实现调整。
 * 当前先按“系统管理-日志查询”的常见口径进行封装，后续对接时只需要改动本文件的 url 即可。
 */

import request from '@/api/http'
import type { IPageLogQueryItem, LogQueryParams } from './types'

// 统一维护接口路径，方便后续对接调整
const LOG_QUERY_PAGE_API = '/report/operationLog/findVocLogList'

/**
 * 分页查询日志列表
 */
export const getLogQueryPage = (data?: LogQueryParams): Promise<BaseResponse<IPageLogQueryItem>> => {
  return request.post(LOG_QUERY_PAGE_API, data)
}
