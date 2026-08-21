<script setup lang="ts">

import HSortNum from '@h5/components/UI/HSortNum/index.vue'
import { defineProps, withDefaults, ref, computed } from 'vue'
import HBrandMentionTrend from '@h5/components/BrandComparison/HBrandMentionTrend.vue'
import { fmtPer, fmtFix } from '@/utils'

defineOptions({
  name: 'BrandComparison'
})

const props = withDefaults(defineProps<{
  data: any[]
}>(), {
  data: () => []
})

const emits = defineEmits<{
  /** 点击任一面包屑项时触发 */
  itemClick: [item: any]
}>()

// 计算排名，跳过市场均值
const getRank = (index: number) => {
  let rank = 1
  for (let i = 0; i < index; i++) {
    if (!props.data[i]?.isMarketAverage) {
      rank++
    }
  }
  return rank
}

const handleClick = (item: any) => {
  emits('itemClick', item)
}

</script>
<template>
  <div>
    <div class="header-layout flex-y-center">
      <div class="rank-item pl-10">排名</div>
      <div class="brand-item">品牌</div>
      <div class="negative-item pr-10">负面率</div>
      <div class="negative-mo-item">负面率环比</div>
    </div>
    <div class="content-layout">
      <div v-for="(item, index) in data" :key="index" >
        <div class="row-item flex-y-center" :class="{'average-row-item': item.isMarketAverage, 'self-row-item': item.isSelf}" @click="handleClick(item)">
          <template v-if="item.isMarketAverage">
            <div class="rank-item text-center">
              -
            </div>
            <div class="brand-item fs-14 fw-500 flex-y-center">
              <div class="ml-28">{{ item.brandName }}</div>
            </div>
          </template>
          <template v-else>
            <div class="rank-item flex-y-center">
              <HSortNum :rank="getRank(index)"></HSortNum>
              <div v-if="item.rankChange" class="ml-4 fw-500 fs-12 flex-y-center">
                <!-- 根据rankChange显示不同颜色和方向的实心三角形 -->
                <span
                  class="triangle-icon"
                  :class="{
                    'triangle-up-red': item.rankChange > 0,
                    'triangle-down-green': item.rankChange < 0
                  }"
                ></span>
                <span>{{ Math.abs(item.rankChange) }}</span>
              </div>
            </div>
            <div class="brand-item fs-14 fw-500 flex-y-center">
              <van-image
                width="24"
                height="24"
                :src="item.brandImg"
                fit="cover"
                radius="8"
              />
              <div class="ml-4">{{ item.brandName }}</div>
            </div>
          </template>
          <div class="negative-item fs-12 fw-500 text-primary flex-y-center">
            <HBrandMentionTrend
              style="width:34px;height: 20px;"
              class="mr-10"
              :trend-data="item.changeRate || []"
              :smooth="true"
              :show-symbol="false"/>
            <div class="negative-rate-text fs-14"  :style="{color: item.rateColor || ''}">{{ fmtPer(item.negativeRate) }}</div>
          </div>
          <div class="negative-mo-item color-grey fs-14 fw-500">{{ fmtFix(item.negativeRateMom) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>
<style scoped lang="scss">
.header-layout{
  height: 32px;
  background: #F5F7FA;
  border-radius: 0;
  border: 1px solid #EBEDF0;
  display: flex;
  align-items: center;
  font-weight: 400;
  font-size: 12px;
  color: #5F6A7A;
}
.row-item{
  height: 60px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #EBEDF0;
}
.rank-item{
  width: 54px;
}
.brand-item{
  //width: 80px;
  flex: 1;
}
.negative-item{
  //width: 80px;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}
.negative-rate-text{
  min-width: 55px;
}
.negative-mo-item{
  width: 80px;
  text-align: center;
}
.color-grey{
  color: #666666;
}
.average-row-item{
  background: #E5FAFE;
}
.self-row-item{
  background: #E2F3FE;
}
.text-center{
  text-align: center;
}

/* 实心三角形样式 */
.triangle-icon {
  display: inline-block;
  width: 0;
  height: 0;
  margin-right: 2px;
}

/* 红色向上三角形 */
.triangle-up-red {
  border-left: 4px solid transparent;
  border-right: 4px solid transparent;
  border-bottom: 6px solid #ff4d4f;
}

/* 绿色向下三角形 */
.triangle-down-green {
  border-left: 4px solid transparent;
  border-right: 4px solid transparent;
  border-top: 6px solid #52c41a;
}
</style>
