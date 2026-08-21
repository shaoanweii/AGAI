<script setup lang="ts">
import { ref } from 'vue'
import MetricSummaryCards from '@/components/Business/Scene/Common/MetricSummaryCards.vue'
import BrandTrend from './BrandTrend.vue'
import CarSeriesRank from './CarSeriesRank.vue'
import type { ProductBriefVo, BrandTrendVo, SeriesRankItemVo } from '@/api/groupAnalysis/types'
import { DEFAULT_MENTION_NEGATIVE_RATE_TYPE } from '@/constants'

/**
 * 综合分析
 */
defineOptions({
  name: 'ComprehensiveAnalysis'
})

// ==================== Props 定义 ====================

interface Props {
  // 数据简报相关
  productBriefData?: ProductBriefVo | null

  // 品牌趋势相关
  brandTrendData?: BrandTrendVo[] | null
  brandTrendDataType?: MentionNegativeRateType

  // 品牌车系排行相关
  brandSeriesRankData?: SeriesRankItemVo[]
  brandSeriesRankDataType?: 'brand' | 'series'
}

const props = withDefaults(defineProps<Props>(), {
  productBriefData: null,
  brandTrendData: null,
  brandTrendDataType: DEFAULT_MENTION_NEGATIVE_RATE_TYPE,
  brandSeriesRankData: () => [],
  brandSeriesRankDataType: 'brand'
})

// ==================== Events 定义 ====================

const emit = defineEmits<{
  'brand-trend-switch': [dataType: MentionNegativeRateType]
  'brand-series-rank-switch': [dataType: 'brand' | 'series']
  'brand-series-rank-sort': [sortField: string, sortOrder: 'asc' | 'desc']
  'brand-trend-click': [data: any]
  'car-series-row-click': [rowData: any]
  cardChange: [cardType: string]
}>()

// ==================== 事件处理 ====================

/**
 * 处理品牌趋势切换事件
 */
const handleBrandTrendSwitch = (dataType: MentionNegativeRateType) => {
  emit('brand-trend-switch', dataType)
}

/**
 * 处理品牌趋势图表点击事件
 */
const handleBrandTrendClick = (data: any) => {
  emit('brand-trend-click', data)
}

/**
 * 处理品牌车系排行切换事件
 */
const handleBrandSeriesRankSwitch = (dataType: 'brand' | 'series') => {
  emit('brand-series-rank-switch', dataType)
}

/**
 * 处理品牌车系排行排序事件
 */
const handleBrandSeriesRankSort = (sortField: string, sortOrder: 'asc' | 'desc') => {
  emit('brand-series-rank-sort', sortField, sortOrder)
}

/**
 * 处理车系排行行点击事件
 */
const handleCarSeriesRowClick = (rowData: any) => {
  emit('car-series-row-click', rowData)
}

const cardChange = (cardType: string) => {
  emit('cardChange', cardType)
}

// CarSeriesRank 组件引用
const carSeriesRankRef = ref<any>(null)

// 重置品牌车系排行排序状态
const resetBrandSeriesRankSort = () => {
  carSeriesRankRef.value?.resetSort()
}

// 暴露方法给父组件
defineExpose({
  resetBrandSeriesRankSort
})
</script>

<template>
  <div class="comprehensive-analysis flex-col" data-page-export-expand>
    <MetricSummaryCards
      :product-brief-data="props.productBriefData"
      @cardChange="cardChange"
    ></MetricSummaryCards>
    <div class="flex mt-24">
      <div class="left" data-page-export-expand>
        <BrandTrend
          :data="props.brandTrendData"
          :data-type="props.brandTrendDataType"
          @switch="handleBrandTrendSwitch"
          @chartClick="handleBrandTrendClick"
        ></BrandTrend>
      </div>
      <div class="right" data-page-export-expand>
        <CarSeriesRank
          ref="carSeriesRankRef"
          :data="props.brandSeriesRankData"
          :data-type="props.brandSeriesRankDataType"
          @switch="handleBrandSeriesRankSwitch"
          @sort="handleBrandSeriesRankSort"
          @row-click="handleCarSeriesRowClick"
        ></CarSeriesRank>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.comprehensive-analysis {
  display: flex;
  height: 739px;
  width: 100%;
  margin-top: 24px;
  min-width: 0; // 确保flex容器可以正确收缩
  overflow: hidden; // 防止内容溢出

  .left {
    flex: 1;
    height: 100%;
    margin-right: 24px;
    min-width: 0; // 确保flex子元素可以正确收缩
    overflow: hidden; // 防止左侧内容溢出
  }
  .right {
    width: 652px;
    flex: none;
    height: 100%;
    min-width: 400px; // 设置更合理的最小宽度
    max-width: 652px; // 设置最大宽度
    overflow: hidden; // 防止右侧内容溢出

    // 根据容器宽度动态调整右侧宽度
    // 当总宽度小于1600px时，右侧宽度为500px
    @container (max-width: 1600px) {
      width: 500px;
      min-width: 400px;
    }

    // 当总宽度小于1400px时，右侧宽度为450px
    @container (max-width: 1400px) {
      width: 450px;
      min-width: 400px;
    }

    // 当总宽度小于1200px时，右侧宽度为400px
    @container (max-width: 1200px) {
      width: 400px;
      min-width: 350px;
    }

    // 传统媒体查询作为后备方案
    @media (max-width: 1600px) {
      width: 500px;
      min-width: 400px;
    }

    @media (max-width: 1400px) {
      width: 450px;
      min-width: 400px;
    }

    @media (max-width: 1200px) {
      width: 400px;
      min-width: 350px;
    }

    @media (max-width: 1000px) {
      width: 350px;
      min-width: 300px;
    }
  }
}
</style>
