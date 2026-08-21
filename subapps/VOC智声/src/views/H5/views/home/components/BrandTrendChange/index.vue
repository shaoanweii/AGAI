<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import HEcharts from '@h5/components/UI/HEcharts/index.vue'
import HCard from '@h5/components/UI/HCard/index.vue'
import type { TrendItem, BrandTrendChangeProps, BrandTrendChangeEmits } from './types'
import { formatAxisLabel, fmtNum, fmtPer } from '@/utils'
import dayjs from 'dayjs'

const props = withDefaults(defineProps<BrandTrendChangeProps>(), {
  showTooltip: true
})

const emit = defineEmits<BrandTrendChangeEmits>()

const LEGEND_NAMES = ['负面率', '正面提及量', '中性提及量', '负面提及量'] as const

type LegendName = (typeof LEGEND_NAMES)[number]
type LegendSelectedMap = Record<LegendName, boolean>

interface AxisRange {
  min: number
  max: number
  interval: number
}

interface AxisRangeOptions {
  minLimit?: number
  maxLimit?: number
  splitNumber?: number
}

/**
 * 创建图例选中态，保证数据切换后恢复默认全选。
 * @returns 图例选中状态映射
 */
const createDefaultLegendSelected = (): LegendSelectedMap => ({
  负面率: true,
  正面提及量: true,
  中性提及量: true,
  负面提及量: true
})

const legendSelected = ref<LegendSelectedMap>(createDefaultLegendSelected())

/**
 * 将 ECharts 图例事件中的 selected 对象归一化为组件内部状态。
 * @param selected ECharts 返回的图例选中状态
 * @returns 完整的图例选中状态
 */
const normalizeLegendSelected = (selected?: Record<string, boolean>): LegendSelectedMap =>
  LEGEND_NAMES.reduce((result, name) => {
    result[name] = selected?.[name] ?? true
    return result
  }, {} as LegendSelectedMap)

/**
 * 转换后端数值，过滤 null、undefined 与非数字内容。
 * @param value 原始接口值
 * @returns 有效数值或 undefined
 */
const toValidNumber = (value: unknown): number | undefined => {
  if (value === null || value === undefined || value === '') return undefined

  const numberValue = Number(value)
  return Number.isFinite(numberValue) ? numberValue : undefined
}

/**
 * 根据原始步长获取更适合展示的坐标轴间隔。
 * @param roughInterval 原始间隔
 * @returns 取整后的友好间隔
 */
const getNiceInterval = (roughInterval: number): number => {
  if (!Number.isFinite(roughInterval) || roughInterval <= 0) return 1

  const exponent = Math.floor(Math.log10(roughInterval))
  const magnitude = Math.pow(10, exponent)
  const normalized = roughInterval / magnitude
  const niceNormalized =
    normalized <= 1 ? 1 : normalized <= 2 ? 2 : normalized <= 5 ? 5 : 10

  return Math.max(Math.ceil(niceNormalized * magnitude), 1)
}

/**
 * 计算自适应纵轴范围，处理空数据、单值数据和边界裁剪。
 * @param sourceData 参与坐标轴计算的数据
 * @param options 坐标轴边界与分段配置
 * @returns ECharts value 轴的 min、max、interval
 */
const calculateAxisRange = (
  sourceData: Array<number | undefined>,
  options: AxisRangeOptions = {}
): AxisRange => {
  const splitNumber = options.splitNumber ?? 5
  const values = sourceData.filter(
    (value): value is number => typeof value === 'number' && Number.isFinite(value)
  )

  if (values.length === 0) {
    const min = options.minLimit ?? 0
    const max = options.maxLimit ?? splitNumber
    const interval = getNiceInterval((max - min) / splitNumber)

    return {
      min,
      max,
      interval
    }
  }

  const dataMin = Math.min(...values)
  const dataMax = Math.max(...values)
  const rawRange = dataMax - dataMin
  const padding = rawRange > 0 ? rawRange * 0.1 : Math.max(Math.abs(dataMax) * 0.1, 1)

  let min = dataMin - padding
  let max = dataMax + padding

  if (typeof options.minLimit === 'number') {
    min = Math.max(min, options.minLimit)
  }
  if (typeof options.maxLimit === 'number') {
    max = Math.min(max, options.maxLimit)
  }

  if (max <= min) {
    if (typeof options.maxLimit === 'number' && max >= options.maxLimit) {
      min = Math.max(options.minLimit ?? Number.NEGATIVE_INFINITY, max - padding)
    } else {
      max = min + padding
    }
  }

  const interval = getNiceInterval((max - min) / splitNumber)
  const roundedMin = Math.max(
    typeof options.minLimit === 'number' ? options.minLimit : Number.NEGATIVE_INFINITY,
    Math.floor(min / interval) * interval
  )
  let roundedMax = Math.min(
    typeof options.maxLimit === 'number' ? options.maxLimit : Number.POSITIVE_INFINITY,
    Math.ceil(max / interval) * interval
  )

  if (roundedMax <= roundedMin) {
    roundedMax = roundedMin + interval * splitNumber
  }

  return {
    min: roundedMin,
    max: roundedMax,
    interval
  }
}

/**
 * 图例点击后同步当前可见系列，用于重新计算左右纵坐标。
 * @param params ECharts 图例选中变化事件参数
 */
const handleLegendSelectChanged = (params: { selected?: Record<string, boolean> }) => {
  legendSelected.value = normalizeLegendSelected(params.selected)
}

const chartEvents = {
  legendselectchanged: handleLegendSelectChanged
}

// tooltip 内“根因分析”点击事件委托处理
const delegateClickHandler = (e: Event) => {
  try {
    const target = e.target as Element | null
    if (!target) return
    const clickable = target.closest('[data-rca="brand-trend"][data-index]') as HTMLElement | null
    if (!clickable) return
    const indexAttr = clickable.dataset.index ?? clickable.getAttribute('data-index') ?? ''
    const index = Number.parseInt(indexAttr, 10)
    if (!Number.isFinite(index) || index < 0) return

    const list = props.items || []
    const item = list[index]
    if (!item) return

    emit('root-cause-click', { startDate: item.startTime, endDate: item.endTime })
  } catch (err) {
    console.warn('品牌趋势 tooltip 事件委托处理失败:', err)
  }
}

onMounted(() => {
  document.addEventListener('click', delegateClickHandler, true)
})

onUnmounted(() => {
  document.removeEventListener('click', delegateClickHandler, true)
})

watch(
  () => props.items,
  () => {
    legendSelected.value = createDefaultLegendSelected()
  }
)

// 组件内部的图表配置
const chartOptions = computed<any>(() => {
  // 从 props.items 提取日期并格式化为 MM-DD
  const xAxisData = props.items.map((item: TrendItem) => {
    return item.date.length >= 10 ? dayjs(item.date).format('MM-DD') : item.date
  })

  const negativeRateData = props.items.map((item: TrendItem) => toValidNumber(item.negativeRate))
  const positiveMentionsData = props.items.map(
    (item: TrendItem) => toValidNumber(item.positiveMentions) ?? 0
  )
  const neutralMentionsData = props.items.map(
    (item: TrendItem) => toValidNumber(item.neutralMentions) ?? 0
  )
  const negativeMentionsData = props.items.map(
    (item: TrendItem) => toValidNumber(item.negativeMentions) ?? 0
  )

  const mentionSeriesData = [
    ...positiveMentionsData,
    ...neutralMentionsData,
    ...negativeMentionsData
  ]

  const visibleNegativeRateData = legendSelected.value['负面率'] ? negativeRateData : []
  const visibleMentionData = [
    ...(legendSelected.value['正面提及量'] ? positiveMentionsData : []),
    ...(legendSelected.value['中性提及量'] ? neutralMentionsData : []),
    ...(legendSelected.value['负面提及量'] ? negativeMentionsData : [])
  ]

  const negativeRateAxis = calculateAxisRange(
    visibleNegativeRateData.filter(value => value !== undefined).length > 0
      ? visibleNegativeRateData
      : negativeRateData,
    {
      minLimit: 0,
      maxLimit: 100
    }
  )
  const mentionAxis = calculateAxisRange(
    visibleMentionData.length > 0 ? visibleMentionData : mentionSeriesData,
    {
      minLimit: 0
    }
  )

  // 根据 X 轴标签数量动态控制展示策略（仅旋转/字号/底部留白，不换行）
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
      show: props.showTooltip,
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
      enterable: true,
      confine: true,
      backgroundColor: 'rgba(255,255,255,.9)',
      position: 'top',
      borderColor: '#ebedf0',
      borderWidth: 1,
      extraCssText:
        'border-radius:8px;box-shadow:0 4px 12px rgba(0,0,0,0.08);padding:12px;pointer-events:none !important;' +
        'max-width:320px;' +
        'color:#5f6a7a;',
      formatter: (params: any) => {
        try {
          const idx = Array.isArray(params) && params.length > 0 ? params[0].dataIndex : -1
          if (idx < 0 || idx >= props.items.length) return ''

          const currentItem = props.items[idx]
          const rows = [
            {
              name: '负面率',
              value: `${fmtPer(currentItem.negativeRate)}`,
              color: '#FAB007'
            },
            {
              name: '正面提及量',
              value: `${fmtNum(currentItem.positiveMentions ?? 0)}`,
              color: '#82E3C7'
            },
            {
              name: '中性提及量',
              value: `${fmtNum(currentItem.neutralMentions ?? 0)}`,
              color: '#0AADFF'
            },
            {
              name: '负面提及量',
              value: `${fmtNum(currentItem.negativeMentions ?? 0)}`,
              color: '#FF8A8B'
            }
          ]

          const header = `<div style="display:flex;align-items:center;justify-content:space-between;height:20px;">
              <div style="font-size:12px;color:#1F2733;">${currentItem.date || ''}</div>
<!--              <div data-rca="brand-trend" data-index="${idx}" style="font-size:12px;color:#929AA6;cursor:pointer;display:flex;align-items:center;pointer-events:auto;">根因分析 <span style="display:inline-block;width:8px;height:8px;border-right:2px solid #929AA6;border-top:2px solid #929AA6;transform:rotate(45deg) translateY(1px);margin-left:4px;"></span></div>-->
            </div>`
          const tableHeader = `<div style="display:flex;align-items:center;height:32px;background:#f5f7fa;border:1px solid #ebedf0;font-size:12px;color:#5F6A7A;font-weight:400">
              <div style="flex:1;text-align:center;">名称</div>
              <div style="flex:1;text-align:center;">数值</div>
            </div>`
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
              ${header}
              <div style="margin-top:8px;">${tableHeader}<div>${tableRows}</div></div>
            </div>`
        } catch {
          return ''
        }
      }
    },
    legend: {
      data: ['负面率', '正面提及量', '中性提及量', '负面提及量'],
      selected: { ...legendSelected.value },
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
        min: negativeRateAxis.min,
        max: negativeRateAxis.max,
        interval: negativeRateAxis.interval,
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
        min: mentionAxis.min,
        max: mentionAxis.max,
        interval: mentionAxis.interval,
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
        symbol: 'circle',
        symbolSize: 2,
        smooth: true,
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
        name: '正面提及量',
        type: 'line',
        yAxisIndex: 1,
        data: positiveMentionsData.length > 0 ? positiveMentionsData : [0],
        symbol: 'circle',
        symbolSize: 2,
        smooth: true,
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
                color: '#82E3C7'
              },
              {
                offset: 1,
                color: 'rgba(130, 227, 199, 0.1)'
              }
            ]
          }
        },
        itemStyle: {
          color: '#82E3C7'
        },
        lineStyle: {
          color: '#82E3C7',
          width: 2
        },
        emphasis: {
          itemStyle: {
            color: '#82E3C7',
            borderColor: '#82E3C7',
            borderWidth: 4
          }
        }
      },
      {
        name: '中性提及量',
        type: 'line',
        yAxisIndex: 1,
        data: neutralMentionsData.length > 0 ? neutralMentionsData : [0],
        symbol: 'circle',
        symbolSize: 2,
        smooth: true,
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
      },
      {
        name: '负面提及量',
        type: 'line',
        yAxisIndex: 1,
        data: negativeMentionsData.length > 0 ? negativeMentionsData : [0],
        symbol: 'circle',
        symbolSize: 2,
        smooth: true,
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
                color: '#FF8A8B'
              },
              {
                offset: 1,
                color: 'rgba(255, 138, 139, 0.1)'
              }
            ]
          }
        },
        itemStyle: {
          color: '#FF8A8B'
        },
        lineStyle: {
          color: '#FF8A8B',
          width: 2
        },
        emphasis: {
          itemStyle: {
            color: '#FF8A8B',
            borderColor: '#FF8A8B',
            borderWidth: 4
          }
        }
      }
    ]
  }
})
</script>

<template>
  <HCard v-if="items.length > 0" title="品牌声量趋势" class="mt-12">
    <HEcharts :options="chartOptions" :events="chartEvents" width="100%" height="267px" />
  </HCard>
</template>

<style scoped lang="scss">
.brand-line-title {
  height: 20px;
  display: flex;
  align-items: center;
}

.brand-line-layout {
  border-radius: 8px 8px 8px 8px;
  border: 1px solid #ebedf0;

  .selected-data-info {
    .selected-date {
      font-weight: 500;
      font-size: 14px;
      color: #1677ff;
      text-align: center;
      padding: 8px 0;
      background: #f0f9ff;
      border-radius: 4px;
      border: 1px solid #e6f7ff;
    }
  }

  .data-section {
    .section-title {
      font-weight: 500;
      font-size: 14px;
      color: #333333;
      margin-bottom: 8px;
      padding: 0 12px;
    }
  }

  .empty-state {
    padding: 40px 0;
    text-align: center;
  }

  .brand-line-table-header {
    height: 32px;
    display: flex;
    align-items: center;
    background: #f5f7fa;
    border-radius: 0px 0px 0px 0px;
    border: 1px solid #ebedf0;
    font-weight: 400;
    font-size: 12px;
    color: #5f6a7a;
  }
  .brand-line-table-item {
    height: 32px;
    display: flex;
    align-items: center;
    font-weight: 400;
    font-size: 12px;
    color: #5f6a7a;
    .spot-class {
      display: inline-block;
      width: 8px;
      height: 8px;
      border-radius: 50%;
      margin-right: 6px;
    }
  }
  .mark-layout {
    height: 32px;
    display: flex;
    align-items: center;
    background: #eaf3ff;
    border-radius: 4px 4px 4px 4px;
    border: 1px solid #ebedf0;
    font-weight: 500;
    font-size: 12px;
    color: #5f6a7a;
  }
}

.fw-600 {
  font-weight: 600;
}
.text-center {
  text-align: center;
}
.root-cause-entry {
  cursor: pointer;
}
</style>
