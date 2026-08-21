import http from '../http'
import type { HttpRequestConfig } from '@h5/api/http'

/**
 * @description: 获取 canswer AuthDataUrl
 * @param {any} params
 * @return {*}
 */
export const getAuthDataUrl = (params: any): Promise<BaseResponse<any>> => {
  return http({
    url: '/report/canswer/getAuthDataUrl',
    method: 'get',
    params
  })
}

/**
 * @description: 洞察引擎免密登录接口
 * @param {any} data
 * @return {*}
 */
export const insFreeRedictLogin = (data: {
  username?: string
  userId?: string
}): Promise<BaseResponse<any>> => {
  return http({
    url: '/insights/base/freeLogin',
    method: 'POST',
    data
  })
}

// 获取用户权限所有数据 包含用户所有信息等
export const fetchH5UserPermissions = (config?: HttpRequestConfig): Promise<BaseResponse> => {
  return http.post('/report/userPermissions', undefined, config)
}
