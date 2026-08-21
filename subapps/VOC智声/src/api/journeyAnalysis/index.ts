/**
 * 旅程分析模块API接口
 */

import request from '@/api/http'
import type {
  CommonReportInvokeModel,
  ResultCommonReportInvoke,
  ResultVoiceUserTop,
  ResultJourneyDetailAnalysis,
  ResultUserTypeDistribution,
  ResultIntentionOpinionTop,
  ResultUserFocusSceneTop,
  ResultSurgingSceneTop,
  ResultRegionDistribution,
  ResultProductBrief,
  ResultHighFreqSceneTop,
  ResultGenderDistribution,
  ResultFocusSceneTop,
  ResultDataTrendChange,
  ResultDataSourceAnalysis,
  ResultChannelNegativeTrend,
  ResultChannelMentionShare,
  ResultAgeDistribution
} from './types'

/**
 * 通用报表调用接口
 * @param params 通用报表调用参数
 * @returns 报表数据
 */
export const callCommonReport = (
  params: CommonReportInvokeModel
): Promise<ResultCommonReportInvoke> => {
  return request.post('/report/report-analysis/invoke/call', params)
}

// ==================== 分割线 ====================
// 以下是从API文档中读取的旅程分析模块接口

/**
 * 发声用户TOP5
 * @param params VoC查询参数
 * @returns 发声用户TOP5数据
 */
export const getVoiceUserTop = (params: VocQueryParams): Promise<ResultVoiceUserTop> => {
  return request.post('/report/journey-analysis/getVoiceUserTop', params)
}

/**
 * 用户类型占比
 * @param params VoC查询参数
 * @returns 用户类型占比数据
 */
export const getUserTypeDistribution = (
  params: VocQueryParams
): Promise<ResultUserTypeDistribution> => {
  return request.post('/report/journey-analysis/getUserTypeDistribution', params)
}

/**
 * 意图观点TOP
 * @param params VoC查询参数
 * @returns 意图观点TOP数据
 */
export const getUserIntentionOpinionTop = (
  params: VocQueryParams
): Promise<ResultIntentionOpinionTop> => {
  return request.post('/report/journey-analysis/getUserIntentionOpinionTop', params)
}

/**
 * 用户关注场景TOP10
 * @param params VoC查询参数
 * @returns 用户关注场景TOP10数据
 */
export const getUserFocusSceneTop = (params: VocQueryParams): Promise<ResultUserFocusSceneTop> => {
  return request.post('/report/journey-analysis/getUserFocusSceneTop', params)
}

/**
 * 飙升场景TOP5
 * @param params VoC查询参数
 * @returns 飙升场景TOP5数据
 */
export const getSurgingSceneTop = (params: VocQueryParams): Promise<ResultSurgingSceneTop> => {
  return request.post('/report/journey-analysis/getSurgingSceneTop', params)
}

/**
 * 所在区域占比
 * @param params VoC查询参数
 * @returns 所在区域占比数据
 */
export const getRegionDistribution = (
  params: VocQueryParams
): Promise<ResultRegionDistribution> => {
  return request.post('/report/journey-analysis/getRegionDistribution', params)
}

/**
 * 综合分析简报
 * @param params VoC查询参数
 * @returns 综合分析简报数据
 */
export const getProductBrief = (params: VocQueryParams): Promise<ResultProductBrief> => {
  return request.post('/report/journey-analysis/getProductBrief', params)
}

/**
 * 旅程细化分析
 * @param params VoC查询参数
 * @returns 旅程细化分析数据
 */
export const getJourneyDetailAnalysis = (
  params: VocQueryParams
): Promise<ResultJourneyDetailAnalysis> => {
  return request.post('/report/journey-analysis/getJourneyDetailAnalysis', params)
}

/**
 * 高频场景TOP5
 * @param params VoC查询参数
 * @returns 高频场景TOP5数据
 */
export const getHighFreqSceneTop = (params: VocQueryParams): Promise<ResultHighFreqSceneTop> => {
  return request.post('/report/journey-analysis/getHighFreqSceneTop', params)
}

/**
 * 用户性别占比
 * @param params VoC查询参数
 * @returns 用户性别占比数据
 */
export const getGenderDistribution = (
  params: VocQueryParams
): Promise<ResultGenderDistribution> => {
  return request.post('/report/journey-analysis/getGenderDistribution', params)
}

/**
 * 关注场景TOP
 * @param params VoC查询参数
 * @returns 关注场景TOP数据
 */
export const getFocusSceneTop = (params: VocQueryParams): Promise<ResultFocusSceneTop> => {
  return request.post('/report/journey-analysis/getFocusSceneTop', params)
}

/**
 * 数据趋势变化
 * @param params VoC查询参数
 * @returns 数据趋势变化数据
 */
export const getDataTrendChange = (params: VocQueryParams): Promise<ResultDataTrendChange> => {
  return request.post('/report/journey-analysis/getDataTrendChange', params)
}

/**
 * 渠道数据排行
 * @param params VoC查询参数
 * @returns 渠道数据排行数据
 */
export const getDataSourceAnalysis = (
  params: VocQueryParams
): Promise<ResultDataSourceAnalysis> => {
  return request.post('/report/journey-analysis/getDataSourceAnalysis', params)
}

/**
 * 渠道负面率趋势变化
 * @param params VoC查询参数
 * @returns 渠道负面率趋势变化数据
 */
export const getChannelNegativeTrend = (
  params: VocQueryParams
): Promise<ResultChannelNegativeTrend> => {
  return request.post('/report/journey-analysis/getChannelNegativeTrend', params)
}

/**
 * 渠道提及量占比
 * @param params VoC查询参数
 * @returns 渠道提及量占比数据
 */
export const getChannelMentionShare = (
  params: VocQueryParams
): Promise<ResultChannelMentionShare> => {
  return request.post('/report/journey-analysis/getChannelMentionShare', params)
}

/**
 * 各年龄段占比
 * @param params VoC查询参数
 * @returns 各年龄段占比数据
 */
export const getAgeDistribution = (params: VocQueryParams): Promise<ResultAgeDistribution> => {
  return request.post('/report/journey-analysis/getAgeDistribution', params)
}
