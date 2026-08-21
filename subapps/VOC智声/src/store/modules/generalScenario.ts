import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 通用场景逻辑处理
 */
export const useGeneralScenarioStore = defineStore('generalScenario', () => {
  // ==================== 状态定义 ====================
  const visible = ref<boolean>(false)
  const componentName = ref<string>('')

  // 旅途分析
  const journeyPageHeadTag = ref<any>({
    title: undefined
  })

  // ==================== 计算属性 ====================

  // ==================== 方法定义 ====================

  const setJourneyPageHeadTag = (tagInfo: any) => {
    journeyPageHeadTag.value = tagInfo
  }

  const handleOpen = (cName: string = 'GeneralScenario') => {
    visible.value = true
    componentName.value = cName
  }

  const handleClose = () => {
    visible.value = false
  }

  // 切换到不同的组件
  const switchComponent = (cName: string) => {
    componentName.value = cName
  }
  // ==================== 返回 Store 接口 ====================

  return {
    visible,
    componentName,
    handleOpen,
    handleClose,
    switchComponent,
    journeyPageHeadTag,
    setJourneyPageHeadTag
  }
})

export default useGeneralScenarioStore
