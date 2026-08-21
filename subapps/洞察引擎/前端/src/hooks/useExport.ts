// useExportExcel.js
import { ref } from 'vue'
// import request from '@/api/index'

/**
 * 导出文件
 */
export function useExport() {
  const MIME_TYPE = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  const FILE_NAME = 'exported_data.xlsx'
  const DEFAULT_FILE_NAME = 'downloaded_file'
  // 导出文件状态
  const exporting = ref(false)
  /**
   * 导出文件
   * @param api 导出接口
   * @param params 导出接口入参
   * @param mimeType 导出文件类型
   */
  const exportFile = async (
    api: (params?: any) => Promise<any>,
    params?: any,
    mimeType?: string
  ) => {
    try {
      exporting.value = true

      // 向后端请求文件流
      const response: any = await api(params)
      const contentDispositionHeader = response.headers['content-disposition']
      const fileName = extractFileNameFromContentDisposition(contentDispositionHeader)
      handleFileResponse(response.data, fileName, mimeType)
    } catch (error) {
      console.error('Error exporting:', error)
    } finally {
      exporting.value = false
    }
  }
  // 导出文件流
  const handleFileResponse = (fileBlob: any, fileName?: string, mimeType?: string) => {
    const type = mimeType || MIME_TYPE
    const blob = new Blob([fileBlob], { type })
    const url = window.URL.createObjectURL(blob)

    const a = document.createElement('a')
    a.href = url
    a.download = fileName || FILE_NAME

    a.click()

    window.URL.revokeObjectURL(url)
  }
  /**
   * 提取请求头中的文件名
   * @param contentDispositionHeader
   */
  const extractFileNameFromContentDisposition = (contentDispositionHeader: string) => {
    const matches = contentDispositionHeader?.match(/filename=([^;]*)/)

    if (matches && matches.length > 1) {
      const encodedFilename = matches[1]
      let str = decodeURIComponent(encodedFilename)
      if (str.includes("utf-8''")) {
        str = str?.substring(7)
      }
      return str
    }

    return DEFAULT_FILE_NAME
  }

  return {
    exportFile,
    exporting
  }
}
