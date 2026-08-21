import request from '@h5/api/http'
import type {
  NewlyEventStatisticsVo,
  EventStatusDistributionVo,
  EventTrendVo,
  MobileSingleEventDetailQuery,
  MobileSingleEventDetailBaseVo,
  MobileSingleEventDetailVo,
  MobileSingleEventRelationEventItem,
  H5VocTaskBaseRequest,
  MobileSingleEventListPageResult,
  SingleEventTopicItem
} from './types'

/**
 * 批量获取观点（标准观点）列表（H5）
 * POST /mobileTerminal/single-event/topics-batch
 * @param strings 体验代码最末级 code 列表（1-3级单选：传 [code]；4级多选：传 code 数组）
 */
export const getSingleEventTopicsBatch = (
  strings: string[]
): Promise<BaseResponse<SingleEventTopicItem[]>> => {
  return request.post('/report/mobileTerminal/single-event/topics-batch', strings, {
    cancelPrevious: true
  })
}

/**
 * 获取当前登录人关联的任务事件
 * POST /mobileTerminal/single-event/get-user-related-events
 * 说明：接口无入参，仅依赖当前登录人上下文返回事件数据
 */
export const getUserRelatedEvents = (): Promise<BaseResponse<any>> => {
  // 按照 H5 侧约定，统一在路径前增加 /report 前缀
  return request.post(
    '/report/mobileTerminal/single-event/get-user-related-events',
    {},
    { cancelPrevious: true }
  )
}

/**
 * 获取单点事件筛选条件（H5）
 * POST /mobileTerminal/single-event/conditions
 * 说明：接口无入参，仅依赖当前登录人上下文返回可用筛选项
 */
export const getTaskEventConditions = (): Promise<BaseResponse<any>> => {
  // 与任务事件列表同样增加 /report 前缀，保持网关路径一致
  return request.post('/report/mobileTerminal/single-event/conditions', {})
}

export const getSysAllDictItems = (): Promise<BaseResponse<any>> => {
  // 与任务事件列表同样增加 /report 前缀，保持网关路径一致
  return request.post('/report/insDictItem/sysAllDictItems', {})
}

/**
 * 获取部门-人员树（用于 H5 筛选“处理人员”）
 * GET insights/accountInfo/findDepartAccountTree
 * 说明：数据量可能很大（万级），前端配合 TreeV2 虚拟化渲染
 */
export const getDepartAccountTree = (): Promise<BaseResponse<any>> => {
  // 该接口属于 insights 域，不走 /report 前缀（按后端网关约定）
  return request.get('/insights/accountInfo/findDepartAccountTree', { cancelPrevious: true })
}

/**
 * 获取新增事件统计（H5）
 * POST /mobileTerminal/single-event/newly-event-statistics
 * 说明：依赖品牌、时间、公私域等筛选条件
 */
export const getNewlyEventStatistics = (
  data: H5VocTaskBaseRequest
): Promise<BaseResponse<NewlyEventStatisticsVo>> => {
  return request.post('/report/mobileTerminal/single-event/newly-event-statistics', data)
}

/**
 * 获取事件状态分布（H5）
 * POST /mobileTerminal/single-event/event-status-distribution
 * 说明：依赖品牌、时间、公私域等筛选条件
 */
export const getEventStatusDistribution = (
  data: H5VocTaskBaseRequest
): Promise<BaseResponse<EventStatusDistributionVo[]>> => {
  return request.post('/report/mobileTerminal/single-event/event-status-distribution', data)
}

/**
 * 获取事件趋势（H5）
 * POST /mobileTerminal/single-event/event-trend
 * 说明：依赖品牌、时间、公私域等筛选条件
 */
export const getEventTrend = (
  data: H5VocTaskBaseRequest
): Promise<BaseResponse<EventTrendVo[]>> => {
  return request.post('/report/mobileTerminal/single-event/event-trend', data)
}

/**
 * 获取单点事件列表（H5）
 * POST /mobileTerminal/single-event/list
 * 说明：依赖品牌、时间、状态等筛选条件，支持分页
 */
export const getMobileSingleEventList = (
  data: H5VocTaskBaseRequest
): Promise<BaseResponse<MobileSingleEventListPageResult>> => {
  return request.post('/report/mobileTerminal/single-event/list', data)
}

/**
 * 获取单点事件详情（H5）
 * POST /mobileTerminal/single-event/get-detail-event
 * 说明：点击事件列表进入详情时，会携带 dataId（原声ID）与 id（事件ID）
 */
export const getMobileSingleEventDetail = (
  data: MobileSingleEventDetailQuery
): Promise<BaseResponse<MobileSingleEventDetailVo>> => {
  return request.post('/report/mobileTerminal/single-event/get-detail-event', data)
}

/**
 * 获取单点事件原声详情（H5）
 * POST /mobileTerminal/single-event/get-detail-base
 * 说明：点击事件列表进入详情时，会携带 dataId（原声ID）与 id（事件ID，非必填）
 */
export const getMobileSingleEventDetailBase = (
  data: MobileSingleEventDetailQuery
): Promise<BaseResponse<MobileSingleEventDetailBaseVo>> => {
  return request.post('/report/mobileTerminal/single-event/get-detail-base', data)
}

/**
 * 获取单点事件关联事件（H5）
 * POST /mobileTerminal/single-event/get-relation-events
 * 说明：依赖 dataId（原声ID）与 id（事件ID）
 */
export const getMobileSingleEventRelationEvents = (
  data: Required<Pick<MobileSingleEventDetailQuery, 'dataId'>> &
    Required<Pick<MobileSingleEventDetailQuery, 'id'>>
): Promise<BaseResponse<MobileSingleEventRelationEventItem[]>> => {
  return request.post('/report/mobileTerminal/single-event/get-relation-events', data)
}

// 导出类型定义
export type {
  NewlyEventStatisticsVo,
  EventStatusDistributionVo,
  EventTrendVo,
  MobileSingleEventDetailQuery,
  MobileSingleEventDetailBaseVo,
  MobileSingleEventDetailVo,
  MobileSingleEventRelationEventItem,
  H5VocTaskBaseRequest,
  MobileSingleEventListPageResult,
  MobileSingleEventListItem,
  SingleEventPrivateMsgModel,
  SingleEventUserModel,
  SingleEventWorkOrderModel,
  TaskEventLogModel,
  TaskEventLogContentModel,
  SingleEventIntentionVo,
  SingleEventTopicItem
} from './types'
