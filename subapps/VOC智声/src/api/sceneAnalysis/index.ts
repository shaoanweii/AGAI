/**
 * 场景分析模块 API 接口
 */

import request from '@/api/http'
import type {
  CustomReportListVo,
  CustomReportDetailVo,
  CustomReportQueryParams,
  CustomReportCreateParams,
  CustomReportUpdateParams,
  CustomReportDeleteParams,
  SpecialAnalysisTypeListVo,
  SpecialTypeQueryParams,
  PageResult,
  ListResult,
  DetailResult,
  OperationResult
} from './types.d'

/**
 * 分页查询自定义报告列表
 * @param params 查询参数
 * @returns 分页数据
 */
export async function getCustomReportList(
  params: CustomReportQueryParams
): Promise<PageResult<CustomReportListVo>> {
  return request.post('/report/custom-report/list', params)
}

/**
 * 查询自定义报告详情
 * @param params 查询参数
 * @returns 报告详情
 */
export async function getCustomReportDetail(params: {
  id: string
}): Promise<DetailResult<CustomReportDetailVo>> {
  return request.post('/report/custom-report/detail', params)
}

/**
 * 新增自定义报告
 * @param params 报告数据
 * @returns 操作结果
 */
export async function createCustomReport(
  params: CustomReportCreateParams
): Promise<OperationResult> {
  return request.post('/report/custom-report/insert', params)
}

/**
 * 更新自定义报告
 * @param params 报告数据
 * @returns 操作结果
 */
export async function updateCustomReport(
  params: CustomReportUpdateParams
): Promise<OperationResult> {
  return request.post('/report/custom-report/update', params)
}

/**
 * @description: 审核发布报告
 * @param {CustomReportUpdateParams} params
 * @return {*}
 */
export async function reviewReport(params: CustomReportUpdateParams): Promise<OperationResult> {
  return request.post('/report/custom-report/reviewReport', params)
}

/**
 * 删除自定义报告
 * @param params 删除参数
 * @returns 操作结果
 */
export async function deleteCustomReport(
  params: CustomReportDeleteParams
): Promise<OperationResult> {
  return request.post('/report/custom-report/delete', params)
}

/**
 * 分页查询专项分析类型
 * @param params 查询参数
 * @returns 专项分析类型列表
 */
export async function getSpecialTypeList(
  params: SpecialTypeQueryParams
): Promise<ListResult<SpecialAnalysisTypeListVo>> {
  return request.post('/report/custom-report/special-type-list', params)
}

// 导出类型
export type * from './types.d'
