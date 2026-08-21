<script setup lang="ts">
import { nextTick, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { LabelTag } from '@/api/common/index.d'

defineOptions({
  name: 'SameLevelExperienceCodeCascader'
})

type ExperienceCodeOption = LabelTag & Record<string, any>

interface Props {
  options?: ExperienceCodeOption[]
  loading?: boolean
  disabled?: boolean
  placeholder?: string
}

const props = withDefaults(defineProps<Props>(), {
  options: () => [],
  loading: false,
  disabled: false,
  placeholder: '请选择'
})

const firstCodeTag = defineModel<string[]>('firstCodeTag', { required: true })
const secondCodeTag = defineModel<string[]>('secondCodeTag', { required: true })
const threeCodeTag = defineModel<string[]>('threeCodeTag', { required: true })
const fourCodeTag = defineModel<string[]>('fourCodeTag', { required: true })

const cascaderValue = ref<string[][]>([])
const lastValidCascaderValue = ref<string[][]>([])
const syncingFromModel = ref(false)

const cascaderProps = {
  value: 'tagCode',
  label: 'tagName',
  children: 'child',
  multiple: true,
  emitPath: true,
  checkStrictly: true
} as const

/**
 * Normalize a model level into stable string codes.
 * @param value Raw model value from one experience-code level.
 * @returns De-duplicated non-empty code array.
 */
const normalizeCodeArray = (value: unknown): string[] => {
  if (!Array.isArray(value)) {
    return value === '' || value === null || value === undefined ? [] : [String(value)]
  }

  return Array.from(
    new Set(
      value
        .filter(item => item !== '' && item !== null && item !== undefined)
        .map(item => String(item))
    )
  )
}

/**
 * Clone cascader paths so rollback never reuses mutable array references.
 * @param paths Current cascader paths.
 * @returns Cloned paths.
 */
const clonePaths = (paths: string[][]) => paths.map(path => [...path])

/**
 * Normalize Element Plus cascader output for both single-path and multi-path shapes.
 * @param value Raw cascader value.
 * @returns Normalized path array.
 */
const normalizeCascaderPaths = (value: unknown): string[][] => {
  if (!Array.isArray(value) || value.length === 0) return []

  const first = value[0]
  if (Array.isArray(first)) {
    return value.map(path =>
      Array.isArray(path) ? path.filter(Boolean).map(item => String(item)) : []
    )
  }

  return [value.filter(Boolean).map(item => String(item))]
}

/**
 * Check that all selected nodes are on the same tree depth.
 * @param paths Selected cascader paths.
 * @returns Whether all selected paths share one level.
 */
const isSameSelectedLevel = (paths: string[][]) => {
  const levels = new Set(paths.filter(path => path.length > 0).map(path => path.length))
  return levels.size <= 1
}

/**
 * Convert valid cascader paths back to the four backend fields.
 * @param paths Valid cascader paths.
 * @returns Four level code arrays.
 */
const buildLevelCodesFromPaths = (paths: string[][]) => {
  const firstCodes: string[] = []
  const secondCodes: string[] = []
  const threeCodes: string[] = []
  const fourCodes: string[] = []

  paths.forEach(path => {
    if (!Array.isArray(path)) return
    if (path[0]) firstCodes.push(path[0])
    if (path[1]) secondCodes.push(path[1])
    if (path[2]) threeCodes.push(path[2])
    if (path[3]) fourCodes.push(path[3])
  })

  return {
    firstCodes: normalizeCodeArray(firstCodes),
    secondCodes: normalizeCodeArray(secondCodes),
    threeCodes: normalizeCodeArray(threeCodes),
    fourCodes: normalizeCodeArray(fourCodes)
  }
}

/**
 * Emit field model updates from a legal cascader selection.
 * @param paths Legal cascader paths.
 */
const syncFieldsFromPaths = (paths: string[][]) => {
  const { firstCodes, secondCodes, threeCodes, fourCodes } = buildLevelCodesFromPaths(paths)
  firstCodeTag.value = firstCodes
  secondCodeTag.value = secondCodes
  threeCodeTag.value = threeCodes
  fourCodeTag.value = fourCodes
}

/**
 * Resolve the deepest selected backend level for display recovery.
 * @returns Codes from the currently deepest selected level.
 */
const getDeepestSelectedCodes = () => {
  const fourCodes = normalizeCodeArray(fourCodeTag.value)
  if (fourCodes.length > 0) return fourCodes

  const threeCodes = normalizeCodeArray(threeCodeTag.value)
  if (threeCodes.length > 0) return threeCodes

  const secondCodes = normalizeCodeArray(secondCodeTag.value)
  if (secondCodes.length > 0) return secondCodes

  return normalizeCodeArray(firstCodeTag.value)
}

/**
 * Find the full cascader path for a code in the tree.
 * @param options Experience-code tree.
 * @param targetCode Target code.
 * @param path Current recursion path.
 * @returns Full path, or null when not found.
 */
const findPathByCode = (
  options: ExperienceCodeOption[],
  targetCode: string,
  path: string[] = []
): string[] | null => {
  for (const node of options || []) {
    const nodeCode = node.tagCode || ''
    const currentPath = [...path, nodeCode].filter(Boolean)

    if (nodeCode === targetCode) return currentPath

    if (node.child?.length) {
      const found = findPathByCode(node.child as ExperienceCodeOption[], targetCode, currentPath)
      if (found) return found
    }
  }

  return null
}

/**
 * Rebuild cascader display value from the four backend fields.
 * Keeps only the deepest level to avoid parent-child mixed display.
 */
const syncCascaderFromFields = () => {
  const deepestCodes = getDeepestSelectedCodes()

  if (deepestCodes.length === 0) {
    cascaderValue.value = []
    lastValidCascaderValue.value = []
    return
  }

  if (props.options.length === 0) return

  const paths = deepestCodes
    .map(code => findPathByCode(props.options, code))
    .filter((path): path is string[] => Array.isArray(path) && path.length > 0)

  cascaderValue.value = clonePaths(paths)
  lastValidCascaderValue.value = clonePaths(paths)
}

/**
 * Roll back the visible cascader selection after an invalid cross-level pick.
 */
const restoreLastValidCascaderValue = async () => {
  syncingFromModel.value = true
  cascaderValue.value = clonePaths(lastValidCascaderValue.value)
  await nextTick()
  syncingFromModel.value = false
}

/**
 * Handle user cascader changes and reject cross-level selections.
 * @param value Raw cascader value.
 */
const handleCascaderChange = async (value: unknown) => {
  if (syncingFromModel.value) return

  const paths = normalizeCascaderPaths(value)
  if (!isSameSelectedLevel(paths)) {
    ElMessage.warning('体验代码仅支持选择同一层级，不允许跨级选择')
    await restoreLastValidCascaderValue()
    return
  }

  syncingFromModel.value = true
  cascaderValue.value = clonePaths(paths)
  lastValidCascaderValue.value = clonePaths(paths)
  syncFieldsFromPaths(paths)
  await nextTick()
  syncingFromModel.value = false
}

watch(
  [firstCodeTag, secondCodeTag, threeCodeTag, fourCodeTag],
  () => {
    if (!syncingFromModel.value) {
      syncCascaderFromFields()
    }
  },
  { deep: true, immediate: true }
)

watch(
  () => props.options,
  () => {
    if (!syncingFromModel.value) {
      syncCascaderFromFields()
    }
  },
  { immediate: true }
)
</script>

<template>
  <div v-loading="loading" class="same-level-experience-code-cascader">
    <el-cascader
      v-model="cascaderValue"
      class="same-level-experience-code-cascader__inner w-full"
      :disabled="disabled || loading"
      :options="options"
      :props="cascaderProps"
      filterable
      clearable
      collapse-tags
      :max-collapse-tags="1"
      collapse-tags-tooltip
      :placeholder="placeholder"
      @change="handleCascaderChange"
    />
  </div>
</template>

<style scoped lang="scss">
.same-level-experience-code-cascader {
  width: 100%;
}

.same-level-experience-code-cascader__inner {
  width: 100%;
}
</style>
