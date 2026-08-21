<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { DEFAULT_MENTION_NEGATIVE_RATE_TYPE } from '@/constants'
import { DrillTabKey } from '../constants'
import { fmtNum, fmtPer, fmtFix } from '@/utils'
import type { SortChangeEvent, TableHeaderGroup } from './DrillDownTable/types'
import CarSeriesRankingBar, { type RankItem } from './CarSeriesRankingBar'
import CommonTitle from './CommonTitle'
import DrillDownTable from './DrillDownTable'
import type { IndicatorListVo, IndicatorRankVo } from '@api/drillDownDialog/types.d.ts'
import { getIndicatorList, getIndicatorRank } from '@api/drillDownDialog'
import useGeneralDrillDownStore from '@/store/modules/generalDrillDown'

// 定义组件名称
defineOptions({
  name: 'IndicatorAnalysis'
})

// 接收父级透传的查询参数与筛选条件
const props = defineProps<{ queryParams?: VocQueryParams; filters?: any[] }>()

const rankMode = ref<MentionNegativeRateType>(DEFAULT_MENTION_NEGATIVE_RATE_TYPE)
const ddStore = useGeneralDrillDownStore()
// 指标排行数据
const rankData = ref<IndicatorRankVo[]>([])
// 指标列表数据
const tableData = ref<IndicatorListVo[]>([])
// 加载状态
const loading = ref(false)
const rankLoading = ref(false)

const ddParams = ref()

// 表格锚点（分页切换后滚动到表格顶部）
const anchorRef = ref<any>()

// 列表分页状态
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

type IndicatorSortOrder = 'asc' | 'desc'

interface IndicatorTableSortState {
  sortField?: string
  sortOrder?: IndicatorSortOrder
}

// 当前列表排序状态：仅在用户点击表头后透传给后端
const tableSort = reactive<IndicatorTableSortState>({})

// 转换rankData为CarSeriesRankingBar所需的RankItem[]类型
const rankItems = computed<RankItem[]>(() => {
  return rankData.value.map(item => ({
    name: item.tagName,
    value: item.value,
    percent: item.value,
    mom: item.valueMom,
    yoy: item.valueYoy,
    tagCode: item.tagCode,
    tagLevel: item.tagLevel
  }))
})

// 表格配置
const tableHeaders: TableHeaderGroup[] = [
  {
    key: 'tagName',
    label: '体验点',
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
      {
        key: 'mentionsShare',
        label: '占比',
        render: (row: IndicatorListVo) => fmtPer(row.mentionsShare)
      },
      { key: 'mentionTrend', label: '趋势', width: '160px', render: 'trend' },
      {
        key: 'mentionsMoM',
        label: '环比',
        render: (row: IndicatorListVo) => fmtFix(row.mentionsMoM),
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
        render: (row: IndicatorListVo) =>
          `<div style="background-color: ${row.rateBackgroundColor}; color: ${row.rateColor}">${fmtPer(row.negativeRateValue)}</div>`,
        sortable: 'custom'
      },
      { key: 'negativeTrend', label: '趋势', width: '160px', render: 'trend' },
      {
        key: 'negativeRateMoM',
        label: '环比',
        render: (row: IndicatorListVo) => fmtFix(row.negativeRateMoM),
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
        render: (row: IndicatorListVo) => fmtPer(row.positiveRateValue),
        sortable: 'custom'
      },
      { key: 'positiveTrend', label: '趋势', width: '160px', render: 'trend' },
      {
        key: 'positiveRateMoM',
        label: '环比',
        render: (row: IndicatorListVo) => fmtFix(row.positiveRateMoM),
        sortable: 'custom'
      }
    ]
  }
]

//切换模式
const rankModeChange = (mode: MentionNegativeRateType) => {
  rankMode.value = mode
  fetchIndicatorRank(ddParams.value)
}

/**
 * 获取指标排行数据
 */
const fetchIndicatorRank = async (params?: VocQueryParams) => {
  try {
    rankLoading.value = true
    const qp: VocQueryParams = {
      dataType: rankMode.value,
      ...(props.queryParams || {}),
      ...(params || {})
    }
    const response = await getIndicatorRank(qp)
    if (response.success && response.result) {
      rankData.value = response.result
    } else {
      rankData.value = []
    }
  } catch (error) {
    console.error('获取指标排行数据失败:', error)
    rankData.value = []
  } finally {
    rankLoading.value = false
  }
}

/**
 * 获取指标列表数据
 */
const fetchIndicatorList = async (params?: VocQueryParams) => {
  try {
    loading.value = true
    const qp: VocQueryParams = {
      ...(props.queryParams || {}),
      ...(params || {}),
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...(tableSort.sortField && tableSort.sortOrder
        ? {
            sortField: tableSort.sortField,
            sortOrder: tableSort.sortOrder
          }
        : {})
    }
    const response = await getIndicatorList(qp)

    const result: any = response?.result
    // 兼容后端返回：数组 / { list, total } 两种结构
    if (response.success && result) {
      if (Array.isArray(result)) {
        tableData.value = result
        pagination.total = result.length
      } else {
        pagination.total = result?.total || 0
        tableData.value = result?.list || []
      }
    } else {
      pagination.total = 0
      tableData.value = []
    }
  } catch (error) {
    console.error('获取指标列表数据失败:', error)
    pagination.total = 0
    tableData.value = []
  } finally {
    loading.value = false
  }
}

// 重置并加载第一页
async function resetAndLoadFirstPage(params?: VocQueryParams) {
  pagination.pageNum = 1
  pagination.total = 0
  tableData.value = []
  await fetchIndicatorList(params)
}

/**
 * 将表格排序方向转换为后端约定值。
 * 清空排序时返回空值，用于移除排序参数。
 */
const normalizeSortOrder = (order: SortChangeEvent['sortOrder']) => {
  if (order === 'ascending') return 'asc' as const
  if (order === 'descending') return 'desc' as const
  return undefined
}

/**
 * 处理指标列表排序变化，改为由后端完成排序。
 * 排序切换后统一回到第一页并重新拉取列表。
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
  await fetchIndicatorList()
  scrollToTop()
}

/**
 * 初始化数据
 */
const initData = () => {
  fetchIndicatorRank()
  resetAndLoadFirstPage()
}

// 对外暴露refresh方法
const refresh = (params?: VocQueryParams) => {
  fetchIndicatorRank(params)
  resetAndLoadFirstPage(params)
}

defineExpose({ refresh })

onMounted(() => {
  initData()
})

/**
 * 分页大小改变
 */
const handleSizeChange = async (size: number) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  await fetchIndicatorList()
  scrollToTop()
}

/**
 * 当前页改变
 */
const handleCurrentChange = async (page: number) => {
  pagination.pageNum = page
  await fetchIndicatorList()
  scrollToTop()
}

const scrollToTop = () => {
  anchorRef.value?.scrollIntoView({
    behavior: 'smooth',
    block: 'start'
  })
}

// 图表继续下钻
const handleBarClick = (params: any) => {
  console.log('params-->指标排行', params)
  // 记录来源与下一层级：用于在弹框容器中基于“来源 + 目标层级”控制可见的 Tab
  // if (params.tagLevel === 4) {
  //   ElMessage.warning('已到最末级')
  //   return
  // }
  const _params = {
    tag1Code: undefined,
    tag2Code: undefined,
    tag3Code: undefined,
    tag4Code: undefined
  }
  if (params.tagLevel === 1) {
    _params.tag2Code = undefined
    _params.tag1Code = params.tagCode
    _params.tag3Code = undefined
  }
  if (params.tagLevel === 2) {
    _params.tag1Code = undefined
    _params.tag2Code = params.tagCode
    _params.tag3Code = undefined
  }
  if (params.tagLevel === 3) {
    _params.tag1Code = undefined
    _params.tag2Code = undefined
    _params.tag3Code = params.tagCode
  }
  if (params.tagLevel === 4) {
    _params.tag1Code = undefined
    _params.tag2Code = undefined
    _params.tag3Code = undefined
    _params.tag4Code = params.tagCode
  }

  ddParams.value = _params
  // 记录来源：指标
  ddStore.updateDDViewParams({
    lastDrillFrom: DrillTabKey.INDICATOR,
    targetTabHidden: params.tagLevel === 4
  })
  ddStore.drillDown(_params, [{ text: params?.name || '', value: _params }])
}
//点击单元格
const handleCellClick = (row: any, column: any) => {
  console.log('点击了表格行', row, column)
  // if (row.tagLevel === 4) {
  //   ElMessage.warning('已到最末级')
  //   return
  // }
  const _params = {
    tag1Code: undefined,
    tag2Code: undefined,
    tag3Code: undefined,
    tag4Code: undefined
  }
  if (row.tagLevel === 1) {
    _params.tag2Code = undefined
    _params.tag1Code = row.tagCode
    _params.tag3Code = undefined
  }
  if (row.tagLevel === 2) {
    _params.tag1Code = undefined
    _params.tag2Code = row.tagCode
    _params.tag3Code = undefined
  }
  if (row.tagLevel === 3) {
    _params.tag1Code = undefined
    _params.tag2Code = undefined
    _params.tag3Code = row.tagCode
  }
  if (row.tagLevel === 4) {
    _params.tag1Code = undefined
    _params.tag2Code = undefined
    _params.tag3Code = undefined
    _params.tag4Code = row.tagCode
  }

  ddParams.value = _params
  // 记录来源与下一层级：用于在弹框容器中基于“来源 + 目标层级”控制可见的 Tab
  // 约定：当从指标分析下钻到第4级时，需要隐藏“指标分析”Tab
  ddStore.updateDDViewParams({
    lastDrillFrom: DrillTabKey.INDICATOR,
    targetTabHidden: row.tagLevel === 4
  })
  ddStore.drillDown(_params, [{ text: row.tagName, value: _params }])
}

watch(
  () => props.queryParams,
  () => {
    ddParams.value = {}
    initData()
  },
  { deep: true }
)
</script>

<template>
  <div class="csa h-full flex flex-col overflow-y-auto pb-2">
    <!-- 负面率排名图表 -->
    <CarSeriesRankingBar
      title="体验点排行"
      :data="rankItems"
      :mode="rankMode"
      @modeChange="rankModeChange"
      height="300px"
      :loading="rankLoading"
      class="csa__ranking-chart"
      @barClick="handleBarClick"
    />
    <!-- 表格标题区域 -->
    <CommonTitle title="体验点列表" class="mb-16" />
    <!-- 锚点的位置（分页切换后滚动到表格顶部） -->
    <div ref="anchorRef"></div>
    <!-- 指标明细表格 -->
    <DrillDownTable
      :data="tableData"
      :columns="tableHeaders"
      header-height="80px"
      height="100%"
      :stripe="false"
      :show-sort-icon="true"
      :loading="loading"
      class="csa__detail-table"
      @cell-click="handleCellClick"
      @sort-change="handleTableSortChange"
    />
    <!-- 分页 -->
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
.csa {
  width: 100%;

  .csa-card-group {
    gap: 16px;
    margin-bottom: 24px;
  }

  &__empty {
    margin-top: 24px;
    text-align: center;
  }
}

// 卡片样式（遵循项目的半透明白底与阴影风格）
.csa-card {
  width: 324px;
  height: 134px;
  padding: 16px 24px 24px;
  background: $neutral-white;
  box-shadow: 0px 1px 1px 0px rgba(10, 13, 18, 0.05);
  border-radius: $border-radius-xl;
  border: 1px solid $border-regular;
  display: flex;
  flex-direction: column;
  transition: all 0.2s ease-in-out;

  &:hover {
    box-shadow: 0px 4px 12px 0px rgba(10, 13, 18, 0.1);
    transform: translateY(-2px);
  }

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 8px;
  }

  &__brand {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  &__logo {
    width: 28px;
    height: 28px;
    border-radius: $border-radius-m;
  }

  &__name {
    font-weight: $font-weight-semibold;
    font-size: $font-size-h4;
    color: $text-primary;
  }

  &__body {
    display: flex;
    align-items: center;
  }

  &__metric {
    display: flex;
    flex-direction: column;
    flex: 1;
    margin-top: 16px;
  }

  &__metric-label {
    font-weight: $font-weight-normal;
    font-size: $font-size-body;
    color: $text-secondary;
    line-height: $line-height-body;
  }

  &__metric-value {
    font-weight: $font-weight-medium;
    font-size: 20px;
    color: $text-primary;
    margin-top: 8px;
    line-height: 28px;
  }
}

// 工具类：垂直居中布局
.flex-y-center {
  display: flex;
  align-items: center;
}
</style>
