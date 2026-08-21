<script setup lang="ts">
import FCollapseSection from '@/components/UI/FCollapseSection/index.vue'
import type { BatchEventProcessingOperationLogItem } from '../../types'

defineOptions({
  name: 'ProcessingProgressOperationLog'
})

defineProps<{
  logs: BatchEventProcessingOperationLogItem[]
}>()
</script>

<template>
  <FCollapseSection title="操作记录" class="processing-progress-operation-log">
    <div class="processing-progress-operation-log__list">
      <div v-for="log in logs" :key="log.id" class="processing-progress-operation-log__item">
        <div class="processing-progress-operation-log__dot"></div>

        <div class="processing-progress-operation-log__content">
          <div class="processing-progress-operation-log__item-title">{{ log.title }}</div>

          <div v-if="log.details.length" class="processing-progress-operation-log__detail-card">
            <div
              v-for="(detail, detailIndex) in log.details"
              :key="`${log.id}-${detailIndex}`"
              class="processing-progress-operation-log__detail-item"
            >
              <span v-if="detail.label" class="processing-progress-operation-log__detail-label">
                {{ detail.label }}
              </span>
              <span class="processing-progress-operation-log__detail-value">{{
                detail.value
              }}</span>
            </div>
          </div>

          <div class="processing-progress-operation-log__meta">
            <span v-if="log.operator" class="processing-progress-operation-log__operator">
              {{ log.operator }}
            </span>
            <span v-if="log.operator" class="processing-progress-operation-log__divider">|</span>
            <span>{{ log.time }}</span>
          </div>
        </div>
      </div>
    </div>
  </FCollapseSection>
</template>

<style lang="scss" scoped>
.processing-progress-operation-log__list {
  display: flex;
  flex-direction: column;
}

.processing-progress-operation-log__item {
  position: relative;
  padding-left: 24px;
  padding-bottom: 20px;

  &:not(:last-child)::before {
    content: '';
    position: absolute;
    left: 3px;
    top: 22px;
    bottom: 0;
    width: 2px;
    background: #dcdcdc;
  }
}

.processing-progress-operation-log__dot {
  position: absolute;
  left: 0;
  top: 8px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #929aa6;
}

.processing-progress-operation-log__content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.processing-progress-operation-log__item-title {
  font-size: 16px;
  line-height: 24px;
  color: var(--processing-progress-text-primary);
}

.processing-progress-operation-log__detail-card {
  padding: 16px 18px;
  border-radius: 4px;
  background: #f5f7fa;
}

.processing-progress-operation-log__detail-item {
  font-size: 14px;
  line-height: 22px;
  color: rgba(0, 0, 0, 0.6);

  & + & {
    margin-top: 6px;
  }
}

.processing-progress-operation-log__detail-label {
  display: inline-block;
  margin-right: 16px;
  color: #86909c;
}

.processing-progress-operation-log__detail-value {
  color: #1d2129;
}

.processing-progress-operation-log__meta {
  font-size: 14px;
  line-height: 22px;
  color: #929aa6;
}

.processing-progress-operation-log__operator {
  margin-right: 8px;
}

.processing-progress-operation-log__divider {
  margin: 0 8px;
}
</style>
