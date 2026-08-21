<template>
  <FEcharts
    :options="chartOptions"
    :width="width"
    :height="height"
    :auto-resize="true"
    :auto-update="true"
    @chart-ready="handleChartReady"
    @chart-click="handleChartClick"
  />
</template>

<script setup lang="ts">
/**
 * MapChart 地图图表组件
 * @description 基于 FEcharts 的中国地图数据可视化组件，支持自定义数据源、字段映射、tooltips 等功能
 * @author Vue Team
 * @since 1.0.0
 */

import { computed, onMounted } from 'vue'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'
import china from '@/constants/china.json'
import FEcharts from '@/components/Charts/FEcharts/index.vue'
import { fmtFix } from '@/utils'

// 定义数据项接口
interface MapDataItem {
  /**
   * 省份名称
   * 示例："广东省"
   */
  provinceName: string

  /**
   * 省份行政区划代码
   * 遵循国家标准 GB/T 2260，6位数字编码
   * 示例："440000"（广东省代码）
   */
  provinceCode: string

  /**
   * 负面率（百分比）
   * 表示该省份相关内容中负面信息所占比例
   * 示例：38.79（即 38.79%）
   */
  negativeRate: number

  /**
   * 负面率环比变化（百分比）
   * 与上一个统计周期（如上月）相比的负面率变化幅度
   * 正值表示环比上升，负值表示环比下降
   * 示例：17.24（即环比上升 17.24%）
   */
  negativeRateMoM?: number

  /**
   * 负面率同比变化（百分比）
   * 与去年同期（如去年同月）相比的负面率变化幅度
   * 正值表示同比上升，负值表示同比下降
   * 示例：16.37（即同比上升 16.37%）
   */
  negativeRateYoY?: number

  /**
   * 提及量
   * 该省份在统计周期内被提及的总次数
   * 示例：678（表示被提及 678 次）
   */
  mentions?: number
  mentionsMoM?: string
  mentionsYoY?: string
}

// 定义 Props 接口
interface Props {
  data: MapDataItem[]
  width?: string
  height?: string
}

// 定义 Props
const props = withDefaults(defineProps<Props>(), {
  data: () => [],
  width: '100%',
  height: '100%'
})

// 定义事件
const emit = defineEmits<{
  'chart-ready': [chart: any]
  'chart-click': [params: any]
}>()

// 注册中国地图数据
onMounted(() => {
  echarts.registerMap('china', china as any)
})

// 省份名称映射函数
const getSimplifiedProvinceName = (fullName: string): string => {
  const mapping: Record<string, string> = {
    北京市: '北京',
    天津市: '天津',
    河北省: '河北',
    山西省: '山西',
    内蒙古自治区: '内蒙古',
    辽宁省: '辽宁',
    吉林省: '吉林',
    黑龙江省: '黑龙江',
    上海市: '上海',
    江苏省: '江苏',
    浙江省: '浙江',
    安徽省: '安徽',
    福建省: '福建',
    江西省: '江西',
    山东省: '山东',
    河南省: '河南',
    湖北省: '湖北',
    湖南省: '湖南',
    广东省: '广东',
    广西壮族自治区: '广西',
    海南省: '海南',
    重庆市: '重庆',
    四川省: '四川',
    贵州省: '贵州',
    云南省: '云南',
    西藏自治区: '西藏',
    陕西省: '陕西',
    甘肃省: '甘肃',
    青海省: '青海',
    宁夏回族自治区: '宁夏',
    新疆维吾尔自治区: '新疆',
    台湾省: '台湾',
    香港特别行政区: '香港',
    澳门特别行政区: '澳门'
  }
  return mapping[fullName] || fullName
}

// 处理地图数据
const processedMapData = computed(() => {
  return props.data.map(item => ({
    name: getSimplifiedProvinceName(item.provinceName),
    value: item.negativeRate,
    ...item
  }))
})

// 计算数值范围
const valueRange = computed(() => {
  if (!props.data.length) return { min: 0, max: 100 }

  const values = props.data.map(item => item.negativeRate).filter(val => val != null && !isNaN(val))
  if (values.length === 0) return { min: 0, max: 100 }

  const min = Math.min(...values)
  const max = Math.max(...values)

  return { min, max }
})

// 事件处理
const handleChartReady = (chart: any) => {
  emit('chart-ready', chart)
}

const handleChartClick = (params: any) => {
  // 找到对应的数据项
  const clickedData = props.data.find(
    item => getSimplifiedProvinceName(item.provinceName) === params.name
  )
  emit('chart-click', {
    ...params,
    data: clickedData
  })
}

// 生成图表配置选项
const chartOptions = computed<any>(() => {
  const { min, max } = valueRange.value
  const hasValidData = props.data.length > 0 && props.data.some(item => item.mentions)

  const baseConfig: any = {
    tooltip: {
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
      position: function (
        point: [number, number],
        params: any,
        dom: HTMLDivElement | null,
        rect: any,
        size: any
      ) {
        const [mouseX, mouseY] = point
        const [tooltipWidth, tooltipHeight] = size.contentSize
        const [chartWidth, chartHeight] = size.viewSize

        // 判断鼠标在地图的左半边还是右半边
        const isLeftHalf = mouseX < chartWidth / 2

        let x,
          y = mouseY + 20

        if (isLeftHalf) {
          // 鼠标在左半边，tooltip显示在右下方
          x = mouseX + 20
        } else {
          // 鼠标在右半边，tooltip显示在左下方
          x = mouseX - tooltipWidth - 20
        }

        // 确保不超出边界
        if (x < 0) x = 10
        if (x + tooltipWidth > chartWidth) x = chartWidth - tooltipWidth - 10
        if (y + tooltipHeight > chartHeight) y = mouseY - tooltipHeight - 10

        return [x, y]
      },
      formatter: (params: any) => {
        // 根据省份名称查找完整数据
        const fullData =
          props.data.find(item => getSimplifiedProvinceName(item.provinceName) === params.name) ||
          ({} as MapDataItem)

        if (fullData) {
          let tableHtml = `
        <div style="background: white; border-radius: 4px; padding: 0; font-size: 12px; min-width: 200px;">
          <div class="mb-12 fs-14 fw-500" style="color: #333">
            ${params.name || ''}
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
              name: '提及量',
              value: fullData?.mentions || '-',
              mom: fmtFix(fullData?.mentionsMoM),
              yoy: fmtFix(fullData?.mentionsYoY)
            },
            {
              name: '负面率',
              value: fmtFix(fullData?.negativeRate),
              mom: fmtFix(fullData?.negativeRateMoM),
              yoy: fmtFix(fullData?.negativeRateYoY)
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
             <td style="padding: 8px 12px; text-align: center; color: #666; font-size: 14px; border-bottom: ${noBorder ? 'none' : '1px solid #e9ecef'};">
              ${row.mom}
            </td>
            <td style="padding: 8px 12px; text-align: center; color: #666; font-size: 14px; border-bottom: ${noBorder ? 'none' : '1px solid #e9ecef'};">
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
        return `${params.name}: 暂无数据`
      }
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
        left: 20,
        right: 20,
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
          areaColor: '#fff'
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
  // 只有在有有效数据时才添加visualMap
  if (hasValidData) {
    baseConfig.visualMap = {
      type: 'continuous',
      min,
      max,
      left: 0,
      bottom: 0,
      text: ['', '负面率'],
      calculable: true,
      orient: 'horizontal',
      itemWidth: 20,
      itemHeight: 120,
      inRange: {
        color: ['#fff', '#FEE9E5', '#FFD0C9', '#FFB5AD', '#FF9991', '#FF5959']
      },
      textStyle: {
        color: '#606266',
        fontSize: 12
      }
    }
  }
  // 数据为空时不添加visualMap，让地图使用itemStyle中的固定颜色

  return baseConfig
})
</script>
