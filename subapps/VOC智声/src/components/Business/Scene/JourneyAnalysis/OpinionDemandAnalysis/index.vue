<script setup lang="ts">
import { computed, ref } from 'vue'
import ODItem from './ODItem.vue'
import type { IntentionOpinionTopVo } from '@/api/journeyAnalysis/types.d'

defineOptions({
  name: 'OpinionDemandAnalysis'
})

interface Props {
  intentionOpinionData?: IntentionOpinionTopVo[]
}

const { intentionOpinionData = [] } = defineProps<Props>()

// 定义emits
const emit = defineEmits<{
  'sort-change': [{ intention: string; prop: string; order: string }]
  'opinion-click': [{ intention: string; type: 'table' | 'original'; data: any }]
}>()

// 处理排序变化
const handleSortChange = (sortData: { intention: string; prop: string; order: string }) => {
  emit('sort-change', sortData)
}

// 处理观点点击事件
const handleOpinionClick = (clickData: {
  intention: string
  type: 'table' | 'original'
  data: any
}) => {
  emit('opinion-click', clickData)
}

const viewpointConfigs = [
  {
    title: '抱怨',
    titleSvg: 'thumbs-down',
    titlebg: '#FE783F',
    opinionbg: '#FEF0E5',
    tableTitle: '抱怨TOP'
  },
  {
    title: '咨询',
    titleSvg: 'notification-message',
    titlebg: '#0AADFF',
    opinionbg: '#E5FEFA',
    tableTitle: '咨询TOP'
  },
  {
    title: '建议',
    titleSvg: 'notification-text',
    titlebg: '#28C7C7',
    opinionbg: '#E5FAFE',
    tableTitle: '建议TOP'
  },
  {
    title: '表扬',
    titleSvg: 'thumbs-up',
    titlebg: '#14CA64',
    opinionbg: '#E5FEEB',
    tableTitle: '表扬TOP'
  }
]

// 组合配置和数据
const viewpointData = computed(() => {
  return viewpointConfigs.map((config, index) => ({
    ...config,
    data: intentionOpinionData[index] || { originalSound: {}, opinionTops: [] }
  }))
})

const odItemRefs = ref<Record<string, InstanceType<typeof ODItem> | null>>({})

const setOdItemRef = (title: string, el: any) => {
  odItemRefs.value[title] = el
}

const clearAllSort = () => {
  viewpointConfigs.forEach(item => {
    odItemRefs.value[item.title]?.clearSort()
  })
}

defineExpose({
  clearAllSort
})
</script>

<template>
  <div class="opinion-demand-analysis">
    <ODItem
      v-for="item in viewpointData"
      :key="item.title"
      :ref="el => setOdItemRef(item.title, el)"
      :config="item"
      :intention-data="item.data"
      @sort-change="handleSortChange"
      @opinion-click="handleOpinionClick"
    ></ODItem>
  </div>
</template>

<style lang="scss" scoped>
.opinion-demand-analysis {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-top: 24px;
  width: 100%;
  overflow: auto;
}
</style>
