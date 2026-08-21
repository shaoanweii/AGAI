import request from '@h5/api/http'
import type { HttpRequestConfig } from '@h5/api/http'
import type {
  BrowseTrendVo,
  DictItemVo,
  H5VocBaseRequest,
  TaskCompletionVo,
  userBrowseRecordVoParam,
  UserVoiceVo
} from './types'

//获取品牌列表和时间筛选
export const userPermissions = (config?: HttpRequestConfig): Promise<BaseResponse> => {
  return request.post('/report/userPermissions', undefined, config)
}

//用户动态评价
export const getUserDynamicEvaluation = (
  data: H5VocBaseRequest,
  config?: HttpRequestConfig
): Promise<BaseResponse> => {
  return request.post(
    '/report/mobileTerminal/homePage/getUserDynamicEvaluation',
    data,
    config
  )
}

//品牌对比
export const getIndustryBrandComparison = (data: H5VocBaseRequest): Promise<BaseResponse> => {
  return request.post('/report/mobileTerminal/homePage/getIndustryBrandComparison', data)
}

// 获取声音列表
export const getVocListSounds = (data: H5VocBaseRequest): Promise<BaseResponse> => {
  return request.post('/report/voc-sounds/getVocListSounds', data)
}

// 任务意图声音列表
export const getBrowseListSounds = (data: H5VocBaseRequest): Promise<BaseResponse> => {
  return request.post('/report/user-browse-record/getBrowseListSounds', data)
}

// 品牌趋势变化
export const getBrandTrendComparison = (
  data: H5VocBaseRequest,
  config?: HttpRequestConfig
): Promise<BaseResponse> => {
  return request.post('/report/mobileTerminal/homePage/getBrandTrendComparison', data, config)
}

// 数据简报与达成率
export const getDataBrief = (
  data: H5VocBaseRequest,
  config?: HttpRequestConfig
): Promise<BaseResponse> => {
  return request.post('/report/mobileTerminal/homePage/getDataBrief', data, config)
}

// 关注场景分析-top榜
export const getFocusSceneAnalysisTop = (
  data: H5VocBaseRequest,
  config?: HttpRequestConfig
): Promise<BaseResponse> => {
  return request.post('/report/mobileTerminal/homePage/getFocusSceneAnalysisTop', data, config)
}

// 关注场景分析-柱状图
export const getFocusSceneAnalysis = (data: H5VocBaseRequest): Promise<BaseResponse> => {
  return request.post('/report/mobileTerminal/homePage/getFocusSceneAnalysis', data)
}

/**
 * @description: 根据声音id获取用户动态评价详情
 * @param {H5VocBaseRequest} data
 * @return {*}
 */
export const getUserDynamicEvaluationInfo = (
  data: H5VocBaseRequest
): Promise<BaseResponse<UserVoiceVo>> => {
  return request.post('/report/mobileTerminal/homePage/getUserDynamicEvaluationInfo', data)
}

/**
 * @description: 添加浏览记录
 * @param {userBrowseRecordVoParam} data
 * @return {*}
 */
export const browseRecordAdd = (data: userBrowseRecordVoParam): Promise<BaseResponse<any>> => {
  return request.post('/report/user-browse-record/add', data)
}

/**
 * @description: 任务完成率
 * @param {any} data
 * @return {*}
 */
export const taskCompletion = (data?: any): Promise<BaseResponse<TaskCompletionVo>> => {
  return request.post('/report/user-browse-record/task-completion', data)
}

/**
 * @description: 浏览趋势
 * @param {any} data
 * @return {*}
 */
export const browseTrend = (data?: any): Promise<BaseResponse<BrowseTrendVo[]>> => {
  return request.post('/report/user-browse-record/browse-trend', data)
}

/**
 * @description: 浏览记录
 * @param {any} data
 * @return {*}
 */
export const browseRecords = (data?: any, config?: HttpRequestConfig): Promise<BaseResponse> => {
  return request.post('/report/user-browse-record/browse-records', data, config)
}

/**
 * @description: 根据字典编码获取字典项列表
 * @param {string} dictIdCode 字典编码
 * @return {*}
 */
export const getDictItemsByDict = (dictIdCode: string): Promise<BaseResponse<DictItemVo[]>> => {
  return request.get(`/report/insDictItem/dict-items-by-dict/${dictIdCode}`)
}

// 导出类型定义
export type { H5VocBaseRequest, VoiceListItem, VoiceType, SceneType, DictItemVo } from './types'
