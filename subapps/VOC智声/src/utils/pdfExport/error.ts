export type PdfExportErrorCode =
  | 'ROOT_NOT_FOUND'
  | 'EXPORT_CONTENT_NOT_READY'
  | 'CANVAS_RENDER_FAILED'
  | 'PDF_BUILD_FAILED'

const PDF_EXPORT_ERROR_MESSAGES: Record<PdfExportErrorCode, string> = {
  ROOT_NOT_FOUND: '当前页面还未渲染完成，请稍后再试',
  EXPORT_CONTENT_NOT_READY: '请等待报告页面内容生成完毕，再导出 PDF',
  CANVAS_RENDER_FAILED: '页面截图生成失败，请稍后重试',
  PDF_BUILD_FAILED: 'PDF文件生成失败，请稍后重试'
}

/**
 * PDF 导出专用错误类型。
 * 用错误码区分失败阶段，便于前端提示和后续埋点排查。
 */
export class PdfExportError extends Error {
  code: PdfExportErrorCode

  constructor(code: PdfExportErrorCode, message = PDF_EXPORT_ERROR_MESSAGES[code]) {
    super(message)
    this.name = 'PdfExportError'
    this.code = code
  }
}

export const getPdfExportErrorMessage = (error: unknown) => {
  if (error instanceof PdfExportError) {
    return error.message
  }

  return '导出PDF失败，请稍后重试'
}
