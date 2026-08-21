export interface DataPlazaCategoryItem {
  id: string
  categoryName: string
  brandCode: string
  brandName: string
  parentId: string
  categoryLevel: number
  listIcon: string
  detailImage: string
  listIconURL: string
  detailImageURL: string
  sortNo: number
  reportCount: number
  children: DataPlazaCategoryItem[]
  createTime: string
  updateTime: string
}

export interface DataPlazaUploadResult {
  key: string
  name: string | null
  url: string
  urlSuffix: string
}

export interface DataPlazaCategorySaveParams {
  id?: string
  categoryName: string
  brandCode: string
  parentId: string
  listIcon: string
  detailImage: string
}

export interface DataPlazaCategorySortItem {
  id: string
  parentId: string
  brandCode: string
  sortNo: number
}

export type DataPlazaCategorySortParams = DataPlazaCategorySortItem[]

export interface DataPlazaReportListParams {
  pageNum?: number
  pageSize?: number
  categoryId?: string
  isPinned?: string | number
  publishStatus?: string | number
  reportName?: string
}

export interface DataPlazaReportDateCondition {
  selectedShortcut: string
  startDate?: string
  endDate?: string
}

export interface DataPlazaReportDefaultCondition {
  dateRange: string
  brandList: string[]
  carSeriesList: string[]
  channelIds: string[]
  sentimentList: string[]
  intentionList: string[]
  tagType: string
  experienceCode: string[][]
  topicCodes: string[]
  usageScenarioCodes: string[]
  scenarioAttr: string[]
  contentTypes: string[]
  advertisementType: string[]
  accountTypes: string[]
}

export interface DataPlazaReportItem {
  id: string
  reportName: string
  categoryId: string
  categoryName: string
  dateCondition: DataPlazaReportDateCondition
  defaultCondition: DataPlazaReportDefaultCondition
  brandCode: string
  brandName: string
  isPinned: 0 | 1
  pinnedTime: string
  publishStatus: 0 | 1
  publishTime: string
  createBy: string
  updateBy: string
  updateTime: string
  createTime: string
}

export interface DataPlazaReportPageResult {
  total: number
  list: DataPlazaReportItem[]
  pageNum: number
  pageSize: number
  size: number
  startRow: number
  endRow: number
  pages: number
  prePage: number
  nextPage: number
  isFirstPage: boolean
  isLastPage: boolean
  hasPreviousPage: boolean
  hasNextPage: boolean
  navigatePages: number
  navigatepageNums: number[]
  navigateFirstPage: number
  navigateLastPage: number
}

export interface DataPlazaConditionOption {
  key: string
  value: string
  code: string | null
  img: string | null
  startThresholdValue: string | number | null
  sort: number | null
  endThresholdValue: string | number | null
  isCore: number | null
  children?: DataPlazaConditionOption[]
}

export interface DataPlazaConditionGroup {
  key: string
  details: DataPlazaConditionOption[]
}

export interface DataPlazaReportSaveParams {
  id?: string
  reportName: string
  categoryId: string
  dateCondition: DataPlazaReportDateCondition
  defaultCondition: DataPlazaReportDefaultCondition
}

export interface DataPlazaReportDeleteParams {
  id: string
}

export interface DataPlazaReportPinParams {
  id: string
  isPinned: 0 | 1
}

export interface DataPlazaReportPublishStatusParams {
  id: string
  publishStatus: 0 | 1
}

export interface DataPlazaBatchReportIdsParams {
  ids: string[]
}

export interface DataPlazaBatchReportMoveParams extends DataPlazaBatchReportIdsParams {
  targetCategoryId: string
}
