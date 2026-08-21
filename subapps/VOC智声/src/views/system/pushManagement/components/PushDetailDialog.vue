<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import AppDialog from '@/components/AppDialog.vue'
import { exportPushMessageUserInfo, getPushMessageUserInfoPage } from '@/api/system/pushManagement'
import type {
  PushMessageUserInfoExportParams,
  PushMessageUserInfoItem,
  PushMessageUserInfoQueryParams,
  PushTaskListItem,
  PushTaskStatusOption,
  PushUserStatusValue
} from '@/api/system/pushManagement/types'
import { useTable } from '@/hooks/useTable'
import { useUserStore } from '@/store'
import { Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useDownloadTaskDialog } from '@/hooks/useDownloadTaskDialog'

defineOptions({
  name: 'PushDetailDialog'
})

interface PushDetailForm {
  statusList: PushUserStatusValue[]
}

const props = defineProps<{
  task: PushTaskListItem | null
}>()

const visible = defineModel<boolean>('visible', { default: false })
const userStore = useUserStore()
const exporting = ref(false)
const { openDownloadTaskDialog } = useDownloadTaskDialog()

const statusOptions = computed<PushTaskStatusOption[]>(() => {
  const dictItems = userStore.getDictItems('push_user_status') || []

  return (dictItems || [])
    .map((item: any) => ({
      label: item.text || item.label,
      value: item.value,
      sort: Number(item.sort || 0)
    }))
    .sort((a: any, b: any) => a.sort - b.sort)
})

/**
 * 获取推送明细查询基础参数。
 * 列表接口在此基础上追加分页参数，导出接口直接复用该参数。
 * @returns 查询基础参数
 */
const buildPushUserInfoBaseParams = (): PushMessageUserInfoExportParams | null => {
  const batchId = props.task?.batchId
  if (!batchId) {
    return null
  }

  return {
    batchId,
    statusList: formData.value.statusList
  }
}

/**
 * 获取用户明细分页数据。
 * @returns 表格列表与总数
 */
const fetchPushUserInfoList = async (): Promise<{
  list: PushMessageUserInfoItem[]
  total: number
}> => {
  const baseParams = buildPushUserInfoBaseParams()
  if (!baseParams) {
    return {
      list: [],
      total: 0
    }
  }

  const params: PushMessageUserInfoQueryParams = {
    ...baseParams,
    pageNum: currentPage.value,
    pageSize: pageSize.value
  }
  const response = await getPushMessageUserInfoPage(params)
  const result = response.result || {}

  return {
    list: result.list || [],
    total: Number(result.total || 0)
  }
}

const {
  tableState: { loading, dataList, currentPage, pageSize, total },
  tableMethods,
  formData
} = useTable<PushDetailForm>({
  immediate: false,
  initialFormData: {
    statusList: []
  },
  fetchDataApi: fetchPushUserInfoList
})

const canExport = computed(() => Number(total.value || 0) > 0 && !loading.value)

/**
 * 筛选条件变化后刷新第一页明细。
 */
const handleFilterChange = async () => {
  if (currentPage.value !== 1) {
    currentPage.value = 1
    return
  }

  await tableMethods.getList()
}

/**
 * 导出推送明细。
 * 导出任务创建成功后，弹出统一下载任务提示；点击前往查看时关闭当前详情弹框。
 */
const handleExport = async () => {
  if (exporting.value) return

  if (!canExport.value) {
    ElMessage.warning('暂无可导出的数据')
    return
  }

  const params = buildPushUserInfoBaseParams()
  if (!params) {
    ElMessage.warning('缺少推送任务信息，无法导出')
    return
  }

  exporting.value = true
  try {
    const response = await exportPushMessageUserInfo(params)
    if (response.success) {
      await openDownloadTaskDialog({
        beforeNavigate: () => {
          visible.value = false
        }
      })
      return
    }

    ElMessage.error(response.message || '导出任务创建失败')
  } catch (error) {
    console.error('推送明细导出失败:', error)
    ElMessage.error(error instanceof Error ? error.message : '导出任务创建失败，请稍后重试')
  } finally {
    exporting.value = false
  }
}

/**
 * 打开弹框时初始化查询条件和分页。
 */
const initDialogTable = async () => {
  formData.value.statusList = []
  if (currentPage.value !== 1) {
    currentPage.value = 1
    return
  }

  await tableMethods.getList()
}

/**
 * 关闭弹框后清空旧列表，避免下次打开时短暂展示上一次任务数据。
 */
const clearDialogTable = () => {
  dataList.value = []
  total.value = 0
}

/**
 * 获取状态圆点颜色。
 * @param row 当前明细行
 * @returns 样式对象
 */
const getStatusStyle = (row: PushMessageUserInfoItem) => {
  const statusValue = row.statusCode

  if (statusValue === '1') {
    return { backgroundColor: '#00b42a' }
  }

  if (statusValue === '2') {
    return { backgroundColor: '#f53f3f' }
  }

  return { backgroundColor: '#86909c' }
}

watch(
  () => visible.value,
  async value => {
    if (value) {
      await initDialogTable()
      return
    }

    clearDialogTable()
  }
)
</script>

<template>
  <AppDialog
    v-model:visible="visible"
    width="960px"
    :show-footer="false"
    destroy-on-close
    class="push-detail-dialog"
  >
    <template #header>推送详情</template>

    <div class="push-detail-dialog__content">
      <div class="push-detail-dialog__toolbar">
        <el-select-v2
          v-model="formData.statusList"
          :options="statusOptions"
          placeholder="请选择状态"
          clearable
          filterable
          multiple
          collapse-tags
          :max-collapse-tags="1"
          class="push-detail-dialog__status-select"
          @change="handleFilterChange"
        />
        <el-button
          type="primary"
          :icon="Download"
          :loading="exporting"
          :disabled="!canExport || exporting"
          @click="handleExport"
        >
          导出数据
        </el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="dataList as PushMessageUserInfoItem[]"
        max-height="420px"
        class="push-detail-dialog__table"
      >
        <el-table-column type="index" label="#" width="56" align="center" />
        <el-table-column prop="userName" label="用户姓名" min-width="180" />
        <el-table-column prop="account" label="员工工号" min-width="180" />
        <el-table-column prop="updateTime" label="推送时间" min-width="180" />
        <el-table-column label="推送状态" min-width="140">
          <template #default="{ row }">
            <div class="push-detail-dialog__status-cell">
              <div class="push-detail-dialog__status-dot" :style="getStatusStyle(row)"></div>
              <span>{{ row.status }}</span>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="push-detail-dialog__pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="->,total, prev, pager, next, sizes"
        />
      </div>
    </div>
  </AppDialog>
</template>

<style scoped lang="scss">
.push-detail-dialog__content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 520px;
}

.push-detail-dialog__toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.push-detail-dialog__status-select {
  width: 220px;
}

.push-detail-dialog__table {
  flex: 1;

  :deep(.el-table__cell) {
    height: 48px;
    padding: 0 !important;
  }

  :deep(.el-table--fit .el-table__inner-wrapper:before) {
    width: 0 !important;
  }

  :deep(.el-table__header) {
    .el-table__cell {
      color: #1d2129;
      font-weight: 600;
      background: #f7f8fa;
    }
  }

  :deep(.el-table__body-wrapper) {
    .el-table__cell {
      color: #1d2129;
      font-weight: 400;
    }
  }
}

.push-detail-dialog__status-cell {
  display: flex;
  align-items: center;
}

.push-detail-dialog__status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 8px;
  flex-shrink: 0;
}

.push-detail-dialog__pagination {
  flex: 0 0 auto;
}

@media (max-width: 768px) {
  .push-detail-dialog__content {
    min-height: 440px;
  }

  .push-detail-dialog__toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .push-detail-dialog__status-select {
    width: 100%;
  }
}
</style>
