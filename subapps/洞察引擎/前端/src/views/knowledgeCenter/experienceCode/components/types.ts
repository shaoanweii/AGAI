import type { ConditionsDetailItem } from '@/types'

/**
 * 分类启用状态值。
 */
export type CategoryStatusValue = '1' | '0'

/**
 * 体验代码状态值。
 */
export type StatusValue = CategoryStatusValue

/**
 * 体验代码类型值直接跟随数据字典，避免页面继续维护写死枚举。
 */
export type ExperienceCodeType = string

/**
 * 批量操作类型。
 */
export type BatchActionType = 'enable' | 'disable' | 'move'

/**
 * 分类树接口原始节点。
 */
export interface ExperienceCategoryApiNode {
  id?: string | number | null
  tagName?: string | null
  level?: string | number | null
  tagType?: string | null
  tagCode?: string | null
  tagDescription?: string | null
  tagStatus?: string | null
  synonyms?: string | null
  leafCount?: number | string | null
  hasFinalCategory?: boolean | null
  hasFinalTopic?: boolean | null
  child?: ExperienceCategoryApiNode[] | null
}

/**
 * 扁平化后的分类实体，直接对齐真实接口字段。
 */
export interface ExperienceCategoryItem {
  id: string
  tagParentId: string
  tagName: string
  tagDescription: string
  synonyms: string
  tagStatus: CategoryStatusValue
  tagType: ExperienceCodeType
  tagCode: string
  leafCount: number
  hasFinalCategory: boolean
  hasFinalTopic: boolean
  level: 1 | 2 | 3
  order: number
  pathNames: string[]
  pathLabel: string
}

/**
 * 体验代码列表接口原始记录。
 */
export interface ExperienceCodeApiItem {
  id?: string | number | null
  tagParentId?: string | number | null
  tagName?: string | null
  tagCode?: string | null
  tagType?: string | null
  tagTypeName?: string | null
  tagStatus?: string | null
  tagStatusText?: string | null
  tagDescription?: string | null
  synonyms?: string | null
  tagLibNameHierarchical?: string | null
  createTime?: string | null
  updateTime?: string | null
  operateUser?: string | null
  hasFinalTopic?: boolean | null
}

/**
 * 操作人下拉接口原始记录。
 */
export interface ExperienceCodeOperatorApiItem {
  id?: string | number | null
  userName?: string | null
}

/**
 * 操作人筛选下拉项。
 */
export interface ExperienceCodeOperatorOption {
  id: string
  userName: string
}

/**
 * 体验代码实体直接沿用列表接口返回结构，避免页面再次加工字段。
 */
export type ExperienceCodeItem = ExperienceCodeApiItem

/**
 * 右侧表格行与接口返回保持一致。
 */
export type ExperienceCodeTableRow = ExperienceCodeItem

/**
 * 左侧当前选中的过滤条件。
 * categoryId 有值表示选中了具体分类；为空表示仍停留在类型节点。
 */
export interface ExperienceFilterTarget {
  queryId: string
  typeCode: ExperienceCodeType
  categoryId?: string
}

/**
 * 分类弹框表单。
 */
export interface CategoryDialogForm {
  tagName: string
  tagDescription: string
  tagType: ExperienceCodeType
  tagParentId: string
  synonyms: string
  tagStatus: CategoryStatusValue
}

/**
 * 分类弹框提交结果。
 */
export interface ExperienceCategorySubmitResult {
  categoryId: string
  categoryName: string
  typeCode: ExperienceCodeType
}

/**
 * 体验代码弹框表单。
 */
export interface ExperienceCodeDialogForm {
  tagName: string
  tagDescription: string
  tagType: ExperienceCodeType
  tagParentId: string
  synonyms: string
  tagStatus: StatusValue
}

/**
 * 批量移动弹框表单。
 */
export interface BatchMoveDialogForm {
  categoryId: string
}

/**
 * 下拉选项通用结构。
 */
export interface OptionItem<T = string> {
  label: string
  value: T
  disabled?: boolean
}

/**
 * 体验代码类型选项，统一由 conditions.tagLibeType 转为页面可直接消费的结构。
 */
export type ExperienceCodeTypeOption = OptionItem<ExperienceCodeType>

/**
 * 将 conditions.tagLibeType 明细转换为页面标准选项。
 */
export const buildExperienceCodeTypeOptions = (
  options: ConditionsDetailItem[] = []
): ExperienceCodeTypeOption[] => {
  return options
    .map(item => ({
      label: String(item?.value ?? '').trim(),
      value: String(item?.key ?? '').trim()
    }))
    .filter(item => item.value)
}

/**
 * 类型节点汇总信息。
 */
export interface ExperienceCategoryTypeSummary {
  nodeId: string
  typeCode: ExperienceCodeType
  label: string
  tagCode: string
  count: number
  hasFinalCategory: boolean
}

/**
 * 批量操作文本映射。
 */
export const BATCH_ACTION_LABEL_MAP: Record<BatchActionType, string> = {
  enable: '批量启用',
  disable: '批量禁用',
  move: '批量移动'
}
