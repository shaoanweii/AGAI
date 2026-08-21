/**
 * H5 分享状态管理 Store
 * 用于统一管理各页面的分享标题和描述
 */

import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { DateOption } from '@h5/views/home/components/HDateFilter/types'

/**
 * 分享参数接口
 */
export interface ShareParams {
  /** 品牌编码 */
  brandCode?: string
  /** 时间维度类型（周/月/季/年） */
  dateUnit?: number
  /** 时间选项 */
  dateTime?: {
    code?: number
    startTime?: string
    endTime?: string
    name?: string
  }
  /** 数据源（仅首页需要：公域/私域/全域） */
  channelCatagory?: string
}

export const useShareStore = defineStore('h5-share', () => {
  // 分享标题
  const shareTitle = ref<string>('客户之声')

  // 分享描述
  const shareDesc = ref<string>('客户之声描述')

  // 分享参数
  const shareParams = ref<ShareParams | null>(null)

  /**
   * @description: 设置分享信息
   * @param {string} title - 分享标题
   * @param {string} desc - 分享描述
   * @return {*}
   */
  const setShareInfo = (title: string, desc: string) => {
    shareTitle.value = title || '客户之声'
    shareDesc.value = desc || '客户之声描述'
  }

  /**
   * @description: 设置分享参数
   * @param {ShareParams} params - 分享参数
   * @return {*}
   */
  const setShareParams = (params: ShareParams | null) => {
    shareParams.value = params
  }

  /**
   * @description: 获取分享参数
   * @return {ShareParams | null}
   */
  const getShareParams = (): ShareParams | null => {
    return shareParams.value
  }

  /**
   * @description: 清除分享参数
   * @return {*}
   */
  const clearShareParams = () => {
    shareParams.value = null
  }

  return {
    shareTitle,
    shareDesc,
    shareParams,
    setShareInfo,
    setShareParams,
    getShareParams,
    clearShareParams
  }
})
