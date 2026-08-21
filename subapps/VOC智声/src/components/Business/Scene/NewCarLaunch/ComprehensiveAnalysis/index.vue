<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import ReportSummary from '@/components/Business/Scene/Common/ReportSummary.vue'
import DataTrend from './DataTrend.vue'
import PeriodCards from './PeriodCards.vue'
import { getNewCarDataTrendChangeResult } from '@/api/reportSummary/index'

import useGeneralDrillDownStore from '@/store/modules/generalDrillDown'

interface Props {
  queryParams?: Record<string, any>
  queryStore?: any // 本地存储的数据
  productBriefData?: {
    preheat: any[]
    launch: any[]
    stable: any[]
  }
  dataTrendChangeData?: any
}

const props = withDefaults(defineProps<Props>(), {
  queryParams: () => ({})
})

// 定义emits
const emit = defineEmits<{
  'intention-top-sort': [{ intention: string; prop: string; order: string }]
  'table-row-click': any
  'chart-click': [data: any]
}>()

// 状态管理
const generalDrillDown = useGeneralDrillDownStore()

// 时期配置
const periodConfig = {
  preheat: {
    name: '预热期',
    color: '#E6F7FF',
    borderColor: '#91D5FF'
  },
  launch: {
    name: '上市期',
    color: '#F6FFED',
    borderColor: '#B7EB8F'
  },
  stable: {
    name: '稳定期',
    color: '#FFF7E6',
    borderColor: '#FFD591'
  }
}

const carName = computed(() => {
  const carInfo = props.queryStore?.currentQueryParams?.newCarSeriesObjList?.[0]
  const x = carInfo?.name ? `-${carInfo?.name}` : ''
  return x
})

// 转换接口数据为组件所需格式
const transformProductBriefData = (data: any[]) => {
  return data.map(item => ({
    ...item,
    brand: item.brandName || '',
    series: item.carSeriesName || item.seriesName || '',
    logo: item.logo || '',
    positiveRate:
      item.positiveRate !== undefined && item.positiveRate !== null
        ? `${item.positiveRate}%`
        : '0%',
    negativeRate:
      item.negativeRate !== undefined && item.negativeRate !== null
        ? `${item.negativeRate}%`
        : '0%',
    mentionCount: item.totalMentions || item.mentionCount || 0
  }))
}

// 计算属性：处理产品简报数据
const periodData = computed(() => {
  const phases = ['preheat', 'launch', 'stable'] as const

  return phases.reduce((acc, phase) => {
    acc[phase] = {
      ...periodConfig[phase],
      data: props.productBriefData?.[phase]
        ? transformProductBriefData(props.productBriefData[phase])
        : []
    }
    return acc
  }, {} as any)
})

// 数据趋势变化数据（从父组件传递）
const dataTrendChangeData = computed(() => {
  const data = props.dataTrendChangeData
  return data
})

// 处理卡片点击事件
const handleCardClick = (period: string, car: any, index: number) => {
  // index =0 为新品车系 index =1 为对比车系
  emit('table-row-click', { period, car, type: index, periodData })
}

// 重置排序状态的方法（供父组件调用）
const resetBrandSeriesRankSort = () => {
  // 这里可以添加重置排序状态的逻辑
}

// 处理图表点击事件
const handleChartClick = (data: any) => {
  emit('chart-click', data)
}

const showAreaTopTitle = [
  {
    type: 'rect',
    left: '66.66%',
    top: '0%',
    width: '33.34%',
    height: '100%',
    style: {
      fill: 'rgba(255, 228, 196, 0.2)',
      opacity: 0.5
    }
  },
  {
    type: 'text',
    left: '16.66%',
    top: '5%',
    style: {
      text: '预热期',
      fontSize: 14,
      fontWeight: 'bold',
      fill: '#666'
    }
  },
  {
    type: 'text',
    left: '50%',
    top: '5%',
    style: {
      text: '上市期',
      fontSize: 14,
      fontWeight: 'bold',
      fill: '#666'
    }
  },
  {
    type: 'text',
    left: '83.33%',
    top: '5%',
    style: {
      text: '稳定期',
      fontSize: 14,
      fontWeight: 'bold',
      fill: '#666'
    }
  }
]

defineExpose({
  resetBrandSeriesRankSort
})
</script>

<template>
  <div class="comprehensive-analysis">
    <!-- 报告解读 -->
    <div class="report-summary-wrapper">
      <ReportSummary
        :api-function="getNewCarDataTrendChangeResult"
        :query-params="props.queryParams"
      ></ReportSummary>
    </div>

    <!-- 三个时期的数据卡片 -->
    <PeriodCards
      :period-data="periodData"
      :query-params="props.queryParams"
      @card-click="handleCardClick"
    />

    <!-- 数据趋势变化 -->
    <div class="data-trend-wrapper mt-24">
      <h3 class="section-title">数据趋势变化{{ carName }}</h3>
      <DataTrend
        :dataTrendChangeData="dataTrendChangeData"
        :queryStore="queryStore"
        :is-show-title="false"
        :showAreaTopTitle="showAreaTopTitle"
        pageLineType="newCarTrendLine"
        @chart-click="handleChartClick"
      />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.comprehensive-analysis {
  width: 100%;

  .report-summary-wrapper {
    margin-bottom: 24px;
  }

  .data-trend-wrapper {
    .section-title {
      margin-bottom: 16px;
    }
  }

  // 通用样式
  .cursor-pointer {
    cursor: pointer;
  }

  .hover-bg {
    transition: all 0.3s ease;

    &:hover {
      background-color: rgba(22, 125, 255, 0.05);
      border-radius: 8px;
    }
  }
}
</style>
