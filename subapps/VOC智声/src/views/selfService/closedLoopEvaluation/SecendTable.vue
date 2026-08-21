<script setup lang="tsx">
import { ref, watch } from 'vue'
import { fmtNum, fmtFix } from '@/utils'
// import { SmallLineTrendChart } from '@/components/Charts/index'
import SortNum from '@/components/UI/SortNum/index.vue'

interface Props {
  data?: any[]
}

const props = withDefaults(defineProps<Props>(), {
  data: () => []
})

const emit = defineEmits<{
  'row-click': [data: any]
}>()

const tableData = ref<any[]>([])

watch(
  () => props.data,
  newData => {
    tableData.value = [...(newData || [])]
  },
  { immediate: true }
)

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
    title: '二级主题',
    dataIndex: 'name',
    showOverflowTooltip: true
    // <SortNum rank={params.rowIndex + 1} />
  },
  {
    title: '事件数',
    dataIndex: 'mentions',
    width: 100,
    render: (params: any) => {
      return (
        <div class="flex-y-center" onClick={() => handleRowClick(params.record)}>
          <span class="mentions-number mr-16">{fmtNum(params.record.value)}</span>
        </div>
      )
    }
  },
  {
    title: '占比',
    dataIndex: 'negativeRate',
    width: 100,
    render: (params: any) => {
      return (
        <div class="flex-y-center" onClick={() => handleRowClick(params.record)}>
          <span class={['mentions-number', 'mr-16']} style={{ color: '#999' }}>
            {fmtFix(params.record.value)}
          </span>
        </div>
      )
    }
  },
  {
    title: '环比',
    dataIndex: 'negativeRate',
    width: 100,
    render: (params: any) => {
      return (
        <div class="flex-y-center" onClick={() => handleRowClick(params.record)}>
          <span class={['mentions-number', 'mr-16']} style={{ color: '#999' }}>
            {fmtFix(params.record.value)}
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
    :height="280"
    class="TopTable"
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
