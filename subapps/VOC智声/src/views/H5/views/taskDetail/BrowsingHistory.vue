<script setup lang="ts">
import type { BrowseRecordVo } from '@h5/api/home/types'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { computed } from 'vue'
import { toRgba } from '@/utils'
import { sentimentColors } from '@/constants'

defineOptions({
  name: 'BrowsingHistory'
})

const router = useRouter()

interface Props {
  data?: BrowseRecordVo[]
}

const props = defineProps<Props>()

const emit = defineEmits(['itemClick'])

// 时间轴
const diaplayList: any = computed(() => {
  const resultMap: any = {}
  props.data?.map((item) => {
    const date = dayjs(item.dateTime).format('YYYY-MM-DD')
    if(resultMap[date]){
      resultMap[date].list.push(item)
    } else {
      const isToday = dayjs(item.dateTime).isSame(dayjs(), 'day')
      const displayDate = isToday ? '今天' : dayjs(item.dateTime).format('MM-DD')
      resultMap[date] = { date, displayDate, list: [item] }
    }
  })
  return resultMap
})

// 格式化时间显示
const formatTime = (dateTime?: string): any => {
  if (!dateTime) return ''
  return dayjs(dateTime).format('HH:mm')
}

//跳转详情
const handleItemClick = (item: BrowseRecordVo) => {
  emit('itemClick', item)
  // router.push({
  //   name: 'H5VoiceDetail',
  //   query: { id: item.soundId, originalId: item.originalId }
  // })
}
</script>

<template>
  <div class="browsing-history">
    <div class="flex-col" v-for="(group, groupIndex) in diaplayList" :key="groupIndex">
      <div class="day">{{ group.displayDate }}</div>
      <div style="margin-top: -14px">
        <div class="bh-item" v-for="(item, index) in group.list" :key="`${item.soundId}${index}`">
          <div class="left">
            <div class="time1 mt-15">{{ formatTime(item.dateTime) }}</div>
            <div class="lline"></div>
          </div>
          <div class="right" @click="handleItemClick(item)">
            <div class="time">
              <van-icon name="clock-o" />
              <span>浏览时长：</span>
              <span>{{ item.browseDuration || '0秒' }}</span>
            </div>

            <div class="content multi-line-ellipsis" v-html="item.originalTexTScene|| '暂无内容'"></div>

            <div class="tag-wrap" v-if="item.topics && item.topics.length">
              <template  v-for="(topic, idx) in item.topics" :key="idx">
                <div
                  v-if="topic.topic"
                  class="tag"
                  :style="{'background-color': `${toRgba(sentimentColors[topic?.sentiment], 0.1)}`, color: `${sentimentColors[(topic?.sentiment)]}`}"
                >{{ topic.topic}}</div>
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<style lang="scss" scoped>
.browsing-history {
  padding: 12px 12px 0;

  .day {
    font-weight: 400;
    font-size: 14px;
    color: #000000;
    line-height: 20px;
  }
  .bh-item {
    display: flex;

    &:last-child {
      .left {
        .lline {
          display: none;
        }
      }

      .right {
        border-bottom: none;
      }
    }
    .left {
      width: 30px;
      flex: none;
      margin-right: 10px;
      position: relative;
      .time1 {
        font-weight: 400;
        font-size: 12px;
        color: #5f6a7a;
        line-height: 20px;
      }

      .lline {
        width: 2px;
        height: 80%;
        background: #f1f1f5;
        position: absolute;
        left: 50%;
        top: 40px;
        transform: translateX(-50%);
      }
    }
    .right {
      flex: 1;
      min-width: 0;
      padding: 16px 12px;
      border-bottom: 1px solid #e9eaeb;

      .time {
        padding: 2px 6px;
        background: #f2f4f7;
        border-radius: 20px 20px 20px 20px;
        display: inline-flex;
        align-items: center;
        span:nth-child(2) {
          font-weight: 400;
          font-size: 14px;
          color: #5f6a7a;
          line-height: 20px;
          margin-left: 4px;
        }
        span:nth-child(3) {
          font-weight: 400;
          font-size: 14px;
          color: #1f2733;
          line-height: 20px;
        }
      }

      .multi-line-ellipsis {
        display: -webkit-box;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 2; /* 显示的最大行数 */
        overflow: hidden; /* 隐藏超出部分 */
      }

      /* iOS 兼容增强：修复个别设备出现“已出现省略号但未裁剪”的问题 */
      .multi-line-ellipsis {
        line-clamp: 2; /* 标准属性，兼容新版本浏览器 */
        max-height: 40px; /* 与 line-height: 20px 保持一致，2 行 = 40px */
        word-break: break-word; /* 防止长词或英文导致换行异常 */
      }

      /* v-html 内容常含 <p>/<br>，在 iOS 上会破坏 -webkit-line-clamp 计算，转为内联消除影响 */
      .multi-line-ellipsis :deep(p) {
        display: inline;
        margin: 0;
      }

      .multi-line-ellipsis :deep(br) {
        display: inline;
      }

      .multi-line-ellipsis :deep(div) {
        display: inline;
      }

      .content {
        font-weight: 400;
        font-size: 14px;
        color: #535862;
        line-height: 20px;
        margin-top: 8px;
      }

      .tag-wrap {
        display: flex;
        align-items: center;
        flex-wrap: wrap;
        gap: 8px;
        margin-top: 8px;
        .tag {
          border-radius: 4px;
          padding: 4px 12px;
          font-weight: 400;
          font-size: 12px;
          background: #FEE9E5;
          color: #FF4B4C;
          line-height: 16px;

          // &:not(:first-child) {
          //   margin-left: 8px;
          // }
        }
      }
    }
  }
}
</style>
