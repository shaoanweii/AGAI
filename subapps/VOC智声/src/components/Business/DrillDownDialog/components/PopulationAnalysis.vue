<script setup lang="ts">
// 人群特征（已切换为无限下钻）
import GenderDistributionChart from '@/components/Business/DrillDownDialog/components/GenderDistributionChart/index.vue'
import CommonTitle from '@components/Business/DrillDownDialog/components/CommonTitle'
import HorizontalSegmentedBar from '@components/Business/DrillDownDialog/components/HorizontalSegmentedBar/index.vue'
import { TriangleRankList, RadialLabelRing } from '@components/Business/DrillDownDialog/components'
import { ref, watch, onMounted, reactive, computed } from 'vue'
import DrillDownTable, {
  type SortChangeEvent,
  type TableHeaderGroup
} from '@components/Business/DrillDownDialog/components/DrillDownTable'
import UserDetails from '@components/Business/DrillDownDialog/components/UserDetails.vue'
import { DrillTabKey } from '../constants'
import useGeneralDrillDownStore from '@store/modules/generalDrillDown'
import { useRoute } from 'vue-router'
import type { EChartsOption } from 'echarts'
import type { UserTypeDistributionVo } from '@api/drillDownDialog/types.d.ts'
import {
  getAgeDistribution,
  getGenderDistribution,
  getRegionDistribution,
  getUserFocusSceneTop,
  getUserList,
  getUserTypeDistribution
} from '@api/drillDownDialog'
import { CHART_THEME_COLORS } from '@/constants'
import { fmtNum, fmtPer } from '@/utils'

// 下钻 store
const ddStore = useGeneralDrillDownStore()
const route = useRoute()
const isRootCause = computed(() => route.name === 'rootCause')

// 接收父级透传的查询参数
const props = defineProps<{ queryParams?: VocQueryParams; filters?: any[] }>()

// loading 状态
const loading = reactive({
  gender: false,
  age: false,
  userType: false,
  region: false,
  focusScene: false,
  userList: false
})

// 数据
const genderData = ref<any>([])
const ageDistributionData = ref<any>([])
const userTypeItems = ref<PopulationUserTypeItem[]>([])
const focusSceneItems = ref<{ label: string }[]>([])
const userListData = ref<any[]>([])
// 用户列表表格实例
const anchorRef = ref<any>()

const userDetailsDialogVisible = ref(false)
const userDetailsPayload = ref<{
  userId?: string | number
  queryParams?: Record<string, any>
} | null>(null)

const ddParams = ref<any>({})

// 用户列表滚动分页状态
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  hasMore: true,
  loadingMore: false
})

type PopulationSortOrder = 'asc' | 'desc'

interface PopulationTableSortState {
  sortField?: string
  sortOrder?: PopulationSortOrder
}

// 当前列表排序状态：仅在用户点击表头后透传给后端
const tableSort = reactive<PopulationTableSortState>({})

const USER_TYPE_BAR_WIDTH = 20

interface PopulationUserTypeItem extends UserTypeDistributionVo {
  label: string
  color: string
}

interface UserTypeChartItem extends PopulationUserTypeItem {
  percentValue: number
  rawData: PopulationUserTypeItem
}

interface ResidentProvinceItem {
  label: string
  value: number
  percent?: number
  displayText?: string
  color?: string
  provinceCode?: string
  provinceName?: string
}

const residentProvinceItems = ref<ResidentProvinceItem[]>([])

/**
 * 将占比字段统一转换为图表可消费的数值，兼容空值和字符串场景。
 * @param value 原始占比值
 * @returns 图表计算使用的数值
 */
const normalizePercentValue = (value: number | string | null | undefined) => {
  const parsedValue = Number(value)
  return Number.isFinite(parsedValue) ? parsedValue : 0
}

/**
 * 将客户类型标签按固定字符数换行，避免中间栏空间受限时标签重叠。
 * @param label 客户类型名称
 * @returns 适合图表横轴展示的标签文本
 */
const formatUserTypeAxisLabel = (label: string) => {
  if (!label) return ''
  const chunks = label.match(/.{1,4}/g)
  return chunks?.join('\n') || label
}

/**
 * 按后端返回顺序构建客户类型图表数据，避免前端重排影响业务展示顺序。
 */
const userTypeChartData = computed<UserTypeChartItem[]>(() => {
  return userTypeItems.value.map(item => ({
    ...item,
    percentValue: normalizePercentValue(item.percent),
    rawData: item
  }))
})

//是否显示人群特征
const isViewMore = computed(() => {
  const { ageCode, gender, custType, custProvinceCodeSet } = props.queryParams || {}
  return !(ageCode?.length || gender || custType || custProvinceCodeSet?.length)
})

const userTypeAxisLabelRich = computed<Record<string, Record<string, string | number>>>(() => {
  return userTypeChartData.value.reduce<Record<string, Record<string, string | number>>>(
    (result, item, index) => {
      result[`label${index}`] = {
        color: item.color,
        fontSize: 13,
        fontWeight: 500,
        lineHeight: 18,
        align: 'center',
        width: 68
      }
      return result
    },
    {}
  )
})

const USER_TYPE_CHART_TOP_PADDING_RATIO = 0.1

/**
 * 根据当前最大占比动态计算纵轴上限，让柱体尽量撑满绘图区，同时为顶部标签预留空隙。
 */
const userTypeChartYAxisMax = computed(() => {
  const maxPercentValue = userTypeChartData.value.reduce((currentMax, item) => {
    return Math.max(currentMax, item.percentValue)
  }, 0)

  if (maxPercentValue <= 0) {
    return 1
  }

  return Number((maxPercentValue * (1 + USER_TYPE_CHART_TOP_PADDING_RATIO)).toFixed(2))
})

const userTypeChartOptions = computed<EChartsOption>(() => {
  if (!userTypeChartData.value.length) {
    return {}
  }

  return {
    animationDuration: 700,
    animationEasing: 'cubicOut',
    grid: {
      left: 0,
      right: 0,
      top: 34,
      bottom: 15,
      containLabel: true
    },
    tooltip: {
      show: false,
      trigger: 'none'
    },
    xAxis: {
      type: 'category',
      data: userTypeChartData.value.map(item => item.userType),
      axisTick: {
        show: false
      },
      axisLine: {
        lineStyle: {
          color: '#D9DEE7',
          width: 1
        }
      },
      axisLabel: {
        interval: 0,
        margin: 14,
        formatter: (value: string, index: number) => {
          return `{label${index}|${formatUserTypeAxisLabel(value)}}`
        },
        rich: userTypeAxisLabelRich.value
      }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: userTypeChartYAxisMax.value,
      show: false
    },
    series: [
      {
        name: '占比',
        type: 'bar',
        barWidth: USER_TYPE_BAR_WIDTH,
        data: userTypeChartData.value.map(item => ({
          value: item.percentValue,
          rawData: item.rawData
        })),
        itemStyle: {
          color: (params: any) =>
            userTypeChartData.value[params.dataIndex]?.color || CHART_THEME_COLORS[0]
        }
      },
      {
        name: '标签层',
        type: 'bar',
        silent: true,
        barGap: '-100%',
        barWidth: USER_TYPE_BAR_WIDTH,
        data: userTypeChartData.value.map(item => item.percentValue),
        itemStyle: {
          color: 'transparent'
        },
        tooltip: {
          show: false
        },
        label: {
          show: true,
          position: 'top',
          distance: 10,
          color: '#26292E',
          fontSize: 14,
          fontWeight: 600,
          formatter: (params: any) => {
            return fmtNum(userTypeChartData.value[params.dataIndex]?.value ?? 0)
          }
        }
      }
    ]
  }
})

// 列配置（与数据字段严格对齐）
const tableHeaders: TableHeaderGroup[] = [
  {
    key: 'userName',
    label: '用户',
    backgroundColor: '#EAF3FF',
    columnPadding: '0 12px 0 8px',
    width: '250px',
    tooltip: { show: true }
  },
  { key: 'dataSource', label: '数据源', sortable: 'custom' },
  {
    key: 'postCount',
    label: '发帖数',
    render: (row: any) => fmtNum(row.postCount),
    sortable: 'custom'
  },
  { key: 'value', label: '提及量', render: (row: any) => fmtNum(row.value), sortable: 'custom' },
  {
    key: 'complainCount',
    label: '抱怨',
    backgroundColor: ['#FEF0E5', '#EAF3FF'],
    render: (row: any) => fmtNum(row.complainCount),
    sortable: 'custom'
  },
  {
    key: 'consultCount',
    label: '咨询',
    backgroundColor: ['#E5FEFA', '#EAF3FF'],
    render: (row: any) => fmtNum(row.consultCount),
    sortable: 'custom'
  },
  {
    key: 'suggestCount',
    label: '建议',
    backgroundColor: ['#E5FAFE', '#EAF3FF'],
    render: (row: any) => fmtNum(row.suggestCount),
    sortable: 'custom'
  },
  {
    key: 'praiseCount',
    label: '表扬',
    backgroundColor: ['#E5FEEB', '#EAF3FF'],
    render: (row: any) => fmtNum(row.praiseCount),
    sortable: 'custom'
  },
  {
    key: 'negativeRate',
    label: '负面率',
    render: (row: any) =>
      `<div style="background-color: ${row.rateBackgroundColor}; color: ${row.rateColor}">${fmtPer(row.negativeRate)}</div>`,
    sortable: 'custom'
  },
  { key: 'Operations', label: '操作' }
]

// 请求
async function fetchGender(params?: VocQueryParams) {
  try {
    loading.gender = true
    const qp: VocQueryParams = {
      ...(props.queryParams || {}),
      ...(params || {}),
      ...ddParams.value
    }
    const response = await getGenderDistribution(qp)
    //女性排在男性前面
    genderData.value = response?.result || []
  } catch (e) {
    console.error('获取性别分布失败:', e)
    genderData.value = []
  } finally {
    loading.gender = false
  }
}

async function fetchAge(params?: VocQueryParams) {
  try {
    loading.age = true
    const qp: VocQueryParams = {
      ...(props.queryParams || {}),
      ...(params || {}),
      ...ddParams.value
    }
    const response = await getAgeDistribution(qp)
    const arr: any[] = response?.result || []
    ageDistributionData.value = arr.map((it: any, idx: number) => ({
      ...it,
      value: normalizePercentValue(it.percent),
      color: CHART_THEME_COLORS[idx % CHART_THEME_COLORS.length]
    }))
  } catch (e) {
    console.error('获取年龄段分布失败:', e)
    ageDistributionData.value = []
  } finally {
    loading.age = false
  }
}

async function fetchUserType(params?: VocQueryParams) {
  try {
    loading.userType = true
    const qp: VocQueryParams = {
      ...(props.queryParams || {}),
      ...(params || {}),
      ...ddParams.value
    }
    const response = await getUserTypeDistribution(qp)
    const arr: UserTypeDistributionVo[] = response?.result || []
    userTypeItems.value = arr.map((it: any, idx: number) => ({
      ...it,
      label: it.userType,
      color: CHART_THEME_COLORS[idx % CHART_THEME_COLORS.length]
    }))
  } catch (e) {
    console.error('获取用户类型占比失败:', e)
    userTypeItems.value = []
  } finally {
    loading.userType = false
  }
}

async function fetchRegion(params?: VocQueryParams) {
  try {
    loading.region = true
    const qp: VocQueryParams = {
      ...(props.queryParams || {}),
      ...(params || {}),
      ...ddParams.value
    }
    const response = await getRegionDistribution(qp)
    const arr: any[] = response?.result || []
    residentProvinceItems.value = arr.map((it: any, idx: number) => {
      const percentValue = normalizePercentValue(it.percent)

      return {
        label: it.provinceName || '',
        value: it.value,
        percent: percentValue,
        // value 用于三角形高度计算，displayText 用于底部占比文案展示。
        displayText: fmtPer(percentValue),
        color: CHART_THEME_COLORS[idx % CHART_THEME_COLORS.length],
        provinceCode: it.provinceCode,
        provinceName: it.provinceName
      }
    })
  } catch (e) {
    console.error('获取常住地省份占比失败:', e)
    residentProvinceItems.value = []
  } finally {
    loading.region = false
  }
}

async function fetchFocusScene(params?: VocQueryParams) {
  try {
    loading.focusScene = true
    const qp: VocQueryParams = {
      ...(props.queryParams || {}),
      ...(params || {}),
      ...ddParams.value
    }
    const response = await getUserFocusSceneTop(qp)
    const arr: any[] = response?.result || []
    focusSceneItems.value = arr.map((it: any) => ({ label: String(it.sceneName ?? '') }))
  } catch (e) {
    console.error('获取用户关注场景TOP失败:', e)
    focusSceneItems.value = []
  } finally {
    loading.focusScene = false
  }
}

// 分页加载用户列表（支持追加）
async function fetchUserListPage(params?: VocQueryParams, append = false) {
  try {
    if (append) {
      pagination.loadingMore = true
    } else {
      loading.userList = true
    }
    const qp: VocQueryParams = {
      ...(props.queryParams || {}),
      ...(params || {}),
      ...ddParams.value,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...(tableSort.sortField && tableSort.sortOrder
        ? {
            sortField: tableSort.sortField,
            sortOrder: tableSort.sortOrder
          }
        : {})
    }
    const response = await getUserList(qp)
    const list = response?.result?.list || []
    pagination.total = response?.result?.total || 0
    if (append) {
      userListData.value = userListData.value.concat(list)
    } else {
      userListData.value = list
    }
    // 小于每页大小则认为没有更多数据
    pagination.hasMore = list.length >= pagination.pageSize
  } catch (e) {
    console.error('获取用户列表失败:', e)
    if (!append) userListData.value = []
  } finally {
    if (append) {
      pagination.loadingMore = false
    } else {
      loading.userList = false
    }
  }
}

// 重置并加载第一页
async function resetAndLoadFirstPage(params?: VocQueryParams) {
  pagination.pageNum = 1
  pagination.hasMore = true
  userListData.value = []
  await fetchUserListPage(params, false)
}

// 触底加载下一页
const handleReachBottom = async () => {
  if (!pagination.hasMore || pagination.loadingMore || loading.userList) return
  pagination.pageNum += 1
  await fetchUserListPage(undefined, true)
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
 * 处理用户列表排序变化，改为由后端完成排序。
 * 排序切换后统一回到第一页并重新请求首屏数据。
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
  pagination.hasMore = true
  await fetchUserListPage(undefined, false)
  scrollToTop()
}

/**
 * 分页大小改变
 */
const handleSizeChange = async (size: number) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  await fetchUserListPage()
  scrollToTop()
}

/**
 * 当前页改变
 */
const handleCurrentChange = async (page: number) => {
  pagination.pageNum = page
  await fetchUserListPage()
  scrollToTop()
}

const scrollToTop = () => {
  anchorRef.value?.scrollIntoView({
    behavior: 'smooth',
    block: 'start'
  })
}

// 统一刷新：按需传参
const refresh = (params?: VocQueryParams) => {
  fetchGender(params)
  fetchAge(params)
  fetchUserType(params)
  fetchRegion(params)
  fetchFocusScene(params)
  resetAndLoadFirstPage(params)
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

// 性别点击事件
const handleChartClick = (params: any) => {
  const gender = params?.data?.gender
  // 记录来源：人群
  ddStore.updateDDViewParams({ lastDrillFrom: DrillTabKey.POPULATION })
  ddStore.drillDown({ gender }, [{ text: String(gender ?? ''), value: { gender } }])
}
const handleAgeClick = (params: any) => {
  console.log('params', params)

  const age = params?.data?.data?.age
  const title = params?.data?.data?.title
  ddStore.updateDDViewParams({ lastDrillFrom: DrillTabKey.POPULATION })
  // 记录来源：人群  // 记录来源：人群
  ddStore.drillDown({ ageCode: age ? [age] : undefined }, [
    {
      text: String(title ?? ''),
      value: { ageCode: age ? [age] : undefined }
    }
  ])
}
const handleCustomerTypeClick = (params: any) => {
  ddStore.updateDDViewParams({ lastDrillFrom: DrillTabKey.POPULATION })
  const custType = params?.userType
  // 记录来源：人群
  ddStore.drillDown({ custType }, [{ text: String(custType ?? ''), value: { custType } }])
}

/**
 * 处理客户类型图表点击，统一将不同图层映射回原始业务数据。
 * @param params ECharts 点击事件参数
 */
const handleUserTypeChartClick = (params: any) => {
  if (params?.componentType !== 'series') return

  const dataIndex = Number(params?.dataIndex)
  if (!Number.isInteger(dataIndex) || dataIndex < 0) return

  const target = params?.data?.rawData || userTypeChartData.value[dataIndex]?.rawData
  if (target) {
    handleCustomerTypeClick(target)
  }
}

const handleResidenceClick = (params: any) => {
  ddStore.updateDDViewParams({ lastDrillFrom: DrillTabKey.POPULATION })
  const custProvinceCodeSet = params?.provinceCode ? [params.provinceCode] : undefined
  ddStore.drillDown({ custProvinceCodeSet }, [
    { text: params?.provinceName, value: { custProvinceCodeSet } }
  ])
}

const handleFocusScene = (sceneData: any) => {
  ddStore.updateDDViewParams({
    lastDrillFrom: DrillTabKey.POPULATION,
    drillScene: DrillTabKey.SCENARIO
  })
  const sceneName = sceneData?.label
  const params = { usageScenarioSecond: sceneName }
  // 记录来源：人群({ lastDrillFrom: DrillTabKey.POPULATION })
  ddStore.drillDown(params, [{ text: String(sceneName ?? ''), value: params }])
}

const handleViewDetail = (user: any) => {
  if (!isRootCause.value) {
    // 非 rootCause：保持原有“替换内容区为用户详情”的行为
    ddStore.showUserDetail({ userId: user.userId, queryParams: { ...(props.queryParams || {}) } })
    return
  }
  userDetailsPayload.value = { userId: user.userId, queryParams: { ...(props.queryParams || {}) } }
  userDetailsDialogVisible.value = true
}
</script>

<template>
  <div class="h-full flex flex-col overflow-y-auto pb-2">
    <div v-if="isViewMore" class="flex w-full border-bottom pb-24 mb-19">
      <div class="flex-1 pr-36 border-right">
        <CommonTitle title="性别" />
        <div v-loading="loading.gender">
          <GenderDistributionChart
            :data="genderData"
            width="400px"
            height="200px"
            @chart-click="handleChartClick"
          />
        </div>
        <CommonTitle title="年龄" class="mb-16" />
        <div v-loading="loading.age">
          <HorizontalSegmentedBar
            :data="ageDistributionData"
            :showLegend="true"
            title="年龄段分布"
            height="24px"
            @chart-click="handleAgeClick"
          />
        </div>
      </div>

      <div class="flex-1 pl-36 pr-36 border-right">
        <CommonTitle title="客户类型" />
        <div v-loading="loading.userType" class="population-user-type-section">
          <div v-if="userTypeChartData.length" class="population-user-type-panel">
            <FEcharts
              class="population-user-type-chart"
              :options="userTypeChartOptions"
              width="100%"
              height="100%"
              @chart-click="handleUserTypeChartClick"
            />
          </div>
          <div v-else class="population-user-type-empty">
            <el-empty description="暂无数据" :image-size="80" />
          </div>
        </div>
        <CommonTitle title="常驻地" class="mt-21 mb-16" />
        <div v-loading="loading.region">
          <TriangleRankList :items="residentProvinceItems" @item-click="handleResidenceClick" />
        </div>
      </div>

      <div class="flex-1 pl-36 pr-36 border-right">
        <CommonTitle title="关注场景" class="mb-16" />
        <div style="height: 300px" v-loading="loading.focusScene">
          <RadialLabelRing
            :items="focusSceneItems"
            :radius="145"
            :badge-size="84"
            @label-click="handleFocusScene"
          />
        </div>
      </div>
    </div>
    <!--    锚点的位置-->
    <div ref="anchorRef"></div>
    <CommonTitle title="用户列表" class="mb-16" />
    <DrillDownTable
      :data="userListData"
      :columns="tableHeaders"
      headerHeight="56px"
      height="100%"
      :stripe="false"
      :show-sort-icon="true"
      :loading="loading.userList"
      class="csa__detail-table"
      @sort-change="handleTableSortChange"
    >
      <template #userName="{ row }">
        <el-tooltip
          :content="row.userName || ''"
          placement="top"
          :disabled="!row.userName"
          popper-class="text-tooltip-light"
        >
          <div class="user-name-cell">{{ row.userName || '' }}</div>
        </el-tooltip>
      </template>
      <template #operations="{ row }">
        <el-button type="primary" size="small" link @click="handleViewDetail(row)">详情</el-button>
      </template>
    </DrillDownTable>

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

    <el-dialog
      v-if="isRootCause && userDetailsDialogVisible"
      v-model="userDetailsDialogVisible"
      destroy-on-close
      :show-close="false"
      align-center
      width="95%"
      append-to=".layout__main"
      style="padding: 0; border-radius: 8px; height: 96%"
      header-class="user-details-dialog-header"
      body-class="user-details-dialog-body"
    >
      <UserDetails
        :payload="userDetailsPayload"
        close-behavior="emit"
        @close="userDetailsDialogVisible = false"
      />
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.border-right {
  border-right: 1px solid $border-regular;
}

.border-bottom {
  border-bottom: 1px solid $border-regular;
}

.population-user-type-section {
  min-height: 220px;
}

.population-user-type-panel {
  height: 220px;
  padding: 10px 12px 0;
}

.population-user-type-chart {
  width: 100%;
  height: 100%;

  :deep(canvas) {
    cursor: pointer;
  }
}

.population-user-type-empty {
  min-height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.user-name-cell) {
  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-align: left;
  padding: 0 12px;
}
</style>
