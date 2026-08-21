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
  name: 'BrandTrend'
})

// 接收数据
interface Props {
  data?: BrandTrendVo[] | null
  dataType?: MentionNegativeRateType
}

const props = withDefaults(defineProps<Props>(), {
  data: null,
  dataType: DEFAULT_MENTION_NEGATIVE_RATE_TYPE
})

// Events 定义
const emit = defineEmits<{
  switch: [dataType: MentionNegativeRateType]
  chartClick: [data: any]
}>()

// 处理切换事件
const handleSwitch = (option: { value: string | number; label: string }) => {
  emit('switch', option.value as MentionNegativeRateType)
}

// 处理图表点击事件
const handleChartClick = (params: any) => {
  emit('chartClick', params.data)
}

// 图表配置
const chartOptions = computed<any>(() => {
  if (!props.data || props.data.length === 0) {
    return {}
  }

  const trendData = props.data
  const colors = CHART_THEME_COLORS

  // 获取日期数据（从每个时间点获取）
  const xAxisData = trendData.map(item => item.date || '')

  // 获取所有品牌名称（从第一个时间点的brandSeries中获取）
  const brandNames = trendData[0]?.brandSeries?.map(brand => brand.brandName) || []
  // 为每个品牌构建系列数据
  const seriesData = brandNames.map((brandName, brandIndex) => {
    const color = colors[brandIndex % colors.length]

    // 为当前品牌收集所有时间点的数据
    const brandData = trendData.map(timePoint => {
      const brandInfo = timePoint.brandSeries?.find(brand => brand.brandName === brandName)
      if (!brandInfo) {
        return {
          value: 0,
          brandName: brandName,
          date: timePoint.date || '',
          value1: 0,
          value2: 0
        }
      }
      return {
        value: props.dataType === 'negativeRate' ? brandInfo.value2 || 0 : brandInfo.value1 || 0,
        brandName: brandInfo.brandName || '',
        brandCode: brandInfo.brandCode || '',
        date: timePoint.date || '',
        value1: brandInfo.value1 || 0, // 提及量
        value2: brandInfo.value2 || 0 // 负面率
      }
    })

    return {
      name: brandName,
      type: 'line',
      data: brandData,
      symbol: 'circle',
      symbolSize: 8,
      itemStyle: {
        color: color,
        opacity: 0 // 隐藏符号但保持点击功能
      },
      // emphasis: {
      //   itemStyle: {
      //     opacity: 1 // 悬停时显示符号
      //   }
      // },
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
              color: hexToRgba(color, 0.1)
            },
            {
              offset: 1,
              color: hexToRgba(color, 0)
            }
          ]
        }
      },

      lineStyle: {
        color: color,
        width: 2,
        type: brandName === '集团均值' ? 'dashed' : 'solid'
      }
    }
  })

  // console.log('chartData@@222',props.data)
  // console.log('seriesData@@222',seriesData)

  return {
    tooltip: {
      show: true,
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#E4E7ED',
      borderWidth: 1,
      textStyle: {
        color: '#606266',
        fontSize: 12
      },
      formatter: (params: any) => {
        if (!params || params.length === 0) return ''

        // console.log('params@@22', params)

        const axisValue = params[0].axisValue // 时间轴值

        // 构建表格样式的 tooltip
        let tooltipContent = `
          <div style="padding: 12px; min-width: 200px;">
            <div style="margin-bottom: 12px; font-weight: 500; font-size: 14px; color: #333;">${axisValue}</div>
            <table style="width: 100%; border-collapse: collapse;">
              <thead>
                <tr style="background: #f0f8ff; padding: 8px 12px; color: #26292E; border-radius: 4px 4px 0 0;">
                  <th style="padding: 8px 12px; text-align: left; font-weight: 400; color: #26292E; font-size: 14px;">品牌</th>
                  <th style="padding: 8px 12px; text-align: right; font-weight: 400; color: #26292E; font-size: 14px;">${props.dataType === 'negativeRate' ? '负面率' : '提及量'}</th>
                  <th style="padding: 8px 12px; text-align: right; font-weight: 400; color: #26292E; font-size: 14px;">${props.dataType === 'negativeRate' ? '提及量' : '负面率'}</th>
                </tr>
              </thead>
              <tbody>
        `
        //params 数据先排序
        params.sort((a: any, b: any) => {
          // 根据 dataType 排序
          if (props.dataType === 'negativeRate') {
            return Number(b.data.value2 || 0) - Number(a.data.value2 || 0)
          } else {
            return Number(b.data.value1 || 0) - Number(a.data.value1 || 0)
          }
        })
        // 显示所有品牌的数据
        params.forEach((param: any) => {
          const data = param.data
          const seriesName = param.seriesName
          const color = param.color

          // 根据 dataType 决定显示顺序和颜色
          const primaryValue =
            props.dataType === 'negativeRate' ? `${fmtPer(data.value2)}` : fmtNum(data.value1)
          const secondaryValue =
            props.dataType === 'negativeRate' ? fmtNum(data.value1) : `${fmtPer(data.value2)}`
          // 同比
          const mom =
            props.dataType === 'negativeRate'
              ? `${fmtFix(data.negativeRateMoM, props.dataType)}`
              : `${fmtFix(data.mentionsMoM, props.dataType)}`
          // 环比
          const yoy =
            props.dataType === 'negativeRate'
              ? `${fmtFix(data.negativeRateYoY, props.dataType)}`
              : `${fmtFix(data.mentionsYoY, props.dataType)}`
          const primaryColor = '#333' // 深色
          const secondaryColor = '#999' // 灰色

          tooltipContent += `
            <tr style="border-bottom: 1px solid #F5F7FA;">
              <td style="padding: 8px 12px; text-align: left;">
                <span style="font-weight: 400; color: #333; font-size: 14px;">${seriesName}</span>
              </td>
              <td style="padding: 8px 12px; text-align: right; font-weight: 400; color: ${primaryColor}; font-size: 14px;">${primaryValue}</td>
              <td style="padding: 8px 12px; text-align: right; font-weight: 400; color: ${secondaryColor}; font-size: 14px;">${secondaryValue}</td>
            </tr>
          `
        })

        tooltipContent += `
              </tbody>
            </table>
          </div>
        `

        return tooltipContent
      },
      confine: true
    },
    legend: {
      data: brandNames,
      bottom: 10,
      left: 'center',
      textStyle: {
        color: '#606266',
        fontSize: 12
      }
    },
    grid: {
      top: 60,
      left: 60,
      right: 40,
      bottom: 80
    },
    xAxis: {
      type: 'category',
      data: xAxisData,
      axisLine: {
        lineStyle: {
          color: '#E4E7ED'
        }
      },
      axisTick: {
        show: false
      },
      axisLabel: {
        color: '#999999',
        fontSize: 12
      }
    },
    yAxis: {
      type: 'value',
      // name: props.dataType === 'negativeRate' ? '负面率(%)' : '提及量',
      name: '',
      nameTextStyle: {
        color: '#999999',
        fontSize: 14
      },
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      axisLabel: {
        color: '#999999',
        fontSize: 14,
        formatter: props.dataType === 'negativeRate' ? '{value}%' : '{value}'
      },
      splitLine: {
        lineStyle: {
          color: '#DDE3EE',
          type: 'dashed'
        }
      }
    },
    series: seriesData
  }
})
</script>

<template>
  <FCard :title="'品牌趋势变化'" titleSize="small" :height="'625px'" class="f-card-border">
    <template #more>
      <SwitchButton
        :model-value="props.dataType"
        :options="MENTION_NEGATIVE_RATE_SWITCH_OPTIONS"
        @change="handleSwitch"
      ></SwitchButton>
    </template>
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
</style>
