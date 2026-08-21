<script setup lang="ts">
import HPage from '@h5/components/UI/HPage'
import HNavBar from '@h5/components/UI/HNavBar'
import { onMounted, reactive, computed, watch, onUnmounted } from 'vue'
import type { BrandItem } from '@views/H5/api/brand/types.d'
import BrandList from '@h5/views/home/components/BrandList/index.vue'
import HDateFilter, { type DateOption } from '@h5/views/home/components/HDateFilter'
import NegativeRateCard from '@h5/views/home/components/NegativeRateCard/index.vue'
import TotalVoice from '@h5/views/home/components/TotalVoice/index.vue'
import HEcharts from '@h5/components/UI/HEcharts/index.vue'
import HCard from '@h5/components/UI/HCard/index.vue'
import HSwitchButton from '@h5/components/UI/HSwitchButton'
import BrandTrendChange from '@h5/views/home/components/BrandTrendChange/index.vue'
import type { TrendItem } from '@h5/views/home/components/BrandTrendChange/types'
import EvaluationList from '@h5/views/home/components/EvaluationList/index.vue'
import HVoiceList from '@h5/components/HVoiceList/index.vue'
import BrandComparison from '@h5/components/BrandComparison/index.vue'
import { useRouter } from 'vue-router'
import emojiSatisfiedPng from '@/assets/images/system/emoji-satisfied.png'
import emojiFrownPng from '@/assets/images/system/emoji-frown.png'
import {
  getBrandTrendComparison,
  getDataBrief,
  getIndustryBrandComparison,
  getUserDynamicEvaluation,
  getFocusSceneAnalysis,
  getFocusSceneAnalysisTop,
  type H5VocBaseRequest
} from '@h5/api/home'
import type { NegativeRateData } from '@h5/views/home/components/NegativeRateCard/types'
import type { EChartsOption } from 'echarts'
import { usePermissionsStore } from '@/views/H5/store/permissions'
import { formatAxisLabel, fmtNum, fmtPer, fmtFix } from '@/utils'
import flagPng from '@/assets/h5/flag.png'
import { useH5AppStore } from '../../store/h5App'

// 关注场景分析数据类型
interface FocusSceneItem {
  /** 标签名称 */
  tagName: string
  /** 标签编码 */
  tagCode: string
  /** 负面率 */
  negativeRate: number
  /** 负面率环比 */
  negativeRateMom: number
  /** 负面率同比 */
  negativeRateYoy: number
  /** 提及量 */
  mention: number
  /** 提及量环比 */
  mentionMom: number
  /** 提及量同比 */
  mentionYoy: number
}

// 观点TOP数据类型
interface IntentionOpinionTopVo {
  /** 观点名 */
  opinion: string
  /** 情感 */
  sentiment: string
  /** 提及量 */
  mentions: number
  /** 提及量环比，% 两位小数 */
  mentionsMoM: number
  /** 提及量同比，% 两位小数 */
  mentionsYoY: number
  /** 事件 */
  remark: string[]
}

// 关注场景分析TOP数据类型
interface FocusSceneAnalysisTopData {
  /** 品牌图片URL，暂无 */
  brandImageUrl: string
  /** 品牌名称 */
  brandName: string
  /** 品牌编码 */
  brandCode: string
  /** 好评观点TOP5 */
  goodOpinions: IntentionOpinionTopVo[]
  /** 差评观点TOP5 */
  badOpinions: IntentionOpinionTopVo[]
}

const router = useRouter()
const userPermStore = usePermissionsStore()
const h5AppStore = useH5AppStore()

const hub = reactive({
  pageContentLoading: true, // 页面内容加载状态
  fetchBrandListFailed: false, // 品牌列表请求失败状态
  currentBrand: null as BrandItem | null,
  brandList: [] as BrandItem[], // 品牌列表
  currentDateFilter: {
    dateUnit: 0, //周月年
    dateTime: {} as DateOption // 本周、本月、本季、本年
  },
  sceneOptions: [] as any[], //关注场景分析
  sceneValue: '', //关注场景分析   服务/产品
  requestParams: {
    //请求参数
    // pageSize: 5,
    // pageNum: 1,
  } as H5VocBaseRequest,
  dataBrief: {
    name: '',
    negativeRate: 0,
    negativeRateMom: 0,
    mentionCount: 0,
    mentionCountMom: 0,
    achieveRate: 0,
    achieveRateTalk: ''
  } as NegativeRateData, //数据简报
  brandComparisonList: [], //品牌对比
  brandTrendList: [] as TrendItem[], //品牌趋势变化
  voiceList: [], //声音列表
  focusSceneAnalysisData: [] as FocusSceneItem[], //关注场景分析数据
  focusSceneAnalysisTopData: null as FocusSceneAnalysisTopData | null //关注场景分析TOP数据
})

//关注场景分析 - 动态计算图表配置
const lineBarChart = computed<EChartsOption>(() => {
  // 从API数据中提取图表数据
  const xAxisData = hub.focusSceneAnalysisData.map((item: FocusSceneItem) => item.tagName)

  // 根据当前场景类型决定显示负面率还是提及量（这里使用实际的数据字段）
  const negativeRateData = hub.focusSceneAnalysisData.map(
    (item: FocusSceneItem) => item.negativeRate
  )
  const mentionData = hub.focusSceneAnalysisData.map((item: FocusSceneItem) => item.mention)

  return {
    grid: {
      top: 30,
      left: 40,
      right: 40,
      bottom: 60
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'line',
        lineStyle: {
          color: '#EBEDF0',
          width: 2,
          type: 'solid'
        },
        label: {
          show: false
        }
      },
      showContent: true,
      enterable: true,
      confine: true,
      backgroundColor: 'rgba(255,255,255,.9)',
      position: 'top',
      borderColor: '#ebedf0',
      borderWidth: 1,
      extraCssText:
        'border-radius:8px;box-shadow:0 4px 12px rgba(0,0,0,0.08);padding:12px;pointer-events:none !important;' +
        'max-width:320px;' +
        'color:#5f6a7a;',
      formatter: (params: any) => {
        try {
          const idx = Array.isArray(params) && params.length > 0 ? params[0].dataIndex : -1
          if (idx < 0 || idx >= hub.focusSceneAnalysisData.length) return ''
          const it: any = hub.focusSceneAnalysisData[idx]
          const dateStr = it?.tagName || ''
          const rows = [
            {
              name: '负面率',
              value: `${fmtPer(it.negativeRate)}`,
              mom: it.negativeRateMom,
              yoy: it.negativeRateYoy,
              color: '#FAB007'
            },
            {
              name: '提及量',
              value: `${fmtNum(it.mention)}`,
              mom: it.mentionMom,
              yoy: it.mentionYoy,
              color: '#0AADFF'
            }
          ]
          const header = `<div style="display:flex;align-items:center;justify-content:space-between;height:20px;">
              <div style="font-size:12px;color:#1F2733;">${dateStr}</div>
              <div data-rca="scene-analysis" data-index="${idx}" style="font-size:12px;color:#929AA6;cursor:pointer;display:flex;align-items:center;pointer-events:auto;">根因分析 <span style="display:inline-block;width:8px;height:8px;border-right:2px solid #929AA6;border-top:2px solid #929AA6;transform:rotate(45deg) translateY(1px);margin-left:4px;"></span></div>
            </div>`
          const tableHeader = `<div style="display:flex;align-items:center;height:32px;background:#f5f7fa;border:1px solid #ebedf0;font-size:12px;color:#5F6A7A;font-weight: 400">
              <div style="flex:1;text-align:center;">名称</div>
              <div style="flex:1;text-align:center;">数值</div>
              <div style="flex:1;text-align:center;">环比</div>
              <div style="flex:1;text-align:center;">同比</div>
            </div>`
          const tableRows = rows
            .map(
              r => `<div style="display:flex;align-items:center;height:32px;font-size:12px;color:#6E7B91;font-weight: 400">
                <div style="flex:1;text-align:center;">
                  <span style="display:inline-block;width:8px;height:8px;border-radius:50%;margin-right:6px;background:${r.color}"></span>${r.name || ''}
                </div>
                <div style="flex:1;text-align:center;font-weight:600;">${r.value}</div>
                <div style="flex:1;text-align:center;">${fmtFix(r.mom)}</div>
                <div style="flex:1;text-align:center;">${fmtFix(r.yoy)}</div>
              </div>`
            )
            .join('')
          const marks = (it.remark || [])
            .map(
              (m: string) =>
                `<div style="height:32px;display:flex;align-items:center;background:#eaf3ff;border:1px solid #ebedf0;border-radius:4px;font-weight:500;font-size:12px;color:#5f6a7a;padding:0 10px;margin-top:8px;"><img style="width: 14px;height: 14px;margin-right: 8px" src="${flagPng}"/>${m || ''}</div>`
            )
            .join('')
          return `<div style="min-width:220px;pointer-events:none;">
              ${header}
              <div style="margin-top:8px;">${tableHeader}<div>${tableRows}</div></div>
              ${marks}
            </div>`
        } catch (e) {
          return ''
        }
      }
    },
    legend: {
      data: ['负面率', '提及量'],
      icon: 'circle',
      itemWidth: 8,
      itemHeight: 8,
      bottom: 5,
      left: 'center',
      textStyle: {
        color: '#6E7B91'
      }
    },
    xAxis: {
      type: 'category',
      data: xAxisData || ['暂无数据'],
      axisLine: {
        lineStyle: {
          color: '#F1F1F5'
        }
      },
      axisTick: {
        show: false
      },
      axisLabel: {
        color: '#92929D'
      }
    },
    yAxis: [
      {
        type: 'value',
        name: '',
        min: 0,
        max: 100,
        splitLine: {
          lineStyle: {
            color: '#F1F1F5'
          }
        },
        axisLabel: {
          formatter: '{value}%',
          color: '#92929D'
        }
      },
      {
        type: 'value',
        name: '',
        min: 0,
        max: Math.max(...mentionData) * 1.2 || 1000,
        splitLine: {
          show: false
        },
        axisLabel: {
          formatter: function (value: number) {
            return formatAxisLabel(value)
          },
          color: '#92929D'
        }
      }
    ],
    series: [
      {
        name: '负面率',
        type: 'line',
        data: negativeRateData,
        yAxisIndex: 0,
        itemStyle: {
          color: '#FAB007'
        }
      },
      {
        name: '提及量',
        type: 'bar',
        data: mentionData,
        yAxisIndex: 1,
        barMaxWidth: 16,
        itemStyle: {
          color: '#0AADFF',
          borderRadius: 2
        },
        emphasis: {
          disabled: false
        }
      }
    ]
  }
})

onMounted(() => {
  // 获取品牌列表
  fetchBrandList()
  document.addEventListener('click', delegateClickHandler, true)
})

onUnmounted(() => {
  document.removeEventListener('click', delegateClickHandler, true)
})

//请求接口
const fetchAllData = async () => {
  if (hub.currentBrand?.key) {
    //当前品牌
    hub.requestParams.brandCode = hub.currentBrand.key
    hub.requestParams.startDate = hub.currentDateFilter.dateTime?.startTime || ''
    hub.requestParams.endDate = hub.currentDateFilter.dateTime?.endTime || ''
    hub.pageContentLoading = true

    // 将每个请求包装为立即触发的Promise
    const promises = [
      fetchDataBrief(),
      fetchBrandComparisonList(),
      fetchBrandTrend(),
      fetchVoiceList(),
      fetchFocusSceneAnalysis(),
      fetchFocusSceneAnalysisTop()
    ]

    try {
      // 使用Promise.race等待第一个请求成功
      await Promise.race(promises)
      // 第一个请求成功后立即隐藏骨架屏
      hub.pageContentLoading = false

      // 等待所有请求完成，但不阻塞页面渲染（在后台执行）
      Promise.allSettled(promises)
        .then(results => {
          // 可以在这里处理所有请求的结果
          const failedRequests = results.filter(result => result.status === 'rejected')
          if (failedRequests.length > 0) {
            console.warn(`有 ${failedRequests.length} 个请求失败`)
          }
        })
        .catch(error => {
          console.error('Promise.allSettled处理失败:', error)
        })
    } catch (error) {
      console.error('fetchAllData error:', error)
      // 即使出错，也隐藏骨架屏显示内容
      hub.pageContentLoading = false
    }
  }
}

/**
 * 处理品牌点击事件
 */
const handleBrandClick = (brand: BrandItem) => {
  hub.currentBrand = brand
  fetchAllData()
}

/**
 * 处理分析点击事件
 */
const jumpPage = (type: string, data?: any) => {
  const requestParams = {
    ...hub.requestParams
  }
  switch (type) {
    case 'rootCauseAnalysis': //根因分析点击
      router.push({
        name: 'H5AnalysisAndVoice',
        query: {
          // 传递当前筛选参数
          ...requestParams
        }
      })
      break
    case 'taskDetail': //任务详情点击
      router.push({
        name: 'H5TaskDetail',
        query: {
          // 传递当前筛选参数
          ...requestParams
        }
      })
      break
    case 'brandRanking': //品牌列表点击
      router.push({
        name: 'H5BrandRanking',
        query: {
          // 传递当前筛选参数
          ...requestParams
        }
      })
      break
    case 'voiceList': //声音列表点击
      console.log('data---voiceList', data)

      router.push({
        name: 'H5AllVoiceList',
        query: {
          // 传递当前筛选参数
          ...requestParams,
          topic: data.opinion,
          intention: data.intention
        }
      })
      break
  }
}

// 点击根因分析
const handleRootCauseClick = (params: any = {}) => {
  router.push({
    name: 'H5AnalysisAndVoice',
    query: { ...hub.requestParams, ...params }
  })
}

//日期类型改变
const dateFilterChange = (type: number, option: any) => {
  hub.currentDateFilter.dateUnit = type
  hub.currentDateFilter.dateTime = option
  fetchAllData()
}

//场景类型改变
const handleSceneChange = (option: any) => {
  // 场景切换时重新获取数据
  if (hub.currentBrand?.key) {
    fetchFocusSceneAnalysis()
    fetchFocusSceneAnalysisTop()
  }
}

/**
 * 处理图表柱状图点击事件
 */
const handleChartClick = (params: any) => {
  if (params.componentType === 'series' && params.seriesType === 'bar') {
    // 获取点击的数据项
    const dataIndex = params.dataIndex

    // 从原始数据中获取详细信息
    if (
      hub.focusSceneAnalysisData &&
      hub.focusSceneAnalysisData.length > 0 &&
      hub.focusSceneAnalysisData[dataIndex]
    ) {
      const itemData = hub.focusSceneAnalysisData[dataIndex]
      fetchFocusSceneAnalysisTop({ tag2Code: itemData.tagCode })
    }
  }
}

// tooltip 内“根因分析”点击事件委托处理
const delegateClickHandler = (e: Event) => {
  try {
    const target = e.target as Element | null
    if (!target) return
    const clickable = target.closest(
      '[data-rca="scene-analysis"][data-index]'
    ) as HTMLElement | null
    if (!clickable) return
    const indexAttr = clickable.dataset.index ?? clickable.getAttribute('data-index') ?? ''
    const index = Number.parseInt(indexAttr, 10)
    if (!Number.isFinite(index) || index < 0) return

    const list = hub.focusSceneAnalysisData || []
    const item = list[index]
    if (!item) return

    handleRootCauseClick({ tag2Code: item.tagCode || '' })
  } catch (err) {
    console.warn('品牌趋势 tooltip 事件委托处理失败:', err)
  }
}

/**
 * 获取品牌列表、时间维度及场景配置
 * 依赖 H5 权限 Store 的统一初始化逻辑
 */
const fetchBrandList = async () => {
  try {
    hub.pageContentLoading = true
    hub.fetchBrandListFailed = false

    // 统一通过 Store 拉取并落库 H5 用户权限数据
    // initUserPermissions 内部已处理异常提示，这里只关心成功后的状态使用
    await userPermStore.initUserPermissions()

    // 品牌 / 时间维度 / 场景配置
    // 品牌列表直接使用 Store 中的派生数据
    hub.brandList = userPermStore.getBrandListForHome
    //服务/品牌 场景选项
    hub.sceneOptions = userPermStore.getSceneOptions
    //默认第一个
    if (hub.sceneOptions && hub.sceneOptions.length > 0) {
      hub.sceneValue = hub.sceneOptions[0].value || ''
    }
    if (hub.brandList && hub.brandList.length > 0) {
      const defCode = h5AppStore.getDefBrandCode
      const defBrand =
        typeof defCode === 'string' && defCode
          ? hub.brandList.find(el => el.key === defCode)
          : null
      hub.currentBrand = defBrand || hub.brandList[0]
      await fetchAllData() // 等待fetchAllData完成
    } else {
      // 品牌列表为空，隐藏骨架屏显示空状态
      hub.pageContentLoading = false
      hub.fetchBrandListFailed = true
    }
  } catch (error) {
    console.error('获取品牌列表失败:', error)
    hub.fetchBrandListFailed = true
    hub.pageContentLoading = false
  }
}

//获取数据简报与达成率
const fetchDataBrief = async () => {
  try {
    const requestParams = {
      ...hub.requestParams
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
    console.error('获取数据简报失败:', error)
  }
}

//品牌对比
const fetchBrandComparisonList = async () => {
  try {
    const requestParams = {
      ...hub.requestParams,
      pageSize: 5,
      pageNum: 1
    }
    const response = await getIndustryBrandComparison(requestParams)
    if (response.success && response.result) {
      hub.brandComparisonList = response.result.list || []
    } else {
      hub.brandComparisonList = []
    }
  } catch (error) {
    console.error('获取声音列表失败:', error)
  }
}

//获取品牌趋势变化
const fetchBrandTrend = async () => {
  try {
    const requestParams = {
      ...hub.requestParams
    }
    const response = await getBrandTrendComparison(requestParams)
    if (response.success && response.result) {
      // 确保result是数组  按时间排序
      hub.brandTrendList = (response.result || []).sort((a: any, b: any) => {
        return new Date(a.date).getTime() - new Date(b.date).getTime()
      })
    } else {
      // 失败时重置数据
      hub.brandTrendList = []
    }
  } catch (error) {
    console.error('获取品牌趋势变化失败:', error)
    // 错误时重置数据
    hub.brandTrendList = []
  }
}

//获取关注场景分析数据
const fetchFocusSceneAnalysis = async () => {
  try {
    const requestParams = {
      ...hub.requestParams,
      tag1Code: hub.sceneValue // 根据场景类型设置数据类型
    }
    const response = await getFocusSceneAnalysis(requestParams)
    if (response.success && response.result) {
      // 直接使用返回的数据数组
      hub.focusSceneAnalysisData = response.result || []
    } else {
      hub.focusSceneAnalysisData = []
    }
  } catch (error) {
    console.error('获取关注场景分析数据失败:', error)
    hub.focusSceneAnalysisData = []
  }
}

//获取关注场景分析TOP数据
const fetchFocusSceneAnalysisTop = async (params?: any) => {
  try {
    const requestParams = {
      ...hub.requestParams,
      ...params,
      tag1Code: hub.sceneValue // 根据场景类型设置数据类型
    }
    hub.focusSceneAnalysisTopData = null
    const response = await getFocusSceneAnalysisTop(requestParams)
    if (response.success && response.result) {
      hub.focusSceneAnalysisTopData = response.result
    } else {
      hub.focusSceneAnalysisTopData = null
    }
  } catch (error) {
    console.error('获取关注场景分析TOP数据失败:', error)
    hub.focusSceneAnalysisTopData = null
  }
}

//获取声音列表
const fetchVoiceList = async () => {
  try {
    const response = await getUserDynamicEvaluation(hub.requestParams)
    if (response.success && response.result) {
      // 转换API返回的数据为组件可用的VoiceListItem格式
      hub.voiceList = response.result.map((item: any) => ({
        ...item,
        id: item.newId,
        originalTexTScene: item.originalTextScene,
        custName: item.username,
        channel: item.channelName,
        topics: item.topics || [],
        dataCreateTime: item.evaluateTime
      }))
    } else {
      hub.voiceList = []
    }
  } catch (error) {
    console.error('获取声音列表失败:', error)
  }
}

/**
 * @description: 行业品牌对比显示逻辑
 * @param {*} computed
 * @return {*}
 */
const isHideBrandCard = computed(() => {
  const isAvg = hub.brandComparisonList.find((el: any) => el.isMarketAverage)
  // 只有行业均值的时候 直接隐藏卡片
  if (isAvg && hub.brandComparisonList?.length === 1) {
    return false
  }
  if (hub.brandComparisonList?.length) {
    return true
  }
  return false
})
</script>
<template>
  <HPage>
    <!-- 导航栏插槽 -->
    <template #nav-bar>
      <!-- <HNavBar
        :left-arrow="false"
        title="客户之声VOC"
        background-color="#1677FF"
        title-color="#ffffff"
      /> -->
      <template v-if="hub.brandList.length > 0">
        <BrandList
          :brandList="hub.brandList"
          :currentBrand="hub.currentBrand"
          @brand-click="handleBrandClick"
        />
        <HDateFilter :default-unit-info="h5AppStore.getDateUnitInfo" @change="dateFilterChange" />
      </template>
    </template>

    <!-- 页面内容插槽 -->
    <template #default>
      <div class="page-content m-12">
        <!-- 骨架屏 -->
        <template v-if="hub.pageContentLoading">
          <van-skeleton title :row="5" />
        </template>

        <!-- 品牌列表请求失败时显示空状态 -->
        <template v-else-if="hub.fetchBrandListFailed">
          <van-empty description="暂无数据" />
        </template>

        <!-- 正常内容 -->
        <template v-else>
          <!--        负面率-->
          <NegativeRateCard
            :name="hub.currentDateFilter.dateTime?.name"
            :data-brief="hub.dataBrief"
            @click="jumpPage('rootCauseAnalysis')"
          />
          <!--        总声数-->
          <TotalVoice :data-brief="hub.dataBrief" class="mt-12" @click="jumpPage('taskDetail')" />
          <!-- <HCard v-if="hub.brandComparisonList.length" title="行业品牌对比" class="mt-12"> -->
          <HCard v-if="isHideBrandCard" title="行业品牌对比" class="mt-12">
            <template #right>
              <div class="pl-20 pt-2 pb-2" @click="jumpPage('brandRanking')">
                <van-icon name="arrow" />
              </div>
            </template>
            <BrandComparison
              :data="hub.brandComparisonList"
              @item-click="(item: any) => handleRootCauseClick({ brandCode: item.code })"
              class="mt-10"
            />
          </HCard>
          <!-- 品牌趋势变化 -->
          <BrandTrendChange
            class="mt-12"
            :items="hub.brandTrendList"
            @root-cause-click="handleRootCauseClick"
          />
          <!-- v-if="hub.focusSceneAnalysisData.length" -->
          <HCard title="关注场景分析" class="mt-12">
            <template v-if="hub.sceneOptions && hub.sceneOptions.length" #right>
              <HSwitchButton
                :options="hub.sceneOptions"
                v-model="hub.sceneValue"
                @change="handleSceneChange"
              />
            </template>
            <div>
              <HEcharts
                :options="lineBarChart"
                width="100%"
                height="278px"
                @chart-click="handleChartClick"
              ></HEcharts>
              <div class="mt-10 mb-10 w-full splite-line"></div>

              <!-- 根据 API 数据显示好评和抱怨 TOP -->
              <!-- v-if="hub.focusSceneAnalysisTopData" -->
              <template v-if="true">
                <EvaluationList
                  title="抱怨TOP"
                  intention="抱怨"
                  :image="emojiFrownPng"
                  :evaluation-data="hub.focusSceneAnalysisTopData?.badOpinions || []"
                  @jumpVoice="(row: any) => jumpPage('voiceList', row)"
                />
                <EvaluationList
                  title="好评TOP"
                  intention="表扬"
                  :image="emojiSatisfiedPng"
                  background-color="#82e3c7"
                  class="mt-10"
                  :evaluation-data="hub.focusSceneAnalysisTopData?.goodOpinions || []"
                  @jumpVoice="(row: any) => jumpPage('voiceList', row)"
                />
              </template>
            </div>
          </HCard>
          <HCard v-if="hub.voiceList.length > 0" title="用户评价动态" class="mt-12">
            <HVoiceList
              :brandCode="hub.currentBrand?.key"
              :voiceList="hub.voiceList"
              :isLoadMore="true"
              @load-more="jumpPage('voiceList')"
            />
          </HCard>
        </template>
      </div>
    </template>
  </HPage>
</template>
<style scoped lang="scss">
.page-content {
  .fw-600 {
    font-weight: 600;
  }

  .text-center {
    text-align: center;
  }

  .splite-line {
    height: 1px;
    border-top: 1px solid #dfe2e8;
  }
}
</style>
