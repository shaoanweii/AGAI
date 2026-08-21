import request from './index'
import type { ChannelParams } from '@/types/baseSeting.types'

/**
 * 获取渠道分类树
 * @param data
 */
export const findChannelCategoryTree = (data: { clientId: string }) => {
  return request<any>({
    url: '/insights/channel/findChannelCategoryTree',
    method: 'POST',
    data
  })
}
/**
 * 分页获取渠道分类下的渠道列表
 * @param data
 */
export const findChannelInfoByParentId = (data: { clientId: string; parentId: string }) => {
  return request<any>({
    url: '/insights/channel/findChannelInfoByParentId',
    method: 'POST',
    data
  })
}

/**
 * 新增渠道分类/新增渠道
 * @param data
 */
export const saveChannel = (data: ChannelParams) => {
  return request<any>({
    url: '/insights/channel/saveChannel',
    method: 'POST',
    data
  })
}
/**
 * 更新渠道分类/更新渠道
 * @param data
 */
export const updateChannel = (data: ChannelParams) => {
  return request<any>({
    url: '/insights/channel/updateChannel',
    method: 'POST',
    data
  })
}

/**
 * 更新渠道分类/更新渠道
 * @param data
 */
export const deleteChannel = (data: ChannelParams) => {
  return request<any>({
    url: '/insights/channel/deleteChannel',
    method: 'POST',
    data
  })
}

/**
 * 区域分类列表
 * @param data
 * @returns
 */
export const findRegionCategoryList = (data: {
  clientId: string
  pageNum: number
  pageSize: number
}) => {
  return request<any>({
    url: '/insights/region/findRegionCategoryList',
    method: 'POST',
    data
  })
}

/**
 * 新增区域分类
 * @param data
 * @returns
 */
export const saveRegionCategory = (data: ChannelParams) => {
  return request<any>({
    url: '/insights/region/saveRegionCategory',
    method: 'POST',
    data
  })
}

/**
 * 更新区域分类
 * @param data
 * @returns
 * */
export const updateRegionCategory = (data: ChannelParams) => {
  return request<any>({
    url: '/insights/region/updateRegionCategory',
    method: 'POST',
    data
  })
}

/**
 * 删除区域分类
 * @param data
 * @returns
 * */
export const deleteRegionCategory = (data: ChannelParams) => {
  return request<any>({
    url: '/insights/region/deleteRegionCategory',
    method: 'POST',
    data
  })
}
/**
 * 新增区域
 * @param data
 * @returns
 */
export const saveRegion = (data: ChannelParams) => {
  return request<any>({
    url: '/insights/region/saveRegion',
    method: 'POST',
    data
  })
}

/**
 * 更新区域
 * @param data
 * @returns
 * */
export const updateRegion = (data: ChannelParams) => {
  return request<any>({
    url: '/insights/region/updateRegion',
    method: 'POST',
    data
  })
}
