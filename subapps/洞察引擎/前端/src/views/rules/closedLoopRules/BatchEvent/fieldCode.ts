/**
 * 批量规则指标字段统一以当前接口最新返回编码为准。
 */
export const BATCH_METRIC_FIELD_CODE = {
  TOP_RANK: 'topRank'
} as const

/**
 * 批量规则维度统计方式统一收口，避免页面继续散落字符串判断。
 */
export const BATCH_DIMENSION_STAT_MODE = {
  ALONE: 'alone'
} as const

/**
 * 仅部分维度在独立计算时需要限制可选数量；
 * 求和等其他统计方式不限制，避免把历史上限误扩散到全部统计口径。
 */
export const BATCH_DIMENSION_ALONE_MULTIPLE_LIMIT_MAP = {
  data_source: 15,
  carSeries: 10,
  experience_code: 1000
} as const

/**
 * 批量规则维度字段统一以当前接口最新返回编码为准，
 * 页面侧不再维护新旧字段别名映射，避免同一语义长期并存两套 code。
 */
export const BATCH_DIMENSION_FIELD_CODE = {
  PROVINCE: 'province',
  DATA_SOURCE: 'data_source',
  ATTRIBUTE: 'attribute',
  CAR_SERIES: 'carSeries',
  BATCH_KH_TYPE: 'batch_kh_type',
  EXPERIENCE_CODE: 'experience_code',
  PUBLISH_USER: 'publish_user',
  ORIGINAL_POST_USER: 'original_post_user',
  AD_TYPE: 'AD_type',
  EMOTION: 'emotion',
  EMOTIONAL_LEVEL: 'affective_level',
  INTENTION: 'intention',
  REGULATION_CONTENT_TYPE: 'regulation_content_type',
  WATER_MAN: 'water_man',
  V_MAN: 'V_man',
  CAR_OWNER: 'car_owner',
  CUSTOMER_GENDER: 'customer_gender',
  TITLE: 'title',
  CONTENT: 'content'
} as const

export type BatchDimensionFieldCode =
  (typeof BATCH_DIMENSION_FIELD_CODE)[keyof typeof BATCH_DIMENSION_FIELD_CODE]

/**
 * 预警周期相关值在表单、回填、保存流程都会复用，
 * 统一集中到这里维护，避免同一业务语义散落多个文件后难以同步。
 */
export const BATCH_ALERT_CYCLE_TYPE = {
  DAILY: 'daily',
  WEEKLY: 'weekly',
  MONTHLY: 'monthly'
} as const

export const BATCH_ALERT_CYCLE_TYPE_LIST = [
  BATCH_ALERT_CYCLE_TYPE.DAILY,
  BATCH_ALERT_CYCLE_TYPE.WEEKLY,
  BATCH_ALERT_CYCLE_TYPE.MONTHLY
] as const

export const BATCH_ALERT_CYCLE_OPTIONS = [
  { label: '每日', value: BATCH_ALERT_CYCLE_TYPE.DAILY },
  { label: '每周', value: BATCH_ALERT_CYCLE_TYPE.WEEKLY },
  { label: '每月', value: BATCH_ALERT_CYCLE_TYPE.MONTHLY }
] as const

/**
 * 星期枚举既用于构建下拉选项，也用于接口回填时的数字转中文，
 * 统一收口后可以避免默认星期、显示文案、索引映射三处定义不一致。
 */
export const BATCH_ALERT_WEEK_DAYS = [
  '周一',
  '周二',
  '周三',
  '周四',
  '周五',
  '周六',
  '周日'
] as const

export const BATCH_ALERT_WEEK_OPTIONS = BATCH_ALERT_WEEK_DAYS.map(item => ({
  label: item,
  value: item
}))

export const BATCH_ALERT_DEFAULT_CONFIG = {
  cycleType: BATCH_ALERT_CYCLE_TYPE.DAILY,
  weekDay: '周一',
  monthDay: '1',
  pushTime: '08:00'
} as const

export const BATCH_ALERT_INVALID_PUSH_TIME = '0'

/**
 * 统一判断后端返回的周期编码是否为页面支持的合法值，
 * 避免业务层继续散落数组 includes 与默认值兜底逻辑。
 * @param value 周期编码
 * @returns 是否为合法周期编码
 */
export const isBatchAlertCycleType = (
  value: string
): value is (typeof BATCH_ALERT_CYCLE_TYPE_LIST)[number] => {
  return BATCH_ALERT_CYCLE_TYPE_LIST.includes(value as (typeof BATCH_ALERT_CYCLE_TYPE_LIST)[number])
}

/**
 * 批量规则必填维度集中配置，界面标识与业务校验统一引用这里，避免两边规则漂移。
 */
export const BATCH_REQUIRED_DIMENSION_FIELD_CODES = [
  BATCH_DIMENSION_FIELD_CODE.DATA_SOURCE
] as const

export type BatchRequiredDimensionFieldCode = (typeof BATCH_REQUIRED_DIMENSION_FIELD_CODES)[number]

/**
 * 判断当前维度字段是否属于批量规则必填维度。
 * @param field 当前字段编码
 * @returns boolean
 */
export const isRequiredBatchDimensionField = (
  field: string
): field is BatchRequiredDimensionFieldCode => {
  return BATCH_REQUIRED_DIMENSION_FIELD_CODES.includes(field as BatchRequiredDimensionFieldCode)
}

/**
 * 批量规则维度的多选上限只在独立计算 alone 下启用。
 * @param field 当前维度字段编码
 * @param statMode 当前统计方式
 * @returns number
 */
export const getBatchDimensionMultipleLimit = (field: string, statMode: string) => {
  if (statMode !== BATCH_DIMENSION_STAT_MODE.ALONE) {
    return 0
  }

  return (
    BATCH_DIMENSION_ALONE_MULTIPLE_LIMIT_MAP[
      field as keyof typeof BATCH_DIMENSION_ALONE_MULTIPLE_LIMIT_MAP
    ] || 0
  )
}
