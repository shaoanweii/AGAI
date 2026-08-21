<script setup lang="ts">
import { computed, ref } from 'vue'
import EventDetail from '@/components/Business/EventHandle/EventDetail/index.vue'
import { EventType } from '@/components/Business/EventHandle/ehConstants'
import { TaskStatusColorMap } from '@/views/customerDirectEngage/singlePointEvent/constants'

interface VoiceRelatedEventProps {
  events: any[]
}

const props = defineProps<VoiceRelatedEventProps>()

defineOptions({
  name: 'VoiceRelatedEvent'
})

const safeEvents = computed(() => (Array.isArray(props.events) ? props.events : []))

const hasEvents = computed(() => safeEvents.value.length > 0)

const edVisible = ref(false)

const curRow = ref<{ dataId: string; id: string }>({
  dataId: '',
  id: ''
})

const handleView = (item: any) => {
  const id = String(item?.id || '')
  const dataId = String(item?.dataId || '')
  if (!id || !dataId) return

  curRow.value = { dataId, id }
  edVisible.value = true
}
</script>

<template>
  <div class="related-event-section">
    <div v-if="!hasEvents" class="empty-text">暂无关联事件</div>

    <div v-else class="related-table">
      <div class="table-header">
        <div class="col col--no">事件编号</div>
        <div class="col col--dept">主责部门</div>
        <div class="col col--status">事件状态</div>
        <div class="col col--action">操作</div>
      </div>

      <div
        v-for="(item, index) in safeEvents"
        :key="String(item.id || item.warningEventNo || index)"
        class="table-row"
      >
        <div class="col col--no van-ellipsis">
          {{ item.warningEventNo || '-' }}
        </div>
        <el-tooltip
          effect="dark"
          :content="item.mainRespOrgName"
          :disabled="!item.mainRespOrgName || item.mainRespOrgName.length <= 12"
          placement="top"
          popper-class="text-tooltip-light"
        >
          <div class="col col--dept van-ellipsis">
            {{ item.mainRespOrgName || '-' }}
          </div>
        </el-tooltip>
        <div class="col col--status">
          <span
            class="status-dot"
            :style="{ backgroundColor: TaskStatusColorMap[item.taskStatus] }"
          ></span>
          <span class="status-text van-ellipsis">
            {{ item.taskStatusName }}
          </span>
        </div>
        <div class="col col--action">
          <span class="action-link" role="button" tabindex="0" @click.stop="handleView(item)">
            查看
          </span>
        </div>
      </div>
    </div>

    <!-- 事件详情 -->
    <EventDetail v-model="edVisible" :row="curRow" :eventType="EventType.VIEW"></EventDetail>
  </div>
</template>

<style scoped lang="scss">
.related-event-section {
  color: #1f2733;
}

.empty-text {
  padding: 8px 0;
  font-size: 12px;
  color: #929aa6;
}

.related-table {
  border: 1px solid #ebedf0;
  overflow: hidden;
  background: #ffffff;
}

.table-header {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #86909c;
  background: #eaf3ff;
  border-bottom: 1px solid #ebedf0;

  .col {
    padding: 5px 8px;
    font-weight: 400;
    font-size: 12px;
    color: #5f6a7a;
    line-height: 22px;
    border-right: 1px solid #ebedf0;
    &:last-child {
      border-right: none;
    }
  }
}

.table-row {
  display: flex;
  align-items: center;
  font-weight: 400;
  font-size: 12px;
  color: #1d2129;
  line-height: 22px;

  & + & {
    border-top: 1px solid #ebedf0;
  }

  .col {
    padding: 5px 8px;
    border-right: 1px solid #ebedf0;

    &:last-child {
      border-right: none;
    }
  }
}

.col {
  min-width: 0;
}

.col--no {
  flex: 1.2;
  padding-right: 10px;
}

.col--dept {
  flex: 1.2;
  padding-right: 10px;
}

.col--status {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 0;
}

.col--action {
  flex: 1;
  text-align: center;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex: 0 0 auto;
  margin-right: 8px;
}

.status-text {
  font-weight: 400;
  font-size: 12px;
  color: #5f6a7a;
  min-width: 0;
}

.action-link {
  font-weight: 400;
  font-size: 12px;
  color: #1677ff;
  line-height: 22px;
  cursor: pointer;
}
</style>
