<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, shallowRef, watch } from 'vue'
import tips2Png from '@/assets/h5/tips-2.png'
import { showToast } from 'vant'
import { EventPriorityTipMap } from '@/views/H5/constants/index'
import type { BatchEventBriefDetailVo } from '@h5/api/batchEvent/types'

interface EventInfoPanelProps {
  data: BatchEventBriefDetailVo
  /** 是否将业务责任人展示为“姓名-工号”，默认沿用仅展示姓名 */
  formatMainRespUserWithEmpNo?: boolean
}

const props = withDefaults(defineProps<EventInfoPanelProps>(), {
  formatMainRespUserWithEmpNo: false
})

const isExpanded = shallowRef(false)
const isTopicOverflow = shallowRef(false)
const topicListRef = ref<HTMLElement | null>(null)
let topicResizeObserver: ResizeObserver | null = null

const mainRespDeptName = computed(() => {
  const primaryDepName = String(props.data.primaryDepName || '').trim()

  return primaryDepName || '-'
})

/**
 * 规范化详情字段展示值，避免空值参与拼接后出现多余连接符。
 * @param value 后端返回的展示字段
 * @returns 去除首尾空格后的展示文案
 */
const normalizeInfoValue = (value?: string | number | null) => {
  return value === null || value === undefined ? '' : String(value).trim()
}

const mainRespUserText = computed(() => {
  const userParts = [
    normalizeInfoValue(props.data.mainRespUserName),
    normalizeInfoValue(props.data.mainRespUserEmpNo)
  ].filter(Boolean)

  return userParts.length ? userParts.join('-') : '-'
})

const infoRows = computed(() => [
  {
    label: '预警频率',
    value: props.data.warningPeriod || '-'
  },
  {
    label: '预警时间',
    value: props.data.warningTime || '-'
  },
  {
    label: '主题分类',
    value: props.data.subjectCategoryName || '-'
  },
  {
    label: '主责部门',
    value: mainRespDeptName.value
  },
  {
    label: '业务责任人',
    value: props.formatMainRespUserWithEmpNo
      ? mainRespUserText.value
      : normalizeInfoValue(props.data.mainRespUserName) || '-'
  }
])

const focusTopics = computed(() => {
  return Array.isArray(props.data.focusTopics) ? props.data.focusTopics.filter(Boolean) : []
})

const priorityText = computed(() => props.data.eventPriorityName?.toLocaleUpperCase() || '-')

/**
 * 从后端优先级字段中提取 P0-P4 编码，兼容 P0/p0/P0级 等展示值。
 * @param value 事件优先级编码或名称
 * @returns 可用于提示映射的优先级 key
 */
const getPriorityTipKey = (value: unknown) => {
  const matched = String(value || '')
    .trim()
    .match(/^p[0-4]/i)

  return matched?.[0].toLowerCase() || ''
}

/**
 * 展示处理优先级说明；无优先级编码或未配置映射时不打扰用户。
 */
const showPriorityTip = () => {
  const priority =
    getPriorityTipKey(props.data.eventPriority) || getPriorityTipKey(props.data.eventPriorityName)
  if (!priority) return

  const tip = EventPriorityTipMap[priority as keyof typeof EventPriorityTipMap]
  if (!tip) return

  showToast(tip)
}

/**
 * 判断聚焦观点是否超过一行，超过时才展示展开/收起入口。
 */
const updateTopicOverflow = async () => {
  await nextTick()
  const topicList = topicListRef.value
  if (!topicList) {
    isTopicOverflow.value = false
    return
  }

  isTopicOverflow.value = topicList.scrollHeight > 24
}

/**
 * 切换聚焦观点展开状态。
 */
const toggleExpanded = () => {
  isExpanded.value = !isExpanded.value
}

watch(
  focusTopics,
  () => {
    isExpanded.value = false
    void updateTopicOverflow()
  },
  { immediate: true }
)

onMounted(() => {
  void updateTopicOverflow()
  if (topicListRef.value && window.ResizeObserver) {
    topicResizeObserver = new ResizeObserver(() => {
      void updateTopicOverflow()
    })
    topicResizeObserver.observe(topicListRef.value)
  }
})

onUnmounted(() => {
  topicResizeObserver?.disconnect()
  topicResizeObserver = null
})
</script>

<template>
  <div class="event-info-panel">
    <div class="summary-grid">
      <div class="summary-item">
        <div class="summary-label">创建人员</div>
        <div class="summary-value">{{ normalizeInfoValue(props.data.createUserName) || '-' }}</div>
      </div>
      <div class="summary-divider"></div>
      <div class="summary-item">
        <div class="summary-label">品牌</div>
        <div class="summary-value">{{ props.data.brandName || '-' }}</div>
      </div>
      <div class="summary-divider"></div>
      <div class="summary-item">
        <div class="summary-label summary-label--with-icon" @click="showPriorityTip">
          <span>处理优先级</span>
          <img class="tips-icon" :src="tips2Png" alt="" />
        </div>
        <div class="summary-value summary-value--priority">
          {{ priorityText }}
        </div>
      </div>
    </div>

    <div class="info-list">
      <div v-for="item in infoRows" :key="item.label" class="info-row">
        <div class="info-label">{{ item.label }}</div>
        <div class="info-value">{{ item.value }}</div>
      </div>

      <div class="info-row info-row--topics">
        <div class="info-label">聚焦观点</div>
        <div
          ref="topicListRef"
          class="topic-list"
          :class="{ 'topic-list--collapsed': !isExpanded }"
        >
          <span v-for="topic in focusTopics" :key="topic" class="topic-tag">
            {{ topic }}
          </span>
          <span v-if="!focusTopics.length" class="topic-empty">-</span>
        </div>
      </div>
    </div>

    <button v-if="isTopicOverflow" class="expand-button" type="button" @click="toggleExpanded">
      <span>{{ isExpanded ? '收起' : '展开更多' }}</span>
      <van-icon :name="isExpanded ? 'arrow-up' : 'arrow-down'" color="#929AA6" size="14" />
    </button>
  </div>
</template>

<style scoped lang="scss">
.event-info-panel {
  color: #1f2733;
}

.summary-grid {
  display: grid;
  grid-template-columns: 1fr 1px 1fr 1px 1fr;
  align-items: center;
  min-height: 64px;
  padding: 10px 6px;
  background: #f5f7fb;
  border-radius: 8px;
}

.summary-item {
  min-width: 0;
  text-align: center;
}

.summary-label {
  display: flex;
  align-items: center;
  justify-content: center;
  column-gap: 3px;
  font-size: 12px;
  line-height: 18px;
  color: #929aa6;
}

.summary-label--with-icon {
  white-space: nowrap;
  cursor: pointer;
}

.tips-icon {
  width: 12px;
  height: 12px;
  flex: 0 0 auto;
}

.summary-value {
  margin-top: 4px;
  font-weight: 600;
  font-size: 13px;
  line-height: 20px;
  color: #1f2733;
  word-break: break-all;
}

.summary-value--priority {
  color: #ff5c64;
}

.summary-divider {
  width: 1px;
  height: 36px;
  background: #ebeef2;
}

.info-list {
  display: flex;
  flex-direction: column;
  row-gap: 10px;
  margin-top: 12px;
}

.info-row {
  display: grid;
  grid-template-columns: 74px minmax(0, 1fr);
  align-items: flex-start;
  column-gap: 10px;
}

.info-label {
  font-size: 12px;
  line-height: 22px;
  color: rgba(0, 0, 0, 0.38);
  text-align: right;
  white-space: nowrap;
}

.info-value {
  font-weight: 500;
  font-size: 12px;
  line-height: 22px;
  color: rgba(0, 0, 0, 0.86);
  word-break: break-word;
}

.topic-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-width: 0;
}

.topic-list--collapsed {
  max-height: 24px;
  overflow: hidden;
}

.topic-tag {
  max-width: 100%;
  padding: 3px 10px;
  border-radius: 2px;
  background: #f7f9fc;
  font-weight: 500;
  font-size: 12px;
  line-height: 18px;
  color: rgba(0, 0, 0, 0.86);
  word-break: break-all;
}

.topic-empty {
  font-weight: 500;
  font-size: 12px;
  line-height: 22px;
  color: rgba(0, 0, 0, 0.86);
}

.expand-button {
  display: flex;
  align-items: center;
  justify-content: center;
  column-gap: 6px;
  width: 112px;
  height: 24px;
  margin: 12px auto -12px;
  padding: 0;
  border: 0;
  background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='112' height='24' viewBox='0 0 112 24' fill='none'%3E%3Cpath d='M0 24L6 4Q6 0 10 0H102Q106 0 106 4L112 24Z' fill='white'/%3E%3Cpath d='M0 24L6 4Q6 0 10 0H102Q106 0 106 4L112 24' stroke='%23EBEDF0' stroke-width='1'/%3E%3C/svg%3E")
    center / 100% 100% no-repeat;
  font-size: 13px;
  color: #606a78;
}
</style>
