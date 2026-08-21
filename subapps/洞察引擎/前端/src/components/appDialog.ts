import { defineComponent, h, reactive, render, type VNodeChild } from 'vue'
import { ElButton } from 'element-plus'
import AppDialog from './AppDialog.vue'

export type AppDialogAction = 'confirm' | 'cancel' | 'close'

export interface AppDialogConfirmOptions {
  /**
   * 弹框标题，默认展示“提示”。
   */
  title?: string
  /**
   * 确认按钮文案，同时兼容 Element Plus 的 confirmButtonText。
   */
  confirmText?: string
  confirmButtonText?: string
  /**
   * 取消按钮文案，同时兼容 Element Plus 的 cancelButtonText。
   */
  cancelText?: string
  cancelButtonText?: string
  /**
   * 是否展示取消按钮；关闭后底部仅保留确认按钮，适用于纯提示场景。
   */
  showCancelButton?: boolean
  /**
   * 透传给 ElDialog 的属性或事件，例如 width、closeOnClickModal、onClosed。
   */
  dialogAttrs?: Record<string, unknown>
}

type AppDialogContent = string | VNodeChild | (() => VNodeChild)

/**
 * 提供类似 ElMessageBox.confirm 的程序化调用方式，统一复用现有 AppDialog 视觉样式。
 * @param message 弹框内容，支持字符串、VNode 或返回 VNode 的函数。
 * @param title 弹框标题，默认“提示”。
 * @param options 额外配置，兼容常见 MessageBox 按钮命名。
 * @returns 点击确定时 resolve；点击取消或关闭时 reject 对应动作。
 */
export function appDialogConfirm(
  message: AppDialogContent,
  title: string = '提示',
  options: AppDialogConfirmOptions = {}
): Promise<void> {
  const container = document.createElement('div')
  document.body.appendChild(container)

  const state = reactive({
    visible: true
  })

  // 只允许结算一次，避免确认后又触发关闭生命周期导致 Promise 二次落定。
  let settled = false

  const cleanup = () => {
    try {
      render(null, container)
    } finally {
      container.remove()
    }
  }

  const confirmText = options.confirmText ?? options.confirmButtonText ?? '确定'
  const cancelText = options.cancelText ?? options.cancelButtonText ?? '取消'
  const showCancelButton = options.showCancelButton !== false
  const { onClosed: userOnClosed, ...restDialogAttrs } = (options.dialogAttrs ?? {}) as Record<
    string,
    unknown
  >

  return new Promise<void>((resolve, reject) => {
    const settle = (nextAction: AppDialogAction) => {
      if (settled) return
      settled = true

      if (nextAction === 'confirm') {
        resolve()
        return
      }

      reject(nextAction)
    }

    const Host = defineComponent({
      name: 'AppDialogConfirmHost',
      setup() {
        const onCancel = () => {
          settle('cancel')
          state.visible = false
        }

        const onClose = () => {
          // 右上角关闭、点击遮罩、按下 ESC 都会走这里，统一按 close 处理。
          settle('close')
          state.visible = false
        }

        const onClosed = () => {
          // 等弹框动画结束后再销毁节点，避免直接卸载造成视觉跳变。
          cleanup()

          // 极端情况下若未经过 cancel/close/confirm 分支，仍兜底视为关闭。
          if (!settled) {
            settle('close')
          }

          if (typeof userOnClosed === 'function') {
            ;(userOnClosed as (...args: any[]) => void)()
          }
        }

        const confirm = async ({ close }: { close: () => void }) => {
          settle('confirm')
          close()
        }

        const renderMessage = () => {
          if (typeof message === 'function') return message()
          if (typeof message === 'string') {
            return h('div', { class: 'app-dialog__message' }, message)
          }
          return message
        }

        return () =>
          h(
            AppDialog as any,
            {
              visible: state.visible,
              'onUpdate:visible': (value: boolean) => {
                state.visible = value
              },
              title: options.title ?? title,
              cancelText,
              confirmText,
              showFooter: true,
              confirm,
              onCancel,
              onClose,
              // 透传到底层 ElDialog，保持与现有组件能力一致。
              onClosed,
              ...restDialogAttrs
            },
            {
              default: renderMessage,
              footer: () => {
                if (showCancelButton) {
                  return [
                    h(
                      ElButton,
                      {
                        class: 'app-dialog__btn-cancel',
                        onClick: onCancel
                      },
                      () => cancelText
                    ),
                    h(
                      ElButton,
                      {
                        class: 'app-dialog__btn-confirm',
                        type: 'primary',
                        onClick: () => void confirm({ close: () => (state.visible = false) })
                      },
                      () => confirmText
                    )
                  ]
                }

                return h(
                  ElButton,
                  {
                    class: 'app-dialog__btn-confirm',
                    type: 'primary',
                    onClick: () => void confirm({ close: () => (state.visible = false) })
                  },
                  () => confirmText
                )
              }
            }
          )
      }
    })

    render(h(Host), container)
  })
}
