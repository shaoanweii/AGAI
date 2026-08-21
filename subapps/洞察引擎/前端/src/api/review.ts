import request from './index'
import { DATA_KNOWLEDGE_API_KEY_HEADER } from './constants'

/**
 * @description: 获取发起人
 * @param {any} data
 * @return {*}
 */
export const queryCreateUserList = (data?: any) => {
  return request({
    method: 'post',
    url: '/insights/addLabel/queryCreateUserList',
    data
  })
}

/**
 * @description: 查询数据明细--分页
 * @param {any} data
 * @return {*}
 */
export const queryDataInfo = (data?: any) => {
  return request({
    method: 'post',
    url: '/insights/addLabel/queryDataInfo',
    data,
    headers: { ...DATA_KNOWLEDGE_API_KEY_HEADER }
  })
}

/**
 * @description: 状态更改--审核
 * @param {any} data
 * @return {*}
 */
export const auditLabelCorrection = (data?: any) => {
  return request({
    method: 'post',
    url: '/insights/addLabel/auditLabelCorrection',
    data,
    headers: { ...DATA_KNOWLEDGE_API_KEY_HEADER }
  })
}

/**
 * @description: 查询纠错信息明细
 * @param {any} data
 * @return {*}
 */
export const queryCorrectionInfo = (data?: any) => {
  return request({
    method: 'post',
    url: '/insights/addLabel/queryCorrectionInfo',
    data,
    headers: { ...DATA_KNOWLEDGE_API_KEY_HEADER }
  })
}
