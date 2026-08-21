<script setup lang="ts">
import { useTable } from '@/hooks/table'
import useConditions from '@/hooks/useConditions'
import { computedCardHeight } from '@/utils'
import { getTagLibStandardView } from '@/api/rules'
import { updateNewWord } from '@/api/newWords'
import NewWordDetailsDialog from '@/views/dataCenter/discovery/NewWordDetailsDialog.vue'
import AppDialog from '@/components/AppDialog.vue'
import { createOpinionSynonym } from '@/api/opinionSynonyms'
import { DATA_KNOWLEDGE_API_KEY_HEADER } from '@/api/constants'
import { getLoginUserMetaOrNull } from '@/utils/loginUserMeta'
import { ElMessage } from 'element-plus'
import { getYesterdayDateRange, normalizeDateRangeOrNull } from '@/utils/dateRange'
import { NEW_WORD_DETAILS_DIALOG_TAB } from '../constants'

defineOptions({
  name: 'DataCenterDiscoveryTextNewWord'
})

interface Props {
  opinionType?: 0 | 1
}

const props = withDefaults(defineProps<Props>(), {
  opinionType: 1
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
  subjectKeyword: string
  descriptionKeyword: string
  originalOpinionKeyword: string
  titleKeyword: string
  contentKeyword: string
  operatorKeyword: string
  standard_opinion: string
  process_status: ProcessStatus
  time_range: TimeRangeValue
}>({
  subjectKeyword: '',
  descriptionKeyword: '',
  originalOpinionKeyword: '',
  titleKeyword: '',
  contentKeyword: '',
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

const {
  table,
  handleReset,
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
  if (queryForm.subjectKeyword) {
    nextFilter.entity = queryForm.subjectKeyword
  }
  if (queryForm.descriptionKeyword) {
    nextFilter.description = queryForm.descriptionKeyword
  }
  if (queryForm.originalOpinionKeyword) {
    nextFilter.opinion = queryForm.originalOpinionKeyword
  }
  if (queryForm.titleKeyword) {
    nextFilter.title = queryForm.titleKeyword
  }
  if (queryForm.contentKeyword) {
    nextFilter.content = queryForm.contentKeyword
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

const timeRangeForDialog = computed(() =>
  Array.isArray(queryForm.time_range) ? queryForm.time_range : []
)

const reset = () => {
  queryForm.subjectKeyword = ''
  queryForm.descriptionKeyword = ''
  queryForm.originalOpinionKeyword = ''
  queryForm.titleKeyword = ''
  queryForm.contentKeyword = ''
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
const getRawDataIdText = (row: any) => row.dataId ?? row.rawDataId ?? row.originalId ?? '-'
const getTitleText = (row: any) => row.title ?? row.rawTitle ?? '-'
const getContentText = (row: any) => row.content ?? row.originalText ?? row.full_opinion ?? '-'
const getSoundFragmentText = (row: any) =>
  row.originalTextScene ?? row.soundFragment ?? row.sound_fragment ?? '-'
const getOriginalOpinionText = (row: any) =>
  row.originalOpinion ?? row.original_opinion ?? row.opinion ?? row.full_opinion ?? '-'
const getPublishTimeText = (row: any) =>
  row.publishTime ?? row.publish_time ?? row.created_time ?? '-'
const getProcessTimeText = (row: any) => row.processed_time ?? row.update_time ?? '-'
const getStandardOpinionText = (row: any) =>
  row.standardOpinion ??
  row.standard_opinion ??
  row.recommended_topic ??
  row.standardOpinionText ??
  '-'
const getRecommendTopicsText = (row: any) => {
  const list = Array.isArray(row.recommend_topic_list) ? row.recommend_topic_list : []
  if (!list.length) {
    const legacyText = getStandardOpinionText(row)
    return legacyText || '-'
  }
  const texts = list.map((item: any) => (item && item.topic_text) || '').filter(text => !!text)
  if (!texts.length) {
    const legacyText = getStandardOpinionText(row)
    return legacyText || '-'
  }
  return texts.join(' | ')
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

const getRowOpinionType = (row: any): 0 | 1 => {
  const raw = row?.opinion_type
  return Number(raw) === 0 ? 0 : 1
}

// 文案：根据语料类型切换问卷语料/文本语料展示
const subjectColumnLabel = computed(() => '主体')

const standardOpinionColumnLabel = computed(() => '推荐观点')

const operatorColumnLabel = computed(() => '操作人')

const listCardTitle = computed(() => '新词列表')
const tableCardHeight = computed(() => computedCardHeight(155))

const detailDialogVisible = ref(false)
const detailRow = ref<any | null>(null)

const detailDialogRef = ref<any | null>(null)

const isPendingRow = (row: any) => {
  const rawStatus = row.status ?? row.enable_status ?? row.auditStatusCode ?? row.auditStatus ?? ''
  return String(rawStatus) === '-1'
}

const openHandleDialog = (row: any) => {
  detailRow.value = row
  if (detailDialogRef.value && typeof detailDialogRef.value.setDefaultActiveTab === 'function') {
    detailDialogRef.value.setDefaultActiveTab(NEW_WORD_DETAILS_DIALOG_TAB.OPERATION)
  }
  detailDialogVisible.value = true
}

const selectedRows = ref<any[]>([])
const batchEnableRows = ref<any[]>([])
const handleSelectionChange = (rows: any[]) => {
  selectedRows.value = rows
}

type RecommendTopicListItem = {
  topic_id?: unknown
  topicId?: unknown
  topic_text?: unknown
  topicText?: unknown
  [key: string]: unknown
}

const normalizeLooseString = (raw: unknown): string => {
  if (raw == null) return ''
  if (typeof raw === 'string') return raw.trim()
  if (typeof raw === 'number') return String(raw)
  return ''
}

// 兼容不同接口字段：topic_id/topicId、topic_text/topicText
const normalizeRecommendTopicList = (row: unknown): RecommendTopicListItem[] => {
  const rawList = (row as any)?.recommend_topic_list
  return Array.isArray(rawList) ? (rawList as RecommendTopicListItem[]) : []
}

const intersectStringSets = (left: Set<string>, right: Set<string>) => {
  const next = new Set<string>()
  for (const id of left) {
    if (right.has(id)) next.add(id)
  }
  return next
}

const findCommonRecommendTopic = (rows: any[]) => {
  if (!Array.isArray(rows) || !rows.length) return null
  let commonIds: Set<string> | null = null
  const idToText: Record<string, string> = {}

  for (const row of rows) {
    const list = normalizeRecommendTopicList(row)
    const idsThis = new Set<string>()
    for (const item of list) {
      if (!item) continue
      const id = normalizeLooseString(item.topic_id ?? item.topicId)
      if (!id) continue
      idsThis.add(id)
      if (!idToText[id]) {
        const text = normalizeLooseString(item.topic_text ?? item.topicText)
        if (text) idToText[id] = text
      }
    }

    commonIds = commonIds ? intersectStringSets(commonIds, idsThis) : idsThis
    if (commonIds.size === 0) break
  }

  if (!commonIds || !commonIds.size) return null
  const firstId = commonIds.values().next().value
  if (!firstId) return null
  return {
    topicId: firstId,
    topicText: idToText[firstId] || ''
  }
}

// 批量操作选项
const batchStatusOptions = [
  { label: '添加', value: 1 },
  { label: '弃用', value: 0 }
]

const batchDialogVisible = ref(false)
const batchDialogMode = ref<'enable' | 'discard'>('enable')
const batchMappingOpinion = ref('')
const batchMappingTouched = ref(false)

const batchMappingOptions = computed(() => standardOpinionOptions.value || [])

const handleBatchChange = (command: string | number) => {
  if (!selectedRows.value.length) {
    ElMessage.warning('请先选择语料')
    return
  }
  if (Number(command) === 1) {
    batchDialogMode.value = 'enable'
    // 批量添加时仅处理待处理状态的数据
    const pendingList = selectedRows.value.filter(row => isPendingRow(row))
    if (!pendingList.length) {
      ElMessage.warning('选中的语料均已处理，无需重复添加')
      return
    }
    batchEnableRows.value = pendingList
    // 默认选中：若所有语料存在公共推荐观点，则取该公共观点；否则留空
    batchMappingTouched.value = false
    const common = findCommonRecommendTopic(batchEnableRows.value)
    if (common) {
      const matchedOption = batchMappingOptions.value.find(
        item => String(item.tagCode) === common.topicId || item.tagName === common.topicText
      )
      batchMappingOpinion.value = matchedOption ? matchedOption.tagName : common.topicText
    } else {
      batchMappingOpinion.value = ''
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

const handleBatchMappingChange = () => {
  batchMappingTouched.value = true
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
    const tasks: Promise<any>[] = []

    if (isEnableMode) {
      const rows = batchEnableRows.value.length
        ? batchEnableRows.value
        : selectedRows.value.filter(row => isPendingRow(row))
      if (!rows.length) {
        ElMessage.warning('没有可添加的待处理语料')
        return
      }

      let globalTopicId: string | undefined
      let globalTopicText: string | undefined

      // 用户在弹窗中手动选择了新的映射观点：对所有选中行统一使用该观点
      if (batchMappingTouched.value && batchMappingOpinion.value) {
        const matched = batchMappingOptions.value.find(
          item => item.tagName === batchMappingOpinion.value
        )
        if (!matched) {
          ElMessage.error('请选择有效的映射观点')
          return
        }
        globalTopicId = matched.tagCode
        globalTopicText = matched.tagName
      }

      rows.forEach(row => {
        if (!row || row.id === undefined || row.id === null) {
          return
        }
        const statusValue = 1

        // 未选择统一观点时，按原模型推荐观点为每行单独推导
        let topicId: string | undefined
        let topicText: string | undefined

        if (globalTopicId && globalTopicText) {
          topicId = globalTopicId
          topicText = globalTopicText
        } else {
          const list = Array.isArray(row.recommend_topic_list) ? row.recommend_topic_list : []
          // 用户未选择新的统一观点：默认使用每条数据自己的推荐观点（列表第一条）
          if (list.length >= 1) {
            const first = list[0] || {}
            const idVal = first.topic_id || first.topicId
            const textVal = first.topic_text || first.topicText
            if (idVal && textVal) {
              topicId = String(idVal)
              topicText = String(textVal)
            }
          } else {
            const fallbackText =
              row.standardOpinion ||
              row.standard_opinion ||
              row.recommended_topic ||
              row.standardOpinionText ||
              ''
            const fallbackId = row.standard_opinion_id || row.standardOpinionId || fallbackText

            if (fallbackText && fallbackId) {
              topicId = String(fallbackId)
              topicText = String(fallbackText)
            }
          }
        }

        const payload: any = {
          id: row.id,
          status: statusValue,
          username: loginMeta.operatorName,
          user_id: loginMeta.employeeId
        }
        if (topicId && topicText) {
          payload.selected_topic_id = topicId
          payload.selected_topic_text = topicText
        }

        tasks.push(
          updateNewWord(payload).then(() => {
            // 仅在有明确映射观点时创建同义词
            if (!topicId || !topicText) {
              return
            }
            const opinionType = getRowOpinionType(row)
            const synonymPayload: any = {
              standard_opinion: topicText,
              standard_opinion_id: String(topicId),
              status_name: 'enabled',
              opinion_type: opinionType
            }
            if (opinionType === 0) {
              synonymPayload.opinion =
                row.subject || row.opinion || row.description || row.full_opinion || ''
            } else {
              synonymPayload.entity = row.subject || row.entity || row.full_opinion || ''
              synonymPayload.description =
                row.description || row.subject_desc || row.full_opinion || ''
            }
            return createOpinionSynonym(synonymPayload)
          })
        )
      })
    } else {
      const discardRows = getSelectedRowsForBatchDiscardOrNull()
      if (!discardRows) return

      discardRows.forEach(row => {
        if (!row || row.id === undefined || row.id === null) {
          return
        }
        const statusValue = 0
        tasks.push(
          updateNewWord({
            id: row.id,
            status: statusValue,
            username: loginMeta.operatorName,
            user_id: loginMeta.employeeId
          })
        )
      })
    }

    if (!tasks.length) {
      ElMessage.warning('没有可处理的数据')
      return
    }

    await Promise.all(tasks)

    if (isEnableMode) {
      ElMessage.success('批量添加成功')
    } else {
      ElMessage.success('批量弃用成功')
    }

    close()
    selectedRows.value = []
    batchEnableRows.value = []
    refreshTableData()
  } catch (error: any) {
    const messageText =
      error && error.message ? error.message : isEnableMode ? '批量添加失败' : '批量弃用失败'
    ElMessage.error(messageText)
  }
}
</script>

<template>
  <div class="text-new-word-page">
    <FtCard title="筛选条件" model="titleOperation" clear-content-top-padding>
      <FFilterLayout class="text-new-word" @query="query" @reset="reset">
        <el-form :model="queryForm">
          <el-row class="w-full" :gutter="24">
            <el-col :span="8">
              <el-form-item label="发布时间" class="mb-18">
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
            <el-col :span="8">
              <el-form-item label="主体" class="mb-18">
                <el-input
                  v-model.trim="queryForm.subjectKeyword"
                  placeholder="请输入主体"
                  maxlength="100"
                  clearable
                  class="w-full"
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="描述" class="mb-18">
                <el-input
                  v-model.trim="queryForm.descriptionKeyword"
                  placeholder="请输入描述"
                  maxlength="100"
                  clearable
                  class="w-full"
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="原始观点" class="mb-18">
                <el-input
                  v-model.trim="queryForm.originalOpinionKeyword"
                  placeholder="请输入原始观点"
                  maxlength="100"
                  clearable
                  class="w-full"
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="标准观点" class="mb-18">
                <el-select-v2
                  v-model="queryForm.standard_opinion"
                  :options="standardOpinionOptions"
                  :loading="standardOpinionLoading"
                  :props="{ label: 'tagName', value: 'tagName' }"
                  placeholder="请选择标准观点"
                  filterable
                  clearable
                  class="w-full"
                  placement="bottom"
                  popper-class="discovery-recommend-opinion-popper"
                  :fit-input-width="DISCOVERY_RECOMMEND_OPINION_DROPDOWN_WIDTH"
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="标题" class="mb-18">
                <el-input
                  v-model.trim="queryForm.titleKeyword"
                  placeholder="请输入标题"
                  maxlength="100"
                  clearable
                  class="w-full"
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="内容" class="mb-18">
                <el-input
                  v-model.trim="queryForm.contentKeyword"
                  placeholder="请输入内容"
                  maxlength="100"
                  clearable
                  class="w-full"
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="操作人" class="mb-18">
                <el-input
                  v-model.trim="queryForm.operatorKeyword"
                  placeholder="请输入操作人"
                  maxlength="50"
                  clearable
                  class="w-full"
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="处理状态" class="mb-18">
                <el-select
                  v-model="queryForm.process_status"
                  placeholder="请选择处理状态"
                  class="w-full"
                >
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
      :style="tableCardHeight"
      :title="listCardTitle"
      model="titleOperation"
      clear-content-top-padding
      class="text-new-word-page__card mt-24"
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
      <div class="text-new-word-page__body">
        <div class="text-new-word-page__table-container">
          <el-table
            v-loading="table.loading"
            :data="table.list"
            class="corpus-mapping-table"
            height="100%"
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column label="原声ID" width="160" show-overflow-tooltip>
              <template #default="{ row }">
                {{ getRawDataIdText(row) }}
              </template>
            </el-table-column>
            <el-table-column label="标题" min-width="160" show-overflow-tooltip>
              <template #default="{ row }">
                {{ getTitleText(row) }}
              </template>
            </el-table-column>
            <el-table-column label="内容" min-width="220" show-overflow-tooltip>
              <template #default="{ row }">
                {{ getContentText(row) }}
              </template>
            </el-table-column>
            <el-table-column label="声音片段" min-width="160" show-overflow-tooltip>
              <template #default="{ row }">
                {{ getSoundFragmentText(row) }}
              </template>
            </el-table-column>
            <el-table-column :label="subjectColumnLabel" width="120" show-overflow-tooltip>
              <template #default="{ row }">
                {{ getSubjectText(row) }}
              </template>
            </el-table-column>
            <el-table-column label="原始观点" min-width="160" show-overflow-tooltip>
              <template #default="{ row }">
                {{ getOriginalOpinionText(row) }}
              </template>
            </el-table-column>
            <el-table-column
              :label="standardOpinionColumnLabel"
              min-width="160"
              show-overflow-tooltip
            >
              <template #default="{ row }">
                {{ getRecommendTopicsText(row) }}
              </template>
            </el-table-column>
            <el-table-column label="发布时间" min-width="160" show-overflow-tooltip>
              <template #default="{ row }">
                {{ getPublishTimeText(row) }}
              </template>
            </el-table-column>
            <el-table-column :label="operatorColumnLabel" width="120" show-overflow-tooltip>
              <template #default="{ row }">
                {{ getOperatorText(row) }}
              </template>
            </el-table-column>
            <el-table-column label="处理时间" min-width="160" show-overflow-tooltip>
              <template #default="{ row }">
                {{ getProcessTimeText(row) }}
              </template>
            </el-table-column>
            <el-table-column label="处理状态" width="120">
              <template #default="{ row }">
                <div class="flex-y-center">
                  <div class="status-icon mr-8" :style="getEnableStatusTagStyle(row)"></div>
                  <span>{{ getEnableStatusText(row) }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="90" fixed="right">
              <template #default="{ row }">
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
        </div>

        <div class="text-new-word-page__pagination">
          <el-pagination
            v-if="table.total > 0"
            v-model:current-page="table.pageNum"
            v-model:page-size="table.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="table.total"
            layout="->, total, sizes, prev, pager, next"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </FtCard>

    <NewWordDetailsDialog
      ref="detailDialogRef"
      v-model:visible="detailDialogVisible"
      :row="detailRow"
      :time-range="timeRangeForDialog"
      :opinion-type="props.opinionType"
      @refresh="refreshTableData(false)"
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
              :props="{ label: 'tagName', value: 'tagName' }"
              placeholder="请选择映射观点"
              filterable
              clearable
              class="w-full"
              placement="bottom"
              popper-class="discovery-recommend-opinion-popper"
              :fit-input-width="DISCOVERY_RECOMMEND_OPINION_DROPDOWN_WIDTH"
              @change="handleBatchMappingChange"
            />
          </el-form-item>
        </el-form>
      </div>
      <div v-else>是否确认批量弃用选中语料？</div>
    </AppDialog>
  </div>
</template>

<style lang="scss" scoped>
.text-new-word-page {
  height: 100%;
  overflow: hidden;
}

.text-new-word-page__body {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  min-height: 0;
}

.text-new-word-page__table-container {
  flex: 1;
  min-height: 0;
}

.text-new-word-page__card {
  :deep(.content) {
    min-height: 0;
  }
}

.text-new-word-page__pagination {
  display: flex;
  justify-content: flex-end;
  flex: 0 0 auto;
  margin-top: 16px;
}

.corpus-mapping-table {
  width: 100%;

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
