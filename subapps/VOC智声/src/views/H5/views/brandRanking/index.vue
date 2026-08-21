<script setup lang="ts">
import HPage from '@h5/components/UI/HPage/index'
import HNavBar from '@h5/components/UI/HNavBar/index'
import { useRouter, useRoute } from 'vue-router'
import BrandComparison from '@h5/components/BrandComparison/index.vue'
import { computed, onMounted, reactive } from 'vue'
import { getIndustryBrandComparison } from '@h5/api/home'
import { showToast } from 'vant'
import type { H5VocBaseRequest } from '@h5/api/home/types'

// 定义品牌对比项类型
interface BrandComparisonItem {
  [key: string]: any
}

const router = useRouter()
const route = useRoute()

// 从路由参数初始化请求参数
const initRequestParams = (): H5VocBaseRequest => {
  const query = route.query
  return {
    ...query
  }
}

const hub = reactive({
  loading: false,
  refreshing: false,
  finished: false,
  list: [] as BrandComparisonItem[],
  pageNum: 1,
  pageSize: 20,
  total: 0,
  requestParams: initRequestParams()
})

// 判断是否为空
const isEmpty = computed(() => !hub.loading && !hub.refreshing && hub.list.length === 0)

onMounted(() => {
  getBrandComparisonList()
})

const handleBack = () => {
  router.back()
}

// 重置并刷新数据
const resetAndRefresh = () => {
  hub.pageNum = 1
  hub.list = []
  hub.finished = false
  getBrandComparisonList()
}

// 下拉刷新
const onRefresh = () => {
  hub.refreshing = true
  hub.pageNum = 1
  hub.list = []
  hub.finished = false
  getBrandComparisonList().finally(() => {
    hub.refreshing = false
  })
}

// 上拉加载
const onLoad = () => {
  if (hub.finished) {
    hub.loading = false
    return
  }
  getBrandComparisonList()
}

// 点击根因分析
const handleRootCauseClick = (item: any) => {
  router.push({
    name: 'H5AnalysisAndVoice',
    query: {
      ...hub.requestParams,
      brandCode: item.code || ''
    }
  })
}

// 获取品牌对比数据
const getBrandComparisonList = (): Promise<void> => {
  hub.loading = true

  const requestParams = {
    ...hub.requestParams,
    pageSize: hub.pageSize,
    pageNum: hub.pageNum
  }

  return getIndustryBrandComparison(requestParams).then((res: BaseResponse) => {
    if (res.success && res.result) {
      const { list, total } = res.result

      if (hub.pageNum === 1) {
        hub.list = list || []
      } else {
        hub.list.push(...(list || []))
      }

      hub.total = total || 0

      // 判断是否已加载完所有数据
      if (hub.list.length >= hub.total || (list && list.length < hub.pageSize)) {
        hub.finished = true
      } else {
        hub.pageNum++
      }
    } else {
      showToast(res.message || '获取品牌对比数据失败')
      hub.finished = true // 请求失败时也应该停止加载
    }
  }).catch((err: any) => {
    console.error('获取品牌对比数据失败:', err)
    showToast('网络错误，请重试')
    hub.finished = true // 网络错误时停止加载
  }).finally(() => {
    hub.loading = false
  })
}
</script>
<template>
  <HPage backgroundColor="#fff">
    <!-- 导航栏插槽 -->
    <template #nav-bar>
      <HNavBar left-text="返回" @click-left="handleBack" />
    </template>
    <template #default>
      <van-pull-refresh
        v-model="hub.refreshing"
        @refresh="onRefresh"
        :disabled="hub.loading"
      >
        <van-list
          v-model:loading="hub.loading"
          v-model:finished="hub.finished"
          :finished-text="isEmpty ? '' : '没有更多了'"
          @load="onLoad"
          :immediate-check="false"
        >
          <div class="p-16">
            <BrandComparison :data="hub.list" @item-click="handleRootCauseClick"/>
          </div>
          <!-- 无数据视图 -->
          <van-empty v-if="isEmpty"
                     description="暂无品牌对比数据"
                     class="empty-container" />
        </van-list>
      </van-pull-refresh>
    </template>
  </HPage>
</template>
<style lang="scss" scoped>
.empty-container {
  padding: 60px 20px;
}
</style>
