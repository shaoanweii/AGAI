<script setup lang="ts">
import VoiceList from '@/components/Business/VoiceListPanel/index.vue'
import UniversaFilter from '@/components/Business/UniversaFilter/index.vue'
import SentimentBranchData from './components/SentimentBranchData.vue'
import useMiddlewareStore from '@/store/modules/middleware'
import { useQueryStore } from '@/store/modules/query'
import { OriginalDataType, TagType } from '@/constants'
import { ref, watch, onBeforeUnmount, onUnmounted, onMounted } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'
import {
  exportOriginalVoiceData,
  exportSentimentBranchVoiceData,
  exportVocSounds
} from '@/api/downloadTask'
import { useDownloadAction } from '@/hooks/useDownloadAction'
import { findFinalTagLibClientVoListByTagId } from '@/api/common'
import type { BatchEventTopicOption } from '@/api/batchEvent/types'

defineOptions({
  name: 'OriginalSoundQuery'
})

const middlewareStore = useMiddlewareStore()
const queryStore = useQueryStore()
const { downloading: exportLoading, downloadByRequest } = useDownloadAction()
// queryStore.setPageDefaultFilter(route.name as string)
const resultDataSearchParams = ref<any>({})
const OriginalDataSearchParams = ref<any>({})
const resultDataStandardTopicOptions = ref<BatchEventTopicOption[]>([])

/**
 * 结果数据页签保留声音管理能力，继续支持高质量筛选、标记与纠错。
 */
const resultDataVoiceListProps = {
  showSortSelect: true,
  showHighQualityFilter: true,
  enableHighQualityActions: true,
  enableHighQualityInfo: true,
  enableVoiceManagementParams: true,
  enableErrorCorrection: true,
  showBatchAction: true,
  enableEventIssueAction: true,
  eventIssueDataSourceType: 'RESULT' as const,
  showCorpusCreateAction: true
}

/**
 * 原始数据页签恢复为只读查看模式，仅保留数据类型筛选与详情查看。
 */
const originalDataVoiceListProps = {
  showSortSelect: true,
  showDataStatus: false,
  showHighQualityFilter: false,
  enableHighQualityActions: false,
  enableHighQualityInfo: false,
  enableVoiceManagementParams: false,
  enableErrorCorrection: false,
  showBatchAction: true,
  enableEventIssueAction: true,
  eventIssueDataSourceType: 'ORIGINAL' as const,
  showCorpusCreateAction: true,
  showTopicsInList: false,
  detailSource: 'list' as const,
  showTopicsInDetail: false,
  showBrandSeriesInDetail: false,
  showRelationEventsInDetail: false,
  showKeywordSearch: true
}

/**
 * 归一化结果数据标准观点下拉同源选项，供原始数据事件下发聚焦观点复用。
 *
 * @param options 标准观点接口返回数据
 * @returns 事件下发弹窗可消费的聚焦观点选项
 */
const normalizeStandardTopicOptions = (options: any[] = []): BatchEventTopicOption[] => {
  const optionMap = new Map<string, BatchEventTopicOption>()

  options.forEach(option => {
    const code = String(option?.tagCode ?? '').trim()
    const name = String(option?.tagName ?? '').trim()
    if (!code || !name || optionMap.has(code)) return

    optionMap.set(code, { code, name })
  })

  return [...optionMap.values()]
}

/**
 * 加载结果数据筛选中“标准观点”下拉的全量数据源。
 * 原始数据事件下发聚焦观点直接复用该数据源，不依赖结果数据是否查询或是否选择。
 */
const loadResultDataStandardTopicOptions = async () => {
  try {
    const response = await findFinalTagLibClientVoListByTagId({
      tagType: TagType.Domain
    })
    resultDataStandardTopicOptions.value = normalizeStandardTopicOptions(response.result || [])
  } catch (error) {
    console.error('获取结果数据标准观点选项失败:', error)
    resultDataStandardTopicOptions.value = []
  }
}

// VoiceList 组件的引用
const resultDataVoiceListRef = ref<InstanceType<typeof VoiceList> | null>(null)
const originalDataVoiceListRef = ref<InstanceType<typeof VoiceList> | null>(null)
const sentimentBranchDataRef = ref<InstanceType<typeof SentimentBranchData> | null>(null)

// 获取当前活动的 VoiceList 组件引用
const getCurrentVoiceListRef = () => {
  if (middlewareStore.originalDataType === OriginalDataType.ResultData) {
    return resultDataVoiceListRef.value
  }

  if (middlewareStore.originalDataType === OriginalDataType.OriginalData) {
    return originalDataVoiceListRef.value
  }

  return sentimentBranchDataRef.value
}

// 结果数据查询条件参数
const handleResultDataSearch = (_formData: any) => {
  resultDataSearchParams.value = _formData
}

// 原始数据查询条件参数
const handleOriginalDataSearch = (_formData: any) => {
  OriginalDataSearchParams.value = _formData
}

/**
 * 导出当前页签的原声数据。
 * 参数由 VoiceListPanel 提供，保持与当前列表查询接口入参一致。
 */
const handleExportData = async (params: VocQueryParams) => {
  const exportConfigMap = {
    [OriginalDataType.ResultData]: {
      request: exportVocSounds,
      exportMenu: '原声查询-结果数据'
    },
    [OriginalDataType.OriginalData]: {
      request: exportOriginalVoiceData,
      exportMenu: '原声查询-原始数据'
    },
    [OriginalDataType.SentimentBranchData]: {
      request: exportSentimentBranchVoiceData,
      exportMenu: '原声查询-情感分支数据'
    }
  }

  const exportConfig = exportConfigMap[middlewareStore.originalDataType]

  await downloadByRequest({
    request: exportConfig.request,
    params,
    exportMenu: exportConfig.exportMenu,
    errorMessage: '导出数据失败，请稍后重试'
  })
}

/**
 * 重置声音管理中的高质量筛选，避免离开原声查询后把该私有条件带入其他页面。
 */
const resetHighQualityFilter = () => {
  queryStore.voiceManagementParams.highQuality = undefined
}

/**
 * 切换结果数据/原始数据/情感分支数据时清空高质量筛选状态。
 * 避免列表请求参数已重置，但下拉框仍保留旧值，导致界面展示与实际查询条件不一致。
 */
watch(
  () => middlewareStore.originalDataType,
  (newType, oldType) => {
    if (!oldType || newType === oldType) return

    resetHighQualityFilter()
  }
)

onMounted(() => {
  void loadResultDataStandardTopicOptions()
})

/**
 * 清理原声查询页内缓存
 * 仅保留跨页面共享的时间字段，避免再次进入页面时带回页内共享/私有筛选条件。
 */
const clearOriginalSoundTabFilterCache = () => {
  queryStore.clearUniversaFilterCacheSearchParams('OriginalSoundQuery_Shared')
  queryStore.clearUniversaFilterCacheSearchParams('OriginalSoundQuery_ResultData')
  queryStore.clearUniversaFilterCacheSearchParams('OriginalSoundQuery_OriginalData')
  queryStore.clearUniversaFilterCacheSearchParams('OriginalSoundQuery_SentimentBranchData')
}

// 注意：切换数据类型时不需要主动调用上报
// 因为组件卸载时（v-if 导致），TheDetails 组件的 onBeforeUnmount 会自动调用 recodeTime 上报
// 如果在这里也调用，会导致重复上报

// 路由离开时上报当前类型的浏览记录
onBeforeRouteLeave(async () => {
  resetHighQualityFilter()

  const currentRef = getCurrentVoiceListRef()
  if (currentRef) {
    try {
      await currentRef.submitBrowseRecord()
    } catch (error) {
      console.error('路由离开时上报浏览记录失败:', error)
    }
  }
})

// 页面卸载前上报当前类型的浏览记录（兜底）
onBeforeUnmount(async () => {
  const currentRef = getCurrentVoiceListRef()
  if (currentRef) {
    try {
      await currentRef.submitBrowseRecord()
    } catch (error) {
      console.error('页面卸载时上报浏览记录失败:', error)
    }
  }
})

// 在页面彻底卸载后清理 tab 私有缓存，确保只在页内 tab 切换时保留筛选状态。
onUnmounted(() => {
  resetHighQualityFilter()
  clearOriginalSoundTabFilterCache()
})
</script>
<template>
  <!-- 复用领导版 -->
  <div
    v-if="middlewareStore.originalDataType === OriginalDataType.ResultData"
    class="voice-list-container"
  >
    <UniversaFilter
      key="ResultData"
      :routeName="`ResultData`"
      cache-key="OriginalSoundQuery_ResultData"
      @search="handleResultDataSearch"
    ></UniversaFilter>
    <div class="content-wrapper">
      <VoiceList
        ref="resultDataVoiceListRef"
        key="ResultDataList"
        class="el-card"
        :queryParams="resultDataSearchParams"
        :show-export-action="true"
        :export-loading="exportLoading"
        v-bind="resultDataVoiceListProps"
        @export-data="handleExportData"
      />
    </div>
  </div>
  <div
    v-if="middlewareStore.originalDataType === OriginalDataType.OriginalData"
    class="voice-list-container"
  >
    <UniversaFilter
      key="OriginalData"
      :routeName="`OriginalData`"
      cache-key="OriginalSoundQuery_OriginalData"
      @search="handleOriginalDataSearch"
    ></UniversaFilter>
    <div class="content-wrapper">
      <VoiceList
        ref="originalDataVoiceListRef"
        key="OriginalDataList"
        class="el-card"
        :queryParams="OriginalDataSearchParams"
        :show-export-action="true"
        :export-loading="exportLoading"
        v-bind="originalDataVoiceListProps"
        :event-issue-topic-options="resultDataStandardTopicOptions"
        list-api-url="/report/vocLeadership/getRawData"
        @export-data="handleExportData"
      />
    </div>
  </div>
  <SentimentBranchData
    v-if="middlewareStore.originalDataType === OriginalDataType.SentimentBranchData"
    ref="sentimentBranchDataRef"
    :export-loading="exportLoading"
    @export-data="handleExportData"
  />
</template>

<style scoped lang="scss">
.voice-list-container {
  width: calc(100% - 5px);
  height: calc(100% + 24px);
  margin-top: -24px;
  box-sizing: border-box;
}

.content-wrapper {
  width: 100%;
  height: calc(100% - 24px);
  padding: 24px 0;
}
// 复用领导版，并定制样式
.el-card.voice-list {
  height: 100%;
  box-sizing: border-box;
  padding: 20px !important;

  // :deep(.voice-list__container) {
  //   height: calc(100% - 52px) !important;
  //   // height: calc(100% - 0px) !important;
  // }
}
</style>
