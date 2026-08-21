<script setup lang="ts">
import { reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type {
  BatchEventProcessingTaskProgressValue,
  BatchEventProcessingVocTaskItem,
  BatchEventProcessingVocUpdateProgressDialogModule
} from '../../types'

defineOptions({
  name: 'ProcessingProgressTaskProgressDialog'
})

const props = defineProps<{
  module: BatchEventProcessingVocUpdateProgressDialogModule
  task?: BatchEventProcessingVocTaskItem | null
}>()

const visible = defineModel<boolean>('visible', { default: false })

const emit = defineEmits<{
  confirm: [
    formData: { progress: BatchEventProcessingTaskProgressValue; description: string },
    close: () => void
  ]
  cancel: []
}>()

type ProcessingProgressTaskProgressFormState = {
  progress: BatchEventProcessingTaskProgressValue | ''
  description: string
}

/**
 * 生成更新进度弹窗默认值。
 * 默认回填当前任务进度，便于用户基于现有状态调整。
 * @returns 默认表单态
 */
const createFormState = (): ProcessingProgressTaskProgressFormState => {
  return {
    progress: (props.task?.progress ||
      props.module.progress.value) as ProcessingProgressTaskProgressFormState['progress'],
    description: props.module.description.value
  }
}

const formState = reactive(createFormState())

watch(
  () => [visible.value, props.task?.id],
  () => {
    Object.assign(formState, createFormState())
  },
  { immediate: true }
)

/**
 * 提交进度更新。
 * @param close 关闭弹窗回调
 */
const handleConfirm = ({ close }: { close: () => void }) => {
  if (!formState.progress) {
    ElMessage.warning('请选择完成进度')
    return
  }

  emit(
    'confirm',
    {
      progress: formState.progress,
      description: formState.description.trim()
    },
    close
  )
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
    width="760px"
    :cancel-text="props.module.cancelText"
    :confirm-text="props.module.confirmText"
    :confirm="handleConfirm"
    @cancel="handleCancel"
  >
    <template #header>
      <span>{{ props.module.title }}</span>
    </template>

    <div class="processing-progress-task-progress-dialog">
      <el-form :model="formState" label-width="96px" @submit.prevent>
        <el-form-item :label="props.module.progress.label" required>
          <el-select
            v-model="formState.progress"
            :placeholder="props.module.progress.placeholder"
            class="processing-progress-task-progress-dialog__select"
          >
            <el-option
              v-for="option in props.module.progress.options"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item :label="props.module.description.label">
          <el-input
            v-model.trim="formState.description"
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
.processing-progress-task-progress-dialog__select {
  width: 100%;
}

.processing-progress-task-progress-dialog :deep(.el-select__wrapper),
.processing-progress-task-progress-dialog :deep(.el-textarea__inner) {
  border-radius: 4px;
  box-shadow: 0 0 0 1px #dfe4ea inset;
}

.processing-progress-task-progress-dialog :deep(.el-select__wrapper) {
  min-height: 32px;
}

.processing-progress-task-progress-dialog :deep(.el-textarea__inner) {
  min-height: 140px;
  padding-top: 12px;
}
</style>
