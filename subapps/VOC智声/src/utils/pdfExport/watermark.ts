import { getWatermarkDataUrl, getWatermarkTileSize } from '@/utils'
import { EXPORT_WATERMARK_CONFIG } from './constants'

const applyInlineStyles = (element: HTMLElement, styles: Partial<CSSStyleDeclaration>) => {
  Object.assign(element.style, styles)
}

/**
 * 给当前导出区域追加临时水印层，截图完成后由调用方移除。
 */
export const appendWatermarkLayer = (
  exportRoot: HTMLElement,
  watermarkName: string,
  watermarkUserName: string
) => {
  applyInlineStyles(exportRoot, {
    position: exportRoot.style.position || 'relative'
  })

  const tileSize = getWatermarkTileSize(EXPORT_WATERMARK_CONFIG.density)
  const watermarkDataUrl = getWatermarkDataUrl(
    watermarkName || '--',
    watermarkUserName || '--',
    EXPORT_WATERMARK_CONFIG.color,
    EXPORT_WATERMARK_CONFIG.opacity,
    EXPORT_WATERMARK_CONFIG.fontSize,
    EXPORT_WATERMARK_CONFIG.angle,
    EXPORT_WATERMARK_CONFIG.density
  )

  if (!watermarkDataUrl) {
    return null
  }

  const watermark = exportRoot.ownerDocument.createElement('div')
  watermark.setAttribute('data-pdf-export-watermark', 'true')
  applyInlineStyles(watermark, {
    position: 'absolute',
    inset: '0',
    pointerEvents: 'none',
    zIndex: '999',
    backgroundImage: `url(${watermarkDataUrl})`,
    backgroundRepeat: 'repeat',
    backgroundSize: `${tileSize}px ${tileSize}px`,
    backgroundPosition: '0 0'
  })
  exportRoot.appendChild(watermark)

  return watermark
}
