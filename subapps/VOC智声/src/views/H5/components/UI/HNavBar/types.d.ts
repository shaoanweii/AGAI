/**
 * HNavBar 组件相关类型定义
 */

export interface HNavBarProps {
  /** 导航栏标题 */
  title?: string
  /** 是否显示左侧返回按钮 */
  leftArrow?: boolean
  /** 左侧返回按钮文本 */
  leftText?: string
  /** 右侧按钮文本 */
  rightText?: string
  /** 导航栏背景色 */
  backgroundColor?: string
  /** 标题颜色 */
  titleColor?: string
  /** 状态栏背景色（移动端）*/
  statusBarColor?: string
  /** 状态栏内容样式（light/dark）*/
  statusBarStyle?: 'light' | 'dark'
  /** 是否开启安全区适配 */
  safeAreaInsetTop?: boolean
  /** 导航栏高度 */
  height?: string
  /** 是否固定在顶部 */
  fixed?: boolean
  /** 层级 */
  zIndex?: number
  /** 边框样式 */
  border?: boolean
  /** 是否隐藏导航栏 */
  hidden?: boolean
}

export interface HNavBarEmits {
  /** 点击左侧按钮时触发 */
  'click-left': []
  /** 点击右侧按钮时触发 */
  'click-right': []
  /** 点击标题时触发 */
  'click-title': []
}

export interface HNavBarSlots {
  /** 自定义左侧内容 */
  left?: any
  /** 自定义标题内容 */
  title?: any
  /** 自定义右侧内容 */
  right?: any
}
