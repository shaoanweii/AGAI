<script setup lang="tsx">
import { computed, ref } from 'vue'
import SortNum from '@/components/UI/SortNum/index.vue'

/**
 * 关注场景TOP
 */
defineOptions({
  name: 'ChanelTable'
})

// Props 定义
interface Props {
  focusSceneTopData?: any[]
}

const { focusSceneTopData = [] } = defineProps<Props>()

// 定义emits
const emit = defineEmits<{
  'sort-change': [{ prop: string; order: string }]
  'row-click': [data: any]
}>()

// ==================== 事件处理方法 ====================

/**
 * 处理行点击事件
 */
// const handleRowClick = (rowData: any) => {
//   emit('row-click', rowData)
// }

// 将接口数据转换为表格数据格式
const tableData = computed(() => {
  return focusSceneTopData
})

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

const columns = ref<any>([
  {
    title: '一级数据源',
    dataIndex: 'scene',
    render: (params: any) => {
      return (
        <div class="flex-y-center" style="cursor: pointer;">
          <SortNum rank={params.rowIndex + 1} />
          <span class="ml-8">{params.record.scene}</span>
        </div>
      )
    }
  },
  {
    title: '二级数据源',
    dataIndex: 'mentions',
    // width: 150,
    render: (params: any) => {
      return (
        <div class="flex-y-center">
          <span class="mentions-number mr-16">{params.record.mentions}</span>
        </div>
      )
    }
  },
  {
    title: '三级数据源',
    dataIndex: 'xxx',
    // width: 150,
    render: (params: any) => {
      return (
        <div class="flex-y-center">
          <span class="mentions-number mr-16">{params.record.xxx}</span>
        </div>
      )
    }
  },
  {
    title: '事件数',
    dataIndex: 'shijianshu',
    sortable: 'custom',
    // width: 150,
    render: (params: any) => {
      return (
        <div class="flex-y-center">
          <span class="mentions-number mr-16">{params.record.shijianshu}</span>
        </div>
      )
    }
  },
  {
    title: '环比',
    dataIndex: 'huanbi',
    sortable: 'custom',
    // width: 150,
    render: (params: any) => {
      return (
        <div class="flex-y-center">
          <span class="mentions-number mr-16">{params.record.huanbi}</span>
        </div>
      )
    }
  }
])
</script>

<template>
  <div style="width: 100%; height: 100%">
    <FTable
      ref="tableRef"
      :has-hover-pop="true"
      :columns="columns"
      :data="tableData"
      :height="'500px'"
      @sort-change="handleSortChange"
    ></FTable>
  </div>
</template>

<style lang="scss" scoped>
.mentions-number {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  flex: 0 0 auto;
}
</style>
