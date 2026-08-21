import { useUserStore, useQueryStore } from '@/store'
import type { FilterFieldConfig } from './types'
import { FE_TIME_DIMENSION_OPTIONS, ProductFilterTagName, ServiceFilterTagName, TagType } from '@/constants'
import { cloneDeep } from 'lodash-es'
import dayjs from 'dayjs'

// ==================== 通用配置项 ====================

/**
 * 日期范围配置
 */
const dateRangeConfig: FilterFieldConfig = {
  type: 'daterange',
  prop: 'dateRange',
  label: '日期范围',
  filterType: '93',
  span: 24,
  defaultValue: undefined
}

/**
 * 数据源配置
 */
const dataSourceConfig: FilterFieldConfig = {
  type: 'dataSource',
  prop: 'channelIds',
  label: '数据源',
  span: 6,
  clearable: true,
  placeholder: '请选择',
  filterType: '95',
  options: []
}

/**
 * 数据类型配置
 */
const contentTypeConfig: FilterFieldConfig = {
  type: 'select',
  prop: 'contentTypes',
  label: '数据类型',
  span: 6,
  clearable: true,
  multiple: true,
  placeholder: '请选择',
  get options() {
    return useUserStore().getDictItems('content_type')
  },
  props: {
    label: 'text',
    value: 'value'
  }
}

/**
 * 占位符配置
 */
const placeholderConfig: FilterFieldConfig = {
  type: 'placeholder',
  prop: '',
  span: 12
}

/**
 * 是否主贴配置
 */
const isMainPostConfig: FilterFieldConfig = {
  type: 'select',
  prop: 'isMainPost',
  label: '是否主贴',
  span: 6,
  clearable: true,
  get options() {
    return useUserStore().getDictItems('is_main_post')
  },
  props: {
    label: 'text',
    value: 'value'
  }
}

/**
 * 客户性别配置
 */
const genderConfig: FilterFieldConfig = {
  type: 'select',
  prop: 'gender',
  label: '客户性别',
  span: 6,
  clearable: true,
  get options() {
    return useUserStore().getDictItems('cust_gender')
  },
  props: {
    label: 'text',
    value: 'value'
  }
}

/**
 * 是否水军配置
 */
const isWsaterArmyConfig: FilterFieldConfig = {
  type: 'select',
  prop: 'isWsaterArmy',
  label: '是否水军',
  span: 6,
  clearable: true,
  get options() {
    return useUserStore().getDictItems('voc_is_wsater_army')
  },
  props: {
    label: 'text',
    value: 'value'
  }
}

/**
 * 省份配置
 */
const provinceConfig: FilterFieldConfig = {
  type: 'select',
  prop: 'custProvinceCodeSet',
  label: '省份',
  span: 6,
  clearable: true,
  multiple: true,
  get options() {
    return useUserStore().getDictItems('voc_province')
  },
  props: {
    label: 'text',
    value: 'value'
  }
}

/**
 * 是否大V配置
 */
const isBigVConfig: FilterFieldConfig = {
  type: 'select',
  prop: 'isBigV',
  label: '账号类型',
  span: 6,
  clearable: true,
  multiple: true,
  get options() {
    return useUserStore().getDictItems('account_type')
  },
  props: {
    label: 'text',
    value: 'value'
  }
}

/**
 * 原声类型配置
 */
const adTypeConfig: FilterFieldConfig = {
  type: 'select',
  prop: 'advertisementType',
  label: '原声类型',
  span: 6,
  clearable: true,
  multiple: true,
  defaultValue: [],
  get options() {
    return useUserStore().getDictItems('batch_ad_type')
  },
  props: {
    label: 'text',
    value: 'value'
  }
}

/**
 * 属性标签配置
 */
const attributeTagConfig: FilterFieldConfig = {
  type: 'select',
  prop: 'scenarioAttr',
  label: '属性标签',
  span: 6,
  clearable: true,
  multiple: true,
  defaultValue: [],
  options: [],
  props: {
    label: 'name',
    value: 'id'
  }
}

/**
 * 客户年龄配置
 */
const custAgeConfig: FilterFieldConfig = {
  type: 'select',
  prop: 'custAges',
  label: '客户年龄',
  span: 6,
  clearable: true,
  multiple: true,
  get options() {
    return useUserStore().getDictItems('voc_cust_age')
  },
  props: {
    label: 'text',
    value: 'value'
  }
}

/**
 * 是否车主配置
 */
const isCarOwnerConfig: FilterFieldConfig = {
  type: 'select',
  prop: 'isCarOwner',
  label: '是否车主',
  span: 6,
  clearable: true,
  get options() {
    return useUserStore().getDictItems('voc_is_car_owner')
  },
  props: {
    label: 'text',
    value: 'value'
  }
}

/**
 * 品牌配置
 */
const brandConfig: FilterFieldConfig = {
  type: 'brand',
  prop: 'brandCode',
  label: '品牌',
  filterType: '91',
  span: 24,
  options: [],
  defaultValue: '',
  showSplitLine: true
}

/**
 * 车系配置
 */
const seriesConfig: FilterFieldConfig = {
  type: 'series',
  prop: 'carSeriesList',
  label: '车系',
  span: 24,
  options: [],
  defaultValue: undefined,
  showSplitLine: true
}

/**
 * 本品品牌配置
 */
const thisProductBrandConfig: FilterFieldConfig = {
  ...brandConfig,
  prop: 'brandCodeList',
  label: '本品品牌',
  defaultValue: [],
  multiple: true
}

/**
 * 获取本品品牌的动态默认值
 * 优先取当前品牌数据源中的第一个品牌，并兼容字段自身的取值配置。
 * @param field 当前字段配置
 * @returns 动态默认值
 */
const getFirstBrandDefaultValue = (field: FilterFieldConfig) => {
  const brandOptions = useUserStore().getBrandService || []
  const firstBrand = brandOptions[0]

  if (!firstBrand) {
    return undefined
  }

  const valueKey = field.props?.value || 'key'
  const brandValue =
    firstBrand?.[valueKey] ?? firstBrand?.key ?? firstBrand?.value ?? firstBrand?.brandCode

  if (brandValue === undefined || brandValue === null || brandValue === '') {
    return undefined
  }

  return field.multiple ? [brandValue] : brandValue
}

/**
 * 本品车系配置
 */
const thisProductSeriesConfig: FilterFieldConfig = {
  ...seriesConfig,
  label: '本品车系',
  defaultValue: [],
  multiple: true
}

/**
 * 竞品品牌车系配置
 */
const compCarSeriesConfig: FilterFieldConfig = {
  type: 'cascader',
  prop: 'compCarSeriesList',
  label: '竞品品牌车系',
  span: 24,
  clearable: true,
  multiple: true,
  options: [],
  defaultValue: [],
  showSplitLine: true,
  cascaderProps: {
    value: 'key',
    label: 'value',
    children: 'children',
    multiple: true,
    emitPath: true,
    checkStrictly: true
  }
}

/**
 * 竞品品牌配置
 */
const compBrandConfig: FilterFieldConfig = {
  type: 'selectv2',
  prop: 'compBrandCodeList',
  label: '竞品品牌',
  span: 12,
  clearable: true,
  multiple: true,
  options: [],
  defaultValue: [],
  placeholder: '请选择',
  props: {
    label: 'value',
    value: 'key'
  },
  showSelectAll: true
}

/**
 * 竞品车系配置
 */
const compSeriesConfig: FilterFieldConfig = {
  type: 'selectv2',
  prop: 'compCarSeriesList',
  label: '竞品车系',
  span: 12,
  clearable: true,
  multiple: true,
  options: [],
  defaultValue: [],
  placeholder: '请选择',
  props: {
    label: 'value',
    value: 'key'
  },
  showSelectAll: true,
  showSplitLine: true
}

/**
 * 用户昵称配置
 */
const userNicknameConfig: FilterFieldConfig = {
  type: 'input',
  prop: 'customerName',
  label: '用户昵称',
  span: 6,
  clearable: true,
  maxLength: 50
}

/**
 * 用户ID配置
 */
const userIdConfig: FilterFieldConfig = {
  type: 'input',
  prop: 'oneId',
  label: '用户ID',
  span: 6,
  clearable: true,
  maxLength: 50
}

/**
 * 主帖用户配置
 */
const retweetedNameConfig: FilterFieldConfig = {
  type: 'input',
  prop: 'retweetedName',
  label: '主帖用户',
  span: 6,
  clearable: true,
  maxLength: 50
}

/**
 * 主帖用户 ID 配置
 */
const retweetedUserIdConfig: FilterFieldConfig = {
  type: 'input',
  prop: 'retweetedUserId',
  label: '主帖用户ID',
  span: 6,
  clearable: true,
  maxLength: 50
}

/**
 * 原始数据ID配置
 */
const originalDataIdConfig: FilterFieldConfig = {
  type: 'input',
  prop: 'dataId',
  label: '原始数据ID',
  span: 6,
  clearable: true,
  maxLength: 50
}

/**
 * 原文链接配置
 */
const originalLinkConfig: FilterFieldConfig = {
  type: 'input',
  prop: 'originalLink',
  label: '原文链接',
  span: 6,
  clearable: true,
  maxLength: 100
}

/**
 * 标题/原声查询配置
 */
const titleQueryConfig: FilterFieldConfig = {
  type: 'input',
  prop: 'titleOrOriginal',
  label: '标题/原声查询',
  span: 12,
  clearable: true,
  maxLength: 50
}

/**
 * 客户体验代码配置
 */
const experienceCodeConfig: FilterFieldConfig = {
  type: 'experienceCode',
  prop: 'experienceCode',
  label: '客户体验代码',
  filterType: '92',
  span: 12
}

/**
 * 客户体验代码（类型 + 级联联动）配置
 * 仅在需要“多套标签体系”切换的页面启用（例如根因分析）
 */
const experienceCodeLinkageConfig: FilterFieldConfig = {
  type: 'experienceCodeLinkage',
  prop: 'experienceCode',
  tagTypeProp: 'tagType',
  tagTypeDefaultValue: 'CA',
  label: '客户体验代码',
  filterType: '92',
  span: 12
}

/**
 * 固定 CA 的客户体验代码级联配置
 * 用于产品/服务/竞品等不展示标签体系切换的页面。
 */
const fixedDomainExperienceCodeLinkageConfig: FilterFieldConfig = {
  type: 'experienceCodeLinkage',
  prop: 'experienceCode',
  fixedTagType: TagType.Domain,
  hideTagType: true,
  requestLevel: 4,
  label: '客户体验代码',
  filterType: '92',
  span: 12
}

/**
 * 标准观点配置
 */
const standardViewpointConfig: FilterFieldConfig = {
  type: 'selectv2',
  prop: 'topicCodes',
  label: '标准观点',
  span: 6,
  clearable: true,
  multiple: true,
  options: [], // 通过接口动态加载，使用 findFinalTagLibClientVoListByTagId({})
  props: {
    label: 'tagName',
    value: 'tagCode'
  },
  showSelectAll: true // 显示全选按钮
}

/**
 * 用车场景配置
 * 内部保留级联路径用于区分一级/二级，查询时转换为后端需要的两个集合字段。
 */
const usageScenarioConfig: FilterFieldConfig = {
  type: 'cascader',
  prop: 'usageScenarioCodes',
  label: '用车场景',
  span: 6,
  clearable: true,
  multiple: true,
  defaultValue: [],
  options: [],
  cascaderProps: {
    value: 'value',
    label: 'value',
    children: 'children',
    multiple: true,
    emitPath: true,
    checkStrictly: true
  }
}

/**
 * 情感配置（根因分析专用）
 */
const emotionConfig: FilterFieldConfig = {
  type: 'btnSwitch',
  // prop: 'sentiment',
  prop: 'sentimentList',
  label: '情感',
  span: 24,
  multiple: true,
  get options() {
    return useUserStore().getDictItems('voc_sentiment')
  },
  props: {
    label: 'text',
    value: 'value'
  },
  showSplitLine: true
}

/**
 * 意图配置（根因分析专用）
 */
const intentConfig: FilterFieldConfig = {
  type: 'btnSwitch',
  // prop: 'intention',
  prop: 'intentionList',
  label: '意图',
  span: 24,
  multiple: true,
  get options() {
    return useUserStore().getDictItems('voc_intention')
  },
  props: {
    label: 'text',
    value: 'value'
  },
  showSplitLine: true
}

/**
 * 获取新品车系的动态默认值
 * 优先取第一个品牌下的第一个车系，供新品车系异步选项加载完成后自动回填。
 * @param field 当前字段配置
 * @returns 新品车系默认值
 */
const getFirstNewCarSeriesDefaultValue = (
  field: FilterFieldConfig,
  otherProps?: { allConfig: FilterFieldConfig[]; route: any }
) => {
  const options = Array.isArray(field.options) ? field.options : []

  // 处理禁用逻辑
  const todoDis = (options: any, value: any, handFile: any) => {
    options?.forEach((option: any) => {
      const children = option[handFile?.cascaderProps?.children]
      if (children?.length) {
        children.forEach((child: any) => {
          if (child[handFile?.cascaderProps?.value] === value) {
            child.disabled = true
          } else {
            child.disabled = false
          }
        })
      }
    })
  }

  // JSON.parse
  const optionsJsonStr = sessionStorage.getItem('seriesCarSessionData')
  let newCarListSeries: any = [] // 新品车系
  // let compareCarListSeries: any = [] // 对比车系
  if (optionsJsonStr) {
    const optionsJson = JSON.parse(optionsJsonStr)
    newCarListSeries = optionsJson.newCarSeries || []
    // compareCarListSeries = optionsJson.compareCarSeries || []
  }
  const childrenKey = field.cascaderProps?.children || 'children'
  const valueKey = field.cascaderProps?.value || 'key'
  const firstBrand = options[0]
  let firstSeries = Array.isArray(firstBrand?.[childrenKey]) ? firstBrand[childrenKey]?.[0] : null
  let seriesValue =
    firstSeries?.[valueKey] ?? firstSeries?.key ?? firstSeries?.value ?? firstSeries?.code

  // 新车上市 新品车系
  if (field.prop === 'newCarSeriesList') {
    // 设置遍历数据
    if (newCarListSeries.length) {
      // 查找isDefault = true设置成默认的
      newCarListSeries.forEach((element: any) => {
        const cars = element.cars || []
        cars.forEach((car: any) => {
          if (car.isDefault) {
            firstSeries = car
            seriesValue = car?.[valueKey]
          }
        })
      })
    }
    // 处理竞品禁用逻辑
    const realFile = otherProps?.allConfig.find((item: any) => item.prop === 'compCarSeriesList')

    if (realFile) {
      todoDis(realFile.options, seriesValue, realFile)
    }

    useQueryStore().updateQueryParams({
      newCarSeriesObjList: firstSeries ? [{ ...firstSeries }] : []
    })
  }

  // 新车上市 对比车系
  if (field.prop === 'compCarSeriesList') {
    // 设置遍历数据
    if (newCarListSeries.length) {
      // 查找isDefault = true设置成默认的
      newCarListSeries.forEach((element: any) => {
        const cars = element.cars || []
        cars.forEach((car: any) => {
          if (car.isDefault) {
            // 说明找到对应的新车系
            const competitiveProduct = car?.competitiveProduct || []
            if (competitiveProduct.length) {
              firstSeries = competitiveProduct[0]
              seriesValue = competitiveProduct[0]?.[valueKey]
            }
          }
        })
      })
    }
    // 处理新品禁用逻辑
    const realFile = otherProps?.allConfig.find((item: any) => item.prop === 'newCarSeriesList')
    if (realFile) {
      todoDis(realFile.options, seriesValue, field)
    }

    // 中转页面跳转 或者 报告列表跳转过来
    if (!(otherProps?.route?.query?.centerJudge || otherProps?.route?.query?.reportJudgeId)) {
      if (!useQueryStore().currentQueryParams?.compCarSeriesObjList?.length) {
        useQueryStore().updateQueryParams({
          compCarSeriesObjList: firstSeries ? [{ ...firstSeries }] : []
        })
      }
    }
  }

  if (seriesValue === undefined || seriesValue === null || seriesValue === '') {
    return undefined
  }

  return seriesValue
}

// 新品车系配置
const newCarSeriesConfig: FilterFieldConfig = {
  type: 'cascader',
  prop: 'newCarSeriesList',
  label: '新品车系',
  span: 6,
  clearable: true,
  multiple: false,
  options: [],
  defaultValue: undefined,
  getDefaultValue: getFirstNewCarSeriesDefaultValue,
  cascaderProps: {
    value: 'code',
    label: 'name',
    children: 'cars',
    multiple: false,
    emitPath: false, // 在选中节点改变时，是否返回由该节点所在的各级菜单的值所组成的数组，若设置 false，则只返回该节点的值
    checkStrictly: false
  }
}

// 对比车系配置
const compareCarSeriesConfig: FilterFieldConfig = {
  type: 'cascader',
  prop: 'compCarSeriesList',
  label: '对比车系',
  span: 6,
  clearable: true,
  multiple: false,
  options: [],
  defaultValue: undefined,
  getDefaultValue: getFirstNewCarSeriesDefaultValue,
  cascaderProps: {
    value: 'code',
    label: 'name',
    children: 'cars',
    multiple: false,
    emitPath: false,
    checkStrictly: false
  }
}

const getFirstAccDefaultValue = (field: FilterFieldConfig) => {
  // const options = otherPros?.mainAccOptions || []
  const options = Array.isArray(field.options) ? field.options : []
  const childrenKey = field.cascaderProps?.children || 'children'
  const valueKey = field.cascaderProps?.value || 'key'
  // 默认 全选“本品官方账号”
  const firstArr = options.find(e => e[valueKey] === '697977628f9730a2035fc8cae7425ae6')
  const firstArrChild = Array.isArray(firstArr?.[childrenKey]) ? firstArr[childrenKey] : []
  const seriesValue = firstArrChild?.map(d => d?.[valueKey])

  // console.log('初始化111', field, seriesValue, options)

  if (seriesValue === undefined || seriesValue === null) {
    return undefined
  }

  return seriesValue
}

/**
 * 重点账号
 */
const mainAccSelectConfig: FilterFieldConfig = {
  type: 'cascader',
  prop: 'keyAccounts',
  label: '重点账号',
  span: 24,
  clearable: true,
  multiple: true,
  placeholder: '请选择',
  defaultValue: undefined,
  getDefaultValue: getFirstAccDefaultValue,
  cascaderProps: {
    value: 'accountId',
    label: 'accountName',
    children: 'children',
    multiple: true,
    emitPath: false, // 在选中节点改变时，是否返回由该节点所在的各级菜单的值所组成的数组，若设置 false，则只返回该节点的值
    checkStrictly: false
  },
  options: []
}

/**
 * 所属主贴
 */
const mainAccInputConfig: FilterFieldConfig = {
  type: 'input',
  prop: 'theTitleofTheMainPost',
  label: '所属主贴',
  span: 6,
  clearable: true,
  placeholder: '请输入',
  maxLength: 50
}
/**
 * 热点事件详情页面日期范围
 */
const dateRangeHotConfig: FilterFieldConfig = {
  type: 'daterange',
  prop: 'customRangeTimes',
  label: '日期范围',
  span: 12,
  defaultValue: undefined
}

/**
 * 本品品牌配置
 */
const brandHotConfig: FilterFieldConfig = {
  ...brandConfig,
  prop: 'brandCodeList',
  label: '品牌',
  defaultValue: [],
  multiple: true
}

// ==================== 路由配置映射 ====================
const filterConfigMap: Record<string, FilterFieldConfig[]> = {
  // 集团分析
  groupAnalysis: [
    dateRangeConfig,
    dataSourceConfig,
    contentTypeConfig,
    placeholderConfig,
    isMainPostConfig,
    genderConfig,
    isWsaterArmyConfig,
    provinceConfig,
    isBigVConfig,
    adTypeConfig,
    attributeTagConfig,
    // custAgeConfig,
    isCarOwnerConfig
  ],
  // 竞品对比
  competitorAnalysis: [
    dateRangeConfig,
    dataSourceConfig,
    contentTypeConfig,
    placeholderConfig,
    fixedDomainExperienceCodeLinkageConfig,
    usageScenarioConfig,
    isMainPostConfig,
    genderConfig,
    isWsaterArmyConfig,
    provinceConfig,
    isBigVConfig,
    adTypeConfig,
    attributeTagConfig,
    // custAgeConfig,
    isCarOwnerConfig
  ],
  // 本品分析
  thisProductAnalysis: [
    dateRangeConfig,
    brandConfig,
    seriesConfig,
    dataSourceConfig,
    contentTypeConfig,
    placeholderConfig,
    usageScenarioConfig,
    isMainPostConfig,
    genderConfig,
    isWsaterArmyConfig,
    provinceConfig,
    isBigVConfig,
    adTypeConfig,
    attributeTagConfig,
    // custAgeConfig,
    isCarOwnerConfig
  ],
  // 旅程分析
  journeyAnalysis: [
    dateRangeConfig,
    brandConfig,
    seriesConfig,
    dataSourceConfig,
    contentTypeConfig,
    userNicknameConfig,
    userIdConfig,
    originalDataIdConfig,
    originalLinkConfig,
    titleQueryConfig,
    experienceCodeConfig,
    standardViewpointConfig,
    isMainPostConfig,
    genderConfig,
    isWsaterArmyConfig,
    provinceConfig,
    isBigVConfig,
    adTypeConfig,
    attributeTagConfig,
    // custAgeConfig,
    isCarOwnerConfig
  ],
  // 产品分析
  productAnalysis: [
    dateRangeConfig,
    brandConfig,
    seriesConfig,
    dataSourceConfig,
    contentTypeConfig,
    userNicknameConfig,
    userIdConfig,
    originalDataIdConfig,
    originalLinkConfig,
    titleQueryConfig,
    {
      ...fixedDomainExperienceCodeLinkageConfig,
      rootTagName: ProductFilterTagName,
      hideRootInCascader: true
    },
    standardViewpointConfig,
    usageScenarioConfig,
    isMainPostConfig,
    genderConfig,
    isWsaterArmyConfig,
    provinceConfig,
    isBigVConfig,
    adTypeConfig,
    attributeTagConfig,
    // custAgeConfig,
    isCarOwnerConfig
  ],
  // 服务分析
  serviceAnalysis: [
    dateRangeConfig,
    brandConfig,
    seriesConfig,
    dataSourceConfig,
    contentTypeConfig,
    userNicknameConfig,
    userIdConfig,
    originalDataIdConfig,
    originalLinkConfig,
    titleQueryConfig,
    {
      ...fixedDomainExperienceCodeLinkageConfig,
      rootTagName: ServiceFilterTagName,
      hideRootInCascader: true
    },
    standardViewpointConfig,
    usageScenarioConfig,
    isMainPostConfig,
    genderConfig,
    isWsaterArmyConfig,
    provinceConfig,
    isBigVConfig,
    adTypeConfig,
    attributeTagConfig,
    // custAgeConfig,
    isCarOwnerConfig
  ],
  // 重点账号
  mainAccount: [
    {
      ...dateRangeConfig,
      defaultValue: [dayjs().startOf('quarter').format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')] // 默认本季
    },
    mainAccSelectConfig,
    dataSourceConfig,
    mainAccInputConfig,
    originalDataIdConfig,
    titleQueryConfig,
    standardViewpointConfig,
    isWsaterArmyConfig
    // isBigVConfig
  ],
  // 根因分析
  rootCause: [
    dateRangeConfig,
    {
      ...thisProductBrandConfig,
      defaultValue: undefined,
      getDefaultValue: getFirstBrandDefaultValue
    },
    { ...thisProductSeriesConfig, showSplitLine: true },
    compBrandConfig,
    compSeriesConfig,
    emotionConfig,
    intentConfig,
    dataSourceConfig,
    contentTypeConfig,
    userNicknameConfig,
    userIdConfig,
    originalDataIdConfig,
    originalLinkConfig,
    titleQueryConfig,
    experienceCodeLinkageConfig,
    standardViewpointConfig,
    usageScenarioConfig,
    isMainPostConfig,
    genderConfig,
    isWsaterArmyConfig,
    provinceConfig,
    isBigVConfig,
    adTypeConfig,
    attributeTagConfig,
    // custAgeConfig,
    isCarOwnerConfig
  ],
  // 结果数据
  ResultData: [
    dateRangeConfig,
    { ...thisProductBrandConfig, defaultValue: [], clearable: true },
    { ...thisProductSeriesConfig, showSplitLine: true },
    compBrandConfig,
    compSeriesConfig,
    emotionConfig,
    intentConfig,
    dataSourceConfig,
    contentTypeConfig,
    userNicknameConfig,
    userIdConfig,
    originalDataIdConfig,
    originalLinkConfig,
    titleQueryConfig,
    // 结果数据页签复用 rootCause 的“标签体系 + 体验代码 + 标准观点”联动方案。
    experienceCodeLinkageConfig,
    standardViewpointConfig,
    usageScenarioConfig,
    isMainPostConfig,
    genderConfig,
    isWsaterArmyConfig,
    provinceConfig,
    isBigVConfig,
    adTypeConfig,
    attributeTagConfig,
    // custAgeConfig,
    isCarOwnerConfig
  ],
  // 原始数据查询
  OriginalData: [
    dateRangeConfig,
    {
      ...thisProductBrandConfig,
      props: {
        label: 'value',
        value: 'value'
      },
      defaultValue: [],
      clearable: true
    },
    {
      ...thisProductSeriesConfig,
      showSplitLine: true,
      props: {
        label: 'value',
        value: 'value'
      }
    },
    {
      ...compBrandConfig,
      props: {
        ...compBrandConfig.props,
        value: 'value'
      }
    },
    {
      ...compSeriesConfig,
      props: {
        ...compSeriesConfig.props,
        value: 'value'
      }
    },
    dataSourceConfig,
    contentTypeConfig,
    userNicknameConfig,
    userIdConfig,
    originalDataIdConfig,
    originalLinkConfig,
    titleQueryConfig,
    usageScenarioConfig,
    isMainPostConfig,
    genderConfig,
    isWsaterArmyConfig,
    provinceConfig,
    isCarOwnerConfig
  ],
  // 情感分支数据查询：当前先复制原始数据筛选条件，后续可独立调整。
  SentimentBranchData: [
    dateRangeConfig,
    {
      ...thisProductBrandConfig,
      props: {
        label: 'value',
        value: 'value'
      },
      defaultValue: [],
      clearable: true
    },
    {
      ...thisProductSeriesConfig,
      showSplitLine: true,
      props: {
        label: 'value',
        value: 'value'
      }
    },
    {
      ...compBrandConfig,
      props: {
        ...compBrandConfig.props,
        value: 'value'
      }
    },
    {
      ...compSeriesConfig,
      props: {
        ...compSeriesConfig.props,
        value: 'value'
      }
    },
    dataSourceConfig,
    contentTypeConfig,
    userNicknameConfig,
    userIdConfig,
    retweetedNameConfig,
    retweetedUserIdConfig,
    originalDataIdConfig,
    originalLinkConfig,
    titleQueryConfig,
    usageScenarioConfig,
    isMainPostConfig,
    genderConfig,
    isWsaterArmyConfig,
    provinceConfig,
    isBigVConfig,
    isCarOwnerConfig
  ],
  // 热点事件
  hotEvents: [
    brandHotConfig,
    seriesConfig,
    attributeTagConfig,
    dataSourceConfig,
    contentTypeConfig,
    { ...experienceCodeLinkageConfig, label: '体验代码' },
    standardViewpointConfig,
    provinceConfig
  ],
  // 热点事件详情页面 结果数据
  hotDetailEvents: [
    dateRangeHotConfig,
    brandHotConfig,
    seriesConfig,
    dataSourceConfig,
    contentTypeConfig,
    { ...experienceCodeLinkageConfig, label: '体验代码' },
    standardViewpointConfig,
    attributeTagConfig,
    provinceConfig
  ],
  // 热点事件详情页面 原始数据
  hotDetailOriginalEvents: [
    dateRangeHotConfig,
    { ...dataSourceConfig, span: 13 },
    attributeTagConfig
  ],
  // 新车上市
  newCarLaunch: [
    newCarSeriesConfig,
    compareCarSeriesConfig,
    dataSourceConfig,
    contentTypeConfig,
    { ...experienceCodeLinkageConfig, tyPageType: 'newCarPage' },
    standardViewpointConfig,
    isMainPostConfig,
    genderConfig,
    isWsaterArmyConfig,
    provinceConfig,
    isBigVConfig,
    attributeTagConfig,
    isCarOwnerConfig
  ]
}

/**
 * 根据路由名称获取筛选配置
 * @param routeName 路由名称
 * @returns 筛选字段配置数组（深拷贝，避免修改共享配置对象）
 */
export function getFilterConfig(routeName: string): FilterFieldConfig[] {
  const config = filterConfigMap[routeName] || []
  // 深拷贝配置数组，避免修改共享的配置对象
  return cloneDeep(config)
}

// ==================== 格式化相关函数 ====================

/**
 * 递归查找数据源树中的节点名称
 * @param nodes 节点数组
 * @param code 要查找的code
 * @returns 节点名称
 */
export function findChannelNameByCode(nodes: any[], code: string | number): string | null {
  for (const node of nodes) {
    if (String(node.code) === String(code)) {
      return node.name
    }
    if (node.child && Array.isArray(node.child)) {
      const found = findChannelNameByCode(node.child, code)
      if (found) return found
    }
  }
  return null
}

/**
 * 递归查找树中的节点名称
 * @param nodes 节点数组
 * @param codeValue 要查找的code
 * @param key w唯一key属性 默认id
 * @returns 节点名称
 */
export function findTreeNameByKey(
  nodes: any[],
  codeValue: string | number,
  props: any
): string | null {
  const { value = 'id', children = 'children', label = 'name' } = props || {}
  for (const node of nodes) {
    if (String(node[value]) === String(codeValue)) {
      return node[label]
    }
    if (node[children] && Array.isArray(node[children])) {
      const found = findTreeNameByKey(node[children], codeValue, props)
      if (found) return found
    }
  }
  return null
}

/**
 * 格式化日期范围标签
 * @param dateRangeValue 日期范围值
 * @param customTimes 自定义时间数组
 * @returns 日期范围标签文本
 */
export function formatDateRangeTag(dateRangeValue: any, customTimes?: string[]): string | null {
  if (!dateRangeValue) return null

  // 如果是自定义日期
  if (dateRangeValue === 'custom') {
    if (customTimes && Array.isArray(customTimes) && customTimes.length === 2) {
      const [startDate, endDate] = customTimes
      if (startDate && endDate) {
        return `日期范围：${startDate}至${endDate}`
      }
    }
    return null
  }

  // 如果是快捷选项
  const option = FE_TIME_DIMENSION_OPTIONS.find(
    item => String(item.code) === String(dateRangeValue)
  )
  if (option) {
    return `日期范围：${option.name}`
  }

  return null
}

/**
 * 格式化多选字段标签（如数据源）
 * @param field 字段配置
 * @param value 字段值
 * @param channelOptions 数据源选项数组（仅当 field.type === 'dataSource' 时需要）
 * @returns 标签文本
 */
export function formatMultiSelectTag(
  field: FilterFieldConfig,
  value: any,
  channelOptions?: any[]
): string | null {
  if (!value || (Array.isArray(value) && value.length === 0)) {
    return null
  }

  const values = Array.isArray(value) ? value : [value]
  if (values.length === 0) return null

  let labels: string[] = []

  // 数据源类型
  if (field.type === 'dataSource') {
    if (!channelOptions || channelOptions.length === 0) {
      // 如果数据源选项还没加载完成，返回 null
      return null
    }
    labels = values
      .map((code: string | number) => {
        const name = findChannelNameByCode(channelOptions, code)
        return name
      })
      .filter(Boolean) as string[]
  } else if (field.prop === 'keyAccounts') {
    if (!channelOptions || channelOptions.length === 0) {
      // 如果数据源选项还没加载完成，返回 null
      return null
    }
    labels = values
      .map((code: string | number) => {
        const name = findTreeNameByKey(channelOptions, code, field.cascaderProps)
        return name
      })
      .filter(Boolean) as string[]
  }
  // 下拉选择类型
  else if (field.type === 'select' && field.options) {
    const propsLabel = field.props?.label || 'label'
    const propsValue = field.props?.value || 'value'
    labels = values
      .map((val: any) => {
        const option = field.options!.find((opt: any) => String(opt[propsValue]) === String(val))
        return option ? option[propsLabel] : null
      })
      .filter(Boolean)
  }

  if (labels.length === 0) return null

  // 如果超过1个，显示前1个，其余用(+N)表示
  if (labels.length > 1) {
    const displayLabels = labels.slice(0, 1)
    const remainingCount = labels.length - 1
    return `${field.label}: ${displayLabels.join(',')}(+${remainingCount})`
  } else {
    return `${field.label}: ${labels.join(',')}`
  }
}

/**
 * 将标签值格式化为“首项 + (+N)”的统一展示文案
 * @param label 标签名
 * @param labels 标签值列表
 * @returns 格式化后的文案
 */
function formatCollapsedTag(label: string | undefined, labels: string[]): string | null {
  if (!labels.length) return null
  const prefix = label ? `${label}: ` : ''
  if (labels.length > 1) {
    return `${prefix}${labels[0]}(+${labels.length - 1})`
  }
  return `${prefix}${labels[0]}`
}

/**
 * 统一将字段值转换为数组，便于多选/单选逻辑复用
 * @param value 原始字段值
 * @returns 数组格式的值集合
 */
function normalizeFieldValues(value: any): any[] {
  if (Array.isArray(value)) {
    return value.filter(item => item !== '' && item !== null && item !== undefined)
  }
  if (value === '' || value === null || value === undefined) {
    return []
  }
  return [value]
}

/**
 * 根据字段配置匹配品牌节点
 * @param field 字段配置
 * @param value 当前值
 * @returns 匹配到的品牌节点列表
 */
function getMatchedBrands(field: FilterFieldConfig, value: any): any[] {
  const brandOptions = useUserStore().getBrandService || []
  const values = normalizeFieldValues(value)
  const valueKey = field.props?.value || 'key'

  return brandOptions.filter((item: any) =>
    values.some(currentValue =>
      [item[valueKey], item.key, item.value].some(
        candidate => candidate !== undefined && String(candidate) === String(currentValue)
      )
    )
  )
}

/**
 * 根据已选品牌合并可用车系
 * @param config 当前页面配置
 * @param formData 表单数据
 * @returns 去重后的车系列表
 */
function getMergedSeriesOptions(config: FilterFieldConfig[], formData: Record<string, any>): any[] {
  const brandField = config.find(field => field.type === 'brand')
  if (!brandField) {
    return []
  }

  const selectedBrands = getMatchedBrands(brandField, formData[brandField.prop])
  const uniqueSeriesMap = new Map<string, any>()

  selectedBrands.forEach((brand: any) => {
    ;(brand.children || []).forEach((series: any) => {
      const uniqueKey = String(series.key ?? series.value ?? '')
      if (uniqueKey && !uniqueSeriesMap.has(uniqueKey)) {
        uniqueSeriesMap.set(uniqueKey, series)
      }
    })
  })

  return Array.from(uniqueSeriesMap.values())
}

/**
 * 判断候选值是否与当前值匹配
 * @param candidates 候选值集合
 * @param currentValue 当前值
 * @returns 是否匹配
 */
function isMatchedValue(candidates: any[], currentValue: any): boolean {
  return candidates.some(
    candidate => candidate !== undefined && String(candidate) === String(currentValue)
  )
}

/**
 * 根据字段值匹配竞品品牌节点
 * @param field 字段配置
 * @param value 当前值
 * @param competitiveTreeOptions 竞品品牌车系树
 * @returns 匹配到的竞品品牌节点
 */
function getMatchedCompetitiveBrands(
  field: FilterFieldConfig,
  value: any,
  competitiveTreeOptions?: any[]
): any[] {
  const values = normalizeFieldValues(value)
  const valueKey = field.props?.value || 'key'

  return (competitiveTreeOptions || []).filter((item: any) =>
    values.some(currentValue =>
      isMatchedValue([item[valueKey], item.key, item.value, item.code], currentValue)
    )
  )
}

/**
 * 根据已选竞品品牌聚合竞品车系
 * @param config 当前页面配置
 * @param formData 表单数据
 * @param competitiveTreeOptions 竞品品牌车系树
 * @returns 去重后的竞品车系选项
 */
function getMergedCompetitiveSeriesOptions(
  config: FilterFieldConfig[],
  formData: Record<string, any>,
  competitiveTreeOptions?: any[]
): any[] {
  const brandField = config.find(field => field.prop === 'compBrandCodeList')
  const seriesField = config.find(field => field.prop === 'compCarSeriesList')
  if (!seriesField) {
    return []
  }

  const sourceBrands = brandField
    ? getMatchedCompetitiveBrands(brandField, formData[brandField.prop], competitiveTreeOptions)
    : competitiveTreeOptions || []
  const uniqueSeriesMap = new Map<string, any>()
  const seriesValueKey = seriesField.props?.value || 'key'

  sourceBrands.forEach((brand: any) => {
    ;(brand.children || []).forEach((series: any) => {
      const uniqueKey = String(series?.[seriesValueKey] ?? series?.key ?? series?.value ?? '')
      if (uniqueKey && !uniqueSeriesMap.has(uniqueKey)) {
        uniqueSeriesMap.set(uniqueKey, series)
      }
    })
  })

  return Array.from(uniqueSeriesMap.values())
}

/**
 * 根据级联字段值递归查找节点文案
 * @param options 级联选项
 * @param targetValue 目标值
 * @param cascaderProps 级联字段配置
 * @returns 节点文案
 */
function findCascaderLabelByValue(
  options: any[],
  targetValue: string | number | Array<string | number>,
  cascaderProps?: Record<string, any>
): string | null {
  const resolvedTargetValue = Array.isArray(targetValue)
    ? [...targetValue].reverse().find(item => item !== undefined && item !== null && item !== '')
    : targetValue

  if (
    !Array.isArray(options) ||
    options.length === 0 ||
    resolvedTargetValue === undefined ||
    resolvedTargetValue === null ||
    resolvedTargetValue === ''
  ) {
    return null
  }

  const valueKey = cascaderProps?.value || 'key'
  const labelKey = cascaderProps?.label || 'value'
  const childrenKey = cascaderProps?.children || 'children'

  for (const option of options) {
    if (String(option?.[valueKey]) === String(resolvedTargetValue)) {
      return option?.[labelKey] || null
    }

    const children = Array.isArray(option?.[childrenKey]) ? option[childrenKey] : []
    const found = findCascaderLabelByValue(children, targetValue, cascaderProps)
    if (found) {
      return found
    }
  }

  return null
}

/**
 * 格式化所有选中的筛选条件为标签数组
 * @param config 字段配置数组
 * @param formData 表单数据对象
 * @param customTimes 自定义时间数组
 * @param channelOptions 数据源选项数组
 * @param standardViewpointOptions 标准观点选项数组
 * @param competitiveTreeOptions 竞品品牌车系选项数组
 * @param experienceCodeNames 客户体验代码每一级的中文名称数组
 * @param mainAccOptions 重点账号选项数组
 * @returns 标签数组
 */
export function formatFilterTags(
  config: FilterFieldConfig[],
  formData: Record<string, any>,
  customTimes?: string[],
  channelOptions?: any[],
  standardViewpointOptions?: any[],
  competitiveTreeOptions?: any[],
  newCarSeriesOptions?: any[],
  experienceCodeNames?: string[],
  mainAccOptions?: any[]
): string[] {
  const tags: string[] = []

  // 格式化日期范围
  const dateTag = formatDateRangeTag(formData.dateRange, customTimes)
  if (dateTag) {
    tags.push(dateTag)
  }

  // 遍历配置，格式化其他字段
  config.forEach(field => {
    if (field.type === 'placeholder' || field.type === 'daterange') {
      return
    }

    const value = formData[field.prop]
    if (value === null || value === undefined || value === '') {
      return
    }

    // 品牌类型
    if (field.type === 'brand') {
      const labels = getMatchedBrands(field, value)
        .map((item: any) => item.value)
        .filter(Boolean)
      const tag = formatCollapsedTag(field.label, labels)
      if (tag) {
        tags.push(tag)
      }
    }
    // 车系类型（多选）
    else if (field.type === 'series') {
      const seriesOptions = getMergedSeriesOptions(config, formData)
      const seriesValueKey = field.props?.value || 'key'
      const seriesLabels = normalizeFieldValues(value)
        .map((seriesValue: string) => {
          const matchedSeries = seriesOptions.find((item: any) =>
            [item[seriesValueKey], item.key, item.value].some(
              candidate => candidate !== undefined && String(candidate) === String(seriesValue)
            )
          )
          return matchedSeries?.value
        })
        .filter(Boolean)

      const tag = formatCollapsedTag(field.label, seriesLabels)
      if (tag) {
        tags.push(tag)
      }
    }
    // 多选字段（数据源、多选下拉等）
    else if (field.prop === 'keyAccounts' && Array.isArray(value) && value.length > 0) {
      const tag = formatMultiSelectTag(field, value, mainAccOptions)

      if (tag) {
        tags.push(tag)
      }
    }
    // 级联字段（竞品品牌车系、新品车系）
    else if (field.type === 'cascader') {
      let options = field.options || []
      if (field.prop === 'compCarSeriesList') {
        options = competitiveTreeOptions || []
      } else if (field.prop === 'newCarSeriesList') {
        options = newCarSeriesOptions || []
      } else if (field.prop === 'keyAccounts') {
        options = mainAccOptions || []
      }

      const normalizedValues = Array.isArray(value)
        ? value.filter(item => item !== '' && item !== null && item !== undefined)
        : value !== '' && value !== null && value !== undefined
          ? [value]
          : []

      const labels = normalizedValues
        .map(currentValue => findCascaderLabelByValue(options, currentValue, field.cascaderProps))
        .filter(Boolean) as string[]

      const tag = formatCollapsedTag(field.label, Array.from(new Set(labels)))
      if (tag) {
        tags.push(tag)
      }
    }
    // 客户体验代码字段
    else if (field.type === 'experienceCode' || field.type === 'experienceCodeLinkage') {
      if (experienceCodeNames && experienceCodeNames.length > 0) {
        // 如果超过1个，显示前1个，其余用(+N)表示
        if (experienceCodeNames.length > 1) {
          const displayName = experienceCodeNames[0]
          const remainingCount = experienceCodeNames.length - 1
          tags.push(`${field.label}: ${displayName}(+${remainingCount})`)
        } else {
          tags.push(`${field.label}: ${experienceCodeNames[0]}`)
        }
      }
    }
    // 标准观点字段（selectv2类型，使用动态加载的选项）
    else if (
      field.type === 'selectv2' &&
      field.prop === 'compBrandCodeList' &&
      Array.isArray(value) &&
      value.length > 0 &&
      competitiveTreeOptions &&
      competitiveTreeOptions.length > 0
    ) {
      const labels = getMatchedCompetitiveBrands(field, value, competitiveTreeOptions)
        .map((item: any) => item.value)
        .filter(Boolean)

      const tag = formatCollapsedTag(field.label, labels)
      if (tag) {
        tags.push(tag)
      }
    }
    // 竞品车系字段（selectv2 类型，选项依赖竞品品牌联动）
    else if (
      field.type === 'selectv2' &&
      field.prop === 'compCarSeriesList' &&
      Array.isArray(value) &&
      value.length > 0 &&
      competitiveTreeOptions &&
      competitiveTreeOptions.length > 0
    ) {
      const seriesOptions = getMergedCompetitiveSeriesOptions(
        config,
        formData,
        competitiveTreeOptions
      )
      const seriesValueKey = field.props?.value || 'key'
      const labels = normalizeFieldValues(value)
        .map((seriesValue: string) => {
          const matchedSeries = seriesOptions.find((item: any) =>
            isMatchedValue([item[seriesValueKey], item.key, item.value], seriesValue)
          )
          return matchedSeries?.value
        })
        .filter(Boolean)

      const tag = formatCollapsedTag(field.label, labels)
      if (tag) {
        tags.push(tag)
      }
    }
    // 标准观点字段（selectv2类型，使用动态加载的选项）
    else if (
      field.type === 'selectv2' &&
      field.prop === 'topicCodes' &&
      Array.isArray(value) &&
      value.length > 0 &&
      standardViewpointOptions &&
      standardViewpointOptions.length > 0
    ) {
      const values = Array.isArray(value) ? value : [value]
      const labels = values
        .map((val: any) => {
          const option = standardViewpointOptions.find(
            (opt: any) => String(opt.tagCode) === String(val)
          )
          return option ? option.tagName : null
        })
        .filter(Boolean)

      const tag = formatCollapsedTag(field.label, labels)
      if (tag) {
        tags.push(tag)
      }
    }
    // 多选字段（数据源、多选下拉等）
    else if (
      (field.type === 'dataSource' || (field.type === 'select' && field.multiple)) &&
      Array.isArray(value) &&
      value.length > 0
    ) {
      const tag = formatMultiSelectTag(field, value, channelOptions)
      if (tag) {
        tags.push(tag)
      }
    }
    // 按钮开关组（多选模式）
    else if (
      field.type === 'btnSwitch' &&
      field.multiple &&
      Array.isArray(value) &&
      value.length > 0
    ) {
      const propsLabel = field.props?.label || 'text'
      const propsValue = field.props?.value || 'value'
      const labels = value
        .map((val: any) => {
          const option = field.options?.find((opt: any) => String(opt[propsValue]) === String(val))
          return option ? option[propsLabel] : null
        })
        .filter(Boolean)

      const tag = formatCollapsedTag(field.label, labels)
      if (tag) {
        tags.push(tag)
      }
    }
    // 按钮开关组（单选模式）
    else if (field.type === 'btnSwitch' && !field.multiple && field.options) {
      const propsLabel = field.props?.label || 'text'
      const propsValue = field.props?.value || 'value'
      const option = field.options.find((opt: any) => String(opt[propsValue]) === String(value))
      if (option) {
        tags.push(`${field.label}: ${option[propsLabel]}`)
      }
    }
    // 单选下拉
    else if (field.type === 'select' && !field.multiple && field.options) {
      const propsLabel = field.props?.label || 'label'
      const propsValue = field.props?.value || 'value'
      const option = field.options.find((opt: any) => String(opt[propsValue]) === String(value))
      if (option) {
        tags.push(`${field.label}: ${option[propsLabel]}`)
      }
    }
    // 输入框
    else if (field.type === 'input' && value) {
      tags.push(`${field.label}: ${value}`)
    }
  })

  return tags
}
