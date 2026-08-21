<script setup lang="ts">
import { computed } from 'vue'
import { showDialog } from 'vant'
import type { EventRateCardProps, EventRateCardEmits, EventRateData } from './types'
import { fmtNum, fmtPer, fmtFix } from '@/utils'

// 定义组件选项
defineOptions({
  name: 'SingleEventRateCard'
})

// 定义Props
const props = withDefaults(defineProps<EventRateCardProps>(), {
  loading: false
})

// 定义Emits
const emit = defineEmits<EventRateCardEmits>()

// 默认数据
const defaultData: EventRateData = {
  currentCounts: 0,
  lastCounts: 0,
  closeRate: 0,
  ringRate: 0
}

// 计算属性
const cardData = computed<EventRateData>(() => ({
  ...defaultData,
  ...(props.data || {})
}))

/**
 * 卡片整体点击（保留原有行为，供外层跳转使用）
 */
const handleCardClick = () => {
  if (props.loading) return
  emit('click', cardData.value)
}

/**
 * “新增事件”文案点击
 * - 仅弹出说明提示，不影响卡片原有点击行为
 */
const handleNewEventTipClick = (event: Event) => {
  // 阻止冒泡，避免触发卡片整体点击
  event.stopPropagation()
  if (props.loading) return

  showDialog({
    title: '新增事件',
    message: '仅展示筛选日期范围内触发的预警事件，不包含历史未关闭事件。',
    theme: 'round-button',
    confirmButtonColor: '#165DFF',
    confirmButtonText: '我知道了'
  })
}
</script>

<template>
  <div
    class="event-rate-card flex-center pl-12 pr-12"
    :class="{
      'is-loading': loading
    }"
    @click="handleCardClick"
  >
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <van-loading size="24px" color="#1677FF">加载中...</van-loading>
    </div>

    <!-- 正常状态 -->
    <div v-else class="card-content flex-1 flex-between flex-y-center">
      <div>
        <div class="flex-y-center">
          <span class="fs-20 fw-500">{{ fmtNum(cardData.currentCounts) }}</span>
        </div>
        <div class="fs-12 fw-400 mt-7 flex-y-center new-event-tip" @click="handleNewEventTipClick">
          <span>新增事件</span>
          <SvgIcon name="h5-tips" width="12px" height="12px" color="#fff" class="ml-5" />
        </div>
      </div>
      <div class="mt-10">
        <div class="flex-y-center">
          <div class="fs-12 fw-400 text-right w-40 title-text-class">环比</div>
          <div class="flex-1 ml-6 text-right right-data-class">
            {{ fmtFix(cardData.ringRate) }}
          </div>
        </div>
        <!-- 标题区域 -->
        <div class="flex-y-center mt-8">
          <div class="fs-12 fw-400 text-right w-40 title-text-class">闭环率</div>
          <div class="ml-6 text-right right-data-class">{{ fmtPer(cardData.closeRate) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>
<style scoped lang="scss">
::v-deep(.van-dialog--round-button .van-action-bar-button--last) {
  border-radius: 8px;
}

.event-rate-card {
  height: 73px;
  background: linear-gradient(180deg, #0daeff 0%, #1677ff 100%);
  border-radius: 0px 20px 0px 8px;
  color: #fff;

  .change-rate {
    min-width: 55px;
    height: 22px;
    padding: 0 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #fafafa;
    border-radius: 16px 16px 16px 16px;
    border: 1px solid #e9eaeb;
    font-weight: 500;
    font-size: 10px;
    color: #5f6a7a;
  }

  .w-40 {
    width: 40px;
  }

  .right-data-class {
    min-width: 64px;
    color: rgba(255, 255, 255, 0.85);
  }

  .title-text-class {
    color: rgba(255, 255, 255, 0.65);
  }

  .text-right {
    text-align: right;
  }

  .new-event-tip {
    cursor: pointer;
  }
}
</style>
