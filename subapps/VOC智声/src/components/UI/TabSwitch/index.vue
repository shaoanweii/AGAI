<template>
  <div class="flex items-center">
    <div
      v-for="(tab, index) in tabs"
      :key="tab.value"
      class="cursor-point"
      :class="[{ untap: modelValue !== tab.value }, index > 0 ? 'ml-24' : '']"
      @click="handleTabClick(tab.value)"
    >
      {{ tab.label }}
    </div>
  </div>
</template>

<script setup lang="ts">
import type { TabItem } from './types'

interface Props {
  modelValue: string | number
  tabs: TabItem[]
}

interface Emits {
  (e: 'update:modelValue', value: string | number): void
  (e: 'change', value: string | number): void
}

defineProps<Props>()
const emit = defineEmits<Emits>()

const handleTabClick = (value: string | number) => {
  emit('update:modelValue', value)
  emit('change', value)
}
</script>

<style scoped>
.cursor-point {
  cursor: pointer;
  color: #1f2733;
  font-weight: 500;
  transition: all 0.3s;
}

.untap {
  color: #929aa6;
  font-weight: 400;
}
</style>
