<script setup lang="ts">
import { computed } from 'vue'
import dayjs from 'dayjs'
import type { VoiceListProps, VoiceListEmits } from './types'
import { useRouter } from 'vue-router'
import { toRgba } from '@/utils'
import { sentimentColors } from '@/constants'

// 为避免名称冲突，命名为 HVoiceListReport
defineOptions({ name: 'HVoiceListReport' })

const router = useRouter()

const props = withDefaults(defineProps<VoiceListProps>(), {
  voiceList: () => [],
  isLoadMore: false,
  loading: true
})

const emit = defineEmits<VoiceListEmits>()

const loadMore = () => {
  emit('load-more')
}

const handleItemClick = (item: any) => {
  emit('item-click', item?.$raw ?? item)
}

// 超过15个字显示省略号
const formatTitle = (title: string | undefined) => {
  return title && title.length > 15 ? title.substring(0, 15) + '...' : title
}

// 统一就近收敛：将展示层所需字段按单条数据映射
const coerceVoiceItemForDisplay = (raw: any, index: number) => {
  try {
    const title = raw?.title ?? ''
    const content = raw?.content ?? raw?.originalTextScene ?? ''
    const topics = Array.isArray(raw?.topics) ? raw.topics : []
    const custName = raw?.custName ?? raw?.username ?? ''
    const channel = raw?.channel ?? raw?.channelName ?? ''
    const dataCreateTime = raw?.dataCreateTime ?? raw?.evaluateTime ?? ''
    return {
      $raw: raw,
      id: raw?.id ?? raw?.newId ?? raw?.originalId ?? String(index),
      title,
      content,
      topics,
      custName,
      channel,
      dataCreateTime
    }
  } catch (e) {
    return {
      $raw: raw,
      id: String(index),
      title: '',
      content: '',
      topics: [],
      custName: '',
      channel: '',
      dataCreateTime: ''
    }
  }
}

const displayList = computed(() => {
  const list = Array.isArray(props.voiceList) ? props.voiceList : []
  return list.map((it, idx) => coerceVoiceItemForDisplay(it, idx))
})
</script>
<template>
  <div>
    <div class="voice-list__items">
      <div
        v-for="(item, index) in displayList"
        :key="`${item.id}-${index}`"
        class="p-12 voice-list__item"
        @click="handleItemClick(item)"
      >
        <!--        <div class="fs-14 fw-500 text-primary mt-8 title-class" v-if="item.title">-->
        <!--          {{ formatTitle(item.title) }}-->
        <!--        </div>-->
        <div
          class="fs-14 fw-400 text-secondary mt-8 title-class"
          v-html="item.content"
          v-if="item.content"
        ></div>
        <div class="voice-list__tags flex flex-wrap mt-10" v-if="item.topics && item.topics.length">
          <template v-for="(topic, idx) in item.topics" :key="idx">
            <span
              v-if="topic.topic"
              class="voice-list__tag fw-400 fs-12"
              :style="{
                'background-color': `${toRgba(sentimentColors[topic?.sentiment], 0.1)}`,
                color: `${sentimentColors[topic?.sentiment]}`
              }"
              >{{ topic.topic }}</span
            >
          </template>
        </div>
        <div class="flex-y-center mt-10" style="white-space: nowrap">
          <template v-if="item.custName">
            <div class="text-secondary fs-12">{{ item.custName }}</div>
            <div class="divider-right"></div>
          </template>

          <template v-if="item.channel">
            <div class="text-secondary single-line-ellipsis fs-12">{{ item.channel }}</div>
            <div class="divider-right"></div>
          </template>
          <template v-if="item.dataCreateTime">
            <div class="text-secondary fs-12">{{ item.dataCreateTime }}</div>
          </template>
        </div>
        <!--        <div class="flex-y-center mt-8" v-if="item.brand || item.carSeries">-->
        <!--          <div class="text-secondary fs-12">{{ item.brand || '' }} {{ item.carSeries || '' }}</div>-->
        <!--        </div>-->
      </div>
    </div>
    <div v-if="loading" class="flex-center">
      <van-loading size="20px">加载中...</van-loading>
    </div>
    <div v-else-if="isLoadMore" class="load-more" @click="loadMore()">点击查看更多</div>
  </div>
</template>
<style scoped lang="scss">
::v-deep(.highlight) {
  color: #1677ff;
}
.voice-list__items {
  background-color: white;
}
.voice-list__item {
  border-bottom: 1px solid #ebebeb;
  font-weight: 400;
  font-size: 14px;
  &:last-child {
    border-bottom: none;
  }
}
.title-class {
  line-height: 20px;
}
.divider-right {
  margin: 0 10px;
  width: 1px;
  height: 10px;
  border-right: 1px solid #929aa6;
}
.two-line-ellipsis {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}
.voice-list__tags {
  gap: 8px;
  .voice-list__tag {
    background: #fee9e5;
    color: #ff4b4c;
    border-radius: 4px;
    padding: 4px 12px;
  }
}
.load-more {
  padding: 10px 0 0 0;
  text-align: center;
  font-weight: 400;
  font-size: 12px;
  color: #929aa6;
}
.title-class {
  line-height: 22px;
}
/* iOS 兼容增强：修复个别设备出现“已出现省略号但未裁剪”的问题 */
.two-line-ellipsis {
  line-clamp: 2; /* 标准属性，兼容新版本浏览器 */
  max-height: 40px; /* 与 .title-class 的 line-height: 20px 对齐，2 行 = 40px */
  word-break: break-word; /* 防止长词或英文导致换行异常 */
}

/* v-html 内容常含 <p>/<br>，在 iOS 上会破坏 -webkit-line-clamp 计算，转为内联消除影响 */
.two-line-ellipsis :deep(p) {
  display: inline;
  margin: 0;
}

.two-line-ellipsis :deep(br) {
  display: inline;
}

.two-line-ellipsis :deep(div) {
  display: inline;
}
</style>
