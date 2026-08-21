<script setup lang="ts">
import { ref, onMounted, computed, watch, onBeforeMount, onBeforeUnmount, onUnmounted } from 'vue'
import { useGeneralScenarioStore } from '@/store'
import { useQueryStore } from '@/store/modules/query'
import SCHeader from '@/components/Business/Scene/Common/SCHeader/index.vue'
import ReportSummary from '@/components/Business/Scene/Common/ReportSummary.vue'
import UniversaFilter from '@/components/Business/UniversaFilter/index.vue'
import VoiceList from '@/components/Business/VoiceListPanel/index.vue'
import SwitchButton from '@/components/UI/SwitchButton/index.vue'
// import DataTrend from '@/components/Business/Scene/Common/DataTrend.vue'
import DataTrend from './DataTrend.vue'
import FWordCloud from '@/components/Charts/FWordCloud/index.vue'
import ViewMore from '@/components/Business/DrillDownDialog/components/ViewMore.vue'
import TopTable from './ScensTable/TopTable.vue'
import top_gd1 from '@/assets/images/top-gd1.png'
import top_gd2 from '@/assets/images/top-gd2.png'
import top_gd3 from '@/assets/images/top-gd3.png'
import top_gd4 from '@/assets/images/top-gd4.png'

import ZhAnalysis from './ZhAnalysis/index.vue'
import FourceScren from './FourceScren/index.vue'

import useMiddlewareStore from '@/store/modules/middleware'

import { useQueryListener } from '@/hooks/useQueryListener'
import useGeneralDrillDownStore from '@/store/modules/generalDrillDown'
import {
  getMainAccZhfxResult,
  getMainAccCjfzResult,
  getMainAccGzcjResult
} from '@/api/reportSummary/index'
import { getMainAccTreeData } from '@/api/common'
import { getDateDimension } from '@/utils/date'

import { DrillTabKey } from '@/components/Business/DrillDownDialog/constants.ts'

import { OriginalDataType, ORIGINAL_DATA_TYPE_OPTIONS } from '@/constants'

import {
  getAccBriefData,
  getAccTendData,
  getAcSeceData,
  getAcWordTopData,
  getAccUserIntentionOpinionTop,
  getAccSceneAnalysisChart
} from '@/api/mainAcc'

import { formatDate } from '@/utils'
import { useRoute, useRouter, onBeforeRouteLeave } from 'vue-router'

/**
 * 新车上市
 */
defineOptions({
  name: 'MainAccount'
})
const generalScenarioStore = useGeneralScenarioStore()
const route = useRoute()
const searchParams = ref<any>({})
const tagPath = ref<Array<{ code: string; name: string; level?: number }> | undefined>(undefined)
const middlewareStore = useMiddlewareStore()

// 初始化 ddStore
const ddStore = useGeneralDrillDownStore()

const queryStore = useQueryStore()

// 卡片数据
const productBriefData = ref<any>(null)

// 数据趋势变化数据
const dataTrendChangeData = ref<any>(null)

// 场景 TOP10
const focusSceneTopData = ref<any[]>([])

// 数据词云数据
const dataWordListData = ref<any[]>([])

const mainAccOptions = ref<any[]>([]) // 重点账号选项
const mainAccLoading = ref(true) // 重点账号选项加载中

// 场景分析 数据趋势变化 选择的柱子数据
const cjfxSelectBarData = ref<any>(null)

// ==================== 场景分析数据状态 ====================
const cjfxActiveTab = ref<string>('prod')
const cjfxData = ref<any>(null)
const cjfxLoading = ref(false)

// 观点TOP数据（复用领导总览组件）
const opinionTopData = ref<any>({
  complaintOpinions: [],
  consultOpinions: [],
  suggestionOpinions: [],
  praiseOpinions: []
})
const opinionLoading = ref(false)

// 表格组件引用
const complaintTableRef = ref<InstanceType<typeof TopTable>>()
const consultTableRef = ref<InstanceType<typeof TopTable>>()
const suggestionTableRef = ref<InstanceType<typeof TopTable>>()
const praiseTableRef = ref<InstanceType<typeof TopTable>>()

// 客户原声tab状态
const voiceActiveTab = ref<string>('result')

/**
 * 结果数据页签保留声音管理能力，继续支持高质量筛选、标记与纠错。
 */
const resultDataVoiceListProps = {
  showSortSelect: true,
  showHighQualityFilter: true,
  enableHighQualityActions: true,
  enableHighQualityInfo: true,
  enableVoiceManagementParams: true,
  enableErrorCorrection: true,
  showBatchAction: true,
  showCorpusCreateAction: true
}

/**
 * 原始数据页签恢复为只读查看模式，仅保留数据类型筛选与详情查看。
 */
const originalDataVoiceListProps = {
  showSortSelect: true,
  showDataStatus: false,
  showHighQualityFilter: false,
  enableHighQualityActions: false,
  enableHighQualityInfo: false,
  enableVoiceManagementParams: false,
  enableErrorCorrection: false,
  showBatchAction: false,
  showCorpusCreateAction: true,
  showTopicsInList: false,
  detailSource: 'list' as const,
  showTopicsInDetail: false,
  showBrandSeriesInDetail: false,
  showRelationEventsInDetail: false,
  showKeywordSearch: true
}

// VoiceList 组件的引用
const resultDataVoiceListRef = ref<InstanceType<typeof VoiceList> | null>(null)
const originalDataVoiceListRef = ref<InstanceType<typeof VoiceList> | null>(null)

// 获取当前活动的 VoiceList 组件引用
const getCurrentVoiceListRef = () => {
  return middlewareStore.originalDataType === OriginalDataType.ResultData
    ? resultDataVoiceListRef.value
    : originalDataVoiceListRef.value
}

/**
 * 重置声音管理中的高质量筛选，避免离开原声查询后把该私有条件带入其他页面。
 */
const resetHighQualityFilter = () => {
  queryStore.voiceManagementParams.highQuality = undefined
}

/**
 * 切换结果数据/原始数据时清空高质量筛选状态。
 * 避免列表请求参数已重置，但下拉框仍保留旧值，导致界面展示与实际查询条件不一致。
 */
watch(
  () => middlewareStore.originalDataType,
  (newType, oldType) => {
    if (!oldType || newType === oldType) return

    resetHighQualityFilter()
  }
)

/**
 * 清理原声查询页内缓存
 * 仅保留跨页面共享的时间字段，避免再次进入页面时带回页内共享/私有筛选条件。
 */
const clearOriginalSoundTabFilterCache = () => {
  queryStore.clearUniversaFilterCacheSearchParams('OriginalSoundQuery_Shared')
  queryStore.clearUniversaFilterCacheSearchParams('OriginalSoundQuery_ResultData')
  queryStore.clearUniversaFilterCacheSearchParams('OriginalSoundQuery_OriginalData')
}

// 注意：切换数据类型时不需要主动调用上报
// 因为组件卸载时（v-if 导致），TheDetails 组件的 onBeforeUnmount 会自动调用 recodeTime 上报
// 如果在这里也调用，会导致重复上报

// 路由离开时上报当前类型的浏览记录
onBeforeRouteLeave(async () => {
  resetHighQualityFilter()

  const currentRef = getCurrentVoiceListRef()
  if (currentRef) {
    try {
      await currentRef.submitBrowseRecord()
    } catch (error) {
      console.error('路由离开时上报浏览记录失败:', error)
    }
  }
})

// 页面卸载前上报当前类型的浏览记录（兜底）
onBeforeUnmount(async () => {
  const currentRef = getCurrentVoiceListRef()
  if (currentRef) {
    try {
      await currentRef.submitBrowseRecord()
    } catch (error) {
      console.error('页面卸载时上报浏览记录失败:', error)
    }
  }
})

// 在页面彻底卸载后清理 tab 私有缓存，确保只在页内 tab 切换时保留筛选状态。
onUnmounted(() => {
  resetHighQualityFilter()
  clearOriginalSoundTabFilterCache()
})

const handConverQuery = () => {
  // 处理参数
  const copyQsStore = {
    ...queryStore.currentQueryParams,
    ...queryStore.commonQueryParams,
    // 公共参数
    brandDataType: 3,
    tagType: 'CA'
  }
  delete copyQsStore.compCarSeriesObjList
  delete copyQsStore.newCarSeriesList
  delete copyQsStore.newCarSeriesObjList

  return copyQsStore
}

// ==================== 数据状态 ====================

// ==================== 接口调用方法 ====================

// 初始化重点账号树选项
const initMainAccTreeOptions = async () => {
  mainAccLoading.value = true
  try {
    const res = await getMainAccTreeData()
    mainAccOptions.value = res.result || []
    mainAccLoading.value = false
  } catch (error) {
    console.error('获取重点账号树选项失败:', error)
    mainAccOptions.value = []
    mainAccLoading.value = false
  }
}

/**
 * 获取卡片数据
 */
const fetchProductBrief = async () => {
  try {
    const response = await getAccBriefData({ ...searchParams.value }) //
    if (response.success) {
      productBriefData.value = response.result
    }
  } catch (error) {
    console.error('获取卡片数据失败:', error)
  }
}

/**
 * 获取数据趋势变化数据
 */
const fetchDataTrendChange = async () => {
  try {
    const response = await getAccTendData(searchParams.value) //
    if (response.success) {
      dataTrendChangeData.value = response.result
    }
  } catch (error) {
    console.error('获取数据趋势变化数据失败:', error)
  }
}

/**
 * 获取关注场景TOP数据
 */
const fetchFocusSceneTop = async () => {
  try {
    const response = await getAcSeceData(searchParams.value)
    if (response.success) {
      focusSceneTopData.value = response.result
    }
  } catch (error) {
    console.error('获取关注场景TOP数据失败:', error)
  }
}

/**
 * 获取场景分析数据
 */
const fetchCjfxData = async () => {
  const type = cjfxActiveTab.value
  const isProd = type === 'prod'
  cjfxLoading.value = true
  try {
    const queryParams: any = {
      ...searchParams.value,
      sceneType: isProd ? 'PRODUCT' : 'SERVICE',
      firstCodeTag: isProd ? ['voc-product-001'] : ['voc-service-001']
    }
    const response = await getAccSceneAnalysisChart(queryParams)
    if (response.success) {
      // 将接口返回的数据转换为DataTrend组件期望的格式
      // tag2Name字段作为x轴字段，需要转换为date字段
      const result = response.result
      let formattedData: any = null

      // 判断接口返回的数据格式
      if (Array.isArray(result)) {
        // 如果直接返回数组，包装成DataTrend组件期望的格式
        formattedData = {
          trend: result.map((item: any) => ({
            ...item,
            date: item.tag2Name || item.date
          }))
        }
      } else if (result && result.trend) {
        // 如果包含trend字段，直接转换
        formattedData = {
          ...result,
          trend: result.trend.map((item: any) => ({
            ...item,
            date: item.tag2Name || item.date
          }))
        }
      } else {
        // 其他情况直接使用
        formattedData = result
      }

      cjfxData.value = formattedData
    }
  } catch (error) {
    console.error('获取场景分析数据失败:', error)
  } finally {
    cjfxLoading.value = false
  }
}

/**
 * 获取词云数据
 */
const fetchWordData = async (params: any) => {
  try {
    const response = await getAcWordTopData(params)
    if (response.success) {
      dataWordListData.value = response.result || []
    }
  } catch (error) {
    console.error('获取词云数据失败:', error)
  }
}

// 处理情感切换事件
const handleSentimentChange = (payload: { sentiment: string | undefined }) => {
  // 重新获取词云数据
  const np = { ...searchParams.value, ...payload }

  fetchWordData(np)
}

/**
 * 获取观点TOP数据（复用领导总览组件格式）
 */
const fetchOpinionTopData = async () => {
  try {
    opinionLoading.value = true

    const intentions = ['抱怨', '咨询', '建议', '表扬']
    const keys = ['complaintOpinions', 'consultOpinions', 'suggestionOpinions', 'praiseOpinions']
    const sceneType = cjfxActiveTab.value === 'prod' ? 'PRODUCT' : 'SERVICE'
    const firstCodeTag = sceneType === 'PRODUCT' ? ['voc-product-001'] : ['voc-service-001']

    const secondCodeTag = cjfxSelectBarData.value ? [cjfxSelectBarData?.value?.tag2Code] : undefined

    const promises = intentions.map(intention =>
      getAccUserIntentionOpinionTop({
        ...searchParams.value,
        topic: '',
        intention,
        sceneType,
        secondCodeTag,
        firstCodeTag
      })
    )

    const responses = await Promise.all(promises)

    const result: any = {}
    responses.forEach((response, index) => {
      if (response.success && response.result) {
        // 将接口返回的字段映射为TopTable组件期望的格式
        result[keys[index]] = response.result.map((item: any) => ({
          ...item
          /**
          opinionName: item.opinion || item.opinionName,
          mentionCount: item.mentions || item.mentionCount,
          mentionRingRatio: item.mentionRingRatio || item.ringRatio || '--'
          */
        }))
      } else {
        result[keys[index]] = []
      }
    })

    opinionTopData.value = result
  } catch (error) {
    console.error('获取用户意图观点TOP数据失败:', error)
  } finally {
    opinionLoading.value = false
  }
}

/**
 * 处理观点TOP排序
 */
const handleOpinionSort = async (intention: string, prop: string, order: string | null) => {
  try {
    const intentionMap: Record<string, string> = {
      抱怨: 'complaintOpinions',
      咨询: 'consultOpinions',
      建议: 'suggestionOpinions',
      表扬: 'praiseOpinions'
    }

    const sceneType = cjfxActiveTab.value === 'prod' ? 'PRODUCT' : 'SERVICE'
    const firstCodeTag = sceneType === 'PRODUCT' ? ['voc-product-001'] : ['voc-service-001']

    const response = await getAccUserIntentionOpinionTop({
      ...searchParams.value,
      topic: '',
      intention,
      sceneType,
      firstCodeTag,
      sortField: prop,
      sortOrder: order
    })

    if (response.success) {
      const key = intentionMap[intention]
      opinionTopData.value[key] = response.result || []
    }
  } catch (error) {
    console.error('排序失败:', error)
  }
}

/**
 * 处理观点TOP行点击
 */
const handleOpinionClick = (opinionName: any, intention: string, data: any) => {
  console.log('点击', opinionName, intention, data)
  const sceneType = cjfxActiveTab.value === 'prod' ? 'PRODUCT' : 'SERVICE'
  const firstCodeTag = sceneType === 'PRODUCT' ? ['voc-product-001'] : ['voc-service-001']

  const secondCodeTag = cjfxSelectBarData.value ? [cjfxSelectBarData?.value?.tag2Code] : undefined

  // console.log('关注场景分析表格行点击数据:', { intention, data })
  // const isViewMore = data?.__viewMore || !data?.opinion

  ddStore.openDD(
    {
      ...searchParams.value,
      intention,
      topic: data.opinion,
      sceneType,
      secondCodeTag,
      firstCodeTag
    },
    { subTitle: data.opinion },
    [
      { text: intention, value: { intention } },
      { text: data.opinion, value: { topic: data.opinion } }
    ]
  )
}

/**
 * 处理观点TOP查看更多
 */
const handleOpinionViewMore = (intention: string) => {
  const sceneType = cjfxActiveTab.value === 'prod' ? 'PRODUCT' : 'SERVICE'
  const firstCodeTag = sceneType === 'PRODUCT' ? ['voc-product-001'] : ['voc-service-001']

  const secondCodeTag = cjfxSelectBarData.value ? [cjfxSelectBarData?.value?.tag2Code] : undefined

  ddStore.openDD(
    {
      ...searchParams.value,
      topic: '',
      intention,
      sceneType,
      secondCodeTag,
      firstCodeTag
    },
    {
      subTitle: `${intention || ''}观点TOP`,
      activeTab: DrillTabKey.VIEWPOINT
    },
    [],
    {
      mergeCommonQueryParams: false
    }
  )
}

// ==================== 事件处理 ====================

/**
 * 处理场景分析Tab切换
 */
const handleCjfxTabChange = () => {
  cjfxSelectBarData.value = null // 清空选择的柱子的数据
  fetchCjfxData()
  fetchOpinionTopData()
}

/**
 * 处理卡片点击事件
 */
const handleCardChange = (data: any) => {
  console.log('卡片点击数据:', data)
  // 在这里处理点击数据，比如跳转到详情页或打开弹窗
  ddStore.openDD({ ...searchParams.value }, { subTitle: '' })
}

/**
 * 处理图表点击事件
 */
const handleChartClick = (data: any) => {
  console.log('趋势图表点击数据:', data)
  // 重新处理开始时间 结束时间
  // 如果是月份
  const date = data?.data?.date
  const dayType = getDateDimension(date)
  let startDate = searchParams.value.startDate
  let endDate = searchParams.value.endDate
  if (dayType === 'month') {
    // 转换成这个月第一天
    startDate = date + '-01'
    // 转换成这个月最后一天
    const endDateFirst = new Date(startDate)
    const endYear = endDateFirst.getFullYear()
    const endMonth = endDateFirst.getMonth() + 1
    // 返回本月最后一天
    const lastDay = new Date(endYear, endMonth, 0).getDate()
    endDate = `${date}-${String(lastDay).padStart(2, '0')}`
  }

  // 在这里处理点击数据，比如跳转到详情页或打开弹窗
  ddStore.openDD(
    {
      ...searchParams.value,
      startDate,
      endDate
    },
    {
      subTitle: data.data.date
    }
  )
}

// 场景分析趋势图点击联动效果处理
const cjfzChartClick = (data: any) => {
  console.log('场景分析-趋势图-点击柱子加载效果处理', data)
  const preName = cjfxSelectBarData.value?.name
  const curName = data?.data?.name
  if (preName === curName) {
    // 说明是点击的本数据 取消
    cjfxSelectBarData.value = null
  } else {
    cjfxSelectBarData.value = data?.data
  }
}

watch(
  () => cjfxSelectBarData.value,
  (newVal, oldVal) => {
    console.log('ddddd', newVal, oldVal)

    if (!newVal && !oldVal) {
      // 初始化的时候不需要重复加载
      return
    }
    // 请求数据
    fetchOpinionTopData()
  },
  { deep: true }
)

/**
 * 处理关注场景TOP排序变化
 * @param sortData 排序数据对象
 */
const handleSceneTopSort = async ({ prop, order }: { prop: string; order: string }) => {
  try {
    const response = await getAcSeceData({
      ...searchParams.value,
      sortField: prop,
      sortOrder: order
    })

    if (response.success) {
      focusSceneTopData.value = response.result
    }
  } catch (error) {
    console.error('获取关注场景TOP排序数据失败:', error)
  }
}

/**
 * 处理场景TOP行点击事件
 */
const handleSceneRowClick = (data: any) => {
  console.log('场景行点击数据:', data)
  // 在这里处理点击数据，比如跳转到详情页或打开弹窗
  ddStore.openDD(
    {
      ...searchParams.value,
      tag4Code: data.tag4Code
    },
    {
      subTitle: data.scene,
      drillScene: DrillTabKey.SCENARIO
    },
    [{ text: data.scene, value: { tag4Code: data.tag4Code } }]
  )
}

/**
 * 处理关注场景TOP“查看更多”点击事件
 */
const handleSceneViewMore = () => {
  console.log('关注场景TOP查看更多点击')
  ddStore.openDD(
    { ...searchParams.value, searchLabelLevel: 4 },
    {
      subTitle: '关注场景TOP',
      activeTab: DrillTabKey.INDICATOR
    }
  )
}

/**
 * 词云TOP50“查看更多”点击事件
 */
const viewMoreWord = (data: any) => {
  console.log('点击更多', data)
  if (data?.data?.__viewMore) {
    // 查看更多
    ddStore.openDD(
      { ...searchParams.value },
      {
        // subTitle: '关注场景TOP',
        activeTab: DrillTabKey.VIEWPOINT
      }
    )
  } else {
    //
    const word = data?.data?.data || {}
    ddStore.openDD(
      {
        ...searchParams.value,
        topic: word.name,
        sentimentList: [word.sentiment]
      },
      { subTitle: word.name },
      [{ text: word.name, value: { topic: word.name } }]
    )
  }
}

// ==================== 生命周期 ====================

const refreshAllData = () => {
  // 初始化加载数据（车系条件数据只在组件挂载时加载一次）
  // 处理切换页面回来执行查询接口异常问题
  if (searchParams.value.startDate && searchParams.value.endDate) {
    fetchProductBrief()
    fetchDataTrendChange()
    fetchFocusSceneTop()
    fetchCjfxData()
    fetchWordData(searchParams.value)
    fetchOpinionTopData()
  }
}

// 使用查询监听 hooks
// const { queryStore } = useQueryListener(refreshAllData)

// ComprehensiveAnalysis 组件引用

onBeforeMount(() => {
  // 车系条件数据只在组件挂载时加载一次
  initMainAccTreeOptions()
})

// 组件挂载时加载数据
onMounted(() => {
  // 设置页面默认筛选条件（加载角色配置的默认值）
  queryStore.setPageDefaultFilter(route.name as string)
  // 初始化搜索参数
  searchParams.value = handConverQuery()
  refreshAllData()
})

const handleSearch = (
  _formData: any,
  _tagPath?: Array<{ code: string; name: string; level?: number }>
) => {
  // console.log('查询条件', _formData, queryStore)
  const copyFd = { ..._formData }
  const paramsCopy = handConverQuery()
  searchParams.value = { ...paramsCopy, ...copyFd }

  tagPath.value = _tagPath
  refreshAllData()
}
</script>

<template>
  <FAnalyseWrap v-model="generalScenarioStore.visible">
    <template #header>
      <SCHeader title="重点账号" subtitle="智行汽车集团"></SCHeader>
    </template>
    <UniversaFilter
      v-if="!mainAccLoading"
      :mainAccList="mainAccOptions"
      :routeName="`${route.name as string}`"
      @search="handleSearch"
    ></UniversaFilter>

    <FCard title="综合分析" tooltip="综合分析" :height="'auto'" class="mt-24">
      <ReportSummary
        :api-function="getMainAccZhfxResult"
        :query-params="searchParams"
      ></ReportSummary>
      <ZhAnalysis
        ref="comprehensiveAnalysisRef"
        :product-brief-data="productBriefData"
        :data-trend-change-data="dataTrendChangeData"
        @cardChange="handleCardChange"
        @chart-click="handleChartClick"
      ></ZhAnalysis>
    </FCard>

    <FCard title="关注场景" tooltip="关注场景" :height="'auto'" class="mt-24">
      <ReportSummary
        :api-function="getMainAccGzcjResult"
        :query-params="searchParams"
      ></ReportSummary>
      <FourceScren
        :focus-scene-top-data="focusSceneTopData"
        :opinionTopVos="dataWordListData"
        @scene-top-sort="handleSceneTopSort"
        @scene-row-click="handleSceneRowClick"
        @scene-view-more="handleSceneViewMore"
        @view-more-word="viewMoreWord"
        @sentiment-change="handleSentimentChange"
      >
      </FourceScren>
    </FCard>

    <FCard title="场景分析" tooltip="场景分析" :height="'auto'" class="mt-24">
      <ReportSummary
        :api-function="getMainAccCjfzResult"
        :query-params="searchParams"
      ></ReportSummary>
      <!-- 产品/服务Tab切换 -->
      <div class="mt-24">
        <SwitchButton
          v-model="cjfxActiveTab"
          :options="[
            { value: 'prod', label: '产品场景分析' },
            { value: 'server', label: '服务场景分析' }
          ]"
          @change="handleCjfxTabChange"
        ></SwitchButton>
      </div>
      <div v-loading="cjfxLoading" class="mt-24">
        <DataTrend
          :data-trend-change-data="cjfxData"
          :is-show-title="true"
          :is-show-legend="true"
          :is-borderless="false"
          :height="'380px'"
          title-text="场景分析"
          @chart-click="cjfzChartClick"
        ></DataTrend>
      </div>
      <!-- 四组观点卡片 -->
      <div class="mt-24 top-container" v-loading="opinionLoading">
        <FCard
          :title="'抱怨观点TOP'"
          :height="'340px'"
          :isShowMore="true"
          class="f-card-border"
          @handleMore="() => handleOpinionViewMore('抱怨')"
        >
          <template #leftExtra>
            <img :src="top_gd1" alt="" class="topImg" />
          </template>
          <template #more>
            <ViewMore />
          </template>
          <TopTable
            ref="complaintTableRef"
            :data="opinionTopData.complaintOpinions"
            intention="抱怨"
            @sort-change="handleOpinionSort"
            @row-click="handleOpinionClick"
          ></TopTable>
        </FCard>
        <FCard
          :title="'咨询观点TOP'"
          :height="'340px'"
          :isShowMore="true"
          class="f-card-border"
          @handleMore="() => handleOpinionViewMore('咨询')"
        >
          <template #leftExtra>
            <img :src="top_gd2" alt="" class="topImg" />
          </template>
          <template #more>
            <ViewMore />
          </template>
          <TopTable
            ref="consultTableRef"
            :data="opinionTopData.consultOpinions"
            intention="咨询"
            @sort-change="handleOpinionSort"
            @row-click="handleOpinionClick"
          ></TopTable>
        </FCard>
        <FCard
          :title="'建议观点TOP'"
          :height="'340px'"
          :isShowMore="true"
          class="f-card-border"
          @handleMore="() => handleOpinionViewMore('建议')"
        >
          <template #leftExtra>
            <img :src="top_gd3" alt="" class="topImg" />
          </template>
          <template #more>
            <ViewMore />
          </template>
          <TopTable
            ref="suggestionTableRef"
            :data="opinionTopData.suggestionOpinions"
            intention="建议"
            @sort-change="handleOpinionSort"
            @row-click="handleOpinionClick"
          ></TopTable>
        </FCard>
        <FCard
          :title="'表扬观点TOP'"
          :height="'340px'"
          :isShowMore="true"
          class="f-card-border"
          @handleMore="() => handleOpinionViewMore('表扬')"
        >
          <template #leftExtra>
            <img :src="top_gd4" alt="" class="topImg" />
          </template>
          <template #more>
            <ViewMore />
          </template>
          <TopTable
            ref="praiseTableRef"
            :data="opinionTopData.praiseOpinions"
            intention="表扬"
            @sort-change="handleOpinionSort"
            @row-click="handleOpinionClick"
          ></TopTable>
        </FCard>
      </div>
    </FCard>

    <!-- 客户原声 -->
    <FCard title="原声查询" tooltip="原声查询" :height="'auto'" class="mt-24">
      <!--
      <template #leftExtra>
        <SwitchButton
          v-model="middlewareStore.originalDataType"
          :options="ORIGINAL_DATA_TYPE_OPTIONS"
          class="ml-16"
        ></SwitchButton>
      </template>
      -->
      <div class="content-voice-wrapper">
        <!-- 结果数据 -->
        <!-- v-if="middlewareStore.originalDataType === OriginalDataType.ResultData" -->
        <VoiceList
          ref="resultDataVoiceListRef"
          key="ResultDataList"
          class="el-card"
          :queryParams="searchParams"
          v-bind="resultDataVoiceListProps"
        />
        <!-- 原始数据 -->
        <!--
        <VoiceList
          v-if="middlewareStore.originalDataType === OriginalDataType.OriginalData"
          ref="originalDataVoiceListRef"
          key="OriginalDataList"
          class="el-card"
          :queryParams="searchParams"
          v-bind="originalDataVoiceListProps"
          list-api-url="/report/vocLeadership/getRawData"
        />
        -->
      </div>
    </FCard>
  </FAnalyseWrap>
</template>

<style lang="scss" scoped>
.top-container {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;

  :deep(.f-card) {
    height: 335px !important;
    background: #fff;
    box-shadow: 0px 1px 2px 0px rgba(10, 13, 18, 0.05) !important;
    border-radius: 12px 12px 12px 12px !important;
    border: 1px solid #ebedf0;

    .fc-header {
      padding: 16px 16px 0;
    }
    .fc-body {
      padding: 16px;
    }

    .fch-left {
      position: relative;
      .text-h2 {
        margin-left: 28px;
        font-size: 16px !important;
      }
      .topImg {
        position: absolute;
        left: 0;
        top: 6px;
        width: 20px;
      }
    }

    .text-h3 {
      line-height: 32px;
      font-size: 16px !important;
    }
  }
}

.wordcloud-container {
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.no-data {
  color: #999;
  font-size: 14px;
}

.content-voice-wrapper {
  width: 100%;
  height: 950px;
}

.content-voice-wrapper :deep(.voice-list) {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.content-voice-wrapper :deep(.voice-list__container) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.content-voice-wrapper :deep(.theWrap) {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.content-voice-wrapper :deep(.theList) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.content-voice-wrapper :deep(.voice-list__items) {
  flex: 1;
  overflow-y: auto;
}
</style>
