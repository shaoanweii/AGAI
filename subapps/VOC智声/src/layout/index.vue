<template>
  <!-- toDo 正式版去掉test -->
  <div class="layout">
    <!-- 侧边栏 -->
    <!-- <Sidebar /> -->
    <!-- <ExpandMenu v-if="!isCollapse"></ExpandMenu> -->
    <ExpandMenu></ExpandMenu>
    <!-- <ExpandMenu></ExpandMenu> -->

    <!-- 主内容区域 -->
    <div class="layout__main">
      <!-- 顶部导航栏 -->
      <LayoutHeader />

      <!-- 页面内容区域 -->
      <!-- :class="{ 'layout__content--collapsed': isCollapse }" -->
      <div
        class="layout__content"
        @scroll="handleScroll"
        :class="{ 'layout__content--collapsed': false }"
      >
        <router-view v-if="reportDetailReady" />
        <section v-else class="report-detail-state" aria-live="polite">
          <div v-if="reportDetailLoading" class="report-detail-state__loading">
            <div class="report-detail-state__loading-title">正在加载报告</div>
            <el-skeleton :rows="7" animated />
          </div>
          <el-result v-else icon="error" title="报告加载失败" :sub-title="reportDetailError">
            <template #extra>
              <el-button :icon="RefreshRight" type="primary" @click="retryReportDetail">
                重新加载
              </el-button>
              <el-button @click="handleBackToSceneAnalysis">返回场景分析</el-button>
            </template>
          </el-result>
        </section>
      </div>
    </div>
    <!-- 下钻弹窗 -->
    <DrillDownDialog
      v-if="shouldMountDrillDownDialog"
      v-model:visible="ddStore.ddVisible"
    ></DrillDownDialog>

    <!-- 6大场景分析页面,动态组件渲染 -->
    <!-- <component
      v-if="generalScenarioStore.visible && generalScenarioStore.componentName"
      :is="componentMap[generalScenarioStore.componentName]"
    ></component> -->
  </div>
</template>

<script setup lang="ts">
import { computed, ref, provide, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { RefreshRight } from '@element-plus/icons-vue'
import LayoutHeader from './components/Header.vue'
import DrillDownDialog from '@components/Business/DrillDownDialog/index.vue'

// 导入所有可能的弹窗组件
// import CompetitorAnalysis from '@/components/Business/Scene/CompetitorAnalysis/index.vue'
// import ProductAnalysis from '@/components/Business/Scene/ProductAnalysis/index.vue'
// import ServiceAnalysis from '@/components/Business/Scene/ServiceAnalysis/index.vue'
// import GroupAnalysis from '@/components/Business/Scene/GroupAnalysis/index.vue'
// import ThisProductAnalysis from '@/components/Business/Scene/ThisProductAnalysis/index.vue'
// import JourneyAnalysis from '@/components/Business/Scene/JourneyAnalysis/index.vue'

import { useUserStore } from '@/store'
import ExpandMenu from './components/ExpandMenu.vue'
import useGeneralDrillDownStore from '@/store/modules/generalDrillDown'
import { debounce } from 'lodash-es'
import { recordMenuVisit } from '@/utils/operationLog'
import { useSceneReportDetailBootstrap } from '@/hooks/useSceneReportDetailBootstrap'

defineOptions({
  name: 'Layout'
})

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const ddStore = useGeneralDrillDownStore()
const shouldMountDrillDownDialog = computed(() => route.name !== 'rootCause')
const { reportDetailReady, reportDetailLoading, reportDetailError, retryReportDetail } =
  useSceneReportDetailBootstrap()

/** 返回 VOC 场景分析首页。 */
const handleBackToSceneAnalysis = () => {
  router.push('/scene/analysis')
}

// console.log('@@', userInfo)

// 滚动相关
const scrollPosition = ref(0) // 用于存储滚动位置

// 监听滚动事件
const handleScroll = debounce(event => {
  const target = event.target as HTMLElement
  scrollPosition.value = target.scrollTop
}, 10) // 防抖时间为200毫秒，你可以根据需要调整这个时间

/**
 * PC 端首页访问记录：使用监听方式补上报
 * - 覆盖“登录后自动进入首页 / 单点直达首页”场景
 * - 首页点击由菜单组件触发跳转，但不在菜单组件里上报，避免重复调用
 */
const lastHomeReportFullPath = ref('')
const reportPcHomeVisitIfNeeded = () => {
  // 等待权限与菜单初始化完成后再上报，保证 homePath/menuPathIdMap 已准备好
  if (!userStore.homePath || userStore.homePath === '/') return

  // 离开首页时重置，确保下次再进入首页仍会再次上报
  if (!userStore.isHomePath(route.path)) {
    lastHomeReportFullPath.value = ''
    return
  }

  // 同一次进入首页过程中（如权限加载导致的重复触发），同一个 fullPath 只上报一次
  if (route.fullPath && route.fullPath === lastHomeReportFullPath.value) return
  lastHomeReportFullPath.value = route.fullPath || userStore.homePath

  const frontRouting = route.fullPath || userStore.homePath
  const visitUrl = `${window.location.origin}${window.location.pathname}${window.location.search}#${frontRouting}`
  const menuName = ((route.meta as any)?.title as string) || ''
  const menuId = userStore.getMenuIdByPath(route.path) || ''

  recordMenuVisit({
    visitUrl,
    frontRouting,
    menuName,
    menuId
  }).catch(() => void 0)
}

watch(
  [() => route.fullPath, () => userStore.homePath, () => userStore.menuPathIdMap],
  () => {
    reportPcHomeVisitIfNeeded()
  },
  { immediate: true }
)

// 使用 provide 传递滚动位置
provide('scrollPosition', scrollPosition)
</script>

<style lang="scss" scoped>
.layout {
  display: flex;
  width: 100%;
  height: 100vh;
  background: #f5f5f5;
  position: relative;

  .layout__main {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-height: 0; // 确保flex子元素可以正确收缩
  }

  .layout__content {
    // flex: 1;
    overflow-y: auto;
    width: calc(100vw - var(--menu-width));
    height: calc(100vh - var(--header-height));
    padding: 24px;
    background: var(--bg-regular);
    min-width: 0;
  }

  .report-detail-state {
    width: 100%;
    min-height: 420px;
    background: #fff;
    border: 1px solid $border-regular;
    border-radius: $border-radius-l;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .report-detail-state__loading {
    width: min(760px, calc(100% - 64px));
  }

  .report-detail-state__loading-title {
    color: $text-primary;
    font-size: $font-size-h4;
    font-weight: $font-weight-medium;
    line-height: $line-height-h4;
    margin-bottom: 24px;
  }
}

// 测试用
.test :deep(.f-analyse-wrap) {
  top: 150px !important;
  .faw-header {
    display: none;
  }
}
</style>

<style lang="scss">
// 全局 悬浮样式
.tool-pop {
  .el-table {
    .el-table--border .el-table__inner-wrapper:after,
    .el-table--border:after,
    .el-table--border:before,
    .el-table__inner-wrapper:before {
      display: none;
    }
    th,
    td {
      text-align: center;
      border: none !important;
    }

    .el-table__header th {
      background-color: #eaf3ff !important;
    }

    .cell {
      color: #26292e;
    }

    td.c666 .cell {
      color: #666;
    }

    .mod {
      background-color: #fff !important;
      border: 1px solid #dfe2e8;
      border-radius: 4px;
    }

    .hot {
      color: #ff5959;
      font-weight: 500;
    }
  }
}

// 图表里悬浮
.chartPop {
  background: white;
  border-radius: 4px;
  padding: 0;
  font-size: 12px;
  min-width: 400px;

  &.chartPop_trend {
    min-width: 200px;
  }

  .chartTr {
    background: #f0f8ff;
    padding: 8px 12px;
    color: #26292e;
    border-radius: 4px 4px 0 0;
  }
  .chartTh {
    padding: 8px 12px;
    text-align: center;
    color: #26292e;
    font-weight: 400px;
    font-size: 14px;

    &:first-child {
      text-align: left;
    }
    // &:last-child { text-align: right;}
  }

  .chartTd {
    padding: 8px 12px;
    text-align: center;
    font-weight: 400;
    color: #26292e;
    font-size: 14px;

    &:first-child {
      text-align: left;
    }
    // &:last-child { text-align: right;}
  }

  .c333 {
    color: #333;
  }

  .c666 {
    color: #666;
  }

  .tl {
    text-align: left;
  }
}
</style>
