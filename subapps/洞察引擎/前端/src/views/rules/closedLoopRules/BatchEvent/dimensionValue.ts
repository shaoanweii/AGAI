import type { BatchDimensionRow, BatchResourceLinkageValue } from './types'

/**
 * 维度值支持文本、数组和资源联动对象三种形态，这里统一判断是否已填写。
 * @param value 维度值
 * @returns boolean
 */
export const hasBatchDimensionValue = (value: BatchDimensionRow['value']) => {
  if (Array.isArray(value)) {
    return value.some(item => String(item || '').trim())
  }

  if (value && typeof value === 'object') {
    const currentValue = value as BatchResourceLinkageValue
    const level1 = Array.isArray(currentValue['1']) ? currentValue['1'].filter(Boolean) : []
    const level2 = Array.isArray(currentValue['2']) ? currentValue['2'].filter(Boolean) : []

    return level1.length > 0 || level2.length > 0
  }

  return !!String(value || '').trim()
}

/**
 * 级联多选字段统一转成字符串数组，便于约束校验和回退逻辑复用。
 * @param value 当前维度值
 * @returns string[]
 */
export const getBatchSelectedCascaderValues = (value: BatchDimensionRow['value']) => {
  if (!Array.isArray(value)) {
    return []
  }

  return value.filter(item => item !== undefined && item !== null).map(item => String(item))
}
