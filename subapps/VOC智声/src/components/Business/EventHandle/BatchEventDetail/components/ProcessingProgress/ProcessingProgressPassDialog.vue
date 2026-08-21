<script setup lang="ts">
import { computed } from 'vue'
import { DoubleConfirmatioTypeEnum } from '@/components/Business/EventHandle/ehConstants'

defineOptions({
  name: 'ProcessingProgressPassDialog'
})

const visible = defineModel<boolean>('visible', { default: false })

const emit = defineEmits<{
  confirm: [type: DoubleConfirmatioTypeEnum, close: () => void]
  cancel: []
}>()

/**
 * 通过审核提示文案。
 * 保持为页内专用实现，不依赖原有公共确认弹窗组件。
 */
const dialogContent = computed(() => '是否确认通过事件审核？')

/**
 * 确认通过审核。
 * 只向父层暴露确认动作，最终提示由父层统一处理。
 * @param close 关闭弹窗回调
 */
const handleConfirm = ({ close }: { close: () => void }) => {
  emit('confirm', DoubleConfirmatioTypeEnum.Pass, close)
}

/**
 * 取消时仅关闭弹窗并通知父层。
 */
const handleCancel = () => {
  visible.value = false
  emit('cancel')
}
</script>

<template>
  <FDialog
    v-model:visible="visible"
    destoryOnClose
    width="480px"
    :confirm="handleConfirm"
    @cancel="handleCancel"
  >
    <template #header>
      <span>通过审核</span>
    </template>

    <div class="processing-progress-pass-dialog">
      <SvgIcon name="info-circle-filled" width="20px" height="20px" />
      <span class="processing-progress-pass-dialog__content">{{ dialogContent }}</span>
    </div>
  </FDialog>
</template>

<style scoped lang="scss">
.processing-progress-pass-dialog {
  display: flex;
  align-items: center;
}

.processing-progress-pass-dialog__content {
  margin-left: 8px;
  font-size: 14px;
  line-height: 22px;
  color: #1d2129;
}
</style>
