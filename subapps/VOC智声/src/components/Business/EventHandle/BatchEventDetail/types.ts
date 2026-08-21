import type { WordCloudItem } from '@/components/DataSourceAnalysis/types.d'
import type { BatchEventOptionVo } from '@/api/batchEvent/types'
import type { BatchEventTaskProgressValue } from './beConstants'

/**
 * 事件统计区基础信息项。
 */
export interface BatchEventStatisticsSummaryItem {
  label: string
  value: string
}

/**
 * 车系分布顶部排行项。
 */
export interface BatchEventStatisticsSeriesItem {
  code: string
  name: string
  mentions: number
}

/**
 * 车系联动模块中的场景分布项。
 */
export interface BatchEventStatisticsSceneItem {
  sceneName: string
  positiveMentions: number
  neutralMentions: number
  negativeMentions: number
}

/**
 * 词云筛选枚举。
 */
export type BatchEventStatisticsSentimentFilter = string

/**
 * 词云情感筛选项。
 */
export interface BatchEventStatisticsSentimentOption {
  label: string
  value: BatchEventStatisticsSentimentFilter
}

/**
 * 省份热力图数据项。
 */
export interface BatchEventStatisticsProvinceMapItem {
  provinceName: string
  provinceCode: string
  mentions: number
  negativeRate: number
}

/**
 * 省份 TOP 展示项。
 */
export interface BatchEventStatisticsProvinceTopItem {
  provinceName: string
  percent: number
  color: string
}

/**
 * 渠道分布表格项。
 */
export interface BatchEventStatisticsChannelItem {
  channelName: string
  mentions: number
  ratio: number
}

/**
 * 单个车系联动详情。
 */
export interface BatchEventStatisticsSeriesDetail {
  sceneDistribution: BatchEventStatisticsSceneItem[]
  wordCloudMap: Record<BatchEventStatisticsSentimentFilter, WordCloudItem[]>
  provinceMapData: BatchEventStatisticsProvinceMapItem[]
  provinceTopList: BatchEventStatisticsProvinceTopItem[]
  channelTable: BatchEventStatisticsChannelItem[]
}

/**
 * 车系分布联动模块数据。
 */
export interface BatchEventStatisticsSeriesDistributionModule {
  sentimentOptions: BatchEventStatisticsSentimentOption[]
  seriesDistribution: BatchEventStatisticsSeriesItem[]
  seriesDetails: Record<string, BatchEventStatisticsSeriesDetail>
}

/**
 * 批量事件处理阶段标识。
 */
export type BatchEventProcessingStageKey = 'approve' | 'confirm' | 'handle' | 'close'

/**
 * 底部操作按钮标识。
 */
export type BatchEventProcessingFooterActionType =
  | 'approve'
  | 'close'
  | 'reject'
  | 'confirm'
  | 'handleClose'
  | 'createTask'
  | 'transferHandler'
  | 'updateProgress'

/**
 * 二次确认弹窗动作标识。
 */
export type BatchEventProcessingConfirmActionType = 'approve' | 'confirm' | 'updateProgress'

/**
 * 底部按钮视觉类型。
 */
export type BatchEventProcessingFooterActionVariant = 'default' | 'primary'

/**
 * 批量事件处理进度步骤状态。
 */
export type BatchEventProcessingStepStatus = 'completed' | 'current' | 'pending'

/**
 * 处理进度步骤定义项。
 */
export interface BatchEventProcessingStepDefinition {
  label: string
  stage?: BatchEventProcessingStageKey
  statusValues: string[]
}

/**
 * 处理进度步骤项。
 */
export interface BatchEventProcessingStepItem {
  label: string
  stage?: BatchEventProcessingStageKey
  status: BatchEventProcessingStepStatus
}

/**
 * 处理进度规则摘要项。
 */
export interface BatchEventProcessingSummaryItem {
  label: string
  value: string
}

/**
 * 操作记录详情项。
 */
export interface BatchEventProcessingOperationLogContentItem {
  label?: string
  value: string
}

/**
 * 处理进度操作记录项。
 */
export interface BatchEventProcessingOperationLogItem {
  id: string
  title: string
  details: BatchEventProcessingOperationLogContentItem[]
  operator?: string
  time: string
}

/**
 * 处理进度通用选项。
 */
export interface BatchEventProcessingSelectOption {
  label: string
  value: string
  rawData?: unknown
}

/**
 * 业务响应阶段选择的闭环处理方式。
 */
export type BatchEventProcessingLoopMode = 'voc-loop' | 'sword-loop'

/**
 * VOC 任务所属部门角色。
 */
export type BatchEventProcessingTaskRoleValue = 'primary' | 'assist'

/**
 * VOC 任务进度枚举。
 */
export type BatchEventProcessingTaskProgressValue = BatchEventTaskProgressValue

/**
 * 处理进度表单字段基础配置。
 */
export interface BatchEventProcessingFieldBase {
  label: string
  required?: boolean
  placeholder?: string
}

/**
 * 处理进度单选字段配置。
 */
export interface BatchEventProcessingSingleSelectField extends BatchEventProcessingFieldBase {
  value: string
  options: BatchEventProcessingSelectOption[]
}

/**
 * 处理进度输入框字段配置。
 */
export interface BatchEventProcessingInputField extends BatchEventProcessingFieldBase {
  value: string
  maxlength?: number
}

/**
 * 处理进度多选字段配置。
 */
export interface BatchEventProcessingMultipleSelectField extends BatchEventProcessingFieldBase {
  value: string[]
  options: BatchEventProcessingSelectOption[]
  collapseTagCount?: number
}

/**
 * 处理进度批量事件条件接口多选字段配置。
 */
export interface BatchEventProcessingConditionMultipleSelectField
  extends BatchEventProcessingFieldBase {
  value: string[]
  options: BatchEventOptionVo[]
  collapseTagCount?: number
}

/**
 * 处理进度按钮组选项配置。
 */
export interface BatchEventProcessingToggleField extends BatchEventProcessingFieldBase {
  value: string
  options: BatchEventProcessingSelectOption[]
}

/**
 * 处理进度文本域字段配置。
 */
export interface BatchEventProcessingTextareaField extends BatchEventProcessingFieldBase {
  value: string
  maxlength?: number
}

/**
 * 预警审核阶段表单模块。
 */
export interface BatchEventProcessingApproveStageModule {
  stage: 'approve'
  mainOwner: BatchEventProcessingSingleSelectField
  description: BatchEventProcessingTextareaField
}

/**
 * 业务响应阶段表单模块。
 */
export interface BatchEventProcessingConfirmStageModule {
  stage: 'confirm'
  mainDepartment: BatchEventProcessingSingleSelectField
  cooperationDepartment: BatchEventProcessingMultipleSelectField
  handleMode: BatchEventProcessingToggleField
  userType: BatchEventProcessingConditionMultipleSelectField
  vehicleScene: BatchEventProcessingConditionMultipleSelectField
  pointIssue: BatchEventProcessingConditionMultipleSelectField
  description: BatchEventProcessingTextareaField
}

/**
 * VOC 任务列表项。
 */
export interface BatchEventProcessingVocTaskItem {
  id: string
  taskName: string
  description: string
  roleValue: BatchEventProcessingTaskRoleValue
  departmentValue: string
  departmentLabel?: string
  handlerValue?: string
  handlerLabel?: string
  progressText?: string
  editable?: boolean
  deletable?: boolean
  reassignable?: boolean
  progressEditable?: boolean
  progress: BatchEventProcessingTaskProgressValue
  processTime: string
}

/**
 * VOC 系统闭环模块。
 */
export interface BatchEventProcessingVocLoopModule {
  taskName: BatchEventProcessingInputField
  description: BatchEventProcessingTextareaField
  departmentRole: BatchEventProcessingSingleSelectField
  departmentOwner: BatchEventProcessingSingleSelectField
  progress: BatchEventProcessingSingleSelectField
  progressDescription: BatchEventProcessingTextareaField
  handler: BatchEventProcessingSingleSelectField
  tasks: BatchEventProcessingVocTaskItem[]
}

/**
 * 执剑者系统闭环模块。
 */
export interface BatchEventProcessingSwordLoopModule {
  tasks: BatchEventProcessingVocTaskItem[]
}

/**
 * 闭环处理阶段表单模块。
 */
export interface BatchEventProcessingHandleStageModuleConfig {
  stage: 'handle'
  vocLoop: BatchEventProcessingVocLoopModule
  swordLoop: BatchEventProcessingSwordLoopModule
}

/**
 * 事件关闭阶段任务表格项。
 * 保持展示字段扁平化，避免关闭态模板再拼装部门、人员与进度文案。
 */
export interface BatchEventProcessingCloseTaskItem {
  id: string
  taskName: string
  description: string
  departmentLabel: string
  handlerLabel: string
  processTime: string
  progressText: string
}

/**
 * 事件关闭阶段表单模块。
 */
export interface BatchEventProcessingCloseStageModule {
  stage: 'close'
  handleMode: BatchEventProcessingSingleSelectField
  handleReason: BatchEventProcessingSingleSelectField
  handler: BatchEventProcessingSingleSelectField
  description: BatchEventProcessingTextareaField
  taskTable: BatchEventProcessingCloseTaskItem[]
}

/**
 * 处理进度表单模块联合类型。
 */
export type BatchEventProcessingEventHandleModule =
  | BatchEventProcessingApproveStageModule
  | BatchEventProcessingConfirmStageModule
  | BatchEventProcessingHandleStageModuleConfig
  | BatchEventProcessingCloseStageModule

/**
 * 关闭事件弹窗配置。
 */
export interface BatchEventProcessingCloseDialogModule {
  title: string
  closeReason: BatchEventProcessingSingleSelectField
  description: BatchEventProcessingTextareaField
  cancelText: string
  confirmText: string
  successMessage: string
}

/**
 * 驳回事件弹窗配置。
 */
export interface BatchEventProcessingRejectDialogModule {
  title: string
  rejectReason: BatchEventProcessingSingleSelectField
  description: BatchEventProcessingTextareaField
  cancelText: string
  confirmText: string
  successMessage: string
}

/**
 * 二次确认弹窗配置。
 */
export interface BatchEventProcessingConfirmDialogModule {
  title: string
  content: string
  cancelText: string
  confirmText: string
  successMessage: string
}

/**
 * VOC 任务新建/编辑弹窗配置。
 */
export interface BatchEventProcessingVocTaskDialogModule {
  title: string
  cancelText: string
  confirmText: string
  successMessage: string
}

/**
 * VOC 任务转派类弹窗配置。
 */
export interface BatchEventProcessingVocTransferDialogModule {
  title: string
  handler: BatchEventProcessingSingleSelectField
  cancelText: string
  confirmText: string
  successMessage: string
}

/**
 * VOC 更新进度弹窗配置。
 */
export interface BatchEventProcessingVocUpdateProgressDialogModule {
  title: string
  progress: BatchEventProcessingSingleSelectField
  description: BatchEventProcessingTextareaField
  cancelText: string
  confirmText: string
  successMessage: string
}

/**
 * 闭环阶段确认类弹窗配置。
 */
export interface BatchEventProcessingHandleConfirmDialogModule {
  title: string
  content: string
  cancelText: string
  confirmText: string
  successMessage: string
}

/**
 * VOC 系统闭环弹窗集合。
 */
export interface BatchEventProcessingVocDialogModules {
  createTask: BatchEventProcessingVocTaskDialogModule
  editTask: BatchEventProcessingVocTaskDialogModule
  transferTask: BatchEventProcessingVocTransferDialogModule
  transferHandler: BatchEventProcessingVocTransferDialogModule
  updateProgress: BatchEventProcessingVocUpdateProgressDialogModule
  deleteTask: BatchEventProcessingHandleConfirmDialogModule
  closeEvent: BatchEventProcessingHandleConfirmDialogModule
}

/**
 * 闭环阶段弹窗集合。
 */
export interface BatchEventProcessingHandleDialogModules {
  vocLoop: BatchEventProcessingVocDialogModules
  swordLoop: {
    closeEvent: BatchEventProcessingHandleConfirmDialogModule
  }
}

/**
 * 底部操作按钮配置。
 */
export interface BatchEventProcessingFooterAction {
  type: BatchEventProcessingFooterActionType
  label: string
  variant: BatchEventProcessingFooterActionVariant
}

/**
 * 抄送人员表格项。
 */
export interface BatchEventProcessingCcPersonnelItem {
  secondaryDepartment: string
  tertiaryDepartment: string
  userName: string
  employeeId: string
}

/**
 * 批量事件处理进度页配置。
 */
export interface BatchEventProcessingConfig {
  stepDefinitions: BatchEventProcessingStepDefinition[]
  stageModules: Record<BatchEventProcessingStageKey, BatchEventProcessingEventHandleModule>
  footerActions: Record<BatchEventProcessingStageKey, BatchEventProcessingFooterAction[]>
  confirmDialogModules: Record<
    BatchEventProcessingConfirmActionType,
    BatchEventProcessingConfirmDialogModule
  >
  handleDialogModules: BatchEventProcessingHandleDialogModules
  closeDialogModule: BatchEventProcessingCloseDialogModule
  rejectDialogModule: BatchEventProcessingRejectDialogModule
}
