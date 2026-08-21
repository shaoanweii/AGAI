<script setup lang="ts">
import { useTable } from '@/hooks/table'
import { computed, inject, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { TableInstance } from 'element-plus'
import { ArrowDown, Plus, Search } from '@element-plus/icons-vue'
import BatchActionDialog from './BatchActionDialog.vue'
import ExperienceCodeFormDialog from './ExperienceCodeFormDialog.vue'
import {
  fetchExperienceCategoryData,
  fetchExperienceCodeListData,
  fetchExperienceCodeOperatorOptions,
  type ExperienceCodeListQuery
} from '../service'
import { experienceCodePageContextKey } from '../context'
import {
  resolveExperienceCodeStatusLabel,
  resolveExperienceCodeStatusOptions
} from '../statusOptions'
import { hasAnyFinalTopicBoundRow, showDisableBlockedDialog } from './disableGuards'
import {
  type BatchActionType,
  type ExperienceCategoryItem,
  type ExperienceCodeItem,
  type ExperienceCodeOperatorOption,
  type ExperienceCodeTableRow,
  type ExperienceCodeType,
  type ExperienceFilterTarget,
  type StatusValue
} from './types'

defineOptions({
  name: 'ExperienceCodeList'
})

type SortProp = 'tagLibNameHierarchical' | 'tagType' | 'tagTypeName' | 'updateTime' | 'createTime'
type SortOrder = 'ascending' | 'descending'

interface Props {
  activeTarget: ExperienceFilterTarget | null
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (e: 'refresh-category-list'): void
}>()

const tableRef = ref<TableInstance>()
const codeDialogVisible = ref(false)
const batchDialogVisible = ref(false)
const currentBatchAction = ref<BatchActionType>('enable')
const editingCode = ref<ExperienceCodeItem | null>(null)
const defaultTypeCode = ref<ExperienceCodeType>('')
const defaultCategoryId = ref('')
const pageContext = inject(experienceCodePageContextKey, null)
const categories = ref<ExperienceCategoryItem[]>([])
const operatorOptions = ref<ExperienceCodeOperatorOption[]>([])
const multipleSelection = ref<ExperienceCodeTableRow[]>([])

const filterForm = reactive({
  status: '' as StatusValue | '',
  operator: '',
  keyword: ''
})
const appliedFilters = reactive({
  status: '' as StatusValue | '',
  operator: '',
  keyword: ''
})
const sortState = reactive({
  prop: 'createTime' as SortProp,
  order: 'ascending' as SortOrder
})

const { table } = useTable(
  {
    method: 'POST',
    url: '/insights/insTagLibClient/findExperienceCodeList',
    pageSize: 10
  },
  res => {
    const pageResult = res.result || {}
    return {
      list: Array.isArray(pageResult.records) ? pageResult.records : [],
      total: Number(pageResult.total || 0)
    }
  }
)

const ORDER_FIELD_MAP: Record<SortProp, string> = {
  tagLibNameHierarchical: 'tagLibNameHierarchical',
  tagType: 'tagType',
  // 表格展示列使用 tagTypeName，排序时仍需映射到真实字段 tagType。
  tagTypeName: 'tagType',
  updateTime: 'updateTime',
  createTime: 'createTime'
}

const categoryList = computed(() => {
  if (pageContext?.categoryData.value.categories.length) {
    return pageContext.categoryData.value.categories
  }
  return categories.value
})
const statusOptions = computed(() => {
  return pageContext?.statusOptions.value || resolveExperienceCodeStatusOptions()
})

/**
 * 批量启用/禁用入口直接复用当前状态字典，保证下拉顺序和场景模块保持一致。
 */
const batchStatusActions = computed(() => {
  return statusOptions.value
    .filter(item => item.value === '1' || item.value === '0')
    .map(item => ({
      label: item.label,
      command: item.value === '1' ? ('enable' as const) : ('disable' as const)
    }))
})

/**
 * 建立分类索引，便于表格映射和弹框回填直接复用。
 */
const categoryMap = computed(() => {
  const map = new Map<string, ExperienceCategoryItem>()
  categoryList.value.forEach(item => map.set(item.id, item))
  return map
})

/**
 * 过滤目标只要带有分类 id，就按具体分类处理；为空时表示仍停留在类型维度。
 */
const isCategoryTarget = (
  target: ExperienceFilterTarget | null
): target is ExperienceFilterTarget & { categoryId: string } => {
  return Boolean(target?.categoryId)
}

const selectedRows = computed(() => multipleSelection.value)

/**
 * 清空表格勾选态，避免筛选、翻页和刷新后残留旧选择。
 */
const clearTableSelection = () => {
  nextTick(() => {
    tableRef.value?.clearSelection()
  })
}

/**
 * 列表请求统一重置勾选，确保批量操作只作用于当前可见数据。
 */
const resetListSelection = () => {
  multipleSelection.value = []
  clearTableSelection()
}

/**
 * 操作人筛选下拉统一走远程接口；筛选值保留用户 id，展示层再使用 userName。
 */
const getOperatorOptions = async () => {
  try {
    operatorOptions.value = await fetchExperienceCodeOperatorOptions()
  } catch (error) {
    console.error('获取体验代码操作人列表失败:', error)
    operatorOptions.value = []
  }
}

/**
 * 左侧树当前选中节点统一映射为接口 tagParentId。
 */
const resolveCurrentQueryId = () => {
  return props.activeTarget?.queryId || ''
}

/**
 * 无论左侧选中类型节点还是具体分类节点，列表查询都要带上当前代码类型，保证接口筛选口径稳定。
 */
const resolveCurrentTagType = () => {
  return props.activeTarget?.typeCode
}

/**
 * 表格排序统一转为后端 order 字段；展示列映射到真实字段，保证服务端分页仍可稳定排序。
 */
const resolveOrderValue = (prop?: SortProp, order?: SortOrder) => {
  if (!prop || !order) return ''
  const orderField = ORDER_FIELD_MAP[prop]
  return orderField ? `${orderField} ${order === 'ascending' ? 'asc' : 'desc'}` : ''
}

/**
 * 构造右侧列表查询参数时，同时合并左树选中、顶部筛选和排序分页条件。
 */
const buildQueryParams = (): ExperienceCodeListQuery => {
  return {
    tagParentId: resolveCurrentQueryId() || undefined,
    tagType: resolveCurrentTagType(),
    tagStatus: appliedFilters.status || undefined,
    operateUser: String(appliedFilters.operator || '').trim() || undefined,
    // 当前接口按 tagName 做关键字过滤，体验代码列表改为直接走后端分页查询口径。
    tagName: appliedFilters.keyword.trim() || undefined,
    order: resolveOrderValue(sortState.prop, sortState.order) || undefined
  }
}

/**
 * 将当前筛选态同步到 useTable，保证分页、刷新和弹框回调都复用同一份查询条件。
 */
const syncTableFilter = () => {
  table.filter = buildQueryParams()
}

/**
 * 重新拉取右侧依赖的分类与体验代码数据；默认只刷新表格，
 * 仅在新增、编辑、移动等会影响分类元数据的场景下才显式刷新分类树。
 */
const refreshList = async (options: { refreshCategory?: boolean; resetPage?: boolean } = {}) => {
  if (!props.activeTarget) {
    table.list = []
    table.total = 0
    resetListSelection()
    return
  }

  const { refreshCategory = false, resetPage = false } = options
  if (resetPage) {
    table.pageNum = 1
  }

  syncTableFilter()

  try {
    const categoryPromise = refreshCategory
      ? pageContext?.refreshCategoryData() || fetchExperienceCategoryData()
      : Promise.resolve(
          pageContext?.categoryData.value || {
            categories: categories.value,
            typeSummaries: []
          }
        )

    table.loading = true
    const [categoryResponse, pageResult] = await Promise.all([
      categoryPromise,
      fetchExperienceCodeListData({
        ...buildQueryParams(),
        pageNum: table.pageNum,
        pageSize: table.pageSize
      })
    ])

    if (!pageContext) {
      categories.value = categoryResponse.categories
    }
    table.list = pageResult.list
    table.total = pageResult.total
  } catch (error: any) {
    table.list = []
    table.total = 0
    ElMessage.error(error?.message || '获取体验代码列表失败，请稍后重试')
  } finally {
    table.loading = false
  }
}

/**
 * 顶部筛选采用“输入态”和“生效态”分离，只有用户明确查询后才刷新服务端分页结果。
 */
const applyFilters = async () => {
  appliedFilters.status = filterForm.status
  appliedFilters.operator = String(filterForm.operator || '').trim()
  appliedFilters.keyword = filterForm.keyword.trim()
  resetListSelection()
  await refreshList({ resetPage: true })
}

/**
 * 分页条切换页码时直接走服务端重新取数，避免本地切片和总数口径不一致。
 */
const handleCurrentChange = (pageNum: number) => {
  resetListSelection()
  table.pageNum = pageNum
  void refreshList()
}

/**
 * 每页条数变化后回到第一页，并让后端按新的分页大小返回当前结果集。
 */
const handlePageSizeChange = (pageSize: number) => {
  resetListSelection()
  table.pageSize = pageSize
  table.pageNum = 1
  void refreshList()
}

/**
 * 打开新建或编辑体验代码弹框时仅继承当前类型，不默认带出分类，避免误提交到当前叶子节点。
 */
const openCodeDialog = (row?: ExperienceCodeItem) => {
  editingCode.value = row || null
  defaultTypeCode.value = props.activeTarget?.typeCode || ''
  defaultCategoryId.value = ''

  if (!row && isCategoryTarget(props.activeTarget)) {
    const category = categoryMap.value.get(props.activeTarget.categoryId)
    if (category) {
      defaultTypeCode.value = category.tagType
    }
  }

  codeDialogVisible.value = true
}

/**
 * 批量操作前做基础校验，避免空选中弹出无效弹框。
 */
const openBatchDialog = (actionType: BatchActionType) => {
  if (!selectedRows.value.length) {
    ElMessage.warning('请先选择需要批量操作的体验代码')
    return
  }
  if (actionType === 'disable' && hasAnyFinalTopicBoundRow(selectedRows.value)) {
    void showDisableBlockedDialog()
    return
  }
  if (actionType === 'move') {
    // 批量移动后的目标分类完全跟随左侧外部类型，因此勾选数据必须先保证属于同一体验代码类型。
    const selectedTypeCodes = Array.from(
      new Set(
        selectedRows.value
          .map(item => String(item.tagType ?? '').trim())
          .filter(typeCode => Boolean(typeCode))
      )
    )
    if (selectedTypeCodes.length !== 1) {
      ElMessage.warning('仅支持相同体验代码类型的记录批量移动')
      return
    }
  }
  currentBatchAction.value = actionType
  batchDialogVisible.value = true
}

/**
 * 表格排序改由服务端处理；清空排序时回退到默认创建时间正序。
 */
const handleSortChange = ({
  prop,
  order
}: {
  prop?: string
  order?: 'ascending' | 'descending' | null
}) => {
  const allowedProps: SortProp[] = [
    'tagLibNameHierarchical',
    'tagType',
    'tagTypeName',
    'updateTime',
    'createTime'
  ]
  if (!prop || !order || !allowedProps.includes(prop as SortProp)) {
    sortState.prop = 'createTime'
    sortState.order = 'ascending'
  } else {
    sortState.prop = prop as SortProp
    sortState.order = order
  }

  resetListSelection()
  void refreshList({ resetPage: true, refreshCategory: false })
}

/**
 * 启用状态统一映射为圆点样式，便于列表视觉表达与场景模块保持一致。
 */
const resolveStatusClass = (status?: string | null) => {
  return String(status ?? '1') === '1' ? 'status-dot--success' : 'status-dot--disabled'
}

/**
 * 状态展示文案优先跟随 stopOrEnable 字典，避免筛选项和列表文案出现双口径。
 */
const resolveStatusText = (status?: string | null) => {
  return resolveExperienceCodeStatusLabel(status, statusOptions.value)
}

/**
 * 接口已返回状态文案，缺失时再回退到字典解析，保证展示口径优先以后端为准。
 */
const resolveRowStatusText = (row: ExperienceCodeTableRow) => {
  return String(row.tagStatusText ?? '').trim() || resolveStatusText(row.tagStatus || '1')
}

/**
 * 单行勾选变化时同步当前页选中记录，供批量操作直接复用。
 */
const handleSelectionChange = (rows: ExperienceCodeTableRow[]) => {
  multipleSelection.value = rows
}

/**
 * 新增/编辑成功后沿用场景模块的刷新节奏：先清空勾选，再按当前筛选条件刷新右侧，最后通知左侧统计更新。
 */
const handleCodeDialogSuccess = async () => {
  resetListSelection()
  await getOperatorOptions()
  await refreshList({ refreshCategory: true })
  emit('refresh-category-list')
}

/**
 * 批量启用/禁用只刷新右侧表格；只有移动场景才需要同步左侧分类统计。
 */
const handleBatchSuccess = async () => {
  resetListSelection()
  const shouldRefreshCategory = currentBatchAction.value === 'move'
  await refreshList({ refreshCategory: shouldRefreshCategory })
  if (shouldRefreshCategory) {
    emit('refresh-category-list')
  }
}

watch(
  () => props.activeTarget,
  async target => {
    resetListSelection()
    table.pageNum = 1

    if (!target) {
      table.list = []
      table.total = 0
      return
    }

    await refreshList({ resetPage: true })
  },
  { deep: true, immediate: true }
)

watch(
  () => table.list,
  () => {
    resetListSelection()
  }
)

onMounted(() => {
  void getOperatorOptions()
})

defineExpose({
  refreshList
})
</script>

<template>
  <section class="experience-code card-class">
    <div class="toolbar">
      <div class="panel-title">标签列表</div>
      <div class="panel-actions">
        <el-select
          v-model="filterForm.status"
          class="toolbar-select"
          clearable
          placeholder="全部状态"
          @change="applyFilters"
        >
          <el-option
            v-for="item in statusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-select-v2
          v-model="filterForm.operator"
          class="toolbar-select toolbar-select--operator"
          :options="operatorOptions"
          :props="{ value: 'id', label: 'userName' }"
          clearable
          filterable
          placeholder="操作人"
          @change="applyFilters"
        />
        <el-input
          v-model.trim="filterForm.keyword"
          class="toolbar-input toolbar-input--keyword"
          clearable
          placeholder="请输入末级标签"
          @clear="applyFilters"
          @keyup.enter="applyFilters"
        >
          <template #suffix>
            <el-icon class="toolbar-input__icon" @click="applyFilters">
              <Search />
            </el-icon>
          </template>
        </el-input>
        <el-dropdown trigger="click" @command="openBatchDialog">
          <el-button :disabled="!selectedRows.length">
            批量操作
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item
                v-for="item in batchStatusActions"
                :key="item.command"
                :command="item.command"
                :disabled="!selectedRows.length"
              >
                {{ item.label }}
              </el-dropdown-item>
              <el-dropdown-item command="move" :disabled="!selectedRows.length">
                移动
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button type="primary" @click="openCodeDialog()">
          <template #icon>
            <Plus />
          </template>
          新建标签
        </el-button>
      </div>
    </div>

    <div class="table-container">
      <el-table
        ref="tableRef"
        v-loading="table.loading"
        :data="table.list || []"
        row-key="id"
        class="experience-code__table"
        height="100%"
        style="width: 100%"
        :tooltip-options="{ popperClass: 'common-tooltip' }"
        @sort-change="handleSortChange"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="56" align="center" reserve-selection />
        <el-table-column prop="tagName" label="末级标签" min-width="200" show-overflow-tooltip />
        <el-table-column
          prop="tagLibNameHierarchical"
          label="所属分类"
          min-width="350"
          sortable="custom"
          show-overflow-tooltip
        />
        <el-table-column prop="operateUser" label="操作人" width="120" show-overflow-tooltip />
        <el-table-column prop="updateTime" label="更新时间" width="180" sortable="custom" />
        <el-table-column prop="createTime" label="创建时间" width="180" sortable="custom" />
        <el-table-column prop="tagStatus" label="启用状态" width="80" fixed="right">
          <template #default="{ row }">
            <div class="status-cell">
              <span :class="['status-dot', resolveStatusClass(row.tagStatus || '1')]"></span>
              <span class="cell-wrap-text">{{ resolveRowStatusText(row) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="108" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openCodeDialog(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="pagination-bar">
      <el-pagination
        v-model:current-page="table.pageNum"
        v-model:page-size="table.pageSize"
        :default-page-size="10"
        :total="table.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="->,total, prev, pager, next, sizes"
        class="pagination"
        @size-change="handlePageSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <ExperienceCodeFormDialog
      v-model:visible="codeDialogVisible"
      :code-data="editingCode"
      :category-list="categoryList"
      :default-type-code="defaultTypeCode"
      :default-category-id="defaultCategoryId"
      @success="handleCodeDialogSuccess"
    />

    <BatchActionDialog
      v-model:visible="batchDialogVisible"
      :action-type="currentBatchAction"
      :selected-rows="selectedRows"
      :category-list="categoryList"
      :target-type-code="props.activeTarget?.typeCode || ''"
      @success="handleBatchSuccess"
    />
  </section>
</template>

<style scoped lang="scss">
.card-class {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 16px 24px;
  background: #ffffff;
  box-shadow: 0 1px 2px 0 rgba(10, 13, 18, 0.05);
  border-radius: 8px;
  box-sizing: border-box;
}

.experience-code {
  flex: 1;
  min-width: 0;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.panel-title {
  flex-shrink: 0;
  font-weight: 600;
  font-size: 20px;
  line-height: 32px;
  color: #1d2129;
}

.panel-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: nowrap;
  justify-content: flex-end;
  min-width: 0;
}

.toolbar-select {
  width: 128px;
  flex-shrink: 0;
}

.toolbar-input {
  flex-shrink: 0;
}

.toolbar-input__icon {
  cursor: pointer;
  color: #667085;
}

.toolbar-select--operator {
  width: 144px;
}

.toolbar-input--keyword {
  width: 168px;
}

.table-container {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.pagination-bar {
  padding-top: 16px;
}

.pagination {
  justify-content: flex-end;
}

:deep(.status-cell) {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 100%;
}

:deep(.status-dot) {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}

:deep(.status-dot--success) {
  background: #2ab940;
}

:deep(.status-dot--disabled) {
  background: #c9cdd4;
}

.is-disabled-text {
  color: #86909c;
}

:deep(.el-table__cell) {
  height: 52px;
  padding-top: 0;
  padding-bottom: 0;
}

:deep(.el-table__header .el-table__cell) {
  color: #1d2129;
  font-weight: 500;
  font-size: 14px;
  background: #f2f4f7;
}

:deep(.text-ellipsis) {
  display: inline-block;
  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:deep(.cell-wrap-text) {
  word-wrap: break-word;
  word-break: break-all;
  white-space: normal;
  overflow-wrap: break-word;
  hyphens: auto;
}
</style>
