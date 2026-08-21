<script setup lang="tsx">
import { computed, onMounted, ref } from 'vue'
import { SmallLineTrendChart } from '@/components/Charts/index'
import SortNum from '@/components/UI/SortNum/index.vue'
import type { SeriesRankItemVo } from '@/api/groupAnalysis/types'
import { fmtNum, fmtPer, fmtFix } from '@/utils'

defineOptions({
  name: 'CarSeriesRank'
})

// ==================== Props 定义 ====================

interface Props {
  data?: SeriesRankItemVo[]
  dataType?: 'brand' | 'series'
}

const props = withDefaults(defineProps<Props>(), {
  data: () => [],
  dataType: 'brand'
})

// ==================== Events 定义 ====================

const emit = defineEmits<{
  switch: [dataType: 'brand' | 'series']
  sort: [sortField: string, sortOrder: 'asc' | 'desc']
  'row-click': [rowData: any]
}>()

// ==================== 数据处理 ====================

// SwitchButton 选项
const switchOptions: [{ value: 'brand'; label: '品牌' }, { value: 'series'; label: '车系' }] = [
  { value: 'brand', label: '品牌' },
  { value: 'series', label: '车系' }
]

// 处理切换事件
const handleSwitch = (option: { value: string | number; label: string }) => {
  emit('switch', option.value as 'brand' | 'series')
}

// 处理排序事件
const handleSort = (sortInfo: { column: any; prop: string; order: string | null }) => {
  if (sortInfo.order) {
    const sortOrder = sortInfo.order === 'ascending' ? 'asc' : 'desc'
    emit('sort', sortInfo.prop, sortOrder)
  }
}

// 处理行点击事件
const handleRowClick = (rowData: any) => {
  emit('row-click', rowData)
}

// 计算卡片标题
const cardTitle = computed(() => {
  return props.dataType === 'brand' ? '品牌排行' : '集团车系排行'
})

// 处理表格数据
const tableData = computed(() => {
  if (!props.data || props.data.length === 0) {
    return []
  }

  return props.data.map((item, index) => ({
    rank: index + 1,
    name: item.name || '',
    imageUrl: item.imageUrl || '',
    mentions: item.mentions || 0,
    negativeRate: `${item.negativeRate || 0}%`,
    negativeRateMoM: item.negativeRateMoM || 0,
    negativeRateYoY: item.negativeRateYoY || 0,
    mentionsMoM: item.mentionsMoM || 0,
    mentionsYoY: item.mentionsYoY || 0,
    mentionsTrend: item.mentionsTrend || [],
    negativeMentionsTrend: item.negativeMentionsTrend || [],
    code: item.code
  }))
})

// 表格列配置
const columns = computed<any>(() => [
  {
    title: props.dataType === 'brand' ? '品牌排行' : '车系排行',
    dataIndex: 'name',
    render: (params: any) => {
      // 根据数据类型设置图片样式类
      const imageClass =
        props.dataType === 'brand'
          ? 'w-48 h-48 ml-8 mr-8 object-contain'
          : 'w-68 h-48 ml-8 mr-8 object-contain'

      return (
        <div class="flex-y-center cursor-point" onClick={() => handleRowClick(params.record)}>
          <SortNum rank={params.rowIndex + 1} />
          {params.record.imageUrl && (
            <img src={params.record.imageUrl} alt={params.record.name} class={imageClass} />
          )}
          <span class="ml-8">{params.record.name}</span>
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
          <span class="mentions-number mr-16">
            {params.record.mentions?.toLocaleString() || '0'}
          </span>
          <SmallLineTrendChart data={params.record.mentionsTrend}></SmallLineTrendChart>
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
          <SmallLineTrendChart data={params.record.negativeMentionsTrend}></SmallLineTrendChart>
        </div>
      )
    }
  }
])

// FTable 组件引用
const tableRef = ref<any>(null)

// 重置排序状态
const resetSort = () => {
  tableRef.value?.clearSort()
}

// 暴露方法给父组件
defineExpose({
  resetSort
})

// onMounted(() => {
//   console.log('@@',props.data)
// })
</script>

<template>
  <FCard :title="cardTitle" titleSize="small" :height="'625px'" class="f-card-border">
    <template #more>
      <SwitchButton
        :model-value="props.dataType"
        :options="switchOptions"
        @change="handleSwitch"
      ></SwitchButton>
    </template>
    <FTable
      ref="tableRef"
      :hasHoverPop="true"
      :columns="columns"
      :data="tableData"
      :height="518"
      @sort-change="handleSort"
    >
    </FTable>
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
