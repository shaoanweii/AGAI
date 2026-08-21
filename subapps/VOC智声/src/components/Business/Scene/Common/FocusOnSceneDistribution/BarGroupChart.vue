<script setup lang="ts">
import { formatChartPop } from '@/utils/chart'
import { computed, ref } from 'vue'
import { debounce } from 'lodash-es'

defineOptions({
  name: 'BarGroupChart'
})

// 定义emits
const emit = defineEmits<{
  'chart-click': [data: any]
}>()

// 定义标签数据项的类型
interface TagDataItem {
  tagName: string
  tagCode: string
  tagLevel: string
  value: number
  valueAvg: number
  valueMoM: string
  valueAvgMoM: string
  valueYoY: string
  valueAvgYoY: string
}

// 定义关注场景分析数据的类型
interface FocusSceneAnalysisData {
  tagData: TagDataItem[]
}

// 接收从父组件传递的关注场景分析数据
interface Props {
  focusSceneAnalysisData?: FocusSceneAnalysisData // 接收完整的接口返回数据
  brandName?: string
  avgName?: string
  dataType?: MentionNegativeRateType // 数据类型，用于判断Y轴显示格式
}

const {
  focusSceneAnalysisData = null,
  brandName = '当前品牌',
  avgName = '集团均值',
  dataType = 'negativeRate'
} = defineProps<Props>()

// 处理数据映射，将接口数据转换为 ECharts 需要的格式
const processedChartData = computed(() => {
  if (
    !focusSceneAnalysisData ||
    !focusSceneAnalysisData.tagData ||
    !focusSceneAnalysisData.tagData.length
  ) {
    return {
      categories: [],
      currentBrandData: [],
      groupAvgData: []
    }
  }

  const tagData: TagDataItem[] = focusSceneAnalysisData.tagData

  // 直接使用接口返回的数据顺序，不进行排序
  // 提取标签名称作为 x 轴分类
  const categories = tagData.map((item: TagDataItem) => item.tagName)

  // 提取当前品牌的数据（使用value字段）
  const currentBrandData = tagData.map((item: TagDataItem) => ({
    name: item.tagName,
    value: item.value,
    tagCode: item.tagCode,
    tagName: item.tagName,
    tagLevel: item.tagLevel,
    valueMoM: item.valueMoM,
    valueYoY: item.valueYoY
  }))

  // 提取集团均值数据（使用valueAvg字段）
  const groupAvgData = tagData.map((item: TagDataItem) => ({
    name: item.tagName,
    value: item.valueAvg,
    tagCode: item.tagCode,
    tagName: item.tagName,
    tagLevel: item.tagLevel,
    valueMoM: item.valueAvgMoM,
    valueYoY: item.valueAvgYoY
  }))

  return {
    categories,
    currentBrandData,
    groupAvgData
  }
})

// 图表点击防抖标志位
const isChartClicking = ref(false)

// 处理图表点击事件（内部实现）
const handleChartClickInternal = (params: any) => {
  // 防止重复点击
  if (isChartClicking.value) {
    return
  }

  isChartClicking.value = true
  emit('chart-click', params)

  // 延迟重置标志位，确保防抖生效
  setTimeout(() => {
    isChartClicking.value = false
  }, 300)
}

// 处理图表点击事件（防抖版本）
const handleChartClick = debounce(handleChartClickInternal, 300)

const chartOptions = computed((): any => {
  const { categories, currentBrandData, groupAvgData } = processedChartData.value

  // 使用从props传递的品牌名称
  // 计算是否需要启用滚动：当数据项超过10个时启用
  const dataCount = categories.length
  const showDataZoom = dataCount > 10
  // 计算显示的数据窗口大小（百分比），固定展示10条数据
  const zoomEnd = showDataZoom ? Math.floor((10 / dataCount) * 100) : 100

  return {
    tooltip: {
      show: true,
      trigger: 'axis',
      axisPointer: {
        type: 'line',
        lineStyle: {
          type: 'dashed',
          color: '#999',
          width: 1
        },
        label: {
          show: false
        }
      },
      formatter: (params: any) => {
        return formatChartPop(params, dataType)
      }
    },
    // 设置图表四周间距
    grid: {
      left: 0,
      right: 0,
      top: 20,
      bottom: showDataZoom ? 60 : 30, // 有滚动条时增加底部间距
      containLabel: true
    },
    legend: {
      data: [brandName, avgName],
      left: 'center',
      bottom: showDataZoom ? 30 : 0 // 有滚动条时调整图例位置
    },
    // 添加数据缩放组件（底部滚动条 + 图表内部滚动）
    dataZoom: showDataZoom
      ? [
          {
            type: 'slider', // 滑动条型数据区域缩放组件（底部可见滚动条）
            show: true,
            xAxisIndex: [0],
            start: 0, // 数据窗口范围的起始百分比
            end: zoomEnd, // 数据窗口范围的结束百分比
            bottom: 0, // 滚动条位置在底部
            height: 20, // 滚动条高度
            zoomLock: true, // 锁定窗口大小，只允许平移
            borderColor: 'transparent',
            backgroundColor: '#F5F7FA',
            fillerColor: 'rgba(22, 119, 255, 0.15)',
            handleSize: 0, // 隐藏手柄，禁止调整窗口大小
            textStyle: {
              color: '#666'
            },
            moveHandleSize: 0,
            showDetail: false,
            brushSelect: false // 禁用滚动条缩放
          },
          {
            type: 'inside', // 内置型数据区域缩放组件（支持图表内部滚动）
            xAxisIndex: [0],
            start: 0,
            end: zoomEnd,
            zoomOnMouseWheel: false, // 禁用鼠标滚轮缩放
            moveOnMouseMove: true, // 开启鼠标拖动平移
            moveOnMouseWheel: true, // 开启鼠标滚轮平移（横向滚动）
            preventDefaultMouseMove: true // 防止默认的鼠标移动行为
          }
        ]
      : [],
    xAxis: {
      type: 'category',
      data: categories,
      axisLabel: {
        interval: 0, // 强制显示所有标签
        formatter: (value: string) => {
          // 如果文字长度大于6个字，在中位数位置换行
          if (value.length > 6) {
            const midIndex = Math.floor(value.length / 2)
            return value.slice(0, midIndex) + '\n' + value.slice(midIndex)
          }
          return value
        }
      },
      splitLine: {
        show: true,
        showMinLine: true,
        showMaxLine: true,
        lineStyle: {
          color: '#DDE3EE',
          width: 1,
          type: 'solid'
        }
      }
    },
    yAxis: {
      type: 'value',
      max: dataType === 'negativeRate' ? 100 : undefined, // 负面率固定最大值100，提及量自适应
      splitLine: {
        show: true,
        showMinLine: true,
        showMaxLine: true,
        lineStyle: {
          color: '#DDE3EE',
          width: 1,
          type: 'dashed'
        }
      },
      axisLabel: {
        formatter: dataType === 'negativeRate' ? '{value}%' : '{value}' // 负面率显示百分比，提及量显示数值
      }
    },
    series: [
      {
        name: brandName,
        type: 'bar',
        data: currentBrandData,
        barWidth: 24,
        itemStyle: {
          color: '#1677FF'
        }
      },
      {
        name: avgName,
        type: 'bar',
        data: groupAvgData,
        barWidth: 24,
        itemStyle: {
          color: '#0AADFF'
        }
      }
    ]
  }
})
</script>

<template>
  <FEcharts
    :options="chartOptions"
    :width="'100%'"
    :height="'280px'"
    @chart-click="handleChartClick"
  />
</template>

<style scoped>
/* 组件样式 */
</style>
