import http from '@/api/http'
import type {
  RoleQueryParams,
  SaveOrUpdateRoleRequest,
  QueryUserPermissionRequest,
  RoleReportAuthVo,
  AccountListParams,
  AccountListUserInfo,
  BatchRole
} from './types'

/**
 * 角色信息模块 API 接口
 * 基于 Swagger 文档生成的接口实现
 */

/**
 * 分页查询角色信息
 * @param data 查询参数
 * @returns 角色列表
 */
export const queryRoleList = (data?: RoleQueryParams): Promise<BaseResponse<any>> => {
  return http.post('/report/role/list', data)
}

/**
 * 新增或更新角色信息
 * @param data 角色数据数组
 * @returns 操作结果
 */
export const saveOrUpdateRole = (data?: SaveOrUpdateRoleRequest[]): Promise<BaseResponse<any>> => {
  return http.post('/report/role/saveOrUpdateRole', data)
}

/**
 * 根据Id查询单条角色信息（查询用户权限）
 * @param data 查询参数
 * @returns 用户权限信息
 */
export const queryUserPermission = (
  data?: QueryUserPermissionRequest
): Promise<BaseResponse<any>> => {
  return http.post('/report/role/queryUserPermission', data)
}

/**
 * 获取权限菜单下拉列表
 * @param data 查询参数
 * @returns 权限菜单列表
 */
export const queryMenuPermissionList = (data?: RoleQueryParams): Promise<BaseResponse<any>> => {
  return http.post('/report/role/queryMenuPermissionList', data)
}

/**
 * 根据 RoleId 获取编辑回显的数据
 * @param data 查询参数
 * @returns 角色编辑数据
 */
export const getListByRoleId = (
  data?: RoleQueryParams
): Promise<BaseResponse<RoleReportAuthVo[]>> => {
  return http.post('/report/role/getListByRoleId', data)
}

/**
 * @description: 关联账户列表
 * @return {*}
 */
export const getUserRoleList = (data?: RoleQueryParams): Promise<BaseResponse<any>> => {
  return http.post('/report/role/getUserRoleList', data)
}

/**
 * @description: 删除角色
 * @param {RoleQueryParams} data
 * @return {*}
 */
export const deleteRoleId = (data?: RoleQueryParams): Promise<BaseResponse<any>> => {
  return http.post('/report/role/deleteRoleId', data)
}

/**
 * @description: 复制角色
 * @param {RoleQueryParams} data
 * @return {*}
 */
export const copyRoleId = (data?: RoleQueryParams): Promise<BaseResponse<any>> => {
  return http.post('/report/role/copyRole', data)
}

/**
 * @description: 关联账号分页查询
 * @param {RoleQueryParams} data
 * @return {*}
 */
export const accountList = (
  data: AccountListParams
): Promise<BaseResponse<AccountListUserInfo[]>> => {
  return http.post('/report/role/accountList', data)
}

/**
 * @description: 移除关联
 * @return {*}
 */
export const batchDeleteRole = (data: BatchRole): Promise<BaseResponse<any>> => {
  return http.post('/report/role/batchDeleteRole', data)
}

/**
 * @description: 添加关联
 * @param {BatchRole} data
 * @return {*}
 */
export const batchAddRole = (data: BatchRole): Promise<BaseResponse<any>> => {
  return http.post('/report/role/batchAddRole', data)
}

// 导出所有接口
export default {
  queryRoleList,
  saveOrUpdateRole,
  queryUserPermission,
  queryMenuPermissionList,
  getListByRoleId
}
