import axios from 'axios'
import type { AxiosError, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import useUserStore from '@/stores/modules/user'
import { TOKEN_KEY } from '@/constant'
const instence = axios.create({
  // baseURL: '/api',
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 6000 * 10
})

type Response<T = any> = {
  code: string
  message: string
  result: T
}

type ErrorResponse = {
  code?: string
  message?: string
  success?: boolean
  tid?: string
}

export default <T>(config: AxiosRequestConfig) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers = config.headers || {}
    config.headers['Authorization'] = 'Bearer ' + token
  }
  return new Promise<Response<T>>((resolve, reject) => {
    instence
      .request<Response<T>>(config)
      .then((response: AxiosResponse<Response<T>>) => {
        // 文件流
        if (response.status === 200 && response.config.responseType === 'blob') {
          resolve(response as any)
        }
        const { code, message } = response.data
        if (code === '200') {
          resolve(response.data)
          // 登录失效处理
        } else if ([401, '401'].includes(code)) {
          ElMessage.error(message)
          resolve(response.data)
        } else if (
          ['100041', '100000', '100008'].includes(code) ||
          message === '没有携带Token信息'
        ) {
          ElMessage.error(message)
          useUserStore().linkLogin()
          reject(response.data)
        } else {
          message && ElMessage.error(message)
          reject(response.data)
          // resolve(response.data)
        }
      })
      .catch((error: AxiosError<ErrorResponse>) => {
        console.log('error', error)
        ElMessage.closeAll()
        // 429 表示请求被服务端限流，此时优先透传后端文案，避免被通用异常提示覆盖。
        if (error.response?.status === 429) {
          ElMessage.error(error.response.data?.message || '访问频率过高，请稍后重试')
          reject(error)
          return
        }
        ElMessage.error('服务异常！')
        reject(error)
      })
  })
}
