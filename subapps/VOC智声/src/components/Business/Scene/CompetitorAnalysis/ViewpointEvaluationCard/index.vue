<script setup lang="ts">
import { ref, watch } from 'vue'
import VeItem from './VeItem.vue'
import type { SceneComparisonVo } from '@/api/competitorAnalysis/types'

defineOptions({
  name: 'ViewpointEvaluationCard'
})

// Props 定义
interface Props {
  data?: SceneComparisonVo[]
}

const { data = [] } = defineProps<Props>()

// 定义emits
const emit = defineEmits<{
  'sentiment-change': [{ index: number; sentiment: string | undefined }]
  'word-click': [{ wordData: any; code: string; name: string }]
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
const handleWordClick = (payload: { wordData: any; index: number }) => {
  const item = data[payload.index]
  emit('word-click', {
    wordData: payload.wordData,
    code: item.code || '',
    name: item.name || ''
  })
}
</script>

<template>
  <div class="scene-refinement">
    <VeItem
      v-for="(item, index) in data"
      :key="`${item.code || index}-${resetKey}`"
      :title="item.name"
      :img-url="item.imgUrl"
      :code="item.code"
      :index="index"
      :opinion-top-vos="item.opinionTopVos"
      @sentiment-change="handleSentimentChange"
      @word-click="handleWordClick"
    ></VeItem>
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
