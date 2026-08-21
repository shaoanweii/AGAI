import { createPinia } from 'pinia'
import useAppStore from '@/stores/modules/app'

const store = createPinia()

export { useAppStore }
export default store
