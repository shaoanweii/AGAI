<script setup lang="ts">
import type { AgeDistributionVo, RegionDistributionVo } from '@/api/journeyAnalysis/types'
import { CHART_THEME_COLORS } from '@/constants'
import type { EChartsOption } from 'echarts'
import { computed } from 'vue'
import { fmtPer, fmtFix, fmtNum } from '@/utils'

defineOptions({
  name: 'Left'
})

const { ageDistributionData, regionDistributionData } = defineProps<{
  ageDistributionData: AgeDistributionVo[]
  regionDistributionData: RegionDistributionVo[]
}>()

interface AgeChartItem extends AgeDistributionVo {
  name: string
  value: number
  color: string
  userCount: number
  percentValue: number
  rawData: AgeDistributionVo
  itemStyle: {
    color: string
    borderColor?: string
    borderWidth?: number
  }
}

interface DistributionTooltipParams {
  title: string
  value: number | string
  percent: number | string
  valueMoM: number | string
  valueYoY: number | string
}

const AGE_RING_SEPARATOR_THRESHOLD = 2
const AGE_RING_SEPARATOR_WIDTH = 1.5

/**
 * 将后端返回的百分比统一转成数值，兼容 number / string 两种返回形态。
 */
const normalizeToNumber = (value: number | string | null | undefined) => {
  const parsedValue = Number(value)
  return Number.isFinite(parsedValue) ? parsedValue : 0
}

/**
 * 统一构建分布类图表的 tooltip，避免年龄段和常驻地重复维护表格结构。
 */
const buildDistributionTooltip = ({
  title,
  value,
  percent,
  valueMoM,
  valueYoY
}: DistributionTooltipParams) => {
  return `
    <div style="background: white; border-radius: 4px; padding: 0; font-size: 12px; min-width: 200px;">
      <div class="mb-12 fs-14 fw-500" style="color: #333">
        ${title || ''}
      </div>
      <table style="width: 100%; border-collapse: collapse; margin: 0;">
        <thead>
          <tr style="background: #f0f8ff; padding: 8px 12px; color: #26292E; border-radius: 4px 4px 0 0;">
            <th style="padding: 8px 12px; text-align: left; color: #26292E;" class="fw-400 fs-14">名称</th>
            <th style="padding: 8px 12px; text-align: center; color: #26292E;" class="fw-400 fs-14">数值</th>
            <th style="padding: 8px 12px; text-align: center; color: #26292E;" class="fw-400 fs-14">占比</th>
            <th style="padding: 8px 12px; text-align: center; color: #26292E;" class="fw-400 fs-14">环比</th>
            <th style="padding: 8px 12px; text-align: center; color: #26292E;" class="fw-400 fs-14">同比</th>
          </tr>
        </thead>
        <tbody>
          <tr style="background: white;">
            <td style="padding: 8px 12px; color: #333; font-size: 14px; border-bottom: none;">
              用户数
            </td>
            <td style="padding: 8px 12px; text-align: center; color: #333; font-size: 14px; border-bottom: none;">
              ${fmtNum(value)}
            </td>
            <td style="padding: 8px 12px; text-align: center; color: #333; font-size: 14px; border-bottom: none;">
              ${fmtPer(percent)}
            </td>
            <td style="padding: 8px 12px; text-align: center; color: #666; font-size: 14px; border-bottom: none;">
              ${fmtFix(valueMoM)}
            </td>
            <td style="padding: 8px 12px; text-align: center; color: #666; font-size: 14px; border-bottom: none;">
              ${fmtFix(valueYoY)}
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  `
}

/**
 * 当年龄段分布较为平均时展示白色分隔线；存在极小占比时移除分隔线，避免小扇区被覆盖。
 * 0 值项仅保留在图例中展示，不参与环形图分隔线计算。
 */
const showAgeRingSeparator = computed(() => {
  const percentValues = [...(ageDistributionData || [])]
    .map(item => normalizeToNumber(item.percent))
    .filter(percent => percent > 0)

  return (
    percentValues.length > 1 &&
    percentValues.every(percent => percent >= AGE_RING_SEPARATOR_THRESHOLD)
  )
})

const ageChartData = computed<AgeChartItem[]>(() => {
  return [...(ageDistributionData || [])].map((item, index) => {
    const percentValue = normalizeToNumber(item.percent)
    const color = CHART_THEME_COLORS[index % CHART_THEME_COLORS.length]
    const itemStyle = showAgeRingSeparator.value
      ? {
          color,
          borderColor: '#FFFFFF',
          borderWidth: AGE_RING_SEPARATOR_WIDTH
        }
      : {
          color
        }

    return {
      ...item,
      name: item.title || '未知',
      value: percentValue,
      color,
      userCount: item.value,
      percentValue,
      rawData: item,
      itemStyle
    }
  })
})

/**
 * 年龄段列表需要保留 0 值项，但环形图只绘制真实占比大于 0 的扇区。
 */
const ageRingData = computed<AgeChartItem[]>(() => {
  return ageChartData.value.filter(item => item.percentValue > 0)
})

/**
 * 当后端返回的年龄段项全部为 0 时，仅展示右侧列表，避免出现“全 0 仍绘制占比环形图”的误导。
 */
const shouldShowAgeRing = computed(() => {
  return ageRingData.value.length > 0
})

const chartOptionsByAgeRing = computed<EChartsOption>(() => {
  return {
    tooltip: {
      trigger: 'item',
      confine: true,
      formatter: (params: any) => {
        if (!params || !params.data) return ''

        const data = params.data as AgeChartItem
        return buildDistributionTooltip({
          title: data.title || data.name || '',
          value: data.userCount,
          percent: data.percentValue,
          valueMoM: data.valueMoM,
          valueYoY: data.valueYoY
        })
      }
    },
    series: [
      {
        type: 'pie',
        radius: ['56%', '80%'],
        center: ['50%', '50%'],
        startAngle: 90,
        clockwise: true,
        minAngle: 2,
        avoidLabelOverlap: true,
        label: {
          show: false
        },
        labelLine: {
          show: false
        },
        emphasis: {
          scale: true,
          scaleSize: 6
        },
        data: ageRingData.value,
        animationDuration: 400,
        animationEasing: 'cubicOut'
      }
    ]
  }
})

const chartOptionsByPictorialBar = computed<any>(() => {
  const colors = ['#4895ef', '#52c4b9', '#fbbf58', '#f17d7a', '#9c94c8']
  const xAixsData = regionDistributionData?.map((el: any) => el.provinceName)
  const seriesData = regionDistributionData?.map((el, index) => {
    const colorIndex = index % colors.length
    return {
      ...el,
      name: el.provinceName,
      value: el.percent,
      viewValue: el.value,
      itemStyle: { color: colors[colorIndex] }
    }
  })

  const rateMax = Math.max(...(seriesData || []).map((el: any) => el.percent))

  return {
    grid: {
      left: 0,
      right: 0,
      top: 20,
      bottom: 0,
      containLabel: true
    },
    tooltip: {
      trigger: 'item',
      confine: true,
      formatter: function (params: any) {
        if (!params || !params.data) return ''

        const data = params.data

        return buildDistributionTooltip({
          title: data.provinceName || '',
          value: data.viewValue,
          percent: data.percent,
          valueMoM: data.valueMoM,
          valueYoY: data.valueYoY
        })
      }
    },
    xAxis: {
      type: 'category',
      data: xAixsData,
      axisTick: {
        show: false
      },
      axisLabel: {
        color: '#333333',
        interval: 0,
        fontSize: 14,
        fontWeight: 500,
        lineHeight: 24,
        formatter: (value: any, index: any) => {
          const curItem = seriesData[index]
          return `${curItem.name}\n{p|${curItem.percent}%}`
        },
        rich: {
          p: {
            color: '#333333',
            fontSize: 12,
            fontWeight: 500,
            lineHeight: 24
          }
        }
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '{value}%'
      },
      splitLine: {
        lineStyle: {
          type: 'dashed'
        }
      },
      // max: 40 // 可根据实际数据调整最大值
      max: rateMax,
      interval: Math.max(Math.floor(rateMax / 5), 4)
    },
    series: [
      {
        type: 'pictorialBar',
        symbol: 'triangle', // 使用三角形作为图形
        // symbolSize: [40, 30], // 图形的宽和高
        symbolPosition: 'center',
        // data: [
        //   { value: 33.87, itemStyle: { color: '#4895ef' } }, // 蓝色
        //   { value: 29.25, itemStyle: { color: '#52c4b9' } }, // 青绿色
        //   { value: 18.35, itemStyle: { color: '#fbbf58' } }, // 黄色
        //   { value: 12.4, itemStyle: { color: '#f17d7a' } }, // 橙红色
        //   { value: 8.43, itemStyle: { color: '#9c94c8' } } // 蓝紫色
        // ]
        data: seriesData
      }
    ]
  }
})

// 事件定义
const emit = defineEmits<{
  (e: 'chart-click', data: any): void
}>()

// 处理年龄段图表点击事件
const handleAgeChartClick = (params: any) => {
  if (params.data) {
    emit('chart-click', { ...params.data, chartType: 'age' })
  }
}

// 处理常驻地图表点击事件
const handleRegionChartClick = (params: any) => {
  if (params.data) {
    emit('chart-click', { ...params.data, chartType: 'region' })
  }
}
</script>

<template>
  <div class="left-view">
    <FCard title="年龄段" titleSize="middle" :height="'357px'" class="f-card-border">
      <div v-if="ageChartData.length" class="age-chart-panel">
        <div v-if="shouldShowAgeRing" class="age-chart-ring">
          <FEcharts
            :options="chartOptionsByAgeRing"
            :width="'100%'"
            :height="'100%'"
            @chart-click="handleAgeChartClick"
          />
        </div>
        <div v-else class="age-chart-ring">
          <el-empty description="暂无有效占比" :image-size="76" style="padding: 0" />
        </div>
        <div class="age-chart-legend">
          <div
            v-for="item in ageChartData"
            :key="`${item.name}-${item.percentValue}`"
            class="age-legend-item"
          >
            <span class="age-legend-dot" :style="{ backgroundColor: item.color }"></span>
            <el-tooltip :content="item.name" placement="top" popper-class="text-tooltip-light">
              <span class="age-legend-name">{{ item.name }}</span>
            </el-tooltip>
            <span class="age-legend-percent">{{ fmtPer(item.percentValue) }}</span>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无数据" style="padding: 0" />
    </FCard>
    <FCard title="常驻地" titleSize="middle" :height="'357px'" class="f-card-border mt-24">
      <div
        v-if="regionDistributionData && regionDistributionData.length"
        class="region-chart-panel"
      >
        <FEcharts
          :options="chartOptionsByPictorialBar"
          :width="'100%'"
          :height="'100%'"
          @chart-click="handleRegionChartClick"
        />
      </div>
      <el-empty v-else description="暂无数据" style="padding: 0" />
    </FCard>
  </div>
</template>

<style lang="scss" scoped>
.left-view {
  .age-chart-panel {
    height: 100%;
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .age-chart-ring {
    flex: 0 0 176px;
    width: 176px;
    height: 176px;
  }

  .age-chart-legend {
    flex: 1;
    min-width: 0;
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    column-gap: 20px;
    row-gap: 18px;
  }

  .age-legend-item {
    display: flex;
    align-items: center;
    min-width: 0;
  }

  .age-legend-dot {
    width: 12px;
    height: 12px;
    border-radius: 4px;
    margin-right: 4px;
    flex-shrink: 0;
  }

  .age-legend-name {
    flex: 1;
    min-width: 0;
    cursor: pointer;
    color: #5b6472;
    font-size: 14px;
    line-height: 20px;
    font-weight: 500;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .age-legend-percent {
    flex-shrink: 0;
    margin-left: 12px;
    color: #2f3640;
    font-size: 14px;
    line-height: 20px;
    font-weight: 600;
  }

  .region-chart-panel {
    height: 261px;
  }
}
</style>
