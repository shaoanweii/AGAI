<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import {
  getBatchNewlyEventStatistics,
  getBatchEventStatusDistribution,
  getBatchEventTrend,
  normalizeBatchEventQueryParams,
  type BatchEventQueryModel,
  type TaskEventNewlyVo
} from '@h5/api/batchEvent'
import type { H5VocTaskBaseRequest } from '@h5/api/taskEvent'
import EventRateCard from './EventRateCard/index.vue'
import EventStatusDistributionCard from './EventStatusDistributionCard/index.vue'
import EventTrendCard from './EventTrendCard/index.vue'
import EventListCard from './EventListCard/index.vue'
import BackToAnchor from '@h5/components/BackToAnchor/index.vue'
import type { EventStatusRawItem } from './EventStatusDistributionCard/types.d.ts'
import type { EventTrendPoint } from './EventTrendCard/types.d.ts'

defineOptions({
  name: 'BatchTaskEventContent'
})

const props = withDefaults(
  defineProps<{
    /** 品牌、时间与批量高级筛选合并后的请求参数 */
    baseRequestParams?: H5VocTaskBaseRequest
  }>(),
  {
    baseRequestParams: () => ({})
  }
)

const eventStatusSummary = ref<EventStatusRawItem[]>([])
const eventTrendList = ref<EventTrendPoint[]>([])
const newlyEventStatistics = ref<TaskEventNewlyVo | null>(null)
const loading = ref(false)
const eventListAnchor = ref<HTMLElement | null>(null)

const defaultNewlyEventStatistics: TaskEventNewlyVo = {
  currentCounts: 0,
  lastCounts: 0,
  closeRate: 0,
  ringRate: 0
}

const eventRateCardData = computed<TaskEventNewlyVo>(() => {
  return newlyEventStatistics.value || defaultNewlyEventStatistics
})

const isBaseReady = computed(() => {
  const params = props.baseRequestParams || {}
  return !!params.brandCode && !!params.startTime && !!params.endTime
})

/**
 * 加载批量新增事件统计。
 * @param requestParams 品牌、时间与批量筛选条件
 */
const fetchBatchNewlyEventStatistics = async (requestParams: BatchEventQueryModel) => {
  try {
    const response = await getBatchNewlyEventStatistics(requestParams)
    newlyEventStatistics.value =
      response.success && response.result ? response.result : { ...defaultNewlyEventStatistics }
  } catch (error) {
    console.error('获取批量新增事件统计失败:', error)
    newlyEventStatistics.value = { ...defaultNewlyEventStatistics }
  }
}

/**
 * 加载批量事件状态分布。
 * @param requestParams 品牌、时间与批量筛选条件
 */
const fetchBatchEventStatusSummary = async (requestParams: BatchEventQueryModel) => {
  try {
    const response = await getBatchEventStatusDistribution(requestParams)
    eventStatusSummary.value =
      response.success && Array.isArray(response.result) ? response.result : []
  } catch (error) {
    console.error('获取批量事件状态分布失败:', error)
    eventStatusSummary.value = []
  }
}

/**
 * 加载批量事件趋势。
 * @param requestParams 品牌、时间与批量筛选条件
 */
const fetchBatchEventTrendList = async (requestParams: BatchEventQueryModel) => {
  try {
    const response = await getBatchEventTrend(requestParams)
    eventTrendList.value = response.success && Array.isArray(response.result) ? response.result : []
  } catch (error) {
    console.error('获取批量事件趋势失败:', error)
    eventTrendList.value = []
  }
}

/**
 * 刷新批量四块内容区数据，列表当前复用 EventListCard 的分页逻辑。
 */
const fetchContentData = async () => {
  if (!isBaseReady.value) return

  const requestParams = normalizeBatchEventQueryParams(props.baseRequestParams)

  loading.value = true
  try {
    await Promise.allSettled([
      fetchBatchNewlyEventStatistics(requestParams),
      fetchBatchEventStatusSummary(requestParams),
      fetchBatchEventTrendList(requestParams)
    ])
  } finally {
    loading.value = false
  }
}

watch(
  [isBaseReady, () => props.baseRequestParams],
  ([ready]) => {
    if (!ready) return
    fetchContentData()
  },
  { deep: true, immediate: true }
)
</script>

<template>
  <template v-if="loading">
    <van-skeleton title :row="4" />
  </template>
  <template v-else>
    <EventRateCard :data="eventRateCardData" />

    <div class="mt-12">
      <EventStatusDistributionCard :data="eventStatusSummary" />
    </div>

    <div class="mt-12">
      <EventTrendCard :data="eventTrendList" />
    </div>

    <div class="mt-12">
      <div ref="eventListAnchor" class="task-event-anchor"></div>
      <EventListCard :base-request-params="baseRequestParams" event-type="batch" />
    </div>

    <BackToAnchor :target-el="eventListAnchor" />
  </template>
</template>

<style scoped lang="scss">
.task-event-anchor {
  height: 0;
}
</style>
