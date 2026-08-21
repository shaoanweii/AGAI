<script setup lang="ts">
/**
 * 数据字典项面板组件
 * 显示选中字典的所有字典项
 */

defineOptions({
  name: 'DictItemPanel'
})

import { h, ref, reactive, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { dictItemApi } from '@/api/dictionary'
import { appDialogConfirm } from '@/components/appDialog'
import SvgIcon from '@/components/UI/SvgIcon/index.vue'
import DictItemFormDialog from './DictItemFormDialog.vue'
import type { DictListVo, DictItemListVo, DictItemQueryParams } from '@/api/dictionary/index.d'

// Props
interface Props {
  dictInfo: DictListVo
}

const props = defineProps<Props>()

// 加载状态
const loading = ref(false)

// 字典项数据
const itemList = ref<DictItemListVo[]>([])

// 分页信息
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 搜索表单
const searchForm = reactive({
  itemText: '',
  status: undefined as number | undefined
})

// 表单弹窗状态
const dialogState = reactive({
  visible: false,
  mode: 'add' as 'add' | 'edit',
  editData: null as DictItemListVo | null
})

// 状态选项
const statusOptions = [
  { label: '全部', value: '' },
  { label: '启用', value: 1 },
  { label: '停用', value: 0 }
]

// 查询字典项列表
const fetchDictItemList = async () => {
  if (!props.dictInfo?.id) return

  try {
    loading.value = true
    const params: DictItemQueryParams = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      dictId: props.dictInfo.id,
      itemText: searchForm.itemText || undefined,
      status: searchForm.status
    }

    const response = await dictItemApi.getDictItemList(params)
    if (response.success) {
      itemList.value = response.result.list
      pagination.total = response.result.total
    } else {
      ElMessage.error(response.message || '查询失败')
    }
  } catch (error) {
    console.error('查询字典项列表失败:', error)
    ElMessage.error('查询失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  fetchDictItemList()
}

// 重置搜索
const handleReset = () => {
  searchForm.itemText = ''
  searchForm.status = undefined
  pagination.pageNum = 1
  fetchDictItemList()
}

// 分页改变
const handlePageChange = (page: number) => {
  pagination.pageNum = page
  fetchDictItemList()
}

const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  fetchDictItemList()
}

// 新增字典项
const handleAdd = () => {
  dialogState.mode = 'add'
  dialogState.editData = null
  dialogState.visible = true
}

// 编辑字典项
const handleEdit = (row: DictItemListVo) => {
  dialogState.mode = 'edit'
  dialogState.editData = row
  dialogState.visible = true
}

// 删除字典项
const handleDelete = async (row: DictItemListVo) => {
  try {
    await appDialogConfirm(
      () =>
        h('div', { class: 'flex items-center' }, [
          h(SvgIcon, { name: 'info-circle-filled', width: '20px', height: '20px' }),
          h('span', { class: 'ml-8' }, `确定要删除字典项"${row.itemText}"吗？`)
        ]),
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )

    const response = await dictItemApi.deleteDictItem(row.id)
    if (response.success) {
      ElMessage.success('删除成功')
      fetchDictItemList()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      console.error('删除字典项失败:', error)
      ElMessage.error('删除失败，请稍后重试')
    }
  }
}

// 弹窗确认
const handleDialogConfirm = () => {
  dialogState.visible = false
  fetchDictItemList()
}

// 监听字典信息变化
watch(
  () => props.dictInfo,
  newDict => {
    if (newDict?.id) {
      // 重置搜索条件和分页
      searchForm.itemText = ''
      searchForm.status = undefined
      pagination.pageNum = 1
      fetchDictItemList()
    }
  },
  { immediate: true }
)
</script>

<template>
  <div class="dict-item-panel">
    <!-- 字典信息 -->
    <div class="dict-info">
      <h4 class="dict-title">{{ dictInfo.dictName }}</h4>
      <p class="dict-desc">{{ dictInfo.description || '暂无描述' }}</p>
      <div class="dict-meta">
        <el-tag size="small" type="primary">{{ dictInfo.dictCode }}</el-tag>
        <el-tag size="small" type="info">共 {{ pagination.total }} 项</el-tag>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="search-section">
      <el-form :model="searchForm" inline size="small">
        <el-form-item label="名称">
          <el-input
            v-model="searchForm.itemText"
            placeholder="请输入名称"
            clearable
            style="width: 140px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 100px"
          >
            <el-option
              v-for="option in statusOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="small" @click="handleSearch">查询</el-button>
          <el-button size="small" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 操作区域 -->
    <div class="action-section">
      <el-button type="primary" size="small" @click="handleAdd">
        <template #icon>
          <el-icon><Plus /></el-icon>
        </template>
        添加
      </el-button>
    </div>

    <!-- 表格区域 -->
    <div class="table-section">
      <el-table v-loading="loading" :data="itemList" stripe size="small" height="100%">
        <el-table-column type="index" label="#" width="40" align="center" />

        <el-table-column
          prop="itemText"
          label="名称"
          min-width="100"
          :show-overflow-tooltip="{ popperClass: 'text-tooltip-light' }"
        />

        <el-table-column
          prop="itemValue"
          label="数据值"
          min-width="80"
          :show-overflow-tooltip="{ popperClass: 'text-tooltip-light' }"
        />

        <el-table-column
          prop="description"
          label="描述"
          min-width="120"
          :show-overflow-tooltip="{ popperClass: 'text-tooltip-light' }"
        />

        <el-table-column prop="sortOrder" label="排序值" width="70" align="center" />

        <el-table-column prop="status" label="状态" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="handleEdit(row)"> 编辑 </el-button>
            <el-button text type="danger" size="small" @click="handleDelete(row)"> 删除 </el-button>
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
        :page-sizes="[10, 20, 50]"
        layout="total, prev, pager, next"
        small
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 表单弹窗 -->
    <DictItemFormDialog
      v-model:visible="dialogState.visible"
      :mode="dialogState.mode"
      :dict-info="dictInfo"
      :edit-data="dialogState.editData"
      @confirm="handleDialogConfirm"
    />
  </div>
</template>

<style lang="scss" scoped>
.dict-item-panel {
  height: 100%;
  display: flex;
  flex-direction: column;

  .dict-info {
    margin-bottom: 16px;
    padding: 16px;
    background: #f9f9f9;
    border-radius: 6px;

    .dict-title {
      margin: 0 0 4px 0;
      font-size: 14px;
      font-weight: 600;
      color: #303133;
    }

    .dict-desc {
      margin: 0 0 4px 0;
      font-size: 13px;
      color: #909399;
    }

    .dict-meta {
      display: flex;
      gap: 4px;
    }
  }

  .search-section {
    margin-bottom: 8px;
    padding: 8px;
    background: #f9f9f9;
    border-radius: 6px;
  }

  .action-section {
    margin-bottom: 8px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .table-section {
    flex: 1;
    min-height: 0;
    border: 1px solid #dcdfe6;
    border-radius: 6px;
  }

  .pagination-section {
    margin-top: 8px;
    display: flex;
    justify-content: center;
  }
}
</style>
