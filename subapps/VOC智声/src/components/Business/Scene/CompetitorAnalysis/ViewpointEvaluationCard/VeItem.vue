<script setup lang="tsx">
import { computed, ref, watch } from 'vue'
import WordCloudChart from './WordCloudChart.vue'
import type { WordCloudItem } from './types.d'
import { useUserStore } from '@/store'

defineOptions({
  name: 'VeItem'
})

// Props 定义
interface Props {
  title?: string
  imgUrl?: string
  code?: string
  index?: number
  opinionTopVos?: any[]
}

const props = defineProps<Props>()

// 定义emits
const emit = defineEmits<{
  'sentiment-change': [{ index: number; sentiment: string | undefined }]
  'word-click': [{ wordData: any; index: number }]
}>()

const userStore = useUserStore()

// 情感选项
const sentimentOptions = ref<any[]>([
  { text: '全部情感', value: 'all' },
  ...userStore.getDictItems('voc_sentiment')
])

// 当前选中的情感
const selectedSentiment = ref('all')

// 词云图数据
const wordData = computed<WordCloudItem[]>(() => {
  if (!props.opinionTopVos) return []
  return props.opinionTopVos.map(item => ({
    name: item.opinion || '',
    value: item.mentions || 0,
    sentiment: item.sentiment,
    mentionsMoM: item.mentionsMoM,
    mentionsYoY: item.mentionsYoY
  }))
})

// 情感切换
const handleSentimentChange = () => {
  if (props.index !== undefined) {
    emit('sentiment-change', {
      index: props.index,
      sentiment: selectedSentiment.value === 'all' ? undefined : selectedSentiment.value
    })
  }
}

// 词云点击
const handleWordClick = (wordData: any) => {
  if (props.index !== undefined) {
    emit('word-click', { wordData, index: props.index })
  }
}

// 监听 code 变化，重置情感（仅在通用条件变化时）
watch(
  () => props.code,
  (newCode, oldCode) => {
    // 只有在 code 真正变化时才重置，避免情感切换时被清空
    if (newCode !== oldCode && oldCode !== undefined) {
      selectedSentiment.value = 'all'
    }
  }
)
</script>

<template>
  <div class="ve-item">
    <div class="vei-head" :class="{ start: index === 0 }">
      <div v-if="imgUrl && index !== 0" class="veih-logo mr-16">
        <img :src="imgUrl" alt="" class="w-24 h-24 object-contain" />
      </div>
      <span>{{ title || '市场均值' }}</span>
      <FSelect
        v-model="selectedSentiment"
        :options="sentimentOptions"
        :fields="{ label: 'text', value: 'value' }"
        class="w-120 ml-16"
        @change="handleSentimentChange"
      ></FSelect>
    </div>

    <WordCloudChart :data="wordData" @word-click="handleWordClick"></WordCloudChart>
  </div>
</template>

<style lang="scss" scoped>
.ve-item {
  width: 561px;
  height: 733px;
  box-shadow: 0px 1px 2px 0px rgba(10, 13, 18, 0.05);
  border-radius: 8px 8px 8px 8px;
  border: 1px solid #ebedf0;
  overflow: hidden;
  flex-shrink: 0;
  flex: 1;

  &:first-child {
    .vei-head {
      background: #e5fafe;
    }
  }

  .vei-head {
    width: 100%;
    height: 64px;
    background: #eaf3ff;
    display: flex;
    justify-content: center;
    align-items: center;
    font-weight: 500;
    font-size: 20px;
    color: #1d2129;
    line-height: 24px;

    &.start {
      background: #e5fafe;
    }

    .veih-logo {
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

  .vei-content {
    padding: 24px;
  }
}
</style>
