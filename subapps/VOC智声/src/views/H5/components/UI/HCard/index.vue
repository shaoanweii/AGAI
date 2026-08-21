<script setup lang="ts">
import { computed } from 'vue'

defineOptions({
  name: 'Card'
})

const { title, height = 'auto' } = defineProps<{
  title?: string
  height?: string
}>()

const bodyHeight = computed(() => {
  if (height === 'auto') {
    return height
  }
  if (title) {
    return `calc(${height} - 20px - 24px)`
  } else {
    return `calc(${height} - 24px)`
  }
})
</script>

<template>
  <div class="card" :style="{ height }">
    <div class="card-header flex-between flex-y-center">
      <div class="flex-y-center" style="overflow: hidden;">
        <div v-if="title" class="card-title">{{ title }}</div>
        <slot v-if="$slots.left" name="left"></slot>
      </div>
      <slot v-if="$slots.right" name="right"></slot>
    </div>

    <div class="card-body" :style="{ height: bodyHeight }">
      <slot></slot>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.card {
  background: #ffffff;
  border-radius: 8px 8px 8px 8px;
  padding: 12px;

  .card-title {
    font-weight: 500;
    font-size: 14px;
    line-height: 20px;
    color: #1f2733;
  }

  .card-body {
  }
}
</style>
