<script setup lang="ts">
import { computed } from 'vue'
import type { NegativeRateCardProps, NegativeRateCardEmits, NegativeRateData } from './types'
import { fmtNum, fmtPer, fmtFix } from '@/utils'

// 定义组件选项
defineOptions({
  name: 'NegativeRateCard'
})

// 定义Props
const props = withDefaults(defineProps<NegativeRateCardProps>(), {
  loading: false,
})

// 定义Emits
const emit = defineEmits<NegativeRateCardEmits>()

// 默认数据
const defaultData: NegativeRateData = {
  name: '',
  negativeRate: 0,
  negativeRateMom: 0,
  mentionCount: 0,
  mentionCountMom: 0,
  achieveRate: 0,
  achieveRateTalk: ''
}

// 计算属性
const cardData = computed(() => props.dataBrief || defaultData)

/**
 * 处理卡片点击
 */
const handleCardClick = () => {
  if (props.loading) return
  emit('click', cardData.value)
}

/**
 * 处理查看详情点击
 */
const handleViewDetails = (event: Event) => {
  event.stopPropagation() // 阻止事件冒泡
  if (props.loading) return
  emit('view-details', cardData.value)
}
</script>

<template>
  <div
    class="negative-rate-card flex-center pl-12 pr-12"
    :class="{
      'is-loading': loading
    }"
    @click="handleCardClick"
  >
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <van-loading size="24px" color="#1677FF">加载中...</van-loading>
    </div>

    <!-- 正常状态 -->
    <div v-else class="card-content flex-1 flex-between flex-y-center">
      <div>
        <div class="flex-y-center">
          <span class="fs-20 fw-500">{{ fmtPer(cardData.negativeRate) }}</span>
          <span class="change-rate ml-6">环比 {{ fmtFix(cardData.negativeRateMom) }}</span>
        </div>
        <div class="fs-12 fw-400 mt-7">
          <span>{{name}}负面率</span>
        </div>
      </div>
      <div class="mt-10">
        <div class="flex-y-center">
          <div class="fs-12 fw-400 text-right w-40 title-text-class">提及量</div>
          <div class="flex-1 ml-6 text-right right-data-class">{{ fmtNum(cardData.mentionCount) }}</div>
        </div>
        <!-- 标题区域 -->
        <div class="flex-y-center mt-8">
          <div class="fs-12 fw-400 text-right w-40 title-text-class">环比</div>
          <div class="ml-6 text-right right-data-class">{{ fmtFix(cardData.mentionCountMom) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.negative-rate-card {
  height: 73px;
  background: linear-gradient( 180deg, #0DAEFF 0%, #1677FF 100%);
  border-radius: 0px 20px 0px 8px;
  color: #fff;
  .change-rate{
    min-width: 55px;
    height: 22px;
    padding: 0 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #FAFAFA;
    border-radius: 16px 16px 16px 16px;
    border: 1px solid #E9EAEB;
    font-weight: 500;
    font-size: 10px;
    color: #5F6A7A;
  }
  .w-40{
    width: 40px;
  }
  .right-data-class{
    min-width: 64px;
    color: rgba(255,255,255,0.85);
  }
  .title-text-class{
    color: rgba(255,255,255,0.65);
  }
  .text-right{
    text-align: right;
  }
}
</style>
