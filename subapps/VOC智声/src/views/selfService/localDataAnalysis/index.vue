<script setup lang="ts">
import { computed, h, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import {
  deleteLocalDataAnalysisTask,
  exportLocalDataAnalysisResult,
  findLocalDataAnalysisDataSource,
  findLocalDataAnalysisVisibleUserList,
  startLocalDataAnalysisTask
} from '@/api/localDataAnalysis'
import type { LocalDataAnalysisDataSourceItem } from '@/api/localDataAnalysis/types'
import { useTable } from '@/hooks/useTable'
import { LOCAL_DATA_ANALYSIS_BTN_MAP } from '@/constants/btnPermMap'
import { hasPermission } from '@/utils/permission'
import { downloadFromBlob } from '@/utils/download'
import { appDialogConfirm } from '@/components/appDialog'
import SvgIcon from '@/components/UI/SvgIcon/index.vue'
import CreateTaskDialog from './components/CreateTaskDialog.vue'

defineOptions({
  name: 'localDataAnalysis'
})

interface UserOption {
  userId: string
  userName: string
}

interface LocalDataAnalysisForm {
  createUserIdList: string[]
  dataSourceName: string
}

const DATA_SOURCE_NAME_MAX_LENGTH = 50
const POLLING_INTERVAL = 60 * 1000

/**
 * 本地数据分析状态码枚举。
 */
enum LocalDataAnalysisStatusCode {
  UNPROCESSED = '0',
  PROCESSING = '1',
  COMPLETED = '2',
  FAILED = '3'
}

const userOptions = ref<UserOption[]>([])
const createTaskDialogVisible = ref(false)
const exportLoadingMap = reactive<Record<string, boolean>>({})
const pollingTimer = ref<number | null>(null)

/**
 * 获取当前账号是否具备查看全部数据权限。
 * @returns 是否可以查看全部用户创建的数据源
 */
const getIsAllVisible = () => {
  if (hasPermission(LOCAL_DATA_ANALYSIS_BTN_MAP.SELECT_ALL)) {
    return true
  }

  return false
}

const showCreatorFilter = computed(() => getIsAllVisible())

const statusConfigMap: Record<
  LocalDataAnalysisStatusCode,
  {
    color: string
  }
> = {
  [LocalDataAnalysisStatusCode.UNPROCESSED]: {
    color: '#c9cdd4'
  },
  [LocalDataAnalysisStatusCode.PROCESSING]: {
    color: '#1677ff'
  },
  [LocalDataAnalysisStatusCode.COMPLETED]: {
    color: '#00b42a'
  },
  [LocalDataAnalysisStatusCode.FAILED]: {
    color: '#f53f3f'
  }
}

/**
 * 获取创建人员筛选项。该接口返回结构沿用现有人员列表处理方式。
 */
const getUserList = async () => {
  try {
    const response = await findLocalDataAnalysisVisibleUserList(getIsAllVisible())
    if (response.success) {
      userOptions.value = (response.result || []).map(
        (user: { userId: string; userName: string; employeeId: string }) => {
          return { userId: user.userId, userName: [user.userName, user.employeeId || ''].join('-') }
        }
      )
      return
    }

    userOptions.value = []
    ElMessage.error(response.message || '获取创建人员列表失败')
  } catch (error) {
    console.error('获取创建人员列表失败:', error)
    userOptions.value = []
  }
}

/**
 * 执行列表查询。
 * @returns 查询结果数据
 */
const fetchLocalDataAnalysisList = async (): Promise<{
  list: LocalDataAnalysisDataSourceItem[]
  total: number
}> => {
  const params = {
    createUserIdList: formData.value.createUserIdList,
    dataSourceName: formData.value.dataSourceName.trim(),
    pageNum: currentPage.value,
    pageSize: pageSize.value,
    isAllVisible: getIsAllVisible()
  }

  const response = await findLocalDataAnalysisDataSource(params)
  if (response.success) {
    return {
      list: response.result.list,
      total: response.result.total
    }
  }

  ElMessage.error(response.message || '获取数据源列表失败')
  throw new Error(response.message || '获取数据源列表失败')
}

/**
 * 将查询回到第一页并刷新列表。
 * 当前页不为 1 时只触发分页切页，避免和手动刷新形成双请求。
 */
const reloadFirstPageList = async () => {
  if (currentPage.value !== 1) {
    currentPage.value = 1
    return
  }

  await tableMethods.handleQuery()
}

const {
  tableState: { loading, dataList, currentPage, pageSize, total },
  tableMethods,
  formData
} = useTable<LocalDataAnalysisForm>({
  immediate: false,
  initialFormData: {
    createUserIdList: [],
    dataSourceName: ''
  },
  fetchDataApi: fetchLocalDataAnalysisList
})

/**
 * 执行查询并回到第一页。
 */
const handleSearch = async () => {
  await reloadFirstPageList()
}

/**
 * 获取状态圆点样式。
 * @param status 当前状态
 * @returns 圆点背景色样式
 */
const getStatusTagStyle = (statusCode: LocalDataAnalysisStatusCode) => {
  return {
    backgroundColor: statusConfigMap[statusCode]?.color || ''
  }
}

/**
 * 手动开始未开始任务。
 * @param row 当前任务
 */
const handleStart = async (row: LocalDataAnalysisDataSourceItem) => {
  try {
    const response = await startLocalDataAnalysisTask({ batchId: row.batchId })
    if (response.success) {
      ElMessage.success(response.message || '任务已开始处理')
      await tableMethods.refresh()
      return
    }

    ElMessage.error(response.message || '任务开始失败')
  } catch (error) {
    console.error('开始处理任务失败:', error)
    ElMessage.error(error instanceof Error ? error.message : '任务开始失败')
  }
}

/**
 * 导出已完成任务结果。
 * @param row 当前任务
 */
const handleExport = async (row: LocalDataAnalysisDataSourceItem) => {
  if (exportLoadingMap[row.batchId]) {
    return
  }

  exportLoadingMap[row.batchId] = true

  try {
    const response = await exportLocalDataAnalysisResult({ batchId: row.batchId })
    if (response.success) {
      downloadFromBlob(response.result, `${row.dataName}.xlsx`)
      ElMessage.success('导出成功')
      return
    }

    ElMessage.error(response.message || '导出失败')
  } catch (error) {
    console.error('导出任务结果失败:', error)
    ElMessage.error(error instanceof Error ? error.message : '导出失败')
  } finally {
    exportLoadingMap[row.batchId] = false
  }
}

/**
 * 判断当前行是否处于导出中。
 * @param batchId 导入批次 ID
 * @returns 是否导出中
 */
const getExportLoading = (batchId: string) => {
  return Boolean(exportLoadingMap[batchId])
}

/**
 * 判断列表中是否存在处理中任务。
 * @returns 是否存在处理中任务
 */
const hasProcessingItems = () => {
  if (!Array.isArray(dataList.value) || dataList.value.length === 0) {
    return false
  }

  return dataList.value.some((item: any) => item.statusCode === LocalDataAnalysisStatusCode.PROCESSING)
}

/**
 * 停止任务列表轮询。
 */
const stopPolling = () => {
  if (pollingTimer.value !== null) {
    window.clearInterval(pollingTimer.value)
    pollingTimer.value = null
  }
}

/**
 * 启动任务列表轮询。
 * 仅在存在处理中任务时每分钟刷新一次列表。
 */
const startPolling = () => {
  if (pollingTimer.value !== null || !hasProcessingItems()) {
    return
  }

  pollingTimer.value = window.setInterval(async () => {
    await tableMethods.handleQuery()
  }, POLLING_INTERVAL)
}

/**
 * 根据当前列表状态同步轮询启停。
 */
const syncPollingState = () => {
  if (hasProcessingItems()) {
    startPolling()
    return
  }

  stopPolling()
}

/**
 * 删除任务。
 * @param row 当前任务
 */
const handleDelete = async (row: LocalDataAnalysisDataSourceItem) => {
  try {
    await appDialogConfirm(
      () =>
        h('div', { class: 'flex items-center' }, [
          h(SvgIcon, { name: 'info-circle-filled', width: '20px', height: '20px' }),
          h('span', { class: 'ml-8' }, '是否删除该任务？')
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
    const response = await deleteLocalDataAnalysisTask({ id: row.id })
    if (response.success) {
      ElMessage.success(response.message || '删除成功')
      await tableMethods.refresh()
      return
    }

    ElMessage.error(response.message || '删除失败')
  } catch (error) {
    console.error('删除任务失败:', error)
    ElMessage.error(error instanceof Error ? error.message : '删除失败')
  }
}

/**
 * 打开新建任务弹框。
 */
const handleCreateTask = () => {
  createTaskDialogVisible.value = true
}

/**
 * 新建任务成功后刷新列表并关闭弹框。
 */
const handleCreateTaskSuccess = async () => {
  createTaskDialogVisible.value = false
  await reloadFirstPageList()
}

watch(
  () => dataList.value,
  () => {
    syncPollingState()
  },
  { deep: true }
)

onMounted(async () => {
  await getUserList()
  await tableMethods.getList()
  syncPollingState()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<template>
  <div class="local-data-analysis">
    <el-card class="table-card" shadow="never">
      <div class="flex-between items-center mb-24">
        <div class="text-h3 page-title">任务列表</div>
        <div class="filter-bar">
          <el-select-v2
            v-if="showCreatorFilter"
            v-model="formData.createUserIdList"
            :options="userOptions"
            :props="{ value: 'userId', label: 'userName' }"
            placeholder="创建人员"
            filterable
            clearable
            multiple
            collapse-tags
            :max-collapse-tags="1"
            style="width: 200px"
            @change="handleSearch"
          />

          <el-input
            v-model="formData.dataSourceName"
            placeholder="请输入关键词搜索"
            clearable
            :maxlength="DATA_SOURCE_NAME_MAX_LENGTH"
            style="width: 200px"
            :suffix-icon="Search"
            @change="handleSearch"
          />

          <el-button type="primary" @click="handleCreateTask">新建任务</el-button>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="dataList as LocalDataAnalysisDataSourceItem[]"
        max-height="calc(100vh - 84px - 48px - 106px - 32px - 10px)"
        class="flex-auto overflow-auto"
      >
        <el-table-column type="index" label="#" width="56" align="center" />
        <el-table-column prop="dataName" label="任务名称" min-width="180" />
        <el-table-column prop="taskInfo" label="任务明细" min-width="260" />
        <el-table-column label="创建人员" min-width="200">
          <template #default="{ row }"> {{ row.createUser }} </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" sortable />
        <el-table-column label="当前状态" width="120">
          <template #default="{ row }">
            <div class="flex-y-center">
              <div class="status-icon mr-8" :style="getStatusTagStyle(row.statusCode)"></div>
              {{ row.status }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              :disabled="row.statusCode !== LocalDataAnalysisStatusCode.UNPROCESSED"
              @click="handleStart(row)"
            >
              处理
            </el-button>
            <el-button
              link
              type="primary"
              :loading="getExportLoading(row.batchId)"
              :disabled="
                row.statusCode !== LocalDataAnalysisStatusCode.COMPLETED || getExportLoading(row.batchId)
              "
              @click="handleExport(row)"
            >
              导出
            </el-button>
            <el-button
              link
              type="primary"
              :disabled="row.statusCode === LocalDataAnalysisStatusCode.PROCESSING"
              @click="handleDelete(row)"
            >
              删除
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

    <CreateTaskDialog
      v-model:visible="createTaskDialogVisible"
      @success="handleCreateTaskSuccess"
    />
  </div>
</template>

<style lang="scss" scoped>
.local-data-analysis {
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

  .page-title {
    font-weight: 600;
  }

  .filter-bar {
    display: flex;
    gap: 16px;
  }

  .status-icon {
    width: 6px;
    height: 6px;
    border-radius: 50%;
  }

  .pagination-wrapper {
    margin-top: 16px;
  }
}

@media (max-width: 768px) {
  .local-data-analysis {
    .filter-bar {
      width: 100%;
      flex-direction: column;
      gap: 12px;
    }

    :deep(.el-select),
    :deep(.el-input) {
      width: 100% !important;
    }

    .pagination-wrapper {
      text-align: center;
    }
  }
}
</style>
