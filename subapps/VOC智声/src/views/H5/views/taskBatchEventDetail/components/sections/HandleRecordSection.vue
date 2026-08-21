<script setup lang="ts">
import { computed, reactive, withDefaults, defineProps } from 'vue'
import dayjs from 'dayjs'
import type { TaskEventLogModel, TaskEventLogContentModel } from '@h5/api/taskEvent'

defineOptions({
  name: 'HandleRecordSection'
})

interface HandleRecordSectionProps {
  records?: TaskEventLogModel[]
}

const props = withDefaults(defineProps<HandleRecordSectionProps>(), {
  records: () => []
})

const hub = reactive({
  expandedMap: {} as Record<string, boolean>
})

const safeText = (val?: string) => {
  const text = String(val ?? '').trim()
  return text ? text : ''
}

const formatOperateTime = (raw?: string) => {
  const text = String(raw ?? '').trim()
  if (!text) return '-'

  // 兼容后端返回的多种时间格式：2025-11-10 22:00:00 / 2025.11.10 22:00:00 / ISO 8601
  const normalized = text.replace(/\./g, '-')
  const parsed = dayjs(normalized)
  return parsed.isValid() ? parsed.format('YYYY.MM.DD HH:mm:ss') : text
}

const normalizeContents = (list?: TaskEventLogContentModel[]) => {
  if (!Array.isArray(list)) return []
  return list.filter(Boolean)
}

const displayRecords = computed(() => {
  return (props.records || []).map((record, index) => {
    const recordId = String(record?.id || `${record?.operateTime || 't'}-${index}`)
    const contents = normalizeContents(record?.content)
    const isLong = contents.length > 2
    const isExpanded = !!hub.expandedMap[recordId]
    const visibleContents = isLong && !isExpanded ? contents.slice(0, 2) : contents

    return {
      record,
      recordId,
      contents,
      visibleContents,
      isLong,
      isExpanded
    }
  })
})

const toggleExpand = (recordId: string) => {
  hub.expandedMap[recordId] = !hub.expandedMap[recordId]
}
</script>

<template>
  <div class="handle-record-list">
    <!-- 空记录占位 -->
    <div v-if="!props.records || props.records.length === 0" class="empty-text">
      暂无操作记录
    </div>

    <!-- 执行记录列表 -->
    <div v-else>
      <div
        v-for="(itemWrap, index) in displayRecords"
        :key="itemWrap.recordId"
        class="record-item"
      >
        <!-- 左侧时间轴节点 -->
        <div class="timeline-col">
          <div class="dot"></div>
          <div v-if="index !== displayRecords.length - 1" class="line"></div>
        </div>

        <!-- 右侧内容 -->
        <div class="record-main">
          <div class="operate-type">{{ safeText(itemWrap.record.operateType) }}</div>
          <div class="operate-time">{{ formatOperateTime(itemWrap.record.operateTime) }}</div>

          <div class="content-card">
            <template v-if="itemWrap.contents.length">
              <div
                v-for="(c, cIndex) in itemWrap.visibleContents"
                :key="`${itemWrap.recordId}-${cIndex}`"
                class="content-row"
              >
                <div v-if="safeText(c.contentType)" class="content-type">{{ safeText(c.contentType) }}</div>
                <div class="content-value">{{ safeText(c.content) }}</div>
              </div>

              <div
                v-if="itemWrap.isLong"
                class="toggle-row"
                @click="toggleExpand(itemWrap.recordId)"
              >
                <span>{{ itemWrap.isExpanded ? '收起' : '查看更多' }}</span>
                <van-icon
                  class="toggle-icon"
                  :name="itemWrap.isExpanded ? 'arrow-up' : 'arrow-down'"
                  size="12"
                  color="#86909C"
                />
              </div>
            </template>
            <div v-else class="content-empty">暂无内容</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.handle-record-list {
  font-size: 14px;
  color: #1f2733;
}

.empty-text {
  padding: 8px 0;
  font-size: 12px;
  color: #929aa6;
}

.record-item {
  display: flex;
}

.record-main {
  flex: 1;
  min-width: 0;
  padding-left: 10px;
  padding-bottom: 20px;

  .operate-type {
    font-weight: 400;
    font-size: 14px;
    color: rgba(0, 0, 0, 0.9);
    line-height: 24px;
  }

  .operate-time {
    margin-top: 8px;
    font-weight: 400;
    font-size: 14px;
    color: #929AA6;
    line-height: 22px;
  }

  .content-card {
    margin-top: 10px;
    background: #f2f3f5;
    border-radius: 6px;
    padding: 12px;
  }

  .content-row {
    display: flex;
    gap: 16px;

    &:not(:first-child) {
      margin-top: 10px;
    }
  }

  .content-type {
    flex: none;
    font-weight: 400;
    font-size: 12px;
    color: #86909C;
    line-height: 22px;
  }

  .content-value {
    flex: 1;
    min-width: 0;
    font-weight: 400;
    font-size: 12px;
    color: #1D2129;
    line-height: 22px;
    word-break: break-word;
  }

  .content-empty {
    font-weight: 400;
    font-size: 14px;
    color: #86909c;
    line-height: 20px;
  }

  .toggle-row {
    margin-top: 12px;
    display: flex;
    justify-content: center;
    align-items: center;
    font-weight: 400;
    font-size: 12px;
    color: #86909c;
    line-height: 18px;
    cursor: pointer;
    user-select: none;
  }

  .toggle-icon {
    margin-left: 4px;
  }
}

.timeline-col {
  position: relative;
  width: 18px;
  flex: none;
  display: flex;
  justify-content: center;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #929AA6;
  margin-top: 6px;
}

.line {
  position: absolute;
  top: 18px;
  bottom: 0;
  width: 2px;
  left: 50%;
  transform: translateX(-50%);
  background: #DCDCDC;
}
</style>
