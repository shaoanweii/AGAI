import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  CDESinglePointEventPageType,
  OriginalDataType,
  SceneManagementTab,
  EventAnalyType
} from '@/constants'
import { QueryType } from '@/constants'

/**
 * 处理跨组件或者跨页面在全局的一些逻辑
 */
export const useMiddlewareStore = defineStore('middleware', () => {
  // 数据查询页面切换类型逻辑===================
  const singlePointEventPageType = ref<CDESinglePointEventPageType>(CDESinglePointEventPageType.All)
  const batchEventPageType = ref<CDESinglePointEventPageType>(CDESinglePointEventPageType.All)

  const setSinglePointEventPageType = (type: CDESinglePointEventPageType) => {
    singlePointEventPageType.value = type
  }

  const setBatchEventPageType = (type: CDESinglePointEventPageType) => {
    batchEventPageType.value = type
  }

  const sceneManagementType = ref<SceneManagementTab>(SceneManagementTab.SCENE)

  const setSceneManagementType = (type: SceneManagementTab) => {
    sceneManagementType.value = type
  }
  // 闭环评价
  const closedLoopType = ref<EventAnalyType>(EventAnalyType.SingleEventAnaly)
  const setClosedLoopType = (type: EventAnalyType) => {
    closedLoopType.value = type
  }

  const originalDataType = ref<OriginalDataType>(OriginalDataType.ResultData)
  const setOriginalDataType = (type: OriginalDataType) => {
    originalDataType.value = type
  }

  const brandServiceCategoryType = ref<QueryType>(QueryType.Brand)
  const setBrandServiceCategoryType = (type: QueryType) => {
    brandServiceCategoryType.value = type
  }

  return {
    closedLoopType,
    setClosedLoopType,
    singlePointEventPageType,
    batchEventPageType,
    setSinglePointEventPageType,
    setBatchEventPageType,
    sceneManagementType,
    setSceneManagementType,
    originalDataType,
    setOriginalDataType,
    brandServiceCategoryType,
    setBrandServiceCategoryType
  }
})

export default useMiddlewareStore
