import request from './index'
import type { Conditions } from '@/types'

/** 启用状态值，后端当前约定：1=启用，0=禁用。 */
const ENABLE_STATUS = {
  ENABLED: '1',
  DISABLED: '0'
} as const

type EnableStatus = (typeof ENABLE_STATUS)[keyof typeof ENABLE_STATUS]

export const carUsageScenarioEnableStatus = ENABLE_STATUS

/** 查询用车场景状态等字典。 */
export const getCarSceneConditions = () => {
  return request<Conditions[]>({
    method: 'GET',
    url: '/insights/carScene/conditions'
  })
}

/** 查询用车场景分类树。 */
export const findCarSceneCategoryList = (data: Api.CarUsageScenarios.CategoryListQuery) => {
  return request<Api.CarUsageScenarios.CategoryNode[]>({
    method: 'POST',
    url: '/insights/carSceneCategory/findCarSceneCategoryList',
    data
  })
}

/** 查询用车场景筛选树，结果数据页按级联面板第一列、第二列拆分查询参数。 */
export const findCarSceneCategoryTree = (data: Api.CarUsageScenarios.CategoryListQuery = {}) => {
  return request<Api.CarUsageScenarios.CategoryNode[]>({
    method: 'POST',
    url: '/insights/carSceneCategory/findCarSceneCategoryTree',
    data
  })
}

/** 新增用车场景分类。 */
export const createCarSceneCategory = (data: Api.CarUsageScenarios.SaveCategoryPayload) => {
  return request({
    method: 'POST',
    url: '/insights/carSceneCategory/saveCarSceneCategory',
    data
  })
}

/** 编辑用车场景分类。 */
export const updateCarSceneCategory = (data: Api.CarUsageScenarios.SaveCategoryPayload) => {
  return request({
    method: 'POST',
    url: '/insights/carSceneCategory/updateCarSceneCategory',
    data
  })
}

/** 删除用车场景分类。 */
export const deleteCarSceneCategory = (data: Api.CarUsageScenarios.DeleteCategoryPayload) => {
  return request({
    method: 'POST',
    url: '/insights/carSceneCategory/deleteCarSceneCategory',
    data
  })
}

/** 分页查询用车场景列表。 */
export const findCarSceneList = (data: Api.CarUsageScenarios.SceneListQuery) => {
  return request<Api.CarUsageScenarios.ScenePageResult>({
    method: 'POST',
    url: '/insights/carScene/findCarSceneList',
    data
  })
}

/** 查询用车场景操作人下拉列表。 */
export const findCarSceneOperatorList = () => {
  return request<Api.CarUsageScenarios.SceneOperatorOption[]>({
    method: 'POST',
    url: '/insights/carScene/findCarSceneOperatorList'
  })
}

/** 新增用车场景。 */
export const createCarScene = (data: Api.CarUsageScenarios.SaveScenePayload) => {
  return request({
    method: 'POST',
    url: '/insights/carScene/saveCarScene',
    data
  })
}

/** 编辑用车场景。 */
export const updateCarScene = (data: Api.CarUsageScenarios.SaveScenePayload) => {
  return request({
    method: 'POST',
    url: '/insights/carScene/updateCarScene',
    data
  })
}

/** 批量移动用车场景。 */
export const batchMoveCarScene = (data: Api.CarUsageScenarios.BatchMoveScenePayload) => {
  return request({
    method: 'POST',
    url: '/insights/carScene/batchMoveCarScene',
    data
  })
}

/** 批量修改用车场景启用状态。 */
export const batchChangeCarSceneStatus = (
  data: Api.CarUsageScenarios.BatchChangeSceneStatusPayload
) => {
  return request({
    method: 'POST',
    url: '/insights/carScene/batchChangeStatus',
    data
  })
}

export type { EnableStatus }
