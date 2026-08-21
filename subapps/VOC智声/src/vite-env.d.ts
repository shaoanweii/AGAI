/// <reference types="vite/client" />

declare module 'virtual:svg-icons-register'

/**
 * 环境变量类型声明
 * 为项目中使用的所有环境变量提供类型支持
 */
interface ImportMetaEnv {
  /**
   * API 基础路径
   * @default '/api'
   * @example 'https://api.example.com'
   */
  readonly VITE_API_BASE_URL: string

  /**
   * 应用标题
   * @default 'VOC后台管理系统'
   */
  readonly VITE_APP_TITLE: string

  /**
   * 应用版本号
   * @default '1.0.0'
   */
  readonly VITE_APP_VERSION: string

  /**
   * 调试模式开关
   * @default 'false'
   * @example 'true' | 'false'
   */
  readonly VITE_DEBUG?: string

  /**
   * 构建模式
   * Vite 内置环境变量
   */
  readonly MODE: string

  /**
   * 基础路径
   * Vite 内置环境变量
   */
  readonly BASE_URL: string

  /**
   * 是否为生产环境
   * Vite 内置环境变量
   */
  readonly PROD: boolean

  /**
   * 是否为开发环境
   * Vite 内置环境变量
   */
  readonly DEV: boolean

  /**
   * 是否为SSR模式
   * Vite 内置环境变量
   */
  readonly SSR: boolean
}

/**
 * 扩展 ImportMeta 接口
 */
interface ImportMeta {
  readonly env: ImportMetaEnv
}
