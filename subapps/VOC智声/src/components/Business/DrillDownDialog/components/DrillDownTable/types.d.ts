/**
 * DrillDownTable 组件类型定义
 * 下钻表格组件相关接口
 */
import type { TableColumnCtx } from 'element-plus'

export type TableSortable = boolean | 'custom'

// 表格头部列配置类型
export interface TableHeaderColumn {
  /** 列键名 */
  key: string
  /** 列标题 */
  label: string
  /** 渲染 */
  render?: any
  /** 是否可排序 */
  sortable?: TableSortable
  /** 列宽度 */
  width?: string | number
  /** 最小宽度 */
  minWidth?: string | number
  // 是否启用列的 tooltip
  tooltip?: {
    show: boolean,
    formatter?: (row: any) => string
  }
}

// 表格头部组配置类型
export interface TableHeaderGroup {
  /** 组键名 */
  key: string
  /** 组标题 */
  label: string
  /** 列的背景色 string: 标题和内容颜色值相同  string[color1, color2]：color1-标题的背景色 color2-内容的背景色  */
  backgroundColor?: string | string[]
  /** 列之间的间隔 */
  columnPadding?: string | string []
  /** 行高度 */
  rowHeight?: string | number
  /** 渲染 */
  render?: any | (() => any)
  /** 是否可排序 */
  sortable?: TableSortable
  /** 列宽度 */
  width?: string | number
  /** 最小宽度 */
  minWidth?: string | number
  // 是否启用列的 tooltip
  tooltip?: {
    show: boolean,
    formatter?: (row: any) => string
  }
  /** 列配置数组 */
  columns?: TableHeaderColumn[]
}

// 排序事件类型
export interface SortChangeEvent {
  /** 排序字段 */
  sortBy: string
  /** 排序方向 */
  sortOrder: 'ascending' | 'descending' | null
}

// 组件 Props 接口
export interface DrillDownTableProps {
  /** 表格标题 */
  title?: string
  /** 表格数据 */
  data: any[]
  /** 表格头部配置 */
  columns: TableHeaderGroup[]
  /** 合并单元格方法 */
  spanMethod?: ({
                 row,
                 column,
                 rowIndex,
                 columnIndex
               }: SpanMethodProps) => {
    rowspan: number
    colspan: number
  }
  /** 表格高度 */
  height?: string
  /** 头部高度 */
  headerHeight?: string
  /** 内容每一行高度 */
  rowHeight?: string | number
  /** 默认头部背景颜色 */
  headerBackgroundColor?: string
  /** 默认内容每一行背景颜色 */
  rowBackgroundColor?: string
  /** 是否显示斑马纹 */
  stripe?: boolean
  /** 是否显示排序图标 */
  showSortIcon?: boolean
  /** 是否显示帮助图标 */
  showHelpIcon?: boolean
  /** 是否显示边框 */
  border?: boolean
  /** 加载状态 */
  loading?: boolean
  /**分页加载中状态 */
  loadingMore?: boolean
  /**是否可滑动*/
  scrollable?: boolean
}

interface SpanMethodProps {
  row: any
  column: TableColumnCtx<any>
  rowIndex: number
  columnIndex: number
}

// 组件事件接口
export interface DrillDownTableEvents {
  /** 排序变化事件 */
  (e: 'sortChange', sortBy: SortChangeEvent['sortBy'], sortOrder: SortChangeEvent['sortOrder']): void

  /**单元格点击事件 */
  (e: 'cellClick', row: any, column: TableColumnCtx<any>, cell: HTMLElement, event: Event): void

  /** 行点击事件 */
  (e: 'rowClick', row: any, index: number): void

  /** 趋势图点击事件 */
  (e: 'trendClick', row: any): void

  /** 帮助图标点击事件 */
  (e: 'viewDetail', row: any, index: number): void

  /**
   * 表格滚动触底事件（用于滚动分页）
   * 说明：无参数，靠近底部阈值时触发
   */
  (e: 'reachBottom'): void
}

// 组件实例接口
export interface DrillDownTableInstance {
  /** 刷新表格 */
  refresh: () => void
  /** 清空排序 */
  clearSort: () => void
  /** 滚动到顶部 */
  scrollToTop: () => void
  /** 获取表格数据 */
  getTableData: () => any[]
}
