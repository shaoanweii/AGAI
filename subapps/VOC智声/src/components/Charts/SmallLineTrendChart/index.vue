<template>
  <FEcharts :options="chartOptions" :width="'64px'" :height="'26px'" />
</template>

<script setup lang="ts">
import { computed } from 'vue'

/**
 * 小型折线趋势图
 */
defineOptions({
  name: 'SmallLineTrendChart'
})

interface Props {
  data?: number[]
}

const props = withDefaults(defineProps<Props>(), {
  data: () => []
})

// 使用 computed 管理图表配置项，根据props动态生成
const chartOptions = computed((): any => {
  // 使用传入的数据
  const chartData = props.data || []

  // 生成对应的x轴数据
  const xAxisData = chartData.map((_, index) => `${index + 1}`)

  return {
    // 设置图表四周间距为0
    grid: {
      left: 0,
      right: 0,
      top: 0,
      bottom: 0,
      containLabel: false
    },
    xAxis: {
      show: false,
      type: 'category',
      boundaryGap: false,
      data: xAxisData,
      // 隐藏X轴分割线
      splitLine: {
        show: false
      }
    },
    yAxis: {
      show: false,
      type: 'value',
      // 隐藏Y轴分割线
      splitLine: {
        show: false
      }
    },
    series: [
      {
        data: chartData,
        type: 'line',
        // 设置折线光滑
        smooth: true,
        symbol: 'none',
        lineStyle: {
          width: 2,
          color: '#60B8EB'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              {
                offset: 0,
                color: 'rgba(138,204,255,0.8)'
              },
              {
                offset: 1,
                color: 'rgba(195,238,253,0)'
              }
            ]
          }
        }
      }
    ]
  }
})
</script>

<style scoped>
/* 组件样式 */
</style>
