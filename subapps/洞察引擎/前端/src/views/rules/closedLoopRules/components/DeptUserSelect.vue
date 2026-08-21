<script setup lang="ts">
import { computed, watch, onMounted, ref, nextTick } from 'vue'
import { debounce } from 'lodash-es'
import { useEmployeesByDept } from '@/views/rules/closedLoopRules/hooks/useEmployeesByDept'

// 中文说明：部门 + 人员二联选择复用组件
// - v-model:deptModel 绑定部门对象 { id, deptNo, name }
// - v-model:userModel 绑定人员对象 { id, employeeId, name }
// - 传入部门选项 deptOptions，支持自定义 valueKey/labelKey，默认 value/name
// - 组件内部统一以“部门编码”code（string）作为选中值，通过 code 映射为部门对象回填给外部
defineOptions({ name: 'DeptUserSelect' })

// 对外双向绑定（对象）
const dept = defineModel<any>('deptModel', { default: { id: '', deptNo: '', name: '' } })
const user = defineModel<any>('userModel', { default: { id: '', employeeId: '', name: '' } })

interface Props {
  deptOptions: any[]
  valueKey?: string
  labelKey?: string
  deptPlaceholder?: string
  userPlaceholder?: string
  userVisible?: boolean
  clearUserOnDeptChange?: boolean
  ttl?: number
  minLength?: number
  debounceMs?: number
  deptWidth?: string
  userWidth?: string
}

const props = withDefaults(defineProps<Props>(), {
  deptOptions: () => [],
  valueKey: 'value',
  labelKey: 'name',
  deptPlaceholder: '部门',
  userPlaceholder: '人员',
  userVisible: true,
  clearUserOnDeptChange: true,
  ttl: 60_000,
  minLength: 1,
  debounceMs: 300,
  deptWidth: '160px',
  userWidth: '162px'
})

const emit = defineEmits<{ (e: 'dept-change', deptCode: string): void }>()

// 内部维护“部门编码”，统一与部门选项的 valueKey 对齐（一般为部门 code）
const deptId = ref<string>('')

// 部门选择弹层的显隐状态
const deptPopoverVisible = ref(false)

// TreeV2 实例引用，用于执行 filter / setExpandedKeys
const deptTreeRef = ref()

// 部门搜索关键字
const deptQuery = ref('')

// 部门树数据：统一映射为 { value, label, children } 结构，兼容后端 child/children 字段
const deptTreeData = computed(() => {
  const v = props.valueKey
  const l = props.labelKey

  const walk = (node: any): any | null => {
    if (!node) return null

    const rawChildren = Array.isArray(node.children)
      ? node.children
      : Array.isArray(node.child)
      ? node.child
      : []

    const mapped: any = {
      value: node?.[v],
      label: node?.[l] ?? '',
      children: [] as any[]
    }

    if (rawChildren.length) {
      mapped.children = rawChildren
        .map((c: any) => walk(c))
        .filter((c: any) => c && c.value !== undefined)
    }

    return mapped
  }

  const source = Array.isArray(props.deptOptions) ? props.deptOptions : []
  return source.map(o => walk(o)).filter(o => o && o.value !== undefined)
})

// 部门树默认展开 key 列表：默认展开所有根节点，行为参考抄送人员组件
const deptDefaultExpanded = computed(() => {
  const roots = Array.isArray(deptTreeData.value) ? deptTreeData.value : []
  return roots.map((root: any) => String(root?.value ?? '')).filter(Boolean)
})

// TreeV2 的属性映射
const deptTreeProps = {
  value: 'value',
  label: 'label',
  children: 'children'
}

// 是否存在任何部门数据，用于区分“暂无数据”和“无匹配部门”两种空态
const hasDeptData = computed(() => {
  const roots = Array.isArray(deptTreeData.value) ? deptTreeData.value : []
  return roots.length > 0
})

// 部门搜索结果是否有命中，用于展示“无匹配部门”的提示
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

// 员工数据（按部门）
const employees = useEmployeesByDept({
  ttl: props.ttl,
  minLength: props.minLength,
  debounceMs: props.debounceMs
})

// 员工下拉选项（用于 el-select-v2）
const userOptions = computed(() => {
  return (employees.list.value || []).map((u: any) => ({
    value: u, // 直接使用对象作为选中值
    employeeId: u.employeeId,
    label: `${u.name || ''}-${u.employeeId || ''}`
  }))
})

// 根据部门编码在原始 options 中取原始节点
const getDeptByCode = (code: string) => {
  const v = props.valueKey
  // 递归查找函数
  const findInTree = (nodes: any[]): any => {
    for (const node of nodes) {
      if (node?.[v] === code) return node
      const children = node.children || node.child || []
      const found = findInTree(children)
      if (found) return found
    }
    return null
  }

  return findInTree(props.deptOptions)
}

// 从外部部门对象中提取“部门编码”，兼容旧数据（可能只带 deptNo）
const getDeptCodeFromModel = (d: any): string => {
  if (!d) return ''
  return d.id || ''
}

// 部门输入框展示文案
const currentDeptLabel = computed(() => {
  const code = getDeptCodeFromModel(dept.value)
  if (!code) return ''
  const raw = getDeptByCode(code)
  return (raw && raw[props.labelKey]) || dept.value?.name || ''
})

// 部门变化的统一处理入口
const onDeptChanged = (val: string) => {
  const prevCode = getDeptCodeFromModel(dept.value)
  const raw = getDeptByCode(val)

  // 回填外部部门对象
  dept.value = {
    id: raw?.id ?? dept.value?.id ?? '',
    // 部门编号统一使用接口返回的 code
    deptNo: raw?.code || '',
    name: raw?.[props.labelKey] || ''
  }

  // 员工查询上下文：此处传递部门 code，接口 url 与入参名保持不变
  employees.setDeptId(val)

  // 部门确实变化时，清空人员，避免编辑回填被误清
  if (props.clearUserOnDeptChange && prevCode && prevCode !== val) {
    user.value = { id: '', employeeId: '', name: '' }
  }

  // 拉取一次数据，保证选中项能正确展示
  employees.searchNow('')
  emit('dept-change', val)
}

// 清空部门选择
const clearDeptSelection = () => {
  deptId.value = ''
  dept.value = { id: '', deptNo: '', name: '' }
  // 清空部门时，一并清空人员
  if (props.clearUserOnDeptChange) {
    user.value = { id: '', employeeId: '', name: '' }
  }
  employees.setDeptId('')
}

// 部门树过滤：根据 label 及其子节点是否命中关键字决定是否展示
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

// 计算整棵部门树“一级节点”的 key，用于默认展开一级
const computeDeptAllExpandedKeys = () => {
  const expanded = new Set<string>()
  const roots = Array.isArray(deptTreeData.value) ? deptTreeData.value : []
  for (const root of roots) {
    if (!root) continue
    // 展开所有根节点，从而只展示根节点的子级（初始展开一级）
    expanded.add(String(root.value ?? ''))
  }

  return Array.from(expanded).filter(Boolean)
}

// 根据关键字计算需要展开的部门 key 列表（空关键字时展开全部）
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
    // 子节点命中时，自动展开当前节点
    if (childHit) expanded.add(String(node.value ?? ''))
    return selfHit || childHit
  }

  const roots = Array.isArray(deptTreeData.value) ? deptTreeData.value : []
  for (const root of roots) visit(root)
  return Array.from(expanded).filter(Boolean)
}

// 实际执行部门树过滤 + 展开逻辑
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

  // 没有搜索关键字时，合并当前选中部门的路径，保证打开弹层时能看到已选节点
  if (!keyword) {
    const code = getDeptCodeFromModel(dept.value)
    if (code) {
      const roots = Array.isArray(deptTreeData.value) ? deptTreeData.value : []
      const path = findDeptPathInTree(roots, code) || []
      for (const k of path) {
        if (k) baseExpanded.add(k)
      }
    }
  }

  deptTreeRef.value?.setExpandedKeys?.(Array.from(baseExpanded))
}

// 部门树过滤 + 联动展开（防抖处理，用于输入框实时搜索）
const doDeptFilter = debounce((q: string) => {
  applyDeptFilterAndExpand(q)
}, 300)

// 部门搜索关键字变化时触发过滤
watch(
  () => deptQuery.value,
  q => doDeptFilter(q)
)

// 弹层打开时，按照当前关键字执行一次同步展开（保证默认场景也全部展开）
watch(
  () => deptPopoverVisible.value,
  visible => {
    if (!visible) return
    nextTick(() => {
      applyDeptFilterAndExpand(deptQuery.value || '')
    })
  }
)

// 部门树选中（单选）
const onDeptNodeCurrentChange = (data: any) => {
  if (!data) return
  const val = data.value as string
  if (!val || val === deptId.value) {
    deptPopoverVisible.value = false
    return
  }
  deptId.value = val
  // 选中后收起部门树弹层
  deptPopoverVisible.value = false
}

// 外部回填 -> 同步到内部 deptId，由下面的 watcher 统一处理
watch(
  () => getDeptCodeFromModel(dept.value),
  v => {
    if (!v) {
      deptId.value = ''
      return
    }
    if (deptId.value === v) return
    deptId.value = v
  }
)

// 内部门编码变化 -> 触发部门联动逻辑（唯一入口）
watch(
  () => deptId.value,
  v => {
    if (!v) return
    onDeptChanged(v)
  }
)

onMounted(() => {
  const initCode = getDeptCodeFromModel(dept.value)
  if (initCode) {
    // 初始化以外部回填为准
    deptId.value = initCode
  }
})
</script>

<template>
  <div class="dept-inline-row">
    <!-- 部门单选树使用 Popover + el-input 展示 -->
    <el-popover
      v-model:visible="deptPopoverVisible"
      placement="bottom-start"
      trigger="click"
      :width="600"
      teleported
    >
      <template #reference>
        <el-input
          :model-value="currentDeptLabel"
          :placeholder="props.deptPlaceholder"
          readonly
          clearable
          class="dept-select"
          :style="{ width: props.deptWidth }"
          @clear="clearDeptSelection"
        />
      </template>

      <div class="dept-panel">
        <div class="dept-panel__search">
          <el-input v-model.trim="deptQuery" placeholder="搜索部门" clearable />
        </div>
        <el-tree-v2
          ref="deptTreeRef"
          key="dept-tree"
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
            <div v-if="!hasDeptData" class="dept-panel__empty">暂无部门数据</div>
            <div v-else-if="!hasDeptSearchResult" class="dept-panel__empty">无匹配部门</div>
          </template>
        </el-tree-v2>
      </div>
    </el-popover>

    <!-- 员工下拉保持不变 -->
    <el-select-v2
      key="user-select"
      v-model="user"
      :options="userOptions"
      value-key="id"
      filterable
      clearable
      remote
      :remote-method="employees.remoteSearch"
      :loading="employees.loading.value"
      :placeholder="props.userPlaceholder"
      class="user-select"
      :fit-input-width="300"
      :style="{ width: props.userWidth }"
    >
      <template #empty>
        <div
          class="p-8 text-center"
          style="cursor: pointer"
          @click="!employees.loading.value && employees.error.value ? employees.retry() : null"
        >
          <template v-if="employees.loading.value"> 加载中... </template>
          <template v-else-if="employees.error.value"> 加载失败，点击重试 </template>
          <template v-else>
            {{ employees.emptyText.value || '无匹配人员' }}
          </template>
        </div>
      </template>
      <template #default="{ item }">
        <div class="flex items-center justify-between">
          <el-text class="w-150px mb-2" truncated>
            {{ item.value.name || '' }}
          </el-text>
          <span>
            {{ item.employeeId }}
          </span>
        </div>
      </template>
    </el-select-v2>
  </div>
</template>

<style scoped>
.dept-inline-row {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.dept-select {
  width: 136px;
}
.user-select {
  width: 120px;
}
.justify-between {
  justify-content: space-between;
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
</style>
