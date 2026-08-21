import { computed, reactive, ref } from 'vue'
import type { UploadRequestOptions } from 'element-plus'
import {
  ImportValidateCheckStatus,
  type ImportValidateCheckMessage,
  type ImportValidateCheckResult,
  type ImportValidateCheckStatusValue,
  type ImportValidateFileInfo,
  type ImportValidateUploadResult
} from './types'

interface UseImportCheckFlowOptions<TUploadResponse = unknown, TCheckResponse = unknown> {
  uploadApi: (formData: FormData, file: UploadRequestOptions['file']) => Promise<TUploadResponse>
  checkApi: (fileInfo: ImportValidateFileInfo) => Promise<TCheckResponse>
  buildUploadFormData?: (file: UploadRequestOptions['file']) => FormData
  normalizeUploadResult: (
    response: TUploadResponse,
    file: UploadRequestOptions['file']
  ) => ImportValidateUploadResult
  normalizeCheckResult: (response: TCheckResponse) => ImportValidateCheckResult
  initialFileInfo?: Partial<ImportValidateFileInfo>
}

const createEmptyFileInfo = (): ImportValidateFileInfo => ({
  fileName: '',
  fileBaseName: '',
  fileUrl: '',
  batchId: ''
})

/**
 * 将页面归一化后的业务校验结果映射为统一状态。
 * 这里不再关心接口原始结构，只依赖业务层明确给出的 success。
 * @param result 页面归一化后的校验结果
 * @returns 校验状态
 */
const resolveCheckStatus = (result: ImportValidateCheckResult): ImportValidateCheckStatusValue => {
  return result.success ? ImportValidateCheckStatus.Success : ImportValidateCheckStatus.Failed
}

/**
 * 管理导入文件与数据校验公共流程，不处理任务名称、推送时间和最终提交等业务字段。
 */
export const useImportCheckFlow = <TUploadResponse = unknown, TCheckResponse = unknown>(
  options: UseImportCheckFlowOptions<TUploadResponse, TCheckResponse>
) => {
  const fileInfo = reactive<ImportValidateFileInfo>({
    ...createEmptyFileInfo(),
    ...options.initialFileInfo
  })
  const uploadLoading = ref(false)
  const checkStatus = ref<ImportValidateCheckStatusValue>(ImportValidateCheckStatus.Unchecked)
  const checkMessages = ref<ImportValidateCheckMessage[]>([])

  const canStartCheck = computed(() => Boolean(fileInfo.fileName) && !uploadLoading.value)
  const hasCheckedSuccess = computed(() => checkStatus.value === ImportValidateCheckStatus.Success)
  const canSubmitBase = computed(() => canStartCheck.value && hasCheckedSuccess.value)

  const resetCheck = () => {
    checkStatus.value = ImportValidateCheckStatus.Unchecked
    checkMessages.value = []
    fileInfo.batchId = ''
  }

  const setFileInfo = (nextFileInfo: Partial<ImportValidateFileInfo>) => {
    Object.assign(fileInfo, createEmptyFileInfo(), nextFileInfo)
  }

  const clearFile = () => {
    setFileInfo(createEmptyFileInfo())
    resetCheck()
  }

  const resetFlow = () => {
    setFileInfo(options.initialFileInfo || createEmptyFileInfo())
    resetCheck()
    uploadLoading.value = false
  }

  const uploadFile = async (uploadOptions: UploadRequestOptions) => {
    const { file, onSuccess, onError } = uploadOptions
    const buildFormData =
      options.buildUploadFormData ||
      ((uploadFile: UploadRequestOptions['file']) => {
        const formData = new FormData()
        formData.append('file', uploadFile)
        return formData
      })

    uploadLoading.value = true

    try {
      const response = await options.uploadApi(buildFormData(file), file)
      const nextFileInfo = options.normalizeUploadResult(response, file)

      setFileInfo({
        ...nextFileInfo,
        fileBaseName: nextFileInfo.fileBaseName || file.name
      })
      resetCheck()
      onSuccess?.(response)
      return response
    } catch (error: any) {
      onError?.(error)
      throw error
    } finally {
      uploadLoading.value = false
    }
  }

  const startCheck = async () => {
    if (!canStartCheck.value || checkStatus.value === ImportValidateCheckStatus.Checking) return

    checkStatus.value = ImportValidateCheckStatus.Checking
    checkMessages.value = []

    try {
      const response = await options.checkApi({ ...fileInfo })
      const result = options.normalizeCheckResult(response)

      fileInfo.batchId = result.batchId || fileInfo.batchId || ''
      checkMessages.value = result.messages || []
      checkStatus.value = resolveCheckStatus(result)

      return result
    } catch (error: any) {
      checkStatus.value = ImportValidateCheckStatus.Failed
      checkMessages.value = [
        {
          type: 'error',
          text: error?.message || '校验失败，请重新校验。'
        }
      ]
      throw error
    }
  }

  return {
    fileInfo,
    uploadLoading,
    checkStatus,
    checkMessages,
    canStartCheck,
    hasCheckedSuccess,
    canSubmitBase,
    setFileInfo,
    clearFile,
    resetCheck,
    resetFlow,
    uploadFile,
    startCheck
  }
}
