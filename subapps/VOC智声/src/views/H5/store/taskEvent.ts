/**
 * H5 任务事件相关 Store
 * - 统一管理“当前登录人是否存在关联事件”标记
 * - 缓存接口结果，避免在单次会话内重复发起相同请求
 */

import { defineStore } from 'pinia'
import {
  getTaskEventConditions,
  getUserRelatedEvents,
  getSysAllDictItems,
  getDepartAccountTree,
  getSingleEventTopicsBatch
} from '../api/taskEvent'
import { getBatchEventRuleCategoryTree } from '../api/batchEvent'
import type { SingleEventTopicItem } from '../api/taskEvent'
import type { BatchEventRuleCategoryVo } from '../api/batchEvent'

let departAccountTreePending: Promise<any[]> | null = null
let batchRuleCategoryTreePending: Promise<BatchEventRuleCategoryVo[]> | null = null
const topicsBatchPendingMap = new Map<string, Promise<SingleEventTopicItem[]>>()

// 关联事件项类型，具体字段按后端接口约定，后续可在此补充
export interface UserRelatedEventItem {
  [key: string]: any
}

// 筛选条件类型：字段由后端返回决定，这里先以 any 承接，后续可逐步收敛到明确类型
export type TaskEventConditions = any

interface TaskEventState {
  // 是否存在关联事件
  hasUserEvents: boolean
  // 当前登录人的关联事件集合
  events: UserRelatedEventItem[]
  // 请求进行中标记
  loading: boolean

  // 单点事件筛选条件
  conditions: TaskEventConditions | null
  // 筛选条件请求进行中标记
  conditionsLoading: boolean

  // 部门-人员树（处理人员筛选）
  departAccountTree: any[]
  // 部门-人员树请求状态
  departAccountTreeLoading: boolean
  // 部门-人员树最近一次请求是否失败（用于组件侧做“打开即重试”）
  departAccountTreeFetchFailed: boolean
  // 部门-人员树最近一次失败原因（用于排查问题）
  departAccountTreeErrorMessage: string

  // 批量事件规则主题分类树
  batchRuleCategoryTree: BatchEventRuleCategoryVo[]
  // 批量事件规则主题分类树请求状态
  batchRuleCategoryTreeLoading: boolean

  // 数据字典
  allDictItems: any
  // 数据字典请求状态：用于避免重复请求
  allDictItemsLoading: boolean

  // 标准观点（topics-batch）缓存：key=体验代码最末级 codes 组合
  topicsBatchCache: Record<string, SingleEventTopicItem[]>
  // 标准观点加载状态：key=体验代码最末级 codes 组合
  topicsBatchLoadingMap: Record<string, boolean>
}

export const useTaskEventStore = defineStore('h5-task-event', {
  state: (): TaskEventState => ({
    //当前登录人关联事件信息
    hasUserEvents: false,
    events: [],
    loading: false,

    // 筛选条件
    conditions: null,
    conditionsLoading: false,

    // 部门-人员树
    departAccountTree: [],
    departAccountTreeLoading: false,
    departAccountTreeFetchFailed: false,
    departAccountTreeErrorMessage: '',

    // 批量事件主题分类树
    batchRuleCategoryTree: [],
    batchRuleCategoryTreeLoading: false,

    //数据字典
    allDictItems: null,
    // 数据字典请求状态：用于避免重复请求
    allDictItemsLoading: false,

    // 标准观点缓存与加载态
    topicsBatchCache: {},
    topicsBatchLoadingMap: {}
  }),

  actions: {
    /**
     * 标准观点（topics-batch）codes 归一化
     * - 去空、去重、排序：保证 cacheKey 稳定，提高缓存命中
     */
    normalizeTopicsBatchCodes(codes: string[]): string[] {
      const raw = Array.isArray(codes) ? codes : []
      const list = raw.map(it => String(it || '').trim()).filter(Boolean)
      const unique = Array.from(new Set(list))
      unique.sort()
      return unique
    },

    /**
     * 计算 topics-batch 缓存 key
     */
    getTopicsBatchKey(codes: string[]): string {
      return this.normalizeTopicsBatchCodes(codes).join(',')
    },

    /**
     * 读取 topics-batch 缓存
     */
    getTopicsBatchOptionsByKey(key: string): SingleEventTopicItem[] {
      const k = String(key || '')
      return Array.isArray(this.topicsBatchCache?.[k]) ? this.topicsBatchCache[k] : []
    },

    /**
     * 判断 topics-batch 是否在加载中
     */
    isTopicsBatchLoadingByKey(key: string): boolean {
      const k = String(key || '')
      return !!this.topicsBatchLoadingMap?.[k]
    },

    /**
     * 拉取标准观点（topics-batch）
     * - 由 Store 负责缓存与并发去重
     * @param codes 体验代码最末级 codes（1-3级单选：传 [code]；4级多选：传 codes 数组）
     * @param force 是否强制刷新
     */
    async fetchTopicsBatchByCodes(codes: string[], force = false): Promise<SingleEventTopicItem[]> {
      const normalizedCodes = this.normalizeTopicsBatchCodes(codes)
      if (normalizedCodes.length === 0) return []

      const key = normalizedCodes.join(',')
      if (!force && Array.isArray(this.topicsBatchCache?.[key])) {
        return this.topicsBatchCache[key]
      }

      const pending = topicsBatchPendingMap.get(key)
      if (pending) return pending

      this.topicsBatchLoadingMap[key] = true

      const promise = (async () => {
        try {
          const res = await getSingleEventTopicsBatch(normalizedCodes)
          if (res?.success && Array.isArray(res?.result)) {
            this.topicsBatchCache[key] = res.result
            return res.result
          }

          if (res?.success && !Array.isArray(res?.result)) {
            console.error('获取标准观点失败：result 非数组')
          } else {
            console.error('获取标准观点失败：', (res as any)?.message || '接口返回异常')
          }

          this.topicsBatchCache[key] = []
          return []
        } catch (error) {
          console.error('获取标准观点异常：', error)
          return Array.isArray(this.topicsBatchCache?.[key]) ? this.topicsBatchCache[key] : []
        } finally {
          this.topicsBatchLoadingMap[key] = false
          topicsBatchPendingMap.delete(key)
        }
      })()

      topicsBatchPendingMap.set(key, promise)
      return promise
    },

    /**
     * 清理标准观点缓存
     */
    resetTopicsBatchCache() {
      this.topicsBatchCache = {}
      this.topicsBatchLoadingMap = {}
      topicsBatchPendingMap.clear()
    },

    /**
     * 拉取批量事件规则主题分类树。
     * - PC/H5 批量事件筛选共用报表服务树形分类接口
     * - Store 内做缓存与并发去重，避免每次打开筛选弹层重复请求
     * @param force 是否强制刷新
     * @returns 批量事件规则主题分类树
     */
    async fetchBatchRuleCategoryTree(force = false): Promise<BatchEventRuleCategoryVo[]> {
      if (
        !force &&
        Array.isArray(this.batchRuleCategoryTree) &&
        this.batchRuleCategoryTree.length > 0
      ) {
        return this.batchRuleCategoryTree
      }

      if (batchRuleCategoryTreePending) {
        return batchRuleCategoryTreePending
      }

      this.batchRuleCategoryTreeLoading = true
      batchRuleCategoryTreePending = (async () => {
        try {
          const res = await getBatchEventRuleCategoryTree()
          if ((res as any)?.success) {
            const list = (res as any)?.result ?? []
            this.batchRuleCategoryTree = Array.isArray(list) ? list : []
            return this.batchRuleCategoryTree
          }

          console.error('获取批量事件主题分类树失败：', (res as any)?.message || '接口返回异常')
          this.batchRuleCategoryTree = []
          return []
        } catch (error) {
          console.error('获取批量事件主题分类树异常：', error)
          return Array.isArray(this.batchRuleCategoryTree) ? this.batchRuleCategoryTree : []
        } finally {
          this.batchRuleCategoryTreeLoading = false
          batchRuleCategoryTreePending = null
        }
      })()

      return batchRuleCategoryTreePending
    },

    /**
     * 拉取当前登录人关联事件信息
     * - 正常场景下仅在首次进入 H5 任务相关页面时调用一次
     * - 后续可通过 force=true 强制刷新
     * @param force 是否强制刷新
     * @returns 是否存在关联事件
     */
    async fetchUserEvents(force = false): Promise<boolean> {
      // 已初始化且不强制刷新时，直接返回已有标记，避免重复请求
      if (!force && this.hasUserEvents) {
        return this.hasUserEvents
      }

      this.loading = true
      try {
        const res = await getUserRelatedEvents()
        const list = (res as any)?.result ?? []
        this.hasUserEvents = !!list
        return this.hasUserEvents
      } catch (error) {
        // 接口异常时仅记录错误日志，不影响其他功能
        console.error('获取当前登录人关联事件信息失败:', error)
        this.events = []
        this.hasUserEvents = false
        return false
      } finally {
        this.loading = false
      }
    },

    /**
     * 拉取单点事件筛选条件
     * - 下沉到 Store 做缓存与并发去重，避免组件频繁触发接口
     * - 正常场景下仅需在组件渲染/挂载时调用一次
     * @param force 是否强制刷新
     */
    async fetchTaskEventConditions(force = false): Promise<TaskEventConditions | null> {
      // 已发起过请求且不强制刷新时，直接返回缓存结果
      const hasCachedConditions =
        this.conditions != null &&
        (Array.isArray(this.conditions)
          ? this.conditions.length > 0
          : typeof this.conditions === 'object' && Object.keys(this.conditions as any).length > 0)
      if (!force && hasCachedConditions) {
        return this.conditions
      }

      this.conditionsLoading = true
      const taskEventConditionsPromise = (async () => {
        try {
          const res = await getTaskEventConditions()
          if ((res as any)?.success) {
            this.conditions = (res as any)?.result ?? null
            return this.conditions
          }

          console.error('获取单点事件筛选条件失败：', (res as any)?.message)
          this.conditions = null
          return null
        } catch (error) {
          console.error('获取单点事件筛选条件异常：', error)
          this.conditions = null
          return null
        }
      })()

      try {
        return await taskEventConditionsPromise
      } finally {
        this.conditionsLoading = false
      }
    },

    /**
     * 拉取部门-人员树（处理人员筛选）
     * - 放到 Store 做缓存与并发去重，避免弹框多次打开重复请求
     * @param force 是否强制刷新
     */
    async fetchDepartAccountTree(force = false): Promise<any[]> {
      if (!force && Array.isArray(this.departAccountTree) && this.departAccountTree.length > 0) {
        return this.departAccountTree
      }

      if (departAccountTreePending) {
        return departAccountTreePending
      }

      this.departAccountTreeLoading = true
      this.departAccountTreeFetchFailed = false
      this.departAccountTreeErrorMessage = ''

      departAccountTreePending = (async () => {
        try {
          const res = await getDepartAccountTree()
          if ((res as any)?.success) {
            const list = (res as any)?.result ?? []
            this.departAccountTree = Array.isArray(list) ? list : []
            this.departAccountTreeFetchFailed = false
            this.departAccountTreeErrorMessage = ''
            return this.departAccountTree
          }

          const msg = String((res as any)?.message || '获取部门人员树失败')
          console.error('获取部门人员树失败：', msg)
          this.departAccountTree = []
          this.departAccountTreeFetchFailed = true
          this.departAccountTreeErrorMessage = msg
          return []
        } catch (error) {
          console.error('获取部门人员树异常：', error)
          this.departAccountTree = []
          this.departAccountTreeFetchFailed = true
          this.departAccountTreeErrorMessage = String(
            (error as any)?.message || error || '获取部门人员树异常'
          )
          return []
        }
      })()

      try {
        return await departAccountTreePending
      } finally {
        this.departAccountTreeLoading = false
        departAccountTreePending = null
      }
    },

    //获取数据字典
    async fetchSysAllDictItems(force = false) {
      // 已拉取过（或本地已有缓存）且不强制刷新时，直接复用
      const hasCachedDict =
        this.allDictItems != null &&
        typeof this.allDictItems === 'object' &&
        Object.keys(this.allDictItems as any).length > 0
      if (!force && hasCachedDict) {
        return this.allDictItems
      }
      this.allDictItemsLoading = true
      const sysAllDictItemsPromise = (async () => {
        try {
          const { result } = await getSysAllDictItems()
          this.allDictItems = result?.sysAllDictItems || {}
        } catch (error) {
          console.error('获取数据字典失败:', error)
          this.allDictItems = this.allDictItems || {}
        } finally {
          this.allDictItemsLoading = false
        }
      })()

      try {
        return await sysAllDictItemsPromise
      } finally {
        this.allDictItemsLoading = false
      }
    },

    /**
     * 清理筛选条件缓存
     * - 用于需要重新拉取条件的场景（如切换账号/切换品牌等）
     */
    resetTaskEventCache() {
      this.conditions = null
      this.departAccountTree = []
      this.batchRuleCategoryTree = []
      this.departAccountTreeFetchFailed = false
      this.departAccountTreeErrorMessage = ''
      batchRuleCategoryTreePending = null
      this.resetTopicsBatchCache()
    }
  }
})
