<script setup lang="ts">
import { FEcharts } from '@/components/global'
import CCCard from './CCCard.vue'
import { computed } from 'vue'
import type { ComparativeBriefVo, TrendVo } from '@/api/competitorAnalysis/types'
import { fmtPer, fmtFix, fmtNum } from '@/utils'

defineOptions({
  name: 'ComprehensiveComparison'
})

const props = defineProps<{
  comparativeBriefData: ComparativeBriefVo[] | undefined
  trendChangeCompareData: TrendVo[] | undefined
}>()

// 定义事件
const emit = defineEmits<{
  (e: 'cardClick', data: ComparativeBriefVo): void
  (e: 'chartClick', data: any): void
}>()

// 处理CCCard卡片点击事件
const handleCardClick = (data: ComparativeBriefVo) => {
  emit('cardClick', data)
}

// 处理FEcharts图表点击事件
const handleChartClick = (params: any) => {
  emit('chartClick', params)
}

// 处理图表数据
const chartData = computed(() => {
  if (!props.trendChangeCompareData || props.trendChangeCompareData.length === 0) {
    return {
      xAxisData: [],
      seriesMap: new Map()
    }
  }

  const seriesMap = new Map<string, any[]>()
  const xAxisData: string[] = []
  const allNames = new Set<string>()

  // 收集所有品牌名称
  props.trendChangeCompareData.forEach(trend => {
    trend.items?.forEach(item => {
      const key = item.name || item.code || ''
      if (key) allNames.add(key)
    })
  })

  // 按品牌车系分组数据，补0处理
  props.trendChangeCompareData.forEach(trend => {
    if (trend.date) {
      xAxisData.push(trend.date)
    }

    // 为每个品牌创建数据项，如果不存在则补0
    allNames.forEach(name => {
      if (!seriesMap.has(name)) {
        seriesMap.set(name, [])
      }

      const existingItem = trend.items?.find(item => (item.name || item.code) === name)

      seriesMap.get(name)!.push({
        date: trend.date,
        code: existingItem?.code || null,
        name: name,
        negativeRate: existingItem?.negativeRate ?? 0,
        negativeRateMoM: existingItem?.negativeRateMoM ?? 0,
        negativeRateYoY: existingItem?.negativeRateYoY ?? 0,
        mentions: existingItem?.mentions ?? 0,
        mentionsMoM: existingItem?.mentionsMoM ?? 0,
        mentionsYoY: existingItem?.mentionsYoY ?? 0
      })
    })
  })

  return {
    xAxisData,
    seriesMap
  }
})

const chartOptions = computed<any>(() => {
  const { xAxisData, seriesMap } = chartData.value

  // 计算Y轴最大值
  const allNegativeRates: number[] = []
  const allMentions: number[] = []

  seriesMap.forEach(data => {
    data.forEach((item: any) => {
      allNegativeRates.push(Number(item.negativeRate) || 0)
      allMentions.push(Number(item.mentions) || 0)
    })
  })

  const maxNegativeRate = Math.max(...allNegativeRates, 0)
  const negativeRateMax = maxNegativeRate <= 100 ? 100 : Math.ceil(maxNegativeRate / 10) * 10

  const maxMention = Math.max(...allMentions, 0)
  const mentionMax = Math.ceil((maxMention * 1.1) / 100) * 100

  // 判断是否显示dataZoom：数据超过12条时显示
  const dataCount = xAxisData.length
  const showDataZoom = dataCount > 12
  const zoomEnd = showDataZoom ? Math.floor((10 / dataCount) * 100) : 100

  // 生成系列数据
  const series: any[] = []
  const legendData: string[] = []
  const colors = ['#82E3C7', '#60B8EB', '#FF8A8B', '#FAB007', '#9B8BFF']
  let colorIndex = 0

  seriesMap.forEach((data, name) => {
    const color = colors[colorIndex % colors.length]
    colorIndex++

    legendData.push(name)

    // 负面率折线
    series.push({
      name: `${name}_line`,
      type: 'line',
      yAxisIndex: 0,
      smooth: true,
      showSymbol: false,
      lineStyle: {
        width: 3,
        // type: 'dashed',
        color
      },
      itemStyle: {
        color
      },
      data: data.map((item: any) => ({
        ...item,
        value: item.negativeRate
      }))
    })

    // 提及量柱状图
    series.push({
      name,
      type: 'bar',
      yAxisIndex: 1,
      barWidth: 24,
      itemStyle: {
        color,
        borderColor: '#fff',
        borderWidth: 2
      },
      data: data.map((item: any) => ({
        ...item,
        value: item.mentions
      }))
    })
  })

  return {
    grid: {
      left: 30,
      right: 30,
      top: 50,
      bottom: showDataZoom ? 90 : 60,
      containLabel: true
    },
    dataZoom: showDataZoom
      ? [
          {
            type: 'slider',
            show: true,
            xAxisIndex: [0],
            start: 0,
            end: zoomEnd,
            bottom: 30,
            height: 20,
            zoomLock: true,
            borderColor: 'transparent',
            backgroundColor: '#F5F7FA',
            fillerColor: 'rgba(22, 119, 255, 0.15)',
            handleSize: 0,
            textStyle: {
              color: '#666'
            },
            moveHandleSize: 0,
            showDetail: false,
            brushSelect: false
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
        type: 'shadow'
      },
      confine: true,
      formatter: function (params: any) {
        if (!params || params.length === 0) return ''

        const date = params[0].axisValue

        // 获取当前时间点的所有数据项
        const trendData = props.trendChangeCompareData?.find(t => t.date === date)
        if (!trendData || !trendData.items) return ''

        // 分离市场均值和品牌车系数据（市场均值的name可能是"市场均值"）
        const marketAvg = trendData.items.find(
          item => item.code === 'market_avg' || item.name === '市场均值'
        )
        const brands = trendData.items.filter(
          item => item.code !== 'market_avg' && item.name !== '市场均值'
        )

        let tableHtml = `
        <div style="background: white; border-radius: 4px; padding: 0; font-size: 12px; min-width: 200px;">
          <div class="mb-12 fs-14 fw-500" style="color: #333">
            ${date || ''}
          </div>
          <table style="width: 100%; border-collapse: collapse; margin: 0;">
            <thead>
              <tr style="background: #f0f8ff; padding: 8px 12px; color: #26292E; border-radius: 4px 4px 0 0;">
                <th style="padding: 8px 12px; text-align: left; color: #26292E;" class="fw-400 fs-14">名称</th>
                <th style="padding: 8px 12px; text-align: center; color: #26292E;" class="fw-400 fs-14">数值</th>
                <th style="padding: 8px 12px; text-align: center; color: #26292E;" class="fw-400 fs-14">负面率</th>
              </tr>
            </thead>
            <tbody>
      `

        // 添加市场均值行
        if (marketAvg) {
          tableHtml += `
            <tr style="background: white;">
              <td style="padding: 8px 12px; color: #333; font-size: 14px; border-bottom: none;">${marketAvg.name || '市场均值'}</td>
              <td style="padding: 8px 12px; text-align: center; color: #333; font-size: 14px; border-bottom: none;">${fmtNum(marketAvg.mentions)}</td>
              <td style="padding: 8px 12px; text-align: center; color: #333; font-size: 14px; border-bottom: none;">${fmtPer(marketAvg.negativeRate)}</td>
            </tr>
          `
        }

        // 添加品牌车系行（最多两个）
        brands.slice(0, 2).forEach((brand, index) => {
          tableHtml += `
            <tr style="background: ${index % 2 === 0 ? '#fafafa' : 'white'};">
              <td style="padding: 8px 12px; color: #333; font-size: 14px; border-bottom: none;">${brand.name || ''}</td>
              <td style="padding: 8px 12px; text-align: center; color: #333; font-size: 14px; border-bottom: none;">${fmtNum(brand.mentions)}</td>
              <td style="padding: 8px 12px; text-align: center; color: #333; font-size: 14px; border-bottom: none;">${fmtPer(brand.negativeRate)}</td>
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
      show: true,
      bottom: showDataZoom ? 50 : 20,
      left: 'center',
      textStyle: {
        color: '#6E7B91'
      },
      data: legendData
    },
    xAxis: [
      {
        type: 'category',
        data: xAxisData,
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
        nameTextStyle: {
          padding: [0, 0, 0, 36]
        },
        axisLabel: {
          formatter: '{value}'
        }
      }
    ],
    series
  }
})
</script>

<template>
  <div class="comprehensive-comparison">
    <CCCard :comparativeBriefData="comparativeBriefData" @cardClick="handleCardClick"></CCCard>

    <div class="cc-chart">
      <FEcharts
        :options="chartOptions"
        :isEmpty="!chartData.xAxisData || chartData.xAxisData.length === 0"
        :width="'100%'"
        :height="'100%'"
        @chartClick="handleChartClick"
      ></FEcharts>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.comprehensive-comparison {
  margin-top: 24px;

  .cc-chart {
    width: 100%;
    height: 553px;
    box-shadow: 0px 1px 2px 0px rgba(10, 13, 18, 0.05);
    border-radius: 8px 8px 8px 8px;
    border: 1px solid #ebedf0;
    margin-top: 24px;
  }
}
</style>
