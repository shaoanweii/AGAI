/**
 * 服务分析模块API接口（与 Swagger 文档对齐）
 */

import request from '@/api/http'
import type {
  // 结果包装类型（Swagger命名）
  ResultListIntentionOpinionTopVo,
  ResultListServiceProvinceRankVo,
  ResultProductBriefVo,
  ResultListSceneTopVo,
  ResultSceneAnalysisBaseVo,
  ResultListServiceDealerRankVo,
  ResultProductTrendVo,
  ResultListDataSourceAnalysisVo,
  ResultListChannelNegativeTrendVo,
  ResultListChannelShareVo
} from './types'

/**
 * 获取用户意图观点TOP
 * @param params 查询条件
 * @returns 用户意图观点TOP数据
 */
export const getUserIntentionOpinionTop = (
  params: VocQueryParams
): Promise<ResultListIntentionOpinionTopVo> => {
  return request.post('/report/service-analysis/getUserIntentionOpinionTop', params)
}

/**
 * 获取省份排行
 * @param params 查询条件
 * @returns 省份排行数据
 */
export const getProvinceRank = (
  params: VocQueryParams
): Promise<ResultListServiceProvinceRankVo> => {
  return request.post('/report/service-analysis/getProvinceRank', params)
}

/**
 * 获取省份地图数据
 * @param params 查询条件
 * @returns 省份地图数据
 */
export const getProvinceMap = (
  params: VocQueryParams
): Promise<ResultListServiceProvinceRankVo> => {
  return request.post('/report/service-analysis/getProvinceMap', params)
}

/**
 * 获取综合分析简报
 * @param params 查询条件
 * @returns 综合分析简报数据
 */
export const getProductBrief = (params: VocQueryParams): Promise<ResultProductBriefVo> => {
  return request.post('/report/service-analysis/getProductBrief', params)
}

/**
 * 获取关注场景TOP
 * @param params 查询条件
 * @returns 关注场景TOP数据
 */
export const getFocusSceneTop = (params: VocQueryParams): Promise<ResultListSceneTopVo> => {
  return request.post('/report/service-analysis/getFocusSceneTop', params)
}

/**
 * 获取关注场景分析
 * @param params 查询条件
 * @returns 关注场景分析数据
 */
export const getFocusSceneAnalysis = (
  params: VocQueryParams
): Promise<ResultSceneAnalysisBaseVo> => {
  return request.post('/report/service-analysis/getFocusSceneAnalysis', params)
}

/**
 * 获取经销商评价排行TOP
 * @param params 查询条件
 * @returns 经销商评价排行TOP数据
 */
export const getDealerRankTop = (
  params: VocQueryParams
): Promise<ResultListServiceDealerRankVo> => {
  return request.post('/report/service-analysis/getDealerRankTop', params)
}

/**
 * 获取数据趋势变化
 * @param params 查询条件
 * @returns 数据趋势变化数据
 */
export const getDataTrendChange = (params: VocQueryParams): Promise<ResultProductTrendVo> => {
  return request.post('/report/service-analysis/getDataTrendChange', params)
}

/**
 * 获取数据来源分析
 * @param params 查询条件
 * @returns 数据来源分析数据
 */
export const getDataSourceAnalysis = (
  params: VocQueryParams
): Promise<ResultListDataSourceAnalysisVo> => {
  return request.post('/report/service-analysis/getDataSourceAnalysis', params)
}

/**
 * 获取渠道负面率趋势变化
 * @param params 查询条件
 * @returns 渠道负面率趋势变化数据
 */
export const getChannelNegativeTrend = (
  params: VocQueryParams
): Promise<ResultListChannelNegativeTrendVo> => {
  return request.post('/report/service-analysis/getChannelNegativeTrend', params)
}

/**
 * 获取渠道提及量占比
 * @param params 查询条件
 * @returns 渠道提及量占比数据
 */
export const getChannelMentionShare = (
  params: VocQueryParams
): Promise<ResultListChannelShareVo> => {
  return request.post('/report/service-analysis/getChannelMentionShare', params)
}
