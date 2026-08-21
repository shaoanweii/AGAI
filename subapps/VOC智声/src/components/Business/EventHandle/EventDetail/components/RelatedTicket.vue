<script setup lang="ts">
import { computed, ref } from 'vue'
import type { EventType } from '../../ehConstants'
import type { SingleEventDetailVo } from '@/api/singlePointEvent/types'

defineOptions({
  name: 'RelatedTicket'
})

const { eventType, eventInfo } = defineProps<{
  eventType: EventType
  eventInfo: SingleEventDetailVo
}>()

const tableData = computed(() => {
  return eventInfo.relatedWorkOrderNos || []
})
</script>

<template>
  <!-- <div class="related-ticket">
    <div class="rt-item">
      <span class="rti-info">工单号：CA12093872131</span>

      <el-icon class="ml-12 cursor-point"><CloseBold /></el-icon>
    </div>

    <div class="rt-item">
      <span class="rti-info">工单号：CA12093872131</span>
      <el-icon class="ml-12 cursor-point"><CloseBold /></el-icon>
    </div>

    <div class="rt-item">
      <span class="rti-info">工单号：CA12093872131</span>
      <el-icon class="ml-12 cursor-point"><CloseBold /></el-icon>
    </div>

    <div class="add-btn">添加关联工单</div>
  </div> -->
  <div class="related-ticket-table">
    <el-table :data="tableData" :style="{ width: '100%' }">
      <el-table-column prop="workOrderNo" label="工单号" />
      <el-table-column prop="topic" label="工单主题" />
      <el-table-column prop="type" label="工单类型" />
      <el-table-column prop="respUserName" label="责任人" />
      <el-table-column prop="createTime" label="创建时间" />
      <el-table-column prop="status" label="工单状态" />
      <!-- <el-table-column prop="address" label="操作" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link>查看</el-button>
          <el-button type="primary" link>取消关联</el-button>
        </template>
      </el-table-column> -->
    </el-table>
  </div>
</template>

<style lang="scss" scoped>
.related-ticket {
  width: 100%;
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 16px;

  .rt-item {
    padding: 5px 12px;
    font-weight: 400;
    font-size: 14px;

    line-height: 22px;
    background: #ffffff;
    border-radius: 4px 4px 4px 4px;
    border: 1px solid #cbe1ff;
    display: flex;
    align-items: center;
    color: rgba(0, 0, 0, 0.4);

    .rti-info {
      color: #1677ff;
    }
  }

  .add-btn {
    font-weight: 600;
    font-size: 14px;
    color: #1677ff;
    line-height: 34px;
    cursor: pointer;
  }
}
</style>
