<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts/core'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { TooltipComponent } from 'echarts/components'
import 'echarts-wordcloud'

use([CanvasRenderer, TooltipComponent])

type WordCloudFontWeight = string | number
type WordCloudSizeRange = [number, number]

interface WordCloudLayout {
  sizeRange: WordCloudSizeRange
  gridSize: number
  width: string
  height: string
}

const props = withDefaults(defineProps<{
  data: any[]
  activeName?: string
  activeColor?: string
  activeFontWeight?: WordCloudFontWeight
  defaultFontWeight?: WordCloudFontWeight
}>(), {
  activeColor: '#1677FF',
  activeFontWeight: '500',
  defaultFontWeight: '500'
})
const emit = defineEmits<{ (e: 'wordClick', word: any): void }>()
const chartRef = ref<HTMLDivElement | null>(null)
let chart: echarts.ECharts | null = null
let resizeObserver: ResizeObserver | null = null

/**
 * 根据词条数量动态压缩字号和网格，优先保证 H5 小画布内能放下更多词条。
 * @param count 词条数量
 * @returns 词云布局参数
 */
const getWordCloudLayout = (count: number): WordCloudLayout => {
  if (count >= 18) {
    return {
      sizeRange: [11, 16],
      gridSize: 3,
      width: '100%',
      height: '100%'
    }
  }

  if (count >= 12) {
    return {
      sizeRange: [12, 17],
      gridSize: 4,
      width: '100%',
      height: '100%'
    }
  }

  return {
    sizeRange: [14, 20],
    gridSize: 8,
    width: '100%',
    height: '100%'
  }
}

const renderChart = () => {
  if (!chartRef.value) return
  if (!chart) {
    chart = echarts.init(chartRef.value)
    chart.on('click', (params: any) => {
      emit('wordClick', params.data)
    })
  }

  const displayData = Array.isArray(props.data) ? props.data : []
  if (!displayData.length) {
    chart.clear()
    return
  }

  const layout = getWordCloudLayout(displayData.length)
  const colorMap: any = { 正面: '#82E3C7', 中性: '#60B8EB', 负面: '#FF8A8B' }
  const textStyle: any = {
    fontFamily: 'sans-serif',
    fontWeight: props.defaultFontWeight,
    color: function (params: any) {
      if (props.activeName && params?.data?.name === props.activeName) {
        return props.activeColor
      }
      return colorMap[params?.data?.sentiment] || '#60B8EB'
    }
  }

  if (props.activeFontWeight !== props.defaultFontWeight) {
    textStyle.fontWeight = function (word: string) {
      if (props.activeName && word === props.activeName) {
        return props.activeFontWeight
      }
      return props.defaultFontWeight
    }
  }

  chart.setOption({
    tooltip: { show: false },
    series: [
      {
        type: 'wordCloud',
        shape: 'circle',
        left: 'center',
        top: 'center',
        width: layout.width,
        height: layout.height,
        keepAspect: false,
        sizeRange: layout.sizeRange,
        rotationRange: [0, 0],
        gridSize: layout.gridSize,
        layoutAnimation: true,
        drawOutOfBound: false,
        textStyle,
        data: displayData
      }
    ]
  })
}

/**
 * 容器尺寸变化后重新计算词云位置，避免首次布局后父容器高度被样式覆盖导致丢词。
 */
const handleChartResize = () => {
  chart?.resize()
  renderChart()
}

onMounted(() => {
  nextTick(renderChart)
  window.addEventListener('resize', handleChartResize)

  if (chartRef.value && typeof ResizeObserver !== 'undefined') {
    resizeObserver = new ResizeObserver(handleChartResize)
    resizeObserver.observe(chartRef.value)
  }
})

watch(
  () => [
    props.data,
    props.activeName,
    props.activeColor,
    props.activeFontWeight,
    props.defaultFontWeight
  ],
  renderChart,
  { deep: true }
)

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleChartResize)
  resizeObserver?.disconnect()
  chart?.dispose()
  chart = null
  resizeObserver = null
})
</script>

<template>
  <div ref="chartRef" class="word-cloud-chart"></div>
</template>

<style scoped lang="scss">
.word-cloud-chart {
  width: 100%;
  height: 100%;
}
</style>
