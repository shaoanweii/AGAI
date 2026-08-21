/**
 * 新车上市模块API接口
 */

import request from '@/api/http'
import type {
  ResultIntentionOpinionTopVo,
  SeriesConditionVo,
  ResultFocusSceneTopVo,
  ResultProductBriefVo
} from './types'

import type { SceneComparisonVo } from '@/api/competitorAnalysis/types'

/**
 * 获取数据来源分析
 * @param params 查询条件
 * @returns 数据来源分析数据
 */
export const getDataSourceAnalysis = (
  params: VocQueryParams
): Promise<ResultIntentionOpinionTopVo> => {
  return request.post('/report/new-car-launch/data-source-analysis', params)
}

/**
 * 获取车系条件
 * @param params 查询条件（可选）
 * @returns 车系条件数据
 */
export const getSeriesCondition = (
  params?: VocQueryParams
): Promise<BaseResponse<SeriesConditionVo>> => {
  return request.post('/report/new-car-launch/seriesCondition', params)
}

/**
 * 获取关注场景TOP（卡片数据）
 * @param params 查询条件
 * @returns 关注场景TOP数据
 */
export const getFocusSceneTop = (params: VocQueryParams): Promise<ResultFocusSceneTopVo> => {
  return request.post('/report/new-car-launch/getFocusSceneTop', params)
}

/**
 * 获取产品简报
 * @param params 查询条件
 * @returns 产品简报数据
 */
export const getProductBrief = (params: VocQueryParams): Promise<ResultProductBriefVo> => {
  return request.post('/report/new-car-launch/getProductBrief', params)
}

/**
 * 整体印象接口
 * @param params 查询条件
 * @returns 整体印象数据
 */
export const getOverallImpression = (
  params?: VocQueryParams
): Promise<BaseResponse<SceneComparisonVo>> => {
  return request.post('/report/new-car-launch/getUseOpinionComparisonTop', params)
}

/**
 * 获取数据趋势变化
 * @param params 查询条件
 * @returns 数据趋势变化数据
 */
export const getDataTrendChange = (params: VocQueryParams) => {
  return request.post('/report/new-car-launch/getDataTrendChange', params)
}

/**
 * 获取观点评价
 * @param params 查询条件
 * @returns 观点评价数据
 */
export const getOpinionEvaluation = (params: VocQueryParams) => {
  return request.post('/report/new-car-launch/get-opinion-evaluation', params)
}
