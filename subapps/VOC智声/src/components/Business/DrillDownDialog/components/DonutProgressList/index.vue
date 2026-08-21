<script setup lang="ts">
import { computed, ref } from 'vue'
import unionPng from '@/assets/images/drilldown/union.png'
import { fmtPer } from '@/utils'

defineOptions({ name: 'DonutProgressList' })

interface DonutItem {
	label: string
	value: number
	color?: string
  percent?: number
  [key: string]: any
}

interface Props {
	items: DonutItem[]
	itemSize?: number
}

const props = withDefaults(defineProps<Props>(), {
	items: () => [],
	itemSize: 120,
})

const emit = defineEmits<{
	(e: 'itemClick', item: DonutItem): void
}>()

function handleItemClick(item: DonutItem) {
	emit('itemClick', item)
}

const defaultColors = ['#3D8BFF', '#26C1FF', '#F6A200', '#FF7A45', '#52C41A', '#A461D8']

const normalizedItems = computed(() => {
	return props.items.map((item: DonutItem, index: number) => {
		return {
			...item,
			color: item.color || defaultColors[index % defaultColors.length]
		}
	})
})

// 颜色工具：将 #RRGGBB 转为 rgba(..., alpha)
function hexToRgba(hex: string, alpha = 1): string {
	const clean = hex.replace('#', '')
	const bigint = parseInt(clean.length === 3 ? clean.split('').map(c => c + c).join('') : clean, 16)
	const r = (bigint >> 16) & 255
	const g = (bigint >> 8) & 255
	const b = bigint & 255
	return `rgba(${r}, ${g}, ${b}, ${alpha})`
}

// 为每个项目生成图表配置
const getChartOptions = (item: DonutItem, index: number): any => {
  const filledColor = item.color || defaultColors[index % defaultColors.length]
  const remainingColor = hexToRgba(filledColor, 0.15)

  return {
    series: [
      {
        type: 'pie',
        radius: ['70%', '85%'],
        center: ['50%', '50%'],
        data: [
          {
            name: 'filled',
            value: Number(item?.percent || 0),
            itemStyle: {
              color: filledColor
            }
          },
          {
            name: 'remaining',
            value: Math.max(0, 100 - Number(item?.percent || 0)),
            itemStyle: {
              color: remainingColor
            }
          }
        ],
        label: {
          show: false
        },
        tooltip: {
          formatter: (params: any) => `${item.label || ''}：${params.percent || 0}%`
        }
      }
    ]
  }
}
</script>

<template>
	<div class="donut-progress-list">
		<div
			v-for="(item, idx) in normalizedItems"
			:key="`${item.label}-${idx}`"
			class="donut-progress-list__item"
			:style="{ width: '100%', height: '100%' }"
			@click="handleItemClick(item)"
		>
			<div class="donut-progress-list__chart">
				<FEcharts
					:width="'100%'"
					:height="'100%'"
					:options="getChartOptions(item, idx)"
				/>
        <div class="donut-progress-list__center" :style="{ color: item.color }">
          <el-image style="width: 27px;height: 34px" :src="unionPng" />
        </div>
				<div class="donut-progress-list__center" :style="{ color: item.color }">
					{{ fmtPer(item.percent || '') }}
				</div>
			</div>
			<div class="donut-progress-list__label" :style="{ color: item.color }">{{ item.label || ''}}</div>
		</div>
	</div>
</template>

<style scoped lang="scss">
.donut-progress-list {
	display: grid;
	grid-template-columns: repeat(4, minmax(80px, 1fr));
	gap: 16px;
}

.donut-progress-list__item {
	position: relative;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: flex-start;
	cursor: pointer;
	transition: transform 0.2s;

	&:hover {
		transform: scale(1.05);
	}
}

.donut-progress-list__chart {
	position: relative;
	width: 100%;
	height: 100%;
}

.donut-progress-list__center {
	position: absolute;
	top: 50%;
	left: 50%;
	transform: translate(-50%, -50%);
	font-weight: 600;
	font-size: 16px;
}

.donut-progress-list__label {
	margin-top: 8px;
	font-size: 16px;
	line-height: 1;
	text-align: center;
}
</style>
