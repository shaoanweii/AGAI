<script setup lang="ts">
import { computed } from 'vue'
import HEcharts from '@h5/components/UI/HEcharts/index.vue'
import HListSingleSelect from '@h5/components/UI/HListSingleSelect'
import HSortNum from '@h5/components/UI/HSortNum/index.vue'
import type { EChartsOption } from 'echarts'
import 'echarts-wordcloud'
import { fmtNum } from '@/utils'
import type {
  BatchEventCarSeriesStatVo,
  BatchEventChannelStatVo,
  BatchEventDataStatVo,
  BatchEventOpinionStatVo,
  BatchEventProvinceStatVo,
  BatchEventSceneStatVo,
  BatchEventTrendStatVo
} from '@h5/api/batchEvent/types'

interface BatchEventMetricCard {
  label: string
  value: string
  tone: 'warning' | 'positive' | 'neutral'
}

interface StatisticOverviewPanelProps {
  metrics: BatchEventDataStatVo
  trends: BatchEventTrendStatVo
  carSeries: BatchEventCarSeriesStatVo
  focusScenes: BatchEventSceneStatVo
  opinions: BatchEventOpinionStatVo
  provinces: BatchEventProvinceStatVo
  channelRanks: BatchEventChannelStatVo
  selectedSeriesCode: string
  selectedSentiment: string
  sentimentOptions: Array<{
    label: string
    value: string
  }>
}

const props = defineProps<StatisticOverviewPanelProps>()
const emit = defineEmits<{
  (event: 'seriesClick', carSeriesCode: string): void
  (event: 'sentimentChange', sentiment: string): void
}>()

const sentimentColorMap = {
  positive: '#65ddbd',
  neutral: '#56b7eb',
  negative: '#ff8588'
}

const metricClassMap: Record<BatchEventMetricCard['tone'], string> = {
  warning: 'metric-card--warning',
  positive: 'metric-card--positive',
  neutral: 'metric-card--neutral'
}

const PROVINCE_TOP_COUNT = 10
const CAR_SERIES_TOP_COUNT = 10
const provinceColors = [
  '#1F6FFF',
  '#2EC9D3',
  '#FFB300',
  '#FF7436',
  '#7B9DD8',
  '#65D6A6',
  '#9B7BFF',
  '#FF8A8B',
  '#38BDF8',
  '#F97316'
]

/**
 * 将接口返回的字符串或数字转换为安全数值。
 * @param value 接口数值字段
 * @returns 可参与图表计算的数值
 */
const toStatNumber = (value?: string | number | null) => {
  const numericValue = Number(value)
  return Number.isFinite(numericValue) ? numericValue : 0
}

/**
 * 将后端小数占比转换为百分比展示。
 * @param value 后端小数占比
 * @returns 百分比文本
 */
const formatRatioValue = (value?: string | number) => {
  if (value === null || value === undefined || value === '') return '-'
  return `${(toStatNumber(value) * 100).toFixed(2)}%`
}

/**
 * 将渠道接口已返回的百分数值格式化为百分比展示。
 * @param value 后端百分数值
 * @returns 百分比文本
 */
const formatPercentValue = (value?: string | number) => {
  if (value === null || value === undefined || value === '') return '-'
  return `${toStatNumber(value).toFixed(2)}%`
}

const metricCards = computed<BatchEventMetricCard[]>(() => [
  {
    label: '负面率',
    value: formatRatioValue(props.metrics.negativeRatio),
    tone: 'warning'
  },
  {
    label: '正面率',
    value: formatRatioValue(props.metrics.positiveRatio),
    tone: 'positive'
  },
  {
    label: '提及量',
    value:
      props.metrics.mentionCount !== null && props.metrics.mentionCount !== undefined
        ? fmtNum(toStatNumber(props.metrics.mentionCount))
        : '-',
    tone: 'neutral'
  },
  {
    label: '用户数',
    value:
      props.metrics.userCount !== null && props.metrics.userCount !== undefined
        ? fmtNum(toStatNumber(props.metrics.userCount))
        : '-',
    tone: 'neutral'
  }
])

const trendItems = computed(() => {
  const itemMap = new Map<
    string,
    { date: string; positiveCount: number; neutralCount: number; negativeCount: number }
  >()

  const ensureItem = (date?: string) => {
    const key = date || ''
    if (!key) return null
    if (!itemMap.has(key)) {
      itemMap.set(key, {
        date: key,
        positiveCount: 0,
        neutralCount: 0,
        negativeCount: 0
      })
    }
    return itemMap.get(key)!
  }

  props.trends.positive?.forEach(item => {
    const target = ensureItem(item.date)
    if (target) target.positiveCount = toStatNumber(item.count)
  })
  props.trends.neutral?.forEach(item => {
    const target = ensureItem(item.date)
    if (target) target.neutralCount = toStatNumber(item.count)
  })
  props.trends.negative?.forEach(item => {
    const target = ensureItem(item.date)
    if (target) target.negativeCount = toStatNumber(item.count)
  })

  return Array.from(itemMap.values()).sort((prev, next) => prev.date.localeCompare(next.date))
})

const carSeriesItems = computed(() => {
  return [...props.carSeries]
    .sort(
      (prev, next) => toStatNumber(next.num ?? next.count) - toStatNumber(prev.num ?? prev.count)
    )
    .slice(0, CAR_SERIES_TOP_COUNT)
})

const sceneItems = computed(() => {
  return [...props.focusScenes].sort((prev, next) => {
    const prevTotal =
      (prev.positiveCount || 0) + (prev.neutralCount || 0) + (prev.negativeCount || 0)
    const nextTotal =
      (next.positiveCount || 0) + (next.neutralCount || 0) + (next.negativeCount || 0)
    return nextTotal - prevTotal
  })
})

const opinionItems = computed(() => {
  return [...props.opinions]
    .sort((prev, next) => toStatNumber(next.totalMentions) - toStatNumber(prev.totalMentions))
    .slice(0, 50)
})

const provinceItems = computed(() => {
  return [...props.provinces]
    .sort(
      (prev, next) => toStatNumber(next.num ?? next.count) - toStatNumber(prev.num ?? prev.count)
    )
    .slice(0, PROVINCE_TOP_COUNT)
})

const channelRankItems = computed(() => {
  return [...props.channelRanks]
    .sort(
      (prev, next) => toStatNumber(next.num ?? next.count) - toStatNumber(prev.num ?? prev.count)
    )
    .map((item, index) => ({
      rank: index + 1,
      channelName: item.channelName || '-',
      mentionCount: toStatNumber(item.num ?? item.count),
      percent: formatPercentValue(item.percentage)
    }))
})

const selectedSentimentValue = computed<string | number | null>({
  get: () => props.selectedSentiment || 'all',
  set: value => {
    emit('sentimentChange', String(value || 'all'))
  }
})

/**
 * 将接口情感值转换成稳定文本，兼容情感筛选字典值。
 * @param sentiment 接口情感值
 * @returns 可用于情感判断的文本
 */
const resolveSentimentText = (sentiment?: string) => {
  const sentimentText = String(sentiment || '')
  const matchedOption = props.sentimentOptions.find(item => {
    return item.value === sentimentText || item.label === sentimentText
  })

  return matchedOption?.label || sentimentText
}

/**
 * 将接口情感值转换为词云颜色标识。
 * @param sentiment 接口情感值
 * @returns 词云情感颜色 key
 */
const getSentimentKey = (sentiment?: string): keyof typeof sentimentColorMap => {
  const text = resolveSentimentText(sentiment).toLocaleLowerCase()
  if (text.includes('正') || text.includes('positive')) return 'positive'
  if (text.includes('负') || text.includes('negative')) return 'negative'
  return 'neutral'
}

const provinceSymbolBoundingData = computed(() => {
  return Math.max(...provinceItems.value.map(item => toStatNumber(item.num ?? item.count)), 1)
})

const provinceChartWidth = computed(() => {
  return provinceItems.value.length > 5 ? `${provinceItems.value.length * 68}px` : '100%'
})

const trendOption = computed<EChartsOption>(() => ({
  color: [sentimentColorMap.positive, sentimentColorMap.neutral, sentimentColorMap.negative],
  grid: {
    top: 28,
    right: 8,
    bottom: 42,
    left: 34
  },
  tooltip: {
    trigger: 'axis',
    confine: true
  },
  legend: {
    bottom: 0,
    icon: 'circle',
    itemWidth: 7,
    itemHeight: 7,
    textStyle: {
      color: '#7f8794',
      fontSize: 10
    },
    data: ['正面提及量', '中性提及量', '负面提及量']
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: trendItems.value.map(item => item.date),
    axisTick: {
      show: false
    },
    axisLine: {
      lineStyle: {
        color: '#edf0f5'
      }
    },
    axisLabel: {
      color: '#9aa3b1',
      fontSize: 10
    }
  },
  yAxis: {
    type: 'value',
    min: 0,
    splitNumber: 4,
    axisLabel: {
      color: '#9aa3b1',
      fontSize: 10
    },
    splitLine: {
      lineStyle: {
        color: '#edf0f5'
      }
    }
  },
  series: [
    {
      name: '正面提及量',
      type: 'line',
      smooth: true,
      symbol: 'none',
      lineStyle: {
        width: 2
      },
      data: trendItems.value.map(item => item.positiveCount)
    },
    {
      name: '中性提及量',
      type: 'line',
      smooth: true,
      symbol: 'none',
      lineStyle: {
        width: 2
      },
      data: trendItems.value.map(item => item.neutralCount)
    },
    {
      name: '负面提及量',
      type: 'line',
      smooth: true,
      symbol: 'none',
      lineStyle: {
        width: 2
      },
      data: trendItems.value.map(item => item.negativeCount)
    }
  ]
}))

const carSeriesOption = computed<EChartsOption>(() => ({
  color: ['#59b7eb'],
  grid: {
    top: 24,
    right: 4,
    bottom: 50,
    left: 34
  },
  tooltip: {
    trigger: 'axis',
    confine: true
  },
  xAxis: {
    type: 'category',
    data: carSeriesItems.value.map(item => item.carSeriesName || item.carSeriesCode || '-'),
    axisTick: {
      show: false
    },
    axisLine: {
      lineStyle: {
        color: '#edf0f5'
      }
    },
    axisLabel: {
      color: '#8d96a5',
      fontSize: 10,
      interval: 0,
      rotate: 35
    }
  },
  yAxis: {
    type: 'value',
    min: 0,
    splitNumber: 5,
    axisLabel: {
      color: '#9aa3b1',
      fontSize: 10
    },
    splitLine: {
      lineStyle: {
        color: '#edf0f5'
      }
    }
  },
  series: [
    {
      type: 'bar',
      barWidth: 16,
      itemStyle: {
        borderRadius: [2, 2, 0, 0]
      },
      label: {
        show: true,
        position: 'top',
        color: '#9aa3b1',
        fontSize: 10
      },
      data: carSeriesItems.value.map(item => ({
        value: toStatNumber(item.num ?? item.count),
        carSeriesCode: item.carSeriesCode || '',
        itemStyle: {
          color: props.selectedSeriesCode === item.carSeriesCode ? '#1677ff' : '#59b7eb'
        }
      }))
    }
  ]
}))

const focusSceneOption = computed<EChartsOption>(() => ({
  color: [sentimentColorMap.positive, sentimentColorMap.neutral, sentimentColorMap.negative],
  grid: {
    top: 24,
    right: 4,
    bottom: 54,
    left: 34
  },
  tooltip: {
    trigger: 'axis',
    confine: true
  },
  legend: {
    bottom: 0,
    icon: 'rect',
    itemWidth: 10,
    itemHeight: 10,
    textStyle: {
      color: '#7f8794',
      fontSize: 10
    },
    data: ['正面', '中性', '负面']
  },
  xAxis: {
    type: 'category',
    data: sceneItems.value.map(item => item.sceneName || item.sceneCode || '-'),
    axisTick: {
      show: false
    },
    axisLine: {
      lineStyle: {
        color: '#edf0f5'
      }
    },
    axisLabel: {
      color: '#8d96a5',
      fontSize: 10,
      interval: 0,
      rotate: 35
    }
  },
  yAxis: {
    type: 'value',
    min: 0,
    splitNumber: 5,
    axisLabel: {
      color: '#9aa3b1',
      fontSize: 10
    },
    splitLine: {
      lineStyle: {
        color: '#edf0f5'
      }
    }
  },
  series: [
    {
      name: '正面',
      type: 'bar',
      stack: 'scene',
      barWidth: 16,
      data: sceneItems.value.map(item => item.positiveCount || 0)
    },
    {
      name: '中性',
      type: 'bar',
      stack: 'scene',
      barWidth: 16,
      data: sceneItems.value.map(item => item.neutralCount || 0)
    },
    {
      name: '负面',
      type: 'bar',
      stack: 'scene',
      barWidth: 16,
      itemStyle: {
        borderRadius: [2, 2, 0, 0]
      },
      data: sceneItems.value.map(item => item.negativeCount || 0)
    }
  ]
}))

const wordCloudOption = computed<EChartsOption>(() => ({
  tooltip: {
    show: false
  },
  series: [
    {
      type: 'wordCloud',
      shape: 'circle',
      left: 'center',
      top: 'center',
      width: '100%',
      height: '100%',
      sizeRange: [11, 20],
      rotationRange: [0, 0],
      gridSize: 8,
      drawOutOfBound: false,
      textStyle: {
        fontWeight: 600,
        color: (params: any) => {
          const sentiment = params?.data?.sentiment as keyof typeof sentimentColorMap
          return sentimentColorMap[sentiment] || '#56b7eb'
        }
      },
      data: opinionItems.value.map(item => ({
        name: item.opinion || '-',
        value: toStatNumber(item.totalMentions),
        sentiment: getSentimentKey(item.sentiment)
      }))
    } as any
  ]
}))

const provinceOption = computed<EChartsOption>(() => {
  const option = {
    grid: {
      top: 14,
      right: 8,
      bottom: 44,
      left: 8
    },
    tooltip: {
      trigger: 'axis',
      confine: true,
      formatter(params: any) {
        const firstParam = Array.isArray(params) ? params[0] : params
        const item = provinceItems.value[firstParam?.dataIndex]
        if (!item) return ''

        const provinceName = item.provinceName || item.provinceCode || '-'
        const mentionCount = toStatNumber(item.num ?? item.count)
        return [
          provinceName,
          `提及量：${fmtNum(mentionCount)}`,
          `占比：${formatPercentValue(item.percentage)}`
        ].join('<br/>')
      }
    },
    xAxis: {
      type: 'category',
      data: provinceItems.value.map(item => item.provinceName || item.provinceCode || '-'),
      axisTick: {
        show: false
      },
      axisLine: {
        show: false
      },
      axisLabel: {
        color: '#7f8794',
        fontSize: 10,
        interval: 0
      }
    },
    yAxis: {
      type: 'value',
      show: false
    },
    series: [
      {
        type: 'pictorialBar',
        symbol: 'path://M0,10 L12,10 L6,0 Z',
        symbolRepeat: false,
        symbolClip: true,
        symbolBoundingData: provinceSymbolBoundingData.value,
        symbolSize: [42, 132],
        symbolPosition: 'start',
        label: {
          show: true,
          position: 'bottom',
          offset: [0, 24],
          formatter: (params: any) => {
            const item = provinceItems.value[params?.dataIndex]
            return `{value|${formatPercentValue(item?.percentage)}}`
          },
          rich: {
            value: {
              color: '#7f8794',
              fontSize: 10,
              lineHeight: 14
            }
          }
        },
        itemStyle: {
          color: (params: any) => provinceColors[params.dataIndex] || '#2c8cff'
        },
        data: provinceItems.value.map(item => toStatNumber(item.num ?? item.count))
      }
    ]
  }

  return option as EChartsOption
})

/**
 * 处理车系柱状图点击事件。
 * @param params ECharts 点击参数
 */
const handleCarSeriesClick = (params: any) => {
  const carSeriesCode = String(params?.data?.carSeriesCode || '')
  if (!carSeriesCode) return
  emit('seriesClick', carSeriesCode)
}
</script>

<template>
  <div class="statistic-overview-panel">
    <div class="metric-grid">
      <div
        v-for="metric in metricCards"
        :key="metric.label"
        class="metric-card"
        :class="metricClassMap[metric.tone]"
      >
        <div class="metric-label">{{ metric.label }}</div>
        <div class="metric-content">
          <span class="metric-value">{{ metric.value }}</span>
        </div>
      </div>
    </div>

    <div class="chart-section">
      <div class="chart-title">趋势分析</div>
      <HEcharts :options="trendOption" height="220px" />
    </div>

    <div class="chart-section">
      <div class="chart-title">车系分布</div>
      <HEcharts :options="carSeriesOption" height="240px" @chart-click="handleCarSeriesClick" />
    </div>

    <div class="chart-section">
      <div class="chart-title">聚焦场景</div>
      <HEcharts :options="focusSceneOption" height="250px" />
    </div>

    <div class="chart-section chart-section--opinion">
      <div class="chart-header">
        <div class="chart-title">评价观点</div>
        <HListSingleSelect
          v-model="selectedSentimentValue"
          class="sentiment-select"
          :options="props.sentimentOptions"
          :searchable="false"
          title="选择情感"
          placeholder="全部情感"
        />
      </div>
      <HEcharts :options="wordCloudOption" height="170px" />
    </div>

    <div class="chart-section">
      <div class="chart-title">省份分布</div>
      <div class="province-chart-scroll">
        <HEcharts :options="provinceOption" :width="provinceChartWidth" height="210px" />
      </div>
    </div>

    <div class="chart-section chart-section--rank">
      <div class="chart-title">渠道分布</div>
      <div class="rank-table">
        <div class="rank-row rank-row--head">
          <div>排名</div>
          <div>渠道名称</div>
          <div class="rank-cell--right">提及量</div>
          <div class="rank-cell--right">占比</div>
        </div>
        <div v-for="item in channelRankItems" :key="item.rank" class="rank-row">
          <div class="rank-icon-cell">
            <HSortNum :rank="item.rank" />
          </div>
          <div class="channel-name">{{ item.channelName }}</div>
          <div class="rank-count">{{ item.mentionCount.toLocaleString() }}</div>
          <div class="rank-percent">{{ item.percent }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.statistic-overview-panel {
  color: #1f2733;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.metric-card {
  min-height: 70px;
  padding: 12px;
  border-radius: 6px;
  background: #f5f7fa;
}

.metric-card--warning {
  background: #fffbe8;

  .metric-value {
    color: #f5a623;
  }
}

.metric-card--positive {
  background: #edfafa;
}

.metric-card--neutral {
  background: #f5f7fa;
}

.metric-label {
  font-size: 12px;
  line-height: 18px;
  color: #929aa6;
}

.metric-content {
  display: flex;
  align-items: center;
  column-gap: 8px;
  margin-top: 8px;
  min-width: 0;
}

.metric-value {
  font-weight: 700;
  font-size: 16px;
  line-height: 20px;
  color: #303846;
}

.chart-section {
  padding-top: 18px;
  margin-top: 18px;
  border-top: 1px dashed #ebeef2;
}

.chart-section:first-of-type {
  border-top: 0;
}

.chart-title {
  font-weight: 600;
  font-size: 14px;
  line-height: 20px;
  color: #1f2733;
}

.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.sentiment-select {
  flex: 0 0 78px;

  :deep(.hlss-trigger) {
    height: 26px;
    padding: 0 6px 0 8px;
    border-radius: 4px;
    box-sizing: border-box;
  }

  :deep(.hlss-trigger__text) {
    font-size: 11px;
    color: #606a78;
  }

  :deep(.hlss-trigger__icon) {
    color: #929aa6;
    font-size: 12px;
  }
}

.chart-section--opinion {
  :deep(.echarts-container) {
    margin-top: 4px;
  }
}

.chart-section--rank {
  padding-bottom: 4px;
}

.province-chart-scroll {
  width: 100%;
  overflow-x: auto;
  overflow-y: hidden;
}

.rank-table {
  margin-top: 14px;
}

.rank-row {
  display: grid;
  grid-template-columns: 34px minmax(0, 1fr) 64px 52px;
  align-items: center;
  min-height: 34px;
  column-gap: 6px;
  font-size: 12px;
  color: #4e5969;
}

.rank-row--head {
  min-height: 30px;
  color: #929aa6;
  border-bottom: 1px solid #ebeef2;
}

.rank-cell--right,
.rank-count,
.rank-percent {
  text-align: right;
}

.rank-icon-cell {
  display: flex;
  align-items: center;
  justify-content: center;
}

.channel-name {
  min-width: 0;
  font-weight: 500;
  color: #303846;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rank-row:nth-child(3) .channel-name {
  color: #1677ff;
}

.rank-count {
  font-weight: 700;
  color: #303846;
}

.rank-percent {
  color: #606a78;
}
</style>
