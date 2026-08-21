/**
 * 新车上市模块API接口
 */

import request from '@/api/http'
/**
 * 创建订阅报告
 * @param params 查询条件
 * @returns 订阅报告数据
 */
export const createSubscribeReport = (params: any): Promise<any> => {
  return request.post('/report/subscribe-task/create', params)
}

/**
 * 查询订阅任务列表
 * @param params 查询条件
 * @returns 订阅任务数据
 */
export const findSubscribeTaskList = (params: any): Promise<any> => {
  return request.post('/report/subscribe-task/list', params)
}

/**
 * 查询推送任务列表
 * @param params 查询条件
 * @returns 推送任务数据
 */
export const findPushTaskList = (params: any): Promise<any> => {
  return request.post('/report/subscribe-task/push-record-list', params)
}

/**
 * @description: 根据订阅id查询订阅任务详情
 * @param {string} id 订阅任务ID
 * @return {*}
 */
export const getSubscribeTaskDetail = (id: string): Promise<BaseResponse<any>> => {
  return request.get(`/report/subscribe-task/get/${id}`)
}

/**
 * 更新订阅任务列表
 * @param params 查询条件
 * @returns 订阅任务数据
 */
export const updateSubscribeTaskList = (params: any): Promise<any> => {
  return request.post('/report/subscribe-task/update', params)
}

/**
 * @description: 根据订阅id更新任务状态
 * @param {string} id 订阅任务ID
 * @return {*}
 */
export const updateSubscribeTaskStatus = (
  id: string,
  status: number
): Promise<BaseResponse<any>> => {
  return request.post(`/report/subscribe-task/updateStatus/${id}/${status}`)
}

// 删除订阅任务
export const deleteSubscribeTask = (id: string): Promise<any> => {
  return request.post(`/report/subscribe-task/delete/${id}`)
}

/**
 * @description: 获取查询列表顶部的人员列表
 * @return {*}
 */
export const getSubscribeTaskUserList = (): Promise<BaseResponse<any>> => {
  return request.get(`/report/subscribe-task/creator`)
}

// 导出订阅报告
export const exportSubscribeReport = (data: any): Promise<BaseResponse<any>> => {
  return request({
    url: '/report/subscribe-task/export',
    method: 'post',
    data,
    responseType: 'blob'
  })
}

// 导出推送记录
export const exportPushRecord = (data: any): Promise<BaseResponse<any>> => {
  return request({
    url: '/report/subscribe-task/push-record-export',
    method: 'post',
    data,
    responseType: 'blob'
  })
}

/**
 * @description: 根据任务id或者报告id查询-查询条件
 * @return {*}
 */
export const getQueryDataByid = (params: any): Promise<BaseResponse<any>> => {
  return request.get(`/report/subscribe-task/queryFilter`, { params })
}
