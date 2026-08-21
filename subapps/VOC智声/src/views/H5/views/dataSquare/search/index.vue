<script setup lang="ts">
import { computed, nextTick, onActivated, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import HPage from '@h5/components/UI/HPage'
import { useKeepAliveScroll } from '@h5/hooks/useKeepAliveScroll'
import { searchH5DataSquareReports, type H5DataSquareReportItem } from '@h5/api/dataSquare'
import DataSquareReportRow from '../components/DataSquareReportRow.vue'
import emptyImage from '@/assets/h5/nodata.png'

defineOptions({
  name: 'H5DataSquareSearch'
})

const route = useRoute()
const router = useRouter()

const pageRef = ref<InstanceType<typeof HPage> | null>(null)
const searchInputRef = ref()

// 搜索条件不同则使用独立滚动缓存，避免新搜索复用旧结果位置
useKeepAliveScroll({
  getCacheKey: currentRoute => String(currentRoute.fullPath || currentRoute.name || '')
})

const hub = reactive({
  keyword: String(route.query.keyword || ''),
  brandCode: String(route.query.brandCode || ''),
  categoryId: String(route.query.categoryId || ''),
  fetching: false,
  loading: false,
  finished: true,
  searched: false,
  pageNum: 1,
  pageSize: 10,
  total: 0,
  list: [] as H5DataSquareReportItem[]
})

const isEmpty = computed(() => hub.searched && !hub.loading && hub.list.length === 0)

/**
 * 生成搜索页路由参数，确保搜索条件可恢复。
 * @param keyword 搜索关键词
 */
const buildSearchQuery = (keyword: string) => ({
  brandCode: hub.brandCode || undefined,
  categoryId: hub.categoryId || undefined,
  keyword: keyword || undefined
})

/**
 * 同步搜索条件到 URL，便于返回、刷新或重新进入时恢复搜索结果。
 * @param keyword 搜索关键词
 * @returns 是否发生了路由参数更新
 */
const syncSearchQuery = (keyword: string) => {
  const currentKeyword = String(route.query.keyword || '')
  if (currentKeyword === keyword) return false

  void router.replace({
    name: 'H5DataSquareSearch',
    query: buildSearchQuery(keyword)
  })
  return true
}

/**
 * 获取 HPage 内部滚动容器，保证 van-list 监听真实滚动区域。
 */
const getScroller = () => pageRef.value?.getScrollContainer?.() || undefined

/**
 * 重置搜索分页状态。
 */
const resetSearchState = () => {
  hub.pageNum = 1
  hub.total = 0
  hub.list = []
  hub.finished = false
}

/**
 * 重置为未搜索状态。
 * 无关键词的缓存实例重新展示时，必须清理上次输入遗留。
 */
const resetIdleSearchState = () => {
  hub.keyword = ''
  hub.searched = false
  hub.fetching = false
  hub.loading = false
  hub.finished = true
  hub.list = []
  hub.total = 0
}

/**
 * 执行报告名称搜索。
 * @param reset 是否重置分页
 */
const fetchSearchList = async (reset = false) => {
  const keyword = hub.keyword.trim()
  if (!keyword) {
    hub.searched = false
    hub.finished = true
    hub.list = []
    hub.total = 0
    return
  }

  if (reset) {
    resetSearchState()
  }

  if (hub.fetching && !reset) return
  if (hub.finished) return

  hub.fetching = true
  hub.loading = true
  hub.searched = true
  try {
    const res = await searchH5DataSquareReports(
      {
        pageNum: hub.pageNum,
        pageSize: hub.pageSize,
        brandCode: hub.brandCode,
        keyword,
        categoryId: hub.categoryId
      },
      { cancelPrevious: true }
    )

    const page = res.result
    const rows = page?.list || []
    hub.total = page?.total || 0
    hub.list = hub.pageNum === 1 ? rows : [...hub.list, ...rows]

    hub.finished =
      hub.list.length >= hub.total || rows.length < hub.pageSize || Boolean(page?.isLastPage)
    if (!hub.finished) {
      hub.pageNum += 1
    }
  } catch (error) {
    console.error('搜索看数广场报告失败:', error)
    hub.finished = true
    if (hub.pageNum === 1) {
      hub.list = []
      hub.total = 0
    }
  } finally {
    hub.fetching = false
    hub.loading = false
  }
}

/**
 * 提交搜索关键词。
 */
const handleSearch = () => {
  const keyword = hub.keyword.trim()
  if (!keyword) {
    showToast('请输入报告名称')
    return
  }

  if (syncSearchQuery(keyword)) return

  void fetchSearchList(true)
}

/**
 * 清空关键词后回到未搜索态。
 */
const handleClear = () => {
  resetIdleSearchState()
  syncSearchQuery('')
}

/**
 * 取消搜索并返回上一页。
 */
const handleCancel = () => {
  router.back()
}

/**
 * van-list 上拉加载下一页。
 */
const handleLoad = () => {
  void fetchSearchList()
}

/**
 * 打开报告详情页。
 * @param report 当前报告
 */
const handleReportClick = (report: H5DataSquareReportItem) => {
  router.push({
    name: 'H5DataSquareReportDetail',
    query: {
      reportId: report.reportId,
      reportName: report.reportName || ''
    }
  })
}

if (hub.keyword.trim()) {
  void fetchSearchList(true)
} else {
  nextTick(() => {
    searchInputRef.value?.focus?.()
  })
}

onActivated(() => {
  const routeKeyword = String(route.query.keyword || '')
  if (hub.keyword === routeKeyword) return

  if (!routeKeyword) {
    resetIdleSearchState()
    nextTick(() => {
      searchInputRef.value?.focus?.()
    })
    return
  }

  hub.keyword = routeKeyword
  void fetchSearchList(true)
})
</script>

<template>
  <HPage ref="pageRef" class="data-square-search" background-color="#f5f7fa">
    <template #nav-bar>
      <div class="data-square-search__bar">
        <van-search
          ref="searchInputRef"
          v-model="hub.keyword"
          placeholder="请输入报告名称搜索"
          shape="round"
          maxlength="30"
          clearable
          autofocus
          @search="handleSearch"
          @clear="handleClear"
        />
        <button class="data-square-search__cancel" type="button" @click="handleCancel">取消</button>
      </div>
    </template>

    <div v-if="hub.keyword" class="data-square-search__content">
      <div class="data-square-search__panel">
        <van-list
          v-if="hub.list.length > 0"
          v-model:loading="hub.loading"
          :finished="hub.finished"
          :finished-text="''"
          :scroller="getScroller()"
          @load="handleLoad"
        >
          <div class="data-square-search__list">
            <DataSquareReportRow
              v-for="report in hub.list"
              :key="report.reportId"
              :report="report"
              @click="handleReportClick"
            />
          </div>
        </van-list>
        <div v-else-if="isEmpty" class="data-square-search__empty">
          <van-empty :image="emptyImage" image-size="112" description="暂无相关搜索结果">
            <template #description>
              <div class="data-square-search__empty-title">暂无相关搜索结果</div>
              <div class="data-square-search__empty-sub">您可以尝试更换关键词搜索</div>
            </template>
          </van-empty>
        </div>
      </div>
    </div>
  </HPage>
</template>

<style scoped lang="scss">
.data-square-search {
  &__bar {
    padding: 12px;
    display: grid;
    grid-template-columns: 1fr 38px;
    gap: 10px;
    align-items: center;

    :deep(.van-search) {
      padding: 0;
      background: transparent;
    }

    :deep(.van-search__content) {
      height: 32px;
      border: 1px solid #e1e6ee;
      background: #ffffff;
    }
    :deep(.van-search__field) {
      height: 32px;
    }
  }

  &__cancel {
    height: 32px;
    padding: 0;
    border: 0;
    background: transparent;
    color: #5f6a7a;
    font-size: 13px;
    line-height: 18px;
  }

  &__content {
    min-height: 100%;
    padding: 0 12px 12px;
  }

  &__panel {
    min-height: calc(100vh - 152px);
    border-radius: 8px;
    background: #ffffff;
    overflow: hidden;
  }

  &__list {
    display: grid;
    gap: 13px;
    padding: 13px 12px;
  }

  &__empty {
    height: calc(100vh - 152px);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__empty-title {
    color: #5f6a7a;
    font-size: 13px;
    line-height: 20px;
    text-align: center;
  }

  &__empty-sub {
    margin-top: 4px;
    color: #a0a8b5;
    font-size: 12px;
    line-height: 18px;
  }
}
</style>
