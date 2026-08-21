<script setup lang="ts">
import { computed } from 'vue'
import { fmtPer } from '@/utils'

defineOptions({ name: 'TriangleRankList' })

interface TriangleItem {
  label: string
  value: number
  percent?: string | number
  displayText?: string
  color?: string
}

interface Props {
  items: TriangleItem[]
  // 固定底边宽度（像素）
  baseWidth?: number
  // 高度范围：最小/最大像素
  minHeight?: number
  maxHeight?: number
}

const props = withDefaults(defineProps<Props>(), {
  items: () => [],
  baseWidth: 64,
  minHeight: 28,
  maxHeight: 96
})

const emit = defineEmits<{
  (e: 'item-click', item: TriangleItem): void
}>()

function handleItemClick(item: TriangleItem) {
  emit('item-click', item)
}

const defaultColors = ['#3D8BFF', '#26C1FF', '#F6A200', '#FF7A45', '#7A8DCC', '#52C41A']

const maxVal = computed(() =>
  props.items.reduce(
    (m: number, it: TriangleItem) => Math.max(m, Number.isFinite(it.value) ? it.value : 0),
    0
  )
)

// 生成等腰三角的 points 字符串（顶点在上，底边在下）
function makeTrianglePoints(height: number, width: number): string {
  // 顶点 (width/2, 0); 左下 (0, height); 右下 (width, height)
  const apexX = width / 2
  return `${apexX},0 0,${height} ${width},${height}`
}

const normalized = computed(() => {
  const span = Math.max(0, props.maxHeight - props.minHeight)
  return props.items.map((it: TriangleItem, idx: number) => {
    const ratio = maxVal.value > 0 ? it.value / maxVal.value : 0
    const height = Math.round(props.minHeight + span * ratio)
    return {
      ...it,
      color: it.color || defaultColors[idx % defaultColors.length],
      width: props.baseWidth,
      height,
      points: makeTrianglePoints(height, props.baseWidth)
    }
  })
})
</script>

<template>
  <div v-if="normalized.length" class="triangle-rank-list">
    <div
      v-for="(it, idx) in normalized"
      :key="`${it.label}-${idx}`"
      class="triangle-rank-list__item"
      @click="handleItemClick(it)"
    >
      <svg
        :width="it.width"
        :height="it.height"
        :viewBox="`0 0 ${it.width} ${it.height}`"
        class="triangle-rank-list__svg"
      >
        <polygon :points="it.points" :fill="it.color" />
      </svg>
      <div class="triangle-rank-list__label single-line-ellipsis">{{ it.label }}</div>
      <!-- || fmtPer(it.percent || '') -->
      <div class="triangle-rank-list__percent">{{ it.displayText }}</div>
    </div>
  </div>
  <div v-else>
    <el-empty description="暂无数据" style="padding: 0" :image-size="80" />
  </div>
</template>

<style scoped lang="scss">
.triangle-rank-list {
  display: grid;
  grid-template-columns: repeat(5, minmax(80px, 1fr));
  gap: 20px;
  align-items: end;
}

.triangle-rank-list__item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: transform 0.2s;

  &:hover {
    transform: scale(1.05);
  }
}

.triangle-rank-list__svg {
  display: block;
}

.triangle-rank-list__label {
  margin-top: 6px;
  font-size: 14px;
  line-height: 1;
  text-align: center;
}

.triangle-rank-list__percent {
  margin-top: 6px;
  font-size: 14px;
  line-height: 1;
  text-align: center;
  opacity: 0.9;
}
</style>
