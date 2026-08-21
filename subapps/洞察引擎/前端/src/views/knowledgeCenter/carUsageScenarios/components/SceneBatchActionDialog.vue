<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { batchChangeCarSceneStatus, carUsageScenarioEnableStatus } from '@/api/carUsageScenarios'
import type { CarUsageScenarioBatchActionType } from './types'

defineOptions({
  name: 'CarUsageScenarioSceneBatchActionDialog'
})

interface Props {
  visible: boolean
  actionType: CarUsageScenarioBatchActionType
  selectedIds: string[]
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}>()

const dialogVisible = computed({
  get: () => props.visible,
  set: value => emit('update:visible', value)
})

const dialogTitle = computed(() => {
  if (props.actionType === 'enable') return '批量启用'
  return '批量禁用'
})

const confirmMessage = computed(() => {
  if (props.actionType === 'enable') {
    return '是否确认批量启用选中用车场景？'
  }
  return '是否确认批量禁用选中用车场景？'
})

const confirmLoading = ref(false)

/**
 * 批量动作统一由弹框内部提交，外层列表只关心成功后的刷新联动。
 */
const handleConfirm = async ({ close }: { close: () => void }) => {
  if (confirmLoading.value || !props.selectedIds.length) return

  confirmLoading.value = true
  try {
    await batchChangeCarSceneStatus({
      ids: props.selectedIds,
      status:
        props.actionType === 'enable'
          ? carUsageScenarioEnableStatus.ENABLED
          : carUsageScenarioEnableStatus.DISABLED
    })
    ElMessage.success(props.actionType === 'enable' ? '批量启用成功' : '批量禁用成功')

    emit('success')
    close()
  } catch (error: any) {
    if (error?.code) {
      return
    }
    if (error?.message) {
      ElMessage.error(error.message)
      return
    }
    ElMessage.error(
      props.actionType === 'enable' ? '批量启用失败，请稍后重试' : '批量禁用失败，请稍后重试'
    )
  } finally {
    confirmLoading.value = false
  }
}
</script>

<template>
  <AppDialog
    v-model:visible="dialogVisible"
    :title="dialogTitle"
    width="520px"
    :confirm="handleConfirm"
  >
    <div class="batch-action-dialog__confirm">
      <SvgIcon name="warnning-icon" class="batch-action-dialog__icon" color="#1677ff" />
      <div class="batch-action-dialog__text">{{ confirmMessage }}</div>
    </div>
  </AppDialog>
</template>

<style scoped lang="scss">
.batch-action-dialog__confirm {
  display: flex;
  align-items: center;
  gap: 8px;
}

.batch-action-dialog__icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.batch-action-dialog__text {
  font-size: 14px;
  line-height: 22px;
  color: #4e5969;
}
</style>
