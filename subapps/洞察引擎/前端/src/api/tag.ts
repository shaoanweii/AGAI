import request from './index'
import type { Conditions } from '@/types'

export interface List {
  records: Item[]
  total: number
  size: number
  current: number
}

export interface Item {
  projectId: string
  projectTitle: string
  description: string
  corpusCount: string
  corpusPlan: string
  creatTime: string
  addLinkUrl: string
  updateLinkUrl: string
}

export const postProjectList = (data: any) => {
  return request<List>({
    url: '/api/model/training/getProjects',
    method: 'POST',
    data
  })
}
/**
 * 获取数据字字典
 */
export const getConditions = () => {
  return request<Conditions[]>({
    url: '/insights/insTagInfo/conditions',
    method: 'get'
  })
}
/**
 * 标签应用 获取数据字字典
 */
export const getClientConditions = () => {
  return request<Conditions[]>({
    url: '/insights/insClientInfo/conditions',
    method: 'get'
  })
}

export interface QueryBySelectReq {
  pageSize: number
  pageNum: number
  order?: string
  type?: string
  firstDimensionCodes: string[]
  name?: string
  enable: number
  seriousness?: string
  clientId?: string
}

/**
 * 标签应用 查询所属分类
 */
export const queryTagClientTree = (data: any) => {
  return request<Conditions[]>({
    url: '/insights/insClientInfo/queryTagClientTree',
    method: 'post',
    data
  })
}

/**
 * 标签库分页查询
 */
export const selectInsTagInfo = (data: any) => {
  return request<any>({
    url: '/insights/insTagInfo/selectInsTagInfo',
    method: 'post',
    data
  })
}

/**
 * 查询所属分类
 */
export const findTagLibCategoryTree = (tagLibType?: string) => {
  return request<Conditions[]>({
    url: `/insights/insTagLib/findTagLibCategoryTree?tagLibType=${tagLibType || ''}`,
    method: 'get'
  })
}

/**
 * 根据标签类型查询关联配置项
 * @param tagType
 */
export const findTagLibRelatedItems = (data: any) => {
  return request<Conditions[]>({
    url: `/insights/insTagLib/findTagLibRelatedItems`,
    method: 'POST',
    data
  })
}

/**
 * 新增标签
 * @param data
 */
export const saveTagLib = (data: any) => {
  return request<Conditions[]>({
    url: `/insights/insTagLib/saveTagLib`,
    method: 'POST',
    data
  })
}
/**
 * 更新标签
 * @param data
 */
export const updateTagLib = (data: any) => {
  return request<Conditions[]>({
    url: `/insights/insTagLib/updateTagLib`,
    method: 'POST',
    data
  })
}

/**
 * 查询客户标签分类树
 */
export const findTagLibClientCategoryTree = (
  clientId: string,
  tagLibType?: string,
  tagName?: string
) => {
  return request<Conditions[]>({
    url: `/insights/insTagLibClient/findTagLibClientCategoryTree?clientId=${
      clientId || ''
    }&tagLibType=${tagLibType || ''}&tagName=${tagName || ''}`,
    method: 'get'
  })
}

/**
 * 查询客户标签分类树
 */
export const findClientCategoryTree = (data: any) => {
  return request<Conditions[]>({
    url: `/insights/insTagLibClient/findClientCategoryTree`,
    method: 'post',
    data
  })
}

/**
 * 新增客户标签信息
 * @param data
 */
export const saveTagLibClient = (data: any) => {
  return request<Conditions[]>({
    url: `/insights/insTagLibClient/saveTagLibClient`,
    method: 'POST',
    data
  })
}
/**
 * 更新客户标签信息
 * @param data
 */
export const updateTagLibClient = (data: any) => {
  return request<Conditions[]>({
    url: `/insights/insTagLibClient/updateTagLibClient`,
    method: 'POST',
    data
  })
}

/**
 * 删除客户标签信息
 * @param data
 */
export const deleteTagLibClient = (data: any) => {
  return request<Conditions[]>({
    url: `/insights/insTagLibClient/deleteTagLibClient`,
    method: 'POST',
    data
  })
}

/**
 * 获取左侧分类列表
 * @param data 支持按分类名称关键字筛选，未传则返回完整分类树
 */
export const findCategoryListClient = (data: { tagName?: string }) => {
  return request<any>({
    url: `/insights/insTagLibClient/findCategoryList`,
    method: 'POST',
    data
  })
}

/**
 * 获取体验代码列表。
 * 说明：右侧列表当前将左侧选中节点 id 映射到 tagParentId 查询，分页参数沿用接口约定。
 */
export const findExperienceCodeListClient = (data: {
  tagParentId?: string
  pageSize: number
  pageNum: number
  tagStatus?: string
  operateUser?: string
  tagName?: string
  tagType?: string
  order?: string
}) => {
  return request<any>({
    url: `/insights/insTagLibClient/findExperienceCodeList`,
    method: 'POST',
    data
  })
}

/**
 * 获取观点/体验代码操作人下拉列表。
 */
export const findTopicOperatorListClient = data => {
  return request<any>({
    url: `/insights/insTagLibClient/findTopicOperatorList`,
    method: 'POST',
    data
  })
}

/**
 * 查询标签分类树
 */
export const findTagLibCategoryTreeClient = (tagLibType?: string) => {
  return request<Conditions[]>({
    url: `/insights/insTagLibClient/findTagLibCategoryTree?tagLibType=${tagLibType || ''}`,
    method: 'get'
  })
}

/**
 * 查询标签分类树
 */
export const findTagTree = data => {
  return request<Conditions[]>({
    url: `/insights/insTagLibClient/findTagTree`,
    method: 'POST',
    data
  })
}

/**
 * 观点编辑：根据观点编码查询详情（用于编辑回显）
 */
export const findTopicByCodeClient = (data: { tagCode: string }) => {
  return request<any>({
    url: `/insights/insTagLibClient/findTopicByCode`,
    method: 'POST',
    data
  })
}

/**
 * 观点新增/编辑保存
 * 说明：接口文档常写为 /insTagLibClient/saveTopic；本项目按既有约定补全 /insights 前缀
 */
export const saveTopicClient = (data: any) => {
  return request<any>({
    url: `/insights/insTagLibClient/saveTopic`,
    method: 'POST',
    data
  })
}

/**
 * 观点批量编辑
 * 说明：接口文档常写为 /insTagLibClient/batchUpdateTopic；本项目按既有约定补全 /insights 前缀
 */
export const batchUpdateTopicClient = (data: any) => {
  return request<any>({
    url: `/insights/insTagLibClient/batchUpdateTopic`,
    method: 'POST',
    data
  })
}

/**
 * 观点批量启用/禁用
 */
export const batchChangeTopicStatusClient = (data: { topicCodes: string[]; tagStatus: string }) => {
  return request<any>({
    url: `/insights/insTagLibClient/batchChangeTopicStatus`,
    method: 'POST',
    data
  })
}

export interface TopicMergeOption {
  id?: string
  tagName?: string
  tagNameEn?: string
  tagCode?: string
}

/**
 * 查询可用于批量合并的观点列表
 */
export const findTopicListClient = () => {
  return request<TopicMergeOption[]>({
    url: `/insights/insTagLibClient/findTopicList`,
    method: 'POST',
    data: {}
  })
}

/**
 * 批量合并观点
 */
export const batchMergeTopicClient = (data: { topicCodes: string[]; tagCode: string }) => {
  return request<any>({
    url: `/insights/insTagLibClient/batchMergeTopic`,
    method: 'POST',
    data
  })
}

/**
 * 根据标签类型查询标签树
 */
export const findTagLibTree = (tagLibType?: string) => {
  return request<Conditions[]>({
    url: `/insights/insTagLibClient/findTagLibTree?tagLibType=${tagLibType || ''}`,
    method: 'get'
  })
}

/**
 * 系统调用
 * @param data
 */
export const copyTagLibClient = (data: any) => {
  return request<Conditions[]>({
    url: `/insights/insTagLibClient/copyTagLibClient`,
    method: 'POST',
    data
  })
}

/**
 * 获取已被当前客户调用过的标签
 * @param data
 */
export const findCalledTagLibClient = (data: { appClient: string; tagType: string }) => {
  return request<any>({
    url: `/insights/insTagLibClient/findCalledTagLibClient`,
    method: 'POST',
    data
  })
}

/**
 * 批量更新客户标签状态信息
 * @param data
 */
export const batchUpdateStatusTagLibClient = (data: {
  appClient: string
  ids: string[]
  tagStatus: string
}) => {
  return request<any>({
    url: `/insights/insTagLibClient/batchUpdateStatusTagLibClient`,
    method: 'POST',
    data
  })
}

/**
 * 批量移动客户标签信息
 * @param data
 */
export const batchMoveTagLibClient = (data: {
  appClient: string
  ids: string[]
  tagParentId: string
  level?: number
}) => {
  return request<any>({
    url: `/insights/insTagLibClient/batchMoveTagLibClient`,
    method: 'POST',
    data
  })
}

/**
 * 批量删除客户标签信息
 * @param data
 */
export const batchDeleteTagLibClient = (data: { appClient: string; ids: string[] }) => {
  return request<any>({
    url: `/insights/insTagLibClient/batchDeleteTagLibClient`,
    method: 'POST',
    data
  })
}

/**
 * 获取全部客户末级标签信息
 * @param data
 */
export const findAllFinalTagLib = (data: { appClient?: string; tagParentIds?: string[] }) => {
  return request<any>({
    url: `/insights/insTagLibClient/findAllFinalTagLib`,
    method: 'POST',
    data
  })
}

/**
 * 获取末级标签列表
 * @param data
 */
export const findTagLibClientList = (data: any) => {
  return request<any>({
    url: `/insights/insTagLibClient/findTagLibClientList`,
    method: 'POST',
    data
  })
}

/**
 * 批量导出标签
 * @param data
 */
export const batchDownloadTagLibClient = (data: any) => {
  return request<any>({
    url: `/insights/insTagLibClient/batchDownloadTagLibClient`,
    method: 'POST',
    data,
    responseType: 'blob'
  })
}

/**
 * 获取完整标签树
 * @param data
 */
export const findTagLibClientTree = (data: any) => {
  return request<any>({
    url: `/insights/insTagLibClient/findTagLibClientTree`,
    method: 'POST',
    data
  })
}

/**
 * 获取当前分类下的末级标签
 * @param data
 */
export const findAllFinalTagLibClientVoList = (data: any) => {
  return request<any>({
    url: `/insights/insTagLibClient/findAllFinalTagLibClientVoList`,
    method: 'POST',
    data
  })
}
