<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import type { MobileSingleEventDetailBaseVo, SingleEventIntentionVo } from '@h5/api/taskEvent'

// import { format } from 'crypto-js' // 未使用，注释掉

interface VoiceDetailSectionProps {
  data: MobileSingleEventDetailBaseVo | null
}

const props = defineProps<VoiceDetailSectionProps>()


const router = useRouter()

const hasText = (value: unknown): boolean => {
  if (value === undefined || value === null) return false
  return String(value).trim().length > 0
}

const formatText = (value: unknown, fallback = '-'): string => {
  return hasText(value) ? String(value).trim() : fallback
}

// 是否为评论, 0是评论, 非评论的 其他值都是帖子。 1 | null
const isMainPost = computed(() => String(props.data?.isMainPost || '') === '1' || String(props.data?.isMainPost || '') === 'Y')
const isComment = computed(() => !isMainPost.value)

const sourceText = computed(() => formatText(props.data?.channelName))
const voiceTypeText = computed(() => {
  if (isMainPost.value) return '主帖'
  if (isComment.value) return '评论'
  return formatText(props.data?.contentTypeName)
})

const userLabel = computed(() => {
  if (isComment.value) return '评论用户'
  if (isMainPost.value) return '发帖用户'
  return '发声用户'
})
const userNameText = computed(() => {
  if (isComment.value) return formatText(props.data?.commentUserName)
  return formatText(props.data?.postUserName)
})

const postTitleText = computed(() => formatText(props.data?.mainPostTitle))
const postContentText = computed(() => formatText(props.data?.mainPostDetails))
const postUserText = computed(() => formatText(props.data?.postUserName))
const postTimeText = computed(() => formatText(props.data?.postTime))
const postUrlText = computed(() => formatText(props.data?.mainPostUrl))

const commentTimeText = computed(() => formatText(props.data?.commentTime))
const commentContentText = computed(() => formatText(props.data?.commentDetails))

const vehicleCardList = computed(() => {
  const d = props.data || {}
  return [
    { label: '品牌', value: formatText(d.brandName) },
    { label: '车系', value: formatText(d.carSeriesName) },
    { label: '车型', value: formatText(d.carModel) },
    { label: '发动机号', value: formatText(d.engineNo) },
    { label: '车牌号', value: formatText(d.licensePlateNo) },
    { label: '车架号', value: formatText(d.vinNo) }
  ]
})

const extraInfoList = computed(() => {
  const d = props.data || {}
  // 用户意图
  const intentionArr = (intentionTags.value || ['-']).map((it: string) => {
    return {
      label: '用户意图',
      value: it
    }
  })
  return [
    { label: '购车时间', value: formatText(d.carPurchaseTime) },
    { label: '经销商', value: formatText(d.dealerName) },
    ...intentionArr
  ]
})

const formatIntention = (it: SingleEventIntentionVo): string => {
  const intentionText = formatText(it?.intentionType, '')
  const tagPath = [it?.domTagFirst, it?.domTagSecond, it?.domTagThree, it?.domTagFour]
    .filter(v => hasText(v))
    .map(v => String(v).trim())
    .join('#')
  const tagText = formatText(tagPath, '')
  const topicText = formatText(it?.topic, '')
  const result = [`${intentionText}`, `${tagText}`, `${topicText}`].filter(Boolean).join('-')
  return formatText(result)
}

const intentionList = computed(() => (props.data?.intentions || []).filter(Boolean))
const intentionTags = computed(() => intentionList.value.map(formatIntention))
const intentionSummaryText = computed(() => {
  if (!intentionTags.value.length) return ''
  // 多条意图使用中文分号拼接，避免堆叠占位过长
  return intentionTags.value.join('；')
})

//查看原文
const handleViewLink = async (link?: string) => {
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
  <div class="voice-detail">
    <!-- 暂无数据占位 -->
    <div v-if="!props.data" class="empty-text">
      暂无原声数据
    </div>

    <!-- 原声详情内容 -->
    <div v-else>
      <!-- 顶部摘要卡：数据来源 / 原声类型 / 发帖(评论)用户 -->
      <div class="summary-card">
        <div class="summary-item">
          <div class="summary-label">数据来源</div>
          <div class="summary-value">{{ sourceText }}</div>
        </div>

        <div class="summary-divider"></div>

        <div class="summary-item">
          <div class="summary-label">原声类型</div>
          <div class="summary-value">{{ voiceTypeText }}</div>
        </div>

        <div class="summary-divider"></div>

        <div class="summary-item">
          <div class="summary-label">{{ userLabel }}</div>
          <div class="summary-value">{{ userNameText }}</div>
        </div>
      </div>

      <!-- 主帖/评论信息 -->
      <div class="info-list mt-12">
        <!-- 评论 -->
        <template v-if="isComment">
          <div class="info-row">
            <div class="label">评论时间</div>
            <div class="value">{{ commentTimeText }}</div>
          </div>

          <div class="info-row">
            <div class="label">评论详情</div>
            <div class="value value-multiline" v-html="commentContentText"></div>
          </div>

          <div class="block-divider mt-12"></div>

          <div class="info-row mt-4">
            <div class="label">主帖标题</div>
            <div class="value value-multiline">{{ postTitleText }}</div>
          </div>
          <div class="info-row">
            <div class="label">主帖内容</div>
            <div class="value value-multiline" v-html="postContentText"></div>
          </div>
          <div class="info-row">
            <div class="label">发帖用户</div>
            <div class="value">{{ postUserText }}</div>
          </div>
          <div class="info-row">
            <div class="label">发帖时间</div>
            <div class="value">{{ postTimeText }}</div>
          </div>
          <div class="info-row">
            <div class="label">主帖链接</div>
            <div class="value">
              <van-text-ellipsis class="link-text" @click="handleViewLink(props.data.mainPostUrl)"
                                 :content="props.data.mainPostUrl || '-'" />
            </div>
          </div>
        </template>

        <!-- 主帖 -->
        <template v-else>
          <div class="info-row">
            <div class="label">主帖标题</div>
            <div class="value value-multiline">{{ postTitleText }}</div>
          </div>
          <div class="info-row">
            <div class="label">主帖内容</div>
            <div class="value value-multiline" v-html="postContentText"></div>
          </div>
          <div class="info-row">
            <div class="label">发帖时间</div>
            <div class="value">{{ postTimeText }}</div>
          </div>
          <div class="info-row">
            <div class="label">主帖链接</div>
            <div class="value">
              <van-text-ellipsis class="link-text" @click="handleViewLink(props.data.mainPostUrl)"
                                 :content="props.data.mainPostUrl || '-'" />
            </div>
          </div>
        </template>
      </div>

      <!-- 车辆信息卡 -->
      <div class="vehicle-card mt-12">
        <div class="vehicle-grid">
          <div v-for="item in vehicleCardList" :key="item.label" class="vehicle-cell">
            <div class="cell-label">{{ item.label }}</div>
            <div class="cell-value">{{ item.value }}</div>
          </div>
        </div>
      </div>

      <!-- 购车时间 / 经销商 / 用户意图 -->
      <div class="info-list mt-12">
        <div v-for="item in extraInfoList" :key="item.label" class="info-row">
          <div class="label">{{ item.label }}</div>
          <div class="value">{{ item.value }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.voice-detail {
  font-size: 14px;
  color: #1f2733;
}

.empty-text {
  padding: 8px 0;
  font-size: 12px;
  color: #929aa6;
}

/* 顶部摘要卡 */
.summary-card {
  display: flex;
  align-items: center;
  padding: 12px 8px;
  background: #F5F7FA;
  border-radius: 8px;
}

.summary-item {
  flex: 1;
  text-align: center;
}

.summary-label {
  font-weight: 400;
  font-size: 12px;
  color: #929AA6;
}

.summary-value {
  margin-top: 3px;
  font-weight: 500;
  font-size: 12px;
  color: #1F2733;
  line-height: 22px;
}

.summary-divider {
  width: 1px;
  height: 32px;
  background: #EBEDF0;
}

/* 信息列表 */
.info-list {
  display: flex;
  flex-direction: column;
  row-gap: 10px;
}

.info-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  column-gap: 8px;

  .label {
    min-width: 48px;
    text-align: right;
    font-weight: 400;
    font-size: 12px;
    color: rgba(0, 0, 0, 0.4);
    line-height: 22px;
  }

  .value {
    flex: 1 1 auto;
    font-weight: 400;
    font-size: 12px;
    color: rgba(0, 0, 0, 0.9);
    line-height: 22px;
    word-break: break-all;
  }
}

.value-multiline {
  // white-space: pre-wrap;
}

.value-empty {
  color: rgba(0, 0, 0, 0.4);
}

.link-text {
  display: inline-block;
  max-width: 100%;
  font-size: 12px;
  color: #1677ff;
  text-decoration: none;
}

.block-divider {
  height: 1px;
  border-top: 1px dashed #EBEDF0;
}

/* 车辆信息卡（3列*2行） */
.vehicle-card {
  padding: 12px 8px;
  background: #F5F7FA;
  border-radius: 8px;
}

.vehicle-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  row-gap: 12px;
}

.vehicle-cell {
  text-align: center;
  position: relative;
}

.vehicle-cell:nth-child(1),
.vehicle-cell:nth-child(2),
.vehicle-cell:nth-child(4),
.vehicle-cell:nth-child(5) {
  &::before {
    position: absolute;
    content: '';
    top: 5px;
    right: 0;
    width: 1px;
    height: calc(100% - 10px);
    background: rgba(0, 0, 0, 0.08);
  }
}

.cell-label {
  font-weight: 400;
  font-size: 12px;
  color: #929AA6;
}

.cell-value {
  margin-top: 3px;
  font-weight: 500;
  font-size: 12px;
  color: #1F2733;
  line-height: 22px;
  word-break: break-all;
}

/* 用户意图 */
.intention-tags {
  display: flex;
  flex-direction: column;
  row-gap: 8px;
}

.intention-tag {
  padding: 10px 12px;
  background: #f5f7fa;
  border-radius: 8px;
  font-size: 12px;
  line-height: 18px;
  color: rgba(0, 0, 0, 0.9);
  word-break: break-all;
}

.intention-summary {
  padding: 10px 12px;
  background: #f5f7fa;
  border-radius: 8px;
  font-size: 12px;
  line-height: 18px;
  color: rgba(0, 0, 0, 0.9);
  word-break: break-all;
}
</style>
