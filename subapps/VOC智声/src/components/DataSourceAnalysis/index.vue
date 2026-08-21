<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import BarAndPointChart from '@/components/Charts/BarAndPointChart/index.vue'
import BarOrLineChart from '@/components/Charts/BarOrLineChart/index.vue'
import WordCloudChart from './WordCloudChart.vue'
import type { DataSourceItem, DataSourceRemark, WordCloudItem } from './types.d'

// Props 定义
const props = defineProps<{
  chartId?: string
  loading?: boolean
  data: DataSourceItem[]
  remarkData?: DataSourceRemark
  attr?: string
  attrName?: string
  preDivId?: string
}>()

// Emits 定义
const emit = defineEmits<{
  (e: 'download'): void
  (e: 'changeDatasource', dataSource: string): void
  (e: 'datasourceSeeDetail', trendItem: any): void
  (e: 'wordCloudChartClick', word: WordCloudItem): void
}>()

// 当前选中数据源
const currentDataSource = ref('')

// 监听props.data变化，更新当前选中的数据源
watch(
  () => props.data,
  val => {
    if (val && val.length > 0 && val[0].dataSource) {
      currentDataSource.value = val[0].dataSource
      console.log('设置当前数据源为:', currentDataSource.value)
    }
  },
  { immediate: true }
)

const handleBarClick = (params: any) => {
  console.log('DataSourceAnalysis 柱子点击事件:', params)
  // BarAndPointChart组件传递的参数是 { date: params.name }
  const dataSourceName = params?.date || params?.name
  if (dataSourceName) {
    console.log(`切换数据源从【${currentDataSource.value}】到【${dataSourceName}】`)
    currentDataSource.value = dataSourceName
    emit('changeDatasource', dataSourceName)
  }
}

const handleTrendClick = (params: any) => {
  emit('datasourceSeeDetail', params)
}

const handleWordCloudClick = (word: WordCloudItem) => {
  emit('wordCloudChartClick', word)
}

const handleDownload = () => {
  emit('download')
}

// 计算高亮最大提及量数据源
const maxSource = computed(() => {
  if (!props.data?.length) return ''
  return props.data.reduce(
    (max, cur) => (cur.totalMentionValue > max.totalMentionValue ? cur : max),
    props.data[0]
  ).dataSource
})

// 转换数据源数据为BarAndPointChart所需格式
const transformedData = computed(() => {
  if (!props.data || props.data.length === 0) {
    console.log('DataSourceAnalysis: 数据为空，返回空数组')
    return []
  }

  const result = props.data.map(item => ({
    customerTagName: item.dataSource || '未知数据源', // BarAndPointChart期望的字段名
    positiveMentions: item.positiveMentionValue || 0, // 修正字段名
    neutralMentions: item.neutralMentionValue || 0, // 修正字段名
    negativeMentions: item.negativeMentionValue || 0, // 修正字段名
    totalMentions: item.totalMentionValue || 0, // 添加总提及量
    experienceValue: 0 // 数据源分析中暂时没有体验值数据
  }))
  console.log('DataSourceAnalysis transformedData:', result)
  return result
})

// 转换趋势数据为VocDataItem格式
const transformedTrendData = computed(() => {
  if (!props.remarkData?.trend) return []
  return props.remarkData.trend.map(item => ({
    date: item.keyWord,
    positiveMentions: item.positiveMentionValue,
    neutralMentions: item.neutralMentionValue,
    negativeMentions: item.negativeMentionValue,
    experienceValue: 0
  }))
})
</script>

<template>
  <div class="data-source-analysis" :id="props.preDivId">
    <div class="header">
      <span class="title">数据来源</span>
    </div>
    <div class="content">
      <div class="main-chart">
        <BarAndPointChart
          :data="transformedData"
          :loading="props.loading"
          :highlight="maxSource"
          @barClick="handleBarClick"
        />
      </div>
      <div class="side-panel">
        <div class="trend-chart">
          <div class="subtitle">【{{ currentDataSource }}】- 提及量趋势</div>
          <BarOrLineChart
            :data="transformedTrendData"
            :loading="props.loading"
            @barClick="handleTrendClick"
          />
        </div>
        <div class="word-cloud">
          <div class="subtitle">【{{ currentDataSource }}】- 词云图</div>
          <WordCloudChart
            :data="props.remarkData?.wordCloud || []"
            @wordClick="handleWordCloudClick"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.data-source-analysis {
  display: flex;
  flex-direction: column;
  width: 100%;
  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    .title {
      font-size: 18px;
      font-weight: bold;
      color: #409eff;
    }
  }
  .content {
    gap: 24px;
    .main-chart {
      width: 100%;
      background: #fff;
    }
    .side-panel {
      width: 100%;
      display: flex;
      gap: 16px;
      margin-top: 24px;
      .trend-chart,
      .word-cloud {
        flex: 1;
        background: #fff;
        border-radius: 8px;
        padding: 12px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
        .subtitle {
          font-size: 15px;
          font-weight: 500;
          color: #409eff;
          margin-bottom: 8px;
          text-align: center;
        }
      }
    }
  }
}
</style>
