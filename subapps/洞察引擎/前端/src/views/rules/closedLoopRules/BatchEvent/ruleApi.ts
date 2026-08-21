import {
  copyBatchRule,
  getBatchRuleDetail,
  insertBatchRule,
  queryBatchRulePage,
  updateBatchRule,
  type BatchRuleSavePayload,
  type InsBatchRuleModel
} from '@/api/batchEventRules'
import { batchEventStore } from './store'
import {
  BATCH_ALERT_CYCLE_TYPE,
  BATCH_ALERT_DEFAULT_CONFIG,
  BATCH_DIMENSION_FIELD_CODE,
  BATCH_ALERT_WEEK_DAYS,
  BATCH_ALERT_WEEK_OPTIONS,
  isBatchAlertCycleType
} from './fieldCode'
import { buildEmotionalLevelNodeValue, parseEmotionalLevelNodeValue } from './emotionalLevel'
import {
  ACCOUNT_RESOURCE_DIMENSION_FIELDS,
  CASCADER_DIMENSION_FIELDS,
  SELECT_MULTI_DIMENSION_FIELDS,
  TEXT_DIMENSION_FIELDS,
  TEXT_LEXICON_DIMENSION_FIELDS,
  getBatchDimensionCascaderOptions,
  getBatchDimensionSelectOptions,
  isAttributeLabelDimensionField,
  isLexiconValueType
} from './dimensionFieldRegistry'
import {
  getBatchMetricTypeOptions,
  getBatchMetricUnit,
  getBatchMetricValueTypeOptions,
  getBatchMetricWildcardOptions
} from './metric'
import type {
  BatchAlertConfig,
  BatchCascaderOption,
  BatchCcPersonnelItem,
  BatchDimensionRow,
  BatchMetricLogic,
  BatchMetricRow,
  BatchResourceLinkageValue,
  BatchRuleFormOptions,
  BatchRuleQueryParams,
  BatchRulePageResult,
  BatchRuleRecord,
  BatchRuleStatus,
  BatchRuleView,
  BatchSelectOption
} from './types'

/**
 * 批量规则页面统一读取批量规则字典，避免业务函数分散判断数据源。
 * @returns Record<string, any>
 */
const getBatchConditions = () => batchEventStore.batchConditions || {}
/**
 * 从 store 构建表单选项（列表页已加载，表单直接复用）
 * @returns BatchRuleFormOptions
 */
export const buildBatchRuleFormOptions = (): BatchRuleFormOptions => {
  const { formOptions } = batchEventStore
  return {
    weekOptions: [...BATCH_ALERT_WEEK_OPTIONS],
    monthDayOptions: Array.from({ length: 31 }, (_, index) => ({
      label: `${index + 1}日`,
      value: String(index + 1)
    })),
    timeOptions: buildTimeOptions(),
    metricLogicOptions: resolveMetricLogicOptions(),
    dimensionDefinitions: formOptions.dimensionDefinitions,
    metricFieldOptions: formOptions.metricFieldOptions,
    metricTypeMap: formOptions.metricTypeMap,
    metricWildcardMap: formOptions.metricWildcardMap,
    metricValueTypeMap: formOptions.metricValueTypeMap,
    metricUnitMap: formOptions.metricUnitMap
  }
}

const DEPRECATED_DIMENSION_FIELDS = ['standpoint']

const CASCADER_MULTI_DIMENSION_FIELDS: string[] = [
  BATCH_DIMENSION_FIELD_CODE.DATA_SOURCE,
  BATCH_DIMENSION_FIELD_CODE.EXPERIENCE_CODE,
  BATCH_DIMENSION_FIELD_CODE.AD_TYPE,
  BATCH_DIMENSION_FIELD_CODE.EMOTIONAL_LEVEL
]

const MULTI_VALUE_DIMENSION_FIELDS: string[] = [
  ...CASCADER_MULTI_DIMENSION_FIELDS,
  ...SELECT_MULTI_DIMENSION_FIELDS
]

/**
 * 统一生成批量规则默认预警配置，确保新建时不再依赖本地测试数据。
 * @returns BatchAlertConfig
 */
const buildDefaultAlertConfig = (): BatchAlertConfig => ({
  ...BATCH_ALERT_DEFAULT_CONFIG
})

/**
 * 查询批量规则分页列表。
 * @param params 分页参数
 * @returns Promise<BatchRulePageResult>
 */
export const fetchBatchEventRulePage = async (
  params: BatchRuleQueryParams
): Promise<BatchRulePageResult> => {
  const response = await queryBatchRulePage({
    categoryId: params.categoryId || undefined,
    pageNum: params.pageNum,
    pageSize: params.pageSize,
    ruleName: params.keyword?.trim() || undefined
  })

  if (!response.success || !response.result) {
    return { total: 0, list: [] }
  }

  return {
    total: Number(response.result?.total || 0),
    list: (response.result?.list || []).map(mapRuleListItem)
  }
}

/**
 * 返回批量规则表单默认值，供新建场景和弹窗重置复用。
 * @param categoryId 当前分类 ID
 * @param categoryName 当前分类名称
 * @returns BatchRuleRecord
 */
export const createDefaultBatchRule = (categoryId = '', categoryName = ''): BatchRuleRecord => ({
  ruleId: '',
  ruleName: '',
  ruleType: 'batch',
  categoryId,
  categoryName,
  creatorName: '',
  creatorEmployeeId: '',
  brand: '',
  alertConfig: buildDefaultAlertConfig(),
  dimensions: [],
  metricLogic: 'AND',
  metrics: [],
  processPriority: '',
  auditMethod: 'manual',
  auditDepartment: null,
  auditor: null,
  mainDepartment: null,
  mainResponder: null,
  ccPersonnel: [],
  isEnabled: 'enabled'
})

/**
 * 查询规则详情并回填到表单结构。
 * @param ruleId 规则 ID
 * @param options 表单选项配置
 * @returns Promise<BatchRuleRecord>
 */
export const fetchBatchEventRuleDetail = async (
  ruleId: string,
  options: BatchRuleFormOptions
): Promise<BatchRuleRecord> => {
  const response = await getBatchRuleDetail(ruleId)

  if (!response.success || !response.result) {
    throw new Error(response.message || '获取规则详情失败')
  }

  return mapRuleDetail(response.result || {}, options)
}

/**
 * 复制批量规则，并统一处理失败提示。
 * @param ruleId 规则 ID
 * @returns Promise<void>
 */
export const copyBatchEventRule = async (ruleId: string): Promise<void> => {
  const response = await copyBatchRule(ruleId)

  if (!response.success) {
    throw new Error(response.message || '复制规则失败')
  }
}

/**
 * 保存批量规则。
 * @param payload 表单数据
 * @param options 表单选项配置
 * @returns Promise<void>
 */
export const saveBatchEventRule = async (
  payload: BatchRuleRecord,
  options: BatchRuleFormOptions
) => {
  const requestPayload = buildSavePayload(payload, options)
  const response = payload.ruleId
    ? await updateBatchRule(requestPayload)
    : await insertBatchRule(requestPayload)

  if (!response.success) {
    throw new Error(response.message || '保存规则失败')
  }
}

function mapRuleListItem(item: InsBatchRuleModel): BatchRuleView {
  const creator = parsePersonnelInfo(item.creator)
  const status = normalizeRuleStatus(item.isEnabled)

  return {
    ruleId: String(item.ruleId || ''),
    ruleName: String(item.ruleName || ''),
    ruleType: 'batch',
    categoryId: String(item.categoryId || ''),
    categoryName: String(item.categoryName || ''),
    creatorName: creator.name,
    creatorEmployeeId: creator.employeeId,
    brand: String(item.brandCode || ''),
    alertConfig: buildDefaultAlertConfig(),
    dimensions: [],
    metricLogic: 'AND',
    metrics: [],
    processPriority: String(item.processPriority || ''),
    auditMethod: item.auditMethod === 'auto' ? 'auto' : 'manual',
    auditDepartment: null,
    auditor: null,
    mainDepartment: null,
    mainResponder: null,
    ccPersonnel: [],
    isEnabled: status,
    creatorDisplayName: creator.displayName,
    status
  }
}

function mapRuleDetail(item: InsBatchRuleModel, options: BatchRuleFormOptions): BatchRuleRecord {
  const auditList = normalizePersonnelList(item.auditor).slice(0, 1)
  const mainList = normalizePersonnelList(item.mainResponder).slice(0, 1)
  // 详情接口中的配置字段既可能直接返回 JSON 字符串，也可能已被网关转成对象，这里统一先反序列化。
  const dimensionConfig = parseConfigPayload<Record<string, any>>(item.dimensionConfig, {})
  const indicatorConfig = parseConfigPayload<Record<string, any>>(item.indicatorConfig, {})
  const indicatorResult = parseIndicators(indicatorConfig, options)

  return {
    ruleId: String(item.ruleId || ''),
    ruleName: String(item.ruleName || ''),
    ruleType: 'batch',
    categoryId: String(item.categoryId || ''),
    categoryName: String(item.categoryName || ''),
    creatorName: '',
    creatorEmployeeId: '',
    brand: String(item.brandCode || ''),
    alertConfig: parseAlertConfig(item),
    dimensions: parseDimensions(dimensionConfig, String(item.brandCode || ''), options),
    metricLogic: indicatorResult.logic,
    metrics: indicatorResult.metrics,
    processPriority: String(item.processPriority || ''),
    auditMethod: item.auditMethod === 'auto' ? 'auto' : 'manual',
    // 详情回填优先兼容单点规则同款字段结构，老数据再回退到历史扁平人员结构。
    auditDepartment: parseDepartmentModel(item.auditDepartment) || toDepartment(auditList[0]),
    auditor: parseUserModel(item.auditor) || toUser(auditList[0]),
    mainDepartment: parseDepartmentModel(item.mainDepartment) || toDepartment(mainList[0]),
    mainResponder: parseUserModel(item.mainResponder) || toUser(mainList[0]),
    ccPersonnel: normalizePersonnelList(item.ccPersonnel),
    isEnabled: normalizeRuleStatus(item.isEnabled)
  }
}

function buildSavePayload(
  payload: BatchRuleRecord,
  options: BatchRuleFormOptions
): BatchRuleSavePayload {
  return {
    ruleId: payload.ruleId || undefined,
    ruleName: payload.ruleName.trim(),
    categoryId: payload.categoryId || undefined,
    brandCode: resolveBrandCode(payload.brand),
    alertType: payload.alertConfig.cycleType,
    alertFrequency: '1',
    alertTime: buildAlertTime(payload.alertConfig),
    // 新增/编辑接口要求配置字段按字符串传输，这里统一在 API 边界序列化，避免表单层感知协议细节。
    dimensionConfig: stringifyConfigPayload(serializeDimensions(payload.dimensions, options)),
    indicatorConfig: stringifyConfigPayload(
      serializeIndicators(payload.metricLogic, payload.metrics, options)
    ),
    processPriority: payload.processPriority,
    auditDepartment: buildDepartmentPayload(payload.auditDepartment),
    auditor: buildUserPayload(payload.auditor),
    auditMethod: payload.auditMethod,
    mainDepartment: buildDepartmentPayload(payload.mainDepartment),
    mainResponder: buildUserPayload(payload.mainResponder),
    ccPersonnel: buildCcPersonnelPayload(payload.ccPersonnel),
    isEnabled: payload.isEnabled
  }
}

function parseAlertConfig(item: InsBatchRuleModel): BatchAlertConfig {
  const rawCycleType = String(item.alertType || '')
  const cycleType = isBatchAlertCycleType(rawCycleType)
    ? rawCycleType
    : BATCH_ALERT_DEFAULT_CONFIG.cycleType
  const rawAlertTime = String(item.alertTime || '')
  const scheduledTime = normalizeScheduleTime(rawAlertTime)
  const parsedWeekDay = extractWeekDay(rawAlertTime)
  const parsedMonthDay = extractMonthDay(rawAlertTime)

  return {
    cycleType,
    weekDay:
      cycleType === BATCH_ALERT_CYCLE_TYPE.WEEKLY
        ? parsedWeekDay || BATCH_ALERT_DEFAULT_CONFIG.weekDay
        : BATCH_ALERT_DEFAULT_CONFIG.weekDay,
    monthDay:
      cycleType === BATCH_ALERT_CYCLE_TYPE.MONTHLY
        ? parsedMonthDay || BATCH_ALERT_DEFAULT_CONFIG.monthDay
        : BATCH_ALERT_DEFAULT_CONFIG.monthDay,
    pushTime: scheduledTime
  }
}

function parseIndicators(
  payload: unknown,
  options: BatchRuleFormOptions
): { logic: BatchMetricLogic; metrics: BatchMetricRow[] } {
  const result = parseJsonSafely<any>(payload, {})
  const conditions = Array.isArray(result.conditions) ? result.conditions : []

  return {
    logic: result.logic_operator === 'OR' ? 'OR' : 'AND',
    metrics: conditions.map((item, index) => {
      const indicator = String(item?.indicator || '')
      const indicatorType = String(item?.indicator_type || '')
      const operator = String(item?.operator || '')
      const valueType = String(item?.value_type || '')
      const metricTypeOptions = getBatchMetricTypeOptions(options, indicator)
      const metricWildcardOptions = getBatchMetricWildcardOptions(options, indicator, indicatorType)
      const metricValueTypeOptions = getBatchMetricValueTypeOptions(
        options,
        indicator,
        indicatorType
      )

      return {
        id: `metric-${index}-${Date.now()}`,
        metric: matchOptionValue(options.metricFieldOptions || [], indicator),
        metricType: matchOptionValue(metricTypeOptions, indicatorType),
        wildcard: matchOptionValue(metricWildcardOptions, operator),
        valueType: matchOptionValue(metricValueTypeOptions, valueType),
        value: String(item?.value ?? ''),
        unit: resolveMetricUnit(
          indicator,
          indicatorType,
          valueType,
          String(item?.unit || ''),
          options
        )
      }
    })
  }
}

function parseDimensions(
  payload: unknown,
  brandCode: string,
  options: BatchRuleFormOptions
): BatchDimensionRow[] {
  const result = parseJsonSafely<any>(payload, [])
  const sourceList: Array<Record<string, any> & { field: string }> =
    result && typeof result === 'object' && !Array.isArray(result)
      ? Object.entries(result).map(([field, item]) => ({
          field,
          ...(typeof item === 'object' && item ? item : {})
        }))
      : []

  return sourceList
    .filter(item => item?.enable !== false)
    .filter(item => {
      // 标准观点维度已并入体验代码，历史废弃字段回填时直接跳过，避免再次进入表单。
      return !DEPRECATED_DIMENSION_FIELDS.includes(String(item?.field || ''))
    })
    .map((item, index) => {
      const field = String(item?.field || '')
      // 详情字段统一按最新接口编码匹配定义，避免页面继续兼容历史字段别名。
      const definition = options.dimensionDefinitions.find(opt => opt.code === field)

      return {
        id: `dim-${index}-${Date.now()}`,
        field,
        wildcard: matchNameCodeValue(definition?.logicalOperator || [], item?.match_type),
        valueType: matchNameCodeValue(definition?.condition || [], item?.value_type),
        statMode: matchNameCodeValue(definition?.countingMethod || [], item?.calculation_method),
        value: parseDimensionValue(
          field,
          item?.values,
          brandCode,
          String(item?.value_type || ''),
          String(item?.parent_value || ''),
          definition
        )
      }
    })
}

function serializeIndicators(
  logic: BatchMetricLogic,
  metrics: BatchMetricRow[],
  options: BatchRuleFormOptions
) {
  return {
    logic_operator: logic,
    // 指标配置新协议改为 conditions，且 indicator、indicator_type、operator、value_type、unit 全部提交 code。
    conditions: (metrics || []).map(item => {
      const indicator = matchOptionValue(options.metricFieldOptions || [], item.metric)
      const indicatorType = matchOptionValue(
        getBatchMetricTypeOptions(options, indicator),
        item.metricType
      )
      const operator = matchOptionValue(
        getBatchMetricWildcardOptions(options, indicator, indicatorType),
        item.wildcard
      )
      const valueType = matchOptionValue(
        getBatchMetricValueTypeOptions(options, indicator, indicatorType),
        item.valueType
      )

      return {
        indicator,
        indicator_type: indicatorType,
        operator,
        value_type: valueType,
        value: normalizeMetricValue(item.value),
        unit: resolveMetricUnit(indicator, indicatorType, valueType, item.unit, options)
      }
    })
  }
}

function serializeDimensions(dimensions: BatchDimensionRow[], options: BatchRuleFormOptions) {
  return (dimensions || []).reduce<Record<string, any>>((result, item) => {
    const definition = options.dimensionDefinitions.find(opt => opt.code === item.field)
    const values = serializeDimensionValues(item.field, item.value, item.valueType)
    const lexiconType = resolveDimensionLexiconType(item.field, item.value, item.valueType)
    const parentValue = resolveDimensionParentValue(item.field, item.value)

    result[item.field] = {
      field: item.field,
      // 维度条件相关字段新增/编辑统一传 code，保证和条件配置接口口径一致。
      match_type: matchNameCodeValue(definition?.logicalOperator || [], item.wildcard),
      value_type: matchNameCodeValue(definition?.condition || [], item.valueType),
      calculation_method: matchNameCodeValue(definition?.countingMethod || [], item.statMode) || '',
      value_level: resolveDimensionValueLevel(item.field, item.value),
      values
    }
    if (lexiconType) {
      result[item.field].lexicon_type = lexiconType
    }
    if (parentValue !== null) {
      // 情感程度需要把父级 value 一并上报，便于后端按同父级语义还原当前选择分组。
      result[item.field].parent_value = parentValue
    }

    return result
  }, {})
}

function parseDimensionValue(
  field: string,
  rawValue: unknown,
  brandCode: string,
  dimensionValueType = '',
  parentValue = '',
  definition?: { name?: string; code?: string }
) {
  const values = normalizeDimensionValues(rawValue)

  if (!values.length) {
    if (ACCOUNT_RESOURCE_DIMENSION_FIELDS.includes(field)) {
      return null
    }

    if (TEXT_LEXICON_DIMENSION_FIELDS.includes(field) && isLexiconValueType(dimensionValueType)) {
      return null
    }

    return MULTI_VALUE_DIMENSION_FIELDS.includes(field) ? [] : ''
  }

  if (ACCOUNT_RESOURCE_DIMENSION_FIELDS.includes(field)) {
    return parseResourceValue(values, getAccountResourceOptions())
  }

  if (TEXT_LEXICON_DIMENSION_FIELDS.includes(field) && isLexiconValueType(dimensionValueType)) {
    return parseResourceValue(values, getTitleResourceOptions())
  }

  if (TEXT_DIMENSION_FIELDS.includes(field)) {
    return values[0] || ''
  }

  if (field === BATCH_DIMENSION_FIELD_CODE.EMOTIONAL_LEVEL) {
    return values.map(item => buildEmotionalLevelNodeValue(item, parentValue)).filter(Boolean)
  }

  if (CASCADER_DIMENSION_FIELDS.includes(field)) {
    const options = getCascaderOptions(field)
    const parsedValues = values
      .map(item => resolveCascaderStoredValue(options, item))
      .filter(Boolean)
    return field === BATCH_DIMENSION_FIELD_CODE.REGULATION_CONTENT_TYPE
      ? parsedValues[0] || values[0]
      : parsedValues
  }

  const options = getSelectOptions(field, brandCode)
  const parsedValues = values.map(item => resolveStoredSelectValue(options, item)).filter(Boolean)
  return SELECT_MULTI_DIMENSION_FIELDS.includes(field) ||
    isAttributeLabelDimensionField(field, definition)
    ? parsedValues
    : parsedValues[0] || values[0]
}

/**
 * 维度配置新协议要求统一以数组形式提交 values，
 * 字典/级联类字段按当前界面绑定的 key/code 直接提交，避免被误转成展示文案。
 * @param field 维度字段
 * @param value 表单值
 * @returns string[]
 */
function serializeDimensionValues(field: string, value: unknown, dimensionValueType = '') {
  if (ACCOUNT_RESOURCE_DIMENSION_FIELDS.includes(field)) {
    return serializeResourceValues(value)
  }

  if (TEXT_LEXICON_DIMENSION_FIELDS.includes(field) && isLexiconValueType(dimensionValueType)) {
    return serializeResourceValues(value)
  }

  if (TEXT_DIMENSION_FIELDS.includes(field)) {
    const text = String(value || '').trim()
    return text ? [text] : []
  }

  if (field === BATCH_DIMENSION_FIELD_CODE.DATA_SOURCE) {
    const values = Array.isArray(value) ? value : [value]
    return serializeDataSourceValues(values)
  }

  if (field === BATCH_DIMENSION_FIELD_CODE.EMOTIONAL_LEVEL) {
    const values = Array.isArray(value) ? value : [value]
    return values.map(item => parseEmotionalLevelNodeValue(item).value).filter(Boolean)
  }

  const values = Array.isArray(value) ? value : [value]
  return values.map(item => String(item || '').trim()).filter(Boolean)
}

function getSelectOptions(field: string, brandCode: string): BatchSelectOption[] {
  // 省份等异步字典维度若当前未拿到选项，则仍然返回空数组，回填阶段会保留原始值。
  return getBatchDimensionSelectOptions(field, brandCode)
}

function getCascaderOptions(field: string): BatchCascaderOption[] {
  return getBatchDimensionCascaderOptions(field)
}

function getAccountResourceOptions() {
  return (batchEventStore.dataResource.accountList || []) as any[]
}

function getTitleResourceOptions() {
  return (batchEventStore.dataResource.allList || []) as any[]
}

/**
 * 资源联动维度详情回填仅识别当前协议中的父级 id 与子级 id，
 * 不再兼容历史中文名称，避免旧口径继续污染新协议。
 * @param rawValues 接口中的原始 values
 * @param options 资源树选项
 * @returns BatchResourceLinkageValue | null
 */
function parseResourceValue(rawValues: string[], options: any[]): BatchResourceLinkageValue | null {
  const values = (rawValues || []).map(item => String(item || '').trim()).filter(Boolean)
  if (!values.length) {
    return null
  }

  const normalizedValues = values.map(item => item.replace(/^@/, ''))
  const parentTypeMap = new Map<string, string>()
  const childIdMap = new Map<string, { parentId: string; type: string }>()

  options.forEach(parent => {
    const parentId = String(parent?.id || '').trim()
    const parentType = String(parent?.type || '').trim()

    if (parentId) {
      parentTypeMap.set(parentId, parentType)
    }

    ;(parent.keywordList || []).forEach((child: any) => {
      const childId = String(child?.id || '').trim()

      if (childId) {
        childIdMap.set(childId, {
          parentId,
          type: parentType
        })
      }
    })
  })

  const parentId = normalizedValues.find(item => parentTypeMap.has(item))
  if (parentId) {
    return {
      '1': [parentId],
      type: parentTypeMap.get(parentId) || ''
    }
  }

  const childIds = normalizedValues.map(item => (childIdMap.has(item) ? item : '')).filter(Boolean)

  if (!childIds.length) {
    return null
  }

  const firstChild = childIdMap.get(childIds[0])
  return {
    '2': childIds,
    type: firstChild?.type || ''
  }
}

/**
 * 资源联动维度提交时统一传 id，不再回退为中文名称；
 * 父级全选提交父级 id，子级勾选提交子级 id。
 * @param value 表单中的资源联动值
 * @returns string[]
 */
function serializeResourceValues(value: unknown) {
  const currentValue = value as BatchResourceLinkageValue | null
  const parentIds = Array.isArray(currentValue?.['1'])
    ? currentValue!['1'].map(item => String(item || '').trim()).filter(Boolean)
    : []
  if (parentIds.length) {
    return parentIds
  }

  return Array.isArray(currentValue?.['2'])
    ? currentValue!['2'].map(item => String(item || '').trim()).filter(Boolean)
    : []
}

/**
 * 词库类维度在提交时需要额外透传资源树返回的 type，
 * 这样后端才能区分账号词库、规则词库等不同词库来源。
 * @param field 维度字段
 * @param value 表单中的资源联动值
 * @param dimensionValueType 当前值类型
 * @returns string
 */
function resolveDimensionLexiconType(field: string, value: unknown, dimensionValueType = '') {
  const isAccountLexiconField = ACCOUNT_RESOURCE_DIMENSION_FIELDS.includes(field)
  const isTitleLexiconField =
    TEXT_LEXICON_DIMENSION_FIELDS.includes(field) && isLexiconValueType(dimensionValueType)

  if (!isAccountLexiconField && !isTitleLexiconField) {
    return ''
  }

  return String((value as BatchResourceLinkageValue | null)?.type || '').trim()
}

/**
 * 资源联动维度使用对象结构承载两级选择结果：
 * - `1` 表示选中了父级全量，此时应提交一级层级
 * - `2` 表示选中了子级明细，此时应提交二级层级
 * 之前这里遗漏了该结构，导致标题/原文等词库维度始终按一级上报。
 * @param field 维度字段
 * @param value 表单取值
 * @returns number | null
 */
function resolveResourceDimensionValueLevel(field: string, value: unknown) {
  if (![...ACCOUNT_RESOURCE_DIMENSION_FIELDS, ...TEXT_LEXICON_DIMENSION_FIELDS].includes(field)) {
    return null
  }

  const currentValue =
    value && typeof value === 'object' && !Array.isArray(value)
      ? (value as BatchResourceLinkageValue)
      : null

  const parentIds = Array.isArray(currentValue?.['1'])
    ? currentValue!['1'].map(item => String(item || '').trim()).filter(Boolean)
    : []
  if (parentIds.length) {
    return 1
  }

  const childIds = Array.isArray(currentValue?.['2'])
    ? currentValue!['2'].map(item => String(item || '').trim()).filter(Boolean)
    : []
  if (childIds.length) {
    return 2
  }

  return null
}

/**
 * 新版维度配置通过 value_level 记录级联层级，便于后端按选中 code 精确还原层级语义。
 * @param field 维度字段
 * @param value 表单取值
 * @returns number
 */
function resolveDimensionValueLevel(field: string, value: unknown) {
  const resourceLevel = resolveResourceDimensionValueLevel(field, value)
  if (resourceLevel) {
    return resourceLevel
  }

  if (field === BATCH_DIMENSION_FIELD_CODE.DATA_SOURCE) {
    return serializeDataSourceValues(Array.isArray(value) ? value : [value]).length ? 3 : 1
  }

  const cascaderLevelFields: string[] = [
    BATCH_DIMENSION_FIELD_CODE.EXPERIENCE_CODE,
    BATCH_DIMENSION_FIELD_CODE.REGULATION_CONTENT_TYPE,
    BATCH_DIMENSION_FIELD_CODE.AD_TYPE,
    BATCH_DIMENSION_FIELD_CODE.EMOTIONAL_LEVEL
  ]

  if (!cascaderLevelFields.includes(field)) {
    return 1
  }

  const options = getCascaderOptions(field)
  const values = Array.isArray(value) ? value : [value]
  const level = values
    .map(item => findCascaderNode(options, String(item || ''))?.depth || 0)
    .find(depth => depth > 0)

  return level || 1
}

/**
 * 情感程度需要额外透传当前选中层级的父级 value：
 * - 选中二级/三级节点时传直属父级 value
 * - 选中顶级节点（如中性）时没有父级，统一传空字符串，便于后端按“根节点”处理
 * 由于前端已限制为同父级同层级多选，因此多选场景取首项父级即可代表整组数据。
 * @param field 维度字段
 * @param value 表单取值
 * @returns string | null
 */
function resolveDimensionParentValue(field: string, value: unknown) {
  if (field !== BATCH_DIMENSION_FIELD_CODE.EMOTIONAL_LEVEL) {
    return null
  }

  const values = (Array.isArray(value) ? value : [value])
    .map(item => String(item || '').trim())
    .filter(Boolean)

  if (!values.length) {
    return ''
  }

  return parseEmotionalLevelNodeValue(values[0]).parentValue
}

/**
 * 维度 values 按当前协议统一使用字符串数组，编辑回填直接复用该结构。
 * @param rawValue 原始值
 * @returns string[]
 */
function normalizeDimensionValues(rawValue: unknown) {
  if (Array.isArray(rawValue)) {
    return rawValue.map(item => String(item || '').trim()).filter(Boolean)
  }

  return []
}

/**
 * 指标阈值在界面中使用输入框维护，提交时优先还原为 number，避免全部以字符串入库。
 * @param value 输入值
 * @returns string | number
 */
function normalizeMetricValue(value: unknown) {
  const text = String(value ?? '').trim()
  if (!text) return ''
  if (/^-?\d+(\.\d+)?$/.test(text)) {
    return Number(text)
  }
  return text
}

/**
 * unit 由配置接口中的 valueFormat 决定；详情 unit 仅用于配置缺失时兜底。
 * @param indicator 指标 code
 * @param indicatorType 指标类型 code
 * @param valueType 值类型 code
 * @param currentUnit 当前 unit
 * @param options 表单选项配置
 * @returns string
 */
function resolveMetricUnit(
  indicator: string,
  indicatorType: string,
  valueType: string,
  currentUnit = '',
  options: BatchRuleFormOptions
) {
  return (
    getBatchMetricUnit(options, indicator, indicatorType, valueType) ||
    String(currentUnit || '') ||
    ''
  )
}

function normalizePersonnelList(payload: unknown): BatchCcPersonnelItem[] {
  const result = parseJsonSafely<any>(payload, [])
  const sourceList = Array.isArray(result) ? result : result ? [result] : []

  return sourceList
    .map(item => ({
      id: String(item.id || ''),
      deptNo: String(item.deptNo || ''),
      deptName: String(item.deptName || ''),
      isAll: Boolean(item.isAll),
      userId: String(item.userId || ''),
      employeeId: String(item.employeeId || ''),
      userName: String(item.userName || '')
    }))
    .filter(item => item.id || item.userId)
}

function toDepartment(item?: BatchCcPersonnelItem) {
  return item?.id ? { id: item.id, deptNo: item.deptNo, name: item.deptName } : null
}

function toUser(item?: BatchCcPersonnelItem) {
  return item?.userId ? { id: item.userId, employeeId: item.employeeId, name: item.userName } : null
}

/**
 * 兼容详情接口的两种部门结构：新协议直接返回部门对象，老协议仍可能混在人员对象里。
 * @param payload 接口返回的部门或人员字段
 * @returns 部门对象或 null
 */
function parseDepartmentModel(payload: unknown) {
  const source = parseJsonSafely<any>(payload, payload)
  const item = Array.isArray(source) ? source[0] : source

  if (!item) return null

  const id = String(item.id || item.deptId || '')
  const deptNo = String(item.deptNo || '')
  const name = String(item.name || item.deptName || '')

  if (!id || !name) {
    return null
  }

  return {
    id,
    deptNo,
    name
  }
}

/**
 * 兼容详情接口的两种人员结构：单点同款用户对象，以及历史的扁平抄送对象。
 * @param payload 接口返回的人员字段
 * @returns 人员对象或 null
 */
function parseUserModel(payload: unknown) {
  const source = parseJsonSafely<any>(payload, payload)
  const item = Array.isArray(source) ? source[0] : source

  if (!item) return null

  const id = String(item.userId || item.id || '')
  const employeeId = String(item.employeeId || '')
  const name = String(item.userName || item.name || '')

  if (!id || !name) {
    return null
  }

  return {
    id,
    employeeId,
    name
  }
}

/**
 * 保存时统一输出单点规则同款部门对象，避免再把部门信息折叠进人员字段。
 * @param department 表单中的部门对象
 * @returns 提交给接口的部门对象
 */
function buildDepartmentPayload(department: BatchRuleRecord['auditDepartment']) {
  if (!department?.id) {
    return null
  }

  return {
    id: department.id,
    deptNo: department.deptNo,
    name: department.name
  }
}

/**
 * 保存时统一输出单点规则同款人员对象，仅保留后端约定字段。
 * @param user 表单中的人员对象
 * @returns 提交给接口的人员对象
 */
function buildUserPayload(user: BatchRuleRecord['auditor']) {
  if (!user?.id) {
    return null
  }

  return {
    id: user.id,
    employeeId: user.employeeId,
    name: user.name
  }
}

/**
 * 抄送人员保持现有数组协议，但剔除空项，避免提交无效占位数据。
 * @param list 抄送人员列表
 * @returns 过滤后的抄送人员数组
 */
function buildCcPersonnelPayload(list: BatchCcPersonnelItem[] = []) {
  return (list || []).filter(item => item?.id || item?.userId)
}

function resolveBrandCode(value: string) {
  const brand = (getBatchConditions().selfBrand || []).find(
    (item: any) => item.key === value || item.value === value
  )
  return String(brand?.key || value || '')
}

function buildAlertTime(config: BatchAlertConfig) {
  const normalizedTime = normalizeScheduleTime(
    config.pushTime || BATCH_ALERT_DEFAULT_CONFIG.pushTime
  )

  if (config.cycleType === BATCH_ALERT_CYCLE_TYPE.WEEKLY) {
    return `${normalizeWeekDay(config.weekDay)} ${normalizedTime}`
  }

  if (config.cycleType === BATCH_ALERT_CYCLE_TYPE.MONTHLY) {
    return `${String(config.monthDay || BATCH_ALERT_DEFAULT_CONFIG.monthDay).replace(
      '日',
      ''
    )}日 ${normalizedTime}`
  }

  return normalizedTime
}

function normalizeRuleStatus(value: unknown): BatchRuleStatus {
  return String(value || '').trim() === 'enabled' ? 'enabled' : 'disabled'
}

function parsePersonnelInfo(payload: unknown) {
  const source = parseJsonSafely<any>(payload, payload)
  const item = Array.isArray(source) ? source[0] : source

  return {
    name: String(item?.name || item?.userName || ''),
    employeeId: String(item?.employeeId || ''),
    displayName:
      item?.name || item?.userName
        ? `${String(item?.name || item?.userName || '')}-${String(item?.employeeId || '')}`.replace(
            /-$/,
            ''
          )
        : ''
  }
}

function parseJsonSafely<T>(payload: unknown, fallback: T): T {
  if (payload === null || payload === undefined) return fallback
  if (typeof payload !== 'string') return payload as T
  if (!payload.trim()) return fallback
  try {
    return JSON.parse(payload) as T
  } catch {
    return fallback
  }
}

/**
 * 将配置字段统一反序列化为 JSON 对象，兼容接口返回 string / object 两种形态。
 * @param payload 接口原始字段
 * @param fallback 反序列化失败时的兜底值
 * @returns T
 */
function parseConfigPayload<T>(payload: unknown, fallback: T): T {
  return parseJsonSafely<T>(payload, fallback)
}

/**
 * 将配置字段统一序列化为字符串，满足新增/编辑接口的协议要求。
 * @param payload 需要提交的配置对象
 * @returns string
 */
function stringifyConfigPayload(payload: unknown) {
  try {
    return JSON.stringify(payload ?? {})
  } catch {
    return JSON.stringify({})
  }
}

function buildTimeOptions() {
  const options: BatchSelectOption[] = []
  for (let hour = 8; hour <= 24; hour += 1) {
    const text = String(hour).padStart(2, '0')
    options.push({ label: `${text}:00`, value: `${text}:00` })
    if (hour !== 24) options.push({ label: `${text}:30`, value: `${text}:30` })
  }
  return options
}

/**
 * 指标逻辑关系完全依赖批量规则字典，接口未返回时保持空数组。
 * @returns BatchSelectOption[]
 */
function resolveMetricLogicOptions() {
  const remoteOptions = (getBatchConditions().indicatorEffectRelation || [])
    .map((item: any) => ({
      label: String(item.value || ''),
      value: String(item.key || '')
    }))
    .filter(item => item.value)

  return uniqueOptions(remoteOptions)
}

function uniqueOptions(options: BatchSelectOption[]) {
  return Array.from(
    new Map(options.filter(item => item.value).map(item => [item.value, item])).values()
  )
}

function normalizeWeekDay(value: string) {
  return BATCH_ALERT_WEEK_DAYS[Number(value) - 1] || value || BATCH_ALERT_DEFAULT_CONFIG.weekDay
}

/**
 * 新版周级预警时间会把"周几 + 时分"拼在 alertTime 中，这里优先拆出周几用于编辑回填。
 * @param value 接口返回的预警时间
 * @returns string
 */
function extractWeekDay(value: string) {
  const match = String(value || '').match(/(周[一二三四五六日])/)
  return match?.[1] || ''
}

/**
 * 月级预警时间改为"几日 + 时分"格式，编辑场景需要从 alertTime 中还原日维度。
 * @param value 接口返回的预警时间
 * @returns string
 */
function extractMonthDay(value: string) {
  const match = String(value || '').match(/(\d{1,2})日/)
  return match?.[1] || ''
}

/**
 * 非小时级预警必须落成标准时分格式，非法值统一回退默认时间，避免把占位值提交给后端。
 * @param value 界面或接口中的时间值
 * @returns string
 */
function normalizeScheduleTime(value: string) {
  const match = String(value || '').match(/(\d{2}:\d{2})/)
  return match?.[1] || BATCH_ALERT_DEFAULT_CONFIG.pushTime
}

function matchOptionValue(options: BatchSelectOption[], rawValue: unknown) {
  const text = String(rawValue || '').trim()
  const match = options.find(item => item.value === text || item.label === text)
  return match?.value || text
}

function matchNameCodeValue(options: Array<{ name: string; code: string }>, rawValue: unknown) {
  const text = String(rawValue || '').trim()
  const match = options.find(item => item.code === text || item.name === text)
  return match?.code || text
}

type ResolvedCascaderNode = BatchCascaderOption & {
  depth: number
  parentValue: string
  pathLabels: string[]
}

/**
 * 级联维度详情回填仅识别当前协议中的 value/code，
 * 不再兼容历史中文文案，避免继续放大旧数据口径。
 * @param options 级联选项
 * @param rawValue 接口原始值
 * @returns string
 */
function resolveCascaderStoredValue(options: BatchCascaderOption[], rawValue: string) {
  const normalizedValue = String(rawValue || '').trim()
  if (!normalizedValue) {
    return ''
  }

  return findCascaderNode(options, normalizedValue)?.value || normalizedValue
}

/**
 * 下拉维度详情回填仅保留当前协议中的 option value；
 * 若字典暂未就绪，则保留原始 code 以便界面后续继续回显。
 * @param options 下拉选项
 * @param rawValue 接口原始值
 * @returns string
 */
function resolveStoredSelectValue(options: BatchSelectOption[], rawValue: string) {
  const normalizedValue = String(rawValue || '').trim()
  if (!normalizedValue) {
    return ''
  }

  return options.some(item => item.value === normalizedValue) ? normalizedValue : normalizedValue
}

function findCascaderNode(
  options: BatchCascaderOption[],
  value: string
): ResolvedCascaderNode | null {
  let matchedNode: ResolvedCascaderNode | null = null

  const walk = (
    nodes: BatchCascaderOption[],
    depth = 1,
    pathLabels: string[] = [],
    parentValue = ''
  ) => {
    nodes.forEach(node => {
      const nextPathLabels = [...pathLabels, node.label]
      if (!matchedNode && node.value === value) {
        matchedNode = {
          ...node,
          depth,
          parentValue,
          pathLabels: nextPathLabels
        }
      }

      if (!matchedNode && node.children?.length) {
        walk(node.children, depth + 1, nextPathLabels, node.value)
      }
    })
  }

  walk(options)
  return matchedNode
}

/**
 * 数据源保存统一只保留三级末级节点 code。
 * 页面正常交互下已经只能勾选叶子；这里再兜底一次，防止旧数据或异常回填把上级节点带入提交。
 * @param values 当前表单中的数据源取值
 * @returns string[]
 */
function serializeDataSourceValues(values: unknown[]) {
  const options = getCascaderOptions(BATCH_DIMENSION_FIELD_CODE.DATA_SOURCE)

  return Array.from(
    new Set(
      (values || [])
        .map(item => String(item || '').trim())
        .filter(Boolean)
        .filter(item => findCascaderNode(options, item)?.depth === 3)
    )
  )
}
