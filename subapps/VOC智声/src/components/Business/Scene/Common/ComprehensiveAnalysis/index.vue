<script setup lang="ts">
import { ref } from 'vue'
import type { ProductBriefVo, SceneTopVo, ProductTrendVo } from '@/api/productAnalysis/types'
import type {
  ProductSelfBriefVo,
  ProductSelfSceneTopVo,
  ProductSelfTrendVo
} from '@/api/thisProductAnalysis/types.d'
import MetricSummaryCards from '@/components/Business/Scene/Common/MetricSummaryCards.vue'
import DataTrend from '@/components/Business/Scene/Common/DataTrend.vue'
import FollowSceneTOP from '@/components/Business/Scene/Common/FollowSceneTOP.vue'

/**
 * 综合分析
 */
defineOptions({
  name: 'ComprehensiveAnalysis'
})

// 通用简报数据类型 - 兼容产品分析和本品分析
type BriefData = ProductBriefVo | ProductSelfBriefVo

// 通用场景TOP数据类型 - 兼容产品分析和本品分析
type SceneTopData = SceneTopVo | ProductSelfSceneTopVo

// 通用趋势数据类型 - 兼容产品分析和本品分析
type TrendData = ProductTrendVo | ProductSelfTrendVo

// Props 定义
interface Props {
  productBriefData?: BriefData | null
  focusSceneTopData?: SceneTopData[]
  dataTrendChangeData?: TrendData | null
}

const props = withDefaults(defineProps<Props>(), {
  productBriefData: null,
  focusSceneTopData: () => [],
  dataTrendChangeData: null
})

// 定义emits
const emit = defineEmits<{
  'scene-top-sort': [{ prop: string; order: string }]
  'scene-row-click': [data: any]
  'scene-view-more': []
  cardChange: [data: any]
  'chart-click': [data: any]
}>()

// FollowSceneTOP 组件引用
const followSceneTopRef = ref<InstanceType<typeof FollowSceneTOP>>()

// 清空关注场景TOP表格的排序状态
const clearSceneTopSort = () => {
  followSceneTopRef.value?.clearSort()
}

// 暴露方法给父组件
defineExpose({
  clearSceneTopSort
})

// 处理关注场景TOP排序事件
const handleSceneTopSort = ({ prop, order }: { prop: string; order: string }) => {
  emit('scene-top-sort', { prop, order })
}

// 处理场景行点击事件
const handleSceneRowClick = (data: any) => {
  emit('scene-row-click', data)
}

// 处理查看更多点击事件
const handleSceneViewMore = () => {
  emit('scene-view-more')
}

// 处理卡片点击事件
const handleCardChange = (data: any) => {
  emit('cardChange', data)
}

// 处理图表点击事件
const handleChartClick = (data: any) => {
  emit('chart-click', data)
}
</script>

<template>
  <div class="comprehensive-analysis flex-col" data-page-export-expand>
    <MetricSummaryCards
      :product-brief-data="productBriefData"
      @cardChange="handleCardChange"
    ></MetricSummaryCards>
    <div class="flex mt-24">
      <div class="left" data-page-export-expand>
        <DataTrend
          :data-trend-change-data="props.dataTrendChangeData"
          @chart-click="handleChartClick"
        ></DataTrend>
      </div>
      <div class="right" data-page-export-expand>
        <FollowSceneTOP
          ref="followSceneTopRef"
          :focus-scene-top-data="focusSceneTopData"
          @sort-change="handleSceneTopSort"
          @row-click="handleSceneRowClick"
          @view-more="handleSceneViewMore"
        ></FollowSceneTOP>
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
