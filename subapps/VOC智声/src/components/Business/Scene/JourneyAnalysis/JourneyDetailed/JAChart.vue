<script setup lang="ts">
import { computed, ref } from 'vue'
import { debounce } from 'lodash-es'
import { emojiMap } from '@/constants'
import type { JourneyDetailAnalysisVo } from '@/api/journeyAnalysis/types'
import { formatChartPop } from '@utils/chart.ts'

defineOptions({
  name: 'JAChart'
})

const { data } = defineProps<{
  data: JourneyDetailAnalysisVo[]
}>()

// 计算属性：处理图表数据
const chartData = computed(() => {
  if (!data || data.length === 0) {
    return {
      xAxisData: [],
      negativeRates: [],
      averageRate: 0
    }
  }

  const xAxisData = data.map(item => item.tagName || '')
  const negativeRates = data.map(item => ({
    ...item,
    name: item.tagName,
    value: item.value || 0,
    valueMoM: item.valueMoM,
    valueYoY: item.valueYoY
  }))

  // 使用第一项的valueAvg作为负面平均值
  const averageRate = data[0]?.valueAvg || 0

  return {
    xAxisData,
    negativeRates,
    averageRate
  }
})

// 事件定义
const emit = defineEmits<{
  (e: 'chart-click', data: JourneyDetailAnalysisVo): void
}>()

// 图表点击防抖标志位
const isChartClicking = ref(false)

// 处理图表点击事件（内部实现）
const handleChartClickInternal = (params: any) => {
  // 防止重复点击
  if (isChartClicking.value) {
    return
  }

  if (params.data) {
    isChartClicking.value = true
    emit('chart-click', params.data)

    // 延迟重置标志位，确保防抖生效
    setTimeout(() => {
      isChartClicking.value = false
    }, 300)
  }
}

// 处理图表点击事件（防抖版本）
const handleChartClick = debounce(handleChartClickInternal, 300)

// 使用 shallowRef 管理图表配置项，避免深度响应式带来的性能问题
const chartOptions = computed<any>(() => ({
  grid: {
    left: 30,
    right: 10,
    top: 30,
    bottom: 0,
    containLabel: true
  },
  tooltip: {
    show: true,
    trigger: 'axis',
    axisPointer: {
      type: 'line',
      lineStyle: {
        type: 'dashed',
        color: '#999',
        width: 1
      },
      label: {
        show: false
      }
    },
    formatter: (params: any) => {
      return formatChartPop(params, 'negativeRate')
    }
  },
  legend: {
    show: false
  },
  xAxis: [
    {
      type: 'category',
      data: chartData.value.xAxisData,
      axisLabel: {
        color: '#666',
        fontSize: 12
      },
      boundaryGap: true,
      axisLine: {
        show: true,
        lineStyle: {
          color: '#E4E7ED'
        }
      },
      axisTick: {
        show: true,
        inside: true,
        alignWithLabel: true,
        length: 300,
        lineStyle: {
          color: '#E4E7ED'
        }
      }
    }
  ],
  yAxis: [
    {
      type: 'value',
      name: '负面率(%)',
      nameTextStyle: {
        color: '#666',
        fontSize: 12,
        padding: [0, 0, 0, -40]
      },
      axisLabel: {
        color: '#666',
        fontSize: 12,
        formatter: '{value}%'
      },
      axisLine: {
        lineStyle: {
          color: '#E4E7ED'
        }
      },
      splitLine: {
        show: true,
        lineStyle: {
          color: '#EAEDF4',
          type: 'dashed'
        }
      }
    }
  ],
  series: [
    {
      name: '负面率',
      type: 'line',
      lineStyle: {
        width: 3,
        color: '#FF8A8B',
        type: 'dashed'
      },
      itemStyle: {
        color: '#FF8A8B'
      },
      data: chartData.value.negativeRates?.map((el: any) => {
        return {
          ...el,
          symbol: `image://${emojiMap[el.emotionType]}`,
          symbolSize: 32
        }
      })
    },
    {
      name: '平均值',
      type: 'line',
      // symbol: 'none',
      lineStyle: {
        width: 2,
        color: '#84D6FF',
        type: 'dashed'
      },
      markLine: {
        silent: true,
        symbol: 'triangle',
        symbolSize: 8,
        lineStyle: {
          color: '#84D6FF',
          type: 'dashed',
          width: 2
        },
        data: [
          {
            yAxis: chartData.value.averageRate,
            label: {
              show: true,
              position: 'insideStartTop',
              formatter: `负面平均值: ${chartData.value.averageRate}%`,
              color: '#717680',
              fontSize: 12,
              lineHeight: 24
            }
          }
        ]
      },
      data: []
    }
  ]
}))
</script>

<template>
  <FEcharts
    :options="chartOptions"
    :isEmpty="!chartData.xAxisData || chartData.xAxisData.length === 0"
    :width="'100%'"
    :height="'100%'"
    @chart-click="handleChartClick"
  />
</template>

<style lang="scss" scoped></style>
