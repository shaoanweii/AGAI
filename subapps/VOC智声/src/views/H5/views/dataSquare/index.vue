<script setup lang="ts">
import { computed, onActivated, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import HPage from '@h5/components/UI/HPage'
import { useH5AppStore } from '@h5/store/h5App'
import { usePermissionsStore } from '@h5/store'
import { useH5MenuVisitRecord } from '@h5/hooks/useH5MenuVisitRecord'
import { useKeepAliveScroll } from '@h5/hooks/useKeepAliveScroll'
import type { HttpRequestConfig } from '@h5/api/http'
import {
  getH5DataSquareBrandList,
  getH5DataSquareHome,
  type H5DataSquareBrandItem,
  type H5DataSquareCategoryItem,
  type H5DataSquareReportItem
} from '@h5/api/dataSquare'
import DataSquareBrandTabs from './components/DataSquareBrandTabs.vue'
import DataSquareCategoryCard from './components/DataSquareCategoryCard.vue'

defineOptions({
  name: 'H5DataSquare'
})

const router = useRouter()
const h5AppStore = useH5AppStore()
const permissionsStore = usePermissionsStore()

// 记忆并恢复 H5 滚动容器的滚动位置
useKeepAliveScroll()

useH5MenuVisitRecord()

const PAGE_REPORT_LIMIT = 3

const hub = reactive({
  loading: false,
  brandLoading: false,
  brandList: [] as H5DataSquareBrandItem[],
  categoryList: [] as H5DataSquareCategoryItem[],
  currentBrand: null as H5DataSquareBrandItem | null,
  keyword: ''
})

// 广场首页请求序号，用于防止旧请求晚于新请求返回后覆盖最新数据
let homeRequestSeq = 0

const hasBrand = computed(() => hub.brandList.length > 0)
const defaultBrandReady = computed(() => !!permissionsStore.hasInited)
const isEmpty = computed(() => !hub.loading && hasBrand.value && hub.categoryList.length === 0)

/**
 * 重置首页搜索入口的临时关键词。
 * 首页被 keep-alive 缓存后，返回时需要展示干净的搜索入口。
 */
const resetEntryKeyword = () => {
  hub.keyword = ''
}

/**
 * 获取广场品牌列表。
 * - 默认品牌来自 H5AppStore，BrandTabs 内部会按默认值或首项触发首次加载
 */
const fetchBrandList = async () => {
  hub.brandLoading = true
  try {
    const res = await getH5DataSquareBrandList()
    hub.brandList = Array.isArray(res.result) ? res.result : []
  } catch (error) {
    console.error('获取看数广场品牌列表失败:', error)
    hub.brandList = []
  } finally {
    hub.brandLoading = false
  }
}

/**
 * 获取广场首页分类与报告列表。
 * @param brandCode 当前品牌编码
 */
const fetchHomeData = async (brandCode: string) => {
  if (!brandCode) return

  const requestSeq = ++homeRequestSeq
  hub.loading = true
  try {
    const requestConfig: HttpRequestConfig = {
      cancelPrevious: true
    }
    const res = await getH5DataSquareHome(
      {
        categoryId: '',
        brandCode,
        reportLimit: PAGE_REPORT_LIMIT
      },
      requestConfig
    )

    // 仅允许最后一次请求写回数据，避免旧品牌请求晚返回覆盖当前品牌内容
    if (requestSeq !== homeRequestSeq) return

    hub.categoryList = Array.isArray(res.result) ? res.result : []
  } catch (error) {
    if (requestSeq !== homeRequestSeq) return
    console.error('获取看数广场首页数据失败:', error)
    hub.categoryList = []
  } finally {
    if (requestSeq === homeRequestSeq) {
      hub.loading = false
    }
  }
}

/**
 * 切换品牌后刷新广场首页数据。
 * @param brand 当前选中品牌
 */
const handleBrandChange = (brand: H5DataSquareBrandItem) => {
  if (hub.currentBrand?.brandCode === brand.brandCode) return

  hub.currentBrand = brand
  void fetchHomeData(brand.brandCode)
}

/**
 * 点击搜索框进入搜索页。
 */
const handleSearchFocus = () => {
  router.push({
    name: 'H5DataSquareSearch',
    query: {
      brandCode: hub.currentBrand?.brandCode || ''
    }
  })
}

/**
 * 首页搜索确认，带上关键词进入搜索结果页。
 */
const handleSearch = () => {
  const keyword = hub.keyword.trim()
  if (!keyword) {
    handleSearchFocus()
    return
  }

  router.push({
    name: 'H5DataSquareSearch',
    query: {
      brandCode: hub.currentBrand?.brandCode || '',
      keyword
    }
  })
}

/**
 * 打开分类详情。
 * @param category 当前分类
 */
const handleMoreClick = (category: H5DataSquareCategoryItem) => {
  if (!category.categoryId) {
    showToast('分类信息缺失')
    return
  }

  router.push({
    name: 'H5DataSquareCategory',
    query: {
      categoryId: category.categoryId
    }
  })
}

/**
 * 打开报告详情页。
 * @param report 当前报告
 */
const handleReportClick = (report: H5DataSquareReportItem) => {
  if (!report.reportId) {
    showToast('报告信息缺失')
    return
  }

  router.push({
    name: 'H5DataSquareReportDetail',
    query: {
      reportId: report.reportId,
      reportName: report.reportName || ''
    }
  })
}

void fetchBrandList()

onActivated(resetEntryKeyword)
</script>

<template>
  <HPage class="data-square-page" background-color="#f5f7fa">
    <template #nav-bar>
      <DataSquareBrandTabs
        :list="hub.brandList"
        :default-brand-code="h5AppStore.getDefDataSquareBrandCode"
        :default-brand-ready="defaultBrandReady"
        @change="handleBrandChange"
      />
      <div class="data-square-page__search-wrap">
        <van-search
          v-model="hub.keyword"
          placeholder="请输入报告名称搜索"
          shape="round"
          maxlength="30"
          clearable
          @focus="handleSearchFocus"
          @search="handleSearch"
        />
      </div>
    </template>

    <div class="data-square-page__content">
      <van-skeleton v-if="hub.brandLoading || hub.loading" title :row="8" />
      <van-empty v-else-if="!hasBrand" description="暂无可用品牌，无法展示广场数据" />
      <van-empty v-else-if="isEmpty" description="暂无数据" />
      <div v-else class="data-square-page__cards">
        <DataSquareCategoryCard
          v-for="category in hub.categoryList"
          :key="category.categoryId"
          :category="category"
          @more="handleMoreClick"
          @report-click="handleReportClick"
        />
      </div>
    </div>
  </HPage>
</template>

<style scoped lang="scss">
.data-square-page {
  &__search-wrap {
    padding: 12px;
    background: #f5f7fa;

    :deep(.van-search) {
      padding: 0;
      background: transparent;
    }

    :deep(.van-search__content) {
      height: 32px;
      border: 1px solid #ebedf0;
      background: #ffffff;
    }

    :deep(.van-search__field) {
      height: 32px;
    }
  }

  &__content {
    padding: 0 12px 12px;
  }

  &__cards {
    display: grid;
    gap: 12px;
  }
}
</style>
