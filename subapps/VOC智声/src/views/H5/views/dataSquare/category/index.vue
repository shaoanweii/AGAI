<script setup lang="ts">
import { computed, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import HPage from '@h5/components/UI/HPage'
import { useKeepAliveScroll } from '@h5/hooks/useKeepAliveScroll'
import {
  getH5DataSquareCategoryReportList,
  type H5DataSquareCategoryItem,
  type H5DataSquareReportItem
} from '@h5/api/dataSquare'
import DataSquareReportRow from '../components/DataSquareReportRow.vue'
import defaultIcon from '@/assets/images/system/dataSquare/default.png'
import defaultBg from '@/assets/images/system/dataSquare/default-bg.png'

defineOptions({
  name: 'H5DataSquareCategory'
})

const route = useRoute()
const router = useRouter()

// 分类不同则使用独立滚动缓存，避免返回时复用其他分类位置
useKeepAliveScroll({
  getCacheKey: currentRoute => String(currentRoute.fullPath || currentRoute.name || '')
})

const hub = reactive({
  loading: false,
  loadMoreLoading: false,
  pageNum: 1,
  pageSize: 10,
  total: 0,
  finished: false,
  category: null as H5DataSquareCategoryItem | null,
  list: [] as H5DataSquareReportItem[]
})

const categoryId = computed(() => String(route.query.categoryId || ''))
const detailImage = computed(() => hub.category?.detailImageURL || defaultBg)
const hasDetailImage = computed(() => Boolean(detailImage.value))
const showLoadMore = computed(() => !hub.loading && hub.list.length > 0 && !hub.finished)

/**
 * 获取分类详情与报告列表。
 * @param reset 是否重置分页
 */
const fetchCategoryReports = async (reset = false) => {
  if (!categoryId.value) {
    showToast('分类信息缺失')
    router.back()
    return
  }

  if (reset) {
    hub.pageNum = 1
    hub.total = 0
    hub.finished = false
    hub.list = []
  }

  if (hub.loading || hub.loadMoreLoading || hub.finished) return

  const isFirstPage = hub.pageNum === 1
  if (isFirstPage) {
    hub.loading = true
  } else {
    hub.loadMoreLoading = true
  }

  try {
    const res = await getH5DataSquareCategoryReportList({
      categoryId: categoryId.value,
      pageNum: hub.pageNum,
      pageSize: hub.pageSize
    })
    const result = res.result
    hub.category = result?.category || hub.category
    const page = result?.reports
    const rows = page?.list || []
    hub.total = page?.total || hub.category?.reportCount || 0
    hub.list = hub.pageNum === 1 ? rows : [...hub.list, ...rows]
    hub.finished =
      hub.list.length >= hub.total || rows.length < hub.pageSize || Boolean(page?.isLastPage)
    if (!hub.finished) {
      hub.pageNum += 1
    }
  } catch (error) {
    console.error('获取看数广场分类报告失败:', error)
    hub.finished = true
    if (hub.pageNum === 1) {
      hub.list = []
      hub.total = 0
    }
  } finally {
    hub.loading = false
    hub.loadMoreLoading = false
  }
}

/**
 * 点击查看更多报告。
 */
const handleLoadMore = () => {
  void fetchCategoryReports()
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

void fetchCategoryReports(true)
</script>

<template>
  <HPage class="data-square-category" background-color="#f5f7fa">
    <template #nav-bar>
      <div
        v-if="hasDetailImage && hub.list.length > 0"
        class="data-square-category__hero"
        :style="{ backgroundImage: `url(${detailImage})` }"
      >
        <div class="data-square-category__hero-title">
          <span class="data-square-category__folder">
            <img :src="hub.category?.listIconURL || defaultIcon" alt="" />
          </span>
          <span class="data-square-category__title">{{ hub.category?.categoryName || '' }}</span>
          <span class="data-square-category__count">{{
            hub.category?.reportCount || hub.total
          }}</span>
        </div>
      </div>
    </template>
    <div class="data-square-category__page">
      <section class="data-square-category__card">
        <header v-if="!hasDetailImage && hub.list.length > 0" class="data-square-category__header">
          <span class="data-square-category__folder">
            <img :src="hub.category?.listIconURL || defaultIcon" alt="" />
          </span>
          <span class="data-square-category__title">{{
            hub.category?.categoryName || '分类详情'
          }}</span>
          <span class="data-square-category__count">{{
            hub.category?.reportCount || hub.total
          }}</span>
        </header>

        <van-skeleton v-if="hub.loading" title :row="8" />
        <van-empty v-else-if="hub.list.length === 0" description="暂无报告" />
        <template v-else>
          <div class="data-square-category__list">
            <DataSquareReportRow
              v-for="report in hub.list"
              :key="report.reportId"
              :report="report"
              @click="handleReportClick"
            />
          </div>
          <button
            v-if="showLoadMore"
            class="data-square-category__load-more"
            type="button"
            :disabled="hub.loadMoreLoading"
            @click="handleLoadMore"
          >
            {{ hub.loadMoreLoading ? '加载中...' : '点击查看更多' }}
          </button>
        </template>
      </section>
    </div>
  </HPage>
</template>

<style scoped lang="scss">
.data-square-category {
  &__page {
    min-height: 100%;
    display: flex;
    flex-direction: column;
  }

  &__hero {
    height: 100px;
    display: flex;
    align-items: center;
    padding: 20px;
    background-image: url('@/assets/images/system/dataSquare/default-bg.png');
    background-repeat: no-repeat;
    background-position: center;
    background-size: cover;
  }

  &__hero-title {
    .data-square-category__count {
      background: #f2f3f5;
    }
  }

  &__hero-title {
    height: 24px;
    display: flex;
    align-items: center;
  }

  &__card {
    flex: 1;
    padding: 12px;
    margin: 12px;
    border-radius: 8px;
    background: #ffffff;
  }

  &__header {
    margin-bottom: 22px;
  }

  &__folder {
    width: 20px;
    height: 20px;
    margin-right: 8px;
    border-radius: 4px;
    color: #ffffff;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    flex-shrink: 0;
    overflow: hidden;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }
  }

  &__title {
    max-width: 190px;
    min-width: 0;
    font-weight: 500;
    font-size: 14px;
    color: #1f2733;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__count {
    height: 22px;
    min-width: 28px;
    margin-left: 9px;
    padding: 1px 8px;
    border-radius: 4px;
    background: #eef1f5;
    font-weight: 500;
    font-size: 12px;
    color: #1d2129;
    line-height: 20px;
    text-align: center;
  }

  &__list {
    display: grid;
    gap: 13px;
  }

  &__load-more {
    width: 100%;
    height: 36px;
    margin-top: 8px;
    padding: 0;
    border: 0;
    background: transparent;
    color: #a0a8b5;
    font-size: 12px;
    line-height: 18px;
  }
}
</style>
