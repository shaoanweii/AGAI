<template>
  <FEcharts
    :options="chartOptions"
    :width="width"
    :height="height"
    :theme="theme"
    :auto-resize="true"
    :auto-update="true"
    @chart-ready="handleChartReady"
    @chart-click="handleChartClick"
  />
</template>

<script setup lang="ts">
/**
 * FMapChart 地图图表组件
 * @description 基于 FEcharts 的中国地图数据可视化组件，支持自定义数据源、字段映射、tooltips 等功能
 * @author Vue Team
 * @since 1.0.0
 */

import { computed, onMounted } from 'vue'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'
import china from '@/constants/china.json'
// import { getExperienceIndexMapTooltip } from '@/utils/echartsTooltipConfig'
import type {
  VisualMapConfig,
  GeoConfig,
  TooltipFormatter,
  FMapChartProps,
  FMapChartEmits
} from './types'

// 定义 Props
const props = withDefaults(defineProps<FMapChartProps>(), {
  data: () => [],
  width: '100%',
  height: '100%',
  theme: '',
  zoom: 1.0,
  roam: false,
  showVisualMap: true,
  fieldMapping: () => ({
    nameField: 'name',
    valueField: 'value'
  })
})

// 定义事件
const emit = defineEmits<FMapChartEmits>()

// 注册中国地图数据
onMounted(() => {
  echarts.registerMap('china', china as any)
  console.log('data', props.data)
})

// 默认视觉映射配置
const defaultVisualMapConfig: VisualMapConfig = {
  type: 'continuous',
  min: 0,
  max: 100,
  right: 10,
  bottom: 20,
  text: ['100%', '0%'],
  calculable: true,
  orient: 'vertical',
  itemWidth: 8,
  itemHeight: 120,
  inRange: {
    color: ['#fff', '#FEE9E5', '#FFD0C9', '#FFB5AD', '#FF9991', '#FF5959']
  },
  textStyle: {
    color: '#606266',
    fontSize: 12
  },
  show: true
}

// 默认地理配置
const defaultGeoConfig: GeoConfig = {
  map: 'china',
  roam: false,
  zoom: 1.0,
  top: 20,
  left: 20,
  right: 20,
  width: '90%',
  height: '95%',
  label: { show: false },
  itemStyle: {
    borderColor: '#D9D9D9',
    areaColor: '#f5f7fa'
  },
  emphasis: {
    label: { show: false },
    itemStyle: {
      areaColor: '#00B0F0',
      borderWidth: 0
    }
  },
  select: {
    label: {
      show: false
    },
    itemStyle: {
      areaColor: '#e1f3d8',
      shadowOffsetX: 0,
      shadowOffsetY: 0,
      borderWidth: 2,
      borderColor: '#D9D9D9'
    }
  }
}

// 默认 Tooltip 格式化函数
const defaultTooltipFormatter: TooltipFormatter = (params: any) => {
  if (params.data) {
    const data = params.data
    const provinceName = data?.provinceName || params?.name || '未知'
    const nr = data?.negativeRate
    const mom = data?.negativeRateMoM
    const yoy = data?.negativeRateYoY
    const mentions = data?.mentions
    return `
      <div style="padding: 12px;">
        <div style="font-weight: 600; margin-bottom: 8px; color: #303133;">${provinceName}</div>
        <div style="margin-bottom: 4px;">负面率: <span style="color: #f56c6c; font-weight: 500;">${nr ? nr + '%' : '-'}</span></div>
        ${typeof mentions === 'number' ? `<div style="margin-bottom: 4px;">提及量: <span style="color: #409eff; font-weight: 500;">${mentions}</span></div>` : ''}
        ${typeof mom === 'number' ? `<div style="margin-bottom: 4px;">环比: <span style="color: ${mom >= 0 ? '#f56c6c' : '#67c23a'}; font-weight: 500;">${mom >= 0 ? '+' : ''}${mom}%</span></div>` : ''}
        ${typeof yoy === 'number' ? `<div>同比: <span style="color: ${yoy >= 0 ? '#f56c6c' : '#67c23a'}; font-weight: 500;">${yoy >= 0 ? '+' : ''}${yoy}%</span></div>` : ''}
      </div>
    `
  }
  return `${params.name}: 暂无数据`
}

// 计算合并后的配置
const mergedVisualMapConfig = computed(() => ({
  ...defaultVisualMapConfig,
  ...props.visualMapConfig
}))

const mergedGeoConfig = computed(() => ({
  ...defaultGeoConfig,
  ...props.geoConfig,
  roam: props.roam,
  zoom: props.zoom
}))

// 处理地图数据
const processedMapData = computed(() => {
  const { nameField, valueField } = props.fieldMapping

  return props.data.map(item => {
    const result = { ...item }
    result.name = item[nameField]
    result.value = item[valueField]
    return result
  })
})

// 生成图表配置选项
const chartOptions = computed<EChartsOption>(() => {
  const visualMapConfig = mergedVisualMapConfig.value
  const geoConfig = mergedGeoConfig.value
  const tooltipFormatter = props.tooltipFormatter || defaultTooltipFormatter

  return {
    tooltip: {
      show: false,
      trigger: 'item',
      backgroundColor: '#fff',
      borderWidth: 1,
      borderColor: '#e4e7ed',
      borderRadius: 4,
      extraCssText: 'box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1)',
      textStyle: {
        color: '#606266',
        fontSize: 14
      },
      formatter: tooltipFormatter
    },
    visualMap: props.showVisualMap
      ? {
          ...visualMapConfig,
          show: visualMapConfig.show
        }
      : {
          show: false
        },

    series: [
      {
        name: '负面率',
        type: 'map',
        map: 'china',
        roam: false,
        zoom: 1.0,
        center: [104, 35],
        top: 20,
        bottom: 20,
        left: 40,
        right: 40,
        data: processedMapData.value,
        label: {
          show: true,
          color: '#303133',
          fontSize: 11,
          fontWeight: 500,
          formatter: '{b}'
        },
        itemStyle: {
          borderColor: '#D9D9D9',
          borderWidth: 1,
          areaColor: '#f5f7fa'
        },
        emphasis: {
          disabled: true
        },
        select: {
          disabled: true,
          label: {
            show: true,
            color: '#303133',
            fontSize: 12,
            fontWeight: 600
          },
          itemStyle: {
            areaColor: '#e1f3d8',
            borderColor: '#D9D9D9',
            borderWidth: 2
          }
        }
      }
    ]
  }
})

// 事件处理
const handleChartReady = (chart: any) => {
  emit('chart-ready', chart)
}

const handleChartClick = (params: any) => {
  emit('chart-click', params)
}
</script>
