import type { App } from 'vue'
import FEcharts from './Charts/FEcharts/index.vue'

/**
 * 自动导入 UI 目录下的所有组件
 * 使用 Vite 的 import.meta.glob 功能动态导入组件
 */
const uiComponents = import.meta.glob('./UI/*/index.vue', { eager: true })

/**
 * 全局组件注册
 * @param app Vue应用实例
 */
export function setupGlobalComponents(app: App) {
  // 自动注册 UI 目录下的所有组件
  Object.entries(uiComponents).forEach(([path, module]) => {
    // 从路径中提取组件名称
    // 例如: './UI/FCard/index.vue' -> 'FCard'
    const componentName = path.replace('./UI/', '').replace('/index.vue', '')

    // 获取组件的默认导出
    const component = (module as any).default

    if (component && componentName) {
      // 检查组件是否已经注册，避免重复注册
      if (!app._context.components[componentName]) {
        // 注册为全局组件
        app.component(componentName, component)
        // console.log(`✅ 全局组件已注册: ${componentName}`)
      }
    }
  })

  // 手动注册其他非 UI 目录的组件
  if (!app._context.components['FEcharts']) {
    app.component('FEcharts', FEcharts)
    // console.log('✅ 全局组件已注册: FEcharts')
  }
}

/**
 * 获取所有已注册的 UI 组件
 * @returns 组件名称数组
 */
export function getRegisteredUIComponents(): string[] {
  return Object.keys(uiComponents).map(path => path.replace('./UI/', '').replace('/index.vue', ''))
}

// 导出 FEcharts 组件，方便按需引入
export { FEcharts }
