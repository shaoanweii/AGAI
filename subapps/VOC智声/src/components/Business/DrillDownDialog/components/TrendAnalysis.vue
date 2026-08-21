<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { DrillTabKey } from '../constants'
import DataTrend from '@components/Business/Scene/Common/DataTrend.vue'
import TrendSummaryCards from './TrendSummaryCards/index.vue'
import { getDataTrendChange, getDrillDownBrief } from '@api/drillDownDialog'
import type { ProductBriefVo } from '@/api/drillDownDialog/types'
import type { ProductTrendVo } from '@/api/productAnalysis/types'
import type { TrendSummaryCardItem } from './TrendSummaryCards/types'
import { getDateRange } from '@/utils/date'
import { fmtFix, fmtNum, fmtPer } from '@/utils'
import useGeneralDrillDownStore from '@store/modules/generalDrillDown.ts'

defineOptions({
  name: 'TrendAnalysis'
})

interface ChartClickParams {
  data?: ProductTrendVo['trend'][number]
}

const ddStore = useGeneralDrillDownStore()
const chartData = ref<ProductTrendVo | null>(null)
const summaryData = ref<ProductBriefVo | null>(null)

// 接收父级透传的查询参数与筛选条件
const props = defineProps<{ queryParams?: VocQueryParams; filters?: any[] }>()

const loading = ref(false)

/**
 * 合并当前组件可用的查询参数。
 * - 顶部卡片与下方趋势图必须共享同一份入参，保证筛选口径完全一致
 * @param params 外部触发刷新时透传的查询参数
 * @returns 合并后的查询参数
 */
const getMergedQueryParams = (params?: VocQueryParams): VocQueryParams => {
  return {
    ...(props.queryParams || {}),
    ...(params || {})
  }
}

/**
 * 将顶部卡片统一转换为页面展示结构。
 * - 保留接口原始字段在状态层流转，只在展示层按现有卡片结构组装
 */
const summaryCards = computed<TrendSummaryCardItem[]>(() => {
  const brief = summaryData.value

  return [
    {
      key: 'mentions',
      icon: 'voiceprint-fill',
      customClass: 'summary-card--neutral',
      metrics: [
        {
          label: '总提及量',
          value: fmtNum(brief?.mentions),
          tag: fmtFix(brief?.mentionsMoM)
        }
      ]
    },
    {
      key: 'negativeMentions',
      icon: 'nsr-zm',
      customClass: 'summary-card--negative',
      metrics: [
        {
          label: '负面提及量',
          value: fmtNum(brief?.negativeMentions),
          tag: fmtFix(brief?.negativeMentionsMoM)
        },
        {
          label: '负面率',
          value: fmtPer(brief?.negativeRate),
          tag: fmtFix(brief?.negativeRateMoM),
          valueClassName: 'summary-card__value--negative'
        }
      ]
    },
    {
      key: 'positiveMentions',
      icon: 'nsr-my',
      customClass: 'summary-card--positive',
      metrics: [
        {
          label: '正面提及量',
          value: fmtNum(brief?.positiveMentions),
          tag: fmtFix(brief?.positiveMentionsMoM)
        },
        {
          label: '正面率',
          value: fmtPer(brief?.positiveRate),
          tag: fmtFix(brief?.positiveRateMoM)
        }
      ]
    },
    {
      key: 'users',
      icon: 'users02',
      iconColor: '#5F6A7A',
      customClass: 'summary-card--neutral',
      metrics: [
        {
          label: '用户数',
          value: fmtNum(brief?.users),
          tag: fmtFix(brief?.usersMoM)
        }
      ]
    }
  ]
})

/**
 * 获取趋势分析所需的顶部卡片与趋势图数据。
 * - 使用 `allSettled` 让两个区域的请求彼此隔离，任一失败都不会阻断另一块渲染
 * @param params 外部触发刷新时透传的查询参数
 */
const fetchTrendAnalysisData = async (params?: VocQueryParams) => {
  const queryParams = getMergedQueryParams(params)

  // 筛选器完成报告条件回填前，下钻组件会先以空参数挂载。
  // 日期是趋势和简报接口的必填项，应等待完整条件后再请求。
  if (!queryParams.startDate || !queryParams.endDate) {
    chartData.value = null
    summaryData.value = null
    loading.value = false
    return
  }

  loading.value = true
  const [trendResponse, briefResponse] = await Promise.allSettled([
    getDataTrendChange(queryParams),
    getDrillDownBrief(queryParams)
  ])

  if (
    trendResponse.status === 'fulfilled' &&
    trendResponse.value.success &&
    trendResponse.value.result
  ) {
    chartData.value = trendResponse.value.result as unknown as ProductTrendVo
  } else {
    chartData.value = null
    if (trendResponse.status === 'rejected') {
      console.error('获取声量趋势失败:', trendResponse.reason)
    }
  }

  if (
    briefResponse.status === 'fulfilled' &&
    briefResponse.value.success &&
    briefResponse.value.result
  ) {
    summaryData.value = briefResponse.value.result
  } else {
    summaryData.value = null
    if (briefResponse.status === 'rejected') {
      console.error('获取下钻简报卡片失败:', briefResponse.reason)
    }
  }

  loading.value = false
}

/**
 * 对外暴露刷新能力，供弹窗 Tab 切换或筛选变化时主动触发。
 * @param params 需要覆盖的查询参数
 */
const refresh = (params?: VocQueryParams) => {
  fetchTrendAnalysisData(params)
}

defineExpose({ refresh })

onMounted(() => {
  fetchTrendAnalysisData()
})

watch(
  () => props.queryParams,
  () => {
    fetchTrendAnalysisData()
  },
  { deep: true }
)

/**
 * 趋势图点击后继续沿用现有下钻链路。
 * 这里仍以日期维度作为下钻入口，不改变原有行为。
 * @param params ECharts 点击事件参数
 */
const chartClick = (params: ChartClickParams) => {
  const item = params.data
  if (!item?.date) return

  const requestParams = {
    ...getDateRange(item.date)
  }
  const text = item.date

  // 记录来源：趋势
  ddStore.updateDDViewParams({ lastDrillFrom: DrillTabKey.TREND })
  ddStore.drillDown(requestParams, [{ text, value: requestParams, deletable: false }])
}
</script>

<template>
  <div class="trend-analysis h-full flex flex-col overflow-y-auto pb-2">
    <TrendSummaryCards :cards="summaryCards" />

    <DataTrend
      v-loading="loading"
      class="trend-analysis__chart"
      :data-trend-change-data="chartData"
      @chart-click="chartClick"
      :is-show-title="true"
      :is-show-legend="true"
      :is-borderless="true"
    />
  </div>
</template>

<style lang="scss" scoped>
.trend-analysis {
  gap: 4px;
}
</style>
