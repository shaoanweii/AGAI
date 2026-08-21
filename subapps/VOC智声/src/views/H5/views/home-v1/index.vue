<script setup lang="ts">
import HPage from '@h5/components/UI/HPage'
import HDateFilter, { type DateOption } from '@h5/views/home/components/HDateFilter'
import { onMounted, reactive, ref, nextTick, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import { usePermissionsStore, useShareStore } from '@h5/store'
import { useH5AppStore } from '@h5/store/h5App.ts'
import {
  getBrandTrendComparison,
  getDataBrief,
  getFocusSceneAnalysisTop,
  getUserDynamicEvaluation,
  type H5VocBaseRequest
} from '@h5/api/home'
import type { NegativeRateData } from '@h5/views/home/components/NegativeRateCard/types.d.ts'
import type { TrendItem } from '@h5/views/home/components/BrandTrendChange/types.d.ts'
import NegativeRateCard from '@h5/views/home/components/NegativeRateCard/index.vue'
import EvaluationListV1 from '@h5/views/home-v1/components/EvaluationList-v1/index.vue'
import noticePng from '@/assets/h5/notice.png'
import arrowRightPng from '@/assets/h5/arrow-right.png'
import BrandList from '@h5/components/BrandList/index.vue'
import BrandTrendChange from '@h5/views/home/components/BrandTrendChange/index.vue'
import HVoiceList from '@h5/components/HVoiceList'
import HCard from '@h5/components/UI/HCard/index.vue'
import { getSeriesRank } from '@h5/api/rootCauseAnalysis'
import { showDialog, showToast } from 'vant'
import HSwitchButton from '@h5/components/UI/HSwitchButton'
import HEcharts from '@h5/components/UI/HEcharts/index.vue'
import { computed, watch } from 'vue'
import { formatAxisLabel, fmtNum } from '@/utils'
import { useKeepAliveScroll } from '@h5/hooks/useKeepAliveScroll'
import ChannelScopeTabsV1 from '@h5/views/home-v1/components/ChannelScopeTabs-v1/index.vue'
import BrowseSummary from '@h5/components/BrowseSummary/index.vue'
import BackToAnchor from '@h5/components/BackToAnchor/index.vue'
import { useH5MenuVisitRecord } from '@h5/hooks/useH5MenuVisitRecord'
import { useRoute } from 'vue-router'
import { parseShareParamsFromQuery, convertDateParamsToDateOption } from '@h5/utils/shareParams'
import type { DateUnitInfo } from '@h5/views/home/components/HDateFilter/types'

// 观点TOP数据类型
interface IntentionOpinionTopVo {
  id?: any
  /** 观点名称 */
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

type OpinionSortField = 'mentions' | 'mentionsMoM'
type OpinionSortOrder = 'asc' | 'desc'

const OPINION_TOP_REQUEST_PAGE_NUM = 1
const OPINION_TOP_REQUEST_PAGE_SIZE = 100
const OPINION_TOP_DISPLAY_PAGE_SIZE = 20

const userPermStore = usePermissionsStore()
const h5AppStore = useH5AppStore()
const shareStore = useShareStore()
const router = useRouter()
const route = useRoute()

// 记忆并恢复 H5 滚动容器的滚动位置
useKeepAliveScroll()

// H5-首页：页面访问操作记录（返回/切换 tab 依赖 keep-alive 的 onActivated）
useH5MenuVisitRecord()

const channelScope = ref<string>('')

// 获取品牌列表
const brandList = computed(() => userPermStore.getBrandListForHome)

// 根据品牌编码获取品牌名称
const getBrandName = (brandCode: string | undefined): string => {
  if (!brandCode) return ''
  const brand = brandList.value.find((item: any) => item.key === brandCode)
  return brand?.value || ''
}

// 根据数据源编码获取数据源名称
const getDataSourceName = (channelCatagory: string | undefined): string => {
  if (!channelCatagory) return '全域'
  if (channelCatagory === '公域') return '公域'
  if (channelCatagory === '私域') return '私域'
  return '全域'
}

const hub = reactive<any>({
  pageContentLoading: true, // 页面内容加载状态
  fetchBrandListFailed: false, // 品牌列表请求失败状态
  requestParams: {
    //请求参数
    // pageSize: 5,
    // pageNum: 1,
    channelCatagory: '' //全域
  } as H5VocBaseRequest,
  currentDateFilter: {
    dateUnit: 0, //周月年
    dateTime: null as DateOption | null // 本周、本月、本季、本年
  },
  sceneOptions: [] as any[], //关注场景分析
  sceneValue: '', //关注场景分析   服务/产品
  dataBrief: {
    name: '',
    negativeRate: 0,
    negativeRateMom: 0,
    mentionCount: 0,
    mentionCountMom: 0,
    achieveRate: 0,
    achieveRateTalk: ''
  } as NegativeRateData, //数据简报
  brandTrendList: [] as TrendItem[], //品牌趋势变化
  opinionData: {
    //观点TOP请求参数
    loading: false,
    pageNum: 1,
    pageSize: OPINION_TOP_DISPLAY_PAGE_SIZE,
    sortField: undefined as OpinionSortField | undefined,
    sortOrder: undefined as OpinionSortOrder | undefined,
    total: 0, //总条数
    sourceList: [] as IntentionOpinionTopVo[], //接口返回的TOP200原始数据
    list: [] as IntentionOpinionTopVo[], //展示的数据
    currentOpinion: {} as any //当前点击的观点
  },
  seriesRankData: [] as any[], //观点TOP请求参数
  currentSeries: {} as any, //当前点击的车系
  voiceData: {
    loading: false,
    finished: false,
    pageNum: 1,
    pageSize: 10,
    total: 0, //总条数
    list: [] as any[] //展示的数据
  }
})

// 从权限 Store 映射品牌和场景配置
const sceneOptionsFromStore = computed(() => userPermStore.getSceneOptions)
const brandListFromStore = computed(() => userPermStore.getBrandListForHome)
const hasPermInited = computed(() => userPermStore.hasInited)

// 标记是否已应用分享参数（避免重复应用）
const shareParamsApplied = ref(false)

/**
 * 根据当前前端页码，从本地TOP100缓存中切出已展示的客户抱怨TOP数据。
 * 后端只负责按当前筛选/排序返回TOP100，“加载更多”仅扩展本地展示范围。
 */
const updateOpinionDisplayList = () => {
  const endIndex = hub.opinionData.pageNum * hub.opinionData.pageSize
  hub.opinionData.list = hub.opinionData.sourceList.slice(0, endIndex)
}

// 解析URL中的分享参数
const shareParamsFromQuery = computed(() => {
  return parseShareParamsFromQuery(route.query as Record<string, any>)
})

// 计算默认品牌编码
// 说明：
// - BrandList 组件会 watch defaultBrandCode 并在变化时触发一次 initCurrentBrand（可能会 emit brand-click）
// - 首页开启 keep-alive 后，虽然页面被切走但组件仍然存在；useRoute() 的 route 会随着当前路由变化而变化
// - 若这里直接依赖 route.query.brandCode，则在跳转到声音详情等页面时，defaultBrandCode 会跟随详情页 query 变化；
//   回到首页时又会回落到权限默认值，进而触发 BrandList 重新 emit，造成首页整页数据“被动刷新”
// 因此：仅在“首页路由”下才允许从 URL 分享参数初始化默认品牌，以该值为准，避免路由切换干扰默认值。
const defaultBrandCode = computed(() => {
  // 用户已在首页选中过品牌：保持稳定，避免路由切换导致回退到权限默认值

  // 仅在“首页路由”下才允许从 URL 分享参数初始化默认品牌
  if (route.name === 'H5Home' && shareParamsFromQuery.value?.brandCode) {
    // 验证品牌是否在品牌列表中
    const brandList = brandListFromStore.value
    const brandExists = brandList.some(
      (brand: any) => brand.key === shareParamsFromQuery.value?.brandCode
    )
    if (brandExists) {
      return shareParamsFromQuery.value.brandCode
    }
  }

  return h5AppStore.getDefBrandCode
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

  return h5AppStore.getDateUnitInfo
})

// 计算默认数据源（优先级：URL参数 > 默认值）
const defaultChannelCatagory = computed(() => {
  if (shareParamsFromQuery.value?.channelCatagory !== undefined) {
    return shareParamsFromQuery.value.channelCatagory
  }
  return ''
})

// 计算分享标题
const shareTitle = computed(() => {
  return '客户之声'
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

// 计算分享描述：品牌-时间筛选-数据源
const shareDesc = computed(() => {
  const brandName = getBrandName(hub.requestParams.brandCode)
  const timeFilter = formatTimeFilter(hub.currentDateFilter.dateTime)
  const dataSource = getDataSourceName(hub.requestParams.channelCatagory)

  const parts = [brandName, timeFilter, dataSource].filter(Boolean)
  return parts.join('-') || '客户之声'
})

// 同步场景选项到本地 hub，默认选中第一个场景
watch(
  sceneOptionsFromStore,
  list => {
    const options = list || []
    hub.sceneOptions = options
    if (options.length > 0 && !hub.sceneValue) {
      hub.sceneValue = options[0].value || ''
    }
  },
  { immediate: true }
)

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

        // 应用数据源（品牌和时间通过 BrandList 和 HDateFilter 组件的 props 自动应用）
        if (shareParamsFromQuery.value.channelCatagory !== undefined) {
          hub.requestParams.channelCatagory = shareParamsFromQuery.value.channelCatagory
          channelScope.value = shareParamsFromQuery.value.channelCatagory
        }

        // 清除分享参数（避免影响后续操作）
        shareStore.clearShareParams()
      }
    }
  },
  { immediate: true }
)

// 车系分布图表配置
const seriesRankChart = computed(() => {
  const list = Array.isArray(hub.seriesRankData) ? hub.seriesRankData : []
  const names = list.map((d: any) => String(d.name || '')) || []
  const values = list.map((d: any) => d.mentions) || []
  // 选中下标：依据当前选中的车系 code 匹配
  const selectedIndex = list.findIndex((d: any) => d?.code && d.code === hub.currentSeries?.code)

  return {
    color: ['#60B8EB'],
    grid: { top: 30, left: 15, right: 0, bottom: 0, containLabel: true },
    tooltip: {
      show: false,
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params: any[]) => {
        const p = params[0]
        return `${p.axisValue}<br/>提及量：${fmtNum(p.value)}`
      }
    },
    xAxis: {
      type: 'category' as const,
      data: names,
      axisTick: { show: false },
      axisLine: {
        lineStyle: { color: '#F1F1F5' }
      },
      axisLabel: {
        show: true,
        interval: 0,
        width: 100,
        overflow: 'break',
        color: '#5F6A7A',
        rotate: names.length >= 5 ? 60 : 0,
        formatter: function (value: string) {
          // // 超过6个字符显示省略号
          // if (value && value.length > 6) {
          //   return value.substring(0, 6) + '...'
          // }
          return value
        }
      }
    },
    yAxis: {
      type: 'value' as const,
      // minInterval: 1,
      splitLine: {
        show: true,
        lineStyle: { color: '#F1F1F5' }
      },
      axisLabel: {
        formatter: function (value: number) {
          return formatAxisLabel(value)
        },
        color: '#92929D'
      }
    },
    series: [
      {
        name: '提及量',
        type: 'bar' as const,
        data: values,
        barMaxWidth: 16,
        // 根据选中状态动态设置柱体颜色（选中高亮，未选中默认）
        itemStyle: {
          color: (p: any) => (p.dataIndex === selectedIndex ? '#1677FF' : '#60B8EB')
        },
        label: {
          show: true,
          position: 'top',
          color: '#666666',
          fontSize: 10,
          formatter: (p: any) => fmtNum(values[p.dataIndex] || 0)
        }
      }
    ]
  } as any
})

// 页面初始化提示信息
// const showTip = () => {
//   const today = new Date().toDateString()
//   const lastShownDate = localStorage.getItem('tip_shown_date')

//   if (lastShownDate !== today) {
//     showDialog({
//       title: '温馨提示',
//       message:
//         'AI赋能，初步实现客户声音”获取-识别-应用“全链路自动化，持续优化大模型能力，精准度和丰富度正持续提升中。'
//     }).then(() => {
//       localStorage.setItem('tip_shown_date', today)
//     })
//   }
// }

onMounted(() => {
  // showTip()
})

// 监听分享标题和描述变化，更新到store
watch(
  [shareTitle, shareDesc],
  ([title, desc]) => {
    shareStore.setShareInfo(title, desc)
  },
  { immediate: true }
)

//默认首页加载
const isFristLoad = ref(true)
onActivated(() => {
  //加载浏览时长
  if (!isFristLoad.value) {
    fetchDataBrief()
  } else {
    isFristLoad.value = false
  }
})

// “客户抱怨TOP”区域锚点
const opinionAnchor = ref<HTMLElement | null>(null)
const seriesAnchor = ref<HTMLElement | null>(null)
// 观点列表组件实例，用于定位内部行元素
const evalListRef = ref<any>(null)

/**
 * 处理品牌点击事件
 * - 由 BrandList-v1 组件负责品牌列表与当前品牌的维护
 * - 这里仅接收选中的品牌，更新请求参数并触发数据刷新
 */
const handleBrandClick = (brand: any) => {
  if (!brand?.key) return
  hub.requestParams.brandCode = brand.key
  // 只有在时间和品牌都准备好时才触发数据请求
  // 避免在组件初始化时参数不完整就触发请求，导致请求被取消
  if (hub.currentDateFilter.dateTime && hub.requestParams.brandCode) {
    fetchAllData()
  }
}

//日期类型改变
const dateFilterChange = (type: number, option: any) => {
  if (!option) return
  hub.currentDateFilter.dateUnit = type
  hub.currentDateFilter.dateTime = option
  // 同步更新请求参数中的时间
  hub.requestParams.startDate = option.startTime || ''
  hub.requestParams.endDate = option.endTime || ''
  // 只有在时间和品牌都准备好时才触发数据请求
  // 避免在组件初始化时参数不完整就触发请求，导致请求被取消
  if (hub.requestParams.brandCode && hub.currentDateFilter.dateTime) {
    fetchAllData()
  }
}

//场景类型改变 获取客户抱怨TOP、车系公布、客户原声

const handleChannelScopeChange = (option: any) => {
  hub.requestParams.channelCatagory = option.key || ''
  fetchAllData()
}
const handleSceneChange = (option: any) => {
  //重置评价、声音列表的请求pageNum
  hub.opinionData.pageNum = 1
  hub.voiceData.pageNum = 1
  hub.voiceData.finished = false
  fetchFocusSceneAnalysisTop()
  fetchSeriesRank()
  fetchVoiceList()
}

//观点点击
const handleOpinionItemClick = (item: IntentionOpinionTopVo) => {
  hub.voiceData.pageNum = 1
  hub.voiceData.finished = false
  hub.opinionData.currentOpinion = item
  fetchSeriesRank()
  fetchVoiceList()
  // 点击后，将“该行”滚动到容器顶部
  nextTick(() => {
    try {
      const container = document.querySelector('.f-page__content') as HTMLElement | null
      if (!container || !seriesAnchor.value) return
      const containerTop = container.getBoundingClientRect().top
      const targetTop = seriesAnchor.value.getBoundingClientRect().top
      const to = container.scrollTop + (targetTop - containerTop)
      container.scrollTo({ top: to, behavior: 'smooth' })
    } catch (e) {
      // 忽略 DOM 捕获异常
    }
  })
}

const handleSeriesRankChartClick = (params: any) => {
  // 防御性获取 dataIndex
  const dataIndex = params?.dataIndex
  if (dataIndex === undefined || dataIndex === null || dataIndex < 0) return

  const clicked = hub.seriesRankData[dataIndex] || {}
  const isSame = clicked?.code && hub.currentSeries?.code && clicked.code === hub.currentSeries.code

  // 重置分页
  hub.voiceData.pageNum = 1
  hub.voiceData.finished = false

  if (isSame) {
    // 再次点击已选中项：取消选中，传空对象
    hub.currentSeries = {}
  } else {
    // 选中新项
    hub.currentSeries = clicked
  }

  // 刷新声音列表
  fetchVoiceList()
}

/**点击声音列表*/
const handleVoiceItemClick = (item: any) => {
  const id = item?.id || item?.newId || ''
  const originalId = item?.originalId || ''
  router.push({
    name: 'H5VoiceDetail',
    query: {
      id,
      originalId,
      brandCode: hub.requestParams.brandCode || '',
      intent: item?.intent || '',
      channelName: item?.channelName || ''
    }
  })
}

/**
 * 展示客户原声统计口径说明。
 */
const handleVoiceTipClick = () => {
  showDialog({
    title: '客户原声',
    message: '遵从各品牌主体数据安全管理规范约束，品牌原声私域数据需对应权限方可查阅。',
    theme: 'round-button',
    confirmButtonColor: '#165DFF',
    confirmButtonText: '我知道了'
  })
}

const handleOpinionLoadMore = () => {
  if (hub.opinionData.loading || hub.opinionData.list.length >= hub.opinionData.sourceList.length)
    return

  hub.opinionData.pageNum++
  updateOpinionDisplayList()
}

/**
 * 处理“客户抱怨TOP”表头排序变化。
 * 说明：
 * - 仅在存在排序状态时向后端透传 sortField/sortOrder；
 * - 切换排序后重置分页、当前观点与联动区域，保证列表顺序和下游明细一致。
 */
const handleOpinionSortChange = (payload: {
  sortField?: OpinionSortField
  sortOrder?: OpinionSortOrder
}) => {
  hub.opinionData.sortField = payload.sortField
  hub.opinionData.sortOrder = payload.sortOrder
  hub.opinionData.pageNum = 1
  hub.voiceData.pageNum = 1
  hub.voiceData.finished = false

  fetchFocusSceneAnalysisTop()
  fetchSeriesRank()
  fetchVoiceList()
}

//加载更多
const handleVoiceLoadMore = () => {
  // van-list 可能在容器仍处于底部时连续触发 load，这里需要同时拦截“请求中”和“已结束”两种状态
  if (hub.voiceData.loading || hub.voiceData.finished) return

  hub.voiceData.pageNum++
  fetchVoiceList()
}

/**
 * 处理分析点击事件
 */
const jumpPage = (type: string) => {
  const requestParams = {
    ...hub.requestParams
  }
  switch (type) {
    case 'taskDetail': //任务详情点击
      router.push({
        name: 'H5TaskDetail',
        query: {
          // 传递当前筛选参数
          ...requestParams
        }
      })
      break
  }
}

/**
 * 获取品牌列表、时间维度及场景配置
 * 依赖 H5 权限 Store 的统一初始化逻辑
 */
//请求接口
const fetchAllData = async () => {
  if (hub.requestParams.brandCode && hub.currentDateFilter.dateTime) {
    //当前品牌
    hub.pageContentLoading = true
    hub.requestParams.startDate = hub.currentDateFilter.dateTime?.startTime || ''
    hub.requestParams.endDate = hub.currentDateFilter.dateTime?.endTime || ''
    //重置评价、声音列表的请求pageNum
    hub.opinionData.pageNum = 1
    hub.voiceData.pageNum = 1
    hub.voiceData.finished = false

    // 将每个请求包装为立即触发的Promise
    const promises = [
      fetchDataBrief(),
      fetchBrandTrend(),
      fetchFocusSceneAnalysisTop(),
      fetchSeriesRank(),
      fetchVoiceList()
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

//获取数据简报与达成率
const fetchDataBrief = async () => {
  try {
    const requestParams = {
      ...hub.requestParams
    }
    const response = await getDataBrief(requestParams, { cancelPrevious: true })
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

//获取品牌趋势变化
const fetchBrandTrend = async () => {
  try {
    const requestParams = {
      ...hub.requestParams
    }
    const response = await getBrandTrendComparison(requestParams, { cancelPrevious: true })
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

//获取抱怨TOP数据
const fetchFocusSceneAnalysisTop = async (params: any = {}) => {
  try {
    hub.opinionData.loading = true
    hub.opinionData.pageNum = 1
    hub.opinionData.sourceList = []
    hub.opinionData.list = []
    hub.opinionData.currentOpinion = {}
    const requestParams = {
      ...hub.requestParams,
      ...params,
      pageNum: OPINION_TOP_REQUEST_PAGE_NUM,
      pageSize: OPINION_TOP_REQUEST_PAGE_SIZE,
      tag1Code: hub.sceneValue, // 根据场景类型设置数据类型
      ...(hub.opinionData.sortField && hub.opinionData.sortOrder
        ? {
            sortField: hub.opinionData.sortField,
            sortOrder: hub.opinionData.sortOrder
          }
        : {})
    }
    const response = await getFocusSceneAnalysisTop(requestParams, { cancelPrevious: true })
    if (response.success && response.result) {
      // 当前业务只取TOP100，前端分页总数以本次接口实际返回条数为准。
      const list = (response.result.list || []).map((item: any, index: number) => {
        return {
          id: index + 1,
          ...item
        }
      })
      hub.opinionData.sourceList = list
      hub.opinionData.total = list.length
      updateOpinionDisplayList()
    } else {
      hub.opinionData.sourceList = []
      hub.opinionData.list = []
      hub.opinionData.total = 0
    }
  } catch (error) {
    console.error('获取关注场景分析TOP数据失败:', error)
    hub.opinionData.sourceList = []
    hub.opinionData.list = []
    hub.opinionData.total = 0
  } finally {
    hub.opinionData.loading = false
  }
}

// 获取车系排行数据
const fetchSeriesRank = async () => {
  try {
    hub.currentSeries = {}
    const res = await getSeriesRank(
      {
        ...hub.requestParams,
        tag1Code: hub.sceneValue, // 根据场景类型设置数据类型
        topic: hub.opinionData.currentOpinion.opinion || '',
        intention: '抱怨'
        // sentiment: hub.opinionData.currentOpinion.sentiment || '',
      },
      { cancelPrevious: true }
    )
    if (res.success) {
      hub.seriesRankData = res.result || []
      //按提及量从高到低排序
      hub.seriesRankData.sort((a: any, b: any) => {
        return b.mentions - a.mentions
      })
    } else {
      console.error('获取车系排行数据失败:', res.message)
      showToast(res.message || '')
    }
  } catch (error) {
    console.error('获取车系排行数据异常:', error)
  }
}

//获取声音列表
const fetchVoiceList = async () => {
  try {
    hub.voiceData.loading = true
    if (hub.voiceData.pageNum === 1) {
      hub.voiceData.list = []
      hub.voiceData.finished = false
    }
    const response = await getUserDynamicEvaluation(
      {
        ...hub.requestParams,
        pageNum: hub.voiceData.pageNum,
        pageSize: hub.voiceData.pageSize,
        tag1Code: hub.sceneValue, // 根据场景类型设置数据类型
        topic: hub.opinionData.currentOpinion.opinion || '',
        // sentiment: hub.opinionData.currentOpinion.sentiment || '',
        intention: '抱怨',
        carSeriesCode: hub.currentSeries?.code || '',
        checkPermission: true
      },
      { cancelPrevious: true }
    )
    if (response.success && response.result) {
      hub.voiceData.total = response.result.total || 0
      // 记录本次实际返回条数，优先按“累计数量/总数”和“本页不足 pageSize”双条件停止分页，
      // 避免后端 total 不准或返回空页时，van-list 因 finished 始终为 false 而无限触发 load。
      const list =
        response.result.list?.map((item: any) => ({
          ...item,
          id: item.newId,
          originalTexTScene: item.originalTextScene,
          custName: item.username,
          channel: item.channelName,
          topics: item.topics || [],
          dataCreateTime: item.evaluateTime,
          brand: item.brandName,
          carSeries: item.carSeriesName
        })) || []
      if (hub.voiceData.pageNum === 1) {
        hub.voiceData.list = list
      } else {
        hub.voiceData.list.push(...list)
      }

      hub.voiceData.finished =
        hub.voiceData.list.length >= hub.voiceData.total || list.length < hub.voiceData.pageSize
    } else {
      hub.voiceData.list = []
      hub.voiceData.total = 0
      hub.voiceData.finished = true
    }
  } catch (error) {
    hub.voiceData.list = []
    hub.voiceData.total = 0
    hub.voiceData.finished = true
    console.error('获取声音列表失败:', error)
  } finally {
    hub.voiceData.loading = false
  }
}
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
      <!--      浏览条数、时长-->
      <BrowseSummary
        :data-brief="hub.dataBrief"
        :request-params="hub.requestParams"
        :current-date-filter="hub.currentDateFilter"
        :channel-catagory="hub.requestParams.channelCatagory"
      />
      <template v-if="!hub.fetchBrandListFailed">
        <BrandList :default-brand-code="defaultBrandCode" @brand-click="handleBrandClick" />
        <div v-show="hub.requestParams.startDate" class="filter-layout">
          <div class="flex-y-center" style="justify-content: space-between">
            <HDateFilter :default-unit-info="defaultDateUnitInfo" @change="dateFilterChange" />
            <ChannelScopeTabs-v1 v-model="channelScope" @change="handleChannelScopeChange" />
          </div>
        </div>
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
          />
          <!-- 品牌趋势变化 -->
          <BrandTrendChange class="mt-12" :items="hub.brandTrendList" />
          <div class="opinion-container">
            <!-- 锚点：客户抱怨TOP起始位置，用于返回定位 -->
            <div ref="opinionAnchor" class="opinion-anchor"></div>
            <!--          负面评价-->
            <HCard title="客户抱怨TOP" class="mt-12">
              <template v-if="hub.sceneOptions && hub.sceneOptions.length" #right>
                <HSwitchButton
                  :options="hub.sceneOptions"
                  v-model="hub.sceneValue"
                  @change="handleSceneChange"
                />
              </template>
              <EvaluationList-v1
                ref="evalListRef"
                v-model="hub.opinionData.currentOpinion"
                :loading="hub.opinionData.loading"
                :is-show-more="hub.opinionData.list.length !== hub.opinionData.total"
                :evaluation-data="hub.opinionData.list || []"
                :sort-field="hub.opinionData.sortField"
                :sort-order="hub.opinionData.sortOrder"
                @item-click="handleOpinionItemClick"
                @load-more="handleOpinionLoadMore"
                @sort-change="handleOpinionSortChange"
              />
            </HCard>
            <div ref="seriesAnchor" class="opinion-anchor"></div>
            <HCard v-if="hub.seriesRankData.length > 0" title="车系分布" class="mt-12">
              <template #left
                ><div
                  v-if="hub.opinionData.currentOpinion.opinion"
                  class="van-ellipsis flex-1"
                  style="color: #1677ff"
                >
                  【{{ hub.opinionData.currentOpinion.opinion }}】
                </div></template
              >
              <HEcharts
                :options="seriesRankChart"
                width="100%"
                height="240px"
                @chart-click="handleSeriesRankChartClick"
              />
            </HCard>
            <HCard v-if="hub.voiceData.total > 0" title="客户原声" class="mt-12">
              <template #left>
                <van-icon
                  name="info-o"
                  class="voice-tip"
                  aria-label="客户原声说明"
                  @click.stop="handleVoiceTipClick"
                />
                <div class="van-ellipsis flex-1" style="color: #1677ff">
                  <span v-if="hub.opinionData.currentOpinion.opinion"
                    >【{{ hub.opinionData.currentOpinion.opinion }}】</span
                  >
                  <span v-if="hub.currentSeries.name">【{{ hub.currentSeries.name }}】</span>
                </div>
              </template>
              <HVoiceList
                :loading="hub.voiceData.loading"
                :voiceList="hub.voiceData.list"
                :isLoadMore="!hub.voiceData.finished"
                @load-more="handleVoiceLoadMore"
                @item-click="handleVoiceItemClick"
              />
            </HCard>
          </div>
          <!-- 回到客户抱怨TOP：长列表滚动后显示 -->
          <BackToAnchor v-if="hub.opinionData.total > 0" :target-el="opinionAnchor" />
        </template>
      </div>
    </template>
  </HPage>
</template>
<style lang="scss" scoped>
.filter-layout {
  background: #ffffff;
  box-shadow:
    0px 4px 4px 0px rgba(0, 0, 0, 0.02),
    0px 0px 4px 0px rgba(0, 0, 0, 0.02);
  border-radius: 0px 0px 12px 12px;
  border: 1px solid #ebedf0;
  padding: 12px;
}

/* 锚点占位元素 */
.opinion-anchor {
  height: 0;
}

.voice-tip {
  flex-shrink: 0;
  margin: 0 6px 0 4px;
  color: #1677ff;
  font-size: 12px;
  line-height: 20px;
  cursor: pointer;
}
</style>
