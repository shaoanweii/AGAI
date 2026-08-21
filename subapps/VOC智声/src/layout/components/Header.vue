<template>
  <div
    class="header flex-x-between-x-center"
    :class="{
      'custom-header': ['selfServiceOriginalSoundQuery', 'rootCause'].includes(
        currentRouteName as string
      )
    }"
  >
    <!-- 左侧区域 -->
    <div class="header__left flex-y-center">
      <div class="page-title" v-if="currentRouteName === 'overview'">{{ pageTitle }}</div>
      <div class="page-title" v-if="currentRouteName === 'leaderOverviewPage'">{{ pageTitle }}</div>
      <div class="page-title flex-y-center" v-if="currentRouteName === 'sceneAnalysisMain'">
        <span>场景分析</span>
      </div>

      <div
        class="page-title flex-y-center"
        v-if="
          [
            'subscribeManagement',
            'UserManagement',
            'RoleManagement',
            'configurationManagement',
            'selfServiceOriginalSoundQuery',
            'rootCause',
            'sysReportManagement',
            'sceneManagement',
            'srReportManagement',
            'sysLogQuery',
            'sysDownloadManagement',
            'selfServiceLocalDataAnalysis',
            'sysPushManagement',
            'systemDataSquare'
          ].includes(currentRouteName as string)
        "
      >
        <el-tooltip v-if="isRootCauseReportDetail" :content="rootCauseBackTitle" placement="bottom">
          <button
            type="button"
            class="root-cause-detail-back"
            aria-label="返回报告管理"
            @click="handleRootCauseDetailBack"
          >
            <SvgIcon name="reverse-left" width="24px" height="24px" color="#5F6A7A" />
          </button>
        </el-tooltip>
        <span>{{ pageTitle }}</span>
      </div>
      <div v-if="['selfServiceOriginalSoundQuery'].includes(currentRouteName as string)">
        <SwitchButton
          v-if="hasPermission(ORIGINA_SOUND_QUERY_BTN_MAP.SELECT_DATA)"
          v-model="middlewareStore.originalDataType"
          :options="ORIGINAL_DATA_TYPE_OPTIONS"
        ></SwitchButton>
      </div>

      <div
        v-if="['overview', 'leaderOverviewPage'].includes(currentRouteName as string)"
        class="w-340"
      >
        <FDatePicker
          v-model="times"
          v-model:shortcut-value="queryStore.globalShortcutValue"
          class="ml-8 iw-full"
        ></FDatePicker>
      </div>
      <!-- 单点事件header tab -->
      <div class="h-title" v-if="['CDESinglePointEvent'].includes(route.name as string)">
        <TabSwitch
          :model-value="middlewareStore.singlePointEventPageType"
          :tabs="[
            { label: '全部事件', value: CDESinglePointEventPageType.All },
            { label: '我的事件', value: CDESinglePointEventPageType.Single }
          ]"
          @update:model-value="
            val => middlewareStore.setSinglePointEventPageType(val as CDESinglePointEventPageType)
          "
        />
      </div>
      <!-- 批量事件header tab -->
      <div class="h-title" v-if="['CDEBatchEvent'].includes(route.name as string)">
        <TabSwitch
          :model-value="middlewareStore.batchEventPageType"
          :tabs="[
            { label: '全部事件', value: CDESinglePointEventPageType.All },
            { label: '我的事件', value: CDESinglePointEventPageType.Single }
          ]"
          @update:model-value="
            val => middlewareStore.setBatchEventPageType(val as CDESinglePointEventPageType)
          "
        />
      </div>

      <!-- 闭环评价header tab -->
      <div
        class="h-title"
        v-if="['selfServiceClosedLoopEvaluation'].includes(route.name as string)"
      >
        <TabSwitch
          :model-value="middlewareStore.closedLoopType"
          :tabs="[
            { label: '单点事件分析', value: EventAnalyType.SingleEventAnaly },
            { label: '批量事件分析', value: EventAnalyType.BatchEventAnaly },
            { label: '用户使用分析', value: EventAnalyType.UserUseAnaly }
          ]"
          @update:model-value="val => middlewareStore.setClosedLoopType(val as EventAnalyType)"
        />
      </div>
    </div>

    <!-- 右侧区域 -->
    <div class="header__right flex-y-center">
      <DemoTools v-if="isLocalDemo()" />
      <!-- 聆听 -->
      <div class="listen" v-if="shouldShowListen">
        <img class="listen__icon" :src="noticePng" alt="notice" />
        <div ref="listenTextWrapperRef" class="listen__text-wrapper">
          <div
            ref="listenTextContainerRef"
            class="listen__text-container"
            :class="{ 'marquee-active': needsMarquee }"
          >
            <div ref="listenTextRef" class="listen__text" v-html="listenText"></div>
            <div v-if="needsMarquee" class="listen__text" v-html="listenText"></div>
          </div>
        </div>
        <div
          class="refresh_wrapper"
          :class="{ 'is-refreshing': listenRefreshing }"
          @click="handleRefreshListenText"
        >
          <img class="refresh_line__icon" src="@/assets/images/refresh-line@2x.png" alt="refresh" />
          <div class="refresh_text">刷新</div>
        </div>
      </div>
      <el-button
        v-if="canPublishRootCauseReport"
        type="primary"
        size="large"
        class="iround-8"
        @click="handlePublishRootCauseReport"
      >
        <SvgIcon name="send-plane-line" width="20px" height="20px" color="#FFFFFF" />
        <span class="ml-8">发布</span>
      </el-button>
      <el-button
        v-if="canExportCurrentPage"
        type="primary"
        plain
        size="large"
        class="iround-8"
        :loading="exporting"
        @click="handleExportPdf"
      >
        <span>导出为PDF</span>
      </el-button>
      <!-- 用户信息 -->
      <el-dropdown trigger="click" @command="handleUserCommand">
        <div class="user-info text-h4 font-500">
          <div class="avatar font-600 flex-center ml-4">U</div>
          <span class="user-name">{{ userInfo.userName }}</span>
          <el-icon class="user-arrow mr-12">
            <ArrowDown />
          </el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item
              v-if="userStore.checkfunctionPermission(FunctionPermission.GO_TO_INSIGHTS)"
              command="linkIns"
            >
              <div
                class="flex items-center lh-36 custom-item"
                @mouseenter="settingsIconColor = '#1677ff'"
                @mouseleave="settingsIconColor = '#717680'"
              >
                <SvgIcon name="recording-01" :color="settingsIconColor" />
                <div class="font-600 ml-10">前往洞察引擎</div>
              </div>
            </el-dropdown-item>
            <el-dropdown-item
              :divided="userStore.checkfunctionPermission(FunctionPermission.GO_TO_INSIGHTS)"
              command="logout"
            >
              <div
                class="flex items-center lh-36 custom-item"
                @mouseenter="logoutIconColor = '#1677ff'"
                @mouseleave="logoutIconColor = '#717680'"
              >
                <SvgIcon name="log-out-01" :color="logoutIconColor" />
                <div class="font-600 ml-10">退出登录</div>
              </div>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
    <PublishReport v-model="publicReportVisible" />
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/store/modules/app'
import { ElMessage, ElMessageBox } from 'element-plus'
import useUserStore from '@/store/modules/user'
import { useQueryStore } from '@/store/modules/query'
import { insUrl } from '@/constants/env'
import { insFreeLogin } from '@/api/common'
import { enCrypt } from '@/utils/encryption'
import { useLoading } from '@/hooks/useLoading'
import { usePdfExport } from '@/hooks/usePdfExport'
import useMiddlewareStore from '@/store/modules/middleware'
import useSceneAnalysisStore from '@/store/modules/sceneAnalysis'
import {
  CDESinglePointEventPageType,
  ORIGINAL_DATA_TYPE_OPTIONS,
  EventAnalyType
} from '@/constants'
import TabSwitch from '@/components/UI/TabSwitch/index.vue'
import { hasPermission } from '@/utils/permission'
import { FunctionPermission, ORIGINA_SOUND_QUERY_BTN_MAP } from '@/constants/btnPermMap'
import { getBrowseSummaryBrief } from '@/api/overview'
import noticePng from '@/assets/images/notice.png'
import PublishReport from '@/components/Business/Scene/Common/PublishReport/index.vue'
import DemoTools from '@/components/Business/DemoTools/index.vue'
import { isLocalDemo } from '@/utils/env'

defineOptions({
  name: 'LayoutHeader'
})

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()
const queryStore = useQueryStore()
const middlewareStore = useMiddlewareStore()
const sceneAnalysisStore = useSceneAnalysisStore()

const userInfo = computed(() => appStore.userInfo)

const settingsIconColor = ref('#717680')
const logoutIconColor = ref('#717680')
const listenText = ref('')
const listenTextWrapperRef = ref<HTMLElement>()
const listenTextRef = ref<HTMLElement>()
const listenTextContainerRef = ref<HTMLElement>()
const needsMarquee = ref(false)
const listenRefreshing = ref(false)
let listenRequestSerial = 0

const LISTEN_HIDDEN_ROUTE_NAMES = [
  'CDESinglePointEvent',
  'CDEBatchEvent',
  'groupAnalysis',
  'thisProductAnalysis',
  'competitorAnalysis',
  'journeyAnalysis',
  'productAnalysis',
  'serviceAnalysis'
]

const currentRouteName = computed(() => route.name)
const shouldShowListen = computed(() => !LISTEN_HIDDEN_ROUTE_NAMES.includes(route.name as string))
const publicReportVisible = ref(false)

/**
 * 归一化路由查询参数，兼容数组和空值。
 * @param value 路由 query 参数
 * @returns 可展示的字符串值
 */
const normalizeRouteQueryValue = (value: unknown): string => {
  if (Array.isArray(value)) {
    return normalizeRouteQueryValue(value[0])
  }

  return typeof value === 'string' ? value.trim() : ''
}

const rootCauseDetailReportName = computed(() => normalizeRouteQueryValue(route.query.reportName))
const rootCauseStoreReportName = computed(
  () => sceneAnalysisStore.sceneOriginData.reportName || rootCauseDetailReportName.value
)
const rootCauseDetailFromPath = computed(() => normalizeRouteQueryValue(route.query.from))

/**
 * 根因分析报告详情使用报告名称和返回入口，兼容系统报告管理与外部直链。
 */
const isRootCauseReportDetail = computed(() => {
  return (
    route.name === 'rootCause' &&
    route.query.isBack === '1' &&
    Boolean(rootCauseStoreReportName.value)
  )
})

const pageTitle = computed(() => {
  return isRootCauseReportDetail.value ? rootCauseStoreReportName.value : route.meta.title
})

const rootCauseBackTitle = computed(() =>
  rootCauseDetailFromPath.value === '/system/report' ? '返回报告管理' : '返回场景分析'
)

/**
 * 返回报告来源页；外部直链默认返回 VOC 场景分析。
 */
const handleRootCauseDetailBack = () => {
  router.push({ path: rootCauseDetailFromPath.value || '/scene/analysis' })
}

/**
 * 根因分析仅在拥有场景发布权限且非已发布报告详情态时显示发布入口。
 */
const canPublishRootCauseReport = computed(() => {
  return (
    route.name === 'rootCause' &&
    !isRootCauseReportDetail.value &&
    !route.query.reportJudgeId &&
    userStore.checkfunctionPermission(FunctionPermission.SCENARIO_PUBLISH)
  )
})

/**
 * 打开根因分析专题报告发布弹窗，复用当前筛选器已缓存的查询条件。
 */
const handlePublishRootCauseReport = () => {
  publicReportVisible.value = true
}

const { showLoading, hideLoading } = useLoading()
const { exporting, canExportCurrentPage, handleExportPdf } = usePdfExport({
  trigger: 'layoutHeader',
  getTitle: () => String(route.meta.title || '领导总览')
})

/**
 * 检查聆听文案是否超出容器，超出时启用跑马灯
 */
const checkListenMarquee = () => {
  requestAnimationFrame(() => {
    if (!listenTextWrapperRef.value || !listenTextRef.value) {
      needsMarquee.value = false
      return
    }

    const textWidth = listenTextRef.value.scrollWidth
    const containerWidth = listenTextWrapperRef.value.clientWidth

    if (textWidth > containerWidth && textWidth > 0) {
      needsMarquee.value = true
      const speed = 50
      const duration = Math.max(10, textWidth / speed)

      if (listenTextContainerRef.value) {
        listenTextContainerRef.value.style.setProperty('--marquee-duration', `${duration}s`)
        listenTextContainerRef.value.style.setProperty('--text-width', `${textWidth}px`)
      }
      return
    }

    needsMarquee.value = false
    if (listenTextContainerRef.value) {
      listenTextContainerRef.value.style.removeProperty('--marquee-duration')
      listenTextContainerRef.value.style.removeProperty('--text-width')
    }
  })
}

/**
 * 重置聆听区文案与跑马灯状态（用于无需展示聆听区的页面）
 */
const resetListenState = () => {
  // 路由切走时递增序号，确保旧请求返回后不会覆盖当前页面状态。
  listenRequestSerial += 1
  listenRefreshing.value = false
  listenText.value = ''
  needsMarquee.value = false
  if (listenTextContainerRef.value) {
    listenTextContainerRef.value.style.removeProperty('--marquee-duration')
    listenTextContainerRef.value.style.removeProperty('--text-width')
  }
}

/**
 * 获取顶部聆听区浏览时长文案
 */
const fetchListenBrief = async () => {
  if (listenRefreshing.value || !shouldShowListen.value) {
    return
  }

  const currentRequestSerial = ++listenRequestSerial
  listenRefreshing.value = true

  try {
    const res = await getBrowseSummaryBrief()
    if (currentRequestSerial !== listenRequestSerial || !shouldShowListen.value) {
      return
    }
    listenText.value = res.result || ''
  } catch {
    if (currentRequestSerial !== listenRequestSerial || !shouldShowListen.value) {
      return
    }
    listenText.value = ''
  } finally {
    if (currentRequestSerial === listenRequestSerial) {
      listenRefreshing.value = false
      nextTick(() => {
        checkListenMarquee()
      })
    }
  }
}

/**
 * 手动刷新顶部聆听区文案，请求中禁止重复触发。
 */
const handleRefreshListenText = () => {
  fetchListenBrief()
}

watch(
  () => listenText.value,
  () => {
    nextTick(() => {
      checkListenMarquee()
    })
  }
)

watch(
  () => route.name,
  routeName => {
    if (LISTEN_HIDDEN_ROUTE_NAMES.includes(routeName as string)) {
      resetListenState()
      return
    }
    fetchListenBrief()
  },
  { immediate: true }
)

const handleResize = () => {
  checkListenMarquee()
}

const times = computed<string[]>({
  get: () => {
    if (queryStore.currentQueryParams.startDate && queryStore.currentQueryParams.endDate) {
      return [queryStore.currentQueryParams.startDate, queryStore.currentQueryParams.endDate]
    }
    return []
  },
  set: (value: string[]) => {
    if (value && value.length === 2) {
      queryStore.updateQueryParams({
        startDate: value[0],
        endDate: value[1]
      })
    }
  }
})

const handleUserCommand = async (command: string) => {
  switch (command) {
    case 'linkIns': {
      try {
        showLoading()
        const res = await insFreeLogin({
          username: enCrypt(appStore.userInfo.userName as string),
          userId: appStore.userInfo.id as string
        })
        if (res.success && res.result.access_token) {
          const token = res.result.access_token
          window.open(`${insUrl}?token=${token}`, '_blank')
        } else {
          ElMessage.error('暂无权限访问洞察引擎，请联系管理员')
        }
      } finally {
        hideLoading()
      }

      break
    }
    case 'logout': {
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        userStore.logout()
        ElMessage.success('已退出登录')
      } catch {
        // 用户取消
      }
      break
    }
  }
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
.header {
  height: var(--header-height);
  background: var(--bg-regular);
  padding: 0 32px;
  border-bottom: 1px solid var(--border-dark);

  &__left {
    gap: 24px;
  }

  &__right {
    gap: 12px;
  }

  .root-cause-detail-back {
    width: 40px;
    height: 40px;
    margin-right: 16px;
    padding: 0;
    border: 0;
    border-radius: 8px;
    background: #f2f4f7;
    color: #5f6a7a;
    cursor: pointer;

    &:hover {
      background: #eaecf0;
    }

    &:focus-visible {
      outline: 2px solid #1677ff;
      outline-offset: 2px;
    }
  }

  .h-title {
    font-weight: 600;
    font-size: 24px;
    color: #1f2733;

    .untap {
      font-weight: 500;
      font-size: 20px;
      color: #333333;
    }
  }

  .listen {
    width: 566px;
    height: 40px;
    background: rgba(255, 255, 255, 0.5);
    border-radius: 40px 40px 40px 40px;
    border: 1px solid #bde2ff;
    display: flex;
    align-items: center;
    padding: 8px 16px;
    box-sizing: border-box;

    &__icon {
      width: 24px;
      height: 24px;
      flex-shrink: 0;
      margin-right: 8px;
    }

    &__text-wrapper {
      flex: 1;
      min-width: 0;
      overflow: hidden;
    }

    &__text-container {
      display: inline-flex;
      white-space: nowrap;
      animation: none;
      will-change: transform;

      &.marquee-active {
        animation: listen-marquee var(--marquee-duration, 15s) linear infinite;
      }
    }

    &__text {
      line-height: 20px;
      font-weight: 400;
      font-size: 14px;
      color: #1f2733;
      white-space: nowrap;
      display: inline-block;
      flex-shrink: 0;
      padding-right: 30px;

      :deep(.highlight) {
        padding-left: 2px;
        padding-right: 2px;
        font-weight: 400;
        color: #1677ff;
        font-size: 14px;
      }
    }

    .refresh_wrapper {
      margin-left: 8px;
      border-left: 1px solid #dfe2e8;
      padding-left: 8px;
      display: flex;
      align-items: center;
      cursor: pointer;

      &.is-refreshing {
        opacity: 0.6;
        pointer-events: none;
      }

      .refresh_line__icon {
        width: 16px;
        height: 16px;
      }
      .refresh_text {
        margin-left: 8px;
        font-weight: 400;
        font-size: 14px;
        color: #1677ff;
        line-height: 20px;
      }
    }
  }

  @keyframes listen-marquee {
    0% {
      transform: translateX(0);
    }

    100% {
      transform: translateX(calc(-1 * var(--text-width, 50%)));
    }
  }
}

.custom-header {
  background: #fff;
}

.my-report-link {
  cursor: pointer;
}

.user-info {
  background: #f8f8f8;
  border-radius: 24px 24px 24px 24px;
  border: 1px solid #d5d7da;
  height: 40px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-sizing: border-box;

  &:hover {
    background: rgba(255, 255, 255, 0.1);
  }

  .avatar {
    width: 32px;
    height: 32px;
    background: #4a9eff;
    border-radius: 50%;
    color: #fff;
  }

  .user-name {
    color: #1b212d;
  }

  .user-arrow {
    color: #999999;
    font-size: 20px;
  }
}

:deep(.el-dropdown-menu) {
  .el-dropdown-menu__item {
    display: flex;
    align-items: center;
    gap: 8px;

    .el-icon {
      margin-right: 0;
    }
  }
}
.custom-item {
  margin: -5px -16px;
  padding: 5px 16px;
}
</style>
