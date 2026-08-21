<script setup lang="ts">
import { computed, ref } from 'vue'

defineOptions({
  name: 'HPage'
})

interface HPageProps {
  /** 页面背景色 */
  backgroundColor?: string
}

const props = withDefaults(defineProps<HPageProps>(), {
  backgroundColor: '#F5F7FA',
})

const contentRef = ref<HTMLElement | null>(null)

defineExpose({
  getScrollContainer: () => contentRef.value
})

// 计算页面样式
const pageStyle = computed(() => ({
  backgroundColor: props.backgroundColor
}))

// 计算容器类名
const containerClass = computed(() => ({
  'f-page': true,
}))

</script>

<template>
  <div :class="containerClass" class="h-full flex-col" :style="pageStyle">
    <!-- 导航栏插槽 -->
    <div class="f-page__nav-bar">
      <slot name="nav-bar" />
    </div>

    <!-- 页面内容插槽 -->
    <div ref="contentRef" data-h5-scroll-container class="f-page__content flex-1 flex-auto overflow-auto">
      <slot />
    </div>
  </div>
</template>

<style scoped lang="scss">
.f-page {
  &__content {
    // 隐藏滚动条但保持滚动功能
    scrollbar-width: none; // Firefox
    -ms-overflow-style: none; // IE/Edge
    
    &::-webkit-scrollbar {
      display: none; // Chrome/Safari/Edge
    }
  }
}
</style>
