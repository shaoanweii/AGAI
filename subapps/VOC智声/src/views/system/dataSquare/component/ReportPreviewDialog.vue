<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import AppDialog from '@/components/AppDialog.vue'
import type { DataPlazaReportItem } from '@/api/dataPlaza/types'

defineOptions({
  name: 'ReportPreviewDialog'
})

const visible = defineModel<boolean>('visible', { default: false })

const props = defineProps<{
  report: DataPlazaReportItem | null
}>()

const router = useRouter()
const iframeLoading = ref(false)

const dialogTitle = computed(() => {
  return '报告预览'
})

const previewUrl = computed(() => {
  if (!props.report?.id) {
    return ''
  }

  return router.resolve({
    name: 'H5DataSquareReportDetail',
    query: {
      reportId: props.report.id,
      reportName: props.report.reportName || '',
      preview: '1',
      previewTitle: props.report.reportName || ''
    }
  }).href
})

/**
 * 关闭预览时清理加载态，避免下一次打开短暂显示旧状态。
 */
const handleClose = () => {
  iframeLoading.value = false
}

/**
 * iframe 页面加载完成后关闭遮罩。
 */
const handleIframeLoad = () => {
  iframeLoading.value = false
}

watch(
  () => [visible.value, props.report?.id],
  ([nextVisible]) => {
    iframeLoading.value = Boolean(nextVisible && previewUrl.value)
  }
)

</script>

<template>
  <AppDialog
    v-model:visible="visible"
    width="640px"
    class="report-preview-dialog"
    destroy-on-close
    :show-footer="false"
    @close="handleClose"
  >
    <template #header>{{ dialogTitle }}</template>

    <div class="report-preview" v-loading="iframeLoading">
      <div class="report-preview__device">
        <iframe
          v-if="previewUrl"
          :key="previewUrl"
          class="report-preview__iframe"
          :src="previewUrl"
          title="报告预览"
          @load="handleIframeLoad"
        ></iframe>
        <el-empty v-else description="报告信息缺失" />
      </div>
    </div>
  </AppDialog>
</template>

<style scoped lang="scss">
:global(.report-preview-dialog.app-dialog .el-dialog__body) {
  padding: 16px 24px 24px !important;
  overflow: hidden;
}

.report-preview {
  display: flex;
  justify-content: center;
  min-height: 640px;
}

.report-preview__device {
  width: 576px;
  height: min(812px, calc(100vh - 150px));
  min-height: 640px;
  overflow: hidden;
  border: 1px solid #e5e6eb;
  border-radius: 18px;
  background: #f5f7fa;
  box-shadow: 0 12px 32px rgba(29, 33, 41, 0.12);
}

.report-preview__iframe {
  width: 100%;
  height: 100%;
  display: block;
  border: 0;
  background: #f5f7fa;
}
</style>
