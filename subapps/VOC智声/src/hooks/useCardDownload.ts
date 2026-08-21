import { inject, provide, type InjectionKey, type MaybeRef } from 'vue'

export interface CardDownloadPayload {
  /** 卡片稳定业务标识，用于匹配统计下载接口 */
  cardKey?: string
  /** 导出来源标识，用于下载任务展示和后端记录 */
  exportMenu?: string
}

export interface CardDownloadContext {
  /** 是否启用卡片下载菜单 */
  enabled?: MaybeRef<boolean>
  /** 是否展示统计下载 */
  showStat?: MaybeRef<boolean>
  /** 是否展示明细下载 */
  showDetail?: MaybeRef<boolean>
  /** 下载中状态 */
  loading?: MaybeRef<boolean>
  /** 统计数据下载处理 */
  onDownloadStat?: (payload: CardDownloadPayload) => void | Promise<void>
  /** 明细数据下载处理 */
  onDownloadDetail?: (payload: CardDownloadPayload) => void | Promise<void>
}

export const CARD_DOWNLOAD_CONTEXT_KEY: InjectionKey<CardDownloadContext> = Symbol(
  'CARD_DOWNLOAD_CONTEXT_KEY'
)

/**
 * 提供卡片下载上下文。
 * 页面侧通常通过 usePageCardDownload 间接调用，避免业务页面直接关心 provide 细节。
 */
export const provideCardDownload = (context: CardDownloadContext) => {
  provide(CARD_DOWNLOAD_CONTEXT_KEY, context)
}

/**
 * 获取卡片下载上下文，供 FCard 内部消费。
 */
export const useCardDownloadContext = () => {
  return inject(CARD_DOWNLOAD_CONTEXT_KEY, null)
}
