<script setup lang="ts">
import { ArrowDown, ArrowUp } from '@element-plus/icons-vue'
import { nextTick, onBeforeUnmount, onMounted, shallowRef, useTemplateRef } from 'vue'
import EventPriorityTag from '@/views/customerDirectEngage/singlePointEvent/components/EventPriorityTag.vue'
import type { BatchEventProcessingSummaryItem } from '../../types'

defineOptions({
  name: 'ProcessingProgressSummaryCard'
})

const props = defineProps<{
  title: string
  priorityTag: string
  businessTag: string
  isRejected: boolean
  rejectReason: string
  summaryItems: BatchEventProcessingSummaryItem[]
  focusTopics: string[]
}>()

const focusTopicsRef = useTemplateRef<HTMLDivElement>('focusTopicsRef')
const isTopicExpanded = shallowRef(false)
const showTopicToggle = shallowRef(false)

let resizeObserver: ResizeObserver | null = null

/**
 * 根据聚焦观点容器真实高度决定是否展示展开按钮。
 * 仅处理前端折叠展示，不涉及任何后端字段推导。
 */
const updateTopicToggleState = async () => {
  await nextTick()

  const container = focusTopicsRef.value
  if (!container) {
    showTopicToggle.value = false
    isTopicExpanded.value = false
    return
  }

  const collapsedHeight = 32
  showTopicToggle.value = container.scrollHeight > collapsedHeight + 1

  if (!showTopicToggle.value) {
    isTopicExpanded.value = false
  }
}

/**
 * 切换聚焦观点展开态。
 * 仅在内容超出单行时开放交互，避免出现空操作。
 */
const toggleTopics = () => {
  if (!showTopicToggle.value) return
  isTopicExpanded.value = !isTopicExpanded.value
}

onMounted(() => {
  void updateTopicToggleState()

  if (typeof ResizeObserver === 'undefined') {
    return
  }

  resizeObserver = new ResizeObserver(() => {
    void updateTopicToggleState()
  })

  if (focusTopicsRef.value) {
    resizeObserver.observe(focusTopicsRef.value)
  }
})

onBeforeUnmount(() => {
  resizeObserver?.disconnect()
  resizeObserver = null
})
</script>

<template>
  <div class="processing-progress-summary-card">
    <div class="processing-progress-summary-card__headline">
      <div class="processing-progress-summary-card__headline-title">{{ props.title }}</div>
      <EventPriorityTag
        v-if="props.priorityTag"
        :tag-name="props.priorityTag"
        :type="props.priorityTag.toLowerCase()"
      />
      <el-tooltip v-if="props.isRejected" :content="props.rejectReason" placement="top">
        <div class="processing-progress-summary-card__reject-tag">驳</div>
      </el-tooltip>
      <div v-if="props.businessTag" class="processing-progress-summary-card__scene-tag">
        <span class="processing-progress-summary-card__scene-dot"></span>
        <span>{{ props.businessTag }}</span>
      </div>
    </div>

    <div class="processing-progress-summary-card__card">
      <div class="processing-progress-summary-card__summary">
        <div
          v-for="item in props.summaryItems"
          :key="item.label"
          class="processing-progress-summary-card__summary-item"
        >
          <div class="processing-progress-summary-card__summary-label">{{ item.label }}</div>
          <div class="processing-progress-summary-card__summary-value" :title="item.value">
            {{ item.value }}
          </div>
        </div>
      </div>

      <div class="processing-progress-summary-card__topics">
        <div class="processing-progress-summary-card__topics-label">聚焦观点</div>

        <div
          ref="focusTopicsRef"
          class="processing-progress-summary-card__topics-list"
          :class="{ 'is-expanded': isTopicExpanded }"
        >
          <template v-if="props.focusTopics.length">
            <span
              v-for="(item, index) in props.focusTopics"
              :key="`${item}-${index}`"
              class="processing-progress-summary-card__topic-chip"
              :title="item"
            >
              {{ item }}
            </span>
          </template>
          <span v-else class="processing-progress-summary-card__topics-empty">-</span>
        </div>

        <div v-if="showTopicToggle" class="processing-progress-summary-card__topics-toggle-wrap">
          <button
            type="button"
            class="processing-progress-summary-card__topics-toggle"
            @click="toggleTopics"
          >
            <span>{{ isTopicExpanded ? '收起详情' : '展开更多' }}</span>
            <el-icon class="processing-progress-summary-card__topics-toggle-icon">
              <component :is="isTopicExpanded ? ArrowUp : ArrowDown" />
            </el-icon>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.processing-progress-summary-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.processing-progress-summary-card__headline {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
  flex-wrap: wrap;
}

.processing-progress-summary-card__headline-title {
  position: relative;
  padding-left: 12px;
  font-weight: 500;
  font-size: 16px;
  line-height: 24px;
  color: var(--processing-progress-text-primary);
  word-break: break-word;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    width: 3px;
    height: 24px;
    border-radius: 99px;
    background: var(--processing-progress-accent);
  }
}

.processing-progress-summary-card__reject-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 34px;
  padding: 1px 8px;
  border-radius: 4px;
  background: #fff7e8;
  color: #ff7d00;
  font-size: 14px;
  font-weight: 500;
  line-height: 22px;
  box-sizing: border-box;
  cursor: default;
}

.processing-progress-summary-card__scene-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0 10px;
  border-radius: 6px;
  background: #edf5ff;
  color: var(--processing-progress-accent);
  font-size: 14px;
  line-height: 24px;
}

.processing-progress-summary-card__scene-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

.processing-progress-summary-card__card {
  padding: 20px 24px 24px;
  border: 1px solid var(--processing-progress-border);
  border-radius: 4px;
  background: #f5f7fa;
}

.processing-progress-summary-card__summary {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 20px 24px;
}

.processing-progress-summary-card__summary-item {
  min-width: 0;
}

.processing-progress-summary-card__summary-label {
  margin-bottom: 10px;
  font-size: 14px;
  line-height: 22px;
  color: var(--processing-progress-text-secondary);
}

.processing-progress-summary-card__summary-value {
  font-size: 14px;
  line-height: 22px;
  color: var(--processing-progress-text-primary);
  word-break: break-word;
}

.processing-progress-summary-card__topics {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
}

.processing-progress-summary-card__topics-label {
  margin-bottom: 12px;
  font-size: 14px;
  line-height: 22px;
  color: var(--processing-progress-text-secondary);
}

.processing-progress-summary-card__topics-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  max-height: 32px;
  overflow: hidden;
  transition: max-height 0.2s ease;

  &.is-expanded {
    max-height: none;
  }
}

.processing-progress-summary-card__topic-chip {
  display: inline-flex;
  align-items: center;
  max-width: 100%;
  padding: 6px 14px;
  border-radius: 6px;
  background: var(--processing-progress-chip-bg);
  color: #4e5969;
  font-size: 14px;
  line-height: 20px;
  white-space: nowrap;
}

.processing-progress-summary-card__topics-empty {
  font-size: 14px;
  line-height: 22px;
  color: var(--processing-progress-text-secondary);
}

.processing-progress-summary-card__topics-toggle-wrap {
  display: inline-flex;
  justify-content: center;
  margin-top: 8px;
}

.processing-progress-summary-card__topics-toggle {
  padding: 2px 14px;
  border: 1px solid var(--processing-progress-border);
  background: #fff;
  color: #1f2733;
  font-size: 14px;
  line-height: 20px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  cursor: pointer;
  user-select: none;
  transition: all 0.3s ease;
  margin: 0 auto -1px;
  clip-path: polygon(10% 0%, 90% 0%, 100% 100%, 0% 100%);
}

.processing-progress-summary-card__topics-toggle-icon {
  font-size: 14px;
}

@media (max-width: 1360px) {
  .processing-progress-summary-card__summary {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .processing-progress-summary-card__card {
    padding: 16px 16px 22px;
  }

  .processing-progress-summary-card__summary {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 16px;
  }
}
</style>
