<script setup lang="ts">
import { computed, ref, watch, withDefaults, defineProps } from 'vue'
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
import type { EventStatusFilterKey } from '@h5/views/taskEvent/components/EventStatusTabs/types.d.ts'
import { getMobileSingleEventList, type MobileSingleEventListItem } from '@h5/api/taskEvent'
import { toRgba } from '@/utils'
import {
  eventStatusMeta,
  taskStatusColorMap,
  taskStatusLabelMap,
  EVENT_PRIORITY_LEVEL_COLORS,
  EVENT_LEVEL_TO_PRIORITY_KEY
} from '@/views/H5/constants/index'

defineOptions({
  name: 'SingleEventListCard'
})

const router = useRouter()

// 组件入参
const props = withDefaults(defineProps<EventListCardProps>(), {
  baseRequestParams: () => ({}),
  eventType: 'single'
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

//根据key获取对应的codes
const getStautsCodesByKey = (key: string) => {
  const item = eventStatusMeta.find(item => item.key === key)
  return item?.codes
}

// 用户意图-一级#二级#三级#四级体验代码-标准观点
const buildIntentionSummary = (raw: MobileSingleEventListItem): string => {
  const domTags = [raw.domTagFirst, raw.domTagSecond, raw.domTagThree, raw.domTagFour]
    .filter(hasText)
    .join('#')

  const left = [raw.intention, domTags, raw.topic || ''].filter(hasText).join('-')
  return `${left}`
}

const mapToTaskEventItem = (raw: any): TaskEventItem => {
  const taskStatus = String(raw.taskStatus || '')
  return {
    ...raw,
    statusLabel: (taskStatusLabelMap as any)[taskStatus] || '',
    statusColor: (taskStatusColorMap as any)[taskStatus] || '#86909c',
    intentionSummary: buildIntentionSummary(raw),
    handlerName: raw.handler?.userName || ''
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

  return {
    ...baseParams,
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    taskStatuses: statusCodes.length > 0 ? statusCodes : undefined
  }
}

/**
 * 获取任务事件列表
 * - 使用移动端真实接口：POST /mobileTerminal/single-event/list
 */
const fetchTaskEventList = async (params: TaskEventListQuery): Promise<TaskEventListResult> => {
  const res = await getMobileSingleEventList({
    ...(params as any)
  })

  const pageResult: any = res.result || {}

  const rawList: MobileSingleEventListItem[] = pageResult.list || []

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

// 点击单点事件卡片进入单点详情页
const handleItemClick = (item: TaskEventItem) => {
  if (!hasText(item.dataId)) {
    showToast('原声ID缺失，无法打开事件详情')
    return
  }
  if (!hasText(item.id)) {
    showToast('事件ID缺失，无法打开事件详情')
    return
  }

  router.push({
    name: 'H5TaskEventDetail',
    query: {
      dataId: item.dataId,
      id: item.id
    }
  })
}
</script>

<template>
  <HCard title="事件列表">
    <EventStatusTabs v-model="currentStatus" @change="handleStatusChange" />
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
            <div class="event-item__header flex-between flex-y-center pb-8">
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
            <!-- 标签区：渠道 + 优先级 + 等级 -->
            <div class="event-item__tags flex-y-center mt-8">
              <div
                v-if="hasText(item.subjectCategoryName)"
                class="event-tag--channel event-tag single-line-ellipsis"
              >
                {{ item.subjectCategoryName }}
              </div>
              <span
                v-if="hasText(item.eventPriority)"
                class="event-tag"
                :style="getTagStyle(item.eventPriority, EVENT_PRIORITY_LEVEL_COLORS)"
              >
                {{ item.eventPriorityName?.toLocaleUpperCase() || '' }}
              </span>
              <span
                v-if="hasText(item.eventLevel)"
                class="event-tag"
                :style="getTagStyle(item.eventLevel, EVENT_LEVEL_TO_PRIORITY_KEY)"
              >
                {{ item.eventLevelName }}级
              </span>
            </div>

            <!-- 底部信息：时间 + 部门 + 处理人 -->
            <div class="event-item__meta flex-y-center mt-10">
              <span>{{ item.warningTime }}</span>
              <template v-if="hasText(item.mainRespOrgName)">
                <span class="meta-divider"></span>
                <span
                  class="meta-text single-line-ellipsis"
                  :style="{ maxWidth: hasText(item.handlerName) ? '' : '100%' }"
                  >{{ item.mainRespOrgName }}</span
                >
              </template>

              <template v-if="hasText(item.handlerName)">
                <span class="meta-divider"></span>
                <span class="meta-text">{{ item.handlerName }}</span>
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
  padding: 12px 9px;
  cursor: pointer;
}

.event-item__title-area {
  max-width: 80%;
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
  line-height: 16px;
}

.event-item__tags {
  gap: 8px;
}

.event-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 10px;
  border-radius: 4px;
  font-weight: 500;
  font-size: 12px;
}

.event-tag--channel {
  max-width: calc(100% - 100px);
  background: #f2f4f7;
  padding: 1px 8px;
  border-radius: 4px;
  font-weight: 500;
  font-size: 12px;
  line-height: 22px;
  color: #5f6a7a;
}

.event-item__meta {
  white-space: nowrap;
  font-weight: 400;
  font-size: 12px;
  color: #5f6a7a;
  line-height: 20px;
}

.meta-text {
  max-width: 40%;
  overflow: hidden;
  text-overflow: ellipsis;
}

.meta-divider {
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
