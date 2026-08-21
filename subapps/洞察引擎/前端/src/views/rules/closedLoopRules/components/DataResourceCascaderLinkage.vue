<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'

// 中文注释：两级级联选择器（左侧一级单选，右侧二级多选），支持联动与回显
// 输出约定：
// - 全选（父级选中/子集全选）=> { '1': [父级id], type: 父级type }
// - 部分选中（子集部分）    => { '2': [子级id...], type: 父级type }
type CascaderLinkageValue = { '1'?: string[]; '2'?: string[]; type?: string } | null

const model = defineModel<CascaderLinkageValue>()

const props = withDefaults(
  defineProps<{
    options: any[]
    placeholder?: string
    disabled?: boolean
    loading?: boolean
    // 中文注释：仅当父级类型为 account 时，展示文案前缀（例如 @）
    prefix?: string
  }>(),
  {
    options: () => [],
    placeholder: '请选择',
    disabled: false,
    loading: false,
    prefix: ''
  }
)

const hub = reactive({
  open: false,
  // 左侧激活态（仅影响右侧展示）
  activeParentId: '' as string,
  // 左侧已选态（仅允许单选）
  selectedParentId: '' as string,
  // 用于处理“父级无子集时仍能选中”的特殊场景
  forceParentChecked: false,
  // 右侧搜索关键字
  keyword: ''
})

// 中文注释：子集选中集合（使用 reactive(Set) 支持 O(1) 增删，避免每次勾选拷贝集合）
const selectedChildSet = reactive(new Set<string>())

// 搜索关键字防抖
const keywordDebounced = ref('')
let keywordTimer: any = null
watch(
  () => hub.keyword,
  v => {
    if (keywordTimer) clearTimeout(keywordTimer)
    keywordTimer = setTimeout(() => (keywordDebounced.value = String(v || '')), 300)
  },
  { flush: 'post' }
)

const parents = computed<any[]>(() => (Array.isArray(props.options) ? props.options : []))

const parentMap = computed<Record<string, any>>(() => {
  const map: Record<string, any> = {}
  parents.value.forEach(p => {
    if (p && p.id != null) map[String(p.id)] = p
  })
  return map
})

// 二级 id 索引：用于通过子级 id 反推父级/名称
const childIndex = computed<Record<string, { id: string; name: string; parentId: string }>>(() => {
  const idx: Record<string, { id: string; name: string; parentId: string }> = {}
  parents.value.forEach(p => {
    const pid = String(p?.id ?? '')
    const list = Array.isArray(p?.keywordList) ? p.keywordList : []
    list.forEach((c: any) => {
      const cid = String(c?.id ?? '')
      if (!cid) return
      idx[cid] = { id: cid, name: String(c?.name ?? ''), parentId: pid }
    })
  })
  return idx
})

const activeParent = computed<any | null>(() => parentMap.value[String(hub.activeParentId)] || null)
const activeChildrenAll = computed<any[]>(() => {
  const list = activeParent.value?.keywordList
  return Array.isArray(list) ? list : []
})

const filteredChildren = computed<any[]>(() => {
  const list = activeChildrenAll.value
  const q = String(keywordDebounced.value || '')
    .trim()
    .toLowerCase()
  if (!q) return list
  return list.filter((c: any) =>
    String(c?.name || '')
      .toLowerCase()
      .includes(q)
  )
})

const getChildrenOfParent = (pid: string) => {
  const parent = parentMap.value[String(pid)]
  const list = parent?.keywordList
  return Array.isArray(list) ? list : []
}

const getSelectedChildIdsFast = () => Array.from(selectedChildSet)

const isParentChecked = (pid: string) => {
  if (String(pid) !== String(hub.selectedParentId)) return false
  const total = getChildrenOfParent(pid).length
  const selectedCount = selectedChildSet.size
  if (total === 0) return hub.forceParentChecked
  return selectedCount > 0 && selectedCount === total
}

const isParentIndeterminate = (pid: string) => {
  if (String(pid) !== String(hub.selectedParentId)) return false
  const total = getChildrenOfParent(pid).length
  if (total <= 0) return false
  const selectedCount = selectedChildSet.size
  return selectedCount > 0 && selectedCount < total
}

const clearSelection = () => {
  hub.selectedParentId = ''
  selectedChildSet.clear()
  hub.forceParentChecked = false
}

// 同步 v-model：按规则输出 { '1': [...], type } / { '2': [...], type } / null
const isInternalUpdate = ref(false)
const emitModel = () => {
  const pid = String(hub.selectedParentId || '')
  if (!pid) {
    isInternalUpdate.value = true
    model.value = null
    queueMicrotask(() => (isInternalUpdate.value = false))
    return
  }

  const parent = parentMap.value[pid]
  const parentType = parent?.type != null ? String(parent.type) : ''

  const next: any = { type: parentType }
  const total = getChildrenOfParent(pid).length
  const selectedCount = selectedChildSet.size

  // 未选择任何子集且未勾选父级时，视为未选择
  if (total > 0 && selectedCount === 0) {
    clearSelection()
    isInternalUpdate.value = true
    model.value = null
    queueMicrotask(() => (isInternalUpdate.value = false))
    return
  }
  if (total === 0 && !hub.forceParentChecked) {
    clearSelection()
    isInternalUpdate.value = true
    model.value = null
    queueMicrotask(() => (isInternalUpdate.value = false))
    return
  }

  const isAllSelected = total === 0 ? hub.forceParentChecked : selectedCount === total

  if (isAllSelected) {
    next['1'] = [pid]
  } else {
    next['2'] = getSelectedChildIdsFast()
  }

  isInternalUpdate.value = true
  model.value = next
  queueMicrotask(() => (isInternalUpdate.value = false))
}

// 左侧：点击行仅切换激活态
const onParentActive = (pid: string) => {
  hub.activeParentId = String(pid || '')
  hub.keyword = ''
}

// 左侧：父级勾选/取消（单选，勾选即全选子集）
const onParentCheckChange = (pid: string, checked: boolean) => {
  if (props.disabled) return
  const nextPid = String(pid || '')
  if (!nextPid) return

  hub.activeParentId = nextPid
  hub.keyword = ''

  if (!checked) {
    // 取消选中：清空子集
    if (String(hub.selectedParentId) === nextPid) {
      clearSelection()
      emitModel()
    }
    return
  }

  // 勾选：仅允许选择一个父级，切换时清空旧选择
  if (hub.selectedParentId && String(hub.selectedParentId) !== nextPid) {
    clearSelection()
  }

  hub.selectedParentId = nextPid
  const children = getChildrenOfParent(nextPid)
  selectedChildSet.clear()
  children.forEach((c: any) => {
    const cid = String(c?.id ?? '')
    if (cid) selectedChildSet.add(cid)
  })
  hub.forceParentChecked = children.length === 0
  emitModel()
}

// 右侧：勾选/取消子集（多选），并同步左侧半选/全选状态
const toggleChild = (cid: string) => {
  if (props.disabled) return
  const activePid = String(hub.activeParentId || '')
  if (!activePid) return

  // 中文注释：右侧选择发生在某父级下时，父级单选需要切换到该父级
  if (hub.selectedParentId && String(hub.selectedParentId) !== activePid) {
    clearSelection()
  }
  if (!hub.selectedParentId) hub.selectedParentId = activePid

  hub.forceParentChecked = false

  const id = String(cid || '')
  if (!id) return
  if (selectedChildSet.has(id)) selectedChildSet.delete(id)
  else selectedChildSet.add(id)

  // 中文注释：全部取消时，自动清空父级已选态
  if (selectedChildSet.size === 0) {
    clearSelection()
  }
  emitModel()
}

// 回显：根据 v-model 初始化内部状态
const initFromModel = () => {
  const v: any = model.value
  clearSelection()
  hub.keyword = ''

  // 中文注释：数组/空值都视为未选择
  if (!v || typeof v !== 'object' || Array.isArray(v)) {
    // 兜底：未选择时默认激活第一项，交互更接近标准 cascader
    if (!hub.activeParentId && parents.value.length)
      hub.activeParentId = String(parents.value[0].id)
    return
  }

  const level1 = Array.isArray(v['1']) ? v['1'] : []
  const level2 = Array.isArray(v['2']) ? v['2'] : []

  let pid = level1.length ? String(level1[0] ?? '') : ''
  if (pid && !parentMap.value[pid]) pid = ''

  // 若只有二级数据，按首个子级反推父级
  if (!pid && level2.length) {
    const firstChildId = String(level2[0] ?? '')
    pid = childIndex.value[firstChildId]?.parentId || ''
  }

  // 无法识别父级时，保持未选择
  if (!pid) {
    if (!hub.activeParentId && parents.value.length)
      hub.activeParentId = String(parents.value[0].id)
    return
  }

  hub.selectedParentId = pid
  hub.activeParentId = pid

  // 父级全选：直接全选子集
  if (level1.length) {
    const children = getChildrenOfParent(pid)
    selectedChildSet.clear()
    children.forEach((c: any) => {
      const cid = String(c?.id ?? '')
      if (cid) selectedChildSet.add(cid)
    })
    hub.forceParentChecked = children.length === 0
    return
  }

  // 子集部分选中
  const childrenSet = new Set(getChildrenOfParent(pid).map((c: any) => String(c?.id ?? '')))
  selectedChildSet.clear()
  level2.forEach((id: any) => {
    const cid = String(id ?? '')
    if (cid && childrenSet.has(cid)) selectedChildSet.add(cid)
  })

  // 中文注释：若回显数据为“子集全选但仍存放在 2 级”，则自动规范化为父级全选结构
  const total = childrenSet.size
  const selectedCount = selectedChildSet.size
  if (total > 0 && selectedCount === total) {
    emitModel()
  }
}

watch(
  () => props.options,
  () => initFromModel(),
  { immediate: true }
)

watch(
  () => model.value,
  () => {
    if (isInternalUpdate.value) return
    initFromModel()
  },
  { deep: true }
)

// 打开弹层时：若未激活任何父级，默认激活“已选父级”或第一项
const onOpen = () => {
  if (hub.activeParentId) return
  if (hub.selectedParentId) hub.activeParentId = String(hub.selectedParentId)
  else if (parents.value.length) hub.activeParentId = String(parents.value[0].id)
}

// 右侧虚拟列表：只渲染可视区域，提升勾选/切换性能（不分页，仍是全量数据）
const RIGHT_HEIGHT = 320
const ITEM_HEIGHT = 34
const OVERSCAN = 10
const rightScrollRef = ref<HTMLDivElement | null>(null)
const rightScrollTop = ref(0)

const onRightScroll = (e: Event) => {
  rightScrollTop.value = (e.target as HTMLDivElement)?.scrollTop || 0
}

const totalRightCount = computed(() => filteredChildren.value.length)
const startIndex = computed(() =>
  Math.max(0, Math.floor(rightScrollTop.value / ITEM_HEIGHT) - OVERSCAN)
)
const visibleCount = computed(() => Math.ceil(RIGHT_HEIGHT / ITEM_HEIGHT) + OVERSCAN * 2)
const endIndex = computed(() =>
  Math.min(totalRightCount.value, startIndex.value + visibleCount.value)
)

const visibleChildren = computed(() =>
  filteredChildren.value.slice(startIndex.value, endIndex.value)
)
const topPadding = computed(() => startIndex.value * ITEM_HEIGHT)
const bottomPadding = computed(() => (totalRightCount.value - endIndex.value) * ITEM_HEIGHT)

const resetRightScroll = () => {
  rightScrollTop.value = 0
  if (rightScrollRef.value) rightScrollRef.value.scrollTop = 0
}

watch(
  () => hub.activeParentId,
  () => resetRightScroll()
)
watch(
  () => keywordDebounced.value,
  () => resetRightScroll()
)

// 展示文案（按A优化）：父级全选时优先展示全部子集，超过阈值则回退为“xxx +N”
const INLINE_FULL_MAX_ITEMS = 1
const INLINE_FULL_MAX_CHARS = 80

const displayText = computed(() => {
  const pid = String(hub.selectedParentId || '')
  if (!pid) return ''

  const parentType = parentMap.value[pid]?.type != null ? String(parentMap.value[pid].type) : ''
  const needPrefix = parentType === 'account' && !!props.prefix

  // 全选（父级选中）时：若子集有数据，优先展示全部子集；超过阈值时回退为“xxx +N”
  if (isParentChecked(pid) || hub.forceParentChecked) {
    const children = getChildrenOfParent(pid)
    if (children.length > 0) {
      const firstName = String(children[0]?.name ?? '')
      const firstLabel = needPrefix ? `${props.prefix}${firstName}` : firstName

      // 子集数量超过阈值：不在输入框内拼接全量字符串，避免卡顿
      if (children.length > INLINE_FULL_MAX_ITEMS) {
        if (!firstLabel) return String(parentMap.value[pid]?.name ?? '')
        return children.length > 1 ? `${firstLabel},+${children.length - 1}` : firstLabel
      }

      // 数量不大：可直接拼接全量文案；若长度过长则回退为“xxx +N”
      const labels = children
        .map((c: any) => String(c?.name ?? ''))
        .filter(Boolean)
        .map(name => (needPrefix ? `${props.prefix}${name}` : name))
      const fullText = labels.join(',')
      if (fullText.length <= INLINE_FULL_MAX_CHARS) return fullText
      const first = labels[0] || ''
      if (!first) return String(parentMap.value[pid]?.name ?? '')
      return labels.length > 1 ? `${first},+${labels.length - 1}` : first
    }
    return String(parentMap.value[pid]?.name ?? '')
  }

  const selectedCount = selectedChildSet.size
  if (!selectedCount) return ''

  const firstId = selectedChildSet.values().next().value as string | undefined
  const firstName = firstId ? childIndex.value[firstId]?.name || '' : ''
  const firstLabel = needPrefix ? `${props.prefix}${firstName}` : firstName
  const fallback = String(parentMap.value[pid]?.name ?? '')
  if (!firstLabel) return fallback
  if (selectedCount > 1) return `${firstLabel},+${selectedCount - 1}`
  return firstLabel
})
</script>

<template>
  <el-popover
    v-model:visible="hub.open"
    placement="bottom-start"
    :width="520"
    trigger="click"
    :hide-after="0"
    :show-arrow="false"
    :disabled="props.disabled"
    popper-class="dr-linkage-popper"
    @show="onOpen"
  >
    <template #reference>
      <el-input
        :model-value="displayText"
        :placeholder="props.placeholder"
        readonly
        :disabled="props.disabled"
      />
    </template>

    <div class="dr-linkage" v-loading="props.loading">
      <div v-if="!props.loading && !parents.length" class="dr-empty">
        <el-empty description="暂无数据" />
      </div>

      <template v-else>
        <div class="dr-pane dr-pane--left">
          <el-scrollbar height="320px">
            <div
              v-for="p in parents"
              :key="p.id"
              class="dr-parent-item"
              :class="{
                active: String(hub.activeParentId) === String(p.id),
                selected:
                  String(hub.selectedParentId) === String(p.id) &&
                  (isParentChecked(String(p.id)) || isParentIndeterminate(String(p.id)))
              }"
              @click="onParentActive(String(p.id))"
            >
              <el-checkbox
                :model-value="isParentChecked(String(p.id))"
                :indeterminate="isParentIndeterminate(String(p.id))"
                :disabled="props.disabled"
                @change="(val: any) => onParentCheckChange(String(p.id), !!val)"
                @click.stop
              />
              <span class="name ml-10" :title="p.name">{{ p.name }}</span>
            </div>
          </el-scrollbar>
        </div>

        <div class="dr-pane dr-pane--right">
          <div class="dr-search">
            <el-input
              v-model="hub.keyword"
              placeholder="请输入二级名称"
              clearable
              size="small"
              :disabled="props.disabled"
            />
          </div>

          <div class="dr-right-scroll" ref="rightScrollRef" @scroll="onRightScroll">
            <div class="dr-list">
              <template v-if="activeChildrenAll.length">
                <template v-if="filteredChildren.length">
                  <div :style="{ height: `${topPadding}px` }" />
                  <div
                    v-for="item in visibleChildren"
                    :key="item.id"
                    class="dr-child-item"
                    @click="toggleChild(String(item.id))"
                    v-memo="[item.id, selectedChildSet.has(String(item.id)), item.name]"
                  >
                    <el-checkbox
                      :model-value="selectedChildSet.has(String(item.id))"
                      :disabled="props.disabled"
                      @click.stop="toggleChild(String(item.id))"
                    />
                    <el-text class="w-230" truncated>{{ item.name }}</el-text>
                  </div>
                  <div :style="{ height: `${bottomPadding}px` }" />
                </template>
                <div v-else class="dr-empty-text">
                  <el-text type="info">暂无匹配结果，</el-text>
                </div>
              </template>
              <div v-else class="dr-empty-text">
                <el-text type="info">暂无数据</el-text>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>
  </el-popover>
</template>

<style scoped lang="scss">
.dr-linkage {
  display: flex;
  width: 100%;
}
.dr-pane {
  padding: 8px;
}
.dr-pane--left {
  width: 190px;
  padding-left: 0;
  border-right: 1px solid var(--el-border-color-light);
}
.dr-pane--right {
  flex: 1;
}
.dr-search {
  padding-bottom: 8px;
}

.dr-parent-item {
  height: 32px;
  display: flex;
  align-items: center;
  padding: 0 8px;
  border-radius: 4px;
  cursor: pointer;
}
.dr-parent-item:hover {
  background: var(--el-fill-color-light);
}
.dr-parent-item.active {
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}
.dr-parent-item.selected .name {
  font-weight: 600;
}
.dr-parent-item .name {
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dr-list {
  padding-right: 6px;
}
.dr-right-scroll {
  height: 320px;
  overflow-y: auto;
  overscroll-behavior: contain;
}
.dr-child-item {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 34px;
  padding: 0 8px;
  border-radius: 4px;
  cursor: pointer;
}
.dr-child-item:hover {
  background: var(--el-fill-color-light);
}
.w-230 {
  width: 230px;
}
.dr-empty-text {
  height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-text-color-secondary);
}

:deep(.dr-linkage-popper) {
  padding: 0;
}
</style>
