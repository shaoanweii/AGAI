<script setup lang="ts">
import { ref, watch } from 'vue'
import VeItem from './VeItem.vue'
import type { SceneComparisonVo } from '@/api/competitorAnalysis/types'
import { INTERVAL_TYPE_OPTIONS } from '../constants'

defineOptions({
  name: 'OverallImpression'
})

// Props 定义
interface Props {
  data?: any[]
}

const {
  data = [] // 模拟数据
} = defineProps<Props>()

const vvList = INTERVAL_TYPE_OPTIONS.map(item => item.value)

// 定义emits
const emit = defineEmits<{
  'sentiment-change': [{ index: number; sentiment: string | undefined }]
  'word-click': any
}>()

// 监听通用条件变化，重置情感选择（仅监听 code 变化）
const resetKey = ref(0)
watch(
  () => data.map(item => item.code).join(','),
  () => {
    resetKey.value++
  }
)

// 处理情感切换事件
const handleSentimentChange = (payload: { index: number; sentiment: string | undefined }) => {
  emit('sentiment-change', payload)
}

// 处理词云点击事件
const handleWordClick = (payload: { e: any; index: number }, item: any) => {
  emit('word-click', {
    index: payload.index,
    e: payload.e,
    item
  })
}
</script>

<template>
  <div class="over-imp">
    <VeItem
      v-for="(item, index) in data"
      :key="`${item.code || index}-${resetKey}`"
      :title="vvList[index]"
      :code="item.code"
      :index="index"
      :opinion-top-vos="item.opinionTopVos"
      @sentiment-change="handleSentimentChange"
      @word-click="e => handleWordClick(e, item)"
    ></VeItem>
  </div>
</template>

<style lang="scss" scoped>
.over-imp {
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
