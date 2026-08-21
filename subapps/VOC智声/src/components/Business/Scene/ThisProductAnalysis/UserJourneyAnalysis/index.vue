<script setup lang="ts">
import { emojiMap } from '@/constants'
import { computed } from 'vue'
import TopRank from './TopRank.vue'
import type { ProductSelfJourneyAnalysisVo } from '@/api/thisProductAnalysis/types'
import { fmtNum, fmtPer, fmtFix, fmtHoverData } from '@/utils'
import { formatChartPop } from '@/utils/chart'
import HoverPopover from '@components/Business/Scene/Common/HoverPopover.vue'

defineOptions({
  name: 'UserJourneyAnalysis'
})

// Props 定义
interface Props {
  data?: ProductSelfJourneyAnalysisVo[]
}

const { data = [] } = defineProps<Props>()

// ==================== 事件定义 ====================

const emit = defineEmits<{
  (
    e: 'journey-click',
    data: { type: string; journey: ProductSelfJourneyAnalysisVo; clickData?: any }
  ): void
  (e: 'row-click', data: any): void
  (e: 'view-more', data: any): void
}>()

// ==================== 事件处理方法 ====================

/**
 * 处理旅程点击事件
 */
const handleJourneyClick = (
  type: string,
  journey: ProductSelfJourneyAnalysisVo,
  clickData?: any
) => {
  emit('journey-click', { type, journey, clickData })
}

/**
 * 处理行点击事件
 */
const handleRowClick = (data: any) => {
  emit('row-click', data)
}

/**
 * 处理TopRank“查看更多”点击事件
 */
const handleTopRankViewMore = (
  payload: any,
  journey: ProductSelfJourneyAnalysisVo,
  category: 'satisfied' | 'dissatisfied'
) => {
  // sentiment 下钻必填：优先取 TopRank 组件回传，否则按分类兜底为“正面/负面”
  const sentiment =
    payload?.sentiment ||
    (category === 'satisfied' ? '正面' : category === 'dissatisfied' ? '负面' : '')
  emit('view-more', { ...payload, sentiment, journey, category })
}

/**
 * 处理负面率曲线点击事件，统一转成旅程点击事件对外抛出。
 * @param params 图表组件回传的点击参数
 */
const handleChartJourneyClick = (params: any) => {
  handleJourneyClick('chart', params)
}

// 直接使用传入的数据
const journeyData = computed(() => {
  return data
})

// 旅程阶段图标映射
const stageIconMap: Record<string, string> = {
  认知: 'yhlc-eye-line',
  选择: 'yhlc-bookmark-add',
  购买: 'yhlc-coins-hand',
  使用: 'yhlc-remote-control-line',
  维保: 'yhlc-tool-02',
  再购: 'yhlc-heart-hand'
}

// 转换数据为TopRank组件需要的格式
const convertToTopRankData = (opinions: any[], sentiment?: string) => {
  return opinions.slice(0, 3).map((opinion, index) => ({
    rank: index + 1,
    label: opinion.opinionName,
    value: opinion.mentions,
    change: fmtFix(opinion.mom),
    // 悬浮用
    mentions: opinion.mentions,
    mentionsMoM: opinion.mom,
    mentionsYoY: opinion.yoy,
    sentiment: opinion.sentiment || sentiment
  }))
}

// 图表配置 - 基于实际数据动态生成
const chartOptions = computed((): any => {
  console.log('journeyData.value1', journeyData.value)
  const negativeRates = journeyData.value.map(journey => {
    return {
      name: journey.journeyName,
      value: journey.negativeRate,
      ...journey
    }
  })
  const journeyNames = journeyData.value.map(journey => journey.journeyName)
  // const avgNegativeRate = negativeRates.reduce((sum, rate) => sum + rate, 0) / negativeRates.length
  console.log('negativeRates', negativeRates)
  return {
    grid: {
      left: 0,
      right: 0,
      top: 40,
      bottom: 0,
      containLabel: true
    },
    tooltip: {
      show: true,
      trigger: 'axis',
      axisPointer: {
        type: 'shadow',
        triggerEmphasis: false,
        shadowStyle: {
          color: 'rgba(0, 0, 0, 0)'
        },
        label: {
          show: false
        }
      },
      formatter: (params: any) => {
        return formatChartPop(params, 'negativeRate', true)
      }
    },
    legend: {
      show: false
    },
    xAxis: [
      {
        show: false,
        type: 'category' as const,
        data: journeyNames
      }
    ],
    yAxis: [
      {
        show: false,
        type: 'value' as const,
        name: '负面率',
        nameTextStyle: {
          padding: [0, 0, 0, -40]
        }
      }
    ],
    series: [
      {
        name: '负面率',
        type: 'line' as const,
        data: negativeRates?.map((el: any) => {
          return {
            ...el,
            symbol: `image://${emojiMap[el.emotionType]}`,
            symbolSize: 32
          }
        }),
        smooth: true,
        lineStyle: {
          width: 2,
          type: 'dashed',
          color: '#FAB007'
        },
        label: {
          show: false,
          position: 'top' as const,
          formatter: '{c}%',
          color: '#717680',
          padding: [4, 8]
        }
        // markLine: {
        //   silent: true,
        //   symbol: ['circle', 'none'],
        //   symbolSize: 8,
        //   lineStyle: {
        //     color: '#A8B3C6',
        //     width: 2,
        //     type: 'dashed' as const
        //   },
        //   label: {
        //     show: true,
        //     position: 'start' as const,
        //     formatter: `负面平均值：${avgNegativeRate.toFixed(1)}%`,
        //     color: '#717680',
        //     padding: [4, 8]
        //   },
        //   data: [
        //     [
        //       {
        //         coord: [0, avgNegativeRate],
        //         name: '平均线起点'
        //       },
        //       {
        //         coord: [journeyNames.length - 1, avgNegativeRate],
        //         name: '平均线终点'
        //       }
        //     ]
        //   ]
        // }
      }
    ]
  }
})

console.log('chartOptions', chartOptions.value.series)
</script>

<template>
  <div class="user-journey-analysis">
    <!-- 标题行 -->
    <div class="header-row">
      <div class="stage-label">阶段</div>
      <div v-for="journey in journeyData" :key="journey.journeyCode" class="stage-header">
        <SvgIcon
          name="yhlvbg"
          :color="journey.rateBackgroundColor"
          width="100%"
          height="64px"
        ></SvgIcon>
        <div class="sh-content">
          <div
            class="stage-name"
            @click="handleJourneyClick('name', journey)"
            style="cursor: pointer"
            :style="{ color: journey.rateColor }"
          >
            {{ journey.journeyName }}
          </div>
          <div class="stage-icon ml-16 flex-center">
            <SvgIcon
              :name="stageIconMap[journey.journeyName] || 'yhlc-eye-line'"
              width="24px"
              height="24px"
              color="#666666"
            ></SvgIcon>
          </div>
        </div>
      </div>
    </div>

    <!-- 数据行 -->
    <div class="data-row">
      <div class="row-label">提及量</div>
      <HoverPopover
        v-for="journey in journeyData"
        :key="journey.journeyCode"
        :width="420"
        :table-config="{
          title: journey.journeyName || '',
          data: fmtHoverData(journey, 'mentions'),
          columns: [
            { title: '名称', dataIndex: 'label', width: 70 },
            { title: '数值', dataIndex: 'value', width: 85 },
            { title: '环比', dataIndex: 'rateMoM', className: 'c666' },
            { title: '同比', dataIndex: 'rateYoY', className: 'c666' }
          ]
        }"
      >
        <template #reference>
          <div
            class="stage-data"
            @click="handleJourneyClick('mentions', journey)"
            style="cursor: pointer"
          >
            <div class="data-count">{{ fmtNum(journey.mentions) }}</div>
            <div
              class="rate-count"
              :class="journey.mentionsMoM >= 0 ? 'is-improving' : 'is-worsening'"
            >
              {{ fmtFix(journey.mentionsMoM) }}
            </div>
          </div>
        </template>
      </HoverPopover>
    </div>

    <div class="data-row">
      <div class="row-label">负面率</div>
      <HoverPopover
        v-for="journey in journeyData"
        :key="journey.journeyCode"
        :width="420"
        :table-config="{
          title: journey.journeyName || '',
          data: fmtHoverData(journey, 'negativeRate'),
          columns: [
            { title: '名称', dataIndex: 'label', width: 70 },
            { title: '数值', dataIndex: 'value', width: 85 },
            { title: '环比', dataIndex: 'rateMoM', className: 'c666' },
            { title: '同比', dataIndex: 'rateYoY', className: 'c666' }
          ]
        }"
      >
        <template #reference>
          <div
            class="stage-data"
            @click="handleJourneyClick('negativeRate', journey)"
            style="cursor: pointer"
          >
            <div class="data-count">{{ fmtPer(journey.negativeRate) }}</div>
            <div
              class="rate-count"
              :class="journey.negativeRateMoM <= 0 ? 'is-improving' : 'is-worsening'"
            >
              {{ fmtFix(journey.negativeRateMoM) }}
            </div>
          </div>
        </template>
      </HoverPopover>
    </div>

    <!-- 流程图占位区域 -->
    <div class="flow-chart-area">
      <div class="flow-chart-label">负面率曲线</div>
      <div class="flow-chart">
        <FEcharts
          :options="chartOptions"
          :width="'100%'"
          :height="'100%'"
          @chart-click="handleChartJourneyClick"
        />
      </div>
    </div>

    <!-- 客户画像TOP区域 - 横向布局 -->
    <div class="customer-top-section">
      <div
        class="section-header"
        @click="handleJourneyClick('satisfied', journeyData[0])"
        style="cursor: pointer"
      >
        <div class="section-icon">
          <SvgIcon name="thumb-up-fill" width="24px" height="24px" color="#666666"></SvgIcon>
        </div>
        <div class="section-title">客户满意<br />TOP</div>
      </div>
      <div
        v-for="journey in journeyData"
        :key="`satisfied-${journey.journeyCode}`"
        class="stage-top-data"
        style="cursor: pointer"
      >
        <TopRank
          :data="convertToTopRankData(journey.satisfiedTop5, '正面')"
          @row-click="handleRowClick"
          @view-more="payload => handleTopRankViewMore(payload, journey, 'satisfied')"
        ></TopRank>
      </div>
    </div>

    <!-- 客户不满TOP区域 - 横向布局 -->
    <div class="customer-complaint-section">
      <div
        class="section-header"
        @click="handleJourneyClick('dissatisfied', journeyData[0])"
        style="cursor: pointer"
      >
        <div class="section-icon">
          <SvgIcon name="thumb-down-fill" width="24px" height="24px" color="#666666"></SvgIcon>
        </div>
        <div class="section-title">客户不满<br />TOP</div>
      </div>
      <div
        v-for="journey in journeyData"
        :key="`dissatisfied-${journey.journeyCode}`"
        class="stage-complaint-data"
        style="cursor: pointer"
      >
        <TopRank
          :data="convertToTopRankData(journey.dissatisfiedTop5, '负面')"
          @row-click="handleRowClick"
          @view-more="payload => handleTopRankViewMore(payload, journey, 'dissatisfied')"
        ></TopRank>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.no-data {
  display: flex;
  align-items: center;
  justify-self: center;
  height: 100%;
}
.user-journey-analysis {
  height: 100%;
  width: 100%;
  position: relative;

  // 公共样式 - 居中布局
  %flex-center {
    display: flex;
    align-items: center;
    justify-content: center;
  }

  // 公共样式 - 标签基础样式
  %label-base {
    @extend %flex-center;
    width: 136px;
    background: #f2f4f7;
    border-radius: 8px;
    font-weight: 500;
    color: #1d2129;
    line-height: 24px;
  }

  // 计算固定宽度 - 根据journeyData长度动态调整
  --fixed-width: calc(
    162px + 136px - 75px + v-bind('journeyData.length * 16') * 1px
  ); // 162px(网格第一列) + 136px(section-header) + 动态间距

  // 公共样式 - 网格布局
  %grid-layout {
    display: grid;
    grid-template-columns: 162px repeat(v-bind('journeyData.length'), 1fr);
  }

  // 公共样式 - section-header
  %section-header-base {
    @extend %flex-center;
    flex-direction: column;
    width: 136px;
    min-height: 154px;
    border-radius: 8px;

    .section-icon {
      font-size: 24px;
      margin-bottom: 8px;
    }

    .section-title {
      font-weight: 500;
      font-size: 16px;
      color: #1d2129;
      line-height: 24px;
      text-align: center;
    }
  }

  // 全局分割线 - 从顶部到底部的连续线条
  &::before {
    content: '';
    position: absolute;
    top: 80px;
    bottom: 165px;
    left: 162px;
    width: 1px;
    background: linear-gradient(to bottom, #d9d9d9 0%, #d9d9d9 100%);
    z-index: 10;
  }

  // 为每个阶段列添加分割线
  &::after {
    content: '';
    position: absolute;
    top: 80px;
    bottom: 165px;
    // background: repeating-linear-gradient(
    //   to right,
    //   transparent 0,
    //   transparent calc((100% - var(--fixed-width)) / v-bind('journeyData.length - 1') - 1px),
    //   #d9d9d9 calc((100% - var(--fixed-width)) / v-bind('journeyData.length - 1') - 1px),
    //   transparent calc((100% - var(--fixed-width)) / v-bind('journeyData.length - 1'))
    // );

    left: 162px;
    right: 0;
    z-index: 10;
    pointer-events: none;
  }

  // 标题行
  .header-row {
    @extend %grid-layout;
    margin-bottom: 16px;

    .stage-label {
      @extend %label-base;
      height: 64px;
      font-size: 20px;
    }

    .stage-header {
      @extend %flex-center;
      text-align: center;
      position: relative;
      min-width: 0;

      &::after {
        position: absolute;
        right: 0;
        top: 80px;
        content: '';
        height: 720px;
        width: 1px;
        background: #d9d9d9;
        // background-color: red;
      }

      // &:last-child::after {
      //   display: none;
      // }

      .sh-content {
        position: absolute;
        inset: 0;
        @extend %flex-center;

        .stage-name {
          font-weight: 500;
          font-size: 20px;
          color: #333333;
          line-height: 24px;
        }

        .stage-icon {
          width: 40px;
          height: 40px;
          background: linear-gradient(180deg, rgba(255, 255, 255, 0.5) 0%, #fafeff 100%);
          border-radius: 20px;
        }
      }
    }
  }

  // 数据行
  .data-row {
    @extend %grid-layout;
    margin-bottom: 24px;

    .row-label {
      @extend %label-base;
      height: 48px;
      font-size: 16px;
    }

    .stage-data {
      @extend %flex-center;
      gap: 10px;
      padding: 8px 12px;
      text-align: center;
      height: 48px;
      background: #f2f4f7;
      font-variant-numeric: tabular-nums;

      .data-count {
        font-weight: 500;
        font-size: 20px;
        color: #333333;
        line-height: 24px;
      }

      .rate-count {
        font-weight: 400;
        font-size: 14px;
        line-height: 20px;
        min-width: 48px;
        padding: 2px 7px;
        border-radius: 10px;

        &.is-improving {
          color: #0f8f5b;
          background: #e8f8f1;
        }

        &.is-worsening {
          color: #cf3f4f;
          background: #fff1f0;
        }
      }
    }
  }

  // 流程图区域
  .flow-chart-area {
    margin-bottom: 24px;
    display: grid;
    grid-template-columns: 162px 1fr;

    .flow-chart-label {
      @extend %label-base;
      height: 168px;
      font-size: 16px;
    }

    .flow-chart {
      width: 100%;
      height: 168px;
    }
  }

  // 客户画像TOP区域 - 横向布局
  .customer-top-section {
    @extend %grid-layout;
    align-items: stretch;
    margin-bottom: 16px;

    .section-header {
      @extend %section-header-base;
      background: #b3f2c6;
    }

    .stage-top-data {
      display: flex;
      flex-direction: column;
      min-height: 154px;
      overflow: hidden;
      border-radius: 8px;
      margin: 0 8px;
      background: rgba(179, 242, 198, 0.5);
      padding: 8px;
    }
  }

  // 客户不满TOP区域 - 横向布局
  .customer-complaint-section {
    @extend %grid-layout;
    align-items: stretch;

    .section-header {
      @extend %section-header-base;
      background: #ffd1c9;
    }

    .stage-complaint-data {
      display: flex;
      flex-direction: column;
      min-height: 154px;
      margin: 0 8px;
      background: rgba(255, 209, 201, 0.5);
      border-radius: 8px;
      padding: 8px;
      overflow: hidden;
    }
  }
}
</style>
