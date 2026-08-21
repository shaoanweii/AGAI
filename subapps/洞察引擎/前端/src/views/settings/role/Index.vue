<template>
  <div>
    <FtCard
      :style="computedCardHeight(0)"
      title="角色列表"
      model="titleOperation"
      clear-content-top-padding
    >
      <template #extra>
        <div class="flex gap-16">
          <el-input
            v-model="table.filter.searchKeyword"
            placeholder="请输入关键词搜索"
            clearable
            style="width: 200px"
            :suffix-icon="Search"
            @keyup.enter="handleSearch"
          />
          <el-button v-auth="`settings-role-add`" type="primary" :icon="Plus" @click="handleAdd"
            >新建角色</el-button
          >
        </div>
      </template>

      <el-table
        :data="table.list"
        v-loading="table.loading"
        style="width: 100%; height: 90%"
        :height="'90%'"
        @sort-change="handleSortChange"
      >
        <el-table-column type="index" label="#" width="56" align="center" />

        <el-table-column label="角色名称" prop="roleName" width="200"> </el-table-column>

        <el-table-column prop="remark" label="备注" align="left" show-overflow-tooltip />

        <el-table-column label="关联账号" width="200">
          <template #default="{ row }">
            <!-- <span>{{ row.userName?.length || 0 }}</span> -->
            <span>{{ row.userCount }}</span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="200" prop="roleStatusName">
          <template #default="{ row }">
            <div class="flex-y-center">
              <div class="status-icon mr-8" :style="getStatusTagType(row.roleStatusName)"></div>
              <span>{{ row.roleStatusName }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)"> 编辑 </el-button>
            <el-button link type="primary" @click="customerHandleDelete(row)"> 删除 </el-button>
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
      />
    </FtCard>

    <!-- 角色表单对话框 -->
    <RoleFormDialog
      v-model:visible="form.visible!"
      :role-data="form.data"
      :is-edit="form.operation === 'edit'"
      :menuPermissionList="[]"
      @success="getFirstPageTableData"
    />
  </div>
</template>

<script setup lang="tsx">
import { useTable } from '@/hooks/table'
import { Plus } from '@element-plus/icons-vue'
// import useConditions from '@/hooks/useConditions'
import FtCard from '@/components/FtCard.vue'
import { computedCardHeight } from '@/utils'
import { useAppStore } from '@/stores'
import useUserStore from '@/stores/modules/user'
import RoleFormDialog from './components/RoleFormDialog/index.vue'
import { Search } from '@element-plus/icons-vue'

// const { conditions } = useConditions({ url: '/insights/role/conditions' })
// provide('conditions', conditions)
const {
  table,
  form,
  getTableData,
  handleSizeChange,
  handleCurrentChange,
  handleAdd,
  handleEdit,
  handleDelete,
  handleSortChange,
  getFirstPageTableData
} = useTable({
  url: '/insights/role/queryRoleList',
  deleteUrl: '/insights/role/deleteRole',
  method: 'POST',
  notResetKey: ['clientId']
})

/**
 * 搜索角色
 */
const handleSearch = async () => {
  getFirstPageTableData()
}

// 重写删除方法
const customerHandleDelete = (row: any) => {
  handleDelete({
    id: row.roleId
  })
}

/**
 * 获取状态标签类型
 */
const getStatusTagType = (status?: string) => {
  let color = ''
  switch (status) {
    case '启用中':
      color = '#00B42A' // 启用
      break
    case '已禁用':
      color = '#C9CDD4' // 停用
      break
    default:
      color = 'red'
      break
  }
  return { backgroundColor: color }
}

onMounted(() => {
  table.filter.clientId = useUserStore().clientId
  getTableData()
})

provide('form', form)
</script>

<style lang="scss">
.status-icon {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}
</style>
