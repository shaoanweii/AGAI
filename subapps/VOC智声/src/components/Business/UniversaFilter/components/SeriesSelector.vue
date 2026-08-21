<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useUserStore } from '@/store'

interface OptionProps {
  label?: string
  value?: string
}

interface Props {
  brandValue?: string | string[] | null // 品牌值
  disabled?: boolean
  options?: any[]
  props?: OptionProps
  expandWhenNonCoreSelected?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  brandValue: null,
  disabled: false,
  options: () => [],
  props: () => ({ label: 'value', value: 'key' }),
  expandWhenNonCoreSelected: false
})

const userStore = useUserStore()
const isExpanded = ref(false)

const selectedValue = defineModel<any>()

const normalizeValues = (value: unknown): any[] => {
  if (Array.isArray(value)) {
    return value.filter(item => item !== '' && item !== null && item !== undefined)
  }
  if (value === '' || value === null || value === undefined) {
    return []
  }
  return [value]
}

// 根据品牌值获取车系列表（直接使用原数据结构）
const seriesOptions = computed(() => {
  if (props.options.length > 0) {
    return props.options
  }

  const selectedBrandValues = normalizeValues(props.brandValue)

  // 如果没有品牌值，返回空数组
  if (selectedBrandValues.length === 0) {
    return []
  }

  const uniqueSeriesMap = new Map<string, any>()

  ;(userStore.getBrandService || []).forEach(brand => {
    const isSelectedBrand = selectedBrandValues.some(
      currentValue => brand.key === currentValue || brand.value === currentValue
    )
    if (!isSelectedBrand) {
      return
    }

    ;(brand.children || []).forEach((series: any) => {
      const uniqueKey = String(series.key ?? series.value ?? '')
      if (uniqueKey && !uniqueSeriesMap.has(uniqueKey)) {
        uniqueSeriesMap.set(uniqueKey, series)
      }
    })
  })

  return Array.from(uniqueSeriesMap.values())
})

const resolvedProps = computed(() => {
  const defaultProps = { label: 'value', value: 'key' }
  const mergedProps = { ...defaultProps, ...(props.props || {}) }

  const sample = seriesOptions.value?.[0]
  if (sample && mergedProps.label in sample && mergedProps.value in sample) {
    return mergedProps
  }

  return defaultProps
})

const getOptionValue = (item: any) => item?.[resolvedProps.value.value]

const getOptionLabel = (item: any) => item?.[resolvedProps.value.label]

// 核心车系（isCore === 1）
const coreSeries = computed(() => seriesOptions.value.filter((item: any) => item.isCore === 1))

// 非核心车系（isCore !== 1）
const nonCoreSeries = computed(() => seriesOptions.value.filter((item: any) => item.isCore !== 1))

const selectedValues = computed(() => {
  if (Array.isArray(selectedValue.value)) return selectedValue.value
  if (selectedValue.value === undefined || selectedValue.value === null) return []
  return [selectedValue.value]
})

const nonCoreSelected = computed(() => {
  if (!props.expandWhenNonCoreSelected) return false
  if (nonCoreSeries.value.length === 0 || selectedValues.value.length === 0) return false
  const nonCoreValueSet = new Set(nonCoreSeries.value.map((item: any) => getOptionValue(item)))
  return selectedValues.value.some(value => nonCoreValueSet.has(value))
})

// 获取当前实时的所有下拉值
const getAllOptions = () => {
  const options = [...coreSeries.value, ...nonCoreSeries.value]
  return {
    options,
    coreSeries: coreSeries.value,
    nonCoreSeries: nonCoreSeries.value
  }
}

// 处理多选
function handleSelect(optionValue: any) {
  if (props.disabled) return
  const currentValue = Array.isArray(selectedValue.value) ? selectedValue.value : []

  if (Array.isArray(currentValue)) {
    const index = currentValue.indexOf(optionValue)
    if (index > -1) {
      // 如果已选中，则取消选中
      selectedValue.value = currentValue.filter(item => item !== optionValue)
    } else {
      // 如果未选中，则添加
      selectedValue.value = [...currentValue, optionValue]
    }
  } else {
    // 如果当前值不是数组，转换为数组
    selectedValue.value = [optionValue]
  }
}

// 判断是否选中
function isSelected(optionValue: any) {
  if (Array.isArray(selectedValue.value)) {
    return selectedValue.value.includes(optionValue)
  }
  return selectedValue.value === optionValue
}

function toggleExpand() {
  isExpanded.value = !isExpanded.value
}

// 监听品牌值变化，移除不再属于当前品牌集合的车系选项
// 这样在多品牌切换时可以尽量保留仍然有效的车系选择，避免用户重复操作
watch(
  () => props.brandValue,
  () => {
    const selectedValues = normalizeValues(selectedValue.value)
    if (selectedValues.length === 0) {
      return
    }

    const availableSet = new Set(seriesOptions.value.map((item: any) => getOptionValue(item)))
    const validSelectedValues = selectedValues.filter(value => availableSet.has(value))

    if (validSelectedValues.length !== selectedValues.length) {
      selectedValue.value = validSelectedValues
    }
  },
  { deep: true }
)

watch(
  [() => props.disabled, () => props.expandWhenNonCoreSelected, nonCoreSelected],
  () => {
    if (!props.disabled || !props.expandWhenNonCoreSelected) return
    isExpanded.value = nonCoreSelected.value
  },
  { immediate: true }
)

// 暴露方法给父组件
defineExpose({
  getAllOptions
})
</script>

<template>
  <div class="series-selector" :class="{ disabled: props.disabled }">
    <div class="flex-between w-full gap-8">
      <div class="flex-1 series-main">
        <div class="switch-btn-wrapper">
          <template v-for="item in coreSeries" :key="getOptionValue(item)">
            <div
              class="sw-item"
              :class="{ active: isSelected(getOptionValue(item)), disabled: props.disabled }"
              @click="handleSelect(getOptionValue(item))"
            >
              {{ getOptionLabel(item) }}
            </div>
          </template>
        </div>
      </div>
      <div v-if="nonCoreSeries.length > 0" class="series-toggle" @click="toggleExpand">
        <template v-if="!isExpanded">
          <span class="mr-8">更多</span>
          <SvgIcon name="chevron-down" width="20px" height="20px" color="#929AA6" />
        </template>
        <template v-else>
          <span class="mr-8">收起</span>
          <SvgIcon name="chevron-up" width="20px" height="20px" color="#929AA6" />
        </template>
      </div>
    </div>
    <Transition name="expand">
      <div class="series-content w-full mt-16" v-show="isExpanded && nonCoreSeries.length > 0">
        <div class="switch-btn-wrapper">
          <template v-for="item in nonCoreSeries" :key="getOptionValue(item)">
            <div
              class="sw-item"
              :class="{ active: isSelected(getOptionValue(item)), disabled: props.disabled }"
              @click="handleSelect(getOptionValue(item))"
            >
              {{ getOptionLabel(item) }}
            </div>
          </template>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style lang="scss" scoped>
.series-selector {
  width: 100%;

  &.disabled {
    pointer-events: none;
    opacity: 0.6;
  }

  .series-main {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
  }

  .series-toggle {
    font-size: 14px;
    color: #5f6a7a;
    line-height: 32px;
    display: flex;
    align-items: center;
    cursor: pointer;
    white-space: nowrap;
  }

  .series-content {
    background: #eaf3ff;
    border-radius: 8px;
    padding: 12px 16px;
    overflow: hidden;
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
  }
}

.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s ease;
}

.expand-enter-from,
.expand-leave-to {
  max-height: 0;
  padding-top: 0;
  padding-bottom: 0;
  opacity: 0;
}

.expand-enter-to,
.expand-leave-from {
  max-height: 500px;
  opacity: 1;
}
</style>
