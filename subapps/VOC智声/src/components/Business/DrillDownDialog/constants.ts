// 公共：DrillDownDialog 的 Tab Key 枚举
// 其他模块请统一引用此枚举，避免硬编码字符串
export enum DrillTabKey {
  TREND = 'trend',
  INDICATOR = 'indicator',
  VIEWPOINT = 'viewpoint',
  SCENARIO = 'scenario',
  CARSERIES = 'carSeries',
  GEOGRAPHIC = 'geographic',
  POPULATION = 'population',
  DATASOURCE = 'dataSource',
  VOICELIST = 'voiceList'
}

export const ONLY_TREND_AND_VOICELIST: DrillTabKey[] = [
  DrillTabKey.TREND,
  DrillTabKey.VOICELIST
]

