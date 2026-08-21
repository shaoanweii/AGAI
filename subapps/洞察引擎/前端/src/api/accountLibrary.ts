import request from './index'

/**
 * 账号词库详情列表
 */
export const findAccountLexiconList = (data: Record<string, any>) => {
  return request({
    method: 'POST',
    url: '/insights/accountLexicon/findAccountLexiconList',
    data
  })
}

/**
 * 新增账号词库详情
 */
export const saveAccountLexiconDetails = (data: Record<string, any>) => {
  return request({
    method: 'POST',
    url: '/insights/accountLexicon/saveAccountLexiconDetails',
    data
  })
}

/**
 * 更新账号词库详情
 */
export const updateAccountLexiconDetails = (data: Record<string, any>) => {
  return request({
    method: 'POST',
    url: '/insights/accountLexicon/updateAccountLexiconDetails',
    data
  })
}

/**
 * 根据 id 查询账号词库详情
 */
export const findAccountLexiconInfo = (data: { id: string }) => {
  return request({
    method: 'POST',
    url: '/insights/accountLexicon/findAccountLexiconInfo',
    data
  })
}

/**
 * 批量变更账号词库状态
 */
export const changeAccountLexiconStatus = (data: Record<string, any>) => {
  return request({
    method: 'POST',
    url: '/insights/accountLexicon/changeAccountLexiconStatus',
    data
  })
}

/**
 * 获取账号渠道树
 */
export const getAccountLexiconChannelTree = (params: Record<string, any> = { status: '1' }) => {
  return request({
    method: 'GET',
    url: '/insights/accountLexicon/getChannelTree',
    params
  })
}
