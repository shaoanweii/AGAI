<script setup lang="ts">
import { computed, ref, watch, h, onMounted } from 'vue'
import { getList, getTopicsByTagId } from '@/api/singlePointEvent'
import { useSingleEventStore } from '@/store/modules/singleEvent'
import type { Column, Placement } from 'element-plus'
import useMiddlewareStore from '@/store/modules/middleware'
import dayjs from 'dayjs'
import { useUserStore } from '@/store'
import { EventAnalyType } from '@/constants'

import DdCard from './card/DdCard.vue'
import DataTrend from './DataTrend.vue'
import ProportionOfPieChart from './ProportionOfPieChart.vue'
import SecendTable from './SecendTable.vue'
import UserEventTrend from './UserEventTrend.vue'
import DataThreeTrend from './DataThreeTrend.vue'
import ChanelTable from './ChanelTable.vue'
import WordTop from './WordTop.vue'

import { cleanEmptyParams } from '@/utils'

defineOptions({
  name: 'closedLoopEvaluation'
})

const fDatePickerRef = ref()

const times = ref<any>([])
const shortcutValue = ref<any>('近7天')
const isExpanded = ref(false)

const singleEventStore = useSingleEventStore()
const middlewareStore = useMiddlewareStore()
const userStore = useUserStore()

const formData = ref<any>({})
const queryTimeRange = computed(() => {
  return {
    startTime: times.value[0] ? `${times.value[0]} 00:00:00` : '',
    endTime: times.value[1] ? `${times.value[1]} 23:59:59` : ''
  }
})

// 离线演示基线，同时覆盖趋势、图表、排行和用户明细组件所需字段。
const mockData = Array.from({ length: 7 }, (_, index) => ({
  name: ['智能座舱', '售后响应', '空间体验', '产品质量', '充电服务', '交付体验', '品牌沟通'][index],
  channelName: ['车主社区', '社交媒体', '电商评价', '服务工单', '调研问卷', '热线回访', '门店反馈'][index],
  date: dayjs().subtract(6 - index, 'day').format('YYYY-MM-DD'),
  totals: 1260 + index * 135,
  posts: 420 + index * 38,
  comments: 840 + index * 97,
  neutralMentions: 210 + index * 16,
  mentions: 186 - index * 14,
  mentionsMoM: Number((8.6 - index * 0.7).toFixed(1)),
  mentionsYoY: Number((12.4 - index * 0.8).toFixed(1)),
  ywxys: 96 + index * 18,
  bhcls: 82 + index * 15,
  sjgbs: 68 + index * 13,
  value: 186 - index * 14,
  share: Number((24.8 - index * 1.9).toFixed(1)),
  warningEventNo: `VOC-EVENT-${String(index + 1).padStart(3, '0')}`,
  subjectCategoryName: ['客户体验中心', '产品质量中心', '售后服务中心'][index % 3],
  eventPriority: ['P0', 'P1', 'P2'][index % 3],
  title: ['演示管理员', '体验分析师', '事件负责人'][index % 3],
  content: '查看、分析、处理与导出',
  originalTextScene: dayjs().subtract(index, 'hour').format('YYYY-MM-DD HH:mm:ss'),
  topicText: 18 + index * 3,
  brandName: 126 + index * 11,
  carSeriesName: 38 + index * 5,
  authorName: `${42 + index * 6}分钟`
}))

const sjqsbhData = ref<any>(mockData) // 数据趋势变化的列表

const zdjjsjDdsjData = ref<any>(mockData) // 单点事件分析 - 重大紧急事件
const ztflqkDdsjData = ref<any>(mockData) // 单点事件分析 - 主题分类情况

const zdjjsjPlsjData = ref<any>(mockData) // 批量事件分析 - 重大紧急事件
const yjztfbPlsjData = ref<any>(mockData) // 批量事件分析 - 一级主题分布
const ejztPlsjData = ref<any>(mockData) // 批量事件分析 - 二级主题TOP

const yhsysjData = ref<any>(mockData) // 用户事件分析 - 用户事件数据
const btppsjData = ref<any>(mockData) // 批量事件分析 - 不同品牌事件
const btzzbmsjData = ref<any>(mockData) // 批量事件分析 - 不同主责部门事件

const channelRankingData = mockData.map((item, index) => ({
  ...item,
  scene: item.channelName,
  mentions: ['品牌社区', '短视频平台', '商品评价', '维修保养', '满意度调研', '客服热线', '授权门店'][index],
  xxx: ['车型论坛', '用户评论', '购车反馈', '服务评价', '问卷回收', '通话记录', '现场回访'][index],
  shijianshu: 268 - index * 23,
  huanbi: `${Number((9.6 - index * 1.1).toFixed(1))}%`
}))
const qdsjphData = ref<any>(channelRankingData) // 渠道数据排行

const cardListData = ref({ value: 186, rate: 8.6, rateColor: '#00b42a' })

const gdctTopData = ref<any>(mockData) // 观点词云TOP50

// 用户使用分析 账号列表 相关的变量
const tableSortState = ref<any>({})
const loading = ref(false) // 表格加载状态
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dataList = ref<any>(mockData) // 用户列表数据
const sortableColumnOrderFieldMap: Record<string, string> = {
  originalTextScene: 'original_text_scene',
  updateTime: 'update_time',
  warningTime: 'warning_time'
}
const sortableColumnKeySet = new Set(Object.keys(sortableColumnOrderFieldMap))
// Table V2 columns configuration
const columns: Column[] = [
  {
    key: 'index',
    width: 55,
    // fixed: true,
    cellRenderer: (data: any) => {
      return data.rowIndex + 1
    },
    headerCellRenderer: () => h('span', '#')
  },
  {
    key: '员工姓名',
    title: '事件编号',
    dataKey: 'warningEventNo',
    width: 180
    // sortable: true,
    // fixed: true
  },
  {
    key: 'subjectCategoryName',
    title: '员工工号',
    dataKey: 'subjectCategoryName',
    width: 180
  },
  {
    key: 'channelName',
    title: '二级部门',
    dataKey: 'channelName',
    width: 180
  },
  {
    key: 'eventPriority',
    title: '三级部门',
    dataKey: 'eventPriority',
    width: 180
  },
  {
    key: 'title',
    title: '访问角色',
    dataKey: 'title',
    width: 220
  },
  {
    key: 'content',
    title: '操作权限',
    dataKey: 'content',
    width: 220
  },
  {
    key: 'originalTextScene',
    title: '最近登录时间',
    dataKey: 'originalTextScene',
    width: 220,
    sortable: true,
    headerClass: 'single-point-sortable-header',
    headerCellRenderer: () => renderDefaultTableSortHeader('最近登录时间', 'originalTextScene')
  },
  {
    key: 'topicText',
    title: '登录次数',
    dataKey: 'topicText',
    width: 220
  },
  {
    key: 'brandName',
    title: '全部原声聆听数',
    dataKey: 'brandName',
    width: 180
  },
  {
    key: 'carSeriesName',
    title: '抱怨原声聆听数',
    dataKey: 'carSeriesName',
    width: 180
  },
  {
    key: 'authorName',
    title: '浏览时长',
    dataKey: 'authorName',
    width: 180
  }
]

/**
 * 将 TableV2 的 asc/desc 状态映射为普通 el-table 的表头类名，
 * 仅用于排序箭头的视觉表现，不影响实际排序参数。
 */
const getDefaultTableSortClass = (sortKey: string) => {
  const order = tableSortState.value?.[sortKey]
  if (order === 'asc') return 'ascending'
  if (order === 'desc') return 'descending'
  return ''
}
/**
 * 复用普通 el-table 的 caret DOM 结构，替换 TableV2 默认排序图标。
 * 点击行为仍由 TableV2 外层表头容器处理，这里只负责渲染视觉。
 */
const renderDefaultTableSortHeader = (title: string, sortKey: string) => {
  return h('div', { class: ['single-point-sort-header', getDefaultTableSortClass(sortKey)] }, [
    h('span', { class: 'single-point-sort-header__text' }, title),
    h(
      'button',
      {
        type: 'button',
        class: 'caret-wrapper',
        tabindex: -1,
        'aria-hidden': 'true'
      },
      [h('i', { class: 'sort-caret ascending' }), h('i', { class: 'sort-caret descending' })]
    )
  ])
}

/**
 * 对齐普通 el-table 的三态排序：
 * 空状态 -> 升序 -> 降序 -> 空状态。
 * 空状态下不向后端传 order 字段。
 */
const getNextSortOrder = (currentOrder?: string) => {
  if (currentOrder === 'asc') return 'desc'
  if (currentOrder === 'desc') return undefined
  return 'asc'
}

const handleColumnSort = ({ key }: { key: string | number | symbol; order?: string }) => {
  const sortKey = String(key)
  if (!sortableColumnKeySet.has(sortKey)) return

  const orderField = sortableColumnOrderFieldMap[sortKey]
  if (!orderField) return

  const currentOrder = tableSortState.value?.[sortKey]
  const nextSortOrder = getNextSortOrder(currentOrder)

  if (!nextSortOrder) {
    tableSortState.value = {}
    formData.value.order = undefined
  } else {
    tableSortState.value = { [sortKey]: nextSortOrder }
    formData.value.order = `${orderField} ${nextSortOrder}`
  }

  if (currentPage.value === 1) {
    handleQuery()
  } else {
    currentPage.value = 1
  }
}

const paginationChange = (page: number, size: number) => {
  currentPage.value = page
  pageSize.value = size
  console.log('分页组件变化', page, size)

  handleQuery()
}

const handleQuery = () => {
  console.log('执行查询')
  const queryParams = cleanEmptyParams({
    pageSize: pageSize.value,
    pageNum: currentPage.value,
    ...formData.value,
    startTime: queryTimeRange.value.startTime,
    endTime: queryTimeRange.value.endTime
  })
}

const handleReset = () => {
  console.log('重置之前处理数据')
  formData.value = {} // 清空查询参数
  sjqsbhData.value = mockData
  qdsjphData.value = channelRankingData
  shortcutValue.value = '近7天'
  const [startTime, endTime] = fDatePickerRef.value?.getShortcutDateRange(shortcutValue.value) || []

  if (startTime && endTime) {
    times.value = [dayjs(startTime).format('YYYY-MM-DD'), dayjs(endTime).format('YYYY-MM-DD')]
  }

  tableSortState.value = {}
}

const handleYjztfbChartClick = (item: any) => {
  console.log('点击了一级主题分布', item)
}

const handleYhfbChartBarClick = (item: any) => {
  console.log('点击了用户事件分布', item)
}

const handleBtppsjChartClick = (item: any) => {
  console.log('点击了不同品牌事件', item)
}
const handleBtzzbmsjChartClick = (item: any) => {
  console.log('点击了不同主责部门事件', item)
}

const handleQdsjphSort = (item: any) => {
  console.log('点击了渠道数据排行', item)
}

// 处理情感切换事件
const handleSentimentChange = (payload: { sentiment: string | undefined }) => {
  // 重新获取词云数据
  console.log('情感切换', payload)
}

const handleDownLoadData = () => {
  console.log('下载数据')
}

// 监听页面类型变化，自动刷新列表
watch(
  () => middlewareStore.closedLoopType,
  () => {
    // 先重置数据 比如查询参数、变量等等
    handleReset()

    handleQuery()
  }
)

// 品牌选项直接取登录态中的品牌树，保持与其他筛选页一致的数据源
const brandOptions = computed(() => {
  const bl = userStore.getBrandService || []

  return bl
})

/**
 * 将筛选值统一转换为可比较的字符串数组，兼容空值、数字和历史缓存值。
 */
const normalizeSelectedValues = (values: unknown): string[] => {
  if (Array.isArray(values)) {
    return values
      .filter(value => value !== '' && value !== null && value !== undefined)
      .map(value => String(value))
  }
  if (values === '' || values === null || values === undefined) {
    return []
  }
  return [String(values)]
}

/**
 * 品牌数据在不同页面里可能同时存在 key/value/brandCode 三套字段，
 * 联动时统一兼容它们，避免多选改造后因字段不一致拿不到车系。
 */
const isMatchedBrandValue = (brand: any, selectedBrandValue: string) => {
  const brandValueCandidates = [brand?.key, brand?.value, brand?.brandCode]
    .filter(value => value !== '' && value !== null && value !== undefined)
    .map(value => String(value))

  return brandValueCandidates.includes(selectedBrandValue)
}

/**
 * 车系也兼容多套值字段，既用于去重，也用于回收失效选中项。
 */
const getSeriesOptionValue = (series: any) => {
  // const value = series?.key ?? series?.value ?? series?.carSeriesCode
  const value = series?.key
  return value === '' || value === null || value === undefined ? '' : String(value)
}

/**
 * 根据已选品牌聚合车系选项。
 * 这里做去重是为了兼容多个品牌下出现同编码车系的场景，避免下拉中出现重复项。
 */
const buildCarSeriesOptionsByBrandCodes = (brandCodes: string[] = []) => {
  const selectedBrandValues = normalizeSelectedValues(brandCodes)
  if (!selectedBrandValues.length) return []

  const seriesMap = new Map<string, any>()

  brandOptions.value.forEach(brand => {
    const isSelectedBrand = selectedBrandValues.some(selectedBrandValue =>
      isMatchedBrandValue(brand, selectedBrandValue)
    )
    if (!isSelectedBrand) {
      return
    }

    brand?.children?.forEach((series: any) => {
      const seriesValue = getSeriesOptionValue(series)
      if (seriesValue && !seriesMap.has(seriesValue)) {
        seriesMap.set(seriesValue, series)
      }
    })
  })

  return Array.from(seriesMap.values())
}

const carSeriesOptions = computed(() => {
  return buildCarSeriesOptionsByBrandCodes(formData.value.brandCodes || [])
})

/**
 * 当品牌变化后，仅保留当前品牌集合下仍然合法的车系编码。
 * 这样可以避免多品牌切换后把失效车系继续带给后端。
 */
const syncCarSeriesCodesByBrandCodes = (brandCodes: string[] = []) => {
  const validCarSeriesCodeSet = new Set(
    buildCarSeriesOptionsByBrandCodes(brandCodes)
      .map((item: any) => getSeriesOptionValue(item))
      .filter(Boolean)
  )
  const selectedCarSeriesCodes = normalizeSelectedValues(formData.value.carSeriesCodes || [])

  formData.value.carSeriesCodes = selectedCarSeriesCodes.filter(code =>
    validCarSeriesCodeSet.has(code)
  )
}

watch(
  () => formData.value.brandCodes,
  brandCodes => {
    syncCarSeriesCodesByBrandCodes(brandCodes || [])
  },
  { deep: true }
)

watch(
  carSeriesOptions,
  () => {
    syncCarSeriesCodesByBrandCodes(formData.value.brandCodes || [])
  },
  { deep: true }
)

const FCardHeight = computed(() => {
  return isExpanded.value ? `calc(100vh - 84px - 252px - 20px)` : `calc(100vh - 34px - 252px)`
})

// 标准观点选项
const topicOptions = ref<any[]>([])

// 获取多个标签的子级选项并集（去重）- 不影响详情页的getTagChildren方法
const getTagChildrenMultiple = (tagCodes: string[]) => {
  if (!tagCodes || tagCodes.length === 0) return []
  const allChildren: any[] = []
  const seenCodes = new Set<string>()

  tagCodes.forEach(code => {
    const children = singleEventStore.getTagChildren(code)
    children.forEach((child: any) => {
      if (!seenCodes.has(child.tagCode)) {
        seenCodes.add(child.tagCode)
        allChildren.push(child)
      }
    })
  })

  return allChildren
}

// 确定体验代码末级（四级 > 三级 > 二级 > 一级）
const getLastLevelCodes = (): string[] => {
  if (formData.value.fourCodeTag && formData.value.fourCodeTag.length > 0) {
    return formData.value.fourCodeTag
  }
  if (formData.value.threeCodeTag && formData.value.threeCodeTag.length > 0) {
    return formData.value.threeCodeTag
  }
  if (formData.value.secondCodeTag && formData.value.secondCodeTag.length > 0) {
    return formData.value.secondCodeTag
  }
  if (formData.value.firstCodeTag && formData.value.firstCodeTag.length > 0) {
    return formData.value.firstCodeTag
  }
  return []
}

// 根据末级体验代码获取标准观点
const fetchTopicsByLastLevel = async () => {
  const lastLevelCodes = getLastLevelCodes()
  // 如果没有选择体验代码，默认查询所有标准观点
  const codesToFetch = lastLevelCodes.length === 0 ? [] : lastLevelCodes

  try {
    const res = await getTopicsByTagId(codesToFetch)
    if (res.success && res.result && Array.isArray(res.result)) {
      // 去重处理，避免重复的观点
      // const seenTopics = new Set<string>()
      // const allTopics: any[] = []
      // res.result.forEach((topic: any) => {
      //   const topicCode = topic.tagCode || topic.code
      //   if (topicCode && !seenTopics.has(topicCode)) {
      //     seenTopics.add(topicCode)
      //     allTopics.push(topic)
      //   }
      // })
      topicOptions.value = res.result
    } else {
      topicOptions.value = []
    }
  } catch (error) {
    console.error('获取标准观点失败:', error)
    topicOptions.value = []
  }
}

// 一级标签变化
const handleFirstCodeChange = () => {
  formData.value.secondCodeTag = []
  formData.value.threeCodeTag = []
  formData.value.fourCodeTag = []
  formData.value.topicList = [] // 清空标准观点
  topicOptions.value = [] // 清空标准观点选项
}

// 二级标签变化
const handleSecondCodeChange = () => {
  formData.value.threeCodeTag = []
  formData.value.fourCodeTag = []
  formData.value.topicList = [] // 清空标准观点
  topicOptions.value = [] // 清空标准观点选项
}

// 三级标签变化
const handleThreeCodeChange = () => {
  formData.value.fourCodeTag = []
  formData.value.topicList = [] // 清空标准观点
  topicOptions.value = [] // 清空标准观点选项
}

// 四级标签变化
const handleFourCodeChange = () => {
  formData.value.topicList = [] // 清空标准观点
  topicOptions.value = [] // 清空标准观点选项
  // 标准观点的更新由 watch 统一处理
}

// 计算属性：获取各级体验代码选项
const firstCodeOptions = computed(() => {
  return singleEventStore.tagTreeList || []
})

const secondCodeOptions = computed(() => {
  return getTagChildrenMultiple(formData.value.firstCodeTag || [])
})

const threeCodeOptions = computed(() => {
  return getTagChildrenMultiple(formData.value.secondCodeTag || [])
})

const fourCodeOptions = computed(() => {
  return getTagChildrenMultiple(formData.value.threeCodeTag || [])
})

// 监听体验代码变化，自动获取标准观点
watch(
  [
    () => formData.value.fourCodeTag,
    () => formData.value.threeCodeTag,
    () => formData.value.secondCodeTag,
    () => formData.value.firstCodeTag
  ],
  () => {
    // 当体验代码变化时，重新获取标准观点（如果没有体验代码，则获取所有标准观点）
    fetchTopicsByLastLevel()
  },
  { deep: true }
)

// 页面初始化时加载所有标准观点
onMounted(() => {
  fetchTopicsByLastLevel()
})
</script>

<template>
  <div>
    <FCard :title="'筛选条件'" titleSize="middle" :is-show-more="false" :height="'auto'">
      <FFilterLayout v-model="isExpanded" @query="handleQuery" @reset="handleReset">
        <el-form layout="inline" :model="formData" label-position="right">
          <!-- 单点事件分析 查询条件 -->
          <el-row
            v-if="middlewareStore.closedLoopType === EventAnalyType.SingleEventAnaly"
            class="w-full"
            :gutter="24"
          >
            <el-col :span="8">
              <el-form-item label="预警时间">
                <FDatePicker
                  v-model="times"
                  v-model:shortcutValue="shortcutValue"
                  type="daterange"
                  :clearable="false"
                  class="iround-4"
                  size="default"
                  ref="fDatePickerRef"
                ></FDatePicker>
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="品牌">
                <el-select
                  v-model="formData.brandCodes"
                  placeholder="全部"
                  clearable
                  filterable
                  multiple
                  :max-collapse-tags="1"
                  collapse-tags
                  :options="brandOptions"
                  :props="{ label: 'value', value: 'key' }"
                />
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="车系">
                <el-select
                  v-model="formData.carSeriesCodes"
                  placeholder="全部"
                  clearable
                  filterable
                  multiple
                  :max-collapse-tags="1"
                  collapse-tags
                  :options="carSeriesOptions"
                  :props="{ label: 'value', value: 'key' }"
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="数据源">
                <el-cascader
                  v-model="formData.channelCodes"
                  :options="singleEventStore.dataChannel"
                  :max-collapse-tags="1"
                  collapse-tags
                  :show-all-levels="false"
                  show-checked-strategy="parent"
                  filterable
                  clearable
                  :props="{
                    label: 'name',
                    value: 'code',
                    children: 'child',
                    multiple: true,
                    emitPath: false
                  }"
                  class="w-full"
                />
              </el-form-item>
            </el-col>

            <el-col :span="24">
              <el-form-item label="体验代码">
                <div class="experience-code-wrapper">
                  <el-select
                    v-model="formData.firstCodeTag"
                    placeholder="请选择"
                    clearable
                    filterable
                    multiple
                    :max-collapse-tags="1"
                    collapse-tags
                    :options="firstCodeOptions"
                    :props="{ label: 'tagName', value: 'tagCode' }"
                    @change="handleFirstCodeChange"
                    class="experience-code-select"
                  />
                  <el-select
                    v-model="formData.secondCodeTag"
                    placeholder="请选择"
                    clearable
                    filterable
                    multiple
                    :max-collapse-tags="1"
                    collapse-tags
                    :options="secondCodeOptions"
                    :props="{ label: 'tagName', value: 'tagCode' }"
                    @change="handleSecondCodeChange"
                    class="experience-code-select"
                  />
                  <el-select
                    v-model="formData.threeCodeTag"
                    placeholder="请选择"
                    clearable
                    filterable
                    multiple
                    :max-collapse-tags="1"
                    collapse-tags
                    :options="threeCodeOptions"
                    :props="{ label: 'tagName', value: 'tagCode' }"
                    @change="handleThreeCodeChange"
                    class="experience-code-select"
                  />
                  <el-select
                    v-model="formData.fourCodeTag"
                    placeholder="请选择"
                    clearable
                    filterable
                    multiple
                    :max-collapse-tags="1"
                    collapse-tags
                    :options="fourCodeOptions"
                    :props="{ label: 'tagName', value: 'tagCode' }"
                    @change="handleFourCodeChange"
                    class="experience-code-select"
                  />
                </div>
              </el-form-item>
            </el-col>

            <el-col :span="4">
              <el-form-item label="主题分类">
                <el-select
                  v-model="formData.subjectCategoryIds"
                  placeholder="不限"
                  clearable
                  filterable
                  :options="singleEventStore.closedLoopCategory"
                  :props="{ label: 'name', value: 'id' }"
                  multiple
                  :max-collapse-tags="1"
                  collapse-tags
                />
              </el-form-item>
            </el-col>

            <el-col :span="4">
              <el-form-item label="事件有效性">
                <el-select
                  v-model="formData.eventValidityList"
                  placeholder="不限"
                  clearable
                  filterable
                  :options="singleEventStore.task_event_validity"
                  :props="{ label: 'text', value: 'value' }"
                  multiple
                  :max-collapse-tags="1"
                  collapse-tags
                />
              </el-form-item>
            </el-col>

            <el-col :span="4">
              <el-form-item label="事件属性">
                <el-select
                  v-model="formData.eventAttributeList"
                  placeholder="不限"
                  clearable
                  filterable
                  :options="singleEventStore.event_attribute"
                  :props="{ label: 'text', value: 'value' }"
                  multiple
                  :max-collapse-tags="1"
                  collapse-tags
                />
              </el-form-item>
            </el-col>
          </el-row>
          <!-- 批量事件分析 查询条件 -->
          <el-row
            v-else-if="middlewareStore.closedLoopType === EventAnalyType.BatchEventAnaly"
            class="w-full"
            :gutter="24"
          >
            <!-- 第一行 -->
            <el-col :span="8">
              <el-form-item label="预警时间">
                <FDatePicker
                  v-model="times"
                  v-model:shortcutValue="shortcutValue"
                  type="daterange"
                  :clearable="false"
                  class="iround-4"
                  size="default"
                  ref="fDatePickerRef"
                ></FDatePicker>
              </el-form-item>
            </el-col>

            <el-col :span="4">
              <el-form-item label="预警频率">
                <el-select
                  v-model="formData.yujingpinlv"
                  placeholder="全部"
                  clearable
                  filterable
                  multiple
                  :max-collapse-tags="1"
                  collapse-tags
                  :options="[]"
                  :props="{ label: 'value', value: 'key' }"
                />
              </el-form-item>
            </el-col>

            <el-col :span="4">
              <el-form-item label="品牌">
                <el-select
                  v-model="formData.brandCodes"
                  placeholder="全部"
                  clearable
                  filterable
                  multiple
                  :max-collapse-tags="1"
                  collapse-tags
                  :options="brandOptions"
                  :props="{ label: 'value', value: 'key' }"
                />
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <el-form-item label="数据源">
                <el-cascader
                  v-model="formData.channelCodes"
                  :options="singleEventStore.dataChannel"
                  :max-collapse-tags="1"
                  collapse-tags
                  :show-all-levels="false"
                  show-checked-strategy="parent"
                  filterable
                  clearable
                  :props="{
                    label: 'name',
                    value: 'code',
                    children: 'child',
                    multiple: true,
                    emitPath: false
                  }"
                  class="w-full"
                />
              </el-form-item>
            </el-col>
            <!-- 第二行 -->
            <el-col :span="4">
              <el-form-item label="事件状态">
                <el-select
                  v-model="formData.taskStatuses"
                  placeholder="不限"
                  clearable
                  filterable
                  :options="singleEventStore.task_event_staus"
                  :props="{ label: 'text', value: 'value' }"
                  multiple
                  :max-collapse-tags="1"
                  collapse-tags
                />
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="处理优先级">
                <el-select
                  v-model="formData.eventPriorities"
                  placeholder="不限"
                  clearable
                  filterable
                  :options="singleEventStore.closed_rule_priority"
                  :props="{ label: 'text', value: 'value' }"
                  multiple
                  :max-collapse-tags="1"
                  collapse-tags
                />
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="主题分类">
                <el-select
                  v-model="formData.subjectCategoryIds"
                  placeholder="不限"
                  clearable
                  filterable
                  :options="singleEventStore.closedLoopCategory"
                  :props="{ label: 'name', value: 'id' }"
                  multiple
                  :max-collapse-tags="1"
                  collapse-tags
                />
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="车系">
                <el-select
                  v-model="formData.carSeriesCodes"
                  placeholder="全部"
                  clearable
                  filterable
                  multiple
                  :max-collapse-tags="1"
                  collapse-tags
                  :options="carSeriesOptions"
                  :props="{ label: 'value', value: 'key' }"
                />
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="主责部门">
                <el-select
                  v-model="formData.mainRespOrgId"
                  placeholder="不限"
                  clearable
                  filterable
                  :options="singleEventStore.mainRespOrgs"
                  :props="{ label: 'mainRespOrgName', value: 'mainRespOrgId' }"
                  multiple
                  :max-collapse-tags="1"
                  collapse-tags
                />
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="处理人员">
                <el-input
                  v-model.trim="formData.handler"
                  clearable
                  placeholder="请输入"
                  :maxlength="20"
                ></el-input>
              </el-form-item>
            </el-col>
            <!-- 第三行 -->
            <el-col :span="6">
              <el-form-item label="事件编号">
                <el-input
                  v-model.trim="formData.warningEventNo"
                  clearable
                  placeholder="请输入"
                  :maxlength="50"
                ></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="事件名称">
                <el-input
                  v-model.trim="formData.shijianmingcheng"
                  clearable
                  placeholder="请输入"
                  :maxlength="50"
                ></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="意图">
                <el-select
                  v-model="formData.yitu"
                  placeholder="全部"
                  clearable
                  filterable
                  multiple
                  :max-collapse-tags="1"
                  collapse-tags
                  :options="[]"
                  :props="{ label: 'value', value: 'key' }"
                />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="创建人员">
                <el-input
                  v-model.trim="formData.chuangjianrenyuan"
                  clearable
                  placeholder="请输入"
                  :maxlength="50"
                ></el-input>
              </el-form-item>
            </el-col>
            <!-- 第四行 -->
            <el-col :span="6">
              <el-form-item label="事件有效性">
                <el-select
                  v-model="formData.eventValidityList"
                  placeholder="不限"
                  clearable
                  filterable
                  :options="singleEventStore.task_event_validity"
                  :props="{ label: 'text', value: 'value' }"
                  multiple
                  :max-collapse-tags="1"
                  collapse-tags
                />
              </el-form-item>
            </el-col>
            <el-col :span="18">
              <el-form-item label="体验代码">
                <div class="experience-code-wrapper">
                  <el-select
                    v-model="formData.firstCodeTag"
                    placeholder="请选择"
                    clearable
                    filterable
                    multiple
                    :max-collapse-tags="1"
                    collapse-tags
                    :options="firstCodeOptions"
                    :props="{ label: 'tagName', value: 'tagCode' }"
                    @change="handleFirstCodeChange"
                    class="experience-code-select"
                  />
                  <el-select
                    v-model="formData.secondCodeTag"
                    placeholder="请选择"
                    clearable
                    filterable
                    multiple
                    :max-collapse-tags="1"
                    collapse-tags
                    :options="secondCodeOptions"
                    :props="{ label: 'tagName', value: 'tagCode' }"
                    @change="handleSecondCodeChange"
                    class="experience-code-select"
                  />
                  <el-select
                    v-model="formData.threeCodeTag"
                    placeholder="请选择"
                    clearable
                    filterable
                    multiple
                    :max-collapse-tags="1"
                    collapse-tags
                    :options="threeCodeOptions"
                    :props="{ label: 'tagName', value: 'tagCode' }"
                    @change="handleThreeCodeChange"
                    class="experience-code-select"
                  />
                  <el-select
                    v-model="formData.fourCodeTag"
                    placeholder="请选择"
                    clearable
                    filterable
                    multiple
                    :max-collapse-tags="1"
                    collapse-tags
                    :options="fourCodeOptions"
                    :props="{ label: 'tagName', value: 'tagCode' }"
                    @change="handleFourCodeChange"
                    class="experience-code-select"
                  />
                </div>
              </el-form-item>
            </el-col>
            <!-- 第五行 -->
            <el-col :span="6">
              <el-form-item label="标准观点">
                <el-select-v2
                  v-model="formData.topicList"
                  placeholder="请选择"
                  clearable
                  filterable
                  multiple
                  :max-collapse-tags="1"
                  collapse-tags
                  :options="topicOptions"
                  :props="{ label: 'tagName', value: 'tagName' }"
                />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="事件属性">
                <el-select
                  v-model="formData.eventAttributeList"
                  placeholder="不限"
                  clearable
                  filterable
                  :options="singleEventStore.event_attribute"
                  :props="{ label: 'text', value: 'value' }"
                  multiple
                  :max-collapse-tags="1"
                  collapse-tags
                />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="业务责任人">
                <el-input
                  v-model.trim="formData.yewuzerenren"
                  clearable
                  placeholder="请输入"
                  :maxlength="20"
                ></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="是否驳回">
                <el-select
                  v-model="formData.shifoubohui"
                  placeholder="不限"
                  clearable
                  filterable
                  :options="[]"
                  :props="{ label: 'text', value: 'value' }"
                  multiple
                  :max-collapse-tags="1"
                  collapse-tags
                />
              </el-form-item>
            </el-col>
          </el-row>
          <!-- 用户使用分析 查询条件 -->
          <el-row v-else class="w-full" :gutter="24">
            <el-col :span="8">
              <el-form-item label="预警时间">
                <FDatePicker
                  v-model="times"
                  v-model:shortcutValue="shortcutValue"
                  type="daterange"
                  :clearable="false"
                  class="iround-4"
                  size="default"
                  ref="fDatePickerRef"
                ></FDatePicker>
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="部门">
                <el-select
                  v-model="formData.mainRespOrgId"
                  placeholder="不限"
                  clearable
                  filterable
                  :options="singleEventStore.mainRespOrgs"
                  :props="{ label: 'mainRespOrgName', value: 'mainRespOrgId' }"
                  multiple
                  :max-collapse-tags="1"
                  collapse-tags
                />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </FFilterLayout>
    </FCard>

    <div
      class="mt-24"
      :style="{
        width: '100%',
        height: FCardHeight,
        background: 'rgba(255, 255, 255, 0.8)',
        overflowY: 'auto',
        padding: '20px 24px 0'
      }"
    >
      <!-- 卡片 -->
      <DdCard :type="middlewareStore.closedLoopType" :card-list-data="cardListData" />
      <!-- 数据趋势变化 -->
      <DataTrend class="mt-24 f-card-border" :data="sjqsbhData"></DataTrend>
      <!-- 单点事件分析饼状图 -->
      <div
        v-if="middlewareStore.closedLoopType === EventAnalyType.SingleEventAnaly"
        class="mt-24"
        style="display: flex"
      >
        <FCard :title="'重大紧急事件'" titleSize="small" :height="'361px'" class="f-card-border">
          <ProportionOfPieChart :data="zdjjsjDdsjData"></ProportionOfPieChart>
        </FCard>
        <div style="width: 24px; height: 361px"></div>

        <FCard :title="'主题分类情况'" titleSize="small" :height="'361px'" class="f-card-border">
          <ProportionOfPieChart :data="ztflqkDdsjData"></ProportionOfPieChart>
        </FCard>
      </div>
      <!-- 批量事件分析饼状图 -->
      <div
        v-if="middlewareStore.closedLoopType === EventAnalyType.BatchEventAnaly"
        class="mt-24"
        style="display: flex"
      >
        <FCard :title="'重大紧急事件'" titleSize="small" :height="'361px'" class="f-card-border">
          <ProportionOfPieChart :data="zdjjsjPlsjData"></ProportionOfPieChart>
        </FCard>
        <div style="width: 24px; height: 361px"></div>

        <FCard :title="'一级主题分布'" titleSize="small" :height="'361px'" class="f-card-border">
          <ProportionOfPieChart
            :data="yjztfbPlsjData"
            @chart-click="handleYjztfbChartClick"
          ></ProportionOfPieChart>
        </FCard>

        <div style="width: 24px; height: 361px"></div>

        <FCard
          :title="'【一级主题】二级主题TOP'"
          titleSize="small"
          :height="'361px'"
          class="f-card-border"
        >
          <SecendTable :data="ejztPlsjData"></SecendTable>
        </FCard>
      </div>

      <!-- 用户使用分析饼状图 -->
      <div v-if="middlewareStore.closedLoopType === EventAnalyType.UserUseAnaly" class="mt-24">
        <FCard :title="'用户分布'" titleSize="small" :height="'361px'" class="f-card-border">
          <UserEventTrend
            :data="yhsysjData"
            @chart-click="handleYhfbChartBarClick"
          ></UserEventTrend>
        </FCard>
      </div>

      <!-- 批量事件分析 不同品牌事件 -->
      <FCard
        v-if="middlewareStore.closedLoopType === EventAnalyType.BatchEventAnaly"
        :title="'不同品牌事件'"
        titleSize="small"
        :height="'361px'"
        class="f-card-border mt-24"
      >
        <DataThreeTrend
          :data-trend-change-data="btppsjData"
          @chart-click="handleBtppsjChartClick"
        ></DataThreeTrend>
      </FCard>

      <!-- 批量事件分析 不同主责部门事件 -->
      <FCard
        v-if="middlewareStore.closedLoopType === EventAnalyType.BatchEventAnaly"
        :title="'不同主责部门事件'"
        titleSize="small"
        :height="'361px'"
        class="f-card-border mt-24"
      >
        <DataThreeTrend
          :data-trend-change-data="btzzbmsjData"
          @chart-click="handleBtzzbmsjChartClick"
        ></DataThreeTrend>
      </FCard>
      <!-- 单点事件分析 批量事件分析 渠道数据排行 -->
      <FCard
        v-if="
          [EventAnalyType.SingleEventAnaly, EventAnalyType.BatchEventAnaly].includes(
            middlewareStore.closedLoopType
          )
        "
        :title="'渠道数据排行'"
        titleSize="small"
        :height="'600px'"
        :isShowMore="false"
        class="f-card-border mt-24"
      >
        <ChanelTable
          :focus-scene-top-data="qdsjphData"
          @sort-change="handleQdsjphSort"
        ></ChanelTable>
      </FCard>

      <!-- 单点事件分析 批量事件分析 观点词云TOP50 -->
      <div
        v-if="
          [EventAnalyType.SingleEventAnaly, EventAnalyType.BatchEventAnaly].includes(
            middlewareStore.closedLoopType
          )
        "
        class="f-card-border mt-24"
      >
        <WordTop :opinionTopVos="gdctTopData" @sentiment-change="handleSentimentChange"> </WordTop>
      </div>

      <!-- 用户使用分析 帐号列表 -->
      <FCard
        v-if="middlewareStore.closedLoopType === EventAnalyType.UserUseAnaly"
        :title="'帐号列表'"
        titleSize="small"
        :height="'700px'"
        class="f-card-border mt-24"
      >
        <template #more>
          <el-button type="primary" @click="handleDownLoadData">
            <el-icon><Download /></el-icon>下载数据
          </el-button>
        </template>
        <!-- 使用div容器包装，高度从FCard动态计算 -->
        <div class="table-container">
          <el-auto-resizer>
            <template #default="slotProps">
              <el-table-v2
                :data-testid="`dataSource-result-table`"
                v-loading="loading"
                :columns="columns"
                :data="dataList"
                :sort-state="tableSortState"
                :on-column-sort="handleColumnSort"
                :width="slotProps.width"
                :height="slotProps.height"
                :row-key="'id'"
                fixed
              />
            </template>
          </el-auto-resizer>
        </div>
        <!-- 分页组件 -->
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100, 200, 500, 1000]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          style="margin-top: 16px; justify-content: flex-end"
          @change="paginationChange"
        />
      </FCard>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.table-container {
  height: calc(100% - 48px); /* 减去分页组件的高度48px */
}

:deep(.single-point-sortable-header .el-table-v2__sort-icon) {
  display: none;
}

:deep(.single-point-sort-header) {
  width: 100%;
  display: inline-flex;
  align-items: center;
  line-height: 23px;
}

:deep(.single-point-sort-header__text) {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:deep(.single-point-sort-header .caret-wrapper) {
  cursor: pointer;
  appearance: none;
  border-radius: var(--el-border-radius-base);
  transition: transform var(--el-transition-duration-fast) ease-in-out;
  background-color: transparent;
  border: none;
  outline: none;
  margin: 0;
  padding: 0;
  vertical-align: middle;
  width: 24px;
  height: 14px;
  overflow: initial;
  flex-direction: column;
  align-items: center;
  display: inline-flex;
  position: relative;
  flex-shrink: 0;
  pointer-events: none;
}

:deep(.single-point-sort-header .sort-caret) {
  border: 5px solid transparent;
  width: 0;
  height: 0;
  position: absolute;
  left: 7px;
  pointer-events: none;
}

:deep(.single-point-sort-header .sort-caret.ascending) {
  border-bottom-color: var(--el-text-color-placeholder);
  top: -5px;
}

:deep(.single-point-sort-header .sort-caret.descending) {
  border-top-color: var(--el-text-color-placeholder);
  bottom: -3px;
}

:deep(.single-point-sort-header.ascending .sort-caret.ascending) {
  border-bottom-color: var(--el-color-primary);
}

:deep(.single-point-sort-header.descending .sort-caret.descending) {
  border-top-color: var(--el-color-primary);
}

:deep(.single-point-event__text-ellipsis) {
  width: 100%;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:deep(.single-point-event__text-tooltip) {
  max-width: min(520px, calc(100vw - 32px));
}

:deep(.single-point-event__text-tooltip-content) {
  max-height: min(320px, calc(100vh - 48px));
  overflow: auto;
  box-sizing: border-box;
  white-space: normal;
  word-break: break-word;
  overflow-wrap: anywhere;
  line-height: 20px;
}

.experience-code-wrapper {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;

  .experience-code-select {
    flex: 1;
    min-width: 200px;
  }
}
</style>
