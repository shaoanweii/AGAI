<script setup lang="ts">
import type { ComparativeBriefVo } from '@/api/competitorAnalysis/types'
import { MarketAverage } from '../constants'
import { fmtNum, fmtPer, fmtFix } from '@/utils'
import HoverPopover from '../../Common/HoverPopover.vue'

defineOptions({
  name: 'CCCard'
})

const { comparativeBriefData } = defineProps<{
  comparativeBriefData: ComparativeBriefVo[] | undefined
}>()

// 定义事件
const emit = defineEmits<{
  (e: 'cardClick', data: ComparativeBriefVo): void
}>()

// 处理卡片点击事件
const handleCardClick = (item: ComparativeBriefVo) => {
  emit('cardClick', item)
}
</script>

<template>
  <div class="cc-card">
    <HoverPopover
      v-for="(item, index) in comparativeBriefData"
      :key="index"
      placement="top"
      :show-after="200"
      :width="410"
      trigger="hover"
      :table-config="{
        title: item.name,
        data: [
          {
            label: '负面率',
            value: fmtPer(item.negativeRate),
            rateMoM: fmtFix(item.negativeRateMoM),
            rateYoY: fmtFix(item.negativeRateYoY)
          },
          {
            label: '提及量',
            value: fmtNum(item.mentions),
            rateMoM: fmtFix(item.mentionsMoM),
            rateYoY: fmtFix(item.mentionsYoY)
          }
        ],
        columns: [
          { title: '名称', dataIndex: 'label', width: 70 },
          { title: '数值', dataIndex: 'value', width: 80 },
          { title: '环比', dataIndex: 'rateMoM', className: 'c666' },
          { title: '同比', dataIndex: 'rateYoY', className: 'c666' }
        ]
      }"
    >
      <template #reference>
        <div
          class="ccc-item"
          :style="{
            backgroundColor: item.rateBackgroundColor,
            cursor: item.name === MarketAverage ? 'default' : 'pointer'
          }"
          @click="handleCardClick(item)"
        >
          <div class="ccci-title">
            <div v-if="item.name !== MarketAverage" class="logo mr-8">
              <img v-if="item.imgUrl" :src="item.imgUrl" alt="" class="w-32 h-32 object-contain" />
            </div>
            <span>{{ item.name }}</span>
          </div>

          <div class="info-group">
            <div class="value" :style="{ color: item.rateColor }">
              {{ fmtPer(item.negativeRate) }}
            </div>
            <div class="label">负面率</div>
          </div>
          <div class="info-group">
            <div class="value">{{ fmtNum(item.mentions) }}</div>
            <div class="label">提及量</div>
          </div>
        </div>
      </template>
    </HoverPopover>
  </div>
</template>

<style lang="scss" scoped>
.cc-card {
  display: flex;
  gap: 40px;

  .ccc-item {
    flex: 1;
    // width: 551px;
    height: 164px;
    background: #eaf3ff;
    border-radius: 8px 8px 8px 8px;
    border: 1px solid #ebedf0;
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    padding: 0 40px;
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      transform: translateY(-2px);
    }

    &.start {
      background: #e5fafe;
    }

    .ccci-title {
      height: 48px;
      font-weight: 600;
      font-size: 24px;
      color: #1f2733;
      border-right: 1px solid #dfe2e8;
      display: flex;
      justify-content: center;
      align-items: center;
      padding-right: 24px;

      .logo {
        width: 48px;
        height: 48px;
        // background: #ffffff;
        border-radius: 50%;
        overflow: hidden;
        display: flex;
        justify-content: center;
        align-items: center;
      }
    }

    .info-group {
      margin-left: 40px;
      .value {
        font-weight: 500;
        font-size: 20px;
        color: #1f2733;
        line-height: 28px;

        &.wraning {
          color: #e5484d;
        }
      }
      .label {
        font-weight: 400;
        font-size: 14px;
        color: #5f6a7a;
        line-height: 14px;
        margin-top: 6px;
      }
    }
  }
}
</style>
