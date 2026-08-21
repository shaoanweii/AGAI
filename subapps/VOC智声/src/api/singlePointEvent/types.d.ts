/**
 * @description: 列表查询入参
 * @return {*}
 */
export interface SingleEventQueryModel {
  /** 每页条数（默认可选，整数类型） */
  pageSize?: number

  /** 页码（默认可选，整数类型） */
  pageNum?: number

  /** 排序字段（默认可选，字符串类型） */
  order?: string

  /** 开始时间（必填，字符串类型） */
  startTime: string

  /** 结束时间（必填，字符串类型） */
  endTime: string

  /** 品牌编码（默认可选，字符串数组类型） */
  brandCodes?: string[]

  /** 车系编码（默认可选，字符串数组类型） */
  carSeriesCodes?: string[]

  /** 渠道编码（默认可选，字符串数组类型） */
  channelCodes?: string[]

  /** 主题分类（默认可选，字符串数组类型） */
  subjectCategoryIds?: string[]

  /** 事件状态（默认可选，字符串数组类型） */
  taskStatuses?: string[]

  /** 事件优先级（默认可选，字符串数组类型） */
  eventPriorities?: string[]

  /** 发声用户名称（默认可选，字符串类型） */
  authorName?: string

  /** 主责部门（默认可选，字符串类型） */
  mainRespOrgId?: string[] | string

  /** 是否处理（默认可选，字符串类型） */
  isProcessed?: string

  /** 处理人（默认可选，字符串类型） */
  handler?: string

  /** 客户姓名（默认可选，字符串类型） */
  custName?: string

  /** 客户手机号（默认可选，字符串类型） */
  custPhone?: string

  /** 私信次数（默认可选，整数类型） */
  privateMsgCount?: number

  /** 私信进度（默认可选，字符串类型） */
  privateMsgProgressCode?: string

  /** 回评进度（默认可选，字符串类型） */
  reviewProgressCode?: string

  /** 关联工单号（默认可选，字符串类型） */
  relationWorkNo?: string

  /** 事件信息（默认可选，字符串类型） */
  eventName?: string

  /** 事件编号（默认可选，字符串类型） */
  warningEventNo?: string

  /** 事件等级（默认可选，字符串类型） */
  eventLevel?: string[] | string

  /** 敏感类型（默认可选，字符串类型） */
  sensitiveType?: string

  /** 事件清晰度（默认可选，字符串类型） */
  eventClarity?: string

  /** 事件有效性（默认可选，字符串数组类型） */
  eventValidityList?: string[]

  /** 标题/原始声音关键字（默认可选，字符串类型） */
  titleContentKey?: string

  /** 事件属性（默认可选，字符串数组类型） */
  eventAttributeList?: string[]

  /** 一级标签（默认可选，字符串数组类型） */
  firstCodeTag?: string[]

  /** 二级标签（默认可选，字符串数组类型） */
  secondCodeTag?: string[]

  /** 三级标签（默认可选，字符串数组类型） */
  threeCodeTag?: string[]

  /** 四级标签（默认可选，字符串数组类型） */
  fourCodeTag?: string[]

  /** 标准观点（默认可选，字符串数组类型） */
  topicList?: string[]

  /** 是否仅自己，用于区分全部事件/我的事件（全部：0 我的事件：1） */
  isMine?: string
}

/**
 * @description: 分页返回值中的list
 * @return {*}
 */
export interface SingleEventPageVo {
  /** 预警ID（字符串类型） */
  id?: string

  /** 原声ID（字符串类型） */
  dataId?: string

  /** 声音ID（字符串类型） */
  soundsId?: string

  /** 事件编号（字符串类型） */
  warningEventNo?: string

  /** 主题分类（字符串类型） */
  subjectCategoryName?: string

  /** 数据来源（字符串类型） */
  channelName?: string

  /** 事件优先级（字符串类型） */
  eventPriorityName?: string

  /** 事件优先级编码（字符串类型） */
  eventPriority?: string

  /** 标题（字符串类型） */
  title?: string

  /** 原始声音（字符串类型） */
  content?: string

  /** 声音片段（字符串类型） */
  originalTextScene?: string

  /** 标准观点（字符串类型） */
  topicText?: string

  /** 品牌名称（字符串类型） */
  brandName?: string

  /** 系列名称（字符串类型） */
  carSeriesName?: string

  /** 意图（字符串类型） */
  intention?: string

  /** 情感（字符串类型） */
  sentiment?: string

  /** 发声用户（字符串类型） */
  authorName?: string

  /** 观点（字符串类型） */
  topic?: string

  /** 主责部门（字符串类型） */
  mainRespOrgName?: string

  /** 是否处理（字符串类型） */
  isProcessed?: string

  /** 回评进度（字符串类型） */
  reviewProgressName?: string

  /** 回评处理人员（字符串类型） */
  reviewHandlerName?: string

  /** 私信进度/状态（字符串类型） */
  privateMsgProgressName?: string

  /** 私信次数（整数类型） */
  privateMsgCount?: number

  /** 私信处理人员（字符串类型） */
  privateMsgHandlerName?: string

  /** 更新时间（字符串类型，格式：date-time） */
  updateTime?: string

  /** 预警时间（字符串类型） */
  warningTime?: string

  /** 事件属性（字符串类型） */
  eventAttribute?: string

  /** 事件有效性名称（字符串类型） */
  eventValidityName?: string

  /** 任务状态（字符串类型） */
  taskStatus?: string

  /** 任务状态名称（字符串类型） */
  taskStatusName?: string

  /** 开始时间（字符串类型，格式：date-time） */
  eventProcessStartTime?: string | undefined

  /** 结束时间（字符串类型，格式：date-time） */
  eventProcessEndTime?: string | undefined
}

/**
 * @description: 分页返回值
 * @return {*}
 */
export interface PageInfoSingleEventPageVo {
  /** 总记录数（长整数类型，对应 integer(int64)） */
  total?: number

  /** 分页数据列表（SingleEventPageVo 类型数组，需确保已定义 SingleEventPageVo 接口） */
  list?: SingleEventPageVo[]

  /** 当前页码（整数类型，对应 integer(int32)） */
  pageNum?: number

  /** 每页条数（整数类型，对应 integer(int32)） */
  pageSize?: number

  /** 当前页实际记录数（整数类型，对应 integer(int32)） */
  size?: number

  /** 当前页第一条记录的行号（长整数类型，对应 integer(int64)） */
  startRow?: number

  /** 当前页最后一条记录的行号（长整数类型，对应 integer(int64)） */
  endRow?: number

  /** 总页数（整数类型，对应 integer(int32)） */
  pages?: number

  /** 上一页页码（整数类型，对应 integer(int32)，无则可能为 0 或 undefined） */
  prePage?: number

  /** 下一页页码（整数类型，对应 integer(int32)，无则可能为 0 或 undefined） */
  nextPage?: number

  /** 是否为第一页（布尔类型） */
  isFirstPage?: boolean

  /** 是否为最后一页（布尔类型） */
  isLastPage?: boolean

  /** 是否有上一页（布尔类型） */
  hasPreviousPage?: boolean

  /** 是否有下一页（布尔类型） */
  hasNextPage?: boolean

  /** 导航页码数（整数类型，对应 integer(int32)，即分页控件显示的页码数量） */
  navigatePages?: number

  /** 导航页码数组（整数类型数组，对应 integer(int32) 数组，存储分页控件显示的所有页码） */
  navigatepageNums?: number[]

  /** 导航第一页页码（整数类型，对应 integer(int32)） */
  navigateFirstPage?: number

  /** 导航最后一页页码（整数类型，对应 integer(int32)） */
  navigateLastPage?: number
}

/**
 * @description: 处理人列表
 * @return {*}
 */
export interface SingleEventCCUserModel {
  /** 抄送人部门ID（必填，字符串类型） */
  departmentId?: string

  /** 抄送人部门编号（必填，字符串类型） */
  departmentNo?: string

  /** 抄送人部门名称（必填，字符串类型） */
  departmentName?: string

  /** 抄送人ID（可选，字符串类型） */
  userId?: string

  /** 抄送人工号（可选，字符串类型） */
  userNo?: string

  /** 抄送人名称（可选，字符串类型） */
  userName?: string

  /** 是否全部（可选，布尔类型，通常用于标识是否抄送部门所有成员） */
  all?: boolean
}
/**
 * @description:
 * @return {*}
 */
export interface SingleEventBatchCloseModel {
  pageQueryModel?: SingleEventPageVo
  /** 事件id列表（可选，字符串数组类型） */
  ids?: string[]

  /** 关闭原因（必填，字符串类型，可选值：无需处理、打标错误等） */
  closeReason?: string

  /** 品牌编码（可选，字符串类型） */
  brandCode?: string

  /** 品牌名称（可选，字符串类型） */
  brandName?: string

  /** 车系编码（可选，字符串类型） */
  carSeriesCode?: string

  /** 车系名称（可选，字符串类型） */
  carSeriesName?: string

  /** 情感（可选，字符串类型） */
  sentiment?: string

  /** 意图（可选，字符串类型） */
  intention?: string

  /** 观点编码（可选，字符串类型） */
  topicCode?: string

  /** 观点名称（可选，字符串类型） */
  topicName?: string

  /** 说明（可选，字符串类型，补充关闭原因的详细说明） */
  description?: string

  /** 全选标识1（可选，布尔类型，需结合实际接口确认含义，如：是否选中全部符合条件的事件） */
  all?: boolean
}

export interface SingleEventBatchConfirmModel {
  pageQueryModel?: SingleEventPageVo
  /** 事件id列表（可选，字符串数组类型） */
  ids?: string[]

  /** 	确认状态（pass、reject） */
  confirmState: string
  /** 	开始时间（确认通过选填） */
  eventProcessStartTime: string
  /** 	结束时间（确认通过选填） */
  eventProcessEndTime: string
  /** 	处理人列表 */
  handlers: SingleEventCCUserModel[]
  /** 	说明（审核通过选填） */
  description?: string
  closedModel?: SingleEventBatchCloseModel
  all?: boolean
}

export interface SingleEventBatchAssignModel {
  pageQueryModel?: SingleEventPageVo
  /** 事件id列表（可选，字符串数组类型） */
  ids?: string[]
  /** 	开始时间（确认通过选填） */
  eventProcessStartTime?: string
  /** 	结束时间（确认通过选填） */
  eventProcessEndTime?: string
  /** 	说明（审核通过选填） */
  description?: string
  all?: boolean
}

export interface SingleEventBatchApproveModel {
  pageQueryModel?: SingleEventPageVo
  /** 事件id列表（可选，字符串数组类型） */
  ids?: string[]
  /** 	开始时间（确认通过选填） */
  eventProcessStartTime?: string
  /** 	结束时间（确认通过选填） */
  eventProcessEndTime?: string
  /** 	说明（审核通过选填） */
  description?: string
  all?: boolean
  /** 	审核状态（pass、reject） */
  approveState?: string
  // 主责部门ID（审核通过必填）
  mainRespOrgId?: string
  // 主责部门编号（审核通过必填）
  mainRespOrgNo?: string
  // 主责部门名称（审核通过必填）
  mainRespOrgName?: string
  // 主责人ID（审核通过必填）
  mainRespUserId?: string
  // 主责人工号（审核通过必填）
  mainRespUserEmpNo?: string
  // 主责人名称（审核通过必填）
  mainRespUserName?: string
  // 抄送人列表（审核通过选填 如果没有抄送人，传null，如果抄送人不是当前组织所有人，用户相关信息必填）
  ccUsers?: SingleEventCCUserModel[]
  closedModel?: SingleEventBatchCloseModel
}

export interface SingleEventBatchConfirmRejectModel {
  // 是否对所有事件操作，页面选择了全选传true(并且将所有的分页条件传过来)，否则为false
  isAll?: boolean
  pageQueryModel?: SingleEventPageVo
  // 事件id列表
  ids?: string[]
  // 关闭原因（无需处理、打标错误）
  rejectReason?: string
  // 说明
  description?: string
}

// 用户意图
export interface SingleEventIntentionVo {
  /**
   * 事件ID（必填）
   */
  id: string

  /**
   * 用户意图（可选）
   */
  intentionType?: string

  /**
   * 全领域1级Code（可选）
   */
  domTagFirstCode?: string

  /**
   * 全领域1级（可选）
   */
  domTagFirst?: string

  /**
   * 全领域2级Code（可选）
   */
  domTagSecondCode?: string

  /**
   * 全领域2级（可选）
   */
  domTagSecond?: string

  /**
   * 全领域3级Code（可选）
   */
  domTagThreeCode?: string

  /**
   * 全领域3级（可选）
   */
  domTagThree?: string

  /**
   * 全领域4级Code（可选）
   */
  domTagFourCode?: string

  /**
   * 全领域4级（可选）
   */
  domTagFour?: string

  /**
   * 观点（可选）
   */
  topic?: string

  /**
   * 标准观点选项列表（前端使用，可选）
   */
  topicOptions?: any[]
  /**
   * 是否显示数据纠错按钮
   */
  correctButton?: boolean
}

// 单点事件详情基本信息返回对象
export interface SingleEventDetailBaseUpdateModel {
  /**
   * 原声ID（必填）
   */
  dataId: string

  /**
   * 车系编码（可选）
   */
  carSeriesCode?: string

  /**
   * 车系名称（可选）
   */
  carSeriesName?: string

  /**
   * 车型（可选）
   */
  carModel?: string

  /**
   * 发动机号（可选）
   */
  engineNo?: string

  /**
   * 车牌号（可选）
   */
  licensePlateNo?: string

  /**
   * 车架号（可选）
   */
  vinNo?: string

  /**
   * 购车时间（可选，格式：date-time，例如：2025-11-19T12:00:00）
   */
  carPurchaseTime?: string

  /**
   * 经销商名称（可选）
   */
  dealerName?: string
  // 用户意图
  intentions?: SingleEventIntentionVo[]
}

export interface SingleEventQueryDetailModel {
  /**
   * 原声ID（必填）
   */
  dataId: string

  /**
   * 事件ID（可选）
   */
  id?: string

  /**
   * 组织ID（可选）
   */
  orgId?: string

  /**
   * 用户ID（可选）
   */
  userId?: string
}

export interface SingleEventDetailBaseVo {
  /**
   * 原声ID
   */
  dataId?: string

  /**
   * 评论用户名称
   */
  commentUserName?: string

  /**
   * 评论用户ID
   */
  commentUserId?: string

  /**
   * 评论时间（格式：date-time，例如：2025-11-19T12:00:00）
   */
  commentTime?: string

  /**
   * 评论详情
   */
  commentDetails?: string

  /**
   * 是否主帖（0=否，1=是）
   */
  isMainPost?: string

  /**
   * 发帖用户ID
   */
  postUserId?: string

  /**
   * 发帖用户名称
   */
  postUserName?: string

  /**
   * 发帖时间
   */
  postTime?: string

  /**
   * 主帖链接
   */
  mainPostUrl?: string

  /**
   * 主帖标题
   */
  mainPostTitle?: string

  /**
   * 主帖详情
   */
  mainPostDetails?: string

  /**
   * 品牌编码
   */
  brandCode?: string

  /**
   * 品牌名称
   */
  brandName?: string

  /**
   * 车系编码
   */
  carSeriesCode?: string

  /**
   * 车系名称
   */
  carSeriesName?: string

  /**
   * 车型
   */
  carModel?: string

  /**
   * 发动机号
   */
  engineNo?: string

  /**
   * 车牌号
   */
  licensePlateNo?: string

  /**
   * 车架号
   */
  vinNo?: string

  /**
   * 购车时间（格式：date-time，例如：2025-11-19T12:00:00）
   */
  carPurchaseTime?: string

  /**
   * 经销商名称
   */
  dealerName?: string

  /**
   * 渠道名称
   */
  channelName?: string

  /**
   * 原声类型
   */
  contentTypeName?: string

  /**
   * 事件ID（已经排好序的）
   */
  eventIds?: string[]

  /**
   * 用户意图
   */
  intentions?: SingleEventIntentionVo[]

  /**
   * 基础信息是否可编辑
   */
  editPermission?: boolean
}

export interface SingleEventPrivateMsgModel {
  /** 私信时间（私信发送/处理的时间，格式建议遵循 ISO8601 标准，如：2024-05-22T09:45:00Z） */
  privateMsgTime?: string
  /** 私信用户ID（执行私信操作的用户ID） */
  userId?: string
  /** 私信用户工号（执行私信操作的用户工号） */
  userEmpNo?: string
  /** 私信用户名称（执行私信操作的用户姓名） */
  userName?: string
  /** 私信用户组织ID（执行私信操作的用户所属组织ID） */
  orgId?: string
  /** 私信用户组织编号（执行私信操作的用户所属组织编号） */
  orgNo?: string
  /** 私信用户组织名称（执行私信操作的用户所属组织名称） */
  orgName?: string
}

export interface SingleEventWorkOrderModel {
  /** 工单编号（唯一标识工单的编号） */
  workOrderNo?: string
  /** 工单主题（工单的核心主题/标题描述） */
  topic?: string
  /** 工单类型（工单对应的分类编码或描述，如：投诉工单/咨询工单/处理工单等） */
  type?: string
  /** 工单责任人ID（负责处理该工单的人员ID） */
  respUserId?: string
  /** 工单责任人名称（负责处理该工单的人员姓名） */
  respUserName?: string
  /** 工单创建时间（工单生成的时间，格式建议遵循 ISO8601 标准，如：2024-05-22T11:30:00Z） */
  createTime?: string
  /** 工单状态（工单当前的处理状态，如：待分配/处理中/已完成/已关闭等） */
  status?: string
}

export interface TaskEventLogContentModel {
  /** 内容类型（内容的分类标识，如：文本/图片/音频/视频/链接等，可关联对应数据字典） */
  contentType?: string
  /** 内容（具体的内容数据，根据 contentType 对应不同格式，如文本内容直接存储、媒体资源存储URL等） */
  content?: string
}

export interface TaskEventLogModel {
  /** 主键ID（操作记录的唯一标识ID） */
  id?: string
  /** 创建时间（操作记录生成的时间，格式遵循 ISO8601 标准，如：2024-05-23T09:20:00Z） */
  operateTime?: string
  /** 事件ID（关联的预警/业务事件唯一标识ID，与核心事件表的id关联） */
  eventId?: string
  /** 声音ID（关联的声音资源唯一标识ID） */
  soundsId?: string
  /** 原声ID（关联的原始声音资源标识ID，与核心事件表的dataId一致） */
  dataId?: string
  /** 组织ID（执行操作的组织唯一标识ID） */
  operateOrgId?: string
  /** 组织编号（执行操作的组织编号） */
  operateOrgNo?: string
  /** 组织名称（执行操作的组织完整名称） */
  operateOrgName?: string
  /** 操作人ID（执行该操作的人员唯一标识ID） */
  operateUserId?: string
  /** 操作人工号（执行该操作的人员工号） */
  operateUserEmpNo?: string
  /** 操作人名称（执行该操作的人员姓名） */
  operateUserName?: string
  /** 操作类型（操作行为的分类编码或描述，如：创建/编辑/审核/关闭等，可关联对应数据字典） */
  operateType?: string
  /**操作内容 */
  content?: TaskEventLogContentModel[]
}

// 事件详情
export interface SingleEventDetailVo {
  /** 事件ID */
  id?: string
  /** 原声ID（原始事件关联ID） */
  dataId?: string
  /** 预警事件编号（唯一标识预警事件的编号） */
  warningEventNo?: string
  /** 事件优先级（优先级编码，如：P0/P1/P2 等） */
  eventPriority?: string
  /** 事件优先级名称（优先级中文描述，如：紧急/高/中/低） */
  eventPriorityName?: string
  /** 事件等级（等级编码，如：L1/L2/L3 等） */
  eventLevel?: string
  /** 事件等级名称（等级中文描述，如：严重/一般/轻微） */
  eventLevelName?: string
  /** 敏感类型（敏感内容分类，如：政治敏感/色情/暴力等） */
  sensitiveType?: string
  /** 事件清晰度（事件信息明确程度，如：清晰/模糊/待核实） */
  eventClarity?: string
  /** 事件信息（事件具体描述内容） */
  eventName?: string
  /** 主题分类名称（事件所属主题分类，如：内容安全/数据安全/运营风险等） */
  subjectCategoryName?: string
  /** 主责组织ID（负责处理该事件的组织ID） */
  mainRespOrgId?: string
  /** 主责组织编号（负责处理该事件的组织编号） */
  mainRespOrgNo?: string
  /** 主责组织名称（负责处理该事件的组织名称） */
  mainRespOrgName?: string
  /** 主责人ID（负责处理该事件的人员ID） */
  mainRespUserId?: string
  /** 主责人工号（负责处理该事件的人员工号） */
  mainRespUserEmpNo?: string
  /** 主责人名称（负责处理该事件的人员姓名） */
  mainRespUserName?: string
  /** 预警时间（事件触发预警的时间，格式：ISO8601 日期时间字符串，如：2024-05-20T14:30:00Z） */
  warningTime?: string
  /** 是否需要回复（标识是否需要对该预警进行回复，如：Y/N/是/否） */
  isNeedReply?: string
  /** 是否需要闭环（标识是否需要对该预警进行闭环处理，如：Y/N/是/否） */
  isNeedClosedLoop?: string
  /** 抄送人 */
  ccUsers?: SingleEventUserModel[]
  /** 处理人 */
  handleUsers?: SingleEventUserModel[] | SingleEventUserModel
  handleUser?: SingleEventUserModel
  /** 事件处理开始时间（格式：ISO8601 日期时间字符串，如：2024-05-20T15:00:00Z） */
  eventProcessStartTime?: string
  /** 事件处理结束时间（格式：ISO8601 日期时间字符串，如：2024-05-20T16:30:00Z） */
  eventProcessEndTime?: string
  /** 是否处理/处理方式（关联数据字典：task_event_is_handled，存储字典编码值） */
  isProcessed?: string
  /**
   * 处理原因（关联数据字典：
   * 当 isProcessed 为"是"时，关联 task_event_is_handled 字典；
   * 当 isProcessed 为"否"时，关联 task_event_close_reason 字典，存储对应字典编码值）
   */
  unprocessedReason?: string
  /** 处理描述（事件处理过程的详细说明、操作记录等文本信息） */
  processDescription?: string
  /** 回评进度编码（回评流程对应的编码标识） */
  reviewProgressCode?: string
  /** 回评进度名称（回评进度的中文描述，如：待回评/回评中/已完成回评） */
  reviewProgressName?: string
  /** 回评时间（回评操作执行的时间，格式：ISO8601 日期时间字符串，如：2024-05-21T10:15:00Z） */
  reviewDate?: string
  /** 回评人员 */
  reviewHandler?: SingleEventUserModel[] | SingleEventUserModel
  /** 回评内容（对事件处理结果的回评详细文本信息） */
  reviewContent?: string
  /** 私信进度编码（私信发送/处理流程对应的编码标识） */
  privateMsgProgressCode?: string
  /** 私信进度名称（私信进度的中文描述，如：待发送/已发送/发送失败/已读） */
  privateMsgProgressName?: string
  /** 私信次数（关联数据字典：task_event_private_mst_count，存储字典编码值，标识私信发送的次数） */
  privateMsgCount?: string
  /** 私信渠道（关联 conditions 中的 dataChannel 字段，存储渠道编码，标识私信发送的渠道） */
  privateMsgChannel?: string
  /** 私信渠道名称（私信发送渠道的中文描述，如：APP内私信/短信/微信公众号） */
  privateMsgChannelName?: string
  /** 客户姓名（涉及事件的客户姓名信息） */
  custName?: string
  /** 客户电话（涉及事件的客户联系电话，可能为手机号等格式） */
  custMobile?: string
  /** 任务状态（当前事件对应的任务状态编码或描述，如：待处理/处理中/已完成/已终止） */
  taskStatus?: string
  /** 	私信详情 */
  privateMsgDetails?: SingleEventPrivateMsgModel[]
  /** 	关联工单 */
  relatedWorkOrderNos?: SingleEventWorkOrderModel[]
  /** 	操作记录 */
  operateLogs?: TaskEventLogModel[]
  /** 	操作权限 */
  permissions?: string[]
  /** 	事件描述 */
  description?: string
  /** 原文 */
  mainPostDetails?: string
  /** 品牌名称 */
  brandName?: string
  /** 事件有效性 */
  eventValidity?: string
  /**  展示类型（1：通过组织、字典渲染  2：直接显示名称 */
  showType?: number
  /**  声音片段内容 */
  originalTextScene?: string
  /**  观点 （ conditions 接口中的 topicList ） */
  topic?: string
  /**  用户意图（数据字典 voc_intention ） */
  intentionType?: string
  // 全领域标签
  domTagFirst?: string
  domTagFirstCode?: string
  domTagSecond?: string
  domTagSecondCode?: string
  domTagThree?: string
  domTagThreeCode?: string
  domTagFour?: string
  domTagFourCode?: string
}

export interface SingleEventApproveModel {
  /** 事件ID（事件的唯一标识，非空） */
  id?: string
  /** 是否系统自动审核（标识事件是否由系统自动完成审核，非空布尔值） */
  isSystem?: boolean
  /** 主责部门ID（负责处理该事件的部门唯一标识，非空） */
  mainRespOrgId?: string
  /** 主责部门编号（负责处理该事件的部门编号，非空） */
  mainRespOrgNo?: string
  /** 主责部门名称（负责处理该事件的部门完整名称，非空） */
  mainRespOrgName?: string
  /** 主责人ID（负责处理该事件的人员唯一标识，非空） */
  mainRespUserId?: string
  /** 主责人工号（负责处理该事件的人员工号，非空） */
  mainRespUserEmpNo?: string
  /** 主责人名称（负责处理该事件的人员姓名，非空） */
  mainRespUserName?: string
  /** 抄送人 */
  ccUsers?: SingleEventUserModel[]
  /**
   * 开始时间（事件处理开始时间，选填项）
   * 格式：ISO8601 日期时间字符串，如：2024-05-25T10:00:00Z
   */
  eventProcessStartTime?: string
  /**
   * 结束时间（事件处理结束时间，选填项）
   * 格式：ISO8601 日期时间字符串，如：2024-05-25T11:30:00Z
   */
  eventProcessEndTime?: string
  /** 说明（事件处理相关的补充说明，选填项） */
  description?: string
  /**
   * 关闭原因（可选值：无需处理、打标错误等，前端无需传递该字段，由后端生成或维护）
   * 注：字段标注为可选，因前端不传，后端返回时可能存在也可能不存在
   */
  rejectReason?: string
}

export interface SingleEventAssignModel {
  /**
   * 事件ID（唯一标识事件的核心ID，必填项，不可为空）
   */
  id: string
  /**
   * 处理周期开始时间（事件处理流程的起始时间，选填项）
   * 格式：ISO8601 日期时间字符串，如：2024-05-26T09:00:00Z
   */
  eventProcessStartTime?: string
  /**
   * 处理周期结束时间（事件处理流程的终止时间，选填项）
   * 格式：ISO8601 日期时间字符串，如：2024-05-26T17:00:00Z
   */
  eventProcessEndTime?: string
  /**
   * 说明
   */
  description?: string
  /**
   * 处理人列表
   */
  handlers?: SingleEventUserModel[]
}

export interface SingleEventCloseModel {
  /**
   * 事件ID（关联的事件唯一标识，选填项）
   */
  id?: string
  /**
   * 关闭原因（事件关闭的具体原因，必填项，不可为空）
   * 可选值示例：无需处理、打标错误（具体以业务数据字典为准）
   */
  closeReason: string
  /**
   * 说明（事件关闭相关的补充说明、备注信息，选填项）
   */
  description?: string
}

export interface SingleEventConfirmModel {
  /**
   * 事件ID（唯一标识事件的核心ID，必填项，不可为空）
   */
  id: string
  /**
   * 处理周期开始时间（事件处理流程的起始时间，选填项）
   * 格式：ISO8601 日期时间字符串，如：2024-05-26T09:00:00Z
   */
  eventProcessStartTime?: string
  /**
   * 处理周期结束时间（事件处理流程的终止时间，选填项）
   * 格式：ISO8601 日期时间字符串，如：2024-05-26T17:00:00Z
   */
  eventProcessEndTime?: string
  /**
   * 说明
   */
  description?: string
  /**
   * 是否系统操作
   */
  isSystem?: string
  /**
   * 处理人列表
   */
  handlers?: SingleEventUserModel[]
  /**
   * 抄送人列表（选填 如果没有抄送人，传空数组不要传null，如果抄送人不是当前组织所有人，用户相关信息必填）
   */
  ccUsers?: SingleEventUserModel[]
}

export interface SingleEventHandleModel {
  /**
   * 事件ID
   */
  id?: string
  /**
   * 是否处理
   */
  isProcessed?: boolean
  /**
   * 原因
   */
  unprocessedReason?: string
  /**
   * 处理周期结束时间（事件处理流程的终止时间，选填项）
   * 格式：ISO8601 日期时间字符串，如：2024-05-26T17:00:00Z
   */
  eventProcessEndTime?: string
  /**
   * 处理人
   */
  handlers?: SingleEventUserModel[]
  /**
   * 	处理说明
   */
  processDescription?: string

  /**
   * 	回评进度编码
   */
  reviewProgressCode?: string
  /**
   * 	回评进度名称
   */
  reviewProgressName?: string
  /**
   * 	回评时间
   */
  reviewDate?: string
  /**
   * 	回评处理人
   */
  reviewHandler?: SingleEventUserModel[]
  /**
   * 	回评话术
   */
  reviewContent?: string
  /**
   * 	私信进度编码
   */
  privateMsgProgressCode?: string
  /**
   * 	私信进度名称
   */
  privateMsgProgressName?: string
  /**
   * 	私信次数
   */
  privateMsgCount?: string
  /**
   * 	私信渠道
   */
  privateMsgChannel?: string
  /**
   * 		私信渠道名称
   */
  privateMsgChannelName?: string
  /**
   * 		私信详情（数组，最多 3 个，存储私信时间 / 处理人等 JSON）
   */
  privateMsgDetails?: SingleEventPrivateMsgModel[]
  /**
   * 		关联工单信息（数组，有手机号时关联，JSON 格式）
   */
  relatedWorkOrderNos?: SingleEventWorkOrderModel[]
  /**
   * 		状态-前端不传
   */
  taskStatus?: string
  /**
   * 		更新时间-前端不传
   */
  updateTime?: string
  /**
   * 		更新人ID-前端不传
   */
  updateUserId?: string
}

export interface SingleEventConfirmRejectModel {
  /**
   * 事件ID（关联的事件唯一标识，必填项，不可为空）
   */
  id: string
  /**
   * 关闭原因（事件关闭/驳回的具体原因，必填项，不可为空）
   * 可选值示例：无需处理、打标错误（具体以业务数据字典为准）
   */
  rejectReason: string
  /**
   * 说明（事件关闭/驳回相关的补充说明、备注信息，选填项）
   */
  description?: string
}

/**
 * 五级分类数据类型定义
 * 所有属性均为可选
 */
export interface CategoryInfo {
  /** 一级分类ID */
  firstId?: string
  /** 一级分类编码 */
  firstCode?: string
  /** 一级分类名称 */
  firstName?: string
  /** 二级分类ID */
  secondId?: string
  /** 二级分类编码 */
  secondCode?: string
  /** 二级分类名称 */
  secondName?: string
  /** 三级分类ID */
  thirdId?: string
  /** 三级分类编码 */
  thirdCode?: string
  /** 三级分类名称 */
  thirdName?: string
  /** 四级分类ID */
  fourthId?: string
  /** 四级分类编码 */
  fourthCode?: string
  /** 四级分类名称 */
  fourthName?: string
  /** 五级分类ID */
  fifthId?: string
  /** 五级分类编码 */
  fifthCode?: string
  /** 五级分类情感倾向 */
  fifthEmotion?: string | null
  /** 类型字段 */
  type?: any | null
}
