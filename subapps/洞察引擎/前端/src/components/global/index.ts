import type { App } from 'vue'
import SvgIcon from '@/components/SvgIcon/index.vue'
import AppDialog from '@/components/AppDialog.vue'
import FFilterLayout from '@/components/UI/FFilterLayout/index.vue'

export default {
  install(_app: App) {
    // Element Plus 组件会通过自动导入插件注册
    // 这里可以注册其他全局组件
    _app.component('SvgIcon', SvgIcon)
    _app.component('AppDialog', AppDialog)
    _app.component('FFilterLayout', FFilterLayout)
  }
}
