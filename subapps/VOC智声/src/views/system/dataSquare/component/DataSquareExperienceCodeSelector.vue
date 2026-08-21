<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import {
  createEmptyExperienceCodeValue,
  type ExperienceCodeValue,
  normalizeExperienceCodeValue,
  normalizeSameLevelExperienceCodeValue
} from './experienceCode'

defineOptions({
  name: 'DataSquareExperienceCodeSelector'
})

type TagTypeOption = {
  key: string
  value: string
}

interface Props {
  modelValue?: ExperienceCodeValue
  tagType?: string
  typeOptions?: TagTypeOption[]
  options?: any[]
  loading?: boolean
  disabled?: boolean
  defaultTagType?: string
}

interface Emits {
  (e: 'update:modelValue', value: ExperienceCodeValue): void
  (e: 'update:tagType', value: string): void
  (e: 'change', value: ExperienceCodeValue): void
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: () => createEmptyExperienceCodeValue(),
  tagType: '',
  typeOptions: () => [],
  options: () => [],
  loading: false,
  disabled: false,
  defaultTagType: 'CA'
})

const emit = defineEmits<Emits>()

const selectedTagType = ref(props.tagType || props.defaultTagType || 'CA')
const cascaderValue = ref<string[][]>([])
const syncingFromParent = ref(false)
const shouldClearAfterTagTypeChange = ref(false)

const cascaderProps = computed(() => {
  return {
    value: 'tagCode',
    label: 'tagName',
    children: 'child',
    multiple: true,
    emitPath: true,
    checkStrictly: true
  } as const
})

const tagOptions = computed(() => props.options || [])

/**
 * 规范化级联返回值，保证组件内部始终使用二维路径数组。
 * @param value el-cascader 返回值
 * @returns 标准化后的路径数组
 */
function normalizeCascaderPaths(value: unknown): ExperienceCodeValue {
  if (!Array.isArray(value) || value.length === 0) {
    return createEmptyExperienceCodeValue()
  }

  const firstValue = value[0]
  if (Array.isArray(firstValue)) {
    return normalizeExperienceCodeValue(value)
  }

  return normalizeExperienceCodeValue([value])
}

/**
 * 同步体验代码类型选项，避免默认值不在可选范围内时出现脏状态。
 * 无外部显式传值时，自动回退到第一个可用类型。
 */
function syncTagTypeByOptions() {
  if (props.tagType || props.typeOptions.length === 0) {
    return
  }

  const exists = props.typeOptions.some(item => item.key === selectedTagType.value)
  if (!exists) {
    selectedTagType.value = props.typeOptions[0].key
  }
}

/**
 * 回填外部传入的体验代码值。
 * 组件以路径数组为唯一回显真源，避免祖先节点被误判为独立选中项。
 * @param value 外部体验代码值
 */
async function syncCascaderValueFromModel(value?: string[][]) {
  await nextTick()
  syncingFromParent.value = true
  cascaderValue.value = normalizeExperienceCodeValue(value)
  await nextTick()
  syncingFromParent.value = false
}

/**
 * 处理级联选择变化，统一转换为结构化体验代码对象。
 * @param value 当前级联值
 */
function handleCascaderChange(value: unknown) {
  if (syncingFromParent.value) {
    return
  }

  const currentValue = normalizeExperienceCodeValue(props.modelValue)
  const nextValue = normalizeSameLevelExperienceCodeValue(
    normalizeCascaderPaths(value),
    currentValue
  )
  cascaderValue.value = nextValue
  emit('update:modelValue', nextValue)
  emit('change', nextValue)
}

/**
 * 切换体验代码类型时清空当前选择，避免跨体系残留无效路径。
 */
function handleTagTypeChange() {
  shouldClearAfterTagTypeChange.value = true
  const emptyValue = createEmptyExperienceCodeValue()
  cascaderValue.value = emptyValue
  emit('update:modelValue', emptyValue)
  emit('change', emptyValue)
}

watch(
  () => props.typeOptions,
  () => {
    syncTagTypeByOptions()
  },
  { immediate: true, deep: true }
)

watch(
  () => props.tagType,
  value => {
    if (value && value !== selectedTagType.value) {
      selectedTagType.value = value
    }
  }
)

watch(
  () => selectedTagType.value,
  async (value, oldValue) => {
    if (!value) {
      return
    }

    if (value !== props.tagType) {
      emit('update:tagType', value)
    }

    if (oldValue && oldValue !== value && shouldClearAfterTagTypeChange.value) {
      await syncCascaderValueFromModel(createEmptyExperienceCodeValue())
      shouldClearAfterTagTypeChange.value = false
      return
    }

    shouldClearAfterTagTypeChange.value = false
    await syncCascaderValueFromModel(props.modelValue)
  },
  { immediate: true }
)

watch(
  () => props.modelValue,
  async value => {
    if (syncingFromParent.value) {
      return
    }
    await syncCascaderValueFromModel(value)
  },
  { deep: true }
)

watch(
  () => props.options,
  async () => {
    if (syncingFromParent.value) {
      return
    }
    await syncCascaderValueFromModel(props.modelValue)
  },
  { deep: true }
)
</script>

<template>
  <div class="data-square-experience-selector">
    <el-select
      v-model="selectedTagType"
      class="data-square-experience-selector__type"
      :disabled="disabled"
      :clearable="false"
      filterable
      :options="typeOptions"
      :props="{ label: 'value', value: 'key' }"
      placeholder="请选择"
      @change="handleTagTypeChange"
    />

    <div v-loading="loading" class="data-square-experience-selector__cascader-wrap">
      <el-cascader
        v-model="cascaderValue"
        class="data-square-experience-selector__cascader"
        :disabled="disabled || loading"
        :options="tagOptions"
        :props="cascaderProps"
        collapse-tags
        :max-collapse-tags="1"
        collapse-tags-tooltip
        filterable
        clearable
        placeholder="不限"
        style="width: 100%"
        @change="handleCascaderChange"
      />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.data-square-experience-selector {
  width: 100%;
  display: flex;
  gap: 12px;

  &__type {
    width: 120px;
    flex: 0 0 auto;
  }

  &__cascader-wrap {
    flex: 1 1 auto;
    min-width: 0;
  }

  &__cascader {
    width: 100%;
  }
}
</style>
