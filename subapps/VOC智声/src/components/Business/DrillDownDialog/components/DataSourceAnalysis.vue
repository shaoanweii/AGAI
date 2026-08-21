<script setup lang="ts">
// 定义：数据源分析（已切换为无限下钻）
import { computed, onMounted, ref, watch, reactive } from 'vue'
import {
  DEFAULT_MENTION_NEGATIVE_RATE_TYPE,
  MENTION_NEGATIVE_RATE_SWITCH_OPTIONS,
  WORD_CLOUD_RANDOM_COLOR_PALETTE
} from '@/constants'
import { DrillTabKey } from '../constants'
import type { SortChangeEvent, TableHeaderGroup } from './DrillDownTable/types'
import { fmtNum, fmtPer, fmtFix } from '@/utils'
import {
  getChannelTop,
  getDataSourceList,
  getProvinceOpinionEvaluateTop
} from '@/api/drillDownDialog'
import type { ChannelTopVo, DataSourceListVo } from './types'
import CommonTitle from '@components/Business/DrillDownDialog/components/CommonTitle'
import ChannelVoiceRankingBar from '@components/Business/DrillDownDialog/components/ChannelVoiceRankingBar'
import WordCloudChart from '@components/DataSourceAnalysis/WordCloudChart.vue'
import DrillDownTable from '@components/Business/DrillDownDialog/components/DrillDownTable'
import useUserStore from '@/store/modules/user.ts'
import useGeneralDrillDownStore from '@/store/modules/generalDrillDown'

defineOptions({ name: 'DataSourceAnalysis' })

// 父级透传查询参数
const props = defineProps<{ queryParams?: VocQueryParams; filters?: any[] }>()

const userStore = useUserStore()
const ddStore = useGeneralDrillDownStore()

// 加载状态
const loading = reactive({ channelTop: false, dataSourceList: false, evaluateTop: false })

// 内部模式与数据
const innerMode = ref<MentionNegativeRateType>(DEFAULT_MENTION_NEGATIVE_RATE_TYPE)
const channelData = ref<ChannelTopVo[]>([])
const dataSourceList = ref<DataSourceListVo[]>([])
const wordCloudData = ref<any[]>([])

const currentSelectedTopic = ref('')
const currentSelectedChannelCode = ref('')

type DataSourceSortOrder = 'asc' | 'desc'

interface DataSourceTableSortState {
  sortField?: string
  sortOrder?: DataSourceSortOrder
}

// 当前列表排序状态：仅在用户点击表头后透传给后端
const tableSort = reactive<DataSourceTableSortState>({})

// 渠道排行映射
const rankData = computed(() => {
  const list = channelData.value
  const total = list.reduce((sum, item) => sum + (item?.value || 0), 0)
  return list.map(item => ({
    ...item,
    name: item?.channelName ?? '-',
    value: item?.value || 0,
    percent: total > 0 ? ((item?.value || 0) / total) * 100 : 0
  }))
})

const wordCloudOptions = ref<any[]>([
  { text: '全部情感', value: '' },
  ...userStore.getDictItems('voc_sentiment')
])
const wordCloudValue = ref('')
// 记录观点评价TOP最近一次请求参数（含联动条件与 sentiment），用于筛选切换与词云下钻复用
const evaluateTopRequestParams = ref<VocQueryParams | null>(null)
// 请求序号：仅接受最后一次请求的响应，避免并发返回覆盖最新词云状态
const evaluateTopRequestId = ref(0)

/**
 * 组装观点评价TOP查询参数
 * 合并优先级：外部 queryParams < 词云联动上下文 < sentiment（以当前下拉选中为准）
 */
const buildEvaluateTopQueryParams = (extraParams?: VocQueryParams | null): VocQueryParams => {
  return {
    ...(props.queryParams || {}),
    ...(extraParams || {}),
    sentiment: wordCloudValue.value || ''
  }
}

// 表格列
const tableHeaders: TableHeaderGroup[] = [
  {
    key: 'channelName',
    label: '数据来源',
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
        render: (row: any) => fmtPer(row.mentionsShare)
      },
      { key: 'mentionTrend', label: '趋势', width: '160px', render: 'trend' },
      {
        key: 'mentionsMoM',
        label: '环比',
        render: (row: any) => fmtFix(row.mentionsMoM),
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
        render: (row: any) =>
          `<div style="background-color: ${row.rateBackgroundColor}; color: ${row.rateColor}">${fmtPer(row.negativeRateValue)}</div>`,
        sortable: 'custom'
      },
      { key: 'negativeTrend', label: '趋势', width: '160px', render: 'trend' },
      {
        key: 'negativeRateMoM',
        label: '环比',
        render: (row: any) => fmtFix(row.negativeRateMoM),
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
        render: (row: any) => fmtPer(row.positiveRateValue),
        sortable: 'custom'
      },
      { key: 'positiveTrend', label: '趋势', width: '160px', render: 'trend' },
      {
        key: 'positiveRateMoM',
        label: '环比',
        render: (row: any) => fmtFix(row.positiveRateMoM),
        sortable: 'custom'
      }
    ]
  }
]

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
 * 处理数据源列表排序变化，改为由后端完成排序。
 * 排序切换后重新请求列表数据。
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

  await fetchDataSourceList()
}

// 事件
const channelModeChange = (option: any) => {
  innerMode.value = option.value
  fetchChannelTopData()
}

/** 获取渠道发声TOP数据 */
const fetchChannelTopData = async (params?: VocQueryParams) => {
  try {
    loading.channelTop = true
    const qp: VocQueryParams = {
      dataType: innerMode.value,
      ...(props.queryParams || {}),
      ...(params || {})
    }
    const response = await getChannelTop(qp)
    if (response.success && response.result) {
      channelData.value = response.result
    } else {
      channelData.value = []
    }
  } catch (error) {
    console.error('获取渠道发声TOP数据失败:', error)
    channelData.value = []
  } finally {
    loading.channelTop = false
  }
}

/** 获取数据源列表数据 */
const fetchDataSourceList = async (params?: VocQueryParams) => {
  try {
    loading.dataSourceList = true
    const qp: VocQueryParams = {
      ...(props.queryParams || {}),
      ...(params || {}),
      ...(tableSort.sortField && tableSort.sortOrder
        ? {
            sortField: tableSort.sortField,
            sortOrder: tableSort.sortOrder
          }
        : {})
    }
    const response = await getDataSourceList(qp)
    if (response.success && response.result) {
      dataSourceList.value = response.result || []
    } else {
      dataSourceList.value = []
    }
  } catch (error) {
    console.error('获取数据源列表数据失败:', error)
    dataSourceList.value = []
  } finally {
    loading.dataSourceList = false
  }
}

// 请求：评价 TOP（词云）
const fetchEvaluateTop = async (params?: VocQueryParams) => {
  // 显式传参优先；无参时复用最近一次请求上下文，保证切换情感筛选不丢联动条件
  const contextParams = params !== undefined ? params || null : evaluateTopRequestParams.value
  const requestId = ++evaluateTopRequestId.value
  try {
    loading.evaluateTop = true
    const qp = buildEvaluateTopQueryParams(contextParams)
    const response = await getProvinceOpinionEvaluateTop(qp)
    // 仅处理最新请求，避免旧响应覆盖当前词云数据
    if (requestId !== evaluateTopRequestId.value) return
    evaluateTopRequestParams.value = { ...qp }
    if (response.success && response.result) {
      wordCloudData.value = (response.result as unknown as any[])?.map(item => ({
        name: item.opinion,
        value: item.totalMentions,
        sentiment: item.sentiment
      }))
    } else {
      wordCloudData.value = []
    }
  } catch (error) {
    if (requestId !== evaluateTopRequestId.value) return
    evaluateTopRequestParams.value = buildEvaluateTopQueryParams(contextParams)
    console.error('获取评价TOP失败:', error)
    wordCloudData.value = []
  } finally {
    if (requestId === evaluateTopRequestId.value) loading.evaluateTop = false
  }
}

// 拉全量
const fetchData = async (params?: VocQueryParams) => {
  await Promise.all([
    fetchChannelTopData(params),
    fetchDataSourceList(params),
    fetchEvaluateTop(params)
  ])
}

// 对外暴露 refresh
const refresh = (params?: VocQueryParams) => {
  fetchData(params)
}
defineExpose({ refresh })

onMounted(() => {
  fetchData()
})

// 下钻改造：改为调用 ddStore.drillDown
const handleWordCloudClick = (item: any): void => {
  if (currentSelectedTopic.value === item.name) return
  currentSelectedTopic.value = item.name
  const drillParams: VocQueryParams = {
    ...(evaluateTopRequestParams.value || buildEvaluateTopQueryParams(null)),
    topic: item.name
  }
  // 记录来源：观点（来源于数据源页的词云点击）
  ddStore.updateDDViewParams({ lastDrillFrom: DrillTabKey.DATASOURCE })
  ddStore.drillDown(drillParams, [{ text: item.name, value: { topic: item.name } }])
}

const handleCellClick = (row: any) => {
  if (currentSelectedChannelCode.value === row.channelCode) {
    return
  }
  currentSelectedChannelCode.value = row.channelCode || ''
  // 记录来源：数据源
  ddStore.updateDDViewParams({ lastDrillFrom: DrillTabKey.DATASOURCE })
  ddStore.drillDown({ channelCode: row.channelCode }, [
    {
      text: row.channelName,
      value: { channelCode: row.channelCode }
    }
  ])
}

// 监听入参变化自动刷新
watch(
  () => props.queryParams,
  () => {
    // 外部查询条件变化时清理缓存，避免跨层级沿用旧上下文
    evaluateTopRequestParams.value = null
    fetchData()
  },
  { deep: true }
)
</script>

<template>
  <div class="dds-data-source h-full flex flex-col overflow-y-auto">
    <div class="flex w-full border-bottom">
      <div class="flex-1 pr-36 border-right" v-loading="loading.channelTop">
        <CommonTitle title="渠道发声TOP">
          <template #right>
            <SwitchButton
              v-model="innerMode"
              :options="MENTION_NEGATIVE_RATE_SWITCH_OPTIONS"
              @change="channelModeChange"
            />
          </template>
        </CommonTitle>
        <ChannelVoiceRankingBar
          :data="rankData"
          :mode="innerMode"
          @bar-click="fetchEvaluateTop({ channelCode: $event?.channelCode })"
          height="304px"
        />
      </div>
      <div class="flex-1 pl-36 mr-36" v-loading="loading.evaluateTop">
        <CommonTitle title="观点评价TOP">
          <template #left>
            <el-select
              v-model="wordCloudValue"
              placeholder=""
              :empty-values="[null, undefined]"
              @change="fetchEvaluateTop()"
              style="width: 108px"
            >
              <el-option
                v-for="item in wordCloudOptions"
                :key="item.value"
                :label="item.text"
                :value="item.value"
              />
            </el-select>
          </template>
        </CommonTitle>
        <div style="height: 304px">
          <WordCloudChart
            class="flex-y-center"
            :data="wordCloudData"
            :color-palette="WORD_CLOUD_RANDOM_COLOR_PALETTE"
            :dim-opacity="0.8"
            :highlight-top-count="10"
            ellipse
            random-palette
            @wordClick="handleWordCloudClick"
          />
        </div>
      </div>
    </div>
    <!-- 表格标题区域 -->
    <CommonTitle title="数据源列表" class="mt-19 mb-16" />
    <!-- 数据源列表表格 -->
    <DrillDownTable
      :data="dataSourceList"
      :columns="tableHeaders"
      header-height="80px"
      height="100%"
      :stripe="false"
      :show-sort-icon="true"
      :loading="loading.dataSourceList"
      class="csa__detail-table"
      @cell-click="handleCellClick"
      @sort-change="handleTableSortChange"
    />
  </div>
</template>

<style scoped lang="scss">
.border-right {
  border-right: 1px solid $border-regular;
}

.border-bottom {
  border-bottom: 1px solid $border-regular;
}
</style>
