<script setup lang="ts">
import dayjs from 'dayjs'
import quarterOfYear from 'dayjs/plugin/quarterOfYear'
import { ref, watch, nextTick, shallowRef, h, computed } from 'vue'
import SvgIcon from '@/components/UI/SvgIcon/index.vue'
import { getShortcutDateRange } from '@/utils/date'
import { FE_TIME_DIMENSION_OPTIONS } from '@/constants'

dayjs.extend(quarterOfYear)

defineOptions({
  name: 'FDatePicker'
})

const { size = 'large', teleported = true } = defineProps<{
  size?: any
  teleported?: boolean
}>()

const times = defineModel<any>()
const shortcutValue = defineModel<string>('shortcutValue')

// 时间选择器
const calendarChangeData = ref<any>([])
// 是否是快捷选项点击
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
 * 监听点击的日期
 * */
const handleCalendarChange = (val: [Date, null | Date]) => {
  calendarChangeData.value = val
}

/**
 * 监听日期选择完成，手动选择时快捷选项标识为自定义
 * */
const handleChange = () => {
  setTimeout(() => {
    if (!isShortcutClick.value) {
      shortcutValue.value = '自定义'
    }
    isShortcutClick.value = false
  }, 0)
}

/**
 * 禁用时间 灵活选择开始和结束日期，最长不超过1年，且不能选择今天之后的日期
 * */
const disabledDate = (date: Date) => {
  // 禁用今天之后的日期
  if (dayjs(date).isAfter(dayjs(), 'day')) return true

  const [start, end] = calendarChangeData.value || []
  if (!start) return false
  const diffDays = Math.abs(dayjs(start).diff(dayjs(date), 'day'))
  return diffDays > 365
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
    // console.log('val----shortcutValue', val)

    // 总是更新样式
    nextTick(() => {
      updateShortcutStyles()
    })

    if (!val || val === '自定义') return

    const [start, end] = getShortcutDateRange(val)
    const newTimes = [dayjs(start).format('YYYY-MM-DD'), dayjs(end).format('YYYY-MM-DD')]
    times.value = newTimes
    calendarChangeData.value = newTimes
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
defineExpose({
  getShortcutDateRange
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
    class="iround-8 FDatePicker"
    :disabled-date="disabledDate"
    :teleported="teleported"
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
    width: 96px !important;
    font-style: normal !important;
  }
}
</style>
