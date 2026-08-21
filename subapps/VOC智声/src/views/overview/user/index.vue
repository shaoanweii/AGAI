<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import BrandView from './components/BrandView/index.vue'
import GeneralScenario from './components/GeneralScenario/index.vue'
import SpecialAnalysis from './components/SpecialAnalysis/index.vue'
import TopTrendingEvents from './components/TopTrendingEvents/index.vue'
import CustomerRant from './components/CustomerRant/index.vue'
import CustomerDrivenDirectly from './components/CustomerDrivenDirectly/index.vue'
import IntelligentQuestionCountCard from './components/IntelligentQuestionCountCard/index.vue'
import {
  getBrandBriefReport,
  getSpecializedAnalysis,
  getGeneralScenario
} from '@/api/overview/index'
import type { ProductExperienceIndexVo, HomeReportTopVo, HomeMenuVo } from '@/api/overview/type'
import type { BatchEventDashboardEventType, DashboardStatCard } from '@/api/batchEvent/types'
import { useQueryListener } from '@/hooks/useQueryListener'
import DrillDownDialog from '@components/Business/DrillDownDialog/index.vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import useMiddlewareStore from '@/store/modules/middleware'
import {
  hasCanswerMenuPermission,
  hasCustomerDrivenDirectlyDetailMenuPermission,
  hasOriginalSoundQueryMenuPermission
} from '@/utils/permission'
import { eventSwitchOpts, OriginalDataType } from '@/constants'

defineOptions({
  name: 'UserOverview'
})

const router = useRouter()
const userStore = useUserStore()
const middlewareStore = useMiddlewareStore()

const GLOBAL_FILTER_CACHE_KEY = '__default__'
const ORIGINAL_SOUND_QUERY_RESULT_DATA_CACHE_KEY = 'OriginalSoundQuery_ResultData'
const ORIGINAL_SOUND_QUERY_ORIGINAL_DATA_CACHE_KEY = 'OriginalSoundQuery_OriginalData'
const ORIGINAL_SOUND_QUERY_SHARED_CACHE_KEY = 'OriginalSoundQuery_Shared'
const SHARED_TIME_CACHE_KEYS = ['startDate', 'endDate', 'dateRange', 'globalShortcutValue'] as const
const CUSTOMER_DRIVEN_DIRECTLY_NO_PERMISSION_MESSAGE =
  '您当账号暂无权限查看明细，如有需要请联系管理员开通。'
const CUSTOMER_DRIVEN_DIRECTLY_LIST_CONFIG: Record<
  BatchEventDashboardEventType,
  {
    path: string
    permissionKey: string
  }
> = {
  BATCH: {
    path: '/customerDirectEngage/batchEvent',
    permissionKey: 'CDEBatchEvent'
  },
  SINGLE: {
    path: '/customerDirectEngage/singlePointEvent',
    permissionKey: 'CDESinglePointEvent'
  }
}

// 事件类型
const eventType = ref<BatchEventDashboardEventType>(
  eventSwitchOpts[0].value as BatchEventDashboardEventType
)

const eventSwitchValue = computed<string | number>({
  get: () => eventType.value,
  set: value => {
    if (value === 'BATCH' || value === 'SINGLE') {
      eventType.value = value
    }
  }
})

/**
 * 根据菜单权限决定是否展示智能问数卡片。
 */
const hasCanswerMenu = computed(() => {
  return hasCanswerMenuPermission(userStore.menus || [])
})

/**
 * 智能问数卡片隐藏时，让品牌区占满整行。
 */
const brandViewSpan = computed(() => (hasCanswerMenu.value ? 16 : 24))

// 品牌简报数据
const brandReportData = ref<ProductExperienceIndexVo[]>([])
const brandReportLoading = ref(false)

// 专项分析数据
const specialAnalysisData = ref<HomeReportTopVo[]>([])
const specialAnalysisLoading = ref(false)

// 通用场景数据
const generalScenarioData = ref<HomeMenuVo[] | null>(null)
const generalScenarioLoading = ref(false)

/**
 * 获取品牌简报数据
 */
const fetchBrandBriefReport = async () => {
  try {
    brandReportLoading.value = true
    // 确保必需的字段存在
    const queryParams: VocQueryParams = {
      startDate: queryStore.currentQueryParams.startDate,
      endDate: queryStore.currentQueryParams.endDate
    }
    const response = await getBrandBriefReport(queryParams)

    if (response.success) {
      brandReportData.value = response.result || []
    } else {
      ElMessage.error(response.message || '获取品牌简报数据失败')
    }
  } catch (error) {
    console.error('获取品牌简报数据失败:', error)
    ElMessage.error('获取品牌简报数据失败，请稍后重试')
  } finally {
    brandReportLoading.value = false
  }
}

/**
 * 获取专项分析数据
 */
const fetchSpecializedAnalysis = async () => {
  try {
    specialAnalysisLoading.value = true
    // 确保必需的字段存在
    const queryParams: any = {
      startDate: queryStore.currentQueryParams.startDate,
      endDate: queryStore.currentQueryParams.endDate,
      roleIds: userStore.roleId ? [userStore.roleId] : undefined
    }
    const response = await getSpecializedAnalysis(queryParams)

    if (response.success) {
      specialAnalysisData.value = response.result || []
    } else {
      ElMessage.error(response.message || '获取专项分析数据失败')
    }
  } catch (error) {
    console.error('获取专项分析数据失败:', error)
    ElMessage.error('获取专项分析数据失败，请稍后重试')
  } finally {
    specialAnalysisLoading.value = false
  }
}

/**
 * 获取通用场景数据
 */
const fetchGeneralScenario = async () => {
  try {
    generalScenarioLoading.value = true
    // 确保必需的字段存在
    const queryParams: VocQueryParams = {
      startDate: queryStore.currentQueryParams.startDate,
      endDate: queryStore.currentQueryParams.endDate
    }
    const response = await getGeneralScenario(queryParams)

    if (response.success && response.result) {
      generalScenarioData.value = response.result || null
    } else {
      ElMessage.error(response.message || '获取通用场景数据失败')
    }
  } catch (error) {
    console.error('获取通用场景数据失败:', error)
    ElMessage.error('获取通用场景数据失败，请稍后重试')
  } finally {
    generalScenarioLoading.value = false
  }
}

/**
 * 刷新所有数据
 */
const refreshAllData = () => {
  fetchBrandBriefReport()
  fetchSpecializedAnalysis()
  fetchGeneralScenario()
}

// 使用查询监听 hooks
const { queryStore } = useQueryListener(refreshAllData)

// 处理通用场景点击事件
const handleGSItemClick = (item: HomeMenuVo) => {
  console.log('item', item)
  router.push({
    path: item.htmlUri
  })
  // if (item.name === '产品分析') {
  //   generalScenarioStore.handleOpen('ProductAnalysis')
  // } else if (item.name === '集团分析') {
  //   generalScenarioStore.handleOpen('GroupAnalysis')
  // } else if (item.name === '服务分析') {
  //   generalScenarioStore.handleOpen('ServiceAnalysis')
  // } else if (item.name === '竞品对比') {
  //   generalScenarioStore.handleOpen('CompetitorAnalysis')
  // } else if (item.name === '本品分析') {
  //   generalScenarioStore.handleOpen('ThisProductAnalysis')
  // } else if (item.name === '旅程分析') {
  //   generalScenarioStore.handleOpen('JourneyAnalysis')
  // }
}

const dialogVisible = ref(false)

const brandChange = (brandItem: any) => {
  if (brandItem.name === '智行汽车集团') {
    router.push({
      path: '/scene/groupAnalysis'
      // query: {
      //   brandId: brandItem.id
      // }
    })
  } else {
    router.push({
      path: '/scene/thisProductAnalysis',
      query: {
        brandCode: brandItem.brandCode
      }
    })
  }
}

const linkAnalysis = () => {
  router.push({
    path: '/scene/analysis'
  })
}

/**
 * 跳转至原声查询页，并带入当前总览页时间与“抱怨”意图。
 * 说明：
 * 1. 时间沿用 UniversaFilter 的全局共享缓存，保证目标页直接回显当前时间范围。
 * 2. 意图仅写入 ResultData 页签私有缓存，避免串到 OriginalData 页签。
 * 3. 进入前清理原声查询页内共享/原始数据缓存，确保本次跳转只保留约定条件。
 */
const handleCustomerRantMore = () => {
  if (!hasOriginalSoundQueryMenuPermission(userStore.menus || [])) {
    ElMessage.warning('暂无原声查询菜单权限,请联系管理员')
    return
  }

  const { startDate, endDate } = queryStore.currentQueryParams
  const existingGlobalCache =
    queryStore.getUniversaFilterCacheSearchParams(GLOBAL_FILTER_CACHE_KEY) || {}
  const nextGlobalCache: Record<string, any> = { ...existingGlobalCache }

  SHARED_TIME_CACHE_KEYS.forEach(key => {
    delete nextGlobalCache[key]
  })

  if (startDate && endDate) {
    Object.assign(nextGlobalCache, {
      startDate,
      endDate,
      dateRange: 'custom',
      globalShortcutValue: '自定义'
    })
  }

  queryStore.setUniversaFilterCacheSearchParams(nextGlobalCache, GLOBAL_FILTER_CACHE_KEY)
  queryStore.clearUniversaFilterCacheSearchParams(ORIGINAL_SOUND_QUERY_SHARED_CACHE_KEY)
  queryStore.clearUniversaFilterCacheSearchParams(ORIGINAL_SOUND_QUERY_ORIGINAL_DATA_CACHE_KEY)
  queryStore.setUniversaFilterCacheSearchParams(
    {
      intentionList: ['抱怨']
    },
    ORIGINAL_SOUND_QUERY_RESULT_DATA_CACHE_KEY
  )

  middlewareStore.setOriginalDataType(OriginalDataType.ResultData)
  router.push({
    path: '/selfService/originalSoundQuery'
  })
}

/**
 * 按当前客情直驱类型获取对应事件列表页路径。
 * @returns 批量事件或单点事件列表路径
 */
const getCustomerDrivenDirectlyListPath = () => {
  return CUSTOMER_DRIVEN_DIRECTLY_LIST_CONFIG[eventType.value].path
}

/**
 * 按当前客情直驱类型校验对应明细页菜单权限。
 * @returns 当前账号是否具备明细页权限
 */
const checkCustomerDrivenDirectlyDetailPermission = () => {
  const { permissionKey, path } = CUSTOMER_DRIVEN_DIRECTLY_LIST_CONFIG[eventType.value]
  const hasPermission = hasCustomerDrivenDirectlyDetailMenuPermission(
    userStore.menus || [],
    permissionKey,
    path
  )

  if (!hasPermission) {
    ElMessage.warning(CUSTOMER_DRIVEN_DIRECTLY_NO_PERMISSION_MESSAGE)
  }

  return hasPermission
}

/**
 * 构建客情直驱列表页跳转参数，沿用总览时间，统计卡片额外带事件状态。
 * @param taskStatus 统计卡片返回的状态编码
 * @returns 路由 query
 */
const buildCustomerDrivenDirectlyQuery = (taskStatus?: string) => {
  const query: Record<string, string> = {
    eventType: eventType.value,
    brandCodes: ''
  }

  if (queryStore.currentQueryParams.startDate) {
    query.startDate = queryStore.currentQueryParams.startDate
  }

  if (queryStore.currentQueryParams.endDate) {
    query.endDate = queryStore.currentQueryParams.endDate
  }

  if (taskStatus) {
    query.taskStatus = taskStatus
  }

  return query
}

/**
 * 查看更多时进入当前类型对应的事件列表页。
 */
const handleCustomerDrivenDirectlyMore = () => {
  if (!checkCustomerDrivenDirectlyDetailPermission()) {
    return
  }

  router.push({
    path: getCustomerDrivenDirectlyListPath(),
    query: buildCustomerDrivenDirectlyQuery()
  })
}

/**
 * 点击统计卡片时带状态过滤进入对应事件列表页。
 * @param card 后端返回的统计卡片
 */
const handleCustomerDrivenDirectlyStatClick = (card: DashboardStatCard) => {
  if (!checkCustomerDrivenDirectlyDetailPermission()) {
    return
  }

  router.push({
    path: getCustomerDrivenDirectlyListPath(),
    query: buildCustomerDrivenDirectlyQuery(card.status)
  })
}
</script>

<template>
  <div>
    <el-row :gutter="24">
      <el-col :span="brandViewSpan">
        <BrandView
          :data="brandReportData"
          :loading="brandReportLoading"
          @brandChange="brandChange"
        ></BrandView>
      </el-col>
      <el-col v-if="hasCanswerMenu" :span="8">
        <IntelligentQuestionCountCard></IntelligentQuestionCountCard>
      </el-col>
    </el-row>

    <!-- <el-button @click="handleGSItemClick({ name: '产品分析' })">产品分析</el-button>
    <el-button @click="handleGSItemClick({ name: '服务分析' })">服务分析</el-button>
    <el-button @click="handleGSItemClick({ name: '集团分析' })">集团分析</el-button>
    <el-button @click="handleGSItemClick({ name: '竞品分析' })">竞品分析</el-button>
    <el-button @click="handleGSItemClick({ name: '本品分析' })">本品分析</el-button>
    <el-button @click="handleGSItemClick({ name: '旅程分析' })">旅程分析</el-button> -->
    <!-- <el-button @click="() => (dialogVisible = true)">下钻弹框</el-button> -->
    <!-- 下钻 -->
    <DrillDownDialog v-model:visible="dialogVisible" />

    <el-row class="mt-24" :gutter="24">
      <el-col :span="8">
        <FCard
          :title="'洞察报告'"
          title-size="middle"
          :height="'364px'"
          isShowMore
          tooltip="洞察报告"
          @handleMore="linkAnalysis"
        >
          <GeneralScenario
            :data="generalScenarioData"
            :loading="generalScenarioLoading"
            @itemClick="handleGSItemClick"
          ></GeneralScenario>
        </FCard>
      </el-col>
      <el-col :span="8">
        <FCard
          :title="'专项分析'"
          title-size="middle"
          :height="'364px'"
          isShowMore
          @handleMore="linkAnalysis"
        >
          <SpecialAnalysis
            :data="specialAnalysisData"
            :loading="specialAnalysisLoading"
            @refresh="fetchSpecializedAnalysis"
          ></SpecialAnalysis>
        </FCard>
      </el-col>
      <el-col :span="8">
        <FCard :title="'热点事件TOP'" title-size="middle" :height="'364px'">
          <TopTrendingEvents></TopTrendingEvents>
        </FCard>
      </el-col>
    </el-row>

    <el-row class="mt-24" :gutter="24">
      <el-col :span="16">
        <FCard
          :title="'用户观点'"
          title-size="middle"
          :height="'370px'"
          isShowMore
          @handleMore="handleCustomerRantMore"
        >
          <CustomerRant></CustomerRant>
        </FCard>
      </el-col>

      <el-col :span="8">
        <FCard
          :title="'客情直驱'"
          title-size="middle"
          :height="'370px'"
          isShowMore
          class="customer-driven-card"
          @handleMore="handleCustomerDrivenDirectlyMore"
        >
          <template #leftExtra>
            <SwitchButton v-model="eventSwitchValue" :options="eventSwitchOpts"></SwitchButton>
          </template>
          <CustomerDrivenDirectly
            :start-date="queryStore.currentQueryParams.startDate"
            :end-date="queryStore.currentQueryParams.endDate"
            :event-type="eventType"
            @stat-click="handleCustomerDrivenDirectlyStatClick"
          ></CustomerDrivenDirectly>
        </FCard>
      </el-col>
    </el-row>
  </div>
</template>

<style lang="scss" scoped>
@media (max-width: 1200px) {
  .customer-driven-card {
    :deep(.fc-header) {
      padding: 16px 12px 0;
    }

    :deep(.fch-left) {
      min-width: 0;
      flex-wrap: wrap;
      row-gap: 4px;
    }

    :deep(.fch-right__more span) {
      display: none;
    }

    :deep(.fc-body) {
      padding: 16px 12px;
    }
  }
}
</style>
