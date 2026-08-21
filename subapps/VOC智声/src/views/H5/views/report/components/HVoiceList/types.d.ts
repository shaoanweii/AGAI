export interface VoiceListProps {
  brandCode?: string
  voiceList: any[]
  isLoadMore?: boolean
  loading?: boolean
}

export interface VoiceListEmits {
  (e: 'load-more'): void
  (e: 'item-click', item: any): void
}
