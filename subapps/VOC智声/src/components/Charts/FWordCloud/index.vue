<template>
  <FEcharts
    :options="chartOptions"
    :width="width"
    :height="height"
    :theme="theme"
    @chart-click="handleChartClick"
  />
</template>

<script setup lang="ts">
import { computed, shallowRef, watch } from 'vue'
import { CHART_THEME_COLORS } from '@/constants'
// import { getWordCloudTooltip } from '@/utils/echartsTooltipConfig'
import type { EChartsOption } from 'echarts'
import type {
  WordCloudDataItem,
  WordCloudFieldMapping,
  TooltipFormatter,
  EmphasisStyle
} from './types'
import 'echarts-wordcloud'

defineOptions({
  name: 'FWordCloud'
})

interface Props {
  data: WordCloudDataItem[]
  width?: string
  height?: string
  sizeRange?: [number, number]
  gridSize?: number
  left?: string
  top?: string
  fontFamily?: string
  fontWeight?: string | number
  theme?: string
  tooltip?: boolean
  tooltipFormatter?: TooltipFormatter
  fieldMapping?: WordCloudFieldMapping
  drawOutOfBound?: boolean
  emphasis?: EmphasisStyle
}

const props = withDefaults(defineProps<Props>(), {
  width: '100%',
  height: '100%',
  sizeRange: () => [12, 30],
  gridSize: 8,
  left: 'center',
  top: 'center',
  fontFamily: 'sans-serif',
  fontWeight: 'bold',
  theme: '',
  tooltip: true,
  tooltipFormatter: undefined,
  fieldMapping: undefined,
  drawOutOfBound: false,
  emphasis: () => ({
    shadowBlur: 10,
    shadowColor: '#333'
  })
})

const emit = defineEmits<{
  chartClick: [params: any]
}>()

// 使用 shallowRef 管理图表配置
const chartOptions = shallowRef<EChartsOption>({})

// 计算处理后的数据
const processedData = computed(() => {
  const nameField = props.fieldMapping?.nameField || 'name'
  const valueField = props.fieldMapping?.valueField || 'value'

  return props.data.map(item => {
    const processedItem: any = { ...item }

    // 如果字段映射不是默认字段，则重新映射
    if (nameField !== 'name') {
      processedItem.name = item[nameField]
    }
    if (valueField !== 'value') {
      processedItem.value = item[valueField]
    }

    return processedItem
  })
})

// 监听数据变化，更新图表配置
watch(
  [() => props.data, () => processedData.value],
  () => {
    if (!props.data || props.data.length === 0) {
      chartOptions.value = {}
      return
    }

    // 获取tooltip配置
    const tooltipConfig = props.tooltip
      ? props.tooltipFormatter
        ? {
            show: true,
            formatter: props.tooltipFormatter
          }
        : {}
      : { show: false }

    chartOptions.value = {
      tooltip: tooltipConfig,
      series: [
        {
          type: 'wordCloud',
          shape: 'circle',
          left: props.left,
          top: props.top,
          width: '90%',
          height: '90%',
          sizeRange: props.sizeRange,
          rotationRange: [0, 0],
          rotationStep: 0,
          gridSize: props.gridSize,
          drawOutOfBound: props.drawOutOfBound,
          textStyle: {
            fontFamily: props.fontFamily,
            fontWeight: props.fontWeight,
            color: function () {
              return CHART_THEME_COLORS[Math.floor(Math.random() * CHART_THEME_COLORS.length)]
            }
          },
          emphasis: {
            textStyle: {
              shadowBlur: props.emphasis?.shadowBlur || 10,
              shadowColor: props.emphasis?.shadowColor || '#333'
            }
          },
          data: processedData.value
        } as any
      ]
    }
  },
  { immediate: true, deep: true }
)

// 处理图表点击事件
const handleChartClick = (params: any) => {
  emit('chartClick', params)
}
</script>
