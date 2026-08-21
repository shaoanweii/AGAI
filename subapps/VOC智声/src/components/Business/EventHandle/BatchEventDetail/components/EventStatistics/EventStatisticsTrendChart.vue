<script setup lang="ts">
import { computed } from 'vue'
import type { EChartsOption } from 'echarts'
import FEcharts from '@/components/Charts/FEcharts/index.vue'
import { formatAxisLabel, fmtNum } from '@/utils'

interface EventTrendSeriesItem {
  date: string
  positiveMentions: number
  neutralMentions: number
  negativeMentions: number
}

interface YAxisRange {
  min: number
  max: number
  interval: number
}

defineOptions({
  name: 'EventStatisticsTrendChart'
})

const props = defineProps<{
  series: EventTrendSeriesItem[]
}>()

/**
 * 图表是否为空。
 * 由父组件接口数据统一走空态兜底，避免图表初始化异常。
 */
const isEmpty = computed(() => props.series.length === 0)

const Y_AXIS_SPLIT_NUMBER = 5
const TREND_SENTIMENT_COLORS = {
  positive: '#82E3C7',
  neutral: '#60B8EB',
  negative: '#FF8A8B'
} as const

/**
 * 根据粗略步长转换为更适合坐标轴展示的 1/2/5/10 阶梯步长。
 * @param roughStep 原始步长
 * @returns 坐标轴友好步长
 */
const getNiceAxisStep = (roughStep: number) => {
  if (!Number.isFinite(roughStep) || roughStep <= 0) return 1

  const exponent = Math.floor(Math.log10(roughStep))
  const base = Math.pow(10, exponent)
  const fraction = roughStep / base

  if (fraction <= 1) return base
  if (fraction <= 2) return 2 * base
  if (fraction <= 5) return 5 * base

  return 10 * base
}

/**
 * 根据趋势数据自动生成 Y 轴范围。
 * 提及量为计数类数据，步长至少为 1；非负数据的下界不低于 0。
 * @param values 三条趋势线的全部数值
 * @returns Y 轴最小值、最大值和刻度间隔
 */
const buildYAxisRange = (values: number[]): YAxisRange => {
  const validValues = values.filter(value => Number.isFinite(value))

  if (validValues.length === 0) {
    return {
      min: 0,
      max: Y_AXIS_SPLIT_NUMBER,
      interval: 1
    }
  }

  const minValue = Math.min(...validValues)
  const maxValue = Math.max(...validValues)

  if (minValue === maxValue) {
    const padding = Math.max(1, getNiceAxisStep(Math.abs(maxValue) * 0.1))
    const min = minValue >= 0 ? Math.max(0, minValue - padding) : minValue - padding
    const max = maxValue + padding
    const interval = Math.max(1, getNiceAxisStep((max - min) / Y_AXIS_SPLIT_NUMBER))

    return {
      min: Math.floor(min / interval) * interval,
      max: Math.ceil(max / interval) * interval,
      interval
    }
  }

  const range = maxValue - minValue
  const padding = range * 0.1
  const paddedMin = minValue - padding
  const paddedMax = maxValue + padding
  const interval = Math.max(
    1,
    getNiceAxisStep((paddedMax - paddedMin) / Y_AXIS_SPLIT_NUMBER)
  )
  const min = minValue >= 0 ? Math.max(0, paddedMin) : paddedMin

  return {
    min: Math.floor(min / interval) * interval,
    max: Math.ceil(paddedMax / interval) * interval,
    interval
  }
}

/**
 * ECharts 配置。
 * 统一展示三条折线、图例、坐标轴与悬浮提示，不引入额外交互。
 */
const chartOptions = computed<EChartsOption>(() => {
  const xAxisData = props.series.map(item => item.date)
  const positiveData = props.series.map(item => item.positiveMentions)
  const neutralData = props.series.map(item => item.neutralMentions)
  const negativeData = props.series.map(item => item.negativeMentions)
  const yAxisRange = buildYAxisRange([...positiveData, ...neutralData, ...negativeData])

  return {
    color: [
      TREND_SENTIMENT_COLORS.positive,
      TREND_SENTIMENT_COLORS.neutral,
      TREND_SENTIMENT_COLORS.negative
    ],
    grid: {
      left: 0,
      right: 16,
      top: 24,
      bottom: 48,
      containLabel: true
    },
    tooltip: {
      trigger: 'axis',
      confine: true,
      backgroundColor: 'rgba(255, 255, 255, 0.96)',
      borderColor: '#E5E7EB',
      borderWidth: 1,
      textStyle: {
        color: '#1F2733'
      },
      formatter(params) {
        if (!Array.isArray(params) || params.length === 0) {
          return ''
        }

        const dataIndex = params[0].dataIndex
        const current = props.series[dataIndex]
        if (!current) {
          return ''
        }

        return [
          `<div style="font-size: 14px; font-weight: 600; margin-bottom: 8px;">${current.date}</div>`,
          `<div style="display:flex;justify-content:space-between;gap:16px;"><span style="color:${TREND_SENTIMENT_COLORS.positive};">正面提及量</span><span>${fmtNum(current.positiveMentions)}</span></div>`,
          `<div style="display:flex;justify-content:space-between;gap:16px;"><span style="color:${TREND_SENTIMENT_COLORS.neutral};">中性提及量</span><span>${fmtNum(current.neutralMentions)}</span></div>`,
          `<div style="display:flex;justify-content:space-between;gap:16px;"><span style="color:${TREND_SENTIMENT_COLORS.negative};">负面提及量</span><span>${fmtNum(current.negativeMentions)}</span></div>`
        ].join('')
      }
    },
    legend: {
      bottom: 0,
      left: 'center',
      itemWidth: 12,
      itemHeight: 12,
      icon: 'roundRect',
      textStyle: {
        color: '#6E7B91',
        fontSize: 14
      },
      data: ['正面提及量', '中性提及量', '负面提及量']
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: xAxisData,
      axisTick: {
        show: false
      },
      axisLine: {
        lineStyle: {
          color: '#D0D5DD'
        }
      },
      axisLabel: {
        color: '#98A2B3',
        fontSize: 14
      }
    },
    yAxis: {
      type: 'value',
      min: yAxisRange.min,
      max: yAxisRange.max,
      interval: yAxisRange.interval,
      splitNumber: Y_AXIS_SPLIT_NUMBER,
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      axisLabel: {
        color: '#98A2B3',
        fontSize: 14,
        formatter: value => formatAxisLabel(Number(value))
      },
      splitLine: {
        lineStyle: {
          color: '#EAECEF',
          type: 'dashed'
        }
      }
    },
    series: [
      {
        name: '正面提及量',
        type: 'line',
        smooth: true,
        symbol: 'none',
        lineStyle: {
          width: 4,
          color: TREND_SENTIMENT_COLORS.positive
        },
        itemStyle: {
          color: TREND_SENTIMENT_COLORS.positive
        },
        data: positiveData
      },
      {
        name: '中性提及量',
        type: 'line',
        smooth: true,
        symbol: 'none',
        lineStyle: {
          width: 4,
          color: TREND_SENTIMENT_COLORS.neutral
        },
        itemStyle: {
          color: TREND_SENTIMENT_COLORS.neutral
        },
        data: neutralData
      },
      {
        name: '负面提及量',
        type: 'line',
        smooth: true,
        symbol: 'none',
        lineStyle: {
          width: 4,
          color: TREND_SENTIMENT_COLORS.negative
        },
        itemStyle: {
          color: TREND_SENTIMENT_COLORS.negative
        },
        data: negativeData
      }
    ]
  }
})
</script>

<template>
  <FCard
    title="数据趋势变化"
    titleSize="small"
    :height="'543px'"
    class="event-statistics-trend f-card-border"
  >
    <FEcharts
      :options="chartOptions"
      :is-empty="isEmpty"
      :width="'100%'"
      :height="'100%'"
      empty-description="暂无趋势数据"
    />
  </FCard>
</template>

<style lang="scss" scoped>
.event-statistics-trend {
  :deep(.fc-body) {
    height: calc(543px - 48px) !important;
  }
}
</style>
