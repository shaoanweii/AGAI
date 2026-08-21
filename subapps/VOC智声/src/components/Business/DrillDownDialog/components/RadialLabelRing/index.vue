<script setup lang="ts">
import { computed } from 'vue'
import groupCarPng from '@/assets/images/group-car.png'

defineOptions({ name: 'RadialLabelRing' })

interface RadialItem {
  // 标签文字
  label: string
}

interface Props {
  items: RadialItem[]
  // 外环半径（px），用于定位标签
  radius?: number
  // 中心内容区域尺寸（px）
  centerSize?: number
  // 每个标签块尺寸（px）
  badgeSize?: number
}

const props = withDefaults(defineProps<Props>(), {
  items: () => [],
  radius: 130,
  centerSize: 196,
  badgeSize: 88
})

// 极坐标到笛卡尔坐标转换
function polarToCartesian(angleDeg: number, radius: number) {
  const rad = (angleDeg * Math.PI) / 180
  return {
    x: Math.cos(rad) * radius,
    y: Math.sin(rad) * radius
  }
}

// 计算每个标签的角度与坐标（平均分布）
const positioned = computed(() => {
  const count = Math.max(props.items.length, 1)
  // 从正上方开始按顺时针排列
  return props.items.map((it, idx) => {
    const angle = -90 + (360 / count) * idx
    const { x, y } = polarToCartesian(angle, props.radius)
    return { ...it, angle, x, y }
  })
})

// 向父组件派发点击事件
const emit = defineEmits(['label-click'])
const handleLabelClick = (it: RadialItem, idx: number) => {
  emit('label-click', it)
}
</script>

<template>
  <div class="radial-label-ring">
    <!-- 中心内容区域（可插槽覆盖） -->
    <div
      class="radial-label-ring__center"
      :style="{ width: props.centerSize + 'px', height: props.centerSize + 'px' }"
    >
      <slot name="center">
        <!-- 默认中心图标（占位车图标） -->
        <el-image :src="groupCarPng" style="width: 196px; height: 196px" />
      </slot>
    </div>

    <!-- 环形标签 -->
    <div
      v-for="(it, idx) in positioned"
      :key="`${it.label}-${idx}`"
      class="radial-label-ring__badge"
      :style="{
        height: '40px',
        left: `calc(50% + ${it.x}px)`,
        top: `calc(50% + ${it.y}px)`,
        transform: 'translate(-50%, -50%)'
      }"
    >
      <span class="fs-14 radial-label-ring__text" @click="handleLabelClick(it, idx)">{{
        it.label
      }}</span>
    </div>
  </div>
</template>

<style scoped lang="scss">
.radial-label-ring {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 260px;
}

.radial-label-ring__center {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.radial-label-ring__badge {
  position: absolute;
  border-radius: 100px 100px 100px 100px;
  background: #f2f4f7;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04) inset;
  padding: 8px 17px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 10px;
  white-space: nowrap;
}

.radial-label-ring__text {
  color: #5f6a7a;
  font-weight: 600;
  cursor: pointer;
}
</style>
