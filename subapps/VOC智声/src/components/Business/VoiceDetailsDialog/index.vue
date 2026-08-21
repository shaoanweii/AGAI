<script setup lang="ts">
/**
 * 声音详情弹框（独立版）
 * - 不依赖下钻弹框 Store
 * - 通过 v-model:visible 控制显隐
 * - 通过传入的声源标识拉取详情数据
 */
import { computed, onBeforeUnmount, reactive, ref, watch } from 'vue'
import { isArray } from 'lodash-es'
import { getSoundsDetails } from '@api/drillDownDialog'
import { browseRecordAdd } from '@/api/common'
import { useBrowseRecord } from '@hooks/useBrowseRecord.ts'
import avatarPng from '@/assets/images/drilldown/avatar2.png'
import { assembleBrandCarSeries, isLink, toRgba } from '@/utils'
import VoiceRelatedEvent from '@/components/Business/VoiceRelatedEvent'
import {
  buildVoiceTopicHighlightHtml,
  getDefaultVoiceTopicIndex,
  getVoiceTopicColor,
  resolveVoiceTopicList
} from '@/utils/voiceTopicHighlight'
import type { VoiceTopicHighlightItem } from '@/utils/voiceTopicHighlight'

interface Props {
  /** 弹框显隐 */
  visible: boolean
  /** 弹框标题 */
  title?: string
  /** 声音标识（需要包含 newId 与 originalId） */
  voice?: any
}

const props = withDefaults(defineProps<Props>(), {
  title: '原声详情',
  voice: null
})

const emit = defineEmits<{
  /** v-model:visible */
  (e: 'update:visible', value: boolean): void
}>()

// 对话框可见性（v-model）
const dialogVisible = computed({
  get: () => props.visible,
  set: val => emit('update:visible', val)
})

// 浏览时长记录
const { resetRecord, consumeBrowseTime } = useBrowseRecord()

interface VoiceSnapshot {
  id: string | number
  originalId: string | number
}

interface BrowseSessionSnapshot {
  voice: VoiceSnapshot | null
  sessionSeed: number
  browseTime: number
}

// 本地状态
const hub = reactive({
  loading: false,
  voiceDetails: {} as any
})
const activeVoice = ref<VoiceSnapshot | null>(null)
const activeTopicIndex = ref(-1)
const browseSessionSeed = ref(0)
const submittingSessionSeeds = new Set<number>()
const submittedSessionSeeds = new Set<number>()

//品牌车系  voiceDetails
const getBrandSeries = computed(() => {
  return assembleBrandCarSeries(hub.voiceDetails?.soundslist || [])
})

const topicList = computed(() =>
  resolveVoiceTopicList<VoiceTopicHighlightItem>(
    hub.voiceDetails?.soundslist,
    props.voice?.soundslist,
    hub.voiceDetails?.topics,
    props.voice?.topics
  )
)

const activeTopic = computed(() => topicList.value[activeTopicIndex.value] || null)

// 处理原声文本高亮
const highlightedOriginalTextHtml = computed(() =>
  buildVoiceTopicHighlightHtml({
    originalText: hub.voiceDetails.originalTextScene,
    activeTopic: activeTopic.value
  })
)

/**
 * 将外部传入的声音标识转换为当前浏览会话快照，避免关闭时取到过期 props。
 */
const createVoiceSnapshot = (voice: any): VoiceSnapshot | null => {
  if (!voice?.originalId) return null
  return {
    id: voice.id,
    originalId: voice.originalId
  }
}

/**
 * 判断两次浏览会话是否仍指向同一条原声，避免异步请求回填串数据。
 */
const isSameVoice = (source: VoiceSnapshot | null, target: VoiceSnapshot | null) => {
  if (!source || !target) return false
  return (
    String(source.id ?? '') === String(target.id ?? '') &&
    String(source.originalId ?? '') === String(target.originalId ?? '')
  )
}

// 获取声音详情
const fetchVoiceDetails = async (
  voice: VoiceSnapshot | null = activeVoice.value,
  sessionSeed: number = browseSessionSeed.value
) => {
  if (!voice?.originalId) {
    if (sessionSeed === browseSessionSeed.value) {
      hub.voiceDetails = {}
      hub.loading = false
    }
    return
  }

  const requestVoice = { ...voice }
  if (sessionSeed === browseSessionSeed.value) {
    hub.loading = true
  }

  try {
    const res = await getSoundsDetails({
      newId: String(requestVoice.id ?? ''),
      originalId: String(requestVoice.originalId ?? '')
    })
    if (sessionSeed !== browseSessionSeed.value || !isSameVoice(activeVoice.value, requestVoice))
      return
    hub.voiceDetails = res?.result || {}
  } catch (err) {
    console.error('获取声音详情失败:', err)
  } finally {
    if (sessionSeed === browseSessionSeed.value && isSameVoice(activeVoice.value, requestVoice)) {
      hub.loading = false
    }
  }
}

/**
 * 提交当前浏览会话的记录。
 * 关闭弹框、切换原声或组件卸载时都复用这一条通道，并通过状态位防止重复提交。
 */
const submitBrowseRecord = async ({ voice, sessionSeed, browseTime }: BrowseSessionSnapshot) => {
  if (submittedSessionSeeds.has(sessionSeed) || submittingSessionSeeds.has(sessionSeed)) return
  if (browseTime <= 0 || !voice?.originalId) return

  submittingSessionSeeds.add(sessionSeed)
  try {
    await browseRecordAdd({
      soundId: String(voice.id ?? ''),
      browseDuration: browseTime,
      originalId: String(voice.originalId)
    })
    submittedSessionSeeds.add(sessionSeed)
  } catch (e) {
    console.error('提交浏览记录失败:', e)
  } finally {
    submittingSessionSeeds.delete(sessionSeed)
  }
}

/**
 * 立即截断当前浏览会话，并根据需要开启下一段计时。
 * 先切断时长，再异步上报，避免请求等待期间把时间串到下一条原声。
 */
const snapshotActiveBrowseSession = (restart: boolean): BrowseSessionSnapshot => ({
  voice: activeVoice.value,
  sessionSeed: browseSessionSeed.value,
  browseTime: consumeBrowseTime(restart)
})

/**
 * 激活新的原声会话。
 * 会先同步切换本地状态，再异步拉取详情，避免 UI 被上报请求阻塞。
 */
const activateBrowseSession = (
  voice: VoiceSnapshot | null,
  sessionSeed: number,
  options: { restartTimer?: boolean } = {}
) => {
  if (sessionSeed !== browseSessionSeed.value) return

  activeVoice.value = voice
  hub.voiceDetails = {}
  hub.loading = !!voice?.originalId

  if (!voice?.originalId) return

  if (options.restartTimer !== false) {
    resetRecord()
  }
  void fetchVoiceDetails(voice, sessionSeed)
}

/**
 * 详情切换后同步默认高亮。
 * 只有一个观点时自动高亮该观点对应的片段。
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
 * 生成观点标签样式，选中观点额外补充描边提示。
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

// 监听弹框开关：打开时启动新的会话，关闭时结算当前会话。
watch(
  () => props.visible,
  (newVal, oldVal) => {
    if (newVal) {
      const sessionSeed = ++browseSessionSeed.value
      activateBrowseSession(createVoiceSnapshot(props.voice), sessionSeed)
      return
    }

    if (oldVal) {
      const closingSession = snapshotActiveBrowseSession(false)
      activeVoice.value = null
      browseSessionSeed.value += 1
      hub.loading = false
      void submitBrowseRecord(closingSession)
    }
  }
)

// 监听声源变化：弹框打开状态下切换到新的原声时，先切换会话，再异步上报上一条。
watch(
  () => [props.voice?.id, props.voice?.originalId],
  () => {
    if (!props.visible) return

    const nextVoice = createVoiceSnapshot(props.voice)
    if (isSameVoice(activeVoice.value, nextVoice)) return

    if (!activeVoice.value) {
      const sessionSeed = ++browseSessionSeed.value
      activateBrowseSession(nextVoice, sessionSeed)
      return
    }

    const previousSession = snapshotActiveBrowseSession(!!nextVoice?.originalId)
    const sessionSeed = ++browseSessionSeed.value
    activateBrowseSession(nextVoice, sessionSeed, { restartTimer: false })
    void submitBrowseRecord(previousSession)
  }
)

watch(
  [topicList, () => hub.voiceDetails.originalTextScene],
  () => {
    resetActiveTopic()
  },
  { immediate: true }
)

// 自定义关闭按钮只负责更新显隐，真正的上报由 visible watcher 统一处理。
const handleClose = () => {
  dialogVisible.value = false
}

// 兜底：组件卸载前再次尝试上报，避免父组件直接销毁时遗漏记录。
onBeforeUnmount(() => {
  void submitBrowseRecord(snapshotActiveBrowseSession(false))
})
</script>

<template>
  <!-- .layout__main  body-->
  <el-dialog
    v-if="dialogVisible"
    v-model="dialogVisible"
    destroy-on-close
    :show-close="false"
    align-center
    width="95%"
    append-to=".layout__main"
    style="padding: 0; border-radius: 8px; height: 96%"
    header-class="voice-details-dialog-header"
    body-class="voice-details-dialog-body"
  >
    <!-- 自定义头部（与现有 UI 保持一致） -->
    <div class="content-header">
      <div class="flex-y-center flex-1">
        <span class="ml-16 font-600 fs-20 text-primary">{{ title }}</span>
      </div>
      <div class="cursor-point" @click="handleClose">
        <SvgIcon name="drilldown-close" width="40px" height="40px" color="#929AA6"></SvgIcon>
      </div>
    </div>

    <!-- 详情主体 -->
    <div
      v-loading="hub.loading"
      class="voice-details__container flex-auto overflow-auto pt-16 pb-16 pl-40 pr-40"
    >
      <template v-if="Object.keys(hub.voiceDetails).length">
        <div>
          <div class="flex-y-center">
            <el-avatar :size="44" :src="avatarPng" />
            <div class="ml-8">
              <div class="flex-y-center mb-2">
                <span class="fs-16 fw-500 text-primary" v-if="hub.voiceDetails.username">{{
                  hub.voiceDetails.username
                }}</span>
                <div
                  v-if="hub.voiceDetails.intent"
                  class="voice-list__badge flex-center ml-8 fs-12 fw-400"
                  :class="{
                    'is-warning': hub.voiceDetails.intent?.includes('疑似'),
                    'is-good': hub.voiceDetails.intent?.includes('高价')
                  }"
                >
                  {{ hub.voiceDetails.intent }}
                </div>
              </div>
              <div class="flex-y-center">
                <span class="ft-14 fw-400" v-if="hub.voiceDetails.channelName">{{
                  hub.voiceDetails.channelName
                }}</span>
                <el-divider
                  v-if="hub.voiceDetails.evaluateTime && hub.voiceDetails.channelName"
                  direction="vertical"
                />
                <span class="ft-14 fw-400">{{ hub.voiceDetails.evaluateTime }}</span>
              </div>
            </div>
          </div>

          <div class="mt-16">
            <div
              class="mt-10 fs-14 fw-400 text-secondary flex-baseline line-height-22"
              v-if="hub.voiceDetails.originalId || props.voice?.originalId"
            >
              原声ID：<span class="text-primary ml-8 flex-1">{{
                hub.voiceDetails.originalId || props.voice?.originalId
              }}</span>
            </div>
            <div
              class="mt-10 fs-14 fw-400 text-secondary flex-baseline line-height-22"
              v-if="getBrandSeries"
            >
              品牌车系：<span class="text-primary ml-8 flex-1">{{ getBrandSeries }}</span>
            </div>
            <div class="mt-10 fs-14 fw-400 flex-baseline" v-if="hub.voiceDetails.title">
              文章标题：
              <!-- text-link -->
              <div class="text-primary ml-8 flex-1 line-height-22">
                {{ hub.voiceDetails.title }}
              </div>
            </div>
            <template v-for="(item, index) of hub.voiceDetails.ext" :key="index">
              <div
                v-if="item.value && !isArray(item.value) && item.value !== '0'"
                class="text-secondary line-height-22"
                :class="{ 'mt-10': Number(index) > -1, 'flex-y-center': isLink(item.value) }"
              >
                {{ item.name }}：
                <template v-if="isLink(item.value)">
                  <span class="text-primary ml-8 single-line-ellipsis flex-1">{{
                    item.value
                  }}</span>
                  <a class="ml-8 text-link" target="_blank" :href="item.value">查看原文>></a>
                </template>
                <template v-else>
                  <span class="text-primary ml-4">{{ item.value }}</span>
                </template>
              </div>

              <div
                v-if="isArray(item.value)"
                class="flex-y-center"
                :class="{ 'mt-12': Number(index) > 0 }"
              >
                <div class="text-secondary">{{ item.name }}</div>
                <div class="voice-list__tags flex flex-wrap" v-if="item.value && item.value.length">
                  <span
                    v-for="(t, idx) in item.value"
                    :key="idx"
                    class="voice-list__tag fw-400 fs-12 text-link"
                    >{{ t }}</span
                  >
                </div>
              </div>
            </template>
            <div v-if="topicList.length" class="flex-baseline mt-12">
              <div class="fs-14 fw-400 text-secondary">识别观点：</div>
              <div class="voice-list__tags ml-8 flex-1 flex flex-wrap">
                <template v-for="(topic, idx) in topicList" :key="idx">
                  <span
                    v-if="topic.topic"
                    class="voice-list__tag fw-400 fs-12 text-link"
                    :class="{ 'voice-list__tag--active': activeTopicIndex === idx }"
                    :style="getTopicTagStyle(topic, idx)"
                    @click="handleTopicClick(idx)"
                    >{{ topic.topic }}</span
                  >
                </template>
              </div>
            </div>
            <!-- 关联事件 -->
            <div v-if="hub.voiceDetails.relationEvents?.length" class="flex-baseline mt-12">
              <div class="fs-14 fw-400 text-secondary">关联事件：</div>
              <div class="ml-8 flex-1 pt-8">
                <VoiceRelatedEvent :events="hub.voiceDetails.relationEvents" />
              </div>
            </div>
            <el-divider
              v-if="hub.voiceDetails.originalTextScene"
              border-style="dashed"
              style="margin: 16px 0"
            />
            <div
              v-if="hub.voiceDetails.originalTextScene"
              class="mt-12 fs-14 fw-400 text-secondary flex-baseline line-height-22"
            >
              <div class="detail-text-label">原声内容：</div>
              <div
                class="detail-text-content ml-8 flex-1"
                v-html="highlightedOriginalTextHtml"
              ></div>
            </div>
          </div>
        </div>
      </template>
      <template v-else>
        <div class="flex-center h-full">
          <el-empty description="暂无数据" />
        </div>
      </template>
    </div>
  </el-dialog>
</template>
<style lang="scss">
.voice-details-dialog-header {
  padding-bottom: 0 !important;
}
.voice-details-dialog-body {
  height: 100%;
  padding: 0 !important;
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}
</style>
<style scoped lang="scss">
.content-header {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  border-bottom: 1px solid #dfe2e8;
  background: #f5f7fa;
  height: 72px;
  padding: 0 24px;
  border-radius: 8px 8px 0 0;
}

.voice-details__container {
  flex: 1;
  min-height: 0;
  overflow: auto;
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

.flex-baseline {
  display: flex;
  align-items: baseline;
}

.voice-list__tags {
  gap: 8px;
  .voice-list__tag {
    background: #e2f3fe;
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

.detail-text-label {
  flex-shrink: 0;
  white-space: nowrap;
}

.detail-text-content {
  min-width: 0;
  color: #6e7b91;
  word-break: break-word;
}

.line-height-22 {
  line-height: 22px;
}
</style>
