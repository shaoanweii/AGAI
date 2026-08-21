/**
 * 全局类型声明文件
 * 只保留项目实际需要的模块声明
 */

/**
 * 基础 ID 类型
 * 用于组件 props 和 API 响应中的 ID 字段
 */
declare type ID = string | number

/**
 * 通用响应类型
 * 用于 API 接口的统一响应格式
 */
declare interface BaseResponse<T = any> {
  success: boolean
  message: string
  code: number | string
  result: T
  tid: string
}

// Vue 组件类型声明
declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

// 图片资源类型声明
declare module '*.png' {
  const src: string
  export default src
}

declare module '*.jpg' {
  const src: string
  export default src
}

declare module '*.jpeg' {
  const src: string
  export default src
}

declare module '*.gif' {
  const src: string
  export default src
}

declare module '*.svg' {
  const src: string
  export default src
}

declare module '*.webp' {
  const src: string
  export default src
}

// CSS 模块类型声明
declare module '*.module.css' {
  const classes: { [key: string]: string }
  export default classes
}

declare module '*.module.scss' {
  const classes: { [key: string]: string }
  export default classes
}

declare module '*.module.sass' {
  const classes: { [key: string]: string }
  export default classes
}

// JSON 文件类型声明
declare module '*.json' {
  const value: any
  export default value
}
