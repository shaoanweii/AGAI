import { TOKEN_KEY } from '@/constants'
import axios from 'axios'
import type {
  AxiosInstance,
  InternalAxiosRequestConfig,
  AxiosResponse,
  AxiosRequestConfig
} from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store'
import { isDev } from '@/utils/env'
import { useH5ssoStore } from '@/views/H5/store/sso'

/**
 * PC 侧 EAC SSO 跳转定时器。
 * - 用于避免多个接口同时鉴权失败时重复注册跳转
 */
let eacSsoRedirectTimer: number | null = null

/**
 * PC 侧 EAC SSO 跳转状态。
 * - 首次触发后进入跳转中状态，避免重复弹窗与重复跳转
 */
let isEacSsoRedirecting = false

/**
 * 展示提示信息后，延时跳转到 EAC SSO 登录页。
 * - 复用现有 `linkLogOut` 封装，保持 token 清理与会话收尾逻辑一致
 * - 仅首次触发时生效，避免多个接口同时失败时出现多次提示与多次跳转
 * @param message 提示文案
 * @param delayMs 延时时长，默认 2000ms
 */
const redirectToEacSso = (message: string, delayMs: number = 2000) => {
  if (isEacSsoRedirecting) return

  isEacSsoRedirecting = true

  ElMessage.error({
    message,
    duration: delayMs
  })

  if (eacSsoRedirectTimer !== null) {
    window.clearTimeout(eacSsoRedirectTimer)
  }

  eacSsoRedirectTimer = window.setTimeout(() => {
    eacSsoRedirectTimer = null
    // useH5ssoStore().linkLogOut()
    useUserStore().linkLogin()
  }, delayMs)
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
    if (token) {
      config.headers = config.headers || {}
      config.headers['Authorization'] = 'Bearer ' + token
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
    const { data } = response

    // 处理 Blob 类型数据（文件下载等场景）
    if (data instanceof Blob) {
      console.log('Blob 数据响应:', data.type, data.size)
      return data as any
    }

    // Axios headers 的取值是联合类型，先收窄为字符串后再做内容类型判断
    const contentType = response.headers['content-type']
    const contentTypeText =
      typeof contentType === 'string'
        ? contentType
        : Array.isArray(contentType)
          ? contentType.join(',')
          : ''

    // 处理流数据（Stream）- 直接返回，与blob类型做相同处理
    if (
      contentTypeText.includes('application/octet-stream') ||
      contentTypeText.includes('text/event-stream') ||
      response.config.responseType === 'stream'
    ) {
      // console.log('responseresponse', response)

      console.log('流数据响应:', contentType)
      return data as any
    }

    // 根据后端接口返回的约定结构处理
    if (data.code === '200') {
      // 正常返回数据
      return data as any
    } else if (['100046', '100047'].includes(data.code)) {
      // 100046/100047 先展示提示，留出可读时间后再统一跳转到 EAC SSO
      const message = data.message || '登录状态异常'
      if (isDev()) {
        // 未授权，跳转到登录页
        useUserStore().logout()
      } else {
        redirectToEacSso(message)
      }
      return Promise.reject(new Error(message))
    } else if (['401', '100000'].includes(data.code)) {
      // 401/100000 同样先展示提示，再延时跳转到 EAC SSO，避免提示一闪而过
      const message = data.message || '未授权'
      if (isDev()) {
        // 未授权，跳转到登录页
        useUserStore().logout()
      } else {
        redirectToEacSso(message)
      }

      return Promise.reject(new Error(message))
    } else {
      // 业务处理错误
      const message = data.message || '请求失败'
      ElMessage.error(message)
      return Promise.reject(new Error(message))
    }
  },
  error => {
    // 如果是主动取消的请求，不显示错误提示
    if (axios.isCancel(error) || error.code === 'ERR_CANCELED') {
      return Promise.reject(error)
    }

    // 处理http错误状态码
    let message = '请求失败'
    if (error.response) {
      const { status } = error.response
      switch (status) {
        case 401:
          message = '未授权，请重新登录'
          // 清除用户信息并跳转登录页
          localStorage.removeItem(TOKEN_KEY)
          // window.location.href = '/#/login'
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

    ElMessage.error(message)

    return Promise.reject(error)
  }
)

// 包装函数，确保返回 BaseResponse 类型
const request = <T = any>(config: AxiosRequestConfig): Promise<BaseResponse<T>> => {
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

    // 处理流数据 - 与Blob类型做相同处理
    if (
      (response &&
        typeof response === 'object' &&
        response.constructor === Object &&
        Object.keys(response).length === 0) ||
      config.responseType === 'stream'
    ) {
      console.log('response', typeof response)

      return response
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
request.get = <T = any>(url: string, config?: AxiosRequestConfig): Promise<BaseResponse<T>> => {
  return request<T>({ ...config, method: 'GET', url })
}

request.post = <T = any>(
  url: string,
  data?: any,
  config?: AxiosRequestConfig
): Promise<BaseResponse<T>> => {
  return request<T>({ ...config, method: 'POST', url, data })
}

request.put = <T = any>(
  url: string,
  data?: any,
  config?: AxiosRequestConfig
): Promise<BaseResponse<T>> => {
  return request<T>({ ...config, method: 'PUT', url, data })
}

request.delete = <T = any>(url: string, config?: AxiosRequestConfig): Promise<BaseResponse<T>> => {
  return request<T>({ ...config, method: 'DELETE', url })
}

request.patch = <T = any>(
  url: string,
  data?: any,
  config?: AxiosRequestConfig
): Promise<BaseResponse<T>> => {
  return request<T>({ ...config, method: 'PATCH', url, data })
}

// 文件下载相关方法
request.download = (url: string, config?: AxiosRequestConfig): Promise<BaseResponse<Blob>> => {
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
  config?: AxiosRequestConfig
): Promise<BaseResponse<Blob>> => {
  return request<Blob>({
    ...config,
    method: 'POST',
    url,
    data,
    responseType: 'blob'
  })
}

// 流数据请求方法
request.stream = <T = any>(url: string, config?: AxiosRequestConfig): Promise<BaseResponse<T>> => {
  return request<T>({
    ...config,
    method: 'GET',
    url,
    responseType: 'stream'
  })
}

request.streamPost = <T = any>(
  url: string,
  data?: any,
  config?: AxiosRequestConfig
): Promise<BaseResponse<T>> => {
  return request<T>({
    ...config,
    method: 'POST',
    url,
    data,
    responseType: 'stream'
  })
}

export default request
