<script setup lang="ts">
/**
 * 场景分析页面布局组件
 * 用于包裹分析页面的头部和内容区域
 * 支持头部高度自定义
 * 支持内容区域滚动
 */

defineOptions({
  name: 'FAnalyseWrap'
})

const visible = defineModel({
  type: Boolean,
  default: false
})

const { headerHeight = '84px', contentCalcHeight = '84px' } = defineProps<{
  headerHeight?: string
  contentCalcHeight?: string
}>()
</script>

<template>
  <!-- appStore.isCollapse -->
  <div
    v-if="visible"
    class="f-analyse-wrap"
    :class="{ collapsed: false }"
    data-page-export-root="scene"
    data-page-export-expand
  >
    <div
      class="faw-header"
      :style="{ height: headerHeight }"
      data-page-export-section="scene-header"
    >
      <slot name="header"></slot>
    </div>
    <!-- <div class="faw-content" :style="{ height: `calc(100vh - ${headerHeight})` }"> -->
    <div
      class="faw-content"
      :style="{ height: `calc(100vh - ${contentCalcHeight})` }"
      data-page-export-section="scene-content"
      data-page-export-expand
    >
      <slot></slot>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.f-analyse-wrap {
  background-color: #e9eef8;
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  left: var(--menu-width);
  z-index: 10;
  transition: width 0.3s ease;
  width: calc(100vw - var(--menu-width));

  &.collapsed {
    left: var(--menu-width-collapsed);
    width: calc(100vw - var(--menu-width-collapsed));
  }
  .faw-header {
    width: 100%;
    background: #ffffff;
    box-shadow: 0px 1px 2px 0px rgba(10, 13, 18, 0.05);
    border-bottom: 1px solid #dfe2e8;
  }
  .faw-content {
    width: 100%;
    overflow-y: auto;
    padding: 0 28px 24px;
  }
}
</style>
