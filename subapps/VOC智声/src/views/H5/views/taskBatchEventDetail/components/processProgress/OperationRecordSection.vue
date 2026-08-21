<script setup lang="ts">
import ProcessSectionCard from './ProcessSectionCard.vue'
import type { BatchEventOpeLogVo } from '@h5/api/batchEvent/types'

defineOptions({
  name: 'BatchOperationRecordSection'
})

interface OperationRecordSectionProps {
  /** 操作记录列表 */
  records: BatchEventOpeLogVo[]
}

const props = defineProps<OperationRecordSectionProps>()

/**
 * 直接按接口字段拼接操作记录标题。
 * @param record 操作记录接口项
 * @returns 标题文案
 */
const getRecordTitle = (record: BatchEventOpeLogVo) => {
  const operator = record.operateUserName || record.operateUserEmpNo || '-'
  const operateType = record.operateTypeName || record.operateType || '-'
  return `${operator}-${operateType}`
}

/**
 * 直接按接口 content 字段展示操作内容。
 * @param record 操作记录接口项
 * @returns 操作内容文案
 */
const getRecordContent = (record: BatchEventOpeLogVo) => {
  if (!Array.isArray(record.content) || record.content.length === 0) return '-'

  return record.content
    .map(item => {
      const label = item?.contentType ? `${item.contentType}：` : ''
      return `${label}${item?.content ?? ''}`
    })
    .filter(Boolean)
    .join('\n')
}
</script>

<template>
  <ProcessSectionCard title="操作记录" collapsible>
    <div v-if="props.records.length === 0" class="record-empty">暂无操作记录</div>

    <div v-else class="record-list">
      <div
        v-for="(record, index) in props.records"
        :key="record.id || `${record.operateType || 'log'}-${record.operateTime || index}`"
        class="record-item"
      >
        <div class="timeline-col">
          <span class="timeline-dot"></span>
          <span v-if="index < props.records.length - 1" class="timeline-line"></span>
        </div>

        <div class="record-main">
          <div class="record-title">{{ getRecordTitle(record) }}</div>
          <div class="record-time">{{ record.operateTime || '-' }}</div>
          <div class="record-content">{{ getRecordContent(record) }}</div>
        </div>
      </div>
    </div>
  </ProcessSectionCard>
</template>

<style scoped lang="scss">
.record-empty {
  padding: 8px 0;
  font-weight: 400;
  font-size: 12px;
  line-height: 18px;
  color: #929aa6;
}

.record-item {
  display: flex;
}

.timeline-col {
  position: relative;
  flex: none;
  width: 18px;
  display: flex;
  justify-content: center;
}

.timeline-dot {
  width: 8px;
  height: 8px;
  margin-top: 6px;
  border-radius: 50%;
  background: #929aa6;
}

.timeline-line {
  position: absolute;
  top: 18px;
  bottom: 0;
  left: 50%;
  width: 2px;
  background: #dcdfe6;
  transform: translateX(-50%);
}

.record-main {
  flex: 1;
  min-width: 0;
  padding-left: 12px;
  padding-bottom: 20px;
}

.record-title {
  font-weight: 400;
  font-size: 14px;
  line-height: 24px;
  color: rgba(0, 0, 0, 0.9);
  word-break: break-all;
}

.record-time {
  margin-top: 8px;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  color: #929aa6;
}

.record-content {
  margin-top: 10px;
  padding: 12px 16px;
  border-radius: 4px;
  background: #f2f3f5;
  font-weight: 400;
  font-size: 12px;
  line-height: 20px;
  color: #5f6a7a;
  word-break: break-all;
}
</style>
