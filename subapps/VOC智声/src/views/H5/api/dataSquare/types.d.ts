/**
 * 移动端看数广场品牌项。
 */
export interface H5DataSquareBrandItem {
  categoryId: string
  categoryName: string
  brandCode: string
  brandName: string
  sortNo: number
}

/**
 * 移动端看数广场分类项。
 */
export interface H5DataSquareCategoryItem {
  categoryId: string
  categoryName: string
  brandCode: string
  brandName: string
  listIcon?: string
  detailImage?: string
  listIconURL?: string
  detailImageURL?: string
  sortNo: number
  reportCount?: number
  hasMore?: boolean
  reports?: H5DataSquareReportItem[]
}

/**
 * 移动端看数广场报告项。
 */
export interface H5DataSquareReportItem {
  reportId: string
  reportName: string
  categoryId: string
  categoryName: string
  brandCode: string
  brandName: string
  isPinned: 0 | 1
  pinnedTime: string
  publishTime: string
  updateTime: string
}

/**
 * 数据报告详情的日期筛选条件。
 */
export interface H5DataSquareReportDateCondition {
  selectedShortcut: string
  startDate: string
  endDate: string
}

/**
 * H5 数据报告筛选条件选项。
 */
export interface H5DataSquareConditionOption {
  key: string
  value: string
  code: string | null
  img: string | null
  startThresholdValue: string | number | null
  sort: number | null
  endThresholdValue: string | number | null
  isCore: number | null
  children?: H5DataSquareConditionOption[]
}

/**
 * H5 数据报告筛选条件分组。
 */
export interface H5DataSquareConditionGroup {
  key: string
  details: H5DataSquareConditionOption[]
}

/**
 * H5 数据源树节点。
 */
export interface H5DataSquareChannelNode {
  name?: string
  code?: string
  child?: H5DataSquareChannelNode[]
}

/**
 * H5 属性标签选项。
 */
export interface H5DataSquareAttributeLabelItem {
  id?: string
  name?: string
}

/**
 * H5 体验代码树节点。
 */
export interface H5DataSquareLabelTag {
  id?: string
  tagParentId?: string
  tagName?: string
  tagCode?: string
  checked?: boolean
  child?: H5DataSquareLabelTag[]
}

/**
 * H5 标准观点选项。
 */
export interface H5DataSquareStandardViewpointOption {
  tagCode?: string
  tagName?: string
}

/**
 * H5 标准观点查询参数。
 */
export interface H5DataSquareTagLibParams {
  tagType?: string
  tagParentId?: string
  codes?: string[]
}

/**
 * 数据报告详情默认筛选条件。
 */
export interface H5DataSquareReportDefaultCondition {
  dateRange?: string
  brandList?: string[]
  carSeriesList?: string[]
  channelIds?: string[]
  sentimentList?: string[]
  intentionList?: string[]
  tagType?: string
  experienceCode?: string[][]
  topicCodes?: string[]
  usageScenarioCodes?: string[]
  scenarioAttr?: string[]
  contentTypes?: string[]
  advertisementType?: string[]
  accountTypes?: string[]
}

/**
 * 数据报告详情配置。
 */
export interface H5DataSquareReportDetail {
  id?: string
  reportId?: string
  reportName?: string
  categoryId?: string
  categoryName?: string
  dateCondition?: H5DataSquareReportDateCondition
  defaultCondition?: H5DataSquareReportDefaultCondition
  brandCode?: string
  brandName?: string
}

/**
 * 数据报告详情入参。
 */
export interface H5DataSquareReportDetailParams {
  id: string
}

/**
 * 数据报告下钻核心数据。
 */
export interface H5DataSquareDrillDownBrief {
  negativeMentions: number
  negativeMentionsMoM: number
  negativeRate: number
  negativeRateMoM: number
  negativeRateYoY: number
  positiveMentions: number
  positiveMentionsMoM: number
  positiveRate: number
  positiveRateMoM: number
  positiveRateYoY: number
  mentions: number
  mentionsMoM: number
  mentionsYoY: number
  users: number
  usersMoM: number
  usersYoY: number
  rateColor?: string
  rateBackgroundColor?: string
}

/**
 * 分页结构，字段按后端 PageInfo 原样保留。
 */
export interface H5DataSquarePageResult<T> {
  total: number
  list: T[]
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

/**
 * 广场首页请求参数。
 */
export interface H5DataSquareHomeParams {
  categoryId?: string
  brandCode: string
  reportLimit: number
}

/**
 * 广场搜索请求参数。
 */
export interface H5DataSquareSearchParams {
  pageSize: number
  pageNum: number
  brandCode: string
  keyword: string
  categoryId?: string
}

/**
 * 分类报告列表请求参数。
 */
export interface H5DataSquareCategoryReportParams {
  pageSize: number
  pageNum: number
  categoryId: string
}

/**
 * 分类详情接口结果。
 */
export interface H5DataSquareCategoryReportResult {
  category: H5DataSquareCategoryItem
  reports: H5DataSquarePageResult<H5DataSquareReportItem>
}
