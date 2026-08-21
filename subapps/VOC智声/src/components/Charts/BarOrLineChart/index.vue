<script setup lang="ts">
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts/core'
import { BarChart, LineChart } from 'echarts/charts'
import { TooltipComponent, GridComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { ECharts, EChartsCoreOption } from 'echarts/core'
import type { VocDataItem } from '@/api/common/index.d'

echarts.use([BarChart, LineChart, TooltipComponent, GridComponent, LegendComponent, CanvasRenderer])

// 定义组件 Props
interface Props {
  data: VocDataItem[]
  width?: string | number
  height?: string | number
  needDetails?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  width: '100%',
  height: 400,
  needDetails: false
})

const emit = defineEmits<{
  (e: 'barClick', payload: { date: string }): void
}>()

const chartRef = ref<HTMLDivElement | null>(null)
let chartInstance: ECharts | null = null

// 工具函数：数据格式化
const formatUtils = {
  formatNum(data: number | string) {
    const n = parseFloat(String(data))
    if (isNaN(n)) return '-'
    return n.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
  },
  makeDataUnit(data: number | string) {
    let n = Math.abs(Number(data))
    if (isNaN(n)) return '-'
    let unit = ''
    if (n >= 1e8) {
      n = n / 1e8
      unit = '亿'
    } else if (n >= 1e7) {
      n = n / 1e7
      unit = '千万'
    } else if (n >= 1e4) {
      n = n / 1e4
      unit = '万'
    } else if (n >= 1e3) {
      n = n / 1e3
      unit = 'K'
    }
    return n.toFixed(unit ? 2 : 0) + unit
  }
}

// 处理数据并生成图表配置
const getChartOption = (): EChartsCoreOption => {
  const xData = props.data.map(item => item.date || '')
  const positive = props.data.map(item => item.positiveMentions || 0)
  const neutral = props.data.map(item => item.neutralMentions || 0)
  const negative = props.data.map(item => -Math.abs(item.negativeMentions || 0))
  const experience = props.data.map(item => item.experienceValue || 0)

  return {
    color: ['#0C92E0', '#3ED4A9', '#FF4A4D', '#5D7092'],
    legend: {
      show: true,
      bottom: 0,
      itemWidth: 14,
      data: ['中性提及量', '正面提及量', '负面提及量', '体验值']
    },
    grid: {
      top: 55,
      right: 10,
      bottom: 40,
      left: 10,
      containLabel: true
    },
    tooltip: {
      trigger: 'axis',
      padding: 0,
      axisPointer: {
        type: 'cross',
        shadowStyle: { color: 'rgba(41, 148, 255, 0.1)' }
      },
      borderColor: '#0077FF',
      formatter(params: any) {
        // params: Array<{ seriesName, value, marker, axisValueLabel }>
        const order = ['体验值', '正面提及量', '中性提及量', '负面提及量']
        const arr: string[] = []
        let total = 0
        let axisValueLabel = ''
        params.forEach((item: any) => {
          if (!axisValueLabel)
            axisValueLabel = `<div class='axis-name'>${item.axisValueLabel}</div>`
          let showData = ''
          if (item.seriesName === '体验值') {
            showData = formatUtils.formatNum(item.value)
          } else {
            showData = formatUtils.makeDataUnit(item.value)
          }
          if (['正面提及量', '中性提及量', '负面提及量'].includes(item.seriesName)) {
            total += Math.abs(item.value)
          }
          arr[order.indexOf(item.seriesName)] =
            `<div class='each-series'><span class='each-series-name'>${item.marker}${item.seriesName}：</span><span class='each-series-value'>${showData}</span></div>`
        })
        let html = `<div class='public-tooltip-div'>${axisValueLabel}${arr.join('')}`
        html += `<div class='each-series'><span class='each-series-name'>&nbsp;&nbsp;&nbsp;总提及量：</span><span class='each-series-value'>${formatUtils.makeDataUnit(total)}</span></div></div>`
        if (props.needDetails) {
          html += `<div class='public-tooltip-click-tips'>点击图形可查看分析</div>`
        }
        return html
      }
    },
    xAxis: {
      type: 'category',
      data: xData,
      axisLabel: {
        margin: 20,
        fontSize: 14,
        rotate: xData.length >= 5 ? 35 : 0,
        width: 100
      },
      triggerEvent: true,
      axisPointer: { type: 'shadow' },
      splitLine: { show: true },
      axisTick: { show: false },
      axisLine: { show: false },
      nameTextStyle: { color: 'rgba(0,0,0,0.45)' }
    },
    yAxis: [
      {
        name: '提及量',
        show: true,
        axisLabel: {
          show: true,
          formatter: (value: number) => formatUtils.makeDataUnit(value)
        },
        splitLine: { show: true, lineStyle: { color: '#F0F0F0' } },
        splitArea: {
          interval: 1,
          show: true,
          areaStyle: { color: ['rgba(255,255,255,0)', 'rgba(250,250,250,1)'] }
        },
        axisTick: { show: false },
        axisLine: { show: false },
        nameTextStyle: { color: 'rgba(0,0,0,0.45)' }
      },
      {
        name: '体验值',
        show: true,
        axisLabel: { show: true },
        splitLine: { show: true, lineStyle: { color: '#F0F0F0' } },
        splitArea: {
          interval: 1,
          show: true,
          areaStyle: { color: ['rgba(255,255,255,0)', 'rgba(250,250,250,1)'] }
        },
        axisTick: { show: false },
        axisLine: { show: false },
        nameTextStyle: { color: 'rgba(0,0,0,0.45)' }
      }
    ],
    series: [
      {
        name: '中性提及量',
        type: 'bar',
        stack: 'total',
        data: neutral,
        itemStyle: {
          borderRadius: 1,
          borderColor: '#fff',
          borderWidth: 0.3,
          emphasis: { shadowBlur: 10, shadowColor: '#0077FF' }
        },
        barMaxWidth: 25
      },
      {
        name: '正面提及量',
        type: 'bar',
        stack: 'total',
        data: positive,
        itemStyle: {
          borderRadius: 1,
          borderColor: '#fff',
          borderWidth: 0.3,
          emphasis: { shadowBlur: 10, shadowColor: '#0077FF' }
        },
        barMaxWidth: 25
      },
      {
        name: '负面提及量',
        type: 'bar',
        stack: 'total',
        data: negative,
        itemStyle: {
          borderRadius: 1,
          borderColor: '#fff',
          borderWidth: 0.3,
          emphasis: { shadowBlur: 10, shadowColor: '#0077FF' }
        },
        barMaxWidth: 25
      },
      {
        name: '体验值',
        type: 'line',
        yAxisIndex: 1,
        data: experience,
        smooth: true,
        lineStyle: { color: '#5D7092' },
        itemStyle: { color: '#5D7092' }
      }
    ]
  }
}

// 渲染图表
const renderChart = () => {
  if (!chartRef.value) return
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  chartInstance.setOption(getChartOption())
}

// 监听数据变化
watch(
  () => props.data,
  () => {
    renderChart()
  },
  { deep: true }
)

// 监听尺寸变化
watch(
  () => [props.width, props.height],
  () => {
    if (chartInstance) {
      chartInstance.resize()
    }
  }
)

// 组件挂载
onMounted(() => {
  renderChart()
  window.addEventListener('resize', resizeChart)

  // 绑定点击事件
  if (chartInstance) {
    chartInstance.on('click', (params: any) => {
      if (params.componentType === 'series' && params.seriesType === 'bar') {
        emit('barClick', { date: params.name })
      }
    })
  }
})

// 窗口大小变化时重新调整图表大小
function resizeChart() {
  if (chartInstance) chartInstance.resize()
}

// 组件卸载前清理资源
onBeforeUnmount(() => {
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
  window.removeEventListener('resize', resizeChart)
})
</script>

<template>
  <div
    ref="chartRef"
    class="bar-or-line-chart"
    :style="{
      width: typeof props.width === 'number' ? `${props.width}px` : props.width,
      height: typeof props.height === 'number' ? `${props.height}px` : props.height
    }"
  ></div>
</template>

<style scoped lang="scss">
.bar-or-line-chart {
  width: 100%;
  height: 400px;
}
</style>
