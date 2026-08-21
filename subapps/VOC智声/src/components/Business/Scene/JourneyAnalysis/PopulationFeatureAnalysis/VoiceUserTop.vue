<script setup lang="ts">
import { computed } from 'vue'
import type { VoiceUserTopVo } from '@/api/journeyAnalysis/types'
import { fmtNum, fmtPer, fmtFix } from '@/utils'
import HoverPopover from '@/components/Business/Scene/Common/HoverPopover.vue'

defineOptions({
  name: 'VoiceUserTop'
})

const { data } = defineProps<{
  data: VoiceUserTopVo[]
}>()


// 事件定义
const emit = defineEmits<{
  (e: 'user-click', data: VoiceUserTopVo): void
}>()

// 处理用户点击事件
const handleUserClick = (item: VoiceUserTopVo) => {
  emit('user-click', item)
}

</script>

<template>
  <div class="voice-user-top">
    <!-- 表头 -->
    <div class="header-row">
      <div class="header-cell user-col">用户</div>
      <div class="header-cell mentions-col">提及量</div>
      <div class="header-cell rate-col">负面率</div>
    </div>

    <!-- 数据行 -->
    <div class="data-rows">
      <div
        v-for="(item, index) in data"
        :key="index"
        class="data-row"
        :class="{ 'top-three': index < 3 }"
      >
        <div class="data-cell user-col">
          <SortNum :rank="index + 1" />
          <HoverPopover
            placement="top"
            :show-after="200"
            :width="380"
            trigger="hover"
            :table-config="{
              title: item.userName,
              data: [
                {
                  name: '负面率',
                  value: fmtPer(item.negativeRate),
                  valueMoM: fmtFix(item.valueMoM),
                  valueYoY: fmtFix(item.valueYoY)
                },
                {
                  name: '提及量',
                  value: fmtNum(item.value),
                  valueMoM: fmtFix(item.valueMoM),
                  valueYoY: fmtFix(item.valueYoY)
                }
              ],
              columns: [
                { title: '名称', dataIndex: 'name', width: 70 },
                { title: '数值', dataIndex: 'value', width: 90 },
                { title: '环比', dataIndex: 'valueMoM', width: 90, className: 'c666' },
                { title: '同比', dataIndex: 'valueYoY', width: 90, className: 'c666' }
              ]
            }"
          >
            <template #reference>
              <span class="user-name" @click="handleUserClick(item)" style="cursor: pointer;">{{ item.userName }}</span>
            </template>
          </HoverPopover>
        </div>
        <div class="data-cell mentions-col">
          {{ fmtNum(item.value) }}
        </div>
        <div class="data-cell rate-col">
          {{ fmtPer(item.negativeRate) }}
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.voice-user-top {
  height: 110%;
  display: flex;
  flex-direction: column;
  margin-top: -10px;

  .header-row {
    display: flex;
    align-items: center;
    height: 30px;
    // border-bottom: 1px solid #f0f0f0;
    margin-bottom: 8px;

    .header-cell {
      font-size: 14px;
      font-weight: 500;
      color: #666666;
      line-height: 20px;

      &.user-col {
        flex: 1;
        min-width: 0;
      }

      &.mentions-col {
        width: 80px;
        text-align: center;
      }

      &.rate-col {
        width: 80px;
        text-align: center;
      }
    }
  }

  .data-rows {
    flex: 1;
    overflow-y: auto;
  }

  .data-row {
    display: flex;
    align-items: center;
    height: 42px;
    padding: 5px 0;
    border-radius: 4px;
    margin-bottom: 6px;

    // 默认背景渐变色（排行4及以后）
    background: linear-gradient(90deg, rgba(239, 238, 243, 0) 0%, rgba(#f1f6ff, 0.7) 100%);

    // 前三名的背景渐变色
    &.top-three {
      background: linear-gradient(90deg, rgba(153, 45, 46, 0) 0%, rgba(#ff4b4c, 0.1) 100%);
    }

    &:not(:last-child) {
      border-bottom: 1px solid #f8f8f8;
    }

    .data-cell {
      font-size: 14px;
      color: #333333;
      line-height: 20px;

      &.user-col {
        flex: 1;
        min-width: 0;
        display: flex;
        align-items: center;

        .user-name {
          margin-left: 8px;
          font-weight: 500;
        }
      }

      &.mentions-col {
        width: 80px;
        text-align: center;
        font-weight: 500;
        color: #1f2733;
      }

      &.rate-col {
        width: 80px;
        text-align: center;
        font-weight: 500;
        color: #1f2733;
      }
    }
  }
}
</style>
