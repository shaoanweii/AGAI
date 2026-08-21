<script setup lang="ts">
export interface BatchEventHeaderInfo {
  /** 批量事件规则名称 */
  eventName?: string
  /** 批量事件编号 */
  warningEventNo?: string
}

interface EventDetailHeaderProps {
  info: BatchEventHeaderInfo
}

const props = defineProps<EventDetailHeaderProps>()
</script>

<template>
  <div class="event-detail-header">
    <div class="header-main">
      <div class="event-title van-ellipsis">
        {{ props.info.eventName || '-' }}
      </div>
      <div class="event-no mt-8">事件编号：{{ props.info.warningEventNo || '-' }}</div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.event-detail-header {
  padding: 18px 16px 14px;
  background: linear-gradient(180deg, #2385ff 0%, #348cff 100%);
  color: #ffffff;
}

.header-main {
  .event-title {
    font-weight: 500;
    font-size: 16px;
    color: #ffffff;
    line-height: 24px;
  }

  .event-no {
    font-weight: 400;
    font-size: 14px;
    line-height: 22px;
    color: rgba(255, 255, 255, 0.85);
  }
}
</style>
