<script setup lang="ts">
import { computed } from 'vue'
import { VALID_EVENT } from '../../ehConstants'

defineOptions({
  name: 'Steps'
})

interface Props {
  taskStatus: string
  eventInfo: any
}

const props = defineProps<Props>()

// 步骤配置：label和对应的进行中状态值
const stepConfig = [
  { label: '声音洞察', statusValues: [] }, // 固定已完成
  { label: '事件预警', statusValues: [] }, // 固定已完成
  { label: '预警初审', statusValues: ['10', '11'] },
  { label: '业务响应', statusValues: ['20'] },
  { label: '闭环处理', statusValues: ['30', '40'] },
  { label: '事件关闭', statusValues: [] } // 90为已完成，无进行中状态
]

const filteredSteps = computed(() => {
  // 有权限
  if (props.eventInfo?.permissions?.length) {
    return stepConfig
  } else {
    // 没有权限且无效事件,老大难事件,过滤掉"处理确认"和"事件处理"
    if (props.eventInfo?.eventValidity !== VALID_EVENT) {
      return stepConfig.filter(step => step.label !== '处理确认' && step.label !== '事件处理')
    } else {
      return stepConfig
    }
  }
})

// 根据taskStatus计算步骤状态
const steps = computed(() => {
  // 无效事件过滤掉"处理确认"和"事件处理"
  // const filteredSteps =
  //   props.eventInfo?.eventValidity !== VALID_EVENT
  //     ? stepConfig.filter(step => step.label !== '处理确认' && step.label !== '事件处理')
  //     : stepConfig

  // 事件关闭特殊处理：90直接全部完成
  if (props.taskStatus === '90') {
    if (props.eventInfo?.eventValidity !== VALID_EVENT) {
      return stepConfig
        .filter(step => step.label !== '处理确认' && step.label !== '事件处理')
        .map(step => ({
          label: step.label,
          status: 'completed' as const
        }))
    }
    return stepConfig.map(step => ({
      label: step.label,
      status: 'completed' as const
    }))
  }

  const currentStepIndex = stepConfig.findIndex(step =>
    step.statusValues.includes(props.taskStatus)
  )

  return stepConfig.map((step, index) => {
    let status: 'completed' | 'current' | 'pending'

    if (currentStepIndex === -1) {
      status = index < 2 ? 'completed' : 'pending'
    } else if (index < currentStepIndex) {
      status = 'completed'
    } else if (index === currentStepIndex) {
      status = 'current'
    } else {
      status = 'pending'
    }

    return {
      label: step.label,
      status
    }
  })
})
</script>

<template>
  <div class="steps-container">
    <div
      v-for="(step, index) in steps"
      :key="index"
      class="step-item"
      :class="{
        'is-completed': step.status === 'completed',
        'is-current': step.status === 'current',
        'is-pending': step.status === 'pending'
      }"
    >
      <!-- 步骤图标 -->
      <div class="step-icon">
        <SvgIcon
          v-if="step.status === 'completed'"
          name="success-line"
          width="24px"
          height="24px"
        ></SvgIcon>
        <SvgIcon
          v-if="step.status === 'current'"
          name="time-fill"
          width="24px"
          height="24px"
        ></SvgIcon>
        <div v-if="step.status === 'pending'" class="icon-placeholder">{{ index + 1 }}</div>
      </div>

      <!-- 步骤文字 -->
      <div class="step-label">{{ step.label }}</div>

      <!-- 连接线 (最后一个步骤不显示) -->
      <div v-if="index < steps.length - 1" class="step-line"></div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.steps-container {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  // background: #f5f7fa;
}

.step-item {
  display: flex;
  align-items: center;
  flex: 1;
  position: relative;

  &:last-child {
    flex: 0;
  }

  // 步骤图标
  .step-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    // width: 24px;
    // height: 24px;
    // border-radius: 50%;
    // background: #e4e7ed;
    // border: 2px solid #e4e7ed;
    z-index: 2;

    .icon-placeholder {
      width: 24px;
      height: 24px;
      background: #fff;
      border-radius: 50%;
      border: 2px solid rgba(0, 0, 0, 0.4);
      font-weight: 600;
      font-size: 16px;
      color: rgba(0, 0, 0, 0.4);
      display: flex;
      justify-content: center;
      align-items: center;
    }
  }

  // 步骤文字
  .step-label {
    font-weight: 400;
    font-size: 16px;
    color: rgba(0, 0, 0, 0.4);
    line-height: 24px;
    margin-left: 16px;
    white-space: nowrap;
  }

  // 连接线
  .step-line {
    flex: 1;
    height: 2px;
    background: #dcdcdc;
    margin: 0 12px;
  }

  // 已完成状态
  &.is-completed {
    .step-icon {
      // background: #409eff;
      // border-color: #409eff;

      // .icon-placeholder {
      //   background: #fff;
      // }
    }

    .step-label {
      font-weight: 400;
      font-size: 16px;
      color: rgba(0, 0, 0, 0.9);
      line-height: 24px;
    }

    .step-line {
      background: #1677ff;
    }
  }

  // 当前状态
  &.is-current {
    .step-icon {
      // background: #409eff;
      // border-color: #409eff;

      // .icon-placeholder {
      //   background: #fff;
      // }
    }

    .step-label {
      font-weight: 600;
      font-size: 16px;
      color: #1677ff;
      line-height: 24px;
    }

    .step-line {
      background: #1677ff;
    }
  }

  // 待处理状态
  &.is-pending {
    .step-icon {
      // background: #e4e7ed;
      // border-color: #e4e7ed;

      // .icon-placeholder {
      //   background: #fff;
      // }
    }

    // .step-label {
    //   color: #909399;
    // }

    // .step-line {
    //   background: #e4e7ed;
    // }
  }
}
</style>
