<script setup lang="ts">
import ProcessSectionCard from './ProcessSectionCard.vue'
import type { BatchProcessStep, BatchProcessStepStatus } from './types'

defineOptions({
  name: 'BatchProgressStepsCard'
})

interface ProgressStepsCardProps {
  /** 处理进度步骤 */
  steps: BatchProcessStep[]
}

const props = defineProps<ProgressStepsCardProps>()

const iconNameMap: Record<BatchProcessStepStatus, string> = {
  completed: 'h5-step-finish',
  current: 'h5-step-time',
  pending: 'h5-step-pending'
}

const iconColorMap: Record<BatchProcessStepStatus, string> = {
  completed: '#1677FF',
  current: '#1677FF',
  pending: '#C9CDD4'
}

/**
 * 已完成节点到下一个节点之间使用蓝色连线。
 * @param step 当前步骤
 * @returns 连线是否高亮
 */
const isLineActive = (step: BatchProcessStep) => step.status === 'completed'
</script>

<template>
  <ProcessSectionCard title="处理进度" :collapsible="false">
    <div class="progress-steps">
      <div
        v-for="(step, index) in props.steps"
        :key="step.key"
        class="progress-step"
        :class="`is-${step.status}`"
      >
        <div class="progress-step__icon">
          <SvgIcon
            :name="iconNameMap[step.status]"
            width="18px"
            height="18px"
            :color="iconColorMap[step.status]"
          />
        </div>
        <div class="progress-step__label">{{ step.label }}</div>
        <div
          v-if="index < props.steps.length - 1"
          class="progress-step__line"
          :class="{ 'is-active': isLineActive(step) }"
        ></div>
      </div>
    </div>
  </ProcessSectionCard>
</template>

<style scoped lang="scss">
.progress-steps {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 12px 0 2px;
}

.progress-step {
  position: relative;
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.progress-step__icon {
  position: relative;
  z-index: 2;
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
}

.progress-step__label {
  margin-top: 6px;
  font-weight: 400;
  font-size: 12px;
  line-height: 18px;
  color: rgba(0, 0, 0, 0.9);
  text-align: center;
  white-space: nowrap;
}

.progress-step__line {
  position: absolute;
  top: 9px;
  right: -9px;
  z-index: 1;
  width: 18px;
  height: 2px;
  border-radius: 2px;
  background: #dcdfe6;
}

.progress-step__line.is-active {
  background: #1677ff;
}

.progress-step.is-pending {
  .progress-step__label {
    color: rgba(0, 0, 0, 0.35);
  }
}
</style>
