import request from '@/api/http/index'
import type {
  CarSeriesListItem,
  CarSeriesRankItem,
  ChannelTopVo,
  DataSourceListVo, IndicatorListVo, IndicatorRankVo, OpinionAnalysisVo,
  OpinionEvaluateTopVo, ScenarioAnalysisVo, ProvinceMapItem, DealerRankTopItem, ProvinceListItem,
  GenderDistributionVo, AgeDistributionVo, UserTypeDistributionVo, RegionDistributionVo, UserFocusSceneTopVo, UserListItemVo,
  ProductBriefVo
} from './types'

/**
 * 下钻筛选条件（用于体验代码类型等）
 * 接口: GET /report/drill-down/conditions
 */
export type DrillDownConditionDetail = {
  key: string
  value: string
  children?: DrillDownConditionDetail[]
}

export type DrillDownConditionItem = {
  key: string
  details?: DrillDownConditionDetail[]
}

export const getDrillDownConditions = (): Promise<BaseResponse<DrillDownConditionItem[]>> => {
  return request({
    url: '/report/drill-down/conditions',
    method: 'GET'
  })
}

/**
 * 下钻简报卡片
 * @param data 查询参数
 */
export const getDrillDownBrief = (data: VocQueryParams) => {
  return request<ProductBriefVo>({
    url: '/report/drill-down/getDrillDownBrief',
    method: 'POST',
    data
  })
}

/**
 * 获取数据变化趋势
 * @param data 查询参数
 */
export const getDataTrendChange = (data: VocQueryParams) => {
  return request<ScenarioAnalysisVo[]>({
    url: '/report/drill-down/data-trend-change',
    method: 'POST',
    data
  })
}

/**
 * 获取场景列表
 * @param data 查询参数
 */
export const getScenarioList = (data: VocQueryParams) => {
  return request({
    url: '/report/drill-down/scene-list',
    method: 'POST',
    data
  })
}

/**
 * 获取指标排行
 * @param data 查询参数
 */
export const getIndicatorRank = (data: VocQueryParams) => {
  return request<IndicatorRankVo[]>({
    url: '/report/drill-down/indicator-rank',
    method: 'POST',
    data
  })
}

/**
 * 获取指标列表
 * @param data 查询参数
 */
export const getIndicatorList = (data: VocQueryParams) => {
  return request<IndicatorListVo[]>({
    url: '/report/drill-down/indicator-list',
    method: 'POST',
    data
  })
}

/**
 * 获取渠道发声TOP数据
 * @param data 查询参数
 */
export const getChannelTop = (data: VocQueryParams) => {
  return request<ChannelTopVo[]>({
    url: '/report/drill-down/channel-top',
    method: 'POST',
    data
  })
}

/**
 * 获取数据源列表
 * @param data 查询参数
 */
export const getDataSourceList = (data: VocQueryParams) => {
  return request<DataSourceListVo[]>({
    url: '/report/drill-down/data-source-list',
    method: 'POST',
    data
  })
}

/**
 * 获取观点评价TOP数据
 * @param data 查询参数
 */
export const getOpinionEvaluateTop = (data: VocQueryParams) => {
  return request<BaseResponse>({
    url: '/report/drill-down/opinion-evaluate-top',
    method: 'POST',
    data
  })
}

/**
 * 品牌简报
 * @param data 查询参数
 */
export const getBrandBrief = (data: VocQueryParams) => {
  return request<BaseResponse>({
    url: '/report/drill-down/brand-brief',
    method: 'POST',
    data
  })
}

/**
 * 获取车系排行数据
 * @param data 查询参数
 */
export const getCarSeriesRank = (data: VocQueryParams) => {
  return request<BaseResponse<CarSeriesRankItem[]>>({
    url: '/report/drill-down/car-series-rank',
    method: 'POST',
    data
  })
}

/**
 * 获取车系列表数据
 * @param data 查询参数
 */
export const getCarSeriesList = (data: VocQueryParams) => {
  return request({
    url: '/report/drill-down/car-series-list',
    method: 'POST',
    data
  })
}

/**
 * 获取观点列表
 * @param data 查询参数
 */
export const getOpinionList = (data: VocQueryParams) => {
  return request({
    url: '/report/drill-down/opinion-list',
    method: 'POST',
    data
  })
}

/**
 * 地域分析 - 省份地图
 * @param data 查询参数
 */
export const getProvinceMap = (data: VocQueryParams) => {
  return request<ProvinceMapItem[]>({
    url: '/report/drill-down/getProvinceMap',
    method: 'POST',
    data
  })
}

/**
 * 地域分析 - 经销商评价排行TOP
 * @param data 查询参数
 */
export const getDealerRankTop = (data: VocQueryParams) => {
  return request<DealerRankTopItem[]>({
    url: '/report/drill-down/getDealerRankTop',
    method: 'POST',
    data
  })
}

/**
 * 地域分析-观点评价Top
 * @param data 查询参数
 */
export const getProvinceOpinionEvaluateTop = (data: VocQueryParams) => {
  return request<BaseResponse>({
    url: '/report/drill-down/province-opinion-evaluate-top',
    method: 'POST',
    data
  })
}

/**
 * 地域分析 - 区域列表
 * @param data 查询参数
 */
export const getProvinceList = (data: VocQueryParams) => {
  return request<ProvinceListItem[]>({
    url: '/report/drill-down/data-province-list',
    method: 'POST',
    data
  })
}

/**
 * 声音列表 - 原始声音
 * @param data 查询参数
 */
export const getVocListSounds = (data: VocQueryParams) => {
  return request<any>({
    url: '/report/voc-sounds/getVocListSounds',
    method: 'POST',
    data
  })
}

/**
 * 获取声音详情
 * @param data 查询参数 {newId: string}
 */
export const getSoundsDetails = (data: VocQueryParams) => {
  return request({
    url: '/report/voc-sounds/getSoundsDetails',
    method: 'POST',
    data
  })
}

/**
 * 人群特征 - 用户性别占比
 */
export const getGenderDistribution = (data: VocQueryParams) => {
  return request<GenderDistributionVo[]>({
    url: '/report/drill-down/getGenderDistribution',
    method: 'POST',
    data
  })
}

/**
 * 人群特征 - 各年龄段占比
 */
export const getAgeDistribution = (data: VocQueryParams) => {
  return request<AgeDistributionVo[]>({
    url: '/report/drill-down/getAgeDistribution',
    method: 'POST',
    data
  })
}

/**
 * 人群特征 - 用户类型占比
 */
export const getUserTypeDistribution = (data: VocQueryParams) => {
  return request<UserTypeDistributionVo[]>({
    url: '/report/drill-down/getUserTypeDistribution',
    method: 'POST',
    data
  })
}

/**
 * 人群特征 - 常住地省份占比
 */
export const getRegionDistribution = (data: VocQueryParams) => {
  return request<RegionDistributionVo[]>({
    url: '/report/drill-down/getRegionDistribution',
    method: 'POST',
    data
  })
}

/**
 * 人群特征 - 用户关注场景TOP10
 */
export const getUserFocusSceneTop = (data: VocQueryParams) => {
  return request<UserFocusSceneTopVo[]>({
    url: '/report/drill-down/getUserFocusSceneTop',
    method: 'POST',
    data
  })
}

/**
 * 人群特征 - 用户列表
 */
export const getUserList = (data: VocQueryParams) => {
  return request({
    url: '/report/drill-down/getUserList',
    method: 'POST',
    data
  })
}


/**
 * 人群特征 - 用户详情
 */
export const getUserDetail = (data: VocQueryParams) => {
  return request({
    url: '/report/drill-down/getUserDetail',
    method: 'POST',
    data
  })
}

/**
 * 用户详情 - 渠道轨迹（用于Tab）
 * @param data 查询参数 { userId, startDate, endDate }
 */
export const getUserDetailChannelTrajectory = (data: VocQueryParams) => {
  return request({
    url: '/report/drill-down/getUserDetail-channel-trajectory',
    method: 'POST',
    data
  })
}

/**
 * 用户详情 - 用户数据轨迹（用于时间线）
 * @param data 查询参数 { userId, channelCode?, startDate, endDate }
 */
export const getUserTrajectory = (data: VocQueryParams) => {
  return request({
    url: '/report/drill-down/getUser-trajectory',
    method: 'POST',
    data
  })
}
