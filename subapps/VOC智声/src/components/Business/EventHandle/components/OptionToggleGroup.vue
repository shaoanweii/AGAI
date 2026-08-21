<script setup lang="ts">
// 根据选项切换
defineOptions({
  name: 'OptionToggleGroup'
})

interface Option {
  [key: string]: any
  disabled?: boolean
}

const props = withDefaults(
  defineProps<{
    options: Option[]
    labelKey?: string
    valueKey?: string
  }>(),
  {
    labelKey: 'label',
    valueKey: 'value'
  }
)

const modelValue = defineModel<string>({ default: '' })

/**
 * 根据字段配置读取选项展示文案，兼容后端原始字段与历史 label/value 字段。
 *
 * @param item 当前选项
 * @returns 选项展示文案
 */
const getOptionLabel = (item: Option) => item[props.labelKey]

/**
 * 根据字段配置读取选项值，避免调用方为了适配组件额外转换后端数据。
 *
 * @param item 当前选项
 * @returns 选项值
 */
const getOptionValue = (item: Option) => item[props.valueKey]

const handleToggle = (item: Option) => {
  if (item.disabled) return
  modelValue.value = getOptionValue(item)
}
</script>

<template>
  <div class="option-toggle-group">
    <div
      v-for="(item, index) of props.options"
      :key="index"
      :class="[
        'otg-item',
        {
          tap: modelValue === getOptionValue(item),
          'is-disabled': item.disabled
        }
      ]"
      :aria-disabled="item.disabled"
      @click="handleToggle(item)"
    >
      {{ getOptionLabel(item) }}
    </div>
  </div>
</template>

<style lang="scss" scoped>
.option-toggle-group {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;

  .otg-item {
    padding: 6px 18px;
    border-radius: 4px;
    border: 1px solid #dfe2e8;
    font-weight: 400;
    font-size: 14px;
    color: #535862;
    line-height: 20px;
    cursor: pointer;

    &.tap {
      border: 1px solid #1677ff;
      color: #1677ff;
      font-weight: 500;
    }

    &.is-disabled {
      border-color: #e4e7ed;
      background-color: #f5f7fa;
      color: #c0c4cc;
      cursor: not-allowed;
    }
  }
}
</style>
