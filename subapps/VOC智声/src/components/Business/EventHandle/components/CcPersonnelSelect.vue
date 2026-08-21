<script setup lang="ts">
import { ref, watch, nextTick, computed } from 'vue'
import { debounce } from 'lodash-es'
import { ElMessage } from 'element-plus'
import useSingleEventStore from '@/store/modules/singleEvent'
import type { InsReportSysDepartVo } from '@/api/common/index.d'
// const MAX_CC_COUNT = 100
const isResettingCcCheck = ref(false)

// 组件职责：抄送人员选择（部门全量/指定人员），对外通过 v-model 暴露标准化结构：
//   { orgId, orgNo, orgName, allFlag, userId, userEmpNo, userName }
// 内部使用 TreeV2 展示部门-人员树，支持关键字过滤与勾选；

const props = withDefaults(
  defineProps<{
    // 搜索框占位符
    searchPlaceholder?: string
    // 最大选择人数
    maxCCCount?: number
    disabled?: boolean
    /**
     * 兼容旧调用；批量事件请传入 departAccountTree，避免组件内部实例化批量 store。
     */
    storeScope?: 'single' | 'batch'
    // 外部传入的部门人员树，批量事件按需加载后透传
    departAccountTree?: InsReportSysDepartVo[]
  }>(),
  {
    searchPlaceholder: '请选择抄送人员',
    maxCCCount: 100,
    disabled: false,
    storeScope: 'single',
    departAccountTree: undefined
  }
)

// 事件：change（对外同步选择值，便于外层联动校验）
const emit = defineEmits<{ (e: 'change', value: any[]): void }>()

// v-model：选择结果数组
const model = defineModel<any[]>({ default: [] })

const singleEventStore = useSingleEventStore()

// TreeV2 状态
const ccTreeRef = ref()
const ccTreeData = ref<any[]>([])
const ccNodeMap = ref<Record<string, any>>({})
const ccDefaultExpanded = ref<string[]>([])
// 注意：TreeV2 的 props.value 作为唯一键，这里映射到我们自建的 key（如 dept:xxx / user:yyy）
const ccTreeProps = { value: 'key', label: 'label', children: 'children' }

// 弹层与交互
const ccPanelVisible = ref(false)
const ccQuery = ref('')
const ccTreeBuilt = ref(false)

// 外层 el-input 展示文案（只读）
const ccDisplayText = computed(() => {
  const list = Array.isArray(model.value) ? model.value : []
  if (!list.length) return ''
  const parts = list.map(i => {
    if (i.allFlag) return `${i.orgName || ''}/全部`
    if (!i.allFlag) return i.orgName + (i.userName ? '/' + i.userName : '')
  })
  return parts.join('，')
})

// 拉取部门-人员树
const fetchDeptEmployeeTree = async () => {
  try {
    const list = Array.isArray(props.departAccountTree)
      ? props.departAccountTree
      : props.storeScope === 'batch'
        ? []
        : singleEventStore.departAccountTree
    buildCcTree(list)
    await nextTick()
    syncTreeCheckedByModel()
    ccTreeBuilt.value = true
  } catch (e) {
    console.error(e)
  }
}

/**
 * 抄送面板首次打开时再构建 TreeV2 数据，避免弹窗未展开就递归大树。
 */
const ensureCcTreeReady = async () => {
  if (ccTreeBuilt.value) {
    await syncTreeCheckedByModel()
    return
  }

  await fetchDeptEmployeeTree()
}

// 过滤：命中自己或任一后代节点即显示
const ccFilterMethod = (query: string, data: any) => {
  const q = String(query || '')
    .trim()
    .toLowerCase()
  if (!q) return true
  const label = String(data?.label || '').toLowerCase()
  const parentDeptName = String(data?.parentDept?.deptName || '').toLowerCase()

  // 1）命中当前节点本身
  if (label.includes(q)) return true

  // 2）关键字命中当前部门名称时，需要展示当前部门下的所有员工
  //    例如：搜索“客户”，命中“客户服务部”，则该部门下的员工也要展示（但子部门员工不强制展示）
  if (parentDeptName && parentDeptName.includes(q)) return true

  // 3）保留原有“命中任意后代则显示当前部门”的能力，方便通过子节点反向定位部门
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

// 转义工具
const escapeHtml = (s: string) =>
  String(s)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')

const escapeRegExp = (s: string) => s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')

// 高亮命中关键字
const renderCcLabel = (label: string) => {
  const text = String(label || '')
  const q = String(ccQuery.value || '').trim()
  if (!q) return escapeHtml(text)
  const re = new RegExp(escapeRegExp(q), 'ig')
  let last = 0
  let out = ''
  let m: RegExpExecArray | null
  while ((m = re.exec(text))) {
    out += escapeHtml(text.slice(last, m.index))
    out += `<span class="cc-hit">${escapeHtml(m[0])}</span>`
    last = m.index + m[0].length
  }
  out += escapeHtml(text.slice(last))
  return out
}

// 后端结构 { id,name,code,account[],child[] } -> TreeV2 节点，并维护 key->meta 映射
const buildCcTree = (list: any[] = []) => {
  const map: Record<string, any> = {}
  const walk = (dept: any): any => {
    const deptKey = `dept:${dept?.id}`
    const node: any = {
      key: deptKey,
      type: 'dept',
      id: dept?.id || '',
      deptNo: dept?.code || '',
      deptName: dept?.name || '',
      label: dept?.name || '',
      children: [] as any[]
    }
    map[deptKey] = node
    // 人员 - 接口字段已改为: userId, userName, employeeId
    const accounts = Array.isArray(dept?.account) ? dept.account : []
    for (const u of accounts) {
      const userId = u?.userId || ''
      const userName = u?.userName || ''
      const employeeId = u?.employeeId || ''
      const userKey = `user:${userId}`
      const unode = {
        key: userKey,
        type: 'user',
        userId,
        userName,
        employeeId,
        label: `${userName}${employeeId ? ' - ' + employeeId : ''}`,
        parentDept: { id: node.id, deptNo: node.deptNo, deptName: node.deptName },
        parentKey: deptKey
      }
      map[userKey] = unode
      node.children.push(unode)
    }
    // 子部门
    const children = Array.isArray(dept?.child) ? dept.child : []
    for (const c of children) node.children.push(walk(c))
    return node
  }
  const tree = (Array.isArray(list) ? list : []).map(walk)
  ccNodeMap.value = map
  ccTreeData.value = tree
  ccDefaultExpanded.value = tree.map((n: any) => n.key)
}

// 当前 v-model -> 勾选 keys
const keysFromModel = (list: any[]): string[] => {
  const keys: string[] = []
  for (const item of list || []) {
    if (item?.allFlag && item?.orgId) keys.push(`dept:${item.orgId}`)
    else if (!item?.allFlag && item?.userId) keys.push(`user:${item.userId}`)
  }
  return keys
}

// 勾选变更 -> 写回 v-model（剔除“选了部门又选其人员”的重复）
const onCcCheck = () => {
  if (isResettingCcCheck.value) {
    return
  }
  // 从 TreeV2 取已勾选 key（不含半选）
  const keys: string[] = (ccTreeRef.value?.getCheckedKeys?.() as string[]) || []
  const deptIds = new Set(keys.filter(k => k.startsWith('dept:')).map(k => k.slice(5)))
  const result: any[] = []
  for (const k of keys) {
    const meta = ccNodeMap.value[k]
    if (!meta) continue
    if (meta.type === 'dept') {
      // 选中部门（全员抄送）
      result.push({
        orgId: meta.id,
        orgNo: meta.deptNo,
        orgName: meta.deptName,
        allFlag: true,
        userId: '',
        userEmpNo: '',
        userName: ''
      })
    } else if (meta.type === 'user') {
      // 若已选中所属部门，则不重复记录用户
      if (deptIds.has(meta?.parentDept?.id)) continue
      result.push({
        orgId: meta?.parentDept?.id || '',
        orgNo: meta?.parentDept?.deptNo || '',
        orgName: meta?.parentDept?.deptName || '',
        allFlag: false,
        userId: meta.userId,
        userEmpNo: meta.employeeId || '',
        userName: meta.userName || ''
      })
    }
  }
  model.value = result
  emit('change', result)
}

// 根据 v-model 同步 Tree 勾选
const syncTreeCheckedByModel = async () => {
  const keys = keysFromModel(model.value)
  await nextTick()
  if (ccTreeRef.value?.setCheckedKeys) ccTreeRef.value.setCheckedKeys(keys)
}

// 按关键字展开路径上的节点
const computeExpandedKeysByQuery = (query: string) => {
  const q = String(query || '')
    .trim()
    .toLowerCase()
  if (!q) return ccDefaultExpanded.value
  const expanded = new Set<string>()
  const visit = (node: any) => {
    const label = String(node?.label || '').toLowerCase()
    const hit = label.includes(q)
    const children = Array.isArray(node?.children) ? node.children : []
    let childHit = false
    for (const c of children) {
      if (visit(c)) childHit = true
    }
    // 自身命中或子节点命中时，都需要将当前节点展开，便于查看该部门下的人员
    if ((hit || childHit) && children.length) expanded.add(node.key)
    return hit || childHit
  }
  for (const root of ccTreeData.value) visit(root)
  return Array.from(expanded)
}

const doCcFilter = debounce((q: string) => {
  ccTreeRef.value?.filter?.(q)
  const exp = computeExpandedKeysByQuery(q)
  ccTreeRef.value?.setExpandedKeys?.(exp)
}, 300)

watch(
  () => ccQuery.value,
  q => doCcFilter(q)
)
watch(
  () => model.value,
  () => syncTreeCheckedByModel(),
  { deep: true }
)

watch(
  () => model.value,
  (val, oldVal) => {
    if (isResettingCcCheck.value) return
    const list = Array.isArray(val) ? val : []
    if (list.length > props.maxCCCount) {
      ElMessage.warning(`最多只能选择${props.maxCCCount}个抄送对象`)
      const prev = Array.isArray(oldVal) ? oldVal : []
      isResettingCcCheck.value = true
      model.value = prev
      emit('change', prev)
      nextTick(() => {
        requestAnimationFrame(() => {
          const prevKeys = keysFromModel(prev)
          ccTreeRef.value?.setCheckedKeys?.(prevKeys)
        })
        isResettingCcCheck.value = false
      })
    }
  },
  { deep: true }
)

watch(
  () => props.departAccountTree,
  () => {
    ccTreeBuilt.value = false
    if (ccPanelVisible.value) {
      void fetchDeptEmployeeTree()
    }
  }
)

watch(
  () => ccPanelVisible.value,
  visible => {
    if (visible) {
      void ensureCcTreeReady()
    }
  }
)

// 清空 & 确认
const clearSelection = () => {
  ccTreeRef.value?.setCheckedKeys?.([])
  model.value = []
  emit('change', [])
}
const confirmCc = () => (ccPanelVisible.value = false)

</script>

<template>
  <el-popover
    v-model:visible="ccPanelVisible"
    placement="bottom-start"
    trigger="click"
    :width="600"
    teleported
    :disabled="props.disabled"
  >
    <template #reference>
      <el-input
        :model-value="ccDisplayText"
        :placeholder="props.searchPlaceholder"
        clearable
        readonly
        @clear="clearSelection"
        style="width: 100%"
        :disabled="props.disabled"
      />
    </template>
    <div class="cc-panel">
      <div class="cc-panel__search">
        <el-input v-model.trim="ccQuery" placeholder="搜索部门/人员" clearable />
      </div>
      <el-tree-v2
        ref="ccTreeRef"
        :data="ccTreeData"
        :props="ccTreeProps"
        :filter-method="ccFilterMethod"
        show-checkbox
        check-on-click-node
        :expand-on-click-node="false"
        :default-expanded-keys="ccDefaultExpanded"
        :height="300"
        @check="onCcCheck"
      >
        <template #default="{ node, data }">
          <el-icon v-if="node.isLeaf && data.employeeId" color="#999" class="mr-4"
            ><UserFilled
          /></el-icon>
          <span v-html="renderCcLabel(data.label)"></span>
        </template>
      </el-tree-v2>
      <div class="cc-panel__footer">
        <el-button size="small" type="primary" @click="confirmCc">确定</el-button>
        <el-button size="small" @click="ccPanelVisible = false">取消</el-button>
        <el-button size="small" text @click="clearSelection">清空</el-button>
      </div>
    </div>
  </el-popover>
</template>

<style scoped>
.cc-hit {
  background: #fff2cc;
  color: #d48806;
  padding: 0 1px;
  border-radius: 2px;
}

.cc-panel {
  width: 100%;
}
.cc-panel__search {
  margin-bottom: 8px;
}
.cc-panel__footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
}
</style>
