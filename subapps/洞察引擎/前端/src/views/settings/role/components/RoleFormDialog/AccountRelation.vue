<template>
  <div class="p-16 h-full flex-col">
    <div class="flex-y-center flex-between">
      <div class="fs-14 text-primary" style="font-weight: 600">关联账号列表</div>
      <div class="flex-y-center">
        <!-- <el-select v-model="queryParams.status" placeholder="关联状态" style="width: 112px">
          <el-option label="全部" :value="undefined" />
          <el-option label="已关联" :value="'1'" />
          <el-option label="未关联" :value="'0'" />
        </el-select> -->
        <el-input
          v-model="queryParams.searchKeyword"
          style="width: 172px"
          class="ml-16"
          clearable
          placeholder="请输入关键词搜索"
          :suffix-icon="Search"
          @input="handleSearch"
        />
      </div>
    </div>
    <!--  max-height="50vh" -->
    <!-- flex-auto overflow-auto -->
    <el-table
      v-loading="loading"
      height="calc(100vh - 380px)"
      :data="accountList"
      class="mt-16"
      @sort-change="sortChange"
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
      <el-table-column label="当前状态" prop="linkType" sortable width="120">
        <template #default="{ row }">
          <div class="flex-y-center">
            <div class="status-icon mr-8" :style="getStatusTagType(row.linkType)"></div>
            {{ getStatusText(row.linkType) }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.linkType === 1"
            link
            type="primary"
            @click="handleRemoveAccount(row)"
          >
            移除关联
          </el-button>
          <el-button v-else link type="primary" @click="handleRemoveAccount(row)">
            添加关联
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="queryParams.pageNum"
      v-model:page-size="queryParams.pageSize"
      :total="accountTotal"
      :page-sizes="[10, 20, 50, 100]"
      layout="->,total, prev, pager, next, sizes"
      class="mt-28"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { getUserRoleList } from '@/api/role'
// import type { AccountInfo, AccountQueryParams } from '@api/user/types.d.ts'
// import { getUserRoleList } from '@/api/role'

interface Props {
  userIds?: string[]
  roleData: any
}

interface Emits {
  (e: 'remove-account', account: any): void
}

const props = withDefaults(defineProps<Props>(), {
  userIds: () => [],
  roleData: () => {}
})

const emit = defineEmits<Emits>()

// 维护的用户ID数组
const linkedUserIds = ref<string[]>([])

// 查询参数
const queryParams = reactive<any>({
  pageNum: 1,
  pageSize: 10,
  searchKeyword: undefined,
  sort: 2
  // userName: '',
  // email: '',
  // status: undefined
})

// 响应式数据
const loading = ref(false)
const accountList = ref<any[]>([])
const accountTotal = ref(0)

/**
 * 获取状态文本
 */
const getStatusText = (linkType?: number) => {
  switch (linkType) {
    case 1:
      return '已关联'
    case 0:
      return '未关联'
    default:
      return '未关联'
  }
}

/**
 * 获取状态标签类型
 */
const getStatusTagType = (linkType?: number) => {
  let color = ''
  switch (linkType) {
    case 1:
      color = '#00B42A' // 启用
      break
    case 0:
      color = '#C9CDD4' // 停用
      break
    default:
      color = '#C9CDD4'
      break
  }
  return { backgroundColor: color }
}

/**
 * 搜索账号
 */
const handleSearch = () => {
  queryParams.pageNum = 1
  fetchAccountList()
}

const sortChange = (data: any) => {
  if (data.prop === 'linkType') {
    if (data.order === 'ascending') {
      queryParams.sort = 1
    } else if (data.order === 'descending') {
      queryParams.sort = 2
    } else {
      queryParams.sort = 2
    }
  }

  handleSearch()
}

/**
 * 移除账号关联
 */
const handleRemoveAccount = (account: any) => {
  if (account.linkType === 1) {
    // 移除关联
    const index = linkedUserIds.value.indexOf(account.userId)
    if (index > -1) {
      linkedUserIds.value.splice(index, 1)
    }
    account.linkType = 0
  } else {
    // 添加关联
    if (!linkedUserIds.value.includes(account.userId)) {
      linkedUserIds.value.push(account.userId)
    }
    account.linkType = 1
  }
  emit('remove-account', account)
}

/**
 * 分页大小改变
 */
const handleSizeChange = async (size: number) => {
  queryParams.pageSize = size
  queryParams.pageNum = 1
  await fetchAccountList()
}

/**
 * 当前页改变
 */
const handleCurrentChange = async (page: number) => {
  queryParams.pageNum = page
  await fetchAccountList()
}

/**
 * 获取账号列表
 */
const fetchAccountList = async () => {
  loading.value = true

  try {
    queryParams.roleId = props?.roleData?.roleId || undefined
    // 这里应该调用实际的API
    const response = await getUserRoleList(queryParams)
    // const response = { result: { list: [], total: 0 } }

    accountList.value = response.result.list
    accountTotal.value = response.result.total

    // 合并当前页面已关联的用户和之前维护的用户ID
    const currentPageLinkedIds =
      accountList.value
        ?.filter((account: any) => account.linkType === 1)
        ?.map((account: any) => account.userId) || []

    // 保留之前的关联用户，添加当前页面的关联用户
    const existingIds = new Set(linkedUserIds.value)
    currentPageLinkedIds.forEach(id => existingIds.add(id))
    linkedUserIds.value = Array.from(existingIds)

    // 更新linkType状态
    // if (linkedUserIds.value?.length) {
    //   accountList.value.forEach((account: any) => {
    //     account.linkType = linkedUserIds.value.includes(account.userId) ? 1 : 0
    //   })
    // }
  } catch (error) {
    console.error('获取账号列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 监听外部传入的userIds
watch(
  () => props.userIds,
  newUserIds => {
    linkedUserIds.value = [...newUserIds]
    // 更新accountList中的linkType
    accountList.value.forEach((account: any) => {
      account.linkType = linkedUserIds.value.includes(account.userId) ? 1 : 0
    })
  },
  { immediate: true }
)

onMounted(() => {
  fetchAccountList()
})

// 获取维护的用户ID数组
const getLinkedUserIds = () => {
  return linkedUserIds.value
}

defineExpose({
  fetchAccountList,
  getLinkedUserIds
})
</script>

<style lang="scss" scoped>
.status-icon {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

:deep(.el-table__inner-wrapper:before) {
  width: 0 !important;
}
</style>
