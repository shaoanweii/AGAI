<script setup lang="ts">
import { computed } from 'vue'
import { emojiMap } from '@/constants'
import type { ProductTrendVo } from '@/api/productAnalysis/types'
import type { ProductSelfTrendVo } from '@/api/thisProductAnalysis/types.d'
import { fmtPer, fmtFix, fmtNum } from '@/utils'

/**
 * 数据趋势变化
 */
defineOptions({
  name: 'DataTrend'
})

// 通用趋势数据类型 - 兼容产品分析和本品分析
type TrendData = ProductTrendVo | ProductSelfTrendVo

// Props 定义
interface Props {
  dataTrendChangeData?: TrendData | null
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

import { ref } from 'vue'

/**
 * 处理图表点击事件
 * 实现柱子高亮，其他变半透明，再次点击恢复
 */
const activeBarIndex = ref<number | null>(null)
const handleChartClick = (params: any) => {
  // params.dataIndex 是点击的索引
  if (activeBarIndex.value === params.dataIndex) {
    activeBarIndex.value = null // 再次点击同一柱子，恢复
  } else {
    activeBarIndex.value = params.dataIndex
  }
  emit('chart-click', params)
}

// 计算属性：处理图表数据
const chartData = computed(() => {
  if (
    !props.dataTrendChangeData ||
    !props.dataTrendChangeData.trend ||
    props.dataTrendChangeData.trend.length === 0
  ) {
    // 如果没有数据，返回空数据
    return {
      xAxisData: [],
      positiveMentions: [],
      neutralMentions: [],
      negativeMentions: [],
      negativeRates: []
    }
  }

  // 处理接口返回的数据
  const trendData = props.dataTrendChangeData.trend
  const xAxisData = trendData.map((item: any) => item.tagName || '')

  // 为每个数据点创建包含完整信息的对象，用于tooltip显示
  const positiveMentions = trendData.map((item: any) => ({
    ...item,
    name: item.tagName,
    value: item.positiveMentions
  }))

  const neutralMentions = trendData.map((item: any) => ({
    ...item,
    name: item.tagName,
    value: item.neutralMentions
  }))

  const negativeMentions = trendData.map((item: any) => ({
    ...item,
    name: item.tagName,
    value: item.negativeMentions
  }))

  const totalMentions = trendData.map((item: any) => ({
    ...item,
    name: item.tagName,
    value: (item.negativeMentions || 0) + (item.neutralMentions || 0) + (item.positiveMentions || 0)
  }))

  const negativeRates = trendData.map((item: any) => ({
    ...item,
    name: item.tagName,
    value: item.negativeRate
  }))

  return {
    xAxisData,
    positiveMentions,
    neutralMentions,
    negativeMentions,
    totalMentions,
    negativeRates
  }
})

// 使用 shallowRef 管理图表配置项，避免深度响应式带来的性能问题
const chartOptions = computed<any>(() => {
  // 动态计算Y轴最大值
  const maxNegativeRateVal = Math.max(
    ...(chartData.value.negativeRates?.map((item: any) => Number(item?.value) || 0) || []),
    0
  )
  // 负面率优先显示到100，超过100再上调到10的倍数
  const negativeRateMax = maxNegativeRateVal <= 100 ? 100 : Math.ceil(maxNegativeRateVal / 10) * 10

  // 计算最大值：+10%头部空间，取百位整数
  const getSimpleMax = (val: number): number => {
    const safeVal = Number.isFinite(val) && val > 0 ? val : 1000
    const padded = safeVal * 1.1
    return Math.ceil(padded / 100) * 100
  }

  const maxMentionVal = Math.max(
    ...(chartData.value.totalMentions?.map((item: any) => Number(item?.value) || 0) || []),
    0
  )
  // const mentionMax = getNiceMax(maxMentionVal)
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
            ${data.tagName || ''}
          </div>
          <table style="width: 100%; border-collapse: collapse; margin: 0;">
            <thead>
              <tr style="background: #f0f8ff; padding: 8px 12px; color: #26292E; border-radius: 4px 4px 0 0;">
                <th style="padding: 8px 12px; text-align: left; color: #26292E;" class="fw-400 fs-14">名称</th>
                <th style="padding: 8px 12px; text-align: center; color: #26292E;" class="fw-400 fs-14">数值</th>
                <th style="padding: 8px 12px; text-align: center; color: #26292E;" class="fw-400 fs-14">环比</th>
                <th style="padding: 8px 12px; text-align: center; color: #26292E;" class="fw-400 fs-14">同比</th>
              </tr>
            </thead>
            <tbody>
      `

        // 定义要显示的数据行
        const dataRows = [
          {
            name: '负面率',
            value: fmtPer(data.negativeRate),
            mom: fmtFix(data.negativeRateMoM), // 	负面率环比
            yoy: fmtFix(data.negativeRateYoY) // 负面率同比
          },
          {
            name: '正面提及量',
            value: fmtNum(data.positiveMentions),
            mom: fmtFix(data.positiveMentionsMoM), // 正面提及量环比
            yoy: fmtFix(data.positiveMentionsYoY) // 正面提及量同比
          },
          {
            name: '中性提及量',
            value: fmtNum(data.neutralMentions),
            mom: fmtFix(data.neutralMentionsMoM),
            yoy: fmtFix(data.neutralMentionsYoY)
          },
          {
            name: '负面提及量',
            value: fmtNum(data.negativeMentions),
            mom: fmtFix(data.negativeMentionsMoM),
            yoy: fmtFix(data.negativeMentionsYoY)
          }
        ]

        // 添加数据行
        dataRows.forEach((row, index) => {
          // const isLast = index === dataRows.length - 1
          const noBorder = true
          tableHtml += `
          <tr style="background: ${index % 2 === 0 ? 'white' : '#fafafa'};">
            <td style="padding: 8px 12px; color: #333; font-size: 14px; border-bottom: ${noBorder ? 'none' : '1px solid #e9ecef'};">
              ${row.name}
            </td>
            <td style="padding: 8px 12px; text-align: center; color: #333; font-size: 14px; border-bottom: ${noBorder ? 'none' : '1px solid #e9ecef'};">
              ${row.value}
            </td>
            <td style="padding: 8px 12px; text-align: center; color: #333; font-size: 14px; border-bottom: ${noBorder ? 'none' : '1px solid #e9ecef'};">
              ${row.mom}
            </td>
            <td style="padding: 8px 12px; text-align: center; color: #333; font-size: 14px; border-bottom: ${noBorder ? 'none' : '1px solid #e9ecef'};">
              ${row.yoy}
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
      // itemWidth: 12,
      // itemHeight: 12,
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
        name: '负面率',
        nameTextStyle: {
          padding: [0, 0, 0, -40]
        },
        min: 0,
        max: negativeRateMax,
        alignTicks: true,
        splitNumber: 5,
        axisLabel: {
          formatter: '{value}%',
          color: '#999'
        }
      },
      {
        type: 'value',
        name: '提及量',
        position: 'right',
        min: 0,
        max: mentionMax,
        alignTicks: true,
        splitNumber: 5,
        axisLine: {
          lineStyle: {}
        },
        nameTextStyle: {
          padding: [0, 0, 0, 36]
        },
        axisLabel: {
          formatter: '{value}'
        }
      }
    ],
    // #FF8A8B,#60B8EB,#82E3C7
    series: [
      {
        name: '负面率',
        type: 'line',
        yAxisIndex: 0,
        smooth: true,
        lineStyle: {
          width: 2,
          type: 'dashed',
          color: '#FAB007'
        },
        data: chartData.value.negativeRates?.map((el: any) => {
          return {
            ...el,
            symbol: `image://${emojiMap[el.emotionType]}`,
            symbolSize: 32
          }
        })
      },
      {
        name: '正面提及量',
        type: 'bar',
        yAxisIndex: 1,
        stack: 'Ad',
        barWidth: 24,
        barCategoryGap: '10%',
        barGap: 4,
        itemStyle: {
          color: '#82E3C7',
          borderColor: '#fff',
          borderWidth: 2
        },
        emphasis: {
          itemStyle: {
            opacity: 1
          }
        },
        data: chartData.value.positiveMentions?.map((el: any, idx: number) => ({
          ...el,
          itemStyle:
            activeBarIndex.value === null
              ? {}
              : activeBarIndex.value === idx
                ? { opacity: 1 }
                : { opacity: 0.2 }
        }))
      },
      {
        name: '中性提及量',
        type: 'bar',
        yAxisIndex: 1,
        stack: 'Ad',
        barWidth: 24,
        barGap: 4,
        barCategoryGap: '10%',
        itemStyle: {
          color: '#60B8EB',
          borderColor: '#fff',
          borderWidth: 2
        },
        emphasis: {
          itemStyle: {
            opacity: 1
          }
        },
        data: chartData.value.neutralMentions?.map((el: any, idx: number) => ({
          ...el,
          itemStyle:
            activeBarIndex.value === null
              ? {}
              : activeBarIndex.value === idx
                ? { opacity: 1 }
                : { opacity: 0.2 }
        }))
      },
      {
        name: '负面提及量',
        type: 'bar',
        yAxisIndex: 1,
        stack: 'Ad',
        barWidth: 24,
        barGap: 4,
        barCategoryGap: '10%',
        itemStyle: {
          color: '#FF8A8B',
          borderColor: '#fff',
          borderWidth: 2
        },
        emphasis: {
          itemStyle: {
            opacity: 1
          }
        },
        data: chartData.value.negativeMentions?.map((el: any, idx: number) => ({
          ...el,
          itemStyle:
            activeBarIndex.value === null
              ? {}
              : activeBarIndex.value === idx
                ? { opacity: 1 }
                : { opacity: 0.5 }
        }))
      }
    ]
  }
})

// onMounted(() => {
//   console.log('props',props.dataTrendChangeData)
// })
</script>

<template>
  <FCard
    :title="isShowTitle ? '数据趋势变化' : ''"
    :is-show-more="false"
    titleSize="small"
    :height="'625px'"
    :class="{
      'f-card-border': props.isShowTitle && !props.isBorderless,
      'data-trend--borderless': props.isBorderless
    }"
  >
    <!-- <template #leftExtra>
        <el-select clearable placeholder="" class="w-150 ml-12">
          <el-option value="均值线" label="均值线"></el-option>
        </el-select>
      </template> -->
    <FEcharts
      :options="chartOptions"
      :isEmpty="!chartData.xAxisData || chartData.xAxisData.length === 0"
      :width="'100%'"
      :height="'100%'"
      @chart-click="handleChartClick"
    />
  </FCard>
</template>

<style lang="scss" scoped>
.data-trend--borderless {
  box-shadow: none !important;
  border: none !important;
  background: transparent;
}
</style>
