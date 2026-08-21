<script setup lang="ts">
import { computed } from 'vue'
import type { IntentionOpinionTopVo } from '@/api/groupAnalysis/types'
import { fmtNum, fmtPer, fmtFix } from '@/utils'
import HoverPopover from '@components/Business/Scene/Common/HoverPopover.vue'
import ViewMore from '@/components/Business/DrillDownDialog/components/ViewMore.vue'

defineOptions({
  name: 'TopTable'
})

interface Props {
  cusTitle?: string
  mode?: 'bad' | 'good'
  data?: IntentionOpinionTopVo[]
}

const { mode = 'good', data, cusTitle } = defineProps<Props>()

// ==================== 事件定义 ====================

const emit = defineEmits<{
  (e: 'row-click', data: IntentionOpinionTopVo): void
  (e: 'view-more', payload: { mode: 'bad' | 'good'; sentiment: string; title: string }): void
}>()

const tableData = computed(() => data || [])
const fallbackSentiment = computed(() => (mode === 'good' ? '正面' : '负面'))

// ==================== 事件处理方法 ====================

/**
 * 处理行点击事件
 */
const handleRowClick = (item: IntentionOpinionTopVo) => {
  emit('row-click', item)
}

/**
 * 处理查看更多点击事件
 * 说明：sentiment 优先取列表首条数据（以接口返回为准）；当表格为空时，按 mode 兜底为“正面/负面”，
 * 保证下钻参数必填，避免出现 sentiment 为空导致下钻查询缺少条件。
 */
const handleViewMore = () => {
  const sentiment = (tableData.value?.[0] as any)?.sentiment || fallbackSentiment.value
  emit('view-more', {
    mode,
    sentiment,
    title: titleText.value
  })
}

// 标题文本
const titleText = computed(() => {
  if (cusTitle) {
    return cusTitle
  }
  return mode === 'good' ? '好评TOP' : '抱怨TOP'
})

// 格式化环比
// const formatMoM = (value: number): string => {
//   const sign = value >= 0 ? '+' : ''
//   return `${sign}${value}%`
// }

// 获取环比颜色
const getMoMColor = (value: number): string => {
  return value >= 0 ? '#14ca64' : '#ff5959'
}

/**组装数据*/
const formatHoverData = (item: any) => {
  return [
    {
      label: '提及量',
      value: fmtNum(item.mentions),
      rateMoM: fmtFix(item.mentionsMoM),
      rateYoY: fmtFix(item.mentionsYoY)
    }
  ]
}
</script>

<template>
  <div class="top-table" :class="[mode === 'bad' ? 'bad' : 'good']">
    <!-- 表头 -->
    <div class="tt-header" :class="[mode === 'bad' ? 'bad' : 'good']">
      <div class="tt-header__left">
        <SvgIcon
          :name="mode === 'bad' ? 'nsr-zm' : 'nsr-my'"
          width="24px"
          height="24px"
          color="#333333"
        ></SvgIcon>
        <span class="tt-header__title">{{ titleText }}</span>
      </div>
      <ViewMore class="tt-header__more" @click="handleViewMore" />
    </div>

    <!-- 表格内容 -->
    <div class="tt-content">
      <!-- 空状态 -->
      <el-empty v-if="!tableData.length" :image-size="100" description="暂无数据" />

      <!-- 有数据时显示表格 -->
      <template v-else>
        <!-- 表格标题行 -->
        <div class="table-header">
          <div class="header-cell opinion-col">观点</div>
          <div class="header-cell mentions-col">提及量</div>
          <!-- <div class="header-cell mom-col">环比</div> -->
        </div>

        <!-- 表格数据行 -->
        <div class="table-body">
          <div v-for="(item, index) in tableData" :key="index" class="table-row">
            <!-- <HoverPopover
              :table-config="{
                title: item.opinion || '',
                data: formatHoverData(item),
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
                  class="data-cell opinion-col"
                  @click="handleRowClick(item)"
                  style="cursor: pointer"
                >
                  <div class="opinion-text">{{ item.opinion }}</div>
                </div>
              </template>
            </HoverPopover> -->

            <div
              class="data-cell opinion-col"
              @click="handleRowClick(item)"
              style="cursor: pointer"
              :title="item.opinion"
            >
              <div class="opinion-text">{{ item.opinion }}</div>
            </div>

            <div class="data-cell mentions-col">{{ fmtNum(item.mentions) }}</div>
            <!-- <div class="data-cell mom-col">{{ fmtFix(item.mentionsMoM) }}</div> -->
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.top-table {
  // width: 318px;
  height: 314px;
  border-radius: 8px;
  padding: 8px;

  &.good {
    background: linear-gradient(180deg, #dcf2ec 0%, rgba(179, 242, 198, 0) 100%);
  }

  &.bad {
    background: linear-gradient(180deg, #f5e6e6 0%, rgba(255, 237, 234, 0) 100%);
  }

  .tt-header {
    // width: 302px;
    height: 40px;
    border-radius: 8px;
    font-weight: 600;
    font-size: 16px;
    color: #333333;
    line-height: 32px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    padding: 0 12px;

    .tt-header__left {
      display: flex;
      align-items: center;
      min-width: 0;
    }

    .tt-header__title {
      margin-left: 8px;
      white-space: nowrap;
    }

    &.good {
      background: rgba(130, 227, 199, 0.5);
    }
    &.bad {
      background: rgba(255, 138, 139, 0.5);
    }
  }

  .tt-content {
    .table-header {
      display: flex;
      padding: 0 16px 8px 16px;
      margin-bottom: 8px;
      border-bottom: 1px solid #dfe2e8;

      .header-cell {
        font-size: 14px;
        color: #5f6a7a;
        font-weight: 400;
        line-height: 20px;

        &.opinion-col {
          min-width: 50px;
          flex: 1;
          text-align: left;
        }

        &.mentions-col {
          width: 80px;
          text-align: center;
        }

        &.mom-col {
          width: 80px;
          text-align: center;
        }
      }
    }

    .table-body {
      .table-row {
        display: flex;
        padding: 8px 16px;
        align-items: center;
        min-height: 44px;

        .data-cell {
          font-size: 14px;
          color: #1f2733;
          line-height: 20px;

          &.opinion-col {
            flex: 1;
            min-width: 50px;
            text-align: left;
            font-weight: 400;
            font-size: 14px;
            color: #333333;
            line-height: 24px;
            overflow: hidden;

            .opinion-text {
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
              width: 100%;
            }
          }

          &.mentions-col {
            width: 80px;
            text-align: center;
            font-weight: 500;
            font-size: 14px;
            color: #1f2733;
            line-height: 20px;
          }

          &.mom-col {
            width: 80px;
            text-align: center;
            font-weight: 500;
            font-size: 14px;
            color: #666666;
            line-height: 24px;
          }
        }
      }
    }
  }
}
</style>
