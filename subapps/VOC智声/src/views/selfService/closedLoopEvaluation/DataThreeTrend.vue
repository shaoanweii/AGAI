<script setup lang="ts">
import { computed } from 'vue'
import { emojiMap } from '@/constants'
import { fmtPer, fmtFix, fmtNum } from '@/utils'

/**
 * 数据趋势变化
 */
defineOptions({
  name: 'DataThreeTrend'
})

// Props 定义
interface Props {
  dataTrendChangeData?: any
  isShowTitle?: boolean
  isShowLegend?: boolean
  isBorderless?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  dataTrendChangeData: null,
  isShowTitle: true,
  isShowLegend: true,
  isBorderless: false
})

// ==================== 事件定义 ====================

const emit = defineEmits<{
  (e: 'chart-click', data: any): void
}>()

// ==================== 事件处理方法 ====================

/**
 * 处理图表点击事件
 */
const handleChartClick = (params: any) => {
  emit('chart-click', params)
}

// 计算属性：处理图表数据
const chartData = computed(() => {
  if (!props.dataTrendChangeData || props.dataTrendChangeData.length === 0) {
    // 如果没有数据，返回空数据
    return {
      xAxisData: [],
      ywxysData: [], // 业务相应数
      bhclsData: [], // 闭环处理数
      sjgbsData: [], // 事件关闭数
      totalMentions: []
    }
  }

  // 处理接口返回的数据
  const trendData = props.dataTrendChangeData
  const xAxisData = trendData.map((item: any) => item.name || '')

  const ywxysData = trendData.map((item: any) => ({
    ...item,
    name: item.name,
    value: item.ywxys
  }))

  const bhclsData = trendData.map((item: any) => ({
    ...item,
    name: item.name,
    value: item.bhcls
  }))

  const sjgbsData = trendData.map((item: any) => ({
    ...item,
    name: item.name,
    value: item.sjgbs
  }))

  // 注意刻度问题  需要解决 三个数字的叠加
  const totalMentions = trendData.map((item: any) => ({
    ...item,
    name: item.date,
    value: (item.ywxys || 0) + (item.bhcls || 0) + (item.sjgbs || 0)
  }))

  return {
    xAxisData,
    ywxysData,
    bhclsData,
    sjgbsData,
    totalMentions
  }
})

// 使用 shallowRef 管理图表配置项，避免深度响应式带来的性能问题
const chartOptions = computed<any>(() => {
  // 动态计算Y轴最大值
  const maxNegativeRateVal = Math.max(
    ...(chartData.value.totalMentions?.map((item: any) => Number(item?.value) || 0) || []),
    0
  )
  // 负面率优先显示到100，超过100再上调到10的倍数
  const negativeRateMax = maxNegativeRateVal <= 100 ? 100 : Math.ceil(maxNegativeRateVal / 10) * 10

  // 计算最大值：+10%头部空间，取百位整数
  const getSimpleMax = (val: number): number => {
    const safeVal = Number.isFinite(val) && val > 0 ? val : 1000
    const padded = safeVal * 1
    return Math.ceil(padded / 100) * 100
  }

  const maxMentionVal = Math.max(
    ...(chartData.value.totalMentions?.map((item: any) => Number(item?.value) || 0) || []),
    0
  )
  const mentionMax = getSimpleMax(maxMentionVal)
  return {
    grid: {
      left: 0,
      right: 20,
      top: 30,
      bottom: props.isShowLegend ? 30 : 0,
      containLabel: true
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      confine: true,
      formatter: function (params: any) {
        if (!params || params.length === 0) return ''

        // 获取第一个数据点的完整数据
        const data = params[0].data
        if (!data) return ''

        // 构建表格HTML
        let tableHtml = `
        <div style="background: white; border-radius: 4px; padding: 0; font-size: 12px; min-width: 200px;">
          <div class="mb-12 fs-14 fw-500" style="color: #333">
            ${data.name || ''}
          </div>
          <table style="width: 100%; border-collapse: collapse; margin: 0;">
            <thead>
              <tr style="background: #f0f8ff; padding: 8px 12px; color: #26292E; border-radius: 4px 4px 0 0;">
                <th style="padding: 8px 12px; text-align: left; color: #26292E;" class="fw-400 fs-14">名称</th>
                <th style="padding: 8px 12px; text-align: center; color: #26292E;" class="fw-400 fs-14">数值</th>
              </tr>
            </thead>
            <tbody>
      `

        // 定义要显示的数据行
        const dataRows = [
          {
            name: '预警事件总数',
            value: (data.ywxys || 0) + (data.bhcls || 0) + (data.sjgbs || 0)
          },
          {
            name: '业务相应数',
            value: data.ywxys
          },
          {
            name: '闭关处理数',
            value: data.bhcls
          },
          {
            name: '事件关闭数',
            value: data.sjgbs
          }
        ]

        // 添加数据行
        dataRows.forEach((row, index) => {
          const noBorder = true
          tableHtml += `
          <tr style="background: ${index % 2 === 0 ? 'white' : '#fafafa'};">
            <td style="padding: 8px 12px; color: #333; font-size: 14px; border-bottom: ${noBorder ? 'none' : '1px solid #e9ecef'};">
              ${row.name}
            </td>
            <td style="padding: 8px 12px; text-align: center; color: #333; font-size: 14px; border-bottom: ${noBorder ? 'none' : '1px solid #e9ecef'};">
              ${row.value}
            </td>
          </tr>
        `
        })

        tableHtml += `
            </tbody>
          </table>
        </div>
      `

        return tableHtml
      }
    },
    legend: {
      show: props.isShowLegend,
      bottom: 0,
      left: 'center',
      textStyle: {
        color: '#6E7B91'
      }
    },
    xAxis: [
      {
        type: 'category',
        data: chartData.value.xAxisData,
        axisTick: { show: false },
        axisLine: {
          lineStyle: {
            color: '#999'
          }
        },
        axisLabel: {
          color: '#999',
          fontSize: 14
        }
      }
    ],
    yAxis: [
      {
        type: 'value',
        name: '事件数',
        nameTextStyle: {
          padding: [0, 0, 0, -40]
        },
        min: 0,
        max: mentionMax,
        alignTicks: true,
        splitNumber: 5,
        axisLabel: {
          formatter: '{value}',
          color: '#999'
        }
      }
    ],
    series: [
      {
        name: '业务相应数',
        type: 'bar',
        yAxisIndex: 0,
        stack: 'Ad',
        barWidth: 24,
        barCategoryGap: '10%',
        barGap: 4,
        itemStyle: {
          color: '#82E3C7',
          // borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2
        },
        data: chartData.value.ywxysData
      },
      {
        name: '闭环处理数',
        type: 'bar',
        yAxisIndex: 0,
        stack: 'Ad',
        barWidth: 24,
        barGap: 4,
        barCategoryGap: '10%',
        itemStyle: {
          color: '#60B8EB',
          borderColor: '#fff',
          borderWidth: 2
          // borderRadius: 8
        },
        data: chartData.value.bhclsData
      },
      {
        name: '事件关闭数',
        type: 'bar',
        yAxisIndex: 0,
        stack: 'Ad',
        barWidth: 24,
        barGap: 4,
        barCategoryGap: '10%',
        itemStyle: {
          color: '#FF8A8B',
          // borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2
        },
        data: chartData.value.sjgbsData
      }
    ]
  }
})
</script>

<template>
  <div style="width: 100%; height: 100%">
    <FEcharts
      :options="chartOptions"
      :isEmpty="!chartData.xAxisData || chartData.xAxisData.length === 0"
      :width="'100%'"
      :height="'100%'"
      @chart-click="handleChartClick"
    />
  </div>
</template>

<style lang="scss" scoped>
.data-trend--borderless {
  box-shadow: none !important;
  border: none !important;
  background: transparent;
}
</style>
