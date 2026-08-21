<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getRealAttr } from '@/views/leaderOverview/leader/common/fn.ts'
import { useQueryStore } from '@/store/modules/query'
import ViewTable from './ViewTable.vue'
import { getGroupBrief } from '@/api/overview/leader'
import { debounce } from 'lodash-es'
const fallbackBrandMark = '/demo-assets/brands/zhixing.png'

defineOptions({
  name: 'BrandView1'
})

// 基础设置
const loading = ref(false)
const theData = ref<any>([])
const queryStore = useQueryStore()
const storePms = queryStore.currentQueryParams

// 调接口
const fetchData = async () => {
  let errMsg = '获取集团简报数据失败'
  try {
    loading.value = true
    theData.value = []
    // 确保必需的字段存在
    const queryParams: VocQueryParams = getRealAttr({
      // ...storePms, 取所有品牌，不传其他值
      startDate: storePms.startDate,
      endDate: storePms.endDate
    })
    const response = await getGroupBrief(queryParams)

    if (response.success) {
      theData.value = response.result || []
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

// 切换品牌
const curIndex = ref(0)
const changeItem = (e: any, index: number, brandCode: string) => {
  curIndex.value = index
  queryStore.updateQueryParams({
    brandCode
  })
  // console.log('@@ 切换 市场横屏-品牌',storePms)
}

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
</script>

<template>
  <div id="brandView1" class="bv-wrap" data-page-export-fixed-reset>
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>
    <!-- 正文 -->
    <template v-else-if="theData.length > 0">
      <div class="scroll-container">
        <div
          class="brand-view"
          :style="{ gridTemplateColumns: `repeat(${theData.length}, minmax(0, 1fr))` }"
        >
          <!-- 数据展示 -->
          <div
            v-for="(item, index) in theData"
            :key="item.brandCode || index"
            class="bv-item"
            :class="{ active: index === curIndex }"
          >
            <!-- 头部 -->
            <div class="flex-between" @click="changeItem($event, index, item.brandCode)">
              <div class="flex-y-center">
                <div class="bvi-logo">
                  <img
                    :src="item.brandImage || fallbackBrandMark"
                    class="brand-mark w-full h-full"
                    :alt="item.name"
                  />
                </div>
                <div class="bvi-title font-600 text-h4 ml-8">{{ item.name || '未知品牌' }}</div>
              </div>
              <div class="bvi-link flex-center">
                <!-- <el-icon :size="14"><TopRight /></el-icon> -->
                <img src="@/assets/images/arrow-up-right.png" alt="" class="w-14 h-14" />
              </div>
            </div>

            <!-- 表格 -->
            <ViewTable
              class="mt-16"
              :data-list="item.topDataList"
              :data-title="item.name"
            ></ViewTable>
          </div>
        </div>
      </div>
    </template>

    <!-- 空状态 -->
    <div v-else class="empty-container">
      <el-empty description="暂无品牌数据" />
    </div>
  </div>
</template>

<style lang="scss" scoped>
// @media screen and (max-width: 1600px) {

// .bv-item {
//   padding:0 10px 8px  !important;
// }

// :deep(.table-header),
// :deep(.table-row){
//   grid-template-columns: 1fr 50px 50px !important;
// }
// }

#brandView1 {
  height: 332px;
}
.bv-item {
  overflow: hidden;
}

.flex-between {
  padding-top: 16px;
  cursor: pointer;
}

.bvi-title {
  white-space: nowrap;
}

// 源码 --------------------
.bv-wrap {
  width: 100%;
  height: 100%;
  position: relative;

  .arrow {
    width: 40px;
    height: 40px;
    background: #f2f3f5;
    border-radius: 20px;
    border: 1px solid #ffffff;
    position: absolute;
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

    &.left {
      top: 50%;
      left: 0;
      transform: translateY(-50%);
    }

    &.right {
      top: 50%;
      right: 0;
      transform: translateY(-50%);
    }
  }
}
.scroll-container {
  width: 100%;
  height: 100%;
  overflow-x: hidden;
  overflow-y: hidden;
  // 隐藏滚动条
  &::-webkit-scrollbar {
    display: none;
  }
  -ms-overflow-style: none;
  scrollbar-width: none;
}
.brand-view {
  display: grid;
  gap: 16px;
  width: 100%;
  min-width: 0;

  .loading-container,
  .empty-container {
    width: 100%;
    height: 288px;
    // min-height: 200px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .bv-item {
    width: auto;
    min-width: 0;
    padding: 0 16px 8px 16px;
    background: #f5f7fa;
    box-shadow: 0px 1px 1px 0px rgba(10, 13, 18, 0.05);
    border-radius: 8px;
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
    }
    .bvi-link {
      width: 28px;
      height: 28px;
      background: #f2f3f5;
      border-radius: 4px;
      border: 1px solid rgba(255, 255, 255, 0.5);
    }
  }
}
</style>
