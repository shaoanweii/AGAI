/**
 * 热点事件模块API接口
 */

import request from '@/api/http'

/**
 * 热点事件列表
 * @param params 查询条件
 * @returns 数据
 */
export const getHotListData = (params: any): Promise<any> => {
  return request.post('/report/hot-event/list', params)
}

/**
 * 查询热点事件创建人列表
 * @param params 查询条件
 * @returns 数据
 */
export const getHotUserCreaterData = (): Promise<any> => {
  return request.get(`/report/hot-event/listCreators`)
}

/**
 * 创建热点事件
 * @param params 查询条件
 * @returns 数据
 */
export const createHotData = (params: any): Promise<any> => {
  return request.post('/report/hot-event/create', params)
}

/**
 * 修改热点事件
 * @param params 查询条件
 * @returns 数据
 */
export const updateHotData = (params: any): Promise<any> => {
  return request.post('/report/hot-event/update', params)
}

/**
 * 删除热点事件
 * @param params 删除条件
 * @returns 数据
 */
export const deleteHotData = (id: any): Promise<any> => {
  return request.post(`/report/hot-event/delete/${id}`)
}

// 根据id查询详情
export const getHotEvDetail = (params: any): Promise<any> => {
  return request.get(`/report/hot-event/getById`, { params })
}

/**
 * 综合分析 - 结果数据-卡片数据
 * @param params 查询条件
 * @returns 数据
 */
export const getHotBriefData = (params: any): Promise<any> => {
  return request.post('/report/hot-event/getHotEventResultBrief', params)
}

/**
 * 综合分析 - 原始数据-卡片数据
 * @param params 查询条件
 * @returns 数据
 */
export const getHotYsBriefData = (params: any): Promise<any> => {
  return request.post('/report/hot-event/getHotEventOriginBrief', params)
}

/**
 * 综合分析 - 结果数据 - 数据趋势数据
 * @param params 查询条件
 * @returns 数据
 */
export const getHotTendData = (params: any): Promise<any> => {
  return request.post('/report/hot-event/getResultDataTrendChange', params)
}

/**
 * 综合分析 - 原始数据-数据趋势数据
 * @param params 查询条件
 * @returns 数据
 */
export const getHotYsTendData = (params: any): Promise<any> => {
  return request.post('/report/hot-event/getOriginDataTrendChange', params)
}

/**
 *  关注场景TOP 数据
 * @param params 查询条件
 * @returns 数据
 */
export const getHotSeceData = (params: any): Promise<any> => {
  return request.post('/report/hot-event/getFocusSceneTop', params)
}

/**
 * 综合分析 词云TOP 数据
 * @param params 查询条件
 * @returns 数据
 */
export const getHotWordTopData = (params: any): Promise<any> => {
  return request.post('/report/hot-event/getUseOpinionWordCloud', params)
}

/**
 * 场景分析 柱状图数据
 * @param params 查询条件
 * @returns 数据
 */
export const getHotSceneAnalysisChart = (params: any): Promise<any> => {
  return request.post('/report/hot-event/getFocusSceneAnalysis', params)
}

/**
 * 场景分析用户意图观点TOP数据
 * @param params 查询条件
 * @returns 数据
 */
export const getHotUserIntentionOpinionTop = (params: any): Promise<any> => {
  return request.post('/report/hot-event/getUserIntentionOpinionTop', params)
}

/**
 * 获取观点评价
 * @param params 查询条件
 * @returns 观点评价数据
 */
export const getHotOpinionEvaluation = (params: any): Promise<any> => {
  return request.post('/report/hot-event/getOpinionEvaluation', params)
}

/**
 * 结果数据-数据来源分析-获取渠道提及量占比
 * @param params 查询条件
 * @returns 渠道提及量占比数据
 */
export const getHotChannelMentionShare = (params: any): Promise<any> => {
  return request.post('/report/hot-event/getChannelMentionShare', params)
}

/**
 * 结果数据-数据来源分析-获取渠道负面率趋势变化
 * @param params 查询条件
 * @returns 渠道负面率趋势变化数据
 */
export const getHotChannelNegativeTrend = (params: any): Promise<any> => {
  return request.post('/report/hot-event/getChannelNegativeTrend', params)
}

/**
 * 结果数据-数据来源分析-获取数据来源分析
 * @param params 查询条件
 * @returns 数据来源分析数据
 */
export const getHotDataSourceAnalysis = (params: any): Promise<any> => {
  return request.post('/report/hot-event/getDataSourceAnalysis', params)
}

/**
 * 原始数据-数据来源分析-获取柱状图数据
 * @param params 查询条件
 * @returns 数据来源分析数据
 */
export const getHotYsDataSourceAnalysis = (params: any): Promise<any> => {
  return request.post('/report/hot-event/getOriginDataSourceAnalysis', params)
}
