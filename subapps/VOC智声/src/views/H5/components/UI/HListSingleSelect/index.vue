<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'

defineOptions({ name: 'HListSingleSelect' })

export type HListSingleSelectFields = {
  /** 展示字段 */
  label: string
  /** 值字段 */
  value: string
  /** 禁用字段（可选） */
  disabled?: string
}

type InnerOption = {
  key: string
  value: any
  label: string
  disabled: boolean
  raw: any
}

const props = withDefaults(
  defineProps<{
    /** 选项列表 */
    options?: any[]
    /** 字段映射：适配不同接口返回结构 */
    fields?: HListSingleSelectFields
    /**
     * 搜索字段（基于原始 options 元素）
     * - 仅支持一层 key（如需嵌套字段，请使用 searchBy）
     * - 未配置时默认使用 label/value 搜索
     */
    searchFields?: string[]
    /**
     * 自定义搜索内容提取函数（优先级高于 searchFields）
     * - 返回内容会参与关键词匹配
     */
    searchBy?: (item: any) => string
    /** 弹框标题 */
    title?: string
    /** 输入框占位文本 */
    placeholder?: string
    /** 是否禁用 */
    disabled?: boolean
    /** 是否支持搜索 */
    searchable?: boolean
  }>(),
  {
    options: () => [],
    fields: () => ({
      label: 'label',
      value: 'value',
      disabled: 'disabled'
    }),
    searchFields: () => [],
    searchBy: undefined,
    title: '请选择',
    placeholder: '请选择',
    disabled: false,
    searchable: true
  }
)

/**
 * v-model：单选值
 * - null 代表未选择
 * - 选中时输出原始 value 类型（如 number/string）
 */
const modelValue = defineModel<string | number | null>({ default: null })
const emit = defineEmits<{
  change: [value: string | number | null, oldValue: string | number | null]
}>()

const popupVisible = ref(false)
const keywordInput = ref('')
const keyword = ref('')
const debounceTimer = ref<number | undefined>(undefined)

// 规范化 options（过滤无效项）
const normalizedOptions = computed<InnerOption[]>(() => {
  const list = Array.isArray(props.options) ? props.options : []
  const labelKey = props.fields?.label || 'label'
  const valueKey = props.fields?.value || 'value'
  const disabledKey = props.fields?.disabled || 'disabled'

  return list
    .map(item => ({
      raw: item,
      value: item?.[valueKey],
      key: String(item?.[valueKey] ?? ''),
      label: String(item?.[labelKey] ?? ''),
      disabled: Boolean(item?.[disabledKey])
    }))
    .filter(item => item.key !== '' && item.label !== '')
})

const labelMap = computed(() => {
  const map = new Map<string, string>()
  normalizedOptions.value.forEach(item => {
    if (!map.has(item.key)) map.set(item.key, item.label)
  })
  return map
})

const selectedKey = computed(() => String(modelValue.value ?? ''))
const hasSelected = computed(() => selectedKey.value !== '')

const selectedText = computed(() => {
  const key = selectedKey.value
  if (!key) return props.placeholder
  return labelMap.value.get(key) || key
})

const isSelected = (key: string) => selectedKey.value === key && key !== ''

// 搜索（轻量防抖，避免大列表频繁过滤）
watch(
  () => keywordInput.value,
  val => {
    if (debounceTimer.value) window.clearTimeout(debounceTimer.value)
    debounceTimer.value = window.setTimeout(() => {
      keyword.value = (val || '').trim()
    }, 200)
  }
)

const filteredOptions = computed(() => {
  const list = normalizedOptions.value
  const kw = keyword.value
  if (!props.searchable || !kw) return list

  const lower = kw.toLowerCase()
  return list.filter(item => {
    // 1) 自定义 searchBy 优先
    if (typeof props.searchBy === 'function') {
      const content = props.searchBy(item.raw)
      return String(content || '').toLowerCase().includes(lower)
    }

    // 2) searchFields：基于原始 item 的字段拼接
    const fields = Array.isArray(props.searchFields) ? props.searchFields : []
    if (fields.length) {
      const content = fields.map(k => String(item.raw?.[k] ?? '')).join(' ')
      return content.toLowerCase().includes(lower)
    }

    // 3) 默认：label/value
    return `${item.label} ${item.value}`.toLowerCase().includes(lower)
  })
})

const openPopup = () => {
  if (props.disabled) return
  popupVisible.value = true
  keywordInput.value = ''
  keyword.value = ''
}

const closePopup = () => {
  popupVisible.value = false
}

const handleCancel = () => {
  closePopup()
}

const commitValue = (val: string | number | null) => {
  const oldValue = modelValue.value
  if (oldValue === val) return
  modelValue.value = val
  emit('change', val, oldValue)
}

const handleItemClick = (option: InnerOption) => {
  if (option.disabled) return
  const nextValue = isSelected(option.key) ? null : option.value
  commitValue(nextValue)
  closePopup()
}

onBeforeUnmount(() => {
  if (debounceTimer.value) window.clearTimeout(debounceTimer.value)
})
</script>

<template>
  <div class="h-list-single-select">
    <div
      class="hlss-trigger"
      :class="{ 'is-disabled': disabled, 'is-placeholder': !hasSelected }"
      @click="openPopup"
    >
      <div class="hlss-trigger__text van-ellipsis">{{ selectedText }}</div>
      <van-icon class="hlss-trigger__icon" name="arrow-down" />
    </div>

    <van-popup
      v-model:show="popupVisible"
      position="bottom"
      round
      :safe-area-inset-bottom="true"
      :lock-scroll="true"
      :style="{ height: '80%' }"
      teleport="body"
      @closed="closePopup"
    >
      <div class="hlss-popup">
        <div class="hlss-popup__header">
          <div class="hlss-popup__action is-left" @click="handleCancel">取消</div>
          <div class="hlss-popup__title van-ellipsis">{{ title }}</div>
          <div class="hlss-popup__action is-right" />
        </div>

        <div v-if="searchable" class="hlss-popup__search">
          <van-search
            v-model="keywordInput"
            placeholder="搜索"
            clearable
            shape="round"
            :show-action="false"
          />
        </div>

        <div class="hlss-popup__content">
          <div v-if="filteredOptions.length === 0" class="hlss-empty">
            <van-empty description="暂无数据" />
          </div>
          <div v-else class="hlss-list">
            <div
              v-for="item in filteredOptions"
              :key="item.key"
              class="hlss-item"
              :class="{ 'is-disabled': item.disabled }"
              @click="handleItemClick(item)"
            >
              <div
                class="hlss-item__label van-ellipsis"
                :class="{ 'is-selected': isSelected(item.key) }"
              >
                {{ item.label }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<style scoped lang="scss">
.h-list-single-select {
  width: 100%;
}

.hlss-trigger {
  width: 100%;
  height: 32px;
  padding: 0 10px 0 12px;
  border: 1px solid #e5e6eb;
  border-radius: 2px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  user-select: none;

  &.is-disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }

  &.is-placeholder {
    .hlss-trigger__text {
      color: #c9cdd4;
    }
  }
}

.hlss-trigger__text {
  flex: 1;
  min-width: 0;
  font-size: 12px;
  color: #1f2733;
}

.hlss-trigger__icon {
  flex: none;
  color: #86909c;
  font-size: 14px;
}

.hlss-popup {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.hlss-popup__header {
  height: 44px;
  padding: 0 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #f2f3f5;
}

.hlss-popup__title {
  flex: 1;
  min-width: 0;
  text-align: center;
  font-weight: 500;
  font-size: 14px;
  color: #1f2733;
}

.hlss-popup__action {
  flex: none;
  width: 56px;
  font-size: 14px;
  color: #1677ff;
  user-select: none;

  &.is-left {
    text-align: left;
  }
  &.is-right {
    text-align: right;
  }
}

.hlss-popup__content {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.hlss-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.hlss-empty {
  padding: 24px 0;
}

.hlss-item {
  height: 44px;
  padding: 0 12px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #f2f3f5;
  user-select: none;

  &.is-disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.hlss-item__label {
  flex: 1;
  min-width: 0;
  font-weight: 400;
  font-size: 12px;
  color: #1D2129;

  &.is-selected {
    color: #1677ff;
    font-weight: 500;
  }
}
</style>
