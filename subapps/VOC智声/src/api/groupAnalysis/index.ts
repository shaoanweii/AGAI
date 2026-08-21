/**
 * 集团分析模块API接口
 */

import request from '@/api/http'
import type {
  ResultProductBriefVo,
  ResultListTagAnalysisRowVo,
  ResultListOpinionTopVo,
  ResultListGroupDataSourceAnalysisVo,
  ResultListBrandTrendVo,
  ResultListSeriesRankItemVo
} from './types'

/**
 * 获取集团综合分析简报
 * @param params 查询条件
 * @returns 集团综合分析简报数据
 */
export const getGroupProductBrief = (params: VocQueryParams): Promise<ResultProductBriefVo> => {
  return request.post('/report/group-analysis/getProductBrief', params)
}

/**
 * 获取服务口碑分析数据
 * @param params 查询条件
 * @returns 服务口碑分析数据
 */
export const getServiceReputationAnalysis = (
  params: VocQueryParams
): Promise<ResultListTagAnalysisRowVo> => {
  return request.post('/report/group-analysis/get-service-reputation-analysis', params)
}

/**
 * 获取产品分析数据
 * @param params 查询条件
 * @returns 产品分析数据
 */
export const getProductTagAnalysis = (
  params: VocQueryParams
): Promise<ResultListTagAnalysisRowVo> => {
  return request.post('/report/group-analysis/get-product-tag-analysis', params)
}

/**
 * 获取观点评价数据
 * @param params 查询条件
 * @returns 观点评价数据
 */
export const getOpinionEvaluation = (params: VocQueryParams): Promise<ResultListOpinionTopVo> => {
  return request.post('/report/group-analysis/get-opinion-evaluation', params)
}

/**
 * 获取集团数据来源分析
 * @param params 查询条件
 * @returns 集团数据来源分析数据
 */
export const getGroupDataSourceAnalysis = (
  params: VocQueryParams
): Promise<ResultListGroupDataSourceAnalysisVo> => {
  return request.post('/report/group-analysis/get-data-source-analysis', params)
}

/**
 * 获取品牌趋势变化数据
 * @param params 查询条件
 * @returns 品牌趋势变化数据
 */
export const getBrandTrendChange = (params: VocQueryParams): Promise<ResultListBrandTrendVo> => {
  return request.post('/report/group-analysis/get-brand-trend-change', params)
}

/**
 * 获取集团车系排行数据
 * @param params 查询条件
 * @returns 集团车系排行数据
 */
export const getBrandSeriesRank = (params: VocQueryParams): Promise<ResultListSeriesRankItemVo> => {
  return request.post('/report/group-analysis/get-brand-series-rank', params)
}
