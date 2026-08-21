<script setup lang="ts">
import { computed, watchEffect } from 'vue'
import type {
  HSelectTabCode,
  HSelectTabOption,
  HSelectTabsEmits,
  HSelectTabsProps,
  HSelectTabsValue
} from './types'

defineOptions({ name: 'HSelectTabs' })

const props = withDefaults(defineProps<HSelectTabsProps>(), {
  options: () => [],
  multiSelect: false,
  disabled: false,
  customClass: '',
  allLabel: '全部',
  allCode: '' as unknown as HSelectTabCode,
  fields: () => ({
    code: 'code',
    name: 'name'
  })
})

const emit = defineEmits<HSelectTabsEmits>()

// v-model：
// - 单选：code；“全部”用空字符串表示
// - 多选：code[]；“全部/不限”用空数组表示（兼容历史的 [allCode] 形态）
// 注意：defineModel 的 default 不能依赖 props（会被提升到 setup 外），默认值统一交给后续规范化逻辑处理
const modelValue = defineModel<HSelectTabsValue>({
  default: '' as unknown as HSelectTabsValue
})

const dedupePreserveOrder = (list: HSelectTabCode[]) => {
  const seen = new Set<HSelectTabCode>()
  const result: HSelectTabCode[] = []
  for (const item of list) {
    if (seen.has(item)) continue
    seen.add(item)
    result.push(item)
  }
  return result
}

// 内部统一后的选项（自动注入“全部”）
const innerOptions = computed<(HSelectTabOption & { isAll?: boolean })[]>(() => {
  const codeKey = props.fields?.code || 'code'
  const nameKey = props.fields?.name || 'name'
  const list = Array.isArray(props.options) ? props.options : []

  const mapped = list
    .map((raw: any) => ({
      code: raw?.[codeKey] as HSelectTabCode,
      name: raw?.[nameKey] as string,
      disabled: raw?.disabled as boolean | undefined
    }))
    .filter(item => item.code !== undefined && item.name !== undefined)

  const hasAll = mapped.some(item => item.code === props.allCode)
  if (hasAll) return mapped

  return [
    {
      code: props.allCode,
      name: props.allLabel,
      isAll: true
    },
    ...mapped
  ]
})

// 当前有效的 code 列表
const availableCodes = computed<HSelectTabCode[]>(() => innerOptions.value.map(o => o.code))

const nonAllCodes = computed<HSelectTabCode[]>(() => {
  return availableCodes.value.filter(c => c !== props.allCode)
})

// 规范化后的 v-model（对外值）
const normalizedModelValue = computed<HSelectTabsValue>(() => {
  const raw = modelValue.value

  if (props.multiSelect) {
    const arr = Array.isArray(raw)
      ? raw.slice()
      : raw === undefined || raw === null || raw === ''
        ? []
        : [raw]

    // 外部语义：空数组代表“全部/不限”，因此这里强制剔除 allCode
    const cleaned = arr.filter(c => c !== props.allCode)
    const uniq = dedupePreserveOrder(cleaned).filter(c => nonAllCodes.value.includes(c))

    // 选中所有非“全部”项，语义上等同“全部/不限”
    if (
      uniq.length === nonAllCodes.value.length &&
      nonAllCodes.value.every(c => uniq.includes(c))
    ) {
      return [] as unknown as HSelectTabsValue
    }

    return uniq as unknown as HSelectTabsValue
  }

  // 单选：空字符串代表“全部”；同时兼容外部传入 allCode 的旧形态
  const single = Array.isArray(raw) ? raw[0] : raw
  if (single === '' || single === undefined || single === null) {
    return '' as unknown as HSelectTabsValue
  }
  if (single === props.allCode) {
    return '' as unknown as HSelectTabsValue
  }
  return nonAllCodes.value.includes(single as HSelectTabCode)
    ? (single as unknown as HSelectTabsValue)
    : ('' as unknown as HSelectTabsValue)
})

// 规范化后的选中值（内部用于高亮，统一为数组处理；“全部”内部用 allCode 表示）
const normalizedSelected = computed<HSelectTabCode[]>(() => {
  const allCode = props.allCode
  if (props.multiSelect) {
    const external = normalizedModelValue.value
    const list = Array.isArray(external) ? external : []
    return list.length > 0 ? list : [allCode]
  }

  const external = normalizedModelValue.value
  const code = Array.isArray(external) ? external[0] : external
  return code === '' ? [allCode] : [code as HSelectTabCode]
})

// 保证外部 v-model 与内部选择状态一致
watchEffect(() => {
  if (props.multiSelect) {
    const next = normalizedModelValue.value as HSelectTabCode[]
    const cur = modelValue.value
    const isSame =
      Array.isArray(cur) &&
      cur.length === next.length &&
      cur.every((c, idx) => c === next[idx])

    if (!isSame) modelValue.value = next as unknown as HSelectTabsValue
  } else {
    const next = normalizedModelValue.value as HSelectTabCode
    const cur = modelValue.value
    const isSame = !Array.isArray(cur) && cur === next
    if (!isSame) modelValue.value = next as unknown as HSelectTabsValue
  }
})

/**
 * 判断是否为选中状态
 */
const isActive = (code: HSelectTabCode) => {
  return normalizedSelected.value.includes(code)
}

/**
 * 点击选项
 * - 单选：直接切换 code
 * - 多选：支持切换选中/取消，并处理“全部”互斥逻辑
 */
const handleClick = (option: HSelectTabOption & { isAll?: boolean }) => {
  if (props.disabled || option.disabled) return

  const oldValue = props.multiSelect
    ? ([...(normalizedModelValue.value as HSelectTabCode[])] as unknown as HSelectTabsValue)
    : (normalizedModelValue.value as HSelectTabsValue)
  const code = option.code

  if (!props.multiSelect) {
    // 单选：选中“全部”返回空字符串
    const next = code === props.allCode ? '' : code
    if (normalizedModelValue.value === next) return
    modelValue.value = next as unknown as HSelectTabsValue
    emit('change', modelValue.value, oldValue)
    return
  }

  // 多选逻辑
  if (code === props.allCode) {
    // 多选：选中“全部/不限”返回空数组
    if ((normalizedModelValue.value as HSelectTabCode[]).length === 0) return
    modelValue.value = [] as unknown as HSelectTabsValue
    emit('change', modelValue.value, oldValue)
    return
  }

  const current = normalizedModelValue.value as HSelectTabCode[]
  const toggled = current.includes(code)
    ? current.filter(c => c !== code)
    : [...current, code]

  const uniq = dedupePreserveOrder(toggled).filter(c => nonAllCodes.value.includes(c))

  // 空数组代表“全部/不限”
  if (uniq.length === 0) {
    modelValue.value = [] as unknown as HSelectTabsValue
    emit('change', modelValue.value, oldValue)
    return
  }

  // 若已选中所有非“全部”项，则收敛为“全部/不限”（空数组）
  if (
    uniq.length === nonAllCodes.value.length &&
    nonAllCodes.value.every(c => uniq.includes(c))
  ) {
    modelValue.value = [] as unknown as HSelectTabsValue
    emit('change', modelValue.value, oldValue)
    return
  }

  modelValue.value = uniq as unknown as HSelectTabsValue
  emit('change', modelValue.value, oldValue)
}
</script>

<template>
  <div class="select-tabs" :class="[customClass, { 'is-disabled': props.disabled }]">
    <div
      v-for="item in innerOptions"
      :key="String(item.code)"
      class="tab-item"
      :class="{
        'is-active': isActive(item.code),
        'is-disabled': props.disabled || item.disabled
      }"
      @click="handleClick(item)"
    >
      {{ item.name }}
    </div>
  </div>
</template>

<style scoped lang="scss">
.select-tabs {
  width: 100%;
  display: inline-flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;

  &.is-disabled {
    opacity: 0.6;
    cursor: not-allowed;

    .tab-item {
      cursor: not-allowed;
    }
  }
}

.tab-item {
  flex: 1;
  padding: 3px 0;
  text-align: center;
  font-weight: 400;
  font-size: 12px;
  color: #222229;
  line-height: 18px;
  background: #f8f8f9;
  border: 1px solid transparent;
  border-radius: 2px;
  transition: background-color 0.15s ease-in-out,
  color 0.15s ease-in-out,
  border-color 0.15s ease-in-out;
  cursor: pointer;
  user-select: none;

  &.is-active {
    color: #1677ff;
    background: #eaf3ff;
    border-color: #1677ff;
  }

  &.is-disabled {
    cursor: not-allowed;
    opacity: 0.6;
  }
}
</style>
