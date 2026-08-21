<script setup lang="ts">
import { onActivated, reactive, ref, computed, watch } from 'vue'
import HPage from '@h5/components/UI/HPage'
import HDateFilter, { type DateOption } from '@h5/views/home/components/HDateFilter'
import { usePermissionsStore, useShareStore } from '@h5/store'
import { useH5AppStore } from '@h5/store/h5App.ts'
import { getDataBrief, type H5VocBaseRequest } from '@h5/api/home'
import { type H5VocTaskBaseRequest } from '@h5/api/taskEvent'
import type { NegativeRateData } from '@h5/views/home/components/NegativeRateCard/types.d.ts'
import BrandList from '@h5/components/BrandList/index.vue'
import BrowseSummary from '@h5/components/BrowseSummary/index.vue'
import TaskEventFilter from '@h5/views/taskEvent/components/TaskEventFilter/index.vue'
import SingleTaskEventContent from '@h5/views/taskEvent/single/index.vue'
import BatchTaskEventContent from '@h5/views/taskEvent/batch/index.vue'
import { useKeepAliveScroll } from '@h5/hooks/useKeepAliveScroll'
import { useH5MenuVisitRecord } from '@h5/hooks/useH5MenuVisitRecord'
import { useRoute } from 'vue-router'
import { parseShareParamsFromQuery, convertDateParamsToDateOption } from '@h5/utils/shareParams'
import type { DateUnitInfo } from '@h5/views/home/components/HDateFilter/types'

defineOptions({
  name: 'H5TaskEvent'
})

type TaskEventType = 'batch' | 'single'

interface TaskEventTypeOption {
  label: string
  value: TaskEventType
}

interface TaskEventFilterConfirmPayload {
  eventType?: TaskEventType
  filters?: Record<string, any>
}

const taskEventTypeOptions: TaskEventTypeOption[] = [
  { label: '批量', value: 'batch' },
  { label: '单点', value: 'single' }
]

const taskEventPermissionKeyMap: Record<TaskEventType, string> = {
  batch: 'H5TaskEventBatch',
  single: 'H5TaskEventSingle'
}

const userPermStore = usePermissionsStore()
const h5AppStore = useH5AppStore()
const shareStore = useShareStore()
const route = useRoute()

// 记忆并恢复 H5 滚动容器的滚动位置
useKeepAliveScroll()

// H5-事件任务：页面访问操作记录（返回/切换 tab 依赖 keep-alive 的 onActivated）
useH5MenuVisitRecord()

// 获取品牌列表
const brandList = computed(() => userPermStore.getBrandListForHome)

// 根据品牌编码获取品牌名称
const getBrandName = (brandCode: string | undefined): string => {
  if (!brandCode) return ''
  const brand = brandList.value.find((item: any) => item.key === brandCode)
  return brand?.value || ''
}

// 计算分享标题
const shareTitle = computed(() => {
  return '客情直驱'
})

// 格式化时间筛选显示文本
const formatTimeFilter = (dateTime: DateOption | null): string => {
  if (!dateTime) return ''

  // 如果是自定义时间（code === 999 或 name === '自定义'），显示具体时间范围
  if (dateTime.code === 999 || dateTime.name === '自定义') {
    if (dateTime.startTime && dateTime.endTime) {
      // 格式化日期：YYYY-MM-DD -> YYYY-MM-DD（保留完整日期）
      const formatDate = (dateStr: string) => {
        if (!dateStr) return ''
        // 如果已经是 YYYY-MM-DD 格式，直接返回
        return dateStr
      }
      return `${formatDate(dateTime.startTime)}至${formatDate(dateTime.endTime)}`
    }
    return '自定义'
  }

  // 非自定义时间，直接返回名称
  return dateTime.name || ''
}

// 计算分享描述：品牌-时间筛选
const shareDesc = computed(() => {
  const brandName = getBrandName(hub.requestParams.brandCode)
  const timeFilter = formatTimeFilter(hub.currentDateFilter.dateTime)

  const parts = [brandName, timeFilter].filter(Boolean)
  return parts.join('-') || '客情直驱'
})

// 页面数据状态
const hub = reactive<any>({
  // 品牌、时间等筛选请求参数
  requestParams: {} as H5VocTaskBaseRequest,
  // 当前日期筛选结果（周 / 月 / 季 / 年 + 具体区间）
  currentDateFilter: {
    dateUnit: 0,
    dateTime: null as DateOption | null
  },
  // 浏览任务达成情况简报（用于 BrowseSummary 展示浏览条数、时长）
  dataBrief: {
    name: '',
    negativeRate: 0,
    negativeRateMom: 0,
    mentionCount: 0,
    mentionCountMom: 0,
    achieveRate: 0,
    achieveRateTalk: ''
  } as NegativeRateData,
  // 品牌列表请求状态
  pageContentLoading: true,
  fetchBrandListFailed: false
})

// 从权限 Store 映射品牌列表与初始化状态
const brandListFromStore = computed(() => userPermStore.getBrandListForHome)
const hasPermInited = computed(() => userPermStore.hasInited)

// 标记是否已应用分享参数（避免重复应用）
const shareParamsApplied = ref(false)

// 当前事件类型筛选：默认展示批量事件筛选 UI，暂不影响现有接口联调
const currentTaskEventType = ref<TaskEventType>('batch')

// 根据 H5 任务菜单子权限生成批量/单点可见切换项；无子权限时按业务要求兜底展示批量。
const visibleTaskEventTypeOptions = computed<TaskEventTypeOption[]>(() => {
  const menus = userPermStore.menus || []
  const taskEventMenu = menus.find((menu: any) => menu?.permissionKey === 'H5TaskEvent')
  const children = Array.isArray(taskEventMenu?.children) ? taskEventMenu.children : []
  const childPermissionKeys = new Set(
    children.map((child: any) => String(child?.permissionKey || '')).filter(Boolean)
  )
  const permittedOptions = taskEventTypeOptions.filter(item =>
    childPermissionKeys.has(taskEventPermissionKeyMap[item.value])
  )

  return permittedOptions.length > 0 ? permittedOptions : [taskEventTypeOptions[0]]
})

// 批量与单点各自保留高级筛选条件，避免切换事件类型时互相污染
const taskEventFilters = reactive<Record<TaskEventType, Record<string, any>>>({
  batch: {},
  single: {}
})

// 当前事件类型对应的内容组件
const activeContentComponent = computed(() => {
  return currentTaskEventType.value === 'batch' ? BatchTaskEventContent : SingleTaskEventContent
})

// 品牌、时间是公共条件；高级筛选按当前批量/单点类型分别合并
const activeRequestParams = computed<H5VocTaskBaseRequest>(() => {
  return {
    ...hub.requestParams,
    ...taskEventFilters[currentTaskEventType.value]
  }
})

// 解析URL中的分享参数
const shareParamsFromQuery = computed(() => {
  return parseShareParamsFromQuery(route.query as Record<string, any>)
})

// 计算默认品牌编码
// 说明：
// - 事件任务页同样开启 keep-alive，useRoute() 的 route 会随当前路由切换而变化
// - 若默认值直接依赖 route.query.brandCode，则从本页跳转到其它页面（如详情页）时，
//   defaultBrandCode 会跟随其它页面的 query 变化，进而触发 BrandList 重新初始化并 emit brand-click，
//   最终导致本页返回时出现“被动刷新/重刷数据”的体验问题
// 因此：仅在“事件任务页路由”下允许使用分享参数初始化默认品牌。避免路由切换干扰默认值。
const defaultBrandCode = computed(() => {
  // 仅在“事件任务页路由”下允许使用分享参数初始化默认品牌
  if (route.name === 'H5TaskEvent' && shareParamsFromQuery.value?.brandCode) {
    // 验证品牌是否在品牌列表中
    const brandList = brandListFromStore.value
    const brandExists = brandList.some(
      (brand: any) => brand.key === shareParamsFromQuery.value?.brandCode
    )
    if (brandExists) {
      return shareParamsFromQuery.value.brandCode
    }
  }

  return h5AppStore.getDefTaskBrandCode
})

// 计算默认时间配置（优先级：URL参数 > h5AppStore默认值）
const defaultDateUnitInfo = computed<DateUnitInfo>(() => {
  const timeDimension = userPermStore.getTimeDimensionList as DateOption[]

  if (shareParamsFromQuery.value && timeDimension.length > 0) {
    const dateOption = convertDateParamsToDateOption(shareParamsFromQuery.value, timeDimension)
    if (dateOption && shareParamsFromQuery.value.dateUnit !== undefined) {
      return {
        isDef: true,
        dateUnit: shareParamsFromQuery.value.dateUnit,
        dateTime: dateOption
      }
    }
  }

  return h5AppStore.getDateTaskUnitInfo
})

// 根据品牌列表与权限初始化状态，更新“品牌列表请求失败”标记
watch(
  [brandListFromStore, hasPermInited],
  ([list, ready]) => {
    if (!ready) return
    if (!list || list.length === 0) {
      // 权限已加载但品牌列表为空，显示空状态
      hub.pageContentLoading = false
      hub.fetchBrandListFailed = true
    } else {
      hub.fetchBrandListFailed = false

      // 权限初始化完成后，应用URL参数中的分享参数（仅应用数据源，品牌和时间通过组件 props 自动应用）
      if (shareParamsFromQuery.value && !shareParamsApplied.value) {
        shareParamsApplied.value = true

        // 清除分享参数（避免影响后续操作）
        shareStore.clearShareParams()
      }
    }
  },
  { immediate: true }
)

// 权限刷新后保证当前选中项始终在可见范围内；只有单点权限时会自动选中单点。
watch(
  visibleTaskEventTypeOptions,
  options => {
    if (options.some(item => item.value === currentTaskEventType.value)) return
    currentTaskEventType.value = options[0]?.value || 'batch'
  },
  { immediate: true }
)

/**
 * 筛选弹框 - 点击“查询”
 * - 将筛选项接入到当前事件类型专属参数，内容组件将随参数变更自动重载
 */
const handleFilterConfirm = (filters: Record<string, any>) => {
  const payload = filters as TaskEventFilterConfirmPayload
  const eventType = payload.eventType || currentTaskEventType.value

  taskEventFilters[eventType] = {
    ...(payload.filters || filters || {})
  }
}

/**
 * 筛选弹框 - 点击“重置”
 * @param eventType 当前重置的事件类型
 */
const handleFilterReset = (eventType: TaskEventType) => {
  taskEventFilters[eventType] = {}
}

/**
 * 切换事件类型筛选。
 * 切换后由动态组件接管对应四块内容区。
 */
const handleTaskEventTypeChange = (eventType: TaskEventType) => {
  if (currentTaskEventType.value === eventType) return
  currentTaskEventType.value = eventType
}

/**
 * 加载浏览任务简报
 * - 使用首页同一套 getDataBrief 接口与入参结构
 * - 依赖 hub.requestParams 中的品牌、时间、公私域等筛选条件
 */
const fetchDataBrief = async () => {
  try {
    const requestParams: H5VocBaseRequest = {
      ...hub.requestParams,
      // 同步时间区间到请求参数
      startDate: hub.currentDateFilter.dateTime?.startTime || '',
      endDate: hub.currentDateFilter.dateTime?.endTime || ''
    }
    const response = await getDataBrief(requestParams)
    if (response.success && response.result) {
      hub.dataBrief = response.result
    } else {
      hub.dataBrief = {
        name: '',
        negativeRate: 0,
        negativeRateMom: 0,
        mentionCount: 0,
        mentionCountMom: 0,
        achieveRate: 0,
        achieveRateTalk: ''
      }
    }
  } catch (error) {
    console.error('获取浏览任务数据简报失败:', error)
  }
}

/**
 * 汇总入口：品牌 / 日期 / 公私域 任一变化时调用
 * - 仅刷新公共的浏览任务简报；四块事件内容由批量/单点内容组件独立刷新
 */
const fetchCommonData = async () => {
  if (!hub.requestParams.brandCode || !hub.currentDateFilter.dateTime) return
  hub.pageContentLoading = true
  try {
    await fetchDataBrief()
  } finally {
    hub.pageContentLoading = false
  }
}

/**
 * 处理品牌点击
 * - 由 BrandList-v1 管理当前选中品牌，这里仅接收选中结果并触发数据刷新
 */
const handleBrandClick = (brand: any) => {
  if (!brand?.key) return
  hub.requestParams.brandCode = brand.key
  // 只有在时间和品牌都准备好时才触发数据请求
  // 避免在组件初始化时参数不完整就触发请求，导致请求被取消
  if (hub.currentDateFilter.dateTime && hub.requestParams.brandCode) {
    fetchCommonData()
  }
}

/**
 * 日期筛选变化
 * - 来自 HDateFilter 的 change 事件
 */
const dateFilterChange = (type: number, option: DateOption) => {
  if (!option) return
  hub.currentDateFilter.dateUnit = type
  hub.currentDateFilter.dateTime = option

  // 事件列表等子组件依赖时间参数做请求，这里统一挂到 requestParams 上透传
  hub.requestParams.startTime = option.startTime ? `${option.startTime} 00:00:00` : ''
  hub.requestParams.endTime = option.endTime ? `${option.endTime} 23:59:59` : ''

  // 只有在时间和品牌都准备好时才触发数据请求
  // 避免在组件初始化时参数不完整就触发请求，导致请求被取消
  if (hub.requestParams.brandCode && hub.currentDateFilter.dateTime) {
    fetchCommonData()
  }
}

// keep-alive 返回时：刷新数据
const isFirstActivated = ref(true)
onActivated(() => {
  if (!isFirstActivated.value) {
    fetchDataBrief()
  } else {
    isFirstActivated.value = false
  }
})

// 监听分享标题和描述变化，更新到store
watch(
  [shareTitle, shareDesc],
  ([title, desc]) => {
    shareStore.setShareInfo(title, desc)
  },
  { immediate: true }
)
</script>

<template>
  <HPage>
    <!-- 导航栏插槽：浏览条数、时长 + 品牌切换 + 时间&公私域筛选 -->
    <template #nav-bar>
      <!-- 浏览条数、时长，总体文案与点击跳转逻辑与首页一致 -->
      <BrowseSummary
        :data-brief="hub.dataBrief"
        :request-params="hub.requestParams"
        :current-date-filter="hub.currentDateFilter"
      />

      <!-- 品牌列表 / 时间筛选，仅在权限接口成功且存在品牌时展示 -->
      <template v-if="!hub.fetchBrandListFailed">
        <!-- 品牌列表：由 BrandList-v1 负责品牌选择和默认品牌初始化 -->
        <BrandList :default-brand-code="defaultBrandCode" @brand-click="handleBrandClick" />

        <!-- 时间筛选 + 事件筛选入口 -->
        <div
          v-show="hub.currentDateFilter.dateTime && hub.currentDateFilter.dateTime.startTime"
          class="filter-layout"
        >
          <div class="task-event-toolbar flex-y-center">
            <HDateFilter :default-unit-info="defaultDateUnitInfo" @change="dateFilterChange" />
            <div class="event-type-switch" role="tablist" aria-label="事件类型筛选">
              <button
                v-for="item in visibleTaskEventTypeOptions"
                :key="item.value"
                type="button"
                class="event-type-switch__item"
                :class="{ 'is-active': currentTaskEventType === item.value }"
                @click="handleTaskEventTypeChange(item.value)"
              >
                {{ item.label }}
              </button>
            </div>
            <TaskEventFilter
              class="task-event-filter-entry"
              :event-type="currentTaskEventType"
              @confirm="handleFilterConfirm"
              @reset="handleFilterReset"
            />
          </div>
        </div>
      </template>
    </template>

    <!-- 页面内容插槽：占位，后续可在此补充具体任务事件内容 -->
    <template #default>
      <div class="task-event-page m-12">
        <template v-if="hub.pageContentLoading">
          <van-skeleton title :row="4" />
        </template>
        <template v-else-if="hub.fetchBrandListFailed">
          <van-empty description="暂无可用品牌权限，无法展示任务数据" />
        </template>
        <template v-else>
          <component :is="activeContentComponent" :base-request-params="activeRequestParams" />
        </template>
      </div>
    </template>
  </HPage>
</template>

<style scoped lang="scss">
.filter-layout {
  background: #ffffff;
  box-shadow:
    0px 4px 4px 0px rgba(0, 0, 0, 0.02),
    0px 0px 4px 0px rgba(0, 0, 0, 0.02);
  border-radius: 0px 0px 12px 12px;
  border: 1px solid #ebedf0;
  padding: 12px;
}

.task-event-toolbar {
  width: 100%;
  justify-content: flex-start;
  gap: 8px;
  overflow: hidden;
}

.task-event-toolbar :deep(.h-date-filter) {
  flex: none;
}

.event-type-switch {
  flex: none;
  display: inline-flex;
  align-items: center;
  overflow: hidden;
  border-radius: 4px;
  background: #f2f3f5;
}

.task-event-filter-entry {
  flex: none;
  margin-left: auto;
}

.event-type-switch__item {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 56px;
  box-sizing: border-box;
  padding: 6px 12px;
  border: 0;
  border-radius: 4px;
  appearance: none;
  background: transparent;
  font-family: inherit;
  font-size: 14px;
  line-height: 1;
  color: #1f2733;

  &.is-active {
    background: #e2f3fe;
    font-weight: 600;
    color: #0062ff;
  }
}
</style>
