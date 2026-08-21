<script setup lang="ts">
import { computed } from 'vue'

// 公私域切换选项
interface ScopeOption {
  /** 业务 key，用于接口入参 */
  key: string
  /** 展示文案 */
  value: string
}

defineOptions({ name: 'ChannelScopeTabsV1' })

const props = withDefaults(
  defineProps<{
    /** 选项列表，包含 key / value */
    options?: ScopeOption[]
  }>(),
  {
    options: () => [
      { key: '', value: '全域' },
      { key: '公域', value: '公域' },
      { key: '私域', value: '私域' }
    ]
  }
)

// 选中值，默认全域
const modelValue = defineModel<string>({ default: '' })

const emit = defineEmits<{
  /** 切换时会抛出当前选中的配置，方便外部拿到对应的 key / value */
  change: [option: ScopeOption]
}>()

// 当前激活的 key，未传入时兜底为默认选项
const activeKey = computed(() => {
  if (modelValue.value) return modelValue.value
  const first = props.options[0]
  return first ? first.key : ''
})
const handleClick = (option: ScopeOption) => {
  if (!option || option.key === activeKey.value) return
  modelValue.value = option.key
  emit('change', option)
}
</script>

<template>
  <div v-if="options && options.length" class="channel-scope-tabs">
    <div
      v-for="item in options"
      :key="item.key"
      class="tab-item"
      :class="{ 'is-active': item.key === activeKey }"
      @click="handleClick(item)"
    >
      {{ item.value }}
    </div>
  </div>
</template>

<style scoped lang="scss">
.channel-scope-tabs {
  display: inline-flex;
  align-items: center;
  background: #f2f3f5;
  border-radius: 4px;
}

.tab-item {
  // width: 45px;
  padding: 6px 8px;
  white-space: nowrap;
  text-align: center;
  font-weight: 400;
  font-size: 14px;
  color: #1f2733;
  cursor: pointer;
  user-select: none;
  transition:
    background-color 0.15s ease-in-out,
    color 0.15s ease-in-out,
    font-weight 0.15s ease-in-out;

  &:first-child {
    border-radius: 4px 0 0 4px !important;
  }
  &:last-child {
    border-radius: 0 4px 4px 0 !important;
  }
}

.tab-item.is-active {
  background: #e2f3fe;
  color: #0062ff;
  font-weight: 600;
}
</style>
