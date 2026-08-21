import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from '@/stores'

// 相关样式引入
// import './style/global.scss'
// import './style/root.scss'
// import './style/mainTable.scss'
// import './style/utilities.scss'
import '@/assets/iconfont/iconfont.css'
import './permission'

// Element Plus 样式
import 'element-plus/dist/index.css'

import 'virtual:svg-icons-register'

// import './style/element-plus-reset.scss'

import globalComponents from '@/components/global'
// 引入Message组件
import { ElMessage } from 'element-plus'
import directives from '@/directives'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import '@/style/base.scss'
import { useDeployAutoReload } from '@/hooks/useDeployAutoReload'

useDeployAutoReload()

const app = createApp(App)

// 注册 Element Plus 图标（排除与UI组件冲突的图标）
const conflictingIcons: any[] = [] // 与UI组件冲突的图标名称
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  if (!conflictingIcons.includes(key)) {
    app.component(key, component)
  }
}

app.config.globalProperties.$message = ElMessage
app.use(directives)
app.use(store)
app.use(router)
app.use(globalComponents)

app.mount('#app')
