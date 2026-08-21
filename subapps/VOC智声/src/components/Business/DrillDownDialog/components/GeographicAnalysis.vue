<script setup lang="ts">
// 地域分析（已切换为无限下钻）
import { computed, ref, watch, reactive, nextTick } from 'vue'
import {
  DEFAULT_MENTION_NEGATIVE_RATE_TYPE,
  MENTION_NEGATIVE_RATE_SWITCH_OPTIONS,
  WORD_CLOUD_RANDOM_COLOR_PALETTE
} from '@/constants'
import { DrillTabKey } from '../constants'
import RegionalDistributionMap from './RegionalDistributionMap.vue'
import type { MapDataItem } from '@/components/Charts/FMapChart/types'
import WordCloudChart from '@components/DataSourceAnalysis/WordCloudChart.vue'
import CommonTitle from '@components/Business/DrillDownDialog/components/CommonTitle'
import DrillDownTable from '@components/Business/DrillDownDialog/components/DrillDownTable'
import type {
  SortChangeEvent,
  TableHeaderGroup
} from '@components/Business/DrillDownDialog/components/DrillDownTable'
import { fmtNum, fmtPer, fmtFix } from '@/utils'
import TopDealerReviews from '@components/Business/DrillDownDialog/components/TopDealerReviews/index.vue'
import {
  getDealerRankTop,
  getOpinionEvaluateTop,
  getProvinceList,
  getProvinceMap
} from '@api/drillDownDialog'
import type { DealerRankTopItem, ProvinceListItem } from '@api/drillDownDialog/types.d.ts'
import useUserStore from '@/store/modules/user.ts'
import useGeneralDrillDownStore from '@/store/modules/generalDrillDown'

defineOptions({ name: 'GeographicAnalysis' })

// 父级透传
const props = defineProps<{ queryParams?: VocQueryParams; filters?: any[]; data?: MapDataItem[] }>()

const userStore = useUserStore()
const ddStore = useGeneralDrillDownStore()

// loading
const loading = reactive({
  provinceMap: false,
  dealerRankTop: false,
  evaluateTop: false,
  provinceList: false
})

// 地图与列表数据
const mapInnerMode = ref<MentionNegativeRateType>(DEFAULT_MENTION_NEGATIVE_RATE_TYPE)
const mapChartData = ref<any[]>([])
const rankInnerMode = ref<MentionNegativeRateType>(DEFAULT_MENTION_NEGATIVE_RATE_TYPE)
const listInnerMode = ref<'province' | 'dealer'>('province')
const dealerRankTopData = ref<DealerRankTopItem[]>([])
const provinceTableData = ref<ProvinceListItem[]>([])
// 列表锚点：分页切换后回到表格顶部（体验与“用户列表”保持一致）
const anchorRef = ref<any>()

// 列表分页状态（参考 PopulationAnalysis 用户列表分页）
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

type GeographicSortOrder = 'asc' | 'desc'

interface GeographicTableSortState {
  sortField?: string
  sortOrder?: GeographicSortOrder
}

// 当前列表排序状态：仅在用户点击表头后透传给后端
const tableSort = reactive<GeographicTableSortState>({})

// 记录列表最近一次额外查询参数（用于分页切换时复用，如切换 dealer 时带入省份过滤）
const provinceListExtraParams = ref<VocQueryParams | null>(null)

// 省份下钻场景：列表强制展示「经销商」；返回到全国时需要恢复，避免一次性渲染过多趋势图导致卡顿
const autoSwitchedToDealer = ref(false)
const syncListModeByProvince = () => {
  const hasProvince = !!props.queryParams?.provinceCodeSet?.length
  if (hasProvince) {
    if (listInnerMode.value !== 'dealer') autoSwitchedToDealer.value = true
    listInnerMode.value = 'dealer'
    return
  }

  // 仅在“曾被自动切到 dealer”的情况下，返回时自动恢复到 province
  if (autoSwitchedToDealer.value) {
    listInnerMode.value = 'province'
    autoSwitchedToDealer.value = false
  }
}

const wordCloudOptions = ref<any[]>([
  { text: '全部情感', value: '' },
  ...userStore.getDictItems('voc_sentiment')
])
const wordCloudValue = ref('')
const wordCloudData = ref<any[]>([])
// 记录观点评价TOP最近一次请求参数（含联动条件与 sentiment），用于筛选切换与词云下钻复用
const evaluateTopRequestParams = ref<VocQueryParams | null>(null)
// 请求序号：仅接受最后一次请求的响应，避免并发返回覆盖最新词云状态
const evaluateTopRequestId = ref(0)

/**
 * 组装观点评价TOP查询参数
 * 合并优先级：外部 queryParams < 词云联动上下文 < sentiment（以当前下拉选中为准）
 */
const buildEvaluateTopQueryParams = (extraParams?: VocQueryParams | null): VocQueryParams => {
  return {
    ...(props.queryParams || {}),
    ...(extraParams || {}),
    sentiment: wordCloudValue.value || ''
  }
}

const currentSelectedTopic = ref('')

// 当前点击的单元格
const curCell = ref()

const listOptions = ref<any[]>([
  { value: 'province', label: '区域' },
  { value: 'dealer', label: '经销商' }
])

//如果有省份，则显示经销商
const showSwitcher = computed(() => {
  return !props.queryParams?.provinceCodeSet?.length
})

// 地图数据
const currentSwitcher = computed(() => {
  return listOptions.value.find(item => item.value === listInnerMode.value) || {}
})

// 表格列
const tableHeaders = computed<TableHeaderGroup[]>(() => [
  {
    key: 'name',
    label: currentSwitcher.value.label,
    width: '250px',
    columnPadding: '0 12px 0 8px',
    backgroundColor: '#EAF3FF',
    tooltip: { show: true }
  },
  {
    key: 'mentions',
    label: '提及',
    backgroundColor: ['#EAF3FF', '#F2F4F7'],
    columns: [
      {
        key: 'mentions',
        label: '提及量',
        render: (row: any) => fmtNum(row.mentions),
        sortable: 'custom'
      },
      { key: 'mentionsShare', label: '占比', render: (row: any) => fmtPer(row.mentionsShare) },
      { key: 'mentionTrend', label: '趋势', width: '160px', render: 'trend' },
      {
        key: 'mentionsMoM',
        label: '环比',
        render: (row: any) => fmtFix(row.mentionsMoM),
        sortable: 'custom'
      }
    ]
  },
  {
    key: 'negative',
    label: '负面',
    backgroundColor: ['#FEE9E5', '#F2F4F7'],
    columns: [
      {
        key: 'negativeRateValue',
        label: '负面率',
        render: (row: any) =>
          `<div style="background-color: ${row.rateBackgroundColor}; color: ${row.rateColor}">${fmtPer(row.negativeRateValue)}</div>`,
        sortable: 'custom'
      },
      { key: 'negativeTrend', label: '趋势', width: '160px', render: 'trend' },
      {
        key: 'negativeRateMoM',
        label: '环比',
        render: (row: any) => fmtFix(row.negativeRateMoM),
        sortable: 'custom'
      }
    ]
  },
  {
    key: 'positive',
    label: '正面',
    backgroundColor: ['#DAF7EE', '#F2F4F7'],
    columns: [
      {
        key: 'positiveRateValue',
        label: '正面率',
        render: (row: any) => fmtPer(row.positiveRateValue),
        sortable: 'custom'
      },
      { key: 'positiveTrend', label: '趋势', width: '160px', render: 'trend' },
      {
        key: 'positiveRateMoM',
        label: '环比',
        render: (row: any) => fmtFix(row.positiveRateMoM),
        sortable: 'custom'
      }
    ]
  }
])

const mapModeChange = (option: any) => {
  mapInnerMode.value = option.value
}

// TopDealerReviews组件引用
const topDealerReviewsRef = ref<any>(null)

const rankModeChange = (option: any) => {
  rankInnerMode.value = option.value
  // 清除表格排序状态
  nextTick(() => {
    topDealerReviewsRef.value?.clearSort()

    fetchDealerRankTop()
  })
}

const listModeChange = (option: any) => {
  listInnerMode.value = option.value
  // 数据维度变更，回到第一页
  pagination.pageNum = 1
  fetchProvinceList({
    provinceCodeSet: curCell.value?.code ? [curCell.value?.code] : undefined
  })
}

//省份点击
const handleProvinceClick = (_provinceName: string, provinceData: any) => {
  // 这里可以处理省份点击后的逻辑，比如打开详情弹窗等
  const provinceCodeSet = provinceData.provinceCode ? [provinceData.provinceCode] : []
  fetchDealerRankTop({ provinceCodeSet: provinceCodeSet })
  fetchEvaluateTop({ provinceCodeSet: provinceCodeSet })
}

// 经销商排行TOP点击
const handleRankClick = (row: DealerRankTopItem): void => {
  fetchEvaluateTop({
    provinceCodeSet: row.provinceCode ? [row.provinceCode] : [],
    dealerCode: row.dealerCode
  })
}

// 经销商排行TOP排序
const handleRankSortChange = (sortInfo: { prop: string; order: string }): void => {
  fetchDealerRankTop({
    provinceCodeSet: curCell.value?.code ? [curCell.value?.code] : undefined,
    sortField: sortInfo.prop,
    sortOrder: sortInfo.order
  })
}

// 请求：省份地图
const fetchProvinceMap = async (params?: VocQueryParams) => {
  try {
    loading.provinceMap = true
    const qp: VocQueryParams = {
      ...(props.queryParams || {}),
      ...(params || {}),
      dataType: mapInnerMode.value
    }
    const response = await getProvinceMap(qp)
    mapChartData.value = response.result || []
  } catch (error) {
    mapChartData.value = []
    console.log(error)
  } finally {
    loading.provinceMap = false
  }
}

// 请求：经销商 TOP
const fetchDealerRankTop = async (params?: VocQueryParams) => {
  try {
    loading.dealerRankTop = true
    const qp: VocQueryParams = {
      ...(props.queryParams || {}),
      ...(params || {}),
      dataType: rankInnerMode.value
    }
    const response = await getDealerRankTop(qp)
    dealerRankTopData.value = response?.result || []
  } catch (error) {
    dealerRankTopData.value = []
    console.log(error)
  } finally {
    loading.dealerRankTop = false
  }
}

// 请求：观点评价 TOP
const fetchEvaluateTop = async (params?: VocQueryParams) => {
  // 显式传参优先；无参时复用最近一次请求上下文，保证切换情感筛选不丢联动条件
  const contextParams = params !== undefined ? params || null : evaluateTopRequestParams.value
  const requestId = ++evaluateTopRequestId.value
  try {
    loading.evaluateTop = true
    const qp = buildEvaluateTopQueryParams(contextParams)
    const response = await getOpinionEvaluateTop(qp)
    // 仅处理最新请求，避免旧响应覆盖当前词云数据
    if (requestId !== evaluateTopRequestId.value) return
    evaluateTopRequestParams.value = { ...qp }
    if (response.success && response.result) {
      wordCloudData.value = (response.result as unknown as any[])?.map((item: any) => ({
        name: item.opinion,
        value: item.totalMentions,
        sentiment: item.sentiment
      }))
    } else {
      wordCloudData.value = []
    }
  } catch (error) {
    if (requestId !== evaluateTopRequestId.value) return
    evaluateTopRequestParams.value = buildEvaluateTopQueryParams(contextParams)
    console.error('获取评价TOP失败:', error)
    wordCloudData.value = []
  } finally {
    if (requestId === evaluateTopRequestId.value) loading.evaluateTop = false
  }
}

const scrollToTop = () => {
  anchorRef.value?.scrollIntoView({
    behavior: 'smooth',
    block: 'start'
  })
}

/**
 * 列表分页大小改变
 */
const handleSizeChange = async (size: number) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  await fetchProvinceList()
  scrollToTop()
}

/**
 * 列表当前页改变
 */
const handleCurrentChange = async (page: number) => {
  pagination.pageNum = page
  await fetchProvinceList()
  scrollToTop()
}

/**
 * 将表格排序方向转换为后端约定值。
 * 当用户清空排序时，返回空值以移除排序参数。
 */
const normalizeSortOrder = (order: SortChangeEvent['sortOrder']) => {
  if (order === 'ascending') return 'asc' as const
  if (order === 'descending') return 'desc' as const
  return undefined
}

/**
 * 处理区域/经销商列表排序变化，改为由后端完成排序。
 * 排序切换后统一回到第一页并重新请求。
 */
const handleTableSortChange = async (
  sortField: SortChangeEvent['sortBy'],
  sortOrder: SortChangeEvent['sortOrder']
) => {
  const normalizedSortOrder = normalizeSortOrder(sortOrder)
  if (!sortField || !normalizedSortOrder) {
    tableSort.sortField = undefined
    tableSort.sortOrder = undefined
  } else {
    tableSort.sortField = sortField
    tableSort.sortOrder = normalizedSortOrder
  }

  pagination.pageNum = 1
  await fetchProvinceList()
  scrollToTop()
}

// 请求：省份/经销商列表（支持分页）
const fetchProvinceList = async (params?: VocQueryParams) => {
  // 有省份时固定展示经销商；返回全国时恢复为区域列表（避免请求/渲染过大数据）
  const prevMode = listInnerMode.value
  syncListModeByProvince()
  // 模式被自动切换时，列表数据维度变化，强制回到第一页
  if (listInnerMode.value !== prevMode) pagination.pageNum = 1

  // 记录额外参数：仅在显式传参时覆盖，避免分页切换丢上下文
  if (params !== undefined) provinceListExtraParams.value = params || null
  try {
    loading.provinceList = true
    const qp: VocQueryParams = {
      ...(provinceListExtraParams.value || {}),
      ...(props.queryParams || {}),
      dataType: listInnerMode.value,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...(tableSort.sortField && tableSort.sortOrder
        ? {
            sortField: tableSort.sortField,
            sortOrder: tableSort.sortOrder
          }
        : {})
    }
    const response = await getProvinceList(qp)

    const result: any = response?.result
    const list = (result?.list || []) as ProvinceListItem[]
    const total = Number(result?.total || 0)
    pagination.total = Number.isFinite(total) ? total : 0
    provinceTableData.value = list
  } catch (error) {
    provinceTableData.value = []
    pagination.total = 0
    console.log(error)
  } finally {
    loading.provinceList = false
  }
}

// 重置并加载第一页（与 PopulationAnalysis 的“用户列表”逻辑对齐）
const resetAndLoadFirstProvincePage = async (params?: VocQueryParams) => {
  pagination.pageNum = 1
  provinceTableData.value = []
  pagination.total = 0
  await fetchProvinceList(params)
}

const refresh = async (params?: VocQueryParams) => {
  // refresh 里先同步列表模式，确保 dataType 正确，避免回退时误请求全国经销商列表
  syncListModeByProvince()
  await Promise.all([
    fetchProvinceMap(params),
    fetchDealerRankTop(params),
    fetchEvaluateTop(params),
    resetAndLoadFirstProvincePage(params)
  ])
}

defineExpose({ refresh })

watch(
  () => props.queryParams,
  () => {
    // 外部查询条件变化时清理缓存，避免跨层级沿用旧上下文
    evaluateTopRequestParams.value = null
    // 从省份下钻返回时，清掉上一次点击的单元格，避免 switch / 排序继续带入旧上下文
    if (!props.queryParams?.provinceCodeSet?.length) curCell.value = undefined
    refresh()
  },
  { deep: true, immediate: true, flush: 'post' }
)

const handleCellClick = (row: any) => {
  // if (row.code === curCell.value?.code) {
  //   ElMessage.warning('已到最末级')
  //   return
  // }
  curCell.value = row
  let _params = {}
  if (listInnerMode.value === 'dealer') {
    //经销商
    _params = { dealerCode: row.code }
  } else {
    _params = {
      provinceCodeSet: row.code ? [row.code] : undefined
    }
  }
  // 记录来源：地域
  ddStore.updateDDViewParams({ lastDrillFrom: DrillTabKey.GEOGRAPHIC })
  ddStore.drillDown(_params, [{ text: row.name || row.code, value: _params }])
}

const handleWordCloudClick = (item: any) => {
  if (currentSelectedTopic.value === item.name) return
  currentSelectedTopic.value = item.name
  const drillParams: VocQueryParams = {
    ...(evaluateTopRequestParams.value || buildEvaluateTopQueryParams(null)),
    topic: item.name
  }
  // 记录来源：观点（来源于地域页的词云点击）
  ddStore.updateDDViewParams({ lastDrillFrom: DrillTabKey.GEOGRAPHIC })
  ddStore.drillDown(drillParams, [{ text: item.name, value: { topic: item.name } }])
}
</script>

<template>
  <div class="geographic-analysis h-full flex flex-col overflow-y-auto">
    <div class="geographic-analysis__top-panels border-bottom">
      <div class="geographic-analysis__top-panels-inner">
        <div
          class="geographic-analysis__panel geographic-analysis__panel--map"
          v-loading="loading.provinceMap"
        >
          <CommonTitle title="区域分布">
            <template #right>
              <SwitchButton
                v-model="mapInnerMode"
                :options="MENTION_NEGATIVE_RATE_SWITCH_OPTIONS"
                @change="mapModeChange"
              />
            </template>
          </CommonTitle>
          <RegionalDistributionMap
            :data="mapChartData"
            :data-type="mapInnerMode"
            @provinceClick="handleProvinceClick"
            height="340px"
          />
        </div>
        <div
          class="geographic-analysis__panel geographic-analysis__panel--dealer"
          v-loading="loading.dealerRankTop"
        >
          <CommonTitle title="经销商评价TOP">
            <template #right>
              <SwitchButton
                v-model="rankInnerMode"
                :options="MENTION_NEGATIVE_RATE_SWITCH_OPTIONS"
                @change="rankModeChange"
              />
            </template>
          </CommonTitle>
          <TopDealerReviews
            ref="topDealerReviewsRef"
            :dealer-rank-top-data="dealerRankTopData"
            :data-type="rankInnerMode"
            @row-click="handleRankClick"
            @sort-change="handleRankSortChange"
            class="mt-20"
          />
        </div>
        <div
          class="geographic-analysis__panel geographic-analysis__panel--opinion"
          v-loading="loading.evaluateTop"
        >
          <CommonTitle title="观点评价TOP">
            <template #left>
              <el-select
                v-model="wordCloudValue"
                placeholder=""
                :empty-values="[null, undefined]"
                @change="fetchEvaluateTop()"
                style="width: 108px"
              >
                <el-option
                  v-for="item in wordCloudOptions"
                  :key="item.value"
                  :label="item.text"
                  :value="item.value"
                />
              </el-select>
            </template>
          </CommonTitle>
          <div style="height: 360px">
            <WordCloudChart
              class="flex-y-center"
              :data="wordCloudData"
              :color-palette="WORD_CLOUD_RANDOM_COLOR_PALETTE"
              :dim-opacity="0.8"
              :highlight-top-count="10"
              ellipse
              random-palette
              @wordClick="handleWordCloudClick"
            />
          </div>
        </div>
      </div>
    </div>
    <!-- 锚点：分页切换后回到列表顶部 -->
    <div ref="anchorRef"></div>
    <CommonTitle :title="`${currentSwitcher.label || ''}列表`" class="mt-19 mb-16">
      <template #right>
        <SwitchButton
          v-if="showSwitcher"
          v-model="listInnerMode"
          :options="listOptions"
          @change="listModeChange"
        />
      </template>
    </CommonTitle>
    <DrillDownTable
      :data="provinceTableData"
      :columns="tableHeaders"
      header-height="80px"
      height="100%"
      :stripe="false"
      :show-sort-icon="true"
      :loading="loading.provinceList"
      class="csa__detail-table"
      @cell-click="handleCellClick"
      @sort-change="handleTableSortChange"
    />

    <template v-if="pagination.total > 0">
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="->,total, prev, pager, next, sizes"
        class="pt-16"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </template>
  </div>
</template>

<style scoped lang="scss">
.geographic-analysis {
  width: 100%;
  min-height: 420px;
}

.geographic-analysis__top-panels {
  flex: 0 0 auto;
  width: 100%;
  min-height: 408px;
  overflow-x: auto;
  overflow-y: hidden;
  padding-bottom: 8px;
}

.geographic-analysis__top-panels-inner {
  display: flex;
  min-width: 1200px;
  width: max(100%, 1200px);
  align-items: stretch;
}

.geographic-analysis__panel {
  box-sizing: border-box;
  min-height: 400px;

  &:not(:last-child) {
    border-right: 1px solid $border-regular;
  }
}

.geographic-analysis__panel--map {
  flex: 1 0 360px;
  padding-right: 36px;
}

.geographic-analysis__panel--dealer {
  flex: 1 0 480px;
  padding: 0 36px;
}

.geographic-analysis__panel--opinion {
  flex: 1 0 360px;
  padding-left: 36px;
  padding-right: 36px;
}

.border-bottom {
  border-bottom: 1px solid $border-regular;
}
</style>
