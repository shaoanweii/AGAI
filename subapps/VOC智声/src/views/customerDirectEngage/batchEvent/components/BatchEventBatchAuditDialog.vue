<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import OptionToggleGroup from '@/components/Business/EventHandle/components/OptionToggleGroup.vue'
import {
  BatchEventActionTypeEnum,
  BatchEventAuditModeEnum,
  createBatchEventBatchAuditFormData,
  type BatchEventActionRow,
  type BatchEventBatchAuditFormData,
  type BatchEventBatchAuditPayload
} from '../types'
import { getBatchActionSelectedIds } from './batchActionShared'
import { useBatchEventOptions } from '../hooks/useBatchEventOptions'

defineOptions({
  name: 'BatchEventBatchAuditDialog'
})

const visible = defineModel<boolean>({ default: false })

const props = defineProps<{
  selection: BatchEventActionRow[]
}>()

const emit = defineEmits<{
  (e: 'confirm', payload: BatchEventBatchAuditPayload): void
  (e: 'cancel'): void
  (e: 'close'): void
}>()

const {
  departAccountCascaderOptions,
  ensureDepartAccountCascaderOptions,
  batchEvent_close_reason_type
} = useBatchEventOptions()

const formData = reactive<BatchEventBatchAuditFormData>(createBatchEventBatchAuditFormData())

const auditModeOptions = [
  { label: '审核通过', value: BatchEventAuditModeEnum.Pass },
  { label: '关闭事件', value: BatchEventAuditModeEnum.Close }
]

const businessOwnerOptions = computed<any[]>(() => {
  return departAccountCascaderOptions.value
})

const selectedIds = computed(() => {
  return getBatchActionSelectedIds(props.selection)
})

const defaultBusinessOwnerUserId = computed(() => {
  const uniqueUserIds = [
    ...new Set(
      props.selection
        .map(item => item.mainRespUserId)
        .filter((id): id is string => typeof id === 'string' && id.length > 0)
    )
  ]

  return uniqueUserIds.length === 1 ? uniqueUserIds[0] : ''
})

/**
 * @description: 重置审核弹窗表单
 * @return {*}
 */
const resetFormData = () => {
  Object.assign(formData, createBatchEventBatchAuditFormData())
}

/**
 * @description: 初始化审核弹窗默认值
 * 1. 默认进入“审核通过”
 * 2. 如果选中数据只有一个业务责任人，则自动回填，减少重复选择
 * @return {*}
 */
const initializeFormData = () => {
  resetFormData()
  formData.businessOwnerUserId = defaultBusinessOwnerUserId.value
}

watch(
  () => visible.value,
  nextVisible => {
    if (nextVisible) {
      void ensureDepartAccountCascaderOptions()
      initializeFormData()
    }
  }
)

watch(
  () => formData.auditMode,
  nextMode => {
    if (nextMode === BatchEventAuditModeEnum.Pass) {
      formData.auditCloseReason = ''
      if (!formData.businessOwnerUserId) {
        formData.businessOwnerUserId = defaultBusinessOwnerUserId.value
      }
      return
    }

    formData.businessOwnerUserId = ''
  }
)

/**
 * @description: 审核弹窗前端校验
 * @return {boolean}
 */
const validateFormData = () => {
  if (formData.auditMode === BatchEventAuditModeEnum.Pass && !formData.businessOwnerUserId) {
    ElMessage.warning('请选择业务责任人')
    return false
  }

  if (formData.auditMode === BatchEventAuditModeEnum.Close && !formData.auditCloseReason) {
    ElMessage.warning('请选择关闭原因')
    return false
  }

  return true
}

/**
 * @description: 生成审核弹窗确认载荷
 * @return {BatchEventBatchAuditPayload}
 */
const createConfirmPayload = (): BatchEventBatchAuditPayload => {
  return {
    actionType: BatchEventActionTypeEnum.Audit,
    mode: formData.auditMode,
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
    width="720px"
    :confirm="handleConfirm"
    @cancel="handleCancel"
    @close="handleClose"
  >
    <template #header>
      <span>批量审核</span>
    </template>

    <div class="batch-action-audit-dialog">
      <el-form :model="formData" label-width="96px" class="batch-action-audit-dialog__form">
        <el-form-item label="批量审核" required class="batch-action-audit-dialog__mode">
          <OptionToggleGroup v-model="formData.auditMode" :options="auditModeOptions" />
        </el-form-item>

        <el-form-item
          v-if="formData.auditMode === BatchEventAuditModeEnum.Pass"
          label="业务责任人"
          required
        >
          <el-cascader
            v-model="formData.businessOwnerUserId"
            :options="businessOwnerOptions"
            :props="{
              label: 'label',
              value: 'value',
              children: 'children',
              emitPath: false,
              checkStrictly: true
            }"
            clearable
            filterable
            :show-all-levels="false"
            placeholder="请选择业务责任人"
            class="batch-action-audit-dialog__field batch-action-audit-dialog__cascader"
          />
        </el-form-item>

        <el-form-item
          v-if="formData.auditMode === BatchEventAuditModeEnum.Close"
          label="关闭原因"
          required
        >
          <el-select
            v-model="formData.auditCloseReason"
            clearable
            filterable
            placeholder="请选择关闭原因"
            class="batch-action-audit-dialog__field"
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
            class="batch-action-audit-dialog__field"
          />
        </el-form-item>
      </el-form>
    </div>
  </FDialog>
</template>

<style scoped lang="scss">
.batch-action-audit-dialog {
  &__form {
    padding-top: 6px;
  }

  &__mode {
    margin-bottom: 18px;
  }

  &__field {
    width: 100%;
  }

  :deep(.batch-action-audit-dialog__cascader.el-cascader) {
    width: 100%;
    max-width: 100%;
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
