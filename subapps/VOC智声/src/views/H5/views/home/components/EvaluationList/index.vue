<script setup lang="ts">
import { computed, reactive } from 'vue'
import { showToast } from 'vant'
import flagPng from '@/assets/h5/flag.png'
import { fmtNum, fmtFix, toRgba } from '@/utils'

defineOptions({
  name: 'EvaluationList'
})

export interface EvaluationListProps {
  /** 标题 */
  title?: string
  /** 背景色 */
  backgroundColor?: string
  /** 图片 */
  image?: string
  /** 规点数据列表 */
  evaluationData?: Array<{
    /** 观点名 */
    opinion: string
    /** 提及量 */
    mentions: number
    /** 提及量环比，% 两位小数 */
    mentionsMoM: number
    /** 提及量同比，% 两位小数 */
    mentionsYoY: number
    /** 观点 */
    remark: string[]
  }>
  /** 声音类型 */
  intention?: string
}

const props = withDefaults(defineProps<EvaluationListProps>(), {
  backgroundColor: '#ff8a8b',
  evaluationData: () => []
})

const emits = defineEmits(['jumpVoice'])

const hub = reactive({
  isOpen: false, //是否展开
  defaultMax: 5 //默认最大显示数量
})

//显示列表 - 优先使用传入的数据，如果没有则使用默认数据
const displayList = computed(() => {
  // 使用传入的数据或默认数据
  const sourceData =
    props.evaluationData && props.evaluationData.length > 0
      ? props.evaluationData.map((item: any) => ({
          ...item,
          opinion: item.opinion || '',
          mentions: fmtNum(item.mentions),
          mentionsMoM: `${fmtFix(item.mentionsMoM)}`,
          mentionsYoY: `${fmtFix(item.mentionsYoY)}`
        }))
      : []

  // 如果默认不展开，实际数据超过了默认最大显示数量，则只显示默认最大数量
  let result = []
  if (hub.isOpen) {
    result = sourceData.slice(0, Math.min(10, sourceData.length))
  } else {
    result = sourceData.slice(0, Math.min(hub.defaultMax, sourceData.length))
  }
  return result
})

const opinionEvent = (item: any) => {
  if (item.remark) {
    showToast(item.remark.join('\n'))
  }

  emits('jumpVoice', { ...item, intention: props.intention })
}
</script>
<template>
  <!--  v-if="displayList.length" -->
  <div
    class="component-layout"
    :style="`background: linear-gradient( 180deg, ${toRgba(backgroundColor, 0.2)} 0%, rgba(255,237,234,0) 100%);`"
  >
    <div class="title-layout flex-center" :style="`background: ${toRgba(backgroundColor, 0.5)}`">
      <van-image width="24" height="24" :src="image" fit="cover" radius="24" />
      <div class="ml-8 title-class">{{ title }}</div>
    </div>
    <div class="table-layout">
      <div class="table-header-layout">
        <div class="table-header-item flex-1">观点</div>
        <div class="table-header-item mation-width text-center">提及量</div>
        <div class="table-header-item mation-width text-center">环比</div>
      </div>
      <div class="table-column-layout">
        <div v-for="(item, index) in displayList" :key="index" class="table-row-item flex-y-center">
          <div
            class="table-column-item flex-1 fs-14 fw-400 flex-y-center single-line-ellipsis"
            @click="opinionEvent(item)"
          >
            <div class="single-line-ellipsis" style="max-width: calc(100% - 20px)">
              {{ item.opinion }}
            </div>
            <van-image
              v-if="item.remark"
              class="ml-4"
              width="14"
              height="14"
              :src="flagPng"
              fit="cover"
            />
          </div>
          <div class="table-column-item mation-width fs-14 fw-500 text-primary text-center">
            {{ item.mentions || 0 }}
          </div>
          <div class="table-column-item mation-width fs-14 fw-500 color-grey text-center">
            {{ item.mentionsMoM }}
          </div>
        </div>
      </div>
    </div>
    <div
      v-if="
        (props.evaluationData && props.evaluationData.length > 0
          ? props.evaluationData.length
          : 0) > hub.defaultMax
      "
      class="load-more flex-center pb-8"
    >
      <div v-if="hub.isOpen" @click="hub.isOpen = false">点击收起<van-icon name="arrow-up" /></div>
      <div v-else @click="hub.isOpen = true">点击加载更多<van-icon name="arrow-down" /></div>
    </div>
  </div>
</template>
<style lang="scss" scoped>
.component-layout {
  //background: linear-gradient( 180deg, #F5E6E6 0%, rgba(255,237,234,0) 100%);
  border-radius: 8px 8px 8px 8px;
  padding: 8px;
  .title-layout {
    height: 40px;
    //background: rgba(255,138,139,0.5);
    border-radius: 4px 4px 4px 4px;
  }
  .title-class {
    font-weight: 600;
    font-size: 16px;
    color: #333333;
  }
  .table-layout {
    .table-header-layout {
      height: 40px;
      padding: 0 16px;
      display: flex;
      align-items: center;
      border-bottom: 1px solid #dfe2e8;
      font-weight: 500;
      font-size: 14px;
      color: #5f6a7a;
    }
    .table-row-item {
      padding: 0 16px;
      height: 42px;
    }
    .table-column-item {
      //display: flex;
      //align-items: center;
    }
  }
  .text-center {
    text-align: center;
  }
  .load-more {
    font-weight: 400;
    font-size: 12px;
    color: #929aa6;
  }
  .color-grey {
    color: #666;
  }
  .mation-width {
    width: 80px;
  }
}
</style>
