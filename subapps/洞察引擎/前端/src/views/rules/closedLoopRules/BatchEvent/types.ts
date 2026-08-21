export interface BatchCategoryRecord {
  id: string
  name: string
  parentId: string | null
  sort: number
}

export interface BatchCategoryTreeNode {
  id: string
  name: string
  parentId: string | null
  sort?: number
  sortOrder?: number
  ruleCount?: number
  children: BatchCategoryTreeNode[]
}

export interface BatchCategoryViewItem extends BatchCategoryTreeNode {
  depth: number
}

export interface BatchCategoryOption {
  label: string
  value: string
}

export interface BatchSelectOption {
  label: string
  value: string
}

export interface BatchNameCodeOption {
  name: string
  code: string
}

export interface BatchCascaderOption {
  label: string
  value: string
  disabled?: boolean
  children?: BatchCascaderOption[]
}

export interface BatchResourceKeywordOption {
  id: string
  name: string
}

export interface BatchResourceOption {
  id: string
  name: string
  type?: string
  keywordList?: BatchResourceKeywordOption[]
}

export interface BatchResourceLinkageValue {
  '1'?: string[]
  '2'?: string[]
  type?: string
}

export type BatchRuleStatus = 'enabled' | 'disabled'
export type BatchAlertCycleType = 'daily' | 'weekly' | 'monthly'
export type BatchMetricLogic = 'AND' | 'OR'
export type BatchAuditMethod = 'auto' | 'manual'

export interface BatchDeptModel {
  id: string
  deptNo: string
  name: string
}

export interface BatchUserModel {
  id: string
  employeeId: string
  name: string
}

export interface BatchCcPersonnelItem {
  id: string
  deptNo: string
  deptName: string
  isAll: boolean
  userId: string
  employeeId: string
  userName: string
}

export interface BatchAlertConfig {
  cycleType: BatchAlertCycleType
  weekDay: string
  monthDay: string
  pushTime: string
}

export interface BatchDimensionRow {
  id: string
  field: string
  wildcard: string
  valueType: string
  statMode: string
  value: any
}

export interface BatchDimensionDefinition {
  name: string
  code: string
  logicalOperator: BatchNameCodeOption[]
  condition: BatchNameCodeOption[]
  countingMethod?: BatchNameCodeOption[]
}

export type BatchDimensionInputType = 'select' | 'cascader' | 'input' | 'resource-linkage'

export interface BatchDimensionFieldMeta {
  definition: BatchDimensionDefinition
  statModeOptions: BatchSelectOption[]
  inputType: BatchDimensionInputType
  multiple: boolean
  valueOptions?: BatchSelectOption[]
  cascaderOptions?: BatchCascaderOption[]
  checkStrictly?: boolean
  sameLevelOnly?: boolean
  sameParentOnly?: boolean
  sameRootOnly?: boolean
  showAllLevels?: boolean
  showCheckedStrategy?: 'child' | 'parent'
  separator?: string
  placeholder?: string
  maxLength?: number
  multipleLimit?: number
  resourceOptions?: BatchResourceOption[]
  resourceLoading?: boolean
  resourcePrefix?: string
}

export interface BatchMetricRow {
  id: string
  metric: string
  metricType: string
  wildcard: string
  valueType: string
  value: string
  unit: string
}

export interface BatchRuleRecord {
  ruleId: string
  ruleName: string
  ruleType: string
  categoryId: string
  categoryName: string
  creatorName: string
  creatorEmployeeId: string
  brand: string
  alertConfig: BatchAlertConfig
  dimensions: BatchDimensionRow[]
  metricLogic: BatchMetricLogic
  metrics: BatchMetricRow[]
  processPriority: string
  auditMethod: BatchAuditMethod
  auditDepartment: BatchDeptModel | null
  auditor: BatchUserModel | null
  mainDepartment: BatchDeptModel | null
  mainResponder: BatchUserModel | null
  ccPersonnel: BatchCcPersonnelItem[]
  isEnabled: BatchRuleStatus
}

export interface BatchRuleView extends BatchRuleRecord {
  creatorDisplayName: string
  status: BatchRuleStatus
}

export interface BatchRuleQueryParams {
  categoryId?: string
  keyword?: string
  pageNum: number
  pageSize: number
}

export interface BatchRulePageResult {
  total: number
  list: BatchRuleView[]
}

export interface SaveBatchCategoryParams {
  id?: string
  name: string
  parentId?: string | null
}

export interface BatchCategorySubmitResult {
  categoryId: string
  categoryName: string
  mode: 'create' | 'edit'
}

export interface BatchRuleSubmitResult {
  ruleId: string
  mode: 'create' | 'edit'
}

/**
 * 预警周期组件只依赖日期与时间相关选项，避免透传整份表单配置。
 */
export interface BatchAlertCycleFormOptions {
  weekOptions: BatchSelectOption[]
  monthDayOptions: BatchSelectOption[]
  timeOptions: BatchSelectOption[]
}

/**
 * 维度配置只消费字段定义，具体字典值由页面按实时 store 数据补齐。
 */
export interface BatchDimensionConfigOptions {
  dimensionDefinitions: BatchDimensionDefinition[]
}

/**
 * 指标配置只依赖指标联动关系，统一收敛为指标专属配置类型。
 */
export interface BatchMetricConfigOptions {
  metricLogicOptions: BatchSelectOption[]
  metricFieldOptions: BatchSelectOption[]
  metricTypeMap: Record<string, BatchSelectOption[]>
  metricWildcardMap: Record<string, BatchSelectOption[]>
  metricValueTypeMap: Record<string, BatchSelectOption[]>
  metricUnitMap: Record<string, string>
}

export type BatchRuleFormOptions = BatchAlertCycleFormOptions &
  BatchDimensionConfigOptions &
  BatchMetricConfigOptions
