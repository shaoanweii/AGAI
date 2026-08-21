<script setup lang="ts">
import { computed } from 'vue'
import HCard from '@h5/components/UI/HCard/index.vue'
import HEcharts from '@h5/components/UI/HEcharts/index.vue'
import { formatAxisLabel, fmtNum } from '@/utils'
import type { TagSentimentAnalysisVo } from '@h5/api/rootCauseAnalysis/types'

defineOptions({
  name: 'IndicatorAnalysisCard'
})

const props = defineProps<{
  data: TagSentimentAnalysisVo[]
  activeCode?: string
  titleSuffix?: string
}>()

const emit = defineEmits<{
  itemClick: [item: TagSentimentAnalysisVo]
}>()

const DATA_ZOOM_VISIBLE_COUNT = 10
const DEFAULT_CHART_HEIGHT = 268
const MAX_CHART_HEIGHT = 420
const LEGEND_SPACE = 28
const DATA_ZOOM_SPACE = 48
const BASE_LABEL_MIN_SPACE = 40
const ROTATED_LABEL_MIN_SPACE = 90
const AXIS_LABEL_FONT_SIZE = 12
const AXIS_LABEL_LINE_HEIGHT = 16
const AXIS_LABEL_MARGIN = 14
const AXIS_LABEL_MAX_WIDTH = 200
const CHINESE_CHAR_WIDTH = 12
const DEFAULT_CHAR_WIDTH = 7

const hasData = computed(() => props.data.length > 0)

/**
 * 估算横坐标标签文本宽度，中文按全角处理，英文和数字按窄字符处理。
 * @param label 横坐标标签文本
 * @returns 标签未旋转时的近似宽度
 */
const getLabelTextWidth = (label: string) => {
  return Array.from(label).reduce((width, char) => {
    return width + (/[\u4e00-\u9fa5]/.test(char) ? CHINESE_CHAR_WIDTH : DEFAULT_CHAR_WIDTH)
  }, 0)
}

/**
 * 根据横坐标文本长度、数量和旋转角度计算布局，避免长标签被图例或容器裁切。
 * @param names 横坐标名称集合
 * @param showDataZoom 是否展示区域缩放
 * @returns 坐标轴旋转角、标签宽度、图表底部留白和容器高度
 */
const getAxisLayout = (names: string[], showDataZoom: boolean) => {
  const maxLabelWidth = Math.max(...names.map(name => getLabelTextWidth(name)), 0)
  const hasLongLabel = maxLabelWidth > AXIS_LABEL_MAX_WIDTH * 0.6
  const axisLabelRotate = hasLongLabel ? 80 : names.length < 6 ? 45 : 75
  const axisLabelWidth = hasLongLabel ? AXIS_LABEL_MAX_WIDTH : 120
  const rotateRadian = (axisLabelRotate * Math.PI) / 180
  const labelRenderWidth = Math.min(maxLabelWidth, axisLabelWidth)
  const rotatedLabelSpace =
    labelRenderWidth * Math.sin(rotateRadian) +
    AXIS_LABEL_LINE_HEIGHT * Math.cos(rotateRadian) +
    AXIS_LABEL_MARGIN
  const minLabelSpace = hasLongLabel ? ROTATED_LABEL_MIN_SPACE : BASE_LABEL_MIN_SPACE
  const labelSpace = Math.max(rotatedLabelSpace, minLabelSpace)
  const zoomSpace = showDataZoom ? DATA_ZOOM_SPACE : 0
  const gridBottom = Math.ceil(labelSpace + zoomSpace + LEGEND_SPACE)
  const chartHeight = Math.min(Math.max(DEFAULT_CHART_HEIGHT, 180 + gridBottom), MAX_CHART_HEIGHT)

  return {
    axisLabelRotate,
    axisLabelWidth,
    gridBottom,
    chartHeight: `${chartHeight}px`
  }
}

const chartHeight = computed(() => {
  const names = props.data.map(item => item.tagName || '')
  const showDataZoom = names.length > DATA_ZOOM_VISIBLE_COUNT

  return getAxisLayout(names, showDataZoom).chartHeight
})

const chartOptions = computed<any>(() => {
  const names = props.data.map(item => item.tagName || '')
  const dataCount = names.length
  const showDataZoom = dataCount > DATA_ZOOM_VISIBLE_COUNT
  const zoomEnd = showDataZoom ? Math.floor((DATA_ZOOM_VISIBLE_COUNT / dataCount) * 100) : 100
  const axisLayout = getAxisLayout(names, showDataZoom)
  const positiveData = props.data.map(item => ({
    value: item.positiveMention || 0,
    name: item.tagName || '',
    ...item,
    itemStyle: {
      opacity: props.activeCode && props.activeCode !== item.tagCode ? 0.35 : 1
    }
  }))
  const neutralData = props.data.map(item => ({
    value: item.neutralMention || 0,
    name: item.tagName || '',
    ...item,
    itemStyle: {
      opacity: props.activeCode && props.activeCode !== item.tagCode ? 0.35 : 1
    }
  }))
  const negativeData = props.data.map(item => ({
    value: item.negativeMention || 0,
    name: item.tagName || '',
    ...item,
    itemStyle: {
      opacity: props.activeCode && props.activeCode !== item.tagCode ? 0.35 : 1
    }
  }))

  return {
    color: ['#82e3c7', '#60b8eb', '#ff8a8b'],
    grid: {
      top: 24,
      left: 36,
      right: 12,
      bottom: axisLayout.gridBottom
    },
    dataZoom: showDataZoom
      ? [
          {
            type: 'slider',
            show: true,
            xAxisIndex: [0],
            start: 0,
            end: zoomEnd,
            bottom: 28,
            height: 18,
            zoomLock: true,
            borderColor: 'transparent',
            backgroundColor: '#F5F7FA',
            fillerColor: 'rgba(22, 119, 255, 0.15)',
            handleSize: 0,
            moveHandleSize: 0,
            showDetail: false,
            brushSelect: false,
            textStyle: {
              color: '#666'
            }
          },
          {
            type: 'inside',
            xAxisIndex: [0],
            start: 0,
            end: zoomEnd,
            zoomOnMouseWheel: false,
            moveOnMouseMove: true,
            moveOnMouseWheel: true,
            preventDefaultMouseMove: true
          }
        ]
      : [],
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow',
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
        const first = Array.isArray(params) && params.length > 0 ? params[0] : null
        const item = first?.data
        if (!item) return ''

        const rows = [
          {
            name: '正面',
            value: fmtNum(item.positiveMention || 0),
            color: '#82e3c7'
          },
          {
            name: '中性',
            value: fmtNum(item.neutralMention || 0),
            color: '#60b8eb'
          },
          {
            name: '负面',
            value: fmtNum(item.negativeMention || 0),
            color: '#ff8a8b'
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
          <div style="font-size:12px;color:#1F2733;min-height:20px;word-break:break-word;white-space:normal;">${item.tagName || ''}</div>
          <div style="display:flex;align-items:center;height:32px;margin-top:8px;background:#f5f7fa;border:1px solid #ebedf0;font-size:12px;color:#5F6A7A;font-weight:400">
            <div style="flex:1;text-align:center;">名称</div>
            <div style="flex:1;text-align:center;">数值</div>
          </div>
          <div>${tableRows}</div>
        </div>`
      }
    },
    legend: {
      data: ['正面', '中性', '负面'],
      icon: 'roundRect',
      itemWidth: 12,
      itemHeight: 12,
      bottom: 0,
      left: 'center',
      textStyle: {
        color: '#6e7b91',
        fontSize: 12
      }
    },
    xAxis: {
      type: 'category',
      data: names.length > 0 ? names : ['暂无数据'],
      axisLine: {
        lineStyle: {
          color: '#f1f1f5'
        }
      },
      axisTick: {
        show: false
      },
      axisLabel: {
        rotate: axisLayout.axisLabelRotate,
        interval: 0,
        width: axisLayout.axisLabelWidth,
        overflow: 'break',
        hideOverlap: false,
        color: '#5F6A7A',
        fontSize: AXIS_LABEL_FONT_SIZE,
        margin: AXIS_LABEL_MARGIN
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: (value: number) => formatAxisLabel(value),
        color: '#92929d',
        fontSize: 12
      },
      splitLine: {
        lineStyle: {
          color: '#f1f1f5'
        }
      }
    },
    series: [
      {
        name: '正面',
        type: 'bar',
        stack: 'total',
        barWidth: 16,
        data: positiveData.length > 0 ? positiveData : [0]
      },
      {
        name: '中性',
        type: 'bar',
        stack: 'total',
        barWidth: 16,
        data: neutralData.length > 0 ? neutralData : [0]
      },
      {
        name: '负面',
        type: 'bar',
        stack: 'total',
        barWidth: 16,
        data: negativeData.length > 0 ? negativeData : [0]
      }
    ]
  }
})

/**
 * 图表点击时兼容堆叠柱任意系列，取出原始指标项。
 * @param params ECharts 点击参数
 */
const handleChartClick = (params: any) => {
  if (!params?.data || typeof params.data !== 'object') return
  emit('itemClick', params.data as TagSentimentAnalysisVo)
}
</script>

<template>
  <HCard title="体验分析">
    <template v-if="titleSuffix" #left>
      <span class="indicator-title-suffix">【{{ titleSuffix }}】</span>
    </template>
    <HEcharts
      v-if="hasData"
      :key="props.data.length + (props.data[0]?.tagName?.length || 0)"
      :options="chartOptions"
      width="100%"
      :height="chartHeight"
      @chart-click="handleChartClick"
    />
    <van-empty v-else image-size="64" description="暂无数据" />
  </HCard>
</template>

<style scoped lang="scss">
.indicator-title-suffix {
  margin-left: 4px;
  color: #1677ff;
  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;
}
</style>
