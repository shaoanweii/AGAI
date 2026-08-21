import type { DrillTabKey } from './constants'
import type { DownloadRequest } from '@/hooks/useDownloadAction'

// Tab项接口
export interface TabItem {
  key: DrillTabKey
  label: string
  component?: DrillTabKey | string
  icon?: string
}

// 组件Props接口
export interface DrillDownDialogProps {
  title?: string
  visible?: boolean
  tabs?: TabItem[]
  activeTab?: string
  brandCode?: string
  carSeriesCode?: string
  statDownloadRequest?: (tabKey: string) => DownloadRequest | undefined
  detailDownloadRequest?: DownloadRequest
}

// 组件事件接口
export interface DrillDownDialogEvents {
  'update:visible': [visible: boolean]
  tabChange: [tabKey: string]
  close: []
}
