<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { CascaderProps } from 'element-plus'
import type { InsReportSysDepartVo } from '@/api/common/index.d'
import OptionToggleGroup from '@/components/Business/EventHandle/components/OptionToggleGroup.vue'
import { buildProcessingProgressHandlerCascaderOptions } from '@/components/Business/EventHandle/BatchEventDetail/components/ProcessingProgress/personnelTree'
import type { BatchEventConditionsVo } from '@/api/batchEvent/types'
import {
  BatchEventActionTypeEnum,
  BatchEventResponseHandleModeEnum,
  BatchEventResponseModeEnum,
  createBatchEventBatchResponseFormData,
  type BatchEventActionRow,
  type BatchEventBatchResponseFormData,
  type BatchEventBatchResponsePayload
} from '../types'
import { getBatchActionSelectedIds } from './batchActionShared'
import { useBatchEventOptions } from '../hooks/useBatchEventOptions'

defineOptions({
  name: 'BatchEventBatchResponseDialog'
})

const visible = defineModel<boolean>({ default: false })

const props = withDefaults(
  defineProps<{
    selection: BatchEventActionRow[]
    departAccountTree?: InsReportSysDepartVo[]
  }>(),
  {
    departAccountTree: () => []
  }
)

const emit = defineEmits<{
  (e: 'confirm', payload: BatchEventBatchResponsePayload): void
  (e: 'cancel'): void
  (e: 'close'): void
}>()

const batchEventOptions = useBatchEventOptions()
const { batchEvent_reject_reason_type } = batchEventOptions

const formData = reactive<BatchEventBatchResponseFormData>(createBatchEventBatchResponseFormData())
const responseConditions = ref<BatchEventConditionsVo>({})

const responseModeOptions = [
  { label: '确认处理', value: BatchEventResponseModeEnum.Confirm },
  { label: '驳回事件', value: BatchEventResponseModeEnum.Reject }
]

const responseHandleModeOptions = [
  { label: 'VOC系统闭环', value: BatchEventResponseHandleModeEnum.Voc },
  {
    label: '天枢星链系统闭环',
    value: BatchEventResponseHandleModeEnum.Sword,
    disabled: true
  }
]

const selectedIds = computed(() => {
  return getBatchActionSelectedIds(props.selection)
})

const selectedIdParam = computed(() => selectedIds.value.join(','))

const responseDepartmentOptions = computed(() => {
  return buildProcessingProgressHandlerCascaderOptions(props.departAccountTree)
})

const showSwordFields = computed(() => {
  return (
    formData.responseMode === BatchEventResponseModeEnum.Confirm &&
    formData.responseHandleMode === BatchEventResponseHandleModeEnum.Sword
  )
})

const mainDepartmentCascaderProps = {
  value: 'value',
  label: 'label',
  children: 'children',
  emitPath: false,
  checkStrictly: false
} satisfies CascaderProps

/**
 * @description: 重置非 VOC 闭环专属字段，避免切回 VOC 模式时残留无效值
 * @return {*}
 */
const resetSwordOnlyFields = () => {
  formData.responseUserType = []
  formData.responseCarScene = []
  formData.responseFocusTopicValues = []
}

/**
 * @description: 重置“确认处理”专属字段
 * @return {*}
 */
const resetConfirmOnlyFields = () => {
  formData.responseMainRespUserId = ''
  formData.responseHandleMode = BatchEventResponseHandleModeEnum.Voc
  resetSwordOnlyFields()
}

/**
 * @description: 初始化“确认处理”模式默认值
 * 1. 主责部门使用与详情弹窗同源的部门/人员级联，由用户明确选择
 * 2. 默认处理方式为 VOC 系统闭环
 * @return {*}
 */
const initializeConfirmFields = () => {
  formData.responseMainRespUserId = ''
  formData.responseHandleMode = BatchEventResponseHandleModeEnum.Voc
  formData.responseRejectReason = ''
  resetSwordOnlyFields()
}

/**
 * @description: 初始化批量响应表单
 * @return {*}
 */
const initializeFormData = () => {
  const nextDescription = ''

  Object.assign(formData, createBatchEventBatchResponseFormData())
  formData.description = nextDescription
  initializeConfirmFields()
}

/**
 * @description: 按当前勾选事件加载非 VOC 闭环下拉条件
 * @return {Promise<void>}
 */
const loadResponseConditions = async () => {
  const id = selectedIdParam.value
  responseConditions.value = {}

  if (!id) {
    return
  }

  const conditions = await batchEventOptions.loadBatchEventConditionsById(id)
  if (visible.value && selectedIdParam.value === id) {
    responseConditions.value = conditions
  }
}

watch(
  () => visible.value,
  nextVisible => {
    if (nextVisible) {
      initializeFormData()
      void loadResponseConditions()
      return
    }

    responseConditions.value = {}
  },
  { immediate: true }
)

watch(
  selectedIdParam,
  () => {
    if (visible.value) {
      void loadResponseConditions()
    }
  }
)

watch(
  () => formData.responseMode,
  nextMode => {
    if (nextMode === BatchEventResponseModeEnum.Confirm) {
      const preservedDescription = formData.description
      initializeConfirmFields()
      formData.description = preservedDescription
      return
    }

    resetConfirmOnlyFields()
  }
)

watch(
  () => formData.responseHandleMode,
  nextMode => {
    if (formData.responseMode !== BatchEventResponseModeEnum.Confirm) {
      return
    }

    if (nextMode === BatchEventResponseHandleModeEnum.Voc) {
      resetSwordOnlyFields()
      return
    }

    resetSwordOnlyFields()
  }
)

/**
 * @description: 批量响应弹窗前端校验
 * @return {boolean}
 */
const validateFormData = () => {
  if (
    formData.responseMode === BatchEventResponseModeEnum.Confirm &&
    !formData.responseMainRespUserId
  ) {
    ElMessage.warning('请选择主责部门')
    return false
  }

  if (
    formData.responseMode === BatchEventResponseModeEnum.Confirm &&
    !formData.responseHandleMode
  ) {
    ElMessage.warning('请选择处理方式')
    return false
  }

  if (
    formData.responseMode === BatchEventResponseModeEnum.Reject &&
    !formData.responseRejectReason
  ) {
    ElMessage.warning('请选择驳回原因')
    return false
  }

  return true
}

/**
 * @description: 生成批量响应确认载荷
 * @return {BatchEventBatchResponsePayload}
 */
const createConfirmPayload = (): BatchEventBatchResponsePayload => {
  return {
    actionType: BatchEventActionTypeEnum.Response,
    mode: formData.responseMode,
    selectedIds: selectedIds.value,
    responseConditions: responseConditions.value,
    formData: {
      ...formData,
      responseFocusTopicValues: [...formData.responseFocusTopicValues]
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
  Object.assign(formData, createBatchEventBatchResponseFormData())
  responseConditions.value = {}
  emit('close')
}
</script>

<template>
  <FDialog
    v-model:visible="visible"
    width="720px"
    :confirm="handleConfirm"
    @cancel="handleCancel"
    @close="handleClose"
  >
    <template #header>
      <span>批量响应</span>
    </template>

    <div class="batch-action-response-dialog">
      <el-form :model="formData" label-width="96px" class="batch-action-response-dialog__form">
        <el-form-item label="批量响应" required class="batch-action-response-dialog__mode">
          <OptionToggleGroup v-model="formData.responseMode" :options="responseModeOptions" />
        </el-form-item>

        <template v-if="formData.responseMode === BatchEventResponseModeEnum.Confirm">
          <el-form-item label="主责部门" required>
            <el-cascader
              v-model="formData.responseMainRespUserId"
              :options="responseDepartmentOptions"
              :props="mainDepartmentCascaderProps"
              filterable
              clearable
              separator="#"
              placeholder="请选择主责部门"
              class="batch-action-response-dialog__field w-full"
            />
          </el-form-item>

          <el-form-item label="处理方式" required>
            <OptionToggleGroup
              v-model="formData.responseHandleMode"
              :options="responseHandleModeOptions"
            />
          </el-form-item>

          <template v-if="showSwordFields">
            <el-form-item label="用户类型">
              <el-select
                v-model="formData.responseUserType"
                multiple
                collapse-tags
                collapse-tags-tooltip
                clearable
                filterable
                placeholder="请选择用户类型"
                class="batch-action-response-dialog__field"
                :options="responseConditions.custTypeList || []"
                :props="{ label: 'name', value: 'code' }"
              />
            </el-form-item>

            <el-form-item label="用车场景">
              <el-select
                v-model="formData.responseCarScene"
                multiple
                collapse-tags
                collapse-tags-tooltip
                clearable
                filterable
                placeholder="请选择用车场景"
                class="batch-action-response-dialog__field"
                :options="responseConditions.usageScenarioList || []"
                :props="{ label: 'name', value: 'code' }"
              />
            </el-form-item>

            <el-form-item label="聚焦观点" required>
              <el-select
                v-model="formData.responseFocusTopicValues"
                multiple
                collapse-tags
                collapse-tags-tooltip
                :max-collapse-tags="1"
                placeholder="请选择聚焦观点"
                class="batch-action-response-dialog__field"
                :options="responseConditions.topicTextList || []"
                :props="{ label: 'name', value: 'code' }"
              />
            </el-form-item>
          </template>
        </template>

        <el-form-item
          v-if="formData.responseMode === BatchEventResponseModeEnum.Reject"
          label="驳回原因"
          required
        >
          <el-select
            v-model="formData.responseRejectReason"
            clearable
            filterable
            placeholder="请选择驳回原因"
            class="batch-action-response-dialog__field"
            :options="batchEvent_reject_reason_type"
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
            class="batch-action-response-dialog__field"
          />
        </el-form-item>
      </el-form>
    </div>
  </FDialog>
</template>

<style scoped lang="scss">
.batch-action-response-dialog {
  &__form {
    padding-top: 6px;
  }

  &__mode {
    margin-bottom: 18px;
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
  :deep(.el-input__wrapper),
  :deep(.el-cascader .el-input__wrapper) {
    min-height: 32px;
  }

  :deep(.el-select__selection) {
    flex-wrap: nowrap;
  }

  :deep(.el-textarea__inner) {
    min-height: 108px !important;
    padding-top: 12px;
    line-height: 22px;
  }
}
</style>
