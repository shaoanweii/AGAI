import type { BatchEventActionRow } from '../types'

/**
 * @description: 从选中行中提取稳定的事件 id 列表
 * @param {BatchEventActionRow[]} selection 当前勾选行
 * @return {string[]}
 */
export const getBatchActionSelectedIds = (selection: BatchEventActionRow[] = []): string[] => {
  return selection
    .map(item => item.id)
    .filter(id => id !== undefined && id !== null && `${id}`.length > 0)
    .map(id => String(id))
}
