import {
  LEADER_EXPORT_BACKGROUND,
  LEADER_EXPORT_ROOT_SELECTOR,
  SCENE_EXPORT_BACKGROUND,
  SCENE_EXPORT_ROOT_SELECTOR
} from './constants'
import type {
  FullPagePdfExportRouteName,
  PdfExportRouteConfig,
  PdfExportRouteName,
  PdfExportTrigger,
  ScenePdfExportRouteName
} from './types'
import { FULL_PAGE_PDF_EXPORT_ROUTE_NAMES, SCENE_PDF_EXPORT_ROUTE_NAMES } from './types'

const createSceneRouteConfig = (): PdfExportRouteConfig => ({
  selector: SCENE_EXPORT_ROOT_SELECTOR,
  trigger: 'sceneHeader',
  background: SCENE_EXPORT_BACKGROUND
})

const createFullPageRouteConfig = (): PdfExportRouteConfig => ({
  selector: LEADER_EXPORT_ROOT_SELECTOR,
  trigger: 'layoutHeader',
  background: LEADER_EXPORT_BACKGROUND,
  exportPadding: '24px',
  scrollContainerSelector: '.layout__content'
})

const createExportRouteConfigMap = (): Record<PdfExportRouteName, PdfExportRouteConfig> => ({
  ...(Object.fromEntries(
    FULL_PAGE_PDF_EXPORT_ROUTE_NAMES.map(routeName => [routeName, createFullPageRouteConfig()])
  ) as Record<FullPagePdfExportRouteName, PdfExportRouteConfig>),
  ...(Object.fromEntries(
    SCENE_PDF_EXPORT_ROUTE_NAMES.map(routeName => [routeName, createSceneRouteConfig()])
  ) as Record<ScenePdfExportRouteName, PdfExportRouteConfig>)
})

const EXPORT_ROUTE_CONFIG_MAP = createExportRouteConfigMap()

export const getExportRouteConfig = (routeName: PdfExportRouteName) => {
  return EXPORT_ROUTE_CONFIG_MAP[routeName]
}

const isMatchedTriggerRoute = (routeName: unknown, trigger: PdfExportTrigger) => {
  if (!isPdfExportRouteName(routeName)) {
    return false
  }

  return getExportRouteConfig(routeName).trigger === trigger
}

export const isPdfExportRouteName = (routeName: unknown): routeName is PdfExportRouteName => {
  return typeof routeName === 'string' && routeName in EXPORT_ROUTE_CONFIG_MAP
}

export const shouldShowLayoutPdfExportButton = (routeName: unknown) => {
  return isMatchedTriggerRoute(routeName, 'layoutHeader')
}

export const shouldShowScenePdfExportButton = (routeName: unknown) => {
  return isMatchedTriggerRoute(routeName, 'sceneHeader')
}

export const shouldShowPdfExportButton = (routeName: unknown, trigger: PdfExportTrigger) => {
  return isMatchedTriggerRoute(routeName, trigger)
}
