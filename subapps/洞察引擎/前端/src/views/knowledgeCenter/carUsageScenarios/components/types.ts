/**
 * 左侧分类树标准化节点。
 */
export interface CarUsageScenarioCategoryItem {
  id: string
  patentId: string
  parentId: string
  categoryName: string
  categoryDescription: string
  synonyms: string
  typeName: string
  level: number
  depth: number
  leafCount: number
  status: Api.Common.EnableStatus
  children: CarUsageScenarioCategoryItem[]
}

/**
 * 左侧列表当前选中的分类。
 */
export type CarUsageScenarioCategorySelection = CarUsageScenarioCategoryItem

/**
 * 右侧列表筛选表单。
 */
export interface CarUsageScenarioSceneQueryForm {
  id?: string
  sceneName: string
  sceneDescription?: string
  synonyms?: string
  operator: string
  status: string
}

/**
 * 场景弹框所属分类下拉项。
 */
export interface CarUsageScenarioCategoryOption {
  id: string
  label: string
  status: Api.Common.EnableStatus
}

/**
 * 分类弹框表单结构。
 */
export interface CarUsageScenarioCategoryForm {
  categoryName: string
  categoryDescription: string
  synonyms: string
  status: Api.Common.EnableStatus
}

/**
 * 分类弹框提交成功后向外透出的结果。
 */
export interface CarUsageScenarioCategorySubmitResult {
  categoryId: string
  categoryName: string
  mode: 'create' | 'edit'
}

/**
 * 场景弹框表单结构。
 */
export interface CarUsageScenarioSceneForm {
  sceneName: string
  sceneDescription: string
  synonyms: string
  status: Api.Common.EnableStatus
}

/**
 * 场景弹框提交成功后向外透出的结果。
 */
export interface CarUsageScenarioSceneSubmitResult {
  sceneId: string
  mode: 'create' | 'edit'
}

/**
 * 场景列表批量操作类型。
 */
export type CarUsageScenarioBatchActionType = 'enable' | 'disable'
