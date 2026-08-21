<script setup lang="ts">
import {computed, onMounted, onUnmounted} from 'vue'
import HEcharts from '@h5/components/UI/HEcharts/index.vue'
import HCard from '@h5/components/UI/HCard/index.vue'
import type {TrendItem, BrandTrendChangeProps, BrandTrendChangeEmits} from './types'
import flagPng from '@/assets/h5/flag.png'
import {formatAxisLabel, fmtNum, fmtPer, formatK} from '@/utils'
import dayjs from 'dayjs'

const props = withDefaults(defineProps<BrandTrendChangeProps>(), {
  showTooltip: true,
  brandName: '智行'
})

const emit = defineEmits<BrandTrendChangeEmits>()

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

    emit('root-cause-click', {startDate: item.startTime, endDate: item.endTime})
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

// 组件内部的图表配置
const chartOptions = computed<any>(() => {
  // 从props.items提取数据
  const xAxisData = props.items.map((item: TrendItem) => {
    const rawDate = item.date || ''
    if (!rawDate) return ''
    // 使用dayjs格式化日期为MM-DD格式
    return rawDate.length >= 10 ? dayjs(rawDate).format('MM-DD') : rawDate
  })

  const negativeRateData = props.items.map((item: TrendItem) => item.negativeRate)
  const mentionData = props.items.map((item: TrendItem) => item.mention)

  // 动态计算Y轴范围，确保左右两侧分段一致
  const maxNegativeRate = Math.max(...negativeRateData, 0)
  const minNegativeRate = Math.min(...negativeRateData, 0)
  const maxMention = Math.max(...mentionData, 0)
  const minMention = Math.min(...mentionData, 0)

  // 计算数据范围并添加适当的边距（上下各增加10%的空间）
  const negativeRateRange = maxNegativeRate - minNegativeRate
  const mentionRange = maxMention - minMention

  const negativeRateMargin = Math.max(negativeRateRange * 0.1, 1)
  const mentionMargin = Math.max(mentionRange * 0.1, 1)

  // 根据数据范围动态确定取整单位
  const mentionStep = mentionRange <= 100 ? 1 :
    mentionRange <= 1000 ? 10 :
      mentionRange <= 10000 ? 100 :
        mentionRange <= 100000 ? 1000 :
          mentionRange <= 1000000 ? 10000 : 100000

  const negativeRateMin = minNegativeRate
  const negativeRateMax = 100
  const mentionMin = minMention
  const mentionMax = Math.ceil((maxMention + mentionMargin) / mentionStep) * mentionStep

  // 确保左右两侧有相同的分段数（5段），间隔值为整数
  const negativeRateInterval = Math.ceil((negativeRateMax - negativeRateMin) / 5)
  const mentionInterval = Math.ceil((mentionMax - mentionMin) / 5)

  // 根据X轴标签数量动态控制展示策略（仅旋转/字号/底部留白，不换行）
  // 目标：固定不换行，尽可能全部显示且不重叠
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
          const it = props.items[idx]
          const dateStr = it?.date || ''
          const rows = [
            {
              name: it.negativeRateName || '--',
              value: `${fmtPer(it.negativeRate)}`,
              mom: it.negativeRateMom,
              yoy: it.negativeRateYoy,
              rateColor: it.rateColor,
              color: '#FAB007'
            },
            {
              name: it.mentionName || '--',
              value: `${fmtNum(it.mention)}`,
              mom: it.mentionMom,
              yoy: it.mentionYoy,
              rateColor: '',
              color: '#0AADFF'
            }
          ]
          const header = `<div style="display:flex;align-items:center;justify-content:space-between;height:20px;">
              <div style="font-size:12px;color:#1F2733;">${dateStr}</div>
            </div>`
          const tableHeader = `<div style="display:flex;align-items:center;height:32px;background:#f5f7fa;border:1px solid #ebedf0;font-size:12px;color:#5F6A7A;font-weight: 400">
              <div style="flex:1;text-align:center;">名称</div>
              <div style="flex:1;text-align:center;">数值</div>
            </div>`
          const tableRows = rows
          .map(
            r => `<div style="display:flex;align-items:center;height:32px;font-size:12px;color:#6E7B91;font-weight: 400">
                <div style="flex:1;text-align:center;">
                  <span style="display:inline-block;width:8px;height:8px;border-radius:50%;margin-right:6px;background:${r.color}"></span>${r.name || ''}
                </div>
                <div style="flex:1;text-align:center;font-weight:600;">${r.value}</div>
              </div>`
          )
          .join('')
          const marks = (it.remark || [])
          .map(
            (m: string) =>
              `<div style="height:32px;display:flex;align-items:center;background:#EAF3FF;border:1px solid #EBEDF0;border-radius:4px;font-weight:500;font-size:12px;color:#5F6A7A;padding:0 10px;margin-top:8px;"><img style=\"width: 14px;height: 14px;margin-right: 8px\" src=\"${flagPng}\"/>${m || ''}</div>`
          )
          .join('')
          return `<div style="min-width:220px;pointer-events:none;">
              ${header}
              <div style="margin-top:8px;">${tableHeader}<div>${tableRows}</div></div>
              ${marks}
            </div>`
        } catch (e) {
          return ''
        }
      }
    },
    legend: {
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
        // interval设为0表示尝试全部展示；配合hideOverlap只隐藏真实重叠的标签
        interval: 0,
        hideOverlap: true,
        showMinLabel: true,
        showMaxLabel: true,
        color: '#5F6A7A',
        rotate: axisLabelRotate,
        fontSize: axisLabelFontSize,
        margin: 16,
      }
    },
    yAxis: [
      {
        type: 'value',
        name: '',
        min: negativeRateMin,
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
            color: '#F1F1F5',
          }
        }
      },
      {
        type: 'value',
        name: '',
        min: mentionMin,
        max: mentionMax,
        interval: mentionInterval,
        axisLabel: {
          formatter: function (value: number) {
            return formatK(value);
          },
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
          width: 2
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
        data: mentionData.length > 0 ? mentionData : [0],
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
      }
    ]
  }
})
</script>
<template>
  <HCard v-if="items.length > 0" title="品牌声量趋势">
    <template #left>
      <span class="brand-trend-title__brand">【{{ props.brandName }}】</span>
    </template>
    <HEcharts :options="chartOptions" width="100%" height="267px" />
  </HCard>

</template>
<style scoped lang="scss">
.brand-line-title {
  height: 20px;
  display: flex;
  align-items: center;
}

.brand-trend-title__brand {
  margin-left: 8px;
  font-weight: 500;
  font-size: 14px;
  color: #1677FF;
}

.brand-line-layout {
  border-radius: 8px 8px 8px 8px;
  border: 1px solid #EBEDF0;

  .selected-data-info {
    .selected-date {
      font-weight: 500;
      font-size: 14px;
      color: #1677FF;
      text-align: center;
      padding: 8px 0;
      background: #F0F9FF;
      border-radius: 4px;
      border: 1px solid #E6F7FF;
    }
  }

  .data-section {
    .section-title {
      font-weight: 500;
      font-size: 14px;
      color: #333;
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
    background: #F5F7FA;
    border-radius: 0px 0px 0px 0px;
    border: 1px solid #EBEDF0;
    font-weight: 400;
    font-size: 12px;
    color: #5F6A7A;
  }

  .brand-line-table-item {
    height: 32px;
    display: flex;
    align-items: center;
    font-weight: 400;
    font-size: 12px;
    color: #5F6A7A;

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
    background: #EAF3FF;
    border-radius: 4px 4px 4px 4px;
    border: 1px solid #EBEDF0;
    font-weight: 500;
    font-size: 12px;
    color: #5F6A7A;
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
