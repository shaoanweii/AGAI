<template>
  <div class="gender-donut-chart">
    <div class="chart-container" :style="{ width: width, height: height }">
      <!-- ECharts 图表容器 -->
      <div ref="chartRef" class="chart-wrapper"></div>

      <!-- 中心图标占位符 -->
      <div class="center-icons">
        <div class="icon-placeholder male-icon">
          <!-- 男性图标占位符 - 后续替换为实际图片 -->
          <div class="icon-bg male-bg">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="8" r="5"/>
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
            </svg>
          </div>
        </div>
        <div class="icon-placeholder female-icon">
          <!-- 女性图标占位符 - 后续替换为实际图片 -->
          <div class="icon-bg female-bg">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="8" r="5"/>
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <path d="M12 2v6"/>
              <path d="M8 5h8"/>
            </svg>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import type { EChartsOption, ECharts } from 'echarts'

defineOptions({ name: 'GenderDonutChart' })

interface Props {
  data: {
    male: number
    female: number
  }
  width?: string
  height?: string
}

const props = withDefaults(defineProps<Props>(), {
  width: '400px',
  height: '300px'
})

const emit = defineEmits<{
  chartClick: [params: any]
  chartReady: [chart: ECharts]
}>()

// 图表引用
const chartRef = ref<HTMLElement>()
const chartInstance = ref<ECharts>()

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return

  // 销毁现有实例
  if (chartInstance.value) {
    chartInstance.value.dispose()
  }

  // 创建新实例
  chartInstance.value = echarts.init(chartRef.value)

  // 设置图表配置
  const option: EChartsOption = {
    tooltip: {
      trigger: 'item',
      backgroundColor: '#fff',
      borderWidth: 0,
      borderRadius: 8,
      padding: [8, 12],
      extraCssText: 'box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);',
      formatter: (params: any) => {
        const gender = params.name === 'male' ? '男性' : '女性'
        return `${gender}<br/>数量: ${params.value}<br/>占比: ${params.percent}%`
      }
    },
    series: [
      {
        name: '性别分布',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '50%'],
        data: [
          {
            name: 'male',
            value: props.data.male,
            itemStyle: {
              color: '#87CEEB' // 男性蓝色
            }
          },
          {
            name: 'female',
            value: props.data.female,
            itemStyle: {
              color: '#FFB6C1' // 女性粉色
            }
          }
        ],
        label: {
          show: false
        },
        labelLine: {
          show: false
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 15,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.3)'
          }
        },
        animationType: 'scale',
        animationEasing: 'elasticOut',
        animationDelay: (idx: number) => idx * 200
      }
    ]
  }

  chartInstance.value.setOption(option)

  // 绑定事件
  chartInstance.value.on('click', (params) => {
    emit('chartClick', params)
  })

  // 触发就绪事件
  emit('chartReady', chartInstance.value)
}

// 更新图表数据
const updateChart = () => {
  if (!chartInstance.value) return

  const option = chartInstance.value.getOption() as echarts.EChartsOption
  if (option && Array.isArray(option.series) && option.series[0]) {
    option.series[0].data = [
      {
        name: 'male',
        value: props.data.male,
        itemStyle: {
          color: '#87CEEB'
        }
      },
      {
        name: 'female',
        value: props.data.female,
        itemStyle: {
          color: '#FFB6C1'
        }
      }
    ]
  }

  chartInstance.value.setOption(option)
}

// 监听数据变化
watch(
  () => props.data,
  () => {
    nextTick(() => {
      updateChart()
    })
  },
  { deep: true, immediate: true }
)

// 监听尺寸变化
watch(
  () => [props.width, props.height],
  () => {
    nextTick(() => {
      if (chartInstance.value) {
        chartInstance.value.resize()
      }
    })
  }
)

// 组件挂载
onMounted(() => {
  nextTick(() => {
    initChart()
  })
})

// 组件卸载
onUnmounted(() => {
  if (chartInstance.value) {
    chartInstance.value.dispose()
  }
})
</script>

<style lang="scss" scoped>
.gender-donut-chart {
  position: relative;
  width: 100%;
  height: 100%;

  .chart-container {
    position: relative;
    width: 100%;
    height: 100%;
  }

  .chart-wrapper {
    width: 100%;
    height: 100%;
  }

  .center-icons {
    position: absolute;
    left: 50%;
    top: 50%;
    transform: translate(-50%, -50%);
    display: flex;
    gap: 16px;
    z-index: 10;
    pointer-events: none; // 确保不影响图表交互

    .icon-placeholder {
      width: 32px;
      height: 32px;

      .icon-bg {
        width: 100%;
        height: 100%;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: all 0.3s ease;
        backdrop-filter: blur(4px);

        &.male-bg {
          background-color: rgba(135, 206, 235, 0.15);
          border: 2px solid rgba(135, 206, 235, 0.8);
          color: #87CEEB;
          box-shadow: 0 2px 8px rgba(135, 206, 235, 0.3);
        }

        &.female-bg {
          background-color: rgba(255, 182, 193, 0.15);
          border: 2px solid rgba(255, 182, 193, 0.8);
          color: #FFB6C1;
          box-shadow: 0 2px 8px rgba(255, 182, 193, 0.3);
        }

        svg {
          width: 100%;
          height: 100%;
          padding: 6px;
          filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.1));
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .gender-donut-chart {
    .center-icons {
      gap: 12px;

      .icon-placeholder {
        width: 28px;
        height: 28px;

        .icon-bg svg {
          padding: 5px;
        }
      }
    }
  }
}
</style>
