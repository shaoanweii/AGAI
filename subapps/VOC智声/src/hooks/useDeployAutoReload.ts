const DEPLOY_AUTO_RELOAD_KEY = '__deploy_auto_reload_timestamp__'
const DEPLOY_AUTO_RELOAD_COOLDOWN = 30 * 1000

let hasRegisteredDeployAutoReload = false

/**
 * 检测是否为发布后旧资源失效错误
 * @param message 错误消息文本
 * @returns 是否命中 chunk 失效特征
 */
const isChunkLoadError = (message: string) => {
  const normalizedMessage = message.toLowerCase()
  const chunkErrorSignatures = [
    'failed to fetch dynamically imported module',
    'loading chunk',
    'chunkloaderror',
    'importing a module script failed'
  ]

  return chunkErrorSignatures.some(signature => normalizedMessage.includes(signature))
}

/**
 * 发布后旧资源失效时自动刷新（带冷却，避免死循环）
 */
const reloadForDeployUpdate = () => {
  try {
    const now = Date.now()
    const lastReloadAt = Number(sessionStorage.getItem(DEPLOY_AUTO_RELOAD_KEY) || '0')
    if (lastReloadAt && now - lastReloadAt < DEPLOY_AUTO_RELOAD_COOLDOWN) {
      return
    }

    sessionStorage.setItem(DEPLOY_AUTO_RELOAD_KEY, String(now))
    window.location.reload()
  } catch (error) {
    console.warn('自动刷新失败，执行兜底刷新:', error)
    window.location.reload()
  }
}

/**
 * 注册全局监听，统一处理发布后 chunk 失效
 */
export const useDeployAutoReload = () => {
  if (hasRegisteredDeployAutoReload) {
    return
  }

  hasRegisteredDeployAutoReload = true

  window.addEventListener('vite:preloadError', event => {
    event.preventDefault()
    reloadForDeployUpdate()
  })

  window.addEventListener('unhandledrejection', event => {
    const reason = (event.reason as Error | string | undefined) ?? ''
    const message = typeof reason === 'string' ? reason : reason?.message || ''
    if (isChunkLoadError(message)) {
      event.preventDefault()
      reloadForDeployUpdate()
    }
  })

  window.addEventListener(
    'error',
    event => {
      const target = event.target
      if (target instanceof HTMLScriptElement && isChunkLoadError(target.src || '')) {
        reloadForDeployUpdate()
        return
      }

      if (isChunkLoadError(event.message || '')) {
        reloadForDeployUpdate()
      }
    },
    true
  )
}
