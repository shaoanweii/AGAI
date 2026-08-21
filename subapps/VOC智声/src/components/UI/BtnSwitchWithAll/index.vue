<script setup lang="ts">
import { computed } from 'vue'
import BtnSwitch from '@/components/UI/BtnSwitch/index.vue'

defineOptions({
  name: 'BtnSwitchWithAll'
})

interface Option {
  label?: string
  value?: string
  key?: string
  disabled?: boolean
  [key: string]: any
}

interface Props {
  /** 选项列表，不需要包含“全部” */
  options: Option[]
  /** 是否禁用整个组件 */
  disabled?: boolean
  /** “全部”展示文案 */
  allLabel?: string
  /** “全部”内部占位值，仅用于组件展示，不会写入外部 v-model */
  allValue?: string
  /** 选中全部具体项时是否收敛为“全部” */
  collapseAllWhenFullSelected?: boolean
  /** 选项标签字段名，默认为 'label' 或 'value' */
  labelKey?: string
  /** 选项值字段名，默认为 'value' 或 'key' */
  valueKey?: string
}

const props = withDefaults(defineProps<Props>(), {
  options: () => [],
  disabled: false,
  allLabel: '全部',
  allValue: 'all',
  collapseAllWhenFullSelected: true,
  labelKey: '',
  valueKey: ''
})

// 对外值只保存真实业务选项，空数组表示“全部/不限”
const modelValue = defineModel<string[]>({
  default: () => []
})

const emit = defineEmits<{
  change: [value: string[]]
}>()

/**
 * 获取选项值，兼容 BtnSwitch 默认的 value/key 规则。
 * @param option 当前选项
 * @returns 选项值
 */
const getOptionValue = (option: Option) => {
  if (props.valueKey) {
    return option[props.valueKey]
  }
  return option.value ?? option.key
}

/**
 * 对数组去重并保留原始顺序。
 * @param list 原始数组
 * @returns 去重后的数组
 */
const dedupePreserveOrder = (list: string[]) => {
  const seen = new Set<string>()
  const result: string[] = []
  list.forEach(item => {
    if (seen.has(item)) {
      return
    }
    seen.add(item)
    result.push(item)
  })
  return result
}

const normalOptions = computed(() => {
  return (props.options || []).filter(item => getOptionValue(item) !== props.allValue)
})

const normalOptionValues = computed(() => {
  return normalOptions.value
    .map(item => getOptionValue(item))
    .filter((item): item is string => typeof item === 'string' && !!item)
})

const optionValueSet = computed(() => new Set(normalOptionValues.value))

const innerOptions = computed<Option[]>(() => [
  {
    label: props.allLabel,
    value: props.allValue
  },
  ...normalOptions.value
])

const innerValue = computed<string[]>({
  get: () => {
    const selected = Array.isArray(modelValue.value) ? modelValue.value : []
    const validSelected = dedupePreserveOrder(
      selected.filter(item => item && optionValueSet.value.has(item))
    )
    return validSelected.length > 0 ? validSelected : [props.allValue]
  },
  set: value => {
    const current = Array.isArray(value) ? value : []
    const selectedValues = dedupePreserveOrder(
      current.filter(item => item && item !== props.allValue && optionValueSet.value.has(item))
    )
    const hasAll = current.includes(props.allValue)
    const previousHasConcrete = Array.isArray(modelValue.value) && modelValue.value.length > 0

    let nextValue: string[] = []
    if (hasAll && previousHasConcrete) {
      // 已选具体项时点击“全部”，回到不限态。
      nextValue = []
    } else if (selectedValues.length === 0) {
      nextValue = []
    } else if (
      props.collapseAllWhenFullSelected &&
      normalOptionValues.value.length > 0 &&
      selectedValues.length === normalOptionValues.value.length &&
      normalOptionValues.value.every(item => selectedValues.includes(item))
    ) {
      nextValue = []
    } else {
      nextValue = selectedValues
    }

    modelValue.value = nextValue
    emit('change', nextValue)
  }
})
</script>

<template>
  <BtnSwitch
    v-model="innerValue"
    :options="innerOptions"
    multiple
    :disabled="disabled"
    :label-key="labelKey"
    :value-key="valueKey"
  />
</template>
