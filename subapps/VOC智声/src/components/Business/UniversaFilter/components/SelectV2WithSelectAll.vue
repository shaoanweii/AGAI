<script setup lang="ts">
import { ref, computed } from 'vue'

interface Props {
  modelValue?: any
  options?: any[]
  props?: Record<string, any>
  clearable?: boolean
  placeholder?: string
  multiple?: boolean
  maxCollapseTags?: number
  collapseTags?: boolean
  filterable?: boolean
  showSelectAll?: boolean // 是否显示全选按钮
  valueKey?: string // 用于提取值的键，默认 'code'
  [key: string]: any // 支持 el-select-v2 的其他属性
}

const props = withDefaults(defineProps<Props>(), {
  options: () => [],
  clearable: true,
  multiple: true,
  maxCollapseTags: 1,
  collapseTags: true,
  filterable: true,
  showSelectAll: false,
  valueKey: 'code'
})

const emit = defineEmits<{
  'update:modelValue': [value: any]
  change: [value: any]
}>()

const selectRef = ref<any>(null)

const modelValue = computed({
  get: () => props.modelValue,
  set: val => {
    emit('update:modelValue', val)
    emit('change', val)
  }
})

/**
 * 从 el-select-v2 的 filteredOptions 中提取选项值
 */
const normalizeCodesFromSelectV2 = (raw: unknown): string[] | null => {
  if (!Array.isArray(raw)) return null
  const seen = new Set<string>()
  const codes: string[] = []
  // 优先使用 props.props.value，如果没有则使用 valueKey
  const valueProp = props.props?.value || props.valueKey
  for (const item of raw) {
    const record = item && typeof item === 'object' ? (item as Record<string, any>) : {}
    if (record.type === 'Group') continue
    const code = String(record[valueProp] ?? '').trim()
    if (!code) continue
    if (seen.has(code)) continue
    seen.add(code)
    codes.push(code)
  }
  return codes
}

/**
 * 获取当前显示的选项值列表（考虑过滤条件）
 */
const displayedCodes = computed(() => {
  const raw = selectRef.value?.filteredOptions
  const codes = normalizeCodesFromSelectV2(raw)
  if (codes !== null) return codes
  // 如果没有过滤选项，返回所有选项的值
  // 优先使用 props.props.value，如果没有则使用 valueKey
  const valueProp = props.props?.value || props.valueKey
  return props.options
    .map((item: any) => {
      return String(item[valueProp] ?? '').trim()
    })
    .filter(Boolean)
})

/**
 * 获取已选中的显示选项数量
 */
const selectedDisplayedCount = computed(() => {
  const displayedSet = new Set(displayedCodes.value)
  const selectedCodes = Array.isArray(modelValue.value) ? modelValue.value : []
  return selectedCodes.filter((code: string) => displayedSet.has(code)).length
})

/**
 * 全选复选框的状态
 */
const checkAll = computed({
  get: () => {
    const displayedCount = displayedCodes.value.length
    if (!displayedCount) return false
    return selectedDisplayedCount.value === displayedCount
  },
  set: val => {
    const checked = Boolean(val)
    const displayedCodesList = displayedCodes.value
    if (!displayedCodesList.length) return

    const displayedSet = new Set(displayedCodesList)
    const currentSelected = Array.isArray(modelValue.value) ? modelValue.value : []

    if (checked) {
      // 全选：合并当前选中和显示的选项
      const merged = [...currentSelected, ...displayedCodesList]
      modelValue.value = Array.from(new Set(merged))
    } else {
      // 取消全选：移除所有显示的选项
      modelValue.value = currentSelected.filter((code: string) => !displayedSet.has(code))
    }
  }
})

/**
 * 全选复选框的半选状态
 */
const indeterminate = computed(() => {
  const displayedCount = displayedCodes.value.length
  if (!displayedCount) return false
  const selectedCount = selectedDisplayedCount.value
  return selectedCount > 0 && selectedCount < displayedCount
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
      <el-checkbox v-model="checkAll" :indeterminate="indeterminate"> 全选 </el-checkbox>
    </template>
  </el-select-v2>
</template>
