import { BATCH_DIMENSION_FIELD_CODE, BATCH_DIMENSION_STAT_MODE } from './fieldCode'
import type { BatchDimensionRow } from './types'

/**
 * 统一提取体验代码维度，供指标显示、业务校验等多处联动复用同一判断口径。
 * @param dimensions 当前表单维度配置
 * @returns BatchDimensionRow | undefined
 */
export const getExperienceCodeDimension = (dimensions: BatchDimensionRow[] = []) => {
  return dimensions.find(item => item.field === BATCH_DIMENSION_FIELD_CODE.EXPERIENCE_CODE)
}

/**
 * 统计当前维度已选条数。
 * 体验代码目前以级联多选数组存储，这里额外兼容字符串回填，避免历史详情值影响显示判断。
 * @param value 维度取值
 * @returns number
 */
export const getDimensionSelectedCount = (value: BatchDimensionRow['value']) => {
  if (Array.isArray(value)) {
    return value.filter(item => String(item || '').trim()).length
  }

  return String(value || '').trim() ? 1 : 0
}

/**
 * TOP 排行指标仅在以下两种场景开放：
 * 1. 体验代码只选中 1 条数据
 * 2. 体验代码统计方式为独立计算 alone
 * @param dimensions 当前表单维度配置
 * @returns boolean
 */
export const canUseTopRankMetric = (dimensions: BatchDimensionRow[] = []) => {
  const experienceCodeDimension = getExperienceCodeDimension(dimensions)

  if (!experienceCodeDimension) {
    return false
  }

  return (
    getDimensionSelectedCount(experienceCodeDimension.value) === 1 ||
    experienceCodeDimension.statMode === BATCH_DIMENSION_STAT_MODE.ALONE
  )
}
