<template>
  <FEcharts
    :options="chartOptions"
    :width="width"
    :height="height"
    :auto-resize="true"
    :auto-update="true"
    @chart-click="handleChartClick"
    @chart-ready="handleChartReady"
  />
</template>

<script setup lang="ts">
import { shallowRef, watch } from 'vue'
import { CHART_THEME_COLORS } from '@/constants'
import type { EChartsOption } from 'echarts'
import type { FieldMapping, TooltipFormatter } from './types.d'

defineOptions({
  name: 'FLineChart'
})

interface Props {
  data: Array<Record<string, any>>
  fieldMapping: FieldMapping
  width?: string
  height?: string
  smooth?: boolean
  showSymbol?: boolean
  showLegend?: boolean
  showGrid?: boolean
  tooltipFormatter?: TooltipFormatter
  clickable?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  width: '100%',
  height: '100%',
  smooth: true,
  showSymbol: false,
  showLegend: true,
  showGrid: true,
  tooltipFormatter: undefined,
  clickable: false
})

const emit = defineEmits<{
  chartClick: [params: any]
  chartReady: [chart: any]
}>()

// 使用 shallowRef 管理图表配置项，避免深度响应式带来的性能问题
const chartOptions = shallowRef<EChartsOption>({})

// 生成图表配置
const generateChartOptions = (
  data: Array<Record<string, any>>,
  fieldMapping: FieldMapping
): EChartsOption => {
  if (!data?.length || !fieldMapping) {
    return {}
  }
  // 提取 X 轴数据
  const xAxisData = data.map(item => item[fieldMapping.xAxis])

  // 生成系列数据
  const series: any = fieldMapping.series.map((seriesConfig, index) => ({
    name: seriesConfig.name,
    type: 'line',
    smooth: props.smooth,
    symbol: props.showSymbol ? 'circle' : 'none',
    symbolSize: 6,
    data: data.map((item, dataIndex) => ({
      value: item[seriesConfig.field],
      // 保留完整的原始数据用于tooltip
      ...item,
      // 保留数据索引和系列配置
      _dataIndex: dataIndex,
      _seriesConfig: seriesConfig
    })),
    lineStyle: {
      color: seriesConfig.color || CHART_THEME_COLORS[index % CHART_THEME_COLORS.length],
      width: 2
    },
    itemStyle: {
      color: seriesConfig.color || CHART_THEME_COLORS[index % CHART_THEME_COLORS.length]
    }
  }))

  // 构建基础配置
  const option: EChartsOption = {
    grid: {
      left: '3%',
      right: '3%',
      bottom: '3%',
      top: props.showLegend ? '12%' : '8%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: xAxisData,
      axisLine: {
        lineStyle: {
          color: '#DDE3EE'
        }
      },
      axisTick: {
        show: false
      },
      axisLabel: {
        color: '#4E5969',
        fontSize: 12
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      splitLine: {
        show: props.showGrid,
        lineStyle: {
          color: '#F0F3FA',
          type: 'dashed'
        }
      },
      axisLabel: {
        color: '#4E5969',
        fontSize: 12
      }
    },
    series
  }

  // 添加图例配置
  if (props.showLegend && fieldMapping.series.length > 1) {
    option.legend = {
      data: fieldMapping.series.map(s => s.name),
      top: 0,
      right: 0,
      itemWidth: 10,
      itemHeight: 10,
      textStyle: {
        color: '#4E5969',
        fontSize: 12
      }
    }
  }

  // 配置 tooltip
  if (props.tooltipFormatter) {
    option.tooltip = {
      formatter: props.tooltipFormatter,
      trigger: 'axis',
      backgroundColor: '#fff',
      borderWidth: 0,
      borderRadius: 4,
      padding: 0,
      extraCssText: 'box-shadow: 0 1px 8px rgba(0,0,0,0.1);'
    }
  } else {
    // 使用默认的趋势变化 tooltip
    // const defaultTooltip = getTrendChangeTooltip('line', {
    //   数值: 'value'
    // })
    option.tooltip = {
      // ...defaultTooltip,
      trigger: 'axis',
      backgroundColor: '#fff',
      borderWidth: 0,
      borderRadius: 4,
      padding: 0,
      extraCssText: 'box-shadow: 0 1px 8px rgba(0,0,0,0.1);'
    }
  }

  return option
}

// 处理图表点击事件
const handleChartClick = (params: any) => {
  if (props.clickable) {
    emit('chartClick', params)
  }
}

// 处理图表初始化完成事件
const handleChartReady = (chart: any) => {
  emit('chartReady', chart)
}

// 监听数据变化，动态更新图表配置
watch(
  () => [
    props.data,
    props.fieldMapping,
    props.smooth,
    props.showSymbol,
    props.showLegend,
    props.showGrid
  ],
  ([newData, newFieldMapping]) => {
    if (!Array.isArray(newData) || !newData.length || !newFieldMapping) {
      chartOptions.value = {}
      return
    }

    chartOptions.value = generateChartOptions(newData, newFieldMapping as FieldMapping)
  },
  { immediate: true, deep: true }
)

// 监听 tooltip 配置变化
watch(
  () => props.tooltipFormatter,
  () => {
    if (props.data?.length && props.fieldMapping) {
      chartOptions.value = generateChartOptions(props.data, props.fieldMapping)
    }
  }
)
</script>

<style scoped>
/* 组件样式 */
</style>
