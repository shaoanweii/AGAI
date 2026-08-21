<script setup lang="ts">
import { computed, ref, watch, nextTick, shallowRef } from 'vue'
import FEcharts from '@/components/Charts/FEcharts/index.vue'
import type { ECharts, EChartsOption } from 'echarts'
import malePng from '@/assets/images/male.png'
import femalePng from '@/assets/images/female.png'

defineOptions({ name: 'GenderDistributionChart' })

// 接口返回结构适配
// gender 性别 string | 可能为 '男'/'女' 或 '男性'/'女性' 或 'male'/'female'
// value 用户数量 number
// percent 占比(%) number
// valueMoM 环比(%) number
// valueYoY 同比(%) number
interface ApiItem {
  gender: string
  value: number
  percent?: number
  valueMoM?: number
  valueYoY?: number
}

interface Props {
  data: ApiItem[]
  width?: string
  height?: string
  theme?: string
}

const props = withDefaults(defineProps<Props>(), {
  width: '400px',
  height: '300px',
  theme: ''
})

const emit = defineEmits<{
  chartClick: [params: any]
  chartReady: [chart: any]
}>()

// 图表尺寸
const chartWidth = ref('100%')
const chartHeight = ref('100%')

// 图表实例 & 选项
const chartInstance = ref<any>(null)
const chartOptions = shallowRef<EChartsOption>({})

// 颜色常量：0-女性，1-男性
const customColors = ['#FF8A8B', '#60B8EB', '#aaa']

// 格式化数据到 ECharts pie 数据   女性在前
const pieSeriesData = computed(() => {
  return props.data.map((item: ApiItem) => (
    {
      ...item,
      name: item.gender
    }
  )).sort((a, b) => {
    if (a.name === '女') return -1
    if (b.name === '女') return 1
    return 0
  })
})

// 构建 ECharts 配置
const buildOptions = (): EChartsOption => {
  return {
    color: customColors,
    tooltip: { trigger: 'item' },
    series: [
      {
        name: '性别分布',
        type: 'pie',
        radius: ['40%', '55%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: true,
        label: {
          show: true,
          position: 'outside',
          color: 'inherit',
          formatter: (params: any) => {
            return `${params.percent}%\n{b|${params.name}}`
          },
          rich: {
            a: {
              fontSize: 14,
              fontWeight: 500
            },
            b: {
              fontSize: 14,
              color: '#5F6A7A',
              fontWeight: 500,
              lineHeight: 24
            },
          },
        },
        labelLine: { show: false },
        data: pieSeriesData.value
      }
    ]
  }
}

// 初始化/更新选项
watch(
  () => [props.data, props.width, props.height],
  () => {
    chartWidth.value = props.width
    chartHeight.value = props.height
    chartOptions.value = buildOptions()
  },
  { immediate: true, deep: true }
)

// 处理图表点击事件
const handleChartClick = (params: any) => {
  emit('chartClick', params)
}

// 处理图表就绪事件
const handleChartReady = (chart: ECharts) => {
  chartInstance.value = chart
  emit('chartReady', chart)
}

</script>
<template>
  <div class="gender-distribution-chart">
    <div class="chart-container" :style="{ width: width, height: height }">
      <!-- 饼图容器 -->
      <div class="pie-chart-wrapper">
        <FEcharts
          :options="chartOptions"
          :width="chartWidth"
          :height="chartHeight"
          :theme="theme"
          @chart-click="handleChartClick"
          @chart-ready="handleChartReady($event)"
        />

        <!-- 中心图标和文字 -->
        <div class="center-content">
          <div class="gender-icons">
            <el-image :src="malePng"  style="width: 27px;height: 34px"/>
            <el-image :src="femalePng"  style="width: 27px;height: 34px"/>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<style lang="scss" scoped>
.gender-distribution-chart {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;

  .chart-container {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .pie-chart-wrapper {
    position: relative;
    width: 100%;
    height: 100%;
  }

  .center-content {
    position: absolute;
    left: 50%;
    top: 50%;
    transform: translate(-50%, -50%);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    z-index: 10;

    .gender-icons {
      display: flex;
      gap: 6px;
      margin-bottom: 8px;
    }
  }

  // 左右侧标签，贴近参考图效果
  .side-label {
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;

    &--left { left: 0; }
    &--right { right: 0; }

    &__percent { font-size: 16px; font-weight: 600; }
    &__name { color: #666; font-size: 14px; }
  }
}
</style>
