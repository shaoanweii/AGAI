import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'

// 导入各个store模块
import { useAppStore } from './modules/app'
import { useQueryStore } from './modules/query'
import { useUserStore } from './modules/user'
import { useGeneralScenarioStore } from './modules/generalScenario'

// 导入H5相关store模块
import { usePermissionsStore } from '../views/H5/store'

const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

export default pinia

// 导出store实例，方便在组件中使用
export {
  useAppStore,
  useQueryStore,
  useUserStore,
  useGeneralScenarioStore,
  usePermissionsStore
}
