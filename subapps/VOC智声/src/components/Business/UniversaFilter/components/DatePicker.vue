<script setup lang="ts">
import { FE_TIME_DIMENSION_OPTIONS } from '@/constants'
import { shallowRef, h, watch, ref } from 'vue'
import SvgIcon from '@/components/UI/SvgIcon/index.vue'
import dayjs from 'dayjs'
import quarterOfYear from 'dayjs/plugin/quarterOfYear'

defineOptions({
  name: 'DatePicker'
})

const props = withDefaults(
  defineProps<{
    defaultValue?: string[] | any
    disabled?: boolean
  }>(),
  {
    disabled: false
  }
)

const modelValue = defineModel<string>()
const customTimes = defineModel<string[]>('customTimes', { default: () => [] })

const CUSTOM = 'custom'
const MAX_RANGE_DAYS = 365

dayjs.extend(quarterOfYear)

/**
 * 记录日期面板当前选择中的时间范围。
 * 用于在用户先选中一端日期后，动态限制另一端日期最多只能选择 365 天内的范围。
 */
const calendarChangeData = ref<Array<Date | string | null>>([])

// 更新快捷选项对应的时间范围
const updateShortcutDateRange = (code: string | number) => {
  if (code === CUSTOM) return

  const option = FE_TIME_DIMENSION_OPTIONS.find(item => item.code?.toString() === code?.toString())
  if (option?.calculate) {
    const end = dayjs()
    const start = option.calculate(end)
    customTimes.value = [start.format('YYYY-MM-DD'), end.format('YYYY-MM-DD')]
  }
}

/**
 * 监听日期面板的临时选中值，供禁用逻辑判断跨度使用。
 * @param val 当前面板中正在选择的起止日期
 */
const handleCalendarChange = (val: [Date | string, Date | string | null] | null) => {
  calendarChangeData.value = val || []
}

/**
 * 禁用未来日期，并限制用户手动选择的日期跨度最长为 365 天。
 * @param date 当前待渲染的日期
 * @returns `true` 表示该日期不可选
 */
const disabledDate = (date: Date) => {
  // 与 overview 的时间控件保持一致：今天之后的日期不允许选择。
  if (dayjs(date).isAfter(dayjs(), 'day')) return true

  // 用户在范围面板中选中一端日期后，另一端日期最多只能选择 365 天内的范围。
  const [start] = calendarChangeData.value || []
  if (!start) return false

  const diffDays = Math.abs(dayjs(start).diff(dayjs(date), 'day'))
  return diffDays > MAX_RANGE_DAYS
}

// 监听 modelValue 的变化，确保能响应父组件的更新
watch(
  () => modelValue.value,
  (newVal, oldVal) => {
    // 如果 modelValue 为空且有默认值，设置默认值
    if (!newVal && FE_TIME_DIMENSION_OPTIONS.length > 0) {
      const defaultCode = FE_TIME_DIMENSION_OPTIONS[0].code.toString()
      modelValue.value = defaultCode
      // 初始化时也要更新 customTimes（但只有在 customTimes 为空时才更新，避免覆盖父组件设置的值）
      if (!customTimes.value || customTimes.value.length === 0) {
        updateShortcutDateRange(defaultCode)
      }
      return
    }

    // 如果 modelValue 是 'custom'，不要更新 customTimes（由父组件控制）
    if (newVal === CUSTOM) {
      return
    }

    // 如果 modelValue 变化且不是自定义，同步更新 customTimes
    // 当 oldVal 为 undefined 时（初始化），也需要更新（但只有在 customTimes 为空时才更新）
    if (newVal && newVal !== CUSTOM && (oldVal === undefined || newVal !== oldVal)) {
      // 如果 customTimes 已经有值且 modelValue 是从 undefined 变为新值（初始化），可能是父组件已经设置了值，不要覆盖
      if (oldVal === undefined && customTimes.value && customTimes.value.length === 2) {
        return
      }
      updateShortcutDateRange(newVal)
    }
  },
  { immediate: true }
)

const handleDateSelect = (code: number | string) => {
  if (props.disabled) return
  modelValue.value = code.toString()

  // 如果不是自定义，计算对应的时间范围
  // 注意：watch 也会处理这个更新，但这里直接更新可以确保立即响应
  if (code !== CUSTOM) {
    updateShortcutDateRange(code)
  }
}

const customPrefix = shallowRef({
  render() {
    if (modelValue.value === CUSTOM) {
      return h('div', { style: { display: 'flex', alignItems: 'center', gap: '4px' } }, [
        h(SvgIcon, { name: 'calendar', width: '20px', height: '20px', color: '#fff' }),
        h('span', { style: { color: '#fff', fontWeight: 500 } }, '自定义:')
      ])
    } else {
      return h('div', { style: { display: 'flex', alignItems: 'center', gap: '4px' } }, [
        h(SvgIcon, { name: 'calendar', width: '20px', height: '20px', color: '#999999' }),
        h('span', { style: { color: '#333', fontWeight: 400 } }, '自定义:')
      ])
    }
  }
})

// 不再需要 getTimes 方法，使用 customTimes 双向绑定即可
</script>

<template>
  <div class="switch-btn-wrapper" :class="{ disabled: props.disabled }">
    <template v-for="item in FE_TIME_DIMENSION_OPTIONS" :key="item.code">
      <div
        class="sw-item"
        :class="{ active: modelValue === item.code.toString(), disabled: props.disabled }"
        @click="handleDateSelect(item.code)"
      >
        {{ item.name }}
      </div>
    </template>
    <div
      :class="{ 'custom-date-picker': modelValue === CUSTOM, 'custom-date-picker-default': true }"
      @click="handleDateSelect(CUSTOM)"
    >
      <el-date-picker
        v-model="customTimes"
        type="daterange"
        placeholder="自定义"
        :prefix-icon="customPrefix"
        start-placeholder="开始时间"
        end-placeholder="结束时间"
        format="YYYY.MM.DD"
        value-format="YYYY-MM-DD"
        :clearable="false"
        :disabled="props.disabled"
        :disabled-date="disabledDate"
        style="width: 300px"
        @calendar-change="handleCalendarChange"
      />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.switch-btn-wrapper.disabled {
  pointer-events: none;
  opacity: 0.6;
}
</style>
<style lang="scss">
.custom-date-picker-default {
  .el-range__icon {
    width: 86px !important;
    font-style: normal !important;
  }
}
.custom-date-picker {
  width: 300px;
  .el-input__wrapper,
  .el-range-editor.is-disabled input {
    background: #1677ff;
    font-weight: 500;
    font-size: 14px;
    color: #ffffff;
    line-height: 20px;

    input::placeholder {
      color: #ffffff !important;
    }
  }
  .el-range-input {
    color: #ffffff;
  }
  .el-range__icon {
    color: #ffffff;
    width: 86px !important;
    font-style: normal !important;
  }
  .el-range-separator,
  .el-range-editor.is-disabled .el-range-separator {
    color: #ffffff;
  }
  .el-range__close-icon {
    color: #ffffff;
  }
}
</style>
