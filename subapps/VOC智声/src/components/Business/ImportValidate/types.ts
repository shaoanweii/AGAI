import type { UploadRequestHandler } from 'element-plus'

export const ImportValidateCheckStatus = {
  Unchecked: 'unchecked',
  Checking: 'checking',
  Success: 'success',
  Failed: 'failed'
} as const

export type ImportValidateCheckStatusValue =
  (typeof ImportValidateCheckStatus)[keyof typeof ImportValidateCheckStatus]

export type ImportValidateMessageType = 'success' | 'warning' | 'error'

export interface ImportValidateStep {
  key: string | number
  title?: string
  status?: 'wait' | 'process' | 'finish' | 'success' | 'error'
}

export interface ImportValidateFileInfo {
  fileName: string
  fileBaseName: string
  fileUrl?: string
  batchId?: string
}

export interface ImportValidateCheckMessage {
  type: ImportValidateMessageType
  text: string
}

export type ImportValidateUploaderTip =
  | string
  | {
      type: 'text'
      text: string
    }
  | {
      type: 'template'
      prefix?: string
      text: string
      suffix?: string
    }

export interface ImportValidateUploadResult {
  fileName?: string
  fileBaseName?: string
  fileUrl?: string
  batchId?: string
}

export interface ImportValidateCheckResult {
  success: boolean
  batchId?: string
  messages?: ImportValidateCheckMessage[]
}

export type ImportValidateUploadRequest = UploadRequestHandler
