import type { BatchRuleRecord } from '@/views/rules/closedLoopRules/BatchEvent/types'
import {
  BATCH_DIMENSION_FIELD_CODE,
  getBatchDimensionMultipleLimit,
  isRequiredBatchDimensionField
} from '@/views/rules/closedLoopRules/BatchEvent/fieldCode'
import { canUseTopRankMetric } from '@/views/rules/closedLoopRules/BatchEvent/dimension'
import { hasBatchDimensionValue } from '@/views/rules/closedLoopRules/BatchEvent/dimensionValue'
import { hasTopRankMetric } from '@/views/rules/closedLoopRules/BatchEvent/metric'

/**
 * 批量规则的业务校验与基础表单校验分层处理。
 * 这里集中处理预警周期、维度配置、指标配置等跨字段约束。
 * @param form 批量规则表单响应式对象
 * @returns { validateBusinessForm: () => string }
 */
export function useBatchRuleBusinessValidation(form: BatchRuleRecord) {
  const dimensionLimitLabelMap: Record<string, string> = {
    [BATCH_DIMENSION_FIELD_CODE.DATA_SOURCE]: '数据源',
    [BATCH_DIMENSION_FIELD_CODE.CAR_SERIES]: '车系',
    [BATCH_DIMENSION_FIELD_CODE.EXPERIENCE_CODE]: '体验代码'
  }

  /**
   * 对复合模块做二次校验，保证预警周期、维度配置和指标配置都具备完整值。
   * @returns string | ''
   */
  const validateBusinessForm = () => {
    if (
      !form.alertConfig.cycleType ||
      !/^\d{2}:\d{2}$/.test(String(form.alertConfig.pushTime || ''))
    ) {
      return '请完整填写预警周期'
    }

    if (form.alertConfig.cycleType === 'weekly' && !form.alertConfig.weekDay) {
      return '请选择每周预警日期'
    }

    if (form.alertConfig.cycleType === 'monthly' && !form.alertConfig.monthDay) {
      return '请选择每月预警日期'
    }

    if (!form.dimensions.length) {
      return '请至少配置一条维度配置'
    }

    const dataSourceDimension = form.dimensions.find(item =>
      isRequiredBatchDimensionField(item.field)
    )

    // 必填维度先单独拦截，避免被通用维度提示掩盖；当前业务必填项为数据源。
    if (!dataSourceDimension) {
      return '请配置数据源'
    }

    if (
      !dataSourceDimension.wildcard ||
      !dataSourceDimension.valueType ||
      !hasBatchDimensionValue(dataSourceDimension.value)
    ) {
      return '请完整填写数据源'
    }

    const dataSourceLimit = getBatchDimensionMultipleLimit(
      dataSourceDimension.field,
      dataSourceDimension.statMode
    )

    if (
      dataSourceLimit > 0 &&
      Array.isArray(dataSourceDimension.value) &&
      dataSourceDimension.value.length > dataSourceLimit
    ) {
      return '数据源最多选择15项'
    }
    const overLimitDimension = form.dimensions.find(item => {
      const limit = getBatchDimensionMultipleLimit(item.field, item.statMode)
      return limit > 0 && Array.isArray(item.value) && item.value.length > limit
    })

    if (overLimitDimension) {
      const limit = getBatchDimensionMultipleLimit(
        overLimitDimension.field,
        overLimitDimension.statMode
      )
      const fieldLabel = dimensionLimitLabelMap[overLimitDimension.field] || '当前维度'
      return `${fieldLabel}最多选择${limit}项`
    }

    const invalidDimension = form.dimensions.some(item => {
      const hasValue = hasBatchDimensionValue(item.value)
      return !item.field || !item.wildcard || !item.valueType || !hasValue
    })

    if (invalidDimension) {
      return '请完整填写维度配置'
    }

    if (!form.metrics.length) {
      return '请至少配置一条指标配置'
    }

    const invalidMetric = form.metrics.some(item => {
      return !item.metric || !item.metricType || !item.wildcard || !item.valueType || !item.value
    })

    if (invalidMetric) {
      return '请完整填写指标配置'
    }

    if (hasTopRankMetric(form.metrics) && !canUseTopRankMetric(form.dimensions)) {
      return '仅当体验代码只选择一条数据或选择独立计算时，才可使用TOP排行指标'
    }

    return ''
  }

  return { validateBusinessForm }
}
