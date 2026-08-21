import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'

// 导入 Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 导入 Vant
import Vant from 'vant'
import 'vant/lib/index.css'

import '@/styles/base.scss'
import './permission'

// 全局组件
import { setupGlobalComponents } from '@/components/global'
import directives from '@/directives'

import 'virtual:svg-icons-register'

import { useDeployAutoReload } from '@/hooks/useDeployAutoReload'

console.log('--->MODE', import.meta.env.MODE)

/**
 * 非生产调试控制台。
 * Vite build --mode test 下 import.meta.env.PROD 仍为 true，因此按 MODE 判断测试环境。
 */
const setupVConsole = async () => {
  if (import.meta.env.MODE === 'local-demo') return
  if (!import.meta.env.DEV && import.meta.env.MODE !== 'test') return

  const { default: VConsole } = await import('vconsole')
  new VConsole()
}

void setupVConsole()

useDeployAutoReload()

// 创建应用实例
const app = createApp(App)
app.use(directives)
// 使用插件
app.use(ElementPlus)
app.use(Vant)
app.use(router)
app.use(store)

// 注册 Element Plus 图标（排除与UI组件冲突的图标）
const conflictingIcons = ['SwitchButton'] // 与UI组件冲突的图标名称
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  if (!conflictingIcons.includes(key)) {
    app.component(key, component)
  }
}

// 注册全局组件（在Element Plus图标之后）
setupGlobalComponents(app)

// 挂载应用
app.mount('#app')
