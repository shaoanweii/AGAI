<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, ref, watch } from 'vue'

defineOptions({
  name: 'HBackToAnchor'
})

interface BackToAnchorProps {
  /**
   * 需要返回到的位置锚点元素
   */
  targetEl?: HTMLElement | null
  /**
   * 滚动容器选择器，默认使用 HPage 的内容容器
   */
  scrollContainerSelector?: string
  /**
   * 控制按钮显示的触发阈值，单位 px
   * 当锚点相对容器顶部位置小于该值时显示按钮
   */
  showThreshold?: number
  /**
   * 滚动到锚点时的顶部预留偏移量，单位 px
   */
  scrollOffset?: number
  /**
   * 按钮右侧距离，单位 px
   */
  right?: number
  /**
   * 按钮底部距离，单位 px
   */
  bottom?: number
  /**
   * 默认按钮文案
   */
  text?: string
}

const props = withDefaults(defineProps<BackToAnchorProps>(), {
  scrollContainerSelector: '.f-page__content',
  showThreshold: -20,
  scrollOffset: 8,
  right: 16,
  bottom: 118,
  text: '返回'
})

const emit = defineEmits<{
  (e: 'click'): void
  (e: 'visible-change', visible: boolean): void
}>()

// 是否展示按钮
const visible = ref(false)

let scrollContainer: HTMLElement | null = null

// 根据当前锚点和容器位置计算是否需要显示按钮
const updateVisible = () => {
  if (!scrollContainer || !props.targetEl) {
    if (visible.value) {
      visible.value = false
      emit('visible-change', false)
    }
    return
  }

  const containerTop = scrollContainer.getBoundingClientRect().top
  const anchorTop = props.targetEl.getBoundingClientRect().top
  const shouldShow = anchorTop - containerTop < props.showThreshold

  if (shouldShow !== visible.value) {
    visible.value = shouldShow
    emit('visible-change', shouldShow)
  }
}

// 滚动事件回调
const handleScroll = () => {
  updateVisible()
}

// 平滑滚动到锚点位置
const scrollToAnchor = () => {
  if (!scrollContainer || !props.targetEl) return
  const containerTop = scrollContainer.getBoundingClientRect().top
  const anchorTop = props.targetEl.getBoundingClientRect().top
  const to = scrollContainer.scrollTop + (anchorTop - containerTop) - props.scrollOffset
  scrollContainer.scrollTo({ top: to, behavior: 'smooth' })
  emit('click')
}

onMounted(() => {
  // 延迟到下一帧，确保 DOM 已经渲染完成
  nextTick(() => {
    scrollContainer = document.querySelector(
      props.scrollContainerSelector
    ) as HTMLElement | null
    if (scrollContainer) {
      scrollContainer.addEventListener('scroll', handleScroll, { passive: true })
      // 初次挂载时根据当前位置计算一次
      updateVisible()
    }
  })
})

onUnmounted(() => {
  // 组件销毁时解绑事件，避免内存泄漏
  if (scrollContainer) {
    scrollContainer.removeEventListener('scroll', handleScroll)
  }
})

// 当父组件的锚点元素发生变化时，重新计算一次显隐状态
watch(
  () => props.targetEl,
  () => {
    nextTick(() => {
      updateVisible()
    })
  }
)
</script>

<template>
  <div
    v-if="visible"
    class="h-back-to-anchor flex-center"
    :style="{ right: right + 'px', bottom: bottom + 'px' }"
    @click="scrollToAnchor"
  >
    <!-- 默认内容：向上三角形 + 文案；也支持通过插槽自定义 -->
    <slot>
      <div class="flex-col flex-center">
        <div class="triangle-up"></div>
        <div class="mt-4 back-to-text">{{ text }}</div>
      </div>
    </slot>
  </div>
</template>

<style scoped lang="scss">
.h-back-to-anchor {
  position: fixed;
  z-index: 1000;
  width: 44px;
  height: 44px;
  background: #4a9eff;
  box-shadow: 0px 1px 4px 0px rgba(12, 12, 13, 0.05),
  0px 1px 4px 0px rgba(12, 12, 13, 0.1);
  border-radius: 50%;

  .back-to-text {
    font-weight: 400;
    font-size: 12px;
    color: #eaf3ff;
  }

  /* 实心向上三角形 */
  .triangle-up {
    width: 0;
    height: 0;
    border-left: 4px solid transparent;
    border-right: 4px solid transparent;
    border-bottom: 4px solid #ffffff;
    margin-bottom: 2px;
  }
}
</style>

