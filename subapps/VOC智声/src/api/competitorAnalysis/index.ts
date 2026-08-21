/**
 * 旅程分析模块API接口
 */

import request from '@/api/http'
import { useGlobalCancelRequestStore } from '@/store/modules/globalCancelRequest'
import type {
  ComparativeBriefVo,
  HighestBrandCarVo,
  SceneComparisonVo,
  SourceCompareVo,
  TagAnalysisVo,
  TrendVo
} from './types'

// 创建带取消功能的请求配置
function createCancelableConfig() {
  const controller = new AbortController()
  const cancelRequestStore = useGlobalCancelRequestStore()
  cancelRequestStore.addCancelRequest(() => controller.abort())
  return { signal: controller.signal }
}

/**
 * 默认提及最高的品牌-车系
 * @param params 通用报表调用参数
 * @returns 默认提及最高的品牌-车系
 */
export const defaultHighestBrandCar = (
  params?: VocQueryParams
): Promise<BaseResponse<HighestBrandCarVo>> => {
  return request.post('/report/competitor-compare/defaultHighestBrandCar', params, createCancelableConfig())
}

/**
 * 所有品牌-车系数据
 * @param params 通用报表调用参数
 * @returns 所有品牌-车系数据
 */
export const getAllBrandOrCarSeriesData = (
  params?: VocQueryParams
): Promise<BaseResponse<any>> => {
  return request.post('/report/competitor-compare/getAllBrandOrCarSeriesData', params, createCancelableConfig())
}

/**
 * 综合对比简报
 * @param params 通用报表调用参数
 * @returns 综合对比简报
 */
export const getComparativeBrief = (
  params?: VocQueryParams
): Promise<BaseResponse<ComparativeBriefVo[]>> => {
  return request.post('/report/competitor-compare/getComparativeBrief', params, createCancelableConfig())
}

/**
 * @description: 趋势变化对比接口
 * @return {*}
 */
export const getTrendChangeCompare = (
  params?: VocQueryParams
): Promise<BaseResponse<TrendVo[]>> => {
  return request.post('/report/competitor-compare/getTrendChangeCompare', params, createCancelableConfig())
}

/**
 * @description: 服务对比分析接口
 * @return {*}
 */
export const getServiceTagAnalysis = (
  params?: VocQueryParams
): Promise<BaseResponse<TagAnalysisVo[]>> => {
  return request.post('/report/competitor-compare/get-service-tag-analysis', params, createCancelableConfig())
}

/**
 * @description: 产品对比分析接口
 * @return {*}
 */
export const getProductTagAnalysis = (
  params?: VocQueryParams
): Promise<BaseResponse<TagAnalysisVo[]>> => {
  return request.post('/report/competitor-compare/get-product-tag-analysis', params, createCancelableConfig())
}

/**
 * @description: 场景对比TOP
 * @return {*}
 */
export const getSceneComparisonTop = (
  params?: VocQueryParams
): Promise<BaseResponse<SceneComparisonVo>> => {
  return request.post('/report/competitor-compare/getSceneComparisonTop', params, createCancelableConfig())
}

/**
 * @description: 用户观点对比TOP
 * @return {*}
 */
export const getUseOpinionComparisonTop = (
  params?: VocQueryParams
): Promise<BaseResponse<SceneComparisonVo>> => {
  return request.post('/report/competitor-compare/getUseOpinionComparisonTop', params, createCancelableConfig())
}

/**
 * @description: 数据来源对比接口
 * @return {*}
 */
export const getComparisonDataSources = (
  params?: VocQueryParams
): Promise<BaseResponse<SourceCompareVo[]>> => {
  return request.post('/report/competitor-compare/getComparisonDataSources', params, createCancelableConfig())
}
