<template>
  <el-tooltip :content="tooltipContent" placement="top" raw-content popper-class="data-tooltip">
    <slot />
  </el-tooltip>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { fmtFix } from '@/utils'

defineOptions({
  name: 'DataTooltip'
})

interface TooltipData {
  title: string
  name: string
  value: string | number
  percent?: string
  mom?: number
  yoy?: number
}

interface Props {
  data: TooltipData
}

const props = defineProps<Props>()

const tooltipContent = computed(() => {
  const { title, name, value, percent, mom, yoy } = props.data

  return `
    <div style="background: white; border-radius: 4px; padding: 12px 16px; font-size: 12px; min-width: 200px;">
      <div style="margin-bottom: 12px; font-size: 14px; font-weight: 500; color: #333;">
        ${title || ''}
      </div>
      <table style="width: 100%; border-collapse: collapse; margin: 0;">
        <thead>
          <tr style="background: #f0f8ff; padding: 8px 12px; color: #26292E; border-radius: 4px 4px 0 0;">
            <th style="padding: 8px 12px; text-align: left; color: #26292E; font-weight: 400; font-size: 14px;">名称</th>
            <th style="padding: 8px 12px; text-align: center; color: #26292E; font-weight: 400; font-size: 14px;">数值</th>
            ${percent ? '<th style="padding: 8px 12px; text-align: center; color: #26292E; font-weight: 400; font-size: 14px;">占比</th>' : ''}
            <th style="padding: 8px 12px; text-align: center; color: #26292E; font-weight: 400; font-size: 14px;">环比</th>
            <th style="padding: 8px 12px; text-align: center; color: #26292E; font-weight: 400; font-size: 14px;">同比</th>
          </tr>
        </thead>
        <tbody>
          <tr style="background: white;">
            <td style="padding: 8px 12px; color: #333; font-size: 14px;">
              ${name}
            </td>
            <td style="padding: 8px 12px; text-align: center; color: #333; font-size: 14px;">
              ${value}
            </td>
            ${percent ? `<td style="padding: 8px 12px; text-align: center; color: #666; font-size: 14px;">${percent}</td>` : ''}
            <td style="padding: 8px 12px; text-align: center; color: #666; font-size: 14px;">
              ${fmtFix(mom)}
            </td>
            <td style="padding: 8px 12px; text-align: center; color: #666; font-size: 14px;">
              ${fmtFix(yoy)}
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  `
})
</script>

<style>
.data-tooltip {
  background: transparent !important;
  border: none !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15) !important;
  padding: 0 !important;
}

.data-tooltip .el-popper__arrow::before {
  background: white !important;
  border: 1px solid white !important;
}

.data-tooltip .el-popper__arrow {
  background: white !important;
  border: 1px solid white !important;
}
</style>
