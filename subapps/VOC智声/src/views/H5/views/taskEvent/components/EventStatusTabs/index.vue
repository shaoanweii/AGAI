<script setup lang="ts">
import { computed } from 'vue'
import { eventStatusMeta } from '@h5/constants'
import type {
  EventStatusFilterKey,
  EventStatusFilterOption,
  EventStatusFilterEmits,
  EventStatusFilterProps
} from './types'

defineOptions({
  name: 'EventStatusTabs'
})

// 组件入参
const props = withDefaults(defineProps<EventStatusFilterProps>(), {
  disabled: false
})

// 选中值，默认“全部”（使用空字符串表示）
const modelValue = defineModel<EventStatusFilterKey>({
  default: ''
})

const emit = defineEmits<EventStatusFilterEmits>()

// 默认状态选项（统一读取 constants 配置，保证分组/文案一致）
const defaultStatusTabs: EventStatusFilterOption[] = [{ key: '', label: '全部' }, ...eventStatusMeta]

// 当前可用的状态选项，支持调用方在不改变状态值的前提下覆盖展示文案。
const statusTabs = computed<EventStatusFilterOption[]>(() => {
  return props.options?.length ? props.options : defaultStatusTabs
})

// 当前激活 key，兜底为“全部”（空字符串）
const activeKey = computed<EventStatusFilterKey>(() => {
  const current = modelValue.value
  const matched = statusTabs.value.find(item => item.key === current)
  return matched ? matched.key : ''
})

/**
 * 处理状态点击
 * - 支持 v-model 双向绑定
 * - 选中变化时向父组件触发 change 事件
 */
const handleClick = (option: EventStatusFilterOption) => {
  if (props.disabled) return
  if (!option || option.key === activeKey.value) return

  modelValue.value = option.key
  emit('change', option)
}
</script>

<template>
  <div class="event-status-tabs" :class="{ 'is-disabled': props.disabled }">
    <div
      v-for="item in statusTabs"
      :key="item.key"
      class="status-tab-item"
      :class="{ 'is-active': item.key === activeKey }"
      @click="handleClick(item)"
    >
      {{ item.label }}
    </div>
  </div>
</template>

<style scoped lang="scss">
.event-status-tabs {
  display: inline-flex;
  align-items: center;
  padding: 16px 0;
  gap: 8px;

  &.is-disabled {
    opacity: 0.6;
    cursor: not-allowed;

    .status-tab-item {
      cursor: not-allowed;
    }
  }
}

.status-tab-item {
  min-width: 59px;
  padding: 3px 0;
  text-align: center;
  font-weight: 400;
  font-size: 12px;
  color: #222229;
  background: #F8F8F9;
  border-radius: 2px;
  transition: background-color 0.15s ease-in-out,
  color 0.15s ease-in-out,
  border-color 0.15s ease-in-out,
  box-shadow 0.15s ease-in-out;

  &:last-child {
    margin-right: 0;
  }

  &.is-active {
    color: #1677ff;
    background: #EAF3FF;
    border: 1px solid #1677FF;
    border-radius: 2px 2px 2px 2px;
  }
}
</style>
