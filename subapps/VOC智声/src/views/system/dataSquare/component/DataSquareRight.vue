<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowDown, Search } from '@element-plus/icons-vue'
import { useTable } from '@/hooks/useTable'
import { getDataPlazaReportList } from '@/api/dataPlaza'
import type { DataPlazaReportItem } from '@/api/dataPlaza/types'
import { useUserStore } from '@/store'
import BatchMoveDialog from './BatchMoveDialog.vue'
import ReportPreviewDialog from './ReportPreviewDialog.vue'
import ReportDialog from './ReportDialog.vue'
import { useReportActions } from '../hooks/useReportActions'

defineOptions({
  name: 'DataSquareRight'
})

interface CategoryRestorePayload {
  selectedParentId: string
  selectedCategoryId: string
}

interface Emits {
  (e: 'restore-selection', payload: CategoryRestorePayload): void
  (e: 'report-changed'): void
}

interface Props {
  categoryId?: string | null
  categoryChangeVersion?: number
}

const props = withDefaults(defineProps<Props>(), {
  categoryId: null,
  categoryChangeVersion: 0
})

const emit = defineEmits<Emits>()

interface ReportFilterForm {
  categoryId: string
  isPinned?: string | number
  publishStatus?: string | number
  reportName: string
}

interface DictItemOption {
  text?: string
  code?: string | number
}

const userStore = useUserStore()

const reportDialogVisible = ref(false)
const reportDialogMode = ref<'create' | 'edit'>('create')
const reportDialogEditData = ref<DataPlazaReportItem | null>(null)
const batchMoveDialogVisible = ref(false)
const previewDialogVisible = ref(false)
const previewReport = ref<DataPlazaReportItem | null>(null)

const pinToTopOptions = computed(() => userStore.getDictItems('pin_to_top') || [])
const onlineStatusOptions = computed(() => userStore.getDictItems('publish_status') || [])

/**
 * 删除后修正分页，避免当前页被删空后仍停留在空页。
 * @param deletedCount 本次删除数量
 */
const adjustPageAfterDelete = (deletedCount: number) => {
  const dataLength = dataList.value?.length || 0
  if (currentPage.value > 1 && deletedCount >= dataLength) {
    currentPage.value -= 1
  }
}

/**
 * 获取报告分页列表。
 */
const fetchReportList = async (): Promise<{ list: DataPlazaReportItem[]; total: number }> => {
  const response = await getDataPlazaReportList({
    pageNum: currentPage.value,
    pageSize: pageSize.value,
    categoryId: formData.value.categoryId || undefined,
    isPinned: formData.value.isPinned,
    publishStatus: formData.value.publishStatus,
    reportName: formData.value.reportName.trim() || undefined
  })

  return {
    list: response.result?.list || [],
    total: response.result?.total || 0
  }
}

const {
  tableState: { loading, dataList, currentPage, pageSize, total },
  tableMethods,
  formData,
  selection
} = useTable<ReportFilterForm>({
  immediate: false,
  initialFormData: {
    categoryId: '',
    isPinned: undefined,
    publishStatus: undefined,
    reportName: ''
  },
  fetchDataApi: fetchReportList
})

const {
  getActionColumnWidth,
  handleTogglePin,
  handleTogglePublishStatus,
  handleCopyReport,
  handleDeleteReport,
  handleBatchPublish,
  handleBatchUnpublish,
  handleBatchDelete
} = useReportActions({
  refreshList: tableMethods.getList,
  adjustPageAfterDelete
})

const selectedRows = computed(() => selection.value as DataPlazaReportItem[])

const canBatchPublish = computed(() => {
  return (
    selectedRows.value.length > 0 &&
    selectedRows.value.every((item: any) => item.publishStatus === 0)
  )
})

const canBatchUnpublish = computed(() => {
  return (
    selectedRows.value.length > 0 &&
    selectedRows.value.every((item: any) => item.publishStatus === 1)
  )
})

const canBatchMove = computed(() => {
  return selectedRows.value.length > 0
})

const canBatchDelete = computed(() => {
  return (
    selectedRows.value.length > 0 &&
    selectedRows.value.every((item: any) => item.publishStatus === 0)
  )
})

/**
 * 从第一页刷新列表，避免筛选条件变化后停留在空页。
 */
const reloadFirstPageList = async () => {
  if (currentPage.value !== 1) {
    currentPage.value = 1
    return
  }
  await tableMethods.getList()
}

/**
 * 同步父级选中的分类，并刷新报告列表。
 */
const syncCategoryFilter = () => {
  formData.value.categoryId = props.categoryId || ''
  void reloadFirstPageList()
}

/**
 * 右侧筛选项变化后刷新表格。
 */
const handleFilterChange = () => {
  void reloadFirstPageList()
}

/**
 * 打开移动端报告详情预览。
 * @param row 当前报告行
 */
const handlePreviewReport = (row: DataPlazaReportItem) => {
  if (!row.id) {
    ElMessage.warning('报告信息缺失，无法预览')
    return
  }

  previewReport.value = row
  previewDialogVisible.value = true
}

/**
 * 打开新建报告弹窗。
 */
const handleCreateReport = () => {
  reportDialogMode.value = 'create'
  reportDialogEditData.value = null
  reportDialogVisible.value = true
}

/**
 * 打开编辑报告弹窗。
 * @param row 当前报告行
 */
const handleEditReport = (row: DataPlazaReportItem) => {
  reportDialogMode.value = 'edit'
  reportDialogEditData.value = row
  reportDialogVisible.value = true
}

/**
 * 处理单条复制成功后的左右联动刷新。
 * @param row 当前报告行
 */
const onCopyReport = async (row: DataPlazaReportItem) => {
  const success = await handleCopyReport(row)
  if (success) {
    emit('report-changed')
  }
}

/**
 * 处理单条删除成功后的左右联动刷新。
 * @param row 当前报告行
 */
const onDeleteReport = async (row: DataPlazaReportItem) => {
  const previousTotal = total.value
  await handleDeleteReport(row)

  if (total.value < previousTotal) {
    emit('report-changed')
  }
}

/**
 * 报告保存成功后关闭弹窗，并通知父层刷新左侧分类树。
 * 右侧列表刷新交给左侧分类树回填后的 change 事件统一触发，避免重复请求。
 */
const handleReportDialogSuccess = async (payload: CategoryRestorePayload) => {
  reportDialogVisible.value = false
  emit('restore-selection', payload)
}

/**
 * 处理批量发布。
 */
const onBatchPublish = async () => {
  if (!selectedRows.value.length) {
    ElMessage.warning('请选择报告')
    return
  }

  if (!canBatchPublish.value) {
    ElMessage.warning('请选择全部为已下架状态的报告')
    return
  }

  await handleBatchPublish(selectedRows.value)
}

/**
 * 处理批量下架。
 */
const onBatchUnpublish = async () => {
  if (!selectedRows.value.length) {
    ElMessage.warning('请选择报告')
    return
  }

  if (!canBatchUnpublish.value) {
    ElMessage.warning('请选择全部为已上架状态的报告')
    return
  }

  await handleBatchUnpublish(selectedRows.value)
}

/**
 * 打开批量移动弹窗前校验选中项。
 */
const handleOpenBatchMoveDialog = () => {
  if (!selectedRows.value.length) {
    ElMessage.warning('请选择报告')
    return
  }

  batchMoveDialogVisible.value = true
}

/**
 * 处理批量删除。
 */
const onBatchDelete = async () => {
  if (!selectedRows.value.length) {
    ElMessage.warning('请选择报告')
    return
  }

  if (!canBatchDelete.value) {
    ElMessage.warning('批量删除仅支持全部为已下架状态的报告')
    return
  }

  const success = await handleBatchDelete(selectedRows.value)
  if (success) {
    emit('report-changed')
  }
}

/**
 * 批量移动成功后按目标分类恢复左侧选中态。
 * 右侧列表刷新交给左侧分类树回填后的 change 事件统一触发。
 */
const handleBatchMoveSuccess = async (payload: CategoryRestorePayload) => {
  batchMoveDialogVisible.value = false
  emit('restore-selection', payload)
}

/**
 * 获取操作人展示文案，优先使用更新人，缺失时回退创建人。
 * @param row 报告行数据
 * @returns 操作人名称
 */
const getOperatorName = (row: DataPlazaReportItem) => {
  return row.updateBy || row.createBy || ''
}

/**
 * 根据字典项 code 匹配状态文案。
 * @param dictKey 字典编码
 * @param status 当前状态值
 * @returns 状态文案
 */
const getDictStatusText = (dictKey: string, status: any) => {
  const dictItems = (userStore.getDictItems(dictKey) || []) as DictItemOption[]
  const matchedItem = dictItems.find((item: any) => item.value == status)
  return matchedItem?.text || ''
}

/**
 * 获取上架状态展示文案。
 * @param status 上架状态值
 * @returns 上架状态文案
 */
const getOnlineStatusText = (status: DataPlazaReportItem['publishStatus']) => {
  return getDictStatusText('publish_status', status) || (status === 1 ? '已上架' : '已下架')
}

/**
 * 获取上架状态点颜色。
 */
const getOnlineStatusStyle = (status: DataPlazaReportItem['publishStatus']) => {
  const colorMap: Record<DataPlazaReportItem['publishStatus'], string> = {
    1: '#1677ff',
    0: '#c9cdd4'
  }
  return { backgroundColor: colorMap[status] }
}

watch(
  () => props.categoryChangeVersion,
  value => {
    if (!value) {
      return
    }
    syncCategoryFilter()
  }
)
</script>

<template>
  <section class="report-panel">
    <div class="panel-toolbar">
      <div class="text-h3 panel-title">报告列表</div>
      <div class="filter-bar">
        <el-select
          v-model="formData.isPinned"
          clearable
          placeholder="是否置顶"
          style="width: 156px"
          @change="handleFilterChange"
        >
          <el-option
            v-for="item in pinToTopOptions"
            :key="item.value"
            :label="item.text"
            :value="item.value"
          />
        </el-select>
        <el-select
          v-model="formData.publishStatus"
          clearable
          placeholder="上架状态"
          style="width: 156px"
          @change="handleFilterChange"
        >
          <el-option
            v-for="item in onlineStatusOptions"
            :key="item.value"
            :label="item.text"
            :value="item.value"
          />
        </el-select>
        <el-input
          v-model="formData.reportName"
          clearable
          placeholder="请输入关键词搜索"
          style="width: 200px"
          :suffix-icon="Search"
          @change="handleFilterChange"
          @keyup.enter="handleFilterChange"
        />
        <el-dropdown trigger="click">
          <el-button :disabled="!selectedRows.length">
            批量操作
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item v-if="canBatchPublish" @click="onBatchPublish">
                批量发布
              </el-dropdown-item>
              <el-dropdown-item v-if="canBatchUnpublish" @click="onBatchUnpublish">
                批量下架
              </el-dropdown-item>
              <el-dropdown-item v-if="canBatchMove" @click="handleOpenBatchMoveDialog">
                批量移动
              </el-dropdown-item>
              <el-dropdown-item v-if="canBatchDelete" @click="onBatchDelete">
                批量删除
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button type="primary" @click="handleCreateReport">新建报告</el-button>
      </div>
    </div>

    <el-table
      v-loading="loading"
      :data="dataList as DataPlazaReportItem[]"
      max-height="calc(100vh - 84px - 48px - 106px - 32px - 10px)"
      class="flex-auto overflow-auto"
      @selection-change="(rows: DataPlazaReportItem[]) => tableMethods.handleSelectionChange(rows)"
    >
      <el-table-column type="selection" width="56" align="center" fixed="left" />
      <el-table-column
        prop="reportName"
        label="报告名称"
        min-width="220"
        :show-overflow-tooltip="{ popperClass: 'text-tooltip-light' }"
      />
      <el-table-column prop="categoryName" label="所属分类" min-width="140" />
      <el-table-column label="操作人" min-width="120">
        <template #default="{ row }">
          {{ getOperatorName(row) }}
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" min-width="170" />
      <el-table-column prop="createTime" label="创建时间" min-width="170" />
      <el-table-column label="是否置顶" width="100">
        <template #default="{ row }">
          {{ getDictStatusText('pin_to_top', row.isPinned) }}
        </template>
      </el-table-column>
      <el-table-column label="上架状态" width="120" fixed="right">
        <template #default="{ row }">
          <div class="status-cell">
            <span class="status-dot" :style="getOnlineStatusStyle(row.publishStatus)"></span>
            <span>{{ getOnlineStatusText(row.publishStatus) }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="340" fixed="right">
        <template #default="{ row }">
          <div class="action-cell" :style="{ minWidth: `${getActionColumnWidth(row)}px` }">
            <el-button link type="primary" @click="handleTogglePin(row)">
              {{ row.isPinned === 1 ? '取消置顶' : '置顶' }}
            </el-button>
            <el-button link type="primary" @click="handlePreviewReport(row)">预览</el-button>
            <el-button link type="primary" @click="handleEditReport(row)">编辑</el-button>
            <el-button link type="primary" @click="handleTogglePublishStatus(row)">
              {{ row.publishStatus === 1 ? '下架' : '发布' }}
            </el-button>
            <el-button link type="primary" @click="onCopyReport(row)">复制</el-button>
            <el-button
              v-if="row.publishStatus === 0"
              link
              type="primary"
              @click="onDeleteReport(row)"
            >
              删除
            </el-button>
          </div>
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

    <ReportDialog
      v-model:visible="reportDialogVisible"
      :mode="reportDialogMode"
      :category-id="formData.categoryId"
      :edit-data="reportDialogEditData"
      @success="handleReportDialogSuccess"
    />

    <BatchMoveDialog
      v-model:visible="batchMoveDialogVisible"
      :selection="selectedRows"
      @success="handleBatchMoveSuccess"
    />

    <ReportPreviewDialog v-model:visible="previewDialogVisible" :report="previewReport" />
  </section>
</template>

<style lang="scss" scoped>
.report-panel {
  flex: 1;
  min-width: 0;
  min-height: 0;
  padding-left: 24px;
  display: flex;
  flex-direction: column;
}

.panel-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;
}

.panel-title {
  font-weight: 600;
  color: #1d2129;
  white-space: nowrap;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.status-cell {
  display: flex;
  align-items: center;
}

.status-dot {
  width: 6px;
  height: 6px;
  margin-right: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.action-cell {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.action-cell :deep(.el-button + .el-button) {
  margin-left: 12px;
}

.pagination-wrapper {
  margin-top: 16px;
}

@media (max-width: 1200px) {
  .report-panel {
    min-width: 760px;
  }
}
</style>
