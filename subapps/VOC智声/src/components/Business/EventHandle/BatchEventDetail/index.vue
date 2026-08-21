<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import VoiceListPanel from '@/components/Business/VoiceListPanel/index.vue'
import { EventType } from '@/components/Business/EventHandle/ehConstants'
import EventStatistics from '@/components/Business/EventHandle/BatchEventDetail/components/EventStatistics/index.vue'
import ProcessingProgress from '@/components/Business/EventHandle/BatchEventDetail/components/ProcessingProgress/index.vue'
import type { ProcessingProgressExpose } from '@/components/Business/EventHandle/BatchEventDetail/components/ProcessingProgress/types'
import { getBatchEventBrief, getBatchEventPermission } from '@/api/batchEvent'
import type {
  BatchEventBriefDetailVo,
  BatchEventDataSourceType,
  BatchEventPermissionVo
} from '@/api/batchEvent/types'
import type { InsReportSysDepartVo } from '@/api/common/index.d'
import {
  batchEventProcessingFooterActions,
  getBatchEventProcessingStageByTaskStatus
} from './config'
import type {
  BatchEventProcessingFooterActionType,
  BatchEventProcessingFooterAction,
  BatchEventProcessingLoopMode
} from './types'
import {
  BatchEventTabKey,
  BatchEventTabOptions,
  type BatchEventTabKey as BatchEventTabKeyType
} from './beConstants'

defineOptions({
  name: 'BatchEventDetail'
})

interface BatchEventDetailProps {
  row?: Record<string, any>
  eventType: EventType
  startTime?: string
  endTime?: string
  departAccountTree?: InsReportSysDepartVo[]
  voiceExportLoading?: boolean
}

const visible = defineModel<boolean>({ default: false })
const props = defineProps<BatchEventDetailProps>()
const emit = defineEmits<{
  refresh: []
  voiceExport: [queryParams: VocQueryParams, dataSourceType: BatchEventDataSourceType]
}>()

const activeTab = ref<BatchEventTabKeyType>(BatchEventTabKey.Statistics)
const processingProgressRef = ref<ProcessingProgressExpose>()
const progressTaskStatus = ref<string>()
const currentLoopMode = ref<BatchEventProcessingLoopMode>('voc-loop')
const briefData = ref<BatchEventBriefDetailVo>({})
const eventPermission = ref<BatchEventPermissionVo>({})
const briefLoadSettled = ref(false)
const useInitialDefaultTab = ref(false)
const TAB_ICON_COLORS = {
  default: '#5F6A7A',
  active: '#F2F4F7'
} as const
const MANUAL_EVENT_SOURCE = 'MANUAL'
const RESULT_DATA_SOURCE_TYPE: BatchEventDataSourceType = 'RESULT'
const ORIGINAL_DATA_SOURCE_TYPE: BatchEventDataSourceType = 'ORIGINAL'
const RESULT_VOICE_LIST_API_URL = '/report/batch-event/getBatchEventListSounds'
const ORIGINAL_VOICE_LIST_API_URL = '/report/batch-event/getRawData'
const RESULT_VOICE_DETAIL_API_URL = '/report/batch-event/getBatchEventSoundsDetail'
let briefRequestSeq = 0
let briefLoadingEventId = ''
let briefRequest: Promise<void> | null = null
let permissionRequestSeq = 0

type BatchEventPermissionKey = keyof Pick<
  BatchEventPermissionVo,
  | 'approve'
  | 'approveClose'
  | 'closeEvent'
  | 'confirm'
  | 'createTask'
  | 'editTask'
  | 'deleteTask'
  | 'updateTaskProgress'
  | 'addCcUser'
  | 'rejectEvent'
  | 'reassignHandler'
>

const footerActionPermissionMap: Partial<
  Record<BatchEventProcessingFooterActionType, BatchEventPermissionKey>
> = {
  approve: 'approve',
  close: 'approveClose',
  reject: 'rejectEvent',
  confirm: 'confirm',
  handleClose: 'closeEvent',
  createTask: 'createTask',
  transferHandler: 'reassignHandler',
  updateProgress: 'closeEvent'
}

const rowData = computed(() => props.row || {})
const eventId = computed(() => rowData.value.id || '')
const isReadOnly = computed(() => props.eventType === EventType.VIEW)
const isManualEvent = computed(() => {
  return String(briefData.value.eventSource || '').toUpperCase() === MANUAL_EVENT_SOURCE
})
const showStatisticsTab = computed(() => briefLoadSettled.value && !isManualEvent.value)
const visibleTabOptions = computed(() => {
  return BatchEventTabOptions.filter(item => {
    return showStatisticsTab.value || item.key !== BatchEventTabKey.Statistics
  })
})
const currentStage = computed(() =>
  getBatchEventProcessingStageByTaskStatus(progressTaskStatus.value)
)
const currentFooterActions = computed<BatchEventProcessingFooterAction[]>(() => {
  if (isReadOnly.value) {
    return []
  }

  if (!progressTaskStatus.value) {
    return []
  }

  return filterFooterActions(batchEventProcessingFooterActions[currentStage.value] || [])
})
const currentDataSourceType = computed<BatchEventDataSourceType>(() => {
  const dataSourceType = String(briefData.value.dataSourceType || '').toUpperCase()
  return dataSourceType === ORIGINAL_DATA_SOURCE_TYPE
    ? ORIGINAL_DATA_SOURCE_TYPE
    : RESULT_DATA_SOURCE_TYPE
})
const isOriginalDataSource = computed(() => currentDataSourceType.value === ORIGINAL_DATA_SOURCE_TYPE)
const voiceListKey = computed(() => `${eventId.value || 'empty'}-${currentDataSourceType.value}`)
const voiceListApiUrl = computed(() =>
  isOriginalDataSource.value ? ORIGINAL_VOICE_LIST_API_URL : RESULT_VOICE_LIST_API_URL
)
const voiceDetailApiUrl = computed(() =>
  isOriginalDataSource.value ? undefined : RESULT_VOICE_DETAIL_API_URL
)
const voiceDetailSource = computed(() => (isOriginalDataSource.value ? 'list' : 'remote'))

/**
 * 判断当前事件是否具备指定功能权限。
 * @param permissionKey Swagger 权限字段
 * @returns 是否允许展示或触发对应操作
 */
const hasBatchEventPermission = (permissionKey: BatchEventPermissionKey) => {
  return eventPermission.value?.[permissionKey] === true
}

/**
 * 按接口权限过滤底部操作按钮；无接口字段的动作保持既有展示逻辑。
 * @param actions 当前阶段候选按钮
 * @returns 可展示按钮
 */
const filterFooterActions = (actions: BatchEventProcessingFooterAction[]) => {
  return actions.filter(action => {
    const permissionKey = footerActionPermissionMap[action.type]
    return !permissionKey || hasBatchEventPermission(permissionKey)
  })
}

/**
 * 构建批量事件详情中的原声列表查询参数。
 * 当前版本只服务批量事件详情场景，直接按批量事件行字段透传，不再保留旧兼容分支。
 */
const voiceListQueryParams = computed<Partial<VocQueryParams> & Record<string, any>>(() => {
  return {
    newId: rowData.value.id
  }
})

/**
 * 仅在“处理进度”页签下展示底部操作按钮。
 * 底部按钮严格跟随处理进度初始化接口返回状态，90 阶段不展示操作入口。
 */
const showProgressFooter = computed(() => {
  return (
    !isReadOnly.value &&
    activeTab.value === BatchEventTabKey.Progress &&
    currentFooterActions.value.length > 0
  )
})

/**
 * 计算弹窗打开或事件来源更新后的默认页签。
 * 手动下发事件不展示事件统计，查看态兜底进入客户原声。
 */
const getDefaultActiveTab = (): BatchEventTabKeyType => {
  if (!isReadOnly.value) {
    return BatchEventTabKey.Progress
  }

  return showStatisticsTab.value ? BatchEventTabKey.Statistics : BatchEventTabKey.VoiceList
}

/**
 * 保证当前激活页签一定存在于可见页签中。
 * @param preferDefault 是否强制使用当前场景默认页签
 */
const syncActiveTabWithVisibleTabs = (preferDefault = false) => {
  if (!briefLoadSettled.value && isReadOnly.value && activeTab.value === BatchEventTabKey.Statistics) {
    return
  }

  const hasActiveTab = visibleTabOptions.value.some(item => item.key === activeTab.value)
  if (preferDefault || !hasActiveTab) {
    activeTab.value = getDefaultActiveTab()
  }
}

/**
 * 重置父层共享的事件简报状态。
 * brief 未返回前不挂载事件统计，避免手动下发事件触发统计接口。
 */
const resetBriefState = () => {
  briefData.value = {}
  briefLoadSettled.value = false
}

/**
 * 重置当前事件的功能权限状态。
 */
const resetPermissionState = () => {
  eventPermission.value = {}
}

/**
 * 加载批量事件简报，供页签显隐、事件统计和处理进度复用。
 * 同一事件并发触发时复用请求，不同事件通过序号避免旧响应覆盖新数据。
 */
const loadBriefData = async () => {
  const requestEventId = eventId.value
  if (!requestEventId) {
    briefLoadSettled.value = true
    syncActiveTabWithVisibleTabs(useInitialDefaultTab.value)
    useInitialDefaultTab.value = false
    return
  }

  if (briefRequest && briefLoadingEventId === requestEventId) {
    return briefRequest
  }

  const requestSeq = ++briefRequestSeq
  briefLoadingEventId = requestEventId
  const request = (async () => {
    try {
      const response = await getBatchEventBrief({ id: requestEventId })
      if (requestSeq !== briefRequestSeq || requestEventId !== eventId.value) return

      briefData.value = response.result || {}
    } catch (error) {
      if (requestSeq !== briefRequestSeq || requestEventId !== eventId.value) return

      console.error('获取批量事件简报失败:', error)
      briefData.value = {}
    } finally {
      if (requestSeq === briefRequestSeq && requestEventId === eventId.value) {
        briefLoadSettled.value = true
        syncActiveTabWithVisibleTabs(useInitialDefaultTab.value)
        useInitialDefaultTab.value = false
      }

      if (requestSeq === briefRequestSeq && briefLoadingEventId === requestEventId) {
        briefRequest = null
        briefLoadingEventId = ''
      }
    }
  })()

  briefRequest = request
  return request
}

/**
 * 加载当前用户在批量事件中的功能权限。
 * 权限接口失败时按无权限处理有明确字段控制的按钮，避免误展示操作入口。
 */
const loadPermissionData = async () => {
  const requestEventId = eventId.value
  const requestSeq = ++permissionRequestSeq
  resetPermissionState()

  if (!requestEventId || isReadOnly.value) {
    return
  }

  try {
    const response = await getBatchEventPermission({ eventId: String(requestEventId) })
    if (requestSeq !== permissionRequestSeq || requestEventId !== eventId.value) return

    eventPermission.value = response.result || {}
  } catch (error) {
    if (requestSeq !== permissionRequestSeq || requestEventId !== eventId.value) return

    console.error('获取批量事件权限失败:', error)
    resetPermissionState()
  }
}

/**
 * 初始化详情弹窗上下文。
 * FDialog 不透传 open 事件时，深链进入的初始打开状态也需要主动触发详情接口。
 */
const initializeDetail = () => {
  progressTaskStatus.value = undefined
  currentLoopMode.value = 'voc-loop'
  useInitialDefaultTab.value = true
  resetBriefState()
  resetPermissionState()
  activeTab.value = isReadOnly.value ? BatchEventTabKey.Statistics : BatchEventTabKey.Progress
  void loadBriefData()
  void loadPermissionData()
}

const handleClose = () => {
  briefRequestSeq += 1
  permissionRequestSeq += 1
  briefRequest = null
  briefLoadingEventId = ''
  resetBriefState()
  resetPermissionState()
  useInitialDefaultTab.value = false
  progressTaskStatus.value = undefined
  activeTab.value = BatchEventTabKey.Statistics
  currentLoopMode.value = 'voc-loop'
}

watch(
  () => eventId.value,
  () => {
    progressTaskStatus.value = undefined
    currentLoopMode.value = 'voc-loop'
    if (visible.value) {
      useInitialDefaultTab.value = true
      resetBriefState()
      resetPermissionState()
      activeTab.value = isReadOnly.value ? BatchEventTabKey.Statistics : BatchEventTabKey.Progress
      void loadBriefData()
      void loadPermissionData()
    }
  }
)

watch(
  () => visible.value,
  value => {
    if (value) {
      initializeDetail()
    }
  },
  { immediate: true }
)

watch(visibleTabOptions, () => syncActiveTabWithVisibleTabs())

/**
 * 根据页签激活态返回对应图标颜色，保持图标与文案高亮表现一致。
 * @param tabKey 页签标识
 * @returns 当前页签对应的图标颜色
 */
const getTabIconColor = (tabKey: BatchEventTabKeyType) => {
  return activeTab.value === tabKey ? TAB_ICON_COLORS.active : TAB_ICON_COLORS.default
}

/**
 * 处理详情页签切换。
 * @param tabKey 页签标识
 */
const handleTabClick = (tabKey: BatchEventTabKeyType) => {
  // if (tabKey !== BatchEventTabKey.VoiceList) {
  //   ElMessage.info('当前版本仅支持查看原声列表')
  //   return
  // }

  useInitialDefaultTab.value = false
  activeTab.value = tabKey
}

/**
 * 根据当前阶段的底部动作触发对应弹窗。
 * 父层只负责转发底部动作，具体弹窗与接口提交由处理进度子组件统一接管。
 * @param actionType 底部动作标识
 */
const handleProgressAction = async (actionType: BatchEventProcessingFooterActionType) => {
  if (isReadOnly.value) {
    return
  }

  const permissionKey = footerActionPermissionMap[actionType]
  if (permissionKey && !hasBatchEventPermission(permissionKey)) {
    return
  }

  switch (actionType) {
    case 'approve':
      await processingProgressRef.value?.openApproveDialog()
      break
    case 'close':
      processingProgressRef.value?.openCloseDialog()
      break
    case 'reject':
      processingProgressRef.value?.openRejectDialog()
      break
    case 'confirm':
      await processingProgressRef.value?.openConfirmDialog()
      break
    case 'updateProgress':
      processingProgressRef.value?.openUpdateProgressDialog()
      break
    case 'handleClose':
      processingProgressRef.value?.openHandleCloseDialog()
      break
    case 'createTask':
      await processingProgressRef.value?.openCreateTaskDialog()
      break
    case 'transferHandler':
      await processingProgressRef.value?.openTransferHandlerDialog()
      break
    default:
      break
  }
}

/**
 * 接收处理进度接口返回的真实任务状态。
 * 父层只用该状态统一计算底部按钮，避免列表行状态参与处理进度展示。
 * @param taskStatus 处理进度接口返回的任务状态码
 */
const handleProgressTaskStatusChange = (taskStatus: string) => {
  progressTaskStatus.value = taskStatus || undefined
}

/**
 * 接收处理进度页签抛出的闭环处理方式变更。
 * 父层统一维护当前闭环模式，保证 footer 与表单区域实时同步。
 * @param mode 闭环处理方式
 */
const handleLoopModeChange = (mode: BatchEventProcessingLoopMode) => {
  currentLoopMode.value = mode
}

/**
 * 处理进度局部操作成功后刷新详情上下文。
 * 转派处理人不会关闭详情弹窗，但当前用户的事件权限可能已变化，需要重新拉取按钮权限。
 */
const handleProgressOperationRefresh = async () => {
  if (!visible.value) {
    return
  }

  briefRequestSeq += 1
  briefRequest = null
  briefLoadingEventId = ''
  useInitialDefaultTab.value = false

  await Promise.all([loadBriefData(), loadPermissionData()])
}

/**
 * 处理进度主流程操作成功后关闭详情，并通知列表页刷新当前数据。
 */
const handleProgressOperationSuccess = () => {
  visible.value = false
  emit('refresh')
}

/**
 * 转发客户原声导出事件，后续由外层页面接入真实导出接口。
 * @param queryParams 当前客户原声列表筛选参数
 */
const handleVoiceExport = (queryParams: VocQueryParams) => {
  emit('voiceExport', queryParams, currentDataSourceType.value)
}
</script>

<template>
  <FDialog
    v-model:visible="visible"
    width="95%"
    style="padding: 0; border-radius: 8px; height: 96%"
    :show-footer="false"
    :destory-on-close="true"
    @close="handleClose"
  >
    <template #header>
      <div class="batch-event-detail__header">
        <span>事件详情</span>
      </div>
    </template>

    <div class="batch-event-detail">
      <div class="batch-event-detail__tabs">
        <button
          v-for="item in visibleTabOptions"
          :key="item.key"
          type="button"
          class="batch-event-detail__tab"
          :class="{ 'is-active': activeTab === item.key }"
          @click="handleTabClick(item.key)"
        >
          <SvgIcon
            :name="item.icon"
            width="20px"
            height="20px"
            :color="getTabIconColor(item.key)"
            class="batch-event-detail__tab-icon"
          />
          <span class="batch-event-detail__tab-label">{{ item.label }}</span>
        </button>
      </div>

      <div class="batch-event-detail__content">
        <EventStatistics
          v-if="showStatisticsTab && activeTab === BatchEventTabKey.Statistics"
          :row="rowData"
          :brief-data="briefData"
          :start-time="startTime"
          :end-time="endTime"
        />
        <VoiceListPanel
          v-if="activeTab === BatchEventTabKey.VoiceList"
          :key="voiceListKey"
          title="客户原声"
          :query-params="voiceListQueryParams"
          query-params-mode="external-only"
          :list-api-url="voiceListApiUrl"
          :detail-api-url="voiceDetailApiUrl"
          :detail-source="voiceDetailSource"
          :default-page-size="10"
          :watch-store-query="false"
          container-mode="embedded"
          :show-sort-select="true"
          :show-high-quality-filter="false"
          :enable-high-quality-actions="false"
          :enable-high-quality-info="false"
          :enable-error-correction="false"
          :show-batch-action="false"
          :enable-event-issue-action="false"
          :event-issue-data-source-type="currentDataSourceType"
          :show-export-action="false"
          :show-event-export-action="true"
          :show-keyword-search="true"
          :show-topics-in-list="!isOriginalDataSource"
          :show-topics-in-detail="!isOriginalDataSource"
          scene-tip-text="事件原声仅显示事件相关标准观点，非全部观点，请知悉。"
          :show-brand-series-in-detail="!isOriginalDataSource"
          :show-relation-events-in-detail="!isOriginalDataSource"
          :show-corpus-create-action="true"
          :export-action-loading="props.voiceExportLoading"
          @export="handleVoiceExport"
        />

        <ProcessingProgress
          v-if="activeTab === BatchEventTabKey.Progress"
          ref="processingProgressRef"
          :row="rowData"
          :brief-data="briefData"
          :loop-mode="currentLoopMode"
          :depart-account-tree="props.departAccountTree || []"
          :permission="eventPermission"
          :read-only="isReadOnly"
          @task-status-change="handleProgressTaskStatusChange"
          @loop-mode-change="handleLoopModeChange"
          @operation-refresh="handleProgressOperationRefresh"
          @operation-success="handleProgressOperationSuccess"
        />
      </div>
    </div>

    <template v-if="showProgressFooter" #footer>
      <el-button
        v-for="action in currentFooterActions"
        :key="action.type"
        :class="action.variant === 'primary' ? 'app-dialog__btn-confirm' : 'app-dialog__btn-cancel'"
        :type="action.variant === 'primary' ? 'primary' : undefined"
        @click="handleProgressAction(action.type)"
        >{{ action.label }}</el-button
      >
    </template>
  </FDialog>
</template>

<style scoped lang="scss">
.batch-event-detail {
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.batch-event-detail__header {
  display: flex;
  align-items: center;
  min-width: 0;
}

.batch-event-detail__tabs {
  display: flex;
  align-items: center;
  gap: 8px;
  // padding-bottom: 16px;

  flex-shrink: 0;
}

.batch-event-detail__tab {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-width: 120px;
  height: 40px;
  padding: 0 20px;
  border: 1px solid #dfe4ea;
  border-radius: 8px 8px 0 0;
  background: #f5f7fa;
  color: #5f6a7a;
  font-size: 14px;
  line-height: 1;
  white-space: nowrap;
  cursor: pointer;
  transition:
    background-color 0.2s ease,
    color 0.2s ease,
    border-color 0.2s ease;

  &.is-active {
    background: #1677ff;
    border-color: #1677ff;
    color: #f2f4f7;
    font-weight: 600;
  }
}

.batch-event-detail__tab-icon {
  flex-shrink: 0;
}

.batch-event-detail__tab-label {
  display: inline-flex;
  align-items: center;
}

.batch-event-detail__content {
  flex: 1;
  min-height: 0;
  // padding-top: 16px;
  padding: 24px 24px;
  border: 1px solid #ebedf0;
  overflow: auto;
}

:deep(.voice-list--embedded) {
  height: 100%;
}

:deep(.voice-list .tip) {
  display: none;
}
</style>
