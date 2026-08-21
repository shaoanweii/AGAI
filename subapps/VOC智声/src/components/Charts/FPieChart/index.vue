<template>
  <FEcharts
    :options="chartOptions"
    :width="width"
    :height="height"
    :theme="theme"
    @chart-click="handleChartClick"
    @chart-ready="handleChartReady"
  />
</template>

<script setup lang="ts">
import { computed, shallowRef, watch } from 'vue'
import { CHART_THEME_COLORS } from '@/constants'
import type { EChartsOption } from 'echarts'
import type { PieDataItem, PieFieldMapping, TooltipFormatter, LabelPosition } from './types'

interface Props {
  data: PieDataItem[]
  width?: string
  height?: string
  radius?: string | string[]
  center?: string[]
  labelPosition?: LabelPosition
  showLabel?: boolean
  showPercentage?: boolean
  theme?: string
  tooltipFormatter?: TooltipFormatter
  fieldMapping?: PieFieldMapping
}

const props = withDefaults(defineProps<Props>(), {
  width: '100%',
  height: '100%',
  radius: () => ['40%', '70%'],
  center: () => ['50%', '50%'],
  labelPosition: 'outside',
  showLabel: true,
  showPercentage: false,
  theme: '',
  tooltipFormatter: undefined,
  fieldMapping: undefined
})

const emit = defineEmits<{
  chartClick: [params: any]
  chartReady: [chart: any]
}>()

// 使用 shallowRef 管理图表配置
const chartOptions = shallowRef<EChartsOption>({})

// 计算处理后的数据
const processedData = computed(() => {
  const nameField = props.fieldMapping?.nameField || 'name'
  const valueField = props.fieldMapping?.valueField || 'value'

  return props.data.map(item => {
    // 只保留name和value字段
    return {
      name: item[nameField],
      value: item[valueField]
    }
  })
})

// 默认的 tooltip 格式化函数
const defaultTooltipFormatter = (params: any) => {
  const percentage = props.showPercentage ? ` (${params.percent}%)` : ''
  return `${params.seriesName}<br/>${params.name}: ${params.value}${percentage}`
}

// 监听数据变化，更新图表配置
watch(
  [
    () => props.data,
    () => processedData.value,
    () => props.labelPosition,
    () => props.showLabel,
    () => props.showPercentage,
    () => props.radius
  ],
  () => {
    const formatter = props.tooltipFormatter || defaultTooltipFormatter

    // 确保即使空数据时也有series结构
    const seriesData = props.data && props.data.length > 0 ? processedData.value : []

    chartOptions.value = {
      tooltip: {
        trigger: 'item',
        backgroundColor: '#fff',
        borderWidth: 0,
        borderRadius: 4,
        padding: 0,
        extraCssText: 'box-shadow: 0 1px 8px rgba(0,0,0,0.1);',
        formatter
      },
      series: [
        {
          name: '数据分布',
          type: 'pie',
          radius: props.radius,
          center: props.center,
          data: seriesData,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          },
          label: {
            show: props.showLabel,
            position: props.labelPosition,
            formatter: props.showPercentage
              ? (params: any) => `${params.name}: ${params.percent}%`
              : '{b}'
          },
          labelLine: {
            show: props.labelPosition === 'outside'
          },
          itemStyle: {
            color: (params: any) => CHART_THEME_COLORS[params.dataIndex % CHART_THEME_COLORS.length]
          }
        }
      ]
    }
  },
  { immediate: true, deep: true }
)

// 处理图表点击事件
const handleChartClick = (params: any) => {
  emit('chartClick', params)
}

// 处理图表就绪事件
const handleChartReady = (chart: any) => {
  emit('chartReady', chart)
}
</script>
