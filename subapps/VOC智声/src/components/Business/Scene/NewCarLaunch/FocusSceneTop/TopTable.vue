<script setup lang="tsx">
import { ref, watch } from 'vue'
import type { FocusSceneTopVo } from '@/api/newCarLaunch/types'
import { fmtNum, fmtFix } from '@/utils'
import { SmallLineTrendChart } from '@/components/Charts/index'
import SortNum from '@/components/UI/SortNum/index.vue'

interface Props {
  data?: FocusSceneTopVo[]
  intention?: string
}

const props = withDefaults(defineProps<Props>(), {
  data: () => [],
  intention: ''
})

const emit = defineEmits<{
  'sort-change': [{ intention: string; prop: string; order: string }]
  'row-click': [data: any]
}>()

const tableData = ref<FocusSceneTopVo[]>([])

watch(
  () => props.data,
  newData => {
    tableData.value = [...(newData || [])]
  },
  { immediate: true }
)

const handleSortChange = ({ prop, order }: { prop: string; order: string | null }) => {
  if (!props.intention || !order) return

  const sortOrder = order === 'ascending' ? 'asc' : 'desc'
  emit('sort-change', {
    intention: props.intention,
    prop,
    order: sortOrder
  })
}

const handleRowClick = (rowData: any) => {
  emit('row-click', rowData)
}

const tableRef = ref<any>(null)

const clearSort = () => {
  tableRef.value?.clearSort()
}

defineExpose({
  clearSort
})

const columns = ref([
  {
    title: '场景',
    dataIndex: 'scenario',
    showOverflowTooltip: true,
    render: (params: any) => {
      return (
        <div class="flex-y-center" onClick={() => handleRowClick(params.record)}>
          <SortNum rank={params.rowIndex + 1} />
          <span class="channel-name mr-16">{params.record.scenario}</span>
        </div>
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
        <div class="flex-y-center" onClick={() => handleRowClick(params.record)}>
          <span class="mentions-number mr-16">{fmtNum(params.record.mentions)}</span>
          <SmallLineTrendChart data={params.record.mentionsMoMGroup}></SmallLineTrendChart>
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
        <div class="flex-y-center" onClick={() => handleRowClick(params.record)}>
          <span class={['mentions-number', 'mr-16']} style={{ color: '#e5484d' }}>
            {fmtFix(params.record.negativeRate)}
          </span>
          <SmallLineTrendChart data={params.record.negativeRateMoMGroup}></SmallLineTrendChart>
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
    :height="480"
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
</style>
