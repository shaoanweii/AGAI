export interface TabsItem {
  tabTitle: string
  key: string
  src: string
  columns: Partial<TabsItemClumns>[]
}

interface TabsItemClumns {
  title: string
  dataIndex: string
  slotName: string
  width: number
  ellipsis: boolean
  tooltip: boolean
}

export interface Table {
  loading: boolean
  list: CategorizeItem[]
}

export interface CategorizeItem {
  id?: string
  name?: string
  tagName?: string
  parentId?: string
  tagParentId?: string
  tagType?: string
  tagCode?: string
  nameEn?: string
  showChild?: boolean
  level?: number
  child?: CategorizeItem[]
  children?: CategorizeItem[] // Element Plus 树形表格需要的属性
  hasChildren?: boolean // Element Plus 树形表格需要的属性
}

export interface TopForm {
  clientId?: string
}

interface Area {
  areaCode: string
  areaName: string
}

export interface Province {
  provinceCode: string
  provinceName: string
  areas: Area[]
}
export interface ChannelParams {
  id?: string
  name?: string
  nameEn?: string
  clientId?: string
  parentId?: string | undefined
  status?: string
  regionCode?: [[string, string]]
  region?: Province[]
  /**
   * Category 渠道分类
   * Channel 渠道
   */
  type?: 'Category' | 'Channel'
}
