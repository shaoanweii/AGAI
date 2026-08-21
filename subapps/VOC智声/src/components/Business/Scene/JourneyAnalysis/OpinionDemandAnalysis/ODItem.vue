<script setup lang="tsx">
import { computed, ref, nextTick, onMounted, onBeforeUnmount, watch } from 'vue'
import type { IntentionOpinionTopVo } from '@/api/journeyAnalysis/types.d'
import SortNum from '@/components/UI/SortNum/index.vue'
import { fmtNum, fmtFix, toRgba } from '@/utils'
import { sentimentColors } from '@/constants'
import ViewMore from '@/components/Business/DrillDownDialog/components/ViewMore.vue'

defineOptions({
  name: 'ODItem'
})

interface Props {
  config: {
    title: string
    titleSvg: string
    titlebg: string
    tableTitle: string
    opinionbg: string
  }
  intentionData?: IntentionOpinionTopVo
}

const { config, intentionData } = defineProps<Props>()

// 定义emits
const emit = defineEmits<{
  'sort-change': [{ intention: string; prop: string; order: string }]
  'opinion-click': [{ intention: string; type: 'table' | 'original'; data: any }]
}>()

// 处理排序变化
const handleSortChange = ({ prop, order }: { prop: string; order: string | null }) => {
  if (!order) return
  const sortOrder = order === 'ascending' ? 'asc' : 'desc'
  emit('sort-change', {
    intention: config.title,
    prop,
    order: sortOrder
  })
}

// 处理观点点击事件
const handleOpinionClick = (data: any, type: 'table' | 'original') => {
  emit('opinion-click', {
    intention: config.title,
    type,
    data
  })
}

// 表格查看更多
const handleTableViewMore = () => {
  handleOpinionClick(
    {
      __viewMore: true,
      tableTitle: config.tableTitle
    },
    'table'
  )
}

// 使用接口返回的数据类型，直接使用opinionTops作为表格数据
const tableData = computed(() => {
  return intentionData?.opinionTops || []
})

// 获取原文和标签数据
const originalSoundData = computed(() => {
  return intentionData?.originalSound || null
})

// ------------------------
// topics 显示控制：最多两行，超出显示 +N
// ------------------------
// 说明：
// - 通过测量每个 tag 的 offsetTop 来判断其所处的行数，保留前两行的标签。
// - 超出两行的标签不渲染，并在容器右下角绝对定位显示 “+N”。
// - 使用 ResizeObserver/窗口 resize 以在宽度变化时自适应重新计算。
const topicWrapRef = ref<HTMLElement | null>(null)
const visibleTopics = ref<any[]>([])
const hiddenCount = ref(0)
const topicsReady = ref(false) // 首次计算前避免闪烁

/** 计算前先渲染全部 topics，测量完成后裁剪到前两行 */
const recalcTopics = async () => {
  const all = originalSoundData.value?.topics || []
  // 若无数据，重置并返回
  if (!all.length) {
    visibleTopics.value = []
    hiddenCount.value = 0
    topicsReady.value = true
    return
  }

  // 渲染全部以便测量
  visibleTopics.value = all.slice()
  await nextTick()

  const wrap = topicWrapRef.value
  if (!wrap) {
    topicsReady.value = true
    return
  }

  const items = Array.from(wrap.querySelectorAll('.odiot-item')) as HTMLElement[]
  if (!items.length) {
    topicsReady.value = true
    return
  }

  const maxRows = 2
  const rowTopToIndex = new Map<number, number>()
  let visibleCount = 0
  for (let i = 0; i < items.length; i++) {
    const top = items[i].offsetTop
    let rowIndex: number
    if (rowTopToIndex.has(top)) {
      rowIndex = rowTopToIndex.get(top)!
    } else {
      rowIndex = rowTopToIndex.size + 1
      rowTopToIndex.set(top, rowIndex)
    }
    if (rowIndex <= maxRows) {
      visibleCount++
    } else {
      break
    }
  }

  // 裁剪并计算隐藏数量
  visibleTopics.value = all.slice(0, visibleCount)
  hiddenCount.value = Math.max(all.length - visibleCount, 0)
  topicsReady.value = true
}

// 监听 topics 变化与容器尺寸变化
let ro: ResizeObserver | null = null

onMounted(() => {
  recalcTopics()
  // ResizeObserver 优先，兼容性不足时退化为 window.resize
  const wrap = topicWrapRef.value
  if (window.ResizeObserver && wrap) {
    ro = new ResizeObserver(() => {
      // 容器尺寸变化时重新计算
      recalcTopics()
    })
    ro.observe(wrap)
  } else {
    window.addEventListener('resize', recalcTopics)
  }
})

onBeforeUnmount(() => {
  if (ro) {
    ro.disconnect()
    ro = null
  } else {
    window.removeEventListener('resize', recalcTopics)
  }
})

// 当原文数据或其 topics 发生变化时重新计算
watch(
  () => originalSoundData.value?.topics,
  () => {
    topicsReady.value = false
    recalcTopics()
  },
  { deep: true }
)

const columns = ref([
  {
    title: '观点',
    dataIndex: 'opinion',
    render: (params: any) => {
      return (
        <div
          class="flex-y-center cursor-point"
          onClick={() => handleOpinionClick(params.record, 'table')}
        >
          <SortNum rank={params.rowIndex + 1} />
          <span class="ml-8 fs-16 fw-500">{params.record.opinion}</span>
        </div>
      )
    }
  },
  {
    title: '提及量',
    dataIndex: 'mentions',
    sortable: 'custom',
    width: 100,
    render: (params: any) => {
      return (
        <div class="flex-y-center">
          <span class="mentions-number mr-16 fs-14 fw-500" style="color: #1F2733">
            {fmtNum(params.record.mentions)}
          </span>
        </div>
      )
    }
  },
  {
    title: '环比',
    dataIndex: 'mentionsMoM',
    sortable: 'custom',
    width: 100,
    render: (params: any) => {
      const value = params.record.mentionsMoM
      return (
        <div class="flex-y-center">
          <span class="mentions-number mr-16 fs-14 fw-500">{fmtFix(value)}</span>
        </div>
      )
    }
  }
])

const tableRef = ref<any>(null)

const clearSort = () => {
  tableRef.value?.clearSort()
}

defineExpose({
  clearSort
})
</script>

<template>
  <div class="od-item">
    <div class="odi-title">
      <SvgIcon name="gdpj_title" width="100%" height="84px" :color="config.titlebg"></SvgIcon>
      <div class="odit-contnet flex-x-center pt-5">
        <div class="flex-center h-64">
          <SvgIcon :name="config.titleSvg" width="28px" height="28px" color="#FFFFFF"></SvgIcon>
          <span class="oditc-title">{{ config.title }}</span>
        </div>
      </div>
    </div>

    <div class="odi-table-title mb-16">
      <span>{{ config.tableTitle }}</span>
      <ViewMore @click="handleTableViewMore" />
    </div>

    <FTable
      ref="tableRef"
      :has-hover-pop="true"
      :hoverTableRows="1"
      :columns="columns"
      :data="tableData"
      :height="330"
      class="clear-table-border"
      @sort-change="handleSortChange"
    ></FTable>

    <div
      class="odi-opinion cursor-point"
      :style="{ background: `${config.opinionbg}CC` }"
      v-if="originalSoundData"
      @click="handleOpinionClick(originalSoundData, 'original')"
    >
      <div class="odio-content" v-html="originalSoundData.content || '暂无原文数据'"></div>
      <div
        class="odio-tag-wrap"
        ref="topicWrapRef"
        :class="{ 'is-ready': topicsReady }"
        v-if="originalSoundData.topics && originalSoundData.topics.length > 0"
      >
        <template v-for="(tag, idx) in visibleTopics" :key="tag && tag.topic ? tag.topic : idx">
          <div
            v-if="tag?.topic"
            class="odiot-item"
            :style="{
              'background-color': `${toRgba(sentimentColors[tag?.sentiment], 0.1)}`,
              color: `${sentimentColors[tag?.sentiment]}`
            }"
          >
            {{ tag.topic }}
          </div>
        </template>
        <div v-if="hiddenCount > 0" class="odiot-more">+{{ hiddenCount }}</div>
      </div>

      <div class="odio-name" v-if="originalSoundData.userName">
        ——{{ originalSoundData.userName }}
      </div>
    </div>

    <!-- 如果没有原文数据，显示默认占位内容 -->
    <div class="odi-opinion" :style="{ background: `${config.opinionbg}CC` }" v-else>
      <div class="odio-content">暂无相关原文数据</div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.od-item {
  width: 100%;
  min-width: 0;
  height: 761px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0px 1px 2px 0px rgba(10, 13, 18, 0.05);
  border-radius: 12px 12px 12px 12px;
  border: 1px solid #ebedf0;
  padding: 24px;
  box-sizing: border-box;

  .odi-title {
    position: relative;
    .odit-contnet {
      position: absolute;
      left: 0;
      right: 0;
      bottom: 0;
      top: 0;

      .oditc-title {
        font-weight: 600;
        font-size: 20px;
        color: #ffffff;
        line-height: 32px;
        margin-left: 10px;
      }
    }
  }
  .odi-table-title {
    font-weight: 600;
    font-size: 16px;
    color: #333333;
    line-height: 32px;
    margin-top: 16px;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .odi-opinion {
    width: 100%;
    height: 224px;
    // background: #fef0e5;
    border-radius: 8px;
    // opacity: 0.8;
    margin-top: 16px;
    padding: 16px;

    .odio-content {
      width: 100%;
      height: 104px;
      overflow-y: auto;
      font-weight: 400;
      font-size: 16px;
      color: #1f2733;
      line-height: 24px;
    }

    .odio-tag-wrap {
      display: flex;
      flex-wrap: wrap;
      margin-top: 10px;
      gap: 8px;
      position: relative; /* 用于 +N 绝对定位 */
      opacity: 0; /* 计算前避免闪烁 */
      transition: opacity 0.2s ease;

      &.is-ready {
        opacity: 1;
      }
      .odiot-item {
        background: #ffffff;
        border-radius: 4px 4px 4px 4px;
        //border: 1px solid #dde3ee;
        padding: 6px 16px;
        font-weight: 400;
        font-size: 12px;
        color: #1f2733;
        line-height: 16px;
      }

      .odiot-more {
        position: absolute;
        right: 0;
        bottom: 0;
        background: #ffffff;
        border: 1px solid #dde3ee;
        border-radius: 4px;
        padding: 6px 10px;
        font-size: 12px;
        line-height: 16px;
        color: #5f6a7a;
        pointer-events: none; /* 让点击事件透传到父级 */
      }
    }
    .odio-name {
      font-weight: 400;
      font-size: 14px;
      color: #5f6a7a;
      line-height: 22px;
      text-align: right;
      overflow-wrap: anywhere;
      word-break: break-all;
    }
  }
}
</style>
