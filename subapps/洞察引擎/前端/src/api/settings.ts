import request from './index'

/**
 * 新增客户信息
 * @param data
 */
export const saveCustomerInfo = (data: any) => {
  return request<any>({
    url: '/insights/customer/saveCustomerInfo',
    method: 'POST',
    data
  })
}

/**
 * 更新客户信息
 * @param data
 */
export const updateCustomerInfo = (data: any) => {
  return request<any>({
    url: '/insights/customer/updateCustomerInfo',
    method: 'POST',
    data
  })
}

/**
 * 校验客户编码是否已存在
 * @param data
 */
export const checkCustomerCode = (data: { code: string; id?: string }) => {
  return request<any>({
    url: `/insights/customer/checkCustomerCode`,
    method: 'post',
    data
  })
}

/**
 * 账号管理-根据id查询账号信息
 * @param data
 */
export const findAccountInfo = (data: any) => {
  return request<any>({
    url: `/insights/accountInfo/findAccountInfo`,
    method: 'post',
    data
  })
}
/**
 * 账号管理-新增账号信息
 * @param data
 */
export const saveAccountInfo = (data: any) => {
  return request<any>({
    url: `/insights/accountInfo/saveAccountInfo`,
    method: 'post',
    data
  })
}
/**
 * 账号管理-更新账号信息
 * @param data
 */
export const updateAccountInfo = (data: any) => {
  return request<any>({
    url: `/insights/accountInfo/updateAccountInfo`,
    method: 'post',
    data
  })
}
/**
 * 获取角色名称下拉
 * @param data
 */
export const queryRoleALlList = (data: Api.Common.Params) => {
  return request<any>({
    url: '/insights/accountInfo/queryRoleALlList',
    method: 'POST',
    data
  })
}

/**
 * @description: 部门下拉
 * @param {*} Promise
 * @return {*}
 */
export const findDepartList = (data: any): Promise<any> => {
  return request<any>({
    url: '/insights/accountInfo/findDepartList',
    method: 'POST',
    data
  })
}

/**
 * @description: 查询角色下拉
 * @param {any} params
 * @return {*}
 */
export const accountInfoQueryRoleALlList = (params: any): Promise<any> => {
  return request<any>({
    url: '/insights/accountInfo/queryRoleALlList',
    method: 'POST',
    data: params
  })
}

/**
 * 根据id查询客户信息
 * @param data
 */
export const findCustomerInfo = (data: any) => {
  return request<any>({
    url: `/insights/customer/findCustomerInfo`,
    method: 'post',
    data
  })
}

/**
 * 根据客户ID查询客户编码
 * @param params
 */
export const queryCodeById = (params: Api.Role.ClientId) => {
  return request<any>({
    url: `/insights/customer/queryCodeById`,
    method: 'get',
    params
  })
}
