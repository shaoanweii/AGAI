import { buildInsightResult, insightMockContracts } from '../lib/insight-demo-api.mjs'

const pageEndpoints = [
  ['/api/insights/insCqCaDataSource/getResultData', 'records'],
  ['/api/insights/insDataSource/findDataProcessingTasks', 'records'],
  ['/api/new-words/search', 'items'],
  ['/api/ai/opinion-synonyms/search', 'items'],
  ['/api/insights/insTagLibClient/findAllTopicList', 'records'],
  ['/api/insights/carScene/findCarSceneList', 'records'],
  ['/api/insights/carSeriesInfo/queryBySelect', 'records'],
  ['/api/insights/brandInfo/queryBySelect', 'records'],
  ['/api/insights/automark/findAutomarkList', 'records'],
  ['/api/insights/insDataResourceDesc/list', 'records'],
  ['/api/insights/accountLexicon/findAccountLexiconList', 'records'],
  ['/api/insights/ruleTest/ruleTestList', 'records'],
  ['/api/insights/accountInfo/findAccountInfoList', 'records'],
  ['/api/insights/role/queryRoleList', 'records'],
  ['/api/insights/downLoad/findDownLoadFileList', 'records']
]

for (const [pathname, listKey] of pageEndpoints) {
  const body = pathname.includes('accountLexicon')
    ? { resourceId: insightMockContracts.accountGroups[0].id }
    : pathname.includes('insDataResourceDesc')
      ? { resourceId: insightMockContracts.ruleGroups[0].id }
      : {}
  const response = buildInsightResult(pathname, 'POST', body)
  if (!response.handled || !Array.isArray(response.result?.[listKey]) || response.result[listKey].length === 0) {
    throw new Error(`${pathname} 未返回非空 ${listKey}`)
  }
}

const arrayEndpoints = [
  '/api/insights/insCqCaDataSource/getChannelTree',
  '/api/insights/insTagLibClient/getTagLibClientTree',
  '/api/insights/insTagLibClient/findAllFinalTagLib',
  '/api/insights/carSceneCategory/findCarSceneCategoryTree',
  '/api/insights/carScene/findCarSceneOperatorList',
  '/api/insights/insDataResource/findAllResourceTree',
  '/api/insights/accountInfo/queryRoleALlList',
  '/api/insights/accountInfo/findDepartList'
]

for (const pathname of arrayEndpoints) {
  const response = buildInsightResult(pathname, 'POST', {})
  if (!response.handled || !Array.isArray(response.result) || response.result.length === 0) {
    throw new Error(`${pathname} 未返回非空数组`)
  }
}

for (const type of ['rule', 'account']) {
  const response = buildInsightResult('/api/insights/insDataResource/findDataResourceList', 'POST', { type })
  if (!response.handled || !response.result.records?.length || response.result.records.some(item => !item.name || !item.cnt)) {
    throw new Error(`${type} 分组数据不完整`)
  }
}

const conditions = buildInsightResult('/api/insights/accountLexicon/conditions', 'GET', {})
const accountStatus = conditions.result?.find(item => item.key === 'ruleStatus')?.details || []
if (!conditions.handled || !accountStatus.some(item => item.key === '1')) {
  throw new Error('词库状态字典缺失')
}

for (const group of insightMockContracts.ruleGroups) {
  const response = buildInsightResult('/api/insights/insDataResourceDesc/list', 'POST', { resourceId: group.id })
  if (response.result.records.length < 4) throw new Error(`${group.name} 规则数据不足`)
}

for (const group of insightMockContracts.accountGroups) {
  const response = buildInsightResult('/api/insights/accountLexicon/findAccountLexiconList', 'POST', { resourceId: group.id })
  if (response.result.records.length < 4) throw new Error(`${group.name} 账号数据不足`)
}

const foreignNamespace = buildInsightResult('/api/report/group-analysis/list', 'POST', {})
if (foreignNamespace.handled) {
  throw new Error('洞察 Mock 不得接管 VOC 接口')
}

console.log(`✓ ${pageEndpoints.length} 个页面列表接口返回完整数据`)
console.log(`✓ ${arrayEndpoints.length} 个树/选项接口返回完整数据`)
console.log('✓ 规则词库与账号词库分组数据已隔离')
console.log('✓ 洞察接口不会回落或污染 VOC Mock')
