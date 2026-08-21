<script setup lang="ts">
import { computed, nextTick, ref } from 'vue'
import { CloseRejectEventEnum, CloseRejectEventTitleByType } from './ehConstants'
import OptionToggleGroup from './components/OptionToggleGroup.vue'
import useSingleEventStore from '@/store/modules/singleEvent'
import { ElMessage } from 'element-plus'

// 关闭、驳回事件
defineOptions({
  name: 'CloseRejectEvent'
})

const visible = defineModel({ default: false })
const { type } = defineProps<{
  type: CloseRejectEventEnum
}>()

const emits = defineEmits(['closeConfirm', 'rejectConfirm', 'cancel'])

const singleEventStore = useSingleEventStore()

const formData = ref({
  description: '',
  rejectReason: singleEventStore.task_event_reject_reason[0]?.value,
  closeReason: undefined as string | undefined
})

// 驳回原因
const rejectOptions = computed(() => {
  return singleEventStore.task_event_reject_reason?.map(el => ({
    label: el.text,
    value: el.value
  }))
})

// 关闭原因
const closeOptions = computed(() => {
  return singleEventStore.task_event_close_reason?.map((el: any) => ({
    label: el.text,
    value: el.value
  }))
})

const handleClose = () => {
  nextTick(() => {
    formData.value = {
      description: '',
      rejectReason: singleEventStore.task_event_reject_reason[0]?.value,
      closeReason: undefined
    }
  })
}

const handleConfirm = ({ close }: any) => {
  console.log('CloseRejectEventEnum--->type', type)

  if (type === CloseRejectEventEnum.Close) {
    if (!formData.value.closeReason) {
      ElMessage.warning('请选择关闭原因')
      return
    }
    emits('closeConfirm', formData.value, close)
  } else if (type === CloseRejectEventEnum.Reject) {
    emits('rejectConfirm', formData.value, close)
  }
}

const handleCancel = () => {
  visible.value = false
  emits('cancel')
}
</script>

<template>
  <FDialog
    v-model:visible="visible"
    destoryOnClose
    @close="handleClose"
    :confirm="handleConfirm"
    @cancel="handleCancel"
  >
    <template #header>
      <span>{{ CloseRejectEventTitleByType[type] }}</span>
    </template>
    <div>
      <el-form :model="formData" ref="formDataRef" @submit.prevent>
        <el-form-item v-if="type === CloseRejectEventEnum.Close" label="关闭原因" prop="">
          <el-select
            v-model="formData.closeReason"
            placeholder="请选择关闭原因"
            filterable
            :options="closeOptions"
          />
        </el-form-item>
        <el-form-item v-if="type === CloseRejectEventEnum.Reject" label="驳回原因" prop="">
          <OptionToggleGroup v-model="formData.rejectReason" :options="rejectOptions">
          </OptionToggleGroup>
        </el-form-item>
        <!-- <el-form-item label="数据纠错" prop="">
          <ErrorCorrectionTable></ErrorCorrectionTable>
        </el-form-item> -->
        <el-form-item label="添加说明" prop="">
          <el-input
            v-model.trim="formData.description"
            clearable
            placeholder=""
            type="textarea"
            :rows="3"
            :maxlength="150"
            resize="none"
            show-word-limit
          ></el-input>
        </el-form-item>
      </el-form>
    </div>
  </FDialog>
</template>

<style lang="scss" scoped></style>
