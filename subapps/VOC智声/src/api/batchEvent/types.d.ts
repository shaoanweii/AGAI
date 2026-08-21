/**
 * 批量事件接口类型定义。
 * 类型来源于报表服务 swagger 的 PC 批量事件目录，移动端接口与废弃接口不在这里声明。
 */

export interface BatchEventOptionVo {
  /** 选项编码 */
  code?: string
  /** 选项名称 */
  name?: string
}

/** 事件下发聚焦观点选项 */
export type BatchEventTopicOption = BatchEventOptionVo

export interface BatchEventRuleCategoryVo extends Record<string, unknown> {
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

/** 事件下发数据源类型：结果数据或原始数据 */
export type BatchEventDataSourceType = 'RESULT' | 'ORIGINAL'

export interface BatchEventQueryModel {
  /** 每页条数 */
  pageSize?: number
  /** 页码 */
  pageNum?: number
  /** 排序字段 */
  order?: string
  /** 是否仅我的事件，沿用页面头部事件范围 */
  isMine?: string
  /** 开始时间 yyyy-MM-dd HH:mm:ss */
  startTime?: string
  /** 结束时间 yyyy-MM-dd HH:mm:ss */
  endTime?: string
  /** 事件名称（Swagger 字段，模糊查询） */
  eventName?: string
  /** 预警事件编号 */
  warningEventNo?: string
  /** 创建人员 */
  createUser?: string
  /** 品牌编码 */
  brandCodes?: string[]
  /** 车系中文名称列表，字段名沿用后端历史约定 */
  carSeriesCodes?: string[]
  /** 观点编码 */
  topic?: string
  /** 主题分类 */
  subjectCategoryIds?: string[]
  /** 事件状态 */
  taskStatuses?: string[]
  /** 处理优先级 */
  eventPriorities?: string[]
  /** 审核人 ID */
  reviewUserId?: string
  /** 主责人 ID */
  mainRespUserId?: string
  /** 事件属性 JSON */
  eventAttribute?: string
  /** 主责部门 ID 列表 */
  mainRespOrgId?: string[]
  /** 事件有效性 */
  eventValidityList?: string[]
  /** 事件属性 */
  eventAttributeList?: string[]
  /** 一级体验代码 */
  firstCodeTag?: string[]
  /** 二级体验代码 */
  secondCodeTag?: string[]
  /** 三级体验代码 */
  threeCodeTag?: string[]
  /** 四级体验代码 */
  fourCodeTag?: string[]
  /** 标准观点 */
  topicList?: string[]
  /** 预警频率 */
  warningPeriods?: string[]
  /** 意图 */
  intentionList?: string[]
  /** 业务责任人 */
  reviewUserName?: string
  /** 协同部门 */
  collaborateOrgIds?: string[]
  /** 处理人员 */
  handlerUserIds?: string[]
  /** 是否驳回 */
  isRejectedList?: string[]
  /** 驳回原因 */
  rejectReasonList?: string[]
  /** 是否超管角色（前端不传） */
  isSuperRole?: boolean
  /** 当前用户组织 ID 及所有父级 ID（前端不传） */
  currentOrgIds?: string[]
  /** 当前用户 ID（前端不传） */
  currentUserId?: string
  /** 查询单位（前端不传） */
  dateUnit?: number
  /** 环比类型（前端不传） */
  typeR?: number
  /** 用户计算环比偏移量（前端不传） */
  scaleMagnitude?: number
  /** 是否移动端（前端不传） */
  isMobile?: boolean
}

export interface BatchEventConditionsQueryModel {
  /** 事件 ID 列表，批量事件页面与批量响应使用 */
  ids?: string[]
  /** 原声结果 ID 列表，仅原声查询页事件下发弹窗使用 */
  soundIds?: string[]
  /** 数据源类型：RESULT-结果数据，ORIGINAL-原始数据 */
  dataSourceType?: BatchEventDataSourceType
}

export interface BatchRiskEventPageVo {
  /** 预警 ID */
  id?: string
  /** 事件编号 */
  warningEventNo?: string
  /** 事件名称 */
  warningEventName?: string
  /** 主题分类 */
  subjectCategoryName?: string
  /** 事件优先级 */
  eventPriority?: string
  /** 结果表数据源名称集合 */
  channelNames?: string
  /** 品牌名称 */
  brandName?: string
  /** 车系名称 */
  carSeriesName?: string
  /** 观点问题 */
  topicText?: string
  /** 意图 */
  intention?: string
  /** 品牌编码 */
  brandCode?: string
  /** 观点列表 */
  topicName?: string
  /** 提及量 */
  mentionCount?: string
  /** 提及量环比 */
  mentionCountRate?: string
  /** 负面率 */
  negativeRate?: string
  /** 负面率环比 */
  negativeRateR?: string
  /** 用户数 */
  userCount?: string
  /** 用户数环比 */
  userRateR?: string
  /** 有效发声数 */
  validUserCount?: string
  /** 有效发声数环比 */
  validUserRateR?: string
  /** 主责人 ID */
  mainRespUserId?: string
  /** 主责人工号 */
  mainRespUserEmpNo?: string
  /** 主责人姓名 */
  mainRespUserName?: string
  /** 主责部门 ID */
  primaryDepId?: string
  /** 主责部门名称 */
  primaryDepName?: string
  /** 数据源 ids */
  ids?: string
  /** 是否处理 */
  isProcessed?: string
  /** 处理人员 */
  processedUserName?: string
  /** 协同部门二级 ID（JSON 数组） */
  coordinateSecondDeptId?: string
  /** 协同部门二级名称（JSON 数组） */
  coordinateSecondDeptName?: string
  /** 协同部门三级 ID（JSON 数组） */
  coordinateThirdDeptId?: string
  /** 协同部门三级名称（JSON 数组） */
  coordinateThirdDeptName?: string
  /** 协同部门人员 ID（JSON 数组） */
  coordinateUserId?: string
  /** 协同部门人员工号（JSON 数组） */
  coordinateUserEmpNo?: string
  /** 协同部门人员名称（JSON 数组） */
  coordinateUserName?: string
  /** 更新时间 */
  updateTime?: string
  /** 预警频率 */
  warningPeriod?: string
  /** 预警时间 */
  warningTime?: string
  /** 事件属性 */
  eventAttributes?: string
  /** 事件有效性 */
  eventValidity?: string
  /** 关闭原因 */
  closeDescription?: string
  /** 是否驳回 */
  isRejected?: string
  /** 驳回原因 */
  rejectReason?: string
  /** 任务状态 */
  taskStatus?: string
  /** 任务状态名称 */
  taskStatusName?: string
  /** 业务责任人二级部门名称 */
  secondDeptName?: string
  /** 业务责任人三级部门名称 */
  thirdDeptName?: string
  /** 审核人姓名 */
  reviewUserName?: string
  /** 用车场景 */
  usageScenario?: string
  /** 用户类型 */
  custType?: string
  /** 体验代码 1 级标签 */
  firstCodeTag?: string
  /** 体验代码 2 级标签 */
  secondCodeTag?: string
  /** 体验代码 3 级标签 */
  threeCodeTag?: string
  /** 体验代码 4 级标签 */
  fourCodeTag?: string
  /** 权限集合（view/approve/confirm/handle） */
  permissions?: string[]
  /** 创建人名称 */
  createUserName?: string
  /** 手动下发事件的数据源类型：RESULT 或 ORIGINAL */
  dataSourceType?: string
}

export interface PageInfoBatchRiskEventPageVo {
  total?: number
  list?: BatchRiskEventPageVo[]
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

export type BatchEventDashboardEventType = 'BATCH' | 'SINGLE'

export interface BatchEventDashboardQueryModel {
  /** 开始日期，格式 yyyy-MM-dd */
  startDate: string
  /** 结束日期，格式 yyyy-MM-dd */
  endDate: string
  /** 事件类型：BATCH-批量事件，SINGLE-单点事件 */
  eventType?: BatchEventDashboardEventType
}

export interface DashboardStatCard {
  /** 状态编码 */
  status?: string
  /** 状态名称 */
  statusName?: string
  /** 数量 */
  count?: number
  /** 环比 */
  changeRate?: string
}

export interface DashboardEventItem {
  /** 事件 ID */
  id?: string
  /** 单点事件详情所需原声 ID，由后端在 SINGLE 场景返回 */
  dataId?: string
  /** 事件名称 */
  eventName?: string
  /** 主题分类 */
  subjectCategoryName?: string
  /** 优先级 */
  eventPriorityName?: string
  /** 主责部门 */
  primaryDepName?: string
  /** 事件状态 */
  taskStatus?: string
  /** 事件状态名称 */
  taskStatusName?: string
  /** 预警时间 */
  warningTime?: string
  /** 品牌名称 */
  brandName?: string
  /** 车系名称 */
  carSeriesName?: string
}

export interface BatchEventConditionsVo {
  /** 任务状态选项列表 */
  taskStatusList?: BatchEventOptionVo[] | null
  /** 品牌选项列表 */
  brandList?: BatchEventOptionVo[] | null
  /** 车系选项列表 */
  carSeriesList?: BatchEventOptionVo[] | null
  /** 事件优先级选项列表 */
  eventPriorityList?: BatchEventOptionVo[] | null
  /** 事件有效性选项列表 */
  eventValidityList?: BatchEventOptionVo[] | null
  /** 用户类型选项列表 */
  custTypeList?: BatchEventOptionVo[] | null
  /** 用车场景选项列表 */
  usageScenarioList?: BatchEventOptionVo[] | null
  /** 聚焦观点选项列表 */
  topicTextList?: BatchEventOptionVo[] | null
}

export interface BatchEventBriefQueryModel {
  /** 事件 ID */
  id?: string
}

export interface BatchEventCommonModel {
  /** 事件 ID */
  id?: string
}

export interface BatchEventPermissionModel {
  /** 事件 ID */
  eventId: string
}

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

export interface BatchEventSceneQueryModel extends BatchEventBriefQueryModel {
  /** 车系标识，来自车系分布接口 */
  carSeriesCode?: string
  /** 情感倾向，来自 voc_sentiment 字典；不传表示全部情感 */
  sentiment?: string
}

export interface BatchEventBriefDetailVo {
  eventName?: string
  warningEventNo?: string
  warningPeriod?: string
  warningTime?: string
  subjectCategoryName?: string
  eventPriorityName?: string
  brandCode?: string
  brandName?: string
  reviewUserName?: string
  mainRespUserName?: string
  mainRespUserEmpNo?: string
  /** 主责部门名称 */
  primaryDepName?: string
  mainRespOrgName?: string
  /** 事件来源：SYSTEM-系统自动，MANUAL-手动下发 */
  eventSource?: 'SYSTEM' | 'MANUAL' | string
  /** 处理方式（VOC/执剑者） */
  handleMode?: string
  /** 数据源类型：RESULT-结果数据，ORIGINAL-原始数据；为空时前端按 RESULT 处理 */
  dataSourceType?: BatchEventDataSourceType
  taskStatusName?: string
  focusTopics?: string[] | null
  /** 事件是否被驳回，1-驳回，其他为空 */
  isReject?: string
  /** 驳回原因 */
  rejectReason?: string
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

export interface BatchEventUserModel {
  /** 抄送人 ID */
  userId?: string
  /** 抄送人姓名 */
  userName?: string
  /** 抄送人工号 */
  userEmpNo?: string
  /** 二级部门 ID（添加抄送人接口 Swagger 字段） */
  leve2DeptId?: string
  /** 二级部门名称（添加抄送人接口 Swagger 字段） */
  leve2DeptName?: string
  /** 三级部门 ID（添加抄送人接口 Swagger 字段） */
  leve3DeptId?: string
  /** 三级部门名称（添加抄送人接口 Swagger 字段） */
  leve3DeptName?: string
  /** 兼容历史提交字段 */
  orgId?: string
  /** 兼容历史提交字段 */
  orgNo?: string
  /** 兼容历史提交字段 */
  orgName?: string
  /** 兼容历史提交字段 */
  deptName?: string
  allFlag?: number | boolean
}

export interface BatchEventApproveModel {
  id?: string
  reviewUserId?: string
  reviewUserName?: string
  reviewUserEmpNo?: string
  /** 业务责任人二级部门名称 */
  secondDeptName?: string
  /** 业务责任人三级部门名称 */
  thirdDeptName?: string
  ccUsers?: BatchEventUserModel[]
  description?: string
}

export interface BatchEventBatchApproveModel {
  eventIds?: string[]
  reviewUserId?: string
  reviewUserName?: string
  reviewUserEmpNo?: string
  /** 业务责任人二级部门名称 */
  secondDeptName?: string
  /** 业务责任人三级部门名称 */
  thirdDeptName?: string
  description?: string
}

export interface BatchEventConfirmModel {
  id?: string
  mainRespUserId?: string
  mainRespUserName?: string
  mainRespUserEmpNo?: string
  mainRespOrgId?: string
  mainRespOrgName?: string
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
  pageUrl?: string
  description?: string
}

export interface BatchEventBatchConfirmModel {
  eventIds?: string[]
  mainRespUserId?: string
  mainRespUserName?: string
  mainRespUserEmpNo?: string
  mainRespOrgId?: string
  mainRespOrgName?: string
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
  pageUrl?: string
  description?: string
}

export interface BatchEventCreateModel {
  /** 原声/结果 ids，多个用英文逗号分隔 */
  ids?: string
  /** 数据源类型：RESULT-结果数据，ORIGINAL-原始数据 */
  dataSourceType?: BatchEventDataSourceType
  /** 事件名称，不超过 20 字符 */
  eventName?: string
  /** 品牌编码 */
  brandCode?: string
  /** 品牌名称 */
  brandName?: string
  /** 处理优先级，可选 P0/P1/P2/P3/P4 */
  eventPriority?: string
  /** 主责二级部门 id */
  primarySecondDepId?: string
  /** 主责二级部门名称 */
  primarySecondDepName?: string
  /** 主责部门 id */
  primaryDepId?: string
  /** 主责部门名称 */
  primaryDepName?: string
  /** 主责人 id */
  primaryId?: string
  /** 主责人工号 */
  primaryEmpNo?: string
  /** 主责人姓名 */
  primaryName?: string
  /** 聚焦观点编码，多个用英文逗号分隔 */
  topic?: string
  /** 聚焦观点名称，多个用英文逗号分隔 */
  topicName?: string
  /** 渠道名称，多个用英文逗号分隔 */
  channelNames?: string
  /** 添加说明，不超过 200 字符 */
  issueDescription?: string
}

export interface BatchEventExecutorCallbackModel {
  /** 事件 ID */
  eventId?: string
  /** 执剑者系统任务 ID */
  executorTaskId?: string
  /** 回调状态 SUCCESS/FAIL */
  callbackStatus?: string
  /** 回调返回内容 */
  callbackContent?: string
}

export interface BatchEventListSoundsQueryModel extends VocQueryParams {
  /** 批量事件 ID，查询批量事件详情原声时传入 */
  newId?: string
}

export interface VocListOriginalContentVo extends Record<string, any> {
  id?: string
  dataId?: string
  channelCode?: string
  channel?: string
  channelCatagory?: string
  originalId?: string
  originalTexTScene?: string
  title?: string
  brand?: string
  series?: string
  dataCreateTime?: string
  oneId?: string
  custName?: string
  topic?: string
  topics?: any[]
}

export interface PageInfoVocListOriginalContentVo {
  total?: number
  list?: VocListOriginalContentVo[]
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

export interface BatchEventCloseModel {
  id?: string
  closeReason?: string
  description?: string
}

export interface BatchEventBatchCloseModel {
  eventIds?: string[]
  closeReason?: string
  description?: string
}

export interface BatchEventRejectModel {
  id?: string
  rejectReason?: string
  description?: string
}

export interface BatchEventBatchRejectModel {
  eventIds?: string[]
  rejectReason?: string
  description?: string
}

export interface BatchEventCcModel {
  id?: string
  ccUsers?: BatchEventUserModel[]
  description?: string
}

export interface BatchEventReassignModel {
  id?: string
  handlerId?: string
  handlerName?: string
  description?: string
}

export interface BatchEventHandleCompleteModel {
  id?: string
  handleResult?: string
  description?: string
}

export interface BatchEventTaskVo {
  /** 任务 ID */
  taskId?: string
  /** 事件 ID */
  eventId?: string
  /** 任务名称 */
  taskName?: string
  /** 处理人 ID */
  assigneeId?: string
  /** 处理人姓名 */
  assigneeName?: string
  /** 处理部门 ID */
  handleDeptId?: string
  /** 处理部门名称 */
  handleDeptName?: string
  /** 责任部门类型：MAIN-主责部门，COOP-协同部门 */
  deptType?: string
  /** 抄送人姓名列表 */
  ccUserNames?: string[]
  /** 进度状态编码 */
  progressStatus?: string
  /** 进度状态名称 */
  progressStatusName?: string
  /** 截止日期 */
  deadline?: string
  /** 任务描述 */
  taskDesc?: string
  /** 处理时间 */
  handleTime?: string
  /** 进度备注 */
  progressRemark?: string
  /** 是否可编辑 */
  editable?: boolean
  /** 是否可删除 */
  deletable?: boolean
  /** 是否可转派 */
  reassignable?: boolean
  /** 是否为执剑者流转任务 */
  isExecutorTask?: boolean
  /** 是否可修改进度 */
  progressEditable?: boolean
  /** 创建时间 */
  createTime?: string
  /** 更新时间 */
  updateTime?: string
}

export interface BatchEventTaskCreateModel {
  /** 事件 ID */
  eventId?: string
  /** 任务名称，20 字上限，必填 */
  taskName?: string
  /** 责任部门类型：MAIN-主责部门，COOP-协同部门，必填 */
  deptType?: string
  /** 处理部门 ID，必填 */
  handleDeptId?: string
  /** 处理部门名称，必填 */
  handleDeptName?: string
  /** 处理人 ID，必填 */
  assigneeId?: string
  /** 处理人姓名，必填 */
  assigneeName?: string
  /** 抄送人 ID 列表 */
  ccUserIds?: string[]
  /** 处理说明，200 字上限，非必填 */
  taskDesc?: string
}

export interface BatchEventTaskEditModel {
  /** 任务 ID */
  taskId?: string
  /** 任务名称，20 字上限，必填 */
  taskName?: string
  /** 责任部门类型：MAIN-主责部门，COOP-协同部门，必填 */
  deptType?: string
  /** 处理部门 ID，必填 */
  handleDeptId?: string
  /** 处理部门名称，必填 */
  handleDeptName?: string
  /** 处理人 ID，必填 */
  assigneeId?: string
  /** 处理人姓名，必填 */
  assigneeName?: string
  /** 抄送人 ID 列表 */
  ccUserIds?: string[]
  /** 处理说明，200 字上限，非必填 */
  taskDesc?: string
}

export interface BatchEventTaskProgressModel {
  /** 任务 ID */
  taskId?: string
  /** 进度状态：NOT_STARTED-未开始，IN_PROGRESS-进行中，COMPLETED-已完成 */
  progressStatus?: string
  /** 进度备注 */
  progressRemark?: string
}

export interface BatchEventTaskDeleteModel {
  /** 任务 ID */
  taskId?: string
}

export interface BatchEventTaskReassignModel {
  /** 任务 ID */
  taskId?: string
  /** 处理部门 ID，必填 */
  handleDeptId?: string
  /** 处理部门名称，必填 */
  handleDeptName?: string
  /** 新处理人 ID，必填 */
  assigneeId?: string
  /** 新处理人姓名，必填 */
  assigneeName?: string
}

export interface BatchEventCcUserVo {
  id?: string
  nodeUserId?: string
  /** 工号 */
  nodeUserEmpNo?: string
  /** 姓名 */
  nodeUserName?: string
  nodeOrgId?: string
  nodeOrgNo?: string
  nodeOrgName?: string
  nodeStatus?: string
  /** 二级部门 ID */
  leve2DeptId?: string
  /** 二级部门名称 */
  leve2DeptName?: string
  /** 三级部门 ID */
  leve3DeptId?: string
  /** 三级部门名称 */
  leve3DeptName?: string
  /** 是否全部人员(1-是/0-否) */
  allFlag?: number
  createTime?: string
}

export interface BatchEventOpeLogContentVo {
  /** 内容类型 */
  contentType?: string
  /** 内容 */
  content?: string
}

export interface BatchEventOpeLogVo {
  id?: number | string
  eventId?: string
  operateType?: string
  operateTypeName?: string
  operateOrgId?: string
  operateOrgName?: string
  operateUserId?: string
  operateUserEmpNo?: string
  operateUserName?: string
  content?: BatchEventOpeLogContentVo[] | null
  operateTime?: string
  operatorId?: string
  operatorName?: string
  operateContent?: string
  remark?: string
  createTime?: string
}
