<template>
  <div>
    <!-- :style="computedCardHeight(133)" -->
    <FtCard
      :style="computedCardHeight(-20)"
      title="项目管理"
      model="titleOperation"
      clear-content-top-padding
    >
      <!-- <template #extra>
        <el-button
          v-auth="`project-projectList-add`"
          :data-testid="`projectManagement-1006`"
          type="primary"
          icon="plus"
          @click="handleAdd"
        >
          <template #icon>
            <icon-plus />
          </template>
          新增项目
        </el-button>
      </template> -->
      <el-table
        :data="table.list"
        v-loading="table.loading"
        style="width: 100%"
        :max-height="'100%'"
        class="project-management-table"
        border
        @sort-change="handleSortChange"
      >
        <el-table-column prop="projectName" label="项目名称" show-overflow-tooltip width="240">
          <template #default="{ row, $index }">
            <span :data-testid="`projectManagement-2001-t0-${$index}`">{{ row.projectName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="projectDesc" label="项目描述" show-overflow-tooltip>
          <template #default="{ row, $index }">
            <span :data-testid="`projectManagement-2001-t1-${$index}`">{{ row.projectDesc }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" sortable="custom" width="180">
          <template #default="{ row, $index }">
            <span :data-testid="`projectManagement-2001-t4-${$index}`">{{ row.createTime }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="启用状态" width="140">
          <template #default="{ row, $index }">
            <div class="status-wrapper">
              <span
                class="status-circle"
                :class="[row.status === '1' ? 'success-bg' : 'forbidden-bg']"
              ></span>
              <span :data-testid="`projectManagement-2001-t5-${$index}`">{{
                row.statusText || '-'
              }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="300">
          <template #default="{ row, $index }">
            <el-button
              :data-testid="`projectManagement-2001-t6-${$index}`"
              :underline="false"
              type="primary"
              link
              @click="handleEdit(row)"
              >编辑
            </el-button>
            <el-button
              :data-testid="`projectManagement-2001-t7-${$index}`"
              :underline="false"
              type="primary"
              link
              @click="handleDetail(row)"
              >查看数据
            </el-button>
            <el-button
              :data-testid="`projectManagement-2001-t8-${$index}`"
              :underline="false"
              type="primary"
              link
              @click="handleWarning(row)"
              >查看预警
            </el-button>
            <el-button
              :data-testid="`projectManagement-2001-t9-${$index}`"
              :underline="false"
              type="primary"
              link
              @click="handleCorrect(row)"
              >查看纠错
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <el-pagination
        v-if="table.total >= useAppStore().showPaginationMinLength"
        v-model:current-page="table.pageNum"
        v-model:page-size="table.pageSize"
        :page-sizes="[10, 15, 20, 25]"
        :total="table.total"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 16px; justify-content: flex-end"
        small
      />
    </FtCard>

    <Form @refreshList="getTableData" />
    <DetailView v-model="detailVisible" :curDataSourceDetail="curDataSourceDetail"></DetailView>
    <!-- :curDataSource="curDataSource" -->

    <WarningView v-model="warningController.visible" :record="warningController.record" />
    <CorrectView v-model="correctController.visible" :record="correctController.record" />
  </div>
</template>

<script setup lang="ts">
import { useTable } from '@/hooks/table'
import Form from './components/Form/index.vue'
import useConditions from '@/hooks/useConditions'
import { computedCardHeight } from '@/utils'
import { useAppStore } from '@/stores'
import DetailView from './components/DetailView/index.vue'
import { useModal } from '@/hooks/useModal'
import useUserStore from '@/stores/modules/user'
import WarningView from './components/warningView/ViewIndex.vue'
import CorrectView from './components/correctView/index.vue'
const { visible: detailVisible, showVisible: detailShowVisible } = useModal()

// const curDataSource = ref<any>()
const curDataSourceDetail = ref<any>({})

const warningController = ref({
  visible: false,
  record: {}
})

const correctController = ref({
  visible: false,
  record: {}
})

const handleWarning = (record: any) => {
  console.log('record', record)

  warningController.value.visible = true
  warningController.value.record = record
}

const handleCorrect = (record: any) => {
  console.log('record', record)

  correctController.value.visible = true
  correctController.value.record = record
}

const handleDetail = (record: any) => {
  curDataSourceDetail.value = record
  detailShowVisible()
}

const userStore = useUserStore()

const {
  table,
  form,
  // getAllSelection,
  // getSlection,
  getTableData,
  handleSizeChange,
  handleCurrentChange,
  handleEdit,
  getFirstPageTableData,
  handleSortChange
} = useTable({
  url: '/insights/insProjectInfo/findProjectList',
  method: 'POST',
  notResetKey: ['clientId']
})

const { conditions } = useConditions({ url: '/insights/insProjectInfo/conditions' })
provide('conditions', conditions)

// const handleApply = (record: any) => {
//   console.log(record)
// }

const handleQuery = () => {
  table.filter.clientId = userStore.clientId
  getFirstPageTableData()
}

onMounted(() => {
  handleQuery()
  // getTableData();
})

provide('form', form)
</script>

<style lang="scss">
.status-wrapper {
  display: flex;
  align-items: center;

  .status-circle {
    display: inline-block;
    width: 6px;
    height: 6px;
    border-radius: 50%;
    margin-right: 8px;
  }

  .success-bg {
    background-color: var(--color-success);
  }

  .forbidden-bg {
    background-color: #c9cdd4;
  }
}

// 项目管理表格样式优化
.project-management-table {
  :deep(.el-table__header-wrapper) {
    th.el-table__cell {
      background-color: var(--bgc-def) !important;
      color: var(--color-high);
      font-weight: 600;
      border-bottom: 1px solid var(--border-color);
    }
  }

  :deep(.el-table__body-wrapper) {
    .el-table__row {
      &:hover {
        background-color: var(--el-table-row-hover-bg-color);
      }
    }

    .el-table__cell {
      border-bottom: 1px solid var(--border-color);
      padding: 12px 0;
    }
  }

  :deep(.el-button--text) {
    padding: 4px 8px;
    margin-right: 8px;

    &:last-child {
      margin-right: 0;
    }
  }
}
</style>
