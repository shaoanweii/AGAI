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
  name: 'QdYsTrend'
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
  const item = props.data.find((d: any) => d.channelName === params.name)
  emit('chartClick', { params, item })
}

const formatChartBarPopQd = (params: any) => {
  // 若 tooltip.trigger= 'axis'  则params为数组，且length为 指标(或系列serie)的数量； 否则params为对象
  if (!params || params.length === 0) return ''

  // 对象另外写方法
  if (!Array.isArray(params)) {
    console.log('请设置tooltip.trigger为axis或单独写方法')
    return
  }

  // 标题取第0项即可，各项一致，如2025-07
  const title = params[0].axisValue

  let trStr = ''

  params.forEach((param: any) => {
    const name = param.name
    const dinf = props.data.find((d: any) => d.channelName === name)

    trStr += `
        <tr>
          <td class="chartTd" >内容条数</td>
          <td class="chartTd">${fmtNum(param.value)}</td>
          <td class="chartTd c666">${fmtFix(dinf?.share)}</td>
          <td class="chartTd c666">${fmtFix(dinf?.mentionsMoM)}</td>
          <td class="chartTd c666">${fmtFix(dinf?.mentionsYoY)}</td>
        </tr>
      `
  })

  return `
        <div class="chartPop">
            <!-- 标题 -->
            <div class="mb-12 fs-14 fw-500 c333" >${title}</div>
            <!-- 表格 -->
            <table style="width: 100%; border-collapse: collapse; margin: 0;">
              <thead>
                <tr class="chartTr">
                  <th class="chartTh">名称</th>
                  <th class="chartTh">数值</th>
                  <th class="chartTh">占比</th>
                  <th class="chartTh">环比</th>
                  <th class="chartTh">同比</th>
                </tr>
              </thead>
              <tbody>
                ${trStr}
              </tbody>
            </table>
        </div>
      `
}

// 图表配置
const chartOptions = computed<any>(() => {
  const xAxisData = props.data.map((item: any) => item.channelName)
  const seriesData = props.data.map((item: any) => item.mentions)
  // 计算是否需要启用滚动：当数据项超过10个时启用
  const dataCount = xAxisData.length
  const showDataZoom = dataCount > 10
  // 计算显示的数据窗口大小（百分比），固定展示10条数据
  const zoomEnd = showDataZoom ? Math.floor((10 / dataCount) * 100) : 100
  const op = {
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
        return formatChartBarPopQd(params)
      }
    },
    grid: {
      left: 0,
      right: 0,
      top: 20,
      bottom: 30, // 有滚动条时增加底部间距
      containLabel: true
    },
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
      data: xAxisData,
      axisLabel: {
        interval: 0, // 强制显示所有标签
        formatter: (value: any) => {
          // 如果文字长度大于6个字，在中位数位置换行
          if (value.length > 6) {
            const midIndex = Math.floor(value.length / 2)
            return value.slice(0, midIndex) + '\n' + value.slice(midIndex)
          }
          return value
        }
      }
    },
    yAxis: {
      type: 'value',
      splitLine: {
        show: true,
        showMinLine: true,
        showMaxLine: true,
        lineStyle: {
          color: '#DDE3EE',
          width: 1,
          type: 'dashed'
        }
      }
    },
    series: [
      {
        data: seriesData,
        type: 'bar',
        barWidth: 24,
        itemStyle: {
          color: '#1677FF'
        }
      }
    ]
  }

  return op
})
</script>

<template>
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
