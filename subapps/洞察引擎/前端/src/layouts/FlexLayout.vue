<template>
  <div class="flex-layout" :style="{ height: cHeight, minHeight: cMinHeight, ...style }">
    <div class="left" :style="{ width: cLeftWidth, ...leftStyle }">
      <slot name="left"></slot>
    </div>
    <div class="space"></div>
    <div class="right" :style="{ ...rightStyle }">
      <slot name="right"></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  leftWidth?: number | string
  height?: number | string
  minHeight?: number | string
  style?: Record<any, any>
  leftStyle?: Record<any, any>
  rightStyle?: Record<any, any>
}
const {
  leftWidth = 200,
  height = 200,
  minHeight = 300,
  style = {},
  leftStyle = {},
  rightStyle = {}
} = defineProps<Props>()

const cHeight = computed(() => {
  if (typeof height === 'number') {
    return `${height}px`
  }
  return height
})
const cLeftWidth = computed(() => {
  if (typeof leftWidth === 'number') {
    return `${leftWidth}px`
  }
  return leftWidth
})
const cMinHeight = computed(() => {
  if (typeof minHeight === 'number') {
    return `${minHeight}px`
  }
  return minHeight
})
</script>

<style lang="scss" scoped>
.flex-layout {
  display: flex;
  box-sizing: border-box;
  .left,
  .right {
    height: 100%;
    width: 100%;
    box-sizing: border-box;
    background-color: var(--color-white);
    border-radius: 4px;
  }
  .left {
  }
  .right {
    flex: 1;
    width: 0;
  }
  .space {
    width: 24px;
    background-color: var(--bgc-def);
    box-sizing: border-box;
  }
}
</style>
