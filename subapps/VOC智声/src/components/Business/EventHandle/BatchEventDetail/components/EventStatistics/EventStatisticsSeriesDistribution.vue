<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { CSSProperties } from 'vue'
import type { EChartsOption } from 'echarts'
import FEcharts from '@/components/Charts/FEcharts/index.vue'
import RegionalDistributionMap from '@/components/Business/DrillDownDialog/components/RegionalDistributionMap.vue'
import WordCloudChart from '@/components/DataSourceAnalysis/WordCloudChart.vue'
import { fmtNum } from '@/utils'
import type {
  BatchEventStatisticsChannelItem,
  BatchEventStatisticsProvinceTopItem,
  BatchEventStatisticsSentimentOption,
  BatchEventStatisticsSeriesDistributionModule,
  BatchEventStatisticsSeriesItem
} from '../../types'

defineOptions({
  name: 'EventStatisticsSeriesDistribution'
})

const props = defineProps<{
  moduleData: BatchEventStatisticsSeriesDistributionModule
  selectedSeriesCode?: string
  selectedSentiment?: string
  sentimentOptions: BatchEventStatisticsSentimentOption[]
}>()

const emit = defineEmits<{
  'update:selected-series-code': [value?: string]
  'update:selected-sentiment': [value: string]
}>()

const DEFAULT_SERIES_DETAIL_KEY = '__all__'
const SENTIMENT_COLOR_MAP = {
  正面: '#82E3C7',
  中性: '#60B8EB',
  负面: '#FF8A8B'
}
const SCENE_Y_AXIS_SPLIT_NUMBER = 5

interface YAxisRange {
  min: number
  max: number
  interval: number
}

/**
 * 将粗略步长转换为更适合坐标轴展示的 1/2/5/10 阶梯步长。
 * @param roughStep 原始步长
 * @returns 坐标轴友好步长
 */
const getNiceAxisStep = (roughStep: number) => {
  if (!Number.isFinite(roughStep) || roughStep <= 0) return 1

  const exponent = Math.floor(Math.log10(roughStep))
  const base = Math.pow(10, exponent)
  const fraction = roughStep / base

  if (fraction <= 1) return base
  if (fraction <= 2) return 2 * base
  if (fraction <= 5) return 5 * base

  return 10 * base
}

/**
 * 根据聚焦场景堆叠总量生成 Y 轴展示范围。
 * 聚焦场景是非负计数堆叠柱图，下界固定从 0 起步，避免低位堆叠分段被裁剪。
 * @param values 每个场景三类情感提及量的堆叠总量
 * @returns Y 轴最小值、最大值和刻度间隔
 */
const buildSceneYAxisRange = (values: number[]): YAxisRange => {
  const validValues = values.filter(value => Number.isFinite(value))

  if (validValues.length === 0) {
    return {
      min: 0,
      max: SCENE_Y_AXIS_SPLIT_NUMBER,
      interval: 1
    }
  }

  const maxValue = Math.max(...validValues, 0)

  if (maxValue <= 0) {
    return {
      min: 0,
      max: SCENE_Y_AXIS_SPLIT_NUMBER,
      interval: 1
    }
  }

  const interval = Math.max(1, getNiceAxisStep((maxValue * 1.1) / SCENE_Y_AXIS_SPLIT_NUMBER))

  return {
    min: 0,
    max: Math.ceil(maxValue / interval) * interval,
    interval
  }
}

/**
 * 监听车系列表变化并修正外部选中项。
 * 当事件切换或车系列表变化后，若当前选中项不存在则取消联动，不再默认选中第一项。
 */
watch(
  () => props.moduleData.seriesDistribution,
  list => {
    if (!props.selectedSeriesCode) return

    const hasCurrent = list.some(item => item.code === props.selectedSeriesCode)

    if (!hasCurrent) {
      emit('update:selected-series-code', undefined)
    }
  },
  { immediate: true, deep: true }
)

const selectedSeries = computed<BatchEventStatisticsSeriesItem | null>(() => {
  if (!props.selectedSeriesCode) {
    return null
  }

  return (
    props.moduleData.seriesDistribution.find(item => item.code === props.selectedSeriesCode) || null
  )
})

const selectedSeriesDetail = computed(() => {
  const detailKey = selectedSeries.value?.code || DEFAULT_SERIES_DETAIL_KEY
  return props.moduleData.seriesDetails[detailKey] || null
})

const currentWordCloudData = computed(() => {
  return selectedSeriesDetail.value?.wordCloudMap.all || []
})

const seriesChartEmpty = computed(() => props.moduleData.seriesDistribution.length === 0)
const sceneChartEmpty = computed(
  () => (selectedSeriesDetail.value?.sceneDistribution.length || 0) === 0
)
const provinceTopList = computed<BatchEventStatisticsProvinceTopItem[]>(() => {
  return selectedSeriesDetail.value?.provinceTopList || []
})
const channelTable = computed<BatchEventStatisticsChannelItem[]>(() => {
  return selectedSeriesDetail.value?.channelTable || []
})
const provinceTopMaxPercent = computed(() => {
  return Math.max(...provinceTopList.value.map(item => item.percent), 0)
})

type ChannelSortField = 'mentions' | 'ratio'
type ChannelSortOrder = 'ascending' | 'descending' | null

const channelSortState = ref<{
  field: ChannelSortField | null
  order: ChannelSortOrder
}>({
  field: null,
  order: null
})

const sortedChannelTable = computed<BatchEventStatisticsChannelItem[]>(() => {
  const { field, order } = channelSortState.value

  if (!field || !order) {
    return channelTable.value
  }

  const sortFactor = order === 'ascending' ? 1 : -1

  return [...channelTable.value].sort((prevItem, nextItem) => {
    return (prevItem[field] - nextItem[field]) * sortFactor
  })
})

const channelColumnCount = computed(() => Math.max(sortedChannelTable.value.length, 1))

/**
 * 生成联动模块标题中的车系前缀。
 * 未选择车系时不展示默认车系，避免误导用户以为已联动。
 * @returns 当前车系标题前缀
 */
const formatSeriesTitlePrefix = () => {
  return selectedSeries.value ? `【${selectedSeries.value.name}】` : ''
}

/**
 * 处理顶部车系柱图点击。
 * 仅当命中有效数据项时切换当前车系，避免点击空白区导致状态抖动。
 * @param params 图表点击参数
 */
const handleSeriesChartClick = (params: any) => {
  const dataIndex = Number(params?.dataIndex)
  if (Number.isNaN(dataIndex)) {
    return
  }

  const targetSeries = props.moduleData.seriesDistribution[dataIndex]
  if (!targetSeries?.code) {
    return
  }

  const nextCode = props.selectedSeriesCode === targetSeries.code ? undefined : targetSeries.code
  emit('update:selected-series-code', nextCode)
}

/**
 * 处理渠道分布排序切换。
 * 使用“升序/降序/不排序”三态规则：再次点击当前生效箭头时恢复原始顺序，
 * 点击另一方向或另一字段时立即切换到对应排序。
 * @param field 排序字段
 * @param order 目标排序方向
 */
const handleChannelSort = (field: ChannelSortField, order: Exclude<ChannelSortOrder, null>) => {
  const isSameField = channelSortState.value.field === field
  const isSameOrder = channelSortState.value.order === order

  if (isSameField && isSameOrder) {
    channelSortState.value = {
      field: null,
      order: null
    }
    return
  }

  channelSortState.value = {
    field,
    order
  }
}

/**
 * 判断指定排序箭头是否处于激活态。
 * 模板使用该方法控制高亮样式，避免在结构中重复拼接类名逻辑。
 * @param field 排序字段
 * @param order 排序方向
 * @returns 当前箭头是否高亮
 */
const isChannelSortActive = (field: ChannelSortField, order: Exclude<ChannelSortOrder, null>) => {
  return channelSortState.value.field === field && channelSortState.value.order === order
}

/**
 * 处理观点评价 TOP 情感切换。
 * 情感筛选由父组件统一控制，并由父组件按当前车系触发接口请求。
 * @param value 情感选项值
 */
const handleSentimentChange = (value: string) => {
  emit('update:selected-sentiment', value)
}

/**
 * 构建顶部车系分布图配置。
 * 采用浅色背景柱 + 实际值前景柱的方式，贴近视觉稿中的“填充量级”效果。
 */
const seriesChartOptions = computed<EChartsOption>(() => {
  const list = props.moduleData.seriesDistribution
  const activeSeriesCode = props.selectedSeriesCode || ''
  const maxValue = Math.max(...list.map(item => item.mentions), 0)
  const yAxisMax = maxValue <= 0 ? 1000 : Math.ceil(maxValue * 1.08)
  const xAxisData = list.map(item => item.name)
  const backgroundData = list.map(() => yAxisMax)
  const valueData = list.map(item => item.mentions)

  return {
    grid: {
      left: 20,
      right: 20,
      top: 30,
      bottom: 70
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'none'
      },
      formatter(params) {
        if (!Array.isArray(params) || params.length === 0) {
          return ''
        }

        const item = list[params[0].dataIndex]
        if (!item) {
          return ''
        }

        return `${item.name}<br/>提及量：${fmtNum(item.mentions)}`
      }
    },
    xAxis: {
      type: 'category',
      data: xAxisData,
      axisTick: {
        show: false
      },
      axisLine: {
        lineStyle: {
          color: '#C9CDD4'
        }
      },
      axisLabel: {
        interval: 0,
        margin: 18,
        fontSize: 12,
        lineHeight: 18,
        formatter(value: string, index: number) {
          if (activeSeriesCode && list[index]?.code === activeSeriesCode) {
            return `{active|${value}}\n{arrow|▼}`
          }

          return `{default|${value}}`
        },
        rich: {
          active: {
            color: '#1677FF',
            fontWeight: 600
          },
          default: {
            color: '#344054',
            fontWeight: 500
          },
          arrow: {
            color: '#1677FF',
            fontSize: 12,
            lineHeight: 16
          }
        }
      }
    },
    yAxis: {
      type: 'value',
      max: yAxisMax,
      splitLine: {
        show: false
      },
      axisLabel: {
        show: false
      },
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      }
    },
    series: [
      {
        type: 'bar',
        barWidth: 32,
        barGap: '-100%',
        silent: true,
        itemStyle: {
          color: '#F5F7FA',
          borderRadius: [0, 0, 0, 0]
        },
        label: {
          show: true,
          position: 'top',
          distance: 10,
          color: '#98A2B3',
          fontSize: 12,
          fontWeight: 600,
          formatter(params: any) {
            return fmtNum(list[params.dataIndex]?.mentions || 0)
          }
        },
        emphasis: {
          disabled: true
        },
        data: backgroundData
      },
      {
        name: '提及量',
        type: 'bar',
        barWidth: 32,
        data: valueData,
        itemStyle: {
          color: (params: any) => {
            const isActive = activeSeriesCode && list[params.dataIndex]?.code === activeSeriesCode
            return isActive ? '#4A90FF' : '#A7CDFF'
          },
          borderRadius: [0, 0, 0, 0]
        }
      }
    ]
  }
})

/**
 * 构建聚焦场景堆叠柱图配置。
 * 按当前车系切换接口返回的聚焦场景明细，保证视觉联动完整。
 */
const sceneChartOptions = computed<EChartsOption>(() => {
  const list = selectedSeriesDetail.value?.sceneDistribution || []
  const xAxisData = list.map(item => item.sceneName)
  const positiveData = list.map(item => item.positiveMentions)
  const neutralData = list.map(item => item.neutralMentions)
  const negativeData = list.map(item => item.negativeMentions)
  const stackedValues = list.map(
    item => item.positiveMentions + item.neutralMentions + item.negativeMentions
  )
  const yAxisRange = buildSceneYAxisRange(stackedValues)

  return {
    grid: {
      left: 20,
      right: 20,
      top: 22,
      bottom: 70,
      containLabel: true
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter(params) {
        if (!Array.isArray(params) || params.length === 0) {
          return ''
        }

        const current = list[params[0].dataIndex]
        if (!current) {
          return ''
        }

        return [
          `<div style="font-size: 14px; font-weight: 600; margin-bottom: 8px;">${current.sceneName}</div>`,
          `<div style="display:flex;justify-content:space-between;gap:16px;"><span style="color:#82E3C7;">正面提及量</span><span>${fmtNum(current.positiveMentions)}</span></div>`,
          `<div style="display:flex;justify-content:space-between;gap:16px;"><span style="color:#60B8EB;">中性提及量</span><span>${fmtNum(current.neutralMentions)}</span></div>`,
          `<div style="display:flex;justify-content:space-between;gap:16px;"><span style="color:#FF8A8B;">负面提及量</span><span>${fmtNum(current.negativeMentions)}</span></div>`
        ].join('')
      }
    },
    legend: {
      bottom: 0,
      left: 'center',
      itemWidth: 10,
      itemHeight: 10,
      icon: 'roundRect',
      textStyle: {
        color: '#6E7B91',
        fontSize: 12
      },
      data: ['正面提及量', '中性提及量', '负面提及量']
    },
    xAxis: {
      type: 'category',
      data: xAxisData,
      axisTick: {
        show: false
      },
      axisLine: {
        lineStyle: {
          color: '#D0D5DD'
        }
      },
      axisLabel: {
        interval: 0,
        color: '#475467',
        fontSize: 12
      }
    },
    yAxis: {
      type: 'value',
      min: yAxisRange.min,
      max: yAxisRange.max,
      interval: yAxisRange.interval,
      splitNumber: SCENE_Y_AXIS_SPLIT_NUMBER,
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      axisLabel: {
        color: '#98A2B3',
        fontSize: 12
      },
      splitLine: {
        lineStyle: {
          color: '#EAECEF',
          type: 'dashed'
        }
      }
    },
    series: [
      {
        name: '正面提及量',
        type: 'bar',
        stack: 'mentions',
        barMaxWidth: 20,
        data: positiveData,
        itemStyle: {
          color: '#82E3C7'
        }
      },
      {
        name: '中性提及量',
        type: 'bar',
        stack: 'mentions',
        barMaxWidth: 20,
        data: neutralData,
        itemStyle: {
          color: '#60B8EB'
        }
      },
      {
        name: '负面提及量',
        type: 'bar',
        stack: 'mentions',
        barMaxWidth: 20,
        data: negativeData,
        itemStyle: {
          color: '#FF8A8B'
        }
      }
    ]
  }
})

/**
 * 为右侧省份峰形榜计算尺寸样式。
 * 通过百分比映射高度与宽度，保持头部省份的视觉差异，同时限制最小尺寸避免过小不可读。
 * @param item 省份排行项
 * @returns 峰形样式对象
 */
const getProvincePeakStyle = (item: BatchEventStatisticsProvinceTopItem): CSSProperties => {
  const maxPercent = provinceTopMaxPercent.value || 1
  const ratio = item.percent / maxPercent
  const isCompact = provinceTopList.value.length > 5

  return {
    width: `${isCompact ? 42 + ratio * 24 : 56 + ratio * 34}px`,
    height: `${72 + ratio * 88}px`,
    background: item.color
  }
}

/**
 * 渲染渠道表格中的比例文案。
 * @param ratio 占比值
 * @returns 百分比文本
 */
const formatRatio = (ratio: number): string => {
  return `${ratio.toFixed(2)}%`
}
</script>

<template>
  <section class="event-series-distribution">
    <div class="event-series-distribution__series-panel">
      <div class="event-series-distribution__title">车系分布</div>
      <FEcharts
        :options="seriesChartOptions"
        :is-empty="seriesChartEmpty"
        :not-merge-on-update="true"
        :width="'100%'"
        :height="'250px'"
        empty-description="暂无车系分布数据"
        @chart-click="handleSeriesChartClick"
      />
    </div>

    <div class="event-series-distribution__cards">
      <div class="event-series-distribution__card event-series-distribution__card--scene">
        <div class="event-series-distribution__card-head">
          <div class="event-series-distribution__section-label">
            <span class="event-series-distribution__series-name">
              {{ formatSeriesTitlePrefix() }}
            </span>
            <span>聚焦场景</span>
          </div>
        </div>
        <div class="event-series-distribution__card-body">
          <FEcharts
            :options="sceneChartOptions"
            :is-empty="sceneChartEmpty"
            :width="'100%'"
            :height="'100%'"
            empty-description="暂无场景分布数据"
          />
        </div>
      </div>

      <div class="event-series-distribution__card event-series-distribution__card--opinion">
        <div class="event-series-distribution__card-head">
          <div class="event-series-distribution__title-inline">
            <div class="event-series-distribution__section-label">
              <span class="event-series-distribution__series-name">
                {{ formatSeriesTitlePrefix() }}
              </span>
              <span>观点评价TOP</span>
            </div>
            <el-select
              :model-value="props.selectedSentiment || 'all'"
              class="event-series-distribution__sentiment-select"
              placeholder=""
              @update:model-value="handleSentimentChange"
            >
              <el-option
                v-for="item in props.sentimentOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </div>
        </div>
        <div class="event-series-distribution__card-body">
          <div class="event-series-distribution__word-cloud">
            <WordCloudChart
              v-if="currentWordCloudData.length"
              :data="currentWordCloudData"
              :color-map="SENTIMENT_COLOR_MAP"
              :dim-opacity="0.8"
              :highlight-top-count="10"
              ellipse
            />
            <el-empty v-else description="暂无观点词云数据" />
          </div>
        </div>
      </div>

      <div class="event-series-distribution__card event-series-distribution__card--province">
        <div class="event-series-distribution__card-head">
          <div class="event-series-distribution__section-label">
            <span class="event-series-distribution__series-name">
              {{ formatSeriesTitlePrefix() }}
            </span>
            <span>省份分布</span>
          </div>
        </div>
        <div class="event-series-distribution__card-body">
          <div class="event-series-distribution__province-layout">
            <div class="event-series-distribution__province-map">
              <RegionalDistributionMap
                :data="selectedSeriesDetail?.provinceMapData || []"
                data-type="mention"
                height="100%"
              />
            </div>

            <div class="event-series-distribution__province-top">
              <template v-if="provinceTopList.length">
                <div
                  v-for="item in provinceTopList"
                  :key="item.provinceName"
                  class="event-series-distribution__province-top-item"
                >
                  <div
                    class="event-series-distribution__province-peak"
                    :style="getProvincePeakStyle(item)"
                  ></div>
                  <div class="event-series-distribution__province-name">
                    {{ item.provinceName }}
                  </div>
                  <div class="event-series-distribution__province-percent">
                    {{ formatRatio(item.percent) }}
                  </div>
                </div>
              </template>
              <el-empty v-else description="暂无省份排行数据" />
            </div>
          </div>
        </div>
      </div>

      <div class="event-series-distribution__card event-series-distribution__card--channel">
        <div class="event-series-distribution__card-head">
          <div class="event-series-distribution__section-label">
            <span class="event-series-distribution__series-name">
              {{ formatSeriesTitlePrefix() }}
            </span>
            <span>渠道分布</span>
          </div>
        </div>

        <div class="event-series-distribution__card-body">
          <div class="event-series-distribution__channel-scroll">
            <div
              class="event-series-distribution__channel-grid"
              :style="{ '--channel-count': channelColumnCount }"
            >
              <div
                class="event-series-distribution__channel-cell event-series-distribution__channel-cell--head"
              >
                数据来源
              </div>

              <div
                v-for="item in sortedChannelTable"
                :key="`channel-${item.channelName}`"
                class="event-series-distribution__channel-cell event-series-distribution__channel-cell--head"
              >
                {{ item.channelName }}
              </div>

              <div
                class="event-series-distribution__channel-cell event-series-distribution__channel-cell--label"
              >
                <span class="event-series-distribution__channel-sort-label">提及量</span>
                <span class="event-series-distribution__channel-sort-icons" aria-hidden="true">
                  <el-icon
                    class="event-series-distribution__channel-sort-icon"
                    :class="{
                      'event-series-distribution__channel-sort-icon--active': isChannelSortActive(
                        'mentions',
                        'ascending'
                      )
                    }"
                    @click="handleChannelSort('mentions', 'ascending')"
                  >
                    <CaretTop />
                  </el-icon>
                  <el-icon
                    class="event-series-distribution__channel-sort-icon"
                    :class="{
                      'event-series-distribution__channel-sort-icon--active': isChannelSortActive(
                        'mentions',
                        'descending'
                      )
                    }"
                    @click="handleChannelSort('mentions', 'descending')"
                  >
                    <CaretBottom />
                  </el-icon>
                </span>
              </div>
              <div
                v-for="item in sortedChannelTable"
                :key="`mentions-${item.channelName}`"
                class="event-series-distribution__channel-cell"
              >
                {{ fmtNum(item.mentions) }}
              </div>

              <div
                class="event-series-distribution__channel-cell event-series-distribution__channel-cell--label"
              >
                <span class="event-series-distribution__channel-sort-label">占比</span>
                <span class="event-series-distribution__channel-sort-icons" aria-hidden="true">
                  <el-icon
                    class="event-series-distribution__channel-sort-icon"
                    :class="{
                      'event-series-distribution__channel-sort-icon--active': isChannelSortActive(
                        'ratio',
                        'ascending'
                      )
                    }"
                    @click="handleChannelSort('ratio', 'ascending')"
                  >
                    <CaretTop />
                  </el-icon>
                  <el-icon
                    class="event-series-distribution__channel-sort-icon"
                    :class="{
                      'event-series-distribution__channel-sort-icon--active': isChannelSortActive(
                        'ratio',
                        'descending'
                      )
                    }"
                    @click="handleChannelSort('ratio', 'descending')"
                  >
                    <CaretBottom />
                  </el-icon>
                </span>
              </div>
              <div
                v-for="item in sortedChannelTable"
                :key="`ratio-${item.channelName}`"
                class="event-series-distribution__channel-cell"
              >
                {{ formatRatio(item.ratio) }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style lang="scss" scoped>
.event-series-distribution {
  --event-series-card-shadow: 0px 1px 2px 0px rgba(10, 13, 18, 0.05);
  --event-series-card-border: 1px solid #ebedf0;

  display: flex;
  flex-direction: column;
  gap: 24px;
  padding: 24px;
  background: #ffffff;
  border-radius: 8px;
  border: var(--event-series-card-border);
  box-shadow: var(--event-series-card-shadow);
  box-sizing: border-box;
}

.event-series-distribution__series-panel {
  width: 100%;
}

.event-series-distribution__title,
.event-series-distribution__section-label {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  font-weight: 600;
  font-size: 18px;
  line-height: 28px;
  color: #1f2733;
}

.event-series-distribution__cards {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.event-series-distribution__card {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 24px;
  background: #ffffff;
  border-radius: 8px;
  border: var(--event-series-card-border);
  box-shadow: var(--event-series-card-shadow);
  box-sizing: border-box;
  overflow: hidden;
}

.event-series-distribution__card--scene {
  height: 571px;
}

.event-series-distribution__card--opinion,
.event-series-distribution__card--province {
  height: 420px;
}

.event-series-distribution__card--channel {
  height: 356px;
}

.event-series-distribution__card-head {
  display: flex;
  align-items: center;
  flex: 0 0 auto;
}

.event-series-distribution__card-body {
  flex: 1;
  min-height: 0;
}

.event-series-distribution__title-inline {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.event-series-distribution__series-name {
  color: #1677ff;
  margin-right: 4px;
}

.event-series-distribution__sentiment-select {
  width: 108px;
  flex: 0 0 auto;
}

.event-series-distribution__word-cloud {
  width: 100%;
  height: 100%;

  :deep(.word-cloud-chart) {
    width: 100%;
    height: 100%;
  }

  :deep(.el-empty) {
    height: 100%;
    margin: 0;
  }
}

.event-series-distribution__province-layout {
  display: flex;
  align-items: stretch;
  gap: 24px;
  width: 100%;
  height: 100%;
}

.event-series-distribution__province-map {
  flex: 0 0 340px;
  min-width: 320px;
  min-height: 0;
}

.event-series-distribution__province-top {
  flex: 1;
  min-height: 0;
  min-width: 0;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
  padding: 8px 8px 0;
  box-sizing: border-box;
  overflow-x: auto;
  overflow-y: hidden;

  :deep(.el-empty) {
    width: 100%;
    height: 100%;
    align-self: center;
    margin: 0;
  }
}

.event-series-distribution__province-top-item {
  flex: 1 0 64px;
  min-width: 64px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
}

.event-series-distribution__province-peak {
  clip-path: polygon(50% 0%, 0% 100%, 100% 100%);
}

.event-series-distribution__province-name {
  margin-top: 12px;
  font-weight: 500;
  font-size: 16px;
  line-height: 24px;
  color: #1f2733;
}

.event-series-distribution__province-percent {
  margin-top: 4px;
  font-size: 14px;
  line-height: 22px;
  color: #344054;
}

.event-series-distribution__channel-scroll {
  width: 100%;
  height: 100%;
  min-height: 0;
  overflow-x: auto;
  overflow-y: hidden;
}

.event-series-distribution__channel-grid {
  display: grid;
  grid-template-columns: 104px repeat(var(--channel-count), minmax(132px, 1fr));
  grid-template-rows: repeat(3, minmax(0, 1fr));
  gap: 12px;
  height: 100%;
  min-width: max(100%, calc(104px + var(--channel-count) * 132px + var(--channel-count) * 12px));
}

.event-series-distribution__channel-cell {
  min-height: 0;
  padding: 12px 14px;
  border-radius: 8px;
  background: #f3f5f8;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  font-size: 15px;
  line-height: 24px;
  font-weight: 600;
  color: #343b48;
  box-sizing: border-box;
  word-break: break-word;
}

.event-series-distribution__channel-cell--head {
  background: #eaf4ff;
  font-weight: 600;
}

.event-series-distribution__channel-cell--label {
  gap: 6px;
  background: #eaf4ff;
  font-weight: 600;
}

.event-series-distribution__channel-sort-label {
  display: inline-flex;
  align-items: center;
}

.event-series-distribution__channel-sort-icons {
  position: relative;
  display: inline-block;
  width: 12px;
  height: 14px;
  flex: 0 0 auto;
}

.event-series-distribution__channel-sort-icon {
  position: absolute;
  left: 0;
  color: #98a2b3;
  font-size: 14px;
  line-height: 12px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s ease;
}

.event-series-distribution__channel-sort-icon:first-child {
  top: -4px;
}

.event-series-distribution__channel-sort-icon:last-child {
  bottom: -4px;
}

.event-series-distribution__channel-sort-icon:hover {
  color: #667085;
}

.event-series-distribution__channel-sort-icon--active,
.event-series-distribution__channel-sort-icon--active:hover {
  color: #1677ff;
}
</style>
