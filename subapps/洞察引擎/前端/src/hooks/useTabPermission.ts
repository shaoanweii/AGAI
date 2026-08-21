import { hasPermission } from '@/utils/permission'

/**
 * 配置tab页签的权限
 * @param pageName
 */
export function useTabPermission(pageName: CommonType.hooks.PageName) {
  const activeKey = ref('')
  const founding: CommonType.hooks.TPPage = {
    defaultActive: '1',
    permKeyMap: new Map([
      // ["1", { perm: "dataCenter-dataSource-select", key: "1" }],
      // ["2", { perm: "dataCenter-corpus-select", key: "2" }],
      ['5', { perm: 'dataCenter-founding-commonDataBase', key: '5' }],
      ['3', { perm: 'dataCenter-carSeries-select', key: '3' }],
      // ["4", { perm: "dataCenter-resources-select", key: "4" }],
      ['6', { perm: 'dataCenter-founding-knowledgeBase', key: '6' }]
    ])
  }
  const processing: CommonType.hooks.TPPage = {
    defaultActive: 'general',
    permKeyMap: new Map([
      ['general', { perm: 'dataCenter-standard-select', key: 'general' }],
      ['custom', { perm: 'dataCenter-customized-select', key: 'custom' }],
      ['4', { perm: 'dataCenter-resources-select', key: '4' }]
    ])
  }
  const discovery: CommonType.hooks.TPPage = {
    defaultActive: '1',
    permKeyMap: new Map([
      ['1', { perm: 'tagManagement-words-select', key: '1' }],
      ['2', { perm: 'tagManagement-opinions-select', key: '2' }]
      // ["3", { perm: "tagManagement-keywords-select", key: "3" }],
    ])
  }
  const baseSettings: CommonType.hooks.TPPage = {
    defaultActive: 'findEnergyInfo',
    permKeyMap: new Map([
      ['findEnergyInfo', { perm: 'settings-energyType-select', key: 'findEnergyInfo' }],
      ['findProvinceAreaInfo', { perm: 'settings-area-select', key: 'findProvinceAreaInfo' }],
      ['findVehicleInfo', { perm: 'settings-carType-select', key: 'findVehicleInfo' }],
      ['findLabelTypeInfo', { perm: 'settings-labelType-select', key: 'findLabelTypeInfo' }],
      [
        'findSeriousnessInfo',
        { perm: 'settings-severityLevel-select', key: 'findSeriousnessInfo' }
      ],
      ['findUserJourneyInfo', { perm: 'settings-userJourney-select', key: 'findUserJourneyInfo' }],
      [
        'channelConfiguration',
        { perm: 'settings-channelConfig-select', key: 'channelConfiguration' }
      ],
      ['regionConfiguration', { perm: 'settings-regionConfig-select', key: 'regionConfiguration' }],
      ['riskKeywords', { perm: 'tagManagement-keywords-select', key: 'riskKeywords' }]
    ])
  }
  const insDataSource: CommonType.hooks.TPPage = {
    defaultActive: 'local',
    permKeyMap: new Map([
      ['local', { perm: 'dataCenter-dataSource-select', key: 'local' }],
      ['system', { perm: 'dataCenter-dataSource-select', key: 'system' }]
    ])
  }
  let curPage: CommonType.hooks.TPPage = founding

  const getActiveByPermission = (pageObj: CommonType.hooks.TPPage) => {
    return (
      [...pageObj?.permKeyMap.values()].find(el => hasPermission(el.perm))?.key ||
      pageObj.defaultActive
    )
  }

  const getHasPermission = (key: string) => {
    if (curPage.permKeyMap.has(key)) {
      // eslint-disable-next-line @typescript-eslint/no-non-null-asserted-optional-chain
      return hasPermission(curPage.permKeyMap.get(key)?.perm!)
    } else {
      return false
    }
  }

  onMounted(() => {
    switch (pageName) {
      case 'founding':
        activeKey.value = getActiveByPermission(founding)
        curPage = founding
        break
      case 'processing':
        activeKey.value = getActiveByPermission(processing)
        curPage = processing
        break
      case 'discovery':
        activeKey.value = getActiveByPermission(discovery)
        curPage = discovery
        break
      case 'baseSettings':
        activeKey.value = getActiveByPermission(baseSettings)
        curPage = baseSettings
        break
      case 'insDataSource':
        activeKey.value = getActiveByPermission(insDataSource)
        curPage = insDataSource
        break
    }
  })
  return {
    activeKey,
    getHasPermission
  }
}
