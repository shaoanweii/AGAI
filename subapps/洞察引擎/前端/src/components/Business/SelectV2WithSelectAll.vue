<script setup lang="ts">
import { computed, ref } from 'vue'

defineOptions({
  name: 'SelectV2WithSelectAll',
  inheritAttrs: false
})

type SelectOption = Record<string, any>

interface SelectV2Expose {
  filteredOptions?: SelectOption[]
}

interface Props {
  modelValue?: unknown[]
  options?: SelectOption[]
  props?: Record<string, any>
  clearable?: boolean
  placeholder?: string
  multiple?: boolean
  maxCollapseTags?: number
  collapseTags?: boolean
  filterable?: boolean
  showSelectAll?: boolean
  valueKey?: string
}

const props = withDefaults(defineProps<Props>(), {
  options: () => [],
  clearable: true,
  multiple: true,
  maxCollapseTags: 1,
  collapseTags: true,
  filterable: true,
  showSelectAll: false,
  valueKey: 'value'
})

const emit = defineEmits<{
  'update:modelValue': [value: unknown[]]
  change: [value: unknown[]]
}>()

const selectRef = ref<SelectV2Expose | null>(null)

/**
 * @description: 统一取下拉值字段，兼容外部通过 props.value 或 valueKey 自定义 value 键名。
 * @return {string} 当前选项 value 字段名
 */
const optionValueField = computed(() => {
  return String(props.props?.value || props.valueKey || 'value')
})

/**
 * @description: 将任意 value 归一化成可比较的 key，保证全选逻辑可稳定去重。
 * @param {unknown} value 原始选项值
 * @return {string} 归一化后的比较 key
 */
const normalizeValueKey = (value: unknown) => {
  return String(value ?? '').trim()
}

const modelValue = computed<unknown[]>({
  get: () => {
    return Array.isArray(props.modelValue) ? props.modelValue : []
  },
  set: value => {
    emit('update:modelValue', value)
    emit('change', value)
  }
})

/**
 * @description: 提取当前下拉面板展示的有效选项值；搜索过滤时优先使用 filteredOptions。
 * @param {unknown} rawOptions el-select-v2 暴露的过滤后选项
 * @return {unknown[] | null} 当前展示的 value 列表；若组件尚未暴露过滤结果则返回 null
 */
const extractDisplayedValues = (rawOptions: unknown): unknown[] | null => {
  if (!Array.isArray(rawOptions)) return null

  const displayedValues: unknown[] = []
  const seenKeys = new Set<string>()

  rawOptions.forEach(option => {
    const record = option && typeof option === 'object' ? (option as SelectOption) : null
    if (!record || record.type === 'Group') return

    const optionValue = record[optionValueField.value]
    const optionKey = normalizeValueKey(optionValue)
    if (!optionKey || seenKeys.has(optionKey)) return

    seenKeys.add(optionKey)
    displayedValues.push(optionValue)
  })

  return displayedValues
}

const displayedValues = computed(() => {
  const filteredValues = extractDisplayedValues(selectRef.value?.filteredOptions)
  if (filteredValues) return filteredValues

  const fallbackValues: unknown[] = []
  const seenKeys = new Set<string>()
  props.options.forEach(option => {
    const optionValue = option?.[optionValueField.value]
    const optionKey = normalizeValueKey(optionValue)
    if (!optionKey || seenKeys.has(optionKey)) return

    seenKeys.add(optionKey)
    fallbackValues.push(optionValue)
  })
  return fallbackValues
})

const selectedDisplayedCount = computed(() => {
  const displayedKeySet = new Set(displayedValues.value.map(item => normalizeValueKey(item)))
  return modelValue.value.filter(item => displayedKeySet.has(normalizeValueKey(item))).length
})

const checkAll = computed({
  get: () => {
    const currentDisplayedCount = displayedValues.value.length
    if (!currentDisplayedCount) return false
    return selectedDisplayedCount.value === currentDisplayedCount
  },
  set: checked => {
    const currentDisplayedValues = displayedValues.value
    if (!currentDisplayedValues.length) return

    const displayedKeySet = new Set(currentDisplayedValues.map(item => normalizeValueKey(item)))
    const currentSelected = Array.isArray(modelValue.value) ? modelValue.value : []

    if (checked) {
      // 中文注释：保留用户已选的其他值，仅把当前过滤结果内的项补齐到选中集合。
      const mergedValues: unknown[] = []
      const seenKeys = new Set<string>()
      ;[...currentSelected, ...currentDisplayedValues].forEach(item => {
        const itemKey = normalizeValueKey(item)
        if (!itemKey || seenKeys.has(itemKey)) return
        seenKeys.add(itemKey)
        mergedValues.push(item)
      })
      modelValue.value = mergedValues
      return
    }

    modelValue.value = currentSelected.filter(item => !displayedKeySet.has(normalizeValueKey(item)))
  }
})

const indeterminate = computed(() => {
  const currentDisplayedCount = displayedValues.value.length
  if (!currentDisplayedCount) return false

  return selectedDisplayedCount.value > 0 && selectedDisplayedCount.value < currentDisplayedCount
})
</script>

<template>
  <el-select-v2
    ref="selectRef"
    v-model="modelValue"
    :options="options"
    :props="props.props"
    :clearable="clearable"
    :placeholder="placeholder"
    :multiple="multiple"
    :max-collapse-tags="maxCollapseTags"
    :collapse-tags="collapseTags"
    :filterable="filterable"
    v-bind="$attrs"
  >
    <template v-if="showSelectAll && multiple" #header>
      <div class="select-v2-header">
        <el-checkbox v-model="checkAll" :indeterminate="indeterminate">全选</el-checkbox>
      </div>
    </template>
  </el-select-v2>
</template>

<style scoped lang="scss">
.select-v2-header {
  padding-left: 10px;
}
</style>
