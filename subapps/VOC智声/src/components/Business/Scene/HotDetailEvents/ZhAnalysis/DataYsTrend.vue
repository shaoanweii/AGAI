<script setup lang="ts">
import { computed } from 'vue'
import type { BrandTrendVo } from '@/api/groupAnalysis/types'
import {
  CHART_THEME_COLORS,
  DEFAULT_MENTION_NEGATIVE_RATE_TYPE,
  MENTION_NEGATIVE_RATE_SWITCH_OPTIONS
} from '@/constants'
import { hexToRgba } from '@/utils/chart'
import { fmtFix, fmtNum, fmtPer } from '@/utils'

defineOptions({
  name: 'DataYsTrend'
})

// 接收数据
interface Props {
  data?: any
}

const props = withDefaults(defineProps<Props>(), {
  data: null
})

// Events 定义
const emit = defineEmits<{
  chartClick: [data: any]
}>()

// 处理图表点击事件
const handleChartClick = (params: any) => {
  emit('chartClick', params.data)
}

// 图表配置
const chartOptions = computed<any>(() => {
  const list = props.data || []

  const legendData = ['内容总数', '主帖数', '评论数']
  //totals: 内容总数 posts:主贴 comments: 评论
  const xAxisData: any = []
  const totalsData: any = []
  const postsData: any = []
  const commentsData: any = []

  list?.forEach?.((item: any) => {
    xAxisData.push(item.date)
    totalsData.push(item.totals)
    postsData.push(item.posts)
    commentsData.push(item.comments)
  })

  const op = {
    title: {
      text: '条数',
      textStyle: {
        fontSize: 12
      }
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: legendData,
      // 位置底部
      bottom: 'bottom'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      axisTick: { show: false },
      axisLine: {
        lineStyle: {
          color: '#999'
        }
      },
      axisLabel: {
        color: '#999',
        fontSize: 14
      },
      data: xAxisData
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '内容总数',
        type: 'line',
        stack: 'Total',
        data: totalsData
      },
      {
        name: '主帖数',
        type: 'line',
        stack: 'Total',
        data: postsData
      },
      {
        name: '评论数',
        type: 'line',
        stack: 'Total',
        data: commentsData
      }
    ]
  }
  return op
})
</script>

<template>
  <!--

    -->
  <FCard :title="'数据趋势变化'" :is-show-more="false" titleSize="small" :height="'625px'">
    <!-- <template #leftExtra>
        <el-select clearable placeholder="" class="w-150 ml-12">
          <el-option value="均值线" label="均值线"></el-option>
        </el-select>
      </template> -->
    <FEcharts
      :options="chartOptions"
      :width="'100%'"
      :height="'100%'"
      :isEmpty="!props.data || props.data.length === 0"
      emptyDescription="暂无数据"
      @chart-click="handleChartClick"
    />
  </FCard>
</template>

<style scoped>
/* 组件样式 */
.dx-box {
  width: 100%;
  height: 100%;
}
</style>
