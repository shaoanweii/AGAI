<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import HCollapseCard from '@h5/components/UI/HCollapseCard/index.vue'
import { getBatchEventUserVoiceList } from '@h5/api/batchEvent'
import VoiceItem from './VoiceItem.vue'
import type {
  BatchEventCustomerVoiceDisplayItem,
  BatchEventCustomerVoiceRawItem,
  BatchEventCustomerVoiceTopic
} from './types'
import type { BatchEventUserVoiceVo } from '@h5/api/batchEvent/types'

defineOptions({
  name: 'BatchEventCustomerVoice'
})

interface BatchEventCustomerVoiceProps {
  /** 批量事件 ID，用于查询该事件关联的客户原声 */
  eventId?: string
  /** 品牌编码，仅用于点击列表项后跳转原声详情 */
  brandCode?: string
}

const props = defineProps<BatchEventCustomerVoiceProps>()
const router = useRouter()

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})
const rawList = ref<BatchEventCustomerVoiceRawItem[]>([])
const loading = ref(false)
const requesting = ref(false)
const finished = ref(false)
const firstLoading = ref(false)

let requestSeq = 0

const normalizedEventId = computed(() => String(props.eventId || '').trim())
const normalizedDetailBrandCode = computed(() => String(props.brandCode || '').trim())

const displayList = computed(() =>
  rawList.value.map((item, index) => normalizeVoiceItem(item, index))
)
const isEmpty = computed(() => displayList.value.length === 0)

const footerText = computed(() => {
  if (isEmpty.value || !finished.value) return ''
  return pagination.total > displayList.value.length ? '' : '没有更多了'
})

/**
 * 判断字段是否有有效文本。
 * @param value 原始字段值
 * @returns 是否可展示
 */
const hasText = (value: unknown): boolean => {
  return value !== undefined && value !== null && String(value).trim().length > 0
}

/**
 * 统一输出去空格文本，避免列表中出现 undefined/null。
 * @param value 原始字段值
 * @returns 展示文本
 */
const formatText = (value: unknown): string => {
  return hasText(value) ? String(value).trim() : ''
}

/**
 * 标准化观点标签，兼容字符串数组与对象数组两种接口格式。
 * @param topics 原始观点列表
 * @returns 可直接展示的观点标签
 */
const normalizeTopics = (topics: unknown): BatchEventCustomerVoiceTopic[] => {
  if (!Array.isArray(topics)) return []

  const normalizedTopics: BatchEventCustomerVoiceTopic[] = []

  topics.forEach(topic => {
    if (typeof topic === 'string') {
      const topicText = topic.trim()
      if (topicText) {
        normalizedTopics.push({
          topic: topicText,
          sentiment: '',
          intention: ''
        })
      }
      return
    }

    if (topic && typeof topic === 'object') {
      const topicRecord = topic as Record<string, unknown>
      const topicText = formatText(topicRecord.topic)
      if (!topicText) return

      normalizedTopics.push({
        topic: topicText,
        sentiment: formatText(topicRecord.sentiment),
        intention: formatText(topicRecord.intention)
      })
    }
  })

  return normalizedTopics
}

/**
 * 将接口列表项归一为移动端展示结构，集中处理字段兼容与兜底。
 * @param item 接口原始列表项
 * @param index 当前序号
 * @returns 标准化后的展示项
 */
const normalizeVoiceItem = (
  item: BatchEventCustomerVoiceRawItem,
  index: number
): BatchEventCustomerVoiceDisplayItem => {
  const safeItem = item || {}
  const fallbackId = `batch-voice-${index}`

  return {
    id: String(
      safeItem.id ?? safeItem.newId ?? safeItem.originalId ?? safeItem.dataId ?? fallbackId
    ),
    title: formatText(safeItem.title),
    content: formatText(
      safeItem.originalTexTScene ?? safeItem.originalTextScene ?? safeItem.content
    ),
    custName: formatText(safeItem.custName ?? safeItem.username),
    channel: formatText(safeItem.channel ?? safeItem.channelName),
    dataCreateTime: formatText(safeItem.dataCreateTime ?? safeItem.evaluateTime),
    topics: normalizeTopics(safeItem.topics),
    raw: safeItem
  }
}

/**
 * 将接口返回的客户原声列表项转换为本组件兼容的原始结构。
 * @param item 后端客户原声列表项
 * @returns 组件归一化前的原声项
 */
const normalizeRawVoiceItem = (item: BatchEventUserVoiceVo): BatchEventCustomerVoiceRawItem => {
  return {
    ...item,
    id: item.id ?? item.newId,
    content: item.content,
    originalTextScene: item.originalTextScene,
    originalTexTScene: item.originalTexTScene,
    custName: item.custName ?? item.username,
    channel: item.channel ?? item.channelName,
    dataCreateTime: item.dataCreateTime ?? item.evaluateTime,
    topics: item.topics || []
  }
}

/**
 * 重置客户原声分页状态，通常在事件 ID 变化或首屏刷新前调用。
 */
const resetList = () => {
  pagination.pageNum = 1
  pagination.total = 0
  rawList.value = []
  finished.value = false
}

/**
 * 加载客户原声列表分页数据。
 * @param refresh 是否刷新首屏数据
 */
const loadVoiceList = async (refresh = false) => {
  if (requesting.value && !refresh) return

  const newId = normalizedEventId.value
  const currentRequestSeq = ++requestSeq

  if (!newId) {
    resetList()
    finished.value = true
    loading.value = false
    requesting.value = false
    firstLoading.value = false
    return
  }

  if (refresh) {
    resetList()
    firstLoading.value = true
  }

  loading.value = true
  requesting.value = true

  try {
    const response = await getBatchEventUserVoiceList({
      newId,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    })
    if (currentRequestSeq !== requestSeq) return

    const result = response.result || {}
    const list = (result.list || []).map(item => normalizeRawVoiceItem(item))

    pagination.total = Number(result.total || 0)
    rawList.value = pagination.pageNum === 1 ? list : rawList.value.concat(list)

    finished.value =
      (pagination.total > 0 && rawList.value.length >= pagination.total) ||
      list.length < pagination.pageSize

    if (!finished.value) {
      pagination.pageNum += 1
    }
  } catch (error) {
    if (currentRequestSeq === requestSeq) {
      rawList.value = []
      pagination.total = 0
      finished.value = true
    }
    console.error('获取批量事件客户原声列表失败:', error)
  } finally {
    if (currentRequestSeq === requestSeq) {
      loading.value = false
      requesting.value = false
      firstLoading.value = false
    }
  }
}

/**
 * 点击客户原声列表项后跳转到 H5 原声详情页。
 * @param item 标准化后的客户原声列表项
 */
const handleVoiceItemClick = (item: BatchEventCustomerVoiceDisplayItem) => {
  const raw = item.raw || {}
  const id = String(raw.id ?? raw.newId ?? item.id ?? '').trim()

  if (!id) return

  router.push({
    name: 'H5VoiceDetail',
    query: {
      id,
      originalId: String(raw.originalId || ''),
      brandCode: String(raw.brandCode || normalizedDetailBrandCode.value),
      intent: String(raw.intent || ''),
      channelName: String(raw.channelName || raw.channel || item.channel || '')
    }
  })
}

watch(
  normalizedEventId,
  () => {
    void loadVoiceList(true)
  },
  { immediate: true }
)
</script>

<template>
  <div class="batch-event-customer-voice">
    <HCollapseCard class="customer-voice-card" title="客户原声" collapsible>
      <div v-if="firstLoading" class="voice-loading">
        <van-loading size="24px" vertical>加载中...</van-loading>
      </div>

      <van-empty
        v-else-if="isEmpty"
        image-size="72"
        description="暂无声音数据"
        class="voice-empty"
      />

      <template v-else>
        <div class="voice-scroll">
          <van-list
            v-model:loading="loading"
            :finished="finished"
            finished-text=""
            @load="loadVoiceList(false)"
          >
            <div class="voice-list">
              <VoiceItem
                v-for="(item, index) in displayList"
                :key="`${item.id}-${index}`"
                :item="item"
                @click="handleVoiceItemClick"
              />
            </div>
          </van-list>

          <div v-if="footerText" class="voice-list-footer">
            <span>{{ footerText }}</span>
          </div>
        </div>
      </template>
    </HCollapseCard>
  </div>
</template>

<style scoped lang="scss">
.batch-event-customer-voice {
  flex: 1;
  display: flex;
  flex-direction: column;
  width: 100%;
  max-width: 100%;
  min-width: 0;
  min-height: 0;
  box-sizing: border-box;
  padding: 12px;
}

.customer-voice-card {
  width: 100%;
  max-width: 100%;
  min-width: 0;
  min-height: 0;
  box-sizing: border-box;
}

.customer-voice-card.h-collapse-card--expanded {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.customer-voice-card :deep(.h-collapse-card__body) {
  flex: 1;
  display: flex;
  width: 100%;
  max-width: 100%;
  min-width: 0;
  min-height: 0;
  overflow: hidden;
}

.voice-scroll {
  flex: 1;
  width: 100%;
  max-width: 100%;
  min-width: 0;
  min-height: 0;
  box-sizing: border-box;
  overflow-x: hidden;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;

  &::-webkit-scrollbar {
    display: none;
  }
}

.voice-list {
  width: 100%;
  max-width: 100%;
  min-width: 0;
  box-sizing: border-box;
  background: #ffffff;
}

.voice-list-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 34px;
  color: #929aa6;
  font-weight: 400;
  font-size: 12px;
  line-height: 18px;
}

.voice-empty {
  flex: 1;
  padding: 28px 0;
}

.voice-loading {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 160px;
}
</style>
