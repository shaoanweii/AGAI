<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import type { MobileSingleEventRelationEventItem } from '@h5/api/taskEvent'
import { taskStatusColorMap, taskStatusLabelMap } from '@/views/H5/constants/index'

interface RelatedEventSectionProps {
  events: MobileSingleEventRelationEventItem[]
}

const props = defineProps<RelatedEventSectionProps>()

defineOptions({
  name: 'RelatedEventSection'
})

const router = useRouter()

const safeEvents = computed(() => (Array.isArray(props.events) ? props.events : []))

const hasEvents = computed(() => safeEvents.value.length > 0)

const handleView = (item: MobileSingleEventRelationEventItem) => {
  const id = String(item?.id || '')
  const dataId = String(item?.dataId || '')
  if (!id || !dataId) return
  router.push({
    name: 'H5TaskEventDetail',
    query: { id, dataId }
  })
}

const getTagStyle = (tag: any, tagMap: any) => {
  return tagMap[tag] || ''
}

const getTagLabel = (tag: any, tagMap: any) => {
  return tagMap[tag]
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

      <div v-for="(item, index) in safeEvents" :key="String(item.id || item.warningEventNo || index)" class="table-row">
        <el-tooltip
          class="box-item"
          effect="light"
          :content="item.warningEventNo"
          :disabled="!item.warningEventNo"
          trigger="click"
          placement="top"
        >
          <div class="col col--no van-ellipsis">
            {{ item.warningEventNo || '-' }}
          </div>
        </el-tooltip>
        <el-tooltip
          effect="light"
          :content="item.mainRespOrgName"
          :disabled="!item.mainRespOrgName"
          placement="top"
          trigger="click"
        >
          <div class="col col--dept van-ellipsis">
            {{ item.mainRespOrgName || '-' }}
          </div>
        </el-tooltip>
        <div class="col col--status">
          <span
            class="status-dot"
            :style="{ backgroundColor: getTagStyle(item.taskStatus, taskStatusColorMap) }"
          ></span>
          <span class="status-text van-ellipsis">
            {{ getTagLabel(item.taskStatus, taskStatusLabelMap) }}
          </span>
        </div>
        <div class="col col--action">
          <span class="action-link" role="button" tabindex="0" @click.stop="handleView(item)">
            查看
          </span>
        </div>
      </div>
    </div>
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
  background: #f2f4f7;
  border-bottom: 1px solid #ebedf0;

  .col {
    padding: 5px 8px;
    font-weight: 400;
    font-size: 12px;
    color: #5F6A7A;
    line-height: 22px;
  }
}

.table-row {
  display: flex;
  align-items: center;
  font-weight: 400;
  font-size: 12px;
  color: #1D2129;
  line-height: 22px;

  & + & {
    border-top: 1px solid #ebedf0;
  }

  .col {
    padding: 5px 8px;
    border-right: 1px solid #EBEDF0;

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
  width: 48px;
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
  color: #5F6A7A;
  min-width: 0;
}

.action-link {
  font-weight: 400;
  font-size: 12px;
  color: #1677FF;
  line-height: 22px;
}
</style>