<script setup lang="ts">
withDefaults(
  defineProps<{
    model?: 'query' | 'titleOperation'
    title?: string
    hideTitle?: boolean
    hideLine?: boolean
    titleBold?: boolean
    clearContentTopPadding?: boolean
  }>(),
  {
    title: '默认标题',
    hideTitle: false,
    hideLine: false,
    titleBold: false,
    clearContentTopPadding: false,
    model: 'query'
  }
)
const operationRef = ref<HTMLElement>()
</script>

<template>
  <div class="ft-card">
    <template v-if="model === 'query'">
      <template v-if="!hideTitle">
        <div class="title-wrapper">
          <div :class="{ bold: titleBold, title: true }">
            <slot name="title">{{ title }}</slot>
          </div>
          <div class="extra">
            <slot name="extra"></slot>
          </div>
        </div>
        <div v-if="!hideLine" class="line"></div>
      </template>
    </template>
    <template v-else-if="model === 'titleOperation'">
      <div ref="operationRef" class="titleOperation">
        <div :class="{ bold: true, title: true }">
          <slot name="title">{{ title }}</slot>
        </div>
        <div class="extra">
          <slot name="extra"></slot>
        </div>
      </div>
    </template>

    <div
      :class="{ 'top-padding': !clearContentTopPadding, content: true }"
      :style="{
        height: operationRef?.offsetHeight ? `calc(100% - ${operationRef?.offsetHeight}px)` : 'auto'
      }"
    >
      <slot name="default"></slot>
    </div>
  </div>
</template>

<style scoped lang="scss">
.ft-card {
  width: 100%;
  background: #ffffff;
  border-radius: 8px;
  .title {
    font-weight: 400;
    font-size: 16px;
    color: #4e5969;
    line-height: 24px;

    &.bold {
      font-weight: 600;
      color: #1d2129;
    }
  }

  .extra {
    min-height: 32px;
  }
  .title-wrapper {
    display: flex;
    justify-content: space-between;
    padding: 16px 24px 0;
    min-height: 56px;
    box-sizing: border-box;
  }
  .titleOperation {
    display: flex;
    justify-content: space-between;
    padding: 24px 24px 16px 24px;
  }

  .line {
    width: 100%;
    height: 1px;
    background-color: #e5e6eb;
    //border-bottom: 1px solid #E5E6EB;
  }

  .content {
    padding: 0 24px 24px;
    box-sizing: border-box;
  }
  .top-padding {
    padding-top: 24px;
  }
}
</style>
