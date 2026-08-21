import { onActivated, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { usePermissionsStore } from '@h5/store'
import { recordMenuVisit } from '@/utils/operationLog'

/**
 * H5 端页面访问记录上报（菜单/页面访问）
 *
 * 约定：
 * - “返回/切换 tab”依赖 keep-alive 场景触发 onActivated，因此默认以 onActivated 作为主要触发时机
 * - 对于非 keepAlive 页面（每次进入都会重新挂载），用 onMounted 兜底首次进入
 * - 失败不影响主流程
 */

export interface UseH5MenuVisitRecordOptions {
  /**
   * frontRouting 的取值方式：
   * - 'path'：与当前存量保持一致（默认）
   * - 'fullPath'：包含 query，便于后续精细化统计
   */
  frontRoutingType?: 'path' | 'fullPath'
  /**
   * 是否启用上报（某些页面可能希望按条件关闭）
   */
  enabled?: () => boolean
  /**
   * 去重窗口（毫秒）
   * - 避免某些极端情况下同一次激活触发多次上报
   * - 注意：不影响“返回/切换 tab”场景的正常统计（时间间隔一般远大于该窗口）
   */
  dedupeMs?: number
}

const DEFAULT_OPTIONS: Required<Pick<UseH5MenuVisitRecordOptions, 'frontRoutingType' | 'dedupeMs'>> = {
  frontRoutingType: 'path',
  dedupeMs: 300
}

const getMenuIdByRouteName = (menus: any[], routeName: unknown) => {
  if (!menus?.length || routeName === undefined || routeName === null) return ''
  const routeNameKey = String(routeName)
  if (!routeNameKey) return ''
  const hit = menus.find((m: any) => String(m?.permissionKey ?? '') === routeNameKey)
  return hit?.id || ''
}

/**
 * 在页面 setup 中调用一次即可：
 * - keepAlive 页面：每次激活都会上报
 * - 非 keepAlive 页面：首次挂载会上报
 */
export const useH5MenuVisitRecord = (options: UseH5MenuVisitRecordOptions = {}) => {
  const route = useRoute()
  const userPermStore = usePermissionsStore()

  const lastReportKey = ref('')
  const lastReportAt = ref(0)

  const shouldEnable = () => {
    try {
      return options.enabled ? options.enabled() : true
    } catch (error) {
      console.warn('H5访问记录 enabled 判断失败:', error)
      return true
    }
  }

  const buildFrontRouting = () => {
    const type = options.frontRoutingType || DEFAULT_OPTIONS.frontRoutingType
    return type === 'fullPath' ? (route.fullPath || route.path || '') : (route.path || '')
  }

  const report = () => {
    if (!shouldEnable()) return

    const frontRouting = buildFrontRouting()
    const key = `${String(route.name ?? '')}__${frontRouting}`
    const now = Date.now()
    const dedupeMs = options.dedupeMs ?? DEFAULT_OPTIONS.dedupeMs

    if (key && key === lastReportKey.value && now - lastReportAt.value < dedupeMs) return
    lastReportKey.value = key
    lastReportAt.value = now

    const menuId = getMenuIdByRouteName(userPermStore.menus || [], route.name)

    recordMenuVisit({
      visitUrl: window.location.href,
      frontRouting,
      menuName: (route.meta as any)?.title || '',
      menuId
    }).catch(() => void 0)
  }

  // keep-alive 页面：用激活时机覆盖“返回/切换 tab”
  onActivated(() => {
    if (route.meta?.keepAlive) {
      report()
    }
  })

  // 非 keep-alive 页面：首次进入用 mounted 即可
  onMounted(() => {
    if (!route.meta?.keepAlive) {
      report()
    }
  })

  return { report }
}
