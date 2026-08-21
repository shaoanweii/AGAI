<script setup lang="ts">
import type { TrendSummaryCardItem } from './types'

defineOptions({
  name: 'TrendSummaryCards'
})

interface Props {
  cards?: TrendSummaryCardItem[]
}

const props = withDefaults(defineProps<Props>(), {
  cards: () => []
})

/**
 * 双指标卡片需要在桌面端横向并排展示。
 * 通过独立判断函数保持模板语义清晰，避免直接在模板里写复杂表达式。
 */
const isDualMetricsCard = (card: TrendSummaryCardItem) => card.metrics.length > 1
</script>

<template>
  <div class="trend-summary-cards">
    <div
      v-for="card in props.cards"
      :key="card.key"
      class="summary-card"
      :class="card.customClass"
    >
      <div class="summary-card__icon">
        <SvgIcon :name="card.icon" width="32px" height="32px" :color="card.iconColor" />
      </div>
      <div
        class="summary-card__content"
        :class="{ 'summary-card__content--dual': isDualMetricsCard(card) }"
      >
        <div
          v-for="metric in card.metrics"
          :key="`${card.key}-${metric.label}`"
          class="summary-card__metric"
          :class="{ 'summary-card__metric--dual': isDualMetricsCard(card) }"
        >
          <div class="summary-card__label">{{ metric.label }}</div>
          <div class="summary-card__value-row">
            <div class="summary-card__value" :class="metric.valueClassName">
              {{ metric.value }}
            </div>
            <div class="summary-card__tag">{{ metric.tag }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.trend-summary-cards {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1.35fr) minmax(0, 1.35fr) minmax(0, 1fr);
  gap: 24px;

  @media (max-width: 1440px) {
    gap: 16px;
  }

  @media (max-width: 1200px) {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

.summary-card {
  min-width: 0;
  min-height: 88px;
  padding: 16px;
  display: flex;
  align-items: center;
  border-radius: 8px;
  border: 1px solid #ebedf0;
  background: #f2f4f7;
  box-sizing: border-box;

  &--neutral {
    background: #f5f7fa;
  }

  &--negative {
    background: linear-gradient(90deg, rgba(255, 247, 215, 0.92) 0%, rgba(255, 248, 226, 0.96) 100%);
  }

  &--positive {
    background: linear-gradient(90deg, rgba(237, 252, 248, 0.96) 0%, rgba(238, 250, 249, 0.96) 100%);
  }

  &__icon {
    width: 56px;
    height: 56px;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    background: #ffffff;
  }

  &__content {
    flex: 1;
    min-width: 0;
    margin-left: 16px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    gap: 8px;

    &--dual {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 24px;
      align-items: center;
    }
  }

  &__metric {
    min-width: 0;

    &--dual {
      display: flex;
      flex-direction: column;
      justify-content: center;
    }
  }

  &__label {
    font-size: 14px;
    line-height: 20px;
    color: #667085;
  }

  &__value-row {
    margin-top: 4px;
    display: flex;
    align-items: center;
    gap: 8px;
    min-width: 0;
  }

  &__value {
    min-width: 0;
    white-space: nowrap;
    font-weight: 500;
    font-size: 20px;
    line-height: 28px;
    color: #1f2733;

    &--negative {
      color: #fab007;
    }
  }

  &__tag {
    flex-shrink: 0;
    padding: 2px 6px;
    border-radius: 16px;
    border: 1px solid #e9eaeb;
    background: #fafafa;
    color: #414651;
    font-size: 12px;
    line-height: 18px;
    white-space: nowrap;
  }

  @media (max-width: 1440px) {
    padding: 14px;

    &__content {
      margin-left: 12px;

      &--dual {
        gap: 16px;
      }
    }

    &__value {
      font-size: 18px;
      line-height: 24px;
    }
  }

  @media (max-width: 900px) {
    &__icon {
      width: 48px;
      height: 48px;
    }

    &__label {
      font-size: 12px;
      line-height: 18px;
    }

    &__value {
      font-size: 16px;
      line-height: 22px;
    }
  }

  @media (max-width: 640px) {
    &__content {
      &--dual {
        grid-template-columns: minmax(0, 1fr);
        gap: 8px;
      }
    }
  }
}
</style>
