<script lang="ts" setup>
import { useTable } from '@/hooks/table'
import { Plus } from '@element-plus/icons-vue'
import Form from './Form.vue'
import type { CategorizeItem, Province } from '@/types/baseSeting.types'
import type { ViewStatus } from '@/types'
import { useModal } from '@/hooks/useModal'
import useUserStore from '@/stores/modules/user'
import { computedCardHeight } from '@/utils'
import { useAppStore } from '@/stores'

const props = withDefaults(
  defineProps<{
    curCategorize: CategorizeItem | undefined
    categorizeTree: any
  }>(),
  {}
)

const { curCategorize } = toRefs(props)

const { visible, showVisible } = useModal()

const viewStatus = ref<ViewStatus>('add')
const editRecord = ref()

const {
  table,
  getFirstPageTableData,
  handleSizeChange,
  handleCurrentChange,
  handleView,
  handleSortChange,
  handleAdd
} = useTable({
  method: 'POST',
  url: '/insights/region/findRegionList'
})
const userStore = useUserStore()
watchEffect(() => {
  table.filter.clientId = userStore.clientId
  table.filter.parentId = curCategorize.value?.id
})

const getList = () => {
  nextTick(() => {
    getFirstPageTableData()
  })
}
const resetList = () => {
  table.list = []
}

const add = () => {
  viewStatus.value = 'add'
  editRecord.value = curCategorize.value
  showVisible()
  handleAdd()
}
const edit = (record: any) => {
  console.log(record, 'edit')
  viewStatus.value = 'edit'
  editRecord.value = record
  showVisible()
  handleView(record)
}

const formatterProvinceText = (regionArr: Province[]): string => {
  return regionArr
    .reduce<string[]>((pre, cur) => {
      return [...pre, ...cur.areas.map(item => `${cur.provinceName} | ${item.areaName}`)]
    }, [])
    .join(',')
}

defineExpose({ getList, resetList })
</script>

<template>
  <div class="table-wrapper cm-card" style="flex: 1">
    <div class="table-header">
      <h3>区域列表</h3>
      <!-- <el-button
        v-auth="`settings-channelConfig-add`"
        :data-testid="`baseSetting-region-r-10001`"
        type="primary"
        icon="plus"
        @click="add"
      >
        <template #icon>
          <icon-plus />
        </template>
        新增区域
      </el-button> -->
    </div>
    <div class="table" :style="computedCardHeight(154)">
      <el-table
        :loading="table.loading"
        :data="table.list"
        :data-testid="`baseSetting-region-r-table`"
        style="width: 100%"
        height="100%"
      >
        <el-table-column prop="name" label="区域名称" show-overflow-tooltip width="120">
          <template #default="{ row, $index }">
            <div :data-testid="`baseSetting-region-r-t1-${$index}`">{{ row.name }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="region" label="关联省市" show-overflow-tooltip>
          <template #default="{ row, $index }">
            <span :data-testid="`baseSetting-region-r-t2-2-${$index}`" class="ml-8">{{
              formatterProvinceText(row.region)
            }}</span>
          </template>
        </el-table-column>
        <!-- <el-table-column prop="status" width="100" label="启用状态">
          <template #default="{ row, $index }">
            <div class="status-wrapper">
              <el-badge
                :data-testid="`baseSetting-region-r-t2-1-${$index}`"
                :status="row.status === '0' ? 'normal' : 'success'"
              />
              <span :data-testid="`baseSetting-region-r-t9-2-${$index}`" class="ml-8">{{
                row.statusText || '-'
              }}</span>
            </div>
          </template>
        </el-table-column> -->
        <!-- <el-table-column label="操作" width="120">
          <template #default="{ row, $index }">
            <el-link
              v-auth="`settings-channelConfig-edit`"
              :data-testid="`baseSetting-region-r-t3-${$index}`"
              :underline="false"
              type="primary"
              @click="edit(row)"
              >编辑
            </el-link>
          </template>
        </el-table-column> -->
      </el-table>

      <!-- 分页组件 -->
      <el-pagination
        v-if="table.total >= useAppStore().showPaginationMinLength"
        v-model:current-page="table.pageNum"
        v-model:page-size="table.pageSize"
        :page-sizes="[10, 15, 20, 25]"
        :total="table.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 16px; display: flex; justify-content: flex-end"
      />
    </div>

    <Form
      v-model="visible"
      :type="2"
      :curCategorize="curCategorize"
      :categorizeTree="categorizeTree"
      :viewStatus="viewStatus"
      :editRecord="editRecord"
      @refreshList="getFirstPageTableData"
    ></Form>
  </div>
</template>

<style scoped lang="scss"></style>
