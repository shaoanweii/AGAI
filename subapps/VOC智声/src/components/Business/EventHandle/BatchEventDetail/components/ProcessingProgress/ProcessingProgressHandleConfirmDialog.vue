<script setup lang="ts">
import type { BatchEventProcessingHandleConfirmDialogModule } from '../../types'

defineOptions({
  name: 'ProcessingProgressHandleConfirmDialog'
})

const props = defineProps<{
  module: BatchEventProcessingHandleConfirmDialogModule
}>()

const visible = defineModel<boolean>('visible', { default: false })

const emit = defineEmits<{
  confirm: [close: () => void]
  cancel: []
}>()

/**
 * 确认闭环阶段的确认类操作。
 * 当前仅向父层透出关闭能力，实际列表更新与提示统一由父层接管。
 * @param close 关闭弹窗回调
 */
const handleConfirm = ({ close }: { close: () => void }) => {
  emit('confirm', close)
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
    width="400px"
    :cancel-text="props.module.cancelText"
    :confirm-text="props.module.confirmText"
    :confirm="handleConfirm"
    @cancel="handleCancel"
  >
    <template #header>
      <span>{{ props.module.title }}</span>
    </template>

    <div class="processing-progress-handle-confirm-dialog">
      <SvgIcon
        name="info-circle-filled"
        width="20px"
        height="20px"
        class="processing-progress-handle-confirm-dialog__icon"
      />
      <span class="processing-progress-handle-confirm-dialog__content">{{ props.module.content }}</span>
    </div>
  </FDialog>
</template>

<style scoped lang="scss">
.processing-progress-handle-confirm-dialog {
  display: flex;
  align-items: center;
}

.processing-progress-handle-confirm-dialog__icon {
  flex: none;
}

.processing-progress-handle-confirm-dialog__content {
  margin-left: 8px;
  font-size: 14px;
  line-height: 22px;
  color: #1d2129;
}
</style>
