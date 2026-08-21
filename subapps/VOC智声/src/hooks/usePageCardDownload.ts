import { computed, type MaybeRefOrGetter, toValue } from 'vue'
import { exportVoiceDetail } from '@/api/downloadTask'
import { provideCardDownload, type CardDownloadPayload } from '@/hooks/useCardDownload'
import { useDownloadAction, type DownloadRequest } from '@/hooks/useDownloadAction'

export interface PageCardDownloadOptions {
  /** 当前页面查询接口入参 */
  getParams: (payload?: CardDownloadPayload) => Record<string, any>
  /** 根据卡片信息返回统计下载接口函数 */
  getStatRequest?: (payload: CardDownloadPayload) => DownloadRequest | undefined
  /** 明细下载接口函数 */
  detailRequest?: DownloadRequest
  /** 是否启用卡片下载 */
  enabled?: MaybeRefOrGetter<boolean>
}

/**
 * 页面级 FCard 下载能力。
 * 目标页面调用一次后，页面内所有 FCard 自动展示下载菜单。
 */
export function usePageCardDownload(options: PageCardDownloadOptions) {
  const { getParams, getStatRequest, detailRequest = exportVoiceDetail, enabled = true } = options
  const { downloading, downloadByRequest } = useDownloadAction()

  const enabledRef = computed(() => toValue(enabled))

  /**
   * 下载当前卡片统计数据。
   * 具体统计接口由页面按卡片信息自行匹配。
   */
  const handleDownloadStat = async (payload: CardDownloadPayload) => {
    await downloadByRequest({
      request: getStatRequest?.(payload),
      params: () => getParams(payload),
      exportMenu: payload.exportMenu,
      pendingMessage: '下载统计数据接口待配置',
      errorMessage: '下载统计数据失败，请稍后重试'
    })
  }

  /**
   * 下载当前页面明细数据。
   * 明细接口统一使用原声列表导出，入参保持与当前页面查询接口一致。
   */
  const handleDownloadDetail = async (payload: CardDownloadPayload = {}) => {
    await downloadByRequest({
      request: detailRequest,
      params: () => getParams(payload),
      exportMenu: payload.exportMenu,
      errorMessage: '下载明细数据失败，请稍后重试'
    })
  }

  provideCardDownload({
    enabled: enabledRef,
    showStat: true,
    showDetail: true,
    loading: downloading,
    onDownloadStat: handleDownloadStat,
    onDownloadDetail: handleDownloadDetail
  })

  return {
    downloading,
    handleDownloadStat,
    handleDownloadDetail
  }
}
