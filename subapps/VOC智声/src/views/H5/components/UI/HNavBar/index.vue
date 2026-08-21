<script setup lang="ts">
import { computed } from 'vue'
import { NavBar } from 'vant'
import 'vant/es/nav-bar/style'
import type { HNavBarProps, HNavBarEmits } from './types'

defineOptions({
  name: 'HNavBar'
})

const props = withDefaults(defineProps<HNavBarProps>(), {
  title: '',
  leftArrow: true,
  leftText: '',
  rightText: '',
  backgroundColor: '#ffffff',
  titleColor: '#1f2733',
  safeAreaInsetTop: true,
  height: '46px',
  fixed: false,
  zIndex: 1,
  border: false,
  hidden: false
})

const emit = defineEmits<HNavBarEmits>()

// 计算样式
const navBarStyle = computed(() => ({
  '--van-nav-bar-background': props.backgroundColor,
  '--van-nav-bar-title-text-color': props.titleColor,
  '--van-nav-bar-text-color': props.titleColor,
  '--van-nav-bar-icon-color': props.titleColor,
  '--van-nav-bar-height': props.height,
  '--van-nav-bar-z-index': props.zIndex.toString(),
  '--van-border-width': 0 ,
}))

// 计算类名
const navBarClass = computed(() => ({
  'f-nav-bar': true,
  'f-nav-bar--border': props.border,
  'f-nav-bar--hidden': props.hidden
}))

// 处理左侧点击事件
const handleClickLeft = () => {
  emit('click-left')
}

// 处理右侧点击事件
const handleClickRight = () => {
  emit('click-right')
}

// 处理标题点击事件
const handleClickTitle = () => {
  emit('click-title')
}
</script>

<template>
  <NavBar
    :class="navBarClass"
    :style="navBarStyle"
    :title="title"
    :left-text="leftText"
    :right-text="rightText"
    :left-arrow="leftArrow"
    :fixed="fixed"
    :safe-area-inset-top="safeAreaInsetTop"
    @click-left="handleClickLeft"
    @click-right="handleClickRight"
    @click-title="handleClickTitle"
  >
    <!-- 左侧内容插槽 -->
    <template v-if="$slots.left" #left>
      <slot name="left" />
    </template>

    <!-- 标题内容插槽 -->
    <template v-if="$slots.title" #title>
      <slot name="title"></slot>
    </template>

    <!-- 右侧内容插槽 -->
    <template v-if="$slots.right" #right>
      <slot name="right" />
    </template>
  </NavBar>
</template>

<style scoped lang="scss">
.f-nav-bar {
  // 自定义边框样式
  &--border {
    border-bottom: 1px solid $border-regular;
  }

  // 隐藏状态
  &--hidden {
    opacity: 0;
    pointer-events: none;
    transition: opacity 0.3s ease;
  }

  // 深度选择器，用于覆盖 Vant 组件样式
  :deep(.van-nav-bar) {
    padding: 0 16px;

    .van-nav-bar__content {
      height: var(--van-nav-bar-height);
    }

    .van-nav-bar__title {
      font-size: $font-size-h4;
      font-weight: $font-weight-medium;
      line-height: $line-height-h4;
    }

    .van-nav-bar__text {
      font-size: $font-size-body;
      font-weight: $font-weight-normal;
    }

    .van-nav-bar__left,
    .van-nav-bar__right {
      min-width: auto;
    }
  }

  // 适配安全区域
  &.van-nav-bar--safe-area-inset-top {
    padding-top: env(safe-area-inset-top);
  }
}
</style>
