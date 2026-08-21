import request from './index'
import type { Conditions } from '@/types'

interface LoginRes {
  access_token: string
  appId: string
  type: string
  name?: string
  employeeId?: string
  userid?: string
  username?: string
}

/**
 * 登录
 * @param data
 */
export const postLogin = (data: Api.User.LoginReq) => {
  return request<LoginRes>({
    url: '/insights/base/login',
    method: 'POST',
    data
  })
}

/**
 * 登出
 */
export const postLogout = () => {
  return request<any>({
    url: '/insights/logout',
    method: 'POST'
  })
}

/**
 * 获取验证码
 * @param param
 */
export const getRandomImage = (param: number) => {
  return request<any>({
    url: '/auth/randomImage/' + param,
    method: 'GET'
  })
}

/**
 * 查询渠道树
 * @param clientId
 * @param controller
 */
export const getGlobalChannelTreeByClientId = (clientId: string, controller = 'regulation') => {
  return request<Conditions[]>({
    method: 'GET',
    url: `/insights/${controller}/getChannelTree?clientId=${clientId}`
  })
}

/**
 * 获取用户信息
 */
export const userInfo = () => {
  return request<any>({
    method: 'post',
    url: `/insights/userInfo`
  })
}

/**
 * 获取用户权限、全局客户选项，客户默认值等信息
 */
export const userPermissions = () => {
  return request<any>({
    method: 'post',
    url: `/insights/userPermissions`
  })
}

/**
 * 获取导出列表
 */
export const getFile = (data: any) => {
  return request<any>({
    method: 'post',
    url: `/insights/getFile`,
    data
  })
}

/**
 * 根据文件地址下载文件
 */
export const downloadByUrl = (url: string, data?: any) => {
  return request<any>({
    method: 'get',
    url: url,
    data,
    responseType: 'blob'
  })
}

/**
 * @description: 体验代码
 * @param {any} data
 * @return {*}
 */
export const findTagTree = (data?: any) => {
  return request<any>({
    method: 'post',
    url: `/insights/insTagLibClient/findTagTree`,
    data
  })
}

/**
 * @description: 获取标签
 * @param {any} data
 * @return {*}
 */
export const getTagLibClientTree = (data?: any) => {
  return request<any>({
    method: 'post',
    url: `/insights/insTagLibClient/getTagLibClientTree`,
    data
  })
}

/**
 * @description: 根据标签ID查询向上层级路径
 * @param {any} data
 * @return {*}
 */
export const findAllUpTagLibHierarchicalByTagId = (data?: any) => {
  return request<any>({
    method: 'post',
    url: `/insights/insTagLibClient/findAllUpTagLibHierarchicalByTagId`,
    data
  })
}

/**
 * @description: 获取全量数据字典
 */
export const insAllDictItems = (): Promise<any> => {
  return request<any>({
    url: '/insights/insDictItem/insAllDictItems',
    method: 'POST'
  })
}
