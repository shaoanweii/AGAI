import type { BatchEventConditionsVo } from '@/api/batchEvent/types'

/**
 * 批量事件页当前实际使用的筛选表单字段。
 * - 只保留页面中仍然存在，或被当前未定条件临时复用的字段。
 * - 已从页面删除且没有任何占位复用的旧字段，在这里一并清理掉。
 */
export interface CustomerDirectEngageFilterFormData {
  warningPeriods: string[]
  warningEventNo?: string
  eventName?: string
  brandCodes: string[]
  reviewUserName?: string
  taskStatuses: string[]
  subjectCategoryIds: string[]
  eventPriorities: string[]
  carSeriesCodes: string[]
  intentionList: string[]
  mainRespOrgId: string[]
  collaborateOrgIds: string[]
  handlerUserIds: string[]
  isRejectedList: string[]
  rejectReasonList: string[]
  eventValidityList: string[]
  eventAttributeList: string[]
  firstCodeTag: string[]
  secondCodeTag: string[]
  threeCodeTag: string[]
  fourCodeTag: string[]
  topicList: string[]
  createUser?: string
}

/**
 * 创建批量事件页筛选表单默认值。
 * - 仅初始化当前仍有效的字段，避免重置时把已删除条件重新带回请求参数。
 */
export const createCustomerDirectEngageFilterFormData = (): CustomerDirectEngageFilterFormData => ({
  warningPeriods: [],
  warningEventNo: undefined,
  eventName: undefined,
  brandCodes: [],
  reviewUserName: undefined,
  taskStatuses: [],
  subjectCategoryIds: [],
  eventPriorities: [],
  carSeriesCodes: [],
  intentionList: [],
  mainRespOrgId: [],
  collaborateOrgIds: [],
  handlerUserIds: [],
  isRejectedList: [],
  rejectReasonList: [],
  eventValidityList: [],
  eventAttributeList: [],
  firstCodeTag: [],
  secondCodeTag: [],
  threeCodeTag: [],
  fourCodeTag: [],
  topicList: [],
  createUser: undefined
})

/**
 * 批量事件页批量操作类型。
 */
export enum BatchEventActionTypeEnum {
  Audit = 'audit',
  Response = 'response',
  Close = 'close'
}

/**
 * 批量审核弹窗内的子操作类型。
 */
export enum BatchEventAuditModeEnum {
  Pass = 'pass',
  Close = 'close'
}

/**
 * 批量响应弹窗内的子操作类型。
 */
export enum BatchEventResponseModeEnum {
  Confirm = 'confirm',
  Reject = 'reject'
}

/**
 * 批量响应中的处理方式。
 */
export enum BatchEventResponseHandleModeEnum {
  Voc = 'VOC',
  Sword = 'ZJZ'
}

/**
 * 批量事件页表格行最小类型。
 * 仅声明当前批量操作组件会读取到的字段，其他字段继续按原始后端结构透传。
 */
export interface BatchEventActionRow extends Record<string, any> {
  id?: string
  mainRespOrgId?: string
  mainRespOrgName?: string
  primaryDepId?: string
  primaryDepName?: string
  mainRespUserId?: string
  mainRespUserName?: string
  mainRespUserEmpNo?: string
  processedUserName?: string
  processedUserEmpNo?: string
  userType?: string
  userTypeName?: string
  sceneName?: string
  topic?: string
  topicName?: string
  topicText?: string
}

/**
 * 批量审核弹窗表单。
 */
export interface BatchEventBatchAuditFormData {
  auditMode: BatchEventAuditModeEnum
  businessOwnerUserId: string
  auditCloseReason: string
  description: string
}

/**
 * 批量响应弹窗表单。
 */
export interface BatchEventBatchResponseFormData {
  responseMode: BatchEventResponseModeEnum
  responseHandleMode: BatchEventResponseHandleModeEnum
  responseMainRespUserId: string
  responseUserType: string[]
  responseCarScene: string[]
  responseFocusTopicValues: string[]
  responseRejectReason: string
  description: string
}

/**
 * 创建批量审核弹窗默认值。
 */
export const createBatchEventBatchAuditFormData = (): BatchEventBatchAuditFormData => ({
  auditMode: BatchEventAuditModeEnum.Pass,
  businessOwnerUserId: '',
  auditCloseReason: '',
  description: ''
})

/**
 * 创建批量响应弹窗默认值。
 */
export const createBatchEventBatchResponseFormData = (): BatchEventBatchResponseFormData => ({
  responseMode: BatchEventResponseModeEnum.Confirm,
  responseHandleMode: BatchEventResponseHandleModeEnum.Voc,
  responseMainRespUserId: '',
  responseUserType: [],
  responseCarScene: [],
  responseFocusTopicValues: [],
  responseRejectReason: '',
  description: ''
})

/**
 * 批量审核弹窗回传数据。
 */
export interface BatchEventBatchAuditPayload {
  actionType: BatchEventActionTypeEnum.Audit
  mode: BatchEventAuditModeEnum
  selectedIds: string[]
  formData: BatchEventBatchAuditFormData
}

/**
 * 批量响应弹窗回传数据。
 */
export interface BatchEventBatchResponsePayload {
  actionType: BatchEventActionTypeEnum.Response
  mode: BatchEventResponseModeEnum
  selectedIds: string[]
  formData: BatchEventBatchResponseFormData
  responseConditions: BatchEventConditionsVo
}

/**
 * 批量关闭弹窗回传数据。
 */
export interface BatchEventBatchClosePayload {
  actionType: BatchEventActionTypeEnum.Close
  mode: BatchEventActionTypeEnum.Close
  selectedIds: string[]
  formData: {
    closeReason: string
    description: string
  }
}

/**
 * 页面批量操作统一回传类型。
 */
export type BatchEventBatchActionPayload =
  | BatchEventBatchAuditPayload
  | BatchEventBatchResponsePayload
  | BatchEventBatchClosePayload
