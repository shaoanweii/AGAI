/**
 * 产品分析模块API接口
 */

import request from '@/api/http'
import type {
  ResultProductBriefVo,
  ResultIntentionOpinionTopVo,
  ResultListSceneTopVo,
  ResultSceneAnalysisBaseVo,
  ResultProductTrendVo,
  ResultListDataSourceAnalysisVo,
  ResultListChannelNegativeTrendVo,
  ResultListChannelMentionShareVo
} from './types'

/**
 * 获取综合分析简报
 * @param params 查询条件
 * @returns 综合分析简报数据
 */
export const getProductBrief = (params: VocQueryParams): Promise<ResultProductBriefVo> => {
  return request.post('/report/product-analysis/getProductBrief', params)
}

/**
 * 获取用户意图观点TOP
 * @param params 查询条件
 * @returns 用户意图观点TOP数据
 */
export const getUserIntentionOpinionTop = (
  params: VocQueryParams
): Promise<ResultIntentionOpinionTopVo> => {
  return request.post('/report/product-analysis/getUserIntentionOpinionTop', params)
}

/**
 * 获取关注场景TOP
 * @param params 查询条件
 * @returns 关注场景TOP数据
 */
export const getFocusSceneTop = (params: VocQueryParams): Promise<ResultListSceneTopVo> => {
  return request.post('/report/product-analysis/getFocusSceneTop', params)
}

/**
 * 获取关注场景分析
 * @param params 查询条件
 * @returns 关注场景分析数据
 */
export const getFocusSceneAnalysis = (
  params: VocQueryParams
): Promise<ResultSceneAnalysisBaseVo> => {
  return request.post('/report/product-analysis/getFocusSceneAnalysis', params)
}

/**
 * 获取数据趋势变化
 * @param params 查询条件
 * @returns 数据趋势变化数据
 */
export const getDataTrendChange = (params: VocQueryParams): Promise<ResultProductTrendVo> => {
  return request.post('/report/product-analysis/getDataTrendChange', params)
}

/**
 * 获取渠道数据排行
 * @param params 查询条件
 * @returns 渠道数据排行数据
 */
export const getDataSourceAnalysis = (
  params: VocQueryParams
): Promise<ResultListDataSourceAnalysisVo> => {
  return request.post('/report/product-analysis/getDataSourceAnalysis', params)
}

/**
 * 获取渠道负面率趋势变化
 * @param params 查询条件
 * @returns 渠道负面率趋势变化数据
 */
export const getChannelNegativeTrend = (
  params: VocQueryParams
): Promise<ResultListChannelNegativeTrendVo> => {
  return request.post('/report/product-analysis/getChannelNegativeTrend', params)
}

/**
 * 获取渠道提及量占比
 * @param params 查询条件
 * @returns 渠道提及量占比数据
 */
export const getChannelMentionShare = (
  params: VocQueryParams
): Promise<ResultListChannelMentionShareVo> => {
  return request.post('/report/product-analysis/getChannelMentionShare', params)
}
