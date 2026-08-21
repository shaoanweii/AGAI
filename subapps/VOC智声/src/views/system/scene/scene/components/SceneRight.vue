<script setup lang="ts">
import { h, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import ZoneDialog from '@views/system/scene/scene/components/ZoneDialog.vue'
import SortDialog from '@views/system/scene/scene/components/SortDialog.vue'
import SvgIcon from '@/components/UI/SvgIcon/index.vue'
import { appDialogConfirm } from '@/components/appDialog'
import { deleteSpecialType, getSpecialTypeList } from '@/api/system/scene'

const hub = reactive({
  loading: false,
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    keyword: '' // 关键字
  },
  total: 0,
  zoneList: [] as any[],
  visible: false,
  zoneData: null as any,
  sortVisible: false
})

// 列定义
const columns = [
  {
    label: '专区名称',
    prop: 'name'
  },
  {
    label: '场景数量',
    prop: 'reportCnt',
    width: '200px'
  },
  {
    label: '关联角色',
    prop: 'roleCnt',
    width: '200px'
  },
  {
    label: '当前状态',
    prop: 'enabled'
  }
]

const keywordSearch = () => {
  // 这里后续可以加防抖，避免频繁请求
  fetchList()
}

const curLeftItem = ref<any>()

const fetchList = async () => {
  const pid = curLeftItem.value?.id
  if (!pid) {
    hub.zoneList = []
    hub.total = 0
    return
  }

  try {
    hub.loading = true
    hub.zoneList = []
    const params: any = {
      pageNum: hub.queryParams.pageNum,
      pageSize: hub.queryParams.pageSize,
      pid,
      type: 2,
      name: hub.queryParams.keyword
    }

    const response = await getSpecialTypeList(params)
    if (response.success && response.result) {
      hub.total = response.result.total || 0
      hub.zoneList =
        response.result.list?.map((item: any) => ({
          ...item,
          isActive: false,
          isDisable: false
        })) || []
    } else {
      ElMessage.error(response.message || '获取专区列表失败')
      hub.zoneList = []
      hub.total = 0
    }
  } catch (error: any) {
    console.error('获取专区列表失败:', error)
    ElMessage.error('获取专区列表失败，请稍后重试')
    hub.zoneList = []
    hub.total = 0
  } finally {
    hub.loading = false
  }
}

const canSort = () => {
  if (hub.loading) return false
  if (!curLeftItem.value?.id) return false
  return hub.zoneList.length > 0
}

// 打开专区排序弹窗
const openSortDialog = () => {
  if (!curLeftItem.value?.id) {
    ElMessage.warning('请先选择左侧分类')
    return
  }
  if (!canSort()) return
  hub.sortVisible = true
}

// 拉取全部专区（用于排序弹窗）
// 排序弹窗内部会按 total 拉全量 + 调用保存接口，这里只负责提供 pid 与刷新

// 编辑
const handleEdit = (row: any) => {
  hub.zoneData = row
  hub.visible = true
}

// 刷新列表
const refreshList = () => {
  fetchList()
}

// 分页
const handleSizeChange = (val: number) => {
  hub.queryParams.pageSize = val
  hub.queryParams.pageNum = 1
  fetchList()
}

// 分页
const handleCurrentChange = (val: number) => {
  hub.queryParams.pageNum = val
  fetchList()
}

// 删除专区
const handleDelete = async (item: any) => {
  try {
    await appDialogConfirm(
      () =>
        h('div', { class: 'flex items-center' }, [
          h(SvgIcon, { name: 'info-circle-filled', width: '20px', height: '20px' }),
          h('span', { class: 'ml-8' }, `确定要删除专区 "${item.name}" 吗？`)
        ]),
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    const response = await deleteSpecialType({ id: item.id })
    if (response.success) {
      ElMessage.success('删除专区成功')
      fetchList()
    } else {
      ElMessage.error(response.message || '删除专区失败')
    }
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    console.error('删除专区失败:', error)
  }
}

const leftChange = (leftItem: any) => {
  curLeftItem.value = leftItem
  hub.queryParams.pageNum = 1
  fetchList()
}

defineExpose({
  leftChange
})
</script>

<template>
  <div class="pl-24 h-full flex-col">
    <div class="flex-between items-center mb-24">
      <div class="text-h3" style="font-weight: 600">专区列表</div>
      <div>
        <el-input
          v-model="hub.queryParams.keyword"
          style="width: 172px"
          clearable
          placeholder="请输入关键词搜索"
          @change="keywordSearch"
        />
        <el-button class="ml-16 sort-btn" :disabled="!canSort()" @click="openSortDialog">
          <SvgIcon name="direction_swap" width="16px" height="16px" color="currentColor" class="mr-6" />
          专区排序
        </el-button>
        <el-button class="ml-16" type="primary" @click="handleEdit(null)">新建专区</el-button>
      </div>
    </div>
    <el-table
      v-loading="hub.loading"
      :data="hub.zoneList"
      max-height="calc(100vh - 260px)"
      class="flex-auto overflow-auto"
    >
      <el-table-column type="index" label="#" width="56" align="center" />
      <template v-for="(column, index) in columns" :key="index">
        <el-table-column v-if="column.prop === 'enabled'" :prop="column.prop" :label="column.label" :width="column.width">
          <template #default="{ row }">
            {{ row.enabled === 1 ? '已启用' : '已禁用' }}
          </template>
        </el-table-column>
        <el-table-column v-else :prop="column.prop" :label="column.label" :width="column.width" />
      </template>

      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <div class="flex-y-center">
            <el-button link type="primary" @click="handleEdit(row)"> 编辑 </el-button>
            <el-button link type="primary" @click="handleDelete(row)"> 删除 </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="hub.queryParams.pageNum"
      v-model:page-size="hub.queryParams.pageSize"
      :total="hub.total"
      :page-sizes="[10, 20, 50, 100]"
      layout="->,total, prev, pager, next, sizes"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
    <zone-dialog v-model:visible="hub.visible" :zone-data="hub.zoneData" :curLeftItem="curLeftItem" @success="refreshList()" />
    <SortDialog
      v-model:visible="hub.sortVisible"
      title="专区排序"
      :type="2"
      :total="hub.total"
      :pid="curLeftItem?.id"
      @success="refreshList()"
    />
  </div>
</template>

<style lang="scss" scoped>
.sort-btn {
  --el-button-text-color: #1d2129;
}

:deep(.el-table .el-table__cell) {
  height: 55px;
  padding: 0 !important;
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

:deep(.el-table--fit .el-table__inner-wrapper:before) {
  width: 0 !important;
}
</style>
