import dayjs from 'dayjs'
import weekOfYear from 'dayjs/plugin/weekOfYear'
import type { ConfigType } from 'dayjs'

dayjs.extend(weekOfYear)

const DEFAULT_RANGE_FORMAT = 'YYYY-MM-DD'

export const getPreviousWeekRange = (format: string = DEFAULT_RANGE_FORMAT): [string, string] => {
  const anchor = dayjs().subtract(1, 'week')
  const start = anchor.startOf('week')
  const end = anchor.endOf('week')
  return [start.format(format), end.format(format)]
}

export const formatWeekRangeLabel = (range: [ConfigType, ConfigType] | ConfigType[]): string => {
  if (!Array.isArray(range) || range.length !== 2) return ''
  const [startValue, endValue] = range
  const start = dayjs(startValue)
  const end = dayjs(endValue)
  if (!start.isValid() || !end.isValid()) return ''
  const week = start.week()
  return `第${week}周：${start.format('YYYY.MM.DD')} — ${end.format('YYYY.MM.DD')}`
}

export interface WeekRangeOption {
  key: string
  label: string
  value: [string, string]
  year: number
  index: number
}

export const buildYearWeekRanges = (year?: number): WeekRangeOption[] => {
  const baseYear = typeof year === 'number' ? year : dayjs().year()
  const startOfYear = dayjs(`${baseYear}-01-01`)
  const endOfYear = startOfYear.endOf('year')
  const result: WeekRangeOption[] = []

  let index = 1
  // 以日历周为单位构造时间段，保持与 getPreviousWeekRange 的 week 边界一致
  let currentStart = startOfYear.startOf('week')

  while (currentStart.isBefore(endOfYear) || currentStart.isSame(endOfYear, 'day')) {
    const currentEnd = currentStart.endOf('week')

    const rangeStart = currentStart.isBefore(startOfYear) ? startOfYear : currentStart
    const rangeEnd = currentEnd.isAfter(endOfYear) ? endOfYear : currentEnd

    const startStr = rangeStart.format(DEFAULT_RANGE_FORMAT)
    const endStr = rangeEnd.format(DEFAULT_RANGE_FORMAT)

    const label = `第${index}周(${rangeStart.format('MM-DD')} ~ ${rangeEnd.format('MM-DD')})`
    const key = `${baseYear}-${index}`

    result.push({
      key,
      label,
      value: [startStr, endStr],
      year: baseYear,
      index
    })

    currentStart = currentStart.add(1, 'week')
    index += 1
  }

  return result
}

export const findWeekOptionForRange = (
  range: [ConfigType, ConfigType] | ConfigType[] | undefined,
  options: WeekRangeOption[]
): WeekRangeOption | undefined => {
  if (!Array.isArray(range) || range.length !== 2 || !options.length) {
    return undefined
  }

  const [, endValue] = range
  const anchor = dayjs(endValue)
  if (!anchor.isValid()) {
    return undefined
  }

  const directMatched = options.find(option => {
    const [startStr, endStr] = option.value
    const start = dayjs(startStr)
    const end = dayjs(endStr)
    if (!start.isValid() || !end.isValid()) {
      return false
    }
    if (
      anchor.isSame(start, 'day') ||
      anchor.isSame(end, 'day') ||
      (!anchor.isBefore(start, 'day') && !anchor.isAfter(end, 'day'))
    ) {
      return true
    }
    return false
  })

  if (directMatched) {
    return directMatched
  }

  let best: WeekRangeOption | undefined
  let bestDiff = Number.POSITIVE_INFINITY

  options.forEach(option => {
    const [startStr, endStr] = option.value
    const start = dayjs(startStr)
    const end = dayjs(endStr)
    if (!start.isValid() || !end.isValid()) {
      return
    }
    let diff = 0
    if (anchor.isBefore(start, 'day')) {
      diff = start.diff(anchor, 'day')
    } else if (anchor.isAfter(end, 'day')) {
      diff = anchor.diff(end, 'day')
    }
    if (diff < bestDiff) {
      bestDiff = diff
      best = option
    }
  })

  return best
}
