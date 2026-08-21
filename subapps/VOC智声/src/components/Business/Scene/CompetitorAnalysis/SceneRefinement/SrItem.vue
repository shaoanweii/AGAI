<script setup lang="tsx">
import { computed, ref } from 'vue'
import { SmallLineTrendChart } from '@/components/Charts/index'
import SortNum from '@/components/UI/SortNum/index.vue'
import ViewMore from '@/components/Business/DrillDownDialog/components/ViewMore.vue'

defineOptions({
  name: 'SrItem'
})

// Props 定义
interface Props {
  title?: string
  imgUrl?: string
  rankData?: any[]
  isMarket?: boolean
}

const { title = '市场均值', rankData = [], isMarket = false } = defineProps<Props>()

// 定义emits
const emit = defineEmits<{
  'sort-change': [{ prop: string; order: string }]
  'row-click': [rowData: any]
  'view-more': []
}>()

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

// 处理行点击事件
const handleRowClick = (rowData: any) => {
  emit('row-click', rowData)
}

// 查看更多
const handleViewMore = () => {
  emit('view-more')
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

// 将接口数据转换为表格数据格式
const tableData = computed(() => {
  return rankData.map(item => ({
    scene: item.scenario,
    mentions: item.mentions,
    negativeRate: `${item.negativeRate}%`,
    negativeRateMoM: item.negativeRateMoM,
    mentionsMoM: item.mentionsMoM,
    mentionsMoMGroup: item.mentionsMoMGroup,
    negativeRateMoMGroup: item.negativeRateMoMGroup,
    tag3Code: item.tag3Code,
    tag4Code: item.tag4Code
  }))
})

const columns = ref([
  {
    title: '场景排行',
    dataIndex: 'scene',
    hasHoverPop: true,
    render: (params: any) => {
      return (
        <div class="flex-y-center cursor-point" onClick={() => handleRowClick(params.record)}>
          <SortNum rank={params.rowIndex + 1} />
          <span class="ml-8">{params.record.scene}</span>
        </div>
      )
    }
  },
  {
    title: '提及量',
    dataIndex: 'mentions',
    sortable: true,
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
    sortable: true,
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
  <div class="sr-item" :class="{ 'is-market': isMarket }">
    <div class="sri-head">
      <div class="sri-head__left">
        <div v-if="imgUrl && !isMarket" class="srih-logo mr-16">
          <img :src="imgUrl" alt="" class="w-24 h-24 object-contain" />
        </div>
        <span>{{ title }}</span>
      </div>
      <ViewMore v-if="!isMarket" class="sri-head__more" @click="handleViewMore" />
    </div>

    <div class="sri-content">
      <FTable
        ref="tableRef"
        :hasHoverPop="true"
        :columns="columns"
        :data="tableData"
        :height="625"
        @sort-change="handleSortChange"
      ></FTable>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.sr-item {
  width: 561px;
  height: 733px;
  box-shadow: 0px 1px 2px 0px rgba(10, 13, 18, 0.05);
  border-radius: 8px 8px 8px 8px;
  border: 1px solid #ebedf0;
  overflow: hidden;
  flex-shrink: 0;
  flex: 1;

  &.is-market {
    .sri-head {
      background: #e5fafe;
    }
  }

  .sri-head {
    width: 100%;
    height: 64px;
    background: #eaf3ff;
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 500;
    font-size: 20px;
    color: #1d2129;
    line-height: 24px;
    padding: 0 16px;

    .sri-head__left {
      display: flex;
      justify-content: center;
      align-items: center;
      min-width: 0;
    }

    .srih-logo {
      // width: 24px;
      // height: 24px;
      // background: #ffffff;
      width: 32px;
      height: 32px;
      border-radius: 50%;
      // background: #ffffff;
      display: flex;
      justify-content: center;
      align-items: center;
    }
  }

  .sri-content {
    padding: 24px;
  }
}
</style>
