<script setup lang="ts">
import { computed } from 'vue'
import HCard from '@h5/components/UI/HCard/index.vue'
import WordCloudChart from '@h5/components/RootCauseAnalysis/WordCloudChart.vue'
import type { IntentionOpinionTopVo } from '@h5/api/rootCauseAnalysis/types'

defineOptions({
  name: 'OpinionCloudCard'
})

const props = defineProps<{
  data: IntentionOpinionTopVo[]
  carSeriesName?: string
  tagName?: string
  activeOpinion?: string
}>()

const emit = defineEmits<{
  wordClick: [item: IntentionOpinionTopVo]
}>()

const hasData = computed(() => props.data.length > 0)

const titleParts = computed(() => {
  return [props.carSeriesName, props.tagName].filter(Boolean)
})

const wordData = computed(() => {
  return props.data.slice(0, 20).map(item => ({
    name: item.opinion,
    value: item.mentions,
    sentiment: item.sentiment,
    mentionsMoM: item.mentionsMoM,
    mentionsYoY: item.mentionsYoY,
    remark: item.remark,
    raw: item
  }))
})

/**
 * 词云点击后抛出原始观点数据，页面负责处理联动开关。
 * @param data 词云节点
 */
const handleWordClick = (data: any) => {
  emit('wordClick', data?.raw || data)
}
</script>

<template>
  <HCard title="观点评价">
    <template v-if="titleParts.length > 0" #left>
      <div class="opinion-title-suffix van-ellipsis flex-1">
        <span v-for="item in titleParts" :key="item">【{{ item }}】</span>
      </div>
    </template>
    <div v-if="hasData" class="opinion-cloud">
      <WordCloudChart :data="wordData" :active-name="activeOpinion" @word-click="handleWordClick" />
    </div>
    <van-empty v-else image-size="64" description="暂无数据" />
  </HCard>
</template>

<style scoped lang="scss">
.opinion-title-suffix {
  min-width: 0;
  overflow: hidden;
  color: #1677ff;
  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.opinion-cloud {
  height: 220px;
  overflow: hidden;
}
</style>
