<script setup lang="ts">
import dayjs from 'dayjs'
import quarterOfYear from 'dayjs/plugin/quarterOfYear'
import { computed, h, nextTick, ref, shallowRef, watch } from 'vue'
import SvgIcon from '@/components/UI/SvgIcon/index.vue'
import { FE_TIME_DIMENSION_OPTIONS } from '@/constants'
import { getShortcutDateRange } from '@/utils/date'

dayjs.extend(quarterOfYear)

defineOptions({
  name: 'PushTaskDatePicker'
})

const {
  size = 'default',
  teleported = true,
  maxRangeDays = 365,
  maxFutureDays = 365
} = defineProps<{
  size?: any
  teleported?: boolean
  /** 日期范围最大跨度天数。 */
  maxRangeDays?: number
  /** 允许选择的最大未来天数。 */
  maxFutureDays?: number
}>()

const times = defineModel<[string, string]>()
const shortcutValue = defineModel<string>('shortcutValue')

const emit = defineEmits<{
  (e: 'change', value: [string, string] | null): void
}>()

const calendarChangeData = ref<Array<Date | string | null>>([])
const isShortcutClick = ref(false)

const shortcuts = computed(() =>
  FE_TIME_DIMENSION_OPTIONS.map(option => ({
    text: option.name,
    value: () => {
      isShortcutClick.value = true
      shortcutValue.value = option.name
      return getShortcutDateRange(option.name)
    }
  }))
)

/**
 * 记录范围面板中已选择的一端日期，用于动态限制另一端日期跨度。
 * @param value 当前面板选择值
 */
const handleCalendarChange = (value: [Date, null | Date]) => {
  calendarChangeData.value = value
}

/**
 * 手动选择完成后标记为自定义时间。
 * @param value 当前日期范围
 */
const handleChange = (value: [string, string] | null) => {
  setTimeout(() => {
    if (!isShortcutClick.value) {
      shortcutValue.value = '自定义'
    }
    isShortcutClick.value = false
  }, 0)
  emit('change', value)
}

/**
 * 判断天数配置是否有效。
 * @param value 天数配置
 * @returns 是否为可用的非负数字
 */
const isValidDayLimit = (value: number | undefined): value is number => {
  return Number.isFinite(value) && Number(value) >= 0
}

/**
 * 限制推送任务筛选日期：
 * - 可选择今天之后一年内的日期；
 * - 手动选择起止日期时，范围最大跨度为 365 天。
 * @param date 当前待判断日期
 * @returns 是否禁用
 */
const disabledDate = (date: Date) => {
  if (isValidDayLimit(maxFutureDays)) {
    const maxFutureDate = dayjs().add(maxFutureDays, 'day')
    if (dayjs(date).isAfter(maxFutureDate, 'day')) return true
  }

  const [start] = calendarChangeData.value || []
  if (!start) return false

  const rangeLimitDays = isValidDayLimit(maxRangeDays) ? maxRangeDays : 365
  const diffDays = Math.abs(dayjs(start).diff(dayjs(date), 'day'))
  return diffDays > rangeLimitDays
}

/**
 * 面板打开时同步快捷项高亮状态。
 * @param visible 面板是否可见
 */
const handleVisibleChange = (visible: boolean) => {
  if (visible) {
    nextTick(() => {
      updateShortcutStyles()
    })
  }
}

/**
 * 根据当前快捷项更新面板左侧快捷按钮样式。
 */
const updateShortcutStyles = () => {
  const shortcutButtons = document.querySelectorAll('.el-picker-panel__shortcut')
  shortcutButtons.forEach(el => {
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

watch(
  () => shortcutValue.value,
  value => {
    nextTick(() => {
      updateShortcutStyles()
    })

    if (!value || value === '自定义') return

    const [start, end] = getShortcutDateRange(value)
    times.value = [dayjs(start).format('YYYY-MM-DD'), dayjs(end).format('YYYY-MM-DD')]
    calendarChangeData.value = times.value
  },
  { immediate: true }
)

const customPrefix = shallowRef({
  render() {
    return h('div', { style: { display: 'flex', alignItems: 'center', gap: '4px' } }, [
      h(SvgIcon, { name: 'calendar', width: '20px', height: '20px', color: '#999999' }),
      h(
        'span',
        { style: { color: '#333', fontWeight: 500 } },
        shortcutValue.value ? `${shortcutValue.value}:` : '自定义:'
      )
    ])
  }
})
</script>

<template>
  <el-date-picker
    v-model="times"
    type="daterange"
    :shortcuts="shortcuts"
    start-placeholder="开始时间"
    end-placeholder="结束时间"
    format="YYYY.MM.DD"
    value-format="YYYY-MM-DD"
    range-separator="–"
    :clearable="false"
    :prefix-icon="customPrefix"
    :size="size"
    class="iround-8 push-task-date-picker"
    :disabled-date="disabledDate"
    :teleported="teleported"
    @calendar-change="handleCalendarChange"
    @visible-change="handleVisibleChange"
    @change="handleChange"
  />
</template>

<style lang="scss" scoped>
.push-task-date-picker {
  :deep(.el-picker-panel__shortcut) {
    transition: all 0.3s;

    &:hover {
      color: #1677ff;
    }
  }
}
</style>

<style lang="scss">
.push-task-date-picker {
  .el-range__icon {
    width: 96px !important;
    font-style: normal !important;
  }
}
</style>
