import { reactive } from 'vue'
import { findAllResourceTree, getChannelTree, getTagLibClientTree } from '@/api/rules'
import { findAllAttributeLabelList } from '@/api/attributeLabel'
import {
  findBatchConditionConfig,
  findBatchIndicatorConditionConfig,
  getBatchProvinceList,
  type BatchProvinceListItem
} from '@/api/batchEventRules'
import useConditions from '@/hooks/useConditions'
import useUserStore from '@/stores/modules/user'
import type { BatchCascaderOption, BatchDimensionDefinition, BatchSelectOption } from './types'
import {
  buildMetricTypeKey,
  buildMetricUnitKey,
  getBatchMetricTypeOptions,
  getBatchMetricUnit,
  getBatchMetricValueTypeOptions,
  getBatchMetricWildcardOptions
} from './metric'

/**
 * 批量事件模块的独立状态管理
 * 包含批量规则专用的字典数据、表单配置和公共资源树
 */

// 批量规则字典接口地址
const BATCH_CONDITION_URL = '/insights/insBatchRule/conditions'

/**
 * 去重选项
 */
const uniqueOptions = (options: BatchSelectOption[]) => {
  return Array.from(
    new Map(options.filter(item => item.value).map(item => [item.value, item])).values()
  )
}

/**
 * 同一“指标 + 指标类型 + 值类型”可能被多个操作符复用，格式以接口首个有效值为准。
 */
const setMetricValueFormat = (map: Record<string, string>, key: string, valueFormat: unknown) => {
  const normalizedValueFormat = String(valueFormat || '').trim()
  if (!key || !normalizedValueFormat || map[key]) return
  map[key] = normalizedValueFormat
}

export const batchEventStore = reactive<{
  // 批量规则字典数据
  batchConditions: Record<string, any>
  dictsLoaded: boolean
  // 渠道树
  channelTree: any[]
  channelTreeLoaded: boolean
  // 词库资源信息
  dataResource: {
    loaded: boolean
    loading: boolean
    allList: any[]
    accountList: any[]
    ruleList: any[]
  }
  // 维度异步选项资源
  dimensionOptionResources: {
    provinceOptions: BatchSelectOption[]
    provinceOptionsLoaded: boolean
    provinceOptionsLoading: boolean
    attributeLabelOptions: BatchSelectOption[]
    attributeLabelOptionsLoaded: boolean
    attributeLabelOptionsLoading: boolean
  }
  // 体验代码标签树
  tagLibOptions: any[]
  tagLibLoaded: boolean
  // 部门员工树
  deptEmployeeTree: any[]
  deptEmployeeTreeLoaded: boolean
  // 表单选项配置（维度定义、指标选项等）
  formOptions: {
    loaded: boolean
    dimensionDefinitions: BatchDimensionDefinition[]
    metricFieldOptions: BatchSelectOption[]
    metricTypeMap: Record<string, BatchSelectOption[]>
    metricWildcardMap: Record<string, BatchSelectOption[]>
    metricValueTypeMap: Record<string, BatchSelectOption[]>
    metricUnitMap: Record<string, string>
  }
  // 资源是否已初始化
  resourceInitialized: boolean
}>({
  batchConditions: {},
  dictsLoaded: false,
  channelTree: [],
  channelTreeLoaded: false,
  dataResource: {
    loaded: false,
    loading: false,
    allList: [],
    accountList: [],
    ruleList: []
  },
  dimensionOptionResources: {
    provinceOptions: [],
    provinceOptionsLoaded: false,
    provinceOptionsLoading: false,
    attributeLabelOptions: [],
    attributeLabelOptionsLoaded: false,
    attributeLabelOptionsLoading: false
  },
  tagLibOptions: [],
  tagLibLoaded: false,
  deptEmployeeTree: [],
  deptEmployeeTreeLoaded: false,
  formOptions: {
    loaded: false,
    dimensionDefinitions: [],
    metricFieldOptions: [],
    metricTypeMap: {},
    metricWildcardMap: {},
    metricValueTypeMap: {},
    metricUnitMap: {}
  },
  resourceInitialized: false
})

export const batchEventActions = {
  /**
   * 加载批量规则字典
   */
  async updateDicts() {
    try {
      // store 里显式请求字典时关闭自动拉取，避免一次初始化触发两次同源接口。
      const { conditions, getConditions } = useConditions({
        url: BATCH_CONDITION_URL,
        immediate: false
      })
      await getConditions()
      batchEventStore.batchConditions = conditions || {}
      batchEventStore.dictsLoaded = true
      return true
    } catch (e) {
      batchEventStore.dictsLoaded = false
      console.error('获取批量规则字典失败', e)
      return false
    }
  },

  /**
   * 加载渠道树
   */
  async updateChannelTree() {
    try {
      const response = await getChannelTree()
      batchEventStore.channelTree = response.result || []
      batchEventStore.channelTreeLoaded = true
      return true
    } catch (e) {
      batchEventStore.channelTreeLoaded = false
      console.error('获取渠道树失败', e)
      return false
    }
  },

  /**
   * 加载资源树（账号、规则词库）
   */
  async updateAllResourceTree() {
    try {
      batchEventStore.dataResource.loading = true
      const response = await findAllResourceTree({ typeList: ['account', 'rule'] })
      if (!response.success) {
        batchEventStore.dataResource.allList = []
        batchEventStore.dataResource.accountList = []
        batchEventStore.dataResource.ruleList = []
        batchEventStore.dataResource.loaded = false
        return false
      }

      batchEventStore.dataResource.allList = response.result || []
      batchEventStore.dataResource.accountList = (response.result || []).filter(
        (item: any) => item.type === 'account'
      )
      batchEventStore.dataResource.ruleList = (response.result || []).filter(
        (item: any) => item.type === 'rule'
      )
      batchEventStore.dataResource.loaded = true
      return true
    } catch (error) {
      batchEventStore.dataResource.loaded = false
      console.error('获取资源树失败', error)
      return false
    } finally {
      batchEventStore.dataResource.loading = false
    }
  },

  /**
   * 加载体验代码树
   */
  async updateTagLibClientTree() {
    if (batchEventStore.tagLibLoaded) return true
    try {
      const response = await getTagLibClientTree({
        // 标准观点已合入体验代码树，表单统一取 5 级 CA 树，避免再拆分两套数据源。
        level: '5',
        tagType: 'CA'
      })
      if (!response.success) {
        batchEventStore.tagLibOptions = []
        batchEventStore.tagLibLoaded = false
        return false
      }

      const secondLevel: any[] = []
      const result = response.result || []
      result.forEach((node: any) => {
        const children = (node?.child || []) as any[]
        if (Array.isArray(children) && children.length > 0) {
          secondLevel.push(...children)
        }
      })
      batchEventStore.tagLibOptions = secondLevel
      batchEventStore.tagLibLoaded = true
      return true
    } catch (e) {
      batchEventStore.tagLibLoaded = false
      console.error('获取体验代码树失败', e)
      return false
    }
  },

  /**
   * 加载部门员工树
   */
  async updateDeptEmployeeTree() {
    try {
      if (!batchEventStore.deptEmployeeTree.length) {
        const userStore = useUserStore()
        const list = await userStore.getDepartAccountTree({ silent: true })
        batchEventStore.deptEmployeeTree = Array.isArray(list) ? list : []
      }
      batchEventStore.deptEmployeeTreeLoaded = true
      return true
    } catch (e) {
      batchEventStore.deptEmployeeTreeLoaded = false
      console.error('获取部门员工树失败', e)
      return false
    }
  },

  /**
   * 省份维度选项统一缓存到 store，避免弹窗重挂后重复请求。
   */
  async updateProvinceOptions() {
    const resource = batchEventStore.dimensionOptionResources

    if (resource.provinceOptionsLoaded || resource.provinceOptionsLoading) {
      return true
    }

    resource.provinceOptionsLoading = true

    try {
      const { result } = await getBatchProvinceList()
      resource.provinceOptions = Array.isArray(result)
        ? result.reduce<BatchSelectOption[]>((options, item: BatchProvinceListItem) => {
            const value = String(item.provinceCode || '').trim()
            const label = String(item.provinceName || '').trim()

            if (!value || !label) {
              return options
            }

            options.push({
              label,
              value
            })

            return options
          }, [])
        : []
      resource.provinceOptionsLoaded = true
      return true
    } catch (error) {
      resource.provinceOptions = []
      resource.provinceOptionsLoaded = false
      console.error('获取省份列表失败', error)
      return false
    } finally {
      resource.provinceOptionsLoading = false
    }
  },

  /**
   * 属性标签维度选项统一缓存到 store，避免组件内重复维护加载状态。
   */
  async updateAttributeLabelOptions() {
    const resource = batchEventStore.dimensionOptionResources

    if (resource.attributeLabelOptionsLoaded || resource.attributeLabelOptionsLoading) {
      return true
    }

    resource.attributeLabelOptionsLoading = true

    try {
      const response: any = await findAllAttributeLabelList({})
      const list = Array.isArray(response?.result) ? response.result : []
      const nextOptions: BatchSelectOption[] = []

      list.forEach((item: any) => {
        const value = String(item?.id || '').trim()
        const label = String(item?.name || '').trim()

        if (!value || !label) {
          return
        }

        nextOptions.push({
          label,
          value
        })
      })

      resource.attributeLabelOptions = nextOptions
      resource.attributeLabelOptionsLoaded = true
      return true
    } catch (error) {
      resource.attributeLabelOptions = []
      resource.attributeLabelOptionsLoaded = false
      console.error('获取属性标签列表失败', error)
      return false
    } finally {
      resource.attributeLabelOptionsLoading = false
    }
  },

  /**
   * 加载表单选项配置（维度定义、指标选项）
   * 列表页初始化时调用，表单直接复用
   */
  async updateFormOptions() {
    if (batchEventStore.formOptions.loaded) return true

    try {
      const [conditionResp, indicatorResp] = await Promise.all([
        findBatchConditionConfig(),
        findBatchIndicatorConditionConfig()
      ])

      // 解析维度定义
      const dimensionDefinitions = mapDimensionDefinitions(conditionResp.result || [])

      // 解析指标选项
      const metricBundle = buildMetricOptionBundle(indicatorResp.result || [])

      batchEventStore.formOptions = {
        loaded: true,
        dimensionDefinitions,
        ...metricBundle
      }
      return true
    } catch (e) {
      console.error('加载表单配置失败', e)
      return false
    }
  },

  /**
   * 初始化页面所需的所有资源（列表页调用一次，表单复用）
   */
  async initPageResources() {
    if (batchEventStore.resourceInitialized) return true

    /**
     * 只有页面初始化依赖的核心资源全部加载成功后，才标记为“已初始化”。
     * 这样可以避免首屏偶发失败后被永久缓存成失败状态，后续弹窗也失去重试机会。
     */
    const results = await Promise.all([
      this.updateDicts(),
      this.updateChannelTree(),
      this.updateAllResourceTree(),
      this.updateDeptEmployeeTree(),
      this.updateFormOptions()
    ])

    batchEventStore.resourceInitialized = results.every(Boolean)
    return batchEventStore.resourceInitialized
  },

  /**
   * 初始化表单所需资源（弹窗打开时调用）
   * 如果列表页已初始化，则只加载体验代码树
   */
  async initFormResources() {
    const resourceTasks: Array<Promise<boolean>> = []

    if (!batchEventStore.dictsLoaded) {
      resourceTasks.push(this.updateDicts())
    }

    if (!batchEventStore.channelTreeLoaded) {
      resourceTasks.push(this.updateChannelTree())
    }

    if (!batchEventStore.dataResource.loaded) {
      resourceTasks.push(this.updateAllResourceTree())
    }

    if (!batchEventStore.deptEmployeeTreeLoaded) {
      resourceTasks.push(this.updateDeptEmployeeTree())
    }

    if (!batchEventStore.formOptions.loaded) {
      resourceTasks.push(this.updateFormOptions())
    }

    const resourceResults = await Promise.all(resourceTasks)

    if (resourceResults.some(item => item === false)) {
      throw new Error('加载表单基础资源失败')
    }

    const isTagLibReady = await this.updateTagLibClientTree()
    if (!isTagLibReady) {
      throw new Error('加载体验代码树失败')
    }
  }
}

/**
 * 映射维度定义
 */
const mapDimensionDefinitions = (items: any[] = []): BatchDimensionDefinition[] => {
  return items.map(item => ({
    name: String(item.name || ''),
    code: String(item.code || ''),
    logicalOperator: (item.logicalOperator || []).map((opt: any) => ({
      name: String(opt.name || ''),
      code: String(opt.code || '')
    })),
    condition: (item.condition || []).map((opt: any) => ({
      name: String(opt.name || ''),
      code: String(opt.code || '')
    })),
    countingMethod: (item.countingMethod || []).map((opt: any) => ({
      name: String(opt.name || ''),
      code: String(opt.code || '')
    }))
  }))
}

/**
 * 构建指标选项包
 */
const buildMetricOptionBundle = (items: any[] = []) => {
  const result = {
    metricFieldOptions: [] as BatchSelectOption[],
    metricTypeMap: {} as Record<string, BatchSelectOption[]>,
    metricWildcardMap: {} as Record<string, BatchSelectOption[]>,
    metricValueTypeMap: {} as Record<string, BatchSelectOption[]>,
    metricUnitMap: {} as Record<string, string>
  }

  items.forEach(item => {
    const indicatorCode = String(item.code || '')
    pushOption(result.metricFieldOptions, item.name, indicatorCode)
    ;(item.types || []).forEach((typeItem: any) => {
      const indicatorTypeCode = String(typeItem.code || '')
      if (!indicatorTypeCode) return

      const typeKey = buildMetricTypeKey(indicatorCode, indicatorTypeCode)
      appendMapOption(result.metricTypeMap, indicatorCode, typeItem.name, indicatorTypeCode)
      ;(typeItem.conditions || []).forEach((condition: any) => {
        const operatorCode = String(condition.operatorCode || '')
        const valueTypeCode = String(condition.valueTypeCode || '')

        appendMapOption(result.metricWildcardMap, typeKey, condition.operatorName, operatorCode)
        appendMapOption(result.metricValueTypeMap, typeKey, condition.valueTypeName, valueTypeCode)

        if (valueTypeCode) {
          setMetricValueFormat(
            result.metricUnitMap,
            buildMetricUnitKey(indicatorCode, indicatorTypeCode, valueTypeCode),
            condition.valueFormat
          )
        }
      })
    })
  })

  return {
    metricFieldOptions: uniqueOptions(result.metricFieldOptions),
    metricTypeMap: mapValuesUnique(result.metricTypeMap),
    metricWildcardMap: mapValuesUnique(result.metricWildcardMap),
    metricValueTypeMap: mapValuesUnique(result.metricValueTypeMap),
    metricUnitMap: result.metricUnitMap
  }
}

const pushOption = (options: BatchSelectOption[], label: unknown, value: unknown) => {
  const normalizedValue = String(value || '')
  if (!normalizedValue) return
  options.push({
    label: String(label || ''),
    value: normalizedValue
  })
}

const appendMapOption = (
  map: Record<string, BatchSelectOption[]>,
  key: string,
  label: unknown,
  value: unknown
) => {
  if (!key) return
  const list = map[key] || []
  pushOption(list, label, value)
  map[key] = list
}

const mapValuesUnique = (map: Record<string, BatchSelectOption[]>) => {
  return Object.fromEntries(
    Object.entries(map).map(([key, options]) => [key, uniqueOptions(options)])
  )
}

/**
 * 获取指标类型选项
 */
export const getMetricTypeOptions = (indicator: string) => {
  return getBatchMetricTypeOptions(batchEventStore.formOptions, indicator)
}

/**
 * 获取指标操作符选项
 */
export const getMetricWildcardOptions = (indicator: string, indicatorType: string) => {
  return getBatchMetricWildcardOptions(batchEventStore.formOptions, indicator, indicatorType)
}

/**
 * 获取指标值类型选项
 */
export const getMetricValueTypeOptions = (indicator: string, indicatorType: string) => {
  return getBatchMetricValueTypeOptions(batchEventStore.formOptions, indicator, indicatorType)
}

/**
 * 获取指标单位
 */
export const getMetricUnit = (indicator: string, indicatorType: string, valueType: string) => {
  return getBatchMetricUnit(batchEventStore.formOptions, indicator, indicatorType, valueType)
}

/**
 * 映射渠道树为级联选项
 */
export const mapChannelTreeOptions = (nodes: any[] = []): BatchCascaderOption[] => {
  return (nodes || []).map(item => ({
    label: item.name,
    value: item.code,
    children: Array.isArray(item.child) ? mapChannelTreeOptions(item.child) : undefined
  }))
}

/**
 * 映射体验代码树为级联选项
 */
export const mapTagTreeOptions = (nodes: any[] = []): BatchCascaderOption[] => {
  return (nodes || []).map(item => ({
    label: item.tagName,
    value: item.tagCode,
    children: Array.isArray(item.child) ? mapTagTreeOptions(item.child) : undefined
  }))
}

/**
 * 映射字典树为级联选项
 */
export const mapDictTreeOptions = (nodes: any[] = []): BatchCascaderOption[] => {
  return (nodes || []).map(item => ({
    label: item.value,
    value: item.key,
    children: Array.isArray(item.children) ? mapDictTreeOptions(item.children) : undefined
  }))
}
