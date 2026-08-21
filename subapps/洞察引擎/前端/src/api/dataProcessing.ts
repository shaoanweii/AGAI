import request from './index'
import type { ValidationRequest } from '@/types/rule.types'
/**
 * 数据处理页面-规则定制-规则维护相关的接口
 */

/**
 * 新增规则信息
 * @param data
 */
export const saveRegulationInfo = (data: any) => {
  return request<any>({
    method: 'POST',
    url: `/insights/regulation/saveRegulationInfo`,
    data
  })
}
/**
 * 更新规则信息
 * @param data
 */
export const updateRegulationInfo = (data: any) => {
  return request<any>({
    method: 'POST',
    url: `/insights/regulation/updateRegulationInfo`,
    data
  })
}
/**
 * 根据id查询规则信息
 * @param data
 */
export const findRegulationInfo = (data: any) => {
  return request<any>({
    method: 'POST',
    url: `/insights/regulation/findRegulationInfo`,
    data
  })
}
/**
 * 禁用启用
 * 启用： Enabled
 * 禁用： Disabled
 * @param data
 */
export const disabledOrEnableRegulationInfo = (data: {
  id: string
  status: string
  clientId?: string
}) => {
  return request<any>({
    method: 'POST',
    url: `/insights/regulation/disabledOrEnableRegulationInfo`,
    data
  })
}
/**
 * 复制规则
 * @param data
 */
export const copyRegulationInfo = (data: { id: string; clientId?: string }) => {
  return request<any>({
    method: 'POST',
    url: `/insights/regulation/copyRegulationInfo`,
    data
  })
}

/**
 * 检索数据
 * @param data
 */
export const findValidateRegulationCondition = (data: ValidationRequest) => {
  return request<any>({
    method: 'POST',
    url: `/insights/regulation/findValidateRegulationCondition`,
    data
  })
}
/**
 * 开始验证规则信息
 * @param data
 */
export const startValidateRegulationInfo = (data: ValidationRequest) => {
  return request<any>({
    method: 'POST',
    url: `/insights/regulation/startValidateRegulationInfo`,
    data
  })
}

/**
 * 开始测试规则信息
 * @param data
 */
export const startTestRegulationInfo = (data: ValidationRequest) => {
  return request<any>({
    method: 'POST',
    url: `/insights/regulation/startTestRegulationInfo`,
    data
  })
}

/**
 * 删除规则
 * @param data
 */
export const deleteRegulationInfo = (data: { id: string; clientId: string }) => {
  return request<any>({
    method: 'POST',
    url: `/insights/regulation/deleteRegulationInfo`,
    data
  })
}
