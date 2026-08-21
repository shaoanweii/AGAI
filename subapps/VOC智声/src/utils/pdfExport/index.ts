import dayjs from 'dayjs'
import { snapdom } from '@zumer/snapdom'
import { downloadFromBlob } from '@/utils/download'
import { MAX_PDF_PAGE_EDGE, PDF_EXPORT_SELECTORS, SNAPDOM_EXPORT_DPR } from './constants'
import {
  getExportRouteConfig,
  isPdfExportRouteName,
  shouldShowLayoutPdfExportButton,
  shouldShowPdfExportButton,
  shouldShowScenePdfExportButton
} from './config'
import { PdfExportError } from './error'
import { buildPdfBlob } from './pdf'
import type { ExportPagePdfOptions, PdfExportRouteConfig } from './types'

export {
  isPdfExportRouteName,
  shouldShowLayoutPdfExportButton,
  shouldShowPdfExportButton,
  shouldShowScenePdfExportButton
}

export type { ExportPagePdfOptions, PdfExportRouteName, PdfExportTrigger } from './types'

type StyledElementSnapshot = {
  element: HTMLElement
  cssText: string
  scrollTop: number
  scrollLeft: number
}

type ScrollElementSnapshot = {
  element: HTMLElement
  scrollTop: number
  scrollLeft: number
}

const PDF_EXPORT_EXPAND_SELECTOR = [
  PDF_EXPORT_SELECTORS.section,
  PDF_EXPORT_SELECTORS.expand,
  PDF_EXPORT_SELECTORS.cardBody,
  PDF_EXPORT_SELECTORS.reportSummary,
  PDF_EXPORT_SELECTORS.reportSummaryContent
].join(',')

const buildFilename = ({ title, startDate, endDate }: ExportPagePdfOptions) => {
  const baseTitle = (title || '场景分析').replace(/[\\/:*?"<>|]/g, '_')
  if (startDate && endDate) {
    return `${baseTitle}_${startDate}_${endDate}.pdf`
  }
  return `${baseTitle}_${dayjs().format('YYYY-MM-DD_HH-mm-ss')}.pdf`
}

const waitForNextFrame = () =>
  new Promise<void>(resolve => {
    window.requestAnimationFrame(() => resolve())
  })

const assertExportReady = (root: HTMLElement) => {
  if (root.querySelector(PDF_EXPORT_SELECTORS.notReady)) {
    throw new PdfExportError('EXPORT_CONTENT_NOT_READY')
  }
}

const resolveExportRoot = (routeConfig: PdfExportRouteConfig) => {
  const root = document.querySelector<HTMLElement>(routeConfig.selector)

  if (!root) {
    throw new PdfExportError('ROOT_NOT_FOUND')
  }

  return root
}

const collectExpandableElements = (root: HTMLElement) => {
  const elements = Array.from(root.querySelectorAll<HTMLElement>(PDF_EXPORT_EXPAND_SELECTOR))
  if (!elements.includes(root)) {
    elements.unshift(root)
  }

  return elements
}

const snapshotStyledElements = (elements: HTMLElement[]): StyledElementSnapshot[] => {
  return elements.map(element => ({
    element,
    cssText: element.style.cssText,
    scrollTop: element.scrollTop,
    scrollLeft: element.scrollLeft
  }))
}

const expandElementForExport = (element: HTMLElement, root: HTMLElement) => {
  Object.assign(element.style, {
    position: element === root ? 'relative' : element.style.position,
    left: element === root ? '0' : element.style.left,
    right: element === root ? 'auto' : element.style.right,
    top: element === root ? '0' : element.style.top,
    bottom: element === root ? 'auto' : element.style.bottom,
    height: 'auto',
    minHeight: '0',
    maxHeight: 'none',
    overflow: 'visible',
    overflowX: 'hidden',
    overflowY: 'visible'
  })
  element.scrollTop = 0
  element.scrollLeft = 0
}

const applyRouteExportRootStyle = (root: HTMLElement, routeConfig: PdfExportRouteConfig) => {
  if (!routeConfig.exportPadding) {
    return
  }

  Object.assign(root.style, {
    boxSizing: 'border-box',
    padding: routeConfig.exportPadding
  })
}

/**
 * 根据导出区域尺寸动态收敛截图倍率，避免长图单页超过 PDF 页面上限后被二次缩放。
 */
const resolveExportDpr = (width: number, height: number) => {
  const maxEdge = Math.max(width, height)

  if (!maxEdge) {
    return SNAPDOM_EXPORT_DPR
  }

  return Math.min(SNAPDOM_EXPORT_DPR, MAX_PDF_PAGE_EDGE / maxEdge)
}

const hideExportExcludedElements = (root: HTMLElement) => {
  const excludedElements = Array.from(
    root.querySelectorAll<HTMLElement>(PDF_EXPORT_SELECTORS.exclude)
  )

  return excludedElements.map(element => {
    const cssText = element.style.cssText
    element.style.display = 'none'

    return () => {
      element.style.cssText = cssText
    }
  })
}

const showExportOnlyElements = (root: HTMLElement) => {
  const onlyElements = Array.from(root.querySelectorAll<HTMLElement>(PDF_EXPORT_SELECTORS.only))

  return onlyElements.map(element => {
    const cssText = element.style.cssText
    const display = element.dataset.pageExportDisplay || 'block'
    element.style.display = display

    return () => {
      element.style.cssText = cssText
    }
  })
}

const restoreStyledElements = (snapshots: StyledElementSnapshot[]) => {
  snapshots.forEach(({ element, cssText, scrollTop, scrollLeft }) => {
    element.style.cssText = cssText
    element.scrollTop = scrollTop
    element.scrollLeft = scrollLeft
  })
}

const snapshotRouteScrollContainer = (
  root: HTMLElement,
  routeConfig: PdfExportRouteConfig
): ScrollElementSnapshot | null => {
  if (!routeConfig.scrollContainerSelector) {
    return null
  }

  const element = root.ownerDocument.querySelector<HTMLElement>(routeConfig.scrollContainerSelector)

  if (!element) {
    return null
  }

  return {
    element,
    scrollTop: element.scrollTop,
    scrollLeft: element.scrollLeft
  }
}

const resetRouteScrollContainer = (snapshot: ScrollElementSnapshot | null) => {
  if (!snapshot) {
    return
  }

  snapshot.element.scrollTop = 0
  snapshot.element.scrollLeft = 0
}

const restoreRouteScrollContainer = (snapshot: ScrollElementSnapshot | null) => {
  if (!snapshot) {
    return
  }

  snapshot.element.scrollTop = snapshot.scrollTop
  snapshot.element.scrollLeft = snapshot.scrollLeft
}

const resolveExportSize = (root: HTMLElement) => {
  const rect = root.getBoundingClientRect()
  const width = Math.ceil(rect.width || root.scrollWidth)
  const height = Math.ceil(Math.max(root.scrollHeight, rect.height))

  if (width <= 0 || height <= 0) {
    throw new PdfExportError('CANVAS_RENDER_FAILED')
  }

  return { width, height }
}

const captureCurrentDomCanvas = async (routeConfig: PdfExportRouteConfig) => {
  const root = resolveExportRoot(routeConfig)
  assertExportReady(root)

  const expandableElements = collectExpandableElements(root)
  const originalRootScrollTop = root.scrollTop
  const originalWindowScrollX = window.scrollX
  const originalWindowScrollY = window.scrollY
  const routeScrollSnapshot = snapshotRouteScrollContainer(root, routeConfig)
  const styledSnapshots = snapshotStyledElements(expandableElements)
  const restoreExcludedElements = hideExportExcludedElements(root)
  const restoreOnlyElements = showExportOnlyElements(root)
  try {
    expandableElements.forEach(element => expandElementForExport(element, root))
    applyRouteExportRootStyle(root, routeConfig)
    resetRouteScrollContainer(routeScrollSnapshot)
    window.scrollTo(0, 0)
    await waitForNextFrame()
    await waitForNextFrame()

    const { width, height } = resolveExportSize(root)
    const exportDpr = resolveExportDpr(width, height)

    return await snapdom
      .toCanvas(root, {
        backgroundColor: routeConfig.background,
        dpr: exportDpr,
        width,
        height,
        embedFonts: true,
        fast: false,
        cache: 'soft'
      })
      .catch((error: unknown) => {
        console.error('PDF导出截图失败:', error)
        throw new PdfExportError('CANVAS_RENDER_FAILED')
      })
  } finally {
    restoreExcludedElements.forEach(restore => restore())
    restoreOnlyElements.forEach(restore => restore())
    restoreStyledElements(styledSnapshots)
    root.scrollTop = originalRootScrollTop
    restoreRouteScrollContainer(routeScrollSnapshot)
    window.scrollTo(originalWindowScrollX, originalWindowScrollY)
  }
}

/**
 * 统一导出当前页面业务区域为 PDF。
 * 当前实现直接使用 snapdom 截取真实 DOM，不再创建 iframe 或复制 DOM。
 */
export const exportPagePdf = async (options: ExportPagePdfOptions) => {
  const routeConfig = getExportRouteConfig(options.routeName)
  const canvas = await captureCurrentDomCanvas(routeConfig)
  const blob = await buildPdfBlob(canvas)

  downloadFromBlob(blob, buildFilename(options))
}
