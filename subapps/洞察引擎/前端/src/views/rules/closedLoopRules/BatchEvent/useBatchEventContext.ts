import { inject, provide, ref, type InjectionKey, type Ref } from 'vue'
import type { BatchCategoryTreeNode } from './types'

interface BatchEventContextValue {
  currentCategory: Ref<BatchCategoryTreeNode | null>
  categoryRefreshToken: Ref<number>
  setCurrentCategory: (category: BatchCategoryTreeNode | null) => void
  notifyRuleChanged: () => void
}

const batchEventContextKey: InjectionKey<BatchEventContextValue> = Symbol('batchEventContext')

/**
 * 页面级状态统一在这里收口，减少分类面板、规则列表和外层容器之间的事件透传。
 * @returns BatchEventContextValue
 */
export const provideBatchEventContext = () => {
  const currentCategory = ref<BatchCategoryTreeNode | null>(null)
  const categoryRefreshToken = ref(0)

  /**
   * 左侧分类切换后统一写入上下文，右侧规则区域直接消费同一份状态。
   * @param category 当前选中的分类节点
   */
  const setCurrentCategory = (category: BatchCategoryTreeNode | null) => {
    currentCategory.value = category
  }

  /**
   * 规则数据变更后只发出一次刷新信号，交给分类面板自行处理统计更新。
   */
  const notifyRuleChanged = () => {
    categoryRefreshToken.value += 1
  }

  const contextValue: BatchEventContextValue = {
    currentCategory,
    categoryRefreshToken,
    setCurrentCategory,
    notifyRuleChanged
  }

  provide(batchEventContextKey, contextValue)

  return contextValue
}

/**
 * 读取批量规则页面上下文；若脱离容器使用，直接抛错帮助快速定位接入问题。
 * @returns BatchEventContextValue
 */
export const useBatchEventContext = () => {
  const context = inject(batchEventContextKey, null)

  if (!context) {
    throw new Error('BatchEvent 页面上下文未初始化')
  }

  return context
}
