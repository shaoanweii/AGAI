<template>
  <div class="horizontal-segmented-bar">
    <!-- 图表容器 -->
    <div class="chart-container">
      <FEcharts
        :options="chartOptions"
        :width="width"
        :height="height"
        @chart-click="handleChartClick"
      />
    </div>

    <!-- 自定义图例和百分比标签 -->
    <div v-if="showLegend" class="custom-legend mt-16">
      <div class="flex flex-wrap" style="gap: 10px">
        <div v-for="(item, index) in data" :key="item.title" class="legend-item">
          <div class="flex-between items-center">
            <div class="flex items-center">
              <div
                class="color-block"
                :style="{ backgroundColor: item.color || colors[index % colors.length] }"
              ></div>
              <div class="label ml-4 fs-14 fw-400">{{ item.title }}</div>
            </div>
            <div class="fs-14 text-primary fw-500">{{ formatLegendPercent(item.value) }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { HorizontalSegmentedBarProps, HorizontalSegmentedBarEvents } from './types'
import { fmtPer } from '@/utils'

// 定义组件名称
defineOptions({
  name: 'HorizontalSegmentedBar'
})

// 定义 Props
const props = withDefaults(defineProps<HorizontalSegmentedBarProps>(), {
  data: () => [],
  width: '100%',
  height: '200px',
  colors: () => ['#1677FF', '#0AADFF', '#FAB007', '#FE7840']
})

// 定义事件
const emit = defineEmits<HorizontalSegmentedBarEvents>()

// 内部状态
const chartInstance = ref<any>(null)

/**
 * 将分段条占比统一转换为数值，避免空值导致图例文案或图表计算异常。
 * @param value 原始占比值
 * @returns 可用于展示和图表堆叠的数值
 */
const normalizeSegmentValue = (value: number | string | null | undefined) => {
  const parsedValue = Number(value)
  return Number.isFinite(parsedValue) ? parsedValue : 0
}

/**
 * 统一格式化图例占比文案，空值场景兜底显示为 0%。
 * @param value 原始占比值
 * @returns 图例中展示的百分比文本
 */
const formatLegendPercent = (value: number | string | null | undefined) => {
  return fmtPer(normalizeSegmentValue(value))
}

/**
 * 处理图表点击事件
 * @param params ECharts 点击事件参数
 */
const handleChartClick = (params: any): void => {
  // params.data.data 包含完整的行数据
  emit('chartClick', params)
}

/**
 * 计算图表配置
 */
const chartOptions = computed(() => {
  if (!props.data || props.data.length === 0) {
    return {}
  }

  // 计算累积值用于堆叠
  const stackData: Array<
    | {
        title: string
        value: number
        percent?: string
        valueMoM?: string
        valueYoY?: string
        color?: string
        stackStart: number
        stackEnd: number
      }
    | any
  > = []
  let cumulative = 0

  for (const item of props.data) {
    const normalizedValue = normalizeSegmentValue(item.value)
    stackData.push({
      ...item,
      value: normalizedValue,
      stackStart: cumulative,
      stackEnd: cumulative + normalizedValue
    })
    cumulative += normalizedValue
  }

  let result = {
    grid: {
      left: '0',
      right: '0',
      top: '0',
      bottom: '0',
      containLabel: false
    },
    xAxis: {
      type: 'value',
      max: 100,
      show: false,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { show: false },
      splitLine: { show: false }
    },
    yAxis: {
      type: 'category',
      data: [props.categoryLabel ?? ''],
      show: props.showCategoryLabel === true,
      axisLine: { show: props.showCategoryLabel === true },
      axisTick: { show: props.showCategoryLabel === true },
      axisLabel: { show: props.showCategoryLabel === true }
    },
    tooltip: {
      trigger: 'item',
      formatter: (params: any) => {
        return `${params.seriesName || ''}<br/>占比：${fmtPer(params.value)}`
      }
    },
    series: stackData.map((item, index) => ({
      name: item.title,
      type: 'bar',
      stack: 'total',
      data: [
        {
          value: item.value,
          data: item // 将完整数据存储在data字段中
        }
      ],
      barWidth: 30,
      barGap: 20,
      itemStyle: {
        color: item.color || props.colors[index % props.colors.length],
        borderRadius: [0]
      },
      label: {
        show: false
      },
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowColor: 'rgba(0, 0, 0, 0.3)'
        }
      }
    }))
  }
  return result
})

// 监听数据变化
watch(
  () => props.data,
  () => {
    // 数据变化时图表会自动更新
  },
  { deep: true }
)

// 暴露方法给父组件
defineExpose({
  getChartInstance: () => chartInstance.value
})
</script>

<style scoped lang="scss">
.horizontal-segmented-bar {
  width: 100%;

  .chart-container {
    position: relative;
  }

  .color-block {
    width: 12px;
    height: 12px;
    border-radius: 2px;
  }

  .legend-item {
    width: calc((100% - 20px) / 3);
  }
}
</style>
