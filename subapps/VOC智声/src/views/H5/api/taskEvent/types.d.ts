/**
 * 单点事件列表查询入参（移动端）
 * - 以移动端接口实际需要为准，当前仅对关键字段做类型约束
 */
export interface H5VocTaskBaseRequest {
  pageNum?: number
  pageSize?: number

  /** 开始时间（示例：2025-12-01 00:00:00） */
  startTime?: string
  /** 结束时间（示例：2025-12-15 23:59:59） */
  endTime?: string

  /** 品牌编码 */
  brandCode?: string

  /** 事件状态（后端通常为编码数组） */
  taskStatuses?: string[]

  /** 透传其他筛选条件 */
  [key: string]: any
}



/**
 * 新增事件统计接口返回数据
 */
export interface NewlyEventStatisticsVo {
  /** 当前周期新增事件数量 */
  currentCounts?: number
  /** 上个周期新增事件数量 */
  lastCounts?: number
  /** 闭环率 */
  closeRate?: number
  /** 环比 */
  ringRate?: number
}

/**
 * 事件状态分布接口返回单项
 */
export interface EventStatusDistributionVo {
  /** 事件状态名称（如：预警初审、业务响应等） */
  taskStatusName?: string
  /** 事件状态编码（10/11/20/30/40/90） */
  taskStatus?: string | number
  /** 当前周期事件数量 */
  currentCounts?: number
  /** 占比（后端字段，单位可能为百分比或比例） */
  percent?: number
}

/**
 * 事件趋势接口返回单项
 */
export interface EventTrendVo {
  /** 事件日期（YYYY-MM-DD） */
  dateStr?: string
  /** 事件数量 */
  counts?: number
}

/**
 * 单点事件列表 - 处理人信息
 */
export interface SingleEventUserModel {
  /** 部门ID */
  orgId?: string
  /** 部门编号 */
  orgNo?: string
  /** 部门名称 */
  orgName?: string
  /** 是否为所选部门所有用户 */
  allFlag?: boolean
  /** 用户ID */
  userId?: string
  /** 用户工号 */
  userEmpNo?: string
  /** 用户名称 */
  userName?: string
}

/**
 * 单点事件列表项（移动端）
 */
export interface MobileSingleEventListItem {
  /** 预警ID/事件ID */
  id?: string
  /** 原声ID */
  dataId?: string
  /** 声音ID */
  soundsId?: string

  /** 事件信息/标题 */
  eventName?: string
  /** 事件编号 */
  warningEventNo?: string
  /** 主题分类 */
  subjectCategoryName?: string

  /** 事件优先级 */
  eventPriority?: string
  /** 事件优先级名称 */
  eventPriorityName?: string

  /** 事件等级 */
  eventLevel?: string
  /** 事件等级名称 */
  eventLevelName?: string

  /** 品牌编码 */
  brandCode?: string
  /** 品牌名称 */
  brandName?: string
  /** 系列名称 */
  carSeriesName?: string

  /** 意图 */
  intention?: string
  /** 全领域级（一级-四级） */
  domTagFirst?: string
  domTagSecond?: string
  domTagThree?: string
  domTagFour?: string

  /** 情感 */
  sentiment?: string
  /** 发声用户 */
  authorName?: string
  /** 观点（标准观点） */
  topic?: string

  /** 主责部门ID */
  mainRespOrgId?: string
  /** 主责部门名称 */
  mainRespOrgName?: string

  /** 处理人员信息 */
  handlerSingleEventUserModel?: SingleEventUserModel

  /** 预警时间 */
  warningTime?: string

  /** 任务状态 */
  taskStatus?: string
  /** 任务状态名称 */
  taskStatusName?: string

  /** 事件有效性 */
  eventValidity?: string
  /** 事件有效性名称 */
  eventValidityName?: string
}

/**
 * 单点事件关联事件项（移动端）
 * POST /mobileTerminal/single-event/get-relation-events
 */
export interface MobileSingleEventRelationEventItem {
  /** 事件ID */
  id?: string
  /** 原声ID */
  dataId?: string
  /** 预警事件编号 */
  warningEventNo?: string
  /** 任务状态 */
  taskStatus?: string
  /** 任务状态名称 */
  taskStatusName?: string
  /** 主责组织名称 */
  mainRespOrgName?: string
}

/**
 * 单点事件列表分页结构（移动端）
 */
export interface MobileSingleEventListPageResult {
  total?: number
  list?: MobileSingleEventListItem[]
  pageNum?: number
  pageSize?: number
}

/**
 * 单点事件详情查询入参（移动端）
 * POST /mobileTerminal/single-event/get-detail-event
 * - 参考 PC 侧接口约定：dataId 为必填，id 可选
 */
export interface MobileSingleEventDetailQuery {
  /** 原声ID（必填） */
  dataId: string
  /** 事件ID（可选） */
  id?: string

  /** 透传其他条件（如后端扩展） */
  [key: string]: any
}

/**
 * 单点事件详情 - 私信详情
 */
export interface SingleEventPrivateMsgModel {
  /** 私信时间 */
  privateMsgTime?: string
  /** 私信用户ID */
  userId?: string
  /** 私信用户工号 */
  userEmpNo?: string
  /** 私信用户名称 */
  userName?: string
  /** 私信用户组织ID */
  orgId?: string
  /** 私信用户组织编号 */
  orgNo?: string
  /** 私信用户组织名称 */
  orgName?: string
}

/**
 * 单点事件详情 - 关联工单
 */
export interface SingleEventWorkOrderModel {
  /** 工单编号 */
  workOrderNo?: string
  /** 工单主题 */
  topic?: string
  /** 工单类型 */
  type?: string
  /** 工单责任人ID */
  respUserId?: string
  /** 工单责任人名称 */
  respUserName?: string
  /** 工单创建时间 */
  createTime?: string
  /** 工单状态 */
  status?: string
}

/**
 * 操作记录内容项
 */
export interface TaskEventLogContentModel {
  /** 内容类型 */
  contentType?: string
  /** 内容 */
  content?: string
}

/**
 * 操作记录项
 */
export interface TaskEventLogModel {
  /** 主键 */
  id?: string
  /** 创建时间 */
  operateTime?: string
  /** 事件ID */
  eventId?: string
  /** 声音ID */
  soundsId?: string
  /** 原声ID */
  dataId?: string

  /** 组织信息 */
  operateOrgId?: string
  operateOrgNo?: string
  operateOrgName?: string

  /** 操作人信息 */
  operateUserId?: string
  operateUserEmpNo?: string
  operateUserName?: string

  /** 操作类型 */
  operateType?: string

  /** 操作内容 */
  content?: TaskEventLogContentModel[]
}

/**
 * 单点事件详情返回对象（移动端）
 * - 字段较多，先按后端返回结构完整定义，页面组件取值后续逐步对齐
 */
export interface MobileSingleEventDetailVo {
  /** 事件ID */
  id?: string
  /** 原声ID */
  dataId?: string
  /** 预警事件编号 */
  warningEventNo?: string

  /** 事件优先级 */
  eventPriority?: string
  eventPriorityName?: string

  /** 事件等级 */
  eventLevel?: string
  eventLevelName?: string

  /** 敏感类型 */
  sensitiveType?: string
  /** 事件清晰度 */
  eventClarity?: string

  /** 事件信息/标题 */
  eventName?: string
  /** 主题分类名称 */
  subjectCategoryName?: string

  /** 主责组织 */
  mainRespOrgId?: string
  mainRespOrgNo?: string
  mainRespOrgName?: string

  /** 主责人 */
  mainRespUserId?: string
  mainRespUserEmpNo?: string
  mainRespUserName?: string

  /** 预警时间 */
  warningTime?: string

  /** 是否需要回复/闭环（后端为 string） */
  isNeedReply?: string
  isNeedClosedLoop?: string

  /** 抄送人 */
  ccUsers?: SingleEventUserModel[]

  /** 处理人 */
  handleUser?: SingleEventUserModel

  /** 事件处理起止时间 */
  eventProcessStartTime?: string
  eventProcessEndTime?: string

  /** 是否处理（字典 task_event_is_handled） */
  isProcessed?: string
  isProcessedName?: string

  /** 处理原因（字典：是 task_event_approve_process_mode / 否 task_event_close_reason） */
  unprocessedReason?: string
  unprocessedReasonName?: string

  /** 处理描述 */
  processDescription?: string

  /** 回评进度 */
  reviewProgressCode?: string
  reviewProgressName?: string
  reviewDate?: string
  reviewHandler?: SingleEventUserModel
  reviewModelContent?: string
  reviewContent?: string

  /** 私信进度 */
  privateMsgProgressCode?: string
  privateMsgProgressName?: string
  privateMsgCount?: string
  privateMsgChannel?: string
  privateMsgChannelName?: string

  /** 客户信息 */
  custName?: string
  custMobile?: string

  /** 任务状态 */
  taskStatus?: string
  taskStatusName?: string

  /** 主帖详情 */
  mainPostDetails?: string

  /** 私信详情 */
  privateMsgDetails?: SingleEventPrivateMsgModel[]

  /** 关联工单 */
  relatedWorkOrderNos?: SingleEventWorkOrderModel[]

  /** 操作记录 */
  operateLogs?: TaskEventLogModel[]

  /** 权限列表 */
  permissions?: string[]

  /** 品牌 */
  brandCode?: string
  brandName?: string

  /** 事件有效性 */
  eventValidity?: string
  eventValidityName?: string

  /** 展示类型（1：通过组织、字典渲染；2：直接显示名称） */
  showType?: number

  /** 用户意图（字典 voc_intention） */
  intentionType?: string

  /** 全领域标签 */
  domTagFirstCode?: string
  domTagFirst?: string
  domTagSecondCode?: string
  domTagSecond?: string
  domTagThreeCode?: string
  domTagThree?: string
  domTagFourCode?: string
  domTagFour?: string

  /** 观点（conditions 接口 topicList） */
  topic?: string

  /** 声音片段内容 */
  originalTextScene?: string

  /** 事件处理人（后端下发） */
  eventHandler?: SingleEventUserModel
}

/**
 * 单点事件原声详情-用户意图项
 * POST /mobileTerminal/single-event/get-detail-base
 */
export interface SingleEventIntentionVo {
  /** 事件ID */
  id?: string
  /** 用户意图（数据字典 voc_intention） */
  intentionType?: string

  /** 全领域标签（一级-四级） */
  domTagFirstCode?: string
  domTagFirst?: string
  domTagSecondCode?: string
  domTagSecond?: string
  domTagThreeCode?: string
  domTagThree?: string
  domTagFourCode?: string
  domTagFour?: string

  /** 观点（conditions 接口 topicList） */
  topic?: string

  /** 原始文本场景（后端字段） */
  originalTextScene?: string

  /** 事件ID（已排序） */
  eventIds?: string[]
}

/**
 * 标准观点（topics-batch）返回项
 * POST /mobileTerminal/single-event/topics-batch
 */
export interface SingleEventTopicItem {
  id?: string
  tagName?: string
  tagNameEn?: string | null
  tagCode?: string
}

/**
 * 单点事件原声详情返回对象（移动端）
 * POST /mobileTerminal/single-event/get-detail-base
 */
export interface MobileSingleEventDetailBaseVo {
  /** 原声ID */
  dataId?: string

  /** 评论信息 */
  commentUserName?: string
  commentUserId?: string
  commentTime?: string
  commentDetails?: string

  /** 是否主帖（0=否，1=是） */
  isMainPost?: string

  /** 渠道信息 */
  channelName?: string
  channelCode?: string

  /** 内容类型 */
  contentType?: string
  contentTypeName?: string

  /** 发帖信息 */
  postUserId?: string
  postUserName?: string
  postTime?: string

  /** 主帖信息 */
  mainPostUrl?: string
  mainPostTitle?: string
  mainPostDetails?: string

  /** 车辆信息 */
  brandCode?: string
  brandName?: string
  carSeriesCode?: string
  carSeriesName?: string
  carModel?: string
  engineNo?: string
  licensePlateNo?: string
  vinNo?: string
  carPurchaseTime?: string
  dealerName?: string

  /** 声音片段内容 */
  originalTextScene?: string

  /** 用户意图列表 */
  intentions?: SingleEventIntentionVo[]

  /** 事件ID（已排序） */
  eventIds?: string[]

  /** 编辑权限 */
  editPermission?: boolean
}
