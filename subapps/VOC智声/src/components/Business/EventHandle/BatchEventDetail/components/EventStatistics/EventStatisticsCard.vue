<script setup lang="ts">
import { ArrowDown, ArrowUp } from '@element-plus/icons-vue'
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import type { BatchEventStatisticsSummaryItem } from '../../types'

/**
 * 事件统计卡片组件。
 * 承载摘要信息展示与聚焦观点展开/收起交互。
 */
defineOptions({
  name: 'EventStatisticsCard'
})

const props = defineProps<{
  summaryItems: BatchEventStatisticsSummaryItem[]
  focusTopics: string[]
}>()

const focusTopicsRef = ref<HTMLDivElement | null>(null)
const isTopicExpanded = ref(false)
const showTopicToggle = ref(false)

let resizeObserver: ResizeObserver | null = null

/**
 * 根据聚焦观点容器的真实高度决定是否展示展开按钮。
 * 这里只处理 UI 折叠态，不做任何字段解析。
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
 * 切换聚焦观点展开/收起状态。
 * 仅在存在超出一行内容时允许切换，避免无效交互。
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
  <div class="event-statistics-card">
    <div class="event-statistics-card__summary">
      <div
        v-for="item in props.summaryItems"
        :key="item.label"
        class="event-statistics-card__summary-item"
      >
        <div class="event-statistics-card__summary-label">{{ item.label }}</div>
        <div class="event-statistics-card__summary-value" :title="item.value">{{ item.value }}</div>
      </div>
    </div>

    <div class="event-statistics-card__topics">
      <div class="event-statistics-card__topics-label">聚焦观点</div>

      <div
        ref="focusTopicsRef"
        class="event-statistics-card__topics-list"
        :class="{ 'is-expanded': isTopicExpanded }"
      >
        <template v-if="props.focusTopics.length">
          <span
            v-for="(item, index) in props.focusTopics"
            :key="`${item}-${index}`"
            class="event-statistics-card__topic-chip"
            :title="item"
          >
            {{ item }}
          </span>
        </template>
        <span v-else class="event-statistics-card__topics-empty">-</span>
      </div>

      <div v-if="showTopicToggle" class="event-statistics-card__topics-toggle-wrapper">
        <button
          type="button"
          class="event-statistics-card__topics-toggle"
          @click="toggleTopics"
        >
          <span class="event-statistics-card__topics-toggle-text">
            {{ isTopicExpanded ? '收起详情' : '展开详情' }}
          </span>
          <el-icon class="event-statistics-card__topics-toggle-icon">
            <component :is="isTopicExpanded ? ArrowUp : ArrowDown" />
          </el-icon>
        </button>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.event-statistics-card {
  padding: 20px 24px 24px;
  border: 1px solid var(--event-statistics-border);
  background: #f5f7fa;
  border-radius: 4px 4px 4px 4px;
}

.event-statistics-card__summary {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 20px 24px;
}

.event-statistics-card__summary-item {
  min-width: 0;
}

.event-statistics-card__summary-label {
  margin-bottom: 10px;
  font-size: 14px;
  line-height: 22px;
  font-weight: 400;
  color: var(--event-statistics-text-secondary);
}

.event-statistics-card__summary-value {
  font-size: 14px;
  line-height: 22px;
  font-weight: 400;
  color: var(--event-statistics-text-primary);
  word-break: break-word;
}

.event-statistics-card__topics {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
}

.event-statistics-card__topics-label {
  margin-bottom: 12px;
  font-size: 14px;
  line-height: 22px;
  color: var(--event-statistics-text-secondary);
}

.event-statistics-card__topics-list {
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

.event-statistics-card__topic-chip {
  display: inline-flex;
  align-items: center;
  max-width: 100%;
  padding: 6px 14px;
  border-radius: 6px;
  background: var(--event-statistics-chip-bg);
  color: #4e5969;
  font-size: 14px;
  line-height: 20px;
  white-space: nowrap;
}

.event-statistics-card__topics-empty {
  font-size: 14px;
  line-height: 22px;
  color: var(--event-statistics-text-secondary);
}

.event-statistics-card__topics-toggle-wrapper {
  display: inline-flex;
  justify-content: center;
  margin-top: 8px;
  margin-bottom: -25px;
}

.event-statistics-card__topics-toggle {
  padding: 2px 14px;
  border: 1px solid var(--event-statistics-border);
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

.event-statistics-card__topics-toggle-text {
  font-weight: 400;
}

.event-statistics-card__topics-toggle-icon {
  font-size: 14px;
}

@media (max-width: 1440px) {
  .event-statistics-card__summary {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 960px) {
  .event-statistics-card {
    padding: 18px 16px 22px;
  }

  .event-statistics-card__summary {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 16px;
  }
}

@media (max-width: 640px) {
  .event-statistics-card__summary {
    grid-template-columns: minmax(0, 1fr);
  }

  .event-statistics-card__topic-chip {
    max-width: 100%;
    white-space: normal;
  }
}
</style>
