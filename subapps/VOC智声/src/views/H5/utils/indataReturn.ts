/**
 * H5 页面跳转至 indata 系统后返回时的本地标记结构。
 * - source 固定为 indata，便于后续扩展更多外部系统来源
 * - createdAt 用于控制标记有效期，避免普通前后台切换误触发登录态探测
 * - targetUrl 仅用于调试与排查，业务判断不依赖它
 */
export interface PendingIndataReturnFlag {
  source: 'indata'
  createdAt: number
  targetUrl?: string
}

export const PENDING_INDATA_RETURN_KEY = 'h5:pending-indata-return'
export const PENDING_INDATA_RETURN_TTL = 10 * 60 * 1000

const INDATA_PATH = '/indata'

/**
 * 安全获取 sessionStorage。
 * @returns 当前页面可用的 sessionStorage；不可用时返回 null
 */
const getSessionStorage = (): Storage | null => {
  if (typeof window === 'undefined') return null

  try {
    return window.sessionStorage
  } catch (error) {
    console.warn('获取 sessionStorage 失败：', error)
    return null
  }
}

/**
 * 判断链接是否指向 /indata 系统页面。
 * @param url 待判断的跳转链接
 * @returns 是否命中 /indata 路径
 */
export const isIndataUrl = (url: string): boolean => {
  if (!url) return false

  try {
    const parsedUrl = new URL(
      url,
      typeof window !== 'undefined' ? window.location.origin : 'https://placeholder.local'
    )
    const normalizedPath = parsedUrl.pathname.replace(/\/+$/, '') || '/'

    return normalizedPath === INDATA_PATH
  } catch {
    return /(^|https?:\/\/[^/]+)?\/indata(?:[/?#]|$)/.test(url)
  }
}

/**
 * 写入“待从 indata 返回”的本地标记。
 * @param targetUrl 后端返回的原始跳转链接，仅用于排查问题时辅助定位
 */
export const setPendingIndataReturnFlag = (targetUrl?: string): void => {
  const storage = getSessionStorage()
  if (!storage) return

  const payload: PendingIndataReturnFlag = {
    source: 'indata',
    createdAt: Date.now(),
    targetUrl
  }

  try {
    storage.setItem(PENDING_INDATA_RETURN_KEY, JSON.stringify(payload))
  } catch (error) {
    console.warn('写入 indata 返回标记失败：', error)
  }
}

/**
 * 清理“待从 indata 返回”的本地标记。
 * - 无论标记是否存在，都保证方法可安全调用
 */
export const clearPendingIndataReturnFlag = (): void => {
  const storage = getSessionStorage()
  if (!storage) return

  try {
    storage.removeItem(PENDING_INDATA_RETURN_KEY)
  } catch (error) {
    console.warn('清理 indata 返回标记失败：', error)
  }
}

/**
 * 读取并消费“待从 indata 返回”的本地标记。
 * - 命中有效标记时立即清理，确保 pageshow / focus / visibilitychange 只触发一次
 * - 标记不存在、结构异常或超时都会返回 false
 * @returns 是否命中一条仍有效的 indata 返回标记
 */
export const consumePendingIndataReturnFlag = (): boolean => {
  const storage = getSessionStorage()
  if (!storage) return false

  let rawValue = ''
  try {
    rawValue = storage.getItem(PENDING_INDATA_RETURN_KEY) || ''
  } catch (error) {
    console.warn('读取 indata 返回标记失败：', error)
    return false
  }

  if (!rawValue) return false

  clearPendingIndataReturnFlag()

  try {
    const payload = JSON.parse(rawValue) as PendingIndataReturnFlag
    if (payload?.source !== 'indata' || !Number.isFinite(payload?.createdAt)) {
      return false
    }

    return Date.now() - payload.createdAt <= PENDING_INDATA_RETURN_TTL
  } catch {
    return false
  }
}
