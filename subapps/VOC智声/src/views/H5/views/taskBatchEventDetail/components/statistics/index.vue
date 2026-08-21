<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import HCollapseCard from '@h5/components/UI/HCollapseCard/index.vue'
import EventInfoPanel from './EventInfoPanel.vue'
import ReportInterpretationPanel from './ReportInterpretationPanel.vue'
import StatisticOverviewPanel from './StatisticOverviewPanel.vue'
import { useTaskEventStore } from '@h5/store'
import {
  getBatchEventCarSeriesStat,
  getBatchEventChannelStat,
  getBatchEventDataStat,
  getBatchEventOpinionStat,
  getBatchEventProvinceStat,
  getBatchEventReportSummaryStream,
  getBatchEventSceneStat,
  getBatchEventTrendStat
} from '@h5/api/batchEvent'
import type {
  BatchEventBriefDetailVo,
  BatchEventCarSeriesStatVo,
  BatchEventChannelStatVo,
  BatchEventDataStatVo,
  BatchEventOpinionStatVo,
  BatchEventProvinceStatVo,
  BatchEventSceneQueryModel,
  BatchEventSceneStatVo,
  BatchEventTrendStatVo
} from '@h5/api/batchEvent/types'

interface BatchEventStatisticsProps {
  eventId: string
  detail: BatchEventBriefDetailVo
}

const props = defineProps<BatchEventStatisticsProps>()
const taskEventStore = useTaskEventStore()

const reportSummaryText = ref('')
const metricData = ref<BatchEventDataStatVo>({})
const trendData = ref<BatchEventTrendStatVo>({})
const carSeriesData = ref<BatchEventCarSeriesStatVo>([])
const sceneData = ref<BatchEventSceneStatVo>([])
const opinionData = ref<BatchEventOpinionStatVo>([])
const provinceData = ref<BatchEventProvinceStatVo>([])
const channelData = ref<BatchEventChannelStatVo>([])
const selectedSeriesCode = ref('')
const selectedSentiment = ref('all')
const statisticsLoading = ref(false)
const reportLoading = ref(false)

let statisticsRequestSeq = 0
let linkedRequestSeq = 0
let opinionRequestSeq = 0
let reportAbortController: AbortController | null = null

/**
 * 将字典字段值转换为稳定字符串，保留 0 这类有效值。
 * @param value 字典字段值
 * @returns 字符串值
 */
const formatDictField = (value: unknown) => {
  return value === null || value === undefined ? '' : String(value)
}

/**
 * 从字典项中按候选字段取出首个有效值，兼容不同端字典字段命名。
 * @param item 字典项
 * @param keys 候选字段名
 * @returns 字典字段值
 */
const pickDictField = (item: Record<string, unknown>, keys: string[]) => {
  for (const key of keys) {
    const value = item?.[key]
    if (value !== null && value !== undefined && value !== '') {
      return value
    }
  }

  return ''
}

/**
 * 获取字典项展示文案。
 * @param item 字典项
 * @returns 字典展示文案
 */
const getDictItemLabel = (item: Record<string, unknown>) => {
  return formatDictField(
    pickDictField(item, ['text', 'label', 'name', 'itemText', 'itemTextEn', 'value', 'itemValue'])
  )
}

/**
 * 获取字典项提交值。
 * @param item 字典项
 * @returns 字典提交值
 */
const getDictItemValue = (item: Record<string, unknown>) => {
  return formatDictField(
    pickDictField(item, ['value', 'itemValue', 'text', 'label', 'name', 'itemText'])
  )
}

const sentimentOptions = computed(() => [
  { label: '全部情感', value: 'all' },
  ...(taskEventStore.allDictItems?.voc_sentiment || []).map((item: Record<string, unknown>) => ({
    label: getDictItemLabel(item),
    value: getDictItemValue(item)
  }))
])

/**
 * 重置统计 Tab 的接口状态，避免切换事件时残留旧事件数据。
 */
const resetStatistics = () => {
  reportSummaryText.value = ''
  metricData.value = {}
  trendData.value = {}
  carSeriesData.value = []
  sceneData.value = []
  opinionData.value = []
  provinceData.value = []
  channelData.value = []
  selectedSeriesCode.value = ''
  selectedSentiment.value = 'all'
}

/**
 * 取消当前未结束的报告解读流，避免切换事件或组件卸载后继续写入旧文本。
 */
const abortReportSummary = () => {
  if (!reportAbortController) return

  reportAbortController.abort()
  reportAbortController = null
}

/**
 * 判断报告解读请求是否仍属于当前事件和当前统计请求轮次。
 * @param id 批量事件 ID
 * @param requestSeq 本轮统计请求序号
 * @returns 是否仍有效
 */
const isCurrentReportRequest = (id: string, requestSeq: number) => {
  return requestSeq === statisticsRequestSeq && id === String(props.eventId || '').trim()
}

/**
 * 从 SSE 风格文本中提取业务内容片段。
 * @param text 当前流片段或补齐后的完整行
 * @returns 可追加展示的文本片段和未完成行缓存
 */
const parseReportSummaryLines = (
  text: string,
  appendText: (value: string) => void
): { pendingLine: string; ended: boolean } => {
  const normalizedText = text.replace(/\r\n/g, '\n')
  const lines = normalizedText.split('\n')
  const pendingLine = normalizedText.endsWith('\n') ? '' : lines.pop() || ''

  for (const line of lines) {
    if (!line.startsWith('data:')) continue

    const data = line.slice(5).trimStart()
    if (data === '[END]') {
      return { pendingLine: '', ended: true }
    }
    if (data.trim() && !data.startsWith('<think>')) {
      appendText(data)
    }
  }

  return { pendingLine, ended: false }
}

/**
 * 查询报告解读。
 * 报告总结响应较慢，独立放到统计数据之后加载，避免阻塞事件统计展示。
 * @param id 批量事件 ID
 * @param requestSeq 本轮统计请求序号
 */
const loadReportSummary = async (id: string, requestSeq: number) => {
  abortReportSummary()
  const controller = new AbortController()
  reportAbortController = controller
  reportLoading.value = true
  reportSummaryText.value = ''

  try {
    const response = await getBatchEventReportSummaryStream({
      id,
      signal: controller.signal
    })
    if (!isCurrentReportRequest(id, requestSeq) || controller.signal.aborted) return

    const reader = response.body?.getReader()
    if (!reader) {
      throw new Error('报告解读接口未返回可读取的流')
    }

    const decoder = new TextDecoder()
    let pendingLine = ''

    try {
      while (true) {
        if (!isCurrentReportRequest(id, requestSeq) || controller.signal.aborted) {
          await reader.cancel()
          return
        }

        const { done, value } = await reader.read()
        if (done) break

        const chunk = decoder.decode(value, { stream: true })
        const parsed = parseReportSummaryLines(pendingLine + chunk, text => {
          if (isCurrentReportRequest(id, requestSeq) && !controller.signal.aborted) {
            reportSummaryText.value += text
          }
        })

        pendingLine = parsed.pendingLine
        if (parsed.ended) return
      }

      const lastChunk = decoder.decode()
      const finalText = pendingLine + lastChunk
      if (finalText) {
        parseReportSummaryLines(`${finalText}\n`, text => {
          if (isCurrentReportRequest(id, requestSeq) && !controller.signal.aborted) {
            reportSummaryText.value += text
          }
        })
      }
    } finally {
      reader.releaseLock()
    }
  } catch (error) {
    if (!isCurrentReportRequest(id, requestSeq)) return
    if ((error as DOMException)?.name === 'AbortError' || controller.signal.aborted) return

    console.error('获取批量事件报告解读失败:', error)
    reportSummaryText.value = ''
  } finally {
    if (reportAbortController === controller) {
      reportAbortController = null
    }
    if (isCurrentReportRequest(id, requestSeq)) {
      reportLoading.value = false
    }
  }
}

/**
 * 查询受车系联动影响的统计模块。
 * @param carSeriesCode 车系编码；为空时查询事件默认统计
 */
const loadLinkedStatistics = async (carSeriesCode = '') => {
  const id = String(props.eventId || '').trim()
  const requestSeq = ++linkedRequestSeq
  const requestSentiment = selectedSentiment.value
  const normalizedSentiment = requestSentiment === 'all' ? '' : requestSentiment

  sceneData.value = []
  opinionData.value = []
  provinceData.value = []
  channelData.value = []

  if (!id) return

  const query: BatchEventSceneQueryModel = {
    id,
    ...(carSeriesCode ? { carSeriesCode } : {})
  }
  const opinionQuery: BatchEventSceneQueryModel = {
    ...query,
    ...(normalizedSentiment ? { sentiment: normalizedSentiment } : {})
  }

  try {
    const [sceneRes, opinionRes, provinceRes, channelRes] = await Promise.allSettled([
      getBatchEventSceneStat(query),
      getBatchEventOpinionStat(opinionQuery),
      getBatchEventProvinceStat(query),
      getBatchEventChannelStat(query)
    ])

    if (requestSeq !== linkedRequestSeq || id !== String(props.eventId || '').trim()) return

    sceneData.value =
      sceneRes.status === 'fulfilled' &&
      sceneRes.value.success &&
      Array.isArray(sceneRes.value.result)
        ? sceneRes.value.result
        : []
    if (
      requestSentiment === selectedSentiment.value &&
      opinionRes.status === 'fulfilled' &&
      opinionRes.value.success &&
      Array.isArray(opinionRes.value.result)
    ) {
      opinionData.value = opinionRes.value.result
    }
    provinceData.value =
      provinceRes.status === 'fulfilled' &&
      provinceRes.value.success &&
      Array.isArray(provinceRes.value.result)
        ? provinceRes.value.result
        : []
    channelData.value =
      channelRes.status === 'fulfilled' &&
      channelRes.value.success &&
      Array.isArray(channelRes.value.result)
        ? channelRes.value.result
        : []
  } catch (error) {
    console.error('获取批量事件联动统计失败:', error)
  }
}

/**
 * 按当前车系和情感单独刷新评价观点，避免切换情感时重载其它联动模块。
 * @param sentiment 情感筛选值，all 表示全部情感
 */
const loadOpinionStatistics = async (sentiment = selectedSentiment.value) => {
  const id = String(props.eventId || '').trim()
  const requestSeq = ++opinionRequestSeq
  const requestSeriesCode = selectedSeriesCode.value
  const normalizedSentiment = sentiment === 'all' ? '' : sentiment

  opinionData.value = []

  if (!id) return

  try {
    const response = await getBatchEventOpinionStat({
      id,
      ...(requestSeriesCode ? { carSeriesCode: requestSeriesCode } : {}),
      ...(normalizedSentiment ? { sentiment: normalizedSentiment } : {})
    })

    if (
      requestSeq !== opinionRequestSeq ||
      id !== String(props.eventId || '').trim() ||
      requestSeriesCode !== selectedSeriesCode.value ||
      sentiment !== selectedSentiment.value
    ) {
      return
    }

    opinionData.value =
      response.success && Array.isArray(response.result) ? response.result : []
  } catch (error) {
    console.error('获取批量事件评价观点失败:', error)
  }
}

/**
 * 查询事件统计 Tab 的基础接口，并加载默认联动统计。
 */
const loadStatistics = async () => {
  const id = String(props.eventId || '').trim()
  const requestSeq = ++statisticsRequestSeq
  abortReportSummary()
  resetStatistics()
  reportLoading.value = false
  statisticsLoading.value = false

  if (!id) return

  statisticsLoading.value = true

  try {
    const [metricRes, trendRes, seriesRes] = await Promise.allSettled([
      getBatchEventDataStat({ id }),
      getBatchEventTrendStat({ id }),
      getBatchEventCarSeriesStat({ id })
    ])

    if (requestSeq !== statisticsRequestSeq || id !== String(props.eventId || '').trim()) return

    metricData.value =
      metricRes.status === 'fulfilled' && metricRes.value.success && metricRes.value.result
        ? metricRes.value.result
        : {}
    trendData.value =
      trendRes.status === 'fulfilled' && trendRes.value.success && trendRes.value.result
        ? trendRes.value.result
        : {}
    carSeriesData.value =
      seriesRes.status === 'fulfilled' &&
      seriesRes.value.success &&
      Array.isArray(seriesRes.value.result)
        ? seriesRes.value.result
        : []

    await loadLinkedStatistics()
  } catch (error) {
    console.error('获取批量事件统计失败:', error)
  } finally {
    if (requestSeq === statisticsRequestSeq && id === String(props.eventId || '').trim()) {
      statisticsLoading.value = false
    }
  }

  if (requestSeq !== statisticsRequestSeq || id !== String(props.eventId || '').trim()) return

  await loadReportSummary(id, requestSeq)
}

/**
 * 处理车系分布图点击；再次点击同一车系时取消联动。
 * @param carSeriesCode 车系编码
 */
const handleSeriesClick = (carSeriesCode: string) => {
  const nextSeriesCode = selectedSeriesCode.value === carSeriesCode ? '' : carSeriesCode
  selectedSeriesCode.value = nextSeriesCode
  void loadLinkedStatistics(nextSeriesCode)
}

/**
 * 处理评价观点情感筛选。
 * @param sentiment 情感字典值
 */
const handleSentimentChange = (sentiment: string) => {
  selectedSentiment.value = sentiment || 'all'
  void loadOpinionStatistics(selectedSentiment.value)
}

watch(
  () => props.eventId,
  () => {
    void loadStatistics()
  },
  { immediate: true }
)

void taskEventStore.fetchSysAllDictItems()

onBeforeUnmount(() => {
  abortReportSummary()
})
</script>

<template>
  <div class="batch-event-statistics">
    <section class="detail-section">
      <HCollapseCard title="事件详情" collapsible>
        <EventInfoPanel :data="props.detail" format-main-resp-user-with-emp-no />
      </HCollapseCard>
    </section>

    <section class="detail-section">
      <HCollapseCard title="报告解读" collapsible>
        <ReportInterpretationPanel :text="reportSummaryText" :loading="reportLoading" />
      </HCollapseCard>
    </section>

    <section class="detail-section">
      <HCollapseCard title="事件统计" collapsible>
        <div v-if="statisticsLoading" class="statistics-loading statistics-loading--overview">
          <van-loading size="24px" vertical>事件统计加载中...</van-loading>
        </div>
        <StatisticOverviewPanel
          v-else
          :metrics="metricData"
          :trends="trendData"
          :car-series="carSeriesData"
          :focus-scenes="sceneData"
          :opinions="opinionData"
          :provinces="provinceData"
          :channel-ranks="channelData"
          :selected-series-code="selectedSeriesCode"
          :selected-sentiment="selectedSentiment"
          :sentiment-options="sentimentOptions"
          @series-click="handleSeriesClick"
          @sentiment-change="handleSentimentChange"
        />
      </HCollapseCard>
    </section>
  </div>
</template>

<style scoped lang="scss">
.batch-event-statistics {
  padding: 12px;
}

.detail-section + .detail-section {
  margin-top: 12px;
}

.statistics-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 96px;
  color: #5f6a7a;
}

.statistics-loading--overview {
  min-height: 220px;
}
</style>
