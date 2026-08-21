/**
 * 环境工具函数
 * 只保留项目实际使用的基础功能
 */

/**
 * 判断是否为开发环境
 */
export const isDev = (): boolean => {
  return import.meta.env.DEV || ['development', 'local-demo'].includes(import.meta.env.MODE)
}

/**
 * 判断是否为完全离线的本地演示模式。
 */
export const isLocalDemo = (): boolean => {
  return import.meta.env.VITE_RUNTIME_MODE === 'local-demo' || import.meta.env.MODE === 'local-demo'
}

/**
 * 判断是否为测试环境
 */
export const isTest = (): boolean => {
  return import.meta.env.MODE === 'test'
}

/**
 * 判断是否为生产环境
 */
export const isProd = (): boolean => {
  return import.meta.env.PROD
}
