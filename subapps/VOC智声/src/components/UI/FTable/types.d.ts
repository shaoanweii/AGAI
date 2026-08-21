// FTable 组件类型定义
import type { Placement } from 'element-plus'

export interface TableOverflowTooltipOptions {
  /** Tooltip 主题 */
  effect?: 'dark' | 'light'
  /** Tooltip 位置 */
  placement?: Placement
  /** Tooltip 弹层类名 */
  popperClass?: string
  /** 鼠标是否可进入 Tooltip */
  enterable?: boolean
  /** Tooltip 偏移量 */
  offset?: number
  /** Popper 配置 */
  popperOptions?: object
  /** 是否显示箭头 */
  showArrow?: boolean
  /** Tooltip 挂载节点 */
  appendTo?: string | HTMLElement
  /** 过渡动画名称 */
  transition?: string
  /** 显示延迟 */
  showAfter?: number
  /** 隐藏延迟 */
  hideAfter?: number
}

export interface TableColumn {
  /** 列标题 */
  title?: string | (() => any)
  /** 数据字段名 */
  dataIndex?: string
  /** 列宽度 */
  width?: number | string
  /** 最小宽度 */
  minWidth?: number | string
  /** 对齐方式 */
  align?: 'left' | 'center' | 'right'
  /** 固定列 */
  fixed?: boolean | 'left' | 'right'
  /** 是否可排序 */
  sortable?: boolean | string
  /** 是否显示溢出提示 */
  showOverflowTooltip?: boolean | TableOverflowTooltipOptions
  /** 自定义渲染函数 */
  render?: (params: { record: any; rowIndex: number; column: TableColumn }) => any
  /** 自定义表头样式 - 基于 Element Plus 官方 API */
  headerCellStyle?:
    | object
    | ((data: { row: any; column: any; rowIndex: number; columnIndex: number }) => object)
  /** 自定义表头类名 - 基于 Element Plus 官方 API */
  headerCellClassName?:
    | string
    | ((data: { row: any; column: any; rowIndex: number; columnIndex: number }) => string)
  /** 自定义单元格样式 - 基于 Element Plus 官方 API */
  cellStyle?:
    | object
    | ((data: { row: any; column: any; rowIndex: number; columnIndex: number }) => object)
  /** 自定义单元格类名 - 基于 Element Plus 官方 API */
  cellClassName?:
    | string
    | ((data: { row: any; column: any; rowIndex: number; columnIndex: number }) => string)
}

export interface FTableProps {
  /** 列配置 */
  columns?: TableColumn[]
  /** 表格数据 */
  data?: any[]
  /** 加载状态 */
  loading?: boolean
  /** 表格高度 */
  height?: string | number
  /** 分页配置 */
  pagination?: boolean | object
  /** 边框配置 */
  bordered?: boolean | object
  /** 滚动配置 */
  scroll?: object
}

// 示例数据接口
export interface TrendingWord {
  keyword: string
  growthRate: number
  userCount: number
}

export interface RankingItem {
  rank: number
  name: string
  value: number
  ratio: number
}
