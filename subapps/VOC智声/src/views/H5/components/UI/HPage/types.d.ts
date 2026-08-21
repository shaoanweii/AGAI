/**
 * HPage 组件相关类型定义
 */

export interface HPageProps {
  /** 页面背景色 */
  backgroundColor?: string
  /** 状态栏背景色（移动端）*/
  statusBarColor?: string
  /** 状态栏内容样式（light/dark）*/
  statusBarStyle?: 'light' | 'dark'
  /** 是否开启安全区适配 */
  safeAreaInsetTop?: boolean
  /** 是否开启底部安全区适配 */
  safeAreaInsetBottom?: boolean
}

export interface HPageSlots {
  /** 自定义导航栏内容 */
  'nav-bar'?: any
  /** 页面主要内容 */
  default?: any
}
