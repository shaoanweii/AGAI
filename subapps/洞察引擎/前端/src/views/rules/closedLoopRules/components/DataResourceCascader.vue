<script setup lang="ts">
// 中文注释：数据资源级联选择器，左侧一级单选，右侧二级多选
// 说明：左侧列表一次性渲染，右侧通过前端分页 + 滚动加载控制渲染数量，避免大数据量卡顿
import { computed, onMounted, reactive, ref, watch, watchEffect } from 'vue'
import { rulesStore } from '../store'

// v-model 约定：{ '1': ['父级id'], '2': ['子级id1', '子级id2'] }
// 新约定：仅支持对象结构，一级和二级都是 string[] 类型
type DataResourceValue = Record<string, string[]> | null
const model = defineModel<DataResourceValue>()

// 选择器 props
const props = withDefaults(
  defineProps<{ options: any; placeholder?: string; disabled?: boolean; prefix?: string }>(),
  {
    options: [] as any[],
    placeholder: '请选择',
    disabled: false,
    prefix: ''
  }
)

// 组件内部状态
const hub = reactive({
  open: false,
  activeParentId: '' as string,
  // 二级选中集合，key 为子级 id
  selectedChildIds: {} as Record<string, true | undefined>,
  // 右侧搜索关键字
  keyword: ''
})

// 搜索关键字防抖
const keywordDebounced = ref('')
let keywordTimer: any = null
watch(
  () => hub.keyword,
  v => {
    if (keywordTimer) clearTimeout(keywordTimer)
    keywordTimer = setTimeout(() => (keywordDebounced.value = v), 200)
  },
  { flush: 'post' }
)

// 一级数据列表
const parents = computed<any[]>(() => props.options || [])

// 一级 id -> 节点映射
const parentMap = computed<Record<string, any>>(() => {
  const map: Record<string, any> = {}
  ;(parents.value || []).forEach(p => (map[p.id] = p))
  return map
})

// 二级 id 索引：便于通过子级 id 反查名称与父级
const childIndex = computed<Record<string, { id: string; name: string; parentId: string }>>(() => {
  const idx: Record<string, { id: string; name: string; parentId: string }> = {}
  ;(parents.value || []).forEach(p => {
    const list = Array.isArray(p.keywordList) ? p.keywordList : []
    list.forEach((c: any) => (idx[c.id] = { id: c.id, name: c.name, parentId: p.id }))
  })
  return idx
})

// 当前激活的一级节点及其下所有子级
const activeParent = computed<any | null>(() => parentMap.value[hub.activeParentId] || null)
const allChildrenOfActive = computed<any[]>(() => {
  const list = activeParent.value?.keywordList
  return Array.isArray(list) ? list : []
})

// 搜索过滤后的子级列表
const filteredChildren = computed<any[]>(() => {
  const q = (keywordDebounced.value || '').trim().toLowerCase()
  const list = allChildrenOfActive.value
  if (!q) return list
  return list.filter((c: any) => {
    const nl = (c._nl ||= String(c.name || '').toLowerCase())
    return nl.includes(q)
  })
})

// 右侧列表分页状态
const paging = reactive({
  page: 1,
  pageSize: 100, // 每页渲染的子项数量
  loading: false,
  finished: false
})

// 右侧列表容器，用于重置滚动条
const rightListRef = ref<HTMLDivElement | null>(null)

// 当前可见的子级列表（按分页裁剪）
const visibleChildren = computed<any[]>(() => {
  const end = paging.page * paging.pageSize
  return filteredChildren.value.slice(0, end)
})

// 监控数据总量，自动更新 finished 标记
watchEffect(() => {
  const end = paging.page * paging.pageSize
  const total = filteredChildren.value.length
  paging.finished = end >= total
})

// 重置分页状态
const resetPaging = () => {
  paging.page = 1
  paging.loading = false
  paging.finished = false
  if (rightListRef.value) rightListRef.value.scrollTop = 0
}

// 加载更多（翻页）
let loadTimer: any = null
const loadMore = () => {
  if (paging.loading || paging.finished) return
  paging.loading = true
  clearTimeout(loadTimer)
  // 使用 setTimeout 避免同一帧内多次累加
  loadTimer = setTimeout(() => {
    paging.page += 1
    paging.loading = false
  }, 0)
}

// 右侧列表滚动到底部时触发加载更多
const onRightScroll = (e: Event) => {
  const el = e.target as HTMLDivElement
  const threshold = 80 // 距离底部触发阈值
  if (el.scrollTop + el.clientHeight + threshold >= el.scrollHeight) {
    loadMore()
  }
}

// v-model 同步：统一输出 { '1': [父级id], '2': [子级id...] } 结构
const emitModel = () => {
  const parentId = hub.activeParentId
  const selectedIds = Object.keys(hub.selectedChildIds)

  // 未选择任何内容时，置空
  if (!parentId && selectedIds.length === 0) {
    model.value = null
    return
  }

  const parentType =
    parentMap.value[parentId || childIndex.value[selectedIds[0]]?.parentId || '']?.type

  const valueObj: Record<string, string[]> = {}

  if (selectedIds.length > 0) {
    // 二级保持多选，统一为 string[]
    valueObj['2'] = selectedIds.map(id => String(id))
  } else {
    valueObj['1'] = [String(parentId)]
  }
  valueObj['type'] = parentType
  model.value = valueObj
}

// 回显：根据 v-model 初始化激活的一级与二级选中
const initFromModel = () => {
  const v = model.value
  hub.selectedChildIds = {}

  // 判空：null/undefined/非对象都认为未选择
  if (!v || typeof v !== 'object') {
    hub.activeParentId = ''
    resetPaging()
    return
  }

  const level1Raw = (v as Record<string, any>)['1']
  const level2Raw = (v as Record<string, any>)['2']

  const level1 = Array.isArray(level1Raw) ? level1Raw : []
  const level2 = Array.isArray(level2Raw) ? level2Raw : []

  // 一级：优先取合法的父级 id
  let parentId = level1.find(id => !!parentMap.value[String(id)]) || ''
  // 兜底：若一级为空但存在二级，尝试从二级反推父级
  if (!parentId && level2.length > 0) {
    const firstChild = childIndex.value[String(level2[0])]
    parentId = firstChild?.parentId || ''
  }
  hub.activeParentId = parentId ? String(parentId) : ''

  // 二级选中集合
  level2.forEach(id => {
    const cid = String(id)
    if (childIndex.value[cid]) hub.selectedChildIds[cid] = true
  })

  resetPaging()
}

// 切换一级：始终保持一级单选，清空当前二级选中
const onParentClick = (pid: string) => {
  if (hub.activeParentId === pid) return
  hub.activeParentId = pid
  hub.selectedChildIds = {}
  hub.keyword = ''
  emitModel()
  resetPaging()
}

// 勾选/取消勾选二级
const toggleChild = (cid: string) => {
  if (hub.selectedChildIds[cid]) {
    delete hub.selectedChildIds[cid]
  } else {
    hub.selectedChildIds[cid] = true
  }
  emitModel()
}

// 展示文案：优先展示二级名称集合，无二级则展示一级名称
const MAX_DISPLAY_COUNT = 1
const displayText = computed<string>(() => {
  const v = model.value
  if (!v || typeof v !== 'object') return ''

  const obj = v as Record<string, any>
  const level1 = Array.isArray(obj['1']) ? obj['1'] : []
  const level2 = Array.isArray(obj['2']) ? obj['2'] : []

  const parentId = level1[0] ? String(level1[0]) : ''
  const childIds = level2.map(id => String(id))

  // 优先展示二级
  if (childIds.length > 0) {
    const names = childIds.map(id => childIndex.value[id]?.name).filter(Boolean) as string[]
    if (names.length === 0) return ''
    const parentType =
      parentMap.value[parentId || childIndex.value[childIds[0]]?.parentId || '']?.type
    // 账号类型需要在前面加 @
    if (parentType === 'account' && props.prefix) {
      const displayNames = names.map(name => `${props.prefix}${name || ''}`)
      if (displayNames.length > MAX_DISPLAY_COUNT) {
        const visible = displayNames.slice(0, MAX_DISPLAY_COUNT)
        const remaining = displayNames.length - MAX_DISPLAY_COUNT
        return visible.join(' ') + ` +${remaining}`
      }
      return displayNames.join(' ')
    }
    if (names.length > MAX_DISPLAY_COUNT) {
      const visible = names.slice(0, MAX_DISPLAY_COUNT)
      const remaining = names.length - MAX_DISPLAY_COUNT
      return visible.join(',') + `,+${remaining}`
    }
    return names.join(',')
  }

  // 无二级则展示一级名称
  if (parentId) {
    return parentMap.value[parentId]?.name || ''
  }
  return ''
})

// 打开弹层时预留的钩子，后续如需懒加载可在此处补充逻辑
const onOpen = async () => {}

// options 变更或首次绑定时，根据 v-model 做一次回显
watch(
  () => props.options,
  () => initFromModel(),
  { immediate: true }
)

// v-model 外部变更时，同步回显
watch(
  () => model.value,
  () => initFromModel()
)

// 搜索关键字变更时，重置分页
watch(
  () => keywordDebounced.value,
  () => resetPaging()
)

// 一级切换时，重置分页（onParentClick 中已处理一次，这里作为兜底）
watch(
  () => hub.activeParentId,
  () => resetPaging()
)

onMounted(() => {
  // 如需在首次挂载时请求完整资源树，可在此处调用
})
</script>

<template>
  <el-popover
    v-model:visible="hub.open"
    placement="bottom-start"
    :width="500"
    trigger="click"
    :hide-after="0"
    :show-arrow="false"
    @show="onOpen"
    popper-class="dr-cascader-popper"
  >
    <template #reference>
      <el-input :model-value="displayText" :placeholder="props.placeholder" readonly />
    </template>
    <el-empty
      v-if="
        !rulesStore.dataResource.loading && !parents.length && (!options || options.length === 0)
      "
      class="dr-empty"
      description="暂无数据"
    />

    <div v-else class="dr-cascader" v-loading="rulesStore.dataResource.loading">
      <div class="dr-pane dr-pane--left">
        <el-scrollbar height="320px">
          <div
            v-for="p in parents"
            :key="p.id"
            class="dr-parent-item"
            :class="{ active: hub.activeParentId === p.id }"
            @click="onParentClick(p.id)"
          >
            <el-checkbox
              :model-value="hub.activeParentId === p.id"
              @change="() => onParentClick(p.id)"
            />
            <span class="name ml-10" :title="p.name">{{ p.name }}</span>
          </div>
        </el-scrollbar>
      </div>

      <div class="dr-pane dr-pane--right">
        <div class="dr-search">
          <el-input v-model="hub.keyword" placeholder="请输入二级名称" clearable size="small" />
        </div>

        <!-- 右侧列表：前端分页 + 滚动加载 + 本地过滤 -->
        <div class="dr-list" ref="rightListRef" @scroll="onRightScroll">
          <div
            v-for="item in visibleChildren"
            :key="item.id"
            class="dr-child-item"
            @click="toggleChild(item.id)"
            v-memo="[item.id, hub.selectedChildIds[item.id], item.name]"
          >
            <el-checkbox
              :key="item.id"
              :model-value="!!hub.selectedChildIds[item.id]"
              @click.stop="toggleChild(item.id)"
            />
            <el-text class="w-150 mb-2" truncated> {{ item.name }} </el-text>
          </div>

          <!-- 底部状态：加载中 / 没有更多 / 无匹配结果 -->
          <div class="dr-bottom-status">
            <el-text v-if="paging.loading" type="info">加载中...</el-text>
            <el-text v-else-if="paging.finished && filteredChildren.length > 0" type="info">
              没有更多了
            </el-text>
            <el-text v-else-if="!filteredChildren.length" type="info">暂无匹配结果</el-text>
          </div>
        </div>
      </div>
    </div>
  </el-popover>
</template>

<style scoped lang="scss">
/* 中文注释：展示样式尽量贴近 element-plus 默认选择器形态 */
.dr-input {
  display: inline-flex;
  align-items: center;
  min-height: 32px;
  line-height: 32px;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  padding: 0 8px;
  cursor: pointer;
  width: 100%;
  background: var(--el-fill-color-blank);
}
.dr-input.is-disabled {
  cursor: not-allowed;
  background: var(--el-fill-color-light);
}
.dr-tags {
  display: inline-flex;
  gap: 4px;
}
.dr-tag {
  max-width: 240px;
  overflow: hidden;
  text-overflow: ellipsis;
}
.dr-placeholder {
  color: var(--el-text-color-placeholder);
}
.dr-suffix {
  margin-left: auto;
  color: var(--el-text-color-secondary);
}

.dr-cascader {
  display: flex;
  width: 100%;
}
.dr-pane {
  padding: 8px;
}
.dr-pane--left {
  width: 180px;
  padding-left: 0;
  border-right: 1px solid var(--el-border-color-light);
}
.dr-pane--right {
  flex: 1;
}
.w-150 {
  width: 230px;
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
.dr-parent-item .name {
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dr-search {
  padding-bottom: 8px;
}
.dr-list {
  height: 320px;
  overflow-y: auto; // 右侧列表允许滚动以承载大数据量
}
.dr-child-item {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 34px;
  padding: 0 8px;
  cursor: pointer;
}
.dr-child-item:hover {
  background: var(--el-fill-color-light);
}
.dr-child-item .label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dr-bottom-status {
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-text-color-secondary);
}

:deep(.dr-cascader-popper) {
  padding: 0;
}
</style>
