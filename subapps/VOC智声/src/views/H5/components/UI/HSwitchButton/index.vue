<script setup lang="ts">
defineOptions({ name: 'HSwitchButton' })

interface HSwitchOption {
  /** 选项值 */
  value: string | number
  /** 选项标签 */
  label: string
  /** 是否禁用 */
  disabled?: boolean
}

interface Props {
  /** 当前选中的值 */
  // modelValue: string | number
  /** 切换选项 */
  options: HSwitchOption[]
  /** 是否禁用整个组件 */
  disabled?: boolean
  /** 自定义样式类名 */
  customClass?: string
}

const modelValue = defineModel<string | number>({ default: 'rate' })

const props = withDefaults(defineProps<Props>(), {
  disabled: false,
  customClass: '',
  options: () => [
    { value: 'rate', label: '负面率' },
    { value: 'value', label: '提及量' }
  ]
})

const emit = defineEmits<{
  change: [value: HSwitchOption]
}>()

/**
 * 处理选项点击
 * @param option 被点击的选项
 */
const handleOptionClick = (option: HSwitchOption): void => {
  if (props.disabled || option.disabled || option.value === modelValue.value) {
    return
  }
  modelValue.value = option.value
  emit('change', option)
}

/**
 * 计算选项的样式类
 * @param option 选项
 */
const getOptionClass = (option: HSwitchOption) => {
  return {
    'switch-btn': true,
    active: option.value === modelValue.value,
    disabled: props.disabled || option.disabled
  }
}
</script>

<template>
  <div class="switch-button11" :class="[customClass, { disabled }]">
    <div
      v-for="option in options"
      :key="option.value"
      class="switch-btn"
      :class="getOptionClass(option)"
      @click="handleOptionClick(option)"
    >
      {{ option.label }}
    </div>
  </div>
</template>

<style scoped lang="scss">
.switch-button11 {
  display: inline-flex;
  background: #e9f2ff;
  border-radius: var(--border-radius-l);
  transition: all 0.2s ease-in-out;

  &.disabled {
    opacity: 0.6;
    cursor: not-allowed;

    .switch-btn {
      cursor: not-allowed;

      // &:hover {
      //   background: #eaf3ff;
      //   color: var(--brand-primary);
      // }
    }
  }

  .switch-btn {
    min-width: 40px;
    padding: 5px 10px;
    text-align: center;
    font-weight: 400;
    font-size: 12px;
    color: #1F2733;
    cursor: pointer;
    background: #eaf3ff;
    user-select: none;
    border: 1px solid var(--brand-primary);

    &:first-child {
      border-radius: var(--border-radius-l) 0 0 var(--border-radius-l);
      border-right-width: 0;
    }

    &:last-child {
      border-radius: 0 var(--border-radius-l) var(--border-radius-l) 0;
      border-left-width: 0;
    }

    &:not(:first-child):not(:last-child) {
      border-radius: 0;
    }
    &:not(:last-child) {
      border-right: 1px solid var(--brand-primary);
    }

    // &:hover:not(.disabled) {
    //   background: rgba(22, 119, 255, 0.1);
    // }

    &.active {
      background: linear-gradient(204deg, #42a8fe 0%, #1677ff 100%);
      color: var(--neutral-white);
    }

    &.disabled {
      cursor: not-allowed;
      opacity: 0.6;
    }
  }
}
</style>
