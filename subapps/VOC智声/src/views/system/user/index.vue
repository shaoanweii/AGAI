<template>
  <div class="user-management">
    <!-- 用户表格 -->
    <el-card class="table-card" shadow="never">
      <div class="user-toolbar mb-24">
        <div class="text-h3 user-toolbar__title">账号列表</div>
        <div class="user-toolbar__content">
          <div class="user-toolbar__filters">
            <el-date-picker
              class="user-filter-control"
              style="
                width: 220px;
                max-width: 100%;
                flex: none;
                --el-date-editor-width: 220px;
                --el-date-editor-monthrange-width: 220px;
              "
              v-model="timeRange"
              type="monthrange"
              value-format="YYYY-MM"
              :clearable="false"
              :disabled-date="disabledDate"
              @change="handleTimeRangeBlur"
            />
            <el-select
              class="user-filter-control"
              style="width: 180px; max-width: 100%"
              v-model="formData.roleIds"
              placeholder="角色"
              multiple
              clearable
              collapse-tags
              collapse-tags-tooltip
              :max-collapse-tags="1"
              @change="handleMultiSelectValueChange('roleIds')"
              @visible-change="visible => handleMultiSelectVisibleChange('roleIds', visible)"
            >
              <el-option
                v-for="item in roles"
                :key="item.id || ''"
                :label="item.roleName || ''"
                :value="item.id || ''"
              />
            </el-select>
            <el-select
              class="user-filter-control"
              style="width: 180px; max-width: 100%"
              v-model="formData.operationRoleIds"
              placeholder="操作权限"
              multiple
              clearable
              collapse-tags
              collapse-tags-tooltip
              :max-collapse-tags="1"
              @change="handleMultiSelectValueChange('operationRoleIds')"
              @visible-change="visible => handleMultiSelectVisibleChange('operationRoleIds', visible)"
            >
              <el-option
                v-for="item in operations"
                :key="item.id || ''"
                :label="item.roleName || ''"
                :value="item.id || ''"
              />
            </el-select>

            <el-cascader
              class="user-filter-control"
              style="width: 350px; max-width: 100%"
              v-model="formData.deptId"
              :options="departs"
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
              show-checked-strategy="parent"
              @blur="handleSearch"
            />
            <!-- <el-select-v2
              v-model="searchForm.deptId"
              :options="departs"
              :props="{ value: 'code', label: 'name' }"
              filterable
              placeholder="部门"
              clearable
              style="width: 150px"
              @change="handleSearch"
            >
            </el-select-v2> -->

            <el-select
              class="user-filter-control"
              style="width: 100px; max-width: 100%"
              v-model="formData.status"
              placeholder="启用状态"
              clearable
              @change="handleSearch"
            >
              <el-option label="启用" value="1" />
              <el-option label="停用" value="0" />
            </el-select>
            <el-input
              class="user-filter-control"
              style="width: 172px; max-width: 100%"
              v-model="formData.userName"
              placeholder="请输入关键词搜索"
              clearable
              :suffix-icon="Search"
              @change="handleSearch"
            />
          </div>
          <div v-if="hasDownloadPermission" class="user-toolbar__actions">
            <el-button type="primary" @click="handleDownload">
              <SvgIcon name="download-01" width="16px" height="16px" color="#fff" class="mr-6" />
              下载数据</el-button
            >
          </div>
        </div>
      </div>
      <el-table
        v-loading="loading"
        :data="dataList as any"
        max-height="calc(100vh - 84px - 48px - 106px - 32px - 10px)"
        class="flex-auto overflow-auto"
        :default-sort="{ prop: 'originalListenCount', order: 'descending' }"
        @sort-change="handleSortChange"
      >
        <el-table-column type="index" label="#" width="56" align="center" />
        <el-table-column label="员工姓名" min-width="150">
          <template #default="{ row }">
            <div class="user-info">
              <div class="user-details">
                <div class="username">{{ row.userName || '未设置' }}</div>
                <!--                <div class="nickname">{{ row.userName || '未设置' }}</div>-->
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="employeeId" label="员工工号" min-width="120" />
        <el-table-column prop="secondDeptName" label="二级部门" sortable="custom" min-width="150" />
        <el-table-column prop="thirdDeptName" label="三级部门" sortable="custom" min-width="150" />
        <!-- <el-table-column prop="deptName" label="所属部门" /> -->
        <el-table-column prop="roleName" label="访问角色" sortable="custom" min-width="120" />
        <el-table-column
          prop="operationRoleName"
          label="操作权限"
          sortable="custom"
          min-width="120"
        />
        <el-table-column
          prop="lastLoginTime"
          label="最近登录时间"
          sortable="custom"
          min-width="180"
        />
        <el-table-column prop="loginCounts" label="登录次数" sortable="custom" min-width="110" />
        <el-table-column
          prop="originalListenCount"
          label="全部原声聆听数"
          sortable="custom"
          min-width="150"
        />
        <el-table-column
          prop="complainOriginalListenCount"
          label="抱怨原声聆听数"
          sortable="custom"
          min-width="150"
        />
        <el-table-column prop="visitDuration" label="浏览时长" sortable="custom" min-width="150" />
        <!-- <el-table-column prop="loginCounts" label="聆听任务完成率" /> -->
        <!-- <el-table-column
          prop="listenTaskCompleteRate"
          label="聆听任务完成率"
          sortable="custom"
          min-width="150"
        >
          <template #default="{ row }">
            <span>{{ row.listenTaskCompleteRate }}</span>
          </template>
        </el-table-column> -->

        <el-table-column label="当前状态" prop="status" sortable="custom" min-width="120">
          <template #default="{ row }">
            <div class="flex-y-center">
              <div class="status-icon mr-8" :style="getStatusTagType(row.status)"></div>
              {{ getStatusText(row.status) }}
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)"> 编辑 </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="->,total, prev, pager, next, sizes"
        />
      </div>
    </el-card>
    <!--    弹框-->
    <UserFormDialog
      v-model:visible="formDialogData.visible"
      :user-data="formDialogData.currentUser"
      :roles="roles"
      :operations="operations"
      :is-edit="formDialogData.isEditMode"
      @success="handleFormSuccess"
    />

    <DownloadDialog v-model:visible="downloadDialogVisible" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { findAccountInfoList } from '@/api/user'
import type { AccountInfo, AccountQueryParams, RoleOption } from '@/api/user/types'
import { queryRoleALlList } from '@api/user'
import { ElMessage } from 'element-plus'
import UserFormDialog from '@views/system/user/components/UserFormDialog.vue'
import { Search } from '@element-plus/icons-vue'
import { findDepartTree } from '@/api/common'
import dayjs from 'dayjs'
import DownloadDialog from '@views/system/user/components/DownloadDialog.vue'
import { useToggle } from '@/hooks/useToggle'
import { useTable } from '@/hooks/useTable'
import { exportAccountInfo } from '@/api/downloadManagement'
import { FunctionPermission } from '@/constants/btnPermMap'
import { useUserStore } from '@/store'

defineOptions({
  name: 'UserManagement'
})

interface UserFilterForm {
  userName: string
  deptId: string[]
  roleIds: string[]
  operationRoleIds: string[]
  status?: string
  completeRate?: number
}

type AccountExportParams = Omit<AccountQueryParams, 'pageNum' | 'pageSize'>
type MultiSelectFilterField = 'roleIds' | 'operationRoleIds'

const userStore = useUserStore()
const hasDownloadPermission = computed(() =>
  userStore.checkfunctionPermission(FunctionPermission.ACCOUNT_DATA_DOWNLOAD)
)
const currentMonth = dayjs().format('YYYY-MM')
const timeRange = ref<[string, string]>([currentMonth, currentMonth])
const [downloadDialogVisible, toggleDownloadDialogVisible] = useToggle(false)
const multiSelectSearchState = reactive<
  Record<MultiSelectFilterField, { panelVisible: boolean; hasPendingChange: boolean }>
>({
  roleIds: {
    panelVisible: false,
    hasPendingChange: false
  },
  operationRoleIds: {
    panelVisible: false,
    hasPendingChange: false
  }
})

// 排序状态管理 - 默认全部原声聆听数降序
const sortOrder = ref<{ prop: string; order: 'asc' | 'desc' }>({
  prop: 'originalListenCount',
  order: 'desc'
})

/**
 * 禁用日期函数
 * 限制：只能选择2025-10到当前月份之间的月份
 */
const disabledDate = (date: Date) => {
  const year = date.getFullYear()
  const month = date.getMonth() + 1 // getMonth() 返回 0-11，需要加1

  // 获取当前日期
  const now = new Date()
  const currentYear = now.getFullYear()
  const currentMonth = now.getMonth() + 1

  // 禁用2025-10之前的月份（即2025年9月及之前的所有月份）
  if (year < 2025 || (year === 2025 && month < 10)) {
    return true
  }

  // 禁用当前月份之后的月份
  if (year > currentYear || (year === currentYear && month > currentMonth)) {
    return true
  }

  return false
}

/**
 * 将月份格式转换为开始和结束日期
 * @param monthRange 月份范围，格式：[YYYY-MM, YYYY-MM]
 * @returns 包含 startTime 和 endTime 的对象
 */
const convertMonthToDateRange = (monthRange: [string, string] | undefined) => {
  if (!monthRange || !monthRange[0] || !monthRange[1]) {
    return { startTime: undefined, endTime: undefined }
  }

  const [startMonth, endMonth] = monthRange
  const startTime = dayjs(startMonth).startOf('month').format('YYYY-MM-DD')
  const endTime = dayjs(endMonth).endOf('month').format('YYYY-MM-DD')

  return { startTime, endTime }
}

/**
 * 规范化多选筛选项
 * @param value 多选下拉值
 * @returns 有效筛选数组，无值时返回 undefined，避免向后端传空数组
 */
const normalizeMultiSelectFilter = (value: string[]) => {
  return value.length > 0 ? value : undefined
}

// 使用 useTable hook，直接在返回值中解构 tableState
const {
  tableState: { loading, dataList, currentPage, pageSize, total },
  tableMethods,
  formData
} = useTable<UserFilterForm>({
  immediate: false, // 手动控制初始化时机
  initialFormData: {
    userName: '',
    deptId: [],
    roleIds: [],
    operationRoleIds: [],
    status: undefined as string | undefined,
    completeRate: undefined as number | undefined
  },
  fetchDataApi: async () => {
    // 合并查询参数：formData、分页参数和时间参数
    const { startTime, endTime } = convertMonthToDateRange(timeRange.value)
    const params: AccountQueryParams = {
      ...formData.value,
      roleIds: normalizeMultiSelectFilter(formData.value.roleIds),
      operationRoleIds: normalizeMultiSelectFilter(formData.value.operationRoleIds),
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      startTime,
      endTime
    }

    // 添加排序参数（默认全部原声聆听数降序）
    if (sortOrder.value) {
      params.order = `${sortOrder.value.prop} ${sortOrder.value.order}`
    }

    const response = await findAccountInfoList(params)
    if (response.success) {
      // 处理不同的响应数据结构（参考原代码 314-317 行）
      const result = response.result
      if (result) {
        // 如果是分页数据结构 (PageInfo)
        if (result.list && Array.isArray(result.list)) {
          return {
            list: result.list,
            total: result.total || 0
          }
        }
        // 如果是直接的数组数据
        else if (Array.isArray(result)) {
          return {
            list: result,
            total: result.length
          }
        }
        // 如果有records字段 (其他分页格式)
        else if (result.records && Array.isArray(result.records)) {
          return {
            list: result.records,
            total: result.total || 0
          }
        }
        // 默认处理
        else {
          return {
            list: [],
            total: 0
          }
        }
      } else {
        return {
          list: [],
          total: 0
        }
      }
    } else {
      ElMessage.error(response.message || '获取账号列表失败')
      return {
        list: [],
        total: 0
      }
    }
  }
})

// 表单对话框相关状态
const formDialogData = reactive<{
  visible: boolean
  currentUser: AccountInfo | null
  isEditMode: boolean
}>({
  visible: false,
  currentUser: null,
  isEditMode: false
})

//获取角色下拉
const roles = ref<RoleOption[]>([])
const operations = ref<RoleOption[]>([])
const getRoleList = async () => {
  const response = await queryRoleALlList({})
  if (response.success) {
    roles.value = response.result?.filter((el: any) => el.roleType?.toString() === '1') || []
    operations.value = response.result?.filter((el: any) => el.roleType?.toString() === '2') || []
  }
}

//获取部门下拉
const departs = ref<any[]>([])
const getDepartList = async () => {
  // findDepartTree()
  // const response = await findDepartList()
  const response = await findDepartTree()
  if (response.success) {
    departs.value = response.result || []
  }
}

//获取完成度下拉
// const completionList = ref<any[]>([])
// const getCompletionList = async () => {
//   const response = await getDictItemsByDict('completion_rate')
//   if (response.success) {
//     completionList.value = response.result || []
//   }
// }

/**
 * 记录多选筛选项的值变化。
 * 面板展开时只标记本次交互有变更，避免每次勾选都立即触发查询；
 * 面板未展开时（如清空标签、删除已选项）则直接查询，保证交互即时生效。
 * @param field 多选筛选字段
 */
const handleMultiSelectValueChange = async (field: MultiSelectFilterField) => {
  const currentFieldState = multiSelectSearchState[field]
  if (currentFieldState.panelVisible) {
    currentFieldState.hasPendingChange = true
    return
  }

  await handleSearch()
}

/**
 * 处理多选下拉的展开/关闭。
 * 仅在关闭面板且本次交互确实修改过值时触发一次查询，避免多选过程中重复请求接口。
 * @param field 多选筛选字段
 * @param visible 当前面板是否可见
 */
const handleMultiSelectVisibleChange = async (
  field: MultiSelectFilterField,
  visible: boolean
) => {
  const currentFieldState = multiSelectSearchState[field]
  currentFieldState.panelVisible = visible

  if (visible) {
    currentFieldState.hasPendingChange = false
    return
  }

  if (!currentFieldState.hasPendingChange) {
    return
  }

  currentFieldState.hasPendingChange = false
  await handleSearch()
}

/**
 * 处理时间范围选择器的 change 事件
 */
const handleTimeRangeBlur = async () => {
  // 重置到第一页并刷新数据
  currentPage.value = 1
  await tableMethods.getList()
}

/**
 * 处理表格排序变化
 * @param param0 排序参数对象
 */
const handleSortChange = ({
  prop,
  order
}: {
  prop: string
  order: 'ascending' | 'descending' | null
}) => {
  if (order === null) {
    // 清除排序时，恢复默认排序（全部原声聆听数降序）
    sortOrder.value = {
      prop: 'originalListenCount',
      order: 'desc'
    }
  } else {
    // 转换 Element Plus 排序值为后端格式
    const backendOrder = order === 'ascending' ? 'asc' : 'desc'
    sortOrder.value = {
      prop,
      order: backendOrder
    }
  }
  // 刷新数据
  tableMethods.getList()
}

/**
 * 搜索处理
 */
const handleSearch = async () => {
  // 重置到第一页并刷新数据
  currentPage.value = 1
  await tableMethods.handleQuery()
}

/**
 * 刷新数据
 */
const handleRefresh = async () => {
  await tableMethods.refresh()
}

/**
 * 下载数据
 */
const handleDownload = async () => {
  try {
    // 收集查询参数
    const { startTime, endTime } = convertMonthToDateRange(timeRange.value)
    const downloadParams: AccountExportParams = {
      ...formData.value,
      roleIds: normalizeMultiSelectFilter(formData.value.roleIds),
      operationRoleIds: normalizeMultiSelectFilter(formData.value.operationRoleIds),
      startTime,
      endTime,
      order: `${sortOrder.value.prop} ${sortOrder.value.order}`
    }

    const response = await exportAccountInfo(downloadParams)
    if (response.success) {
      // 接口调用成功，打开弹窗
      toggleDownloadDialogVisible(true)
    } else {
      ElMessage.error(response.message || '下载数据失败')
    }
  } catch (error) {
    console.error('下载数据失败:', error)
    ElMessage.error('下载数据失败，请稍后重试')
  }
}

/**
 * 编辑用户 - TODO: 需要实现账号编辑功能
 */
const handleEdit = (user: AccountInfo | null) => {
  formDialogData.visible = true
  formDialogData.currentUser = user
  formDialogData.isEditMode = !!user
}

/**
 * 表单提交成功
 */
const handleFormSuccess = () => {
  handleRefresh()
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

/**
 * 获取状态文本
 */
const getStatusText = (status?: string) => {
  switch (status) {
    case '1':
      return '已启用'
    case '0':
      return '未启用'
    default:
      return '未知'
  }
}

// 生命周期
onMounted(async () => {
  // 初始化数据
  await tableMethods.getList()
  getRoleList()
  getDepartList()
  // getCompletionList()
})
</script>

<style lang="scss" scoped>
.user-management {
  height: 100%;
  display: flex;
  flex-direction: column;

  .table-card {
    flex: 1;
    display: flex;
    flex-direction: column;

    .user-toolbar {
      display: flex;
      align-items: start;
      justify-content: space-between;
      flex-wrap: wrap;
      gap: 16px;

      &__title {
        display: flex;
        align-items: center;
        min-height: 32px;
        font-weight: 600;
        white-space: nowrap;
      }

      &__content {
        display: flex;
        flex: 1 1 auto;
        flex-wrap: wrap;
        align-items: flex-start;
        justify-content: flex-end;
        gap: 16px;
        min-width: 0;
        margin-left: auto;
      }

      &__filters {
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        justify-content: flex-end;
        gap: 16px;
        flex: 0 1 auto;
        min-width: 0;
        max-width: 100%;
      }

      &__actions {
        display: flex;
        flex: 0 0 auto;
        align-items: center;
        justify-content: flex-end;
        min-height: 32px;
        white-space: nowrap;
      }
    }

    .user-filter-control {
      flex: 0 0 auto;
      max-width: 100%;
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

    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;

      .user-avatar {
        flex-shrink: 0;
      }

      .user-details {
        word-break: break-all;
        .nickname {
          font-size: 12px;
          color: #909399;
          margin-top: 2px;
        }
      }
    }

    .role-tag {
      margin-right: 4px;
      margin-bottom: 2px;
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

// 响应式设计
@media (max-width: 960px) {
  .user-management {
    .table-card {
      .user-toolbar {
        &__content {
          flex-basis: 100%;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .user-management {
    .table-card {
      .user-toolbar {
        &__content,
        &__filters {
          gap: 12px;
        }
      }

      .pagination-wrapper {
        text-align: center;
      }
    }
  }
}
</style>
