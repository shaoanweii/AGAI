import request from './index'

/**
 * 批量事件分类树查询参数。
 */
export interface BatchRuleCategoryListQuery {
  searchKey?: string
}

/**
 * 批量事件分类树原始节点。
 */
export interface BatchRuleCategoryNode {
  id?: string | number
  name?: string
  parentId?: string | number | null
  count?: string | number
  ruleCount?: string | number
  sort?: string | number
  sortOrder?: string | number
  type?: string
  status?: string
  operator?: string
  createTime?: string
  updateTime?: string
  delFlag?: number
  children?: BatchRuleCategoryNode[]
}

/**
 * 批量事件分类新增/编辑载荷。
 */
export interface BatchRuleCategorySavePayload {
  id?: string
  name: string
  parentId?: string | null
}

/**
 * 批量事件分类接口统一响应结构。
 */
export interface BatchRuleCategoryResponse<T> {
  success?: boolean
  message: string
  code: string
  result: T
  tid?: string
}

/**
 * 批量事件规则分页查询参数。
 */
export interface BatchRulePageQuery {
  pageNum: number
  pageSize: number
  categoryId?: string
  ruleName?: string
}

/**
 * 批量事件规则保存载荷。
 * creator、updater、createTime、updateTime 由后端按登录态和落库规则自动维护，前端不传。
 */
export interface BatchRuleSavePayload {
  ruleId?: string
  ruleName: string
  categoryId?: string
  brandCode: string
  alertType: string
  alertFrequency: string | number
  alertTime: string
  dimensionConfig: string
  indicatorConfig: string
  processPriority: string
  auditDepartment?: Record<string, any> | null
  auditor?: Record<string, any> | null
  auditMethod: string
  mainDepartment?: Record<string, any> | null
  mainResponder?: Record<string, any> | null
  ccPersonnel?: Record<string, any>[]
  isEnabled: string
}

/**
 * 批量事件规则分页接口原始数据项。
 */
export interface InsBatchRuleModel {
  ruleId?: string
  ruleName?: string
  categoryId?: string
  categoryName?: string
  brandCode?: string
  brandName?: string
  alertType?: string
  alertFrequency?: string
  alertTime?: string
  alertCron?: string
  dimensionConfig?: string | Record<string, any>
  indicatorConfig?: string | Record<string, any>
  processPriority?: string
  auditDepartment?: string | Record<string, any>
  auditor?: string | Record<string, any> | Record<string, any>[]
  auditMethod?: string
  mainDepartment?: string | Record<string, any>
  mainResponder?: string | Record<string, any> | Record<string, any>[]
  ccPersonnel?: string | Record<string, any>[]
  isEnabled?: string
  version?: number
  creator?: string
  updater?: string
  createTime?: string
  updateTime?: string
}

/**
 * 批量事件规则分页结果。
 */
export interface BatchRulePageModel {
  total?: number
  list?: InsBatchRuleModel[]
}

/**
 * 批量规则维度条件配置项。
 */
export interface BatchRuleConditionOption {
  name?: string
  code?: string
}

/**
 * 批量规则维度条件配置。
 */
export interface BatchRuleConditionConfigModel {
  name?: string
  code?: string
  logicalOperator?: BatchRuleConditionOption[]
  condition?: BatchRuleConditionOption[]
  countingMethod?: BatchRuleConditionOption[]
}

/**
 * 省份列表接口字段已在联调中确认：
 * 页面使用 provinceCode 作为提交值，provinceName 作为展示文案。
 */
export interface BatchProvinceListItem {
  order?: string | null
  id?: string | number | null
  areaCode?: string | null
  areaName?: string | null
  provinceCode?: string
  provinceName?: string
}

/**
 * 批量规则指标类型配置。
 */
export interface BatchRuleIndicatorTypeModel {
  name?: string
  code?: string
  conditions?: BatchRuleIndicatorConditionModel[]
}

/**
 * 批量规则指标条件配置。
 */
export interface BatchRuleIndicatorConditionModel {
  operatorName?: string
  operatorCode?: string
  valueTypeName?: string
  valueTypeCode?: string
  valueFormat?: string
}

/**
 * 批量规则指标配置。
 */
export interface BatchRuleIndicatorConfigModel {
  name?: string
  code?: string
  types?: BatchRuleIndicatorTypeModel[]
  conditions?: BatchRuleIndicatorConditionModel[]
}

/**
 * 批量事件规则接口统一响应结构。
 */
export interface BatchRuleResponse<T> {
  success?: boolean
  message: string
  code: string
  result: T
  tid?: string
}

/**
 * 查询批量事件分类树。
 */
export const findBatchRuleCategoryList = (
  params?: BatchRuleCategoryListQuery
): Promise<BatchRuleCategoryResponse<BatchRuleCategoryNode[]>> => {
  return request<BatchRuleCategoryNode[]>({
    // 后端已切换为 GET 入参规范，这里统一通过查询参数透传，避免请求体被服务端忽略。
    method: 'GET',
    url: '/insights/insBatchRuleCategory/tree',
    params
  })
}

/**
 * 新建批量事件分类。
 */
export const createBatchRuleCategory = (
  data: BatchRuleCategorySavePayload
): Promise<BatchRuleCategoryResponse<boolean | BatchRuleCategoryNode>> => {
  return request<boolean | BatchRuleCategoryNode>({
    method: 'POST',
    // 创建分类接口按当前后端路由规范使用单斜杠路径，避免网关严格匹配时 404
    url: '/insights/insBatchRuleCategory/insert',
    data
  })
}

/**
 * 编辑批量事件分类。
 */
export const updateBatchRuleCategory = (
  data: BatchRuleCategorySavePayload
): Promise<BatchRuleCategoryResponse<boolean | BatchRuleCategoryNode>> => {
  return request<boolean | BatchRuleCategoryNode>({
    method: 'PUT',
    url: '/insights/insBatchRuleCategory/update',
    data
  })
}

/**
 * 删除批量事件分类。
 * 后端已切换为 RESTful 路径参数，分类 ID 直接拼接到 URL 中。
 */
export const deleteBatchRuleCategory = (
  categoryId: string
): Promise<BatchRuleCategoryResponse<boolean>> => {
  return request<boolean>({
    method: 'DELETE',
    url: `/insights/insBatchRuleCategory/${categoryId}`
  })
}

/**
 * 查询批量事件规则分页列表。
 */
export const queryBatchRulePage = (
  data: BatchRulePageQuery
): Promise<BatchRuleResponse<BatchRulePageModel>> => {
  return request<BatchRulePageModel>({
    method: 'POST',
    url: '/insights/insBatchRule/queryRulePage',
    data
  })
}

/**
 * 查询批量事件规则详情。
 */
export const getBatchRuleDetail = (
  ruleId: string
): Promise<BatchRuleResponse<InsBatchRuleModel>> => {
  return request<InsBatchRuleModel>({
    method: 'GET',
    url: `/insights/insBatchRule/${ruleId}`
  })
}

/**
 * 复制批量事件规则。
 * 后端采用 RESTful 风格，通过规则 ID 直接触发复制动作。
 */
export const copyBatchRule = (
  ruleId: string
): Promise<BatchRuleResponse<boolean | string | InsBatchRuleModel>> => {
  return request<boolean | string | InsBatchRuleModel>({
    method: 'PUT',
    url: `/insights/insBatchRule/${ruleId}`
  })
}

/**
 * 新增批量事件规则。
 */
export const insertBatchRule = (
  data: BatchRuleSavePayload
): Promise<BatchRuleResponse<boolean | string | InsBatchRuleModel>> => {
  return request<boolean | string | InsBatchRuleModel>({
    method: 'PUT',
    url: '/insights/insBatchRule/insert',
    data
  })
}

/**
 * 编辑批量事件规则。
 */
export const updateBatchRule = (
  data: BatchRuleSavePayload
): Promise<BatchRuleResponse<boolean | string | InsBatchRuleModel>> => {
  return request<boolean | string | InsBatchRuleModel>({
    method: 'PUT',
    url: '/insights/insBatchRule/update',
    data
  })
}

/**
 * 查询批量规则指标配置。
 */
export const findBatchIndicatorConditionConfig = (): Promise<
  BatchRuleResponse<BatchRuleIndicatorConfigModel[]>
> => {
  return request<BatchRuleIndicatorConfigModel[]>({
    method: 'GET',
    url: '/insights/insBatchRule/findIndicatorConditionConfig'
  })
}

/**
 * 查询批量规则维度条件配置。
 */
export const findBatchConditionConfig = (): Promise<
  BatchRuleResponse<BatchRuleConditionConfigModel[]>
> => {
  return request<BatchRuleConditionConfigModel[]>({
    method: 'GET',
    url: '/insights/insBatchRule/findConditionConfig'
  })
}

/**
 * 查询省份维度列表。
 */
export const getBatchProvinceList = (): Promise<BatchRuleResponse<BatchProvinceListItem[]>> => {
  return request<BatchProvinceListItem[]>({
    method: 'GET',
    url: '/insights/insProvinceArea/provinceList'
  })
}
