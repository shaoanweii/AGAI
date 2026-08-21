<script setup lang="tsx">
import { computed, ref } from 'vue'
import type { SceneTopVo } from '@/api/productAnalysis/types'
import type { ProductSelfSceneTopVo } from '@/api/thisProductAnalysis/types.d'
import { SmallLineTrendChart } from '@/components/Charts/index'
import SortNum from '@/components/UI/SortNum/index.vue'
import ViewMore from '@/components/Business/DrillDownDialog/components/ViewMore.vue'

/**
 * 关注场景TOP
 */
defineOptions({
  name: 'FollowSceneTOP'
})

// 通用场景TOP数据类型 - 兼容产品分析和本品分析
type SceneTopData = SceneTopVo | ProductSelfSceneTopVo

// Props 定义
interface Props {
  focusSceneTopData?: SceneTopData[]
}

const { focusSceneTopData = [] } = defineProps<Props>()

// 定义emits
const emit = defineEmits<{
  'sort-change': [{ prop: string; order: string }]
  'row-click': [data: any]
  'view-more': []
}>()

// ==================== 事件处理方法 ====================

/**
 * 处理行点击事件
 */
const handleRowClick = (rowData: any) => {
  emit('row-click', rowData)
}

/**
 * 处理查看更多点击事件
 */
const handleViewMore = () => {
  emit('view-more')
}

// 将接口数据转换为表格数据格式
const tableData = computed(() => {
  return focusSceneTopData.map(item => ({
    ...item,
    scene: item.scenario,
    negativeRate: `${item.negativeRate}%`
  }))
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
    title: '场景',
    dataIndex: 'scene',
    render: (params: any) => {
      return (
        <div
          class="flex-y-center"
          onClick={() => handleRowClick(params.record)}
          style="cursor: pointer;"
        >
          <SortNum rank={params.rowIndex + 1} />
          <span class="ml-8">{params.record.scene}</span>
        </div>
      )
    }
  },
  {
    title: '提及量',
    dataIndex: 'mentions',
    sortable: 'custom',
    width: 150,
    render: (params: any) => {
      return (
        <div class="flex-y-center">
          <span class="mentions-number mr-16">{params.record.mentions}</span>
          <SmallLineTrendChart data={params.record.mentionsMoMGroup}></SmallLineTrendChart>
        </div>
      )
    }
  },
  {
    title: '负面率',
    dataIndex: 'negativeRate',
    sortable: 'custom',
    width: 150,
    render: (params: any) => {
      return (
        <div class="flex-y-center">
          <span class="mentions-number mr-16">{params.record.negativeRate}</span>
          <SmallLineTrendChart data={params.record.negativeRateMoMGroup}></SmallLineTrendChart>
        </div>
      )
    }
  }
])
</script>

<template>
  <FCard
    :title="'关注场景TOP'"
    titleSize="small"
    :height="'625px'"
    :isShowMore="true"
    class="f-card-border"
    @handleMore="handleViewMore"
  >
    <template #more>
      <ViewMore />
    </template>
    <FTable
      ref="tableRef"
      :has-hover-pop="true"
      :columns="columns"
      :data="tableData"
      :height="518"
      @sort-change="handleSortChange"
    ></FTable>
  </FCard>
</template>

<style lang="scss" scoped>
.mentions-number {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  flex: 0 0 auto;
}
</style>
