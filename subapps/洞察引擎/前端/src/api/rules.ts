import request from './index'
import { DATA_KNOWLEDGE_API_KEY_HEADER } from './constants'

/**
 * @description: 闭环规则新增分类
 * @param {any} data
 * @return {*}
 */
export const insDataResourceInsert = (data?: any) => {
  return request({
    method: 'post',
    url: '/insights/insDataResource/insert',
    data
  })
}

/**
 * @description: 闭环规则更新分类
 * @param {any} data
 * @return {*}
 */
export const insDataResourceUpdate = (data?: any) => {
  return request({
    method: 'post',
    url: '/insights/insDataResource/update',
    data
  })
}

/**
 * @description: 闭环规则删除分类
 * @param {any} data
 * @return {*}
 */
export const insDataResourceDelete = (data?: any): any => {
  return request({
    method: 'post',
    url: '/insights/insDataResource/delete',
    data
  })
}

/**
 * @description: 获取闭环规则分类列表
 * @param {any} data
 * @return {*}
 */
export const findDataResourceList = (data?: any): any => {
  return request({
    method: 'post',
    url: '/insights/insDataResource/findDataResourceList',
    data
  })
}

/**
 * @description: 获取闭环规则的规则列表
 * @param {any} data
 * @return {*}
 */
export const queryRulePage = (data?: any): any => {
  return request({
    method: 'post',
    url: '/insights/insClosedRule/queryRulePage',
    data
  })
}

//复制规则
export const getCopyRule = (ruleId): any => {
  return request({
    method: 'put',
    url: `/insights/insClosedRule/${ruleId}`
  })
}

// 详情查询：根据 ruleId 获取闭环规则详情
export const getRuleDetail = (ruleId): any => {
  return request({
    method: 'get',
    url: `/insights/insClosedRule/${ruleId}`
  })
}

//获取渠道树
export const getChannelTree = (): any => {
  return request({
    method: 'get',
    url: `/insights/insClosedRule/getChannelTree`
  })
}

//获取部门树
export const getDeptTree = (data): any => {
  return request({
    method: 'post',
    url: `/insights/accountInfo/findDepartTree`,
    data
  })
}

//根据部门id获取员工
export const getEmployeeList = (deptId): any => {
  return request({
    method: 'post',
    url: `/insights/accountInfo/findAccountByDeptId`,
    data: { deptId }
  })
}

//获取规则条件配置
export const findConditionConfig = (): any => {
  return request({
    method: 'get',
    url: '/insights/insClosedRule/findConditionConfig'
  })
}

//1-4级全领域业务
export const getTagLibClientTree = (data): any => {
  return request({
    method: 'post',
    url: '/insights/insTagLibClient/getTagLibClientTree',
    data
  })
}

//标准观点
export const getTagLibStandardView = (data): any => {
  return request({
    method: 'post',
    url: '/insights/insTagLibClient/findAllFinalTagLibClientVoList',
    data,
    headers: { ...DATA_KNOWLEDGE_API_KEY_HEADER }
  })
}

//新增规则
export const insertRule = (data): any => {
  return request({
    method: 'put',
    url: '/insights/insClosedRule/insert',
    data
  })
}

//编辑
export const updateRule = (data): any => {
  return request({
    method: 'put',
    url: '/insights/insClosedRule/update',
    data
  })
}

//获取账号词库
export const findAllResourceTree = (data): any => {
  return request({
    method: 'post',
    url: '/insights/insDataResource/findAllResourceTree',
    data
  })
}

// 批量操作：启用/停用等
export const batchOperateClosedRule = (data): any => {
  return request({
    method: 'post',
    url: '/insights/insClosedRule/batchOperation',
    data
  })
}

//获取下拉创建用户
export const queryCreateUserList = (data): any => {
  return request({
    method: 'post',
    url: '/insights/ruleTest/queryCreateUserList',
    data
  })
}

//获取规则下拉列表
export const ruleSelect = (data): any => {
  return request({
    method: 'post',
    url: '/insights/ruleTest/ruleSelect',
    data
  })
}

/**
 * 上传文件
 * @param data
 */
export const uploadFile = (data: any) => {
  return request<any>({
    method: 'POST',
    url: '/insights/ruleTest/uploadRuleTest',
    data
  })
}

/***下载模板 */
export const downloadTemplate = (data: any) => {
  return request<any>({
    method: 'GET',
    url: '/insights/ruleTest/downloadRuleTest',
    params: data,
    responseType: 'blob'
  })
}

/** 检查上传文件*/
export const checkUploadRuleTest = (data: any) => {
  return request<any>({
    method: 'POST',
    url: '/insights/ruleTest/checkUploadRuleTest',
    data
  })
}

/** 添加规则测试*/
export const addRuleTestList = (data: any) => {
  return request<any>({
    method: 'POST',
    url: '/insights/ruleTest/addRuleTestList',
    data
  })
}

/** 复制规则测试*/
export const copyRuleTest = (data: any) => {
  return request<any>({
    method: 'POST',
    url: '/insights/ruleTest/copyRuleTest',
    data
  })
}

/** 获取规则测试详情*/
export const getInfoRuleId = (data: any) => {
  return request<any>({
    method: 'POST',
    url: '/insights/ruleTest/getInfoRuleId',
    data
  })
}

/** 开始规则测试*/
export const startRuleTest = (data: any) => {
  return request<any>({
    method: 'POST',
    url: '/insights/ruleTest/startRuleTest',
    data
  })
}

/** 获取规则信息列表*/
export const getRuleInfo = (data: any) => {
  return request<any>({
    method: 'POST',
    url: '/insights/ruleTest/getRuleInfo',
    data
  })
}

/** 获取规则测试执行详情*/
export const getExecuteRuleTestInfo = (data: any) => {
  return request<any>({
    method: 'POST',
    url: '/insights/ruleTest/getExecuteRuleTestInfo',
    data
  })
}
