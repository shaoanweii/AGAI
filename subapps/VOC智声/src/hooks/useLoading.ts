import { ElLoading } from 'element-plus'
import type { LoadingInstance } from 'element-plus/es/components/loading/src/loading'

/**
 * Loading 配置选项
 */
export interface LoadingOptions {
  /** 显示在加载图标下方的加载文案 */
  text?: string
  /** Loading 遮罩的背景色 */
  background?: string
  /** Loading 的目标 DOM 节点，可以传入一个 DOM 对象或字符串 */
  target?: HTMLElement | string
  /** 是否显示为全屏 Loading */
  fullscreen?: boolean
  /** 是否锁定屏幕的滚动 */
  lock?: boolean
  /** Loading 的自定义类名 */
  customClass?: string
  /** Loading 图标的自定义类名 */
  spinner?: string
  /** Loading 图标的 SVG 配置 */
  svg?: string
  /** Loading SVG 图标的自定义类名 */
  svgViewBox?: string
}

/**
 * 默认 Loading 配置
 */
const DEFAULT_OPTIONS: LoadingOptions = {
  text: '加载中...',
  background: 'rgba(0, 0, 0, 0.7)',
  // background: 'rgba(255, 255, 255, 0.7)',
  fullscreen: true,
  lock: true
}

/**
 * 全局 Loading 实例管理
 */
class LoadingManager {
  private loadingInstance: LoadingInstance | null = null
  private loadingCount = 0

  /**
   * 显示 Loading
   * @param options Loading 配置选项
   * @returns Loading 实例
   */
  show(options?: LoadingOptions): LoadingInstance {
    this.loadingCount++

    // 如果已经有 Loading 实例，直接返回
    if (this.loadingInstance) {
      return this.loadingInstance
    }

    const mergedOptions = {
      ...DEFAULT_OPTIONS,
      ...options
    }

    this.loadingInstance = ElLoading.service(mergedOptions)
    return this.loadingInstance
  }

  /**
   * 关闭 Loading
   * @param force 是否强制关闭（忽略计数器）
   */
  close(force = false): void {
    if (force) {
      this.loadingCount = 0
    } else {
      this.loadingCount--
    }

    // 只有当计数器为 0 时才真正关闭 Loading
    if (this.loadingCount <= 0 && this.loadingInstance) {
      this.loadingInstance.close()
      this.loadingInstance = null
      this.loadingCount = 0
    }
  }

  /**
   * 获取当前 Loading 实例
   */
  getInstance(): LoadingInstance | null {
    return this.loadingInstance
  }

  /**
   * 检查是否正在加载
   */
  isLoading(): boolean {
    return this.loadingInstance !== null
  }
}

// 创建全局单例
const loadingManager = new LoadingManager()

/**
 * 全局 Loading Hook
 * 提供统一的 Loading 管理功能，支持计数器防止多次调用导致的闪烁
 *
 * @example
 * ```ts
 * const { showLoading, hideLoading, isLoading } = useLoading()
 *
 * // 显示 Loading
 * showLoading({ text: '正在加载数据...' })
 *
 * // 关闭 Loading
 * hideLoading()
 *
 * // 检查是否正在加载
 * if (isLoading()) {
 *   console.log('正在加载中...')
 * }
 * ```
 */
export function useLoading() {
  /**
   * 显示 Loading
   * @param options Loading 配置选项
   * @returns Loading 实例
   */
  const showLoading = (options?: LoadingOptions): LoadingInstance => {
    return loadingManager.show(options)
  }

  /**
   * 关闭 Loading
   * @param force 是否强制关闭（忽略计数器）
   */
  const hideLoading = (force = false): void => {
    loadingManager.close(force)
  }

  /**
   * 获取当前 Loading 实例
   */
  const getLoadingInstance = (): LoadingInstance | null => {
    return loadingManager.getInstance()
  }

  /**
   * 检查是否正在加载
   */
  const isLoading = (): boolean => {
    return loadingManager.isLoading()
  }

  /**
   * 包装异步函数，自动显示和隐藏 Loading
   * @param asyncFn 异步函数
   * @param options Loading 配置选项
   * @returns 包装后的异步函数
   *
   * @example
   * ```ts
   * const { withLoading } = useLoading()
   *
   * const fetchData = withLoading(async () => {
   *   const response = await api.getData()
   *   return response.data
   * }, { text: '正在获取数据...' })
   *
   * await fetchData()
   * ```
   */
  const withLoading = <T extends (...args: any[]) => Promise<any>>(
    asyncFn: T,
    options?: LoadingOptions
  ): T => {
    return (async (...args: any[]) => {
      showLoading(options)
      try {
        const result = await asyncFn(...args)
        return result
      } finally {
        hideLoading()
      }
    }) as T
  }

  return {
    showLoading,
    hideLoading,
    getLoadingInstance,
    isLoading,
    withLoading
  }
}

/**
 * 直接导出全局 Loading 方法（不使用 Hook）
 * 适用于在非 Vue 组件中使用
 */
export const globalLoading = {
  show: (options?: LoadingOptions) => loadingManager.show(options),
  hide: (force = false) => loadingManager.close(force),
  getInstance: () => loadingManager.getInstance(),
  isLoading: () => loadingManager.isLoading()
}
