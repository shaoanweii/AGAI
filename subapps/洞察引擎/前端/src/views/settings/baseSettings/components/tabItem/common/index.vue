<script setup lang="ts">
import { useTable } from '@/hooks/table'
import { computedCardHeight } from '@/utils'
import { useAppStore } from '@/stores'

const props = withDefaults(
  defineProps<{
    tabItem: any
  }>(),
  {}
)

const { tabItem } = toRefs(props)

let { table, getTableData, handleSizeChange, handleCurrentChange } = useTable({
  url: tabItem.value.src
})
onMounted(() => {
  getTableData()
})
</script>

<template>
  <div class="common-tabs-content" :style="computedCardHeight(82)">
    <el-table
      :data="table.list"
      :data-testid="`table-${tabItem.key}`"
      v-loading="table.loading"
      style="width: 100%; height: 100%"
    >
      <!-- 动态列将通过 tabItem.columns 配置生成 -->
      <el-table-column
        v-for="column in tabItem.columns"
        :key="column.dataIndex"
        :prop="column.dataIndex"
        :label="column.title"
        :width="column.width"
        show-overflow-tooltip
      >
        <template #default="{ row, $index }">
          <span :data-testid="`${tabItem.key}-baseSettings-2001-t0-${$index}`">
            {{ row[column.dataIndex] || '-' }}
          </span>
        </template>
      </el-table-column>
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
</template>

<style scoped lang="scss"></style>
