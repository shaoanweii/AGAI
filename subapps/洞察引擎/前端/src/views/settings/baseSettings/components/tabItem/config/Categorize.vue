<script setup lang="ts">
import { Plus } from '@element-plus/icons-vue'
import Form from './Form.vue'
import { deleteRegionCategory, findRegionCategoryList } from '@/api/baseSettings'
import type { Table, CategorizeItem } from '@/types/baseSeting.types'
import { useModal } from '@/hooks/useModal'
import type { ViewStatus } from '@/types'
import { ElMessage } from 'element-plus'
import CategorizeTree from './CategorizeTree.vue'
import useUserStore from '@/stores/modules/user'
import { computedCardHeight, listHeight } from '@/utils'

const emit = defineEmits(['tapCategorizeChange', 'getCategorize'])
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
      findRegionCategoryTreeByClientId()
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
//       findRegionCategoryTreeByClientId()
//     }
//   },
// })

const pageInfo = ref({
  total: 0,
  pageSize: 20,
  current: 1
})
// 处理分类数据
const transitionCategory = (data: CategorizeItem[], level = 0): CategorizeItem[] => {
  if (!data || !Array.isArray(data)) return data
  return data.map(item => {
    item.level = level
    let showChild
    // if (curCategorize.value && Object.keys(curCategorize.value).length > 0) {
    //   showChild = item.level <= (curCategorize.value?.level || 0)
    // } else {
    //   // 默认展开一级目录
    //   showChild = level === 0
    // }

    showChild = level === 0

    item.showChild = showChild

    item.child = transitionCategory(item.child!, level + 1)
    return item
  })
}
// 获取区域分类
const findRegionCategoryTreeByClientId = async () => {
  try {
    const responseObj = await findRegionCategoryList({
      clientId: userStore.clientId!,
      pageNum: pageInfo.value.current,
      pageSize: pageInfo.value.pageSize
    }).then(res => res.result)
    const response = responseObj.list
    pageInfo.value.total = responseObj.total
    pageInfo.value.current = responseObj.pageNum
    pageInfo.value.pageSize = responseObj.pageSize
    categorizeTable.value.list = transitionCategory(response)
    if (!(curCategorize.value && Object.keys(curCategorize.value).length > 0)) {
      curCategorize.value = response[0]
    }
    userStore.clientId && sessionStorage.setItem(userStore.clientId, '')
    emit('getCategorize', response)
  } catch (err: any) {
    categorizeTable.value.list = []
    curCategorize.value = {}
    pageInfo.value.total = 0
    pageInfo.value.current = 1
    pageInfo.value.pageSize = 20
    ElMessage.error(err.message)
  }
}

onMounted(() => {
  findRegionCategoryTreeByClientId()
})

const handleCategorizeChange = (item: CategorizeItem) => {
  curCategorize.value = item
}

const add = () => {
  viewStatus.value = 'add'
  editRecord.value = curCategorize.value
  showVisible()
}
const handleEdit = (record: any) => {
  editRecord.value = record
  viewStatus.value = 'edit'
  showVisible()
}
const handleDelete = (record: any) => {
  deleteRegionCategory({
    id: record.id,
    clientId: userStore.clientId
  })
    .then(res => {
      if (res.code === '200') {
        record.id === curCategorize.value.id && (curCategorize.value = {})
        findRegionCategoryTreeByClientId()
      }
    })
    .catch(err => {
      ElMessage.error(err.message)
    })
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
      <!-- <el-button
        v-auth="`settings-regionConfig-add`"
        :data-testid="`baseSetting-region-left-10001`"
        type="primary"
        @click="add"
      >
        <template #icon>
          <icon-plus />
        </template>
        新增分类
      </el-button> -->
    </div>
    <div v-loading="categorizeTable.loading" style="width: 100%">
      <div class="main-list list-wrapper">
        <div class="list-header">
          <span style="font-weight: bold; line-height: 22px">全部分类</span>
        </div>
        {{ categorizeTable.list.length }}
        <!--<div v-if="categorizeTable.list.length" :style="computedCardHeight(254)" style="overflow: auto">-->
        <div
          v-if="categorizeTable.list.length"
          :style="{ 'max-height': `${listHeight(254)}px`, overflow: 'auto' }"
        >
          <CategorizeTree
            :tree-data="categorizeTable.list"
            :cur-categorize="curCategorize"
            @handle-categorize-change="handleCategorizeChange"
            @handle-edit="handleEdit"
            @handle-delete="handleDelete"
          ></CategorizeTree>
        </div>
        <!-- <div style="width: 50%;"
          v-if="useAppStore().showPaginationMinLength && pageInfo.total >= useAppStore().showPaginationMinLength">
          <el-pagination
           :total="pageInfo.total"
           :page-size="pageInfo.pageSize"
           :current="pageInfo.current"
           show-total show-page-size/>
        </div> -->
      </div>
    </div>

    <Form
      v-model="visible"
      :type="1"
      :categorizeTree="categorizeTable.list"
      :viewStatus="viewStatus"
      :editRecord="editRecord"
      @refreshList="findRegionCategoryTreeByClientId"
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
</style>
