import dayjs from 'dayjs'

export const DEFAULT_DATE_FORMAT = 'YYYY-MM-DD'

export type DateRangeValue = [string, string] | null

export const getYesterdayDateRange = (format: string = DEFAULT_DATE_FORMAT): [string, string] => {
  const date = dayjs().subtract(1, 'day').format(format)
  return [date, date]
}

export const normalizeDateRangeOrNull = (
  raw: unknown,
  options?: { maxRangeDays?: number; format?: string }
): [string, string] | null => {
  if (!Array.isArray(raw) || raw.length !== 2) return null
  const [startRaw, endRaw] = raw
  if (!startRaw || !endRaw) return null

  const start = dayjs(startRaw)
  const end = dayjs(endRaw)
  if (!start.isValid() || !end.isValid()) return null

  const [sortedStart, sortedEnd] = start.isAfter(end, 'day') ? [end, start] : [start, end]
  const diffDays = sortedEnd.diff(sortedStart, 'day') + 1
  if (diffDays < 1) return null

  const maxDays = options?.maxRangeDays
  const format = options?.format || DEFAULT_DATE_FORMAT

  if (maxDays && maxDays > 0 && diffDays > maxDays) {
    const clampedEnd = sortedStart.add(maxDays - 1, 'day')
    return [sortedStart.format(format), clampedEnd.format(format)]
  }

  return [sortedStart.format(format), sortedEnd.format(format)]
}
