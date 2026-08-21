import { ref, onMounted, onUnmounted } from 'vue'

/**
 * 页面浏览时长记录Hook
 * 支持H5页面可见性变化（切换后台、息屏等）
 * @returns 浏览时长（秒）和获取浏览时长的方法
 */
export function useBrowseRecord() {
  const startTime = ref<number>(0)
  const totalTime = ref<number>(0)
  const isVisible = ref<boolean>(true)

  // 开始记录浏览时间
  const startRecord = () => {
    startTime.value = Date.now()
  }

  // 暂停记录（页面不可见时）
  const pauseRecord = () => {
    if (startTime.value > 0 && isVisible.value) {
      totalTime.value += Date.now() - startTime.value
      isVisible.value = false
    }
  }

  // 恢复记录（页面可见时）
  const resumeRecord = () => {
    if (!isVisible.value) {
      startTime.value = Date.now()
      isVisible.value = true
    }
  }

  // 获取浏览时长（秒）
  const getBrowseTime = (): number => {
    let currentTotal = totalTime.value
    if (startTime.value > 0 && isVisible.value) {
      currentTotal += Date.now() - startTime.value
    }
    return Math.floor(currentTotal / 1000)
  }

  // 页面可见性变化处理
  const handleVisibilityChange = () => {
    if (document.hidden) {
      pauseRecord()
    } else {
      resumeRecord()
    }
  }

  /**
   * 读取当前累计浏览时长，并按需立即切到下一段计时。
   * 用于列表切换等场景，避免等待异步上报时把耗时串到下一条数据。
   * @param restart 是否立即开始新的计时段
   * @returns 当前计时段的浏览时长（秒）
   */
  const consumeBrowseTime = (restart: boolean = false): number => {
    const browseTime = getBrowseTime()
    totalTime.value = 0
    startTime.value = restart && isVisible.value ? Date.now() : 0
    return browseTime
  }

  //重置计时
  const resetRecord = () => {
    startTime.value = Date.now()
    totalTime.value = 0
    isVisible.value = true
  }

  // 页面挂载时开始记录
  onMounted(() => {
    startRecord()
    document.addEventListener('visibilitychange', handleVisibilityChange)
    window.addEventListener('blur', pauseRecord)
    window.addEventListener('focus', resumeRecord)
  })

  // 页面卸载时清理事件监听
  onUnmounted(() => {
    document.removeEventListener('visibilitychange', handleVisibilityChange)
    window.removeEventListener('blur', pauseRecord)
    window.removeEventListener('focus', resumeRecord)
  })

  return {
    resetRecord,
    getBrowseTime,
    consumeBrowseTime,
    pauseRecord
  }
}
