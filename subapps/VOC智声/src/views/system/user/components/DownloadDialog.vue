<script setup lang="ts">
import { useRouter } from 'vue-router'

// 事件处理二次确认
defineOptions({
  name: 'DoubleConfirmatio'
})

const router = useRouter()
const visible = defineModel({ default: false })
const emits = defineEmits<{
  confirm: [close: () => void]
  cancel: []
}>()

const handleConfirm = ({ close }: any) => {
  // 跳转到下载管理页面
  router.push('/system/downloadManagement')
  // 关闭弹窗
  close()
  emits('confirm', close)
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
    cancelText="稍后再说"
    confirmText="前往查看"
    :confirm="handleConfirm"
    @cancel="handleCancel"
  >
    <template #header>
      <span>下载数据</span>
    </template>
    <div class="flex items-center">
      <SvgIcon name="info-circle-filled" width="20px" height="20px"></SvgIcon>
      <span class="text-h4 ml-8"
        >已创建下载任务，请前往 <span class="text-link">下载管理</span> 页面进行查看</span
      >
    </div>
  </FDialog>
</template>

<style lang="scss" scoped></style>
