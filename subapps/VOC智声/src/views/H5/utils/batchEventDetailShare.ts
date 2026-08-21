const BATCH_EVENT_DETAIL_TARGET_PATH = '/customerDirectEngage/batchEvent'
const H5_REDIRECT_ROUTE_PATH = '/h5Rct'
const DEFAULT_DETAIL_TYPE = 'confirm'

/**
 * 生成批量事件详情 PC 访问链接。
 * H5 独立保留一份实现，避免后续 H5 分支删除 PC 代码后影响执剑者回跳。
 * @param detailId 批量事件 ID
 * @param detailType PC 详情打开类型，默认进入处理进度确认态
 * @returns 经过 /h5Rct 初始化并跳转 PC 详情弹窗的完整链接
 */
export const buildBatchEventDetailPcShareLink = (
  detailId?: string | number,
  detailType = DEFAULT_DETAIL_TYPE
) => {
  const pathname = window.location.pathname.replace(/\/$/, '')
  const baseUrl = `${window.location.origin}${pathname}`

  const targetParams = new URLSearchParams()
  targetParams.set('detailId', String(detailId ?? ''))
  targetParams.set('detailType', detailType)

  const shareParams = new URLSearchParams()
  shareParams.set(
    'pcTarget',
    encodeURIComponent(`${BATCH_EVENT_DETAIL_TARGET_PATH}?${targetParams.toString()}`)
  )

  return `${baseUrl}/#${H5_REDIRECT_ROUTE_PATH}?${shareParams.toString()}`
}
