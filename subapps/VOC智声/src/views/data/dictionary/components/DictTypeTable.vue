<script setup lang="ts">
/**
 * 数据字典类型表格组件
 * 包含搜索、新增、编辑、删除功能
 */

defineOptions({
  name: 'DictTypeTable'
})

import { h, ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { dictApi } from '@/api/dictionary'
import { appDialogConfirm } from '@/components/appDialog'
import SvgIcon from '@/components/UI/SvgIcon/index.vue'
import DictFormDialog from './DictFormDialog.vue'
import type { DictListVo, DictQueryParams } from '@/api/dictionary/index.d'

// Emits
const emit = defineEmits<{
  dictSelect: [dict: DictListVo]
  dictUpdated: []
}>()

// 加载状态
const loading = ref(false)

// 表格数据
const tableData = ref<DictListVo[]>([])

// 选中的行
const selectedRow = ref<DictListVo | null>(null)

// 分页信息
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 搜索表单
const searchForm = reactive({
  dictName: '',
  dictCode: ''
})

// 表单弹窗状态
const dialogState = reactive({
  visible: false,
  mode: 'add' as 'add' | 'edit',
  editData: null as DictListVo | null
})

// 数据类型映射
const typeMap = {
  0: '字符串',
  1: '数字',
  2: '布尔值'
}

// 数据类型标签类型映射
const typeTagMap: any = {
  0: 'primary',
  1: 'success',
  2: 'warning'
}

// 查询字典列表
const fetchDictList = async () => {
  try {
    loading.value = true
    const params: DictQueryParams = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      dictName: searchForm.dictName || undefined,
      dictCode: searchForm.dictCode || undefined
    }

    const response = await dictApi.getDictList(params)
    if (response.success) {
      tableData.value = response.result.list
      pagination.total = response.result.total
    } else {
      ElMessage.error(response.message || '查询失败')
    }
  } catch (error) {
    console.error('查询字典列表失败:', error)
    ElMessage.error('查询失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  fetchDictList()
}

// 重置搜索
const handleReset = () => {
  searchForm.dictName = ''
  searchForm.dictCode = ''
  pagination.pageNum = 1
  fetchDictList()
}

// 分页改变
const handlePageChange = (page: number) => {
  pagination.pageNum = page
  fetchDictList()
}

const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  fetchDictList()
}

// 新增字典
const handleAdd = () => {
  dialogState.mode = 'add'
  dialogState.editData = null
  dialogState.visible = true
}

// 编辑字典
const handleEdit = (row: DictListVo) => {
  dialogState.mode = 'edit'
  dialogState.editData = row
  dialogState.visible = true
}

// 删除字典
const handleDelete = async (row: DictListVo) => {
  try {
    await appDialogConfirm(
      () =>
        h('div', { class: 'flex items-center' }, [
          h(SvgIcon, { name: 'info-circle-filled', width: '20px', height: '20px' }),
          h(
            'span',
            { class: 'ml-8' },
            `确定要删除字典"${row.dictName}"吗？删除后相关的字典项也会被删除。`
          )
        ]),
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )

    const response = await dictApi.deleteDict(row.id)
    if (response.success) {
      ElMessage.success('删除成功')
      // 如果删除的是当前选中行，清除选中状态
      if (selectedRow.value?.id === row.id) {
        selectedRow.value = null
        emit('dictUpdated')
      }
      fetchDictList()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      console.error('删除字典失败:', error)
      ElMessage.error('删除失败，请稍后重试')
    }
  }
}

// 表格行点击
const handleRowClick = (row: DictListVo) => {
  selectedRow.value = row
  emit('dictSelect', row)
}

// 弹窗确认
const handleDialogConfirm = () => {
  dialogState.visible = false
  fetchDictList()
}

// 获取表格行样式类名
const getRowClassName = ({ row }: { row: DictListVo }) => {
  return selectedRow.value?.id === row.id ? 'selected-row' : ''
}

// 初始化
onMounted(() => {
  fetchDictList()
})
</script>

<template>
  <div class="dict-type-table">
    <!-- 搜索区域 -->
    <div class="search-section">
      <el-form :model="searchForm" inline>
        <el-form-item label="字典名称">
          <el-input
            v-model="searchForm.dictName"
            placeholder="请输入字典名称"
            clearable
            style="width: 180px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="字典编号">
          <el-input
            v-model="searchForm.dictCode"
            placeholder="请输入字典编号"
            clearable
            style="width: 180px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 操作区域 -->
    <div class="action-section">
      <el-button type="primary" @click="handleAdd">
        <template #icon>
          <el-icon><Plus /></el-icon>
        </template>
        添加
      </el-button>
    </div>

    <!-- 表格区域 -->
    <div class="table-section">
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        highlight-current-row
        :row-class-name="getRowClassName"
      >
        <el-table-column type="index" label="#" width="50" align="center" />

        <el-table-column prop="dictName" label="字典名称" min-width="150">
          <template #default="{ row }">
            <span class="clickable-text" @click="handleRowClick(row)">
              {{ row.dictName }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="dictCode" label="字典编号" min-width="150" />

        <el-table-column prop="type" label="字典类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="typeTagMap[row.type as keyof typeof typeTagMap]" size="small">
              {{ typeMap[row.type as keyof typeof typeMap] }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
          prop="description"
          label="描述"
          min-width="200"
          :show-overflow-tooltip="{ popperClass: 'text-tooltip-light' }"
        />

        <el-table-column prop="itemCount" label="字典项数量" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.itemCount || 0 }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="operator" label="创建人" width="120" align="center" />

        <el-table-column prop="createTime" label="创建时间" width="180" align="center">
          <template #default="{ row }">
            {{ row.createTime ? new Date(row.createTime).toLocaleString() : '' }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click.stop="handleEdit(row)">
              编辑
            </el-button>
            <el-button link type="primary" size="small" @click.stop="handleRowClick(row)">
              查看
            </el-button>
            <el-button link type="danger" size="small" @click.stop="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页区域 -->
    <div class="pagination-section">
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 表单弹窗 -->
    <DictFormDialog
      v-model:visible="dialogState.visible"
      :mode="dialogState.mode"
      :edit-data="dialogState.editData"
      @confirm="handleDialogConfirm"
    />
  </div>
</template>

<style lang="scss" scoped>
.dict-type-table {
  height: 100%;
  display: flex;
  flex-direction: column;

  .search-section {
    margin-bottom: 16px;
    padding: 16px;
    background: #f9f9f9;
    border-radius: 6px;
  }

  .action-section {
    margin-bottom: 16px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .table-section {
    flex: 1;
    min-height: 0;

    .clickable-text {
      color: #409eff;
      cursor: pointer;

      &:hover {
        text-decoration: underline;
      }
    }

    :deep(.selected-row) {
      background-color: rgba(#409eff, 0.1) !important;
    }

    :deep(.el-table__row) {
      cursor: pointer;

      &:hover {
        background-color: #f9f9f9 !important;
      }
    }
  }

  .pagination-section {
    margin-top: 16px;
    display: flex;
    justify-content: center;
  }
}
</style>
