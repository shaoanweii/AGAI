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
import { getScenarioList } from '@api/drillDownDialog'
import type { ScenarioAnalysisVo } from '@api/drillDownDialog/types.d.ts'
import { fmtNum, fmtPer, fmtFix } from '@/utils'
import useUserStore from '@/store/modules/user.ts'
import useGeneralDrillDownStore from '@/store/modules/generalDrillDown'

defineOptions({
  name: 'ScenarioAnalysis'
})

// 接收父级透传的查询参数与筛选条件
const props = defineProps<{ queryParams?: VocQueryParams; filters?: any[] }>()

const userStore = useUserStore()

// 场景分析数据
const tableData = ref<ScenarioAnalysisVo[]>([])
// 场景分析表格实例
const tableRef = ref<DrillDownTableInstance>()
const selectValue = ref<string>('')
const selectOptions = ref<any[]>([
  { text: '全部场景', value: '' }
  // ...userStore.getDictItems('voc_sentiment')
])
// 加载状态
const loading = ref(false)

// 列表分页状态
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

type ScenarioSortOrder = 'asc' | 'desc'

interface ScenarioTableSortState {
  sortField?: string
  sortOrder?: ScenarioSortOrder
}

// 当前列表排序状态：仅在用户点击表头后透传给后端
const tableSort = reactive<ScenarioTableSortState>({})

const tableHeaders: TableHeaderGroup[] = [
  { key: 'scenarioFirst', label: '一级场景', columnPadding: '0 12px 0 8px', tooltip: { show: true } },
  { key: 'scenario', label: '二级场景', columnPadding: '0 12px 0 8px', tooltip: { show: true } },
  {
    key: 'mentions',
    label: '提及量',
    render: (row: any) => fmtNum(row.mentions),
    width: '150px',
    sortable: 'custom'
  },
  {
    key: 'mentionsShare',
    label: '占比',
    render: (row: any) => fmtPer(row.mentionsShare),
    width: '150px',
    sortable: 'custom'
  },
  { key: 'mentionTrend', label: '趋势', render: 'trend', width: '150px' },
  {
    key: 'mentionsMoM',
    label: '环比',
    render: (row: any) => fmtFix(row.mentionsMoM),
    width: '150px',
    sortable: 'custom'
  }
]

// 重置并加载第一页
async function resetAndLoadFirstPage(params?: VocQueryParams) {
  pagination.pageNum = 1
  tableData.value = []
  await fetchScenarioData(params)
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
 * 处理场景列表排序变化，改为由后端完成排序。
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
  await fetchScenarioData()
  tableRef.value?.scrollToTop()
}

/**
 * 获取场景分析数据
 */
const fetchScenarioData = async (params?: VocQueryParams) => {
  try {
    loading.value = true
    const qp: VocQueryParams = {
      ...(props.queryParams || {}),
      ...(params || {}),
      sentiment: selectValue.value || '',
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...(tableSort.sortField && tableSort.sortOrder
        ? {
            sortField: tableSort.sortField,
            sortOrder: tableSort.sortOrder
          }
        : {})
    }
    const response = await getScenarioList(qp)
    pagination.total = response?.result?.total || 0
    if (response.success && response.result) {
      tableData.value = response.result.list || []
    } else {
      tableData.value = []
    }
  } catch (error) {
    console.error('获取场景分析数据失败:', error)
    tableData.value = []
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
  await fetchScenarioData()
  tableRef.value?.scrollToTop()
}

/**
 * 当前页改变
 */
const handleCurrentChange = async (page: number) => {
  pagination.pageNum = page
  await fetchScenarioData()
  tableRef.value?.scrollToTop()
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

const ddStore = useGeneralDrillDownStore()
const handleCellClick = (row: any, column: any) => {
  console.log('点击了表格行', row, column)
  // 记录来源：场景
  ddStore.updateDDViewParams({
    lastDrillFrom: DrillTabKey.SCENARIO,
    drillScene: DrillTabKey.SCENARIO
  })
  ddStore.drillDown({ usageScenarioSecond: row.scenario }, [
    {
      text: row.scenario,
      value: { usageScenarioSecond: row.scenario }
    }
  ])
}
</script>
<template>
  <div class="h-full flex flex-col">
    <CommonTitle title="场景列表" class="mb-16">
      <template #left>
        <!--        <el-select
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
        </el-select>-->
      </template>
    </CommonTitle>
    <!-- 场景分析表格 -->
    <DrillDownTable
      ref="tableRef"
      :data="tableData"
      :columns="tableHeaders"
      headerHeight="56px"
      height="100%"
      :stripe="false"
      :scrollable="true"
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
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </template>
  </div>
</template>
<style scoped lang="less"></style>
