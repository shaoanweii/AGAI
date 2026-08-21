/**
 * 常量统一导出文件
 * 只保留项目实际使用的常量
 */

// NSR 数据 URI 常量
// 从 nsrDataURI.ts 导入并重新导出
export * from './nsrDataURI'
export * from './switchOptions'
export * from './cardExportKeys'

// 存储token的key
export const TOKEN_KEY = 'report_token'
export const USER_NAME_KEY = 'report_user_name'
export const USER_ID_KEY = 'report_user_id'
export const VERSION_KEY = 'report_version'

/**
 * 知识库相关接口统一请求头。
 * “新增语料”等知识库接口需携带 api-key。
 */
export const DATA_KNOWLEDGE_API_KEY_HEADER = {
  'api-key': 'voc-voice-local-demo'
} as const

/**
 * 标签类型
 * 用户全旅途: userJourney
 * 服务体验指数: VRT
 * 品质体验指数: CPT
 * 全领域业务: Domain
 * 商品化属性: CommodityAttr
 * NPS: NPS
 */
export enum TagType {
  UserJourney = 'JOUR',
  VRT = 'VRT',
  CPT = 'CPT',
  Domain = 'CA',
  CommodityAttr = 'PR0',
  NPS = 'NPS'
}

/**
 * 单点事件页面类型
 * 全部事件: All: 0
 * 我的事件: Single: 1
 */
export enum CDESinglePointEventPageType {
  All = '0',
  Single = '1'
}

/**
 * 闭环评价
 * 单点事件分析: SingleEventAnaly: 0
 * 批量事件分析: BatchEventAnaly: 1
 * 用户使用分析：UserUseAnaly: 2
 */
export enum EventAnalyType {
  SingleEventAnaly = '0',
  BatchEventAnaly = '1',
  UserUseAnaly = '2'
}

/**
 * 场景管理页面title
 */
export enum SceneManagementTab {
  SCENE = 'scene',
  REPORT = 'report'
}

// 产品过滤标签数据
export const ProductFilterTagName = '产品'
export const ProductFilterTagCode = 'voc-product-001'
// 服务过滤标签数据
export const ServiceFilterTagName = '服务'
export const ServiceFilterTagCode = 'voc-service-001'

/**
 * 事件下发处理优先级选项。
 * 后端 create-event 接口要求提交 P0-P4 原始编码，展示文案与编码保持一致。
 */
export const EVENT_ISSUE_PRIORITY_OPTIONS = ['P0', 'P1', 'P2', 'P3', 'P4'].map(item => ({
  label: item,
  value: item
}))

// 图表主题色常量
// 参考 docs/guide/UI规范.md 图表主题色规范
// 用于 Echarts、图表等全局主题色统一管理

export const CHART_THEME_COLORS: readonly string[] = [
  // '#1677FF',
  // '#0AADFF',
  // '#28C7C7',
  // '#14CA64',
  // '#FACE0C',
  // '#FAB007',
  // '#FE7840',
  // '#FF5959',
  // '#9772FB',
  // '#6675FF',
  // '#7298D0'
  '#1677FF',
  '#28C7C7',
  '#FAB007',
  '#FE7940',
  '#7298D0',
  '#0DAEFF',
  '#14CA64',
  '#FF5959',
  '#929AA6'
]

/**
 * @description: 需要过滤掉的筛选项
 * 91 品牌单选
 * 911 品牌多选
 * 92 标签
 * 93时间，
 * 94移动端时间
 * @return {*}
 */
export const FILTER_TYPE = ['91', '911', '92', '93', '94']
export const FILTER_TYPE_TIME = ['93', '94']

/**
 * @description: ichangan Auth授权  sso登录
 * @return {*}
 */
export const SSO_URL = `${location.origin}${import.meta.env.VITE_API_BASE_URL}/local/session/enter`

//情感颜色
export const sentimentColors: any = {
  正面: '#14CA64',
  中性: '#1677FF',
  负面: '#FF4B4C'
}

/**
 * 词云文字随机色板。
 * 词云文字从该色板中稳定随机取色，不再按情感倾向分配颜色。
 */
export const WORD_CLOUD_RANDOM_COLOR_PALETTE = [
  '#180436',
  '#09245A',
  '#18539F',
  '#1978C8',
  '#21A0E7',
  '#3BBDE9'
] as const

/**
 * @description: 时间默认值选项, src/components/UI/FDatePicker/index.vue中的shortcuts
 * @return {*}
 */
export const FE_TIME_DIMENSION_OPTIONS = [
  {
    code: '2',
    name: '近7天',
    calculate: (end: any) => end.subtract(6, 'day')
  },
  {
    code: '3',
    name: '近30天',
    calculate: (end: any) => end.subtract(29, 'day')
  },
  {
    code: '12',
    name: '本月',
    calculate: (end: any) => end.startOf('month')
  },
  {
    code: '22',
    name: '本季',
    calculate: (end: any) => end.startOf('quarter')
  },
  {
    code: '32',
    name: '本年',
    calculate: (end: any) => end.startOf('year')
  }
]

/**
 * @description: 原声查询页面-原始数据类型
 * 结果数据: resultData
 * 原始数据: originalData
 * 情感分支数据: sentimentBranchData
 */
export enum OriginalDataType {
  ResultData = 'resultData',
  OriginalData = 'originalData',
  SentimentBranchData = 'sentimentBranchData'
}
/**
 * @description: 原声查询页面-原始数据类型
 * @return {*}
 */
export const ORIGINAL_DATA_TYPE_OPTIONS = [
  { value: OriginalDataType.ResultData, label: '结果数据' },
  { value: OriginalDataType.OriginalData, label: '原始数据' },
  { value: OriginalDataType.SentimentBranchData, label: '情感分支数据' }
]

/**
 * @description: 数据查询类型枚举 品牌/车系
 * 原位于 CompetitorAnalysis/constants，移至此处以避免 store chunk 与 components chunk 之间的循环依赖
 */
export enum QueryType {
  Brand = 'brand',
  Series = 'series'
}

export enum QueryTypeInName {
  Brand = '品牌',
  Series = '车系'
}

/**
 * 客情直驱 事件类型切换选项
 */
export const eventSwitchOpts = [
  { value: 'BATCH', label: '批量事件' },
  { value: 'SINGLE', label: '单点事件' }
]

// 订阅周期选择按钮
export enum PeriodType {
  Day30 = '30day',
  Day90 = '90day',
  Day180 = '180day',
  Custom = 'custom'
}

// 发送规则选择按钮
export enum SendRuleType {
  Daily = 1,
  Weekly = 2,
  Monthly = 3
}

// 系统设置订阅页面tabs
export enum SubscribeTabType {
  // 全部订阅
  SubscribeAll = 'all',
  // 全部推送记录
  SubscribeRecord = 'record',
  // 我创建的订阅
  SubscribeMy = 'my',
  // 推送给我的记录
  SubscribeReceived = 'received'
}
