<script setup lang="ts">
import {} from 'vue'
import dayjs from 'dayjs'
import type { VoiceListItem } from '@h5/api/home/types'
import type { VoiceListProps, VoiceListEmits } from './types'
import { useRouter } from 'vue-router'
import { toRgba } from '@/utils'
import { sentimentColors } from '@/constants'

defineOptions({ name: 'HVoiceList' })

const router = useRouter()

const props = withDefaults(defineProps<VoiceListProps>(), {
  voiceList: () => [],
  isLoadMore: false,
  loading: true
})

const emit = defineEmits<VoiceListEmits>()

const loadMore = () => {
  // 触底加载更多事件，交由父组件拉取数据
  emit('load-more')
}

const handleItemClick = (item: VoiceListItem) => {
  emit('item-click', item)
}

// 标题超过 15 个字显示省略号
const formatTitle = (title: string | undefined) => {
  return title && title.length > 15 ? title.substring(0, 15) + '...' : title
}
</script>
<template>
  <div>
    <!-- infinite=true 时，内部接管滚动加载与完成提示 -->
    <!-- 列表初次加载骨架屏 -->
    <template v-if="(!voiceList || voiceList.length === 0) && loading">
      <van-skeleton class="mt-10" title :row="5" />
    </template>
    <van-list v-else :loading="loading" :finished="!isLoadMore" @load="loadMore">
      <div class="voice-list__items">
        <div
          v-for="(item, index) in voiceList"
          :key="`${item.id}-${index}`"
          class="p-16 voice-list__item"
          @click="handleItemClick(item)"
        >
          <!-- 标题 -->
          <div
            class="fs-14 fw-500 text-primary mt-8 title-class single-line-ellipsis"
            v-if="item.title && item.title !== '-'"
          >
            {{ item.title }}
          </div>
          <!--声音片段内容-->
          <div class="voice-content-class mt-8">
            <div
              class="fs-14 fw-400 text-secondary two-line-ellipsis title-class"
              v-html="item.content"
              v-if="item.content"
            ></div>
          </div>
          <!--观点标签-->
          <div
            class="voice-list__tags flex flex-wrap mt-10"
            v-if="item.topics && item.topics.length"
          >
            <template v-for="(topic, idx) in item.topics" :key="idx">
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
          <div class="flex-y-center mt-10" style="white-space: nowrap">
            <template v-if="item.custName">
              <div class="text-secondary fs-12">{{ item.custName }}</div>
              <div class="divider-right"></div>
            </template>

            <template v-if="item.channel">
              <div class="text-secondary single-line-ellipsis fs-12">{{ item.channel }}</div>
              <div class="divider-right"></div>
            </template>
            <template v-if="item.dataCreateTime">
              <div class="text-secondary fs-12">{{ item.dataCreateTime }}</div>
            </template>
          </div>
          <!-- 品牌和车系信息（如需展示可放开） -->
          <!--
          <div class="flex-y-center mt-8" v-if="item.brand || item.carSeries">
            <div class="text-secondary fs-12">{{ item.brand || '' }} {{ item.carSeries || '' }}</div>
            <div class="text-secondary fs-12" v-if="item.brand">{{ item.brand }}</div>
            <div class="divider-right" v-if="item.brand && item.carSeries"></div>
            <div class="text-secondary fs-12" v-if="item.carSeries">{{ item.carSeries }}</div>
          </div>
          -->
        </div>
      </div>
      <template #finished>
        <div class="flex-center">
          <van-divider class="finish-text">已显示全部原声</van-divider>
        </div>
      </template>
    </van-list>
  </div>
</template>
<style scoped lang="scss">
::v-deep(.highlight) {
  color: #1677ff;
}
.voice-list__items {
  background-color: white;
}
.voice-list__item {
  border-bottom: 1px solid #ebebeb;
  font-weight: 400;
  font-size: 14px;
  &:last-child {
    border-bottom: none;
  }
}
.title-class {
  line-height: 20px;
}

.voice-content-class {
  max-height: 40px;
  overflow: hidden;
}
.divider-right {
  margin: 0 10px;
  width: 1px;
  height: 10px;
  border-right: 1px solid #929aa6;
}
.two-line-ellipsis {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2; /* 显示的最大行数 */
  overflow: hidden; /* 隐藏超出部分 */
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

.finish-text {
  width: 160px;
  margin: 0 !important;
  text-align: center;
  font-weight: 400;
  font-size: 12px;
  color: #929aa6;
}

/* iOS 兼容增强：修复个别设备出现“已出现省略号但未裁剪”的问题 */
.two-line-ellipsis {
  line-clamp: 2; /* 标准属性，兼容新版本浏览器 */
  max-height: 40px; /* 与 line-height: 20px 保持一致，2 行 = 40px */
  word-break: break-word; /* 防止长词或英文导致换行异常 */
}

/* v-html 内容常含 <p>/<br>，在 iOS 上会破坏 -webkit-line-clamp 计算，转为内联消除影响 */
.two-line-ellipsis :deep(p) {
  display: inline;
  margin: 0;
}

.two-line-ellipsis :deep(br) {
  display: inline;
}

.two-line-ellipsis :deep(div) {
  display: inline;
}
</style>
