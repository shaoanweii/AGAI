import request from './index'

/**
 * 根据Id查询词汇信息
 * @param params
 */
export const queryWordsInfo = (params: any) => {
  return request<any>({
    url: `/insights/words/queryWordsInfo`,
    method: 'GET',
    params
  })
}

/**
 * 根据Id查询词汇信息
 * @param params
 */
export const queryOpinionsInfo = (params: any) => {
  return request<any>({
    url: `/insights/opinions/queryOpinionsInfo`,
    method: 'GET',
    params
  })
}

/**
 * 查询标签分类 clientId
 * @param params
 */
export const getTagType = (params: any) => {
  return request<any>({
    url: `/insights/words/getTagType`,
    method: 'GET',
    params
  })
}

/**
 * 查询标签分类树 clientId
 * @param params
 */
export const findTagLibCategoryTree = (params: any) => {
  return request<any>({
    url: `/insights/words/findTagLibCategoryTree`,
    method: 'GET',
    params
  })
}

/**
 * 分配高频词汇信息
 * @param data
 */
export const allocationWords = (data: any) => {
  return request<any>({
    url: `/insights/words/allocationWords`,
    method: 'POST',
    data
  })
}
/**
 * 分配高频观点信息
 * @param data
 */
export const allocationOpinions = (data: any) => {
  return request<any>({
    url: `/insights/opinions/allocationOpinions`,
    method: 'POST',
    data
  })
}

/**
 * 新增/更新风险关键词
 * @param data
 */
export const addRisKeywords = (data: any) => {
  return request<any>({
    url: `/insights/keywords/addRisKeywords`,
    method: 'POST',
    data
  })
}
