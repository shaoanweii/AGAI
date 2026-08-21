<script setup lang="ts">
import HCard from '@h5/components/UI/HCard/index.vue'
import HEcharts from '@h5/components/UI/HEcharts/index.vue'
import WordCloudChart from './WordCloudChart.vue'
import CarSeriesRank from './CarSeriesRank/index.vue'
import { ref, onMounted, computed, reactive } from 'vue'
import { useRoute } from 'vue-router'
import dayjs from 'dayjs'
import {
  getUserIntentionOpinionTop,
  getDataTrendChange,
  getTagAnalysis,
  getSeriesRank
} from '@h5/api/rootCauseAnalysis'
import type {
  IntentionOpinionTopVo,
  ProductTrendPointVo,
  TagSentimentAnalysisVo,
  SeriesRankItemVo
} from '@h5/api/rootCauseAnalysis/types'
import type { H5VocBaseRequest } from '@h5/api/home/types'
import { showToast } from 'vant'
import { formatAxisLabel } from '@/utils'

/**
 * 根因分析
 */
defineOptions({
  name: 'RootCauseAnalysis'
})

const route = useRoute()
const adFilter = ref<any>()

// 接口数据
const intentionOpinionData = ref<IntentionOpinionTopVo[]>([])
const dataTrendData = ref<ProductTrendPointVo[]>([])
const tagAnalysisData = ref<TagSentimentAnalysisVo[]>([])
const seriesRankData = ref<SeriesRankItemVo[]>([])

// 从路由query获取请求参数
const requestParams = computed<H5VocBaseRequest>(() => {
  return {
    ...route.query,
    filterItems: adFilter.value ? adFilter.value : undefined,
    ...ddParams
  } as H5VocBaseRequest
})

// 继续下钻的参数
const ddParams = reactive<any>({})

// 当前选中的意图
const activeIntention = ref('全部')

// 获取观点评价数据
const fetchIntentionOpinionData = async (intention?: string) => {
  try {
    const params = {
      ...requestParams.value,
      intention: intention === '全部' ? undefined : intention
    }
    const res = await getUserIntentionOpinionTop(params)
    if (res.success) {
      intentionOpinionData.value = res.result || []
      updateWordData()
    } else {
      console.error('获取观点评价数据失败:', res.message)
      showToast(res.message)
    }
  } catch (error) {
    console.error('获取观点评价数据异常:', error)
  }
}

// 处理意图切换
const handleIntentionChange = (intention: string) => {
  activeIntention.value = intention
  fetchIntentionOpinionData(intention)
}

// 获取数据趋势变化
const fetchDataTrendChange = async () => {
  try {
    const res = await getDataTrendChange(requestParams.value)
    if (res.success) {
      dataTrendData.value = res.result || []
      updateLineBarChart()
    } else {
      console.error('获取趋势数据失败:', res.message)
      showToast(res.message)
    }
  } catch (error) {
    console.error('获取趋势数据异常:', error)
  }
}

// 获取指标分析数据
const fetchTagAnalysis = async () => {
  try {
    const res = await getTagAnalysis(requestParams.value)
    if (res.success) {
      tagAnalysisData.value = res.result || []
      updateStackedBarChart()
    } else {
      console.error('获取指标分析数据失败:', res.message)
      showToast(res.message)
    }
  } catch (error) {
    console.error('获取指标分析数据异常:', error)
  }
}

// 获取车系排行数据
const fetchSeriesRank = async () => {
  try {
    const res = await getSeriesRank(requestParams.value)
    if (res.success) {
      seriesRankData.value = res.result || []
    } else {
      console.error('获取车系排行数据失败:', res.message)
      showToast(res.message)
    }
  } catch (error) {
    console.error('获取车系排行数据异常:', error)
  }
}

const resetAndRefresh = (filters?: any) => {
  adFilter.value = filters
  fetchIntentionOpinionData()
  fetchDataTrendChange()
  fetchTagAnalysis()
  fetchSeriesRank()
}

onMounted(() => {
  resetAndRefresh()
})

const lineBarChart = ref<any>({
  grid: {
    top: 30,
    left: 0,
    right: 0,
    bottom: 40,
    containLabel: true
  },
  tooltip: {
    show: false,
    trigger: 'axis',
    axisPointer: {
      type: 'cross'
    }
  },
  legend: {
    data: ['负面率', '提及量'],
    icon: 'circle',
    itemWidth: 8,
    itemHeight: 8,
    bottom: 5,
    left: 'center',
    textStyle: {
      color: '#6E7B91'
    }
  },
  xAxis: {
    type: 'category',
    data: [],
    axisLine: {
      lineStyle: {
        color: '#F1F1F5'
      }
    },
    axisTick: {
      show: false
    },
    axisLabel: {
      color: '#92929D'
    }
  },
  yAxis: [
    {
      type: 'value',
      name: '',
      min: 0,
      max: 100,
      splitLine: {
        lineStyle: {
          color: '#F1F1F5'
        }
      },
      axisLabel: {
        formatter: '{value}%',
        color: '#92929D'
      }
    },
    {
      type: 'value',
      name: '',
      min: 0,
      splitLine: {
        show: false
      },
      axisLabel: {
        formatter: function (value: number) {
          return formatAxisLabel(value)
        },
        color: '#92929D'
      }
    }
  ],
  series: [
    {
      name: '负面率',
      type: 'line',
      data: [],
      yAxisIndex: 0,
      symbol: 'none',
      smooth: 'none',
      itemStyle: {
        color: '#FAB007'
      },
      lineStyle: {
        color: '#FAB007'
      }
    },
    {
      name: '提及量',
      type: 'bar',
      data: [],
      yAxisIndex: 1,
      barMaxWidth: 16,
      itemStyle: {
        color: '#0AADFF',
        borderRadius: 2
      }
    }
  ]
})

// 更新趋势图表数据
const updateLineBarChart = () => {
  const dates = dataTrendData.value.map(item => {
    if(item.date){
      return item.date.length >= 10 ? item.date.substring(5) : item.date
    }
    return ''
  })
  const negativeRates = dataTrendData.value.map(item => {
    return {
      value: item.negativeRate || 0,
      name: item.date?.substring(5) || '',
      ...item
    }
  })
  const totalMentions = dataTrendData.value.map(item => {
    return {
      value: item.totalMentions || 0,
      name: item.date?.substring(5) || '',
      ...item
    }
  })

  lineBarChart.value.xAxis.data = dates
  lineBarChart.value.series[0].data = negativeRates
  lineBarChart.value.series[1].data = totalMentions

  // 动态调整Y轴最大值
  const maxMention = Math.max(...totalMentions.map((item: any) => item.value))
  const adjustedMax = maxMention * 1.2

  // 智能计算取整单位
  const magnitude = Math.pow(10, Math.floor(Math.log10(adjustedMax)))
  const roundedMax = Math.ceil(adjustedMax / magnitude) * magnitude

  lineBarChart.value.yAxis[1].max = roundedMax
}

const stackedBarChart = ref<any>({
  color: ['#82E3C7', '#60B8EB', '#FF8A8B'],
  grid: {
    top: 30,
    left: 0,
    right: 0,
    bottom: 60,
    containLabel: true
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    }
  },
  legend: {
    data: ['正面提及量', '中性提及量', '负面提及量'],
    icon: 'roundRect',
    itemWidth: 12,
    itemHeight: 12,
    bottom: 10,
    left: 'center',
    textStyle: {
      color: '#6E7B91'
    }
  },
  xAxis: {
    type: 'category',
    data: [],
    axisLine: {
      lineStyle: {
        color: '#F1F1F5'
      }
    },
    axisLabel: {
      rotate: 45,
      color: '#92929D'
    },
    axisTick: {
      show: false
    }
  },
  yAxis: {
    type: 'value',
    name: '',
    splitLine: {
      lineStyle: {
        color: '#F1F1F5'
      }
    },
    axisLabel: {
      formatter: function (value: number) {
        return formatAxisLabel(value)
      },
      color: '#92929D'
    }
  },
  series: [
    {
      name: '正面提及量',
      type: 'bar',
      stack: 'total',
      barWidth: 16,
      data: []
    },
    {
      name: '中性提及量',
      type: 'bar',
      stack: 'total',
      barWidth: 16,
      data: []
    },
    {
      name: '负面提及量',
      type: 'bar',
      stack: 'total',
      barWidth: 16,
      data: []
    }
  ]
})

// 更新堆叠柱状图数据
const updateStackedBarChart = () => {
  const tagNames = tagAnalysisData.value.map(item => item.tagName || '')
  const positiveMentions = tagAnalysisData.value.map(item => ({
    value: item.positiveMention || 0,
    name: item.tagName || '',
    ...item
  }))
  const neutralMentions = tagAnalysisData.value.map(item => ({
    value: item.neutralMention || 0,
    name: item.tagName || '',
    ...item
  }))
  const negativeMentions = tagAnalysisData.value.map(item => ({
    value: item.negativeMention || 0,
    name: item.tagName || '',
    ...item
  }))

  stackedBarChart.value.xAxis.data = tagNames
  stackedBarChart.value.series[0].data = positiveMentions
  stackedBarChart.value.series[1].data = neutralMentions
  stackedBarChart.value.series[2].data = negativeMentions
}

// 词云图数据
const wordData = ref<any[]>([])

// 更新词云图数据
const updateWordData = () => {
  wordData.value = intentionOpinionData.value.map(item => ({
    name: item.opinion,
    value: item.mentions,
    sentiment: item.sentiment,
    mentionsMoM: item.mentionsMoM,
    mentionsYoY: item.mentionsYoY,
    remark: item.remark
  }))
}

// 处理趋势图继续下钻
const lineBarChartClick = (params: any) => {
  const date = params.data.date
  if (!date) return

  // 判断是月份还是天
  const isMonth = date.length === 7 // YYYY-MM 格式
  const isDay = date.length === 10 // YYYY-MM-DD 格式

  if (isDay) {
    // 如果是天，检查是否与已存储的天相同
    if (ddParams.startDate === date && ddParams.endDate === date) {
      return // 相同则直接结束
    }
    // 设置当天的起始和结束时间
    ddParams.startDate = date
    ddParams.endDate = date
  } else if (isMonth) {
    // 如果是月份，计算该月的起始和结束时间
    const monthStart = dayjs(date).startOf('month').format('YYYY-MM-DD')
    const monthEnd = dayjs(date).endOf('month').format('YYYY-MM-DD')

    ddParams.startDate = monthStart
    ddParams.endDate = monthEnd
  }
  resetAndRefresh(adFilter.value)
}

// 处理车系点击事件，继续下钻
const handleCarSeriesClick = (data: SeriesRankItemVo) => {
  console.log('车系点击数据:', data)
  ddParams.carSeriesCode = data.code

  resetAndRefresh(adFilter.value)
}

// 处理指标分析继续下钻
const stackedBarChartClick = (params: any) => {
  console.log('params', params)

  if (params.data.tagLevel === '4') {
    return
  }

  if (params.data.tagLevel === '1') {
    ddParams.tag1Code = params.data.tagCode
    ddParams.tag2Code = undefined
    ddParams.tag3Code = undefined
  } else if (params.data.tagLevel === '2') {
    ddParams.tag2Code = params.data.tagCode
    ddParams.tag1Code = undefined
    ddParams.tag3Code = undefined
  } else if (params.data.tagLevel === '3') {
    ddParams.tag3Code = params.data.tagCode
    ddParams.tag1Code = undefined
    ddParams.tag2Code = undefined
  }

  resetAndRefresh(adFilter.value)
}

// 处理词云图点击事件
const handleWordCloudClick = (data: any) => {
  console.log('词云图点击数据:', data)
  if (ddParams.tooltip === data.name) {
    return
  }
  ddParams.topic = data.name

  resetAndRefresh(adFilter.value)
}

const isHideCarSeriesCard = computed(() => {
  if (ddParams.carSeriesCode) {
    const firstCard = (seriesRankData.value?.length && seriesRankData.value?.[0]) as any

    if (seriesRankData.value?.length === 1 && firstCard.code === ddParams.carSeriesCode) {
      return false
    }
    return true
  }
  return true
})

defineExpose({
  resetAndRefresh
})
</script>

<template>
  <div class="root-cause-analysis">
    <HCard title="趋势变化" height="294px">
      <HEcharts
        :options="lineBarChart"
        width="100%"
        height="100%"
        @chart-click="lineBarChartClick"
      ></HEcharts>
    </HCard>

    <HCard v-if="isHideCarSeriesCard" title="车系排行" class="mt-12">
      <CarSeriesRank :data="seriesRankData" @row-click="handleCarSeriesClick"></CarSeriesRank>
    </HCard>

    <HCard title="指标分析" height="322px" class="mt-12">
      <HEcharts
        :options="stackedBarChart"
        width="100%"
        height="100%"
        @chart-click="stackedBarChartClick"
      ></HEcharts>
    </HCard>

    <HCard title="观点评价" height="340px" class="mt-12">
      <div class="r-btn mt-10">
        <div
          v-for="item in ['全部', '抱怨', '咨询', '建议', '表扬']"
          :key="item"
          class="r-item"
          :class="{ 'r-active': activeIntention === item }"
          @click="handleIntentionChange(item)"
        >
          {{ item }}
        </div>
      </div>
      <div class="gdpj-content">
        <WordCloudChart :data="wordData" @word-click="handleWordCloudClick"></WordCloudChart>
      </div>
    </HCard>
  </div>
</template>

<style lang="scss" scoped>
.root-cause-analysis {
  .gdpj-content {
    width: 100%;
    height: 252px;
    background: #f5f7fa;
    box-shadow: 0px 1px 1px 0px rgba(10, 13, 18, 0.05);
    border-radius: 8px 8px 8px 8px;
    margin-top: 10px;
  }

  .r-btn {
    display: flex;
    .r-item {
      padding: 4px 15px;
      background: #f2f3f5;
      border-radius: 4px 4px 4px 4px;
      font-size: 12px;
      color: #1f2733;
      & + .r-item {
        margin-left: 8px;
      }
      &.r-active {
        border: 1px solid #1677ff;
        background: #e2f3fe;
        font-weight: 600;
        font-size: 12px;
        color: #0062ff;
      }
    }
  }
}
</style>
