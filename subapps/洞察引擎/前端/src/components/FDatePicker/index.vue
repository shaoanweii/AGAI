<script setup lang="ts">
import dayjs from 'dayjs'
import quarterOfYear from 'dayjs/plugin/quarterOfYear'
import { computed, ref, watch, nextTick } from 'vue'
import SvgIcon from '@/components/SvgIcon/index.vue'

dayjs.extend(quarterOfYear)

defineOptions({
  name: 'FDatePicker'
})

const props = withDefaults(
  defineProps<{
    size?: any
    // 可选：选择范围最大天数（含首尾），如 7 表示 1-7 天
    maxRangeDays?: number
    // 可选：允许选择的最大日期（含当天）；超过该日期的天会被禁用
    maxSelectableDate?: string | Date
    // 可选：自定义快捷选项；不传时使用内置快捷选项
    shortcuts?: Array<{ text: string; value: () => [Date, Date] }>
  }>(),
  {
    size: 'default',
    maxRangeDays: undefined,
    maxSelectableDate: undefined,
    shortcuts: undefined
  }
)

const times = defineModel<any>()
const shortcutValue = defineModel<string>('shortcutValue')

// 时间选择器
const calendarChangeData = ref<any>([])
// 是否是快捷选项点击
const isShortcutClick = ref(false)

const normalizeMaxSelectableDateOrNull = (raw: unknown) => {
  if (!raw) return null
  const parsed = dayjs(raw as any)
  if (!parsed.isValid()) return null
  return parsed.startOf('day')
}

const maxSelectableDayjs = computed(() => normalizeMaxSelectableDateOrNull(props.maxSelectableDate))

/**
 * 计算快捷选项的时间范围
 * */
const getShortcutDateRange = (type: string): [Date, Date] => {
  const end = maxSelectableDayjs.value ?? dayjs()
  let start = end

  switch (type) {
    case '近7天':
      start = end.subtract(6, 'day')
      break
    case '本月':
      start = end.startOf('month')
      break
    case '本季':
      start = end.startOf('quarter' as any)
      break
    case '本年':
      start = end.startOf('year')
      break
  }

  return [start.toDate(), end.toDate()]
}

const defaultShortcuts = [
  {
    text: '近7天',
    value: () => {
      isShortcutClick.value = true
      shortcutValue.value = '近7天'
      return getShortcutDateRange('近7天')
    }
  },
  {
    text: '本月',
    value: () => {
      isShortcutClick.value = true
      shortcutValue.value = '本月'
      return getShortcutDateRange('本月')
    }
  },
  {
    text: '本季',
    value: () => {
      isShortcutClick.value = true
      shortcutValue.value = '本季'
      return getShortcutDateRange('本季')
    }
  },
  {
    text: '本年',
    value: () => {
      isShortcutClick.value = true
      shortcutValue.value = '本年'
      return getShortcutDateRange('本年')
    }
  }
]

const isRangeExceedMaxDays = (start: any, end: any, maxDays: number) => {
  const startDate = dayjs(start)
  const endDate = dayjs(end)
  if (!startDate.isValid() || !endDate.isValid()) return false
  const diff = Math.abs(endDate.diff(startDate, 'day')) + 1
  return diff > maxDays
}

const resolvedShortcuts = computed(() => {
  // 显式传入 shortcuts（即使为空数组）则完全按调用方配置渲染：
  // - undefined: 使用内置快捷选项
  // - []: 不显示快捷选项
  const source = props.shortcuts === undefined ? defaultShortcuts : props.shortcuts
  const maxDays = props.maxRangeDays
  if (!maxDays || source !== defaultShortcuts) {
    return source
  }
  // 避免调用 shortcut.value() 触发副作用，直接按内置规则计算长度
  return source.filter(item => {
    const [start, end] = getShortcutDateRange(item.text)
    return !isRangeExceedMaxDays(start, end, maxDays)
  })
})

/**
 * 监听点击的日期
 * */
const handleCalendarChange = (val: [Date, null | Date]) => {
  calendarChangeData.value = val
}

/**
 * 监听日期选择完成，手动选择时清除快捷选项标识
 * */
const handleChange = () => {
  setTimeout(() => {
    if (!isShortcutClick.value) {
      shortcutValue.value = ''
    }
    isShortcutClick.value = false
  }, 0)
}

/**
 * 禁用时间：
 * - 选择开始日期后：如传入 maxRangeDays，则仅允许选择开始日期前后 (maxRangeDays - 1) 天
 * - 未传 maxRangeDays：允许选择开始日期前后 1 年
 * */
const disabledDate = (date: Date) => {
  const currentDate = dayjs(date)
  const maxSelectable = maxSelectableDayjs.value
  if (maxSelectable && currentDate.isAfter(maxSelectable, 'day')) return true

  const [start, end] = calendarChangeData.value || []
  if (!start || end) return false
  const maxDays = props.maxRangeDays
  if (maxDays && maxDays > 0) {
    const rangeDays = Math.max(0, maxDays - 1)
    const minByRange = dayjs(start).subtract(rangeDays, 'day')
    const maxByRange = dayjs(start).add(rangeDays, 'day')
    if (currentDate.isBefore(minByRange, 'day') || currentDate.isAfter(maxByRange, 'day'))
      return true
    return false
  }

  const minByYear = dayjs(start).subtract(1, 'year')
  const maxByYear = dayjs(start).add(1, 'year')
  if (currentDate.isBefore(minByYear, 'day') || currentDate.isAfter(maxByYear, 'day')) return true
  return false
}

/**
 * 面板打开时高亮快捷选项
 * */
const handleVisibleChange = (visible: boolean) => {
  if (visible) {
    nextTick(() => {
      updateShortcutStyles()
    })
  }
}

/**
 * 更新快捷选项样式
 * */
const updateShortcutStyles = () => {
  const shortcuts = document.querySelectorAll('.el-picker-panel__shortcut')
  shortcuts.forEach(el => {
    const button = el as HTMLElement
    if (shortcutValue.value && button.textContent?.trim() === shortcutValue.value) {
      button.style.color = '#1677ff'
      button.style.fontWeight = '600'
    } else {
      button.style.color = ''
      button.style.fontWeight = ''
    }
  })
}

// 监听shortcutValue变化，更新快捷选项高亮和times
watch(
  () => shortcutValue.value,
  val => {
    // 总是更新样式
    nextTick(() => {
      updateShortcutStyles()
    })

    if (!val) return

    const [start, end] = getShortcutDateRange(val)
    const maxDays = props.maxRangeDays
    const maxSelectable = maxSelectableDayjs.value
    let startDate = dayjs(start)
    let endDate = dayjs(end)
    if (maxDays && isRangeExceedMaxDays(start, end, maxDays)) {
      endDate = startDate.add(maxDays - 1, 'day')
    }
    if (maxSelectable && endDate.isAfter(maxSelectable, 'day')) {
      endDate = maxSelectable
    }
    if (startDate.isAfter(endDate, 'day')) {
      startDate = endDate
    }
    const newTimes = [startDate.format('YYYY-MM-DD'), endDate.format('YYYY-MM-DD')]
    times.value = newTimes
    calendarChangeData.value = newTimes
  },
  { immediate: true }
)

const isClampingTimes = ref(false)

const clampTimesIfNeeded = (raw: any) => {
  const maxDays = props.maxRangeDays
  if (!Array.isArray(raw) || raw.length !== 2) return
  const [startRaw, endRaw] = raw
  if (!startRaw || !endRaw) return

  const startDate = dayjs(startRaw)
  const endDate = dayjs(endRaw)
  if (!startDate.isValid() || !endDate.isValid()) return
  const maxSelectable = maxSelectableDayjs.value

  const clampedStart =
    maxSelectable && startDate.isAfter(maxSelectable, 'day') ? maxSelectable : startDate
  const clampedEnd =
    maxSelectable && endDate.isAfter(maxSelectable, 'day') ? maxSelectable : endDate
  const [sortedStart, sortedEnd] = clampedStart.isAfter(clampedEnd, 'day')
    ? [clampedEnd, clampedStart]
    : [clampedStart, clampedEnd]

  if (maxDays && maxDays > 0) {
    const diffDays = sortedEnd.diff(sortedStart, 'day') + 1
    if (diffDays > maxDays) {
      const endByDays = sortedStart.add(maxDays - 1, 'day')
      const nextEnd =
        maxSelectable && endByDays.isAfter(maxSelectable, 'day') ? maxSelectable : endByDays
      const normalized = [sortedStart.format('YYYY-MM-DD'), nextEnd.format('YYYY-MM-DD')]
      isClampingTimes.value = true
      times.value = normalized
      calendarChangeData.value = normalized
      isClampingTimes.value = false
      return
    }
  }

  const normalized = [sortedStart.format('YYYY-MM-DD'), sortedEnd.format('YYYY-MM-DD')]
  if (normalized[0] === startRaw && normalized[1] === endRaw) return
  isClampingTimes.value = true
  times.value = normalized
  calendarChangeData.value = normalized
  isClampingTimes.value = false
}

watch(
  () => times.value,
  val => {
    if (isClampingTimes.value) return
    if (!Array.isArray(val) || val.length !== 2 || !val[0] || !val[1]) {
      calendarChangeData.value = []
      shortcutValue.value = ''
      isShortcutClick.value = false
      return
    }
    clampTimesIfNeeded(val)
  }
)
const customPrefix = shallowRef({
  render() {
    return h(SvgIcon, { name: 'calendar', width: '20px', height: '20px', color: '#999999' })
  }
})
</script>

<template>
  <!-- iround-8 -->
  <el-date-picker
    v-model="times"
    type="daterange"
    :shortcuts="resolvedShortcuts"
    start-placeholder="开始时间"
    end-placeholder="结束时间"
    value-format="YYYY-MM-DD"
    :size="props.size"
    class="FDatePicker"
    :prefix-icon="customPrefix"
    :disabled-date="disabledDate"
    @calendar-change="handleCalendarChange"
    @visible-change="handleVisibleChange"
    @change="handleChange"
  />
</template>

<style lang="scss" scoped>
.FDatePicker {
  :deep(.el-picker-panel__shortcut) {
    &:hover {
      color: #1677ff;
    }
    transition: all 0.3s;
  }
}
</style>

<style lang="scss">
.FDatePicker {
  .el-range__icon {
    width: 86px !important;
    font-style: normal !important;
  }
}
</style>
