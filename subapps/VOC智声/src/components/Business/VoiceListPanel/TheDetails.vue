<script setup lang="ts">
// 右侧详情（与两处实现一致，抽取为通用）
import { ElMessage } from 'element-plus'
import { ref, onBeforeUnmount, onMounted, watch, computed } from 'vue'
import VoiceRelatedEvent from '@/components/Business/VoiceRelatedEvent'
import CorpusCreateDialog from './components/CorpusCreateDialog.vue'
import { getRealAttr } from '@/views/leaderOverview/leader/common/fn.ts'
import avatarPng from '@/assets/images/drilldown/avatar2.png'
import { postSoundsDetailsByUrl } from '@/api/overview/leader'
import { useBrowseRecord } from '@hooks/useBrowseRecord.ts'
import { browseRecordAdd } from '@/api/common'
import { assembleBrandCarSeries, toRgba, isLink } from '@/utils'
import { isArray } from 'lodash-es'
import {
  buildVoiceTopicHighlightHtml,
  getDefaultVoiceTopicIndex,
  getVoiceTopicColor,
  resolveVoiceTopicList
} from '@/utils/voiceTopicHighlight'
import type { VoiceTopicHighlightItem } from '@/utils/voiceTopicHighlight'
import { FunctionPermission } from '@/constants/btnPermMap.ts'
import useUserStore from '@/store/modules/user'
import useGeneralDrillDownStore from '@/store/modules/generalDrillDown'

type UserDetailMode = 'back' | 'close'

interface Props {
  lastItem: any
  curItem: any
  enableErrorCorrection?: boolean
  // 是否展示“添加语料”按钮及弹窗入口
  showCorpusCreateAction?: boolean
  // 详情数据来源：remote=详情接口；list=直接使用列表项数据
  detailSource?: 'remote' | 'list'
  // 详情接口地址（POST）
  detailApiUrl?: string
  // 是否展示识别观点
  showTopics?: boolean
  // 是否展示品牌车系
  showBrandSeries?: boolean
  // 是否展示关联事件
  showRelationEvents?: boolean
  // 用户详情沿用的查询上下文
  queryParams?: Record<string, any>
  // 用户详情关闭模式：下钻内使用 back，其它独立页面使用 close
  userDetailMode?: UserDetailMode
}

const props = withDefaults(defineProps<Props>(), {
  lastItem: {},
  curItem: {},
  enableErrorCorrection: false,
  showCorpusCreateAction: true,
  detailSource: 'remote',
  detailApiUrl: '/report/vocLeadership/getSoundsDetails',
  showTopics: true,
  showBrandSeries: true,
  showRelationEvents: true,
  queryParams: () => ({}),
  userDetailMode: 'close'
})

const { consumeBrowseTime } = useBrowseRecord()

const defDetail = {
  originalTextScene: '',
  title: '',
  quality: '',
  browsingDuration: null,
  opinion: null,
  newId: '',
  originalId: '',
  intent: '',
  userId: '',
  username: '',
  channelName: '',
  channelCode: '',
  evaluateTime: '',
  ext: [] as any[],
  relationEvents: [] as any[],
  soundslist: [] as VoiceTopicHighlightItem[],
  topics: [] as VoiceTopicHighlightItem[]
}

const curDetail = ref<any>(defDetail)
const loading2 = ref(true)
const activeTopicIndex = ref(-1)
const corpusCreateVisible = ref(false)

const userStore = useUserStore()
const ddStore = useGeneralDrillDownStore()

const getBrandSeries = computed(() => {
  let brandSeries = assembleBrandCarSeries(curDetail.value?.soundslist || [])
  if (!brandSeries && (curDetail.value?.brand || curDetail.value?.series)) {
    brandSeries = [`${curDetail.value?.brand || ''}`, `${curDetail.value?.series || ''}`].join(' ')
  }
  return brandSeries || ''
})

const detailTopics = computed(() =>
  resolveVoiceTopicList<VoiceTopicHighlightItem>(
    curDetail.value?.soundslist,
    props.curItem?.soundslist,
    curDetail.value?.topics,
    props.curItem?.topics
  )
)

const activeTopic = computed(() => detailTopics.value[activeTopicIndex.value] || null)

// 处理原声文本高亮
const highlightedOriginalTextHtml = computed(() =>
  buildVoiceTopicHighlightHtml({
    originalText: curDetail.value.originalTextScene,
    activeTopic: activeTopic.value
  })
)

const normalizeDetailFromItem = (item: any) => {
  const safeItem = item || {}
  const detail = {
    ...defDetail,
    ...safeItem,
    username: safeItem.username || safeItem.custName || safeItem.customerName || '',
    originalTextScene: safeItem.originalTextScene || safeItem.originalTexTScene || '',
    channelName: safeItem.channelName || safeItem.channel || '',
    evaluateTime: safeItem.evaluateTime ?? safeItem.dataCreateTime ?? null,
    ext: safeItem.ext || []
  }
  return detail
}

/**
 * 打开新增语料弹窗。
 */
const handleOpenCorpusCreateDialog = () => {
  if (!props.showCorpusCreateAction) return
  corpusCreateVisible.value = true
}

/**
 * 打开用户详情。
 * 用户详情统一交给下钻 store 承载，关闭模式由调用场景决定。
 */
const handleOpenUserDetails = () => {
  const userId =
    curDetail.value?.userId ||
    curDetail.value?.oneId ||
    props.curItem?.oneId ||
    props.curItem?.userId

  if (!userId) {
    return
  }

  ddStore.showUserDetail(
    { userId, queryParams: { ...(props.queryParams || {}) } },
    props.userDetailMode
  )
}

/**
 * 按指定原声项提交浏览记录。
 * 切换列表项时需要结算上一条，关闭详情或离开页面时需要结算当前条。
 */
const submitBrowseRecordByItem = async (item: any, restart: boolean = false) => {
  const browseTime = consumeBrowseTime(restart)
  if (browseTime <= 0 || !item?.originalId) return

  try {
    await browseRecordAdd({
      soundId: item.id,
      browseDuration: browseTime,
      originalId: item.originalId
    })
  } catch (error) {
    console.error('提交浏览记录失败:', error)
  }
}

/**
 * 切换原声时，上报刚离开的那一条浏览记录。
 */
const reportPreviousItemBrowse = async () => {
  await submitBrowseRecordByItem(props.lastItem, true)
}

/**
 * 关闭详情或离开页面时，上报当前仍在查看的原声。
 */
const reportCurrentItemBrowse = async () => {
  await submitBrowseRecordByItem(props.curItem)
}

// 详情请求
const fetchDetail = async () => {
  const errMsg = '获取客户原声详情数据失败'
  try {
    loading2.value = true
    const queryParams: VocQueryParams = getRealAttr({
      newId: props.curItem.id,
      originalId: props.curItem.originalId
    })
    const response = await postSoundsDetailsByUrl(props.detailApiUrl, queryParams)
    if (response.success) {
      curDetail.value = normalizeDetailFromItem(response.result || defDetail)
    } else {
      curDetail.value = defDetail
      ElMessage.error(response.message || errMsg)
    }
  } catch (error) {
    console.error(`${errMsg}:, ${error}`)
    curDetail.value = defDetail
    ElMessage.error(`${errMsg}，请稍后重试`)
  } finally {
    loading2.value = false
  }
}

const applyLocalDetail = () => {
  loading2.value = true
  curDetail.value = normalizeDetailFromItem(props.curItem)
  loading2.value = false
}

const refreshDetail = () => {
  if (props.detailSource === 'list') {
    applyLocalDetail()
    return
  }
  fetchDetail()
}

/**
 * 详情切换后重置默认高亮。
 * 仅当观点只有一个时自动选中，否则保持未选中。
 */
const resetActiveTopic = () => {
  activeTopicIndex.value = getDefaultVoiceTopicIndex(detailTopics.value)
}

/**
 * 点击观点标签后切换正文高亮；再次点击当前观点时取消高亮。
 */
const handleTopicClick = (index: number) => {
  activeTopicIndex.value = activeTopicIndex.value === index ? -1 : index
}

/**
 * 根据观点情感返回标签颜色，并在选中时补充描边。
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

onMounted(() => {
  refreshDetail()
})

onBeforeUnmount(() => {
  void reportCurrentItemBrowse()
})

// 监听 curItem 变化，触发详情接口
watch(
  () => [props.curItem?.originalId, props.curItem?.id, props.detailSource, props.detailApiUrl],
  () => {
    refreshDetail()
  }
)

watch(
  [detailTopics, () => curDetail.value.originalTextScene],
  () => {
    resetActiveTopic()
  },
  { immediate: true }
)

// 监听 lastItem 变化，上报浏览时长
watch(
  () => props.lastItem?.originalId,
  (newVal, oldVal) => {
    if (!newVal || newVal === oldVal) return
    void reportPreviousItemBrowse()
  }
)

// 暴露上报方法给父组件
defineExpose({
  submitBrowseRecord: reportCurrentItemBrowse
})
</script>

<template>
  <div class="theDetail">
    <div class="voice-details__header flex-between">
      <div class="fs-16 fw-500 text-primary">
        <span>原声详情</span>
      </div>
      <el-button
        v-if="
          props.showCorpusCreateAction &&
          userStore.checkfunctionPermission(FunctionPermission.ADD_CORPUS)
        "
        type="primary"
        class="voice-details__action-btn ml-16"
        @click="handleOpenCorpusCreateDialog"
      >
        添加语料
      </el-button>
    </div>
    <div class="voice-details__content mt-24 pt-16 pb-17" v-loading="loading2">
      <div>
        <div class="flex-y-center">
          <el-avatar
            :size="44"
            :src="avatarPng"
            class="voice-details__avatar"
            @click="handleOpenUserDetails"
          />
          <div class="ml-8">
            <!-- 姓名 -->
            <div class="flex-y-center mb-3">
              <span class="fs-16 fw-500 text-primary">{{ curDetail.username || '-' }}</span>
              <div
                v-if="curDetail.quality"
                class="voice-list__badge flex-center ml-8 fs-12 fw-400"
                :class="{
                  'is-warning': curDetail.quality.includes('疑似'),
                  'is-good': curDetail.quality.includes('高价')
                }"
              >
                {{ curDetail.quality }}
              </div>
            </div>
            <div class="flex-y-center">
              <span class="ft-14 fw-400">{{
                curItem.channel || curDetail.channelName || '-'
              }}</span>
              <template v-if="curDetail.evaluateTime">
                <el-divider direction="vertical" />
                <span class="ft-14 fw-400">{{ curDetail.evaluateTime }}</span>
              </template>
            </div>
          </div>
        </div>

        <div class="mt-16" style="line-height: 28px">
          <div
            class="mt-10 fs-14 fw-400 text-secondary flex-baseline line-height-22"
            v-if="curDetail.originalId || curItem.originalId"
          >
            原声ID：<span class="text-primary ml-8 flex-1">{{
              curDetail.originalId || curItem.originalId
            }}</span>
          </div>
          <div
            class="mt-10 fs-14 fw-400 text-secondary flex-baseline line-height-22"
            v-if="props.showBrandSeries && getBrandSeries"
          >
            品牌车系：<span class="text-primary ml-8 flex-1">{{ getBrandSeries }}</span>
          </div>
          <div class="mt-10 fs-14 fw-400 flex-baseline" v-if="curDetail.title">
            文章标题：
            <!-- text-link -->
            <div class="text-primary ml-8 flex-1 line-height-22">
              {{ curDetail.title }}
            </div>
          </div>
          <template v-for="(item, index) of curDetail.ext" :key="index">
            <div
              v-if="item.value && !isArray(item.value) && item.value !== '0'"
              class="text-secondary lh-22"
              :class="{ 'mt-10': Number(index) > -1, 'flex-y-center': isLink(item.value) }"
            >
              {{ item.name }}：
              <template v-if="isLink(item.value)">
                <span class="text-primary ml-8 single-line-ellipsis flex-1">{{ item.value }}</span>
                <a class="ml-8 text-link theLink" target="_blank" :href="item.value">查看原文>></a>
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
              <div class="text-secondary">{{ item.name }}：</div>
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

          <div class="flex-baseline mt-10" v-if="props.showTopics && detailTopics.length">
            <div class="fs-14 fw-400 text-secondary">识别观点：</div>
            <div
              class="voice-list__tags flex flex-wrap flex-1"
              style="line-height: 12px"
              v-if="detailTopics.length"
            >
              <template v-for="(topic, idx) in detailTopics" :key="idx">
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
          <div
            v-if="props.showRelationEvents && curDetail.relationEvents?.length"
            class="flex-baseline mt-12"
          >
            <div class="fs-14 fw-400 text-secondary">关联事件：</div>
            <div class="ml-8 flex-1 pt-8">
              <VoiceRelatedEvent :events="curDetail.relationEvents" />
            </div>
          </div>
          <el-divider
            v-if="curDetail.originalTextScene"
            border-style="dashed"
            style="margin: 16px 0"
          />
          <div
            v-if="curDetail.originalTextScene"
            class="mt-12 fs-14 fw-400 text-secondary flex-baseline line-height-22"
          >
            <div class="detail-text-label">原声内容：</div>
            <div class="detail-text-content ml-8 flex-1" v-html="highlightedOriginalTextHtml"></div>
          </div>
        </div>
      </div>
    </div>

    <CorpusCreateDialog v-if="props.showCorpusCreateAction" v-model:visible="corpusCreateVisible" />
  </div>
</template>

<style scoped lang="scss">
.theDetail {
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.voice-details__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
}

.voice-details__action-btn {
  min-width: 88px;
}

.voice-details__avatar {
  cursor: pointer;
  flex-shrink: 0;
}

.voice-details__content {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 16px 24px;
  background: #f5f7fa;
  border-radius: 8px;
  border: 1px solid #dde3ee;
}

.theLink {
  &:hover {
    color: #006adc !important;
  }
}

.voice-list__tags {
  gap: 8px;
}
.voice-list__badge {
  padding: 3px 9px;
  border-radius: 12px;
  background: #e6f4ff;
  color: #175cd3;
  border: 1px solid #175cd3;

  &.is-warning {
    background: #fffaeb;
    color: #b54708;
    border: 1px solid #fedf89;
  }
  &.is-good {
    background: #e6f4ff;
    color: #175cd3;
    border: 1px solid #175cd3;
  }
}

.detail-text-label {
  flex-shrink: 0;
  white-space: nowrap;
}

.detail-text-content {
  min-width: 0;
  color: #6e7b91;
  line-height: 22px;
  text-align: justify;
  word-break: break-word;
}

.voice-list__tags .voice-list__tag {
  background: #e2f3fe;
  border-radius: 4px;
  padding: 6px 12px;
  transition:
    box-shadow 0.2s ease,
    transform 0.2s ease;
}

.voice-list__tags .voice-list__tag.voice-list__tag--active {
  font-weight: 500;
}

.fw-500 {
  font-weight: 500;
}
</style>
