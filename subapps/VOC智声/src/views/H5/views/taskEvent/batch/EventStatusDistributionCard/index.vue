<script setup lang="ts">
import { computed } from 'vue'
import type { EChartsOption } from 'echarts'
import HCard from '@h5/components/UI/HCard/index.vue'
import HEcharts from '@h5/components/UI/HEcharts/index.vue'
import { fmtNum, fmtPer } from '@/utils'
import { eventStatusMeta } from '@h5/constants'
import type { EventStatusDistributionProps, EventStatusRawItem } from './types'

defineOptions({
  name: 'BatchEventStatusDistributionCard'
})

// 组件入参
const props = withDefaults(defineProps<EventStatusDistributionProps>(), {
  data: () => [],
  loading: false
})

// 事件状态展示项
interface StatusDisplayItem {
  key: string
  label: string
  color: string
  count: number
  rate: number
}

// 将单个或多个状态编码统一成字符串数组，便于按后端状态码分组匹配。
const normalizeStatusCodes = (codes: string | string[]) => {
  return Array.isArray(codes) ? codes.map(String) : [String(codes)]
}

// 读取接口状态编码，兼容历史字段。
const getStatusCode = (row: EventStatusRawItem) => {
  return String(row.taskStatus ?? row.statusCode ?? '')
}

// 汇总同一状态分组的数量。
const sumStatusCount = (rows: EventStatusRawItem[]) => {
  return rows.reduce((sum, row) => sum + (row.currentCounts ?? row.count ?? 0), 0)
}

// 汇总同一状态分组的占比。
const sumStatusRate = (rows: EventStatusRawItem[]) => {
  return rows.reduce((sum, row) => sum + (row.percent ?? 0), 0)
}

// 从接口返回中取状态名称；没有名称时不使用前端默认文案兜底。
const getRemoteStatusName = (rows: EventStatusRawItem[]) => {
  return rows.find(row => row.taskStatusName?.trim())?.taskStatusName?.trim() || ''
}

// 将接口返回的状态数据转换为展示项；空数据不补齐前端默认状态。
const statusList = computed<StatusDisplayItem[]>(() => {
  const rawList: EventStatusRawItem[] = props.data || []

  if (!rawList.length) return []

  const knownStatusCodes = new Set<string>()

  const knownStatusItems = eventStatusMeta
    .map((meta: any) => {
      const codes = normalizeStatusCodes(meta.codes)
      codes.forEach(code => knownStatusCodes.add(code))
      const matchedRows = rawList.filter(row => codes.includes(getStatusCode(row)))
      const label = getRemoteStatusName(matchedRows)

      if (!matchedRows.length || !label) return null

      return {
        key: meta.key,
        label,
        color: meta.color,
        count: sumStatusCount(matchedRows),
        rate: sumStatusRate(matchedRows)
      }
    })
    .filter((item): item is StatusDisplayItem => Boolean(item))

  const unknownStatusItems = rawList
    .map((row, index) => {
      const code = getStatusCode(row)
      const label = getRemoteStatusName([row])
      return {
        key: code || `unknown-${index}`,
        label,
        color: '#86909C',
        count: row.currentCounts ?? row.count ?? 0,
        rate: row.percent ?? 0,
        code
      }
    })
    .filter(item => item.label && !knownStatusCodes.has(item.code))
    .map(item => ({
      key: item.key,
      label: item.label,
      color: item.color,
      count: item.count,
      rate: item.rate
    }))

  return [...knownStatusItems, ...unknownStatusItems]
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
    <div v-if="statusList.length" class="pt-10 event-status-card flex-y-center">
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
    <div v-else class="event-status-empty">
      <van-empty class="event-status-empty__content" description="暂无数据" :image-size="80" />
    </div>
  </HCard>
</template>

<style scoped lang="scss">
.event-status-empty {
  height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;

  :deep(.event-status-empty__content) {
    padding: 0;
  }
}

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
