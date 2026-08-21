<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import { showToast } from 'vant'
import HDialog from '@h5/components/UI/HDialog'
import HFilterMultiSelect from '@h5/views/taskEvent/components/HFilterMultiSelect.vue'
import HFilterPersonnelSelect from '@h5/views/taskEvent/components/HFilterPersonnelSelect.vue'
import type {
  BatchEventOptionVo,
  BatchEventTaskVo,
  BatchEventTaskProgressModel
} from '@h5/api/batchEvent/types'
import type { BatchProcessActionDialogMode } from './types'

defineOptions({
  name: 'BatchProcessActionDialog'
})

type DialogFormData = {
  closeReason?: string
  rejectReason?: string
  description?: string
  copyUserIds?: string[]
  taskName?: string
  taskDesc?: string
  deptType?: string
  assigneeId?: string
  progressStatus?: string
  progressRemark?: string
}

interface ProcessActionDialogProps {
  /** 弹窗显隐 */
  show: boolean
  /** 弹窗类型 */
  mode: BatchProcessActionDialogMode
  /** 关闭原因字典 */
  closeReasonOptions?: BatchEventOptionVo[]
  /** 驳回原因字典 */
  rejectReasonOptions?: BatchEventOptionVo[]
  /** 完成进度选项 */
  progressOptions?: BatchEventOptionVo[]
  /** 部门-人员树 */
  departAccountTree?: any[]
  /** 部门-人员树加载状态 */
  departAccountTreeLoading?: boolean
  /** 默认选中的抄送人员 ID 列表 */
  defaultCopyUserIds?: string[]
  /** 当前操作任务 */
  task?: BatchEventTaskVo | null
}

const props = withDefaults(defineProps<ProcessActionDialogProps>(), {
  closeReasonOptions: () => [],
  rejectReasonOptions: () => [],
  progressOptions: () => [],
  departAccountTree: () => [],
  departAccountTreeLoading: false,
  defaultCopyUserIds: () => [],
  task: null
})
const emit = defineEmits<{
  /** 同步弹窗显隐 */
  (e: 'update:show', value: boolean): void
  /** 点击确定 */
  (e: 'confirm', mode: BatchProcessActionDialogMode, formData: DialogFormData): void
}>()

const visible = computed({
  get: () => props.show,
  set: value => emit('update:show', value)
})

const formState = reactive<Required<DialogFormData>>({
  closeReason: '',
  rejectReason: '',
  description: '',
  copyUserIds: [],
  taskName: '',
  taskDesc: '',
  deptType: '',
  assigneeId: '',
  progressStatus: '',
  progressRemark: ''
})

const titleMap: Record<BatchProcessActionDialogMode, string> = {
  close: '关闭事件',
  approve: '通过审核',
  copy: '添加抄送人员',
  reject: '驳回事件',
  confirm: '确认处理',
  closedLoopClose: '关闭事件',
  deleteTask: '删除任务',
  transferTask: '转派任务',
  transferHandler: '转派处理人',
  updateProgress: '更新进度',
  createTask: '新建任务',
  editTask: '编辑任务'
}

const confirmMessageMap: Partial<Record<BatchProcessActionDialogMode, string>> = {
  approve: '确认通过审核？',
  confirm: '确认提交处理？',
  closedLoopClose: '确认关闭事件？',
  deleteTask: '确认删除当前任务？'
}

const deptTypeOptions: BatchEventOptionVo[] = [
  { label: '主责部门', value: 'MAIN' },
  { label: '协同部门', value: 'COOP' }
]

const dialogTitle = computed(() => titleMap[props.mode])
const dialogBodyClass = computed(() => `is-${props.mode}`)
const isConfirmMessageMode = computed(() =>
  ['approve', 'confirm', 'closedLoopClose', 'deleteTask'].includes(props.mode)
)
const confirmMessage = computed(() => confirmMessageMap[props.mode] || '')
const selectedCopyUserIds = computed({
  get: () => (Array.isArray(formState.copyUserIds) ? formState.copyUserIds : []),
  set: value => {
    formState.copyUserIds = Array.isArray(value) ? value : []
  }
})
const selectedAssigneeIds = computed({
  get: () => (formState.assigneeId ? [formState.assigneeId] : []),
  set: value => {
    formState.assigneeId = Array.isArray(value) ? value[0] || '' : ''
  }
})
const selectedDeptTypes = computed({
  get: () => (formState.deptType ? [formState.deptType] : []),
  set: value => {
    formState.deptType = Array.isArray(value) ? value[0] || '' : ''
  }
})
const selectedCloseReasons = computed({
  get: () => (formState.closeReason ? [formState.closeReason] : []),
  set: value => {
    formState.closeReason = Array.isArray(value) ? value[0] || '' : ''
  }
})
const selectedRejectReasons = computed({
  get: () => (formState.rejectReason ? [formState.rejectReason] : []),
  set: value => {
    formState.rejectReason = Array.isArray(value) ? value[0] || '' : ''
  }
})
const selectedProgressStatuses = computed({
  get: () => (formState.progressStatus ? [formState.progressStatus] : []),
  set: value => {
    formState.progressStatus = Array.isArray(value) ? value[0] || '' : ''
  }
})

/**
 * 从接口选项中读取展示和值字段，供 HFilterMultiSelect 使用。
 * @param option 接口选项
 * @returns 选项文案
 */
const getOptionLabel = (option: BatchEventOptionVo) => {
  return String(option.label || option.name || option.value || option.code || '')
}

/**
 * 从接口选项中读取 value，供 HFilterMultiSelect 使用。
 * @param option 接口选项
 * @returns 选项值
 */
const getOptionValue = (option: BatchEventOptionVo) => {
  return String(option.value || option.code || option.label || option.name || '')
}

const normalizedCloseReasonOptions = computed(() =>
  props.closeReasonOptions.map(item => ({
    ...item,
    label: getOptionLabel(item),
    value: getOptionValue(item)
  }))
)
const normalizedRejectReasonOptions = computed(() =>
  props.rejectReasonOptions.map(item => ({
    ...item,
    label: getOptionLabel(item),
    value: getOptionValue(item)
  }))
)
const normalizedProgressOptions = computed(() =>
  props.progressOptions.map(item => ({
    ...item,
    label: getOptionLabel(item),
    value: getOptionValue(item)
  }))
)

/**
 * 去重并过滤空人员 ID，避免弹窗初始化时携带无效选项。
 * @param userIds 人员 ID 列表
 * @returns 可写入表单的人员 ID 列表
 */
const normalizeUserIds = (userIds?: string[]) => {
  return Array.from(
    new Set(
      (Array.isArray(userIds) ? userIds : []).map(item => String(item || '').trim()).filter(Boolean)
    )
  )
}

/**
 * 弹窗打开时按当前模式和任务接口数据重置表单。
 */
const resetForm = () => {
  formState.closeReason = ''
  formState.rejectReason = ''
  formState.description = ''
  formState.copyUserIds = []
  formState.taskName = ''
  formState.taskDesc = ''
  formState.deptType = ''
  formState.assigneeId = ''
  formState.progressStatus = ''
  formState.progressRemark = ''

  if (props.mode === 'editTask' && props.task) {
    formState.taskName = props.task.taskName || ''
    formState.taskDesc = props.task.taskDesc || ''
    formState.deptType = props.task.deptType || ''
    formState.assigneeId = props.task.assigneeId || ''
  }

  if (props.mode === 'updateProgress' && props.task) {
    formState.progressStatus = props.task.progressStatus || ''
    formState.progressRemark = props.task.progressRemark || ''
  }

  if (props.mode === 'transferTask' && props.task) {
    formState.assigneeId = props.task.assigneeId || ''
  }

  if (props.mode === 'copy') {
    formState.copyUserIds = normalizeUserIds(props.defaultCopyUserIds)
  }
}

watch(
  () => [props.show, props.mode, props.task?.taskId],
  () => {
    if (!props.show) return
    resetForm()
  },
  { immediate: true }
)

/**
 * 校验当前弹窗表单。
 * @returns 是否允许提交
 */
const validateForm = () => {
  if (props.mode === 'close' && !formState.closeReason) {
    showToast('请选择关闭原因')
    return false
  }

  if (props.mode === 'reject' && !formState.rejectReason) {
    showToast('请选择驳回原因')
    return false
  }

  if (props.mode === 'createTask' || props.mode === 'editTask') {
    if (!formState.taskName.trim()) {
      showToast('请输入任务名称')
      return false
    }

    if (!formState.deptType) {
      showToast('请选择部门类型')
      return false
    }

    if (!formState.assigneeId) {
      showToast('请选择处理人员')
      return false
    }
  }

  if (props.mode === 'transferTask' && !formState.assigneeId) {
    showToast('请选择处理人员')
    return false
  }

  if (props.mode === 'copy' && !formState.copyUserIds.length) {
    showToast('请选择抄送人员')
    return false
  }

  if (props.mode === 'transferHandler' && !formState.assigneeId) {
    showToast('请选择处理人员')
    return false
  }

  if (props.mode === 'updateProgress' && !formState.progressStatus) {
    showToast('请选择完成进度')
    return false
  }

  return true
}

/**
 * 确认当前弹窗，向父级传出真实表单值。
 */
const handleConfirm = ({ close }: { close: () => void }) => {
  if (!validateForm()) return

  emit('confirm', props.mode, {
    closeReason: formState.closeReason,
    rejectReason: formState.rejectReason,
    description: formState.description.trim(),
    copyUserIds: [...formState.copyUserIds],
    taskName: formState.taskName.trim(),
    taskDesc: formState.taskDesc.trim(),
    deptType: formState.deptType,
    assigneeId: formState.assigneeId,
    progressStatus: formState.progressStatus as BatchEventTaskProgressModel['progressStatus'],
    progressRemark: formState.progressRemark.trim()
  })
  close()
}
</script>

<template>
  <HDialog v-model:visible="visible" :title="dialogTitle" width="335px" :confirm="handleConfirm">
    <div class="process-action-dialog" :class="dialogBodyClass">
      <template v-if="props.mode === 'close'">
        <div class="dialog-form-row">
          <div class="dialog-label is-required">关闭原因</div>
          <div class="dialog-control">
            <HFilterMultiSelect
              v-model="selectedCloseReasons"
              :options="normalizedCloseReasonOptions"
              :max-selected="1"
              title="关闭原因"
              placeholder="请选择关闭原因"
              :searchable="false"
            />
          </div>
        </div>
      </template>

      <div v-else-if="isConfirmMessageMode" class="confirm-message">
        <van-icon name="info" size="18" color="#1677FF" />
        <span>{{ confirmMessage }}</span>
      </div>

      <template v-else-if="props.mode === 'copy'">
        <div class="dialog-form-row">
          <div class="dialog-label is-required">抄送人员</div>
          <div class="dialog-control">
            <HFilterPersonnelSelect
              v-model="selectedCopyUserIds"
              :options="props.departAccountTree"
              :loading="props.departAccountTreeLoading"
              display-mode="departmentPath"
              title="抄送人员"
              placeholder="请选择抄送人员"
            />
          </div>
        </div>
      </template>

      <template v-else-if="props.mode === 'reject'">
        <div class="dialog-form-row">
          <div class="dialog-label is-required">驳回原因</div>
          <div class="dialog-control">
            <HFilterMultiSelect
              v-model="selectedRejectReasons"
              :options="normalizedRejectReasonOptions"
              :max-selected="1"
              title="驳回原因"
              placeholder="请选择驳回原因"
              :searchable="false"
            />
          </div>
        </div>

        <div class="dialog-form-row dialog-form-row--textarea">
          <div class="dialog-label">添加说明</div>
          <div class="dialog-control">
            <textarea
              v-model="formState.description"
              class="dialog-textarea"
              rows="4"
              maxlength="200"
              placeholder="请添加说明"
            ></textarea>
          </div>
        </div>
      </template>

      <template v-else-if="props.mode === 'transferTask'">
        <div class="dialog-form-row">
          <div class="dialog-label is-required">处理人员</div>
          <div class="dialog-control">
            <HFilterPersonnelSelect
              v-model="selectedAssigneeIds"
              :options="props.departAccountTree"
              :loading="props.departAccountTreeLoading"
              :max-selected="1"
              title="处理人员"
              placeholder="请选择处理人员"
            />
          </div>
        </div>
      </template>

      <template v-else-if="props.mode === 'transferHandler'">
        <div class="dialog-form-row">
          <div class="dialog-label is-required">处理人员</div>
          <div class="dialog-control">
            <HFilterPersonnelSelect
              v-model="selectedAssigneeIds"
              :options="props.departAccountTree"
              :loading="props.departAccountTreeLoading"
              :max-selected="1"
              display-mode="departmentPath"
              title="处理人员"
              placeholder="请选择处理人员"
            />
          </div>
        </div>

        <div class="dialog-form-row dialog-form-row--textarea">
          <div class="dialog-label">添加说明</div>
          <div class="dialog-control">
            <textarea
              v-model="formState.description"
              class="dialog-textarea"
              rows="4"
              maxlength="200"
              placeholder="请添加说明"
            ></textarea>
          </div>
        </div>
      </template>

      <template v-else-if="props.mode === 'updateProgress'">
        <div class="dialog-form-row">
          <div class="dialog-label is-required">完成进度</div>
          <div class="dialog-control">
            <HFilterMultiSelect
              v-model="selectedProgressStatuses"
              :options="normalizedProgressOptions"
              :max-selected="1"
              title="完成进度"
              placeholder="请选择完成进度"
              :searchable="false"
            />
          </div>
        </div>

        <div class="dialog-form-row dialog-form-row--textarea">
          <div class="dialog-label">添加说明</div>
          <div class="dialog-control">
            <textarea
              v-model="formState.progressRemark"
              class="dialog-textarea"
              rows="4"
              maxlength="200"
              placeholder="请添加说明"
            ></textarea>
          </div>
        </div>
      </template>

      <template v-else-if="props.mode === 'createTask' || props.mode === 'editTask'">
        <div class="dialog-form-row">
          <div class="dialog-label is-required">任务名称</div>
          <div class="dialog-control">
            <input
              v-model="formState.taskName"
              class="dialog-input"
              type="text"
              maxlength="20"
              placeholder="请输入任务名称"
            />
          </div>
        </div>

        <div class="dialog-form-row dialog-form-row--textarea">
          <div class="dialog-label">添加说明</div>
          <div class="dialog-control">
            <textarea
              v-model="formState.taskDesc"
              class="dialog-textarea"
              rows="4"
              maxlength="200"
              placeholder="请添加说明"
            ></textarea>
          </div>
        </div>

        <div class="dialog-form-row">
          <div class="dialog-label is-required">部门类型</div>
          <div class="dialog-control">
            <HFilterMultiSelect
              v-model="selectedDeptTypes"
              :options="deptTypeOptions"
              :max-selected="1"
              title="部门类型"
              placeholder="请选择部门类型"
              :searchable="false"
            />
          </div>
        </div>

        <div class="dialog-form-row">
          <div class="dialog-label is-required">处理人员</div>
          <div class="dialog-control">
            <HFilterPersonnelSelect
              v-model="selectedAssigneeIds"
              :options="props.departAccountTree"
              :loading="props.departAccountTreeLoading"
              :max-selected="1"
              title="处理人员"
              placeholder="请选择处理人员"
            />
          </div>
        </div>
      </template>
    </div>
  </HDialog>
</template>

<style scoped lang="scss">
.process-action-dialog {
  &.is-close,
  &.is-reject,
  &.is-updateProgress,
  &.is-transferHandler {
    min-height: 176px;
    padding-top: 4px;
  }

  &.is-approve,
  &.is-confirm,
  &.is-closedLoopClose,
  &.is-deleteTask {
    min-height: 72px;
    display: flex;
    align-items: center;
  }

  &.is-transferTask {
    min-height: 54px;
    padding-top: 4px;
  }

  &.is-copy {
    min-height: 54px;
    padding-top: 4px;
  }

  &.is-createTask,
  &.is-editTask {
    min-height: 294px;
    padding-top: 4px;
  }
}

.process-action-dialog.is-updateProgress,
.process-action-dialog.is-transferHandler,
.process-action-dialog.is-createTask,
.process-action-dialog.is-editTask {
  .dialog-form-row + .dialog-form-row {
    margin-top: 10px;
  }
}

.dialog-form-row {
  display: flex;
  align-items: center;
  column-gap: 8px;
}

.dialog-form-row + .dialog-form-row {
  margin-top: 14px;
}

.dialog-form-row--textarea {
  align-items: flex-start;
}

.dialog-label {
  flex: none;
  width: 68px;
  text-align: right;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  color: #5f6a7a;
}

.dialog-label.is-required::before {
  content: '*';
  margin-right: 4px;
  color: #f53f3f;
}

.dialog-control {
  flex: 1;
  min-width: 0;
}

.dialog-input {
  width: 100%;
  height: 36px;
  padding: 0 12px;
  border: 1px solid #e5e6eb;
  border-radius: 4px;
  background: #ffffff;
  outline: none;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  color: #1f2733;
}

.dialog-input::placeholder {
  color: #c9cdd4;
}

.dialog-textarea {
  width: 100%;
  height: 88px;
  min-height: 88px;
  padding: 9px 12px;
  border: 1px solid #e5e6eb;
  border-radius: 4px;
  resize: none;
  outline: none;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  color: #1f2733;
}

.dialog-textarea::placeholder {
  color: #c9cdd4;
}

.confirm-message {
  width: 100%;
  display: flex;
  align-items: center;
  column-gap: 8px;
  padding: 0 0 0 8px;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  color: #5f6a7a;
}

.dialog-control :deep(.hfms-trigger),
.dialog-control :deep(.hfps-trigger) {
  height: 36px;
  border-color: #e5e6eb;
  border-radius: 4px;
}
</style>
