// H5 rem 适配与 viewport 相关逻辑的基础 Hook
// 用于独立 H5 页面（如 /h5NotAuth）或需要基础适配的场景
import 'amfe-flexible'
import { isH5Route, refreshRemAdaptation } from '@/utils/rem.ts'

import { onMounted, onBeforeUnmount } from 'vue'
import { useH5ssoStore } from '@h5/store/sso'
import { consumePendingIndataReturnFlag } from '@h5/utils/indataReturn'

const H5_VIEWPORT_CONTENT =
  'width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no,viewport-fit=cover'
const DEFAULT_VIEWPORT_CONTENT = 'width=device-width, initial-scale=1.0'

/**
 * H5 布局基础逻辑 Hook（简化版）
 * - 统一管理 body 类名（如 h5-active 等）
 * - 管理 viewport 配置与 rem 适配，禁止缩放并适配刘海屏
 * - 不包含权限初始化和访问时长统计（这些由 useH5Layout 负责）
 */
export function useH5LayoutSimple() {
  const h5ssoStore = useH5ssoStore()
  let originalViewport: string | null = null
  let originalFontSize: string | null = null
  let recalibrationTimer: number | null = null

  /**
   * 设置或更新 viewport 配置。
   * @param content viewport 元信息内容
   */
  const setViewport = (content: string) => {
    if (typeof document === 'undefined') return

    let viewportMeta = document.querySelector('meta[name="viewport"]') as HTMLMetaElement | null
    if (!viewportMeta) {
      viewportMeta = document.createElement('meta')
      viewportMeta.name = 'viewport'
      document.head.appendChild(viewportMeta)
    }
    viewportMeta.setAttribute('content', content)
  }

  /**
   * 应用 H5 页面统一的 viewport 配置。
   * - 禁止缩放，避免企微 PC 返回时出现缩放状态残留
   * - 保持与 rem 计算前提一致
   */
  const applyH5Viewport = () => {
    setViewport(H5_VIEWPORT_CONTENT)
  }

  /**
   * 恢复离开 H5 后的基础 viewport。
   * - 优先恢复进入 H5 前的原始配置
   * - 若未记录到原始值，则回退到站点默认 viewport
   */
  const restoreBaseViewport = () => {
    if (originalViewport) {
      setViewport(originalViewport)
      return
    }

    setViewport(DEFAULT_VIEWPORT_CONTENT)
  }

  /**
   * 重新校准 H5 页面适配状态。
   * - 跨应用返回、BFCache 恢复时，组件不一定重新挂载
   * - 这里先重写 viewport，再立即/延迟各执行一次 rem 重算
   * - 延迟重算用于兜底企微 PC 恢复时 clientWidth 尚未稳定的场景
   */
  const recalibrateH5Layout = () => {
    if (typeof document === 'undefined' || !isH5Route()) return

    document.body.classList.add('h5-active')
    applyH5Viewport()
    refreshRemAdaptation()

    if (recalibrationTimer !== null) {
      window.clearTimeout(recalibrationTimer)
    }

    recalibrationTimer = window.setTimeout(() => {
      applyH5Viewport()
      refreshRemAdaptation()
      recalibrationTimer = null
    }, 30)
  }

  /**
   * 页面恢复后，除了重算 viewport/rem，仅在 indata 返回场景下补做一次登录态探测。
   * - 当前只有 CanswerLogin 跳往的 indata 页面会返回本系统，因此不再对所有恢复场景都探测
   * - 命中标记后立即消费，避免 pageshow / focus / visibilitychange 连续触发多次请求
   * - 具体的鉴权失败与 SSO 自动登录逻辑交给 H5 请求拦截器与 SSO Store 处理
   */
  const restoreH5PageState = () => {
    recalibrateH5Layout()

    if (!consumePendingIndataReturnFlag()) return

    void h5ssoStore.ensureH5SessionOnResume()
  }

  /**
   * 页面从浏览器历史或 BFCache 恢复时重新校准 H5 布局。
   */
  const handlePageShow = () => {
    restoreH5PageState()
  }

  /**
   * 页面重新可见时兜底重校准 H5 布局。
   */
  const handleVisibilityChange = () => {
    if (document.visibilityState !== 'visible') return
    restoreH5PageState()
  }

  /**
   * 外层容器重新聚焦时兜底重校准 H5 布局。
   */
  const handleWindowFocus = () => {
    restoreH5PageState()
  }

  onMounted(() => {
    if (typeof document === 'undefined') return

    document.body.classList.add('h5-active')

    // 记录进入 H5 布局前的根字体大小，方便离开时恢复
    originalFontSize = getComputedStyle(document.documentElement).fontSize

    // 记录原始 viewport 配置
    const existingViewport = document.querySelector('meta[name="viewport"]') as HTMLMetaElement | null
    if (existingViewport) {
      originalViewport = existingViewport.getAttribute('content')
    }

    applyH5Viewport()
    refreshRemAdaptation()

    window.addEventListener('pageshow', handlePageShow)
    document.addEventListener('visibilitychange', handleVisibilityChange)
    window.addEventListener('focus', handleWindowFocus)
  })

  onBeforeUnmount(() => {
    if (typeof document === 'undefined') return

    window.removeEventListener('pageshow', handlePageShow)
    document.removeEventListener('visibilitychange', handleVisibilityChange)
    window.removeEventListener('focus', handleWindowFocus)

    if (recalibrationTimer !== null) {
      window.clearTimeout(recalibrationTimer)
      recalibrationTimer = null
    }

    document.body.classList.remove('h5-active')

    // 恢复原始根字体大小，避免影响其他页面
    if (originalFontSize) {
      document.documentElement.style.fontSize = originalFontSize
    } else {
      document.documentElement.style.fontSize = ''
    }

    // 恢复原始 viewport 配置，避免影响非 H5 页面
    restoreBaseViewport()
  })
}
