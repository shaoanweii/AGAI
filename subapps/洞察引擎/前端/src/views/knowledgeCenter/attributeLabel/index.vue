<script setup lang="ts">
import { computed, nextTick, onMounted, provide, reactive, ref } from 'vue'
import { useTable } from '@/hooks/table'
import useConditions from '@/hooks/useConditions'
import { computedCardHeight } from '@/utils'
import { ArrowDown, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { TableInstance } from 'element-plus'
import { attributeLabelEnableStatus, batchChangeAttributeLabelStatus } from '@/api/attributeLabel'
import AttributeLabelFormDialog from './components/AttributeLabelFormDialog.vue'

defineOptions({
  name: 'KnowledgeCenterAttributeLabel'
})

interface AttributeLabelQueryForm {
  name: string
  status: string
}

interface StatusOption {
  key: Api.Common.EnableStatus
  value: string
}

const queryForm = ref<AttributeLabelQueryForm>({
  name: '',
  status: ''
})

const tableRef = ref<TableInstance>()
const multipleSelection = ref<Api.AttributeLabel.RecordItem[]>([])
const currentOrder = ref('')
const { conditions } = useConditions({ url: '/insights/attributeLabel/conditions' })
const attributeLabelDialogVisible = ref(false)
const currentEditingRecord = ref<Api.AttributeLabel.RecordItem | null>(null)
const batchDialogState = reactive({
  visible: false,
  status: '' as Api.Common.EnableStatus | '',
  title: '',
  message: ''
})
const { table, handleSizeChange, handleCurrentChange, getFirstPageTableData, refreshTableData } =
  useTable(
    {
      method: 'POST',
      url: '/insights/attributeLabel/findAttributeLabelList',
      pageSize: 10
    },
    res => {
      const pageResult = res.result || {}
      return {
        list: Array.isArray(pageResult.records) ? pageResult.records : [],
        total: Number(pageResult.total || 0)
      }
    }
  )

const statusOptions = computed<StatusOption[]>(() => {
  const options = Array.isArray(conditions.stopOrEnable) ? conditions.stopOrEnable : []
  return options
    .map(item => ({
      key: String(item?.key ?? '').trim() as Api.Common.EnableStatus,
      value: String(item?.value ?? '').trim()
    }))
    .filter(item => Boolean(item.key) && Boolean(item.value))
})

/**
 * 当前页勾选项仅用于批量操作，统一在这里收口去重后的有效ID集合。
 */
const selectedIds = computed(() => {
  const ids = multipleSelection.value.map(item => String(item.id || '').trim()).filter(Boolean)
  return Array.from(new Set(ids))
})

/**
 * 查询、重置或批量提交后都需要清空表格勾选，避免页面展示与内部状态不一致。
 */
const clearTableSelection = () => {
  nextTick(() => {
    tableRef.value?.clearSelection()
  })
}

/**
 * 统一构建查询参数，避免空字符串进入接口造成筛选口径不一致。
 */
const buildQueryParams = (): Api.AttributeLabel.QueryParams => {
  const trimmedName = queryForm.value.name.trim()
  return {
    id: '',
    name: trimmedName || '',
    status: queryForm.value.status || '',
    createUser: '',
    updateUser: '',
    createTime: '',
    updateTime: '',
    ids: [],
    order: currentOrder.value || ''
  }
}

/**
 * 同步查询条件到表格，并清空当前勾选，避免旧选择污染新结果集。
 */
const syncTableFilter = () => {
  table.filter = buildQueryParams()
  multipleSelection.value = []
  clearTableSelection()
}

/**
 * 查询时统一回到第一页，保证筛选与分页结果一致。
 */
const query = () => {
  syncTableFilter()
  void getFirstPageTableData()
}

/**
 * 重置筛选条件后恢复默认列表口径，同时清空当前勾选项。
 */
const reset = () => {
  queryForm.value = {
    name: '',
    status: ''
  }
  currentOrder.value = ''
  syncTableFilter()
  void getFirstPageTableData()
}

/**
 * 页面统一持有弹框开关和编辑数据，避免新增与编辑走出两套状态流。
 */
const openCreateDialog = () => {
  currentEditingRecord.value = null
  attributeLabelDialogVisible.value = true
}

/**
 * 编辑时仅透传当前行必要字段，减少弹框对列表字段结构的依赖。
 */
const openEditDialog = (row: Api.AttributeLabel.RecordItem) => {
  currentEditingRecord.value = {
    id: row.id,
    name: row.name,
    status: row.status,
    statusName: row.statusName
  }
  attributeLabelDialogVisible.value = true
}

/**
 * 保存后刷新当前列表页；若当前页已越界，则 useTable 内部会自动回退到有效页码。
 */
const handleFormSuccess = () => {
  attributeLabelDialogVisible.value = false
  multipleSelection.value = []
  clearTableSelection()
  void refreshTableData()
}

/**
 * 列表勾选变化时同步当前页记录，供批量状态修改直接复用。
 */
const handleSelectionChange = (rows: Api.AttributeLabel.RecordItem[]) => {
  multipleSelection.value = rows
}

/**
 * 批量操作统一复用状态字典，既保证文案与单条表单一致，也避免前端写死状态枚举。
 */
const handleBatchCommand = (action: StatusOption) => {
  if (!selectedIds.value.length) {
    ElMessage.warning('请先选择需要操作的属性标签')
    return
  }

  batchDialogState.status = action.key
  batchDialogState.title = `批量${action.value}`
  batchDialogState.message = `是否确认批量${action.value}选中属性标签？`
  batchDialogState.visible = true
}

/**
 * 批量状态提交前再次校验选中ID，避免筛选切换后残留旧状态导致误操作。
 */
const handleBatchStatusConfirm = async ({ close }: { close: () => void }) => {
  if (!selectedIds.value.length) {
    ElMessage.warning('请先选择需要操作的属性标签')
    return
  }

  try {
    const res = await batchChangeAttributeLabelStatus({
      status: batchDialogState.status as Api.Common.EnableStatus,
      ids: selectedIds.value
    })
    ElMessage.success(res?.message || '操作成功')
    batchDialogState.visible = false
    multipleSelection.value = []
    close()
    clearTableSelection()
    await refreshTableData(false)
  } catch (error: any) {
    console.error('批量修改属性标签状态失败', error)
    ElMessage.error(error?.message || '批量操作失败，请稍后重试')
  }
}

/**
 * 将前端排序事件转换成后端 order 字段，避免前端本地排序干扰分页。
 */
const handleSortChange = ({
  prop,
  order
}: {
  prop?: string
  order?: 'ascending' | 'descending' | null
}) => {
  if (!prop || !order) {
    currentOrder.value = ''
  } else {
    currentOrder.value = `${prop} ${order === 'ascending' ? 'asc' : 'desc'}`
  }
  query()
}

/**
 * 启用状态统一使用圆点样式表达，和知识中心其他列表视觉保持一致。
 */
const resolveStatusClass = (status: string | undefined) => {
  return String(status || '') === attributeLabelEnableStatus.ENABLED
    ? 'status-dot--success'
    : 'status-dot--disabled'
}

/**
 * 文案兜底统一转为短横线，避免列表出现空白单元格。
 */
const formatCellText = (value: unknown) => {
  if (value === 0) return '0'
  if (value === null || value === undefined) return '-'
  const text = String(value).trim()
  return text || '-'
}

onMounted(() => {
  syncTableFilter()
  void getFirstPageTableData()
})

provide('conditions', conditions)
</script>

<template>
  <div class="attribute-label-page">
    <FtCard title="筛选条件" model="titleOperation" clear-content-top-padding>
      <div class="filter-layout">
        <div class="filter-layout__content">
          <el-form :model="queryForm" layout="inline">
            <el-row class="w-full" :gutter="24">
              <el-col :span="8">
                <el-form-item label="标签名称">
                  <el-input
                    v-model.trim="queryForm.name"
                    placeholder="请输入"
                    :maxlength="30"
                    clearable
                    @keyup.enter="query"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="启用状态">
                  <el-select v-model="queryForm.status" placeholder="不限" clearable>
                    <el-option
                      v-for="item in statusOptions"
                      :key="item.key"
                      :label="item.value"
                      :value="item.key"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>
        <div class="filter-layout__actions">
          <el-button type="primary" @click="query">
            <el-icon class="mr-8"><Search /></el-icon>
            查询
          </el-button>
          <el-button color="#F2F3F5" @click="reset">
            <el-icon class="mr-8"><RefreshRight /></el-icon>
            重置
          </el-button>
        </div>
      </div>
    </FtCard>

    <FtCard
      title="标签列表"
      model="titleOperation"
      clear-content-top-padding
      class="mt-24"
      :style="computedCardHeight(175)"
    >
      <template #extra>
        <el-dropdown
          placement="bottom-end"
          :disabled="selectedIds.length === 0"
          @command="handleBatchCommand"
        >
          <el-button :disabled="multipleSelection.length === 0">
            批量操作
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item v-for="item in statusOptions" :key="item.key" :command="item">
                {{ item.value }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button class="ml-8" type="primary" @click="openCreateDialog">
          <el-icon class="mr-8"><Plus /></el-icon>
          新建标签
        </el-button>
      </template>

      <div class="table-container">
        <el-table
          ref="tableRef"
          v-loading="table.loading"
          :data="table.list || []"
          row-key="id"
          style="width: 100%"
          height="100%"
          @selection-change="handleSelectionChange"
          @sort-change="handleSortChange"
        >
          <el-table-column type="selection" width="56" align="center" />
          <el-table-column prop="name" label="标签名称" min-width="220" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="text-ellipsis">{{ formatCellText(row.name) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="updateUser" label="操作人" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="text-ellipsis">{{ formatCellText(row.updateUser) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="updateTime" label="更新时间" min-width="190" sortable="custom">
            <template #default="{ row }">
              <span>{{ formatCellText(row.updateTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" min-width="190" sortable="custom">
            <template #default="{ row }">
              <span>{{ formatCellText(row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="statusName" label="启用状态" min-width="120">
            <template #default="{ row }">
              <div class="status-cell">
                <span :class="['status-dot', resolveStatusClass(row.status)]"></span>
                <span>{{ formatCellText(row.statusName || row.status) }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click="openEditDialog(row)"> 编辑 </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <el-pagination
        v-model:current-page="table.pageNum"
        v-model:page-size="table.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="table.total"
        layout="total, sizes, prev, pager, next"
        class="pagination"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />

      <AttributeLabelFormDialog
        v-model:visible="attributeLabelDialogVisible"
        :form-data="currentEditingRecord"
        @success="handleFormSuccess"
      />

      <AppDialog
        v-model:visible="batchDialogState.visible"
        :title="batchDialogState.title"
        width="400px"
        :confirm="handleBatchStatusConfirm"
      >
        <div class="batch-status-content">
          <SvgIcon name="info-circle-filled" style="width: 20px; height: 20px" color="#1677ff" />
          <div class="batch-status-content__text">{{ batchDialogState.message }}</div>
        </div>
      </AppDialog>
    </FtCard>
  </div>
</template>

<style scoped lang="scss">
.filter-layout {
  display: flex;
  align-items: stretch;
}

.filter-layout__content {
  flex: 1;
  min-width: 0;
}

.filter-layout__actions {
  width: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  border-left: 1px solid #e5e6eb;
}

.table-container {
  height: calc(100% - 48px);
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

.batch-status-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.batch-status-content__text {
  font-size: 14px;
  color: #4e5969;
  line-height: 22px;
}

:deep(.status-cell) {
  display: flex;
  align-items: center;
  gap: 6px;
}

:deep(.status-dot) {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}

:deep(.status-dot--success) {
  background: #2ab940;
}

:deep(.status-dot--disabled) {
  background: #c9cdd4;
}

:deep(.text-ellipsis) {
  display: inline-block;
  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
