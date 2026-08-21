/**
 * FMapChart 地图图表组件类型定义
 * @description 基于 ECharts 的中国地图数据可视化组件的类型声明
 */

/**
 * 地图数据项接口
 */
export interface MapDataItem {
  /** 省份/地区名称 */
  name: string
  /** 数值 */
  value: number
  /** 环比变化 */
  mom?: number
  /** 同比变化 */
  yoy?: number
  /** 其他自定义字段 */
  [key: string]: any
}

/**
 * 字段映射配置接口
 */
export interface MapFieldMapping {
  /** 名称字段，默认 'name' */
  nameField: string
  /** 数值字段，默认 'value' */
  valueField: string
}

/**
 * 视觉映射配置接口
 */
export interface VisualMapConfig {
  /** 类型：连续型或分段型 */
  type?: 'continuous' | 'piecewise'
  /** 最小值 */
  min?: number
  /** 最大值 */
  max?: number
  /** 文本标签 */
  text?: string[]
  /** 是否可计算 */
  calculable?: boolean
  /** 布局方向 */
  orient?: 'horizontal' | 'vertical'
  /** 左侧位置 */
  left?: string | number
  /** 高度 */
  itemHeight?: number
  /** 宽度 */
  itemWidth?: number
  /** 对齐方式 */
  align?: 'left' | 'right'
  /** 是否显示 */
  show?: boolean
  /** 范围内颜色配置 */
  inRange?: {
    color?: string[]
  }
  [key: string]: any | any[]
}

/**
 * 地理坐标系配置接口
 */
export interface GeoConfig {
  /** 地图类型 */
  map?: string
  /** 是否开启漫游 */
  roam?: boolean
  /** 缩放比例 */
  zoom?: number
  /** 顶部位置 */
  top?: string | number
  /** 左侧位置 */
  left?: string | number
  /** 右侧位置 */
  right?: string | number
  /** 宽度 */
  width?: string | number
  /** 高度 */
  height?: string | number
  /** 标签配置 */
  label?: {
    show?: boolean
  }
  /** 样式配置 */
  itemStyle?: {
    borderColor?: string
    areaColor?: string
  }
  /** 高亮状态配置 */
  emphasis?: {
    label?: {
      show?: boolean
    }
    itemStyle?: {
      areaColor?: string
      borderWidth?: number
    }
  }
  /** 选中状态配置 */
  select?: {
    label?: {
      show?: boolean
    }
    itemStyle?: {
      areaColor?: string
      shadowOffsetX?: number
      shadowOffsetY?: number
      borderWidth?: number
      borderColor?: string
    }
  }
}

/**
 * Tooltip 格式化函数类型
 */
export type TooltipFormatter = (params: any) => string

/**
 * 组件 Props 接口
 */
export interface FMapChartProps {
  /** 地图数据源 */
  data?: MapDataItem[]
  /** 组件宽度 */
  width?: string
  /** 组件高度 */
  height?: string
  /** 图表主题 */
  theme?: string
  /** 地图缩放比例 */
  zoom?: number
  /** 是否开启地图漫游 */
  roam?: boolean
  /** 是否显示视觉映射组件 */
  showVisualMap?: boolean
  /** 视觉映射配置 */
  visualMapConfig?: VisualMapConfig
  /** 地理坐标系配置 */
  geoConfig?: GeoConfig
  /** 自定义 tooltip 格式化函数 */
  tooltipFormatter?: TooltipFormatter
  /** 字段映射配置 */
  fieldMapping?: MapFieldMapping
}

/**
 * 组件事件接口
 */
export interface FMapChartEmits {
  /** 地图区域点击事件 */
  (event: 'chart-click', params: any): void
  /** 图表初始化完成事件 */
  (event: 'chart-ready', chart: any): void
}
