<script setup lang="ts">
defineOptions({ name: 'CommonTitle' })

interface Props {
  /** 标题文字 */
  title: string
  /** 提示文字 */
  tooltip?: string
}

withDefaults(defineProps<Props>(), {
  tooltip: ''
})
</script>

<template>
  <div class="common-title">
    <div class="common-title__left flex-y-center">
      <span class="common-title__text">{{ title }}</span>
      <el-tooltip
        v-if="tooltip"
        :content="tooltip"
        placement="top"
        popper-class="text-tooltip-light"
      >
        <span class="flex-center ml-8">
          <SvgIcon name="question-mark" width="20px" height="20px" />
        </span>
      </el-tooltip>
      <div v-if="$slots.left" class="common-title__custom-left">
        <slot name="left" />
      </div>
    </div>
    <div v-if="$slots.right" class="common-title__right">
      <slot name="right" />
    </div>
  </div>
</template>

<style scoped lang="scss">
.common-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;

  &__left {
    display: flex;
    align-items: center;
  }

  &__text {
    font-weight: 600;
    font-size: 20px;
    color: var(--text-primary);
    line-height: 28px;
  }

  &__custom-left {
    margin-left: 16px;
  }

  &__right {
    display: flex;
    align-items: center;
  }
}
</style>
