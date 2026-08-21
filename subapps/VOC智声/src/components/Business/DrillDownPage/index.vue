<script setup lang="ts">
/**
 * 下钻分析（页面版）
 * - 复用 DrillDownDialog 的业务逻辑与各 Tab 组件
 * - 去掉 el-dialog 容器，避免以弹窗形式展示
 */

import { computed, defineAsyncComponent, nextTick, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import useGeneralDrillDownStore, { ContentType } from '@/store/modules/generalDrillDown'
import type { TabItem } from '@components/Business/DrillDownDialog/types'
import UserDetails from '@components/Business/DrillDownDialog/components/UserDetails.vue'
import dayjs from 'dayjs'
import { DrillTabKey } from '@components/Business/DrillDownDialog/constants'
import { resolveDrillDownActiveTab } from '@components/Business/DrillDownDialog/utils'
import DownloadMoreAction from '@/components/Business/DownloadMoreAction/index.vue'
import { useDownloadAction, type DownloadRequest } from '@/hooks/useDownloadAction'
import { exportVoiceDetail, getDrillDownStatExportRequest } from '@/api/downloadTask'

defineOptions({ name: 'DrillDownPage' })

type DrillDownPageProps = {
  tabs?: TabItem[]
  activeTab?: string
  statDownloadRequest?: (tabKey: string) => DownloadRequest | undefined
  detailDownloadRequest?: DownloadRequest
}

type TabChangePayload = {
  previousTab: string
  nextTab: string
}

const props = withDefaults(defineProps<DrillDownPageProps>(), {
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
  activeTab: '',
  statDownloadRequest: undefined,
  detailDownloadRequest: undefined
})

const emit = defineEmits<{
  (e: 'tab-change', payload: TabChangePayload): void
}>()

const route = useRoute()
const ddStore = useGeneralDrillDownStore()

const currentTab = ref<string>('')
const currentTabRef = ref<any>(null)
const { downloading, downloadByRequest } = useDownloadAction()

const queryParams = computed<any>(() => ddStore.ddQueryParams)
const viewParams = computed<any>(() => ddStore.ddViewParams)

const filteredTabs = computed(() => {
  let tabs: TabItem[] = [...props.tabs]

  const drillScene: DrillTabKey | '' = (viewParams.value || {}).drillScene || ''

  const savedKeys = (ddStore as any).currentLevel?.view?.visibleTabs as string[] | undefined
  if (Array.isArray(savedKeys) && savedKeys.length > 0) {
    const allow = new Set(savedKeys as any)
    return tabs.filter(tab => allow.has(tab.key as any))
  }

  const {
    startDate,
    endDate,
    tag3Code,
    tag4Code,
    carSeriesCode,
    carSeriesList,
    topic,
    channelCode,
    dealerCode,
    tagType
  } = queryParams.value || {}
  const selectedCarSeriesCount = Array.isArray(carSeriesList)
    ? carSeriesList.filter(item => item !== '' && item !== null && item !== undefined).length
    : 0

  if (startDate && endDate) {
    const shouldHideTrend = Math.abs(dayjs(startDate).diff(dayjs(endDate), 'day')) < 1
    if (shouldHideTrend) {
      tabs = tabs.filter((tab: TabItem) => tab.key !== DrillTabKey.TREND)
    }
  }

  if (
    ((route.name === 'journeyAnalysis' || tagType === 'JOUR') && tag3Code) ||
    tag4Code ||
    topic ||
    drillScene === DrillTabKey.SCENARIO
  ) {
    tabs = tabs.filter((tab: TabItem) => tab.key !== DrillTabKey.INDICATOR)
  }

  if (topic) {
    tabs = tabs.filter((tab: TabItem) => tab.key !== DrillTabKey.VIEWPOINT)
  }

  if (drillScene === DrillTabKey.SCENARIO) {
    tabs = tabs.filter((tab: TabItem) => tab.key !== DrillTabKey.SCENARIO)
  }

  if (carSeriesCode || selectedCarSeriesCount === 1) {
    tabs = tabs.filter((tab: TabItem) => tab.key !== DrillTabKey.CARSERIES)
  }

  if (dealerCode || topic || drillScene === DrillTabKey.SCENARIO) {
    tabs = tabs.filter((tab: TabItem) => tab.key !== DrillTabKey.GEOGRAPHIC)
  }

  if (topic || drillScene === DrillTabKey.SCENARIO) {
    tabs = tabs.filter((tab: TabItem) => tab.key !== DrillTabKey.POPULATION)
  }

  if (channelCode) {
    tabs = tabs.filter((tab: TabItem) => tab.key !== DrillTabKey.DATASOURCE)
  }

  return tabs
})

/**
 * 解析当前层应展示的 Tab。
 * 层级切换时优先使用当前层快照与显式传入值，不再沿用上一层残留的 currentTab，避免返回后停留在错误 Tab。
 * @param fallback 当当前层没有显式快照时可选的兜底 Tab
 * @returns 当前层最终应激活的合法 Tab Key
 */
const resolveCurrentTab = (fallback?: string) => {
  return resolveDrillDownActiveTab(filteredTabs.value, [
    props.activeTab,
    (ddStore as any).currentLevel?.activeTab,
    (viewParams.value || {}).activeTab,
    (viewParams.value || {}).lastDrillFrom,
    fallback
  ])
}

/**
 * 同步当前激活的 Tab。
 * 用户切换或层级切换后，需要同时更新本地状态与 store 快照，确保返回上一层时有稳定的恢复依据。
 * @param tabKey 需要激活的 Tab Key
 * @param syncStore 是否同步回写当前层快照
 */
const applyCurrentTab = (tabKey: string, syncStore = false) => {
  currentTab.value = tabKey

  if (syncStore && tabKey) {
    ddStore.setActiveTab?.(tabKey)
  }
}

watch(
  () => filteredTabs.value,
  newList => {
    ddStore.updateDDViewParams({ visibleTabs: (newList || []).map(t => t.key) })
  },
  { immediate: true, deep: true }
)

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

watch(
  () => ddStore.ddQueryParams,
  () => {
    const currentTabExists = filteredTabs.value.some((tab: TabItem) => tab.key === currentTab.value)
    if (!currentTabExists && filteredTabs.value.length > 0) {
      applyCurrentTab(resolveCurrentTab(), true)
    }
  },
  { deep: true }
)

watch(
  () => (viewParams.value || {}).activeTab,
  newVal => {
    if (newVal && filteredTabs.value.some((t: TabItem) => t.key === newVal)) {
      applyCurrentTab(newVal as string)
    }
  }
)

watch(
  () => (ddStore as any).currentLevel?.id,
  () => {
    const nextTab = resolveCurrentTab()
    if (nextTab) {
      applyCurrentTab(nextTab, true)
    }
  }
)

const handleTabClick = (tabKey: string) => {
  const previousTab = currentTab.value
  if (previousTab === tabKey) return

  applyCurrentTab(tabKey, true)
  emit('tab-change', {
    previousTab,
    nextTab: tabKey
  })
}

watch(
  () => filteredTabs.value.length,
  len => {
    if (len > 0 && !currentTab.value) {
      nextTick(() => {
        const nextTab = resolveCurrentTab()
        if (nextTab) {
          applyCurrentTab(nextTab, true)
        }
      })
    }
  },
  { immediate: true }
)

const componentMap: Record<string, any> = {
  trend: defineAsyncComponent(
    () => import('@components/Business/DrillDownDialog/components/TrendAnalysis.vue')
  ),
  carSeries: defineAsyncComponent(
    () => import('@components/Business/DrillDownDialog/components/CarSeriesAnalysis.vue')
  ),
  indicator: defineAsyncComponent(
    () => import('@components/Business/DrillDownDialog/components/IndicatorAnalysis.vue')
  ),
  viewpoint: defineAsyncComponent(
    () => import('@components/Business/DrillDownDialog/components/ViewpointAnalysis.vue')
  ),
  scenario: defineAsyncComponent(
    () => import('@components/Business/DrillDownDialog/components/ScenarioAnalysis.vue')
  ),
  dataSource: defineAsyncComponent(
    () => import('@components/Business/DrillDownDialog/components/DataSourceAnalysis.vue')
  ),
  geographic: defineAsyncComponent(
    () => import('@components/Business/DrillDownDialog/components/GeographicAnalysis.vue')
  ),
  population: defineAsyncComponent(
    () => import('@components/Business/DrillDownDialog/components/PopulationAnalysis.vue')
  ),
  voiceList: defineAsyncComponent(
    () => import('@components/Business/DrillDownDialog/components/VoiceList.vue')
  )
}

/**
 * 当前 tab 是否展示统计下载。
 * 客户原声只保留明细下载。
 */
const showStatDownload = computed(() => currentTab.value !== DrillTabKey.VOICELIST)

/**
 * 下载当前 tab 的统计数据。
 * 具体统计接口后续按 tab 补充，这里先保留统一接入点。
 */
const handleDownloadStat = async () => {
  await downloadByRequest({
    request:
      props.statDownloadRequest?.(currentTab.value) ||
      getDrillDownStatExportRequest(currentTab.value),
    params: () => ({ ...queryParams.value }),
    exportMenu: buildExportMenu('根因分析'),
    pendingMessage: '下载统计数据接口待配置',
    errorMessage: '下载统计数据失败，请稍后重试'
  })
}

/**
 * 下载当前 tab 的明细数据。
 * 入参与当前 tab 接口保持一致。
 */
const handleDownloadDetail = async () => {
  await downloadByRequest({
    request: props.detailDownloadRequest || exportVoiceDetail,
    params: () => ({ ...queryParams.value }),
    exportMenu: buildExportMenu('根因分析'),
    errorMessage: '下载明细数据失败，请稍后重试'
  })
}
</script>

<template>
  <div class="drill-down-page">
    <template v-if="ddStore.headerMode !== 'close'">
      <div v-show="ddStore.componentName === ContentType.MAIN" class="drill-down-page__main">
        <div class="drill-down-page__body">
          <div class="drill-down-page__header flex items-center justify-between">
            <div class="drill-down-page__tags pb-6">
            <div
              v-if="ddStore.showBack"
              class="drill-down-page__back cursor-point mr-8 mb-8"
              @click="ddStore.popLevel()"
            >
              <SvgIcon name="reverse-left" width="24px" height="24px" color="#5F6A7A"></SvgIcon>
            </div>
            <el-tag
              v-for="(tag, index) in ddStore.currentLevel?.tags || []"
              :key="index + ':' + tag.text"
              type="primary"
              effect="plain"
              :closable="tag.deletable !== false"
              :title="tag.text"
              @close="ddStore.removeTag(tag, index)"
              class="mr-8 mb-8"
              >{{ tag.text || '' }}
            </el-tag>
          </div>
          <div class="drill-down-page__actions">
            <DownloadMoreAction
              :loading="downloading"
              :show-stat="showStatDownload"
              :show-detail="true"
              @download-stat="handleDownloadStat"
              @download-detail="handleDownloadDetail"
            />
          </div>
          </div>

          <div class="drill-down-page__tabs cursor-point">
            <div
              v-for="tab in filteredTabs"
              :key="tab.key"
              class="drill-down-page__tab-item"
              :class="{ 'drill-down-page__tab-item--active': currentTab === tab.key }"
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
              <span class="drill-down-page__tab-label fs-16">{{ tab.label }}</span>
            </div>
          </div>

          <div class="drill-down-page__content">
            <component
              v-if="currentTabData?.component && componentMap[currentTabData.component]"
              :is="componentMap[currentTabData.component]"
              :tab-key="currentTab"
              :query-params="queryParams"
              ref="currentTabRef"
            />
            <div v-else class="drill-down-page__content-placeholder">
              <el-empty description="暂无内容" />
            </div>
          </div>
        </div>
      </div>
    </template>

    <UserDetails v-if="ddStore.componentName === ContentType.USER" />
  </div>
</template>

<style lang="scss" scoped>
.drill-down-page {
  height: 100%;
}

.drill-down-page__main {
  height: 100%;
  display: flex;
  flex-direction: column;
  border: 1px solid #ebedf0;
  border-radius: 8px;
  background: #ffffff;
}

.drill-down-page__body {
  flex: 1;
  min-height: 0;
  padding: 14px 24px 24px;
  display: flex;
  flex-direction: column;
}

.drill-down-page__actions {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 6px;
}

.drill-down-page__tags {
  display: flex;
  flex-wrap: wrap;
}

.drill-down-page__tabs {
  overflow-x: auto;
  display: flex;
  align-items: center;
  gap: 4px;
  padding-bottom: 6px;
}

.drill-down-page__tab-item {
  width: 160px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: #f2f4f7;
  border-radius: 8px 8px 0 0;
  color: #5f6a7a;
  font-weight: 500;
}

.drill-down-page__tab-item--active {
  color: #ffffff;
  background: #1677ff;
  font-weight: 600;
}

.drill-down-page__content {
  flex: 1;
  min-height: 0;
  padding: 24px;
  border: 1px solid #ebedf0;
}

.drill-down-page__content-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200px;
}
</style>
