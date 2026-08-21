import type { App } from 'vue'
import { authDirective } from '@/directives/auth'

export default {
  install(Vue: App) {
    Vue.directive('auth', authDirective)
  }
}
