<script setup lang="ts">
import { ref } from 'vue'
import VoiceList from '@/components/Business/VoiceListPanel/index.vue'
import UniversaFilter from '@/components/Business/UniversaFilter/index.vue'
import SentimentBranchEventIssueDialog from './SentimentBranchEventIssueDialog.vue'

defineOptions({
  name: 'SentimentBranchData'
})

interface Props {
  exportLoading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  exportLoading: false
})

const emit = defineEmits<{
  (e: 'export-data', params: VocQueryParams): void
}>()

type EventIssueMode = 'single' | 'batch'

interface EventIssuePayload {
  mode: EventIssueMode
  selection: Array<Record<string, any>>
}

const sentimentBranchDataSearchParams = ref<any>({})
const sentimentBranchDataVoiceListRef = ref<InstanceType<typeof VoiceList> | null>(null)
const eventIssueVisible = ref(false)
const eventIssueMode = ref<EventIssueMode>('single')
const eventIssueSelection = ref<Array<Record<string, any>>>([])

/**
 * 情感分支数据查询条件参数。
 * 当前先复制原始数据页签逻辑，后续展示字段和接口可在本文件内独立调整。
 */
const handleSentimentBranchDataSearch = (_formData: any) => {
  sentimentBranchDataSearchParams.value = _formData
}

/**
 * 情感分支数据页签复用原始数据只读查看模式。
 */
const sentimentBranchDataVoiceListProps = {
  showSortSelect: true,
  showDataStatus: false,
  showHighQualityFilter: false,
  enableHighQualityActions: false,
  enableHighQualityInfo: false,
  enableVoiceManagementParams: false,
  enableErrorCorrection: false,
  showBatchAction: true,
  enableEventIssueAction: true,
  showCorpusCreateAction: true,
  showSentimentFilter: true,
  sentimentQueryField: 'sentimentList' as const,
  showEmotionalLevelFilter: true,
  showTopicsInList: false,
  detailSource: 'remote' as const,
  showTopicsInDetail: false,
  showBrandSeriesInDetail: false,
  showRelationEventsInDetail: false,
  showKeywordSearch: true
}

/**
 * 抛出情感分支数据导出参数，由父页面统一创建下载任务并提示。
 *
 * @param params 当前列表实际查询参数
 */
const handleExportData = (params: VocQueryParams) => {
  emit('export-data', params)
}

/**
 * 打开情感分支事件下发弹窗。
 *
 * @param payload 公共原声列表抛出的下发模式与选中原声
 */
const handleOpenEventIssue = (payload: EventIssuePayload) => {
  eventIssueMode.value = payload.mode
  eventIssueSelection.value = payload.selection
  eventIssueVisible.value = true
}

/**
 * 事件下发样式校验通过后清理批量选择态，避免旧勾选残留到下一次操作。
 */
const handleEventIssueSuccess = () => {
  sentimentBranchDataVoiceListRef.value?.clearBatchSelection()
}

/**
 * 向父页面暴露浏览记录上报能力，保持路由离开/页面卸载时的上报行为一致。
 */
const submitBrowseRecord = async () => {
  await sentimentBranchDataVoiceListRef.value?.submitBrowseRecord()
}

defineExpose({
  submitBrowseRecord
})
</script>

<template>
  <div class="voice-list-container">
    <UniversaFilter
      key="SentimentBranchData"
      route-name="SentimentBranchData"
      cache-key="OriginalSoundQuery_SentimentBranchData"
      @search="handleSentimentBranchDataSearch"
    ></UniversaFilter>
    <div class="content-wrapper">
      <VoiceList
        ref="sentimentBranchDataVoiceListRef"
        key="SentimentBranchDataList"
        class="el-card"
        :query-params="sentimentBranchDataSearchParams"
        :show-export-action="true"
        :export-loading="props.exportLoading"
        v-bind="sentimentBranchDataVoiceListProps"
        list-api-url="/report/tags/getUnlabeledTagList"
        detail-api-url="/report/tags/getUnlabeledTagInfo"
        @export-data="handleExportData"
        @event-issue="handleOpenEventIssue"
      />
    </div>

    <SentimentBranchEventIssueDialog
      v-model:visible="eventIssueVisible"
      :mode="eventIssueMode"
      :selection="eventIssueSelection"
      @success="handleEventIssueSuccess"
    />
  </div>
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

.el-card.voice-list {
  height: 100%;
  box-sizing: border-box;
  padding: 20px !important;
}
</style>
