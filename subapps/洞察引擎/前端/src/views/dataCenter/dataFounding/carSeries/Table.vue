<template>
  <div class="table-wrapper cm-card" style="flex: 1; width: 0">
    <div class="table-header">
      <h3>车系列表</h3>
      <el-button
        v-auth="`dataCenter-carSeries-add`"
        :data-testid="`founding-table-10001`"
        type="primary"
        @click="handleAdd"
      >
        <template #icon>
          <Plus />
        </template>
        新增车系</el-button
      >
    </div>
    <div class="table" :style="computedCardHeight(274)">
      <el-table
        v-loading="table.loading"
        :data="table.list"
        style="width: 100%"
        :max-height="'100%'"
      >
        <el-table-column prop="name" label="车系名称" show-overflow-tooltip>
          <template #default="{ row, $index }">
            <span :data-testid="`founding-table-10001-t0-${$index}`">{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="carLevel2Text" label="车辆级别" width="220">
          <template #default="{ row, $index }">
            <span :data-testid="`founding-table-10001-t1-${$index}`">{{
              row.carLevel2Text || '-'
            }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="energyType2Text" label="能源类型" width="220">
          <template #default="{ row, $index }">
            <span :data-testid="`founding-table-10001-t2-${$index}`">{{
              row.energyType2Text
            }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row, $index }">
            <el-button
              v-auth="`dataCenter-carSeries-edit`"
              :data-testid="`founding-table-10001-b1-${$index}`"
              :underline="false"
              type="primary"
              link
              :disabled="row.name?.endsWith('无车型信息')"
              @click="handleEdit(row)"
              >编辑</el-button
            >
            <el-popconfirm title="确认删除?" @confirm="confirmDelete({ id: row.id })">
              <template #reference>
                <el-button
                  v-auth="`dataCenter-carSeries-delete`"
                  :data-testid="`founding-table-10001-b2-${$index}`"
                  :underline="false"
                  :disabled="row.name?.endsWith('无车型信息')"
                  type="danger"
                  link
                  >删除</el-button
                >
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <el-pagination
        v-if="table.total >= 10"
        v-model:current-page="table.pageNum"
        v-model:page-size="table.pageSize"
        :page-sizes="[10, 15, 20, 25]"
        :total="table.total"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 16px; justify-content: flex-end"
        small
      />
    </div>
    <SeriesForm :filter="filter" @refreshCarseriesList="getTableData" />
  </div>
</template>
<script lang="ts" setup>
import { useTable } from '@/hooks/table'
import SeriesForm from './SeriesForm.vue'
import { Plus } from '@element-plus/icons-vue'
import { computedCardHeight } from '@/utils'

const {
  table,
  form,
  getTableData,
  handleSizeChange,
  handleCurrentChange,
  handleAdd,
  handleEdit,
  handleDelete
} = useTable({
  method: 'POST',
  url: '/insights/carSeriesInfo/queryBySelect',
  deleteUrl: '/insights/carSeriesInfo/deleteCarSeriesInfo'
})

provide('form', form)

const props = defineProps<{
  filter: object
}>()

const refreshTable = () => {
  table.filter = props.filter
  table.pageNum = 1
  getTableData()
}

defineExpose({
  getTableData: refreshTable
})

const confirmDelete = (param: any) => {
  handleDelete(param)
}
</script>
