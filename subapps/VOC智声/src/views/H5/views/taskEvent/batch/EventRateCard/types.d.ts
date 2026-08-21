/**
 * 新增事件统计卡片的数据接口
 */

// 新增事件统计数据类型
export interface EventRateData {
  /** 当前周期新增事件数量 */
  currentCounts: number
  /** 上个周期新增事件数量 */
  lastCounts: number
  /** 闭环率（%） */
  closeRate: number
  /** 环比（%） */
  ringRate: number
}

// 新增事件统计卡片组件 Props 类型
export interface EventRateCardProps {
  /** 数据 */
  data?: Partial<EventRateData>
  /** 本周、本月等文案 */
  name?: string
  /** 加载状态 */
  loading?: boolean
}

// 新增事件统计卡片组件 Emits 类型
export interface EventRateCardEmits {
  /** 卡片点击 */
  (e: 'click', data: EventRateData): void

  /** 查看详情点击（预留） */
  (e: 'view-details', data: EventRateData): void
}
