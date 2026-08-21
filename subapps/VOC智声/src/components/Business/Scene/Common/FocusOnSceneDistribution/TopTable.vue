<script setup lang="tsx">
import { ref, watch } from 'vue'
import SortNum from '@/components/UI/SortNum/index.vue'
import type { IntentionOpinionTopVo } from '@/api/productAnalysis/types'
import type { ServiceIntentionOpinionTopVo } from '@/api/serviceAnalysis/types'
import { fmtNum, fmtFix } from '@/utils'
import HoverPopover from '@/components/Business/Scene/Common/HoverPopover.vue'

defineOptions({
  name: 'TopTable'
})

// 接收props
interface Props {
  data?: IntentionOpinionTopVo[] | ServiceIntentionOpinionTopVo[]
  intention?: string
}

const props = withDefaults(defineProps<Props>(), {
  data: () => [],
  intention: ''
})

// 定义emits
const emit = defineEmits<{
  'sort-change': [{ intention: string; prop: string; order: string }]
  'row-click': [data: any]
}>()

// 表格数据
const tableData = ref<IntentionOpinionTopVo[] | ServiceIntentionOpinionTopVo[]>([])

// 监听props变化，更新表格数据
watch(
  () => props.data,
  newData => {
    tableData.value = [...newData]
  },
  { immediate: true }
)

// 格式化环比数据
// const formatMoMValue = (value: number | undefined | null) => {
//   if (value === undefined || value === null || isNaN(value)) {
//     return '--'
//   }
//   const sign = value >= 0 ? '+' : ''
//   return `${sign}${value}%`
// }

// 处理排序变化 - emit排序事件到主页面
const handleSortChange = ({ prop, order }: { prop: string; order: string | null }) => {
  if (!props.intention || !order) return

  // 将排序事件emit到主页面
  const sortOrder = order === 'ascending' ? 'asc' : 'desc'
  emit('sort-change', {
    intention: props.intention,
    prop,
    order: sortOrder
  })
}

// 处理行点击事件
const handleRowClick = (rowData: any) => {
  emit('row-click', rowData)
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

const columns = ref([
  {
    title: '观点',
    dataIndex: 'opinion',
    showOverflowTooltip: false,
    render: (params: any) => {
      return (
        <HoverPopover
          placement="top"
          show-after={200}
          width={410}
          trigger="hover"
          table-config={{
            title: params.record.opinion,
            data: [
              {
                ...params.record,
                name: '提及量',
                mentions: fmtNum(params.record.mentions),
                mentionsMoM: fmtFix(params.record.mentionsMoM),
                mentionsYoY: fmtFix(params.record.mentionsYoY)
              }
            ],
            columns: [
              { title: '名称', dataIndex: 'name', width: 70 },
              { title: '数值', dataIndex: 'mentions', width: 80 },
              { title: '环比', dataIndex: 'mentionsMoM', className: 'c666' },
              { title: '同比', dataIndex: 'mentionsYoY', className: 'c666' }
            ]
          }}
        >
          {{
            reference: () => (
              <span
                class="inline-flex-y-center w-full cursor-point"
                onClick={() => handleRowClick(params.record)}
              >
                <SortNum rank={params.rowIndex + 1} />
                <span class="ml-8 flex-1 single-line-ellipsis">{params.record.opinion}</span>
              </span>
            )
          }}
        </HoverPopover>
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
          <span class="mentions-number mr-16">{fmtNum(params.record.mentions)}</span>
        </div>
      )
    }
  },
  {
    title: '环比',
    dataIndex: 'mentionsMoM',
    sortable: 'custom',
    width: 100,
    render: (params: any) => {
      return (
        <div class="flex-y-center">
          <span class={['mentions-number', 'mr-16']}>{fmtFix(params.record.mentionsMoM)}</span>
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
    :height="265"
    class="TopTable"
    @sort-change="handleSortChange"
  ></FTable>
</template>

<style lang="scss" scoped>
.TopTable {
  :deep(.el-table__cell) {
    &.is-leaf {
      border-bottom-color: transparent;
    }

    border-bottom-color: transparent;
  }
  :deep(.el-table__inner-wrapper) {
    &::before {
      height: 0;
    }
  }
}

.mentions-number {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  flex: 0 0 auto;
}

.text-danger {
  color: #f56c6c;
}

.text-success {
  color: #67c23a;
}
</style>
