import type { VoiceListItem } from '@h5/api/home/types'

/**
 * 声音列表组件状态接口
 */
export interface VoiceListState {
  /** 声音列表数据 */
  voiceList: VoiceListItem[]
  /** 加载状态 */
  loading: boolean
  /** 刷新状态 */
  refreshing: boolean
  /** 是否已加载完成 */
  finished: boolean
  /** 当前页码 */
  pageNum: number
  /** 每页大小 */
  pageSize: number
  /** 总数量 */
  total: number
}

/**
 * 声音列表组件暴露的方法接口
 */
export interface VoiceListTabExpose {
  /** 初始化数据 */
  initData: () => void
  /** 重置并刷新数据 */
  resetAndRefresh: (filter?: any) => void
}
