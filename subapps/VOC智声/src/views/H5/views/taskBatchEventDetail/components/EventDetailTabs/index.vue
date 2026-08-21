<script setup lang="ts">
import { computed } from 'vue'

/**
 * 事件详情页粘性 Tab 组件
 * - 只负责展示和点击交互，不直接处理滚动逻辑
 */

export type EventDetailTabKey = 'statistics' | 'customerVoice' | 'processProgress'

export interface EventDetailTabItem {
  key: EventDetailTabKey
  label: string
}

interface EventDetailTabsProps {
  tabs: EventDetailTabItem[]
}

const props = defineProps<EventDetailTabsProps>()

// 当前选中值，使用 v-model:active-key 双向绑定
const modelValue = defineModel<EventDetailTabKey>('activeKey', {
  default: 'statistics'
})

const emit = defineEmits<{
  (e: 'tab-click', key: EventDetailTabKey): void
}>()

// 兜底选中第一个 Tab，避免外部传入异常值
const activeKey = computed<EventDetailTabKey>(() => {
  const current = modelValue.value
  const matched = props.tabs.find(item => item.key === current)
  return (matched?.key || props.tabs[0]?.key || 'statistics') as EventDetailTabKey
})

/**
 * 处理 Tab 点击
 * - 更新 v-model
 * - 向父组件抛出 tab-click 事件
 */
const handleTabClick = (item: EventDetailTabItem) => {
  if (!item || item.key === activeKey.value) return
  modelValue.value = item.key
  emit('tab-click', item.key)
}
</script>

<template>
  <div class="event-detail-tabs">
    <div
      v-for="item in props.tabs"
      :key="item.key"
      class="tab-item"
      :class="{ 'is-active': item.key === activeKey }"
      @click="handleTabClick(item)"
    >
      <span class="tab-label">{{ item.label }}</span>
    </div>
  </div>
</template>

<style scoped lang="scss">
.event-detail-tabs {
  display: flex;
  align-items: center;
  justify-content: space-around;
  background: #348cff;
}

.tab-item {
  position: relative;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 7px 0;
  font-weight: 400;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.65);

  .tab-label {
    white-space: nowrap;
  }

  &.is-active {
    font-weight: 500;
    color: #ffffff;

    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 24px;
      height: 2px;
      background: #ffffff;
    }

    .tab-label {
      padding: 4px 10px;
    }
  }
}
</style>
