<script setup lang="ts">
import { ref, computed, nextTick, onMounted, onBeforeUnmount, watch } from 'vue'
import { Search } from '@element-plus/icons-vue'
import type { SpecialAnalysisTypeListVo, CustomReportListVo } from '@/api/sceneAnalysis/types.d'

defineOptions({
  name: 'SpecialAnalysis'
})

const MY_PUBLISH_CATEGORY_ID = 'my_publish'

// Props 定义
interface Props {
  categoryData?: SpecialAnalysisTypeListVo[]
  reportData?: CustomReportListVo[]
  loading?: boolean
  reportLoading?: boolean
  secondTypeOptios?: any[]
  secondTypeLoading?: boolean
  myPublishTotal?: number
  /**
   * 父组件用于控制默认选中项（例如：当“我发布的”有数据时默认选中）
   */
  defaultCategoryId?: string
  /**
   * 接口分页参数（由父组件驱动）
   */
  pageNum?: number
  pageSize?: number
  total?: number
}

const {
  categoryData = [],
  reportData = [],
  loading = false,
  reportLoading = false,
  secondTypeOptios = [],
  secondTypeLoading = false,
  myPublishTotal = 0,
  defaultCategoryId = '',
  pageNum = 1,
  pageSize = 10,
  total = 0
} = defineProps<Props>()

const emits = defineEmits<{
  (e: 'open-d-d', data: any): void
  (e: 'categoryChange', categoryId: string): void
  (e: 'secondChange', data: any): void
  (e: 'queryChange', data: any): void
  (e: 'pageChange', pageNum: number): void
  (e: 'pageSizeChange', pageSize: number): void
}>()

// 当前选中的分类ID
const activeCategoryId = ref<string>('')
const hasUserSelectCategory = ref(false)

const isMyPublishActive = computed(() => activeCategoryId.value === MY_PUBLISH_CATEGORY_ID)

watch(
  () => defaultCategoryId,
  nextDefaultId => {
    // 用户已手动选择过分类时，不再用默认值覆盖
    if (hasUserSelectCategory.value) return
    if (!nextDefaultId) return
    // 仅在初始未选中任何分类时应用默认值，避免后续异步更新影响用户操作
    if (!activeCategoryId.value) {
      activeCategoryId.value = nextDefaultId
    }
  },
  { immediate: true }
)

// 处理分类数据，添加"全部"选项
const categories = computed(() => {
  const myPublishCategory = {
    id: MY_PUBLISH_CATEGORY_ID,
    name: '我发布的',
    icon: 'my_publish',
    reportCnt: myPublishTotal
  }

  const allCategory = {
    id: '',
    name: '全部',
    icon: '',
    reportCnt: categoryData.reduce((total, item) => total + item.reportCnt, 0)
  }

  const nextCategories = [allCategory, ...categoryData]

  // 当“我发布的”已上架报告为 0 时隐藏该分类
  if (myPublishTotal > 0) {
    return [myPublishCategory, ...nextCategories]
  }

  return nextCategories
})

const activeBrandTab = ref('')

// “全部”和“我发布的”不展示品牌标签，其余分类保持原有二级标签逻辑
const isBrandTabsVisible = computed(() => {
  return !isMyPublishActive.value && activeCategoryId.value !== ''
})

// 二级分类加载期间保留布局占位，避免旧标签闪现
const isBrandTabsContentVisible = computed(() => {
  return isBrandTabsVisible.value && !secondTypeLoading
})

const queryForm = ref({
  reportName: undefined,
  sortField: '',
  sortOrder: 'desc'
})

// 直接使用接口返回的报告数据
const analysisData = computed(() => {
  return reportData
})

// 分页：接口分页由父组件驱动，当前组件只负责展示与触发事件
const gridRef = ref<HTMLElement>()

const scrollGridToTop = async () => {
  await nextTick()
  gridRef.value?.scrollTo({ top: 0 })
}

const handleSizeChange = (nextSize: number) => {
  emits('pageSizeChange', nextSize)
}

const handleCurrentChange = (nextPage: number) => {
  emits('pageChange', nextPage)
}

const queryChange = () => {
  emits('queryChange', queryForm.value)
}

// 顶部按钮组数据
const brandTabs = computed(() => {
  return [
    { id: '', name: '全部', active: activeBrandTab.value === '' },
    ...secondTypeOptios.map(tab => ({
      ...tab,
      active: activeBrandTab.value === tab.id
    }))
  ]
})

// 顶部品牌标签：数据过多时提供左右切换（横向滚动）
const brandScrollContainer = ref<HTMLElement>()
const brandScrollLeft = ref(0)
// DOM 尺寸变化不会自动触发 computed，这里用一个 tick 强制刷新 canScrollRight 计算
const brandContainerTick = ref(0)
let brandResizeObserver: ResizeObserver | undefined

const updateBrandScrollLeft = () => {
  if (!brandScrollContainer.value) return
  brandScrollLeft.value = brandScrollContainer.value.scrollLeft
}

const canScrollLeft = computed(() => brandScrollLeft.value > 0)
const canScrollRight = computed(() => {
  if (brandContainerTick.value < 0 || !brandScrollContainer.value) return false
  const maxScrollLeft =
    brandScrollContainer.value.scrollWidth - brandScrollContainer.value.clientWidth
  return maxScrollLeft > 0 && brandScrollLeft.value < maxScrollLeft - 1
})

const getBrandScrollStep = () => {
  // 以容器宽度为主，保证一次滚动能明显前进（同时避免“跳太远”）
  const el = brandScrollContainer.value
  if (!el) return 240
  return Math.max(200, el.clientWidth - 80)
}

const scrollLeftHandler = () => {
  const el = brandScrollContainer.value
  if (!el) return
  const newLeft = Math.max(0, brandScrollLeft.value - getBrandScrollStep())
  el.scrollTo({ left: newLeft, behavior: 'smooth' })
}

const scrollRightHandler = () => {
  const el = brandScrollContainer.value
  if (!el) return
  const maxScrollLeft = el.scrollWidth - el.clientWidth
  const newLeft = Math.min(maxScrollLeft, brandScrollLeft.value + getBrandScrollStep())
  el.scrollTo({ left: newLeft, behavior: 'smooth' })
}

const handleBrandScroll = () => {
  updateBrandScrollLeft()
}

onMounted(() => {
  nextTick(() => {
    if (!isBrandTabsContentVisible.value) return

    updateBrandScrollLeft()
    brandContainerTick.value++

    if (typeof ResizeObserver !== 'undefined' && brandScrollContainer.value) {
      brandResizeObserver = new ResizeObserver(() => {
        brandContainerTick.value++
        updateBrandScrollLeft()
      })
      brandResizeObserver.observe(brandScrollContainer.value)
    }
  })
})

onBeforeUnmount(() => {
  brandResizeObserver?.disconnect()
  brandResizeObserver = undefined
})

watch(
  () => isBrandTabsContentVisible.value,
  async nextIsVisible => {
    // 顶部标签隐藏时清理监听，避免保留已卸载 DOM 的滚动状态
    if (!nextIsVisible) {
      brandResizeObserver?.disconnect()
      brandResizeObserver = undefined
      brandScrollLeft.value = 0
      return
    }

    await nextTick()
    updateBrandScrollLeft()
    brandContainerTick.value++

    if (typeof ResizeObserver !== 'undefined' && brandScrollContainer.value) {
      brandResizeObserver?.disconnect()
      brandResizeObserver = new ResizeObserver(() => {
        brandContainerTick.value++
        updateBrandScrollLeft()
      })
      brandResizeObserver.observe(brandScrollContainer.value)
    }
  }
)

watch(
  () => brandTabs.value.length,
  async () => {
    if (!isBrandTabsContentVisible.value) return

    // tabs 变化后需要等待 DOM 更新再计算 scrollWidth/clientWidth
    await nextTick()
    brandContainerTick.value++
    updateBrandScrollLeft()
  }
)

const skeletonCount = computed(() => {
  // 骨架屏过多会影响渲染性能，做个上限
  return Math.min(pageSize, 12)
})

watch(
  () => reportData,
  () => {
    // 数据刷新后回到顶部，避免用户停留在中间位置造成“页面卡住”的错觉
    void scrollGridToTop()
  }
)

watch(
  () => [pageNum, pageSize],
  () => {
    void scrollGridToTop()
  }
)

// 方法
const selectCategory = (categoryId: string, fromUser = false) => {
  activeCategoryId.value = categoryId
  if (fromUser) {
    hasUserSelectCategory.value = true
  }
  // 切换一级分类时，重置二级分类与查询条件，避免残留导致展示/请求不一致
  activeBrandTab.value = ''
  queryForm.value = {
    reportName: undefined,
    sortField: '',
    sortOrder: 'desc'
  }
  // 通知父组件分类变化
  emits('categoryChange', categoryId)
}

const selectBrandTab = (item: any) => {
  activeBrandTab.value = item.id
  emits('secondChange', item)
}

const handleItem = (item: any) => {
  emits('open-d-d', item)
}
</script>

<template>
  <div class="special-analysis">
    <!-- 主要内容区域 -->
    <div v-loading="loading" class="sa-main">
      <!-- 左侧分类列表 -->
      <div class="sa-sidebar">
        <div class="sidebar-content">
          <div v-if="loading" class="loading-container">
            <el-skeleton :rows="6" animated />
          </div>
          <div v-else>
            <div
              class="sidebar-item"
              v-for="category in categories"
              :key="category.id"
              :class="{ active: activeCategoryId === category.id }"
              @click="selectCategory(category.id, true)"
            >
              <div class="flex-y-center">
                <SvgIcon
                  v-if="category.icon"
                  :name="`zxfx-${category.icon}`"
                  width="20px"
                  height="20px"
                  class="mr-8"
                ></SvgIcon>
                <span class="category-name">{{ category.name }}</span>
              </div>

              <span class="category-count tag-def">{{ category.reportCnt }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧内容区域 -->
      <div class="sa-content-area">
        <div v-if="!isMyPublishActive" class="sac-header mb-16">
          <!-- 顶部品牌标签 -->
          <div class="sach-left mr-16" :class="{ 'is-placeholder': !isBrandTabsContentVisible }">
            <template v-if="canScrollLeft || canScrollRight">
              <div
                class="brand-arrow left"
                :class="{ disabled: !canScrollLeft }"
                @click.stop="scrollLeftHandler"
              >
                <el-icon color="#999999">
                  <ArrowLeftBold />
                </el-icon>
              </div>
              <div
                class="brand-arrow right"
                :class="{ disabled: !canScrollRight }"
                @click.stop="scrollRightHandler"
              >
                <el-icon color="#999999">
                  <ArrowRightBold />
                </el-icon>
              </div>
            </template>

            <div
              ref="brandScrollContainer"
              class="brand-scroll-container"
              :class="{ 'has-arrows': canScrollLeft || canScrollRight }"
              @scroll="handleBrandScroll"
            >
              <el-radio-group
                v-model="activeBrandTab"
                size="large"
                fill="#EAF3FF"
                text-color="#1677FF"
              >
                <el-radio-button
                  v-for="tab in brandTabs"
                  :key="tab.id"
                  :class="{ 'active active-border': tab.active }"
                  @click="selectBrandTab(tab)"
                  :label="tab.name"
                  :value="tab.id"
                />
                <!-- <div class="sachl-radio">

                </div> -->
              </el-radio-group>
            </div>
          </div>

          <!-- 搜索和筛选区域 -->
          <div class="sa-toolbar flex-y-center gap-16">
            <el-input
              v-model="queryForm.reportName"
              style="width: 172px; height: 32px"
              placeholder="请输入关键词搜索"
              :suffix-icon="Search"
              @change="queryChange"
            />

            <el-select
              v-model="queryForm.sortField"
              placeholder=""
              :empty-values="[null, undefined]"
              style="width: 108px"
              @change="queryChange"
            >
              <el-option value="" label="综合排序"></el-option>
              <el-option value="create_time" label="时间排序"></el-option>
              <el-option value="view_count" label="热度排序"></el-option>
            </el-select>
          </div>
        </div>

        <!-- 卡片网格 -->
        <div ref="gridRef" class="sa-grid">
          <!-- 加载状态 -->
          <div v-if="reportLoading" class="skeleton-grid">
            <div v-for="n in skeletonCount" :key="n" class="skeleton-card">
              <el-skeleton animated>
                <template #template>
                  <el-skeleton-item variant="image" style="width: 100%; height: 120px" />
                  <div style="padding: 14px">
                    <el-skeleton-item variant="h3" style="width: 80%" />
                    <div
                      style="
                        display: flex;
                        align-items: center;
                        justify-content: space-between;
                        margin-top: 16px;
                      "
                    >
                      <el-skeleton-item variant="text" style="width: 30%" />
                      <el-skeleton-item variant="text" style="width: 30%" />
                    </div>
                  </div>
                </template>
              </el-skeleton>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-else-if="analysisData.length === 0" class="empty-container">
            <el-empty description="暂无报告数据" />
          </div>

          <!-- 报告卡片 -->
          <div
            v-else
            class="sa-item"
            v-for="item in analysisData"
            :key="item.id"
            @click="handleItem(item)"
          >
            <!-- 图片占位区域 -->
            <div class="sa-image"></div>

            <!-- 内容区域 -->
            <div class="sa-content">
              <div class="flex">
                <!-- 热门标签 -->
                <div v-if="item.viewCount > 1000" class="sa-tag mr-8">热门</div>
                <div class="flex">
                  <div v-if="item.pinToTop === 1" class="top-tag mr-8">置顶</div>
                  <div class="sa-title flex-1">
                    <span>{{ item.reportName }}</span>
                  </div>
                </div>
              </div>

              <div class="sa-meta">
                <div class="sa-date">
                  <SvgIcon
                    name="calendar"
                    width="20px"
                    height="20px"
                    class="mr-8"
                    color="#A4A7AE"
                  ></SvgIcon>
                  <span>{{ item.createTime }}</span>
                </div>
                <div class="flex-y-center">
                  <div class="sa-views">
                    <SvgIcon
                      name="eye"
                      width="20px"
                      height="20px"
                      class="mr-8"
                      color="#A4A7AE"
                    ></SvgIcon>
                    <span>{{ item.viewCount }}</span>
                  </div>
                  <div class="sa-likes ml-16">
                    <SvgIcon
                      name="heart-circle"
                      width="20px"
                      height="20px"
                      class="mr-8"
                      color="#A4A7AE"
                    ></SvgIcon>
                    <span>{{ item.collectionCount }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <!-- 分页区域 -->
        <div v-if="!reportLoading && total > 0" class="sa-pagination">
          <el-pagination
            background
            :total="total"
            :page-size="pageSize"
            :current-page="pageNum"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :hide-on-single-page="total <= pageSize"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.special-analysis {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;

  .sa-main {
    display: flex;
    min-height: 600px; // 设置最小高度，避免过度收缩
    max-height: 100vh;
    gap: 16px;

    // 左侧分类列表
    .sa-sidebar {
      width: 220px;
      height: auto;
      flex-shrink: 0;
      .sidebar-content {
        width: 100%;
        height: 100%;
        overflow: auto;
        background: #f8fafc;
        border-radius: 8px;
        border: 1px solid #d9d9d9;
        padding: 16px;
      }

      .sidebar-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px 16px;
        margin-bottom: 16px;
        border-radius: 8px;
        cursor: pointer;
        font-size: 16px;
        font-weight: 500;
        color: #171819;
        &:last-child {
          margin-bottom: 0;
        }
        &.active {
          background: #eaf3ff;
          color: #1677ff;

          .category-count {
            color: #1677ff;
            border: none;
            background-color: transparent;
          }
        }

        .category-name {
          font-size: 14px;
          font-weight: 500;
        }

        .category-count {
        }
      }
    }

    // 右侧内容区域
    .sa-content-area {
      flex: 1;
      display: flex;
      flex-direction: column;
      overflow: hidden;
      min-height: 0; // 防止 flex 子元素过度拉伸

      .sac-header {
        display: flex;
        justify-content: space-between;

        :deep(.el-radio-button--large .el-radio-button__inner) {
          height: 48px;
          display: flex;
          align-items: center;
          font-weight: normal;
          font-size: 16px;
          color: #1d252f;
          border-color: #d5d7da !important;
        }

        :deep(.el-radio-button.is-active .el-radio-button__inner) {
          font-weight: 600 !important;
        }
      }

      .sach-left {
        flex: 1;
        min-width: 0;
        overflow: hidden;
        position: relative;

        &.is-placeholder {
          visibility: hidden;
          pointer-events: none;
        }

        .brand-arrow {
          width: 32px;
          height: 32px;
          background: #f2f3f5;
          border-radius: 16px;
          border: 1px solid #ffffff;
          position: absolute;
          display: flex;
          justify-content: center;
          align-items: center;
          cursor: pointer;
          z-index: 2;
          top: 50%;
          transform: translateY(-50%);
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
            left: 0;
          }

          &.right {
            right: 0;
          }
        }

        .brand-scroll-container {
          width: 100%;
          overflow-x: auto;
          overflow-y: hidden;
          white-space: nowrap;

          &.has-arrows {
            padding: 0 40px;
            box-sizing: border-box;
          }

          // 隐藏滚动条
          &::-webkit-scrollbar {
            display: none;
          }

          -ms-overflow-style: none;
          scrollbar-width: none;
        }

        :deep(.el-radio-group) {
          width: max-content;
          white-space: nowrap;
          display: flex !important;
          flex-wrap: nowrap !important;
          align-items: center;
        }

        :deep(.el-radio-button) {
          flex-shrink: 0;
          white-space: nowrap;
        }

        :deep(.el-radio-button__inner) {
          white-space: nowrap;
        }
      }

      // 搜索和筛选工具栏
      .sa-toolbar {
        display: flex;
        justify-content: flex-end;
        min-width: 300px;
      }

      // 卡片网格
      .sa-grid {
        display: grid;
        grid-template-columns: repeat(4, 1fr);
        gap: 16px;
        overflow-y: auto;
        flex: 1;
        align-content: start; // 内容从顶部开始排列，避免拉伸
        grid-auto-rows: max-content; // 行高自适应内容
        min-height: 400px; // 设置最小高度，确保有足够的显示空间

        // 骨架屏网格样式
        .skeleton-grid {
          display: grid;
          grid-template-columns: repeat(4, 1fr);
          gap: 16px;
          width: 100%;
          grid-column: 1 / -1; // 占满整个网格

          .skeleton-card {
            background: #ffffff;
            border-radius: 8px;
            border: 1px solid #e5e7eb;
            overflow: hidden;
            height: fit-content;
          }
        }

        .loading-container {
          display: grid;
          grid-template-columns: repeat(4, 1fr);
          gap: 16px;
          width: 100%;

          .skeleton-card {
            background: #ffffff;
            border-radius: 8px;
            border: 1px solid #e5e7eb;
            overflow: hidden;
          }
        }

        .empty-container {
          grid-column: 1 / -1;
          display: flex;
          justify-content: center;
          align-items: center;
          min-height: 300px;
        }

        .sa-item {
          background: #ffffff;
          border-radius: 8px;
          border: 1px solid #e5e7eb;
          // overflow: hidden;
          transition: all 0.2s ease;
          cursor: pointer;
          position: relative;
          height: fit-content; // 高度适应内容，不拉伸
          align-self: start; // 在网格中从顶部对齐

          &:hover {
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
            transform: translateY(-2px);
          }

          .sa-tag {
            position: absolute;
            top: 8px;
            left: 8px;
            height: 22px;
            line-height: 22px;
            background: #f97316;
            color: white;
            padding: 0 8px;
            border-radius: 12px;
            font-size: 12px;
            z-index: 1;
          }

          .top-tag {
            width: 40px;
            height: 22px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            background: linear-gradient(271deg, #fb9a0e 0%, #ff7d38 81.03%);
            border-radius: 4px 4px 4px 4px;
            font-weight: 500;
            font-size: 12px;
            color: #ffffff;
            line-height: 28px;
          }

          .sa-image {
            height: 120px;
            background-color: #cee9ff;
            background-image: url('/demo-assets/report-cover-v2.png');
            background-size: cover;
            background-position: center;
            background-repeat: no-repeat;
            position: relative;
            display: flex;
            align-items: center;
            justify-content: center;
          }

          .sa-content {
            padding: 12px;

            .sa-title {
              font-size: 16px;
              font-weight: 500;
              color: #181d27;
              line-height: 24px;
              margin-bottom: 8px;
              display: -webkit-box;
              -webkit-line-clamp: 2;
              -webkit-box-orient: vertical;
              overflow: hidden;
              text-overflow: ellipsis;
              min-height: 40px;
            }

            .sa-meta {
              display: flex;
              align-items: center;
              justify-content: space-between;
              gap: 12px;
              font-size: 14px;
              color: #6b7280;

              .sa-date,
              .sa-views,
              .sa-likes {
                display: flex;
                align-items: center;
                gap: 2px;
              }
            }
          }
        }
      }

      // 分页
      .sa-pagination {
        flex-shrink: 0;
        display: flex;
        justify-content: flex-end;
        padding-top: 16px;

        :deep(.el-pagination) {
          // 避免分页过长时挤压布局
          flex-wrap: wrap;
          justify-content: flex-end;
        }
      }
    }
  }
}
</style>
