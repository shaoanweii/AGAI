import { TOKEN_KEY } from '@/constants'
import http from '@/api/http'
import type {
  BatchEventApproveModel,
  BatchEventCreateModel,
  BatchEventDashboardQueryModel,
  BatchEventBatchApproveModel,
  BatchEventBatchCloseModel,
  BatchEventBatchConfirmModel,
  BatchEventBatchRejectModel,
  BatchEventBriefDetailVo,
  BatchEventBriefQueryModel,
  BatchEventCarSeriesStatVo,
  BatchEventCcModel,
  BatchEventCcUserVo,
  BatchEventChannelStatVo,
  BatchEventCloseModel,
  BatchEventCommonModel,
  BatchEventConditionsQueryModel,
  BatchEventConditionsVo,
  BatchEventConfirmModel,
  BatchEventDataStatVo,
  BatchEventExecutorCallbackModel,
  BatchEventHandleCompleteModel,
  BatchEventListSoundsQueryModel,
  BatchEventOpeLogVo,
  BatchEventOpinionStatVo,
  BatchEventPermissionModel,
  BatchEventPermissionVo,
  BatchEventProvinceStatVo,
  BatchEventQueryModel,
  BatchEventReassignModel,
  BatchEventRejectModel,
  BatchEventReportSummaryVo,
  BatchEventRuleCategoryVo,
  BatchEventSceneQueryModel,
  BatchEventSceneStatVo,
  BatchEventTaskCreateModel,
  BatchEventTaskDeleteModel,
  BatchEventTaskEditModel,
  BatchEventTaskProgressModel,
  BatchEventTaskReassignModel,
  BatchEventTaskVo,
  DashboardEventItem,
  DashboardStatCard,
  BatchEventTrendStatVo,
  PageInfoVocListOriginalContentVo,
  PageInfoBatchRiskEventPageVo
} from './types'

const BATCH_EVENT_BASE_URL = '/report/batch-event'

type BatchEventStreamRequest<T extends object> = T & {
  signal?: AbortSignal
}

/**
 * 批量事件流式 POST 请求。
 * 报告解读接口返回 text/event-stream 风格文本，不能经过 axios 的 JSON code 拦截器。
 * @param url 接口路径
 * @param data 请求体；signal 会透传给 fetch，不写入业务参数
 * @returns 原生 Response，用于 ReportSummary 读取流式内容
 */
const batchEventStreamPost = async <T extends object>(
  url: string,
  data: BatchEventStreamRequest<T>
): Promise<Response> => {
  const token = localStorage.getItem(TOKEN_KEY)
  const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'
  const { signal, ...requestData } = data || {}

  const response = await fetch(`${baseURL}${url}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
      Accept: 'text/event-stream',
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    body: JSON.stringify(requestData),
    signal
  })

  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`)
  }

  return response
}

/**
 * 查询批量事件列表。
 * @param data 列表查询参数
 * @returns 批量事件分页列表
 */
export const getBatchEventList = (
  data?: BatchEventQueryModel
): Promise<BaseResponse<PageInfoBatchRiskEventPageVo>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/batchEventList`, data)
}

/**
 * 查询批量事件仪表盘统计卡片。
 * @param data 仪表盘查询参数
 * @returns 各状态统计卡片
 */
export const getBatchEventDashboardStatCards = (
  data: BatchEventDashboardQueryModel
): Promise<BaseResponse<DashboardStatCard[]>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/dashboard/stat-cards`, data)
}

/**
 * 查询批量事件仪表盘事件列表。
 * @param data 仪表盘查询参数
 * @returns 事件列表
 */
export const getBatchEventDashboardEventList = (
  data: BatchEventDashboardQueryModel
): Promise<BaseResponse<DashboardEventItem[]>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/dashboard/event-list`, data)
}

/**
 * 查询批量事件筛选条件。
 * @param data 事件 ID 参数，批量场景用英文逗号拼接多个 ID
 * @returns 批量事件专属筛选条件
 */
export const getBatchEventConditions = (
  data: BatchEventConditionsQueryModel
): Promise<BaseResponse<BatchEventConditionsVo>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/conditions`, data)
}

/**
 * 查询批量事件规则主题分类树。
 * @returns 批量事件规则分类树
 */
export const getBatchEventRuleCategoryTree = (): Promise<
  BaseResponse<BatchEventRuleCategoryVo[]>
> => {
  return http.get(`${BATCH_EVENT_BASE_URL}/batch-rule-category`)
}

/**
 * 初始化批量事件预警状态。
 * @returns 操作结果
 */
export const initBatchWarning = (): Promise<BaseResponse<number>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/init-batch-warning`)
}

/**
 * 查询事件简报。
 * @param data 事件 ID 参数
 * @returns 事件简报
 */
export const getBatchEventBrief = (
  data?: BatchEventBriefQueryModel
): Promise<BaseResponse<BatchEventBriefDetailVo>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/brief`, data)
}

/**
 * 查询事件统计指标。
 * @param data 事件 ID 参数
 * @returns 负面率、正面率、提及量、用户数统计
 */
export const getBatchEventDataStat = (
  data?: BatchEventBriefQueryModel
): Promise<BaseResponse<BatchEventDataStatVo>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/data-stat`, data)
}

/**
 * 查询事件趋势统计。
 * @param data 事件 ID 参数
 * @returns 正中负提及量趋势
 */
export const getBatchEventTrendStat = (
  data?: BatchEventBriefQueryModel
): Promise<BaseResponse<BatchEventTrendStatVo>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/trend-stat`, data)
}

/**
 * 查询车系分布。
 * @param data 事件 ID 参数
 * @returns 车系提及量排行
 */
export const getBatchEventCarSeriesStat = (
  data?: BatchEventBriefQueryModel
): Promise<BaseResponse<BatchEventCarSeriesStatVo>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/car-series-stat`, data)
}

/**
 * 查询聚焦场景。
 * @param data 事件 ID 与车系标识
 * @returns 当前车系的场景分布
 */
export const getBatchEventSceneStat = (
  data?: BatchEventSceneQueryModel
): Promise<BaseResponse<BatchEventSceneStatVo>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/scene-stat`, data)
}

/**
 * 查询评价观点。
 * @param data 事件 ID 与车系标识
 * @returns 当前车系的观点 TOP
 */
export const getBatchEventOpinionStat = (
  data?: BatchEventSceneQueryModel
): Promise<BaseResponse<BatchEventOpinionStatVo>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/opinion-stat`, data)
}

/**
 * 查询省份分布。
 * @param data 事件 ID 与车系标识
 * @returns 当前车系的省份分布
 */
export const getBatchEventProvinceStat = (
  data?: BatchEventSceneQueryModel
): Promise<BaseResponse<BatchEventProvinceStatVo>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/province-stat`, data)
}

/**
 * 查询渠道分布。
 * @param data 事件 ID 与车系标识
 * @returns 当前车系的渠道分布
 */
export const getBatchEventChannelStat = (
  data?: BatchEventSceneQueryModel
): Promise<BaseResponse<BatchEventChannelStatVo>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/channel-stat`, data)
}

/**
 * 查询报告解读。
 * @param data 事件 ID 参数
 * @returns 模型总结文本
 */
export const getBatchEventReportSummary = (
  data?: BatchEventBriefQueryModel
): Promise<BaseResponse<BatchEventReportSummaryVo>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/report-summary`, data)
}

/**
 * 查询报告解读流。
 * @param data 事件 ID 参数
 * @returns 原生流式响应
 */
export const getBatchEventReportSummaryStream = (
  data: BatchEventStreamRequest<BatchEventBriefQueryModel>
): Promise<Response> => {
  return batchEventStreamPost(`${BATCH_EVENT_BASE_URL}/report-summary`, data)
}

/**
 * 查询批量事件详情客户原声列表。
 * @param data 客户原声列表查询参数
 * @returns 客户原声分页列表
 */
export const getBatchEventListSounds = (
  data?: BatchEventListSoundsQueryModel
): Promise<BaseResponse<PageInfoVocListOriginalContentVo>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/getBatchEventListSounds`, data)
}

/**
 * 获取当前处理状态。
 * @param data 事件 ID 参数
 * @returns 后端当前状态码
 */
export const initBatchEvent = (data?: BatchEventBriefQueryModel): Promise<BaseResponse<string>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/init`, data)
}

/**
 * 查询当前用户在指定批量事件中的功能权限。
 * @param data 事件 ID 参数
 * @returns 批量事件功能权限
 */
export const getBatchEventPermission = (
  data: BatchEventPermissionModel
): Promise<BaseResponse<BatchEventPermissionVo>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/permission`, data)
}

/**
 * 查询处理进度操作记录。
 * @param data 事件 ID 参数
 * @returns 操作记录列表
 */
export const getBatchEventOperationLogs = (
  data?: BatchEventBriefQueryModel
): Promise<BaseResponse<BatchEventOpeLogVo[]>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/ope-log-list`, data)
}

/**
 * 查询抄送人列表。
 * @param data 事件 ID 参数
 * @returns 抄送人列表
 */
export const getBatchEventCcUserList = (
  data?: BatchEventBriefQueryModel
): Promise<BaseResponse<BatchEventCcUserVo[]>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/cc-user-list`, data)
}

/**
 * 查询批量事件任务列表。
 * @param data 事件 ID 参数
 * @returns 批量事件闭环任务列表
 */
export const getBatchEventTaskList = (
  data?: BatchEventBriefQueryModel
): Promise<BaseResponse<BatchEventTaskVo[]>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/task-list`, data)
}

/**
 * 审核通过。
 * @param data 审核参数
 * @returns 操作结果
 */
export const approveBatchEvent = (data?: BatchEventApproveModel): Promise<BaseResponse<number>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/approve`, data)
}

/**
 * 批量审核通过。
 * @param data 批量审核参数
 * @returns 操作结果
 */
export const batchApproveBatchEvent = (
  data?: BatchEventBatchApproveModel
): Promise<BaseResponse<number>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/batch-approve`, data)
}

/**
 * 确认处理。
 * @param data 确认处理参数
 * @returns 操作结果
 */
export const confirmBatchEvent = (data?: BatchEventConfirmModel): Promise<BaseResponse<number>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/confirm`, data)
}

/**
 * 批量确认处理。
 * @param data 批量确认处理参数
 * @returns 操作结果
 */
export const batchConfirmBatchEvent = (
  data?: BatchEventBatchConfirmModel
): Promise<BaseResponse<number>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/batch-confirm`, data)
}

/**
 * 关闭事件。
 * @param data 关闭参数
 * @returns 操作结果
 */
export const closeBatchEvent = (data?: BatchEventCloseModel): Promise<BaseResponse<number>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/close`, data)
}

/**
 * 批量关闭事件。
 * @param data 批量关闭参数
 * @returns 操作结果
 */
export const batchCloseBatchEvent = (
  data?: BatchEventBatchCloseModel
): Promise<BaseResponse<number>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/batch-close`, data)
}

/**
 * 驳回事件。
 * @param data 驳回参数
 * @returns 操作结果
 */
export const rejectBatchEvent = (data?: BatchEventRejectModel): Promise<BaseResponse<number>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/reject`, data)
}

/**
 * 批量驳回事件。
 * @param data 批量驳回参数
 * @returns 操作结果
 */
export const batchRejectBatchEvent = (
  data?: BatchEventBatchRejectModel
): Promise<BaseResponse<number>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/batch-reject`, data)
}

/**
 * 批量事件下发。
 * @param data 批量事件下发参数
 * @returns 新事件 ID
 */
export const createBatchEvent = (
  data?: BatchEventCreateModel
): Promise<BaseResponse<string>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/create-event`, data)
}

/**
 * 导出批量事件。
 * @param data 导出查询参数
 * @returns 导出任务结果
 */
export const exportBatchEvent = (
  data?: BatchEventQueryModel
): Promise<BaseResponse<any>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/export-event`, data)
}

/**
 * 导出批量事件详情客户原声。
 * @param data 事件 ID 参数
 * @returns 导出任务创建结果
 */
export const exportBatchEventDetail = (
  data?: BatchEventCommonModel
): Promise<BaseResponse<any>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/export-event-detail`, data)
}

/**
 * 导出批量事件详情原始数据客户原声。
 * @param data 事件 ID 参数
 * @returns 导出任务创建结果
 */
export const exportBatchEventRaw = (
  data?: BatchEventCommonModel
): Promise<BaseResponse<any>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/export-event-raw`, data)
}

/**
 * 执剑者回调。
 * @param data 执剑者回调参数
 * @returns 操作结果
 */
export const executorCallbackBatchEvent = (
  data?: BatchEventExecutorCallbackModel
): Promise<BaseResponse<number>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/executor-callback`, data)
}

/**
 * 添加抄送人。
 * @param data 抄送人参数
 * @returns 操作结果
 */
export const ccBatchEvent = (data?: BatchEventCcModel): Promise<BaseResponse<number>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/cc`, data)
}

/**
 * 转派处理人。
 * @param data 转派参数
 * @returns 操作结果
 */
export const reassignBatchEvent = (
  data?: BatchEventReassignModel
): Promise<BaseResponse<number>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/reassign`, data)
}

/**
 * 新建批量事件闭环任务。
 * @param data 新建任务参数
 * @returns 新任务 ID
 */
export const createBatchEventTask = (
  data?: BatchEventTaskCreateModel
): Promise<BaseResponse<number>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/create-task`, data)
}

/**
 * 编辑批量事件闭环任务。
 * @param data 编辑任务参数
 * @returns 操作结果
 */
export const editBatchEventTask = (
  data?: BatchEventTaskEditModel
): Promise<BaseResponse<number>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/edit-task`, data)
}

/**
 * 更新批量事件闭环任务进度。
 * @param data 任务进度参数
 * @returns 操作结果
 */
export const updateBatchEventTaskProgress = (
  data?: BatchEventTaskProgressModel
): Promise<BaseResponse<number>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/update-task-progress`, data)
}

/**
 * 删除批量事件闭环任务。
 * @param data 删除任务参数
 * @returns 操作结果
 */
export const deleteBatchEventTask = (
  data?: BatchEventTaskDeleteModel
): Promise<BaseResponse<number>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/delete-task`, data)
}

/**
 * 转派批量事件闭环任务。
 * @param data 转派任务参数
 * @returns 操作结果
 */
export const reassignBatchEventTask = (
  data?: BatchEventTaskReassignModel
): Promise<BaseResponse<number>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/reassign-task`, data)
}

/**
 * 处理完成。
 * @param data 处理完成参数
 * @returns 操作结果
 */
export const handleCompleteBatchEvent = (
  data?: BatchEventHandleCompleteModel
): Promise<BaseResponse<number>> => {
  return http.post(`${BATCH_EVENT_BASE_URL}/handle-complete`, data)
}
