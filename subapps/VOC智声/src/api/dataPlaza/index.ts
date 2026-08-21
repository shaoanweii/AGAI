import http from '@/api/http'
import type {
  DataPlazaCategoryItem,
  DataPlazaCategorySaveParams,
  DataPlazaConditionGroup,
  DataPlazaCategorySortParams,
  DataPlazaBatchReportIdsParams,
  DataPlazaBatchReportMoveParams,
  DataPlazaReportDeleteParams,
  DataPlazaReportListParams,
  DataPlazaReportPinParams,
  DataPlazaReportPageResult,
  DataPlazaReportPublishStatusParams,
  DataPlazaReportSaveParams,
  DataPlazaUploadResult
} from './types'

/**
 * 获取数据广场分类树。
 * @returns 分类树结果，按接口原字段返回
 */
export const getDataPlazaCategoryTree = (): Promise<BaseResponse<DataPlazaCategoryItem[]>> => {
  return http.post('/report/data-plaza/category/tree', {})
}

/**
 * 获取数据广场报告分页列表。
 * @param data 查询参数
 * @returns 报告分页结果，按接口原字段返回
 */
export const getDataPlazaReportList = (
  data: DataPlazaReportListParams
): Promise<BaseResponse<DataPlazaReportPageResult>> => {
  return http.post('/report/data-plaza/report/list', data)
}

/**
 * 获取数据广场筛选条件。
 * @returns 筛选条件配置
 */
export const getDataPlazaConditions = (): Promise<BaseResponse<DataPlazaConditionGroup[]>> => {
  return http.get('/report/data-plaza/conditions')
}

/**
 * 上传数据广场分类图片。
 * @param formData 上传表单
 * @returns 上传结果
 */
export const uploadDataPlazaImage = (
  formData: FormData
): Promise<BaseResponse<DataPlazaUploadResult>> => {
  return http.post('/report/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 新增数据广场分类。
 * @param data 分类参数
 * @returns 新增结果
 */
export const insertDataPlazaCategory = (
  data: DataPlazaCategorySaveParams
): Promise<BaseResponse<number>> => {
  return http.post('/report/data-plaza/category/insert', data)
}

/**
 * 更新数据广场分类。
 * @param data 分类参数
 * @returns 更新结果
 */
export const updateDataPlazaCategory = (
  data: Required<Pick<DataPlazaCategorySaveParams, 'id'>> & DataPlazaCategorySaveParams
): Promise<BaseResponse<number>> => {
  return http.post('/report/data-plaza/category/update', data)
}

/**
 * 删除数据广场分类。
 * @param data 分类主键
 * @returns 删除结果
 */
export const deleteDataPlazaCategory = (data: { id: string }): Promise<BaseResponse<number>> => {
  return http.post('/report/data-plaza/category/delete', data)
}

/**
 * 更新数据广场分类排序。
 * @param data 批量排序参数
 * @returns 排序结果
 */
export const updateDataPlazaCategorySort = (
  data: DataPlazaCategorySortParams
): Promise<BaseResponse<number>> => {
  return http.post('/report/data-plaza/category/updateSort', data)
}

/**
 * 新增数据广场报告。
 * @param data 报告参数
 * @returns 新增结果
 */
export const insertDataPlazaReport = (
  data: DataPlazaReportSaveParams
): Promise<BaseResponse<number>> => {
  return http.post('/report/data-plaza/report/insert', data)
}

/**
 * 更新数据广场报告。
 * @param data 报告参数
 * @returns 更新结果
 */
export const updateDataPlazaReport = (
  data: Required<Pick<DataPlazaReportSaveParams, 'id'>> & DataPlazaReportSaveParams
): Promise<BaseResponse<number>> => {
  return http.post('/report/data-plaza/report/update', data)
}

/**
 * 删除数据广场报告。
 * @param data 报告主键
 * @returns 删除结果
 */
export const deleteDataPlazaReport = (
  data: DataPlazaReportDeleteParams
): Promise<BaseResponse<number>> => {
  return http.post('/report/data-plaza/report/delete', data)
}

/**
 * 更新数据广场报告置顶状态。
 * @param data 报告主键与置顶状态
 * @returns 更新结果
 */
export const updateDataPlazaReportPin = (
  data: DataPlazaReportPinParams
): Promise<BaseResponse<number>> => {
  return http.post('/report/data-plaza/report/updatePin', data)
}

/**
 * 更新数据广场报告发布状态。
 * @param data 报告主键与发布状态
 * @returns 更新结果
 */
export const updateDataPlazaReportPublishStatus = (
  data: DataPlazaReportPublishStatusParams
): Promise<BaseResponse<number>> => {
  return http.post('/report/data-plaza/report/updatePublishStatus', data)
}

/**
 * 复制数据广场报告。
 * @param data 报告主键
 * @returns 复制结果
 */
export const copyDataPlazaReport = (
  data: DataPlazaReportDeleteParams
): Promise<BaseResponse<number>> => {
  return http.post('/report/data-plaza/report/copy', data)
}

/**
 * 批量发布数据广场报告。
 * @param data 报告主键列表
 * @returns 发布结果
 */
export const batchPublishDataPlazaReport = (
  data: DataPlazaBatchReportIdsParams
): Promise<BaseResponse<number>> => {
  return http.post('/report/data-plaza/report/batchPublish', data)
}

/**
 * 批量删除数据广场报告。
 * @param data 报告主键列表
 * @returns 删除结果
 */
export const batchDeleteDataPlazaReport = (
  data: DataPlazaBatchReportIdsParams
): Promise<BaseResponse<number>> => {
  return http.post('/report/data-plaza/report/batchDelete', data)
}

/**
 * 批量下架数据广场报告。
 * @param data 报告主键列表
 * @returns 下架结果
 */
export const batchUnpublishDataPlazaReport = (
  data: DataPlazaBatchReportIdsParams
): Promise<BaseResponse<number>> => {
  return http.post('/report/data-plaza/batchUnpublish', data)
}

/**
 * 批量移动数据广场报告。
 * @param data 报告主键列表与目标二级分类
 * @returns 移动结果
 */
export const batchMoveDataPlazaReport = (
  data: DataPlazaBatchReportMoveParams
): Promise<BaseResponse<number>> => {
  return http.post('/report/data-plaza/report/batchMove', data)
}
