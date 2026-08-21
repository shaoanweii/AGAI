<script setup lang="ts">
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import Form from './Form.vue'
import { deleteChannel, findChannelCategoryTree } from '@/api/baseSettings'
import type { Table, CategorizeItem } from '@/types/baseSeting.types'
import { useModal } from '@/hooks/useModal'
import type { ViewStatus } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'
import useUserStore from '@/stores/modules/user'
import { computedCardHeight } from '@/utils'

const emit = defineEmits(['tapCategorizeChange', 'getCategorize'])
const { visible, showVisible } = useModal()
const categorizeTable = ref<Table>({
  loading: false,
  list: []
})
const curCategorize = ref<CategorizeItem>({})
const viewStatus = ref<ViewStatus>('add')
const editRecord = ref()
const currentRow = ref<CategorizeItem>()
// 允许新增分类的最大层级
// const LEVEL_MAX = 5

watch(
  curCategorize,
  (nval: CategorizeItem | undefined) => {
    emit('tapCategorizeChange', nval)
  },
  {
    deep: true
  }
)
const userStore = useUserStore()

watch(
  () => userStore.clientId,
  () => {
    curCategorize.value = {}
    if (userStore?.clientId) {
      findChannelCategoryTreeByClientId()
    }
  },
  {
    deep: true
  }
)

// useEmitt({
//   name: emittName.clientChange,
//   callback: () => {
//     curCategorize.value = {}
//     if (userStore?.clientId) {
//       findChannelCategoryTreeByClientId()
//     }
//   },
// })

// 处理分类数据 - 转换为树形表格格式
const transitionCategory = (data: CategorizeItem[], level = 0): CategorizeItem[] => {
  if (!data || !Array.isArray(data)) return data
  return data.map(item => {
    item.level = level
    // 为 Element Plus 树形表格设置必要属性
    if (item.child && item.child.length > 0) {
      item.children = transitionCategory(item.child, level + 1)
      item.hasChildren = true
    } else {
      item.children = []
      item.hasChildren = false
    }
    return item
  })
}

// 行选择处理
const handleRowClick = (row: CategorizeItem) => {
  currentRow.value = row
  curCategorize.value = row
}
// 获取渠道分类
const findChannelCategoryTreeByClientId = async () => {
  try {
    const response = await findChannelCategoryTree({ clientId: userStore.clientId! }).then(
      res => res.result
    )
    categorizeTable.value.list = transitionCategory(response)
    if (!(curCategorize.value && Object.keys(curCategorize.value).length > 0)) {
      curCategorize.value = response[0]
    }
    userStore.clientId && sessionStorage.setItem(userStore.clientId, '')
    emit('getCategorize', response)
  } catch (err: any) {
    categorizeTable.value.list = []
    curCategorize.value = {}
    ElMessage.error(err.message)
  }
}

onMounted(() => {
  findChannelCategoryTreeByClientId()
})

const add = () => {
  viewStatus.value = 'add'
  editRecord.value = curCategorize.value
  showVisible()
}
const handleEdit = (record: CategorizeItem) => {
  editRecord.value = record
  viewStatus.value = 'edit'
  showVisible()
}

const handleDelete = async (record: CategorizeItem) => {
  try {
    await ElMessageBox.confirm(`确定要删除分类"${record.name}"吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await deleteChannel({
      id: record.id,
      clientId: userStore.clientId
    })

    if (res.code === '200') {
      ElMessage.success('删除成功')
      if (record.id === curCategorize.value.id) {
        curCategorize.value = {}
        currentRow.value = undefined
      }
      findChannelCategoryTreeByClientId()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// const addDisabled = computed(() => {
//   // 分类只有默认分类的时候允许点击新增分类按钮
//   if (categorizeTable.value.list?.length === 1 && curCategorize.value.id === '-1') {
//     return false
//   }
//   // 超过允许新增分类的最大层级不能再继续添加分类
//   return curCategorize.value?.level! >= LEVEL_MAX || curCategorize.value.id === '-1'
// })
</script>

<template>
  <div class="table-wrapper" :style="computedCardHeight(106)">
    <div class="title">
      <h3>分类列表</h3>
      <el-button
        v-auth="`settings-channelConfig-add`"
        :data-testid="`baseSetting-channel-left-10001`"
        type="primary"
        @click="add"
      >
        <template #icon>
          <Plus />
        </template>
        新增分类
      </el-button>
    </div>

    <div v-loading="categorizeTable.loading" class="main-list">
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
        <el-table-column prop="name" label="分类名称" min-width="200">
          <template #default="{ row }">
            <span :data-testid="`baseSetting-channel-left-10002-${row.id}`">
              {{ row.name }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="" width="100" align="center">
          <template #default="{ row }">
            <div
              class="action-buttons"
              v-if="row.id !== '-1'"
              :class="{ 'show-actions': currentRow?.id === row.id }"
            >
              <el-button
                v-auth="`settings-channelConfig-edit`"
                :data-testid="`baseSetting-channel-left-10003-${row.id}`"
                type="text"
                :icon="Edit"
                :disabled="row.name === '未确认渠道'"
                @click.stop="handleEdit(row)"
                title="编辑"
              />
              <el-button
                v-auth="`settings-channelConfig-delete`"
                :data-testid="`baseSetting-channel-left-10004-${row.id}`"
                type="text"
                :icon="Delete"
                :disabled="row.name === '未确认渠道'"
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
      @refreshList="findChannelCategoryTreeByClientId"
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
