<script setup lang="ts">
import { reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { BatchEventProcessingCloseDialogModule } from '../../types'

defineOptions({
  name: 'ProcessingProgressCloseDialog'
})

const props = defineProps<{
  module: BatchEventProcessingCloseDialogModule
}>()

const visible = defineModel<boolean>('visible', { default: false })

const emit = defineEmits<{
  confirm: [formData: { closeReason?: string; description: string }, close: () => void]
  cancel: []
}>()

/**
 * 基于弹窗配置生成关闭事件弹窗本地表单态。
 * 该弹窗只负责前端必填校验，每次打开均回到默认值。
 */
const createFormState = (module: BatchEventProcessingCloseDialogModule) => {
  return {
    closeReason: module.closeReason.value,
    description: module.description.value
  }
}

const formData = reactive(createFormState(props.module))

/**
 * 重置本地表单，避免上一次编辑内容残留到下次打开。
 */
const resetForm = () => {
  Object.assign(formData, createFormState(props.module))
}

watch(
  () => visible.value,
  newValue => {
    if (!newValue) {
      resetForm()
    }
  }
)

/**
 * 确认关闭事件。
 * 关闭原因为必填项；本页只透出表单数据与关闭回调，实际成功提示由父层统一处理。
 * @param close 关闭弹窗回调
 */
const handleConfirm = ({ close }: { close: () => void }) => {
  if (!formData.closeReason) {
    ElMessage.warning('请选择关闭原因')
    return
  }

  emit('confirm', { ...formData }, close)
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
    :cancel-text="props.module.cancelText"
    :confirm-text="props.module.confirmText"
    :confirm="handleConfirm"
    @cancel="handleCancel"
  >
    <template #header>
      <span>{{ props.module.title }}</span>
    </template>

    <div class="processing-progress-close-dialog">
      <el-form :model="formData" label-width="84px" @submit.prevent>
        <el-form-item :label="props.module.closeReason.label" required>
          <el-select
            v-model="formData.closeReason"
            :placeholder="props.module.closeReason.placeholder"
            filterable
            class="processing-progress-close-dialog__select"
          >
            <el-option
              v-for="item in props.module.closeReason.options"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item :label="props.module.description.label">
          <el-input
            v-model.trim="formData.description"
            clearable
            :placeholder="props.module.description.placeholder"
            type="textarea"
            :rows="4"
            :maxlength="props.module.description.maxlength"
            resize="none"
            show-word-limit
          />
        </el-form-item>
      </el-form>
    </div>
  </FDialog>
</template>

<style scoped lang="scss">
.processing-progress-close-dialog__select {
  width: 100%;
}

.processing-progress-close-dialog :deep(.el-select__wrapper),
.processing-progress-close-dialog :deep(.el-textarea__inner) {
  border-radius: 4px;
  box-shadow: 0 0 0 1px #dfe4ea inset;
}

.processing-progress-close-dialog :deep(.el-select__wrapper) {
  min-height: 32px;
}
</style>
