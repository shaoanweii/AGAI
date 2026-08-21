import request from '@/api/http/index'
import type {
  GroupBreifVo,
  ProductExperienceIndexVo,
  HomeReportTopVo,
  HotEventsVo,
  CustomerTeasingVo,
  CustomerEmotionVo,
  QueryConditionsVo,
  TagTypeVo,
  RiskLevelVo,
  TimeVo,
  HomeMenuVo,
  SpecialZoneTreeVo,
  PublishReportQueryParams
} from './type'

/**
 * 获取顶部聆听播报文案
 */
export const getBrowseSummaryBrief = () => {
  return request<string>({
    url: '/report/user-browse-record/listenBroadcast',
    method: 'POST'
  })
}

/**
 * 获取集团简报
 * @param data 查询参数
 */
export const getGroupBrief = (data: VocQueryParams) => {
  return request<any[]>({
    url: '/report/vocLeadership/getGroupBrief',
    method: 'POST',
    data
  })
}

/**
 * 获取品牌车系排行数据
 * @param params 查询条件
 * @returns 品牌车系排行数据
 */
export const getBrandRanking = (data: VocQueryParams) => {
  return request<any[]>({
    url: '/report/vocLeadership/getBrandRanking',
    method: 'POST',
    data
  })
}

/**
 * 获取品牌简报
 * @param data 查询参数
 */
export const getBrandBriefReport = (data: VocQueryParams) => {
  return request<ProductExperienceIndexVo[]>({
    url: '/report/homePage/getBrandBriefReport',
    method: 'POST',
    data
  })
}

/**
 * 获取专项分析
 * @param data 查询参数
 */
export const getSpecializedAnalysis = (data: VocQueryParams) => {
  return request<HomeReportTopVo[]>({
    url: '/report/homePage/getSpecializedAnalysis',
    method: 'POST',
    data
  })
}

/**
 * 获取热点事件top
 * @param data 查询参数
 */
export const getHotEventsTop = (data: VocQueryParams) => {
  return request<HotEventsVo[]>({
    url: '/report/homePage/getHotEventsTop',
    method: 'POST',
    data
  })
}

/**
 * 获取客户吐槽
 * @param data 查询参数
 */
export const getCustomerTeasing = (data: VocQueryParams) => {
  return request<CustomerTeasingVo[]>({
    url: '/report/homePage/getCustomerTeasing',
    method: 'POST',
    data
  })
}

/**
 * 获取客情直驱
 * @param data 查询参数
 */
export const getCustomerEmotion = (data: VocQueryParams) => {
  return request<CustomerEmotionVo[]>({
    url: '/report/homePage/getCustomerEmotion',
    method: 'POST',
    data
  })
}

/**
 * 获取查询条件
 */
export const getConditions = () => {
  return request<QueryConditionsVo>({
    url: '/report/homePage/conditions_2',
    method: 'GET'
  })
}

/**
 * 获取时间信息
 */
export const getTime = () => {
  return request<TimeVo>({
    url: '/report/homePage/getTime_4',
    method: 'GET'
  })
}

/**
 * 获取标签类型
 */
export const getTagType = () => {
  return request<TagTypeVo>({
    url: '/report/homePage/getTagType_4',
    method: 'GET'
  })
}

/**
 * 获取风险等级
 */
export const getRiskLevel = () => {
  return request<RiskLevelVo>({
    url: '/report/homePage/getRiskLevel_4',
    method: 'GET'
  })
}

/**
 * 专项分析报告查看
 */
export const saveReportViewLog = (reportId: string) => {
  return request<BaseResponse<any>>({
    url: '/report/homePage/saveReportViewLog',
    method: 'post',
    data: {
      reportId
    }
  })
}

/**
 * 通用场景
 * @param data 查询参数
 */
export const getGeneralScenario = (data: VocQueryParams) => {
  return request<HomeMenuVo[]>({
    url: '/report/homePage/getGeneralScenario',
    method: 'POST',
    data
  })
}

/**
 * @description: 获取一二级绑定的专区字段选择
 * @param {VocQueryParams} data
 * @return {*}
 */
export const getSpecialZoneOptions = (data: any) => {
  return request<SpecialZoneTreeVo[]>({
    url: '/report/custom-report/special-zone-options',
    method: 'POST',
    data
  })
}

/**
 * @description: 发布报告
 * @param {any} data
 * @return {*}
 */
export const publishReport = (data: PublishReportQueryParams) => {
  return request<any>({
    url: '/report/custom-report/insert',
    method: 'POST',
    data
  })
}
