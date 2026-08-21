<template>
  <div class="role-management">
    <!-- 角色表格 -->
    <el-card class="table-card" shadow="never">
      <div class="flex-between items-center mb-24">
        <div class="text-h3" style="font-weight: 600">角色列表</div>
        <div class="flex gap-16">
          <el-select
            v-model="searchForm.roleType"
            clearable
            placeholder="请选择角色类型"
            :options="userStore.getDictItems('role_type')"
            :props="{ label: 'text', value: 'value' }"
            style="width: 200px"
            @change="handleSearch"
          >
          </el-select>
          <el-input
            v-model="searchForm.searchKeyword"
            placeholder="请输入关键词搜索"
            clearable
            style="width: 200px"
            :suffix-icon="Search"
            @change="handleSearch"
            @keyup.enter="handleSearch"
          />

          <!-- <el-select
            v-model="searchForm.enabled"
            placeholder="请选择状态"
            clearable
            style="width: 120px"
          >
            <el-option label="启用" value="1" />
            <el-option label="禁用" value="0" />
          </el-select> -->

          <el-button type="primary" @click="handleAdd">新建角色</el-button>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="roleList"
        max-height="calc(100vh - 84px - 48px - 106px - 32px - 10px)"
        class="flex-auto overflow-auto"
      >
        <el-table-column type="index" label="#" width="56" align="center" />

        <el-table-column label="角色名称" prop="roleName" width="200"> </el-table-column>
        <el-table-column label="角色类型" prop="roleTypeName" width="200"> </el-table-column>

        <el-table-column
          prop="remark"
          label="备注"
          align="left"
          :show-overflow-tooltip="{ popperClass: 'text-tooltip-light' }"
        />

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

        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleOpenAccountRelation(row)">
              关联账号
            </el-button>
            <el-button link type="primary" @click="handleEdit(row)"> 编辑 </el-button>
            <el-button link type="primary" @click="handleCopy(row)"> 复制 </el-button>
            <el-button
              link
              type="primary"
              :disabled="row.roleStatusName === '启用中'"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="->,total, prev, pager, next, sizes"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 角色表单对话框 -->
    <RoleFormDialog
      v-model:visible="formDialogVisible"
      :role-data="currentEditRole"
      :is-edit="isEditMode"
      :menuPermissionList="menuPermissionList"
      @success="handleFormSuccess"
    />

    <!-- 关联账号对话框 -->
    <AccountRelationDialog
      v-model:visible="accountRelationDialogVisible"
      :role-data="currentRoleForRelation"
      @success="handleAccountRelationSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { h, ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { copyRoleId, deleteRoleId, queryMenuPermissionList, queryRoleList } from '@/api/role'
import type { Role } from '@/types/system'
import type { RoleQueryParams } from '@/api/role/types'
import RoleFormDialog from './components/RoleFormDialog/index.vue'
import AccountRelationDialog from './components/AccountRelationDialog/index.vue'
import { useUserStore } from '@/store'
import { appDialogConfirm } from '@/components/appDialog'
import SvgIcon from '@/components/UI/SvgIcon/index.vue'

defineOptions({
  name: 'RoleManagement'
})

const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const roleList = ref<Role[]>([])
const total = ref(0)

// 查询参数
const queryParams = reactive<RoleQueryParams>({
  pageSize: 10,
  pageNum: 1,
  enabled: undefined,
  searchKeyword: undefined,
  order: 'createTime desc'
})

// 搜索表单
const searchForm = reactive({
  searchKeyword: '',
  roleType: undefined,
  enabled: undefined as string | undefined
})

const formDialogVisible = ref(false)
const currentEditRole = ref<Role | null>(null)
const isEditMode = ref(false)

// 关联账号弹窗
const accountRelationDialogVisible = ref(false)
const currentRoleForRelation = ref<Role | null>(null)

/**
 * 获取状态标签类型
 */
const getStatusTagType = (status?: string) => {
  let color = ''
  switch (status) {
    case '启用中':
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

// 方法
/**
 * 获取角色列表
 */
const fetchRoleList = async () => {
  loading.value = true
  roleList.value = []
  try {
    const response = await queryRoleList({
      ...queryParams,
      searchKeyword: searchForm.searchKeyword || undefined,
      enabled: searchForm.enabled || undefined,
      roleType: searchForm.roleType || undefined
    })

    if (response.success) {
      roleList.value = response.result.list || []
      total.value = response.result.total || 0
    } else {
      roleList.value = []
      total.value = 0
      ElMessage.error(response.message || '获取角色列表失败')
    }
  } catch (error) {
    console.error('获取角色列表失败:', error)
    ElMessage.error('获取角色列表失败')
    roleList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

/**
 * 搜索角色
 */
const handleSearch = async () => {
  queryParams.pageNum = 1
  await fetchRoleList()
}

/**
 * 重置搜索
 */
const handleReset = async () => {
  searchForm.searchKeyword = ''
  searchForm.enabled = undefined
  queryParams.pageNum = 1
  await fetchRoleList()
}

/**
 * 刷新列表
 */
const handleRefresh = async () => {
  await fetchRoleList()
}

const handleAdd = () => {
  currentEditRole.value = null
  isEditMode.value = false
  formDialogVisible.value = true
}

const handleEdit = (role: Role) => {
  currentEditRole.value = role
  isEditMode.value = true
  formDialogVisible.value = true
}

/**
 * 复制角色
 */
const handleCopy = async (role: any) => {
  try {
    // await ElMessageBox.confirm(`确定要复制角色 "${role.roleName}" 吗？`, '复制确认', {
    //   confirmButtonText: '确定',
    //   cancelButtonText: '取消',
    //   type: 'warning'
    // })

    const response = await copyRoleId({ roleId: role.roleId })
    if (response.success) {
      ElMessage.success('复制成功')
      await handleRefresh()
    } else {
      ElMessage.error(response.message || '复制失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('复制角色失败:', error)
      ElMessage.error(error.message || '复制角色失败')
    }
  }
}

/**
 * 删除角色
 */
const handleDelete = async (role: any) => {
  // 检查是否为超级管理员角色
  // if (role.roleName === '超级管理员' || role.roleId === 'SUPER_ADMIN') {
  //   ElMessage.warning('不能删除超级管理员角色')
  //   return
  // }

  try {
    await appDialogConfirm(
      () =>
        h('div', { class: 'flex items-center' }, [
          h(SvgIcon, { name: 'info-circle-filled', width: '20px', height: '20px' }),
          h('span', { class: 'ml-8' }, `确定要删除角色 "${role.roleName}" 吗？`)
        ]),
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )

    // 调用删除接口
    const response = await deleteRoleId({ roleId: role.roleId })
    if (response.success) {
      ElMessage.success('删除成功')
      await handleRefresh()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      console.error('删除角色失败:', error)
      // ElMessage.error('删除角色失败')
    }
  }
}

/**
 * 分页大小改变
 */
const handleSizeChange = async (size: number) => {
  queryParams.pageSize = size
  queryParams.pageNum = 1
  await fetchRoleList()
}

/**
 * 当前页改变
 */
const handleCurrentChange = async (page: number) => {
  queryParams.pageNum = page
  await fetchRoleList()
}

const handleFormSuccess = async () => {
  formDialogVisible.value = false
  await handleRefresh()
}

/**
 * 打开关联账号弹窗
 */
const handleOpenAccountRelation = (role: Role) => {
  currentRoleForRelation.value = role
  accountRelationDialogVisible.value = true
}

/**
 * 关联账号操作成功回调
 */
const handleAccountRelationSuccess = async () => {
  // 可以在这里刷新角色列表，更新关联账号数量
  await handleRefresh()
}

const menuPermissionList = ref<any>({})

// 获取权限菜单下拉选项
const fetchMenuPermissionList = async () => {
  const res = await queryMenuPermissionList({})
  if (res.success) {
    menuPermissionList.value = res.result
  }
}

// 生命周期
onMounted(async () => {
  await fetchRoleList()
  fetchMenuPermissionList()
})
</script>

<style lang="scss" scoped>
.role-management {
  height: 100%;
  display: flex;
  flex-direction: column;

  .table-card {
    flex: 1;
    display: flex;
    flex-direction: column;

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

    :deep(.el-card__body) {
      flex: 1;
      display: flex;
      flex-direction: column;
    }

    :deep(.el-table__body-wrapper) {
      .el-table__cell {
        color: #1d2129;
        font-weight: 400;
      }
    }

    .role-info {
      display: flex;
      align-items: center;
      gap: 8px;

      .role-icon {
        flex-shrink: 0;
      }

      .role-details {
        .role-name {
          font-weight: 600;
          color: #303133;
        }

        .role-code {
          font-size: 12px;
          color: #909399;
          margin-top: 2px;
          font-family: monospace;
        }
      }
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
}
</style>
