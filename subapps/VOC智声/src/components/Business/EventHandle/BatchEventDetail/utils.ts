const normalizeBatchEventDetailValue = (value?: string | number | null) => {
  return value === null || value === undefined ? '' : String(value).trim()
}

/**
 * 格式化批量事件详情标题。
 * @param eventName 事件名称
 * @param warningEventNo 事件编号
 * @returns 事件详情标题，优先展示为“事件名称-事件编号”
 */
export const formatBatchEventDetailTitle = (
  eventName?: string | number | null,
  warningEventNo?: string | number | null
) => {
  const normalizedEventName = normalizeBatchEventDetailValue(eventName)
  const normalizedWarningEventNo = normalizeBatchEventDetailValue(warningEventNo)
  const titleParts = [normalizedEventName, normalizedWarningEventNo].filter(Boolean)

  return titleParts.length > 0 ? titleParts.join('-') : '-'
}

/**
 * 格式化批量事件业务责任人展示文案。
 * @param mainRespUserName 业务责任人姓名
 * @param mainRespUserEmpNo 业务责任人工号
 * @returns 业务责任人文案，优先展示为“名称-工号”
 */
export const formatBatchEventMainRespUser = (
  mainRespUserName?: string | number | null,
  mainRespUserEmpNo?: string | number | null
) => {
  const userParts = [
    normalizeBatchEventDetailValue(mainRespUserName),
    normalizeBatchEventDetailValue(mainRespUserEmpNo)
  ].filter(Boolean)

  return userParts.length > 0 ? userParts.join('-') : '-'
}
