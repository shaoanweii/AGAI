<script setup lang="ts">
import {
  DoubleConfirmatioContentByType,
  DoubleConfirmatioTitleByType,
  DoubleConfirmatioTypeEnum
} from './ehConstants'

// 事件处理二次确认
defineOptions({
  name: 'DoubleConfirmatio'
})

const visible = defineModel({ default: false })
const { type } = defineProps<{
  type: DoubleConfirmatioTypeEnum
}>()

const emits = defineEmits<{
  confirm: [type: DoubleConfirmatioTypeEnum, close: () => void]
  cancel: []
}>()

const handleConfirm = ({ close }: any) => {
  emits('confirm', type, close)
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
    width="480px"
    :confirm="handleConfirm"
    @cancel="handleCancel"
  >
    <template #header>
      <span>{{ DoubleConfirmatioTitleByType[type] }}</span>
    </template>
    <div class="flex items-center">
      <SvgIcon name="info-circle-filled" width="20px" height="20px"></SvgIcon>
      <span class="text-h4 ml-8">{{ DoubleConfirmatioContentByType[type] }}</span>
    </div>
  </FDialog>
</template>

<style lang="scss" scoped></style>
