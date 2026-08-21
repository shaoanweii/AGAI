import http from '../http/index'
import { DATA_KNOWLEDGE_API_KEY_HEADER } from '@/constants'
import type {
  AttributeLabelItem,
  CorpusAuditSavePayload,
  InsReportAccountInfoModel,
  InsReportAccountInfoVo,
  InsReportSysDepartVo,
  LabelTag,
  TagLibClientTreeVo,
  TagLibParams
} from './index.d'

/**
 * @description: 获取高级动态查询
 * @param {any} data
 * @return {*}
 */
export const getRoleFilterTypeList = (data?: any): Promise<BaseResponse<any[]>> => {
  return http({
    url: '/report/role/getRoleFilterTypeList',
    method: 'post',
    data: {
      ...data
    }
  })
}

/**
 * @description: 根据标签类型获取标签树
 * @param {any} data
 * @return {*}
 */
export const findTagLabelByType = (data?: any): Promise<BaseResponse<LabelTag[]>> => {
  return http({
    url: '/report/findTagLabelByType',
    method: 'post',
    data: {
      ...data
    }
  })
}

/**
 * @description: 获取一到四级标签
 * @param {any} data
 * @return {*}
 */
export const getTagLibClientTree = (data?: any): Promise<BaseResponse<LabelTag[]>> => {
  return http({
    url: '/report/getTagLibClientTree',
    method: 'post',
    data: {
      ...data
    }
  })
}

/**
 * @description: 校验token有效性
 * @param {string} token
 * @return {*}
 */
export const checkToken = (data: { tokenKey: string }): Promise<BaseResponse<any>> => {
  return http({
    url: `/local/session/check`,
    method: 'POST',
    data
  })
}

//所有数据字典
export const sysAllDictItems = (): Promise<BaseResponse> => {
  return http({
    url: '/report/insDictItem/sysAllDictItems',
    method: 'POST'
  })
}

/**
 * @description: 根据userId换token
 * @param {*} Promise
 * @return {*}
 */
export const getTokenByUserAccount = (account: string): Promise<BaseResponse<any>> => {
  return http({
    url: '/local/session/by-user',
    method: 'POST',
    data: {
      account
    }
  })
}

/**
 * @description: 添加浏览记录
 * @param {BrowseRecordParams} data 浏览记录参数
 * @return {*}
 */
export interface BrowseRecordParams {
  // 声音id
  soundId?: string
  // 原文id
  originalId?: string
  // 浏览时长(秒)
  browseDuration?: number
  // 声音意图
  soundIntention?: string
}

export const browseRecordAdd = (data: BrowseRecordParams): Promise<BaseResponse<any>> => {
  return http({
    url: '/report/user-browse-record/add',
    method: 'POST',
    data
  })
}

/**
 * @description: sso登录
 * @param {*} Promise
 * @return {*}
 */
export const sso = (): Promise<BaseResponse> => {
  return http({
    url: '/local/session/enter',
    method: 'get'
  })
}

/**
 * @description: 洞察引擎免密登录接口
 * @param {any} data
 * @return {*}
 */
export const insFreeLogin = (data: {
  username?: string
  userId?: string
}): Promise<BaseResponse<any>> => {
  return http({
    url: '/insights/base/freeLogin',
    method: 'POST',
    data
  })
}

/**
 * @description: 获取部门树
 * @param {*} Promise
 * @return {*}
 */
export const findDepartTree = (): Promise<BaseResponse<InsReportSysDepartVo[]>> => {
  return http({
    url: '/report/accountInfo/findDepartTree',
    method: 'get'
  })
}

/**
 * @description: 获取部门用户树
 * @param {*} Promise
 * @return {*}
 */
export const findDepartAccountTree = (): Promise<BaseResponse<InsReportSysDepartVo[]>> => {
  return http({
    url: '/report/accountInfo/findDepartAccountTree',
    method: 'get'
  })
}

/**
 * @description: 根据部门id获取部门用户树
 * @param {InsReportAccountInfoModel} data 查询参数，当前主要使用 deptId
 * @return {Promise<BaseResponse<InsReportSysDepartVo[]>>}
 */
export const findDepartAccountTreeByDeptId = (
  data: InsReportAccountInfoModel
): Promise<BaseResponse<InsReportSysDepartVo[]>> => {
  return http({
    url: '/report/accountInfo/findDepartAccountTreeByDeptId',
    method: 'post',
    data
  })
}

/**
 * @description: 根据部门id获取用户列表
 * @param {*} Promise
 * @return {*}
 */
export const findAccountByDeptId = (
  data: InsReportAccountInfoModel
): Promise<BaseResponse<InsReportAccountInfoVo[]>> => {
  return http({
    url: '/report/accountInfo/findAccountByDeptId',
    method: 'post',
    data
  })
}

/**
 * @description: 获取 canswer AuthDataUrl
 * @param {any} params
 * @return {*}
 */
export const getAuthDataUrl = (params: any): Promise<BaseResponse<any>> => {
  return http({
    url: '/report/canswer/getAuthDataUrl',
    method: 'get',
    params
  })
}

/**
 * @description: 回评话术
 * @return {*}
 */
export const ask_simple = (data: {
  question: string
  bot_name: string
  return_all_candidates: boolean
}): Promise<BaseResponse<any>> => {
  return http({
    url: '/review/qa/ask_simple',
    method: 'post',
    data,
    headers: {
      ...DATA_KNOWLEDGE_API_KEY_HEADER
    }
  })
}

/**
 * @description: 用户获取渠道树权限数据
 * @param {*} Promise
 * @return {*}
 */
export const getUserChannelTree = (): Promise<BaseResponse<any[]>> => {
  return http({
    url: '/report/accountInfo/getUserChannelTree',
    method: 'post'
  })
}

/**
 * @description: 获取重点账号树数据
 * @param {*} Promise
 * @return {*}
 */
export const getMainAccTreeData = (data?: any): Promise<BaseResponse<any[]>> => {
  return http({
    url: '/report/keyAccount/findKeyAccountList',
    method: 'post',
    data: data || {}
  })
}

export const getReceiversUserList = (): Promise<BaseResponse<any[]>> => {
  return http({
    url: '/report/subscribe-task/accountList',
    method: 'get'
  })
}

/**
 * @description: 根据标签ID查询标准观点
 * @param {*} Promise
 * @return {*}
 */
export const findFinalTagLibClientVoListByTagId = (
  data: TagLibParams
): Promise<BaseResponse<TagLibClientTreeVo[]>> => {
  return http({
    url: '/report/findAllFinalTagLibClientVoList',
    method: 'post',
    data
  })
}

/**
 * @description: 查询全部属性标签
 * @return {Promise<BaseResponse<AttributeLabelItem[]>>}
 */
export const findAllAttributeLabelList = (
  data: any
): Promise<BaseResponse<AttributeLabelItem[]>> => {
  return http({
    url: '/report/findAllAttributeLabelList',
    method: 'post',
    data
  })
}

/**
 * @description: 新增语料审核
 * @param {CorpusAuditSavePayload} data 新增语料审核参数
 * @return {Promise<BaseResponse<string>>}
 */
export const saveCorpus = (data: CorpusAuditSavePayload): Promise<BaseResponse<string>> => {
  return http({
    url: '/report/vocLeadership/saveCorpus',
    method: 'post',
    data
  })
}

/**
 * 下载文件
 * @param data
 * @returns
 */
export const downloadFile = (data: { id: string }): Promise<BaseResponse<any>> => {
  return http({
    url: '/report/reportDownLoad/downloadFile',
    method: 'post',
    data,
    responseType: 'blob'
  })
}
