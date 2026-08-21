/**
 * 批量事件查询入参（移动端）
 * POST /mobileTerminal/batch-event/*
 */
export interface BatchEventRuleCategoryVo {
  /** 分类 ID */
  id?: string
  /** 分类名称 */
  name?: string
  /** 父分类 ID，顶级分类为 0 */
  parentId?: string
  /** 分类类型 */
  type?: string
  /** 状态：Enabled/Disabled */
  status?: string
  /** 排序顺序 */
  sortOrder?: number
  /** 操作人 */
  operator?: string
  /** 创建时间 */
  createTime?: string
  /** 更新时间 */
  updateTime?: string
  /** 删除状态：0 正常，1 已删除 */
  delFlag?: number
  /** 子分类列表 */
  children?: BatchEventRuleCategoryVo[]
  /** 规则数量 */
  ruleCount?: number
}

export interface BatchEventQueryModel {
  pageSize?: number
  pageNum?: number
  order?: string

  /** 开始时间 yyyy-MM-dd HH:mm:ss */
  startTime?: string
  /** 结束时间 yyyy-MM-dd HH:mm:ss */
  endTime?: string

  /** 事件名称（模糊查询） */
  eventName?: string
  /** 预警事件编号 */
  warningEventNo?: string
  /** 品牌编码列表 */
  brandCodes?: string[]
  /** 车系编码列表 */
  carSeriesCodes?: string[]
  /** 观点编码 */
  topic?: string
  /** 主题分类 ID 列表 */
  subjectCategoryIds?: string[]
  /** 任务状态列表 */
  taskStatuses?: string[]
  /** 事件优先级列表 */
  eventPriorities?: string[]
  /** 审核人 ID */
  reviewUserId?: string
  /** 主责人 ID */
  mainRespUserId?: string
  /** 事件属性 JSON */
  eventAttribute?: string
  /** 事件有效性列表 */
  eventValidityList?: string[]
  /** 预警周期列表(每日/每周/每月) */
  warningPeriods?: string[]
  /** 业务责任人姓名 */
  reviewUserName?: string
  /** 主责部门 ID 列表 */
  mainRespOrgId?: string[]
  /** 协同部门 ID 列表 */
  collaborateOrgIds?: string[]
  /** 处理人 ID 列表 */
  handlerUserIds?: string[]
  /** 是否驳回列表 */
  isRejectedList?: string[]
  /** 驳回原因列表 */
  rejectReasonList?: string[]
  /** 事件属性列表 */
  eventAttributeList?: string[]
  /** 观点名称列表 */
  topicList?: string[]
  /** 意图列表 */
  intentionList?: string[]
  /** 体验代码 1 级列表 */
  firstCodeTag?: string[]
  /** 体验代码 2 级列表 */
  secondCodeTag?: string[]
  /** 体验代码 3 级列表 */
  threeCodeTag?: string[]
  /** 体验代码 4 级列表 */
  fourCodeTag?: string[]
  /** 是否我的事件（1 是我的事件，0 或空为全部事件） */
  isMine?: string

  /** 透传后端扩展筛选条件 */
  [key: string]: any
}

/**
 * H5 页面原始筛选参数。
 * 页面当前仍会携带 brandCode、eventValidity、topics 等 UI 字段，调用批量接口前统一转换。
 */
export interface BatchEventPageRequestParams extends Record<string, any> {
  brandCode?: string
  brandCodes?: string[]
  /** 旧版 H5 单选事件有效性字段，调用批量接口前转换为 eventValidityList */
  eventValidity?: string
  eventValidityList?: string[]
  /** 旧版 H5 标准观点字段，调用批量接口前转换为 topicList */
  topics?: string[]
  topicList?: string[]
  /** 旧版 H5 处理人员字段，调用批量接口前转换为 handlerUserIds */
  handlerIds?: string[]
  handlerUserIds?: string[]
}

/**
 * 新增事件统计接口返回数据。
 */
export interface TaskEventNewlyVo {
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
 * 事件状态分布接口返回单项。
 */
export interface TaskEventStatusDistributionVo {
  /** 任务状态编码 */
  taskStatus?: string
  /** 任务状态名称 */
  taskStatusName?: string
  /** 当前周期事件数量 */
  currentCounts?: number
  /** 占比 */
  percent?: number
}

/**
 * 事件趋势接口返回单项。
 */
export interface TaskEventTrendVo {
  /** 日期 */
  dateStr?: string
  /** 事件数量 */
  counts?: number
}

/**
 * 批量事件移动端分页列表项。
 */
export interface BatchEventMobilePageVo {
  /** 事件 ID */
  id?: string
  /** 预警事件编号 */
  warningEventNo?: string
  /** 事件名称 */
  eventName?: string
  /** 主题分类 */
  subjectCategoryName?: string
  /** 事件等级 */
  eventLevelName?: string
  /** 品牌名称 */
  brandName?: string
  /** 车系名称 */
  carSeriesName?: string
  /** 观点名称 */
  topicName?: string
  /** 观点问题/标准观点 */
  topicText?: string
  /** 用车场景 */
  usageScenario?: string
  /** 用户类型 */
  custType?: string
  /** 提及量 */
  mentionCount?: string
  /** 负面率 */
  negativeRate?: string
  /** 事件优先级名称 */
  eventPriorityName?: string
  /** 审核人姓名 */
  reviewUserName?: string
  /** 主责人姓名 */
  mainRespUserName?: string
  /** 主责部门 */
  primaryDepName?: string
  /** 任务状态编码 */
  taskStatus?: string
  /** 任务状态名称 */
  taskStatusName?: string
  /** 事件有效性 */
  eventValidity?: string
  /** 预警时间 */
  warningTime?: string
  /** 更新时间 */
  updateTime?: string
  /** 当前用户权限列表 */
  permissions?: string[]
  /** 体验代码1级标签 */
  firstCodeTag?: string
  /** 体验代码2级标签 */
  secondCodeTag?: string
  /** 体验代码3级标签 */
  threeCodeTag?: string
  /** 体验代码4级标签 */
  fourCodeTag?: string

  /** 兼容详情页入参，批量列表 swagger 当前未返回 dataId */
  dataId?: string
  /** 兼容现有列表卡片字段 */
  eventPriority?: string
  eventLevel?: string
  mainRespOrgName?: string
  topic?: string

  [key: string]: any
}

/**
 * 批量事件列表分页结构。
 */
export interface PageInfoBatchEventMobilePageVo {
  total?: number
  list?: BatchEventMobilePageVo[]
  pageNum?: number
  pageSize?: number
  size?: number
  startRow?: number
  endRow?: number
  pages?: number
  prePage?: number
  nextPage?: number
  isFirstPage?: boolean
  isLastPage?: boolean
  hasPreviousPage?: boolean
  hasNextPage?: boolean
  navigatePages?: number
  navigatepageNums?: number[]
  navigateFirstPage?: number
  navigateLastPage?: number
}

/**
 * 批量事件客户原声列表查询入参。
 * 当前后端仅要求事件 newId 与分页参数。
 */
export interface BatchEventUserVoiceListQuery {
  pageSize?: number
  pageNum?: number
  /** 批量事件 ID，按后端约定透传到 newId 查询 */
  newId?: string
}

/**
 * 批量事件客户原声观点标签。
 */
export interface BatchEventUserVoiceTopicVo {
  /** 观点 */
  topic?: string
  /** 意图 */
  intention?: string
  /** 情感 */
  sentiment?: string
}

/**
 * 批量事件客户原声列表项。
 */
export interface BatchEventUserVoiceVo {
  /** 原始声音 */
  originalTextScene?: string
  /** 标题 */
  title?: string
  /** 文章质量 */
  quality?: string
  /** 浏览时长 */
  browsingDuration?: string
  /** 观点 */
  topics?: BatchEventUserVoiceTopicVo[]
  /** 声音 id */
  newId?: string
  /** 原文 id */
  originalId?: string
  /** 意图 */
  intent?: string
  /** 情感 */
  sentiment?: string
  /** 单观点文本 */
  topic?: string
  /** 原文内容 */
  content?: string
  /** 用户 id */
  userId?: string
  /** 品牌名称 */
  brandName?: string
  /** 车系名称 */
  carSeriesName?: string
  /** 车型名称 */
  modelName?: string
  /** 用户名 */
  username?: string
  /** 渠道名称 */
  channelName?: string
  /** 渠道编码 */
  channelCode?: string
  /** 评价时间 */
  evaluateTime?: string
  /** 兼容声音列表历史字段 */
  id?: string | number
  dataId?: string
  originalTexTScene?: string
  custName?: string
  channel?: string
  dataCreateTime?: string
  /** 透传后端扩展字段 */
  [key: string]: any
}

/**
 * 批量事件客户原声分页结构。
 */
export interface PageInfoBatchEventUserVoiceVo {
  total?: number
  list?: BatchEventUserVoiceVo[]
  pageNum?: number
  pageSize?: number
  size?: number
  startRow?: number
  endRow?: number
  pages?: number
  prePage?: number
  nextPage?: number
  isFirstPage?: boolean
  isLastPage?: boolean
  hasPreviousPage?: boolean
  hasNextPage?: boolean
  navigatePages?: number
  navigatepageNums?: number[]
  navigateFirstPage?: number
  navigateLastPage?: number
}

/**
 * 批量事件 ID 入参。
 */
export interface BatchEventCommonModel {
  /** 事件 ID */
  id?: string
}

/**
 * 批量事件权限查询入参。
 */
export interface BatchEventPermissionModel {
  /** 事件 ID */
  eventId: string
}

/**
 * 当前用户在批量事件中的功能权限。
 */
export interface BatchEventPermissionVo {
  /** 查看事件 */
  viewEvent?: boolean
  /** 审核通过 */
  approve?: boolean
  /** 审核关闭 */
  approveClose?: boolean
  /** 关闭事件 */
  closeEvent?: boolean
  /** 确认处理 */
  confirm?: boolean
  /** 新建任务 */
  createTask?: boolean
  /** 修改任务 */
  editTask?: boolean
  /** 删除任务 */
  deleteTask?: boolean
  /** 更新任务进度 */
  updateTaskProgress?: boolean
  /** 下发事件 */
  createEvent?: boolean
  /** 导出事件 */
  exportEvent?: boolean
  /** 新增抄送人 */
  addCcUser?: boolean
  /** 事件驳回 */
  rejectEvent?: boolean
  /** 转派处理人 */
  reassignHandler?: boolean
  /** 用户角色类型 */
  roleType?: string
  /** 用户角色类型名称 */
  roleTypeName?: string
}

/**
 * 批量事件多 ID 入参。
 */
export interface BatchEventBatchCommonModel {
  /** 事件 ID 列表 */
  ids?: string[]
  /** 原声结果 ID 列表，仅原声事件下发弹窗使用 */
  soundIds?: string[]
}

export interface BatchEventCreateModel {
  /** 原声 ID 列表，多个使用英文逗号拼接 */
  ids?: string
  /** 数据源类型：RESULT-结果数据，ORIGINAL-原始数据 */
  dataSourceType?: 'RESULT' | 'ORIGINAL'
  /** 事件名称 */
  eventName?: string
  /** 品牌编码 */
  brandCode?: string
  /** 品牌名称 */
  brandName?: string
  /** 事件优先级 */
  eventPriority?: string
  /** 主责二级部门 ID */
  primarySecondDepId?: string
  /** 主责二级部门名称 */
  primarySecondDepName?: string
  /** 主责部门 ID */
  primaryDepId?: string
  /** 主责部门名称 */
  primaryDepName?: string
  /** 主责人 ID */
  primaryId?: string
  /** 主责人工号 */
  primaryEmpNo?: string
  /** 主责人姓名 */
  primaryName?: string
  /** 聚焦观点编码，多个使用英文逗号拼接 */
  topic?: string
  /** 聚焦观点名称，多个使用英文逗号拼接 */
  topicName?: string
  /** 渠道名称，多个使用英文逗号拼接 */
  channelNames?: string
  /** 添加说明 */
  issueDescription?: string
}

/**
 * 批量事件聚焦场景查询入参。
 */
export interface BatchEventSceneQueryModel extends BatchEventCommonModel {
  /** 车系标识 */
  carSeriesCode?: string
  /** 情感倾向；不传表示全部情感 */
  sentiment?: string
}

export interface BatchEventTaskCreateModel {
  eventId?: string
  taskName?: string
  deptType?: string
  handleDeptId?: string
  handleDeptName?: string
  assigneeId?: string
  assigneeName?: string
  ccUserIds?: string[]
  taskDesc?: string
}

export interface BatchEventTaskEditModel extends Omit<BatchEventTaskCreateModel, 'eventId'> {
  taskId?: string
}

export interface BatchEventTaskDeleteModel {
  taskId?: string
}

export interface BatchEventTaskReassignModel {
  taskId?: string
  handleDeptId?: string
  handleDeptName?: string
  assigneeId?: string
  assigneeName?: string
}

export interface BatchEventTaskProgressModel {
  taskId?: string
  progressStatus?: string
  progressRemark?: string
}

export interface BatchEventRejectModel extends BatchEventCommonModel {
  rejectReason?: string
  description?: string
}

export interface BatchEventBatchRejectModel {
  eventIds?: string[]
  rejectReason?: string
  description?: string
}

export interface BatchEventCloseModel extends BatchEventCommonModel {
  closeReason?: string
  description?: string
}

export interface BatchEventBatchCloseModel {
  eventIds?: string[]
  closeReason?: string
  description?: string
}

export interface BatchEventUserModel {
  /** 抄送人ID */
  userId?: string
  /** 抄送人姓名 */
  userName?: string
  /** 抄送人工号 */
  userEmpNo?: string
  /** 二级部门id */
  leve2DeptId?: string
  /** 二级部门名称 */
  leve2DeptName?: string
  /** 三级部门id */
  leve3DeptId?: string
  /** 三级部门名称 */
  leve3DeptName?: string
  /** 是否全部人员(1-是/0-否) */
  allFlag?: number
}

export interface BatchEventApproveModel extends BatchEventCommonModel {
  /** 审核人(业务责任人)ID */
  reviewUserId?: string
  /** 审核人(业务责任人)工号 */
  reviewUserEmpNo?: string
  /** 审核人(业务责任人)姓名 */
  reviewUserName?: string
  /** 抄送人列表 */
  ccUsers?: BatchEventUserModel[]
  /** 审核说明 */
  description?: string
}

export interface BatchEventConfirmModel extends BatchEventCommonModel {
  /** 主责人ID */
  mainRespUserId?: string
  /** 主责人姓名 */
  mainRespUserName?: string
  /** 主责人工号 */
  mainRespUserEmpNo?: string
  /** 主责部门ID */
  mainRespOrgId?: string
  /** 主责部门名称 */
  mainRespOrgName?: string
  /** 处理方式(VOC - 系统闭环/ZJZ -执剑者闭环) */
  handleMode?: string
  /** 协同部门二级 ID，多个用英文逗号分隔 */
  coordinateSecondDeptId?: string
  /** 协同部门二级名称，多个用英文逗号分隔 */
  coordinateSecondDeptName?: string
  /** 协同部门三级 ID，多个用英文逗号分隔 */
  coordinateThirdDeptId?: string
  /** 协同部门三级名称，多个用英文逗号分隔 */
  coordinateThirdDeptName?: string
  /** 协同人员 ID，多个用英文逗号分隔 */
  coordinateUserId?: string
  /** 协同人员工号，多个用英文逗号分隔 */
  coordinateUserEmpNo?: string
  /** 协同人员名称，多个用英文逗号分隔 */
  coordinateUserName?: string
  /** 用户类型名称，多个用英文逗号分隔 */
  custType?: string
  /** 用车场景名称，多个用英文逗号分隔 */
  usageScenario?: string
  /** 观点问题名称，多个用英文逗号分隔 */
  topicText?: string
  /** 页面 URL */
  pageUrl?: string
  /** 确认说明 */
  description?: string
}

export interface BatchEventCcModel extends BatchEventCommonModel {
  /** 抄送人列表 */
  ccUsers?: BatchEventUserModel[]
  /** 抄送说明 */
  description?: string
}

export interface BatchEventReassignModel extends BatchEventCommonModel {
  /** 事件转派人ID */
  handlerId?: string
  /** 事件转派人姓名 */
  handlerName?: string
  /** 转派说明 */
  description?: string
}

export interface BatchEventHandleCompleteModel extends BatchEventCommonModel {
  /** 处理结果 */
  handleResult?: string
  /** 处理说明 */
  description?: string
}

export interface BatchEventExecutorCallbackModel {
  eventId?: string
  executorTaskId?: string
  callbackStatus?: string
  callbackContent?: string
}

export interface BatchEventOptionVo {
  label?: string
  value?: string
  name?: string
  code?: string
  [key: string]: any
}

export interface BatchEventConditionsVo {
  taskStatusList?: BatchEventOptionVo[]
  brandList?: BatchEventOptionVo[]
  carSeriesList?: BatchEventOptionVo[]
  eventPriorityList?: BatchEventOptionVo[]
  eventValidityList?: BatchEventOptionVo[]
  custTypeList?: BatchEventOptionVo[]
  usageScenarioList?: BatchEventOptionVo[]
  topicTextList?: BatchEventOptionVo[]
}

export interface BatchEventBriefDetailVo {
  /** 事件名称 */
  eventName?: string
  /** 事件编号 */
  warningEventNo?: string
  /** 预警频率 */
  warningPeriod?: string
  /** 预警时间 */
  warningTime?: string
  /** 主题分类名称 */
  subjectCategoryName?: string
  /** 处理优先级 */
  eventPriorityName?: string
  /** 品牌编码 */
  brandCode?: string
  /** 品牌名称 */
  brandName?: string
  /** 创建人员姓名 */
  createUserName?: string
  /** 审核人姓名 */
  reviewUserName?: string
  /** 业务责任人工号 */
  mainRespUserEmpNo?: string
  /** 业务责任人姓名 */
  mainRespUserName?: string
  /** 主责部门名称 */
  primaryDepName?: string
  /** 业务责任人二级部门名称 */
  secondDeptName?: string
  /** 业务责任人三级部门名称 */
  thirdDeptName?: string
  /** 业务责任部门名称，后端已废弃，仅用于兼容旧数据 */
  mainRespOrgName?: string
  /** 处理方式(VOC/执剑者) */
  handleMode?: string
  /** 当前处理进度 */
  taskStatusName?: string
  /** 聚焦观点列表 */
  focusTopics?: string[]
  /** 事件是否被驳回，1-驳回，其他为空 */
  isReject?: string
  [key: string]: any
}

export interface BatchEventDataStatVo {
  /** 负面率占比 */
  negativeRatio?: string | number
  /** 正面率占比 */
  positiveRatio?: string | number
  /** 提及量数量 */
  mentionCount?: string | number
  /** 用户数数量 */
  userCount?: string | number
}

export interface BatchEventDateCountItem {
  date?: string
  count?: string | number
}

export interface BatchEventTrendStatVo {
  positive?: BatchEventDateCountItem[] | null
  neutral?: BatchEventDateCountItem[] | null
  negative?: BatchEventDateCountItem[] | null
}

export interface BatchEventCarSeriesItem {
  carSeriesName?: string
  carSeriesCode?: string
  num?: string | number
  /** 旧接口字段，仅作灰度兼容 */
  count?: string | number
}

export type BatchEventCarSeriesStatVo = BatchEventCarSeriesItem[]

export interface BatchEventSceneItem {
  sceneName?: string
  sceneCode?: string
  positiveCount?: number
  neutralCount?: number
  negativeCount?: number
}

export type BatchEventSceneStatVo = BatchEventSceneItem[]

export interface BatchEventOpinionItem {
  opinion?: string
  totalMentions?: string | number
  /** 情感倾向：正面/中性/负面，或 voc_sentiment 对应字典值 */
  sentiment?: string
}

export type BatchEventOpinionStatVo = BatchEventOpinionItem[]

export interface BatchEventProvinceItem {
  provinceCode?: string
  provinceName?: string
  num?: string | number
  percentage?: string | number
  /** 旧接口字段，仅作灰度兼容 */
  count?: string | number
}

export type BatchEventProvinceStatVo = BatchEventProvinceItem[]

export interface BatchEventChannelItem {
  channelName?: string
  num?: string | number
  percentage?: string | number
  /** 旧接口字段，仅作灰度兼容 */
  count?: string | number
}

export type BatchEventChannelStatVo = BatchEventChannelItem[]

export interface BatchEventReportSummaryVo {
  summary?: string | null
}

export interface BatchEventTaskVo {
  taskId?: string
  eventId?: string
  taskName?: string
  assigneeId?: string
  assigneeName?: string
  handleDeptId?: string
  handleDeptName?: string
  deptType?: string
  ccUserNames?: string[]
  progressStatus?: string
  progressStatusName?: string
  deadline?: string
  taskDesc?: string
  handleTime?: string
  progressRemark?: string
  editable?: boolean
  deletable?: boolean
  reassignable?: boolean
  isExecutorTask?: boolean
  progressEditable?: boolean
  createTime?: string
  updateTime?: string
}

export interface BatchEventCcUserVo {
  id?: string
  /** 抄送人员 ID */
  nodeUserId?: string
  /** 兼容后端可能直接返回的人员 ID */
  userId?: string
  nodeUserEmpNo?: string
  nodeUserName?: string
  createTime?: string
  leve2DeptId?: string
  leve2DeptName?: string
  leve3DeptId?: string
  leve3DeptName?: string
  allFlag?: number
}

export interface BatchEventOpeLogVo {
  id?: number
  eventId?: string
  operateType?: string
  operateTypeName?: string
  operateOrgId?: string
  operateOrgName?: string
  operateUserId?: string
  operateUserEmpNo?: string
  operateUserName?: string
  content?: Array<Record<string, any>>
  operateTime?: string
}
