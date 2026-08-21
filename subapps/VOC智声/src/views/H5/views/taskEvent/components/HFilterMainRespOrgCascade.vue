<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import HFilterLinkedCascadeSelect, {
  type HFilterLinkedCascadeFields,
  type HFilterLinkedCascadeLevel
} from './HFilterLinkedCascadeSelect.vue'

defineOptions({ name: 'HFilterMainRespOrgCascade' })

type ApiDeptNode = {
  id: string
  name: string
  code?: string
  parentId?: string
  child?: ApiDeptNode[] | null
}

type CascadeValue = string | string[] | null

const props = withDefaults(
  defineProps<{
    /** 部门-人员树（取全量数据，仅使用部门节点） */
    options?: ApiDeptNode[]
    /** 字段映射 */
    fields?: HFilterLinkedCascadeFields
    /** 当前账号部门（用于优先展示） */
    currentDeptId?: string
    currentDeptCode?: string
    /** 外部禁用/加载态 */
    disabled?: boolean
    loading?: boolean
  }>(),
  {
    options: () => [],
    fields: () => ({
      label: 'name',
      value: 'id',
      children: 'child'
    }),
    currentDeptId: '',
    currentDeptCode: '',
    disabled: false,
    loading: false
  }
)

/**
 * v-model：主责部门
 * - 空数组代表“不限”
 * - 选择到 1/2 级：返回 [id]
 * - 选择到 3 级（多选）：返回 id 数组
 */
const modelValue = defineModel<string[]>({ default: () => [] })

const levels = computed<HFilterLinkedCascadeLevel[]>(() => [
  { type: 'single', title: '一级部门', placeholder: '请选择' },
  { type: 'single', title: '二级部门', placeholder: '请选择' },
  { type: 'multi', title: '三级部门', placeholder: '请选择' }
])

const sortByName = (a: { name?: string }, b: { name?: string }) =>
  String(a?.name || '').localeCompare(String(b?.name || ''), 'zh-Hans-CN', { sensitivity: 'base' })

const buildSortedTree = (list: ApiDeptNode[] = []) => {
  const curId = String(props.currentDeptId || '')
  const curCode = String(props.currentDeptCode || '')

  const isCurrent = (node: ApiDeptNode) => {
    const id = String(node?.id || '')
    const code = String(node?.code || '')
    return (curId && id === curId) || (curCode && code === curCode)
  }

  const dfs = (node: ApiDeptNode): { node: ApiDeptNode; containsCurrent: boolean } => {
    const rawChildren = Array.isArray(node?.child) ? node.child : []
    const builtChildren = rawChildren.map(dfs)
    builtChildren.sort((a, b) => {
      const ac = a.containsCurrent ? 1 : 0
      const bc = b.containsCurrent ? 1 : 0
      if (ac !== bc) return bc - ac
      return sortByName(a.node, b.node)
    })

    const nextNode: ApiDeptNode = {
      ...node,
      child: builtChildren.map(x => x.node)
    }

    const containsCurrent = isCurrent(node) || builtChildren.some(x => x.containsCurrent)
    return { node: nextNode, containsCurrent }
  }

  const built = (Array.isArray(list) ? list : []).map(dfs)
  built.sort((a, b) => {
    const ac = a.containsCurrent ? 1 : 0
    const bc = b.containsCurrent ? 1 : 0
    if (ac !== bc) return bc - ac
    return sortByName(a.node, b.node)
  })
  return built.map(x => x.node)
}

const sortedDeptTree = computed(() => buildSortedTree(props.options || []))

// 组件内部的级联值： [一级, 二级, 三级(多选)]
const cascadeValues = ref<CascadeValue[]>([null, null, []])

const findPathById = (list: ApiDeptNode[], targetId: string, maxDepth = 3): ApiDeptNode[] | null => {
  const walk = (nodes: ApiDeptNode[], depth: number): ApiDeptNode[] | null => {
    if (!Array.isArray(nodes) || nodes.length === 0) return null
    for (const node of nodes) {
      if (String(node?.id || '') === targetId) return [node]
      if (depth >= maxDepth) continue
      const children = Array.isArray(node?.child) ? node.child : []
      const sub = walk(children, depth + 1)
      if (sub) return [node, ...sub]
    }
    return null
  }
  return walk(list, 1)
}

const deriveCascadeFromModel = (val: string[]) => {
  const next: CascadeValue[] = [null, null, []]
  const ids = (Array.isArray(val) ? val : []).filter(Boolean).map(String)
  if (ids.length === 0) return next

  // 多选：默认视为“三级部门多选”，以第一个值推导路径
  if (ids.length > 1) {
    const path = findPathById(sortedDeptTree.value as any, ids[0], 3)
    if (path && path.length >= 1) next[0] = String(path[0].id)
    if (path && path.length >= 2) next[1] = String(path[1].id)
    next[2] = ids
    return next
  }

  // 单选：通过 id 在树中的深度判断选到了第几级
  const id = ids[0]
  const path = findPathById(sortedDeptTree.value as any, id, 3)
  if (!path) {
    // 找不到时只保留原值在一级（避免直接丢失）
    next[0] = id
    return next
  }

  if (path.length >= 1) next[0] = String(path[0].id)
  if (path.length >= 2) next[1] = String(path[1].id)
  if (path.length >= 3) next[2] = [String(path[2].id)]
  return next
}

const deriveModelFromCascade = (vals: CascadeValue[]) => {
  const v1 = typeof vals?.[0] === 'string' ? (vals[0] as string) : ''
  const v2 = typeof vals?.[1] === 'string' ? (vals[1] as string) : ''
  const v3 = Array.isArray(vals?.[2]) ? ((vals[2] as string[]).filter(Boolean).map(String) as string[]) : []
  if (v3.length > 0) return v3
  if (v2) return [v2]
  if (v1) return [v1]
  return []
}

const syncFromModelToCascade = () => {
  cascadeValues.value = deriveCascadeFromModel(modelValue.value)
}

// 外部值变更（重置/回填）时同步到级联 UI
watch(
  () => modelValue.value,
  () => syncFromModelToCascade(),
  { deep: true, immediate: true }
)

// 数据源加载完成/变更后，重新推导一次路径（用于回显）
watch(
  () => sortedDeptTree.value,
  () => syncFromModelToCascade(),
  { deep: false }
)

// 内部级联变更时反向写回主责部门 v-model
watch(
  () => cascadeValues.value,
  vals => {
    const next = deriveModelFromCascade(vals)
    const cur = Array.isArray(modelValue.value) ? modelValue.value : []
    const isSame =
      Array.isArray(next) &&
      cur.length === next.length &&
      cur.every((x, i) => String(x) === String(next[i]))
    if (!isSame) modelValue.value = next
  },
  { deep: true }
)
</script>

<template>
  <HFilterLinkedCascadeSelect
    v-model="cascadeValues"
    :options="sortedDeptTree"
    :fields="fields"
    :levels="levels"
    :disabled="disabled || loading"
  />
</template>
