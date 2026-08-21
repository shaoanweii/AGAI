<script setup lang="ts">
import { computed } from 'vue'
import type { ChannelNegativeTrendVo } from '@/api/productAnalysis/types'
import { CHART_THEME_COLORS } from '@/constants'
import { hexToRgba, formatChartPop_trend } from '@utils/chart.ts'
import { fmtFix, fmtNum, fmtPer } from '@/utils'

defineOptions({
  name: 'RateTrendChart'
})

// 接收数据
interface Props {
  data?: ChannelNegativeTrendVo[]
  dataType?: MentionNegativeRateType
}

const props = withDefaults(defineProps<Props>(), {
  data: () => [],
  dataType: 'negativeRate'
})

// 颜色配置
const colors = CHART_THEME_COLORS

// 处理图表数据
const chartData = computed(() => {
  if (!props.data || props.data.length === 0) {
    return {
      xAxisData: [],
      seriesData: [],
      legendData: []
    }
  }

  const propData = props.data

  // 获取所有日期（从数据中提取）
  const xAxisData = propData.map(item => item.date || '')

  // 获取所有渠道名称（从第一个日期的 chDatas 中提取）
  const chDatas0 = propData[0]?.chDatas || []
  const legendData = chDatas0.map(ch => ch.channelName || '-')

  // 构建系列数据 - 按渠道组织数据
  const seriesData = legendData.map((serieName, index) => {
    const color = colors[index % colors.length]
    // 为每个渠道收集所有日期的数据
    const serieData = propData.map(itemData => {
      // name可能为null，所以用code
      const subData = itemData.chDatas.find(ch => (ch.channelName || '-') === serieName)

      // return subData?.value || 0
      return {
        value: subData?.value || 0,
        valueMoM: subData?.valueMoM,
        valueYoY: subData?.valueYoY,
        channelName: subData?.channelName || serieName,
        date: itemData.date || ''
      }
    })

    return {
      name: serieName,
      type: 'line',
      data: serieData,
      symbol: 'circle',
      symbolSize: 6,
      smooth: true,
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            {
              offset: 0,
              color: hexToRgba(color, 0.1)
            },
            {
              offset: 1,
              color: hexToRgba(color, 0)
            }
          ]
        }
      },
      itemStyle: {
        color: color,
        opacity: 0
      },
      emphasis: {
        itemStyle: {
          opacity: 1
        }
      },
      lineStyle: {
        color: colors[index % colors.length]
      }
    }
  })

  // console.log('props.data@@111',props.data)
  // console.log('seriesData@@111',seriesData)

  // 构建图例选中状态对象 - 默认只显示前5条
  const legendSelected = legendData.reduce(
    (acc, name, index) => {
      acc[name] = index < 5
      return acc
    },
    {} as Record<string, boolean>
  )

  return {
    xAxisData,
    seriesData,
    legendData,
    legendSelected
  }
})

// Y轴配置
const yAxisConfig = computed(() => {
  if (props.dataType === 'mention') {
    // 提及量模式：显示数值，不设置最大值
    return {
      type: 'value',
      axisLabel: {
        formatter: '{value}'
      }
    }
  } else {
    // 负面率模式：显示百分比，设置最大值为100
    return {
      type: 'value',
      axisLabel: {
        formatter: '{value}%'
      },
      max: 100
    }
  }
})

const chartOptions = computed((): any => ({
  grid: {
    left: 0,
    right: 30,
    top: 30,
    bottom: 30,
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
      return formatChartPop_trend(params, props.dataType)
    }
  },
  legend: {
    type: 'scroll',
    data: chartData.value.legendData,
    selected: chartData.value.legendSelected,
    top: 0
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    axisTick: { show: false },
    data: chartData.value.xAxisData
  },
  yAxis: yAxisConfig.value,
  series: chartData.value.seriesData
}))
// console.log('chartOptions@@11111', chartData.value)

// 事件定义
const emit = defineEmits<{
  (e: 'chart-click', data: ChannelNegativeTrendVo): void
}>()

// 处理图表点击事件
const handleChartClick = (params: any) => {
  console.log('handleChartClick--->', params)
  console.log('props.data--->', props.data)
  if (params.data && props.data && props.data.length > 0) {
    // 查找对应的数据
    const dateIndex = params.dataIndex
    const seriesName = params.seriesName
    const dateData = props.data[dateIndex]
    const channelData = dateData?.chDatas.find(ch => ch.channelName === seriesName)

    if (channelData && dateData) {
      emit('chart-click', {
        date: dateData.date,
        chDatas: [channelData]
      } as ChannelNegativeTrendVo)
    }
  }
}

// 空状态描述
const emptyDescription = computed(() => {
  return props.dataType === 'mention' ? '暂无渠道提及量趋势数据' : '暂无渠道负面率趋势数据'
})
</script>

<template>
  <FEcharts
    :options="chartOptions"
    :width="'100%'"
    :height="'260px'"
    :isEmpty="!props.data || props.data.length === 0"
    :emptyDescription="emptyDescription"
    @chart-click="handleChartClick"
  />
</template>

<style scoped>
/* 组件样式 */
</style>
