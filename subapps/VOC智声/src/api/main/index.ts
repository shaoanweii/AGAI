import request from '@/api/http/index'
import type { LoginReq, LoginRes } from './type'

/**
 * 登录
 * @param data
 */
export const postLogin = (data: LoginReq) => {
  return request<LoginRes>({
    url: '/report/base/login',
    method: 'POST',
    data
  })
}

/**
 * 登出
 */
export const postLogout = () => {
  return request<any>({
    url: '/report/logout',
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
 * 获取用户信息
 */
export const userInfo = () => {
  return request<any>({
    method: 'post',
    url: `/report/userInfo`
  })
}

/**
 * 获取用户权限、全局客户选项，客户默认值等信息
 * 获取用户菜单, 权限, 高级筛选,等一些公共数据
 */
export const userPermissions = () => {
  return request<any>({
    method: 'post',
    url: `/report/userPermissions`
  })
}
