<script setup lang="ts">
defineOptions({
  name: 'SwitchButton'
})

interface SwitchOption {
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
  options: SwitchOption[]
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
  change: [value: SwitchOption]
}>()

/**
 * 处理选项点击
 * @param option 被点击的选项
 */
const handleOptionClick = (option: SwitchOption): void => {
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
const getOptionClass = (option: SwitchOption) => {
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
  border: 1px solid var(--brand-primary);
  transition: all 0.2s ease-in-out;
  line-height: normal;
  vertical-align: middle;
  height: 32px;
  box-sizing: border-box;

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
    min-width: 64px;
    padding: 9px 10px;
    text-align: center;
    font-size: var(--font-size-caption);
    color: var(--brand-primary);
    cursor: pointer;
    background: #eaf3ff;
    user-select: none;
    line-height: 12px;
    height: 30px;
    box-sizing: border-box;
    display: flex;
    align-items: center;
    justify-content: center;

    &:first-child {
      border-radius: var(--border-radius-l) 0 0 var(--border-radius-l);
    }

    &:last-child {
      border-radius: 0 var(--border-radius-l) var(--border-radius-l) 0;
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
