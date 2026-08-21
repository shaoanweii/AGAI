/**
 * VOC数据项接口
 */
export interface VocDataItem {
  /** 日期 */
  date?: string
  /** 总提及量 */
  totalMentions?: number
  /** 正面提及量 */
  positiveMentions?: number
  /** 中性提及量 */
  neutralMentions?: number
  /** 负面提及量 */
  negativeMentions?: number
  /** 体验值 */
  experienceValue?: number
  /** 体验值环比 */
  experienceValueMoM?: number
}

/**
 * 客户标签体验值VO
 */
export interface CustomerTagExperienceVo {
  /** 全旅程客户标签名称 */
  customerTagName?: string
  /** 全旅程客户标签体验值 */
  experienceValue?: number
  /** 正面提及量 */
  positiveMentions?: number
  /** 中性提及量 */
  neutralMentions?: number
  /** 负面提及量 */
  negativeMentions?: number
  /** 总提及量 */
  totalMentions?: number
}

/**
 * 标签接口
 */
export interface LabelTag {
  id?: string
  tagParentId?: string
  tagName?: string
  tagCode?: string
  checked?: boolean
  child?: LabelTag[]
}

export interface AttributeLabelItem {
  id?: string
  name?: string
}

export interface InsReportAccountInfoVo {
  /** 用户id */
  userId?: string

  /** 账号名称 */
  accountName?: string

  /** 用户名 */
  userName?: string

  /** 员工编号 */
  employeeId?: string

  /** 部门名称 */
  deptName?: string

  /** 部门id */
  deptId?: string

  /** 一级部门名称 */
  firstDeptName?: string

  /** 一级部门编号 */
  firstDeptCode?: string

  /** 二级部门名称 */
  secondDeptName?: string

  /** 二级部门编号 */
  secondDeptCode?: string

  /** 三级部门名称 */
  thirdDeptName?: string

  /** 三级部门编号 */
  thirdDeptCode?: string

  /** 角色名称 */
  roleName?: string

  /** 角色id */
  roleId?: string

  /** 操作角色名称 */
  operationRoleName?: string

  /** 操作角色id */
  operationRoleId?: string

  /** 停用/启用状态：停用(0)、启用(1)，默认启用 */
  status?: string

  /** 登录次数 */
  loginCounts?: number // 对应 integer(int64)

  /** 最后一次登录时间（日期时间格式） */
  lastLoginTime?: string // 对应 date-time 格式字符串

  /** 完成率 */
  completeRate?: number // 对应 integer(int32)

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

  /** 电话（字段名称未明确，保留原始定义） */
  phone?: string

  /** 原声聆听数 */
  originalListenCount?: number

  /** 聆听任务完成率 */
  listenTaskCompleteRate?: string

  /** 访问时长 */
  visitDuration?: string

  /** 抱怨原声聆听数 */
  complainOriginalListenCount?: number

  /** 开始时间 */
  startTime?: string
}

export interface InsReportSysDepartVo {
  id?: string
  // 名称
  name?: string
  // 编号
  code?: string
  // 上级id
  parentId?: string
  // 部门下的子部门
  child?: InsReportSysDepartVo[]
  // 部门下的账号
  account?: InsReportAccountInfoVo[]
}

export interface InsReportAccountInfoModel {
  /** 每页条数 */
  pageSize?: number // 对应 integer(int32)

  /** 页码 */
  pageNum?: number // 对应 integer(int32)

  /** 排序字段（格式通常为 "字段名,asc/desc"，如 "lastLoginTime,desc"） */
  order?: string

  /** 用户id */
  userId?: string

  /** 部门id列表（数组类型，元素为部门id字符串） */
  deptId?: string[]

  /** 角色ID */
  roleId?: string

  /** 操作角色ID */
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

  /** 停用/启用状态：停用(0)、启用(1)，默认启用 */
  status?: string

  /** 客户id */
  clientId?: string

  /** 启用状态标识（字段含义未明确，保留原始定义） */
  enable?: string

  /** 登录类型：表单(base)、邮箱(email)，默认为表单类型 */
  loginType?: string

  /** 完成率 */
  completeRate?: number // 对应 integer(int32)

  /** 开始时间 */
  startTime?: string

  /** 结束时间 */
  endTime?: string

  /** 用户id列表 */
  userIds?: string[]

  /** 应用id */
  appId?: string

  /** 任务id */
  taskId?: string

  /** 文件名 */
  fileName?: string

  /** 文件类型 */
  fileType?: string
}

export interface TagLibParams {
  tagType?: string
  tagParentId?: string
  codes?: string[]
}

export interface TagLibClientTreeVo {
  /** 标签唯一标识 */
  id?: string
  /** 父标签ID */
  tagParentId?: string
  /** 标签名称（中文） */
  tagName?: string
  /** 标签名称（英文） */
  tagNameEn?: string
  /** 标签编码 */
  tagCode?: string
  /** 标签类型 */
  tagType?: string
  /** 标签属性 */
  tagAttribute?: string
  /** 能源类型列表 */
  energyType?: string[]
  /** 车辆类型列表 */
  carType?: string[]
  /** 标签状态 */
  tagStatus?: string
  /** 标签描述 */
  tagDescription?: string
  /** 严重程度 */
  seriousness?: string
  /** 用户旅程列表 */
  userJourney?: string[]
  /** 情感倾向 */
  emotion?: string
  /** 应用客户端类型 */
  appClient?: string
  /** 标签库名称层级结构 */
  tagLibNameHierarchical?: string
}

export interface CorpusAuditRequestItem {
  pageSize: number
  pageNum: number
  order: string
  /** 主键 */
  id: string
  /** 模型id */
  modelId: string
  /** 主键集合 */
  ids: string[]
  /** 原语料主体 */
  corpusSubject: string
  /** 原语料描述 */
  corpusDesc: string
  startTime: string
  endTime: string
  /** 修改后的语料主体 */
  modifiedCorpusSubject: string
  /** 修改后的语料描述 */
  modifiedCorpusDesc: string
  /** 原观点编码 */
  opinionCode: string
  /** 观点编码集合 */
  opinionCodeList: string[]
  /** 标签编码集合 */
  tagCodeList: string[]
  /** 原观点名称 */
  opinionName: string
  /** 修改后的观点编码 */
  modifiedOpinionCode: string
  /** 修改后的观点名称 */
  modifiedOpinionName: string
  /** 原状态 */
  status: string
  /** 修改后的状态 */
  modifiedStatus: 'enabled' | 'disabled'
  /** 语料类型：文本语料 1，问卷语料 0 */
  corpusType: '0' | '1'
  /** 操作类型：add 新增 */
  operateType: 'add'
  auditStatus: string
  auditStatusList: string[]
  auditUser: string
  auditTime: string
  /** 发起人 */
  initiator: string
  initiatorList: string[]
  initiateTime: string
  /** 客户id：新增语料按审核接口样例传空字符串 */
  clientId: string
  /** 模型id集合 */
  modelIds: string[]
}

export interface CorpusAuditSavePayload {
  /** 客户id：新增语料按审核接口样例传空字符串 */
  clientId: string
  /** 语料审核新增列表 */
  corpusAuditList: CorpusAuditRequestItem[]
}
