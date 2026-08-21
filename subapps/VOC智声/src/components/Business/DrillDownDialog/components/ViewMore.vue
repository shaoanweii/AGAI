<script setup lang="ts">
defineOptions({
  name: 'ViewMore'
})

withDefaults(
  defineProps<{
    text?: string
    disabled?: boolean
    textColor?: string
  }>(),
  {
    text: '查看更多',
    textColor: '',
    disabled: false
  }
)

const emit = defineEmits<{
  (e: 'click', ev: MouseEvent): void
}>()

const handleClick = (ev: MouseEvent) => {
  if (ev.defaultPrevented) return
  emit('click', ev)
}
</script>

<template>
  <button class="view-more" type="button" :disabled="disabled" @click="handleClick">
    <span class="view-more__text" :style="{ color: textColor ? textColor : '#5f6a7a' }">{{
      text
    }}</span>
    <el-icon class="view-more__icon" :size="16" :color="textColor || '#929AA6'">
      <ArrowRightBold />
    </el-icon>
  </button>
</template>

<style lang="scss" scoped>
.view-more {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 0;
  border: none;
  background: transparent;
  cursor: pointer;
  user-select: none;

  &:disabled {
    cursor: not-allowed;
    opacity: 0.5;
  }

  .view-more__text {
    font-size: 14px;
    font-weight: 400;
    line-height: 20px;
  }
}
</style>
