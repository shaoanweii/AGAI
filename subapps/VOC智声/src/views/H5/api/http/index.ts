import { TOKEN_KEY } from '@/constants'
import axios from 'axios'
import router from '@/router'
import type {
  AxiosInstance,
  InternalAxiosRequestConfig,
  AxiosResponse,
  AxiosRequestConfig
} from 'axios'
import { showToast } from 'vant'
import { useH5ssoStore } from '../../store/sso'
import { isDev } from '@/utils/env'
import { isValidToken, removeToken } from '@/utils'

// 自定义请求配置，增加 cancelPrevious / silentError 开关
export type HttpRequestConfig = AxiosRequestConfig & {
  cancelPrevious?: boolean
  silentError?: boolean
}

/**
 * 判断当前请求是否应静默处理错误提示。
 * - 仅用于恢复探测等基础设施请求，避免把瞬时失败暴露给用户
 * - 不影响 401/100000 的 SSO 自动恢复逻辑
 */
const shouldSilenceError = (config?: AxiosRequestConfig | InternalAxiosRequestConfig): boolean => {
  return Boolean((config as HttpRequestConfig | undefined)?.silentError)
}

// SSO处理状态标记，防止重复调用
let isSsoProcessing = false

/**
 * 统一触发 H5 鉴权恢复流程。
 * - 复用现有 SSO 自动登录链路
 * - 增加短时间互斥，避免多个接口同时鉴权失败时重复触发
 * @param runner 具体的恢复动作
 */
const triggerH5SsoRecovery = (runner: () => Promise<void> | void) => {
  if (isSsoProcessing) return

  isSsoProcessing = true
  Promise.resolve(runner()).finally(() => {
    setTimeout(() => {
      isSsoProcessing = false
    }, 3000)
  })
}

// 记录同一路径正在进行中的请求，用于“同接口仅保留最后一次”
// key 规则：METHOD + 空格 + 去掉查询参数的 URL 路径。例如：GET /api/user/list
const pendingMap = new Map<string, AbortController>()

// 生成去重/取消 key（忽略查询参数，聚焦接口路径）
const getCancelKey = (config: InternalAxiosRequestConfig) => {
  const method = (config.method || 'get').toUpperCase()
  const rawUrl = config.url || ''
  const pathOnly = rawUrl.split('?')[0]
  return `${method} ${pathOnly}`
}

// 是否启用“同接口仅保留最后一次请求”逻辑
// 默认：GET 启用；可通过 config.cancelPrevious 显式开启/关闭
const shouldCancelPrevious = (config: InternalAxiosRequestConfig): boolean => {
  const m = (config.method || 'get').toUpperCase()
  const explicit = (config as any).cancelPrevious
  if (explicit === true) return true
  if (explicit === false) return false
  // 默认仅对 GET 开启，避免对提交类接口造成影响；文件下载场景（blob）默认跳过
  return m === 'GET' && config.responseType !== 'blob'
}

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem(TOKEN_KEY)
    if (isValidToken(token)) {
      config.headers = config.headers || {}
      config.headers['Authorization'] = 'Bearer ' + token
      // 同接口仅保留最后一次请求：在发起新请求前取消上一次
      if (shouldCancelPrevious(config)) {
        const key = getCancelKey(config)
        const prev = pendingMap.get(key)
        if (prev) {
          prev.abort('同接口发起新的请求，自动取消上一次')
        }
        const controller = new AbortController()
        if ((config as any).signal instanceof AbortSignal) {
          const extSignal: AbortSignal = (config as any).signal
          if (extSignal.aborted) {
            controller.abort('外部取消')
          } else {
            extSignal.addEventListener('abort', () => controller.abort('外部取消'))
          }
        }
        ;(config as any).signal = controller.signal
        pendingMap.set(key, controller)
        ;(config as any).__cancelKey = key
        ;(config as any).__abortController = controller
      }
    } else if (token) {
      // 清理历史遗留的 `{}` 等非法占位值，禁止发送 Bearer {}。
      removeToken()
    }
    // 在这里可以添加token等认证信息
    // config.headers.Authorization = `Bearer ${localStorage.getItem('token')}`
    // config.headers.Authorization = `Bearer 1234567890`
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    // 清理 pendingMap 中对应的请求（仅清理仍是自己占用的 key）
    try {
      const cfg: any = response.config || {}
      const key: string | undefined = cfg.__cancelKey
      const ctrl: AbortController | undefined = cfg.__abortController
      if (key && ctrl && pendingMap.get(key) === ctrl) {
        pendingMap.delete(key)
      }
    } catch (error: any) {
      console.log(error.message)
    }
    const ssoStore = useH5ssoStore()
    const { data } = response

    // 处理 Blob 类型数据（文件下载等场景）
    if (data instanceof Blob) {
      console.log('Blob 数据响应:', data.type, data.size)
      return data as any
    }

    // 根据后端接口返回的约定结构处理
    if (data.code === '200') {
      // 正常返回数据
      return data as any
    } else if (['100046', '100047'].includes(data.code)) {
      // 权限不足，跳转到无权限页面  100046 表示没有配置角色
      // 100047: 当前登录用户没有系统访问权限，请联系管理员
      router.replace('/h5NotAuth')
      return Promise.reject(new Error(data.message || '权限不足'))
    } else if (['100000'].includes(data.code)) {
      // 防止多个接口同时返回100000时重复处理
      triggerH5SsoRecovery(() => ssoStore.ssoByInterceptors())
      return Promise.reject(new Error('登录状态已过期，正在重新登录...'))
    } else if (['401'].includes(data.code)) {
      // 未授权，跳转到异常页面
      //跳转错误页面
      if (isDev()) {
        router.replace('/h5404')
      } else {
        triggerH5SsoRecovery(() => ssoStore.ssoH5ByEAC())
      }

      return Promise.reject(new Error(data.message || '未授权'))
    } else {
      // 业务处理错误
      if (!shouldSilenceError(response.config)) {
        showToast(data.message || '请求失败')
      }
      return Promise.reject(new Error(data.message || '未知错误'))
    }
  },
  error => {
    // 取消与清理：若为同接口新请求触发的主动取消，不弹错误
    try {
      const cfg: any = error.config || {}
      const key: string | undefined = cfg.__cancelKey
      const ctrl: AbortController | undefined = cfg.__abortController
      if (key && ctrl && pendingMap.get(key) === ctrl) {
        pendingMap.delete(key)
      }
    } catch (error: any) {
      console.log(error.message)
    }

    if (
      error?.code === 'ERR_CANCELED' ||
      error?.name === 'CanceledError' ||
      (typeof error?.message === 'string' && error.message.toLowerCase().includes('canceled'))
    ) {
      return Promise.reject(error)
    }

    // 处理http错误状态码
    let message = '请求失败'
    if (error.response) {
      const { status } = error.response
      const ssoStore = useH5ssoStore()
      switch (status) {
        case 401:
          message = '未授权，请重新登录'
          // 清除用户信息并跳转登录页
          localStorage.removeItem(TOKEN_KEY)
          removeToken()
          if (isDev()) {
            window.location.href = '/#/h5404'
          } else {
            triggerH5SsoRecovery(() => ssoStore.ssoH5ByEAC())
          }
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '请求地址出错'
          break
        case 429:
          message = error.response.data?.message || '请求过于频繁，请稍后再试'
          break
        case 500:
          message = '服务器内部错误'
          break
        default:
          message = `未知错误(${status})`
      }
    } else if (error.message.includes('timeout')) {
      message = '请求超时，请稍后再试'
    }

    if (!shouldSilenceError(error.config)) {
      showToast(message)
    }
    return Promise.reject(error)
  }
)

// 包装函数，确保返回 BaseResponse 类型
const request = <T = any>(config: HttpRequestConfig): Promise<BaseResponse<T>> => {
  return service(config).then((response: any) => {
    // 处理 Blob 类型数据（文件下载等场景）
    if (response instanceof Blob) {
      return {
        success: true,
        message: '文件下载成功',
        code: 200,
        result: response,
        tid: ''
      } as BaseResponse<T>
    }

    // 如果响应数据已经是 BaseResponse 格式，直接返回
    if (response && typeof response === 'object' && 'code' in response) {
      // 确保 code 是数字类型，符合 BaseResponse 定义
      const code = typeof response.code === 'string' ? parseInt(response.code) : response.code
      return {
        success: response.success !== undefined ? response.success : true,
        message: response.message || '请求成功',
        code: code,
        result: response.result || response.data,
        tid: response.tid || ''
      } as BaseResponse<T>
    }

    // 如果不是标准格式，包装成 BaseResponse
    return {
      success: true,
      message: '请求成功',
      code: 200,
      result: response,
      tid: ''
    } as BaseResponse<T>
  })
}

// 扩展 request 函数，添加常用的 HTTP 方法
request.get = <T = any>(url: string, config?: HttpRequestConfig): Promise<BaseResponse<T>> => {
  return request<T>({ ...config, method: 'GET', url })
}

request.post = <T = any>(
  url: string,
  data?: any,
  config?: HttpRequestConfig
): Promise<BaseResponse<T>> => {
  return request<T>({ ...config, method: 'POST', url, data })
}

request.put = <T = any>(
  url: string,
  data?: any,
  config?: HttpRequestConfig
): Promise<BaseResponse<T>> => {
  return request<T>({ ...config, method: 'PUT', url, data })
}

request.delete = <T = any>(url: string, config?: HttpRequestConfig): Promise<BaseResponse<T>> => {
  return request<T>({ ...config, method: 'DELETE', url })
}

request.patch = <T = any>(
  url: string,
  data?: any,
  config?: HttpRequestConfig
): Promise<BaseResponse<T>> => {
  return request<T>({ ...config, method: 'PATCH', url, data })
}

// 文件下载相关方法
request.download = (url: string, config?: HttpRequestConfig): Promise<BaseResponse<Blob>> => {
  return request<Blob>({
    ...config,
    method: 'GET',
    url,
    responseType: 'blob'
  })
}

request.downloadPost = (
  url: string,
  data?: any,
  config?: HttpRequestConfig
): Promise<BaseResponse<Blob>> => {
  return request<Blob>({
    ...config,
    method: 'POST',
    url,
    data,
    responseType: 'blob'
  })
}

export default request
