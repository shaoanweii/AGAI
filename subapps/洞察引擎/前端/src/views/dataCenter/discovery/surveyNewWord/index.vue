<script setup lang="ts">
import { useTable } from '@/hooks/table'
import useConditions from '@/hooks/useConditions'
import { computedCardHeight } from '@/utils'
import { getTagLibStandardView } from '@/api/rules'
import NewWordDetailsDialog from '@/views/dataCenter/discovery/NewWordDetailsDialog.vue'
import AppDialog from '@/components/AppDialog.vue'
import { batchUpdateNewWords, updateNewWord } from '@/api/newWords'
import { createOpinionSynonym } from '@/api/opinionSynonyms'
import { DATA_KNOWLEDGE_API_KEY_HEADER } from '@/api/constants'
import { getYesterdayDateRange, normalizeDateRangeOrNull } from '@/utils/dateRange'
import { getLoginUserMetaOrNull } from '@/utils/loginUserMeta'
import { ElMessage } from 'element-plus'
import { NEW_WORD_DETAILS_DIALOG_TAB } from '../constants'

defineOptions({
  name: 'DataCenterDiscoverySurveyNewWord'
})

interface Props {
  opinionType?: 0 | 1
}

const props = withDefaults(defineProps<Props>(), {
  opinionType: 0
})

const { conditions } = useConditions({
  url: '/insights/addLabel/conditions',
  headers: { ...DATA_KNOWLEDGE_API_KEY_HEADER }
})
provide('conditions', conditions)

interface StandardOpinionOption {
  tagName: string
  tagCode: string
  [key: string]: any
}

type ProcessStatus = 'all' | -1 | 0 | 1

// 标准观点下拉选项（接口返回原始结构，统一使用 tagName / tagCode）
const standardOpinionOptions = ref<StandardOpinionOption[]>([])
const standardOpinionLoading = ref(false)

const MAX_TIME_RANGE_DAYS = 7

type TimeRangeValue = [string, string] | null

const yesterdayDateRange = getYesterdayDateRange()
const latestSelectableDate = yesterdayDateRange[1]

const TIME_RANGE_REQUIRED_MESSAGE = '时间范围不能为空'
const DISCOVERY_RECOMMEND_OPINION_DROPDOWN_WIDTH = 600

const queryForm = reactive<{
  subject_desc: string
  operatorKeyword: string
  standard_opinion: string
  process_status: ProcessStatus
  time_range: TimeRangeValue
}>({
  subject_desc: '',
  operatorKeyword: '',
  standard_opinion: '',
  process_status: 'all' as ProcessStatus,
  time_range: yesterdayDateRange
})

const normalizeRequiredTimeRange = (raw: unknown): [string, string] => {
  const range = normalizeDateRangeOrNull(raw, { maxRangeDays: MAX_TIME_RANGE_DAYS })
  if (!range) {
    throw new Error(TIME_RANGE_REQUIRED_MESSAGE)
  }
  return range
}

const fetchStandardOpinions = async () => {
  standardOpinionLoading.value = true
  try {
    const resp = await getTagLibStandardView({
      tagType: 'CA',
      tagParentId: ''
    })
    const list = Array.isArray(resp?.result) ? resp.result : []
    standardOpinionOptions.value = list.filter((item: any) => !!item && !!item.tagCode)
  } catch (error) {
    standardOpinionOptions.value = []
  } finally {
    standardOpinionLoading.value = false
  }
}

// 列表数据归一化：按类型兼容文本语料与问卷语料
const normalizeCorpusMappingItem = (item: any) => {
  const opinionType = props.opinionType ?? 1
  if (opinionType === 0) {
    return {
      ...item,
      subject: item.opinion ?? item.subject ?? item.description ?? item.entity ?? item.full_opinion,
      subject_desc: item.description ?? item.subject_desc ?? item.full_opinion,
      standardOpinion: item.standard_opinion ?? item.recommended_topic
    }
  }
  return {
    ...item,
    subject: item.entity ?? item.full_opinion,
    subject_desc: item.description ?? item.full_opinion,
    standardOpinion: item.standard_opinion ?? item.recommended_topic
  }
}

const normalizeLooseString = (value: any) => {
  if (value === undefined || value === null) return ''
  return String(value).trim()
}

const resolveSynonymOpinionText = (row: any) => {
  return row?.opinion || row?.subject || row?.description || row?.full_opinion || ''
}

const intersectStringSets = (left: Set<string>, right: Set<string>) => {
  const next = new Set<string>()
  left.forEach(value => {
    if (right.has(value)) next.add(value)
  })
  return next
}

const findCommonRecommendTopicId = (rows: any[]) => {
  if (!Array.isArray(rows) || rows.length === 0) return null

  let commonIds: Set<string> | null = null

  for (const row of rows) {
    const list = Array.isArray(row?.recommend_topic_list) ? row.recommend_topic_list : []
    const idsThis = new Set<string>()

    for (const item of list) {
      if (!item) continue
      const id = normalizeLooseString(item.topic_id ?? item.topicId)
      if (!id) continue
      idsThis.add(id)
    }

    commonIds = commonIds ? intersectStringSets(commonIds, idsThis) : idsThis
    if (commonIds.size === 0) return null
  }

  if (!commonIds || commonIds.size === 0) return null
  return commonIds.values().next().value || null
}

const {
  table,
  handleReset,
  getTableData,
  getFirstPageTableData,
  refreshTableData,
  handleCurrentChange,
  handleSizeChange
} = useTable(
  {
    method: 'POST',
    url: '/new-words/search',
    timeout: 0,
    pageKey: 'page',
    pageSizeKey: 'page_size',
    headers: { ...DATA_KNOWLEDGE_API_KEY_HEADER }
  },
  res => {
    const result = res.result || {}
    const items = Array.isArray(result.items) ? result.items : []
    const list = items.map(normalizeCorpusMappingItem)
    return {
      list,
      total: result.total ?? 0
    }
  }
)

const syncTableFilters = (timeRange: [string, string]) => {
  const nextFilter: any = {}
  if (queryForm.subject_desc) {
    nextFilter.full_opinion = queryForm.subject_desc
  }
  if (queryForm.operatorKeyword) {
    nextFilter.operator = queryForm.operatorKeyword
  }
  if (props.opinionType === 1 && queryForm.standard_opinion) {
    nextFilter.recommend_topic_text = queryForm.standard_opinion
  }
  if (queryForm.process_status !== 'all') {
    nextFilter.status = queryForm.process_status
  }
  nextFilter.start_date = timeRange[0]
  nextFilter.end_date = timeRange[1]
  nextFilter.opinion_type = props.opinionType
  table.filter = nextFilter
}

const query = () => {
  try {
    const timeRange = normalizeRequiredTimeRange(queryForm.time_range)
    syncTableFilters(timeRange)
    getFirstPageTableData()
  } catch (error: any) {
    ElMessage.warning(error?.message || TIME_RANGE_REQUIRED_MESSAGE)
  }
}

const reset = () => {
  queryForm.subject_desc = ''
  queryForm.operatorKeyword = ''
  queryForm.standard_opinion = ''
  queryForm.process_status = 'all'
  queryForm.time_range = getYesterdayDateRange()
  handleReset(() => {
    const timeRange = normalizeRequiredTimeRange(queryForm.time_range)
    syncTableFilters(timeRange)
  })
}

onMounted(() => {
  query()
  fetchStandardOpinions()
})

const newWordStatusLabelMap: Record<string, string> = {
  '-1': '待处理',
  '1': '已添加',
  '0': '已弃用'
}

const getEnableStatusText = (row: any) => {
  const rawStatus = row.status ?? row.enable_status ?? row.auditStatusCode ?? row.auditStatus ?? ''
  const statusKey = `${rawStatus}`
  return newWordStatusLabelMap[statusKey] || '-'
}

const getEnableStatusTagStyle = (row: any) => {
  const text = getEnableStatusText(row)
  let color = ''
  switch (text) {
    case '已添加':
      color = '#00B42A'
      break
    case '已弃用':
      color = '#C9CDD4'
      break
    case '待处理':
      color = '#1677FF'
      break
    default:
      color = '#C9CDD4'
      break
  }
  return { backgroundColor: color }
}

const getSubjectText = (row: any) => row.subject ?? row.entity ?? row.full_opinion ?? '-'
const getFrequencyText = (row: any) => row.frequency ?? row.freq ?? row.count ?? '-'

const buildLastOperatorName = (item: any) => {
  const history = Array.isArray(item?.modification_history) ? item.modification_history : []
  if (!history.length) return ''

  for (let index = history.length - 1; index >= 0; index -= 1) {
    const record = history[index]
    if (record && record.username) {
      return String(record.username)
    }
  }

  return '系统操作'
}

const getOperatorText = (row: any) => {
  if (row && typeof row.last_operator === 'string' && row.last_operator) {
    return row.last_operator
  }
  if (row && typeof row.username === 'string' && row.username) {
    return row.username
  }
  const lastOperator = buildLastOperatorName(row)
  return lastOperator || '-'
}

// 文案：根据语料类型切换问卷语料/文本语料展示
const filterSubjectLabel = computed(() => '规则描述')
const filterSubjectPlaceholder = computed(() => '请输入规则描述')
const subjectColumnLabel = computed(() => '规则描述')

const operatorColumnLabel = computed(() => '操作人')

const listCardTitle = computed(() => '新词列表')

const timeRangeForDialog = computed(() =>
  Array.isArray(queryForm.time_range) ? queryForm.time_range : []
)

const selectedRows = ref<any[]>([])
const handleSelectionChange = (rows: any[]) => {
  selectedRows.value = rows
}

const isPendingRow = (row: any) => {
  const rawStatus = row.status ?? row.enable_status ?? row.auditStatusCode ?? row.auditStatus ?? ''
  return String(rawStatus) === '-1'
}

const getSelectedRowsForBatchDiscardOrNull = () => {
  if (!selectedRows.value.length) {
    ElMessage.warning('请先选择语料')
    return null
  }

  const pendingRows = selectedRows.value.filter(row => isPendingRow(row))
  if (!pendingRows.length) {
    ElMessage.warning('选中的语料均已处理，无需重复弃用')
    return null
  }
  if (pendingRows.length !== selectedRows.value.length) {
    ElMessage.warning('批量弃用仅支持待处理状态的数据')
    return null
  }

  return pendingRows
}

const batchStatusOptions = [
  { label: '添加', value: 1 },
  { label: '弃用', value: 0 }
]

const batchDialogVisible = ref(false)
const batchDialogMode = ref<'enable' | 'discard'>('enable')
const batchEnableRows = ref<any[]>([])
const batchMappingOpinion = ref('')

const batchMappingOptions = computed(() => standardOpinionOptions.value || [])

const handleBatchChange = (command: string | number) => {
  if (!selectedRows.value.length) {
    ElMessage.warning('请先选择语料')
    return
  }
  if (Number(command) === 1) {
    batchDialogMode.value = 'enable'
    const pendingList = selectedRows.value.filter(row => isPendingRow(row))
    if (!pendingList.length) {
      ElMessage.warning('选中的语料均已处理，无需重复添加')
      return
    }
    batchEnableRows.value = pendingList
    batchMappingOpinion.value = ''
    const commonTopicId = findCommonRecommendTopicId(batchEnableRows.value)
    if (commonTopicId) {
      const matchedOption = batchMappingOptions.value.find(
        item => normalizeLooseString(item.tagCode) === normalizeLooseString(commonTopicId)
      )
      if (matchedOption) {
        batchMappingOpinion.value = matchedOption.tagName
      }
    }
    batchDialogVisible.value = true
    return
  }
  if (Number(command) === 0) {
    const discardRows = getSelectedRowsForBatchDiscardOrNull()
    if (!discardRows) return
    batchDialogMode.value = 'discard'
    batchDialogVisible.value = true
  }
}

const handleBatchConfirm = async ({ close }: { close: () => void }) => {
  if (!selectedRows.value.length) {
    ElMessage.warning('请先选择语料')
    return
  }

  const loginMeta = getLoginUserMetaOrNull()
  if (!loginMeta) {
    ElMessage.error('缺少登录用户信息，无法批量处理')
    return
  }

  const isEnableMode = batchDialogMode.value === 'enable'

  try {
    if (isEnableMode) {
      const rows = batchEnableRows.value.length
        ? batchEnableRows.value
        : selectedRows.value.filter(row => isPendingRow(row))

      if (!rows.length) {
        ElMessage.warning('没有可添加的待处理语料')
        return
      }

      const ids = rows.map(row => row?.id).filter(id => id !== undefined && id !== null)
      if (!ids.length) {
        ElMessage.warning('没有可处理的数据')
        return
      }
      if (ids.length > 1000) {
        ElMessage.warning('批量处理一次最多支持 1000 条数据')
        return
      }

      const matched = batchMappingOptions.value.find(
        item => item.tagName === batchMappingOpinion.value
      )
      if (!matched) {
        ElMessage.error('请选择有效的映射观点')
        return
      }

      const tasks: Promise<any>[] = []

      rows.forEach(row => {
        if (!row || row.id === undefined || row.id === null) {
          return
        }
        const statusValue = 1
        const payload: any = {
          id: row.id,
          status: statusValue,
          selected_topic_id: matched.tagCode,
          selected_topic_text: matched.tagName,
          username: loginMeta.operatorName,
          user_id: loginMeta.employeeId
        }

        tasks.push(
          updateNewWord(payload).then(() => {
            const synonymPayload: any = {
              standard_opinion: matched.tagName,
              standard_opinion_id: String(matched.tagCode),
              status_name: 'enabled',
              opinion_type: 0,
              opinion: resolveSynonymOpinionText(row),
              operator: loginMeta.operatorName
            }
            return createOpinionSynonym(synonymPayload)
          })
        )
      })

      if (!tasks.length) {
        ElMessage.warning('没有可处理的数据')
        return
      }

      await Promise.all(tasks)
      ElMessage.success('批量添加成功')
    } else {
      const rows = getSelectedRowsForBatchDiscardOrNull()
      if (!rows) return

      const ids = rows.map(row => row?.id).filter(id => id !== undefined && id !== null)
      if (!ids.length) {
        ElMessage.warning('没有可处理的数据')
        return
      }
      if (ids.length > 1000) {
        ElMessage.warning('批量处理一次最多支持 1000 条数据')
        return
      }

      const resp: any = await batchUpdateNewWords({
        ids,
        operation: 'disable',
        username: loginMeta.operatorName,
        user_id: loginMeta.employeeId
      })

      const result = resp?.result || {}
      const failedCount = Number(result.failed_count ?? 0)
      const successCount = Number(result.success_count ?? 0)

      if (failedCount > 0) {
        ElMessage.warning(`批量更新完成，成功${successCount}条，失败${failedCount}条`)
      } else {
        ElMessage.success('批量弃用成功')
      }
    }

    close()
    selectedRows.value = []
    batchEnableRows.value = []
    refreshTableData()
  } catch (error: any) {
    ElMessage.error(error?.message || (isEnableMode ? '批量添加失败' : '批量弃用失败'))
  }
}

const detailDialogVisible = ref(false)
const detailRow = ref<any | null>(null)
const detailDialogRef = ref<any | null>(null)

const openHandleDialog = (row: any) => {
  detailRow.value = row
  if (detailDialogRef.value && typeof detailDialogRef.value.setDefaultActiveTab === 'function') {
    detailDialogRef.value.setDefaultActiveTab(NEW_WORD_DETAILS_DIALOG_TAB.OPERATION)
  }
  detailDialogVisible.value = true
}

const openViewDialog = (row: any) => {
  detailRow.value = row
  if (detailDialogRef.value && typeof detailDialogRef.value.setDefaultActiveTab === 'function') {
    detailDialogRef.value.setDefaultActiveTab(NEW_WORD_DETAILS_DIALOG_TAB.DETAILS)
  }
  detailDialogVisible.value = true
}
</script>

<template>
  <div>
    <FtCard title="筛选条件" model="titleOperation" clear-content-top-padding>
      <FFilterLayout class="survey-new-word" @query="query" @reset="reset">
        <el-form layout="inline" :model="queryForm">
          <el-row class="w-full" :gutter="24">
            <el-col :span="8">
              <el-form-item label="时间范围">
                <FDatePicker
                  v-model="queryForm.time_range"
                  :max-range-days="MAX_TIME_RANGE_DAYS"
                  :max-selectable-date="latestSelectableDate"
                  :shortcuts="[]"
                  :clearable="true"
                  class="w-full"
                />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item :label="filterSubjectLabel">
                <el-input
                  v-model.trim="queryForm.subject_desc"
                  :placeholder="filterSubjectPlaceholder"
                  maxlength="100"
                  clearable
                />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="操作人">
                <el-input
                  v-model.trim="queryForm.operatorKeyword"
                  placeholder="请输入操作人"
                  maxlength="50"
                  clearable
                />
              </el-form-item>
            </el-col>

            <el-col v-if="props.opinionType === 1" :span="6">
              <el-form-item label="推荐观点">
                <el-select-v2
                  v-model="queryForm.standard_opinion"
                  :options="standardOpinionOptions"
                  :loading="standardOpinionLoading"
                  :props="{ label: 'tagName', value: 'tagName' }"
                  placeholder="请选择推荐观点"
                  filterable
                  clearable
                  class="w-full"
                  placement="bottom"
                  :fit-input-width="600"
                />
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="处理状态">
                <el-select v-model="queryForm.process_status" placeholder="请选择处理状态">
                  <el-option label="不限" value="all" />
                  <el-option label="待处理" :value="-1" />
                  <el-option label="已弃用" :value="0" />
                  <el-option label="已添加" :value="1" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </FFilterLayout>
    </FtCard>

    <FtCard
      :style="computedCardHeight(220)"
      :title="listCardTitle"
      model="titleOperation"
      clear-content-top-padding
      class="mt-24"
    >
      <template #extra>
        <el-dropdown trigger="click" placement="bottom-end" @command="handleBatchChange">
          <el-button text bg :disabled="!selectedRows.length">批量操作</el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item
                v-for="opt in batchStatusOptions"
                :key="opt.value"
                :command="opt.value"
              >
                {{ opt.label }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </template>
      <el-table
        v-loading="table.loading"
        :data="table.list"
        class="corpus-mapping-table"
        style="width: 100%; height: 90%"
        :height="'90%'"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="序号" type="index" width="56" align="center" />
        <el-table-column :label="subjectColumnLabel" show-overflow-tooltip>
          <template #default="{ row }">
            {{ getSubjectText(row) }}
          </template>
        </el-table-column>
        <el-table-column label="频率" width="100">
          <template #default="{ row }">
            {{ getFrequencyText(row) }}
          </template>
        </el-table-column>
        <el-table-column label="处理时间" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.processed_time }}
          </template>
        </el-table-column>
        <el-table-column :label="operatorColumnLabel" show-overflow-tooltip>
          <template #default="{ row }">
            {{ getOperatorText(row) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <div class="flex-y-center">
              <div class="status-icon mr-8" :style="getEnableStatusTagStyle(row)"></div>
              <span>{{ getEnableStatusText(row) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openViewDialog(row)"> 查看 </el-button>
            <el-button
              link
              type="primary"
              :disabled="!isPendingRow(row)"
              @click="openHandleDialog(row)"
            >
              处理
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="table.total > 0"
        v-model:current-page="table.pageNum"
        v-model:page-size="table.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="table.total"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        class="mt-16 flex justify-end"
      />
    </FtCard>

    <NewWordDetailsDialog
      ref="detailDialogRef"
      v-model:visible="detailDialogVisible"
      :row="detailRow"
      :time-range="timeRangeForDialog"
      :opinion-type="props.opinionType"
      @refresh="getTableData(false)"
    />

    <AppDialog
      v-model:visible="batchDialogVisible"
      :title="batchDialogMode === 'enable' ? '批量添加' : '批量弃用'"
      width="480px"
      :confirm="handleBatchConfirm"
    >
      <div v-if="batchDialogMode === 'enable'">
        <el-form label-width="80px">
          <el-form-item label="映射观点">
            <el-select-v2
              v-model="batchMappingOpinion"
              :options="batchMappingOptions"
              :loading="standardOpinionLoading"
              :props="{ label: 'tagName', value: 'tagName' }"
              placeholder="请选择映射观点"
              filterable
              clearable
              class="w-full"
              placement="bottom"
              popper-class="discovery-recommend-opinion-popper"
              :fit-input-width="DISCOVERY_RECOMMEND_OPINION_DROPDOWN_WIDTH"
            />
          </el-form-item>
        </el-form>
      </div>
      <div v-else>是否确认批量弃用选中语料？</div>
    </AppDialog>
  </div>
</template>

<style lang="scss" scoped>
.survey-new-word .el-form-item {
  margin-bottom: 0;
}
.corpus-mapping-table {
  :deep(.el-table__cell) {
    height: 55px;
    padding-top: 0;
    padding-bottom: 0;
  }
}
</style>

<style lang="scss">
.discovery-recommend-opinion-popper {
  width: 600px !important;
  max-width: calc(100vw - 40px);
}
</style>
