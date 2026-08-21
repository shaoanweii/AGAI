export interface ListItem {
  name: string
  id: string
  pageNum: number
  pageSize: number
}

export interface SeriesItem {
  name: string
  resourceId: string | undefined
}

export interface DetailObj {
  visible: boolean
  data: object
}

export interface statusItem {
  status: string
  statusText: string
  statusCount: string
  [key: string]: any
}
