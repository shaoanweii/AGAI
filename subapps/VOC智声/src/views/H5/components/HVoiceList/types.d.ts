import type { VoiceListItem } from '@h5/api/home/types'

/**
 * 声音列表组件Props接口
 */
export interface VoiceListProps {
  //品牌
  brandCode?: string
  /** 声音列表数据 */
  voiceList: VoiceListItem[]
  /** 是否显示加载更多按钮 */
  isLoadMore?: boolean
  loading?: boolean
}

/**
 * 声音列表组件事件接口
 */
export interface VoiceListEmits {
  /** 点击加载更多事件 */
  (e: 'load-more'): void
  /** 点击声音列表项事件 */
  (e: 'item-click', item: VoiceListItem): void
}
