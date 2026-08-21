import { h } from 'vue'
import { useRouter } from 'vue-router'
import SvgIcon from '@/components/UI/SvgIcon/index.vue'
import { appDialogConfirm } from '@/components/appDialog'

export interface OpenDownloadTaskDialogOptions {
  /** 跳转前的收尾动作，例如关闭当前业务弹框。 */
  beforeNavigate?: () => void | Promise<void>
}

/**
 * 下载任务创建成功弹框。
 * 用于统一下载成功后的提示与下载管理页跳转行为。
 */
export function useDownloadTaskDialog() {
  const router = useRouter()

  /**
   * 打开下载任务提示弹框。
   * 确认后跳转下载管理页，取消或关闭时保持当前页面不变。
   */
  const openDownloadTaskDialog = async (options: OpenDownloadTaskDialogOptions = {}) => {
    try {
      await appDialogConfirm(
        () =>
          h('div', { class: 'flex items-center' }, [
            h(SvgIcon, {
              name: 'info-circle-filled',
              width: '20px',
              height: '20px'
            }),
            h('span', { class: 'ml-8' }, [
              '已创建下载任务，请前往 ',
              h('span', { class: 'text-link' }, '下载管理'),
              ' 页面进行查看'
            ])
          ]),
        '下载数据',
        {
          cancelText: '稍后再说',
          confirmText: '前往查看',
          dialogAttrs: {
            width: '480px',
            destroyOnClose: true
          }
        }
      )

      await options.beforeNavigate?.()
      router.push('/system/downloadManagement')
    } catch {
      // 用户取消或关闭弹框时无需额外处理。
    }
  }

  return {
    openDownloadTaskDialog
  }
}
