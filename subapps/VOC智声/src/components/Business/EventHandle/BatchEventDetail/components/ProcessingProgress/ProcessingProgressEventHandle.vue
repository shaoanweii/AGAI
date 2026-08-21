<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import type { CascaderOption, CascaderProps } from 'element-plus'
import FCollapseSection from '@/components/UI/FCollapseSection/index.vue'
import type { InsReportSysDepartVo } from '@/api/common/index.d'
import {
  type BatchEventProcessingApproveStageModule,
  type BatchEventProcessingCloseStageModule,
  type BatchEventProcessingCloseTaskItem,
  type BatchEventProcessingConfirmStageModule,
  type BatchEventProcessingEventHandleModule,
  type BatchEventProcessingHandleStageModuleConfig,
  type BatchEventProcessingLoopMode,
  type BatchEventProcessingTaskProgressValue,
  type BatchEventProcessingTaskRoleValue,
  type BatchEventProcessingVocTaskItem
} from '../../types'
import { BatchEventTaskProgressValue } from '../../beConstants'
import {
  buildProcessingProgressHandlerCascaderOptions,
  findAccountByUserId,
  getAccountFullLabelByUserId
} from './personnelTree'

defineOptions({
  name: 'ProcessingProgressEventHandle'
})

type ProcessingProgressHandleAction =
  | 'update-progress'
  | 'transfer-task'
  | 'edit-task'
  | 'delete-task'

interface ProcessingProgressEventHandleFormState {
  mainDepartment: string
  mainOwner: string
  cooperationDepartment: string[]
  confirmHandleMode: string
  userType: string[]
  vehicleScene: string[]
  pointIssue: string[]
  handleMode: string
  handleReason: string
  handler: string
  description: string
}

export type ProcessingProgressEventHandleFormValue = ProcessingProgressEventHandleFormState

const props = defineProps<{
  module: BatchEventProcessingEventHandleModule
  currentLoopMode: BatchEventProcessingLoopMode
  departAccountTree: InsReportSysDepartVo[]
  readOnly?: boolean
}>()

const emit = defineEmits<{
  'update:loop-mode': [mode: BatchEventProcessingLoopMode]
  'handle-action': [
    payload: {
      action: ProcessingProgressHandleAction
      taskId?: string
    }
  ]
}>()
const isSectionExpanded = ref(true)

/**
 * 根据当前阶段配置生成本地表单态。
 * 表单提交由父组件统一处理，这里保留一份本地可编辑态，避免直接修改 props。
 * @param module 当前阶段表单模块
 * @returns 标准化后的本地表单态
 */
const createFormState = (
  module: BatchEventProcessingEventHandleModule
): ProcessingProgressEventHandleFormState => {
  const baseState: ProcessingProgressEventHandleFormState = {
    mainDepartment: '',
    mainOwner: '',
    cooperationDepartment: [],
    confirmHandleMode: '',
    userType: [],
    vehicleScene: [],
    pointIssue: [],
    handleMode: '',
    handleReason: '',
    handler: '',
    description: ''
  }

  if (module.stage === 'approve') {
    return {
      ...baseState,
      mainOwner: module.mainOwner.value,
      description: module.description.value
    }
  }

  if (module.stage === 'confirm') {
    return {
      ...baseState,
      mainDepartment: module.mainDepartment.value,
      cooperationDepartment: [...module.cooperationDepartment.value],
      confirmHandleMode: module.handleMode.value,
      userType: [...module.userType.value],
      vehicleScene: [...module.vehicleScene.value],
      pointIssue: [...module.pointIssue.value],
      description: module.description.value
    }
  }

  if (module.stage === 'handle') {
    return {
      ...baseState,
      confirmHandleMode: props.currentLoopMode
    }
  }

  return {
    ...baseState,
    handleMode: module.handleMode.value,
    handleReason: module.handleReason.value,
    handler: module.handler.value,
    description: module.description.value
  }
}

const formState = reactive(createFormState(props.module))

const approveModule = computed<BatchEventProcessingApproveStageModule | null>(() => {
  return props.module.stage === 'approve' ? props.module : null
})

const confirmModule = computed<BatchEventProcessingConfirmStageModule | null>(() => {
  return props.module.stage === 'confirm' ? props.module : null
})

const handleModule = computed<BatchEventProcessingHandleStageModuleConfig | null>(() => {
  return props.module.stage === 'handle' ? props.module : null
})

const closeModule = computed<BatchEventProcessingCloseStageModule | null>(() => {
  return props.module.stage === 'close' ? props.module : null
})

const currentVocLoopModule = computed(() => {
  return handleModule.value?.vocLoop ?? null
})

const currentSwordLoopModule = computed(() => {
  return handleModule.value?.swordLoop ?? null
})

const currentSwordLoopTableData = computed<BatchEventProcessingVocTaskItem[]>(() => {
  return currentSwordLoopModule.value?.tasks ?? []
})

/**
 * 关闭态直接消费独立的只读任务表格数据，避免模板层再做字段拼装。
 */
const closeTaskTableData = computed<BatchEventProcessingCloseTaskItem[]>(() => {
  return closeModule.value?.taskTable ?? []
})

/**
 * 收起/展开按钮在闭环处理与事件关闭阶段展示。
 * 审核、业务响应阶段保持原有无按钮表现，避免表单区交互发生变化。
 */
const shouldShowSectionToggle = computed(() => {
  return Boolean(handleModule.value || closeModule.value)
})

const formResetKey = computed(() => {
  const module = props.module

  if (module.stage === 'approve') {
    return [module.stage, module.mainOwner.value, module.description.value].join('|')
  }

  if (module.stage === 'confirm') {
    return [
      module.stage,
      module.mainDepartment.value,
      module.cooperationDepartment.value.join(','),
      module.handleMode.value,
      module.userType.value.join(','),
      module.vehicleScene.value.join(','),
      module.pointIssue.value.join(','),
      module.description.value
    ].join('|')
  }

  if (module.stage === 'handle') {
    return [module.stage, props.currentLoopMode].join('|')
  }

  return [
    module.stage,
    module.handleMode.value,
    module.handleReason.value,
    module.handler.value,
    module.description.value
  ].join('|')
})

watch(
  formResetKey,
  () => {
    Object.assign(formState, createFormState(props.module))
  },
  { immediate: true }
)

watch(
  () => props.module.stage,
  () => {
    isSectionExpanded.value = true
  }
)

watch(
  () => props.currentLoopMode,
  value => {
    formState.confirmHandleMode = value
  },
  { immediate: true }
)

const taskRoleLabelMap = computed<Record<BatchEventProcessingTaskRoleValue, string>>(() => {
  const options = currentVocLoopModule.value?.departmentRole.options ?? []
  return options.reduce<Record<BatchEventProcessingTaskRoleValue, string>>(
    (result, item) => {
      result[item.value as BatchEventProcessingTaskRoleValue] = item.label
      return result
    },
    {} as Record<BatchEventProcessingTaskRoleValue, string>
  )
})

const taskProgressLabelMap = computed<Record<BatchEventProcessingTaskProgressValue, string>>(() => {
  const options = currentVocLoopModule.value?.progress.options ?? []
  return options.reduce<Record<BatchEventProcessingTaskProgressValue, string>>(
    (result, item) => {
      result[item.value as BatchEventProcessingTaskProgressValue] = item.label
      return result
    },
    {} as Record<BatchEventProcessingTaskProgressValue, string>
  )
})

const taskProgressClassMap: Record<BatchEventProcessingTaskProgressValue, string> = {
  [BatchEventTaskProgressValue.NotStarted]: 'is-not-started',
  [BatchEventTaskProgressValue.InProgress]: 'is-in-progress',
  [BatchEventTaskProgressValue.Completed]: 'is-completed'
}

const showSwordFields = computed(() => {
  return Boolean(confirmModule.value && formState.confirmHandleMode === 'sword-loop')
})

const personnelCascaderOptions = computed(() => {
  return buildProcessingProgressHandlerCascaderOptions(props.departAccountTree)
})

const singlePersonnelCascaderProps = {
  value: 'value',
  label: 'label',
  children: 'children',
  emitPath: false,
  checkStrictly: false
} satisfies CascaderProps

const multiplePersonnelCascaderProps = {
  value: 'value',
  label: 'label',
  children: 'children',
  emitPath: false,
  multiple: true,
  checkStrictly: true,
  disabled: (option: CascaderOption) => option.type === 'dept'
} satisfies CascaderProps

const hasVisibleHandleActions = computed(() => {
  return !props.readOnly
})

/**
 * 判断任务行操作是否被任务权限禁用。
 * @param action 任务行操作
 * @param task 当前任务行
 * @returns 是否禁用按钮
 */
const isTaskActionDisabled = (
  action: ProcessingProgressHandleAction,
  task: BatchEventProcessingVocTaskItem
) => {
  if (props.readOnly) {
    return true
  }

  if (action === 'update-progress') {
    return task.progressEditable === false
  }

  if (action === 'transfer-task') {
    return task.reassignable === false
  }

  if (action === 'edit-task') {
    return task.editable === false
  }

  if (action === 'delete-task') {
    return task.deletable === false
  }

  return false
}

/**
 * 切换业务响应阶段的处理方式按钮态，并将当前闭环模式上抛给父层统一管理。
 * @param value 处理方式值
 */
const handleConfirmStageModeChange = (value: string) => {
  if (props.readOnly) {
    return
  }

  formState.confirmHandleMode = value
  if (value !== 'sword-loop') {
    formState.userType = []
    formState.vehicleScene = []
    formState.pointIssue = []
  }
  emit('update:loop-mode', value as BatchEventProcessingLoopMode)
}

/**
 * 透出闭环处理区的行内任务操作事件。
 * 顶部按钮已回收到 footer，这里仅保留任务表格行内交互。
 * @param action 操作标识
 * @param task 任务行
 */
const emitHandleAction = (
  action: ProcessingProgressHandleAction,
  task?: BatchEventProcessingVocTaskItem
) => {
  if (!task || isTaskActionDisabled(action, task)) {
    return
  }

  emit('handle-action', { action, taskId: task.id })
}

/**
 * 获取任务的责任部门展示文本。
 * @param task 任务项
 * @returns 展示文案
 */
const getTaskDepartmentLabel = (
  task: Pick<BatchEventProcessingVocTaskItem, 'roleValue' | 'departmentValue' | 'departmentLabel'>
) => {
  if (task.departmentLabel) {
    const roleLabel = taskRoleLabelMap.value[task.roleValue] || ''
    return roleLabel
      ? `(${roleLabel === '主责部门' ? '主' : '协'}) ${task.departmentLabel}`
      : task.departmentLabel
  }

  const roleLabel = taskRoleLabelMap.value[task.roleValue] || '-'
  const ownerLabel =
    findAccountByUserId(props.departAccountTree, task.departmentValue)?.deptName || '-'

  return `(${roleLabel === '主责部门' ? '主' : '协'}) ${ownerLabel}`
}

/**
 * 获取任务进度展示文案。
 * @param progress 进度值
 * @returns 进度文案
 */
const getTaskProgressLabel = (progress: BatchEventProcessingTaskProgressValue) => {
  return taskProgressLabelMap.value[progress] || '-'
}

/**
 * 获取任务进度样式类名。
 * 任务值使用后端枚举，样式类名沿用页面现有语义。
 * @param progress 进度值
 * @returns 进度样式类名
 */
const getTaskProgressClass = (progress: BatchEventProcessingTaskProgressValue) => {
  return (
    taskProgressClassMap[progress] || taskProgressClassMap[BatchEventTaskProgressValue.InProgress]
  )
}

/**
 * 获取任务进度展示文案，优先使用接口返回的状态名称。
 * @param task 任务项
 * @returns 进度文案
 */
const getTaskProgressText = (task: BatchEventProcessingVocTaskItem) => {
  return task.progressText || getTaskProgressLabel(task.progress)
}

/**
 * 从主页面透传的原始人员树中按需查找处理人展示名。
 * @param userId 用户 id
 * @returns 处理人展示文案
 */
const getHandlerLabel = (userId?: string) => {
  return getAccountFullLabelByUserId(props.departAccountTree, userId) || '-'
}

/**
 * 获取任务处理人员展示文案，优先使用接口返回的处理人姓名。
 * @param task 任务项
 * @returns 处理人员展示文案
 */
const getTaskHandlerLabel = (task: BatchEventProcessingVocTaskItem) => {
  return task.handlerLabel || getHandlerLabel(task.handlerValue || task.departmentValue)
}

defineExpose({
  getFormState: (): ProcessingProgressEventHandleFormValue => ({
    ...formState,
    cooperationDepartment: [...formState.cooperationDepartment],
    pointIssue: [...formState.pointIssue]
  })
})
</script>

<template>
  <FCollapseSection
    v-model="isSectionExpanded"
    title="事件处理"
    :show-toggle="shouldShowSectionToggle"
    class="processing-progress-event-handle"
  >
    <div class="processing-progress-event-handle__form">
      <el-form
        v-if="approveModule"
        :model="formState"
        label-width="100px"
        class="processing-progress-event-handle__edit-form"
        @submit.prevent
      >
        <el-row :gutter="24" class="processing-progress-event-handle__form-row">
          <el-col :span="24">
            <el-form-item
              :label="approveModule.mainOwner.label"
              :required="approveModule.mainOwner.required"
              class="processing-progress-event-handle__form-item"
            >
              <el-cascader
                v-model="formState.mainOwner"
                :options="personnelCascaderOptions"
                :props="singlePersonnelCascaderProps"
                filterable
                clearable
                separator="#"
                :disabled="props.readOnly"
                :placeholder="approveModule.mainOwner.placeholder"
                class="processing-progress-event-handle__select w-full"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row class="processing-progress-event-handle__form-row">
          <el-col :span="24">
            <el-form-item
              :label="approveModule.description.label"
              :required="approveModule.description.required"
              class="processing-progress-event-handle__form-item"
            >
              <el-input
                v-model="formState.description"
                type="textarea"
                :rows="4"
                resize="none"
                :maxlength="approveModule.description.maxlength"
                :disabled="props.readOnly"
                :placeholder="approveModule.description.placeholder"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <el-form
        v-else-if="confirmModule"
        :model="formState"
        label-width="88px"
        class="processing-progress-event-handle__edit-form"
        @submit.prevent
      >
        <el-row :gutter="24" class="processing-progress-event-handle__form-row">
          <el-col :span="12">
            <el-form-item
              :label="confirmModule.mainDepartment.label"
              :required="confirmModule.mainDepartment.required"
              class="processing-progress-event-handle__form-item"
            >
              <el-cascader
                v-model="formState.mainDepartment"
                :options="personnelCascaderOptions"
                :props="singlePersonnelCascaderProps"
                filterable
                clearable
                separator="#"
                :disabled="props.readOnly"
                :placeholder="confirmModule.mainDepartment.placeholder"
                class="processing-progress-event-handle__select w-full"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item
              :label="confirmModule.cooperationDepartment.label"
              :required="confirmModule.cooperationDepartment.required"
              class="processing-progress-event-handle__form-item"
            >
              <el-cascader
                v-model="formState.cooperationDepartment"
                :options="personnelCascaderOptions"
                :props="multiplePersonnelCascaderProps"
                filterable
                clearable
                collapse-tags
                collapse-tags-tooltip
                :max-collapse-tags="confirmModule.cooperationDepartment.collapseTagCount"
                separator="#"
                :disabled="props.readOnly"
                :placeholder="confirmModule.cooperationDepartment.placeholder"
                class="processing-progress-event-handle__select w-full"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row class="processing-progress-event-handle__form-row">
          <el-col :span="24">
            <el-form-item
              :label="confirmModule.handleMode.label"
              :required="confirmModule.handleMode.required"
              class="processing-progress-event-handle__form-item"
            >
              <div class="processing-progress-event-handle__mode-group">
                <button
                  v-for="option in confirmModule.handleMode.options"
                  :key="option.value"
                  type="button"
                  class="processing-progress-event-handle__mode-button"
                  :class="{ 'is-active': formState.confirmHandleMode === option.value }"
                  :disabled="props.readOnly"
                  @click="handleConfirmStageModeChange(option.value)"
                >
                  {{ option.label }}
                </button>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row
          v-if="showSwordFields"
          :gutter="24"
          class="processing-progress-event-handle__form-row"
        >
          <el-col :span="8">
            <el-form-item
              :label="confirmModule.userType.label"
              :required="confirmModule.userType.required"
              class="processing-progress-event-handle__form-item"
            >
              <el-select
                v-model="formState.userType"
                multiple
                filterable
                collapse-tags
                collapse-tags-tooltip
                :max-collapse-tags="confirmModule.userType.collapseTagCount"
                :disabled="props.readOnly"
                :placeholder="confirmModule.userType.placeholder"
                class="processing-progress-event-handle__select"
              >
                <el-option
                  v-for="option in confirmModule.userType.options"
                  :key="option.code"
                  :label="option.name"
                  :value="option.code!"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item
              :label="confirmModule.vehicleScene.label"
              :required="confirmModule.vehicleScene.required"
              class="processing-progress-event-handle__form-item"
            >
              <el-select
                v-model="formState.vehicleScene"
                multiple
                filterable
                collapse-tags
                collapse-tags-tooltip
                :max-collapse-tags="confirmModule.vehicleScene.collapseTagCount"
                :disabled="props.readOnly"
                :placeholder="confirmModule.vehicleScene.placeholder"
                class="processing-progress-event-handle__select"
              >
                <el-option
                  v-for="option in confirmModule.vehicleScene.options"
                  :key="option.code"
                  :label="option.name"
                  :value="option.code!"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item
              :label="confirmModule.pointIssue.label"
              :required="confirmModule.pointIssue.required"
              class="processing-progress-event-handle__form-item"
            >
              <el-select
                v-model="formState.pointIssue"
                multiple
                filterable
                collapse-tags
                collapse-tags-tooltip
                :max-collapse-tags="confirmModule.pointIssue.collapseTagCount"
                :disabled="props.readOnly"
                :placeholder="confirmModule.pointIssue.placeholder"
                class="processing-progress-event-handle__select"
              >
                <el-option
                  v-for="option in confirmModule.pointIssue.options"
                  :key="option.code"
                  :label="option.name"
                  :value="option.code!"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row class="processing-progress-event-handle__form-row">
          <el-col :span="24">
            <el-form-item
              :label="confirmModule.description.label"
              :required="confirmModule.description.required"
              class="processing-progress-event-handle__form-item"
            >
              <el-input
                v-model="formState.description"
                type="textarea"
                :rows="4"
                resize="none"
                :maxlength="confirmModule.description.maxlength"
                :disabled="props.readOnly"
                :placeholder="confirmModule.description.placeholder"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template v-else-if="handleModule && currentLoopMode === 'voc-loop' && currentVocLoopModule">
        <div class="processing-progress-event-handle__table-wrap">
          <el-table
            :data="currentVocLoopModule.tasks"
            row-key="id"
            table-layout="fixed"
            class="processing-progress-event-handle__table"
          >
            <el-table-column prop="taskName" label="任务名称" />
            <el-table-column label="任务说明">
              <template #default="{ row }">
                <div class="processing-progress-event-handle__description-cell">
                  {{ row.description }}
                </div>
              </template>
            </el-table-column>
            <el-table-column label="处理部门">
              <template #default="{ row }">
                {{ getTaskDepartmentLabel(row) }}
              </template>
            </el-table-column>
            <el-table-column label="处理人员">
              <template #default="{ row }">
                {{ getTaskHandlerLabel(row) }}
              </template>
            </el-table-column>
            <el-table-column prop="processTime" label="处理时间" />
            <el-table-column label="处理进度">
              <template #default="{ row }">
                <span
                  class="processing-progress-event-handle__progress-chip"
                  :class="getTaskProgressClass(row.progress)"
                >
                  <span class="processing-progress-event-handle__progress-dot" />
                  {{ getTaskProgressText(row) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="290px">
              <template #default="{ row }">
                <span
                  v-if="props.readOnly || !hasVisibleHandleActions"
                  class="processing-progress-event-handle__action-link is-disabled"
                >
                  -
                </span>
                <div v-else class="processing-progress-event-handle__table-actions">
                  <button
                    type="button"
                    class="processing-progress-event-handle__action-link"
                    :class="{
                      'is-disabled': isTaskActionDisabled('update-progress', row)
                    }"
                    :disabled="isTaskActionDisabled('update-progress', row)"
                    @click="emitHandleAction('update-progress', row)"
                  >
                    更新进度
                  </button>
                  <button
                    type="button"
                    class="processing-progress-event-handle__action-link"
                    :class="{
                      'is-disabled': isTaskActionDisabled('transfer-task', row)
                    }"
                    :disabled="isTaskActionDisabled('transfer-task', row)"
                    @click="emitHandleAction('transfer-task', row)"
                  >
                    转派任务
                  </button>
                  <button
                    type="button"
                    class="processing-progress-event-handle__action-link"
                    :class="{
                      'is-disabled': isTaskActionDisabled('edit-task', row)
                    }"
                    :disabled="isTaskActionDisabled('edit-task', row)"
                    @click="emitHandleAction('edit-task', row)"
                  >
                    编辑
                  </button>
                  <button
                    type="button"
                    class="processing-progress-event-handle__action-link"
                    :class="{
                      'is-disabled': isTaskActionDisabled('delete-task', row)
                    }"
                    :disabled="isTaskActionDisabled('delete-task', row)"
                    @click="emitHandleAction('delete-task', row)"
                  >
                    删除
                  </button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </template>

      <template
        v-else-if="handleModule && currentLoopMode === 'sword-loop' && currentSwordLoopModule"
      >
        <div class="processing-progress-event-handle__table-wrap">
          <el-table
            :data="currentSwordLoopTableData"
            row-key="id"
            table-layout="fixed"
            class="processing-progress-event-handle__table"
          >
            <el-table-column prop="taskName" label="任务名称" />
            <el-table-column label="任务说明">
              <template #default="{ row }">
                <div class="processing-progress-event-handle__description-cell">
                  {{ row.description }}
                </div>
              </template>
            </el-table-column>
            <el-table-column label="处理部门">
              <template #default="{ row }">
                {{ getTaskDepartmentLabel(row) }}
              </template>
            </el-table-column>
            <el-table-column label="处理人员">
              <template #default="{ row }">
                {{ getTaskHandlerLabel(row) }}
              </template>
            </el-table-column>
            <el-table-column prop="processTime" label="处理时间" />
            <el-table-column label="处理进度">
              <template #default="{ row }">
                <span
                  class="processing-progress-event-handle__progress-chip"
                  :class="getTaskProgressClass(row.progress)"
                >
                  <span class="processing-progress-event-handle__progress-dot" />
                  {{ getTaskProgressText(row) }}
                </span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </template>

      <template v-else-if="closeModule">
        <div class="processing-progress-event-handle__table-wrap">
          <el-table
            :data="closeTaskTableData"
            row-key="id"
            table-layout="fixed"
            class="processing-progress-event-handle__table"
          >
            <el-table-column prop="taskName" label="任务名称" width="180px" />
            <el-table-column label="任务说明">
              <template #default="{ row }">
                <div class="processing-progress-event-handle__description-cell">
                  {{ row.description }}
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="departmentLabel" label="处理部门" width="220px" />
            <el-table-column prop="handlerLabel" label="处理人员" width="180px" />
            <el-table-column prop="processTime" label="处理时间" width="180px" />
            <el-table-column label="处理进度" width="120px">
              <template #default="{ row }">
                <span class="processing-progress-event-handle__close-progress">
                  {{ row.progressText }}
                </span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </template>
    </div>
  </FCollapseSection>
</template>

<style scoped lang="scss">
.processing-progress-event-handle__edit-form {
  width: 100%;
}

.processing-progress-event-handle__form-row + .processing-progress-event-handle__form-row {
  margin-top: 16px;
}

.processing-progress-event-handle__form-item {
  margin-bottom: 0;
}

.processing-progress-event-handle__form-item :deep(.el-form-item__label) {
  height: 32px;
  line-height: 32px;
  color: rgba(0, 0, 0, 0.65);
}

.processing-progress-event-handle__form-item :deep(.el-form-item__content) {
  min-width: 0;
  line-height: 32px;
}

.processing-progress-event-handle__select {
  width: 100%;
}

.processing-progress-event-handle__mode-group {
  display: flex;
  align-items: center;
  gap: 16px;
}

.processing-progress-event-handle__mode-button {
  min-width: 148px;
  height: 32px;
  padding: 0 18px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fff;
  color: rgba(0, 0, 0, 0.65);
  font-size: 14px;
  line-height: 1;
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    color 0.2s ease,
    background-color 0.2s ease;
}

.processing-progress-event-handle__mode-button.is-active {
  border-color: #1677ff;
  background: #edf5ff;
  color: #1677ff;
  font-weight: 600;
}

.processing-progress-event-handle__mode-button:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.processing-progress-event-handle__table-wrap {
  overflow: hidden;
  border-top: 1px dashed #ebedf0;
  border-bottom: 1px dashed #ebedf0;
}

.processing-progress-event-handle__table {
  width: 100%;
}

.processing-progress-event-handle__description-cell {
  color: rgba(0, 0, 0, 0.65);
}

.processing-progress-event-handle__progress-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: rgba(0, 0, 0, 0.65);
}

.processing-progress-event-handle__progress-chip.is-not-started
  .processing-progress-event-handle__progress-dot {
  background: #c0c4cc;
}

.processing-progress-event-handle__progress-chip.is-in-progress
  .processing-progress-event-handle__progress-dot {
  background: #1677ff;
}

.processing-progress-event-handle__progress-chip.is-completed
  .processing-progress-event-handle__progress-dot {
  background: #22c55e;
}

.processing-progress-event-handle__progress-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.processing-progress-event-handle__table-actions {
  display: flex;
  align-items: center;
  gap: 18px;
  flex-wrap: wrap;
}

.processing-progress-event-handle__action-link {
  padding: 0;
  border: none;
  background: transparent;
  color: #4c96ff;
  font-size: 14px;
  line-height: 22px;
  cursor: pointer;
}

.processing-progress-event-handle__action-link:disabled,
.processing-progress-event-handle__action-link.is-disabled {
  color: rgba(0, 0, 0, 0.45);
  cursor: not-allowed;
}

.processing-progress-event-handle__close-progress {
  color: rgba(0, 0, 0, 0.88);
}

.processing-progress-event-handle__table :deep(.el-table__inner-wrapper::before) {
  display: none;
}

.processing-progress-event-handle__table :deep(.el-table__header-wrapper th.el-table__cell) {
  padding: 18px 20px;
  background: #f5f7fa;
  font-weight: 600;
  font-size: 14px;
  line-height: 22px;
  color: rgba(0, 0, 0, 0.85);
  text-align: left;
}

.processing-progress-event-handle__table :deep(.el-table__body-wrapper td.el-table__cell) {
  padding: 18px 20px;
  font-size: 14px;
  line-height: 22px;
  color: rgba(0, 0, 0, 0.88);
  text-align: left;
  vertical-align: middle;
}

.processing-progress-event-handle__table :deep(.el-table__header-wrapper th.el-table__cell .cell),
.processing-progress-event-handle__table :deep(.el-table__body-wrapper td.el-table__cell .cell) {
  padding: 0;
  line-height: 22px;
  white-space: normal;
  word-break: break-word;
}

.processing-progress-event-handle__table
  :deep(.el-table__body-wrapper tbody tr:last-child td.el-table__cell) {
  border-bottom: none;
}

.processing-progress-event-handle__table :deep(.el-table__body tbody tr:hover > td.el-table__cell) {
  background: #fff;
}

.processing-progress-event-handle :deep(.el-select__wrapper),
.processing-progress-event-handle :deep(.el-cascader .el-input__wrapper),
.processing-progress-event-handle :deep(.el-textarea__inner) {
  border-radius: 4px;
  box-shadow: 0 0 0 1px #dfe4ea inset;
}

.processing-progress-event-handle :deep(.el-select__wrapper),
.processing-progress-event-handle :deep(.el-cascader .el-input__wrapper) {
  min-height: 32px;
}

.processing-progress-event-handle :deep(.el-textarea__inner) {
  padding-top: 12px;
}
</style>
