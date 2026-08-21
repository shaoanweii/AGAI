import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 全局处理取消请求的逻辑
 */
export const useGlobalCancelRequestStore = defineStore('globalCancelRequest', () => {
  // 需要取消的请求数组
  const globalCancelRequestList = ref<(() => void)[]>([])

  // 添加需要取消的请求
  const addCancelRequest = (cancel: () => void) => {
    globalCancelRequestList.value.push(cancel)
  }

  // 取消所有请求
  const cancelAllRequests = () => {
    globalCancelRequestList.value.forEach(cancel => {
      try {
        cancel()
      } catch (error) {
        console.error('取消请求失败:', error)
      }
    })
    globalCancelRequestList.value = []
  }

  // 清空请求列表
  const clearRequests = () => {
    globalCancelRequestList.value = []
  }

  return {
    addCancelRequest,
    cancelAllRequests,
    clearRequests,
    globalCancelRequestList
  }
})

export default useGlobalCancelRequestStore
