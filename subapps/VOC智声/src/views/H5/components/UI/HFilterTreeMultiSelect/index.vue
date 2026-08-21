<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { showToast } from 'vant'

defineOptions({ name: 'HFilterTreeMultiSelect' })

export type HFilterTreeMultiSelectFields = {
  /** 展示字段 */
  label: string
  /** 值字段 */
  value: string
  /** 子节点字段 */
  children: string
  /** 禁用字段 */
  disabled?: string
}

export type HFilterTreeMultiSelectVariant = 'tree' | 'group'

type TreeNode = {
  key: string
  value: string
  label: string
  pathLabel: string
  depth: number
  disabled: boolean
  raw: any
  parentKey: string
  children: TreeNode[]
  leafValues: string[]
  selectableValues: string[]
}

type TreeRow = {
  key: string
  node: TreeNode
}

const props = withDefaults(
  defineProps<{
    /** 树形选项 */
    options?: any[]
    /** 字段映射 */
    fields?: HFilterTreeMultiSelectFields
    /** 展示模式：tree 用于 3 级及以上，group 用于 2 级分组 */
    variant?: HFilterTreeMultiSelectVariant
    /** v-model 是否只输出叶子节点 */
    leafOnly?: boolean
    /** 点击父级时是否联动子级 */
    cascadeCheck?: boolean
    /** 是否显示搜索 */
    searchable?: boolean
    /** 默认展开层级 */
    defaultExpandLevel?: number
    /** 最大可选数量，0 表示不限制 */
    maxSelected?: number
    /** 弹框标题 */
    title?: string
    /** 占位文案 */
    placeholder?: string
    /** 是否禁用 */
    disabled?: boolean
  }>(),
  {
    options: () => [],
    fields: () => ({
      label: 'label',
      value: 'value',
      children: 'children',
      disabled: 'disabled'
    }),
    variant: 'tree',
    leafOnly: true,
    cascadeCheck: true,
    searchable: true,
    defaultExpandLevel: 1,
    maxSelected: 0,
    title: '请选择',
    placeholder: '请选择',
    disabled: false
  }
)

const modelValue = defineModel<string[]>({ default: () => [] })

const popupVisible = ref(false)
const selectedPopupVisible = ref(false)
const keywordInput = ref('')
const keyword = ref('')
const debounceTimer = ref<number | undefined>(undefined)
const draftSelected = ref<string[]>([])
const expandedKeys = ref<Set<string>>(new Set())

const mergedFields = computed(() => ({
  label: props.fields?.label || 'label',
  value: props.fields?.value || 'value',
  children: props.fields?.children || 'children',
  disabled: props.fields?.disabled || 'disabled'
}))

/**
 * 将任意输入规整为不重复的字符串数组。
 * @param value 原始值
 * @returns 去重后的字符串数组
 */
function normalizeStringList(value: unknown) {
  if (!Array.isArray(value)) {
    return []
  }

  const seen = new Set<string>()
  const result: string[] = []
  value.forEach(item => {
    const val = String(item ?? '').trim()
    if (!val || seen.has(val)) {
      return
    }
    seen.add(val)
    result.push(val)
  })
  return result
}

/**
 * 读取节点子级，屏蔽接口返回异常结构。
 * @param node 原始节点
 * @returns 子级列表
 */
function getRawChildren(node: any) {
  const children = node?.[mergedFields.value.children]
  return Array.isArray(children) ? children : []
}

/**
 * 生成当前节点的可选值集合。
 * @param node 当前节点
 * @returns 可选值
 */
function resolveSelectableValues(node: TreeNode) {
  if (props.leafOnly) {
    return node.leafValues
  }

  const values = node.disabled ? [] : [node.value]
  if (props.cascadeCheck) {
    values.push(...node.children.flatMap(child => child.selectableValues))
  }
  return Array.from(new Set(values)).filter(Boolean)
}

/**
 * 标准化树结构，同时预计算路径、叶子值和可选值。
 * @param list 原始树
 * @param parent 上级节点
 * @param parentNames 上级路径
 * @param depth 当前层级
 * @returns 标准化节点
 */
function normalizeTree(
  list: any[],
  parent: TreeNode | null = null,
  parentNames: string[] = [],
  depth = 0
): TreeNode[] {
  return (Array.isArray(list) ? list : [])
    .map((raw, index) => {
      const label = String(raw?.[mergedFields.value.label] ?? '').trim()
      const value = String(raw?.[mergedFields.value.value] ?? '').trim()
      const childrenRaw = getRawChildren(raw)
      const key = value || `${parent?.key || 'root'}-${depth}-${index}`
      const pathNames = label ? [...parentNames, label] : parentNames
      const node: TreeNode = {
        key,
        value,
        label,
        pathLabel: pathNames.join(' / '),
        depth,
        disabled: Boolean(raw?.[mergedFields.value.disabled]),
        raw,
        parentKey: parent?.key || '',
        children: [],
        leafValues: [],
        selectableValues: []
      }

      node.children = normalizeTree(childrenRaw, node, pathNames, depth + 1)
      node.leafValues =
        node.children.length > 0
          ? node.children.flatMap(child => child.leafValues)
          : node.disabled || !node.value
            ? []
            : [node.value]
      node.selectableValues = resolveSelectableValues(node)
      return node
    })
    .filter(node => node.label && node.value)
}

const treeOptions = computed(() => normalizeTree(props.options || []))

const allNodes = computed(() => {
  const nodes: TreeNode[] = []
  const walk = (list: TreeNode[]) => {
    list.forEach(node => {
      nodes.push(node)
      walk(node.children)
    })
  }
  walk(treeOptions.value)
  return nodes
})

const selectableNodes = computed(() => {
  return allNodes.value.filter(node => {
    if (!node.selectableValues.length) {
      return false
    }
    if (props.leafOnly) {
      return node.children.length === 0
    }
    return !node.disabled
  })
})

const valueNodeMap = computed(() => {
  const map = new Map<string, TreeNode>()
  selectableNodes.value.forEach(node => {
    if (!map.has(node.value)) {
      map.set(node.value, node)
    }
  })
  return map
})

const orderedSelectableValues = computed(() => selectableNodes.value.map(node => node.value))

const committedSelected = computed(() => normalizeSelectedValues(modelValue.value))
const selectedCount = computed(() => draftSelected.value.length)
const showSelectedBar = computed(() => selectedCount.value > 0)

const selectedText = computed(() => {
  const selected = committedSelected.value
  if (!selected.length) {
    return props.placeholder
  }

  const firstNode = valueNodeMap.value.get(selected[0])
  const firstLabel = firstNode?.label || selected[0]
  if (selected.length === 1) {
    return firstLabel
  }
  return `${firstLabel} 等${selected.length}项`
})

const selectedItems = computed(() => {
  return draftSelected.value.map(value => {
    const node = valueNodeMap.value.get(value)
    return {
      value,
      label: node?.pathLabel || node?.label || value
    }
  })
})

const searchOptions = computed(() => {
  const list = selectableNodes.value
  const kw = keyword.value.trim().toLowerCase()
  if (!props.searchable || !kw) {
    return []
  }

  return list.filter(node => {
    const content = `${node.pathLabel} ${node.value}`.toLowerCase()
    return content.includes(kw)
  })
})

const visibleRows = computed<TreeRow[]>(() => {
  const rows: TreeRow[] = []
  const walk = (nodes: TreeNode[]) => {
    nodes.forEach(node => {
      rows.push({ key: node.key, node })
      if (expandedKeys.value.has(node.key)) {
        walk(node.children)
      }
    })
  }
  walk(treeOptions.value)
  return rows
})

/**
 * 按树的展示顺序规整选中值。父级值会转换为其下叶子值，保证接口只收到末级值。
 * @param value 原始选中值
 * @returns 稳定排序后的选中值
 */
function normalizeSelectedValues(value: unknown) {
  const inputSet = new Set(normalizeStringList(value))
  if (!inputSet.size) {
    return []
  }

  const expanded = new Set<string>()
  allNodes.value.forEach(node => {
    if (!inputSet.has(node.value)) {
      return
    }
    node.selectableValues.forEach(item => expanded.add(item))
  })

  inputSet.forEach(item => {
    if (valueNodeMap.value.has(item)) {
      expanded.add(item)
    }
  })

  return orderedSelectableValues.value.filter(item => expanded.has(item))
}

/**
 * 判断节点是否已全选。
 * @param node 当前节点
 * @returns 是否全选
 */
function isChecked(node: TreeNode) {
  const values = node.selectableValues
  if (!values.length) {
    return false
  }
  const selected = new Set(draftSelected.value)
  return values.every(value => selected.has(value))
}

/**
 * 判断节点是否处于半选态。
 * @param node 当前节点
 * @returns 是否半选
 */
function isIndeterminate(node: TreeNode) {
  const values = node.selectableValues
  if (!values.length || isChecked(node)) {
    return false
  }
  const selected = new Set(draftSelected.value)
  return values.some(value => selected.has(value))
}

/**
 * 切换展开状态。
 * @param node 当前节点
 */
function toggleExpand(node: TreeNode) {
  if (!node.children.length) {
    return
  }
  const next = new Set(expandedKeys.value)
  if (next.has(node.key)) {
    next.delete(node.key)
  } else {
    next.add(node.key)
  }
  expandedKeys.value = next
}

/**
 * 设置草稿选中值，统一按树顺序排序。
 * @param values 待设置值
 */
function setDraftSelected(values: string[]) {
  const valueSet = new Set(values)
  draftSelected.value = orderedSelectableValues.value.filter(value => valueSet.has(value))
}

/**
 * 切换节点选中状态。
 * @param node 当前节点
 */
function toggleNode(node: TreeNode) {
  const values = node.selectableValues
  if (!values.length) {
    return
  }

  const selected = new Set(draftSelected.value)
  const shouldRemove = values.every(value => selected.has(value))
  if (shouldRemove) {
    values.forEach(value => selected.delete(value))
    setDraftSelected(Array.from(selected))
    return
  }

  const maxSelected = Number(props.maxSelected || 0)
  const nextValues = Array.from(new Set([...draftSelected.value, ...values]))
  if (maxSelected > 0 && nextValues.length > maxSelected) {
    showToast(`最多可选${maxSelected}项`)
    return
  }
  setDraftSelected(nextValues)
}

/**
 * 搜索结果只切换单个末级节点，避免父级批量语义不清晰。
 * @param node 当前搜索结果节点
 */
function toggleSearchNode(node: TreeNode) {
  if (!node.value || node.disabled) {
    return
  }
  const selected = new Set(draftSelected.value)
  if (selected.has(node.value)) {
    selected.delete(node.value)
    setDraftSelected(Array.from(selected))
    return
  }

  const maxSelected = Number(props.maxSelected || 0)
  if (maxSelected > 0 && selected.size >= maxSelected) {
    showToast(`最多可选${maxSelected}项`)
    return
  }
  selected.add(node.value)
  setDraftSelected(Array.from(selected))
}

/**
 * 删除单个已选项。
 * @param value 选中值
 */
function removeSelectedValue(value: string) {
  setDraftSelected(draftSelected.value.filter(item => item !== value))
}

/**
 * 清空已选值。
 */
function handleClearAll() {
  draftSelected.value = []
  selectedPopupVisible.value = false
}

/**
 * 初始化展开状态：优先展开已选项祖先，否则展开默认层级。
 */
function initExpandedKeys() {
  const selected = new Set(draftSelected.value)
  const next = new Set<string>()
  if (selected.size > 0) {
    allNodes.value.forEach(node => {
      if (node.children.length && node.selectableValues.some(value => selected.has(value))) {
        next.add(node.key)
      }
    })
  } else {
    allNodes.value.forEach(node => {
      if (node.children.length && node.depth < props.defaultExpandLevel) {
        next.add(node.key)
      }
    })
  }
  expandedKeys.value = next
}

/**
 * 打开弹层，拷贝外部值为草稿。
 */
async function openPopup() {
  if (props.disabled) {
    return
  }
  draftSelected.value = committedSelected.value.slice()
  keywordInput.value = ''
  keyword.value = ''
  initExpandedKeys()
  popupVisible.value = true
  await nextTick()
}

/**
 * 关闭弹层并清理附属弹框。
 */
function closePopup() {
  popupVisible.value = false
  selectedPopupVisible.value = false
}

/**
 * 取消本次选择。
 */
function handleCancel() {
  closePopup()
}

/**
 * 完成选择并回写外部 v-model。
 */
function handleConfirm() {
  modelValue.value = draftSelected.value.slice()
  closePopup()
}

watch(
  () => keywordInput.value,
  value => {
    if (debounceTimer.value) {
      window.clearTimeout(debounceTimer.value)
    }
    debounceTimer.value = window.setTimeout(() => {
      keyword.value = (value || '').trim()
    }, 200)
  }
)

onBeforeUnmount(() => {
  if (debounceTimer.value) {
    window.clearTimeout(debounceTimer.value)
  }
})
</script>

<template>
  <div class="h-filter-tree-multi-select" :class="`is-${variant}`">
    <div
      class="hftms-trigger"
      :class="{ 'is-disabled': disabled, 'is-placeholder': !committedSelected.length }"
      @click="openPopup"
    >
      <div class="hftms-trigger__text van-ellipsis">{{ selectedText }}</div>
      <van-icon class="hftms-trigger__icon" name="arrow-down" />
    </div>

    <van-popup
      v-model:show="popupVisible"
      position="bottom"
      round
      :safe-area-inset-bottom="true"
      :lock-scroll="true"
      :style="{ height: '84%' }"
      teleport="body"
      @closed="closePopup"
    >
      <div class="hftms-popup">
        <div class="hftms-popup__header">
          <div class="hftms-popup__action is-left" @click="handleCancel">取消</div>
          <div class="hftms-popup__title van-ellipsis">{{ title }}</div>
          <div class="hftms-popup__action is-right" @click="handleConfirm">完成</div>
        </div>

        <div v-if="searchable" class="hftms-popup__search">
          <van-search
            v-model="keywordInput"
            placeholder="搜索"
            clearable
            shape="round"
            :show-action="false"
          />
        </div>

        <div class="hftms-popup__content" :class="{ 'has-selected-bar': showSelectedBar }">
          <template v-if="keyword">
            <div v-if="searchOptions.length === 0" class="hftms-empty">
              <van-empty description="暂无数据" />
            </div>
            <div v-else class="hftms-search-list">
              <div
                v-for="node in searchOptions"
                :key="node.key"
                class="hftms-search-item"
                @click="toggleSearchNode(node)"
              >
                <div class="hftms-search-item__label van-multi-ellipsis--l2">
                  {{ node.pathLabel }}
                </div>
                <span
                  class="hftms-search-item__check"
                  :class="{ 'is-checked': draftSelected.includes(node.value) }"
                >
                  <van-icon v-if="draftSelected.includes(node.value)" name="success" />
                </span>
              </div>
            </div>
          </template>

          <template v-else>
            <div v-if="visibleRows.length === 0" class="hftms-empty">
              <van-empty description="暂无数据" />
            </div>
            <div v-else class="hftms-tree">
              <div
                v-for="row in visibleRows"
                :key="row.key"
                class="hftms-tree-item"
                :class="{
                  'is-root': row.node.depth === 0,
                  'is-disabled': !row.node.selectableValues.length
                }"
                :style="{ paddingLeft: `${12 + row.node.depth * 18}px` }"
              >
                <div class="hftms-tree-item__expand" @click="toggleExpand(row.node)">
                  <van-icon
                    v-if="row.node.children.length"
                    :name="expandedKeys.has(row.node.key) ? 'arrow-down' : 'arrow'"
                  />
                </div>
                <div class="hftms-tree-item__check" @click="toggleNode(row.node)">
                  <span v-if="isChecked(row.node)" class="hftms-check-checked">
                    <van-icon name="success" />
                  </span>
                  <span v-else-if="isIndeterminate(row.node)" class="hftms-check-half" />
                  <span v-else class="hftms-check-empty" />
                </div>
                <div class="hftms-tree-item__label van-ellipsis" @click="toggleNode(row.node)">
                  {{ row.node.label }}
                </div>
              </div>
            </div>
          </template>
        </div>

        <div v-if="showSelectedBar" class="hftms-selected-bar">
          <div class="hftms-selected-bar__left" @click="selectedPopupVisible = true">
            <span class="hftms-selected-bar__label">已选 {{ selectedCount }} 项</span>
            <van-icon class="hftms-selected-bar__arrow" name="arrow-up" />
          </div>
          <div class="hftms-selected-bar__clear" @click="handleClearAll">清空</div>
          <van-button class="hftms-selected-bar__confirm" type="primary" @click="handleConfirm">
            完成
          </van-button>
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
      <div class="hftms-selected-popup">
        <div class="hftms-selected-popup__header">
          <div class="hftms-selected-popup__title">已选（{{ selectedCount }}）</div>
          <div class="hftms-selected-popup__actions">
            <div class="hftms-selected-popup__action" @click="handleClearAll">清空</div>
            <div class="hftms-selected-popup__action" @click="selectedPopupVisible = false">
              关闭
            </div>
          </div>
        </div>
        <div class="hftms-selected-popup__content">
          <div v-if="selectedItems.length === 0" class="hftms-empty">
            <van-empty description="暂无已选" />
          </div>
          <div v-else class="hftms-selected-list">
            <div v-for="item in selectedItems" :key="item.value" class="hftms-selected-item">
              <div class="hftms-selected-item__label van-multi-ellipsis--l2">
                {{ item.label }}
              </div>
              <van-icon
                class="hftms-selected-item__remove"
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
.h-filter-tree-multi-select {
  width: 100%;
}

.hftms-trigger {
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
  }

  &.is-placeholder {
    .hftms-trigger__text {
      color: #c9cdd4;
    }
  }
}

.hftms-trigger__text {
  flex: 1;
  min-width: 0;
  color: #1f2733;
  font-size: 12px;
}

.hftms-trigger__icon {
  flex: none;
  color: #86909c;
  font-size: 14px;
}

.hftms-popup,
.hftms-selected-popup {
  height: 100%;
  background: #fff;
  display: flex;
  flex-direction: column;
}

.hftms-popup {
  position: relative;
}

.hftms-popup__header,
.hftms-selected-popup__header {
  height: 44px;
  padding: 0 12px;
  border-bottom: 1px solid #f2f3f5;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.hftms-popup__title,
.hftms-selected-popup__title {
  flex: 1;
  min-width: 0;
  color: #1f2733;
  font-size: 14px;
  font-weight: 500;
  text-align: center;
}

.hftms-popup__action {
  flex: none;
  width: 56px;
  color: #1677ff;
  font-size: 14px;
  user-select: none;

  &.is-left {
    text-align: left;
  }

  &.is-right {
    text-align: right;
  }
}

.hftms-popup__content {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;

  &.has-selected-bar {
    padding-bottom: calc(64px + env(safe-area-inset-bottom));
  }
}

.hftms-empty {
  padding: 24px 0;
}

.hftms-tree,
.hftms-search-list,
.hftms-selected-list {
  padding: 4px 0;
}

.hftms-tree-item,
.hftms-search-item,
.hftms-selected-item {
  min-height: 44px;
  padding: 0 12px;
  border-bottom: 1px solid #f2f3f5;
  display: flex;
  align-items: center;
  user-select: none;
}

.hftms-tree-item {
  gap: 8px;

  &.is-root {
    background: #fafafb;

    .hftms-tree-item__label {
      color: #1f2733;
      font-weight: 500;
    }
  }

  &.is-disabled {
    .hftms-tree-item__label {
      color: #c9cdd4;
    }
  }
}

.hftms-tree-item__expand {
  flex: none;
  width: 18px;
  height: 44px;
  color: #86909c;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hftms-tree-item__check {
  flex: none;
  width: 22px;
  height: 44px;
  color: #1677ff;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hftms-check-empty,
.hftms-check-half,
.hftms-check-checked,
.hftms-search-item__check {
  width: 16px;
  height: 16px;
  border: 1px solid #c9cdd4;
  border-radius: 2px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.hftms-check-half {
  border-color: #1677ff;

  &::after {
    content: '';
    width: 8px;
    height: 2px;
    border-radius: 1px;
    background: #1677ff;
  }
}

.hftms-check-checked,
.hftms-search-item__check.is-checked {
  border-color: #1677ff;
  background: #1677ff;
  color: #fff;
  font-size: 12px;
}

.hftms-tree-item__label {
  flex: 1;
  min-width: 0;
  color: #1d2129;
  font-size: 13px;
  line-height: 20px;
}

.hftms-search-item {
  gap: 10px;
}

.hftms-search-item__label,
.hftms-selected-item__label {
  flex: 1;
  min-width: 0;
  color: #1d2129;
  font-size: 13px;
  line-height: 18px;
}

.hftms-search-item__check {
  flex: none;
}

.hftms-selected-bar {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 10px 12px calc(10px + env(safe-area-inset-bottom));
  border-top: 1px solid #f2f3f5;
  background: rgba(255, 255, 255, 0.98);
  display: grid;
  grid-template-columns: minmax(0, 1fr) 48px 82px;
  gap: 10px;
  align-items: center;
}

.hftms-selected-bar__left {
  min-width: 0;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.hftms-selected-bar__label {
  color: #1f2733;
  font-size: 14px;
  font-weight: 500;
}

.hftms-selected-bar__arrow {
  flex: none;
  color: #86909c;
  font-size: 14px;
}

.hftms-selected-bar__clear {
  color: #1677ff;
  font-size: 13px;
  text-align: center;
}

.hftms-selected-bar__confirm {
  height: 34px;
  border-radius: 2px;
  font-size: 13px;
}

.hftms-selected-popup__title {
  text-align: left;
}

.hftms-selected-popup__actions {
  flex: none;
  display: inline-flex;
  align-items: center;
  gap: 18px;
}

.hftms-selected-popup__action {
  color: #1677ff;
  font-size: 14px;
}

.hftms-selected-popup__content {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.hftms-selected-item {
  gap: 10px;
}

.hftms-selected-item__remove {
  flex: none;
  padding-left: 8px;
  color: #86909c;
  font-size: 16px;
}
</style>
