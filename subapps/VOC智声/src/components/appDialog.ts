import { defineComponent, h, reactive, render, type VNodeChild } from 'vue'
import AppDialog from './AppDialog.vue'

export type AppDialogAction = 'confirm' | 'cancel' | 'close'

export interface AppDialogConfirmOptions {
  /**
   * 弹框标题（默认：提示）
   */
  title?: string
  /**
   * 确认按钮文案（同时兼容 Element Plus 的 confirmButtonText）
   */
  confirmText?: string
  confirmButtonText?: string
  /**
   * 取消按钮文案（同时兼容 Element Plus 的 cancelButtonText）
   */
  cancelText?: string
  cancelButtonText?: string
  /**
   * 透传给 ElDialog 的属性/事件（例如：width、closeOnClickModal、destroyOnClose、onClosed 等）
   */
  dialogAttrs?: Record<string, unknown>
}

type AppDialogContent = string | VNodeChild | (() => VNodeChild)

/**
 * 类似 ElMessageBox.confirm 的用法：
 * await appDialogConfirm('确定要删除吗？', '提示')
 *
 * - 点击“确定” => resolve
 * - 点击“取消”/关闭弹框（mask、ESC、右上角关闭） => reject('cancel' | 'close')
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

  // 只允许结算一次，避免 confirm 后又触发 close 导致二次 reject
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
  const { onClosed: userOnClosed, ...restDialogAttrs } = (options.dialogAttrs ?? {}) as Record<
    string,
    unknown
  >

  return new Promise<void>((resolve, reject) => {
    const settle = (nextAction: AppDialogAction) => {
      if (settled) return
      settled = true

      if (nextAction === 'confirm') resolve()
      else reject(nextAction)
    }

    const Host = defineComponent({
      name: 'AppDialogConfirmHost',
      setup() {
        const onCancel = () => {
          settle('cancel')
          state.visible = false
        }

        const onClose = () => {
          // 右上角关闭 / 点击遮罩 / ESC 等都会走 close
          settle('close')
          state.visible = false
        }

        const onClosed = () => {
          // 等弹框完全关闭后再销毁，避免过渡动画被硬切
          cleanup()

          // 兜底：极端情况下没有走 cancel/close 事件但触发了 closed
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
              'onUpdate:visible': (v: boolean) => {
                state.visible = v
              },
              title,
              cancelText,
              confirmText,
              showFooter: true,
              confirm,
              onCancel,
              onClose,
              width: '480px',
              // 透传到 ElDialog（由 AppDialog 内部 v-bind="$attrs" 转发）
              onClosed,
              ...restDialogAttrs
            },
            {
              default: renderMessage
            }
          )
      }
    })

    render(h(Host), container)
  })
}
