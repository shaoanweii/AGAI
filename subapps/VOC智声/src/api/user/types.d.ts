/**
 * 账号管理服务类型定义
 */

/**
 * 账号信息模型 (InsReportAccountInfoModel)
 */
export interface AccountInfo {
  /** 页面大小 */
  pageSize?: number
  /** 页码 */
  pageNum?: number
  /** 排序 */
  order?: string
  /** 用户ID */
  userId?: string
  /** 部门ID */
  deptId?: string[] | string
  /** 部门名称 */
  deptName?: string
  /** 角色ID */
  roleId?: string
  /** 操作权限ID */
  operationRoleId?: string
  /** 员工编号 */
  employeeId?: string
  /** 账号名称 */
  accountName?: string
  /** 账号密码 */
  accountPwd?: string
  /** 用户名 */
  userName?: string
  /** 联系方式 */
  contact?: string
  /** 职位 */
  position?: string
  /** 邮箱 */
  email?: string
  /** 备注 */
  remark?: string
  /** 停用/启用状态 停用:0 启用:1 默认启用 */
  status?: string
  /** 客户ID */
  clientId?: string
  /** 启用状态 */
  enable?: string
  /** 登录类型 表单:base 邮箱:email 默认为表单类型 */
  loginType?: string
  /** 完成率 */
  completeRate?: number | undefined
}

/**
 * 账号查询参数 (分页查询账号信息列表)
 */
export interface AccountQueryParams extends AccountInfo {
  /** 页码 */
  pageNum: number
  /** 页面大小 */
  pageSize: number
  /** 角色ID集合 */
  roleIds?: string[]
  /** 操作角色ID集合 */
  operationRoleIds?: string[]
  /** 开始时间 */
  startTime?: string
  /** 结束时间 */
  endTime?: string
  /** 排序 */
  order?: string
}

/**
 * 新增账号参数 (saveAccountInfo)
 */
export interface CreateAccountParams extends AccountInfo {
  // 继承 AccountInfo 的所有字段
}

/**
 * 更新账号参数 (updateRegulationInfo)
 */
export interface UpdateAccountParams extends AccountInfo {
  // 继承 AccountInfo 的所有字段
}

/**
 * 根据ID查询账号信息参数 (findRegulationInfo)
 */
export interface FindAccountByIdParams {
  /** 账号ID */
  id: string
}

/**
 * 根据ID删除账号信息参数 (deleteAccountInfo)
 */
export interface DeleteAccountParams {
  /** 账号ID */
  id: string
}

/**
 * 角色下拉选项 (queryRoleALlList)
 */
export interface RoleOption {
  /** 角色名称 */
  roleName?: string
  /** 角色代码 */
  roleId: string
  id: string
  /** 状态 */
  enabled?: string | number
  /** 分页大小 */
  pageSize?: number
  /** 页码 */
  pageNum?: number
  /** 排序 */
  order?: string
  /** 客户ID */
  clientId?: string
  /** 搜索关键字 */
  searchKeyword?: string
  /** 品牌编码 */
  brandCode?: string
  /** 品牌名称 */
  brandName?: string
  /** 是否校验管理员 */
  checkAdmin?: boolean
  /** 是否全选 */
  selectAll?: boolean
  /** 权限ID列表 */
  permissionIdList?: string[]
  /** 标签类型 */
  tagLibType?: string
}

/**
 * 获取列表参数 (findDepartList)
 */
export interface DepartmentListParams {
  // 根据 Swagger 文档，这个接口只需要 Authorization header，没有其他参数
}

/**
 * 部门信息
 */
export interface DepartmentInfo {
  /** 部门ID */
  id?: string
  /** 部门名称 */
  departmentName?: string
  /** 部门代码 */
  departmentCode?: string
  /** 父级部门ID */
  parentId?: string
  /** 排序 */
  sort?: number
  /** 状态 */
  status?: string
  /** 创建时间 */
  createTime?: string
  /** 更新时间 */
  updateTime?: string
  /** 备注 */
  remark?: string
}

/**
 * 获取时间参数 (getTime)
 */
export interface GetTimeParams {
  /** 类型 */
  type: string
}

/**
 * 获取标签类型参数 (getTagType)
 */
export interface GetTagTypeParams {
  /** 品牌代码 */
  brandCode: string
}
