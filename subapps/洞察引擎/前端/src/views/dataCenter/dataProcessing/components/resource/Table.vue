<template>
  <div class="table-wrapper cm-card" style="flex: 1">
    <div class="table-header">
      <h3>数据详情</h3>
      <el-button
        v-auth="`dataCenter-resources-add`"
        :data-testid="`founding-index-detail-30001`"
        type="primary"
        @click="handleAdd"
      >
        <template #icon>
          <el-icon><Plus /></el-icon>
        </template>
        新增数据
      </el-button>
    </div>
    <div class="table" :style="computedCardHeight(230)">
      <!-- :loading="table.loading"-->
      <el-table :data="table.list" style="width: 100%" :max-height="'100%'">
        <el-table-column prop="name" label="数据详情" show-overflow-tooltip>
          <template #default="{ row, $index }">
            <span :data-testid="`founding-index-detail-30002-t1-${$index}`">{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="statusText" label="启用状态" width="220">
          <template #default="{ row, $index }">
            <span class="status-circle" :class="checkCircleBg(row.status)"></span>
            <span :data-testid="`founding-index-detail-30002-t2-${$index}`">{{
              row.statusText || '-'
            }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row, $index }">
            <el-button
              v-auth="`dataCenter-resources-edit`"
              :data-testid="`founding-index-detail-30003-b1-${$index}`"
              v-if="row.status === 'NotEnabled'"
              :underline="false"
              type="primary"
              link
              @click="handleEdit(row)"
              >编辑
            </el-button>
            <el-button
              v-auth="`dataCenter-resources-enable`"
              :data-testid="`founding-index-detail-30003-b2-${$index}`"
              v-if="row.status === 'NotEnabled'"
              :underline="false"
              type="primary"
              link
              @click="handleOperation(row, 'enable')"
              >启用
            </el-button>
            <el-button
              v-auth="`dataCenter-resources-delete`"
              :data-testid="`founding-index-detail-30003-b3-${$index}`"
              v-if="row.status === 'Disabled' || row.status === 'NotEnabled'"
              :underline="false"
              type="danger"
              link
              @click="handleOperation(row, 'delete')"
              >删除
            </el-button>
            <el-button
              v-auth="`dataCenter-resources-enable`"
              :data-testid="`founding-index-detail-30003-b4-${$index}`"
              v-if="row.status === 'Enabled'"
              :underline="false"
              type="primary"
              link
              @click="handleOperation(row, 'disable')"
              >禁用
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
    </div>

    <DetailForm @refreshDetailList="getTableData" :filter="filter" />
  </div>
</template>
<script lang="ts" setup>
import { useTable } from '@/hooks/table'
import DetailForm from './DetailForm.vue'
import { Plus } from '@element-plus/icons-vue'
import { deleteDesc, updateDescStatus } from '@/api/dataCenter'
import { ElMessage, ElMessageBox } from 'element-plus'
import { computedCardHeight } from '@/utils'
import { useAppStore } from '@/stores'

const {
  table,
  form,
  getTableData,
  handleSizeChange,
  handleCurrentChange,
  handleAdd,
  handleEdit,
  getFirstPageTableData,
  handleReset
} = useTable({
  method: 'POST',
  url: '/insights/insDataResourceDesc/list',
  notResetKey: ['customer', 'resourceId']
})

provide('form', form)

const props = withDefaults(
  defineProps<{
    filter: any
  }>(),
  {
    // filter: {}
  }
)

const query = async () => {
  table.filter = props.filter
  await getFirstPageTableData()
}

// 操作按钮
const handleOperation = async (record: any, type: string) => {
  let title = ''
  let message = ''
  let confirmButtonText = ''
  let cancelButtonText = ''

  if (type === 'delete') {
    title = '删除确认'
    message = '请确定是否已与相关负责人确认删除当前数据'
    confirmButtonText = '确定删除'
    cancelButtonText = '取消删除'
  } else if (type === 'enable') {
    title = '启用确认'
    message = '请确定是否已完成规则校验和测试，并与相关负责人确认启用当前数据'
    confirmButtonText = '确定启用'
    cancelButtonText = '取消启用'
  } else if (type === 'disable') {
    title = '禁用确认'
    message = '请确定是否已与相关负责人确认禁用当前数据'
    confirmButtonText = '确定禁用'
    cancelButtonText = '取消禁用'
  }

  try {
    await ElMessageBox.confirm(message, title, {
      confirmButtonText,
      cancelButtonText,
      type: type === 'delete' ? 'warning' : 'info',
      center: true
    })

    // 确认后执行操作
    changeStatus(record, type)
  } catch {
    // 用户取消操作，不做任何处理
  }
}
const changeStatus = (param: any, type: string) => {
  if (type === 'delete') {
    const params = {
      id: param.id,
      resourceId: param.resourceId,
      customer: table.filter?.customer
    }
    deleteDesc(params).then(res => {
      console.log(res)
      ElMessage.success('删除成功')
      getTableData()
    })
  } else if (type === 'enable' || type === 'disable') {
    let status = ''
    switch (type) {
      case 'enable':
        status = 'Enabled'
        break
      case 'disable':
        status = 'Disabled'
        break
      default:
        break
    }
    let params = {
      resourceId: param.resourceId,
      id: param.id,
      status: status,
      customer: table.filter?.customer
    }
    updateDescStatus(params).then(() => {
      const successMessage = type === 'enable' ? '启用成功' : '禁用成功'
      ElMessage.success(successMessage)
      getTableData()
    })
  }
}

const checkCircleBg = (type: string) => {
  if (type === 'Enabled') {
    return 'enabled-bg'
  } else if (type === 'Disabled') {
    return 'disabled-bg'
  } else if (type === 'NotEnabled') {
    return 'not-enabled-bg'
  }
}

defineExpose({
  getFirstPageTableData: query,
  handleReset
})
</script>

<style lang="scss" scoped>
.el-table-td-content {
  .el-link {
    margin-right: 10px;

    &:last-child {
      margin-right: 0;
    }
  }
}

.status-circle {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 8px;
}

.enabled-bg {
  background-color: var(--color-success);
}

.not-enabled-bg {
  background-color: #4e5969;
}

.disabled-bg {
  background-color: #c9cdd4;
}
</style>
