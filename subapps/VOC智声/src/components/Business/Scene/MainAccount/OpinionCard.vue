<script setup lang="ts">
import { computed } from 'vue'
import ViewMore from '@/components/Business/DrillDownDialog/components/ViewMore.vue'
import FWordCloud from '@/components/Charts/FWordCloud/index.vue'
import type { WordCloudDataItem } from '@/components/Charts/FWordCloud/types'

defineOptions({
  name: 'OpinionCard'
})

interface Props {
  title?: string
  opinionTopVos?: any[]
  intention?: string
  index?: number
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'word-click': [data: any]
}>()

// 图标配置
const iconClasses = ['opinion-icon-baoyuan', 'opinion-icon-zixun', 'opinion-icon-jianyi', 'opinion-icon-biaoyang']

// 词云图数据
const wordData = computed<WordCloudDataItem[]>(() => {
  if (!props.opinionTopVos || !Array.isArray(props.opinionTopVos)) return []
  return props.opinionTopVos.map(item => ({
    name: item.opinion || '',
    value: item.mentions || 0,
    sentiment: item.sentiment,
    mentionsMoM: item.mentionsMoM,
    mentionsYoY: item.mentionsYoY
  }))
})

// 词云点击
const handleWordClick = (params?: any) => {
  const word = params?.data
  emit('word-click', { word, intention: props.intention })
}

// 查看更多点击
const handleViewMore = () => {
  emit('word-click', { word: undefined, intention: props.intention })
}
</script>

<template>
  <div class="opinion-card">
    <!-- 头部 -->
    <div class="card-header">
      <div class="header-left">
        <span class="icon" :class="iconClasses[index || 0]"></span>
        <span class="title">{{ title || '--' }}</span>
      </div>
      <ViewMore text="查看更多" @click="handleViewMore" />
    </div>

    <!-- 词云内容区 -->
    <div class="card-content">
      <FWordCloud
        v-if="wordData.length > 0"
        :data="wordData"
        :height="'160px'"
        :size-range="[14, 28]"
        :grid-size="4"
        :tooltip="true"
        @chart-click="handleWordClick"
      />
      <div v-else class="no-data">暂无数据</div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.opinion-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;

  .header-left {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .icon {
    width: 20px;
    height: 20px;
    display: inline-block;
    background-size: contain;
    background-repeat: no-repeat;
  }

  .opinion-icon-baoyuan {
    background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='%23646A73' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M17 3a2.828 2.828 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5L17 3z'/%3E%3C/svg%3E");
  }

  .opinion-icon-zixun {
    background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='%23646A73' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z'/%3E%3Ccircle cx='12' cy='11' r='2'/%3E%3C/svg%3E");
  }

  .opinion-icon-jianyi {
    background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='%23646A73' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z'/%3E%3Cpath d='M14 2v6h6M16 13H8M16 17H8M10 9H8'/%3E%3C/svg%3E");
  }

  .opinion-icon-biaoyang {
    background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='%23646A73' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2'/%3E%3Ccircle cx='12' cy='7' r='4'/%3E%3C/svg%3E");
  }

  .title {
    font-size: 14px;
    font-weight: 600;
    color: #1f2329;
  }
}

.card-content {
  flex: 1;
  padding: 12px;
  overflow: hidden;
}

.no-data {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 160px;
  color: #929aa6;
  font-size: 14px;
}
</style>
