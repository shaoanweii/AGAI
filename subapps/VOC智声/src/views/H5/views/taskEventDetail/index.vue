<script setup lang="ts">
import { computed, reactive, ref, onMounted, onUnmounted, onActivated, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import HPage from '@h5/components/UI/HPage'
import HNavBar from '@h5/components/UI/HNavBar'
import HCollapseCard from '@h5/components/UI/HCollapseCard/index.vue'
import EventDetailHeader from './components/EventDetailHeader/index.vue'
import EventDetailTabs from './components/EventDetailTabs/index.vue'
import EventBaseInfoSection from './components/sections/EventBaseInfoSection.vue'
import HandleDetailSection from './components/sections/HandleDetailSection.vue'
import VoiceDetailSection from './components/sections/VoiceDetailSection.vue'
import HandleRecordSection from './components/sections/HandleRecordSection.vue'
import RelatedEventSection from '@h5/components/RelatedEventSection/index.vue'
import {
  getMobileSingleEventDetail,
  getMobileSingleEventDetailBase,
  getMobileSingleEventRelationEvents
} from '@h5/api/taskEvent'
import type {
  MobileSingleEventDetailBaseVo,
  MobileSingleEventDetailVo,
  MobileSingleEventRelationEventItem
} from '@h5/api/taskEvent'
import { useKeepAliveScroll } from '@h5/hooks/useKeepAliveScroll'
import { invokeShareAppMessage } from '@h5/utils/weWork'
import { hasPreviousPage, isPreviousPageRedirect } from '../../utils'
import { useShareStore } from '@h5/store'
import { isWeWorkEnvironment } from '@/utils/environment'
import { useH5MenuVisitRecord } from '@h5/hooks/useH5MenuVisitRecord'

defineOptions({
  name: 'H5TaskEventDetail'
})

/**
 * 事件详情页 Tab 标识
 * - 后续如需通过路由参数控制默认 Tab，可复用该枚举
 */
type EventDetailTabKey = 'event' | 'handle' | 'voice' | 'record' | 'related'

interface EventDetailTab {
  key: EventDetailTabKey
  label: string
}

const router = useRouter()
const route = useRoute()
const shareStore = useShareStore()

// H5-事件详情：页面访问操作记录（返回/切换 tab 依赖 keep-alive 的 onActivated）
useH5MenuVisitRecord()

// 记忆并恢复 H5 滚动容器的滚动位置（同一路由不同事件详情需要隔离缓存）
useKeepAliveScroll({
  getCacheKey: r => {
    const query = (r?.query || {}) as Record<string, any>
    const params = (r?.params || {}) as Record<string, any>
    const id = String(params.id ?? query.id ?? '')
    const dataId = String(query.dataId ?? '')
    return `H5TaskEventDetail:${dataId}:${id}`
  }
})

// 当前事件 ID / 原声 ID，从路由参数中解析（事件列表页进入时会同时携带）
const eventId = computed(() => (route.params.id || route.query.id || '') as string)
const dataId = computed(() => (route.query.dataId || '') as string)

//是否显示标题 如果是消息推送场景点开的链接，则不显示标题
const showTitle = computed(() => !route.query.redirect)

// taskStatus=闭环处理/事件关闭时：增加「处理详情」Tab（放在事件详情、原声详情之间）
const showHandleTab = computed(() => {
  const statusCode = String(hub.detailData?.taskStatus ?? '')
  return ['30', '40', '90'].includes(statusCode)
})

const tabs = computed<EventDetailTab[]>(() => {
  const list: EventDetailTab[] = [
    { key: 'event', label: '事件详情' },
    ...(showHandleTab.value ? [{ key: 'handle' as const, label: '处理详情' }] : []),
    { key: 'voice', label: '原声详情' },
    { key: 'record', label: '操作记录' },
    { key: 'related', label: '关联事件' }
  ]
  return list
})

const visibleTabKeys = computed<EventDetailTabKey[]>(() => tabs.value.map(t => t.key))

// 当前激活 Tab
const activeTab = ref<EventDetailTabKey>('event')

// 计算分享标题
const shareTitle = computed(() => {
  return '事件详情'
})

// 计算分享描述：用户意图+1-4级标签+5级观点
const shareDesc = computed(() => {
  const detailData = hub.detailData
  if (!detailData) return '事件详情描述'

  const parts: string[] = []

  // 用户意图
  if (detailData.intentionType) {
    parts.push(detailData.intentionType)
  }

  // 1-4级标签
  const tags = [
    detailData.domTagFirst,
    detailData.domTagSecond,
    detailData.domTagThree,
    detailData.domTagFour
  ].filter(Boolean)

  if (tags.length > 0) {
    parts.push(tags.join('-'))
  }

  // 5级观点
  if (detailData.topic) {
    parts.push(detailData.topic)
  }

  return parts.length > 0 ? parts.join('+') : '事件详情描述'
})

// 「处理详情」折叠卡展开状态：由 HandleDetailSection 判断是否有内容来决定默认态，同时允许用户手动展开/收起
const handleDetailExpanded = ref<boolean>(false)

// 粘性 Tab 外层容器，用于计算滚动偏移
const tabsWrapperRef = ref<HTMLElement | null>(null)

// Section 锚点 ref，用于后续实现滚动联动
const eventSectionRef = ref<HTMLElement | null>(null)
const handleSectionRef = ref<HTMLElement | null>(null)
const voiceSectionRef = ref<HTMLElement | null>(null)
const recordSectionRef = ref<HTMLElement | null>(null)
const relatedSectionRef = ref<HTMLElement | null>(null)

type SectionRef = typeof eventSectionRef

// Tab key 与 Section ref 的映射，方便统一处理滚动计算
const sectionRefMap: Record<EventDetailTabKey, SectionRef> = {
  event: eventSectionRef,
  handle: handleSectionRef,
  voice: voiceSectionRef,
  record: recordSectionRef,
  related: relatedSectionRef
}

// 统一维护的滚动容器
let scrollContainer: HTMLElement | null = null

// 是否处于「点击 Tab 触发的平滑滚动」过程中，用于避免滚动事件抖动切换 Tab
let isScrollingByClick = false
let scrollLockTimer: number | null = null

// 事件详情整体数据占位：后续接入接口时在此集中管理
const hub = reactive<{
  detailData: MobileSingleEventDetailVo | null
  voiceDetail: MobileSingleEventDetailBaseVo | null
  relatedEvents: MobileSingleEventRelationEventItem[]
  loading: boolean
}>({
  detailData: null,
  voiceDetail: null,
  relatedEvents: [],
  loading: false
})

/**
 * 获取页面所需数据（事件详情 + 原声详情）
 * - 事件详情：POST /mobileTerminal/single-event/get-detail-event
 * - 原声详情：POST /mobileTerminal/single-event/get-detail-base
 */
const fetchPageData = async (isLoading = true) => {
  try {
    hub.loading = isLoading
    const query = {
      dataId: dataId.value,
      ...(eventId.value ? { id: eventId.value } : {})
    }
    await Promise.allSettled([
      fetchEventDetail(query),
      fetchEventDetailBase(query),
      fetchRelatedEvents(query)
    ])
  } catch (error: any) {
    console.log('获取详情数据失败', error?.message || error)
    hub.detailData = null
    hub.voiceDetail = null
    hub.relatedEvents = []
  } finally {
    hub.loading = false
  }
}

// 事件详情页
const fetchEventDetail = async (query: any) => {
  try {
    const res = await getMobileSingleEventDetail(query)
    hub.detailData = res?.result || {}
  } catch (error: any) {
    hub.detailData = null
    console.log('获取关联事件失败', error?.message || error)
  }
}

//原声详情
const fetchEventDetailBase = async (query: any) => {
  try {
    const res = await getMobileSingleEventDetailBase(query)
    hub.voiceDetail = res?.result || {}
  } catch (error: any) {
    hub.voiceDetail = null
    console.log('获取关联事件失败', error?.message || error)
  }
}

const fetchRelatedEvents = async (query: any) => {
  try {
    const res = await getMobileSingleEventRelationEvents(query)
    hub.relatedEvents = res?.result || []
  } catch (error: any) {
    hub.relatedEvents = []
    console.log('获取关联事件失败', error?.message || error)
  }
}

/**
 * 解析并缓存滚动容器
 * - 默认使用 HPage 内部的 .f-page__content 作为主滚动容器
 */
const resolveScrollContainer = (): HTMLElement | null => {
  if (scrollContainer) return scrollContainer
  scrollContainer = document.querySelector('.f-page__content') as HTMLElement | null
  return scrollContainer
}

/**
 * 计算滚动到锚点时需要预留的顶部偏移量
 * - 主要用于避免目标 Section 被粘性 Tab 遮挡
 */
const getScrollOffset = (): number => {
  const tabsEl = tabsWrapperRef.value
  if (!tabsEl) return 0
  // 预留少量间距，避免紧贴在 Tab 下方
  return (tabsEl.offsetHeight || 0) + 8
}

/**
 * 滚动到指定 Tab 对应的 Section
 */
const scrollToSection = (key: EventDetailTabKey) => {
  const container = resolveScrollContainer()
  const sectionRef = sectionRefMap[key]
  const targetEl = sectionRef?.value

  if (!container || !targetEl) return

  const containerTop = container.getBoundingClientRect().top
  const targetTop = targetEl.getBoundingClientRect().top
  const offset = getScrollOffset()

  const to = container.scrollTop + (targetTop - containerTop) - offset

  // 标记为点击触发的滚动，避免 handleScroll 中频繁切换 Tab
  isScrollingByClick = true
  if (scrollLockTimer !== null) {
    window.clearTimeout(scrollLockTimer)
  }

  container.scrollTo({ top: to, behavior: 'smooth' })

  // 简单超时兜底，滚动结束后恢复为正常监听
  scrollLockTimer = window.setTimeout(() => {
    isScrollingByClick = false
  }, 400)
}

/**
 * 根据当前滚动位置更新激活 Tab
 * - 通过对比 Section 顶部相对容器顶部的位置，找到最接近「判定线」的 Section
 */
const handleScroll = () => {
  const container = resolveScrollContainer()
  if (!container || isScrollingByClick) return

  const containerRect = container.getBoundingClientRect()
  const offset = getScrollOffset()
  const threshold = offset // 判定线：粘性 Tab 底部附近

  const order = visibleTabKeys.value

  let currentKey: EventDetailTabKey = 'event'
  let maxTop = -Infinity

  order.forEach(key => {
    const el = sectionRefMap[key].value
    if (!el) return

    const rect = el.getBoundingClientRect()
    const top = rect.top - containerRect.top

    // 仅考虑已经经过判定线的 Section，选择其中“最接近判定线”的那一个
    if (top <= threshold && top > maxTop) {
      maxTop = top
      currentKey = key
    }
  })

  if (currentKey !== activeTab.value) {
    activeTab.value = currentKey
  }
}

/**
 * Tab 点击回调
 */
const handleTabClick = (key: EventDetailTabKey) => {
  activeTab.value = key
  scrollToSection(key)
}

/**
 * 处理返回按钮点击
 */
const handleBack = () => {
  // 如果上一页是中间页 /h5Rct，直接跳转到任务列表页
  if (isPreviousPageRedirect(route)) {
    router.push('/h5/task')
    return
  }
  // 如果没有上一页，则返回到任务列表页
  if (!hasPreviousPage()) {
    router.push('/h5/task')
  } else {
    router.back()
  }
}

// 处理分享点击
const handleShare = () => {
  invokeShareAppMessage(shareStore.shareTitle, shareStore.shareDesc)
}

const reset = () => {
  activeTab.value = 'event'
  isFirstLoad.value = true
}

// 当处理详情 Tab 条件不满足时，避免停留在已隐藏的 Tab
watch(
  showHandleTab,
  value => {
    if (!value && activeTab.value === 'handle') {
      activeTab.value = 'event'
    }
  },
  { immediate: true }
)

/**
 * 做这个处理原因：当前详情页面从关联事件跳转至另一个详情页面时，返回要求数据不加载，滚动位置不变；
 * 但从其他页面跳转到详情页面时，要求数据加载，滚动位置重置为顶部；
 */
const DETAIL_ROUTE_NAME = 'H5TaskEventDetail'
watch(
  () => router.currentRoute.value,
  async (to, from) => {
    const toName = String(to?.name ?? '')
    const fromName = String(from?.name ?? '')

    if (toName !== DETAIL_ROUTE_NAME) return reset()
    // H5TaskEventDetail -> H5TaskEventDetail：不做处理
    if (fromName === DETAIL_ROUTE_NAME) return
  },
  { flush: 'post' }
)

onMounted(() => {
  // fetchPageData()
  // 等待 DOM 渲染完成后再绑定滚动事件，避免容器尚未挂载
  nextTick(() => {
    const container = resolveScrollContainer()
    if (container) {
      container.addEventListener('scroll', handleScroll, { passive: true })
      // 初始进页面时根据当前位置计算一次激活 Tab
      handleScroll()
    }
  })
})

// 监听分享标题和描述变化，更新到store
watch(
  [shareTitle, shareDesc],
  ([title, desc]) => {
    shareStore.setShareInfo(title, desc)
  },
  { immediate: true }
)

onUnmounted(() => {
  const container = scrollContainer
  if (container) {
    container.removeEventListener('scroll', handleScroll)
  }
  if (scrollLockTimer !== null) {
    window.clearTimeout(scrollLockTimer)
    scrollLockTimer = null
  }
})

//控制loading
const isFirstLoad = ref(true)
onActivated(() => {
  fetchPageData(isFirstLoad.value)
  if (isFirstLoad.value) {
    isFirstLoad.value = false
  }
})
</script>

<template>
  <HPage>
    <!-- 导航栏：返回 + 标题 -->
    <template #nav-bar>
      <HNavBar
        v-if="showTitle"
        title="事件详情"
        background-color="#1677FF"
        title-color="#fff"
        @click-left="handleBack"
      >
        <template #right>
          <img
            v-if="isWeWorkEnvironment()"
            src="@/assets/h5/share-fill-white.png"
            class="w-24 h-24 share-img"
            @click="handleShare"
          />
        </template>
      </HNavBar>
    </template>

    <!-- 页面内容 -->
    <template #default>
      <van-empty v-if="!hub.loading && (!dataId || !hub.detailData)" description="暂无数据" />
      <van-skeleton v-else-if="hub.loading" class="mt-12" title :row="10" />
      <div v-else class="task-event-detail-page">
        <!-- 头部蓝色区域：事件概要 + 流程进度 -->
        <EventDetailHeader v-if="hub.detailData" :info="hub.detailData" />

        <!-- 粘性 Tab 导航 -->
        <div ref="tabsWrapperRef" class="detail-tabs-sticky">
          <EventDetailTabs
            v-model:active-key="activeTab"
            :tabs="tabs"
            @tab-click="handleTabClick"
          />
        </div>

        <!-- 事件详情 Section -->
        <section ref="eventSectionRef" class="detail-section mt-12">
          <HCollapseCard title="事件详情" collapsible>
            <EventBaseInfoSection :data="hub.detailData" />
          </HCollapseCard>
        </section>

        <!-- 处理详情 Section（闭环处理/事件关闭时展示） -->
        <section v-if="showHandleTab" ref="handleSectionRef" class="detail-section mt-12">
          <HCollapseCard :defaultExpanded="handleDetailExpanded" title="处理详情" collapsible>
            <HandleDetailSection v-model:expanded="handleDetailExpanded" :data="hub.detailData" />
          </HCollapseCard>
        </section>

        <!-- 原声详情 Section -->
        <section ref="voiceSectionRef" class="detail-section mt-12">
          <HCollapseCard title="原声详情" collapsible>
            <VoiceDetailSection :data="hub.voiceDetail" />
          </HCollapseCard>
        </section>

        <!-- 执行记录 Section -->
        <section ref="recordSectionRef" class="detail-section mt-12">
          <HCollapseCard title="操作记录">
            <HandleRecordSection :records="hub.detailData?.operateLogs" />
          </HCollapseCard>
        </section>

        <!-- 关联事件 Section -->
        <section ref="relatedSectionRef" class="detail-section mt-12">
          <HCollapseCard title="关联事件">
            <RelatedEventSection :events="hub.relatedEvents" />
          </HCollapseCard>
        </section>
      </div>
    </template>
  </HPage>
</template>

<style scoped lang="scss">
/* Tab 粘性容器，仅负责吸附效果，内部具体样式由 EventDetailTabs 控制 */
.detail-tabs-sticky {
  position: sticky;
  top: 0;
  z-index: 10;
  background-color: #ffffff;
}

.detail-section {
  // Section 公共样式占位，后续可根据设计稿进一步调整
  margin: 12px;
}
</style>
