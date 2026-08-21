<script setup lang="tsx">
import SortNum from '@/components/UI/SortNum/index.vue'
import { computed, ref } from 'vue'
import type { HighFreqSceneTopVo } from '@/api/journeyAnalysis/types'
import { fmtNum, fmtPer, fmtFix } from '@/utils'

defineOptions({
  name: 'TopFrequentScenarios'
})

const { data } = defineProps<{
  data: HighFreqSceneTopVo[]
}>()

// 事件定义
const emit = defineEmits<{
  (e: 'scene-click', data: HighFreqSceneTopVo): void
}>()

// 处理场景点击事件
const handleSceneClick = (record: HighFreqSceneTopVo) => {
  emit('scene-click', record)
}

const columns = ref([
  {
    title: '场景',
    dataIndex: 'sceneName',
    render: (params: any) => {
      return (
        <div class="flex-y-center cursor-point" onClick={() => handleSceneClick(params.record)}>
          <SortNum rank={params.rowIndex + 1} />
          <span class="ml-8">{params.record.sceneName}</span>
        </div>
      )
    }
  },
  {
    title: '提及量',
    dataIndex: 'mentions',
    width: 150,
    headerCellStyle: { backgroundColor: '#f4f9ff', textAlign: 'center' },
    cellStyle: { backgroundColor: '#f4f9ff', textAlign: 'center' },
    render: (params: any) => {
      return (
        <div class="flex-center">
          <span class="mentions-number">{fmtNum(params.record.mentions)}</span>
        </div>
      )
    }
  },
  {
    title: '提及量环比',
    dataIndex: 'mentionsChange',
    width: 150,
    headerCellStyle: { textAlign: 'center' },
    cellStyle: { textAlign: 'center' },
    render: (params: any) => {
      const change = params.record.mentionsMoM
      return (
        <div class="flex-center">
          <span class={`mentions-change`}>{fmtFix(change)}</span>
        </div>
      )
    }
  },
  {
    title: '负面率',
    dataIndex: 'negativeRate',
    width: 150,
    headerCellStyle: { textAlign: 'center' },
    cellStyle: { textAlign: 'center' },
    render: (params: any) => {
      return (
        <div class="flex-center">
          <span class="mentions-number">{fmtPer(params.record.negativeRate)}</span>
        </div>
      )
    }
  },
  {
    title: '负面率环比',
    dataIndex: 'negativeRateChange',
    width: 150,
    headerCellStyle: { textAlign: 'center' },
    cellStyle: { textAlign: 'center' },
    render: (params: any) => {
      const change = params.record.negativeRateMoM
      return (
        <div class="flex-center">
          <span class={`mentions-change`}>{fmtFix(change)}</span>
        </div>
      )
    }
  }
])
</script>

<template>
  <div>
    <FTable :columns="columns" :has-hover-pop="true" :data="data" :height="238"></FTable>
  </div>
</template>

<style lang="scss" scoped>
.flex-center {
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>
