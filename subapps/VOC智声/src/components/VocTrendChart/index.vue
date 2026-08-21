<script setup lang="ts">
import BarOrLineChart from '@/components/Charts/BarOrLineChart/index.vue'
import type { VocDataItem } from '@/api/common/index.d'

const props = defineProps<{
  data: VocDataItem[]
  loading?: boolean
  needDetails?: boolean
}>()

const emit = defineEmits<{
  (e: 'barClick', payload: { date: string }): void
}>()

// 处理点击事件
const handleBarClick = (payload: { date: string }) => {
  emit('barClick', payload)
}
</script>

<template>
  <div class="voc-trend-chart-container">
    <div v-if="props.loading" class="voc-trend-chart-loading">加载中...</div>
    <div v-else-if="!props.data || props.data.length === 0" class="voc-trend-chart-empty">
      暂无数据
    </div>
    <div v-else class="voc-trend-chart-content">
      <BarOrLineChart
        :data="props.data"
        :need-details="props.needDetails"
        width="100%"
        height="400"
        @bar-click="handleBarClick"
      />
    </div>
  </div>
</template>

<style scoped lang="scss">
.voc-trend-chart-container {
  width: 100%;
  min-height: 420px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px 0 rgba(0, 0, 0, 0.03);
  padding: 24px 24px 0 24px;
  position: relative;

  .voc-trend-chart-loading,
  .voc-trend-chart-empty {
    width: 100%;
    height: 400px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #999;
    font-size: 18px;
  }

  .voc-trend-chart-content {
    width: 100%;
    height: 400px;
  }
}
</style>
