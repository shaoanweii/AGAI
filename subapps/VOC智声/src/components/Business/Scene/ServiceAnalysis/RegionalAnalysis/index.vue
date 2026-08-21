<script setup lang="ts">
import { computed, ref } from 'vue'
import ProvinceRank from './ProvinceRank.vue'
import DealerEvaluationTOPRank from './DealerEvaluationTOPRank.vue'
import MapChart from './MapChart.vue'
import type { ServiceProvinceRankVo, ServiceDealerRankVo } from '@/api/serviceAnalysis/types'

defineOptions({
  name: 'RegionalAnalysis'
})

// Props 定义
interface Props {
  provinceRankData?: ServiceProvinceRankVo[]
  provinceMapData?: ServiceProvinceRankVo[]
  dealerRankTopData?: ServiceDealerRankVo[]
  selectedProvinceCode?: string
}

const props = withDefaults(defineProps<Props>(), {
  provinceRankData: () => [],
  provinceMapData: () => [],
  dealerRankTopData: () => [],
  selectedProvinceCode: ''
})

// 定义emits
const emit = defineEmits<{
  'province-rank-sort': [{ prop: string; order: string }]
  'dealer-rank-top-sort': [{ prop: string; order: string }]
  'province-select': [provinceCode: string]
  'map-click': [data: any]
}>()

// 表格组件引用
const provinceRankRef = ref<InstanceType<typeof ProvinceRank>>()
const dealerRankTopRef = ref<InstanceType<typeof DealerEvaluationTOPRank>>()

// 清空所有表格的排序状态
const clearAllSort = () => {
  provinceRankRef.value?.clearSort()
  dealerRankTopRef.value?.clearSort()
}

// 暴露方法给父组件
defineExpose({
  clearAllSort
})

// 处理省份排行排序事件
const handleProvinceRankSort = ({ prop, order }: { prop: string; order: string }) => {
  emit('province-rank-sort', { prop, order })
}

// 处理经销商评价TOP排序事件
const handleDealerRankTopSort = ({ prop, order }: { prop: string; order: string }) => {
  emit('dealer-rank-top-sort', { prop, order })
}

// 处理省份选择事件
const handleProvinceSelect = (provinceCode: string) => {
  emit('province-select', provinceCode)
}

// 处理地图点击事件
const handleMapClick = (data: any) => {
  emit('map-click', data)
}

// 转换省份地图数据格式，适配 FMapChart 组件
const mapChartData = computed<any[]>(() => {
  return props.provinceMapData.map(item => ({
    ...item
    // name: item.provinceName,
    // value: item.negativeRate,
    // mom: item.negativeRateMoM,
    // yoy: item.negativeRateYoY,
    // mentions: item.mentions,
    // provinceCode: item.provinceCode
  }))
})
</script>

<template>
  <FCard :title="'区域分布'" :height="'739px'" class="f-card-border mt-24">
    <div class="region-analysis-scroll">
      <div class="region-analysis-content">
        <div class="region-analysis-map">
          <MapChart
            :data="mapChartData"
            width="100%"
            height="430px"
            @chart-click="handleMapClick"
          ></MapChart>
        </div>

        <div class="pl-83 pr-50">
          <FCard
            :title="'省份排行'"
            :width="'421px'"
            :height="'571px'"
            class="f-card-border flex-s-none province-rank-class"
          >
            <ProvinceRank
              ref="provinceRankRef"
              :province-rank-data="props.provinceRankData"
              :selected-province-code="props.selectedProvinceCode"
              @sort-change="handleProvinceRankSort"
              @province-select="handleProvinceSelect"
            ></ProvinceRank>
          </FCard>
        </div>

        <FCard
          :title="'经销商评价TOP'"
          :width="'626px'"
          :height="'630px'"
          class="f-card-border primary flex-s-none"
        >
          <DealerEvaluationTOPRank
            ref="dealerRankTopRef"
            :dealer-rank-top-data="props.dealerRankTopData"
            @sort-change="handleDealerRankTopSort"
          ></DealerEvaluationTOPRank>
        </FCard>
      </div>
    </div>
  </FCard>
</template>

<style lang="scss" scoped>
.region-analysis-scroll {
  width: 100%;
  height: 100%;
  overflow-x: hidden;
  overflow-y: hidden;
}

.region-analysis-content {
  width: 100%;
  min-width: 0;
  height: 100%;
  display: grid;
  align-items: center;
  grid-template-columns: 1fr 554px 626px;
}

.region-analysis-map {
  min-width: 0;
}

@media screen and (max-width: 1300px) {
  .region-analysis-scroll {
    overflow-x: auto;
    padding-bottom: 8px;
  }

  .region-analysis-content {
    width: max(100%, 1740px);
    min-width: 1740px;
    grid-template-columns: minmax(560px, 1fr) 554px 626px;
  }

  .region-analysis-map {
    min-width: 560px;
  }
}

.province-rank-class {
  position: relative;

  // 向右的实心三角形箭头，宽20px高10px
  &::before {
    content: '';
    display: inline-block;
    position: absolute;
    right: -10px;
    top: 50%;
    transform: translateY(-50%);
    width: 0;
    height: 0;
    border-top: 10px solid transparent;
    border-bottom: 10px solid transparent;
    border-left: 10px solid #1890ff;
  }
}
</style>
