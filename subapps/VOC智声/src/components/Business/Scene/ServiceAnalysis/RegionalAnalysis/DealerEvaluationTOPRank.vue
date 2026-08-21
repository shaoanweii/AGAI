<script setup lang="tsx">
import { ref, watch } from 'vue'
import SortNum from '@/components/UI/SortNum/index.vue'
import type { ServiceDealerRankVo } from '@/api/serviceAnalysis/types'

defineOptions({
  name: 'DealerEvaluationTOPRank'
})

// Props 定义
interface Props {
  dealerRankTopData?: ServiceDealerRankVo[]
}

const props = withDefaults(defineProps<Props>(), {
  dealerRankTopData: () => []
})

// 定义emits
const emit = defineEmits<{
  'sort-change': [{ prop: string; order: string }]
}>()

// 表格数据
const tableData = ref<ServiceDealerRankVo[]>([])

// 监听props变化，更新表格数据
watch(
  () => props.dealerRankTopData,
  newData => {
    tableData.value = [...newData]
  },
  { immediate: true }
)

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

const columns = ref([
  {
    title: '经销商排行',
    dataIndex: 'dealerName',
    showOverflowTooltip: { popperClass: 'text-tooltip-light' },
    render: (params: any) => {
      return (
        <span class="inline-flex-y-center w-full">
          <SortNum rank={params.rowIndex + 1} />
          <span class="ml-8 flex-1 single-line-ellipsis">{params.record.dealerName || '--'}</span>
        </span>
      )
    }
  },
  {
    title: '省份',
    dataIndex: 'provinceName',
    showOverflowTooltip: { popperClass: 'text-tooltip-light' },
    render: (params: any) => {
      return (
        <span class="inline-flex-y-center w-full">
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
    :height="550"
    @sort-change="handleSortChange"
  ></FTable>
</template>

<style lang="scss" scoped>
.mentions-number {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  flex: 0 0 auto;
}
</style>
