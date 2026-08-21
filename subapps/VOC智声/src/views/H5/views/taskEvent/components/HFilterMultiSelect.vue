<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { showToast } from 'vant'

defineOptions({ name: 'HFilterMultiSelect' })

export type HFilterMultiSelectFields = {
  /** 展示字段 */
  label: string
  /** 值字段 */
  value: string
  /** 原始数据字段（可选） */
  rawData?: string
}

// 选项置顶模式
export type HFilterMultiSelectPinMode = 'none' | 'selected' | 'recent' | 'selected+recent'

export type HFilterMultiSelectOption = {
  rawData?: any
  value: string
  label: string
}

type InnerOption = HFilterMultiSelectOption & {
  raw?: any
}

type VirtualRow =
  | { kind: 'header'; key: string; text: string }
  | { kind: 'option'; key: string; option: InnerOption }

const props = withDefaults(
  defineProps<{
    /** 选项列表：数据量可能很大（数万级） */
    options?: any[]
    /** 字段映射：适配不同接口返回结构 */
    fields?: HFilterMultiSelectFields
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
    /** 置顶策略（仅在未输入关键词时生效） */
    pinMode?: HFilterMultiSelectPinMode
    /** 最近选择保留条数（pinMode 包含 recent 时生效） */
    recentLimit?: number
    /** 最大可选数量（0/undefined 表示不限制） */
    maxSelected?: number
    /** 弹框标题 */
    title?: string
    /** 输入框占位文本 */
    placeholder?: string
    /** 是否禁用 */
    disabled?: boolean
    /** 是否支持搜索 */
    searchable?: boolean
    /** 单行高度（用于虚拟列表计算，需与样式保持一致） */
    itemHeight?: number
  }>(),
  {
    options: () => [],
    fields: () => ({
      label: 'label',
      value: 'value',
      rawData: 'rawData'
    }),
    searchFields: () => [],
    searchBy: undefined,
    pinMode: 'none',
    recentLimit: 20,
    maxSelected: 0,
    title: '请选择',
    placeholder: '请选择',
    disabled: false,
    searchable: true,
    itemHeight: 44
  }
)

/**
 * v-model：多选值数组
 * - 空数组代表未选择
 */
const modelValue = defineModel<string[]>({ default: () => [] })

const popupVisible = ref(false)
const selectedPopupVisible = ref(false)
const keywordInput = ref('')
const keyword = ref('')
const debounceTimer = ref<number | undefined>(undefined)

const draftSelected = ref<string[]>([])
const committedSelected = computed<string[]>(() => (Array.isArray(modelValue.value) ? modelValue.value : []))
const recentValues = ref<string[]>([])

// 规范化 options（过滤无效项）
const normalizedOptions = computed<InnerOption[]>(() => {
  const list = Array.isArray(props.options) ? props.options : []
  const labelKey = props.fields?.label || 'label'
  const valueKey = props.fields?.value || 'value'
  const rawDataKey = props.fields?.rawData || 'rawData'
  return list
    .map(item => ({
      raw: item,
      rawData: item?.[rawDataKey],
      value: String(item?.[valueKey] ?? ''),
      label: String(item?.[labelKey] ?? '')
    }))
    .filter(item => item.value !== '' && item.label !== '')
})

const labelMap = computed(() => {
  const map = new Map<string, string>()
  normalizedOptions.value.forEach(item => {
    if (!map.has(item.value)) map.set(item.value, item.label)
  })
  return map
})

const selectedText = computed(() => {
  const selected = committedSelected.value
  if (!selected.length) return props.placeholder

  const firstLabel = labelMap.value.get(selected[0]) || selected[0]
  if (selected.length === 1) return firstLabel
  return `${firstLabel} 等${selected.length}项`
})

const selectedCount = computed(() => draftSelected.value.length)
const showSelectedBar = computed(() => selectedCount.value > 0)
const selectedItems = computed(() => {
  return draftSelected.value.map(value => ({
    value,
    label: labelMap.value.get(value) || value
  }))
})

// 搜索（做轻量防抖，避免大列表频繁过滤）
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
    if (fields.length > 0) {
      const parts = fields.map(key => {
        try {
          return String(item.raw?.[key] ?? '')
        } catch {
          return ''
        }
      })
      return parts.join(' ').toLowerCase().includes(lower)
    }

    // 3) 默认：label/value
    const label = item.label || ''
    const value = item.value || ''
    return label.toLowerCase().includes(lower) || value.toLowerCase().includes(lower)
  })
})

const buildPinnedRows = (baseList: InnerOption[]) => {
  const rows: VirtualRow[] = []
  // “已选”展示改为底部弹框形式：这里不再生成“已选”分组，仅保留 recent 置顶能力
  const pinMode: HFilterMultiSelectPinMode =
    props.pinMode === 'selected' ? 'none' : props.pinMode === 'selected+recent' ? 'recent' : props.pinMode
  if (pinMode === 'none') {
    return baseList.map(item => ({ kind: 'option' as const, key: item.value, option: item }))
  }
  if (keyword.value) {
    return baseList.map(item => ({ kind: 'option' as const, key: item.value, option: item }))
  }

  const recentLimit = Math.max(0, Number(props.recentLimit || 0))
  const recentList = recentLimit > 0 ? recentValues.value.slice(0, recentLimit) : []
  const recentSet = new Set(recentList)

  const recentOptions: InnerOption[] = []

  baseList.forEach(item => {
    const val = item.value
    const isRecent = recentSet.has(val)

    if (pinMode === 'recent') {
      if (isRecent) {
        recentOptions.push(item)
        return
      }
    }
  })

  if (pinMode === 'recent') {
    if (recentOptions.length > 0) {
      rows.push({
        kind: 'header' as const,
        key: '__header_recent__',
        text: `最近选择（${recentOptions.length}）`
      })
      recentOptions.forEach(item =>
        rows.push({ kind: 'option' as const, key: `__recent__${item.value}`, option: item })
      )
    }
  }

  // “其他选项”分组展示全部数据，不排除已选/最近（作为快捷区的补充入口）
  if (rows.length > 0) {
    rows.push({ kind: 'header' as const, key: '__header_all__', text: '其他选项' })
  }
  baseList.forEach(item => rows.push({ kind: 'option' as const, key: `__all__${item.value}`, option: item }))

  return rows
}

const virtualRows = computed<VirtualRow[]>(() => buildPinnedRows(filteredOptions.value))

// 虚拟列表
const viewportRef = ref<HTMLElement | null>(null)
const scrollTop = ref(0)
const viewportHeight = ref(0)
const resizeObserver = ref<ResizeObserver | null>(null)
const overscan = 6

const totalHeight = computed(() => virtualRows.value.length * props.itemHeight)
const startIndex = computed(() => {
  const raw = Math.floor(scrollTop.value / props.itemHeight) - overscan
  return Math.max(0, raw)
})
const visibleCount = computed(() => {
  if (!viewportHeight.value) return 20
  return Math.ceil(viewportHeight.value / props.itemHeight) + overscan * 2
})
const endIndex = computed(() => Math.min(virtualRows.value.length, startIndex.value + visibleCount.value))
const offsetY = computed(() => startIndex.value * props.itemHeight)
const visibleRows = computed(() => virtualRows.value.slice(startIndex.value, endIndex.value))

const measureViewport = () => {
  const el = viewportRef.value
  if (!el) return
  viewportHeight.value = el.clientHeight || 0
}

const handleScroll = (e: Event) => {
  const target = e.target as HTMLElement
  scrollTop.value = target?.scrollTop || 0
}

const openPopup = async () => {
  if (props.disabled) return
  popupVisible.value = true
  draftSelected.value = committedSelected.value.slice()
  if (!recentValues.value.length && committedSelected.value.length) {
    recentValues.value = committedSelected.value.slice()
  }
  keywordInput.value = ''
  keyword.value = ''
  scrollTop.value = 0

  await nextTick()
  measureViewport()

  if (viewportRef.value) {
    viewportRef.value.scrollTop = 0
  }

  if (typeof ResizeObserver !== 'undefined' && viewportRef.value) {
    resizeObserver.value?.disconnect()
    resizeObserver.value = new ResizeObserver(() => measureViewport())
    resizeObserver.value.observe(viewportRef.value)
  }
}

const closePopup = () => {
  popupVisible.value = false
  selectedPopupVisible.value = false
  resizeObserver.value?.disconnect()
  resizeObserver.value = null
}

const handleCancel = () => {
  closePopup()
}

const handleConfirm = () => {
  modelValue.value = draftSelected.value.slice()
  closePopup()
}

const handleClearAll = () => {
  if (!draftSelected.value.length) return
  draftSelected.value = []
  selectedPopupVisible.value = false
}

const removeSelectedValue = (val: string) => {
  if (!draftSelected.value.includes(val)) return
  draftSelected.value = draftSelected.value.filter(v => v !== val)
}

const toggleValue = (val: string) => {
  const current = draftSelected.value
  if (current.includes(val)) {
    draftSelected.value = current.filter(v => v !== val)
  } else {
    const maxSelected = Number(props.maxSelected || 0)
    if (maxSelected > 0 && current.length >= maxSelected) {
      showToast(`最多可选${maxSelected}项`)
      return
    }
    draftSelected.value = [...current, val]
  }

  // 记录最近选择（保留交互历史，便于快速回选）
  const next = [val, ...recentValues.value.filter(v => v !== val)]
  const limit = Math.max(0, Number(props.recentLimit || 0))
  recentValues.value = limit > 0 ? next.slice(0, limit) : next
}

const isChecked = (val: string) => draftSelected.value.includes(val)

onBeforeUnmount(() => {
  resizeObserver.value?.disconnect()
  resizeObserver.value = null
  if (debounceTimer.value) window.clearTimeout(debounceTimer.value)
})
</script>

<template>
  <div class="h-filter-multi-select">
    <div
      class="hfms-trigger"
      :class="{ 'is-disabled': disabled, 'is-placeholder': !committedSelected.length }"
      @click="openPopup"
    >
      <div class="hfms-trigger__text van-ellipsis">{{ selectedText }}</div>
      <van-icon class="hfms-trigger__icon" name="arrow-down" />
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
      <div class="hfms-popup">
        <div class="hfms-popup__header">
          <div class="hfms-popup__action is-left" @click="handleCancel">取消</div>
          <div class="hfms-popup__title van-ellipsis">{{ title }}</div>
          <div class="hfms-popup__action is-right" @click="handleConfirm">完成</div>
        </div>

        <div v-if="searchable" class="hfms-popup__search">
          <van-search
            v-model="keywordInput"
            placeholder="搜索"
            clearable
            shape="round"
            :show-action="false"
          />
        </div>

        <div class="hfms-popup__content" :class="{ 'has-selected-bar': showSelectedBar }">
          <div ref="viewportRef" class="hfms-list" @scroll="handleScroll">
            <div v-if="filteredOptions.length === 0" class="hfms-empty">
              <van-empty description="暂无数据" />
            </div>
            <div v-else class="hfms-virtual" :style="{ height: `${totalHeight}px` }">
              <div class="hfms-virtual__inner" :style="{ transform: `translateY(${offsetY}px)` }">
                <div
                  v-for="row in visibleRows"
                  :key="row.key"
                  class="hfms-item"
                  :class="{ 'is-header': row.kind === 'header' }"
                  @click="row.kind === 'option' ? toggleValue(row.option.value) : undefined"
                >
                  <div class="hfms-item__label van-ellipsis">
                    {{ row.kind === 'header' ? row.text : row.option.label }}
                  </div>
                  <van-icon
                    v-if="row.kind === 'option' && isChecked(row.option.value)"
                    name="success"
                    class="hfms-item__icon"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="showSelectedBar" class="hfms-selected-bar">
          <div class="hfms-selected-bar__left" @click="selectedPopupVisible = true">
              <span class="hfms-selected-bar__label">已选： {{selectedCount}}项</span>
              <van-icon class="hfms-selected-bar__arrow ml-5" name="arrow-up" />
            </div>
            <div
              class="hfms-selected-bar__clear"
              :class="{ 'is-disabled': selectedCount === 0 }"
              @click="handleClearAll"
            >
              清空
            </div>
        </div>
      </div>
    </van-popup>

    <van-popup
      v-model:show="selectedPopupVisible"
      position="bottom"
      round
      :safe-area-inset-bottom="true"
      :lock-scroll="true"
      :style="{ height: '60%' }"
      teleport="body"
      @closed="selectedPopupVisible = false"
    >
      <div class="hfms-selected-popup">
        <div class="hfms-selected-popup__header">
          <div class="hfms-selected-popup__title">已选（{{ selectedCount }}）</div>
          <div class="hfms-selected-popup__actions">
            <div
              class="hfms-selected-popup__action mr-20"
              :class="{ 'is-disabled': selectedCount === 0 }"
              @click="handleClearAll"
            >
              清空
            </div>
            <div class="hfms-selected-popup__action" @click="selectedPopupVisible = false">关闭</div>
          </div>
        </div>

        <div class="hfms-selected-popup__content">
          <div v-if="selectedItems.length === 0" class="hfms-selected-popup__empty">
            <van-empty description="暂无已选" />
          </div>
          <div v-else class="hfms-selected-popup__list">
            <div
              v-for="item in selectedItems"
              :key="item.value"
              class="hfms-selected-item"
            >
              <div class="hfms-selected-item__label van-ellipsis">{{ item.label }}</div>
              <van-icon
                class="hfms-selected-item__remove"
                name="cross"
                @click="removeSelectedValue(item.value)"
              />
            </div>
          </div>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<style scoped lang="scss">
.h-filter-multi-select {
  width: 100%;
}

.hfms-trigger {
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
    .hfms-trigger__text {
      color: #c9cdd4;
    }
  }
}

.hfms-trigger__text {
  flex: 1;
  min-width: 0;
  font-size: 12px;
  color: #1f2733;
}

.hfms-trigger__icon {
  flex: none;
  color: #86909c;
  font-size: 14px;
}

.hfms-popup {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
  position: relative;
}

.hfms-popup__header {
  height: 44px;
  padding: 0 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #f2f3f5;
}

.hfms-popup__title {
  flex: 1;
  min-width: 0;
  text-align: center;
  font-weight: 500;
  font-size: 14px;
  color: #1f2733;
}

.hfms-popup__action {
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

.hfms-popup__search {
  // padding: 8px 12px 0;
}

.hfms-popup__content {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;

  &.has-selected-bar {
    padding-bottom: calc(56px + env(safe-area-inset-bottom));
  }
}

.hfms-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.hfms-empty {
  padding: 24px 0;
}

.hfms-virtual {
  position: relative;
  width: 100%;
}

.hfms-virtual__inner {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
}

.hfms-item {
  height: 44px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #f2f3f5;
  user-select: none;

  &.is-header {
    background: #fafafb;
    .hfms-item__label {
      font-size: 12px;
      color: #86909c;
      font-weight: 500;
    }
  }
}

.hfms-item__label {
  flex: 1;
  min-width: 0;
  font-weight: 400;
  font-size: 12px;
  color: #1D2129;
}

.hfms-item__icon {
  flex: none;
  font-size: 16px;
  color: #1677ff;
}

.hfms-selected-bar {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 16px 12px calc(16px + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.98);
  border-top: 1px solid #f2f3f5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  user-select: none;
}

.hfms-selected-bar__left {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #1f2733;
}

.hfms-selected-bar__label {
  font-weight: 500;
  font-size: 14px;
  color: #1F2733;
}

.hfms-selected-bar__count {
  min-width: 18px;
  height: 18px;
  padding: 0 6px;
  border-radius: 9px;
  background: #1677ff;
  color: #fff;
  font-size: 12px;
  line-height: 18px;
  text-align: center;
}

.hfms-selected-bar__arrow {
  font-size: 14px;
  color: #86909c;
}

.hfms-selected-bar__clear {
  font-size: 13px;
  color: #1677ff;
  user-select: none;

  &.is-disabled {
    color: #c9cdd4;
  }
}

.hfms-selected-popup {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.hfms-selected-popup__header {
  height: 44px;
  padding: 0 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #f2f3f5;
}

.hfms-selected-popup__title {
  font-size: 14px;
  font-weight: 500;
  color: #1f2733;
}

.hfms-selected-popup__actions {
  display: inline-flex;
  align-items: center;
  gap: 16px;
}

.hfms-selected-popup__action {
  font-size: 14px;
  color: #1677ff;
  user-select: none;

  &.is-disabled {
    color: #c9cdd4;
  }
}

.hfms-selected-popup__content {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.hfms-selected-popup__empty {
  padding: 24px 0;
}

.hfms-selected-popup__list {
  padding: 4px 0;
}

.hfms-selected-item {
  height: 44px;
  padding: 0 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #f2f3f5;
}

.hfms-selected-item__label {
  flex: 1;
  min-width: 0;
  font-weight: 400;
  font-size: 12px;
  color: #1D2129;
}

.hfms-selected-item__remove {
  flex: none;
  font-size: 16px;
  color: #86909c;
  padding-left: 12px;
}
</style>
