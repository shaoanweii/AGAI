<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import DataResourceCascaderLinkage from '../../components/DataResourceCascaderLinkage.vue'
import {
  buildBatchCascaderMetaMap,
  buildBatchConstrainedCascaderOptions,
  resolveBatchCascaderConstraintState,
  resolveBatchCascaderValidationReason
} from '../dimensionCascaderConstraint'
import { getBatchSelectedCascaderValues } from '../dimensionValue'
import type {
  BatchDimensionFieldMeta,
  BatchDimensionRow,
  BatchResourceLinkageValue
} from '../types'

defineOptions({
  name: 'BatchEventDimensionValueInput'
})

const props = withDefaults(
  defineProps<{
    row: BatchDimensionRow
    fieldMeta: BatchDimensionFieldMeta
    disabled?: boolean
  }>(),
  {
    disabled: false
  }
)

const previousValidCascaderValues = ref<string[]>([])
const constrainedFieldLabel = computed(() => props.fieldMeta.definition.name || '当前维度')
const hasSameLevelConstraint = computed(() => !!props.fieldMeta.sameLevelOnly)
const hasSameParentConstraint = computed(() => !!props.fieldMeta.sameParentOnly)
const hasSameRootConstraint = computed(() => !!props.fieldMeta.sameRootOnly)
const shouldUseBranchStateFallback = computed(() => !props.fieldMeta.checkStrictly)
const isConstrainedCascaderField = computed(() => {
  return (
    props.fieldMeta.inputType === 'cascader' &&
    props.fieldMeta.multiple &&
    (hasSameLevelConstraint.value ||
      hasSameParentConstraint.value ||
      hasSameRootConstraint.value ||
      !!props.fieldMeta.multipleLimit)
  )
})

const cascaderMetaMap = computed(() =>
  buildBatchCascaderMetaMap(props.fieldMeta.cascaderOptions || [])
)
const currentCascaderValues = computed(() => getBatchSelectedCascaderValues(props.row.value))
const cascaderConstraintState = computed(() => {
  const state = resolveBatchCascaderConstraintState(
    currentCascaderValues.value,
    cascaderMetaMap.value
  )

  // 仅同层级约束场景才锁定层级；数据源等非同层级场景继续保持原有可浏览/可勾选口径。
  return {
    ...state,
    selectedLevel: hasSameLevelConstraint.value ? state.selectedLevel : 0
  }
})

const cascaderOptions = computed(() => {
  const options = props.fieldMeta.cascaderOptions || []

  if (!isConstrainedCascaderField.value) {
    return options
  }

  return buildBatchConstrainedCascaderOptions(
    options,
    cascaderMetaMap.value,
    cascaderConstraintState.value,
    {
      sameLevelOnly: hasSameLevelConstraint.value,
      sameParentOnly: hasSameParentConstraint.value,
      sameRootOnly: hasSameRootConstraint.value,
      multipleLimit: props.fieldMeta.multipleLimit || 0,
      checkStrictly: !!props.fieldMeta.checkStrictly,
      shouldUseBranchStateFallback: shouldUseBranchStateFallback.value
    }
  )
})

const cascaderProps = computed(() => ({
  label: 'label',
  value: 'value',
  children: 'children',
  disabled: 'disabled',
  emitPath: false,
  multiple: props.fieldMeta.multiple,
  checkStrictly: !!props.fieldMeta.checkStrictly
}))

/**
 * 级联展示参数交由字段元数据驱动，便于数据源直接复用体验代码的展示口径。
 */
const cascaderDisplayConfig = computed(() => ({
  showAllLevels: props.fieldMeta.showAllLevels ?? false,
  showCheckedStrategy: props.fieldMeta.showCheckedStrategy || 'child',
  separator: props.fieldMeta.separator || ' / '
}))

const resourceOptions = computed(() => props.fieldMeta.resourceOptions || [])

/**
 * 词库联动组件固定消费对象结构，这里只做当前表单值的标准读取。
 * @returns BatchResourceLinkageValue | null
 */
const getResourceLinkageValue = () => {
  const value = props.row.value

  return value && typeof value === 'object' && !Array.isArray(value)
    ? (value as BatchResourceLinkageValue)
    : null
}

/**
 * 维度值输入组件直接回写当前行数据，保持和父级动态表单的双向绑定口径一致。
 * @param value 需要写回的维度值
 */
const updateRowValue = (value: BatchDimensionRow['value']) => {
  // eslint-disable-next-line vue/no-mutating-props
  props.row.value = value
}

const resourceLinkageValue = computed<BatchResourceLinkageValue | null>({
  get: () => getResourceLinkageValue(),
  set: value => updateRowValue(value)
})

/**
 * 级联组件会持有传入数组的引用并在内部更新，因此这里始终返回一份快照，
 * 避免组件内部直接把外层表单值原地改脏，导致回退逻辑失效。
 */
const cascaderModelValue = computed<BatchDimensionRow['value']>(() => {
  return Array.isArray(props.row.value) ? [...props.row.value] : props.row.value
})

watch(
  () =>
    [
      props.row.field,
      props.row.value,
      props.fieldMeta.multipleLimit,
      props.fieldMeta.sameLevelOnly,
      props.fieldMeta.sameParentOnly,
      props.fieldMeta.sameRootOnly
    ] as const,
  () => {
    if (!isConstrainedCascaderField.value) {
      previousValidCascaderValues.value = []
      return
    }

    const selectedValues = currentCascaderValues.value
    const validationReason = resolveBatchCascaderValidationReason(
      selectedValues,
      cascaderMetaMap.value,
      {
        sameLevelOnly: hasSameLevelConstraint.value,
        sameParentOnly: hasSameParentConstraint.value,
        sameRootOnly: hasSameRootConstraint.value,
        multipleLimit: props.fieldMeta.multipleLimit || 0
      }
    )

    if (!validationReason) {
      previousValidCascaderValues.value = [...selectedValues]
    }
  },
  { immediate: true, deep: true }
)

/**
 * 级联更新统一走显式事件拦截：非法选择不写入模型，并强制回退到上一份合法快照。
 * @param value 组件尝试写入的最新值
 */
const handleCascaderModelUpdate = (value: string[] | string) => {
  if (!isConstrainedCascaderField.value) {
    updateRowValue(value)
    return
  }

  const selectedValues = getBatchSelectedCascaderValues(value)
  const validationReason = resolveBatchCascaderValidationReason(
    selectedValues,
    cascaderMetaMap.value,
    {
      sameLevelOnly: hasSameLevelConstraint.value,
      sameParentOnly: hasSameParentConstraint.value,
      sameRootOnly: hasSameRootConstraint.value,
      multipleLimit: props.fieldMeta.multipleLimit || 0
    }
  )

  if (validationReason) {
    updateRowValue([...previousValidCascaderValues.value])

    if (validationReason === 'limit') {
      ElMessage.warning(
        `${constrainedFieldLabel.value}最多可选择${props.fieldMeta.multipleLimit || 0}项`
      )
      return
    }

    if (validationReason === 'level') {
      ElMessage.warning(`${constrainedFieldLabel.value}仅支持选择同一层级，不允许跨级选择`)
      return
    }

    if (validationReason === 'root') {
      ElMessage.warning(
        `${constrainedFieldLabel.value}仅支持在同一第一级分类下选择，不允许跨第一级混选`
      )
      return
    }

    if (validationReason === 'parent') {
      ElMessage.warning(`${constrainedFieldLabel.value}仅支持选择同一父级的同一层级数据`)
    }

    return
  }

  previousValidCascaderValues.value = [...selectedValues]
  updateRowValue(selectedValues)
}
</script>

<template>
  <div class="dimension-value-input">
    <!-- 中文注释：维度值输入统一收口到该组件，便于后续继续按字段扩展输入形态 -->
    <!-- eslint-disable vue/no-mutating-props -->
    <el-input
      v-if="props.fieldMeta.inputType === 'input'"
      v-model.trim="props.row.value"
      :disabled="props.disabled"
      :maxlength="props.fieldMeta.maxLength"
      :placeholder="props.fieldMeta.placeholder || '请输入'"
      show-word-limit
    />

    <el-select-v2
      v-else-if="props.fieldMeta.inputType === 'select'"
      v-model="props.row.value"
      :multiple="props.fieldMeta.multiple"
      :multiple-limit="props.fieldMeta.multipleLimit || 0"
      collapse-tags
      :max-collapse-tags="1"
      collapse-tags-tooltip
      filterable
      clearable
      :disabled="props.disabled"
      :options="props.fieldMeta.valueOptions || []"
      class="dimension-value-input__control"
    />

    <DataResourceCascaderLinkage
      v-else-if="props.fieldMeta.inputType === 'resource-linkage'"
      v-model="resourceLinkageValue"
      :loading="!!props.fieldMeta.resourceLoading"
      :prefix="props.fieldMeta.resourcePrefix"
      :options="resourceOptions"
      :disabled="props.disabled"
    />

    <el-cascader
      v-else
      :model-value="cascaderModelValue"
      :options="cascaderOptions"
      :disabled="props.disabled"
      clearable
      filterable
      collapse-tags
      :max-collapse-tags="1"
      collapse-tags-tooltip
      :show-all-levels="cascaderDisplayConfig.showAllLevels"
      :show-checked-strategy="cascaderDisplayConfig.showCheckedStrategy"
      :separator="cascaderDisplayConfig.separator"
      :props="cascaderProps"
      :class="[
        'w-full',
        'dimension-value-input__cascader',
        { 'multiple-tags': Array.isArray(props.row.value) && props.row.value.length > 1 }
      ]"
      @update:model-value="handleCascaderModelUpdate"
    />
  </div>
</template>

<style scoped lang="scss">
.dimension-value-input {
  flex: 1 1 0;
  min-width: 0;
}

:deep(.dimension-value-input__control) {
  width: 100%;
  min-width: 0;
}
</style>
