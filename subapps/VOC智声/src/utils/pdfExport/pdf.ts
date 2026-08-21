import jsPDF from 'jspdf'
import { MAX_PDF_PAGE_EDGE, PDF_EXPORT_IMAGE_FORMAT, PDF_EXPORT_IMAGE_QUALITY } from './constants'
import { PdfExportError } from './error'

const resolvePdfImageSize = (canvas: HTMLCanvasElement) => {
  const scale = Math.min(1, MAX_PDF_PAGE_EDGE / Math.max(canvas.width, canvas.height))

  return {
    width: Math.max(1, Math.floor(canvas.width * scale)),
    height: Math.max(1, Math.floor(canvas.height * scale))
  }
}

const resizeCanvasForPdf = (sourceCanvas: HTMLCanvasElement, width: number, height: number) => {
  if (sourceCanvas.width === width && sourceCanvas.height === height) {
    return sourceCanvas
  }

  const outputCanvas = document.createElement('canvas')
  outputCanvas.width = width
  outputCanvas.height = height

  const context = outputCanvas.getContext('2d')
  if (!context) {
    throw new PdfExportError('PDF_BUILD_FAILED')
  }

  context.imageSmoothingEnabled = true
  context.imageSmoothingQuality = 'high'
  context.drawImage(sourceCanvas, 0, 0, width, height)

  return outputCanvas
}

/**
 * 将整张页面截图写入单页 PDF。
 * 使用 JPEG 压缩图片内容，减少整页截图式 PDF 的文件体积。
 */
export const buildPdfBlob = async (canvas: HTMLCanvasElement) => {
  if (!canvas.width || !canvas.height) {
    throw new PdfExportError('PDF_BUILD_FAILED')
  }

  const { width, height } = resolvePdfImageSize(canvas)
  const pdfCanvas = resizeCanvasForPdf(canvas, width, height)
  const orientation = width >= height ? 'landscape' : 'portrait'
  const pdf = new jsPDF({
    orientation,
    unit: 'pt',
    format: [width, height],
    compress: true
  })
  const imageData = pdfCanvas.toDataURL('image/jpeg', PDF_EXPORT_IMAGE_QUALITY)

  pdf.addImage(imageData, PDF_EXPORT_IMAGE_FORMAT, 0, 0, width, height)

  return pdf.output('blob')
}
