import { batchEventStore } from './store'
import { mapEmotionalLevelTreeOptions } from './emotionalLevel'
import { BATCH_DIMENSION_FIELD_CODE, getBatchDimensionMultipleLimit } from './fieldCode'
import type {
  BatchCascaderOption,
  BatchDimensionDefinition,
  BatchDimensionFieldMeta,
  BatchNameCodeOption,
  BatchSelectOption
} from './types'

interface ResolveDimensionFieldMetaOptions {
  brand: string
  valueType: string
  statMode: string
}

const mapSharedDictOptions = (options: any[] = []) => {
  return options
    .map(item => ({
      label: String(item?.value ?? ''),
      value: String(item?.key ?? '')
    }))
    .filter(item => item.value)
}

/**
 * 数据源维度仅保留可落到三级末级的分支，确保页面交互和提交协议保持一致。
 * @param nodes 渠道树节点
 * @param level 当前层级
 * @returns BatchCascaderOption[]
 */
export const mapBatchDataSourceTreeOptions = (
  nodes: any[] = [],
  level = 1
): BatchCascaderOption[] => {
  if (level > 3) {
    return []
  }

  return (nodes || []).reduce<BatchCascaderOption[]>((options, item) => {
    const children = Array.isArray(item.child)
      ? mapBatchDataSourceTreeOptions(item.child, level + 1)
      : []

    if (level < 3 && children.length === 0) {
      return options
    }

    options.push({
      label: item.name,
      value: item.code,
      children: children.length ? children : undefined
    })

    return options
  }, [])
}

/**
 * 体验代码维度保留完整标签树结构，编辑回填时可兼容历史已选节点。
 * @param nodes 标签树节点
 * @returns BatchCascaderOption[]
 */
export const mapBatchTagTreeOptions = (nodes: any[] = []): BatchCascaderOption[] => {
  return (nodes || []).map(item => ({
    label: item.tagName,
    value: item.tagCode,
    children: Array.isArray(item.child) ? mapBatchTagTreeOptions(item.child) : undefined
  }))
}

/**
 * 将树形字典统一映射为级联组件可消费的结构。
 * @param nodes 原始树形字典
 * @returns BatchCascaderOption[]
 */
export const mapBatchDictTreeOptions = (nodes: any[] = []): BatchCascaderOption[] => {
  return (nodes || []).map(item => ({
    label: item.value,
    value: item.key,
    children: Array.isArray(item.children) ? mapBatchDictTreeOptions(item.children) : undefined
  }))
}

/**
 * name/code 结构统一转成 label/value，避免页面和 API 层重复写转换。
 * @param options 原始 name/code 选项
 * @returns BatchSelectOption[]
 */
export const mapBatchNameCodeOptions = (options: BatchNameCodeOption[] = []) => {
  return options.map(item => ({
    label: item.name,
    value: item.code
  })) as BatchSelectOption[]
}

/**
 * 统计方式以下发配置为准，页面不再内置历史默认项。
 * @param definition 当前维度定义
 * @returns BatchSelectOption[]
 */
export const getBatchDimensionStatModeOptions = (definition: BatchDimensionDefinition) => {
  return mapBatchNameCodeOptions(definition.countingMethod || [])
}

export const ACCOUNT_RESOURCE_DIMENSION_FIELDS: string[] = [
  BATCH_DIMENSION_FIELD_CODE.PUBLISH_USER,
  BATCH_DIMENSION_FIELD_CODE.ORIGINAL_POST_USER
]

export const TEXT_DIMENSION_FIELDS: string[] = [
  BATCH_DIMENSION_FIELD_CODE.TITLE,
  BATCH_DIMENSION_FIELD_CODE.CONTENT
]

export const TEXT_LEXICON_DIMENSION_FIELDS: string[] = [...TEXT_DIMENSION_FIELDS]

export const CASCADER_DIMENSION_FIELDS: string[] = [
  BATCH_DIMENSION_FIELD_CODE.DATA_SOURCE,
  BATCH_DIMENSION_FIELD_CODE.EXPERIENCE_CODE,
  BATCH_DIMENSION_FIELD_CODE.REGULATION_CONTENT_TYPE,
  BATCH_DIMENSION_FIELD_CODE.AD_TYPE,
  BATCH_DIMENSION_FIELD_CODE.EMOTIONAL_LEVEL
]

export const SELECT_MULTI_DIMENSION_FIELDS: string[] = [
  BATCH_DIMENSION_FIELD_CODE.PROVINCE,
  BATCH_DIMENSION_FIELD_CODE.ATTRIBUTE,
  BATCH_DIMENSION_FIELD_CODE.CAR_SERIES,
  BATCH_DIMENSION_FIELD_CODE.BATCH_KH_TYPE,
  BATCH_DIMENSION_FIELD_CODE.EMOTION
]

/**
 * 词库类值类型口径统一在这里判断，避免 UI 和保存逻辑出现大小写漂移。
 * @param valueType 值类型
 * @returns boolean
 */
export const isLexiconValueType = (valueType: string) => {
  const text = String(valueType || '')
    .trim()
    .toLowerCase()
  return text === 'lexicon'
}

/**
 * 属性标签联调阶段编码可能有轻微差异，统一按 code/name 双兜底识别。
 * @param field 字段编码
 * @param definition 当前维度定义
 * @returns boolean
 */
export const isAttributeLabelDimensionField = (
  field: string,
  definition?: { name?: string; code?: string }
) => {
  const fieldCode = String(field || '').trim()
  const definitionCode = String(definition?.code || '').trim()
  const definitionName = String(definition?.name || '').trim()

  return (
    fieldCode === BATCH_DIMENSION_FIELD_CODE.ATTRIBUTE ||
    definitionCode === BATCH_DIMENSION_FIELD_CODE.ATTRIBUTE ||
    definitionName === '属性标签'
  )
}

export const shouldLoadProvinceDimensionOptions = (field: string) => {
  return field === BATCH_DIMENSION_FIELD_CODE.PROVINCE
}

export const shouldLoadAttributeLabelDimensionOptions = (field: string) => {
  return field === BATCH_DIMENSION_FIELD_CODE.ATTRIBUTE
}

/**
 * 获取下拉型维度的实时选项，统一从 store 资源中取值。
 * @param field 字段编码
 * @param brand 当前品牌编码
 * @returns BatchSelectOption[]
 */
export const getBatchDimensionSelectOptions = (field: string, brand: string) => {
  const batchConditions = batchEventStore.batchConditions || {}

  switch (field) {
    case BATCH_DIMENSION_FIELD_CODE.PROVINCE:
      return batchEventStore.dimensionOptionResources.provinceOptions
    case BATCH_DIMENSION_FIELD_CODE.CAR_SERIES: {
      const matchedBrand = (batchConditions.selfBrandCar || []).find(
        (item: any) => item.key === brand || item.value === brand
      )
      return mapSharedDictOptions(matchedBrand?.children || [])
    }
    case BATCH_DIMENSION_FIELD_CODE.EMOTION:
      return mapSharedDictOptions(batchConditions.vocSentiment || [])
    case BATCH_DIMENSION_FIELD_CODE.INTENTION:
      return mapSharedDictOptions(batchConditions.vocIntention || [])
    case BATCH_DIMENSION_FIELD_CODE.BATCH_KH_TYPE:
      return mapSharedDictOptions(batchConditions.batchKhType || [])
    case BATCH_DIMENSION_FIELD_CODE.WATER_MAN:
      return mapSharedDictOptions(batchConditions.waterMan || [])
    case BATCH_DIMENSION_FIELD_CODE.V_MAN:
      return mapSharedDictOptions(batchConditions.vMan || [])
    case BATCH_DIMENSION_FIELD_CODE.CAR_OWNER:
      return mapSharedDictOptions(batchConditions.carOwner || [])
    case BATCH_DIMENSION_FIELD_CODE.CUSTOMER_GENDER:
      return mapSharedDictOptions(batchConditions.customerGender || [])
    case BATCH_DIMENSION_FIELD_CODE.ATTRIBUTE:
      return batchEventStore.dimensionOptionResources.attributeLabelOptions
    default:
      return []
  }
}

/**
 * 获取级联型维度的实时选项，统一从 store 资源中取值。
 * @param field 字段编码
 * @returns BatchCascaderOption[]
 */
export const getBatchDimensionCascaderOptions = (field: string) => {
  const batchConditions = batchEventStore.batchConditions || {}

  switch (field) {
    case BATCH_DIMENSION_FIELD_CODE.DATA_SOURCE:
      return mapBatchDataSourceTreeOptions(batchEventStore.channelTree || [])
    case BATCH_DIMENSION_FIELD_CODE.EXPERIENCE_CODE:
      return mapBatchTagTreeOptions(batchEventStore.tagLibOptions || [])
    case BATCH_DIMENSION_FIELD_CODE.REGULATION_CONTENT_TYPE:
      return mapBatchDictTreeOptions(batchConditions.contentType || [])
    case BATCH_DIMENSION_FIELD_CODE.AD_TYPE:
      return mapBatchDictTreeOptions(batchConditions.batchAdType || [])
    case BATCH_DIMENSION_FIELD_CODE.EMOTIONAL_LEVEL:
      return mapEmotionalLevelTreeOptions(batchConditions.batchEmotionalLevel || [])
    default:
      return []
  }
}

/**
 * 根据字段定义和运行时上下文，生成维度行最终渲染元信息。
 * @param definition 当前维度定义
 * @param runtime 运行时上下文
 * @returns Omit<BatchDimensionFieldMeta, 'definition'>
 */
export const resolveBatchDimensionFieldMeta = (
  definition: BatchDimensionDefinition,
  runtime: ResolveDimensionFieldMetaOptions
): Omit<BatchDimensionFieldMeta, 'definition'> => {
  const fieldCode = definition.code
  const statModeOptions = getBatchDimensionStatModeOptions(definition)

  switch (fieldCode) {
    case BATCH_DIMENSION_FIELD_CODE.DATA_SOURCE:
      return {
        statModeOptions,
        inputType: 'cascader',
        multiple: true,
        cascaderOptions: getBatchDimensionCascaderOptions(fieldCode),
        checkStrictly: false,
        showAllLevels: false,
        // 数据源只能勾选末级，已选数量也必须与末级叶子节点保持一致，避免被父级折叠后产生误导。
        showCheckedStrategy: 'child',
        multipleLimit: getBatchDimensionMultipleLimit(fieldCode, runtime.statMode)
      }
    case BATCH_DIMENSION_FIELD_CODE.PROVINCE:
      return {
        statModeOptions,
        inputType: 'select',
        multiple: true,
        valueOptions: getBatchDimensionSelectOptions(fieldCode, runtime.brand)
      }
    case BATCH_DIMENSION_FIELD_CODE.CAR_SERIES:
      return {
        statModeOptions,
        inputType: 'select',
        multiple: true,
        valueOptions: getBatchDimensionSelectOptions(fieldCode, runtime.brand),
        multipleLimit: getBatchDimensionMultipleLimit(fieldCode, runtime.statMode)
      }
    case BATCH_DIMENSION_FIELD_CODE.EXPERIENCE_CODE:
      return {
        statModeOptions,
        inputType: 'cascader',
        multiple: true,
        cascaderOptions: getBatchDimensionCascaderOptions(fieldCode),
        checkStrictly: true,
        sameLevelOnly: true,
        sameRootOnly: true,
        showAllLevels: true,
        separator: '#',
        multipleLimit: getBatchDimensionMultipleLimit(fieldCode, runtime.statMode)
      }
    case BATCH_DIMENSION_FIELD_CODE.PUBLISH_USER:
    case BATCH_DIMENSION_FIELD_CODE.ORIGINAL_POST_USER:
      return {
        statModeOptions: [],
        inputType: 'resource-linkage',
        multiple: true,
        resourceOptions: batchEventStore.dataResource.accountList,
        resourceLoading: batchEventStore.dataResource.loading
      }
    case BATCH_DIMENSION_FIELD_CODE.AD_TYPE:
      return {
        statModeOptions: [],
        inputType: 'cascader',
        multiple: true,
        cascaderOptions: getBatchDimensionCascaderOptions(fieldCode),
        checkStrictly: true
      }
    case BATCH_DIMENSION_FIELD_CODE.EMOTION:
      return {
        statModeOptions: [],
        inputType: 'select',
        multiple: true,
        valueOptions: getBatchDimensionSelectOptions(fieldCode, runtime.brand)
      }
    case BATCH_DIMENSION_FIELD_CODE.EMOTIONAL_LEVEL:
      return {
        statModeOptions: [],
        inputType: 'cascader',
        multiple: true,
        cascaderOptions: getBatchDimensionCascaderOptions(fieldCode),
        checkStrictly: true,
        sameLevelOnly: true,
        sameParentOnly: true
      }
    case BATCH_DIMENSION_FIELD_CODE.INTENTION:
    case BATCH_DIMENSION_FIELD_CODE.WATER_MAN:
    case BATCH_DIMENSION_FIELD_CODE.V_MAN:
    case BATCH_DIMENSION_FIELD_CODE.CAR_OWNER:
    case BATCH_DIMENSION_FIELD_CODE.CUSTOMER_GENDER:
      return {
        statModeOptions: [],
        inputType: 'select',
        multiple: false,
        valueOptions: getBatchDimensionSelectOptions(fieldCode, runtime.brand)
      }
    case BATCH_DIMENSION_FIELD_CODE.BATCH_KH_TYPE:
      return {
        statModeOptions: [],
        inputType: 'select',
        multiple: true,
        valueOptions: getBatchDimensionSelectOptions(fieldCode, runtime.brand)
      }
    case BATCH_DIMENSION_FIELD_CODE.ATTRIBUTE:
      return {
        statModeOptions,
        inputType: 'select',
        multiple: true,
        valueOptions: getBatchDimensionSelectOptions(fieldCode, runtime.brand)
      }
    case BATCH_DIMENSION_FIELD_CODE.REGULATION_CONTENT_TYPE:
      return {
        statModeOptions: [],
        inputType: 'cascader',
        multiple: false,
        cascaderOptions: getBatchDimensionCascaderOptions(fieldCode),
        checkStrictly: true
      }
    case BATCH_DIMENSION_FIELD_CODE.TITLE:
    case BATCH_DIMENSION_FIELD_CODE.CONTENT:
      if (isLexiconValueType(runtime.valueType)) {
        return {
          statModeOptions: [],
          inputType: 'resource-linkage',
          multiple: true,
          resourceOptions: batchEventStore.dataResource.allList,
          resourceLoading: batchEventStore.dataResource.loading,
          resourcePrefix: '@'
        }
      }

      return {
        statModeOptions: [],
        inputType: 'input',
        multiple: false,
        placeholder: '请输入',
        maxLength: 50
      }
    default:
      return {
        statModeOptions: [],
        inputType: 'select',
        multiple: false,
        valueOptions: []
      }
  }
}

/**
 * 统一根据字段元信息创建默认值，避免切换字段时沿用旧值结构。
 * @param fieldMeta 字段元信息
 * @returns BatchDimensionRow['value']
 */
export const createBatchDimensionDefaultValue = (
  fieldMeta: Omit<BatchDimensionFieldMeta, 'definition'>
) => {
  if (fieldMeta.inputType === 'input') {
    return ''
  }

  if (fieldMeta.inputType === 'resource-linkage') {
    return null
  }

  return fieldMeta.multiple ? [] : ''
}
