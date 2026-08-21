<script setup lang="ts">
import {
  BATCH_ALERT_CYCLE_OPTIONS,
  BATCH_ALERT_CYCLE_TYPE,
  BATCH_ALERT_DEFAULT_CONFIG,
  BATCH_ALERT_INVALID_PUSH_TIME
} from '../fieldCode'
import type { BatchAlertConfig, BatchAlertCycleFormOptions } from '../types'

defineOptions({
  name: 'BatchEventAlertCycleConfig'
})

interface Props {
  options: BatchAlertCycleFormOptions
  disabled?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false
})

const model = defineModel<BatchAlertConfig>({
  required: true
})

const cycleOptions = BATCH_ALERT_CYCLE_OPTIONS

/**
 * 切换周期类型时补齐默认日期和推送时间，避免表单残留空值导致保存失败。
 * @param cycleType 当前选择的预警周期
 */
const handleCycleChange = (cycleType: BatchAlertConfig['cycleType']) => {
  model.value.cycleType = cycleType

  if (!model.value.weekDay) {
    model.value.weekDay = BATCH_ALERT_DEFAULT_CONFIG.weekDay
  }

  if (!model.value.monthDay) {
    model.value.monthDay = BATCH_ALERT_DEFAULT_CONFIG.monthDay
  }

  if (!model.value.pushTime || model.value.pushTime === BATCH_ALERT_INVALID_PUSH_TIME) {
    model.value.pushTime = BATCH_ALERT_DEFAULT_CONFIG.pushTime
  }
}

/**
 * 当前行是否为选中态，用于控制行内控件的启用状态与样式表现。
 * @param cycleType 预警周期类型
 * @returns boolean
 */
const isActiveCycle = (cycleType: BatchAlertConfig['cycleType']) => {
  return model.value.cycleType === cycleType
}
</script>

<template>
  <div class="batch-alert-card">
    <div v-for="item in cycleOptions" :key="item.value" class="batch-alert-card__row">
      <el-radio
        :model-value="model.cycleType"
        :value="item.value"
        :disabled="props.disabled"
        @change="handleCycleChange(item.value as BatchAlertConfig['cycleType'])"
      >
        {{ item.label }}
      </el-radio>

      <el-select
        v-if="item.value === BATCH_ALERT_CYCLE_TYPE.WEEKLY"
        v-model="model.weekDay"
        :disabled="props.disabled || !isActiveCycle(BATCH_ALERT_CYCLE_TYPE.WEEKLY)"
        class="batch-alert-card__date"
      >
        <el-option
          v-for="option in options.weekOptions"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </el-select>

      <el-select
        v-if="item.value === BATCH_ALERT_CYCLE_TYPE.MONTHLY"
        v-model="model.monthDay"
        :disabled="props.disabled || !isActiveCycle(BATCH_ALERT_CYCLE_TYPE.MONTHLY)"
        class="batch-alert-card__date"
      >
        <el-option
          v-for="option in options.monthDayOptions"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </el-select>

      <el-select
        v-model="model.pushTime"
        :disabled="props.disabled || !isActiveCycle(item.value as BatchAlertConfig['cycleType'])"
        class="batch-alert-card__time"
      >
        <el-option
          v-for="option in options.timeOptions"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </el-select>
    </div>
  </div>
</template>

<style scoped lang="scss">
.batch-alert-card {
  width: 100%;
}

.batch-alert-card__row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.batch-alert-card__row + .batch-alert-card__row {
  margin-top: 16px;
}

.batch-alert-card__date {
  width: 120px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #1d2129;
}

.batch-alert-card__date--disabled {
  color: var(--el-disabled-text-color);
}

.batch-alert-card__time {
  flex: 1;
  flex-shrink: 0;
}

.batch-alert-card__text {
  flex: 1;
  min-width: 0;
  font-size: 14px;
  color: #1d2129;
}

:deep(.batch-alert-card__row .el-radio) {
  width: 58px;
  margin-right: 0 !important;
  flex-shrink: 0;
}

:deep(.el-select__wrapper) {
  background: #ffffff;
  width: 100%;
}
</style>
