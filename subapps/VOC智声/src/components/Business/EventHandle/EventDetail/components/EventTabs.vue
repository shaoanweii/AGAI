<script setup lang="ts">
import { computed, useTemplateRef } from 'vue'
import ETabsGroup from './ETabsGroup.vue'
import Steps from './Steps.vue'
import EventNumber from './EventNumber.vue'
import EventHandle from './EventHandle.vue'
import OperationLog from './OperationLog.vue'
import { numberToChinese } from '@/utils'
import {
  EventType,
  taskStatusMap,
  VALID_EVENT,
  eventPriorityTipMap,
  eventLevelTipMap
} from '../../ehConstants'
import type { SingleEventDetailVo } from '@/api/singlePointEvent/types'
import EventPriorityTag from '@/views/customerDirectEngage/singlePointEvent/components/EventPriorityTag.vue'

defineOptions({
  name: 'EventTabs'
})

const { row, eventType, detailEvents, originalSoundDetails } = defineProps<{
  row: any
  eventType: EventType
  detailEvents: SingleEventDetailVo[]
  originalSoundDetails: any
}>()

const eventHandleRef = useTemplateRef('eventHandleRef')
// const activeEvent = ref('1')
const activeEvent = defineModel<string>()
// numberToChinese

const tabsOptions = computed(() => {
  return detailEvents.map((item, index) => {
    return {
      label: `事件${numberToChinese(index + 1)}`,
      value: String(index + 1)
    }
  })
})

// 事件详情
const curEventDetail = computed(() => {
  const index = Number(activeEvent.value) - 1
  return detailEvents[index] || {}
})

// 当前tab的事件详情
const getCurrentEventDetail = () => {
  return curEventDetail.value as any
}

const handleFormData = () => {
  return eventHandleRef.value?.handleFormData()
}

// 显示事件处理
const showEvemtHandle = computed(() => {
  // taskStatusMap
  if (taskStatusMap.close.includes(curEventDetail.value.taskStatus!)) {
    if (curEventDetail.value.eventValidity === VALID_EVENT) {
      return true
    } else {
      return false
    }
  } else {
    return true
  }
})

defineExpose({
  getCurrentEventDetail,
  activeEvent,
  handleFormData
})
</script>

<template>
  <div class="event-tabs">
    <!-- 当只有单一事件时，不显示事件TAB -->
    <ETabsGroup v-if="tabsOptions.length > 1" v-model="activeEvent" :options="tabsOptions" />
    <!-- <template v-for="(eventItem, index) in detailEvents" :key="index">  -->
    <div
      class="et-content"
      :style="{ borderTopLeftRadius: tabsOptions.length > 1 ? '0px' : '8px' }"
    >
      <!-- 事件编号 -->
      <div class="etc-title">
        <span>事件编号-{{ curEventDetail.warningEventNo }}</span>
        <EventPriorityTag
          v-if="curEventDetail.eventPriority"
          :tagName="curEventDetail.eventPriorityName?.toLocaleUpperCase()!"
          :type="curEventDetail.eventPriority!"
          :tooltip="
            eventPriorityTipMap[curEventDetail.eventPriority as keyof typeof eventPriorityTipMap]
          "
          class="ml-8"
        ></EventPriorityTag>
        <!-- <el-tag type="danger" class="ml-8">P0</el-tag> -->
        <EventPriorityTag
          v-if="curEventDetail.eventLevel"
          :tagName="curEventDetail.eventLevelName!"
          :type="curEventDetail.eventLevel"
          :tooltip="eventLevelTipMap[curEventDetail.eventLevel as keyof typeof eventLevelTipMap]"
          class="ml-8"
        ></EventPriorityTag>
        <!-- <el-tag type="danger" class="ml-8">S级</el-tag> -->
        <el-tag v-if="curEventDetail.sensitiveType" type="primary" class="ml-8">{{
          curEventDetail.sensitiveType || ''
        }}</el-tag>
        <el-tag v-if="curEventDetail.eventClarity" type="primary" class="ml-8">{{
          curEventDetail.eventClarity || ''
        }}</el-tag>
      </div>
      <!-- 事件编号 -->
      <EventNumber :eventInfo="curEventDetail"></EventNumber>

      <div class="divid-line mx-16"></div>

      <!-- 事件进度 -->
      <div class="etc-title mt-24">
        <span>事件进度</span>
      </div>
      <!-- 步骤条 -->
      <Steps
        v-if="curEventDetail.taskStatus"
        :task-status="curEventDetail.taskStatus"
        :eventInfo="curEventDetail"
        class="mt-24"
      />

      <div class="divid-line mx-16"></div>

      <!-- 事件处理 -->
      <!-- v-if="eventType !== EventType.VIEW" -->
      <template v-if="showEvemtHandle">
        <div class="etc-title mt-24 mb-16">
          <span>事件处理</span>
        </div>
        <EventHandle
          ref="eventHandleRef"
          :row="row"
          :key="curEventDetail.id"
          :eventType="eventType"
          :eventInfo="curEventDetail"
          :originalSoundDetails="originalSoundDetails"
        ></EventHandle>

        <div class="divid-line mx-16"></div>
      </template>

      <div class="etc-title mt-24 mb-16">
        <span>操作记录</span>
      </div>
      <OperationLog :eventInfo="curEventDetail"></OperationLog>
    </div>
    <!-- </template> -->
  </div>
</template>

<style lang="scss" scoped>
.event-tabs {
  .et-content {
    border-radius: 0px 8px 8px 8px;
    border: 1px solid #ebedf0;
    padding: 24px;

    .etc-title {
      position: relative;
      padding-left: 10px;
      font-weight: 500;
      font-size: 16px;
      color: rgba(0, 0, 0, 0.9);
      line-height: 24px;
      display: flex;
      align-items: center;

      &::after {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 2px;
        height: 15px;
        background: #1677ff;
      }
    }
  }
}
</style>
