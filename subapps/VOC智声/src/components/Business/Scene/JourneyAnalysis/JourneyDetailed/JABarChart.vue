<script setup lang="ts">
import { computed, ref } from 'vue'
import { debounce } from 'lodash-es'
import type { JourneyDetailAnalysisVo } from '@/api/journeyAnalysis/types'
import { formatChartPop } from '@utils/chart.ts'

defineOptions({
  name: 'JABarChart'
})

const { data, dataType = 'mention' } = defineProps<{
  data: JourneyDetailAnalysisVo[]
  dataType?: MentionNegativeRateType
}>()

// 计算属性：处理图表数据
const chartData = computed(() => {
  if (!data || data.length === 0) {
    return {
      xAxisData: [],
      values: [],
      averageRate: 0
    }
  }

  const xAxisData = data.map(item => item.tagName || '')
  const values = data.map(item => ({
    ...item,
    name: item.tagName,
    value: item.value || 0,
    valueMoM: item.valueMoM,
    valueYoY: item.valueYoY
  }))

  // 使用第一项的valueAvg作为平均值
  const averageRate = data[0]?.valueAvg || 0

  return {
    xAxisData,
    values,
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

const chartOptions = computed<any>(() => ({
  grid: {
    left: 0,
    right: 0,
    top: 30,
    bottom: 0,
    containLabel: true
  },
  tooltip: {
    show: true,
    trigger: 'axis',
    axisPointer: {
      type: 'shadow',
      shadowStyle: {
        color: 'rgba(0,0,0,0.1)'
      }
    },
    formatter: (params: any) => {
      return formatChartPop(params, dataType)
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
      axisLine: {
        show: false,
        lineStyle: {
          color: '#E4E7ED'
        }
      },
      splitLine: {
        show: true,
        lineStyle: {
          color: '#EAEDF4',
          type: 'solid'
        }
      }
    }
  ],
  yAxis: [
    {
      type: 'value',
      name: dataType === 'negativeRate' ? '负面率(%)' : '提及量',
      nameTextStyle: {
        color: '#666',
        fontSize: 12,
        padding: [0, 0, 0, -40]
      },
      axisLabel: {
        color: '#666',
        fontSize: 12,
        formatter: dataType === 'negativeRate' ? '{value}%' : '{value}'
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
      name: dataType === 'negativeRate' ? '负面率' : '提及量',
      type: 'bar',
      barWidth: 24,
      itemStyle: {
        color: '#7298D0'
      },
      data: chartData.value.values
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
              formatter: `提及量均值: ${chartData.value.averageRate}`,
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
