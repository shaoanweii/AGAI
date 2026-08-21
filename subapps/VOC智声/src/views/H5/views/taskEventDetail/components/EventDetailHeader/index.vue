<script setup lang="ts">
import type {
  MobileSingleEventDetailVo
} from '@h5/api/taskEvent'
import EventProcessSteps from './EventProcessSteps.vue'

interface EventDetailHeaderProps {
  info: MobileSingleEventDetailVo
}

const props = defineProps<EventDetailHeaderProps>()
</script>

<template>
  <div class="event-detail-header">
    <!-- 顶部事件基础信息 -->
    <div class="header-main">
      <div class="event-title van-ellipsis">
        {{ props.info.eventName }}
      </div>
      <div class="event-no mt-8">
        事件编号：{{ props.info.warningEventNo }}
      </div>
    </div>

    <!-- 事件处理流程进度条 -->
    <div class="header-steps mt-12">
      <EventProcessSteps :task-status="props.info.taskStatus" :event-info="props.info" />
    </div>
  </div>
</template>

<style scoped lang="scss">
.event-detail-header {
  padding: 12px;
  background: linear-gradient(180deg, #1677FF 0%, #398BFF 100%);
  color: #ffffff;
}

.header-main {
  .event-title {
    font-weight: 500;
    font-size: 16px;
    color: #FFFFFF;
    line-height: 24px;
  }

  .event-no {
    font-weight: 400;
    font-size: 14px;
    color: #FFFFFF;
    line-height: 22px;
    color: rgba(255, 255, 255, 0.85);
  }
}

.header-steps {
  // 外层仅负责间距，内部样式由 EventProcessSteps 控制
}
</style>
