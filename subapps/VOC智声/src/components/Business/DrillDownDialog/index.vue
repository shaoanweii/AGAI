<script setup lang="ts">
/**
 * 下钻分析弹框
 * 支持 Tab 切换与无限下钻
 */

import { ref, computed, watch, defineAsyncComponent, nextTick } from 'vue'
import useGeneralDrillDownStore, { ContentType } from '@/store/modules/generalDrillDown'
import type { DrillDownDialogProps, DrillDownDialogEvents, TabItem } from './types'
import UserDetails from '@components/Business/DrillDownDialog/components/UserDetails.vue'
import AdvancedFilter from '@components/Business/AdvancedFilter/index.vue'
import dayjs from 'dayjs'
import { DrillTabKey } from './constants'
import { useRoute } from 'vue-router'
import { resolveDrillDownActiveTab } from './utils'
import { useQueryStore } from '@/store/modules/query'
import DownloadMoreAction from '@/components/Business/DownloadMoreAction/index.vue'
import { useDownloadAction } from '@/hooks/useDownloadAction'
import { exportVoiceDetail, getDrillDownStatExportRequest } from '@/api/downloadTask'

defineOptions({ name: 'DrillDownDialog' })

// 组件 Props
const props = withDefaults(defineProps<DrillDownDialogProps>(), {
  title: '详细分析',
  visible: false,
  tabs: () => [
    {
      key: DrillTabKey.TREND,
      label: '声量趋势',
      component: DrillTabKey.TREND,
      icon: 'drilldown-line-chart-up'
    },
    {
      key: DrillTabKey.INDICATOR,
      label: '体验分析',
      component: DrillTabKey.INDICATOR,
      icon: 'drilldown-compass'
    },
    {
      key: DrillTabKey.VIEWPOINT,
      label: '观点分析',
      component: DrillTabKey.VIEWPOINT,
      icon: 'drilldown-speak-line'
    },
    {
      key: DrillTabKey.SCENARIO,
      label: '场景分析',
      component: DrillTabKey.SCENARIO,
      icon: 'drilldown-spam-line'
    },
    {
      key: DrillTabKey.CARSERIES,
      label: '车系分析',
      component: DrillTabKey.CARSERIES,
      icon: 'drilldown-car'
    },
    {
      key: DrillTabKey.GEOGRAPHIC,
      label: '地域分析',
      component: DrillTabKey.GEOGRAPHIC,
      icon: 'drilldown-map-pin-line'
    },
    {
      key: DrillTabKey.POPULATION,
      label: '人群特征',
      component: DrillTabKey.POPULATION,
      icon: 'drilldown-spy-line'
    },
    {
      key: DrillTabKey.DATASOURCE,
      label: '数据源分析',
      component: DrillTabKey.DATASOURCE,
      icon: 'drilldown-database'
    },
    {
      key: DrillTabKey.VOICELIST,
      label: '客户原声',
      component: DrillTabKey.VOICELIST,
      icon: 'drilldown-discuss-line'
    }
  ],
  activeTab: ''
})

const emit = defineEmits<DrillDownDialogEvents>()

// 状态管理（下钻专用 store）
const generalDrillDown = useGeneralDrillDownStore()
const queryStore = useQueryStore()
const route = useRoute()

// 本地状态
const currentTab = ref<string>('')
const isVisible = ref<boolean>(false)
const { downloading, downloadByRequest } = useDownloadAction()

// 统一的查询参数与展示参数（来自下钻专用 store）
const queryParams = computed<any>(() => generalDrillDown.ddQueryParams)
const viewParams = computed<any>(() => generalDrillDown.ddViewParams)

// 高级筛选默认值
const afDefault = ref<any>()

// 当前 tab 组件实例
const currentTabRef = ref<any>(null)

// 根据查询参数与来源控制可见 tabs
const filteredTabs = computed(() => {
  let tabs: TabItem[] = [...props.tabs]

  // 下钻场景
  const drillScene: DrillTabKey | '' = (viewParams.value || {}).drillScene || ''

  // 优先使用本层已保存的 tabs（用于返回时恢复上一层展示的内容）
  const savedKeys = (generalDrillDown as any).currentLevel?.view?.visibleTabs as
    | string[]
    | undefined
  if (Array.isArray(savedKeys) && savedKeys.length > 0) {
    const allow = new Set(savedKeys as any)
    return tabs.filter(tab => allow.has(tab.key as any))
  }

  /**
   * ！！！注意 ！！！
   * 观点：【趋势分析、场景、车系、数据源、客户原声】
   * 场景：【趋势分析、观点分析、车系、数据源、客户原声】
   * */
  const {
    startDate,
    endDate,
    tag3Code,
    tag4Code,
    carSeriesCode,
    carSeriesList,
    topic,
    channelCode,
    dealerCode
  } = queryParams.value || {}
  const selectedCarSeriesCount = Array.isArray(carSeriesList)
    ? carSeriesList.filter(item => item !== '' && item !== null && item !== undefined).length
    : 0

  // 趋势：同日范围默认隐藏；若来源为“趋势”下钻则保留当前 tab
  if (startDate && endDate) {
    const shouldHideTrend = Math.abs(dayjs(startDate).diff(dayjs(endDate), 'day')) < 1
    if (shouldHideTrend) {
      tabs = tabs.filter((tab: TabItem) => tab.key !== DrillTabKey.TREND)
    }
  }

  /**
   *  指标隐藏条件：当从指标分析下钻到第4级或者有观点的时候时，隐藏“指标分析”Tab
   *  注意：旅程分析页面只有三级，所以下钻到3级的时候需要隐藏
   */
  if (
    (route.name === 'journeyAnalysis' && tag3Code) ||
    tag4Code ||
    topic ||
    drillScene === DrillTabKey.SCENARIO
  ) {
    tabs = tabs.filter((tab: TabItem) => tab.key !== DrillTabKey.INDICATOR)
  }

  // 观点：当有观点的时候，隐藏“观点分析”Tab
  if (topic) {
    tabs = tabs.filter((tab: TabItem) => tab.key !== DrillTabKey.VIEWPOINT)
  }

  // 场景：当来源为“场景”下钻则保留当前 tab
  if (drillScene === DrillTabKey.SCENARIO) {
    tabs = tabs.filter((tab: TabItem) => tab.key !== DrillTabKey.SCENARIO)
  }

  // 车系隐藏
  if (carSeriesCode || selectedCarSeriesCount === 1) {
    tabs = tabs.filter((tab: TabItem) => tab.key !== DrillTabKey.CARSERIES)
  }

  // 地域隐藏
  if (dealerCode || topic || drillScene === DrillTabKey.SCENARIO) {
    tabs = tabs.filter((tab: TabItem) => tab.key !== DrillTabKey.GEOGRAPHIC)
  }

  // 人群隐藏
  if (topic || drillScene === DrillTabKey.SCENARIO) {
    tabs = tabs.filter((tab: TabItem) => tab.key !== DrillTabKey.POPULATION)
  }

  // 数据源隐藏
  if (channelCode) {
    tabs = tabs.filter((tab: TabItem) => tab.key !== DrillTabKey.DATASOURCE)
  }

  return tabs
})

/**
 * 解析当前层应展示的 Tab。
 * 层级切换时只信任当前层保存的快照与显式入参，避免把旧层级残留的 currentTab 带回当前层。
 * @param fallback 当当前层没有显式快照时可选的兜底 Tab
 * @returns 当前层最终应激活的合法 Tab Key
 */
const resolveCurrentTab = (fallback?: string) => {
  return resolveDrillDownActiveTab(filteredTabs.value, [
    props.activeTab,
    (generalDrillDown as any).currentLevel?.activeTab,
    (viewParams.value || {}).activeTab,
    (viewParams.value || {}).lastDrillFrom,
    fallback
  ])
}

/**
 * 同步当前激活的 Tab。
 * 手动切换、打开弹框和层级返回都会走这里，确保当前层快照与本地展示保持一致。
 * @param tabKey 需要激活的 Tab Key
 * @param syncStore 是否同步回写当前层快照
 */
const applyCurrentTab = (tabKey: string, syncStore = false) => {
  currentTab.value = tabKey

  if (syncStore && tabKey) {
    generalDrillDown.setActiveTab?.(tabKey)
  }
}

/**
 * 重置客户原声高质量筛选，避免弹窗内客户原声 tab 的私有条件跨 tab 残留。
 */
const resetHighQualityFilter = () => {
  queryStore.voiceManagementParams.highQuality = undefined
}

// 将当前可见 tabs 持久到 ddViewParams，供下钻时写入层级
watch(
  () => filteredTabs.value,
  newList => {
    generalDrillDown.updateDDViewParams({ visibleTabs: (newList || []).map(t => t.key) })
  },
  { immediate: true, deep: true }
)

// 当前 tab 数据
const currentTabData = computed(() => {
  return (
    filteredTabs.value.find((tab: TabItem) => tab.key === currentTab.value) || filteredTabs.value[0]
  )
})

/**
 * 获取当前 tab 的展示名称，作为导出菜单后缀。
 * @returns 当前激活 tab 的中文名称
 */
const getCurrentTabLabel = () => {
  return currentTabData.value?.label || ''
}

/**
 * 组装导出菜单文案。
 * @param prefix 导出场景前缀
 * @returns 带当前 tab 名称的导出菜单文案
 */
const buildExportMenu = (prefix: string) => {
  const tabLabel = getCurrentTabLabel()
  return tabLabel ? `${prefix}-${tabLabel}` : prefix
}

// 头部副标题
const headerSubLabel = computed(() => {
  const nameFromView = (viewParams.value || {}).subTitle
  return nameFromView || ''
})

// 外部 v-model:visible 变化
watch(
  () => props.visible,
  (newVal: boolean) => {
    nextTick(() => {
      isVisible.value = newVal
      if (newVal) {
        const nextTab = resolveCurrentTab()
        if (nextTab) {
          applyCurrentTab(nextTab, true)
        }
      }
      if (!newVal && generalDrillDown.headerMode !== 'close') generalDrillDown.hideDetail()
    })
  },
  { immediate: true }
)

// 下钻入参变化时，确保当前 tab 合法
watch(
  () => generalDrillDown.ddQueryParams,
  () => {
    const currentTabExists = filteredTabs.value.some((tab: TabItem) => tab.key === currentTab.value)
    if (!currentTabExists && filteredTabs.value.length > 0) {
      applyCurrentTab(resolveCurrentTab(), true)
    }
  },
  { deep: true }
)

// 当上/下钻切换层级时，store 会写入上一层记录的 activeTab，这里同步激活对应 tab
watch(
  () => (viewParams.value || {}).activeTab,
  newVal => {
    if (newVal && filteredTabs.value.some((t: TabItem) => t.key === newVal)) {
      applyCurrentTab(newVal as string)
    }
  }
)

// 当层级变化（下钻或返回）时，优先恢复该层记录的 activeTab
watch(
  () => (generalDrillDown as any).currentLevel?.id,
  () => {
    const nextTab = resolveCurrentTab()
    if (nextTab) {
      applyCurrentTab(nextTab, true)
    }
  }
)

// 内部可见状态变化
watch(isVisible, (newVal: boolean) => {
  emit('update:visible', newVal)
  if (!newVal) {
    resetHighQualityFilter()
    emit('close')
    generalDrillDown.hideDetail()
  }
})

// 事件与方法
const handleTabClick = (tabKey: string) => {
  if (currentTab.value === tabKey) return

  resetHighQualityFilter()
  applyCurrentTab(tabKey, true)
  emit('tabChange', tabKey)
}

const handleClose = () => {
  isVisible.value = false
}

/**
 * 跳转下载管理前先关闭当前下钻弹框，避免路由切换时弹框残留。
 */
const closeDialogBeforeNavigate = async () => {
  if (!isVisible.value) return

  handleClose()
  await nextTick()
}

const filterConfirm = (conditions: any) => {
  // 将过滤条件写入下钻专用 store
  generalDrillDown.updateDDQueryParams({ filterItems: conditions })
}

const handleOpen = () => {
  // openDD 完成初始化，这里仅设置筛选默认值
  nextTick(() => {
    afDefault.value = (generalDrillDown.ddQueryParams as any)?.filterItems
  })
}

// 动态组件映射
const componentMap: Record<string, any> = {
  trend: defineAsyncComponent(() => import('./components/TrendAnalysis.vue')),
  carSeries: defineAsyncComponent(() => import('./components/CarSeriesAnalysis.vue')),
  indicator: defineAsyncComponent(() => import('./components/IndicatorAnalysis.vue')),
  viewpoint: defineAsyncComponent(() => import('./components/ViewpointAnalysis.vue')),
  scenario: defineAsyncComponent(() => import('./components/ScenarioAnalysis.vue')),
  dataSource: defineAsyncComponent(() => import('./components/DataSourceAnalysis.vue')),
  geographic: defineAsyncComponent(() => import('./components/GeographicAnalysis.vue')),
  population: defineAsyncComponent(() => import('./components/PopulationAnalysis.vue')),
  voiceList: defineAsyncComponent(() => import('./components/VoiceList.vue'))
}

/**
 * 当前 tab 是否展示统计下载。
 * 客户原声页签只允许下载明细数据。
 */
const showStatDownload = computed(() => currentTab.value !== DrillTabKey.VOICELIST)

/**
 * 下载当前 tab 的统计数据。
 * 统计下载接口后续按 tab 补充，这里保留统一接入点。
 */
const handleDownloadStat = async () => {
  await downloadByRequest({
    request:
      props.statDownloadRequest?.(currentTab.value) ||
      getDrillDownStatExportRequest(currentTab.value),
    params: () => ({ ...queryParams.value }),
    exportMenu: buildExportMenu('详细分析'),
    pendingMessage: '下载统计数据接口待配置',
    errorMessage: '下载统计数据失败，请稍后重试',
    dialogOptions: {
      beforeNavigate: closeDialogBeforeNavigate
    }
  })
}

/**
 * 下载当前 tab 的明细数据。
 * 入参与当前 tab 接口查询条件保持一致。
 */
const handleDownloadDetail = async () => {
  await downloadByRequest({
    request: props.detailDownloadRequest || exportVoiceDetail,
    params: () => ({ ...queryParams.value }),
    exportMenu: buildExportMenu('详细分析'),
    errorMessage: '下载明细数据失败，请稍后重试',
    dialogOptions: {
      beforeNavigate: closeDialogBeforeNavigate
    }
  })
}
</script>

<template>
  <el-dialog
    v-model="isVisible"
    destroy-on-close
    :show-close="false"
    align-center
    width="95%"
    style="padding: 0; border-radius: 8px; height: 96%; z-index: 10000"
    header-class="drill-down-dialog-header"
    body-class="drill-down-dialog-body"
    @open="handleOpen"
  >
    <!-- 主内容区 -->
    <template v-if="isVisible && generalDrillDown.headerMode !== 'close'">
      <div v-show="generalDrillDown.componentName === ContentType.MAIN" class="drill-down-dialog">
        <!-- 对话框头部 -->
        <div class="dialog-header">
          <!-- 标题与筛选 -->
          <div class="flex-y-center flex-1">
            <!-- 返回按钮（最底层不显示） -->
            <div
              v-if="generalDrillDown.showBack"
              class="cursor-point"
              @click="generalDrillDown.popLevel()"
            >
              <SvgIcon name="reverse-left" width="24px" height="24px" color="#5F6A7A"></SvgIcon>
            </div>
            <div class="flex-y-center">
              <span class="ml-16 font-600 fs-20 text-primary">{{ title }}</span>
              <div v-if="title && headerSubLabel" class="separator inline-block"></div>
              <span class="font-600 fs-20 text-brand">{{ headerSubLabel }}</span>
            </div>
            <!-- 标签展示（如需启用高级筛选可放开） -->
            <div v-if="false" class="header-filter">
              <AdvancedFilter
                key="dddAf"
                :defaultValue="afDefault"
                @confirm="filterConfirm"
                class="ml-16"
              />
            </div>
          </div>
          <!-- 关闭 -->
          <div class="cursor-point" @click="handleClose">
            <SvgIcon name="drilldown-close" width="40px" height="40px" color="#929AA6"></SvgIcon>
          </div>
        </div>
        <!-- Tab + 内容 -->
        <div class="dialog-body p-24" style="padding-top: 14px">
          <div class="flex items-center justify-between">
            <div class="dd-tags pb-6">
              <el-tag
                v-for="(tag, index) in generalDrillDown.currentLevel?.tags || []"
                :key="index + ':' + tag.text"
                type="primary"
                effect="plain"
                :closable="tag.deletable !== false"
                :title="tag.text"
                @close="generalDrillDown.removeTag(tag, index)"
                class="mr-8 mb-8"
                >{{ tag.text || '' }}
              </el-tag>
            </div>
            <DownloadMoreAction
              v-if="generalDrillDown.componentName === ContentType.MAIN"
              class="mr-12"
              :loading="downloading"
              :show-stat="showStatDownload"
              :show-detail="true"
              @download-stat="handleDownloadStat"
              @download-detail="handleDownloadDetail"
            />
          </div>
          <div class="dialog-tabs cursor-point">
            <div
              v-for="tab in filteredTabs"
              :key="tab.key"
              class="tab-item"
              :class="{ active: currentTab === tab.key }"
              @click="handleTabClick(tab.key)"
            >
              <SvgIcon
                v-if="currentTab !== tab.key"
                :name="tab.icon"
                width="16px"
                height="16px"
                class="mr-8"
                :color="currentTab === tab.key ? '#fff' : '#5F6A7A'"
              />
              <span class="tab-label fs-16">{{ tab.label }}</span>
            </div>
          </div>

          <!-- 弹窗内容 -->
          <div class="dialog-content">
            <component
              v-if="currentTabData?.component && componentMap[currentTabData.component]"
              :is="componentMap[currentTabData.component]"
              :tab-key="currentTab"
              :query-params="queryParams"
              ref="currentTabRef"
            />
            <div v-else class="content-placeholder">
              <el-empty description="暂无内容" />
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- 详情页面容器 -->
    <UserDetails v-if="generalDrillDown.componentName === ContentType.USER" />
  </el-dialog>
</template>

<style lang="scss">
.drill-down-dialog-header {
  padding-bottom: 0 !important;
}

.drill-down-dialog-body {
  height: 100%;
}
</style>

<style lang="scss" scoped>
.drill-down-dialog {
  height: 100%;
  .dialog-header {
    display: flex;
    align-items: center;
    border-bottom: 1px solid #dfe2e8;
    background: #f5f7fa;
    height: 72px;
    padding: 0 24px;
    border-radius: 8px 8px 0 0;

    .separator {
      width: 15px;
      height: 1px;
      margin: 0 16px;
      background-color: #929aa6;
    }

    .dd-tags {
      display: flex;
      flex-wrap: wrap;
    }
  }

  .dialog-tabs {
    overflow-x: auto;
    display: flex;
    align-items: center;
    gap: 4px;

    .tab-item {
      width: 160px;
      height: 48px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      background: #f2f4f7;
      border-radius: 8px 8px 0px 0px;
      color: #5f6a7a;
      font-weight: 500;

      &.active {
        color: #fff;
        background: #1677ff;
        font-weight: 600;
      }
    }
  }

  .dialog-body {
    height: calc(100% - 110px);
  }

  .dialog-content {
    height: calc(100% - 48px);
    padding: 24px;
    border: 1px solid #ebedf0;

    .content-placeholder,
    .content-loading,
    .content-error {
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 200px;
    }
  }
}

.detail-container {
  height: 100%;
  display: flex;
  flex-direction: column;

  .detail-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    border-bottom: 1px solid #dfe2e8;
    background: #f5f7fa;
    height: 72px;
    padding: 0 24px;
    border-radius: 8px 8px 0 0;

    .header-actions {
      display: flex;
      align-items: center;
      gap: 12px;
    }
  }

  .detail-content {
    flex: 1;
    padding: 24px;
    overflow-y: auto;
  }
}
</style>
