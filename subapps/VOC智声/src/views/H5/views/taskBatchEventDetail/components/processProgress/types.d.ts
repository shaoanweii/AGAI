import type { BatchEventStatisticsDetail } from '../statistics/types'

export type BatchProcessStepStatus = 'completed' | 'current' | 'pending'

export type BatchProcessStage = 'warningReview' | 'businessResponse' | 'closedLoop' | 'eventClosed'

export type BatchClosedLoopMode = 'voc' | 'sword'

export type BatchClosedLoopTaskStatus = 'processing' | 'pending' | 'completed'

export type BatchProcessActionDialogMode =
  | 'close'
  | 'approve'
  | 'copy'
  | 'reject'
  | 'confirm'
  | 'closedLoopClose'
  | 'deleteTask'
  | 'transferTask'
  | 'transferHandler'
  | 'updateProgress'
  | 'createTask'
  | 'editTask'

export interface BatchProcessStep {
  /** 流程步骤唯一标识 */
  key: string
  /** 流程步骤展示名称 */
  label: string
  /** 当前步骤状态 */
  status: BatchProcessStepStatus
  /** 可切换的事件处理阶段，未配置时仅展示不响应点击 */
  stage?: BatchProcessStage
}

export interface BatchProcessSelectOption {
  /** 选项文案 */
  label: string
  /** 选项值 */
  value: string
  /** 原始接口选项 */
  rawData?: any
}

export interface BatchClosedLoopModeOption extends BatchProcessSelectOption {
  /** 闭环方式值 */
  value: BatchClosedLoopMode
}

export interface BatchWarningReviewHandleForm {
  /** 业务责任人选项 */
  ownerOptions: BatchProcessSelectOption[]
  /** 当前选中的业务责任人 */
  selectedOwner?: string
  /** 添加说明 */
  description?: string
}

export interface BatchBusinessResponseHandleForm {
  /** 处理方式选项 */
  handleModeOptions: BatchClosedLoopModeOption[]
  /** 当前选中的处理方式 */
  selectedHandleMode: BatchClosedLoopMode
  /** 用户类型选项 */
  userTypeOptions: BatchProcessSelectOption[]
  /** 用车场景选项 */
  carSceneOptions: BatchProcessSelectOption[]
  /** 聚焦观点选项 */
  focusTopicOptions: BatchProcessSelectOption[]
  /** 添加说明 */
  description?: string
}

export interface BatchBusinessResponseHandleState {
  /** 主责人 ID */
  mainRespUserId: string
  /** 协同人员 ID 列表 */
  coordinatingUserIds: string[]
  /** 处理方式 */
  handleMode: BatchClosedLoopMode
  /** 用户类型选项值 */
  custTypeValues: string[]
  /** 用车场景选项值 */
  usageScenarioValues: string[]
  /** 聚焦观点选项值 */
  topicTextValues: string[]
  /** 添加说明 */
  description: string
}

export interface BatchClosedLoopTask {
  /** 任务唯一标识 */
  id: string
  /** 任务名称 */
  name: string
  /** 任务状态 */
  status: BatchClosedLoopTaskStatus
  /** 任务状态文案 */
  statusText: string
  /** 任务时间 */
  time: string
  /** 处理部门 */
  department: string
  /** 处理人 */
  handler: string
  /** 任务说明 */
  description: string
}

export interface BatchClosedLoopHandleData {
  /** VOC 系统闭环任务列表 */
  vocTasks: BatchClosedLoopTask[]
  /** 天枢星链系统闭环任务列表 */
  swordTasks: BatchClosedLoopTask[]
}

export interface BatchEventClosedHandleData {
  /** 事件关闭后展示的任务结果列表 */
  tasks: BatchClosedLoopTask[]
}

export interface BatchProcessCopyPerson {
  /** 二级部门/三级部门 */
  departmentText: string
  /** 姓名工号 */
  userText: string
}

export interface BatchProcessOperateRecord {
  /** 操作记录唯一标识 */
  id: string
  /** 操作人 */
  operatorName: string
  /** 操作类型 */
  operateType: string
  /** 操作时间 */
  operateTime: string
  /** 操作内容 */
  content: string
}

export interface BatchProcessDialogs {
  /** 关闭事件原因选项 */
  closeReasonOptions: BatchProcessSelectOption[]
  /** 默认关闭事件原因 */
  closeReasonValue: string
  /** 默认关闭事件说明 */
  closeDescription?: string
  /** 抄送人员选项 */
  copyPersonOptions: BatchProcessSelectOption[]
  /** 默认抄送人员 */
  copyPersonValue: string
  /** 通过审核确认文案 */
  approveMessage: string
  /** 驳回原因选项 */
  rejectReasonOptions: BatchProcessSelectOption[]
  /** 默认驳回原因 */
  rejectReasonValue: string
  /** 默认驳回说明 */
  rejectDescription?: string
  /** 确认处理文案 */
  confirmMessage: string
  /** 闭环处理关闭事件确认文案 */
  closedLoopCloseMessage: string
  /** 删除任务确认文案 */
  deleteTaskMessage: string
  /** 处理人员选项 */
  handlerOptions: BatchProcessSelectOption[]
  /** 默认处理人员 */
  handlerValue: string
  /** 完成进度选项 */
  progressOptions: BatchProcessSelectOption[]
  /** 默认完成进度 */
  progressValue: string
  /** 默认进度说明 */
  progressDescription?: string
  /** 新建/编辑任务名称 */
  taskName?: string
  /** 新建/编辑任务说明 */
  taskDescription?: string
  /** 部门类型选项 */
  departmentTypeOptions: BatchProcessSelectOption[]
  /** 默认部门类型 */
  departmentTypeValue: string
  /** 处理部门选项 */
  processDepartmentOptions: BatchProcessSelectOption[]
  /** 默认处理部门 */
  processDepartmentValue: string
}

export interface BatchEventProcessProgressData {
  /** 当前处理进度阶段 */
  currentStage: BatchProcessStage
  /** 流程步骤 */
  steps: BatchProcessStep[]
  /** 事件详情模块数据 */
  eventDetail: BatchEventStatisticsDetail
  /** 预警审核事件处理表单 */
  warningReviewHandle: BatchWarningReviewHandleForm
  /** 业务响应事件处理表单 */
  businessResponseHandle: BatchBusinessResponseHandleForm
  /** 闭环处理事件处理数据 */
  closedLoopHandle: BatchClosedLoopHandleData
  /** 事件关闭事件处理数据 */
  eventClosedHandle: BatchEventClosedHandleData
  /** 抄送人员 */
  copyPersons: BatchProcessCopyPerson[]
  /** 操作记录 */
  operateRecords: BatchProcessOperateRecord[]
  /** 弹窗表单数据 */
  dialogs: BatchProcessDialogs
}
