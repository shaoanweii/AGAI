<script setup lang="ts">
defineOptions({
  name: 'BtnSwitch'
})

interface Option {
  label?: string
  value?: any
  key?: string
  disabled?: boolean
  [key: string]: any
}

interface Props {
  /** 选项列表 */
  options: Option[]
  /** 是否多选模式 */
  multiple?: boolean
  /** 是否禁用整个组件 */
  disabled?: boolean
  /** 选项标签字段名，默认为 'label' 或 'value' */
  labelKey?: string
  /** 选项值字段名，默认为 'value' 或 'key' */
  valueKey?: string
}

const props = withDefaults(defineProps<Props>(), {
  multiple: false,
  disabled: false,
  labelKey: '',
  valueKey: ''
})

/** 当前选中的值，单选时为单个值，多选时为数组 */
const modelValue = defineModel<any>()

// 初始化默认值：如果 modelValue 未定义，根据 multiple 模式设置默认值
if (modelValue.value === undefined || modelValue.value === null) {
  modelValue.value = props.multiple ? [] : undefined
}

const emit = defineEmits<{
  change: [value: any]
}>()

/**
 * 获取选项的显示标签
 */
const getOptionLabel = (option: Option): string => {
  if (props.labelKey) {
    return option[props.labelKey] ?? ''
  }
  return option.label ?? option.value ?? ''
}

/**
 * 获取选项的值
 */
const getOptionValue = (option: Option): any => {
  if (props.valueKey) {
    return option[props.valueKey]
  }
  return option.value ?? option.key
}

/**
 * 判断选项是否被选中
 */
const isSelected = (value: any): boolean => {
  if (props.multiple) {
    const selectedValues = Array.isArray(modelValue.value) ? modelValue.value : []
    return selectedValues.includes(value)
  }
  return modelValue.value === value
}

/**
 * 处理选项点击
 */
const handleSelect = (option: Option): void => {
  if (props.disabled || option.disabled) {
    return
  }

  const value = getOptionValue(option)

  if (props.multiple) {
    // 多选模式
    const currentValue = Array.isArray(modelValue.value) ? modelValue.value : []
    const index = currentValue.indexOf(value)

    if (index > -1) {
      // 如果已选中，则取消选中
      modelValue.value = currentValue.filter(item => item !== value)
    } else {
      // 如果未选中，则添加
      modelValue.value = [...currentValue, value]
    }
  } else {
    // 单选模式
    if (modelValue.value !== value) {
      modelValue.value = value
    }
  }

  emit('change', modelValue.value)
}
</script>

<template>
  <div class="switch-btn-wrapper" :class="{ disabled: disabled }">
    <template v-for="(item, index) in options" :key="index">
      <div
        class="sw-item"
        :class="{
          active: isSelected(getOptionValue(item)),
          disabled: disabled || item.disabled
        }"
        @click="handleSelect(item)"
      >
        {{ getOptionLabel(item) }}
      </div>
    </template>
  </div>
</template>

<style lang="scss" scoped>
.switch-btn-wrapper.disabled {
  pointer-events: none;
  opacity: 0.6;
}
</style>
