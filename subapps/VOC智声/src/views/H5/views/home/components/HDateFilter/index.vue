<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { HDateFilterProps, HDateFilterEmits, DateOption, QuickRangeConfig } from './types'
import dayjs from 'dayjs'
import { usePermissionsStore } from '@h5/store'

// 组件名称（便于调试）
defineOptions({ name: 'HDateFilter' })

// 组件 Props
const props = withDefaults(defineProps<HDateFilterProps>(), {
  tabs: () => [],
  defaultType: 0,
  excludeCodes: () => [4, 2025],
  defaultValue: () => ({}) as DateOption,
  quickRanges: () => [] as QuickRangeConfig[]
})

// 组件 Emits
const emit = defineEmits<HDateFilterEmits>()

// 权限 Store（时间维度）
const permissionsStore = usePermissionsStore()

// 内部响应式状态
const currentType = ref<number>(props.defaultType)
const currentValue = ref<DateOption>(props.defaultValue)
// 子级选项区域显示（仅对有子级的标签）
const isContentVisible = ref<boolean>(false)
// 自定义日期弹框显示
const showCalendar = ref<boolean>(false)
// 自定义日期默认区间（用于回显）
// 约定：默认使用“本月初至今天”作为初始区间
const getThisMonthToTodayRange = (): [Date, Date] => {
  return [dayjs().startOf('month').toDate(), dayjs().toDate()]
}
const customDefaultRange = ref<[Date, Date] | null>(getThisMonthToTodayRange())
// 自定义标签 code
const customCode = 999
// 记录最近一次快捷选择（用于区分“本年/本季度”等相同区间）
const lastQuickKey = ref<string | null>(null)

// 默认快捷日期配置（自定义弹框底部）
const defaultQuickRanges: QuickRangeConfig[] = [
  { key: 'last30', label: '近30天', mode: 'lastDays', value: 30 },
  { key: 'thisQuarter', label: '本季度', mode: 'thisQuarter' },
  { key: 'thisYear', label: '本年', mode: 'thisYear' }
]

// 实际生效的快捷配置：优先使用外部传入，其次使用默认配置
const quickRanges = computed<QuickRangeConfig[]>(() => {
  const list =
    props.quickRanges && props.quickRanges.length > 0 ? props.quickRanges : defaultQuickRanges
  return list
})

const maxDate = ref<Date>(dayjs().endOf('day').toDate())
const minDate = ref<Date>(dayjs().subtract(3, 'year').startOf('day').toDate())
const maxRange = 366 // 含首尾天，覆盖闰年

const isSameOption = (a: DateOption | null | undefined, b: DateOption | null | undefined) => {
  if (!a || !b) return false
  return a.code === b.code && a.startTime === b.startTime && a.endTime === b.endTime
}

/**
 * 初始化当前选中日期：
 * 1. 优先使用父组件传入的默认时间（defaultUnitInfo）
 * 2. 其次使用外部传入的 defaultType / defaultValue
 * 3. 最后回退到 tabs 第一项（有子级则取第一个子级）
 * 初始化完成后会触发一次 change/typeChange，方便父组件拉取首屏数据
 */
const initSelection = () => {
  const tabs = baseTabs.value || []
  const displayTabsList = displayTabs.value || []
  if (!tabs.length) return

  let targetType: number | undefined
  let targetOption: DateOption | undefined

  // 1. 父组件传入的默认时间
  const dateUnitInfo = props.defaultUnitInfo
  if (dateUnitInfo?.isDef && typeof dateUnitInfo.dateUnit === 'number') {
    // 自定义时间（code=999）特殊处理
    if (dateUnitInfo.dateUnit === customCode) {
      // 自定义时间：直接使用传入的 dateTime
      if (
        dateUnitInfo.dateTime &&
        dateUnitInfo.dateTime.startTime &&
        dateUnitInfo.dateTime.endTime
      ) {
        targetType = customCode
        targetOption = dateUnitInfo.dateTime
      }
    } else {
      // 普通时间维度从 baseTabs 中查找
      const tab = tabs.find(t => t.code === dateUnitInfo.dateUnit)
      if (tab) {
        targetType = tab.code
        if (Array.isArray(tab.child) && tab.child.length) {
          const defChildCode = dateUnitInfo.dateTime?.code
          if (typeof defChildCode === 'number') {
            const child = tab.child.find(c => c.code === defChildCode)
            targetOption = child || tab.child[0]
          } else {
            targetOption = tab.child[0]
          }
        } else {
          // 没有子级，直接使用该 tab
          // 如果提供了 startTime 和 endTime，更新它们（用于自定义子级选项的情况）
          if (dateUnitInfo.dateTime?.startTime && dateUnitInfo.dateTime?.endTime) {
            targetOption = {
              ...tab,
              startTime: dateUnitInfo.dateTime.startTime,
              endTime: dateUnitInfo.dateTime.endTime
            }
          } else {
            targetOption = tab
          }
        }
      }
    }
  }

  // 2. 外部传入的默认类型 / 默认值
  if (!targetOption && typeof props.defaultType === 'number') {
    const tab = tabs.find(t => t.code === props.defaultType)
    if (tab) {
      targetType = tab.code
      if (props.defaultValue && typeof props.defaultValue.code === 'number') {
        if (Array.isArray(tab.child) && tab.child.length) {
          const child = tab.child.find(c => c.code === props.defaultValue!.code)
          targetOption = child || tab.child[0]
        } else {
          targetOption = tab
        }
      } else {
        targetOption = (tab.child && tab.child[0]) || tab
      }
    }
  }

  // 3. 兜底：tabs 第一项
  if (!targetOption) {
    const tab = tabs[0]
    targetType = tab.code
    targetOption = (tab.child && tab.child[0]) || tab
  }

  if (!targetOption || typeof targetType !== 'number') return

  // 初始化阶段可能被多路 watch 触发：同值不重复触发 change，避免父组件首屏请求重复
  if (currentType.value === targetType && isSameOption(currentValue.value, targetOption)) return

  currentType.value = targetType
  currentValue.value = targetOption

  // 如果是自定义时间，需要设置自定义日期的默认区间
  if (targetType === customCode && targetOption.startTime && targetOption.endTime) {
    const start = dayjs(targetOption.startTime)
    const end = dayjs(targetOption.endTime)
    if (start.isValid() && end.isValid()) {
      const [s, e] = clampRange([start.toDate(), end.toDate()])
      customDefaultRange.value = [s, e]
    }
  }

  emit('change', targetType, targetOption)
  emit('typeChange', targetType)
}

// 监听 props 变化，同步内部状态
watch(
  () => props.defaultType,
  (newType: number) => {
    currentType.value = newType
  },
  { immediate: true }
)

watch(
  () => props.defaultValue,
  (newValue: DateOption) => {
    currentValue.value = newValue
  },
  { immediate: true }
)

// 基础 tabs：优先使用外部传入，其次使用 H5 权限 Store 中的 timeDimension
const baseTabs = computed<DateOption[]>(() => {
  let tabs: DateOption[] = []
  const propTabs = props.tabs || []

  if (Array.isArray(propTabs) && propTabs.length > 0) {
    tabs = propTabs as DateOption[]
  } else {
    tabs = (permissionsStore.getTimeDimensionList || []) as DateOption[]
  }

  if (Array.isArray(props.excludeCodes) && props.excludeCodes.length > 0) {
    const excludes = props.excludeCodes
    tabs = tabs.filter(t => !excludes!.includes(t.code))
  }

  return tabs
})

// tabs 末尾补充“自定义”标签，用于弹出日历
const displayTabs = computed(() => {
  const customTab: DateOption = {
    name: '自定义',
    code: customCode,
    startTime: '',
    endTime: ''
  }
  return [...baseTabs.value, customTab]
})

// 当前选中标签对象
const currentTab = computed(() =>
  displayTabs.value.find((tab: DateOption) => tab.code === currentType.value)
)

// 监听 tabs 变化：当有合法的 tabs 数据时，初始化一次默认选中项
watch(
  baseTabs,
  newTabs => {
    if (Array.isArray(newTabs) && newTabs.length > 0) {
      initSelection()
    }
  },
  { immediate: true }
)

// 默认时间配置变化时（例如权限/菜单刷新），重新初始化选中项
watch(
  () => props.defaultUnitInfo,
  () => {
    initSelection()
  }
)

/**
 * 子级选项选择：更新状态并透出事件，关闭子级面板
 */
const handleDateSelect = (type: number, option: DateOption): void => {
  currentType.value = type
  currentValue.value = option
  emit('change', type, option)
  isContentVisible.value = false
}

/**
 * 顶部类型切换
 * - 普通标签：无子级直接选中；有子级展开子级面板
 * - 自定义（code=999）：弹出日历并支持回显
 */
const handleTypeChange = (type: number): void => {
  currentType.value = type
  emit('typeChange', type)

  // 自定义：弹出日历
  if (type === customCode) {
    // 打开前设置默认回显区间：优先使用“当前已选 option 的日期范围”
    // 例如当前选了“近七日”，则自定义默认回显近七日的起止日期
    if (currentValue.value && currentValue.value.startTime && currentValue.value.endTime) {
      const start = dayjs(currentValue.value.startTime)
      const end = dayjs(currentValue.value.endTime)
      if (start.isValid() && end.isValid()) {
        const [s, e] = clampRange([start.toDate(), end.toDate()])
        customDefaultRange.value = [s, e]
      } else {
        customDefaultRange.value = getThisMonthToTodayRange()
      }
    } else {
      // 无可用的当前范围时，回退到“本月初至今天”
      customDefaultRange.value = getThisMonthToTodayRange()
    }
    showCalendar.value = true
    return
  }

  const tab = displayTabs.value.find((t: DateOption) => t.code === type)
  const hasChild = !!(tab && tab.child && tab.child.length > 0)
  if (!hasChild && tab) {
    handleDateSelect(type, tab)
    return
  }
  isContentVisible.value = !isContentVisible.value
}

/**
 * 标签是否激活（用于样式）
 */
const isActivedObj = (tab: DateOption): any => {
  let active = false
  let activeItem: any = undefined
  if (tab.child && tab.child.length > 0) {
    activeItem = tab.child.find((child: DateOption) => {
      return child.name === currentValue.value.name
    })
    active = !!activeItem
  } else {
    activeItem = tab
    active = tab.name === currentValue.value.name
  }
  return {
    active: active,
    activeItem: activeItem
  }
}

/**
 * 获取标签展示名称
 * - 若父级存在子级且选中其一，展示子级名称
 * - 否则展示父级原始名称
 */
const getTabDisplayName = (tab: DateOption): string => {
  const hasChild = !!(tab.child && tab.child.length > 0)
  if (hasChild) {
    const activeItem = isActivedObj(tab)
    if (activeItem.active && activeItem.activeItem) {
      return activeItem.activeItem.name
    }
  }
  return tab.name
}

// 当前是否匹配某个快捷范围，用于高亮底部快捷按钮
const currentQuickKey = computed<string | null>(() => {
  // 仅在自定义模式下才考虑快捷激活状态
  if (currentType.value !== customCode) return null
  if (!currentValue.value || !currentValue.value.startTime || !currentValue.value.endTime)
    return null

  const curStart = dayjs(currentValue.value.startTime).startOf('day')
  const curEnd = dayjs(currentValue.value.endTime).startOf('day')

  if (lastQuickKey.value) {
    const lastConfig = quickRanges.value.find(item => item.key === lastQuickKey.value)
    if (lastConfig) {
      const [s, e] = getQuickRange(lastConfig)
      const [clampedStart, clampedEnd] = clampRange([s, e])
      const sDay = dayjs(clampedStart).startOf('day')
      const eDay = dayjs(clampedEnd).startOf('day')
      if (curStart.isSame(sDay, 'day') && curEnd.isSame(eDay, 'day')) {
        return lastQuickKey.value
      }
    }
  }

  for (const item of quickRanges.value) {
    const [s, e] = getQuickRange(item)
    const [clampedStart, clampedEnd] = clampRange([s, e])
    const sDay = dayjs(clampedStart).startOf('day')
    const eDay = dayjs(clampedEnd).startOf('day')
    if (curStart.isSame(sDay, 'day') && curEnd.isSame(eDay, 'day')) {
      return item.key
    }
  }

  return null
})

/**
 * van-calendar 确认回调（range）
 * - 隐藏确认按钮（show-confirm=false），选择完成即触发 confirm
 * - 将所选区间按组件既有逻辑赋值，并保存回显区间
 */
const onCalendarConfirm = (values: [Date, Date], fromQuick = false) => {
  try {
    if (!values || values.length < 2) return
    if (!fromQuick) {
      lastQuickKey.value = null
    }
    const [startRaw, endRaw] = values
    const [start, end] = clampRange([startRaw, endRaw])
    // 保存回显区间
    customDefaultRange.value = [start, end]
    // 使用原组件赋值逻辑（name 固定为“自定义”以保持激活判断一致）
    const option: DateOption = {
      name: '自定义',
      code: customCode,
      startTime: dayjs(start).format('YYYY-MM-DD'),
      endTime: dayjs(end).format('YYYY-MM-DD')
    }
    handleDateSelect(customCode, option)
  } finally {
    showCalendar.value = false
  }
}

/**
 * 日历内快捷日期选择
 * - 配置化支持多种快捷模式
 * - 选择后直接关闭弹框并透出 change 事件
 */
const getQuickRange = (config: QuickRangeConfig): [Date, Date] => {
  const today = dayjs()
  switch (config.mode) {
    case 'lastDays': {
      // 近 N 天：从 N-1 天前到今天
      const days = Number(config.value) || 0
      if (days <= 0) {
        return [today.toDate(), today.toDate()]
      }
      const start = today
        .subtract(days - 1, 'day')
        .startOf('day')
        .toDate()
      const end = today.endOf('day').toDate()
      return [start, end]
    }
    case 'thisQuarter': {
      // 本季度：根据当前月份计算季度起始月
      const month = today.month() // 0 ~ 11
      const quarterStartMonth = Math.floor(month / 3) * 3
      const start = today.month(quarterStartMonth).startOf('month').toDate()
      const end = today.endOf('day').toDate()
      return [start, end]
    }
    case 'thisYear': {
      // 本年：当年第一天到今天
      const start = today.startOf('year').toDate()
      const end = today.endOf('day').toDate()
      return [start, end]
    }
    default:
      // 兜底：返回今天
      return [today.toDate(), today.toDate()]
  }
}

const handleQuickRange = (config: QuickRangeConfig) => {
  lastQuickKey.value = config.key
  const [start, end] = getQuickRange(config)
  onCalendarConfirm([start, end], true)
}

/**
 * 收敛区间到约束：
 * - 结束不得晚于今天
 * - 区间长度最多一年（含首尾天）
 */
function clampRange(range: [Date, Date]): [Date, Date] {
  let [s, e] = range
  if (s > e) [s, e] = [e, s]
  const end = e > maxDate.value ? new Date(maxDate.value) : new Date(e)
  const start = new Date(s)
  // 计算含首尾天数
  const oneDay = 24 * 60 * 60 * 1000
  const startDay = new Date(start)
  startDay.setHours(0, 0, 0, 0)
  const endDay = new Date(end)
  endDay.setHours(0, 0, 0, 0)
  const diffDays = Math.floor((endDay.getTime() - startDay.getTime()) / oneDay) + 1
  if (diffDays > maxRange) {
    const newStart = new Date(endDay)
    newStart.setDate(newStart.getDate() - (maxRange - 1))
    return [newStart, endDay]
  }
  return [startDay, endDay]
}
</script>

<template>
  <div v-if="displayTabs && displayTabs.length > 0" class="h-date-filter">
    <!-- 顶部日期类型标签 -->
    <div class="date-tabs flex-y-center">
      <div
        v-for="tab in displayTabs"
        :key="tab.code"
        class="tab-item"
        :class="{
          'is-active': isActivedObj(tab).active,
          'is-empty': !tab.child || tab.child.length === 0
        }"
        @click="handleTypeChange(tab.code)"
      >
        <span style="word-break: keep-all">{{ getTabDisplayName(tab) }}</span>
        <van-icon
          v-if="tab.child && tab.child.length"
          name="arrow-down"
          color="#4E5969"
          size="12"
        />
      </div>
    </div>

    <!-- 子级内容区（仅在存在子级时展示） -->
    <div
      v-if="isContentVisible && currentTab && currentTab.child && currentTab.child.length"
      class="date-content flex-center"
    >
      <div class="date-layout flex-y-center flex-wrap">
        <template v-for="(option, index) in currentTab.child" :key="index">
          <div
            class="date-item"
            v-if="index < 5"
            :class="{ 'is-active': currentValue?.name === option.name }"
            @click="handleDateSelect(currentType, option)"
          >
            {{ option.name }}
          </div>
        </template>
      </div>
    </div>

    <!-- 自定义日期选择弹框：van-calendar -->
    <van-calendar
      v-model:show="showCalendar"
      type="range"
      :row-height="52"
      :show-confirm="false"
      :allow-same-day="true"
      :max-range="maxRange"
      :min-date="minDate"
      :max-date="maxDate"
      :default-date="customDefaultRange"
      range-prompt="最多可选择一年"
      class="date-custom-calendar"
      @confirm="onCalendarConfirm"
    >
      <template #subtitle>
        <div class="calendar-quick-bar">
          <div
            v-for="item in quickRanges"
            :key="item.key"
            class="quick-item"
            :class="{ 'is-active': currentQuickKey === item.key }"
            @click="handleQuickRange(item)"
          >
            {{ item.label }}
          </div>
        </div>
      </template>
    </van-calendar>
  </div>
</template>

<style lang="scss" scoped>
::v-deep(.van-popup__close-icon) {
  position: absolute !important;
}

.date-custom-calendar {
  .calendar-quick-bar {
    display: flex;
    justify-content: space-between;
    gap: 8px;
    padding: 0 10px;

    .quick-item {
      flex: 1;
      height: 24px;
      line-height: 24px;
      text-align: center;
      border-radius: 18px;
      background: #f2f3f5;
      font-weight: 400;
      font-size: 12px;
      color: #1f2733;

      &.is-active {
        font-weight: 600;
        background: #e2f3fe;
        color: #0062ff;
      }
    }
  }
}

.h-date-filter {
  width: fit-content;
}

.date-tabs {
  // gap: 12px;
  background: #f2f3f5;
  border-radius: 4px;

  .tab-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 6px 8px;
    font-weight: 400;
    font-size: 14px;
    color: #1f2733;
    &:first-child {
      border-radius: 4px 0 0 4px !important;
    }
    &:last-child {
      border-radius: 0 4px 4px 0 !important;
    }

    &.is-active {
      background: #e2f3fe !important;
      font-weight: 600 !important;
      color: #0062ff !important;
    }
    &.is-empty {
      justify-content: center !important;
    }
  }
}

.date-content {
  padding-top: 12px;

  .date-layout {
    gap: 16px;
  }

  .date-item {
    padding: 4px 12px;
    background: #f2f3f5;
    border-radius: 18px;
    font-weight: 400;
    font-size: 12px;
    color: #1f2733;
  }

  .is-active {
    background: #e2f3fe;
    border-radius: 18px;
    font-weight: 600;
    font-size: 12px;
    color: #0062ff;
  }
}
</style>
