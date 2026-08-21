<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getBatchEventDashboardEventList, getBatchEventDashboardStatCards } from '@/api/batchEvent'
import type {
  BatchEventDashboardEventType,
  DashboardEventItem,
  DashboardStatCard
} from '@/api/batchEvent/types'
import BatchEventDetail from '@/components/Business/EventHandle/BatchEventDetail/index.vue'
import EventDetail from '@/components/Business/EventHandle/EventDetail/index.vue'
import { EventType } from '@/components/Business/EventHandle/ehConstants'
import BoxItem from './BoxItem.vue'

defineOptions({
  name: 'CustomerDrivenDirectly'
})

const props = defineProps<{
  startDate?: string
  endDate?: string
  eventType: BatchEventDashboardEventType
}>()

const emit = defineEmits<{
  'stat-click': [card: DashboardStatCard]
}>()

const CARD_BG_COLORS = ['#E6F4FE', '#E5FAFE', '#FFFAEB', '#FEF3F2'] as const

const statCards = ref<DashboardStatCard[]>([])
const tableData = ref<DashboardEventItem[]>([])
const loading = ref(false)
const curRow = ref<DashboardEventItem>()
const batchDetailVisible = ref(false)
const singleDetailVisible = ref(false)
let requestSeq = 0

const detailStartTime = computed(() => (props.startDate ? `${props.startDate} 00:00:00` : ''))
const detailEndTime = computed(() => (props.endDate ? `${props.endDate} 23:59:59` : ''))

/**
 * 按当前总览时间和事件类型拉取客情直驱统计卡片与列表。
 */
const fetchDashboardData = async () => {
  if (!props.startDate || !props.endDate) {
    statCards.value = []
    tableData.value = []
    return
  }

  const currentSeq = ++requestSeq
  loading.value = true

  try {
    const queryParams = {
      startDate: props.startDate,
      endDate: props.endDate,
      eventType: props.eventType
    }

    const [statCardsResponse, eventListResponse] = await Promise.all([
      getBatchEventDashboardStatCards(queryParams),
      getBatchEventDashboardEventList(queryParams)
    ])

    if (currentSeq !== requestSeq) {
      return
    }

    if (statCardsResponse.success) {
      statCards.value = statCardsResponse.result || []
    } else {
      statCards.value = []
      ElMessage.error(statCardsResponse.message || '获取客情直驱统计数据失败')
    }

    if (eventListResponse.success) {
      tableData.value = eventListResponse.result || []
    } else {
      tableData.value = []
      ElMessage.error(eventListResponse.message || '获取客情直驱事件列表失败')
    }
  } catch (error) {
    if (currentSeq === requestSeq) {
      statCards.value = []
      tableData.value = []
      console.error('获取客情直驱数据失败:', error)
      ElMessage.error('获取客情直驱数据失败，请稍后重试')
    }
  } finally {
    if (currentSeq === requestSeq) {
      loading.value = false
    }
  }
}

/**
 * 点击统计卡片时，交给总览页按当前事件类型跳转对应列表页。
 * @param card 后端返回的统计卡片
 */
const handleStatCardClick = (card: DashboardStatCard) => {
  emit('stat-click', card)
}

/**
 * 点击列表整行打开当前事件类型对应的详情弹窗。
 * @param row 后端返回的事件行
 */
const handleEventRowClick = (row: DashboardEventItem) => {
  curRow.value = row

  if (props.eventType === 'BATCH') {
    batchDetailVisible.value = true
    return
  }

  if (!row.dataId) {
    ElMessage.warning('当前事件缺少 dataId，无法打开详情')
    return
  }

  singleDetailVisible.value = true
}

watch(
  () => [props.startDate, props.endDate, props.eventType],
  () => {
    void fetchDashboardData()
  },
  { immediate: true }
)
</script>

<template>
  <div class="customer-driven-directly" v-loading="loading">
    <div v-if="statCards.length" class="customer-driven-directly__stats">
      <BoxItem
        v-for="(card, index) in statCards"
        :key="card.status || index"
        :card="card"
        :bgcolor="CARD_BG_COLORS[index % CARD_BG_COLORS.length]"
        @click="handleStatCardClick"
      />
    </div>
    <div v-else class="customer-driven-directly__stat-empty">
      <el-empty :image-size="44" description="暂无统计数据" />
    </div>

    <el-table
      :data="tableData"
      style="width: 100%"
      :height="'165px'"
      class="mt-12"
      empty-text="暂无事件数据"
      @row-click="handleEventRowClick"
    >
      <el-table-column prop="eventName" label="事件名称" min-width="120" show-overflow-tooltip />
      <el-table-column prop="subjectCategoryName" label="主题分类" show-overflow-tooltip />
      <el-table-column prop="eventPriorityName" label="优先级" width="90">
        <template #default="{ row }">
          <span class="px-10 text-small">{{ row.eventPriorityName || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="primaryDepName" label="责任部门" show-overflow-tooltip />
      <el-table-column prop="taskStatusName" label="事件状态" width="100">
        <template #default="{ row }">
          <el-tag type="primary" round hit>
            <span>{{ row.taskStatusName || '-' }}</span>
          </el-tag>
        </template>
      </el-table-column>
    </el-table>

    <BatchEventDetail
      v-model="batchDetailVisible"
      :row="curRow"
      :event-type="EventType.VIEW"
      :start-time="detailStartTime"
      :end-time="detailEndTime"
      @refresh="fetchDashboardData"
    />

    <EventDetail
      v-model="singleDetailVisible"
      :row="curRow"
      :event-type="EventType.VIEW"
      :start-time="detailStartTime"
      :end-time="detailEndTime"
      @confirm="fetchDashboardData"
      @refresh="fetchDashboardData"
    />
  </div>
</template>

<style lang="scss" scoped>
.customer-driven-directly {
  min-height: 274px;

  &__stats {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 8px;
    width: 100%;
    min-width: 0;
  }

  ::v-deep(.el-tag.el-tag--primary.is-hit) {
    border-color: #b2ddff !important;
  }

  :deep(.el-table__row) {
    cursor: pointer;
  }

  :deep(.el-empty) {
    padding: 0;
  }

  &__stat-empty {
    height: 92px;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 1px dashed #d5d9e2;
    border-radius: 8px;
    background: #f8fafc;
  }
}
</style>
