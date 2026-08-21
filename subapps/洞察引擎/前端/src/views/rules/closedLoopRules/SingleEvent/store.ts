import { reactive, computed } from 'vue'
import { findAllResourceTree, getChannelTree, getTagLibClientTree } from '@/api/rules'
import useConditions from '@/hooks/useConditions'
import useUserStore from '@/stores/modules/user'

/**
 * 单点事件模块的独立状态管理
 * 包含单点规则专用的字典数据和公共资源树
 */

// 单点规则字典接口地址
const SINGLE_CONDITION_URL = '/insights/insClosedRule/conditions'

export const singleEventStore = reactive<{
  // 单点规则字典数据
  conditions: Record<string, any>
  // 渠道树
  channelTree: any[]
  // 词库资源信息
  dataResource: {
    loading: boolean
    allList: any[]
    accountList: any[]
    ruleList: any[]
  }
  // 体验代码标签树（多层级结构）
  tagLibOptions: any[]
  tagLibOptionsMap: Record<string, any>
  // 部门树
  departOptions: any[]
  // 部门员工树
  deptEmployeeTree: any[]
  // 资源是否已初始化（避免重复请求）
  resourceInitialized: boolean
}>({
  conditions: {},
  channelTree: [],
  dataResource: {
    loading: false,
    allList: [],
    accountList: [],
    ruleList: []
  },
  tagLibOptions: [],
  tagLibOptionsMap: {},
  departOptions: [],
  deptEmployeeTree: [],
  resourceInitialized: false
})

export const singleEventActions = {
  /**
   * 加载单点规则字典
   */
  async updateDicts() {
    try {
      // store 里会手动等待字典返回，这里关闭自动拉取，避免页面初始化时重复请求。
      const { conditions, getConditions } = useConditions({
        url: SINGLE_CONDITION_URL,
        immediate: false
      })
      await getConditions()
      singleEventStore.conditions = conditions || {}
    } catch (e) {
      console.error('获取单点规则字典失败', e)
    }
  },

  /**
   * 加载渠道树
   */
  async updateChannelTree() {
    try {
      const response = await getChannelTree()
      singleEventStore.channelTree = response.result || []
    } catch (e) {
      console.error('获取渠道树失败', e)
    }
  },

  /**
   * 加载资源树（账号、规则词库）
   */
  async updateAllResourceTree() {
    try {
      singleEventStore.dataResource.loading = true
      const response = await findAllResourceTree({ typeList: ['account', 'rule'] })
      if (response.success) {
        singleEventStore.dataResource.allList = response.result || []
      }
      singleEventStore.dataResource.accountList = (response.result || []).filter(
        (item: any) => item.type === 'account'
      )
      singleEventStore.dataResource.ruleList = (response.result || []).filter(
        (item: any) => item.type === 'rule'
      )
    } catch (error) {
      console.error('获取资源树失败', error)
    } finally {
      singleEventStore.dataResource.loading = false
    }
  },

  /**
   * 加载体验代码树
   */
  async updateTagLibClientTree() {
    if (singleEventStore.tagLibOptions.length) return
    try {
      const response = await getTagLibClientTree({
        level: '4',
        tagAttribute: 'Category',
        tagType: 'CA'
      })
      if (response.success) {
        // 从第二层开始构建展示树
        const secondLevel: any[] = []
        const result = response.result || []
        result.forEach((node: any) => {
          const children = (node?.child || []) as any[]
          if (Array.isArray(children) && children.length > 0) {
            secondLevel.push(...children)
          }
        })
        singleEventStore.tagLibOptions = secondLevel
        singleEventStore.tagLibOptionsMap = flattenTagLibOptions(singleEventStore.tagLibOptions)
      }
    } catch (e) {
      console.error('获取体验代码树失败', e)
    }
  },

  /**
   * 加载部门列表
   */
  async updateDepartList() {
    if (singleEventStore.departOptions.length) return
    try {
      const userStore = useUserStore()
      const list = await userStore.getDepartAccountTree({ silent: true })
      singleEventStore.departOptions = Array.isArray(list) ? list : []
    } catch (error: any) {
      console.error('获取部门列表失败', error)
    }
  },

  /**
   * 加载部门员工树
   */
  async updateDeptEmployeeTree() {
    if (!singleEventStore.deptEmployeeTree.length) {
      const userStore = useUserStore()
      const list = await userStore.getDepartAccountTree({ silent: true })
      singleEventStore.deptEmployeeTree = Array.isArray(list) ? list : []
    }
    return singleEventStore.deptEmployeeTree
  },

  /**
   * 初始化页面所需的所有资源（列表页调用一次，表单复用）
   */
  async initPageResources() {
    if (singleEventStore.resourceInitialized) return

    await Promise.all([
      this.updateDicts(),
      this.updateChannelTree(),
      this.updateAllResourceTree(),
      this.updateDeptEmployeeTree()
    ])

    singleEventStore.resourceInitialized = true
  }
}

/**
 * 根据字典值获取字典名称
 */
export const getNameByDictValue = (dict: any[], value: any) => {
  return dict?.find(item => item.key === value)?.value || ''
}

/**
 * 单点规则类型默认值
 */
export const singleRuleTypeValue = computed(() => {
  return singleEventStore.conditions.closedRuleType?.[0]?.key || ''
})

/**
 * 启用状态默认值
 */
export const enabledStatusValue = computed(() => {
  return singleEventStore.conditions.closedRuleEnabledStatus?.[0]?.key || ''
})

/**
 * 扁平化体验代码树为 Map
 */
const flattenTagLibOptions = (nodes: any[]): Record<string, any> => {
  const resultMap: Record<string, any> = {}

  const flatten = (items: any[]) => {
    items.forEach(node => {
      if (node.tagCode) {
        resultMap[node.tagCode] = node
      }
      if (node.child && Array.isArray(node.child) && node.child.length > 0) {
        flatten(node.child)
      }
    })
  }

  flatten(nodes)
  return resultMap
}
