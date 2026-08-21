<script setup lang="ts">
import { ref } from 'vue'
import type { ProductBriefVo, ProductTrendVo } from '@/api/productAnalysis/types'
import type { ProductSelfBriefVo, ProductSelfTrendVo } from '@/api/thisProductAnalysis/types.d'
import ZhCards from './ZhCards.vue'
import ZhYsCards from './ZhYsCards.vue'
import DataYsTrend from './DataYsTrend.vue'
import DataTrend from '@/components/Business/Scene/Common/DataTrend.vue'
import useMiddlewareStore from '@/store/modules/middleware'
import { OriginalDataType } from '@/constants'

/**
 * 综合分析
 */
defineOptions({
  name: 'ZhAnalysis'
})

// 通用简报数据类型 - 兼容产品分析和本品分析
type BriefData = ProductBriefVo | ProductSelfBriefVo

// 通用趋势数据类型 - 兼容产品分析和本品分析
type TrendData = ProductTrendVo | ProductSelfTrendVo

// Props 定义
interface Props {
  productBriefData?: BriefData | null
  dataTrendChangeData?: TrendData | null
}

const props = withDefaults(defineProps<Props>(), {
  productBriefData: null,
  dataTrendChangeData: null
})

// 定义emits
const emit = defineEmits<{
  cardChange: [data: any]
  'chart-click': [data: any]
}>()

const middlewareStore = useMiddlewareStore()

// FollowSceneTOP 组件引用
// const followSceneTopRef = ref<InstanceType<typeof FollowSceneTOP>>()

// 清空关注场景TOP表格的排序状态
// const clearSceneTopSort = () => {
//   followSceneTopRef.value?.clearSort()
// }

// 暴露方法给父组件
defineExpose({
  // clearSceneTopSort
})
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
  <div class="main-hot-container flex-col">
    <ZhCards
      v-if="middlewareStore.originalDataType === OriginalDataType.ResultData"
      :product-brief-data="productBriefData"
      @cardChange="handleCardChange"
    ></ZhCards>
    <ZhYsCards
      v-if="middlewareStore.originalDataType === OriginalDataType.OriginalData"
      :product-brief-data="productBriefData"
      @cardChange="handleCardChange"
    ></ZhYsCards>
    <div class="flex mt-24">
      <div class="box">
        <DataTrend
          v-if="middlewareStore.originalDataType === OriginalDataType.ResultData"
          :data-trend-change-data="props.dataTrendChangeData"
          @chart-click="handleChartClick"
        ></DataTrend>
        <DataYsTrend
          v-if="middlewareStore.originalDataType === OriginalDataType.OriginalData"
          :data="props.dataTrendChangeData"
          @chart-click="handleChartClick"
        ></DataYsTrend>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.main-hot-container {
  display: flex;
  height: 739px;
  width: 100%;
  margin-top: 24px;
  min-width: 0; // 确保flex容器可以正确收缩
  overflow: hidden; // 防止内容溢出

  .box {
    width: 100%;
  }
}
</style>
