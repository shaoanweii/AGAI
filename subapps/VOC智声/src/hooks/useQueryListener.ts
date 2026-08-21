import { onMounted, onUnmounted } from 'vue'
import { throttle } from 'lodash-es'
import { useQueryStore } from '@/store/modules/query'

/**
 * 监听查询参数变化的 Hook
 * @param refreshCallback 查询参数变化时的回调函数
 * @param actions 需要监听的 action 名称数组，默认监听 updateQueryParams
 * @param throttleDelay 节流延迟时间（毫秒），默认300ms
 * @param autoRefreshOnMount 是否在组件挂载时自动触发刷新，默认true
 */
export function useQueryListener(
  refreshCallback: () => void,
  actions: string[] = ['updateQueryParams'],
  throttleDelay: number = 300,
  autoRefreshOnMount: boolean = true
) {
  const queryStore = useQueryStore()
  let unsubscribe: (() => void) | null = null
  let isListenerActive = false // 监听器激活标志，防止初始化阶段的重复调用

  // 使用lodash节流函数，确保最新参数能被获取到
  const throttledCallback = throttle(refreshCallback, throttleDelay, {
    leading: false, // 不在开始时执行
    trailing: true // 在结束时执行，确保获取最新参数
  })

  onMounted(() => {
    // 监听指定的 action 执行
    unsubscribe = queryStore.$onAction(({ args, name, after }) => {
      // 只在监听器激活后才响应 action，防止初始化阶段的重复调用
      if (actions.includes(name) && isListenerActive) {
        // console.log(`action ${name} 被调用，参数：`, args)
        after(result => {
          // console.log(`action ${name} 执行成功，返回值：`, result)
          throttledCallback()
        })
      }
    })

    // 根据参数决定是否在初始化时触发
    if (autoRefreshOnMount) {
      setTimeout(() => {
        throttledCallback()
        // 初始化完成后激活监听器
        isListenerActive = true
      }, 100)
    } else {
      // 禁用自动刷新时，延迟激活监听器，避免 SCHeader 初始化时的 updateQueryParams 被响应
      // 这样可以让 handleHeaderInitComplete 中的 refreshAllData 作为唯一的初始化调用
      setTimeout(() => {
        isListenerActive = true
      }, 500)
    }
  })

  onUnmounted(() => {
    // 取消节流函数的待执行调用
    throttledCallback.cancel()
    if (unsubscribe) {
      unsubscribe()
    }
  })

  return {
    queryStore,
    throttledCallback
  }
}
