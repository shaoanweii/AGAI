<script setup lang="ts">
import HSortNum from '@h5/components/UI/HSortNum/index.vue'
import type { SeriesRankItemVo } from '@h5/api/rootCauseAnalysis/types'
import { fmtNum, fmtPer } from '@/utils'

defineOptions({
  name: 'CarSeriesRank'
})

interface Props {
  data?: SeriesRankItemVo[]
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (e: 'row-click', data: SeriesRankItemVo): void
}>()

const handleRowClick = (item: SeriesRankItemVo) => {
  emit('row-click', item)
}
</script>

<template>
  <div class="table-container">
    <div class="table-header">
      <div class="header-item">排名</div>
      <div class="header-item">车系</div>
      <div class="header-item">提及量</div>
      <div class="header-item">负面率</div>
    </div>
    <div class="table-body">
      <div
        class="table-row"
        v-for="(item, index) in props.data"
        :key="item.code || index"
        @click="handleRowClick(item)"
      >
        <div class="row-item flex-x-center">
          <HSortNum :rank="index + 1"></HSortNum>
        </div>
        <div class="row-item row-car">
          <template v-if="item.imageUrl">
            <img :src="item.imageUrl" :alt="item.name" class="car-img" />
          </template>
          <template v-else>
            <img src="@/assets/images/group-car.png" :alt="item.name" class="car-img" />
          </template>
          <span class="car-name">{{ item.name }}</span>
        </div>
        <div class="row-item">{{ fmtNum(item.mentions) }}</div>
        <div class="row-item negative">{{ fmtPer(item.negativeRate || '') }}</div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.table-container {
  margin-top: 10px;
  width: 100%;
}

%table-layout {
  display: grid;
  grid-template-columns: 50px 1fr 70px 70px;
}

.table-header {
  @extend %table-layout;
  background: #f5f7fa;
  border-bottom: 1px solid #ebedf0;
  height: 32px;
  line-height: 32px;
}
.header-item {
  font-weight: 400;
  font-size: 12px;
  color: #5f6a7a;
  text-align: center;
}

.table-body {
  .table-row {
    @extend %table-layout;
    height: 60px;
    align-items: center;
    text-align: center;
    &:not(:last-child) {
      border-bottom: 1px solid #ebedf0;
    }
    .row-item {
      font-weight: 500;
      font-size: 12px;
      color: #666666;
      line-height: 24px;
    }

    .negative {
      font-weight: 500;
      font-size: 12px;
      color: #1f2733;
      line-height: 24px;

      &.danger {
        color: #ff5959;
      }
    }

    .row-car {
      font-weight: 500;
      font-size: 14px;
      color: #333333;
      line-height: 24px;
      display: flex;
      align-items: center;
      min-width: 0;
      text-align: left;
      .car-img {
        width: 48px;
        height: 36px;
        margin-right: 4px;
      }
      .car-name {
        font-size: 12px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        flex: 1;
        min-width: 0;
      }
    }
  }
}
</style>
