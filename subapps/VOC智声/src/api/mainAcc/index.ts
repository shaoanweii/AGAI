/**
 * 新车上市模块API接口
 */

import request from '@/api/http'
import type { ResultProductBriefVo } from './types'

/**
 * 综合分析卡片数据
 * @param params 查询条件
 * @returns 数据
 */
export const getAccBriefData = (params: any): Promise<ResultProductBriefVo> => {
  return request.post('/report/keyAccount/getKeyAccountBrief', params)
}

/**
 * 综合分析数据趋势数据
 * @param params 查询条件
 * @returns 数据
 */
export const getAccTendData = (params: any): Promise<any> => {
  return request.post('/report/keyAccount/getDataTrendChange', params)
}

/**
 * 综合分析 关注场景TOP 数据
 * @param params 查询条件
 * @returns 数据
 */
export const getAcSeceData = (params: any): Promise<any> => {
  return request.post('/report/keyAccount/getFocusSceneTop', params)
}

/**
 * 综合分析 词云TOP 数据
 * @param params 查询条件
 * @returns 数据
 */
export const getAcWordTopData = (params: any): Promise<any> => {
  return request.post('/report/keyAccount/getUseOpinionWordCloud', params)
}

/**
 * 场景分析报告解读数据
 * @param params 查询条件
 * @returns 数据
 */
export const getAccCjfzResult = (params: any): Promise<any> => {
  return request.post('/report/keyAccount/getFocusSceneAnalysisResult', params)
}

/**
 * 场景分析用户意图观点TOP数据
 * @param params 查询条件
 * @returns 数据
 */
export const getAccUserIntentionOpinionTop = (params: any): Promise<any> => {
  return request.post('/report/keyAccount/getUserIntentionOpinionTop', params)
}

/**
 * 场景分析柱状图数据
 * @param params 查询条件
 * @returns 数据
 */
export const getAccSceneAnalysisChart = (params: any): Promise<any> => {
  return request.post('/report/keyAccount/getFocusSceneAnalysis', params)
}
