<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import HCard from '@h5/components/UI/HCard/index.vue'
import EventStatusTabs from '@h5/views/taskEvent/components/EventStatusTabs/index.vue'
import type {
  EventListCardProps,
  TaskEventItem,
  TaskEventListQuery,
  TaskEventListResult
} from './types.d.ts'
import type {
  EventStatusFilterKey,
  EventStatusFilterOption
} from '@h5/views/taskEvent/components/EventStatusTabs/types.d.ts'
import {
  getMobileBatchEventList,
  normalizeBatchEventQueryParams,
  type BatchEventMobilePageVo
} from '@h5/api/batchEvent'
import { toRgba } from '@/utils'
import {
  eventStatusMeta,
  taskStatusColorMap,
  taskStatusLabelMap,
  EVENT_PRIORITY_LEVEL_COLORS
} from '@/views/H5/constants/index'

defineOptions({
  name: 'BatchEventListCard'
})

const router = useRouter()

// 组件入参
const props = withDefaults(defineProps<EventListCardProps>(), {
  baseRequestParams: () => ({}),
  eventType: 'batch'
})

// 当前状态筛选 key，默认全部
const currentStatus = ref<EventStatusFilterKey>('')

// 列表相关状态
const loading = ref(false)
const finished = ref(false)
const list = ref<TaskEventItem[]>([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 批量事件筛选展示文案与单点事件存在差异，状态 key/codes 仍沿用统一配置。
const batchStatusTabs: EventStatusFilterOption[] = [
  { key: '', label: '全部' },
  ...eventStatusMeta.map(item => ({
    key: item.key,
    label: item.key === '10' ? '事件初审' : item.label,
    codes: item.codes
  }))
]

// 外层筛选是否已具备基本条件（品牌 + 起止日期）
const isBaseReady = computed(() => {
  const params = props.baseRequestParams || {}
  const brandCode = (params as any).brandCode
  const startTime = (params as any).startTime
  const endTime = (params as any).endTime
  return !!brandCode && !!startTime && !!endTime
})

// 标签样式：前景色使用配置色，背景使用 toRgba(alpha=0.1)
const getTagStyle = (key: string | undefined, colorMap: Record<string, string>) => {
  const safeKey = key || ''
  const color = colorMap[safeKey] || '#86909c'
  return {
    color,
    backgroundColor: toRgba(color, 0.1)
  }
}

const hasText = (value: unknown): boolean => {
  if (value === undefined || value === null) return false
  return String(value).trim().length > 0
}

const getText = (value: unknown): string => {
  return hasText(value) ? String(value).trim() : ''
}

const joinWithSeparator = (values: unknown[], separator: string) => {
  return values.map(getText).filter(Boolean).join(separator)
}

/**
 * 格式化后端可能以数组或 JSON 数组字符串返回的展示字段。
 * @param value 原始字段值
 * @returns 多值使用竖线分隔的展示文案
 */
const formatDisplayText = (value: unknown): string => {
  if (Array.isArray(value)) {
    return value.map(getText).filter(Boolean).join(' | ')
  }

  const text = getText(value)
  if (!text.startsWith('[')) return text

  try {
    const parsed = JSON.parse(text)
    return Array.isArray(parsed) ? parsed.map(getText).filter(Boolean).join(' | ') : text
  } catch {
    return text
  }
}

//根据key获取对应的codes
const getStautsCodesByKey = (key: string) => {
  const item = eventStatusMeta.find(item => item.key === key)
  return item?.codes
}

/**
 * 生成批量事件卡片摘要。
 * @param raw 批量事件列表原始项
 * @returns 品牌车系观点链路与新版画像字段拼接后的展示文案
 */
const buildIntentionSummary = (raw: BatchEventMobilePageVo): string => {
  const brandSeriesTopic = joinWithSeparator(
    [raw.brandName, raw.carSeriesName, formatDisplayText(raw.topicName || raw.topic)],
    '-'
  )
  return joinWithSeparator(
    [
      brandSeriesTopic,
      formatDisplayText(raw.topicText),
      formatDisplayText(raw.usageScenario),
      formatDisplayText(raw.custType)
    ],
    ' | '
  )
}

/**
 * 获取优先级色板 key。
 * @param raw 批量事件列表原始项
 * @returns P0-P4 对应的小写 key
 */
const getPriorityColorKey = (raw: BatchEventMobilePageVo): string => {
  return getText(raw.eventPriority || raw.eventPriorityName).toLowerCase()
}

const mapToTaskEventItem = (raw: BatchEventMobilePageVo): TaskEventItem => {
  const taskStatus = String(raw.taskStatus || '')
  const eventPriority = getText(raw.eventPriority || raw.eventPriorityName)
  const primaryDepDisplayName = getText(raw.primaryDepName)
  const handlerName = raw.mainRespUserName || raw.reviewUserName || ''
  return {
    ...raw,
    id: String(raw.id || ''),
    dataId: raw.dataId || raw.id,
    eventPriority,
    priorityColorKey: getPriorityColorKey(raw),
    statusCode: taskStatus,
    statusLabel: (taskStatusLabelMap as any)[taskStatus] || '',
    statusColor: (taskStatusColorMap as any)[taskStatus] || '#86909c',
    intentionSummary: buildIntentionSummary(raw),
    primaryDepDisplayName,
    handlerName,
    metaItems: [raw.warningTime, primaryDepDisplayName, handlerName].map(getText).filter(Boolean)
  }
}

// 重置分页与列表状态
const resetState = () => {
  list.value = []
  total.value = 0
  pageNum.value = 1
  finished.value = false
  loading.value = false
}

// 将外层参数与内部分页、状态筛选组装为查询参数
const buildQueryParams = (): TaskEventListQuery => {
  const baseParams = props.baseRequestParams || {}
  const statusCodes = getStautsCodesByKey(currentStatus.value) || []

  const query = normalizeBatchEventQueryParams({
    ...baseParams,
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    taskStatuses: statusCodes.length > 0 ? statusCodes : undefined
  })

  return query as TaskEventListQuery
}

/**
 * 获取批量任务事件列表。
 */
const fetchTaskEventList = async (params: TaskEventListQuery): Promise<TaskEventListResult> => {
  const res = await getMobileBatchEventList(params)

  const pageResult = res.result || {}

  const rawList: BatchEventMobilePageVo[] = pageResult.list || []

  const mapped = rawList.map(mapToTaskEventItem).filter(item => hasText(item.id))
  const totalCount = Number(pageResult.total) || 0
  return { list: mapped, total: totalCount }
}

// 核心加载逻辑：用于首屏和上拉加载
const loadList = async () => {
  if (!isBaseReady.value) {
    // 外层筛选条件不完整时不请求数据
    resetState()
    return
  }

  if (finished.value) {
    return
  }

  loading.value = true
  try {
    const params = buildQueryParams()

    const result = await fetchTaskEventList(params)

    if (pageNum.value === 1) {
      list.value = result.list || []
    } else {
      list.value = [...list.value, ...(result.list || [])]
    }

    total.value = result.total || 0

    const loadedCount = list.value.length
    const currentPageSize = params.pageSize || pageSize.value

    // 判断是否已加载完所有数据
    if (loadedCount >= total.value || (result.list && result.list.length < currentPageSize)) {
      finished.value = true
    } else {
      pageNum.value += 1
    }
  } catch (error) {
    console.error('获取任务事件列表失败:', error)
    // 请求失败时终止本次加载，防止陷入死循环
    finished.value = true
  } finally {
    loading.value = false
  }
}

// 对外暴露的刷新方法，便于父组件在必要时主动刷新
const reload = () => {
  resetState()
  if (isBaseReady.value) {
    loadList()
  }
}

defineExpose({
  reload
})

// 监听外层筛选条件 + 当前状态筛选
watch(
  [isBaseReady, () => props.baseRequestParams, currentStatus],
  ([ready]) => {
    if (!ready) {
      // 筛选条件不完整时重置列表
      resetState()
      return
    }
    // 品牌 / 时间 / 状态 任一变化时重置并重新拉取
    reload()
  },
  { deep: true, immediate: true }
)

// 处理“上拉加载”
const handleLoadMore = () => {
  loadList()
}

// 状态切换回调：这里只更新 currentStatus，实际请求由 watch 统一触发
const handleStatusChange = () => {
  // 由 watch([isBaseReady, baseRequestParams, currentStatus]) 统一处理刷新逻辑
}

// 点击批量事件卡片进入批量详情页
const handleItemClick = (item: TaskEventItem) => {
  if (!hasText(item.id)) {
    showToast('事件ID缺失，无法打开事件详情')
    return
  }

  router.push({
    name: 'H5TaskBatchEventDetail',
    query: {
      dataId: item.dataId || item.id,
      id: item.id
    }
  })
}
</script>

<template>
  <HCard title="事件列表">
    <EventStatusTabs
      v-model="currentStatus"
      :options="batchStatusTabs"
      @change="handleStatusChange"
    />
    <div class="event-list-card">
      <!-- 首屏加载骨架屏 -->
      <div v-if="loading && list.length === 0" class="h-220">
        <van-skeleton v-for="n in 2" :key="n" title :row="2" class="mt-8" />
      </div>
      <template v-else>
        <van-list
          v-model:loading="loading"
          v-model:finished="finished"
          :immediate-check="false"
          @load="handleLoadMore"
        >
          <div
            v-for="item in list"
            :key="item.id"
            class="event-item mb-16"
            role="button"
            tabindex="0"
            @click="handleItemClick(item)"
          >
            <!-- 头部：事件信息 + 事件编号 + 状态 -->
            <div class="event-item__header pb-8">
              <div class="event-item__title-area">
                <div v-if="hasText(item.eventName)" class="event-item__title single-line-ellipsis">
                  {{ item.eventName }}
                </div>
                <div v-if="hasText(item.warningEventNo)" class="event-item__no">
                  {{ item.warningEventNo }}
                </div>
              </div>
              <div
                v-if="hasText(item.taskStatusName) || hasText(item.statusLabel)"
                class="event-item__status flex-y-center"
              >
                <span
                  class="event-item__status-dot"
                  :style="{ backgroundColor: item.statusColor }"
                ></span>
                <span class="event-item__status-text">{{
                  item.taskStatusName || item.statusLabel
                }}</span>
              </div>
            </div>

            <div class="divider"></div>
            <!-- 摘要内容 -->
            <div
              v-if="hasText(item.intentionSummary)"
              class="event-item__summary mt-8 two-line-ellipsis"
            >
              {{ item.intentionSummary }}
            </div>
            <!-- 标签区：主题分类 + 优先级 -->
            <div class="event-item__tags flex-y-center mt-8">
              <div
                v-if="hasText(item.subjectCategoryName)"
                class="event-tag--channel event-tag single-line-ellipsis"
              >
                {{ item.subjectCategoryName }}
              </div>
              <span
                v-if="hasText(item.eventPriorityName) || hasText(item.eventPriority)"
                class="event-tag"
                :style="getTagStyle(item.priorityColorKey, EVENT_PRIORITY_LEVEL_COLORS)"
              >
                {{ (item.eventPriorityName || item.eventPriority)?.toLocaleUpperCase() || '' }}
              </span>
            </div>

            <!-- 底部信息：时间 + 部门 + 处理人 -->
            <div v-if="item.metaItems?.length" class="event-item__meta flex-y-center mt-10">
              <template v-for="(meta, metaIndex) in item.metaItems" :key="`${item.id}-${metaIndex}`">
                <span v-if="metaIndex > 0" class="meta-divider"></span>
                <span
                  class="meta-text single-line-ellipsis"
                  :class="{ 'meta-text--time': metaIndex === 0 }"
                  >{{ meta }}</span
                >
              </template>
            </div>
          </div>

          <!-- 空数据占位 -->
          <van-empty
            v-if="!loading && list.length === 0"
            class="event-empty h-220"
            description="暂无数据"
          />
          <template #finished>
            <div v-if="list.length" class="flex-center">
              <van-divider class="finish-text">已显示全部事件</van-divider>
            </div>
          </template>
        </van-list>
      </template>
    </div>
  </HCard>
</template>

<style scoped lang="scss">
.event-item {
  background: #ffffff;
  border-radius: 8px 8px 8px 8px;
  border: 1px solid #ebedf0;
  padding: 12px;
  cursor: pointer;
}

.event-item__header {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 8px;
  align-items: center;
}

.event-item__title-area {
  min-width: 0;
}

.event-item__title {
  font-weight: 500;
  font-size: 14px;
  color: #1f2733;
  line-height: 24px;
}

.event-item__no {
  font-weight: 400;
  font-size: 14px;
  color: rgba(0, 0, 0, 0.4);
  line-height: 22px;
}

.divider {
  width: 100%;
  border-top: 1px solid #ebedf0;
}

.event-item__status {
  flex-shrink: 0;
  font-weight: 400;
  font-size: 12px;
  color: #5f6a7a;
}

.event-item__status-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 8px;
}

.event-item__status-text {
  white-space: nowrap;
}

.event-item__summary {
  font-weight: 400;
  font-size: 12px;
  color: #1f2733;
  line-height: 18px;
}

.event-item__tags {
  gap: 8px;
  min-width: 0;
}

.event-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 10px;
  border-radius: 4px;
  font-weight: 500;
  font-size: 12px;
  line-height: 16px;
  white-space: nowrap;
}

.event-tag--channel {
  max-width: calc(100% - 64px);
  background: #f2f4f7;
  padding: 1px 8px;
  border-radius: 4px;
  font-weight: 500;
  font-size: 12px;
  line-height: 22px;
  color: #5f6a7a;
}

.event-item__meta {
  min-width: 0;
  white-space: nowrap;
  font-weight: 400;
  font-size: 12px;
  color: #5f6a7a;
  line-height: 20px;
}

.meta-text {
  min-width: 0;
  max-width: 32%;
  overflow: hidden;
  text-overflow: ellipsis;
}

.meta-text--time {
  flex-shrink: 0;
  max-width: none;
}

.meta-divider {
  flex-shrink: 0;
  margin: 0 8px;
  width: 1px;
  height: 10px;
  background-color: #dfe2e8;
}

.h-220 {
  height: 220px !important;
}

.finish-text {
  width: 160px;
  margin: 0 !important;
  text-align: center;
  font-weight: 400;
  font-size: 12px;
  color: #929aa6;
}
</style>
