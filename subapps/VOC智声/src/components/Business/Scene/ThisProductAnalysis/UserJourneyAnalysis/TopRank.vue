<script setup lang="ts">
import { fmtNum, fmtPer, fmtFix, fmtHoverData } from '@/utils'
import HoverPopover from '@components/Business/Scene/Common/HoverPopover.vue'
import ViewMore from '@/components/Business/DrillDownDialog/components/ViewMore.vue'

defineOptions({
  name: 'TopRank'
})

type TopRankItem = {
  rank: number
  label: string
  value: number
  change: string
  // 额外字段（用于悬浮与下钻）
  mentions?: number
  mentionsMoM?: number
  mentionsYoY?: number
  sentiment?: string
}

const { data, showMore = true } = defineProps<{
  data: TopRankItem[]
  showMore?: boolean
}>()

// ==================== 事件定义 ====================

const emit = defineEmits<{
  (e: 'row-click', data: any): void
  (e: 'view-more', payload: { sentiment?: string }): void
}>()

// ==================== 事件处理方法 ====================

/**
 * 处理行点击事件
 */
const handleRowClick = (item: any) => {
  emit('row-click', item)
}

/**
 * 处理查看更多点击事件
 */
const handleViewMore = () => {
  const sentiment = (data?.[0] as any)?.sentiment
  emit('view-more', { sentiment })
}

// onMounted(() => {
//   console.log('data@@',data)
// })
</script>

<template>
  <div class="top-rank">
    <div class="top-rank__content">
      <template v-if="data.length > 0">
        <div v-for="(item, index) in data" :key="item.rank" class="top-item">
          <SortNum :rank="index + 1"></SortNum>
          <HoverPopover
            :table-config="{
              title: item.label || '',
              data: fmtHoverData(item, 'mentions'),
              columns: [
                { title: '名称', dataIndex: 'label', width: 70 },
                { title: '数值', dataIndex: 'value', width: 80 },
                { title: '环比', dataIndex: 'rateMoM', className: 'c666' },
                { title: '同比', dataIndex: 'rateYoY', className: 'c666' }
              ]
            }"
          >
            <template #reference>
              <span
                class="label single-line-ellipsis"
                :title="item.label"
                @click="handleRowClick(item)"
                style="cursor: pointer"
                >{{ item.label }}</span
              >
            </template>
          </HoverPopover>

          <span class="value">{{ fmtNum(item.value) }}</span>
          <span class="change">{{ fmtPer(item.change) }}</span>
        </div>
      </template>
      <div v-else class="no-data">暂无数据</div>
    </div>
    <div v-if="showMore && data.length > 0" class="more-wrap">
      <ViewMore @click="handleViewMore" />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.top-rank {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.top-rank__content {
  flex: 1;
  min-height: 0;
}

.no-data {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100%;
}

.top-item {
  display: flex;
  align-items: center;
  font-size: 12px;
  height: 40px;

  &:not(:last-child) {
    margin-bottom: 8px;
  }

  .label {
    flex: 1;
    font-weight: 500;
    font-size: 14px;
    color: #333333;
    line-height: 24px;
    min-width: 0;
    margin-left: 4px;
  }

  .value {
    width: 60px;
    margin-right: 8px;
    font-weight: 500;
    font-size: 14px;
    color: #1f2733;
    line-height: 20px;
    text-align: right;
  }

  .change {
    width: 60px;
    font-weight: 500;
    font-size: 12px;
    color: #666666;
    line-height: 24px;
    text-align: right;
  }
}

.more-wrap {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: auto;
  padding-top: 12px;
}
</style>
