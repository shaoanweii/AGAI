<script setup lang="tsx">
import { ref, computed, watch } from 'vue'
import SortNum from '@/components/UI/SortNum/index.vue'
import type { ServiceProvinceRankVo } from '@/api/serviceAnalysis/types'

defineOptions({
  name: 'ProvinceRank'
})

// Props 定义
interface Props {
  provinceRankData?: ServiceProvinceRankVo[]
  selectedProvinceCode?: string
}

const props = withDefaults(defineProps<Props>(), {
  provinceRankData: () => [],
  selectedProvinceCode: ''
})

// 定义emits
const emit = defineEmits<{
  'sort-change': [{ prop: string; order: string }]
  'province-select': [provinceCode: string]
}>()

// 表格数据
const tableData = ref<ServiceProvinceRankVo[]>([])

// 监听props变化，更新表格数据
watch(
  () => props.provinceRankData,
  newData => {
    tableData.value = [...newData]
  },
  { immediate: true }
)

// 处理排序变化 - emit排序事件到主页面
const handleSortChange = ({ prop, order }: { prop: string; order: string | null }) => {
  if (!order) return

  // 将排序事件emit到主页面
  const sortOrder = order === 'ascending' ? 'asc' : 'desc'
  emit('sort-change', {
    prop,
    order: sortOrder
  })
}

// FTable 引用
const tableRef = ref<any>(null)

// 清空排序状态
const clearSort = () => {
  tableRef.value?.clearSort()
}

// 暴露方法给父组件
defineExpose({
  clearSort
})

// 处理行点击事件
const handleRowClick = (row: ServiceProvinceRankVo) => {
  emit('province-select', row.provinceCode)
}

// 判断行是否被选中
const isRowSelected = (row: ServiceProvinceRankVo) => {
  return row.provinceCode === props.selectedProvinceCode
}

// 获取行的 CSS 类名
const getRowClassName = ({ row }: { row: ServiceProvinceRankVo }) => {
  return isRowSelected(row) ? 'selected-row' : ''
}

const columns = ref([
  {
    title: '省份',
    dataIndex: 'provinceName',
    showOverflowTooltip: { popperClass: 'text-tooltip-light' },
    render: (params: any) => {
      return (
        <span class="inline-flex-y-center w-full">
          <SortNum rank={params.rowIndex + 1} />
          <span class="ml-8 flex-1 single-line-ellipsis">{params.record.provinceName || '--'}</span>
        </span>
      )
    }
  },
  {
    title: '提及量',
    dataIndex: 'mentions',
    sortable: 'custom',
    width: 100,
    render: (params: any) => {
      return (
        <div class="flex-y-center">
          <span class="mentions-number mr-16">{params.record.mentions || 0}</span>
        </div>
      )
    }
  },
  {
    title: '负面率',
    dataIndex: 'negativeRate',
    sortable: 'custom',
    width: 100,
    render: (params: any) => {
      return (
        <div class="flex-y-center">
          <span class="mentions-number mr-16">
            {params.record.negativeRate != null ? `${params.record.negativeRate}%` : '--'}
          </span>
        </div>
      )
    }
  }
])
</script>

<template>
  <FTable
    ref="tableRef"
    :columns="columns"
    :data="tableData"
    :size="'default'"
    :border="false"
    :height="470"
    class="clear-table-border province-rank-table"
    :row-class-name="getRowClassName"
    @sort-change="handleSortChange"
    @row-click="handleRowClick"
  ></FTable>
</template>

<style lang="scss" scoped>
.province-rank-table {
  :deep(.selected-row) {
    background-color: #e6f7ff !important;

    &:hover {
      background-color: #bae7ff !important;
    }
  }

  :deep(.el-table__row) {
    cursor: pointer;

    &:hover {
      background-color: #f5f5f5;
    }
  }
}

.mentions-number {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  flex: 0 0 auto;
}
</style>
