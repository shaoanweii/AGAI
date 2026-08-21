<script setup lang="ts">
import { computed, onMounted, ref, nextTick } from 'vue'
import { getUserDetailChannelTrajectory, getUserTrajectory } from '@/api/drillDownDialog'
import type { UserTrajectoryItem } from '@/api/drillDownDialog/types'
import { fmtNum, toRgba } from '@/utils'
import { sentimentColors } from '@/constants'

defineOptions({ name: 'TimelineList' })

// 平台 Tab（来自接口）
const platformTabs = ref<Array<{ channelName: string; voiceNum: number; channelCode?: string }>>([])

// 展示的数据集合（接口返回）
const displayListMap = ref<
  Record<string, { yearMonth: string; children: UserTrajectoryItem[] }> | undefined
>({})
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 请求参数
const requestParams = ref<any>({})

// 当前选中平台（默认“全部轨迹”）
const activeChannelCode = ref<string>('')

// 滚动容器引用
const scrollContainer = ref<HTMLElement>()
const scrollLeft = ref(0)

// 每次滚动的距离
const scrollStep = 90

// 计算是否可以向左/右滚动
const canScrollLeft = computed(() => scrollLeft.value > 0)
const canScrollRight = computed(() => {
  if (!scrollContainer.value || !platformTabs.value?.length) return false
  const maxScrollLeft = scrollContainer.value.scrollWidth - scrollContainer.value.clientWidth
  return scrollLeft.value < maxScrollLeft
})

// 向左滚动
const scrollLeftHandler = () => {
  if (!scrollContainer.value) return
  const newScrollLeft = Math.max(0, scrollLeft.value - scrollStep)
  scrollContainer.value.scrollTo({ left: newScrollLeft, behavior: 'smooth' })
}

// 向右滚动
const scrollRightHandler = () => {
  if (!scrollContainer.value) return
  const maxScrollLeft = scrollContainer.value.scrollWidth - scrollContainer.value.clientWidth
  const newScrollLeft = Math.min(maxScrollLeft, scrollLeft.value + scrollStep)
  scrollContainer.value.scrollTo({ left: newScrollLeft, behavior: 'smooth' })
}

// 监听滚动事件，更新滚动位置
const handleScroll = () => {
  if (scrollContainer.value) {
    scrollLeft.value = scrollContainer.value.scrollLeft
  }
}

// 使当前选中的渠道Tab在可视范围内
const scrollActiveIntoView = () => {
  const container = scrollContainer.value
  if (!container) return
  const activeEl = container.querySelector('.timeline-tabs__item.tab-active') as HTMLElement | null
  if (!activeEl) return

  const elementLeft = activeEl.offsetLeft
  const elementRight = elementLeft + activeEl.offsetWidth
  const viewLeft = container.scrollLeft
  const viewRight = viewLeft + container.clientWidth

  let newScrollLeft = viewLeft
  if (elementLeft < viewLeft) {
    newScrollLeft = elementLeft
  } else if (elementRight > viewRight) {
    newScrollLeft = elementRight - container.clientWidth
  }

  if (newScrollLeft !== viewLeft) {
    container.scrollTo({ left: newScrollLeft, behavior: 'smooth' })
  }
}

const setActive = (code?: string) => {
  activeChannelCode.value = code || ''
  pageNum.value = 1
  fetchTimeline()
  // 选中后保证标签可见（等待DOM更新选中态）
  nextTick(() => scrollActiveIntoView())
}

//更新请求参数方法
const updateRequestParams = (params: any) => {
  requestParams.value = params
}

//更新
const refreshTimeline = (params: any) => {
  requestParams.value = params
  pageNum.value = 1
  fetchTimeline()
}

/**
 * 分页大小改变
 */
const handleSizeChange = (size: number) => {
  pageSize.value = size
  pageNum.value = 1
  fetchTimeline()
}

/**
 * 当前页改变
 */
const handleCurrentChange = (page: number) => {
  pageNum.value = page
  fetchTimeline()
}

// 加载渠道Tab
const loadingTabs = ref(false)
const fetchTabs = async (params: any) => {
  if (!params.oneId) return
  loadingTabs.value = true
  try {
    requestParams.value = params
    const response = await getUserDetailChannelTrajectory(params)
    platformTabs.value = response?.result || []
    // 默认选中“全部轨迹”
    //默认选中第一个
    setActive(platformTabs.value[0]?.channelCode)
  } catch (err) {
    console.error('加载用户渠道轨迹失败', err)
    platformTabs.value = []
    activeChannelCode.value = ''
  } finally {
    loadingTabs.value = false
  }
}

// 加载时间线
const loadingList = ref(false)
const fetchTimeline = async () => {
  loadingList.value = true
  displayListMap.value = {} // 重置数据
  try {
    const response = await getUserTrajectory({
      ...requestParams.value,
      channelCode: activeChannelCode.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    let list = response.result?.list || []
    list.forEach((item: UserTrajectoryItem) => {
      if (displayListMap.value) {
        if (displayListMap.value[item.yearMonth]) {
          displayListMap.value[item.yearMonth].children.push(item)
        } else {
          displayListMap.value[item.yearMonth] = {
            yearMonth: item.yearMonth,
            children: [item]
          }
        }
      }
    })
    total.value = response.result?.total || 0
  } catch (err) {
    console.error('加载用户时间线失败', err)
    displayListMap.value = {}
  } finally {
    loadingList.value = false
  }
}

onMounted(() => {
  // 初始化滚动位置
  scrollLeft.value = 0
})

defineExpose({
  fetchTabs,
  setActive,
  updateRequestParams,
  refreshTimeline
})
</script>

<template>
  <div class="timeline-list h-full flex-col">
    <!-- 平台 Tab -->
    <div class="flex-y-center">
      <div
        ref="scrollContainer"
        @scroll="handleScroll"
        class="timeline-tabs flex-y-center ml-24 mr-24"
      >
        <div
          v-for="tab in platformTabs"
          :key="tab.channelCode"
          class="timeline-tabs__item cursor-point fs-16 flex-center"
          :class="[
            activeChannelCode === (tab.channelCode || '')
              ? 'text-primary fw-700 tab-active'
              : 'fw-500'
          ]"
          @click="setActive(tab.channelCode)"
        >
          <span class="name">{{ tab.channelName }}</span>
          <span class="ml-8">{{ fmtNum(tab.voiceNum) }}</span>
        </div>
      </div>
      <template v-if="canScrollLeft || canScrollRight">
        <div class="arrow" :class="{ disabled: !canScrollLeft }" @click="scrollLeftHandler">
          <el-icon color="#929AA6"><CaretLeft /></el-icon>
        </div>
        <div class="arrow ml-4" :class="{ disabled: !canScrollRight }" @click="scrollRightHandler">
          <el-icon color="#929AA6"><CaretRight /></el-icon>
        </div>
      </template>
    </div>
    <div class="timeline-list__container flex-col pt-16 pr-24 pl-32">
      <el-skeleton animated :count="3" v-if="loadingList" />
      <template v-else-if="total">
        <div class="flex-auto overflow-auto">
          <div v-for="(items, yearMonth) in displayListMap" :key="yearMonth">
            <div class="fs-16 fw-500 text-primary">{{ yearMonth }}</div>
            <el-timeline class="pt-24">
              <el-timeline-item
                v-for="(activity, index) in items.children"
                :key="activity.originalId || index"
              >
                <div class="flex">
                  <div class="fs-16 fw-500 text-primary pb-24">{{ activity.monthDay }}</div>
                  <div class="flex-1 ml-24">
                    <div class="flex-baseline">
                      <div
                        v-if="activity.channelName"
                        class="type-class flex-center fs-14 fw-500 text-primary"
                      >
                        {{ activity.channelName }}
                      </div>
                      <div
                        v-if="activity.title"
                        class="ml-8 fs-16 fw-500 text-primary flex-1 line-height-22"
                      >
                        {{ activity.title }}
                      </div>
                    </div>
                    <div class="details-content-class mt-16 p-16">
                      <div
                        class="fs-14 fw-400 content-text line-height-22"
                        v-html="activity.content"
                      ></div>
                      <div class="flex-y-center flex-wrap mt-16 tag-layout">
                        <template v-for="(tag, index) in activity.topics || []" :key="index">
                          <div
                            class="tag-item"
                            :style="{
                              'background-color': `${toRgba(sentimentColors[tag?.sentiment], 0.1)}`,
                              color: `${sentimentColors[tag?.sentiment]}`
                            }"
                          >
                            {{ tag.topic }}
                          </div>
                        </template>
                      </div>
                    </div>
                  </div>
                </div>
              </el-timeline-item>
            </el-timeline>
          </div>
        </div>
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="->,total, prev, pager, next, sizes"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          class="mt-4"
        />
      </template>
      <div v-else-if="total === 0" class="pt-24 pb-24">
        <el-empty description="暂无内容" />
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.timeline-tabs {
  width: 610px;
  overflow-x: auto;
  white-space: nowrap;
  gap: 24px;

  .fw-700 {
    font-weight: 700;
  }

  &__item {
    height: 64px;
  }

  .tab-active {
    position: relative;

    &::before {
      content: '';
      position: absolute;
      bottom: 0;
      left: 0;
      width: 100%;
      height: 2px;
      background-color: #1677ff;
    }
  }
}

.timeline-list__container {
  height: calc(100% - 64px);
}

.arrow {
  width: 24px;
  height: 24px;
  background: #f2f4f7;
  border-radius: 3px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  z-index: 10;
  transition: all 0.3s ease;

  &:hover:not(.disabled) {
    background: #e6f7ff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }

  &.disabled {
    opacity: 0.5;
    cursor: not-allowed;
    pointer-events: none;
  }
}

.type-class {
  padding: 0 7px;
  height: 24px;
  background: #eaf3ff;
  border-radius: 4px;
  border: 1px solid $border-dark;
}

.details-content-class {
  background: #f5f7fa;
  border-radius: 8px;

  .content-text {
    color: #4b5468;
  }

  .tag-layout {
    gap: 8px;

    .tag-item {
      padding: 6px 16px;
      background: #ffffff;
      border-radius: 4px;
      //border: 1px solid #DDE3EE;
    }
  }
}
</style>
