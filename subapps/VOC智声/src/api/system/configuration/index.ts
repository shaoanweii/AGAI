/**
 * 显示规则管理模块API接口
 */

import request from '@/api/http'
import type {
  DisplayRuleItem,
  DisplayRuleQueryParams,
  UpdateDisplayRuleRequest
} from './types'

/**
 * 获取显示规则列表
 * @param params 查询参数
 * @returns 显示规则列表
 */
export const getDisplayRuleList = (params?: DisplayRuleQueryParams): Promise<BaseResponse<DisplayRuleItem[]>> => {
  return request.post('/report/display-rule/list', params)
}

/**
 * 新增显示规则
 * @param data 新增规则数据
 * @returns 操作结果
 */
export const createDisplayRule = (data: UpdateDisplayRuleRequest): Promise<BaseResponse<number>> => {
  return request.post('/report/display-rule/insert', data)
}

/**
 * 更新显示规则
 * @param data 更新规则数据
 * @returns 操作结果
 */
export const updateDisplayRule = (data: UpdateDisplayRuleRequest): Promise<BaseResponse<DisplayRuleItem[]>> => {
  return request.post('/report/display-rule/update', data)
}

/**
 * 删除显示规则
 * @param id 规则ID
 * @returns 操作结果
 */
export const deleteDisplayRule = (id: string): Promise<BaseResponse<any>> => {
  return request.delete(`/report/display-rule/${id}`)
}
