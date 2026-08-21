import request from './index'

interface regulationConditionList {
  id: string
  regulationCondition: string
}

interface regulationResult {
  id: string
  name: string
  matchingRule: string
  regulationCondition: regulationConditionList[]
  status: string
  createTime: string
  updateTime: string
}

export const findRegulationInfo = (data: object) => {
  return request<regulationResult>({
    method: 'POST',
    url: '/insights/regulation/findRegulationInfo',
    data
  })
}
export const saveRegulationInfo = (data: object) => {
  return request({
    method: 'POST',
    url: '/insights/regulation/saveRegulationInfo',
    data
  })
}
export const updateRegulationInfo = (data: object) => {
  return request({
    method: 'POST',
    url: '/insights/regulation/updateRegulationInfo',
    data
  })
}
export const countAll = (data: object) => {
  return request({
    method: 'POST',
    url: '/insights/insDataExpect/countAll',
    data
  })
}

export const addBrandInfo = (data: object) => {
  return request({
    method: 'POST',
    url: '/insights/brandInfo/addBrandInfo',
    data
  })
}
export const updateBrandInfo = (data: object) => {
  return request({
    method: 'POST',
    url: '/insights/brandInfo/updateBrandInfo',
    data
  })
}
export const deleteBrandInfo = (data: object) => {
  return request({
    method: 'GET',
    url: '/insights/brandInfo/deleteBrandInfo',
    data
  })
}
export const addCarSeriesInfo = (data: object) => {
  return request({
    method: 'POST',
    url: '/insights/carSeriesInfo/addCarSeriesInfo',
    data
  })
}
export const updateCarSeriesInfo = (data: object) => {
  return request({
    method: 'POST',
    url: '/insights/carSeriesInfo/updateCarSeriesInfo',
    data
  })
}

export const insertGroup = (data: object) => {
  return request({
    method: 'POST',
    url: '/insights/insDataResource/insert',
    data
  })
}

export const updateGroup = (data: object) => {
  return request({
    method: 'PUT',
    url: '/insights/insDataResource/update',
    data
  })
}
export const findResourceCount = (data: any) => {
  return request({
    method: 'POST',
    url: '/insights/insDataResource/findResourceCount',
    data
  })
}
export const groupDelete = (data: object) => {
  return request({
    method: 'DELETE',
    url: '/insights/insDataResource/delete',
    data
  })
}

export const insertDesc = (data: object) => {
  return request({
    method: 'POST',
    url: '/insights/insDataResourceDesc/insert',
    data
  })
}

export const updateDesc = (data: object) => {
  return request({
    method: 'POST',
    url: '/insights/insDataResourceDesc/update',
    data
  })
}
export const updateDescStatus = (data: object) => {
  return request({
    method: 'PUT',
    url: '/insights/insDataResourceDesc/updateStatus',
    data
  })
}
export const deleteDesc = (data: any) => {
  return request({
    method: 'POST',
    url: '/insights/insDataResourceDesc/delete',
    data
  })
}

export const listResourceDesc = (data: any) => {
  return request<any>({
    method: 'POST',
    url: '/insights/insDataResourceDesc/list',
    data
  })
}

export const changeResourceStatus = (data: { ids: string[]; status: string }) => {
  return request({
    method: 'POST',
    url: '/insights/insDataResourceDesc/changeResourceStatus',
    data
  })
}

/**
 * 根据客户id查询资源组
 * @param data
 */
export const findResourceGroupByAppClient = (data: object) => {
  return request({
    method: 'POST',
    url: '/insights/insDataResource/findResourceGroupByAppClient',
    data
  })
}

/**
 * 新增数据源
 * @param data
 */
export const saveDataSource = (data: any) => {
  return request({
    method: 'POST',
    url: '/insights/insDataSource/saveDataSource',
    data
  })
}
/**
 * 更新数据源
 * @param data
 */
export const updateDataSource = (data: any) => {
  return request({
    method: 'POST',
    url: '/insights/insDataSource/updateDataSource',
    data
  })
}

/**
 * 删除数据源
 * @param data
 */
export const deleteDataSource = (data: any) => {
  return request({
    method: 'POST',
    url: '/insights/insDataSource/deleteDataSource',
    data
  })
}
/**
 * 删除数据列表
 * @param data
 */
export const deleteDataSourceDetail = (data: any) => {
  return request({
    method: 'POST',
    url: '/insights/insDataSource/deleteDataSourceDetail',
    data
  })
}

/**
 * 上传文件
 * @param data
 */
export const uploadDataSource = (data: any) => {
  return request<any>({
    method: 'POST',
    url: '/insights/insDataSource/uploadDataSource',
    data
  })
}

/**
 * 数据校验
 * @param data
 */
export const checkUploadDataSource = (data: any) => {
  return request<any>({
    method: 'POST',
    url: '/insights/insDataSource/checkUploadDataSource',
    data
  })
}

/**
 * 下载模版
 * @param data
 */
export const downloadDataSource = (data: any) => {
  return request({
    method: 'GET',
    url: '/insights/insDataSource/downloadDataSource',
    params: data,
    responseType: 'blob'
  })
}

/**
 * 本地上传数据保存
 * @param data
 */
export const saveUploadDataSource = (data: any) => {
  return request({
    method: 'POST',
    url: '/insights/insDataSource/saveUploadDataSource',
    data
  })
}

/**
 * 根据数据源类型获取数据源列表
 * @param data
 */
export const getDataSourceList = (data: any) => {
  return request<any>({
    method: 'POST',
    url: '/insights/insDataSource/getDataSourceList',
    data
  })
}

/**
 * 导出原始数据
 * @param data
 */
export const exportRawData = (data: any) => {
  return request({
    method: 'POST',
    url: '/insights/insDataSource/exportRawData',
    data
    // responseType: 'blob'
  })
}

export const exportSIRawData = (data: any) => {
  return request({
    method: 'POST',
    url: '/insights/insDataSource/exportSIRawData',
    data
    // responseType: 'blob'
  })
}

/**
 * 导出结果数据
 * @param data
 */
export const exportRawDataResult = (data: any) => {
  return request({
    method: 'POST',
    url: '/insights/insDataSource/exportRawDataResult',
    data
    // responseType: 'blob'
  })
}
export const exportSIRawDataResult = (data: any) => {
  return request({
    method: 'POST',
    url: '/insights/insDataSource/exportSIRawDataResult',
    data
    // responseType: 'blob'
  })
}

/**
 * 查看原始数据与结果数据查询条件的下拉选项
 * @param data
 */
export const getDataSourceSearchCriteria = (data: Api.InsDataSource.params) => {
  return request({
    method: 'POST',
    url: '/insights/insDataSource/getDataSourceSearchCriteria',
    data
  })
}

export const getSIDataSourceSearchCriteria = (data: Api.InsDataSource.params) => {
  return request({
    method: 'POST',
    url: '/insights/insDataSource/getSIDataSourceSearchCriteria',
    data
  })
}

/**
 * 处理
 * @param data
 */
export const startProcessing = (data: Api.InsDataSource.params) => {
  return request({
    method: 'POST',
    url: '/insights/insDataSource/startProcessing',
    data
  })
}

/**
 * 导出
 * @param data
 */
export const exportRawDataByStatus = (data: Api.InsDataSource.ExportRawParams) => {
  return request({
    method: 'POST',
    url: '/insights/insDataSource/exportRawDataByStatus',
    data,
    responseType: 'blob'
  })
}

/**
 * 导出
 * @param data
 */
export const exportSIRawDataByStatus = (data: Api.InsDataSource.ExportRawParams) => {
  return request({
    method: 'POST',
    url: '/insights/insDataSource/exportSIRawDataByStatus',
    data,
    responseType: 'blob'
  })
}

/**
 * 公域数据库获取标签类型
 * @param data
 */
export const findTagLibTreeByPublicDB = (tagLibType: string) => {
  return request({
    method: 'GET',
    url: `/insights/commonDataBase/findTagLibTree?tagLibType=${tagLibType}`
  })
}
/**
 * 知识库获取标签类型
 * @param data
 */
export const findTagLibTreeByinsKnowledgeBase = (tagLibType: string) => {
  return request({
    method: 'GET',
    url: `/insights/insKnowledgeBase/findTagLibTree?tagLibType=${tagLibType}`
  })
}
/**
 * 知识库-添加
 * @param data
 */
export const insKnowledgeBaseAdd = (data: any) => {
  return request({
    method: 'post',
    url: `/insights/insKnowledgeBase/add`,
    data
  })
}
/**
 * 知识库-查询
 * @param data
 */
export const insKnowledgeBaseListSelect = (data = {}) => {
  return request({
    method: 'post',
    url: `/insights/insKnowledgeBase/listSelect`,
    data
  })
}
/**
 * 知识库-编辑
 * @param data
 */
export const insKnowledgeBaseEdit = (data: any) => {
  return request({
    method: 'post',
    url: `/insights/insKnowledgeBase/edit`,
    data
  })
}
/**
 * 知识库-删除
 * @param data
 */
export const insKnowledgeBaseDel = (data: any) => {
  return request({
    method: 'post',
    url: `/insights/insKnowledgeBase/delete`,
    data
  })
}
/**
 * 知识库-文件上传
 * @param data
 */
export const insKnowledgeBaseUploadData = (data: any) => {
  return request({
    method: 'post',
    url: `/insights/insKnowledgeBase/uploadData`,
    data
  })
}
/**
 * 知识库明细-删除
 * @param data
 */
export const insKnowledgeBaseDetailsDel = (data: any) => {
  return request({
    method: 'post',
    url: `/insights/insKnowledgeBaseDetails/delete`,
    data
  })
}
/**
 * 知识库明细-批量删除
 * @param data
 */
export const insKnowledgeBaseDetailsDels = (data: any) => {
  return request({
    method: 'post',
    url: `/insights/insKnowledgeBaseDetails/batchDelete`,
    data
  })
}
/**
 * 知识库明细-移动
 * @param data
 */
export const insKnowledgeBaseDetailsBatchMove = (data: any) => {
  return request({
    method: 'post',
    url: `/insights/insKnowledgeBaseDetails/batchMove`,
    data
  })
}
/**
 * 知识库明细-同步
 * @param data
 */
export const insKnowledgeBaseDetailsBatchSynchronous = (data: any) => {
  return request({
    method: 'post',
    url: `/insights/insKnowledgeBaseDetails/batchSynchronous`,
    data
  })
}
/**
 * 知识库明细-批量编辑
 * @param data
 */
export const insKnowledgeBaseDetailsBatchEdit = (data: any) => {
  return request({
    method: 'post',
    url: `/insights/insKnowledgeBaseDetails/batchEdit`,
    data
  })
}
/**
 * 知识库明细-编辑
 * @param data
 */
export const insKnowledgeBaseDetailsEdit = (data: any) => {
  return request({
    method: 'post',
    url: `/insights/insKnowledgeBaseDetails/edit`,
    data
  })
}
/**
 * 知识库明细-数据校验
 * @param data
 */
export const insKnowledgeBaseDetailsCheckUploadData = (data: any) => {
  return request({
    method: 'post',
    url: `/insights/insKnowledgeBase/checkUploadData`,
    data
  })
}
/**
 * 知识库明细-导出数据
 * @param data
 */
export const knowledgeBaseDetailsExport = (data: any) => {
  return request({
    method: 'post',
    url: `/insights/insKnowledgeBaseDetails/knowledgeBaseDetailsExport`,
    data,
    responseType: 'blob'
  })
}

/**
 * 知识库明细-下载模板
 * @param data
 */
export const downloadKnowledgeBase = (data: any) => {
  return request({
    method: 'GET',
    url: '/insights/insKnowledgeBaseDetails/downloadKnowledgeBase',
    params: data,
    responseType: 'blob'
  })
}
/**
 * 知识库明细-保存数据
 * @param data
 */
export const insKnowledgeBaseSaveUploadData = (data: any) => {
  return request({
    method: 'post',
    url: '/insights/insKnowledgeBase/saveUploadData',
    data
  })
}

/**
 * @description: 提交纠错
 * @param {any} data
 * @return {*}
 */
export const dataQueryInsertLabelCorrection = (data: any) => {
  return request({
    method: 'post',
    url: '/insights/addLabel/insertLabelCorrection',
    data
  })
}
