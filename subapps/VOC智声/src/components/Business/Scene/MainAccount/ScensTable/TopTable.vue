<script setup lang="tsx">
import { ref, watch } from 'vue'
import CustomTable from './CustomTable.vue'
import type { TableInstance } from 'element-plus'
import { fmtPer, fmtFix, fmtNum } from '@/utils'

// 接收props
interface Props {
  data?: any[]
  intention?: string
}

const props = withDefaults(defineProps<Props>(), {
  data: () => [],
  intention: ''
})

// 表格数据
const theData = ref<any[]>([])

watch(
  () => props.data,
  newData => {
    const slidData = newData.slice(0, 5)

    theData.value = slidData

    // console.log('theData', theData.value)
  },
  { immediate: true }
)

// 处理排序变化 - emit排序事件到主页面

const emit = defineEmits<{
  'sort-change': [intention: string, prop: string, order: string | null]
  'row-click': [opinionName: string, intention: string, item: any]
}>()

const handleSort = ({ prop, order }: { prop: string; order: string | null }) => {
  if (!props.intention) return
  if (order) {
    emit('sort-change', props.intention, prop, order === 'ascending' ? 'asc' : 'desc')
  } else {
    emit('sort-change', props.intention, '', null)
  }
}

/**
 * 转发观点名称与意图，供父组件更新客户原声筛选。
 *
 * @param opinionName 当前点击的观点名称
 */
const handleRowClick = (opinionName: string, item: any) => {
  emit('row-click', opinionName, props.intention, item)
}

// 表格实例引用
const tableRef = ref<TableInstance>()

// 清空排序状态
const clearSort = () => {
  tableRef.value?.clearSort()
}

// 暴露方法供父组件调用
defineExpose({
  clearSort
})

// const curRowName = ref<string>('')
// const disabled = ref<boolean>(false)

// el-table悬浮
// const handleEnter = (row: any) => {}
// const handleLeave = (row: any) => {}
</script>

<template>
  <!-- height: 265 -->
  <el-table
    ref="tableRef"
    :data="theData"
    style="width: 100%; display: flex"
    :height="50"
    @sort-change="handleSort"
    class="TopTable"
  >
    <el-table-column prop="opinion" label="观点" />
    <el-table-column prop="mentions" label="提及量" width="100" sortable align="center" />
    <el-table-column prop="mentionsMoM" label="环比" width="100" sortable align="center" />
  </el-table>

  <CustomTable :tableData="theData" @row-click="handleRowClick" />
</template>

<style lang="scss" scoped>
.TopTable {
  :deep(.el-table__cell) {
    position: relative;
    padding: 0 !important;
    color: #5f6a7a !important;
    border-bottom-color: transparent;

    &.is-leaf {
      border-bottom-color: transparent;
    }

    .cell {
      height: 42px;
      line-height: 42px;
      padding: 0 16px;
      font-weight: 500;
      font-size: 14px;
      white-space: nowrap;
      text-overflow: ellipsis;
    }

    // &:hover{
    //   background-color: transparent !important;
    // }
  }

  // 先隐藏表格滚动
  // :deep(.el-scrollbar__bar){
  //   display: none !important;
  // }

  // 边框线
  :deep(.el-table__inner-wrapper) {
    &::before {
      height: 0;
    }
  }
}

.mentionCount {
  font-size: 14px;
  font-weight: 500;
  color: #1f2733;
}

.hot {
  color: #e5484d;
}

.gray {
  color: #666;
}
.green {
  color: #67c23a;
}
</style>
