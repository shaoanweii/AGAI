<script setup lang="ts">
import { computed } from 'vue'
import { sentimentColors } from '@/constants'
import { toRgba } from '@/utils'
import type { BatchEventCustomerVoiceDisplayItem } from './types'

defineOptions({
  name: 'BatchEventCustomerVoiceItem'
})

interface VoiceItemProps {
  /** 标准化后的客户原声展示数据 */
  item: BatchEventCustomerVoiceDisplayItem
}

const props = defineProps<VoiceItemProps>()
const emit = defineEmits<{
  /** 点击原声列表项，交由父组件跳转详情。 */
  click: [item: BatchEventCustomerVoiceDisplayItem]
}>()

const DEFAULT_TOPIC_COLOR = '#1677FF'

/**
 * 判断展示字段是否有有效文本，避免元信息区域出现空分隔符。
 * @param value 原始字段值
 * @returns 是否可展示
 */
const hasText = (value: unknown): boolean => {
  return value !== undefined && value !== null && String(value).trim().length > 0
}

const contentHtml = computed(() => {
  return hasText(props.item.content) ? props.item.content : props.item.title || '-'
})

const metaList = computed(() => {
  return [props.item.custName, props.item.channel, props.item.dataCreateTime]
    .filter(hasText)
    .map(item => String(item).trim())
})

/**
 * 根据观点情感生成标签颜色，未知情感走蓝色兜底。
 * @param sentiment 情感名称
 * @returns 标签样式
 */
const getTopicStyle = (sentiment?: string) => {
  const color = sentimentColors?.[sentiment || ''] || DEFAULT_TOPIC_COLOR
  return {
    backgroundColor: toRgba(color, 0.1),
    color
  }
}
</script>

<template>
  <div class="batch-event-voice-item" @click="emit('click', props.item)">
    <div class="voice-content two-line-ellipsis" v-html="contentHtml"></div>

    <div v-if="props.item.topics.length > 0" class="voice-tags">
      <template v-for="(topic, index) in props.item.topics" :key="`${topic.topic}-${index}`">
        <span v-if="topic.topic" class="voice-tag" :style="getTopicStyle(topic.sentiment)">
          {{ topic.topic }}
        </span>
      </template>
    </div>

    <div v-if="metaList.length > 0" class="voice-meta">
      <template v-for="(text, index) in metaList" :key="`${text}-${index}`">
        <span class="voice-meta__text">{{ text }}</span>
        <span v-if="index < metaList.length - 1" class="voice-meta__divider"></span>
      </template>
    </div>
  </div>
</template>

<style scoped lang="scss">
.batch-event-voice-item {
  width: 100%;
  max-width: 100%;
  min-width: 0;
  box-sizing: border-box;
  padding: 14px 12px;
  background: #ffffff;
  border-bottom: 1px solid #ebeef2;
  overflow: hidden;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;

  &:active {
    background: #f4f6f9;
  }
}

.voice-content {
  max-width: 100%;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  color: #5f6a7a;
  overflow-wrap: anywhere;
  word-break: break-word;
}

.voice-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  gap: 8px;
  max-width: 100%;
  min-width: 0;
  margin-top: 10px;
  overflow: hidden;
}

.voice-tag {
  max-width: 100%;
  box-sizing: border-box;
  padding: 4px 10px;
  border-radius: 4px;
  font-weight: 400;
  font-size: 12px;
  line-height: 18px;
  white-space: normal;
  overflow-wrap: anywhere;
  word-break: break-word;
}

.voice-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px 0;
  max-width: 100%;
  min-width: 0;
  margin-top: 10px;
  color: #5f6a7a;
  font-weight: 400;
  font-size: 12px;
  line-height: 18px;
}

.voice-meta__text {
  max-width: 100%;
  min-width: 0;
  overflow-wrap: anywhere;
  word-break: break-word;
}

.voice-meta__divider {
  flex: 0 0 auto;
  width: 1px;
  height: 10px;
  margin: 0 10px;
  background: #dfe2e8;
}

.two-line-ellipsis {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  max-width: 100%;
  max-height: 44px;
  overflow: hidden;
}

.two-line-ellipsis :deep(*) {
  max-width: 100%;
  overflow-wrap: anywhere;
  word-break: break-word;
}

.two-line-ellipsis :deep(p),
.two-line-ellipsis :deep(div),
.two-line-ellipsis :deep(br) {
  display: inline;
  margin: 0;
}
</style>
