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

const props = defineProps<{ data: WordCloudItem[] }>()
const emit = defineEmits<{ 'word-click': [word: WordCloudItem] }>()
const chartRef = ref<HTMLDivElement | null>(null)
let chart: echarts.ECharts | null = null
let resizeObserver: ResizeObserver | null = null

/**
 * 按词条数量压缩字号和网格间距，优先在不重叠、不越界的前提下展示更多数据。
 * @param count 词云词条数量
 * @returns 词云布局参数
 */
const getWordCloudLayout = (count: number) => {
  if (count >= 80) {
    return {
      sizeRange: [10, 24] as [number, number],
      gridSize: 6
    }
  }

  if (count >= 40) {
    return {
      sizeRange: [12, 28] as [number, number],
      gridSize: 8
    }
  }

  return {
    sizeRange: [14, 32] as [number, number],
    gridSize: 12
  }
}

/**
 * 格式化同比/环比百分比，空值或非法数字统一展示占位符。
 * @param val 后端返回的百分比数值
 * @returns 带符号的一位小数字符串
 */
const formatValue = (val: unknown) => {
  if (val === null || val === undefined) return '-'
  const num = Number(val)
  return isNaN(num) ? '-' : `${num > 0 ? '+' : ''}${num.toFixed(1)}%`
}

/**
 * 渲染词云图；通过横向绘图区让 circle 形状按插件椭圆率生成椭圆云。
 */
const renderChart = () => {
  if (!chartRef.value) return
  if (!chart) {
    chart = echarts.init(chartRef.value)
    chart.on('click', (params: any) => {
      emit('word-click', params.data)
    })
  }

  const displayData = Array.isArray(props.data) ? props.data : []
  const layout = getWordCloudLayout(displayData.length)
  const resolveTextColor = createWordCloudTextColorResolver(displayData, {
    palette: WORD_CLOUD_RANDOM_COLOR_PALETTE,
    highlightTopCount: 10,
    dimOpacity: 0.8
  })

  chart.setOption({
    tooltip: {
      show: true,
      confine: true,
      backgroundColor: 'white',
      borderColor: '#E5E6EB',
      borderWidth: 1,
      padding: 12,
      textStyle: {
        color: '#333',
        fontSize: 14
      },
      formatter: (params: any) => {
        const data = params.data
        return `
          <div style="background: white; border-radius: 4px; padding: 0; font-size: 12px; min-width: 200px;">
            <div class="mb-12 fs-14 fw-500" style="color: #333; margin-bottom: 12px;">
              ${data.name || '-'}
            </div>
            <table style="width: 100%; border-collapse: collapse; margin: 0;">
              <thead>
                <tr style="background: #f0f8ff;">
                  <th style="padding: 8px 12px; text-align: left; color: #26292E; font-weight: 400; font-size: 14px;">名称</th>
                  <th style="padding: 8px 12px; text-align: center; color: #26292E; font-weight: 400; font-size: 14px;">数值</th>
                </tr>
              </thead>
              <tbody>
                <tr style="background: white;">
                  <td style="padding: 8px 12px; color: #333; font-size: 14px;">提及量</td>
                  <td style="padding: 8px 12px; text-align: center; color: #333; font-size: 14px;">${data.value || 0}</td>
                </tr>
              </tbody>
            </table>
          </div>
        `
      }
    },
    series: [
      {
        type: 'wordCloud',
        shape: 'circle',
        left: 'center',
        top: 'center',
        width: '96%',
        height: '62%',
        keepAspect: false,
        sizeRange: layout.sizeRange,
        rotationRange: [0, 0],
        gridSize: layout.gridSize,
        layoutAnimation: true,
        drawOutOfBound: false,
        textStyle: {
          fontFamily: 'sans-serif',
          fontWeight: '500',
          color: function (params: any) {
            return resolveTextColor(params)
          }
        },
        data: displayData
      }
    ]
  })
}

/**
 * 响应容器尺寸变化，避免弹性布局变化后椭圆词云位置或比例失准。
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

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleChartResize)
  resizeObserver?.disconnect()
  chart?.dispose()
  chart = null
  resizeObserver = null
})

watch(() => props.data, renderChart, { deep: true })
</script>

<template>
  <div ref="chartRef" class="word-cloud-chart"></div>
</template>

<style lang="scss" scoped>
.word-cloud-chart {
  width: 100%;
  height: 677px;
}
</style>
