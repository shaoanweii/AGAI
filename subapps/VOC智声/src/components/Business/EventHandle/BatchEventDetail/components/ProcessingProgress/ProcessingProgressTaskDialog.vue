<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { CascaderProps } from 'element-plus'
import type { InsReportSysDepartVo } from '@/api/common/index.d'
import type {
  BatchEventProcessingHandleStageModuleConfig,
  BatchEventProcessingVocTaskDialogModule,
  BatchEventProcessingVocTaskItem
} from '../../types'
import { buildProcessingProgressHandlerCascaderOptions } from './personnelTree'

defineOptions({
  name: 'ProcessingProgressTaskDialog'
})

interface ProcessingProgressTaskDialogFormData {
  taskName: string
  description: string
  roleValue: string
  departmentValue: string
}

const props = defineProps<{
  module: BatchEventProcessingVocTaskDialogModule
  handleModule: BatchEventProcessingHandleStageModuleConfig
  editingTask?: BatchEventProcessingVocTaskItem | null
  departAccountTree: InsReportSysDepartVo[]
}>()

const visible = defineModel<boolean>('visible', { default: false })

const emit = defineEmits<{
  confirm: [formData: ProcessingProgressTaskDialogFormData, close: () => void]
  cancel: []
}>()

/**
 * 基于当前任务和闭环模块生成表单默认值。
 * 新建时回到配置默认项，编辑时按任务数据回填。
 * @returns 表单默认值
 */
const createFormState = (): ProcessingProgressTaskDialogFormData => {
  const vocLoop = props.handleModule.vocLoop

  if (props.editingTask) {
    return {
      taskName: props.editingTask.taskName,
      description: props.editingTask.description,
      roleValue: props.editingTask.roleValue,
      departmentValue: props.editingTask.departmentValue
    }
  }

  return {
    taskName: vocLoop.taskName.value,
    description: vocLoop.description.value,
    roleValue: vocLoop.departmentRole.value,
    departmentValue: vocLoop.departmentOwner.value
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

/**
 * 重置表单，避免上一次编辑内容污染下一次打开。
 */
const resetForm = () => {
  Object.assign(formState, createFormState())
}

watch(
  () => [visible.value, props.editingTask?.id, props.module.title],
  () => {
    resetForm()
  },
  { immediate: true }
)

/**
 * 提交任务表单。
 * 任务名称和责任部门为闭环任务最小必填项。
 * @param close 关闭弹窗回调
 */
const handleConfirm = ({ close }: { close: () => void }) => {
  if (!formState.taskName.trim()) {
    ElMessage.warning('请输入任务名称')
    return
  }

  if (!formState.roleValue) {
    ElMessage.warning('请选择部门类型')
    return
  }

  if (!formState.departmentValue) {
    ElMessage.warning('请选择处理人员')
    return
  }

  emit(
    'confirm',
    {
      taskName: formState.taskName.trim(),
      description: formState.description.trim(),
      roleValue: formState.roleValue,
      departmentValue: formState.departmentValue
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

    <div class="processing-progress-task-dialog">
      <el-form :model="formState" label-width="96px" @submit.prevent>
        <el-form-item :label="props.handleModule.vocLoop.taskName.label" required>
          <el-input
            v-model.trim="formState.taskName"
            :maxlength="props.handleModule.vocLoop.taskName.maxlength"
            :placeholder="props.handleModule.vocLoop.taskName.placeholder"
          />
        </el-form-item>

        <el-form-item :label="props.handleModule.vocLoop.description.label">
          <el-input
            v-model.trim="formState.description"
            type="textarea"
            :rows="5"
            resize="none"
            :maxlength="props.handleModule.vocLoop.description.maxlength"
            :placeholder="props.handleModule.vocLoop.description.placeholder"
          />
        </el-form-item>

        <el-form-item
          :label="props.handleModule.vocLoop.departmentRole.label"
          required
          class="processing-progress-task-dialog__department-item"
        >
          <div class="processing-progress-task-dialog__department">
            <el-select
              v-model="formState.roleValue"
              clearable
              placeholder="请选择部门类型"
              class="processing-progress-task-dialog__role"
            >
              <el-option
                v-for="option in props.handleModule.vocLoop.departmentRole.options"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
            <el-cascader
              v-model="formState.departmentValue"
              :options="personnelCascaderOptions"
              :props="personnelCascaderProps"
              filterable
              clearable
              separator="#"
              class="processing-progress-task-dialog__owner"
              :placeholder="
                props.handleModule.vocLoop.departmentOwner.placeholder || '请选择处理人员'
              "
            />
          </div>
        </el-form-item>
      </el-form>
    </div>
  </FDialog>
</template>

<style scoped lang="scss">
.processing-progress-task-dialog__department-item :deep(.el-form-item__content) {
  width: 100%;
  min-width: 0;
}

.processing-progress-task-dialog__department {
  display: grid;
  grid-template-columns: 136px minmax(0, 1fr);
  gap: 12px;
  width: 100%;
  min-width: 0;
}

.processing-progress-task-dialog__role,
.processing-progress-task-dialog__owner {
  width: 100%;
  min-width: 0;
}

.processing-progress-task-dialog :deep(.el-input__wrapper),
.processing-progress-task-dialog :deep(.el-cascader .el-input__wrapper),
.processing-progress-task-dialog :deep(.el-select__wrapper),
.processing-progress-task-dialog :deep(.el-textarea__inner) {
  border-radius: 4px;
  box-shadow: 0 0 0 1px #dfe4ea inset;
}

.processing-progress-task-dialog :deep(.el-select__wrapper),
.processing-progress-task-dialog :deep(.el-cascader .el-input__wrapper),
.processing-progress-task-dialog :deep(.el-input__wrapper) {
  min-height: 32px;
}

.processing-progress-task-dialog :deep(.el-textarea__inner) {
  min-height: 140px;
  padding-top: 12px;
}
</style>
