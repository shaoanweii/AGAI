<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { CascaderProps } from 'element-plus'
import type { InsReportSysDepartVo } from '@/api/common/index.d'
import type {
  BatchEventProcessingVocTransferDialogModule,
  BatchEventProcessingVocTaskItem
} from '../../types'
import { buildProcessingProgressHandlerCascaderOptions } from './personnelTree'

defineOptions({
  name: 'ProcessingProgressTransferDialog'
})

const props = defineProps<{
  module: BatchEventProcessingVocTransferDialogModule
  task?: BatchEventProcessingVocTaskItem | null
  departAccountTree: InsReportSysDepartVo[]
}>()

const visible = defineModel<boolean>('visible', { default: false })

const emit = defineEmits<{
  confirm: [formData: { handler: string }, close: () => void]
  cancel: []
}>()

/**
 * 生成处理人员默认值。
 * 转派任务需要用户重新选择处理人员，不回填当前任务人员。
 * @returns 默认表单值
 */
const createFormState = () => {
  return {
    handler: props.module.handler.value || ''
  }
}

const formState = reactive(createFormState())

const personnelCascaderOptions = computed(() => {
  return buildProcessingProgressHandlerCascaderOptions(props.departAccountTree)
})

const personnelCascaderProps = {
  value: 'value',
  label: 'label',
  children: 'children',
  emitPath: false,
  checkStrictly: false
} satisfies CascaderProps

watch(
  () => [visible.value, props.task?.id, props.module.title],
  () => {
    Object.assign(formState, createFormState())
  },
  { immediate: true }
)

/**
 * 确认转派。
 * @param close 关闭弹窗回调
 */
const handleConfirm = ({ close }: { close: () => void }) => {
  if (!formState.handler) {
    ElMessage.warning('请选择处理人员')
    return
  }

  emit('confirm', { handler: formState.handler }, close)
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

    <div class="processing-progress-transfer-dialog">
      <el-form :model="formState" label-width="96px" @submit.prevent>
        <el-form-item :label="props.module.handler.label" required>
          <el-cascader
            v-model="formState.handler"
            :options="personnelCascaderOptions"
            :props="personnelCascaderProps"
            filterable
            clearable
            separator="#"
            :placeholder="props.module.handler.placeholder"
            class="processing-progress-transfer-dialog__select"
          />
        </el-form-item>
      </el-form>
    </div>
  </FDialog>
</template>

<style scoped lang="scss">
.processing-progress-transfer-dialog,
.processing-progress-transfer-dialog :deep(.el-form),
.processing-progress-transfer-dialog :deep(.el-form-item),
.processing-progress-transfer-dialog :deep(.el-form-item__content) {
  width: 100%;
}

.processing-progress-transfer-dialog :deep(.el-form-item__content) {
  min-width: 0;
}

.processing-progress-transfer-dialog__select {
  display: block;
  width: 100%;
}

.processing-progress-transfer-dialog :deep(.el-cascader) {
  width: 100%;
}

.processing-progress-transfer-dialog :deep(.el-cascader .el-input__wrapper) {
  min-height: 32px;
  border-radius: 4px;
  box-shadow: 0 0 0 1px #dfe4ea inset;
}
</style>
