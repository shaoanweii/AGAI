<script setup lang="ts">
/**
 * 单张指标卡片的数据结构。
 * 使用明确的展示字段，避免模板内出现隐式拼装逻辑。
 */
interface EventStatisticsMetricItem {
  label: string
  value: string
  tone: 'negative' | 'positive' | 'neutral'
  iconName: string
}

defineOptions({
  name: 'EventStatisticsMetricCards'
})

const props = defineProps<{
  items: EventStatisticsMetricItem[]
}>()

/**
 * 卡片主题样式映射。
 * 按负面/正面/中性拆分背景与数值色，便于后续真实数据接入时复用。
 */
const toneClassMap: Record<EventStatisticsMetricItem['tone'], string> = {
  negative: 'event-statistics-metric-cards__item--negative',
  positive: 'event-statistics-metric-cards__item--positive',
  neutral: 'event-statistics-metric-cards__item--neutral'
}
</script>

<template>
  <div class="event-statistics-metric-cards">
    <div
      v-for="item in props.items"
      :key="item.label"
      class="event-statistics-metric-cards__item"
      :class="toneClassMap[item.tone]"
    >
      <div class="event-statistics-metric-cards__icon">
        <SvgIcon
          :name="item.iconName"
          width="32px"
          height="32px"
          :color="item.tone === 'neutral' ? '#5F6A7A' : undefined"
        />
      </div>

      <div class="event-statistics-metric-cards__content">
        <div class="event-statistics-metric-cards__label">{{ item.label }}</div>
        <div class="event-statistics-metric-cards__value-row">
          <div class="event-statistics-metric-cards__value">{{ item.value }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.event-statistics-metric-cards {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 24px;
}

.event-statistics-metric-cards__item {
  display: flex;
  align-items: center;
  min-width: 0;
  padding: 16px 20px;
  border: 1px solid #ebedf0;
  border-radius: 12px;
  box-shadow: 0px 1px 2px 0px rgba(10, 13, 18, 0.05);
}

.event-statistics-metric-cards__item--negative {
  background: linear-gradient(90deg, rgba(255, 237, 196, 0.8) 0%, rgba(255, 250, 238, 0.95) 100%);

  .event-statistics-metric-cards__value {
    color: #f59e0b;
  }
}

.event-statistics-metric-cards__item--positive {
  background: linear-gradient(90deg, rgba(223, 247, 239, 0.85) 0%, rgba(243, 252, 248, 0.95) 100%);

  .event-statistics-metric-cards__value {
    color: #1f2937;
  }
}

.event-statistics-metric-cards__item--neutral {
  background: linear-gradient(90deg, rgba(245, 247, 250, 0.95) 0%, rgba(250, 251, 252, 0.95) 100%);

  .event-statistics-metric-cards__value {
    color: #1f2733;
  }
}

.event-statistics-metric-cards__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: none;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: inset 0 0 0 1px rgba(235, 237, 240, 0.9);
}

.event-statistics-metric-cards__content {
  flex: 1;
  min-width: 0;
  margin-left: 16px;
}

.event-statistics-metric-cards__label {
  font-size: 14px;
  line-height: 22px;
  color: #5f6a7a;
}

.event-statistics-metric-cards__value-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
  min-width: 0;
}

.event-statistics-metric-cards__value {
  font-weight: 600;
  font-size: 20px;
  line-height: 28px;
  white-space: nowrap;
}

@media (max-width: 1360px) {
  .event-statistics-metric-cards {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .event-statistics-metric-cards {
    grid-template-columns: minmax(0, 1fr);
    gap: 16px;
  }

  .event-statistics-metric-cards__item {
    padding: 14px 16px;
  }

  .event-statistics-metric-cards__value {
    font-size: 18px;
    line-height: 26px;
  }
}
</style>
