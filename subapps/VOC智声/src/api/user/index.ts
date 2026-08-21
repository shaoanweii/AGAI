/**
 * 账号管理服务 API
 */

import request from '@/api/http'
import type {
  AccountInfo,
  AccountQueryParams,
  CreateAccountParams,
  UpdateAccountParams,
  RoleOption,
  DepartmentInfo,
  GetTimeParams,
  GetTagTypeParams
} from './types'

/**
 * 分页查询账号信息列表
 * @param data 查询参数
 * @returns 操作结果
 */
export const findAccountInfoList = (data: AccountQueryParams): Promise<BaseResponse<any>> => {
  return request.post('/report/accountInfo/findAccountInfoList', data)
}

/**
 * 更新账号信息
 * @param data 查询参数
 * @returns 操作结果
 */
export const updateAccountInfo = (data: AccountInfo): Promise<BaseResponse<any>> => {
  return request.post('/report/accountInfo/updateAccountInfo', data)
}

/**
 * 根据ID查询账号信息
 * @param data 查询参数
 * @returns 账号信息
 */
export const findRegulationInfo = (data: { id: string }): Promise<BaseResponse<AccountInfo>> => {
  return request.post('/report/accountInfo/findAccountInfo', data)
}

/**
 * 新增账号信息
 * @param data 账号信息
 * @returns 操作结果
 */
export const saveAccountInfo = (data: CreateAccountParams): Promise<BaseResponse<any>> => {
  return request.post('/report/accountInfo/saveAccountInfo', data)
}

/**
 * 更新账号信息
 * @param data 账号信息
 * @returns 操作结果
 */
export const updateRegulationInfo = (data: UpdateAccountParams): Promise<BaseResponse<any>> => {
  return request.post('/report/accountInfo/updateRegulationInfo', data)
}

/**
 * 根据ID删除账号信息
 * @param data 删除参数
 * @returns 操作结果
 */
export const deleteAccountInfo = (data: { id: string }): Promise<BaseResponse<any>> => {
  return request.post('/report/accountInfo/deleteAccountInfo', data)
}

/**
 * 获取角色名称下拉列表
 * @returns 角色选项列表
 */
export const queryRoleALlList = (params: any): Promise<BaseResponse<RoleOption[]>> => {
  return request.post('/report/accountInfo/queryRoleALlList',  params)
}

/**
 * 获取列表 (部门列表)
 * @returns 部门列表
 */
export const findDepartList = (): Promise<BaseResponse<any[]>> => {
  return request.post('/report/accountInfo/findDepartList')
}




/**
 * 获取列表 (部门列表)
 * @returns 部门列表
 */
export const findDepartList_1 = (): Promise<BaseResponse<DepartmentInfo[]>> => {
  return request.post('/report/accountInfo/findDepartList_1')
}


/**
 * @description: 根据字典编码获取字典项列表
 * @param {string} dictIdCode 字典编码
 * @return {*}
 */
export const getDictItemsByDict = (dictIdCode: string): Promise<BaseResponse<any[]>> => {
  return request.get(`/report/insDictItem/dict-items-by-dict/${dictIdCode}`)
}

/**
 * getTime_7
 * @param params 时间参数
 * @returns 时间数据
 */
export const getTime_7 = (params: GetTimeParams): Promise<BaseResponse<any>> => {
  return request.get('/report/accountInfo/getTime', { params })
}

/**
 * getTagType_7
 * @param params 标签类型参数
 * @returns 标签类型数据
 */
export const getTagType_7 = (params: GetTagTypeParams): Promise<BaseResponse<any>> => {
  return request.get('/report/accountInfo/getTagType', { params })
}

/**
 * getRiskLevel_7
 * @returns 风险等级数据
 */
export const getRiskLevel_7 = (): Promise<BaseResponse<any>> => {
  return request.get('/report/accountInfo/getRiskLevel')
}

/**
 * 查询条件 (conditions_4)
 * @returns 查询条件数据
 */
export const conditions_4 = (): Promise<BaseResponse<any>> => {
  return request.get('/report/accountInfo/conditions')
}

// 导出所有接口
export default {
  findAccountInfoList,
  findRegulationInfo,
  saveAccountInfo,
  updateRegulationInfo,
  deleteAccountInfo,
  queryRoleALlList,
  findDepartList_1,
  getTime_7,
  getTagType_7,
  getRiskLevel_7,
  conditions_4
}
