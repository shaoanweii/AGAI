/**
 * 角色信息模块类型定义
 * 基于 Swagger 文档生成的接口类型
 */

/**
 * 角色查询参数
 */
export interface RoleQueryParams {
  /** 分页大小 */
  pageSize?: number
  /** 页码 */
  pageNum?: number
  /** 排序 */
  order?: string
  /** 角色状态 */
  enabled?: string
  /** 角色名称 */
  roleName?: string
  /** 客户ID */
  clientId?: string
  /** 角色ID */
  roleId?: string
  /** 搜索关键字 */
  searchKeyword?: string
  roleType?: string
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
 * 角色保存或更新请求参数
 */
export interface SaveOrUpdateRoleRequest {
  /** 角色Id编辑时必传 */
  id?: string
  /** 角色ID */
  roleId?: string
  /** 客户ID不能为空 */
  clientId: string
  /** 角色名称 */
  roleName: string
  /** 菜单IdList */
  permissionIdList: string[]
  /** 关联车系，传code以后英文逗号(,)分隔 */
  seriesIds: string[]
  /** 关联渠道ID，以后英文逗号(,)分隔 */
  channelIds: string[]
  /** 关联业务标签，传code以后英文逗号(,)分隔 */
  businessTagIds?: string[]
  /** 关联业务标签，传code以后英文逗号(,)分隔 */
  serviceTagIds?: string[]
  /** 关联质量标签，传code以后英文逗号(,)分隔 */
  qualityTagIds?: string[]
  /** 关联业务标签，传code以后英文逗号(,)分隔 */
  areaIds: string[]
  /** 功能权限：是否可以导出 true为是 */
  isExport?: boolean
  /** 功能权限：是否可以下载 true为是 */
  isDownload?: boolean
  /** 是否拥有所有权限 true:是 */
  allPermission?: boolean
  /** 品牌code */
  brandCode: string
  /** 单点事件范围 */
  singleEventScope?: string[]
  /** 单点事件操作 */
  singleEventOperation?: string[]
  /** 批量事件范围 */
  batchEventScope?: string[]
  /** 批量事件操作 */
  batchEventOperation?: string[]
  /** 角色状态 */
  enabled?: number
  /** 备注 */
  remark?: string
}

/**
 * 查询用户权限请求参数
 */
export interface QueryUserPermissionRequest {
  /** 用户ID */
  userId: string
  /** 客户ID */
  clientId: string
  /** 是否树形结构 */
  tree?: boolean
  /** 是否管理员 */
  admin?: boolean
}

/**
 * 角色权限树节点
 */
export interface RoleReportAuthTree {
  /** 菜单ID */
  id?: string
  /** Code */
  code?: string
  /** 父级ID */
  pid?: string
  /** icon */
  icon?: string
  /** 菜单名称 */
  name?: string
  /** 路径 */
  path?: string
  /** 权限键 */
  permissionKey?: string
  /** 是否是按钮 */
  checkButton?: boolean
  /** 排序 */
  sort?: number
  /** 是否选中 */
  checked?: boolean
  /** 下钻权限 */
  drillDownPermission?: boolean
  /** API路径 */
  apiPath?: string
  /** 总是显示 */
  alwaysShow?: string
  /** 子节点 */
  children?: RoleReportAuthTree[]
}

/**
 * 关联车系信息
 */
export interface RelationCarVo {
  /** ID */
  id?: string
  /** 车系代码 */
  carSeriesCode?: string
  /** 车系名称 */
  carSeriesName?: string
  /** 是否选中 */
  checked?: boolean
}

/**
 * 角色权限认证信息
 */
export interface RoleReportAuthVo {
  /** 数据渠道 */
  dataChannel?: any
  /** 关联业务标签 */
  relationBuTag?: any
  /** 质量标签 */
  qualityTag?: any
  /** 服务标签 */
  serviceTag?: any
  /** 关联车系 */
  relationCar?: RelationCarVo[]
  /** 应用看板 */
  appKanban?: RoleReportAuthTree[]
  /** 区域 */
  area?: any
  /** 关联车系，传code以后英文逗号(,)分隔 */
  seriesIds?: string[]
  /** 服务标签，传code以后英文逗号(,)分隔 */
  serviceTagIds?: string[]
  /** 关联渠道ID，以后英文逗号(,)分隔 */
  channelIds?: string[]
  /** 关联产品标签，传code以后英文逗号(,)分隔 */
  businessTagIds?: string[]
  /** 关联质量标签，传code以后英文逗号(,)分隔 */
  qualityTagIds?: string[]
  /** 关联业务标签，传code以后英文逗号(,)分隔 */
  areaIds?: string[]
  /** 角色ID */
  roleId?: string
  /** 角色名称 */
  roleName?: string
  /** 角色类型 */
  roleType?: number
  /** 品牌代码 */
  brandCode?: string
  /** 品牌名称 */
  brandName?: string
  /** 品牌代码列表 */
  brandCodeList?: string[]
  /** 是否可导出 */
  isExport?: boolean
  /** 是否可下载 */
  isDownload?: boolean
  /** 是否拥有所有权限 */
  allPermission?: boolean
  /** 单点事件范围 */
  singleEventScope?: string[]
  /** 单点事件操作 */
  singleEventOperation?: string[]
  /** 批量事件范围 */
  batchEventScope?: string[]
  /** 批量事件操作 */
  batchEventOperation?: string[]
  /** 状态 */
  status?: number
  /** 备注 */
  remark?: string
  /** 是否选中 */
  checked?: boolean
  /** 应用标签 */
  appTags?: string[]
}

export interface AccountListParams {
  /** 分页大小 */
  pageSize?: number
  /** 页码 */
  pageNum?: number
  // 1 已关联   2 未关联
  accountType: string
  /** 部门ID */
  deptId?: string
  deptIdList?: string[]
  searchKeyword?: string
}

export interface AccountListUserInfo {
  /** 用户id */
  userId: string
  /** 账号名称 */
  accountName: string
  /** 用户名 */
  userName: string
  /** 员工编号 */
  employeeId: string
  /** 部门名称 */
  deptName: string
  /** 部门id */
  deptId: string
  /** 一级部门名称 */
  firstDeptName: string
  /** 一级部门编号 */
  firstDeptCode: string
  /** 二级部门名称 */
  secondDeptName: string
  /** 二级部门编号 */
  secondDeptCode: string
  /** 三级部门名称 */
  thirdDeptName: string
  /** 三级部门编号 */
  thirdDeptCode: string
  /** 角色名称 */
  roleName: string
  /** 角色id */
  roleId: string
  /** 停用/启用状态 停用:0 启用:1 默认启用 */
  status: '0' | '1'
  /** 登录次数 */
  loginCounts: number
  /** 最后一次登录时间（格式：yyyy-MM-dd HH:mm:ss） */
  lastLoginTime: string
  /** 完成率 */
  completeRate?: number
  /** 联系方式 */
  contact?: string
  /** 职位 */
  position?: string
  /** 邮箱 */
  email?: string
  /** 备注 */
  remark?: string
  /** 办公电话 */
  officePhone?: string
  /** 家庭电话 */
  homePhone?: string
  /** 电话（原属性无注释） */
  phone?: string
  /** 原声聆听数 */
  originalListenCount?: number
  /** 聆听任务完成率（字符串类型，适配百分比/小数等场景） */
  listenTaskCompleteRate?: string
  /** 访问时长（字符串类型，适配如"1h30m"、"90分钟"等格式） */
  visitDuration?: string
  /** 抱怨原声聆听数 */
  complainOriginalListenCount?: number
  /** 开始时间（日期格式，适配yyyy-MM-dd） */
  startTime?: string
}

export interface BatchRole {
  roleId: string
  userIdList: string[]
}
