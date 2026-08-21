<script setup lang="ts">
import { computed, ref } from 'vue'
import { Delete, Loading, UploadFilled } from '@element-plus/icons-vue'
import type { UploadInstance } from 'element-plus'
import type {
  ImportValidateFileInfo,
  ImportValidateUploaderTip,
  ImportValidateUploadRequest
} from '@/components/Business/ImportValidate/types'

defineOptions({
  name: 'ImportValidateDataUploader'
})

const props = withDefaults(
  defineProps<{
    fileInfo?: ImportValidateFileInfo
    uploadRequest: ImportValidateUploadRequest
    uploadLoading?: boolean
    disabled?: boolean
    accept?: string
    templateText?: string
    templateTip?: string
    tips?: ImportValidateUploaderTip[]
    uploadText?: string
    reuploadText?: string
    showTemplateLink?: boolean
  }>(),
  {
    uploadLoading: false,
    disabled: false,
    accept:
      'application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,.csv',
    templateText: '数据模板',
    templateTip: '请先下载模板',
    tips: () => [
      '文件支持 excel/csv 格式的文件',
      {
        type: 'template',
        prefix: '请先下载模板',
        text: '数据模板',
        suffix: '，并按照格式进行调整后上传'
      }
    ],
    uploadText: '上传文件',
    reuploadText: '重新上传',
    showTemplateLink: true
  }
)

const emit = defineEmits<{
  (e: 'download-template'): void
  (e: 'clear'): void
  (e: 'preview'): void
}>()

const uploadRef = ref<UploadInstance>()

const hasFile = computed(() => Boolean(props.fileInfo?.fileName || props.fileInfo?.fileBaseName))
const displayFileName = computed(
  () => props.fileInfo?.fileBaseName || props.fileInfo?.fileName || ''
)
const normalizedTips = computed<ImportValidateUploaderTip[]>(() => {
  const hasTemplateTip = props.tips.some(tip => typeof tip !== 'string' && tip.type === 'template')
  const tips = hasTemplateTip
    ? props.tips
    : [
        ...props.tips,
        {
          type: 'template' as const,
          prefix: props.templateTip,
          text: props.templateText
        }
      ]

  if (props.showTemplateLink) return tips

  return tips.filter(tip => typeof tip === 'string' || tip.type !== 'template')
})

const isTemplateTip = (
  tip: ImportValidateUploaderTip
): tip is Extract<ImportValidateUploaderTip, { type: 'template' }> => {
  return typeof tip !== 'string' && tip.type === 'template'
}

/**
 * 清理 Upload 内部文件状态，保证同一文件可重复选择。
 */
const clearUploadSelection = () => {
  uploadRef.value?.clearFiles()
}

/**
 * 清空当前文件并同步重置 Upload 内部状态。
 */
const handleClear = () => {
  clearUploadSelection()
  emit('clear')
}
</script>

<template>
  <div class="import-data-uploader">
    <el-upload
      ref="uploadRef"
      :accept="accept"
      :disabled="disabled || uploadLoading"
      :show-file-list="false"
      :http-request="uploadRequest"
      class="import-data-uploader__upload"
    >
      <template #trigger>
        <button
          type="button"
          class="import-data-uploader__trigger"
          :class="{ 'is-disabled': disabled || uploadLoading }"
          :disabled="disabled || uploadLoading"
          @click="clearUploadSelection"
        >
          <el-icon v-if="uploadLoading" class="import-data-uploader__icon is-loading">
            <Loading />
          </el-icon>
          <el-icon v-else class="import-data-uploader__icon">
            <UploadFilled />
          </el-icon>
          <span>{{ uploadLoading ? '上传中...' : hasFile ? reuploadText : uploadText }}</span>
        </button>
      </template>
    </el-upload>

    <div v-if="hasFile" class="import-data-uploader__file">
      <i class="iconfont icon-file-excel-2-line import-data-uploader__file-icon"></i>
      <button type="button" class="import-data-uploader__file-name" @click="emit('preview')">
        {{ displayFileName }}
      </button>
      <el-icon class="import-data-uploader__delete" @click="handleClear">
        <Delete />
      </el-icon>
    </div>

    <div class="import-data-uploader__tips">
      <div v-for="(tip, index) in normalizedTips" :key="index">
        <template v-if="typeof tip === 'string'">
          {{ tip }}
        </template>
        <template v-else-if="isTemplateTip(tip)">
          {{ tip.prefix }}
          <el-link
            type="primary"
            class="import-data-uploader__template"
            @click="emit('download-template')"
          >
            《{{ tip.text }}》
          </el-link>
          {{ tip.suffix }}
        </template>
        <template v-else>
          {{ tip.text }}
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.import-data-uploader {
  width: 100%;
}

.import-data-uploader__upload {
  width: 100%;
  background: #eaf3ff;
  text-align: center;

  :deep(.el-upload) {
    width: 100%;
  }
}

.import-data-uploader__trigger {
  width: 100%;
  height: 76px;
  padding-top: 2px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 0;
  border-radius: 2px;
  background: #eaf3ff;
  font-weight: 500;
  font-size: 14px;
  color: #4e5969;
  cursor: pointer;
}

.import-data-uploader__trigger.is-disabled {
  cursor: not-allowed;
  color: #929aa6;
  background: #f2f3f5;
}

.import-data-uploader__icon {
  font-size: 18px;
  color: #5f6a7a;
}

.import-data-uploader__icon.is-loading {
  animation: import-data-uploader-spin 1s linear infinite;
}

.import-data-uploader__file {
  min-height: 30px;
  margin-top: 12px;
  padding: 4px 12px;
  display: flex;
  align-items: center;
  background: #f7f8fa;
  border-radius: 2px;
}

.import-data-uploader__file-icon {
  flex: 0 0 auto;
  color: #5f6a7a;
}

.import-data-uploader__file-name {
  min-width: 0;
  flex: 1 1 auto;
  margin-left: 8px;
  padding: 0;
  border: 0;
  background: transparent;
  color: #1d2129;
  text-align: left;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: pointer;
}

.import-data-uploader__delete {
  flex: 0 0 auto;
  margin-left: 8px;
  color: #5f6a7a;
  cursor: pointer;
}

.import-data-uploader__tips {
  margin-top: 8px;
  font-size: 12px;
  line-height: 22px;
  color: #929aa6;
}

.import-data-uploader__template {
  font-size: 12px !important;
  vertical-align: baseline;
}

@keyframes import-data-uploader-spin {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}
</style>
