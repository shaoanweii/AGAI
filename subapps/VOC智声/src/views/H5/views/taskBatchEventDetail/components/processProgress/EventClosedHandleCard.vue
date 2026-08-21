<script setup lang="ts">
import ProcessSectionCard from './ProcessSectionCard.vue'
import type { BatchEventTaskVo } from '@h5/api/batchEvent/types'

defineOptions({
  name: 'BatchEventClosedHandleCard'
})

interface EventClosedHandleCardProps {
  /** 事件关闭步骤任务接口数据 */
  tasks: BatchEventTaskVo[]
}

const props = defineProps<EventClosedHandleCardProps>()
</script>

<template>
  <ProcessSectionCard title="事件处理" collapsible>
    <div class="event-closed-handle">
      <div v-if="props.tasks.length === 0" class="event-closed-empty">暂无任务</div>

      <article
        v-for="(task, index) in props.tasks"
        :key="task.taskId || `${task.eventId || 'task'}-${index}`"
        class="event-closed-task"
      >
        <div class="event-closed-task__header">
          <div class="event-closed-task__name van-ellipsis">{{ task.taskName || '-' }}</div>
          <div class="event-closed-task__status">
            <span class="event-closed-task__status-dot"></span>
            <span>{{ task.progressStatusName || task.progressStatus || '-' }}</span>
          </div>
        </div>

        <div class="event-closed-task__meta">
          <span>{{ task.handleTime || task.updateTime || task.createTime || '-' }}</span>
          <span>{{ task.handleDeptName || '-' }}</span>
          <span>{{ task.assigneeName || '-' }}</span>
        </div>

        <div class="event-closed-task__desc">{{ task.taskDesc || '-' }}</div>
      </article>
    </div>
  </ProcessSectionCard>
</template>

<style scoped lang="scss">
.event-closed-handle {
  display: flex;
  flex-direction: column;
  row-gap: 12px;
}

.event-closed-empty {
  padding: 8px 0;
  font-weight: 400;
  font-size: 12px;
  line-height: 18px;
  color: #929aa6;
}

.event-closed-task {
  padding: 12px;
  border: 1px solid #ebeef2;
  border-radius: 6px;
  background: #ffffff;
}

.event-closed-task__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  column-gap: 12px;
}

.event-closed-task__name {
  flex: 1;
  min-width: 0;
  font-weight: 600;
  font-size: 14px;
  line-height: 22px;
  color: #1f2733;
}

.event-closed-task__status {
  flex: none;
  display: flex;
  align-items: center;
  column-gap: 6px;
  font-weight: 400;
  font-size: 13px;
  line-height: 20px;
  color: #1f2733;
}

.event-closed-task__status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #22c55e;
}

.event-closed-task__meta {
  margin-top: 4px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px 8px;
  font-weight: 400;
  font-size: 11px;
  line-height: 18px;
  color: #5f6a7a;

  span + span::before {
    content: '|';
    margin-right: 8px;
    color: #c9cdd4;
  }
}

.event-closed-task__desc {
  margin-top: 8px;
  min-height: 36px;
  padding: 9px 12px;
  border-radius: 4px;
  background: #f5f7fb;
  font-weight: 400;
  font-size: 12px;
  line-height: 18px;
  color: #5f6a7a;
}
</style>
