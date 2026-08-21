<script setup lang="ts">
import type { H5DataSquareCategoryItem, H5DataSquareReportItem } from '@h5/api/dataSquare'
import DataSquareReportRow from './DataSquareReportRow.vue'
import defaultIcon from '@/assets/images/system/dataSquare/default.png'

defineOptions({
  name: 'DataSquareCategoryCard'
})

const props = defineProps<{
  category: H5DataSquareCategoryItem
}>()

const emit = defineEmits<{
  more: [category: H5DataSquareCategoryItem]
  'report-click': [report: H5DataSquareReportItem]
}>()

const getReportList = () => {
  return Array.isArray(props.category.reports) ? props.category.reports : []
}

/**
 * 打开当前分类详情。
 */
const handleMoreClick = () => {
  emit('more', props.category)
}
</script>

<template>
  <section class="category-card">
    <header class="category-card__header">
      <div class="category-card__title-wrap">
        <span class="category-card__icon">
          <img :src="category.listIconURL || defaultIcon" alt="" />
        </span>
        <span class="category-card__title">{{ category.categoryName || '' }}</span>
      </div>
      <button
        v-if="category.hasMore"
        class="category-card__more"
        type="button"
        @click="handleMoreClick"
      >
        <span>查看更多</span>
        <van-icon name="arrow" />
      </button>
    </header>

    <div class="category-card__list">
      <DataSquareReportRow
        v-for="report in getReportList()"
        :key="report.reportId"
        :report="report"
        @click="emit('report-click', report)"
      />
      <van-empty
        v-if="getReportList().length === 0"
        image-size="72"
        description="暂无报告"
        class="category-card__empty"
      />
    </div>
  </section>
</template>

<style scoped lang="scss">
.category-card {
  padding: 12px;
  border-radius: 8px;
  background: #ffffff;

  &__header {
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;
  }

  &__title-wrap {
    min-width: 0;
    display: flex;
    align-items: center;
  }

  &__icon {
    width: 20px;
    height: 20px;
    margin-right: 8px;
    border-radius: 4px;
    color: #ffffff;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    overflow: hidden;
    font-size: 14px;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }
  }

  &__title {
    min-width: 0;
    font-weight: 500;
    font-size: 14px;
    color: #1F2733;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__more {
    height: 22px;
    padding: 0;
    border: 0;
    background: transparent;
    color: #929aa6;
    display: inline-flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
    line-height: 18px;
    flex-shrink: 0;
  }

  &__list {
    display: grid;
    gap: 16px;
  }

  &__empty {
    padding: 8px 0;
  }
}
</style>
