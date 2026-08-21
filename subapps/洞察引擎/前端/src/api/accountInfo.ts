import request from './index'

/**
 * 获取部门-账号树
 * - 统一走 /api/insights/accountInfo/findDepartAccountTree（baseURL 默认 /api）
 * - 返回结构通常为：[{ id,name,code, account: [], child: [] }]
 */
export const findDepartAccountTree = () => {
  return request<any>({
    method: 'get',
    url: '/insights/accountInfo/findDepartAccountTree'
  })
}
