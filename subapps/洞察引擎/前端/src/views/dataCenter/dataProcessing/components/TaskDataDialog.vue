<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import request from '@/api'

type DataStage = 'raw' | 'clean' | 'result'

interface DataProcessingTask {
  batchId?: string
  dataSourceId?: string
  taskName?: string
  dataSourceName?: string
  completedCount?: number
  totalCount?: number
  status?: string
  availableDataStages?: DataStage[]
  resultDataAvailable?: boolean
}

interface TableColumn {
  label: string
  dataKeys: string[]
  width?: number
  minWidth?: number
  fixed?: 'left' | 'right'
}

const props = defineProps<{
  modelValue: boolean
  task: DataProcessingTask | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const visible = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
})

const stageDefinitions: Array<{ name: DataStage; label: string; url: string }> = [
  { name: 'raw', label: '原始数据', url: '/insights/insCqCaDataSource/getRawData' },
  { name: 'clean', label: '清洗数据', url: '/insights/insCqCaDataSource/getCleanData' },
  { name: 'result', label: '结果数据', url: '/insights/insCqCaDataSource/getResultData' }
]

const rawColumns: TableColumn[] = [
  { label: '原声ID', dataKeys: ['dataId', 'rawDataId', 'originalId'], width: 190, fixed: 'left' },
  { label: '标题', dataKeys: ['title'], minWidth: 220 },
  { label: '内容', dataKeys: ['content', 'originalText'], minWidth: 360 },
  { label: '一级内容类型', dataKeys: ['firstContentType', 'contentType'], width: 140 },
  { label: '二级内容类型', dataKeys: ['secondContentType'], width: 140 },
  { label: '一级渠道分类', dataKeys: ['firstChannelName', 'isOuter'], width: 140 },
  { label: '二级渠道分类', dataKeys: ['secondChannelName'], width: 140 },
  { label: '渠道名称', dataKeys: ['channelName'], width: 120 },
  { label: '发布时间', dataKeys: ['dataCreateTime', 'publishTime'], width: 180 },
  { label: '发声用户昵称', dataKeys: ['authorNick'], width: 150 },
  { label: '发声用户ID', dataKeys: ['authorId'], width: 150 },
  { label: '品牌', dataKeys: ['brand', 'brandName'], width: 120 },
  { label: '车系', dataKeys: ['series', 'carSeriesName'], width: 140 },
  { label: '车型', dataKeys: ['model', 'modelName'], width: 120 },
  { label: '内容权重', dataKeys: ['weight'], width: 110 }
]

const cleanColumns: TableColumn[] = [
  ...rawColumns,
  { label: '清洗后ID', dataKeys: ['cleanDataId'], width: 190 },
  { label: '清洗时间', dataKeys: ['cleanTime'], width: 180 },
  { label: '命中规则', dataKeys: ['hitRule'], width: 180 },
  { label: '数据状态', dataKeys: ['dataStatus'], width: 120, fixed: 'right' }
]

const resultColumns: TableColumn[] = [
  { label: '声音片段ID', dataKeys: ['id', 'soundsId'], width: 190, fixed: 'left' },
  { label: '原声ID', dataKeys: ['dataId', 'originalId'], width: 190 },
  { label: '标题', dataKeys: ['title'], minWidth: 220 },
  { label: '声音片段', dataKeys: ['originalTextScene'], minWidth: 220 },
  { label: '原始观点', dataKeys: ['opinion'], minWidth: 220 },
  { label: '标准观点', dataKeys: ['topicText'], width: 160 },
  { label: '情感', dataKeys: ['sentiment'], width: 90 },
  { label: '意图', dataKeys: ['intention'], width: 90 },
  { label: '用车场景', dataKeys: ['usageScenarioSecond', 'usageScenarioFirst'], width: 150 },
  { label: '标签体系', dataKeys: ['domTagFour', 'domTagThree', 'domTagSecond'], width: 180 },
  { label: '用户旅程', dataKeys: ['userJourney4', 'userJourney3', 'userJourney2'], width: 160 },
  { label: '品牌', dataKeys: ['brandName'], width: 120 },
  { label: '车系', dataKeys: ['carSeriesName'], width: 140 },
  { label: '车型', dataKeys: ['modelName'], width: 120 },
  { label: '渠道名称', dataKeys: ['channelName'], width: 120 },
  { label: '发布时间', dataKeys: ['publishTime', 'dataCreateTime'], width: 180 },
  { label: '数据状态', dataKeys: ['dataStatus'], width: 120, fixed: 'right' }
]

const activeStage = ref<DataStage>('raw')
const requestSequence = ref(0)
const table = reactive({
  loading: false,
  list: [] as Record<string, any>[],
  total: 0,
  pageNum: 1,
  pageSize: 10
})

const availableStages = computed<DataStage[]>(() => {
  const configured = props.task?.availableDataStages?.filter(stage =>
    stageDefinitions.some(item => item.name === stage)
  )
  if (configured?.length) return configured
  if (props.task?.status === '2') return ['raw', 'clean', 'result']
  if (props.task?.status === '-1') {
    return props.task.resultDataAvailable ? ['raw', 'clean', 'result'] : ['raw', 'clean']
  }
  return []
})

const visibleStageDefinitions = computed(() =>
  stageDefinitions.filter(item => availableStages.value.includes(item.name))
)

const currentColumns = computed(() => {
  if (activeStage.value === 'clean') return cleanColumns
  if (activeStage.value === 'result') return resultColumns
  return rawColumns
})

const dialogTitle = computed(() =>
  props.task?.taskName ? `任务数据 - ${props.task.taskName}` : '任务数据'
)

const statusLabel = computed(
  () =>
    ({ '0': '待处理', '1': '处理中', '2': '已完成', '-1': '处理失败' }[props.task?.status || '0'] ||
    '待处理')
)

function resolveCellValue(row: Record<string, any>, dataKeys: string[]) {
  for (const key of dataKeys) {
    const value = row?.[key]
    if (value !== undefined && value !== null && String(value).trim() !== '') {
      return String(value)
    }
  }
  return '-'
}

async function loadTaskData() {
  const stage = stageDefinitions.find(item => item.name === activeStage.value)
  if (!visible.value || !props.task?.batchId || !stage) return

  const sequence = requestSequence.value + 1
  requestSequence.value = sequence
  table.loading = true
  try {
    const response = await request<any>({
      method: 'POST',
      url: stage.url,
      data: {
        batchId: props.task.batchId,
        dataSourceId: props.task.dataSourceId,
        pageNum: table.pageNum,
        pageSize: table.pageSize
      }
    })
    if (sequence !== requestSequence.value) return
    const result = response?.result || {}
    table.list = result.list || result.records || []
    table.total = Number(result.total || 0)
  } catch {
    if (sequence !== requestSequence.value) return
    table.list = []
    table.total = 0
  } finally {
    if (sequence === requestSequence.value) {
      table.loading = false
    }
  }
}

function changePage(page: number) {
  table.pageNum = page
  loadTaskData()
}

function changePageSize(pageSize: number) {
  table.pageNum = 1
  table.pageSize = pageSize
  loadTaskData()
}

watch(
  [() => props.modelValue, () => props.task?.batchId],
  ([isVisible]) => {
    if (!isVisible) return
    activeStage.value = availableStages.value[0] || 'raw'
    table.pageNum = 1
    table.list = []
    table.total = 0
    loadTaskData()
  },
  { flush: 'post' }
)

watch(activeStage, () => {
  if (!visible.value) return
  table.pageNum = 1
  table.list = []
  table.total = 0
  loadTaskData()
})
</script>

<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="92vw"
    top="5vh"
    destroy-on-close
    class="task-data-dialog"
  >
    <div class="task-data-dialog__summary">
      <span>数据源：{{ task?.dataSourceName || '-' }}</span>
      <span>任务进度：{{ task?.completedCount || 0 }} / {{ task?.totalCount || 0 }}</span>
      <span>任务状态：{{ statusLabel }}</span>
    </div>

    <el-tabs v-model="activeStage" class="task-data-dialog__tabs">
      <el-tab-pane
        v-for="stage in visibleStageDefinitions"
        :key="stage.name"
        :name="stage.name"
        :label="stage.label"
      />
    </el-tabs>

    <el-table
      v-loading="table.loading"
      :data="table.list"
      height="calc(72vh - 150px)"
      border
      empty-text="当前任务暂无该阶段数据"
    >
      <el-table-column
        v-for="column in currentColumns"
        :key="`${activeStage}-${column.label}`"
        :label="column.label"
        :width="column.width"
        :min-width="column.minWidth"
        :fixed="column.fixed"
        show-overflow-tooltip
      >
        <template #default="{ row }">
          {{ resolveCellValue(row, column.dataKeys) }}
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="table.pageNum"
      v-model:page-size="table.pageSize"
      :page-sizes="[10, 20, 50, 100]"
      :total="table.total"
      layout="total, sizes, prev, pager, next"
      class="task-data-dialog__pagination"
      @current-change="changePage"
      @size-change="changePageSize"
    />
  </el-dialog>
</template>

<style scoped lang="scss">
.task-data-dialog {
  &__summary {
    display: flex;
    align-items: center;
    gap: 32px;
    min-height: 44px;
    padding: 0 16px;
    color: #4e5969;
    background: #f7f8fa;
    border-radius: 4px;
  }

  &__tabs {
    margin-top: 8px;
  }

  &__pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
}

:global(.task-data-dialog .el-dialog__body) {
  padding-top: 12px;
}
</style>
