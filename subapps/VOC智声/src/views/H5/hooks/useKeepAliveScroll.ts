import { ref, onMounted, onActivated, onDeactivated, nextTick, getCurrentInstance } from 'vue'
import { useRoute, onBeforeRouteLeave, onBeforeRouteUpdate } from 'vue-router'

interface KeepAliveScrollOptions {
  // 自定义滚动容器选择器，默认 HPage 内部容器
  selector?: string
  // 自定义获取容器的方法，优先级高于 selector
  getContainer?: () => HTMLElement | null
  // 缓存键，默认使用路由 name
  cacheKey?: string
  // 动态缓存键（优先级高于 cacheKey），用于同一路由不同参数/Query 的场景
  getCacheKey?: (route: any) => string
  // 恢复滚动时的行为
  behavior?: ScrollBehavior
  // 首次 onMounted 是否尝试恢复，默认 true
  restoreOnMounted?: boolean
}

// 位置缓存（进程级内存，不跨页面刷新）
const positionStore = new Map<string, number>()

export function useKeepAliveScroll(options: KeepAliveScrollOptions = {}) {
  const route = useRoute()
  const selector = options.selector ?? '.f-page__content'
  const behavior = options.behavior ?? 'auto'
  const staticCacheKey = options.cacheKey ?? (route.name ? String(route.name) : location.pathname)

  const containerRef = ref<HTMLElement | null>(null)
  const vm = getCurrentInstance()

  const resolveCacheKey = (r: any = route) => {
    if (typeof options.getCacheKey === 'function') {
      try {
        const key = options.getCacheKey(r)
        if (key) return String(key)
      } catch {
        // 忽略缓存键计算异常
      }
    }
    return String(staticCacheKey)
  }

  // 获取滚动容器
  const resolveContainer = (): HTMLElement | null => {
    // 1) 外部显式传入的方法优先
    if (typeof options.getContainer === 'function') {
      try {
        const el = options.getContainer()
        if (el) return el
      } catch {
        // 忽略容器获取异常
      }
    }
    // 2) 在当前组件子树内查找，避免与其他页面同名容器冲突
    const root = (vm?.proxy as any)?.$el as HTMLElement | null
    if (root) {
      const local = root.querySelector(selector) as HTMLElement | null
      if (local) return local
    }
    // 3) 兜底全局查找（不推荐，仅在无法定位本地容器时使用）
    return document.querySelector(selector) as HTMLElement | null
  }

  // 恢复滚动位置
  const restore = (r: any = route) => {
    const el = containerRef.value ?? resolveContainer()
    containerRef.value = el
    if (!el) return
    const cacheKey = resolveCacheKey(r)
    const pos = positionStore.get(cacheKey)
    el.scrollTo({ top: typeof pos === 'number' ? pos : 0, behavior })
  }

  // 记录滚动位置
  const record = (r: any = route) => {
    const el = containerRef.value ?? resolveContainer()
    containerRef.value = el
    if (!el) return
    const cacheKey = resolveCacheKey(r)
    positionStore.set(cacheKey, el.scrollTop || 0)
  }

  onMounted(() => {
    nextTick(() => {
      if (options.restoreOnMounted !== false) restore()
    })
  })

  onActivated(() => {
    nextTick(restore)
  })

  onDeactivated(() => {
    // record()
  })

  // 离开当前路由前也记录一次，确保在容器切换前拿到正确位置
  onBeforeRouteLeave(() => {
    record()
  })

  // 同一路由组件复用（仅参数/Query 变化）时，记录旧页位置并恢复新页位置
  onBeforeRouteUpdate((to, from) => {
    record(from)
    nextTick(() => restore(to))
  })

  return {
    containerRef,
    restore,
    record,
    // 便于调试或外部控制
    get position() {
      return positionStore.get(resolveCacheKey(route)) ?? 0
    },
    setPosition(v: number) {
      positionStore.set(resolveCacheKey(route), v)
    }
  }
}
