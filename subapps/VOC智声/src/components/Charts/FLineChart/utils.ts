import type { FieldMapping, SeriesConfig } from './types.d'

/**
 * 创建基础字段映射配置
 * @param xAxis - X轴字段名
 * @param valueField - 数值字段名
 * @param seriesName - 系列名称，默认为"数值"
 * @returns FieldMapping 字段映射配置
 */
export function createBasicFieldMapping(
  xAxis: string,
  valueField: string,
  seriesName: string = '数值'
): FieldMapping {
  return {
    xAxis,
    series: [{ name: seriesName, field: valueField }]
  }
}

/**
 * 创建多系列字段映射配置
 * @param xAxis - X轴字段名
 * @param seriesConfigs - 系列配置数组
 * @returns FieldMapping 字段映射配置
 */
export function createMultiSeriesFieldMapping(
  xAxis: string,
  seriesConfigs: SeriesConfig[]
): FieldMapping {
  return {
    xAxis,
    series: seriesConfigs
  }
}

/**
 * 格式化时间维度数据
 * @param data - 原始数据
 * @param timeField - 时间字段名
 * @param format - 时间格式化函数，可选
 * @returns 格式化后的数据
 */
export function formatTimeData(
  data: Array<Record<string, any>>,
  timeField: string,
  format?: (value: any) => string
): Array<Record<string, any>> {
  if (!format) return data

  return data.map(item => ({
    ...item,
    [timeField]: format(item[timeField])
  }))
}

/**
 * 生成模拟趋势数据
 * @param months - 月份数组
 * @param fields - 需要生成的字段数组
 * @param baseValue - 基础值，默认为100
 * @param variation - 变化幅度，默认为50
 * @returns 模拟数据数组
 */
export function generateMockTrendData(
  months: string[],
  fields: string[],
  baseValue: number = 100,
  variation: number = 50
): Array<Record<string, any>> {
  return months.map(month => {
    const item: Record<string, any> = { month }

    fields.forEach(field => {
      item[field] = baseValue + Math.floor(Math.random() * variation * 2) - variation
    })

    return item
  })
}

/**
 * 数据验证：检查数据是否符合字段映射要求
 * @param data - 数据数组
 * @param fieldMapping - 字段映射配置
 * @returns 验证结果
 */
export function validateData(
  data: Array<Record<string, any>>,
  fieldMapping: FieldMapping
): { valid: boolean; errors: string[] } {
  const errors: string[] = []

  if (!Array.isArray(data) || data.length === 0) {
    errors.push('数据必须是非空数组')
    return { valid: false, errors }
  }

  if (!fieldMapping.xAxis) {
    errors.push('字段映射缺少 xAxis 配置')
  }

  if (!fieldMapping.series || fieldMapping.series.length === 0) {
    errors.push('字段映射缺少 series 配置')
  }

  // 检查第一行数据是否包含所需字段
  const firstRow = data[0]
  if (!(fieldMapping.xAxis in firstRow)) {
    errors.push(`数据中缺少 X轴字段: ${fieldMapping.xAxis}`)
  }

  fieldMapping.series.forEach((series, index) => {
    if (!series.name) {
      errors.push(`系列 ${index} 缺少名称`)
    }
    if (!series.field) {
      errors.push(`系列 ${index} 缺少字段名`)
    } else if (!(series.field in firstRow)) {
      errors.push(`数据中缺少字段: ${series.field}`)
    }
  })

  return { valid: errors.length === 0, errors }
}
