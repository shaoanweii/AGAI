/**
 * 报告管理批量操作类型
 */
export enum ReportBatchType {
  Review = 'review',
  Release = 'release',
  Delisted = 'delisted',
  Move = 'move'
}

/**
 * 批量弹窗titile
 */
export const BatchTitleMap: Record<ReportBatchType, string> = {
  review: '批量审核',
  release: '批量发布',
  delisted: '批量下架',
  move: '批量移动'
}

export enum TipType {
  Release = 'release',
  Delisted = 'delisted'
}
/**
 * @description: 处理单条数据二次确认提示title
 * @return {*}
 */
export const TipTitleMap: Record<TipType, string> = {
  release: '报告发布',
  delisted: '报告下架'
}

export const TipInfoMap: Record<TipType, string> = {
  release: '是否确认发布当前报告？',
  delisted: '是否确认下架当前报告？'
}

/**
 * @description: 审核方式
 * @return {*}
 */
export const reviewOptions = [
  {
    label: '通过',
    value: '1'
  },
  {
    label: '拒绝',
    value: '3'
  }
]
