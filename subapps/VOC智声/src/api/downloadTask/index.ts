import request from '@/api/http/index'
import { DrillTabKey } from '@/components/Business/DrillDownDialog/constants'
import { CARD_EXPORT_KEYS } from '@/constants/cardExportKeys'
import type { CardDownloadPayload } from '@/hooks/useCardDownload'
import type { DownloadRequest } from '@/hooks/useDownloadAction'

type CardExportKeyGroup = Readonly<Record<string, string>>

export type CardStatExportMap<T extends CardExportKeyGroup> = Readonly<
  Record<T[keyof T], DownloadRequest>
>

/**
 * 创建 POST 下载任务请求函数。
 *
 * @param url 下载任务接口地址
 * @returns 统一的下载任务请求函数
 */
const createDownloadPostRequest = (url: string): DownloadRequest => {
  return data => request({ url, method: 'post', data })
}

/**
 * 由卡片 key 与接口地址配置生成统计导出接口映射。
 *
 * @param entries 卡片 key 和导出接口地址元组
 * @returns 卡片 key 对应的下载任务请求映射
 */
const createDownloadRequestMap = <K extends string>(
  entries: ReadonlyArray<readonly [K, string]>
): Readonly<Record<K, DownloadRequest>> => {
  return Object.fromEntries(
    entries.map(([key, url]) => [key, createDownloadPostRequest(url)])
  ) as Readonly<Record<K, DownloadRequest>>
}

/**
 * 根据卡片稳定 key 获取统计导出接口。
 *
 * @param map 当前页面卡片 key 与导出接口映射
 * @param payload FCard 透传的卡片信息
 * @returns 当前卡片对应的统计导出接口
 */
export const getCardStatExportRequest = (
  map: Partial<Record<string, DownloadRequest>>,
  payload: CardDownloadPayload
): DownloadRequest | undefined => {
  const cardKey = String(payload.cardKey || '').trim()
  return map[cardKey]
}

/**
 * 原声查询结果数据导出。
 * 调用方只负责传递当前查询条件与导出菜单标识。
 */
export const exportVocSounds = createDownloadPostRequest('/report/voc-sounds/exportVocSounds')

/**
 * 原声列表明细导出。
 * 当前仍复用原声结果数据导出接口，便于现有调用方平滑过渡。
 */
export const exportVoiceDetail = exportVocSounds

/**
 * 原声查询原始数据导出。
 * 与结果数据导出分开走独立接口，便于后端区分不同数据源。
 */
export const exportOriginalVoiceData = createDownloadPostRequest(
  '/report/voc-sounds/exportVocOriginalData'
)

/**
 * 原声查询情感分支数据导出。
 * 后端创建导出任务，调用方沿用下载任务提示交互。
 */
export const exportSentimentBranchVoiceData = createDownloadPostRequest(
  '/report/tags/exportRawDataResult'
)

/**
 * 下钻分析统计导出接口映射。
 * key 与 DrillDownDialog 的 tab key 保持一致，便于页面和弹框共用。
 */
export const drillDownStatExportRequestMap: Partial<Record<DrillTabKey, DownloadRequest>> = {
  ...createDownloadRequestMap([
    [DrillTabKey.TREND, '/report/drill-down/exportDrillDownBrief'],
    [DrillTabKey.INDICATOR, '/report/drill-down/exportExperienceAnalysis'],
    [DrillTabKey.VIEWPOINT, '/report/drill-down/exportOpinionList'],
    [DrillTabKey.SCENARIO, '/report/drill-down/exportSceneList'],
    [DrillTabKey.GEOGRAPHIC, '/report/drill-down/exportRegionAnalysis'],
    [DrillTabKey.DATASOURCE, '/report/drill-down/exportDataSourceAnalysis'],
    [DrillTabKey.POPULATION, '/report/drill-down/exportCrowdCharacteristics'],
    [DrillTabKey.CARSERIES, '/report/drill-down/exportCarSeriesAnalysis']
  ] as const)
}

/**
 * 根据下钻 tab 获取统计导出接口函数。
 *
 * @param tabKey 当前下钻 tab key
 * @returns 当前 tab 对应的统计下载接口函数
 */
export const getDrillDownStatExportRequest = (tabKey: string): DownloadRequest | undefined => {
  return drillDownStatExportRequestMap[tabKey as DrillTabKey]
}

/**
 * 集团分析页面卡片统计导出接口映射。
 */
export const groupStatExportMap: CardStatExportMap<typeof CARD_EXPORT_KEYS.group> =
  createDownloadRequestMap([
    [CARD_EXPORT_KEYS.group.Composite, '/report/group-analysis/export-composite-analysis'],
    [CARD_EXPORT_KEYS.group.DataSource, '/report/group-analysis/export-data-source-analysis'],
    [CARD_EXPORT_KEYS.group.Opinion, '/report/group-analysis/export-opinion-evaluation'],
    [CARD_EXPORT_KEYS.group.Product, '/report/group-analysis/export-product-tag-analysis'],
    [CARD_EXPORT_KEYS.group.Service, '/report/group-analysis/export-service-reputation-analysis']
  ] as const)

/**
 * 竞品对比页面卡片统计导出接口映射。
 */
export const competitorStatExportMap: CardStatExportMap<typeof CARD_EXPORT_KEYS.competitor> =
  createDownloadRequestMap([
    [CARD_EXPORT_KEYS.competitor.Composite, '/report/competitor-compare/export-composite-analysis'],
    [CARD_EXPORT_KEYS.competitor.Service, '/report/competitor-compare/export-service-analysis'],
    [CARD_EXPORT_KEYS.competitor.Product, '/report/competitor-compare/export-product-analysis'],
    [
      CARD_EXPORT_KEYS.competitor.DataSource,
      '/report/competitor-compare/export-data-source-analysis'
    ],
    [CARD_EXPORT_KEYS.competitor.Opinion, '/report/competitor-compare/export-opinion-analysis'],
    [CARD_EXPORT_KEYS.competitor.Scene, '/report/competitor-compare/export-scene-analysis']
  ] as const)

/**
 * 本品分析页面卡片统计导出接口映射。
 */
export const productSelfStatExportMap: CardStatExportMap<typeof CARD_EXPORT_KEYS.productSelf> =
  createDownloadRequestMap([
    [
      CARD_EXPORT_KEYS.productSelf.Composite,
      '/report/product-self-analysis/export-composite-analysis'
    ],
    [
      CARD_EXPORT_KEYS.productSelf.DataSource,
      '/report/product-self-analysis/export-data-source-analysis'
    ],
    [CARD_EXPORT_KEYS.productSelf.Product, '/report/product-self-analysis/export-product-analysis'],
    [CARD_EXPORT_KEYS.productSelf.Service, '/report/product-self-analysis/export-service-analysis'],
    [
      CARD_EXPORT_KEYS.productSelf.UserJourney,
      '/report/product-self-analysis/export-user-journey-analysis'
    ]
  ] as const)

/**
 * 旅程分析页面卡片统计导出接口映射。
 */
export const journeyStatExportMap: CardStatExportMap<typeof CARD_EXPORT_KEYS.journey> =
  createDownloadRequestMap([
    [CARD_EXPORT_KEYS.journey.Composite, '/report/journey-analysis/export-composite-analysis'],
    [CARD_EXPORT_KEYS.journey.Crowd, '/report/journey-analysis/export-crowd-characteristics'],
    [CARD_EXPORT_KEYS.journey.DataSource, '/report/journey-analysis/export-data-source-analysis'],
    [CARD_EXPORT_KEYS.journey.Detail, '/report/journey-analysis/export-journey-detail-analysis'],
    [CARD_EXPORT_KEYS.journey.Opinion, '/report/journey-analysis/export-opinion-analysis']
  ] as const)

/**
 * 产品分析页面卡片统计导出接口映射。
 */
export const productStatExportMap: CardStatExportMap<typeof CARD_EXPORT_KEYS.product> =
  createDownloadRequestMap([
    [CARD_EXPORT_KEYS.product.Composite, '/report/product-analysis/export-composite-analysis'],
    [CARD_EXPORT_KEYS.product.DataSource, '/report/product-analysis/export-data-source-analysis'],
    [CARD_EXPORT_KEYS.product.FocusScene, '/report/product-analysis/export-focus-scene-analysis']
  ] as const)

/**
 * 服务分析页面卡片统计导出接口映射。
 */
export const serviceStatExportMap: CardStatExportMap<typeof CARD_EXPORT_KEYS.service> =
  createDownloadRequestMap([
    [CARD_EXPORT_KEYS.service.Composite, '/report/service-analysis/export-composite-analysis'],
    [CARD_EXPORT_KEYS.service.DataSource, '/report/service-analysis/export-data-source-analysis'],
    [CARD_EXPORT_KEYS.service.FocusScene, '/report/service-analysis/export-focus-scene-analysis'],
    [CARD_EXPORT_KEYS.service.Region, '/report/service-analysis/export-region-analysis']
  ] as const)

/**
 * 领导总览页面卡片统计导出接口映射。
 */
export const leaderStatExportMap: CardStatExportMap<typeof CARD_EXPORT_KEYS.leader> =
  createDownloadRequestMap([
    [CARD_EXPORT_KEYS.leader.MarketComparison, '/report/vocLeadership/exportMarketComparison'],
    [CARD_EXPORT_KEYS.leader.BrandInsight, '/report/vocLeadership/exportBrandInsightAnalysis']
  ] as const)
