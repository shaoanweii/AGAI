<script setup lang="ts">
import { computed } from 'vue'
import type { EChartsOption } from 'echarts'
import HCard from '@h5/components/UI/HCard/index.vue'
import HEcharts from '@h5/components/UI/HEcharts/index.vue'
import { fmtNum, fmtPer } from '@/utils'
import { eventStatusMeta } from '@h5/constants'
import type { EventStatusDistributionProps, EventStatusRawItem } from './types'

defineOptions({
  name: 'SingleEventStatusDistributionCard'
})

// 组件入参
const props = withDefaults(defineProps<EventStatusDistributionProps>(), {
  data: () => [],
  loading: false
})

// 事件状态展示项（不再做叠加聚合，直接按接口状态展示）
interface StatusDisplayItem {
  key: string
  label: string
  color: string
  count: number
  rate: number
}

// 将接口返回的状态数据按预设顺序补齐并计算占比
const statusList = computed<StatusDisplayItem[]>(() => {
  const rawList: EventStatusRawItem[] = props.data || []

  // 按配置初始化结构，保证展示顺序及默认值
  const base: StatusDisplayItem[] = eventStatusMeta.map((item: any) => ({
    key: item.key,
    label: item.label,
    color: item.color,
    count: 0,
    rate: 0
  }))

  if (!rawList.length) return base

  // 按“状态分组 key”做聚合，兼容接口返回 10/11/20/30/40/90 等不同粒度
  const countMap = new Map<string, any>()
  rawList.forEach(row => {
    const code = String(row.taskStatus ?? '')
    countMap.set(code, {
      ...row,
      key: code
    })
  })

  // 计算占比，最后一个用100减去前面的总和
  let accumulatedRate = 0
  return base.map((item, index) => {
    const row = countMap.get(item.key)
    const count = row?.currentCounts ?? 0
    let rate: number
    if (index === base.length - 1) {
      // 最后一个数据用100减去之前的累计占比
      rate = Number((100 - accumulatedRate).toFixed(1))
    } else {
      rate = row?.percent ?? 0
      accumulatedRate += Number(rate)
    }
    return {
      ...item,
      count,
      rate
    }
  })
})

// 事件总数
const totalCount = computed(() =>
  statusList.value.reduce((sum: number, item: any) => sum + (item.count || 0), 0)
)

// 饼图配置
const chartOptions = computed<EChartsOption>(() => {
  const total = totalCount.value
  const seriesData = statusList.value.map((item: any) => ({
    name: item.label,
    value: item.count,
    itemStyle: {
      color: item.color
    }
  }))

  const colors = statusList.value.map((item: any) => item.color)

  return {
    color: colors,
    tooltip: {
      show: false,
      trigger: 'item',
      confine: true,
      formatter: (params: any) => {
        const value = params.data?.value ?? 0
        const rate = total ? (value / total) * 100 : 0
        return [`${params.name}`, `数量：${fmtNum(value)}`, `占比：${fmtPer(rate)}`].join('<br/>')
      }
    },
    legend: {
      show: false
    },
    series: [
      {
        type: 'pie',
        radius: ['65%', '100%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: false,
        label: {
          show: false
        },
        labelLine: {
          show: false
        },
        data: seriesData,
        itemStyle: {
          borderColor: '#FFFFFF',
          borderWidth: 2
        },
        emphasis: {
          disabled: true
        }
      }
    ],
    // 中心总数文案
    graphic: [
      {
        type: 'group',
        left: 'center',
        top: 'center',
        children: [
          {
            type: 'text',
            left: 'center',
            top: -16,
            style: {
              text: fmtNum(total) || '-',
              fill: '#5F6A7A',
              fontSize: 14,
              fontWeight: 500,
              align: 'center'
            }
          },
          {
            type: 'text',
            left: 'center',
            top: 6,
            style: {
              text: '事件总数',
              fill: '#5F6A7A',
              fontSize: 12,
              fontWeight: 400,
              align: 'center'
            }
          }
        ]
      }
    ]
  }
})
</script>

<template>
  <!-- 卡片高度略大于图表高度，避免图表和中心文字被裁切 -->
  <HCard title="事件状态分布">
    <div class="pt-10 event-status-card flex-y-center">
      <div class="chart-area">
        <HEcharts :options="chartOptions" height="140px" width="100%" />
      </div>
      <div class="legend-area">
        <div
          v-for="item in statusList"
          :key="item.key"
          class="legend-row flex-between flex-y-center"
        >
          <div class="legend-left flex-y-center">
            <span class="legend-dot" :style="{ backgroundColor: item.color }"></span>
            <span class="legend-label">{{ item.label }}</span>
          </div>
          <div class="legend-right">
            <span class="legend-count mr-8">{{ fmtNum(item.count) }}</span>
            <span class="legend-percent">{{ fmtPer(item.rate) }}</span>
          </div>
        </div>
      </div>
    </div>
  </HCard>
</template>

<style scoped lang="scss">
.event-status-card {
  display: flex;
  align-items: center;
}

.chart-area {
  flex: 0 0 44%;
  min-width: 0;
}

.legend-area {
  flex: 1;
  padding-left: 12px;
}

.legend-row {
  margin-bottom: 8px;

  &:last-child {
    margin-bottom: 0;
  }
}

.legend-left {
  .legend-dot {
    display: inline-block;
    width: 8px;
    height: 8px;
    border-radius: 50%;
    margin-right: 8px;
  }

  .legend-label {
    font-weight: 400;
    font-size: 12px;
    color: #6e7b91;
    line-height: 20px;
  }
}

.legend-right {
  display: flex;
  align-items: baseline;
  font-size: 12px;
  color: #1f2733;

  .legend-count {
    text-align: right;
    font-weight: 500;
    font-size: 14px;
    color: #5f6a7a;
    line-height: 14px;
  }

  .legend-percent {
    min-width: 50px;
    text-align: right;
    font-weight: 500;
    font-size: 14px;
    color: #1f2733;
    line-height: 14px;
  }
}
</style>
