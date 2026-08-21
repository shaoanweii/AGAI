import type { TabItem } from './types'

/**
 * 解析当前层应激活的下钻 Tab。
 * 按候选优先级依次命中首个仍然可见的 Tab；若全部失效，则回退到第一个可见 Tab。
 * 该方法用于层级切换后的状态恢复，避免把上一层残留的 currentTab 继续带入当前层。
 * @param tabs 当前层允许展示的 Tab 列表
 * @param candidates 候选 Tab Key，按优先级从高到低传入
 * @returns 当前层最终应激活的合法 Tab Key；无可见 Tab 时返回空串
 */
export const resolveDrillDownActiveTab = (
  tabs: TabItem[],
  candidates: Array<string | null | undefined>
): string => {
  for (const candidate of candidates) {
    if (candidate && tabs.some(tab => tab.key === candidate)) {
      return candidate
    }
  }

  return tabs[0]?.key || ''
}
