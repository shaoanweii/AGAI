<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import searchPng from '@/assets/imgs/rules/search.png'
import { listResourceDesc, insertDesc, updateDesc, changeResourceStatus } from '@/api/dataCenter'
import useConditions from '@/hooks/useConditions'

const hub = reactive({
  loading: false,
  queryParams: {
    pageNum: 1,
    pageSize: 20,
    keyword: ''
  },
  total: 0,
  ruleList: [] as Array<any>
})

const selectedIds = ref<string[]>([])

// 列定义
const columns = [
  {
    label: '关键词',
    prop: 'name'
  },
  {
    label: '创建时间',
    prop: 'createTime',
    width: '200px'
  },
  {
    label: '启用状态',
    prop: 'status',
    width: '140px'
  }
]

const keywordSearch = () => {
  fetchList()
}

const curLeftItem = ref()
const fetchList = async (resetScroll = true) => {
  try {
    hub.loading = true
    if (resetScroll) {
      hub.ruleList = []
    }
    const params: any = {
      pageNum: hub.queryParams.pageNum,
      pageSize: hub.queryParams.pageSize,
      resourceId: curLeftItem.value?.id,
      nameDescFilter: hub.queryParams.keyword
    }

    const response = (await listResourceDesc(params)) as any
    if ((response.success || response.code === '200') && response.result) {
      const { list, records, total } = response.result as any
      hub.ruleList = list || records || []
      hub.total = total || 0
    } else {
      ElMessage.error(response.message || '获取分组详情失败')
      hub.ruleList = []
    }
  } catch (error: any) {
    console.error('获取分组详情失败:', error)
    ElMessage.error('获取分组详情失败，请稍后重试')
    hub.ruleList = []
  } finally {
    hub.loading = false
  }
}

//编辑
const dialogVisible = ref(false)
const dialogFormRef = ref<FormInstance>()
const dialogForm = reactive({
  id: '',
  name: '',
  status: 'Enabled'
})

const dialogRules = {
  name: [{ required: true, message: '请输入关键词', trigger: 'blur' }],
  status: [{ required: true, message: '请选择启用状态', trigger: 'change' }]
}
const defaultStatusOptions = [
  { label: '启用', value: 'Enabled' },
  { label: '禁用', value: 'Disabled' }
]

const { conditions } = useConditions({ url: '/insights/insDataResourceDesc/conditions' })

const statusOptions = computed(() => {
  const list = (conditions?.ruleStatus as any[]) || []
  if (!Array.isArray(list) || list.length === 0) {
    return defaultStatusOptions
  }
  return list.map(item => ({ label: item.value, value: item.key }))
})

const statusLabelMap = computed(() => {
  const map: Record<string, string> = {}
  statusOptions.value.forEach(opt => {
    map[opt.value] = opt.label
  })
  return map
})

const dialogNamePlaceholder = computed(() =>
  dialogForm.id ? '请输入关键词' : '请输入关键词，批量新增请以逗号,隔开'
)

const openDialog = (record?: any) => {
  if (!curLeftItem.value?.id) {
    ElMessage.warning('请先选择规则分组')
    return
  }
  dialogForm.id = record?.id || ''
  dialogForm.name = record?.name || ''
  const defaultStatus = statusOptions.value[0]?.value || 'Enabled'
  dialogForm.status =
    record?.status && statusLabelMap.value?.[record.status] ? record.status : defaultStatus
  dialogVisible.value = true
}

watch(
  () => statusOptions.value,
  opts => {
    if (!dialogVisible.value && !dialogForm.id && opts.length) {
      dialogForm.status = opts[0].value
    }
  },
  { immediate: true }
)

const handleEdit = (row: any) => {
  openDialog(row)
}

const handleCreate = () => {
  openDialog()
}

//刷新列表
const refreshList = () => {
  fetchList()
}

const handleSelectionChange = (rows: any[]) => {
  selectedIds.value = rows.map(item => item.id)
}

const handleBatchChange = async (status: string) => {
  if (!selectedIds.value.length) {
    return ElMessage.warning('请先选择关键词')
  }
  try {
    const response = (await changeResourceStatus({ ids: selectedIds.value, status })) as any
    if (response.success || response.code === '200') {
      ElMessage.success(response.message || '操作成功')
      selectedIds.value = []
      fetchList(false)
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error: any) {
    ElMessage.error(error?.message || '操作失败')
  }
}

// 分页
const handleSizeChange = (val: number) => {
  hub.queryParams.pageSize = val
  fetchList()
}

// 分页
const handleCurrentChange = (val: number) => {
  hub.queryParams.pageNum = val
  fetchList()
}

// 删除分类
const handleDelete = async (item: any) => {
  //调用接口
  // try {
  //   await ElMessageBox.confirm(`确定要删除分类 "${item.name}" 吗？`, '删除确认', {
  //     confirmButtonText: '确定',
  //     cancelButtonText: '取消',
  //     type: 'warning'
  //   })
  //   const response = await deleteSpecialType({ id: item.id })
  //   if (response.success) {
  //     ElMessage.success('删除分类成功')
  //     fetchList()
  //   } else {
  //     ElMessage.error(response.message || '删除分类失败')
  //   }
  // } catch (e) {
  //   console.error('删除分类失败:', e)
  // }
}

const submitDialog = async () => {
  if (!dialogFormRef.value) return
  try {
    await dialogFormRef.value.validate()
    if (!curLeftItem.value?.id) {
      ElMessage.error('请选择规则分组')
      return
    }
    const payload: Record<string, any> = {
      resourceId: curLeftItem.value?.id,
      name: dialogForm.name,
      status: dialogForm.status
    }
    let response
    if (dialogForm.id) {
      payload.id = dialogForm.id
      response = await updateDesc(payload)
    } else {
      response = await insertDesc(payload)
    }
    if (response.success || response.code === '200') {
      ElMessage.success(dialogForm.id ? '编辑成功' : '新增成功')
      dialogVisible.value = false
      fetchList(!dialogForm.id)
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error: any) {
    if (error?.message) {
      ElMessage.error(error.message)
    }
  }
}

const leftChange = (leftItem: any) => {
  curLeftItem.value = leftItem
  fetchList()
}

defineExpose({
  leftChange
})
</script>
<template>
  <div class="h-full flex-col">
    <div class="flex-between items-center mb-24">
      <div class="header-title-class">规则列表</div>
      <div>
        <el-input
          v-model="hub.queryParams.keyword"
          style="width: 172px"
          clearable
          placeholder="请输入关键词搜索"
          @change="keywordSearch"
        >
          <template #suffix>
            <el-image :src="searchPng" style="width: 20px; height: 20px" />
          </template>
        </el-input>
        <el-dropdown trigger="click" placement="bottom-end" @command="handleBatchChange">
          <el-button class="ml-16" text bg :disabled="!selectedIds.length">批量操作</el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item v-for="opt in statusOptions" :key="opt.value" :command="opt.value">
                {{ opt.label }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button style="margin-left: 16px" type="primary" @click="handleCreate">
          <template #icon>
            <el-icon>
              <el-icon-plus />
            </el-icon>
          </template>
          新建规则</el-button
        >
      </div>
    </div>
    <el-table
      v-loading="hub.loading"
      :data="hub.ruleList"
      class="flex-auto overflow-auto"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="56" align="center" />
      <template v-for="(column, index) in columns" :key="index">
        <el-table-column
          v-if="column.prop === 'status'"
          :prop="column.prop"
          :label="column.label"
          :width="column.width"
        >
          <template #default="{ row }">
            <span class="status-badge" :class="`status-${row.status}`"></span>
            {{ statusLabelMap[row.status] || row.statusText || '-' }}
          </template>
        </el-table-column>
        <el-table-column v-else :prop="column.prop" :label="column.label" :width="column.width" />
      </template>

      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <div class="flex-y-center">
            <el-button link type="primary" @click="handleEdit(row)"> 编辑 </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-if="hub.total > 0"
      v-model:current-page="hub.queryParams.pageNum"
      v-model:page-size="hub.queryParams.pageSize"
      :total="hub.total"
      :page-sizes="[10, 20, 50, 100]"
      layout="->,total, prev, pager, next, sizes"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      class="pt-16"
    />
    <AppDialog
      v-model:visible="dialogVisible"
      :title="dialogForm.id ? '编辑关键词' : '新建关键词'"
      :confirm="submitDialog"
    >
      <el-form
        ref="dialogFormRef"
        :model="dialogForm"
        :rules="dialogRules"
        label-width="80px"
        class="keyword-dialog-form"
      >
        <el-form-item label="关键词" prop="name">
          <el-input v-model.trim="dialogForm.name" :placeholder="dialogNamePlaceholder" clearable />
        </el-form-item>
        <el-form-item label="是否启用" prop="status">
          <el-radio-group v-model="dialogForm.status">
            <el-radio v-for="opt in statusOptions" :key="opt.value" :label="opt.value">
              {{ opt.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </AppDialog>
  </div>
</template>
<style lang="scss" scoped>
.header-title-class {
  font-weight: 600;
  font-size: 20px;
  color: #333333;
  line-height: 32px;
}
:deep(.el-table .el-table__cell) {
  height: 55px;
  padding: 0 !important;
}

:deep(.el-table__header) {
  .el-table__cell {
    color: #1d2129;
    font-weight: 500;
    font-size: 14px;
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
    font-size: 14px;
  }
}

:deep(.el-table--fit .el-table__inner-wrapper:before) {
  width: 0 !important;
}

.keyword-dialog-form {
  width: 420px;
  margin: 0 auto;
  :deep(.el-form-item) {
    margin-bottom: 20px;
  }
}

.status-badge {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 6px;
  background: #c9cdd4;

  &.status-Enabled {
    background: #00b42a;
  }

  &.status-Disabled {
    background: #c9cdd4;
  }
}
</style>
