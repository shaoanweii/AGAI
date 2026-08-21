<template>
  <FEcharts :options="chartOptions" :width="'94px'" :height="'46px'" />
  <!-- <FEcharts :options="chartOptions" :width="'94px'" :is-empty="!props.trendData" :height="'46px'" /> -->
</template>

<script setup lang="ts">
import { computed } from 'vue'
/**
 * 小型折线趋势图
 */
defineOptions({
  name: 'LineTrend'
})

interface Props {
  /** 趋势数据 */
  trendData?: number[]
  /** 是否平滑曲线 */
  smooth?: boolean
  /** 是否显示符号点 */
  showSymbol?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  trendData: () => [],
  smooth: true,
  showSymbol: false
})

const fmtTrend = (list:any):number[] =>  {
  if(!list) return []
  else return  list.map((item:any) => {
    return parseFloat(item)
  })
}

// 计算图表配置项
const chartOptions = computed((): any => {
  // 使用传入的趋势数据，如果没有则使用默认数据
  const seriesData = fmtTrend(props.trendData)

  // 生成对应的X轴数据
  const xAxisData = seriesData.map((_, index) => `Day${index + 1}`)

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
        data: seriesData,
        type: 'line',
        // 设置折线光滑
        smooth: props.smooth,
        symbol: props.showSymbol ? 'circle' : 'none',
        lineStyle: {
          width: 2,
          // color: '#1890ff'
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
