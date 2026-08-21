export type HDialogConfirmHandler = (ctx: { close: () => void }) => unknown | Promise<unknown>

export interface HDialogProps {
  /** 弹窗显示状态 */
  visible: boolean
  /** 弹窗标题 */
  title?: string
  /** 弹窗宽度，支持 px/%/calc */
  width?: string
  /** 是否展示默认底部 */
  showFooter?: boolean
  /** 取消按钮文案 */
  cancelText?: string
  /** 确认按钮文案 */
  confirmText?: string
  /** 是否点击遮罩关闭 */
  closeOnClickOverlay?: boolean
  /** 是否在关闭时销毁内容 */
  destroyOnClose?: boolean
  /** 自定义确认处理函数，存在时接管默认确认关闭逻辑 */
  confirm?: HDialogConfirmHandler
}

export interface HDialogEmits {
  /** 同步弹窗显示状态 */
  'update:visible': [value: boolean]
  /** 弹窗关闭 */
  close: []
  /** 点击取消 */
  cancel: []
  /** 点击确认 */
  confirm: []
}
