<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { debounce } from 'lodash-es'
import { CircleClose, ArrowDown } from '@element-plus/icons-vue'
import useUserStore from '@/stores/modules/user'

// 中文说明：标准观点-主责部门选择组件（仅部门）
// - v-model:deptModel 绑定部门对象 { id, deptNo, name }
// - 内部从用户 store 缓存的部门树取值（数据源为 findDepartAccountTree）
defineOptions({ name: 'StandardPointDeptSelect' })

export type DeptModel = {
  id: string
  deptNo: string
  name: string
}

const dept = defineModel<DeptModel>('deptModel', {
  default: () => ({ id: '', deptNo: '', name: '' })
})

interface Props {
  deptPlaceholder?: string
  deptWidth?: string
  popoverWidth?: number | string
  disabled?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  deptPlaceholder: '部门',
  deptWidth: '240px',
  popoverWidth: 600,
  disabled: false
})

const deptOptions = ref<any[]>([])
const loading = ref(false)
const error = ref<any>(null)
const userStore = useUserStore()

const fetchDeptTree = async () => {
  if (loading.value) return
  loading.value = true
  error.value = null
  try {
    const list = await userStore.getDepartAccountTree({ silent: true })
    deptOptions.value = Array.isArray(list) ? list : []
  } catch (e: any) {
    deptOptions.value = []
    error.value = e
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  void fetchDeptTree()
})

const deptId = ref<string>('') // 内部选中值：部门 id（string）
const deptPopoverVisible = ref(false)
const deptTreeRef = ref()
const deptQuery = ref('')

// 将后端树数据统一映射为 TreeV2 结构 { value, label, children }
const deptTreeData = computed(() => {
  const walk = (node: any): any | null => {
    if (!node) return null
    const rawChildren = Array.isArray(node.children)
      ? node.children
      : Array.isArray(node.child)
      ? node.child
      : []

    const mapped: any = {
      value: node?.id != null ? String(node.id) : '',
      label: String(node?.name || ''),
      children: [] as any[]
    }

    if (rawChildren.length) {
      mapped.children = rawChildren
        .map((c: any) => walk(c))
        .filter((c: any) => c && c.value !== undefined && c.value !== null)
    }
    return mapped
  }

  const source = Array.isArray(deptOptions.value) ? deptOptions.value : []
  return source.map(o => walk(o)).filter(o => o && o.value)
})

const deptDefaultExpanded = computed(() => {
  const roots = Array.isArray(deptTreeData.value) ? deptTreeData.value : []
  return roots.map((root: any) => String(root?.value ?? '')).filter(Boolean)
})

const deptTreeProps = {
  value: 'value',
  label: 'label',
  children: 'children'
}

const hasDeptData = computed(() => {
  const roots = Array.isArray(deptTreeData.value) ? deptTreeData.value : []
  return roots.length > 0
})

const hasDeptSearchResult = computed(() => {
  const roots = Array.isArray(deptTreeData.value) ? deptTreeData.value : []
  if (!roots.length) return false

  const q = String(deptQuery.value || '')
    .trim()
    .toLowerCase()
  if (!q) return true

  const matchNode = (node: any): boolean => {
    if (!node) return false
    const label = String(node?.label || '').toLowerCase()
    if (label.includes(q)) return true
    const children = Array.isArray(node?.children) ? node.children : []
    return children.some((c: any) => matchNode(c))
  }

  return roots.some((root: any) => matchNode(root))
})

// 根据部门 id 在原始 options 中取原始节点
const getDeptById = (id: string) => {
  const findInTree = (nodes: any[]): any => {
    for (const node of nodes) {
      if (!node) continue
      if (String(node?.id ?? '') === String(id)) return node
      const children = node.children || node.child || []
      const found = findInTree(children)
      if (found) return found
    }
    return null
  }
  return findInTree(Array.isArray(deptOptions.value) ? deptOptions.value : [])
}

const getDeptIdFromModel = (d: DeptModel | any): string => {
  if (!d) return ''
  return d.id ? String(d.id) : ''
}

const tryFillDeptModelFromTree = () => {
  const id = getDeptIdFromModel(dept.value) || String(deptId.value || '')
  if (!id) return

  const raw = getDeptById(id)
  if (!raw) return

  const current = dept.value || { id: '', deptNo: '', name: '' }
  const needFill = !String(current?.name || '').trim() || !String(current?.deptNo || '').trim()
  if (!needFill) return

  dept.value = {
    id: raw?.id != null ? String(raw.id) : String(id),
    deptNo: String(current?.deptNo || raw?.code || ''),
    name: String(current?.name || raw?.name || '')
  }
}

const currentDeptLabel = computed(() => {
  const id = getDeptIdFromModel(dept.value)
  if (!id) return ''
  const raw = getDeptById(id)
  return String(raw?.name || dept.value?.name || '')
})

const onDeptChanged = (val: string) => {
  const raw = getDeptById(val)
  dept.value = {
    id: raw?.id != null ? String(raw.id) : String(val || ''),
    deptNo: raw?.code != null ? String(raw.code) : String(dept.value?.deptNo || ''),
    name: raw?.name != null ? String(raw.name) : String(dept.value?.name || '')
  }
}

const clearDeptSelection = () => {
  deptId.value = ''
  dept.value = { id: '', deptNo: '', name: '' }
}

const isHovering = ref(false)

const deptFilterMethod = (query: string, data: any) => {
  const q = String(query || '')
    .trim()
    .toLowerCase()
  if (!q) return true
  const label = String(data?.label || '').toLowerCase()
  if (label.includes(q)) return true
  const children = Array.isArray(data?.children) ? data.children : []
  const hasDescendant = (list: any[]): boolean => {
    for (const c of list) {
      const l = String(c?.label || '').toLowerCase()
      if (l.includes(q)) return true
      if (Array.isArray(c?.children) && c.children.length && hasDescendant(c.children)) return true
    }
    return false
  }
  return hasDescendant(children)
}

const computeDeptAllExpandedKeys = () => {
  const expanded = new Set<string>()
  const roots = Array.isArray(deptTreeData.value) ? deptTreeData.value : []
  for (const root of roots) {
    if (!root) continue
    expanded.add(String(root.value ?? ''))
  }
  return Array.from(expanded).filter(Boolean)
}

const computeDeptExpandedKeysByQuery = (query: string) => {
  const q = String(query || '')
    .trim()
    .toLowerCase()
  if (!q) return computeDeptAllExpandedKeys()

  const expanded = new Set<string>()

  const visit = (node: any): boolean => {
    if (!node) return false
    const label = String(node?.label || '').toLowerCase()
    const selfHit = label.includes(q)
    const children = Array.isArray(node?.children) ? node.children : []
    let childHit = false
    for (const c of children) {
      if (visit(c)) childHit = true
    }
    if (childHit) expanded.add(String(node.value ?? ''))
    return selfHit || childHit
  }

  const roots = Array.isArray(deptTreeData.value) ? deptTreeData.value : []
  for (const root of roots) visit(root)
  return Array.from(expanded).filter(Boolean)
}

const findDeptPathInTree = (nodes: any[], target: string, path: string[] = []): string[] | null => {
  const list = Array.isArray(nodes) ? nodes : []
  for (const node of list) {
    if (!node) continue
    const key = String(node.value ?? '')
    const nextPath = [...path, key]
    if (key === target) return nextPath
    const children = Array.isArray(node.children) ? node.children : []
    const found = findDeptPathInTree(children, target, nextPath)
    if (found && found.length) return found
  }
  return null
}

const applyDeptFilterAndExpand = (q: string) => {
  const keyword = q || ''
  deptTreeRef.value?.filter?.(keyword)
  const baseExpanded = new Set<string>(computeDeptExpandedKeysByQuery(keyword))

  if (!keyword) {
    const id = getDeptIdFromModel(dept.value)
    if (id) {
      const roots = Array.isArray(deptTreeData.value) ? deptTreeData.value : []
      const path = findDeptPathInTree(roots, id) || []
      for (const k of path) {
        if (k) baseExpanded.add(k)
      }
    }
  }

  deptTreeRef.value?.setExpandedKeys?.(Array.from(baseExpanded))
}

const doDeptFilter = debounce((q: string) => {
  applyDeptFilterAndExpand(q)
}, 300)

watch(
  () => deptQuery.value,
  q => doDeptFilter(q)
)

watch(
  () => deptPopoverVisible.value,
  visible => {
    if (!visible) return
    // 打开弹层时兜底拉取一次，避免首屏未加载完成导致空白
    if (!deptOptions.value.length && !loading.value) void fetchDeptTree()
    tryFillDeptModelFromTree()
    nextTick(() => {
      applyDeptFilterAndExpand(deptQuery.value || '')
    })
  }
)

const onDeptNodeCurrentChange = (data: any) => {
  if (!data) return
  const val = String(data.value || '')
  if (!val || val === deptId.value) {
    deptPopoverVisible.value = false
    return
  }
  deptId.value = val
  deptPopoverVisible.value = false
}

watch(
  () => getDeptIdFromModel(dept.value),
  v => {
    if (!v) {
      deptId.value = ''
      return
    }
    if (deptId.value === v) return
    deptId.value = v
  }
)

watch(
  () => deptId.value,
  v => {
    onDeptChanged(v)
  }
)

watch(
  () => deptOptions.value,
  () => {
    const id = getDeptIdFromModel(dept.value)
    if (id && deptId.value !== id) deptId.value = id
    tryFillDeptModelFromTree()
  }
)
</script>

<template>
  <div class="dept-only-select">
    <el-popover
      v-model:visible="deptPopoverVisible"
      placement="bottom-start"
      trigger="click"
      :width="props.popoverWidth"
      teleported
      :disabled="props.disabled"
    >
      <template #reference>
        <div @mouseenter="isHovering = true" @mouseleave="isHovering = false">
          <el-input
            :model-value="currentDeptLabel"
            :placeholder="props.deptPlaceholder"
            readonly
            :disabled="props.disabled"
            class="dept-select"
            :style="{ width: props.deptWidth }"
          >
            <template #suffix>
              <span v-if="currentDeptLabel && !props.disabled" class="suffix-icons">
                <el-icon v-show="isHovering" class="clear-icon" @click.stop="clearDeptSelection">
                  <CircleClose />
                </el-icon>
                <el-icon
                  v-show="!isHovering"
                  class="arrow-icon"
                  :class="{ 'is-reverse': deptPopoverVisible }"
                >
                  <ArrowDown />
                </el-icon>
              </span>
              <el-icon v-else class="arrow-icon" :class="{ 'is-reverse': deptPopoverVisible }">
                <ArrowDown />
              </el-icon>
            </template>
          </el-input>
        </div>
      </template>

      <div class="dept-panel">
        <div class="dept-panel__search">
          <el-input v-model.trim="deptQuery" placeholder="搜索部门" clearable />
        </div>
        <el-tree-v2
          ref="deptTreeRef"
          key="dept-tree"
          v-loading="loading"
          :data="deptTreeData"
          :props="deptTreeProps"
          :default-expanded-keys="deptDefaultExpanded"
          :height="264"
          :highlight-current="true"
          :current-node-key="deptId"
          :empty-text="''"
          :filter-method="deptFilterMethod"
          @current-change="onDeptNodeCurrentChange"
        >
          <template #empty>
            <div v-if="loading" class="dept-panel__empty">加载中...</div>
            <div
              v-else-if="error"
              class="dept-panel__empty dept-panel__empty--action"
              @click="fetchDeptTree"
            >
              加载失败，点击重试
            </div>
            <div v-else-if="!hasDeptData" class="dept-panel__empty">暂无部门数据</div>
            <div v-else-if="!hasDeptSearchResult" class="dept-panel__empty">无匹配部门</div>
          </template>
        </el-tree-v2>
      </div>
    </el-popover>
  </div>
</template>

<style scoped>
.dept-only-select {
  /* display: inline-flex;
  align-items: center; */
}
.dept-panel {
  width: 100%;
}
.dept-panel__search {
  margin-bottom: 8px;
}
.dept-panel__empty {
  padding-top: 20px;
  font-size: 12px;
  color: #999;
  text-align: center;
}
.dept-panel__empty--action {
  cursor: pointer;
}
.suffix-icons {
  display: flex;
  align-items: center;
  gap: 4px;
}
.clear-icon {
  cursor: pointer;
  color: var(--el-text-color-placeholder);
  transition: color 0.2s;
}
.clear-icon:hover {
  color: var(--el-text-color-regular);
}
.arrow-icon {
  color: var(--el-text-color-placeholder);
  transition: transform 0.3s;
}
.arrow-icon.is-reverse {
  transform: rotate(180deg);
}
</style>
