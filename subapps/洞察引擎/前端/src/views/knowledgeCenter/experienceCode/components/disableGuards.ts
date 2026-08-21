import { appDialogConfirm } from '@/components/appDialog'

const DISABLED_STATUS_VALUE = '0'

/**
 * 禁用拦截提示文案统一收口，避免左右两侧各自维护一份导致口径不一致。
 */
export const FINAL_TOPIC_DISABLE_BLOCK_MESSAGE =
  '当前体验代码下已绑定启用状态的标准观点，请调整后重试。'

/**
 * 编辑场景下只要提交结果为禁用，且当前数据已绑定启用状态的标准观点，就统一拦截。
 * 这样无论是“启用改禁用”，还是“本身已禁用但继续保存”，都能稳定给出一致提示。
 */
export const shouldBlockDisableSubmit = (options: {
  hasFinalTopic?: boolean | null
  nextStatus?: string | null
}) => {
  const { hasFinalTopic = false, nextStatus } = options
  return hasFinalTopic === true && String(nextStatus ?? '') === DISABLED_STATUS_VALUE
}

/**
 * 批量禁用时只要存在一条绑定启用标准观点的数据，就整体阻断本次操作。
 */
export const hasAnyFinalTopicBoundRow = (rows: Array<{ hasFinalTopic?: boolean | null }> = []) => {
  return rows.some(item => item?.hasFinalTopic === true)
}

/**
 * 统一弹出拦截提示，确保左右侧和批量场景的交互文案保持一致。
 */
export const showDisableBlockedDialog = () => {
  return appDialogConfirm(FINAL_TOPIC_DISABLE_BLOCK_MESSAGE, '提示', {
    confirmButtonText: '我知道了',
    showCancelButton: false,
    dialogAttrs: {
      width: '400px',
      closeOnClickModal: false,
      closeOnPressEscape: false
    }
  }).catch(() => undefined)
}
