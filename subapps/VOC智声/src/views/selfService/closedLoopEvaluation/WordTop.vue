<script setup lang="ts">
import { ref, computed } from 'vue'

import WordCloudChart from '@/components/Business/Scene/CompetitorAnalysis/ViewpointEvaluationCard/WordCloudChart.vue'
import type { WordCloudItem } from '@/components/Business/Scene/CompetitorAnalysis/ViewpointEvaluationCard/types.d.ts'
import { useUserStore } from '@/store'

/**
 * 综合分析
 */
defineOptions({
  name: 'WordTop'
})

// Props 定义
interface Props {
  opinionTopVos?: any[]
}

const props = withDefaults(defineProps<Props>(), {
  opinionTopVos: () => []
})

const userStore = useUserStore()
// 当前选中的情感
const selectedSentiment = ref('all')
// 情感选项
const sentimentOptions = ref<any[]>([
  { text: '全部情感', value: 'all' },
  ...userStore.getDictItems('voc_sentiment')
])

// 定义emits
const emit = defineEmits<{
  'sentiment-change': [{ sentiment: string | undefined }]
  'view-more-word': any
}>()

// 词云图数据
const wordData = computed<WordCloudItem[]>(() => {
  if (!props.opinionTopVos) return []
  return props.opinionTopVos.map(item => ({
    name: item.name || '',
    value: item.value || 0,
    sentiment: item.sentiment,
    mentionsMoM: item.mentionsMoM,
    mentionsYoY: item.mentionsYoY
  }))
})
// 暴露方法给父组件
// defineExpose({

// })
// 情感切换
const handleSentimentChange = () => {
  emit('sentiment-change', {
    sentiment: selectedSentiment.value === 'all' ? undefined : selectedSentiment.value
  })
}
</script>

<template>
  <FCard
    :title="'观点词云TOP50'"
    titleSize="small"
    :height="'700px'"
    :isShowMore="false"
    class="f-card-border"
  >
    <template #leftExtra>
      <FSelect
        v-model="selectedSentiment"
        :options="sentimentOptions"
        :fields="{ label: 'text', value: 'value' }"
        class="w-120 ml-16"
        @change="handleSentimentChange"
      ></FSelect>
    </template>
    <WordCloudChart :data="wordData"></WordCloudChart>
  </FCard>
</template>

<style lang="scss" scoped></style>
