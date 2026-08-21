import SDK from '@/integrations/mobile-container'

/**
 * @description: 打开系统浏览器
 * */
export const openSystemBrowser = async (link: string) => {
  if (!link) return
  try {
    await SDK.openSystemBrowser(link)
  } catch (e) {
    console.error(e)
  }
}

/**
 * @description: 去除HTML标签并截取指定长度的文本
 * @param {string} html - HTML字符串
 * @param {number} length - 截取长度
 * @return {string} 处理后的文本
 */
export const stripHtmlAndTruncate = (html: string | undefined | null, length: number): string => {
  if (!html) return ''

  // 创建临时DOM元素来去除HTML标签
  const tempDiv = document.createElement('div')
  tempDiv.innerHTML = html

  // 获取纯文本内容
  const text = tempDiv.textContent || tempDiv.innerText || ''

  // 截取指定长度
  return text.length > length ? text.substring(0, length) : text
}

/**
 * @description: 判断是否有上一页（用于返回逻辑）
 * @return {boolean} 是否有上一页
 */
export const hasPreviousPage = (): boolean => {
  const state = window.history.state
  const backPath = state?.back

  // Vue Router 的 SPA 内部跳转不会稳定更新 document.referrer，
  // 但会在 history.state.back 中记录上一条路由。
  if (backPath) {
    return true
  }

  // 检查历史记录长度和referrer
  // 如果历史记录长度小于等于1，或者没有referrer，说明可能是从外部链接直接进入
  return window.history.length > 1 && !!document.referrer
}

/**
 * @description: 判断上一页是否是中间页 /h5Rct
 * @param route 当前路由对象（可选），用于检查 query 参数
 * @return {boolean} 上一页是否是中间页
 */
export const isPreviousPageRedirect = (route?: { query?: Record<string, any> }): boolean => {
  // 优先从 window.history.state 获取上一页信息
  // Vue Router 会在 history.state 中存储路由状态信息
  if (window.history.state) {
    try {
      const state = window.history.state
      
      // Vue Router 在 history.state 中可能存储的结构：
      // - state.key: 路由的唯一标识
      // - state.current: 当前路由路径
      // - state.back: 上一页的路由信息
      // - 或者其他自定义字段
      
      // 检查 state 中是否有 back 字段（可能存储上一页信息）
      if (state.back) {
        const backPath = typeof state.back === 'string' ? state.back : state.back.path || state.back.fullPath
        if (backPath && backPath.includes('/h5Rct')) {
          return true
        }
      }
      
      // 检查 state 中是否有 previous 或 from 字段
      if (state.previous) {
        const prevPath = typeof state.previous === 'string' ? state.previous : state.previous.path || state.previous.fullPath
        if (prevPath && prevPath.includes('/h5Rct')) {
          return true
        }
      }
      
      if (state.from) {
        const fromPath = typeof state.from === 'string' ? state.from : state.from.path || state.from.fullPath
        if (fromPath && fromPath.includes('/h5Rct')) {
          return true
        }
      }
      
      // 检查 state 中是否有 previousLocation 字段
      if (state.previousLocation && typeof state.previousLocation === 'string' && state.previousLocation.includes('/h5Rct')) {
        return true
      }
      
      // 检查 state 中是否有 pathname 字段（可能存储当前或上一页路径）
      if (state.pathname && typeof state.pathname === 'string' && state.pathname.includes('/h5Rct')) {
        return true
      }
      
      // 检查整个 state 对象的字符串表示中是否包含 /h5Rct
      // 这可以作为兜底方案，检查是否有任何字段包含中间页路径
      const stateStr = JSON.stringify(state)
      if (stateStr.includes('/h5Rct')) {
        return true
      }
    } catch {
      // 如果解析失败，继续使用其他方法作为兜底
    }
  }
  
  // 检查当前路由的 query 参数中是否有 _fromRedirect 标记
  // 中间页跳转时会设置此标记
  if (route?.query?._fromRedirect === '1') {
    return true
  }
  
  // 兜底方案：检查 referrer 是否包含 /h5Rct
  // 中间页路径是 /h5Rct，如果 referrer 包含该路径，说明是从中间页跳转过来的
  if (document.referrer) {
    try {
      const referrerUrl = new URL(document.referrer)
      return referrerUrl.pathname.includes('/h5Rct')
    } catch {
      // 如果 URL 解析失败，使用字符串匹配作为兜底
      return document.referrer.includes('/h5Rct')
    }
  }
  
  return false
}
