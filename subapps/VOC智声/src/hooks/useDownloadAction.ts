import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  useDownloadTaskDialog,
  type OpenDownloadTaskDialogOptions
} from '@/hooks/useDownloadTaskDialog'

export type DownloadRequest = (params?: Record<string, any>) => Promise<BaseResponse<any>>

export interface DownloadActionOptions {
  /** 下载接口函数，由具体页面按当前 tab/card 场景传入 */
  request?: DownloadRequest
  /** 下载接口入参，保持与当前页面或 tab 查询接口入参一致 */
  params?: Record<string, any> | (() => Record<string, any>)
  /** 导出来源标识，统一用于区分不同页面/模块的导出入口 */
  exportMenu?: string
  /** 未配置接口时的提示文案 */
  pendingMessage?: string
  /** 接口失败时的兜底提示文案 */
  errorMessage?: string
  /** 下载提示弹框的扩展配置。 */
  dialogOptions?: OpenDownloadTaskDialogOptions
}

/**
 * 统一下载任务创建逻辑。
 * 负责接口调用、防重复提交和成功后的任务提示弹框。
 */
export function useDownloadAction() {
  const downloading = ref(false)
  const { openDownloadTaskDialog } = useDownloadTaskDialog()

  /**
   * 获取下载接口入参。
   * 支持传入对象或函数，函数形式便于点击瞬间读取最新筛选条件。
   */
  const resolveParams = (params?: DownloadActionOptions['params']) => {
    if (typeof params === 'function') return params()
    return params || {}
  }

  /**
   * 执行下载任务创建接口。
   * 接口成功后统一弹出“下载任务已创建”提示。
   */
  const downloadByRequest = async (options: DownloadActionOptions) => {
    const {
      request,
      params,
      exportMenu,
      pendingMessage = '下载接口待配置',
      errorMessage = '下载任务创建失败，请稍后重试',
      dialogOptions
    } = options

    if (downloading.value) return false

    if (!request) {
      ElMessage.warning(pendingMessage)
      return false
    }

    downloading.value = true
    try {
      const resolvedParams = resolveParams(params)
      const response = await request({
        ...resolvedParams,
        ...(exportMenu ? { exportMenu } : {})
      })
      if (response?.success) {
        await openDownloadTaskDialog(dialogOptions)
        return true
      }

      ElMessage.error(response?.message || errorMessage)
      return false
    } catch (error) {
      console.error('下载任务创建失败:', error)
      ElMessage.error(errorMessage)
      return false
    } finally {
      downloading.value = false
    }
  }

  return {
    downloading,
    downloadByRequest
  }
}
