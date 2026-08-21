/**
 * 分享参数转换工具函数
 * 用于解析URL参数、转换时间参数等
 */

import type { DateOption } from '@h5/views/home/components/HDateFilter/types'
import type { ShareParams } from '@h5/store/share'

/**
 * 从路由 query 中解析分享参数
 * @param query 路由查询参数
 * @returns 分享参数对象
 */
export function parseShareParamsFromQuery(query: Record<string, any>): ShareParams | null {
  const params: ShareParams = {}

  // 品牌编码
  if (query.brandCode && typeof query.brandCode === 'string') {
    params.brandCode = query.brandCode
  }

  // 时间维度类型
  if (query.dateUnit !== undefined) {
    const dateUnit = Number(query.dateUnit)
    if (!isNaN(dateUnit)) {
      params.dateUnit = dateUnit
    }
  }

  // 时间选项
  if (query.dateTimeCode !== undefined || query.startTime || query.endTime) {
    params.dateTime = {}
    if (query.dateTimeCode !== undefined) {
      const code = Number(query.dateTimeCode)
      if (!isNaN(code)) {
        params.dateTime.code = code
      }
    }
    if (query.startTime && typeof query.startTime === 'string') {
      params.dateTime.startTime = query.startTime
    }
    if (query.endTime && typeof query.endTime === 'string') {
      params.dateTime.endTime = query.endTime
    }
    if (query.dateTimeName && typeof query.dateTimeName === 'string') {
      params.dateTime.name = query.dateTimeName
    }
  }

  // 数据源（仅首页需要）
  if (query.channelCatagory !== undefined && typeof query.channelCatagory === 'string') {
    params.channelCatagory = query.channelCatagory
  }

  // 如果没有任何参数，返回 null
  if (!params.brandCode && params.dateUnit === undefined && !params.dateTime && params.channelCatagory === undefined) {
    return null
  }

  return params
}

/**
 * 将分享参数中的时间参数转换为 DateOption
 * 需要从 timeDimension 中查找对应的选项
 * @param shareParams 分享参数
 * @param timeDimension 时间维度列表
 * @returns DateOption 或 null
 */
export function convertDateParamsToDateOption(
  shareParams: ShareParams | null,
  timeDimension: DateOption[]
): DateOption | null {
  if (!shareParams || !shareParams.dateUnit || !shareParams.dateTime) {
    return null
  }

  // 自定义时间（code=999）特殊处理
  const customCode = 999
  if (shareParams.dateUnit === customCode || shareParams.dateTime.code === customCode) {
    // 自定义时间：直接构造 DateOption
    if (shareParams.dateTime.startTime && shareParams.dateTime.endTime) {
      return {
        name: shareParams.dateTime.name || '自定义',
        code: customCode,
        startTime: shareParams.dateTime.startTime,
        endTime: shareParams.dateTime.endTime
      }
    }
    return null
  }

  // 查找对应的时间维度类型
  const dateUnitTab = timeDimension.find(tab => tab.code === shareParams.dateUnit)
  if (!dateUnitTab) {
    return null
  }

  // 如果有子级，从子级中查找
  if (dateUnitTab.child && dateUnitTab.child.length > 0) {
    const childOption = dateUnitTab.child.find(
      child => child.code === shareParams.dateTime?.code
    )
    if (childOption) {
      // 如果提供了 startTime 和 endTime，更新它们（用于自定义子级选项的情况）
      if (shareParams.dateTime.startTime && shareParams.dateTime.endTime) {
        return {
          ...childOption,
          startTime: shareParams.dateTime.startTime,
          endTime: shareParams.dateTime.endTime
        }
      }
      return childOption
    }
    // 如果找不到对应的子级，但提供了 startTime 和 endTime，构造一个自定义选项
    if (shareParams.dateTime.startTime && shareParams.dateTime.endTime) {
      return {
        name: shareParams.dateTime.name || '自定义',
        code: shareParams.dateTime.code || customCode,
        startTime: shareParams.dateTime.startTime,
        endTime: shareParams.dateTime.endTime
      }
    }
  } else {
    // 没有子级，直接使用该 tab
    // 如果提供了 startTime 和 endTime，更新它们
    if (shareParams.dateTime.startTime && shareParams.dateTime.endTime) {
      return {
        ...dateUnitTab,
        startTime: shareParams.dateTime.startTime,
        endTime: shareParams.dateTime.endTime
      }
    }
    return dateUnitTab
  }

  return null
}

/**
 * 构建分享查询字符串
 * @param shareParams 分享参数
 * @returns 查询字符串（不包含 ?）
 */
export function buildShareQueryString(shareParams: ShareParams | null): string {
  if (!shareParams) {
    return ''
  }

  const params = new URLSearchParams()

  if (shareParams.brandCode) {
    params.set('brandCode', shareParams.brandCode)
  }
  if (shareParams.dateUnit !== undefined) {
    params.set('dateUnit', String(shareParams.dateUnit))
  }
  if (shareParams.dateTime) {
    if (shareParams.dateTime.code !== undefined) {
      params.set('dateTimeCode', String(shareParams.dateTime.code))
    }
    if (shareParams.dateTime.startTime) {
      params.set('startTime', shareParams.dateTime.startTime)
    }
    if (shareParams.dateTime.endTime) {
      params.set('endTime', shareParams.dateTime.endTime)
    }
    if (shareParams.dateTime.name) {
      params.set('dateTimeName', shareParams.dateTime.name)
    }
  }
  if (shareParams.channelCatagory !== undefined) {
    params.set('channelCatagory', shareParams.channelCatagory)
  }

  return params.toString()
}
