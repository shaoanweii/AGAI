<script setup lang="ts">
// 定义组件名称
import CommonTitle from './CommonTitle'
import DrillDownTable from './DrillDownTable'
import type {
  DrillDownTableInstance,
  SortChangeEvent,
  TableHeaderGroup
} from './DrillDownTable/types'
import { onMounted, ref, watch, reactive } from 'vue'
import { DrillTabKey } from '../constants'
import { getOpinionList } from '@api/drillDownDialog'
import { fmtNum, fmtPer, fmtFix } from '@/utils'
import useUserStore from '@/store/modules/user.ts'
import useGeneralDrillDownStore from '@/store/modules/generalDrillDown'

defineOptions({
  name: 'ViewpointAnalysis'
})

// 接收父级透传的查询参数与筛选条件
const props = defineProps<{ queryParams?: VocQueryParams; filters?: any[] }>()

const userStore = useUserStore()

// 观点分析数据
const opinionListData = ref<any[]>([])
// 观点分析表格实例
const tableRef = ref<DrillDownTableInstance>()
//options
const selectValue = ref<string>('')
// 记录最近一次从 queryParams 带入的 intention，避免覆盖用户在当前 Tab 内的手动筛选
const lastQueryIntention = ref<string>('')
const selectOptions = ref<any[]>([
  { text: '全部意图', value: '' },
  ...userStore.getDictItems('voc_intention')
])
// 观点评价Top数据
const loading = ref(false)

// 列表分页状态
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

type ViewpointSortOrder = 'asc' | 'desc'

interface ViewpointTableSortState {
  sortField?: string
  sortOrder?: ViewpointSortOrder
}

// 当前列表排序状态：仅在用户点击表头后透传给后端
const tableSort = reactive<ViewpointTableSortState>({})

const opinionListHeaders: TableHeaderGroup[] = [
  { key: 'opinion',
    label: '观点', 
    columnPadding: '0 12px 0 8px',
    tooltip: { show: true }
  },
  { key: 'intention', label: '意图', width: '243px' },
  {
    key: 'mentions',
    label: '提及量',
    render: (row: any) => fmtNum(row.mentions),
    width: '243px',
    sortable: 'custom'
  },
  {
    key: 'mentionsShare',
    label: '占比',
    render: (row: any) => fmtPer(row.mentionsShare),
    width: '243px',
    sortable: 'custom'
  },
  { key: 'mentionTrend', label: '趋势', render: 'trend', width: '243px' },
  {
    key: 'mentionsMoM',
    label: '环比',
    render: (row: any) => fmtFix(row.mentionsMoM),
    width: '243px',
    sortable: 'custom'
  }
]

//点击单元格
const ddStore = useGeneralDrillDownStore()

const handleCellClick = (row: any, column: any) => {
  console.log('点击了表格行', row, column)
  // 记录来源：观点
  ddStore.updateDDViewParams({ lastDrillFrom: DrillTabKey.VIEWPOINT })
  ddStore.drillDown({ intention: row.intention, topic: row.opinion }, [
    { text: row.opinion, value: { intention: row.intention, topic: row.opinion } }
  ])
}

// 重置并加载第一页
async function resetAndLoadFirstPage(params?: VocQueryParams) {
  pagination.pageNum = 1
  opinionListData.value = []
  await fetchOpinionListData(params)
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
 * 处理观点列表排序变化，改为由后端完成排序。
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
  await fetchOpinionListData()
  tableRef.value?.scrollToTop()
}

/**
 * 获取观点分析数据
 */
const fetchOpinionListData = async (params?: VocQueryParams) => {
  try {
    loading.value = true
    const queryIntention = ((props.queryParams || {}) as any)?.intention || ''
    const qp: VocQueryParams = {
      ...(props.queryParams || {}),
      // 说明：selectValue 为空代表“全部意图”，此时应保留 queryParams 中的 intention（若有）
      intention: selectValue.value || queryIntention,
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
    const response = await getOpinionList(qp)
    pagination.total = response?.result?.total || 0
    if (response.success && response.result) {
      opinionListData.value = response.result.list || []
    } else {
      opinionListData.value = []
    }
  } catch (error) {
    console.error('获取观点分析数据失败:', error)
    opinionListData.value = []
  } finally {
    loading.value = false
  }
}

// 对外暴露refresh方法
const refresh = (params?: VocQueryParams) => {
  resetAndLoadFirstPage(params)
}

/**
 * 分页大小改变
 */
const handleSizeChange = async (size: number) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  await fetchOpinionListData()
  tableRef.value?.scrollToTop()
}

/**
 * 当前页改变
 */
const handleCurrentChange = async (page: number) => {
  pagination.pageNum = page
  await fetchOpinionListData()
  tableRef.value?.scrollToTop()
}

defineExpose({ refresh })

onMounted(() => {
  refresh()
})

watch(
  () => ((props.queryParams || {}) as any)?.intention,
  newVal => {
    const next = newVal || ''
    // 初次进入或外部下钻切换 intention 时，跟随 queryParams 进行预选
    if (!lastQueryIntention.value || next) {
      // 仅当外部提供了 intention（next 非空），或首次初始化时同步
      if (next && selectValue.value !== next) {
        selectValue.value = next
      }
      if (!lastQueryIntention.value && !selectValue.value) {
        selectValue.value = next
      }
    } else {
      // 外部清空 intention 时，仅当当前选择仍等于上一次外部值，才同步清空
      if (selectValue.value === lastQueryIntention.value) {
        selectValue.value = ''
      }
    }

    lastQueryIntention.value = next
  },
  { immediate: true }
)

watch(
  () => props.queryParams,
  () => {
    refresh()
  },
  { deep: true }
)
</script>
<template>
  <div class="h-full flex flex-col">
    <CommonTitle title="观点列表" class="mb-16">
      <template #left>
        <el-select
          v-model="selectValue"
          placeholder=""
          :empty-values="[null, undefined]"
          @change="refresh()"
          style="width: 108px"
        >
          <el-option
            v-for="item in selectOptions"
            :key="item.value"
            :label="item.text"
            :value="item.value"
          />
        </el-select>
      </template>
    </CommonTitle>
    <!-- 观点分析表格 -->
    <DrillDownTable
      ref="tableRef"
      :data="opinionListData"
      :columns="opinionListHeaders"
      headerHeight="56px"
      height="100%"
      :stripe="false"
      :scrollable="true"
      :show-sort-icon="true"
      :loading="loading"
      @cell-click="handleCellClick"
      @sort-change="handleTableSortChange"
      class="csa__detail-table"
    />
    <!-- 分页 -->
    <template v-if="pagination.total > 0">
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="->,total, prev, pager, next, sizes"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </template>
  </div>
</template>
