<script setup lang="ts">
import HCard from '@h5/components/UI/HCard/index.vue'
import { fmtNum, fmtPer } from '@/utils'
import type { H5DataSquareDrillDownBrief } from '@h5/api/dataSquare'

defineOptions({
  name: 'CoreDataCard'
})

defineProps<{
  data?: Partial<H5DataSquareDrillDownBrief> | null
}>()
</script>

<template>
  <HCard title="核心数据">
    <div class="core-data-card">
      <div class="core-data-card__summary">
        <div class="core-data-card__summary-item">
          <div class="core-data-card__label">提及量</div>
          <div class="core-data-card__value-row">
            <span class="core-data-card__value">{{ fmtNum(data?.mentions || 0) }}</span>
            <span class="core-data-card__pill">
              {{ fmtPer(data?.mentionsMoM || 0) }}
            </span>
          </div>
        </div>
        <div class="core-data-card__summary-item">
          <div class="core-data-card__label">用户数</div>
          <div class="core-data-card__value-row">
            <span class="core-data-card__value">{{ fmtNum(data?.users || 0) }}</span>
            <span class="core-data-card__pill">
              {{ fmtPer(data?.usersMoM || 0) }}
            </span>
          </div>
        </div>
      </div>

      <div class="core-data-card__rate core-data-card__rate--negative">
        <div class="core-data-card__rate-left">
          <div class="core-data-card__rate-value">
            {{ fmtPer(data?.negativeRate || 0) }}
            <span class="core-data-card__pill">
              {{ fmtPer(data?.negativeRateMoM || 0) }}
            </span>
          </div>
          <div class="core-data-card__label">负面率</div>
        </div>
        <div class="core-data-card__rate-right">
          <div class="core-data-card__rate-count-wrap">
            <span class="core-data-card__label">负面提及量</span>
            <span class="core-data-card__rate-count core-data-card__rate-right-wrap">{{ fmtNum(data?.negativeMentions || 0) }}</span>
          </div>
          <div class="core-data-card__rate-mom">
            <span class="core-data-card__label">环比</span>
            <span class="core-data-card__rate-right-wrap">
              {{ fmtPer(data?.negativeMentionsMoM || 0) }}
            </span>
          </div>
        </div>
      </div>

      <div class="core-data-card__rate core-data-card__rate--positive">
        <div class="core-data-card__rate-left">
          <div class="core-data-card__rate-value">
            {{ fmtPer(data?.positiveRate || 0) }}
            <span class="core-data-card__pill">
              {{ fmtPer(data?.positiveRateMoM || 0) }}
            </span>
          </div>
          <div class="core-data-card__label">正面率</div>
        </div>
        <div class="core-data-card__rate-right">
          <div class="core-data-card__rate-count-wrap">
            <span class="core-data-card__label">正面提及量</span>
            <span class="core-data-card__rate-count core-data-card__rate-right-wrap">{{ fmtNum(data?.positiveMentions || 0) }}</span>
          </div>
          <div class="core-data-card__rate-mom">
            <span class="core-data-card__label">环比</span>
            <span class="core-data-card__rate-right-wrap">
              {{ fmtPer(data?.positiveMentionsMoM || 0) }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </HCard>
</template>

<style scoped lang="scss">
.core-data-card {
  display: grid;
  gap: 12px;
  margin-top: 10px;

  &__summary {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 12px;
  }

  &__summary-item {
    min-width: 0;
    height: 82px;
    padding: 16px;
    border-radius: 8px;
    background: #F2F4F7;
    border: 1px solid #EBEDF0;
  }

  &__label {
    font-weight: 400;
    font-size: 12px;
    color: #5F6A7A;
    line-height: 14px;
    white-space: nowrap;
  }

  &__value-row {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-top: 8px;
    min-width: 0;
  }

  &__value {
    font-weight: 500;
    font-size: 14px;
    color: #1F2733;
    line-height: 28px;
  }

  &__pill {
    display: inline-flex;
    align-items: center;
    height: 22px;
    padding: 2px 8px 2px 6px;
    background: #FAFAFA;
    border-radius: 16px;
    border: 1px solid #E9EAEB;
    font-weight: 500;
    font-size: 12px;
    color: #414651;
    line-height: 18px;
    white-space: nowrap;
  }

  &__rate {
    display: flex;
    justify-content: space-between;
    min-height: 72px;
    padding: 12px;
    border-radius: 8px;
  }

  &__rate--negative {
    background: #FEFAE0;
    border: 1px solid #EBEDF0;

    .core-data-card__rate-value {
      font-weight: 500;
      font-size: 16px;
      color: #FAB007;
    }
  }

  &__rate--positive {
    background: #E5FEEB;
    border: 1px solid #EBEDF0;
  }

  &__rate-left,
  &__rate-right {
    display: grid;
    align-content: center;
    gap: 8px;
  }

  &__rate-count-wrap {
   line-height: 28px;
  }

  &__rate-left {
    min-width: 92px;
  }

  &__rate-right {
    text-align: right;
  }

  &__rate-value {
    display: flex;
    align-items: center;
    gap: 8px;
    line-height: 28px;
    font-weight: 500;
    font-size: 16px;
    color: #1F2733;
  }

  &__rate-count {
    margin-left: 6px;
    font-weight: 500;
    font-size: 14px;
    color: #1F2733;
  }

  &__rate-right-wrap {
    display: inline-block;
    min-width: 64px;
    text-align: right;
  }

  &__rate-mom {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
  }
}
</style>
