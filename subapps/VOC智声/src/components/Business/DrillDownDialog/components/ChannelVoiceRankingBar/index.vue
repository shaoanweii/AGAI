<template>
  <div class="cvr">
    <FEcharts
      :options="chartOptions"
      :width="'100%'"
      :height="height"
      @chart-click="handleChartClick"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { ChannelVoiceRankingBarProps, ChannelVoiceRankingBarEvents } from './types'
import { fmtNum, fmtPer } from '@/utils'

defineOptions({ name: 'ChannelVoiceRankingBar' })

const props = withDefaults(defineProps<ChannelVoiceRankingBarProps>(), {
  data: () => [],
  mode: 'negativeRate',
  height: '220px',
  barHeight: 18
})

const emit = defineEmits<ChannelVoiceRankingBarEvents>()

// 内部模式，支持 v-model:mode
const innerMode = ref<MentionNegativeRateType>(props.mode)
watch(
  () => props.mode,
  m => {
    if (m && m !== innerMode.value) innerMode.value = m
  }
)
watch(innerMode, m => {
  emit('update:mode', m)
  emit('modeChange', m)
})

const handleChartClick = (params: any) => {
  const idx = params?.dataIndex
  if (typeof idx === 'number' && idx >= 0 && idx < props.data.length) {
    emit('barClick', props.data[idx])
  }
}

// 计算 ECharts 配置（横向条形图）
const chartOptions = computed<any>(() => {
  const names = props.data.map(d => d.name)

  if (innerMode.value === 'negativeRate') {
    const filled = props.data.map(d => Number(d.value || 0))

    return {
      grid: { left: 24, right: 120, top: 10, bottom: 10, containLabel: true },
      xAxis: { type: 'value', max: 100, show: false, splitLine: { show: false } },
      yAxis: {
        type: 'category',
        data: names,
        inverse: true,
        axisTick: { show: false },
        axisLine: { show: true, lineStyle: { color: '#C9CDD4' } },
        axisLabel: {
          color: '#333333',
          fontSize: 16,
          fontWeight: 500,
          margin: 12,
          interval: 0,
          lineHeight: 24
        }
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow',
          triggerEmphasis: false,
          shadowStyle: {
            color: 'rgba(0, 0, 0, 0)'
          },
          label: {
            show: false
          }
        },
        formatter: (params: any[]) => {
          const p = params.find(p => p.seriesName === '占比') || params[0]
          return `${p.name || ''}<br/>负面率：${fmtPer(p.value)}`
        }
      },
      series: [
        {
          name: '占比',
          type: 'bar',
          stack: 'ratio',
          data: filled,
          barWidth: props.barHeight,
          itemStyle: {
            borderRadius: [0, 4, 4, 0],
            color: (p: any) => (props.data[p.dataIndex]?.highlight ? '#FF6B6B' : '#4A9EFF'),
            emphasis: {
              disabled: false
            }
          },
          label: {
            show: true,
            position: 'right',
            distance: 6,
            formatter: (p: any) => `${filled[p.dataIndex] || 0}%`,
            color: '#333',
            fontWeight: 500,
            fontSize: 14
          }
        }
      ]
    }
  }

  // value 模式：数值排行，右侧显示值
  const values = props.data.map(d => d.value || 0)

  return {
    grid: { left: 24, right: 120, top: 10, bottom: 10, containLabel: true },
    xAxis: { type: 'value', show: false },
    yAxis: {
      type: 'category',
      data: names,
      inverse: true,
      axisTick: { show: false },
      axisLine: { show: true, lineStyle: { color: '#C9CDD4' } },
      axisLabel: {
        color: '#333333',
        fontSize: 16,
        fontWeight: 500,
        margin: 12,
        interval: 0,
        lineHeight: 24
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow',
        triggerEmphasis: false,
        shadowStyle: {
          color: 'rgba(0, 0, 0, 0)'
        },
        label: {
          show: false
        }
      },
      formatter: (params: any[]) => {
        const p = params.find(p => p.seriesName === '值') || params[0]
        return `${p.name}<br/>提及量：${fmtNum(p.value)}`
      }
    },
    series: [
      {
        name: '值',
        type: 'bar',
        stack: 'value',
        data: values,
        barWidth: props.barHeight,
        itemStyle: {
          borderRadius: [0, 4, 4, 0],
          color: (p: any) => (props.data[p.dataIndex]?.highlight ? '#FF6B6B' : '#4A9EFF'),
          emphasis: {
            disabled: false
          }
        },
        label: {
          show: true,
          position: 'right',
          distance: 6,
          formatter: (p: any) => `${fmtNum(values[p.dataIndex])}`,
          color: '#333',
          fontWeight: 500,
          fontSize: 14
        }
      }
    ]
  }
})

// 实例方法
const switchMode = (m: MentionNegativeRateType) => {
  innerMode.value = m
}
const getCurrentMode = () => innerMode.value
const refreshChart = () => {}

defineExpose({ switchMode, getCurrentMode, refreshChart })
</script>

<style scoped lang="scss">
.cvr {
}
</style>
