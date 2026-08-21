<script setup lang="ts">
/**
 * H5人员选择组件
 * 支持部门-人员树形结构选择，搜索过滤，多选等功能
 */
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { debounce } from 'lodash-es'
import { showToast } from 'vant'
import { usePermissionsStore, useTaskEventStore } from '@h5/store'

defineOptions({ name: 'HFilterPersonnelSelect' })

/** API返回的账号节点类型 */
type ApiAccountNode = {
  id: string
  name: string
  employeeId?: string
}

/** API返回的部门节点类型 */
type ApiDeptNode = {
  id: string
  name: string
  code?: string
  parentId?: string
  account?: ApiAccountNode[]
  child?: ApiDeptNode[] | null
}

/** 父级部门元信息 */
type ParentDeptMeta = {
  deptId: string
  deptName: string
  deptCode?: string
  parentDeptId?: string
  parentDeptName?: string
}

/** 树形节点类型 */
type TreeNode = {
  key: string
  type: 'dept' | 'user'
  label: string
  /** 预计算的搜索文本（小写），用于大数据量下的快速匹配 */
  searchText?: string
  disabled?: boolean
  children?: TreeNode[]
  parentDept?: ParentDeptMeta
  userId?: string
  employeeId?: string
  deptId?: string
  deptCode?: string
  deptName?: string
  parentDeptId?: string
}

/** 组件属性定义 */
const props = withDefaults(
  defineProps<{
    /** 接口返回的部门-人员树 */
    options?: ApiDeptNode[]
    /** 外部加载状态 */
    loading?: boolean
    /** 弹框标题 */
    title?: string
    /** 输入框占位 */
    placeholder?: string
    /** 是否禁用 */
    disabled?: boolean
    /** 是否支持搜索 */
    searchable?: boolean
    /** 最大选择人数，默认 100；传 1 时可作为单选人员弹框使用 */
    maxSelected?: number
    /** 选中人员展示格式 */
    displayMode?: 'default' | 'departmentPath'
    /** 外部补充的选中人员展示文案，用于已选 userId 暂未从人员树补齐标签时兜底显示 */
    fallbackSelectedLabel?: string
  }>(),
  {
    options: () => [],
    loading: false,
    title: '处理人员',
    placeholder: '请选择',
    disabled: false,
    searchable: true,
    maxSelected: 100,
    displayMode: 'default',
    fallbackSelectedLabel: ''
  }
)

/**
 * v-model：人员 userId 数组
 * - 空数组代表"不限"
 */
const modelValue = defineModel<string[]>({ default: () => [] })

// Store 实例
const permStore = usePermissionsStore()
const taskEventStore = useTaskEventStore()
/** 当前用户所属部门ID */
const currentDeptId = computed(() => String(permStore.finalDeptId || ''))
/** 当前用户ID */
const currentUserId = computed(() => String(permStore.userId || ''))

// 弹框与搜索相关状态
/** 主弹框显示状态 */
const popupVisible = ref(false)
/** 已选人员列表弹框显示状态 */
const selectedSheetVisible = ref(false)
/** 搜索输入框的值 */
const keywordInput = ref('')
/** 实际搜索关键词 */
const keyword = ref('')
/** 防抖定时器 */
const debounceTimer = ref<number | undefined>(undefined)

// TreeV2 相关引用
/** 树组件引用 */
const treeRef = ref<any>()
/** 树容器元素引用 */
const treeWrapRef = ref<HTMLElement | null>(null)
/** 树组件高度 */
const treeHeight = ref(320)
/** 尺寸监听器 */
const resizeObserver = ref<ResizeObserver | null>(null)

// 草稿选择状态（取消操作不影响外部 v-model）
/** 草稿选中的用户ID数组 */
const draftSelected = ref<string[]>([])
/** 上次确认的用户ID数组 */
const lastAcceptedUserIds = ref<string[]>([])
/** 上次确认的树节点key数组 */
const lastAcceptedCheckedKeys = ref<string[]>([])
/** 已提交的选中项（来自v-model） */
const committedSelected = computed<string[]>(() => (Array.isArray(modelValue.value) ? modelValue.value : []))

/** 最大选择人数限制 */
const maxSelectedCount = computed(() => Math.max(1, Number(props.maxSelected || 100)))

// Tree 数据与映射（用于展示/定位）
/** 树形数据 */
const treeData = ref<TreeNode[]>([])
/** 用户ID到显示标签的映射 */
const userLabelMap = ref<Map<string, string>>(new Map())
/** 用户ID到部门信息的映射 */
const userDeptMetaMap = ref<Map<string, ParentDeptMeta>>(new Map())
/** 默认展开的节点key数组 */
const defaultExpandedKeys = ref<string[]>([])
/** 默认当前部门节点key */
const defaultCurrentDeptKey = ref<string>('')
/** 当前部门节点key */
const currentDeptKey = ref<string>('')

/** 树组件属性配置 */
const treeProps = { value: 'key', label: 'label', children: 'children', disabled: 'disabled' }

/**
 * 标准化搜索查询字符串
 * @param q 原始查询字符串
 * @returns 标准化后的小写字符串
 */
const normalizeQuery = (q: string) =>
  String(q || '')
    .trim()
    .toLowerCase()

// 搜索状态（避免 filterMethod 内部做深度遍历，降低到 O(n)）
/** 标准化的搜索查询 */
const searchQueryNormalized = ref('')
/** 搜索时可见的节点key集合 */
const searchVisibleKeys = ref<Set<string> | null>(null)
/** 搜索时展开的节点key数组 */
const searchExpandedKeys = ref<string[]>([])

/** 已选择的人员项目列表 */
const selectedItems = computed(() => {
  return (Array.isArray(draftSelected.value) ? draftSelected.value : [])
    .filter(Boolean)
    .map(userId => ({
      userId,
      label: getUserDisplayLabel(userId)
    }))
})

/** 按部门分组的已选择人员 */
const selectedGroups = computed(() => {
  const ids = (Array.isArray(draftSelected.value) ? draftSelected.value : []).filter(Boolean)
  const groups = new Map<
    string,
    { deptId: string; deptName: string; items: { userId: string; label: string }[] }
  >()

  // 按部门分组
  for (const userId of ids) {
    const meta = userDeptMetaMap.value.get(userId)
    const deptId = String(meta?.deptId || '')
    const deptName = String(meta?.deptName || '未知部门')
    const groupKey = deptId || '__unknown__'
    if (!groups.has(groupKey)) {
      groups.set(groupKey, { deptId, deptName, items: [] })
    }
    groups.get(groupKey)!.items.push({
      userId,
      label: getUserDisplayLabel(userId)
    })
  }

  // 按部门名称排序
  const list = Array.from(groups.values())
  list.sort((a, b) =>
    String(a.deptName || '').localeCompare(String(b.deptName || ''), 'zh-Hans-CN', { sensitivity: 'base' })
  )
  return list
})

/** 选择器显示文本 */
const selectedText = computed(() => {
  const selected = committedSelected.value
  if (!selected.length) return props.placeholder
  const firstLabel = getUserDisplayLabel(selected[0])
  if (selected.length === 1) return firstLabel
  return `${firstLabel} 等${selected.length}人`
})

/**
 * 生成选中人员展示文案。
 * @param userId 人员 ID
 * @returns 展示文案
 */
const getUserDisplayLabel = (userId: string) => {
  const baseLabel =
    userLabelMap.value.get(userId) ||
    (committedSelected.value.length === 1 ? props.fallbackSelectedLabel : '') ||
    userId
  if (props.displayMode !== 'departmentPath') return baseLabel

  const meta = userDeptMetaMap.value.get(userId)
  if (!meta) return baseLabel

  const departmentText = [meta.parentDeptName, meta.deptName].filter(Boolean).join('#')
  return departmentText ? `${departmentText}#${baseLabel}` : baseLabel
}

/**
 * 同步树组件高度
 */
const syncTreeHeight = () => {
  const el = treeWrapRef.value
  const h = el?.clientHeight || 0
  treeHeight.value = Math.max(220, Math.floor(h))
}

/**
 * 设置尺寸监听器
 */
const setupResizeObserver = async () => {
  await nextTick()
  syncTreeHeight()
  if (!treeWrapRef.value) return
  resizeObserver.value?.disconnect()
  resizeObserver.value = new ResizeObserver(() => syncTreeHeight())
  resizeObserver.value.observe(treeWrapRef.value)
}

/**
 * 同步树视图状态（搜索、选中、展开等）
 */
const syncTreeViewState = async () => {
  await nextTick()
  const q = normalizeQuery(keyword.value)
  if (q) {
    buildSearchState(q)
    treeRef.value?.filter?.(q)
  } else {
    treeRef.value?.filter?.('')
  }
  await syncTreeCheckedByDraft()
  const exp = keyword.value ? searchExpandedKeys.value : defaultExpandedKeys.value
  treeRef.value?.setExpandedKeys?.(exp)
  if (currentDeptKey.value) treeRef.value?.setCurrentKey?.(currentDeptKey.value)

  lastAcceptedUserIds.value = [...draftSelected.value]
  lastAcceptedCheckedKeys.value = keysFromModel(draftSelected.value)
}

/**
 * 重建树形数据结构
 * 将后端返回的部门-人员结构转换为TreeV2组件所需的节点格式
 * @param list API返回的部门节点数组
 */
const rebuildTree = (list: ApiDeptNode[] = []) => {
  const userMap = new Map<string, string>()
  const deptMetaMap = new Map<string, ParentDeptMeta>()
  const parentMap = new Map<string, string>()
  const deptNameMap = new Map<string, string>()
  const curDeptId = currentDeptId.value
  const curUserId = currentUserId.value

  // 中文排序器
  const collator = new Intl.Collator('zh-Hans-CN', { sensitivity: 'base' })
  const sortByName = (a: { label?: string }, b: { label?: string }) =>
    collator.compare(String(a?.label || ''), String(b?.label || ''))

  /**
   * 构建部门节点
   * @param dept 部门数据
   * @returns 包含节点和是否包含当前用户部门的对象
   */
  const buildDept = (dept: ApiDeptNode): { node: TreeNode; containsCurrent: boolean } => {
    const deptId = String(dept?.id || '')
    const deptName = String(dept?.name || '')
    const deptCode = dept?.code ? String(dept.code) : ''
    const parentDeptId = dept?.parentId ? String(dept.parentId) : ''
    if (deptId) parentMap.set(deptId, parentDeptId)
    if (deptId) deptNameMap.set(deptId, deptName)
    const parentDeptName = parentDeptId ? deptNameMap.get(parentDeptId) || '' : ''

    const deptKey = `dept:${deptId}`
    const node: TreeNode = {
      key: deptKey,
      type: 'dept',
      deptId,
      deptName,
      deptCode,
      parentDeptId,
      label: deptName,
      searchText: normalizeQuery([deptName, deptCode].filter(Boolean).join(' ')),
      disabled: false,
      children: []
    }

    // 构建人员节点：当前用户优先显示
    const accounts = Array.isArray(dept?.account) ? dept.account : []
    const userNodes: TreeNode[] = accounts
      .map(acc => {
        const userId = String(acc?.id || '')
        if (!userId) return null
        const userName = String(acc?.name || '')
        const empNo = acc?.employeeId ? String(acc.employeeId) : ''
        const label = empNo ? `${userName} - ${empNo}` : userName
        const userKey = `user:${userId}`
        userMap.set(userId, label)
        deptMetaMap.set(userId, { deptId, deptName, deptCode, parentDeptId, parentDeptName })
        return {
          key: userKey,
          type: 'user' as const,
          label,
          searchText: normalizeQuery([label, deptName, deptCode].filter(Boolean).join(' ')),
          userId,
          employeeId: empNo,
          parentDept: { deptId, deptName, deptCode }
        }
      })
      .filter(Boolean) as TreeNode[]

    // 人员排序：当前用户优先，其他按姓名排序
    userNodes.sort((a, b) => {
      const aIsCurrent = curUserId && a.userId === curUserId ? 1 : 0
      const bIsCurrent = curUserId && b.userId === curUserId ? 1 : 0
      if (aIsCurrent !== bIsCurrent) return bIsCurrent - aIsCurrent
      return sortByName(a, b)
    })

    userNodes.forEach(u => node.children!.push(u))

    // 构建子部门：包含当前部门链路的分支优先
    const childList = Array.isArray(dept?.child) ? dept.child : []
    const builtChildren = childList.map(buildDept)
    builtChildren.sort((a, b) => {
      const ac = a.containsCurrent ? 1 : 0
      const bc = b.containsCurrent ? 1 : 0
      if (ac !== bc) return bc - ac
      return sortByName(a.node, b.node)
    })
    builtChildren.forEach(c => node.children!.push(c.node))

    const containsCurrent = (curDeptId && deptId === curDeptId) || builtChildren.some(c => c.containsCurrent)
    return { node, containsCurrent }
  }

  // 构建根节点并排序
  const roots = (Array.isArray(list) ? list : []).map(buildDept)
  roots.sort((a, b) => {
    const ac = a.containsCurrent ? 1 : 0
    const bc = b.containsCurrent ? 1 : 0
    if (ac !== bc) return bc - ac
    return sortByName(a.node, b.node)
  })

  // 更新响应式数据
  treeData.value = roots.map(r => r.node)
  userLabelMap.value = userMap
  userDeptMetaMap.value = deptMetaMap

  // 设置默认展开的部门链路
  // - 正常：展开当前登录人所属部门的祖先链路，便于快速定位
  // - 兜底：若当前部门未在树中匹配到（例如权限口径与树数据不一致），则默认展开一级菜单
  const hasMatchedCurrentDept = !!curDeptId && parentMap.has(curDeptId)
  defaultExpandedKeys.value = hasMatchedCurrentDept ? computeDeptExpandedKeys(curDeptId, parentMap) : roots.map(r => r.node.key)
  defaultCurrentDeptKey.value = hasMatchedCurrentDept ? `dept:${curDeptId}` : ''
  if (!currentDeptKey.value) currentDeptKey.value = defaultCurrentDeptKey.value
}

// rebuildTree end

/**
 * 重建已选人员的元信息
 * 仅为"已选人员展示"补齐 label 与部门信息
 * - 不构建 TreeV2 全量节点，避免外层筛选弹层首次渲染时做大量同步工作
 * - 只有在人员选择弹框真正打开时，才构建完整树结构
 * @param list API返回的部门节点数组
 * @param selectedUserIds 已选择的用户ID数组
 */
const rebuildSelectedMeta = (list: ApiDeptNode[] = [], selectedUserIds: string[] = []) => {
  const rawIds = Array.isArray(selectedUserIds) ? selectedUserIds : []
  const ids = rawIds.map(id => String(id || '').trim()).filter(Boolean)
  if (!ids.length) return

  const remain = new Set(ids)
  const foundUserMap = new Map<string, string>()
  const foundDeptMetaMap = new Map<string, ParentDeptMeta>()

  const stack: ApiDeptNode[] = Array.isArray(list) ? [...list] : []
  while (stack.length && remain.size > 0) {
    const dept = stack.pop() as ApiDeptNode
    const deptId = String(dept?.id || '')
    const deptName = String(dept?.name || '')
    const deptCode = dept?.code ? String(dept.code) : ''

    const accounts = Array.isArray(dept?.account) ? dept.account : []
    for (const acc of accounts) {
      const userId = String((acc as any)?.id || '')
      if (!userId || !remain.has(userId)) continue
      const userName = String((acc as any)?.name || '')
      const empNo = (acc as any)?.employeeId ? String((acc as any).employeeId) : ''
      const label = empNo ? `${userName} - ${empNo}` : userName
      foundUserMap.set(userId, label)
      foundDeptMetaMap.set(userId, { deptId, deptName, deptCode })
      remain.delete(userId)
      if (remain.size === 0) break
    }

    const children = Array.isArray(dept?.child) ? (dept.child as ApiDeptNode[]) : []
    if (children.length) stack.push(...children)
  }

  if (foundUserMap.size) {
    const next = new Map(userLabelMap.value)
    foundUserMap.forEach((v, k) => next.set(k, v))
    userLabelMap.value = next
  }
  if (foundDeptMetaMap.size) {
    const next = new Map(userDeptMetaMap.value)
    foundDeptMetaMap.forEach((v, k) => next.set(k, v))
    userDeptMetaMap.value = next
  }
}

/**
 * 计算部门展开的key数组
 * @param deptId 部门ID
 * @param parentMap 部门父级关系映射
 * @returns 需要展开的部门key数组
 */
const computeDeptExpandedKeys = (deptId: string, parentMap: Map<string, string>) => {
  const id = String(deptId || '')
  if (!id) return []
  const keys: string[] = []
  const visited = new Set<string>()
  let cur = id
  while (cur && !visited.has(cur)) {
    visited.add(cur)
    keys.push(`dept:${cur}`)
    cur = parentMap.get(cur) || ''
  }
  // 祖先在前更符合展开逻辑
  return keys.reverse()
}

// 搜索输入防抖处理
watch(
  () => keywordInput.value,
  val => {
    if (debounceTimer.value) window.clearTimeout(debounceTimer.value)
    debounceTimer.value = window.setTimeout(() => {
      keyword.value = (val || '').trim()
    }, 200)
  }
)

/**
 * 树节点过滤方法
 * 过滤：命中自己或任一后代节点即显示（搜索可通过部门名反向命中人员）
 * - 过滤逻辑在 doTreeFilter 中一次性计算，filterMethod 只做 O(1) 判定
 * @param query 搜索查询
 * @param data 节点数据
 * @returns 是否显示该节点
 */
const treeFilterMethod = (query: string, data: any) => {
  const q = normalizeQuery(query)
  if (!q) return true
  if (searchQueryNormalized.value !== q) return true
  const key = String(data?.key || '')
  return key ? !!searchVisibleKeys.value?.has(key) : true
}

/**
 * 构建搜索状态
 * @param rawQuery 原始搜索查询
 */
const buildSearchState = (rawQuery: string) => {
  const q = normalizeQuery(rawQuery)
  if (!q) {
    searchQueryNormalized.value = ''
    searchVisibleKeys.value = null
    searchExpandedKeys.value = []
    return
  }

  const visible = new Set<string>()
  const expanded = new Set<string>()

  /**
   * 递归访问节点，判断是否匹配搜索条件
   * @param node 树节点
   * @returns 是否匹配
   */
  const visit = (node: TreeNode): boolean => {
    const children = Array.isArray(node?.children) ? node.children : []
    let childHit = false
    for (const c of children) {
      if (visit(c)) childHit = true
    }

    const text = node?.searchText || normalizeQuery(String(node?.label || ''))
    const hit = !!text && text.includes(q)
    const any = hit || childHit
    if (any) visible.add(node.key)
    if (childHit && children.length) expanded.add(node.key)
    return any
  }

  for (const root of treeData.value) visit(root)

  searchQueryNormalized.value = q
  searchVisibleKeys.value = visible
  searchExpandedKeys.value = Array.from(expanded)
}

/**
 * 执行树过滤（防抖处理）
 */
const doTreeFilter = debounce((q: string) => {
  const qq = normalizeQuery(q)
  buildSearchState(qq)
  treeRef.value?.filter?.(qq)
  const exp = qq ? searchExpandedKeys.value : defaultExpandedKeys.value
  treeRef.value?.setExpandedKeys?.(exp)
}, 300)

// 监听搜索关键词变化
watch(
  () => keyword.value,
  q => doTreeFilter(q)
)

/**
 * HTML转义工具（用于 v-html 高亮）
 * @param s 待转义的字符串
 * @returns 转义后的字符串
 */
const escapeHtml = (s: string) =>
  String(s)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')

/**
 * 正则表达式转义
 * @param s 待转义的字符串
 * @returns 转义后的字符串
 */
const escapeRegExp = (s: string) => s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')

/**
 * 渲染带高亮的标签
 * @param label 标签文本
 * @returns 带高亮标记的HTML字符串
 */
const renderLabel = (label: string) => {
  const text = String(label || '')
  const q = String(keyword.value || '').trim()
  if (!q) return escapeHtml(text)
  const qLower = q.toLowerCase()
  const textLower = text.toLowerCase()
  if (!textLower.includes(qLower)) return escapeHtml(text)
  const re = new RegExp(escapeRegExp(q), 'ig')
  let last = 0
  let out = ''
  let m: RegExpExecArray | null
  while ((m = re.exec(text))) {
    out += escapeHtml(text.slice(last, m.index))
    out += `<span class="hfps-hit">${escapeHtml(m[0])}</span>`
    last = m.index + m[0].length
  }
  out += escapeHtml(text.slice(last))
  return out
}

/**
 * 从用户ID数组生成树节点key数组
 * @param ids 用户ID数组
 * @returns 树节点key数组
 */
const keysFromModel = (ids: string[]) => (Array.isArray(ids) ? ids : []).filter(Boolean).map(id => `user:${id}`)

/**
 * 限制选择数量到最大值
 * @param ids 用户ID数组
 * @param showTip 是否显示提示
 * @returns 限制后的用户ID数组
 */
const trimToMaxSelected = (ids: string[], showTip = false) => {
  const list = (Array.isArray(ids) ? ids : []).filter(Boolean)
  const maxSelected = maxSelectedCount.value
  if (list.length <= maxSelected) return list
  if (showTip) showToast(`最多选择${maxSelected}人，已自动保留前${maxSelected}人`)
  return list.slice(0, maxSelected)
}

/**
 * 根据草稿选择同步树的选中状态
 */
const syncTreeCheckedByDraft = async () => {
  await nextTick()
  const keys = keysFromModel(draftSelected.value)
  treeRef.value?.setCheckedKeys?.(keys)
}

/**
 * 打开人员选择弹框
 */
const openPopup = async () => {
  if (props.disabled) return
  popupVisible.value = true
  selectedSheetVisible.value = false
  draftSelected.value = trimToMaxSelected(committedSelected.value, true)
  keywordInput.value = ''
  keyword.value = ''
  searchQueryNormalized.value = ''
  searchVisibleKeys.value = null
  searchExpandedKeys.value = []
  // 每次打开都回到当前部门，方便快速定位
  currentDeptKey.value = defaultCurrentDeptKey.value

  // 打开弹框时再构建完整树结构，避免外层筛选弹层首次渲染被同步计算阻塞
  if (Array.isArray(props.options) && props.options.length) {
    rebuildTree(props.options)
  }

  // 如果部门人员树上次拉取失败，则在打开时自动重试一次
  if (taskEventStore.departAccountTreeFetchFailed && !taskEventStore.departAccountTreeLoading) {
    taskEventStore.fetchDepartAccountTree()
  }

  await setupResizeObserver()
  await syncTreeViewState()
}

/**
 * 关闭弹框
 */
const closePopup = () => {
  resizeObserver.value?.disconnect()
  resizeObserver.value = null
  selectedSheetVisible.value = false
  keywordInput.value = ''
  keyword.value = ''
  searchQueryNormalized.value = ''
  searchVisibleKeys.value = null
  searchExpandedKeys.value = []
}

/**
 * 处理取消操作
 */
const handleCancel = () => {
  popupVisible.value = false
}

/**
 * 处理确认操作
 */
const handleConfirm = () => {
  if (draftSelected.value.length > maxSelectedCount.value) {
    showToast(`最多选择${maxSelectedCount.value}人`)
    draftSelected.value = trimToMaxSelected(draftSelected.value)
    syncTreeCheckedByDraft()
    return
  }
  modelValue.value = [...draftSelected.value]
  popupVisible.value = false
}

/**
 * 处理清空选择
 */
const handleClearSelected = () => {
  draftSelected.value = []
  treeRef.value?.setCheckedKeys?.([])
  lastAcceptedUserIds.value = []
  lastAcceptedCheckedKeys.value = []
  selectedSheetVisible.value = false
}

/**
 * 处理移除选中的人员
 * @param userId 用户ID
 */
const handleRemoveSelected = async (userId: string) => {
  const id = String(userId || '')
  if (!id) return
  draftSelected.value = (draftSelected.value || []).filter(x => String(x || '') !== id)
  await syncTreeCheckedByDraft()
  lastAcceptedUserIds.value = [...draftSelected.value]
  lastAcceptedCheckedKeys.value = keysFromModel(draftSelected.value)
}

/**
 * 打开已选人员列表
 */
const openSelectedSheet = () => {
  if (!selectedItems.value.length) return
  selectedSheetVisible.value = true
}

/**
 * 处理树节点选中状态变化
 * @param _data 节点数据
 * @param checkedInfo 选中信息
 */
const handleCheckedChange = (_data?: any, checkedInfo?: any) => {
  const rawKeys = (checkedInfo?.checkedKeys || treeRef.value?.getCheckedKeys?.() || []) as any[]
  const keys = rawKeys.map(k => String(k || '')).filter(Boolean)
  const ids = keys
    .filter(k => k.startsWith('user:'))
    .map(k => k.slice('user:'.length))
    .filter(Boolean)

  if (ids.length > maxSelectedCount.value) {
    showToast(`最多选择${maxSelectedCount.value}人`)
    draftSelected.value = [...lastAcceptedUserIds.value]
    nextTick(() => {
      treeRef.value?.setCheckedKeys?.(lastAcceptedCheckedKeys.value)
    })
    return
  }

  draftSelected.value = ids
  lastAcceptedUserIds.value = ids
  lastAcceptedCheckedKeys.value = keysFromModel(ids)
}

/**
 * 处理树节点点击
 * @param data 节点数据
 */
const handleNodeClick = (data: any) => {
  if (data?.type !== 'dept') return
  currentDeptKey.value = String(data?.key || '')
}

// 监听选项数据变化
watch(
  () => props.options,
  async val => {
    const list = Array.isArray(val) ? (val as any) : []
    // 未打开人员选择弹框时，避免构建全量 TreeV2（成本高，且外层筛选弹层首次打开会被拖慢）
    if (!popupVisible.value) {
      if (committedSelected.value.length) rebuildSelectedMeta(list, committedSelected.value)
      return
    }

    rebuildTree(list as any)
    // options 切换后，搜索状态需要重算一次（避免使用旧 key 集合）
    if (keyword.value) buildSearchState(keyword.value)
    if (props.loading) return
    await setupResizeObserver()
    await syncTreeViewState()
  },
  { immediate: true }
)

// 监听已提交选择变化
watch(
  () => committedSelected.value,
  ids => {
    if (popupVisible.value) return
    if (!Array.isArray(ids) || ids.length === 0) return
    rebuildSelectedMeta(props.options as any, ids)
  },
  { deep: true }
)

// 监听加载状态变化
watch(
  () => props.loading,
  async loading => {
    if (!popupVisible.value) return
    if (loading) return
    await setupResizeObserver()
    await syncTreeViewState()
  }
)

// 监听已选项目数量变化
watch(
  () => selectedItems.value.length,
  len => {
    if (len > 0) return
    selectedSheetVisible.value = false
  }
)

// 监听v-model值变化
watch(
  () => modelValue.value,
  () => {
    if (!popupVisible.value) return
    draftSelected.value = trimToMaxSelected(committedSelected.value, true)
    syncTreeCheckedByDraft()
  },
  { deep: true }
)

// 组件卸载前清理
onBeforeUnmount(() => {
  resizeObserver.value?.disconnect()
  resizeObserver.value = null
  if (debounceTimer.value) window.clearTimeout(debounceTimer.value)
  doTreeFilter.cancel?.()
})
</script>

<template>
  <div class="h-filter-personnel-select">
    <div
      class="hfps-trigger"
      :class="{ 'is-disabled': disabled, 'is-placeholder': !committedSelected.length }"
      @click="openPopup"
    >
      <div class="hfps-trigger__text van-ellipsis">{{ selectedText }}</div>
      <van-icon class="hfps-trigger__icon" name="arrow-down" />
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
      <div class="hfps-popup">
        <div class="hfps-popup__header">
          <div class="hfps-popup__action is-left" @click="handleCancel">取消</div>
          <div class="hfps-popup__title van-ellipsis">{{ title }}</div>
          <div class="hfps-popup__action is-right" @click="handleConfirm">完成</div>
        </div>

        <div v-if="searchable" class="hfps-popup__search">
          <van-search v-model="keywordInput" placeholder="搜索部门/人员" clearable shape="round" :show-action="false" />
        </div>

        <div class="hfps-popup__content" :class="{ 'has-selected-bar': selectedItems.length }">
          <div v-if="loading" class="hfps-loading flex-center">
            <van-loading size="24px" color="#1677FF">加载中...</van-loading>
          </div>

          <div v-else ref="treeWrapRef" class="hfps-tree">
            <div v-if="!treeData.length" class="hfps-empty">
              <van-empty description="暂无数据" />
            </div>
            <el-tree-v2
              v-else
              ref="treeRef"
              class="hfps-tree__inner pt-10"
              :data="treeData"
              :props="treeProps"
              :filter-method="treeFilterMethod"
              show-checkbox
              highlight-current
              :expand-on-click-node="true"
              :default-expanded-keys="defaultExpandedKeys"
              :current-node-key="currentDeptKey"
              :height="treeHeight"
              @check="handleCheckedChange"
              @node-click="handleNodeClick"
            >
              <template #default="{ node, data }">
                <div class="hfps-node">
                  <el-icon v-if="node.isLeaf && data.employeeId" color="#999" class="mr-4"
                    ><UserFilled
                  /></el-icon>
                  <span class="hfps-node__label" v-html="renderLabel(data.label)"></span>
                </div>
              </template>
            </el-tree-v2>
          </div>
        </div>

        <div v-if="selectedItems.length" class="hfps-selected-bar">
          <div class="hfps-selected-bar__text" @click="openSelectedSheet">已选： {{ selectedItems.length }} 人
            <van-icon class="hfps-selected-bar__icon ml-5" name="arrow-up" />
          </div>
          <div class="hfps-selected-bar__right">
            <div class="hfps-selected-bar__clear" @click.stop="handleClearSelected">清空</div>
          </div>
        </div>
      </div>

      <van-popup
        v-model:show="selectedSheetVisible"
        position="bottom"
        round
        :safe-area-inset-bottom="true"
        :lock-scroll="true"
        :style="{ height: '60%' }"
        teleport="body"
      >
        <div class="hfps-selected-panel">
          <div class="hfps-selected-panel__header">
            <div class="hfps-selected-panel__title">已选（{{ selectedItems.length }}）</div>
            <div class="hfps-selected-panel__actions">
              <div class="hfps-selected-panel__clear mr-20" @click.stop="handleClearSelected">清空</div>
              <div class="hfps-selected-panel__close" @click.stop="selectedSheetVisible = false">关闭</div>
            </div>
          </div>

          <div class="hfps-selected-panel__list">
            <div v-for="group in selectedGroups" :key="group.deptId || group.deptName" class="hfps-selected-group">
              <div class="hfps-selected-group__title">{{ group.deptName }}</div>
              <div class="hfps-selected-group__tags">
                <van-tag
                  v-for="item in group.items"
                  :key="item.userId"
                  type="primary"
                  closeable
                  size="medium"
                  @close="handleRemoveSelected(item.userId)"
                >
                  {{ item.label }}
                </van-tag>
              </div>
            </div>
          </div>
        </div>
      </van-popup>
    </van-popup>
  </div>
</template>

<style scoped lang="scss">
.h-filter-personnel-select {
  width: 100%;
}

.hfps-trigger {
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
    .hfps-trigger__text {
      color: #c9cdd4;
    }
  }
}

.hfps-trigger__text {
  flex: 1;
  min-width: 0;
  font-size: 12px;
  color: #1f2733;
}

.hfps-trigger__icon {
  flex: none;
  color: #86909c;
  font-size: 14px;
}

.hfps-popup {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
  position: relative;
  padding-bottom: 20px;
}

.hfps-popup__header {
  height: 44px;
  padding: 0 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #f2f3f5;
}

.hfps-popup__title {
  flex: 1;
  min-width: 0;
  text-align: center;
  font-weight: 500;
  font-size: 14px;
  color: #1f2733;
}

.hfps-popup__action {
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

.hfps-popup__search {
  // padding: 10px 12px 8px;
}

.hfps-popup__content {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  padding: 0 12px 12px;
}

.hfps-popup__content.has-selected-bar {
  padding-bottom: 64px;
}

.hfps-loading,
.hfps-empty {
  flex: 1;
  min-height: 0;
}

.hfps-tree {
  flex: 1;
  min-height: 0;
}

.hfps-selected-bar {
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

.hfps-selected-bar__text {
  flex: 1;
  min-width: 0;
  font-weight: 500;
  font-size: 14px;
  color: #1F2733;
}

.hfps-selected-bar__right {
  flex: none;
  display: flex;
  align-items: center;
  gap: 10px;
}

.hfps-selected-bar__clear {
  flex: none;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  border: 1px solid #f2f3f5;
  color: #1677ff;
  background: #fff;
  user-select: none;
}

.hfps-selected-bar__icon {
  flex: none;
  color: #86909c;
  font-size: 14px;
}

.hfps-selected-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 12px 12px calc(12px + env(safe-area-inset-bottom));
  background: #fff;
}

.hfps-selected-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f2f3f5;
}

.hfps-selected-panel__title {
  flex: 1;
  min-width: 0;
  font-size: 14px;
  font-weight: 500;
  color: #1f2733;
}

.hfps-selected-panel__actions {
  flex: none;
  display: flex;
  align-items: center;
  gap: 12px;
}

.hfps-selected-panel__clear,
.hfps-selected-panel__close {
  font-size: 14px;
  color: #1677ff;
  user-select: none;
}

.hfps-selected-panel__list {
  flex: 1;
  min-height: 0;
  padding-top: 12px;
  overflow: auto;
}

.hfps-selected-group {
  padding-bottom: 12px;
}

.hfps-selected-group__title {
  font-size: 12px;
  color: #86909c;
  margin-bottom: 8px;
}

.hfps-selected-group__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.hfps-tree__inner {
  width: 100%;
}

.hfps-node {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.hfps-node__label {
  flex: 1;
  min-width: 0;
  font-size: 13px;
  color: #1f2733;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  padding: 2px 0;
}

.hfps-hit {
  background: #fff2cc;
  color: #d48806;
  padding: 0 1px;
  border-radius: 2px;
}
</style>
