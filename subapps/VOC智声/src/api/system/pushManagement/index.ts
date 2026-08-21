import request from '@/api/http'
import type {
  PushTaskDetailParams,
  PushTaskDetailResult,
  PushMessageCheckUploadParams,
  PushMessageCheckUploadResult,
  PushMessageUserInfoExportParams,
  PushMessageUserInfoPageResult,
  PushMessageUserInfoQueryParams,
  PushMessageUploadResult,
  SaveUploadPushMessageParams,
  PushTaskDeleteParams,
  PushTaskPageResult,
  PushTaskQueryParams
} from './types'

/**
 * 获取推送管理分页列表。
 */
export const getPushTaskPage = (
  params: PushTaskQueryParams
): Promise<BaseResponse<PushTaskPageResult>> => {
  return request.post('/report/pushMessage/pageMessageData', params)
}

/**
 * 获取推送任务用户明细分页列表。
 */
export const getPushMessageUserInfoPage = (
  params: PushMessageUserInfoQueryParams
): Promise<BaseResponse<PushMessageUserInfoPageResult>> => {
  return request.post('/report/pushMessage/pageMessageUserInfo', params)
}

/**
 * 导出推送任务用户明细。
 */
export const exportPushMessageUserInfo = (
  params: PushMessageUserInfoExportParams
): Promise<BaseResponse<any>> => {
  return request.post('/report/pushMessage/exportMessageUserInfo', params)
}

/**
 * 下载推送用户模板。
 */
export const downloadPushMessageTemplate = (): Promise<BaseResponse<Blob>> => {
  return request.get('/report/pushMessage/downloadMessage', {
    responseType: 'blob'
  })
}

/**
 * 上传推送用户数据。
 */
export const uploadPushMessageData = (
  formData: FormData
): Promise<BaseResponse<PushMessageUploadResult>> => {
  return request.post('/report/pushMessage/uploadMessage', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 校验已上传的推送用户数据。
 */
export const checkUploadPushMessage = (
  params: PushMessageCheckUploadParams
): Promise<BaseResponse<PushMessageCheckUploadResult>> => {
  return request.post('/report/pushMessage/checkUploadMessage', params)
}

/**
 * 保存上传推送消息任务。
 */
export const saveUploadPushMessage = (
  params: SaveUploadPushMessageParams
): Promise<BaseResponse<any>> => {
  return request.post('/report/pushMessage/saveUploadDataMessage', params)
}

/**
 * 删除未开始的推送任务。
 */
export const deletePushTask = (params: PushTaskDeleteParams): Promise<BaseResponse<boolean>> => {
  return request.post('/report/pushMessage/deleteMessageTask', params)
}
