<script setup lang="ts">
import { ref, watch, onBeforeUnmount, computed } from 'vue'
// 移除路由依赖，组件仅作为受控弹框使用
import { getUserDynamicEvaluationInfo, browseRecordAdd } from '@h5/api/home'
import { showToast } from 'vant'
import { useBrowseRecord } from '@/hooks/useBrowseRecord'
import { isArray } from 'lodash-es'
import type { UserVoiceVo } from '@h5/api/home/types'
import { openSystemBrowser } from '@h5/utils'
import { assembleBrandCarSeries, isLink, toRgba } from '@/utils'
import { sentimentColors } from '@/constants'
import { useRouter } from 'vue-router'
import { useH5ssoStore } from '../../store/sso'
import RelatedEventSection from '@h5/components/RelatedEventSection/index.vue'

defineOptions({
  name: 'H5VoiceDetailDialog'
})

// 对外参数：仅作为弹框组件受控使用
const props = defineProps<{
  show?: boolean
  id?: string
  originalId?: string
  brandCode?: string
  voiceData?: any
  intent?: string
}>()

const emit = defineEmits<{
  (e: 'update:show', val: boolean): void
  (e: 'closed'): void
  (e: 'refresh-browse-ecord'): void
}>()

const { getBrowseTime } = useBrowseRecord()
// 提交防重标记
const hasSubmitted = ref<boolean>(false)

// 内部显示状态（受控：默认 false）
const innerShow = ref<boolean>(props.show ?? false)
const loading = ref<boolean>(false)

// 仅从 props 解析参数
const soundId = ref<string>('')
const soundOriginalId = ref<string>('')
const soundBrandCode = ref<string>('')
const resolveParams = () => {
  soundId.value = props.id ?? ''
  soundOriginalId.value = props.originalId ?? ''
  soundBrandCode.value = props.brandCode ?? ''
}

const isEmpty = computed(() => {
  return !soundOriginalId.value || Object.keys(voiceVo.value).length === 0
})

//品牌车系
const brandCarSeriesName = computed(() => {
  return assembleBrandCarSeries(voiceVo.value.soundslist || [])
})

// 处理原声文本的换行
const formattedOriginalText = computed(() => {
  if (!voiceVo.value.originalTextScene) return ''
  return voiceVo.value.originalTextScene.replace(/\n/g, '<br>')
})

const voiceVo = ref<UserVoiceVo>({})

// 获取声音详情
const fetchUserDynamicEvaluationInfo = async () => {
  if (!soundOriginalId.value) return
  loading.value = true
  try {
    const res = await getUserDynamicEvaluationInfo({
      newId: soundId.value,
      originalId: soundOriginalId.value,
      brandCode: soundBrandCode.value
    })
    if (res.success) {
      voiceVo.value = res.result || {}
    } else {
      showToast(res.message)
    }
  } catch (error) {
    console.error('获取用户动态评价详情失败:', error)
  } finally {
    loading.value = false
  }
}

// 提交浏览记录
const submitBrowseRecord = async () => {
  if (hasSubmitted.value) return
  const browseTime = getBrowseTime()

  if (browseTime > 0 && (props.originalId || voiceVo.value.originalId)) {
    try {
      await browseRecordAdd({
        soundId: soundId.value,
        browseDuration: browseTime,
        originalId: voiceVo.value.originalId || props.originalId,
        soundIntention: voiceVo.value.intent || props.intent
      })
      hasSubmitted.value = true
      emit('refresh-browse-ecord')
    } catch (error) {
      console.error('提交浏览记录失败:', error)
    }
  }
}

// 关闭（仅触发受控关闭与事件）
const handleClose = async () => {
  innerShow.value = false
  await submitBrowseRecord()
  emit('closed')
  emit('update:show', false)
}

// 外部受控同步
watch(
  () => props.show,
  val => {
    if (val === undefined) return
    innerShow.value = val
  }
)

// 显隐监听：打开时按参数拉取详情；关闭时提交浏览记录
watch(
  () => innerShow.value,
  async val => {
    if (val) {
      resolveParams()
      hasSubmitted.value = false
      await fetchUserDynamicEvaluationInfo()
    }
  },
  { immediate: true }
)

// 受控参数变化：打开状态下重新拉取
watch([() => props.id, () => props.originalId, () => props.brandCode], async () => {
  if (innerShow.value) {
    resolveParams()
    await fetchUserDynamicEvaluationInfo()
  }
})

// 卸载前提交浏览记录（组件销毁）
onBeforeUnmount(async () => {
  await submitBrowseRecord()
})

const router = useRouter()
const ssoStore = useH5ssoStore()

//查看原文
const handleViewLink = async (link: string) => {
  if (link) {
    // ssoStore.openBySdk(link)
    router.push({
      path: '/h5/originalView',
      query: {
        link: encodeURIComponent(link)
      }
    })
  }
  // try {
  //   openSystemBrowser(link)
  // } catch (e) {
  //   console.error(e);
  // }
}
</script>

<template>
  <!-- 使用 Vant 弹框，右上角可关闭，内容区域可滚动 -->
  <van-popup
    v-if="innerShow"
    v-model:show="innerShow"
    :overlay="true"
    position="bottom"
    round
    closeable
    :lock-scroll="true"
    :safe-area-inset-bottom="true"
    :style="{
      maxHeight: '90%',
      minHeight: '40%',
      width: '100%',
      overflow: 'hidden',
      display: 'flex',
      flexDirection: 'column'
    }"
    @click-close-icon="handleClose"
    @click-overlay="handleClose"
  >
    <template v-if="loading">
      <van-skeleton title :row="5" class="mt-20" />
    </template>
    <!-- 品牌列表请求失败时显示空状态 -->
    <template v-else-if="isEmpty">
      <van-empty description="暂无数据" />
    </template>
    <div v-else class="dialog-container flex-1">
      <div class="dialog-scroll mt-16 pl-16 pr-16 pb-16 fs-12 fw-400">
        <div>
          <div
            class="fs-16 fw-500 text-primary mr-25 title-layout multi-line-ellipsis"
            v-if="voiceVo.title && voiceVo.title !== '-'"
          >
            {{ voiceVo.title }}
          </div>
          <div class="flex-y-center mt-10 pb-8">
            <div class="text-secondary">{{ voiceVo.username }}</div>
            <template v-if="voiceVo.channelName || voiceData?.channelName">
              <div class="divider-right"></div>
              <div class="text-secondary">{{ voiceVo.channelName || voiceData?.channelName }}</div>
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
        <div class="flex-auto overflow-y-auto pb-10">
          <div
            class="content-layout fs-16"
            v-if="voiceVo.originalTextScene"
            v-html="formattedOriginalText"
          ></div>
          <div class="mt-8">
            <!--          <div class="text-secondary">内容类型：<span class="text-primary ml-4">主贴</span></div>-->
            <div
              class="mt-10 text-secondary flex-baseline line-height-20"
              v-if="brandCarSeriesName"
            >
              <div class="text-secondary">品牌车系</div>
              <div class="text-primary ml-8 flex-1">{{ brandCarSeriesName }}</div>
            </div>
            <!--          <div class="mt-10 text-secondary" v-if="voiceVo.title">-->
            <!--            文章标题：<span class="text-primary ml-4 text-link">{{ voiceVo.title }}</span>-->
            <!--          </div>-->
            <div
              v-if="voiceVo.topics && voiceVo.topics.length > 0"
              class="mt-10 flex voice-list__topic"
            >
              <div class="text-secondary">识别观点</div>
              <div class="voice-list__tags flex-1 flex flex-wrap ml-8">
                <template v-for="(topic, idx) in voiceVo.topics" :key="idx">
                  <span
                    v-if="topic.topic"
                    class="voice-list__tag fw-400 fs-12"
                    :style="{
                      'background-color': `${toRgba(sentimentColors[topic?.sentiment], 0.1)}`,
                      color: `${sentimentColors[topic?.sentiment]}`
                    }"
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
          <!-- 关联订单 -->
          <div v-if="voiceVo.relationEvents && voiceVo.relationEvents.length > 0">
            <div class="event-divider"></div>
            <div class="text-secondary pb-8">关联事件：</div>
            <RelatedEventSection :events="voiceVo.relationEvents" />
          </div>
        </div>
      </div>
    </div>
  </van-popup>
</template>

<style lang="scss" scoped>
::v-deep(.van-popup__close-icon) {
  position: absolute !important;
}
::v-deep(.highlight) {
  color: #1677ff;
}
.dialog-container {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  height: 100%;
  width: 100%;
}

.line-height-20 {
  line-height: 20px;
}

.multi-line-ellipsis {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3; /* 显示的最大行数 */
  overflow: hidden; /* 隐藏超出部分 */
}
.dialog-scroll {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  -webkit-overflow-scrolling: touch;
}
.divider-right {
  margin: 0 10px;
  width: 1px;
  height: 10px;
  border-right: 1px solid #929aa6;
}
.title-layout {
  line-height: 22px;
}
.content-layout {
  background: #f5f7fa;
  border-radius: 8px;
  border: 1px solid #dde3ee;
  padding: 12px;
  color: #1f2733;
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
.voice-list__topic {
  align-items: baseline;
}
.voice-list__tags {
  gap: 8px;
  .voice-list__tag {
    background: #fee9e5;
    color: #ff4b4c;
    border-radius: 4px;
    padding: 4px 12px;
  }
}

.event-divider {
  width: 100%;
  height: 1px;
  margin: 12px 0;
  border-top: 1px dashed #EBEDF0;
}
</style>
