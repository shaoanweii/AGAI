<script setup lang="ts">
import { type H5VocBaseRequest } from '@h5/api/home'
import type { NegativeRateData } from '@h5/views/home/components/NegativeRateCard/types.d.ts'
import type { DateOption } from '@h5/views/home/components/HDateFilter/types'
import noticePng from '@/assets/h5/notice.png'
import arrowRightPng from '@/assets/h5/arrow-right.png'
import { useRouter } from 'vue-router'
import { usePermissionsStore, useShareStore } from '@h5/store'
import { invokeShareAppMessage } from '@h5/utils/weWork'
import { ref, onMounted, watch, nextTick } from 'vue'
import { isWeWorkEnvironment } from '@/utils/environment'

defineOptions({
  name: 'BrowseSummary'
})

const router = useRouter()
const userPermStore = usePermissionsStore()
const shareStore = useShareStore()

const props = defineProps<{
  /** 数据简报，用于展示浏览任务达成情况 */
  dataBrief?: NegativeRateData
  /** 请求参数（品牌、时间、公私域等），用于请求数据简报 */
  requestParams?: H5VocBaseRequest
  /** 当前时间筛选状态（用于分享时携带时间参数） */
  currentDateFilter?: {
    dateUnit?: number
    dateTime?: DateOption | null
  }
  /** 数据源（仅首页需要，用于分享时携带数据源参数） */
  channelCatagory?: string
}>()

const handleClick = () => {
  if (!userPermStore.executivePermission) {
    return
  }
  const requestParams = {
    ...props.requestParams
  }
  router.push({
    name: 'H5TaskDetail',
    query: {
      // 传递当前筛选参数
      ...requestParams
    }
  })
}

const handleShare = () => {
  // 提取分享参数
  const shareParams: {
    brandCode?: string
    dateUnit?: number
    dateTime?: {
      code?: number
      startTime?: string
      endTime?: string
      name?: string
    }
    channelCatagory?: string
  } = {}

  // 品牌编码
  if (props.requestParams?.brandCode) {
    shareParams.brandCode = props.requestParams.brandCode
  }

  // 时间筛选参数
  if (props.currentDateFilter?.dateUnit !== undefined) {
    shareParams.dateUnit = props.currentDateFilter.dateUnit
  }
  if (props.currentDateFilter?.dateTime) {
    shareParams.dateTime = {
      code: props.currentDateFilter.dateTime.code,
      startTime: props.currentDateFilter.dateTime.startTime,
      endTime: props.currentDateFilter.dateTime.endTime,
      name: props.currentDateFilter.dateTime.name
    }
  }

  // 数据源（仅首页需要）
  if (props.channelCatagory !== undefined) {
    shareParams.channelCatagory = props.channelCatagory
  }

  // 存储分享参数到 store
  shareStore.setShareParams(shareParams)

  // 调用分享
  invokeShareAppMessage(shareStore.shareTitle, shareStore.shareDesc)
}

const isWeWork = isWeWorkEnvironment()
// const isWeWork = true

// 跑马灯相关
const progressTextContainerRef = ref<HTMLElement>()
const progressTextRef = ref<HTMLElement>()
const progressTextWrapperRef = ref<HTMLElement>()
const needsMarquee = ref(false)

// 计算是否需要跑马灯效果
const checkMarquee = () => {
  // 使用 requestAnimationFrame 确保 DOM 完全渲染后再计算
  requestAnimationFrame(() => {
    if (!progressTextRef.value || !progressTextWrapperRef.value) {
      needsMarquee.value = false
      return
    }

    const textWidth = progressTextRef.value.scrollWidth
    const containerWidth = progressTextWrapperRef.value.clientWidth

    // 只有当文本宽度大于容器宽度时才需要跑马灯
    if (textWidth > containerWidth && textWidth > 0) {
      needsMarquee.value = true
      // 根据文本宽度动态计算动画时长，保持恒定的滚动速度（50px/秒）
      // 移动距离是文本宽度（因为有两个重复的文本，移动第一个文本的宽度）
      const speed = 50 // 像素/秒
      const duration = Math.max(10, textWidth / speed) // 最少10秒，确保不会太快

      // 设置CSS变量
      if (progressTextContainerRef.value) {
        progressTextContainerRef.value.style.setProperty('--marquee-duration', `${duration}s`)
        progressTextContainerRef.value.style.setProperty('--text-width', `${textWidth}px`)
      }
    } else {
      needsMarquee.value = false
      // 清除CSS变量
      if (progressTextContainerRef.value) {
        progressTextContainerRef.value.style.removeProperty('--marquee-duration')
        progressTextContainerRef.value.style.removeProperty('--text-width')
      }
    }
  })
}

onMounted(() => {
  // 等待 DOM 渲染完成后再检查
  nextTick(() => {
    checkMarquee()
  })
})

// 监听文本内容变化
watch(
  () => props.dataBrief?.achieveRateTalk,
  () => {
    // 等待 DOM 更新完成后再检查
    nextTick(() => {
      checkMarquee()
    })
  },
  { immediate: false }
)
</script>

<template>
  <!-- 企微环境：使用完整的 browse-summary-container 结构 -->
  <div v-if="isWeWork" class="browse-summary-container">
    <div class="content">
      <div class="browse-layout flex-center" @click="handleClick">
        <van-image
          v-show="dataBrief?.achieveRateTalk"
          width="24"
          height="24"
          :src="noticePng"
          fit="cover"
          class="icon-spacing"
        />
        <div ref="progressTextWrapperRef" class="flex-1 pl-12 pr-11 progress-text-wrapper">
          <!-- 使用跑马灯效果（两个重复的文本） -->
          <div
            ref="progressTextContainerRef"
            class="progress-text-container"
            :class="{ 'marquee-active': needsMarquee }"
          >
            <div
              ref="progressTextRef"
              class="progress-text"
              v-html="dataBrief?.achieveRateTalk || ''"
            ></div>
            <div
              v-if="needsMarquee"
              class="progress-text"
              v-html="dataBrief?.achieveRateTalk || ''"
            ></div>
          </div>
        </div>
        <template v-if="userPermStore.executivePermission">
          <van-image
            v-show="dataBrief?.achieveRateTalk"
            width="24"
            height="24"
            :src="arrowRightPng"
            fit="cover"
          />
        </template>
      </div>
    </div>
    <div class="icon" @click="handleShare">
      <img src="@/assets/h5/share-fill.png" />
    </div>
  </div>
  <!-- 其他环境：只展示 browse-layout -->
  <div v-else class="browse-layout flex-center" @click="handleClick">
    <van-image
      v-show="dataBrief?.achieveRateTalk"
      width="24"
      height="24"
      :src="noticePng"
      fit="cover"
    />
    <div ref="progressTextWrapperRef" class="flex-1 pl-12 pr-11 progress-text-wrapper">
      <!-- 默认环境同样使用跑马灯（仅文本溢出时启用） -->
      <div
        ref="progressTextContainerRef"
        class="progress-text-container"
        :class="{ 'marquee-active': needsMarquee }"
      >
        <div
          ref="progressTextRef"
          class="progress-text"
          v-html="dataBrief?.achieveRateTalk || ''"
        ></div>
        <div
          v-if="needsMarquee"
          class="progress-text"
          v-html="dataBrief?.achieveRateTalk || ''"
        ></div>
      </div>
    </div>
    <template v-if="userPermStore.executivePermission">
      <van-image
        v-show="dataBrief?.achieveRateTalk"
        width="24"
        height="24"
        :src="arrowRightPng"
        fit="cover"
      />
    </template>
  </div>
</template>

<style scoped lang="scss">
.browse-summary-container {
  // height: 74px;
  width: 100%;
  background: #ffffff;
  padding: 10px 20px 0;

  display: flex;
  gap: 16px;

  .content {
    flex: 1;
    height: 44px;
    min-width: 0; // 确保 flex 子元素可以正确收缩
  }

  .icon {
    flex: none;
    width: 44px;
    height: 44px;
    background: #e2f3fe;
    border-radius: 8px 8px 8px 8px;
    display: flex;
    justify-content: center;
    align-items: center;

    img {
      width: 24px;
      height: 24px;
    }
  }
}
.browse-layout {
  padding: 10px 16px;
  background: #eaf3ff;
  min-width: 0; // 确保 flex 容器可以正确收缩
  height: 44px;

  // 企微环境才使用圆角
  .browse-summary-container & {
    border-radius: 8px 8px 8px 8px;
    background: #f5f7fa;
  }

  .icon-spacing {
    margin-right: 8px;
  }

  .progress-text-wrapper {
    overflow: hidden;
    min-width: 0; // 确保 flex 子元素可以正确收缩
    position: relative;
    // 明确设置 padding，确保 pl-12 和 pr-11 生效
    padding-left: 12px;
    padding-right: 11px;
  }

  .progress-text-container {
    display: inline-flex;
    white-space: nowrap;
    // 性能优化：提前告知浏览器该元素会发生变化
    will-change: transform;

    // 默认不启用动画
    animation: none;

    // 只有在需要时才启用动画
    &.marquee-active {
      animation: marquee var(--marquee-duration, 15s) linear infinite;
    }
  }

  .progress-text {
    line-height: 20px;
    font-weight: 400;
    font-size: 14px;
    color: #1f2733;
    white-space: nowrap;
    display: inline-block;
    flex-shrink: 0;
    // 两段重复文本之间保留固定间距，避免滚动衔接过于紧凑
    padding-right: 30px;

    ::v-deep(.highlight) {
      padding-left: 2px;
      padding-right: 2px;
      font-weight: 400;
      color: #1677ff;
      font-size: 14px;
    }
  }

  .task-text {
    line-height: 20px;
    font-weight: 400;
    font-size: 14px;
    color: #1f2733;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    min-width: 0;
  }

  @keyframes marquee {
    0% {
      // 从初始位置开始
      transform: translateX(0);
    }
    100% {
      // 移动第一个文本的宽度，让第一个文本移出，第二个文本正好接上
      // 由于两个文本相同，移动第一个文本的宽度正好让第二个文本到达第一个文本的初始位置
      transform: translateX(calc(-1 * var(--text-width, 50%)));
    }
  }
}
</style>
