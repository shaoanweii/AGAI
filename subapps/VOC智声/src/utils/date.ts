/**
 * 时间处理函数
 */

import dayjs from 'dayjs'
import { FE_TIME_DIMENSION_OPTIONS } from '@/constants'

/**
 * 根据日期字符串获取日期范围
 * @param dateStr 日期字符串，格式为 "2024-09" 或 "2024-09-18"
 * @returns 包含 startDate 和 endDate 的对象
 */
export function getDateRange(dateStr: string): { startDate: string; endDate: string } {
  if (!dateStr) return { startDate: '', endDate: '' }

  // 判断是月份还是天
  const isMonth = dateStr.length === 7 // YYYY-MM 格式
  const isDay = dateStr.length === 10 // YYYY-MM-DD 格式

  if (isDay) {
    return {
      startDate: dateStr,
      endDate: dateStr
    }
  } else if (isMonth) {
    const monthStart = dayjs(dateStr).startOf('month').format('YYYY-MM-DD')
    const monthEnd = dayjs(dateStr).endOf('month').format('YYYY-MM-DD')
    return {
      startDate: monthStart,
      endDate: monthEnd
    }
  }

  return { startDate: '', endDate: '' }
}

/**
 * 判断日期字符串是天维度还是月维度
 * @param dateStr 日期字符串
 * @returns 'day' | 'month' | 'unknown'
 */
export function getDateDimension(dateStr: string): 'day' | 'month' | 'unknown' {
  if (!dateStr) return 'unknown'

  if (dateStr.length === 10 && /^\d{4}-\d{2}-\d{2}$/.test(dateStr)) {
    return 'day'
  }

  if (dateStr.length === 7 && /^\d{4}-\d{2}$/.test(dateStr)) {
    return 'month'
  }

  return 'unknown'
}

/**
 * 判断日期字符串是否为天维度
 * @param dateStr 日期字符串
 * @returns boolean
 */
export function isDayDimension(dateStr: string): boolean {
  return dateStr?.length === 10 && /^\d{4}-\d{2}-\d{2}$/.test(dateStr)
}

/**
 * 判断日期字符串是否为月维度
 * @param dateStr 日期字符串
 * @returns boolean
 */
export function isMonthDimension(dateStr: string): boolean {
  return dateStr?.length === 7 && /^\d{4}-\d{2}$/.test(dateStr)
}

/**
 * 根据 code 或 name 获取时间维度选项
 * @param type 时间维度代码或名称
 * @returns 时间维度选项
 */
export const getTimeDimensionByCode = (type: string | number) => {
  return FE_TIME_DIMENSION_OPTIONS.find(
    item => item.code?.toString() === type?.toString() || item.name === type
  )
}

/**
 * 计算快捷选项的时间范围
 * @param type 时间类型，支持 name（如 '近7天'）或 code（如 2）
 * @returns 时间范围 [startDate, endDate]
 */
export const getShortcutDateRange = (type: string | number): [string, string] => {
  const end = dayjs()

  const option = FE_TIME_DIMENSION_OPTIONS.find(
    item => item.code?.toString() === type?.toString() || item.name === type
  )

  const start = option?.calculate(end) || end
  return [start.format('YYYY-MM-DD'), end.format('YYYY-MM-DD')]
}

// 计算两个日期例如开始时间2026-01-01和结束时间2026-01-31相差多少天
export const calcDiffType = (startDate: string, endDate: string, type: any = 'day') => {
  if (!startDate || !endDate) return null
  const start = dayjs(startDate)
  const end = dayjs(endDate)
  const diff = end.diff(start, type) // 结果为30
  return diff
}
