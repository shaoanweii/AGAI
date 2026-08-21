<script setup lang="ts">
import { ref, watch } from 'vue'

// 中文注释：单选级联组件，统一输出 {层级:id} 结构对象
type CascaderSingleValue = Record<string, any> | null

// v-model：外部期望绑定到 row.value，类型为对象或 null
const model = defineModel<CascaderSingleValue>()

const props = withDefaults(
  defineProps<{
    options: any[]
    placeholder?: string
    disabled?: boolean
    props: {
      value: string
      label: string
      children: string
    }
  }>(),
  {
    options: () => [],
    placeholder: '请选择',
    disabled: false,
    props: () => ({
      value: 'tagCode',
      label: 'tagName',
      children: 'child'
    })
  }
)

const emit = defineEmits<{
  (e: 'change', value: CascaderSingleValue): void
}>()

// 内部使用的 el-cascader 选中值（emitPath=false 时为末级 id）
const innerValue = ref<any>('')

// 在 options 树中按 id 查找路径，返回从根到目标节点的数组
const findPathById = (nodes: any[], targetId: any, path: any[] = []): any[] => {
  if (!Array.isArray(nodes)) return []
  for (const node of nodes) {
    const currentPath = [...path, node]
    if (String((node as any)[props.props.value]) === String(targetId)) return currentPath
    const children = Array.isArray((node as any)[props.props.children])
      ? (node as any)[props.props.children]
      : []
    const childPath = findPathById(children, targetId, currentPath)
    if (childPath.length) return childPath
  }
  return []
}

// 根据选中的 id 构造 {层级:id} 对象，层级从 1 开始递增
const buildValueObject = (id: any): CascaderSingleValue => {
  if (id === undefined || id === null || id === '') return null
  const tree = props.options || []
  const pathNodes = findPathById(tree, id)
  if (!pathNodes.length) {
    // 兜底：找不到完整路径时，至少保留一层
    return { '1': id }
  }

  const valueObj: Record<string, any> = {}
  pathNodes.forEach((node: any, index: number) => {
    if (
      index === pathNodes.length - 1 &&
      node &&
      node[props.props.value] !== undefined &&
      node[props.props.value] !== null &&
      node[props.props.value] !== ''
    ) {
      valueObj[String(index + 1)] = node[props.props.value]
    }
  })
  return Object.keys(valueObj).length ? valueObj : null
}

// 从 {层级:id} 对象中还原当前选中的末级 id
const resolveSelectedIdFromObject = (obj: any): any => {
  if (!obj || typeof obj !== 'object') return ''
  const keys = Object.keys(obj)
    .filter(k => !Number.isNaN(Number(k)))
    .sort((a, b) => Number(a) - Number(b))
  if (!keys.length) return ''
  const lastKey = keys[keys.length - 1]
  return (obj as any)[lastKey]
}

// v-model 外部变更时，同步更新内部级联选中值
watch(
  () => model.value,
  v => {
    if (v && typeof v === 'object') {
      // 新结构：{层级:id}，直接按对象回显
      innerValue.value = resolveSelectedIdFromObject(v)
    } else if (v !== null && v !== undefined && String(v) !== '') {
      // 兼容旧数据：可能是直接存储的末级 id，这里转为对象结构
      const obj = buildValueObject(v)
      model.value = obj
      innerValue.value = resolveSelectedIdFromObject(obj)
    } else {
      innerValue.value = ''
    }
  },
  { immediate: true }
)

// options 变更时，基于当前 v-model 再做一次回显并尝试补全路径
watch(
  () => props.options,
  () => {
    const selectedId = resolveSelectedIdFromObject(model.value)
    if (!selectedId) {
      innerValue.value = ''
      return
    }
    const obj = buildValueObject(selectedId)
    model.value = obj
    innerValue.value = resolveSelectedIdFromObject(obj)
  }
)

// 级联选中变化时，构造对象并通过 v-model 往外同步
const onChange = (val: any) => {
  // el-cascader 在 emitPath=false 下，val 为末级节点 id
  const valueObj = buildValueObject(val)
  model.value = valueObj
  emit('change', valueObj)
}
</script>

<template>
  <el-cascader
    v-model="innerValue"
    :options="props.options"
    :props="{ checkStrictly: true, emitPath: false, multiple: false, ...props.props }"
    :placeholder="props.placeholder"
    :disabled="props.disabled"
    clearable
    filterable
    class="w-full"
    @change="onChange"
  />
</template>
