<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import searchPng from '@/assets/imgs/rules/search.png'
import useConditions from '@/hooks/useConditions'
import {
  changeAccountLexiconStatus,
  findAccountLexiconInfo,
  findAccountLexiconList,
  getAccountLexiconChannelTree,
  saveAccountLexiconDetails,
  updateAccountLexiconDetails
} from '@/api/accountLibrary'

const hub = reactive({
  loading: false,
  queryParams: {
    pageNum: 1,
    pageSize: 20,
    keyword: '',
    channel: ''
  },
  total: 0,
  ruleList: [] as Array<any>
})

const selectedIds = ref<string[]>([])
const curLeftItem = ref<any>()
const channelTree = ref<any[]>([])
const { conditions } = useConditions({ url: '/insights/accountLexicon/conditions' })

// 列显示配置（顺序：账号 | ID | 渠道 | 创建时间 | 启用状态）
const columns = [
  {
    label: '账号',
    prop: 'accountName'
  },
  {
    label: 'ID',
    prop: 'accountId',
    width: '160px'
  },
  {
    label: '渠道',
    prop: 'channel',
    width: '160px'
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

const fallbackStatusOptions = [
  { label: '启用', value: '1' },
  { label: '禁用', value: '0' }
]

const statusOptions = computed(() => {
  const dict = conditions.ruleStatus
  if (Array.isArray(dict) && dict.length) {
    return dict.map(item => ({
      label: item.value,
      value: item.key
    }))
  }
  return fallbackStatusOptions
})

const statusLabelMap = computed(() => {
  return statusOptions.value.reduce((acc, cur) => {
    acc[cur.value] = cur.label
    return acc
  }, {} as Record<string, string>)
})

const normalizeChannelNodes = (nodes: any[], level = 1): any[] => {
  if (!Array.isArray(nodes)) return []
  return nodes.map(node => {
    const rawCode = node.code ?? node.channelCode ?? node.value
    const value =
      rawCode !== undefined && rawCode !== null && rawCode !== ''
        ? String(rawCode)
        : node.id !== undefined && node.id !== null
        ? String(node.id)
        : node.value ?? ''
    const label = node.name || node.label || node.value || ''
    const childrenSource = node.children ? node.children : node.child ? node.child : undefined
    const children = childrenSource ? normalizeChannelNodes(childrenSource, level + 1) : undefined
    const normalized: any = {
      value,
      label
    }
    if (children && children.length) {
      normalized.children = children
    }
    return normalized
  })
}

const normalizedChannelTree = computed(() => normalizeChannelNodes(channelTree.value))
const channelOptions = computed(() => normalizedChannelTree.value)
const firstLevelChannelValues = computed(() =>
  (normalizedChannelTree.value || []).map(node => node.value).filter(Boolean)
)
const channelCascaderProps = {
  value: 'value',
  label: 'label',
  children: 'children',
  emitPath: false,
  checkStrictly: true
}

const channelLabelMap = computed(() => {
  const map: Record<string, string> = {}
  const traverse = (nodes: any[]) => {
    nodes?.forEach(node => {
      const value = node.value
      const label = node.label
      if (value) {
        map[value] = label
      }
      if (node.children?.length) {
        traverse(node.children)
      }
    })
  }
  traverse(normalizedChannelTree.value)
  return map
})

const keywordSearch = () => {
  hub.queryParams.pageNum = 1
  fetchList()
}

const handleChannelFilterChange = (value: any) => {
  let normalized = ''
  if (Array.isArray(value) && value.length) {
    normalized = String(value[value.length - 1])
  } else if (value) {
    normalized = String(value)
  }
  hub.queryParams.channel = normalized
  hub.queryParams.pageNum = 1
  fetchList()
}

const getCurrentResourceId = () => {
  const id = curLeftItem.value?.id
  return id !== undefined && id !== null && id !== '' ? String(id) : ''
}

const buildListParams = () => {
  const params: Record<string, any> = {
    pageNum: hub.queryParams.pageNum,
    pageSize: hub.queryParams.pageSize,
    resourceId: getCurrentResourceId()
  }
  const keyword = hub.queryParams.keyword?.trim()
  if (keyword) {
    params.keyword = keyword
  }
  if (hub.queryParams.channel) {
    params.channel = hub.queryParams.channel
  }
  return params
}

const fetchList = async (resetScroll = true) => {
  const resourceId = getCurrentResourceId()
  if (!resourceId) {
    hub.ruleList = []
    hub.total = 0
    return
  }
  try {
    hub.loading = true
    if (resetScroll) {
      hub.ruleList = []
    }
    const response = (await findAccountLexiconList(buildListParams())) as any
    if ((response.success || response.code === '200') && response.result) {
      const result: any = response.result
      const { list, records, total } = result
      hub.ruleList = list || records || []
      hub.total = total ?? result.total ?? hub.ruleList.length
    } else {
      hub.ruleList = []
      hub.total = 0
      ElMessage.error(response.message || '获取账号词库详情失败')
    }
  } catch (error: any) {
    hub.ruleList = []
    hub.total = 0
    ElMessage.error(error?.message || '获取账号词库详情失败，请稍后重试')
  } finally {
    selectedIds.value = []
    hub.loading = false
  }
}

// 弹窗相关状态
const dialogVisible = ref(false)
const dialogFormRef = ref<FormInstance>()
const dialogForm = reactive({
  id: '',
  accountName: '',
  accountId: '',
  channel: '',
  status: ''
})

const validateAccountNameOrId = (_rule: any, _value: string, callback: (error?: Error) => void) => {
  if (dialogForm.accountName || dialogForm.accountId) {
    callback()
    return
  }
  callback(new Error('账号名称或账号ID至少填写一项'))
}

const validateChannel = (_rule: any, value: string, callback: (error?: Error) => void) => {
  const val = value ? String(value) : ''
  if (!val) {
    callback(new Error('请选择所属渠道'))
    return
  }
  if (firstLevelChannelValues.value.includes(val)) {
    callback(new Error('不能选择一级渠道，请选择下级渠道'))
    return
  }
  callback()
}

const dialogRules = {
  accountName: [{ validator: validateAccountNameOrId, trigger: 'blur' }],
  accountId: [{ validator: validateAccountNameOrId, trigger: 'blur' }],
  channel: [{ required: true, validator: validateChannel, trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const getDefaultStatus = () => statusOptions.value[0]?.value || ''

watch(
  statusOptions,
  opts => {
    if (!opts.length) return
    if (!dialogForm.id && !opts.some(opt => opt.value === dialogForm.status)) {
      dialogForm.status = opts[0].value
    }
  },
  { immediate: true }
)

const resetDialogForm = () => {
  dialogForm.id = ''
  dialogForm.accountName = ''
  dialogForm.accountId = ''
  dialogForm.channel = ''
  dialogForm.status = getDefaultStatus()
}

const fillFormWithDetail = (detail: any) => {
  const detailId = detail?.id
  dialogForm.id =
    detailId !== undefined && detailId !== null && detailId !== '' ? String(detailId) : ''
  dialogForm.accountName = detail?.accountName || ''
  dialogForm.accountId = detail?.accountId || ''
  dialogForm.channel = detail?.channel || ''
  dialogForm.status =
    detail?.status && statusLabelMap.value[detail.status] ? detail.status : getDefaultStatus()
}

const openDialog = async (record?: any) => {
  if (!getCurrentResourceId()) {
    ElMessage.warning('请先选择账号分类')
    return
  }
  resetDialogForm()
  if (record?.id) {
    try {
      const response = (await findAccountLexiconInfo({ id: String(record.id) })) as any
      if ((response.success || response.code === '200') && response.result) {
        fillFormWithDetail(response.result)
      } else {
        ElMessage.error(response.message || '获取账号详情失败')
        return
      }
    } catch (error: any) {
      ElMessage.error(error?.message || '获取账号详情失败')
      return
    }
  }
  dialogVisible.value = true
  await nextTick()
  dialogFormRef.value?.clearValidate()
}

const submitDialog = async () => {
  if (!dialogFormRef.value) return
  const resourceId = getCurrentResourceId()
  if (!resourceId) {
    ElMessage.warning('请选择账号分类')
    return
  }
  try {
    await dialogFormRef.value.validate()
    const payload = {
      resourceId,
      accountName: dialogForm.accountName,
      accountId: dialogForm.accountId,
      channel: dialogForm.channel,
      status: dialogForm.status
    }
    let response: any
    if (dialogForm.id) {
      response = await updateAccountLexiconDetails({ ...payload, id: dialogForm.id })
    } else {
      response = await saveAccountLexiconDetails(payload)
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

const handleEdit = (row: any) => {
  openDialog(row)
}

const handleCreate = () => {
  openDialog()
}

const handleSelectionChange = (rows: any[]) => {
  selectedIds.value = rows
    .map(item => item?.id)
    .filter(id => id !== undefined && id !== null && id !== '')
    .map(id => String(id))
}

const handleBatchChange = async (status: string) => {
  if (!selectedIds.value.length) {
    ElMessage.warning('请先选择账号')
    return
  }
  const resourceId = getCurrentResourceId()
  if (!resourceId) {
    ElMessage.warning('请选择账号分类')
    return
  }
  try {
    const response = (await changeAccountLexiconStatus({
      ids: selectedIds.value,
      status,
      resourceId
    })) as any
    if (response.success || response.code === '200') {
      ElMessage.success('批量操作成功')
      selectedIds.value = []
      fetchList(false)
    } else {
      ElMessage.error(response.message || '批量操作失败')
    }
  } catch (error: any) {
    ElMessage.error(error?.message || '批量操作失败')
  }
}

const handleSizeChange = (val: number) => {
  hub.queryParams.pageSize = val
  fetchList()
}

const handleCurrentChange = (val: number) => {
  hub.queryParams.pageNum = val
  fetchList()
}

const leftChange = (leftItem: any) => {
  curLeftItem.value = leftItem
  hub.queryParams.pageNum = 1
  fetchList()
}

defineExpose({
  leftChange
})

const fetchChannelTree = async () => {
  try {
    const response = (await getAccountLexiconChannelTree({ status: '1' })) as any
    if ((response.success || response.code === '200') && response.result) {
      channelTree.value = Array.isArray(response.result) ? response.result : []
    } else {
      channelTree.value = []
      ElMessage.error(response.message || '获取渠道树失败')
    }
  } catch (error: any) {
    channelTree.value = []
    ElMessage.error(error?.message || '获取渠道树失败')
  }
}

onMounted(() => {
  fetchChannelTree()
})
</script>
<template>
  <div class="h-full flex-col">
    <div class="flex-between items-center mb-24">
      <div class="header-title-class">账号列表</div>
      <div>
        <el-cascader
          v-model="hub.queryParams.channel"
          :options="channelOptions"
          :props="channelCascaderProps"
          :show-all-levels="false"
          clearable
          style="width: 150px"
          placeholder="所属渠道"
          @change="handleChannelFilterChange"
        />
        <el-input
          v-model="hub.queryParams.keyword"
          style="width: 160px; margin-left: 12px"
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
          新建账号</el-button
        >
      </div>
    </div>
    <el-table
      v-loading="hub.loading"
      :data="hub.ruleList"
      class="flex-auto overflow-auto"
      row-key="id"
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
        <el-table-column
          v-else-if="column.prop === 'channel'"
          :prop="column.prop"
          :label="column.label"
          :width="column.width"
        >
          <template #default="{ row }">
            {{
              channelLabelMap[row.channel] ||
              row.channelName ||
              row.channelText ||
              row.channel ||
              '-'
            }}
          </template>
        </el-table-column>
        <el-table-column
          v-else-if="column.prop === 'createTime'"
          :prop="column.prop"
          :label="column.label"
          :width="column.width"
        >
          <template #default="{ row }">
            {{ row.createTime || row.updateTime || '-' }}
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
      :title="dialogForm.id ? '编辑账号' : '新建账号'"
      :confirm="submitDialog"
    >
      <el-form
        ref="dialogFormRef"
        :model="dialogForm"
        :rules="dialogRules"
        label-width="80px"
        class="keyword-dialog-form"
      >
        <el-form-item label="账号名称" prop="accountName">
          <el-input
            v-model.trim="dialogForm.accountName"
            placeholder="请输入账号名称"
            maxlength="64"
          />
        </el-form-item>
        <el-form-item label="账号ID" prop="accountId">
          <el-input v-model.trim="dialogForm.accountId" placeholder="请输入账号ID" maxlength="64" />
        </el-form-item>
        <el-form-item label="所属渠道" prop="channel">
          <el-cascader
            v-model="dialogForm.channel"
            :options="channelOptions"
            :props="channelCascaderProps"
            :show-all-levels="false"
            clearable
            placeholder="请选择所属渠道"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
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
}

.status-1,
.status-Enabled {
  background: #00b42a;
}

.status-0,
.status-Disabled {
  background: #c9cdd4;
}
</style>
