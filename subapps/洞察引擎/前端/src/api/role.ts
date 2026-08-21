import request from './index'

/**
 * 根据Id查询单条信息
 * @param data
 */
export const queryRoleInfo = (data: Api.Role.QueryInfoById) => {
  return request<Api.Role.QueryInfoByIdRecord>({
    url: '/insights/role/queryRoleInfo',
    method: 'POST',
    data
  })
}

/**
 * 获取权限菜单下拉
 * @param data
 */
export const queryMenuPermissionList = (data: Api.Role.QueryMenu) => {
  return request<Api.Role.PermissionTree[]>({
    url: '/insights/role/queryMenuPermissionList',
    method: 'POST',
    data
  })
}

/**
 * 新增/更新角色信息
 * @param data
 */
export const saveOrUpdateRole = (data: Api.Role.RoleParams) => {
  return request<any>({
    url: '/insights/role/saveOrUpdateRole',
    method: 'POST',
    data
  })
}

/**
 * @description: 关联账户列表
 * @return {*}
 */
export const getUserRoleList = (data: Api.Role.RoleParams) => {
  return request<any>({
    url: '/insights/role/getUserRoleList',
    method: 'POST',
    data
  })
}

// export const deleteRoleId = (data: Api.Role.RoleParams) => {
//   return request<any>({
//     url: '/insights/role/deleteRoleId',
//     method: 'POST',
//     data
//   })
// }
