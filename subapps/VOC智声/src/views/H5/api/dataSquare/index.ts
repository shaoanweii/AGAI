import request from '@h5/api/http'
import type { HttpRequestConfig } from '@h5/api/http'
import type {
  H5DataSquareAttributeLabelItem,
  H5DataSquareBrandItem,
  H5DataSquareCategoryItem,
  H5DataSquareCategoryReportParams,
  H5DataSquareCategoryReportResult,
  H5DataSquareChannelNode,
  H5DataSquareConditionGroup,
  H5DataSquareConditionOption,
  H5DataSquareDrillDownBrief,
  H5DataSquareHomeParams,
  H5DataSquareLabelTag,
  H5DataSquarePageResult,
  H5DataSquareReportDateCondition,
  H5DataSquareReportDefaultCondition,
  H5DataSquareReportDetail,
  H5DataSquareReportDetailParams,
  H5DataSquareReportItem,
  H5DataSquareSearchParams,
  H5DataSquareStandardViewpointOption,
  H5DataSquareTagLibParams
} from './types'
import type { H5VocBaseRequest } from '@h5/api/home/types'
import type { SeriesRankItemVo, TagSentimentAnalysisVo } from '@h5/api/rootCauseAnalysis/types'

/**
 * 获取移动端看数广场品牌列表。
 */
export const getH5DataSquareBrandList = (): Promise<BaseResponse<H5DataSquareBrandItem[]>> => {
  return request.post('/report/mobile-data-plaza/brand/list')
}

/**
 * 获取移动端看数广场首页分类及报告。
 * @param data 品牌、分类与每类报告展示数量
 */
export const getH5DataSquareHome = (
  data: H5DataSquareHomeParams,
  config?: HttpRequestConfig
): Promise<BaseResponse<H5DataSquareCategoryItem[]>> => {
  return request.post('/report/mobile-data-plaza/home', data, config)
}

/**
 * 按报告名称搜索移动端看数广场报告。
 * @param data 搜索关键词、品牌、分类与分页参数
 */
export const searchH5DataSquareReports = (
  data: H5DataSquareSearchParams,
  config?: HttpRequestConfig
): Promise<BaseResponse<H5DataSquarePageResult<H5DataSquareReportItem>>> => {
  return request.post('/report/mobile-data-plaza/report/search', data, config)
}

/**
 * 获取分类详情和分类下报告列表。
 * @param data 分类 ID 与分页参数
 */
export const getH5DataSquareCategoryReportList = (
  data: H5DataSquareCategoryReportParams
): Promise<BaseResponse<H5DataSquareCategoryReportResult>> => {
  return request.post('/report/mobile-data-plaza/category/report/list', data)
}

/**
 * 获取移动端数据报告详情配置。
 * @param data 报告 ID
 */
export const getH5DataSquareReportDetail = (
  data: H5DataSquareReportDetailParams,
  config?: HttpRequestConfig
): Promise<BaseResponse<H5DataSquareReportDetail>> => {
  return request.post('/report/data-plaza/report/detail', data, config)
}

/**
 * 获取移动端数据报告核心下钻简报。
 * @param data 报告筛选条件与联动条件
 */
export const getH5DataSquareDrillDownBrief = (
  data: H5VocBaseRequest,
  config?: HttpRequestConfig
): Promise<BaseResponse<H5DataSquareDrillDownBrief>> => {
  return request.post('/report/mobile-data-plaza/report/getDrillDownBrief', data, config)
}

/**
 * 获取移动端数据报告车系排行。
 * @param data 报告筛选条件与分页参数
 */
export const getH5DataSquareSeriesRank = (
  data: H5VocBaseRequest,
  config?: HttpRequestConfig
): Promise<BaseResponse<SeriesRankItemVo[]>> => {
  return request.post('/report/mobile-data-plaza/report/get-series-rank', data, config)
}

/**
 * 获取移动端数据报告指标分析。
 * @param data 报告筛选条件与车系联动条件
 */
export const getH5DataSquareTagAnalysis = (
  data: H5VocBaseRequest,
  config?: HttpRequestConfig
): Promise<BaseResponse<TagSentimentAnalysisVo[]>> => {
  return request.post('/report/mobile-data-plaza/report/get-tag-analysis', data, config)
}

/**
 * 获取 H5 数据报告筛选条件配置。
 */
export const getH5DataSquareConditions = (): Promise<
  BaseResponse<H5DataSquareConditionGroup[]>
> => {
  return request.get('/report/data-plaza/conditions')
}

/**
 * 获取 H5 当前用户可见的数据源树。
 */
export const getH5UserChannelTree = (): Promise<BaseResponse<H5DataSquareChannelNode[]>> => {
  return request.post('/report/accountInfo/getUserChannelTree')
}

/**
 * 获取 H5 报告筛选使用的属性标签。
 * @param data 查询参数
 */
export const findH5AllAttributeLabelList = (
  data: Record<string, unknown>
): Promise<BaseResponse<H5DataSquareAttributeLabelItem[]>> => {
  return request.post('/report/findAllAttributeLabelList', data)
}

/**
 * 获取 H5 体验代码树。
 * @param data 标签类型
 */
export const getH5TagLibClientTree = (
  data?: Record<string, unknown>
): Promise<BaseResponse<H5DataSquareLabelTag[]>> => {
  return request.post('/report/getTagLibClientTree', data || {})
}

/**
 * 根据体验代码查询 H5 标准观点。
 * @param data 标签类型与末级体验代码
 */
export const findH5FinalTagLibClientVoListByTagId = (
  data: H5DataSquareTagLibParams
): Promise<BaseResponse<H5DataSquareStandardViewpointOption[]>> => {
  return request.post('/report/findAllFinalTagLibClientVoList', data)
}

export type {
  H5DataSquareAttributeLabelItem,
  H5DataSquareBrandItem,
  H5DataSquareCategoryItem,
  H5DataSquareCategoryReportParams,
  H5DataSquareCategoryReportResult,
  H5DataSquareChannelNode,
  H5DataSquareConditionGroup,
  H5DataSquareConditionOption,
  H5DataSquareDrillDownBrief,
  H5DataSquareHomeParams,
  H5DataSquareLabelTag,
  H5DataSquarePageResult,
  H5DataSquareReportDateCondition,
  H5DataSquareReportDefaultCondition,
  H5DataSquareReportDetail,
  H5DataSquareReportDetailParams,
  H5DataSquareReportItem,
  H5DataSquareSearchParams,
  H5DataSquareStandardViewpointOption,
  H5DataSquareTagLibParams
}
