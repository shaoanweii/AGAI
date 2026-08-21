<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getRealAttr } from '@/views/leaderOverview/leader/common/fn.ts'
import { useQueryStore } from '@/store/modules/query'
import LineTrend from '@/views/leaderOverview/leader/common/LineTrend.vue'
import { getBrandInsight } from '@/api/overview/leader'
import { fmtNum, fmtPer, fmtFix } from '@/utils'
import { debounce } from 'lodash-es'
const fallbackBrandMark = '/demo-assets/brands/zhixing.png'

defineOptions({
  name: 'BrandView2'
})

// 获取品牌简报数据 ------------

// 基础设置
const loading = ref(false)
const theData = ref<any>([])
const queryStore = useQueryStore()
const storePms = queryStore.currentQueryParams

// 调接口
const fetchData = async () => {
  let errMsg = '获取品牌简报数据失败'
  try {
    loading.value = true
    theData.value = []

    const queryParams: VocQueryParams = getRealAttr({
      // ...storePms, 取所有品牌，不传其他值
      startDate: storePms.startDate,
      endDate: storePms.endDate,
      channelCatagory: storePms.channelCatagory
    })

    const response = await getBrandInsight(queryParams)

    if (response.success && response.result) {
      theData.value = response.result.slice(0, 6)
    } else {
      ElMessage.error(response.message || errMsg)
    }
  } catch (error) {
    console.error(`${errMsg}:, ${error}`)
    ElMessage.error(`${errMsg}，请稍后重试`)
  } finally {
    loading.value = false
  }
}

/**
 * 切换品牌洞察品牌卡片。
 * 品牌洞察统一使用 tempCode 保存当前卡片选中值，
 * 请求时再根据 groupCode 转换为 automark。
 *
 * @param index 当前点击卡片索引
 * @param brandCode 当前品牌编码
 */
const curIndex = ref(0)
const changeItem = (index: number, brandCode?: string) => {
  curIndex.value = index
  queryStore.updateQueryParams({
    tempCode: brandCode,
    tag2Code: '',
    intention: '',
    topic: '',
    tagType: undefined
  })
  // console.log('@@ 切换 品牌洞察-品牌',storePms)
}

// 格式化提及量
// const formatMentionCount = (value?: number): string => {
//   if (value === undefined || value === null) return '--'
//   if (value >= 10000) {
//     return `${(value / 10000).toFixed(1)}w`
//   }
//   return value.toString()
// }

const fetchDataDelay = debounce(fetchData, 300)

onMounted(() => {
  fetchDataDelay()
})

watch(
  () => ({
    startDate: storePms.startDate,
    endDate: storePms.endDate
  }),
  () => {
    curIndex.value = 0
    fetchDataDelay()
  }
)
watch(
  () => [storePms.channelCatagory],
  () => {
    fetchDataDelay()
  }
)
</script>

<template>
  <div id="brandView2" class="bv-wrap" data-page-export-fixed-reset>
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="3" animated />
    </div>

    <!-- 数据展示 -->
    <div v-else-if="theData.length > 0" class="brand-view gap-16 flex-y-center">
      <div
        v-for="(item, index) in theData"
        :key="item.brandCode || index"
        class="bv-item"
        :class="{ default: index !== 0, active: index === curIndex }"
      >
        <!-- 头部 -->
        <div class="flex-between" @click="changeItem(Number(index), item.brandCode)">
          <div class="flex-y-center">
            <div class="bvi-logo">
              <img :src="item.imgUrl || fallbackBrandMark" class="brand-mark w-full h-full" :alt="item.name" />
            </div>
            <div class="bvi-title font-600 text-h4 ml-8">{{ item.name || '未知品牌' }}</div>
          </div>
          <div class="bvi-link flex-center">
            <!-- <el-icon :size="14"><TopRight /></el-icon> -->
            <img src="@/assets/images/arrow-up-right.png" alt="" class="w-14 h-14" />
          </div>
        </div>

        <!-- 正文 -->
        <div class="bv-cont">
          <div class="text-body text-secondary mt-16">负面率</div>
          <div class="flex-y-center">
            <div
              class="text-h3"
              style="font-weight: 500 !important"
              :style="{ color: item.rateColor }"
            >
              {{ fmtPer(item.negativeRate) }}
            </div>
            <div class="ml-6 tag-def">{{ fmtFix(item.growth) }}</div>
          </div>

          <div class="flex mt-16">
            <div class="mr-6 w-60">
              <div class="text-body text-tertiary">提及量</div>
              <div class="text-h4 font-600">{{ fmtNum(item.mentionCount) }}</div>
            </div>
            <div class="chartBox">
              <LineTrend :trend-data="item.growthTrend" :width="'100%'"></LineTrend>
            </div>
          </div>
        </div>
      </div>
    </div>
    <!-- 空状态 -->
    <div v-else class="empty-container">
      <el-empty description="暂无品牌数据" />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.bv-item {
  overflow: hidden;
}

@media screen and (max-width: 1600px) {
  .bv-item {
    padding: 0 10px 16px !important;
  }
}

#brandView2 {
  min-height: 190px;
}

.flex-between {
  padding-top: 16px;
  cursor: pointer;
}

.bvi-title {
  white-space: nowrap;
}

.chartBox {
  width: calc(100% - 66px);
}

// 源码 --------------------

.brand-view {
  display: grid;
  // grid-template-columns: repeat(6, minmax(226px, 1fr));
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
  overflow: hidden;

  .loading-container,
  .empty-container {
    width: 100%;
    min-height: 190px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .bv-item {
    padding: 0 16px 16px;
    background: #f5f7fa;
    box-shadow: 0px 1px 1px 0px rgba(10, 13, 18, 0.05);
    border-radius: 8px 8px 8px 8px;
    border: 1px solid #dfe2e8;

    &.active {
      border: 2px solid #1677ff;
      background: #eaf3ff;
    }

    .bvi-logo {
      width: 28px;
      height: 28px;
      img {
        width: 100%;
        height: 100%;
        object-fit: contain;
        border-radius: 6px;
      }
    }

    .bvi-title {
      color: #1d252f;
      white-space: nowrap;
    }
    .bvi-link {
      width: 28px;
      height: 28px;
      background: #f2f3f5;
      border-radius: 4px;
      border: 1px solid rgba(255, 255, 255, 0.5);
    }
    .text-body {
      white-space: nowrap;
    }
  }
}

.tag-def {
  padding: 0 8px !important;
  color: #5f6a7a !important;
}
</style>
