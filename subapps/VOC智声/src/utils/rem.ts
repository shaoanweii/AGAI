/**
 * H5页面rem适配工具
 * 配合 postcss-pxtorem + amfe-flexible 使用
 */

// 设计稿宽度
// const DESIGN_WIDTH: number = 375;
// 基准字体大小（设计稿宽度的1/10）
const BASE_FONT_SIZE: number = 37.5

/**
 * 统一绑定 H5 rem 适配所需的窗口事件。
 * - 仅在 H5 页面中启用，避免影响 PC 端页面
 * - 使用同一回调引用，重复 addEventListener 不会产生重复绑定
 */
function bindRemResizeListeners(): void {
  window.addEventListener('resize', debouncedSetRem)
  window.addEventListener('orientationchange', debouncedSetRem)
}

/**
 * 统一解绑 H5 rem 适配的窗口事件。
 * - 离开 H5 路由时及时清理，避免非 H5 页面继续沿用 rem 监听
 */
function unbindRemResizeListeners(): void {
  window.removeEventListener('resize', debouncedSetRem)
  window.removeEventListener('orientationchange', debouncedSetRem)
}

/**
 * 检查当前是否处于 H5 页面路由。
 * - 同时兼容 hash 模式与 pathname 模式
 * - 供布局恢复和 rem 计算逻辑复用
 */
export function isH5Route(): boolean {
  const currentPath: string = window.location.hash || window.location.pathname
  return currentPath.includes('/h5') || currentPath.includes('#/h5')
}

/**
 * 动态设置根元素字体大小
 * 配合 amfe-flexible 使用，在特定场景下进行微调
 */
function setRem(): void {
  // 只在H5页面路由下才进行rem适配
  if (!isH5Route()) {
    // 如果不是H5页面，恢复默认字体大小，交给amfe-flexible处理
    document.documentElement.style.fontSize = ''
    return
  }

  // 获取当前屏幕宽度
  const clientWidth: number = document.documentElement.clientWidth
  // const scale: number = clientWidth / DESIGN_WIDTH;

  // 计算字体大小，与amfe-flexible保持一致
  // amfe-flexible通常设置为 clientWidth / 10
  const fontSize: number = clientWidth / 10

  // 设置合理的字体大小范围
  const minFontSize: number = 12 // 最小字体大小
  const maxFontSize: number = 54 // 最大字体大小
  const finalFontSize: number = Math.min(Math.max(fontSize, minFontSize), maxFontSize)

  document.documentElement.style.fontSize = `${finalFontSize}px`
}

/**
 * px转rem工具函数
 * 注意：postcss-pxtorem会自动转换，此函数主要用于JS中的动态计算
 */
export function pxToRem(px: number): string {
  return `${px / BASE_FONT_SIZE}rem`
}

/**
 * rem转px工具函数
 */
export function remToPx(rem: number): number {
  const rootFontSize: number = parseFloat(getComputedStyle(document.documentElement).fontSize)
  return rem * rootFontSize
}

/**
 * 获取当前根字体大小
 */
export function getRootFontSize(): number {
  return parseFloat(getComputedStyle(document.documentElement).fontSize)
}

/**
 * 获取视口宽度对应的rem值
 */
export function getViewportRem(): number {
  return document.documentElement.clientWidth / 10
}

// 防抖函数，避免频繁调用
function debounce<T extends (...args: any[]) => any>(func: T, wait: number): T {
  let timeout: number | null = null
  return function executedFunction(...args: any[]) {
    const later = () => {
      timeout = null
      func(...args)
    }
    if (timeout !== null) {
      clearTimeout(timeout)
    }
    timeout = window.setTimeout(later, wait)
  } as T
}

// 防抖处理的setRem函数
const debouncedSetRem = debounce(setRem, 100)

/**
 * 主动刷新 rem 适配状态。
 * - 路由切换时由全局监听自动触发
 * - 页面从外部应用返回、BFCache 恢复时可由业务主动调用
 */
export function refreshRemAdaptation(): void {
  if (isH5Route()) {
    setRem()
    bindRemResizeListeners()
    return
  }

  unbindRemResizeListeners()
  document.documentElement.style.fontSize = ''
}

/**
 * 初始化rem适配
 * 配合amfe-flexible使用，主要处理路由切换时的适配
 */
function initRemAdaptation(): void {
  refreshRemAdaptation()
}

// 监听路由变化 (适配Vue Router的hash模式)
window.addEventListener('hashchange', initRemAdaptation)
window.addEventListener('popstate', initRemAdaptation)

// 页面加载完成后初始化
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', initRemAdaptation)
} else {
  initRemAdaptation()
}

export default setRem
