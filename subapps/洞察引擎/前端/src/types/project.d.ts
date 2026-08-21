interface ProjectInfo {
  projectName: string
  projectDesc: string
  status: string
  clientId: string
  brand: Brand[]
}

interface Brand {
  brandCode: string
  brandName: string
  channel: string[]
  tags: string[]
  dataSource: string[]
  region: Region[]
  carSeries: CarSeries[]
  competitiveProduct: CompetitiveProduct[]
  riskEarlyWarning: RiskEarlyWarning[]
}

export interface CompetitiveProduct {
  competitiveBrandCode: string
  competitiveBrandName: string
  competitiveCarSeriesCode?: string
  competitiveCarSeriesName?: string
  core: string
}

export interface CarInfo {
  brandCode: string
  brandName: string
  carSeriesCode?: string
  carSeriesName?: string
  competitiveProduct: CompetitiveProduct[]
}

interface Car {
  name: string
  code: string
  id: string
}

interface CarBrand {
  name: string
  code: string
  id: string
  cars: Car[]
}

export interface ProjectDetail {
  projectName: string
  projectDesc: string
  createTime: string
  status: string
  id: string
  region: string[]
  brand: CarInfo[]
  channel: string[]
  dataSource: string[]
  tags: string[]
}

interface Tag {
  id: string
  tagParentId: string
  tagName: string
  tagCode: string
  child?: Tag[] | null // child 可以是 Tag 数组或 null
}

interface TagData {
  id: string
  tagParentId: string
  tagName: string
  tagCode: string
  child: Tag[] // child 必须是 Tag 数组
}

interface Region {
  provinceCode: string
  provinceName: string
  areas: Area[]
}

interface Area {
  areaCode: string
  areaName: string
}

interface CarSeries {
  brandCode?: any
  brandName?: any
  core?: string
  haltSales?: string
  carSeriesCode: string
  carSeriesName: string
  competitiveCarSeries: CompetitiveCarSeries[]
}
interface CompetitiveCarSeries {
  competitiveBrandCode: string
  competitiveBrandName: string
  competitiveCarSeriesCode?: string
  competitiveCarSeriesName?: string
  core: string
}

interface RiskEarlyWarning {
  warningType: string
  riskSetting: RiskSetting[]
  riskLevel: RiskLevel[]
}

interface RiskSetting {
  periodType: string
  negative: number
  complaint: number
  riskWords: number
  channelNum: number
  affective: number
  isApply: boolean
}

interface RiskLevel {
  level: string
  startValue: number
  endValue: number
  color: string
  isApply: boolean
}
