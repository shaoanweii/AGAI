<script setup lang="ts">
import DimensionItem from './DimensionItem.vue'
import type { OpinionTopVo } from '@/api/groupAnalysis/types'

/**
 * 观点评价
 */
defineOptions({
  name: 'ViewpointEvaluation'
})

interface Props {
  data?: OpinionTopVo[] | null
}

const { data } = defineProps<Props>()

// ==================== 事件定义 ====================

const emit = defineEmits<{
  (e: 'row-click', data: any, item: any): void
  (e: 'view-more', payload: any, item: any): void
}>()

// ==================== 事件处理方法 ====================

/**
 * 处理行点击事件
 */
const handleRowClick = (rowData: any, item: any) => {
  emit('row-click', rowData, item)
}

/**
 * 处理查看更多点击事件
 */
const handleViewMore = (payload: any, item: any) => {
  emit('view-more', payload, item)
}
</script>

<template>
  <div class="viewpoint-evaluation">
    <DimensionItem
      v-for="item in data"
      :key="item.brandCode"
      :data="item"
      @row-click="(rowData: any) => handleRowClick(rowData, item)"
      @view-more="(payload: any) => handleViewMore(payload, item)"
    ></DimensionItem>
  </div>
</template>

<style lang="scss" scoped>
.viewpoint-evaluation {
  width: 100%;
  height: 100%;
  display: grid;
  // grid-template-columns: repeat(auto-fill, minmax(334px, 1fr));
  grid-template-columns: repeat(5, minmax(0px, 1fr));
  gap: 16px;
  margin-top: 24px;
}
</style>
