// 通用下钻弹框 Store（结构优化版）
// 目标：不改变对外行为的前提下，梳理数据结构、抽离通用工具、统一注释，提升可维护性。
// 要点：
// 1) openDD / drillDown 均支持传入 tags，入参与展示参数解耦；
// 2) tags 归一化（按 value 的 key 有序拼接去重，保留最新）；
// 3) ddQueryParams 始终为“当前层有效查询条件”的干净快照（移除 undefined）；
// 4) 弹框内部 view 参数（ddViewParams）仅用于展示状态，不污染接口入参。

import { defineStore } from 'pinia'
import { ref, reactive, computed, watch } from 'vue'
import { useQueryStore } from '@/store/modules/query'
import { useRoute } from 'vue-router'

// 详情页面类型
export enum ContentType {
  MAIN = 'main',
  USER = 'user'
}

// 详情页面数据
export interface DetailPageData {
  type: ContentType
  data: any
  title?: string
  headerMode?: 'back' | 'close'
}

// 标签对象：text 为展示文本；value 为影响查询的键集合；deletable 控制是否允许在 UI 关闭
export type TagItem = { text: string; value: Record<string, any>; deletable?: boolean }

// 下钻层级对象：记录本层查询条件、可视状态与生成时间戳
export type DrillLevel = {
  id: number
  params: Record<string, any>
  view?: Record<string, any>
  tags: TagItem[]
  ts: number
  // 记录该层选择的 tab，便于返回时恢复
  activeTab?: string
}

// 打开下钻弹框时的可选配置
export type OpenDDOptions = {
  // 是否合并 queryStore.commonQueryParams，默认合并
  mergeCommonQueryParams?: boolean
}

// ========== 私有工具 ==========
// 说明：仅在本模块内使用的帮助函数，集中放置，便于后续维护与测试。

// 生成层级唯一 id（时间戳 + 随机数）
const makeId = () => Date.now() + Math.floor(Math.random() * 1000)

// 浅层移除对象中值为 undefined 的键（不处理 null）
const cleanUndefined = <T extends Record<string, any>>(obj: T): T => {
  const ret: Record<string, any> = {}
  Object.keys(obj || {}).forEach(k => {
    if (obj[k] !== undefined) ret[k] = obj[k]
  })
  return ret as T
}

// 标签去重：基于 value 的 key 集合（按字母序拼接）判定同类，仅保留“较新的”一项
const normalizeTags = (tags?: TagItem[]): TagItem[] => {
  if (!Array.isArray(tags) || tags.length === 0) return []
  const unique: TagItem[] = []
  const seen = new Set<string>()
  for (let i = tags.length - 1; i >= 0; i--) {
    const t = tags[i]
    const key = Object.keys(t?.value || {})
      .sort((a, b) => a.localeCompare(b))
      .join(',')
    if (!key || !seen.has(key)) {
      if (key) seen.add(key)
      unique.unshift(t)
    }
  }
  return unique
}

// 判断两个标签是否“相同”：text 相同且 value 的键值集合完全一致
const isSameTag = (a?: TagItem, b?: TagItem): boolean => {
  if (!a || !b) return false
  if (a.text !== b.text) return false
  const aKeys = Object.keys(a.value || {}).sort()
  const bKeys = Object.keys(b.value || {}).sort()
  if (aKeys.length !== bKeys.length) return false
  for (let i = 0; i < aKeys.length; i++) {
    const ak = aKeys[i]
    const bk = bKeys[i]
    if (ak !== bk) return false
    if ((a.value as any)[ak] !== (b.value as any)[bk]) return false
  }
  return true
}

/**
 * 复制层级视图快照，避免可见 Tab 数组在层级之间共享引用。
 * @param view 当前层视图状态
 * @returns 独立的视图状态副本
 */
const cloneLevelView = (view?: DrillLevel['view']): DrillLevel['view'] => {
  if (!view) return undefined

  return {
    ...view,
    ...(Array.isArray(view.visibleTabs) ? { visibleTabs: [...view.visibleTabs] } : {})
  }
}

export const useGeneralDrillDownStore = defineStore('generalDrillDown', () => {
  // ========== 基础状态 ==========
  const ddVisible = ref<boolean>(false)
  const componentName = ref<string>(ContentType.MAIN)
  const componentData = ref<any>(null)
  const headerMode = ref<'back' | 'close'>('back')

  // 弹框内部专用的查询与展示参数（与全局查询解耦）
  const ddQueryParams = reactive<any>({})
  const ddViewParams = reactive<any>({})

  const route = useRoute()
  const shouldUseDialog = computed(() => route.name !== 'rootCause')

  // rootCause 页面不使用弹窗容器：强制关闭弹窗，避免跨路由“闪开/闪关”
  watch(
    () => route.name,
    name => {
      if (name === 'rootCause') ddVisible.value = false
    },
    { immediate: true }
  )

  // ========== 层级栈 ==========
  const stack = reactive<DrillLevel[]>([])
  const activeIndex = ref<number>(-1)
  const currentLevel = computed<DrillLevel | null>(() => stack[activeIndex.value] || null)
  const showBack = computed<boolean>(() => stack.length > 1)

  // ========== 同步函数 ==========
  // 将当前层 params 同步到 ddQueryParams（保持引用不变，移除 undefined）
  const syncToQuery = (level: DrillLevel | null) => {
    Object.keys(ddQueryParams).forEach(k => delete (ddQueryParams as any)[k])
    if (level) {
      Object.assign(ddQueryParams, cleanUndefined(level.params || {}))
    }
  }

  /**
   * 将指定层级记录的视图状态恢复到 ddViewParams。
   * lastDrillFrom 仅描述最近一次下钻动作，返回时清空；drillScene 属于下钻链路来源，需要按层恢复。
   * @param level 需要恢复视图状态的目标层级
   */
  const restoreViewParamsFromLevel = (level: DrillLevel | null) => {
    const drillScene =
      typeof level?.view?.drillScene === 'string' ? (level.view.drillScene as string) : ''
    Object.assign(ddViewParams, { lastDrillFrom: '', drillScene })

    if (level?.activeTab) {
      Object.assign(ddViewParams, { activeTab: level.activeTab })
    } else {
      delete ddViewParams.activeTab
    }

    if (level?.view && Array.isArray((level.view as any).visibleTabs)) {
      Object.assign(ddViewParams, { visibleTabs: (level.view as any).visibleTabs })
    } else {
      delete ddViewParams.visibleTabs
    }
  }

  // ========== 业务辅助 ==========
  // 根据路由设置默认下钻固定条件
  const setDefaultDDParamsByRouteName = () => {
    let brandDataType: number | undefined
    let tagType: string | undefined
    if (route.name === 'thisProductAnalysis') {
      brandDataType = 1
      tagType = 'DOM'
    } else if (route.name === 'journeyAnalysis') {
      brandDataType = 1
      tagType = 'CJ'
    } else if (route.name === 'productAnalysis') {
      brandDataType = 1
      tagType = 'PROD'
    } else if (route.name === 'serviceAnalysis') {
      brandDataType = 1
      tagType = 'SERVICE'
    }
    return { brandDataType, tagType }
  }

  // ========== 层级构建与切换 ==========
  // 组装层级对象：统一清洗 params/tags
  const makeLevel = (
    params: Record<string, any>,
    tags?: TagItem[],
    activeTab?: string
  ): DrillLevel => {
    return {
      id: makeId(),
      params: cleanUndefined({ ...(params || {}) }),
      tags: normalizeTags(tags),
      ts: Date.now(),
      ...(activeTab ? { activeTab } : {})
    }
  }

  // 重置为首层
  const resetStack = (params: Record<string, any>, tags?: TagItem[], activeTab?: string) => {
    stack.splice(0, stack.length)
    const first = makeLevel(params, tags, activeTab)
    stack.push(first)
    activeIndex.value = 0
    syncToQuery(first)
  }

  // ========== 对外方法 ==========
  const setView = (detailData: DetailPageData) => {
    componentName.value = detailData.type
    componentData.value = detailData.data || {}
    headerMode.value = detailData.headerMode || 'back'
  }

  // 显示主内容区（仅切换内容，不改变弹窗可见性）
  const showMain = () => {
    setView({ type: ContentType.MAIN, data: null, title: '主内容', headerMode: 'back' })
  }

  // 显示详情（仅切换内容，不改变弹窗可见性）
  const showDetail = (detailData: DetailPageData) => {
    setView(detailData)
  }

  // 隐藏详情（回到主内容，不打开弹窗）
  const hideDetail = () => {
    showMain()
  }

  // 打开弹框（首层）。保持 initParams 与 tags 独立：tags 不回写到 params
  const openDD = (
    initParams?: Record<string, any>,
    viewParams?: Record<string, any>,
    filterTags: TagItem[] = [],
    options: OpenDDOptions = {}
  ) => {
    if (shouldUseDialog.value) ddVisible.value = true
    const queryStore = useQueryStore()
    const shouldMergeCommonParams = options.mergeCommonQueryParams !== false
    const baseParams: any = {
      // ...(queryStore?.currentQueryParams || {}),
      ...(shouldMergeCommonParams ? queryStore?.commonQueryParams || {} : {}),
      ...setDefaultDDParamsByRouteName(),
      ...(initParams || {})
    }
    // 展示参数独立存放，避免污染接口入参
    Object.keys(ddViewParams).forEach(key => delete (ddViewParams as any)[key])
    Object.assign(ddViewParams, viewParams || {})
    // 默认处理日期筛选标签展示（置于首位，不可删除）
    const dateText =
      baseParams.startDate === baseParams.endDate
        ? baseParams.startDate || ''
        : `${baseParams.startDate || ''} - ${baseParams.endDate || ''}`
    const nextTags: TagItem[] = [
      {
        text: dateText,
        value: { startDate: baseParams.startDate, endDate: baseParams.endDate },
        deletable: false
      },
      ...(filterTags || [])
    ]
    // 处理筛选标签
    resetStack(baseParams, normalizeTags(nextTags), (viewParams as any)?.activeTab)
    if (currentLevel.value) {
      currentLevel.value.view = {
        drillScene: typeof ddViewParams.drillScene === 'string' ? ddViewParams.drillScene : ''
      }
    }
    showMain()
  }

  // 关闭弹框（延迟复位头部状态以配合动画）
  const closeDD = () => {
    ddVisible.value = false
    setTimeout(() => {
      showMain()
    }, 500)
  }

  // 下钻：在当前层基础上合并 delta 形成下一层，并合并标签
  const drillDown = (delta: Record<string, any>, filterTags: TagItem[] = []) => {
    const base = currentLevel.value?.params || {}
    const merged = { ...base, ...(delta || {}) }
    const prevTags = currentLevel.value?.tags || []
    // 记录当前层的已选 tab，便于回退恢复
    if (stack[activeIndex.value]) {
      // 获取当前选中的 tab（优先使用 ddViewParams.activeTab，如果没有则使用 stack 中记录的）
      const currentActiveTab =
        (ddViewParams as any)?.activeTab || stack[activeIndex.value].activeTab
      stack[activeIndex.value].activeTab = currentActiveTab
      // 保存当前层 Tab 快照并保留既有来源场景；本次动作产生的新来源只写入下一层
      const vt = (ddViewParams as any)?.visibleTabs
      stack[activeIndex.value].view = {
        ...(stack[activeIndex.value].view || {}),
        ...(Array.isArray(vt) ? { visibleTabs: [...vt] } : {})
      }
    }
    const next = makeLevel(
      merged,
      normalizeTags([...(prevTags || []), ...(filterTags || [])]),
      (ddViewParams as any)?.activeTab
    )
    next.view = {
      drillScene: typeof ddViewParams.drillScene === 'string' ? ddViewParams.drillScene : ''
    }
    stack.splice(activeIndex.value + 1)
    stack.push(next)
    activeIndex.value = stack.length - 1
    syncToQuery(next)
  }

  // 删除当前层的某一标签：
  // 优先按“回退层级”的方式处理（回退到未包含该标签的最近一层），
  // 若无法回退（仅一层或找不到更早层），则在当前层清理对应查询条件。
  const removeTag = (_target: any, idx: number) => {
    const curIndex = activeIndex.value
    const cur = currentLevel.value
    if (!cur) return

    // 不可删除的标签（如日期范围）直接忽略
    if (_target?.deletable === false) return

    // 先尝试按“标签来源层级”回退：
    // 从当前层往上找，第一个不包含该标签的层级索引。
    let targetIndex = -1
    for (let i = curIndex - 1; i >= 0; i--) {
      const level = stack[i]
      if (!level) continue
      const exists = (level.tags || []).some(t => isSameTag(t, _target))
      if (!exists) {
        targetIndex = i
        break
      }
    }

    if (targetIndex >= 0) {
      // 逐层回退到目标层，复用 popLevel 的恢复逻辑
      for (let i = curIndex; i > targetIndex; i--) {
        // popLevel 内部已处理 ddQueryParams 与 ddViewParams 的同步
        ;(popLevel as () => void)()
      }
      return
    }

    // 回退失败时降级为“仅修改当前层条件”的旧逻辑
    const tags = [...(cur.tags || [])]
    if (idx < 0 || idx >= tags.length) return
    tags.splice(idx, 1)
    const newParams = { ...(cur.params || {}) }
    Object.keys(_target?.value || {}).forEach(k => {
      newParams[k] = undefined
    })
    const next = makeLevel(newParams, normalizeTags(tags), cur.activeTab)
    // 查询条件发生变化后，旧 visibleTabs 已失效；保留入口场景但让弹框重新计算 Tab。
    next.view = cloneLevelView(cur.view)
    if (next.view) delete next.view.visibleTabs
    stack[activeIndex.value] = next
    delete ddViewParams.visibleTabs
    syncToQuery(next)
  }

  // 返回上一层
  const popLevel = () => {
    if (stack.length <= 1) return
    stack.pop()
    activeIndex.value = stack.length - 1
    const prev = currentLevel.value
    syncToQuery(prev)
    restoreViewParamsFromLevel(prev)
  }

  // 仅更新弹框内部查询参数（不影响全局）
  const updateDDQueryParams = (params: Record<string, any>) => {
    Object.assign(ddQueryParams, params)
  }
  // 仅更新展示参数（不参与接口）
  const updateDDViewParams = (params: Record<string, any>) => {
    Object.assign(ddViewParams, params)
  }

  // 设置当前层选中的 tab（供组件在切换 tab 时调用）
  const setActiveTab = (tabKey: string) => {
    Object.assign(ddViewParams, { activeTab: tabKey })
    if (stack[activeIndex.value]) {
      stack[activeIndex.value].activeTab = tabKey
    }
  }

  return {
    // 状态
    ddVisible,
    componentName,
    componentData,
    headerMode,
    ddQueryParams,
    ddViewParams,
    // 层级/历史
    stack,
    activeIndex,
    currentLevel,
    showBack,
    // 方法
    showDetail,
    hideDetail,
    showUserDetail: (userData: any, mode?: 'back' | 'close') => {
      if (shouldUseDialog.value) ddVisible.value = true
      showDetail({ type: ContentType.USER, data: userData, title: '用户详情', headerMode: mode })
    },
    openDD,
    closeDD,
    updateDDQueryParams,
    updateDDViewParams,
    setActiveTab,
    setDefaultDDParamsByRouteName,
    drillDown,
    removeTag,
    popLevel
  }
})

export default useGeneralDrillDownStore
