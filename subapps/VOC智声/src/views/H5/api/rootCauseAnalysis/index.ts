import request from '@h5/api/http'
import type { HttpRequestConfig } from '@h5/api/http'
import type {
  IntentionOpinionTopVo,
  ProductTrendPointVo,
  SeriesRankItemVo,
  TagSentimentAnalysisVo
} from './types'
import type { H5VocBaseRequest } from '@h5/api/home/types'

/**
 * @description: 观点评价
 * @return {*}
 */
export const getUserIntentionOpinionTop = (
  data: H5VocBaseRequest,
  config?: HttpRequestConfig
): Promise<BaseResponse<IntentionOpinionTopVo[]>> => {
  return request.post(
    '/report/mobileTerminal/rootCause-analysis/getUserIntentionOpinionTop',
    data,
    config
  )
}

/**
 * @description: 数据趋势变化
 * @return {*}
 */
export const getDataTrendChange = (
  data: H5VocBaseRequest,
  config?: HttpRequestConfig
): Promise<BaseResponse<ProductTrendPointVo[]>> => {
  return request.post('/report/mobileTerminal/rootCause-analysis/getDataTrendChange', data, config)
}

/**
 * @description: 指标分析
 * @return {*}
 */
export const getTagAnalysis = (
  data: H5VocBaseRequest
): Promise<BaseResponse<TagSentimentAnalysisVo[]>> => {
  return request.post('/report/mobileTerminal/rootCause-analysis/get-tag-analysis', data)
}

/**
 * @description: 车系排行接口
 * @return {*}
 */
export const getSeriesRank = (
  data: H5VocBaseRequest,
  config?: HttpRequestConfig
): Promise<BaseResponse<SeriesRankItemVo[]>> => {
  return request.post('/report/mobileTerminal/rootCause-analysis/get-series-rank', data, config)
}
