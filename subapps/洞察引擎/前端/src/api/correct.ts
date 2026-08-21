import request from './index'

/**
 * @description: 标签纠错-添加
 * @return {*}
 */
export const insertLabelCorrection = (data: any) => {
  return request<any>({
    url: `/insights/addLabel/insertLabelCorrection`,
    method: 'post',
    data
  })
}

/**
 * @description: 标签纠错-查询
 * @return {*}
 */
export const queryDataInfo = (data: any) => {
  return request<any>({
    url: `/insights/addLabel/queryDataInfo`,
    method: 'post',
    data
  })
}

/**
 * @description: 标签纠错-历史记录
 * @return {*}
 */
export const getLabelHistoryRecordList = (data: any) => {
  return request<any>({
    url: `/insights/addLabel/labelHistoryRecordList`,
    method: 'post',
    data
  })
}

/**
 * @description: 标签纠错-观点,情感
 * @return {*}
 */
export const getConditions = (data: any) => {
  return request<any>({
    url: `/insights/addLabel/conditions`,
    method: 'get',
    params: data
  })
}

/**
 * @description: 标签纠错-标签
 * @return {*}
 */
export const findTagList = (data: any) => {
  return request<any>({
    url: `/insights/addLabel/findTagList`,
    method: 'post',
    data
  })
}
