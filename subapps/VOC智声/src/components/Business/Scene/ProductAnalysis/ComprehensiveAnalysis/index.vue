<script setup lang="ts">
import type { ProductBriefVo, SceneTopVo, ProductTrendVo } from '@/api/productAnalysis/types'
import MetricSummaryCards from '@/components/Business/Scene/Common/MetricSummaryCards.vue'
import DataTrend from '@/components/Business/Scene/Common/DataTrend.vue'
import FollowSceneTOP from '@/components/Business/Scene/Common/FollowSceneTOP.vue'

/**
 * 综合分析
 */
defineOptions({
  name: 'ComprehensiveAnalysis'
})

// Props 定义
interface Props {
  productBriefData?: ProductBriefVo | null
  focusSceneTopData?: SceneTopVo[]
  dataTrendChangeData?: ProductTrendVo | null
}

const props = withDefaults(defineProps<Props>(), {
  productBriefData: null,
  focusSceneTopData: () => [],
  dataTrendChangeData: null
})
</script>

<template>
  <div class="comprehensive-analysis" data-page-export-expand>
    <div class="left" data-page-export-expand>
      <MetricSummaryCards :product-brief-data="productBriefData"></MetricSummaryCards>
      <DataTrend class="mt-24" :data-trend-change-data="props.dataTrendChangeData"></DataTrend>
    </div>
    <div class="right" data-page-export-expand>
      <FollowSceneTOP :focus-scene-top-data="focusSceneTopData"></FollowSceneTOP>
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
