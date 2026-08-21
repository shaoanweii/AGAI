<script setup lang="ts">
import { ref, computed } from 'vue'
import type { ChannelMentionShareVo } from '@/api/productAnalysis/types'
import { CHART_THEME_COLORS } from '@/constants'
import { fmtNum, fmtPer, fmtFix } from '@/utils'

defineOptions({
  name: 'ProportionOfPieChart'
})

// 接收数据
interface Props {
  data?: any
}

const props = withDefaults(defineProps<Props>(), {
  data: () => []
})

// 颜色配置
const colors = CHART_THEME_COLORS

// 处理图表数据
const chartData = computed(() => {
  if (!props.data || props.data.length === 0) {
    return []
  }
  return props.data.map((item: any, index: number) => ({
    name: item.name,
    value: item.value, // 使用 value 字段
    ...item, // 使用对象解构将剩余参数解构到包装对象中
    itemStyle: { color: colors[index % colors.length] }
  }))
})

// 事件定义
const emit = defineEmits<{
  (e: 'chart-click', data: ChannelMentionShareVo): void
}>()

// 处理图表点击事件
const handleChartClick = (params: any) => {
  if (params.data) {
    emit('chart-click', params.data)
  }
}

const chartOptions = computed((): any => ({
  tooltip: {
    show: true,
    trigger: 'item',
    axisPointer: {
      type: 'shadow'
    },
    confine: true,
    formatter: function (params: any) {
      const _data = params.data
      if (!_data) return ''
      // 构建表格HTML
      let tableHtml = `
        <div style="background: white; border-radius: 4px; padding: 0; font-size: 12px; min-width: 200px;">
          <div class="mb-12 fs-14 fw-500" style="color: #333">
            ${_data.channelName || ''}
          </div>
          <table style="width: 100%; border-collapse: collapse; margin: 0;">
            <thead>
              <tr style="background: #f0f8ff; padding: 8px 12px; color: #26292E; border-radius: 4px 4px 0 0;">
                <th style="padding: 8px 12px; text-align: left; color: #26292E;" class="fw-400 fs-14">名称</th>
                <th style="padding: 8px 12px; text-align: center; color: #26292E;" class="fw-400 fs-14">数值</th>
                <th style="padding: 8px 12px; text-align: center; color: #26292E;" class="fw-400 fs-14">占比</th>
                <th style="padding: 8px 12px; text-align: center; color: #26292E;" class="fw-400 fs-14">环比</th>
              </tr>
            </thead>
            <tbody>
      `

      // 定义要显示的数据行
      const dataRows = [
        {
          name: '事件数',
          value: fmtNum(_data.mentions),
          mom: fmtFix(_data.mentionsMoM),
          yoy: fmtFix(_data.mentionsYoY),
          percent: fmtPer(params?.percent)
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
              ${row.percent}
            </td>
             <td style="padding: 8px 12px; text-align: center; color: #666; font-size: 14px; border-bottom: ${noBorder ? 'none' : '1px solid #e9ecef'};">
              ${row.mom}
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
  series: [
    {
      name: '平台占比',
      type: 'pie',
      radius: ['45%', '70%'], // 设置环形的内外半径，这里 50% 是内半径，70% 是外半径
      data: chartData.value,
      label: {
        show: true,
        formatter: (params: any) => {
          return `${params.name}: ${params.data.share}%`
        }
        // 标签格式，显示名称和百分比数值
      },
      itemStyle: {
        borderRadius: 0, // 扇形角的圆角半径，让扇形边缘更圆润
        borderColor: '#fff', // 边框颜色设为白色
        borderWidth: 1 // 边框宽度控制间隙大小，值越大间隙越宽
      }
    }
  ]
}))
</script>

<template>
  <FEcharts
    :options="chartOptions"
    :width="'100%'"
    :height="'260px'"
    :isEmpty="chartData.length === 0"
    emptyDescription="暂无数据"
    @chart-click="handleChartClick"
  />
</template>

<style scoped>
/* 组件样式 */
</style>
