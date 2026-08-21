<template>
  <div class="csr mt-16">
    <CommonTitle :title="title">
      <template #right>
        <SwitchButton
          v-if="showSwitcher"
          v-model="innerMode"
          :options="MENTION_NEGATIVE_RATE_SWITCH_OPTIONS"
        />
      </template>
    </CommonTitle>
    <FEcharts
      :options="chartOptions"
      :width="'100%'"
      :height="height"
      @chart-click="handleChartClick"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch, nextTick } from 'vue'
import {
  DEFAULT_MENTION_NEGATIVE_RATE_TYPE,
  MENTION_NEGATIVE_RATE_SWITCH_OPTIONS
} from '@/constants'
import CommonTitle from '../CommonTitle'
import type { CarSeriesRankingBarProps, RankItem } from './types'
import {
  fmtNum, // 完全等于 formatNumber
  fmtPer, // 等价 formatPercent  自动+'%'
  fmtFix // 等价 formatRatePrefix  自动+'%'
} from '@/utils'

// 定义组件名称
defineOptions({
  name: 'CarSeriesRankingBar'
})

// 定义 Props
const props = withDefaults(defineProps<CarSeriesRankingBarProps>(), {
  data: () => [],
  mode: DEFAULT_MENTION_NEGATIVE_RATE_TYPE,
  title: '',
  height: '220px',
  barWidth: 40,
  showSwitcher: true
})

// 定义事件
const emit = defineEmits<{
  (e: 'update:mode', value: MentionNegativeRateType): void
  (e: 'barClick', data: RankItem): void
  (e: 'modeChange', mode: MentionNegativeRateType): void
}>()

// 内部模式状态，支持 v-model:mode
const innerMode = ref<MentionNegativeRateType>(props.mode)

// 监听 props.mode 变化
watch(
  () => props.mode,
  newMode => {
    if (newMode && newMode !== innerMode.value) {
      innerMode.value = newMode
    }
  }
)

// 监听内部模式变化并发射事件
watch(innerMode, newMode => {
  emit('update:mode', newMode)
  emit('modeChange', newMode)
})

/**
 * 处理图表点击事件
 * @param params ECharts 点击事件参数
 */
const handleChartClick = (params: any): void => {
  const dataIndex = params.dataIndex
  if (dataIndex >= 0 && dataIndex < props.data.length) {
    const clickedData = props.data[dataIndex]
    emit('barClick', clickedData)
  }
}

/**
 * 公开的实例方法
 */
const switchMode = (mode: MentionNegativeRateType): void => {
  innerMode.value = mode
}

const getCurrentMode = (): MentionNegativeRateType => {
  return innerMode.value
}

const refreshChart = (): void => {
  // 强制触发 chartOptions 重新计算
  innerMode.value = innerMode.value === 'negativeRate' ? 'mention' : 'negativeRate'
  nextTick(() => {
    innerMode.value = innerMode.value === 'negativeRate' ? 'mention' : 'negativeRate'
  })
}

// 计算 ECharts 配置
const chartOptions = computed<any>(() => {
  const names = props.data.map(d => d.name || '')

  if (innerMode.value === 'negativeRate') {
    const filled = props.data.map(d => Number(d.percent || 0))
    const empty = filled.map(v => Math.max(0, 100 - v))

    return {
      grid: {
        left: 0,
        right: 0,
        top: 64,
        bottom: 30,
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: names,
        axisTick: { show: false },
        axisLine: { lineStyle: { color: '#999' } },
        axisLabel: {
          interval: 0,
          hideOverlap: true,
          color: '#333',
          fontSize: 14,
          fontWeight: 500,
          margin: 20,
          rotate: 45,
          formatter: (value: any) => value.replace(/(.{12})/g, '$1\n')
        }
      },
      yAxis: {
        type: 'value',
        show: false
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
          const item = props.data[p.dataIndex]
          return `${p.axisValue}<br/>负面率：${fmtPer(p.value)}<br/>环比：${fmtFix(item.mom)}<br/>同比：${fmtFix(item.yoy)}`
        }
      },
      series: [
        {
          name: '占比',
          type: 'bar',
          stack: 'ratio',
          data: filled,
          barMaxWidth: props.barWidth,
          itemStyle: {
            color: (p: any) => (props.data[p.dataIndex]?.highlight ? '#FF8A8B' : '#60B8EB')
          },
          label: {
            show: false
          }
        },
        {
          name: '余量',
          type: 'bar',
          stack: 'ratio',
          data: empty,
          barMaxWidth: props.barWidth,
          // itemStyle: { color: '#F2F4F7' },
          itemStyle: {
            color: 'transparent'
          },
          emphasis: { disabled: false },
          tooltip: { show: false },
          label: {
            show: true,
            position: 'top',
            distance: 20,
            formatter: (p: any) => `${filled[p.dataIndex]}%`,
            color: '#333',
            fontWeight: 500,
            fontSize: 16
          }
        }
      ]
    }
  } else {
    // value 模式：按值绘制，颜色同上，高亮项红色
    const values = props.data.map(d => d.value || 0)
    const max = Math.max(1, ...values)
    // 额外浅色堆叠一段，达到"顶部浅色块"视觉（用 max - value）
    const remainder = values.map(v => Math.max(0, max - v))

    return {
      grid: {
        left: 0,
        right: 0,
        top: 64,
        bottom: 30,
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: names,
        axisTick: { show: false },
        axisLine: { lineStyle: { color: '#999' } },
        axisLabel: {
          interval: 0,
          hideOverlap: true,
          color: '#333',
          fontSize: 14,
          fontWeight: 500,
          margin: 20,
          rotate: 45,
          formatter: (value: any) => value.replace(/(.{12})/g, '$1\n')
        }
      },
      yAxis: {
        type: 'value',
        show: false
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
          const p = params.find(p => p.seriesName === '提及量') || params[0]
          const item = props.data[p.dataIndex]
          return `${p.axisValue}<br/>提及量：${fmtNum(p.value)}<br/>环比：${fmtFix(item.mom)}<br/>同比：${fmtFix(item.yoy)}`
        }
      },
      series: [
        {
          name: '提及量',
          type: 'bar',
          stack: 'value',
          data: values,
          barWidth: props.barWidth,
          itemStyle: {
            color: (p: any) => (props.data[p.dataIndex]?.highlight ? '#FF6B6B' : '#60B8EB')
          },
          label: { show: false }
        },
        {
          name: '剩余',
          type: 'bar',
          stack: 'value',
          data: remainder,
          barWidth: props.barWidth,
          // itemStyle: { color: '#E9EFF8' },
          itemStyle: { color: 'transparent' },
          emphasis: { disabled: true },
          tooltip: { show: false },
          label: {
            show: true,
            position: 'top',
            distance: 20,
            formatter: (p: any) => `${fmtNum(values[p.dataIndex])}`,
            color: '#333',
            fontWeight: 500,
            fontSize: 16
          }
        }
      ]
    }
  }
})

// 暴露给父组件使用
defineExpose({
  switchMode,
  getCurrentMode,
  refreshChart
})
</script>

<style scoped lang="scss">
.csr {
  // 移除原有的 header 和 switch 样式，因为已经使用 CommonTitle 和 SwitchButton 组件
}
</style>
