<script setup lang="ts">
import { useUserStore } from '@/store'
import { computed } from 'vue'

interface BrandOption {
  label?: string
  value?: any
  [key: string]: any
}

interface OptionProps {
  label?: string
  value?: string
}

interface Props {
  modelValue?: any
  options?: BrandOption[]
  disabled?: boolean
  props?: OptionProps
  multiple?: boolean
  /**
   * 是否允许再次点击已选中的品牌进行取消（清空选择）
   * 默认 false，保持现有页面行为不变
   */
  clearable?: boolean
  atLeastOne?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  options: () => [],
  disabled: false,
  props: () => ({ label: 'value', value: 'key' }),
  multiple: false,
  atLeastOne: false, // 是否至少选择一项
  clearable: false
})

const selectedValue = defineModel<any>()

const userStore = useUserStore()

const brandOptions = computed(() => {
  // 有传入options就用options，否则使用全局品牌数据. 只显示品牌,不显示车系
  return props.options.length > 0 ? props.options : userStore.getBrandService
})

const resolvedProps = computed(() => {
  const defaultProps = { label: 'value', value: 'key' }
  const mergedProps = { ...defaultProps, ...(props.props || {}) }

  const sample = brandOptions.value?.[0]
  if (sample && mergedProps.label in sample && mergedProps.value in sample) {
    return mergedProps
  }

  return defaultProps
})

const getOptionValue = (item: BrandOption) => item?.[resolvedProps.value.value]

const getOptionLabel = (item: BrandOption) => item?.[resolvedProps.value.label]

const selectedValues = computed(() => {
  if (Array.isArray(selectedValue.value)) {
    return selectedValue.value
  }
  if (
    selectedValue.value === undefined ||
    selectedValue.value === null ||
    selectedValue.value === ''
  ) {
    return []
  }
  return [selectedValue.value]
})

const isSelected = (value: any) => {
  return selectedValues.value.includes(value)
}

const handleSelect = (value: any) => {
  if (props.disabled) return

  if (props.multiple) {
    if (isSelected(value)) {
      if (props.atLeastOne && selectedValues.value.length <= 1) {
        // 至少选择一项
        return
      }
      selectedValue.value = selectedValues.value.filter(item => item !== value)
      return
    }
    selectedValue.value = [...selectedValues.value, value]
    return
  }

  // 可清空模式：再次点击已选项则取消选中
  if (props.clearable && selectedValue.value === value) {
    selectedValue.value = ''
    return
  }

  selectedValue.value = value
}
</script>

<template>
  <div class="switch-btn-wrapper" :class="{ disabled: props.disabled }">
    <template v-for="item in brandOptions" :key="getOptionValue(item)">
      <div
        class="sw-item"
        :class="{ active: isSelected(getOptionValue(item)), disabled: props.disabled }"
        @click="handleSelect(getOptionValue(item))"
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
