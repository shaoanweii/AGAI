/**
 * 报告管理页面按钮权限Map
 */
export const SYSTEM_REPORT_BTN_MAP = {
  RMRSU: 'srReportManagementReviewListingUnlisting'
}

/**
 * 原声查询页面按钮权限Map
 */
export const ORIGINA_SOUND_QUERY_BTN_MAP = {
  SELECT_DATA: 'selectData'
}

/**
 * @description: 下载管理页面按钮权限Map
 * selectAll  可见全部
 * selectOneself 可见自己
 * @return {*}
 */
export const DOWNLOAD_MANAGEMENT_BTN_MAP = {
  SELECT_ALL: 'selectAll',
  SELECT_ONESELF: 'selectOneself'
}

/**
 * @description: 本地数据分析页面按钮权限Map
 * selectAll  可见全部
 * selectOneself 可见自己
 * @return {*}
 */
export const LOCAL_DATA_ANALYSIS_BTN_MAP = {
  SELECT_ALL: 'sysLocalDataAnalysis_selectAll',
  SELECT_ONESELF: 'sysLocalDataAnalysis_selectOneself'
}

/**
 * @description: 操作权限
 *  前往洞察引擎:GO_TO_INSIGHTS
 *  场景发布:SCENARIO_PUBLISH
 *  数据纠错:DATA_CORRENCTION
 *  标记高质量声音:HIGH_QUALITY_SOUND
 *  账号数据下载:ACCOUNT_DATA_DOWNLOAD
 *  高管任务:EXECUTIVE_TASK
 *  原声下载:ORIGINAL_SOUND_DOWNLOAD
 *  事件原声下载:EVENT_ORIGINAL_SOUND_DOWNLOAD
 *  批量事件下载:BULK_EVENT_DOWNLOAD
 *  单点事件下载:SINGLE_POINT_EVENT_DOWNLOAD
 *  PC事件下发:PC_EVENT_ISSUANCE
 *  H5事件下发:H5_EVENT_DISPATCH
 *  统计数据下载:STATISTICAL_DOWNLOAD
 *  明细数据下载:DETAILED_DATA_DOWNLOAD
 * @return {*}
 */
export enum FunctionPermission {
  // 前往洞察引擎
  GO_TO_INSIGHTS = 'goToInsights',
  // 场景发布
  SCENARIO_PUBLISH = 'scenarioPublish',
  // 数据纠错
  DATA_CORRENCTION = 'dataCorrection',
  // 添加语料
  ADD_CORPUS = 'addCorpus',
  // 标记高质量声音
  HIGH_QUALITY_SOUND = 'highQualitySound',
  // 账号数据下载
  ACCOUNT_DATA_DOWNLOAD = 'accountDataDownload',
  // 高管任务
  EXECUTIVE_TASK = 'executiveTask',
  // 原声下载
  ORIGINAL_SOUND_DOWNLOAD = 'originalSoundDownload',
  // 事件原声下载
  EVENT_ORIGINAL_SOUND_DOWNLOAD = 'eventOriginalSoundDownload',
  // 批量事件下载
  BULK_EVENT_DOWNLOAD = 'bulkEventDownload',
  // 单点事件下载
  SINGLE_POINT_EVENT_DOWNLOAD = 'SinglePointEventDownload',
  // PC事件下发
  PC_EVENT_ISSUANCE = 'pcEventIssuance',
  // H5事件下发
  H5_EVENT_DISPATCH = 'h5EventDispatch',
  // 统计数据下载
  STATISTICAL_DOWNLOAD = 'statisticsDownload',
  // 明细数据下载
  DETAILED_DATA_DOWNLOAD = 'detailedDataDownload'
}
