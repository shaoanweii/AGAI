<script setup lang="ts">
import type { DashboardStatCard } from '@/api/batchEvent/types'

defineOptions({
  name: 'BoxItem'
})

const props = defineProps<{
  card: DashboardStatCard
  bgcolor?: string
}>()

const emit = defineEmits<{
  click: [card: DashboardStatCard]
}>()

/**
 * 透传统计卡片点击数据，供父级带入列表页筛选。
 */
const handleClick = () => {
  emit('click', props.card)
}
</script>

<template>
  <div
    class="box-item"
    :style="{ background: props.bgcolor }"
    role="button"
    tabindex="0"
    @click="handleClick"
    @keydown.enter.prevent="handleClick"
  >
    <div class="text-h4">{{ props.card.count ?? 0 }}</div>
    <div class="text-body fw-400 box-item__name">{{ props.card.statusName || '-' }}</div>
    <div class="text-small fw-400">
      <span class="text-secondary">环比</span>
      <span class="ml-4 text-brand-secondary">{{ props.card.changeRate || '-' }}</span>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.box-item {
  min-width: 0;
  width: 100%;
  height: 92px;
  background: #e6f4fe;
  border-radius: 8px 8px 8px 8px;
  border: 1px solid #f0f0f0;
  text-align: center;
  padding: 8px;
  cursor: pointer;
  transition:
    transform 0.16s ease,
    box-shadow 0.16s ease;

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 6px 16px rgba(10, 13, 18, 0.08);
  }

  &:focus-visible {
    outline: 2px solid var(--brand-primary);
    outline-offset: 2px;
  }

  &__name {
    color: #425166;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  @media (max-width: 1200px) {
    padding: 8px 2px;

    .text-h4 {
      font-size: 16px;
    }

    .text-body,
    .text-small {
      font-size: 12px;
    }

    .ml-4 {
      margin-left: 2px;
    }
  }
}
</style>
