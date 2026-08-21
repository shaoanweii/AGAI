<script setup lang="ts">
import { computed, watch } from 'vue'
import HListSingleSelect from '@h5/components/UI/HListSingleSelect'
import HFilterMultiSelect from './HFilterMultiSelect.vue'

defineOptions({ name: 'HFilterLinkedCascadeSelect' })

export type HFilterLinkedCascadeFields = {
  /** 展示字段 */
  label: string
  /** 值字段 */
  value: string
  /** 子节点字段 */
  children?: string
}

export type HFilterLinkedCascadeLevel = {
  /** single：单选；multi：多选 */
  type: 'single' | 'multi'
  /** 弹框标题 */
  title?: string
  /** 占位 */
  placeholder?: string
  /** 单级禁用（会叠加外部 disabled 与联动禁用） */
  disabled?: boolean
}

type CascadeValue = string | string[] | null

const props = withDefaults(
  defineProps<{
    /** 根节点列表（树结构） */
    options?: any[]
    /** 字段映射 */
    fields?: HFilterLinkedCascadeFields
    /** 级联层级配置（按顺序渲染多个选择框） */
    levels: HFilterLinkedCascadeLevel[]
    /** 全局禁用 */
    disabled?: boolean
  }>(),
  {
    options: () => [],
    fields: () => ({
      label: 'label',
      value: 'value',
      children: 'child'
    }),
    disabled: false
  }
)

/**
 * v-model：级联每一级的值（按 levels 顺序）
 * - 单选级：string | null
 * - 多选级：string[]
 */
const modelValue = defineModel<CascadeValue[]>({ default: () => [] })

const levels = computed(() => (Array.isArray(props.levels) ? props.levels : []))

const mergedFields = computed(() => {
  return {
    label: props.fields?.label || 'label',
    value: props.fields?.value || 'value',
    children: props.fields?.children || 'child'
  }
})

const normalizeValues = (input: any): CascadeValue[] => {
  const levels = Array.isArray(props.levels) ? props.levels : []
  const base = Array.isArray(input) ? input : []
  const out: CascadeValue[] = []
  for (let i = 0; i < levels.length; i++) {
    const lv = levels[i]
    const raw = base[i]
    if (lv.type === 'multi') {
      if (Array.isArray(raw)) out[i] = raw.filter(Boolean).map(String)
      else if (raw == null || raw === '') out[i] = []
      else out[i] = [String(raw)]
    } else {
      if (raw == null || raw === '') out[i] = null
      else if (Array.isArray(raw)) out[i] = raw.length ? String(raw[0]) : null
      else out[i] = String(raw)
    }
  }
  return out
}

const isSame = (a: CascadeValue[], b: CascadeValue[]) => {
  if (a.length !== b.length) return false
  for (let i = 0; i < a.length; i++) {
    const av = a[i]
    const bv = b[i]
    const aIsArr = Array.isArray(av)
    const bIsArr = Array.isArray(bv)
    if (aIsArr !== bIsArr) return false
    if (aIsArr && bIsArr) {
      const aa = av as string[]
      const bb = bv as string[]
      if (aa.length !== bb.length) return false
      for (let j = 0; j < aa.length; j++) {
        if (aa[j] !== bb[j]) return false
      }
      continue
    }
    if (String(av ?? '') !== String(bv ?? '')) return false
  }
  return true
}

const fixedValues = computed(() => normalizeValues(modelValue.value))

const findNodeByValue = (list: any[], value: string) => {
  const valueKey = mergedFields.value.value
  return (Array.isArray(list) ? list : []).find(item => String(item?.[valueKey] ?? '') === value) || null
}

const getChildren = (node: any) => {
  const childrenKey = mergedFields.value.children
  const children = node?.[childrenKey]
  return Array.isArray(children) ? children : []
}

const optionsByLevel = computed(() => {
  const roots = Array.isArray(props.options) ? props.options : []
  const vals = fixedValues.value
  const res: any[][] = []
  res[0] = roots
  for (let i = 1; i < levels.value.length; i++) {
    const prev = vals[i - 1]
    if (typeof prev !== 'string' || !prev) {
      res[i] = []
      continue
    }
    const parentNode = findNodeByValue(res[i - 1], prev)
    res[i] = parentNode ? getChildren(parentNode) : []
  }
  return res
})

const disabledByLevel = computed(() => {
  const vals = fixedValues.value
  return levels.value.map((lv, idx) => {
    if (props.disabled) return true
    if (lv.disabled) return true
    if (idx === 0) return false
    const parent = vals[idx - 1]
    return typeof parent !== 'string' || !parent
  })
})

const applyCascadeChange = (index: number, next: CascadeValue) => {
  const oldVals = fixedValues.value
  const nextVals = normalizeValues(oldVals)
  const lv = levels.value[index]
  if (lv?.type === 'multi') {
    nextVals[index] = Array.isArray(next) ? next.filter(Boolean).map(String) : []
  } else {
    if (next == null || next === '') nextVals[index] = null
    else if (Array.isArray(next)) nextVals[index] = next.length ? String(next[0]) : null
    else nextVals[index] = String(next)
  }

  // 切换上一级：清空后面几级
  for (let i = index + 1; i < levels.value.length; i++) {
    nextVals[i] = levels.value[i].type === 'multi' ? [] : null
  }

  if (!isSame(oldVals, nextVals)) {
    modelValue.value = nextVals
  }
}

// 外部直接改 v-model 时，做一次联动修正（保证“父级为空则子级清空”）
watch(
  () => modelValue.value,
  val => {
    const normalized = normalizeValues(val)
    const next = normalizeValues(normalized)
    for (let i = 1; i < levels.value.length; i++) {
      const parent = next[i - 1]
      if (typeof parent !== 'string' || !parent) {
        for (let j = i; j < levels.value.length; j++) {
          next[j] = levels.value[j].type === 'multi' ? [] : null
        }
        break
      }
    }
    if (!isSame(normalized, next)) {
      modelValue.value = next
    } else if (!isSame(val as any, normalized)) {
      modelValue.value = normalized
    }
  },
  { deep: true, immediate: true }
)
</script>

<template>
  <div class="h-filter-linked-cascade">
    <div
      v-for="(lv, idx) in levels"
      :key="idx"
      class="h-filter-linked-cascade__item"
    >
      <HFilterMultiSelect
        v-if="lv.type === 'multi'"
        :model-value="(fixedValues[idx] as string[]) || []"
        :title="lv.title || '请选择'"
        :placeholder="lv.placeholder || '请选择'"
        :options="optionsByLevel[idx] || []"
        :fields="{
          label: mergedFields.label,
          value: mergedFields.value
        }"
        :disabled="disabledByLevel[idx]"
        @update:modelValue="val => applyCascadeChange(idx, Array.isArray(val) ? val : [])"
      />
      <HListSingleSelect
        v-else
        :model-value="(fixedValues[idx] as string | null) ?? null"
        :title="lv.title || '请选择'"
        :placeholder="lv.placeholder || '请选择'"
        :options="optionsByLevel[idx] || []"
        :fields="{
          label: mergedFields.label,
          value: mergedFields.value
        }"
        :disabled="disabledByLevel[idx]"
        @update:modelValue="val => applyCascadeChange(idx, (val as any) ?? null)"
      />
    </div>
  </div>
</template>

<style scoped lang="scss">
.h-filter-linked-cascade {
  width: 100%;
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: flex-end;
}

.h-filter-linked-cascade__item {
  flex: 1;
  min-width: 0;
}
</style>
