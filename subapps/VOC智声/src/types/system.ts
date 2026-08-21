// 系统管理相关类型定义

// 用户状态枚举
export enum UserStatus {
  ACTIVE = 'active', // 正常
  DISABLED = 'disabled', // 禁用
  LOCKED = 'locked' // 锁定
}

// 角色状态枚举
export enum RoleStatus {
  ENABLE = 1, // 启用
  DISABLE = 0 // 禁用
}

// 数据权限范围枚举
export enum DataScope {
  ALL = 'all', // 全部数据
  DEPT = 'dept', // 本部门
  DEPT_AND_SUB = 'dept_sub', // 本部门及下级
  SELF = 'self' // 仅本人
}

// 权限类型枚举
export enum PermissionType {
  MENU = 'menu', // 菜单
  BUTTON = 'button', // 按钮
  API = 'api' // 接口
}

// 权限状态枚举
export enum PermissionStatus {
  ENABLE = 'enable', // 启用
  DISABLE = 'disable' // 禁用
}

// 基础权限接口
export interface Permission {
  id: number // 权限ID
  name: string // 权限名称
  code: string // 权限标识码
  type: PermissionType // 权限类型
  parentId?: number // 父权限ID
  path?: string // 路由路径
  component?: string // 组件路径
  icon?: string // 图标
  method?: string // HTTP方法（适用于API权限）
  sort: number // 排序
  status: PermissionStatus // 状态
  children?: Permission[] // 子权限（用于树形结构）
}

// 菜单权限接口
export interface Menu {
  id: number // 菜单ID
  name: string // 菜单名称
  path: string // 路由路径
  component?: string // 组件路径
  icon?: string // 图标
  parentId?: number // 父菜单ID
  sort: number // 排序
  status: PermissionStatus // 状态
  children?: Menu[] // 子菜单
}

// 角色接口
export interface Role {
  id: number // 角色ID
  roleId?: string // 角色ID
  name: string // 角色名称
  code: string // 角色编码（唯一）
  description?: string // 角色描述
  level: number // 角色层级（1-超级管理员，2-管理员，3-普通用户）
  permissions: Permission[] // 权限列表
  menus: Menu[] // 菜单权限
  dataScope: DataScope // 数据权限范围
  status: RoleStatus // 角色状态
  createTime: Date // 创建时间
  updateTime: Date // 更新时间
}

// 用户接口
export interface User {
  id: number // 用户ID
  username: string // 用户名（登录账号）
  nickname: string // 显示名称
  email: string // 邮箱
  phone?: string // 手机号
  avatar?: string // 头像URL
  status: UserStatus // 用户状态
  roles: Role[] // 关联角色列表
  lastLoginTime?: Date // 最后登录时间
  createTime: Date // 创建时间
  updateTime: Date // 更新时间
  remark?: string // 备注
}

// 分页查询参数
export interface PageParams {
  page: number // 页码
  pageSize: number // 每页大小
  total?: number // 总数
}

// 分页结果
export interface PageResult<T> {
  data: T[] // 数据列表
  total: number // 总数
  page: number // 当前页
  pageSize: number // 每页大小
}

// 用户查询参数
export interface UserQueryParams extends PageParams {
  username?: string // 用户名
  nickname?: string // 显示名称
  email?: string // 邮箱
  status?: UserStatus // 状态
  roleId?: number // 角色ID
  startTime?: string // 开始时间
  endTime?: string // 结束时间
}

// 角色查询参数
export interface RoleQueryParams extends PageParams {
  name?: string // 角色名称
  code?: string // 角色编码
  status?: RoleStatus // 状态
  level?: number // 角色层级
}

// 创建用户请求
export interface CreateUserRequest {
  username: string // 用户名
  nickname: string // 显示名称
  email: string // 邮箱
  phone?: string // 手机号
  password: string // 密码
  roleIds: number[] // 角色ID列表
  remark?: string // 备注
}

// 更新用户请求
export interface UpdateUserRequest {
  nickname?: string // 显示名称
  email?: string // 邮箱
  phone?: string // 手机号
  status?: UserStatus // 状态
  roleIds?: number[] // 角色ID列表
  remark?: string // 备注
}

// 创建角色请求
export interface CreateRoleRequest {
  name: string // 角色名称
  code: string // 角色编码
  description?: string // 角色描述
  level: number // 角色层级
  permissionIds: number[] // 权限ID列表
  menuIds: number[] // 菜单ID列表
  dataScope: DataScope // 数据权限范围
}

// 更新角色请求
export interface UpdateRoleRequest {
  name?: string // 角色名称
  description?: string // 角色描述
  level?: number // 角色层级
  permissionIds?: number[] // 权限ID列表
  menuIds?: number[] // 菜单ID列表
  dataScope?: DataScope // 数据权限范围
  status?: RoleStatus // 状态
}

// API 响应基础接口
export interface ApiResponse<T = any> {
  code: number // 响应码
  message: string // 响应消息
  data: T // 响应数据
  success: boolean // 是否成功
}

// 登录响应
export interface LoginResponse {
  token: string // JWT Token
  userInfo: User // 用户信息
  permissions: string[] // 权限列表
  menus: Menu[] // 菜单列表
}

// 权限验证参数
export interface PermissionCheck {
  permission: string | string[] // 权限标识
  mode?: 'any' | 'all' // 多权限验证模式
}
