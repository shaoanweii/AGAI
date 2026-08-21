import request from '@/api/http'

type LabelCorrectionTimeRange = {
  startTime?: string
  endTime?: string
}

export type QuerySoundsInfoParams = LabelCorrectionTimeRange & {
  dataIdList: string[]
  pageNum?: number
  pageSize?: number
  sentiment?: string[]
  intention?: string[]
  brandCode?: string[]
  carSeries?: string[]
  firstCodeTag?: string[]
  secondCodeTag?: string[]
  threeCodeTag?: string[]
  fourCodeTag?: string[]
  topicList?: string[]
}

export const querySoundsInfo = (data: QuerySoundsInfoParams): Promise<BaseResponse<any>> => {
  return request({
    method: 'post',
    url: '/report/labelCorrection/querySoundsInfo',
    data
  })
}

export type InsertLabelCorrectionParams = LabelCorrectionTimeRange & {
  newId: Array<string | number>
  errorType: '1' | '2'
  brandCode?: string
  carSeriesCode?: string
  sentiment?: string
  intention?: string
  topicCode?: string
  brandName?: string
  carSeriesName?: string
  topicName?: string
  usageScenarioFirst?: string
  usageScenarioSecond?: string
}

export const insertLabelCorrection = (
  data: InsertLabelCorrectionParams
): Promise<BaseResponse<any>> => {
  return request({
    method: 'post',
    url: '/report/labelCorrection/insertLabelCorrection',
    data
  })
}

export const querySearchTag = (data: Record<string, any> = {}): Promise<BaseResponse<any>> => {
  return request({
    method: 'post',
    url: '/report/labelCorrection/querySearchTag',
    data
  })
}

export type QueryBrandListParams = LabelCorrectionTimeRange & {
  dataIdList: string[]
}

export const queryBrandList = (data: QueryBrandListParams): Promise<BaseResponse<any>> => {
  return request({
    method: 'post',
    url: '/report/labelCorrection/queryBrandList',
    data
  })
}

export const queryAllBrandList = (): Promise<BaseResponse<any>> => {
  return request({
    method: 'post',
    url: '/report/labelCorrection/queryAllBrandList',
    data: {}
  })
}

export type QueryAllCarSeriesListParams = {
  brandCode: string
  status?: string
}

export const queryAllCarSeriesList = (
  data: QueryAllCarSeriesListParams
): Promise<BaseResponse<any>> => {
  return request({
    method: 'post',
    url: '/report/labelCorrection/queryAllCarSeriesList',
    data
  })
}

export type FindAllFinalTagLibClientVoListParams = LabelCorrectionTimeRange & {
  dataIdList?: string[]
}

export const findAllFinalTagLibClientVoList = (
  data: FindAllFinalTagLibClientVoListParams = {}
): Promise<BaseResponse<any>> => {
  return request({
    method: 'post',
    url: '/report/labelCorrection/findAllFinalTagLibClientVoList',
    data
  })
}

export const findTopicList = (data: Record<string, any> = {}): Promise<BaseResponse<any>> => {
  return request({
    method: 'post',
    url: '/report/labelCorrection/findTopicList',
    data
  })
}

export type QueryCarSeriesListParams = LabelCorrectionTimeRange & {
  dataIdList: string[]
  brandCode: string[]
}

export const queryCarSeriesList = (data: QueryCarSeriesListParams): Promise<BaseResponse<any>> => {
  return request({
    method: 'post',
    url: '/report/labelCorrection/queryCarSeriesList',
    data
  })
}
