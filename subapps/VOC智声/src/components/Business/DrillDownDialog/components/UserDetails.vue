<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { WORD_CLOUD_RANDOM_COLOR_PALETTE } from '@/constants'
import closePng from '@/assets/images/close.png'
import avatarPng from '@/assets/images/drilldown/avatar1.png'
import phonePng from '@/assets/images/drilldown/phone.png'
import carPng from '@/assets/images/drilldown/car.png'
import thumbsDownPng from '@/assets/images/drilldown/thumbs-down.png'
import voiceprintFillPng from '@/assets/images/drilldown/voiceprint-fill.png'
import HorizontalSegmentedBar from '@components/Business/DrillDownDialog/components/HorizontalSegmentedBar'
import WordCloudChart from '@components/DataSourceAnalysis/WordCloudChart.vue'
import { TriangleRankList } from '@components/Business/DrillDownDialog/components/index.ts'
import CommonTitle from '@components/Business/DrillDownDialog/components/CommonTitle'
import TimelineList from '@components/Business/DrillDownDialog/components/TimelineList/index.vue'
import useGeneralDrillDownStore from '@store/modules/generalDrillDown'
import { getUserDetail } from '@/api/drillDownDialog'
import type { UserDetailVo } from '@/api/drillDownDialog/types'
import { ElMessage } from 'element-plus'
import { fmtNum, fmtPer } from '@/utils'

defineOptions({
  name: 'UserDetails'
})

type UserDetailsPayload = {
  userId?: string | number
  queryParams?: Record<string, any>
}

type NormalizedUserDetailsPayload = {
  userId?: string
  queryParams: Record<string, any>
}

type IntentionTypeItem = {
  name: string
  value?: number | string | null
  backgroundColor: string
}

type Props = {
  payload?: UserDetailsPayload | null
  closeBehavior?: 'store' | 'emit'
}

const props = withDefaults(defineProps<Props>(), {
  payload: null,
  closeBehavior: 'store'
})

const emit = defineEmits<{
  (e: 'close'): void
}>()

//时间轴列表 定义 TimelineList 组件的引用类型
const generalDrillDown = useGeneralDrillDownStore()
const timelineListRef = ref<InstanceType<typeof TimelineList> | null>(null)
// 用户详情数据
const userDetail = ref<UserDetailVo | null | any>(null)
// 加载状态
const loading = ref(false)
//展开更多
const isOpenMore = ref(false)

// 头部按钮显示模式：'back' | 'close'
const headerMode = computed(() => {
  const mode = generalDrillDown.headerMode
  if (mode === 'close') return mode
  return 'back'
})

const normalizeUserDetailsPayload = (input: unknown): NormalizedUserDetailsPayload => {
  const obj = input && typeof input === 'object' ? (input as any) : {}
  const rawUserId = obj.userId
  const userId =
    typeof rawUserId === 'string'
      ? rawUserId
      : typeof rawUserId === 'number'
        ? String(rawUserId)
        : undefined
  const rawQueryParams = obj.queryParams
  const queryParams =
    rawQueryParams && typeof rawQueryParams === 'object' && !Array.isArray(rawQueryParams)
      ? (rawQueryParams as Record<string, any>)
      : {}
  return { userId, queryParams }
}

const effectivePayload = computed(() => {
  return normalizeUserDetailsPayload(props.payload ?? generalDrillDown.componentData)
})

onMounted(() => {
  fetchUserDetail()
  timelineListRef.value?.fetchTabs(requestParams.value)
})

//请求参数
const requestParams = computed<VocQueryParams>(() => {
  const currentUser = effectivePayload.value
  return {
    ...(currentUser.queryParams as VocQueryParams),
    oneId: currentUser.userId
  }
})

// 负面率分布数据 - 从API数据计算得出
const badDistributionData = computed<any>(() => {
  if (!userDetail.value) return []

  const total = userDetail.value.negative + userDetail.value.positive + userDetail.value.neutral
  if (total === 0) return []

  return [
    {
      title: '正面',
      value: fmtPer((userDetail.value.positive / total) * 100)
    },
    {
      title: '中性',
      value: fmtPer((userDetail.value.neutral / total) * 100)
    },
    {
      title: '负面',
      value: fmtPer((userDetail.value.negative / total) * 100)
    }
  ]
})

//更多基本信息
const baseInfoData = computed(() => {
  return [
    { title: '手机号：', content: userDetail.value?.mobile || '-' },
    { title: '最高学历：', content: userDetail.value?.education || '-' },
    { title: '户籍地：', content: userDetail.value?.province || '-' },
    { title: '职业：', content: userDetail.value?.occupation || '-' },
    { title: '家庭月收入：', content: userDetail.value?.householdIncome || '-' },
    { title: '是否换购：', content: userDetail.value?.exchangeBuy || '-' }
  ]
})

// 类型数据 - 从API数据转换
const typeData = computed<IntentionTypeItem[]>(() => {
  if (!userDetail.value) return []

  return [
    {
      name: '抱怨',
      value: userDetail.value.complaint,
      backgroundColor: '#FEF0E5'
    },
    {
      name: '咨询',
      value: userDetail.value.consult,
      backgroundColor: '#E5FEFA'
    },
    {
      name: '建议',
      value: userDetail.value.suggest,
      backgroundColor: '#E5FAFE'
    },
    {
      name: '表扬',
      value: userDetail.value.praise,
      backgroundColor: '#E5FEEB'
    }
  ]
})

// 词云数据 - 从关注场景转换
const wordCloudData = computed(() => {
  if (!userDetail.value?.focusScenes) return []

  return userDetail.value.focusScenes.map((scene: any) => ({
    name: scene.sceneName,
    value: scene.value
  }))
})
// 发声渠道数据 - 转换为TriangleRankList需要的格式
const voiceChannelItems = computed(() => {
  if (!userDetail.value?.voiceChannels) return []

  // 为每个渠道分配颜色
  const colors = ['#3D8BFF', '#26C1FF', '#F6A200', '#FF7A45', '#7A8DCC']

  return userDetail.value.voiceChannels.map((channel: any, index: any) => ({
    ...channel,
    label: channel.channelName,
    value: channel.mentions,
    percent: channel.share,
    // 用户详情场景底部文案展示提及量，三角高度仍按提及量做相对映射。
    displayText: fmtNum(channel.mentions),
    color: colors[index % colors.length]
  }))
})

/**
 * 获取用户详情数据
 */
const fetchUserDetail = async () => {
  try {
    loading.value = true
    isOpenMore.value = false
    const currentUser = effectivePayload.value
    if (!currentUser.userId) {
      ElMessage.error('缺少用户ID参数')
      return
    }
    const response = await getUserDetail(requestParams.value)
    if (response.success) {
      userDetail.value = response.result || {}
    } else {
      ElMessage.error(response.message || '获取用户详情失败')
    }
  } catch (error) {
    console.error('获取用户详情失败:', error)
    ElMessage.error('获取用户详情失败')
  } finally {
    loading.value = false
  }
}

/**
 * 意图类型点击事件处理
 * */
const handleTypeClick = (item: any) => {
  console.log('点击意图类型:', item)
  timelineListRef.value?.fetchTabs({ ...requestParams.value, intention: item.name })
}

/**
 * 词云点击事件处理
 */
const handleWordCloudClick = (item: any) => {
  console.log('点击词云:', item) //
  timelineListRef.value?.fetchTabs({ ...requestParams.value, usageScenarioSecond: item.name || '' })
}

/**
 * 时间轴点击事件处理
 * */
const handleVoiceChannelClick = (item: any) => {
  console.log('点击时间轴:', item)
  timelineListRef.value?.updateRequestParams({ ...requestParams.value })
  timelineListRef.value?.setActive(item.channelCode)
}

/**
 * 关闭按钮点击事件处理
 * */
const closeClick = () => {
  if (props.closeBehavior === 'emit') {
    emit('close')
    return
  }
  if (headerMode.value === 'close') generalDrillDown.closeDD()
  if (headerMode.value === 'back') generalDrillDown.hideDetail()
}

watch(
  () => effectivePayload.value.userId,
  (newVal, oldVal) => {
    if (!newVal || newVal === oldVal) return
    fetchUserDetail()
    timelineListRef.value?.fetchTabs(requestParams.value)
  }
)
</script>
<template>
  <div class="h-full">
    <div class="content-header flex-between">
      <div class="flex-y-center">
        <!--        标题-->
        <div class="flex-y-center">
          <span class="ml-16 font-600 fs-20 text-primary">用户详情</span>
        </div>
      </div>
      <!-- 关闭 -->
      <div class="cursor-point" @click="closeClick">
        <el-image :src="closePng" style="width: 40px; height: 40px" />
      </div>
    </div>

    <div v-loading="loading" class="flex p-24 content-layout">
      <div class="base-info">
        <div class="flex-auto overflow-auto pr-24">
          <!--      用户信息  -->
          <div class="flex">
            <el-avatar :size="80" :src="avatarPng" fit="fill" />
            <div class="ml-16 flex-1">
              <div class="flex-y-center flex-between" style="height: 40px">
                <div class="fs-24 text-primary" style="font-weight: 600">
                  {{ userDetail?.nickname || '用户昵称' }}
                </div>
                <!--                <el-button size="large">账号绑定</el-button>-->
              </div>
              <div class="flex-y-center">
                <div class="fs-16 fw-400 color-grey">{{ userDetail?.gender || '-' }}</div>
                <el-divider direction="vertical" />
                <div class="fs-16 fw-400 color-grey">{{ userDetail?.age || '-' }}岁</div>
                <el-divider direction="vertical" />
                <div class="fs-16 fw-400 color-grey">{{ userDetail?.province || '-' }}</div>
                <el-divider direction="vertical" />
                <div class="fs-16 fw-400 color-grey">{{ userDetail?.userType || '-' }}</div>
              </div>
              <div class="mt-6 flex-between flex-y-center">
                <div class="flex-y-center">
                  <div class="flex-y-center border-class">
                    <el-image style="width: 20px; height: 20px" :src="phonePng" />
                    <div class="fs-16 fw-400 ml-4">{{ userDetail?.mobile || '未绑定' }}</div>
                  </div>
                  <div class="flex-y-center border-class ml-8">
                    <el-image style="width: 20px; height: 20px" :src="carPng" />
                    <div class="fs-16 fw-400 ml-8">{{ userDetail?.vin || '未绑定' }}</div>
                  </div>
                </div>
                <div class="cursor-point fs-16 fw-400 color-grey">
                  <div v-if="isOpenMore" @click="isOpenMore = false">
                    收起<van-icon name="arrow-up" />
                  </div>
                  <div v-else @click="isOpenMore = true">
                    查看更多<van-icon name="arrow-down" />
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div v-if="isOpenMore" class="base-info-more-layout mt-16">
            <div
              v-for="(item, index) in baseInfoData"
              :key="index"
              class="fs-14 fw-400 base-info-more-item"
            >
              <div class="color-grey-light">{{ item.title }}</div>
              <div class="text-color mt-4">{{ item.content }}</div>
            </div>
          </div>
          <el-divider />
          <!--        负面率-->
          <div>
            <div class="bg-class flex">
              <div class="image-layout">
                <el-image style="width: 24px; height: 24px" :src="thumbsDownPng" />
              </div>
              <div class="ml-16 flex-1">
                <div v-if="userDetail?.negativeRate" class="fs-20 fw-500 text-error">
                  {{ fmtPer(userDetail?.negativeRate) }}
                </div>
                <div class="flex-between flex-y-center mt-10">
                  <div class="fs-14 fw-400 text-secondary">负面率</div>
                  <div style="width: 310px; height: 12px">
                    <HorizontalSegmentedBar
                      :data="badDistributionData"
                      :showLegend="false"
                      :colors="['#3FD4A9', '#60B8EB', '#FF4B4C']"
                      height="12px"
                      @chart-click="() => {}"
                    />
                  </div>
                </div>
              </div>
            </div>
            <div class="bg-class mt-8 flex-between flex-y-center">
              <div class="flex-y-center">
                <span class="fs-14 fw-400 text-secondary">发帖数</span>
                <span class="fs-20 fw-500 text-primary ml-6">{{
                  fmtNum(userDetail?.postNum)
                }}</span>
              </div>
              <div class="flex-y-center">
                <span class="fs-14 fw-400 text-secondary">渠道数</span>
                <span class="fs-20 fw-500 text-primary ml-6">{{
                  fmtNum(userDetail?.channelNum)
                }}</span>
              </div>
            </div>
            <div class="bg-class flex mt-8">
              <div class="flex flex-y-center overflow-x-auto">
                <div class="image-layout">
                  <el-image style="width: 24px; height: 24px" :src="voiceprintFillPng" />
                </div>
                <div class="ml-16">
                  <div class="fs-20 fw-500 text-primary">
                    {{ fmtNum(userDetail?.totalMentions) }}
                  </div>
                  <div class="fs-14 fw-400 text-secondary mt-10">提及量</div>
                </div>
                <div class="ml-16 flex-1 overflow-x-auto">
                  <div class="type-list flex-y-center">
                    <div
                      v-for="(item, index) in typeData"
                      :key="index"
                      class="border-class type-item-class cursor-point"
                      :style="{ backgroundColor: item.backgroundColor }"
                      @click="handleTypeClick(item)"
                    >
                      <div class="flex-center flex-column p-8">
                        <div class="fs-20 fw-500 text-primary">{{ fmtNum(item.value) }}</div>
                        <div class="fs-14 fw-400 text-secondary mt-10">{{ item.name }}</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <!--        关注场景-->
          <div class="card-layout mt-16">
            <CommonTitle title="关注场景" />
            <div style="height: 136px">
              <WordCloudChart
                v-if="wordCloudData.length > 0"
                :data="wordCloudData"
                :color-palette="WORD_CLOUD_RANDOM_COLOR_PALETTE"
                :dim-opacity="0.8"
                :highlight-top-count="10"
                ellipse
                random-palette
                @wordClick="handleWordCloudClick"
              />
              <div v-else class="flex-center" style="height: 100%; color: #999">
                暂无关注场景数据
              </div>
            </div>
          </div>
          <!--        发声渠道-->
          <div class="card-layout mt-16">
            <CommonTitle title="发声渠道" />
            <div
              class="voice-channel-section"
              :class="{ 'voice-channel-section--empty': voiceChannelItems.length === 0 }"
            >
              <TriangleRankList
                v-if="voiceChannelItems.length > 0"
                :items="voiceChannelItems"
                @item-click="handleVoiceChannelClick"
              />
              <div v-else class="voice-channel-empty">暂无发声渠道数据</div>
            </div>
          </div>
        </div>
      </div>
      <div class="flex-1 h-full">
        <TimelineList ref="timelineListRef" />
      </div>
    </div>
  </div>
</template>
<style scoped lang="scss">
.content-header {
  display: flex;
  align-items: center;
  border-bottom: 1px solid $border-dark;
  background: #f5f7fa;
  height: 72px;
  padding: 0 24px;
  border-radius: 8px 8px 0 0;
}
.content-layout {
  height: calc(100% - 72px);
}
.base-info {
  width: 584px;
  border-right: 1px solid $border-regular;
  display: flex;
  flex-direction: column;

  .color-grey {
    color: #535862;
  }

  :deep(.el-divider--horizontal) {
    margin: 16px 0 !important;
  }

  .border-class {
    background: #ffffff;
    padding: 4px 8px;
    box-shadow: 0px 1px 2px 0px rgba(10, 13, 18, 0.05);
    border-radius: 6px 6px 6px 6px;
    border: 1px solid $border-dark;
  }

  .type-item-class {
    min-width: 74px;
  }

  .bg-class {
    background: #f5f7fa;
    border-radius: 8px;
    padding: 16px 24px;
  }

  .text-error {
    color: $color-error;
  }

  .flex-column {
    flex-direction: column;
  }

  .type-list {
    gap: 12px;
  }

  .overflow-x-auto {
    overflow-x: auto;
  }

  .card-layout {
    padding: 16px;
    border-radius: 12px;
    border: 1px solid $border-regular;
  }

  // 用户详情里的“发声渠道”需要固定内容区高度，避免不同用户数据把卡片撑成不同高度。
  .voice-channel-section {
    height: 144px;
    display: flex;
    align-items: flex-end;
  }

  .voice-channel-section--empty {
    align-items: center;
    justify-content: center;
  }

  .voice-channel-empty {
    color: #999;
  }

  .voice-channel-section :deep(.triangle-rank-list) {
    width: 100%;
  }
}
.base-info-more-layout {
  background: #f5f7fa;
  border-radius: 8px 8px 8px 8px;
  padding: 16px;
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}
.base-info-more-item {
  width: 105px;
}
.color-grey-light {
  color: #86909c;
}
.text-color {
  color: #1d2129;
}
.image-layout {
  width: 48px;
  height: 48px;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #ffffff;
  border-radius: 50%;
}
</style>
