export interface Options {
  url?: string
  method?: 'POST' | 'GET' | undefined
  selectionUrl?: string
  deleteUrl?: string
  filter?: object | undefined
  notResetKey?: string[]
  timeout?: number
  pageKey?: string
  pageSizeKey?: string
  pageSize?: number
  headers?: Record<string, any>
}

export interface Form<T = DefaultData> {
  visible?: boolean
  operation: 'add' | 'view' | 'edit' | ''
  data: T
}

export interface DefaultData {
  [key: string]: any
}

export interface SelectionItem {
  key: string
  value: string
  code?: string
}

export interface Selection {
  key: string
  details: SelectionItem[]
}

export interface Table<Item = any> {
  filter?: any
  selection?: Selection[]
  loading?: boolean
  list: Item[]
  total: number
  pageNum: number
  pageSize: number
  [key: string]: any
}
