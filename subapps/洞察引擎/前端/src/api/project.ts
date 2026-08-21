import request from './index'
import type { Conditions } from '@/types'

export interface ProjectList {
  records: Project[]
  total: number
  size: number
  current: number
}

export interface Project {
  projectId: string
  projectTitle: string
  description: string
  corpusCount: string
  corpusPlan: string
  creatTime: string
  addLinkUrl: string
  updateLinkUrl: string
}

export const postProjectList = (data: any) => {
  return request<ProjectList>({
    url: '/api/model/training/getProjects',
    method: 'POST',
    data
  })
}

export const insert = (data: any) => {
  return request({
    url: '/insights/insProjectInfo/insert',
    method: 'POST',
    data
  })
}
export const update = (data: any) => {
  return request({
    url: '/insights/insProjectInfo/update',
    method: 'PATCH',
    data
  })
}
// 查询渠道树
export const getChannelTree = (data: any) => {
  return request({
    url: '/insights/insProjectInfo/getChannelTree',
    method: 'GET',
    params: data
  })
}
// 查询标签分类
export const getTagType = (data: any) => {
  return request({
    url: '/insights/insProjectInfo/getTagType',
    method: 'GET',
    params: data
  })
}
// 查询标签分类
export const findTagLibCategoryTree = (data: any) => {
  return request({
    url: '/insights/insProjectInfo/findTagLibCategoryTree',
    method: 'GET',
    params: data
  })
}

export const findDataSourceInfo = (data: any) => {
  return request({
    url: '/insights/insProjectInfo/findDataSourceInfo',
    method: 'POST',
    data
  })
}
export const findRegionInfo = (data: any) => {
  return request({
    url: '/insights/insProjectInfo/findRegionInfo',
    method: 'POST',
    data
  })
}
export const findBrandCarSeriesInfo = () => {
  return request({
    url: '/insights/insProjectInfo/findBrandCarSeriesInfo',
    method: 'GET'
  })
}

export const saveProjectInfo = (data: any) => {
  return request<any>({
    url: '/insights/insProjectInfo/saveProjectInfo',
    method: 'POST',
    data
  })
}

export const updateProjectInfo = (data: any) => {
  return request<any>({
    url: '/insights/insProjectInfo/updateProjectInfo',
    method: 'POST',
    data
  })
}

export const findProjectInfo = (data: any) => {
  return request<any>({
    url: `/insights/insProjectInfo/findProjectInfo`,
    method: 'post',
    data
  })
}

export const findSearchCriteria = (data: any) => {
  return request({
    method: 'POST',
    url: '/insights/insProjectInfo/findSearchCriteria',
    data
  })
}
export const findLabelSearchCriteria = (data: any) => {
  return request({
    method: 'POST',
    url: '/insights/label/findSearchCriteria',
    data
  })
}
export const findLabelConditions = (data: any) => {
  return request({
    method: 'GET',
    url: '/insights/label/conditions',
    params: data
  })
}
/**
 * 导出标签纠错数据
 * @param data
 */
export const exportLabelCorrectionList = (data: any) => {
  return request({
    method: 'POST',
    url: '/insights/label/labelCorrectionListExportXls',
    data,
    responseType: 'blob'
  })
}
/**
 * 审核标签纠错数据
 * @param data
 */
export const auditLabelCorrection = (data: any) => {
  return request({
    method: 'POST',
    url: '/insights/label/auditLabelCorrection',
    data
  })
}
/**
 * 导出原始数据
 * @param data
 */
export const exportProjectRawDataResult = (data: any) => {
  return request({
    method: 'POST',
    url: '/insights/insProjectInfo/exportProjectRawDataResult ',
    data
    // responseType: 'blob'
  })
}

/**
 * 导出结果数据
 * @param data
 */
export const exportProjectResultData = (data: any) => {
  return request({
    method: 'POST',
    url: '/insights/insProjectInfo/exportProjectResultData ',
    data
    // responseType: 'blob'
  })
}
