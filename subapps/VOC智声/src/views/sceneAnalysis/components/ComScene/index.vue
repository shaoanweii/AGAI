<script setup lang="ts">
import { ref, computed } from 'vue'
import type { HomeMenuVo } from '@/api/overview/type'
import { useRouter } from 'vue-router'
import { recordMenuVisit } from '@/utils/operationLog'
import { useUserStore } from '@/store'

defineOptions({
  name: 'ComScene'
})

// 接收从父组件传递的数据和加载状态
interface Props {
  data?: HomeMenuVo[] | null
  loading?: boolean
}

const { data, loading = false } = defineProps<Props>()

// 滚动容器引用
const scrollContainer = ref<HTMLElement>()
const scrollLeft = ref(0)

// 每次滚动的距离（一个卡片的宽度 + 间距）
const scrollStep = 336 // 320px(卡片宽度) + 16px(间距)

// 计算是否可以向左/右滚动
const canScrollLeft = computed(() => scrollLeft.value > 0)
const canScrollRight = computed(() => {
  if (!scrollContainer.value || !data?.length) return false
  const maxScrollLeft = scrollContainer.value.scrollWidth - scrollContainer.value.clientWidth
  return scrollLeft.value < maxScrollLeft
})

// 向左滚动
const scrollLeftHandler = () => {
  if (!scrollContainer.value) return
  // 计算新的滚动位置，确保不会滚动到负值
  const newScrollLeft = Math.max(0, scrollLeft.value - scrollStep)
  scrollContainer.value.scrollTo({
    left: newScrollLeft,
    behavior: 'smooth'
  })
}

// 向右滚动
const scrollRightHandler = () => {
  if (!scrollContainer.value) return
  const maxScrollLeft = scrollContainer.value.scrollWidth - scrollContainer.value.clientWidth
  const newScrollLeft = Math.min(maxScrollLeft, scrollLeft.value + scrollStep)
  scrollContainer.value.scrollTo({
    left: newScrollLeft,
    behavior: 'smooth'
  })
}

// 监听滚动事件，更新滚动位置
const handleScroll = () => {
  if (scrollContainer.value) {
    scrollLeft.value = scrollContainer.value.scrollLeft
  }
}

const router = useRouter()
const userStore = useUserStore()

const sceneImageMap: Record<string, string> = {
  '/scene/groupAnalysis': '/demo-assets/scenes/scene-group-v2.png',
  '/scene/thisProductAnalysis': '/demo-assets/scenes/scene-own-product.png',
  '/scene/competitorAnalysis': '/demo-assets/scenes/scene-competitor.png',
  '/scene/journeyAnalysis': '/demo-assets/scenes/scene-journey.png',
  '/scene/productAnalysis': '/demo-assets/scenes/scene-product.png',
  '/scene/serviceAnalysis': '/demo-assets/scenes/scene-service.png',
  '/scene/newCarLaunch': '/demo-assets/scenes/scene-new-car.png',
  '/scene/mainAccount': '/demo-assets/scenes/scene-key-account.png',
  '/scene/hotEvents': '/demo-assets/scenes/scene-hot-event.png'
}

/** 使用接口图片与本地场景素材，避免场景卡片出现破图。 */
const resolveSceneImage = (item: HomeMenuVo) =>
  item.bigImage ||
  item.smallImage ||
  sceneImageMap[item.htmlUri || ''] ||
  sceneImageMap['/scene/groupAnalysis']

/**
 * 通用场景卡片点击跳转：补齐“操作记录”上报
 * - 复用现有 recordMenuVisit（失败不影响跳转）
 * - menuId：PC 端统一从 userStore 的 path->id 映射中取
 */
const reportSceneCardVisit = (targetPath: string, item?: HomeMenuVo) => {
  try {
    const resolved = router.resolve(targetPath)

    const frontRouting = resolved?.fullPath || targetPath || ''
    const lastMatched = resolved?.matched?.[resolved.matched.length - 1]
    const menuId = userStore.getMenuIdByPath(resolved?.path || frontRouting || targetPath) || ''
    const menuName = item?.name || (lastMatched?.meta as any)?.title || ''
    const visitUrl = `${window.location.origin}${window.location.pathname}${window.location.search}#${frontRouting}`

    recordMenuVisit({
      visitUrl,
      frontRouting,
      menuName,
      menuId
    }).catch(() => void 0)
  } catch (error) {
    console.warn('通用场景跳转操作记录失败:', error)
  }
}

const handleItem = (item: HomeMenuVo) => {
  const targetPath = item?.htmlUri || ''
  if (!targetPath) return

  reportSceneCardVisit(targetPath, item)

  router.push({
    path: targetPath,
    query: {
      isBack: '1'
    }
  })
}
</script>

<template>
  <div class="com-scene">
    <template v-if="canScrollLeft || canScrollRight">
      <div class="arrow left" :class="{ disabled: !canScrollLeft }" @click="scrollLeftHandler">
        <el-icon color="#999999"><ArrowLeftBold /></el-icon>
      </div>
      <div class="arrow right" :class="{ disabled: !canScrollRight }" @click="scrollRightHandler">
        <el-icon color="#999999"><ArrowRightBold /></el-icon>
      </div>
    </template>
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="3" animated />
    </div>

    <!-- 数据展示 -->
    <template v-else-if="data && data.length > 0">
      <div ref="scrollContainer" class="scroll-container" @scroll="handleScroll">
        <div class="content-wrapper">
          <div
            class="cs-item cursor-point"
            v-for="item in data"
            :key="item.name || item.htmlUri"
            @click="handleItem(item)"
          >
            <div class="cs-visual">
              <img :src="resolveSceneImage(item)" :alt="item.name" />
            </div>
            <div class="sci-content">
              <div class="text-h4 font-500 text-primary">{{ item.name }}</div>
              <div class="text-body text-secondary mt-16" style="white-space: wrap">
                {{ item.description }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- 无数据状态 -->
    <div v-else class="no-data">
      <el-empty description="暂无数据" />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.com-scene {
  width: 100%;
  height: 100%;
  position: relative;

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
    z-index: 10;
    transition: all 0.3s ease;

    &:hover:not(.disabled) {
      background: #e6f7ff;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }

    &.disabled {
      opacity: 0.5;
      cursor: not-allowed;
      pointer-events: none;
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

  .content-wrapper {
    display: flex;
    gap: 16px;
    white-space: nowrap;
  }

  .cs-item {
    width: 320px;
    background: #f2f9fe;
    border-radius: 8px;
    flex-shrink: 0;
    overflow: hidden;

    .cs-visual {
      height: 180px;
      display: flex;
      align-items: center;
      justify-content: center;
      background:
        radial-gradient(circle at 76% 24%, rgba(22, 119, 255, 0.18), transparent 30%),
        linear-gradient(135deg, #eef6ff 0%, #dcecff 100%);

      img {
        width: 132px;
        height: 132px;
        object-fit: contain;
      }
    }

    .sci-content {
      padding: 16px;
    }
  }

  .loading-container {
    width: 100%;
    padding: 20px;
  }

  .no-data {
    width: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 200px;
  }
}
</style>
