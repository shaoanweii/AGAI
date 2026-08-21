<script setup lang="ts">
import { computed } from 'vue'
import tips2Png from '@/assets/h5/tips-2.png'
import { EVENT_PRIORITY_LEVEL_COLORS, EVENT_LEVEL_TO_PRIORITY_KEY } from '@/views/H5/constants'
import { showToast } from 'vant'
import { EventLevelTipMap, EventPriorityTipMap } from '@/views/H5/constants/index'

export interface EventBaseInfo {
  /** 事件等级标签，例如 S级/A级 */
  eventLevelName?: string
  /** 处理优先级标签，例如 P0/P1 */
  eventLevel?: string
  eventPriority?: string
  eventPriorityName?: string
  /** 处理人员 */
  handleUser?: {
    userName?: string
  }

  /** 主题分类名称 */
  subjectCategoryName?: string
  /** 触发/预警时间 */
  warningTime?: string
  /** 主责部门 */
  mainRespOrgName?: string
  /** 敏感类型（名称优先） */
  sensitiveType?: string
  /** 事件清晰度（名称优先） */
  eventClarity?: string
}

interface EventBaseInfoSectionProps {
  data: EventBaseInfo | null
}

const props = defineProps<EventBaseInfoSectionProps>()

const getTagColor = (tag?: string, mapping: Record<string, string> = {}, defaultColor = '') => {
  if (!tag) return defaultColor
  return mapping[tag] || defaultColor
}

const levelText = computed(() => props.data?.eventLevelName ? `${props.data?.eventLevelName}级` : '-')
const priorityText = computed(() => props.data?.eventPriorityName?.toLocaleUpperCase() || '-')
const handlerText = computed(() => props.data?.handleUser?.userName || '-')

const levelColor = computed(() => getTagColor(props.data?.eventLevel || '', EVENT_LEVEL_TO_PRIORITY_KEY))
const priorityColor = computed(() => getTagColor(props.data?.eventPriority || '', EVENT_PRIORITY_LEVEL_COLORS))

const infoList = computed(() => {

  const d = props.data || {}

  return [
    {
      label: '主题分类',
      value: d.subjectCategoryName || '-'
    },
    {
      label: '触发时间',
      value: d.warningTime || '-'
    },
    {
      label: '主责部门',
      value: d.mainRespOrgName || '-'
    },
    {
      label: '敏感类型',
      value: d.sensitiveType || '-'
    },
    {
      label: '事件清晰度',
      value: d.eventClarity || '-'
    }
  ]
})

const clickTip = (tag?: string, mapping: Record<string, string> = {}) => {
  if (!tag) return
  const tip = mapping[tag]
  if (!tip) return
  showToast(tip)
}
</script>

<template>
  <div class="event-base-info">
    <!-- 暂无数据占位 -->
    <div v-if="!props.data" class="empty-text">
      暂无数据
    </div>

    <div v-else>
      <!-- 顶部摘要卡：事件等级 / 处理优先级 / 处理人员 -->
      <div class="summary-card">
        <div class="summary-item">
          <div class="summary-label" @click="clickTip(props.data?.eventLevel, EventLevelTipMap)">
            <span>事件等级</span>
            <img class="tips-icon" :src="tips2Png" alt="" />
          </div>
          <div class="summary-value" :style="{ color: levelColor }">
            {{ levelText }}
          </div>
        </div>

        <div class="summary-divider"></div>

        <div class="summary-item">
          <div class="summary-label" @click="clickTip(props.data?.eventPriority, EventPriorityTipMap)">
            <span>处理优先级</span>
            <img class="tips-icon" :src="tips2Png" alt="" />
          </div>
          <div class="summary-value" :style="{ color: priorityColor }">
            {{ priorityText }}
          </div>
        </div>

        <div class="summary-divider"></div>

        <div class="summary-item">
          <div class="summary-label">
            <span>处理人员</span>
          </div>
          <div class="summary-value">
            {{ handlerText }}
          </div>
        </div>
      </div>

      <!-- 基础信息列表 -->
      <div class="info-list mt-12">
        <div v-for="item in infoList" :key="item.label" class="info-row">
          <div class="label">{{ item.label }}</div>
          <div class="value">{{ item.value }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.event-base-info {
  font-size: 14px;
  color: #1f2733;
}

.empty-text {
  padding: 8px 0;
  font-size: 12px;
  color: #929aa6;
}

.placeholder-row {
  padding: 4px 0;
}

/* 顶部摘要卡 */
.summary-card {
  display: flex;
  align-items: center;
  padding: 12px 8px;
  background: #F5F7FA;
  border-radius: 8px 8px 8px 8px;
}

.summary-item {
  flex: 1;
  text-align: center;
}

.summary-label {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  font-weight: 400;
  font-size: 12px;
  color: #929AA6;
}

.tips-icon {
  width: 12px;
  height: 12px;
  object-fit: cover;
}

.summary-value {
  margin-top: 4px;
  font-weight: 500;
  font-size: 12px;
  line-height: 22px;
}

.summary-divider {
  width: 1px;
  height: 36px;
  background: #e5e6eb;
}

/* 基础信息列表 */
.info-list {
  display: flex;
  flex-direction: column;
  row-gap: 10px;
}

.info-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  column-gap: 8px;

  .label {
    min-width: 65px;
    text-align: right;
    font-weight: 400;
    font-size: 12px;
    color: rgba(0, 0, 0, 0.4);
    line-height: 22px;
  }

  .value {
    flex: 1 1 auto;
    font-weight: 400;
    font-size: 12px;
    color: rgba(0, 0, 0, 0.9);
    line-height: 22px;
    word-break: break-all;
  }
}

.section-title {
  font-size: 13px;
  font-weight: 500;
  color: #1f2733;
}

.description-box {
  padding: 8px 10px;
  background: #f5f6f8;
  border-radius: 4px;
  font-size: 13px;
  line-height: 20px;
  color: #4e5969;
}
</style>


