<script setup lang="ts">
import { computed, nextTick, ref, shallowRef, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import HPage from '@h5/components/UI/HPage'
import HNavBar from '@h5/components/UI/HNavBar'
import EventDetailHeader from './components/EventDetailHeader/index.vue'
import EventDetailTabs from './components/EventDetailTabs/index.vue'
import BatchEventStatistics from './components/statistics/index.vue'
import BatchEventCustomerVoice from './components/customerVoice/index.vue'
import BatchEventProcessProgress from './components/processProgress/index.vue'
import { useKeepAliveScroll } from '@h5/hooks/useKeepAliveScroll'
import { invokeShareAppMessage } from '@h5/utils/weWork'
import { hasPreviousPage, isPreviousPageRedirect } from '../../utils'
import { useShareStore } from '@h5/store'
import { isWeWorkEnvironment } from '@/utils/environment'
import { useH5MenuVisitRecord } from '@h5/hooks/useH5MenuVisitRecord'
import { getBatchEventBrief } from '@h5/api/batchEvent'
import type { BatchEventBriefDetailVo } from '@h5/api/batchEvent/types'

defineOptions({
  name: 'H5TaskBatchEventDetail'
})

/**
 * 批量详情页一级 Tab。
 * 事件统计先实现完整内容，客户原声与处理进度保留独立目录占位。
 */
type EventDetailTabKey = 'statistics' | 'customerVoice' | 'processProgress'

interface EventDetailTab {
  key: EventDetailTabKey
  label: string
}

const router = useRouter()
const route = useRoute()
const shareStore = useShareStore()
const DETAIL_ROUTE_NAME = 'H5TaskBatchEventDetail'
const MANUAL_EVENT_SOURCE = 'MANUAL'

// H5-批量事件详情：复用菜单访问记录能力，保持详情页行为一致。
useH5MenuVisitRecord()

// 记忆并恢复 H5 滚动容器的滚动位置，批量详情与单点详情使用独立缓存 key。
useKeepAliveScroll({
  getCacheKey: r => {
    const query = (r?.query || {}) as Record<string, any>
    const params = (r?.params || {}) as Record<string, any>
    const id = String(params.id ?? query.id ?? '')
    const dataId = String(query.dataId ?? '')
    return `H5TaskBatchEventDetail:${dataId}:${id}`
  }
})

/**
 * 从批量事件详情路由中解析事件 ID。
 * @param targetRoute 当前或目标路由对象
 * @returns 批量事件 ID
 */
const resolveBatchEventId = (targetRoute = route) =>
  (targetRoute.params.id || targetRoute.query.id || '') as string

// 当前批量事件 ID 独立缓存，避免离开 keep-alive 页面时被原声详情路由 query.id 覆盖。
const eventId = ref(resolveBatchEventId())
const hasEventId = computed(() => String(eventId.value || '').trim().length > 0)

// 是否显示标题。如果是消息推送场景点开的链接，则不显示标题。
const showTitle = ref(!route.query.redirect)

// 当前激活 Tab。
const activeTab = shallowRef<EventDetailTabKey>('statistics')

// 批量事件简报用于顶部标题、页签显隐与事件统计详情展示。
const batchDetailData = ref<BatchEventBriefDetailVo>({})
const detailLoadSettled = ref(false)
const detailLoadSucceeded = ref(false)

const allTabs: EventDetailTab[] = [
  { key: 'statistics', label: '事件统计' },
  { key: 'customerVoice', label: '客户原声' },
  { key: 'processProgress', label: '处理进度' }
]

const isManualEvent = computed(() => {
  return String(batchDetailData.value.eventSource || '').toUpperCase() === MANUAL_EVENT_SOURCE
})
const showStatisticsTab = computed(
  () => detailLoadSettled.value && detailLoadSucceeded.value && !isManualEvent.value
)
const tabs = computed<EventDetailTab[]>(() => {
  return allTabs.filter(item => showStatisticsTab.value || item.key !== 'statistics')
})

// 分享信息。
const shareTitle = computed(() => '事件详情')
const shareDesc = computed(() => batchDetailData.value.eventName || '事件详情')

// 粘性 Tab 外层容器，用于切换 Tab 后定位内容顶部。
const tabsWrapperRef = ref<HTMLElement | null>(null)

let scrollContainer: HTMLElement | null = null
let detailRequestSeq = 0

/**
 * 获取当前事件可展示的默认 Tab。
 * 统计 Tab 不可见时默认进入客户原声，避免手动下发事件触发统计接口。
 */
const getDefaultActiveTab = (): EventDetailTabKey => {
  return showStatisticsTab.value ? 'statistics' : 'customerVoice'
}

/**
 * 保证当前激活 Tab 存在于可见 Tab 中。
 * @param preferDefault 是否强制回到当前场景默认 Tab
 */
const syncActiveTabWithVisibleTabs = (preferDefault = false) => {
  if (!detailLoadSettled.value && activeTab.value === 'statistics') {
    return
  }

  const hasActiveTab = tabs.value.some(item => item.key === activeTab.value)
  if (preferDefault || !hasActiveTab) {
    activeTab.value = getDefaultActiveTab()
  }
}

/**
 * 解析并缓存滚动容器。
 * 默认使用 HPage 内部的 .f-page__content 作为主滚动容器。
 */
const resolveScrollContainer = (): HTMLElement | null => {
  if (scrollContainer) return scrollContainer
  scrollContainer = document.querySelector('.f-page__content') as HTMLElement | null
  return scrollContainer
}

/**
 * 切换一级 Tab 后回到 Tab 区域顶部，避免从统计长页面底部进入空白目录。
 */
const scrollToTabsTop = async () => {
  await nextTick()

  const container = resolveScrollContainer()
  const tabsEl = tabsWrapperRef.value
  if (!container || !tabsEl) return

  const containerTop = container.getBoundingClientRect().top
  const targetTop = tabsEl.getBoundingClientRect().top
  const to = container.scrollTop + (targetTop - containerTop)
  container.scrollTo({ top: to, behavior: 'smooth' })
}

/**
 * Tab 点击回调。
 * @param key Tab 标识。
 */
const handleTabClick = (key: EventDetailTabKey) => {
  activeTab.value = key
  scrollToTabsTop()
}

/**
 * 处理返回按钮点击。
 */
const handleBack = () => {
  if (isPreviousPageRedirect(route)) {
    router.push('/h5/task')
    return
  }
  if (!hasPreviousPage()) {
    router.push('/h5/task')
  } else {
    router.back()
  }
}

/**
 * 处理分享点击。
 */
const handleShare = () => {
  invokeShareAppMessage(shareStore.shareTitle, shareStore.shareDesc)
}

/**
 * 加载批量事件简报。
 * @param id 批量事件 ID
 */
const loadBatchDetail = async (id: string) => {
  const requestSeq = ++detailRequestSeq
  batchDetailData.value = {}
  detailLoadSettled.value = false
  detailLoadSucceeded.value = false

  if (!id) {
    detailLoadSettled.value = true
    syncActiveTabWithVisibleTabs(true)
    return
  }

  try {
    const response = await getBatchEventBrief({ id })
    if (requestSeq !== detailRequestSeq) return

    detailLoadSucceeded.value = response.success === true
    batchDetailData.value = response.success && response.result ? response.result : {}
  } catch (error) {
    if (requestSeq === detailRequestSeq) {
      batchDetailData.value = {}
      detailLoadSucceeded.value = false
    }
    console.error('获取批量事件简报失败:', error)
  } finally {
    if (requestSeq === detailRequestSeq) {
      detailLoadSettled.value = true
      syncActiveTabWithVisibleTabs()
    }
  }
}

/**
 * 处理进度操作成功后刷新外层详情上下文。
 */
const handleProgressOperationSuccess = () => {
  void loadBatchDetail(String(eventId.value || '').trim())
}

const reset = () => {
  detailLoadSettled.value = false
  detailLoadSucceeded.value = false
  activeTab.value = 'statistics'
}

watch(tabs, () => syncActiveTabWithVisibleTabs())

watch(
  () => router.currentRoute.value,
  (to, from) => {
    const toName = String(to?.name ?? '')
    const fromName = String(from?.name ?? '')

    if (toName !== DETAIL_ROUTE_NAME) return

    eventId.value = resolveBatchEventId(to)
    showTitle.value = !to.query.redirect

    if (fromName === DETAIL_ROUTE_NAME) return
    reset()
  },
  { flush: 'post' }
)

watch(
  eventId,
  id => {
    void loadBatchDetail(String(id || '').trim())
  },
  { immediate: true }
)

watch(
  [shareTitle, shareDesc],
  ([title, desc]) => {
    shareStore.setShareInfo(title, desc)
  },
  { immediate: true }
)
</script>

<template>
  <HPage>
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

    <template #default>
      <van-empty v-if="!hasEventId" description="事件ID缺失，无法打开事件详情" />
      <div v-else class="task-batch-event-detail-page">
        <EventDetailHeader :info="batchDetailData" />

        <div ref="tabsWrapperRef" class="detail-tabs-sticky">
          <EventDetailTabs
            v-model:active-key="activeTab"
            :tabs="tabs"
            @tab-click="handleTabClick"
          />
        </div>

        <section v-if="showStatisticsTab && activeTab === 'statistics'" class="tab-section">
          <BatchEventStatistics :event-id="eventId" :detail="batchDetailData" />
        </section>

        <section v-else-if="activeTab === 'customerVoice'" class="tab-section tab-section--fill">
          <BatchEventCustomerVoice :event-id="eventId" :brand-code="batchDetailData.brandCode" />
        </section>

        <section v-else-if="activeTab === 'processProgress'" class="tab-section">
          <BatchEventProcessProgress
            :event-id="eventId"
            :detail="batchDetailData"
            @operation-success="handleProgressOperationSuccess"
          />
        </section>
      </div>
    </template>
  </HPage>
</template>

<style scoped lang="scss">
.task-batch-event-detail-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 100%;
  background: #f5f7fb;
}

.detail-tabs-sticky {
  position: sticky;
  top: 0;
  z-index: 10;
  background-color: #348cff;
}

.tab-section {
  min-height: 240px;
}

.tab-section--fill {
  flex: 1;
  display: flex;
  min-height: 0;
}
</style>
