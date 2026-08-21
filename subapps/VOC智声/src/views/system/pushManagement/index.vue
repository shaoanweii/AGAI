<script setup lang="ts">
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { computed, h, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { debounce } from 'lodash-es'
import { appDialogConfirm } from '@/components/appDialog'
import SvgIcon from '@/components/UI/SvgIcon/index.vue'
import { deletePushTask, getPushTaskPage } from '@/api/system/pushManagement'
import CreatePushDialog from './components/CreatePushDialog.vue'
import PushTaskDatePicker from './components/PushTaskDatePicker.vue'
import PushDetailDialog from './components/PushDetailDialog.vue'
import type {
  PushTaskListItem,
  PushTaskQueryParams,
  PushTaskStatusOption,
  PushTaskStatusValue
} from '@/api/system/pushManagement/types'
import { useTable } from '@/hooks/useTable'
import { useUserStore } from '@/store'

defineOptions({
  name: 'pushManagement'
})

interface PushManagementForm {
  dateRange: [string, string]
  statusList: PushTaskStatusValue[]
}

enum PushTaskDisplayStatus {
  NotStarted = '0',
  Processing = '1',
  Completed = '2',
  Failed = '3',
  Unknown = 'UNKNOWN'
}

const DEFAULT_PUSH_TASK_DATE_SHORTCUT = '本月'
const PUSH_STATUS_DICT_KEY = 'push_status'
const PUSH_TASK_POLLING_INTERVAL = 60 * 1000
const PUSH_TASK_DATE_LIMIT_DAYS = 365

/**
 * 获取当前月默认时间范围。
 */
const getDefaultPushTaskDateRange = (): [string, string] => {
  return [dayjs().startOf('month').format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')]
}

const toSafeNumber = (value: unknown): number => {
  const parsed = Number(value)
  return Number.isFinite(parsed) && parsed >= 0 ? parsed : 0
}

/**
 * 获取状态标签样式。
 */
const getPushTaskStatusStyle = (task: PushTaskListItem) => {
  const status = task.statusCode || PushTaskDisplayStatus.Unknown

  const colorMap: Record<PushTaskDisplayStatus, string> = {
    [PushTaskDisplayStatus.NotStarted]: '#c9cdd4',
    [PushTaskDisplayStatus.Processing]: '#1677ff',
    [PushTaskDisplayStatus.Completed]: '#00b42a',
    [PushTaskDisplayStatus.Failed]: '#f53f3f',
    [PushTaskDisplayStatus.Unknown]: '#86909c'
  }

  return {
    backgroundColor: colorMap[status as PushTaskDisplayStatus]
  }
}

/**
 * 获取成功人数与总人数展示值。
 */
const getPushTaskDisplayCount = (task: PushTaskListItem): string => {
  const successCount = toSafeNumber(task.successTotal)
  const totalCount = toSafeNumber(task.pushTotal)

  return `${successCount}/${totalCount}`
}

/**
 * 获取进度条展示百分比。
 */
const getPushTaskProgressPercentage = (task: PushTaskListItem): number => {
  return Math.min(100, toSafeNumber(task.successRate))
}

const userStore = useUserStore()
const dateShortcutValue = ref(DEFAULT_PUSH_TASK_DATE_SHORTCUT)
const createPushDialogVisible = ref(false)
const pushDetailDialogVisible = ref(false)
const currentDetailTask = ref<PushTaskListItem | null>(null)
const pollingTimer = ref<number | null>(null)
const statusOptions = computed<PushTaskStatusOption[]>(() => {
  return (userStore.getDictItems(PUSH_STATUS_DICT_KEY) || []).map((item: any) => ({
    label: item.text,
    value: item.value
  }))
})
const FILTER_CHANGE_DEBOUNCE_DELAY = 300

/**
 * 获取推送管理分页数据。
 */
const fetchPushTaskList = async (): Promise<{
  list: PushTaskListItem[]
  total: number
}> => {
  const [startDate = '', endDate = ''] = formData.value.dateRange || []
  const params: PushTaskQueryParams = {
    startDate,
    endDate,
    statusList: formData.value.statusList,
    pageNum: currentPage.value,
    pageSize: pageSize.value
  }

  const response = await getPushTaskPage(params)
  return {
    list: response.result.list,
    total: Number(response.result.total)
  }
}

const {
  tableState: { loading, dataList, currentPage, pageSize, total },
  tableMethods,
  formData
} = useTable<PushManagementForm>({
  immediate: false,
  initialFormData: {
    dateRange: getDefaultPushTaskDateRange(),
    statusList: []
  },
  fetchDataApi: fetchPushTaskList
})

/**
 * 让查询回到第一页，避免筛选条件变化后停留在空页。
 */
const reloadFirstPageList = async () => {
  if (currentPage.value !== 1) {
    currentPage.value = 1
    return
  }

  await tableMethods.handleQuery()
}

/**
 * 筛选条件变化后刷新列表。
 */
const debouncedReloadFirstPageList = debounce(() => {
  void reloadFirstPageList()
}, FILTER_CHANGE_DEBOUNCE_DELAY)

const handleFilterChange = () => {
  debouncedReloadFirstPageList()
}

/**
 * 打开新建推送弹窗。
 */
const handleCreatePush = () => {
  createPushDialogVisible.value = true
}

/**
 * 新建推送成功后刷新当前列表。
 */
const handleCreatePushSuccess = async () => {
  await reloadFirstPageList()
}

/**
 * 判断当前列表是否存在处理中任务。
 * @returns 是否存在处理中任务
 */
const hasProcessingPushTasks = (): boolean => {
  if (!Array.isArray(dataList.value) || dataList.value.length === 0) {
    return false
  }

  return dataList.value.some(
    (item: PushTaskListItem) => item.statusCode === PushTaskDisplayStatus.Processing
  )
}

/**
 * 停止推送任务列表轮询。
 */
const stopPolling = () => {
  if (pollingTimer.value !== null) {
    window.clearInterval(pollingTimer.value)
    pollingTimer.value = null
  }
}

/**
 * 启动推送任务列表轮询。
 * 仅在存在处理中任务时定时刷新列表状态。
 */
const startPolling = () => {
  if (pollingTimer.value !== null || !hasProcessingPushTasks()) {
    return
  }

  pollingTimer.value = window.setInterval(async () => {
    await tableMethods.handleQuery()
  }, PUSH_TASK_POLLING_INTERVAL)
}

/**
 * 根据当前列表任务状态同步轮询启停。
 */
const syncPollingState = () => {
  if (hasProcessingPushTasks()) {
    startPolling()
    return
  }

  stopPolling()
}

/**
 * 删除未开始任务。
 */
const handleDelete = async (row: PushTaskListItem) => {
  try {
    await appDialogConfirm(
      () =>
        h('div', { class: 'flex items-center' }, [
          h(SvgIcon, { name: 'info-circle-filled', width: '20px', height: '20px' }),
          h('span', { class: 'ml-8' }, '确定删除该推送任务吗？删除后不可恢复。')
        ]),
      '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      dialogAttrs: {
        width: '480px',
        closeOnClickModal: false,
        closeOnPressEscape: false
      }
    })
  } catch {
    return
  }

  try {
    const response = await deletePushTask({
      id: row.id
    })
    if (response.success) {
      ElMessage.success(response.message || '删除成功')
      await tableMethods.refresh()
      return
    }

    ElMessage.error(response.message || '删除失败')
  } catch (error) {
    console.error('删除推送任务失败:', error)
    ElMessage.error(error instanceof Error ? error.message : '删除失败')
  }
}

/**
 * 打开推送任务详情弹框。
 * @param row 当前推送任务
 */
const handleView = (row: PushTaskListItem) => {
  currentDetailTask.value = row
  pushDetailDialogVisible.value = true
}

watch(
  () => dataList.value,
  () => {
    syncPollingState()
  },
  { deep: true }
)

onMounted(async () => {
  await tableMethods.getList()
  syncPollingState()
})

onBeforeUnmount(() => {
  debouncedReloadFirstPageList.cancel()
  stopPolling()
})
</script>

<template>
  <div class="push-management">
    <el-card class="table-card" shadow="never">
      <div class="toolbar">
        <div class="page-title">推送列表</div>
        <div class="filter-bar">
          <PushTaskDatePicker
            v-model="formData.dateRange"
            v-model:shortcutValue="dateShortcutValue"
            size="default"
            :teleported="false"
            :max-range-days="PUSH_TASK_DATE_LIMIT_DAYS"
            :max-future-days="PUSH_TASK_DATE_LIMIT_DAYS"
            class="date-range-picker"
            @change="handleFilterChange"
          />

          <el-select-v2
            v-model="formData.statusList"
            :options="statusOptions"
            placeholder="请选择状态"
            clearable
            filterable
            multiple
            collapse-tags
            :max-collapse-tags="1"
            style="width: 220px"
            @change="handleFilterChange"
          />

          <el-button type="primary" @click="handleCreatePush">新建推送</el-button>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="dataList as PushTaskListItem[]"
        max-height="calc(100vh - 84px - 48px - 106px - 32px - 10px)"
        class="flex-auto overflow-auto"
      >
        <el-table-column type="index" label="#" width="56" align="center" />
        <el-table-column prop="pushTime" label="推送开始时间" min-width="180" />
        <el-table-column label="成功人数/推送总数" min-width="160">
          <template #default="{ row }">
            <div>
              <div>{{ getPushTaskDisplayCount(row) }}</div>
              <el-progress :percentage="getPushTaskProgressPercentage(row)" />
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createUser" label="创建人员" min-width="180"> </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="180" />
        <el-table-column label="当前状态" width="120">
          <template #default="{ row }">
            <div class="status-cell">
              <div class="status-dot" :style="getPushTaskStatusStyle(row)"></div>
              <span>{{ row.status }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button
              :disabled="row.statusCode !== PushTaskDisplayStatus.NotStarted"
              link
              type="primary"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
            <el-button
              :disabled="row.statusCode !== PushTaskDisplayStatus.Completed"
              link
              type="primary"
              @click="handleView(row)"
            >
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="->,total, prev, pager, next, sizes"
        />
      </div>
    </el-card>

    <CreatePushDialog
      v-model:visible="createPushDialogVisible"
      @success="handleCreatePushSuccess"
    />
    <PushDetailDialog v-model:visible="pushDetailDialogVisible" :task="currentDetailTask" />
  </div>
</template>

<style lang="scss" scoped>
.push-management {
  height: 100%;
  display: flex;
  flex-direction: column;

  .table-card {
    flex: 1;
    display: flex;
    flex-direction: column;

    :deep(.el-card__body) {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 24px;
    }

    :deep(.el-table .el-table__cell) {
      height: 55px;
      padding: 0 !important;
    }

    :deep(.el-table--fit .el-table__inner-wrapper:before) {
      width: 0 !important;
    }

    :deep(.el-table__header) {
      .el-table__cell {
        color: #1d2129;
        font-weight: 600;
      }
    }

    :deep(.el-table__body-wrapper) {
      .el-table__cell {
        color: #1d2129;
        font-weight: 400;
      }
    }
  }

  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 16px;
    flex-wrap: wrap;
  }

  .page-title {
    font-size: 20px;
    font-weight: 600;
    color: #1d2129;
  }

  .filter-bar {
    display: flex;
    align-items: center;
    gap: 16px;
    flex-wrap: wrap;
  }

  .date-range-picker {
    width: 340px;
  }

  .status-cell {
    display: flex;
    align-items: center;
  }

  .status-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    margin-right: 8px;
    flex-shrink: 0;
  }

  .pagination-wrapper {
    margin-top: 16px;
  }
}

@media (max-width: 768px) {
  .push-management {
    .toolbar {
      align-items: flex-start;
    }

    .filter-bar {
      width: 100%;
    }

    .date-range-picker {
      width: 100%;
    }

    :deep(.el-select-v2),
    :deep(.el-select) {
      width: 100% !important;
    }
  }
}
</style>
