<script setup lang="ts">
import { computed } from 'vue'
import type { EChartsOption } from 'echarts'
import dayjs from 'dayjs'
import HCard from '@h5/components/UI/HCard/index.vue'
import HEcharts from '@h5/components/UI/HEcharts/index.vue'
import { fmtNum } from '@/utils'
import type { EventTrendCardProps, EventTrendPoint } from './types'

defineOptions({
  name: 'SingleEventTrendCard'
})

// 组件入参
const props = withDefaults(defineProps<EventTrendCardProps>(), {
  data: () => [],
  loading: false
})

/**
 * 计算图表配置
 * - 纵轴根据当前数据峰值自动适配
 * - 横轴日期展示与首页 BrandTrendChange 保持一致（MM-DD）
 * - 点击图表时显示竖线和高亮圆点，方便在移动端查看单日数据
 */
const chartOptions = computed<EChartsOption>(() => {
  const list = (props.data || []) as EventTrendPoint[]

  const xAxisData = list.map(item => {
    const rawDate = item?.dateStr || ''
    if (!rawDate) return ''
    // 日期统一格式化为 MM-DD，保持与首页趋势组件一致
    return rawDate.length >= 10 ? dayjs(rawDate).format('MM-DD') : rawDate
  })

  const valueData = list.map(item => {
    const rawCount = item?.counts || 0
    return Number(rawCount) || 0
  })

  // 处理空数据场景，避免 ECharts 抛错
  const hasData = valueData.length > 0
  const safeValues = hasData ? valueData : [0]

  // 纵轴最大值：在峰值基础上预留 20% 头部空间，并向上取整到合适刻度
  const rawMax = Math.max(...safeValues)
  const paddedMax = rawMax === 0 ? 5 : rawMax * 1.2
  const magnitude = Math.pow(10, Math.floor(Math.log10(paddedMax)))
  const roundedMax = Math.ceil(paddedMax / magnitude) * magnitude
  const yMax = Math.max(5, roundedMax)

  // 默认拆分为 4 段，实际刻度数量与 BrandTrendChange 保持接近
  // 为避免纵坐标出现小数，强制使用整数间隔，并把最大值对齐到整数刻度
  const splitNumber = 4
  let interval = Math.ceil(yMax / splitNumber) || 1
  interval = Math.max(1, interval)
  const alignedYMax = interval * splitNumber

  // 横轴标签展示策略：在数据较多时旋转标签，避免严重重叠
  const labelCount = xAxisData.length
  const axisLabelRotate = labelCount > 28 ? 60 : labelCount > 5 ? 45 : 0
  const axisLabelFontSize = labelCount > 28 ? 10 : 12
  const gridBottom = axisLabelRotate >= 60 ? 80 : axisLabelRotate >= 45 ? 70 : 50

  return {
    grid: {
      top: 30,
      left: 40,
      right: 20,
      bottom: gridBottom
    },
    tooltip: {
      show: true,
      trigger: 'axis',
      // 移动端以点击为主交互方式，点击后展示竖线和高亮圆点
      triggerOn: 'click',
      axisPointer: {
        type: 'line',
        lineStyle: {
          color: '#EBEDF0',
          width: 2,
          type: 'solid'
        },
        label: {
          show: false
        }
      },
      confine: true,
      backgroundColor: 'rgba(0,0,0,0.85)',
      borderWidth: 0,
      padding: [8, 12],
      textStyle: {
        color: '#FFF',
        fontSize: 12
      },
      formatter: (params: any) => {
        try {
          const first = Array.isArray(params) && params.length > 0 ? params[0] : null
          if (!first) return ''
          const index = first.dataIndex ?? 0
          const item = list[index]
          if (!item) return ''

          const dateStr = item.dateStr ?? ''
          const rawCount = item.counts ?? 0
          return `${dateStr}<br/><div style='display: inline-block;width:8px;height:8px;border-radius:50%;background-color:#0AADFF;margin-right:8px'></div>事件数：${fmtNum(rawCount)}`
        } catch {
          // tooltip 渲染失败时不影响主流程
          return ''
        }
      }
    },
    xAxis: {
      type: 'category',
      data: hasData ? xAxisData : [''],
      axisLine: {
        lineStyle: {
          color: '#F1F1F5'
        }
      },
      axisTick: {
        show: false
      },
      axisLabel: {
        interval: 0,
        hideOverlap: true,
        showMinLabel: true,
        showMaxLabel: true,
        color: '#5F6A7A',
        rotate: axisLabelRotate,
        fontSize: axisLabelFontSize,
        margin: 16
      }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: alignedYMax,
      interval,
      minInterval: 1,
      axisLabel: {
        color: '#92929D',
        formatter: (value: number) => {
          // 保证纵坐标仅展示整数
          return String(Math.round(value))
        }
      },
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      splitLine: {
        lineStyle: {
          color: '#F1F1F5'
        }
      }
    },
    series: [
      {
        name: '事件数',
        type: 'line',
        data: safeValues,
        smooth: true,
        symbol: 'circle',
        symbolSize: 2,
        showSymbol: true,
        itemStyle: {
          color: '#0AADFF'
        },
        lineStyle: {
          color: '#0AADFF',
          width: 2
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
                color: '#E7F7FF'
              },
              {
                offset: 1,
                color: '#E7F7FF'
              }
            ]
          }
        },
        emphasis: {
          itemStyle: {
            color: '#0AADFF',
            borderColor: '#0AADFF',
            borderWidth: 4
          }
        }
      }
    ]
  }
})
</script>

<template>
  <HCard title="事件趋势">
    <div class="event-trend-card">
      <HEcharts :options="chartOptions" width="100%" height="240px" />
    </div>
  </HCard>
</template>

<style scoped lang="scss">
.event-trend-card {
  width: 100%;
}
</style>
