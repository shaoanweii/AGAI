<script setup lang="ts">
import { ref } from 'vue'

// 定义周期类型
type PeriodType = 'preheat' | 'launch' | 'stable'
interface Props {
  periodData: {
    preheat: {
      name: string
      color: string
      borderColor: string
      titleColor: string
      data: Array<{
        brand: string
        series: string
        imgUrl: string
        positiveRate: string
        negativeRate: string
        mentionCount: string
      }>
    }
    launch: {
      name: string
      color: string
      borderColor: string
      titleColor: string
      data: Array<{
        brand: string
        series: string
        imgUrl: string
        positiveRate: string
        negativeRate: string
        mentionCount: string
      }>
    }
    stable: {
      name: string
      color: string
      borderColor: string
      titleColor: string
      data: Array<{
        brand: string
        series: string
        imgUrl: string
        positiveRate: string
        negativeRate: string
        mentionCount: string
      }>
    }
  }
  queryParams?: Record<string, any>
}

const props = withDefaults(defineProps<Props>(), {
  queryParams: () => ({})
})

// 定义周期数组
const periodTypes: PeriodType[] = ['preheat', 'launch', 'stable']

const emit = defineEmits<{
  (e: 'card-click', period: any, car: any, index: number): void
}>()

const handleCardClick = (period: any, car: any, index: number) => {
  // index =0 为新品车系 index =1 为对比车系
  emit('card-click', period, car, index)
}
</script>

<template>
  <div class="periods-container">
    <!-- 预热期 上市期 稳定期-->
    <div
      class="period-card"
      v-for="(threeType, threeIndex) in periodTypes"
      :key="threeType + threeIndex"
    >
      <div
        class="period-title"
        :style="{
          backgroundColor: periodData[threeType].color,
          color: periodData[threeType].titleColor
        }"
      >
        {{ periodData[threeType].name }}
      </div>
      <div class="period-content">
        <div
          v-if="periodData[threeType].data && periodData[threeType].data.length > 0"
          class="data-grid"
        >
          <!-- 品牌信息行 -->
          <div class="brands-row">
            <div class="empty-cell"></div>
            <div
              v-for="(item, index) in periodData[threeType].data"
              :key="index"
              class="brand-item cursor-pointer"
              @click="handleCardClick(threeType, item, index)"
            >
              <img v-if="item.imgUrl" :src="item.imgUrl" alt="" class="car-logo" />
              <div class="car-info">
                <div class="car-brand">{{ item.brand }}</div>
                <div class="car-series">{{ item.series }}</div>
              </div>
            </div>
          </div>
          <!-- 数据行 -->
          <div class="data-row">
            <div class="stat-label">正面率</div>
            <div class="data-values">
              <div
                v-for="(item, index) in periodData[threeType].data"
                :key="index"
                class="data-cell positive cursor-pointer"
                @click="handleCardClick(threeType, item, index)"
              >
                {{ item.positiveRate }}
              </div>
            </div>
          </div>
          <div class="data-row">
            <div class="stat-label">负面率</div>
            <div class="data-values">
              <div
                v-for="(item, index) in periodData[threeType].data"
                :key="index"
                class="data-cell negative cursor-pointer"
                @click="handleCardClick(threeType, item, index)"
              >
                {{ item.negativeRate }}
              </div>
            </div>
          </div>
          <div class="data-row">
            <div class="stat-label">提及量</div>
            <div class="data-values">
              <div
                v-for="(item, index) in periodData[threeType].data"
                :key="index"
                class="data-cell cursor-pointer"
                @click="handleCardClick(threeType, item, index)"
              >
                {{ item.mentionCount }}
              </div>
            </div>
          </div>
        </div>
        <div v-else class="no-data">暂无数据</div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.periods-container {
  display: flex;
  gap: 20px;

  .period-card {
    flex: 1;
    padding: 0;
    border-radius: 12px;
    border: 1px solid #e8e8e8;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    overflow: hidden;

    .period-title {
      font-size: 18px;
      font-weight: 600;
      padding: 14px 24px;
      text-align: center;
      color: #1f2733;
    }

    .period-content {
      padding: 24px;

      .data-grid {
        width: 100%;
        position: relative;

        // 品牌信息行
        .brands-row {
          display: flex;
          margin-bottom: 16px;

          .empty-cell {
            width: 60px;
          }

          .brand-item {
            flex: 1;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 16px;
            margin: 0 8px;
            border-radius: 8px;
            transition: all 0.3s ease;

            &:hover {
              background-color: rgba(22, 125, 255, 0.05);
            }

            .car-logo {
              width: 48px;
              height: 48px;
              border-radius: 8px;
              margin-right: 12px;
              border: 1px solid rgba(0, 0, 0, 0.1);
              background-color: #fafafa;
              display: flex;
              align-items: center;
              justify-content: center;
            }

            .car-info {
              flex: 1;
              min-width: 0;
              text-align: center;

              .car-brand {
                font-size: 16px;
                font-weight: 600;
                color: #1f2733;
                margin-bottom: 4px;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
              }
              .car-series {
                font-size: 13px;
                color: #666;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
              }
            }
          }
        }

        // 数据行
        .data-row {
          display: flex;
          margin-bottom: 12px;

          &:last-child {
            margin-bottom: 0;
          }

          .stat-label {
            width: 60px;
            display: flex;
            align-items: center;
            font-size: 14px;
            color: #666;
            font-weight: 500;
          }

          .data-values {
            flex: 1;
            display: flex;

            .data-cell {
              flex: 1;
              display: flex;
              align-items: center;
              justify-content: center;
              font-size: 18px;
              font-weight: 600;
              padding: 12px 16px;
              margin: 0 8px;
              border-radius: 8px;
              transition: all 0.3s ease;

              &:hover {
                background-color: rgba(22, 125, 255, 0.05);
              }

              &.positive {
                color: #52c41a;
              }

              &.negative {
                color: #1677ff;
              }
            }
          }
        }
      }
    }
  }
}

// 通用样式
.cursor-pointer {
  cursor: pointer;
}

// 暂无数据样式
.no-data {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
  font-size: 16px;
  color: #999;
  background-color: #fafafa;
  border-radius: 8px;
}

// 响应式设计
@media screen and (max-width: 1200px) {
  .periods-container {
    flex-direction: column;

    .period-card {
      margin-bottom: 20px;

      &:last-child {
        margin-bottom: 0;
      }
    }
  }
}
</style>
