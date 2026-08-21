<template>
  <div class="account-list-tab">
    <div class="account-list-header">
      <div class="text-h4" style="font-weight: 600">账号列表</div>
      <div class="filter-bar flex-y-center">
        <el-cascader
          v-model="formData.deptIdList"
          :options="props.departs"
          filterable
          placeholder="部门"
          clearable
          :props="{
            value: 'code',
            label: 'name',
            children: 'child',
            multiple: true,
            emitPath: false,
            checkStrictly: true
          }"
          :max-collapse-tags="1"
          collapse-tags
          style="width: 240px"
          show-checked-strategy="parent"
          @blur="handleSearch"
        />
        <el-input
          v-model="formData.searchKeyword"
          placeholder="请输入关键词"
          clearable
          style="width: 150px; margin-left: 16px"
          @keyup.enter="handleSearch"
          @blur="handleSearch"
        >
          <template #suffix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" style="margin-left: 16px" @click="handleBatchOperation">
          {{ tabType === 'linked' ? '批量移除' : '批量添加' }}
        </el-button>
      </div>
    </div>

    <div class="table-wrapper">
      <el-table
        v-loading="loading"
        :data="accountListData"
        :max-height="tableMaxHeight"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="员工姓名" prop="userName" width="100" />
        <el-table-column label="员工工号" prop="employeeId" width="120" />
        <el-table-column label="二级部门" prop="secondDeptName" />
        <el-table-column label="三级部门" prop="thirdDeptName" />
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleSingleOperation(row)">
              {{ tabType === 'linked' ? '移除关联' : '添加关联' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="->,total, prev, pager, next, sizes"
      />
    </div>

    <!-- 确认弹窗 -->
    <ConfirmDialog
      v-model:visible="confirmDialogVisible"
      :title="confirmDialogTitle"
      :message="confirmDialogMessage"
      @confirm="handleConfirmOperation"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { debounce } from 'lodash-es'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { Role } from '@/types/system'
import { accountList, batchAddRole, batchDeleteRole } from '@/api/role'
import type { AccountListUserInfo } from '@/api/role/types'
import { useTable } from '@/hooks/useTable'
import ConfirmDialog from './ConfirmDialog.vue'

defineOptions({
  name: 'AccountListTab'
})

interface Props {
  tabType: 'linked' | 'unlinked'
  roleData?: Role | null
  departs?: any[]
}

const props = withDefaults(defineProps<Props>(), {
  roleData: null,
  departs: () => []
})

const emit = defineEmits<{
  (e: 'changed'): void
}>()

// 计算表格最大高度：70vh弹窗 - 头部64px - tabs 40px - 内容padding 48px - 列表头部100px - 分页器50px - tab内padding 48px
const tableMaxHeight = computed(() => {
  return 'calc(70vh - 350px)'
})

const selectedRows = ref<AccountListUserInfo[]>([])

// 确认弹窗
const confirmDialogVisible = ref(false)
const confirmDialogTitle = ref('')
const confirmDialogMessage = ref('')
const pendingOperation = ref<{
  type: 'single' | 'batch'
  row?: any
  rows?: any[]
} | null>(null)

const {
  tableState: { loading, dataList: accountListData, currentPage, pageSize, total },
  tableMethods,
  formData
} = useTable({
  immediate: false,
  initialFormData: {
    deptIdList: [] as string[],
    searchKeyword: undefined as string | undefined
  },
  fetchDataApi: async () => {
    const accountType = props.tabType === 'linked' ? '1' : '2'
    const deptIdList = formData.value.deptIdList || []
    const params = {
      accountType,
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      deptIdList: deptIdList.length ? deptIdList : undefined,
      searchKeyword: formData.value.searchKeyword || undefined,
      roleId: props.roleData?.roleId
    }

    const response = await accountList(params)
    if (!response.success) {
      ElMessage.error(response.message || '获取账号列表失败')
      return { list: [], total: 0 }
    }

    const result: any = response.result
    if (!result) return { list: [], total: 0 }
    if (Array.isArray(result)) return { list: result, total: result.length }
    if (Array.isArray(result.list)) return { list: result.list, total: Number(result.total || 0) }
    if (Array.isArray(result.records))
      return { list: result.records, total: Number(result.total || 0) }
    return { list: [], total: 0 }
  }
})

const handleQuery = () => {
  tableMethods.handleQuery()
}

// 搜索
const handleSearch = debounce(() => {
  currentPage.value = 1
  handleQuery()
}, 300)

// 选择变化
const handleSelectionChange = (selection: AccountListUserInfo[]) => {
  selectedRows.value = selection
}

// 单个操作
const handleSingleOperation = (row: any) => {
  pendingOperation.value = { type: 'single', row }
  if (props.tabType === 'linked') {
    confirmDialogTitle.value = '移除关联'
    confirmDialogMessage.value = '是否确认移除关联?'
  } else {
    confirmDialogTitle.value = '添加关联'
    confirmDialogMessage.value = '是否确认添加关联?'
  }
  confirmDialogVisible.value = true
}

// 批量操作
const handleBatchOperation = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请至少选择一条记录')
    return
  }

  pendingOperation.value = { type: 'batch', rows: [...selectedRows.value] }
  if (props.tabType === 'linked') {
    confirmDialogTitle.value = '批量移除关联'
    confirmDialogMessage.value = '是否确认批量移除关联?'
  } else {
    confirmDialogTitle.value = '批量添加关联'
    confirmDialogMessage.value = '是否确认批量添加关联?'
  }
  confirmDialogVisible.value = true
}

// 确认操作
const handleConfirmOperation = async () => {
  if (!pendingOperation.value) return

  const roleId = props.roleData?.roleId
  if (!roleId) {
    ElMessage.warning('未获取到角色信息')
    confirmDialogVisible.value = false
    pendingOperation.value = null
    return
  }

  const getUserIdList = () => {
    if (pendingOperation.value?.type === 'single') {
      const userId = pendingOperation.value.row?.userId
      return userId ? [userId] : []
    }
    const rows = pendingOperation.value?.rows || []
    return rows.map(item => item.userId).filter(Boolean)
  }

  const userIdList = getUserIdList()
  if (userIdList.length === 0) {
    ElMessage.warning('未选择账号')
    confirmDialogVisible.value = false
    pendingOperation.value = null
    return
  }

  try {
    const request =
      props.tabType === 'linked'
        ? batchDeleteRole({ roleId, userIdList })
        : batchAddRole({ roleId, userIdList })

    const response = await request
    if (!response.success) {
      ElMessage.error(response.message || '操作失败')
      return
    }

    const successMessage =
      pendingOperation.value.type === 'single'
        ? props.tabType === 'linked'
          ? '移除关联成功'
          : '添加关联成功'
        : props.tabType === 'linked'
          ? '批量移除关联成功'
          : '批量添加关联成功'

    ElMessage.success(successMessage)

    confirmDialogVisible.value = false
    pendingOperation.value = null

    tableMethods.getList()
    selectedRows.value = []
    emit('changed')
  } catch (error) {
    console.error('关联账号操作失败:', error)
    ElMessage.error('操作失败')
  }
}

defineExpose({
  handleQuery
})
</script>

<style lang="scss" scoped>
.account-list-tab {
  padding: 24px;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.account-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.filter-bar {
  display: flex;
  align-items: center;
}

.table-wrapper {
  flex: 1;
  margin-top: 16px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.pagination-wrapper {
  margin-top: 16px;
  flex-shrink: 0;
}

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

:deep(.el-table__body-wrapper) {
  .el-table__cell {
    color: #1d2129;
    font-weight: 400;
  }
}
</style>
