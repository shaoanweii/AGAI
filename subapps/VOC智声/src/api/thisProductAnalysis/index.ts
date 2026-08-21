/**
 * 本品分析模块API接口
 */

import request from '@/api/http'
import type {
  ResultListProductSelfJourneyAnalysisVo,
  ResultProductSelfBriefVo,
  ResultListProductSelfSceneTopVo,
  ResultProductSelfTrendVo,
  ResultListProductSelfDataSourceAnalysisVo,
  ResultListProductSelfChannelNegativeTrendVo,
  ResultListProductSelfChannelMentionShareVo,
  ResultListProductSelfTagAnalysisRowVo
} from './types'

/**
 * 本品分析-用户旅程分析
 * @param params 查询条件
 * @returns 用户旅程分析数据
 */
export const getUserJourneyAnalysis = (
  params: VocQueryParams
): Promise<ResultListProductSelfJourneyAnalysisVo> => {
  return request.post('/report/product-self-analysis/user-journey-analysis', params)
}

/**
 * 获取本品分析综合分析简报
 * @param params 查询条件
 * @returns 综合分析简报数据
 */
export const getProductSelfBrief = (params: VocQueryParams): Promise<ResultProductSelfBriefVo> => {
  return request.post('/report/product-self-analysis/getProductBrief', params)
}

/**
 * 获取本品分析关注场景TOP
 * @param params 查询条件
 * @returns 关注场景TOP数据
 */
export const getFocusSceneTop = (
  params: VocQueryParams
): Promise<ResultListProductSelfSceneTopVo> => {
  return request.post('/report/product-self-analysis/getFocusSceneTop', params)
}

/**
 * 获取本品分析数据趋势变化
 * @param params 查询条件
 * @returns 数据趋势变化数据
 */
export const getDataTrendChange = (params: VocQueryParams): Promise<ResultProductSelfTrendVo> => {
  return request.post('/report/product-self-analysis/getDataTrendChange', params)
}

/**
 * 获取本品分析渠道数据排行
 * @param params 查询条件
 * @returns 渠道数据排行数据
 */
export const getDataSourceAnalysis = (
  params: VocQueryParams
): Promise<ResultListProductSelfDataSourceAnalysisVo> => {
  return request.post('/report/product-self-analysis/getDataSourceAnalysis', params)
}

/**
 * 获取本品分析渠道负面率趋势变化
 * @param params 查询条件
 * @returns 渠道负面率趋势变化数据
 */
export const getChannelNegativeTrend = (
  params: VocQueryParams
): Promise<ResultListProductSelfChannelNegativeTrendVo> => {
  return request.post('/report/product-self-analysis/getChannelNegativeTrend', params)
}

/**
 * 获取本品分析渠道提及量占比
 * @param params 查询条件
 * @returns 渠道提及量占比数据
 */
export const getChannelMentionShare = (
  params: VocQueryParams
): Promise<ResultListProductSelfChannelMentionShareVo> => {
  return request.post('/report/product-self-analysis/getChannelMentionShare', params)
}

/**
 * 获取本品分析服务标签分析
 * @param params 查询条件
 * @returns 服务标签分析数据
 */
export const getServiceTagAnalysis = (
  params: VocQueryParams
): Promise<ResultListProductSelfTagAnalysisRowVo> => {
  return request.post('/report/product-self-analysis/get-service-tag-analysis', params)
}

/**
 * 获取本品分析产品标签分析
 * @param params 查询条件
 * @returns 产品标签分析数据
 */
export const getProductTagAnalysis = (
  params: VocQueryParams
): Promise<ResultListProductSelfTagAnalysisRowVo> => {
  return request.post('/report/product-self-analysis/get-product-tag-analysis', params)
}
