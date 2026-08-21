<script setup lang="ts">
import { computed, unref, useSlots } from 'vue'
import { useRoute } from 'vue-router'
import DownloadMoreAction from '@/components/Business/DownloadMoreAction/index.vue'
import { useCardDownloadContext } from '@/hooks/useCardDownload'

defineOptions({
  name: 'FCard'
})

const slots = useSlots()
const route = useRoute()
const cardDownloadContext = useCardDownloadContext()

const {
  title = '标题',
  width = '100%',
  height = '100%',
  isShowMore = false,
  tooltip = '',
  titleSize = 'default',
  cardKey = '',
  downloadable = false
} = defineProps<{
  title?: string
  /** 卡片稳定业务标识，用于下载接口映射 */
  cardKey?: string
  /** 是否展示卡片下载入口，避免页面级下载上下文影响内部嵌套卡片 */
  downloadable?: boolean
  width?: string
  height?: string
  isShowMore?: boolean
  tooltip?: string
  titleSize?: 'default' | 'small' | 'middle'
  // moda? 'default' | 'small'
  // 自定义头部样式
  headerClass?: string
  // 自定义头部左侧样式
  leftExtraClass?: string
  // 自定义头部右侧样式
  rightExtraClass?: string
}>()

const emits = defineEmits(['handleMore'])

const handleMore = () => {
  emits('handleMore')
}

const showDownloadAction = computed(
  () => downloadable && Boolean(unref(cardDownloadContext?.enabled ?? false))
)
const exportMenu = computed(() => {
  const menuTitle = String(route.meta?.title || '').trim()
  const cardTitle = String(title || '').trim()
  return [menuTitle, cardTitle].filter(Boolean).join('-')
})

const handleDownloadStat = () => {
  if (!cardDownloadContext?.onDownloadStat) return
  void cardDownloadContext.onDownloadStat({
    cardKey,
    exportMenu: exportMenu.value
  })
}

const handleDownloadDetail = () => {
  if (!cardDownloadContext?.onDownloadDetail) return
  void cardDownloadContext.onDownloadDetail({
    cardKey,
    exportMenu: exportMenu.value
  })
}
</script>

<template>
  <div class="f-card" :style="{ width, height }">
    <div class="fc-header" :class="[headerClass]">
      <div class="fch-left" :class="[leftExtraClass]">
        <slot name="title">
          <span v-if="titleSize === 'default'" class="text-h2 mr-8 font-600">{{ title }}</span>
          <span v-if="titleSize === 'small'" class="text-h4 mr-8 font-600">{{ title }}</span>
          <span v-if="titleSize === 'middle'" class="text-h3 mr-8 font-600">{{ title }}</span>
          <!-- <span class="text-h3 mr-8 font-600">{{ title }}</span> -->
        </slot>
        <el-tooltip
          v-if="tooltip"
          :content="tooltip"
          placement="top"
          popper-class="text-tooltip-light"
        >
          <span class="flex-center">
            <SvgIcon name="question-mark" width="20px" height="20px"></SvgIcon>
          </span>
        </el-tooltip>

        <slot name="leftExtra"> </slot>
      </div>
      <div v-if="isShowMore || slots.more || showDownloadAction" class="fch-right">
        <div v-if="isShowMore || slots.more" class="fch-right__more" @click="handleMore">
          <slot name="more">
            <span class="text-body text-secondary">查看更多</span>
            <el-icon :size="16" color="#929AA6"><ArrowRightBold /></el-icon>
          </slot>
        </div>
        <DownloadMoreAction
          v-if="showDownloadAction"
          :loading="Boolean(unref(cardDownloadContext?.loading))"
          :show-stat="Boolean(unref(cardDownloadContext?.showStat ?? true))"
          :show-detail="Boolean(unref(cardDownloadContext?.showDetail ?? true))"
          @download-stat="handleDownloadStat"
          @download-detail="handleDownloadDetail"
        />
      </div>
    </div>
    <div
      class="fc-body"
      data-page-export-card-body
      :style="{ height: height === 'auto' ? 'auto' : `calc(${height} - 48px)`, width }"
    >
      <slot></slot>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.f-card {
  background: rgba(255, 255, 255, 0.8);
  box-shadow: 0px 1px 2px 0px rgba(10, 13, 18, 0.05);
  border-radius: 12px;
  .fc-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 24px 0;
    .fch-left {
      flex: 1;
      display: flex;
      align-items: center;
    }
    .fch-right {
      display: flex;
      align-items: center;
      gap: 8px;

      .fch-right__more {
        display: inline-flex;
        align-items: center;
        cursor: pointer;
      }
    }
  }
  .fc-body {
    padding: 24px;
  }
}
</style>
