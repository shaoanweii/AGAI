<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import {
  getNewlyEventStatistics,
  getEventStatusDistribution,
  getEventTrend,
  type NewlyEventStatisticsVo,
  type H5VocTaskBaseRequest
} from '@h5/api/taskEvent'
import EventRateCard from './EventRateCard/index.vue'
import EventStatusDistributionCard from './EventStatusDistributionCard/index.vue'
import EventTrendCard from './EventTrendCard/index.vue'
import EventListCard from './EventListCard/index.vue'
import BackToAnchor from '@h5/components/BackToAnchor/index.vue'
import type { EventStatusRawItem } from './EventStatusDistributionCard/types.d.ts'
import type { EventTrendPoint } from './EventTrendCard/types.d.ts'

defineOptions({
  name: 'SingleTaskEventContent'
})

const props = withDefaults(
  defineProps<{
    /** 品牌、时间与单点高级筛选合并后的请求参数 */
    baseRequestParams?: H5VocTaskBaseRequest
  }>(),
  {
    baseRequestParams: () => ({})
  }
)

// 事件状态分布数据
const eventStatusSummary = ref<EventStatusRawItem[]>([])

// 事件趋势数据
const eventTrendList = ref<EventTrendPoint[]>([])

// 新增事件统计数据
const newlyEventStatistics = ref<NewlyEventStatisticsVo | null>(null)

// 内容区加载状态，独立于页面顶部品牌/日期区域
const loading = ref(false)

// “事件列表”锚点引用，用于回到事件列表卡片位置
const eventListAnchor = ref<HTMLElement | null>(null)

// 新增事件统计默认值
const defaultNewlyEventStatistics: NewlyEventStatisticsVo = {
  currentCounts: 0,
  lastCounts: 0,
  closeRate: 0,
  ringRate: 0
}

// EventRateCard 展示用数据（空值时兜底）
const eventRateCardData = computed<NewlyEventStatisticsVo>(() => {
  return newlyEventStatistics.value || defaultNewlyEventStatistics
})

// 外层筛选是否已具备基本条件（品牌 + 起止日期）
const isBaseReady = computed(() => {
  const params = props.baseRequestParams || {}
  return !!params.brandCode && !!params.startTime && !!params.endTime
})

/**
 * 加载新增事件统计。
 * @param requestParams 品牌、时间与单点筛选条件
 */
const fetchNewlyEventStatistics = async (requestParams: H5VocTaskBaseRequest) => {
  try {
    const response = await getNewlyEventStatistics(requestParams)
    newlyEventStatistics.value =
      response.success && response.result ? response.result : { ...defaultNewlyEventStatistics }
  } catch (error) {
    console.error('获取单点新增事件统计失败:', error)
    newlyEventStatistics.value = { ...defaultNewlyEventStatistics }
  }
}

/**
 * 加载事件状态分布数据。
 * @param requestParams 品牌、时间与单点筛选条件
 */
const fetchEventStatusSummary = async (requestParams: H5VocTaskBaseRequest) => {
  try {
    const response = await getEventStatusDistribution(requestParams)
    eventStatusSummary.value =
      response.success && Array.isArray(response.result) ? response.result : []
  } catch (error) {
    console.error('获取单点事件状态分布失败:', error)
    eventStatusSummary.value = []
  }
}

/**
 * 加载事件趋势数据。
 * @param requestParams 品牌、时间与单点筛选条件
 */
const fetchEventTrendList = async (requestParams: H5VocTaskBaseRequest) => {
  try {
    const response = await getEventTrend(requestParams)
    eventTrendList.value = response.success && Array.isArray(response.result) ? response.result : []
  } catch (error) {
    console.error('获取单点事件趋势失败:', error)
    eventTrendList.value = []
  }
}

/**
 * 刷新单点四块内容区数据，列表由 EventListCard 根据参数自行刷新。
 */
const fetchContentData = async () => {
  if (!isBaseReady.value) return

  const requestParams: H5VocTaskBaseRequest = {
    ...props.baseRequestParams
  }

  loading.value = true
  try {
    await Promise.allSettled([
      fetchNewlyEventStatistics(requestParams),
      fetchEventStatusSummary(requestParams),
      fetchEventTrendList(requestParams)
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
      <EventListCard :base-request-params="baseRequestParams" event-type="single" />
    </div>

    <BackToAnchor :target-el="eventListAnchor" />
  </template>
</template>

<style scoped lang="scss">
.task-event-anchor {
  height: 0;
}
</style>
