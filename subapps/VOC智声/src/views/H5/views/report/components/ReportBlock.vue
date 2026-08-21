<script setup lang="ts">
import { computed } from 'vue'

defineOptions({ name: 'ReportBlock' })

interface Props {
  title: string
  /** 标题背景：'report-bg' | 'report-bg2' */
  titleBg?: 'report-bg' | 'report-bg2'
  /** 标题容器宽度（px） */
  titleWidth?: number | string
  /** 标题容器高度（px） */
  titleHeight?: number
  /** 下方模块向上覆盖标题的像素值 */
  overlayOffset?: number
}

const props = withDefaults(defineProps<Props>(), {
  titleBg: 'report-bg',
  titleWidth: 'auto',
  titleHeight: 56,
  overlayOffset: 16
})

const titleStyle = computed(() => ({
  width: props.titleWidth + 'px',
  height: props.titleHeight + 'px'
}))

const bodyStyle = computed(() => ({
  marginTop: `-${props.overlayOffset}px`
}))

const bgClass = computed(() =>
  props.titleBg === 'report-bg2' ? 'report-block__title--report-bg2' : 'report-block__title--report-bg'
)
</script>

<template>
  <section class="h5-report__section">
    <div class="report-block">
      <div class="report-block__title" :class="bgClass" :style="titleStyle">
        <div class="report-block__title-text">{{ title }}</div>
      </div>
      <div class="report-block__body" :style="bodyStyle">
        <slot />
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.report-block {
  position: relative;
}
.report-block__title {
  position: relative;
  background-repeat: no-repeat;
  background-size: cover;
  background-position: left top;
}
.report-block__title--report-bg {
  background-image: url('@/assets/h5/report/title-bg.png');
}
.report-block__title--report-bg2 {
  background-image: url('@/assets/h5/report/title-bg2.png');
}
.report-block__title-text {
  position: absolute;
  top: 9px; // 上边距 9px
  left: 16px; // 左边距 16px
  font-size: 16px;
  color: #ffffff;
  text-align: center;
  font-style: normal;
  text-transform: none;
  font-weight: 500;
}
.report-block__body {
  margin-top: -16px;
  position: relative;
  z-index: 10;
}
</style>

