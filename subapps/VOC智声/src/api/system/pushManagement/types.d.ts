export type PushTaskStatusValue = string

/**
 * 推送管理查询参数
 */
export interface PushTaskQueryParams {
  /** 开始日期 */
  startDate: string
  /** 结束日期 */
  endDate: string
  /** 状态列表 */
  statusList?: PushTaskStatusValue[]
  /** 页码 */
  pageNum?: number
  /** 每页条数 */
  pageSize?: number
}

export type PushUserStatusValue = string

/**
 * 推送用户明细查询参数
 */
export interface PushMessageUserInfoQueryParams {
  /** 批次 ID */
  batchId: string
  /** 用户推送状态列表 */
  statusList?: PushUserStatusValue[]
  /** 页码 */
  pageNum?: number
  /** 每页条数 */
  pageSize?: number
}

/**
 * 推送用户明细导出参数。
 * 与明细分页查询条件保持一致，但导出不传分页参数。
 */
export type PushMessageUserInfoExportParams = Omit<
  PushMessageUserInfoQueryParams,
  'pageNum' | 'pageSize'
>

/**
 * 推送状态筛选项
 */
export interface PushTaskStatusOption {
  /** 展示文案 */
  label: string
  /** 选项值 */
  value: PushTaskStatusValue
}

/**
 * 推送任务列表项
 */
export interface PushTaskListItem {
  /** 任务 ID */
  id: string
  /** 推送总人数 */
  pushTotal: number | string
  /** 推送成功人数 */
  successTotal: number | string
  /** 推送类型 */
  pushType: string | null
  /** 推送开始时间 */
  pushTime: string | null
  /** 批次 ID */
  batchId: string | null
  /** 当前状态名称 */
  status: string | null
  /** 创建人 */
  createUser: string | null
  /** 创建人 ID */
  createUserId: string | null
  /** 创建时间 */
  createTime: string | null
  /** 当前状态编码 */
  statusCode: PushTaskStatusValue
  /** 推送成功率，范围 0-100 */
  successRate: number | string
}

/**
 * 推送任务分页结果
 */
export interface PushTaskPageResult {
  /** 总条数 */
  total: number
  /** 当前页数据 */
  list: PushTaskListItem[]
  /** 当前页码 */
  pageNum: number
  /** 每页条数 */
  pageSize: number
  /** 当前页数量 */
  size: number
  /** 当前页起始行 */
  startRow: number
  /** 当前页结束行 */
  endRow: number
  /** 总页数 */
  pages: number
  /** 上一页页码 */
  prePage: number
  /** 下一页页码 */
  nextPage: number
  /** 是否第一页 */
  isFirstPage: boolean
  /** 是否最后一页 */
  isLastPage: boolean
  /** 是否存在上一页 */
  hasPreviousPage: boolean
  /** 是否存在下一页 */
  hasNextPage: boolean
  /** 导航页数 */
  navigatePages: number
  /** 导航页码列表 */
  navigatepageNums: number[]
  /** 导航首页 */
  navigateFirstPage: number
  /** 导航末页 */
  navigateLastPage: number
}

/**
 * 推送用户明细列表项
 * 后端暂未提供完整字段，保留常用展示字段与扩展字段，便于后续直接对接。
 */
export interface PushMessageUserInfoItem {
  /** 明细 ID */
  id?: string
  /** 用户姓名 */
  userName?: string | null
  /** 用户名称 */
  name?: string | null
  /** 手机号 */
  phone?: string | null
  /** 手机号 */
  mobile?: string | null
  /** 推送时间 */
  pushTime?: string | null
  /** 推送状态名称 */
  status?: string | null
  /** 推送状态编码 */
  statusCode?: PushUserStatusValue | null
  [key: string]: unknown
}

/**
 * 推送用户明细分页结果
 */
export interface PushMessageUserInfoPageResult {
  /** 总条数 */
  total?: number
  /** 当前页数据 */
  list?: PushMessageUserInfoItem[]
  /** 当前页码 */
  pageNum?: number
  /** 每页条数 */
  pageSize?: number
}

/**
 * 推送任务详情
 */
export interface PushTaskDetailResult extends PushTaskListItem {}

/**
 * 推送任务详情参数
 */
export interface PushTaskDetailParams {
  /** 任务 ID */
  id: string
}

/**
 * 删除推送任务参数
 */
export interface PushTaskDeleteParams {
  /** 任务 ID */
  id: string
}

/**
 * 推送消息上传结果
 */
export interface PushMessageUploadResult {
  /** 文件对象 key */
  key: string
  /** 文件原始名称 */
  name: string | null
  /** 文件访问地址 */
  url: string | null
}

/**
 * 推送用户数据校验参数
 */
export interface PushMessageCheckUploadParams {
  /** 上传接口返回的文件名 */
  fileName: string
}

/**
 * 推送用户数据校验结果
 */
export interface PushMessageCheckUploadResult {
  /** 校验提示信息 */
  message: string
  /** 批次 ID */
  batchId: string
  /** 校验成功条数 */
  success: string
  /** 数据总数 */
  total: string
}

/**
 * 保存上传推送消息参数
 */
export interface SaveUploadPushMessageParams {
  /** 任务 ID，新建时传空字符串 */
  id: string
  /** 上传接口返回的文件名 */
  fileName: string
  /** 上传批次 ID */
  batchId: string
  /** 推送类型：1 立即推送，2 定时推送 */
  pushType: '1' | '2'
  /** 定时推送开始时间 */
  pushTime?: string
}
