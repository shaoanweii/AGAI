<script setup lang="ts">
import { computed } from 'vue'
import ProcessSectionCard from './ProcessSectionCard.vue'
import type { BatchEventTaskVo } from '@h5/api/batchEvent/types'
import type { BatchClosedLoopMode, BatchProcessActionDialogMode } from './types'

defineOptions({
  name: 'BatchClosedLoopHandleCard'
})

interface ClosedLoopHandleCardProps {
  /** 闭环方式，来自业务响应步骤的处理方式 */
  mode: BatchClosedLoopMode
  /** 闭环处理任务接口数据 */
  tasks: BatchEventTaskVo[]
  /** 是否只读展示任务，不渲染任务操作入口 */
  readOnly?: boolean
}

const props = defineProps<ClosedLoopHandleCardProps>()
const emit = defineEmits<{
  /** 触发任务动作 */
  (e: 'action', mode: BatchProcessActionDialogMode, task: BatchEventTaskVo): void
}>()

const visibleTasks = computed(() => {
  return props.tasks
})

/**
 * 根据任务接口状态生成状态点颜色类名。
 * @param task 任务接口项
 * @returns 状态类名
 */
const getStatusClass = (task: BatchEventTaskVo) => {
  const statusText = task.progressStatusName || ''
  const statusCode = String(task.progressStatus || '').toUpperCase()

  if (statusText.includes('完成') || ['COMPLETED', 'COMPLETE', 'DONE'].includes(statusCode)) {
    return 'is-completed'
  }

  if (statusText.includes('未') || ['NOT_STARTED', 'NOT-STARTED'].includes(statusCode)) {
    return 'is-pending'
  }

  return 'is-processing'
}

/**
 * 按接口权限字段控制任务操作。
 * @param task 任务接口项
 * @param mode 操作类型
 * @returns 是否禁用任务操作
 */
const isTaskActionDisabled = (task: BatchEventTaskVo, mode: BatchProcessActionDialogMode) => {
  if (props.readOnly) return true
  if (mode === 'editTask') return task.editable === false
  if (mode === 'updateProgress') return task.progressEditable === false
  if (mode === 'transferTask') return task.reassignable === false
  if (mode === 'deleteTask') return task.deletable === false
  return false
}

/**
 * 触发任务动作。禁用状态下不向外派发事件。
 * @param task 任务接口项
 * @param mode 弹窗类型
 */
const handleTaskAction = (task: BatchEventTaskVo, mode: BatchProcessActionDialogMode) => {
  if (isTaskActionDisabled(task, mode)) return
  emit('action', mode, task)
}
</script>

<template>
  <ProcessSectionCard title="事件处理" collapsible>
    <div class="closed-loop-handle" :class="`is-${props.mode}`">
      <div v-if="visibleTasks.length === 0" class="closed-loop-empty">暂无任务</div>

      <article
        v-for="(task, index) in visibleTasks"
        :key="task.taskId || `${task.eventId || 'task'}-${index}`"
        class="closed-loop-task"
      >
        <div class="closed-loop-task__header">
          <div class="closed-loop-task__name van-ellipsis">{{ task.taskName || '-' }}</div>
          <div class="closed-loop-task__status" :class="getStatusClass(task)">
            <span class="closed-loop-task__status-dot"></span>
            <span>{{ task.progressStatusName || task.progressStatus || '-' }}</span>
          </div>
        </div>

        <div class="closed-loop-task__meta">
          <span>{{ task.handleTime || task.updateTime || task.createTime || '-' }}</span>
          <span>{{ task.handleDeptName || '-' }}</span>
          <span>{{ task.assigneeName || '-' }}</span>
        </div>

        <div class="closed-loop-task__desc">{{ task.taskDesc || '-' }}</div>

        <div v-if="props.mode === 'voc' && !props.readOnly" class="closed-loop-task__actions">
          <button
            class="task-action-button"
            :disabled="isTaskActionDisabled(task, 'editTask')"
            type="button"
            @click="handleTaskAction(task, 'editTask')"
          >
            编辑任务
          </button>
          <button
            class="task-action-button"
            :disabled="isTaskActionDisabled(task, 'updateProgress')"
            type="button"
            @click="handleTaskAction(task, 'updateProgress')"
          >
            更新进度
          </button>
          <button
            class="task-action-button"
            :disabled="isTaskActionDisabled(task, 'transferTask')"
            type="button"
            @click="handleTaskAction(task, 'transferTask')"
          >
            转派任务
          </button>
          <button
            class="task-action-button"
            :disabled="isTaskActionDisabled(task, 'deleteTask')"
            type="button"
            @click="handleTaskAction(task, 'deleteTask')"
          >
            删除任务
          </button>
        </div>
      </article>
    </div>
  </ProcessSectionCard>
</template>

<style scoped lang="scss">
.closed-loop-handle {
  display: flex;
  flex-direction: column;
  row-gap: 12px;
}

.closed-loop-empty {
  padding: 8px 0;
  font-weight: 400;
  font-size: 12px;
  line-height: 18px;
  color: #929aa6;
}

.closed-loop-task {
  padding: 12px;
  border: 1px solid #ebeef2;
  border-radius: 6px;
  background: #ffffff;
}

.closed-loop-task__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  column-gap: 12px;
}

.closed-loop-task__name {
  flex: 1;
  min-width: 0;
  font-weight: 600;
  font-size: 14px;
  line-height: 22px;
  color: #1f2733;
}

.closed-loop-task__status {
  flex: none;
  display: flex;
  align-items: center;
  column-gap: 6px;
  font-weight: 400;
  font-size: 13px;
  line-height: 20px;
  color: #1f2733;
}

.closed-loop-task__status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #c9cdd4;
}

.closed-loop-task__status.is-processing {
  .closed-loop-task__status-dot {
    background: #1677ff;
  }
}

.closed-loop-task__status.is-completed {
  .closed-loop-task__status-dot {
    background: #22c55e;
  }
}

.closed-loop-task__status.is-pending {
  .closed-loop-task__status-dot {
    background: #c9cdd4;
  }
}

.closed-loop-task__meta {
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

.closed-loop-task__desc {
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

.closed-loop-task__actions {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
}

.task-action-button {
  height: 32px;
  border: 0;
  border-radius: 4px;
  background: #e8f2ff;
  color: #1677ff;
  font-weight: 400;
  font-size: 13px;
  line-height: 18px;

  &:disabled {
    border: 1px solid #e5e6eb;
    background: #f7f8fa;
    color: #c9cdd4;
  }
}
</style>
