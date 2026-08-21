<script setup lang="ts">
import { computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { batchEventActions } from '../store'
import DimensionValueInput from './DimensionValueInput.vue'
import {
  BATCH_DIMENSION_FIELD_CODE,
  getBatchDimensionMultipleLimit,
  isRequiredBatchDimensionField
} from '../fieldCode'
import type {
  BatchDimensionDefinition,
  BatchDimensionFieldMeta,
  BatchDimensionRow,
  BatchDimensionConfigOptions
} from '../types'
import {
  createBatchDimensionDefaultValue,
  mapBatchNameCodeOptions,
  resolveBatchDimensionFieldMeta,
  shouldLoadAttributeLabelDimensionOptions,
  shouldLoadProvinceDimensionOptions
} from '../dimensionFieldRegistry'

defineOptions({
  name: 'BatchEventDimensionConfig'
})

interface Props {
  options: BatchDimensionConfigOptions
  brand: string
  disabled?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  brand: '',
  disabled: false
})

const EMPTY_DIMENSION_DEFINITION: BatchDimensionDefinition = {
  name: '',
  code: '',
  logicalOperator: [],
  condition: []
}

const model = defineModel<BatchDimensionRow[]>({
  required: true
})

const fieldDefinitions = computed(() => props.options.dimensionDefinitions || [])
const isBrandReady = computed(() => !!props.brand)
const usedFieldCodes = computed(
  () => new Set(model.value.map(item => item.field).filter(field => !!field))
)
const isAddDisabled = computed(() => {
  const all = fieldDefinitions.value
  if (!Array.isArray(all) || all.length === 0) {
    return false
  }

  return usedFieldCodes.value.size >= all.length
})

/**
 * 将接口返回的字段定义按 code 建立索引，便于动态驱动逻辑符和值类型渲染。
 */
const fieldDefinitionMap = computed(() => {
  return new Map(fieldDefinitions.value.map(item => [item.code, item]))
})

/**
 * 根据字段 code 返回额外补充配置。接口负责描述“字段+逻辑符+条件类型”，
 * 页面负责补齐具体控件类型、可选值来源、统计方式和限制规则。
 * @param fieldCode 字段编码
 * @param valueTypeCode 当前值类型
 * @returns FieldMeta 补充配置
 */
const resolveFieldMeta = (
  definition: BatchDimensionDefinition,
  valueTypeCode: string,
  statMode = ''
): Omit<BatchDimensionFieldMeta, 'definition'> => {
  return resolveBatchDimensionFieldMeta(definition, {
    brand: props.brand,
    valueType: valueTypeCode,
    statMode
  })
}

/**
 * 获取字段定义。空白行未选字段时返回空定义，避免新增行时被自动带出默认项。
 * @param fieldCode 字段编码
 * @returns BatchDimensionDefinition
 */
const getFieldDefinition = (fieldCode: string) => {
  return fieldDefinitionMap.value.get(fieldCode) || EMPTY_DIMENSION_DEFINITION
}

/**
 * 将接口定义与前端补充配置合并，得到当前行最终的渲染元信息。
 * @param row 当前维度行
 * @returns BatchDimensionFieldMeta
 */
const getFieldMeta = (row: BatchDimensionRow): BatchDimensionFieldMeta => {
  const definition = getFieldDefinition(row.field)
  const extendMeta = resolveFieldMeta(definition, row.valueType, row.statMode)

  return {
    definition,
    ...extendMeta
  }
}

/**
 * 根据当前字段元信息生成默认值，确保切换到词库联动组件时不会沿用旧级联数组结构。
 * @param fieldMeta 字段元信息
 * @returns BatchDimensionRow['value']
 */
const createDefaultFieldValue = (fieldMeta: Omit<BatchDimensionFieldMeta, 'definition'>) => {
  return createBatchDimensionDefaultValue(fieldMeta)
}

const createRow = (fieldCode?: string): BatchDimensionRow => {
  const definition = getFieldDefinition(fieldCode || '')
  const valueType = definition.condition[0]?.code || ''
  const fieldMeta = definition.code
    ? resolveFieldMeta(definition, valueType, definition.countingMethod?.[0]?.code || '')
    : {
        statModeOptions: [],
        inputType: 'select' as const,
        multiple: false,
        valueOptions: []
      }

  return {
    id: `dim-${Date.now()}-${Math.random().toString(16).slice(2, 6)}`,
    field: definition.code,
    wildcard: definition.logicalOperator[0]?.code || '',
    valueType,
    statMode: fieldMeta.statModeOptions[0]?.value || '',
    value: createDefaultFieldValue(fieldMeta)
  }
}

/**
 * 切换字段时，按接口返回的逻辑符和条件类型重新初始化当前行，避免沿用旧字段的配置残留。
 * @param row 当前维度行
 */
const handleFieldChange = (row: BatchDimensionRow) => {
  const definition = getFieldDefinition(row.field)

  if (!definition.code) {
    row.wildcard = ''
    row.valueType = ''
    row.statMode = ''
    row.value = ''
    return
  }

  const nextValueType = definition.condition[0]?.code || ''
  const nextStatMode = definition.countingMethod?.[0]?.code || ''
  const fieldMeta = resolveFieldMeta(definition, nextValueType, nextStatMode)

  row.wildcard = definition.logicalOperator[0]?.code || ''
  row.valueType = nextValueType
  row.statMode = nextStatMode
  row.value = createDefaultFieldValue(fieldMeta)

  if (shouldLoadProvinceDimensionOptions(row.field)) {
    void batchEventActions.updateProvinceOptions().then(success => {
      if (!success) {
        ElMessage.error('获取省份列表失败')
      }
    })
  }

  if (shouldLoadAttributeLabelDimensionOptions(row.field)) {
    void batchEventActions.updateAttributeLabelOptions().then(success => {
      if (!success) {
        ElMessage.error('获取属性标签列表失败')
      }
    })
  }
}

/**
 * 条件类型切换时，同步调整输入控件和值结构，主要用于标题/原文在词库和值之间切换。
 * @param row 当前维度行
 */
const handleValueTypeChange = (row: BatchDimensionRow) => {
  if (!row.field || !row.valueType) {
    row.statMode = ''
    row.value = ''
    return
  }

  const fieldMeta = resolveFieldMeta(getFieldDefinition(row.field), row.valueType, row.statMode)
  // 仅当当前统计方式已不在新配置里时才回退首项，避免值类型切换误清空用户选择。
  row.statMode = fieldMeta.statModeOptions.some(item => item.value === row.statMode)
    ? row.statMode
    : fieldMeta.statModeOptions[0]?.value || ''
  row.value = createDefaultFieldValue(fieldMeta)
}

/**
 * 统计方式切回独立计算时，需要立即收敛多选数量，避免界面继续保留超过上限的历史选项。
 * @param row 当前维度行
 */
const handleStatModeChange = (row: BatchDimensionRow) => {
  const limit = getBatchDimensionMultipleLimit(row.field, row.statMode)

  if (!limit || !Array.isArray(row.value) || row.value.length <= limit) {
    return
  }

  const fieldName = getFieldDefinition(row.field).name || '当前维度'
  row.value = row.value.slice(0, limit)
  ElMessage.warning(`${fieldName}在独立计算时最多可选择${limit}项，已保留前${limit}项`)
}

/**
 * 维度字段和单点规则保持一致：同一批量规则中同一维度只能出现一次。
 * @param fieldCode 维度字段编码
 * @param currentIndex 当前行索引
 * @returns boolean
 */
const isFieldDisabled = (fieldCode: string, currentIndex: number) => {
  return model.value.some((item, index) => index !== currentIndex && item.field === fieldCode)
}

/**
 * 批量规则的维度配置依赖品牌口径，未选品牌时先拦截，避免车系等品牌相关选项提前进入错误状态。
 * @returns boolean
 */
const ensureBrandReady = () => {
  if (isBrandReady.value) {
    return true
  }

  ElMessage.warning('请选择品牌')
  return false
}

const addRow = () => {
  if (!ensureBrandReady()) {
    return
  }

  model.value.push(createRow())
}

const removeRow = (index: number) => {
  model.value.splice(index, 1)
}

watch(
  () => model.value.map(item => item.field),
  fields => {
    if (fields.some(field => field === BATCH_DIMENSION_FIELD_CODE.PROVINCE)) {
      void batchEventActions.updateProvinceOptions().then(success => {
        if (!success) {
          ElMessage.error('获取省份列表失败')
        }
      })
    }

    if (fields.some(field => field === BATCH_DIMENSION_FIELD_CODE.ATTRIBUTE)) {
      void batchEventActions.updateAttributeLabelOptions().then(success => {
        if (!success) {
          ElMessage.error('获取属性标签列表失败')
        }
      })
    }
  },
  { immediate: true }
)
</script>

<template>
  <div class="batch-config-card">
    <div v-for="(row, index) in model" :key="row.id" class="batch-config-card__row">
      <el-select
        v-model="row.field"
        :class="[
          'w-100',
          { 'batch-config-card__field-select--required': isRequiredBatchDimensionField(row.field) }
        ]"
        :disabled="props.disabled"
        @change="handleFieldChange(row)"
      >
        <el-option
          v-for="item in fieldDefinitions"
          :key="item.code"
          :label="item.name"
          :value="item.code"
          :disabled="isFieldDisabled(item.code, index)"
        >
          <span>{{ item.name }}</span>
          <span
            v-if="isRequiredBatchDimensionField(item.code)"
            class="batch-config-card__required-mark"
          >
            *
          </span>
        </el-option>
      </el-select>

      <el-select v-model="row.wildcard" class="w-80" placeholder="包含" :disabled="props.disabled">
        <el-option
          v-for="item in mapBatchNameCodeOptions(getFieldMeta(row).definition.logicalOperator)"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>

      <el-select
        v-model="row.valueType"
        class="w-80"
        placeholder="选项"
        :disabled="props.disabled"
        @change="handleValueTypeChange(row)"
      >
        <el-option
          v-for="item in mapBatchNameCodeOptions(getFieldMeta(row).definition.condition)"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>

      <el-select
        v-if="getFieldMeta(row).statModeOptions.length"
        v-model="row.statMode"
        class="w-100"
        :disabled="props.disabled"
        @change="handleStatModeChange(row)"
      >
        <el-option
          v-for="item in getFieldMeta(row).statModeOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>

      <DimensionValueInput :row="row" :field-meta="getFieldMeta(row)" :disabled="props.disabled" />

      <el-button v-if="!props.disabled" link @click="removeRow(index)">
        <el-icon><Close /></el-icon>
      </el-button>
    </div>

    <div>
      <el-button v-if="!props.disabled" type="primary" :disabled="isAddDisabled" @click="addRow">
        <el-icon class="mr-4"><Plus /></el-icon>
        添加维度配置
      </el-button>
    </div>
  </div>
</template>

<style scoped lang="scss">
.batch-config-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.batch-config-card__row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.batch-config-card__required-mark {
  margin-left: 2px;
  color: var(--el-color-danger);
}

:deep(.batch-config-card__field-select--required .el-select__selected-item) {
  display: inline-flex;
  align-items: center;
}

:deep(.batch-config-card__field-select--required .el-select__selected-item::after) {
  content: '*';
  margin-left: 2px;
  margin-top: 5px;
  color: var(--el-color-danger);
}

.batch-config-card__placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 32px;
  color: #86909c;
  background: #fff;
  border-radius: 6px;
}

.w-80 {
  width: 80px;
}

.w-100 {
  width: 100px;
}

.w-120 {
  width: 120px;
}

.w-140 {
  width: 140px;
}

.mr-4 {
  margin-right: 4px;
}
</style>
