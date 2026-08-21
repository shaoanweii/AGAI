<script setup lang="ts">
import { ref, computed } from 'vue'
import {
  DEFAULT_MENTION_NEGATIVE_RATE_TYPE,
  MENTION_NEGATIVE_RATE_SWITCH_OPTIONS
} from '@/constants'
import type {
  ChannelMentionShareVo,
  ChannelNegativeTrendVo,
  DataSourceAnalysisVo
} from '@/api/productAnalysis/types'
import type {
  ProductSelfChannelMentionShareVo,
  ProductSelfChannelNegativeTrendVo,
  ProductSelfDataSourceAnalysisVo
} from '@/api/thisProductAnalysis/types.d'
import ProportionOfMentionsChart from './ProportionOfMentionsChart.vue'
import RateTrendChart from './RateTrendChart.vue'
import DSTable from './DSTable.vue'

defineOptions({
  name: 'DataSourceAnalysis'
})

// 定义通用的数据类型，支持两个模块的类型
type ChannelMentionShareData = ChannelMentionShareVo | ProductSelfChannelMentionShareVo
type ChannelNegativeTrendData = ChannelNegativeTrendVo | ProductSelfChannelNegativeTrendVo
type DataSourceAnalysisData = DataSourceAnalysisVo | ProductSelfDataSourceAnalysisVo

// 接收父组件传递的数据
interface Props {
  channelMentionShareData?: ChannelMentionShareData[]
  channelNegativeTrendData?: ChannelNegativeTrendData[]
  dataSourceAnalysisData?: DataSourceAnalysisData[]
}

const props = withDefaults(defineProps<Props>(), {
  channelMentionShareData: () => [],
  channelNegativeTrendData: () => [],
  dataSourceAnalysisData: () => []
})

// 定义事件
const emit = defineEmits<{
  'data-type-change': [dataType: MentionNegativeRateType]
  'chart-click': [{ type: 'proportion' | 'trend'; data: any }]
  'cell-click': [data: any]
}>()

// 处理图表点击事件
const handleChartClick = (data: any, type: 'proportion' | 'trend') => {
  emit('chart-click', { type, data })
}

// 处理单元格点击事件
const handleCellClick = (data: any) => {
  emit('cell-click', data)
}

// 数据类型：负面率或提及量，默认值统一读取全局常量配置
const dataType = ref<MentionNegativeRateType>(DEFAULT_MENTION_NEGATIVE_RATE_TYPE)

// 渠道提及量占比排序
// 说明：为了统一各业务场景的显示效果，将排序逻辑下沉至 DataSourceAnalysis 组件内部，
// 按照提及量 `mentions` 从高到低排序。此处不直接修改父组件传入的 props，使用浅拷贝后排序。
const sortedChannelMentionShareData = computed<ChannelMentionShareData[]>(() => {
  const list = Array.isArray(props.channelMentionShareData)
    ? [...props.channelMentionShareData]
    : []
  return list.sort((a: any, b: any) => Number(b?.mentions || 0) - Number(a?.mentions || 0))
})

// 处理SwitchButton切换事件
const handleDataTypeChange = (option: any) => {
  dataType.value = option.value
  emit('data-type-change', option.value)
}
</script>

<template>
  <div class="data-source-analysis">
    <div class="channel-container">
      <FCard
        :title="'渠道提及量占比'"
        titleSize="small"
        :width="'640px'"
        :height="'361px'"
        class="f-card-border"
      >
        <ProportionOfMentionsChart
          :data="sortedChannelMentionShareData"
          @chart-click="(data: any) => handleChartClick(data, 'proportion')"
        ></ProportionOfMentionsChart>
      </FCard>
      <FCard
        :title="'渠道数据趋势'"
        titleSize="small"
        isShowMore
        :height="'361px'"
        class="f-card-border"
      >
        <template #more>
          <SwitchButton
            v-model="dataType"
            :options="MENTION_NEGATIVE_RATE_SWITCH_OPTIONS"
            @change="handleDataTypeChange"
          ></SwitchButton>
        </template>
        <RateTrendChart
          :data="props.channelNegativeTrendData"
          :dataType="dataType"
          @chart-click="(data: any) => handleChartClick(data, 'trend')"
        ></RateTrendChart>
      </FCard>
    </div>
    <FCard :title="'渠道数据排行'" titleSize="small" :height="'440px'" class="f-card-border mt-24">
      <DSTable :data="props.dataSourceAnalysisData" @cell-click="handleCellClick"></DSTable>
    </FCard>
  </div>
</template>

<style lang="scss" scoped>
.data-source-analysis {
  .channel-container {
    display: grid;
    grid-template-columns: 640px 1fr;
    gap: 24px;
    margin-top: 24px;
  }
}
</style>
