import request from '@h5/api/http'
import type { HttpRequestConfig } from '@h5/api/http'
import { TOKEN_KEY } from '@/constants'
import type {
  BatchEventApproveModel,
  BatchEventBatchCloseModel,
  BatchEventBatchCommonModel,
  BatchEventBatchRejectModel,
  BatchEventBriefDetailVo,
  BatchEventCcModel,
  BatchEventCcUserVo,
  BatchEventCarSeriesStatVo,
  BatchEventChannelStatVo,
  BatchEventCloseModel,
  BatchEventCommonModel,
  BatchEventConfirmModel,
  BatchEventConditionsVo,
  BatchEventCreateModel,
  BatchEventExecutorCallbackModel,
  BatchEventHandleCompleteModel,
  BatchEventUserVoiceListQuery,
  BatchEventPageRequestParams,
  BatchEventDataStatVo,
  BatchEventOpinionStatVo,
  BatchEventPermissionModel,
  BatchEventPermissionVo,
  BatchEventQueryModel,
  BatchEventProvinceStatVo,
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
  BatchEventTrendStatVo,
  BatchEventOpeLogVo,
  PageInfoBatchEventMobilePageVo,
  PageInfoBatchEventUserVoiceVo,
  TaskEventNewlyVo,
  TaskEventStatusDistributionVo,
  TaskEventTrendVo
} from './types'

const BATCH_EVENT_BASE_URL = '/report/mobileTerminal/batch-event'
const BATCH_EVENT_REPORT_BASE_URL = '/report/batch-event'

const FRONTEND_OMIT_BATCH_EVENT_QUERY_KEYS = [
  'isSuperRole',
  'currentOrgIds',
  'currentUserId',
  'dateUnit',
  'typeR',
  'scaleMagnitude',
  'isMobile'
]

type BatchEventStreamRequest<T extends object> = T & {
  signal?: AbortSignal
}

/**
 * H5 批量事件流式 POST 请求。
 * 报告解读接口返回 text/event-stream 风格文本，不能经过 axios 的 JSON code 拦截器。
 * @param url 接口路径
 * @param data 请求体；signal 会透传给 fetch，不写入业务参数
 * @returns 原生 Response，用于调用方读取 ReadableStream
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
 * 将筛选值规范为字符串数组，用于兼容 H5 旧单值字段和新版列表字段。
 * @param value 表单筛选值
 * @returns 过滤空值后的字符串数组
 */
const toStringList = (value: unknown): string[] => {
  const list = Array.isArray(value) ? value : value === undefined || value === null ? [] : [value]
  return list
    .filter(item => item !== undefined && item !== null && String(item) !== '')
    .map(item => String(item))
}

/**
 * 将 H5 批量事件页面筛选参数转换为 swagger 中的 BatchEventQueryModel。
 * @param params 页面公共筛选和批量高级筛选的合并参数
 * @returns 批量事件主查询接口入参
 */
export const normalizeBatchEventQueryParams = (
  params: BatchEventPageRequestParams = {}
): BatchEventQueryModel => {
  const { brandCode, eventValidity, topics, handlerIds, ...restParams } = params

  const query: BatchEventQueryModel = {
    ...restParams
  }

  FRONTEND_OMIT_BATCH_EVENT_QUERY_KEYS.forEach(key => {
    delete query[key]
  })

  const brandCodes = Array.isArray(params.brandCodes) ? params.brandCodes.filter(Boolean) : []
  if (brandCodes.length > 0) {
    query.brandCodes = brandCodes.map(String)
  } else if (brandCode) {
    query.brandCodes = [String(brandCode)]
  }

  const eventValidityList = toStringList(query.eventValidityList)
  if (eventValidityList.length > 0) {
    query.eventValidityList = eventValidityList
  } else {
    const legacyEventValidityList = toStringList(eventValidity)
    if (legacyEventValidityList.length > 0) {
      query.eventValidityList = legacyEventValidityList
    }
  }

  const topicList = toStringList(query.topicList)
  if (topicList.length > 0) {
    query.topicList = topicList
  } else {
    const legacyTopicList = toStringList(topics)
    if (legacyTopicList.length > 0) {
      query.topicList = legacyTopicList
    }
  }

  const handlerUserIds = toStringList(query.handlerUserIds)
  if (handlerUserIds.length > 0) {
    query.handlerUserIds = handlerUserIds
  } else {
    const legacyHandlerUserIds = toStringList(handlerIds)
    if (legacyHandlerUserIds.length > 0) {
      query.handlerUserIds = legacyHandlerUserIds
    }
  }

  return query
}

/**
 * 获取新增事件统计（H5 批量事件）。
 * POST /mobileTerminal/batch-event/newly-event-statistics
 */
export const getBatchNewlyEventStatistics = (
  data: BatchEventQueryModel
): Promise<BaseResponse<TaskEventNewlyVo>> => {
  return request.post(`${BATCH_EVENT_BASE_URL}/newly-event-statistics`, data)
}

/**
 * 获取事件状态分布（H5 批量事件）。
 * POST /mobileTerminal/batch-event/event-status-distribution
 */
export const getBatchEventStatusDistribution = (
  data: BatchEventQueryModel
): Promise<BaseResponse<TaskEventStatusDistributionVo[]>> => {
  return request.post(`${BATCH_EVENT_BASE_URL}/event-status-distribution`, data)
}

/**
 * 获取事件趋势（H5 批量事件）。
 * POST /mobileTerminal/batch-event/event-trend
 */
export const getBatchEventTrend = (
  data: BatchEventQueryModel
): Promise<BaseResponse<TaskEventTrendVo[]>> => {
  return request.post(`${BATCH_EVENT_BASE_URL}/event-trend`, data)
}

/**
 * 获取批量事件列表（H5）。
 * POST /mobileTerminal/batch-event/list
 */
export const getMobileBatchEventList = (
  data: BatchEventQueryModel
): Promise<BaseResponse<PageInfoBatchEventMobilePageVo>> => {
  return request.post(`${BATCH_EVENT_BASE_URL}/list`, data)
}

/**
 * 获取批量事件筛选条件（H5）。
 * POST /mobileTerminal/batch-event/conditions
 */
export const getBatchEventConditions = (
  data: BatchEventBatchCommonModel
): Promise<BaseResponse<BatchEventConditionsVo>> => {
  return request.post(`${BATCH_EVENT_BASE_URL}/conditions`, data)
}

/**
 * 获取批量事件规则主题分类树（H5 筛选复用 PC 报表服务接口）。
 * GET /report/batch-event/batch-rule-category
 */
export const getBatchEventRuleCategoryTree = (): Promise<
  BaseResponse<BatchEventRuleCategoryVo[]>
> => {
  return request.get(`${BATCH_EVENT_REPORT_BASE_URL}/batch-rule-category`, {
    cancelPrevious: true
  })
}

/**
 * 创建批量事件（H5 原声事件下发）。
 * POST /mobileTerminal/batch-event/create-event
 */
export const createBatchEvent = (data: BatchEventCreateModel): Promise<BaseResponse<number>> => {
  return request.post(`${BATCH_EVENT_BASE_URL}/create-event`, data)
}

/**
 * 获取事件简报。
 * POST /mobileTerminal/batch-event/brief
 */
export const getBatchEventBrief = (
  data: BatchEventCommonModel
): Promise<BaseResponse<BatchEventBriefDetailVo>> => {
  return request.post(`${BATCH_EVENT_BASE_URL}/brief`, data)
}

/**
 * 获取当前用户在批量事件中的功能权限。
 * POST /report/batch-event/permission
 */
export const getBatchEventPermission = (
  data: BatchEventPermissionModel
): Promise<BaseResponse<BatchEventPermissionVo>> => {
  return request.post(`${BATCH_EVENT_REPORT_BASE_URL}/permission`, data)
}

/**
 * 获取批量事件客户原声列表。
 * POST /mobileTerminal/batch-event/user-voice-list
 */
export const getBatchEventUserVoiceList = (
  data: BatchEventUserVoiceListQuery,
  config?: HttpRequestConfig
): Promise<BaseResponse<PageInfoBatchEventUserVoiceVo>> => {
  return request.post(`${BATCH_EVENT_BASE_URL}/user-voice-list`, data, config)
}

/**
 * 获取事件初始化状态。
 * POST /mobileTerminal/batch-event/init
 */
export const initBatchEvent = (data: BatchEventCommonModel): Promise<BaseResponse<string>> => {
  return request.post(`${BATCH_EVENT_BASE_URL}/init`, data)
}

/**
 * 获取批量事件数据统计。
 * POST /mobileTerminal/batch-event/data-stat
 */
export const getBatchEventDataStat = (
  data: BatchEventCommonModel
): Promise<BaseResponse<BatchEventDataStatVo>> => {
  return request.post(`${BATCH_EVENT_BASE_URL}/data-stat`, data)
}

/**
 * 获取批量事件趋势分析。
 * POST /mobileTerminal/batch-event/trend-stat
 */
export const getBatchEventTrendStat = (
  data: BatchEventCommonModel
): Promise<BaseResponse<BatchEventTrendStatVo>> => {
  return request.post(`${BATCH_EVENT_BASE_URL}/trend-stat`, data)
}

/**
 * 获取批量事件车系分布。
 * POST /mobileTerminal/batch-event/car-series-stat
 */
export const getBatchEventCarSeriesStat = (
  data: BatchEventCommonModel
): Promise<BaseResponse<BatchEventCarSeriesStatVo>> => {
  return request.post(`${BATCH_EVENT_BASE_URL}/car-series-stat`, data)
}

/**
 * 获取批量事件聚焦场景。
 * POST /mobileTerminal/batch-event/scene-stat
 */
export const getBatchEventSceneStat = (
  data: BatchEventSceneQueryModel
): Promise<BaseResponse<BatchEventSceneStatVo>> => {
  return request.post(`${BATCH_EVENT_BASE_URL}/scene-stat`, data)
}

/**
 * 获取批量事件评价观点。
 * POST /mobileTerminal/batch-event/opinion-stat
 */
export const getBatchEventOpinionStat = (
  data: BatchEventSceneQueryModel
): Promise<BaseResponse<BatchEventOpinionStatVo>> => {
  return request.post(`${BATCH_EVENT_BASE_URL}/opinion-stat`, data)
}

/**
 * 获取批量事件省份分布。
 * POST /mobileTerminal/batch-event/province-stat
 */
export const getBatchEventProvinceStat = (
  data: BatchEventSceneQueryModel
): Promise<BaseResponse<BatchEventProvinceStatVo>> => {
  return request.post(`${BATCH_EVENT_BASE_URL}/province-stat`, data)
}

/**
 * 获取批量事件渠道分布。
 * POST /mobileTerminal/batch-event/channel-stat
 */
export const getBatchEventChannelStat = (
  data: BatchEventSceneQueryModel
): Promise<BaseResponse<BatchEventChannelStatVo>> => {
  return request.post(`${BATCH_EVENT_BASE_URL}/channel-stat`, data)
}

/**
 * 获取批量事件报告解读。
 * POST /mobileTerminal/batch-event/report-summary
 */
export const getBatchEventReportSummary = (
  data: BatchEventCommonModel
): Promise<BaseResponse<BatchEventReportSummaryVo>> => {
  return request.post(`${BATCH_EVENT_BASE_URL}/report-summary`, data)
}

/**
 * 获取批量事件报告解读流。
 * POST /mobileTerminal/batch-event/report-summary
 */
export const getBatchEventReportSummaryStream = (
  data: BatchEventStreamRequest<BatchEventCommonModel>
): Promise<Response> => {
  return batchEventStreamPost(`${BATCH_EVENT_BASE_URL}/report-summary`, data)
}

/**
 * 获取事件任务列表。
 * POST /mobileTerminal/batch-event/task-list
 */
export const getBatchEventTaskList = (
  data: BatchEventCommonModel
): Promise<BaseResponse<BatchEventTaskVo[]>> => {
  return request.post(`${BATCH_EVENT_BASE_URL}/task-list`, data)
}

/**
 * 新建批量事件任务。
 * POST /mobileTerminal/batch-event/create-task
 */
export const createBatchEventTask = (
  data: BatchEventTaskCreateModel
): Promise<BaseResponse<number>> => {
  return request.post(`${BATCH_EVENT_BASE_URL}/create-task`, data)
}

/**
 * 编辑批量事件任务。
 * POST /mobileTerminal/batch-event/edit-task
 */
export const editBatchEventTask = (
  data: BatchEventTaskEditModel
): Promise<BaseResponse<number>> => {
  return request.post(`${BATCH_EVENT_BASE_URL}/edit-task`, data)
}

/**
 * 删除批量事件任务。
 * POST /mobileTerminal/batch-event/delete-task
 */
export const deleteBatchEventTask = (
  data: BatchEventTaskDeleteModel
): Promise<BaseResponse<number>> => {
  return request.post(`${BATCH_EVENT_BASE_URL}/delete-task`, data)
}

/**
 * 转派批量事件任务。
 * POST /mobileTerminal/batch-event/reassign-task
 */
export const reassignBatchEventTask = (
  data: BatchEventTaskReassignModel
): Promise<BaseResponse<number>> => {
  return request.post(`${BATCH_EVENT_BASE_URL}/reassign-task`, data)
}

/**
 * 更新批量事件任务进度。
 * POST /mobileTerminal/batch-event/update-task-progress
 */
export const updateBatchEventTaskProgress = (
  data: BatchEventTaskProgressModel
): Promise<BaseResponse<number>> => {
  return request.post(`${BATCH_EVENT_BASE_URL}/update-task-progress`, data)
}

/**
 * 批量事件驳回。
 * POST /mobileTerminal/batch-event/reject
 */
export const rejectBatchEvent = (data: BatchEventRejectModel): Promise<BaseResponse<number>> => {
  return request.post(`${BATCH_EVENT_BASE_URL}/reject`, data)
}

/**
 * 批量驳回批量事件。
 * POST /mobileTerminal/batch-event/batch-reject
 */
export const batchRejectBatchEvent = (
  data: BatchEventBatchRejectModel
): Promise<BaseResponse<number>> => {
  return request.post(`${BATCH_EVENT_BASE_URL}/batch-reject`, data)
}

/**
 * 关闭批量事件。
 * POST /mobileTerminal/batch-event/close
 */
export const closeBatchEvent = (data: BatchEventCloseModel): Promise<BaseResponse<number>> => {
  return request.post(`${BATCH_EVENT_BASE_URL}/close`, data)
}

/**
 * 批量关闭批量事件。
 * POST /mobileTerminal/batch-event/batch-close
 */
export const batchCloseBatchEvent = (
  data: BatchEventBatchCloseModel
): Promise<BaseResponse<number>> => {
  return request.post(`${BATCH_EVENT_BASE_URL}/batch-close`, data)
}

/**
 * 审核通过。
 * POST /mobileTerminal/batch-event/approve
 */
export const approveBatchEvent = (data: BatchEventApproveModel): Promise<BaseResponse<number>> => {
  return request.post(`${BATCH_EVENT_BASE_URL}/approve`, data)
}

/**
 * 确认处理。
 * POST /mobileTerminal/batch-event/confirm
 */
export const confirmBatchEvent = (data: BatchEventConfirmModel): Promise<BaseResponse<number>> => {
  return request.post(`${BATCH_EVENT_BASE_URL}/confirm`, data)
}

/**
 * 添加/更新抄送人员。
 * POST /mobileTerminal/batch-event/cc
 */
export const ccBatchEvent = (data: BatchEventCcModel): Promise<BaseResponse<number>> => {
  return request.post(`${BATCH_EVENT_BASE_URL}/cc`, data)
}

/**
 * 转派处理人。
 * POST /mobileTerminal/batch-event/reassign
 */
export const reassignBatchEvent = (
  data: BatchEventReassignModel
): Promise<BaseResponse<number>> => {
  return request.post(`${BATCH_EVENT_BASE_URL}/reassign`, data)
}

/**
 * 闭环完成/闭环关闭事件。
 * POST /mobileTerminal/batch-event/handle-complete
 */
export const handleCompleteBatchEvent = (
  data: BatchEventHandleCompleteModel
): Promise<BaseResponse<number>> => {
  return request.post(`${BATCH_EVENT_BASE_URL}/handle-complete`, data)
}

/**
 * 执剑者回调。
 * POST /mobileTerminal/batch-event/executor-callback
 */
export const batchEventExecutorCallback = (
  data: BatchEventExecutorCallbackModel
): Promise<BaseResponse<number>> => {
  return request.post(`${BATCH_EVENT_BASE_URL}/executor-callback`, data)
}

/**
 * 获取批量事件抄送人列表。
 * POST /mobileTerminal/batch-event/cc-user-list
 */
export const getBatchEventCcUserList = (
  data: BatchEventCommonModel
): Promise<BaseResponse<BatchEventCcUserVo[]>> => {
  return request.post(`${BATCH_EVENT_BASE_URL}/cc-user-list`, data)
}

/**
 * 获取批量事件操作记录列表。
 * POST /mobileTerminal/batch-event/ope-log-list
 */
export const getBatchEventOpeLogList = (
  data: BatchEventCommonModel
): Promise<BaseResponse<BatchEventOpeLogVo[]>> => {
  return request.post(`${BATCH_EVENT_BASE_URL}/ope-log-list`, data)
}

export type {
  BatchEventApproveModel,
  BatchEventBatchCloseModel,
  BatchEventBatchCommonModel,
  BatchEventBatchRejectModel,
  BatchEventBriefDetailVo,
  BatchEventCcModel,
  BatchEventCcUserVo,
  BatchEventCarSeriesStatVo,
  BatchEventChannelStatVo,
  BatchEventCloseModel,
  BatchEventCommonModel,
  BatchEventConfirmModel,
  BatchEventConditionsVo,
  BatchEventCreateModel,
  BatchEventDataStatVo,
  BatchEventExecutorCallbackModel,
  BatchEventHandleCompleteModel,
  BatchEventUserVoiceListQuery,
  BatchEventUserVoiceTopicVo,
  BatchEventUserVoiceVo,
  BatchEventMobilePageVo,
  BatchEventOpinionStatVo,
  BatchEventPageRequestParams,
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
  BatchEventTrendStatVo,
  BatchEventOpeLogVo,
  PageInfoBatchEventMobilePageVo,
  PageInfoBatchEventUserVoiceVo,
  TaskEventNewlyVo,
  TaskEventStatusDistributionVo,
  TaskEventTrendVo
} from './types'
