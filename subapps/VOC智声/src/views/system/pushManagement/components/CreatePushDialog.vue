<script setup lang="ts">
import dayjs from 'dayjs'
import { computed, nextTick, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import AppDialog from '@/components/AppDialog.vue'
import {
  checkUploadPushMessage,
  downloadPushMessageTemplate,
  saveUploadPushMessage,
  uploadPushMessageData
} from '@/api/system/pushManagement'
import type {
  PushMessageCheckUploadResult,
  PushMessageUploadResult
} from '@/api/system/pushManagement/types'
import {
  ImportValidateDataCheckPanel,
  ImportValidateDataUploader,
  ImportValidateStepProgress,
  useImportCheckFlow
} from '@/components/Business/ImportValidate'
import {
  ImportValidateCheckStatus,
  type ImportValidateCheckResult,
  type ImportValidateStep,
  type ImportValidateUploadResult
} from '@/components/Business/ImportValidate/types'
import { downloadFromBlob } from '@/utils/download'

defineOptions({
  name: 'CreatePushDialog'
})

type PushType = '1' | '2'

interface PushForm {
  pushType: PushType
  pushTime: string
}

const TEMPLATE_FILE_NAME = '推送用户模板.xlsx'

const visible = defineModel<boolean>('visible', { default: false })

const emit = defineEmits<{
  (e: 'success'): void
}>()

const formRef = ref()
const submitting = ref(false)

const form = reactive<PushForm>({
  pushType: '1',
  pushTime: ''
})

const rules = {
  pushTime: [
    {
      validator: (_rule: unknown, value: string, callback: (error?: Error) => void) => {
        if (form.pushType === '1') {
          callback()
          return
        }

        if (!value) {
          callback(new Error('请选择推送开始时间'))
          return
        }

        callback()
      },
      trigger: 'change'
    }
  ]
}

/**
 * 上传推送用户文件。
 * @param formData 上传表单
 * @returns 上传响应
 */
const uploadPushFile = async (
  formData: FormData
): Promise<BaseResponse<PushMessageUploadResult>> => {
  return uploadPushMessageData(formData)
}

/**
 * 将上传响应转换为统一文件信息。
 * @param response 上传响应
 * @param file 当前文件
 * @returns 上传文件信息
 */
const normalizeUploadResult = (
  response: BaseResponse<PushMessageUploadResult>,
  file: File
): ImportValidateUploadResult => {
  const result = response.result

  return {
    fileName: result.key,
    fileBaseName: result.name || file.name,
    fileUrl: result?.url || '',
    batchId: ''
  }
}

/**
 * 调用推送用户数据校验接口。
 * @param fileInfo 当前上传文件信息
 * @returns 校验响应
 */
const checkUploadedFile = async (fileInfo: {
  fileName: string
}): Promise<BaseResponse<PushMessageCheckUploadResult>> => {
  return checkUploadPushMessage({
    fileName: fileInfo.fileName
  })
}

/**
 * 将校验结果转换为页面展示数据。
 * 展示文案直接使用后端返回的 message。
 * 只有接口调用成功且有效数据条数大于 0 时，才允许进入校验成功状态。
 * @param response 校验响应
 * @returns 校验结果
 */
const normalizeCheckResult = (
  response: BaseResponse<PushMessageCheckUploadResult>
): ImportValidateCheckResult => {
  const result = response.result
  const validSuccessCount = Number(result.success || 0)
  const success = response.success && Number.isFinite(validSuccessCount) && validSuccessCount > 0

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
  uploadApi: uploadPushFile,
  checkApi: checkUploadedFile,
  normalizeUploadResult,
  normalizeCheckResult
})

const isSchedulePush = computed(() => form.pushType === '2')
const hasValidPushTime = computed(() => {
  return form.pushType === '1' || Boolean(form.pushTime)
})
const canConfirm = computed(() => {
  return Boolean(hasCheckedSuccess.value && hasValidPushTime.value && !uploadLoading.value)
})
const activeStep = computed(() => {
  if (hasCheckedSuccess.value) return 2
  if (fileInfo.fileName) return 1
  return 0
})

const stepList = computed<ImportValidateStep[]>(() => [
  {
    key: 'importData',
    status: fileInfo.fileName ? 'success' : 'process'
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
  },
  {
    key: 'pushTime',
    status: hasCheckedSuccess.value ? (hasValidPushTime.value ? 'success' : 'process') : 'wait'
  }
])

/**
 * 限制定时推送不能选择当前日期之前的日期。
 * @param date 面板日期
 * @returns 是否禁用
 */
const disablePastDate = (date: Date): boolean => {
  return dayjs(date).isBefore(dayjs().startOf('day'))
}

/**
 * 重置弹窗状态。
 */
const resetDialog = () => {
  form.pushType = '1'
  form.pushTime = ''
  resetFlow()
  formRef.value?.clearValidate?.()
}

/**
 * 下载推送用户模板。
 */
const handleDownloadTemplate = async () => {
  const response = await downloadPushMessageTemplate()

  if (response.success) {
    downloadFromBlob(response.result, TEMPLATE_FILE_NAME)
  }
}

/**
 * 清空上传文件。
 */
const handleClearFile = () => {
  clearFile()
}

/**
 * 触发数据校验。
 */
const handleStartCheck = async () => {
  try {
    await startCheck()
  } catch (error) {
    console.error('推送用户数据校验失败:', error)
  }
}

/**
 * 生成保存接口入参。
 * @returns 保存参数
 */
const buildSaveParams = () => {
  return {
    id: '',
    fileName: fileInfo.fileName,
    batchId: fileInfo.batchId || '',
    pushType: form.pushType,
    pushTime: form.pushType === '2' ? form.pushTime : ''
  }
}

/**
 * 提交保存新建推送。
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

    if (form.pushType === '2' && dayjs(form.pushTime).isBefore(dayjs())) {
      ElMessage.warning('定时推送开始时间不能早于当前时间')
      return
    }

    const response = await saveUploadPushMessage(buildSaveParams())
    if (response.success) {
      ElMessage.success(response.message || '新建推送成功')
      emit('success')
      visible.value = false
      return
    }

    ElMessage.error(response.message || '新建推送失败')
  } catch (error) {
    console.error('新建推送失败:', error)
    ElMessage.error(error instanceof Error ? error.message : '新建推送失败')
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

watch(
  visible,
  value => {
    if (value) resetDialog()
  },
  { flush: 'post' }
)

watch(
  () => form.pushType,
  async value => {
    if (value === '1') {
      form.pushTime = ''
      await nextTick()
      formRef.value?.clearValidate?.('pushTime')
    }
  }
)
</script>

<template>
  <AppDialog v-model:visible="visible" width="680px" destroy-on-close @close="resetDialog">
    <template #header>新建推送</template>

    <div class="create-push-dialog__notice">
      <SvgIcon
        name="info-circle-filled"
        width="16px"
        height="16px"
        class="create-push-dialog__notice-icon"
      />
      <span class="create-push-dialog__notice-text">
        温馨提醒：同一用户在一个自然日内仅可推送一次，多次推送均做推送失败处理。
      </span>
    </div>
    <div class="create-push-dialog">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="92px"
        class="create-push-dialog__form"
      >
        <ImportValidateStepProgress
          class="create-push-dialog__steps"
          :steps="stepList"
          :active-step="activeStep"
        >
          <template #default="{ step }">
            <el-form-item
              v-if="step.key === 'importData'"
              label="导入数据"
              required
              class="create-push-dialog__upload-item"
            >
              <ImportValidateDataUploader
                :file-info="fileInfo"
                :upload-request="uploadFile"
                :upload-loading="uploadLoading"
                :tips="[
                  '文件支持 excel 格式的文件，文件数据限制10000条以内；',
                  {
                    type: 'template',
                    prefix: '请先下载模板',
                    text: '推送用户模板',
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
              class="create-push-dialog__check-item"
            >
              <ImportValidateDataCheckPanel
                :status="checkStatus"
                :messages="checkMessages"
                :disabled="!canStartCheck"
                @check="handleStartCheck"
              />
            </el-form-item>

            <el-form-item
              v-else-if="step.key === 'pushTime'"
              label="推送时间"
              prop="pushTime"
              :required="isSchedulePush"
              class="create-push-dialog__time-item"
            >
              <div class="create-push-dialog__time-row">
                <el-radio-group v-model="form.pushType">
                  <el-radio value="1">立即推送</el-radio>
                  <el-radio value="2">定时推送</el-radio>
                </el-radio-group>

                <el-date-picker
                  v-if="isSchedulePush"
                  v-model="form.pushTime"
                  type="datetime"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  placeholder="请选择推送开始时间"
                  :disabled-date="disablePastDate"
                  class="create-push-dialog__datetime"
                />
              </div>
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
            @click="handleConfirm"
          >
            确定
          </el-button>
        </div>
      </div>
    </template>
  </AppDialog>
</template>

<style scoped lang="scss">
.create-push-dialog {
  padding: 0 48px 0 16px;
}

.create-push-dialog__notice {
  min-height: 40px;
  margin-bottom: 20px;
  padding: 9px 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  background: #eaf6ff;
  border: 1px solid #b7dcff;
  border-radius: 4px;
  box-sizing: border-box;
}

.create-push-dialog__notice-icon {
  flex: 0 0 auto;
}

.create-push-dialog__notice-text {
  min-width: 0;
  font-size: 14px;
  line-height: 22px;
  color: #4e5969;
}

.create-push-dialog__steps {
  width: 100%;
}

.create-push-dialog__form {
  min-width: 0;

  :deep(.el-form-item) {
    margin-bottom: 18px;
  }

  :deep(.el-form-item__content) {
    min-width: 0;
  }
}

.create-push-dialog__upload-item,
.create-push-dialog__check-item,
.create-push-dialog__time-item {
  :deep(.el-form-item__content) {
    display: block;
  }
}

.create-push-dialog__check-item,
.create-push-dialog__time-item {
  margin-bottom: 0 !important;
}

.create-push-dialog__time-row {
  min-height: 32px;
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.create-push-dialog__datetime {
  width: 240px;
}

@media (max-width: 768px) {
  .create-push-dialog {
    padding-right: 12px;
  }

  .create-push-dialog__notice {
    align-items: flex-start;
    padding: 8px 12px;
  }

  .create-push-dialog__form {
    :deep(.el-form-item__label) {
      width: 76px !important;
    }
  }

  .create-push-dialog__datetime {
    width: 100%;
  }
}
</style>
