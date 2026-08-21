<script setup lang="ts">
import type { DataSourceAnalysisVo } from '@/api/productAnalysis/types'
import { fmtPer, fmtFix } from '@/utils'
import HoverPopover from '@/components/Business/Scene/Common/HoverPopover.vue'
import SortNum from '@/components/UI/SortNum/index.vue'

defineOptions({
  name: 'DSTable'
})

// 接收数据
interface Props {
  data?: DataSourceAnalysisVo[]
}

const props = withDefaults(defineProps<Props>(), {
  data: () => []
})

// 事件定义
const emit = defineEmits<{
  (e: 'cell-click', data: DataSourceAnalysisVo): void
}>()

// 处理单元格点击事件
const handleCellClick = (item: DataSourceAnalysisVo) => {
  emit('cell-click', item)
}

</script>

<template>
  <!-- 空状态展示 -->
  <FEmpty v-if="!props.data.length" />

  <div v-else class="ds-table">
    <div class="cloumns">
      <div class="cell">排名</div>
      <div class="cell">数据来源</div>
      <div class="cell">提及量</div>
      <div class="cell">负面率</div>
    </div>

    <div class="cloumns flex-1" v-for="(item, index) in props.data" :key="item.channelCode">
      <div class="cell">
        <SortNum :rank="index + 1" />
      </div>
      <div class="cell">{{ item.channelName }}</div>
      <HoverPopover
        placement="top"
        :show-after="200"
        :width="410"
        trigger="hover"
        :table-config="{
          title: item.channelName,
          data: [
            {
              ...item,
              name: '提及量',
              value: item.mentions.toLocaleString(),
              mentionsMoM: fmtPer(item.mentionsMoM),
              mentionsYoY: fmtPer(item.mentionsYoY)
            }
          ],
          columns: [
            { title: '名称', dataIndex: 'name', width: 70 },
            { title: '数值', dataIndex: 'value', width: 90 },
            { title: '环比', dataIndex: 'mentionsMoM', className: 'c666' },
            { title: '同比', dataIndex: 'mentionsYoY', className: 'c666' }
          ]
        }"
      >
        <template #reference>
          <div class="cell cell_body" @click="handleCellClick(item)" style="cursor: pointer">
            {{ item.mentions.toLocaleString() }}
            <br />
            {{ fmtFix(item.mentionsMoM) }}
          </div>
        </template>
      </HoverPopover>

      <HoverPopover
        placement="top"
        :show-after="200"
        :width="410"
        trigger="hover"
        :table-config="{
          title: item.channelName,
          data: [
            {
              ...item,
              name: '负面率',
              negativeRate: fmtPer(item.negativeRate),
              negativeRateMoM: fmtPer(item.negativeRateMoM),
              negativeRateYoY: fmtPer(item.negativeRateYoY)
            }
          ],
          columns: [
            { title: '名称', dataIndex: 'name', width: 70 },
            { title: '数值', dataIndex: 'negativeRate', width: 90 },
            { title: '环比', dataIndex: 'negativeRateMoM', className: 'c666' },
            { title: '同比', dataIndex: 'negativeRateYoY', className: 'c666' }
          ]
        }"
      >
        <template #reference>
          <div
            class="cell cell_body"
            @click="handleCellClick(item)"
            :style="{
              cursor: 'pointer',
              backgroundColor: item.rateBackgroundColor,
              color: item.rateColor
            }"
          >
            {{ fmtPer(item.negativeRate) }}
            <br />
            {{ fmtFix(item.negativeRateMoM) }}
          </div>
        </template>
      </HoverPopover>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.ds-table {
  width: 100%;
  height: 100%;
  overflow-x: auto;
  display: flex;
  .cloumns {
    display: flex;
    flex-direction: column;
    gap: 10px;
    margin-right: 16px;

    &:not(:first-child) {
      margin-right: 8px;
    }
    &:last-child {
      margin-right: 0 !important;
    }
    .cell {
      min-width: 120px;
      height: 80px;
      background: #eaf3ff;
      border-radius: 8px 8px 8px 8px;
      display: flex;
      align-items: center;
      justify-content: center;

      font-weight: 500;
      font-size: 16px;
      color: #333333;
      line-height: 24px;

      &.cell_body {
        background: #f2f4f7;
      }

      &.drange {
        background: #ffd1c9;
      }
    }
  }
}
</style>
