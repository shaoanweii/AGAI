<script setup lang="ts">
import { onMounted, ref, watch, reactive, computed } from 'vue'
import { DEFAULT_MENTION_NEGATIVE_RATE_TYPE } from '@/constants'
import { DrillTabKey } from '../constants'
import { fmtNum, fmtPer, fmtFix } from '@/utils'
import type { SortChangeEvent, TableHeaderGroup } from './DrillDownTable/types'
import type { RateDataItem } from '@/mock'
import { CarSeriesRankingBar } from '@components/Business/DrillDownDialog/components/CarSeriesRankingBar'
import { DrillDownTable } from '@components/Business/DrillDownDialog/components/DrillDownTable'
import { CommonTitle } from '@components/Business/DrillDownDialog/components/CommonTitle'
import { getBrandBrief, getCarSeriesList, getCarSeriesRank } from '@api/drillDownDialog'
import type { CarSeriesListItem } from '@api/drillDownDialog/types.d.ts'
import HoverPopover from '@components/Business/Scene/Common/HoverPopover.vue'
import useGeneralDrillDownStore from '@/store/modules/generalDrillDown'
import { ElMessage } from 'element-plus'

// 定义组件名称
defineOptions({
  name: 'CarSeriesAnalysis'
})

// 接收父级透传的查询参数与筛选条件
const props = defineProps<{ queryParams?: VocQueryParams; filters?: any[] }>()

// 车系基础数据
const items = ref<any>([])
// 负面率排名数据
const rankMode = ref<MentionNegativeRateType>(DEFAULT_MENTION_NEGATIVE_RATE_TYPE)
const rateData = ref<RateDataItem[]>([])
// 车系明细表格数据
const tableData = ref<any>([])

// 车系明细表格实例
const anchorRef = ref<any>()

// 列表分页状态
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

type CarSeriesSortOrder = 'asc' | 'desc'

interface CarSeriesTableSortState {
  sortField?: string
  sortOrder?: CarSeriesSortOrder
}

// 当前列表排序状态：默认不排序，仅在用户点击表头后才透传给后端
const tableSort = reactive<CarSeriesTableSortState>({})

// 当前tab继续下钻的参数
const curTabDDParams = ref<any>({})

//是否显示品牌
const isShowBrand = computed(() => {
  const brandCodeList = props.queryParams?.brandCodeList || []
  return (
    !props.queryParams?.brandCode && !(Array.isArray(brandCodeList) && brandCodeList.length === 1)
  )
})

// 表格配置
const tableHeaders: TableHeaderGroup[] = [
  {
    key: 'brandName',
    label: '品牌',
    width: '160px',
    columnPadding: '0 8px',
    backgroundColor: '#EAF3FF'
  },
  {
    key: 'carSeriesName',
    label: '车系',
    width: '180px',
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
        render: (row: CarSeriesListItem) => fmtPer(row.mentionsShare)
      },
      { key: 'mentionTrend', label: '趋势', width: '160px', render: 'trend' },
      {
        key: 'mentionsMoM',
        label: '环比',
        render: (row: CarSeriesListItem) => fmtPer(row.mentionsMoM),
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
        render: (row: CarSeriesListItem) =>
          `<div style="background-color: ${row.rateBackgroundColor}; color: ${row.rateColor}">${fmtPer(row.negativeRateValue)}</div>`,
        sortable: 'custom'
      },
      { key: 'negativeTrend', label: '趋势', width: '160px', render: 'trend' },
      {
        key: 'negativeRateMoM',
        label: '环比',
        render: (row: CarSeriesListItem) => fmtPer(row.negativeRateMoM),
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
        render: (row: CarSeriesListItem) => fmtPer(row.positiveRateValue),
        sortable: 'custom'
      },
      { key: 'positiveTrend', label: '趋势', width: '160px', render: 'trend' },
      {
        key: 'positiveRateMoM',
        label: '环比',
        render: (row: CarSeriesListItem) => fmtPer(row.positiveRateMoM),
        sortable: 'custom'
      }
    ]
  }
]

//切换模式
const rankModeChange = (mode: MentionNegativeRateType) => {
  rankMode.value = mode
  fetchCarSeriesRank(curTabDDParams.value)
}

// 转换车系排行数据以适配排名图表组件
const convertRankData = (list: any): RateDataItem[] => {
  return list.map((item: any) => ({
    name: item.carSeriesName,
    value: item.mentions,
    percent: item.negativeRate,
    mom: item.mom,
    yoy: item.yoy,
    // highlight: item.negativeRate > 70,
    carSeriesCode: item.carSeriesCode
  }))
}

const loading = reactive({
  brandBrief: false,
  carSeriesRank: false,
  carSeriesList: false
})

const fetchBrandBrief = async (params?: VocQueryParams) => {
  try {
    loading.brandBrief = true
    const qp: VocQueryParams = { ...(props.queryParams || {}), ...(params || {}) }
    const response = await getBrandBrief(qp)
    if (response.success && response.result) {
      items.value = response.result || []
    } else {
      items.value = []
    }
  } catch (error) {
    console.error('获取品牌简报数据失败:', error)
    items.value = []
  } finally {
    loading.brandBrief = false
  }
}

// 获取车系排行数据
const fetchCarSeriesRank = async (params?: VocQueryParams) => {
  try {
    loading.carSeriesRank = true
    const qp: VocQueryParams = {
      dataType: rankMode.value,
      ...(props.queryParams || {}),
      ...(params || {})
    }
    const response = await getCarSeriesRank(qp)
    if (response.success && response.result) {
      rateData.value = convertRankData(response.result || [])
    } else {
      rateData.value = []
    }
  } catch (error) {
    console.error('获取车系排行数据失败:', error)
    rateData.value = []
  } finally {
    loading.carSeriesRank = false
  }
}

// 重置并加载第一页
async function resetAndLoadFirstPage(params?: VocQueryParams) {
  pagination.pageNum = 1
  tableData.value = []
  await fetchCarSeriesList(params)
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
 * 处理车系列表排序变化，改为由后端完成排序。
 * 1. 记录当前排序列与方向
 * 2. 排序切换后回到第一页重新请求
 * 3. 若用户取消排序，则恢复为不传排序参数
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
  await fetchCarSeriesList()
  scrollToTop()
}

// 获取车系列表数据
const fetchCarSeriesList = async (params?: VocQueryParams) => {
  try {
    loading.carSeriesList = true
    const qp: VocQueryParams = {
      ...(props.queryParams || {}),
      ...(params || {}),
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      // 仅在用户主动触发表头排序后，才向后端透传排序字段和方向
      ...(tableSort.sortField && tableSort.sortOrder
        ? {
            sortField: tableSort.sortField,
            sortOrder: tableSort.sortOrder
          }
        : {})
    }
    const response = await getCarSeriesList(qp)
    pagination.total = response?.result?.total || 0
    if (response.success && response.result) {
      tableData.value = response.result.list || []
    } else {
      tableData.value = []
    }
  } catch (error) {
    console.error('获取车系列表数据失败:', error)
    tableData.value = []
  } finally {
    loading.carSeriesList = false
  }
}

// 对外暴露刷新方法
const refresh = (params?: VocQueryParams) => {
  fetchCarSeriesRank(params)
  resetAndLoadFirstPage(params)
  fetchBrandBrief(params)
}

defineExpose({ refresh })

onMounted(() => {
  refresh()
})

watch(
  () => props.queryParams,
  () => {
    refresh()
  },
  { deep: true }
)

// 品牌chang
const ddStore = useGeneralDrillDownStore()

const handleBrandChange = (brand: any) => {
  console.log('品牌:brand', brand)
  curTabDDParams.value.brandCode = brand.code
  // 记录来源：车系
  ddStore.updateDDViewParams({ lastDrillFrom: DrillTabKey.CARSERIES })
  ddStore.drillDown({ brandCode: brand.code }, [
    { text: brand.name, value: { brandCode: brand.code } }
  ])
}

// 车系图表chang
const handleBarClick = (params: any) => {
  console.log('车系排行:params', params)
  if (params.carSeriesCode === curTabDDParams.value.carSeriesCode) {
    ElMessage.warning('已到最末级')
    return
  }
  curTabDDParams.value.carSeriesCode = params.carSeriesCode
  // 记录来源：车系
  ddStore.updateDDViewParams({ lastDrillFrom: DrillTabKey.CARSERIES })
  ddStore.drillDown({ carSeriesCode: params.carSeriesCode }, [
    { text: params.name, value: { carSeriesCode: params.carSeriesCode } }
  ])
}

/**
 * 分页大小改变
 */
const handleSizeChange = async (size: number) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  await fetchCarSeriesList()
  scrollToTop()
}

/**
 * 当前页改变
 */
const handleCurrentChange = async (page: number) => {
  pagination.pageNum = page
  await fetchCarSeriesList()
  scrollToTop()
}

const scrollToTop = () => {
  anchorRef.value?.scrollIntoView({
    behavior: 'smooth',
    block: 'start'
  })
}

//点击单元格
const handleCellClick = (row: any, column: any) => {
  console.log('点击了表格行', row, column)

  if (
    curTabDDParams.value.brandCode === row.brandCode &&
    curTabDDParams.value.carSeriesCode === row.carSeriesCode
  ) {
    ElMessage.warning('已到最末级')
    return
  }
  curTabDDParams.value.brandCode = row.brandCode
  curTabDDParams.value.carSeriesCode = row.carSeriesCode
  // 记录来源：车系
  ddStore.updateDDViewParams({ lastDrillFrom: DrillTabKey.CARSERIES })
  ddStore.drillDown({ brandCode: row.brandCode, carSeriesCode: row.carSeriesCode }, [
    { text: row.brandName, value: { brandCode: row.brandCode } },
    { text: row.carSeriesName, value: { carSeriesCode: row.carSeriesCode } }
  ])
}

/**组装数据*/
const formatHoverData = (item: any) => {
  return [
    {
      label: '负面率',
      value: fmtPer(item.negativeRate),
      rateMoM: fmtFix(item.negativeRateMoM),
      rateYoY: fmtFix(item.negativeRateYoY)
    },
    {
      label: '提及量',
      value: fmtNum(item.mentions),
      rateMoM: fmtFix(item.mentionsMoM),
      rateYoY: fmtFix(item.mentionsYoY)
    }
  ]
}
</script>

<template>
  <div class="csa h-full flex flex-col overflow-y-auto pb-2">
    <div
      v-if="isShowBrand && items && items.length > 0"
      v-loading="loading.brandBrief"
      class="flex-y-center csa-card-group mb-12"
    >
      <template v-for="(item, idx) in items" :key="idx">
        <HoverPopover
          placement="top"
          :show-after="200"
          :width="380"
          trigger="hover"
          :table-config="{
            title: item.name || '',
            data: formatHoverData(item),
            columns: [
              { title: '名称', dataIndex: 'label', width: 70 },
              { title: '数值', dataIndex: 'value', width: 100 },
              { title: '环比', dataIndex: 'rateMoM', className: 'c666' },
              { title: '同比', dataIndex: 'rateYoY', className: 'c666' }
            ]
          }"
        >
          <template #reference>
            <div class="csa-card" @click="handleBrandChange(item)">
              <div class="csa-card__header">
                <div class="csa-card__brand">
                  <img
                    v-if="item.imageUrl"
                    class="csa-card__logo"
                    :src="item.imageUrl"
                    alt="logo"
                  />
                  <span class="csa-card__name">{{ item.name }}</span>
                </div>
              </div>

              <div class="csa-card__body">
                <div class="csa-card__metric">
                  <div class="csa-card__metric-label">负面率</div>
                  <div class="csa-card__metric-value">{{ fmtPer(item.negativeRate) }}</div>
                </div>
                <div class="csa-card__metric">
                  <div class="csa-card__metric-label">提及量</div>
                  <div class="csa-card__metric-value">{{ fmtNum(item.mentions) }}</div>
                </div>
              </div>
            </div>
          </template>
        </HoverPopover>
      </template>
    </div>

    <!-- 负面率排名图表 -->
    <CarSeriesRankingBar
      title="车系排行"
      :data="rateData"
      :mode="rankMode"
      :loading="loading.carSeriesRank"
      @modeChange="rankModeChange"
      height="400px"
      class="csa__ranking-chart"
      @barClick="handleBarClick"
    />
    <!--    锚点的位置-->
    <div ref="anchorRef"></div>
    <!-- 表格标题区域 -->
    <CommonTitle class="mb-16" title="车系列表" />
    <!-- 车系明细表格 -->
    <DrillDownTable
      :data="tableData"
      :columns="tableHeaders"
      header-height="80px"
      height="100%"
      :stripe="false"
      :loading="loading.carSeriesList"
      :show-sort-icon="true"
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
    <!--    <div v-if="!items || items.length === 0" class="csa__empty">
      <el-empty description="暂无数据" />
    </div>-->
  </div>
</template>

<style scoped lang="scss">
.csa {
  width: 100%;
  position: relative;

  &__loading {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(255, 255, 255, 0.8);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
  }

  &__chart-loading,
  &__table-loading {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(255, 255, 255, 0.6);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 100;
  }

  .csa-card-group {
    gap: 16px;
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
