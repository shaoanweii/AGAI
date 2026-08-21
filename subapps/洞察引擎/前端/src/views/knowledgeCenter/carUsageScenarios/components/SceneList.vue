<script setup lang="ts">
import { computed, inject, nextTick, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { TableInstance } from 'element-plus'
import { carUsageScenarioEnableStatus, findCarSceneOperatorList } from '@/api/carUsageScenarios'
import { showOverflowTooltipConfig } from '@/constant/index'
import FtCard from '@/components/FtCard.vue'
import FFilterLayout from '@/components/UI/FFilterLayout/index.vue'
import { useTable } from '@/hooks/table'
import { computedCardHeight } from '@/utils'
import SceneBatchActionDialog from './SceneBatchActionDialog.vue'
import SceneFormDialog from './SceneFormDialog.vue'
import type { CarUsageScenarioBatchActionType, CarUsageScenarioSceneQueryForm } from './types'
import { carUsageScenarioPageContextKey } from '../context'
import { resolveCarUsageScenarioStatusOptions } from './statusOptions'

defineOptions({
  name: 'CarUsageScenarioSceneList'
})

const queryForm = ref<CarUsageScenarioSceneQueryForm>({
  operator: '',
  sceneName: '',
  status: ''
})
const isFilterExpanded = ref(false)
const tableRef = ref<TableInstance>()
const multipleSelection = ref<Api.CarUsageScenarios.SceneRecord[]>([])
const operatorOptions = ref<Api.CarUsageScenarios.SceneOperatorOption[]>([])
const batchDialogVisible = ref(false)
const batchActionType = ref<CarUsageScenarioBatchActionType>('enable')
const sceneDialogVisible = ref(false)
const editingScene = ref<Api.CarUsageScenarios.SceneRecord | null>(null)
const currentOrder = ref('')

type SceneTableSortOrder = 'ascending' | 'descending' | null

const { table, handleSizeChange, handleCurrentChange, getFirstPageTableData, refreshTableData } =
  useTable(
    {
      method: 'POST',
      url: '/insights/carScene/findCarSceneList',
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

const pageContext = inject(carUsageScenarioPageContextKey, null)
const tableCardStyle = computed(() => computedCardHeight(isFilterExpanded.value ? 205 : 141))
const statusOptions = computed(() =>
  resolveCarUsageScenarioStatusOptions(pageContext?.conditionMap.value || {})
)
const selectedIds = computed(
  () => (multipleSelection.value || []).map(item => item?.id).filter(Boolean) as string[]
)

/**
 * 用车场景不再具有分类关系，查询只保留场景自身的业务条件。
 */
const buildQueryParams = () => ({
  operator: queryForm.value.operator || undefined,
  sceneName: queryForm.value.sceneName.trim() || undefined,
  status: queryForm.value.status || undefined,
  order: currentOrder.value || undefined
})

/** 统一表格文案兜底，避免空字段直接显示空白。 */
const formatCellText = (value: unknown) => {
  if (value === 0) return '0'
  if (value === null || value === undefined) return '-'
  const text = String(value).trim()
  return text || '-'
}

/** 拉取操作人筛选项，页面不再加载或依赖场景分类树。 */
const loadFilterOptions = async () => {
  try {
    const operatorResult = await findCarSceneOperatorList()
    const optionMap = new Map<string, Api.CarUsageScenarios.SceneOperatorOption>()
    ;(operatorResult.result || []).forEach(item => {
      const id = String(item?.id || '').trim()
      const userName = String(item?.userName || '').trim()
      if (id && userName && !optionMap.has(id)) optionMap.set(id, { id, userName })
    })
    operatorOptions.value = Array.from(optionMap.values())
  } catch (error: any) {
    operatorOptions.value = []
    ElMessage.error(error?.message || '获取用车场景筛选项失败，请稍后重试')
  }
}

/** 查询统一回到第一页，避免筛选后保留无效分页状态。 */
const query = () => {
  table.filter = buildQueryParams()
  multipleSelection.value = []
  clearTableSelection()
  void getFirstPageTableData()
}

/** 重置筛选条件并重新获取全量场景。 */
const reset = () => {
  queryForm.value = {
    operator: '',
    sceneName: '',
    status: ''
  }
  currentOrder.value = ''
  query()
}

/** 将排序事件转换为服务端约定的排序参数。 */
const handleSortChange = ({ prop, order }: { prop?: string; order?: SceneTableSortOrder }) => {
  currentOrder.value = prop && order ? `${prop} ${order === 'ascending' ? 'asc' : 'desc'}` : ''
  query()
}

/** 状态颜色与启用字典保持一致。 */
const resolveStatusClass = (status: string | undefined) =>
  String(status || '') === carUsageScenarioEnableStatus.ENABLED
    ? 'status-dot--success'
    : 'status-dot--disabled'

/** 清空表格勾选态，防止筛选或翻页后残留旧选择。 */
const clearTableSelection = () => {
  nextTick(() => tableRef.value?.clearSelection())
}

/** 同步当前页已选记录，供批量操作直接复用。 */
const handleSelectionChange = (rows: Api.CarUsageScenarios.SceneRecord[]) => {
  multipleSelection.value = rows
}

/** 批量操作先校验选中项，再根据命令打开对应弹框。 */
const handleBatchCommand = (command: CarUsageScenarioBatchActionType) => {
  if (!selectedIds.value.length) {
    ElMessage.warning('请先选择用车场景')
    return
  }
  batchActionType.value = command
  batchDialogVisible.value = true
}

/** 打开新建或编辑弹框时仅维护当前记录。 */
const openSceneDialog = (row?: Api.CarUsageScenarios.SceneRecord) => {
  editingScene.value = row || null
  sceneDialogVisible.value = true
}

/** 保存后刷新筛选项和当前列表，保持无分类页面数据一致。 */
const handleSceneDialogSuccess = async () => {
  table.filter = buildQueryParams()
  multipleSelection.value = []
  clearTableSelection()
  await loadFilterOptions()
  await refreshTableData(false)
}

/** 批量状态调整后刷新当前列表。 */
const handleBatchDialogSuccess = async () => {
  table.filter = buildQueryParams()
  multipleSelection.value = []
  clearTableSelection()
  await refreshTableData(false)
}

onMounted(async () => {
  await loadFilterOptions()
  query()
})

watch(
  () => table.list,
  () => {
    multipleSelection.value = []
    clearTableSelection()
  }
)
</script>

<template>
  <section class="scene-list flex-col h-full">
    <FtCard title="筛选条件" model="titleOperation" clear-content-top-padding>
      <FFilterLayout v-model="isFilterExpanded" @query="query" @reset="reset">
        <el-form :inline="true" :model="queryForm">
          <el-form-item label="场景名称" style="width: 260px">
            <el-input
              v-model.trim="queryForm.sceneName"
              clearable
              placeholder="请输入"
              @keyup.enter="query"
            />
          </el-form-item>
          <el-form-item label="操作人" style="width: 220px">
            <el-select-v2
              v-model="queryForm.operator"
              :options="operatorOptions"
              :props="{ value: 'id', label: 'userName' }"
              clearable
              filterable
              placeholder="不限"
            />
          </el-form-item>
          <el-form-item label="启用状态" style="width: 190px">
            <el-select v-model="queryForm.status" clearable placeholder="不限">
              <el-option
                v-for="item in statusOptions"
                :key="item.key"
                :label="item.value"
                :value="item.key"
              />
            </el-select>
          </el-form-item>
        </el-form>
      </FFilterLayout>
    </FtCard>

    <FtCard
      :style="tableCardStyle"
      title="用车场景列表"
      model="titleOperation"
      clear-content-top-padding
      class="mt-24 scene-list__table-card"
    >
      <template #extra>
        <el-dropdown trigger="click" class="mr-16" @command="handleBatchCommand">
          <el-button :disabled="!selectedIds.length">批量操作</el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item
                v-for="item in statusOptions"
                :key="item.key"
                :command="item.key === carUsageScenarioEnableStatus.ENABLED ? 'enable' : 'disable'"
                :disabled="!selectedIds.length"
              >
                {{ item.value }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button type="primary" @click="openSceneDialog()">
          <template #icon><Plus /></template>
          新建场景
        </el-button>
      </template>

      <div class="scene-list__table-content">
        <el-table
          ref="tableRef"
          v-loading="table.loading"
          :data="table.list || []"
          row-key="id"
          height="100%"
          style="width: 100%"
          @selection-change="handleSelectionChange"
          @sort-change="handleSortChange"
        >
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column
            prop="sceneName"
            label="场景名称"
            min-width="220"
            :show-overflow-tooltip="showOverflowTooltipConfig"
          >
            <template #default="{ row }"
              ><span>{{ formatCellText(row.sceneName) }}</span></template
            >
          </el-table-column>
          <el-table-column
            prop="operator"
            label="操作人"
            min-width="170"
            :show-overflow-tooltip="showOverflowTooltipConfig"
          >
            <template #default="{ row }"
              ><span>{{ formatCellText(row.operator) }}</span></template
            >
          </el-table-column>
          <el-table-column
            prop="updateTime"
            label="更新时间"
            min-width="210"
            sortable="custom"
            :show-overflow-tooltip="showOverflowTooltipConfig"
          >
            <template #default="{ row }"
              ><span>{{ formatCellText(row.updateTime) }}</span></template
            >
          </el-table-column>
          <el-table-column
            prop="createTime"
            label="创建时间"
            min-width="210"
            sortable="custom"
            :show-overflow-tooltip="showOverflowTooltipConfig"
          >
            <template #default="{ row }"
              ><span>{{ formatCellText(row.createTime) }}</span></template
            >
          </el-table-column>
          <el-table-column prop="statusName" label="启用状态" min-width="150">
            <template #default="{ row }">
              <div class="status-cell">
                <span :class="['status-dot', resolveStatusClass(row.status)]"></span>
                <span>{{ formatCellText(row.statusName || row.status) }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click="openSceneDialog(row)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="scene-list__pagination">
          <el-pagination
            v-model:current-page="table.pageNum"
            v-model:page-size="table.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="table.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </FtCard>

    <SceneFormDialog
      v-model:visible="sceneDialogVisible"
      :scene-data="editingScene"
      @success="handleSceneDialogSuccess"
    />
    <SceneBatchActionDialog
      v-model:visible="batchDialogVisible"
      :action-type="batchActionType"
      :selected-ids="selectedIds"
      @success="handleBatchDialogSuccess"
    />
  </section>
</template>

<style scoped lang="scss">
.scene-list {
  &__table-card :deep(.content) {
    min-height: 0;
  }

  &__table-content {
    display: flex;
    flex-direction: column;
    height: 100%;
    min-height: 0;

    .el-table {
      flex: 1;
      min-height: 0;
    }
  }

  &__pagination {
    display: flex;
    justify-content: flex-end;
    padding-top: 16px;
  }
}

:deep(.status-cell) {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 100%;
}

// 表格按容器弹性分配列宽，同时禁止时间字段换行造成行高抖动。
:deep(.el-table__header-wrapper .cell),
:deep(.el-table__body-wrapper .cell) {
  white-space: nowrap;
}

:deep(.status-dot) {
  display: inline-block;
  width: 6px;
  height: 6px;
  flex-shrink: 0;
  border-radius: 50%;
}

:deep(.status-dot--success) {
  background: #2ab940;
}

:deep(.status-dot--disabled) {
  background: #c9cdd4;
}
</style>
