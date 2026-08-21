<script setup lang="ts">
import { computed } from 'vue'
import type { ImportValidateStep } from './types'

defineOptions({
  name: 'ImportValidateStepProgress'
})

const props = withDefaults(
  defineProps<{
    steps: ImportValidateStep[]
    activeStep?: number
  }>(),
  {
    activeStep: 0
  }
)

/**
 * 按显式状态或当前激活位置推导步骤状态，未传状态时仍保持基础进度展示。
 */
const resolveStepStatus = (step: ImportValidateStep, index: number) => {
  if (step.status) return step.status
  if (index < active.value) return 'success'
  if (index === active.value) return 'process'
  return 'wait'
}

const active = computed(() => Math.max(0, props.activeStep))
</script>

<template>
  <div class="import-validate-step-progress">
    <div
      v-for="(step, index) in steps"
      :key="step.key"
      class="import-validate-step-progress__item"
      :class="`is-${resolveStepStatus(step, index)}`"
    >
      <div class="import-validate-step-progress__axis">
        <div class="import-validate-step-progress__icon">{{ index + 1 }}</div>
        <div v-if="index < steps.length - 1" class="import-validate-step-progress__line" />
      </div>

      <div class="import-validate-step-progress__content">
        <slot :step="step" :index="index" :status="resolveStepStatus(step, index)">
          <div v-if="step.title" class="import-validate-step-progress__title">{{ step.title }}</div>
        </slot>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.import-validate-step-progress {
  width: 100%;
}

.import-validate-step-progress__item {
  display: flex;
  align-items: stretch;

  &.is-process,
  &.is-finish,
  &.is-success {
    .import-validate-step-progress__icon {
      color: #fff;
      background: #1677ff;
      border-color: #1677ff;
    }
  }

  &.is-error {
    .import-validate-step-progress__icon {
      color: #fff;
      background: #f53f3f;
      border-color: #f53f3f;
    }
  }
}

.import-validate-step-progress__axis {
  display: flex;
  flex: 0 0 24px;
  flex-direction: column;
  align-items: center;
}

.import-validate-step-progress__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  font-size: 14px;
  font-weight: 400;
  color: #5f6a7a;
  background: #c9ced6;
  border: 1px solid #e5e6eb;
  border-radius: 50%;
  box-sizing: border-box;
}

.import-validate-step-progress__line {
  flex: 1 1 auto;
  width: 1px;
  min-height: 18px;
  background-color: #ebedf0;
}

.import-validate-step-progress__content {
  flex: 1 1 auto;
  min-width: 0;
}

.import-validate-step-progress__title {
  min-height: 32px;
  line-height: 32px;
  color: #1d2129;
}
</style>
