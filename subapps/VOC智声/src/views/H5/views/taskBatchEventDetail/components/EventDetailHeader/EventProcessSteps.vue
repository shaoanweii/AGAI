<script setup lang="ts">
import { computed } from 'vue'
import type { MobileSingleEventDetailVo } from '@h5/api/taskEvent'
import { EVENT_STATUS_KEY_TO_LABEL, TaskStatusEnum, VALID_EVENT } from '@h5/constants'

defineOptions({
  name: 'H5EventProcessSteps'
})

type StepStatus = 'completed' | 'current' | 'pending'

interface StepConfigItem {
  key: string
  label: string
  statusValues: string[]
}

interface Props {
  taskStatus?: string | number
  eventInfo?: MobileSingleEventDetailVo
}

const props = defineProps<Props>()

const stepConfig: StepConfigItem[] = [
  { key: 'insight', label: '声音洞察', statusValues: [] },
  { key: 'warning', label: '事件预警', statusValues: [] },
  {
    key: 'warningReview',
    label: EVENT_STATUS_KEY_TO_LABEL.warningReview,
    statusValues: [TaskStatusEnum.WarningReview10, TaskStatusEnum.WarningReview11]
  },
  {
    key: 'businessResponse',
    label: EVENT_STATUS_KEY_TO_LABEL.businessResponse,
    statusValues: [TaskStatusEnum.BusinessResponse]
  },
  {
    key: 'closedLoop',
    label: EVENT_STATUS_KEY_TO_LABEL.closedLoop,
    statusValues: [TaskStatusEnum.ClosedLoop30, TaskStatusEnum.ClosedLoop40]
  },
  { key: 'eventClosed', label: EVENT_STATUS_KEY_TO_LABEL.eventClosed, statusValues: [] }
]

const visibleStepConfig = computed(() => {
  // 有权限：展示完整流程
  if (props.eventInfo?.permissions?.length) return stepConfig

  // 无权限且无效事件：隐藏「业务响应」「闭环处理」
  if (props.eventInfo?.eventValidity && props.eventInfo.eventValidity !== VALID_EVENT) {
    return stepConfig.filter(step => step.key !== 'businessResponse' && step.key !== 'closedLoop')
  }

  return stepConfig
})

const steps = computed(() => {
  const config = visibleStepConfig.value
  const statusCode = String(props.taskStatus ?? '')

  // 事件关闭：全部完成
  if (statusCode === TaskStatusEnum.EventClosed) {
    return config.map(step => ({
      ...step,
      status: 'completed' as const
    }))
  }

  const currentIndex = config.findIndex(step => step.statusValues.includes(statusCode))

  return config.map((step, index) => {
    let status: StepStatus

    // 未命中任何状态：默认前两步完成，其余待处理
    if (currentIndex === -1) {
      status = index < 2 ? 'completed' : 'pending'
    } else if (index < currentIndex) {
      status = 'completed'
    } else if (index === currentIndex) {
      status = 'current'
    } else {
      status = 'pending'
    }

    return {
      ...step,
      status
    }
  })
})
</script>

<template>
  <div class="event-process-steps">
    <div
      v-for="(step, index) in steps"
      :key="step.key"
      class="step-item"
      :class="{
        'is-completed': step.status === 'completed',
        'is-current': step.status === 'current',
        'is-pending': step.status === 'pending'
      }"
    >
      <div class="step-icon">
        <SvgIcon
          v-if="step.status === 'completed'"
          name="h5-step-finish"
          width="24px"
          height="24px"
          color="#1677FF"
        />
        <SvgIcon
          v-else-if="step.status === 'current'"
          name="h5-step-time"
          width="24px"
          height="24px"
          color="#1677FF"
        />
        <SvgIcon
          v-else
          name="h5-step-pending"
          width="24px"
          height="24px"
          color="#929AA6"
        />
      </div>

      <div class="step-label">
        {{ step.label }}
      </div>

      <div
        v-if="index < steps.length - 1"
        class="step-line"
        :class="{
          'is-active': step.status === 'completed',
          'is-dashed': step.status !== 'completed'
        }"
      ></div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.event-process-steps {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 12px 8px;
  background: #ffffff;
  border-radius: 6px;
}

.step-item {
  position: relative;
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 0;

  .step-icon {
    z-index: 2;
    width: 24px;
    height: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .icon-placeholder {
    width: 24px;
    height: 24px;
    border-radius: 50%;
    border: 2px solid rgba(0, 0, 0, 0.15);
    background: #ffffff;
  }

  .step-label {
    margin-top: 4px;
    font-weight: 400;
    font-size: 12px;
    color: rgba(0, 0, 0, 0.9);
    line-height: 24px;
    text-align: center;
    white-space: nowrap;
  }

  .step-line {
    width: 18px;
    height: 2px;
    position: absolute;
    top: 12px;
    right: -9px;
    z-index: 1;
    border-radius: 2px;
    background-color: #DCDCDC;

    &.is-active {
      background-color: #1677ff;
    }
  }

  &.is-completed {
    .step-label {
      color: rgba(0, 0, 0, 0.9);
    }
  }

  &.is-current {
    .step-label {
      color: rgba(0, 0, 0, 0.9);
    }
  }

  &.is-pending {
    .step-label {
      color: rgba(0, 0, 0, 0.4);
    }
  }
}
</style>
