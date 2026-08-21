<script setup lang="ts">
import { Edit, Delete } from '@element-plus/icons-vue'
import Form from './Form.vue'
import type { Table, CategorizeItem } from '@/types/baseSeting.types'
import { useModal } from '@/hooks/useModal'
import type { ViewStatus } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'
import useUserStore from '@/stores/modules/user'
import { computedCardHeight } from '@/utils'
import { deleteTagLibClient, findClientCategoryTree } from '@/api/tag'
import { cloneDeep } from 'lodash-es'
import { Plus } from '@element-plus/icons-vue'

const { filter, setLoading } = defineProps<{
  filter: any
  setLoading: (val: boolean) => void
}>()

const emit = defineEmits(['CategoryChange'])
const { visible, showVisible } = useModal()
const categorizeTable = ref<Table>({
  loading: false,
  list: []
})
const curCategorize = ref<CategorizeItem>({})
const viewStatus = ref<ViewStatus>('add')
const editRecord = ref()
// 允许新增分类的最大层级
// const LEVEL_MAX = 5

watch(
  curCategorize,
  (nval: CategorizeItem | undefined) => {
    const newNval = cloneDeep(nval)
    if (newNval?.tagParentId === '-1') {
      newNval.tagType = categorizeTable.value.list?.[0]?.child?.[0]?.tagType || filter?.tagType
    }
    emit('CategoryChange', newNval)
  },
  {
    deep: true
  }
)
const userStore = useUserStore()

const params = computed(() => {
  return {
    appClient: userStore.clientId,
    tagType: filter.tagType
    // tagName: filter.tagName || undefined
  }
})

// 处理分类数据 - 转换为树形表格数据格式
const transitionCategory = (data: CategorizeItem[]): CategorizeItem[] => {
  if (!data || !Array.isArray(data)) return data
  return data.map(item => {
    // 为树形表格添加必要的属性
    const hasChildren = item.child && item.child.length > 0
    const newItem: CategorizeItem = {
      ...item,
      children: hasChildren ? transitionCategory(item.child!) : undefined,
      hasChildren: hasChildren
    }
    return newItem
  })
}
// 获取标签分类
const findTagLibClientCategoryTreeByClientId = async (flag?: number) => {
  try {
    setLoading(true)
    const response = await findClientCategoryTree(params.value).then(res => res.result as any)
    categorizeTable.value.list = transitionCategory(response)
    console.log('categorizeTable.value.list', categorizeTable.value.list)

    if (!(curCategorize.value && Object.keys(curCategorize.value).length > 0)) {
      curCategorize.value = response[0]
      currentRow.value = response[0]
    }
    if (flag === 1) {
      curCategorize.value = response[0]
      currentRow.value = response[0]
      if (curCategorize.value?.tagType && curCategorize.value?.tagType !== filter.tagType) {
        curCategorize.value = response[0]
        currentRow.value = response[0]
      }
      // emit('CategoryChange', curCategorize.value)
    }

    setLoading(false)
  } catch (err: any) {
    categorizeTable.value.list = []
    curCategorize.value = {}
    ElMessage.error(err.message)
    setLoading(false)
  }
}

// onMounted(() => {
//   findTagLibClientCategoryTreeByClientId()
// })

const query = async (flag?: number) => {
  await findTagLibClientCategoryTreeByClientId(flag)
  return curCategorize.value
}

// 当前选中的行
const currentRow = ref<CategorizeItem>()

// 行点击事件
const handleRowClick = (row: CategorizeItem) => {
  currentRow.value = row
  curCategorize.value = row
}

// 新增分类
const add = () => {
  viewStatus.value = 'add'
  editRecord.value = curCategorize.value
  showVisible()
}

// 编辑分类
const handleEdit = (record: CategorizeItem) => {
  editRecord.value = record
  viewStatus.value = 'edit'
  showVisible()
}

// 删除分类
const handleDelete = async (record: CategorizeItem) => {
  try {
    await ElMessageBox.confirm(`确定要删除分类"${record.tagName}"吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await deleteTagLibClient({
      id: record.id!,
      appClient: userStore.clientId
    })

    if (res.code === '200') {
      ElMessage.success('删除成功')
      // 如果删除的是当前选中项，清空选中状态
      if (record.id === curCategorize.value.id) {
        curCategorize.value = {}
        currentRow.value = undefined
      }
      findTagLibClientCategoryTreeByClientId()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const refreshList = () => {
  findTagLibClientCategoryTreeByClientId()
  // emit('CategoryChange', curCategorize.value)
}

// const addDisabled = computed(() => {
//   // 分类只有默认分类的时候允许点击新增分类按钮
//   if (categorizeTable.value.list?.length === 1 && curCategorize.value.id === '-1') {
//     return false
//   }
//   // 超过允许新增分类的最大层级不能再继续添加分类
//   return curCategorize.value?.level! >= LEVEL_MAX || curCategorize.value.id === '-1'
// })

defineExpose({ query })
</script>

<template>
  <div
    class="table-wrapper cm-card"
    style="margin-right: 24px; width: 380px"
    :style="computedCardHeight(182)"
  >
    <div class="title">
      <h3>分类列表</h3>
      <el-button
        v-auth="`tagManagement-application-add`"
        :data-testid="`baseSetting-channel-left-10001`"
        type="primary"
        :icon="Plus"
        @click="add"
      >
        新增分类
      </el-button>
    </div>

    <div class="main-list">
      <el-table
        :data="categorizeTable.list"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :highlight-current-row="true"
        :current-row="currentRow"
        @current-change="handleRowClick"
        style="width: 100%"
        :height="'100%'"
        class="category-tree-table"
      >
        <el-table-column prop="tagName" label="分类名称" min-width="200">
          <template #default="{ row }">
            <span>{{ row.tagName }}</span>
          </template>
        </el-table-column>

        <el-table-column label="" width="100" align="center">
          <template #default="{ row }">
            <div
              class="action-buttons"
              v-if="row.tagParentId !== '-1'"
              :class="{ 'show-actions': currentRow?.id === row.id }"
            >
              <el-button
                v-auth="'tagManagement-application-edit'"
                type="text"
                :icon="Edit"
                @click.stop="handleEdit(row)"
                title="编辑"
              />
              <el-button
                v-auth="'tagManagement-application-del'"
                type="text"
                :icon="Delete"
                @click.stop="handleDelete(row)"
                title="删除"
                class="delete-btn"
              />
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <Form
      v-model="visible"
      :type="1"
      :categorizeTree="categorizeTable.list"
      :viewStatus="viewStatus"
      :editRecord="editRecord"
      :filter="filter"
      @refreshList="refreshList"
    ></Form>
  </div>
</template>

<style scoped lang="scss">
.title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  h3 {
    font-weight: 700;
    color: var(--color-high);
  }
}

.main-list {
  flex: 1;
  overflow: hidden;
  height: 95%;
}

.category-tree-table {
  // :deep(.el-table__body-wrapper) {
  //   max-height: 400px;
  //   overflow-y: auto;
  // }

  :deep(.el-table__row) {
    cursor: pointer;

    &:hover {
      background-color: var(--el-table-row-hover-bg-color);

      // hover时显示操作按钮
      .action-buttons {
        opacity: 1;
      }
    }
  }

  :deep(.current-row) {
    background-color: var(--el-color-primary-light-9);

    td {
      background-color: var(--el-color-primary-light-9) !important;
    }
  }
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.2s ease-in-out;

  .el-button {
    padding: 4px;
    margin: 0;

    &.delete-btn {
      color: var(--el-color-danger);

      &:hover {
        color: var(--el-color-danger-light-3);
      }
    }
  }

  // 选中行时显示操作按钮
  &.show-actions {
    opacity: 1;
  }
}
</style>
