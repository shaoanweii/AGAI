<script setup lang="ts">
import HPage from '@h5/components/UI/HPage'
import HNavBar from '@h5/components/UI/HNavBar'
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router'
import { computed, onActivated, onBeforeUnmount, ref, watch } from 'vue'
import { getUserDynamicEvaluationInfo, browseRecordAdd } from '@h5/api/home'
import { showToast } from 'vant'
import { useBrowseRecord } from '@/hooks/useBrowseRecord'
import { isArray } from 'lodash-es'
import type { UserVoiceVo } from '@h5/api/home/types'
import { assembleBrandCarSeries, isLink, toRgba } from '@/utils'
import SvgIcon from '@/components/UI/SvgIcon/index.vue'
import RelatedEventSection from '@h5/components/RelatedEventSection/index.vue'
import { useClipboard } from '@h5/hooks/useClipboard'
import { useKeepAliveScroll } from '@h5/hooks/useKeepAliveScroll.ts'
import { invokeShareAppMessage } from '@h5/utils/weWork'
import { stripHtmlAndTruncate, hasPreviousPage, isPreviousPageRedirect } from '../../utils'
import { usePermissionsStore, useShareStore } from '@h5/store'
import { isWeWorkEnvironment } from '@/utils/environment'
import { useH5MenuVisitRecord } from '@h5/hooks/useH5MenuVisitRecord'
import EventIssueDialog from './components/EventIssueDialog.vue'
import { FunctionPermission } from '@/constants/btnPermMap'
import {
  buildVoiceTopicHighlightHtml,
  getDefaultVoiceTopicIndex,
  getVoiceTopicColor,
  resolveVoiceTopicList
} from '@/utils/voiceTopicHighlight'
import type { VoiceTopicHighlightItem } from '@/utils/voiceTopicHighlight'

const router = useRouter()
const route = useRoute()
const { resetRecord, getBrowseTime, pauseRecord } = useBrowseRecord()
const { copy } = useClipboard()
const shareStore = useShareStore()
const permissionsStore = usePermissionsStore()

// H5-声音详情：页面访问操作记录（返回/切换 tab 依赖 keep-alive 的 onActivated）
useH5MenuVisitRecord()

// 记忆并恢复 H5 滚动容器的滚动位置（同一路由不同事件详情需要隔离缓存）
useKeepAliveScroll({
  getCacheKey: r => {
    const query = (r?.query || {}) as Record<string, any>
    const params = (r?.params || {}) as Record<string, any>
    const id = String(params.id ?? query.id ?? '')
    const dataId = String(query.originalId ?? '')
    const brandCode = String(query.brandCode ?? '')
    const intent = String(query.intent ?? '')
    return `H5VoiceDetail:${dataId}:${id}:${brandCode}:${intent}`
  }
})

const soundId = ref<string>('')
const originalId = ref<string>('')
const brandCode = ref<string>('')
const intent = ref<string>('')
const channelName = ref<string>('')

const resolveParams = () => {
  const routeVoiceId = (route.query.id || route.query.newId || route.query.originalId) as string
  soundId.value = routeVoiceId || ''
  originalId.value = (route.query.originalId as string) || routeVoiceId || ''
  brandCode.value = (route.query.brandCode as string) || ''
  intent.value = (route.query.intent as string) || ''
  channelName.value = (route.query.channelName as string) || ''
}

const voiceVo = ref<UserVoiceVo>({})
const loading = ref<boolean>(false)
const hasSubmitted = ref<boolean>(false)
const browseSession = ref<number>(0)
const activeTopicIndex = ref(-1)
const eventIssueVisible = ref(false)

const isEmpty = computed(() => {
  return !originalId.value || Object.keys(voiceVo.value || {}).length === 0
})

// 品牌车系
const brandCarSeriesName = computed(() => {
  return assembleBrandCarSeries(voiceVo.value.soundslist || [])
})

const topicList = computed(() =>
  resolveVoiceTopicList<VoiceTopicHighlightItem>(voiceVo.value.soundslist, voiceVo.value.topics)
)

const activeTopic = computed(() => topicList.value[activeTopicIndex.value] || null)
const eventIssueIds = computed(() => {
  const soundslist = Array.isArray(voiceVo.value.soundslist) ? voiceVo.value.soundslist : []
  const ids = soundslist.map(item => String(item?.id ?? '').trim()).filter(Boolean)
  return [...new Set(ids)]
})
const eventIssueChannelName = computed(() => String(voiceVo.value.channelName || '').trim())
const canShowEventIssueAction = computed(() =>
  permissionsStore.checkfunctionPermission(FunctionPermission.H5_EVENT_DISPATCH)
)

// 处理原声文本高亮
const highlightedOriginalTextHtml = computed(() =>
  buildVoiceTopicHighlightHtml({
    originalText: voiceVo.value.originalTextScene,
    activeTopic: activeTopic.value
  })
)

// 计算分享标题：标题前10个字，无标题时取正文前10个字
const shareTitle = computed(() => {
  const title = voiceVo.value.title
  if (title && title !== '-') {
    return title.length > 10 ? title.substring(0, 10) : title
  }
  const content = stripHtmlAndTruncate(voiceVo.value.originalTextScene, 10)
  return content || '原声详情'
})

// 计算分享描述：正文前20个字
const shareDesc = computed(() => {
  const content = stripHtmlAndTruncate(voiceVo.value.originalTextScene, 20)
  return content || '原声详情描述'
})

const DETAIL_ROUTE_NAME = 'H5OriginalView'
watch(
  () => router.currentRoute.value,
  async (to, from) => {
    const toName = String(to?.name ?? '')
    const fromName = String(from?.name ?? '')

    // H5OriginalView -> H5VoiceDetail：不做处理
    if (fromName === DETAIL_ROUTE_NAME && toName === 'H5VoiceDetail') return
    if (toName !== DETAIL_ROUTE_NAME) return reset()
  },
  { flush: 'post' }
)

//控制loading
const isFirstLoad = ref(true)
onActivated(async () => {
  resolveParams()
  //重置计时时长
  if (isFirstLoad.value) {
    browseSession.value += 1
    hasSubmitted.value = false
    resetRecord()
  }
  await fetchUserDynamicEvaluationInfo(isFirstLoad.value)
  if (isFirstLoad.value) {
    isFirstLoad.value = false
  }
})

// 监听分享标题和描述变化，更新到store
watch(
  [shareTitle, shareDesc],
  ([title, desc]) => {
    shareStore.setShareInfo(title, desc)
  },
  { immediate: true }
)

// 查看原文
const handleViewLink = async (link: string) => {
  if (!link) return

  router.push({
    path: '/h5/originalView',
    query: {
      link: encodeURIComponent(link)
    }
  })
}

// 提交浏览记录（防重）
const submitBrowseRecord = async () => {
  if (hasSubmitted.value) return
  const currentSession = browseSession.value
  // 点击返回/离开路由时先冻结计时，避免 keep-alive + focus/visibility 事件在临界点恢复导致口径抖动
  pauseRecord()
  const browseTime = getBrowseTime()

  // originalId 优先使用接口返回，兜底使用路由参数
  const finalOriginalId = voiceVo.value.originalId || originalId.value
  const finalIntent = voiceVo.value.intent || intent.value

  if (browseTime > 0 && finalOriginalId) {
    try {
      await browseRecordAdd({
        soundId: soundId.value,
        browseDuration: browseTime,
        originalId: finalOriginalId,
        soundIntention: finalIntent
      })
      // 避免“离开时异步上报”与“重新进入后重置计时”并发，导致 hasSubmitted 串话
      if (browseSession.value === currentSession) {
        hasSubmitted.value = true
      }
    } catch (error) {
      console.error('提交浏览记录失败:', error)
    }
  }
}

const handleBack = async () => {
  await submitBrowseRecord()
  // 如果上一页是中间页 /h5Rct，直接跳转到首页
  if (isPreviousPageRedirect(route)) {
    router.push('/h5/home')
    return
  }
  // 如果没有上一页，则返回首页
  if (!hasPreviousPage()) {
    router.push('/h5/home')
  } else {
    router.back()
  }
}

// 兜底：路由离开时上报（覆盖系统返回键、TabBar 切换等），跳转“原文查看”不上报，仅暂停计时
onBeforeRouteLeave(to => {
  const toName = String(to?.name ?? '')
  if (toName === DETAIL_ROUTE_NAME) return true
  // pauseRecord()
  void submitBrowseRecord()
  return true
})

// 页面卸载前提交浏览记录
onBeforeUnmount(async () => {
  pauseRecord()
  await submitBrowseRecord()
})

const reset = () => {
  isFirstLoad.value = true
  activeTopicIndex.value = -1
}

/**
 * 详情切换后同步默认高亮。
 * 只有一个观点时自动高亮对应的声音片段。
 */
const resetActiveTopic = () => {
  activeTopicIndex.value = getDefaultVoiceTopicIndex(topicList.value)
}

/**
 * 点击观点标签后切换正文高亮；再次点击当前观点时取消高亮。
 */
const handleTopicClick = (index: number) => {
  activeTopicIndex.value = activeTopicIndex.value === index ? -1 : index
}

/**
 * 根据观点情感生成标签样式，并在选中时补充描边。
 */
const getTopicTagStyle = (topic: VoiceTopicHighlightItem, index: number) => {
  const color = getVoiceTopicColor(topic)
  return {
    'background-color': `${toRgba(color, 0.1)}`,
    color,
    cursor: 'pointer',
    'box-shadow': activeTopicIndex.value === index ? `inset 0 0 0 1px ${color}` : 'none'
  }
}

watch(
  [topicList, () => voiceVo.value.originalTextScene],
  () => {
    resetActiveTopic()
  },
  { immediate: true }
)

// 处理分享点击
const handleShare = () => {
  invokeShareAppMessage(shareStore.shareTitle, shareStore.shareDesc)
}

/**
 * 打开事件下发弹窗；事件下发接口使用详情接口 soundslist 中全部有效 id。
 */
const handleOpenEventIssue = () => {
  if (!eventIssueIds.value.length) {
    showToast('当前原声缺少 id，无法执行事件下发')
    return
  }

  eventIssueVisible.value = true
}

/**
 * 事件下发成功后刷新详情，保证关联事件区域能同步后端最新结果。
 */
const handleEventIssueSuccess = () => {
  void fetchUserDynamicEvaluationInfo(false)
}

// 获取声音详情
const fetchUserDynamicEvaluationInfo = async (isLoading = true) => {
  if (!originalId.value) return

  loading.value = isLoading
  try {
    const res = await getUserDynamicEvaluationInfo({
      newId: soundId.value,
      originalId: originalId.value,
      brandCode: brandCode.value
    })

    if (res.success) {
      voiceVo.value = res.result || {}
    } else {
      showToast(res.message)
      voiceVo.value = {}
    }
  } catch (error) {
    console.error('获取用户动态评价详情失败:', error)
    voiceVo.value = {}
  } finally {
    loading.value = false
  }
}
</script>
<template>
  <HPage backgroundColor="#fff">
    <!-- 导航栏插槽 -->
    <template #nav-bar>
      <HNavBar
        @click-left="handleBack"
        title="原文详情"
        background-color="#1677FF"
        title-color="#fff"
      >
        <template #right>
          <div class="voice-detail-nav-actions">
            <img
              v-if="isWeWorkEnvironment()"
              src="@/assets/h5/share-fill-white.png"
              class="w-24 h-24 share-img"
              @click="handleShare"
            />
            <button
              v-if="canShowEventIssueAction"
              class="voice-detail-nav-action"
              type="button"
              @click="handleOpenEventIssue"
            >
              事件下发
            </button>
          </div>
        </template>
      </HNavBar>
    </template>
    <template #default>
      <div class="p-16 fs-12 fw-400">
        <template v-if="loading">
          <van-skeleton title :row="5" class="mt-20" />
        </template>
        <template v-else-if="isEmpty">
          <van-empty description="暂无数据" />
        </template>
        <template v-else>
          <div>
            <div class="voice-detail-title-row">
              <div
                v-if="voiceVo.title && voiceVo.title !== '-'"
                class="fs-16 fw-500 text-primary title-layout multi-line-ellipsis"
              >
                {{ voiceVo.title }}
              </div>
              <div v-else class="voice-detail-title-placeholder"></div>
            </div>
            <div class="flex-y-center mt-10 pb-8">
              <div class="text-secondary">{{ voiceVo.username }}</div>
              <template v-if="voiceVo.channelName || channelName">
                <div class="divider-right"></div>
                <div class="text-secondary">{{ voiceVo.channelName || channelName }}</div>
              </template>
              <template v-if="voiceVo.evaluateTime">
                <div class="divider-right"></div>
                <div class="text-secondary">{{ voiceVo.evaluateTime }}</div>
              </template>
              <div
                v-if="voiceVo.quality"
                class="voice-list__badge flex-center ml-8 fs-12 fw-400"
                :class="{
                  'is-warning': voiceVo.quality && voiceVo.quality.includes('疑似'),
                  'is-good': voiceVo.quality && voiceVo.quality.includes('高价')
                }"
              >
                {{ voiceVo.quality }}
              </div>
            </div>
          </div>

          <div
            class="content-layout fs-16"
            v-if="voiceVo.originalTextScene"
            v-html="highlightedOriginalTextHtml"
          ></div>

          <div class="mt-8">
            <div
              class="mt-10 text-secondary flex-baseline line-height-20"
              v-if="voiceVo.originalId || originalId"
            >
              <div class="detail-info-label text-secondary">原声ID</div>
              <div class="detail-info-value text-primary ml-8 flex-1">
                {{ voiceVo.originalId || originalId }}
              </div>
            </div>
            <div
              class="mt-10 text-secondary flex-baseline line-height-20"
              v-if="brandCarSeriesName"
            >
              <div class="detail-info-label text-secondary">品牌车系</div>
              <div class="detail-info-value text-primary ml-8 flex-1">{{ brandCarSeriesName }}</div>
            </div>

            <div v-if="topicList.length > 0" class="mt-10 flex voice-list__topic">
              <div class="detail-info-label text-secondary">识别观点</div>
              <div class="voice-list__tags detail-info-value flex-1 flex flex-wrap ml-8">
                <template v-for="(topic, idx) in topicList" :key="idx">
                  <span
                    v-if="topic.topic"
                    class="voice-list__tag fw-400 fs-12"
                    :class="{ 'voice-list__tag--active': activeTopicIndex === idx }"
                    :style="getTopicTagStyle(topic, idx)"
                    @click="handleTopicClick(idx)"
                    >{{ topic.topic }}</span
                  >
                </template>
              </div>
            </div>

            <template v-for="(item, index) of voiceVo.ext || []" :key="index">
              <div
                v-if="item.value && !isArray(item.value) && item.value !== '0'"
                class="text-secondary line-height-20"
                :class="{ 'mt-10': index > -1, 'flex-y-center': isLink(item.value) }"
              >
                {{ item.name }}
                <template v-if="isLink(item.value)">
                  <span class="text-primary ml-8 single-line-ellipsis flex-1">{{
                    item.value
                  }}</span>
                  <SvgIcon
                    name="h5-copy-06"
                    width="16px"
                    height="16px"
                    class="copy-link-icon ml-8"
                    color="#5F6A7A"
                    @click="copy(item.value)"
                  />
                  <div @click="handleViewLink(item.value)" class="text-link ml-8">查看原文</div>
                </template>
                <template v-else>
                  <span class="text-primary ml-4">{{ item.value }}</span>
                </template>
              </div>
              <div
                v-else-if="isArray(item.value)"
                class="flex-y-center"
                :class="{ 'mt-10': index > 0 }"
              >
                <div class="text-secondary">{{ item.name }}</div>
                <div class="voice-list__tags flex flex-wrap">
                  <span
                    v-for="(t, idx) in item.value"
                    :key="idx"
                    class="voice-list__tag fw-400 fs-12"
                    >{{ t }}</span
                  >
                </div>
              </div>
            </template>
          </div>

          <!-- 关联事件 -->
          <div v-if="voiceVo.relationEvents && voiceVo.relationEvents.length > 0">
            <div class="event-divider"></div>
            <div class="text-secondary pb-8">关联事件：</div>
            <RelatedEventSection :events="voiceVo.relationEvents" />
          </div>
        </template>
      </div>
      <EventIssueDialog
        v-model:visible="eventIssueVisible"
        :issue-ids="eventIssueIds"
        :channel-name="eventIssueChannelName"
        @success="handleEventIssueSuccess"
      />
    </template>
  </HPage>
</template>
<style lang="scss" scoped>
.voice-detail-nav-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.voice-detail-nav-action {
  flex: none;
  height: 32px;
  padding: 0 14px;
  border: 0;
  border-radius: 4px;
  background: #ffffff;
  color: #1677ff;
  font-size: 14px;
  font-weight: 500;
  line-height: 32px;
  white-space: nowrap;
}

.voice-detail-title-row {
  display: flex;
  align-items: flex-start;
}

.voice-detail-title-placeholder,
.title-layout {
  flex: 1;
  min-width: 0;
}

.share-img {
  flex: none;
}

.divider-right {
  margin: 0 10px;
  width: 1px;
  height: 10px;
  border-right: 1px solid #929aa6;
}
.content-layout {
  background: #f5f7fa;
  border-radius: 8px;
  border: 1px solid #dde3ee;
  padding: 12px;
  color: #1f2733;
  line-height: 22px;
  word-wrap: break-word;
}
.line-height-20 {
  line-height: 20px;
}

.detail-info-label {
  flex-shrink: 0;
  white-space: nowrap;
}

.detail-info-value {
  min-width: 0;
}

.copy-link-icon {
  flex-shrink: 0;
  cursor: pointer;
  vertical-align: middle;
}

.multi-line-ellipsis {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
  overflow: hidden;
}
.title-layout {
  line-height: 22px;
}
.voice-list__badge {
  padding: 3px 9px;
  border-radius: 12px;
  background: #e6f4ff;
  color: #175cd3;
  border: 1px solid #175cd3;
}
.voice-list__badge.is-warning {
  background: #fffaeb;
  color: #b54708;
  border: 1px solid #fedf89;
}
.voice-list__badge.is-good {
  background: #e6f4ff;
  color: #175cd3;
  border: 1px solid #175cd3;
}
.voice-list__tags {
  gap: 8px;
  .voice-list__tag {
    background: #fee9e5;
    color: #ff4b4c;
    border-radius: 4px;
    padding: 4px 12px;
    transition:
      box-shadow 0.2s ease,
      transform 0.2s ease;
  }

  .voice-list__tag.voice-list__tag--active {
    font-weight: 500;
  }
}

.voice-list__topic {
  align-items: baseline;
}

.event-divider {
  width: 100%;
  height: 1px;
  margin: 12px 0;
  border-top: 1px dashed #ebedf0;
}

::v-deep(.highlight) {
  color: #1677ff;
}
</style>
