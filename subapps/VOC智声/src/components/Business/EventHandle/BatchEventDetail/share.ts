import { EventType } from '@/components/Business/EventHandle/ehConstants'

const BATCH_EVENT_DETAIL_TARGET_PATH = '/customerDirectEngage/batchEvent'
const H5_REDIRECT_ROUTE_PATH = '/h5Rct'

/**
 * 生成批量事件详情的分享链接。
 * 链接会先进入 /h5Rct 做初始化，再跳转回 PC 批量事件页并恢复详情弹窗状态。
 * @param detailId 批量事件 ID；允许空值用于手动拼接测试
 * @param detailType 详情打开类型，默认按“确认”态恢复处理进度
 * @returns 可直接用于分享的完整 URL
 */
export const buildBatchEventDetailShareLink = (
  detailId?: string,
  detailType: EventType = EventType.CONFIRM
) => {
  const pathname = window.location.pathname.replace(/\/$/, '')
  const baseUrl = `${window.location.origin}${pathname}`

  const targetParams = new URLSearchParams()
  targetParams.set('detailId', detailId ?? '')
  targetParams.set('detailType', String(detailType))

  const shareParams = new URLSearchParams()
  shareParams.set(
    'pcTarget',
    encodeURIComponent(`${BATCH_EVENT_DETAIL_TARGET_PATH}?${targetParams.toString()}`)
  )

  return `${baseUrl}/#${H5_REDIRECT_ROUTE_PATH}?${shareParams.toString()}`
}

/**
 * 将路由 query 中的详情类型归一化为枚举值。
 * 未识别值默认回退到确认态，确保分享链接能打开处理进度 tab。
 * @param value 路由 query 值
 * @returns 批量事件详情类型
 */
export const normalizeBatchEventDetailType = (value?: unknown) => {
  const rawValue = Array.isArray(value) ? value[0] : value
  const normalizedValue = String(rawValue ?? '').trim().toLowerCase()

  switch (normalizedValue) {
    case EventType.VIEW:
      return EventType.VIEW
    case EventType.APPROVE:
      return EventType.APPROVE
    case EventType.CONFIRM:
      return EventType.CONFIRM
    case EventType.REJECT:
      return EventType.REJECT
    case EventType.ASSIGN:
      return EventType.ASSIGN
    case EventType.HANDLE:
      return EventType.HANDLE
    case EventType.CLOSE:
      return EventType.CLOSE
    case EventType.IN_PROC:
      return EventType.IN_PROC
    default:
      return EventType.CONFIRM
  }
}
