<script setup lang="tsx">
import { ref, watch, computed } from 'vue'
import SortNum from '@/components/UI/SortNum/index.vue'
import type { DealerRankTopItem } from '@api/drillDownDialog/types.d.ts'
import { fmtPer, fmtFix } from '@/utils'

defineOptions({
  name: 'TopDealerReviews'
})

// Props 定义
interface Props {
  dealerRankTopData?: DealerRankTopItem[]
  dataType?: MentionNegativeRateType
}

const props = withDefaults(defineProps<Props>(), {
  dealerRankTopData: () => [],
  dataType: 'mention'
})

// 定义emits
const emit = defineEmits<{
  'sort-change': [{ prop: string; order: string }]
  //行点击事件
  'row-click': [row: any]
}>()

// 表格数据
const tableData = ref<DealerRankTopItem[]>([])

// 监听props变化，更新表格数据
watch(
  () => props.dealerRankTopData,
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

// 点击行
const handleRowClick = (row: DealerRankTopItem) => {
  emit('row-click', row)
}

// FTable引用
const tableRef = ref<any>(null)

// 暴露清除排序方法
defineExpose({
  clearSort: () => tableRef.value?.clearSort()
})

// 动态列配置
const columns = computed(() => {
  const baseColumns = [
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
      width: 120,
      render: (params: any) => {
        return (
          <span class="inline-flex-y-center w-full">
            <span class="ml-8 flex-1 single-line-ellipsis">
              {params.record.provinceName || '--'}
            </span>
          </span>
        )
      }
    }
  ]

  // 根据dataType动态添加第三、四列
  if (props.dataType === 'negativeRate') {
    baseColumns.push(
      {
        title: '负面率',
        dataIndex: 'negativeRate',
        sortable: 'custom',
        width: 100,
        render: (params: any) => {
          return (
            <div class="flex-y-center">
              <span class="mentions-number mr-16">{fmtPer(params.record.negativeRate)}</span>
            </div>
          )
        }
      } as any,
      {
        title: '环比',
        dataIndex: 'negativeRateMoM',

        width: 100,
        render: (params: any) => {
          return (
            <div class="flex-y-center">
              <span class="mentions-number mr-16">{fmtFix(params.record.negativeRateMoM)}</span>
            </div>
          )
        }
      } as any
    )
  } else {
    baseColumns.push(
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
      } as any,
      {
        title: '环比',
        dataIndex: 'mentionsMoM',

        width: 100,
        render: (params: any) => {
          return (
            <div class="flex-y-center">
              <span class="mentions-number mr-16">{fmtFix(params.record.mentionsMoM)}</span>
            </div>
          )
        }
      } as any
    )
  }

  return baseColumns
})
</script>

<template>
  <FTable
    ref="tableRef"
    :columns="columns"
    :data="tableData"
    :size="'default'"
    :border="false"
    :height="340"
    @row-click="handleRowClick"
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
