import http from '@/api/http'
import type {
  CategoryInfo,
  PageInfoSingleEventPageVo,
  SingleEventApproveModel,
  SingleEventAssignModel,
  SingleEventBatchApproveModel,
  SingleEventBatchAssignModel,
  SingleEventBatchCloseModel,
  // SingleEventBatchConfirmModel,
  SingleEventBatchConfirmRejectModel,
  SingleEventCloseModel,
  SingleEventConfirmModel,
  SingleEventConfirmRejectModel,
  SingleEventDetailBaseUpdateModel,
  SingleEventDetailBaseVo,
  SingleEventDetailVo,
  SingleEventHandleModel,
  SingleEventQueryDetailModel,
  SingleEventQueryModel
} from './types'

/**
 * @description: 查询单点事件公共条件
 * @param {any} data
 * @return {*}
 */
export const conditions = (data?: any): Promise<BaseResponse<any>> => {
  return http.post('/report/single-event/conditions', data)
}

/**
 * @description: 查询单点事件列表
 * @param {any} data
 * @return {*}
 */
export const getList = (
  data?: SingleEventQueryModel
): Promise<BaseResponse<PageInfoSingleEventPageVo>> => {
  return http.post('/report/single-event/list', data)
}

/**
 * 创建单点事件异步导出任务。
 * @param data 与单点事件列表查询一致的筛选参数
 * @return 导出任务创建结果
 */
export const exportSingleEvent = (data?: SingleEventQueryModel): Promise<BaseResponse<any>> => {
  return http.post('/report/single-event/export-event', data)
}

/**
 * @description: 更新单点事件
 * @return {*}
 */
export const updateOriginalSoundDetail = (
  data?: SingleEventDetailBaseUpdateModel
): Promise<BaseResponse<any>> => {
  return http.post('/report/single-event/update-detail-base', data)
}

/**
 * @description: 查询单点事件原声信息
 * @return {*}
 */
export const getDetailBase = (
  data?: SingleEventQueryDetailModel
): Promise<BaseResponse<SingleEventDetailBaseVo>> => {
  return http.post('/report/single-event/get-detail-base', data)
}

/**
 * @description: 批量确认
 * @return {*}
 */
export const batchConfirm = (
  data?: SingleEventBatchConfirmRejectModel
): Promise<BaseResponse<any>> => {
  return http.post('/report/single-event/batch-confirm', data)
}

/**
 * @description: 批量关闭
 * @return {*}
 */
export const batchCloseApi = (data?: SingleEventBatchCloseModel): Promise<BaseResponse<any>> => {
  return http.post('/report/single-event/batch-close', data)
}

/**
 * @description: 批量分派
 * @return {*}
 */
export const batchAssign = (data?: SingleEventBatchAssignModel): Promise<BaseResponse<any>> => {
  return http.post('/report/single-event/batch-assign', data)
}

/**
 * @description: 批量审核
 * @param {SingleEventBatchApproveModel} data
 * @return {*}
 */
export const batchApprove = (data?: SingleEventBatchApproveModel): Promise<BaseResponse<any>> => {
  return http.post('/report/single-event/batch-approve', data)
}

/**
 * @description: 批量操作-确认驳回
 * @param {SingleEventBatchConfirmRejectModel} data
 * @return {*}
 */
export const batchConfirmReject = (
  data?: SingleEventBatchConfirmRejectModel
): Promise<BaseResponse<any>> => {
  return http.post('/report/single-event/batch-confirm-reject', data)
}

/**
 * @description: 查询单点事件详细信息
 * @param {SingleEventQueryDetailModel} data
 * @return {*}
 */
export const getDetailEvent = (
  data?: SingleEventQueryDetailModel
): Promise<BaseResponse<SingleEventDetailVo>> => {
  return http.post('/report/single-event/get-detail-event', data)
}

/**
 * @description: 查询单点事件详细信息(数组)
 * @param {SingleEventQueryDetailModel} data
 * @return {*}
 */
export const getDetailEvents = (
  data?: SingleEventQueryDetailModel
): Promise<BaseResponse<SingleEventDetailVo[]>> => {
  return http.post('/report/single-event/get-detail-events', data)
}

/**
 * @description: 审核
 * @param {SingleEventQueryDetailModel} data
 * @return {*}
 */
export const approve = (data?: SingleEventApproveModel): Promise<BaseResponse<any>> => {
  return http.post('/report/single-event/approve', data)
}

/**
 * @description: 分派
 * @param {SingleEventAssignModel} data
 * @return {*}
 */
export const assign = (data?: SingleEventAssignModel): Promise<BaseResponse<any>> => {
  return http.post('/report/single-event/assign', data)
}

/**
 * @description: 关闭&审核关闭
 * @param {SingleEventCloseModel} data
 * @return {*}
 */
export const closeApi = (data?: SingleEventCloseModel): Promise<BaseResponse<any>> => {
  return http.post('/report/single-event/close', data)
}

/**
 * @description: 确认
 * @param {SingleEventConfirmModel} data
 * @return {*}
 */
export const confirm = (data?: SingleEventConfirmModel): Promise<BaseResponse<any>> => {
  return http.post('/report/single-event/confirm', data)
}

/**
 * @description: 处理完成
 * @param {SingleEventHandleModel} data
 * @return {*}
 */
export const handleComplete = (data?: SingleEventHandleModel): Promise<BaseResponse<any>> => {
  return http.post('/report/single-event/handle-complete', data)
}

/**
 * @description: 保存处理结果
 * @param {SingleEventHandleModel} data
 * @return {*}
 */
export const handleSave = (data?: SingleEventHandleModel): Promise<BaseResponse<any>> => {
  return http.post('/report/single-event/handle-save', data)
}

/**
 * @description: 确认驳回
 * @param {SingleEventConfirmRejectModel} data
 * @return {*}
 */
export const rejectApi = (data?: SingleEventConfirmRejectModel): Promise<BaseResponse<any>> => {
  return http.post('/report/single-event/reject', data)
}

/**
 * @description: 通过标签Code查询观点列表
 * @param {string[]} tagParentCode
 * @return {*}
 */
export const getTopicsByTagId = (data?: string[]): Promise<BaseResponse<any>> => {
  // return http.post('/report/single-event/topics', null, { params: { tagParentCode } })
  return http.post('/report/single-event/topics', data)
}

/**
 * @description: 通过标签名称查询标签层级信息
 * @param {string} tagName 标签名称
 * @return {*}
 */
export const findAllUpTagLibHierarchicalByTagId = (
  tagName?: string
): Promise<BaseResponse<CategoryInfo[]>> => {
  return http.post('/report/single-event/findAllUpTagLibHierarchicalByTagId', { tagName })
}

/**
 * @description: 更新意图标签观点
 * @return {*}
 */
export const updateTagBase = (data: {
  intentions: any[]
  id: string
}): Promise<BaseResponse<CategoryInfo[]>> => {
  return http.post('/report/single-event/update-tag-base', data)
}
