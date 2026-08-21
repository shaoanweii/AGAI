<script setup lang="ts">
import type { BatchEventProcessingStepItem } from '../../types'

defineOptions({
  name: 'ProcessingProgressSteps'
})

defineProps<{
  steps: BatchEventProcessingStepItem[]
}>()
</script>

<template>
  <div class="processing-progress-steps">
    <div
      v-for="(step, index) in steps"
      :key="step.label"
      class="processing-progress-steps__item"
      :class="{
        'is-completed': step.status === 'completed',
        'is-current': step.status === 'current',
        'is-pending': step.status === 'pending'
      }"
    >
      <div class="processing-progress-steps__icon">
        <SvgIcon
          v-if="step.status === 'completed'"
          name="success-line"
          width="24px"
          height="24px"
        />
        <SvgIcon
          v-else-if="step.status === 'current'"
          name="time-fill"
          width="24px"
          height="24px"
        />
        <div v-else class="processing-progress-steps__icon-placeholder">{{ index + 1 }}</div>
      </div>

      <div class="processing-progress-steps__label">{{ step.label }}</div>

      <div v-if="index < steps.length - 1" class="processing-progress-steps__line"></div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.processing-progress-steps {
  display: flex;
  align-items: center;
  width: 100%;
  min-width: 0;
  padding: 8px 16px 0;
  box-sizing: border-box;
}

.processing-progress-steps__item {
  display: flex;
  align-items: center;
  flex: 1 1 0;
  min-width: 0;

  &:last-child {
    flex: 0 1 auto;
  }
}

.processing-progress-steps__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: none;
  z-index: 1;
}

.processing-progress-steps__icon-placeholder {
  width: 24px;
  height: 24px;
  border: 2px solid rgba(0, 0, 0, 0.4);
  border-radius: 50%;
  background: #fff;
  color: rgba(0, 0, 0, 0.4);
  font-weight: 600;
  font-size: 16px;
  line-height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.processing-progress-steps__label {
  margin-left: 16px;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 16px;
  line-height: 24px;
  color: rgba(0, 0, 0, 0.4);
}

.processing-progress-steps__line {
  flex: 1 1 auto;
  min-width: 8px;
  height: 2px;
  margin: 0 12px;
  background: #dcdcdc;
}

.processing-progress-steps__item.is-completed {
  .processing-progress-steps__label {
    color: rgba(0, 0, 0, 0.9);
  }

  .processing-progress-steps__line {
    background: #1677ff;
  }
}

.processing-progress-steps__item.is-current {
  .processing-progress-steps__label {
    color: #1677ff;
    font-weight: 600;
  }

  .processing-progress-steps__line {
    background: #1677ff;
  }
}
</style>
