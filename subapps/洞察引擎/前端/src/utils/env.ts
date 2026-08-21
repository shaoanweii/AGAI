/**
 * 环境工具函数
 * 只保留项目实际使用的基础功能
 */

/**
 * 判断是否为开发环境
 */
export const isDev = (): boolean => {
  return import.meta.env.DEV || ['build_dev', 'development'].includes(import.meta.env.MODE)
}

/**
 * 判断是否为生产环境
 */
export const isProd = (): boolean => {
  return import.meta.env.PROD
}
