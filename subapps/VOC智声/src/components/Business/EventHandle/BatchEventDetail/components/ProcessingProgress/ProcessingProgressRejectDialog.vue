<script setup lang="ts">
import { reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { BatchEventProcessingRejectDialogModule } from '../../types'

defineOptions({
  name: 'ProcessingProgressRejectDialog'
})

const props = defineProps<{
  module: BatchEventProcessingRejectDialogModule
}>()

const visible = defineModel<boolean>('visible', { default: false })

const emit = defineEmits<{
  confirm: [formData: { rejectReason: string; description: string }, close: () => void]
  cancel: []
}>()

/**
 * 基于弹窗配置生成本地表单态。
 * 该弹窗每次打开都重置为配置默认值。
 */
const createFormState = (module: BatchEventProcessingRejectDialogModule) => {
  return {
    rejectReason: module.rejectReason.value,
    description: module.description.value
  }
}

const formState = reactive(createFormState(props.module))

/**
 * 重置弹窗表单，确保每次打开都回到配置默认值。
 */
const resetForm = () => {
  Object.assign(formState, createFormState(props.module))
}

watch(
  () => visible.value,
  () => {
    resetForm()
  }
)

/**
 * 确认驳回事件。
 * 当前仅校验必填原因，最终关闭与提示由父层统一处理。
 * @param close 关闭弹窗回调
 */
const handleConfirm = ({ close }: { close: () => void }) => {
  if (!formState.rejectReason) {
    ElMessage.warning('请选择驳回原因')
    return
  }

  emit('confirm', { ...formState }, close)
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
    width="680px"
    :cancel-text="props.module.cancelText"
    :confirm-text="props.module.confirmText"
    :confirm="handleConfirm"
    @cancel="handleCancel"
  >
    <template #header>
      <span>{{ props.module.title }}</span>
    </template>

    <div class="processing-progress-reject-dialog">
      <el-form :model="formState" label-width="80px" @submit.prevent>
        <el-form-item :label="props.module.rejectReason.label" required>
          <el-select
            v-model="formState.rejectReason"
            :placeholder="props.module.rejectReason.placeholder"
            class="processing-progress-reject-dialog__select"
          >
            <el-option
              v-for="option in props.module.rejectReason.options"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item :label="props.module.description.label">
          <el-input
            v-model="formState.description"
            type="textarea"
            :rows="5"
            resize="none"
            :maxlength="props.module.description.maxlength"
            :placeholder="props.module.description.placeholder"
          />
        </el-form-item>
      </el-form>
    </div>
  </FDialog>
</template>

<style scoped lang="scss">
.processing-progress-reject-dialog__select {
  width: 100%;
}

.processing-progress-reject-dialog :deep(.el-select__wrapper),
.processing-progress-reject-dialog :deep(.el-textarea__inner) {
  border-radius: 4px;
  box-shadow: 0 0 0 1px #dfe4ea inset;
}

.processing-progress-reject-dialog :deep(.el-select__wrapper) {
  min-height: 32px;
}

.processing-progress-reject-dialog :deep(.el-textarea__inner) {
  min-height: 180px;
  padding-top: 12px;
}
</style>
