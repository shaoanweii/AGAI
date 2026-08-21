import { BATCH_METRIC_FIELD_CODE } from './fieldCode'
import type { BatchMetricConfigOptions, BatchMetricRow, BatchSelectOption } from './types'

type BatchMetricOptionSource = Pick<
  BatchMetricConfigOptions,
  | 'metricFieldOptions'
  | 'metricTypeMap'
  | 'metricWildcardMap'
  | 'metricValueTypeMap'
  | 'metricUnitMap'
>

/**
 * 指标联动统一使用“指标 + 指标类型”的复合键，避免不同指标下相同类型 code 串值。
 * @param metric 指标 code
 * @param metricType 指标类型 code
 * @returns string
 */
export const buildMetricTypeKey = (metric: string, metricType: string) =>
  `${metric || ''}__${metricType || ''}`

/**
 * 指标单位和值格式统一由“指标 + 指标类型 + 值类型”决定。
 * @param metric 指标 code
 * @param metricType 指标类型 code
 * @param valueType 值类型 code
 * @returns string
 */
export const buildMetricUnitKey = (metric: string, metricType: string, valueType: string) =>
  `${buildMetricTypeKey(metric, metricType)}__${valueType || ''}`

/**
 * 判断当前指标列表里是否包含 TOP 排行。
 * @param metrics 指标配置列表
 * @returns boolean
 */
export const hasTopRankMetric = (metrics: Array<Pick<BatchMetricRow, 'metric'>> = []) => {
  return metrics.some(item => item.metric === BATCH_METRIC_FIELD_CODE.TOP_RANK)
}

/**
 * 根据当前指标获取指标类型选项。
 * @param options 指标配置选项源
 * @param metric 指标 code
 * @returns BatchSelectOption[]
 */
export const getBatchMetricTypeOptions = (
  options: BatchMetricOptionSource,
  metric: string
): BatchSelectOption[] => {
  if (!metric) {
    return []
  }

  return options.metricTypeMap[metric] || []
}

/**
 * 根据当前“指标 + 指标类型”获取操作符选项。
 * @param options 指标配置选项源
 * @param metric 指标 code
 * @param metricType 指标类型 code
 * @returns BatchSelectOption[]
 */
export const getBatchMetricWildcardOptions = (
  options: BatchMetricOptionSource,
  metric: string,
  metricType: string
): BatchSelectOption[] => {
  if (!metric || !metricType) {
    return []
  }

  return options.metricWildcardMap[buildMetricTypeKey(metric, metricType)] || []
}

/**
 * 根据当前“指标 + 指标类型”获取值类型选项。
 * @param options 指标配置选项源
 * @param metric 指标 code
 * @param metricType 指标类型 code
 * @returns BatchSelectOption[]
 */
export const getBatchMetricValueTypeOptions = (
  options: BatchMetricOptionSource,
  metric: string,
  metricType: string
): BatchSelectOption[] => {
  if (!metric || !metricType) {
    return []
  }

  return options.metricValueTypeMap[buildMetricTypeKey(metric, metricType)] || []
}

/**
 * 根据当前“指标 + 指标类型 + 值类型”获取单位/格式。
 * @param options 指标配置选项源
 * @param metric 指标 code
 * @param metricType 指标类型 code
 * @param valueType 值类型 code
 * @returns string
 */
export const getBatchMetricUnit = (
  options: BatchMetricOptionSource,
  metric: string,
  metricType: string,
  valueType: string
) => {
  if (!metric || !metricType || !valueType) {
    return ''
  }

  return options.metricUnitMap[buildMetricUnitKey(metric, metricType, valueType)] || ''
}
