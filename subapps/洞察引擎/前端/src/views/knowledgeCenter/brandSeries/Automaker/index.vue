<script setup lang="ts">
import { ref } from 'vue'
import { useTable } from '@/hooks/table'
import useConditions from '@/hooks/useConditions'
import { computedCardHeight } from '@/utils'
import { ElMessage } from 'element-plus'
import { batchUpdateAutomakerStatus, brandSeriesEnableStatus } from '@/api/brandSeries'
import { showOverflowTooltipConfig } from '@/constant/index'
import FtCard from '@/components/FtCard.vue'
import AppDialog from '@/components/AppDialog.vue'
import AutomakerFormDialog from './AutomakerFormDialog.vue'

defineOptions({
  name: 'Automaker'
})

const { conditions } = useConditions({ url: '/insights/automark/conditions' })

const getConditionOptions = (key: string) => conditions?.[key] || []

const {
  table,
  handleReset,
  handleSizeChange,
  handleCurrentChange,
  getTableData,
  getFirstPageTableData
} = useTable(
  {
    method: 'POST',
    url: '/insights/automark/findAutomarkList'
  },
  res => {
    const pageResult = res.result || {}
    // 兼容不同环境下的字段命名，确保车企列有稳定展示值
    const list = (pageResult.list || pageResult.records || []).map((item: any) => ({
      ...item,
      name: item.name || item.automark || item.automarkName || ''
    }))
    return {
      list,
      total: Number(pageResult.total || 0)
    }
  }
)

const isExpanded = ref(false)
const multipleSelection = ref<any[]>([])
const tableRef = ref<any>()
const coreOptions = computed(() => getConditionOptions('isCore'))

const competitiveTypeOptions = computed(() => getConditionOptions('competitiveType'))

const statusOptions = computed(() => getConditionOptions('stopOrEnable'))

type BatchAction = 'enable' | 'disable'
const batchDialogVisible = ref(false)
const batchDialogAction = ref<BatchAction>('enable')

const formDialogVisible = ref(false)
const formDialogMode = ref<'add' | 'edit'>('add')
const formDialogEditData = ref<any>(null)

const init = () => {
  table.filter = {
    name: '',
    isCore: undefined,
    competitiveType: undefined,
    operator: '',
    status: undefined
  }
}

init()

onMounted(() => {
  query()
})

const query = (resetPage = true) => {
  if (resetPage) {
    getFirstPageTableData()
  } else {
    getTableData()
  }
}

const reset = () => {
  multipleSelection.value = []
  delete table.filter.order
  handleReset()
  tableRef.value?.clearSort?.()
}

const handleSortChange = ({
  prop,
  order
}: {
  prop?: string
  order?: 'ascending' | 'descending' | null
}) => {
  // 统一使用后端自定义排序，避免前端排序与服务端分页数据不一致。
  if (!prop || !order) {
    delete table.filter.order
    getFirstPageTableData()
    return
  }
  const sortStr = order === 'ascending' ? 'asc' : 'desc'
  table.filter.order = `${prop} ${sortStr}`
  getFirstPageTableData()
}

const handleSelectionChange = (selection: any[]) => {
  multipleSelection.value = selection
}

const openCreateDialog = () => {
  formDialogMode.value = 'add'
  formDialogEditData.value = null
  formDialogVisible.value = true
}

const openEditDialog = (row: any) => {
  formDialogMode.value = 'edit'
  formDialogEditData.value = row
  formDialogVisible.value = true
}

const handleFormSuccess = () => {
  formDialogVisible.value = false
  query(false)
}

const handleBatchCommand = (command: BatchAction) => {
  if (!multipleSelection.value.length) {
    ElMessage.warning('请先选择数据')
    return
  }
  if (command !== 'enable' && command !== 'disable') return
  batchDialogAction.value = command
  batchDialogVisible.value = true
}

const handleBatchConfirm = async ({ close }: { close: () => void }) => {
  if (!multipleSelection.value.length) {
    ElMessage.warning('请先选择数据')
    return
  }
  const text = batchDialogAction.value === 'enable' ? '启用' : '禁用'
  const ids = multipleSelection.value.map(item => item.id).filter(Boolean)
  if (!ids.length) {
    ElMessage.warning('当前选中数据缺少ID，无法执行批量操作')
    return
  }
  await batchUpdateAutomakerStatus(
    ids,
    batchDialogAction.value === 'enable'
      ? brandSeriesEnableStatus.ENABLED
      : brandSeriesEnableStatus.DISABLED
  )
  ElMessage.success(`批量${text}成功`)
  multipleSelection.value = []
  query(false)
  close()
}

const tableFcardHeight = computed(() => {
  return computedCardHeight(isExpanded.value ? 275 : 155)
})
</script>

<template>
  <FtCard title="筛选条件" model="titleOperation" clear-content-top-padding>
    <FFilterLayout v-model="isExpanded" @query="query" @reset="reset">
      <el-form layout="inline" :model="table.filter" label-position="right" class="custom-form">
        <el-row class="w-full" :gutter="16">
          <el-col :span="6">
            <el-form-item label="车企">
              <el-input
                v-model.trim="table.filter.name"
                placeholder="请输入"
                :maxlength="20"
                clearable
              />
            </el-form-item>
          </el-col>

          <el-col :span="6">
            <el-form-item label="是否核心">
              <el-select-v2
                v-model="table.filter.isCore"
                placeholder="不限"
                clearable
                filterable
                :options="coreOptions"
                :props="{ label: 'value', value: 'key' }"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="本竞品类型">
              <el-select v-model="table.filter.competitiveType" placeholder="本竞品类型" clearable>
                <el-option
                  v-for="(item, index) in competitiveTypeOptions"
                  :key="`${item.key}-${index}`"
                  :label="item.value"
                  :value="item.key"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="6">
            <el-form-item label="操作人">
              <el-input
                v-model.trim="table.filter.operator"
                placeholder="请输入"
                :data-testid="`dataSource-result-10009`"
                :maxlength="20"
                clearable
              />
            </el-form-item>
          </el-col>

          <el-col :span="6">
            <el-form-item label="启用状态">
              <el-select-v2
                v-model="table.filter.status"
                placeholder="不限"
                clearable
                filterable
                :options="statusOptions"
                :props="{ label: 'value', value: 'key' }"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </FFilterLayout>
  </FtCard>

  <!-- <el-divider /> -->
  <FtCard
    :style="tableFcardHeight"
    title="车企列表"
    model="titleOperation"
    clear-content-top-padding
    class="mt-24"
  >
    <template #extra>
      <el-dropdown trigger="click" placement="bottom-end" @command="handleBatchCommand as any">
        <el-button :disabled="!table.list?.length" :data-testid="`dataSource-result-10012`">
          批量操作<el-icon class="el-icon--right"><arrow-down /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="enable">启用</el-dropdown-item>
            <el-dropdown-item command="disable">禁用</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      <el-button type="primary" class="ml-12" @click="openCreateDialog">
        <el-icon class="mr-5"><Plus /></el-icon>
        新增车企
      </el-button>
    </template>

    <!-- <div class="flex justify-between mb-12">
      <h3 class="table-left-title flex item-center">数据列表</h3>
      <el-button
        :disabled="!table.list?.length"
        :data-testid="`dataSource-result-10012`"
        type="primary"
        class="ml-8"
        @click="handleExport"
      >
        <template #icon>
          <i class="iconfont icon-Export"></i>
        </template>
        导出数据
      </el-button>
    </div> -->
    <div class="table-container">
      <el-table
        ref="tableRef"
        :data-testid="`dataSource-result-table`"
        v-loading="table.loading"
        :data="table.list || []"
        row-key="id"
        height="100%"
        :tooltip-options="{ popperClass: 'common-tooltip' }"
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
      >
        <el-table-column type="selection" width="55" fixed="left" />
        <el-table-column prop="name" label="车企" min-width="160" show-overflow-tooltip />
        <el-table-column
          prop="isCoreName"
          label="是否核心"
          min-width="120"
          :show-overflow-tooltip="showOverflowTooltipConfig"
        />
        <el-table-column
          prop="competitiveTypeName"
          label="本竞品类型"
          min-width="130"
          :show-overflow-tooltip="showOverflowTooltipConfig"
        />
        <el-table-column prop="operator" label="操作人" min-width="120" show-overflow-tooltip />
        <el-table-column
          prop="updateTime"
          label="更新时间"
          min-width="170"
          sortable="custom"
          show-overflow-tooltip
        />
        <el-table-column
          prop="createTime"
          label="创建时间"
          min-width="170"
          sortable="custom"
          show-overflow-tooltip
        />
        <el-table-column prop="statusName" label="启用状态" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEditDialog(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <!-- v-if="table.total > 0" -->
    <!-- 分页组件 -->
    <el-pagination
      v-model:current-page="table.pageNum"
      v-model:page-size="table.pageSize"
      :page-sizes="[10, 20, 50, 100]"
      :total="table.total"
      layout="total, sizes, prev, pager, next"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      style="margin-top: 16px; justify-content: flex-end"
    />

    <AppDialog
      v-model:visible="batchDialogVisible"
      :title="batchDialogAction === 'enable' ? '批量启用' : '批量禁用'"
      width="480px"
      :confirm="handleBatchConfirm"
    >
      <template v-if="batchDialogAction === 'enable'">是否确认批量启用选中车企？</template>
      <template v-else>是否确认批量禁用选中车企？</template>
    </AppDialog>

    <AutomakerFormDialog
      v-model:visible="formDialogVisible"
      :mode="formDialogMode"
      :row-data="formDialogEditData"
      :is-core-options="coreOptions"
      :competitive-type-options="competitiveTypeOptions"
      :status-options="statusOptions"
      @success="handleFormSuccess"
    />
  </FtCard>
</template>

<style scoped lang="scss">
.table-container {
  height: calc(100% - 48px); /* 减去分页组件的高度48px */
}

// .custom-form {
//   :deep(.el-form-item__label) {
//     line-height: 18px;
//   }
// }

:deep(.text-ellipsis) {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:deep(.cell-wrap-text) {
  word-wrap: break-word;
  word-break: break-all;
  white-space: normal;
  overflow-wrap: break-word;
  hyphens: auto;
}
</style>
