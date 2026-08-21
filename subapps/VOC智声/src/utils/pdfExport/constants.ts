import type { ExportWatermarkConfig } from './types'

export const PDF_EXPORT_ROOT_VALUES = {
  leader: 'leader',
  scene: 'scene'
} as const

const createPdfExportRootSelector = (rootValue: string) => {
  return `[data-page-export-root="${rootValue}"]`
}

export const PDF_EXPORT_SELECTORS = {
  leaderRoot: createPdfExportRootSelector(PDF_EXPORT_ROOT_VALUES.leader),
  sceneRoot: createPdfExportRootSelector(PDF_EXPORT_ROOT_VALUES.scene),
  section: '[data-page-export-section]',
  exclude: '[data-page-export-exclude]',
  only: '[data-page-export-only]',
  cardBody: '[data-page-export-card-body]',
  expand: '[data-page-export-expand]',
  reportSummary: '[data-page-export-report-summary]',
  reportSummaryContent: '[data-page-export-report-summary-content]',
  fixedReset: '[data-page-export-fixed-reset]',
  notReady: '[data-page-export-ready="false"]'
} as const

export const LEADER_EXPORT_ROOT_SELECTOR = PDF_EXPORT_SELECTORS.leaderRoot
export const SCENE_EXPORT_ROOT_SELECTOR = PDF_EXPORT_SELECTORS.sceneRoot
export const LEADER_EXPORT_BACKGROUND = '#eaf3ff'
export const SCENE_EXPORT_BACKGROUND = '#e9eef8'
export const MAX_PDF_PAGE_EDGE = 14400
export const SNAPDOM_EXPORT_DPR = 2
export const PDF_EXPORT_IMAGE_FORMAT = 'JPEG'
export const PDF_EXPORT_IMAGE_QUALITY = 0.92

export const EXPORT_WATERMARK_CONFIG: ExportWatermarkConfig = {
  color: '#1f2733',
  opacity: 0.08,
  fontSize: 20,
  angle: 20,
  density: 320
}
