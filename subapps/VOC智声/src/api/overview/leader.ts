import request from '@/api/http/index'
/**
 * 获取集团简报
 * @param data 查询参数
 */
export const getGroupBrief = (data: VocQueryParams) => {
  return request<any[]>({
    url: '/report/vocLeadership/getGroupBrief',
    method: 'POST',
    data
  })
}

/**
 * 获取品牌车系排行数据
 * @param data 查询参数
 */
export const getBrandRanking = (data: VocQueryParams) => {
  return request<any[]>({
    url: '/report/vocLeadership/getBrandRanking',
    method: 'POST',
    data
  })
}

/**
 * 获取品牌简报
 * @param data 查询参数
 */
export const getBrandInsight = (data: VocQueryParams) => {
  return request<any[]>({
    url: '/report/vocLeadership/getBrandInsight',
    method: 'POST',
    data
  })
}

/**
 * 产品场景分析
 * @param data 查询参数
 */
export const getProductAnalysis = (data: VocQueryParams) => {
  return request<any[]>({
    url: '/report/vocLeadership/getProductScenarioAnalysis',
    method: 'POST',
    data
  })
}

/**
 * 服务场景分析
 * @param data 查询参数
 */
export const getServiceAnalysis = (data: VocQueryParams) => {
  return request<any[]>({
    url: '/report/vocLeadership/getServiceScenarioAnalysis',
    method: 'POST',
    data
  })
}

/**
 * 用户意图观点TOP
 * @param data 查询参数
 */
export const getOpinionTop = (data: VocQueryParams) => {
  return request<any>({
    url: '/report/vocLeadership/getUserIntentOpinionTop',
    method: 'POST',
    data
  })
}

/**
 * @description: 根据意图获取观点
 * @param {VocQueryParams} data
 * @return {*}
 */
export const getUserIntentionOpinionTop = (data: VocQueryParams) => {
  return request<any>({
    url: '/report/vocLeadership/getUserIntentionOpinionTop',
    method: 'POST',
    data
  })
}

/**
 * 所有声音列表
 * @param data 查询参数
 */
export const getVocListSounds = (data: VocQueryParams) => {
  return postVocListSoundsByUrl('/report/vocLeadership/getVocListSounds', data)
}

/**
 * 通用：按 URL 拉取声音列表（POST）
 * 说明：用于复用 VoiceListPanel，但根据业务场景切换接口地址；返回结构需与 getVocListSounds 保持一致
 */
export const postVocListSoundsByUrl = (url: string, data: VocQueryParams) => {
  return request<any>({
    url,
    method: 'POST',
    data
  })
}

/**
 * 原始数据查询（自助服务）
 * @param data 查询参数
 */
export const getRawData = (data: VocQueryParams) => {
  return postVocListSoundsByUrl('/report/vocLeadership/getRawData', data)
}

/**
 * 通用：按 URL 拉取声音详情（POST）
 * 说明：用于复用 VoiceListPanel，根据业务场景切换详情接口地址；
 * 详情接口需兼容 { newId, originalId } 入参与统一详情返回结构。
 */
export const postSoundsDetailsByUrl = (url: string, data: VocQueryParams) => {
  return request<any>({
    url,
    method: 'POST',
    data
  })
}

/**
 * 根据ID查询声音数据
 */
export const getSoundsDetails = (data: VocQueryParams) => {
  return postSoundsDetailsByUrl('/report/vocLeadership/getSoundsDetails', data)
}

/**
 * @description: 声音数据高质量打标
 * @param {string[]} idList
 * @return {*}
 */
export const highQuality = (idList: string[]) => {
  return request<any>({
    url: `/report/highQuality/edit`,
    method: 'POST',
    data: {
      idList
    }
  })
}

/**
 * @description: 取消标记
 * @param {string[]} idList
 * @return {*}
 */
export const highQualityDel = (idList: string[]) => {
  return request<any>({
    url: `/report/highQuality/del`,
    method: 'POST',
    data: {
      idList
    }
  })
}
