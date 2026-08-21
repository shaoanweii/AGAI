<script setup lang="ts">
import { ref } from 'vue'
import SrItem from './SrItem.vue'
import type { SceneComparisonVo } from '@/api/competitorAnalysis/types'

defineOptions({
  name: 'SceneRefinement'
})

// Props 定义
interface Props {
  data?: SceneComparisonVo[]
}

const { data = [] } = defineProps<Props>()

// 定义emits
const emit = defineEmits<{
  'sort-change': [{ index: number; prop: string; order: string }]
  'row-click': [{ rowData: any; code: string; name: string }]
  'view-more': [{ code: string; name: string }]
}>()

// SrItem 组件引用数组
const srItemRefs = ref<(InstanceType<typeof SrItem> | null)[]>([])

// 设置 ref 的函数
const setSrItemRef = (el: InstanceType<typeof SrItem> | null, index: number) => {
  if (el) {
    srItemRefs.value[index] = el
  }
}

// 处理排序变化事件
const handleSortChange = (index: number, sortData: { prop: string; order: string }) => {
  emit('sort-change', {
    index,
    ...sortData
  })
}

// 处理行点击事件
const handleRowClick = (index: number, rowData: any) => {
  const item = data[index]
  emit('row-click', {
    rowData,
    code: item.code || '',
    name: item.name || ''
  })
}

// 处理查看更多点击事件
const handleViewMore = (index: number) => {
  const item = data[index]
  emit('view-more', {
    code: item.code || '',
    name: item.name || ''
  })
}

// 清空所有表格的排序状态
const clearAllSort = () => {
  srItemRefs.value.forEach(ref => {
    ref?.clearSort()
  })
}

// 暴露方法给父组件
defineExpose({
  clearAllSort
})
</script>

<template>
  <div class="scene-refinement">
    <SrItem
      v-for="(item, index) in data"
      :key="item.code || index"
      :ref="el => setSrItemRef(el as InstanceType<typeof SrItem> | null, index)"
      :title="item.name"
      :img-url="item.imgUrl"
      :rank-data="item.sceneTopVos"
      :is-market="index === 0"
      @sort-change="sortData => handleSortChange(index, sortData)"
      @row-click="rowData => handleRowClick(index, rowData)"
      @view-more="() => handleViewMore(index)"
    ></SrItem>
  </div>
</template>

<style lang="scss" scoped>
.scene-refinement {
  margin-top: 24px;
  width: 100%;
  height: 741px;
  // display: grid;
  // grid-template-columns: repeat(auto-fill, minmax(561px, 1fr));
  display: flex;
  overflow-x: auto;
  gap: 24px;
}
</style>
