<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import AppDialog from '@/components/AppDialog.vue'
import {
  checkUploadLocalDataAnalysisDataSource,
  downloadLocalDataAnalysisTemplate,
  saveLocalDataAnalysisUploadDataSource,
  uploadLocalDataAnalysisDataSource
} from '@/api/localDataAnalysis'
import type {
  LocalDataAnalysisCheckUploadResult,
  LocalDataAnalysisUploadResult
} from '@/api/localDataAnalysis/types'
import {
  ImportValidateDataCheckPanel,
  ImportValidateDataUploader,
  ImportValidateStepProgress,
  useImportCheckFlow
} from '@/components/Business/ImportValidate'
import {
  ImportValidateCheckStatus,
  type ImportValidateUploadResult,
  type ImportValidateCheckResult,
  type ImportValidateStep
} from '@/components/Business/ImportValidate/types'
import { downloadFromBlob } from '@/utils/download'

defineOptions({
  name: 'CreateLocalDataAnalysisTaskDialog'
})

interface CreateTaskForm {
  dataSourceName: string
}

interface CreateLocalDataAnalysisTaskPayload {
  id?: string
  dataSourceName: string
  fileName: string
  batchId: string
}

const TASK_NAME_MAX_LENGTH = 20
const IMPORT_LIMIT = 5000
const TEMPLATE_FILE_NAME = '本地数据分析数据模板.xlsx'

const visible = defineModel<boolean>('visible', { default: false })

const props = withDefaults(
  defineProps<{
    editData?: {
      id?: string
      dataSourceName?: string
    }
  }>(),
  {
    editData: () => ({})
  }
)

const emit = defineEmits<{
  (e: 'success', payload: CreateLocalDataAnalysisTaskPayload): void
}>()

const formRef = ref()
const submitting = ref(false)
const form = reactive<CreateTaskForm>({
  dataSourceName: ''
})

const rules = {
  dataSourceName: [
    { required: true, message: '请输入数据源名称', trigger: 'blur' },
    {
      max: TASK_NAME_MAX_LENGTH,
      message: `数据源名称最多${TASK_NAME_MAX_LENGTH}个字`,
      trigger: 'blur'
    }
  ]
}

/**
 * 调用真实上传接口，返回结构保持后端原始数据包。
 * @param formData 上传表单
 * @returns 上传响应
 */
const uploadDataSourceFile = async (
  formData: FormData
): Promise<BaseResponse<LocalDataAnalysisUploadResult>> => {
  return uploadLocalDataAnalysisDataSource(formData)
}

/**
 * 调用真实校验接口，入参只传后端要求的文件名。
 * @param fileInfo 当前上传文件信息
 * @returns 校验响应
 */
const checkUploadedFile = async (fileInfo: {
  fileName: string
}): Promise<BaseResponse<LocalDataAnalysisCheckUploadResult>> => {
  return checkUploadLocalDataAnalysisDataSource({
    fileName: fileInfo.fileName
  })
}

/**
 * 将上传结果转换为页面使用的数据。
 * 上传字段只按后端返回的 key、name、url 处理。
 * @param response 上传接口返回
 * @param file 当前上传文件
 * @returns 上传结果
 */
const normalizeUploadResult = (
  response: BaseResponse<LocalDataAnalysisUploadResult>,
  file: File
): ImportValidateUploadResult => {
  return {
    fileName: response.result.key,
    fileBaseName: response.result.name || file.name,
    fileUrl: response.result.url || '',
    batchId: ''
  }
}

/**
 * 将校验结果转换为页面展示数据。
 * 展示文案直接使用后端返回的 message。
 * 只有接口调用成功且有效数据条数大于 0 时，才允许进入校验成功状态。
 * @param response 校验接口返回
 * @returns 校验结果
 */
const normalizeCheckResult = (
  response: BaseResponse<LocalDataAnalysisCheckUploadResult>
): ImportValidateCheckResult => {
  const result = response.result
  const validSuccessCount = Number(result.success || 0)
  const success =
    response.success && Number.isFinite(validSuccessCount) && validSuccessCount > 0

  return {
    success,
    batchId: result.batchId,
    messages: [
      {
        type: success ? 'success' : 'error',
        text: result.message
      }
    ]
  }
}

/**
 * 保存本地数据源。
 * @returns 保存响应
 */
const saveUploadDataSource = async () => {
  return saveLocalDataAnalysisUploadDataSource({
    id: props.editData.id,
    fileName: fileInfo.fileName,
    batchId: fileInfo.batchId || '',
    dataSourceName: form.dataSourceName.trim()
  })
}

const {
  fileInfo,
  uploadLoading,
  checkStatus,
  checkMessages,
  canStartCheck,
  hasCheckedSuccess,
  clearFile,
  resetFlow,
  uploadFile,
  startCheck
} = useImportCheckFlow({
  uploadApi: uploadDataSourceFile,
  checkApi: checkUploadedFile,
  normalizeUploadResult,
  normalizeCheckResult
})

const activeStep = computed(() => {
  if (hasCheckedSuccess.value || checkStatus.value === ImportValidateCheckStatus.Failed) return 2
  if (fileInfo.fileName) return 1
  return 0
})

const stepList = computed<ImportValidateStep[]>(() => [
  {
    key: 'taskName',
    status: form.dataSourceName.trim() ? 'success' : 'process'
  },
  {
    key: 'importData',
    status: fileInfo.fileName ? 'success' : form.dataSourceName.trim() ? 'process' : 'wait'
  },
  {
    key: 'dataCheck',
    status:
      checkStatus.value === ImportValidateCheckStatus.Failed
        ? 'error'
        : hasCheckedSuccess.value
          ? 'success'
          : fileInfo.fileName
            ? 'process'
            : 'wait'
  }
])

const canConfirm = computed(() => Boolean(form.dataSourceName.trim() && hasCheckedSuccess.value))

/**
 * 重置弹窗状态。
 */
const resetDialog = () => {
  form.dataSourceName = props.editData.dataSourceName || ''
  resetFlow()
  formRef.value?.clearValidate?.()
}

/**
 * 下载模板文件。
 */
const handleDownloadTemplate = async () => {
  const response = await downloadLocalDataAnalysisTemplate()
  if (response.success) {
    downloadFromBlob(response.result, TEMPLATE_FILE_NAME)
  }
}

/**
 * 清空当前上传文件。
 */
const handleClearFile = () => {
  clearFile()
}

/**
 * 触发校验。
 */
const handleStartCheck = async () => {
  try {
    await startCheck()
  } catch (error) {
    console.error('数据校验失败:', error)
  }
}

/**
 * 提交保存。
 * 提交过程中禁用重复点击，校验失败或保存失败都会正常兜底。
 */
const handleConfirm = async () => {
  if (submitting.value) return

  submitting.value = true
  try {
    await formRef.value?.validate?.()

    if (!hasCheckedSuccess.value) {
      ElMessage.warning('请先完成数据校验')
      return
    }

    const response = await saveUploadDataSource()
    if (response.success) {
      ElMessage.success(response.message || '保存成功')
      emit('success', {
        id: props.editData.id,
        dataSourceName: form.dataSourceName.trim(),
        fileName: fileInfo.fileName,
        batchId: fileInfo.batchId || ''
      })
      visible.value = false
      return
    }

    ElMessage.error(response.message || '保存失败')
  } catch (error) {
    console.error('保存本地数据分析任务失败:', error)
    ElMessage.error(error instanceof Error ? error.message : '保存失败')
  } finally {
    submitting.value = false
  }
}

/**
 * 点击取消按钮关闭弹窗。
 */
const handleCancel = () => {
  visible.value = false
}

/**
 * 点击确定按钮提交。
 */
const handleFooterConfirm = () => {
  handleConfirm()
}

watch(
  visible,
  value => {
    if (value) resetDialog()
  },
  { flush: 'post' }
)
</script>

<template>
  <AppDialog
    v-model:visible="visible"
    width="680px"
    destroy-on-close
    @close="resetDialog"
  >
    <template #header>{{ props.editData.id ? '修改任务' : '新建任务' }}</template>

    <div class="create-task-dialog">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="98px"
        class="create-task-dialog__form"
      >
        <ImportValidateStepProgress
          class="create-task-dialog__steps"
          :steps="stepList"
          :active-step="activeStep"
        >
          <template #default="{ step }">
            <el-form-item
              v-if="step.key === 'taskName'"
              label="数据源名称"
              prop="dataSourceName"
              required
            >
              <el-input
                v-model.trim="form.dataSourceName"
                :maxlength="TASK_NAME_MAX_LENGTH"
                placeholder="请输入数据源名称"
              />
            </el-form-item>

            <el-form-item
              v-else-if="step.key === 'importData'"
              label="导入数据"
              required
              class="create-task-dialog__upload-item"
            >
              <ImportValidateDataUploader
                :file-info="fileInfo"
                :upload-request="uploadFile"
                :upload-loading="uploadLoading"
                :tips="[
                  `文件支持excel格式的文件，文件数据限制${IMPORT_LIMIT}条以内；`,
                  {
                    type: 'template',
                    prefix: '请先下载模板',
                    text: '数据模板',
                    suffix: '先按照格式进行调整后上传；'
                  }
                ]"
                @download-template="handleDownloadTemplate"
                @clear="handleClearFile"
              />
            </el-form-item>

            <el-form-item
              v-else-if="step.key === 'dataCheck'"
              label="数据校验"
              required
              class="create-task-dialog__check-item"
            >
              <ImportValidateDataCheckPanel
                :status="checkStatus"
                :messages="checkMessages"
                :disabled="!canStartCheck"
                @check="handleStartCheck"
              />
            </el-form-item>
          </template>
        </ImportValidateStepProgress>
      </el-form>
    </div>

    <template #footer>
      <div class="app-dialog__footer">
        <div class="app-dialog__footer-btns">
          <el-button class="app-dialog__btn-cancel" @click="handleCancel">取消</el-button>
          <el-button
            class="app-dialog__btn-confirm"
            type="primary"
            :loading="submitting"
            :disabled="!canConfirm || submitting"
            @click="handleFooterConfirm"
          >
            确定
          </el-button>
        </div>
      </div>
    </template>
  </AppDialog>
</template>

<style scoped lang="scss">
.create-task-dialog {
  padding: 0 48px 0 16px;
}

.create-task-dialog__steps {
  width: 100%;
}

.create-task-dialog__form {
  min-width: 0;

  :deep(.el-form-item) {
    margin-bottom: 18px;
  }

  :deep(.el-form-item__content) {
    min-width: 0;
  }

  :deep(.el-input__wrapper) {
    border-radius: 2px;
  }
}

.create-task-dialog__upload-item {
  :deep(.el-form-item__content) {
    display: block;
  }
}

.create-task-dialog__check-item {
  margin-bottom: 0 !important;

  :deep(.el-form-item__content) {
    display: block;
  }
}

@media (max-width: 768px) {
  .create-task-dialog {
    gap: 8px;
  }

  .create-task-dialog__form {
    :deep(.el-form-item__label) {
      width: 76px !important;
    }
  }
}
</style>
