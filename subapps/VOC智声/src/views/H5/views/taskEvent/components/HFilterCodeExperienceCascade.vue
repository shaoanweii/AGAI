<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import HFilterLinkedCascadeSelect, {
  type HFilterLinkedCascadeFields,
  type HFilterLinkedCascadeLevel
} from './HFilterLinkedCascadeSelect.vue'

defineOptions({ name: 'HFilterCodeExperienceCascade' })

type ApiTagNode = {
  id: string
  tagParentId?: string
  tagName: string
  tagNameEn?: string | null
  tagType?: string
  tagCode: string
  checked?: boolean
  sort?: number
  child?: ApiTagNode[] | null
}

type CascadeValue = string | string[] | null

const props = withDefaults(
  defineProps<{
    /** 全领域业务标签树（全量） */
    options?: ApiTagNode[]
    /** 字段映射 */
    fields?: HFilterLinkedCascadeFields
    disabled?: boolean
  }>(),
  {
    options: () => [],
    fields: () => ({
      label: 'tagName',
      value: 'tagCode',
      children: 'child'
    }),
    disabled: false
  }
)

// 体验代码：1-3 级单选，4 级多选
const firstCode = defineModel<string>('firstCode', { default: '' })
const secondCode = defineModel<string>('secondCode', { default: '' })
const thirdCode = defineModel<string>('thirdCode', { default: '' })
const fourCodes = defineModel<string[]>('fourCodes', { default: () => [] })

const levels = computed<HFilterLinkedCascadeLevel[]>(() => [
  { type: 'single', title: '一级标签', placeholder: '请选择' },
  { type: 'single', title: '二级标签', placeholder: '请选择' },
  { type: 'single', title: '三级标签', placeholder: '请选择' },
  { type: 'multi', title: '四级标签', placeholder: '请选择' }
])

const cascadeValues = ref<CascadeValue[]>([null, null, null, []])

const findPathByCode = (list: ApiTagNode[], targetCode: string, maxDepth = 4): ApiTagNode[] | null => {
  const walk = (nodes: ApiTagNode[], depth: number): ApiTagNode[] | null => {
    if (!Array.isArray(nodes) || nodes.length === 0) return null
    for (const node of nodes) {
      if (String(node?.tagCode || '') === targetCode) return [node]
      if (depth >= maxDepth) continue
      const children = Array.isArray(node?.child) ? node.child : []
      const sub = walk(children, depth + 1)
      if (sub) return [node, ...sub]
    }
    return null
  }
  return walk(list, 1)
}

const deriveCascadeFromModels = () => {
  // 优先用最深层的值回推路径，保证外部只回填了某一级时也能显示完整链路
  const four = Array.isArray(fourCodes.value) ? fourCodes.value.filter(Boolean).map(String) : []
  const deepest = four[0] || thirdCode.value || secondCode.value || firstCode.value || ''

  const next: CascadeValue[] = [null, null, null, []]

  if (!deepest) return next

  const path = findPathByCode(props.options || [], String(deepest), 4)
  if (path && path.length >= 1) next[0] = String(path[0].tagCode)
  if (path && path.length >= 2) next[1] = String(path[1].tagCode)
  if (path && path.length >= 3) next[2] = String(path[2].tagCode)

  // 四级多选：以外部 fourCodes 为准；外部为空但 deepest 是四级时，补一个单选回显
  if (four.length > 0) next[3] = four
  else if (path && path.length >= 4) next[3] = [String(path[3].tagCode)]
  else next[3] = []

  return next
}

const syncCascadeToModels = (vals: CascadeValue[]) => {
  const v1 = typeof vals?.[0] === 'string' ? (vals[0] as string) : ''
  const v2 = typeof vals?.[1] === 'string' ? (vals[1] as string) : ''
  const v3 = typeof vals?.[2] === 'string' ? (vals[2] as string) : ''
  const v4 = Array.isArray(vals?.[3]) ? ((vals[3] as string[]).filter(Boolean).map(String) as string[]) : []

  if (firstCode.value !== v1) firstCode.value = v1
  if (secondCode.value !== v2) secondCode.value = v2
  if (thirdCode.value !== v3) thirdCode.value = v3
  // 四级多选：按顺序对比，减少不必要的触发
  const cur4 = Array.isArray(fourCodes.value) ? fourCodes.value : []
  const same4 = cur4.length === v4.length && cur4.every((x, i) => String(x) === String(v4[i]))
  if (!same4) fourCodes.value = v4
}

const syncing = ref(false)

watch(
  () => [firstCode.value, secondCode.value, thirdCode.value, fourCodes.value] as const,
  () => {
    if (syncing.value) return
    syncing.value = true
    try {
      cascadeValues.value = deriveCascadeFromModels()
    } finally {
      syncing.value = false
    }
  },
  { deep: true, immediate: true }
)

watch(
  () => props.options,
  () => {
    if (syncing.value) return
    syncing.value = true
    try {
      cascadeValues.value = deriveCascadeFromModels()
    } finally {
      syncing.value = false
    }
  },
  { deep: false }
)

watch(
  () => cascadeValues.value,
  vals => {
    if (syncing.value) return
    syncing.value = true
    try {
      syncCascadeToModels(vals)
    } finally {
      syncing.value = false
    }
  },
  { deep: true }
)
</script>

<template>
  <HFilterLinkedCascadeSelect
    v-model="cascadeValues"
    :options="options"
    :fields="fields"
    :levels="levels"
    :disabled="disabled"
  />
</template>
