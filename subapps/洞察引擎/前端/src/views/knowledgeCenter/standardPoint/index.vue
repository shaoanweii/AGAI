<script setup lang="ts">
/**
 * 知识中心 - 标准观点管理页面
 * 功能：观点的查询、新建、编辑、批量操作（编辑、合并、启用/停用）
 */
import { computed, h, onMounted, reactive, ref, toRaw, watch } from 'vue'
import FtCard from '@/components/FtCard.vue'
import useConditions from '@/hooks/useConditions'
import { useTable } from '@/hooks/table'
import { computedCardHeight } from '@/utils'
import { showOverflowTooltipConfig } from '@/constant/index'
import { ElButton, ElCheckbox, ElMessage, ElTooltip, TableV2FixedDir } from 'element-plus'
import type { Column } from 'element-plus'
import { ArrowDown, Plus } from '@element-plus/icons-vue'
import {
  BatchMergeDialog,
  StandardPointFormDialog,
  type StandardPointDialogMode,
  type StandardPointFormData
} from './components'
import { batchChangeTopicStatusClient } from '@/api/tag'
import cloneDeep from 'lodash-es/cloneDeep'

defineOptions({
  name: 'knowledgeCenterStandardPoint'
})

// 观点标签客户端VO（用于不同体验代码类型的标签层级）
type TopicTagClientVo = {
  firstName?: string
  secondName?: string
  thirdName?: string
  fourthName?: string
  fifthEmotion?: string
  [key: string]: any
}

// 观点VO类型定义
type TopicVo = {
  topicCode?: string
  topicName?: string
  topicDesc?: string
  synonyms?: string
  mappingCode?: string
  emotion?: string
  intention?: string
  tagCustomerIssueClassification?: string
  tagIssueSeverity?: string
  eventClarity?: string
  susceptiveType?: string
  tagBusinessDomain?: string
  d2cResponsibleDept?: string
  tagAccuracy?: string
  tagComplaintFlagNeedingReply?: string
  tagNeedForvclosedLoop?: string
  ca?: TopicTagClientVo
  jour?: TopicTagClientVo
  vrt?: TopicTagClientVo
  cpt?: TopicTagClientVo
  pro?: TopicTagClientVo
  nps?: TopicTagClientVo
  operateUser?: string
  updateTime?: string
  createUser?: string
  createTime?: string
  tagStatus?: string
  [key: string]: any
}

// 筛选条件展开/收起状态
const isExpanded = ref(false)

// 获取筛选条件下拉选项数据
const { conditions } = useConditions({ url: '/insights/insTagLibClient/conditions' })
const getConditionOptions = (key: string) => {
  return conditions?.[key] || []
}

// 根据多个key获取条件选项（返回第一个有数据的）
const getConditionOptionsByKeys = (keys: string[]) => {
  for (const key of keys) {
    const list = getConditionOptions(key)
    if (Array.isArray(list) && list.length) return list
  }
  return []
}

// 启用状态选项
const enableTypeOptions = computed(() => {
  const fromConditions = getConditionOptionsByKeys(['stopOrEnable'])
  return fromConditions || []
})

const vocSentimentOptions = computed(() => {
  const fromConditions = getConditionOptionsByKeys(['vocSentiment'])
  return fromConditions || []
})

const vocIntentionOptions = computed(() => {
  const fromConditions = getConditionOptionsByKeys(['vocIntention'])
  return fromConditions || []
})

// 筛选表单数据
const filterForm = reactive({
  topicName: '',
  operateUser: '',
  emotions: null as string | number | null,
  intentions: null as string | number | null,
  tagStatus: null as string | number | null
})

// 保存初始筛选表单数据，用于重置
const initialFilterForm = cloneDeep(toRaw(filterForm))

// 构建观点列表查询参数
const buildTopicListFilter = () => {
  const topicName = filterForm.topicName?.trim()
  const operateUser = filterForm.operateUser?.trim()

  return {
    ...filterForm,
    topicName: topicName || undefined,
    operateUser: operateUser || undefined
  }
}

// 观点列表表格数据管理
const {
  table: topicTable,
  handleSizeChange: handleTopicSizeChange,
  handleCurrentChange: handleTopicCurrentChange,
  getFirstPageTableData: getTopicFirstPageTableData,
  refreshTableData: refreshTopicTableData
} = useTable(
  {
    method: 'POST',
    url: '/insights/insTagLibClient/findAllTopicList'
  },
  res => {
    const page = res?.result || {}
    const records = Array.isArray(page?.records) ? page.records : []
    const total = Number(page?.total ?? 0)
    return {
      list: records,
      total: Number.isFinite(total) ? total : 0
    }
  }
)

// 表格卡片高度计算（根据筛选条件展开状态）
const tableCardHeight = computed(() => {
  return computedCardHeight(isExpanded.value ? 275 : 155)
})

// 表格选中行数据
const selectedRows = ref<TopicVo[]>([])
const getTopicRowKey = (row: TopicVo) => String(row?.topicCode ?? '')

// 表格数据变化时清空选中行
watch(
  () => topicTable.list,
  () => {
    selectedRows.value = []
  }
)

// 格式化单元格文本（空值显示为'-'）
const formatCellText = (val: any) => {
  if (val === 0) return '0'
  if (val === null || val === undefined) return '-'
  if (Array.isArray(val)) {
    const text = val.filter(Boolean).join('、')
    return text || '-'
  }
  const text = String(val).trim()
  return text ? text : '-'
}

// 渲染带省略号的单元格（可选tooltip）
const renderEllipsisCell = (val: any, options?: { tooltip?: boolean }) => {
  const content = formatCellText(val)
  const renderContent = () => h('div', { class: 'text-ellipsis' }, content)

  // 默认不展示 Tooltip；需要展示时由列配置显式开启
  // 无数据展示为“-”时不展示 Tooltip
  if (!options?.tooltip || content === '-') return renderContent()
  // 该页面 Tooltip 不使用可滚动容器，避免在“始终显示滚动条”的系统设置下出现滚动条
  return h(
    ElTooltip,
    {
      content,
      placement: 'top',
      ...showOverflowTooltipConfig,
      popperClass: 'standard-point-tooltip common-tooltip'
    },
    renderContent
  )
}

// 创建文本列配置
const createTextColumn = (params: {
  key: string
  title: string
  dataKey?: string
  width?: number
  fixed?: boolean | TableV2FixedDir
  tooltip?: boolean
}) => {
  return {
    key: params.key,
    title: params.title,
    dataKey: params.dataKey ?? params.key,
    width: params.width ?? 160,
    fixed: params.fixed,
    cellRenderer: ({ cellData }: any) => renderEllipsisCell(cellData, { tooltip: params.tooltip })
  } as Column
}

// 创建自定义取值列配置（通过getter函数获取数据）
const createGetterColumn = (params: {
  key: string
  title: string
  width?: number
  fixed?: boolean | TableV2FixedDir
  getter: (row: TopicVo) => any
  tooltip?: boolean
}) => {
  return {
    key: params.key,
    title: params.title,
    dataKey: params.key,
    width: params.width ?? 160,
    fixed: params.fixed,
    cellRenderer: ({ rowData }: any) =>
      renderEllipsisCell(params.getter(rowData as TopicVo), { tooltip: params.tooltip })
  } as Column
}

// 列表将多层级关联收敛为与表单一致的单列路径展示。
const formatAssociationPath = (association?: TopicTagClientVo) => {
  if (!association) return '-'
  const path = [
    association.firstName,
    association.secondName,
    association.thirdName,
    association.fourthName
  ]
    .map(item => String(item ?? '').trim())
    .filter(Boolean)
  return path.length ? path.join(' / ') : '-'
}

// 判断是否为启用状态
const isEnabledStatus = (tagStatus: any) => {
  return String(tagStatus) === '1'
}

// 获取状态文本
const statusText = (tagStatus: any) => {
  const element = enableTypeOptions.value.find((item: any) => item.key === tagStatus)
  return formatCellText(element?.value)
}

// 表格列配置
const columns: Column[] = [
  {
    key: 'selection',
    width: 55,
    fixed: true,
    cellRenderer: ({ rowData }: any) => {
      const rowKey = getTopicRowKey(rowData as TopicVo)
      return h(ElCheckbox, {
        modelValue: selectedRows.value.some(item => getTopicRowKey(item) === rowKey),
        'onUpdate:modelValue': (val: any) => {
          const checked = Boolean(val)
          if (checked) {
            if (!selectedRows.value.some(item => getTopicRowKey(item) === rowKey)) {
              selectedRows.value = [...selectedRows.value, rowData]
            }
          } else {
            selectedRows.value = selectedRows.value.filter(item => getTopicRowKey(item) !== rowKey)
          }
        }
      })
    },
    headerCellRenderer: () => {
      const tableList = (topicTable.list || []) as TopicVo[]
      const allSelected = tableList.length > 0 && selectedRows.value.length === tableList.length
      const indeterminate =
        selectedRows.value.length > 0 && selectedRows.value.length < tableList.length
      return h(ElCheckbox, {
        modelValue: allSelected,
        indeterminate,
        'onUpdate:modelValue': (val: any) => {
          if (val) {
            selectedRows.value = [...tableList]
          } else {
            selectedRows.value = []
          }
        }
      })
    }
  },

  createTextColumn({ key: 'topicName', title: '观点名称', width: 220, tooltip: true, fixed: true }),
  createTextColumn({ key: 'topicDesc', title: '观点描述', width: 240, tooltip: true }),
  createTextColumn({ key: 'synonyms', title: '同义词', width: 260, tooltip: true }),
  createGetterColumn({
    key: 'associatedTag',
    title: '关联标签',
    width: 320,
    getter: row => formatAssociationPath(row.ca),
    tooltip: true
  }),
  createGetterColumn({
    key: 'associatedJourney',
    title: '关联旅程',
    width: 320,
    getter: row => formatAssociationPath(row.jour),
    tooltip: true
  }),
  createTextColumn({ key: 'emotion', title: '情感', width: 100 }),
  createTextColumn({ key: 'intention', title: '意图', width: 100 }),

  createTextColumn({ key: 'operateUser', title: '操作人', width: 140 }),
  createTextColumn({ key: 'updateTime', title: '操作时间', width: 180 }),
  createTextColumn({ key: 'createUser', title: '创建人', width: 140 }),
  createTextColumn({ key: 'createTime', title: '创建时间', width: 180 }),

  {
    key: 'tagStatus',
    title: '状态',
    dataKey: 'tagStatus',
    width: 120,
    fixed: TableV2FixedDir.RIGHT,
    cellRenderer: ({ rowData }: any) => {
      const enabled = isEnabledStatus((rowData as TopicVo)?.tagStatus)
      return h('div', { class: 'status-wrapper' }, [
        h('span', { class: ['status-circle', enabled ? 'success-bg' : 'forbidden-bg'] }),
        h('span', { class: 'ml-8' }, statusText((rowData as TopicVo)?.tagStatus))
      ])
    }
  },
  {
    key: 'operation',
    title: '操作',
    dataKey: 'operation',
    width: 100,
    fixed: TableV2FixedDir.RIGHT,
    cellRenderer: ({ rowData }: any) => {
      return h(
        ElButton,
        {
          type: 'primary',
          link: true,
          onClick: () => {
            openEditDialog(rowData as TopicVo)
          }
        },
        () => '编辑'
      )
    }
  }
]

// 获取选中的观点编码列表
const getSelectedTopicCodes = () => {
  const codes = (selectedRows.value || []).map(r => getTopicRowKey(r)).filter(Boolean)
  return Array.from(new Set(codes))
}

// 批量操作弹窗数据
const batchData = reactive({
  visible: false,
  action: '',
  title: '',
  messages: ''
})

// 批量合并弹窗状态
const batchMergeVisible = ref(false)

// 打开批量编辑弹窗
function openBatchEditDialog() {
  const topicCodes = getSelectedTopicCodes()
  if (!topicCodes.length) {
    ElMessage.warning('请选择需要编辑的观点')
    return
  }

  formDialogMode.value = 'batch'
  formDialogEditData.value = { topicCodes }
  formDialogVisible.value = true
}

// 打开批量合并弹窗
function openBatchMergeDialog() {
  const topicCodes = getSelectedTopicCodes()
  if (topicCodes.length < 2) {
    ElMessage.warning('请至少选择两个观点进行合并')
    return
  }

  batchMergeVisible.value = true
}

// 打开批量状态操作弹窗（启用/停用）
function openBatchStatusDialog(action: any) {
  const topicCodes = getSelectedTopicCodes()
  if (!topicCodes.length) {
    ElMessage.warning('请选择需要操作的观点')
    return
  }
  batchData.action = action.key
  batchData.title = `批量${action.value}`
  batchData.messages = `是否确认批量${action.value}选中标准观点？`
  batchData.visible = true
}

// 批量状态操作确认
const handleBatchStatusConfirm = async ({ close }: { close: () => void }) => {
  const topicCodes = getSelectedTopicCodes() || []
  if (!topicCodes.length) {
    ElMessage.warning('未选择任何观点，无法操作')
    return
  }

  try {
    await batchChangeTopicStatusClient({
      topicCodes,
      tagStatus: batchData.action
    })
    ElMessage.success('操作成功')
    close()
    await refreshTopicTableData()
  } catch (e: any) {
    console.error('批量更新观点状态失败', e)
    ElMessage.error('批量操作失败，请稍后重试')
  }
}

// 批量操作命令处理
const handleBatchCommand = async (_command: string) => {
  const command = String(_command || '').trim()

  if (command === 'batchEdit') {
    openBatchEditDialog()
  } else if (command === 'batchMerge') {
    openBatchMergeDialog()
  } else {
    openBatchStatusDialog(_command)
  }
}

// 表单弹窗状态管理
const formDialogVisible = ref(false)
const formDialogMode = ref<StandardPointDialogMode>('create')
const formDialogEditData = ref<Partial<StandardPointFormData> | null>(null)

// 打开新建观点弹窗
function openCreateDialog() {
  formDialogMode.value = 'create'
  formDialogEditData.value = null
  formDialogVisible.value = true
}

// 打开编辑观点弹窗
function openEditDialog(row: TopicVo) {
  const code = String(row?.topicCode ?? '').trim()
  if (!code) {
    ElMessage.warning('观点编码为空，无法编辑')
    return
  }

  formDialogMode.value = 'edit'
  // 编辑回显：优先使用列表行数据，详情由弹框内部自行补齐
  formDialogEditData.value = row
  formDialogVisible.value = true
}

// 弹窗操作成功回调（刷新列表）
const handleDialogSuccess = async () => {
  if (formDialogMode.value === 'create') {
    await getTopicFirstPageTableData()
    return
  }
  await refreshTopicTableData(false)
}

// 批量合并会改变多条观点状态，成功后刷新当前页并借由表格数据变化清空勾选。
const handleBatchMergeSuccess = async () => {
  await refreshTopicTableData(false)
}

// 查询按钮处理
const handleQuery = () => {
  topicTable.filter = buildTopicListFilter()
  void getTopicFirstPageTableData()
}

// 重置按钮处理
const handleReset = () => {
  // 表单重置：一次性恢复初始值，避免逐字段置空带来的多次响应更新
  Object.assign(filterForm, cloneDeep(initialFilterForm))

  topicTable.filter = buildTopicListFilter()
  void getTopicFirstPageTableData()
}

// 页面挂载时初始化数据
onMounted(async () => {
  topicTable.filter = buildTopicListFilter()
  await getTopicFirstPageTableData()
})
</script>

<template>
  <div class="standard-point-page flex-col h-full">
    <FtCard title="筛选条件" model="titleOperation" clear-content-top-padding>
      <FFilterLayout
        v-model="isExpanded"
        collapsed-text="更多"
        expanded-text="收起"
        @query="handleQuery"
        @reset="handleReset"
      >
        <el-form :model="filterForm" layout="inline" class="custom-form">
          <el-row class="w-full" :gutter="0">
            <el-col :span="6">
              <el-form-item label="观点名称">
                <el-input
                  v-model.trim="filterForm.topicName"
                  placeholder="请输入"
                  :maxlength="50"
                  clearable
                />
              </el-form-item>
            </el-col>

            <el-col :span="3">
              <el-form-item label="情感">
                <el-select
                  v-model="filterForm.emotions"
                  placeholder="不限"
                  collapse-tags
                  collapse-tags-tooltip
                  multiple
                  clearable
                >
                  <el-option
                    v-for="item in vocSentimentOptions"
                    :key="item.key"
                    :label="item.value"
                    :value="item.key"
                  />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="3">
              <el-form-item label="意图">
                <el-select
                  v-model="filterForm.intentions"
                  placeholder="不限"
                  collapse-tags
                  collapse-tags-tooltip
                  multiple
                  clearable
                >
                  <el-option
                    v-for="item in vocIntentionOptions"
                    :key="item.key"
                    :label="item.value"
                    :value="item.key"
                  />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <el-form-item label="操作人/创建人">
                <el-input
                  v-model.trim="filterForm.operateUser"
                  placeholder="请输入"
                  :maxlength="50"
                  clearable
                />
              </el-form-item>
            </el-col>

            <el-col :span="4">
              <el-form-item label="启用状态">
                <el-select v-model="filterForm.tagStatus" placeholder="不限" clearable>
                  <el-option
                    v-for="item in enableTypeOptions"
                    :key="item.key"
                    :label="item.value"
                    :value="item.key"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </FFilterLayout>
    </FtCard>

    <FtCard
      :style="tableCardHeight"
      title="观点列表"
      model="titleOperation"
      clear-content-top-padding
      class="mt-16"
    >
      <template #extra>
        <el-dropdown
          placement="bottom-end"
          :disabled="selectedRows.length === 0"
          @command="handleBatchCommand"
        >
          <el-button :disabled="selectedRows.length === 0">
            批量操作
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="batchEdit">编辑</el-dropdown-item>
              <el-dropdown-item command="batchMerge">合并</el-dropdown-item>
              <el-dropdown-item v-for="item in enableTypeOptions" :key="item.key" :command="item">
                {{ item.value }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button class="ml-8" type="primary" :icon="Plus" @click="openCreateDialog"
          >新建观点</el-button
        >
      </template>

      <div class="table-container">
        <el-auto-resizer>
          <template #default="slotProps">
            <el-table-v2
              v-loading="topicTable.loading"
              :columns="columns"
              :data="topicTable.list || []"
              :width="slotProps.width"
              :height="slotProps.height"
              :row-key="'topicCode'"
              fixed
            />
          </template>
        </el-auto-resizer>
      </div>

      <el-pagination
        v-model:current-page="topicTable.pageNum"
        v-model:page-size="topicTable.pageSize"
        :page-sizes="[10, 20, 50, 100, 200]"
        :total="topicTable.total"
        layout="total, sizes, prev, pager, next"
        @size-change="handleTopicSizeChange"
        @current-change="handleTopicCurrentChange"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </FtCard>

    <StandardPointFormDialog
      v-model:visible="formDialogVisible"
      :mode="formDialogMode"
      :edit-data="formDialogEditData"
      :conditions="conditions"
      @success="handleDialogSuccess"
    />

    <BatchMergeDialog
      v-model:visible="batchMergeVisible"
      :selected-topics="selectedRows"
      @success="handleBatchMergeSuccess"
    />

    <AppDialog
      v-model:visible="batchData.visible"
      :title="batchData.title"
      width="480px"
      :confirm="handleBatchStatusConfirm"
    >
      <div class="batch-status-content">
        <SvgIcon name="info-circle-filled" style="width: 20px; height: 20px" color="#1677ff" />
        <div class="batch-status-content__text">{{ batchData.messages || '' }}</div>
      </div>
    </AppDialog>
  </div>
</template>

<style scoped lang="scss">
.standard-point-page {
  display: flex;
}

.table-container {
  height: calc(100% - 48px);
}

:deep(.text-ellipsis) {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:deep(.status-wrapper) {
  display: flex;
  align-items: center;
}

:deep(.status-circle) {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

:deep(.success-bg) {
  background-color: var(--color-success);
}

:deep(.forbidden-bg) {
  background-color: #c9cdd4;
}

.batch-status-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.batch-status-content__text {
  font-size: 14px;
  color: #4e5969;
  line-height: 22px;
}
</style>
