<script setup lang="ts">
import { ref, computed } from 'vue'
import type { SceneTopVo } from '@/api/productAnalysis/types'
import type { ProductSelfSceneTopVo } from '@/api/thisProductAnalysis/types.d'

import FollowSceneTOP from './FollowSceneTOP.vue'
import WordCloudChart from '@/components/Business/Scene/CompetitorAnalysis/ViewpointEvaluationCard/WordCloudChart.vue'
import type { WordCloudItem } from '@/components/Business/Scene/CompetitorAnalysis/ViewpointEvaluationCard/types.d.ts'
import { useUserStore } from '@/store'

/**
 * 综合分析
 */
defineOptions({
  name: 'FourceScren'
})

// 通用场景TOP数据类型 - 兼容产品分析和本品分析
type SceneTopData = SceneTopVo | ProductSelfSceneTopVo

// Props 定义
interface Props {
  focusSceneTopData?: SceneTopData[]
  opinionTopVos?: any[]
}

const props = withDefaults(defineProps<Props>(), {
  opinionTopVos: () => [],
  focusSceneTopData: () => []
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
  'scene-top-sort': [{ prop: string; order: string }]
  'scene-row-click': [data: any]
  'scene-view-more': []
  'view-more-word': any
}>()

// FollowSceneTOP 组件引用
const followSceneTopRef = ref<InstanceType<typeof FollowSceneTOP>>()

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

// 清空关注场景TOP表格的排序状态
const clearSceneTopSort = () => {
  followSceneTopRef.value?.clearSort()
}

// 暴露方法给父组件
defineExpose({
  clearSceneTopSort
})

// 处理关注场景TOP排序事件
const handleSceneTopSort = ({ prop, order }: { prop: string; order: string }) => {
  emit('scene-top-sort', { prop, order })
}

// 处理场景行点击事件
const handleSceneRowClick = (data: any) => {
  emit('scene-row-click', data)
}

// 处理查看更多点击事件
const handleSceneViewMore = () => {
  emit('scene-view-more')
}

// 词云查看更多
const handleViewMore = (isMore: boolean, data: any) => {
  emit('view-more-word', {
    data: {
      __viewMore: isMore,
      data
    }
  })
}

// 词云点击
const handleWordClick = (wordData: any) => {
  emit('view-more-word', {
    data: {
      __viewMore: false,
      data: wordData
    }
  })
}

// 情感切换
const handleSentimentChange = () => {
  emit('sentiment-change', {
    sentiment: selectedSentiment.value === 'all' ? undefined : selectedSentiment.value
  })
}
</script>

<template>
  <div class="fource-scren flex-col">
    <div class="flex mt-24">
      <div class="left">
        <FollowSceneTOP
          ref="followSceneTopRef"
          :focus-scene-top-data="focusSceneTopData"
          @sort-change="handleSceneTopSort"
          @row-click="handleSceneRowClick"
          @view-more="handleSceneViewMore"
        ></FollowSceneTOP>
      </div>
      <div class="right">
        <FCard
          :title="'观点词云TOP50'"
          titleSize="small"
          :height="'700px'"
          :isShowMore="true"
          class="f-card-border"
          @handleMore="() => handleViewMore(true, {})"
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
          <WordCloudChart :data="wordData" @word-click="handleWordClick"></WordCloudChart>
        </FCard>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.fource-scren {
  display: flex;
  // height: 850px;
  width: 100%;
  margin-top: 24px;
  min-width: 0; // 确保flex容器可以正确收缩
  overflow: hidden; // 防止内容溢出

  .left {
    flex: 1;
    height: 100%;
    margin-right: 24px;
    min-width: 0; // 确保flex子元素可以正确收缩
    overflow: hidden; // 防止左侧内容溢出
  }
  .right {
    width: 652px;
    flex: none;
    height: 100%;
    min-width: 400px; // 设置更合理的最小宽度
    max-width: 652px; // 设置最大宽度
    overflow: hidden; // 防止右侧内容溢出

    // 根据容器宽度动态调整右侧宽度
    // 当总宽度小于1600px时，右侧宽度为500px
    @container (max-width: 1600px) {
      width: 500px;
      min-width: 400px;
    }

    // 当总宽度小于1400px时，右侧宽度为450px
    @container (max-width: 1400px) {
      width: 450px;
      min-width: 400px;
    }

    // 当总宽度小于1200px时，右侧宽度为400px
    @container (max-width: 1200px) {
      width: 400px;
      min-width: 350px;
    }

    // 传统媒体查询作为后备方案
    @media (max-width: 1600px) {
      width: 500px;
      min-width: 400px;
    }

    @media (max-width: 1400px) {
      width: 450px;
      min-width: 400px;
    }

    @media (max-width: 1200px) {
      width: 400px;
      min-width: 350px;
    }

    @media (max-width: 1000px) {
      width: 350px;
      min-width: 300px;
    }
  }
}
</style>
