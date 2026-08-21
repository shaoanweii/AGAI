<script setup lang="ts">
import BrandMentionTrend from './BrandMentionTrend.vue'
import type { ProductExperienceIndexVo } from '@/api/overview/type'
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { fmtNum, fmtPer, fmtFix } from '@/utils'
const fallbackBrandMark = '/demo-assets/brands/zhixing.png'

defineOptions({
  name: 'BrandView'
})

// Props 定义
interface Props {
  /** 品牌简报数据 */
  data?: ProductExperienceIndexVo[]
  /** 加载状态 */
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  data: () => [],
  loading: false
})

const emits = defineEmits<{
  (e: 'brandChange', brandItem: any): void
}>()

const brandChange = (brandItem: any) => {
  emits('brandChange', brandItem)
}

/**
 * 品牌未配置图片或图片加载失败时，使用中性品牌标志避免破图。
 */
const handleBrandImageError = (event: Event) => {
  const image = event.currentTarget as HTMLImageElement
  if (image.src !== fallbackBrandMark) image.src = fallbackBrandMark
}

// 滚动容器引用
const scrollContainer = ref<HTMLElement>()
const brandViewRef = ref<HTMLElement>()
const scrollLeft = ref(0)
const maxScrollLeft = ref(0)
const resizeObserver = ref<ResizeObserver>()

const CARD_GAP = 16
const ACTIVE_CARD_MIN_WIDTH = 211
const FALLBACK_SCROLL_STEP = ACTIVE_CARD_MIN_WIDTH + CARD_GAP

// 计算是否可以向左/右滚动
const canScrollLeft = computed(() => scrollLeft.value > 1) // 容差值为1px
const canScrollRight = computed(() => scrollLeft.value < maxScrollLeft.value - 1)

/**
 * 同步滚动容器当前位置与最大可滚动距离。
 * 这样在品牌数量变化、容器宽度变化时，箭头显隐也能实时更新。
 */
const syncScrollState = () => {
  if (!scrollContainer.value) {
    scrollLeft.value = 0
    maxScrollLeft.value = 0
    return
  }

  scrollLeft.value = scrollContainer.value.scrollLeft
  maxScrollLeft.value = Math.max(
    scrollContainer.value.scrollWidth - scrollContainer.value.clientWidth,
    0
  )
}

/**
 * 获取品牌卡片之间的实际间距。
 * 优先读取容器真实样式，避免 JS 与 CSS 常量脱节。
 */
const getCardGap = () => {
  if (!brandViewRef.value) return CARD_GAP

  const styles = window.getComputedStyle(brandViewRef.value)
  const gapValue = parseFloat(styles.columnGap || styles.gap || `${CARD_GAP}`)

  return Number.isFinite(gapValue) ? gapValue : CARD_GAP
}

/**
 * 根据当前首张卡片的实际宽度计算滚动步长。
 * 当品牌少时卡片会自动拉宽，此处同步按真实宽度滚动。
 */
const getScrollStep = () => {
  const firstCard = brandViewRef.value?.querySelector<HTMLElement>('.bv-item')
  if (!firstCard) return FALLBACK_SCROLL_STEP

  return firstCard.offsetWidth + getCardGap()
}

/**
 * 监听滚动区域尺寸变化，确保布局伸缩后箭头状态正确。
 */
const observeScrollElements = () => {
  resizeObserver.value?.disconnect()

  if (!scrollContainer.value || typeof ResizeObserver === 'undefined') return

  resizeObserver.value = new ResizeObserver(() => {
    syncScrollState()
  })

  resizeObserver.value.observe(scrollContainer.value)

  if (brandViewRef.value) {
    resizeObserver.value.observe(brandViewRef.value)
  }
}

// 向左滚动
const scrollLeftHandler = (event: MouseEvent) => {
  event.stopPropagation()
  event.preventDefault()

  // 如果不能向左滚动，直接返回（但仍然阻止事件传播）
  if (!canScrollLeft.value) return
  if (!scrollContainer.value) return

  // 计算新的滚动位置，确保不会滚动到负值
  const newScrollLeft = Math.max(0, scrollLeft.value - getScrollStep())
  scrollContainer.value.scrollTo({
    left: newScrollLeft,
    behavior: 'smooth'
  })
}

// 向右滚动
const scrollRightHandler = (event: MouseEvent) => {
  event.stopPropagation()
  event.preventDefault()

  // 如果不能向右滚动，直接返回（但仍然阻止事件传播）
  if (!canScrollRight.value) return
  if (!scrollContainer.value) return

  const newScrollLeft = Math.min(maxScrollLeft.value, scrollLeft.value + getScrollStep())
  scrollContainer.value.scrollTo({
    left: newScrollLeft,
    behavior: 'smooth'
  })
}

// 监听滚动事件，更新滚动位置
const handleScroll = () => {
  syncScrollState()
}

watch(
  () => props.data?.length ?? 0,
  async () => {
    await nextTick()
    syncScrollState()
    observeScrollElements()
  }
)

onMounted(async () => {
  await nextTick()
  syncScrollState()
  observeScrollElements()
})

onBeforeUnmount(() => {
  resizeObserver.value?.disconnect()
})
</script>

<template>
  <div class="brand-layout w-full">
    <template v-if="canScrollLeft || canScrollRight">
      <div
        class="arrow left"
        :class="{ disabled: !canScrollLeft }"
        @click.prevent.stop="scrollLeftHandler"
      >
        <el-icon color="#999999"><ArrowLeftBold /></el-icon>
      </div>
      <div
        class="arrow right"
        :class="{ disabled: !canScrollRight }"
        @click.prevent.stop="scrollRightHandler"
      >
        <el-icon color="#999999"><ArrowRightBold /></el-icon>
      </div>
    </template>
    <!-- 加载状态 -->
    <div v-if="props.loading" class="loading-container">
      <el-skeleton :rows="3" animated />
    </div>

    <!-- 数据展示 -->
    <template v-else-if="props.data && props.data.length > 0">
      <div ref="scrollContainer" class="scroll-container" @scroll="handleScroll">
        <div ref="brandViewRef" class="brand-view">
          <div
            v-for="(item, index) in props.data"
            :key="item.brandCode || index"
            class="bv-item cursor-point"
            :class="{ default: index !== 0, active: index === 0 }"
            @click="brandChange(item)"
          >
            <div class="flex-between">
              <div class="flex-y-center">
                <div class="bvi-logo">
                  <img
                    :src="item.imgUrl || fallbackBrandMark"
                    class="brand-mark w-full h-full"
                    :alt="item.name"
                    @error="handleBrandImageError"
                  />
                </div>
                <div class="bvi-title font-600 text-h4 ml-8">{{ item.name || '未知品牌' }}</div>
              </div>
              <div class="bvi-link flex-center cursor-point">
                <!-- <el-icon :size="14"><TopRight /></el-icon> -->
                <img src="@/assets/images/arrow-up-right.png" alt="" class="w-14 h-14" />
              </div>
            </div>

            <div class="text-body text-secondary mt-16">负面率</div>
            <div class="flex-y-center">
              <div class="text-h3" :style="{ color: item.rateColor }">
                {{ fmtPer(item.negativeRate) }}
              </div>
              <div class="ml-6 tag-def" style="color: #5f6a7a">
                {{ fmtFix(item.growth) }}
              </div>
            </div>

            <div class="flex mt-16">
              <div class="mr-6 w-60">
                <div class="text-body text-tertiary">提及量</div>
                <div class="text-h4 font-600">{{ fmtNum(item.mentionCount) }}</div>
              </div>
              <BrandMentionTrend :trend-data="item.growthTrend"></BrandMentionTrend>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- 空状态 -->
    <div v-else class="empty-container">
      <el-empty description="暂无品牌数据" />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.brand-layout {
  position: relative;
  .loading-container,
  .empty-container {
    width: 100%;
    min-height: 200px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .arrow {
    width: 40px;
    height: 40px;
    background: #f2f3f5;
    border-radius: 20px;
    border: 1px solid #ffffff;
    position: absolute;
    display: flex;
    justify-content: center;
    align-items: center;
    cursor: pointer;
    z-index: 999;
    transition: all 0.3s ease;

    &:hover:not(.disabled) {
      background: #e6f7ff;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }

    &.disabled {
      opacity: 0.5;
      cursor: not-allowed;
      // 移除 pointer-events: none，改为在 JS 中处理
    }

    &.left {
      top: 50%;
      left: 0;
      transform: translateY(-50%);
    }

    &.right {
      top: 50%;
      right: 0;
      transform: translateY(-50%);
    }
  }
  .scroll-container {
    width: 100%;
    height: 100%;
    overflow-x: auto;
    overflow-y: hidden;
    // 隐藏滚动条
    &::-webkit-scrollbar {
      display: none;
    }
    -ms-overflow-style: none;
    scrollbar-width: none;
  }
  .brand-view {
    display: flex;
    align-items: center;
    gap: 16px;
    width: max-content;
    min-width: 100%;

    .bv-item {
      &.default {
        flex: 1 0 193px;
        min-width: 193px;
        height: 184px;
      }
      &.active {
        flex: 1 0 211px;
        min-width: 211px;
        height: 204px;
      }
      box-sizing: border-box;
      padding: 16px;
      background: rgba(255, 255, 255, 0.8);
      box-shadow: 0px 1px 1px 0px rgba(10, 13, 18, 0.05);
      border-radius: 8px 8px 8px 8px;

      .bvi-logo {
        width: 28px;
        height: 28px;
        // background: #0b457f;
        // border-radius: 5px 5px 5px 5px;
        img {
          object-fit: contain;
          border-radius: 6px;
        }
      }
      .bvi-title {
        color: #1d252f;
      }
      .bvi-link {
        width: 28px;
        height: 28px;
        background: #f2f3f5;
        border-radius: 4px;
        border: 1px solid rgba(255, 255, 255, 0.5);
      }
    }
  }
}
</style>
