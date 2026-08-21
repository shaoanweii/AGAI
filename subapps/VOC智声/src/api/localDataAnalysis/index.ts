import http from '../http/index'
import type {
  LocalDataAnalysisCheckUploadParams,
  LocalDataAnalysisCheckUploadResult,
  LocalDataAnalysisDataSourcePage,
  LocalDataAnalysisDataSourceQueryParams,
  LocalDataAnalysisQueryParams,
  LocalDataAnalysisSaveUploadDataSourceParams,
  LocalDataAnalysisUploadResult,
  LocalDataAnalysisTaskPage
} from './types'

/**
 * 查询本地数据分析数据源列表。
 * @param data 查询条件与分页参数
 * @returns 数据源分页数据
 */
export const findLocalDataAnalysisDataSource = (
  data?: LocalDataAnalysisDataSourceQueryParams
): Promise<BaseResponse<LocalDataAnalysisDataSourcePage>> => {
  return http({
    url: '/report/dataSource/findDataSource',
    method: 'post',
    data
  })
}

/**
 * 获取本地数据分析可见创建人员列表。
 * @param isAllVisible 是否具备查看全部数据权限
 * @returns 创建人员列表
 */
export const findLocalDataAnalysisVisibleUserList = (
  isAllVisible: boolean
): Promise<BaseResponse<any>> => {
  return http({
    url: '/report/dataSource/findVisibleUserList',
    method: 'get',
    params: { isAllVisible }
  })
}

/**
 * 下载本地数据分析模板。
 * @returns 模板文件 Blob
 */
export const downloadLocalDataAnalysisTemplate = (): Promise<BaseResponse<Blob>> => {
  return http({
    url: '/report/dataSource/downloadDataSource',
    method: 'get',
    responseType: 'blob'
  })
}

/**
 * 上传本地数据分析文件。
 * @param formData 上传表单
 * @returns 上传结果
 */
export const uploadLocalDataAnalysisDataSource = (
  formData: FormData
): Promise<BaseResponse<LocalDataAnalysisUploadResult>> => {
  return http.post('/report/dataSource/uploadDataSource', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 校验已上传的本地数据分析文件。
 * @param data 校验参数
 * @returns 校验结果
 */
export const checkUploadLocalDataAnalysisDataSource = (
  data: LocalDataAnalysisCheckUploadParams
): Promise<BaseResponse<LocalDataAnalysisCheckUploadResult>> => {
  return http({
    url: '/report/dataSource/checkUploadDataSource',
    method: 'post',
    data
  })
}

/**
 * 保存本地数据源。
 * @param data 保存参数
 * @returns 保存结果
 */
export const saveLocalDataAnalysisUploadDataSource = (
  data: LocalDataAnalysisSaveUploadDataSourceParams
): Promise<BaseResponse<any>> => {
  return http({
    url: '/report/dataSource/saveUploadDataSource',
    method: 'post',
    data
  })
}

/**
 * 查询本地数据分析任务列表。
 * @param data 查询条件与分页参数
 * @returns 任务分页数据
 */
export const findLocalDataAnalysisTaskList = (
  data?: LocalDataAnalysisQueryParams
): Promise<BaseResponse<LocalDataAnalysisTaskPage>> => {
  return http({
    url: '/report/localDataAnalysis/findTaskList',
    method: 'post',
    data
  })
}

/**
 * 开始处理本地数据源。
 * @param data 任务 ID
 * @returns 操作结果
 */
export const startLocalDataAnalysisTask = (data: { batchId: string }): Promise<BaseResponse<any>> => {
  return http({
    url: '/report/dataSource/startProcessing',
    method: 'post',
    data
  })
}

/**
 * 导出本地数据源原始数据结果。
 * @param data 任务 ID
 * @returns 导出文件 Blob
 */
export const exportLocalDataAnalysisResult = (data: {
  batchId: string
}): Promise<BaseResponse<Blob>> => {
  return http({
    url: '/report/dataSource/exportRawDataResult',
    method: 'post',
    data,
    responseType: 'blob'
  })
}

/**
 * 删除本地数据分析任务。
 * @param data 任务 ID
 * @returns 操作结果
 */
export const deleteLocalDataAnalysisTask = (data: { id: string }): Promise<BaseResponse<any>> => {
  return http({
    url: '/report/dataSource/deleteDataSource',
    method: 'post',
    data
  })
}
