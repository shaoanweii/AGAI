import request from '@/api/http'

/**
 * 分页查询专项分析类型列表
 * */
export const getSpecialTypeList = (params?: any): Promise<BaseResponse> => {
  return request.post('/report/special-analysis-type/list', params)
}

/**
 * 更新专项分析类型排序
 *
 * 入参说明：
 * - type：1 一级（左侧分类），2 二级（右侧专区）
 * - idAndSortNo：["id,sortNo", ...]，id 与序号使用英文逗号分隔（sortNo 从 1 开始）
 */
export const updateSpecialTypeSort = (params: {
  type: number
  idAndSortNo: string[]
}): Promise<BaseResponse> => {
  return request.post('/report/special-analysis-type/updateSort', params)
}

/**
 * 新增专项分析类型
 * */
export const insertSpecialType = (params: any): Promise<BaseResponse> => {
  return request.post('/report/special-analysis-type/insert', params)
}

/**
 * 更新专项分析类型
 * */
export const updateSpecialType = (params: any): Promise<BaseResponse> => {
  return request.post('/report/special-analysis-type/update', params)
}

/**
 * 删除专项分析类型
 * */
export const deleteSpecialType = (params: any): Promise<BaseResponse> => {
  return request.post('/report/special-analysis-type/delete', params)
}

/**
 * 查询专项分析类型详情
 * */
export const satDetail = (params: any): Promise<BaseResponse> => {
  return request.post('/report/special-analysis-type/detail', params)
}

/**
 * 分页查询专项分析分类类型列表
 * */
export const classificationList = (params: any): Promise<BaseResponse> => {
  return request.post('/report/special-analysis-type/classificationList', params)
}
