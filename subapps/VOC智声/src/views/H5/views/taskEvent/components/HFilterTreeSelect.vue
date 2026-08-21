<script setup lang="ts">
import { computed, ref } from 'vue'

defineOptions({ name: 'HFilterTreeSelect' })

export type HFilterTreeSelectFields = {
  /** 展示字段 */
  label: string
  /** 值字段 */
  value: string
  /** 子节点字段 */
  children?: string
}

type TreeNode = Record<string, any>

type TreeRow = {
  raw: TreeNode
  value: string
  label: string
  level: number
  hasChildren: boolean
  leafValues: string[]
  selectable: boolean
}

const props = withDefaults(
  defineProps<{
    /** 树形选项 */
    options?: TreeNode[]
    /** 字段映射 */
    fields?: HFilterTreeSelectFields
    /** 弹层标题 */
    title?: string
    /** 触发器占位 */
    placeholder?: string
    /** 是否禁用 */
    disabled?: boolean
    /** 是否加载中 */
    loading?: boolean
    /** 是否只允许选择末级节点 */
    leafOnly?: boolean
  }>(),
  {
    options: () => [],
    fields: () => ({
      label: 'label',
      value: 'value',
      children: 'children'
    }),
    title: '请选择',
    placeholder: '请选择',
    disabled: false,
    loading: false,
    leafOnly: false
  }
)

/**
 * v-model：已选节点 id 数组。
 * 父子节点不做强联动；leafOnly 开启时仅末级节点会写入同一个数组。
 */
const modelValue = defineModel<string[]>({ default: () => [] })

const popupVisible = ref(false)
const selectedPopupVisible = ref(false)
const draftSelected = ref<string[]>([])
const expandedValues = ref<Set<string>>(new Set())

const mergedFields = computed(() => ({
  label: props.fields?.label || 'label',
  value: props.fields?.value || 'value',
  children: props.fields?.children || 'children'
}))

const getChildren = (node: TreeNode) => {
  const children = node?.[mergedFields.value.children]
  return Array.isArray(children) ? children : []
}

const getNodeValue = (node: TreeNode) => String(node?.[mergedFields.value.value] ?? '')

const getNodeLabel = (node: TreeNode) => String(node?.[mergedFields.value.label] ?? '')

/**
 * 收集指定节点下所有末级节点值。
 * @param node 树节点
 * @returns 当前节点子树内的末级节点值
 */
const getLeafValues = (node: TreeNode): string[] => {
  const value = getNodeValue(node)
  const children = getChildren(node)
  if (children.length === 0) return value ? [value] : []

  return children.flatMap(child => getLeafValues(child))
}

const nodeLeafValueMap = computed(() => {
  const map = new Map<string, string[]>()

  const walk = (list: TreeNode[] = []) => {
    list.forEach(node => {
      const value = getNodeValue(node)
      const children = getChildren(node)
      if (value) {
        map.set(value, getLeafValues(node))
      }
      if (children.length > 0) walk(children)
    })
  }

  walk(props.options || [])
  return map
})

const leafValueSet = computed(() => {
  const set = new Set<string>()
  nodeLeafValueMap.value.forEach(values => {
    values.forEach(value => set.add(value))
  })
  return set
})

/**
 * 过滤可提交的选中值。
 * leafOnly 开启时，父级节点会归一化为其子树下全部末级节点。
 * @param values 组件内部或外部传入的选中值
 * @returns 去空、去重后的可选节点值
 */
const filterSelectableValues = (values: string[]) => {
  const raw = Array.isArray(values) ? values.map(item => String(item || '')).filter(Boolean) : []
  const unique = Array.from(new Set(raw))
  if (!props.leafOnly) return unique

  const leafValues = unique.flatMap(value => nodeLeafValueMap.value.get(value) || [])
  return Array.from(new Set(leafValues))
}

const committedSelected = computed(() => filterSelectableValues(modelValue.value))

const labelMap = computed(() => {
  const map = new Map<string, string>()
  const walk = (list: TreeNode[] = []) => {
    list.forEach(node => {
      const value = getNodeValue(node)
      const label = getNodeLabel(node)
      if (value && label && !map.has(value)) map.set(value, label)
      const children = getChildren(node)
      if (children.length > 0) walk(children)
    })
  }

  walk(props.options || [])
  return map
})

const selectedText = computed(() => {
  const selected = committedSelected.value
  if (!selected.length) return props.placeholder

  const firstLabel = labelMap.value.get(selected[0]) || selected[0]
  if (selected.length === 1) return firstLabel
  return `${firstLabel} 等${selected.length}项`
})

const draftSelectableSelected = computed(() => filterSelectableValues(draftSelected.value))

const selectedCount = computed(() => draftSelectableSelected.value.length)
const showSelectedBar = computed(() => selectedCount.value > 0)

const selectedItems = computed(() =>
  draftSelectableSelected.value.map(value => ({
    value,
    label: labelMap.value.get(value) || value
  }))
)

const buildVisibleRows = (list: TreeNode[] = [], level = 0): TreeRow[] => {
  const rows: TreeRow[] = []
  list.forEach(node => {
    const value = getNodeValue(node)
    const label = getNodeLabel(node)
    if (!value || !label) return

    const children = getChildren(node)
    const leafValues = getLeafValues(node)
    rows.push({
      raw: node,
      value,
      label,
      level,
      hasChildren: children.length > 0,
      leafValues,
      selectable: !props.leafOnly || leafValues.length > 0
    })

    if (children.length > 0 && expandedValues.value.has(value)) {
      rows.push(...buildVisibleRows(children, level + 1))
    }
  })

  return rows
}

const visibleRows = computed(() => buildVisibleRows(props.options || []))

const rootValues = computed(() => {
  return (props.options || []).map(item => getNodeValue(item)).filter(Boolean)
})

const buildAncestorValueSet = (selectedValues: string[]) => {
  const selected = new Set(selectedValues)
  const ancestors = new Set<string>()

  const walk = (list: TreeNode[] = [], parentValues: string[] = []) => {
    list.forEach(node => {
      const value = getNodeValue(node)
      const nextParents = value ? [...parentValues, value] : parentValues
      if (value && selected.has(value)) {
        parentValues.forEach(parent => ancestors.add(parent))
      }
      const children = getChildren(node)
      if (children.length > 0) walk(children, nextParents)
    })
  }

  walk(props.options || [])
  return ancestors
}

const openPopup = () => {
  if (props.disabled) return

  draftSelected.value = committedSelected.value.slice()
  const nextExpanded = buildAncestorValueSet(draftSelected.value)
  if (nextExpanded.size === 0) {
    rootValues.value.forEach(value => nextExpanded.add(value))
  }
  expandedValues.value = nextExpanded
  popupVisible.value = true
}

const closePopup = () => {
  popupVisible.value = false
  selectedPopupVisible.value = false
}

const handleCancel = () => {
  closePopup()
}

const handleConfirm = () => {
  modelValue.value = filterSelectableValues(draftSelected.value)
  closePopup()
}

const toggleExpand = (value: string) => {
  const next = new Set(expandedValues.value)
  if (next.has(value)) {
    next.delete(value)
  } else {
    next.add(value)
  }
  expandedValues.value = next
}

const toggleValue = (value: string) => {
  if (props.leafOnly && !leafValueSet.value.has(value)) return

  const current = draftSelected.value
  if (current.includes(value)) {
    draftSelected.value = current.filter(item => item !== value)
  } else {
    draftSelected.value = [...current, value]
  }
}

const toggleLeafValues = (values: string[]) => {
  if (!values.length) return

  const valueSet = new Set(values)
  const current = draftSelected.value.filter(value => leafValueSet.value.has(value))
  const currentSet = new Set(current)
  const allChecked = values.every(value => currentSet.has(value))

  if (allChecked) {
    draftSelected.value = current.filter(value => !valueSet.has(value))
  } else {
    draftSelected.value = Array.from(new Set([...current, ...values]))
  }
}

const getRowSelectionState = (row: TreeRow) => {
  if (!props.leafOnly) {
    const checked = draftSelected.value.includes(row.value)
    return { checked, indeterminate: false }
  }

  const values = row.leafValues
  if (!values.length) return { checked: false, indeterminate: false }

  const selectedSet = new Set(filterSelectableValues(draftSelected.value))
  const selectedCount = values.filter(value => selectedSet.has(value)).length
  return {
    checked: selectedCount === values.length,
    indeterminate: selectedCount > 0 && selectedCount < values.length
  }
}

const handleRowCheck = (row: TreeRow) => {
  if (!row.selectable) {
    if (row.hasChildren) toggleExpand(row.value)
    return
  }

  if (props.leafOnly) {
    toggleLeafValues(row.leafValues)
    return
  }

  toggleValue(row.value)
}

const handleClearAll = () => {
  if (!draftSelected.value.length) return
  draftSelected.value = []
  selectedPopupVisible.value = false
}

const removeSelectedValue = (value: string) => {
  if (!draftSelected.value.includes(value)) return
  draftSelected.value = draftSelected.value.filter(item => item !== value)
}
</script>

<template>
  <div class="h-filter-tree-select">
    <div
      class="hfts-trigger"
      :class="{ 'is-disabled': disabled, 'is-placeholder': !committedSelected.length }"
      @click="openPopup"
    >
      <div class="hfts-trigger__text van-ellipsis">{{ selectedText }}</div>
      <van-icon class="hfts-trigger__icon" name="arrow-down" />
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
      <div class="hfts-popup">
        <div class="hfts-popup__header">
          <div class="hfts-popup__action is-left" @click="handleCancel">取消</div>
          <div class="hfts-popup__title van-ellipsis">{{ title }}</div>
          <div class="hfts-popup__action is-right" @click="handleConfirm">完成</div>
        </div>

        <div class="hfts-popup__content" :class="{ 'has-selected-bar': showSelectedBar }">
          <div v-if="loading" class="hfts-loading">
            <van-loading size="24px" color="#1677FF">加载中...</van-loading>
          </div>
          <div v-else-if="visibleRows.length === 0" class="hfts-empty">
            <van-empty description="暂无数据" />
          </div>
          <div v-else class="hfts-list">
            <div
              v-for="row in visibleRows"
              :key="row.value"
              class="hfts-row"
              :style="{ paddingLeft: `${12 + row.level * 18}px` }"
            >
              <div
                class="hfts-row__expand"
                @click.stop="row.hasChildren && toggleExpand(row.value)"
              >
                <van-icon
                  v-if="row.hasChildren"
                  :name="expandedValues.has(row.value) ? 'arrow-down' : 'arrow'"
                />
              </div>
              <div
                class="hfts-row__check"
                :class="{ 'is-branch': !row.selectable }"
                @click="handleRowCheck(row)"
              >
                <span
                  v-if="row.selectable"
                  class="hfts-checkbox"
                  :class="{
                    'is-checked': getRowSelectionState(row).checked,
                    'is-indeterminate': getRowSelectionState(row).indeterminate
                  }"
                >
                  <van-icon v-if="getRowSelectionState(row).checked" name="success" />
                  <span
                    v-else-if="getRowSelectionState(row).indeterminate"
                    class="hfts-checkbox__line"
                  ></span>
                </span>
                <span v-else class="hfts-checkbox-spacer"></span>
                <span class="hfts-row__label van-ellipsis">{{ row.label }}</span>
              </div>
            </div>
          </div>
        </div>

        <div v-if="showSelectedBar" class="hfts-selected-bar">
          <div class="hfts-selected-bar__left" @click="selectedPopupVisible = true">
            <span class="hfts-selected-bar__label">已选：{{ selectedCount }}项</span>
            <van-icon class="hfts-selected-bar__arrow" name="arrow-up" />
          </div>
          <div class="hfts-selected-bar__clear" @click="handleClearAll">清空</div>
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
      <div class="hfts-selected-popup">
        <div class="hfts-selected-popup__header">
          <div class="hfts-selected-popup__title">已选（{{ selectedCount }}）</div>
          <div class="hfts-selected-popup__actions">
            <div class="hfts-selected-popup__action" @click="handleClearAll">清空</div>
            <div class="hfts-selected-popup__action" @click="selectedPopupVisible = false">
              关闭
            </div>
          </div>
        </div>

        <div class="hfts-selected-popup__content">
          <div v-if="selectedItems.length === 0" class="hfts-selected-popup__empty">
            <van-empty description="暂无已选" />
          </div>
          <div v-else class="hfts-selected-popup__list">
            <div v-for="item in selectedItems" :key="item.value" class="hfts-selected-item">
              <div class="hfts-selected-item__label van-ellipsis">{{ item.label }}</div>
              <van-icon
                class="hfts-selected-item__remove"
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
.h-filter-tree-select {
  width: 100%;
}

.hfts-trigger {
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
    .hfts-trigger__text {
      color: #c9cdd4;
    }
  }
}

.hfts-trigger__text {
  flex: 1;
  min-width: 0;
  font-size: 12px;
  color: #1f2733;
}

.hfts-trigger__icon {
  flex: none;
  color: #86909c;
  font-size: 14px;
}

.hfts-popup {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
  position: relative;
}

.hfts-popup__header {
  height: 44px;
  padding: 0 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #f2f3f5;
}

.hfts-popup__title {
  flex: 1;
  min-width: 0;
  text-align: center;
  font-weight: 500;
  font-size: 14px;
  color: #1f2733;
}

.hfts-popup__action {
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

.hfts-popup__content {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;

  &.has-selected-bar {
    padding-bottom: calc(56px + env(safe-area-inset-bottom));
  }
}

.hfts-loading,
.hfts-empty {
  min-height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hfts-list {
  padding: 4px 0;
}

.hfts-row {
  height: 44px;
  padding-right: 12px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #f2f3f5;
  user-select: none;
}

.hfts-row__expand {
  flex: none;
  width: 24px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #86909c;
  font-size: 14px;
}

.hfts-row__check {
  flex: 1;
  min-width: 0;
  height: 44px;
  display: flex;
  align-items: center;
  gap: 8px;

  &.is-branch {
    .hfts-row__label {
      color: #1d2129;
    }
  }
}

.hfts-checkbox {
  flex: none;
  width: 16px;
  height: 16px;
  border: 1px solid #c9cdd4;
  border-radius: 2px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 12px;

  &.is-checked {
    border-color: #1677ff;
    background: #1677ff;
  }

  &.is-indeterminate {
    border-color: #1677ff;
  }
}

.hfts-checkbox__line {
  width: 8px;
  height: 2px;
  border-radius: 1px;
  background: #1677ff;
}

.hfts-checkbox-spacer {
  flex: none;
  width: 16px;
  height: 16px;
}

.hfts-row__label {
  flex: 1;
  min-width: 0;
  font-weight: 400;
  font-size: 12px;
  color: #1d2129;
}

.hfts-selected-bar {
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

.hfts-selected-bar__left {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #1f2733;
}

.hfts-selected-bar__label {
  font-weight: 500;
  font-size: 14px;
  color: #1f2733;
}

.hfts-selected-bar__arrow {
  font-size: 14px;
  color: #86909c;
}

.hfts-selected-bar__clear {
  font-size: 13px;
  color: #1677ff;
}

.hfts-selected-popup {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.hfts-selected-popup__header {
  height: 44px;
  padding: 0 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #f2f3f5;
}

.hfts-selected-popup__title {
  font-size: 14px;
  font-weight: 500;
  color: #1f2733;
}

.hfts-selected-popup__actions {
  display: inline-flex;
  align-items: center;
  gap: 16px;
}

.hfts-selected-popup__action {
  font-size: 14px;
  color: #1677ff;
}

.hfts-selected-popup__content {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.hfts-selected-popup__empty {
  padding: 24px 0;
}

.hfts-selected-popup__list {
  padding: 4px 0;
}

.hfts-selected-item {
  height: 44px;
  padding: 0 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #f2f3f5;
}

.hfts-selected-item__label {
  flex: 1;
  min-width: 0;
  font-weight: 400;
  font-size: 12px;
  color: #1d2129;
}

.hfts-selected-item__remove {
  flex: none;
  padding-left: 12px;
  color: #86909c;
  font-size: 16px;
}
</style>
