<script setup lang="ts">
import { computed } from 'vue'
import { BATCH_METRIC_FIELD_CODE } from '../fieldCode'
import type { BatchMetricConfigOptions, BatchMetricLogic, BatchMetricRow } from '../types'
import {
  getBatchMetricTypeOptions,
  getBatchMetricUnit,
  getBatchMetricValueTypeOptions,
  getBatchMetricWildcardOptions
} from '../metric'

defineOptions({
  name: 'BatchEventMetricConfig'
})

interface Props {
  options: BatchMetricConfigOptions
  canUseTopRank?: boolean
  disabled?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  canUseTopRank: false,
  disabled: false
})

const metricLogic = defineModel<BatchMetricLogic>('logic', {
  required: true
})

const model = defineModel<BatchMetricRow[]>({
  required: true
})

const METRIC_VALUE_FORMAT_INTEGER = '正整数'
const METRIC_VALUE_FORMAT_PERCENT = '百分数'

/**
 * 详情回填里的 unit 可能沿用旧口径，这里统一收敛格式值，避免展示判断被历史值干扰。
 * @param valueFormat 原始格式值
 * @returns string
 */
const normalizeMetricValueFormat = (valueFormat: string) => {
  const normalizedValue = String(valueFormat || '').trim()

  if (
    normalizedValue === METRIC_VALUE_FORMAT_PERCENT ||
    normalizedValue === '%' ||
    normalizedValue === '百分比'
  ) {
    return METRIC_VALUE_FORMAT_PERCENT
  }

  if (normalizedValue === METRIC_VALUE_FORMAT_INTEGER) {
    return METRIC_VALUE_FORMAT_INTEGER
  }

  return normalizedValue
}

/**
 * 百分数允许录入负号和小数点，这里保留输入中的中间态，避免用户刚输入时被强制截断。
 * @param value 原始输入值
 * @returns string
 */
const normalizePercentValue = (value: string) => {
  const rawValue = String(value || '').replace(/[^\d.-]/g, '')
  const isNegative = rawValue.startsWith('-')
  const unsignedValue = rawValue.replace(/-/g, '')
  const firstDotIndex = unsignedValue.indexOf('.')
  const normalizedBody =
    firstDotIndex === -1
      ? unsignedValue
      : `${unsignedValue.slice(0, firstDotIndex + 1)}${unsignedValue
          .slice(firstDotIndex + 1)
          .replace(/\./g, '')}`

  if (!normalizedBody) {
    return isNegative ? '-' : ''
  }

  if (normalizedBody === '.') {
    return isNegative ? '-0.' : '0.'
  }

  const [integerPart = '', decimalPart] = normalizedBody.split('.')
  const normalizedInteger = integerPart === '' ? '' : integerPart.replace(/^0+(?=\d)/g, '') || '0'

  if (decimalPart === undefined) {
    return `${isNegative ? '-' : ''}${normalizedInteger}`
  }

  // 百分数输入统一限制为最多两位小数，避免规则值精度在前后端之间出现歧义。
  return `${isNegative ? '-' : ''}${normalizedInteger || '0'}.${decimalPart.slice(0, 2)}`
}

/**
 * 指标值输入格式完全依赖当前条件的 valueFormat，这里统一做输入清洗，避免非法字符进入表单。
 * @param value 原始输入值
 * @param valueFormat 指标条件返回的格式
 * @returns string
 */
const normalizeMetricValueByFormat = (value: string, valueFormat: string) => {
  if (valueFormat === METRIC_VALUE_FORMAT_INTEGER) {
    return String(value || '')
      .replace(/\D/g, '')
      .replace(/^0{1,}/g, '')
  }

  if (valueFormat === METRIC_VALUE_FORMAT_PERCENT) {
    return normalizePercentValue(value)
  }

  return String(value || '')
}

const availableMetricOptions = computed(() => {
  return props.options.metricFieldOptions.filter(item => {
    return item.value !== BATCH_METRIC_FIELD_CODE.TOP_RANK || props.canUseTopRank
  })
})

const createRow = (): BatchMetricRow => ({
  id: `metric-${Date.now()}-${Math.random().toString(16).slice(2, 6)}`,
  metric: '',
  metricType: '',
  wildcard: '',
  valueType: '',
  value: '',
  unit: ''
})

const getMetricTypeOptions = (row: BatchMetricRow) => {
  return getBatchMetricTypeOptions(props.options, row.metric)
}

const getWildcardOptions = (row: BatchMetricRow) => {
  return getBatchMetricWildcardOptions(props.options, row.metric, row.metricType)
}

/**
 * 指标切换后立即按当前指标回填默认组合，保证新增行先展示空白，选中后再进入完整态。
 * @param row 当前指标行
 */
const handleMetricChange = (row: BatchMetricRow) => {
  if (!row.metric) {
    row.metricType = ''
    row.wildcard = ''
    row.valueType = ''
    row.value = ''
    row.unit = ''
    return
  }

  row.metricType = getMetricTypeOptions(row)[0]?.value || ''
  row.wildcard = getWildcardOptions(row)[0]?.value || ''
  row.valueType = getValueTypeOptions(row)[0]?.value || ''
  row.unit = getMetricUnit(row)
  row.value = ''
}

/**
 * 指标类型改变后，自动回填对应的值类型默认项，避免出现不存在的组合。
 * @param row 当前指标行
 */
const handleMetricTypeChange = (row: BatchMetricRow) => {
  row.wildcard = getWildcardOptions(row)[0]?.value || ''
  row.valueType = getValueTypeOptions(row)[0]?.value || ''
  row.unit = getMetricUnit(row)
  row.value = ''
}

const getValueTypeOptions = (row: BatchMetricRow) => {
  return getBatchMetricValueTypeOptions(props.options, row.metric, row.metricType)
}

/**
 * 操作符不参与 valueFormat 计算，切换时仅同步展示单位，保留用户已录入阈值。
 * @param row 当前指标行
 */
const handleWildcardChange = (row: BatchMetricRow) => {
  row.unit = getMetricUnit(row)
}

/**
 * 值类型切换后要重新匹配 valueFormat，并清空旧输入，避免不同格式之间串值。
 * @param row 当前指标行
 */
const handleValueTypeChange = (row: BatchMetricRow) => {
  row.unit = getMetricUnit(row)
  row.value = ''
}

/**
 * 根据当前条件的 valueFormat 实时清洗输入值，保证正整数和百分数的录入约束生效。
 * @param row 当前指标行
 * @param value 输入框最新值
 */
const handleValueInput = (row: BatchMetricRow, value: string) => {
  row.value = normalizeMetricValueByFormat(value, resolveMetricValueFormat(row))
}

/**
 * 百分数在失焦时统一补齐到两位小数，保证展示口径和最终提交值一致。
 * @param row 当前指标行
 */
const handleValueBlur = (row: BatchMetricRow) => {
  const valueFormat = resolveMetricValueFormat(row)
  if (valueFormat !== METRIC_VALUE_FORMAT_PERCENT) {
    return
  }

  const normalizedValue = normalizeMetricValueByFormat(row.value, valueFormat)
  if (!normalizedValue || normalizedValue === '-') {
    row.value = ''
    return
  }

  const numericValue = Number.parseFloat(normalizedValue)
  row.value = Number.isNaN(numericValue) ? '' : numericValue.toFixed(2)
}

const getMetricUnit = (row: BatchMetricRow) => {
  return normalizeMetricValueFormat(
    getBatchMetricUnit(props.options, row.metric, row.metricType, row.valueType)
  )
}

/**
 * 指标格式优先取当前联动配置，其次兜底详情回填值，避免旧数据把最新配置判断覆盖掉。
 * @param row 当前指标行
 * @returns string
 */
const resolveMetricValueFormat = (row: BatchMetricRow) => {
  const mappedValueFormat = getMetricUnit(row)
  if (mappedValueFormat) {
    return mappedValueFormat
  }

  return normalizeMetricValueFormat(row.unit)
}

/**
 * 根据 valueFormat 提示移动端键盘类型，减少无效字符录入。
 * @param row 当前指标行
 * @returns string
 */
const getMetricInputMode = (row: BatchMetricRow) => {
  const valueFormat = resolveMetricValueFormat(row)

  if (valueFormat === METRIC_VALUE_FORMAT_INTEGER) {
    return 'numeric'
  }

  if (valueFormat === METRIC_VALUE_FORMAT_PERCENT) {
    return 'decimal'
  }

  return 'text'
}

/**
 * 百分数后缀只影响输入框视觉提示，真实提交值仍保持纯数字字符串，避免污染接口参数。
 * @param row 当前指标行
 * @returns boolean
 */
const isPercentMetricRow = (row: BatchMetricRow) => {
  return resolveMetricValueFormat(row) === METRIC_VALUE_FORMAT_PERCENT
}

const addRow = () => {
  model.value.push(createRow())
}

const removeRow = (index: number) => {
  model.value.splice(index, 1)
}
</script>

<template>
  <div class="batch-metric-card">
    <el-radio-group v-model="metricLogic" :disabled="props.disabled">
      <el-radio
        v-for="item in props.options.metricLogicOptions"
        :key="item.value"
        :value="item.value"
        :label="item.label"
      >
        {{ item.label }}
      </el-radio>
    </el-radio-group>

    <div v-for="(row, index) in model" :key="row.id" class="batch-metric-card__row">
      <el-select
        v-model="row.metric"
        class="w-100"
        :disabled="props.disabled"
        @change="handleMetricChange(row)"
      >
        <el-option
          v-for="item in availableMetricOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>

      <el-select
        v-model="row.metricType"
        class="w-80"
        placeholder="数值"
        :disabled="props.disabled"
        @change="handleMetricTypeChange(row)"
      >
        <el-option
          v-for="item in getMetricTypeOptions(row)"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>

      <el-select
        v-model="row.wildcard"
        class="w-64"
        placeholder=">"
        :disabled="props.disabled"
        @change="handleWildcardChange(row)"
      >
        <el-option
          v-for="item in getWildcardOptions(row)"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>

      <el-select
        v-model="row.valueType"
        class="w-104"
        :disabled="props.disabled"
        @change="handleValueTypeChange(row)"
      >
        <el-option
          v-for="item in getValueTypeOptions(row)"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>

      <el-input
        v-model.trim="row.value"
        :disabled="props.disabled"
        :inputmode="getMetricInputMode(row)"
        placeholder="请输入"
        :class="['flex-1', 'metric-input', { 'metric-input--percent': isPercentMetricRow(row) }]"
        @input="handleValueInput(row, $event)"
        @blur="handleValueBlur(row)"
      >
        <template #suffix>
          <span v-if="isPercentMetricRow(row)" class="metric-input-suffix">%</span>
        </template>
      </el-input>

      <el-button v-if="!props.disabled" link @click="removeRow(index)">
        <el-icon><Close /></el-icon>
      </el-button>
    </div>

    <div>
      <el-button v-if="!props.disabled" type="primary" @click="addRow">
        <el-icon class="mr-4"><Plus /></el-icon>
        添加指标配置
      </el-button>
    </div>
  </div>
</template>

<style scoped lang="scss">
.batch-metric-card {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.batch-metric-card__row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.flex-1 {
  flex: 1 1 0;
}

.w-64 {
  width: 64px;
}

.w-80 {
  width: 80px;
}

.w-86 {
  width: 86px;
}

.w-100 {
  width: 100px;
}

.w-120 {
  width: 120px;
}

.w-104 {
  width: 104px;
}

.w-140 {
  width: 140px;
}

.mr-4 {
  margin-right: 4px;
}

.metric-input-suffix {
  color: var(--el-text-color-secondary);
}

.metric-input:not(.metric-input--percent) {
  :deep(.el-input__suffix) {
    display: none;
  }
}
</style>
