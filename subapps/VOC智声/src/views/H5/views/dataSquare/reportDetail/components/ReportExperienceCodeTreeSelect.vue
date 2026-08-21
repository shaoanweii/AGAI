<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import type { H5DataSquareLabelTag } from '@h5/api/dataSquare'
import { normalizeExperienceCodeValue, normalizeSameLevelExperienceCodeValue } from '../utils'

defineOptions({
  name: 'ReportExperienceCodeTreeSelect'
})

type ExperienceTreeNode = {
  key: string
  value: string
  label: string
  path: string[]
  pathKey: string
  pathLabel: string
  depth: number
  disabled: boolean
  parentPathKey: string
  children: ExperienceTreeNode[]
  descendantPathKeys: string[]
}

type TreeRow = {
  key: string
  node: ExperienceTreeNode
}

const props = withDefaults(
  defineProps<{
    options?: H5DataSquareLabelTag[]
    loading?: boolean
    disabled?: boolean
    title?: string
    placeholder?: string
  }>(),
  {
    options: () => [],
    loading: false,
    disabled: false,
    title: '体验代码',
    placeholder: '请选择'
  }
)

const modelValue = defineModel<string[][]>({ default: () => [] })

const popupVisible = ref(false)
const selectedPopupVisible = ref(false)
const keywordInput = ref('')
const keyword = ref('')
const debounceTimer = ref<number | undefined>(undefined)
const expandedKeys = ref<Set<string>>(new Set())
const draftSelected = ref<string[][]>([])

/**
 * 使用 JSON 序列化路径作为 key，避免 code 中出现分隔符时冲突。
 * @param path 体验代码路径
 * @returns 路径 key
 */
function getPathKey(path: string[]) {
  return JSON.stringify(path)
}

/**
 * 读取节点子级，兜底接口返回空值。
 * @param node 标签节点
 * @returns 子级列表
 */
function getChildren(node: H5DataSquareLabelTag) {
  return Array.isArray(node?.child) ? node.child : []
}

/**
 * 标准化体验代码树，预计算路径、展示文案和子孙路径。
 * @param list 原始标签树
 * @param parent 上级节点
 * @param parentLabels 上级名称路径
 * @param depth 当前层级
 * @returns 标准化树
 */
function normalizeTree(
  list: H5DataSquareLabelTag[] = [],
  parent: ExperienceTreeNode | null = null,
  parentLabels: string[] = [],
  depth = 0
): ExperienceTreeNode[] {
  return (Array.isArray(list) ? list : [])
    .map((raw, index) => {
      const value = String(raw?.tagCode || '').trim()
      const label = String(raw?.tagName || '').trim()
      const path = [...(parent?.path || []), value].filter(Boolean)
      const pathLabels = label ? [...parentLabels, label] : parentLabels
      const pathKey = getPathKey(path)
      const node: ExperienceTreeNode = {
        key: pathKey || `${parent?.key || 'root'}-${depth}-${index}`,
        value,
        label,
        path,
        pathKey,
        pathLabel: pathLabels.join(' / '),
        depth,
        disabled: false,
        parentPathKey: parent?.pathKey || '',
        children: [],
        descendantPathKeys: []
      }

      node.children = normalizeTree(getChildren(raw), node, pathLabels, depth + 1)
      node.descendantPathKeys = node.children.flatMap(child => [
        child.pathKey,
        ...child.descendantPathKeys
      ])
      return node
    })
    .filter(node => node.value && node.label && node.path.length > 0)
}

const treeOptions = computed(() => normalizeTree(props.options || []))

const allNodes = computed(() => {
  const result: ExperienceTreeNode[] = []
  const walk = (nodes: ExperienceTreeNode[]) => {
    nodes.forEach(node => {
      result.push(node)
      walk(node.children)
    })
  }
  walk(treeOptions.value)
  return result
})

const nodeMap = computed(() => {
  const map = new Map<string, ExperienceTreeNode>()
  allNodes.value.forEach(node => {
    map.set(node.pathKey, node)
  })
  return map
})

const orderedPathKeys = computed(() => allNodes.value.map(node => node.pathKey))

const selectedPaths = computed(() => normalizeExperienceCodeValue(modelValue.value))
const selectedCount = computed(() => selectedPaths.value.length)
const draftSelectedCount = computed(() => draftSelected.value.length)
const draftSelectedKeySet = computed(() => {
  return new Set(normalizeExperienceCodeValue(draftSelected.value).map(path => getPathKey(path)))
})
const showSelectedBar = computed(() => draftSelectedCount.value > 0)

const triggerText = computed(() => {
  if (props.loading) {
    return '加载中...'
  }
  if (!selectedCount.value) {
    return props.placeholder
  }
  return `已选 ${selectedCount.value} 项`
})

const selectedItems = computed(() => {
  return normalizeExperienceCodeValue(draftSelected.value).map(path => {
    const pathKey = getPathKey(path)
    const node = nodeMap.value.get(pathKey)
    return {
      path,
      pathKey,
      label: node?.pathLabel || path.join(' / ')
    }
  })
})

const searchOptions = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) {
    return []
  }

  return allNodes.value.filter(node => {
    const content = `${node.pathLabel} ${node.value}`.toLowerCase()
    return content.includes(kw)
  })
})

const visibleRows = computed<TreeRow[]>(() => {
  const rows: TreeRow[] = []
  const walk = (nodes: ExperienceTreeNode[]) => {
    nodes.forEach(node => {
      rows.push({ key: node.pathKey, node })
      if (expandedKeys.value.has(node.pathKey)) {
        walk(node.children)
      }
    })
  }
  walk(treeOptions.value)
  return rows
})

/**
 * 按树展示顺序排序路径，未知路径保留在末尾，避免回显值丢失。
 * @param paths 路径数组
 * @returns 排序后的路径
 */
function sortSelectedPaths(paths: string[][]) {
  const normalized = normalizeExperienceCodeValue(paths)
  const pathMap = new Map(normalized.map(path => [getPathKey(path), path]))
  const sorted = orderedPathKeys.value
    .map(pathKey => pathMap.get(pathKey))
    .filter((path): path is string[] => Array.isArray(path))
  const knownSet = new Set(sorted.map(path => getPathKey(path)))
  const unknown = normalized.filter(path => !knownSet.has(getPathKey(path)))
  return [...sorted, ...unknown]
}

/**
 * 判断当前路径是否已选。
 * @param node 当前节点
 * @returns 是否选中
 */
function isChecked(node: ExperienceTreeNode) {
  return draftSelectedKeySet.value.has(node.pathKey)
}

/**
 * 判断当前节点是否半选。半选仅提示子孙中存在已选项，不代表当前节点已选。
 * @param node 当前节点
 * @returns 是否半选
 */
function isIndeterminate(node: ExperienceTreeNode) {
  if (isChecked(node)) {
    return false
  }
  return node.descendantPathKeys.some(pathKey => draftSelectedKeySet.value.has(pathKey))
}

/**
 * 切换展开状态。
 * @param node 当前节点
 */
function toggleExpand(node: ExperienceTreeNode) {
  if (!node.children.length) {
    return
  }
  const next = new Set(expandedKeys.value)
  if (next.has(node.pathKey)) {
    next.delete(node.pathKey)
  } else {
    next.add(node.pathKey)
  }
  expandedKeys.value = next
}

/**
 * 设置草稿选中路径，统一去重和排序。
 * @param paths 待设置路径
 * @param previousPaths 变更前路径，用于识别本次操作层级
 */
function setDraftSelected(paths: string[][], previousPaths: string[][] = draftSelected.value) {
  draftSelected.value = sortSelectedPaths(
    normalizeSameLevelExperienceCodeValue(paths, previousPaths)
  )
}

/**
 * 切换节点自身选中状态。父子互不强制联动，但同一批选择只保留同一层级。
 * @param node 当前节点
 */
function toggleNode(node: ExperienceTreeNode) {
  if (node.disabled || !node.path.length) {
    return
  }
  const selectedKeySet = new Set(draftSelectedKeySet.value)
  if (selectedKeySet.has(node.pathKey)) {
    setDraftSelected(draftSelected.value.filter(path => getPathKey(path) !== node.pathKey))
    return
  }
  setDraftSelected([...draftSelected.value, node.path], draftSelected.value)
}

/**
 * 删除已选路径。
 * @param pathKey 路径 key
 */
function removeSelectedPath(pathKey: string) {
  setDraftSelected(draftSelected.value.filter(path => getPathKey(path) !== pathKey))
}

/**
 * 清空已选路径。
 */
function handleClearAll() {
  draftSelected.value = []
  selectedPopupVisible.value = false
}

/**
 * 初始化展开状态，有默认值时展开已选祖先，否则展开第一个一级节点。
 */
function initExpandedKeys() {
  const next = new Set<string>()
  const selectedKeySet = new Set(draftSelected.value.map(path => getPathKey(path)))

  if (selectedKeySet.size > 0) {
    allNodes.value.forEach(node => {
      if (node.children.length && node.descendantPathKeys.some(key => selectedKeySet.has(key))) {
        next.add(node.pathKey)
      }
    })
  } else {
    const firstRoot = treeOptions.value.find(node => node.children.length > 0)
    if (firstRoot) {
      next.add(firstRoot.pathKey)
    }
  }

  expandedKeys.value = next
}

/**
 * 打开选择弹层。
 */
async function openPopup() {
  if (props.disabled || props.loading) {
    return
  }
  draftSelected.value = sortSelectedPaths(modelValue.value)
  keywordInput.value = ''
  keyword.value = ''
  initExpandedKeys()
  popupVisible.value = true
  await nextTick()
}

/**
 * 关闭选择弹层。
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
 * 确认选择并回写外部模型。
 */
function handleConfirm() {
  modelValue.value = normalizeExperienceCodeValue(draftSelected.value)
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
  <div class="report-experience-code-tree-select">
    <div
      class="rects-trigger"
      :class="{
        'is-disabled': disabled || loading,
        'is-placeholder': !selectedCount
      }"
      @click="openPopup"
    >
      <div class="rects-trigger__text van-ellipsis">{{ triggerText }}</div>
      <van-icon class="rects-trigger__icon" name="arrow-down" />
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
      <div class="rects-popup">
        <div class="rects-popup__header">
          <div class="rects-popup__action is-left" @click="handleCancel">取消</div>
          <div class="rects-popup__title van-ellipsis">{{ title }}</div>
          <div class="rects-popup__action is-right" @click="handleConfirm">完成</div>
        </div>

        <div class="rects-popup__search">
          <van-search
            v-model="keywordInput"
            placeholder="搜索体验代码"
            clearable
            shape="round"
            :show-action="false"
          />
        </div>

        <div class="rects-popup__content" :class="{ 'has-selected-bar': showSelectedBar }">
          <template v-if="keyword">
            <div v-if="searchOptions.length === 0" class="rects-empty">
              <van-empty description="暂无数据" />
            </div>
            <div v-else class="rects-search-list">
              <div
                v-for="node in searchOptions"
                :key="node.pathKey"
                class="rects-search-item"
                @click="toggleNode(node)"
              >
                <div class="rects-search-item__label van-multi-ellipsis--l2">
                  {{ node.pathLabel }}
                </div>
                <span
                  class="rects-check"
                  :class="{ 'is-checked': draftSelectedKeySet.has(node.pathKey) }"
                >
                  <van-icon v-if="draftSelectedKeySet.has(node.pathKey)" name="success" />
                </span>
              </div>
            </div>
          </template>

          <template v-else>
            <div v-if="visibleRows.length === 0" class="rects-empty">
              <van-empty description="暂无数据" />
            </div>
            <div v-else class="rects-tree">
              <div
                v-for="row in visibleRows"
                :key="row.key"
                class="rects-tree-item"
                :class="{ 'is-root': row.node.depth === 0 }"
                :style="{ paddingLeft: `${12 + row.node.depth * 18}px` }"
              >
                <div class="rects-tree-item__expand" @click="toggleExpand(row.node)">
                  <van-icon
                    v-if="row.node.children.length"
                    :name="expandedKeys.has(row.node.pathKey) ? 'arrow-down' : 'arrow'"
                  />
                </div>
                <div class="rects-tree-item__check" @click="toggleNode(row.node)">
                  <span v-if="isChecked(row.node)" class="rects-check is-checked">
                    <van-icon name="success" />
                  </span>
                  <span v-else-if="isIndeterminate(row.node)" class="rects-check is-half" />
                  <span v-else class="rects-check" />
                </div>
                <div class="rects-tree-item__label van-ellipsis" @click="toggleNode(row.node)">
                  {{ row.node.label }}
                </div>
              </div>
            </div>
          </template>
        </div>

        <div v-if="showSelectedBar" class="rects-selected-bar">
          <div class="rects-selected-bar__left" @click="selectedPopupVisible = true">
            <span class="rects-selected-bar__label">已选 {{ draftSelectedCount }} 项</span>
            <van-icon class="rects-selected-bar__arrow" name="arrow-up" />
          </div>
          <div class="rects-selected-bar__clear" @click="handleClearAll">清空</div>
          <van-button class="rects-selected-bar__confirm" type="primary" @click="handleConfirm">
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
      <div class="rects-selected-popup">
        <div class="rects-selected-popup__header">
          <div class="rects-selected-popup__title">已选（{{ draftSelectedCount }}）</div>
          <div class="rects-selected-popup__actions">
            <div class="rects-selected-popup__action" @click="handleClearAll">清空</div>
            <div class="rects-selected-popup__action" @click="selectedPopupVisible = false">
              关闭
            </div>
          </div>
        </div>
        <div class="rects-selected-popup__content">
          <div v-if="selectedItems.length === 0" class="rects-empty">
            <van-empty description="暂无已选" />
          </div>
          <div v-else class="rects-selected-list">
            <div v-for="item in selectedItems" :key="item.pathKey" class="rects-selected-item">
              <div class="rects-selected-item__label van-multi-ellipsis--l2">
                {{ item.label }}
              </div>
              <van-icon
                class="rects-selected-item__remove"
                name="cross"
                @click="removeSelectedPath(item.pathKey)"
              />
            </div>
          </div>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<style scoped lang="scss">
.report-experience-code-tree-select {
  width: 100%;
}

.rects-trigger {
  width: 100%;
  height: 28px;
  padding: 0 10px;
  border: 1px solid #e5e6eb;
  border-radius: 3px;
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
    .rects-trigger__text {
      color: #c9cdd4;
    }
  }
}

.rects-trigger__text {
  flex: 1;
  min-width: 0;
  color: #1f2733;
  font-size: 12px;
}

.rects-trigger__icon {
  flex: none;
  color: #86909c;
  font-size: 14px;
}

.rects-popup,
.rects-selected-popup {
  height: 100%;
  background: #fff;
  display: flex;
  flex-direction: column;
}

.rects-popup {
  position: relative;
}

.rects-popup__header,
.rects-selected-popup__header {
  height: 44px;
  padding: 0 12px;
  border-bottom: 1px solid #f2f3f5;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.rects-popup__title,
.rects-selected-popup__title {
  flex: 1;
  min-width: 0;
  color: #1f2733;
  font-size: 14px;
  font-weight: 500;
  text-align: center;
}

.rects-popup__action {
  flex: none;
  width: 56px;
  color: #1677ff;
  font-size: 14px;

  &.is-left {
    text-align: left;
  }

  &.is-right {
    text-align: right;
  }
}

.rects-popup__content {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;

  &.has-selected-bar {
    padding-bottom: calc(64px + env(safe-area-inset-bottom));
  }
}

.rects-empty {
  padding: 24px 0;
}

.rects-tree,
.rects-search-list,
.rects-selected-list {
  padding: 4px 0;
}

.rects-tree-item,
.rects-search-item,
.rects-selected-item {
  min-height: 44px;
  padding: 0 12px;
  border-bottom: 1px solid #f2f3f5;
  display: flex;
  align-items: center;
  user-select: none;
}

.rects-tree-item {
  gap: 8px;

  &.is-root {
    background: #fafafb;

    .rects-tree-item__label {
      color: #1f2733;
      font-weight: 500;
    }
  }
}

.rects-tree-item__expand {
  flex: none;
  width: 18px;
  height: 44px;
  color: #86909c;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.rects-tree-item__check {
  flex: none;
  width: 22px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.rects-check {
  flex: none;
  width: 16px;
  height: 16px;
  border: 1px solid #c9cdd4;
  border-radius: 2px;
  display: inline-flex;
  align-items: center;
  justify-content: center;

  &.is-checked {
    border-color: #1677ff;
    background: #1677ff;
    color: #fff;
    font-size: 12px;
  }

  &.is-half {
    border-color: #1677ff;

    &::after {
      content: '';
      width: 8px;
      height: 2px;
      border-radius: 1px;
      background: #1677ff;
    }
  }
}

.rects-tree-item__label,
.rects-search-item__label,
.rects-selected-item__label {
  flex: 1;
  min-width: 0;
  color: #1d2129;
  font-size: 13px;
  line-height: 18px;
}

.rects-search-item,
.rects-selected-item {
  gap: 10px;
}

.rects-selected-bar {
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

.rects-selected-bar__left {
  min-width: 0;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.rects-selected-bar__label {
  color: #1f2733;
  font-size: 14px;
  font-weight: 500;
}

.rects-selected-bar__arrow {
  flex: none;
  color: #86909c;
  font-size: 14px;
}

.rects-selected-bar__clear {
  color: #1677ff;
  font-size: 13px;
  text-align: center;
}

.rects-selected-bar__confirm {
  height: 34px;
  border-radius: 2px;
  font-size: 13px;
}

.rects-selected-popup__title {
  text-align: left;
}

.rects-selected-popup__actions {
  flex: none;
  display: inline-flex;
  align-items: center;
  gap: 18px;
}

.rects-selected-popup__action {
  color: #1677ff;
  font-size: 14px;
}

.rects-selected-popup__content {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.rects-selected-item__remove {
  flex: none;
  padding-left: 8px;
  color: #86909c;
  font-size: 16px;
}
</style>
