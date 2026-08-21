export type PdfExportTrigger = 'layoutHeader' | 'sceneHeader'

export type InlineStyleMap = Partial<CSSStyleDeclaration>

export interface PdfExportRouteConfig {
  selector: string
  trigger: PdfExportTrigger
  background: string
  exportPadding?: string
  scrollContainerSelector?: string
}

const SCENE_PDF_EXPORT_ROUTE_NAMES = [
  'groupAnalysis',
  'thisProductAnalysis',
  'competitorAnalysis',
  'journeyAnalysis',
  'serviceAnalysis',
  'productAnalysis'
  // 'newCarLaunch'
] as const

const FULL_PAGE_PDF_EXPORT_ROUTE_NAMES = ['leaderOverviewPage'] as const

export type ScenePdfExportRouteName = (typeof SCENE_PDF_EXPORT_ROUTE_NAMES)[number]
export type FullPagePdfExportRouteName = (typeof FULL_PAGE_PDF_EXPORT_ROUTE_NAMES)[number]
export type PdfExportRouteName = FullPagePdfExportRouteName | ScenePdfExportRouteName

export interface ExportPagePdfOptions {
  routeName: PdfExportRouteName
  title?: string
  startDate?: string
  endDate?: string
  watermarkName?: string
  watermarkUserName?: string
}

export interface ExportWatermarkConfig {
  color: string
  opacity: number
  fontSize: number
  angle: number
  density: number
}

export { FULL_PAGE_PDF_EXPORT_ROUTE_NAMES, SCENE_PDF_EXPORT_ROUTE_NAMES }
