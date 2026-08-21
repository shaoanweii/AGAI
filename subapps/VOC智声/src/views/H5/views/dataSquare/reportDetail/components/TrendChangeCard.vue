<script setup lang="ts">
import { computed } from 'vue'
import dayjs from 'dayjs'
import HCard from '@h5/components/UI/HCard/index.vue'
import HEcharts from '@h5/components/UI/HEcharts/index.vue'
import { formatAxisLabel, fmtNum, fmtPer } from '@/utils'
import type { ProductTrendPointVo } from '@h5/api/rootCauseAnalysis/types'

defineOptions({
  name: 'TrendChangeCard'
})

const props = defineProps<{
  data: ProductTrendPointVo[]
}>()

/**
 * 根据提及量数据计算右侧坐标轴范围，保持刻度数量稳定且避免图形贴顶。
 * @param values 提及量数据集合
 * @returns 右侧坐标轴最小值、最大值与刻度间隔
 */
const getMentionAxisRange = (values: number[]) => {
  const maxMention = Math.max(...values, 0)
  const minMention = Math.min(...values, 0)
  const mentionRange = maxMention - minMention
  const mentionMargin = Math.max(mentionRange * 0.1, 1)
  const mentionStep =
    mentionRange <= 100
      ? 1
      : mentionRange <= 1000
        ? 10
        : mentionRange <= 10000
          ? 100
          : mentionRange <= 100000
            ? 1000
            : mentionRange <= 1000000
              ? 10000
              : 100000
  const mentionMax = Math.max(
    Math.ceil((maxMention + mentionMargin) / mentionStep) * mentionStep,
    mentionStep
  )
  const mentionInterval = Math.max(Math.ceil((mentionMax - minMention) / 5), 1)

  return {
    min: minMention,
    max: mentionMax,
    interval: mentionInterval
  }
}

/**
 * 将接口数值字段统一转为可计算数字，避免 undefined/null 影响坐标轴范围。
 * @param value 原始接口字段值
 * @returns 有效数字
 */
const toChartNumber = (value?: number | null) => {
  const num = Number(value)
  return Number.isFinite(num) ? num : 0
}

const chartOptions = computed<any>(() => {
  const list = Array.isArray(props.data) ? props.data : []
  const xAxisData = list.map(item => {
    const date = item.date || ''
    return date.length >= 10 ? dayjs(date).format('MM-DD') : date
  })
  const negativeRateData = list.map(item => toChartNumber(item.negativeRate))
  const totalMentionsData = list.map(item => toChartNumber(item.totalMentions))
  const minNegativeRate = Math.min(...negativeRateData, 0)
  const negativeRateMax = 100
  const negativeRateInterval = Math.max(Math.ceil((negativeRateMax - minNegativeRate) / 5), 1)
  const mentionAxisRange = getMentionAxisRange(totalMentionsData)

  // 横轴标签数量较多时参考首页趋势图进行旋转和底部留白，避免日期互相遮挡。
  const labelCount = xAxisData.length
  const axisLabelRotate = labelCount > 28 ? 60 : labelCount > 5 ? 45 : 0
  const axisLabelFontSize = labelCount > 28 ? 10 : 12
  const gridBottom = axisLabelRotate >= 60 ? 80 : axisLabelRotate >= 45 ? 70 : 50

  return {
    grid: {
      top: 30,
      left: 40,
      right: 40,
      bottom: gridBottom
    },
    tooltip: {
      trigger: 'axis',
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
      showContent: true,
      confine: true,
      backgroundColor: 'rgba(255,255,255,.9)',
      position: 'top',
      borderColor: '#ebedf0',
      borderWidth: 1,
      extraCssText:
        'border-radius:8px;box-shadow:0 4px 12px rgba(0,0,0,0.08);padding:12px;' +
        'max-width:320px;' +
        'color:#5f6a7a;',
      formatter: (params: any[]) => {
        const dataIndex = Array.isArray(params) && params.length > 0 ? params[0].dataIndex : -1
        const item = list[dataIndex]
        if (!item) return ''

        const rows = [
          {
            name: '负面率',
            value: fmtPer(toChartNumber(item.negativeRate)),
            color: '#FAB007'
          },
          {
            name: '提及量',
            value: fmtNum(toChartNumber(item.totalMentions)),
            color: '#0AADFF'
          }
        ]
        const tableRows = rows
          .map(
            row => `<div style="display:flex;align-items:center;height:32px;font-size:12px;color:#6E7B91;font-weight:400">
              <div style="flex:1;text-align:center;">
                <span style="display:inline-block;width:8px;height:8px;border-radius:50%;margin-right:6px;background:${row.color}"></span>${row.name}
              </div>
              <div style="flex:1;text-align:center;font-weight:600;">${row.value}</div>
            </div>`
          )
          .join('')

        return `<div style="min-width:180px;pointer-events:none;">
          <div style="font-size:12px;color:#1F2733;height:20px;">${item.date || ''}</div>
          <div style="display:flex;align-items:center;height:32px;margin-top:8px;background:#f5f7fa;border:1px solid #ebedf0;font-size:12px;color:#5F6A7A;font-weight:400">
            <div style="flex:1;text-align:center;">名称</div>
            <div style="flex:1;text-align:center;">数值</div>
          </div>
          <div>${tableRows}</div>
        </div>`
      }
    },
    legend: {
      show: false,
      data: ['负面率', '提及量'],
      icon: 'circle',
      itemWidth: 8,
      itemHeight: 8,
      bottom: -5,
      left: 'center',
      textStyle: {
        color: '#6E7B91'
      }
    },
    xAxis: {
      type: 'category',
      data: xAxisData.length > 0 ? xAxisData : ['暂无数据'],
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
    yAxis: [
      {
        type: 'value',
        name: '',
        min: minNegativeRate,
        max: negativeRateMax,
        interval: negativeRateInterval,
        axisLabel: {
          formatter: '{value}%',
          color: '#92929D'
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
      {
        type: 'value',
        name: '',
        min: mentionAxisRange.min,
        max: mentionAxisRange.max,
        interval: mentionAxisRange.interval,
        axisLabel: {
          formatter: (value: number) => formatAxisLabel(value),
          color: '#92929D'
        },
        axisLine: {
          show: false
        },
        axisTick: {
          show: false
        },
        splitLine: {
          show: false
        }
      }
    ],
    series: [
      {
        name: '负面率',
        type: 'line',
        yAxisIndex: 0,
        data: negativeRateData.length > 0 ? negativeRateData : [0],
        smooth: true,
        symbol: 'circle',
        symbolSize: 2,
        itemStyle: {
          color: '#FAB007'
        },
        lineStyle: {
          color: '#FAB007',
          width: 2,
          type: 'dashed'
        },
        emphasis: {
          itemStyle: {
            color: '#FAB007',
            borderColor: '#FAB007',
            borderWidth: 4
          }
        }
      },
      {
        name: '提及量',
        type: 'line',
        yAxisIndex: 1,
        data: totalMentionsData.length > 0 ? totalMentionsData : [0],
        smooth: true,
        symbol: 'circle',
        symbolSize: 2,
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
        itemStyle: {
          color: '#0AADFF'
        },
        lineStyle: {
          color: '#0AADFF',
          width: 2
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
  <HCard title="趋势变化">
    <div class="trend-change-card">
      <HEcharts :options="chartOptions" width="100%" height="247px" />
      <div class="trend-change-card__legend">
        <div class="trend-change-card__legend-item">
          <span class="trend-change-card__legend-dashed"></span>
          <span>负面率</span>
        </div>
        <div class="trend-change-card__legend-item">
          <span class="trend-change-card__legend-dot"></span>
          <span>提及量</span>
        </div>
      </div>
    </div>
  </HCard>
</template>

<style scoped lang="scss">
.trend-change-card {
  &__legend {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 20px;
    height: 20px;
    margin-top: -2px;
    color: #6e7b91;
    font-size: 12px;
    line-height: 20px;
  }

  &__legend-item {
    display: inline-flex;
    align-items: center;
    gap: 6px;
  }

  &__legend-dashed {
    width: 18px;
    height: 2px;
    overflow: hidden;
    background:
      radial-gradient(circle at 0 50%, #fab007 0 3px, transparent 3.1px),
      radial-gradient(circle at 50% 50%, #fab007 0 3px, transparent 3.1px),
      radial-gradient(circle at 100% 50%, #fab007 0 3px, transparent 3.1px);
    background-repeat: no-repeat;
    background-size:
      100% 100%,
      100% 100%,
      100% 100%;
    transform: translateY(1px);
  }

  &__legend-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #0aadff;
  }
}
</style>
