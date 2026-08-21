import http from '../http/index'
import type { FindReportDownLoadFileList, IPageReportDownLoadFileVo } from './index.d'

/**
 * @description: 下载账号信息
 * @param {any} data
 * @return {*}
 */
export const exportAccountInfo = (data?: any): Promise<BaseResponse<any>> => {
  return http({
    url: '/report/accountInfo/exportAccountInfo',
    method: 'post',
    data
  })
}
/**
 * @description: 查询下载列表
 * @param {any} data
 * @return {*}
 */
export const findReportDownLoadFileList = (
  data?: FindReportDownLoadFileList
): Promise<BaseResponse<IPageReportDownLoadFileVo>> => {
  return http({
    url: '/report/reportDownLoad/findReportDownLoadFileList',
    method: 'post',
    data
  })
}

/**
 * @description: 重新下载
 * @param {any} data
 * @return {*}
 */
export const downloadAgain = (data: { id: string }): Promise<BaseResponse<any>> => {
  return http({
    url: '/report/reportDownLoad/downloadAgain',
    method: 'post',
    data
  })
}

/**
 * @description: 获取可见用户列表
 * @param {boolean} isAllVisible
 * @return {*}
 */
export const findVisibleUserList = (isAllVisible: boolean): Promise<BaseResponse<any>> => {
  return http({
    url: '/report/reportDownLoad/findVisibleUserList',
    method: 'get',
    params: { isAllVisible }
  })
}
