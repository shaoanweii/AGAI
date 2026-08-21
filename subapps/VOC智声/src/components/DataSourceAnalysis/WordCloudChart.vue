<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts/core'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { TooltipComponent } from 'echarts/components'
import 'echarts-wordcloud'
import type { WordCloudItem } from './types.d'
import { WORD_CLOUD_RANDOM_COLOR_PALETTE } from '@/constants'
import { createWordCloudTextColorResolver } from '@/utils/wordCloud'

use([CanvasRenderer, TooltipComponent])

type WordCloudColorMap = Partial<Record<string, string>>

interface WordCloudLayout {
  sizeRange: [number, number]
  gridSize: number
  width: string
  height: string
}

const DEFAULT_COLOR_MAP: Record<string, string> = {
  正面: '#82E3C7',
  中性: '#60B8EB',
  负面: '#FF8A8B'
}

const props = withDefaults(
  defineProps<{
    /** 词云数据 */
    data?: WordCloudItem[]
    /** 是否启用横向椭圆布局 */
    ellipse?: boolean
    /** 是否启用随机色板模式 */
    randomPalette?: boolean
    /** 随机色板 */
    colorPalette?: readonly string[]
    /** 不透明高亮词条数量 */
    highlightTopCount?: number
    /** 非高亮词条透明度 */
    dimOpacity?: number
    /** 自定义情感颜色映射 */
    colorMap?: WordCloudColorMap
  }>(),
  {
    data: () => [],
    ellipse: false,
    randomPalette: false,
    colorPalette: () => WORD_CLOUD_RANDOM_COLOR_PALETTE,
    highlightTopCount: 10,
    dimOpacity: 0.8,
    colorMap: () => ({})
  }
)

const emit = defineEmits<{ (e: 'wordClick', word: WordCloudItem): void }>()
const chartRef = ref<HTMLDivElement | null>(null)
let chart: echarts.ECharts | null = null
let resizeObserver: ResizeObserver | null = null

/**
 * 根据词条数量和布局模式计算字号与网格，椭圆模式优先展示更多词条。
 * @param count 词条数量
 * @returns 词云布局参数
 */
const getWordCloudLayout = (count: number): WordCloudLayout => {
  if (props.ellipse) {
    if (count >= 80) {
      return {
        sizeRange: [9, 22],
        gridSize: 5,
        width: '100%',
        height: '48%'
      }
    }

    if (count >= 40) {
      return {
        sizeRange: [10, 26],
        gridSize: 7,
        width: '100%',
        height: '48%'
      }
    }

    return {
      sizeRange: [12, 30],
      gridSize: 10,
      width: '100%',
      height: '48%'
    }
  }

  if (count > 100) {
    return {
      sizeRange: [10, 20],
      gridSize: 4,
      width: '90%',
      height: '90%'
    }
  }

  if (count > 60) {
    return {
      sizeRange: [10, 24],
      gridSize: 6,
      width: '90%',
      height: '90%'
    }
  }

  if (count > 30) {
    return {
      sizeRange: [12, 30],
      gridSize: 8,
      width: '90%',
      height: '90%'
    }
  }

  return {
    sizeRange: [16, 40],
    gridSize: 24,
    width: '90%',
    height: '90%'
  }
}

/**
 * 渲染词云图，支持默认圆形布局和下钻弹窗使用的横向椭圆布局。
 */
const renderChart = () => {
  if (!chartRef.value) return

  // 初始化图表实例
  if (!chart) {
    chart = echarts.init(chartRef.value)
    chart.on('click', (params: any) => {
      emit('wordClick', params.data)
    })
  }

  const displayData = Array.isArray(props.data) ? props.data : []
  // 没有数据时清空图表
  if (!displayData.length) {
    chart.clear()
    return
  }

  const layout = getWordCloudLayout(displayData.length)
  const colorMap = { ...DEFAULT_COLOR_MAP, ...props.colorMap }
  const resolveTextColor = props.randomPalette
    ? createWordCloudTextColorResolver(displayData, {
        palette: props.colorPalette,
        highlightTopCount: props.highlightTopCount,
        dimOpacity: props.dimOpacity
      })
    : null

  chart.setOption({
    tooltip: {
      show: true
    },
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
        drawOutOfBound: false,
        textStyle: {
          fontFamily: 'sans-serif',
          fontWeight: '500',
          color: function (params: any) {
            if (resolveTextColor) return resolveTextColor(params)
            return colorMap[params?.data?.sentiment] || colorMap.中性
          }
        },
        emphasis: {
          textStyle: {
            shadowBlur: 6,
            shadowColor: 'rgba(0, 0, 0, 0.3)'
          }
        },
        data: displayData
      }
    ]
  })
}

/**
 * 容器或窗口尺寸变化时同步调整图表，避免椭圆词云比例失准。
 */
const handleChartResize = () => {
  chart?.resize()
}

onMounted(() => {
  renderChart()
  window.addEventListener('resize', handleChartResize)

  if (chartRef.value && typeof ResizeObserver !== 'undefined') {
    resizeObserver = new ResizeObserver(handleChartResize)
    resizeObserver.observe(chartRef.value)
  }
})

watch(
  () => [
    props.data,
    props.ellipse,
    props.randomPalette,
    props.colorPalette,
    props.highlightTopCount,
    props.dimOpacity,
    props.colorMap
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

<style lang="scss" scoped>
.word-cloud-chart {
  width: 100%;
  height: 100%;
}
</style>
