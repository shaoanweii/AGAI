import request from '@h5/api/http'
import type { H5VocBaseRequest } from '@h5/api/home/types'

/**
 * 获取H5报告
 * POST /mobileTerminal/report/getReport
 * @param data 仅需品牌编码
 */
export const getH5Report = (data: { brandCode: string }): Promise<BaseResponse<any>> => {
  return request.post('/report/mobileTerminal/report/getReport', data)
}

/**
 * 根据文件名称获取文件地址
 * POST /report/subscribe-task/getPdfFileUrl
 * @param data
 */
export const getFileByFileName = (fileName: string): Promise<BaseResponse<any>> => {
  return request.get(`/report/subscribe-task/getPdfFileUrl/${fileName}`)
}

/**
 * 根据任务id或者报告id查询-查询条件
 * POST /report/subscribe-task/getPdfFileUrl
 * @param data
 */
export const getQueryDataByid = (params: any): Promise<BaseResponse<any>> => {
  // 将params对象转成url参数
  const query = Object.keys(params)
    .filter(key => params[key])
    .map(key => `${key}=${params[key]}`)
    .join('&')
  return request.get(`/report/subscribe-task/queryFilter?${query}`)
}

export default {}
