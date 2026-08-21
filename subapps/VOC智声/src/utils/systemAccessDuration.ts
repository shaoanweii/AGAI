import { TOKEN_KEY, USER_ID_KEY, USER_NAME_KEY } from '@/constants'
import { isMobileEnvironment, isWeWorkEnvironment } from './environment'
import dayjs from 'dayjs'

/**
 * 用户系统访问时长采集客户端
 *
 * 设计目标：
 * - 统计“进入系统到离开系统”的整体时长（非单页面/非单文章）
 * - 允许同一用户 PC / 移动端并行会话，后端按 session 叠加
 * - 前端以心跳维持会话存活，结束事件尽量可靠（fetch keepalive）
 *
 * 注意：
 * - 这里刻意不依赖 axios/request，避免与登录/拦截器形成循环依赖
 * - 后端建议对 init/heartbeat 做幂等或 upsert，减少前端时序敏感性
 */

/**
 * 会话状态：
 * 1-未结束 2-已结束 3-异常兜底
 */
type AccessDurationStatus = 1 | 2 | 3

/**
 * 结束原因（前端约束到可控集合，避免随意传值导致行为不一致）
 * - logout/leave-h5/manual：认为用户明确离开系统，会上报 end 并清理 sessionStorage
 * - reload：刷新/关闭标签页等不可可靠识别的离开场景，统一不上报 end、不清理 sessionStorage（保持 sessionId 复用）；由后端根据心跳超时兜底结束
 */
type StopReason = 'logout' | 'leave-h5' | 'manual' | 'reload'

const ACCESS_DURATION_STATUS = {
  /** 未结束（会话存活） */
  NOT_ENDED: 1 as AccessDurationStatus,
  /** 已结束（正常结束） */
  ENDED: 2 as AccessDurationStatus,
  /** 异常兜底（无法判断/状态不一致时的降级） */
  FALLBACK: 3 as AccessDurationStatus
} as const

export interface SystemAccessDurationContext {
  userId?: string
  username?: string
  sessionId?: string
  device?: string
  browser?: string
}

export interface StartAccessDurationOptions extends SystemAccessDurationContext {
  /**
   * 心跳间隔（毫秒）
   * - 默认 30s
   * - 若后端兜底超时时间设置较长，可适当放大（例如 60s）降低压力
   */
  heartbeatIntervalMs?: number
}

const API_PATH = {
  init: '/report/user-system-access/init',
  heartbeat: '/report/user-system-access/heartbeat',
  end: '/report/user-system-access/end'
} as const

const DEFAULT_HEARTBEAT_INTERVAL_MS = 120_000

/**
 * 会话标识存储在 sessionStorage：
 * - 同一标签页（Tab）内复用（路由切换/组件卸载不会丢失）
 * - 关闭标签页/浏览器自动清除
 * - 登出/离开系统时主动清除，确保重新进入是新会话
 */
const SESSION_STORAGE_KEYS = {
  sessionId: 'report_access_session_id',
  startedAt: 'report_access_started_at'
} as const

/**
 * 统一使用本地时间字符串，避免不同端对时区的理解不一致
 * 格式：yyyy-MM-dd HH:mm:ss
 */
const formatDateTime = (d: Date) => dayjs(d).format('YYYY-MM-DD HH:mm:ss')

const getToken = () => localStorage.getItem(TOKEN_KEY) || ''

const buildApiUrl = (path: string) => {
  const base = (import.meta.env.VITE_API_BASE_URL || '/api') as string
  const baseTrim = base.endsWith('/') ? base.slice(0, -1) : base
  return `${baseTrim}${path}`
}

const postJson = async (url: string, method: 'POST' | 'PUT', body: any, keepalive = false) => {
  const token = getToken()
  if (!token) return

  try {
    await fetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json;charset=UTF-8',
        Authorization: `Bearer ${token}`
      },
      body: JSON.stringify(body || {}),
      // keepalive 用于卸载/跳转场景，提升请求被送达的概率（浏览器会限制 body 大小）
      keepalive
    })
  } catch (error) {
    // 这里不做 UI 提示，避免影响用户操作；只留日志便于排查
    console.warn('访问时长上报失败:', error)
  }
}

const isSuccessCode = (code: unknown) => code === 200 || code === '200'
const isAuthExpiredCode = (code: unknown) =>
  code === 401 || code === '401' || code === 100000 || code === '100000'

/**
 * 仅用于需要根据后端业务 code 判断是否成功的场景（例如 init 初始化）
 * - 不依赖 axios/request，避免与登录/拦截器形成循环依赖
 * - 失败时返回 null，并输出告警日志（不做 UI 提示）
 */
const postJsonWithResult = async (
  url: string,
  method: 'POST' | 'PUT',
  body: any,
  keepalive = false
): Promise<BaseResponse<any> | null> => {
  const token = getToken()
  if (!token) return null

  try {
    const response = await fetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json;charset=UTF-8',
        Authorization: `Bearer ${token}`
      },
      body: JSON.stringify(body || {}),
      keepalive
    })

    // init/heartbeat 这类接口通常是 JSON；若后端返回空 body 或非 JSON，则视为失败
    const data = (await response.json().catch(() => null)) as BaseResponse<any> | null
    return data
  } catch (error) {
    console.warn('访问时长上报失败:', error)
    return null
  }
}

const detectDevice = () => {
  // - “移动端&PC端同时登录时叠加”由后端按 session 聚合完成
  // - 前端仅尽量提供一个可辨识的 device 字段
  return isMobileEnvironment() ? 'h5' : 'pc'
}

const detectBrowser = () => {
  const ua = navigator?.userAgent || ''
  if (isWeWorkEnvironment()) return 'WeCom'
  if (/MicroMessenger/i.test(ua)) return 'WeChat'
  if (/Edg\//i.test(ua)) return 'Edge'
  if (/Chrome\//i.test(ua) && !/Edg\//i.test(ua)) return 'Chrome'
  if (/Safari\//i.test(ua) && !/Chrome\//i.test(ua)) return 'Safari'
  if (/Firefox\//i.test(ua)) return 'Firefox'
  return 'Unknown'
}

const generateSessionId = () => {
  try {
    if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
      return crypto.randomUUID()
    }
  } catch {
    // ignore：降级走后备方案
  }
  return `sid_${Math.random().toString(36).slice(2, 10)}_${Date.now()}`
}

const getSessionStorageItem = (key: string) => {
  try {
    return sessionStorage.getItem(key)
  } catch {
    return null
  }
}

const setSessionStorageItem = (key: string, value: string) => {
  try {
    sessionStorage.setItem(key, value)
  } catch {
    // ignore：部分环境可能禁用 sessionStorage
  }
}

const removeSessionStorageItem = (key: string) => {
  try {
    sessionStorage.removeItem(key)
  } catch {
    // ignore
  }
}

const getOrCreateStartedAt = () => {
  const existing = getSessionStorageItem(SESSION_STORAGE_KEYS.startedAt)
  if (existing) return existing

  const startedAt = formatDateTime(new Date())
  setSessionStorageItem(SESSION_STORAGE_KEYS.startedAt, startedAt)
  return startedAt
}

const ensureSessionId = (explicitSessionId?: string) => {
  const stored = getSessionStorageItem(SESSION_STORAGE_KEYS.sessionId)
  if (explicitSessionId) {
    // 外部透传 sessionId 时，以透传值为准，并落入 sessionStorage 供后续刷新复用
    if (stored !== explicitSessionId) {
      setSessionStorageItem(SESSION_STORAGE_KEYS.sessionId, explicitSessionId)
    }
    // 只要 sessionId 已存在（无论来源），就不触发 init；因此这里不认为是“新会话”
    return { sessionId: explicitSessionId, createdByClient: false }
  }

  if (stored) {
    return { sessionId: stored, createdByClient: false }
  }

  const sid = generateSessionId()
  setSessionStorageItem(SESSION_STORAGE_KEYS.sessionId, sid)
  return { sessionId: sid, createdByClient: true }
}

const clearSessionStorage = () => {
  removeSessionStorageItem(SESSION_STORAGE_KEYS.sessionId)
  removeSessionStorageItem(SESSION_STORAGE_KEYS.startedAt)
}

class SystemAccessDurationClient {
  private status: AccessDurationStatus = ACCESS_DURATION_STATUS.ENDED
  private heartbeatTimer: number | null = null
  private context: SystemAccessDurationContext = {}
  private readonly startedAt: { value?: string } = {}
  private heartbeatIntervalMs = DEFAULT_HEARTBEAT_INTERVAL_MS
  private endReported = false

  private shouldReportEnd(reason: StopReason) {
    // “刷新页面”不应触发 end；浏览器层面无法 100% 精准区分刷新与关闭页签
    // 因此这里仅在“业务明确离开系统”的场景上报 end，关闭页签遗漏由后端兜底。
    return reason === 'logout' || reason === 'manual' || reason === 'leave-h5'
  }

  private shouldClearSession(reason: StopReason) {
    return this.shouldReportEnd(reason)
  }

  private onVisibilityChange = () => {
    // 页面回到前台时补一个心跳，减少后台定时器被节流导致的误判
    if (document.visibilityState === 'visible') {
      this.heartbeat().catch(() => void 0)
    }
  }

  private onPageHide = (e: PageTransitionEvent) => {
    // pagehide 比 beforeunload 更适合移动端；这里统一按 reload 场景处理离开事件
    if (this.status !== ACCESS_DURATION_STATUS.NOT_ENDED) return

    // bfcache 场景：页面可能会被恢复，不应结束会话
    if (e?.persisted) return

    // 策略说明：
    // - 不尝试在前端区分“刷新 vs 关闭标签页”（浏览器无法稳定判断）
    // - 为避免刷新被误判成关闭，pagehide 统一按 reload 处理：不调 end、不清 sessionStorage
    // - 关闭标签页导致的会话结束，由后端基于 heartbeat 超时兜底
    this.stop('reload')
  }

  private onBeforeUnload = (e: BeforeUnloadEvent) => {
    // beforeunload 阶段不做结束上报：
    // - 浏览器无法可靠区分“刷新 vs 关闭标签页”
    // - 本模块选择宁可漏报关闭，相关兜底交由后端基于心跳超时处理
    void e
  }

  /**
   * 启动统计（幂等）
   * - 若已启动：仅更新上下文（例如后续补齐 userId）
   */
  start(options?: StartAccessDurationOptions) {
    if (options) {
      this.context = { ...this.context, ...options }
      if (options.heartbeatIntervalMs) {
        this.heartbeatIntervalMs = options.heartbeatIntervalMs
      }
    }

    if (this.status === ACCESS_DURATION_STATUS.NOT_ENDED) return

    const token = getToken()
    if (!token) {
      this.status = ACCESS_DURATION_STATUS.FALLBACK
      return
    }

    // 每次启动视为一个新会话（除非外部显式透传 sessionId）
    this.status = ACCESS_DURATION_STATUS.NOT_ENDED
    this.endReported = false
    // 尽量从本地缓存补齐用户信息，减少后端对入参的依赖
    this.context.userId = this.context.userId || localStorage.getItem(USER_ID_KEY) || undefined
    this.context.username = this.context.username || localStorage.getItem(USER_NAME_KEY) || undefined
    // sessionId 存在时复用（刷新不产生新会话）；不存在才创建并触发 init
    const { sessionId, createdByClient } = ensureSessionId(options?.sessionId)
    this.context.sessionId = sessionId
    this.context.device = this.context.device || detectDevice()
    this.context.browser = this.context.browser || detectBrowser()
    // startedAt 与 sessionId 绑定，按标签页会话复用
    this.startedAt.value = getOrCreateStartedAt()

    // 绑定生命周期监听：尽可能捕获“离开系统”时刻
    // 先解绑旧监听器，避免重复绑定
    try {
      document.removeEventListener('visibilitychange', this.onVisibilityChange)
      window.removeEventListener('pagehide', this.onPageHide)
      window.removeEventListener('beforeunload', this.onBeforeUnload)
      
      document.addEventListener('visibilitychange', this.onVisibilityChange)
      window.addEventListener('pagehide', this.onPageHide)
      window.addEventListener('beforeunload', this.onBeforeUnload)
    } catch (error) {
      console.warn('访问时长监听绑定失败:', error)
    }

    // 仅在“本标签页首次创建 sessionId”时调用 init：刷新复用 sessionId，不重复 init
    if (createdByClient) {
      this.init().catch(() => void 0)
    }

    // 立即心跳一次，让后端尽快拿到 heartbeat_last_time
    // this.heartbeat().catch(() => void 0)

    this.heartbeatTimer = window.setInterval(() => {
      this.heartbeat().catch(() => void 0)
    }, this.heartbeatIntervalMs)
  }

  /**
   * 停止统计（幂等）
   */
  stop(reason: StopReason = 'manual') {
    // 已结束的会话不重复上报
    if (this.status === ACCESS_DURATION_STATUS.ENDED) return

    const shouldReportEnd = this.shouldReportEnd(reason)
    const shouldClearSession = this.shouldClearSession(reason)

    // 未处于“未结束”状态时，视为异常兜底：尽力补发 end
    if (this.status !== ACCESS_DURATION_STATUS.NOT_ENDED) {
      this.status = ACCESS_DURATION_STATUS.FALLBACK

      const token = getToken()
      if (!token || !this.context.sessionId || this.endReported || !shouldReportEnd) {
        if (shouldClearSession) {
          clearSessionStorage()
        }
        this.status = ACCESS_DURATION_STATUS.ENDED
        return
      }

      this.endReported = true
      const endTime = formatDateTime(new Date())
      const fallbackPayload = {
        userId: this.context.userId,
        sessionId: this.context.sessionId,
        accessEndTime: endTime,
        reason, // 该字段接口暂未使用，传值便于排查问题
        status: ACCESS_DURATION_STATUS.FALLBACK
      }
      postJson(buildApiUrl(API_PATH.end), 'PUT', fallbackPayload, true).catch(() => void 0)

      // 异常兜底场景：只在明确的离开场景才清除会话
      if (shouldClearSession) {
        clearSessionStorage()
      }

      this.status = ACCESS_DURATION_STATUS.ENDED
      return
    }

    this.status = ACCESS_DURATION_STATUS.ENDED

    if (this.heartbeatTimer) {
      window.clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }

    try {
      document.removeEventListener('visibilitychange', this.onVisibilityChange)
      window.removeEventListener('pagehide', this.onPageHide)
      window.removeEventListener('beforeunload', this.onBeforeUnload)
    } catch (error) {
      console.warn('访问时长监听解绑失败:', error)
    }

    const endTime = formatDateTime(new Date())
    const payload = {
      userId: this.context.userId,
      sessionId: this.context.sessionId,
      accessEndTime: endTime,
      reason,
      status: ACCESS_DURATION_STATUS.ENDED
    }

    const url = buildApiUrl(API_PATH.end)

    // 结束上报：关闭标签页/离开系统场景用 keepalive 尽力送达
    if (shouldReportEnd) {
      if (!this.endReported) {
        this.endReported = true
        postJson(url, 'PUT', payload, true).catch(() => void 0)
      }
    }

    // 无论 end 是否成功送达，离开场景都清理本地会话标识，避免下一次进入复用旧会话
    if (shouldClearSession) {
      clearSessionStorage()
    }
  }

  /**
   * 主动更新上下文（例如补齐 userId / username）
   */
  updateContext(ctx: Partial<SystemAccessDurationContext>) {
    this.context = { ...this.context, ...ctx }
  }

  private async init(): Promise<boolean> {
    const payload = {
      userId: this.context.userId,
      sessionId: this.context.sessionId,
      accessStartTime: this.startedAt.value,
      device: this.context.device,
      browser: this.context.browser,
      status: ACCESS_DURATION_STATUS.NOT_ENDED
    }
    const result = await postJsonWithResult(buildApiUrl(API_PATH.init), 'POST', payload, false)
    const ok = isSuccessCode(result?.code)
    if (!ok) {
      console.log('[访问时长] init 未成功:', result)
    }
    return ok
  }

  private async heartbeat() {
    if (this.status !== ACCESS_DURATION_STATUS.NOT_ENDED) return

    const token = getToken()
    if (!token) {
      this.stop('reload')
      return
    }

    const payload = {
      userId: this.context.userId,
      sessionId: this.context.sessionId,
      heartbeatLastTime: formatDateTime(new Date()),
      status: ACCESS_DURATION_STATUS.NOT_ENDED
    }
    try {
      const response = await fetch(buildApiUrl(API_PATH.heartbeat), {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json;charset=UTF-8',
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify(payload)
      })

      let tokenExpired = response.status === 401
      if (!tokenExpired) {
        // 心跳接口按约定返回统一结构，解析失败时按非鉴权问题处理
        const data = (await response.json().catch(() => null)) as BaseResponse<any> | null
        tokenExpired = isAuthExpiredCode(data?.code)
      }

      if (tokenExpired) {
        console.warn('[访问时长] heartbeat 鉴权失效，停止后续心跳')
        this.stop('reload')
      }
    } catch (error) {
      console.warn('访问时长上报失败:', error)
    }
  }
}

export const systemAccessDuration = new SystemAccessDurationClient()
