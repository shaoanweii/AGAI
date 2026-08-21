<script setup lang="tsx">
import { computed, ref, watch } from 'vue'
import WordCloudChart from './WordCloudChart.vue'
import type { WordCloudItem } from './types.d'
import { useUserStore } from '@/store'
import ViewMore from '@/components/Business/DrillDownDialog/components/ViewMore.vue'

defineOptions({
  name: 'VeItem'
})

// Props 定义
interface Props {
  title?: string
  code?: string
  index?: number
  opinionTopVos?: any[]
}

const props = defineProps<Props>()

const headBg = ['#eaf3ff', '#e6fffb', '#fff7e6']

// 定义emits
const emit = defineEmits<{
  'sentiment-change': [{ index: number; sentiment: string | undefined }]
  'word-click': any
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
const handleWordClick = (e: any) => {
  if (props.index !== undefined) {
    emit('word-click', { e, index: props.index })
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
    <div class="vei-head" :style="{ background: headBg[index || 0] }">
      <div class="veih-logo mr-16">
        <div class="tit">{{ title || '--' }}</div>
        <ViewMore text="更多" @click="handleWordClick" />
      </div>

      <FSelect
        v-model="selectedSentiment"
        :options="sentimentOptions"
        :fields="{ label: 'text', value: 'value' }"
        class="w-120 ml-16"
        @change="handleSentimentChange"
      ></FSelect>
    </div>

    <WordCloudChart
      v-if="wordData && wordData.length > 0"
      :data="wordData"
      @word-click="handleWordClick"
    ></WordCloudChart>
    <div v-else class="no-data">暂无数据</div>
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

  // &:first-child {
  //   .vei-head {
  //     background: #e5fafe;
  //   }
  // }

  // 暂无数据样式
  .no-data {
    display: flex;
    width: 100%;
    height: calc(100% - 66px);
    align-items: center;
    justify-content: center;
    font-size: 16px;
    color: #999;
    background-color: #fafafa;
  }

  .vei-head {
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
    padding: 0px 16px;

    // &.start {
    //   background: #e5fafe;
    // }

    .veih-logo {
      height: 32px;
      border-radius: 50%;
      // background: #ffffff;
      display: flex;
      justify-content: center;
      align-items: center;
    }
    .tit {
      margin-right: 4px;
      font-size: 17px;
      color: #1d252f;
      font-weight: 600;
    }
  }

  .vei-content {
    padding: 24px;
  }
}
</style>
