<script setup lang="ts">
import TopTable from './TopTable.vue'
import type { OpinionTopVo } from '@/api/groupAnalysis/types'
import brandLogo from '@/assets/images/brand/voc-voice-mark-v2.png'

defineOptions({
  name: 'DimensionItem'
})

interface Props {
  data?: OpinionTopVo
}

const { data } = defineProps<Props>()

// ==================== 事件定义 ====================

const emit = defineEmits<{
  (e: 'row-click', data: any): void
  (e: 'view-more', payload: any): void
}>()

// ==================== 事件处理方法 ====================

/**
 * 处理行点击事件
 */
const handleRowClick = (rowData: any) => {
  emit('row-click', rowData)
}

/**
 * 处理查看更多点击事件
 */
const handleViewMore = (payload: any) => {
  emit('view-more', payload)
}
</script>

<template>
  <div class="dimension-item">
    <div class="di-header">
      <div class="dih-logo">
        <img
          :src="data?.brandImageUrl || brandLogo"
          class="brand-mark w-24 h-24 object-contain"
        />
      </div>
      <span class="ml-16">{{ data?.brandName || '品牌名称' }}</span>
    </div>

    <TopTable
      mode="good"
      class="mt-16 ml-8 mr-8"
      :data="data?.goodOpinions"
      @row-click="handleRowClick"
      @view-more="handleViewMore"
    ></TopTable>
    <TopTable
      mode="bad"
      class="mt-24 ml-8 mr-8"
      :data="data?.badOpinions"
      @row-click="handleRowClick"
      @view-more="handleViewMore"
    ></TopTable>
  </div>
</template>

<style lang="scss" scoped>
.dimension-item {
  // width: 334px;
  height: 741px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0px 1px 2px 0px rgba(10, 13, 18, 0.05);
  border-radius: 8px 8px 8px 8px;
  border: 1px solid #ebedf0;

  .di-header {
    width: 100%;
    height: 52px;
    background: #f2f4f7;
    border-radius: 8px 8px 0px 0px;
    border-bottom: 1px solid #dfe2e8;

    font-weight: 600;
    font-size: 16px;
    color: #1d252f;
    line-height: 28px;
    display: flex;
    align-items: center;
    justify-content: center;

    .dih-logo {
      width: 24px;
      height: 24px;
      // background: #ffffff;

      .brand-mark {
        border-radius: 5px;
      }
    }
  }
}
</style>
