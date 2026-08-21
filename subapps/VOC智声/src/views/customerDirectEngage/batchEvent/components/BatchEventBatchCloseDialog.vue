<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  BatchEventActionTypeEnum,
  type BatchEventActionRow,
  type BatchEventBatchClosePayload
} from '../types'
import { getBatchActionSelectedIds } from './batchActionShared'
import { useBatchEventOptions } from '../hooks/useBatchEventOptions'

defineOptions({
  name: 'BatchEventBatchCloseDialog'
})

const visible = defineModel<boolean>({ default: false })

const props = defineProps<{
  selection: BatchEventActionRow[]
}>()

const emit = defineEmits<{
  (e: 'confirm', payload: BatchEventBatchClosePayload): void
  (e: 'cancel'): void
  (e: 'close'): void
}>()

const { batchEvent_close_reason_type } = useBatchEventOptions()

const formData = reactive({
  closeReason: '',
  description: ''
})

const selectedIds = computed(() => {
  return getBatchActionSelectedIds(props.selection)
})

/**
 * @description: 重置批量关闭弹窗表单
 * @return {*}
 */
const resetFormData = () => {
  formData.closeReason = ''
  formData.description = ''
}

watch(
  () => visible.value,
  nextVisible => {
    if (nextVisible) {
      resetFormData()
    }
  }
)

/**
 * @description: 校验批量关闭弹窗表单
 * @return {boolean}
 */
const validateFormData = () => {
  if (!formData.closeReason) {
    ElMessage.warning('请选择关闭原因')
    return false
  }

  return true
}

/**
 * @description: 生成批量关闭确认载荷
 * @return {BatchEventBatchClosePayload}
 */
const createConfirmPayload = (): BatchEventBatchClosePayload => {
  return {
    actionType: BatchEventActionTypeEnum.Close,
    mode: BatchEventActionTypeEnum.Close,
    selectedIds: selectedIds.value,
    formData: {
      ...formData
    }
  }
}

const handleConfirm = ({ close }: { close: () => void }) => {
  if (!validateFormData()) {
    return
  }

  emit('confirm', createConfirmPayload())
  close()
}

const handleCancel = () => {
  emit('cancel')
}

const handleClose = () => {
  resetFormData()
  emit('close')
}
</script>

<template>
  <FDialog
    v-model:visible="visible"
    width="680px"
    :confirm="handleConfirm"
    @cancel="handleCancel"
    @close="handleClose"
  >
    <template #header>
      <span>批量关闭</span>
    </template>

    <div class="batch-action-close-dialog">
      <el-form :model="formData" label-width="96px" class="batch-action-close-dialog__form">
        <el-form-item label="关闭原因" required>
          <el-select
            v-model="formData.closeReason"
            clearable
            filterable
            placeholder="请选择关闭原因"
            class="batch-action-close-dialog__field"
            :options="batchEvent_close_reason_type"
            :props="{ label: 'text', value: 'value' }"
          />
        </el-form-item>

        <el-form-item label="添加说明">
          <el-input
            v-model.trim="formData.description"
            type="textarea"
            :rows="4"
            resize="none"
            maxlength="150"
            show-word-limit
            placeholder="请添加说明"
            class="batch-action-close-dialog__field"
          />
        </el-form-item>
      </el-form>
    </div>
  </FDialog>
</template>

<style scoped lang="scss">
.batch-action-close-dialog {
  &__form {
    padding-top: 6px;
  }

  &__field {
    width: 100%;
  }

  :deep(.el-input__wrapper),
  :deep(.el-select__wrapper),
  :deep(.el-textarea__inner) {
    border-radius: 4px;
    box-shadow: 0 0 0 1px #dfe4ea inset;
  }

  :deep(.el-form-item) {
    margin-bottom: 18px;
  }

  :deep(.el-select__wrapper),
  :deep(.el-input__wrapper) {
    min-height: 32px;
  }

  :deep(.el-textarea__inner) {
    min-height: 108px !important;
    padding-top: 12px;
    line-height: 22px;
  }
}
</style>
