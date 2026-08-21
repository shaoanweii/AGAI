import { TOKEN_KEY } from '@/constants'

/**
 * 操作记录 - 菜单/页面访问上报
 *
 * 设计原则：
 * - 不依赖 axios/request：避免拦截器弹窗影响用户操作，也避免与权限/登录形成循环依赖
 * - 失败只做控制台告警，不阻断主流程
 * - 入参字段按接口约定补齐，缺省统一传空串
 */

export interface MenuVisitRecordPayload {
  /** 访问 URL（建议为完整地址，包含 hash） */
  visitUrl?: string
  /** 菜单ID：来自 userPermissions 返回的 menu.id；无则传空 */
  menuId?: string
  /** 菜单名称：来自路由 meta.title */
  menuName?: string
  /** 前端路由：route.fullPath */
  frontRouting?: string
}

const API_PATH = '/report/operationLog/menuVisitRecord'

const getToken = () => localStorage.getItem(TOKEN_KEY) || ''

const buildApiUrl = (path: string) => {
  const base = (import.meta.env.VITE_API_BASE_URL || '/api') as string
  const baseTrim = base.endsWith('/') ? base.slice(0, -1) : base
  return `${baseTrim}${path}`
}

const toSafeString = (val: unknown) => (val === undefined || val === null ? '' : String(val))

/**
 * 上报菜单/页面访问记录（fire-and-forget）
 */
export const recordMenuVisit = async (payload: MenuVisitRecordPayload) => {
  const token = getToken()
  if (!token) return

  const body = {
    visitUrl: toSafeString(payload?.visitUrl),
    menuId: toSafeString(payload?.menuId),
    menuName: toSafeString(payload?.menuName),
    frontRouting: toSafeString(payload?.frontRouting)
  }

  try {
    await fetch(buildApiUrl(API_PATH), {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json;charset=UTF-8',
        Authorization: `Bearer ${token}`
      },
      body: JSON.stringify(body)
    })
  } catch (error) {
    // 操作记录属于非关键链路：失败不提示用户，避免干扰正常使用
    console.warn('操作记录上报失败:', error)
  }
}

