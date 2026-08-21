<template>
  <div class="data-processing-page flex-col h-full">
    <FtCard title="筛选条件" model="titleOperation" clear-content-top-padding>
      <FFilterLayout v-model="isFilterExpanded" @query="query" @reset="reset">
        <el-form :inline="true" :model="table.filter">
          <el-form-item label="任务名称" style="width: 260px">
            <el-input v-model.trim="table.filter.taskName" clearable placeholder="请输入" />
          </el-form-item>
          <el-form-item label="任务类型" style="width: 190px">
            <el-select v-model="table.filter.taskType" clearable placeholder="不限">
              <el-option label="本地" value="local" />
              <el-option label="系统" value="system" />
            </el-select>
          </el-form-item>
          <el-form-item label="数据源" style="width: 190px">
            <el-select v-model="table.filter.dataSourceName" clearable placeholder="不限">
              <el-option
                v-for="item in dataSourceOptions"
                :key="item"
                :label="item"
                :value="item"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="任务状态" style="width: 190px">
            <el-select v-model="table.filter.status" clearable placeholder="不限">
              <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="创建人员" style="width: 220px">
            <el-input v-model.trim="table.filter.createUser" clearable placeholder="请输入" />
          </el-form-item>
        </el-form>
      </FFilterLayout>
    </FtCard>

    <FtCard
      model="titleOperation"
      :style="tableCardStyle"
      title="任务列表"
      clear-content-top-padding
      class="mt-24 data-processing-page__table-card"
    >
      <div class="data-processing-page__table-content">
        <el-table v-loading="table.loading" :data="table.list" class="w-full" height="100%">
          <el-table-column prop="taskName" label="任务名称" min-width="180" show-overflow-tooltip />
          <el-table-column prop="taskType" label="任务类型" width="112">
            <template #default="{ row }">{{ row.taskType === 'local' ? '本地' : '系统' }}</template>
          </el-table-column>
          <el-table-column
            prop="dataSourceName"
            label="数据源"
            min-width="130"
            show-overflow-tooltip
          />
          <el-table-column label="任务明细" min-width="130">
            <template #default="{ row }"
              >{{ row.completedCount || 0 }} / {{ row.totalCount || 0 }}</template
            >
          </el-table-column>
          <el-table-column prop="createUser" label="创建人员" width="150" show-overflow-tooltip />
          <el-table-column prop="createTime" label="创建时间" width="176" />
          <el-table-column label="任务状态" width="120">
            <template #default="{ row }">
              <span class="data-processing-page__status" :class="`is-${row.status}`">
                <i></i>{{ getStatusLabel(row.status) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column fixed="right" label="操作" width="180">
            <template #default="{ row }">
              <el-button link type="primary" :disabled="!canViewTask(row)" @click="viewTask(row)"
                >查看</el-button
              >
              <el-button
                link
                type="primary"
                :disabled="row.status === '1' || row.status === '2'"
                @click="processTask(row)"
                >处理</el-button
              >
              <el-button link type="primary" :disabled="row.status === '1'" @click="deleteTask(row)"
                >删除</el-button
              >
            </template>
          </el-table-column>
        </el-table>
        <div class="data-processing-page__pagination">
          <el-pagination
            v-model:current-page="table.pageNum"
            v-model:page-size="table.pageSize"
            :page-sizes="[10, 20, 50]"
            :total="table.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </FtCard>

    <TaskDataDialog v-model="detailVisible" :task="currentTask" />
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import FtCard from '@/components/FtCard.vue'
import FFilterLayout from '@/components/UI/FFilterLayout/index.vue'
import TaskDataDialog from './components/TaskDataDialog.vue'
import { useTable } from '@/hooks/table'
import { deleteDataSourceDetail, startProcessing } from '@/api/dataCenter'
import useUserStore from '@/stores/modules/user'
import { computedCardHeight } from '@/utils'

defineOptions({ name: 'dataCenter-insDataSource' })

interface DataProcessingTask {
  batchId: string
  dataSourceId: string
  taskName: string
  taskType: 'local' | 'system'
  dataSourceName: string
  completedCount: number
  totalCount: number
  createUser: string
  createTime: string
  status: string
  availableDataStages?: Array<'raw' | 'clean' | 'result'>
  resultDataAvailable?: boolean
}

const userStore = useUserStore()
const isFilterExpanded = ref(false)
const detailVisible = ref(false)
const currentTask = reactive<Partial<DataProcessingTask>>({})
const statusOptions = [
  { label: '待处理', value: '0' },
  { label: '处理中', value: '1' },
  { label: '已完成', value: '2' },
  { label: '处理失败', value: '-1' }
]
const dataSourceOptions = ['本地上传', '小红书', '汽车之家', '抖音', '车质网']
const tableCardStyle = computed(() => computedCardHeight(isFilterExpanded.value ? 205 : 141))

const { table, handleSizeChange, handleCurrentChange, getFirstPageTableData } = useTable({
  method: 'POST',
  url: '/insights/insDataSource/findDataProcessingTasks',
  pageSize: 10
})

function getStatusLabel(status?: string) {
  return statusOptions.find(item => item.value === status)?.label || '待处理'
}

async function query() {
  table.filter.clientId = userStore.clientId
  await getFirstPageTableData()
}

function reset() {
  table.filter = {}
  query()
}

function viewTask(row: DataProcessingTask) {
  if (!canViewTask(row)) {
    ElMessage.warning('任务完成处理后才可查看数据')
    return
  }
  Object.assign(currentTask, row)
  detailVisible.value = true
}

function canViewTask(row: DataProcessingTask) {
  return row.status === '2' || row.status === '-1'
}

async function processTask(row: DataProcessingTask) {
  try {
    await startProcessing({
      clientId: userStore.clientId,
      batchId: row.batchId,
      dataSourceId: row.dataSourceId
    })
    ElMessage.success('任务已开始处理')
    query()
  } catch (error: any) {
    ElMessage.error(error?.message || '任务处理失败')
  }
}

async function deleteTask(row: DataProcessingTask) {
  try {
    await ElMessageBox.confirm(`确认删除任务“${row.taskName}”吗？`, '删除任务', { type: 'warning' })
    await deleteDataSourceDetail({ clientId: userStore.clientId, batchId: row.batchId })
    ElMessage.success('删除成功')
    query()
  } catch (error: any) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error?.message || '删除失败')
    }
  }
}

onMounted(() => {
  userStore.setCilenId(userStore.defaultClientId)
  query()
})
</script>

<style scoped lang="scss">
.data-processing-page {
  &__table-card {
    :deep(.content) {
      min-height: 0;
    }
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
    padding-top: 16px;
    display: flex;
    justify-content: flex-end;
  }

  &__status {
    display: inline-flex;
    align-items: center;
    color: #4e5969;

    i {
      width: 6px;
      height: 6px;
      margin-right: 8px;
      border-radius: 50%;
      background: #86909c;
    }

    &.is-1 i {
      background: #165dff;
    }
    &.is-2 i {
      background: #00b42a;
    }
    &.is--1 i {
      background: #f53f3f;
    }
  }
}
</style>
