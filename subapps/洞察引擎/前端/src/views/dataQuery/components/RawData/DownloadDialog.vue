<script setup lang="ts">
import { useRouter } from 'vue-router'

defineOptions({
  name: 'RawDataDownloadDialog'
})

const router = useRouter()
const visible = defineModel<boolean>({ default: false })

const closeDialog = () => {
  visible.value = false
}

const handleGo = async () => {
  await router.push('/settings/download')
  closeDialog()
}

const handleLater = () => {
  closeDialog()
}
</script>

<template>
  <AppDialog
    v-model:visible="visible"
    title="下载数据"
    width="480px"
    body-class="raw-data-download-dialog__body"
    class="raw-data-download-dialog"
  >
    <div class="raw-data-download-dialog__content">
      <SvgIcon class="raw-data-download-dialog__icon" name="info-circle-filled" color="#1677ff" />
      <span class="raw-data-download-dialog__text">
        已创建下载任务，请前往
        <span class="raw-data-download-dialog__link" @click="handleGo">下载管理</span>
        页面进行查看
      </span>
    </div>

    <template #footer>
      <el-button class="raw-data-download-dialog__btn" @click="handleLater">稍后再说</el-button>
      <el-button class="raw-data-download-dialog__btn" type="primary" @click="handleGo">
        前往查看
      </el-button>
    </template>
  </AppDialog>
</template>

<style scoped lang="scss">
.raw-data-download-dialog__content {
  min-height: 40px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.raw-data-download-dialog__icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.raw-data-download-dialog__text {
  font-weight: 400;
  font-size: 16px;
  color: #4e5969;
  line-height: 22px;
}

.raw-data-download-dialog__link {
  color: #1677ff;
  cursor: pointer;
  margin: 0 4px;
}

.raw-data-download-dialog__btn {
  height: 32px;
  font-size: 14px;
  line-height: 22px;
}

:deep(.raw-data-download-dialog .app-dialog__header) {
  background: #fff;
}

:deep(.raw-data-download-dialog__body) {
  padding-top: 30px !important;
  padding-bottom: 30px !important;
}
</style>
