import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  DataType,
  KeywordLibraryTab,
  CorpusMappingTab,
  DiscoveryTab,
  BrandSeriesTab,
  ClosedLoopRulesTab
} from '@/constant'

/**
 * 处理跨组件或者跨页面在全局的一些逻辑
 */
export const useMiddlewareStore = defineStore('middleware', () => {
  // 数据查询页面切换类型逻辑===================
  const dataType = ref<DataType>(DataType.RESULT)
  const keywordLibraryType = ref<KeywordLibraryTab>(KeywordLibraryTab.RULE)
  const corpusMappingType = ref<CorpusMappingTab>(CorpusMappingTab.TEXT)
  const discoveryType = ref<DiscoveryTab>(DiscoveryTab.TEXT)
  const closedLoopRulesType = ref<ClosedLoopRulesTab>(ClosedLoopRulesTab.SINGLE)
  const brandSeriesTab = ref<BrandSeriesTab>(BrandSeriesTab.SERIES)

  const setDataType = (type: DataType) => {
    dataType.value = type
  }

  const setKeywordLibraryType = (type: KeywordLibraryTab) => {
    keywordLibraryType.value = type
  }
  const setCorpusMappingType = (type: CorpusMappingTab) => {
    corpusMappingType.value = type
  }
  const setDiscoveryType = (type: DiscoveryTab) => {
    discoveryType.value = type
  }

  // 闭环规则页签切换统一放在中间态中，便于头部与内容区域联动。
  const setClosedLoopRulesType = (type: ClosedLoopRulesTab) => {
    closedLoopRulesType.value = type
  }

  const setBransSeriewType = (type: BrandSeriesTab) => {
    brandSeriesTab.value = type
  }

  return {
    dataType,
    keywordLibraryType,
    corpusMappingType,
    discoveryType,
    closedLoopRulesType,
    brandSeriesTab,
    setDataType,
    setKeywordLibraryType,
    setCorpusMappingType,
    setDiscoveryType,
    setClosedLoopRulesType,
    setBransSeriewType
  }
})

export default useMiddlewareStore
