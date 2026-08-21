<template>
  <div>
    <FtCard
      :style="computedCardHeight(0)"
      title="账号列表"
      model="titleOperation"
      clear-content-top-padding
    >
      <template #extra>
        <div class="flex gap-16">
          <el-select
            v-model="table.filter.roleId"
            placeholder="角色"
            clearable
            style="width: 100px"
            @change="handleSearch"
          >
            <el-option
              v-for="item in roles"
              :key="item.id || ''"
              :label="item.roleName || ''"
              :value="item.id || ''"
            />
          </el-select>

          <el-select-v2
            v-model="table.filter.deptId"
            :options="departs"
            :props="{ value: 'code', label: 'name' }"
            filterable
            placeholder="部门"
            clearable
            style="width: 150px"
            @change="handleSearch"
          >
            <!-- <el-option
              v-for="item in departs"
              :key="item.value || ''"
              :label="item.name || ''"
              :value="item.value || ''"
            /> -->
          </el-select-v2>

          <el-select
            v-model="table.filter.completeRate"
            placeholder="完成率"
            clearable
            style="width: 100px"
            @change="handleSearch"
          >
            <el-option
              v-for="item in conditions.completionRate"
              :key="item.key || ''"
              :label="item.value || ''"
              :value="item.key || ''"
            />
          </el-select>

          <el-select
            v-model="table.filter.status"
            placeholder="当前状态"
            clearable
            style="width: 100px"
            @change="handleSearch"
          >
            <el-option
              v-for="item in conditions.enableType"
              :key="item.key || ''"
              :label="item.value || ''"
              :value="item.key || ''"
            />
          </el-select>
          <el-input
            v-model="table.filter.userName"
            placeholder="请输入关键词搜索"
            clearable
            style="width: 172px"
            :suffix-icon="Search"
            @change="handleSearch"
          />

          <!-- <el-button
            v-auth="`settings-accountManagement-add`"
            :data-testid="`accountmanagement-10006`"
            type="primary"
            :icon="Plus"
            @click="handleAdd"
          >
            导出数据
          </el-button> -->
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
        <el-table-column label="员工姓名">
          <template #default="{ row }">
            <div class="user-info">
              <div class="user-details">
                <div class="username">{{ row.userName || '未设置' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="employeeId" label="员工工号" />
        <el-table-column prop="deptName" label="所属部门" />
        <el-table-column prop="roleName" label="系统角色" />
        <el-table-column prop="loginCounts" label="登录次数" />
        <el-table-column prop="lastLoginTime" label="最近登录时间" />
        <el-table-column prop="completeRate" label="完成率">
          <template #default="{ row }">
            <span>{{ row.completeRate }}%</span>
          </template>
        </el-table-column>

        <el-table-column label="当前状态" width="100">
          <template #default="{ row }">
            <div class="flex-y-center">
              <div class="status-icon mr-8" :style="getStatusTagType(row.status)"></div>
              {{ row.statusText }}
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              v-auth="`settings-accountManagement-edit`"
              link
              type="primary"
              @click="handleEdit(row)"
            >
              编辑
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
      />
    </FtCard>

    <UserFormDialog
      v-model:visible="form.visible!"
      :user-data="form.data"
      :roles="[]"
      :is-edit="form.operation === 'edit'"
      @success="getFirstPageTableData"
    ></UserFormDialog>
  </div>
</template>

<script setup lang="ts">
import { useTable } from '@/hooks/table'
// import { Plus } from '@element-plus/icons-vue'
import useConditions from '@/hooks/useConditions'
import { computedCardHeight } from '@/utils'
import { useAppStore } from '@/stores'
import useUserStore from '@/stores/modules/user'
import { Search } from '@element-plus/icons-vue'
import UserFormDialog from './UserFormDialog.vue'
import { accountInfoQueryRoleALlList, findDepartList, queryRoleALlList } from '@/api/settings'

const { conditions } = useConditions({ url: '/insights/accountInfo/conditions' })
provide('conditions', conditions)
const {
  table,
  form,
  getTableData,
  handleSizeChange,
  handleCurrentChange,
  handleEdit,
  handleSortChange,
  getFirstPageTableData
} = useTable({
  url: '/insights/accountInfo/findAccountInfoList',
  method: 'POST',
  notResetKey: ['clientId']
})

// 查询参数
// const queryParams = reactive<any>({
//   pageNum: 1,
//   pageSize: 10,
//   userName: '',
//   deptId: [],
//   roleId: '',
//   email: '',
//   status: undefined,
//   completeRate: undefined as number | undefined
// })

// 搜索表单
// const searchForm = reactive({
//   userName: '',
//   deptId: [],
//   roleId: '',
//   email: '',
//   status: undefined as string | undefined,
//   completeRate: undefined as number | undefined
// })

//获取角色下拉
const roles = ref<any[]>([])
const getRoleList = async () => {
  const response = await accountInfoQueryRoleALlList({})
  if (response.success) {
    roles.value = response.result || []
  }
}

//获取部门下拉
const departs = ref<any[]>([])
const getDepartList = async () => {
  const response = await findDepartList({})
  if (response.success) {
    departs.value = response.result || []
  }
}

//获取完成度下拉
const completionList = ref<any[]>([])
const getCompletionList = async () => {
  // const response = await getDictItemsByDict('completion_rate')
  // if (response.success) {
  //   completionList.value = response.result || []
  // }
}

/**
 * 搜索处理
 */
const handleSearch = async () => {
  // 将搜索表单数据同步到查询参数
  // Object.assign(queryParams, {
  //   ...searchForm,
  //   pageNum: 1 // 搜索时重置到第一页
  //   // accountName: searchForm.accountName,
  //   // userName: searchForm.userName,
  //   // email: searchForm.email,
  //   // status: searchForm.status
  // })
  // await fetchAccountList()
  getFirstPageTableData()
}

// 辅助方法
/**
 * 获取状态标签类型
 */
const getStatusTagType = (status?: string) => {
  let color = ''
  switch (status) {
    case '1':
      color = '#00B42A' // 启用
      break
    case '0':
      color = '#C9CDD4' // 停用
      break
    default:
      color = 'red'
      break
  }
  return { backgroundColor: color }
}

onMounted(() => {
  getRoleList()
  getDepartList()
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
